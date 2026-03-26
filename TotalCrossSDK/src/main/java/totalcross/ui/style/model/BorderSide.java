// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import totalcross.ui.Control;
import totalcross.util.UnitsConverter;

/**
 * Describes a single side of a box border.
 */
public final class BorderSide {
    /**
     * Supported border styles.
     */
    public static final class Style {
        public static final int NONE = 0;
        public static final int SOLID = 1;
        public static final int DASHED = 2;
        public static final int DOTTED = 3;

        private Style() {
        }
    }

    /**
     * Supported border alignment modes.
     */
    public static final class Align {
        public static final int INSIDE = 0;
        public static final int CENTER = 1;
        public static final int OUTSIDE = 2;

        private Align() {
        }
    }

    public final double width;
    public final int style;
    public final int color;

    public final int align;

    /** Constant for an invisible border side. */
    public static final BorderSide NONE = with(0, Style.NONE, 0);

    private BorderSide(double width, int style, int color, int align) {
        validateStyle(style);
        validateAlign(align);
        this.width = UnitsConverter.toPixels(Control.DP + width);
        this.style = style;
        this.color = color;
        this.align = align;
    }

    /**
     * Creates a border side using inside alignment.
     */
    public static BorderSide with(double width, int style, int color) {
        return with(width, style, color, Align.INSIDE);
    }

    /**
     * Creates a border side.
     */
    public static BorderSide with(double width, int style, int color, int align) {
        return new BorderSide(width, style, color, align);
    }

    /**
     * Returns whether this side should be painted.
     */
    public boolean isVisible() {
        return style != Style.NONE && width > 0;
    }

    private static void validateStyle(int style) {
        if (!isValidStyle(style)) {
            throw new IllegalArgumentException("Invalid BorderSide.Style: " + style);
        }
    }

    private static void validateAlign(int align) {
        if (!isValidAlign(align)) {
            throw new IllegalArgumentException("Invalid BorderSide.Align: " + align);
        }
    }

    /**
     * Returns whether the given value is a valid border style.
     */
    public static boolean isValidStyle(int style) {
        return style == Style.NONE
            || style == Style.SOLID
            || style == Style.DASHED
            || style == Style.DOTTED;
    }

    /**
     * Returns whether the given value is a valid border alignment.
     */
    public static boolean isValidAlign(int align) {
        return align == Align.INSIDE
            || align == Align.CENTER
            || align == Align.OUTSIDE;
    }

}
