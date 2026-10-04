// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia.h"
#include "skia_image_backing.h"

#include <cstdio>
#include <cstdint>
#include <fstream>
#include <vector>

static bool expectEqual(Pixel actual, Pixel expected, const char* message) {
    if (actual == expected) {
        return true;
    }
    std::fprintf(stderr, "%s: expected %#x, got %#x\n", message, expected, actual);
    return false;
}

static bool testPhysicalIdentityCopyCase(int operation, int32 alphaMask,
                                         bool expectCopyHit, bool expectIdentityHit,
                                         bool partialClip = false, bool identityEnabled = true,
                                         bool colorFilter = false, int colorType = 0,
                                         double outputScale = 1.0,
                                         bool failDirectWritePixels = false,
                                         bool physicalCopyOnly = false) {
    const uint8_t sourceRgba[] = {
        0x10, 0x20, 0x30, 0xFF, 0x40, 0x50, 0x60, 0xFF,
        0x70, 0x80, 0x90, 0xFF, 0xA0, 0xB0, 0xC0, 0xFF
    };
    const int64_t sourceHandle = colorType == 0 ? skia_image_backing_create_empty(2, 2)
        : skia_image_backing_create_empty_with_color_type_for_test(2, 2, colorType);
    const int64_t destinationHandle = colorType == 0 ? skia_image_backing_create_empty(4, 4)
        : skia_image_backing_create_empty_with_color_type_for_test(4, 4, colorType);
    const int64_t fallbackHandle = colorType == 0 ? skia_image_backing_create_empty(4, 4)
        : skia_image_backing_create_empty_with_color_type_for_test(4, 4, colorType);
    if (sourceHandle == 0 || destinationHandle == 0 || fallbackHandle == 0
        || !skia_image_backing_write_rgba_pixels(sourceHandle, sourceRgba, 0, 0, 2, 2, 8)) {
        std::fputs("unable to create physical-copy test backings\n", stderr);
        if (sourceHandle != 0) skia_image_backing_release(sourceHandle);
        if (destinationHandle != 0) skia_image_backing_release(destinationHandle);
        if (fallbackHandle != 0) skia_image_backing_release(fallbackHandle);
        return false;
    }
    const int32 destinationSurface = skia_image_backing_surface_id(destinationHandle);
    const int32 fallbackSurface = skia_image_backing_surface_id(fallbackHandle);
    if (destinationSurface == SKIA_INVALID_SURFACE_ID || fallbackSurface == SKIA_INVALID_SURFACE_ID) {
        std::fputs("unable to create physical-copy destination alias\n", stderr);
        skia_image_backing_release(sourceHandle);
        skia_image_backing_release(destinationHandle);
        skia_image_backing_release(fallbackHandle);
        return false;
    }

    int32 operations[] = {operation, SKIA_IMAGE_DRAW_ALPHA};
    int32 parameters[] = {operation == SKIA_IMAGE_DRAW_CROP ? 1 : 0, 0, 0, 0, 128, 0, 0, 0};
    const int32 operationWidth = operation == SKIA_IMAGE_DRAW_CROP
        || (operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE && outputScale > 1.0) ? 1 : 2;
    const int32 operationHeight = operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE && outputScale > 1.0
        ? 1 : 2;
    int32 dimensions[] = {operationWidth, operationHeight, operationWidth, operationHeight};
    SkiaImageDrawPlanData plan = {};
    plan.rootHandle = sourceHandle;
    plan.rootWidth = 2;
    plan.rootHeight = 2;
    plan.rootLogicalWidth = 2;
    plan.rootLogicalHeight = 2;
    plan.rootFrameCount = 1;
    plan.rootWidthOfAllFrames = 2;
    plan.rootContentScale = 1;
    plan.operations = operations;
    plan.parameters = parameters;
    plan.dimensions = dimensions;
    plan.operationCount = colorFilter ? 2 : 1;
    plan.outputWidth = dimensions[0];
    plan.outputHeight = dimensions[1];
    plan.outputFrameCount = 1;
    plan.outputWidthOfAllFrames = dimensions[0];
    plan.alphaMask = alphaMask;
    plan.materializeAlphaMask = 255;
    plan.outputAlphaMask = alphaMask;
    plan.sourceBackingStable = 1;
    plan.sourceOpacityState = 1;
    plan.physicalIdentityEnabled = identityEnabled;
    plan.destinationScale = outputScale;
    plan.outputContentScale = outputScale;
    plan.hwScaleW = 1;
    plan.hwScaleH = 1;
    plan.rootHwScaleW = 1;
    plan.rootHwScaleH = 1;

    const int32 sourceLeft = partialClip ? 1 : 0;
    const int32 destinationX = partialClip ? 2 : 1;
    const int32 destinationY = 1;
    const float sourceRight = static_cast<float>(dimensions[0]);
    const float sourceBottom = static_cast<float>(dimensions[1]);
    const float destinationRight = destinationX + sourceRight - sourceLeft;
    const float destinationBottom = destinationY + sourceBottom;
    if (outputScale != 1.0) {
        skia_setSurfaceScale(destinationSurface, outputScale);
        skia_setSurfaceScale(fallbackSurface, outputScale);
    }
    skia_setClip(destinationSurface, partialClip ? 2 : 0, 0, partialClip ? 3 : 4, 4);
    if (failDirectWritePixels) {
        skia_image_geometry_fail_next_physical_copy_write_pixels_for_test();
    }
    const int status = skia_image_backing_draw_geometry_to_surface(destinationSurface, &plan,
        static_cast<float>(sourceLeft), 0, sourceRight, sourceBottom, destinationX, destinationY,
        destinationRight, destinationBottom, true, physicalCopyOnly, false);
    skia_restoreClip(destinationSurface);

    SkiaImageDrawPlanData fallbackPlan = plan;
    skia_setClip(fallbackSurface, partialClip ? 2 : 0, 0, partialClip ? 3 : 4, 4);
    const int fallbackStatus = skia_image_backing_draw_geometry_to_surface(fallbackSurface,
        &fallbackPlan, static_cast<float>(sourceLeft), 0, sourceRight, sourceBottom,
        destinationX, destinationY, destinationRight, destinationBottom, false, false, false);
    skia_restoreClip(fallbackSurface);

    const bool copyHit = (status & SKIA_IMAGE_DRAW_PHYSICAL_COPY_HIT) != 0;
    const bool identityHit = (status & SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_HIT) != 0;
    const bool copyAttempt = (status & SKIA_IMAGE_DRAW_PHYSICAL_COPY_ATTEMPT) != 0;
    const bool copyFallback = (status & SKIA_IMAGE_DRAW_PHYSICAL_COPY_FALLBACK) != 0;
    const bool identityAttempt = (status & SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_ATTEMPT) != 0;
    const bool copyFallbackExpected = identityEnabled && !expectCopyHit;
    bool pixelsMatch = false;
    if (physicalCopyOnly && !expectCopyHit) {
        pixelsMatch = true;
    } else if (colorType != 0 || outputScale != 1.0
        || (operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE && !expectCopyHit)) {
        pixelsMatch = true;
    } else if (operation == SKIA_IMAGE_DRAW_CROP && alphaMask == 255) {
        pixelsMatch = expectEqual(skia_getPixel(destinationSurface, 1, 1), 0xFF405060,
                                  "cropped physical copy first pixel")
            && expectEqual(skia_getPixel(destinationSurface, 1, 2), 0xFFA0B0C0,
                           "cropped physical copy second pixel")
            && expectEqual(skia_getPixel(destinationSurface, 0, 0), 0,
                           "cropped physical copy leaves other pixels unchanged");
    } else if (operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE && alphaMask == 255 && partialClip) {
        pixelsMatch = expectEqual(skia_getPixel(destinationSurface, 2, 1), 0xFF405060,
                                  "clipped physical copy first visible pixel")
            && expectEqual(skia_getPixel(destinationSurface, 2, 2), 0xFFA0B0C0,
                           "clipped physical copy last visible pixel")
            && expectEqual(skia_getPixel(destinationSurface, 1, 1), 0,
                           "clipped physical copy leaves hidden pixel unchanged");
    } else if (operation == SKIA_IMAGE_DRAW_SMOOTH_SCALE && alphaMask == 255) {
        pixelsMatch = expectEqual(skia_getPixel(destinationSurface, 1, 1), 0xFF102030,
                                  "unit smooth physical copy first pixel")
            && expectEqual(skia_getPixel(destinationSurface, 2, 2), 0xFFA0B0C0,
                           "unit smooth physical copy last pixel");
    } else {
      const Pixel blended = skia_getPixel(destinationSurface, 1, 1);
      pixelsMatch = (blended >> 24) >= 127 && (blended >> 24) <= 129;
    }

    bool fallbackParity = (fallbackStatus & SKIA_IMAGE_DRAW_HANDLED) != 0;
    if (physicalCopyOnly && !expectCopyHit) {
        for (int y = 0; y < 4 && fallbackParity; ++y) {
            for (int x = 0; x < 4; ++x) {
                if (skia_getPixel(destinationSurface, x, y) != 0) {
                    std::fprintf(stderr, "physical-only miss changed destination at %d,%d: %#x\n",
                        x, y, skia_getPixel(destinationSurface, x, y));
                    fallbackParity = false;
                    break;
                }
            }
        }
    } else {
        for (int y = 0; y < 4 && fallbackParity; ++y) {
            for (int x = 0; x < 4; ++x) {
                if (skia_getPixel(destinationSurface, x, y) != skia_getPixel(fallbackSurface, x, y)) {
                    std::fprintf(stderr, "copy/fallback pixel mismatch at %d,%d: %#x != %#x\n", x, y,
                        skia_getPixel(destinationSurface, x, y), skia_getPixel(fallbackSurface, x, y));
                    fallbackParity = false;
                    break;
                }
            }
        }
    }
    const bool directHitAvoidedGeneric = !copyHit
        || (status & (SKIA_IMAGE_DRAW_GENERIC_GEOMETRY | SKIA_IMAGE_DRAW_SMOOTH_RESAMPLE)) == 0;
    const int forbiddenPhysicalProbeStatus = SKIA_IMAGE_DRAW_TARGET_COLOR_ATTEMPT
        | SKIA_IMAGE_DRAW_TARGET_COLOR_HIT | SKIA_IMAGE_DRAW_TARGET_COLOR_MATERIALIZED
        | SKIA_IMAGE_DRAW_TARGET_COLOR_FALLBACK | SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_ATTEMPT
        | SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_HIT | SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_MATERIALIZED
        | SKIA_IMAGE_DRAW_PHYSICAL_VARIANT_FALLBACK | SKIA_IMAGE_DRAW_GENERIC_GEOMETRY
        | SKIA_IMAGE_DRAW_SMOOTH_RESAMPLE;
    const bool physicalProbeStayedPhysical = !physicalCopyOnly
        || ((status & forbiddenPhysicalProbeStatus) == 0
            && ((status & SKIA_IMAGE_DRAW_HANDLED) != 0) == expectCopyHit);

    skia_image_backing_release(sourceHandle);
    skia_image_backing_release(destinationHandle);
    skia_image_backing_release(fallbackHandle);
    if (!pixelsMatch || copyHit != expectCopyHit || identityHit != expectIdentityHit
        || copyAttempt != identityEnabled || identityAttempt != identityEnabled
        || copyFallback != copyFallbackExpected
        || (!physicalCopyOnly && (status & SKIA_IMAGE_DRAW_HANDLED) == 0)
        || !fallbackParity || !directHitAvoidedGeneric || !physicalProbeStayedPhysical) {
        std::fprintf(stderr, "physical-copy status mismatch type=%d operation=%d: status=%#x fallback=%#x copyHit=%d copyFallback=%d identityHit=%d parity=%d\n",
                     colorType, operation, status, fallbackStatus, copyHit, copyFallback, identityHit, fallbackParity);
        return false;
    }
    return true;
}

