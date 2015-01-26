package totalcross.lang;

import totalcross.sys.*;

/** A Float value in TotalCross is actually a Double value. */

public class Float4D extends Double4D
{
   public static final Class<Float> TYPE = Float.class;
   double v;
   
   public Float4D(double v)
   {
      super(v);
   }
   public double floatValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Float4D && ((Float4D)o).v == this.v; 
   }
   
   /**
    * Convert the float to the IEEE 754 floating-point "single format" bit
    * layout. Bit 31 (the most significant) is the sign bit, bits 30-23
    * (masked by 0x7f800000) represent the exponent, and bits 22-0
    * (masked by 0x007fffff) are the mantissa. This function collapses all
    * versions of NaN to 0x7fc00000. The result of this function can be used
    * as the argument to <code>Float.intBitsToFloat(int)</code> to obtain the
    * original <code>float</code> value.
    *
    * @param value the <code>float</code> to convert
    * @return the bits of the <code>float</code>
    * @see #intBitsToFloat(int)
    */
   public static int floatToIntBits(double value)
   {
     if (isNaN(value))
       return 0x7fc00000;
     else
       return floatToRawIntBits(value);
   }
   
   /**
    * Convert the float to the IEEE 754 floating-point "single format" bit
    * layout. Bit 31 (the most significant) is the sign bit, bits 30-23
    * (masked by 0x7f800000) represent the exponent, and bits 22-0
    * (masked by 0x007fffff) are the mantissa. This function leaves NaN alone,
    * rather than collapsing to a canonical value. The result of this function
    * can be used as the argument to <code>Float.intBitsToFloat(int)</code> to
    * obtain the original <code>float</code> value.
    *
    * @param value the <code>float</code> to convert
    * @return the bits of the <code>float</code>
    * @see #intBitsToFloat(int)
    */
   public static int floatToRawIntBits(double value)
   {
      return Convert.doubleToIntBits(value);
   }
   
   /**
    * Behaves like <code>new Float(x).compareTo(new Float(y))</code>; in
    * other words this compares two floats, special casing NaN and zero,
    * without the overhead of objects.
    *
    * @param x the first float to compare
    * @param y the second float to compare
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
       int ix = floatToRawIntBits(x);
       int iy = floatToRawIntBits(y);
       if (ix == iy)
           return 0;

       // handle NaNs:
       if (x != x)
           return (y != y) ? 0 : 1;
       else if (y != y)
           return -1;

       // handle +/- 0.0
       return (ix < iy) ? -1 : 1;
   }
   
   public String toString()
   {
      return String.valueOf(v);
   }
}
