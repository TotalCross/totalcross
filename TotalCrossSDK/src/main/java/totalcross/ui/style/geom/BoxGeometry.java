// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.geom;

import totalcross.ui.Insets;
import totalcross.ui.gfx.RRect;
import totalcross.ui.gfx.Rect;
import totalcross.ui.style.model.BorderSide;
import totalcross.ui.style.model.BoxBorder;
import totalcross.ui.style.model.BoxLayout;
import totalcross.ui.style.model.BoxPaint;
import totalcross.ui.style.model.BoxShape;
import totalcross.ui.style.model.BoxStyle;
import totalcross.ui.style.model.CornerRadii;

/**
 * Resolves the concrete rectangles and corner radii used to paint a box.
 */
public final class BoxGeometry {
    public final Rect borderBox = new Rect();
    public final Rect paddingBox = new Rect();
    public final Rect contentBox = new Rect();

    public CornerRadii borderRadii = CornerRadii.ZERO;
    public CornerRadii paddingRadii = CornerRadii.ZERO;
    public CornerRadii contentRadii = CornerRadii.ZERO;

    /**
     * Returns the border box as a rounded rectangle.
     */
    public RRect borderRRect() {
        return new RRect(
                borderBox.x,
                borderBox.y,
                borderBox.width,
                borderBox.height,
                new double[] {
                        borderRadii.topLeftX, borderRadii.topLeftY,
                        borderRadii.topRightX, borderRadii.topRightY,
                        borderRadii.bottomRightX, borderRadii.bottomRightY,
                        borderRadii.bottomLeftX, borderRadii.bottomLeftY
                });
    }

    /**
     * Returns the padding box as a rounded rectangle.
     */
    public RRect paddingRRect() {
        return new RRect(
                paddingBox.x,
                paddingBox.y,
                paddingBox.width,
                paddingBox.height,
                new double[] {
                        paddingRadii.topLeftX, paddingRadii.topLeftY,
                        paddingRadii.topRightX, paddingRadii.topRightY,
                        paddingRadii.bottomRightX, paddingRadii.bottomRightY,
                        paddingRadii.bottomLeftX, paddingRadii.bottomLeftY
                });
    }

    /**
     * Returns the content box as a rounded rectangle.
     */
    public RRect contentRRect() {
        return new RRect(
                contentBox.x,
                contentBox.y,
                contentBox.width,
                contentBox.height,
                new double[] {
                        contentRadii.topLeftX, contentRadii.topLeftY,
                        contentRadii.topRightX, contentRadii.topRightY,
                        contentRadii.bottomRightX, contentRadii.bottomRightY,
                        contentRadii.bottomLeftX, contentRadii.bottomLeftY
                });
    }

    /**
     * Computes box geometry for the given bounds and style.
     */
    public static BoxGeometry compute(int x, int y, int w, int h, BoxStyle style) {
        BoxLayout layout = style == null ? null : style.layout;
        BoxPaint paint = style == null ? null : style.paint;
        BoxShape shape = style == null ? null : style.shape;
        BoxGeometry g = new BoxGeometry();
        int safeW = Math.max(0, w);
        int safeH = Math.max(0, h);
        Insets padding = layout == null ? null : layout.padding;
        BoxBorder border = paint == null ? null : paint.border;
        Insets borderInsets = border == null ? new Insets() : border.getInsets();
        int align = resolveStrokeAlign(border);

        int borderBoxX = x;
        int borderBoxY = y;
        int borderBoxW = safeW;
        int borderBoxH = safeH;

        if (align == BorderSide.Align.CENTER) {
            borderBoxX -= borderInsets.left / 2;
            borderBoxY -= borderInsets.top / 2;
            borderBoxW += (borderInsets.left + borderInsets.right) / 2;
            borderBoxH += (borderInsets.top + borderInsets.bottom) / 2;
        } else if (align == BorderSide.Align.OUTSIDE) {
            borderBoxX -= borderInsets.left;
            borderBoxY -= borderInsets.top;
            borderBoxW += borderInsets.left + borderInsets.right;
            borderBoxH += borderInsets.top + borderInsets.bottom;
        }

        g.borderBox.set(borderBoxX, borderBoxY, Math.max(0, borderBoxW), Math.max(0, borderBoxH));

        g.borderRadii = clampRadii(
                shape == null ? CornerRadii.ZERO : shape.radii,
                g.borderBox.width,
                g.borderBox.height);

        insetRect(g.borderBox, borderInsets, g.paddingBox);
        g.paddingRadii = clampRadii(
                insetRadii(g.borderRadii, borderInsets),
                g.paddingBox.width,
                g.paddingBox.height);

        Insets safePadding = padding == null ? new Insets() : padding;
        insetRect(g.paddingBox, safePadding, g.contentBox);
        g.contentRadii = clampRadii(
                insetRadii(g.paddingRadii, safePadding),
                g.contentBox.width,
                g.contentBox.height);

        return g;
    }

    /**
     * Insets the source rectangle into the destination rectangle.
     */
    public static void insetRect(Rect src, Insets insets, Rect dst) {
        int x = src.x + insets.left;
        int y = src.y + insets.top;
        int w = src.width - insets.left - insets.right;
        int h = src.height - insets.top - insets.bottom;
        dst.set(x, y, Math.max(0, w), Math.max(0, h));
    }

    /**
     * Insets corner radii according to the provided insets.
     */
    public static CornerRadii insetRadii(CornerRadii src, Insets insets) {
        if (src == null) {
            return CornerRadii.ZERO;
        }
        return CornerRadii.of(
                Math.max(0, src.topLeftX - insets.left),
                Math.max(0, src.topLeftY - insets.top),
                Math.max(0, src.topRightX - insets.right),
                Math.max(0, src.topRightY - insets.top),
                Math.max(0, src.bottomRightX - insets.right),
                Math.max(0, src.bottomRightY - insets.bottom),
                Math.max(0, src.bottomLeftX - insets.left),
                Math.max(0, src.bottomLeftY - insets.bottom));
    }

    /**
     * Clamps corner radii so they fit inside the given dimensions.
     */
    public static CornerRadii clampRadii(CornerRadii radii, int w, int h) {
        if (radii == null) {
            return CornerRadii.ZERO;
        }
        double maxX = Math.max(0, w / 2.0);
        double maxY = Math.max(0, h / 2.0);
        return CornerRadii.of(
                Math.min(radii.topLeftX, maxX),
                Math.min(radii.topLeftY, maxY),
                Math.min(radii.topRightX, maxX),
                Math.min(radii.topRightY, maxY),
                Math.min(radii.bottomRightX, maxX),
                Math.min(radii.bottomRightY, maxY),
                Math.min(radii.bottomLeftX, maxX),
                Math.min(radii.bottomLeftY, maxY));
    }

    private static int resolveStrokeAlign(BoxBorder border) {
        if (border == null || border.top == null) {
            return BorderSide.Align.INSIDE;
        }
        return border.top.align;
    }
}
