// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "PalmFont.h"
#include "GraphicsPrimitives.h"
#include "math.h"

#if defined (WP8)
#include "openglWrapper.h"
#endif

#define TRANSITION_NONE  0
#define TRANSITION_OPEN  1
#define TRANSITION_CLOSE 2

#ifdef __gl2_h_
extern int32 appW,appH;
float ftransp[16];
float f255[256];
extern float *glXYA;
void glClearClip();
void glSetClip(int32 x1, int32 y1, int32 x2, int32 y2);
void glDrawDots(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb1, int32 rgb2);

void setupLookupBuffers()
{
   int32 i;
   for (i = 0; i < 14; i++)
      ftransp[i + 1] = (float)(i << 4) / (float)255; // make it lighter. since ftransp[0] is never used, shift it to [1]
   ftransp[15] = 1;
   for (i = 0; i <= 255; i++)
      f255[i] = (float)i / (float)255;
}

/*
static void glDrawPixelG(TCObject g, int32 xx, int32 yy, int32 color, int32 alpha)
{
   xx += Graphics_transX(g);
   yy += Graphics_transY(g);
   if (Graphics_clipX1(g) <= xx && xx <= Graphics_clipX2(g) && Graphics_clipY1(g) <= yy && yy <= Graphics_clipY2(g))
      glDrawPixel(xx,yy,color,alpha);
}
 */
#define graphicsLock(screenSurface, on) true
#define NO_GRAPHICS_LOCK_NEEDED

#endif


bool graphicsStartup(ScreenSurface screen, int16 appTczAttr);
bool graphicsCreateScreenSurface(ScreenSurface screen);
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen);
void graphicsDestroy(ScreenSurface screen, bool isScreenChange);
#if !defined(NO_GRAPHICS_LOCK_NEEDED)
bool graphicsLock(ScreenSurface screen, bool on);
#endif
void graphicsDestroyPrimitives();

static bool createScreenSurface(Context currentContext, bool isScreenChange);

void updateScreen(Context currentContext);
void privateScreenChange(int32 w, int32 h);
void markWholeScreenDirty(Context currentContext);
static bool translateAndClip(TCObject g, int32 *pX, int32 *pY, int32 *pWidth, int32 *pHeight);

#define Get_Clip(g) Graphics_clipX1(g), Graphics_clipY1(g), Graphics_clipX2(g), Graphics_clipY2(g)

// >>>>>>>>>
// DO NOT LOCK THE SCREEN ON THESE METHODS. THE CALLER MUST DO THAT.
static Pixel* getSurfacePixels(TCObject surf)
{
   TCObject pix;
   bool isImage=false;
   if (surf != null)
   {
      CharP name = OBJ_CLASS(surf)->name;
      if (name[0] == 't' && strEq(name,"totalcross.ui.gfx.Graphics")) // if the surface is a Graphics, get the target surface
         surf = Graphics_surface(surf);
      isImage = Surface_isImage(surf);
   }
   pix = isImage ? Image_pixels(surf) : screen.mainWindowPixels;
   return (Pixel*)ARRAYOBJ_START(pix);
}

static Pixel* getGraphicsPixels(TCObject g)
{
   TCObject surf = Graphics_surface(g);
   return getSurfacePixels(surf);
}
// <<<<<<<<<

void repaintActiveWindows(Context currentContext)
{
   static Method repaintActiveWindows;
   if (repaintActiveWindows == null && mainClass != null)
      repaintActiveWindows = getMethod(OBJ_CLASS(mainClass), true, "repaintActiveWindows", 0);
   if (repaintActiveWindows != null)
      executeMethod(currentContext, repaintActiveWindows);
}

void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int32 hRes, int32 vRes, bool nothingChanged) // rotate the screen
{
   // IMPORTANT: this is the only place that changes tcSettings
   screen.screenW = *tcSettings.screenWidthPtr  = newWidth;
   screen.pitch = screen.screenW * screen.bpp / 8;
   screen.screenH = *tcSettings.screenHeightPtr = newHeight;
   screen.hRes = *tcSettings.screenWidthInDPIPtr = hRes;
   screen.vRes = *tcSettings.screenHeightInDPIPtr = vRes;
   markWholeScreenDirty(currentContext);
   privateScreenChange(newWidth, newHeight);
   if (!nothingChanged)
   {
      graphicsDestroy(&screen, true);
      createScreenSurface(currentContext, true);
   }
   // post the event to the vm
   if (mainClass != null)
      postEvent(currentContext, KEYEVENT_SPECIALKEY_PRESS, SK_SCREEN_CHANGE, 0,0,-1); //XXX
   repaintActiveWindows(mainContext);
}

Pixel makePixelA(int32 a, int32 r, int32 g, int32 b)
{
   PixelConv p;
   p.a = (uint8)(a & 0xFF);
   p.r = (uint8)(r & 0xFF);
   p.g = (uint8)(g & 0xFF);
   p.b = (uint8)(b & 0xFF);
   return p.pixel;
}
Pixel makePixel(int32 r, int32 g, int32 b)
{
   PixelConv p;
   p.a = 0xFF;
   p.r = (uint8)(r & 0xFF);
   p.g = (uint8)(g & 0xFF);
   p.b = (uint8)(b & 0xFF);
   return p.pixel;
}
Pixel makePixelRGB(int32 rgb) // from Java's big endian to native format
{
   PixelConv p;
   p.a = 0xFF;
   p.r = (uint8)((rgb >> 16) & 0xFF);
   p.g = (uint8)((rgb >> 8)  & 0xFF);
   p.b = (uint8)( rgb        & 0xFF);
   return p.pixel;
}
Pixel makePixelARGB(int32 rgb) // from Java's big endian to native format
{
   PixelConv p;
   p.a = (uint8)((rgb >> 24) & 0xFF);
   p.r = (uint8)((rgb >> 16) & 0xFF);
   p.g = (uint8)((rgb >> 8)  & 0xFF);
   p.b = (uint8)( rgb        & 0xFF);
   return p.pixel;
}

// Updates the dirty area (or extends the current one)
// DO NOT LOCK THE SCREEN ON THIS METHOD. THE CALLER MUST DO THAT.
static void markScreenDirty(Context currentContext, int32 x, int32 y, int32 w, int32 h)
{
   LOCKVAR(screen);
   if (w > 0 && h > 0 && x < screen.screenW && y < screen.screenH)
   {
      int32 x2,y2;
      /* Clip with the Screen surface */
      if (x < 0) x = 0;
      if (y < 0) y = 0;
      x2 = x+w;
      y2 = y+h;
      if (x2 > screen.screenW) x2 = screen.screenW;
      if (y2 > screen.screenH) y2 = screen.screenH;

      if (currentContext->dirtyX1 < x)
      {
         x2 = (currentContext->dirtyX2 > x2) ? currentContext->dirtyX2 : x2;
         x  = currentContext->dirtyX1;
      }
      else
      if (currentContext->dirtyX2 > x2)
         x2 = currentContext->dirtyX2;

      if (currentContext->dirtyY1 < y)
      {
         y2 = (currentContext->dirtyY2 > y2) ? currentContext->dirtyY2 : y2;
         y  = currentContext->dirtyY1;
      }
      else
      if (currentContext->dirtyY2 > y2)
         y2 = currentContext->dirtyY2;

      currentContext->dirtyX1 = x;
      currentContext->dirtyY1 = y;
      currentContext->dirtyX2 = x2;
      currentContext->dirtyY2 = y2;
      if (x == 0 && y == 0 && x2 == screen.screenW && y2 == screen.screenH)
         currentContext->fullDirty = true;
   }
   UNLOCKVAR(screen);
}

#ifdef ANDROID
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "TotalCross", __VA_ARGS__)
#else
#define LOGD(...) debug(__VA_ARGS__)
#endif

void markDirty(Context currentContext, TCObject surface, int x, int y, int w, int h) {
    if (Graphics_isImageSurface(surface)) {
        Image_changed(Graphics_surface(surface)) = true;
    } else {
        currentContext->dirtyX1 = min32(currentContext->dirtyX1, x);
        currentContext->dirtyY1 = min32(currentContext->dirtyY1, y);
        currentContext->dirtyX2 = max32(currentContext->dirtyX2, x + w);
        currentContext->dirtyY2 = max32(currentContext->dirtyY2, y + h);
    }
}

// This is the main routine that draws a surface (a Control or an Image) in the destination GfxSurface.
// Destination is always a Graphics object.
#ifndef SKIA_H
static void drawSurface(Context currentContext, TCObject dstSurf, TCObject srcSurf, int32 srcX, int32 srcY, int32 width, int32 height,
                       int32 dstX, int32 dstY, int32 doClip)
{
   uint32 i;
   Pixel * srcPixels;
   Pixel * dstPixels;
   int32 srcPitch, srcWidth, srcHeight, alphaMask = 0;
   bool isSrcScreen = !Surface_isImage(srcSurf);
   bool unlockSrc = false;
   if (Surface_isImage(srcSurf))
   {                                
#ifdef __gl2_h_ // for opengl, we will use the smoothScaled only if we will draw on an image. for win32, we will always use smoothScale
      bool forcedSmoothScale = Graphics_isImageSurface(dstSurf); // the destination is always a Graphics object
#else
      bool forcedSmoothScale = true;
#endif
      srcPitch = srcWidth = (int32)(Image_width(srcSurf) * Image_hwScaleW(srcSurf));
      srcHeight = (int32)(Image_height(srcSurf) * Image_hwScaleH(srcSurf));
      if (forcedSmoothScale && (Image_hwScaleW(srcSurf) != 1 || Image_hwScaleH(srcSurf) != 1))
      {
         static Method mGetScaledInstance, mGetSmoothScaledInstance;
         TCObject newSurf;
         if (mGetScaledInstance == null)
         {
            mGetScaledInstance       = getMethod(OBJ_CLASS(srcSurf), false, "getScaledInstance", 2, J_INT, J_INT);
            mGetSmoothScaledInstance = getMethod(OBJ_CLASS(srcSurf), false, "getSmoothScaledInstance", 2, J_INT, J_INT);
         }
         disableGC = true; // the gc may collect the image before we lock it here (*)
         newSurf = executeMethod(currentContext, Image_hwScaleW(srcSurf) < 1 && Image_hwScaleH(srcSurf) < 1 ? mGetSmoothScaledInstance : mGetScaledInstance, srcSurf, srcWidth, srcHeight).asObj;
         if (newSurf == null || newSurf == (TCObject)0xFFFFFFFF)
         {
            currentContext->thrownException = null;
            return;
         }
         else
         {
            srcSurf = newSurf;
            setObjectLock(newSurf, LOCKED); // (*)
            unlockSrc = true;
         }
         disableGC = false;
         srcPitch = srcWidth = Image_width(srcSurf);
         srcHeight = Image_height(srcSurf);
      }
      alphaMask = Image_alphaMask(srcSurf);
   }
   else
   {
      srcPitch = srcWidth = screen.screenW;
      srcHeight = screen.screenH;
   }
   dstPixels = getSurfacePixels(dstSurf);
   srcPixels = getSurfacePixels(srcSurf);
   if (!doClip)
   {
      /*
      | Even if no clip is required, we still have to make sure that the
      | area of the bitmap that we want to copy is inside its area.
      */
      if (srcX <= -width  || srcX >= srcWidth || srcY <= -height || srcY >= srcHeight)
         goto end;
      dstX += Graphics_transX(dstSurf);
      dstY += Graphics_transY(dstSurf);
   }
   else
   {
      dstX += Graphics_transX(dstSurf);
      dstY += Graphics_transY(dstSurf);

      /* clip the source rectangle to the source surface */
      if (srcX < 0)
      {
         width += srcX;
         dstX -= srcX;
         srcX = 0;
      }
      i = srcWidth - srcX;
      if (width > (int32)i)
         width = i;
      if (srcY < 0)
      {
         height += srcY;
         dstY -= srcY;
         srcY = 0;
      }
      i = srcHeight - srcY;
      if (height > (int32)i)
         height = i;

      /* clip the destination rectangle against the clip rectangle */
      if (dstX < Graphics_clipX1(dstSurf))
      {
         i = Graphics_clipX1(dstSurf) - dstX;
         dstX = Graphics_clipX1(dstSurf);
         srcX += i;
         width -= i;
      }
      if ((dstX + width) > Graphics_clipX2(dstSurf))
         width = Graphics_clipX2(dstSurf) - dstX;
      if (dstY < Graphics_clipY1(dstSurf))
      {
         i = Graphics_clipY1(dstSurf) - dstY;
         dstY = Graphics_clipY1(dstSurf);
         srcY += i;
         height -= i;
      }
      if ((dstY + height) > Graphics_clipY2(dstSurf))
         height = Graphics_clipY2(dstSurf) - dstY;

      /* check the validity */
      if (width <= 0 || height <= 0)
         goto end;
   }

   srcPixels += srcY * srcPitch + srcX;
   dstPixels += dstY * Graphics_pitch(dstSurf) + dstX;
#ifdef __gl2_h_
   if (isSrcScreen)
      glGetPixels(dstPixels,srcX,srcY,width,height,Graphics_pitch(dstSurf));
   else
   if (Graphics_useOpenGL(dstSurf))
   {
      int32 fc;
      int frame;

      if (Image_changed(srcSurf))
         applyChanges(currentContext, srcSurf);
      fc = Image_frameCount(srcSurf);
      frame = (fc <= 1) ? 0 : Image_currentFrame(srcSurf);
      Image_lastAccess(srcSurf) = getTimeStamp();
/*img*/ glDrawTexture(Image_textureId(srcSurf), srcX+frame*srcPitch,srcY,width,height, dstX,dstY, 0,0, fc > 1 ? (int32)(Image_widthOfAllFrames(srcSurf) * Image_hwScaleW(srcSurf)) : srcWidth,srcHeight, null, alphaMask);
   }
   else
#endif
   for (i=0; i < (uint32)height; i++) // in opengl, only case of image drawing on image
   {
      PixelConv *ps = (PixelConv*)srcPixels;
      PixelConv *pt = (PixelConv*)dstPixels;
      uint32 count = width;
      if (isSrcScreen)
         for (;count != 0; pt++,ps++, count--)
         {
            pt->pixel = ps->pixel;
            pt->a = 0xFF;
         }
      else
         for (;count != 0; pt++,ps++, count--)
         {
            int32 a = ps->a * alphaMask;
            a = (a+1 + (a >> 8)) >> 8; // alphaMask * a / 255
            if (a == 0xFF)
               pt->pixel = ps->pixel;
            else
            if (a != 0)
            {
               int32 ma = 0xFF-a;
               int32 r = (a * ps->r + ma * pt->r);
               int32 g = (a * ps->g + ma * pt->g);
               int32 b = (a * ps->b + ma * pt->b);
               pt->r = (r+1 + (r >> 8)) >> 8; // fast way to divide by 255
               pt->g = (g+1 + (g >> 8)) >> 8;
               pt->b = (b+1 + (b >> 8)) >> 8;
            }
         }
      srcPixels += srcPitch;
      dstPixels += Graphics_pitch(dstSurf);
   }
#ifndef __gl2_h_
   if (!currentContext->fullDirty && !Graphics_isImageSurface(dstSurf)) markScreenDirty(currentContext, dstX, dstY, width, height);
#else
   if (Graphics_isImageSurface(dstSurf))
      Image_changed(Graphics_surface(dstSurf)) = true;
   else
      currentContext->fullDirty = true;
#endif
end:
   if (unlockSrc)
      setObjectLock(srcSurf, UNLOCKED);
}
#else
static void drawSurface(Context currentContext, TCObject dstSurf, TCObject srcSurf, int32 srcX, int32 srcY, int32 w, int32 h,
                        int32 dstX, int32 dstY, int32 doClip)
{
    if (Surface_isImage(srcSurf))
    {
        TCObject pixelsObj = Image_pixels(srcSurf);
        Pixel *pixels = (Pixel *)ARRAYOBJ_START(pixelsObj);
        int32 width = Image_width(srcSurf);
        int32 height = Image_height(srcSurf);
        int32 id = Image_textureId(srcSurf);

        Image_textureId(srcSurf) = skia_makeBitmap(id, pixels, width, height);

        dstX += Graphics_transX(dstSurf);
        dstY += Graphics_transY(dstSurf);
        skia_setClip(Get_Clip(dstSurf));
        skia_drawSurface(0, Image_textureId(srcSurf), srcX, srcY, w, h, w, h, dstX, dstY, doClip);
        skia_restoreClip();
    }
    else
    {
        LOGD("Trying to draw a control surface into some other surface");
    }

    markDirty(currentContext, dstSurf, dstX, dstY, w, h);
}
#endif

