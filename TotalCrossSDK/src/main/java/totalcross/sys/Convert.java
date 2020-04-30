// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>   
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

package totalcross.sys;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.Launcher;
import totalcross.ui.font.FontMetrics;
import totalcross.util.Comparable;
import totalcross.util.Date;
import totalcross.util.InvalidDateException;
import totalcross.util.Vector;

/**
 * Convert is used to convert between objects and basic types. It also contains many
 * other utility methods.
 */

public final class Convert {
  public static final String CRLF = "\r\n";
  public static final byte[] CRLF_BYTES = { '\r', '\n' };

  static void newLauncherInstance() {
    /* guich@200b4_150: totalcross.sys.Convert is always the first native method
     * class created. It may have been created by a compiler or by Retroguard
     * during the obfuscating process. When it is created this way, the
     * Applet was not initialized, and such here we test and
     * create it if necessary. The values in the class aren't important; but
     * they must not be null.
     */
    try {
      if (Launcher.instance == null) {
        new Launcher();
      }
      if (Launcher.instance == null) {
        new Launcher();
        System.out.println("******************** NULL");
      }
      Launcher.instance.fillSettings(); // guich@tc100
    } catch (java.awt.HeadlessException he) {
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  static void newLauncherInstance4D() {
  }

  static {
    newLauncherInstance();
  }

  public static char[] b2h = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  private static byte[] h2b = new byte['f' + 1];
  private static byte h2bInvalid = (byte) 0xFF;
  static {
    fill(h2b, 0, h2b.length, h2bInvalid); // fill with invalid values

    for (int i = '0', j = 0; i <= '9'; i++, j++) {
      h2b[i] = (byte) j;
    }
    for (int i = 'A', j = 10; i <= 'F'; i++, j++) {
      h2b[i] = (byte) j;
    }
    for (int i = 'a', j = 10; i <= 'f'; i++, j++) {
      h2b[i] = (byte) j;
    }
  }

  /** The bytes are converted to char and vice-versa using the CharacterConverter associated in this charConverter member.
   * @see totalcross.sys.Convert#setDefaultConverter(String)
   * @see totalcross.sys.CharacterConverter
   * @see totalcross.sys.UTF8CharacterConverter
   */
  public static totalcross.sys.AbstractCharacterConverter charConverter = new totalcross.sys.CharacterConverter();

  private static Map<String, AbstractCharacterConverter> htConvs = new HashMap<>();
  static {
    // ISO-8859-1
    AbstractCharacterConverter iso88591_1 = new CharacterConverter();
    htConvs.put("", iso88591_1);
    registerCharacterConverter(iso88591_1);

    // UTF-8
    registerCharacterConverter(new UTF8CharacterConverter());

    // Code Page 437
    registerCharacterConverter(new Cp437CharacterConverter());
  }

  /**
   * Registers the given subclass of AbstractCharacterConverter to the list of
   * encodings supported to this instance of the running TotalCross VM
   * 
   * @param characterConverter
   *          the AbstractCharacterConverter to be registered to this instance
   *          of the running TotalCross VM
   */
  public static void registerCharacterConverter(AbstractCharacterConverter characterConverter) {
    htConvs.put(characterConverter.name().toLowerCase(), characterConverter);
    Set<String> aliases = characterConverter.aliases();
    for (String alias : aliases) {
      htConvs.put(alias.toLowerCase(), characterConverter);
    }
  }

  /** Changes the default Character Converter to the given one.
   * Use like
   * <pre>Convert.setDefaultConverter("UTF8");</pre>
   * to change all bytes_to_char and char_to_bytes operations to use UTF8 instead.
   * <pre>Convert.setDefaultConverter("");</pre>
   * sets back the default encoder.
   * @return true if the given encoder was found, false otherwise. If not found, the encoder is reset to the default one (ISO 8859-1).
   * @since SuperWaba 4.1
   * @see totalcross.sys.Convert#charConverter
   * @see totalcross.sys.CharacterConverter
   * @see totalcross.sys.UTF8CharacterConverter
   */
  public static boolean setDefaultConverter(String name) {
    AbstractCharacterConverter cc = (AbstractCharacterConverter) charsetForName(name);
    if (cc == null) {
      return false;
    }
    charConverter = cc;
    return true;
  }

  /**
   * Returns a charset object for the named charset.
   * 
   * @param charsetName
   *          The name of the requested charset; may be either a canonical name
   *          or an alias
   * @return A charset object for the named charset or null if no support for
   *         the named charset is available
   * @throws IllegalArgumentException
   *           If the given charsetName is null
   */
  public static Charset charsetForName(String charsetName) throws IllegalArgumentException {
    if (charsetName == null) {
      throw new IllegalArgumentException();
    }
    return htConvs.get(charsetName.toLowerCase());
  }

  /** The minimum char value: '\u0000' */
  public static final char MIN_CHAR_VALUE = '\u0000';
  /** The maximum char value:  */
  public static final char MAX_CHAR_VALUE = '\uFFFF';
  /** The maximum short value: 32767 */
  public static final short MAX_SHORT_VALUE = 32767;
  /** The minimum short value: -32768 */
  public static final short MIN_SHORT_VALUE = -32768;
  /** The minimum long value: -9223372036854775808 */
  public static final long MIN_LONG_VALUE = 0x8000000000000000L;
  /** The maximum long value: 9223372036854775807 */
  public static final long MAX_LONG_VALUE = 0x7fffffffffffffffL;
  /** The minimum int value: -2147483648 */
  public static final int MIN_INT_VALUE = 0x80000000;
  /** The maximum int value: 2147483647 */
  public static final int MAX_INT_VALUE = 0x7fffffff;
  /** The maximum double value: 1.7976931348623157E308d */
  public static final double MAX_DOUBLE_VALUE = 1.7976931348623157E308d; // 2^53
  /** The minimum double value: 4.9E-324d */
  public static final double MIN_DOUBLE_VALUE = 4.9E-324d;
  /** The maximum number of digits in a double value, used when formatting to string. */
  public static final int MAX_DOUBLE_DIGITS = 15;
  private static final double DOUBLE_MAX_NON_EXP = 9.007199254740992E15; // 2^53
  private static final double DOUBLE_MIN_NON_EXP = 1.1102230246251565E-16; // 2^-53

  static final char[] TITLE = "\u01c4\u01c5\u01c5\u01c5\u01c6\u01c5\u01c7\u01c8\u01c8\u01c8\u01c9\u01c8\u01ca\u01cb\u01cb\u01cb\u01cc\u01cb\u01f1\u01f2\u01f2\u01f2\u01f3\u01f2"
      .toCharArray();

  static final boolean useNative = !Settings.onJavaSE;

  private Convert() {
  }

  /**
   * Converts a Unicode character into its titlecase equivalent mapping.
   * If a mapping does not exist, then the character passed is returned.
   * Note that isTitleCase(toTitleCase(ch)) does not always return true.
   *
   * @param ch character to convert to titlecase
   * @return titlecase mapping of ch, or ch if titlecase mapping does
   *         not exist
   * @see #toLowerCase(char)
   * @see #toUpperCase(char)
   */
  public static char toTitleCase(char ch) {
    char[] title = TITLE;
    // As title is short, it doesn't hurt to exhaustively iterate over it.
    for (int i = title.length - 2; i >= 0; i -= 2) {
      if (title[i] == ch) {
        return title[i + 1];
      }
    }
    return toUpperCase(ch);
  }

  /** Creates a copy of the given array. */
  public static String[] cloneStringArray(String[] strs) // guich@220_2
  {
    String[] ret = null;
    if (strs != null) {
      int n = strs.length;
      ret = new String[n];
      Vm.arrayCopy(strs, 0, ret, 0, n);
    }
    return ret;
  }

  /** Converts the given object array to a String array by calling toString in each object. */
  public static String[] toStringArray(Object[] objs) // guich@200b4_26
  {
    int i;
    String as[] = null;
    if (objs != null) {
      as = new String[objs.length];
      for (i = objs.length; --i >= 0;) {
        Object o = objs[i];
        as[i] = o instanceof String ? (String) o : objs[i].toString();
      }
    }
    return as;
  }

  /** Convert a string to the short type. Note that this method is slower than <code>(short)Convert.toInt()</code>
   * @since TotalCross 1.22
   */
  public static short toShort(String s) throws InvalidNumberException {
    if (s == null) {
      throw new NullPointerException("Argument 's' cannot have a null value");
    } else if (s.length() == 0) {
      throw new InvalidNumberException("Argument 's' is empty and cannot be converted into a number.");
    }
    try {
      int v = toInt(s);
      if (v < MIN_SHORT_VALUE || v > MAX_SHORT_VALUE) {
        throw new InvalidNumberException("Value " + s + " is out of short range.");
      }
      return (short) v;
    } catch (NumberFormatException e) {
      throw new InvalidNumberException("Error: " + s + " is not a valid integer value.");
    }
  }

  /**
   * Converts the given String to an int. The number may be prefixed with 0's.
   * @throws InvalidNumberException If the string passed is not a valid number
   */
  @ReplacedByNativeOnDeploy
  public static int toInt(String s) throws InvalidNumberException {
    if (s == null) {
      throw new NullPointerException("Argument 's' cannot have a null value");
    } else if (s.length() == 0) {
      throw new InvalidNumberException("Argument 's' is empty and cannot be converted into a number.");
    }
    try {
      return java.lang.Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new InvalidNumberException("Error: " + s + " is not a valid integer value.");
    }
  }

  /**
   * Converts the given String to an int. The number may be prefixed with 0's.
   * If the string is not a valid number, returns defaultValue.
   * @since TotalCross 2.0
   */
  public static int toInt(String s, int defaultValue) {
    try {
      return toInt(s);
    } catch (InvalidNumberException ine) {
      return defaultValue;
    }
  }

  /**
   * Converts the given String to a double. 
   * If the string is not a valid number, returns defaultValue.
   * @since TotalCross 2.0
   */
  public static double toDouble(String s, double defaultValue) {
    try {
      return toDouble(s);
    } catch (InvalidNumberException ine) {
      return defaultValue;
    }
  }

  /** Converts the given boolean to a String. */
  public static String toString(boolean b) {
    return b ? "true" : "false";
  }

  /** Converts the given char to a String. */
  @ReplacedByNativeOnDeploy
  public static String toString(char c) {
    return String.valueOf(c);
  }

  /** Converts the given double to its bit representation in IEEE 754 format, using 4 bytes instead of 8 (a convertion to float is applied). */
  @ReplacedByNativeOnDeploy
  public static int doubleToIntBits(double f) {
    return Float.floatToIntBits((float) f);
  }

  /** Converts the given IEEE 754 bit representation of a float to a double. */
  @ReplacedByNativeOnDeploy
  public static double intBitsToDouble(int i) {
    return (double) Float.intBitsToFloat(i);
  }

  /** Converts the given int to a String. */
  @ReplacedByNativeOnDeploy
  public static String toString(int i) {
    return java.lang.Integer.toString(i);
  }

  /** Converts the given double to a String, formatting it with the given number of decimal places.
   @since SuperWaba 2.0 */
  @ReplacedByNativeOnDeploy
  public static String toString(double val, int decimalCount) {
    if (decimalCount < -1) {
      throw new IllegalArgumentException("Invalid value '" + decimalCount + "' for argument '" + decimalCount + "'"); // guich@tc123_9
    }
    StringBuffer dest = new StringBuffer(25);

    //System.out.print(val+", "+decimalCount);
    long bits = Double.doubleToLongBits(val);
    if (val == 0) {
      if (decimalCount == -1) {
        dest.append("0.0");
      } else {
        dest.append('0');
        if (decimalCount > 0) {
          dest.append('.').append(Constants.zeros, 0, decimalCount);
        }
      }
    } else if (bits == DOUBLE_NAN_BITS) {
      dest.append("NaN");
    } else if (bits == DOUBLE_POSITIVE_INFINITY_BITS || bits == DOUBLE_NEGATIVE_INFINITY_BITS) {
      dest.append(val < 0 ? '-' : '+').append("Inf");
    } else {
      // example: -1000.5432
      long integral, fract = 0;
      int exponent;
      boolean floating = decimalCount < 0;
      if (floating) {
        decimalCount = MAX_DOUBLE_DIGITS;
      }
      if (val < 0) {
        val = -val; // 1000.5432
        dest.append('-');
      }

      exponent = (int) (Math.log(val) / Constants.LN10); // 3 : 1000.5432 = 1.0005432*10^3
      if (DOUBLE_MIN_NON_EXP <= val && val <= DOUBLE_MAX_NON_EXP) // does it fit without sci notation?
      {
        if (decimalCount == 0) {
          val += 0.5;
        }
        integral = (long) val; // 1000
        exponent = 0;
      } else {
        boolean adjusted = false; // guich@tc111_5
        while (true) // guich@580_23: copied from c, which was correct.
        {
          // pow10: fast pow10
          double pow10 = (exponent >= 0) ? Constants.p1[exponent & 31] * Constants.p32[exponent >> 5]
              : Constants.np32[-exponent >> 5] / Constants.p1[-exponent & 31]; // guich@570_51: when exp >= 320, p32 index is 10, and 1e320 is impossible
          double mant = val / pow10;
          if (decimalCount < 18) {
            mant += (double) Constants.rounds5[decimalCount];
          }
          integral = (long) mant;
          if (integral == 0 && !adjusted) {
            adjusted = true;
            exponent--; // 0.12345 ?
          } else if (integral >= 10 && !adjusted) {
            adjusted = true;
            exponent++; // 10.12345 ?
          } else {
            val = mant;
            break;
          }
        }
      }
      if (decimalCount == 0) {
        dest.append(integral);
      } else {
        int i, firstNonZero = -1; // number of zeros between . and first non-zero
        double pow10 = (decimalCount >= 0) ? Constants.p1[decimalCount & 31] * Constants.p32[decimalCount >> 5]
            : Constants.np32[-decimalCount >> 5] / Constants.p1[-decimalCount & 31]; // guich@570_51: when exp >= 320, p32 index is 10, and 1e320 is impossible
        long ipow10 = (long) pow10;
        double f = val - integral; // 1000.5432-1000 = 0.5432
        if (f > 1.0e-16) {
          fract = (long) (f * pow10 + (exponent == 0 ? 0.5 : 0));
          if (fract == ipow10) // case of Convert.toString(49.999,2)
          {
            fract = 0;
            integral++;
          }
        }
        dest.append(integral);

        do {
          ipow10 /= 10;
          firstNonZero++;
        } while (ipow10 > fract);
        String s = Long.toString(fract);
        i = decimalCount - s.length();
        dest.append('.');
        if (0 < firstNonZero && firstNonZero < decimalCount) {
          i -= firstNonZero;
          dest.append(Constants.zeros, 0, firstNonZero);
        }
        dest.append(s);
        if (floating) {
          while (dest.charAt(dest.length() - 2) != '.' && dest.charAt(dest.length() - 1) == '0') {
            dest.setLength(dest.length() - 1);
          }
        } else if (i > 0) {
          dest.append(Constants.zeros, 0, i < 20 ? i : 20); // this should not respect the maximum allowed width, because its just for user formatting
        }
      }

      if (exponent != 0) {
        dest.append('E').append(exponent);
      }
    }
    String ret = dest.toString();
    int l = ret.length();
    if (l > 0 && ret.charAt(0) == '-') // guich@tc200b5: check if its -0.00... and change to 0.00...
    {
      boolean only0 = true;
      for (int i = 1; i < l && only0; i++) {
        char c = ret.charAt(i);
        only0 &= c == '.' || c == '0';
      }
      if (only0) {
        ret = ret.substring(1); // remove the -
      }
    }
    //System.out.println(" -> "+dest);
    return ret;
  }

  /** Converts the String to a double.
   * @throws InvalidNumberException If the string passed is not a valid number
   @since SuperWaba 2.0 */
  @ReplacedByNativeOnDeploy
  public static double toDouble(String s) throws InvalidNumberException {
    if (s == null) {
      throw new java.lang.NullPointerException("Argument 's' cannot have a null value");
    }
    try {
      return Double.valueOf(s).doubleValue();
    } catch (NumberFormatException e) {
      throw new InvalidNumberException("Error: " + s + " is not a valid double value.");
    }
  }

  /** formats a String as a double, <b>rounding</b> with n decimal places.
   * @throws InvalidNumberException If the string passed is not a valid number
   */
  @ReplacedByNativeOnDeploy
  public static String toString(String doubleValue, int n) throws InvalidNumberException {
    if (doubleValue == null) {
      throw new java.lang.NullPointerException("Argument 'doubleValue' cannot have a null value");
    }
    if (n < -1) {
      throw new IllegalArgumentException("Invalid value '" + n + "' for argument '" + n + "'"); // guich@tc123_9
    }
    try {
      return toString(toDouble(doubleValue), n);
    } catch (InvalidNumberException e) {
      throw new InvalidNumberException("Error: " + doubleValue + " is not a valid double value.");
    }
  }

  /**
   * Returns a representation of the specified floating-point value
   * according to the IEEE 754 floating-point "double
   * format" bit layout.
   * <p>
   * Bit 63 represents the sign of the floating-point number. Bits
   * 62-52 represent the exponent. Bits 51-0 represent
   * the significand (sometimes called the mantissa) of the
   * floating-point number.
   * <p>
   * If the argument is positive infinity, the result is
   * <code>0x7ff0000000000000L</code>.
   * <p>
   * If the argument is negative infinity, the result is
   * <code>0xfff0000000000000L</code>.
   * <p>
   * If the argument is NaN, the result is
   * <code>0x7ff8000000000000L</code>.
   *
   * @param   value   a double precision floating-point number.
   * @return  the bits that represent the floating-point number.
   * @since   SuperWaba 2.0
   */
  @ReplacedByNativeOnDeploy
  public static long doubleToLongBits(double value) {
    return Double.doubleToLongBits(value);
  }

  /**
   * Returns the double-float corresponding to a given bit represention.
   * The argument is considered to be a representation of a
   * floating-point value according to the IEEE 754 floating-point
   * "double precision" bit layout. That floating-point
   * value is returned as the result.
   * <p>
   * If the argument is <code>0x7f80000000000000L</code>, the result
   * is positive infinity.
   * <p>
   * If the argument is <code>0xff80000000000000L</code>, the result
   * is negative infinity.
   * <p>
   * If the argument is any value in the range
   * <code>0x7ff0000000000001L</code> through
   * <code>0x7fffffffffffffffL</code> or in the range
   * <code>0xfff0000000000001L</code> through
   * <code>0xffffffffffffffffL</code>, the result is NaN. All IEEE 754
   * NaN values are, in effect, lumped together by the Java language
   * into a single value.
   *
   * @param   bits   any <code>long</code> integer.
   * @return  the <code>double</code> floating-point value with the same
   *          bit pattern.
   * @since   SuperWaba 2.0
   */
  @ReplacedByNativeOnDeploy
  public static double longBitsToDouble(long bits) {
    return Double.longBitsToDouble(bits);
  }

  /** returns the value of the digit in the specified radix. this method is simplified to only handle the standard ascii table.
   @since SuperWaba 2.0 */
  public static int digitOf(char ch, int radix) {
    if (radix < 2 || radix > 16) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'radix'");
    }
    int value = -1;
    if (2 <= radix && radix <= 16) {
      if ('0' <= ch && ch <= '9') {
        value = ch - '0';
      } else if ('A' <= ch && ch <= 'F') {
        value = ch - 'A' + 10;
      } else {
        value = ch - 'a' + 10;
      }
    }
    return (0 <= value && value < radix) ? value : -1;
  }

  /** returns the digit in the corresponding radix.
   @since SuperWaba 2.0 */
  public static char forDigit(int digit, int radix) {
    if (radix < 2 || radix > 16) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'radix'");
    }
    if (digit >= radix || digit < 0 || radix < 2 || radix > 16) {
      return '?';
    }
    if (digit < 10) {
      return (char) ('0' + digit);
    }
    return (char) ('a' - 10 + digit);
  }

