// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"
#include "skia_image_geometry_internal.h"

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
#include <iterator>
#include <set>
#include <vector>

namespace {

using skia_image_backing_internal::NativeImageBackingRecord;

std::map<int64_t, std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord>> backings;
std::map<int32, int64_t> surfaceAliases;
std::map<int64_t, int32> backingAliases;
int64_t nextHandle = 1;
int32 nextSurfaceAlias = std::numeric_limits<int32>::min() + 1;
bool failNextSnapshotAllocationForTest;
bool failNextPromotionAllocationForTest;
bool failNextDetachedAdoptionForTest;
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
uint64_t writePixelsRegularAttemptsForTest;
uint64_t writePixelsRegularHitsForTest;
uint64_t writePixelsRegularFallbacksForTest;
uint64_t writePixelsRegularCopiedBytesForTest;
uint64_t writePixelsRegularClippedHitsForTest;
uint64_t writePixelsRejectInvalidTargetOrSourceForTest;
uint64_t writePixelsRejectAlphaMaskForTest;
uint64_t writePixelsRejectMatrixForTest;
uint64_t writePixelsRejectSaveCountForTest;
uint64_t writePixelsRejectSourceRectForTest;
uint64_t writePixelsRejectSizeMismatchForTest;
uint64_t writePixelsRejectFractionalDestinationForTest;
uint64_t writePixelsRejectDestinationBoundsForTest;
uint64_t writePixelsRejectOpacityForTest;
uint64_t writePixelsRejectSourcePixelsForTest;
uint64_t writePixelsRejectWriteFailureForTest;
uint64_t writePixelsDeviceOneToOneCandidatesForTest;
uint64_t writePixelsDeviceOneToOneKnownOpaqueCandidatesForTest;
uint64_t genericGeometryDrawsForTest;
uint64_t smoothResampleDrawsForTest;
uint64_t physicalIdentityAttemptsForTest;
uint64_t physicalIdentityHitsForTest;
uint64_t physicalIdentityFallbacksForTest;
uint64_t physicalIdentityResamplesAvoidedForTest;
uint64_t physicalIdentityRejectionsForTest[SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST];
uint64_t targetColorAttemptsForTest;
uint64_t targetColorMaterializationsForTest;
uint64_t targetColorHitsForTest;
uint64_t targetColorFallbacksForTest;
uint64_t targetColorConvertedBytesForTest;
uint64_t physicalVariantLookupsForTest;
uint64_t physicalVariantHitsForTest;
uint64_t physicalVariantMissesForTest;
uint64_t physicalVariantMaterializationsForTest;
uint64_t physicalVariantEvictionsForTest;
uint64_t physicalVariantBytesForTest;
std::set<uint64_t> targetColorUniqueSourcesForTest;
std::set<uint64_t> targetColorUniqueFullKeysForTest;
std::set<uint64_t> targetColorUniqueNoDestinationKeysForTest;
std::set<uint64_t> targetColorUniqueIntrinsicKeysForTest;
uint64_t targetColorPendingReplacementsForTest;
uint64_t targetColorRejectionsForTest[SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST];
std::set<uint64_t> physicalVariantUniqueFullKeysForTest;
std::set<uint64_t> physicalVariantUniqueNoSurfaceSizeKeysForTest;
uint64_t physicalVariantPendingReplacementsForTest;
uint64_t physicalVariantRejectionsForTest[SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST];
uint64_t sharedSlotTargetToPhysicalForTest;
uint64_t sharedSlotPhysicalToTargetForTest;
uint64_t sharedPendingTargetToPhysicalForTest;
uint64_t sharedPendingPhysicalToTargetForTest;
uint64_t targetColorSaveCountBucketsForTest[6];
uint64_t physicalVariantSaveCountBucketsForTest[6];
uint64_t physicalIdentitySaveCountBucketsForTest[6];
uint64_t targetColorMappingSubreasonsForTest[
    SKIA_RASTER_MAPPING_REJECT_REASON_COUNT_FOR_TEST];
uint64_t physicalVariantMappingSubreasonsForTest[
    SKIA_RASTER_MAPPING_REJECT_REASON_COUNT_FOR_TEST];
uint64_t physicalIdentityMappingSubreasonsForTest[
    SKIA_RASTER_MAPPING_REJECT_REASON_COUNT_FOR_TEST];
uint64_t backingBytesLiveByFormatForTest[4];
uint64_t backingBytesPeakByFormatForTest[4];
uint64_t compactDirectDecodeCountForTest;
uint64_t compactDirectDecodeBytesForTest;
uint64_t temporaryRgbaDecodeBytesForTest;
uint64_t compactReadbackCountForTest;
uint64_t compactRowScratchPeakBytesForTest;
uint64_t promotionAttemptsForTest;
uint64_t promotionSuccessesForTest;
uint64_t promotionFailuresForTest;
uint64_t promotionBytesForTest;

uint64_t diagnosticHash(uint64_t hash, uint64_t value) {
    hash ^= value + UINT64_C(0x9e3779b97f4a7c15) + (hash << 6) + (hash >> 2);
    return hash;
}

uint64_t rasterVariantKeyHash(const skia_image_backing_internal::RasterVariantKey& key,
                              bool omitDestination, bool omitSurfaceSize) {
    uint64_t hash = UINT64_C(0xcbf29ce484222325);
    const uint64_t values[] = {
        key.sourceGeneration, key.sourceDecodeGeneration,
        static_cast<uint64_t>(static_cast<int64_t>(key.sourceLeft)),
        static_cast<uint64_t>(static_cast<int64_t>(key.sourceTop)),
        static_cast<uint64_t>(static_cast<int64_t>(key.sourceRight)),
        static_cast<uint64_t>(static_cast<int64_t>(key.sourceBottom)),
        omitDestination ? 0 : static_cast<uint64_t>(static_cast<int64_t>(key.destinationLeft)),
        omitDestination ? 0 : static_cast<uint64_t>(static_cast<int64_t>(key.destinationTop)),
        omitDestination ? 0 : static_cast<uint64_t>(static_cast<int64_t>(key.destinationRight)),
        omitDestination ? 0 : static_cast<uint64_t>(static_cast<int64_t>(key.destinationBottom)),
        omitSurfaceSize ? 0 : static_cast<uint64_t>(static_cast<int64_t>(key.targetWidth)),
        omitSurfaceSize ? 0 : static_cast<uint64_t>(static_cast<int64_t>(key.targetHeight)),
        static_cast<uint64_t>(static_cast<int64_t>(key.targetColorType)), key.kind,
    };
    for (uint64_t value : values) {
        hash = diagnosticHash(hash, value);
    }
    for (uint64_t value : key.geometrySignature) {
        hash = diagnosticHash(hash, value);
    }
    return hash;
}

uint64_t rasterVariantIntrinsicKeyHash(
        const skia_image_backing_internal::RasterVariantKey& key) {
    uint64_t hash = UINT64_C(0xcbf29ce484222325);
    hash = diagnosticHash(hash, key.sourceGeneration);
    hash = diagnosticHash(hash, key.sourceDecodeGeneration);
    hash = diagnosticHash(hash,
                          static_cast<uint64_t>(static_cast<int64_t>(key.targetColorType)));
    return hash;
}

void recordRasterVariantIdentityForTest(
        skia_image_backing_internal::NativeImageBackingRecord* source,
        const skia_image_backing_internal::RasterVariantKey& key) {
    if (!backingAccountingForTest || !source) {
        return;
    }
    const bool physical = key.kind == skia_image_backing_internal::RASTER_VARIANT_PHYSICAL;
    if (physical) {
        physicalVariantUniqueFullKeysForTest.insert(rasterVariantKeyHash(key, false, false));
        physicalVariantUniqueNoSurfaceSizeKeysForTest.insert(
            rasterVariantKeyHash(key, false, true));
    } else {
        targetColorUniqueSourcesForTest.insert(diagnosticHash(
            diagnosticHash(reinterpret_cast<uintptr_t>(source), key.sourceGeneration),
            key.sourceDecodeGeneration));
        targetColorUniqueFullKeysForTest.insert(rasterVariantKeyHash(key, false, false));
        targetColorUniqueNoDestinationKeysForTest.insert(
            rasterVariantKeyHash(key, true, false));
        targetColorUniqueIntrinsicKeysForTest.insert(rasterVariantIntrinsicKeyHash(key));
    }
}

size_t bytesPerPixel(ImageBackingFormat format) {
    switch (format) {
    case IMAGE_BACKING_FORMAT_RGB565:
    case IMAGE_BACKING_FORMAT_ARGB4444:
        return 2;
    case IMAGE_BACKING_FORMAT_GRAY8:
        return 1;
    case IMAGE_BACKING_FORMAT_RGBA8888:
    default:
        return 4;
    }
}

bool isCompact(ImageBackingFormat format) {
    return format != IMAGE_BACKING_FORMAT_RGBA8888;
}

uint64_t backingBytes(const NativeImageBackingRecord& backing) {
    const size_t rowBytes = backing.rowBytes != 0
        ? backing.rowBytes
        : static_cast<size_t>(backing.width) * bytesPerPixel(backing.format);
    return static_cast<uint64_t>(rowBytes) * static_cast<uint64_t>(backing.height);
}

void recordBackingCreated(const NativeImageBackingRecord& backing) {
    if (!backingAccountingForTest) {
        return;
    }
    const uint64_t bytes = backingBytes(backing);
    const int32 format = static_cast<int32>(backing.format);
    ++backingRecordsCreatedForTest;
    ++backingRecordsLiveForTest;
    backingBytesLiveForTest += bytes;
    if (format >= 0 && format < 4) {
        backingBytesLiveByFormatForTest[format] += bytes;
        backingBytesPeakByFormatForTest[format] = std::max(
            backingBytesPeakByFormatForTest[format], backingBytesLiveByFormatForTest[format]);
    }
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
    const int32 format = static_cast<int32>(backing.format);
    backingBytesLiveForTest = backingBytesLiveForTest >= bytes ? backingBytesLiveForTest - bytes : 0;
    if (format >= 0 && format < 4) {
        backingBytesLiveByFormatForTest[format] = backingBytesLiveByFormatForTest[format] >= bytes
            ? backingBytesLiveByFormatForTest[format] - bytes : 0;
    }
}

void recordWritePixelsAttemptForTest(bool regular) {
    if (!backingAccountingForTest) {
        return;
    }
    ++writePixelsAttemptsForTest;
    if (regular) {
        ++writePixelsRegularAttemptsForTest;
    }
}

void recordWritePixelsFallbackForTest(bool regular) {
    if (!backingAccountingForTest) {
        return;
    }
    ++writePixelsFallbacksForTest;
    if (regular) {
        ++writePixelsRegularFallbacksForTest;
    }
}

void recordWritePixelsHitForTest(bool regular, bool clipped, uint64_t copiedBytes) {
    if (!backingAccountingForTest) {
        return;
    }
    ++writePixelsHitsForTest;
    if (regular) {
        ++writePixelsRegularHitsForTest;
        if (clipped) {
            ++writePixelsRegularClippedHitsForTest;
        }
    }
    writePixelsCopiedBytesForTest += copiedBytes;
    if (regular) {
        writePixelsRegularCopiedBytesForTest += copiedBytes;
    }
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

bool proveOpaqueWithoutSnapshot(NativeImageBackingRecord* source) {
    if (!source) {
        return false;
    }
    if (source->opacity == SKIA_IMAGE_OPACITY_OPAQUE) {
        return true;
    }
    if (source->opacity == SKIA_IMAGE_OPACITY_TRANSLUCENT) {
        return false;
    }
    if (source->format != IMAGE_BACKING_FORMAT_RGBA8888) {
        return false;
    }
    SkPixmap pixmap;
    const bool peeked = source->image
        ? source->image->peekPixels(&pixmap)
        : source->surface && source->surface->peekPixels(&pixmap);
    if (!peeked) {
        return false;
    }
    for (int32 y = 0; y < source->height; ++y) {
        const uint8_t* row = static_cast<const uint8_t*>(pixmap.addr(0, y));
        if (!row) {
            return false;
        }
        for (int32 x = 0; x < source->width; ++x) {
            if (row[static_cast<size_t>(x) * 4 + 3] != 0xff) {
                source->opacity = SKIA_IMAGE_OPACITY_TRANSLUCENT;
                return false;
            }
        }
    }
    source->opacity = SKIA_IMAGE_OPACITY_OPAQUE;
    return true;
}

bool readRgbaBytes(NativeImageBackingRecord* backing, void* output, int32 x, int32 y,
                   int32 width, int32 height);

SkImageInfo rasterInfo(int32 width, int32 height,
                       ImageBackingFormat format = IMAGE_BACKING_FORMAT_RGBA8888);

bool integralWritePixelsCoordinate(float value) {
    return std::isfinite(value) && std::floor(value) == value;
}

struct WritePixelsDeviceCopyPlan {
    SkIRect sourcePixels;
    SkIRect destinationPixels;
    bool clipped;
};

enum class WritePixelsRejectReason {
    InvalidTargetOrSource,
    AlphaMask,
    Matrix,
    SourceRect,
    SizeMismatch,
    FractionalDestination,
    DestinationBounds,
};

bool rejectWritePixelsPlan(WritePixelsRejectReason reason) {
    if (!backingAccountingForTest) {
        return false;
    }
    switch (reason) {
    case WritePixelsRejectReason::InvalidTargetOrSource:
        ++writePixelsRejectInvalidTargetOrSourceForTest;
        break;
    case WritePixelsRejectReason::AlphaMask:
        ++writePixelsRejectAlphaMaskForTest;
        break;
    case WritePixelsRejectReason::Matrix:
        ++writePixelsRejectMatrixForTest;
        break;
    case WritePixelsRejectReason::SourceRect:
        ++writePixelsRejectSourceRectForTest;
        break;
    case WritePixelsRejectReason::SizeMismatch:
        ++writePixelsRejectSizeMismatchForTest;
        break;
    case WritePixelsRejectReason::FractionalDestination:
        ++writePixelsRejectFractionalDestinationForTest;
        break;
    case WritePixelsRejectReason::DestinationBounds:
        ++writePixelsRejectDestinationBoundsForTest;
        break;
    }
    return false;
}

bool buildWritePixelsDeviceCopyPlan(
    SkCanvas* targetCanvas, bool validSource, int32 sourceWidth, int32 sourceHeight,
    float srcLeft, float srcTop, float srcRight, float srcBottom,
    float dstLeft, float dstTop, float dstRight, float dstBottom,
    int32 alphaMask, bool knownOpaque, WritePixelsDeviceCopyPlan* plan) {
    if (!targetCanvas || !validSource || sourceWidth <= 0 || sourceHeight <= 0 || !plan) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::InvalidTargetOrSource);
    }
    if (alphaMask != 255) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::AlphaMask);
    }
    if (!integralWritePixelsCoordinate(srcLeft) || !integralWritePixelsCoordinate(srcTop)
        || !integralWritePixelsCoordinate(srcRight) || !integralWritePixelsCoordinate(srcBottom)
        || srcRight <= srcLeft || srcBottom <= srcTop
        || srcLeft < 0 || srcTop < 0 || srcRight > sourceWidth || srcBottom > sourceHeight) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::SourceRect);
    }

    const SkMatrix matrix = targetCanvas->getTotalMatrix();
    if (matrix.hasPerspective() || matrix.getSkewX() != 0 || matrix.getSkewY() != 0
        || !std::isfinite(matrix.getScaleX()) || !std::isfinite(matrix.getScaleY())
        || !std::isfinite(matrix.getTranslateX()) || !std::isfinite(matrix.getTranslateY())
        || matrix.getScaleX() <= 0 || matrix.getScaleY() <= 0) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::Matrix);
    }

    const float deviceLeft = matrix.getScaleX() * dstLeft + matrix.getTranslateX();
    const float deviceTop = matrix.getScaleY() * dstTop + matrix.getTranslateY();
    const float deviceRight = matrix.getScaleX() * dstRight + matrix.getTranslateX();
    const float deviceBottom = matrix.getScaleY() * dstBottom + matrix.getTranslateY();
    if (!integralWritePixelsCoordinate(deviceLeft) || !integralWritePixelsCoordinate(deviceTop)
        || !integralWritePixelsCoordinate(deviceRight)
        || !integralWritePixelsCoordinate(deviceBottom)) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::FractionalDestination);
    }

    const SkIRect sourceRect = SkIRect::MakeLTRB(
        static_cast<int32>(srcLeft), static_cast<int32>(srcTop),
        static_cast<int32>(srcRight), static_cast<int32>(srcBottom));
    const SkIRect mappedDestination = SkIRect::MakeLTRB(
        static_cast<int32>(deviceLeft), static_cast<int32>(deviceTop),
        static_cast<int32>(deviceRight), static_cast<int32>(deviceBottom));
    if (mappedDestination.width() != sourceRect.width()
        || mappedDestination.height() != sourceRect.height()) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::SizeMismatch);
    }

    SkIRect deviceClip;
    SkPixmap targetPixels;
    if (!targetCanvas->getDeviceClipBounds(&deviceClip) || !targetCanvas->peekPixels(&targetPixels)) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::DestinationBounds);
    }
    SkIRect visibleDestination = mappedDestination;
    if (!visibleDestination.intersect(deviceClip)
        || !visibleDestination.intersect(SkIRect::MakeWH(targetPixels.width(), targetPixels.height()))) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::DestinationBounds);
    }

    const int32 sourceOffsetX = visibleDestination.left() - mappedDestination.left();
    const int32 sourceOffsetY = visibleDestination.top() - mappedDestination.top();
    const SkIRect visibleSource = SkIRect::MakeLTRB(
        sourceRect.left() + sourceOffsetX, sourceRect.top() + sourceOffsetY,
        sourceRect.left() + sourceOffsetX + visibleDestination.width(),
        sourceRect.top() + sourceOffsetY + visibleDestination.height());
    if (!sourceRect.contains(visibleSource)
        || visibleSource.width() != visibleDestination.width()
        || visibleSource.height() != visibleDestination.height()) {
        return rejectWritePixelsPlan(WritePixelsRejectReason::SourceRect);
    }

    plan->sourcePixels = visibleSource;
    plan->destinationPixels = visibleDestination;
    plan->clipped = visibleDestination.left() != mappedDestination.left()
        || visibleDestination.top() != mappedDestination.top()
        || visibleDestination.right() != mappedDestination.right()
        || visibleDestination.bottom() != mappedDestination.bottom();
    if (backingAccountingForTest) {
        ++writePixelsDeviceOneToOneCandidatesForTest;
        if (knownOpaque) {
            ++writePixelsDeviceOneToOneKnownOpaqueCandidatesForTest;
        }
    }
    return true;
}