//   Device specific routine.
//   Gets the color value of the pixel, using the current translation
//   Returns -1 if error (out of clip bounds)
static int32 getPixel(TCObject g, int32 x, int32 y)
{
   int32 ret = -1;
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y < Graphics_clipY2(g))
   {
      PixelConv p;
#ifdef SKIA_H
        return skia_getPixel(0, x, y);
#else
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
         return glGetPixel(x,y);
      else
#endif
         p.pixel = getGraphicsPixels(g)[y * Graphics_pitch(g) + x];
      ret = (p.r << 16) | (p.g << 8) | p.b;
#endif
   }
   return ret;
}

#ifndef SKIA_H
static PixelConv getPixelConv(TCObject g, int32 x, int32 y)
{
   PixelConv p;
   p.pixel = -1;
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y < Graphics_clipY2(g))
   {
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
         p.pixel = glGetPixel(x,y);
      else
#endif
         p.pixel = getGraphicsPixels(g)[y * Graphics_pitch(g) + x];
   }
   return p;
}
#endif

// Device specific routine.
// Sets the pixel to the given color, translating and clipping
#ifndef SKIA_H
static void setPixel(Context currentContext, TCObject g, int32 x, int32 y, Pixel pixel)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y < Graphics_clipY2(g))
   {
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
      {
         glDrawPixel(x,y,pixel,255);
         if (Graphics_isImageSurface(g))
            Image_changed(Graphics_surface(g)) = true;
         else
            currentContext->fullDirty = true;
      }
      else
#endif
      {
         getGraphicsPixels(g)[y * Graphics_pitch(g) + x] = pixel;
         if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, x, y, 1, 1);
      }
   }
}
#else
static void setPixel(Context currentContext, TCObject g, int32 x, int32 y, Pixel pixel)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_setPixel(0, x, y, pixel | Graphics_alpha(g));
   skia_restoreClip();

   markDirty(currentContext, g, x, y, 1, 1);
}
#endif

#ifndef SKIA_H

static PixelConv interpolatePC(PixelConv c, PixelConv d, int32 factor)
{
   int m = 255 - factor;
   c.r = (c.r*factor + d.r*m) / 255;
   c.g = (c.g*factor + d.g*m) / 255;
   c.b = (c.b*factor + d.b*m) / 255;
   return c;
}

static int32 interpolate(PixelConv c, PixelConv d, int32 factor)
{
   return interpolatePC(c,d,factor).pixel;
}

static bool surelyOutsideClip(TCObject g, int32 x1, int32 y1, int32 x2, int32 y2)
{
   int cx1 = Graphics_clipX1(g);
   int cx2 = Graphics_clipX2(g);
   int cy1 = Graphics_clipY1(g);
   int cy2 = Graphics_clipY2(g);
   x1 += Graphics_transX(g);
   x2 += Graphics_transX(g);
   y1 += Graphics_transY(g);
   y2 += Graphics_transY(g);
   return (x1 < cx1 && x2 < cx1) || (x1 > cx2 && x2 > cx2) || (y1 < cy1 && y2 < cy1) || (y1 > cy2 && y2 > cy2);
}

//   Device specific routine.
//   Draws a horizontal line from x to x+w-1, translating and clipping
static void drawHLine(Context currentContext, TCObject g, int32 x, int32 y, int32 width, Pixel pixel1, Pixel pixel2)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   /*
   | line must lie inside y clip bounds, must not end before clip x1
   | and must not start after clip x2
   */
   if (Graphics_clipY1(g) <= y && y < Graphics_clipY2(g) && Graphics_clipX1(g) <= (x+width) && x < Graphics_clipX2(g)) // NOPT
   {
      Pixel* pTgt;
      if (x < Graphics_clipX1(g))           // line start before clip x1
      {
         width -= Graphics_clipX1(g)-x;
         x = Graphics_clipX1(g);
      }
      if ((x+width) > Graphics_clipX2(g))   // line stops after clip x2
         width = Graphics_clipX2(g)-x;

      if (width <= 0)
         return;
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
      {
#ifdef WP8
         if (pixel1 == pixel2)
            glDrawLine(x, y, x + width, y, pixel1, 255);
         else
         if (checkGLfloatBuffer(currentContext, width/2+2))
         {
            float *xya = glXYA;
            int32 xx, ww, nn=0;
            for (xx = x, ww = width; ww > 0; ww -= 2, xx += 2, nn++)
            {
               *xya++ = (float)xx;   
               *xya++ = (float)y; // vertices
               *xya++ = 1; // alpha
            }
            if (nn > 0) glDrawPixels(nn,pixel1);

            xya = glXYA;
            nn = 0;
            for (xx = x + 1, ww = width - 1; ww > 0; ww -= 2, xx += 2, nn++)
            {
               *xya++ = (float)xx;
               *xya++ = (float)y; // vertices
               *xya++ = 1; // alpha
            }
            if (nn > 0) glDrawPixels(nn,pixel2);
         }
#else
         if (pixel1 == pixel2)
            glDrawLine(x, y, x + width, y, pixel1, 255);
         else
            glDrawDots(x, y, x + width, y, pixel1, pixel2);
#endif
         currentContext->fullDirty = true;
      }
      else
#endif
      {
         pTgt = getGraphicsPixels(g) + y * Graphics_pitch(g) + x;
         if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, x, y, width, 1);
         if (pixel1 == pixel2) // same color?
         {
            while (width-- > 0)
               *pTgt++ = pixel1;          // plot the pixel
         }
         else
         {
            int32 i=0;
            while (width-- > 0)
               *pTgt++ = (i++ & 1) ? pixel1 : pixel2;
         }
      }
   }
}

//   Device specific routine.
//   Draws a vertical line from y to y+h-1 using the given color
static void drawVLine(Context currentContext, TCObject g, int32 x, int32 y, int32 height, Pixel pixel1, Pixel pixel2)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   /*
   | line must lie inside x clip bounds, must not end before clip y1
   | and must not start after clip y2
   */
   if (Graphics_clipX1(g) <= x && x < Graphics_clipX2(g) && Graphics_clipY1(g) <= (y+height) && y < Graphics_clipY2(g)) // NOPT
   {
      Pixel * pTgt;
      int32 pitch = Graphics_pitch(g);
      uint32 n;
      if (y < Graphics_clipY1(g))           // line start before clip y1
      {
         height -= Graphics_clipY1(g)-y;
         y = Graphics_clipY1(g);
      }
      if ((y+height) > Graphics_clipY2(g))
         height = Graphics_clipY2(g)-y;           // line stops after clip y2

      if (height <= 0)
         return;
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
      {
#ifdef WP8
         if (pixel1 == pixel2)
            glDrawLine(x, y, x, y + height, pixel1, 255);
         else
         if (checkGLfloatBuffer(currentContext, height/2+2))
         {
            float *xya = glXYA;
            int32 yy, hh, nn = 0;
            for (yy=y,hh=height; hh > 0; hh -= 2, yy += 2, nn++)
            {
               *xya++ = (float)x;
               *xya++ = (float)yy; // vertices
               *xya++ = 1; // alpha
            }
            if (nn > 0) glDrawPixels(nn,pixel1);

            xya = glXYA;                                                
            nn = 0;
            for (yy = y+1, hh = height-1; hh > 0; hh -= 2, yy += 2, nn++)
            {
               *xya++ = (float)x;
               *xya++ = (float)yy; // vertices
               *xya++ = 1; // alpha
            }
            if (nn > 0) glDrawPixels(nn,pixel2);
         }
#else
         if (pixel1 == pixel2)
            glDrawLine(x, y, x, y + height, pixel1, 255);
         else
            glDrawDots(x, y, x, y + height, pixel1, pixel2);
#endif
         currentContext->fullDirty = true;
      }
      else
#endif
      {
         pTgt = getGraphicsPixels(g) + y * pitch + x;
         n = (uint32)height;
         if (pixel1 == pixel2) // same color?
         {
            for (; n != 0; pTgt += pitch, n--)
               *pTgt = pixel1;          // plot the pixel
         }
         else
         {
            uint32 i=0;
            for (; n != 0; pTgt += pitch, n--)
               *pTgt = (i++ & 1) ? pixel1 : pixel2;          // plot the pixel
         }
         if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, x, y, 1, height);
      }
   }
}

static void drawDottedLine(Context currentContext, TCObject g, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2)
{
    // guich@501_10: added pyInc and clipping support for this routine.
    // store the change in X and Y of the line endpoints
    int32 dX,dY;
    // DETERMINE "DIRECTIONS" TO INCREMENT X AND Y (REGARDLESS OF DECISION)
    int32 xInc,yInc,pyInc; // py incs by pixel, y incs by row
    // used in clipping
    int32 xMin,yMin;
    int32 tX = Graphics_transX(g), tY = Graphics_transY(g);

    if (surelyOutsideClip(g, x1,y1,x2,y2)) // guich@tc115_63
       return;

    // compute the values
    if (x1 <= x2)
    {
       dX = x2-x1;
       xMin = x1;
       xInc = 1;
    }
    else
    {
       dX = x1-x2;
       xMin = x2;
       xInc = -1;
    }
    if (y1 <= y2)
    {
       dY = y2-y1;
       yMin = y1;
       pyInc = 1;
    }
    else
    {
       dY = y1-y2;
       yMin = y2;
       pyInc = -1;
    }

    // guich: if its a pixel, draw only the pixel
    if (dX == 0 && dY == 0)
       setPixel(currentContext, g, xMin,yMin,pixel1);
    else
    if (dY == 0) // horizontal line?
       drawHLine(currentContext, g, min32(x1,x2),min32(y1,y2),dX+1,pixel1,pixel2);
    else
    if (dX == 0) // vertical line?
       drawVLine(currentContext, g, min32(x1,x2),min32(y1,y2),dY+1,pixel1,pixel2);
    else // guich@566_43: removed the use of drawH/VLine to make sure that it will draw the same of desktop
    {
       int32 currentX,currentY;  // saved for later markScreenDirty
       Pixel *row;
       int32 on = 1;
       int32 clipX1 = Graphics_clipX1(g), clipY1 = Graphics_clipY1(g), clipX2 = Graphics_clipX2(g), clipY2 = Graphics_clipY2(g);
       bool dontClip = true; // the most common will be draw lines that do not cross the clip bounds, so we may speedup a little

       xMin += tX;
       yMin += tY;

       if (xMin < clipX1 || (xMin+dX) >= clipX2 || yMin < clipY1 || (yMin+dY) >= clipY2)
          dontClip = false;

       currentX = x1+tX;
       currentY = y1+tY;

       row = getGraphicsPixels(g) + (pyInc>0 ? yMin : (yMin+dY)) * Graphics_pitch(g) + (xInc>0 ? xMin : (xMin+dX)); // currentX    | else start from the max value

       yInc = pyInc>0 ? Graphics_pitch(g) : -Graphics_pitch(g);  // now that we have active, correctly assign the y incrementing value

       // DETERMINE INDEPENDENT VARIABLE (ONE THAT ALWAYS INCREMENTS BY 1 (OR -1) )
       // AND INITIATE APPROPRIATE LINE DRAWING ROUTINE (BASED ON FIRST OCTANT
       // ALWAYS). THE X AND Y'S MAY BE FLIPPED IF Y IS THE INDEPENDENT VARIABLE.
       if (dX >= dY)   // if X is the independent variable
       {
          int32 dPr     = dY<<1;                         // amount to increment decision if right is chosen (always)
          int32 dPru    = dPr - (dX<<1);                 // amount to increment decision if up is chosen
          int32 p       = dPr - dX;                      // decision variable start value

          if (pixel1 == pixel2) // quick optimization
             for (; dX >= 0; dX--)                       // process each point in the line one at a time (just use dX)
             {
                if (dontClip || (clipX1 <= currentX && currentX < clipX2 && clipY1 <= currentY && currentY < clipY2))
                {
#ifdef __gl2_h_
                   if (Graphics_useOpenGL(g))
                      glDrawPixel(currentX, currentY, pixel1, 255);
                   else
#endif
                      *row = pixel1;                        // plot the pixel - never in opengl
                }
                row      += xInc;                        // increment independent variable
                currentX += xInc;
                if (p > 0)                               // is the pixel going right AND up?
                {
                   currentY += pyInc;
                   p += dPru;                            // increment decision (for up)
                   row += yInc;                          // increment dependent variable
                }
                else                                     // is the pixel just going right?
                   p += dPr;                             // increment decision (for right)
             }
          else
             for (; dX >= 0; dX--)                       // process each point in the line one at a time (just use dX)
             {
                if (dontClip || (clipX1 <= currentX && currentX < clipX2 && clipY1 <= currentY && currentY < clipY2))
                {
#ifdef __gl2_h_
                   if (Graphics_useOpenGL(g))
                      glDrawPixel(currentX, currentY, (on++ & 1) ? pixel1 : pixel2, 255);
                   else
#endif
                   *row = (on++ & 1) ? pixel1 : pixel2;  // plot the pixel
                }
                row += xInc;                             // increment independent variable
                currentX += xInc;
                if (p > 0)                               // is the pixel going right AND up?
                {
                   currentY += pyInc;
                   p += dPru;                            // increment decision (for up)
                   row += yInc;                          // increment dependent variable
                }
                else                                     // is the pixel just going right?
                   p += dPr;                             // increment decision (for right)
             }
       }
       else            // if Y is the independent variable
       {
          int32 dPr     = dX<<1;                         // amount to increment decision if right is chosen (always)
          int32 dPru    = dPr - (dY<<1);                 // amount to increment decision if up is chosen
          int32 p       = dPr - dY;                      // decision variable start value

          if (pixel1 == pixel2) // quick optimization
             for (; dY >= 0; dY--)                       // process each point in the line one at a time (just use dY)
             {
                if (dontClip || (clipX1 <= currentX && currentX < clipX2 && clipY1 <= currentY && currentY < clipY2))
                {
#ifdef __gl2_h_
                   if (Graphics_useOpenGL(g))
                      glDrawPixel(currentX, currentY, pixel1, 255);
                   else
#endif
                      *row = pixel1;                        // plot the pixel
                }
                row += yInc;                             // increment independent variable
                currentY += pyInc;
                if (p > 0)                               // is the pixel going up AND right?
                {
                   row += xInc;                          // increment dependent variable
                   currentX += xInc;
                   p += dPru;                            // increment decision (for up)
                }
                else                                     // is the pixel just going up?
                   p += dPr;                             // increment decision (for right)
             }
          else
             for (; dY >= 0; dY--)                       // process each point in the line one at a time (just use dY)
             {
                if (dontClip || (clipX1 <= currentX && currentX < clipX2 && clipY1 <= currentY && currentY < clipY2))
                {
#ifdef __gl2_h_
                   if (Graphics_useOpenGL(g))
                      glDrawPixel(currentX, currentY, (on++ & 1) ? pixel1 : pixel2, 255);
                   else
#endif
                   *row = (on++ & 1) ? pixel1 : pixel2;  // plot the pixel
                }
                row += yInc;                             // increment independent variable
                currentY += pyInc;
                if (p > 0)                               // is the pixel going up AND right?
                {
                   row += xInc;                          // increment dependent variable
                   currentX += xInc;
                   p += dPru;                            // increment decision (for up)
                }
                else                                     // is the pixel just going up?
                   p += dPr;                             // increment decision (for right)
             }
       }
#ifndef __gl2_h_
       if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, xMin, yMin, (x2>x1)?(x2-x1):(x1-x2), (y2>y1)?(y2-y1):(y1-y2));
#else
      if (Graphics_isImageSurface(g))
         Image_changed(Graphics_surface(g)) = true;
      else
         currentContext->fullDirty = true;
#endif
    }
}
#else
static void drawDottedLine(Context currentContext, TCObject g, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2)
{
    x1 += Graphics_transX(g);
    y1 += Graphics_transY(g);
    x2 += Graphics_transX(g);
    y2 += Graphics_transY(g);
    skia_setClip(Get_Clip(g));
    skia_drawDottedLine(0, x1, y1, x2, y2, pixel1 | Graphics_alpha(g), pixel2 | Graphics_alpha(g));
    skia_restoreClip();

    markDirty(currentContext, g, min32(x1, x2), min32(y1, y2), abs(x2 - x1), abs(y2 - y1));
}
#endif

