// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"

#include <cstddef>
#include <cstdint>
#include <cstring>
#include <cstdlib>
#include <algorithm>
#include <cmath>
#include <limits>
#include <map>
#include <memory>
#include <new>
#include <vector>

namespace {

using skia_image_backing_internal::NativeImageBackingRecord;

std::map<int64_t, std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord>> backings;
std::map<int32, int64_t> surfaceAliases;
std::map<int64_t, int32> backingAliases;
int64_t nextHandle = 1;
int32 nextSurfaceAlias = std::numeric_limits<int32>::min() + 1;
bool failNextSnapshotAllocationForTest;
bool backingAccountingForTest;
uint64_t backingRecordsCreatedForTest;
uint64_t backingRecordsReleasedForTest;
uint64_t backingRecordsLiveForTest;
uint64_t backingRecordsPeakLiveForTest;
uint64_t backingBytesLiveForTest;
uint64_t backingBytesPeakLiveForTest;
uint64_t writePixelsAttemptsForTest;
uint64_t writePixelsHitsForTest;
uint64_t writePixelsFallbacksForTest;
uint64_t writePixelsCopiedBytesForTest;

uint64_t backingBytes(const NativeImageBackingRecord& backing) {
    return static_cast<uint64_t>(backing.width) * static_cast<uint64_t>(backing.height) * 4;
}

void recordBackingCreated(const NativeImageBackingRecord& backing) {
    if (!backingAccountingForTest) {
        return;
    }
    const uint64_t bytes = backingBytes(backing);
    ++backingRecordsCreatedForTest;
    ++backingRecordsLiveForTest;
    backingBytesLiveForTest += bytes;
    backingRecordsPeakLiveForTest = std::max(backingRecordsPeakLiveForTest, backingRecordsLiveForTest);
    backingBytesPeakLiveForTest = std::max(backingBytesPeakLiveForTest, backingBytesLiveForTest);
}

void recordBackingReleased(const NativeImageBackingRecord& backing) {
    if (!backingAccountingForTest) {
        return;
    }
    ++backingRecordsReleasedForTest;
    if (backingRecordsLiveForTest > 0) {
        --backingRecordsLiveForTest;
    }
    const uint64_t bytes = backingBytes(backing);
    backingBytesLiveForTest = backingBytesLiveForTest >= bytes ? backingBytesLiveForTest - bytes : 0;
}

skia_image_backing_internal::NativeImageBackingRecord* findBacking(int64_t handle) {
    auto found = backings.find(handle);
    return found == backings.end() ? nullptr : found->second.get();
}

int64_t registerBackingRecord(std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord> backing) {
    if (!backing || nextHandle <= 0) {
        return 0;
    }
    const int64_t handle = nextHandle++;
    try {
        auto inserted = backings.emplace(handle, std::move(backing));
        if (!inserted.second) {
            return 0;
        }
        recordBackingCreated(*inserted.first->second);
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

bool proveOpaqueForWritePixels(NativeImageBackingRecord* source) {
    if (!source) {
        return false;
    }
    if (source->opacity == SKIA_IMAGE_OPACITY_OPAQUE) {
        return true;
    }
    if (source->opacity == SKIA_IMAGE_OPACITY_TRANSLUCENT) {
        return false;
    }
    sk_sp<SkImage> image = source->snapshot();
    SkPixmap pixmap;
    if (!image || !image->peekPixels(&pixmap)) {
        return false;
    }
    bool opaque = true;
    for (int32 y = 0; y < source->height && opaque; ++y) {
        const uint8_t* row = static_cast<const uint8_t*>(pixmap.addr(0, y));
        if (!row) {
            return false;
        }
        for (int32 x = 0; x < source->width; ++x) {
            if (row[static_cast<size_t>(x) * 4 + 3] != 0xff) {
                opaque = false;
                break;
            }
        }
    }
    source->opacity = opaque ? SKIA_IMAGE_OPACITY_OPAQUE : SKIA_IMAGE_OPACITY_TRANSLUCENT;
    imageRecordOpacityFallbackScanForTest(static_cast<int32>(
        static_cast<uint64_t>(source->width) * static_cast<uint64_t>(source->height)));
    return opaque;
}

bool tryWritePixels(SkCanvas* targetCanvas, NativeImageBackingRecord* source,
                    float srcLeft, float srcTop, float srcRight, float srcBottom,
                    float dstLeft, float dstTop, float dstRight, float dstBottom,
                    int32 alphaMask, int32 optimizationMask) {
#if TC_GRAPHICS_SOFTWARE
    constexpr int32 kOpaqueWritePixelsBit = 1 << 2;
    if ((optimizationMask & kOpaqueWritePixelsBit) == 0) {
        return false;
    }
    ++writePixelsAttemptsForTest;
    auto fallback = []() {
        ++writePixelsFallbacksForTest;
        return false;
    };
    if (!targetCanvas || !source || alphaMask != 255
        || !targetCanvas->getTotalMatrix().isIdentity()
        || targetCanvas->getSaveCount() != 1 || srcLeft != 0.0f || srcTop != 0.0f
        || srcRight != source->width || srcBottom != source->height
        || srcRight - srcLeft != dstRight - dstLeft
        || srcBottom - srcTop != dstBottom - dstTop
        || std::floor(dstLeft) != dstLeft || std::floor(dstTop) != dstTop) {
        return fallback();
    }
    const int32 dstX = static_cast<int32>(dstLeft);
    const int32 dstY = static_cast<int32>(dstTop);
    const SkImageInfo targetInfo = targetCanvas->imageInfo();
    if (dstX < 0 || dstY < 0 || dstX > targetInfo.width() - source->width
        || dstY > targetInfo.height() - source->height) {
        return fallback();
    }
    if (!proveOpaqueForWritePixels(source)) {
        return fallback();
    }
    sk_sp<SkImage> image = source->snapshot();
    SkPixmap pixmap;
    if (!image || !image->peekPixels(&pixmap)) {
        return fallback();
    }
    if (!targetCanvas->writePixels(pixmap.info(), pixmap.addr(), pixmap.rowBytes(), dstX, dstY)) {
        return fallback();
    }
    ++writePixelsHitsForTest;
    writePixelsCopiedBytesForTest += static_cast<uint64_t>(source->width)
        * static_cast<uint64_t>(source->height) * 4;
    return true;
#else
    UNUSED(targetCanvas)
    UNUSED(source)
    UNUSED(srcLeft)
    UNUSED(srcTop)
    UNUSED(srcRight)
    UNUSED(srcBottom)
    UNUSED(dstLeft)
    UNUSED(dstTop)
    UNUSED(dstRight)
    UNUSED(dstBottom)
    UNUSED(alphaMask)
    UNUSED(optimizationMask)
    return false;
#endif
}

SkImageInfo rasterInfo(int32 width, int32 height) {
    return SkImageInfo::Make(width, height, kRGBA_8888_SkColorType, kUnpremul_SkAlphaType);
}

void releaseOwnedPixels(const void* pixels, void*) {
    delete[] static_cast<const uint8_t*>(pixels);
}

void releaseMallocPixels(const void* pixels, void*) {
    std::free(const_cast<void*>(pixels));
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

namespace skia_image_backing_internal {

NativeImageBackingRecord* findBacking(int64_t handle) {
    return ::findBacking(handle);
}

int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing) {
    return ::registerBackingRecord(std::move(backing));
}

SkImageInfo rasterInfo(int32 width, int32 height) {
    return ::rasterInfo(width, height);
}

void markMutated(NativeImageBackingRecord* backing) {
    if (!backing) {
        return;
    }
    ++backing->generation;
    backing->applyColor2AnalysisValid = false;
    backing->opacity = SKIA_IMAGE_OPACITY_UNKNOWN;
}

}

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
        return registerBackingRecord(std::move(backing));
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
        return registerBackingRecord(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_from_owned_rgba_pixels(void* pixels, int32 width, int32 height) {
    if (!pixels || width <= 0 || height <= 0) {
        return 0;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    if (pixelCount > std::numeric_limits<size_t>::max() / 4) {
        return 0;
    }
    std::unique_ptr<uint8_t, decltype(&std::free)> owner(
        static_cast<uint8_t*>(pixels), &std::free);
    const size_t rowBytes = static_cast<size_t>(width) * 4;
    const size_t byteCount = static_cast<size_t>(pixelCount) * 4;
    try {
        sk_sp<SkData> data = SkData::MakeWithProc(pixels, byteCount, releaseMallocPixels, nullptr);
        if (!data) {
            return 0;
        }
        owner.release();
        sk_sp<SkImage> image = SkImage::MakeRasterData(rasterInfo(width, height), data, rowBytes);
        if (!image) {
            return 0;
        }
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->image = std::move(image);
        backing->width = width;
        backing->height = height;
        return registerBackingRecord(std::move(backing));
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

void skia_image_backing_set_opacity(int64_t handle, int32 opacity) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing || (opacity != SKIA_IMAGE_OPACITY_UNKNOWN
        && opacity != SKIA_IMAGE_OPACITY_OPAQUE
        && opacity != SKIA_IMAGE_OPACITY_TRANSLUCENT)) {
        return;
    }
    backing->opacity = opacity;
}

int32 skia_image_backing_opacity(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->opacity : SKIA_IMAGE_OPACITY_UNKNOWN;
}

int skia_image_backing_snapshot_status(int64_t handle, int64_t* snapshotHandle) {
    if (snapshotHandle) {
        *snapshotHandle = 0;
    }
    NativeImageBackingRecord* source = findBacking(handle);
    if (!source) {
        return SKIA_IMAGE_BACKING_SNAPSHOT_INVALID;
    }
    if (failNextSnapshotAllocationForTest) {
        failNextSnapshotAllocationForTest = false;
        return SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE;
    }
    try {
        sk_sp<SkImage> snapshot = source->snapshot();
        if (!snapshot) {
            return SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE;
        }
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->image = std::move(snapshot);
        backing->width = source->width;
        backing->height = source->height;
        backing->opacity = source->opacity;
        const int64_t newHandle = registerBackingRecord(std::move(backing));
        if (newHandle == 0) {
            return SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE;
        }
        if (snapshotHandle) {
            *snapshotHandle = newHandle;
        }
        return SKIA_IMAGE_BACKING_SNAPSHOT_OK;
    } catch (const std::bad_alloc&) {
        return SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE;
    }
}

int64_t skia_image_backing_snapshot(int64_t handle) {
    int64_t snapshotHandle = 0;
    return skia_image_backing_snapshot_status(handle, &snapshotHandle)
        == SKIA_IMAGE_BACKING_SNAPSHOT_OK ? snapshotHandle : 0;
}

void skia_image_backing_fail_next_snapshot_for_test(void) {
    failNextSnapshotAllocationForTest = true;
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
        backing->opacity = source->opacity;
        return registerBackingRecord(std::move(backing));
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
    const int result = drawOnCanvas(target->canvas(), source, srcLeft, srcTop, srcRight, srcBottom,
                                    dstLeft, dstTop, dstRight, dstBottom, alphaMask);
    if (result != 0) {
        skia_image_backing_internal::markMutated(target);
    }
    return result;
}

int32 skia_image_backing_surface_id(int64_t handle) {
    return registerSurfaceAlias(handle);
}

int skia_image_backing_draw_to_surface(int32 targetSurface, int64_t sourceHandle,
                                       float srcLeft, float srcTop, float srcRight, float srcBottom,
                                       float dstLeft, float dstTop, float dstRight, float dstBottom,
                                       int32 alphaMask, int32 optimizationMask) {
    SkCanvas* canvas = skiaGetCanvas(targetSurface);
    NativeImageBackingRecord* source = findBacking(sourceHandle);
    if (tryWritePixels(canvas, source, srcLeft, srcTop, srcRight, srcBottom,
                       dstLeft, dstTop, dstRight, dstBottom, alphaMask, optimizationMask)) {
        skia_image_backing_mark_surface_mutated(targetSurface);
        return 1;
    }
    const int result = drawOnCanvas(canvas, source, srcLeft, srcTop, srcRight, srcBottom,
                                    dstLeft, dstTop, dstRight, dstBottom, alphaMask);
    if (result != 0) {
        skia_image_backing_mark_surface_mutated(targetSurface);
    }
    return result;
}

SkCanvas* skia_image_backing_canvas(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->canvas() : nullptr;
}

SkCanvas* skia_image_backing_canvas_for_surface_id(int32 surfaceId) {
    return canvasForSurfaceAlias(surfaceId);
}

void skia_image_backing_mark_surface_mutated(int32 surfaceId) {
    auto alias = surfaceAliases.find(surfaceId);
    if (alias != surfaceAliases.end()) {
        skia_image_backing_internal::markMutated(findBacking(alias->second));
    }
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

int skia_image_backing_read_argb_rows(int64_t handle, Pixel* output, int32 y, int32 width,
                                      int32 height) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing || !output || y < 0 || width <= 0 || height <= 0
        || width > backing->width || y > backing->height - height) {
        return 0;
    }
    try {
        const size_t rowBytes = static_cast<size_t>(width) * 4;
        std::vector<uint8_t> rgba(rowBytes);
        const SkImageInfo info = rasterInfo(width, 1);
        for (int32 row = 0; row < height; ++row) {
            const int32 sourceY = y + row;
            bool copied = false;
            if (backing->surface) {
                copied = backing->surface->readPixels(info, rgba.data(), rowBytes, 0, sourceY);
            } else if (backing->image) {
                copied = backing->image->readPixels(info, rgba.data(), rowBytes, 0, sourceY);
            }
            if (!copied) {
                return 0;
            }
            Pixel* target = output + static_cast<size_t>(row) * width;
            for (int32 x = 0; x < width; ++x) {
                const uint8_t* pixel = rgba.data() + static_cast<size_t>(x) * 4;
                target[x] = (static_cast<Pixel>(pixel[3]) << 24)
                    | (static_cast<Pixel>(pixel[0]) << 16)
                    | (static_cast<Pixel>(pixel[1]) << 8)
                    | static_cast<Pixel>(pixel[2]);
            }
        }
        return 1;
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

void skia_image_backing_release(int64_t handle) {
    if (handle != 0) {
        auto alias = backingAliases.find(handle);
        if (alias != backingAliases.end()) {
            surfaceAliases.erase(alias->second);
            backingAliases.erase(alias);
        }
        auto found = backings.find(handle);
        if (found != backings.end()) {
            recordBackingReleased(*found->second);
            backings.erase(found);
        }
    }
}

void skia_image_backing_reset_accounting_for_test(void) {
    backingAccountingForTest = true;
    skia_image_backing_clear_accounting_counters_for_test();
}

void skia_image_backing_clear_accounting_counters_for_test(void) {
    backingRecordsCreatedForTest = 0;
    backingRecordsReleasedForTest = 0;
    backingRecordsLiveForTest = 0;
    backingRecordsPeakLiveForTest = 0;
    backingBytesLiveForTest = 0;
    backingBytesPeakLiveForTest = 0;
    if (!backingAccountingForTest) {
        return;
    }
    for (const auto& entry : backings) {
        if (entry.second) {
            ++backingRecordsLiveForTest;
            backingBytesLiveForTest += backingBytes(*entry.second);
        }
    }
    backingRecordsPeakLiveForTest = backingRecordsLiveForTest;
    backingBytesPeakLiveForTest = backingBytesLiveForTest;
    writePixelsAttemptsForTest = 0;
    writePixelsHitsForTest = 0;
    writePixelsFallbacksForTest = 0;
    writePixelsCopiedBytesForTest = 0;
}

void skia_image_backing_set_accounting_for_test(int enabled) {
    backingAccountingForTest = enabled != 0;
    if (!backingAccountingForTest) {
        backingRecordsCreatedForTest = 0;
        backingRecordsReleasedForTest = 0;
        backingRecordsLiveForTest = 0;
        backingRecordsPeakLiveForTest = 0;
        backingBytesLiveForTest = 0;
        backingBytesPeakLiveForTest = 0;
    }
}

uint64_t skia_image_backing_records_created_for_test(void) {
    return backingRecordsCreatedForTest;
}

uint64_t skia_image_backing_records_released_for_test(void) {
    return backingRecordsReleasedForTest;
}

uint64_t skia_image_backing_records_live_for_test(void) {
    return backingRecordsLiveForTest;
}

uint64_t skia_image_backing_records_peak_live_for_test(void) {
    return backingRecordsPeakLiveForTest;
}

uint64_t skia_image_backing_bytes_live_for_test(void) {
    return backingBytesLiveForTest;
}

uint64_t skia_image_backing_bytes_peak_live_for_test(void) {
    return backingBytesPeakLiveForTest;
}

uint64_t skia_image_backing_write_pixels_attempts_for_test(void) {
    return writePixelsAttemptsForTest;
}

uint64_t skia_image_backing_write_pixels_hits_for_test(void) {
    return writePixelsHitsForTest;
}

uint64_t skia_image_backing_write_pixels_fallbacks_for_test(void) {
    return writePixelsFallbacksForTest;
}

uint64_t skia_image_backing_write_pixels_copied_bytes_for_test(void) {
    return writePixelsCopiedBytesForTest;
}
