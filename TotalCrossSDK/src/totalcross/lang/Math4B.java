/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 1998 Free Software Foundation, Inc.                            *
 *  Copyright (C) 2001 Ralf Kleberhoff                                           *
 *  Copyright (C) 2001-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

// $Id: Math4B.java,v 1.9 2011-01-04 13:18:58 guich Exp $

package totalcross.lang;

import net.rim.device.api.util.MathUtilities;
import totalcross.sys.Convert;

public final class Math4B
{
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
  private static final double PI_2     = 0.5 * PI;
  private static final double PI_4     = 0.25 * PI;
  private static final double PI3_4    = 0.75 * PI;

  private static final double pS0 =  1.66666666666666657415e-01;
  private static final double pS1 = -3.25565818622400915405e-01;
  private static final double pS2 =  2.01212532134862925881e-01;
  private static final double pS3 = -4.00555345006794114027e-02;
  private static final double pS4 =  7.91534994289814532176e-04;
  private static final double pS5 =  3.47933107596021167570e-05;
  private static final double qS1 = -2.40339491173441421878e+00;
  private static final double qS2 =  2.02094576023350569471e+00;
  private static final double qS3 = -6.88283971605453293030e-01;
  private static final double qS4 =  7.70381505559019352791e-02;

  private static final double AT0 =  3.33333333333329318027e-01;
  private static final double AT1 = -1.99999999998764832476e-01;
  private static final double AT2 =  1.42857142725034663711e-01;
  private static final double AT3 = -1.11111104054623557880e-01;
  private static final double AT4 =  9.09088713343650656196e-02;
  private static final double AT5 = -7.69187620504482999495e-02;
  private static final double AT6 =  6.66107313738753120669e-02;
  private static final double AT7 = -5.83357013379057348645e-02;
  private static final double AT8 =  4.97687799461593236017e-02;
  private static final double AT9 = -3.65315727442169155270e-02;
  private static final double AT10 =  1.62858201153657823623e-02;