#ifndef SKIA_H
#if !defined(WP8)
static int32 abs32(int32 a)
{
   return a < 0 ? -a : a;
}
#endif

static void drawLine(Context currentContext, TCObject g, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel)
{
   drawDottedLine(currentContext, g, x1, y1, x2, y2, pixel, pixel);
}
#else
static void drawLine(Context currentContext, TCObject g, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel)
{
   x1 += Graphics_transX(g);
   y1 += Graphics_transY(g);
   x2 += Graphics_transX(g);
   y2 += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_drawLine(0, x1, y1, x2, y2, pixel | Graphics_alpha(g));
   skia_restoreClip();

   markDirty(currentContext, g, min32(x1, x2), min32(y1, y2), abs(x2 - x1), abs(y2 - y1));
}
#endif

//   Draws a rectangle with the given color
#ifndef SKIA_H
static void drawRect(Context currentContext, TCObject g, int32 x, int32 y, int32 width, int32 height, Pixel pixel)
{
   drawHLine(currentContext, g, x, y, width, pixel, pixel);
   drawHLine(currentContext, g, x, y+height-1, width, pixel, pixel);
   drawVLine(currentContext, g, x, y, height, pixel, pixel);
   drawVLine(currentContext, g, x+width-1, y, height, pixel, pixel);
}
#else
static void drawRect(Context currentContext, TCObject g, int32 x, int32 y, int32 w, int32 h, Pixel pixel)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_drawRect(0, x, y, w, h, pixel | Graphics_alpha(g));
   skia_restoreClip();

   markDirty(currentContext, g, x, y, w, h);
}
#endif

// Description:
//   Device specific routine.
//   Fills a rectangle with the given color
#ifndef SKIA_H
static void fillRect(Context currentContext, TCObject g, int32 x, int32 y, int32 width, int32 height, Pixel pixel)
{
   int32 clipX1 = Graphics_clipX1(g);
   int32 clipX2 = Graphics_clipX2(g);
   int32 clipY1 = Graphics_clipY1(g);
   int32 clipY2 = Graphics_clipY2(g);
   x += Graphics_transX(g);
   y += Graphics_transY(g);

   if (x < clipX1)             // line starts before clip x1
   {
      width -= clipX1-x;
      x = clipX1;
   }
   if ((x+width) > clipX2)     // line stops after clip x2
      width = clipX2-x;

   if (y < clipY1)             // line starts before clip y1
   {
      height -= clipY1-y;
      y = clipY1;
   }
   if ((y+height) > clipY2)    // line stops after clip y2
      height = clipY2-y;

   if (height > 0 && width > 0)
   {
#ifdef __gl2_h_
      if (Graphics_useOpenGL(g))
      {
         glFillRect(x,y,width,height,pixel,255);
         if (Graphics_isImageSurface(g))
            Image_changed(Graphics_surface(g)) = true;
         else
            currentContext->fullDirty = true;
      }
      else
#endif
      {
         uint32 count;
         int32 pitch = Graphics_pitch(g);
         Pixel* to = getGraphicsPixels(g) + y * pitch + x;
         if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, x, y, width, height);
         if (x == 0 && width == pitch) // filling with full width?
         {
            int32* t = (int32*)to;
            int32 p2 = pixel;
            count = width*height;
            for (; count != 0; count--)
               *t++ = p2;
         }
         else
         {
            uint32 i = width, j = height;
            for (pitch -= width; j != 0;  to += pitch, i=width, j--)
               for (; i != 0; i--)
                  *to++ = pixel;
         }
      }
   }
}
#else
static void fillRect(Context currentContext, TCObject g, int32 x, int32 y, int32 w, int32 h, Pixel pixel)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_fillRect(0, x, y, w, h, pixel | Graphics_alpha(g));
   skia_restoreClip();

   markDirty(currentContext, g, x, y, w, h);
}
#endif

#ifdef SKIA_H
// Darkens the screen
static void fadeScreen(Context currentContext, int32 amount) {
    skia_fillRect(0, 0, 0, screen.screenW, screen.screenH,  amount << 24);
    currentContext->dirtyX1 = 0;
    currentContext->dirtyY1 = 0;
    currentContext->dirtyX2 = screen.screenW;
    currentContext->dirtyY2 = screen.screenH;
}
#endif

#ifndef SKIA_H
#define INTERP(j,f,shift) (j + (((f - j) * transparency) >> shift)) & 0xFF

static uint8 _ands8[8] = {0x80,0x40,0x20,0x10,0x08,0x04,0x02,0x01};
uint8* getResizedCharPixels(Context currentContext, UserFont uf, JChar ch, int32 w, int32 h);

static void drawText(Context currentContext, TCObject g, JCharP text, int32 chrCount, int32 x0, int32 y0, Pixel foreColor, int32 justifyWidth)
{
   TCObject fontObj = Graphics_font(g);
   int32 startBit, currentBit, incY, y1, r, rmax, istart;
   uint8 *bitmapTable, *ands, *current, *start;
   uint16* bitIndexTable;
   int32 rowWIB, offset, xMin, xMax, yMin, yMax, x, y, yDif, width, width0, height, spaceW = 0, k, clipX1,clipX2,clipY1,clipY2, pitch;
   Pixel transparency, *row0, *row;
   PixelConv *i;
   bool isNibbleStartingLow, isLowNibble, isClipped;
   int aaType;
   JChar ch, first, last;
   UserFont uf = null;
   PixelConv fc;
   int32 extraPixelsPerChar = 0, extraPixelsRemaining = -1, rem;
   uint8 *ands8 = _ands8;
   int32 fcR, fcG, fcB;
#ifdef __gl2_h_
   int32 charXY[2];
   float *xya;
#endif
   int32 diffW;
   bool isVert = Graphics_isVerticalText(g);
   bool isGL = Graphics_useOpenGL(g);

   if (!text || chrCount == 0 || fontObj == null) return;

   fc.pixel = foreColor;
   fcR = fc.r;
   fcG = fc.g;
   fcB = fc.b;

   uf = loadUserFontFromFontObj(currentContext, fontObj, ' ');
   if (uf == null) return;
   diffW = uf->ubase && uf->isDefaultFont;
   rowWIB = uf->rowWidthInBytes;
   bitIndexTable = uf->bitIndexTable;
   bitmapTable = uf->bitmapTable;
   first = uf->fontP.firstChar;
   last = uf->fontP.lastChar;

   aaType = uf->fontP.antialiased;
   height = uf->fontP.maxHeight;
   incY = height + justifyWidth;

   x0 += Graphics_transX(g);
   y0 += Graphics_transY(g);

   if (justifyWidth > 0)
   {
      while (chrCount > 0 && text[chrCount - 1] <= (JChar)' ')
         chrCount--;
      if (chrCount == 0) return;
      rem = justifyWidth - getJCharPWidth(currentContext, fontObj, text, chrCount);
      if (rem > 0)
      {
         extraPixelsPerChar = rem / chrCount;
         extraPixelsRemaining = rem % chrCount;
      }
   }
   clipX1 = Graphics_clipX1(g);
   clipX2 = Graphics_clipX2(g);
   clipY1 = Graphics_clipY1(g);
   clipY2 = Graphics_clipY2(g);

   xMax = xMin = (x0 < clipX1) ? clipX1 : x0;
   yMax = y0 + (isVert ? chrCount * incY : height);
   yMin = (y0 < clipY1) ? clipY1 : y0;
   if (yMax >= clipY2)
      yMax = clipY2;
   if (getGraphicsPixels(g) == null)
      return;
   row0 = getGraphicsPixels(g) + yMin * Graphics_pitch(g);
   yDif = yMin - y0;
   y = y0;

   pitch = Graphics_pitch(g);
   for (k = 0; k < chrCount; k++) // guich@402
   {
      ch = *text++;
      if (ch <= ' ' || ch == 160)
      {
         if (ch == ' ' || ch == '\t' || ch == 160)
         {
            if (isVert)
               y += ch == '\t' ? incY * *tabSizeField : incY;
            else
            {
               x0 += getJCharWidth(currentContext, fontObj, ch)+extraPixelsPerChar;
               if (k <= extraPixelsRemaining)
                  x0++;
            }
         }
         continue;
      }
      if (uf == null || ch < first || ch > last)
      {
         uf = loadUserFontFromFontObj(currentContext, fontObj, ch);
         if (uf == null || ch < uf->fontP.firstChar || ch > uf->fontP.lastChar) // invalid char - guich@tc122_23: must also check the font's range
         {
            x0 += spaceW ? spaceW : (spaceW=getJCharWidth(currentContext, fontObj, ' ')) + extraPixelsPerChar;
            if (k <= extraPixelsRemaining)
               x0++;
            continue;
         }
         rowWIB = uf->rowWidthInBytes;
         bitIndexTable = uf->bitIndexTable;
         bitmapTable = uf->bitmapTable;
         first = uf->fontP.firstChar;
         last = uf->fontP.lastChar;
      }
#ifdef __gl2_h_
      if (!checkGLfloatBuffer(currentContext, uf->fontP.maxHeight * uf->fontP.maxWidth))
         return;
#endif
      // valid char, get its start
      offset = bitIndexTable[ch];
      width0 = width = bitIndexTable[ch+1] - offset - diffW;
      isClipped = false;

      if (uf->ubase != null) width = width * height / uf->ubase->fontP.maxHeight;
      
      if ((xMax = x0 + width) > clipX2)
      {
         isClipped = true;
         xMax = clipX2;   
      }
      y1 = y; r=0;
      istart = 0;
      if (!isVert)
      {
         if (y0 < yMin) // guich@tc100b4_1: skip rows before yMin
            istart += yMin-y0;
         y = yMin;
      }
      else
      if (y < yMin)
      {
         r += yMin-y;
         istart += yMin-y; // guich@tc100b4_1: skip rows before yMin
         y = yMin;
      }
      row0 = getGraphicsPixels(g) + y * Graphics_pitch(g);
      rmax = (y+height > yMax) ? yMax - y : height;
      isClipped |= x0 < clipX1 || istart != 0 || rmax != height;

      switch (aaType)
      {
         case AA_NO:
         {
            start     = bitmapTable + (offset >> 3) + rowWIB * istart;
            startBit  = offset & 7;

            // draws the char, a row at a time
   #ifdef __gl2_h_
            if (isGL)
            {
               int32 nn=0;
               
               xya = glXYA;
               for (; r < rmax; start+=rowWIB, r++,row += pitch,y++)    // draw each row
               {
                  current = start;
                  ands = ands8 + (currentBit = startBit);
                  for (x=x0; x < xMax; x++)
                  {
                     if ((*current & *ands++) != 0 && x >= xMin)
                     {
                        *xya++ = (float)x;
                        *xya++ = (float)y;
                        *xya++ = 1;
                        nn++;
                     }
                     if (++currentBit == 8)   // finished this uint8?
                     {
                        currentBit = 0;       // reset counter
                        ands = ands8;         // reset test bit pointer
                        ++current;            // inc current uint8
                     }
                  }
               }
               if (nn > 0) // flush vertices buffer
                  glDrawPixels(nn,foreColor);
            }
            else
   #endif
            for (row=row0; r < rmax; start+=rowWIB, r++,row += pitch)    // draw each row
            {
               current = start;
               ands = ands8 + (currentBit = startBit);
               for (x=x0; x < xMax; x++)
               {
                  if ((*current & *ands++) != 0 && x >= xMin)
                     row[x] = foreColor;
                  if (++currentBit == 8)   // finished this uint8?
                  {
                     currentBit = 0;       // reset counter
                     ands = ands8;         // reset test bit pointer
                     ++current;            // inc current uint8
                  }
               }
            }
            break;
         }
         case AA_4BPP:
         {
            start = bitmapTable + (offset >> 1) + rowWIB * istart;
            isNibbleStartingLow = (offset & 1) == 1;
            // draws the char, a row at a time
   #ifdef __gl2_h_
            if (isGL)
            {
               int32 nn=0;
               
               xya = glXYA;
               for (; r < rmax; start+=rowWIB, r++,y++)    // draw each row
               {
                  current = start;
                  isLowNibble = isNibbleStartingLow;
                  for (x=x0; x < xMax; x++)
                  {
                     transparency = isLowNibble ? (*current++ & 0xF) : ((*current >> 4) & 0xF);
                     isLowNibble = !isLowNibble;
                     if (transparency == 0 || x < xMin)
                        continue;

                     // alpha
                     // vertices
                     *xya++ = (float)x;
                     *xya++ = (float)y;
                     *xya++ = ftransp[transparency];
                     nn++;
                  }
               }
               if (nn > 0) // flush vertices buffer
                  glDrawPixels(nn,foreColor);
            }
            else
   #endif
               for (row=row0; r < rmax; start+=rowWIB, r++,row += pitch)    // draw each row
               {
                  current = start;
                  isLowNibble = isNibbleStartingLow;
                  i = (PixelConv*)&row[x0];
                  for (x=x0; x < xMax; x++,i++)
                  {
                     transparency = isLowNibble ? (*current++ & 0xF) : ((*current >> 4) & 0xF);
                     isLowNibble = !isLowNibble;
                     if (transparency == 0 || x < xMin)
                        continue;
                     if (transparency == 0xF)
                        i->pixel = foreColor;
                     else
                     {
                        i->r = INTERP(i->r, fcR, 4);
                        i->g = INTERP(i->g, fcG, 4);
                        i->b = INTERP(i->b, fcB, 4);
                     }
                  }
               }
         }
         break;
         case AA_8BPP: // textured font files
         {
            // draws the char, a row at a time
   #ifdef __gl2_h_
            if (isGL)
            {       
               if (!isClipped && getCharPosInTexture(currentContext, uf->ubase, ch, charXY))
/*text*/          glDrawTexture(uf->ubase->textureId, 
                               charXY[0], charXY[1], width0, uf->ubase->fontP.maxHeight, // source char position
                               x0, y, width, height,                                     // target bitmap position
                               uf->ubase->maxW, uf->ubase->maxH, &fc, 255);              // total bitmap size
               else
               {                         
                  uint8* alpha = getResizedCharPixels(currentContext, uf->ubase, ch, width+diffW, height);
                  if (alpha)
                  {                             
                     int32 nn=0;
                     rowWIB = width+diffW;
                     start = alpha + istart * rowWIB;
                     xya = glXYA;
                     for (; r < rmax; start+=rowWIB, r++,y++)    // draw each row
                     {
                        current = start;
                        for (x=x0; x < xMax; x++)
                        {
                           transparency = *current++;
                           if (transparency == 0 || x < xMin)
                              continue;
   
                           // alpha
                           // vertices
                           *xya++ = (float)x;
                           *xya++ = (float)y;
                           *xya++ = f255[transparency];
                           nn++;
                        }
                     }
                     if (nn > 0) // flush vertices buffer
                        glDrawPixels(nn,foreColor);
                  }
               }
            }
            else
   #endif // case 2
            {
               uint8* alpha = getResizedCharPixels(currentContext, uf->ubase, ch, width+diffW, height);
               if (alpha)
               {                             
                  rowWIB = width+diffW;
                  start = alpha + istart * rowWIB;
                  for (row=row0; r < rmax; start+=rowWIB, r++,row += pitch)    // draw each row
                  {
                     current = start;
                     i = (PixelConv*)&row[x0];
                     for (x=x0; x < xMax; x++,i++)
                     {
                        transparency = *current++;
                        if (transparency == 0 || x < xMin)
                           continue;
                        if (transparency == 0xFF)
                           i->pixel = foreColor;
                        else
                        {
                           i->r = INTERP(i->r, fcR, 8);
                           i->g = INTERP(i->g, fcG, 8);
                           i->b = INTERP(i->b, fcB, 8);
                        }
                     }
                  }
               }
            }
         }
      }
      if (isVert)
      {
         y = y1 + incY;
         if (y >= yMax)
            break;
      }
      else
      {
         if (xMax >= clipX2)
         {
            xMax = clipX2;
            break;
         }
         x0 = xMax; // next character
         x0 += extraPixelsPerChar;
         if (k <= extraPixelsRemaining)
            x0++;
      }
   }
#ifndef __gl2_h_
   if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, xMin, yMin, (xMax - xMin), (yMax - yMin));
