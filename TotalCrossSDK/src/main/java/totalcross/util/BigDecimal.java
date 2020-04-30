// Copyright (C) 1999-2006 Free Software Foundation, Inc.
// Copyright (C) 2009-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
/* java.math.BigDecimal -- Arbitrary precision decimals.
   Copyright (C) 1999, 2000, 2001, 2003, 2005, 2006 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package totalcross.util;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;

/**
 * Arbitrary-precision signed decimal numbers. A <code>BigDecimal</code> consists of an arbitrary precision integer <i>unscaled value</i> and a 
 * 32-bit integer <i>scale</i>. If zero or positive, the scale is the number of digits to the right of the decimal point. If negative, the unscaled 
 * value of the number is multiplied by ten to the power of the negation of the scale. The value of the number represented by the 
 * <code>BigDecimal</code> is therefore <tt>(unscaledValue &times; 10<sup>-scale</sup>)</tt>.
 */
public class BigDecimal implements Comparable {
  private BigInteger intVal;
  private int scale;
  private int precision;

  /**
   * The constant zero as a <code>BigDecimal</code> with scale zero.
   */
  public static final BigDecimal ZERO = new BigDecimal(BigInteger.ZERO, 0);

  /**
   * The constant one as a <code>BigDecimal</code> with scale zero.
   */
  public static final BigDecimal ONE = new BigDecimal(BigInteger.ONE, 0);

  /**
   * The constant ten as a <code>BigDecimal</code> with scale zero.
   */
  public static final BigDecimal TEN = new BigDecimal(BigInteger.TEN, 0);

  /** 
   * Rounding mode to round away from zero. 
   */
  public static final int ROUND_UP = 0;

  /** 
   * Rounding mode to round towards zero. 
   */
  public static final int ROUND_DOWN = 1;

  /** 
   * Rounding mode to round towards positive infinity. 
   */
  public static final int ROUND_CEILING = 2;

  /** 
   * Rounding mode to round towards negative infinity.
   */
  public static final int ROUND_FLOOR = 3;

  /** 
   * Rounding mode to round towards the "nearest neighbor" unless both neighbors are equidistant, in which case round up.
   */
  public static final int ROUND_HALF_UP = 4;

  /** 
   * Rounding mode to round towards the "nearest neighbor" unless both neighbors are equidistant, in which case round down.
   */
  public static final int ROUND_HALF_DOWN = 5;

  /** 
   * Rounding mode to round towards the "nearest neighbor" unless both neighbors are equidistant, in which case round towards the even neighbor.
   */
  public static final int ROUND_HALF_EVEN = 6;

  /** 
   * Rounding mode to assert that the requested operation has an exact result, hence no rounding is necessary. 
   */
  public static final int ROUND_UNNECESSARY = 7;

  /**
   * Translates the string representation of a <code>BigDecimal</code> into a <code>BigDecimal</code> object.
   *
   * @param num The <code>BigDecimal</code> as a string.
   * @throws InvalidNumberException If the given number is not a valid representation of a <code>BigDecimal</code>.
   */
  public BigDecimal(String num) throws InvalidNumberException {
    int len = num.length();
    int start = 0, point = 0;
    int dot = -1;
    boolean negative = false;
    if (num.charAt(0) == '+') {
      ++start;
      ++point;
    } else if (num.charAt(0) == '-') {
      ++start;
      ++point;
      negative = true;
    }

    while (point < len) {
      char c = num.charAt(point);
      if (c == '.') {
        if (dot >= 0) {
          throw new InvalidNumberException("multiple '.'s in number " + num);
        }
        dot = point;
      } else if (c == 'e' || c == 'E') {
        break;
      } else if (Convert.digitOf(c, 10) < 0) {
        throw new InvalidNumberException("unrecognized character: " + c);
      }
      ++point;
    }

    String val;
    if (dot >= 0) {
      val = num.substring(start, dot) + num.substring(dot + 1, point);
      scale = point - 1 - dot;
    } else {
      val = num.substring(start, point);
      scale = 0;
    }
    if (val.length() == 0) {
      throw new InvalidNumberException("no digits seen");
    }

    if (negative) {
      val = "-" + val;
    }
    intVal = new BigInteger(val);

    // Now parse exponent.
    if (point < len) {
      point++;
      if (num.charAt(point) == '+') {
        point++;
      }

      if (point >= len) {
        throw new InvalidNumberException("no exponent following e or E");
      }

      try {
        if (dot >= 0) {
          scale -= Convert.toInt(num.substring(point));
        } else {
          scale += Convert.toInt(num.substring(point));
        }
      } catch (InvalidNumberException ex) {
        throw new InvalidNumberException("malformed exponent");
      }
    }
  }

