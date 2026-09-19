// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia.h"
#include "skia_image_backing.h"
#include "skia_image_backing_internal.h"

#include <cstdio>
#include <cstdlib>
#include <cstdint>
#include <cstring>
#include <fstream>
#include <vector>

static bool expectEqual(Pixel actual, Pixel expected, const char* message) {
    if (actual == expected) {
        return true;
    }
    std::fprintf(stderr, "%s: expected %#x, got %#x\n", message, expected, actual);
    return false;
}

static bool readBinaryFile(const char* path, std::vector<unsigned char>& data) {
    std::ifstream input(path, std::ios::binary | std::ios::ate);
    if (!input) {
        std::fprintf(stderr, "unable to open font fixture: %s\n", path);
        return false;
    }
    const std::streamoff size = input.tellg();
    if (size <= 0) {
        std::fprintf(stderr, "font fixture is empty: %s\n", path);
        return false;
    }
    data.resize(static_cast<size_t>(size));
    input.seekg(0, std::ios::beg);
    if (!input.read(reinterpret_cast<char*>(data.data()), size)) {
        std::fprintf(stderr, "unable to read font fixture: %s\n", path);
        return false;
    }
    return true;
}

static bool testTypefaceRegistry(const char* fontPath) {
    std::vector<unsigned char> fontData;
    if (!readBinaryFile(fontPath, fontData)) {
        return false;
    }

    unsigned char invalidData[] = {0, 1, 2, 3};
    char invalidName[] = "skia-invalid-font";
    if (skia_makeTypeface(invalidName, invalidData, sizeof(invalidData)) != -1 ||
        skia_getTypefaceIndex(invalidName) != -1) {
        std::fputs("invalid TTF data entered the typeface registry\n", stderr);
        return false;
    }

    char repeatedName[] = "skia-repeated-font";
    const int32 repeatedIndex = skia_makeTypeface(
        repeatedName, fontData.data(), static_cast<int32>(fontData.size()));
    if (repeatedIndex < 0 ||
        skia_makeTypeface(repeatedName, fontData.data(), static_cast<int32>(fontData.size())) != repeatedIndex ||
        skia_getTypefaceIndex(repeatedName) != repeatedIndex) {
        std::fputs("valid typeface names were not cached stably\n", stderr);
        return false;
    }

    int32 indices[40];
    for (int i = 0; i < 40; ++i) {
        char name[64];
        std::snprintf(name, sizeof(name), "skia-capacity-font-%d", i);
        indices[i] = skia_makeTypeface(
            name, fontData.data(), static_cast<int32>(fontData.size()));
        if (indices[i] < 0 || (i > 0 && indices[i] <= indices[i - 1]) ||
            skia_getTypefaceIndex(name) != indices[i]) {
            std::fputs("typeface registry did not retain stable capacity entries\n", stderr);
            return false;
        }
    }

    for (int i = 0; i < 40; ++i) {
        char name[64];
        std::snprintf(name, sizeof(name), "skia-capacity-font-%d", i);
        if (skia_getTypefaceIndex(name) != indices[i]) {
            std::fputs("typeface registry indices changed after insertion\n", stderr);
            return false;
        }
    }
    std::puts("skia typeface registry assertions passed");
    return true;
}

