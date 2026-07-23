// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "PalmFont.h"
#include "GraphicsPrimitives.h"
#include "math.h"

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


int32 graphicsStartup(ScreenSurface screen, int16 appTczAttr);
int32 graphicsCreateScreenSurface(ScreenSurface screen);
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen);
void graphicsDestroy(ScreenSurface screen, int32 isScreenChange);
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
#include "GraphicsPrimitivesSkia_c.h"

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
         if (pixel1 == pixel2)
            glDrawLine(x, y, x + width, y, pixel1, 255);
         else
            glDrawDots(x, y, x + width, y, pixel1, pixel2);
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
         if (pixel1 == pixel2)
            glDrawLine(x, y, x, y + height, pixel1, 255);
         else
            glDrawDots(x, y, x, y + height, pixel1, pixel2);
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
    skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
    skia_drawDottedLine(skiaSurfaceForGraphics(g), x1, y1, x2, y2, pixel1 | Graphics_alpha(g), pixel2 | Graphics_alpha(g));
    skia_restoreClip(skiaSurfaceForGraphics(g));

    markDirty(currentContext, g, min32(x1, x2), min32(y1, y2), abs(x2 - x1), abs(y2 - y1));
}
#endif

#ifndef SKIA_H
static int32 abs32(int32 a)
{
   return a < 0 ? -a : a;
}

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
   skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
   skia_drawLine(skiaSurfaceForGraphics(g), x1, y1, x2, y2, pixel | Graphics_alpha(g));
   skia_restoreClip(skiaSurfaceForGraphics(g));

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
   skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
   skia_drawRect(skiaSurfaceForGraphics(g), x, y, w, h, pixel | Graphics_alpha(g));
   skia_restoreClip(skiaSurfaceForGraphics(g));

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
   skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
   skia_fillRect(skiaSurfaceForGraphics(g), x, y, w, h, pixel | Graphics_alpha(g));
   skia_restoreClip(skiaSurfaceForGraphics(g));

   markDirty(currentContext, g, x, y, w, h);
}
#endif

#ifdef SKIA_H
// Darkens the screen
static void fadeScreen(Context currentContext, int32 amount) {
    skia_fillRect(SKIA_SCREEN_SURFACE_ID, 0, 0, screen.screenW, screen.screenH,  amount << 24);
    currentContext->dirtyX1 = 0;
    currentContext->dirtyY1 = 0;
    currentContext->dirtyX2 = screen.screenW;
    currentContext->dirtyY2 = screen.screenH;
}
#endif

#include "GraphicsPrimitivesText_c.h"

#include "GraphicsPrimitivesShapes_c.h"

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
extern int32 setShiftYonNextUpdateScreen;

static void checkKeyboardAndSIP(Context currentContext, int32 *shiftY, int32 *shiftH)
{
   int32 appHeightOnSipOpen = screen.screenH - keyboardH;
   if (appHeightOnSipOpen != lastAppHeightOnSipOpen)
   {
      lastAppHeightOnSipOpen = appHeightOnSipOpen;
      markWholeScreenDirty(currentContext);
   }
   if ((*shiftY + *shiftH) > screen.screenH)
      *shiftH = screen.screenH - *shiftY;

   if ((*shiftY + *shiftH) < appHeightOnSipOpen) // don't shift the screen if above
      *shiftY = 0;
   else
   {
      *shiftY -= appHeightOnSipOpen - *shiftH;
      *shiftH = appHeightOnSipOpen ;
   }
   if (oldShiftY != *shiftY)
   {
      oldShiftY = *shiftY;
      setShiftYonNextUpdateScreen = true;
   }
}
#elif defined(ANDROID)
extern int realAppH;
extern int32 setShiftYonNextUpdateScreen;
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
         int32 appHeightOnSipOpen = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
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

         if (appHeightOnSipOpen != lastAppHeightOnSipOpen)
         {
            lastAppHeightOnSipOpen = appHeightOnSipOpen;
            markWholeScreenDirty(currentContext);
         }
         if ((*shiftY + *shiftH) > screen.screenH)
            *shiftH = screen.screenH - *shiftY;

         if ((*shiftY + *shiftH) < appHeightOnSipOpen) // don't shift the screen if above
            *shiftY = 0;
         else
         {
            *shiftY -= appHeightOnSipOpen - *shiftH;
            *shiftH = appHeightOnSipOpen ;
         }
         if (oldShiftY != *shiftY) // prevent 100% cpu use - shift can change on ENTER or PEN_UP - now same code of iOS
         {
             if (*shiftY == 0) { // keyboard is closing
                 lastShiftY = oldShiftY; // save the original shiftY value for the slide down animation
             }
            oldShiftY = *shiftY;
            setShiftYonNextUpdateScreen = true;
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

   gray.pixel = makePixelARGB(*unsafeAreaColorP);

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
               #if defined(WIN32)
               SETPIXEL565_(t, f->pixel)
               #else
               *t++ = (Pixel565)SETPIXEL565(f->r, f->g, f->b);
               #endif
         else
         {
            for (count = shiftH * screenW, f += shiftY * screenW; count != 0; f++,count--)
               #if defined(WIN32)
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
                  #if defined(WIN32)
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
   int32 screenW, screenH, shiftY = 0, shiftH = 0;
   TCClass window;
   PixelConv gray;

   gray.pixel = *unsafeAreaColorP;
   
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

#if defined ANDROID || defined darwin
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

   desiredScreenShiftY = shiftY; // will be set with glScreenShiftY in updateScreen

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
    skia_setClip(skiaSurfaceForGraphics(g), Get_Clip(g));
    skia_drawRoundGradient(skiaSurfaceForGraphics(g), startX, startY, endX, endY, topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius, startColor | Graphics_alpha(g), endColor | Graphics_alpha(g), vertical);
    skia_restoreClip(skiaSurfaceForGraphics(g));

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

      if (skia_getsetRGB(skiaSurfaceForGraphics(g), (void*) data, offset, x, y, w, h, isGet) == 1) {
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
#include "GraphicsPrimitivesScreen_c.h"
