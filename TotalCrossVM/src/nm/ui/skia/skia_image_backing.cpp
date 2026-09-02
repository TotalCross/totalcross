// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing.h"

#include "skia_internal.h"

#include "include/core/SkSamplingOptions.h"

#include <cstddef>
#include <cstdint>
#include <cstring>
#include <cmath>
#include <limits>
#include <map>
#include <memory>
#include <new>
#include <vector>

namespace {

struct NativeImageBackingRecord {
    sk_sp<SkImage> image;
    sk_sp<SkSurface> surface;
    int32 width;
    int32 height;

    SkCanvas* canvas() const {
        return surface ? surface->getCanvas() : nullptr;
    }

    sk_sp<SkImage> snapshot() const {
        return image ? image : surface ? surface->makeImageSnapshot() : nullptr;
    }
};

std::map<int64_t, std::unique_ptr<NativeImageBackingRecord>> backings;
std::map<int32, int64_t> surfaceAliases;
std::map<int64_t, int32> backingAliases;
int64_t nextHandle = 1;
int32 nextSurfaceAlias = std::numeric_limits<int32>::min() + 1;

NativeImageBackingRecord* findBacking(int64_t handle) {
    auto found = backings.find(handle);
    return found == backings.end() ? nullptr : found->second.get();
}

int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing) {
    if (!backing || nextHandle <= 0) {
        return 0;
    }
    const int64_t handle = nextHandle++;
    try {
        backings.emplace(handle, std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
    return handle;
}

int32 registerSurfaceAlias(int64_t handle) {
    if (!findBacking(handle)) {
        return SKIA_INVALID_SURFACE_ID;
    }
    auto existing = backingAliases.find(handle);
    if (existing != backingAliases.end()) {
        return existing->second;
    }
    try {
        for (size_t attempts = 0; attempts < 1024; ++attempts) {
            const int32 alias = nextSurfaceAlias++;
            if (alias >= SKIA_INVALID_SURFACE_ID) {
                nextSurfaceAlias = std::numeric_limits<int32>::min() + 1;
            }
            if (surfaceAliases.find(alias) == surfaceAliases.end()) {
                surfaceAliases.emplace(alias, handle);
                try {
                    backingAliases.emplace(handle, alias);
                    return alias;
                } catch (const std::bad_alloc&) {
                    surfaceAliases.erase(alias);
                    throw;
                }
            }
        }
    } catch (const std::bad_alloc&) {
    }
    return SKIA_INVALID_SURFACE_ID;
}

SkCanvas* canvasForSurfaceAlias(int32 surfaceId) {
    auto alias = surfaceAliases.find(surfaceId);
    return alias == surfaceAliases.end() ? nullptr : skia_image_backing_canvas(alias->second);
}

int drawOnCanvas(SkCanvas* canvas, NativeImageBackingRecord* source,
                 float srcLeft, float srcTop, float srcRight, float srcBottom,
                 float dstLeft, float dstTop, float dstRight, float dstBottom,
                 int32 alphaMask) {
    if (!canvas || !source || alphaMask < 0 || alphaMask > 255) {
        return 0;
    }
    sk_sp<SkImage> image = source->snapshot();
    if (!image) {
        return 0;
    }
    SkPaint paint;
    paint.setAlpha(alphaMask);
    paint.setFilterQuality(kNone_SkFilterQuality);
    canvas->drawImageRect(image.get(), SkRect::MakeLTRB(srcLeft, srcTop, srcRight, srcBottom),
                          SkRect::MakeLTRB(dstLeft, dstTop, dstRight, dstBottom), &paint,
                          SkCanvas::kStrict_SrcRectConstraint);
    return 1;
}

SkImageInfo rasterInfo(int32 width, int32 height) {
    return SkImageInfo::Make(width, height, kRGBA_8888_SkColorType, kUnpremul_SkAlphaType);
}

void releaseOwnedPixels(const void* pixels, void*) {
    delete[] static_cast<const uint8_t*>(pixels);
}

bool readRgbaBytes(NativeImageBackingRecord* backing, void* output, int32 x, int32 y,
                   int32 width, int32 height) {
    if (!backing || !output || x < 0 || y < 0 || width <= 0 || height <= 0 ||
        x > backing->width - width || y > backing->height - height) {
        return false;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    if (pixelCount > std::numeric_limits<size_t>::max() / 4) {
        return false;
    }

    try {
        std::vector<uint8_t> rgba(static_cast<size_t>(pixelCount) * 4);
        const SkImageInfo info = rasterInfo(width, height);
        const size_t rowBytes = static_cast<size_t>(width) * 4;
        bool copied = false;
        if (backing->surface) {
            copied = backing->surface->readPixels(info, rgba.data(), rowBytes, x, y);
        } else if (backing->image) {
            copied = backing->image->readPixels(info, rgba.data(), rowBytes, x, y);
        }
        if (!copied) {
            return false;
        }

        std::memcpy(output, rgba.data(), rgba.size());
        return true;
    } catch (const std::bad_alloc&) {
        return false;
    }
}

bool readRgba(NativeImageBackingRecord* backing, void* output, int32 x, int32 y,
              int32 width, int32 height) {
    if (!output) {
        return false;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    if (pixelCount > std::numeric_limits<size_t>::max() / sizeof(Pixel)) {
        return false;
    }
    try {
        std::vector<uint8_t> rgba(static_cast<size_t>(pixelCount) * 4);
        if (!readRgbaBytes(backing, rgba.data(), x, y, width, height)) {
            return false;
        }
        Pixel* pixels = static_cast<Pixel*>(output);
        for (size_t i = 0; i < static_cast<size_t>(pixelCount); ++i) {
            const uint8_t* pixel = rgba.data() + i * 4;
            pixels[i] = (static_cast<Pixel>(pixel[3]) << 24)
                | (static_cast<Pixel>(pixel[0]) << 16)
                | (static_cast<Pixel>(pixel[1]) << 8)
                | static_cast<Pixel>(pixel[2]);
        }
        return true;
    } catch (const std::bad_alloc&) {
        return false;
    }
}

} // namespace

int64_t skia_image_backing_create_empty(int32 width, int32 height) {
    if (width <= 0 || height <= 0) {
        return 0;
    }
    try {
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->surface = SkSurface::MakeRaster(rasterInfo(width, height));
        if (!backing->surface) {
            return 0;
        }
        backing->width = width;
        backing->height = height;
        return registerBacking(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_from_rgba_pixels(void* pixels, int32 width, int32 height) {
    if (!pixels || width <= 0 || height <= 0) {
        return 0;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    if (pixelCount > std::numeric_limits<size_t>::max() / 4) {
        return 0;
    }
    const size_t rowBytes = static_cast<size_t>(width) * 4;
    const size_t byteCount = static_cast<size_t>(pixelCount) * 4;
    try {
        sk_sp<SkData> data = SkData::MakeWithProc(pixels, byteCount, releaseOwnedPixels, nullptr);
        sk_sp<SkImage> image = SkImage::MakeRasterData(rasterInfo(width, height), data, rowBytes);
        if (!image) {
            return 0;
        }
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->image = std::move(image);
        backing->width = width;
        backing->height = height;
        return registerBacking(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_from_argb_pixels(const void* pixels, int32 width, int32 height) {
    if (!pixels || width <= 0 || height <= 0) {
        return 0;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    if (pixelCount > std::numeric_limits<size_t>::max() / 4) {
        return 0;
    }
    try {
        const uint8_t* source = static_cast<const uint8_t*>(pixels);
        std::unique_ptr<uint8_t[]> rgba(new uint8_t[static_cast<size_t>(pixelCount) * 4]);
        for (size_t i = 0; i < static_cast<size_t>(pixelCount); ++i) {
            rgba[i * 4] = source[i * 4 + 3];
            rgba[i * 4 + 1] = source[i * 4 + 2];
            rgba[i * 4 + 2] = source[i * 4 + 1];
            rgba[i * 4 + 3] = source[i * 4];
        }
        uint8_t* owned = rgba.release();
        return skia_image_backing_create_from_rgba_pixels(owned, width, height);
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_snapshot(int64_t handle) {
    NativeImageBackingRecord* source = findBacking(handle);
    if (!source) {
        return 0;
    }
    try {
        sk_sp<SkImage> snapshot = source->snapshot();
        if (!snapshot) {
            return 0;
        }
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->image = std::move(snapshot);
        backing->width = source->width;
        backing->height = source->height;
        return registerBacking(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int skia_image_backing_make_mutable(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing) {
        return 0;
    }
    if (backing->surface) {
        return 1;
    }
    if (!backing->image) {
        return 0;
    }
    try {
        sk_sp<SkSurface> surface = SkSurface::MakeRaster(rasterInfo(backing->width, backing->height));
        if (!surface) {
            return 0;
        }
        surface->getCanvas()->drawImage(backing->image, 0, 0);
        backing->surface = std::move(surface);
        backing->image.reset();
        return 1;
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_scale(int64_t handle, int32 outputWidth, int32 outputHeight, bool smooth) {
    NativeImageBackingRecord* source = findBacking(handle);
    if (!source || outputWidth <= 0 || outputHeight <= 0) {
        return 0;
    }
    try {
        sk_sp<SkImage> image = source->snapshot();
        if (!image) {
            return 0;
        }
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->surface = SkSurface::MakeRaster(rasterInfo(outputWidth, outputHeight));
        if (!backing->surface) {
            return 0;
        }
        SkPaint paint;
        paint.setFilterQuality(smooth ? kLow_SkFilterQuality : kNone_SkFilterQuality);
        backing->surface->getCanvas()->drawImageRect(
            image.get(),
            SkRect::MakeWH(image->width(), image->height()),
            SkRect::MakeWH(outputWidth, outputHeight),
            &paint,
            SkCanvas::kStrict_SrcRectConstraint);
        backing->width = outputWidth;
        backing->height = outputHeight;
        return registerBacking(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

struct GeometryTransform {
    double a;
    double b;
    double c;
    double d;
    double tx;
    double ty;
    double width;
    double height;
    SkRect validRoot;
    bool smooth;
    bool hasFill;
    Pixel fillColor;
};

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

static bool geometryOperation(const SkiaImageGeometryPlanData* plan, int index, int32* operation,
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

static void geometrySetRootFrame(GeometryTransform* transform, const SkiaImageGeometryPlanData* plan,
                                 int frame) {
    const int count = std::max(1, plan->rootFrameCount);
    const double fullWidth = plan->rootWidthOfAllFrames > 0 ? plan->rootWidthOfAllFrames : plan->rootWidth;
    const double frameWidth = fullWidth / count;
    const int normalized = geometryFrame(frame, count);
    transform->validRoot = SkRect::MakeLTRB(static_cast<float>(normalized * frameWidth), 0,
                                             static_cast<float>((normalized + 1) * frameWidth),
                                             static_cast<float>(plan->rootHeight));
}

static bool compileGeometry(const SkiaImageGeometryPlanData* plan, int frameOverride,
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
        case 11: { // FRAME_SELECT
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
        case 13: { // FRAME_LAYOUT
            const int count = std::max(1, parameters[0]);
            const int frame = geometryFrame(requestedFrame, count);
            geometryTranslate(transform, frame * outputWidth, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            if (plan->rootFrameCount == 1) {
                const double fullWidth = plan->rootWidthOfAllFrames > 0 ? plan->rootWidthOfAllFrames : plan->rootWidth;
                const double frameWidth = fullWidth / count;
                transform->validRoot = SkRect::MakeLTRB(static_cast<float>(frame * frameWidth), 0,
                                                         static_cast<float>((frame + 1) * frameWidth),
                                                         static_cast<float>(plan->rootHeight));
            }
            break;
        }
        case 12: // CROP
            geometryTranslate(transform, parameters[0], parameters[1]);
            transform->width = outputWidth;
            transform->height = outputHeight;
            break;
        case 0: // SCALE
            geometryCompose(transform, oldWidth / outputWidth, 0, 0, oldHeight / outputHeight, 0, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            break;
        case 1: // SMOOTH_SCALE
            geometryCompose(transform, oldWidth / outputWidth, 0, 0, oldHeight / outputHeight, 0, 0);
            transform->width = outputWidth;
            transform->height = outputHeight;
            transform->smooth = true;
            break;
        case 2: { // ROTATE_SCALE
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
            transform->fillColor = parameters[2] == -1
                ? static_cast<Pixel>(plan->transparentColor) : static_cast<Pixel>(parameters[2]);
            break;
        }
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
                                 bool applyPixelCenterOffset) {
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
        fill.setColor(skiaColorFromPixel(transform.fillColor));
        fill.setAlpha(alphaMask);
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
    paint.setShader(image->makeShader(SkTileMode::kClamp, SkTileMode::kClamp, sampling,
        applyPixelCenterOffset ? &shaderMatrix : nullptr));
    canvas->drawRect(SkRect::MakeWH(image->width(), image->height()), paint);
    canvas->restore();
    return true;
}

static bool geometryDraw(const SkiaImageGeometryPlanData* plan, SkCanvas* canvas, float srcLeft,
                         float srcTop, float srcRight, float srcBottom, float dstLeft, float dstTop,
                         float dstRight, float dstBottom, int frameOverride) {
    NativeImageBackingRecord* source = plan ? findBacking(plan->rootHandle) : nullptr;
    if (!source || !canvas) {
        return false;
    }
    try {
        sk_sp<SkImage> image = source->snapshot();
        GeometryTransform transform;
        if (!image || !compileGeometry(plan, frameOverride, &transform)) {
            return false;
        }
        return geometryDrawCompiled(canvas, image.get(), transform, srcLeft, srcTop, srcRight, srcBottom,
                                    dstLeft, dstTop, dstRight, dstBottom, plan->alphaMask,
                                    std::abs(plan->outputContentScale - 1.0) < 0.000001);
    } catch (const std::bad_alloc&) {
        return false;
    }
}

int skia_image_backing_draw_geometry_to_surface(int32 targetSurface,
    const SkiaImageGeometryPlanData* plan, float srcLeft, float srcTop, float srcRight,
    float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom) {
    return geometryDraw(plan, skiaGetCanvas(targetSurface), srcLeft, srcTop, srcRight, srcBottom,
                        dstLeft, dstTop, dstRight, dstBottom, -1) ? 1 : 0;
}

int64_t skia_image_backing_materialize_geometry(const SkiaImageGeometryPlanData* plan) {
    if (!plan || plan->outputWidth <= 0 || plan->outputHeight <= 0 || plan->outputFrameCount <= 0
        || !std::isfinite(plan->outputContentScale) || plan->outputContentScale <= 0) {
        return 0;
    }
    const double physicalFrameWidth = std::ceil(plan->outputWidth * plan->outputContentScale);
    const double physicalHeight = std::ceil(plan->outputHeight * plan->outputContentScale);
    const double physicalFullWidth = physicalFrameWidth * plan->outputFrameCount;
    if (!std::isfinite(physicalFrameWidth) || !std::isfinite(physicalHeight)
        || !std::isfinite(physicalFullWidth) || physicalFrameWidth <= 0 || physicalHeight <= 0
        || physicalFullWidth > std::numeric_limits<int32>::max()
        || physicalHeight > std::numeric_limits<int32>::max()) {
        return 0;
    }
    NativeImageBackingRecord* source = findBacking(plan->rootHandle);
    if (!source) {
        return 0;
    }
    try {
        sk_sp<SkImage> image = source->snapshot();
        if (!image) {
            return 0;
        }
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->surface = SkSurface::MakeRaster(rasterInfo(static_cast<int32>(physicalFullWidth),
                                                             static_cast<int32>(physicalHeight)));
        if (!backing->surface) {
            return 0;
        }
        SkCanvas* target = backing->surface->getCanvas();
        target->clear(SK_ColorTRANSPARENT);
        target->scale(static_cast<float>(plan->outputContentScale),
                      static_cast<float>(plan->outputContentScale));
        for (int frame = 0; frame < plan->outputFrameCount; ++frame) {
            GeometryTransform transform;
            if (!compileGeometry(plan, frame, &transform)
                || !geometryDrawCompiled(target, image.get(), transform, 0, 0,
                    static_cast<float>(plan->outputWidth), static_cast<float>(plan->outputHeight),
                    static_cast<float>(frame * plan->outputWidth), 0,
                    static_cast<float>((frame + 1) * plan->outputWidth),
                    static_cast<float>(plan->outputHeight), plan->materializeAlphaMask,
                    std::abs(plan->outputContentScale - 1.0) < 0.000001)) {
                return 0;
            }
        }
        backing->width = static_cast<int32>(physicalFullWidth);
        backing->height = static_cast<int32>(physicalHeight);
        return registerBacking(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int skia_image_backing_draw(int64_t targetHandle, int64_t sourceHandle,
                            float srcLeft, float srcTop, float srcRight, float srcBottom,
                            float dstLeft, float dstTop, float dstRight, float dstBottom,
                            int32 alphaMask) {
    NativeImageBackingRecord* source = findBacking(sourceHandle);
    NativeImageBackingRecord* target = findBacking(targetHandle);
    if (!target || !target->surface || !source) {
        return 0;
    }
    return drawOnCanvas(target->canvas(), source, srcLeft, srcTop, srcRight, srcBottom,
                        dstLeft, dstTop, dstRight, dstBottom, alphaMask);
}

int32 skia_image_backing_surface_id(int64_t handle) {
    return registerSurfaceAlias(handle);
}

int skia_image_backing_draw_to_surface(int32 targetSurface, int64_t sourceHandle,
                                       float srcLeft, float srcTop, float srcRight, float srcBottom,
                                       float dstLeft, float dstTop, float dstRight, float dstBottom,
                                       int32 alphaMask) {
    return drawOnCanvas(skiaGetCanvas(targetSurface), findBacking(sourceHandle), srcLeft, srcTop,
                        srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom, alphaMask);
}

SkCanvas* skia_image_backing_canvas(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->canvas() : nullptr;
}

SkCanvas* skia_image_backing_canvas_for_surface_id(int32 surfaceId) {
    return canvasForSurfaceAlias(surfaceId);
}

int32 skia_image_backing_width(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->width : 0;
}

int32 skia_image_backing_height(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->height : 0;
}

int skia_image_backing_read_pixels(int64_t handle, void* output, int32 x, int32 y,
                                   int32 width, int32 height) {
    return readRgba(findBacking(handle), output, x, y, width, height) ? 1 : 0;
}

int skia_image_backing_read_row(int64_t handle, void* output, int32 y, int32 width) {
    return skia_image_backing_read_pixels(handle, output, 0, y, width, 1);
}

int skia_image_backing_read_rgba_row(int64_t handle, void* output, int32 y, int32 width) {
    return readRgbaBytes(findBacking(handle), output, 0, y, width, 1) ? 1 : 0;
}

void skia_image_backing_release(int64_t handle) {
    if (handle != 0) {
        auto alias = backingAliases.find(handle);
        if (alias != backingAliases.end()) {
            surfaceAliases.erase(alias->second);
            backingAliases.erase(alias);
        }
        backings.erase(handle);
    }
}