  /**
   * Constructs a new <code>BigDecimal</code> whose unscaled value is <code>val</code> and whose scale is zero.
   * 
   * @param val The value of the new <code>BigDecimal</code>.
   */
  public BigDecimal(int val) {
    this.intVal = BigInteger.valueOf(val);
  }

  /**
   * Constructs a new <code>BigDecimal</code> whose unscaled value is <code>val</code> and whose scale is zero.
   * 
   * @param val The value of the new <code>BigDecimal</code>.
   */
  public BigDecimal(long val) {
    this.intVal = BigInteger.valueOf(val);
  }

  /**
   * Constructs a new <code>BigDecimal</code> whose unscaled value is <code>num</code> and whose scale is zero.
   * 
   * @param num The value of the new <code>BigDecimal</code>.
   */
  public BigDecimal(BigInteger num) {
    this(num, 0);
  }

  /**
   * Constructs a new <code>BigDecimal</code> whose unscaled value is <code>num</code> and with the given scale.
   * 
   * @param num The unscaled value of the new <code>BigDecimal</code>.
   * @param scale The given scale.
   */
  public BigDecimal(BigInteger num, int scale) {
    this.intVal = num;
    this.scale = scale;
  }

  /**
   * Translates a <code>double</code> into a <code>BigDecimal</code> which is the exact decimal representation of the <code>double</code>s binary 
   * floating-point value. The scale of the returned <code>BigDecimal</code> is the smallest value such that 
   * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
   * 
   * @param num The number to be converted
   * @throws InvalidNumberException If the number passed is infinite or NaN.
   */
  public BigDecimal(double num) throws InvalidNumberException {
    long bits = Convert.doubleToLongBits(num);
    if (bits == Convert.DOUBLE_POSITIVE_INFINITY_BITS || bits == Convert.DOUBLE_NEGATIVE_INFINITY_BITS
        || bits == Convert.DOUBLE_NAN_BITS) {
      throw new InvalidNumberException("invalid argument: " + num);
      // Note we can't convert NUM to a String and then use the
      // String-based constructor. The BigDecimal documentation makes
      // it clear that the two constructors work differently.
    }

    final int mantissaBits = 52;
    final int exponentBits = 11;
    final long mantMask = (1L << mantissaBits) - 1;
    final long expMask = (1L << exponentBits) - 1;

    long mantissa = bits & mantMask;
    long exponent = (bits >>> mantissaBits) & expMask;
    boolean denormal = exponent == 0;

    // Correct the exponent for the bias.
    exponent -= denormal ? 1022 : 1023;

    // Now correct the exponent to account for the bits to the right
    // of the decimal.
    exponent -= mantissaBits;
    // Ordinary numbers have an implied leading '1' bit.
    if (!denormal) {
      mantissa |= (1L << mantissaBits);
    }

    // Shave off factors of 10.
    while (exponent < 0 && (mantissa & 1) == 0) {
      ++exponent;
      mantissa >>= 1;
    }

    intVal = BigInteger.valueOf(bits < 0 ? -mantissa : mantissa);
    if (exponent < 0) {
      // We have MANTISSA * 2 ^ (EXPONENT).
      // Since (1/2)^N == 5^N * 10^-N we can easily convert this
      // into a power of 10.
      scale = (int) (-exponent);
      BigInteger mult = BigInteger.valueOf(5).pow(scale);
      intVal = intVal.multiply(mult);
    } else {
      intVal = intVal.shiftLeft((int) exponent);
      scale = 0;
    }
  }

  /**
   * Translates a <code>long</code> value into a <code>BigDecimal</code> object with a scale of zero. This "static factory method"
   * is provided in preference to a <code>(long)</code> constructor because it allows for reuse of frequently used <code>BigDecimal</code> values.
   *
   * @param val The value of the <code>BigDecimal</code>.
   * @return A <code>BigDecimal</code> whose value is <code>val</code>.
   */
  public static BigDecimal valueOf(long val) throws InvalidNumberException {
    return valueOf(val, 0);
  }

