// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;

public class Double4D extends Number4D implements Comparable<Double4D> {
  public static final double POSITIVE_INFINITY = 1.0 / 0.0;
  public static final double NEGATIVE_INFINITY = -1.0 / 0.0;
  public static final double NaN = 0.0d / 0.0;
  public static final double MAX_VALUE = 0x1.fffffffffffffP+1023; // 1.7976931348623157e+308
  public static final double MIN_NORMAL = 0x1.0p-1022; // 2.2250738585072014E-308
  public static final double MIN_VALUE = 0x0.0000000000001P-1022; // 4.9e-324
  public static final int MAX_EXPONENT = 1023;
  public static final int MIN_EXPONENT = -1022;
  public static final int SIZE = 64;
  public static final int BYTES = SIZE / Byte.SIZE;
  public static final Class<Double> TYPE = Double.class;
  double v;

  public Double4D(double v) {
    this.v = v;
  }

  public Double4D(String s) throws NumberFormatException {
    this.v = parseDouble(s);
  }

  @Override
  public double doubleValue() {
    return v;
  }

  @Override
  public boolean equals(Object o) {
    return o != null && o instanceof Double4D && ((Double4D) o).v == this.v;
  }

  @Override
  public int hashCode() // same of java to keep compatibility
  {
    long r = Convert.doubleToLongBits(v);
    if ((r & 0x7FF0000000000000L) == 0x7FF0000000000000L && (r & 0x000FFFFFFFFFFFFFL) != 0L) {
      r = 0x7ff8000000000000L;
    }
    return (int) (r ^ (r >>> 32));
  }

  @Override
  public String toString() {
    return String.valueOf(v);
  }

  public static Double4D valueOf(double d) {
    return new Double4D(d);
  }

  public static Double4D valueOf(String s) throws NumberFormatException {
    try {
      return new Double4D(Convert.toDouble(s));
    } catch (InvalidNumberException ine) {
      throw new NumberFormatException(ine.getMessage());
    }
  }

  public static double parseDouble(String str) throws NumberFormatException {
    try {
      return Convert.toDouble(str);
    } catch (InvalidNumberException e) {
      throw new NumberFormatException(e.getMessage());
    }
  }

  public static boolean isNaN(double v) {
    return v == Convert.DOUBLE_NAN_VALUE;
  }

  public static boolean isInfinite(double v) {
    return v == Convert.DOUBLE_POSITIVE_INFINITY_VALUE || v == Convert.DOUBLE_NEGATIVE_INFINITY_VALUE;
  }

  public static String toString(double d) {
    return Convert.toString(d);
  }

  public boolean isInfinite() {
    return v == Convert.DOUBLE_POSITIVE_INFINITY_VALUE || v == Convert.DOUBLE_NEGATIVE_INFINITY_VALUE;
  }

  public boolean isNaN() {
    return v == Convert.DOUBLE_NAN_VALUE;
  }

  /**
   * Convert the double to the IEEE 754 floating-point "double format" bit
   * layout. Bit 63 (the most significant) is the sign bit, bits 62-52
   * (masked by 0x7ff0000000000000L) represent the exponent, and bits 51-0
   * (masked by 0x000fffffffffffffL) are the mantissa. This function
   * collapses all versions of NaN to 0x7ff8000000000000L. The result of this
   * function can be used as the argument to
   * <code>Double.longBitsToDouble(long)</code> to obtain the original
   * <code>double</code> value.
   *
   * @param value the <code>double</code> to convert
   * @return the bits of the <code>double</code>
   * @see #longBitsToDouble(long)
   */
  public static long doubleToLongBits(double value) {
    if (isNaN(value)) {
      return 0x7ff8000000000000L;
    } else {
      return doubleToRawLongBits(value);
    }
  }

  /**
   * Convert the double to the IEEE 754 floating-point "double format" bit
   * layout. Bit 63 (the most significant) is the sign bit, bits 62-52
   * (masked by 0x7ff0000000000000L) represent the exponent, and bits 51-0
   * (masked by 0x000fffffffffffffL) are the mantissa. This function
   * leaves NaN alone, rather than collapsing to a canonical value. The
   * result of this function can be used as the argument to
   * <code>Double.longBitsToDouble(long)</code> to obtain the original
   * <code>double</code> value.
   *
   * @param value the <code>double</code> to convert
   * @return the bits of the <code>double</code>
   * @see #longBitsToDouble(long)
   */
  public static long doubleToRawLongBits(double value) {
    return Convert.doubleToLongBits(value);
  }

  /**
   * Behaves like <code>new Double(x).compareTo(new Double(y))</code>; in
   * other words this compares two doubles, special casing NaN and zero,
   * without the overhead of objects.
   *
   * @param x the first double to compare
   * @param y the second double to compare
   * @return the comparison
   * @since 1.4
   */
  public static int compare(double x, double y) {
    // handle the easy cases:
    if (x < y) {
      return -1;
    }
    if (x > y) {
      return 1;
    }

    // handle equality respecting that 0.0 != -0.0 (hence not using x == y):
    long lx = doubleToRawLongBits(x);
    long ly = doubleToRawLongBits(y);
    if (lx == ly) {
      return 0;
    }

    // handle NaNs:
    if (x != x) {
      return (y != y) ? 0 : 1;
    } else if (y != y) {
      return -1;
    }

    // handle +/- 0.0
    return (lx < ly) ? -1 : 1;
  }

  @Override
  public int intValue() {
    return (int) v;
  }

  @Override
  public long longValue() {
    return (long) v;
  }

  @Override
  public int compareTo(Double4D o) {
	return Double4D.compare(this.v, o.v);
  }
}
