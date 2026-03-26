// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia.h"

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
#include "include/core/SkRRect.h"
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

#include "../gfx.h"
#include "../../instancefields.h"


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

static void releaseProc(void* addr, void* ) {
    delete[] static_cast<int32*>(addr);
}

int skia_makeBitmap(int32 id, void *data, int32 w, int32 h) {
    SKIA_TRACE()

    const size_t count = (size_t)w * (size_t)h;
    int32* const converted = new int32[count];
    const Pixel* const src = reinterpret_cast<const Pixel*>(data);

    for (size_t i = 0; i < count; i++) {
        converted[i] = SWAP32(src[i]);
    }

    if (id < 0) { // must create a new bitmap
        SkBitmap bitmap;
        bitmap.installPixels(SkImageInfo::Make(w, h, kN32_SkColorType, kUnpremul_SkAlphaType),
                             (void*)converted, sizeof(Pixel) * w, releaseProc, nullptr);

        // TODO: Reevaluate this optimization; it was added for embedded Linux ARM, but forced scale draws broke on Android/iOS.
#if USE_COMPUTE_OPAQUE
        if (SkBitmap::ComputeIsOpaque(bitmap)) {
            bitmap.setAlphaType(kOpaque_SkAlphaType);
        }
#endif

        // TODO: Recheck the color type before re-enabling on Android/iOS; macOS did not reproduce the forced scale issue.
#if USE_COLORTYPE_CONVERSION
        if (bitmap.isOpaque()) {
            void* dstPixels = operator new(w * h * canvas->imageInfo().bytesPerPixel());
            SkImageInfo dstImageInfo = 
                SkImageInfo::Make(
                    w, 
                    h, 
                    canvas->imageInfo().colorType(), 
                    canvas->imageInfo().alphaType()
                );
            bitmap.readPixels(dstImageInfo, dstPixels, dstImageInfo.minRowBytes(), 0, 0);
            bitmap.installPixels(dstImageInfo, dstPixels, dstImageInfo.minRowBytes(), releaseProc, nullptr);
        }
#endif
        id = textures.size();
        textures.emplace_back(std::move(bitmap));
    } else {
        SkBitmap& bitmap = textures[id];
        bitmap.installPixels(SkImageInfo::Make(w, h, kN32_SkColorType, kUnpremul_SkAlphaType), (void*)converted, sizeof(Pixel) * w, releaseProc, nullptr);
    }

    return id;
}
void skia_deleteBitmap(int32 id) {
    SKIA_TRACE()

    if (id >= 0) {
        textures[id].reset();
    }
}



void skia_setClip(int32 x1, int32 y1, int32 x2, int32 y2)
{
    canvas->save();
    canvas->clipRect(SkRect::MakeLTRB(x1, y1, x2, y2));
}

void skia_applyClip(TCObject g)
{
    TCObject roundClip = Graphics_roundClip(g);

    canvas->save();
    if (roundClip != null)
    {
        const double *radii = RRect_radii(roundClip);
        SkVector corners[4];
        SkRRect rrect;
        int i;

        for (i = 0; i < 4; i++)
        {
            corners[i].set((SkScalar)radii[i * 2], (SkScalar)radii[i * 2 + 1]);
        }

        rrect.setRectRadii(
            SkRect::MakeXYWH(
                (SkScalar)Rect_x(roundClip),
                (SkScalar)Rect_y(roundClip),
                (SkScalar)Rect_width(roundClip),
                (SkScalar)Rect_height(roundClip)),
            corners);
        canvas->clipRRect(rrect, true);
    }
    else
    {
        canvas->clipRect(SkRect::MakeLTRB(
            Graphics_clipX1(g),
            Graphics_clipY1(g),
            Graphics_clipX2(g),
            Graphics_clipY2(g)));
    }
}

void skia_restoreClip()
{
    canvas->restore();
}

