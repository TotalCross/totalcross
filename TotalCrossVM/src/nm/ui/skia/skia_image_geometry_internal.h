// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_GEOMETRY_INTERNAL_H
#define SKIA_IMAGE_GEOMETRY_INTERNAL_H

#include "skia_internal.h"
#include "skia_image_draw_color_internal.h"

struct GeometryTransform {
    double a;
    double b;
    double c;
    double d;
    double tx;
    double ty;
    double width;
    double height;
    SkRect validRoot;
    bool smooth;
    bool hasFill;
    Pixel fillColor;
};

bool skia_image_geometry_compile(const SkiaImageDrawPlanData* plan, int frameOverride,
                                 GeometryTransform* transform);
bool skia_image_geometry_draw_compiled(SkCanvas* canvas, const SkImage* image,
                                       const GeometryTransform& transform, float srcLeft,
                                       float srcTop, float srcRight, float srcBottom, float dstLeft,
                                       float dstTop, float dstRight, float dstBottom, int32 alphaMask,
                                       bool applyPixelCenterOffset,
                                       const SkiaImageDrawColorFilters* colorFilters);

#endif
