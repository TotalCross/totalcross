// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"
#include "skia_image_geometry_internal.h"

#include "include/core/SkPixmap.h"
#include "include/core/SkSamplingOptions.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <limits>
#include <memory>
#include <vector>

using skia_image_backing_internal::NativeImageBackingRecord;
using skia_image_backing_internal::RASTER_VARIANT_HIT;
using skia_image_backing_internal::RASTER_VARIANT_INVALID;
using skia_image_backing_internal::RASTER_VARIANT_MATERIALIZE;
using skia_image_backing_internal::RASTER_VARIANT_MISS;
using skia_image_backing_internal::RASTER_VARIANT_PHYSICAL;
using skia_image_backing_internal::RASTER_VARIANT_TARGET_COLOR;
using skia_image_backing_internal::findBacking;
using skia_image_backing_internal::rasterInfo;
using skia_image_backing_internal::registerBacking;
using skia_image_backing_internal::rasterVariantFail;
using skia_image_backing_internal::rasterVariantObserve;
using skia_image_backing_internal::rasterVariantStore;

namespace {

static bool failNextPhysicalCopyWritePixelsForTest;

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

static bool geometryDraw(const SkiaImageDrawPlanData* plan, SkCanvas* canvas, float srcLeft,
                         float srcTop, float srcRight, float srcBottom, float dstLeft, float dstTop,
                         float dstRight, float dstBottom, int frameOverride) {
    NativeImageBackingRecord* source = plan ? findBacking(plan->rootHandle) : nullptr;
    if (!source || !canvas) {
        return false;
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

static bool integerCoordinate(double value) {
    return std::isfinite(value) && std::floor(value) == value;
}

struct PhysicalIdentityMapping {
    NativeImageBackingRecord* source;
    GeometryTransform transform;
    SkMatrix matrix;
    SkRect sourceRect;
    SkRect destinationRect;
    SkRect deviceDestination;
};

static bool physicalIdentityMapping(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                                    float srcLeft, float srcTop, float srcRight, float srcBottom,
                                    float dstLeft, float dstTop, float dstRight, float dstBottom,
                                    bool allowSmooth, PhysicalIdentityMapping* mapping) {
    if (!plan || !canvas || !mapping || !plan->sourceBackingStable
        || plan->sourceMutationGeneration != plan->backingMutationGeneration) {
        return false;
    }
    NativeImageBackingRecord* source = findBacking(plan->rootHandle);
    if (!source || source->width <= 0 || source->height <= 0) {
        return false;
    }

    GeometryTransform transform;
    if (!compileGeometry(plan, -1, &transform) || transform.hasFill
        || (!allowSmooth && transform.smooth)
        || (allowSmooth && transform.smooth
            && std::abs(plan->outputContentScale - 1.0) < 0.000001)
        || !std::isfinite(transform.a) || !std::isfinite(transform.d)
        || transform.a <= 0 || transform.d <= 0 || transform.b != 0.0 || transform.c != 0.0
        || !integerCoordinate(transform.tx) || !integerCoordinate(transform.ty)) {
        return false;
    }

    const SkMatrix& matrix = canvas->getTotalMatrix();
    if (matrix.hasPerspective() || !std::isfinite(matrix.getScaleX()) || !std::isfinite(matrix.getScaleY())
        || matrix.getSkewX() != 0 || matrix.getSkewY() != 0
        || matrix.getScaleX() <= 0 || matrix.getScaleY() <= 0
        || transform.a != matrix.getScaleX() || transform.d != matrix.getScaleY()) {
        return false;
    }
    const double sourceLeft = transform.a * srcLeft + transform.tx;
    const double sourceTop = transform.d * srcTop + transform.ty;
    const double sourceRight = transform.a * srcRight + transform.tx;
    const double sourceBottom = transform.d * srcBottom + transform.ty;
    SkRect sourceRect = SkRect::MakeLTRB(static_cast<float>(sourceLeft), static_cast<float>(sourceTop),
                                         static_cast<float>(sourceRight), static_cast<float>(sourceBottom));
    SkRect destinationRect = SkRect::MakeLTRB(dstLeft, dstTop, dstRight, dstBottom);
    SkRect deviceDestination;
    matrix.mapRect(&deviceDestination, destinationRect);
    if (sourceRight <= sourceLeft || sourceBottom <= sourceTop
        || sourceLeft < 0 || sourceTop < 0 || sourceRight > source->width || sourceBottom > source->height
        || !transform.validRoot.contains(sourceRect)
        || !integerCoordinate(sourceLeft) || !integerCoordinate(sourceTop)
        || !integerCoordinate(sourceRight) || !integerCoordinate(sourceBottom)
        || !integerCoordinate(deviceDestination.left()) || !integerCoordinate(deviceDestination.top())
        || !integerCoordinate(deviceDestination.right()) || !integerCoordinate(deviceDestination.bottom())
        || deviceDestination.width() != sourceRight - sourceLeft
        || deviceDestination.height() != sourceBottom - sourceTop) {
        return false;
    }

    mapping->source = source;
    mapping->transform = transform;
    mapping->matrix = matrix;
    mapping->sourceRect = sourceRect;
    mapping->destinationRect = destinationRect;
    mapping->deviceDestination = deviceDestination;
    return true;
}

static bool physicalIdentityDraw(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                                 float srcLeft, float srcTop, float srcRight, float srcBottom,
                                 float dstLeft, float dstTop, float dstRight, float dstBottom) {
    PhysicalIdentityMapping mapping;
    if (!physicalIdentityMapping(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, false, &mapping)) {
        return false;
    }
    NativeImageBackingRecord* source = mapping.source;

    sk_sp<SkImage> image;
    SkiaImageDrawColorFilters colorFilters;
    try {
        image = source->snapshot();
        if (!image || !skia_image_draw_color_filters(plan, &colorFilters)) {
            return false;
        }
    } catch (const std::bad_alloc&) {
        return false;
    }
    const SkImageInfo targetInfo = canvas->imageInfo();
    if (image->colorType() == kUnknown_SkColorType || targetInfo.colorType() == kUnknown_SkColorType
        || image->colorType() != targetInfo.colorType()
        || image->alphaType() == kUnknown_SkAlphaType || targetInfo.alphaType() == kUnknown_SkAlphaType) {
        return false;
    }

    SkPaint paint;
    paint.setAlpha(plan->alphaMask);
    paint.setFilterQuality(kNone_SkFilterQuality);
    if (colorFilters.content) {
        paint.setColorFilter(colorFilters.content);
    }
    canvas->drawImageRect(image.get(), mapping.sourceRect, mapping.destinationRect, &paint,
                          SkCanvas::kStrict_SrcRectConstraint);
    return true;
}

static bool writePhysicalCopyPixels(SkCanvas* canvas, const SkPixmap& source,
                                    int x, int y) {
    if (failNextPhysicalCopyWritePixelsForTest) {
        failNextPhysicalCopyWritePixelsForTest = false;
        return false;
    }
    // SkSurface::writePixels returns void in the pinned Skia API; SkCanvas reports success.
    return canvas->writePixels(source.info(), source.addr(), source.rowBytes(), x, y);
}

static bool knownDirectCopyColorType(SkColorType type) {
    return type == kRGBA_8888_SkColorType || type == kBGRA_8888_SkColorType
        || type == kRGB_565_SkColorType;
}

static bool knownDirectCopyAlphaType(SkAlphaType type) {
    return type == kPremul_SkAlphaType || type == kUnpremul_SkAlphaType
        || type == kOpaque_SkAlphaType;
}

static bool memoryRangesOverlap(const void* first, size_t firstSize,
                                const void* second, size_t secondSize) {
    if (!first || !second) {
        return true;
    }
    const uintptr_t firstStart = reinterpret_cast<uintptr_t>(first);
    const uintptr_t secondStart = reinterpret_cast<uintptr_t>(second);
    const uintptr_t maxAddress = std::numeric_limits<uintptr_t>::max();
    if (firstSize > maxAddress - firstStart || secondSize > maxAddress - secondStart) {
        return true;
    }
    return firstStart < secondStart + secondSize && secondStart < firstStart + firstSize;
}

static bool physicalIdentityCopyDraw(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                                     float srcLeft, float srcTop, float srcRight, float srcBottom,
                                     float dstLeft, float dstTop, float dstRight, float dstBottom) {
    PhysicalIdentityMapping mapping;
    if (!physicalIdentityMapping(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, true, &mapping)
        || plan->alphaMask != 255 || plan->outputAlphaMask != 255
        || plan->materializeAlphaMask != 255 || plan->sourceOpacityState != 1) {
        return false;
    }

    SkiaImageDrawColorFilters colorFilters;
    if (!skia_image_draw_color_filters(plan, &colorFilters)
        || colorFilters.content || colorFilters.fill) {
        return false;
    }

    SkSurface* target = canvas->getSurface();
    if (!target || target->recordingContext() || canvas->getSaveCount() != 2
        || !canvas->isClipRect()) {
        return false;
    }
    SkIRect deviceClip;
    if (!canvas->getDeviceClipBounds(&deviceClip)) {
        return false;
    }
    const SkImageInfo targetInfo = target->imageInfo();
    if (mapping.deviceDestination.left() < 0 || mapping.deviceDestination.top() < 0
        || mapping.deviceDestination.right() > targetInfo.width()
        || mapping.deviceDestination.bottom() > targetInfo.height()) {
        return false;
    }
    const SkIRect deviceRect = SkIRect::MakeLTRB(
        static_cast<int>(mapping.deviceDestination.left()),
        static_cast<int>(mapping.deviceDestination.top()),
        static_cast<int>(mapping.deviceDestination.right()),
        static_cast<int>(mapping.deviceDestination.bottom()));
    if (!deviceClip.contains(deviceRect) || deviceRect.isEmpty()
        || deviceRect.right() <= deviceRect.left() || deviceRect.bottom() <= deviceRect.top()) {
        return false;
    }

    SkPixmap sourcePixels;
    SkPixmap targetPixels;
    if (mapping.source->image) {
        if (!mapping.source->image->peekPixels(&sourcePixels)) {
            return false;
        }
    } else if (!mapping.source->surface || !mapping.source->surface->peekPixels(&sourcePixels)) {
        return false;
    }
    if (!target->peekPixels(&targetPixels)) {
        return false;
    }

    const SkImageInfo sourceInfo = sourcePixels.info();
    const SkImageInfo targetPixelInfo = targetPixels.info();
    const int sourceX = static_cast<int>(mapping.sourceRect.left());
    const int sourceY = static_cast<int>(mapping.sourceRect.top());
    const int copyWidth = static_cast<int>(mapping.sourceRect.width());
    const int copyHeight = static_cast<int>(mapping.sourceRect.height());
    const size_t bytesPerPixel = sourceInfo.bytesPerPixel();
    const size_t maxSize = std::numeric_limits<size_t>::max();
    if (sourceInfo.width() <= 0 || sourceInfo.height() <= 0
        || targetPixelInfo.width() <= 0 || targetPixelInfo.height() <= 0
        || bytesPerPixel == 0
        || static_cast<size_t>(sourceInfo.width()) > maxSize / bytesPerPixel
        || static_cast<size_t>(targetPixelInfo.width()) > maxSize / bytesPerPixel) {
        return false;
    }
    const size_t minimumSourceRowBytes = static_cast<size_t>(sourceInfo.width()) * bytesPerPixel;
    const size_t minimumTargetRowBytes = static_cast<size_t>(targetPixelInfo.width()) * bytesPerPixel;
    if (sourceInfo.width() != mapping.source->width || sourceInfo.height() != mapping.source->height
        || targetPixelInfo.width() != targetInfo.width() || targetPixelInfo.height() != targetInfo.height()
        || targetPixelInfo.colorType() != targetInfo.colorType()
        || targetPixelInfo.alphaType() != targetInfo.alphaType()
        || targetPixelInfo.colorSpace() != targetInfo.colorSpace()
        || sourceInfo.colorType() != targetPixelInfo.colorType()
        || sourceInfo.alphaType() != targetPixelInfo.alphaType()
        || sourceInfo.colorSpace() != targetPixelInfo.colorSpace()
        || !knownDirectCopyColorType(sourceInfo.colorType())
        || !knownDirectCopyAlphaType(sourceInfo.alphaType())
        || copyWidth <= 0 || copyHeight <= 0
        || sourcePixels.addr() == nullptr || targetPixels.writable_addr() == nullptr
        || sourcePixels.rowBytes() < minimumSourceRowBytes
        || targetPixels.rowBytes() < minimumTargetRowBytes) {
        return false;
    }
    if (sourceInfo.height() > 0
        && sourcePixels.rowBytes() > std::numeric_limits<size_t>::max() / sourceInfo.height()) {
        return false;
    }
    if (targetPixelInfo.height() > 0
        && targetPixels.rowBytes() > std::numeric_limits<size_t>::max() / targetPixelInfo.height()) {
        return false;
    }
    const size_t sourceBytes = sourcePixels.rowBytes() * static_cast<size_t>(sourceInfo.height());
    const size_t targetBytes = targetPixels.rowBytes() * static_cast<size_t>(targetPixelInfo.height());
    if (memoryRangesOverlap(sourcePixels.addr(), sourceBytes,
            targetPixels.addr(), targetBytes)) {
        return false;
    }

    SkPixmap sourceRect;
    const SkIRect sourceBounds = SkIRect::MakeLTRB(
        sourceX, sourceY, sourceX + copyWidth, sourceY + copyHeight);
    if (!sourcePixels.extractSubset(&sourceRect, sourceBounds)) {
        return false;
    }
    if (!writePhysicalCopyPixels(canvas, sourceRect, deviceRect.left(), deviceRect.top())) {
        return false;
    }
    return true;
}

static void appendWord(std::vector<uint32_t>* words, uint32_t value) {
    words->push_back(value);
}

static void appendDouble(std::vector<uint32_t>* words, double value) {
    uint64_t bits = 0;
    std::memcpy(&bits, &value, sizeof(bits));
    words->push_back(static_cast<uint32_t>(bits));
    words->push_back(static_cast<uint32_t>(bits >> 32));
}

static void appendLong(std::vector<uint32_t>* words, uint64_t value) {
    words->push_back(static_cast<uint32_t>(value));
    words->push_back(static_cast<uint32_t>(value >> 32));
}

static void appendScalar(std::vector<uint32_t>* words, SkScalar value) {
    uint32_t bits = 0;
    std::memcpy(&bits, &value, sizeof(bits));
    words->push_back(bits);
}

static bool buildRasterVariantKey(const SkiaImageDrawPlanData* plan,
                                  const NativeImageBackingRecord* source, SkCanvas* canvas,
                                  int32_t kind, float srcLeft, float srcTop, float srcRight,
                                  float srcBottom, float dstLeft, float dstTop, float dstRight,
                                  float dstBottom, std::vector<uint32_t>* words) {
    if (!plan || !source || !canvas || !words || plan->operationCount < 0
        || plan->operationCount > std::numeric_limits<int32_t>::max() / 4
        || (plan->operationCount > 0 && (!plan->operations || !plan->parameters || !plan->dimensions))) {
        return false;
    }
    try {
        words->clear();
        const size_t operationCount = static_cast<size_t>(plan->operationCount);
        words->reserve(static_cast<size_t>(64) + operationCount * 7);
        appendWord(words, static_cast<uint32_t>(kind));
        appendLong(words, static_cast<uint64_t>(plan->sourceDecodeGeneration));
        appendLong(words, static_cast<uint64_t>(plan->sourceMutationGeneration));
        appendLong(words, static_cast<uint64_t>(plan->backingMutationGeneration));
        appendLong(words, source->generation);
        appendWord(words, static_cast<uint32_t>(plan->currentFrame));
        appendWord(words, static_cast<uint32_t>(plan->operationCount));
        appendWord(words, static_cast<uint32_t>(plan->rootWidth));
        appendWord(words, static_cast<uint32_t>(plan->rootHeight));
        appendWord(words, static_cast<uint32_t>(plan->rootLogicalWidth));
        appendWord(words, static_cast<uint32_t>(plan->rootLogicalHeight));
        appendWord(words, static_cast<uint32_t>(plan->rootFrameCount));
        appendWord(words, static_cast<uint32_t>(plan->rootWidthOfAllFrames));
        appendWord(words, static_cast<uint32_t>(plan->outputWidth));
        appendWord(words, static_cast<uint32_t>(plan->outputHeight));
        appendWord(words, static_cast<uint32_t>(plan->outputFrameCount));
        appendWord(words, static_cast<uint32_t>(plan->outputWidthOfAllFrames));
        appendWord(words, static_cast<uint32_t>(plan->alphaMask));
        appendWord(words, static_cast<uint32_t>(plan->transparentColor));
        appendWord(words, static_cast<uint32_t>(plan->materializeAlphaMask));
        appendWord(words, static_cast<uint32_t>(plan->outputAlphaMask));
        appendWord(words, static_cast<uint32_t>(plan->sourceOpacityState));
        appendDouble(words, plan->rootContentScale);
        appendDouble(words, plan->destinationScale);
        appendDouble(words, plan->outputContentScale);
        appendDouble(words, plan->hwScaleW);
        appendDouble(words, plan->hwScaleH);
        appendDouble(words, plan->rootHwScaleW);
        appendDouble(words, plan->rootHwScaleH);
        appendScalar(words, srcLeft);
        appendScalar(words, srcTop);
        appendScalar(words, srcRight);
        appendScalar(words, srcBottom);
        appendScalar(words, dstLeft);
        appendScalar(words, dstTop);
        appendScalar(words, dstRight);
        appendScalar(words, dstBottom);
        const SkImageInfo info = canvas->imageInfo();
        appendWord(words, static_cast<uint32_t>(info.width()));
        appendWord(words, static_cast<uint32_t>(info.height()));
        appendWord(words, static_cast<uint32_t>(info.colorType()));
        appendWord(words, static_cast<uint32_t>(info.alphaType()));
        const uintptr_t colorSpace = reinterpret_cast<uintptr_t>(info.colorSpace());
        appendLong(words, static_cast<uint64_t>(colorSpace));
        const SkMatrix& matrix = canvas->getTotalMatrix();
        SkScalar matrixValues[9];
        matrix.get9(matrixValues);
        for (SkScalar value : matrixValues) {
            appendScalar(words, value);
        }
        appendWord(words, static_cast<uint32_t>(source->width));
        appendWord(words, static_cast<uint32_t>(source->height));
        for (int32_t i = 0; i < plan->operationCount; ++i) {
            appendWord(words, static_cast<uint32_t>(plan->operations[i]));
        }
        for (size_t i = 0; i < operationCount * 4; ++i) {
            appendWord(words, static_cast<uint32_t>(plan->parameters[i]));
        }
        for (size_t i = 0; i < operationCount * 2; ++i) {
            appendWord(words, static_cast<uint32_t>(plan->dimensions[i]));
        }
        return true;
    } catch (const std::bad_alloc&) {
        words->clear();
        return false;
    }
}

static bool knownAlphaType(SkAlphaType type) {
    return type == kPremul_SkAlphaType || type == kUnpremul_SkAlphaType
        || type == kOpaque_SkAlphaType;
}

static bool knownRasterColorType(SkColorType type) {
    return type == kRGBA_8888_SkColorType || type == kBGRA_8888_SkColorType
        || type == kRGB_565_SkColorType;
}

static bool drawPlanImage(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                          const SkImage* image, float srcLeft, float srcTop, float srcRight,
                          float srcBottom, float dstLeft, float dstTop, float dstRight,
                          float dstBottom) {
    GeometryTransform transform;
    SkiaImageDrawColorFilters colorFilters;
    return image && compileGeometry(plan, -1, &transform)
        && skia_image_draw_color_filters(plan, &colorFilters)
        && geometryDrawCompiled(canvas, image, transform, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, plan->alphaMask,
            std::abs(plan->outputContentScale - 1.0) < 0.000001, &colorFilters);
}

static bool targetColorVariantDraw(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                                   float srcLeft, float srcTop, float srcRight, float srcBottom,
                                   float dstLeft, float dstTop, float dstRight, float dstBottom,
                                   int* status) {
    if (!plan || !plan->targetColorConversionEnabled || !canvas || !status) {
        return false;
    }
    NativeImageBackingRecord* source = findBacking(plan->rootHandle);
    if (!source) {
        return false;
    }
    const SkImageInfo targetInfo = canvas->imageInfo();
    const SkImageInfo sourceInfo = source->image ? source->image->imageInfo()
        : source->surface ? source->surface->imageInfo() : SkImageInfo();
    if (sourceInfo.colorType() == targetInfo.colorType()
        && sourceInfo.colorSpace() == targetInfo.colorSpace()) {
        return false;
    }
    *status |= SKIA_IMAGE_DRAW_TARGET_COLOR_ATTEMPT;
    auto fallback = [&]() {
        *status |= SKIA_IMAGE_DRAW_TARGET_COLOR_FALLBACK;
        return false;
    };
    if (!plan->sourceBackingStable
        || plan->sourceMutationGeneration != plan->backingMutationGeneration
        || !knownRasterColorType(sourceInfo.colorType())
        || !knownRasterColorType(targetInfo.colorType())
        || !knownAlphaType(sourceInfo.alphaType()) || !knownAlphaType(targetInfo.alphaType())
        || sourceInfo.colorSpace() != targetInfo.colorSpace()
        || !canvas->getSurface() || canvas->getSurface()->recordingContext()) {
        return fallback();
    }
    if (targetInfo.colorType() == kRGB_565_SkColorType
        && (plan->sourceOpacityState != 1 || plan->alphaMask != 255 || plan->outputAlphaMask != 255
            || plan->materializeAlphaMask != 255)) {
        return fallback();
    }
    if (targetInfo.alphaType() == kOpaque_SkAlphaType && plan->sourceOpacityState != 1) {
        return fallback();
    }
    if (targetInfo.colorType() == kRGB_565_SkColorType) {
        GeometryTransform transform;
        if (!compileGeometry(plan, -1, &transform) || transform.smooth) {
            return fallback();
        }
        for (int32_t i = 0; i < plan->operationCount; ++i) {
            if (plan->operations[i] >= SKIA_IMAGE_DRAW_TOUCH_UP
                && plan->operations[i] <= SKIA_IMAGE_DRAW_SET_TRANSPARENT_COLOR) {
                return fallback();
            }
        }
    }

    std::vector<uint32_t> key;
    if (!buildRasterVariantKey(plan, source, canvas, RASTER_VARIANT_TARGET_COLOR,
            srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, &key)) {
        return fallback();
    }
    sk_sp<SkImage> variant;
    bool provenOpaque = false;
    const int decision = rasterVariantObserve(source, RASTER_VARIANT_TARGET_COLOR, key,
                                               &variant, &provenOpaque);
    if (decision == RASTER_VARIANT_HIT) {
        if (!drawPlanImage(plan, canvas, variant.get(), srcLeft, srcTop, srcRight, srcBottom,
                dstLeft, dstTop, dstRight, dstBottom)) {
            return fallback();
        }
        *status |= SKIA_IMAGE_DRAW_TARGET_COLOR_HIT;
        return true;
    }
    if (decision == RASTER_VARIANT_MISS) {
        return fallback();
    }
    if (decision != RASTER_VARIANT_MATERIALIZE) {
        return fallback();
    }

    if (skia_image_backing_consume_variant_materialization_failure_for_test()) {
        rasterVariantFail(source, RASTER_VARIANT_TARGET_COLOR, key);
        return fallback();
    }
    try {
        sk_sp<SkImage> image = source->snapshot();
        if (!image || !knownAlphaType(image->alphaType())) {
            rasterVariantFail(source, RASTER_VARIANT_TARGET_COLOR, key);
            return fallback();
        }
        SkAlphaType convertedAlpha = targetInfo.alphaType();
        if (targetInfo.colorType() == kRGB_565_SkColorType) {
            convertedAlpha = kOpaque_SkAlphaType;
        }
        SkImageInfo convertedInfo = SkImageInfo::Make(image->width(), image->height(),
            targetInfo.colorType(), convertedAlpha, targetInfo.refColorSpace());
        sk_sp<SkSurface> converted = SkSurface::MakeRaster(convertedInfo);
        if (!converted) {
            rasterVariantFail(source, RASTER_VARIANT_TARGET_COLOR, key);
            return fallback();
        }
        converted->getCanvas()->clear(SK_ColorTRANSPARENT);
        converted->getCanvas()->drawImage(image.get(), 0, 0);
        variant = converted->makeImageSnapshot();
        if (!variant || !rasterVariantStore(source, RASTER_VARIANT_TARGET_COLOR, key,
                variant, plan->sourceOpacityState == 1)) {
            rasterVariantFail(source, RASTER_VARIANT_TARGET_COLOR, key);
            return fallback();
        }
    } catch (const std::bad_alloc&) {
        rasterVariantFail(source, RASTER_VARIANT_TARGET_COLOR, key);
        return fallback();
    }
    if (!drawPlanImage(plan, canvas, variant.get(), srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom)) {
        return fallback();
    }
    *status |= SKIA_IMAGE_DRAW_TARGET_COLOR_MATERIALIZED;
    return true;
}

static bool drawPhysicalVariant(SkCanvas* canvas, const SkImage* image) {
    if (!canvas || !image || image->width() != canvas->imageInfo().width()
        || image->height() != canvas->imageInfo().height()) {
        return false;
    }
    SkPaint paint;
    paint.setFilterQuality(kNone_SkFilterQuality);
    canvas->save();
    canvas->resetMatrix();
    canvas->drawImageRect(image, SkRect::MakeWH(image->width(), image->height()),
        SkRect::MakeWH(image->width(), image->height()), &paint,
        SkCanvas::kStrict_SrcRectConstraint);
    canvas->restore();
    return true;
}

static bool physicalVariantDraw(const SkiaImageDrawPlanData* plan, SkCanvas* canvas,
                                float srcLeft, float srcTop, float srcRight, float srcBottom,
                                float dstLeft, float dstTop, float dstRight, float dstBottom,
                                int* status) {
    if (!plan || !plan->physicalVariantCacheEnabled || !canvas || !status) {
        return false;
    }
    *status |= SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_ATTEMPT;
    auto fallback = [&]() {
        *status |= SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_FALLBACK;
        return false;
    };
    NativeImageBackingRecord* source = findBacking(plan->rootHandle);
    if (!source || !plan->sourceBackingStable
        || plan->sourceMutationGeneration != plan->backingMutationGeneration
        || !canvas->getSurface() || canvas->getSurface()->recordingContext()) {
        return fallback();
    }
    const SkImageInfo info = canvas->imageInfo();
    const int64_t pixelCount = static_cast<int64_t>(info.width()) * info.height();
    if (info.width() <= 0 || info.height() <= 0 || pixelCount <= 0 || pixelCount > 4 * 1024 * 1024
        || (info.colorType() != kRGBA_8888_SkColorType && info.colorType() != kBGRA_8888_SkColorType)
        || !knownAlphaType(info.alphaType()) || info.alphaType() == kOpaque_SkAlphaType) {
        return fallback();
    }
    const SkMatrix& matrix = canvas->getTotalMatrix();
    if (matrix.hasPerspective() || matrix.getSkewX() != 0 || matrix.getSkewY() != 0
        || !std::isfinite(matrix.getScaleX()) || !std::isfinite(matrix.getScaleY())
        || matrix.getScaleX() <= 0 || matrix.getScaleY() <= 0
        || !integerCoordinate(matrix.getTranslateX()) || !integerCoordinate(matrix.getTranslateY())) {
        return fallback();
    }
    SkRect deviceBounds;
    matrix.mapRect(&deviceBounds, SkRect::MakeLTRB(dstLeft, dstTop, dstRight, dstBottom));
    if (!integerCoordinate(deviceBounds.left()) || !integerCoordinate(deviceBounds.top())
        || !integerCoordinate(deviceBounds.right()) || !integerCoordinate(deviceBounds.bottom())) {
        return fallback();
    }

    std::vector<uint32_t> key;
    if (!buildRasterVariantKey(plan, source, canvas, RASTER_VARIANT_PHYSICAL,
            srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, &key)) {
        return fallback();
    }
    sk_sp<SkImage> variant;
    bool provenOpaque = false;
    const int decision = rasterVariantObserve(source, RASTER_VARIANT_PHYSICAL, key,
                                               &variant, &provenOpaque);
    UNUSED(provenOpaque)
    if (decision == RASTER_VARIANT_HIT) {
        if (!drawPhysicalVariant(canvas, variant.get())) {
            return fallback();
        }
        *status |= SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_HIT;
        return true;
    }
    if (decision == RASTER_VARIANT_MISS) {
        return fallback();
    }
    if (decision != RASTER_VARIANT_MATERIALIZE) {
        return fallback();
    }

    if (skia_image_backing_consume_variant_materialization_failure_for_test()) {
        rasterVariantFail(source, RASTER_VARIANT_PHYSICAL, key);
        return fallback();
    }
    try {
        sk_sp<SkSurface> materialized = SkSurface::MakeRaster(info);
        if (!materialized) {
            rasterVariantFail(source, RASTER_VARIANT_PHYSICAL, key);
            return fallback();
        }
        SkCanvas* target = materialized->getCanvas();
        target->clear(SK_ColorTRANSPARENT);
        target->setMatrix(matrix);
        if (!geometryDraw(plan, target, srcLeft, srcTop, srcRight, srcBottom,
                dstLeft, dstTop, dstRight, dstBottom, -1)) {
            rasterVariantFail(source, RASTER_VARIANT_PHYSICAL, key);
            return fallback();
        }
        variant = materialized->makeImageSnapshot();
        if (!variant || !rasterVariantStore(source, RASTER_VARIANT_PHYSICAL, key,
                variant, false)) {
            rasterVariantFail(source, RASTER_VARIANT_PHYSICAL, key);
            return fallback();
        }
    } catch (const std::bad_alloc&) {
        rasterVariantFail(source, RASTER_VARIANT_PHYSICAL, key);
        return fallback();
    }
    if (!drawPhysicalVariant(canvas, variant.get())) {
        return fallback();
    }
    *status |= SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_MATERIALIZED;
    return true;
}

}

void skia_image_geometry_fail_next_physical_copy_write_pixels_for_test() {
    failNextPhysicalCopyWritePixelsForTest = true;
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
    float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom,
    bool allowPhysicalCopy, bool physicalCopyOnly) {
    SkCanvas* canvas = skiaGetCanvas(targetSurface);
    int status = 0;
    if (plan && plan->physicalIdentityEnabled) {
        status |= SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_ATTEMPT;
        if (allowPhysicalCopy) {
            status |= SKIA_IMAGE_DRAW_PHYSICAL_COPY_ATTEMPT;
            if (physicalIdentityCopyDraw(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
                    dstLeft, dstTop, dstRight, dstBottom)) {
                return status | SKIA_IMAGE_DRAW_PHYSICAL_COPY_HIT | SKIA_IMAGE_DRAW_HANDLED;
            }
            status |= SKIA_IMAGE_DRAW_PHYSICAL_COPY_FALLBACK;
        }
        if (physicalCopyOnly) {
            return status;
        }
        if (physicalIdentityDraw(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
                                 dstLeft, dstTop, dstRight, dstBottom)) {
            return status | SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_HIT | SKIA_IMAGE_DRAW_HANDLED;
        }
        status |= SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_FALLBACK;
    }
    if (physicalCopyOnly) {
        return status;
    }
    if (targetColorVariantDraw(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, &status)) {
        return status | SKIA_IMAGE_DRAW_HANDLED;
    }
    if ((status & SKIA_IMAGE_DRAW_TARGET_COLOR_ATTEMPT) == 0
        && physicalVariantDraw(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, &status)) {
        return status | SKIA_IMAGE_DRAW_HANDLED;
    }
    if (!geometryDraw(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
                      dstLeft, dstTop, dstRight, dstBottom, -1)) {
        return status;
    }
    status |= SKIA_IMAGE_DRAW_HANDLED | SKIA_IMAGE_DRAW_GENERIC_GEOMETRY;
    GeometryTransform transform;
    PhysicalIdentityMapping exactMapping;
    if (compileGeometry(plan, -1, &transform) && transform.smooth
        && !physicalIdentityMapping(plan, canvas, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, true, &exactMapping)) {
        status |= SKIA_IMAGE_DRAW_SMOOTH_RESAMPLE;
    }
    return status;
}