  private static final double ATAN_05 = 0.463647609000806093515;
  private static final double ATAN_10 = 0.785398163397448278999;
  private static final double ATAN_15 = 0.982793723247329054082;

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
  public static int abs(int a)
  {
     return java.lang.Math.abs(a);
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
  public static long abs(long a)
  {
    return java.lang.Math.abs(a);
  }

  /**
   * Take the absolute value of the argument.
   * (Absolute value means make it positive.)
   * @param a the number to take the absolute value of.
   * @return the absolute value.
   */
  public static double abs(double a)
  {
    return java.lang.Math.abs(a);
  }

  /**
   * Return whichever argument is smaller.
   * @param a the first number
   * @param b a second number
   * @return the smaller of the two numbers.
   */
  public static int min(int a, int b)
  {
    return java.lang.Math.min(a, b);
  }

  /**
   * Return whichever argument is smaller.
   * @param a the first number
   * @param b a second number
   * @return the smaller of the two numbers.
   */
  public static long min(long a, long b)
  {
    return java.lang.Math.min(a, b);
  }

  /**
   * Return whichever argument is smaller.
   * @param a the first number
   * @param b a second number
   * @return the smaller of the two numbers.
   */
  public static double min(double a, double b)
  {
    return java.lang.Math.min(a, b);
  }

  /**
   * Return whichever argument is larger.
   * @param a the first number
   * @param b a second number
   * @return the larger of the two numbers.
   */
  public static int max(int a, int b)
  {
    return java.lang.Math.max(a, b);
  }

  /**
   * Return whichever argument is larger.
   * @param a the first number
   * @param b a second number
   * @return the larger of the two numbers.
   */
  public static long max(long a, long b)
  {
    return java.lang.Math.max(a, b);
  }

  /**
   * Return whichever argument is larger.
   * @param a the first number
   * @param b a second number
   * @return the larger of the two numbers.
   */
  public static double max(double a, double b)
  {
    return java.lang.Math.max(a, b);
  }

  /**
   * The trigonometric function <em>sin</em>.
   * @param x the angle (in radians).
   * @return sin(x).
   */
  public static double sin(double x)
  {
    return java.lang.Math.sin(x);
  }

  /**
   * The trigonometric function <em>cos</em>.
   * @param x the angle (in radians).
   * @return cos(x).
   */
  public static double cos(double x)
  {
    return java.lang.Math.cos(x);
  }

  /**
   * The trigonometric function <em>tan</em>.
   * @param x the angle (in radians).
   * @return tan(x).
   */
  public static double tan(double x)
  {
    return java.lang.Math.tan(x);
  }

  /**
   * The trigonometric function <em>arcsin</em>.
   * The range of angles you will get are from -pi/2 to pi/2 radians (-90 to 90 degrees)
   * @param x the sin to turn back into an angle.
   * @return arcsin(x).
   */
  public static double asin(double x)
  {
    int hx = highPart(x);
    int lx = lowPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 1
    if (ix >= 0x3ff00000)
    {
       if ((ix == 0x3ff00000) && (lx == 0))
       {
          if (hx == 0x3ff00000)
             return PI_2;
          else
             return -PI_2;
       }
       else
          return NaN;
    }
    // |x| < 2e-27
    else if (ix < 0x3e400000)
      return x;

    // |x| <= 0.5
    else if (ix <= 0x3fe00000)
    {
      double x2 = x*x;
      double p = x2*(pS0 + x2*(pS1 + x2*(pS2 + x2*(pS3 + x2*(pS4 + x2*pS5)))));
      double q = 1.0 + x2*(qS1 + x2*(qS2 + x2*(qS3 + x2*qS4)));
      return x + x*(p/q);
    }
    // 0.5 < x < 1.0
    else if (hx > 0)
      return (PI_2 - 2.0 * asin (sqrt (0.5 * (1.0 - x))));
    // -1.0 < x < -0.5
    else
      return (2.0 * asin (sqrt (0.5 * (1.0 + x))) - PI_2);
  }

  /**
   * The trigonometric function <em>arccos</em>.
   * The range of angles you will get are from 0 to pi radians (0 to 180 degrees).
   * @param x the cos to turn back into an angle.
   * @return arccos(x).
   */
  public static double acos(double x)
  {
    int hx = highPart(x);
    int lx = lowPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 1
    if (ix >= 0x3ff00000)
    {
       if ((ix == 0x3ff00000) && (lx == 0))
       {
          if (hx == 0x3ff00000)
             return 0.0;
          else
             return PI;
       }
       else
          return NaN;
    }
    // |x| < 2e-27
    else if (ix < 0x3e400000)
      return PI_2 - x;

    // |x| <= 0.5
    else if (ix <= 0x3fe00000)
    {
      double x2 = x*x;
      double p = x2*(pS0 + x2*(pS1 + x2*(pS2 + x2*(pS3 + x2*(pS4 + x2*pS5)))));
      double q = 1.0 + x2*(qS1 + x2*(qS2 + x2*(qS3 + x2*qS4)));
      return PI_2 - x - x*(p/q);
    }
    // 0.5 < x < 1.0
    else if (hx > 0)
      return 2.0 * asin (sqrt (0.5 * (1.0 - x)));

    // -1.0 < x < -0.5
    else
      return PI - 2.0 * asin (sqrt (0.5 * (1.0 + x)));
  }

  /**
   * The trigonometric function <em>arctan</em>.
   * The range of angles you will get are from -pi/2 to pi/2 radians (-90 to 90 degrees)
   * @param x the tan to turn back into an angle.
   * @return arctan(x).
   * @see #atan2(double,double)
   */
  public static double atan(double x)
  {
    int hx = highPart(x);
    int ix = hx & 0x7fffffff;

    // |x| >= 2**66
    if (ix >= 0x44100000)
    {
      if (hx > 0)
         return PI_2;
      else
         return -PI_2;
    }
    // |x| <= 7/16 (=0.4375)
    else if (ix <= 0x3fdc0000)
      return kernelAtan(x);

    else if (hx > 0)
    {
      // x <= 11/16
      if (ix <= 0x3fe60000)
         return ATAN_05 + kernelAtan ((2.0*x - 1.0) / (2.0 + x));
      // x <= 19/16
      else if (ix <= 0x3ff30000)
         return ATAN_10 + kernelAtan ((x - 1.0) / (1.0 + x));
      // x <= 39/16
      else if (ix <= 0x40038000)
         return ATAN_15 + kernelAtan ((2.0*x - 3.0) / (2.0 + 3.0*x));
      // x > 39/16
      else
         return PI_2 - kernelAtan (1.0 / x);
    }
    else
    {
      // x >= -11/16
      if (ix <= 0x3fe60000)
         return -ATAN_05 + kernelAtan ((2.0*x + 1.0) / (2.0 - x));
      // x >= -19/16
      else if (ix <= 0x3ff30000)
         return -ATAN_10 + kernelAtan ((x + 1.0) / (1.0 - x));
      // x >= -39/16
      else if (ix <= 0x40038000)
         return -ATAN_15 + kernelAtan ((2.0*x + 3.0) / (2.0 - 3.0*x));
      // x < -39/16
      else
         return -PI_2 - kernelAtan (1.0 / x);
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
  public static double atan2(double y, double x)
  {
    int hx = highPart(x);
    int ix = hx & 0x7fffffff;
    int hy = highPart(y);
    int iy = hy & 0x7fffffff;

    // cases with infinity or NaN
    if ((ix >= 0x7ff00000) || (iy >= 0x7ff00000))
    {
      switch (hx)
      {
         case 0x7ff00000:
            switch (hy)
            {
               case 0x7ff00000: return PI_4;
               case 0xfff00000: return -PI_4;
               case 0x7ff80000: return NaN;
               default: return 0.0;
            }
         case 0xfff00000:
            switch (hy)
            {
               case 0x7ff00000: return PI3_4;
               case 0xfff00000: return -PI3_4;
               case 0x7ff80000: return NaN;
               default: return (hy < 0 ? -PI : PI);
            }
         case 0x7ff80000: return NaN;
         default:
            switch (hy)
            {
               case 0x7ff00000: return PI_2;
               case 0xfff00000: return -PI_2;
               default: return NaN;
            }
      }
    }
    if (y == 0) // guich@502_2: fixed case where y == 0
       return PI;
    else
    if (hy > 0)
       return PI_2 - atan(x/y);
    else if (hy == 0)
       return (hx >= 0 ? 0.0 : PI);
    else
       return -PI_2 - atan(x/y);
  }

  /**
   * Take <em>e</em><sup>x</sup>.  The opposite of <code>log()</code>.
   * @param x the number to raise to the power.
   * @return the number raised to the power of <em>e</em>.
   * @see #log(double)
   * @see #pow(double,double)
   */
  public static double exp(double x)
  {
    return MathUtilities.exp(x);
  }

  /**
   * Take ln(x) (the natural log).  The opposite of <code>exp()</code>.
   * Note that the way to get log<sub>b</sub>(a) is to do this:
   * <code>ln(a) / ln(b)</code>.
   * @param x the number to take the natural log of.
   * @return the natural log of <code>x</code>.
   * @see #exp(double)
   */
  public static double log(double x)
  {
    return MathUtilities.log(x);
  }

  /**
   * Take a square root.
   * For other roots, to pow(x,1/rootNumber).
   * @param x the numeric argument
   * @return the square root of the argument.
   * @see #pow(double,double)
   */
  public static double sqrt(double x)
  {
    return java.lang.Math.sqrt(x);
  }

  /**
   * Take a number to a power.
   * @param x the number to raise.
   * @param y the power to raise it to.
   * @return x<sup>y</sup>.
   */
  public static double pow(double x, double y)
  {
    if (isNaN(x) || isNaN(y))
      return NaN;

    // added to improve accuracy for integer exponents
    // P M Dickerson, 28May2003
    int iy = (int)y;
    if ( iy >= 0 && iy == y && iy <= 1024 )
    {
        double result = 1.0;
        while ( iy != 0 )
        {
            if ( (iy & 1) != 0 )
                result = result * x;
            x = x*x;
            iy >>>= 1;
        }
        return result;
    }

    if (x < 0.0)
    {
      long lly = Convert.doubleToLongBits(y);
      int expY = ((int) ((lly & 0x7ff0000000000000L) >> 52)) - 0x3ff;

      // 0=non-int, 1=odd, 2=even
      int yIntType;
      if (y == 0.0)
         yIntType = 2;
      else if (expY < 0)
         yIntType = 0;
      else if (expY <= 54)
      {
         long temp = ((((lly & 0x000fffffffffffffL) + 0x0010000000000000L) << expY) & 0x001fffffffffffffL);
         if (temp == 0L)
            yIntType = 2;
         else if (temp == 0x0010000000000000L)
           yIntType = 1;
         else
           yIntType = 0;
      }
      else
         yIntType = 2;

      if (yIntType == 0)
         return NaN;
      else if (yIntType == 1)
         return -exp (y * log (-x));
      else return exp (y * log (-x));
    }
    else
      return exp (y * log (x));
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
  public static double IEEEremainder(double x, double y)
  {
    return x - y*rint(x/y);
  }


  /**
   * Take the nearest integer that is that is greater than or equal to the
   * argument.
   * @param x the value to act upon.
   * @return the nearest integer >= <code>x</code>.
   */
  public static double ceil(double x)
  {
    return java.lang.Math.ceil(x);
  }

  /**
   * Take the nearest integer that is that is less than or equal to the
   * argument.
   * @param x the value to act upon.
   * @return the nearest integer <= <code>x</code>.
   */
  public static double floor(double x)
  {
    return java.lang.Math.floor(x);
  }

  /**
   * Take the nearest integer to the argument.  If it is exactly between
   * two integers, the even integer is taken.
   * @param x the value to act upon.
   * @return the nearest integer to <code>x</code>.
   */
  public static double rint(double x)
  {
    if (x == 0.0)
      return x;

    long lx = Convert.doubleToLongBits(x);
    if ((lx & 0x7ff0000000000000L) == 0x7ff0000000000000L)
      return x;

    int exp = (((int)(lx >>> 52)) & 0x7ff) - 1023;
    if (exp >= 52)
      return x;

    // 0 ... +/-0.49999
    else if (exp <= -2)
      return 0.0;

    // +/-0.5 ... +/-0.99999
    else if (exp == -1)
    {
      if ((lx & 0x000fffffffffffffL) == 0L)
         return 0.0;
      else
         return (x > 0.0 ? 1.0 : -1.0);
    }
    else
    {
      double adder = 0.0;
      long fractMask = 0x000fffffffffffffL >> exp;
      long halfMask  = 0x0008000000000000L >> exp;
      long lfract = lx & fractMask;
      if (lfract > halfMask)
         adder = (x > 0.0 ? 1.0 : -1.0);
      else if (lfract == halfMask)
      {
         if ((lx & (halfMask << 1)) != 0L)
            adder = (x > 0.0 ? 1.0 : -1.0);
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
  public static long round(double a)
  {
    return (long)java.lang.Math.floor(a + 0.5d);
  }

  /**
   * Convert from degrees to radians.
   * The formula for this is radians = degrees * (pi/180).
   * @param degrees an angle in degrees
   * @return the angle in radians
   */
  public static double toRadians(double degrees)
  {
    return degrees * 0.017453292519943295; /* (degrees * (PI/180)) */
  }

  /**
   * Convert from radians to degrees.
   * The formula for this is degrees = radians * (180/pi).
   * @param rads an angle in radians
   * @return the angle in degrees
   */
  public static double toDegrees(double rads)
  {
    return rads / 0.017453292519943295; /* (rads / (PI/180)) */
  }

  /**
   * arc tangent for x in [-7/16 ... 7/16].
   */
  private static double kernelAtan (double x)
  {
    int ix = highPart(x) & 0x7fffffff;
    if (ix < 0x3e200000)
      return x;

    else
    {
      double z = x*x;
      double w = z*z;
      double s1 = z*(AT0 + w*(AT2 + w*(AT4 + w*(AT6 + w*(AT8 + w*AT10)))));
      double s2 = w*(AT1 + w*(AT3 + w*(AT5 + w*(AT7 + w*AT9))));
      return x - x*(s1 + s2);
    }
  }

  private static int highPart (double x)
  {
    return (int) (Convert.doubleToLongBits(x) >> 32);
  }

  private static int lowPart (double x)
  {
    return (int) Convert.doubleToLongBits(x);
  }

  public static boolean isNaN(double d)
  {
     return Convert.doubleToLongBits(d) == 0xfff0000000000000L;
  }
}
