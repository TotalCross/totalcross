// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"
#include "skia_image_geometry_internal.h"

#include "include/core/SkSamplingOptions.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <limits>
#include <memory>

using skia_image_backing_internal::NativeImageBackingRecord;
using skia_image_backing_internal::findBacking;
using skia_image_backing_internal::rasterInfo;
using skia_image_backing_internal::registerBacking;

namespace {

static Pixel geometryFillColor(const SkiaImageDrawPlanData* plan, int32 publicColor) {
    if (publicColor == 0) {
        return 0;
    }
    const int32 rgb = publicColor == -1 ? plan->transparentColor : publicColor;
    return static_cast<Pixel>(static_cast<uint32_t>(rgb) | 0xff000000u);
}

static int geometryFrame(int frame, int count) {
    if (count <= 1) {
        return 0;
    }
    if (frame < 0) {
        return count - 1;
    }
    return frame >= count ? 0 : frame;
}

static void geometryTranslate(GeometryTransform* transform, double x, double y) {
    transform->tx += transform->a * x + transform->b * y;
    transform->ty += transform->c * x + transform->d * y;
}

static void geometryCompose(GeometryTransform* transform, double a, double b, double c, double d,
                            double tx, double ty) {
    const double oldA = transform->a;
    const double oldB = transform->b;
    const double oldC = transform->c;
    const double oldD = transform->d;
    const double oldTx = transform->tx;
    const double oldTy = transform->ty;
    transform->a = oldA * a + oldB * c;
    transform->b = oldA * b + oldB * d;
    transform->c = oldC * a + oldD * c;
    transform->d = oldC * b + oldD * d;
    transform->tx = oldA * tx + oldB * ty + oldTx;
    transform->ty = oldC * tx + oldD * ty + oldTy;
}

static bool geometryOperation(const SkiaImageDrawPlanData* plan, int index, int32* operation,
                              const int32** parameters, const int32** dimensions) {
    if (!plan || index < 0 || index >= plan->operationCount || !plan->operations || !plan->parameters
        || !plan->dimensions) {
        return false;
    }
    *operation = plan->operations[index];
    *parameters = plan->parameters + index * 4;
    *dimensions = plan->dimensions + index * 2;
    return true;
}

static void geometrySetRootFrame(GeometryTransform* transform, const SkiaImageDrawPlanData* plan,
                                 int frame) {
    const int count = std::max(1, plan->rootFrameCount);
    const int fullWidth = plan->rootWidthOfAllFrames > 0 ? plan->rootWidthOfAllFrames : plan->rootWidth;
    const int frameWidth = fullWidth / count;
    const int normalized = geometryFrame(frame, count);
    transform->validRoot = SkRect::MakeLTRB(static_cast<float>(normalized * frameWidth), 0,
                                             static_cast<float>((normalized + 1) * frameWidth),
                                             static_cast<float>(plan->rootHeight));
}

static bool compileGeometry(const SkiaImageDrawPlanData* plan, int frameOverride,
                            GeometryTransform* transform) {
    if (!plan || !transform || plan->rootWidth <= 0 || plan->rootHeight <= 0
        || plan->rootLogicalWidth <= 0 || plan->rootLogicalHeight <= 0
        || !std::isfinite(plan->rootContentScale) || plan->rootContentScale <= 0
        || !std::isfinite(plan->rootHwScaleW) || plan->rootHwScaleW <= 0
        || !std::isfinite(plan->rootHwScaleH) || plan->rootHwScaleH <= 0
        || plan->operationCount <= 0 || plan->outputWidth <= 0 || plan->outputHeight <= 0) {
        return false;
    }

    transform->a = plan->rootContentScale / plan->rootHwScaleW;
    transform->b = 0;
    transform->c = 0;
    transform->d = plan->rootContentScale / plan->rootHwScaleH;
    transform->tx = 0;
    transform->ty = 0;
    transform->width = plan->rootLogicalWidth;
    transform->height = plan->rootLogicalHeight;
    transform->validRoot = SkRect::MakeWH(static_cast<float>(plan->rootWidthOfAllFrames > 0
        ? plan->rootWidthOfAllFrames : plan->rootWidth), static_cast<float>(plan->rootHeight));
    transform->smooth = false;
    transform->hasFill = false;
    transform->fillColor = 0;

    bool hasExplicitFrame = false;
    for (int i = 0; i < plan->operationCount; ++i) {
        if (plan->operations[i] == 11) {
            hasExplicitFrame = true;
            break;
        }
    }
    const int requestedFrame = frameOverride >= 0 ? frameOverride : plan->currentFrame;
    if (plan->rootFrameCount > 1 && !hasExplicitFrame) {
        const int frame = geometryFrame(requestedFrame, plan->rootFrameCount);
        geometryTranslate(transform, frame * transform->width, 0);
        geometrySetRootFrame(transform, plan, frame);
    }

    for (int i = 0; i < plan->operationCount; ++i) {
        int32 operation;
        const int32* parameters;
        const int32* dimensions;
        if (!geometryOperation(plan, i, &operation, &parameters, &dimensions)) {
            return false;
        }
        const double oldWidth = transform->width;
        const double oldHeight = transform->height;
        const double outputWidth = dimensions[0];
        const double outputHeight = dimensions[1];
        if (outputWidth <= 0 || outputHeight <= 0) {
            return false;
        }
        switch (operation) {
        case SKIA_IMAGE_DRAW_FRAME_SELECT: {
            const int count = std::max(1, plan->rootFrameCount > 1 ? plan->rootFrameCount : 1);
            const int frame = geometryFrame(parameters[0], count);
            geometryTranslate(transform, frame * oldWidth, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            if (plan->rootFrameCount > 1) {
                geometrySetRootFrame(transform, plan, frame);
            }
            break;
        }
        case SKIA_IMAGE_DRAW_FRAME_LAYOUT: {
            const int count = std::max(1, parameters[0]);
            const int frame = geometryFrame(requestedFrame, count);
            geometryTranslate(transform, frame * outputWidth, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            if (plan->rootFrameCount == 1) {
                const int fullWidth = plan->rootWidthOfAllFrames > 0 ? plan->rootWidthOfAllFrames : plan->rootWidth;
                const int frameWidth = fullWidth / count;
                transform->validRoot = SkRect::MakeLTRB(static_cast<float>(frame * frameWidth), 0,
                                                         static_cast<float>((frame + 1) * frameWidth),
                                                         static_cast<float>(plan->rootHeight));
            }
            break;
        }
        case SKIA_IMAGE_DRAW_CROP:
            geometryTranslate(transform, parameters[0], parameters[1]);
            transform->width = outputWidth;
            transform->height = outputHeight;
            break;
        case SKIA_IMAGE_DRAW_SCALE:
            geometryCompose(transform, oldWidth / outputWidth, 0, 0, oldHeight / outputHeight, 0, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            break;
        case SKIA_IMAGE_DRAW_SMOOTH_SCALE:
            geometryCompose(transform, oldWidth / outputWidth, 0, 0, oldHeight / outputHeight, 0, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            transform->smooth = true;
            break;
        case SKIA_IMAGE_DRAW_ROTATE_SCALE: {
            int scale = parameters[0] <= 0 ? 1 : parameters[0];
            int angle = parameters[1] % 360;
            int rawSine = 0;
            int rawCosine = 0;
            int sine = 0;
            int cosine = 0;
            if ((angle % 90) == 0) {
                if (angle < 0) {
                    angle += 360;
                }
                switch (angle) {
                case 0:
                    rawCosine = 0x10000;
                    cosine = 0x640000 / scale;
                    break;
                case 90:
                    rawSine = 0x10000;
                    sine = 0x640000 / scale;
                    break;
                case 180:
                    rawCosine = -0x10000;
                    cosine = -0x640000 / scale;
                    break;
                default:
                    rawSine = -0x10000;
                    sine = -0x640000 / scale;
                    break;
                }
            } else {
                const double radians = angle * 0.0174532925;
                rawSine = static_cast<int>(std::sin(radians) * 0x10000);
                rawCosine = static_cast<int>(std::cos(radians) * 0x10000);
                sine = (rawSine * 100) / scale;
                cosine = (rawCosine * 100) / scale;
            }
            int xMin = 0;
            int yMin = 0;
            int xMax = 0;
            int yMax = 0;
            const int cornersX[3] = {
                (static_cast<int>(oldWidth) * rawCosine) >> 16,
                ((static_cast<int>(oldWidth) * rawCosine) >> 16)
                    + ((-static_cast<int>(oldHeight) * rawSine) >> 16),
                (-static_cast<int>(oldHeight) * rawSine) >> 16
            };
            const int cornersY[3] = {
                (static_cast<int>(oldWidth) * rawSine) >> 16,
                ((static_cast<int>(oldWidth) * rawSine) >> 16)
                    + ((static_cast<int>(oldHeight) * rawCosine) >> 16),
                (static_cast<int>(oldHeight) * rawCosine) >> 16
            };
            for (int corner = 2; corner >= 0; --corner) {
                if (cornersX[corner] < xMin) {
                    xMin = cornersX[corner];
                } else if (cornersX[corner] > xMax) {
                    xMax = cornersX[corner];
                }
                if (cornersY[corner] < yMin) {
                    yMin = cornersY[corner];
                } else if (cornersY[corner] > yMax) {
                    yMax = cornersY[corner];
                }
            }
            if (oldWidth == oldHeight) {
                xMax = yMax = static_cast<int>(oldWidth);
                xMin = yMin = 0;
            }
            const int spanX = xMax - xMin;
            const int spanY = yMax - yMin;
            const int64_t x0 = ((static_cast<int64_t>(oldWidth) << 16)
                - (static_cast<int64_t>(spanX) * rawCosine - static_cast<int64_t>(spanY) * rawSine) - 1) / 2;
            const int64_t y0 = ((static_cast<int64_t>(oldHeight) << 16)
                - (static_cast<int64_t>(spanX) * rawSine + static_cast<int64_t>(spanY) * rawCosine) - 1) / 2;
            geometryCompose(transform, static_cast<double>(cosine) / 65536.0,
                -static_cast<double>(sine) / 65536.0, static_cast<double>(sine) / 65536.0,
                static_cast<double>(cosine) / 65536.0, static_cast<double>(x0) / 65536.0,
                static_cast<double>(y0) / 65536.0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            transform->hasFill = true;
            transform->fillColor = geometryFillColor(plan, parameters[2]);
            break;
        }
        case SKIA_IMAGE_DRAW_TOUCH_UP:
        case SKIA_IMAGE_DRAW_FADE:
        case SKIA_IMAGE_DRAW_ALPHA:
        case SKIA_IMAGE_DRAW_APPLY_COLOR:
        case SKIA_IMAGE_DRAW_APPLY_FADE:
            break;
        default:
            return false;
        }
    }
    return std::abs(transform->width - plan->outputWidth) < 0.001
        && std::abs(transform->height - plan->outputHeight) < 0.001;
}

static bool geometryDrawCompiled(SkCanvas* canvas, const SkImage* image,
                                 const GeometryTransform& transform, float srcLeft, float srcTop,
                                 float srcRight, float srcBottom, float dstLeft, float dstTop,
                                 float dstRight, float dstBottom, int32 alphaMask,
                                 bool applyPixelCenterOffset,
                                 const SkiaImageDrawColorFilters* colorFilters) {
    if (!canvas || !image || alphaMask < 0 || alphaMask > 255 || srcRight <= srcLeft
        || srcBottom <= srcTop || dstRight <= dstLeft || dstBottom <= dstTop) {
        return false;
    }
    const double scaleX = (dstRight - dstLeft) / (srcRight - srcLeft);
    const double scaleY = (dstBottom - dstTop) / (srcBottom - srcTop);
    if (!std::isfinite(scaleX) || !std::isfinite(scaleY) || scaleX <= 0 || scaleY <= 0) {
        return false;
    }
    SkMatrix rootToCanvas;
    const double planTx = srcLeft - dstLeft / scaleX;
    const double planTy = srcTop - dstTop / scaleY;
    const SkMatrix canvasToRoot = SkMatrix::MakeAll(
        static_cast<float>(transform.a / scaleX), static_cast<float>(transform.b / scaleY),
        static_cast<float>(transform.a * planTx + transform.b * planTy + transform.tx),
        static_cast<float>(transform.c / scaleX), static_cast<float>(transform.d / scaleY),
        static_cast<float>(transform.c * planTx + transform.d * planTy + transform.ty),
        0, 0, 1);
    if (!canvasToRoot.invert(&rootToCanvas)) {
        return false;
    }

    const SkRect destination = SkRect::MakeLTRB(dstLeft, dstTop, dstRight, dstBottom);
    canvas->save();
    if (transform.hasFill) {
        SkPaint fill;
        const SkColor color = skiaColorFromPixel(transform.fillColor);
        const int32 fillAlpha = (SkColorGetA(color) * alphaMask + 127) / 255;
        fill.setColor(color);
        fill.setAlpha(fillAlpha);
        if (colorFilters) {
            fill.setColorFilter(colorFilters->fill);
        }
        canvas->drawRect(destination, fill);
    }

    canvas->clipRect(destination, SkClipOp::kIntersect, false);
    canvas->concat(rootToCanvas);
    SkPath validPath;
    validPath.addRect(transform.validRoot);
    canvas->clipPath(validPath, SkClipOp::kIntersect, false);
    const SkSamplingOptions sampling = transform.smooth
        ? SkSamplingOptions(SkCubicResampler{0.0f, 0.5f})
        : SkSamplingOptions(SkFilterOptions{SkSamplingMode::kNearest, SkMipmapMode::kNone});
    // At unit output scale, align shader sampling with the legacy nearest-neighbor
    // pixel centers. Scaled backing surfaces already provide that alignment.
    const SkMatrix shaderMatrix = SkMatrix::Translate(0.5f, 0.5f);
    SkPaint paint;
    paint.setAlpha(alphaMask);
    if (colorFilters) {
        paint.setColorFilter(colorFilters->content);
    }
    paint.setShader(image->makeShader(SkTileMode::kClamp, SkTileMode::kClamp, sampling,
        applyPixelCenterOffset ? &shaderMatrix : nullptr));
    canvas->drawRect(SkRect::MakeWH(image->width(), image->height()), paint);
    canvas->restore();
    return true;
}

struct RasterPhysicalPlan {
    GeometryTransform transform;
    SkRect sourcePixels;
    SkRect destinationPixels;
};

static SkPoint mapPoint(const SkMatrix& matrix, float x, float y) {
    SkPoint point = SkPoint::Make(x, y);
    matrix.mapPoints(&point, 1);
    return point;
}

static bool exactValue(float value, double expected) {
    return std::isfinite(value) && value == static_cast<float>(expected);
}

static bool integerValue(float value, int32* result) {
    if (!std::isfinite(value)) {
        return false;
    }
    const double rounded = std::round(static_cast<double>(value));
    if (value != static_cast<float>(rounded)
        || rounded < std::numeric_limits<int32>::min()
        || rounded > std::numeric_limits<int32>::max()) {
        return false;
    }
    *result = static_cast<int32>(rounded);
    return true;
}

static bool purePhysicalGeometry(const SkiaImageDrawPlanData* plan) {
    if (!plan || plan->operationCount <= 0 || !plan->operations) {
        return false;
    }
    for (int i = 0; i < plan->operationCount; ++i) {
        switch (plan->operations[i]) {
        case SKIA_IMAGE_DRAW_SCALE:
        case SKIA_IMAGE_DRAW_SMOOTH_SCALE:
        case SKIA_IMAGE_DRAW_FRAME_SELECT:
        case SKIA_IMAGE_DRAW_CROP:
        case SKIA_IMAGE_DRAW_FRAME_LAYOUT:
            break;
        default:
            return false;
        }
    }
    return true;
}

static bool isTrivialWritePixelsPlan(const SkiaImageDrawPlanData* plan);

static bool buildRasterPhysicalPlan(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                                    NativeImageBackingRecord* source, float srcLeft, float srcTop,
                                    float srcRight, float srcBottom, float dstLeft, float dstTop,
                                    float dstRight, float dstBottom, RasterPhysicalPlan* result) {
    if (!plan || !canvas || !source || !result || !purePhysicalGeometry(plan)
        || plan->alphaMask != 255 || plan->materializeAlphaMask != 255
        || plan->outputAlphaMask != 255 || canvas->getSaveCount() != 1) {
        return false;
    }

    SkPixmap targetPixels;
    if (!canvas->peekPixels(&targetPixels) || targetPixels.width() <= 0 || targetPixels.height() <= 0) {
        return false;
    }
    SkIRect deviceClip;
    if (!canvas->getDeviceClipBounds(&deviceClip)
        || deviceClip != SkIRect::MakeWH(targetPixels.width(), targetPixels.height())) {
        return false;
    }

    GeometryTransform transform;
    if (!compileGeometry(plan, -1, &transform)) {
        return false;
    }
    const double scaleX = (dstRight - dstLeft) / (srcRight - srcLeft);
    const double scaleY = (dstBottom - dstTop) / (srcBottom - srcTop);
    if (!std::isfinite(scaleX) || !std::isfinite(scaleY) || scaleX <= 0 || scaleY <= 0) {
        return false;
    }
    const double planTx = srcLeft - dstLeft / scaleX;
    const double planTy = srcTop - dstTop / scaleY;
    const SkMatrix canvasToRoot = SkMatrix::MakeAll(
        static_cast<float>(transform.a / scaleX), static_cast<float>(transform.b / scaleY),
        static_cast<float>(transform.a * planTx + transform.b * planTy + transform.tx),
        static_cast<float>(transform.c / scaleX), static_cast<float>(transform.d / scaleY),
        static_cast<float>(transform.c * planTx + transform.d * planTy + transform.ty),
        0, 0, 1);
    SkMatrix rootToCanvas;
    if (!canvasToRoot.invert(&rootToCanvas)) {
        return false;
    }
    const SkMatrix canvasMatrix = canvas->getTotalMatrix();
    if (canvasMatrix.hasPerspective()) {
        return false;
    }

    const SkPoint rootOriginInCanvas = mapPoint(rootToCanvas, 0, 0);
    const SkPoint rootXInCanvas = mapPoint(rootToCanvas, 1, 0);
    const SkPoint rootYInCanvas = mapPoint(rootToCanvas, 0, 1);
    const SkPoint rootOrigin = mapPoint(canvasMatrix, rootOriginInCanvas.fX, rootOriginInCanvas.fY);
    const SkPoint rootX = mapPoint(canvasMatrix, rootXInCanvas.fX, rootXInCanvas.fY);
    const SkPoint rootY = mapPoint(canvasMatrix, rootYInCanvas.fX, rootYInCanvas.fY);
    if (!exactValue(rootX.fX - rootOrigin.fX, 1) || !exactValue(rootX.fY - rootOrigin.fY, 0)
        || !exactValue(rootY.fX - rootOrigin.fX, 0) || !exactValue(rootY.fY - rootOrigin.fY, 1)) {
        return false;
    }

    const SkPoint destinationTopLeft = mapPoint(canvasMatrix, dstLeft, dstTop);
    const SkPoint destinationTopRight = mapPoint(canvasMatrix, dstRight, dstTop);
    const SkPoint destinationBottomLeft = mapPoint(canvasMatrix, dstLeft, dstBottom);
    const SkPoint destinationBottomRight = mapPoint(canvasMatrix, dstRight, dstBottom);
    if (!exactValue(destinationTopRight.fY, destinationTopLeft.fY)
        || !exactValue(destinationBottomLeft.fX, destinationTopLeft.fX)
        || !exactValue(destinationBottomRight.fX, destinationTopRight.fX)
        || !exactValue(destinationBottomRight.fY, destinationBottomLeft.fY)
        || destinationTopRight.fX <= destinationTopLeft.fX
        || destinationBottomLeft.fY <= destinationTopLeft.fY) {
        return false;
    }

    int32 destinationLeft;
    int32 destinationTop;
    int32 destinationRight;
    int32 destinationBottom;
    int32 sourceLeft;
    int32 sourceTop;
    int32 sourceRight;
    int32 sourceBottom;
    if (!integerValue(destinationTopLeft.fX, &destinationLeft)
        || !integerValue(destinationTopLeft.fY, &destinationTop)
        || !integerValue(destinationTopRight.fX, &destinationRight)
        || !integerValue(destinationBottomLeft.fY, &destinationBottom)
        || !integerValue(destinationLeft - rootOrigin.fX, &sourceLeft)
        || !integerValue(destinationTop - rootOrigin.fY, &sourceTop)
        || !integerValue(destinationRight - rootOrigin.fX, &sourceRight)
        || !integerValue(destinationBottom - rootOrigin.fY, &sourceBottom)
        || sourceRight <= sourceLeft || sourceBottom <= sourceTop
        || destinationRight - destinationLeft != sourceRight - sourceLeft
        || destinationBottom - destinationTop != sourceBottom - sourceTop) {
        return false;
    }
    if (destinationLeft < 0 || destinationTop < 0
        || destinationRight > targetPixels.width() || destinationBottom > targetPixels.height()
        || sourceLeft < 0 || sourceTop < 0 || sourceRight > source->width || sourceBottom > source->height) {
        return false;
    }

    const SkRect validRoot = transform.validRoot;
    int32 validRootLeft;
    int32 validRootTop;
    int32 validRootRight;
    int32 validRootBottom;
    if (!integerValue(validRoot.fLeft, &validRootLeft)
        || !integerValue(validRoot.fTop, &validRootTop)
        || !integerValue(validRoot.fRight, &validRootRight)
        || !integerValue(validRoot.fBottom, &validRootBottom)
        || sourceLeft < validRootLeft || sourceTop < validRootTop
        || sourceRight > validRootRight || sourceBottom > validRootBottom) {
        return false;
    }

    if (transform.b != 0.0 || transform.c != 0.0) {
        return false;
    }
    result->transform = transform;
    result->sourcePixels = SkRect::MakeLTRB(static_cast<float>(sourceLeft), static_cast<float>(sourceTop),
                                             static_cast<float>(sourceRight), static_cast<float>(sourceBottom));
    result->destinationPixels = SkRect::MakeLTRB(static_cast<float>(destinationLeft),
                                                  static_cast<float>(destinationTop),
                                                  static_cast<float>(destinationRight),
                                                  static_cast<float>(destinationBottom));
    return true;
}

static bool isTrivialWritePixelsPlan(const SkiaImageDrawPlanData* plan) {
    if (!plan || plan->rootFrameCount != 1 || plan->outputFrameCount != 1
        || plan->rootWidthOfAllFrames > 0 && plan->rootWidthOfAllFrames != plan->rootWidth
        || plan->outputWidthOfAllFrames > 0 && plan->outputWidthOfAllFrames != plan->outputWidth
        || plan->rootWidth != plan->outputWidth || plan->rootHeight != plan->outputHeight
        || plan->rootContentScale != 1.0 || plan->outputContentScale != 1.0
        || plan->destinationScale != 1.0 || plan->hwScaleW != 1.0 || plan->hwScaleH != 1.0
        || plan->rootHwScaleW != 1.0 || plan->rootHwScaleH != 1.0
        || plan->alphaMask != 255 || plan->materializeAlphaMask != 255
        || plan->outputAlphaMask != 255 || plan->operationCount <= 0) {
        return false;
    }
    for (int i = 0; i < plan->operationCount; ++i) {
        if (plan->operations[i] != SKIA_IMAGE_DRAW_CROP
            || plan->parameters[i * 4] != 0 || plan->parameters[i * 4 + 1] != 0
            || plan->parameters[i * 4 + 2] != plan->rootLogicalWidth
            || plan->parameters[i * 4 + 3] != plan->rootLogicalHeight
            || plan->dimensions[i * 2] != plan->rootWidth
            || plan->dimensions[i * 2 + 1] != plan->rootHeight) {
            return false;
        }
    }
    return true;
}

static bool geometryDraw(const SkiaImageDrawPlanData* plan, SkCanvas* canvas, float srcLeft,
                         float srcTop, float srcRight, float srcBottom, float dstLeft, float dstTop,
                         float dstRight, float dstBottom, int frameOverride) {
    NativeImageBackingRecord* source = plan ? findBacking(plan->rootHandle) : nullptr;
    if (!source || !canvas) {
        return false;
    }
#if TC_GRAPHICS_SOFTWARE
    constexpr int32 kPhysicalIdentityFoldingBit = 1 << 15;
    if (plan->optimizationMask & kPhysicalIdentityFoldingBit) {
        skia_image_backing_record_physical_identity_attempt_for_test();
        RasterPhysicalPlan physicalPlan;
        if (buildRasterPhysicalPlan(plan, canvas, source, srcLeft, srcTop, srcRight, srcBottom,
                                    dstLeft, dstTop, dstRight, dstBottom, &physicalPlan)) {
            if (isTrivialWritePixelsPlan(plan)
                && skia_image_backing_try_write_pixels(canvas, plan->rootHandle, srcLeft, srcTop,
                    srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, plan->alphaMask,
                    plan->optimizationMask)) {
                skia_image_backing_record_physical_identity_hit_for_test();
                skia_image_backing_record_physical_identity_resample_avoided_for_test();
                return true;
            }
            try {
                sk_sp<SkImage> image = source->snapshot();
                if (image) {
                    GeometryTransform identityTransform = physicalPlan.transform;
                    identityTransform.smooth = false;
                    identityTransform.validRoot = physicalPlan.sourcePixels;
                    SkiaImageDrawColorFilters colorFilters;
                    if (geometryDrawCompiled(canvas, image.get(), identityTransform, srcLeft, srcTop,
                                             srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom,
                                             plan->alphaMask, false, &colorFilters)) {
                        skia_image_backing_record_physical_identity_hit_for_test();
                        skia_image_backing_record_physical_identity_resample_avoided_for_test();
                        return true;
                    }
                }
            } catch (const std::bad_alloc&) {
            }
        }
        skia_image_backing_record_physical_identity_fallback_for_test();
    }
#endif
    if (isTrivialWritePixelsPlan(plan)
        && skia_image_backing_try_write_pixels(canvas, plan->rootHandle, srcLeft, srcTop,
            srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, plan->alphaMask,
            plan->optimizationMask)) {
        return true;
    }
    try {
        sk_sp<SkImage> image = source->snapshot();
        SkiaImageDrawColorFilters colorFilters;
        GeometryTransform transform;
        if (!image || !skia_image_draw_color_filters(plan, &colorFilters)
            || !compileGeometry(plan, frameOverride, &transform)) {
            return false;
        }
        return geometryDrawCompiled(canvas, image.get(), transform, srcLeft, srcTop, srcRight, srcBottom,
                                    dstLeft, dstTop, dstRight, dstBottom, plan->alphaMask,
                                    std::abs(plan->outputContentScale - 1.0) < 0.000001,
                                    &colorFilters);
    } catch (const std::bad_alloc&) {
        return false;
    }
}

}

bool skia_image_geometry_compile(const SkiaImageDrawPlanData* plan, int frameOverride,
                                 GeometryTransform* transform) {
    return compileGeometry(plan, frameOverride, transform);
}

bool skia_image_geometry_draw_compiled(SkCanvas* canvas, const SkImage* image,
                                       const GeometryTransform& transform, float srcLeft,
                                       float srcTop, float srcRight, float srcBottom, float dstLeft,
                                       float dstTop, float dstRight, float dstBottom, int32 alphaMask,
                                       bool applyPixelCenterOffset,
                                       const SkiaImageDrawColorFilters* colorFilters) {
    return geometryDrawCompiled(canvas, image, transform, srcLeft, srcTop, srcRight, srcBottom,
                                dstLeft, dstTop, dstRight, dstBottom, alphaMask,
                                applyPixelCenterOffset, colorFilters);
}

int skia_image_backing_draw_geometry_to_surface(int32 targetSurface,
    const SkiaImageDrawPlanData* plan, float srcLeft, float srcTop, float srcRight,
    float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom) {
    const int result = geometryDraw(plan, skiaGetCanvas(targetSurface), srcLeft, srcTop, srcRight,
                                    srcBottom, dstLeft, dstTop, dstRight, dstBottom, -1) ? 1 : 0;
    if (result != 0) {
        skia_image_backing_mark_surface_mutated(targetSurface);
    }
    return result;
}
