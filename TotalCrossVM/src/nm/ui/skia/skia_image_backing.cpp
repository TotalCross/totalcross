// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing.h"

#include "skia_internal.h"

#include <cstddef>
#include <cstdint>
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
int64_t nextHandle = 1;

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

SkImageInfo rasterInfo(int32 width, int32 height) {
    return SkImageInfo::Make(width, height, kRGBA_8888_SkColorType, kUnpremul_SkAlphaType);
}

void releaseOwnedPixels(const void* pixels, void*) {
    delete[] static_cast<const uint8_t*>(pixels);
}

bool readRgba(NativeImageBackingRecord* backing, void* output, int32 x, int32 y,
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

int skia_image_backing_draw(int64_t targetHandle, int64_t sourceHandle,
                            float srcLeft, float srcTop, float srcRight, float srcBottom,
                            float dstLeft, float dstTop, float dstRight, float dstBottom,
                            int32 alphaMask) {
    NativeImageBackingRecord* target = findBacking(targetHandle);
    NativeImageBackingRecord* source = findBacking(sourceHandle);
    if (!target || !target->surface || !source || alphaMask < 0 || alphaMask > 255) {
        return 0;
    }
    sk_sp<SkImage> image = source->snapshot();
    SkCanvas* canvas = target->canvas();
    if (!image || !canvas) {
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

SkCanvas* skia_image_backing_canvas(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->canvas() : nullptr;
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

void skia_image_backing_release(int64_t handle) {
    if (handle != 0) {
        backings.erase(handle);
    }
}
