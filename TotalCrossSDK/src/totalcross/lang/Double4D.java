package totalcross.lang;

import totalcross.sys.*;

public class Double4D
{
   public static final Class<Double> TYPE = Double.class;
   double v;
   
   public Double4D(double v)
   {
      this.v = v;
   }
   public Double4D(String s) throws NumberFormatException
   {
      this.v = parseDouble(s);
   }
   public double doubleValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Double4D && ((Double4D)o).v == this.v; 
   }
   public int hashCode() // same of java to keep compatibility
   {
      long r = Convert.doubleToLongBits(v);
      if ((r & 0x7FF0000000000000L) == 0x7FF0000000000000L && (r & 0x000FFFFFFFFFFFFFL) != 0L)
         r = 0x7ff8000000000000L;
      return (int)(r ^ (r >>> 32));
   }
   public String toString()
   {
      return String.valueOf(v);
   }
   public static Double4D valueOf(double d)
   {
      return new Double4D(d);
   }
   public static Double4D valueOf(String s) throws NumberFormatException
   {
      try
      {
         return new Double4D(Convert.toDouble(s));
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
   public static double parseDouble(String str) throws NumberFormatException
   {
      try
      {
         return Convert.toDouble(str);
      }
      catch (InvalidNumberException e)
      {
         throw new NumberFormatException(e.getMessage());
      }
   }
   public static boolean isNaN(double v)
   {
      return v == Convert.DOUBLE_NAN_VALUE;
   }
   public static boolean isInfinite(double v)
   {
      return v == Convert.DOUBLE_POSITIVE_INFINITY_VALUE || v == Convert.DOUBLE_NEGATIVE_INFINITY_VALUE;
   }
   public static String toString(double d)
   {
      return Convert.toString(d);
   }
   public boolean isInfinite()
   {
      return v == Convert.DOUBLE_POSITIVE_INFINITY_VALUE || v == Convert.DOUBLE_NEGATIVE_INFINITY_VALUE;
   }
   public boolean isNaN()
   {
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
   public static long doubleToLongBits(double value)
   {
     if (isNaN(value))
       return 0x7ff8000000000000L;
     else
       return doubleToRawLongBits(value);
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
   public static long doubleToRawLongBits(double value)
   {
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
   public static int compare(double x, double y)
   {
       // handle the easy cases:
       if (x < y)
           return -1;
       if (x > y)
           return 1;

       // handle equality respecting that 0.0 != -0.0 (hence not using x == y):
       long lx = doubleToRawLongBits(x);
       long ly = doubleToRawLongBits(y);
       if (lx == ly)
           return 0;

       // handle NaNs:
       if (x != x)
           return (y != y) ? 0 : 1;
       else if (y != y)
           return -1;

       // handle +/- 0.0
       return (lx < ly) ? -1 : 1;
   }
}
