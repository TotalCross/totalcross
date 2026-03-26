// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.render;

import totalcross.ui.Control;
import totalcross.ui.Insets;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.gfx.RRect;
import totalcross.ui.gfx.Span;
import totalcross.ui.style.geom.BoxGeometry;
import totalcross.ui.style.model.BorderSide;
import totalcross.ui.style.model.BoxBorder;
import totalcross.ui.style.model.BoxPaint;
import totalcross.ui.style.model.BoxShape;
import totalcross.ui.style.model.BoxStyle;
import totalcross.ui.style.model.CornerRadii;
import totalcross.ui.style.model.Elevation;
import totalcross.ui.style.model.Shadow;
import totalcross.util.UnitsConverter;

/**
 * Renders the visual representation of a {@link BoxStyle}.
 */
public final class BoxRenderer implements ControlRenderer {
    private static final double SHADOW_ALPHA_GAIN = 1.8d;

    public final BoxStyle style;
    public final BoxBorder spec;
    public final CornerRadii radii;
    public final int backgroundColor;
    public final int pressedColor;
    public final Elevation elevation;

    /**
     * Creates a painter for the given style.
     */
    public BoxRenderer(BoxStyle style) {
        this.style = style;
        BoxPaint paint = style == null ? null : style.paint;
        BoxShape shape = style == null ? null : style.shape;
        this.spec = paint == null ? null : paint.border;
        this.radii = shape == null || shape.radii == null ? CornerRadii.ZERO : shape.radii;
        this.backgroundColor = paint == null ? 0 : paint.backgroundColor;
        this.pressedColor = paint == null ? Color.WHITE : paint.pressedColor;
        this.elevation = style == null ? Elevation.NONE : style.elevation;
    }

    /**
     * Paints the box for the given bounds.
     */
    @Override
    public void draw(Graphics g, int x, int y, int w, int h, boolean pressed) {
        BoxGeometry geom = BoxGeometry.compute(x, y, w, h, style);
        drawElevation(g, geom, elevation);
        int bg = pressed ? pressedColor : backgroundColor;
        g.drawRRect(geom.borderRRect(), bg, true);
        drawBorderStrip(g, geom, spec);
    }

    /**
     * Returns how much the renderer extends beyond the control bounds.
     */
    @Override
    public double getOutset() {
        return elevation == null ? 0d : elevation.outset;
    }

    /**
     * Returns the effective renderer insets, including layout padding and border widths.
     */
    @Override
    public Insets getInsets() {
        Insets insets = new Insets();
        if (style != null && style.layout != null) {
            insets.left += style.layout.padding.left;
            insets.right += style.layout.padding.right;
            insets.top += style.layout.padding.top;
            insets.bottom += style.layout.padding.bottom;
        }
        if (spec != null) {
            Insets borderInsets = spec.getInsets();
            insets.left += borderInsets.left;
            insets.right += borderInsets.right;
            insets.top += borderInsets.top;
            insets.bottom += borderInsets.bottom;
        }
        return insets;
    }

    /**
     * Returns whether child painting should be clipped.
     */
    @Override
    public boolean shouldClipChildren() {
        return style != null && style.clip != null && style.clip.shouldClip();
    }

    /**
     * Returns the clip used for children for the given control size.
     */
    @Override
    public RRect getChildrenClip(int width, int height) {
        if (!shouldClipChildren()) {
            return null;
        }
        BoxGeometry geom = BoxGeometry.compute(0, 0, width, height, style);
        return geom.paddingRRect();
    }

    /**
     * Paints only the border strip for an already computed geometry.
     */
    public static void drawBorderStrip(Graphics g, BoxGeometry geom, BoxBorder spec) {
        if (!spec.isVisible()) {
            return;
        }

        int outerTop = geom.borderBox.y;
        int outerBottom = geom.borderBox.y + geom.borderBox.height - 1;
        int innerTop = geom.paddingBox.y;
        int innerBottom = geom.paddingBox.y + geom.paddingBox.height - 1;

        for (int yy = outerTop; yy <= outerBottom; yy++) {
            Span outer = geom.borderRRect().horizontalSpan(yy);
            if (!outer.isValid()) {
                continue;
            }

            if (geom.paddingBox.width <= 0 || geom.paddingBox.height <= 0 || yy < innerTop || yy > innerBottom) {
                BorderSide side = yy < innerTop ? spec.top : spec.bottom;
                drawStyledHorizontal(g, side, outer.start, yy, outer.end);
                continue;
            }

            Span inner = geom.paddingRRect().horizontalSpan(yy);
            if (!inner.isValid()) {
                drawStyledHorizontal(g, spec.top, outer.start, yy, outer.end);
                continue;
            }

            if (outer.start < inner.start) {
                drawStyledHorizontal(g, spec.left, outer.start, yy, inner.start - 1);
            }
            if (inner.end < outer.end) {
                drawStyledHorizontal(g, spec.right, inner.end + 1, yy, outer.end);
            }
        }
    }

