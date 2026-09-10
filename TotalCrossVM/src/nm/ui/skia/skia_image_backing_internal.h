// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_BACKING_INTERNAL_H
#define SKIA_IMAGE_BACKING_INTERNAL_H

#include "skia_image_backing.h"
#include "skia_internal.h"

#include <cstdint>
#include <vector>
#include <cstddef>

#include "ui/ImageBackingFormat.h"

namespace skia_image_backing_internal {

enum RasterVariantKind : uint8_t {
    RASTER_VARIANT_TARGET_COLOR = 1,
    RASTER_VARIANT_PHYSICAL = 2,
};

struct RasterVariantKey {
    uint64_t sourceGeneration = 0;
    uint64_t sourceDecodeGeneration = 0;
    int32 sourceLeft = 0;
    int32 sourceTop = 0;
    int32 sourceRight = 0;
    int32 sourceBottom = 0;
    int32 destinationLeft = 0;
    int32 destinationTop = 0;
    int32 destinationRight = 0;
    int32 destinationBottom = 0;
    int32 targetWidth = 0;
    int32 targetHeight = 0;
    int32 targetColorType = 0;
    uint8_t kind = 0;
    std::vector<uint64_t> geometrySignature;

    bool operator==(const RasterVariantKey& other) const {
        return sourceGeneration == other.sourceGeneration
            && sourceDecodeGeneration == other.sourceDecodeGeneration
            && sourceLeft == other.sourceLeft && sourceTop == other.sourceTop
            && sourceRight == other.sourceRight && sourceBottom == other.sourceBottom
            && destinationLeft == other.destinationLeft && destinationTop == other.destinationTop
            && destinationRight == other.destinationRight && destinationBottom == other.destinationBottom
            && targetWidth == other.targetWidth && targetHeight == other.targetHeight
            && targetColorType == other.targetColorType && kind == other.kind
            && geometrySignature == other.geometrySignature;
    }
};

struct RasterVariantSlot {
    sk_sp<SkImage> image;
    RasterVariantKey key;
    bool valid = false;
};

struct NativeImageBackingRecord {
    sk_sp<SkImage> image;
    sk_sp<SkSurface> surface;
    int32 width;
    int32 height;
    ImageBackingFormat format = IMAGE_BACKING_FORMAT_RGBA8888;
    size_t rowBytes = 0;
    int32 opacity = SKIA_IMAGE_OPACITY_UNKNOWN;
    uint64_t generation = 0;
    bool applyColor2AnalysisValid = false;
    uint64_t applyColor2AnalysisGeneration = 0;
    uint8_t applyColor2HighestRed = 0;
    uint8_t applyColor2HighestGreen = 0;
    uint8_t applyColor2HighestBlue = 0;
    uint8_t applyColor2HighestChannel = 0;
    RasterVariantSlot rasterVariant;
    RasterVariantKey pendingRasterVariantKey;
    uint8_t pendingRasterVariantObservations = 0;
    bool pendingRasterVariant = false;

    SkCanvas* canvas() const {
        return surface ? surface->getCanvas() : nullptr;
    }

    sk_sp<SkImage> snapshot() const {
        return image ? image : surface ? surface->makeImageSnapshot() : nullptr;
    }
};

NativeImageBackingRecord* findBacking(int64_t handle);
int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing);
SkImageInfo rasterInfo(int32 width, int32 height,
      ImageBackingFormat format = IMAGE_BACKING_FORMAT_RGBA8888);
void markMutated(NativeImageBackingRecord* backing);

int tryWritePixelsImage(SkCanvas* targetCanvas, const SkImage* image, int32 width, int32 height,
                        bool sourceOpaque, float srcLeft, float srcTop, float srcRight,
                        float srcBottom, float dstLeft, float dstTop, float dstRight,
                        float dstBottom, int32 alphaMask, int32 optimizationMask);
int tryDirectPhysicalCopy(SkCanvas* targetCanvas, NativeImageBackingRecord* source,
                          int32 sourceLeft, int32 sourceTop, int32 sourceRight, int32 sourceBottom,
                          int32 destinationLeft, int32 destinationTop, int32 destinationRight,
                          int32 destinationBottom, int32 alphaMask, int32 optimizationMask);
bool proveOpaque(NativeImageBackingRecord* source);
void recordTargetColorAttemptForTest();
void recordTargetColorFallbackForTest();

enum RasterVariantUse : uint8_t {
    RASTER_VARIANT_NOT_USED = 0,
    RASTER_VARIANT_OBSERVED = 1,
    RASTER_VARIANT_MATERIALIZED = 2,
    RASTER_VARIANT_HIT = 3,
    RASTER_VARIANT_FAILED = 4,
};

RasterVariantUse acquireTargetColorVariant(NativeImageBackingRecord* source,
                                           const RasterVariantKey& key, SkColorType targetColorType,
                                           sk_sp<SkImage>* image);
RasterVariantUse acquirePhysicalVariant(NativeImageBackingRecord* source,
                                         const RasterVariantKey& key,
                                         const SkiaImageDrawPlanData* plan,
                                         SkColorType targetColorType, sk_sp<SkImage>* image);
void clearRasterVariant(NativeImageBackingRecord* source);

}

#endif
