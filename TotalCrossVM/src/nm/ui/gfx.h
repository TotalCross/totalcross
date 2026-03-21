// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_NM_UI_GFX_H
#define TC_NM_UI_GFX_H

#include "xtypes.h"
#include "tcclass.h"
#include "objectmemorymanager.h"

#ifdef __cplusplus
extern "C"
{
#endif

// totalcross.ui.gfx.RRect
#define RRect_radii(o)              ((o) == null ? null : (double*)ARRAYOBJ_START(FIELD_OBJ(o, OBJ_CLASS(o), 0)))

// totalcross.ui.gfx.Paint
typedef struct GfxPaint {
   const int32 *color;
   const int32 *style;
   const double *strokeWidth;
   const int32 *strokeCap;
   const int32 *strokeJoin;
   const double *strokeMiter;
   const int32 *antiAlias;
   const int32 *dither;
   const TCObject *pathEffect;
} GfxPaint;

static __inline GfxPaint gfxPaint(TCObject o)
{
   GfxPaint fields;
   if (o == null)
   {
      fields.color = null;
      fields.style = null;
      fields.strokeWidth = null;
      fields.strokeCap = null;
      fields.strokeJoin = null;
      fields.strokeMiter = null;
      fields.antiAlias = null;
      fields.dither = null;
      fields.pathEffect = null;
      return fields;
   }

   fields.color = &FIELD_I32(o, 0);
   fields.style = &FIELD_I32(o, 1);
   fields.strokeWidth = &FIELD_DBL(o, OBJ_CLASS(o), 0);
   fields.strokeCap = &FIELD_I32(o, 2);
   fields.strokeJoin = &FIELD_I32(o, 3);
   fields.strokeMiter = &FIELD_DBL(o, OBJ_CLASS(o), 1);
   fields.antiAlias = &FIELD_I32(o, 4);
   fields.dither = &FIELD_I32(o, 5);
   fields.pathEffect = &FIELD_OBJ(o, OBJ_CLASS(o), 0);
   return fields;
}

static __inline GfxPaint gfxPaintFromColor(const int32 *color)
{
   GfxPaint fields;
   fields.color = color;
   fields.style = null;
   fields.strokeWidth = null;
   fields.strokeCap = null;
   fields.strokeJoin = null;
   fields.strokeMiter = null;
   fields.antiAlias = null;
   fields.dither = null;
   fields.pathEffect = null;
   return fields;
}

double gfxEllipseDxRRect(double rx, double ry, double dy);
double gfxEllipseLeftBoundRRect(double cx, double cy, double rx, double ry, double py);
double gfxEllipseRightBoundRRect(double cx, double cy, double rx, double ry, double py);
double gfxLeftBoundForYRRect(double py, double left, double top, double bottom,
   double topRx, double topRy, double bottomRx, double bottomRy);
double gfxRightBoundForYRRect(double py, double right, double top, double bottom,
   double topRx, double topRy, double bottomRx, double bottomRy);
bool gfxComputeRRectSpan(int32 x, int32 y, int32 w, int32 h, const double *radii, int32 scanY, int32 *start, int32 *end);

#ifdef __cplusplus
}
#endif

#endif
