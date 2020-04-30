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
#if defined ANDROID || defined darwin || defined HEADLESS
#define Graphics_forePixel(o) (Graphics_foreColor(o) | 0xFF000000)
#define Graphics_backPixel(o) (Graphics_backColor(o) | 0xFF000000)
#else
#define Graphics_forePixel(o)      makePixelRGB(Graphics_foreColor(o))
#define Graphics_backPixel(o)      makePixelRGB(Graphics_backColor(o))
#endif
#include "GraphicsPrimitives_c.h"

#ifdef WP8
#include "wp8/gfx_Graphics_c.h"
#elif defined(WINCE) || defined(WIN32)
 #include "win/gfx_Graphics_c.h"
#elif defined(ANDROID) || defined(darwin)
 #include "android/gfx_Graphics_c.h"
#elif defined(linux) && !defined(darwin)
 #include "linux/gfx_Graphics_c.h"
#endif

bool initGraphicsBeforeSettings(Context currentContext, int16 appTczAttr) // no thread are running at this point
{
   return startupGraphics(appTczAttr) && createScreenSurface(currentContext, false) && (screen.bpp != 8 || createColorPaletteLookupTables());
}

void destroyGraphics()
{
   graphicsDestroyPrimitives();
   if (screen.pixels) graphicsDestroy(&screen, false);
   screen.pixels = null; // disallow future updates of the screen
}

