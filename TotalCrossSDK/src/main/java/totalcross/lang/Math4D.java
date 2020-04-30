// Copyright (C) 1998 Free Software Foundation, Inc.
// Copyright (C) 2001 Ralf Kleberhoff
// Copyright (C) 2001-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

/**
<p>
   java.lang.Math
   Copyright (C) 1998 Free Software Foundation, Inc.
   Copyright (C) 2001 Ralf Kleberhoff & Guilherme Campos Hazan
<p>
Ralf Kleberhoff notes:
This file is part of GNU Classpath.
I adopted it for Jump and included pure Java implementations of the
methods previously written in C.
<p>
GNU notes:
GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
<p>
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.
<p>
You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.
<p>
As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License.
 */

package totalcross.lang;

import totalcross.sys.Convert;

/**
 * Helper class containing useful mathematical functions and constants.
 * <P>
 * The Math class used in the device is not the same one used at Java SE. Be sure to refer
 * to this documentation to see what you can use.
 * <P>
 * Note that angles are specified in radians.  Conversion functions are
 * provided for your convenience.
 * <br><br>
 * IMPORTANT: the totalcross.lang package is the java.lang that will be used in the device.
 * You CANNOT use nor import totalcross.lang package in desktop. When tc.Deploy is called,
 * all references to java.lang are replaced by totalcross.lang automatically. Given this,
 * you must use only the classes and methods that exists BOTH in java.lang and totalcross.lang.
 * For example, you can't use java.lang.ClassLoader because there are no totalcross.lang.ClassLoader.
 * Another example, you can't use java.lang.String.indexOfIgnoreCase because there are no
 * totalcross.lang.String.indexOfIgnoreCase method. Trying to use a class or method from the java.lang package
 * that has no correspondence with totalcross.lang will make the tc.Deploy program to abort, informing
 * where the problem occured. A good idea is to always refer to this javadoc to know what is and what isn't
 * available.
 *
 * @author Paul Fisher
 * @author John Keiser
 */
public final class Math4D {
  static public final double NaN = Convert.longBitsToDouble(0x7ff8000000000000L);
  static public final double POSITIVE_INFINITY = Convert.longBitsToDouble(0x7ff0000000000000L);
  static public final double NEGATIVE_INFINITY = Convert.longBitsToDouble(0xfff0000000000000L);

  /**
   * The mathematical constant <em>e</em>.
   * Used in natural log and exp.
   * @see #log(double)
   * @see #exp(double)
   */
  public static final double E = 2.7182818284590452354;

  /**
   * The mathematical constant <em>pi</em>.
   * This is the ratio of a circle's diameter to its circumference.
   */
  public static final double PI = 3.14159265358979323846;

  /* constants used in the various methods. */
  private static final double INV_PI_2 = 2.0 / PI;
  private static final double PI_2 = 0.5 * PI;
  private static final double PI_4 = 0.25 * PI;
  private static final double PI3_4 = 0.75 * PI;

  private static final double S1 = -1.66666666666666324348e-01;
  private static final double S2 = 8.33333333332248946124e-03;
  private static final double S3 = -1.98412698298579493134e-04;
  private static final double S4 = 2.75573137070700676789e-06;
  private static final double S5 = -2.50507602534068634195e-08;
  private static final double S6 = 1.58969099521155010221e-10;

  private static final double C1 = 4.16666666666666019037e-02;
  private static final double C2 = -1.38888888888741095749e-03;
  private static final double C3 = 2.48015872894767294178e-05;
  private static final double C4 = -2.75573143513906633035e-07;
  private static final double C5 = 2.08757232129817482790e-09;
  private static final double C6 = -1.13596475577881948265e-11;

  private static final double T1 = 3.33333333333334091986e-01;
  private static final double T2 = 1.33333333333201242699e-01;
  private static final double T3 = 5.39682539762260521377e-02;
  private static final double T4 = 2.18694882948595424599e-02;
  private static final double T5 = 8.86323982359930005737e-03;
  private static final double T6 = 3.59207910759131235356e-03;
  private static final double T7 = 1.45620945432529025516e-03;
  private static final double T8 = 5.88041240820264096874e-04;
  private static final double T9 = 2.46463134818469906812e-04;
  private static final double T10 = 7.81794442939557092300e-05;
  private static final double T11 = 7.14072491382608190305e-05;
  private static final double T12 = -1.85586374855275456654e-05;
  private static final double T13 = 2.59073051863633712884e-05;