void skia_drawSurface(int32 skiaSurface, int32 id, float srcLeft, float srcTop, float srcRight, float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask)
{
    SKIA_TRACE()

	const auto& texture {textures[id]};
    const SkRect srcRect = SkRect::MakeLTRB(srcLeft, srcTop, srcRight, srcBottom);
    const SkRect dstRect = SkRect::MakeLTRB(dstLeft, dstTop, dstRight, dstBottom);
    const bool fullSource = srcLeft == 0.0f && srcTop == 0.0f && srcRight == texture.width() && srcBottom == texture.height();
    const bool sameSize = (srcRight - srcLeft) == (dstRight - dstLeft) && (srcBottom - srcTop) == (dstBottom - dstTop);

#if USE_WRITE_PIXELS
    /*
        Fast drawing, can only be used to draw fully opaque images
        without sampling (src and dst dimensions are the same).
        Makes drawing JPEG and opaque PNGs over 10x faster.

        TODO:
            - add actual numbers to back this statement
    */
if (texture.isOpaque() && alphaMask == 255 && fullSource && sameSize) {
    canvas->writePixels(
        texture.info(),
        texture.getPixels(),
        texture.rowBytes(),
        (int)dstLeft,
        (int)dstTop
    );
} else
#endif
    {
        alphaPaint.setAlpha(alphaMask);
        alphaPaint.setFilterQuality(sameSize ? kNone_SkFilterQuality : kLow_SkFilterQuality);
        canvas->drawBitmapRect(
            texture,
            srcRect,
            dstRect,
            &alphaPaint,
            fullSource ? SkCanvas::kFast_SrcRectConstraint : SkCanvas::kStrict_SrcRectConstraint
        );
    }
}

// The getPixel call demands a 1-pixel readback from the GPU. Avoid it if possible.
Pixel skia_getPixel(int32 skiaSurface, int32 x, int32 y)
{
    SKIA_TRACE()

    SkBitmap bitmap;
    bitmap.allocPixels(SkImageInfo::MakeN32Premul(1, 1));
    if (!canvas->readPixels(bitmap, x, y))
    {
        return -1;
    }

    Pixel pixel = bitmap.getAddr32(0, 0)[0];
    return (((pixel >> 24) & 0xFF) << 24) | (((pixel & 0xFF) << 16) | (((pixel >> 8) & 0xFF) << 8) | ((pixel >> 16) & 0xFF));
}

void skia_setPixel(int32 skiaSurface, int32 x, int32 y, Pixel pixel)
{
    SKIA_TRACE()
    backPaint.setColor(pixel);
    canvas->drawRect(SkRect::MakeXYWH(x, y, 1, 1), backPaint);
}

void skia_drawDottedLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2)
{
    SKIA_TRACE()
    float intervals[] = {5, 5};
    GfxPaint paint1 = gfxPaintFromColor((int32*)&pixel1);
    GfxPaint paint2 = gfxPaintFromColor((int32*)&pixel2);
    forePaint.setPathEffect(SkDashPathEffect::Make(intervals, 2, 2.5f));
    skia_drawLine(skiaSurface, x1, y1, x2, y2, paint1);
    forePaint.setPathEffect(nullptr);

    forePaint.setPathEffect(SkDashPathEffect::Make(intervals, 2, 7.5f));
    skia_drawLine(skiaSurface, x1, y1, x2, y2, paint2);
    forePaint.setPathEffect(nullptr);
}

void skia_drawLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, GfxPaint paint)
{
    SKIA_TRACE()
    forePaint.setColor(*paint.color);
    canvas->drawLine(x1, y1, x2, y2, forePaint);
}

void skia_drawRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel)
{
    SKIA_TRACE()
    forePaint.setColor(pixel);
    canvas->drawRect(SkRect::MakeXYWH(x, y, w, h), forePaint);
}

void skia_fillRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel)
{
    SKIA_TRACE()
    // printf("Exe log: skia fill rect = %#010x\n",pixel);
    backPaint.setColor(pixel);
    canvas->drawRect(SkRect::MakeXYWH(x, y, w, h), backPaint);
}

void skia_drawText(int32 skiaSurface, const void *text, int32 chrCount, int32 x0, int32 y0, Pixel foreColor, int32 justifyWidth, int32 fontSize, int32 typefaceIndex)
{
    SKIA_TRACE()
    const auto newTypeFace = skia_getTypeface(typefaceIndex);

    if(skFont.getTypeface() != newTypeFace.get()) {
        skFont.setTypeface(newTypeFace);
    }
    if(skFont.getSize() != fontSize) {
        skFont.setSize(fontSize);
    }
    if(backPaint.getColor() != foreColor){
        backPaint.setColor(foreColor);
    }
    canvas->drawTextBlob(SkTextBlob::MakeFromText(text,chrCount,skFont,SkTextEncoding::kUTF16),x0,y0,backPaint);
}