#else
   if (Graphics_isImageSurface(g))
      Image_changed(Graphics_surface(g)) = true;
   else
      currentContext->fullDirty = true;
#endif
}
#else
static void drawText(Context currentContext, TCObject g, JCharP text, int32 chrCount, int32 x, int32 y, Pixel foreColor, int32 justifyWidth)
{
   TCObject fontObj = Graphics_font(g);
   int32 fontSize = (int)(Font_size(fontObj) * (*tcSettings.screenDensityPtr));
   int32 typefaceIndex = Font_skiaIndex(fontObj);

   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_drawText(0, text, chrCount * sizeof(JChar), x, y + fontSize, foreColor | Graphics_alpha(g), justifyWidth, fontSize, typefaceIndex);
   skia_restoreClip();

   markDirty(currentContext, g, x, y, skia_stringWidth(text, chrCount * sizeof(JChar), typefaceIndex, fontSize), fontSize);
}
#endif

static SurfaceType getSurfaceType(Context currentContext, TCObject surface)
{
   // cache class pointers for performance
   return (surface != NULL && areClassesCompatible(currentContext, OBJ_CLASS(surface), "totalcross.ui.image.Image") == 1) == COMPATIBLE ? SURF_IMAGE : SURF_CONTROL;
}

#ifndef SKIA_H
////////////////////////////////////////////////////////////////////////////
static void quadPixel(Context currentContext, TCObject g, int32 xc, int32 yc, int32 x, int32 y, Pixel c)
{
   // draw 4 points using symetry
   setPixel(currentContext, g,xc + x, yc + y, c);
   setPixel(currentContext, g,xc + x, yc - y, c);
   setPixel(currentContext, g,xc - x, yc + y, c);
   setPixel(currentContext, g,xc - x, yc - y, c);
}

static void quadLine(Context currentContext, TCObject g, int32 xc, int32 yc, int32 x, int32 y, Pixel c)
{
   int32 w = x+x+1; // plus 1 for the drawHLine (draws to width-1)
   // draw 2 lines using symetry
   drawHLine(currentContext, g,xc - x, yc - y, w, c, c);
   drawHLine(currentContext, g,xc - x, yc + y, w, c, c);
}

// draws an ellipse incrementally
static void ellipseDrawAndFill(Context currentContext, TCObject g, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, bool fill, bool gradient)
{
   int32 numSteps=0, startRed=0, startGreen=0, startBlue=0, endRed=0, endGreen=0, endBlue=0, redInc=0, greenInc=0, blueInc=0, red=0, green=0, blue=0;
   PixelConv c,c1,c2;
   // intermediate terms to speed up loop
   int64 t1 = (int64)rx*(int64)rx, t2 = t1<<1, t3 = t2<<1;
   int64 t4 = (int64)ry*(int64)ry, t5 = t4<<1, t6 = t5<<1;
   int64 t7 = (int64)rx*t5, t8 = t7<<1, t9 = 0L;
   int64 d1 = t2 - t7 + (t4>>1);    // error terms
   int64 d2 = (t1>>1) - t8 + t5;
   int32 x = rx;      // ellipse points
   int32 y = 0;       // ellipse points
   if (rx < 0 || ry < 0) // guich@501_13
      return;
   c1.pixel = pc1;
   c2.pixel = pc2;

   if (gradient)
   {
      numSteps = ry + ry; // guich@tc110_11: support horizontal gradient
      startRed   = c1.r;
      startGreen = c1.g;
      startBlue = c1.b;
      endRed = c2.r;
      endGreen = c2.g;
      endBlue = c2.b;
      redInc = ((endRed - startRed) << 16) / numSteps;
      greenInc = ((endGreen - startGreen) << 16) / numSteps;
      blueInc = ((endBlue - startBlue) << 16) / numSteps;
      red = startRed << 16;
      green = startGreen << 16;
      blue = startBlue << 16;
   }
   else c.pixel = c1.pixel;

   while (d2 < 0)          // til slope = -1
   {
      if (gradient)
      {
         c.r = (red >> 16) & 0xFF;
         c.g = (green >> 16) & 0xFF;
         c.b = (blue >> 16) & 0xFF;
         red += redInc;
         green += greenInc;
         blue += blueInc;
      }
      if (fill)
         quadLine(currentContext, g,xc,yc,x,y,c.pixel);
      else
         quadPixel(currentContext, g,xc,yc,x,y,c.pixel);
      y++;          // always move up here
      t9 += t3;
      if (d1 < 0)   // move straight up
      {
         d1 += t9 + t2;
         d2 += t9;
      }
      else        // move up and left
      {
         --x;
         t8 -= t6;
         d1 += t9 + t2 - t8;
         d2 += t9 + t5 - t8;
      }
   }

   do             // rest of top right quadrant
   {
      if (gradient)
      {
         c.r = (red >> 16) & 0xFF;
         c.g = (green >> 16) & 0xFF;
         c.b = (blue >> 16) & 0xFF;
         red += redInc;
         green += greenInc;
         blue += blueInc;
      }
      // draw 4 points using symmetry
      if (fill)
         quadLine(currentContext, g,xc,yc,x,y,c.pixel);
      else
         quadPixel(currentContext, g,xc,yc,x,y,c.pixel);
      --x;        // always move left here
      t8 -= t6;
      if (d2 < 0)  // move up and left
      {
         ++y;
         t9 += t3;
         d2 += t9 + t5 - t8;
      }
      else d2 += t5 - t8; // move straight left
   } while (x >= 0);
}
#else
static void ellipseDrawAndFill(Context currentContext, TCObject g, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, bool fill, bool gradient)
{
   xc += Graphics_transX(g);
   yc += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_ellipseDrawAndFill(0, xc, yc, rx, ry, pc1 | Graphics_alpha(g), pc2 | Graphics_alpha(g), fill, gradient);
   skia_restoreClip();

   markDirty(currentContext, g, xc - rx, yc + ry, rx * 2, ry * 2);
}
#endif

#ifndef SKIA_H
////////////////////////////////////////////////////////////////////////////
// Generalized Polygon Fill
static void qsortInts(int32 *items, int32 first, int32 last)
{
   int32 low = first;
   int32 high = last, mid;
   if (first >= last)
      return;
   mid = items[(first+last) >> 1];
   while (true)
   {
      while (high >= low && items[low] < mid) // guich@566_25: added "high > low" here and below - guich@568_5: changed to >=
         low++;
      while (high >= low && items[high] > mid)
         high--;
      if (low <= high)
      {
         int32 temp = items[low];
         items[low++] = items[high];
         items[high--] = temp;
      }
      else break;
   }
   if (first < high)
      qsortInts(items, first,high);
   if (low < last)
      qsortInts(items, low,last);
}

static TCObject growIntArray(Context currentContext, TCObject oldArrayObj, int32 newLen) // must unlock the returned obj
{
   TCObject newArrayObj = createArrayObject(currentContext, INT_ARRAY, newLen);
   int32 *newArray,*oldArray, oldLen;
   if (newArrayObj != null)
   {
      newArray = (int32*)ARRAYOBJ_START(newArrayObj);
      oldArray = (int32*)ARRAYOBJ_START(oldArrayObj);
      oldLen = ARRAYOBJ_LEN(oldArrayObj);
      xmemmove(newArray, oldArray, oldLen * 4);
   }
   return newArrayObj;
}