  private static final double pS0 = 1.66666666666666657415e-01;
  private static final double pS1 = -3.25565818622400915405e-01;
  private static final double pS2 = 2.01212532134862925881e-01;
  private static final double pS3 = -4.00555345006794114027e-02;
  private static final double pS4 = 7.91534994289814532176e-04;
  private static final double pS5 = 3.47933107596021167570e-05;
  private static final double qS1 = -2.40339491173441421878e+00;
  private static final double qS2 = 2.02094576023350569471e+00;
  private static final double qS3 = -6.88283971605453293030e-01;
  private static final double qS4 = 7.70381505559019352791e-02;

  private static final double AT0 = 3.33333333333329318027e-01;
  private static final double AT1 = -1.99999999998764832476e-01;
  private static final double AT2 = 1.42857142725034663711e-01;
  private static final double AT3 = -1.11111104054623557880e-01;
  private static final double AT4 = 9.09088713343650656196e-02;
  private static final double AT5 = -7.69187620504482999495e-02;
  private static final double AT6 = 6.66107313738753120669e-02;
  private static final double AT7 = -5.83357013379057348645e-02;
  private static final double AT8 = 4.97687799461593236017e-02;
  private static final double AT9 = -3.65315727442169155270e-02;
  private static final double AT10 = 1.62858201153657823623e-02;

  private static final double ATAN_05 = 0.463647609000806093515;
  private static final double ATAN_10 = 0.785398163397448278999;
  private static final double ATAN_15 = 0.982793723247329054082;

  // 0.693147180369123816490 + 0.190821492927058770002e-09;
  private static final double LN2 = 0.693147180559945309417;
  private static final double INV_LN2 = 1.44269504088896338700e+00;

  private static final double P1 = 1.66666666666666019037e-01;
  private static final double P2 = -2.77777777770155933842e-03;
  private static final double P3 = 6.61375632143793436117e-05;
  private static final double P4 = -1.65339022054652515390e-06;
  private static final double P5 = 4.13813679705723846039e-08;

  private static final double Lg1 = 6.666666666666735130e-01;
  private static final double Lg2 = 3.999999999940941908e-01;
  private static final double Lg3 = 2.857142874366239149e-01;
  private static final double Lg4 = 2.222219843214978396e-01;
  private static final double Lg5 = 1.818357216161805012e-01;
  private static final double Lg6 = 1.531383769920937332e-01;
  private static final double Lg7 = 1.479819860511658591e-01;

  private static final int[] SQRT_PARMS = { 0, 1024, 3062, 5746, 9193, 13348, 18162, 23592, 29598, 36145, 43202, 50740,
      58733, 67158, 75992, 85215, 83599, 71378, 60428, 50647, 41945, 34246, 27478, 21581, 16499, 12183, 8588, 5674,
      3403, 1742, 661, 130 };

  /**
   * Take the absolute value of the argument.
   * (Absolute value means make it positive.)
   * <P>
   *
   * Note that the largest negative value (Integer.MIN_VALUE) cannot
   * be made positive.  In this case, because of the rules of negation in
   * a computer, MIN_VALUE is what will be returned.
   * This is a <em>negative</em> value.  You have been warned.
   *
   * @param a the number to take the absolute value of.
   * @return the absolute value.
   */
  public static int abs(int a) {
    return (a < 0) ? -a : a;
  }

  /**
   * Take the absolute value of the argument.
   * (Absolute value means make it positive.)
   * <P>
   *
   * Note that the largest negative value (Long.MIN_VALUE) cannot
   * be made positive.  In this case, because of the rules of negation in
   * a computer, MIN_VALUE is what will be returned.
   * This is a <em>negative</em> value.  You have been warned.
   *
   * @param a the number to take the absolute value of.
   * @return the absolute value.
   */
  public static long abs(long a) {
    return (a < 0) ? -a : a;
  }

