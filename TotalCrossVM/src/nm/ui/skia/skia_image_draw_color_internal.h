// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_IMAGE_DRAW_COLOR_INTERNAL_H
#define SKIA_IMAGE_DRAW_COLOR_INTERNAL_H

#include "skia_internal.h"
#include "include/core/SkColorFilter.h"

struct SkiaImageDrawColorFilters {
    sk_sp<SkColorFilter> content;
    sk_sp<SkColorFilter> fill;
};

bool skia_image_draw_color_filters(const SkiaImageDrawPlanData* plan,
                                   SkiaImageDrawColorFilters* filters);

#endif