static void fillPolygon(Context currentContext, TCObject g, int32 *xPoints1, int32 *yPoints1, int32 nPoints1, int32 *xPoints2, int32 *yPoints2, int32 nPoints2, int32 tx, int32 ty, Pixel c1, Pixel c2, bool gradient, bool isPie)
{
   int32 x1, y1, x2, y2,y,n=0,temp, i,j, miny, maxy, a, numSteps=0, startRed=0, startGreen=0, startBlue=0, endRed=0, endGreen=0, endBlue=0, redInc=0, greenInc=0, blueInc=0, red=0, green=0, blue=0;
   int32 *yp;
   int32 *axPoints[2], *ayPoints[2], anPoints[2];
   TCObject *intsObj = &Graphics_ints(g);
   int32 *ints = *intsObj ? (int32*)ARRAYOBJ_START(*intsObj) : null;
   PixelConv c;

   if (!xPoints1 || !yPoints1 || nPoints1 < 2)
      return;

#if defined __gl2_h_ && !defined WP8
   if (!gradient && (nPoints1 == 0 || isConvexAndInsideClip(g, tx, ty, xPoints1, yPoints1, nPoints1, isPie)) && (nPoints2 == 0 || isConvexAndInsideClip(g, tx, ty, xPoints2, yPoints2, nPoints2, isPie)) && Graphics_useOpenGL(g)) // opengl doesnt fills non-convex polygons well
   {
      if (nPoints1 > 0)
         glDrawLines(currentContext, g, xPoints1, yPoints1, nPoints1, tx + Graphics_transX(g), ty + Graphics_transY(g), c1, true);
      if (nPoints2 > 0)
         glDrawLines(currentContext, g, xPoints2, yPoints2, nPoints2, tx + Graphics_transX(g), ty + Graphics_transY(g), c1, true);
      return;
   }
#endif

   axPoints[0] = xPoints1; ayPoints[0] = yPoints1; anPoints[0] = nPoints1;
   axPoints[1] = xPoints2; ayPoints[1] = yPoints2; anPoints[1] = nPoints2;

   yp = yPoints1;
   miny = maxy = *yp++;
   for (i = nPoints1; --i > 0; yp++)
   {
      if (*yp < miny) miny = *yp;
      if (*yp > maxy) maxy = *yp;
   }
   yp = yPoints2;
   for (i = nPoints2; --i >= 0; yp++)
   {
      if (*yp < miny) miny = *yp;
      if (*yp > maxy) maxy = *yp;
   }
   miny += ty;
   maxy += ty;

   if (ints == null)
   {
      *intsObj = createArrayObject(currentContext, INT_ARRAY, 2); // 2 is the most used length
      if (*intsObj == null)
         return;
      setObjectLock(*intsObj, UNLOCKED);
      ints = (int32*)ARRAYOBJ_START(*intsObj);
   }
   if (gradient)
   {
      numSteps = maxy - miny; // guich@tc110_11: support horizontal gradient
      if (numSteps == 0) numSteps = 1; // guich@tc115_86: prevent divide by 0
      c.pixel = c1;
      startRed   = c.r;
      startGreen = c.g;
      startBlue  = c.b;
      c.pixel = c2;
      endRed   = c.r;
      endGreen = c.g;
      endBlue  = c.b;
      redInc = ((endRed - startRed) << 16) / numSteps;
      greenInc = ((endGreen - startGreen) << 16) / numSteps;
      blueInc = ((endBlue - startBlue) << 16) / numSteps;
      red = startRed << 16;
      green = startGreen << 16;
      blue = startBlue << 16;
   }
   else c.pixel = c1;
   for (y = miny; y <= maxy; y++)
   {
      n = 0;
      for (a = 0; a < 2; a++)
      {
         int32 nPoints = anPoints[a];
         int32* xPoints = axPoints[a];
         int32* yPoints = ayPoints[a];
         j = nPoints-1;
         for (i = 0; i < nPoints; j=i,i++)
         {
            y1 = yPoints[j]+ty;
            y2 = yPoints[i]+ty;
            if (y1 == y2)
               continue;
            if (y1 > y2) // invert
            {
               temp = y1;
               y1 = y2;
               y2 = temp;
            }
            // compute next x point
            if ( (y1 <= y && y < y2) || (y == maxy && y1 < y && y <= y2) )
            {
               if (n == (int32)ARRAYOBJ_LEN(*intsObj)) // have to grow the ints array?
               {
                  TCObject newIntsObj = growIntArray(currentContext, *intsObj, n * 2);
                  if (newIntsObj == null)
                     return;
                  *intsObj = newIntsObj;
                  setObjectLock(*intsObj, UNLOCKED);
                  ints = (int32*)ARRAYOBJ_START(*intsObj);
               }
               if (yPoints[j] < yPoints[i])
               {
                  x1 = xPoints[j]+tx;
                  x2 = xPoints[i]+tx;
               }
               else
               {
                  x2 = xPoints[j]+tx;
                  x1 = xPoints[i]+tx;
               }
               ints[n++] = (y - y1) * (x2 - x1) / (y2 - y1) + x1;
            }
         }
      }
      if (n >= 2)
      {
         if (gradient)
         {
            c.r = (red   >> 16) & 0xFF;
            c.g = (green >> 16) & 0xFF;
            c.b = (blue  >> 16) & 0xFF;
            red += redInc;
            green += greenInc;
            blue += blueInc;
         }
         if (n == 2) // most of the times
         {
            if (ints[1] > ints[0])
               drawHLine(currentContext, g,ints[0],y,ints[1]-ints[0],c.pixel,c.pixel);
            else
               drawHLine(currentContext, g,ints[1],y,ints[0]-ints[1],c.pixel,c.pixel);
         }
         else
         {
            qsortInts(ints, 0, n-1);
            for (n>>=1, yp = ints; --n >= 0; yp+=2)
               drawHLine(currentContext, g,yp[0],y,yp[1]-yp[0],c.pixel,c.pixel);
         }
      }
   }
}
#else
static void fillPolygon(Context currentContext, TCObject g, int32 *xPoints1, int32 *yPoints1, int32 nPoints1, int32 *xPoints2, int32 *yPoints2, int32 nPoints2, int32 tx, int32 ty, Pixel c1, Pixel c2, bool gradient, bool isPie)
{
   skia_setClip(Get_Clip(g));
   skia_fillPolygon(0, xPoints1, yPoints1, nPoints1, Graphics_transX(g), Graphics_transY(g), c1 | Graphics_alpha(g), c2 | Graphics_alpha(g), gradient, isPie);
   skia_restoreClip();

   // to avoid computing the polygon's bounds, we mark dirty the current clip
   markDirty(currentContext, g, Graphics_clipX1(g), Graphics_clipY1(g), Graphics_clipX2(g) - Graphics_clipX1(g), Graphics_clipY2(g) - Graphics_clipY1(g));
}
#endif

////////////////////////////////////////////////////////////////////////////
// draws a polygon. if the polygon is not closed, close it
#ifndef SKIA_H
static void drawPolygon(Context currentContext, TCObject g, int32 *xPoints1, int32 *yPoints1, int32 nPoints1, int32 *xPoints2, int32 *yPoints2, int32 nPoints2, int32 tx, int32 ty, Pixel pixel)
{
   if (xPoints1 && yPoints1 && nPoints1 >= 2)
   {
#if defined __gl2_h_ && !defined WP8
      if (Graphics_useOpenGL(g) && (nPoints1 == 0 || isInsideClip(g, tx, ty, xPoints1, yPoints1, nPoints1)) && (nPoints2 == 0 || isInsideClip(g, tx, ty, xPoints2, yPoints2, nPoints2)))
      {
         if (nPoints1 > 0)
            glDrawLines(currentContext, g, xPoints1, yPoints1, nPoints1, tx + Graphics_transX(g), ty + Graphics_transY(g), pixel, false);
         if (nPoints2 > 0)
            glDrawLines(currentContext, g, xPoints2, yPoints2, nPoints2, tx + Graphics_transX(g), ty + Graphics_transY(g), pixel, false);
      } 
      else
#endif
      {
         int32 i;
         for (i=1; i < nPoints1; i++)
            drawLine(currentContext, g,tx + xPoints1[i-1], ty + yPoints1[i-1], tx + xPoints1[i], ty + yPoints1[i], pixel);
         for (i=1; i < nPoints2; i++)
            drawLine(currentContext, g,tx + xPoints2[i-1], ty + yPoints2[i-1], tx + xPoints2[i], ty + yPoints2[i], pixel);
      }
   }
}
#else
static void drawPolygon(Context currentContext, TCObject g, int32 *xPoints1, int32 *yPoints1, int32 nPoints1, int32 *xPoints2, int32 *yPoints2, int32 nPoints2, int32 tx, int32 ty, Pixel pixel)
{
   skia_setClip(Get_Clip(g));
   skia_drawPolygon(0, xPoints1, yPoints1, nPoints1, Graphics_transX(g), Graphics_transY(g), pixel | Graphics_alpha(g));
   skia_restoreClip();

   // to avoid computing the polygon's bounds, we mark dirty the current clip
   markDirty(currentContext, g, Graphics_clipX1(g), Graphics_clipY1(g), Graphics_clipX2(g) - Graphics_clipX1(g), Graphics_clipY2(g) - Graphics_clipY1(g));
}
#endif
////////////////////////////////////////////////////////////////////////////
// draw an elliptical arc from startAngle to endAngle.
// c is the fill color and c2 is the outline color
// (if in fill mode - otherwise, c = outline color)
#ifdef SKIA_H
static void arcPiePointDrawAndFill(Context currentContext, TCObject g, int32 xc, int32 yc, int32 rx, int32 ry, double startAngle, double endAngle, Pixel c, Pixel c2, bool fill, bool pie, bool gradient)
{
   xc += Graphics_transX(g);
   yc += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_arcPiePointDrawAndFill(0, xc, yc, rx, ry, startAngle, endAngle, c | Graphics_alpha(g), c2 | Graphics_alpha(g), fill, pie, gradient);
   skia_restoreClip();

   markDirty(currentContext, g, xc - rx, yc + ry, rx * 2, ry * 2);
}
#else
static void arcPiePointDrawAndFill(Context currentContext, TCObject g, int32 xc, int32 yc, int32 rx, int32 ry, double startAngle, double endAngle, Pixel c, Pixel c2, bool fill, bool pie, bool gradient)
{
   // this algorithm was created by Guilherme Campos Hazan
   double ppd;
   int32 startIndex,endIndex,index,i,nq,size=0,oldX1=0,oldY1=0,last,oldX2=0,oldY2=0;
   bool sameR,startSetTo0 = true;
   TCObject *xPointsObj = &Graphics_xPoints(g);
   TCObject *yPointsObj = &Graphics_yPoints(g);
   int32 *xPoints = *xPointsObj ? (int32*)ARRAYOBJ_START(*xPointsObj) : null;
   int32 *yPoints = *yPointsObj ? (int32*)ARRAYOBJ_START(*yPointsObj) : null;
   int32 clipFactor = Graphics_minX(g) * 1000000000 + Graphics_maxX(g) * 10000000 + Graphics_minY(g) * 100000 + Graphics_maxY(g);
   bool sameClipFactor = Graphics_lastClipFactor(g) == clipFactor;

   if (rx < 0 || ry < 0) // guich@501_13
      return;
   // make sure the values are -359 <= x <= 359
   while (startAngle <= -360) startAngle += 360;
   while (endAngle   <= -360) endAngle   += 360;
   while (startAngle >   360) startAngle -= 360;
   while (endAngle   >   360) endAngle   -= 360;

   if (startAngle == endAngle) // guich@501_13
      return;
   if (startAngle > endAngle) // eg 235 to 45
      startAngle -= 360; // set to -45 to 45 so we can handle it correctly
   if (startAngle >= 0 && endAngle <= 0) // eg 135 to -135
      endAngle += 360; // set to 135 to 225

   // step 0: correct angle values
   if (startAngle < 0.1 && endAngle > 359.9) // full circle? use the fastest routine instead
   {
      if (fill)
         ellipseDrawAndFill(currentContext, g,xc, yc, rx, ry, c, c2, true, gradient);
      ellipseDrawAndFill(currentContext, g,xc, yc, rx, ry, c, c, false, gradient);
      return;
   }

   // step 0: if possible, use cached results
   sameR = rx == Graphics_lastRX(g) && ry == Graphics_lastRY(g);
   if (!sameClipFactor || !sameR)
   {
      // step 1: computes how many points the circle has (computes only 45 degrees and mirrors the rest)
      // intermediate terms to speed up loop
      int64 t1 = (int64)rx*(int64)rx, t2 = t1<<1, t3 = t2<<1;
      int64 t4 = (int64)ry*(int64)ry, t5 = t4<<1, t6 = t5<<1;
      int64 t7 = (int64)rx*t5, t8 = t7<<1, t9 = 0L;
      int64 d1 = t2 - t7 + (t4>>1);    // error terms
      int64 d2 = (t1>>1) - t8 + t5;
      int32 x = rx;                 // ellipse points
      int32 y = 0;                  // ellipse points

      while (d2 < 0)              // til slope = -1
      {
         t9 += t3;
         if (d1 < 0)             // move straight up
         {
            d1 += t9 + t2;
            d2 += t9;
         }
         else                   // move up and left
         {
            --x;
            t8 -= t6;
            d1 += t9 + t2 - t8;
            d2 += t9 + t5 - t8;
         }
         ++size;
      }

      do             // rest of top right quadrant
      {
         --x;         // always move left here
         t8 -= t6;
         if (d2 < 0)  // move up and left
         {
            t9 += t3;
            d2 += t9 + t5 - t8;
         }
         else d2 += t5 - t8;  // move straight left
         ++size;
      } while (x >= 0);
      nq = size;
      size *= 4;
      // step 2: computes how many points per degree
      ppd = (double)size / 360.0f;
      // step 3: create space in the buffer so it can save all the circle
      size+=2;
      if (pie) size++;
      if (xPoints == null || ARRAYOBJ_LEN(*xPointsObj) != (uint32)size) // guich@tc304: changed < to != to fix a glytch when drawing two pies with different radius
      {
         *xPointsObj = createArrayObject(currentContext, INT_ARRAY, max32(3,size));
         if (*xPointsObj == null)
            return;
         *yPointsObj = createArrayObject(currentContext, INT_ARRAY, max32(3,size));
         if (*yPointsObj == null)
         {
            setObjectLock(*xPointsObj, UNLOCKED);
            return;
         }
         setObjectLock(*xPointsObj, UNLOCKED);
         setObjectLock(*yPointsObj, UNLOCKED);
      }
      xPoints = (int32*)ARRAYOBJ_START(*xPointsObj);
      yPoints = (int32*)ARRAYOBJ_START(*yPointsObj);
      if (pie) {xPoints++; yPoints++;} // make sure that startIndex-1 is at a valid pointer

      // step 4: stores all the circle in the array. the odd arcs are drawn in reverse order
      // intermediate terms to speed up loop
      t2 = t1<<1;
      t3 = t2<<1;
      t8 = t7<<1;
      t9 = 0;
      d1 = t2 - t7 + (t4>>1); // error terms
      d2 = (t1>>1) - t8 + t5;
      x = rx;
      i=0;
      while (d2 < 0)          // til slope = -1
      {
         // save 4 points using symmetry
         index = nq*0+i;      // 0/3
         xPoints[index]=+x;
         yPoints[index]=-y;

         index = (nq<<1)-i-1;    // 1/3
         xPoints[index]=-x;
         yPoints[index]=-y;

         index = (nq<<1)+i;      // 2/3
         xPoints[index]=-x;
         yPoints[index]=+y;

         index = (nq<<2)-i-1;    // 3/3
         xPoints[index]=+x;
         yPoints[index]=+y;
         i++;
         y++;        // always move up here
         t9 += t3;
         if (d1 < 0)  // move straight up
         {
             d1 += t9 + t2;
             d2 += t9;
         }
         else      // move up and left
         {
             x--;
             t8 -= t6;
             d1 += t9 + t2 - t8;
             d2 += t9 + t5 - t8;
         }
      }

      do             // rest of top right quadrant
      {
         // save 4 points using symmetry
         index = nq*0+i;    // 0/3
         xPoints[index]=+x;
         yPoints[index]=-y;

         index = (nq<<1)-i-1;  // 1/3
         xPoints[index]=-x;
         yPoints[index]=-y;

         index = (nq<<1)+i;    // 2/3
         xPoints[index]=-x;
         yPoints[index]=+y;

         index = (nq<<2)-i-1;  // 3/3
         xPoints[index]=+x;
         yPoints[index]=+y;

         ++i;
         --x;        // always move left here
         t8 -= t6;
         if (d2 < 0)  // move up and left
         {
            ++y;
            t9 += t3;
            d2 += t9 + t5 - t8;
         }
         else d2 += t5 - t8;   // move straight left
      } while (x >= 0);
      // save last arguments
      //Graphics_lastXC(g)   = xc; no longer
      //Graphics_lastYC(g)   = yc;  needed
      Graphics_lastRX(g)   = rx;
      Graphics_lastRY(g)   = ry;
      Graphics_lastPPD(g)  = ppd;
      Graphics_lastSize(g) = size;
      Graphics_lastClipFactor(g) = clipFactor;
   }
   else
   {
      size = Graphics_lastSize(g);
      ppd = Graphics_lastPPD(g);
   }
   // step 5: computes the start and end indexes that will become part of the arc
   if (startAngle < 0)
      startAngle += 360;
   if (endAngle < 0)
      endAngle += 360;
   startIndex = (int32)(ppd * startAngle);
   endIndex = (int32)(ppd * endAngle);

   last = size-2;
   if (endIndex >= last) // 360?
      endIndex--;
   // step 6: fill or draw the polygons
   endIndex++;
   if (pie)
   {
      // connect two lines from the center to the two edges of the arc
      oldX1 = xPoints[endIndex];
      oldY1 = yPoints[endIndex];
      xPoints[endIndex] = yPoints[endIndex] = 0;
      if (xPoints[startIndex] == 0 && yPoints[startIndex] == 0)
         startSetTo0 = false;
      else
      {
         startIndex--;
         oldX2 = xPoints[startIndex];
         oldY2 = yPoints[startIndex];
         xPoints[startIndex] = yPoints[startIndex] = 0;
      }
      endIndex++;
   }
  
   if (startIndex > endIndex) // drawing from angle -30 to +30 ? (startIndex = 781, endIndex = 73, size=854)
   {
      int p1 = last-startIndex;
      if (fill)
         fillPolygon(currentContext, g, xPoints+startIndex, yPoints+startIndex, p1, xPoints, yPoints, endIndex, xc,yc, gradient ? c : c2, c2, gradient, true); // lower half, upper half
      if (!gradient) drawPolygon(currentContext, g, xPoints+startIndex, yPoints+startIndex, p1-1, xPoints+1, yPoints+1, endIndex-1, xc,yc, c);
   }
   else
   {
      int32 arc = pie ? 0 : 1;
      if (fill)
         fillPolygon(currentContext, g, xPoints+startIndex, yPoints+startIndex, endIndex-startIndex, 0,0,0, xc,yc, gradient ? c : c2, c2, gradient, true);   
      if (!gradient) drawPolygon(currentContext, g, xPoints+startIndex+arc, yPoints+startIndex+arc, endIndex-startIndex-arc, 0,0,0, xc,yc, c);
   }
   if (pie)  // restore saved points
   {
      if (startSetTo0)
      {
         xPoints[startIndex] = oldX2;
         yPoints[startIndex] = oldY2;
      }
      endIndex--;
      xPoints[endIndex]   = oldX1;
      yPoints[endIndex]   = oldY1;
#ifdef ANDROID
      if (!gradient && endAngle == 360) 
         drawLine(currentContext,g, xc,yc, xc+xPoints[endIndex-1], yc+yPoints[endIndex-1], c);
#endif         
   }
}
#endif
////////////////////////////////////////////////////////////////////////////
#ifndef SKIA_H
static void drawRoundRect(Context currentContext, TCObject g, int32 x, int32 y, int32 width, int32 height, int32 r, Pixel c)
{
   int32 x1, y1, x2, y2, dec, xx, yy;
   int32 w, h;
   r = min32(r,min32(width/2,height/2));
   w = width - 2*r;
   h = height - 2*r;
   x1 = x+r;
   y1 = y+r;
   x2 = x+width-r-1;
   y2 = y+height-r-1;
   dec = 3-2*r;

   drawHLine(currentContext, g,x+r, y, w, c, c); // top
   drawHLine(currentContext, g,x+r, y+height-1, w, c, c); // bottom
   drawVLine(currentContext, g,x, y+r, h, c, c); // left
   drawVLine(currentContext, g,x+width-1, y+r, h, c, c); // right

   // draw the round rectangles.
   for (xx = 0, yy = r; xx <= yy; xx++)
   {
      setPixel(currentContext, g,x2+xx, y2+yy, c);
      setPixel(currentContext, g,x2+xx, y1-yy, c);
      setPixel(currentContext, g,x1-xx, y2+yy, c);
      setPixel(currentContext, g,x1-xx, y1-yy, c);

      setPixel(currentContext, g,x2+yy, y2+xx, c);
      setPixel(currentContext, g,x2+yy, y1-xx, c);
      setPixel(currentContext, g,x1-yy, y2+xx, c);
      setPixel(currentContext, g,x1-yy, y1-xx, c);
      if (dec >= 0)
         dec += -4*(yy--)+4;
      dec += 4*xx+6;
   }
}
#else
static void drawRoundRect(Context currentContext, TCObject g, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_drawRoundRect(0, x, y, w, h, r, c | Graphics_alpha(g));
   skia_restoreClip();

   markDirty(currentContext, g, x, y, w, h);
}
#endif