int tryWritePixelsImage(SkCanvas* targetCanvas, const SkImage* image, int32 width, int32 height,
                        bool sourceOpaque, float srcLeft, float srcTop, float srcRight,
                        float srcBottom, float dstLeft, float dstTop, float dstRight,
                        float dstBottom, int32 alphaMask, int32 optimizationMask) {
#if TC_GRAPHICS_SOFTWARE
    constexpr int32 kOpaqueWritePixelsBit = 1 << 2;
    if ((optimizationMask & kOpaqueWritePixelsBit) == 0) {
        return 0;
    }
    recordWritePixelsAttemptForTest(true);
    auto fallback = []() {
        recordWritePixelsFallbackForTest(true);
        return 0;
    };
    WritePixelsDeviceCopyPlan plan;
    if (!buildWritePixelsDeviceCopyPlan(
            targetCanvas, image != nullptr, width, height, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, alphaMask, sourceOpaque, &plan)) {
        return fallback();
    }
    if (!sourceOpaque) {
        if (backingAccountingForTest) {
            ++writePixelsRejectOpacityForTest;
        }
        return fallback();
    }
    SkPixmap pixmap;
    SkPixmap subset;
    if (!image->peekPixels(&pixmap)
        || !pixmap.extractSubset(&subset, plan.sourcePixels)) {
        if (backingAccountingForTest) {
            ++writePixelsRejectSourcePixelsForTest;
        }
        return fallback();
    }
    if (!targetCanvas->writePixels(subset.info(), subset.addr(), subset.rowBytes(),
                                   plan.destinationPixels.left(), plan.destinationPixels.top())) {
        if (backingAccountingForTest) {
            ++writePixelsRejectWriteFailureForTest;
        }
        return fallback();
    }
    const uint64_t copiedBytes = static_cast<uint64_t>(plan.sourcePixels.width())
        * static_cast<uint64_t>(plan.sourcePixels.height()) * 4;
    recordWritePixelsHitForTest(true, plan.clipped, copiedBytes);
    return 1;
#else
    UNUSED(targetCanvas)
    UNUSED(image)
    UNUSED(width)
    UNUSED(height)
    UNUSED(sourceOpaque)
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
    return 0;
#endif
}

