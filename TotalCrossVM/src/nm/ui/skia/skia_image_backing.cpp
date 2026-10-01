// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"

#include "include/core/SkPixmap.h"

#include <cstddef>
#include <cstdint>
#include <algorithm>
#include <limits>
#include <map>
#include <memory>
#include <new>
#include <vector>

namespace {

using skia_image_backing_internal::NativeImageBackingRecord;
using skia_image_backing_internal::RASTER_VARIANT_HIT;
using skia_image_backing_internal::RASTER_VARIANT_INVALID;
using skia_image_backing_internal::RASTER_VARIANT_MATERIALIZE;
using skia_image_backing_internal::RASTER_VARIANT_MISS;
using skia_image_backing_internal::RASTER_VARIANT_PHYSICAL;
using skia_image_backing_internal::RASTER_VARIANT_TARGET_COLOR;
using skia_image_backing_internal::BackingFormat;

std::map<int64_t, std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord>> backings;
std::map<int32, int64_t> surfaceAliases;
std::map<int64_t, int32> backingAliases;
int64_t nextHandle = 1;
int32 nextSurfaceAlias = std::numeric_limits<int32>::min() + 1;
bool failNextSnapshotAllocationForTest;
bool failNextVariantMaterializationForTest;
bool failNextPromotionForTest;
bool backingAccountingForTest;
uint64_t backingRecordsCreatedForTest;
uint64_t backingRecordsReleasedForTest;
uint64_t backingRecordsLiveForTest;
uint64_t backingRecordsPeakLiveForTest;
uint64_t backingBytesLiveForTest;
uint64_t backingBytesPeakLiveForTest;
uint64_t backingBytesLiveByFormatForTest[4];
uint64_t backingBytesPeakByFormatForTest[4];
uint64_t compactDecodeCountByFormatForTest[4];
uint64_t compactDecodeBytesByFormatForTest[4];
uint64_t compactReadbackCountForTest;
uint64_t compactRowScratchPeakBytesForTest;
uint64_t fullRgbaDecodeTempBytesForTest;
uint64_t promotionAttemptsForTest;
uint64_t promotionSuccessesForTest;
uint64_t promotionFailuresForTest;
uint64_t promotionBytesForTest;

enum BackingTestMetric {
    TEST_METRIC_LIVE_BYTES_BY_FORMAT = 0,
    TEST_METRIC_PEAK_BYTES_BY_FORMAT = 1,
    TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT = 2,
    TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT = 3,
    TEST_METRIC_COMPACT_READBACK_COUNT = 4,
    TEST_METRIC_ROW_SCRATCH_PEAK_BYTES = 5,
    TEST_METRIC_FULL_RGBA_DECODE_TEMP_BYTES = 6,
    TEST_METRIC_PROMOTION_ATTEMPTS = 7,
    TEST_METRIC_PROMOTION_SUCCESSES = 8,
    TEST_METRIC_PROMOTION_FAILURES = 9,
    TEST_METRIC_PROMOTION_BYTES = 10
};

uint64_t backingBytes(const NativeImageBackingRecord& backing) {
    return backing.backingBytes;
}

int backingFormatIndex(BackingFormat format) {
    const int index = static_cast<int>(format);
    return index >= 0 && index < 4 ? index : -1;
}

void recordFormatBytesAdded(BackingFormat format, uint64_t bytes) {
    const int index = backingFormatIndex(format);
    if (index < 0) {
        return;
    }
    backingBytesLiveByFormatForTest[index] += bytes;
    backingBytesPeakByFormatForTest[index] = std::max(
        backingBytesPeakByFormatForTest[index], backingBytesLiveByFormatForTest[index]);
}

void recordFormatBytesRemoved(BackingFormat format, uint64_t bytes) {
    const int index = backingFormatIndex(format);
    if (index < 0) {
        return;
    }
    backingBytesLiveByFormatForTest[index] = backingBytesLiveByFormatForTest[index] >= bytes
        ? backingBytesLiveByFormatForTest[index] - bytes : 0;
}

BackingFormat backingFormatForColorType(SkColorType colorType) {
    switch (colorType) {
        case kRGBA_8888_SkColorType: return BackingFormat::RGBA8888;
        case kRGB_565_SkColorType: return BackingFormat::RGB565;
        case kGray_8_SkColorType: return BackingFormat::GRAY8;
        case kARGB_4444_SkColorType: return BackingFormat::ARGB4444;
        default: return BackingFormat::UNKNOWN;
    }
}

bool captureStorageMetadata(NativeImageBackingRecord* backing) {
    if (!backing || backing->width <= 0 || backing->height <= 0) {
        return false;
    }
    SkPixmap pixmap;
    SkImageInfo info;
    size_t rowBytes = 0;
    if (backing->surface && backing->surface->peekPixels(&pixmap)) {
        info = pixmap.info();
        rowBytes = pixmap.rowBytes();
    } else if (backing->image && backing->image->peekPixels(&pixmap)) {
        info = pixmap.info();
        rowBytes = pixmap.rowBytes();
    } else if (backing->surface) {
        info = backing->surface->imageInfo();
        rowBytes = info.minRowBytes();
    } else if (backing->image) {
        info = backing->image->imageInfo();
        rowBytes = info.minRowBytes();
    } else {
        return false;
    }
    if (rowBytes == 0 || rowBytes < info.minRowBytes()
            || rowBytes > std::numeric_limits<uint64_t>::max()
                / static_cast<uint64_t>(backing->height)) {
        return false;
    }
    backing->format = backingFormatForColorType(info.colorType());
    backing->rowBytes = rowBytes;
    backing->backingBytes = static_cast<uint64_t>(rowBytes) * backing->height;
    return true;
}

SkColorType colorTypeForFormat(int32 format) {
    switch (format) {
        case 0: return kRGBA_8888_SkColorType;
        case 1: return kRGB_565_SkColorType;
        case 2: return kGray_8_SkColorType;
        case 3: return kARGB_4444_SkColorType;
        default: return kUnknown_SkColorType;
    }
}

SkAlphaType alphaTypeForFormat(int32 format) {
    return format == 1 || format == 2 ? kOpaque_SkAlphaType
        : format == 3 ? kPremul_SkAlphaType : kUnpremul_SkAlphaType;
}

void recordBackingCreated(const NativeImageBackingRecord& backing) {
    if (!backingAccountingForTest) {
        return;
    }
    const uint64_t bytes = backingBytes(backing);
    ++backingRecordsCreatedForTest;
    ++backingRecordsLiveForTest;
    backingBytesLiveForTest += bytes;
    recordFormatBytesAdded(backing.format, bytes);
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
    recordFormatBytesRemoved(backing.format, bytes);
    backingBytesLiveForTest = backingBytesLiveForTest >= bytes ? backingBytesLiveForTest - bytes : 0;
}

skia_image_backing_internal::NativeImageBackingRecord* findBacking(int64_t handle) {
    auto found = backings.find(handle);
    return found == backings.end() ? nullptr : found->second.get();
}

int rasterVariantObserveInternal(NativeImageBackingRecord* backing, int32_t kind,
                                 const std::vector<uint32_t>& words, sk_sp<SkImage>* hit,
                                 bool* provenOpaque) {
    if (hit) {
        hit->reset();
    }
    if (provenOpaque) {
        *provenOpaque = false;
    }
    if (!backing || (kind != RASTER_VARIANT_TARGET_COLOR && kind != RASTER_VARIANT_PHYSICAL)) {
        return RASTER_VARIANT_INVALID;
    }
    if (backing->rasterVariantValid && backing->rasterVariant
        && backing->rasterVariantKey.equals(kind, words)) {
        if (backing->rasterVariantPending
            && !backing->rasterVariantPendingKey.equals(kind, words)) {
            backing->rasterVariantPending = false;
            backing->rasterVariantPendingKey.words.clear();
        }
        if (hit) {
            *hit = backing->rasterVariant;
        }
        if (provenOpaque) {
            *provenOpaque = backing->rasterVariantOpaque;
        }
        return RASTER_VARIANT_HIT;
    }
    if (backing->rasterVariantPending && backing->rasterVariantPendingKey.equals(kind, words)) {
        return RASTER_VARIANT_MATERIALIZE;
    }
    try {
        backing->rasterVariantPendingKey.kind = kind;
        backing->rasterVariantPendingKey.words = words;
        backing->rasterVariantPending = true;
        return RASTER_VARIANT_MISS;
    } catch (const std::bad_alloc&) {
        backing->rasterVariantPending = false;
        backing->rasterVariantPendingKey.words.clear();
        return RASTER_VARIANT_INVALID;
    }
}

bool rasterVariantStoreInternal(NativeImageBackingRecord* backing, int32_t kind,
                                const std::vector<uint32_t>& words, sk_sp<SkImage> image,
                                bool provenOpaque) {
    if (!backing || !image || !backing->rasterVariantPending
        || !backing->rasterVariantPendingKey.equals(kind, words)) {
        return false;
    }
    skia_image_backing_internal::RasterVariantKey candidate;
    try {
        candidate.kind = kind;
        candidate.words = words;
    } catch (const std::bad_alloc&) {
        backing->rasterVariantPending = false;
        backing->rasterVariantPendingKey.words.clear();
        return false;
    }
    backing->rasterVariantKey = std::move(candidate);
    backing->rasterVariant = std::move(image);
    backing->rasterVariantOpaque = provenOpaque;
    backing->rasterVariantValid = true;
    backing->rasterVariantPending = false;
    backing->rasterVariantPendingKey.words.clear();
    return true;
}

void rasterVariantFailInternal(NativeImageBackingRecord* backing, int32_t kind,
                               const std::vector<uint32_t>& words) {
    if (backing && backing->rasterVariantPending
        && backing->rasterVariantPendingKey.equals(kind, words)) {
        backing->rasterVariantPending = false;
        backing->rasterVariantPendingKey.words.clear();
    }
}

void rasterVariantClearInternal(NativeImageBackingRecord* backing) {
    if (!backing) {
        return;
    }
    backing->rasterVariant.reset();
    backing->rasterVariantValid = false;
    backing->rasterVariantOpaque = false;
    backing->rasterVariantKey.words.clear();
    backing->rasterVariantPending = false;
    backing->rasterVariantPendingKey.words.clear();
}

int64_t registerBackingRecord(std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord> backing) {
    if (!backing || nextHandle <= 0 || !captureStorageMetadata(backing.get())) {
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

void recordSurfaceMutation(int32 surfaceId) {
    auto alias = surfaceAliases.find(surfaceId);
    if (alias == surfaceAliases.end()) {
        return;
    }
    NativeImageBackingRecord* backing = findBacking(alias->second);
    if (!backing) {
        return;
    }
    ++backing->generation;
    backing->applyColor2AnalysisValid = false;
    skia_image_backing_internal::rasterVariantClear(backing);
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
        x > backing->width - width || y > backing->height - height ||
        static_cast<uint64_t>(width) > std::numeric_limits<size_t>::max() / 4) {
        return false;
    }
    const SkImageInfo info = rasterInfo(width, height);
    const size_t rowBytes = static_cast<size_t>(width) * 4;
    if (backing->surface) {
        return backing->surface->readPixels(info, output, rowBytes, x, y);
    }
    return backing->image && backing->image->readPixels(info, output, rowBytes, x, y);
}

bool readRgba(NativeImageBackingRecord* backing, void* output, int32 x, int32 y,
              int32 width, int32 height) {
    if (!backing || !output || width <= 0 || height <= 0 || x < 0 || y < 0
            || x > backing->width - width || y > backing->height - height) {
        return false;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    if (pixelCount > std::numeric_limits<size_t>::max() / sizeof(Pixel)
            || static_cast<uint64_t>(width) > std::numeric_limits<size_t>::max() / 4) {
        return false;
    }
    try {
        std::vector<uint8_t> rgba(static_cast<size_t>(width) * 4);
        Pixel* pixels = static_cast<Pixel*>(output);
        for (int32 row = 0; row < height; ++row) {
            if (!readRgbaBytes(backing, rgba.data(), x, y + row, width, 1)) {
                return false;
            }
            for (int32 column = 0; column < width; ++column) {
                const uint8_t* pixel = rgba.data() + static_cast<size_t>(column) * 4;
                const size_t outputIndex = static_cast<size_t>(row) * width + column;
                pixels[outputIndex] = (static_cast<Pixel>(pixel[3]) << 24)
                    | (static_cast<Pixel>(pixel[0]) << 16)
                    | (static_cast<Pixel>(pixel[1]) << 8)
                    | static_cast<Pixel>(pixel[2]);
            }
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

bool updateStorageMetadata(NativeImageBackingRecord* backing) {
    if (!backing) {
        return false;
    }
    const uint64_t oldBytes = backing->backingBytes;
    const BackingFormat oldFormat = backing->format;
    if (!::captureStorageMetadata(backing)) {
        return false;
    }
    if (backingAccountingForTest) {
        if (backing->backingBytes >= oldBytes) {
            backingBytesLiveForTest += backing->backingBytes - oldBytes;
        } else {
            backingBytesLiveForTest -= std::min(backingBytesLiveForTest, oldBytes - backing->backingBytes);
        }
        if (oldFormat != backing->format) {
            recordFormatBytesRemoved(oldFormat, oldBytes);
            recordFormatBytesAdded(backing->format, backing->backingBytes);
        } else {
            const int index = backingFormatIndex(backing->format);
            if (index >= 0) {
                if (backing->backingBytes >= oldBytes) {
                    backingBytesLiveByFormatForTest[index] += backing->backingBytes - oldBytes;
                } else {
                    backingBytesLiveByFormatForTest[index] -= std::min(
                        backingBytesLiveByFormatForTest[index], oldBytes - backing->backingBytes);
                }
                backingBytesPeakByFormatForTest[index] = std::max(
                    backingBytesPeakByFormatForTest[index], backingBytesLiveByFormatForTest[index]);
            }
        }
        backingBytesPeakLiveForTest = std::max(backingBytesPeakLiveForTest, backingBytesLiveForTest);
    }
    return true;
}

SkImageInfo rasterInfo(int32 width, int32 height) {
    return ::rasterInfo(width, height);
}

int rasterVariantObserve(NativeImageBackingRecord* backing, int32_t kind,
                         const std::vector<uint32_t>& words, sk_sp<SkImage>* hit,
                         bool* provenOpaque) {
    return ::rasterVariantObserveInternal(backing, kind, words, hit, provenOpaque);
}

bool rasterVariantStore(NativeImageBackingRecord* backing, int32_t kind,
                        const std::vector<uint32_t>& words, sk_sp<SkImage> image,
                        bool provenOpaque) {
    return ::rasterVariantStoreInternal(backing, kind, words, std::move(image), provenOpaque);
}

void rasterVariantFail(NativeImageBackingRecord* backing, int32_t kind,
                       const std::vector<uint32_t>& words) {
    ::rasterVariantFailInternal(backing, kind, words);
}

void rasterVariantClear(NativeImageBackingRecord* backing) {
    ::rasterVariantClearInternal(backing);
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

int64_t skia_image_backing_create_empty_with_format(int32 width, int32 height, int32 format) {
    const SkColorType colorType = colorTypeForFormat(format);
    if (width <= 0 || height <= 0 || colorType == kUnknown_SkColorType) {
        return 0;
    }
    try {
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        const SkImageInfo info = SkImageInfo::Make(width, height, colorType, alphaTypeForFormat(format));
        backing->surface = SkSurface::MakeRaster(info);
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

int skia_image_backing_compact_storage_available(void) {
    const SkImageInfo grayInfo = SkImageInfo::Make(1, 1, kGray_8_SkColorType, kOpaque_SkAlphaType);
    const SkImageInfo rgb565Info = SkImageInfo::Make(1, 1, kRGB_565_SkColorType, kOpaque_SkAlphaType);
    const SkImageInfo argb4444Info = SkImageInfo::Make(1, 1, kARGB_4444_SkColorType, kPremul_SkAlphaType);
    sk_sp<SkSurface> gray = SkSurface::MakeRaster(grayInfo);
    sk_sp<SkSurface> rgb565 = SkSurface::MakeRaster(rgb565Info);
    sk_sp<SkSurface> argb4444 = SkSurface::MakeRaster(argb4444Info);
    return gray && rgb565 && argb4444 ? 1 : 0;
}

int skia_image_backing_finish_decode(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing || !backing->surface || backing->image) {
        return 0;
    }
    sk_sp<SkImage> image = backing->surface->makeImageSnapshot();
    if (!image) {
        return 0;
    }
    backing->image = std::move(image);
    backing->surface.reset();
    return skia_image_backing_internal::updateStorageMetadata(backing) ? 1 : 0;
}

int32 skia_image_backing_format(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? static_cast<int32>(backing->format) : -1;
}

int32 skia_image_backing_row_bytes(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing && backing->rowBytes <= static_cast<size_t>(std::numeric_limits<int32>::max())
        ? static_cast<int32>(backing->rowBytes) : -1;
}

int64_t skia_image_backing_byte_count(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing && backing->backingBytes <= static_cast<uint64_t>(std::numeric_limits<int64_t>::max())
        ? static_cast<int64_t>(backing->backingBytes) : -1;
}

int64_t skia_image_backing_create_empty_with_color_type_for_test(int32 width, int32 height,
                                                                  int32 colorType) {
    if (width <= 0 || height <= 0) {
        return 0;
    }
    SkColorType targetType = kUnknown_SkColorType;
    SkAlphaType alphaType = kUnpremul_SkAlphaType;
    switch (colorType) {
        case 0: targetType = kRGBA_8888_SkColorType; break;
        case 1: targetType = kBGRA_8888_SkColorType; break;
        case 2: targetType = kRGB_565_SkColorType; alphaType = kOpaque_SkAlphaType; break;
        case 3: targetType = kAlpha_8_SkColorType; alphaType = kPremul_SkAlphaType; break;
        default: return 0;
    }
    try {
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        const SkImageInfo info = SkImageInfo::Make(width, height, targetType, alphaType);
        backing->surface = SkSurface::MakeRaster(info);
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

int32 skia_image_backing_color_type_for_test(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    sk_sp<SkImage> image = backing ? backing->snapshot() : nullptr;
    return image ? static_cast<int32>(image->colorType()) : static_cast<int32>(kUnknown_SkColorType);
}

int skia_image_backing_skew_surface_for_test(int32 surfaceId, float skewX, float skewY) {
    SkCanvas* target = canvasForSurfaceAlias(surfaceId);
    if (!target) {
        return 0;
    }
    target->skew(skewX, skewY);
    return 1;
}

void skia_image_backing_fail_next_variant_materialization_for_test(void) {
    failNextVariantMaterializationForTest = true;
}

bool skia_image_backing_consume_variant_materialization_failure_for_test(void) {
    const bool fail = failNextVariantMaterializationForTest;
    failNextVariantMaterializationForTest = false;
    return fail;
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

void skia_image_backing_invalidate_variants(int64_t handle) {
    skia_image_backing_internal::rasterVariantClear(skia_image_backing_internal::findBacking(handle));
}

int skia_image_backing_variant_observe_for_test(int64_t handle, int32 kind,
                                                const uint32_t* words, int32 wordCount) {
    skia_image_backing_internal::NativeImageBackingRecord* backing =
        skia_image_backing_internal::findBacking(handle);
    if (!backing || !words || wordCount <= 0) {
        return skia_image_backing_internal::RASTER_VARIANT_INVALID;
    }
    try {
        const std::vector<uint32_t> key(words, words + wordCount);
        const int decision = skia_image_backing_internal::rasterVariantObserve(backing, kind, key, nullptr, nullptr);
        if (decision == skia_image_backing_internal::RASTER_VARIANT_MATERIALIZE) {
            sk_sp<SkImage> source = backing->snapshot();
            if (!source) {
                skia_image_backing_internal::rasterVariantFail(backing, kind, key);
                return skia_image_backing_internal::RASTER_VARIANT_INVALID;
            }
            sk_sp<SkSurface> copy = SkSurface::MakeRaster(source->imageInfo());
            if (!copy) {
                skia_image_backing_internal::rasterVariantFail(backing, kind, key);
                return skia_image_backing_internal::RASTER_VARIANT_INVALID;
            }
            copy->getCanvas()->drawImage(source.get(), 0, 0);
            sk_sp<SkImage> derived = copy->makeImageSnapshot();
            if (!derived) {
                skia_image_backing_internal::rasterVariantFail(backing, kind, key);
                return skia_image_backing_internal::RASTER_VARIANT_INVALID;
            }
            if (!skia_image_backing_internal::rasterVariantStore(backing, kind, key,
                    std::move(derived), false)) {
                return skia_image_backing_internal::RASTER_VARIANT_INVALID;
            }
        }
        return decision;
    } catch (const std::bad_alloc&) {
        return skia_image_backing_internal::RASTER_VARIANT_INVALID;
    }
}

int32 skia_image_backing_variant_state_for_test(int64_t handle) {
    skia_image_backing_internal::NativeImageBackingRecord* backing =
        skia_image_backing_internal::findBacking(handle);
    if (!backing) {
        return 0;
    }
    return (backing->rasterVariantValid && backing->rasterVariant ? 1 : 0)
        | (backing->rasterVariantPending ? 2 : 0);
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
    const bool compactPromotion = backing->format != BackingFormat::RGBA8888;
    if (compactPromotion && backingAccountingForTest) {
        ++promotionAttemptsForTest;
    }
    if (compactPromotion && failNextPromotionForTest) {
        failNextPromotionForTest = false;
        if (backingAccountingForTest) {
            ++promotionFailuresForTest;
        }
        return 0;
    }
    bool committed = false;
    try {
        sk_sp<SkImage> previousImage = backing->image;
        sk_sp<SkSurface> surface = SkSurface::MakeRaster(rasterInfo(backing->width, backing->height));
        if (!surface) {
            goto failed;
        }
        surface->getCanvas()->drawImage(previousImage, 0, 0);
        backing->surface = std::move(surface);
        backing->image.reset();
        if (!skia_image_backing_internal::updateStorageMetadata(backing)) {
            backing->surface.reset();
            backing->image = std::move(previousImage);
            skia_image_backing_internal::updateStorageMetadata(backing);
            goto failed;
        }
        if (compactPromotion) {
            ++backing->generation;
            backing->applyColor2AnalysisValid = false;
            rasterVariantClearInternal(backing);
            if (backingAccountingForTest) {
                ++promotionSuccessesForTest;
                promotionBytesForTest += backing->backingBytes;
            }
        }
        committed = true;
    } catch (const std::bad_alloc&) {
        goto failed;
    }
    return committed ? 1 : 0;

failed:
    if (compactPromotion && backingAccountingForTest) {
        ++promotionFailuresForTest;
    }
    return 0;
}

int skia_image_backing_is_compact(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing && backing->format != BackingFormat::RGBA8888
        && backing->format != BackingFormat::UNKNOWN;
}

void skia_image_backing_fail_next_promotion_for_test(void) {
    failNextPromotionForTest = true;
}

int skia_image_backing_write_rgba_pixels(int64_t handle, const uint8_t* pixels, int32 x, int32 y,
                                         int32 width, int32 height, int32 rowBytes) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing || !pixels || width <= 0 || height <= 0 || x < 0 || y < 0
        || x > backing->width - width || y > backing->height - height
        || rowBytes < static_cast<int64_t>(width) * 4) {
        return 0;
    }
    if (!skia_image_backing_make_mutable(handle)) {
        return 0;
    }
    const SkPixmap source(rasterInfo(width, height), pixels, static_cast<size_t>(rowBytes));
    if (!backing->surface->getCanvas()->writePixels(source.info(), source.addr(),
                                                     source.rowBytes(), x, y)) {
        return 0;
    }
    ++backing->generation;
    backing->applyColor2AnalysisValid = false;
    return 1;
}

int skia_image_backing_write_gray_pixels(int64_t handle, const uint8_t* pixels, int32 x, int32 y,
                                         int32 width, int32 height, int32 rowBytes) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing || !backing->surface || backing->format != BackingFormat::GRAY8 || !pixels
        || width <= 0 || height <= 0 || x < 0 || y < 0
        || x > backing->width - width || y > backing->height - height || rowBytes < width) {
        return 0;
    }
    const SkImageInfo info = SkImageInfo::Make(width, height, kGray_8_SkColorType, kOpaque_SkAlphaType);
    if (!backing->surface->getCanvas()->writePixels(info, pixels, static_cast<size_t>(rowBytes), x, y)) {
        return 0;
    }
    ++backing->generation;
    backing->applyColor2AnalysisValid = false;
    return 1;
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
        ++target->generation;
        target->applyColor2AnalysisValid = false;
    }
    return result;
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

void skia_image_backing_record_surface_mutation(int32 surfaceId) {
    recordSurfaceMutation(surfaceId);
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
    NativeImageBackingRecord* backing = findBacking(handle);
    const bool read = readRgba(backing, output, x, y, width, height);
    if (read && backing && backingAccountingForTest && backingFormatIndex(backing->format) > 0) {
        ++compactReadbackCountForTest;
    }
    return read ? 1 : 0;
}

int skia_image_backing_read_row(int64_t handle, void* output, int32 y, int32 width) {
    return skia_image_backing_read_pixels(handle, output, 0, y, width, 1);
}

int skia_image_backing_read_rgba_row(int64_t handle, void* output, int32 y, int32 width) {
    NativeImageBackingRecord* backing = findBacking(handle);
    const bool read = readRgbaBytes(backing, output, 0, y, width, 1);
    if (read && backing && backingAccountingForTest && backingFormatIndex(backing->format) > 0) {
        ++compactReadbackCountForTest;
    }
    return read ? 1 : 0;
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
    backingRecordsCreatedForTest = 0;
    backingRecordsReleasedForTest = 0;
    backingRecordsLiveForTest = 0;
    backingRecordsPeakLiveForTest = 0;
    backingBytesLiveForTest = 0;
    backingBytesPeakLiveForTest = 0;
    compactReadbackCountForTest = 0;
    compactRowScratchPeakBytesForTest = 0;
    fullRgbaDecodeTempBytesForTest = 0;
    promotionAttemptsForTest = 0;
    promotionSuccessesForTest = 0;
    promotionFailuresForTest = 0;
    promotionBytesForTest = 0;
    for (int i = 0; i < 4; ++i) {
        backingBytesLiveByFormatForTest[i] = 0;
        backingBytesPeakByFormatForTest[i] = 0;
        compactDecodeCountByFormatForTest[i] = 0;
        compactDecodeBytesByFormatForTest[i] = 0;
    }
    for (const auto& entry : backings) {
        if (entry.second) {
            ++backingRecordsLiveForTest;
            backingBytesLiveForTest += backingBytes(*entry.second);
            recordFormatBytesAdded(entry.second->format, backingBytes(*entry.second));
        }
    }
    backingRecordsPeakLiveForTest = backingRecordsLiveForTest;
    backingBytesPeakLiveForTest = backingBytesLiveForTest;
    for (int i = 0; i < 4; ++i) {
        backingBytesPeakByFormatForTest[i] = backingBytesLiveByFormatForTest[i];
    }
    backingAccountingForTest = true;
}

void skia_image_backing_record_decode_scratch_for_test(uint64_t rowScratchBytes,
                                                        uint64_t fullRgbaTempBytes) {
    if (!backingAccountingForTest) {
        return;
    }
    compactRowScratchPeakBytesForTest = std::max(compactRowScratchPeakBytesForTest, rowScratchBytes);
    fullRgbaDecodeTempBytesForTest += fullRgbaTempBytes;
}

void skia_image_backing_record_compact_decode_for_test(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    const int index = backing ? backingFormatIndex(backing->format) : -1;
    if (!backingAccountingForTest || !backing || index <= 0) {
        return;
    }
    ++compactDecodeCountByFormatForTest[index];
    compactDecodeBytesByFormatForTest[index] += backing->backingBytes;
}

uint64_t skia_image_backing_test_metric(int32 metric, int32 format) {
    if (format < 0 || format >= 4) {
        format = 0;
    }
    switch (metric) {
        case TEST_METRIC_LIVE_BYTES_BY_FORMAT: return backingBytesLiveByFormatForTest[format];
        case TEST_METRIC_PEAK_BYTES_BY_FORMAT: return backingBytesPeakByFormatForTest[format];
        case TEST_METRIC_COMPACT_DECODE_COUNT_BY_FORMAT: return compactDecodeCountByFormatForTest[format];
        case TEST_METRIC_COMPACT_DECODE_BYTES_BY_FORMAT: return compactDecodeBytesByFormatForTest[format];
        case TEST_METRIC_COMPACT_READBACK_COUNT: return compactReadbackCountForTest;
        case TEST_METRIC_ROW_SCRATCH_PEAK_BYTES: return compactRowScratchPeakBytesForTest;
        case TEST_METRIC_FULL_RGBA_DECODE_TEMP_BYTES: return fullRgbaDecodeTempBytesForTest;
        case TEST_METRIC_PROMOTION_ATTEMPTS: return promotionAttemptsForTest;
        case TEST_METRIC_PROMOTION_SUCCESSES: return promotionSuccessesForTest;
        case TEST_METRIC_PROMOTION_FAILURES: return promotionFailuresForTest;
        case TEST_METRIC_PROMOTION_BYTES: return promotionBytesForTest;
        default: return 0;
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
