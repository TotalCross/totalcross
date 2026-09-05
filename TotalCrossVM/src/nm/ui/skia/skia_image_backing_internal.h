// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_BACKING_INTERNAL_H
#define SKIA_IMAGE_BACKING_INTERNAL_H

#include "skia_image_backing.h"
#include "skia_internal.h"

namespace skia_image_backing_internal {

struct NativeImageBackingRecord {
    sk_sp<SkImage> image;
    sk_sp<SkSurface> surface;
    int32 width;
    int32 height;
    uint64_t generation = 0;
    bool applyColor2AnalysisValid = false;
    uint64_t applyColor2AnalysisGeneration = 0;
    uint8_t applyColor2HighestRed = 0;
    uint8_t applyColor2HighestGreen = 0;
    uint8_t applyColor2HighestBlue = 0;
    uint8_t applyColor2HighestChannel = 0;

    SkCanvas* canvas() const {
        return surface ? surface->getCanvas() : nullptr;
    }

    sk_sp<SkImage> snapshot() const {
        return image ? image : surface ? surface->makeImageSnapshot() : nullptr;
    }
};

NativeImageBackingRecord* findBacking(int64_t handle);
int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing);
SkImageInfo rasterInfo(int32 width, int32 height);

}

#endif
