// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

/**
 * Represents the x and y radii for each corner of a box.
 */
public final class CornerRadii {
    /** Constant for zero radii on all corners. */
    public static final CornerRadii ZERO = new CornerRadii(0, 0, 0, 0, 0, 0, 0, 0);

    public final double topLeftX;
    public final double topLeftY;
    public final double topRightX;
    public final double topRightY;
    public final double bottomRightX;
    public final double bottomRightY;
    public final double bottomLeftX;
    public final double bottomLeftY;

    private CornerRadii(
        double topLeftX,
        double topLeftY,
        double topRightX,
        double topRightY,
        double bottomRightX,
        double bottomRightY,
        double bottomLeftX,
        double bottomLeftY
    ) {
        validateNonNegative("topLeftX", topLeftX);
        validateNonNegative("topLeftY", topLeftY);
        validateNonNegative("topRightX", topRightX);
        validateNonNegative("topRightY", topRightY);
        validateNonNegative("bottomRightX", bottomRightX);
        validateNonNegative("bottomRightY", bottomRightY);
        validateNonNegative("bottomLeftX", bottomLeftX);
        validateNonNegative("bottomLeftY", bottomLeftY);
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
    }

    /**
     * Creates radii with the same value on all corners.
     */
    public static CornerRadii all(double value) {
        return of(value, value, value, value);
    }

    /**
     * Creates radii with one value per corner, using the same value for x and y.
     */
    public static CornerRadii of(double topLeft, double topRight, double bottomRight, double bottomLeft) {
        return of(
            topLeft, topLeft,
            topRight, topRight,
            bottomRight, bottomRight,
            bottomLeft, bottomLeft
        );
    }

    /**
     * Creates radii with explicit x and y values for each corner.
     */
    public static CornerRadii of(
        double topLeftX,
        double topLeftY,
        double topRightX,
        double topRightY,
        double bottomRightX,
        double bottomRightY,
        double bottomLeftX,
        double bottomLeftY
    ) {
        if (topLeftX == 0 && topLeftY == 0
            && topRightX == 0 && topRightY == 0
            && bottomRightX == 0 && bottomRightY == 0
            && bottomLeftX == 0 && bottomLeftY == 0) {
            return ZERO;
        }
        return new CornerRadii(
            topLeftX, topLeftY,
            topRightX, topRightY,
            bottomRightX, bottomRightY,
            bottomLeftX, bottomLeftY
        );
    }

    /**
     * Returns whether all radii are zero.
     */
    public boolean isZero() {
        return this == ZERO
            || (topLeftX == 0 && topLeftY == 0
                && topRightX == 0 && topRightY == 0
                && bottomRightX == 0 && bottomRightY == 0
                && bottomLeftX == 0 && bottomLeftY == 0);
    }

    /**
     * Returns a copy with the given delta applied to all radii, clamped at zero.
     */
    public CornerRadii offset(double delta) {
        return of(
            Math.max(0, topLeftX + delta),
            Math.max(0, topLeftY + delta),
            Math.max(0, topRightX + delta),
            Math.max(0, topRightY + delta),
            Math.max(0, bottomRightX + delta),
            Math.max(0, bottomRightY + delta),
            Math.max(0, bottomLeftX + delta),
            Math.max(0, bottomLeftY + delta)
        );
    }

    private static void validateNonNegative(String name, double value) {
        if (value < 0) {
            throw new IllegalArgumentException("CornerRadii " + name + " must be >= 0: " + value);
        }
    }
}
