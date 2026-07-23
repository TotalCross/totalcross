// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_internal.h"

static void releaseProc(void* addr, void*) {
    delete[] static_cast<int32*>(addr);
}

SkCanvas* skiaGetCanvas(int32 surfaceId) {
    if (surfaceId == SKIA_SCREEN_SURFACE_ID) {
        return canvas;
    }
    if (surfaceId < 0 || static_cast<size_t>(surfaceId) >= imageSurfaces.size()) {
        return nullptr;
    }
    const auto& imageSurface = imageSurfaces[static_cast<size_t>(surfaceId)];
    return imageSurface && imageSurface->canvas ? imageSurface->canvas.get() : nullptr;
}

SkBitmap* skiaGetBitmap(int32 surfaceId) {
    if (surfaceId < 0 || static_cast<size_t>(surfaceId) >= imageSurfaces.size()) {
        return nullptr;
    }
    const auto& imageSurface = imageSurfaces[static_cast<size_t>(surfaceId)];
    return imageSurface ? &imageSurface->bitmap : nullptr;
}

int skia_makeBitmap(int32 id, void *data, int32 w, int32 h) {
    SKIA_TRACE()
    if (!data || w <= 0 || h <= 0) {
        return SKIA_INVALID_SURFACE_ID;
    }
    if (id >= 0 && (static_cast<size_t>(id) >= imageSurfaces.size() || !imageSurfaces[id])) {
        return SKIA_INVALID_SURFACE_ID;
    }

    const size_t count = static_cast<size_t>(w) * static_cast<size_t>(h);
    int32* const converted = new int32[count];
    const Pixel* const src = reinterpret_cast<const Pixel*>(data);
    for (size_t i = 0; i < count; i++) {
        converted[i] = SWAP32(src[i]);
    }

    auto imageSurface = std::make_unique<SkiaImageSurface>();
    imageSurface->bitmap.installPixels(
        SkImageInfo::Make(w, h, kN32_SkColorType, kUnpremul_SkAlphaType),
        converted,
        sizeof(Pixel) * static_cast<size_t>(w),
        releaseProc,
        nullptr);

    // TODO: Reevaluate this optimization; it was added for embedded Linux ARM,
    // but forced scale draws broke on Android/iOS.
#if USE_COMPUTE_OPAQUE
    if (SkBitmap::ComputeIsOpaque(imageSurface->bitmap)) {
        imageSurface->bitmap.setAlphaType(kOpaque_SkAlphaType);
    }
#endif

    // TODO: Recheck the color type before re-enabling on Android/iOS; macOS did
    // not reproduce the forced scale issue.
#if USE_COLORTYPE_CONVERSION
    if (canvas && imageSurface->bitmap.isOpaque()) {
        const SkImageInfo screenInfo = canvas->imageInfo();
        void* dstPixels = operator new(static_cast<size_t>(w) * static_cast<size_t>(h) * screenInfo.bytesPerPixel());
        const SkImageInfo dstImageInfo = SkImageInfo::Make(
            w, h, screenInfo.colorType(), screenInfo.alphaType());
        imageSurface->bitmap.readPixels(dstImageInfo, dstPixels, dstImageInfo.minRowBytes(), 0, 0);
        imageSurface->bitmap.installPixels(
            dstImageInfo, dstPixels, dstImageInfo.minRowBytes(), releaseProc, nullptr);
    }
#endif

    imageSurface->canvas = std::make_unique<SkCanvas>(imageSurface->bitmap);
    if (id < 0) {
        const int newId = static_cast<int>(imageSurfaces.size());
        imageSurfaces.emplace_back(std::move(imageSurface));
        return newId;
    }

    imageSurfaces[static_cast<size_t>(id)] = std::move(imageSurface);
    return id;
}

void skia_deleteBitmap(int32 id) {
    SKIA_TRACE()
    if (id >= 0 && static_cast<size_t>(id) < imageSurfaces.size()) {
        imageSurfaces[static_cast<size_t>(id)].reset();
    }
}

void skia_setClip(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2) {
    if (SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface)) {
        targetCanvas->save();
        targetCanvas->clipRect(SkRect::MakeLTRB(x1, y1, x2, y2));
    }
}

void skia_restoreClip(int32 skiaSurface) {
    if (SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface)) {
        targetCanvas->restore();
    }
}

