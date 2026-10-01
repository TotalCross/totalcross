// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_BACKING_INTERNAL_H
#define SKIA_IMAGE_BACKING_INTERNAL_H

#include "skia_image_backing.h"
#include "skia_internal.h"

#include <cstdint>
#include <vector>

namespace skia_image_backing_internal {

enum class BackingFormat : uint8_t {
    RGBA8888 = 0,
    RGB565 = 1,
    GRAY8 = 2,
    ARGB4444 = 3,
    UNKNOWN = 255
};

enum RasterVariantKind {
    RASTER_VARIANT_TARGET_COLOR = 1,
    RASTER_VARIANT_PHYSICAL = 2
};

enum RasterVariantDecision {
    RASTER_VARIANT_MISS = 0,
    RASTER_VARIANT_MATERIALIZE = 1,
    RASTER_VARIANT_HIT = 2,
    RASTER_VARIANT_INVALID = 3
};

struct RasterVariantKey {
    int32_t kind = 0;
    std::vector<uint32_t> words;

    bool equals(int32_t otherKind, const std::vector<uint32_t>& otherWords) const {
        return kind == otherKind && words == otherWords;
    }
};

struct NativeImageBackingRecord {
    sk_sp<SkImage> image;
    sk_sp<SkSurface> surface;
    int32 width = 0;
    int32 height = 0;
    BackingFormat format = BackingFormat::RGBA8888;
    size_t rowBytes = 0;
    uint64_t backingBytes = 0;
    uint64_t generation = 0;
    bool applyColor2AnalysisValid = false;
    uint64_t applyColor2AnalysisGeneration = 0;
    uint8_t applyColor2HighestRed = 0;
    uint8_t applyColor2HighestGreen = 0;
    uint8_t applyColor2HighestBlue = 0;
    uint8_t applyColor2HighestChannel = 0;
    bool rasterVariantValid = false;
    bool rasterVariantOpaque = false;
    RasterVariantKey rasterVariantKey;
    sk_sp<SkImage> rasterVariant;
    bool rasterVariantPending = false;
    RasterVariantKey rasterVariantPendingKey;

    SkCanvas* canvas() const {
        return surface ? surface->getCanvas() : nullptr;
    }

    sk_sp<SkImage> snapshot() const {
        return image ? image : surface ? surface->makeImageSnapshot() : nullptr;
    }
};

NativeImageBackingRecord* findBacking(int64_t handle);
int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing);
bool updateStorageMetadata(NativeImageBackingRecord* backing);
SkImageInfo rasterInfo(int32 width, int32 height);
int rasterVariantObserve(NativeImageBackingRecord* backing, int32_t kind,
                         const std::vector<uint32_t>& words, sk_sp<SkImage>* hit,
                         bool* provenOpaque);
bool rasterVariantStore(NativeImageBackingRecord* backing, int32_t kind,
                        const std::vector<uint32_t>& words, sk_sp<SkImage> image,
                        bool provenOpaque);
void rasterVariantFail(NativeImageBackingRecord* backing, int32_t kind,
                       const std::vector<uint32_t>& words);
void rasterVariantClear(NativeImageBackingRecord* backing);

}

#endif
