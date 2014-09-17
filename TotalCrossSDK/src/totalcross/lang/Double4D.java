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
      return super.toString()+" v="+v;
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
}