  /**
   * Translates a <code>long</code> unscaled value and an <code>int</code> scale into a <code>BigDecimal</code>. This "static factory method" is 
   * provided in preference to a <code>(long, int)</code> constructor because it allows for reuse of frequently used <code>BigDecimal</code> values.
   *
   * @param scale Scale of the <code>BigDecimal</code>.
   * @return A <code>BigDecimal</code> whose value is <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
   */
  public static BigDecimal valueOf(long val, int scale) throws InvalidNumberException {
    if ((scale == 0) && ((int) val == val)) {
      switch ((int) val) {
      case 0:
        return ZERO;
      case 1:
        return ONE;
      }
    }
    return new BigDecimal(BigInteger.valueOf(val), scale);
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is <code>(this + val)</code>, and whose scale is <code>max(this.scale(), val.scale())</code>.
   *
   * @param val The value to be added to this <code>BigDecimal</code>.
   * @return <code>(this + val)</code>.
   */
  public BigDecimal add(BigDecimal val) {
    // For addition, need to line up decimals. Note that the movePointRight
    // method cannot be used for this as it might return a BigDecimal with
    // scale == 0 instead of the scale we need.
    if (scale == val.scale) {
      return new BigDecimal(intVal.add(val.intVal), scale);
    }
    BigInteger op1 = intVal;
    BigInteger op2 = val.intVal;
    if (scale < val.scale) {
      op1 = op1.multiply(BigInteger.TEN.pow(val.scale - scale));
    } else {
      op2 = op2.multiply(BigInteger.TEN.pow(scale - val.scale)); // >
    }

    return new BigDecimal(op1.add(op2), Math.max(scale, val.scale));
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is <code>(this - val)</code>, and whose scale is <code>max(this.scale(), val.scale())</code>.
   *
   * @param val The value to be subtracted from this <code>BigDecimal</code>.
   * @return <code>(this - val)</code>.
   */
  public BigDecimal subtract(BigDecimal val) {
    if (scale == val.scale) {
      return new BigDecimal(intVal.subtract(val.intVal), scale);
    }
    return this.add(val.negate());
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is <tt>(this &times; val)</tt>, and whose scale is <code>(this.scale() + val.scale())</code>.
   *
   * @param  val The value to be multiplied by this <code>BigDecimal</code>.
   * @return <code>(this * val)</code>.
   */
  public BigDecimal multiply(BigDecimal val) {
    return new BigDecimal(intVal.multiply(val.intVal), scale + val.scale);
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is <code>(this / val)</code>, and whose scale is <code>this.scale()</code>. If rounding must be 
   * performed to generate a result with the given scale, the specified rounding mode is applied.
   * 
   * @param val The value by which this <code>BigDecimal</code> is to be divided.
   * @param roundingMode The rounding mode to apply.
   * @return <code>(this / val)</code>.
   * @throws ArithmeticException If <code>val == 0</code>, or <code>roundingMode == ROUND_UNNECESSARY</code> and <code>this.scale()</code> is 
   * insufficient to represent the result of the division exactly.
   * @throws IllegalArgumentException If <code>roundingMode</code> does not represent a valid rounding mode.
   */
  public BigDecimal divide(BigDecimal val, int roundingMode) throws ArithmeticException, IllegalArgumentException {
    return divide(val, scale, roundingMode);
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is <code>(this / val)</code>, and whose scale is as specified. If rounding must be 
   * performed to generate a result with the given scale, the specified rounding mode is applied.
   * 
   * @param val The value by which this <code>BigDecimal</code> is to be divided.
   * @param roundingMode The rounding mode to apply.
   * @return <code>(this / val)</code>.
   * @throws ArithmeticException If <code>val == 0</code>, or <code>roundingMode == ROUND_UNNECESSARY</code> and <code>this.scale()</code> is 
   * insufficient to represent the result of the division exactly.
   * @throws IllegalArgumentException If <code>roundingMode</code> does not represent a valid rounding mode.
   */
  public BigDecimal divide(BigDecimal val, int newScale, int roundingMode)
      throws ArithmeticException, IllegalArgumentException {
    if (roundingMode < 0 || roundingMode > 7) {
      throw new IllegalArgumentException("illegal rounding mode: " + roundingMode);
    }

    if (intVal.signum() == 0) {
      return newScale == 0 ? ZERO : new BigDecimal(ZERO.intVal, newScale);
    }

    // Ensure that pow gets a non-negative value.
    BigInteger valIntVal = val.intVal;
    int power = newScale - (scale - val.scale);
    if (power < 0) {
      // Effectively increase the scale of val to avoid an
      // ArithmeticException for a negative power.
      valIntVal = valIntVal.multiply(BigInteger.TEN.pow(-power));
      power = 0;
    }

    BigInteger dividend = intVal.multiply(BigInteger.TEN.pow(power));

    BigInteger parts[] = dividend.divideAndRemainder(valIntVal);

    BigInteger unrounded = parts[0];
    if (parts[1].signum() == 0) {
      return new BigDecimal(unrounded, newScale);
    }

    if (roundingMode == ROUND_UNNECESSARY) {
      throw new ArithmeticException("Rounding necessary");
    }

    int sign = intVal.signum() * valIntVal.signum();

    if (roundingMode == ROUND_CEILING) {
      roundingMode = (sign > 0) ? ROUND_UP : ROUND_DOWN;
    } else if (roundingMode == ROUND_FLOOR) {
      roundingMode = (sign < 0) ? ROUND_UP : ROUND_DOWN;
    } else {
      // half is -1 if remainder*2 < positive intValue (*power), 0 if equal,
      // 1 if >. This implies that the remainder to round is less than,
      // equal to, or greater than half way to the next digit.
      BigInteger posRemainder = parts[1].signum() < 0 ? parts[1].negate() : parts[1];
      valIntVal = valIntVal.signum() < 0 ? valIntVal.negate() : valIntVal;
      int half = posRemainder.shiftLeft(1).compareTo(valIntVal);

      switch (roundingMode) {
      case ROUND_HALF_UP:
        roundingMode = (half < 0) ? ROUND_DOWN : ROUND_UP;
        break;
      case ROUND_HALF_DOWN:
        roundingMode = (half > 0) ? ROUND_UP : ROUND_DOWN;
        break;
      case ROUND_HALF_EVEN:
        if (half < 0) {
          roundingMode = ROUND_DOWN;
        } else if (half > 0) {
          roundingMode = ROUND_UP;
        } else if (unrounded.testBit(0)) {
          roundingMode = ROUND_UP;
        } else {
          // even, ROUND_HALF_DOWN
          roundingMode = ROUND_DOWN;
        }
        break;
      }
    }

    if (roundingMode == ROUND_UP) {
      unrounded = unrounded.add(BigInteger.valueOf(sign > 0 ? 1 : -1));
    }

    // roundingMode == ROUND_DOWN
    return new BigDecimal(unrounded, newScale);
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is <code>(this / val)</code>, and whose scale is <code>this.scale()</code>. If rounding must be 
   * performed to generate a result with the given scale, an <code>ArithmeticException</code> is thrown.
   * 
   * @param divisor The value by which this <code>BigDecimal</code> is to be divided.
   * @return <code>(this / val)</code>.
   * @throws ArithmeticException If <code>val == 0</code>, or <code>this.scale()</code> is insufficient to represent the result of the division 
   * exactly.
   */
  public BigDecimal divide(BigDecimal divisor) throws ArithmeticException {
    return divide(divisor, scale, ROUND_UNNECESSARY);
  }

  /**
   * Returns a <code>BigDecimal</code> whose value is the remainder in the quotient <code>this / val</code>. This is obtained by
   * <code>subtract(divideToIntegralValue(val).multiply(val))</code>.
   * 
   * @param val The divisor.
   * @return A <code>BigDecimal</code> whose value is the remainder
   * @throws InvalidNumberException If an internal method throws it.
   * @throws IllegalArgumentException If an internal method throws it. 
   * @throws ArithmeticException If <code>val == 0</code>.
   */
  public BigDecimal remainder(BigDecimal val)
      throws ArithmeticException, IllegalArgumentException, InvalidNumberException {
    return subtract(divideToIntegralValue(val).multiply(val));
  }

  /**
   * Returns a <code>BigDecimal</code> array, where its first element is the integer part of <code>this / val</code>, and its second element is the 
   * remainder of the division.
   * 
   * @param val The divisor.
   * @return The above described <code>BigDecimal</code> array.
   * @throws InvalidNumberException If an internal method throws it.
   * @throws IllegalArgumentException If an internal method throws it.
   * @throws ArithmeticException If <code>val == 0</code>.
   */
  public BigDecimal[] divideAndRemainder(BigDecimal val)
      throws ArithmeticException, IllegalArgumentException, InvalidNumberException {
    BigDecimal[] result = new BigDecimal[2];
    result[0] = divideToIntegralValue(val);
    result[1] = subtract(result[0].multiply(val));
    return result;
  }

  /**
   * Returns a BigDecimal whose value is the integer part of the quotient this / val. The preferred scale is this.scale
   * - val.scale.
   * 
   * @param val
   *           the divisor
   * @return a BigDecimal whose value is the integer part of this / val.
   * @throws InvalidNumberException 
   * @throws IllegalArgumentException 
   * @throws ArithmeticException 
   * @throws ArithmeticException
   *            if val == 0
   */
  public BigDecimal divideToIntegralValue(BigDecimal val)
      throws ArithmeticException, IllegalArgumentException, InvalidNumberException {
    return divide(val, ROUND_DOWN).floor().setScale(scale - val.scale, ROUND_DOWN);
  }

  /**
   * Mutates this BigDecimal into one with no fractional part, whose value is equal to the largest integer that is <=
   * to this BigDecimal. Note that since this method is private it is okay to mutate this BigDecimal.
   * 
   * @return the BigDecimal obtained through the floor operation on this BigDecimal.
   * @throws InvalidNumberException 
   */
  private BigDecimal floor() throws InvalidNumberException {
    if (scale <= 0) {
      return this;
    }
    String intValStr = intVal.toString();
    int end = intValStr.length() - scale;
    intValStr = end > 0 ? intValStr.substring(0, end) : "0";
    intVal = new BigInteger(intValStr).multiply(BigInteger.TEN.pow(scale));
    return this;
  }

  public int compareTo(BigDecimal val) {
    int delta = scale - val.scale;
    BigInteger thisIntVal;
    BigInteger valIntVal;

    // Putting both BigIntegers in the same scale...
    if (delta == 0) {
      thisIntVal = intVal;
      valIntVal = val.intVal;
    } else if (delta > 0) {
      thisIntVal = intVal;
      valIntVal = val.intVal.multiply(BigInteger.TEN.pow(delta));
    } else {
      thisIntVal = intVal.multiply(BigInteger.TEN.pow(-delta));
      valIntVal = val.intVal;
    }

    // and compare them
    return thisIntVal.compareTo(valIntVal);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof BigDecimal && scale == ((BigDecimal) o).scale && compareTo((BigDecimal) o) == 0);
  }

  @Override
  public int hashCode() {
    return intValue() ^ scale;
  }

  public BigDecimal max(BigDecimal val) {
    switch (compareTo(val)) {
    case 1:
      return this;
    default:
      return val;
    }
  }

  public BigDecimal min(BigDecimal val) {
    switch (compareTo(val)) {
    case -1:
      return this;
    default:
      return val;
    }
  }

  public BigDecimal movePointLeft(int n) {
    return (n < 0) ? movePointRight(-n) : new BigDecimal(intVal, scale + n);
  }

  public BigDecimal movePointRight(int n) {
    if (n < 0) {
      return movePointLeft(-n);
    }

    if (scale >= n) {
      return new BigDecimal(intVal, scale - n);
    }

    return new BigDecimal(intVal.multiply(BigInteger.TEN.pow(n - scale)), 0);
  }

  public int signum() {
    return intVal.signum();
  }

  public int scale() {
    return scale;
  }

  public BigInteger unscaledValue() {
    return intVal;
  }

  public BigDecimal abs() {
    return new BigDecimal(intVal.abs(), scale);
  }

  public BigDecimal negate() {
    return new BigDecimal(intVal.negate(), scale);
  }

  /**
   * Returns this BigDecimal. This is included for symmetry with the method negate().
   * 
   * @return this
   */
  public BigDecimal plus() {
    return this;
  }

  /**
   * Returns the precision of this BigDecimal (the number of digits in the unscaled value). The precision of a zero
   * value is 1.
   * 
   * @return the number of digits in the unscaled value, or 1 if the value is zero.
   */
  public int precision() {
    if (precision == 0) {
      String s = intVal.toString();
      precision = s.length() - ((s.charAt(0) == '-') ? 1 : 0);
    }
    return precision;
  }

  /**
   * Returns the String representation of this BigDecimal, using scientific notation if necessary. The following steps
   * are taken to generate the result: 1. the BigInteger unscaledValue's toString method is called and if
   * <code>scale == 0</code> is returned.
   * 2. an <code>int adjExp</code> is created which is equal to the negation of <code>scale</code> plus the number of
   * digits in the unscaled value, minus one. 3. if <code>scale >= 0 && adjExp >= -6</code> then we represent this
   * BigDecimal without scientific notation. A decimal is added if the scale is positive and zeros are prepended as
   * necessary. 4. if scale is negative or adjExp is less than -6 we use scientific notation. If the unscaled value has
   * more than one digit, a decimal as inserted after the first digit, the character 'E' is appended and adjExp is
   * appended.
   */
  @Override
  public String toString() {
    // bigStr is the String representation of the unscaled value. If
    // scale is zero we simply return this.
    String bigStr = intVal.toString();
    if (scale == 0) {
      return bigStr;
    }

    boolean negative = (bigStr.charAt(0) == '-');
    int point = bigStr.length() - scale - (negative ? 1 : 0);

    StringBuffer val = new StringBuffer(20);

    if (scale >= 0 && (point - 1) >= -6) {
      // Convert to character form without scientific notation.
      if (point <= 0) {
        // Zeros need to be prepended to the StringBuilder.
        if (negative) {
          val.append('-');
        }
        // Prepend a '0' and a '.' and then as many more '0's as necessary.
        val.append('0').append('.');
        while (point < 0) {
          val.append('0');
          point++;
        }
        // Append the unscaled value.
        val.append(bigStr.substring(negative ? 1 : 0));
      } else {
        // No zeros need to be prepended so the String is simply the
        // unscaled value with the decimal point inserted.
        val.append(bigStr);
        Convert.insertAt(val, point + (negative ? 1 : 0), '.');
      }
    } else {
      // We must use scientific notation to represent this BigDecimal.
      val.append(bigStr);
      // If there is more than one digit in the unscaled value we put a
      // decimal after the first digit.
      if (bigStr.length() > 1) {
        Convert.insertAt(val, (negative ? 2 : 1), '.');
      }
      // And then append 'E' and the exponent = (point - 1).
      val.append('E');
      // guich: 1E+9 was translating to 1E-9 if (point - 1 >= 0) val.append('+'); val.append(point - 1);
      if (scale >= 0) {
        val.append('+');
      }
      val.append(scale);
    }
    return val.toString();
  }

  /**
   * Returns the String representation of this BigDecimal, using engineering notation if necessary. This is similar to
   * toString() but when exponents are used the exponent is made to be a multiple of 3 such that the integer part is
   * between 1 and 999.
   * 
   * @return a String representation of this BigDecimal in engineering notation
   */
  public String toEngineeringString() {
    // bigStr is the String representation of the unscaled value. If
    // scale is zero we simply return this.
    String bigStr = intVal.toString();
    if (scale == 0) {
      return bigStr;
    }

    boolean negative = (bigStr.charAt(0) == '-');
    int point = bigStr.length() - scale - (negative ? 1 : 0);

    // This is the adjusted exponent described above.
    int adjExp = point - 1;
    StringBuffer val = new StringBuffer(20);

    if (scale >= 0 && adjExp >= -6) {
      // Convert to character form without scientific notation.
      if (point <= 0) {
        // Zeros need to be prepended to the StringBuilder.
        if (negative) {
          val.append('-');
        }
        // Prepend a '0' and a '.' and then as many more '0's as necessary.
        val.append('0').append('.');
        while (point < 0) {
          val.append('0');
          point++;
        }
        // Append the unscaled value.
        val.append(bigStr.substring(negative ? 1 : 0));
      } else {
        // No zeros need to be prepended so the String is simply the
        // unscaled value with the decimal point inserted.
        val.append(bigStr);
        Convert.insertAt(val, point + (negative ? 1 : 0), '.');
      }
    } else {
      // We must use scientific notation to represent this BigDecimal.
      // The exponent must be a multiple of 3 and the integer part
      // must be between 1 and 999.
      val.append(bigStr);
      int zeros = adjExp % 3;
      int dot = 1;
      if (adjExp > 0) {
        // If the exponent is positive we just move the decimal to the
        // right and decrease the exponent until it is a multiple of 3.
        dot += zeros;
        adjExp -= zeros;
      } else {
        // If the exponent is negative then we move the dot to the right
        // and decrease the exponent (increase its magnitude) until
        // it is a multiple of 3. Note that this is not adjExp -= zeros
        // because the mod operator doesn't give us the distance to the
        // correct multiple of 3. (-5 mod 3) is -2 but the distance from
        // -5 to the correct multiple of 3 (-6) is 1, not 2.
        if (zeros == -2) {
          dot += 1;
          adjExp -= 1;
        } else if (zeros == -1) {
          dot += 2;
          adjExp -= 2;
        }
      }

      // Either we have to append zeros because, for example, 1.1E+5 should
      // be 110E+3, or we just have to put the decimal in the right place.
      if (dot > val.length()) {
        while (dot > val.length()) {
          val.append('0');
        }
      } else if (bigStr.length() > dot) {
        Convert.insertAt(val, dot + (negative ? 1 : 0), '.');
      }

      // And then append 'E' and the exponent (adjExp).
      val.append('E');
      if (adjExp >= 0) {
        val.append('+');
      }
      val.append(adjExp);
    }
    return val.toString();
  }

  /**
   * Returns a String representation of this BigDecimal without using scientific notation. This is how toString()
   * worked for releases 1.4 and previous. Zeros may be added to the end of the String. For example, an unscaled value
   * of 1234 and a scale of -3 would result in the String 1234000, but the toString() method would return 1.234E+6.
   * 
   * @return a String representation of this BigDecimal
   */
  public String toPlainString() {
    // If the scale is zero we simply return the String representation of the
    // unscaled value.
    if (scale == 0) {
      return intVal.toString(10);
    }
    StringBuffer sb = new StringBuffer(10);
    intVal.toStringBuffer(10, sb);
    int l = sb.length();

    // Remember if we have to put a negative sign at the start.
    boolean negative = (sb.charAt(0) == '-');

    int point = l - scale - (negative ? 1 : 0);

    if (point <= 0) {
      // We have to prepend zeros and a decimal point. (-5000,6) -> -0.005000
      sb.reverse();
      if (negative) {
        sb.setLength(l - 1);
      }
      for (; point < 0; point++) {
        sb.append('0');
      }
      sb.append(negative ? ".0-" : ".0");
      sb.reverse();
    } else if (point < l) {
      // No zeros need to be prepended or appended, just put the decimal
      // in the right place.
      Convert.insertAt(sb, point + (negative ? 1 : 0), '.');
    } else {
      // We must append zeros instead of using scientific notation.
      for (int i = l; i < point; i++) {
        sb.append('0');
      }
    }
    return sb.toString();
  }

  /**
   * Converts this BigDecimal to a BigInteger. Any fractional part will be discarded.
   * 
   * @return a BigDecimal whose value is equal to floor[this]
   */
  public BigInteger toBigInteger() {
    // If scale > 0 then we must divide, if scale > 0 then we must multiply,
    // and if scale is zero then we just return intVal;
    if (scale > 0) {
      return intVal.divide(BigInteger.TEN.pow(scale));
    } else if (scale < 0) {
      return intVal.multiply(BigInteger.TEN.pow(-scale));
    }
    return intVal;
  }

  /**
   * Converts this BigDecimal into a BigInteger, throwing an ArithmeticException if the conversion is not exact.
   * 
   * @return a BigInteger whose value is equal to the value of this BigDecimal
   */
  public BigInteger toBigIntegerExact() {
    if (scale > 0) {
      // If we have to divide, we must check if the result is exact.
      BigInteger[] result = intVal.divideAndRemainder(BigInteger.TEN.pow(scale));
      if (result[1].equals(BigInteger.ZERO)) {
        return result[0];
      }
      throw new ArithmeticException("No exact BigInteger representation");
    } else if (scale < 0) {
      // If we're multiplying instead, then we needn't check for exactness.
      return intVal.multiply(BigInteger.TEN.pow(-scale));
    }
    // If the scale is zero we can simply return intVal.
    return intVal;
  }

  public int intValue() {
    return toBigInteger().intValue();
  }

  /**
   * Returns a BigDecimal which is numerically equal to this BigDecimal but with no trailing zeros in the
   * representation. For example, if this BigDecimal has [unscaledValue, scale] = [6313000, 4] this method returns a
   * BigDecimal with [unscaledValue, scale] = [6313, 1]. As another example, [12400, -2] would become [124, -4].
   * 
   * @return a numerically equal BigDecimal with no trailing zeros
   * @throws InvalidNumberException 
   */
  public BigDecimal stripTrailingZeros() throws InvalidNumberException {
    String intValStr = intVal.toString();
    int newScale = scale;
    int pointer = intValStr.length() - 1;
    // This loop adjusts pointer which will be used to give us the substring
    // of intValStr to use in our new BigDecimal, and also accordingly
    // adjusts the scale of our new BigDecimal.
    while (intValStr.charAt(pointer) == '0') {
      pointer--;
      newScale--;
    }
    // Create a new BigDecimal with the appropriate substring and then
    // set its scale.
    BigDecimal result = new BigDecimal(intValStr.substring(0, pointer + 1));
    result.scale = newScale;
    return result;
  }

  public long longValue() {
    return toBigInteger().longValue();
  }

  public double doubleValue() throws InvalidNumberException {
    return Convert.toDouble(toString());
  }

  public BigDecimal setScale(int scale) throws ArithmeticException {
    return setScale(scale, ROUND_UNNECESSARY);
  }

  public BigDecimal setScale(int scale, int roundingMode) throws ArithmeticException, IllegalArgumentException {
    // NOTE: The 1.5 JRE doesn't throw this, ones prior to it do and
    // the spec says it should. Nevertheless, if 1.6 doesn't fix this
    // we should consider removing it.
    if (scale < 0) {
      throw new ArithmeticException("Scale parameter < 0.");
    }
    return divide(ONE, scale, roundingMode);
  }

  /**
   * Returns a new BigDecimal constructed from the BigDecimal(String) constructor using the Double .toString(double)
   * method to obtain the String.
   * 
   * @param val
   *           the double value used in Double .toString(double)
   * @return a BigDecimal representation of val
   * @throws InvalidNumberException 
   *            if val is NaN or infinite
   */
  public static BigDecimal valueOf(double val) throws InvalidNumberException {
    long bits = Convert.doubleToLongBits(val);
    if (bits == Convert.DOUBLE_POSITIVE_INFINITY_BITS || bits == Convert.DOUBLE_NEGATIVE_INFINITY_BITS
        || bits == Convert.DOUBLE_NAN_BITS) {
      throw new InvalidNumberException("argument cannot be NaN or infinite.");
    }
    return new BigDecimal(Convert.toString(val));
  }

  /**
   * Returns a BigDecimal whose numerical value is the numerical value of this BigDecimal multiplied by 10 to the power
   * of <code>n</code>.
   * 
   * @param n
   *           the power of ten
   * @return the new BigDecimal
   */
  public BigDecimal scaleByPowerOfTen(int n) {
    BigDecimal result = new BigDecimal(intVal, scale - n);
    result.precision = precision;
    return result;
  }

  /**
   * Returns a BigDecimal whose value is <code>this</code> to the power of <code>n</code>.
   * 
   * @param n
   *           the power
   * @return the new BigDecimal
   */
  public BigDecimal pow(int n) {
    if (n < 0 || n > 999999999) {
      throw new ArithmeticException("n must be between 0 and 999999999");
    }
    BigDecimal result = new BigDecimal(intVal.pow(n), scale * n);
    return result;
  }

  /**
   * Returns the size of a unit in the last place of this BigDecimal. This returns a BigDecimal with [unscaledValue,
   * scale] = [1, this.scale()].
   * 
   * @return the size of a unit in the last place of <code>this</code>.
   */
  public BigDecimal ulp() {
    return new BigDecimal(BigInteger.ONE, scale);
  }

  /**
   * Converts this BigDecimal to a long value.
   * 
   * @return the long value
   * @throws ArithmeticException
   *            if rounding occurs or if overflow occurs
   */
  public long longValueExact() {
    // Set scale will throw an exception if rounding occurs.
    BigDecimal temp = setScale(0, ROUND_UNNECESSARY);
    BigInteger tempVal = temp.intVal;
    // Check for overflow.
    long result = intVal.longValue();
    if (tempVal.compareTo(BigInteger.valueOf(Convert.MAX_LONG_VALUE)) > 1 || (result < 0 && signum() == 1)
        || (result > 0 && signum() == -1)) {
      throw new ArithmeticException("this BigDecimal is too " + "large to fit into the return type");
    }

    return intVal.longValue();
  }

  /**
   * Converts this BigDecimal into an int by first calling longValueExact and then checking that the <code>long</code>
   * returned from that method fits into an <code>int</code>.
   * 
   * @return an int whose value is <code>this</code>
   * @throws ArithmeticException
   *            if this BigDecimal has a fractional part or is too large to fit into an int.
   */
  public int intValueExact() {
    long temp = longValueExact();
    int result = (int) temp;
    if (result != temp) {
      throw new ArithmeticException("this BigDecimal cannot fit into an int");
    }
    return result;
  }

  /**
   * Converts this BigDecimal into a byte by first calling longValueExact and then checking that the <code>long</code>
   * returned from that method fits into a <code>byte</code>.
   * 
   * @return a byte whose value is <code>this</code>
   * @throws ArithmeticException
   *            if this BigDecimal has a fractional part or is too large to fit into a byte.
   */
  public byte byteValueExact() {
    long temp = longValueExact();
    byte result = (byte) temp;
    if (result != temp) {
      throw new ArithmeticException("this BigDecimal cannot fit into a byte");
    }
    return result;
  }

  /**
   * Converts this BigDecimal into a short by first calling longValueExact and then checking that the <code>long</code>
   * returned from that method fits into a <code>short</code>.
   * 
   * @return a short whose value is <code>this</code>
   * @throws ArithmeticException
   *            if this BigDecimal has a fractional part or is too large to fit into a short.
   */
  public short shortValueExact() {
    long temp = longValueExact();
    short result = (short) temp;
    if (result != temp) {
      throw new ArithmeticException("this BigDecimal cannot fit into a short");
    }
    return result;
  }

  @Override
  public int compareTo(Object other) throws ClassCastException {
    return compareTo((BigDecimal) other);
  }
}