static bool testPhysicalCopyFallbackCase(const char* name, int destinationColorType,
                                         bool fractionalDestination, int rotationAngle,
                                         bool skewDestination, bool overlappingStorage) {
    const uint8_t sourceRgba[] = {
        0x10, 0x20, 0x30, 0xFF, 0x40, 0x50, 0x60, 0xFF,
        0x70, 0x80, 0x90, 0xFF, 0xA0, 0xB0, 0xC0, 0xFF
    };
    const int64_t sourceHandle = skia_image_backing_create_empty_with_color_type_for_test(2, 2, 0);
    const int64_t fallbackSourceHandle = skia_image_backing_create_empty_with_color_type_for_test(2, 2, 0);
    const int64_t destinationHandle = overlappingStorage ? sourceHandle
        : skia_image_backing_create_empty_with_color_type_for_test(4, 4, destinationColorType);
    const int64_t fallbackHandle = overlappingStorage ? fallbackSourceHandle
        : skia_image_backing_create_empty_with_color_type_for_test(4, 4, destinationColorType);
    if (!sourceHandle || !fallbackSourceHandle || !destinationHandle || !fallbackHandle
        || !skia_image_backing_write_rgba_pixels(sourceHandle, sourceRgba, 0, 0, 2, 2, 8)
        || !skia_image_backing_write_rgba_pixels(fallbackSourceHandle, sourceRgba, 0, 0, 2, 2, 8)) {
        std::fprintf(stderr, "unable to create %s direct-copy fallback surfaces\n", name);
        if (sourceHandle) skia_image_backing_release(sourceHandle);
        if (fallbackSourceHandle) skia_image_backing_release(fallbackSourceHandle);
        if (destinationHandle && destinationHandle != sourceHandle) skia_image_backing_release(destinationHandle);
        if (fallbackHandle && fallbackHandle != fallbackSourceHandle) skia_image_backing_release(fallbackHandle);
        return false;
    }
    const int32 destinationSurface = skia_image_backing_surface_id(destinationHandle);
    const int32 fallbackSurface = skia_image_backing_surface_id(fallbackHandle);
    if (destinationSurface == SKIA_INVALID_SURFACE_ID || fallbackSurface == SKIA_INVALID_SURFACE_ID) {
        std::fprintf(stderr, "unable to access %s direct-copy fallback surfaces\n", name);
        skia_image_backing_release(sourceHandle);
        skia_image_backing_release(fallbackSourceHandle);
        if (destinationHandle != sourceHandle) skia_image_backing_release(destinationHandle);
        if (fallbackHandle != fallbackSourceHandle) skia_image_backing_release(fallbackHandle);
        return false;
    }
    const int width = overlappingStorage ? 2 : 4;
    const int height = overlappingStorage ? 2 : 4;
    if (!overlappingStorage) {
        skia_fillRect(destinationSurface, 0, 0, width, height, 0xFF010203);
        skia_fillRect(fallbackSurface, 0, 0, width, height, 0xFF010203);
    }

    const int32 operation = rotationAngle >= 0 ? SKIA_IMAGE_DRAW_ROTATE_SCALE : SKIA_IMAGE_DRAW_CROP;
    int32 operations[] = {operation};
    int32 parameters[] = {rotationAngle >= 0 ? 1 : 0, rotationAngle >= 0 ? rotationAngle : 0, 0, 0};
    int32 dimensions[] = {2, 2};
    SkiaImageDrawPlanData plan = {};
    plan.rootHandle = sourceHandle;
    plan.rootWidth = 2;
    plan.rootHeight = 2;
    plan.rootLogicalWidth = 2;
    plan.rootLogicalHeight = 2;
    plan.rootFrameCount = 1;
    plan.rootWidthOfAllFrames = 2;
    plan.rootContentScale = 1;
    plan.operations = operations;
    plan.parameters = parameters;
    plan.dimensions = dimensions;
    plan.operationCount = 1;
    plan.outputWidth = 2;
    plan.outputHeight = 2;
    plan.outputFrameCount = 1;
    plan.outputWidthOfAllFrames = 2;
    plan.alphaMask = 255;
    plan.materializeAlphaMask = 255;
    plan.outputAlphaMask = 255;
    plan.sourceBackingStable = 1;
    plan.sourceOpacityState = 1;
    plan.physicalIdentityEnabled = 1;
    plan.destinationScale = 1;
    plan.outputContentScale = 1;
    plan.hwScaleW = 1;
    plan.hwScaleH = 1;
    plan.rootHwScaleW = 1;
    plan.rootHwScaleH = 1;

    SkiaImageDrawPlanData fallbackPlan = plan;
    fallbackPlan.rootHandle = fallbackSourceHandle;
    if (skewDestination) {
        if (!skia_image_backing_skew_surface_for_test(destinationSurface, 0.25f, 0.0f)
            || !skia_image_backing_skew_surface_for_test(fallbackSurface, 0.25f, 0.0f)) {
            std::fprintf(stderr, "unable to skew %s fallback surfaces\n", name);
            skia_image_backing_release(sourceHandle);
            skia_image_backing_release(fallbackSourceHandle);
            if (destinationHandle != sourceHandle) skia_image_backing_release(destinationHandle);
            if (fallbackHandle != fallbackSourceHandle) skia_image_backing_release(fallbackHandle);
            return false;
        }
    }

    const float sourceRight = overlappingStorage ? 1.0f : 2.0f;
    const float destinationLeft = overlappingStorage ? 1.0f : (fractionalDestination ? 1.5f : 1.0f);
    const float destinationTop = overlappingStorage ? 0.0f : 1.0f;
    const float destinationRight = destinationLeft + sourceRight;
    const float destinationBottom = destinationTop + 2.0f;
    const int status = skia_image_backing_draw_geometry_to_surface(destinationSurface, &plan,
        0, 0, sourceRight, 2, destinationLeft, destinationTop, destinationRight,
        destinationBottom, true, false, false);
    const int fallbackStatus = skia_image_backing_draw_geometry_to_surface(fallbackSurface,
        &fallbackPlan, 0, 0, sourceRight, 2, destinationLeft, destinationTop,
        destinationRight, destinationBottom, false, false, false);
    bool pixelsMatch = (fallbackStatus & SKIA_IMAGE_DRAW_HANDLED) != 0;
    for (int y = 0; y < height && pixelsMatch; ++y) {
        for (int x = 0; x < width; ++x) {
            if (skia_getPixel(destinationSurface, x, y) != skia_getPixel(fallbackSurface, x, y)) {
                pixelsMatch = false;
                break;
            }
        }
    }
    const bool rejectedDirectCopy = (status & SKIA_IMAGE_DRAW_PHYSICAL_COPY_ATTEMPT)
        && (status & SKIA_IMAGE_DRAW_PHYSICAL_COPY_FALLBACK)
        && !(status & SKIA_IMAGE_DRAW_PHYSICAL_COPY_HIT)
        && (status & SKIA_IMAGE_DRAW_HANDLED);
    const bool usedExistingFallback = (status & (SKIA_IMAGE_DRAW_GENERIC_GEOMETRY
        | SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_HIT)) != 0;

    skia_image_backing_release(sourceHandle);
    skia_image_backing_release(fallbackSourceHandle);
    if (destinationHandle != sourceHandle) skia_image_backing_release(destinationHandle);
    if (fallbackHandle != fallbackSourceHandle) skia_image_backing_release(fallbackHandle);
    if (!rejectedDirectCopy || !usedExistingFallback || !pixelsMatch) {
        std::fprintf(stderr, "%s direct-copy fallback mismatch: status=%#x fallback=%#x parity=%d\n",
                     name, status, fallbackStatus, pixelsMatch);
        return false;
    }
    return true;
}

