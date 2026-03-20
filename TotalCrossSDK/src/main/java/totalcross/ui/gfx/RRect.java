// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.gfx;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import java.util.Arrays;

/**
 * Describes a rounded rectangle.
 * <p>
 * The {@link #radii} array is always normalized to 8 components, storing
 * {@code rx, ry} for each corner in clockwise order:
 * top-left, top-right, bottom-right, bottom-left.
 * <p>
 * The constructor accepts radii arrays with 1, 2, 4, or 8 values:
 * <ul>
 * <li>1 value: same {@code rx} and {@code ry} for all corners</li>
 * <li>2 values: first is {@code rx}, second is {@code ry}, repeated for all corners</li>
 * <li>4 values: one radius per corner, with {@code rx == ry}</li>
 * <li>8 values: explicit {@code rx, ry} for each corner</li>
 * </ul>
 */
public class RRect extends Rect {
    private static final double[] ZERO_RADII = new double[] {0, 0, 0, 0, 0, 0, 0, 0};

    /**
     * Corner radii normalized to 8 components in the order
     * top-left, top-right, bottom-right, bottom-left, each one as {@code rx, ry}.
     */
    public final double[] radii;

    /**
     * Creates a rounded rectangle.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width
     * @param height the height
     * @param radii the corner radii in 1, 2, 4, or 8-component form
     */
    public RRect(int x, int y, int width, int height, double[] radii) {
        super(x, y, width, height);
        this.radii = normalizeRadii(radii);
    }

    /**
     * Creates a rounded rectangle with the same radius on every corner axis.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width
     * @param height the height
     * @param radius the radius repeated across all corner components
     */
    public RRect(int x, int y, int width, int height, double radius) {
        this(x, y, width, height, new double[] {radius});
    }

    /**
     * Creates a rounded rectangle with zero radii on every corner.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width
     * @param height the height
     */
    public RRect(int x, int y, int width, int height) {
        this(x, y, width, height, 0d);
    }

    private static double[] normalizeRadii(double[] radii) {
        if (radii == null) {
            double[] copy = new double[8];
            System.arraycopy(ZERO_RADII, 0, copy, 0, 8);
            return copy;
        }

        switch (radii.length) {
            case 1:
                return new double[] {
                    radii[0], radii[0],
                    radii[0], radii[0],
                    radii[0], radii[0],
                    radii[0], radii[0]
                };
            case 2:
                return new double[] {
                    radii[0], radii[1],
                    radii[0], radii[1],
                    radii[0], radii[1],
                    radii[0], radii[1]
                };
            case 4:
                return new double[] {
                    radii[0], radii[0],
                    radii[1], radii[1],
                    radii[2], radii[2],
                    radii[3], radii[3]
                };
            case 8:
                double[] copy = new double[8];
                System.arraycopy(radii, 0, copy, 0, 8);
                return copy;
            default:
                throw new IllegalArgumentException(
                    "RRect.radii must have length 1, 2, 4, or 8"
                );
        }
    }

    /**
     * Returns whether the given point is inside this rounded rectangle.
     */
    @Override
    public boolean contains(int xx, int yy) {
        double px = xx + 0.5d;
        double py = yy + 0.5d;
        double left = x;
        double top = y;
        double right = x + width;
        double bottom = y + height;

        if (px < left || px >= right || py < top || py >= bottom) {
            return false;
        }

        return containsCorner(px, py, left, top, radii[0], radii[1], 0)
            && containsCorner(px, py, right, top, radii[2], radii[3], 1)
            && containsCorner(px, py, right, bottom, radii[4], radii[5], 2)
            && containsCorner(px, py, left, bottom, radii[6], radii[7], 3);
    }