static bool testBoldStyle(const char* fontPath) {
    std::vector<unsigned char> fontData;
    if (!readBinaryFile(fontPath, fontData)) {
        return false;
    }

    char name[] = "skia-bold-style-font";
    const int32 typefaceIndex = skia_makeTypeface(
        name, fontData.data(), static_cast<int32>(fontData.size()));
    if (typefaceIndex < 0) {
        std::fputs("unable to load the bold-style font fixture\n", stderr);
        return false;
    }

    const std::uint16_t text[] = {'A'};
    const double plainWidthBefore = skia_stringWidthD(
        text, sizeof(text), typefaceIndex, 32, false);
    const double boldWidth = skia_stringWidthD(
        text, sizeof(text), typefaceIndex, 32, true);
    const double plainWidthAfter = skia_stringWidthD(
        text, sizeof(text), typefaceIndex, 32, false);
    if (plainWidthBefore != plainWidthAfter || boldWidth < 0) {
        std::fputs("Skia bold state leaked into a later plain measurement\n", stderr);
        return false;
    }

    constexpr int width = 64;
    constexpr int height = 64;
    Pixel plainPixels[width * height] = {};
    Pixel boldPixels[width * height] = {};
    Pixel resetPixels[width * height] = {};
    const int plainSurface = skia_makeBitmap(-1, plainPixels, width, height);
    const int boldSurface = skia_makeBitmap(-1, boldPixels, width, height);
    const int resetSurface = skia_makeBitmap(-1, resetPixels, width, height);
    if (plainSurface < 0 || boldSurface < 0 || resetSurface < 0) {
        std::fputs("unable to create Skia bold-style test surfaces\n", stderr);
        return false;
    }

    skia_drawText(plainSurface, text, sizeof(text), 4, 40, 0xFF000000, 0, 32,
                  typefaceIndex, false);
    skia_drawText(boldSurface, text, sizeof(text), 4, 40, 0xFF000000, 0, 32,
                  typefaceIndex, true);
    skia_drawText(resetSurface, text, sizeof(text), 4, 40, 0xFF000000, 0, 32,
                  typefaceIndex, false);

    bool boldChangedPixels = false;
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            const Pixel plainPixel = skia_getPixel(plainSurface, x, y);
            const Pixel boldPixel = skia_getPixel(boldSurface, x, y);
            const Pixel resetPixel = skia_getPixel(resetSurface, x, y);
            if (plainPixel != resetPixel) {
                std::fputs("Skia bold state leaked into a later plain draw\n", stderr);
                return false;
            }
            if (plainPixel != boldPixel) {
                boldChangedPixels = true;
            }
        }
    }
    skia_deleteBitmap(plainSurface);
    skia_deleteBitmap(boldSurface);
    skia_deleteBitmap(resetSurface);
    if (!boldChangedPixels) {
        std::fputs("Skia bold draw did not alter the rendered glyph\n", stderr);
        return false;
    }
    std::puts("skia bold-style assertions passed");
    return true;
}

static bool expectBackingPixel(int64_t handle, int32 x, int32 y, Pixel expected,
                               const char* message) {
    Pixel row[8] = {};
    if (!skia_image_backing_read_argb_rows(handle, row, y, x + 1, 1)) {
        std::fprintf(stderr, "%s: unable to read backing pixel\n", message);
        return false;
    }
    const Pixel actual = row[x];
    if (actual == expected) {
        return true;
    }
    std::fprintf(stderr, "%s: expected %#x, got %#x\n", message, expected, actual);
    return false;
}

static int runRegularWritePixels(SkCanvas* targetCanvas, const SkImage* sourceImage,
                                 bool sourceOpaque, float dstLeft, float dstTop,
                                 float dstRight, float dstBottom, int32 alphaMask) {
    return skia_image_backing_internal::tryWritePixelsImage(
        targetCanvas, sourceImage, 2, 2, sourceOpaque, 0, 0, 2, 2,
        dstLeft, dstTop, dstRight, dstBottom, alphaMask, 1 << 2);
}

