// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_INTERNAL_H
#define SKIA_INTERNAL_H

#include "skia.h"

#include <cstdint>
#include <cstdio>
#include <map>
#include <memory>
#include <string>
#include <vector>

#include "include/core/SkBitmap.h"
#include "include/core/SkCanvas.h"
#include "include/core/SkColorSpace.h"
#include "include/core/SkGraphics.h"
#include "include/core/SkImage.h"
#include "include/core/SkImageEncoder.h"
#include "include/core/SkImageInfo.h"
#include "include/core/SkPath.h"
#include "include/core/SkPathEffect.h"
#include "include/core/SkString.h"
#include "include/core/SkSurface.h"
#include "include/core/SkTextBlob.h"
#include "include/core/SkTypeface.h"
#include "include/effects/SkDashPathEffect.h"
#include "include/effects/SkGradientShader.h"
#include "include/gpu/GrBackendSurface.h"
#include "include/gpu/GrDirectContext.h"
#include "include/gpu/GrTypes.h"
#include "include/gpu/gl/GrGLAssembleInterface.h"
#include "include/gpu/gl/GrGLConfig.h"
#include "include/gpu/gl/GrGLExtensions.h"
#include "include/gpu/gl/GrGLFunctions.h"
#include "include/gpu/gl/GrGLInterface.h"
#include "include/gpu/gl/GrGLTypes.h"
#include "include/utils/SkRandom.h"

static inline SkColor skiaColorFromPixel(Pixel pixel) {
    return SkColorSetARGB((pixel >> 24) & 0xFF, (pixel >> 16) & 0xFF,
                          (pixel >> 8) & 0xFF, pixel & 0xFF);
}

static inline Pixel skiaPixelFromColor(SkColor color) {
    return ((Pixel)SkColorGetA(color) << 24) | ((Pixel)SkColorGetR(color) << 16)
        | ((Pixel)SkColorGetG(color) << 8) | SkColorGetB(color);
}

#ifndef USE_COMPUTE_OPAQUE
#if __APPLE__ || ANDROID
#define USE_COMPUTE_OPAQUE 0
#else
#define USE_COMPUTE_OPAQUE 1
#endif
#endif

#ifndef USE_COLORTYPE_CONVERSION
#if __APPLE__ || ANDROID
#define USE_COLORTYPE_CONVERSION 0
#else
#define USE_COLORTYPE_CONVERSION 1
#endif
#endif

#ifndef USE_NATIVE_SWAP
#if __APPLE__ || ANDROID
#define USE_NATIVE_SWAP 0
#else
#define USE_NATIVE_SWAP 1
#endif
#endif

#define USE_WRITE_PIXELS 1

#define SKIA_DEBUG

#if defined SKIA_DEBUG && defined ANDROID
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "TotalCross", __VA_ARGS__)
#else
#define LOGD(...) printf("SKIA %s, %d\n", __FUNCTION__, __LINE__)
#endif

#if defined SKIA_TRACE && defined ANDROID
#define SKIA_TRACE() LOGD(__FUNCTION__);
#else
#define SKIA_TRACE() //LOGD();
#endif

#if USE_NATIVE_SWAP
inline uint32_t builtinSwap32(uint32_t val) noexcept {
#if defined(__clang__)
    return __builtin_bswap32(val);
#elif defined(__GNUG__)
    return __builtin_bswap32(val);
#elif defined(_MSC_VER)
    return _byteswap_ulong(val);
#endif
}
#define SWAP32(n) builtinSwap32(n)
#else
#define SWAP32(n) (((n >> 24) & 0xFF)) | ((((n >> 16) & 0xFF) << 8) | (((n >> 8) & 0xFF) << 16) | ((n & 0xFF) << 24))
#endif

extern sk_sp<SkSurface> surface;
extern SkCanvas *canvas;
extern SkPaint forePaint;
extern SkPaint backPaint;
extern SkPaint alphaPaint;
extern SkBitmap bitmap;
extern SkFont skFont;

struct SkiaImageSurface {
    SkBitmap bitmap;
    std::unique_ptr<SkCanvas> canvas;
};

extern std::vector<std::unique_ptr<SkiaImageSurface>> imageSurfaces;

SkCanvas* skiaGetCanvas(int32 surfaceId);
SkBitmap* skiaGetBitmap(int32 surfaceId);

#define TYPEFACE_LEN 32
extern sk_sp<SkTypeface> typefaces[TYPEFACE_LEN];
extern int typefaceIdx;
extern std::map<std::string, int> typefaceIndexMap;

sk_sp<SkTypeface> skia_getTypeface(int32 typefaceIndex);

#endif
