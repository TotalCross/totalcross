// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import totalcross.ui.gfx.Color;

/**
 * Describes a single box shadow layer.
 */
public final class Shadow {
    /** Constant for an empty shadow. */
    public static final Shadow NONE = new Shadow(0, 0, 0, 0, 0, Color.BLACK);

    public final double dx;
    public final double dy;
    public final double blurRadius;
    public final int alpha;
    public final double spread;
    public final int color;

    private Shadow(double dx, double dy, double blurRadius, int alpha, double spread, int color) {
        this.dx = dx;
        this.dy = dy;
        this.blurRadius = blurRadius;
        this.alpha = alpha;
        this.spread = spread;
        this.color = color;
    }

    /**
     * Creates a shadow using black as the color.
     */
    public static Shadow of(double dx, double dy, double blurRadius, int alpha, double spread) {
        return of(dx, dy, blurRadius, alpha, spread, Color.BLACK);
    }

    /**
     * Creates a shadow.
     */
    public static Shadow of(double dx, double dy, double blurRadius, int alpha, double spread, int color) {
        if (dx == 0 && dy == 0 && blurRadius == 0 && alpha == 0 && spread == 0 && color == Color.BLACK) {
            return NONE;
        }
        return new Shadow(dx, dy, blurRadius, alpha, spread, color);
    }
}