  /**
   * Take the absolute value of the argument.
   * (Absolute value means make it positive.)
   * @param a the number to take the absolute value of.
   * @return the absolute value.
   */
  public static double abs(double a) {
    return Convert.longBitsToDouble((Convert.doubleToLongBits(a) << 1) >>> 1);
  }

  /**
   * Return whichever argument is smaller.
   * @param a the first number
   * @param b a second number
   * @return the smaller of the two numbers.
   */
  public static int min(int a, int b) {
    return (a < b) ? a : b;
  }

  /**
   * Return whichever argument is smaller.
   * @param a the first number
   * @param b a second number
   * @return the smaller of the two numbers.
   */
  public static long min(long a, long b) {
    return (a < b) ? a : b;
  }

  /**
   * Return whichever argument is smaller.
   * @param a the first number
   * @param b a second number
   * @return the smaller of the two numbers.
   */
  public static double min(double a, double b) {
    if (a == 0.0d && b == 0.0d) {
      return ((Convert.doubleToLongBits(a) >> 63) == 1) ? a : b;
    }
    return (a < b) ? a : b;
  }

  /**
   * Return whichever argument is larger.
   * @param a the first number
   * @param b a second number
   * @return the larger of the two numbers.
   */
  public static int max(int a, int b) {
    return (a > b) ? a : b;
  }

  /**
   * Return whichever argument is larger.
   * @param a the first number
   * @param b a second number
   * @return the larger of the two numbers.
   */
  public static long max(long a, long b) {
    return (a > b) ? a : b;
  }

  /**
   * Return whichever argument is larger.
   * @param a the first number
   * @param b a second number
   * @return the larger of the two numbers.
   */
  public static double max(double a, double b) {
    if (a == 0.0d && b == 0.0d) {
      return ((Convert.doubleToLongBits(a) >> 63) == 0) ? a : b;
    }
    return (a > b) ? a : b;
  }

  /**
   * The trigonometric function <em>sin</em>.
   * @param x the angle (in radians).
   * @return sin(x).
   */
  public static double sin(double x) {
    /* High word of x. */
    int ix = highPart(x) & 0x7fffffff;

    // |x| ~< pi/4
    if (ix <= 0x3fe921fb) {
      return kernelSin(x);
    } else if (ix >= 0x7ff00000) {
      return NaN;
    } else {
      double d = reducedArcValue(x);
      switch (reducedArcDirection(x)) {
      case 0:
        return kernelSin(d);
      case 1:
        return kernelCos(d);
      case 2:
        return -kernelSin(d);
      default:
        return -kernelCos(d);
      }
    }
  }

  /**
   * The trigonometric function <em>cos</em>.
   * @param x the angle (in radians).
   * @return cos(x).
   */
  public static double cos(double x) {
    /* High word of x. */
    int ix = highPart(x) & 0x7fffffff;

    // |x| ~< pi/4
    if (ix <= 0x3fe921fb) {
      return kernelCos(x);
    } else if (ix >= 0x7ff00000) {
      return NaN;
    } else {
      double d = reducedArcValue(x);
      switch (reducedArcDirection(x)) {
      case 0:
        return kernelCos(d);
      case 1:
        return -kernelSin(d);
      case 2:
        return -kernelCos(d);
      default:
        return kernelSin(d);
      }
    }
  }

  /**
   * The trigonometric function <em>tan</em>.
   * @param x the angle (in radians).
   * @return tan(x).
   */
  public static double tan(double x) {
    /* High word of x. */
    int ix = highPart(x) & 0x7fffffff;

    // |x| ~< pi/4
    if (ix <= 0x3fe921fb) {
      return kernelTan(x);
    } else if (ix >= 0x7ff00000) {
      return NaN;
      // general case: argument reduction needed
    } else {
      switch (reducedArcDirection(x)) {
      case 0:
      case 2:
        return kernelTan(reducedArcValue(x));
      default:
        return -1.0 / kernelTan(reducedArcValue(x));
      }
    }
  }

