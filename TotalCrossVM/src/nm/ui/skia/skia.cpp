// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_internal.h"

#define USE_WRITE_PIXELS 1

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

#if __APPLE__
#ifdef darwin
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#else
#include <OpenGL/gl.h>
#include <OpenGL/glu.h>
#endif
#else
#if !defined(__arm__) && !defined(ANDROID)
#include <GL/gl.h>
#else
#include <EGL/egl.h>
// #include <GLES/gl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#if defined ANDROID
#include <jni.h>
#include <android/bitmap.h>
#endif
#endif
#endif
#if defined HEADLESS
#include "../../../init/tcsdl.h"
#endif
#include <math.h>


#include "include/core/SkPathEffect.h"
#include "include/core/SkGraphics.h"
#include "include/core/SkSurface.h"
#include "include/core/SkString.h"
#include "include/core/SkTime.h"
#include "include/core/SkCanvas.h"
#include "include/utils/SkRandom.h"
#include "include/core/SkTypeface.h"
#include "include/core/SkImage.h"
#include "include/core/SkImageInfo.h"
#include "include/core/SkImageEncoder.h"
#include "include/core/SkPath.h"
#include "include/effects/SkGradientShader.h"
#include "include/core/SkTextBlob.h"

#include "include/gpu/gl/GrGLAssembleInterface.h"
#include "include/gpu/gl/GrGLConfig.h"
#include "include/gpu/gl/GrGLExtensions.h"
#include "include/gpu/gl/GrGLFunctions.h"
#include "include/gpu/gl/GrGLInterface.h"
#include "include/gpu/gl/GrGLTypes.h"

#include "include/gpu/GrBackendSurface.h"
#include "include/gpu/GrDirectContext.h"
#include "include/gpu/GrTypes.h"

#include "include/core/SkColorSpace.h"
#include "include/effects/SkDashPathEffect.h"

#include <vector>
#include <map>


extern "C" {
#if !defined APPLE && !defined ANDROID && !defined darwin && defined linux && defined __arm__ && !defined __aarch64__
// Avoid dependency on glibc 2.27
// These functions are used by Skia .a file, so we have to define a wrapper.
// https://stackoverflow.com/questions/8823267/linking-against-older-symbol-version-in-a-so-file
__asm__(".symver log2f,log2f@GLIBC_2.4");
float __wrap_log2f(float x) {
  return log2f(x);
}

__asm__(".symver powf,powf@GLIBC_2.4");
float __wrap_powf(float x, float y) {
  return powf(x, y);
}

__asm__(".symver expf,expf@GLIBC_2.4");
float __wrap_expf(float x) {
  return expf(x);
}

__asm__(".symver exp2f,exp2f@GLIBC_2.4");
float __wrap_exp2f(float x) {
  return exp2f(x);
}
#endif
}


#define SKIA_DEBUG
// #define SKIA_TRACE

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
            return  __builtin_bswap32(val);
        #elif defined(__GNUG__)
            return  __builtin_bswap32(val);
        #elif defined(_MSC_VER)
             return = _byteswap_ulong(val);
        #endif
}
#define SWAP32(n) builtinSwap32(n)
#else
#define SWAP32(n) (((n >> 24) & 0xFF)) | ((((n >> 16) & 0xFF) << 8) | (((n >> 8) & 0xFF) << 16) | ((n & 0xFF) << 24))
#endif

sk_sp<SkSurface> surface;
SkCanvas *canvas;
SkPaint forePaint; // used for contours
SkPaint backPaint; // used for fills
SkPaint alphaPaint; // used for alphaMask
SkBitmap bitmap;
SkFont   skFont;

#define TYPEFACE_LEN 32
sk_sp<SkTypeface> typefaces[TYPEFACE_LEN];
int typefaceIdx = 0;

std::vector<SkBitmap> textures;

std::map<std::string, int> typefaceIndexMap;

void initSkia(int w, int h, void * pixels, int pitch, uint32_t pixelformat)
{
    SKIA_TRACE()
#ifdef HEADLESS
    bitmap.installPixels(SkImageInfo::Make(w,
                                           h,
                                           (SkColorType) colorType(pixelformat), kPremul_SkAlphaType), (Uint32 *)pixels, pitch);
    canvas = new SkCanvas(bitmap);
#else
    // To use Skia's GPU backend, a OpenGL context is needed. Skia uses the "Gr" library to abstract
    // the different OpenGL variants (Core, ES, etc). Most of the code bellow is dedicated to create
    // a GL context and produce a valid rendertarget out of it for rendering.
    auto interface = GrGLMakeNativeInterface();
    sk_sp<GrDirectContext> grContext(GrDirectContext::MakeGL(interface));

    GLint defaultFBO;
    glGetIntegerv(GL_FRAMEBUFFER_BINDING, &defaultFBO);
    GrGLFramebufferInfo framebuffer_info;
    framebuffer_info.fFBOID = defaultFBO;
    framebuffer_info.fFormat = GL_RGB8_OES;

    GrBackendRenderTarget render_target(
        w, h, 0, 8,
        framebuffer_info);

    SkSurfaceProps surface_props =
        SkSurfaceProps();

    sk_sp<SkSurface> gpuSurface = SkSurface::MakeFromBackendRenderTarget(
        grContext.get(), render_target, kBottomLeft_GrSurfaceOrigin,
        kRGB_888x_SkColorType, nullptr, &surface_props);

    SkCanvas *gpuCanvas = gpuSurface->getCanvas();

    // We cache a reference for the surface and canvas for later use.
    surface = gpuSurface;
    canvas = gpuCanvas;
#endif
    skFont.setSize(16);
    // The forepaint is used for "draw" methods
    forePaint.setStyle(SkPaint::kStroke_Style);
    forePaint.setAntiAlias(true);
    forePaint.setAntiAlias(true);

    //the backpaint is used for "fill" methods
    backPaint.setStyle(SkPaint::kFill_Style);
    backPaint.setAntiAlias(true);

    backPaint.setAntiAlias(true);
    canvas->clear(SK_ColorWHITE);
    flushSkia();
}