  /** Converts the long to a String at base 10.
   @since SuperWaba 2.0 */
  @ReplacedByNativeOnDeploy
  public static String toString(long l) {
    return toString(l, 10);
  }

  /** Converts the int to a String in the given radix. Radix can be 2, 8, 10 or 16. */
  public static String toString(int i, int radix) {
    if (radix < 2 || radix > 16) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'radix'");
    }
    if (i == 0) {
      return "0";
    }
    if (radix == 10) {
      return toString(i);
    }
    char[] buf = new char[radix >= 8 ? 12 : 33];
    int pos = buf.length;
    boolean negative = (i < 0);
    if (!negative) {
      i = -i;
    }
    while (i <= -radix) {
      buf[--pos] = forDigit(-(i % radix), radix);
      i /= radix;
    }
    buf[--pos] = forDigit(-i, radix);
    if (negative && radix == 10) {
      buf[--pos] = '-';
    }
    return new String(buf, pos, buf.length - pos);
  }

  /** Converts the long to a String in the given radix. Radix can be 2, 8, 10 or 16. */
  public static String toString(long i, int radix) {
    if (radix < 2 || radix > 16) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'radix'");
    }
    if (i == 0) {
      return "0";
    }
    if (radix == 10 && useNative) {
      return toString(i);
    }
    char[] buf = new char[radix >= 8 ? 23 : 65];
    int pos = buf.length;
    boolean negative = (i < 0);
    if (!negative) {
      i = -i;
    }
    while (i <= -radix) {
      buf[--pos] = forDigit((int) (-(i % radix)), radix);
      i /= radix;
    }
    buf[--pos] = forDigit((int) (-i), radix);
    if (negative && radix == 10) {
      buf[--pos] = '-';
    }
    // reverse and return the string
    return new String(buf, pos, buf.length - pos);
  }

  public static String toString4D(long i, int radix) {
    if (radix < 2 || radix > 16) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'radix'");
    }
    if (i == 0) {
      return "0";
    }
    if (radix == 10) {
      return toString(i);
    }
    char[] buf = new char[radix >= 8 ? 23 : 65];
    int pos = buf.length;
    boolean negative = (i < 0);
    if (!negative) {
      i = -i;
    }
    while (i <= -radix) {
      buf[--pos] = forDigit((int) (-(i % radix)), radix);
      i /= radix;
    }
    buf[--pos] = forDigit((int) (-i), radix);
    if (negative && radix == 10) {
      buf[--pos] = '-';
    }
    // reverse and return the string
    return new String(buf, pos, buf.length - pos);
  }

  /** Converts the String to a long.
   * @throws InvalidNumberException If the string passed is not a valid number
   @since SuperWaba 2.0 */
  @ReplacedByNativeOnDeploy
  public static long toLong(String s) throws InvalidNumberException // guich@500_15: made this routine identical to its C equivalent.
  {
    if (s == null) {
      throw new java.lang.NullPointerException("Argument 's' cannot have a null value");
    }
    int i;
    long r = 0;
    long m = 1;
    char[] ac = s.toCharArray();
    int len = ac.length - 1;
    boolean isNeg = false; // juliana@116_41: added overflow and underflow check for longs.
    if (len >= 0 && (ac[len] == 'L' || ac[len] == 'l')) {
      len--;
    }

    for (i = len; i >= 0; i--) {
      char c = ac[i];
      if (c == '+') {
        break;
      }
      if (c == '-') {
        isNeg = true;
        r = -r;
        break;
      }

      int digit = c - '0';
      if (digit < 0 || c > '9') {
        throw new InvalidNumberException("Error: " + s + " is not a valid long value.");
      }
      r += m * (c - '0');
      if (m == 1) {
        m = 10;
      } else {
        m *= 10;
      }
    }
    if (isNeg != (r < 0)) {
      throw new InvalidNumberException("Error: " + s + " is not a valid long value.");
    }
    return r;
  }

  /** Converts the String to a long in the given radix. Radix can range from 2 to 16. At any error, 0 is returned.
   * @throws InvalidNumberException If the string passed is not a valid number
   @since SuperWaba 2.0 */
  public static long toLong(String s, int radix) throws InvalidNumberException // guich@500_15: made this routine identical to its C equivalent.
  {
    if (s == null) {
      throw new java.lang.NullPointerException("Argument 's' cannot have a null value");
    }
    if (radix < 2 || radix > 16) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'radix'");
    }
    int i;
    long r = 0;
    long m = 1;
    char[] ac = s.toCharArray();
    int len = ac.length - 1;
    if (ac[len] == 'L' || ac[len] == 'l') {
      len--;
    }

    for (i = len; i >= 0; i--) {
      char c = ac[i];
      if (c == '+') {
        break;
      }
      if (c == '-') {
        r = -r;
        break;
      }

      int digit = digitOf(c, radix);
      if (digit < 0) {
        throw new InvalidNumberException("Error: " + s + " is not a valid long value.");
      }
      r += m * digit;
      m *= radix;
    }
    return r;
  }

  /** Converts the given double to a String, formatting it using scientific notation.
   @since SuperWaba 2.0 */
  public static String toString(double d) {
    return toString(d, -1); // guich@200b4_201: fixed exp notation
  }

  /** Converts the char to lower case letter */
  @ReplacedByNativeOnDeploy
  public static char toLowerCase(char c) {
    if (c <= 64) {
      return c;
    }
    if (('A' <= c && c <= 'Z') || ('\u00c0' <= c && c <= '\u00dd' && c != 215)) {
      return (char) (c + 32);
    }
    int idx = Constants.UPPER.indexOf("\u0000" + c); // eisvogel@450_20
    return (idx >= 0) ? Constants.LOWER.charAt(idx + 1) : (c);
  }

  /** Converts the char to upper case letter */
  @ReplacedByNativeOnDeploy
  public static char toUpperCase(char c) {
    if (c <= 64) {
      return c;
    }
    if (('a' <= c && c <= 'z') || ('\u00e0' <= c && c <= '\u00fd' && c != 247)) {
      return (char) (c - 32);
    }
    int idx = Constants.LOWER.indexOf("\u0000" + c); // eisvogel@450_20
    return (idx >= 0) ? Constants.UPPER.charAt(idx + 1) : c;
  }

  /** Pads the given string with the char 160 (that have the same width of a number), putting it before or after the string.
   * @since TotalCross 1.53
   */
  public static String numberPad(String s, int size) // guich@tc115_61
  {
    if (s == null) {
      throw new java.lang.NullPointerException("Argument 's' cannot have a null value");
    }
    int n = size - s.length();
    if (n > 0) {
      if (n >= sp160.length()) {
        sp160 = dup('\u00A0', n + 1);
      }
      return sp160.substring(0, n).concat(s);
    }
    return s;
  }

  /** Converts the int to String and pads it with the char 160 (that have the same width of a number) at left. 
   * @since TotalCross 1.53
   * @see #numberPad(String, int)
   */
  public static String numberPad(int s, int size) // guich@tc115_22
  {
    return numberPad(toString(s), size);
  }

  /** Pads the string with zeroes at left. */
  @ReplacedByNativeOnDeploy
  public static String zeroPad(String s, int size) {
    if (s == null) {
      throw new java.lang.NullPointerException("Argument 's' cannot have a null value");
    }
    int n = size - s.length();
    if (n > 0) {
      if (n >= Constants.zerosp.length()) {
        Constants.zerosp = dup('0', n + 1);
      }
      return Constants.zerosp.substring(0, n).concat(s);
    }
    return s;
  }

  /** Converts the int to String and pads it with zeroes at left. 
   * @since TotalCross 1.15
   * @see #zeroPad(String, int)
   */
  @ReplacedByNativeOnDeploy
  public static String zeroPad(int s, int size) // guich@tc115_22
  {
    return zeroPad(toString(s), size);
  }

  /**
   * Tokenize the given input string. If there's no delim chars in input, returns the input string. Two consecutive
   * delimeters results in an empty String, not in NULL.
   */
  public static String[] tokenizeString(String input, char delim) // guich@450_38: now using native indexOf
  {
    if (input == null) {
      throw new java.lang.NullPointerException("Argument 'input' cannot have a null value");
    }
    int position = 0, newPosition;
    int count = 1;
    while ((newPosition = input.indexOf(delim, position)) >= 0) {
      count++;
      position = newPosition + 1;
    }
    if (count == 1) {
      return new String[] { input }; // if no delims, return the input itself
    }
    String[] as = new String[count];
    // reuse the last index found
    as[count - 1] = input.substring(position, input.length());
    // get the other ones
    position = 0;
    int i = 0;
    while (--count > 0) {
      newPosition = input.indexOf(delim, position);
      as[i++] = input.substring(position, newPosition);
      position = newPosition + 1;
    }
    return as;
  }

  /**
   * Tokenize the given input string. If there's no delim chars in input, returns the input string. The delim parameter
   * is not a set of possible single characters: it is a whole string that is searched inside the given input.<br>
   * <br>
   * Note that this method is much slower than the one with same name, which receives a char instead of a String as the
   * delimiter.
   * 
   * @since SuperWaba 4.02
   * @author Kathrin Braunwarth
   */
  public static String[] tokenizeString(String input, String delim) // kb@402_40
  {
    if (input == null) {
      throw new java.lang.NullPointerException("Argument 'input' cannot have a null value");
    }
    if (delim == null) {
      throw new java.lang.NullPointerException("Argument 'delim' cannot have a null value");
    }
    Vector vtok = new Vector(10);
    int inc = delim.length();
    if (inc == 0 || input.length() == 0) {
      return new String[] { input }; // guich@566_21
    }
    int position = 0;
    boolean loop = true;
    while (loop) {
      int newPosition = input.indexOf(delim, position);
      if (newPosition == -1) {
        newPosition = input.length();
        loop = false;
      }
      if (position != newPosition) {
        vtok.addElement(input.substring(position, newPosition));
      }
      position = newPosition + inc;
    }
    return (String[]) vtok.toObjectArray();
  }

  /**
   * Splits the specified string around matches of the given delimiters. The characters in the delim argument are the
   * delimiters for separating tokens. Delimiter characters themselves will not be treated as tokens.
   * 
   * @param input
   *           a string to be parsed.
   * @param delims
   *           the delimiters.
   * @return the array of strings computed by splitting this string around matches of the given delimiters
   * @since TotalCross 1.15
   */
  public static String[] tokenizeString(String input, char... delims) {
    if (input == null || delims == null) {
      throw new NullPointerException();
    }
    if (delims.length == 1) {
      return tokenizeString(input, delims[0]);
    }

    Vector vtok = new Vector(10); // used below

    int inputLen = input.length();
    int start = 0;
    char[] inputChars = input.toCharArray();

    for (int i = 0; i < inputLen; i++) {
      for (char t : delims) {
        if (t == inputChars[i]) {
          if (i - start >= 0) {
            vtok.addElement(new String(inputChars, start, i - start));
          }
          start = i + 1;
          break;
        }
      }
    }
    if (start > 0 && start <= inputLen) {
      vtok.addElement(new String(inputChars, start, inputLen - start));
    }
    String[] ret = (String[]) vtok.toObjectArray();
    return ret == null ? new String[] { input } : ret;
  }

  /** This method is useful to insert line breaks into the text used in the MessageBox constructor. It receives
   * a String and returns the parsed String. Here is an example of use:
   * <code>Convert.insertLineBreak(Settings.screenWidth-10,getFontMetrics(getFont()),"This is a very long text and i dont want to waste my time parsing it to be fit in the MessageBox!");</code>
   * <br><br>
   * If you want to use another separator besides \n, just call replace('\n',separator) in the returned string.
   * @param maxWidth the maximum width that is permitted for each line. For a text used in the MessageBox class, use <code>Settings.screenWidth-6</code> or a lower number.
   * @param fm the FontMetrics of the font you will use to display the text
   * @param text the text to be parsed
   * @return the parsed text
   */
  public static String insertLineBreak(int maxWidth, totalcross.ui.font.FontMetrics fm, String text) // guich@200b4_30 - guich@tc100: changed to use the new StringBuffer functions
  {
    StringBuffer chars = new StringBuffer(text); // guich@tc114_76: change | to \n before applying our algorithm.
    int last = chars.length() - 1;
    for (int pos = 0; pos <= last; pos++) {
      pos = getBreakPos(fm, chars, pos, maxWidth, true);
      if (pos < 0 || pos > last) {
        break;
      }
      if (chars.charAt(pos) != '\n') {
        insertAt(chars, pos, '\n');
        last++;
      }
    }
    return chars.toString();
  }

  private static int getLineCount(int maxWidth, totalcross.ui.font.FontMetrics fm, String text) // guich@200b4_30 - guich@tc100: changed to use the new StringBuffer functions
  {
    StringBuffer chars = new StringBuffer(text); // guich@tc114_76: change | to \n before applying our algorithm.
    int last = chars.length() - 1;
    int lines = 1;
    for (int pos = 0; pos <= last; pos++) {
      pos = getBreakPos(fm, chars, pos, maxWidth, true);
      if (pos < 0 || pos > last) {
        break;
      }
      if (chars.charAt(pos) != '\n') {
        insertAt(chars, pos, '\n');
        last++;
        lines++;
      }
    }
    return lines;
  }

  /** This method is useful to insert line breaks into the text used in the Toast.show; it behaves like
   * insertLineBreak but tries to split the string in a balanced number of chars.
   * @see #insertLineBreak(int, FontMetrics, String)
   */
  public static String insertLineBreakBalanced(int maxWidth, totalcross.ui.font.FontMetrics fm, String text) // guich@200b4_30 - guich@tc100: changed to use the new StringBuffer functions
  {
    // check if the string already fits
    if (fm.stringWidth(text) <= maxWidth) {
      return text;
    }
    int parts = 2;
    StringBuffer sb = new StringBuffer(text);
    int lt = text.length();
    for (int i = 0; i < lt; i++, parts++) {
      int l = lt / parts; // finds the number of parts
      int ww = fm.sbWidth(sb, 0, l);
      if (ww < maxWidth) // does the first string already fits in the desired max width?
      {
        while (l < lt) // checks if the number of lines equals to the number of parts; if not, skip to next word and try again
        {
          int lines = getLineCount(ww + fm.height / 4, fm, text);
          if (lines == parts) {
            return insertLineBreak(ww + fm.height / 4, fm, text);
          }
          l++;
          while (l < lt && sb.charAt(l) != ' ') {
            l++;
          }
          ww = fm.sbWidth(sb, 0, l);
        }
      }
    }
    return insertLineBreak(maxWidth, fm, text);
  }

  /** Finds the best position to break the line, with word-wrap and respecting \n.
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public static int getBreakPos(FontMetrics fm, StringBuffer sb, int start, int width, boolean doWordWrap) {
    int oldStart = start;
    if (fm == null) {
      throw new java.lang.NullPointerException("Argument 'fm' cannot have a null value");
    }
    if (sb == null) {
      throw new java.lang.NullPointerException("Argument 'sb' cannot have a null value");
    }
    if (start < 0) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'start': " + start);
    }
    if (width < 0) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 'width': " + width);
    }
    int n = sb.length() - start;
    if (doWordWrap) {
      int lastSpace = -1;
      for (; n-- > 0 && width > 0; start++) {
        char c = sb.charAt(start);
        if (c == '\n') {
          return start;
        }
        if (c == ' ') {
          lastSpace = start;
        }
        width -= fm.charWidth(c);
      }
      start--; // stop at the previous letter
      if (((n > 0 || width < 0) || (width == 0 && sb.charAt(start) != ' ')) && lastSpace >= 0) {
        start = lastSpace <= start ? lastSpace : (lastSpace + 1); // the space is "included" in the previous word
      } else if (width < 0) {
        start = Math.max(oldStart, start - 1);
      }
      return start + 1;
    } else {
      for (; n-- > 0 && width > 0; start++) {
        width -= fm.charWidth(sb.charAt(start));
      }
      if (width < 0) {
        start--;
      }
      return start;
    }
  }

  /** To be used in the qsort method; the type of sort will be automatically detected.
   * @see #detectSortType(Object)
   */
  public static final int SORT_AUTODETECT = -1;
  /** To be used in the qsort method; the Object array will be compared converting to string. */
  public static final int SORT_OBJECT = 0;
  /** To be used in the qsort method; the Object array contain String objects (case sensitive). */
  public static final int SORT_STRING = 1;
  /** To be used in the qsort method; the Object array contain String objects that represents an integer. */
  public static final int SORT_INT = 2;
  /** To be used in the qsort method; the Object array contain String objects that represents a double. */
  public static final int SORT_DOUBLE = 3;
  /** To be used in the qsort method; the Object array contain String objects that represents a Date with day, month and year. */
  public static final int SORT_DATE = 4;
  /** To be used in the qsort method; the Object array contain Comparable objects. */
  public static final int SORT_COMPARABLE = 5;
  /** To be used in the qsort method; the Object array contain String objects, using a case insensitive (and slower) algorithm.
   * This mode is never set unless you specify it. */
  public static final int SORT_STRING_NOCASE = 6;

  /** Applies the Quick Sort algorithm to the elements of the given array.
   * The type of sort is automatically detected,
   * You may call this way: Convert.qsort(items,0,items.length-1).
   * You can sort subportions of the array as well.
   * @see #SORT_AUTODETECT
   */
  public static void qsort(Object[] items, int first, int last) // guich@220_34
  {
    qsort(items, first, last, SORT_AUTODETECT);
  }

  /** Returns the sort type for the given item sample (which is usually the first item of the array being sorted) based on:
   * <ul>
   * <li> If item is not of String type, SORT_OBJECT is returned. The toString method will be called for each Object to convert from Object to something comparable.
   * <li> If item starts with "0.0" or convert to double works, returns SORT_DOUBLE.
   * <li> If item equals "0" or converted to int works, returns SORT_INT.
   * <li> If item converted to totalcross.util.Date equals the column's data itself, returns SORT_DATE.
   * <li> else, the sort type is SORT_STRING.
   * </ul>
   * Important: if your data are floating point ones, be sure that the first element is NOT something
   * that can be converted to an integer. For example, be sure it is "2.0" and not "2" (converting "2.0"
   * to int results in 0, because the dot is not part of an integer number.
   * @see #SORT_AUTODETECT
   * @see #SORT_OBJECT
   * @see #SORT_STRING
   * @see #SORT_INT
   * @see #SORT_DOUBLE
   * @see #SORT_DATE
   * @see #SORT_COMPARABLE
   * @see #SORT_STRING_NOCASE
   */
  public static int detectSortType(Object item) // guich@566_1
  {
    int sortType;
    // autodetect the sort type
    if (item instanceof Comparable) {
      sortType = SORT_COMPARABLE;
    } else if (!(item instanceof String)) {
      sortType = SORT_OBJECT;
    } else {
      String s = (String) item;
      sortType = SORT_STRING;
      if (s.equals("0")) {
        sortType = SORT_INT;
      } else {
        try {
          toInt(s);
          sortType = SORT_INT;
        } catch (InvalidNumberException e) {
          if (s.startsWith("0.")) {
            sortType = SORT_DOUBLE;
          } else {
            try {
              toDouble(s);
              sortType = SORT_DOUBLE;
            } catch (InvalidNumberException ee) {
              try {
                new totalcross.util.Date(s);
                sortType = SORT_DATE; // no exception thrown...
              } catch (InvalidDateException ide) {
              } // is this a date in the platform-defined format?
            }
          }
        }
      }
    }
    return sortType;
  }

  /** Applies the Quick Sort algorithm to the elements of the given array, sorting in ascending order.
   * If they are Strings,
   * the sort will be much faster because a direct cast to String is done;
   * otherwise, if they are not strings, the toString() method is used to return
   * the string that will be used for comparision.
   * You may call this way:
   * <pre>
   * Convert.qsort(items,0,items.length-1, SORT_AUTODETECT).
   * </pre>
   * You can sort subportions of the array as well.
   * @see #SORT_AUTODETECT
   * @see #SORT_OBJECT
   * @see #SORT_STRING
   * @see #SORT_INT
   * @see #SORT_DOUBLE
   * @see #SORT_DATE
   * @see #SORT_COMPARABLE
   * @see #SORT_STRING_NOCASE
   * @see #detectSortType(Object)
   */
  public static void qsort(Object[] items, int first, int last, int sortType) // guich@566_1
  {
    qsort(items, first, last, sortType, true);
  }

  private static void qsortComparable(Object[] items, int first, int last, boolean ascending) {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    Comparable mid = (Comparable) items[(first + last) >> 1];
    while (true) {
      if (ascending) {
        while (high >= low && mid.compareTo(items[low]) > 0) {
          low++;
        }
        while (high >= low && mid.compareTo(items[high]) < 0) {
          high--;
        }
      } else {
        while (high >= low && mid.compareTo(items[low]) < 0) {
          low++;
        }
        while (high >= low && mid.compareTo(items[high]) > 0) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortComparable(items, first, high, ascending);
    }
    if (low < last) {
      qsortComparable(items, low, last, ascending);
    }
  }

  private static void qsortObject(Object[] items, int first, int last, boolean ascending) {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    String mid = items[(first + last) >> 1].toString();
    while (true) {
      if (ascending) {
        while (high >= low && mid.compareTo(items[low].toString()) > 0) {
          low++;
        }
        while (high >= low && mid.compareTo(items[high].toString()) < 0) {
          high--;
        }
      } else {
        while (high >= low && mid.compareTo(items[low].toString()) < 0) {
          low++;
        }
        while (high >= low && mid.compareTo(items[high].toString()) > 0) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortObject(items, first, high, ascending);
    }
    if (low < last) {
      qsortObject(items, low, last, ascending);
    }
  }

  private static void qsortString(Object[] items, int first, int last, boolean ascending) {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    String mid = (String) items[(first + last) >> 1];
    while (true) {
      if (ascending) {
        while (high >= low && mid.compareTo((String) items[low]) > 0) {
          low++;
        }
        while (high >= low && mid.compareTo((String) items[high]) < 0) {
          high--;
        }
      } else {
        while (high >= low && mid.compareTo((String) items[low]) < 0) {
          low++;
        }
        while (high >= low && mid.compareTo((String) items[high]) > 0) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortString(items, first, high, ascending);
    }
    if (low < last) {
      qsortString(items, low, last, ascending);
    }
  }

  private static void qsortStringNocase(Object[] items, int first, int last, boolean ascending) {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    String mid = ((String) items[(first + last) >> 1]).toLowerCase();
    while (true) {
      if (ascending) {
        while (high >= low && mid.compareTo(((String) items[low]).toLowerCase()) > 0) {
          low++;
        }
        while (high >= low && mid.compareTo(((String) items[high]).toLowerCase()) < 0) {
          high--;
        }
      } else {
        while (high >= low && mid.compareTo(((String) items[low]).toLowerCase()) < 0) {
          low++;
        }
        while (high >= low && mid.compareTo(((String) items[high]).toLowerCase()) > 0) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortStringNocase(items, first, high, ascending);
    }
    if (low < last) {
      qsortStringNocase(items, low, last, ascending);
    }
  }

  private static void qsortInt(Object[] items, int first, int last, boolean ascending) throws InvalidNumberException {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    int mid = toInt((String) items[(first + last) >> 1], Convert.MAX_INT_VALUE);
    while (true) {
      if (ascending) {
        while (high >= low && mid > toInt((String) items[low], Convert.MAX_INT_VALUE)) {
          low++;
        }
        while (high >= low && mid < toInt((String) items[high], Convert.MAX_INT_VALUE)) {
          high--;
        }
      } else {
        while (high >= low && mid < toInt((String) items[low], Convert.MAX_INT_VALUE)) {
          low++;
        }
        while (high >= low && mid > toInt((String) items[high], Convert.MAX_INT_VALUE)) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortInt(items, first, high, ascending);
    }
    if (low < last) {
      qsortInt(items, low, last, ascending);
    }
  }

  private static void qsortDouble(Object[] items, int first, int last, boolean ascending)
      throws InvalidNumberException {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    double mid = toDouble((String) items[(first + last) >> 1], Convert.MAX_DOUBLE_VALUE);
    while (true) {
      if (ascending) {
        while (high >= low && mid > toDouble((String) items[low], Convert.MAX_DOUBLE_VALUE)) {
          low++;
        }
        while (high >= low && mid < toDouble((String) items[high], Convert.MAX_DOUBLE_VALUE)) {
          high--;
        }
      } else {
        while (high >= low && mid < toDouble((String) items[low], Convert.MAX_DOUBLE_VALUE)) {
          low++;
        }
        while (high >= low && mid > toDouble((String) items[high], Convert.MAX_DOUBLE_VALUE)) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortDouble(items, first, high, ascending);
    }
    if (low < last) {
      qsortDouble(items, low, last, ascending);
    }
  }

  private static void qsortDate(Object[] items, int first, int last, boolean ascending) throws InvalidDateException {
    if (first >= last) {
      return;
    }
    int low = first;
    int high = last;

    Date d = new Date();
    byte df = Settings.dateFormat;

    int mid = d.set((String) items[(first + last) >> 1], df);

    while (true) {
      if (ascending) {
        while (high >= low && mid > d.set((String) items[low], df)) {
          low++;
        }
        while (high >= low && mid < d.set((String) items[high], df)) {
          high--;
        }
      } else {
        while (high >= low && mid < d.set((String) items[low], df)) {
          low++;
        }
        while (high >= low && mid > d.set((String) items[high], df)) {
          high--;
        }
      }
      if (low <= high) {
        Object temp = items[low];
        items[low++] = items[high];
        items[high--] = temp;
      } else {
        break;
      }
    }

    if (first < high) {
      qsortDate(items, first, high, ascending);
    }
    if (low < last) {
      qsortDate(items, low, last, ascending);
    }
  }

  /** Applies the Quick Sort algorithm to the elements of the given array, in ascending or descending order.
   * If they are Strings,
   * the sort will be much faster because a direct cast to String is done;
   * otherwise, if they are not strings, the toString() method is used to return
   * the string that will be used for comparision.
   * You may call this way:
   * <pre>
   * Convert.qsort(items,0,items.length-1, Convert.SORT_DATE, true).
   * </pre>
   * You can sort subportions of the array as well.<br>
   * Use the SORT_xxx to define the sort type, or SORT_AUTODETECT to
   * automatically detect it.
   * @see #SORT_AUTODETECT
   * @see #SORT_OBJECT
   * @see #SORT_STRING
   * @see #SORT_INT
   * @see #SORT_DOUBLE
   * @see #SORT_DATE
   * @see #SORT_COMPARABLE
   * @see #SORT_STRING_NOCASE
   * @see #detectSortType(Object)
   */
  public static void qsort(Object[] items, int first, int last, int sortType, boolean ascending) // guich@566_1
  {
    if (sortType == SORT_AUTODETECT) {
      sortType = detectSortType(items[first]);
    }
    try {
      switch (sortType) {
      case SORT_OBJECT:
        qsortObject(items, first, last, ascending);
        break;
      case SORT_STRING:
        qsortString(items, first, last, ascending);
        break;
      case SORT_STRING_NOCASE:
        qsortStringNocase(items, first, last, ascending);
        break;
      case SORT_INT:
        try {
          qsortInt(items, first, last, ascending);
        } catch (InvalidNumberException ine) {
          qsortDouble(items, first, last, ascending);
        }
        break;
      case SORT_DOUBLE:
        qsortDouble(items, first, last, ascending);
        break;
      case SORT_DATE:
        qsortDate(items, first, last, ascending);
        break;
      case SORT_COMPARABLE:
        qsortComparable(items, first, last, ascending);
        break;
      }
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Returns a String with the conversion of the unsigned integer to hexadecimal, using the given number of places (up
   * to 8). This routine is 90 times faster than toString(long, int). The hex digits are in upper case. If places is
   * smaller than the total number of digits, then the number will be truncated, and the lower part of the number will
   * be returned.
   * 
   * @since SuperWaba 3.0
   */
  @ReplacedByNativeOnDeploy
  public static String unsigned2hex(int b, int places) // guich@300_8
  {
    if (places < 0 || places > 8) {
      throw new IllegalArgumentException("Invalid value '" + places + "' for argument '" + places + "'"); // guich@tc123_9
    }
    byte[] c = new byte[places];
    for (int i = places - 1; i >= 0; i--) {
      c[i] = (byte) b2h[b & 0xF];
      b >>= 4;
    }
    return new String(c);
  }

  /**
   * Returns an int with the conversion of the unsigned hexadecimal to integer. The hex digits can be in upper case or
   * lower case.
   * 
   * @since TotalCross 1.0
   */
  public static int hex2unsigned(String s) {
    int places;
    if ((places = s.length()) > 8) {
      throw new java.lang.IllegalArgumentException("Invalid value for argument 's'");
    }

    char[] c = s.toCharArray();
    int b = 0, i = 0;

    while (--places >= 0) {
      b <<= 4;
      b |= h2b[c[i++]];
    }

    return b;
  }

  /**
   * Returns an int with the conversion of the signed hexadecimal to integer. The hex digits can be in upper case or
   * lower case.
   * 
   * @param s
   *           the string representation of the signed hexadecimal
   * @return a signed integer with the converted value
   * @since TotalCross 1.15
   */
  public static int hex2signed(String s) {
    if (s.charAt(0) == '-') {
      return -hex2unsigned(s.substring(1));
    }
    return hex2unsigned(s);
  }

  /** Converts the given sequence of bytes to a String.
   * The hexadecimal String is in upper case.
   * @since TotalCross 1.0 beta 4 */
  public static String bytesToHexString(byte[] b) {
    return bytesToHexString(b, 0, b.length);
  }

  /** Converts the given sequence of bytes to a String.
   * The hexadecimal String is in upper case.
   * @since TotalCross 1.01 */
  public static String bytesToHexString(byte[] b, int off, int len) {
    StringBuffer sb = new StringBuffer(25);
    for (int count = off + len, i = 0; --count >= off; i++) {
      sb.append(b2h[((b[i] >> 4) & 0xF)]);
      sb.append(b2h[(b[i] & 0xF)]);
    }
    return sb.toString();
  }

  /** Converts the given String into an array of bytes.
   * Each two consecutive characters are converted into a byte.
   * @param s The hex string to convert to bytes
   * @since TotalCross 1.0 beta 4
   */
  public static byte[] hexStringToBytes(String s) {
    return hexStringToBytes(s, false);
  }

  /** Converts the given String into an array of bytes.
   * Each two consecutive characters are converted into a byte.
   * @param s The hex string to convert to bytes
   * @param ignoreNonHexChars Flag indicating if non hex chars should be ignored or not
   * @since TotalCross 1.0 beta 5
   */
  public static byte[] hexStringToBytes(String s, boolean ignoreNonHexChars) {
    char[] chars = s.toCharArray();
    int count = chars.length;

    byte[] buf = new byte[count];
    boolean firstChar = true;
    int pos = 0;
    byte b, invalid = h2bInvalid;

    for (int i = 0; --count >= 0; i++) {
      b = h2b[chars[i]];
      if (b == invalid) // non hex char
      {
        if (!ignoreNonHexChars) {
          throw new IllegalArgumentException("The string has one or more non hex characters.");
        }
      } else {
        if (firstChar) {
          buf[pos] = b;
        } else {
          buf[pos] = (byte) ((buf[pos] << 4) | b);
          pos++;
        }

        firstChar = !firstChar;
      }
    }

    if (!firstChar) {
      pos++;
    }

    byte[] bytes = new byte[pos];
    Vm.arrayCopy(buf, 0, bytes, 0, pos);

    return bytes;
  }

  /** Do a rol of n bits in the given i(nt). n must be < <code>bits</code>. This differs from the
   * shift left operator (<<) in that the bits are not lost,
   * they are reinserted in order at the right.
   * @param i The positive number to be rolled
   * @param n The number of bits to be rolled, where 0 &lt;= n &lt;= bits
   * @param bits The number of bits that will be considered, where 0 &lt; bits &lt;= 64. Bits outside the bit range are masked out. If 0 is given, 64 will be used.
   * In order to make it faster, the routine does not check for out-of-bounds parameters.
   */
  public static long rol(long i, int n, int bits) // guich@330_8: moved to here to make Math compatible with JDK versions.
  {
    long mask = (1L << bits) - 1L; // peterd@450_26: fixed this routine
    if (mask > 0) {
      i &= mask;
    }
    i = ((i >>> (bits - n)) | (i << n));
    if (mask > 0) {
      i &= mask;
    }
    return i;
  }

  /** Do a ror of n bits in the given i(nt). n must be < <code>bits</code>. This differs from the
   * shift right operator (>>) in that the bits are not lost,
   * they are reinserted in order at the left.
   * @param i The positive number to be rolled
   * @param n The number of bits to be rolled, where 0 &lt;= n &lt;= bits
   * @param bits The number of bits that will be considered, where 0 &lt; bits &lt;= 64 Bits outside the bit range are masked out. If 0 is given, 64 will be used.
   * In order to make it faster, the routine does not check for out-of-bounds parameters.
   */
  public static long ror(long i, int n, int bits) {
    if (bits <= 0) {
      return 0; // guich@566_22
    }
    long mask = (1L << bits) - 1L; // peterd@450_26: fixed this routine
    if (mask > 0) {
      i &= mask;
    }
    i = (i >>> n) | (i << (bits - n));
    if (mask > 0) {
      i &= mask;
    }
    return i;
  }

  /** Convert a creator id or a type to an int.
   * @param fourChars Four characters representing a creator id.
   */
  public static int chars2int(String fourChars) // guich@330_17
  {
    return (fourChars.charAt(0) << 24) | (fourChars.charAt(1) << 16) | (fourChars.charAt(2) << 8) | fourChars.charAt(3);
  }

  /** Convert an int to a creator id or a type. */
  public static String int2chars(int i) // brunosoares@tc100
  {
    byte[] chars = new byte[4];
    chars[0] = (byte) (i >> 24);
    chars[1] = (byte) (i >> 16);
    chars[2] = (byte) (i >> 8);
    chars[3] = (byte) i;

    return new String(chars);
  }

  /** Duplicates the given char <code>count</code> times.
   * @since SuperWaba 5.72
   * @throws IllegalArgumentException If count is below 0
   */
  @ReplacedByNativeOnDeploy
  public static String dup(char c, int count) // guich@572_17
  {
    if (count < 0) {
      throw new IllegalArgumentException("count can't be below 0: " + count);
    }
    char buf[] = new char[count];
    fill(buf, 0, count, c);
    return new String(buf);
  }

  /** Gets the hashcode of the string being stored by this StringBuffer.
   * This saves memory since there's no need to convert the StringBuffer to a String.
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public static int hashCode(StringBuffer sb) {
    if (sb == null) {
      throw new java.lang.NullPointerException("Argument 'sb' cannot have a null value");
    }

    int hash = 0, length = sb.length(), i = 0;

    while (--length >= 0) {
      hash = (hash << 5) - hash + (int) sb.charAt(i++);
    }
    return hash;
  }

  /** Removes the given set of chars from the String.
   * Example:
   * <pre>
   * String s = Convert.remove("abcdef","df"); // returns "abce";
   * </pre>
   * @since TotalCross 3.05
   */
  public static String remove(String source, String chars) {
    if (chars.length() == 1) {
      return replace(source, chars, "");
    }
    int len = source.length();
    StringBuffer sb = new StringBuffer(len);
    char ch;
    for (int i = 0; i < len; i++) {
      if (chars.indexOf(ch = source.charAt(i)) == -1) {
        sb.append(ch);
      }
    }
    return sb.length() == len ? source : sb.toString();
  }

  /** Replace all occurences of the <code>from</code> String by the <code>to</code> String in the given <code>source</code> String.
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public static String replace(String source, String from, String to) {
    // guich@tc123_10: pre-compute the number of changes and create a single string to store the result. 
    // The pattern is searched inside the String and replaced inside the target string
    int f = from.length();
    int t = to.length();
    if (f == 1 && t == 1) {
      return source.replace(from.charAt(0), to.charAt(0));
    }
    int last = 0, tlast = 0;
    // count how many times the string appears
    int count = 0;
    while (last >= 0) {
      last = source.indexOf(from, last);
      if (last == -1) {
        break;
      }
      count++;
      last += f;
    }
    if (count == 0) {
      return source;
    }
    int s = source.length();
    char[] target = new char[s + (t - f) * count]; // create a string with the new length
    last = 0;
    while (last >= 0) {
      int now = source.indexOf(from, last);
      if (now == -1) {
        // copy to the end of the string
        if (s != last) {
          source.getChars(last, s, target, tlast);
        }
        break;
      }
      source.getChars(last, now, target, tlast); // copy from old position into the new one
      tlast += now - last;
      last = now;
      to.getChars(0, t, target, tlast);
      last += f;
      tlast += t;
    }
    return new String(target);
  }

  /** Returns the number of chars in a String.
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public static int numberOf(String s, char c) {
    int n = 0;
    for (int i = s.length() - 1; i >= 0; i--) {
      if (s.charAt(i) == c) {
        n++;
      }
    }
    return n;
  }

  /** Inserts the given char at the position in the StringBuffer. This is to keep compatibility with JDK 1.1.x in browsers, because
   * StringBuffer.insert was introduced in JDK 1.2 only.
   * @since TotalCross 1.0
   */
  @ReplacedByNativeOnDeploy
  public static void insertAt(StringBuffer sb, int pos, char c) {
    int n = sb.length();
    sb.append(c);
    if (pos < n) {
      for (int i = n; i > pos; i--) {
        sb.setCharAt(i, sb.charAt(i - 1));
      }
      sb.setCharAt(pos, c);
    }
  }

  /** This method appends a number of characters into a StringBuffer. It greatly reduces the amount of
   * memory needed. For example, if you're building a fixed-width line with spaces, you can do:
   * <pre>
   * int dif = colWidth - value.length();
   * sb.apppend(value);
   * Convert.append(sb, ' ', dif); // pad column with spaces
   * </pre>
   * ... where sb is the StringBuffer you're using to concatenate the strings, value is the value you're appending,
   * and colWidth is the current column width.
   * @throws ArrayIndexOutOfBoundsException If count is below 0.
   * @since TotalCross 1.2
   */
  @ReplacedByNativeOnDeploy
  public static void append(StringBuffer sb, char c, int count) {
    if (c < 0) {
      throw new ArrayIndexOutOfBoundsException("Count must be >= 0: " + count);
    }
    sb.append(dup(c, count));
  }

  /** Returns the bytes of the given StringBuffer using the current CharacterConverter instance.
   * This can help save memory since it does not create the intermediate String, in the usual code:
   * <pre>
   * byte[] b = sb.toString().getBytes();
   * </pre>
   * There's no performance gain in Blackberry nor in Java SE.
   * @since TotalCross 1.23
   */
  @ReplacedByNativeOnDeploy
  public static byte[] getBytes(StringBuffer sb) {
    String s = sb.toString();
    if (charConverter instanceof UTF8CharacterConverter) {
      try {
        return s.getBytes("UTF-8");
      } catch (UnsupportedEncodingException uee) {
      }
    }
    return s.getBytes();
  }

  /** Strips the left zeros of the String. Note that Convert.toInt DOES support zeros before the number, 
   * so there's no need to call this method before converting "0011" into 11.
   * @since TotalCross 1.0
   */
  public static String zeroUnpad(String s) {
    int i = 0;
    for (int n = s.length(); i < n && s.charAt(i) == '0';) {
      i++;
    }
    return i == 0 ? s : s.substring(i);
  }

  /** Returns the maximum value among the given ints
   * @since TotalCross 1.15
   */
  public static int max(int... ai) // guich@tc115_61
  {
    int m = ai[0];
    for (int i = ai.length; --i >= 1;) {
      if (ai[i] > m) {
        m = ai[i];
      }
    }
    return m;
  }

  /** Returns the sum of the given ints, in the given range (startIndex <= x < endIndex).
   * @since TotalCross 2.22
   */
  public static int sum(int[] ai, int startIndex, int endIndex) // guich@tc115_61
  {
    int m = 0;
    for (int i = startIndex; i < endIndex; i++) {
      m += ai[i];
    }
    return m;
  }

  /** Returns the sum of the given ints
   * @since TotalCross 1.15
   */
  public static int sum(int... ai) // guich@tc115_61
  {
    int m = 0;
    for (int i = ai.length; --i >= 0;) {
      m += ai[i];
    }
    return m;
  }

  /** Returns the minimum value among the given ints
   * @since TotalCross 1.15
   */
  public static int min(int... ai) // guich@tc115_61
  {
    int m = ai[0];
    for (int i = ai.length; --i >= 1;) {
      if (ai[i] < m) {
        m = ai[i];
      }
    }
    return m;
  }

  /** Pads the given string with spaces, putting it before or after the string.
   * If count is less than the string's length, the string is returned without being changed.
   * @since TotalCross 1.15
   */
  @ReplacedByNativeOnDeploy
  public static String spacePad(String what, int count, boolean before) // guich@tc115_61
  {
    count -= what.length();
    if (count <= 0) {
      return what;
    }
    if (Constants.spaces.length() < count) {
      Constants.spaces = dup(' ', count);
    }
    String sp = Constants.spaces.substring(0, count);
    return before ? sp.concat(what) : what.concat(sp);
  }

  /** Concatenates two strings ensuring there's a single slash between them. Strings are normalized using / as path separator 
   * and removing any duplicate separators. You must ensure that both strings are not null neither empty.
   * @since TotalCross 1.0
   */
  public static String appendPath(String path1, String path2) {
    return normalizePath(path1 + "/" + path2);
  }

  /**
   * Normalizes a string using / as the path separator and removing any duplicate separators.
   * @param path The string to normalize.
   * @return The normalized string.
   * @since TotalCross 1.20
   */
  public static String normalizePath(String path) //bruno@tc125_9: fixed to correctly remove unnecessary consecutive slashes (directory separators).
  {
    String onlySlashes = path.replace('\\', '/');
    while (onlySlashes.indexOf("//") >= 0) {
      onlySlashes = replace(onlySlashes, "//", "/");
    }
    return onlySlashes;
  }

  /** Converts the given int array into a byte array. The int array must be in big endian format (least significant bits at last).
   * This helps decrease file size if you're storing in the code an array of bytes.
   * <b>If the byte array has 13 or more elements, using this technique will decrease the file's size.</b>
   * <br><br>For example, the array
   * <code>{(byte)0x12,(byte)0x34,(byte)0x56,(byte)0x78,(byte)0xAB,(byte)0xCD,(byte)0xEF}</code> must be stored as
   * <code>{0x78563412,0x00EFCDAB}</code> and you must call this method passing 7 for count.
   * <br><br>You can use the following method to dump to the console the convertion of your byte arrays:
   * <pre>
   * static void bytes2intsDump(byte[] in)
   * {
   *    System.out.print("Convert.ints2bytes(new int[]{");
   *    for (int i = 0; i < in.length;)
   *    {
   *       int b1 = i < in.length ? in[i++] : 0;
   *       int b2 = i < in.length ? in[i++] : 0;
   *       int b3 = i < in.length ? in[i++] : 0;
   *       int b4 = i < in.length ? in[i++] : 0;
   *       int out = b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
   *       System.out.print("0x"+Convert.unsigned2hex(out,8)+",");
   *    }
   *    System.out.println("}, "+in.length+");");
   * }
   * </pre>
   * For example, calling it with:
   * <pre>
   * byte[] in = {1,2,3,4,5,6,7,8,1,2,3,4,5,6,7};
   * bytes2intsDump(in);
   * </pre>
   * Will output this:
   * <pre>
   * Convert.ints2bytes(new int[]{0x04030201,0x08070605,0x04030201,0x00070605}, 15);
   * </pre>
   * Note that you must not include the bytes2intsDump into the program that is going to the device. It is a desktop-only utility method.
   * @param from The source int array
   * @param count The length of the target byte array. It may differ from <code>from.length*4</code> because the last value of the array may be padded.
   * @since TotalCross 1.01
   */
  public static byte[] ints2bytes(int[] from, int count) {
    byte[] to = new byte[count];
    for (int j = 0, t = 0; j < count; t >>>= 8) {
      if ((j & 3) == 0) {
        t = from[j >> 2];
      }
      to[j++] = (byte) (t & 0xFF);
    }
    return to;
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to. */
  @ReplacedByNativeOnDeploy
  public static void fill(char[] a, int from, int to, char value) {
    try {
      java.util.Arrays.fill(a, from, to, value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to. */
  @ReplacedByNativeOnDeploy
  public static void fill(boolean[] a, int from, int to, boolean value) {
    try {
      java.util.Arrays.fill(a, from, to, value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to. */
  @ReplacedByNativeOnDeploy
  public static void fill(int[] a, int from, int to, int value) {
    try {
      java.util.Arrays.fill(a, from, to, value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to. */
  @ReplacedByNativeOnDeploy
  public static void fill(double[] a, int from, int to, double value) {
    try {
      java.util.Arrays.fill(a, from, to, value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to.
   * The int value is casted to short. */
  @ReplacedByNativeOnDeploy
  public static void fill(short[] a, int from, int to, int value) {
    try {
      java.util.Arrays.fill(a, from, to, (short) value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = (short) value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to.
   * The int value is casted to byte. */
  @ReplacedByNativeOnDeploy
  public static void fill(byte[] a, int from, int to, int value) {
    try {
      java.util.Arrays.fill(a, from, to, (byte) value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = (byte) value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to. */
  @ReplacedByNativeOnDeploy
  public static void fill(long[] a, int from, int to, long value) {
    try {
      java.util.Arrays.fill(a, from, to, value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = value;
      }
    }
  }

  /** Fills the given array, within the range, with the value specified. The array is filled as from &lt;= n &lt; to. */
  @ReplacedByNativeOnDeploy
  public static void fill(Object[] a, int from, int to, Object value) {
    try {
      java.util.Arrays.fill(a, from, to, value);
    } catch (Throwable t) // jdk 1.x
    {
      for (; from < to; from++) {
        a[from] = value;
      }
    }
  }

  /** Returns the given double formatted using the Settings decimal and thousands separators.
   * @param d the double to be converted to String.
   * @param decimalPlaces the number of decimal places. Must be >= -1.
   * @since TotalCross 1.01
   * @see #toCurrencyString(String,int)
   */
  public static String toCurrencyString(double d, int decimalPlaces) // guich@tc110_76
  {
    return toCurrencyString(toString(d, decimalPlaces), decimalPlaces);
  }

  /** Returns the given double formatted using the Settings decimal and thousands separators.
   * @param s the double String to be converted to String. It must already have the given number of decimal places.
   * @param decimalPlaces the number of decimal places. Must be >= -1.
   * @since TotalCross 1.14
   * @see #toCurrencyString(double,int)
   */
  public static String toCurrencyString(String s, int decimalPlaces) // guich@tc110_76
  {
    if (Settings.decimalSeparator != '.' && decimalPlaces != 0) {
      s = s.replace('.', Settings.decimalSeparator);
    }
    // insert the thousands separators
    boolean isNegative = s.charAt(0) == '-';
    if (isNegative) {
      s = s.substring(1);
    }
    for (int i = s.length() - decimalPlaces - (decimalPlaces != 0 ? 4 : 3); i > 0; i -= 3) {
      s = s.substring(0, i) + Settings.thousandsSeparator + s.substring(i);
    }
    if (isNegative) {
      s = '-' + s;
    }
    return s;
  }

  /** Tokenizes a command line arguments, breaking it into an array of strings. Useful when getting parameters from MainWindow.getCommandLine as an
   * unique string and changing it to a String array as in Java's main method. Supports parsing strings with quotes, but the quote itself is not returned.
   * Consecutive spaces outside "" are ignored.
   * @since TotalCross 1.2
   */
  public static String[] tokenizeArguments(String arg) // guich@tc120_9
  {
    if (arg == null) {
      return null;
    }

    Vector v = new Vector(10);
    char[] chars = arg.toCharArray();
    StringBuffer sb = new StringBuffer(50);
    for (int i = 0; i < chars.length; i++) {
      switch (chars[i]) {
      case ' ':
        if (sb.length() > 0) {
          v.addElement(sb.toString());
        }
        sb.setLength(0);
        break;
      case '"':
        while (++i < chars.length && chars[i] != '"') {
          sb.append(chars[i]);
        }
        v.addElement(sb.toString());
        sb.setLength(0);
        break;
      default:
        sb.append(chars[i]);
      }
    }
    if (sb.length() > 0) {
      v.addElement(sb.toString());
    }
    return (String[]) v.toObjectArray();
  }

  /** Returns true if the given char is in uppercase. Supports unicode.
   * @since TotalCross 1.13
   */
  public static boolean isUpperCase(char c) // guich@tc113_4
  {
    return ('A' <= c && c <= 'Z') || c == toUpperCase(c);
  }

  /** Returns true if the given char is in lowercase. Supports unicode.
   * @since TotalCross 1.13
   */
  public static boolean isLowerCase(char c) // guich@tc113_4
  {
    return ('a' <= c && c <= 'z') || c == toLowerCase(c);
  }

  /** Computes the distance between a point (x,y) 
   * and a rectangle (corners x1,y1 and x2,y2).
   * Note that, if the point is inside the rect, 0 is returned.
   * @since TotalCross 1.2
   */
  public static double getDistancePoint2Rect(int x, int y, int x1, int y1, int x2, int y2) {
    int xMx1 = x - x1;
    int xMx2 = x - x2;
    int yMy1 = y - y1;
    int yMy2 = y - y2;

    if (y < y1) {
      if (x < x1) {
        return Math.sqrt(xMx1 * xMx1 + yMy1 * yMy1); // distance from point (x,y) to point (x1,y1)
      } else if (x < x2) {
        return y1 - y; // distance from point (x,y) to line (x1,y1)->(x2,y1)
      } else {
        return Math.sqrt(xMx2 * xMx2 + yMy1 * yMy1); // distance from point (x,y) to point (x2,y1)
      }
    } else if (y < y2) {
      if (x < x1) {
        return x1 - x; // distance from point (x,y) to line (x1,y1)->(x1,y2)
      } else if (x < x2) {
        return 0;
      } else {
        return x - x2; // distance from point (x,y) to line (x2,y1)->(x2,y2)
      }
    } else {
      if (x < x1) {
        return Math.sqrt(xMx1 * xMx1 + yMy2 * yMy2); // distance from point (x,y) to point (x1,y2)
      } else if (x < x2) {
        return y - y2; // distance from point (x,y) to line (x1,y2)->(x2,y2)
      } else {
        return Math.sqrt(xMx2 * xMx2 + yMy2 * yMy2); // distance from point (x,y) to point (x2,y2)
      }
    }
  }

  /** Appends the given timestamp to a StringBuffer, using the current Date format and separators.
   * @since TotalCross 1.24 
   */
  public static StringBuffer appendTimeStamp(StringBuffer sb, Time t, boolean appendDate, boolean appendTime) // guich@tc123_62
  {
    if (appendDate) {
      int i1, i2, i3;
      if (Settings.dateFormat == Settings.DATE_DMY) {
        i1 = t.day;
        i2 = t.month;
        i3 = t.year;
      } else if (Settings.dateFormat == Settings.DATE_MDY) {
        i1 = t.month;
        i2 = t.day;
        i3 = t.year;
      } else {
        i1 = t.year;
        i2 = t.month;
        i3 = t.day;
      }
      if (i1 < 10) {
        sb.append("0");
      }
      sb.append(i1).append(Settings.dateSeparator);
      if (i2 < 10) {
        sb.append("0");
      }
      sb.append(i2).append(Settings.dateSeparator);
      if (i3 < 10) {
        sb.append("0");
      }
      sb.append(i3);
    }
    if (appendDate && appendTime) {
      sb.append(" ");
    }
    t.dump(sb, Settings.timeSeparator == ':' ? ":" : String.valueOf(Settings.timeSeparator), true);
    return sb;
  }

  /** Given a full path name, returns the normalized path of the file, without the slash. If the file contains
   * only a name, without slashes, returns null.
   * @since TotalCross 1.27
   */
  public static String getFilePath(String fullPath) // guich@tc126_6
  {
    fullPath = normalizePath(fullPath);
    int idx = fullPath.lastIndexOf('/');
    return idx == -1 ? null : fullPath.substring(0, idx);
  }

  /** Given a full path name, returns the name of the file. If the file contains
   * only a name, without slashes, returns the fullPath.
   * @since TotalCross 1.27
   */
  public static String getFileName(String fullPath) // guich@tc126_6
  {
    fullPath = normalizePath(fullPath);
    int idx = fullPath.lastIndexOf('/');
    return idx == -1 ? fullPath : fullPath.substring(idx + 1);
  }

  /** Given a full path name or a single file name, returns the name of the file without the extension.
   * @since TotalCross 1.27
   */
  public static String getFileNameOnly(String fullPath) // guich@tc126_6
  {
    String name = getFileName(fullPath);
    int idx = name.lastIndexOf('.');
    return idx == -1 ? name : name.substring(0, idx);
  }

  /////// double routines - same of vm //////////////////////////////////

  /* Tester - currently 204 diffs
      int dif=0;
      for (int i =-324; i <= 308; i++)
      {
         double d = pow10(i);
         double e = Math.pow(10,i);
         if (d != e)
         {
            dif++;
            System.out.println(i+" : "+d+ " != "+e+"  -  "+Long.toHexString(Convert.doubleToLongBits(d))+" != "+Long.toHexString(Convert.doubleToLongBits(e)));
         }
      }
      System.out.println(dif);
   */
  private static String sp160 = "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"; // guich@tc100: optimized the zeroPad method
  public static final long DOUBLE_POSITIVE_INFINITY_BITS = 0x7ff0000000000000L;
  public static final long DOUBLE_NEGATIVE_INFINITY_BITS = 0xfff0000000000000L;
  public static final long DOUBLE_NAN_BITS = 0x7ff8000000000000L;
  public static final double DOUBLE_POSITIVE_INFINITY_VALUE = longBitsToDouble(DOUBLE_POSITIVE_INFINITY_BITS);
  public static final double DOUBLE_NEGATIVE_INFINITY_VALUE = longBitsToDouble(DOUBLE_NEGATIVE_INFINITY_BITS);
  public static final double DOUBLE_NAN_VALUE = longBitsToDouble(DOUBLE_NAN_BITS);

  static class Constants // get rid of symbols not used in device.
  {
    private static String spaces = "";
    private static String zerosp = "0000000000"; // guich@tc100: optimized the zeroPad method
    private final static String LOWER = "\u0000\u0131\u0000\u03BC\u0000\u0101\u0000\u0103\u0000\u0105\u0000\u0107\u0000\u0109\u0000\u010B\u0000\u010D\u0000\u010F\u0000\u0111\u0000\u0113\u0000\u0115\u0000\u0117\u0000\u0119\u0000\u011B\u0000\u011D\u0000\u011F\u0000\u0121\u0000\u0123\u0000\u0125\u0000\u0127\u0000\u0129\u0000\u012B\u0000\u012D\u0000\u012F\u0000\u0069\u0000\u0133\u0000\u0135\u0000\u0137\u0000\u013A\u0000\u013C\u0000\u013E\u0000\u0140\u0000\u0142\u0000\u0144\u0000\u0146\u0000\u0148\u0000\u014B\u0000\u014D\u0000\u014F\u0000\u0151\u0000\u0153\u0000\u0155\u0000\u0157\u0000\u0159\u0000\u015B\u0000\u015D\u0000\u015F\u0000\u0161\u0000\u0163\u0000\u0165\u0000\u0167\u0000\u0169\u0000\u016B\u0000\u016D\u0000\u016F\u0000\u0171\u0000\u0173\u0000\u0175\u0000\u0177\u0000\u00FF\u0000\u017A\u0000\u017C\u0000\u017E\u0000\u0073\u0000\u0253\u0000\u0183\u0000\u0185\u0000\u0254\u0000\u0188\u0000\u0256\u0000\u0257\u0000\u018C\u0000\u01DD\u0000\u0259\u0000\u025B\u0000\u0192\u0000\u0260\u0000\u0263\u0000\u0269\u0000\u0268\u0000\u0199\u0000\u026F\u0000\u0272\u0000\u0275\u0000\u01A1\u0000\u01A3\u0000\u01A5\u0000\u0280\u0000\u01A8\u0000\u0283\u0000\u01AD\u0000\u0288\u0000\u01B0\u0000\u028A\u0000\u028B\u0000\u01B4\u0000\u01B6\u0000\u0292\u0000\u01B9\u0000\u01BD\u0000\u01C6\u0000\u01C6\u0000\u01C9\u0000\u01C9\u0000\u01CC\u0000\u01CC\u0000\u01CE\u0000\u01D0\u0000\u01D2\u0000\u01D4\u0000\u01D6\u0000\u01D8\u0000\u01DA\u0000\u01DC\u0000\u01DF\u0000\u01E1\u0000\u01E3\u0000\u01E5\u0000\u01E7\u0000\u01E9\u0000\u01EB\u0000\u01ED\u0000\u01EF\u0000\u01F3\u0000\u01F3\u0000\u01F5\u0000\u0195\u0000\u01BF\u0000\u01F9\u0000\u01FB\u0000\u01FD\u0000\u01FF\u0000\u0201\u0000\u0203\u0000\u0205\u0000\u0207\u0000\u0209\u0000\u020B\u0000\u020D\u0000\u020F\u0000\u0211\u0000\u0213\u0000\u0215\u0000\u0217\u0000\u0219\u0000\u021B\u0000\u021D\u0000\u021F\u0000\u019E\u0000\u0223\u0000\u0225\u0000\u0227\u0000\u0229\u0000\u022B\u0000\u022D\u0000\u022F\u0000\u0231\u0000\u0233\u0000\u03B9\u0000\u03AC\u0000\u03AD\u0000\u03AE\u0000\u03AF\u0000\u03CC\u0000\u03CD\u0000\u03CE\u0000\u03B1\u0000\u03B2\u0000\u03B3\u0000\u03B4\u0000\u03B5\u0000\u03B6\u0000\u03B7\u0000\u03B8\u0000\u03B9\u0000\u03BA\u0000\u03BB\u0000\u03BC\u0000\u03BD\u0000\u03BE\u0000\u03BF\u0000\u03C0\u0000\u03C1\u0000\u03C3\u0000\u03C4\u0000\u03C5\u0000\u03C6\u0000\u03C7\u0000\u03C8\u0000\u03C9\u0000\u03CA\u0000\u03CB\u0000\u03C3\u0000\u03B2\u0000\u03B8\u0000\u03C6\u0000\u03C0\u0000\u03D9\u0000\u03DB\u0000\u03DD\u0000\u03DF\u0000\u03E1\u0000\u03E3\u0000\u03E5\u0000\u03E7\u0000\u03E9\u0000\u03EB\u0000\u03ED\u0000\u03EF\u0000\u03BA\u0000\u03C1\u0000\u03B8\u0000\u03B5\u0000\u03F8\u0000\u03F2\u0000\u03FB\u0000\u0450\u0000\u0451\u0000\u0452\u0000\u0453\u0000\u0454\u0000\u0455\u0000\u0456\u0000\u0457\u0000\u0458\u0000\u0459\u0000\u045A\u0000\u045B\u0000\u045C\u0000\u045D\u0000\u045E\u0000\u045F\u0000\u0430\u0000\u0431\u0000\u0432\u0000\u0433\u0000\u0434\u0000\u0435\u0000\u0436\u0000\u0437\u0000\u0438\u0000\u0439\u0000\u043A\u0000\u043B\u0000\u043C\u0000\u043D\u0000\u043E\u0000\u043F\u0000\u0440\u0000\u0441\u0000\u0442\u0000\u0443\u0000\u0444\u0000\u0445\u0000\u0446\u0000\u0447\u0000\u0448\u0000\u0449\u0000\u044A\u0000\u044B\u0000\u044C\u0000\u044D\u0000\u044E\u0000\u044F\u0000\u0461\u0000\u0463\u0000\u0465\u0000\u0467\u0000\u0469\u0000\u046B\u0000\u046D\u0000\u046F\u0000\u0471\u0000\u0473\u0000\u0475\u0000\u0477\u0000\u0479\u0000\u047B\u0000\u047D\u0000\u047F\u0000\u0481\u0000\u048B\u0000\u048D\u0000\u048F\u0000\u0491\u0000\u0493\u0000\u0495\u0000\u0497\u0000\u0499\u0000\u049B\u0000\u049D\u0000\u049F\u0000\u04A1\u0000\u04A3\u0000\u04A5\u0000\u04A7\u0000\u04A9\u0000\u04AB\u0000\u04AD\u0000\u04AF\u0000\u04B1\u0000\u04B3\u0000\u04B5\u0000\u04B7\u0000\u04B9\u0000\u04BB\u0000\u04BD\u0000\u04BF\u0000\u04C2\u0000\u04C4\u0000\u04C6\u0000\u04C8\u0000\u04CA\u0000\u04CC\u0000\u04CE\u0000\u04D1\u0000\u04D3\u0000\u04D5\u0000\u04D7\u0000\u04D9\u0000\u04DB\u0000\u04DD\u0000\u04DF\u0000\u04E1\u0000\u04E3\u0000\u04E5\u0000\u04E7\u0000\u04E9\u0000\u04EB\u0000\u04ED\u0000\u04EF\u0000\u04F1\u0000\u04F3\u0000\u04F5\u0000\u04F9\u0000\u0501\u0000\u0503\u0000\u0505\u0000\u0507\u0000\u0509\u0000\u050B\u0000\u050D\u0000\u050F\u0000\u0561\u0000\u0562\u0000\u0563\u0000\u0564\u0000\u0565\u0000\u0566\u0000\u0567\u0000\u0568\u0000\u0569\u0000\u056A\u0000\u056B\u0000\u056C\u0000\u056D\u0000\u056E\u0000\u056F\u0000\u0570\u0000\u0571\u0000\u0572\u0000\u0573\u0000\u0574\u0000\u0575\u0000\u0576\u0000\u0577\u0000\u0578\u0000\u0579\u0000\u057A\u0000\u057B\u0000\u057C\u0000\u057D\u0000\u057E\u0000\u057F\u0000\u0580\u0000\u0581\u0000\u0582\u0000\u0583\u0000\u0584\u0000\u0585\u0000\u0586\u0000\u1E01\u0000\u1E03\u0000\u1E05\u0000\u1E07\u0000\u1E09\u0000\u1E0B\u0000\u1E0D\u0000\u1E0F\u0000\u1E11\u0000\u1E13\u0000\u1E15\u0000\u1E17\u0000\u1E19\u0000\u1E1B\u0000\u1E1D\u0000\u1E1F\u0000\u1E21\u0000\u1E23\u0000\u1E25\u0000\u1E27\u0000\u1E29\u0000\u1E2B\u0000\u1E2D\u0000\u1E2F\u0000\u1E31\u0000\u1E33\u0000\u1E35\u0000\u1E37\u0000\u1E39\u0000\u1E3B\u0000\u1E3D\u0000\u1E3F\u0000\u1E41\u0000\u1E43\u0000\u1E45\u0000\u1E47\u0000\u1E49\u0000\u1E4B\u0000\u1E4D\u0000\u1E4F\u0000\u1E51\u0000\u1E53\u0000\u1E55\u0000\u1E57\u0000\u1E59\u0000\u1E5B\u0000\u1E5D\u0000\u1E5F\u0000\u1E61\u0000\u1E63\u0000\u1E65\u0000\u1E67\u0000\u1E69\u0000\u1E6B\u0000\u1E6D\u0000\u1E6F\u0000\u1E71\u0000\u1E73\u0000\u1E75\u0000\u1E77\u0000\u1E79\u0000\u1E7B\u0000\u1E7D\u0000\u1E7F\u0000\u1E81\u0000\u1E83\u0000\u1E85\u0000\u1E87\u0000\u1E89\u0000\u1E8B\u0000\u1E8D\u0000\u1E8F\u0000\u1E91\u0000\u1E93\u0000\u1E95\u0000\u1E61\u0000\u1EA1\u0000\u1EA3\u0000\u1EA5\u0000\u1EA7\u0000\u1EA9\u0000\u1EAB\u0000\u1EAD\u0000\u1EAF\u0000\u1EB1\u0000\u1EB3\u0000\u1EB5\u0000\u1EB7\u0000\u1EB9\u0000\u1EBB\u0000\u1EBD\u0000\u1EBF\u0000\u1EC1\u0000\u1EC3\u0000\u1EC5\u0000\u1EC7\u0000\u1EC9\u0000\u1ECB\u0000\u1ECD\u0000\u1ECF\u0000\u1ED1\u0000\u1ED3\u0000\u1ED5\u0000\u1ED7\u0000\u1ED9\u0000\u1EDB\u0000\u1EDD\u0000\u1EDF\u0000\u1EE1\u0000\u1EE3\u0000\u1EE5\u0000\u1EE7\u0000\u1EE9\u0000\u1EEB\u0000\u1EED\u0000\u1EEF\u0000\u1EF1\u0000\u1EF3\u0000\u1EF5\u0000\u1EF7\u0000\u1EF9\u0000\u1F00\u0000\u1F01\u0000\u1F02\u0000\u1F03\u0000\u1F04\u0000\u1F05\u0000\u1F06\u0000\u1F07\u0000\u1F10\u0000\u1F11\u0000\u1F12\u0000\u1F13\u0000\u1F14\u0000\u1F15\u0000\u1F20\u0000\u1F21\u0000\u1F22\u0000\u1F23\u0000\u1F24\u0000\u1F25\u0000\u1F26\u0000\u1F27\u0000\u1F30\u0000\u1F31\u0000\u1F32\u0000\u1F33\u0000\u1F34\u0000\u1F35\u0000\u1F36\u0000\u1F37\u0000\u1F40\u0000\u1F41\u0000\u1F42\u0000\u1F43\u0000\u1F44\u0000\u1F45\u0000\u1F51\u0000\u1F53\u0000\u1F55\u0000\u1F57\u0000\u1F60\u0000\u1F61\u0000\u1F62\u0000\u1F63\u0000\u1F64\u0000\u1F65\u0000\u1F66\u0000\u1F67\u0000\u1F80\u0000\u1F81\u0000\u1F82\u0000\u1F83\u0000\u1F84\u0000\u1F85\u0000\u1F86\u0000\u1F87\u0000\u1F90\u0000\u1F91\u0000\u1F92\u0000\u1F93\u0000\u1F94\u0000\u1F95\u0000\u1F96\u0000\u1F97\u0000\u1FA0\u0000\u1FA1\u0000\u1FA2\u0000\u1FA3\u0000\u1FA4\u0000\u1FA5\u0000\u1FA6\u0000\u1FA7\u0000\u1FB0\u0000\u1FB1\u0000\u1F70\u0000\u1F71\u0000\u1FB3\u0000\u03B9\u0000\u1F72\u0000\u1F73\u0000\u1F74\u0000\u1F75\u0000\u1FC3\u0000\u1FD0\u0000\u1FD1\u0000\u1F76\u0000\u1F77\u0000\u1FE0\u0000\u1FE1\u0000\u1F7A\u0000\u1F7B\u0000\u1FE5\u0000\u1F78\u0000\u1F79\u0000\u1F7C\u0000\u1F7D\u0000\u1FF3\u0000\u03C9\u0000\u006B\u0000\u00E5\u0000\u2170\u0000\u2171\u0000\u2172\u0000\u2173\u0000\u2174\u0000\u2175\u0000\u2176\u0000\u2177\u0000\u2178\u0000\u2179\u0000\u217A\u0000\u217B\u0000\u217C\u0000\u217D\u0000\u217E\u0000\u217F\u0000\u24D0\u0000\u24D1\u0000\u24D2\u0000\u24D3\u0000\u24D4\u0000\u24D5\u0000\u24D6\u0000\u24D7\u0000\u24D8\u0000\u24D9\u0000\u24DA\u0000\u24DB\u0000\u24DC\u0000\u24DD\u0000\u24DE\u0000\u24DF\u0000\u24E0\u0000\u24E1\u0000\u24E2\u0000\u24E3\u0000\u24E4\u0000\u24E5\u0000\u24E6\u0000\u24E7\u0000\u24E8\u0000\u24E9\u0000\uFF41\u0000\uFF42\u0000\uFF43\u0000\uFF44\u0000\uFF45\u0000\uFF46\u0000\uFF47\u0000\uFF48\u0000\uFF49\u0000\uFF4A\u0000\uFF4B\u0000\uFF4C\u0000\uFF4D\u0000\uFF4E\u0000\uFF4F\u0000\uFF50\u0000\uFF51\u0000\uFF52\u0000\uFF53\u0000\uFF54\u0000\uFF55\u0000\uFF56\u0000\uFF57\u0000\uFF58\u0000\uFF59\u0000\uFF5A";
    private final static String UPPER = "\u0000\u0049\u0000\u00B5\u0000\u0100\u0000\u0102\u0000\u0104\u0000\u0106\u0000\u0108\u0000\u010A\u0000\u010C\u0000\u010E\u0000\u0110\u0000\u0112\u0000\u0114\u0000\u0116\u0000\u0118\u0000\u011A\u0000\u011C\u0000\u011E\u0000\u0120\u0000\u0122\u0000\u0124\u0000\u0126\u0000\u0128\u0000\u012A\u0000\u012C\u0000\u012E\u0000\u0130\u0000\u0132\u0000\u0134\u0000\u0136\u0000\u0139\u0000\u013B\u0000\u013D\u0000\u013F\u0000\u0141\u0000\u0143\u0000\u0145\u0000\u0147\u0000\u014A\u0000\u014C\u0000\u014E\u0000\u0150\u0000\u0152\u0000\u0154\u0000\u0156\u0000\u0158\u0000\u015A\u0000\u015C\u0000\u015E\u0000\u0160\u0000\u0162\u0000\u0164\u0000\u0166\u0000\u0168\u0000\u016A\u0000\u016C\u0000\u016E\u0000\u0170\u0000\u0172\u0000\u0174\u0000\u0176\u0000\u0178\u0000\u0179\u0000\u017B\u0000\u017D\u0000\u017F\u0000\u0181\u0000\u0182\u0000\u0184\u0000\u0186\u0000\u0187\u0000\u0189\u0000\u018A\u0000\u018B\u0000\u018E\u0000\u018F\u0000\u0190\u0000\u0191\u0000\u0193\u0000\u0194\u0000\u0196\u0000\u0197\u0000\u0198\u0000\u019C\u0000\u019D\u0000\u019F\u0000\u01A0\u0000\u01A2\u0000\u01A4\u0000\u01A6\u0000\u01A7\u0000\u01A9\u0000\u01AC\u0000\u01AE\u0000\u01AF\u0000\u01B1\u0000\u01B2\u0000\u01B3\u0000\u01B5\u0000\u01B7\u0000\u01B8\u0000\u01BC\u0000\u01C4\u0000\u01C5\u0000\u01C7\u0000\u01C8\u0000\u01CA\u0000\u01CB\u0000\u01CD\u0000\u01CF\u0000\u01D1\u0000\u01D3\u0000\u01D5\u0000\u01D7\u0000\u01D9\u0000\u01DB\u0000\u01DE\u0000\u01E0\u0000\u01E2\u0000\u01E4\u0000\u01E6\u0000\u01E8\u0000\u01EA\u0000\u01EC\u0000\u01EE\u0000\u01F1\u0000\u01F2\u0000\u01F4\u0000\u01F6\u0000\u01F7\u0000\u01F8\u0000\u01FA\u0000\u01FC\u0000\u01FE\u0000\u0200\u0000\u0202\u0000\u0204\u0000\u0206\u0000\u0208\u0000\u020A\u0000\u020C\u0000\u020E\u0000\u0210\u0000\u0212\u0000\u0214\u0000\u0216\u0000\u0218\u0000\u021A\u0000\u021C\u0000\u021E\u0000\u0220\u0000\u0222\u0000\u0224\u0000\u0226\u0000\u0228\u0000\u022A\u0000\u022C\u0000\u022E\u0000\u0230\u0000\u0232\u0000\u0345\u0000\u0386\u0000\u0388\u0000\u0389\u0000\u038A\u0000\u038C\u0000\u038E\u0000\u038F\u0000\u0391\u0000\u0392\u0000\u0393\u0000\u0394\u0000\u0395\u0000\u0396\u0000\u0397\u0000\u0398\u0000\u0399\u0000\u039A\u0000\u039B\u0000\u039C\u0000\u039D\u0000\u039E\u0000\u039F\u0000\u03A0\u0000\u03A1\u0000\u03A3\u0000\u03A4\u0000\u03A5\u0000\u03A6\u0000\u03A7\u0000\u03A8\u0000\u03A9\u0000\u03AA\u0000\u03AB\u0000\u03C2\u0000\u03D0\u0000\u03D1\u0000\u03D5\u0000\u03D6\u0000\u03D8\u0000\u03DA\u0000\u03DC\u0000\u03DE\u0000\u03E0\u0000\u03E2\u0000\u03E4\u0000\u03E6\u0000\u03E8\u0000\u03EA\u0000\u03EC\u0000\u03EE\u0000\u03F0\u0000\u03F1\u0000\u03F4\u0000\u03F5\u0000\u03F7\u0000\u03F9\u0000\u03FA\u0000\u0400\u0000\u0401\u0000\u0402\u0000\u0403\u0000\u0404\u0000\u0405\u0000\u0406\u0000\u0407\u0000\u0408\u0000\u0409\u0000\u040A\u0000\u040B\u0000\u040C\u0000\u040D\u0000\u040E\u0000\u040F\u0000\u0410\u0000\u0411\u0000\u0412\u0000\u0413\u0000\u0414\u0000\u0415\u0000\u0416\u0000\u0417\u0000\u0418\u0000\u0419\u0000\u041A\u0000\u041B\u0000\u041C\u0000\u041D\u0000\u041E\u0000\u041F\u0000\u0420\u0000\u0421\u0000\u0422\u0000\u0423\u0000\u0424\u0000\u0425\u0000\u0426\u0000\u0427\u0000\u0428\u0000\u0429\u0000\u042A\u0000\u042B\u0000\u042C\u0000\u042D\u0000\u042E\u0000\u042F\u0000\u0460\u0000\u0462\u0000\u0464\u0000\u0466\u0000\u0468\u0000\u046A\u0000\u046C\u0000\u046E\u0000\u0470\u0000\u0472\u0000\u0474\u0000\u0476\u0000\u0478\u0000\u047A\u0000\u047C\u0000\u047E\u0000\u0480\u0000\u048A\u0000\u048C\u0000\u048E\u0000\u0490\u0000\u0492\u0000\u0494\u0000\u0496\u0000\u0498\u0000\u049A\u0000\u049C\u0000\u049E\u0000\u04A0\u0000\u04A2\u0000\u04A4\u0000\u04A6\u0000\u04A8\u0000\u04AA\u0000\u04AC\u0000\u04AE\u0000\u04B0\u0000\u04B2\u0000\u04B4\u0000\u04B6\u0000\u04B8\u0000\u04BA\u0000\u04BC\u0000\u04BE\u0000\u04C1\u0000\u04C3\u0000\u04C5\u0000\u04C7\u0000\u04C9\u0000\u04CB\u0000\u04CD\u0000\u04D0\u0000\u04D2\u0000\u04D4\u0000\u04D6\u0000\u04D8\u0000\u04DA\u0000\u04DC\u0000\u04DE\u0000\u04E0\u0000\u04E2\u0000\u04E4\u0000\u04E6\u0000\u04E8\u0000\u04EA\u0000\u04EC\u0000\u04EE\u0000\u04F0\u0000\u04F2\u0000\u04F4\u0000\u04F8\u0000\u0500\u0000\u0502\u0000\u0504\u0000\u0506\u0000\u0508\u0000\u050A\u0000\u050C\u0000\u050E\u0000\u0531\u0000\u0532\u0000\u0533\u0000\u0534\u0000\u0535\u0000\u0536\u0000\u0537\u0000\u0538\u0000\u0539\u0000\u053A\u0000\u053B\u0000\u053C\u0000\u053D\u0000\u053E\u0000\u053F\u0000\u0540\u0000\u0541\u0000\u0542\u0000\u0543\u0000\u0544\u0000\u0545\u0000\u0546\u0000\u0547\u0000\u0548\u0000\u0549\u0000\u054A\u0000\u054B\u0000\u054C\u0000\u054D\u0000\u054E\u0000\u054F\u0000\u0550\u0000\u0551\u0000\u0552\u0000\u0553\u0000\u0554\u0000\u0555\u0000\u0556\u0000\u1E00\u0000\u1E02\u0000\u1E04\u0000\u1E06\u0000\u1E08\u0000\u1E0A\u0000\u1E0C\u0000\u1E0E\u0000\u1E10\u0000\u1E12\u0000\u1E14\u0000\u1E16\u0000\u1E18\u0000\u1E1A\u0000\u1E1C\u0000\u1E1E\u0000\u1E20\u0000\u1E22\u0000\u1E24\u0000\u1E26\u0000\u1E28\u0000\u1E2A\u0000\u1E2C\u0000\u1E2E\u0000\u1E30\u0000\u1E32\u0000\u1E34\u0000\u1E36\u0000\u1E38\u0000\u1E3A\u0000\u1E3C\u0000\u1E3E\u0000\u1E40\u0000\u1E42\u0000\u1E44\u0000\u1E46\u0000\u1E48\u0000\u1E4A\u0000\u1E4C\u0000\u1E4E\u0000\u1E50\u0000\u1E52\u0000\u1E54\u0000\u1E56\u0000\u1E58\u0000\u1E5A\u0000\u1E5C\u0000\u1E5E\u0000\u1E60\u0000\u1E62\u0000\u1E64\u0000\u1E66\u0000\u1E68\u0000\u1E6A\u0000\u1E6C\u0000\u1E6E\u0000\u1E70\u0000\u1E72\u0000\u1E74\u0000\u1E76\u0000\u1E78\u0000\u1E7A\u0000\u1E7C\u0000\u1E7E\u0000\u1E80\u0000\u1E82\u0000\u1E84\u0000\u1E86\u0000\u1E88\u0000\u1E8A\u0000\u1E8C\u0000\u1E8E\u0000\u1E90\u0000\u1E92\u0000\u1E94\u0000\u1E9B\u0000\u1EA0\u0000\u1EA2\u0000\u1EA4\u0000\u1EA6\u0000\u1EA8\u0000\u1EAA\u0000\u1EAC\u0000\u1EAE\u0000\u1EB0\u0000\u1EB2\u0000\u1EB4\u0000\u1EB6\u0000\u1EB8\u0000\u1EBA\u0000\u1EBC\u0000\u1EBE\u0000\u1EC0\u0000\u1EC2\u0000\u1EC4\u0000\u1EC6\u0000\u1EC8\u0000\u1ECA\u0000\u1ECC\u0000\u1ECE\u0000\u1ED0\u0000\u1ED2\u0000\u1ED4\u0000\u1ED6\u0000\u1ED8\u0000\u1EDA\u0000\u1EDC\u0000\u1EDE\u0000\u1EE0\u0000\u1EE2\u0000\u1EE4\u0000\u1EE6\u0000\u1EE8\u0000\u1EEA\u0000\u1EEC\u0000\u1EEE\u0000\u1EF0\u0000\u1EF2\u0000\u1EF4\u0000\u1EF6\u0000\u1EF8\u0000\u1F08\u0000\u1F09\u0000\u1F0A\u0000\u1F0B\u0000\u1F0C\u0000\u1F0D\u0000\u1F0E\u0000\u1F0F\u0000\u1F18\u0000\u1F19\u0000\u1F1A\u0000\u1F1B\u0000\u1F1C\u0000\u1F1D\u0000\u1F28\u0000\u1F29\u0000\u1F2A\u0000\u1F2B\u0000\u1F2C\u0000\u1F2D\u0000\u1F2E\u0000\u1F2F\u0000\u1F38\u0000\u1F39\u0000\u1F3A\u0000\u1F3B\u0000\u1F3C\u0000\u1F3D\u0000\u1F3E\u0000\u1F3F\u0000\u1F48\u0000\u1F49\u0000\u1F4A\u0000\u1F4B\u0000\u1F4C\u0000\u1F4D\u0000\u1F59\u0000\u1F5B\u0000\u1F5D\u0000\u1F5F\u0000\u1F68\u0000\u1F69\u0000\u1F6A\u0000\u1F6B\u0000\u1F6C\u0000\u1F6D\u0000\u1F6E\u0000\u1F6F\u0000\u1F88\u0000\u1F89\u0000\u1F8A\u0000\u1F8B\u0000\u1F8C\u0000\u1F8D\u0000\u1F8E\u0000\u1F8F\u0000\u1F98\u0000\u1F99\u0000\u1F9A\u0000\u1F9B\u0000\u1F9C\u0000\u1F9D\u0000\u1F9E\u0000\u1F9F\u0000\u1FA8\u0000\u1FA9\u0000\u1FAA\u0000\u1FAB\u0000\u1FAC\u0000\u1FAD\u0000\u1FAE\u0000\u1FAF\u0000\u1FB8\u0000\u1FB9\u0000\u1FBA\u0000\u1FBB\u0000\u1FBC\u0000\u1FBE\u0000\u1FC8\u0000\u1FC9\u0000\u1FCA\u0000\u1FCB\u0000\u1FCC\u0000\u1FD8\u0000\u1FD9\u0000\u1FDA\u0000\u1FDB\u0000\u1FE8\u0000\u1FE9\u0000\u1FEA\u0000\u1FEB\u0000\u1FEC\u0000\u1FF8\u0000\u1FF9\u0000\u1FFA\u0000\u1FFB\u0000\u1FFC\u0000\u2126\u0000\u212A\u0000\u212B\u0000\u2160\u0000\u2161\u0000\u2162\u0000\u2163\u0000\u2164\u0000\u2165\u0000\u2166\u0000\u2167\u0000\u2168\u0000\u2169\u0000\u216A\u0000\u216B\u0000\u216C\u0000\u216D\u0000\u216E\u0000\u216F\u0000\u24B6\u0000\u24B7\u0000\u24B8\u0000\u24B9\u0000\u24BA\u0000\u24BB\u0000\u24BC\u0000\u24BD\u0000\u24BE\u0000\u24BF\u0000\u24C0\u0000\u24C1\u0000\u24C2\u0000\u24C3\u0000\u24C4\u0000\u24C5\u0000\u24C6\u0000\u24C7\u0000\u24C8\u0000\u24C9\u0000\u24CA\u0000\u24CB\u0000\u24CC\u0000\u24CD\u0000\u24CE\u0000\u24CF\u0000\uFF21\u0000\uFF22\u0000\uFF23\u0000\uFF24\u0000\uFF25\u0000\uFF26\u0000\uFF27\u0000\uFF28\u0000\uFF29\u0000\uFF2A\u0000\uFF2B\u0000\uFF2C\u0000\uFF2D\u0000\uFF2E\u0000\uFF2F\u0000\uFF30\u0000\uFF31\u0000\uFF32\u0000\uFF33\u0000\uFF34\u0000\uFF35\u0000\uFF36\u0000\uFF37\u0000\uFF38\u0000\uFF39\u0000\uFF3A";
    private static double[] p1 = { 1.0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10, 1e11, 1e12, 1e13, 1e14, 1e15,
        1e16, 1e17, 1e18, 1e19, 1e20, 1e21, 1e22, 1e23, 1e24, 1e25, 1e26, 1e27, 1e28, 1e29, 1e30, 1e31 };
    private static double[] p32 = { 1.0, 1e32, 1e64, 1e96, 1e128, 1e160, 1e192, 1e224, 1e256, 1e288, };
    private static double[] np32 = // guich@570_51
        { 1e-0, 1e-32, 1e-64, 1e-96, 1e-128, 1e-160, 1e-192, 1e-224, 1e-256, 1e-288, 1e-320, };
    private static final double LN10 = 2.30258509299404568402;

    private static double rounds5[] = { 5e-1, 5e-2, 5e-3, 5e-4, 5e-5, 5e-6, 5e-7, 5e-8, 5e-9, 5e-10, 5e-11, 5e-12,
        5e-13, 5e-14, 5e-15, 5e-16, 5e-17, 5e-18 };
    private static char[] zeros = "00000000000000000000".toCharArray();
  }

  static class Constants4D {
  }

  static String withAcc = "áÁâÂàÀåÅãÃäÄçÇéÉêÊèÈëËíÍîÎìÌïÏñÑóÓôÔòÒõÕöÖúÚûÛùÙüÜýÝÿÿ";
  static char[] woutAcc = "aAaAaAaAaAaAcCeEeEeEeEiIiIiIiInNoOoOoOoOoOuUuUuUuUyYyY".toCharArray();

  /** Returns the given string without accentuation characters, using the unicode range 0-255 */
  public static String removeAccentuation(String s) {
    if (s == null) {
      return null;
    }
    char[] chars = s.toCharArray();
    boolean changed = false;
    for (int i = chars.length; --i >= 0;) {
      int normal = withAcc.indexOf(chars[i]);
      if (normal != -1) {
        changed = true;
        chars[i] = woutAcc[normal];
      }
    }
    return changed ? new String(chars) : s;
  }

  @ReplacedByNativeOnDeploy
  public static boolean equals(byte[] b1, byte[] b2) {
    if (b1 != null && b2 != null && b1.length == b2.length) {
      for (int i = b1.length; --i >= 0;) {
        if (b1[i] != b2[i]) {
          return false;
        }
      }
      return true;
    }
    return b1 == b2;
  }
}