  /**
   * The trigonometric function <em>arcsin</em>.
   * The range of angles you will get are from -pi/2 to pi/2 radians (-90 to 90 degrees)
   * @param x the sin to turn back into an angle.
   * @return arcsin(x).
   */
  public static double asin(double x) {
    int hx = highPart(x);
    int lx = lowPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 1
    if (ix >= 0x3ff00000) {
      if ((ix == 0x3ff00000) && (lx == 0)) {
        if (hx == 0x3ff00000) {
          return PI_2;
        } else {
          return -PI_2;
        }
      } else {
        return NaN;
      }
    }
    // |x| < 2e-27
    else if (ix < 0x3e400000) {
      return x;
    } else if (ix <= 0x3fe00000) {
      double x2 = x * x;
      double p = x2 * (pS0 + x2 * (pS1 + x2 * (pS2 + x2 * (pS3 + x2 * (pS4 + x2 * pS5)))));
      double q = 1.0 + x2 * (qS1 + x2 * (qS2 + x2 * (qS3 + x2 * qS4)));
      return x + x * (p / q);
    }
    // 0.5 < x < 1.0
    else if (hx > 0) {
      return (PI_2 - 2.0 * asin(sqrt(0.5 * (1.0 - x))));
      // -1.0 < x < -0.5
    } else {
      return (2.0 * asin(sqrt(0.5 * (1.0 + x))) - PI_2);
    }
  }

  /**
   * The trigonometric function <em>arccos</em>.
   * The range of angles you will get are from 0 to pi radians (0 to 180 degrees).
   * @param x the cos to turn back into an angle.
   * @return arccos(x).
   */
  public static double acos(double x) {
    int hx = highPart(x);
    int lx = lowPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 1
    if (ix >= 0x3ff00000) {
      if ((ix == 0x3ff00000) && (lx == 0)) {
        if (hx == 0x3ff00000) {
          return 0.0;
        } else {
          return PI;
        }
      } else {
        return NaN;
      }
    }
    // |x| < 2e-27
    else if (ix < 0x3e400000) {
      return PI_2 - x;
    } else if (ix <= 0x3fe00000) {
      double x2 = x * x;
      double p = x2 * (pS0 + x2 * (pS1 + x2 * (pS2 + x2 * (pS3 + x2 * (pS4 + x2 * pS5)))));
      double q = 1.0 + x2 * (qS1 + x2 * (qS2 + x2 * (qS3 + x2 * qS4)));
      return PI_2 - x - x * (p / q);
    }
    // 0.5 < x < 1.0
    else if (hx > 0) {
      return 2.0 * asin(sqrt(0.5 * (1.0 - x)));
    } else {
      return PI - 2.0 * asin(sqrt(0.5 * (1.0 + x)));
    }
  }

  /**
   * The trigonometric function <em>arctan</em>.
   * The range of angles you will get are from -pi/2 to pi/2 radians (-90 to 90 degrees)
   * @param x the tan to turn back into an angle.
   * @return arctan(x).
   * @see #atan2(double,double)
   */
  public static double atan(double x) {
    int hx = highPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 2**66
    if (ix >= 0x44100000) {
      if (hx > 0) {
        return PI_2;
      } else {
        return -PI_2;
      }
    }
    // |x| <= 7/16 (=0.4375)
    else if (ix <= 0x3fdc0000) {
      return kernelAtan(x);
    } else if (hx > 0) {
      // x <= 11/16
      if (ix <= 0x3fe60000) {
        return ATAN_05 + kernelAtan((2.0 * x - 1.0) / (2.0 + x));
      } else if (ix <= 0x3ff30000) {
        return ATAN_10 + kernelAtan((x - 1.0) / (1.0 + x));
      } else if (ix <= 0x40038000) {
        return ATAN_15 + kernelAtan((2.0 * x - 3.0) / (2.0 + 3.0 * x));
        // x > 39/16
      } else {
        return PI_2 - kernelAtan(1.0 / x);
      }
    } else {
      // x >= -11/16
      if (ix <= 0x3fe60000) {
        return -ATAN_05 + kernelAtan((2.0 * x + 1.0) / (2.0 - x));
      } else if (ix <= 0x3ff30000) {
        return -ATAN_10 + kernelAtan((x + 1.0) / (1.0 - x));
      } else if (ix <= 0x40038000) {
        return -ATAN_15 + kernelAtan((2.0 * x + 3.0) / (2.0 - 3.0 * x));
        // x < -39/16
      } else {
        return -PI_2 - kernelAtan(1.0 / x);
      }
    }
  }