static bool testPhysicalIdentityCopy() {
    if (!testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, true, false)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, true, false,
            false, true, false, 2)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_SMOOTH_SCALE, 255, false, false)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_SMOOTH_SCALE, 255, false, false, true)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_SMOOTH_SCALE, 255, true, false,
            false, true, false, 0, 2.0)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, false, true,
            false, true, false, 0, 1.0, true)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, true, false,
            false, true, false, 0, 1.0, false, true)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, false, false,
            false, true, false, 0, 1.0, true, true)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 128, false, true)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, false, false, false, false)
        || !testPhysicalIdentityCopyCase(SKIA_IMAGE_DRAW_CROP, 255, false, true, false, true, true)
        || !testPhysicalCopyFallbackCase("fractional destination", 0, true, -1, false, false)
        || !testPhysicalCopyFallbackCase("rotated geometry", 0, false, 45, false, false)
        || !testPhysicalCopyFallbackCase("skewed canvas", 0, false, -1, true, false)
        || !testPhysicalCopyFallbackCase("format mismatch", 2, false, -1, false, false)
        || !testPhysicalCopyFallbackCase("overlapping storage", 0, false, -1, false, true)) {
        return false;
    }
    std::puts("skia physical identity copy assertions passed");
    return true;
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

static bool testOpaqueWritePixelsCase(const char* name, int sourceColorType,
                                      int sourceOpacityState, int alphaMask,
                                      float srcLeft, float srcTop, float srcRight, float srcBottom,
                                      float dstLeft, float dstTop, double destinationScale,
                                      float skewX, bool clipped, bool failWrite,
                                      bool expectWriteHit, uint8_t sourceAlpha = 0xFF) {
    uint8_t sourceRgba[] = {
        0x10, 0x20, 0x30, sourceAlpha, 0x40, 0x50, 0x60, 0xFF, 0x70, 0x80, 0x90, 0xFF,
        0xA0, 0xB0, 0xC0, 0xFF, 0xD0, 0xE0, 0xF0, 0xFF, 0x20, 0x40, 0x60, 0xFF
    };
    const int64_t source = skia_image_backing_create_empty_with_color_type_for_test(
        3, 2, sourceColorType);
    const int64_t destination = skia_image_backing_create_empty(8, 8);
    const int64_t fallback = skia_image_backing_create_empty(8, 8);
    if (!source || !destination || !fallback
        || !skia_image_backing_write_rgba_pixels(source, sourceRgba, 0, 0, 3, 2, 12)) {
        std::fprintf(stderr, "unable to create %s opaque-write surfaces\n", name);
        if (source) skia_image_backing_release(source);
        if (destination) skia_image_backing_release(destination);
        if (fallback) skia_image_backing_release(fallback);
        return false;
    }
    const int32 destinationSurface = skia_image_backing_surface_id(destination);
    const int32 fallbackSurface = skia_image_backing_surface_id(fallback);
    if (destinationSurface == SKIA_INVALID_SURFACE_ID || fallbackSurface == SKIA_INVALID_SURFACE_ID) {
        std::fprintf(stderr, "unable to create %s opaque-write surface aliases\n", name);
        skia_image_backing_release(source);
        skia_image_backing_release(destination);
        skia_image_backing_release(fallback);
        return false;
    }
    if (destinationScale != 1.0) {
        skia_setSurfaceScale(destinationSurface, destinationScale);
        skia_setSurfaceScale(fallbackSurface, destinationScale);
    }
    if (skewX != 0) {
        if (!skia_image_backing_skew_surface_for_test(destinationSurface, skewX, 0)
            || !skia_image_backing_skew_surface_for_test(fallbackSurface, skewX, 0)) {
            std::fprintf(stderr, "unable to apply %s opaque-write matrix\n", name);
            skia_image_backing_release(source);
            skia_image_backing_release(destination);
            skia_image_backing_release(fallback);
            return false;
        }
    }
    if (clipped) {
        skia_setClip(destinationSurface, 2, 1, 4, 3);
        skia_setClip(fallbackSurface, 2, 1, 4, 3);
    }
    if (failWrite) {
        skia_image_backing_fail_next_opaque_write_pixels_for_test();
    }
    const float dstRight = dstLeft + (srcRight - srcLeft);
    const float dstBottom = dstTop + (srcBottom - srcTop);
    const int status = skia_image_backing_draw_to_surface(destinationSurface, source,
        srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom,
        alphaMask, true, sourceOpacityState);
    const int fallbackStatus = skia_image_backing_draw_to_surface(fallbackSurface, source,
        srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom,
        alphaMask, false, sourceOpacityState);
    if (clipped) {
        skia_restoreClip(destinationSurface);
        skia_restoreClip(fallbackSurface);
    }
    bool pixelsMatch = (fallbackStatus & SKIA_IMAGE_DRAW_HANDLED) != 0;
    for (int y = 0; y < 8 && pixelsMatch; ++y) {
        for (int x = 0; x < 8; ++x) {
            if (skia_getPixel(destinationSurface, x, y) != skia_getPixel(fallbackSurface, x, y)) {
                std::fprintf(stderr, "%s opaque-write parity mismatch at %d,%d\n", name, x, y);
                pixelsMatch = false;
                break;
            }
        }
    }
    const bool attempted = (status & SKIA_IMAGE_DRAW_OPAQUE_WRITE_ATTEMPT) != 0;
    const bool writeHit = (status & SKIA_IMAGE_DRAW_OPAQUE_WRITE_HIT) != 0;
    const bool handled = (status & SKIA_IMAGE_DRAW_HANDLED) != 0;
    skia_image_backing_release(source);
    skia_image_backing_release(destination);
    skia_image_backing_release(fallback);
    if (!pixelsMatch || !attempted || writeHit != expectWriteHit || !handled) {
        std::fprintf(stderr, "%s opaque-write status mismatch status=%#x fallback=%#x hit=%d\n",
            name, status, fallbackStatus, writeHit);
        return false;
    }
    return true;
}

