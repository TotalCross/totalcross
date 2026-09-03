// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia.h"

#include <cstdio>
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
    for (int i = 0; i < width * height; ++i) {
        if (plainPixels[i] != resetPixels[i]) {
            std::fputs("Skia bold state leaked into a later plain draw\n", stderr);
            return false;
        }
        if (plainPixels[i] != boldPixels[i]) {
            boldChangedPixels = true;
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

int main(int argc, char** argv) {
    if (argc > 1) {
        if (!testTypefaceRegistry(argv[1]) || !testBoldStyle(argv[1])) {
            return 1;
        }
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

    skia_deleteBitmap(source);
    skia_deleteBitmap(destination);
    skia_deleteBitmap(channelOrderSurface);
    skia_deleteBitmap(primitiveDestination);
    skia_deleteBitmap(clippedDestination);
    std::puts("skia surface copy assertions passed");
    return 0;
}