  /**
   * A special version of the trigonometric function <em>arctan</em>.
   * Given a position (x,y), this function will give you the angle of
   * that position.
   * The range of angles you will get are from -pi to pi radians (-180 to 180 degrees),
   * the whole spectrum of angles.  That is what makes this function so
   * much more useful than the other <code>atan()</code>.
   * @param y the y position
   * @param x the x position
   * @return arctan(y/x).
   * @see #atan(double)
   */
  public static double atan2(double y, double x) {
    int hx = highPart(x);
    int ix = hx & 0x7fffffff;
    int hy = highPart(y);
    int iy = hy & 0x7fffffff;

    // cases with infinity or NaN
    if ((ix >= 0x7ff00000) || (iy >= 0x7ff00000)) {
      switch (hx) {
      case 0x7ff00000:
        switch (hy) {
        case 0x7ff00000:
          return PI_4;
        case 0xfff00000:
          return -PI_4;
        case 0x7ff80000:
          return NaN;
        default:
          return 0.0;
        }
      case 0xfff00000:
        switch (hy) {
        case 0x7ff00000:
          return PI3_4;
        case 0xfff00000:
          return -PI3_4;
        case 0x7ff80000:
          return NaN;
        default:
          return (hy < 0 ? -PI : PI);
        }
      case 0x7ff80000:
        return NaN;
      default:
        switch (hy) {
        case 0x7ff00000:
          return PI_2;
        case 0xfff00000:
          return -PI_2;
        default:
          return NaN;
        }
      }
    }
    if (y == 0) {
      return PI;
    } else if (hy > 0) {
      return PI_2 - atan(x / y);
    } else if (hy == 0) {
      return (hx >= 0 ? 0.0 : PI);
    } else {
      return -PI_2 - atan(x / y);
    }
  }

  /**
   * Take <em>e</em><sup>x</sup>.  The opposite of <code>log()</code>.
   * @param x the number to raise to the power.
   * @return the number raised to the power of <em>e</em>.
   * @see #log(double)
   * @see #pow(double,double)
   */
  public static double exp(double x) {
    int hx = highPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 709.78, infinite, NaN: non-finite result
    if (ix >= 0x40862e42) {
      if (hx == 0x7ff80000) {
        return NaN;
      } else if (hx < 0) {
        return 0.0;
      } else {
        return POSITIVE_INFINITY;
      }
    }

    int k = (int) round(x * INV_LN2);
    double rx = x - k * LN2;
    double t = rx * rx;
    double c = rx - t * (P1 + t * (P2 + t * (P3 + t * (P4 + t * P5))));
    double y = 1.0 - ((rx * c) / (c - 2.0) - rx);
    if (k == 0) {
      return y;
    } else {
      long yl = Convert.doubleToLongBits(y) + (((long) k) << 52);
      // overflow/underflow result in yl < 0,
      // when all exponent bits = 0: "denormalized numbers"
      if (yl < 0x0010000000000000L) {
        return (k > 0 ? POSITIVE_INFINITY : 0.0);
      } else {
        return Convert.longBitsToDouble(yl);
      }
    }
  }