void skia_ellipseDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, bool fill, bool gradient)
{
    SKIA_TRACE()
    if (fill) {
        if (gradient) {
            SkPoint points[3] = {
                    SkPoint::Make(xc, yc - ry),
                    SkPoint::Make(xc, yc + ry),
                    SkPoint::Make(xc, yc + ry * 2)
            };
            SkColor colors[3] = {pc2, pc1, pc2};
            backPaint.setShader(SkGradientShader::MakeLinear(
                    points, colors, nullptr, 3,
                    SkTileMode::kClamp, 0, nullptr));
            canvas->drawOval(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), backPaint);
            backPaint.setShader(nullptr);
        } else {
            backPaint.setColor(pc2);
            canvas->drawOval(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), backPaint);
        }
    } else {
        forePaint.setColor(pc1);
        canvas->drawOval(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), forePaint);
    }
}

SkPath _skia_makePath(int32 *x, int32 *y, int32 n)
{
    SKIA_TRACE()
    SkPath path;
    path.moveTo(x[0], y[0]);
    for (int i = 1; i < n; ++i) {
        path.lineTo(x[i], y[i]);
    }

    return path;
}
void _skia_getPathBounds(int32 *x, int32 *y, int32 n, int32* minY, int32* maxY)
{
    SKIA_TRACE()
    *minY = y[0];
    *maxY = y[0];
    for (int i = 1; i < n; ++i) {
        *minY = y[i] < *minY ? y[i] : *minY;
        *maxY = *maxY < y[i] ? y[i] : *maxY;
    }
}
void skia_drawPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel pixel)
{
    SKIA_TRACE()
    forePaint.setColor(pixel);
    canvas->translate(tx, ty);
    canvas->drawPath(_skia_makePath(xPoints, yPoints, nPoints), forePaint);
    canvas->translate(-tx, -ty);
}

void skia_fillPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel c1, Pixel c2, bool gradient, bool isPie)
{
    SKIA_TRACE()
    SkPath path = _skia_makePath(xPoints, yPoints, nPoints);

    backPaint.setColor(c1);
    if (gradient) {
        int32 minY, maxY;
        _skia_getPathBounds(xPoints, yPoints, nPoints, &minY, &maxY);
        SkPoint points[2] = {
                SkPoint::Make(xPoints[0], minY),
                SkPoint::Make(xPoints[0], maxY),
        };
        SkColor colors[2] = {c1, c2};
        backPaint.setShader(SkGradientShader::MakeLinear(
                points, colors, nullptr, 2,
                SkTileMode::kClamp, 0, nullptr));
    }

    canvas->translate(tx, ty);
    canvas->drawPath(path, backPaint);
    canvas->translate(-tx, -ty);

    if (gradient) {
        backPaint.setShader(nullptr);
    }
}

// Adapted from SkPathPriv::CreateDrawArcPath
SkPath _skia_makeArcPath(const SkRect& oval, SkScalar startAngle, SkScalar sweepAngle, bool useCenter) {
    SkASSERT(!oval.isEmpty());
    SkASSERT(sweepAngle);

    SkPath path;
    path.setIsVolatile(true);
    path.setFillType(SkPathFillType::kWinding);
    path.reset();
    if (SkScalarAbs(sweepAngle) >= 360.f) {
        path.addOval(oval);
        return path;
    }
    if (useCenter) {
        path.moveTo(oval.centerX(), oval.centerY());
    }
    // Arc to mods at 360 and drawArc is not supposed to.
    bool forceMoveTo = !useCenter;
    while (sweepAngle <= -360.f) {
        path.arcTo(oval, startAngle, -180.f, forceMoveTo);
        startAngle -= 180.f;
        path.arcTo(oval, startAngle, -180.f, false);
        startAngle -= 180.f;
        forceMoveTo = false;
        sweepAngle += 360.f;
    }
    while (sweepAngle >= 360.f) {
        path.arcTo(oval, startAngle, 180.f, forceMoveTo);
        startAngle += 180.f;
        path.arcTo(oval, startAngle, 180.f, false);
        startAngle += 180.f;
        forceMoveTo = false;
        sweepAngle -= 360.f;
    }
    path.arcTo(oval, startAngle, sweepAngle, forceMoveTo);
    if (useCenter) {
        path.close();
    }

    return path;
}
void skia_arcPiePointDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, double startAngle, double endAngle, Pixel c, Pixel c2, bool fill, bool pie, bool gradient)
{
    double start = -startAngle;
    double sweepAngle = -(endAngle - startAngle);
    SKIA_TRACE()
    if (fill) {
        backPaint.setColor(c2);
        if (gradient) {
            SkPath arcPath = _skia_makeArcPath(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie);
            SkRect r = arcPath.computeTightBounds();

            SkPoint points[2] = {
                SkPoint::Make(r.centerX(), r.y()),
                SkPoint::Make(r.centerX(), r.y() + r.height() * 2)
            };
            SkColor colors[2] = {c, c2};
            backPaint.setShader(SkGradientShader::MakeLinear(
                    points, colors, nullptr, 3,
                    SkTileMode::kClamp, 0, nullptr));

            canvas->drawPath(arcPath, backPaint);
            backPaint.setShader(nullptr);
        } else {
            canvas->drawArc(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie, backPaint);
            forePaint.setColor(c);
            SkScalar strokeWidth = forePaint.getStrokeWidth();
            forePaint.setStrokeWidth(2);
            canvas->drawArc(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie, forePaint);
            forePaint.setStrokeWidth(strokeWidth);
        }
    } else {
        forePaint.setColor(c);
        canvas->drawArc(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie, forePaint);
    }
}