int tryWritePixels(SkCanvas* targetCanvas, NativeImageBackingRecord* source,
                    float srcLeft, float srcTop, float srcRight, float srcBottom,
                    float dstLeft, float dstTop, float dstRight, float dstBottom,
                    int32 alphaMask, int32 optimizationMask) {
#if TC_GRAPHICS_SOFTWARE
    constexpr int32 kOpaqueWritePixelsBit = 1 << 2;
    if ((optimizationMask & kOpaqueWritePixelsBit) == 0) {
        return 0;
    }
    recordWritePixelsAttemptForTest(true);
    auto fallback = []() {
        recordWritePixelsFallbackForTest(true);
        return 0;
    };
    WritePixelsDeviceCopyPlan plan;
    if (!buildWritePixelsDeviceCopyPlan(
            targetCanvas, source != nullptr, source ? source->width : 0,
            source ? source->height : 0, srcLeft, srcTop, srcRight, srcBottom,
            dstLeft, dstTop, dstRight, dstBottom, alphaMask,
            source && source->opacity == SKIA_IMAGE_OPACITY_OPAQUE, &plan)) {
        return fallback();
    }
    if (source->format == IMAGE_BACKING_FORMAT_RGBA8888) {
        if (!proveOpaqueForWritePixels(source)) {
            if (backingAccountingForTest) {
                ++writePixelsRejectOpacityForTest;
            }
            return fallback();
        }
    } else if (source->format == IMAGE_BACKING_FORMAT_ARGB4444
            && source->opacity != SKIA_IMAGE_OPACITY_OPAQUE) {
        if (backingAccountingForTest) {
            ++writePixelsRejectOpacityForTest;
        }
        return fallback();
    }
    if (source->format != IMAGE_BACKING_FORMAT_RGBA8888) {
        try {
            const size_t rowBytes = static_cast<size_t>(plan.sourcePixels.width()) * 4;
            std::vector<uint8_t> rgba(rowBytes);
            const SkImageInfo info = rasterInfo(plan.sourcePixels.width(), 1,
                IMAGE_BACKING_FORMAT_RGBA8888);
            for (int32 row = 0; row < plan.sourcePixels.height(); ++row) {
                if (!readRgbaBytes(source, rgba.data(), plan.sourcePixels.left(),
                                   plan.sourcePixels.top() + row, plan.sourcePixels.width(), 1)) {
                    if (backingAccountingForTest) {
                        ++writePixelsRejectSourcePixelsForTest;
                    }
                    return fallback();
                }
                if (!targetCanvas->writePixels(info, rgba.data(), rowBytes,
                                               plan.destinationPixels.left(),
                                               plan.destinationPixels.top() + row)) {
                    if (backingAccountingForTest) {
                        ++writePixelsRejectWriteFailureForTest;
                    }
                    return fallback();
                }
            }
            const uint64_t copiedBytes = static_cast<uint64_t>(plan.sourcePixels.width())
                * static_cast<uint64_t>(plan.sourcePixels.height()) * 4;
            recordWritePixelsHitForTest(true, plan.clipped, copiedBytes);
            return 1;
        } catch (const std::bad_alloc&) {
            return fallback();
        }
    }
    sk_sp<SkImage> image = source->snapshot();
    SkPixmap pixmap;
    SkPixmap subset;
    if (!image || !image->peekPixels(&pixmap)
        || !pixmap.extractSubset(&subset, plan.sourcePixels)) {
        if (backingAccountingForTest) {
            ++writePixelsRejectSourcePixelsForTest;
        }
        return fallback();
    }
    if (!targetCanvas->writePixels(subset.info(), subset.addr(), subset.rowBytes(),
                                   plan.destinationPixels.left(), plan.destinationPixels.top())) {
        if (backingAccountingForTest) {
            ++writePixelsRejectWriteFailureForTest;
        }
        return fallback();
    }
    const uint64_t copiedBytes = static_cast<uint64_t>(plan.sourcePixels.width())
        * static_cast<uint64_t>(plan.sourcePixels.height()) * 4;
    recordWritePixelsHitForTest(true, plan.clipped, copiedBytes);
    return 1;
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
    return 0;
#endif
}

int tryDirectImageCopy(SkCanvas* targetCanvas, const SkImage* image,
                       int32 sourceLeft, int32 sourceTop, int32 sourceRight, int32 sourceBottom,
                       int32 destinationLeft, int32 destinationTop, int32 destinationRight,
                       int32 destinationBottom, bool sourceOpaque, int32 alphaMask,
                       int32 optimizationMask) {
#if TC_GRAPHICS_SOFTWARE
    constexpr int32 kOpaqueWritePixelsBit = 1 << 2;
    if ((optimizationMask & kOpaqueWritePixelsBit) == 0) {
        return 0;
    }
    recordWritePixelsAttemptForTest(false);
    auto fallback = []() {
        recordWritePixelsFallbackForTest(false);
        return 0;
    };
    const int32 width = sourceRight - sourceLeft;
    const int32 height = sourceBottom - sourceTop;
    if (!targetCanvas || !image || !sourceOpaque || alphaMask != 255 || width <= 0 || height <= 0
        || width != destinationRight - destinationLeft
        || height != destinationBottom - destinationTop
        || sourceLeft < 0 || sourceTop < 0 || sourceRight > image->width()
        || sourceBottom > image->height()) {
        return fallback();
    }
    SkPixmap targetPixels;
    if (!targetCanvas->peekPixels(&targetPixels)
        || destinationLeft < 0 || destinationTop < 0
        || destinationRight > targetPixels.width() || destinationBottom > targetPixels.height()) {
        return fallback();
    }
    SkPixmap pixmap;
    SkPixmap subset;
    const SkIRect sourceRect = SkIRect::MakeLTRB(sourceLeft, sourceTop, sourceRight, sourceBottom);
    if (!image->peekPixels(&pixmap) || !pixmap.extractSubset(&subset, sourceRect)
        || !targetCanvas->writePixels(subset.info(), subset.addr(), subset.rowBytes(),
                                      destinationLeft, destinationTop)) {
        return fallback();
    }
    recordWritePixelsHitForTest(false, false, static_cast<uint64_t>(width)
        * static_cast<uint64_t>(height) * 4);
    return 1;
#else
    UNUSED(targetCanvas)
    UNUSED(image)
    UNUSED(sourceLeft)
    UNUSED(sourceTop)
    UNUSED(sourceRight)
    UNUSED(sourceBottom)
    UNUSED(destinationLeft)
    UNUSED(destinationTop)
    UNUSED(destinationRight)
    UNUSED(destinationBottom)
    UNUSED(sourceOpaque)
    UNUSED(alphaMask)
    UNUSED(optimizationMask)
    return 0;
#endif
}

int tryDirectPhysicalCopy(SkCanvas* targetCanvas, NativeImageBackingRecord* source,
                          int32 sourceLeft, int32 sourceTop, int32 sourceRight, int32 sourceBottom,
                          int32 destinationLeft, int32 destinationTop, int32 destinationRight,
                          int32 destinationBottom, int32 alphaMask, int32 optimizationMask) {
#if TC_GRAPHICS_SOFTWARE
    constexpr int32 kOpaqueWritePixelsBit = 1 << 2;
    constexpr int32 kTargetColorConversionBit = 1 << 13;
    if ((optimizationMask & kOpaqueWritePixelsBit) == 0) {
        return 0;
    }
    recordWritePixelsAttemptForTest(false);
    auto fallback = []() {
        recordWritePixelsFallbackForTest(false);
        return 0;
    };
    const int32 width = sourceRight - sourceLeft;
    const int32 height = sourceBottom - sourceTop;
    if (!targetCanvas || !source || alphaMask != 255 || width <= 0 || height <= 0
        || width != destinationRight - destinationLeft
        || height != destinationBottom - destinationTop
        || sourceLeft < 0 || sourceTop < 0 || sourceRight > source->width
        || sourceBottom > source->height || !std::isfinite(static_cast<float>(destinationLeft))
        || !std::isfinite(static_cast<float>(destinationTop))) {
        return fallback();
    }
    if (source->format != IMAGE_BACKING_FORMAT_RGBA8888
        && (optimizationMask & kTargetColorConversionBit) == 0) {
        return fallback();
    }
    if (!proveOpaqueWithoutSnapshot(source)) {
        return fallback();
    }
    SkPixmap targetPixels;
    if (!targetCanvas->peekPixels(&targetPixels)
        || destinationLeft < 0 || destinationTop < 0
        || destinationRight > targetPixels.width() || destinationBottom > targetPixels.height()) {
        return fallback();
    }
    try {
        if (source->format == IMAGE_BACKING_FORMAT_RGBA8888) {
            SkPixmap pixmap;
            SkPixmap subset;
            const SkIRect sourceRect = SkIRect::MakeLTRB(sourceLeft, sourceTop,
                                                         sourceRight, sourceBottom);
            const bool peeked = source->image
                ? source->image->peekPixels(&pixmap)
                : source->surface && source->surface->peekPixels(&pixmap);
            if (!peeked
                || !pixmap.extractSubset(&subset, sourceRect)
                || !targetCanvas->writePixels(subset.info(), subset.addr(), subset.rowBytes(),
                                              destinationLeft, destinationTop)) {
                return fallback();
            }
        } else {
            const size_t rowBytes = static_cast<size_t>(width) * 4;
            std::vector<uint8_t> rgba(rowBytes);
            const SkImageInfo info = rasterInfo(width, 1, IMAGE_BACKING_FORMAT_RGBA8888);
            for (int32 row = 0; row < height; ++row) {
                if (!readRgbaBytes(source, rgba.data(), sourceLeft, sourceTop + row, width, 1)
                    || !targetCanvas->writePixels(info, rgba.data(), rowBytes,
                                                  destinationLeft, destinationTop + row)) {
                    return fallback();
                }
            }
        }
        recordWritePixelsHitForTest(false, false, static_cast<uint64_t>(width)
            * static_cast<uint64_t>(height) * 4);
        return 1;
    } catch (const std::bad_alloc&) {
        return fallback();
    }
#else
    UNUSED(targetCanvas)
    UNUSED(source)
    UNUSED(sourceLeft)
    UNUSED(sourceTop)
    UNUSED(sourceRight)
    UNUSED(sourceBottom)
    UNUSED(destinationLeft)
    UNUSED(destinationTop)
    UNUSED(destinationRight)
    UNUSED(destinationBottom)
    UNUSED(alphaMask)
    UNUSED(optimizationMask)
    return 0;
#endif
}

