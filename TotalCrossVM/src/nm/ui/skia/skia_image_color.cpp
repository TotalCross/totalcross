// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_image_backing_internal.h"

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

static bool transform(std::vector<uint8_t>* pixels, int32 width, int32 height, int32 operation,
                      int32 parameter1, int32 parameter2, int32 frameCount, int32 visibleWidth,
                      int32 currentFrame) {
    if (!pixels || width <= 0 || height <= 0) {
        return false;
    }
    size_t first = 0;
    size_t last = pixels->size();
    if (operation == SKIA_IMAGE_COLOR_APPLY_FADE && frameCount > 1) {
        if (visibleWidth <= 0 || static_cast<int64_t>(visibleWidth) * frameCount > width) {
            return false;
        }
        int frame = currentFrame < 0 ? frameCount - 1 : currentFrame;
        if (frame >= frameCount) {
            frame = 0;
        }
        first = static_cast<size_t>(frame) * visibleWidth * 4;
        last = first + static_cast<size_t>(visibleWidth) * height * 4;
    }

    switch (operation) {
    case SKIA_IMAGE_COLOR_APPLY_FADE:
        for (size_t i = first; i < last; i += 4) {
            (*pixels)[i] = static_cast<uint8_t>((*pixels)[i] * parameter1 / 255);
            (*pixels)[i + 1] = static_cast<uint8_t>((*pixels)[i + 1] * parameter1 / 255);
            (*pixels)[i + 2] = static_cast<uint8_t>((*pixels)[i + 2] * parameter1 / 255);
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
