// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_internal.h"
#include "../android_skia_egl_lifecycle_diagnostics.h"

#if defined(ANDROID)
#include <jni.h>
#include <android/bitmap.h>
#if defined(TC_ANDROID_EGL_LIFECYCLE_TESTS)
#include <sys/system_properties.h>
static bool androidSkiaLifecycleTestProperty(const char *name)
{
    char value[PROP_VALUE_MAX];
    return __system_property_get(name, value) > 0
        && value[0] == '1' && value[1] == '\0';
}
#endif
#endif

#if TC_GRAPHICS_GLES

#if __APPLE__
#ifdef darwin
#include <OpenGLES/ES2/gl.h>
#include <OpenGLES/ES2/glext.h>
#else
#include <OpenGL/gl.h>
#include <OpenGL/glu.h>
#endif

#elif !defined(__arm__) && !defined(ANDROID)

#include <GL/gl.h>

#else

#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#endif

#endif // TC_GRAPHICS_GLES
#if TC_WINDOWING_SDL
#include "../../../init/tcsdl.h"
#endif
#include <math.h>


#include "include/core/SkPathEffect.h"
#include "include/core/SkGraphics.h"
#include "include/core/SkSurface.h"
#include "include/gpu/GrDirectContext.h"
#include "include/gpu/GrRecordingContext.h"
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

#include "include/core/SkColorSpace.h"
#include "include/effects/SkDashPathEffect.h"

#include <vector>
#include <map>

#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
#include <android/log.h>
#include <mutex>
#include <sys/syscall.h>
#include <unistd.h>

namespace {
struct AndroidEglLifecycleState {
    uint64_t sequence = 0;
    uint32_t generation = 0;
    const void *window = nullptr;
    const void *candidateWindow = nullptr;
    const void *display = nullptr;
    const void *eglSurface = nullptr;
    const void *expectedContext = nullptr;
    const void *skiaContext = nullptr;
    const void *skiaSurface = nullptr;
    const void *canvas = nullptr;
    uint32_t flags = 0;
};

AndroidEglLifecycleState androidEglLifecycleState;
std::mutex androidEglLifecycleMutex;
const void *androidSkiaContextIdentity = nullptr;
const void *androidSkiaSurfaceIdentity = nullptr;
const void *androidSkiaCanvasIdentity = nullptr;

void logAndroidEglLifecycleLocked(const char *event)
{
    const uint64_t sequence = ++androidEglLifecycleState.sequence;
    const long tid = static_cast<long>(syscall(SYS_gettid));
    const EGLDisplay currentDisplay = eglGetCurrentDisplay();
    const EGLSurface currentSurface = eglGetCurrentSurface(EGL_DRAW);
    const EGLContext currentContext = eglGetCurrentContext();

    __android_log_print(ANDROID_LOG_INFO, "TC_EGL_LIFECYCLE",
        "seq=%llu tid=%ld event=%s gen=%u window=%p candidate=%p "
        "display=%p surface=%p expectedContext=%p currentDisplay=%p "
        "currentSurface=%p currentContext=%p skiaContext=%p "
        "skiaSurface=%p canvas=%p flags=0x%x",
        static_cast<unsigned long long>(sequence), tid, event,
        androidEglLifecycleState.generation,
        androidEglLifecycleState.window,
        androidEglLifecycleState.candidateWindow,
        androidEglLifecycleState.display,
        androidEglLifecycleState.eglSurface,
        androidEglLifecycleState.expectedContext,
        currentDisplay, currentSurface, currentContext,
        androidEglLifecycleState.skiaContext,
        androidEglLifecycleState.skiaSurface,
        androidEglLifecycleState.canvas,
        androidEglLifecycleState.flags);
}
}

extern "C" void tcAndroidEglLifecycleLogNative(const char *event,
        uint32_t generation, const void *window, const void *candidateWindow,
        const void *display, const void *eglSurface,
        const void *expectedContext, uint32_t flags)
{
    std::lock_guard<std::mutex> lock(androidEglLifecycleMutex);
    androidEglLifecycleState.generation = generation;
    androidEglLifecycleState.window = window;
    androidEglLifecycleState.candidateWindow = candidateWindow;
    androidEglLifecycleState.display = display;
    androidEglLifecycleState.eglSurface = eglSurface;
    androidEglLifecycleState.expectedContext = expectedContext;
    androidEglLifecycleState.flags = flags;
    logAndroidEglLifecycleLocked(event);
}

extern "C" void tcAndroidEglLifecycleLogSkia(const char *event,
        const void *context, const void *skiaSurface, const void *skiaCanvas,
        uint32_t flags)
{
    std::lock_guard<std::mutex> lock(androidEglLifecycleMutex);
    androidEglLifecycleState.skiaContext = context;
    androidEglLifecycleState.skiaSurface = skiaSurface;
    androidEglLifecycleState.canvas = skiaCanvas;
    androidEglLifecycleState.flags = flags;
    logAndroidEglLifecycleLocked(event);
}
#endif


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