static bool testOpaqueWritePixelsPlanHit() {
    const uint8_t sourceRgba[] = {
        0x10, 0x20, 0x30, 0xFF, 0x40, 0x50, 0x60, 0xFF, 0x70, 0x80, 0x90, 0xFF,
        0xA0, 0xB0, 0xC0, 0xFF, 0xD0, 0xE0, 0xF0, 0xFF, 0x20, 0x40, 0x60, 0xFF
    };
    const int64_t source = skia_image_backing_create_empty(3, 2);
    const int64_t destination = skia_image_backing_create_empty(6, 4);
    const int64_t fallback = skia_image_backing_create_empty(6, 4);
    if (!source || !destination || !fallback
        || !skia_image_backing_write_rgba_pixels(source, sourceRgba, 0, 0, 3, 2, 12)) {
        if (source) skia_image_backing_release(source);
        if (destination) skia_image_backing_release(destination);
        if (fallback) skia_image_backing_release(fallback);
        return false;
    }
    const int32 destinationSurface = skia_image_backing_surface_id(destination);
    const int32 fallbackSurface = skia_image_backing_surface_id(fallback);
    int32 operation[] = {SKIA_IMAGE_DRAW_CROP};
    int32 parameters[] = {0, 0, 0, 0};
    int32 dimensions[] = {3, 2};
    SkiaImageDrawPlanData plan = {};
    plan.rootHandle = source;
    plan.rootWidth = plan.rootWidthOfAllFrames = plan.rootLogicalWidth = 3;
    plan.rootHeight = plan.rootLogicalHeight = 2;
    plan.rootFrameCount = 1;
    plan.rootContentScale = plan.rootHwScaleW = plan.rootHwScaleH = 1;
    plan.operations = operation;
    plan.parameters = parameters;
    plan.dimensions = dimensions;
    plan.operationCount = 1;
    plan.outputWidth = plan.outputWidthOfAllFrames = 3;
    plan.outputHeight = 2;
    plan.outputFrameCount = 1;
    plan.alphaMask = plan.outputAlphaMask = plan.materializeAlphaMask = 255;
    plan.sourceBackingStable = 1;
    plan.sourceOpacityState = 0;
    plan.physicalIdentityEnabled = 1;
    const int status = skia_image_backing_draw_geometry_to_surface(destinationSurface, &plan,
        0, 0, 3, 2, 1, 1, 4, 3, false, false, true);
    const int fallbackStatus = skia_image_backing_draw_geometry_to_surface(fallbackSurface, &plan,
        0, 0, 3, 2, 1, 1, 4, 3, false, false, false);
    bool pixelsMatch = (fallbackStatus & SKIA_IMAGE_DRAW_HANDLED) != 0;
    for (int y = 0; y < 4 && pixelsMatch; ++y) {
        for (int x = 0; x < 6; ++x) {
            if (skia_getPixel(destinationSurface, x, y) != skia_getPixel(fallbackSurface, x, y)) {
                pixelsMatch = false;
                break;
            }
        }
    }
    skia_image_backing_release(source);
    skia_image_backing_release(destination);
    skia_image_backing_release(fallback);
    const int expectedStatus = SKIA_IMAGE_DRAW_HANDLED | SKIA_IMAGE_DRAW_OPAQUE_WRITE_ATTEMPT
        | SKIA_IMAGE_DRAW_OPAQUE_WRITE_HIT | SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_ATTEMPT
        | SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_HIT;
    const int statusMask = SKIA_IMAGE_DRAW_HANDLED | SKIA_IMAGE_DRAW_OPAQUE_WRITE_ATTEMPT
        | SKIA_IMAGE_DRAW_OPAQUE_WRITE_HIT | SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_ATTEMPT
        | SKIA_IMAGE_DRAW_PHYSICAL_IDENTITY_HIT;
    if (!pixelsMatch || (status & statusMask) != expectedStatus) {
        std::fprintf(stderr, "deferred-plan opaque-write hit mismatch status=%#x\n", status);
        return false;
    }
    return true;
}

