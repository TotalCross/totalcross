// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef SKIA_H
#define SKIA_H

#include "tc_platform.h"

#include <stdint.h>

#define SKIA_SCREEN_SURFACE_ID (-1)
#define SKIA_INVALID_SURFACE_ID (-2)

#ifdef __cplusplus
extern "C"
{
#endif

typedef int int32;
typedef unsigned int uint32;
typedef uint32 Pixel32; // 32 bpp
typedef Pixel32 Pixel;

#if TC_WINDOWING_SDL
int32 colorType(uint32 pixelformat);
#endif
void initSkia(int w, int h, void * pixels, int pitch, uint32 pixelformat);
void destroySkiaScreen();
void flushSkia();

int skia_makeTypeface(char* name, void *data, int32 size);
int32 skia_getTypefaceIndex(char* name);
int32 skia_stringWidth(const void *text, int32 charCount, int32 typefaceIndex, double fontSize, int32 bold);
double skia_stringWidthD(const void *text, int32 charCount, int32 typefaceIndex, double fontSize, int32 bold);
void skia_fontMetrics(int32 typefaceIndex, double fontSize, int32 bold, double* ascent, double* descent, double* leading);

int skia_makeBitmap(int32 id, void *data, int32 w, int32 h);
void skia_deleteBitmap(int32 id);

void skia_setClip(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2);
void skia_restoreClip(int32 skiaSurface);
void skia_setSurfaceScale(int32 skiaSurface, double contentScale);

void skia_drawSurface(int32 skiaSurface, int32 id, float srcLeft, float srcTop, float srcRight, float srcBottom, float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask);
void skia_drawDottedLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2);
Pixel skia_getPixel(int32 skiaSurface, int32 x, int32 y);
int skia_getPixelRow(int32 skiaSurface, void *output, int32 y, int32 width);
void skia_setPixel(int32 skiaSurface, int32 x, int32 y, Pixel pixel);
void skia_drawLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel);
void skia_drawRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel);
void skia_fillRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel);
void skia_drawText(int32 skiaSurface, const void *text, int32 chrCount, double x0, double y0, Pixel foreColor, int32 justifyWidth, double fontSize, int32 typefaceIndex, int32 bold);
void skia_ellipseDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, int32 fill, int32 gradient);
void skia_fillPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel c1, Pixel c2, int32 gradient, int32 isPie);
void skia_drawPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel pixel);
void skia_arcPiePointDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, double startAngle, double endAngle, Pixel c, Pixel c2, int32 fill, int32 pie, int32 gradient);
void skia_drawRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c);
void skia_fillRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c);
void skia_drawRoundGradient(int32 skiaSurface, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, int32 vertical);
int32 skia_getsetRGB(int32 skiaSurface, void *dataObj, int32 offset, int32 x, int32 y, int32 w, int32 h, int32 isGet);
void skia_shiftScreen(float w, float h, float glShiftY);

int64_t skia_image_backing_create_empty(int32 width, int32 height);
int64_t skia_image_backing_create_from_rgba_pixels(void* pixels, int32 width, int32 height);
int64_t skia_image_backing_create_from_argb_pixels(const void* pixels, int32 width, int32 height);
#define SKIA_IMAGE_BACKING_SNAPSHOT_OK 0
#define SKIA_IMAGE_BACKING_SNAPSHOT_INVALID 1
#define SKIA_IMAGE_BACKING_SNAPSHOT_ALLOCATION_FAILURE 2
int64_t skia_image_backing_snapshot(int64_t handle);
int skia_image_backing_snapshot_status(int64_t handle, int64_t* snapshotHandle);
void skia_image_backing_fail_next_snapshot_for_test(void);
int skia_image_backing_make_mutable(int64_t handle);
int64_t skia_image_backing_scale(int64_t handle, int32 outputWidth, int32 outputHeight, bool smooth);
int32 skia_image_backing_width(int64_t handle);
int32 skia_image_backing_height(int64_t handle);
int skia_image_backing_read_pixels(int64_t handle, void* output, int32 x, int32 y, int32 width, int32 height);
int skia_image_backing_read_row(int64_t handle, void* output, int32 y, int32 width);
int skia_image_backing_read_rgba_row(int64_t handle, void* output, int32 y, int32 width);
int skia_image_backing_draw(int64_t targetHandle, int64_t sourceHandle,
    float srcLeft, float srcTop, float srcRight, float srcBottom,
    float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask);
int32 skia_image_backing_surface_id(int64_t handle);
int skia_image_backing_draw_to_surface(int32 targetSurface, int64_t sourceHandle,
    float srcLeft, float srcTop, float srcRight, float srcBottom,
    float dstLeft, float dstTop, float dstRight, float dstBottom, int32 alphaMask);
void skia_image_backing_release(int64_t handle);
#ifdef __cplusplus
}
#endif

#endif
