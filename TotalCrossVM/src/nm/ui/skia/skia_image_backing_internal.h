// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_BACKING_INTERNAL_H
#define SKIA_IMAGE_BACKING_INTERNAL_H

#include "skia_internal.h"

namespace skia_image_backing_internal {

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

NativeImageBackingRecord* findBacking(int64_t handle);
int64_t registerBacking(std::unique_ptr<NativeImageBackingRecord> backing);
SkImageInfo rasterInfo(int32 width, int32 height);

}

#endif