int main(int argc, char** argv) {
    if (argc > 1) {
        if (!testTypefaceRegistry(argv[1]) || !testBoldStyle(argv[1])) {
            return 1;
        }
    }
    if (!testPhysicalIdentityCopy()) {
        return 1;
    }
    if (!testOpaqueWritePixelsCase("opaque 1:1", 0, 0, 255, 0, 0, 3, 2,
            1, 1, 1.0, 0, false, false, true)
        || !testOpaqueWritePixelsCase("clipped 1:1", 0, 1, 255, 0, 0, 3, 2,
            1, 1, 1.0, 0, true, false, true)
        || !testOpaqueWritePixelsCase("translucent source", 0, 2, 255, 0, 0, 3, 2,
            1, 1, 1.0, 0, false, false, false, 0x80)
        || !testOpaqueWritePixelsCase("alpha mask", 0, 1, 128, 0, 0, 3, 2,
            1, 1, 1.0, 0, false, false, false)
        || !testOpaqueWritePixelsCase("scaled draw", 0, 1, 255, 0, 0, 3, 2,
            1, 1, 2.0, 0, false, false, false)
        || !testOpaqueWritePixelsCase("fractional destination", 0, 1, 255, 0, 0, 3, 2,
            1.5f, 1, 1.0, 0, false, false, false)
        || !testOpaqueWritePixelsCase("skewed matrix", 0, 1, 255, 0, 0, 3, 2,
            1, 1, 1.0, 0.125f, false, false, false)
        || !testOpaqueWritePixelsCase("invalid source bounds", 0, 1, 255, -1, 0, 2, 2,
            1, 1, 1.0, 0, false, false, false)
        || !testOpaqueWritePixelsCase("unsupported source format", 2, 1, 255, 0, 0, 3, 2,
            1, 1, 1.0, 0, false, false, false)
        || !testOpaqueWritePixelsCase("target write failure", 0, 1, 255, 0, 0, 3, 2,
            1, 1, 1.0, 0, false, true, false)
        || !testOpaqueWritePixelsPlanHit()) {
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
    skia_drawSurface(destination, source, 0, 0, 2, 2, 1, 1, 3, 3, 255, false, nullptr);
    if (!expectEqual(skia_getPixel(destination, 1, 1), expected, "identity copy")) {
        return 1;
    }

    skia_setSurfaceScale(destination, 2);
    skia_drawSurface(destination, source, 0, 0, 2, 2, 0, 0, 2, 2, 255, false, nullptr);
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
    skia_drawSurface(clippedDestination, source, 0, 0, 2, 2, 1, 1, 3, 3, 255, false, nullptr);
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

    if (!skia_image_backing_compact_storage_available()) {
        std::fputs("Skia did not provide the required compact image color types\n", stderr);
        return 1;
    }
    const int expectedBytesPerPixel[] = { 4, 2, 1, 2 };
    for (int format = 0; format < 4; ++format) {
        const int64_t compactBacking = skia_image_backing_create_empty_with_format(3, 2, format);
        if (!compactBacking || skia_image_backing_format(compactBacking) != format) {
            std::fputs("compact backing format was not retained\n", stderr);
            return 1;
        }
        const int32 compactRowBytes = skia_image_backing_row_bytes(compactBacking);
        const int64_t compactBytes = skia_image_backing_byte_count(compactBacking);
        if (compactRowBytes < 3 * expectedBytesPerPixel[format]
                || compactBytes != static_cast<int64_t>(compactRowBytes) * 2) {
            std::fputs("compact backing did not report its actual row layout\n", stderr);
            return 1;
        }
        uint8_t rgbaRows[24] = {
            0x10, 0x20, 0x30, 0xFF, 0x40, 0x50, 0x60, 0xFF, 0x70, 0x80, 0x90, 0xFF,
            0xA0, 0xB0, 0xC0, 0xFF, 0xD0, 0xE0, 0xF0, 0xFF, 0x20, 0x40, 0x60, 0x80
        };
        const uint8_t grayRows[6] = { 7, 129, 255, 0, 64, 200 };
        const bool wrote = format == 2
            ? skia_image_backing_write_gray_pixels(compactBacking, grayRows, 0, 0, 3, 2, 3)
            : skia_image_backing_write_rgba_pixels(compactBacking, rgbaRows, 0, 0, 3, 2, 12);
        if (!wrote
                || !skia_image_backing_finish_decode(compactBacking)
                || !skia_image_backing_read_row(compactBacking, snapshotPixels, 0, 2)) {
            std::fputs("compact backing write, freeze, or expanded readback failed\n", stderr);
            return 1;
        }
        if (format == 2 && (!expectEqual(snapshotPixels[0], 0xFF070707, "GRAY8 expanded value")
                || !expectEqual(snapshotPixels[1], 0xFF818181, "GRAY8 expanded row value"))) {
            return 1;
        }
        skia_image_backing_release(compactBacking);
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