    @ReplacedByNativeOnDeploy
    private static boolean containsCorner(double px, double py, double x, double y, double rx, double ry, int corner) {
        if (rx <= 0 || ry <= 0) {
            return true;
        }

        double cx = (corner == 0 || corner == 3) ? x + rx : x - rx;
        double cy = (corner == 0 || corner == 1) ? y + ry : y - ry;
        boolean inCornerBox =
            (corner == 0 && px < x + rx && py < y + ry) ||
            (corner == 1 && px >= x - rx && py < y + ry) ||
            (corner == 2 && px >= x - rx && py >= y - ry) ||
            (corner == 3 && px < x + rx && py >= y - ry);

        if (!inCornerBox) {
            return true;
        }

        double dx = (px - cx) / rx;
        double dy = (py - cy) / ry;
        return dx * dx + dy * dy <= 1d;
    }

    /**
     * Computes the horizontal span occupied by this rounded rectangle for a given scanline.
     *
     * @param yy the scanline y coordinate
     * @return the horizontal span for the scanline, or an invalid span if the scanline does not intersect this shape
     */
    public Span horizontalSpan(int yy) {
        double py = yy + 0.5d;
        double left = x;
        double top = y;
        double right = x + width;
        double bottom = y + height;

        if (py < top || py >= bottom) {
            return new Span(1, 0);
        }

        double start = Math.max(left, leftBoundForY(py, left, top, bottom, radii[0], radii[1], radii[6], radii[7]));
        double end = Math.min(right, rightBoundForY(py, right, top, bottom, radii[2], radii[3], radii[4], radii[5]));

        return new Span((int) Math.ceil(start - 0.5d), (int) Math.floor(end - 0.5d));
    }

    @ReplacedByNativeOnDeploy
    private static double leftBoundForY(double py, double left, double top, double bottom,
        double topRx, double topRy, double bottomRx, double bottomRy) {
        if (topRx > 0 && topRy > 0 && py < top + topRy) {
            return ellipseLeftBound(left + topRx, top + topRy, topRx, topRy, py);
        }
        if (bottomRx > 0 && bottomRy > 0 && py >= bottom - bottomRy) {
            return ellipseLeftBound(left + bottomRx, bottom - bottomRy, bottomRx, bottomRy, py);
        }
        return left;
    }

    @ReplacedByNativeOnDeploy
    private static double rightBoundForY(double py, double right, double top, double bottom,
        double topRx, double topRy, double bottomRx, double bottomRy) {
        if (topRx > 0 && topRy > 0 && py < top + topRy) {
            return ellipseRightBound(right - topRx, top + topRy, topRx, topRy, py);
        }
        if (bottomRx > 0 && bottomRy > 0 && py >= bottom - bottomRy) {
            return ellipseRightBound(right - bottomRx, bottom - bottomRy, bottomRx, bottomRy, py);
        }
        return right;
    }

    @ReplacedByNativeOnDeploy
    private static double ellipseLeftBound(double cx, double cy, double rx, double ry, double py) {
        return cx - ellipseDx(rx, ry, py - cy);
    }

    @ReplacedByNativeOnDeploy
    private static double ellipseRightBound(double cx, double cy, double rx, double ry, double py) {
        return cx + ellipseDx(rx, ry, py - cy);
    }

    @ReplacedByNativeOnDeploy
    private static double ellipseDx(double rx, double ry, double dy) {
        if (rx <= 0 || ry <= 0) {
            return 0d;
        }
        double t = 1d - (dy * dy) / (ry * ry);
        if (t <= 0d) {
            return 0d;
        }
        return rx * Math.sqrt(t);
    }

    /**
     * Returns a modified copy of this rounded rectangle.
     */
    @Override
    public RRect modifiedBy(int deltaX, int deltaY, int deltaW, int deltaH) {
        return new RRect(x + deltaX, y + deltaY, width + deltaW, height + deltaH, radii);
    }

    /**
     * Compares this rounded rectangle with another object.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RRect)) {
            return false;
        }
        RRect r = (RRect) other;
        return r.x == this.x
            && r.y == this.y
            && r.width == this.width
            && r.height == this.height
            && Arrays.equals(r.radii, this.radii);
    }

    /**
     * Returns the hash code for this rounded rectangle.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(radii);
        return result;
    }

    /**
     * Returns a string representation of this rounded rectangle.
     */
    @Override
    public String toString() {
        return super.toString() + ",radii=" + Arrays.toString(radii);
    }
}
