// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "skia_internal.h"

void skia_drawDottedLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel1, Pixel pixel2)
{
    SKIA_TRACE()
    float intervals[] = {5, 5};
    forePaint.setPathEffect(SkDashPathEffect::Make(intervals, 2, 2.5f));
    skia_drawLine(skiaSurface, x1, y1, x2, y2, pixel1);
    forePaint.setPathEffect(nullptr);

    forePaint.setPathEffect(SkDashPathEffect::Make(intervals, 2, 7.5f));
    skia_drawLine(skiaSurface, x1, y1, x2, y2, pixel2);
    forePaint.setPathEffect(nullptr);
}

void skia_drawLine(int32 skiaSurface, int32 x1, int32 y1, int32 x2, int32 y2, Pixel pixel)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    forePaint.setColor(skiaColorFromPixel(pixel));
    targetCanvas->drawLine(x1, y1, x2, y2, forePaint);
}

void skia_drawRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    forePaint.setColor(skiaColorFromPixel(pixel));
    targetCanvas->drawRect(SkRect::MakeXYWH(x, y, w, h), forePaint);
}

void skia_fillRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, Pixel pixel)
{
    SKIA_TRACE()
    // printf("Exe log: skia fill rect = %#010x\n",pixel);
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    backPaint.setColor(skiaColorFromPixel(pixel));
    targetCanvas->drawRect(SkRect::MakeXYWH(x, y, w, h), backPaint);
}

void skia_drawText(int32 skiaSurface, const void *text, int32 chrCount, int32 x0, int32 y0, Pixel foreColor, int32 justifyWidth, int32 fontSize, int32 typefaceIndex)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    const auto newTypeFace = skia_getTypeface(typefaceIndex);

    if(skFont.getTypeface() != newTypeFace.get()) {
        skFont.setTypeface(newTypeFace);
    }
    if(skFont.getSize() != fontSize) {
        skFont.setSize(fontSize);
    }
    if(backPaint.getColor() != foreColor){
        backPaint.setColor(skiaColorFromPixel(foreColor));
    }
    targetCanvas->drawTextBlob(SkTextBlob::MakeFromText(text,chrCount,skFont,SkTextEncoding::kUTF16),x0,y0,backPaint);
}

void skia_ellipseDrawAndFill(int32 skiaSurface, int32 xc, int32 yc, int32 rx, int32 ry, Pixel pc1, Pixel pc2, bool fill, bool gradient)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    if (fill) {
        if (gradient) {
            SkPoint points[3] = {
                    SkPoint::Make(xc, yc - ry),
                    SkPoint::Make(xc, yc + ry),
                    SkPoint::Make(xc, yc + ry * 2)
            };
            SkColor colors[3] = {skiaColorFromPixel(pc2), skiaColorFromPixel(pc1), skiaColorFromPixel(pc2)};
            backPaint.setShader(SkGradientShader::MakeLinear(
                    points, colors, nullptr, 3,
                    SkTileMode::kClamp, 0, nullptr));
            targetCanvas->drawOval(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), backPaint);
            backPaint.setShader(nullptr);
        } else {
            backPaint.setColor(skiaColorFromPixel(pc2));
            targetCanvas->drawOval(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), backPaint);
        }
    } else {
        forePaint.setColor(skiaColorFromPixel(pc1));
        targetCanvas->drawOval(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), forePaint);
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
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    forePaint.setColor(skiaColorFromPixel(pixel));
    targetCanvas->translate(tx, ty);
    targetCanvas->drawPath(_skia_makePath(xPoints, yPoints, nPoints), forePaint);
    targetCanvas->translate(-tx, -ty);
}

void skia_fillPolygon(int32 skiaSurface, int32 *xPoints, int32 *yPoints, int32 nPoints, int32 tx, int32 ty, Pixel c1, Pixel c2, bool gradient, bool isPie)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    SkPath path = _skia_makePath(xPoints, yPoints, nPoints);

    backPaint.setColor(skiaColorFromPixel(c1));
    if (gradient) {
        int32 minY, maxY;
        _skia_getPathBounds(xPoints, yPoints, nPoints, &minY, &maxY);
        SkPoint points[2] = {
                SkPoint::Make(xPoints[0], minY),
                SkPoint::Make(xPoints[0], maxY),
        };
        SkColor colors[2] = {skiaColorFromPixel(c1), skiaColorFromPixel(c2)};
        backPaint.setShader(SkGradientShader::MakeLinear(
                points, colors, nullptr, 2,
                SkTileMode::kClamp, 0, nullptr));
    }

    targetCanvas->translate(tx, ty);
    targetCanvas->drawPath(path, backPaint);
    targetCanvas->translate(-tx, -ty);

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
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    if (fill) {
        backPaint.setColor(skiaColorFromPixel(c2));
        if (gradient) {
            SkPath arcPath = _skia_makeArcPath(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie);
            SkRect r = arcPath.computeTightBounds();

            SkPoint points[2] = {
                SkPoint::Make(r.centerX(), r.y()),
                SkPoint::Make(r.centerX(), r.y() + r.height() * 2)
            };
            SkColor colors[2] = {skiaColorFromPixel(c), skiaColorFromPixel(c2)};
            backPaint.setShader(SkGradientShader::MakeLinear(
                    points, colors, nullptr, 3,
                    SkTileMode::kClamp, 0, nullptr));

            targetCanvas->drawPath(arcPath, backPaint);
            backPaint.setShader(nullptr);
        } else {
            targetCanvas->drawArc(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie, backPaint);
            forePaint.setColor(skiaColorFromPixel(c));
            SkScalar strokeWidth = forePaint.getStrokeWidth();
            forePaint.setStrokeWidth(2);
            targetCanvas->drawArc(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie, forePaint);
            forePaint.setStrokeWidth(strokeWidth);
        }
    } else {
        forePaint.setColor(skiaColorFromPixel(c));
        targetCanvas->drawArc(SkRect::MakeXYWH(xc - rx, yc - ry, rx * 2, ry * 2), start, sweepAngle, pie, forePaint);
    }
}

void skia_drawRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    forePaint.setColor(skiaColorFromPixel(c));
    targetCanvas->drawRRect(SkRRect::MakeRectXY(SkRect::MakeXYWH(x, y, w, h), r, r), forePaint);
}

void skia_fillRoundRect(int32 skiaSurface, int32 x, int32 y, int32 w, int32 h, int32 r, Pixel c)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
    backPaint.setColor(skiaColorFromPixel(c));
    targetCanvas->drawRRect(SkRRect::MakeRectXY(SkRect::MakeXYWH(x, y, w, h), r, r), backPaint);
}

void skia_drawRoundGradient(int32 skiaSurface, int32 startX, int32 startY, int32 endX, int32 endY, int32 topLeftRadius, int32 topRightRadius, int32 bottomLeftRadius, int32 bottomRightRadius, int32 startColor, int32 endColor, bool vertical)
{
    SKIA_TRACE()
    SkCanvas* targetCanvas = skiaGetCanvas(skiaSurface);
    if (!targetCanvas) return;
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

    SkColor colors[2] = {
        skiaColorFromPixel(static_cast<Pixel>(startColor)),
        skiaColorFromPixel(static_cast<Pixel>(endColor))};
    backPaint.setShader(SkGradientShader::MakeLinear(
            points, colors, nullptr, 3,
            SkTileMode::kClamp, 0, nullptr));

    targetCanvas->drawRRect(SkRRect::MakeRectXY(SkRect::MakeXYWH(startX, startY, w, h), topLeftRadius, topLeftRadius), backPaint);
    backPaint.setShader(nullptr);
}
