// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_draw_color_internal.h"

#include "include/effects/SkTableColorFilter.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <utility>

namespace {

static int clampChannel(int value) {
    return value < 0 ? 0 : value > 255 ? 255 : value;
}

static void buildContrastTable(int32 level, uint8_t* table) {
    const double factor = level < 0
        ? (level + 128) / 128.0
        : 127.0 / std::max(127 - level, 1);
    for (int i = 0; i <= 127; ++i) {
        const int value = static_cast<int>(127.0 * std::pow(i / 127.0, factor)) & 0xff;
        table[i] = static_cast<uint8_t>(value);
        table[255 - i] = static_cast<uint8_t>(255 - value);
    }
}

static sk_sp<SkColorFilter> touchUpFilter(int32 brightness, int32 contrast) {
    uint8_t table[256];
    uint8_t contrastTable[256];
    if (contrast != 0) {
        buildContrastTable(contrast, contrastTable);
    }
    const bool useBrightness = brightness != 0;
    int32 multiplier = 0;
    int32 offset = 0;
    if (useBrightness) {
        const double effective = (brightness + 128.0) / 128.0;
        if (brightness <= 0) {
            multiplier = static_cast<int32>(std::sqrt(effective) * 0x10000);
        } else {
            double f = effective - 1.0;
            f *= f;
            offset = static_cast<int32>(f * 0xFF0000);
            multiplier = static_cast<int32>((1.0 - f) * effective * 0x10000);
        }
    }
    for (int value = 0; value < 256; ++value) {
        int result = contrast == 0 ? value : contrastTable[value];
        if (useBrightness) {
            result = (multiplier * result + offset) >> 16;
        }
        table[value] = static_cast<uint8_t>(clampChannel(result));
    }
    return SkTableColorFilter::MakeARGB(nullptr, table, table, table);
}

static sk_sp<SkColorFilter> fadeFilter(int32 background) {
    uint8_t red[256];
    uint8_t green[256];
    uint8_t blue[256];
    const int backgroundRed = (background >> 16) & 0xff;
    const int backgroundGreen = (background >> 8) & 0xff;
    const int backgroundBlue = background & 0xff;
    for (int value = 0; value < 256; ++value) {
        red[value] = static_cast<uint8_t>((backgroundRed + value) / 2);
        green[value] = static_cast<uint8_t>((backgroundGreen + value) / 2);
        blue[value] = static_cast<uint8_t>((backgroundBlue + value) / 2);
    }
    return SkTableColorFilter::MakeARGB(nullptr, red, green, blue);
}

static sk_sp<SkColorFilter> alphaFilter(int32 delta) {
    uint8_t alpha[256];
    for (int value = 0; value < 256; ++value) {
        alpha[value] = static_cast<uint8_t>(value == 0 ? 0 : clampChannel(value + delta));
    }
    return SkTableColorFilter::MakeARGB(alpha, nullptr, nullptr, nullptr);
}

static sk_sp<SkColorFilter> applyFadeFilter(int32 fadeValue) {
    uint8_t table[256];
    for (int value = 0; value < 256; ++value) {
        table[value] = static_cast<uint8_t>((value * fadeValue) / 255);
    }
    return SkTableColorFilter::MakeARGB(nullptr, table, table, table);
}

static sk_sp<SkColorFilter> applyColorFilter(int32 color) {
    const int red = (color >> 16) & 0xff;
    const int green = (color >> 8) & 0xff;
    const int blue = color & 0xff;
    const int redMultiplier = static_cast<int>(std::sqrt((red + 128.0) / 128.0) * 0x10000);
    const int greenMultiplier = static_cast<int>(std::sqrt((green + 128.0) / 128.0) * 0x10000);
    const int blueMultiplier = static_cast<int>(std::sqrt((blue + 128.0) / 128.0) * 0x10000);
    uint8_t redTable[256];
    uint8_t greenTable[256];
    uint8_t blueTable[256];
    for (int value = 0; value < 256; ++value) {
        redTable[value] = static_cast<uint8_t>(clampChannel((redMultiplier * value) >> 16));
        greenTable[value] = static_cast<uint8_t>(clampChannel((greenMultiplier * value) >> 16));
        blueTable[value] = static_cast<uint8_t>(clampChannel((blueMultiplier * value) >> 16));
    }
    return SkTableColorFilter::MakeARGB(nullptr, redTable, greenTable, blueTable);
}

static bool appendFilter(sk_sp<SkColorFilter>* chain, sk_sp<SkColorFilter> next) {
    if (!next) {
        return false;
    }
    *chain = *chain ? SkColorFilters::Compose(next, *chain) : std::move(next);
    return true;
}

static bool isGeometryOperation(int32 operation) {
    return operation == SKIA_IMAGE_DRAW_SCALE || operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE
        || operation == SKIA_IMAGE_DRAW_ROTATE_SCALE || operation == SKIA_IMAGE_DRAW_FRAME_SELECT
        || operation == SKIA_IMAGE_DRAW_CROP || operation == SKIA_IMAGE_DRAW_FRAME_LAYOUT;
}

static int normalizedFrame(int frame, int count) {
    if (count <= 1) {
        return 0;
    }
    return frame < 0 ? count - 1 : frame >= count ? 0 : frame;
}

static bool applyFadeToDrawnFrame(const SkiaImageDrawPlanData* plan, int index, int capturedFrame) {
    int frameCount = std::max(1, plan->rootFrameCount);
    int selectedFrame = normalizedFrame(plan->currentFrame, frameCount);
    for (int prior = 0; prior < index; ++prior) {
        const int operation = plan->operations[prior];
        if (operation == SKIA_IMAGE_DRAW_FRAME_SELECT) {
            frameCount = 1;
            selectedFrame = 0;
        } else if (operation == SKIA_IMAGE_DRAW_FRAME_LAYOUT) {
            frameCount = std::max(1, plan->parameters[prior * 4]);
            selectedFrame = normalizedFrame(plan->currentFrame, frameCount);
        }
    }
    if (frameCount <= 1) {
        return true;
    }
    return selectedFrame == normalizedFrame(capturedFrame, frameCount);
}

}

