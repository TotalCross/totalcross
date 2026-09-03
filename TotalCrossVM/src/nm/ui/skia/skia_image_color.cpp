// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"

#include <algorithm>
#include <cmath>
#include <cstddef>
#include <cstdint>
#include <limits>
#include <memory>
#include <vector>

using skia_image_backing_internal::NativeImageBackingRecord;
using skia_image_backing_internal::findBacking;
using skia_image_backing_internal::rasterInfo;
using skia_image_backing_internal::registerBacking;

namespace {

static bool readRgba(NativeImageBackingRecord* source, std::vector<uint8_t>* pixels) {
    if (!source || !pixels || source->width <= 0 || source->height <= 0) {
        return false;
    }
    const uint64_t pixelCount = static_cast<uint64_t>(source->width)
        * static_cast<uint64_t>(source->height);
    if (pixelCount > std::numeric_limits<size_t>::max() / 4) {
        return false;
    }
    try {
        pixels->resize(static_cast<size_t>(pixelCount) * 4);
        sk_sp<SkImage> image = source->snapshot();
        return image && image->readPixels(rasterInfo(source->width, source->height), pixels->data(),
            static_cast<size_t>(source->width) * 4, 0, 0);
    } catch (const std::bad_alloc&) {
        return false;
    }
}

static sk_sp<SkImage> makeImage(const std::vector<uint8_t>& pixels, int32 width, int32 height) {
    if (width <= 0 || height <= 0 || pixels.size() != static_cast<size_t>(width) * height * 4) {
        return nullptr;
    }
    sk_sp<SkData> data = SkData::MakeWithCopy(pixels.data(), pixels.size());
    if (!data) {
        return nullptr;
    }
    return SkImage::MakeRasterData(rasterInfo(width, height), data,
        static_cast<size_t>(width) * 4);
}

static int channelAverage(int first, int second) {
    return (first + second) / 2;
}

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

static void touchUp(std::vector<uint8_t>* pixels, int32 brightness, int32 contrast) {
    uint8_t table[256];
    const bool useContrast = contrast != 0;
    const bool useBrightness = brightness != 0;
    int32 multiplier = 0;
    int32 offset = 0;
    if (useContrast) {
        buildContrastTable(contrast, table);
    }
    if (useBrightness) {
        const double effective = (brightness + 128.0) / 128.0;
        if (brightness <= 1) {
            multiplier = static_cast<int32>(std::sqrt(effective) * 0x10000);
        } else {
            double f = effective - 1.0;
            f *= f;
            offset = static_cast<int32>(f * 0xFF0000);
            multiplier = static_cast<int32>((1.0 - f) * effective * 0x10000);
        }
    }
    for (size_t i = 0; i < pixels->size(); i += 4) {
        int red = (*pixels)[i];
        int green = (*pixels)[i + 1];
        int blue = (*pixels)[i + 2];
        if (useContrast) {
            red = table[red];
            green = table[green];
            blue = table[blue];
        }
        if (useBrightness) {
            red = (multiplier * red + offset) >> 16;
            green = (multiplier * green + offset) >> 16;
            blue = (multiplier * blue + offset) >> 16;
        }
        (*pixels)[i] = static_cast<uint8_t>(clampChannel(red));
        (*pixels)[i + 1] = static_cast<uint8_t>(clampChannel(green));
        (*pixels)[i + 2] = static_cast<uint8_t>(clampChannel(blue));
    }
}

static bool transform(std::vector<uint8_t>* pixels, int32 width, int32 height, int32 operation,
                      int32 parameter1, int32 parameter2, int32 frameCount, int32 visibleWidth,
                      int32 currentFrame) {
    if (!pixels || width <= 0 || height <= 0) {
        return false;
    }
    size_t first = 0;
    size_t last = pixels->size();
    size_t rowStride = 0;
    if (operation == SKIA_IMAGE_COLOR_APPLY_FADE && frameCount > 1) {
        if (visibleWidth <= 0 || static_cast<int64_t>(visibleWidth) * frameCount > width) {
            return false;
        }
        int frame = currentFrame < 0 ? frameCount - 1 : currentFrame;
        if (frame >= frameCount) {
            frame = 0;
        }
        rowStride = static_cast<size_t>(width) * 4;
        first = static_cast<size_t>(frame) * visibleWidth * 4;
        last = first + static_cast<size_t>(visibleWidth) * 4;
    }

    switch (operation) {
    case SKIA_IMAGE_COLOR_APPLY_FADE:
        if (frameCount > 1) {
            for (int32 y = 0; y < height; ++y) {
                const size_t rowFirst = static_cast<size_t>(y) * rowStride + first;
                for (size_t i = rowFirst; i < rowFirst + (last - first); i += 4) {
                    (*pixels)[i] = static_cast<uint8_t>((*pixels)[i] * parameter1 / 255);
                    (*pixels)[i + 1] = static_cast<uint8_t>((*pixels)[i + 1] * parameter1 / 255);
                    (*pixels)[i + 2] = static_cast<uint8_t>((*pixels)[i + 2] * parameter1 / 255);
                }
            }
        } else {
            for (size_t i = first; i < last; i += 4) {
                (*pixels)[i] = static_cast<uint8_t>((*pixels)[i] * parameter1 / 255);
                (*pixels)[i + 1] = static_cast<uint8_t>((*pixels)[i + 1] * parameter1 / 255);
                (*pixels)[i + 2] = static_cast<uint8_t>((*pixels)[i + 2] * parameter1 / 255);
            }
        }
        return true;
    case SKIA_IMAGE_COLOR_FADE_INSTANCE: {
        const int red = (parameter1 >> 16) & 0xff;
        const int green = (parameter1 >> 8) & 0xff;
        const int blue = parameter1 & 0xff;
        for (size_t i = 0; i < pixels->size(); i += 4) {
            (*pixels)[i] = static_cast<uint8_t>(channelAverage(red, (*pixels)[i]));
            (*pixels)[i + 1] = static_cast<uint8_t>(channelAverage(green, (*pixels)[i + 1]));
            (*pixels)[i + 2] = static_cast<uint8_t>(channelAverage(blue, (*pixels)[i + 2]));
        }
        return true;
    }
    case SKIA_IMAGE_COLOR_ALPHA_INSTANCE:
        for (size_t i = 0; i < pixels->size(); i += 4) {
            if ((*pixels)[i + 3] != 0) {
                (*pixels)[i + 3] = static_cast<uint8_t>(clampChannel((*pixels)[i + 3] + parameter1));
            }
        }
        return true;
    case SKIA_IMAGE_COLOR_TOUCH_UP_INSTANCE:
        touchUp(pixels, parameter1, parameter2);
        return true;
    case SKIA_IMAGE_COLOR_APPLY_COLOR: {
        const int multiplierRed = static_cast<int>(std::sqrt((((parameter1 >> 16) & 0xff) + 128.0) / 128.0)
            * 0x10000);
        const int multiplierGreen = static_cast<int>(std::sqrt((((parameter1 >> 8) & 0xff) + 128.0) / 128.0)
            * 0x10000);
        const int multiplierBlue = static_cast<int>(std::sqrt(((parameter1 & 0xff) + 128.0) / 128.0)
            * 0x10000);
        for (size_t i = 0; i < pixels->size(); i += 4) {
            if ((*pixels)[i + 3] == 0) {
                continue;
            }
            (*pixels)[i] = static_cast<uint8_t>(clampChannel((multiplierRed * (*pixels)[i]) >> 16));
            (*pixels)[i + 1] = static_cast<uint8_t>(clampChannel((multiplierGreen * (*pixels)[i + 1]) >> 16));
            (*pixels)[i + 2] = static_cast<uint8_t>(clampChannel((multiplierBlue * (*pixels)[i + 2]) >> 16));
        }
        return true;
    }
    case SKIA_IMAGE_COLOR_APPLY_COLOR2: {
        const int targetRed = (parameter1 >> 16) & 0xff;
        const int targetGreen = (parameter1 >> 8) & 0xff;
        const int targetBlue = parameter1 & 0xff;
        const bool changeAlpha = ((parameter1 >> 24) & 0xff) == 0xaa;
        int highestBrightness = 0;
        int highestRed = 0;
        int highestGreen = 0;
        int highestBlue = 0;
        for (size_t i = 0; i < pixels->size(); i += 4) {
            if ((*pixels)[i + 3] == 0xff) {
                const int brightness = (3 * (*pixels)[i] + 4 * (*pixels)[i + 1] + (*pixels)[i + 2]) >> 3;
                if (brightness > highestBrightness) {
                    highestBrightness = brightness;
                    highestRed = (*pixels)[i];
                    highestGreen = (*pixels)[i + 1];
                    highestBlue = (*pixels)[i + 2];
                }
            }
        }
        if (highestRed == 0) {
            highestRed = 255;
        }
        if (highestGreen == 0) {
            highestGreen = 255;
        }
        if (highestBlue == 0) {
            highestBlue = 255;
        }
        const int highestChannel = std::max(highestRed, std::max(highestGreen, highestBlue));
        for (size_t i = 0; i < pixels->size(); i += 4) {
            if ((*pixels)[i + 3] == 0) {
                continue;
            }
            const int red = clampChannel((*pixels)[i] * targetRed / highestRed);
            const int green = clampChannel((*pixels)[i + 1] * targetGreen / highestGreen);
            const int blue = clampChannel((*pixels)[i + 2] * targetBlue / highestBlue);
            if (changeAlpha) {
                const int brightest = std::max((*pixels)[i], std::max((*pixels)[i + 1], (*pixels)[i + 2]));
                (*pixels)[i + 3] = static_cast<uint8_t>(clampChannel(brightest * 255 / highestChannel));
            }
            (*pixels)[i] = static_cast<uint8_t>(red);
            (*pixels)[i + 1] = static_cast<uint8_t>(green);
            (*pixels)[i + 2] = static_cast<uint8_t>(blue);
        }
        return true;
    }
    case SKIA_IMAGE_COLOR_CHANGE_COLORS: {
        const uint32_t from = static_cast<uint32_t>(parameter1);
        const uint32_t to = static_cast<uint32_t>(parameter2);
        for (size_t i = 0; i < pixels->size(); i += 4) {
            const uint32_t pixel = (static_cast<uint32_t>((*pixels)[i + 3]) << 24)
                | (static_cast<uint32_t>((*pixels)[i]) << 16)
                | (static_cast<uint32_t>((*pixels)[i + 1]) << 8)
                | static_cast<uint32_t>((*pixels)[i + 2]);
            if (pixel == from) {
                (*pixels)[i] = static_cast<uint8_t>(to >> 16);
                (*pixels)[i + 1] = static_cast<uint8_t>(to >> 8);
                (*pixels)[i + 2] = static_cast<uint8_t>(to);
                (*pixels)[i + 3] = static_cast<uint8_t>(to >> 24);
            }
        }
        return true;
    }
    case SKIA_IMAGE_COLOR_SET_TRANSPARENT_COLOR: {
        const uint32_t color = static_cast<uint32_t>(parameter1);
        if (static_cast<int32_t>(color) == -1) {
            for (size_t i = 0; i < pixels->size(); i += 4) {
                (*pixels)[i + 3] = 255;
            }
            return true;
        }
        const uint32_t rgb = color & 0x00ffffff;
        for (size_t i = 0; i < pixels->size(); i += 4) {
            const uint32_t pixelRgb = (static_cast<uint32_t>((*pixels)[i]) << 16)
                | (static_cast<uint32_t>((*pixels)[i + 1]) << 8)
                | static_cast<uint32_t>((*pixels)[i + 2]);
            if (pixelRgb == rgb) {
                (*pixels)[i] = static_cast<uint8_t>(rgb >> 16);
                (*pixels)[i + 1] = static_cast<uint8_t>(rgb >> 8);
                (*pixels)[i + 2] = static_cast<uint8_t>(rgb);
                (*pixels)[i + 3] = 0;
            } else {
                (*pixels)[i + 3] = 255;
            }
        }
        return true;
    }
    default:
        return false;
    }
}

static bool transformedPixels(int64_t handle, int32 operation, int32 parameter1, int32 parameter2,
                              int32 frameCount, int32 visibleWidth, int32 currentFrame,
                              std::vector<uint8_t>* pixels, NativeImageBackingRecord** source) {
    *source = findBacking(handle);
    if (!*source || !readRgba(*source, pixels)) {
        return false;
    }
    return transform(pixels, (*source)->width, (*source)->height, operation, parameter1, parameter2,
        frameCount, visibleWidth, currentFrame);
}

}

