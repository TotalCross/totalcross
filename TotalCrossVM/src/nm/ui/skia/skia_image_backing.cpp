// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing.h"

#include "skia_internal.h"

#include <cstddef>
#include <cstdint>
#include <cstring>
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
