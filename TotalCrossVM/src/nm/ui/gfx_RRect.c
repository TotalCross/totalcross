// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"
#include "gfx.h"
#include <math.h>

static bool gfxContainsCornerRRect(double px, double py, double x, double y, double rx, double ry, int32 corner)
{
   double cx, cy, dx, dy;
   bool inCornerBox;

   if (rx <= 0 || ry <= 0)
      return true;

   cx = (corner == 0 || corner == 3) ? x + rx : x - rx;
   cy = (corner == 0 || corner == 1) ? y + ry : y - ry;
   inCornerBox =
      (corner == 0 && px < x + rx && py < y + ry) ||
      (corner == 1 && px >= x - rx && py < y + ry) ||
      (corner == 2 && px >= x - rx && py >= y - ry) ||
      (corner == 3 && px < x + rx && py >= y - ry);

   if (!inCornerBox)
      return true;

   dx = (px - cx) / rx;
   dy = (py - cy) / ry;
   return dx * dx + dy * dy <= 1.0;
}

double gfxEllipseDxRRect(double rx, double ry, double dy)
{
   double t;
   if (rx <= 0 || ry <= 0)
      return 0;
   t = 1.0 - (dy * dy) / (ry * ry);
   if (t <= 0)
      return 0;
   return rx * sqrt(t);
}

double gfxEllipseLeftBoundRRect(double cx, double cy, double rx, double ry, double py)
{
   return cx - gfxEllipseDxRRect(rx, ry, py - cy);
}

double gfxEllipseRightBoundRRect(double cx, double cy, double rx, double ry, double py)
{
   return cx + gfxEllipseDxRRect(rx, ry, py - cy);
}

double gfxLeftBoundForYRRect(double py, double left, double top, double bottom,
   double topRx, double topRy, double bottomRx, double bottomRy)
{
   if (topRx > 0 && topRy > 0 && py < top + topRy)
      return gfxEllipseLeftBoundRRect(left + topRx, top + topRy, topRx, topRy, py);
   if (bottomRx > 0 && bottomRy > 0 && py >= bottom - bottomRy)
      return gfxEllipseLeftBoundRRect(left + bottomRx, bottom - bottomRy, bottomRx, bottomRy, py);
   return left;
}

double gfxRightBoundForYRRect(double py, double right, double top, double bottom,
   double topRx, double topRy, double bottomRx, double bottomRy)
{
   if (topRx > 0 && topRy > 0 && py < top + topRy)
      return gfxEllipseRightBoundRRect(right - topRx, top + topRy, topRx, topRy, py);
   if (bottomRx > 0 && bottomRy > 0 && py >= bottom - bottomRy)
      return gfxEllipseRightBoundRRect(right - bottomRx, bottom - bottomRy, bottomRx, bottomRy, py);
   return right;
}

bool gfxComputeRRectSpan(int32 x, int32 y, int32 w, int32 h, const double *radii, int32 scanY, int32 *start, int32 *end)
{
   double py = scanY + 0.5;
   double left = x;
   double top = y;
   double right = x + w;
   double bottom = y + h;
   double rr[8] = {0,0,0,0,0,0,0,0};
   double spanStart, spanEnd;
   int i;

   if (py < top || py >= bottom)
      return false;

   if (radii != null)
      for (i = 0; i < 8; i++)
         rr[i] = radii[i];

   spanStart = gfxLeftBoundForYRRect(py, left, top, bottom, rr[0], rr[1], rr[6], rr[7]);
   if (spanStart < left)
      spanStart = left;

   spanEnd = gfxRightBoundForYRRect(py, right, top, bottom, rr[2], rr[3], rr[4], rr[5]);
   if (spanEnd > right)
      spanEnd = right;

   *start = (int32)ceil(spanStart - 0.5);
   *end = (int32)floor(spanEnd - 0.5);
   return *end >= *start;
}

TC_API void tugRR_containsCorner_ddddddi(NMParams p)
{
   p->retI = gfxContainsCornerRRect(p->dbl[0], p->dbl[1], p->dbl[2], p->dbl[3], p->dbl[4], p->dbl[5], p->i32[0]);
}

TC_API void tugRR_leftBoundForY_dddddddd(NMParams p)
{
   p->retD = gfxLeftBoundForYRRect(p->dbl[0], p->dbl[1], p->dbl[2], p->dbl[3], p->dbl[4], p->dbl[5], p->dbl[6], p->dbl[7]);
}

TC_API void tugRR_rightBoundForY_dddddddd(NMParams p)
{
   p->retD = gfxRightBoundForYRRect(p->dbl[0], p->dbl[1], p->dbl[2], p->dbl[3], p->dbl[4], p->dbl[5], p->dbl[6], p->dbl[7]);
}

TC_API void tugRR_ellipseLeftBound_ddddd(NMParams p)
{
   p->retD = gfxEllipseLeftBoundRRect(p->dbl[0], p->dbl[1], p->dbl[2], p->dbl[3], p->dbl[4]);
}

TC_API void tugRR_ellipseRightBound_ddddd(NMParams p)
{
   p->retD = gfxEllipseRightBoundRRect(p->dbl[0], p->dbl[1], p->dbl[2], p->dbl[3], p->dbl[4]);
}

TC_API void tugRR_ellipseDx_ddd(NMParams p)
{
   p->retD = gfxEllipseDxRRect(p->dbl[0], p->dbl[1], p->dbl[2]);
}