void flushSkia()
{
    if (surface) {
        surface->flushAndSubmit();
    } else if (canvas) {
        canvas->flush();
    }
#ifdef HEADLESS
    TCSDL_UpdateTexture(bitmap.width(), bitmap.height(), bitmap.rowBytes(),bitmap.getPixels());
#endif
}

// Creates a SkTypeface object out of a in-memory TTF file (probably inside some TCZ archive)
int32 skia_makeTypeface(char* name, void *data, int32 size)
{
    SKIA_TRACE()
    std::string key = name;
    int32 idx = skia_getTypefaceIndex(name);
    
    if (idx == -1 && typefaceIdx < TYPEFACE_LEN - 1) {
        sk_sp<SkData> typefaceData = SkData::MakeWithCopy(data, size);
        sk_sp<SkTypeface> typeface = SkTypeface::MakeFromData(typefaceData);
        idx = typefaceIdx;
        typefaces[typefaceIdx++] = typeface;
        typefaceIndexMap[key] = idx;
    }
    
    return idx;
}

int32 skia_getTypefaceIndex(char* name) {
    std::string key = name;
    std::map<std::string, int>::iterator it = typefaceIndexMap.find(key);

    if (it != typefaceIndexMap.end()) {
    	return it->second;
    }
    
    return -1;
}

sk_sp<SkTypeface> skia_getTypeface(int32 typefaceIndex) {
    if (typefaceIndex >= 0 && typefaceIndex < typefaceIdx) {
        return typefaces[typefaceIndex];
    } else {
        return SkTypeface::MakeFromName(nullptr, SkFontStyle());
    }
}

int32 skia_stringWidth(const void *text, int32 charCount, int32 typefaceIndex, int32 fontSize)
{
    const auto newTypeFace = skia_getTypeface(typefaceIndex);

    if(skFont.getTypeface() != newTypeFace.get()) {
        skFont.setTypeface(newTypeFace);
    }
    if(skFont.getSize() != fontSize) {
        skFont.setSize(fontSize);
    }
    return skFont.measureText(text,charCount,SkTextEncoding::kUTF16);
}



void skia_shiftScreen(float w, float h, float glShiftY) {
    canvas->restoreToCount(1);

    // resets the matrix before translating
    canvas->setMatrix(SkMatrix::I());

    // equivalent to:
    // 1.0f - (2.0f * glShiftY / h)
    canvas->translate(0, glShiftY);

    flushSkia();
}

#ifdef ANDROID
/**
 * Draws something into the given bitmap
 * @param  env
 * @param  thiz
 * @param  dstBitmap   The bitmap to place the results of skia into
 * @param  elapsedTime The number of milliseconds since the app was started
 */
extern "C" JNIEXPORT void JNICALL Java_totalcross_Launcher4A_drawIntoBitmap(JNIEnv *env,
                                                                            jobject thiz, jobject dstBitmap, jlong elapsedTime)
{
    // Grab the dst bitmap info and pixels
    AndroidBitmapInfo dstInfo;
    void *dstPixels;
    AndroidBitmap_getInfo(env, dstBitmap, &dstInfo);
    AndroidBitmap_lockPixels(env, dstBitmap, &dstPixels);

    SkImageInfo info = SkImageInfo::MakeN32Premul(dstInfo.width, dstInfo.height);

    // write from canvas to the screen bitmap
    if (canvas != NULL)
    {
        bool ret = canvas->readPixels(info, dstPixels, dstInfo.stride, 0, 0);
    }
    // Unlock the dst's pixels
    AndroidBitmap_unlockPixels(env, dstBitmap);
}
#endif

#ifdef HEADLESS
int32 colorType(uint32 pixelformat) {
    if (SDL_PIXELTYPE(pixelformat) == SDL_PIXELTYPE_PACKED16) {
        if (SDL_PIXELORDER(pixelformat) == SDL_PACKEDORDER_XRGB) {
            if (SDL_PIXELLAYOUT(pixelformat) == SDL_PACKEDLAYOUT_565) {
                return kRGB_565_SkColorType;
            }
        }
    }
    else if (SDL_PIXELTYPE(pixelformat) == SDL_PIXELTYPE_PACKED32) {
        if (SDL_PIXELLAYOUT(pixelformat) == SDL_PACKEDLAYOUT_8888) {
            if (SDL_PIXELORDER(pixelformat) == SDL_PACKEDORDER_XRGB ||
                SDL_PIXELORDER(pixelformat) == SDL_PACKEDORDER_ARGB) {
                return kBGRA_8888_SkColorType;
            }
        }
    }
    debug("Unsupported pixel format %s, try mapping your color format on %s - %s", SDL_GetPixelFormatName(pixelformat), __FILE__, __FUNCTION__);
    return kUnknown_SkColorType;
}
#endif