sk_sp<SkSurface> surface;
SkCanvas *canvas;
SkPaint forePaint; // used for contours
SkPaint backPaint; // used for fills
SkPaint alphaPaint; // used for alphaMask
SkBitmap bitmap;
SkFont   skFont;

std::vector<sk_sp<SkTypeface>> typefaces;

std::vector<std::unique_ptr<SkiaImageSurface>> imageSurfaces;

std::map<std::string, int> typefaceIndexMap;

int32_t initSkia(int w, int h, void * pixels, int pitch, uint32_t pixelformat)
{
    SKIA_TRACE()
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    TC_ANDROID_EGL_DIAG_SKIA("skia.init.begin", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity,
            (surface ? 1u : 0u) | (canvas ? 2u : 0u));
#endif
    destroySkiaScreen();
#if TC_GRAPHICS_SOFTWARE
    if (!bitmap.installPixels(SkImageInfo::Make(w,
                                           h,
                                           (SkColorType) colorType(pixelformat), kPremul_SkAlphaType), pixels, pitch))
        return false;
    canvas = new SkCanvas(bitmap);
#elif TC_GRAPHICS_GLES
    // To use Skia's GPU backend, a OpenGL context is needed. Skia uses the "Gr" library to abstract
    // the different OpenGL variants (Core, ES, etc). Most of the code bellow is dedicated to create
    // a GL context and produce a valid rendertarget out of it for rendering.
    auto interface = GrGLMakeNativeInterface();
    sk_sp<GrDirectContext> grContext(GrDirectContext::MakeGL(interface));
    if (!grContext)
    {
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
        androidSkiaContextIdentity = nullptr;
        androidSkiaSurfaceIdentity = nullptr;
        androidSkiaCanvasIdentity = nullptr;
        TC_ANDROID_EGL_DIAG_SKIA("skia.context.create.failed", nullptr, nullptr, nullptr, 0);
#endif
        return false;
    }
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    androidSkiaContextIdentity = grContext.get();
    TC_ANDROID_EGL_DIAG_SKIA("skia.context.created", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity, 0);
#endif

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
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_TESTS)
    if (androidSkiaLifecycleTestProperty("debug.tcvm.failSkiaSurfaceInit"))
        gpuSurface.reset();
#endif
    if (!gpuSurface)
    {
        grContext->releaseResourcesAndAbandonContext();
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
        androidSkiaContextIdentity = nullptr;
        androidSkiaSurfaceIdentity = nullptr;
        androidSkiaCanvasIdentity = nullptr;
        TC_ANDROID_EGL_DIAG_SKIA("skia.surface.create.failed", nullptr, nullptr, nullptr, 0);
#endif
        return false;
    }
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    androidSkiaSurfaceIdentity = gpuSurface.get();
    TC_ANDROID_EGL_DIAG_SKIA("skia.surface.created", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, nullptr, gpuSurface ? 1u : 0u);
#endif

    SkCanvas *gpuCanvas = gpuSurface->getCanvas();
    if (!gpuCanvas)
    {
        grContext->releaseResourcesAndAbandonContext();
        gpuSurface.reset();
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
        androidSkiaContextIdentity = nullptr;
        androidSkiaSurfaceIdentity = nullptr;
        androidSkiaCanvasIdentity = nullptr;
        TC_ANDROID_EGL_DIAG_SKIA("skia.canvas.create.failed", nullptr, nullptr, nullptr, 0);
#endif
        return false;
    }
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    androidSkiaCanvasIdentity = gpuCanvas;
    TC_ANDROID_EGL_DIAG_SKIA("skia.canvas.created", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity, 0);
#endif

    // We cache a reference for the surface and canvas for later use.
    surface = gpuSurface;
    canvas = gpuCanvas;
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    TC_ANDROID_EGL_DIAG_SKIA("skia.surface.committed", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity,
            (surface ? 1u : 0u) | (canvas ? 2u : 0u));
#endif
#else
    #error "Unsupported graphics backend"
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
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    TC_ANDROID_EGL_DIAG_SKIA("skia.init.end", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity, 0);
#endif
    return true;
}

enum SkiaScreenCleanupMode
{
    kReleaseSkiaScreenWrappers,
    kReleaseSkiaScreenGpuResources,
    kAbandonSkiaScreenGpuResources
};