    private static void drawElevation(Graphics g, BoxGeometry geom, Elevation elevation) {
        if (elevation == null || elevation.shadows == null || elevation.shadows.length == 0) {
            return;
        }
        for (int i = 0; i < elevation.shadows.length; i++) {
            drawShadow(g, geom, elevation.shadows[i]);
        }
    }

    private static void drawShadow(Graphics g, BoxGeometry geom, Shadow shadow) {
        if (shadow == null || shadow.alpha <= 0) {
            return;
        }

        int dx = toPixels(shadow.dx);
        int dy = toPixels(shadow.dy);
        int spread = Math.max(0, toPixels(shadow.spread));
        int blurPixels = Math.max(0, toPixels(shadow.blurRadius));
        int layers = blurPixels <= 0 ? 0 : Math.max(1, Math.min(4, (blurPixels + 2) / 3));
        int layerStep = layers <= 0 ? 0 : Math.max(1, (int) Math.ceil((double) blurPixels / layers));
        int totalWeight = layers <= 0 ? 1 : (layers + 1) * (layers + 2) / 2;
        int savedAlpha = g.alpha;
        int savedForeColor = g.foreColor;

        try {
            for (int layer = layers; layer >= 0; layer--) {
                int blurOffset = layer * layerStep;
                int inset = -(spread + blurOffset);
                int shadowX = geom.borderBox.x + dx + inset;
                int shadowY = geom.borderBox.y + dy + inset;
                int shadowW = geom.borderBox.width - inset * 2;
                int shadowH = geom.borderBox.height - inset * 2;

                if (shadowW <= 0 || shadowH <= 0) {
                    continue;
                }

                int weight = layers - layer + 1;
                int layerAlpha = Math.max(1, Math.min(255,
                    (int) Math.round(((shadow.alpha * weight) / (double) totalWeight) * SHADOW_ALPHA_GAIN)));
                CornerRadii shadowRadii = geom.borderRadii
                    .offset(spread + blurOffset)
                    .clamp(shadowW, shadowH);

                g.alpha = (layerAlpha & 0xFF) << 24;
                g.foreColor = shadow.color;
                g.drawRRect(
                    new RRect(
                        shadowX,
                        shadowY,
                        shadowW,
                        shadowH,
                        new double[] {
                            shadowRadii.topLeftX, shadowRadii.topLeftY,
                            shadowRadii.topRightX, shadowRadii.topRightY,
                            shadowRadii.bottomRightX, shadowRadii.bottomRightY,
                            shadowRadii.bottomLeftX, shadowRadii.bottomLeftY
                        }
                    ),
                    shadow.color,
                    true
                );
            }
        } finally {
            g.alpha = savedAlpha;
            g.foreColor = savedForeColor;
        }
    }

    private static int toPixels(double value) {
        return (int) Math.round(UnitsConverter.toPixels(Control.DP + value));
    }

    private static void drawStyledHorizontal(Graphics g, BorderSide side, int x1, int y, int x2) {
        if (side == null || !side.isVisible() || x2 < x1) {
            return;
        }
        g.foreColor = side.color;
        if (side.style == BorderSide.Style.DASHED) {
            drawDashedLine(g, x1, y, x2, y);
        } else if (side.style == BorderSide.Style.DOTTED) {
            drawDottedLine(g, x1, y, x2, y);
        } else {
            g.drawLine(x1, y, x2, y);
        }
    }

    private static void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2) {
        int dash = 4;
        int gap = 3;
        int dx = x2 - x1;
        int dy = y2 - y1;
        int len = Math.max(Math.abs(dx), Math.abs(dy));
        if (len == 0) {
            g.drawLine(x1, y1, x2, y2);
            return;
        }

        for (int i = 0; i < len; i += dash + gap) {
            int sx = x1 + (dx * i) / len;
            int sy = y1 + (dy * i) / len;
            int ex = x1 + (dx * Math.min(i + dash, len)) / len;
            int ey = y1 + (dy * Math.min(i + dash, len)) / len;
            g.drawLine(sx, sy, ex, ey);
        }
    }

    private static void drawDottedLine(Graphics g, int x1, int y1, int x2, int y2) {
        int step = 3;
        int dx = x2 - x1;
        int dy = y2 - y1;
        int len = Math.max(Math.abs(dx), Math.abs(dy));
        if (len == 0) {
            g.setPixel(x1, y1);
            return;
        }

        for (int i = 0; i < len; i += step) {
            int px = x1 + (dx * i) / len;
            int py = y1 + (dy * i) / len;
            g.setPixel(px, py);
        }
    }

}
