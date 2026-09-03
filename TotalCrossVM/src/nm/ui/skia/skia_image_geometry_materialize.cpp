// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_geometry_internal.h"

#include "skia_image_backing_internal.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <limits>
#include <memory>

using skia_image_backing_internal::NativeImageBackingRecord;
using skia_image_backing_internal::findBacking;
using skia_image_backing_internal::rasterInfo;
using skia_image_backing_internal::registerBacking;

namespace {

static bool hasDestinationScaledGeometry(const SkiaImageGeometryPlanData* plan, int endExclusive) {
    if (!plan || !plan->operations || endExclusive < 0 || endExclusive > plan->operationCount) {
        return false;
    }
    for (int i = 0; i < endExclusive; ++i) {
        if (plan->operations[i] == 0 || plan->operations[i] == 1 || plan->operations[i] == 2) {
            return true;
        }
    }
    return false;
}

}

int64_t skia_image_backing_materialize_geometry(const SkiaImageGeometryPlanData* plan) {
    if (!plan || plan->outputWidth <= 0 || plan->outputHeight <= 0 || plan->outputFrameCount <= 0
        || !std::isfinite(plan->outputContentScale) || plan->outputContentScale <= 0) {
        return 0;
    }
    const bool frameLayout = plan->outputFrameCount > 1 && plan->outputWidthOfAllFrames > 0
        && plan->operations && plan->operationCount > 0
        && plan->operations[plan->operationCount - 1] == 13;
    const int prefixOperationCount = frameLayout ? plan->operationCount - 1 : plan->operationCount;
    const bool destinationScaledFrameLayout = frameLayout
        && hasDestinationScaledGeometry(plan, prefixOperationCount);
    const double physicalFrameWidth = frameLayout && !destinationScaledFrameLayout
        ? static_cast<double>(plan->outputWidthOfAllFrames / plan->outputFrameCount)
        : std::ceil(plan->outputWidth * plan->outputContentScale);
    const double physicalHeight = std::ceil(plan->outputHeight * plan->outputContentScale);
    double physicalFullWidth = frameLayout
        ? static_cast<double>(plan->outputWidthOfAllFrames)
        : physicalFrameWidth * plan->outputFrameCount;
    if (destinationScaledFrameLayout && prefixOperationCount > 0 && plan->dimensions) {
        const int prefixWidth = plan->dimensions[(prefixOperationCount - 1) * 2];
        const double transformedWidth = std::ceil(prefixWidth * plan->destinationScale);
        physicalFullWidth = std::max(physicalFullWidth,
            std::max(transformedWidth, physicalFrameWidth * plan->outputFrameCount));
    }
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
        if (frameLayout && prefixOperationCount > 0 && plan->dimensions) {
            SkiaImageGeometryPlanData prefix = *plan;
            prefix.operationCount = prefixOperationCount;
            prefix.outputWidth = plan->dimensions[(prefixOperationCount - 1) * 2];
            prefix.outputHeight = plan->dimensions[(prefixOperationCount - 1) * 2 + 1];
            prefix.outputFrameCount = 1;
            prefix.outputWidthOfAllFrames = prefix.outputWidth;
            GeometryTransform transform;
            if (!skia_image_geometry_compile(&prefix, -1, &transform)) {
                return 0;
            }
            target->scale(static_cast<float>(plan->outputContentScale),
                          static_cast<float>(plan->outputContentScale));
            if (!skia_image_geometry_draw_compiled(target, image.get(), transform, 0, 0,
                static_cast<float>(prefix.outputWidth), static_cast<float>(prefix.outputHeight),
                0, 0, static_cast<float>(prefix.outputWidth), static_cast<float>(prefix.outputHeight),
                plan->materializeAlphaMask,
                std::abs(plan->outputContentScale - 1.0) < 0.000001)) {
                return 0;
            }
        } else {
            target->scale(static_cast<float>(plan->outputContentScale),
                          static_cast<float>(plan->outputContentScale));
            for (int frame = 0; frame < plan->outputFrameCount; ++frame) {
                GeometryTransform transform;
                if (!skia_image_geometry_compile(plan, frame, &transform)
                    || !skia_image_geometry_draw_compiled(target, image.get(), transform, 0, 0,
                        static_cast<float>(plan->outputWidth), static_cast<float>(plan->outputHeight),
                        static_cast<float>(frame * plan->outputWidth), 0,
                        static_cast<float>((frame + 1) * plan->outputWidth),
                        static_cast<float>(plan->outputHeight), plan->materializeAlphaMask,
                        std::abs(plan->outputContentScale - 1.0) < 0.000001)) {
                    return 0;
                }
            }
        }
        backing->width = static_cast<int32>(physicalFullWidth);
        backing->height = static_cast<int32>(physicalHeight);
        return registerBacking(std::move(backing));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}