#ifndef SKIA_H
////////////////////////////////////////////////////////////////////////////
static void setPixelA(Context currentContext, TCObject g, int32 x, int32 y, PixelConv color, int32 alpha);

////////////////////////////////////////////////////////////////////////////
static void fillRoundRect(Context currentContext, TCObject g, int32 xx, int32 yy, int32 width, int32 height, int32 r, Pixel c)
{
   int32 px1,px2,py1,py2,xm,ym,x,y=0, i, x2, e2, err;
   PixelConv color;
   if (r > (width/2) || r > (height/2)) r = min32(width/2,height/2); // guich@200b4_6: correct bug that crashed the device.

   x = -r;
   err = 2 - 2 * r;
   color.pixel = c;

   px1 = xx+r;
   py1 = yy+r;
   px2 = xx+width-r-1;
   py2 = yy+height-r-1;

   height -= 2*r;
   yy += r;
   while (height--)
      drawHLine(currentContext, g,xx, yy++, width, c, c);

   r = 1 - err;
   do
   {
      i = 255 - 255 * abs(err - 2 * (x + y) - 2) / r;

      drawLine(currentContext, g, px1+x+1,py1-y,px2-x-1,py1-y,c);
      drawLine(currentContext, g, px1+x+1,py2+y,px2-x-1,py2+y,c);

      if (i < 256 && i > 0)
      {
         xm = px2; ym = py2; setPixelA(currentContext, g, xm - x, ym + y, color, i); // br
         xm = px1; ym = py2; setPixelA(currentContext, g, xm - y, ym - x, color, i); // bl
         xm = px1; ym = py1; setPixelA(currentContext, g, xm + x, ym - y, color, i); // tl
         xm = px2; ym = py1; setPixelA(currentContext, g, xm + y, ym + x, color, i); // tr
      }
      e2 = err;
      x2 = x;
      if (err + y > 0)
      {
         i = 255 - 255 * (err - 2 * x - 1) / r;
         if (i < 256 && i > 0)
         {
            xm = px2; ym = py2; setPixelA(currentContext, g, xm - x, ym + y + 1, color, i);
            xm = px1; ym = py2; setPixelA(currentContext, g, xm - y - 1, ym - x, color, i);
            xm = px1; ym = py1; setPixelA(currentContext, g, xm + x, ym - y - 1, color, i);
            xm = px2; ym = py1; setPixelA(currentContext, g, xm + y + 1, ym + x, color, i);
         }
         err += ++x * 2 + 1;
      }
      if (e2 + x2 <= 0)
      {
         i = 255 - 255 * (2 * y + 3 - e2) / r;
         if (i < 256 && i > 0)
         {
            xm = px2; ym = py2; setPixelA(currentContext, g, xm - x2 - 1, ym + y, color, i);
            xm = px1; ym = py2; setPixelA(currentContext, g, xm - y, ym - x2 - 1, color, i);
            xm = px1; ym = py1; setPixelA(currentContext, g, xm + x2 + 1, ym - y, color, i);
            xm = px2; ym = py1; setPixelA(currentContext, g, xm + y, ym + x2 + 1, color, i);
         }
         err += ++y * 2 + 1;
      }
   } while (x < 0);
}
#else
static void fillRoundRect(Context currentContext, TCObject g, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c)
{
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   skia_setClip(Get_Clip(g));
   skia_fillRoundRect(0, x, y, w, h, r, c | Graphics_alpha(g));
   skia_restoreClip();

   markDirty(currentContext, g, x, y, w, h);
}
#endif

#if 1//ndef SKIA_H
// Translates the given coords and returns the intersection between
// the clip rect and the coords passed.
// Returns: 1 if OK, 0 if the coords are outside the clip rect
static bool translateAndClip(TCObject g, int32 *pX, int32 *pY, int32 *pWidth, int32 *pHeight)
{
   int32 x = *pX;
   int32 y = *pY;
   int32 w = *pWidth;
   int32 h = *pHeight;
   x += Graphics_transX(g);
   y += Graphics_transY(g);
   if (x < Graphics_clipX1(g))
   {
      if ((x+w) > Graphics_clipX2(g))
         w = Graphics_clipX2(g) - Graphics_clipX1(g);
      else
         w -= Graphics_clipX1(g)-x;
      x = Graphics_clipX1(g);
   }
   else
   if ((x+w) > Graphics_clipX2(g))
      w = Graphics_clipX2(g) - x;
   if (y < Graphics_clipY1(g))
   {
      if ((y+h) > Graphics_clipY2(g))
         h = Graphics_clipY2(g) - Graphics_clipY1(g);
      else
         h -= Graphics_clipY1(g)-y;
      y = Graphics_clipY1(g);
   }
   else
   if ((y+h) > Graphics_clipY2(g))
      h = Graphics_clipY2(g) - y;

   if (x < 0 || y < 0 || h <= 0 || w <= 0) return false; // guich@566_42: check the resulting w/h - guich@tc112_34: check also x and y

   *pX      = x;
   *pY      = y;
   *pWidth  = w;
   *pHeight = h;
   return true;
}
#endif

static void createGfxSurface(int32 w, int32 h, TCObject g, SurfaceType stype)
{
   Graphics_clipX2(g) = Graphics_width (g) = w;
   Graphics_clipY2(g) = Graphics_height(g) = h;
   if (stype == SURF_IMAGE)
      Graphics_pitch(g) = w;
   else
      Graphics_pitch(g) = screen.screenW;

    Graphics_alpha(g) = 0xFF000000;
}

int32 *shiftYfield, *shiftHfield, *lastShiftYfield, *needsPaint, lastShiftY, screenY = 0;
#ifndef SKIA_H
#define BITMAP_PTR(p, dline, pitch)      (((uint8*)p) + (dline * pitch))
#define IS_PITCH_OPTIMAL(w, pitch, bpp)  (((uint32)w * (uint32)bpp / 8) == (uint32)pitch) // 240 * 32 / 8 == 960 ?

#ifndef __gl2_h_
static bool firstUpdate = true;
#endif
#endif

static int32 oldShiftY=9000000;
#ifdef darwin
static int32 lastAppHeightOnSipOpen;
extern int keyboardH,realAppH;
extern bool setShiftYonNextUpdateScreen;