  /**
   * Take ln(x) (the natural log).  The opposite of <code>exp()</code>.
   * Note that the way to get log<sub>b</sub>(a) is to do this:
   * <code>ln(a) / ln(b)</code>.
   * @param x the number to take the natural log of.
   * @return the natural log of <code>x</code>.
   * @see #exp(double)
   */
  public static double log(double x) {
    int hx = highPart(x);

    if (hx < 0x00100000) {
      if (hx < 0) {
        return NaN;
      } else {
        return NEGATIVE_INFINITY;
      }
    } else if (hx >= 0x7ff00000) {
      if (hx == 0x7ff00000) {
        return POSITIVE_INFINITY;
      } else {
        return NaN;
      }
    }

    int k;
    if ((hx & 0x000fffff) <= 0x6a09c) {
      k = (hx >> 20) - 1023;
    } else {
      k = (hx >> 20) - 1022;
    }

    double f = (Convert.longBitsToDouble(Convert.doubleToLongBits(x) - (((long) k) << 52)) - 1.0);
    double s = f / (2.0 + f);
    double z = s * s;
    double w = z * z;
    double r = w * (Lg2 + w * (Lg4 + w * Lg6)) + z * (Lg1 + w * (Lg3 + w * (Lg5 + w * Lg7)));
    return k * LN2 + f - s * (f - r);
  }

  /**
   * Take a square root.
   * For other roots, to pow(x,1/rootNumber).
   * @param x the numeric argument
   * @return the square root of the argument.
   * @see #pow(double,double)
   */
  public static double sqrt(double x) {
    int hx = highPart(x);

    if (hx < 0) {
      return NaN;
    } else if (x == 0.0) {
      return 0.0;
    } else if ((hx & 0x7ff00000) == 0x7ff00000) {
      return x;
    }

    int k = (hx >> 1) + 0x1ff80000;
    double y = Convert.longBitsToDouble(((long) (k - SQRT_PARMS[(k >> 15) & 31])) << 32);

    y = (y + x / y) / 2.0;
    y = (y + x / y) / 2.0;

    return y - (y - x / y) / 2.0;
  }

  /**
   * Take a number to a power.
   * @param x the number to raise.
   * @param y the power to raise it to.
   * @return x<sup>y</sup>.
   */
  public static double pow(double x, double y) {
    if (isNaN(x) || isNaN(y)) {
      return NaN;
    }

    // added to improve accuracy for integer exponents
    // P M Dickerson, 28May2003
    int iy = (int) y;
    if (iy >= 0 && iy == y && iy <= 1024) {
      double result = 1.0;
      while (iy != 0) {
        if ((iy & 1) != 0) {
          result = result * x;
        }
        x = x * x;
        iy >>>= 1;
      }
      return result;
    }

    if (x < 0.0) {
      long lly = Convert.doubleToLongBits(y);
      int expY = ((int) ((lly & 0x7ff0000000000000L) >> 52)) - 0x3ff;

      // 0=non-int, 1=odd, 2=even
      int yIntType;
      if (y == 0.0) {
        yIntType = 2;
      } else if (expY < 0) {
        yIntType = 0;
      } else if (expY <= 54) {
        long temp = ((((lly & 0x000fffffffffffffL) + 0x0010000000000000L) << expY) & 0x001fffffffffffffL);
        if (temp == 0L) {
          yIntType = 2;
        } else if (temp == 0x0010000000000000L) {
          yIntType = 1;
        } else {
          yIntType = 0;
        }
      } else {
        yIntType = 2;
      }

      if (yIntType == 0) {
        return NaN;
      } else if (yIntType == 1) {
        return -exp(y * log(-x));
      } else {
        return exp(y * log(-x));
      }
    } else {
      return exp(y * log(x));
    }
  }

  /**
   * Get the floating point remainder on two numbers,
   * which really does the following:
   * <P>
   *
   * <OL>
   *   <LI>
   *       Takes x/y and finds the nearest integer <em>n</em> to the
   *       quotient.  (Uses the <code>rint()</code> function to do this.
   *   </LI>
   *   <LI>
   *       Takes x - y*<em>n</em>.
   *   </LI>
   *   <LI>
   *       If x = y*n, then the result is 0 if x is positive and -0 if x
   *       is negative.
   *   </LI>
   * </OL>
   *
   * @param x the dividend (the top half)
   * @param y the divisor (the bottom half)
   * @return the IEEE 754-defined floating point remainder of x/y.
   * @see #rint(double)
   */
  public static double IEEEremainder(double x, double y) {
    return x - y * rint(x / y);
  }