static bool testRegularDeviceSpaceWritePixels() {
    const std::uint8_t sourceData[] = {
        0x10, 0x20, 0x30, 0xFF, 0x40, 0x50, 0x60, 0xFF,
        0x70, 0x80, 0x90, 0xFF, 0xA0, 0xB0, 0xC0, 0xFF,
    };
    auto* sourcePixels = static_cast<std::uint8_t*>(std::malloc(sizeof(sourceData)));
    if (!sourcePixels) {
        std::fputs("unable to allocate regular writePixels source pixels\n", stderr);
        return false;
    }
    std::memcpy(sourcePixels, sourceData, sizeof(sourceData));
    const int64_t sourceBacking = skia_image_backing_create_from_rgba_pixels(
        sourcePixels, 2, 2);
    auto* sourceRecord = skia_image_backing_internal::findBacking(sourceBacking);
    const sk_sp<SkImage> sourceImage = sourceRecord ? sourceRecord->snapshot() : nullptr;
    if (!sourceBacking || !sourceImage) {
        std::fputs("unable to create regular writePixels source image\n", stderr);
        skia_image_backing_release(sourceBacking);
        return false;
    }

    skia_image_backing_reset_accounting_for_test();
    constexpr Pixel kTopLeft = 0xFF102030;
    constexpr Pixel kTopRight = 0xFF405060;
    constexpr Pixel kBottomLeft = 0xFF708090;
    constexpr Pixel kBottomRight = 0xFFA0B0C0;
    bool passed = true;

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        passed = target && canvas
            && runRegularWritePixels(canvas, sourceImage.get(), true, 1, 1, 3, 3, 255) == 1
            && expectBackingPixel(target, 1, 1, kTopLeft, "identity writePixels")
            && expectBackingPixel(target, 2, 2, kBottomRight, "identity writePixels corner");
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        if (canvas) {
            canvas->scale(2, 2);
        }
        passed = passed && target && canvas
            && runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 1, 1, 255) == 1
            && expectBackingPixel(target, 0, 0, kTopLeft, "content-scale writePixels")
            && expectBackingPixel(target, 1, 1, kBottomRight, "content-scale corner");
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        if (canvas) {
            canvas->clear(SK_ColorMAGENTA);
            canvas->save();
            canvas->clipRect(SkRect::MakeLTRB(1, 1, 2, 2));
        }
        passed = passed && target && canvas
            && runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 2, 2, 255) == 1
            && expectBackingPixel(target, 1, 1, kBottomRight, "saved partial clip writePixels")
            && expectBackingPixel(target, 0, 1, 0xFFFF00FF, "saved partial clip left edge")
            && expectBackingPixel(target, 2, 1, 0xFFFF00FF, "saved partial clip right edge")
            && expectBackingPixel(target, 1, 0, 0xFFFF00FF, "saved partial clip top edge")
            && expectBackingPixel(target, 1, 2, 0xFFFF00FF, "saved partial clip bottom edge");
        if (canvas) {
            canvas->restore();
        }
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        if (canvas) {
            canvas->clear(SK_ColorMAGENTA);
            canvas->save();
            canvas->translate(1, 0);
            canvas->clipRect(SkRect::MakeLTRB(0, 0, 1, 2));
        }
        passed = passed && target && canvas
            && runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 2, 2, 255) == 1
            && expectBackingPixel(target, 1, 0, kTopLeft, "translated partial clip top")
            && expectBackingPixel(target, 1, 1, kBottomLeft, "translated partial clip bottom")
            && expectBackingPixel(target, 0, 0, 0xFFFF00FF, "translated partial clip left edge")
            && expectBackingPixel(target, 2, 0, 0xFFFF00FF, "translated partial clip right edge")
            && expectBackingPixel(target, 1, 2, 0xFFFF00FF, "translated partial clip bottom edge")
            && expectBackingPixel(target, 1, 3, 0xFFFF00FF, "translated partial clip outside bottom");
        if (canvas) {
            canvas->restore();
        }
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        if (canvas) {
            canvas->save();
            canvas->skew(0.25f, 0);
        }
        const int result = canvas
            ? runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 2, 2, 255) : 0;
        passed = passed && target && canvas && result == 0;
        if (canvas) {
            canvas->restore();
        }
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        if (canvas) {
            canvas->save();
            canvas->rotate(15);
        }
        const int result = canvas
            ? runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 2, 2, 255) : 0;
        passed = passed && target && canvas && result == 0;
        if (canvas) {
            canvas->restore();
        }
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        if (canvas) {
            canvas->save();
            canvas->translate(0.5f, 0);
        }
        const int result = canvas
            ? runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 2, 2, 255) : 0;
        passed = passed && target && canvas && result == 0;
        if (canvas) {
            canvas->restore();
        }
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        const int result = canvas
            ? runRegularWritePixels(canvas, sourceImage.get(), true, 0, 0, 2, 2, 128) : 0;
        passed = passed && target && canvas && result == 0;
        skia_image_backing_release(target);
    }

    {
        const int64_t target = skia_image_backing_create_empty(4, 4);
        SkCanvas* canvas = skia_image_backing_canvas(target);
        const int result = canvas
            ? runRegularWritePixels(canvas, sourceImage.get(), false, 0, 0, 2, 2, 255) : 0;
        passed = passed && target && canvas && result == 0;
        skia_image_backing_release(target);
    }

    passed = passed
        && skia_image_backing_write_pixels_regular_attempts_for_test() == 9
        && skia_image_backing_write_pixels_regular_hits_for_test() == 4
        && skia_image_backing_write_pixels_regular_fallbacks_for_test() == 5
        && skia_image_backing_write_pixels_regular_copied_bytes_for_test() == 44
        && skia_image_backing_write_pixels_regular_clipped_hits_for_test() == 2;
    if (!passed) {
        std::fputs("regular device-space writePixels assertions failed\n", stderr);
        skia_image_backing_release(sourceBacking);
        return false;
    }
    skia_image_backing_release(sourceBacking);
    std::puts("regular device-space writePixels assertions passed");
    return true;
}