static void checkKeyboardAndSIP(Context currentContext, int32 *shiftY, int32 *shiftH)
{
   int32 appHeightOnSipOpen = screen.screenH - keyboardH;
/*
   if (appHeightOnSipOpen != lastAppHeightOnSipOpen)
   {
      lastAppHeightOnSipOpen = appHeightOnSipOpen;
      markWholeScreenDirty(currentContext);
   }
*/
   if ((*shiftY + *shiftH) > screen.screenH)
      *shiftH = screen.screenH - *shiftY;

   if ((*shiftY + *shiftH) < appHeightOnSipOpen) // don't shift the screen if above
      *shiftY = 0;
   else
   {
      int32 diffBetweenShiftAndAppHOSOpen = appHeightOnSipOpen - (*shiftY - (screen.screenH - *shiftY)); // difference between shift and app height on sip open.
      *shiftY += diffBetweenShiftAndAppHOSOpen - *shiftH; // add remaining space between sip and component
   }
/*
   if (oldShiftY != *shiftY)
   {
      oldShiftY = *shiftY;
      setShiftYonNextUpdateScreen = true;
   }
*/
}
#elif defined(ANDROID)
extern int realAppH;
extern bool setShiftYonNextUpdateScreen;
static int32 lastAppHeightOnSipOpen;
static int desiredShiftY=-1;
static void checkKeyboardAndSIP(Context currentContext, int32 *shiftY, int32 *shiftH)
{
   JNIEnv *env = getJNIEnv();
   if (env == null) return;

   if ((*env)->GetStaticBooleanField(env, applicationClass, jhardwareKeyboardIsVisible))
      *shiftY = *shiftH = 0;
   else
   {
      bool sipVisible = (*env)->GetStaticBooleanField(env, applicationClass, jsipVisible);
      // we know the height if:
      // 1. Portrait and Android 2.x
      // 2. Portrait/landscape and Android 3.x
      if (sipVisible && (screen.screenH > screen.screenW || *tcSettings.romVersionPtr >= 11))
      {
         int32 appHeightOnSipOpen = screen.screenH - (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
         int32 appTitleH = (*env)->GetStaticIntField(env, applicationClass, jappTitleH);
         // when application is in full screen, this function would erase a possibly valid shiftY value;
         // so, here i store the desired shiftY and restore it when the application is really not full screen
         bool isFullScreen = appTitleH != 0 && (appTitleH+appHeightOnSipOpen) == screen.screenH;
         if (!isFullScreen && desiredShiftY != -1)
         {
            *shiftY = desiredShiftY;
            desiredShiftY = -1;
         }
         else
         if (isFullScreen && desiredShiftY == -1)
            desiredShiftY = *shiftY;
         
            if (*shiftY < appHeightOnSipOpen) {// don't shift the screen if above
                *shiftY = - appHeightOnSipOpen + screen.screenH;
            }
            else {
                *shiftY += *shiftH;
            }
        }
    }
}
#endif

#define UNDEFINED_SHIFTY -999999
int32 desiredScreenShiftY=UNDEFINED_SHIFTY;
void setShiftYgl(int32 shiftY);

#ifndef SKIA_H
// not used with opengl
static bool updateScreenBits(Context currentContext) // copy the 888 pixels to the native format
{
   int32 screenW, screenH, shiftY = 0, shiftH = 0;
   TCClass window;
   PixelConv gray;
#ifndef __gl2_h_
   int32 y, count;
#endif

   gray.pixel = *shiftScreenColorP;

#ifndef __gl2_h_
   if (screen.mainWindowPixels == null || ARRAYOBJ_LEN(screen.mainWindowPixels) < (uint32)(screen.screenW * screen.screenH))
      return false;

   if (!graphicsLock(&screen, true))
   {
      if (firstUpdate)
         throwException(currentContext, RuntimeException, "Cannot lock screen");
      return false;
   }
   firstUpdate = false;
#endif

   if (shiftYfield == null && (window = loadClass(currentContext, "totalcross.ui.Window", false)) != null)
   {              
      needsPaint = getStaticFieldInt(window, "needsPaint");
      shiftYfield = getStaticFieldInt(window, "shiftY");
      shiftHfield = getStaticFieldInt(window, "shiftH");
      lastShiftYfield = getStaticFieldInt(window, "lastShiftY");
      if (shiftYfield == null)
         return false;
   }

   shiftY = *shiftYfield;
   shiftH = *shiftHfield;

#ifdef WINCE
   if (!isWindowsMobile) {
	   shiftY = 0;
   }
#elif defined ANDROID || defined darwin
   checkKeyboardAndSIP(currentContext, &shiftY,&shiftH);
#ifdef ANDROID
   if (*shiftYfield != shiftY && lastAppHeightOnSipOpen != screen.screenH)
#else
   if (*shiftYfield != shiftY && lastAppHeightOnSipOpen != realAppH)
#endif
   {
      *lastShiftYfield = *shiftYfield = shiftY;
      *shiftHfield = shiftH;
   }
#endif
   screenW = screen.screenW;
   screenH = screen.screenH;

   if ((shiftY+shiftH) > screen.screenH)
      shiftH = screen.screenH - shiftY;
   if (shiftY != 0 && shiftH <= 0)
      return false;

   if (!currentContext->fullDirty && shiftY != 0) // *1* clip dirty Y values to screen shift area
   {
      if (shiftY != lastShiftY) // the first time a shift is made, we must paint everything, to let the gray part be painted
      {
         lastShiftY = shiftY;
         markWholeScreenDirty(currentContext);
      }
      else
      {
         if (currentContext->dirtyY1 <   shiftY)         currentContext->dirtyY1 = shiftY;
         if (currentContext->dirtyY2 >= (shiftY+shiftH)) currentContext->dirtyY2 = shiftY+shiftH;
         currentContext->dirtyY1 -= shiftY;
         currentContext->dirtyY2 = currentContext->dirtyY1 + min32(currentContext->dirtyY2-(currentContext->dirtyY1+shiftY), shiftH);
      }
   }
   
#ifdef __gl2_h_
   desiredScreenShiftY = shiftY; // will be set with glScreenShiftY in updateScreen
#else
   screen.shiftY = shiftY;
   // screen bytes must be aligned to a 4-byte boundary, but screen.g bytes don't
   if (screen.bpp == 16)
   {
      Pixel565 grayp = SETPIXEL565(gray.r,gray.g,gray.b);
      if (currentContext->fullDirty && IS_PITCH_OPTIMAL(screenW, screen.pitch, screen.bpp)) // fairly common: the MainWindow is often fully repainted, and Palm OS and Windows always have pitch=width
      {
         PixelConv *f = (PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels);
         Pixel565 *t = (Pixel565*)screen.pixels;
         if (shiftY == 0)
            for (count = screenH * screenW; count != 0; f++,count--)
               #if defined(WIN32) && !defined(WP8)
               SETPIXEL565_(t, f->pixel)
               #else
               *t++ = (Pixel565)SETPIXEL565(f->r, f->g, f->b);
               #endif
         else
         {
            for (count = shiftH * screenW, f += shiftY * screenW; count != 0; f++,count--)
               #if defined(WIN32) && !defined(WP8)
               SETPIXEL565_(t, f->pixel)
               #else
               *t++ = (Pixel565)SETPIXEL565(f->r, f->g, f->b);
               #endif
            if (screenH > shiftH)
               for (count = (screenH-shiftH)*screenW; count != 0; f++,count--)
                  *t++ = grayp;
         }
      }
      else
      {
         PixelConv *f = ((PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels)) + (currentContext->dirtyY1+shiftY) * screenW + currentContext->dirtyX1, *rowf, *pf;
         Pixel565 *t = ((Pixel565*)BITMAP_PTR(screen.pixels, currentContext->dirtyY1, screen.pitch)) + currentContext->dirtyX1, *rowt, *pt;
         for (pf=rowf=f, pt=rowt=t, y = currentContext->dirtyY1; y < currentContext->dirtyY2; y++, pt = (rowt = (Pixel565*)(((uint8*)rowt) + screen.pitch)), pf = (rowf += screenW))
            if (shiftY != 0 && y >= shiftH)
            {
               if (currentContext->fullDirty) // draw gray area only if first time (full dirty, set above *1*)
                  for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; count--)
                     *pt++ = grayp;
            }
            else
            {
               for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; pf++, count--)
                  #if defined(WIN32) && !defined(WP8)
                  SETPIXEL565_(pt, pf->pixel)
                  #else
                  *pt++ = (Pixel565)SETPIXEL565(pf->r, pf->g, pf->b);
                  #endif
            }
      }
   }
   else
   if (screen.bpp == 8)
   {
      uint32 r,g,b;
      uint8* toR = lookupR;
      uint8* toG = lookupG;
      uint8* toB = lookupB;
      uint8* toGray = lookupGray;
      PixelPal grayp = toGray[gray.r];
      if (currentContext->fullDirty && IS_PITCH_OPTIMAL(screenW, screen.pitch, screen.bpp)) // fairly common: the MainWindow is often fully repainted, and Palm OS and Windows always have pitch=width
      {
         PixelConv *f = (PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels);
         PixelPal *t = (PixelPal*)screen.pixels;
         if (shiftY == 0)
            for (count = screenH * screenW; count != 0; f++, count--)
            {
               r = f->r; g = f->g; b = f->b;
               *t++ = (PixelPal)((g == r && g == b) ? toGray[r] : (toR[r] + toG[g] + toB[b]));
            }
         else
         {
            PixelPal grayp = toGray[gray.r];
            for (count = shiftH * screenW, f += shiftY * screenW; count != 0; f++, count--)
            {
               r = f->r; g = f->g; b = f->b;
               *t++ = (PixelPal)((g == r && g == b) ? toGray[r] : (toR[r] + toG[g] + toB[b]));
            }
            if (screenH > shiftH)
               for (count = (screenH-shiftH)*screenW; count != 0; f++, count--)
                  *t++ = grayp;
         }
      }
      else
      {
         PixelConv *f = ((PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels)) + (currentContext->dirtyY1+shiftY) * screenW + currentContext->dirtyX1, *rowf, *pf;
         PixelPal *t = ((PixelPal*)BITMAP_PTR(screen.pixels, currentContext->dirtyY1, screen.pitch)) + currentContext->dirtyX1, *rowt, *pt;
         for (pf=rowf=f, pt=rowt=t, y = currentContext->dirtyY1; y < currentContext->dirtyY2; y++, pt = (rowt = (PixelPal*)(((uint8*)rowt) + screen.pitch)), pf = (rowf += screenW))
            if (shiftY != 0 && y >= shiftH)
            {
               if (currentContext->fullDirty) // draw gray area only if first time (full dirty, set above *1*)
                  for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; count--)
                     *pt++ = grayp;
            }
            else
            {
               for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; pf++, count--)
               {
                  r = pf->r; g = pf->g; b = pf->b;
                  *pt++ = (PixelPal)((g == r && g == b) ? toGray[r] : (toR[r] + toG[g] + toB[b]));
               }
            }
      }
   }
   else
   if (screen.bpp == 32)
   {
#ifdef WIN32
      #define SETPIXEL_32(p) (((p)->pixel) >> 8)
#else
      #define SETPIXEL_32(p) SETPIXEL32((p)->r, (p)->g, (p)->b)
#endif

      Pixel32 grayp = SETPIXEL_32(&gray);
      if (currentContext->fullDirty && IS_PITCH_OPTIMAL(screenW, screen.pitch, screen.bpp)) // fairly common: the MainWindow is often fully repainted, and Palm OS and Windows always have pitch=width
      {
         PixelConv *f = (PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels);
         Pixel32 *t = (Pixel32*)screen.pixels;
         if (shiftY == 0)
            for (count = screenH * screenW; count != 0; f++, count--)
               *t++ = SETPIXEL_32(f);
         else
         {
            for (count = shiftH * screenW, f += shiftY * screenW; count != 0; f++,count--)
               *t++ = SETPIXEL_32(f);
            if (screenH > shiftH)
               for (count = (screenH-shiftH)*screenW; count != 0; f++, count--)
                  *t++ = grayp;
         }
      }
      else
      {
         PixelConv *f = ((PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels)) + (currentContext->dirtyY1+shiftY) * screenW + currentContext->dirtyX1, *rowf, *pf=f;
         Pixel32 *t = ((Pixel32*)BITMAP_PTR(screen.pixels, currentContext->dirtyY1, screen.pitch)) + currentContext->dirtyX1, *rowt, *pt=t;
         for (pf=rowf=f, pt=rowt=t, y = currentContext->dirtyY1; y < currentContext->dirtyY2; y++, pt = (rowt = (Pixel32*)(((uint8*)rowt) + screen.pitch)), pf = (rowf += screenW))
            if (shiftY != 0 && y >= shiftH)
            {
               if (currentContext->fullDirty) // draw gray area only if first time (full dirty, set above *1*)
                  for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; count--)
                     *pt++ = grayp;
            }
            else
            {
               for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; pf++, count--)
                  *pt++ = SETPIXEL_32(pf);
            }
      }
   }
   else
   if (screen.bpp == 24)
   {
      if (currentContext->fullDirty && IS_PITCH_OPTIMAL(screenW, screen.pitch, screen.bpp)) // fairly common: the MainWindow is often fully repainted, and Palm OS and Windows always have pitch=width
      {
         PixelConv *f = (PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels);
         Pixel24 *t = (Pixel24*)screen.pixels;
         if (shiftY == 0)
            for (count = screenH * screenW; count != 0; f++, t++, count--)
               SETPIXEL24(t,f)
         else
         {
            Pixel24 grayp;
            SETPIXEL24((&grayp),(&gray));
            for (count = shiftH * screenW, f += shiftY * screenW; count != 0; f++, t++, count--)
               SETPIXEL24(t,f)
            if (screenH > shiftH)
               for (count = (screenH-shiftH)*screenW; count != 0; count--)
                  *t++ = grayp;
         }
      }
      else
      {
         PixelConv *f = ((PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels)) + (currentContext->dirtyY1+shiftY) * screenW + currentContext->dirtyX1, *rowf, *pf=f;
         Pixel24 *t = ((Pixel24*)BITMAP_PTR(screen.pixels, currentContext->dirtyY1, screen.pitch)) + currentContext->dirtyX1, *rowt, *pt=t;
         for (pf=rowf=f, pt=rowt=t, y = currentContext->dirtyY1; y < currentContext->dirtyY2; y++, pt = (rowt = (Pixel24*)(((uint8*)rowt) + screen.pitch)), pf = (rowf += screenW))
            for (count = currentContext->dirtyX2 - currentContext->dirtyX1; count != 0; pf++,pt++, count--)
               SETPIXEL24(pt, pf);
      }

   }
#endif
   graphicsLock(&screen, false);
   return true;
}
#else
static bool updateScreenBits(Context currentContext) // copy the 888 pixels to the native format
{
   int32 shiftY = 0, shiftH = 0, lastShiftY = 0;
   TCClass window;
   
    if (shiftYfield == null && (window = loadClass(currentContext, "totalcross.ui.Window", false)) != null)
    {
        needsPaint = getStaticFieldInt(window, "needsPaint");
        shiftYfield = getStaticFieldInt(window, "shiftY");
        shiftHfield = getStaticFieldInt(window, "shiftH");
        lastShiftYfield = getStaticFieldInt(window, "lastShiftY");
        if (shiftYfield == null)
            return false;
    }
    
   return true;
}
#endif

static bool createColorPaletteLookupTables()
{
   uint32 i, r,g,b;
   lookupR = (uint8*)xmalloc(256);
   lookupG = (uint8*)xmalloc(256);
   lookupB = (uint8*)xmalloc(256);
   lookupGray = (uint8*)xmalloc(256);
   if (!lookupR || !lookupG || !lookupB || !lookupGray)
   {
      xfree(lookupR);
      xfree(lookupG);
      xfree(lookupB);
      xfree(lookupGray);
      return false;
   }

   for (i = 0; i < 256; i++)
   {
      r = (i+1) * 6 / 256; if (r > 0) r--;
      g = (i+1) * 8 / 256; if (g > 0) g--;
      b = (i+1) * 5 / 256; if (b > 0) b--;
      lookupR[i] = (uint8)(r*40);
      lookupG[i] = (uint8)(g*5);
      lookupB[i] = (uint8)(b+16);
      lookupGray[i] = (uint8)(i / 0x11);
   }
   return true;
}

#ifndef SKIA_H
static void fillWith8bppPalette(uint32* ptr)
{
   uint32 R = 6, G = 8, B = 5,r,g,b,rr,gg,bb,k,kk;
   // gray values  0-15
   for (k =0; k <= 15; k++)
   {
      kk = k * 0x11;
      *ptr++ = SETPIXEL32(kk,kk,kk);
   }
   // color values  16 - 255
   for (r = 1; r <= R; r++)
   {
      rr = r * 256/R; if (rr > 255) rr = 255;
      for (g = 1; g <= G; g++)
      {
         gg = g * 256/G; if (gg > 255) gg = 255;
         for (b = 1; b <= B; b++)
         {
            bb = b * 256/B; if (bb > 255) bb = 255;
            *ptr++ = SETPIXEL32(rr, gg, bb);
         }
      }
   }
}
#endif

#ifndef SKIA_H
static int getOffset(int radius, int y)
{
   return radius - (int32)sqrt((double)radius * radius - y * y);
}

static void drawFadedPixel(Context currentContext, TCObject g, int32 xx, int32 yy, int32 c) // guich@tc124_4
{
#ifdef __gl2_h_
   if (Graphics_useOpenGL(g))
      glDrawPixelG(g,xx,yy,c,20*255/100);
   else
#endif
   {
   PixelConv c1,c2;
   c1.pixel = c;
   c2 = getPixelConv(g, xx, yy);
   setPixel(currentContext, g, xx, yy, interpolate(c1, c2, 20*255/100));
   }
}