void skia_drawSurface(int32 skiaSurface, int32 id, float srcLeft, float srcTop,
                     float srcRight, float srcBottom, float dstLeft, float dstTop,
                     float dstRight, float dstBottom, int32 alphaMask) {
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    SkBitmap* texture = skiaGetBitmap(id);
    if (!targetCanvas || !texture) {
        return;
    }

    const SkRect srcRect = SkRect::MakeLTRB(srcLeft, srcTop, srcRight, srcBottom);
    const SkRect dstRect = SkRect::MakeLTRB(dstLeft, dstTop, dstRight, dstBottom);
    const bool fullSource = srcLeft == 0.0f && srcTop == 0.0f &&
        srcRight == texture->width() && srcBottom == texture->height();
    const bool sameSize = (srcRight - srcLeft) == (dstRight - dstLeft) &&
        (srcBottom - srcTop) == (dstBottom - dstTop);

#if USE_WRITE_PIXELS
    if (texture->isOpaque() && alphaMask == 255 && fullSource && sameSize) {
        targetCanvas->writePixels(
            texture->info(), texture->getPixels(), texture->rowBytes(),
            static_cast<int>(dstLeft), static_cast<int>(dstTop));
    } else
#endif
    {
        alphaPaint.setAlpha(alphaMask);
        alphaPaint.setFilterQuality(sameSize ? kNone_SkFilterQuality : kLow_SkFilterQuality);
        targetCanvas->drawBitmapRect(
            *texture, srcRect, dstRect, &alphaPaint,
            fullSource ? SkCanvas::kFast_SrcRectConstraint : SkCanvas::kStrict_SrcRectConstraint);
    }
}

Pixel skia_getPixel(int32 skiaSurface, int32 x, int32 y) {
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) {
        return -1;
    }
    SkBitmap pixelBitmap;
    pixelBitmap.allocPixels(SkImageInfo::MakeN32Premul(1, 1));
    if (!targetCanvas->readPixels(pixelBitmap, x, y)) {
        return -1;
    }
    return skiaPixelFromColor(pixelBitmap.getColor(0, 0));
}

int skia_getPixelRow(int32 skiaSurface, void *output, int32 y, int32 width) {
    SKIA_TRACE()
    SkCanvas* sourceCanvas = skiaGetCanvas(skiaSurface);
    const SkImageInfo sourceInfo = sourceCanvas ? sourceCanvas->imageInfo() : SkImageInfo();
    if (!sourceCanvas || !output || y < 0 || width < 0 || y >= sourceInfo.height() ||
        width > sourceInfo.width()) {
        return 0;
    }
    SkBitmap rowBitmap;
    rowBitmap.allocPixels(SkImageInfo::Make(
        width, 1, kRGBA_8888_SkColorType, kUnpremul_SkAlphaType));
    if (!sourceCanvas->readPixels(rowBitmap, 0, y)) {
        return 0;
    }
    uint8_t* row = static_cast<uint8_t*>(output);
    for (int32 x = 0; x < width; ++x) {
        const uint8_t* rgba = rowBitmap.getAddr8(x, 0);
        const SkColor color = SkColorSetARGB(rgba[3], rgba[0], rgba[1], rgba[2]);
        *row++ = SkColorGetR(color);
        *row++ = SkColorGetG(color);
        *row++ = SkColorGetB(color);
        *row++ = SkColorGetA(color);
    }
    return 1;
}

void skia_setPixel(int32 skiaSurface, int32 x, int32 y, Pixel pixel) {
    SKIA_TRACE()
    if (SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface)) {
        backPaint.setColor(skiaColorFromPixel(pixel));
        targetCanvas->drawRect(SkRect::MakeXYWH(x, y, 1, 1), backPaint);
    }
}

int skia_getsetRGB(int32 skiaSurface, void *pixels, int32 offset, int32 x, int32 y,
                   int32 w, int32 h, bool isGet) {
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) {
        return 0;
    }
    if (!pixels || w <= 0 || h <= 0) {
        return 0;
    }
    SkBitmap pixelBitmap;
    pixelBitmap.allocPixels(SkImageInfo::Make(
        w, h, kRGBA_8888_SkColorType, kUnpremul_SkAlphaType));
    uint8_t* rgba = pixelBitmap.getAddr8(0, 0);
    Pixel* source = static_cast<Pixel*>(pixels) + offset;
    if (!isGet) {
        for (int32 row = 0; row < h; ++row) {
            for (int32 column = 0; column < w; ++column) {
                const Pixel pixel = source[row * w + column];
                const SkColor color = skiaColorFromPixel(pixel);
                *rgba++ = SkColorGetR(color);
                *rgba++ = SkColorGetG(color);
                *rgba++ = SkColorGetB(color);
                *rgba++ = SkColorGetA(color);
            }
        }
    }
    if (isGet) {
        if (!targetCanvas->readPixels(pixelBitmap, x, y)) {
            return 0;
        }
        rgba = pixelBitmap.getAddr8(0, 0);
        for (int32 row = 0; row < h; ++row) {
            for (int32 column = 0; column < w; ++column) {
                const uint8_t red = *rgba++;
                const uint8_t green = *rgba++;
                const uint8_t blue = *rgba++;
                const uint8_t alpha = *rgba++;
                source[row * w + column] = skiaPixelFromColor(
                    SkColorSetARGB(alpha, red, green, blue));
            }
        }
        return 1;
    }
    return targetCanvas->writePixels(pixelBitmap, x, y);
}