int main(int argc, char** argv) {
    if (argc > 1) {
        if (!testTypefaceRegistry(argv[1]) || !testBoldStyle(argv[1])) {
            return 1;
        }
    }
    if (!testRegularDeviceSpaceWritePixels()) {
        return 1;
    }
    Pixel sourcePixels[4] = { 0xFF102030, 0xFF405060, 0xFF708090, 0xFFA0B0C0 };
    Pixel destinationPixels[16] = {};
    const int source = skia_makeBitmap(-1, sourcePixels, 2, 2);
    const int destination = skia_makeBitmap(-1, destinationPixels, 4, 4);
    if (source < 0 || destination < 0) {
        std::fputs("unable to create Skia test surfaces\n", stderr);
        return 1;
    }

    Pixel channelOrderPixel = 0x1020E0FF;
    const Pixel expectedChannelOrder = 0xFF1020E0;
    const int channelOrderSurface = skia_makeBitmap(-1, &channelOrderPixel, 1, 1);
    if (channelOrderSurface < 0 ||
        !expectEqual(skia_getPixel(channelOrderSurface, 0, 0), expectedChannelOrder,
                     "asymmetric image channels")) {
        return 1;
    }

    const Pixel expected = skia_getPixel(source, 0, 0);
    skia_drawSurface(destination, source, 0, 0, 2, 2, 1, 1, 3, 3, 255);
    if (!expectEqual(skia_getPixel(destination, 1, 1), expected, "identity copy")) {
        return 1;
    }

    skia_setSurfaceScale(destination, 2);
    skia_drawSurface(destination, source, 0, 0, 2, 2, 0, 0, 2, 2, 255);
    if (!expectEqual(skia_getPixel(destination, 3, 3), skia_getPixel(source, 1, 1),
                     "scaled fallback copy")) {
        return 1;
    }

    Pixel primitivePixels[16] = {};
    const int primitiveDestination = skia_makeBitmap(-1, primitivePixels, 4, 4);
    skia_setSurfaceScale(primitiveDestination, 2);
    skia_fillRect(primitiveDestination, 1, 1, 1, 1, 0xFF223344);
    if (!expectEqual(skia_getPixel(primitiveDestination, 3, 3), 0xFF223344,
                     "scaled primitive destination")) {
        return 1;
    }
    skia_setClip(primitiveDestination, 0, 0, 1, 1);
    skia_fillRect(primitiveDestination, 1, 1, 1, 1, 0xFF556677);
    skia_restoreClip(primitiveDestination);
    if (!expectEqual(skia_getPixel(primitiveDestination, 3, 3), 0xFF223344,
                     "scaled logical clip")) {
        return 1;
    }

    const double scales[] = {1.0, 1.5, 2.0, 3.0};
    for (double scale : scales) {
        Pixel scalePixels[576] = {};
        const int scaleDestination = skia_makeBitmap(-1, scalePixels, 24, 24);
        skia_setSurfaceScale(scaleDestination, scale);
        skia_fillRect(scaleDestination, 2, 2, 4, 4, 0xFF778899);
        const int inside = static_cast<int>(3 * scale);
        if (!expectEqual(skia_getPixel(scaleDestination, inside, inside), 0xFF778899,
                         "scaled primitive coverage")) {
            return 1;
        }
        skia_deleteBitmap(scaleDestination);
    }

    skia_setPixel(destination, 3, 3, 0xFF010203);
    if (!expectEqual(skia_getPixel(destination, 3, 3), 0xFF010203, "physical raw pixel")) {
        return 1;
    }

    Pixel clippedPixels[16] = {};
    const int clippedDestination = skia_makeBitmap(-1, clippedPixels, 4, 4);
    skia_setClip(clippedDestination, 0, 0, 1, 1);
    skia_drawSurface(clippedDestination, source, 0, 0, 2, 2, 1, 1, 3, 3, 255);
    skia_restoreClip(clippedDestination);
    if (!expectEqual(skia_getPixel(clippedDestination, 1, 1), 0, "clipped fallback copy")) {
        return 1;
    }

    auto* firstPixels = new uint8_t[8]{ 0x10, 0x20, 0x30, 0xFF, 0x40, 0x50, 0x60, 0xFF };
    auto* secondPixels = new uint8_t[8]{ 0xA0, 0xB0, 0xC0, 0xFF, 0xD0, 0xE0, 0xF0, 0xFF };
    const int64_t firstBacking = skia_image_backing_create_from_rgba_pixels(firstPixels, 2, 1);
    const int64_t secondBacking = skia_image_backing_create_from_rgba_pixels(secondPixels, 2, 1);
    const int64_t mutableBacking = skia_image_backing_create_empty(2, 1);
    const int64_t invalidBacking = 0x7fffffff;
    if (!firstBacking || !secondBacking || !mutableBacking ||
        !skia_image_backing_draw(mutableBacking, firstBacking, 0, 0, 2, 1, 0, 0, 2, 1, 255)) {
        std::fputs("unable to create native image backing probe surfaces\n", stderr);
        return 1;
    }

    Pixel snapshotPixels[2] = {};
    const int64_t snapshotBacking = skia_image_backing_snapshot(mutableBacking);
    if (!snapshotBacking || !skia_image_backing_read_row(snapshotBacking, snapshotPixels, 0, 2) ||
        !expectEqual(snapshotPixels[0], 0xFF102030, "native backing ARGB readback") ||
        !expectEqual(snapshotPixels[1], 0xFF405060, "native backing row readback")) {
        return 1;
    }

    if (!skia_image_backing_draw(mutableBacking, secondBacking, 0, 0, 2, 1, 0, 0, 2, 1, 255) ||
        !skia_image_backing_read_row(snapshotBacking, snapshotPixels, 0, 2) ||
        !expectEqual(snapshotPixels[0], 0xFF102030, "native snapshot copy-on-write") ||
        !skia_image_backing_read_row(mutableBacking, snapshotPixels, 0, 2) ||
        !expectEqual(snapshotPixels[0], 0xFFA0B0C0, "native mutable surface update")) {
        return 1;
    }

    if (skia_image_backing_snapshot(invalidBacking) != 0 ||
        skia_image_backing_read_row(invalidBacking, snapshotPixels, 0, 2)) {
        std::fputs("invalid native backing handle was accepted\n", stderr);
        return 1;
    }
    skia_image_backing_release(firstBacking);
    skia_image_backing_release(secondBacking);
    skia_image_backing_release(mutableBacking);
    skia_image_backing_release(snapshotBacking);
    skia_image_backing_release(snapshotBacking);

    skia_deleteBitmap(source);
    skia_deleteBitmap(destination);
    skia_deleteBitmap(channelOrderSurface);
    skia_deleteBitmap(primitiveDestination);
    skia_deleteBitmap(clippedDestination);
    std::puts("skia surface copy assertions passed");
    return 0;
}