static size_t rasterVariantBytes(SkColorType colorType, int32 width, int32 height) {
    const size_t bytesPerPixel = colorType == kRGB_565_SkColorType ? 2 : 4;
    return static_cast<size_t>(width) * static_cast<size_t>(height) * bytesPerPixel;
}

static bool rasterImageOpaque(const SkImage* image) {
    if (!image) {
        return false;
    }
    if (image->isOpaque()) {
        return true;
    }
    SkPixmap pixmap;
    if (!image->peekPixels(&pixmap)) {
        return false;
    }
    const SkColorType colorType = pixmap.colorType();
    if (colorType == kRGB_565_SkColorType || colorType == kGray_8_SkColorType) {
        return true;
    }
    if (colorType != kRGBA_8888_SkColorType && colorType != kBGRA_8888_SkColorType) {
        return false;
    }
    for (int32 y = 0; y < pixmap.height(); ++y) {
        const uint8_t* row = static_cast<const uint8_t*>(pixmap.addr(0, y));
        if (!row) {
            return false;
        }
        for (int32 x = 0; x < pixmap.width(); ++x) {
            if (row[static_cast<size_t>(x) * 4 + 3] != 0xff) {
                return false;
            }
        }
    }
    return true;
}

static sk_sp<SkImage> makeTargetColorVariant(NativeImageBackingRecord* source,
                                               SkColorType targetColorType) {
    if (!source || source->width <= 0 || source->height <= 0) {
        return nullptr;
    }
    sk_sp<SkImage> image = source->snapshot();
    if (!image) {
        return nullptr;
    }
    const SkAlphaType alphaType = targetColorType == kRGB_565_SkColorType
        ? kOpaque_SkAlphaType : kUnpremul_SkAlphaType;
    const SkImageInfo info = SkImageInfo::Make(source->width, source->height, targetColorType,
                                               alphaType);
    sk_sp<SkSurface> surface = SkSurface::MakeRaster(info);
    if (!surface) {
        return nullptr;
    }
    SkPaint paint;
    paint.setFilterQuality(kNone_SkFilterQuality);
    surface->getCanvas()->drawImage(image, 0, 0, &paint);
    return surface->makeImageSnapshot();
}

SkImageInfo rasterInfo(int32 width, int32 height, ImageBackingFormat format) {
    switch (format) {
    case IMAGE_BACKING_FORMAT_RGB565:
        return SkImageInfo::Make(width, height, kRGB_565_SkColorType, kOpaque_SkAlphaType);
    case IMAGE_BACKING_FORMAT_GRAY8:
        return SkImageInfo::Make(width, height, kGray_8_SkColorType, kOpaque_SkAlphaType);
    case IMAGE_BACKING_FORMAT_ARGB4444:
        return SkImageInfo::Make(width, height, kARGB_4444_SkColorType, kPremul_SkAlphaType);
    case IMAGE_BACKING_FORMAT_RGBA8888:
    default:
        return SkImageInfo::Make(width, height, kRGBA_8888_SkColorType, kUnpremul_SkAlphaType);
}
}

void releaseMallocPixels(const void* pixels, void*) {
    std::free(const_cast<void*>(pixels));
}

static int64_t detachedHandle(
    std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord> backing) {
    return backing ? reinterpret_cast<int64_t>(backing.release()) : 0;
}

static skia_image_backing_internal::NativeImageBackingRecord* detachedRecord(int64_t handle) {
    return handle == 0 ? nullptr : reinterpret_cast<skia_image_backing_internal::NativeImageBackingRecord*>(
        static_cast<uintptr_t>(handle));
}