void skia_drawRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c)
{
    SKIA_TRACE()
    forePaint.setColor(c);
    canvas->drawRRect(SkRRect::MakeRectXY(SkRect::MakeXYWH(x, y, w, h), r, r), forePaint);
}

void skia_fillRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c)
{
    SKIA_TRACE()
    backPaint.setColor(c);
    canvas->drawRRect(SkRRect::MakeRectXY(SkRect::MakeXYWH(x, y, w, h), r, r), backPaint);
}

void skia_drawRRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, const double *radii, Pixel c, bool filled)
{
    SKIA_TRACE()
    SkRect rect = SkRect::MakeXYWH(x, y, w, h);
    SkPaint &paint = filled ? backPaint : forePaint;
    paint.setColor(c);

    if (radii == nullptr) {
        canvas->drawRect(rect, paint);
        return;
    }

    SkVector corners[4] = {
        SkVector::Make((SkScalar)radii[0], (SkScalar)radii[1]),
        SkVector::Make((SkScalar)radii[2], (SkScalar)radii[3]),
        SkVector::Make((SkScalar)radii[4], (SkScalar)radii[5]),
        SkVector::Make((SkScalar)radii[6], (SkScalar)radii[7])
    };

    SkRRect rr;
    rr.setRectRadii(rect, corners);
    canvas->drawRRect(rr, paint);
}

void skia_drawRoundGradient(int32 skiaSurface, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, bool vertical)
{
    SKIA_TRACE()
    int32 w = endX - startX;
    int32 h = endY - startY;
    SkPoint points[2];
    if (vertical) {
        points[0] = SkPoint::Make(startX, startY);
        points[1] = SkPoint::Make(startX, startY + h * 2);
    } else {
        points[0] = SkPoint::Make(startX, startY);
        points[1] = SkPoint::Make(startX + w * 2, startY);
    }

    SkColor colors[2] = {static_cast<SkColor>(startColor), static_cast<SkColor>(endColor)};
    backPaint.setShader(SkGradientShader::MakeLinear(
            points, colors, nullptr, 3,
            SkTileMode::kClamp, 0, nullptr));

    canvas->drawRRect(SkRRect::MakeRectXY(SkRect::MakeXYWH(startX, startY, w, h), topLeftRadius, topLeftRadius), backPaint);
    backPaint.setShader(nullptr);
}
int skia_getsetRGB(int32 skiaSurface, void *pixels, int32 offset, int32 x, int32 y, int32 w, int32 h, bool isGet)
{
    SKIA_TRACE()
    // SkImageInfo info = SkImageInfo::MakeN32Premul(w, h);

    SkBitmap bitmap;
    // bitmap.installPixels(SkImageInfo::Make(w, h, kN32_SkColorType, kUnpremul_SkAlphaType), (void*)pixels, sizeof(Pixel) * w, nullptr, nullptr);

    // SkBitmap bitmap;
    bitmap.allocPixels(SkImageInfo::MakeN32Premul(w, h));
    if (isGet) {
        return canvas->readPixels(bitmap, x, y);
    } else {
        return canvas->writePixels(bitmap, x, y);
    }

    // Pixel pixel = bitmap.getAddr32(0, 0)[0];
    // return (((pixel >> 24) & 0xFF) << 24) | (((pixel & 0xFF) << 16) | (((pixel >> 8) & 0xFF) << 8) | ((pixel >> 16) & 0xFF));

    // if (isGet) {
    //     return canvas->readPixels(info, pixels, info.minRowBytes(), x, y);
    // } else {
    //     return canvas->writePixels(info, pixels, info.minRowBytes(), x, y);
    // }
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