static void drawRoundGradient(Context currentContext, TCObject g, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, bool vertical)
{
   int32 numSteps = max32(1, vertical ? abs32(endY - startY) : abs32(endX - startX)); // guich@tc110_11: support horizontal gradient - guich@gc114_41: prevent div by 0 if numsteps is 0
   int32 startRed = (startColor >> 16) & 0xFF;
   int32 startGreen = (startColor >> 8) & 0xFF;
   int32 startBlue = startColor & 0xFF;
   int32 endRed = (endColor >> 16) & 0xFF;
   int32 endGreen = (endColor >> 8) & 0xFF;
   int32 endBlue = endColor & 0xFF;
   int32 redInc = ((endRed - startRed) << 16) / numSteps;
   int32 greenInc = ((endGreen - startGreen) << 16) / numSteps;
   int32 blueInc = ((endBlue - startBlue) << 16) / numSteps;
   int32 red = startRed << 16;
   int32 green = startGreen << 16;
   int32 blue = startBlue << 16;
   int32 i;
   int32 leftOffset = 0;
   int32 rightOffset = 0;
   bool hasRadius = (topLeftRadius + topRightRadius + bottomLeftRadius + bottomRightRadius) > 0;
   Pixel p;
   bool drawFadedPixels = !Graphics_useOpenGL(g);
#ifdef __gl2_h_    
   int32 clipX1 = Graphics_clipX1(g) - Graphics_transX(g), clipY1 = Graphics_clipY1(g) - Graphics_transY(g), clipX2 = Graphics_clipX2(g) - Graphics_transX(g), clipY2 = Graphics_clipY2(g) - Graphics_transY(g);
   bool optimize = Graphics_useOpenGL(g) && (startX >= clipX1 && startY >= clipY1 && endX < clipX2 && endY < clipY2) && topLeftRadius == topRightRadius && bottomLeftRadius == bottomRightRadius && topLeftRadius == bottomLeftRadius;
#else
   bool optimize = false;
#endif
   int32 ri, gi, bi, rf, gf, bf, stage=0;

   if (startX > endX)
   {
      int32 temp = startX;
      startX = endX;
      endX = temp;
   }
   if (startY > endY)
   {
      int32 temp = startY;
      startY = endY;
      endY = temp;
   }

   for (i = 0; i < numSteps; i++)
   {
      if (hasRadius)
      {
         leftOffset = rightOffset = 0;

         if (topLeftRadius > 0 && i < topLeftRadius)
            leftOffset = getOffset(topLeftRadius, topLeftRadius - i - 1) - 1;
         else
         if (bottomLeftRadius > 0 && i > numSteps - bottomLeftRadius)
            leftOffset = getOffset(bottomLeftRadius, bottomLeftRadius - (numSteps - i + 1)) - 1;

         if (topRightRadius > 0 && i < topRightRadius)
            rightOffset = getOffset(topRightRadius, topRightRadius - i - 1) - 1;
         else
         if (bottomRightRadius > 0 && i > numSteps - bottomRightRadius)
            rightOffset = getOffset(bottomRightRadius, bottomRightRadius - (numSteps - i + 1)) - 1;

         if (leftOffset < 0) leftOffset = 0;
         if (rightOffset < 0) rightOffset = 0;
      }
      p = makePixel(red >> 16, green >> 16, blue >> 16);
      if (!optimize || leftOffset != 0 || rightOffset != 0)
      {
         if (vertical)
         {
            int32 fc = p;
            drawLine(currentContext, g, startX + leftOffset, startY + i, endX - rightOffset, startY + i, p);
            if (drawFadedPixels && rightOffset != 0) // since there's no fading of pixels in opengl, we can safely ignore this
               drawFadedPixel(currentContext, g, endX - rightOffset + 1, startY + i, fc);
            if (drawFadedPixels && leftOffset != 0)
               drawFadedPixel(currentContext, g, startX + leftOffset - 1, startY + i, fc);
         }
         else
            drawLine(currentContext, g, startX + i, startY + leftOffset, startX + i, endY - rightOffset, p);
      }
      if (stage < 2) // find starting and ending colors
      {
         bool hasOffset = leftOffset != 0 || rightOffset != 0;
         if (stage == 0 && !hasOffset)
         {
            ri = red; gi = green; bi = blue;
            stage++;
         }
         else
         if (stage == 1 && hasOffset)
         {
            rf = red; gf = green; bf = blue;
            stage++;
         }
      }

      red += redInc;
      green += greenInc;
      blue += blueInc;
   }
   if (stage == 1) // case has no radius
   {
      rf = red; gf = green; bf = blue;
   }
#ifdef __gl2_h_
   if (optimize)
   {
      int32 r = topLeftRadius/2;
      PixelConv pi,pf;
      int32 tx = Graphics_transX(g), ty = Graphics_transY(g);
      pi.pixel = makePixelA(255,ri >> 16, gi >> 16,bi >> 16);
      pf.pixel = makePixelA(255,rf >> 16, gf >> 16,bf >> 16);
      if (vertical)
         glFillShadedRect(g, startX + tx, startY + ty + r - 1, endX - startX, endY - startY - r * 2 + 1, pf, pi, false);
      else
         glFillShadedRect(g, startX + tx + r - 1, startY + ty, endX - startX - r * 2 + 1, endY - startY, pf, pi, true);
   }  
#endif
}
#else
static void drawRoundGradient(Context currentContext, TCObject g, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, bool vertical)
{
    startX += Graphics_transX(g);
    startY += Graphics_transY(g);
    endX += Graphics_transX(g);
    endY += Graphics_transY(g);
    skia_setClip(Get_Clip(g));
    skia_drawRoundGradient(0, startX, startY, endX, endY, topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius, startColor | Graphics_alpha(g), endColor | Graphics_alpha(g), vertical);
    skia_restoreClip();

    markDirty(currentContext, g, startX, startY, endX - startX, endY - startY);
}
#endif

//#ifndef SKIA_H
#if 1
static int getsetRGB(Context currentContext, TCObject g, TCObject dataObj, int32 offset, int32 x, int32 y, int32 w, int32 h, bool isGet)
{
   if (dataObj == null)
      throwException(currentContext, NullPointerException, "Argument 'data' can't be null");
   else
   if (!checkArrayRange(currentContext, dataObj, offset, w*h))
      ;
   else
   if (translateAndClip(g, &x, &y, &w, &h))
   {
      Pixel* data = ((Pixel*)ARRAYOBJ_START(dataObj)) + offset;
      int32 inc = Graphics_pitch(g), count = w * h;
      Pixel* pixels = getGraphicsPixels(g) + y * inc + x;
      bool markDirty = !currentContext->fullDirty && !Graphics_isImageSurface(g);
#if 0//def __gl2_h_
      currentContext->fullDirty |= markDirty;
      if (isGet && Graphics_useOpenGL(g))
         glGetPixels(data,x,y,w,h,w);
      else
#endif
      if (isGet)
         for (; h-- > 0; pixels += inc, data += w)
            xmemmove(data, pixels, w<<2);
      else
         for (; h-- > 0; pixels += inc, data += w)
         {
            xmemmove(pixels, data, w<<2);
#ifndef __gl2_h_
            if (markDirty)
               markScreenDirty(currentContext, x, y++, w, 1);
#endif
         }
      return count;
   }
   return 0;
}
#else
static int getsetRGB(Context currentContext, TCObject g, TCObject dataObj, int32 offset, int32 x, int32 y, int32 w, int32 h, bool isGet)
{
   if (dataObj == null)
      throwException(currentContext, NullPointerException, "Argument 'data' can't be null");
   else
   if (!checkArrayRange(currentContext, dataObj, offset, w*h))
      ;
   else {
      Pixel* data = ((Pixel*)ARRAYOBJ_START(dataObj)) + offset;
      int32 count = w * h;

      if (skia_getsetRGB(0, (void*) data, offset, x, y, w, h, isGet) == 1) {
         return count;
      }
   }
   return 0;
}
#endif

#ifndef SKIA_H
static void setPixelA(Context currentContext, TCObject g, int32 x, int32 y, PixelConv color, int32 alpha)
{
#ifdef __gl2_h_
   if (Graphics_useOpenGL(g))
   {
      x += Graphics_transX(g);
      y += Graphics_transY(g);
      if (Graphics_clipX1(g) <= x && x <= Graphics_clipX2(g) && Graphics_clipY1(g) <= y && y <= Graphics_clipY2(g))
         glDrawPixel(x,y,color.pixel,alpha);
   }
   else
#endif
      setPixel(currentContext, g, x,y,interpolate(color, getPixelConv(g, x,y), alpha));
}
#endif

static void addError(PixelConv *pixel, int32 x, int32 y, int32 w, int32 h, int32 errR, int32 errG, int32 errB, int32 j, int32 k)
{
    int32 r, g, b;
    if (x >= w || y >= h || x < 0)
        return;
    r = pixel->r + j * errR / k;
    g = pixel->g + j * errG / k;
    b = pixel->b + j * errB / k;
    if (r > 255)
        r = 255;
    else if (r < 0)
        r = 0;
    if (g > 255)
        g = 255;
    else if (g < 0)
        g = 0;
    if (b > 255)
        b = 255;
    else if (b < 0)
        b = 0;
    pixel->r = r;
    pixel->g = g;
    pixel->b = b;
}

static void dither(Context currentContext, TCObject g, int32 x0, int32 y0, int32 w, int32 h)
{
    PixelConv *pixels;
    int32 oldR, oldG, oldB, newR, newG, newB, errR, errG, errB, pitch, x, y, yf = y0 + h, xf = x0 + w;
    pitch = Graphics_pitch(g);

    x0 += Graphics_transX(g);
    y0 += Graphics_transY(g);

      // based on http://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
      for (y=y0; y < yf; y++)
      {
         pixels = (PixelConv*)(getGraphicsPixels(g) + y * pitch + x0);
         for (x=x0; x < xf; x++,pixels++)
         {
            // get current pixel values
            oldR = pixels->r;
            oldG = pixels->g;
            oldB = pixels->b;
            // convert to 565 component values
            newR = oldR >> 3 << 3;
            newG = oldG >> 2 << 2;
            newB = oldB >> 3 << 3;
            // compute error
            errR = oldR-newR;
            errG = oldG-newG;
            errB = oldB-newB;
            // set new pixel
            pixels->r = newR;
            pixels->g = newG;
            pixels->b = newB;

            addError(pixels+1      , x+1, y ,w,h, errR,errG,errB,7,16);
            addError(pixels-1+pitch, x-1,y+1,w,h, errR,errG,errB,3,16);
            addError(pixels  +pitch, x,y+1  ,w,h, errR,errG,errB,5,16);
            addError(pixels+1+pitch, x+1,y+1,w,h, errR,errG,errB,1,16);
         }
      }
      if (!currentContext->fullDirty && !Graphics_isImageSurface(g)) markScreenDirty(currentContext, x0, y0, w, h);
}

/////////////// Start of Device-dependant functions ///////////////
static bool startupGraphics(int16 appTczAttr) // there are no threads running at this point
{
    return graphicsStartup(&screen, appTczAttr);
}

#ifdef SKIA_H
static void shiftScreen(Context currentContext) {
    int32 shiftY = 0, shiftH = 0;
    TCClass window;
    
    if (shiftYfield == null && (window = loadClass(currentContext, "totalcross.ui.Window", false)) != null)
    {
        needsPaint = getStaticFieldInt(window, "needsPaint");
        shiftYfield = getStaticFieldInt(window, "shiftY");
        shiftHfield = getStaticFieldInt(window, "shiftH");
        lastShiftYfield = getStaticFieldInt(window, "lastShiftY");
        if (shiftYfield == null)
            return;
    }
    shiftY = *shiftYfield;
    shiftH = *shiftHfield;
    
// #if defined ANDROID || defined darwin
//     checkKeyboardAndSIP(currentContext, &shiftY, &shiftH); // Adjust shift according to the keyboard
//     int32 delta = shiftY - screen.screenH;
// #ifdef ANDROID
//     delta = 0;
// #endif
//     if(lastShiftY == shiftY) return;
//     debug("c shiftY: %d", shiftY);
//     debug("c screenH: %d", screen.screenH); 
//     if(shiftY == 0) {
//         skia_shiftScreen(0, -screenY); // return to origin
//         screenY = 0;
//     }
//     else {
//         delta -= screenY;
//         skia_shiftScreen(0, delta);
//         screenY += delta;
//     }
//     //screenY += delta;
//     lastShiftY = shiftY;
// #endif
}
#endif

static bool createScreenSurface(Context currentContext, bool isScreenChange)
{
   bool ret = false;
   if (screen.screenW <= 0 || screen.screenH <= 0)
      return false;

   if (graphicsCreateScreenSurface(&screen))
   {
      TCObject *screenObj;
      screenObj = getStaticFieldObject(currentContext,loadClass(currentContext, "totalcross.ui.gfx.Graphics",false), "mainWindowPixels");
#ifdef darwin // in darwin, the pixels buffer is pre-initialized and never changed
      if (controlEnableUpdateScreenPtr == null)
         controlEnableUpdateScreenPtr = getStaticFieldInt(loadClass(currentContext, "totalcross.ui.Control",false), "enableUpdateScreen");
      ret = true;
#else

      if (isScreenChange)
      {
         screen.mainWindowPixels = *screenObj = null;
         gc(currentContext); // let the gc collect the old screen object
      }
      else
      {
         controlEnableUpdateScreenPtr = getStaticFieldInt(loadClass(currentContext, "totalcross.ui.Control",false), "enableUpdateScreen");
      }

      *screenObj = screen.mainWindowPixels = createArrayObject(currentContext, INT_ARRAY, screen.screenW * screen.screenH);
      setObjectLock(*screenObj, UNLOCKED);
      ret = screen.mainWindowPixels != null && controlEnableUpdateScreenPtr != null;
#endif
   }
   return ret;
}

void markWholeScreenDirty(Context currentContext)
{
   LOCKVAR(screen);
   currentContext->dirtyX1 = currentContext->dirtyY1 = 0;
   currentContext->dirtyX2 = screen.screenW;
   currentContext->dirtyY2 = screen.screenH;
   currentContext->fullDirty = true;
   UNLOCKVAR(screen);
}

static bool checkScreenPixels()
{
   return screen.pixels != null;
}

void updateScreen(Context currentContext)
{
#ifdef ANDROID
   if (appPaused) return;
#endif
   LOCKVAR(screen);
   if (keepRunning && checkScreenPixels() && controlEnableUpdateScreenPtr && *controlEnableUpdateScreenPtr && (currentContext->fullDirty || (currentContext->dirtyX1 != screen.screenW && currentContext->dirtyX2 != 0 && currentContext->dirtyY1 != screen.screenH && currentContext->dirtyY2 != 0)))
   {
      if (updateScreenBits(currentContext)) // move the temporary buffer to the real screen
      {
#ifdef darwin
         UNLOCKVAR(screen); // without this, a deadlock can occur in iOS if the user minimizes the application, since another thread can trigger a markScreenDirty
#endif
         graphicsUpdateScreen(currentContext, &screen);
#ifdef darwin
         LOCKVAR(screen);
#endif
      }
      currentContext->dirtyX1 = screen.screenW;
      currentContext->dirtyY1 = screen.screenH;
      currentContext->dirtyX2 = currentContext->dirtyY2 = 0;
      currentContext->fullDirty = false;
   }
   UNLOCKVAR(screen);
#ifdef SKIA_H
    shiftScreen(currentContext);
#endif
}

void graphicsDestroyPrimitives()
{
   xfree(lookupR);
   xfree(lookupG);
   xfree(lookupB);
   xfree(lookupGray);
   fontDestroy();
}
/////////////// End of Device-dependant functions ///