bool skia_image_draw_color_filters(const SkiaImageDrawPlanData* plan,
                                   SkiaImageDrawColorFilters* filters) {
    if (!plan || !filters || plan->operationCount <= 0 || !plan->operations || !plan->parameters) {
        return false;
    }
    filters->content.reset();
    filters->fill.reset();
    bool colorSeen = false;
    bool fillStarted = false;
    for (int index = 0; index < plan->operationCount; ++index) {
        const int32 operation = plan->operations[index];
        const int32* parameters = plan->parameters + index * 4;
        if (operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE && colorSeen) {
            return false;
        }
        if (operation == SKIA_IMAGE_DRAW_ROTATE_SCALE) {
            if (fillStarted) {
                return false;
            }
            fillStarted = true;
            continue;
        }
        if (isGeometryOperation(operation)) {
            continue;
        }
        sk_sp<SkColorFilter> next;
        bool applies = true;
        switch (operation) {
        case SKIA_IMAGE_DRAW_TOUCH_UP:
            next = touchUpFilter(parameters[0], parameters[1]);
            break;
        case SKIA_IMAGE_DRAW_FADE:
            next = fadeFilter(parameters[0]);
            break;
        case SKIA_IMAGE_DRAW_ALPHA:
            next = alphaFilter(parameters[0]);
            break;
        case SKIA_IMAGE_DRAW_APPLY_FADE:
            applies = applyFadeToDrawnFrame(plan, index, parameters[1]);
            if (applies) {
                next = applyFadeFilter(parameters[0]);
            }
            break;
        case SKIA_IMAGE_DRAW_APPLY_COLOR:
            next = applyColorFilter(parameters[0]);
            break;
        default:
            return false;
        }
        if (!applies) {
            continue;
        }
        if (!appendFilter(&filters->content, next)) {
            return false;
        }
        if (fillStarted && !appendFilter(&filters->fill, next)) {
            return false;
        }
        colorSeen = true;
    }
    return true;
}
