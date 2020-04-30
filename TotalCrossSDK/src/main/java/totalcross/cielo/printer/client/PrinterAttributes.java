// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.cielo.printer.client;

public class PrinterAttributes {

    /**
     * Printing alignment, value must be one of PrinterAttributes.VAL_ALIGN_CENTER,
     * PrinterAttributes.VAL_ALIGN_LEFT or PrinterAttributes.VAL_ALIGN_RIGHT
     * 
     * @see PrinterAttributes.VAL_ALIGN_CENTER
     * @see PrinterAttributes.VAL_ALIGN_LEFT
     * @see PrinterAttributes.VAL_ALIGN_RIGHT
     */
    public static final String KEY_ALIGN = "key_attributes_align";

    /**
     * Text size, must be an integer value
     */
    public static final String KEY_TEXT_SIZE = "key_attributes_textsize";

    /**
     * Text font, must be an integer between 0 and 8, where each value is a
     * different font
     */
    public static final String KEY_TYPEFACE = "key_attributes_typeface";

    /**
     * Left margin, must be an integer value
     */
    public static final String KEY_MARGIN_LEFT = "key_attributes_marginleft";

    /**
     * Right margin, must be an integer value
     */
    public static final String KEY_MARGIN_RIGHT = "key_attributes_marginright";

    /**
     * Top margin, must be an integer value
     */
    public static final String KEY_MARGIN_TOP = "key_attributes_margintop";

    /**
     * Bottom margin, must be an integer value
     */
    public static final String KEY_MARGIN_BOTTOM = "key_attributes_marginbottom";

    /**
     * Spacing between consecutive lines, must be an integer value
     */
    public static final String KEY_LINE_SPACE = "key_attributes_linespace";

    /**
     * Used when printing multiple columns, to choose the weight of each column,
     * must be an integer value
     */
    public static final String KEY_WEIGHT = "key_attributes_weight";

    public static final int VAL_ALIGN_CENTER = 0;
    public static final int VAL_ALIGN_LEFT = 1;
    public static final int VAL_ALIGN_RIGHT = 2;
}