bool initGraphicsAfterSettings(Context currentContext)
{
   updateScreenSettings(screen.screenW, screen.screenH, screen.hRes, screen.vRes, screen.bpp);
   if (!fontInit(currentContext))
   {
      destroyGraphics();
      return false;
   }
   return true;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tugG_create_g(NMParams p) // totalcross/ui/gfx/Graphics native protected void create(totalcross.ui.gfx.GfxSurface surface);
{
   TCObject g = p->obj[0];
   TCObject surface = p->obj[1];
   SurfaceType stype = getSurfaceType(p->currentContext, surface);
   int32 w,h;

   if (stype == SURF_IMAGE)
   {
      w = *getInstanceFieldInt(surface, "width",  "totalcross.ui.image.Image");
      h = *getInstanceFieldInt(surface, "height", "totalcross.ui.image.Image");
   }
   else
   {
#ifdef ENABLE_TEST_SUITE // no thread are running during a test_suite
      w = screen.screenW;
      h = screen.screenH;
#else
      w = *getInstanceFieldInt(surface, "width",  "totalcross.ui.Control");
      h = *getInstanceFieldInt(surface, "height", "totalcross.ui.Control");
#endif
   }
   createGfxSurface(w, h, g, stype);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawEllipse_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawEllipse(int xc, int yc, int rx, int ry);
{
   TCObject g = p->obj[0];
   ellipseDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_forePixel(g), Graphics_forePixel(g), false, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillEllipse_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void fillEllipse(int xc, int yc, int rx, int ry);
{
   TCObject g = p->obj[0];
   ellipseDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_backPixel(g), Graphics_backPixel(g), true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillEllipseGradient_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void fillEllipseGradient(int xc, int yc, int rx, int ry);
{
   TCObject g = p->obj[0];
   ellipseDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_forePixel(g), Graphics_backPixel(g), true, true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawArc_iiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void drawArc(int xc, int yc, int r, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_forePixel(g), false, false, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawPie_iiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void drawPie(int xc, int yc, int r, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_forePixel(g), false, true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillPie_iiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void fillPie(int xc, int yc, int r, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_backPixel(g), true, true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillPieGradient_iiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void fillPieGradient(int xc, int yc, int r, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_backPixel(g), true, true, true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawEllipticalArc_iiiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void drawEllipticalArc(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_forePixel(g), false, false, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawEllipticalPie_iiiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void drawEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_forePixel(g), false, true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillEllipticalPie_iiiidd(NMParams p) // totalcross/ui/gfx/Graphics native public void fillEllipticalPie(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_backPixel(g), true, true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillEllipticalPieGradient_i(NMParams p) // totalcross/ui/gfx/Graphics native public void fillEllipticalPieGradient(int xc, int yc, int rx, int ry, double startAngle, double endAngle);
{
   TCObject g = p->obj[0];
   arcPiePointDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], p->dbl[0], p->dbl[1], Graphics_forePixel(g), Graphics_backPixel(g), true, true, true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawCircle_iii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawCircle(int xc, int yc, int r);
{
   TCObject g = p->obj[0];
   ellipseDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], Graphics_forePixel(g), Graphics_forePixel(g), false, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillCircle_iii(NMParams p) // totalcross/ui/gfx/Graphics native public void fillCircle(int xc, int yc, int r);
{
   TCObject g = p->obj[0];
   ellipseDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], Graphics_backPixel(g), Graphics_backPixel(g), true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillCircleGradient_iii(NMParams p) // totalcross/ui/gfx/Graphics native public void fillCircleGradient(int xc, int yc, int r);
{
   TCObject g = p->obj[0];
   ellipseDrawAndFill(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[2], Graphics_foreColor(g), Graphics_backPixel(g), true, true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_getPixel_ii(NMParams p) // totalcross/ui/gfx/Graphics native public int getPixel(int x, int y);
{
   TCObject g = p->obj[0];
   p->retI = getPixel(g, p->i32[0], p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_setPixel_ii(NMParams p) // totalcross/ui/gfx/Graphics native public void setPixel(int x, int y);
{
   TCObject g = p->obj[0];
   setPixel(p->currentContext, g, p->i32[0], p->i32[1], Graphics_forePixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawLine_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawLine(int ax, int ay, int bx, int by);
{
   TCObject g = p->obj[0];
   drawLine(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_forePixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawLine_iiiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawLine(int ax, int ay, int bx, int by, int c);
{
    TCObject g = p->obj[0];
    drawLine(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->i32[4]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawDots_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawDots(int ax, int ay, int bx, int by);
{
   TCObject g = p->obj[0];
   drawDottedLine(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_forePixel(g), Graphics_backPixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawRect_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawRect(int x, int y, int w, int h);
{
   TCObject g = p->obj[0];
   drawRect(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_forePixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillRect_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void fillRect(int x, int y, int w, int h);
{
   TCObject g = p->obj[0];
   fillRect(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], Graphics_backPixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillPolygon_IIi(NMParams p) // totalcross/ui/gfx/Graphics native public void fillPolygon(int []xPoints, int []yPoints, int nPoints);
{
   TCObject g = p->obj[0];
   TCObject xPoints = p->obj[1];
   TCObject yPoints = p->obj[2];
   int32 nPoints = p->i32[0];
   // fdie@ the vm has a 4bytes pointer!
   int32* xp = (int32 *)ARRAYOBJ_START(xPoints);
   int32* yp = (int32 *)ARRAYOBJ_START(yPoints);

   if (checkArrayRange(p->currentContext, xPoints, 0, nPoints) && checkArrayRange(p->currentContext, yPoints, 0, nPoints))
      fillPolygon(p->currentContext, g, xp, yp, nPoints, 0, 0, 0, 0, 0, Graphics_backPixel(g), Graphics_backPixel(g), false, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillPolygonGradient_IIi(NMParams p) // totalcross/ui/gfx/Graphics native public void fillPolygonGradient(int []xPoints, int []yPoints, int nPoints);
{
   TCObject g = p->obj[0];
   TCObject xPoints = p->obj[1];
   TCObject yPoints = p->obj[2];
   int32 nPoints = p->i32[0];
   // fdie@ the vm has a 4bytes pointer!
   int32* xp = (int32 *)ARRAYOBJ_START(xPoints);
   int32* yp = (int32 *)ARRAYOBJ_START(yPoints);

   if (checkArrayRange(p->currentContext, xPoints, 0, nPoints) && checkArrayRange(p->currentContext, yPoints, 0, nPoints))
      fillPolygon(p->currentContext, g, xp, yp, nPoints, 0, 0, 0, 0, 0, Graphics_forePixel(g), Graphics_backPixel(g), true, false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawPolygon_IIi(NMParams p) // totalcross/ui/gfx/Graphics native public void drawPolygon(int []xPoints, int []yPoints, int nPoints);
{
   TCObject g = p->obj[0];
   TCObject xPoints = p->obj[1];
   TCObject yPoints = p->obj[2];
   int32 nPoints = p->i32[0];
   // fdie@ the vm has a 4bytes pointer!
   int32* xp = (int32 *)ARRAYOBJ_START(xPoints);
   int32* yp = (int32 *)ARRAYOBJ_START(yPoints);

   if (checkArrayRange(p->currentContext, xPoints, 0, nPoints) && checkArrayRange(p->currentContext, yPoints, 0, nPoints))
   {
      drawPolygon(p->currentContext, g, xp, yp, nPoints, 0, 0, 0, 0, 0, Graphics_forePixel(g));
      drawLine(p->currentContext, g, xp[0],yp[0],xp[nPoints-1],yp[nPoints-1], Graphics_forePixel(g));
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawPolyline_IIi(NMParams p) // totalcross/ui/gfx/Graphics native public void drawPolyline(int []xPoints, int []yPoints, int nPoints);
{
   TCObject g = p->obj[0];
   TCObject xPoints = p->obj[1];
   TCObject yPoints = p->obj[2];
   int32 nPoints = p->i32[0];
   // fdie@ the vm has a 4bytes pointer!
   int32* xp = (int32 *)ARRAYOBJ_START(xPoints);
   int32* yp = (int32 *)ARRAYOBJ_START(yPoints);

   if (checkArrayRange(p->currentContext, xPoints, 0, nPoints) && checkArrayRange(p->currentContext, yPoints, 0, nPoints))
      drawPolygon(p->currentContext, g, xp, yp, nPoints, 0, 0, 0, 0, 0, Graphics_forePixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawText_siii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawText(String text, int x, int y, int justifyWidth);
{
	TCObject text;
	TCObject g = p->obj[0];
	if ((text = p->obj[1]) != null)
		drawText(p->currentContext, g, String_charsStart(text), String_charsLen(text), p->i32[0], p->i32[1], Graphics_forePixel(g), p->i32[2]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawRoundRect_iiiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawRoundRect(int x, int y, int width, int height, int r);
{
   TCObject g = p->obj[0];
   drawRoundRect(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->i32[4], Graphics_forePixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fillRoundRect_iiiii(NMParams p) // totalcross/ui/gfx/Graphics native public void fillRoundRect(int x, int y, int width, int height, int r);
{
   TCObject g = p->obj[0];
   fillRoundRect(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->i32[4], Graphics_backPixel(g));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_copyRect_giiiiii(NMParams p) // totalcross/ui/gfx/Graphics native public void copyRect(totalcross.ui.gfx.GfxSurface surface, int x, int y, int width, int height, int dstX, int dstY);
{
   TCObject hDest = p->obj[0];
   TCObject hOrig = p->obj[1];
   if (hOrig)
      drawSurface(p->currentContext, hDest, hOrig, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->i32[4], p->i32[5], true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawRoundGradient_iiiiiiiii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawRoundGradient(int startX, int startY, int endX, int endY, int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius,int startColor, int endColor);
{
   TCObject g = p->obj[0];
   drawRoundGradient(p->currentContext, g, p->i32[0],p->i32[1],p->i32[2],p->i32[3],p->i32[4],p->i32[5],p->i32[6],p->i32[7],p->i32[8],p->i32[9], p->i32[10]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawImage_iiib(NMParams p) // totalcross/ui/gfx/Graphics native public void drawImage(totalcross.ui.image.Image image, int x, int y, boolean doClip);
{
   TCObject surfDest = p->obj[0];
   TCObject surfOrig = p->obj[1];
   if (surfOrig) drawSurface(p->currentContext, surfDest, surfOrig, 0, 0, (int32)(Image_width(surfOrig) * Image_hwScaleW(surfOrig)), (int32)(Image_height(surfOrig) * Image_hwScaleH(surfOrig)), p->i32[0], p->i32[1], (bool)p->i32[2]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_copyImageRect_iiiiib(NMParams p) // totalcross/ui/gfx/Graphics native public void copyImageRect(totalcross.ui.image.Image image, int x, int y, int width, int height, boolean doClip);
{
   TCObject surfDest = p->obj[0];
   TCObject surfOrig = p->obj[1];
   if (surfOrig) drawSurface(p->currentContext, surfDest, surfOrig, p->i32[0], p->i32[1], p->i32[2], p->i32[3], 0,0, (bool)p->i32[4]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_setPixels_IIi(NMParams p) // totalcross/ui/gfx/Graphics native public void setPixels(int []xPoints, int []yPoints, int nPoints);
{
   TCObject g = p->obj[0];
   TCObject xPoints = p->obj[1];
   TCObject yPoints = p->obj[2];
   int32 nPoints = p->i32[0];
   Pixel c = Graphics_forePixel(g);

   // fdie@ the vm has a 4bytes pointer!
   int32 * xp = (int32 *)ARRAYOBJ_START(xPoints);
   int32 * yp = (int32 *)ARRAYOBJ_START(yPoints);

   if (checkArrayRange(p->currentContext, xPoints, 0, nPoints) && checkArrayRange(p->currentContext, yPoints, 0, nPoints))
      while (nPoints-- > 0)
         setPixel(p->currentContext, g, *xp++, *yp++, c);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_refresh_iiiiiif(NMParams p) // totalcross/ui/gfx/Graphics native public void refresh(int sx, int sy, int sw, int sh, int tx, int ty, totalcross.ui.font.Font f);
{
   TCObject g = p->obj[0];
   TCObject surf = Graphics_surface(g);
   int32 scrW,scrH;
   if (Surface_isImage(surf)) // update everything, because a screen rotation may have occured
   {
      scrW = Graphics_clipX2(g) = Graphics_width (g) = Graphics_pitch(g) = (int32)(Image_width(surf) * Image_hwScaleW(surf));
      scrH = Graphics_clipY2(g) = Graphics_height(g) = (int32)(Image_height(surf) * Image_hwScaleH(surf));
   }
   else
   {
      scrW = Graphics_clipX2(g) = Graphics_width (g) = Graphics_pitch(g) = screen.screenW;
      scrH = Graphics_clipY2(g) = Graphics_height(g) = screen.screenH;
   }
   Graphics_clipX1(g) = Graphics_minX(g) = max32(0,p->i32[0]);
   Graphics_clipY1(g) = Graphics_minY(g) = max32(0,p->i32[1]);
   Graphics_clipX2(g) = Graphics_maxX(g) = min32(p->i32[0]+p->i32[2],scrW);
   Graphics_clipY2(g) = Graphics_maxY(g) = min32(p->i32[1]+p->i32[3],scrH);
   Graphics_transX(g) = p->i32[4];
   Graphics_transY(g) = p->i32[5];
   if (p->obj[1]) Graphics_font(g) = p->obj[1];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_drawImage_iii(NMParams p) // totalcross/ui/gfx/Graphics native public void drawImage(totalcross.ui.image.Image image, int x, int y);
{
   //copyRect(image, 0, 0, image.getWidth(),image.getHeight(), x, y);
   TCObject surfDest = p->obj[0];
   TCObject surfOrig = p->obj[1];
   if (surfOrig) drawSurface(p->currentContext, surfDest, surfOrig, 0,0, (int32)(Image_width(surfOrig) * Image_hwScaleW(surfOrig)), (int32)(Image_height(surfOrig) * Image_hwScaleH(surfOrig)), p->i32[0], p->i32[1], true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_getRGB_Iiiiii(NMParams p) // totalcross/ui/gfx/Graphics native public int getRGB(int []data, int offset, int x, int y, int w, int h);
{
   TCObject g = p->obj[0];
   TCObject data = p->obj[1];
   p->retI = getsetRGB(p->currentContext, g, data, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->i32[4],true);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_setRGB_Iiiiii(NMParams p) // totalcross/ui/gfx/Graphics native public int setRGB(int []data, int offset, int x, int y, int w, int h);
{
   TCObject g = p->obj[0];
   TCObject data = p->obj[1];
   p->retI = getsetRGB(p->currentContext, g, data, p->i32[0], p->i32[1], p->i32[2], p->i32[3], p->i32[4],false);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_fadeScreen_i(NMParams p) // totalcross/ui/gfx/Graphics native public static void fadeScreen(int fadeValue);
{
#ifndef SKIA_H
#ifdef __gl2_h_
   glFillRect(0,0,appW,appH,0,p->i32[0]);
#else   
   if (graphicsLock(&screen, true))
   {
      int32 fadeValue = p->i32[0], len,r,g,b;
      PixelConv *pixels = (PixelConv*)ARRAYOBJ_START(screen.mainWindowPixels);
      for (len = screen.screenH * screen.screenW; len-- > 0; pixels++)
      {
         r = pixels->r * fadeValue; pixels->r = (r+1 + (r >> 8)) >> 8;
         g = pixels->g * fadeValue; pixels->g = (g+1 + (g >> 8)) >> 8;
         b = pixels->b * fadeValue; pixels->b = (b+1 + (b >> 8)) >> 8;
      }
      graphicsLock(&screen, false);
   }                          
#endif
#else
   int32 fadeValue = p->i32[0];
   fadeScreen(p->currentContext, fadeValue);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tugG_dither_iiii(NMParams p) // totalcross/ui/gfx/Graphics native public void dither(int x, int y, int w, int h);
{
   TCObject g = p->obj[0];
   dither(p->currentContext, g, p->i32[0], p->i32[1], p->i32[2], p->i32[3]);
}

#ifdef ENABLE_TEST_SUITE
#include "gfx_Graphics_test.h"
#endif
