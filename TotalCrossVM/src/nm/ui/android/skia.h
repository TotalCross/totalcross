// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_H
#define SKIA_H

#ifdef __cplusplus
extern "C"
{
#endif

typedef int int32;
typedef unsigned int uint32;
typedef uint32 Pixel32; // 32 bpp
typedef Pixel32 Pixel;

void initSkia(int w, int h);
void flushSkia();

int skia_makeTypeface(char* name, void *data, int32 size);
int32 skia_getTypefaceIndex(char* name);
int32 skia_stringWidth(const void *text, int32 charCount, int32 typefaceIndex, int32 fontSize);

int skia_makeBitmap(int32 id, void *data, int32 w, int32 h);
void skia_deleteBitmap(int32 id);

void skia_setClip(int32 x1, int32 y1, int32 x2, int32 y2);
void skia_restoreClip();

void skia_drawSurface(int32 skiaSurface, int32 id, int32 srcX, int32 srcY, int32 srcW, int32 srcH, int32 w, int32 h, int32 dstX, int32 dstY, int32 doClip);
void skia_drawDottedLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2);
Pixel skia_getPixel(int32 skiaSurface, int32 x, int32 y);
void skia_setPixel(int32 skiaSurface, int32 x, int32 y, Pixel pixel);
void skia_drawLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel);
void skia_drawRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel);
void skia_fillRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel);
void skia_drawText(int32 skiaSurface, const void *text, int32 chrCount, int32 x0, int32 y0, Pixel foreColor, int32 justifyWidth, int32 fontSize, int32 typefaceIndex);
void skia_ellipseDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, bool fill, bool gradient);
void skia_fillPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel c1, Pixel c2, bool gradient, bool isPie);
void skia_drawPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel pixel);
void skia_arcPiePointDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, double startAngle, double endAngle, Pixel c, Pixel c2, bool fill, bool pie, bool gradient);
void skia_drawRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c);
void skia_fillRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c);
void skia_drawRoundGradient(int32 skiaSurface, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, bool vertical);
int skia_getsetRGB(int32 skiaSurface, void *dataObj, int32 offset, int32 x, int32 y, int32 w, int32 h, bool isGet);
void skia_shiftScreen(int32 x, int32 y);
#ifdef __cplusplus
}
#endif

#endif