static int64_t createFromRgbaPixelsImpl(void* pixels, int32 width, int32 height, bool detached) {
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
        sk_sp<SkData> data = SkData::MakeWithProc(pixels, byteCount, releaseMallocPixels, nullptr);
        sk_sp<SkImage> image = SkImage::MakeRasterData(rasterInfo(width, height), data, rowBytes);
        if (!image) {
            return 0;
        }
        std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord> backing(
            new skia_image_backing_internal::NativeImageBackingRecord());
        backing->image = std::move(image);
        backing->width = width;
        backing->height = height;
        backing->rowBytes = rowBytes;
        return detached ? detachedHandle(std::move(backing)) : registerBackingRecord(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

static int64_t createFromOwnedPixelsImpl(void* pixels, int32 width, int32 height,
                                         ImageBackingFormat format, bool detached) {
    if (!pixels || width <= 0 || height <= 0) {
        return 0;
    }
    if (format < IMAGE_BACKING_FORMAT_RGBA8888 || format > IMAGE_BACKING_FORMAT_ARGB4444) {
        return 0;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(width) * static_cast<uint64_t>(height);
    const size_t pixelBytes = bytesPerPixel(format);
    if (pixelCount > std::numeric_limits<size_t>::max() / pixelBytes) {
        return 0;
    }
    std::unique_ptr<uint8_t, decltype(&std::free)> owner(
        static_cast<uint8_t*>(pixels), &std::free);
    const size_t rowBytes = static_cast<size_t>(width) * pixelBytes;
    const size_t byteCount = static_cast<size_t>(pixelCount) * pixelBytes;
    try {
        sk_sp<SkData> data = SkData::MakeWithProc(pixels, byteCount, releaseMallocPixels, nullptr);
        if (!data) {
            return 0;
        }
        owner.release();
        sk_sp<SkImage> image = SkImage::MakeRasterData(rasterInfo(width, height, format), data, rowBytes);
        if (!image) {
            return 0;
        }
        std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord> backing(
            new skia_image_backing_internal::NativeImageBackingRecord());
        backing->image = std::move(image);
        backing->width = width;
        backing->height = height;
        backing->format = format;
        backing->rowBytes = rowBytes;
        return detached ? detachedHandle(std::move(backing)) : registerBackingRecord(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
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
    if (backingAccountingForTest && isCompact(backing->format)) {
        ++compactReadbackCountForTest;
        compactRowScratchPeakBytesForTest = std::max(
            compactRowScratchPeakBytesForTest, static_cast<uint64_t>(width) * 4);
    }

    const SkImageInfo info = rasterInfo(width, height);
    const size_t rowBytes = static_cast<size_t>(width) * 4;
    if (backing->surface) {
        return backing->surface->readPixels(info, output, rowBytes, x, y);
    }
    if (backing->image) {
        return backing->image->readPixels(info, output, rowBytes, x, y);
    }
    return false;
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
    if (backing && isCompact(backing->format)) {
        try {
            std::vector<uint8_t> row(static_cast<size_t>(width) * 4);
            Pixel* pixels = static_cast<Pixel*>(output);
            for (int32 currentRow = 0; currentRow < height; ++currentRow) {
                if (!readRgbaBytes(backing, row.data(), x, y + currentRow, width, 1)) {
                    return false;
                }
                for (int32 column = 0; column < width; ++column) {
                    const uint8_t* pixel = row.data() + static_cast<size_t>(column) * 4;
                    pixels[static_cast<size_t>(currentRow) * width + column] =
                        (static_cast<Pixel>(pixel[3]) << 24)
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

int skia_image_backing_try_write_pixels(void* targetCanvas, int64_t sourceHandle,
                                        float srcLeft, float srcTop, float srcRight,
                                        float srcBottom, float dstLeft, float dstTop,
                                        float dstRight, float dstBottom, int32 alphaMask,
                                        int32 optimizationMask) {
    return tryWritePixels(static_cast<SkCanvas*>(targetCanvas), findBacking(sourceHandle),
                          srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight,
                          dstBottom, alphaMask, optimizationMask);
}

namespace skia_image_backing_internal {

NativeImageBackingRecord* findBacking(int64_t handle) {
    return ::findBacking(handle);
}

int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing) {
    return ::registerBackingRecord(std::move(backing));
}

SkImageInfo rasterInfo(int32 width, int32 height, ImageBackingFormat format) {
    return ::rasterInfo(width, height, format);
}

int tryWritePixelsImage(SkCanvas* targetCanvas, const SkImage* image, int32 width, int32 height,
                        bool sourceOpaque, float srcLeft, float srcTop, float srcRight,
                        float srcBottom, float dstLeft, float dstTop, float dstRight,
                        float dstBottom, int32 alphaMask, int32 optimizationMask) {
    return ::tryWritePixelsImage(targetCanvas, image, width, height, sourceOpaque, srcLeft,
                                 srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight,
                                 dstBottom, alphaMask, optimizationMask);
}

int tryDirectImageCopy(SkCanvas* targetCanvas, const SkImage* image,
                       int32 sourceLeft, int32 sourceTop, int32 sourceRight, int32 sourceBottom,
                       int32 destinationLeft, int32 destinationTop, int32 destinationRight,
                       int32 destinationBottom, bool sourceOpaque, int32 alphaMask,
                       int32 optimizationMask) {
    return ::tryDirectImageCopy(targetCanvas, image, sourceLeft, sourceTop, sourceRight,
                                sourceBottom, destinationLeft, destinationTop, destinationRight,
                                destinationBottom, sourceOpaque, alphaMask, optimizationMask);
}

int tryDirectPhysicalCopy(SkCanvas* targetCanvas, NativeImageBackingRecord* source,
                          int32 sourceLeft, int32 sourceTop, int32 sourceRight, int32 sourceBottom,
                          int32 destinationLeft, int32 destinationTop, int32 destinationRight,
                          int32 destinationBottom, int32 alphaMask, int32 optimizationMask) {
    return ::tryDirectPhysicalCopy(targetCanvas, source, sourceLeft, sourceTop, sourceRight,
                                   sourceBottom, destinationLeft, destinationTop, destinationRight,
                                   destinationBottom, alphaMask, optimizationMask);
}

bool proveOpaque(NativeImageBackingRecord* source) {
    return ::proveOpaqueForWritePixels(source);
}

void recordTargetColorAttemptForTest() {
    if (backingAccountingForTest) {
        ++targetColorAttemptsForTest;
    }
}

void recordTargetColorFallbackForTest() {
    if (backingAccountingForTest) {
        ++targetColorFallbacksForTest;
    }
}

void recordPhysicalVariantEvictionForTest() {
    if (backingAccountingForTest) {
        ++physicalVariantEvictionsForTest;
    }
}

void recordTargetColorRejectionForTest(int32 reason) {
    if (backingAccountingForTest && reason >= 0
        && reason < SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST) {
        ++targetColorRejectionsForTest[reason];
    }
}

void recordPhysicalVariantRejectionForTest(int32 reason) {
    if (backingAccountingForTest && reason >= 0
        && reason < SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST) {
        ++physicalVariantRejectionsForTest[reason];
    }
}

static void recordSaveCountForTest(uint64_t* buckets, int32 saveCount) {
    if (!backingAccountingForTest || !buckets) {
        return;
    }
    const int32 bucket = saveCount <= 0 ? 0 : saveCount >= 5 ? 5 : saveCount;
    ++buckets[bucket];
}

void recordTargetColorSaveCountForTest(int32 saveCount) {
    recordSaveCountForTest(targetColorSaveCountBucketsForTest, saveCount);
}

void recordPhysicalVariantSaveCountForTest(int32 saveCount) {
    recordSaveCountForTest(physicalVariantSaveCountBucketsForTest, saveCount);
}

void recordPhysicalIdentitySaveCountForTest(int32 saveCount) {
    recordSaveCountForTest(physicalIdentitySaveCountBucketsForTest, saveCount);
}

static void recordMappingSubreasonForTest(uint64_t* counters, int32 reason) {
    if (backingAccountingForTest && counters && reason >= 0
        && reason < SKIA_RASTER_MAPPING_REJECT_REASON_COUNT_FOR_TEST) {
        ++counters[reason];
    }
}

void recordTargetColorMappingSubreasonForTest(int32 reason) {
    recordMappingSubreasonForTest(targetColorMappingSubreasonsForTest, reason);
}

void recordPhysicalVariantMappingSubreasonForTest(int32 reason) {
    recordMappingSubreasonForTest(physicalVariantMappingSubreasonsForTest, reason);
}

void recordPhysicalIdentityMappingSubreasonForTest(int32 reason) {
    recordMappingSubreasonForTest(physicalIdentityMappingSubreasonsForTest, reason);
}

static SkImageInfo testRasterInfo(int32 width, int32 height, int32 colorType) {
    SkColorType skColorType = kUnknown_SkColorType;
    SkAlphaType alphaType = kUnpremul_SkAlphaType;
    if (colorType == SKIA_TEST_COLOR_RGBA8888) {
        skColorType = kRGBA_8888_SkColorType;
    } else if (colorType == SKIA_TEST_COLOR_BGRA8888) {
        skColorType = kBGRA_8888_SkColorType;
    } else if (colorType == SKIA_TEST_COLOR_RGB565) {
        skColorType = kRGB_565_SkColorType;
        alphaType = kOpaque_SkAlphaType;
    }
    return SkImageInfo::Make(width, height, skColorType, alphaType);
}

void markMutated(NativeImageBackingRecord* backing) {
    if (!backing) {
        return;
    }
    ++backing->generation;
    backing->applyColor2AnalysisValid = false;
    backing->opacity = SKIA_IMAGE_OPACITY_UNKNOWN;
    clearRasterVariant(backing);
}

void clearRasterVariant(NativeImageBackingRecord* backing) {
    if (!backing) {
        return;
    }
    backing->rasterVariant.image.reset();
    backing->rasterVariant.opaque = false;
    backing->rasterVariant.valid = false;
    backing->pendingRasterVariant = false;
    backing->pendingRasterVariantObservations = 0;
}

template<typename Materializer>
RasterVariantUse acquireVariant(NativeImageBackingRecord* source, const RasterVariantKey& key,
                                sk_sp<SkImage>* image, Materializer materializer) {
    if (image) {
        image->reset();
    }
    if (!source || !image) {
        return RASTER_VARIANT_FAILED;
    }
    const bool physical = key.kind == RASTER_VARIANT_PHYSICAL;
    if (backingAccountingForTest) {
        recordRasterVariantIdentityForTest(source, key);
    }
    if (backingAccountingForTest && physical) {
        ++physicalVariantLookupsForTest;
    }
    if (source->rasterVariant.valid && source->rasterVariant.key == key) {
        *image = source->rasterVariant.image;
        if (backingAccountingForTest) {
            if (physical) {
                ++physicalVariantHitsForTest;
            } else {
                ++targetColorHitsForTest;
            }
        }
        return *image ? RASTER_VARIANT_HIT : RASTER_VARIANT_FAILED;
    }
    if (backingAccountingForTest && physical) {
        ++physicalVariantMissesForTest;
    }

    if (!source->pendingRasterVariant || !(source->pendingRasterVariantKey == key)) {
        if (backingAccountingForTest && source->pendingRasterVariant) {
            const bool pendingPhysical = source->pendingRasterVariantKey.kind
                == RASTER_VARIANT_PHYSICAL;
            if (physical) {
                ++physicalVariantPendingReplacementsForTest;
            } else {
                ++targetColorPendingReplacementsForTest;
            }
            if (pendingPhysical && !physical) {
                ++sharedPendingPhysicalToTargetForTest;
            } else if (!pendingPhysical && physical) {
                ++sharedPendingTargetToPhysicalForTest;
            }
        }
        source->pendingRasterVariant = true;
        source->pendingRasterVariantKey = key;
        source->pendingRasterVariantObservations = 1;
        return RASTER_VARIANT_OBSERVED;
    }
    sk_sp<SkImage> candidate = materializer();
    if (!candidate) {
        source->pendingRasterVariant = false;
        source->pendingRasterVariantObservations = 0;
        return RASTER_VARIANT_FAILED;
    }
    if (backingAccountingForTest && source->rasterVariant.valid) {
        const bool previousPhysical = source->rasterVariant.key.kind == RASTER_VARIANT_PHYSICAL;
        if (physical && previousPhysical) {
            ++physicalVariantEvictionsForTest;
        }
        if (previousPhysical && !physical) {
            ++sharedSlotPhysicalToTargetForTest;
        } else if (!previousPhysical && physical) {
            ++sharedSlotTargetToPhysicalForTest;
        }
    }
    source->rasterVariant.image = std::move(candidate);
    source->rasterVariant.key = key;
    source->rasterVariant.opaque = rasterImageOpaque(source->rasterVariant.image.get());
    source->rasterVariant.valid = true;
    source->pendingRasterVariant = false;
    source->pendingRasterVariantObservations = 0;
    const size_t bytes = rasterVariantBytes(key.targetColorType == kRGB_565_SkColorType
        ? kRGB_565_SkColorType : static_cast<SkColorType>(key.targetColorType),
        source->rasterVariant.image->width(), source->rasterVariant.image->height());
    if (backingAccountingForTest) {
        if (physical) {
            ++physicalVariantMaterializationsForTest;
            physicalVariantBytesForTest += bytes;
        } else {
            ++targetColorMaterializationsForTest;
            targetColorConvertedBytesForTest += bytes;
        }
    }
    *image = source->rasterVariant.image;
    return *image ? RASTER_VARIANT_MATERIALIZED : RASTER_VARIANT_FAILED;
}

RasterVariantUse acquireTargetColorVariant(NativeImageBackingRecord* source,
                                           const RasterVariantKey& key, SkColorType targetColorType,
                                           sk_sp<SkImage>* image) {
    return acquireVariant(source, key, image,
        [source, targetColorType]() {
            return makeTargetColorVariant(source, targetColorType);
        });
}

RasterVariantUse acquirePhysicalVariant(NativeImageBackingRecord* source,
                                         const RasterVariantKey& key,
                                         const SkiaImageDrawPlanData* plan,
                                         SkColorType targetColorType, sk_sp<SkImage>* image) {
    return acquireVariant(source, key, image,
        [plan, targetColorType]() {
            return skia_image_backing_materialize_geometry_variant(plan, targetColorType,
                                                                    nullptr, nullptr);
        });
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
        backing->rowBytes = backing->surface->imageInfo().minRowBytes();
        return registerBackingRecord(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_empty_for_test(int32 width, int32 height, int32 colorType) {
    if (width <= 0 || height <= 0 || colorType < SKIA_TEST_COLOR_RGBA8888
        || colorType > SKIA_TEST_COLOR_RGB565) {
        return 0;
    }
    try {
        std::unique_ptr<NativeImageBackingRecord> backing(new NativeImageBackingRecord());
        backing->surface = SkSurface::MakeRaster(
            skia_image_backing_internal::testRasterInfo(width, height, colorType));
        if (!backing->surface) {
            return 0;
        }
        backing->width = width;
        backing->height = height;
        if (colorType == SKIA_TEST_COLOR_RGB565) {
            backing->opacity = SKIA_IMAGE_OPACITY_OPAQUE;
        }
        return registerBackingRecord(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_from_rgba_pixels(void* pixels, int32 width, int32 height) {
    return createFromRgbaPixelsImpl(pixels, width, height, false);
}

int64_t skia_image_backing_create_from_owned_pixels(void* pixels, int32 width, int32 height,
                                                    ImageBackingFormat format) {
    return createFromOwnedPixelsImpl(pixels, width, height, format, false);
}

int64_t skia_image_backing_create_from_owned_rgba_pixels(void* pixels, int32 width, int32 height) {
    return createFromOwnedPixelsImpl(pixels, width, height,
        IMAGE_BACKING_FORMAT_RGBA8888, false);
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
        return createFromRgbaPixelsImpl(owned, width, height, false);
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_detached_from_owned_pixels(void* pixels, int32 width, int32 height,
                                                             ImageBackingFormat format) {
    return createFromOwnedPixelsImpl(pixels, width, height, format, true);
}

int64_t skia_image_backing_create_detached_from_owned_rgba_pixels(void* pixels, int32 width, int32 height) {
    return createFromOwnedPixelsImpl(pixels, width, height,
        IMAGE_BACKING_FORMAT_RGBA8888, true);
}

int64_t skia_image_backing_create_detached_from_argb_pixels(const void* pixels, int32 width, int32 height) {
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
        return createFromRgbaPixelsImpl(owned, width, height, true);
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_adopt_detached(int64_t handle) {
    skia_image_backing_internal::NativeImageBackingRecord* raw = detachedRecord(handle);
    if (!raw) {
        return 0;
    }
    std::unique_ptr<skia_image_backing_internal::NativeImageBackingRecord> backing(raw);
    if (failNextDetachedAdoptionForTest) {
        failNextDetachedAdoptionForTest = false;
        return 0;
    }
    return registerBackingRecord(std::move(backing));
}

void skia_image_backing_release_detached(int64_t handle) {
    delete detachedRecord(handle);
}

void skia_image_backing_set_detached_opacity(int64_t handle, int32 opacity) {
    skia_image_backing_internal::NativeImageBackingRecord* backing = detachedRecord(handle);
    if (!backing || (opacity != SKIA_IMAGE_OPACITY_UNKNOWN
        && opacity != SKIA_IMAGE_OPACITY_OPAQUE
        && opacity != SKIA_IMAGE_OPACITY_TRANSLUCENT)) {
        return;
    }
    backing->opacity = opacity;
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
        backing->format = source->format;
        backing->rowBytes = source->rowBytes;
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

void skia_image_backing_fail_next_promotion_for_test(void) {
    failNextPromotionAllocationForTest = true;
}

void skia_image_backing_fail_next_adoption_for_test(void) {
    failNextDetachedAdoptionForTest = true;
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
        const bool compact = isCompact(backing->format);
        if (backingAccountingForTest && compact) {
            ++promotionAttemptsForTest;
        }
        sk_sp<SkSurface> surface = SkSurface::MakeRaster(
            rasterInfo(backing->width, backing->height, IMAGE_BACKING_FORMAT_RGBA8888));
        if (!surface) {
            if (backingAccountingForTest && compact) {
                ++promotionFailuresForTest;
            }
            return 0;
        }
        if (compact) {
            const size_t rowBytes = static_cast<size_t>(backing->width) * 4;
            std::vector<uint8_t> rgba(rowBytes);
            const SkImageInfo info = rasterInfo(backing->width, 1,
                IMAGE_BACKING_FORMAT_RGBA8888);
            for (int32 row = 0; row < backing->height; ++row) {
                if (!readRgbaBytes(backing, rgba.data(), 0, row, backing->width, 1)) {
                    if (backingAccountingForTest) {
                        ++promotionFailuresForTest;
                    }
                    return 0;
                }
                surface->writePixels(SkPixmap(info, rgba.data(), rowBytes), 0, row);
            }
        } else {
            surface->getCanvas()->drawImage(backing->image, 0, 0);
        }
        if (compact && failNextPromotionAllocationForTest) {
            failNextPromotionAllocationForTest = false;
            if (backingAccountingForTest) {
                ++promotionFailuresForTest;
            }
            return 0;
        }
        if (compact) {
            const uint64_t oldBytes = backingBytes(*backing);
            const size_t newRowBytes = surface->imageInfo().minRowBytes();
            const uint64_t newBytes = static_cast<uint64_t>(newRowBytes)
                * static_cast<uint64_t>(backing->height);
            if (backingAccountingForTest) {
                backingBytesLiveForTest = backingBytesLiveForTest >= oldBytes
                    ? backingBytesLiveForTest - oldBytes : 0;
                backingBytesLiveForTest += newBytes;
                const int32 oldFormat = static_cast<int32>(backing->format);
                if (oldFormat >= 0 && oldFormat < 4) {
                    backingBytesLiveByFormatForTest[oldFormat] =
                        backingBytesLiveByFormatForTest[oldFormat] >= oldBytes
                        ? backingBytesLiveByFormatForTest[oldFormat] - oldBytes : 0;
                }
                backingBytesLiveByFormatForTest[IMAGE_BACKING_FORMAT_RGBA8888] += newBytes;
                backingBytesPeakLiveForTest = std::max(backingBytesPeakLiveForTest, backingBytesLiveForTest);
                backingBytesPeakByFormatForTest[IMAGE_BACKING_FORMAT_RGBA8888] = std::max(
                    backingBytesPeakByFormatForTest[IMAGE_BACKING_FORMAT_RGBA8888],
                    backingBytesLiveByFormatForTest[IMAGE_BACKING_FORMAT_RGBA8888]);
            }
            backing->format = IMAGE_BACKING_FORMAT_RGBA8888;
            backing->rowBytes = newRowBytes;
            if (backingAccountingForTest) {
                promotionBytesForTest += newBytes;
                ++promotionSuccessesForTest;
            }
        }
        backing->surface = std::move(surface);
        backing->image.reset();
        return 1;
    } catch (const std::bad_alloc&) {
        if (isCompact(backing->format)) {
            if (backingAccountingForTest) {
                ++promotionFailuresForTest;
            }
        }
        return 0;
    }
}

int skia_image_backing_mutate_for_test(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    if (!backing || !skia_image_backing_make_mutable(handle)) {
        return 0;
    }
    backing = findBacking(handle);
    SkCanvas* canvas = backing ? backing->canvas() : nullptr;
    if (!canvas) {
        return 0;
    }
    SkPaint paint;
    paint.setColor(SkColorSetARGB(255, 16, 32, 48));
    canvas->drawRect(SkRect::MakeWH(backing->width, backing->height), paint);
    skia_image_backing_internal::markMutated(backing);
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
    if (skia_image_backing_try_write_pixels(canvas, sourceHandle, srcLeft, srcTop, srcRight,
                       srcBottom, dstLeft, dstTop, dstRight, dstBottom, alphaMask, optimizationMask)) {
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
        if (backingAccountingForTest && isCompact(backing->format)) {
            compactReadbackCountForTest += static_cast<uint64_t>(height);
            compactRowScratchPeakBytesForTest = std::max(
                compactRowScratchPeakBytesForTest, static_cast<uint64_t>(rowBytes));
        }
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
    std::fill(std::begin(backingBytesLiveByFormatForTest),
              std::end(backingBytesLiveByFormatForTest), 0);
    std::fill(std::begin(backingBytesPeakByFormatForTest),
              std::end(backingBytesPeakByFormatForTest), 0);
    compactDirectDecodeCountForTest = 0;
    compactDirectDecodeBytesForTest = 0;
    temporaryRgbaDecodeBytesForTest = 0;
    compactReadbackCountForTest = 0;
    compactRowScratchPeakBytesForTest = 0;
    promotionAttemptsForTest = 0;
    promotionSuccessesForTest = 0;
    promotionFailuresForTest = 0;
    promotionBytesForTest = 0;
    writePixelsAttemptsForTest = 0;
    writePixelsHitsForTest = 0;
    writePixelsFallbacksForTest = 0;
    writePixelsCopiedBytesForTest = 0;
    writePixelsRegularAttemptsForTest = 0;
    writePixelsRegularHitsForTest = 0;
    writePixelsRegularFallbacksForTest = 0;
    writePixelsRegularCopiedBytesForTest = 0;
    writePixelsRegularClippedHitsForTest = 0;
    writePixelsRejectInvalidTargetOrSourceForTest = 0;
    writePixelsRejectAlphaMaskForTest = 0;
    writePixelsRejectMatrixForTest = 0;
    writePixelsRejectSaveCountForTest = 0;
    writePixelsRejectSourceRectForTest = 0;
    writePixelsRejectSizeMismatchForTest = 0;
    writePixelsRejectFractionalDestinationForTest = 0;
    writePixelsRejectDestinationBoundsForTest = 0;
    writePixelsRejectOpacityForTest = 0;
    writePixelsRejectSourcePixelsForTest = 0;
    writePixelsRejectWriteFailureForTest = 0;
    writePixelsDeviceOneToOneCandidatesForTest = 0;
    writePixelsDeviceOneToOneKnownOpaqueCandidatesForTest = 0;
    genericGeometryDrawsForTest = 0;
    smoothResampleDrawsForTest = 0;
    targetColorUniqueSourcesForTest.clear();
    targetColorUniqueFullKeysForTest.clear();
    targetColorUniqueNoDestinationKeysForTest.clear();
    targetColorUniqueIntrinsicKeysForTest.clear();
    targetColorPendingReplacementsForTest = 0;
    std::fill(std::begin(targetColorRejectionsForTest),
              std::end(targetColorRejectionsForTest), 0);
    physicalVariantUniqueFullKeysForTest.clear();
    physicalVariantUniqueNoSurfaceSizeKeysForTest.clear();
    physicalVariantPendingReplacementsForTest = 0;
    std::fill(std::begin(physicalVariantRejectionsForTest),
              std::end(physicalVariantRejectionsForTest), 0);
    sharedSlotTargetToPhysicalForTest = 0;
    sharedSlotPhysicalToTargetForTest = 0;
    sharedPendingTargetToPhysicalForTest = 0;
    sharedPendingPhysicalToTargetForTest = 0;
    std::fill(std::begin(targetColorSaveCountBucketsForTest),
              std::end(targetColorSaveCountBucketsForTest), 0);
    std::fill(std::begin(physicalVariantSaveCountBucketsForTest),
              std::end(physicalVariantSaveCountBucketsForTest), 0);
    std::fill(std::begin(physicalIdentitySaveCountBucketsForTest),
              std::end(physicalIdentitySaveCountBucketsForTest), 0);
    std::fill(std::begin(targetColorMappingSubreasonsForTest),
              std::end(targetColorMappingSubreasonsForTest), 0);
    std::fill(std::begin(physicalVariantMappingSubreasonsForTest),
              std::end(physicalVariantMappingSubreasonsForTest), 0);
    std::fill(std::begin(physicalIdentityMappingSubreasonsForTest),
              std::end(physicalIdentityMappingSubreasonsForTest), 0);
    if (!backingAccountingForTest) {
        return;
    }
    for (const auto& entry : backings) {
        if (entry.second) {
            ++backingRecordsLiveForTest;
            const uint64_t bytes = backingBytes(*entry.second);
            const int32 format = static_cast<int32>(entry.second->format);
            backingBytesLiveForTest += bytes;
            if (format >= 0 && format < 4) {
                backingBytesLiveByFormatForTest[format] += bytes;
            }
        }
    }
    backingRecordsPeakLiveForTest = backingRecordsLiveForTest;
    backingBytesPeakLiveForTest = backingBytesLiveForTest;
    physicalIdentityAttemptsForTest = 0;
    physicalIdentityHitsForTest = 0;
    physicalIdentityFallbacksForTest = 0;
    physicalIdentityResamplesAvoidedForTest = 0;
    std::fill(std::begin(physicalIdentityRejectionsForTest),
              std::end(physicalIdentityRejectionsForTest), 0);
    targetColorAttemptsForTest = 0;
    targetColorMaterializationsForTest = 0;
    targetColorHitsForTest = 0;
    targetColorFallbacksForTest = 0;
    targetColorConvertedBytesForTest = 0;
    physicalVariantLookupsForTest = 0;
    physicalVariantHitsForTest = 0;
    physicalVariantMissesForTest = 0;
    physicalVariantMaterializationsForTest = 0;
    physicalVariantEvictionsForTest = 0;
    physicalVariantBytesForTest = 0;
    for (int32 format = 0; format < 4; ++format) {
        backingBytesPeakByFormatForTest[format] = backingBytesLiveByFormatForTest[format];
    }
}

void skia_image_backing_set_accounting_for_test(int enabled) {
    backingAccountingForTest = enabled != 0;
    screen_diagnostics_set_for_test(enabled);
    if (!backingAccountingForTest) {
        backingRecordsCreatedForTest = 0;
        backingRecordsReleasedForTest = 0;
        backingRecordsLiveForTest = 0;
        backingRecordsPeakLiveForTest = 0;
        backingBytesLiveForTest = 0;
        backingBytesPeakLiveForTest = 0;
        std::fill(std::begin(backingBytesLiveByFormatForTest),
                  std::end(backingBytesLiveByFormatForTest), 0);
        std::fill(std::begin(backingBytesPeakByFormatForTest),
                  std::end(backingBytesPeakByFormatForTest), 0);
        compactDirectDecodeCountForTest = 0;
        compactDirectDecodeBytesForTest = 0;
        temporaryRgbaDecodeBytesForTest = 0;
        compactReadbackCountForTest = 0;
        compactRowScratchPeakBytesForTest = 0;
        promotionAttemptsForTest = 0;
        promotionSuccessesForTest = 0;
        promotionFailuresForTest = 0;
        promotionBytesForTest = 0;
        writePixelsAttemptsForTest = 0;
        writePixelsHitsForTest = 0;
        writePixelsFallbacksForTest = 0;
        writePixelsCopiedBytesForTest = 0;
        writePixelsRegularAttemptsForTest = 0;
        writePixelsRegularHitsForTest = 0;
        writePixelsRegularFallbacksForTest = 0;
        writePixelsRegularCopiedBytesForTest = 0;
        writePixelsRegularClippedHitsForTest = 0;
        writePixelsRejectInvalidTargetOrSourceForTest = 0;
        writePixelsRejectAlphaMaskForTest = 0;
        writePixelsRejectMatrixForTest = 0;
        writePixelsRejectSaveCountForTest = 0;
        writePixelsRejectSourceRectForTest = 0;
        writePixelsRejectSizeMismatchForTest = 0;
        writePixelsRejectFractionalDestinationForTest = 0;
        writePixelsRejectDestinationBoundsForTest = 0;
        writePixelsRejectOpacityForTest = 0;
        writePixelsRejectSourcePixelsForTest = 0;
        writePixelsRejectWriteFailureForTest = 0;
        writePixelsDeviceOneToOneCandidatesForTest = 0;
        writePixelsDeviceOneToOneKnownOpaqueCandidatesForTest = 0;
        genericGeometryDrawsForTest = 0;
        smoothResampleDrawsForTest = 0;
        physicalIdentityAttemptsForTest = 0;
        physicalIdentityHitsForTest = 0;
        physicalIdentityFallbacksForTest = 0;
        physicalIdentityResamplesAvoidedForTest = 0;
        std::fill(std::begin(physicalIdentityRejectionsForTest),
                  std::end(physicalIdentityRejectionsForTest), 0);
        targetColorAttemptsForTest = 0;
        targetColorMaterializationsForTest = 0;
        targetColorHitsForTest = 0;
        targetColorFallbacksForTest = 0;
        targetColorConvertedBytesForTest = 0;
        physicalVariantLookupsForTest = 0;
        physicalVariantHitsForTest = 0;
        physicalVariantMissesForTest = 0;
        physicalVariantMaterializationsForTest = 0;
        physicalVariantEvictionsForTest = 0;
        physicalVariantBytesForTest = 0;
        targetColorUniqueSourcesForTest.clear();
        targetColorUniqueFullKeysForTest.clear();
        targetColorUniqueNoDestinationKeysForTest.clear();
        targetColorUniqueIntrinsicKeysForTest.clear();
        targetColorPendingReplacementsForTest = 0;
        std::fill(std::begin(targetColorRejectionsForTest),
                  std::end(targetColorRejectionsForTest), 0);
        physicalVariantUniqueFullKeysForTest.clear();
        physicalVariantUniqueNoSurfaceSizeKeysForTest.clear();
        physicalVariantPendingReplacementsForTest = 0;
        std::fill(std::begin(physicalVariantRejectionsForTest),
                  std::end(physicalVariantRejectionsForTest), 0);
        sharedSlotTargetToPhysicalForTest = 0;
        sharedSlotPhysicalToTargetForTest = 0;
        sharedPendingTargetToPhysicalForTest = 0;
        sharedPendingPhysicalToTargetForTest = 0;
        std::fill(std::begin(targetColorSaveCountBucketsForTest),
                  std::end(targetColorSaveCountBucketsForTest), 0);
        std::fill(std::begin(physicalVariantSaveCountBucketsForTest),
                  std::end(physicalVariantSaveCountBucketsForTest), 0);
        std::fill(std::begin(physicalIdentitySaveCountBucketsForTest),
                  std::end(physicalIdentitySaveCountBucketsForTest), 0);
        std::fill(std::begin(targetColorMappingSubreasonsForTest),
                  std::end(targetColorMappingSubreasonsForTest), 0);
        std::fill(std::begin(physicalVariantMappingSubreasonsForTest),
                  std::end(physicalVariantMappingSubreasonsForTest), 0);
        std::fill(std::begin(physicalIdentityMappingSubreasonsForTest),
                  std::end(physicalIdentityMappingSubreasonsForTest), 0);
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

uint64_t skia_image_backing_write_pixels_regular_attempts_for_test(void) {
    return writePixelsRegularAttemptsForTest;
}

uint64_t skia_image_backing_write_pixels_regular_hits_for_test(void) {
    return writePixelsRegularHitsForTest;
}

uint64_t skia_image_backing_write_pixels_regular_fallbacks_for_test(void) {
    return writePixelsRegularFallbacksForTest;
}

uint64_t skia_image_backing_write_pixels_regular_copied_bytes_for_test(void) {
    return writePixelsRegularCopiedBytesForTest;
}

uint64_t skia_image_backing_write_pixels_regular_clipped_hits_for_test(void) {
    return writePixelsRegularClippedHitsForTest;
}

uint64_t skia_image_backing_write_pixels_reject_invalid_target_or_source_for_test(void) {
    return writePixelsRejectInvalidTargetOrSourceForTest;
}

uint64_t skia_image_backing_write_pixels_reject_alpha_mask_for_test(void) {
    return writePixelsRejectAlphaMaskForTest;
}

uint64_t skia_image_backing_write_pixels_reject_matrix_for_test(void) {
    return writePixelsRejectMatrixForTest;
}

uint64_t skia_image_backing_write_pixels_reject_save_count_for_test(void) {
    return writePixelsRejectSaveCountForTest;
}

uint64_t skia_image_backing_write_pixels_reject_source_rect_for_test(void) {
    return writePixelsRejectSourceRectForTest;
}

uint64_t skia_image_backing_write_pixels_reject_size_mismatch_for_test(void) {
    return writePixelsRejectSizeMismatchForTest;
}

uint64_t skia_image_backing_write_pixels_reject_fractional_destination_for_test(void) {
    return writePixelsRejectFractionalDestinationForTest;
}

uint64_t skia_image_backing_write_pixels_reject_destination_bounds_for_test(void) {
    return writePixelsRejectDestinationBoundsForTest;
}

uint64_t skia_image_backing_write_pixels_reject_opacity_for_test(void) {
    return writePixelsRejectOpacityForTest;
}

uint64_t skia_image_backing_write_pixels_reject_source_pixels_for_test(void) {
    return writePixelsRejectSourcePixelsForTest;
}

uint64_t skia_image_backing_write_pixels_reject_write_failure_for_test(void) {
    return writePixelsRejectWriteFailureForTest;
}

uint64_t skia_image_backing_write_pixels_device_one_to_one_candidates_for_test(void) {
    return writePixelsDeviceOneToOneCandidatesForTest;
}

uint64_t skia_image_backing_write_pixels_device_one_to_one_known_opaque_candidates_for_test(void) {
    return writePixelsDeviceOneToOneKnownOpaqueCandidatesForTest;
}

uint64_t skia_image_backing_generic_geometry_draws_for_test(void) {
    return genericGeometryDrawsForTest;
}

uint64_t skia_image_backing_smooth_resample_draws_for_test(void) {
    return smoothResampleDrawsForTest;
}

void skia_image_backing_record_generic_geometry_draw_for_test(void) {
    if (backingAccountingForTest) {
        ++genericGeometryDrawsForTest;
    }
}

void skia_image_backing_record_smooth_resample_draw_for_test(void) {
    if (backingAccountingForTest) {
        ++smoothResampleDrawsForTest;
    }
}

void skia_image_backing_record_physical_identity_attempt_for_test(void) {
    if (backingAccountingForTest) {
        ++physicalIdentityAttemptsForTest;
    }
}

void skia_image_backing_record_physical_identity_hit_for_test(void) {
    if (backingAccountingForTest) {
        ++physicalIdentityHitsForTest;
    }
}

void skia_image_backing_record_physical_identity_fallback_for_test(void) {
    if (backingAccountingForTest) {
        ++physicalIdentityFallbacksForTest;
    }
}

void skia_image_backing_record_physical_identity_resample_avoided_for_test(void) {
    if (backingAccountingForTest) {
        ++physicalIdentityResamplesAvoidedForTest;
    }
}

void skia_image_backing_record_physical_identity_rejection_for_test(int32 reason) {
    if (backingAccountingForTest && reason >= 0
        && reason < SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST) {
        ++physicalIdentityRejectionsForTest[reason];
    }
}

uint64_t skia_image_backing_physical_identity_attempts_for_test(void) {
    return physicalIdentityAttemptsForTest;
}

uint64_t skia_image_backing_physical_identity_hits_for_test(void) {
    return physicalIdentityHitsForTest;
}

uint64_t skia_image_backing_physical_identity_fallbacks_for_test(void) {
    return physicalIdentityFallbacksForTest;
}

uint64_t skia_image_backing_physical_identity_resamples_avoided_for_test(void) {
    return physicalIdentityResamplesAvoidedForTest;
}

uint64_t skia_image_backing_physical_identity_rejections_for_test(int32 reason) {
    return reason >= 0 && reason < SKIA_RASTER_REJECT_REASON_COUNT_FOR_TEST
        ? physicalIdentityRejectionsForTest[reason] : 0;
}

uint64_t skia_image_backing_target_color_attempts_for_test(void) {
    return targetColorAttemptsForTest;
}

uint64_t skia_image_backing_target_color_materializations_for_test(void) {
    return targetColorMaterializationsForTest;
}

uint64_t skia_image_backing_target_color_hits_for_test(void) {
    return targetColorHitsForTest;
}

uint64_t skia_image_backing_target_color_fallbacks_for_test(void) {
    return targetColorFallbacksForTest;
}

uint64_t skia_image_backing_target_color_converted_bytes_for_test(void) {
    return targetColorConvertedBytesForTest;
}

uint64_t skia_image_backing_physical_variant_lookups_for_test(void) {
    return physicalVariantLookupsForTest;
}

uint64_t skia_image_backing_physical_variant_hits_for_test(void) {
    return physicalVariantHitsForTest;
}

uint64_t skia_image_backing_physical_variant_misses_for_test(void) {
    return physicalVariantMissesForTest;
}

uint64_t skia_image_backing_physical_variant_materializations_for_test(void) {
    return physicalVariantMaterializationsForTest;
}

uint64_t skia_image_backing_physical_variant_evictions_for_test(void) {
    return physicalVariantEvictionsForTest;
}

uint64_t skia_image_backing_physical_variant_bytes_for_test(void) {
    return physicalVariantBytesForTest;
}

namespace skia_image_backing_internal {

int64_t diagnosticMetricForTest(int32 kind) {
    if (kind >= 8 && kind < 15) {
        return static_cast<int64_t>(targetColorRejectionsForTest[kind - 8]);
    }
    if (kind >= 24 && kind < 31) {
        return static_cast<int64_t>(physicalVariantRejectionsForTest[kind - 24]);
    }
    if (kind >= 40 && kind < 46) {
        return static_cast<int64_t>(targetColorSaveCountBucketsForTest[kind - 40]);
    }
    if (kind >= 48 && kind < 54) {
        return static_cast<int64_t>(physicalVariantSaveCountBucketsForTest[kind - 48]);
    }
    if (kind >= 56 && kind < 62) {
        return static_cast<int64_t>(physicalIdentitySaveCountBucketsForTest[kind - 56]);
    }
    if (kind >= 64 && kind < 74) {
        return static_cast<int64_t>(targetColorMappingSubreasonsForTest[kind - 64]);
    }
    if (kind >= 80 && kind < 90) {
        return static_cast<int64_t>(physicalVariantMappingSubreasonsForTest[kind - 80]);
    }
    if (kind >= 96 && kind < 106) {
        return static_cast<int64_t>(physicalIdentityMappingSubreasonsForTest[kind - 96]);
    }
    switch (kind) {
    case 0:
        return static_cast<int64_t>(targetColorUniqueSourcesForTest.size());
    case 1:
        return static_cast<int64_t>(targetColorUniqueFullKeysForTest.size());
    case 2:
        return static_cast<int64_t>(targetColorUniqueNoDestinationKeysForTest.size());
    case 3:
        return static_cast<int64_t>(targetColorUniqueIntrinsicKeysForTest.size());
    case 4:
        return static_cast<int64_t>(targetColorPendingReplacementsForTest);
    case 16:
        return static_cast<int64_t>(physicalVariantUniqueFullKeysForTest.size());
    case 17:
        return static_cast<int64_t>(physicalVariantUniqueNoSurfaceSizeKeysForTest.size());
    case 18:
        return static_cast<int64_t>(physicalVariantPendingReplacementsForTest);
    case 32:
        return static_cast<int64_t>(sharedSlotTargetToPhysicalForTest);
    case 33:
        return static_cast<int64_t>(sharedSlotPhysicalToTargetForTest);
    case 34:
        return static_cast<int64_t>(sharedPendingTargetToPhysicalForTest);
    case 35:
        return static_cast<int64_t>(sharedPendingPhysicalToTargetForTest);
    default:
        return 0;
    }
}

}

int32 skia_image_backing_format_for_test(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? static_cast<int32>(backing->format) : -1;
}

uint64_t skia_image_backing_generation_for_test(int64_t handle) {
    NativeImageBackingRecord* backing = findBacking(handle);
    return backing ? backing->generation : 0;
}

uint64_t skia_image_backing_bytes_for_format_for_test(ImageBackingFormat format) {
    return format >= IMAGE_BACKING_FORMAT_RGBA8888 && format <= IMAGE_BACKING_FORMAT_ARGB4444
        ? backingBytesLiveByFormatForTest[static_cast<int32>(format)] : 0;
}

uint64_t skia_image_backing_compact_direct_decode_count_for_test(void) {
    return compactDirectDecodeCountForTest;
}

uint64_t skia_image_backing_compact_direct_decode_bytes_for_test(void) {
    return compactDirectDecodeBytesForTest;
}

uint64_t skia_image_backing_temporary_rgba_decode_bytes_for_test(void) {
    return temporaryRgbaDecodeBytesForTest;
}

uint64_t skia_image_backing_compact_readback_count_for_test(void) {
    return compactReadbackCountForTest;
}

uint64_t skia_image_backing_compact_row_scratch_peak_bytes_for_test(void) {
    return compactRowScratchPeakBytesForTest;
}

uint64_t skia_image_backing_promotion_attempts_for_test(void) {
    return promotionAttemptsForTest;
}

uint64_t skia_image_backing_promotion_successes_for_test(void) {
    return promotionSuccessesForTest;
}

uint64_t skia_image_backing_promotion_failures_for_test(void) {
    return promotionFailuresForTest;
}

uint64_t skia_image_backing_promotion_bytes_for_test(void) {
    return promotionBytesForTest;
}

void skia_image_backing_record_compact_decode_for_test(ImageBackingFormat format, uint64_t bytes) {
    if (backingAccountingForTest && isCompact(format)) {
        ++compactDirectDecodeCountForTest;
        compactDirectDecodeBytesForTest += bytes;
    }
}

void skia_image_backing_record_temporary_rgba_decode_for_test(uint64_t bytes) {
    if (backingAccountingForTest) {
        temporaryRgbaDecodeBytesForTest += bytes;
    }
}
