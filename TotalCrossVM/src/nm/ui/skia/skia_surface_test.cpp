// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia.h"

#include <cstdio>
#include <cstring>

static bool expectEqual(Pixel actual, Pixel expected, const char* message) {
    if (actual == expected) {
        return true;
    }
    std::fprintf(stderr, "%s: expected %#x, got %#x\n", message, expected, actual);
    return false;
}

int main() {
    Pixel sourcePixels[4] = { 0xFF102030, 0xFF405060, 0xFF708090, 0xFFA0B0C0 };
    Pixel destinationPixels[16] = {};
    const int source = skia_makeBitmap(-1, sourcePixels, 2, 2);
    const int destination = skia_makeBitmap(-1, destinationPixels, 4, 4);
    if (source < 0 || destination < 0) {
        std::fputs("unable to create Skia test surfaces\n", stderr);
        return 1;
    }

    Pixel channelOrderPixel = 0xFF1020E0;
    const int channelOrderSurface = skia_makeBitmap(-1, &channelOrderPixel, 1, 1);
    if (channelOrderSurface < 0 ||
        !expectEqual(skia_getPixel(channelOrderSurface, 0, 0), channelOrderPixel,
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