static void cleanupSkiaScreen(enum SkiaScreenCleanupMode mode)
{
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    TC_ANDROID_EGL_DIAG_SKIA("skia.destroy.begin", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity,
            (surface ? 1u : 0u) | (canvas ? 2u : 0u));
#endif
#if TC_GRAPHICS_SOFTWARE
    delete canvas;
#endif
#if TC_GRAPHICS_GLES
    GrDirectContext *directContext = surface
        ? GrAsDirectContext(surface->recordingContext()) : nullptr;
    if (directContext && mode == kReleaseSkiaScreenGpuResources)
        directContext->releaseResourcesAndAbandonContext();
    else if (directContext && mode == kAbandonSkiaScreenGpuResources)
        directContext->abandonContext();
#endif
    canvas = nullptr;
    bitmap.reset();
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    TC_ANDROID_EGL_DIAG_SKIA("skia.surface.reset.begin", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, androidSkiaCanvasIdentity, surface ? 1u : 0u);
#endif
    surface.reset();
#if defined(ANDROID) && defined(TC_ANDROID_EGL_LIFECYCLE_DIAGNOSTICS)
    TC_ANDROID_EGL_DIAG_SKIA("skia.surface.reset.end", androidSkiaContextIdentity,
            androidSkiaSurfaceIdentity, nullptr, 0);
    androidSkiaContextIdentity = nullptr;
    androidSkiaSurfaceIdentity = nullptr;
    androidSkiaCanvasIdentity = nullptr;
    TC_ANDROID_EGL_DIAG_SKIA("skia.destroy.end", nullptr, nullptr, nullptr, 0);
#endif
}

void destroySkiaScreen()
{
    cleanupSkiaScreen(kReleaseSkiaScreenWrappers);
}

void releaseSkiaScreenBeforeEglTeardown()
{
    cleanupSkiaScreen(kReleaseSkiaScreenGpuResources);
}

void abandonSkiaScreenBeforeEglTeardown()
{
    cleanupSkiaScreen(kAbandonSkiaScreenGpuResources);
}

void flushSkia()
{
    if (surface) {
        surface->flushAndSubmit();
    } else if (canvas) {
        canvas->flush();
    }
#if TC_WINDOWING_SDL
    TCSDL_UpdateTexture(bitmap.width(), bitmap.height(), bitmap.rowBytes(),bitmap.getPixels());
#endif
}

// Creates a SkTypeface object out of a in-memory TTF file (probably inside some TCZ archive)
int32 skia_makeTypeface(char* name, void *data, int32 size)
{
    SKIA_TRACE()
    std::string key = name;
    int32 idx = skia_getTypefaceIndex(name);

    if (idx >= 0) {
        return idx;
    }

    sk_sp<SkData> typefaceData = SkData::MakeWithCopy(data, size);
    sk_sp<SkTypeface> typeface = SkTypeface::MakeFromData(typefaceData);
    if (!typeface) {
        return -1;
    }

    idx = (int32)typefaces.size();
    typefaces.push_back(typeface);
    typefaceIndexMap[key] = idx;
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
    if (typefaceIndex >= 0 && typefaceIndex < (int32)typefaces.size()) {
        return typefaces[typefaceIndex];
    } else {
        return SkTypeface::MakeFromName(nullptr, SkFontStyle());
    }
}

static void configureSkFont(int32 typefaceIndex, double fontSize, bool bold)
{
    const auto newTypeFace = skia_getTypeface(typefaceIndex);

    if(skFont.getTypeface() != newTypeFace.get()) {
        skFont.setTypeface(newTypeFace);
    }
    if(skFont.getSize() != fontSize) {
        skFont.setSize(fontSize);
    }
    skFont.setEmbolden(bold);
}

int32 skia_stringWidth(const void *text, int32 charCount, int32 typefaceIndex, double fontSize, int32 bold)
{
    return (int32)ceil(skia_stringWidthD(text, charCount, typefaceIndex, fontSize, bold));
}

double skia_stringWidthD(const void *text, int32 charCount, int32 typefaceIndex, double fontSize, int32 bold)
{
    configureSkFont(typefaceIndex, fontSize, bold != 0);
    return skFont.measureText(text, charCount, SkTextEncoding::kUTF16);
}

void skia_fontMetrics(int32 typefaceIndex, double fontSize, int32 bold, double* ascent, double* descent, double* leading)
{
    SkFont metricsFont(skia_getTypeface(typefaceIndex), fontSize);
    metricsFont.setEmbolden(bold != 0);
    SkFontMetrics metrics;
    metricsFont.getMetrics(&metrics);
    *ascent = -metrics.fAscent;
    *descent = metrics.fDescent;
    *leading = metrics.fLeading;
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

#if TC_TARGET_ANDROID
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

#if TC_WINDOWING_SDL
int32 colorType(uint32 pixelformat) {
    if (pixelformat == SDL_PIXELFORMAT_ARGB8888)
        return kBGRA_8888_SkColorType;
    if (pixelformat == SDL_PIXELFORMAT_RGB565)
        return kRGB_565_SkColorType;
    debug("Unsupported pixel format %s, try mapping your color format on %s - %s", SDL_GetPixelFormatName(pixelformat), __FILE__, __FUNCTION__);
    return kUnknown_SkColorType;
}
#endif