  /**
   * Take the nearest integer that is that is greater than or equal to the
   * argument.
   * @param x the value to act upon.
   * @return the nearest integer >= <code>x</code>.
   */
  public static double ceil(double x) {
    if (x == 0.0) {
      return x;
    }

    long lx = Convert.doubleToLongBits(x);
    if ((lx & 0x7ff0000000000000L) == 0x7ff0000000000000L) {
      return x;
    }

    int exp = (((int) (lx >>> 52)) & 0x7ff) - 1075;
    if (exp >= 0) {
      return x;
    } else if (lx > 0L) {
      if (exp < -52) {
        return 1.0;
      } else {
        long ltrunc = lx & (0xffffffffffffffffL << (-exp));
        if (ltrunc == lx) {
          return x;
        } else {
          return Convert.longBitsToDouble(ltrunc) + 1.0;
        }
      }
    } else {
      if (exp < -52) {
        return 0.0;
      } else {
        lx = lx & (0xffffffffffffffffL << (-exp));
        return Convert.longBitsToDouble(lx);
      }
    }
  }

  /**
   * Take the nearest integer that is that is less than or equal to the
   * argument.
   * @param x the value to act upon.
   * @return the nearest integer <= <code>x</code>.
   */
  public static double floor(double x) {
    if (x == 0.0) {
      return x;
    }

    long lx = Convert.doubleToLongBits(x);
    if ((lx & 0x7ff0000000000000L) == 0x7ff0000000000000L) {
      return x;
    }

    int exp = (((int) (lx >>> 52)) & 0x7ff) - 1075;
    if (exp >= 0) {
      return x;
    } else if (lx > 0L) {
      if (exp < -52) {
        return 0.0;
      } else {
        return Convert.longBitsToDouble(lx & (0xffffffffffffffffL << (-exp)));
      }
    } else {
      if (exp < -52) {
        return -1.0;
      } else {
        long ltrunc = lx & (0xffffffffffffffffL << (-exp));
        if (ltrunc == lx) {
          return x;
        } else {
          return Convert.longBitsToDouble(ltrunc) - 1.0;
        }
      }
    }
  }

  /**
   * Take the nearest integer to the argument.  If it is exactly between
   * two integers, the even integer is taken.
   * @param x the value to act upon.
   * @return the nearest integer to <code>x</code>.
   */
  public static double rint(double x) {
    if (x == 0.0) {
      return x;
    }

    long lx = Convert.doubleToLongBits(x);
    if ((lx & 0x7ff0000000000000L) == 0x7ff0000000000000L) {
      return x;
    }

    int exp = (((int) (lx >>> 52)) & 0x7ff) - 1023;
    if (exp >= 52) {
      return x;
    } else if (exp <= -2) {
      return 0.0;
    } else if (exp == -1) {
      if ((lx & 0x000fffffffffffffL) == 0L) {
        return 0.0;
      } else {
        return (x > 0.0 ? 1.0 : -1.0);
      }
    } else {
      double adder = 0.0;
      long fractMask = 0x000fffffffffffffL >> exp;
      long halfMask = 0x0008000000000000L >> exp;
      long lfract = lx & fractMask;
      if (lfract > halfMask) {
        adder = (x > 0.0 ? 1.0 : -1.0);
      } else if (lfract == halfMask) {
        if ((lx & (halfMask << 1)) != 0L) {
          adder = (x > 0.0 ? 1.0 : -1.0);
        }
      }
      return (Convert.longBitsToDouble(lx & ~fractMask) + adder);
    }
  }

  /**
   * Take the nearest integer to the argument.  If it is exactly between
   * two integers, then the lower of the two (-10 lower than -9) is taken.
   * If the argument is less than Long.MIN_VALUE or negative infinity,
   * Long.MIN_VALUE will be returned.  If the argument is greater than
   * Long.MAX_VALUE, Long.MAX_VALUE will be returned.
   *
   * @param a the argument to round.
   * @return the nearest integer to the argument.
   */
  public static long round(double a) {
    return (long) floor(a + 0.5d);
  }