int skia_image_backing_apply_color_mutation(int64_t handle, int32 operation, int32 parameter1,
                                            int32 parameter2, int32 frameCount, int32 visibleWidth,
                                            int32 currentFrame) {
    try {
        std::vector<uint8_t> pixels;
        NativeImageBackingRecord* source = nullptr;
        if (!transformedPixels(handle, operation, parameter1, parameter2, frameCount, visibleWidth,
                               currentFrame, &pixels, &source)) {
            return 0;
        }
        sk_sp<SkImage> image = makeImage(pixels, source->width, source->height);
        if (!image) {
            return 0;
        }
        source->image = std::move(image);
        source->surface.reset();
        return 1;
    } catch (const std::bad_alloc&) {
        return 0;
    }
}

int64_t skia_image_backing_create_color_instance(int64_t handle, int32 operation, int32 parameter1,
                                                 int32 parameter2) {
    try {
        std::vector<uint8_t> pixels;
        NativeImageBackingRecord* source = nullptr;
        if (!transformedPixels(handle, operation, parameter1, parameter2, 1, 0, 0, &pixels, &source)) {
            return 0;
        }
        sk_sp<SkImage> image = makeImage(pixels, source->width, source->height);
        if (!image) {
            return 0;
        }
        std::unique_ptr<NativeImageBackingRecord> result(new NativeImageBackingRecord());
        result->image = std::move(image);
        result->width = source->width;
        result->height = source->height;
        return registerBacking(std::move(result));
    } catch (const std::bad_alloc&) {
        return 0;
    }
}
