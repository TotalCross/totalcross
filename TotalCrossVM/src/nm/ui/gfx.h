// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TC_NM_UI_GFX_H
#define TC_NM_UI_GFX_H

#include "tcclass.h"

#ifdef __cplusplus
extern "C"
{
#endif

// totalcross.ui.gfx.RRect
#define RRect_radii(o)              ((o) == null ? null : (double*)ARRAYOBJ_START(FIELD_OBJ(o, OBJ_CLASS(o), 0)))

// totalcross.ui.gfx.Paint
#define Paint_color(o)              FIELD_I32(o, 0)

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