  /**
   * Convert from degrees to radians.
   * The formula for this is radians = degrees * (pi/180).
   * @param degrees an angle in degrees
   * @return the angle in radians
   */
  public static double toRadians(double degrees) {
    return degrees * 0.017453292519943295; /* (degrees * (PI/180)) */
  }

  /**
   * Convert from radians to degrees.
   * The formula for this is degrees = radians * (180/pi).
   * @param rads an angle in radians
   * @return the angle in degrees
   */
  public static double toDegrees(double rads) {
    return rads / 0.017453292519943295; /* (rads / (PI/180)) */
  }

  /**
   * compute sin(x) for x in [-pi/4 ... pi/4].
   */
  private static double kernelSin(double x) {
    // abs(x) < 2**-27:  sin(x) = x
    if ((highPart(x) & 0x7fffffff) < 0x3e400000) {
      return x;
    }
    double x2 = x * x;
    return x * (1.0 + x2 * (S1 + x2 * (S2 + x2 * (S3 + x2 * (S4 + x2 * (S5 + x2 * S6))))));
  }

  /**
   * compute cos(x) for x in [-pi/4 ... pi/4].
   */
  private static double kernelCos(double x) {
    if ((highPart(x) & 0x7fffffff) < 0x3e400000) {
      return 1.0;
    }

    double x2 = x * x;
    return 1.0 + x2 * (-0.5 + x2 * (C1 + x2 * (C2 + x2 * (C3 + x2 * (C4 + x2 * (C5 + x2 * C6))))));
  }

  /**
   * compute tan(x) for x in [-pi/4 ... pi/4].
   */
  private static double kernelTan(double x) {
    int hx = highPart(x);
    int ix = hx & 0x7fffffff;
    if (ix < 0x3e400000) {
      return x;
    }
    // |x|>=0.6744
    if (ix >= 0x3fe59428) {
      if (hx >= 0) {
        double t = kernelTan(PI_4 - x);
        return (1.0 - t) / (1.0 + t);
      } else {
        double t = kernelTan(PI_4 + x);
        return (t - 1.0) / (1.0 + t);
      }
    }
    double x2 = x * x;
    double x4 = x2 * x2;
    return (x + x * x2 * (T1 + x4 * (T3 + x4 * (T5 + x4 * (T7 + x4 * (T9 + x4 * (T11 + x4 * T13))))))
        + x * x4 * (T2 + x4 * (T4 + x4 * (T6 + x4 * (T8 + x4 * (T10 + x4 * T12))))));
  }

  /**
   * arc tangent for x in [-7/16 ... 7/16].
   */
  private static double kernelAtan(double x) {
    int ix = highPart(x) & 0x7fffffff;
    if (ix < 0x3e200000) {
      return x;
    } else {
      double z = x * x;
      double w = z * z;
      double s1 = z * (AT0 + w * (AT2 + w * (AT4 + w * (AT6 + w * (AT8 + w * AT10)))));
      double s2 = w * (AT1 + w * (AT3 + w * (AT5 + w * (AT7 + w * AT9))));
      return x - x * (s1 + s2);
    }
  }

  private static int highPart(double x) {
    return (int) (Convert.doubleToLongBits(x) >> 32);
  }

  private static int lowPart(double x) {
    return (int) Convert.doubleToLongBits(x);
  }

  public static boolean isNaN(double d) {
    return Convert.doubleToLongBits(d) == 0xfff0000000000000L;
  }

  /**
   * An arc reduced into the range [-PI/4 ... PI/4].
   * The arc is equivalent to:
   * direction * PI/2 + value
   */
  private static int reducedArcDirection(double x) {
    long n = round(INV_PI_2 * x);
    return ((int) n) & 0x0003;
  }

  /**
   * An arc reduced into the range [-PI/4 ... PI/4].
   * The arc is equivalent to:
   * direction * PI/2 + value
   */
  private static double reducedArcValue(double x) {
    long n = round(INV_PI_2 * x);
    return x - n * PI_2;
  }
}
