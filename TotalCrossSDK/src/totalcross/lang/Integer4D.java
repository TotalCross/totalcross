package totalcross.lang;

import totalcross.sys.*;

public class Integer4D
{
   public static final Class<Integer> TYPE = Integer.class;
   int v;
   
   public Integer4D(int v)
   {
      this.v = v;
   }
   public int intValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Integer4D && ((Integer4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return v;
   }
   public String toString()
   {
      return String.valueOf(v);
   }
   public static Integer4D valueOf(int i)
   {
      return new Integer4D(i);
   }
   
   public static Integer4D valueOf(String s) throws NumberFormatException
   {
      try
      {
         return new Integer4D(Convert.toInt(s));
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
   public static int parseInt(String str) throws NumberFormatException
   {
      try
      {
         return Convert.toInt(str);
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
      
   public static int parseInt(String str, int radix) throws NumberFormatException
   {
      try
      {
         if (radix == 10)
            return Convert.toInt(str);
         return (int)Convert.toLong(str, radix);
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }

   public static String toHexString(int i)
   {
     return Convert.unsigned2hex(i, 4);
   }
   public static String toString(int v)
   {
      return String.valueOf(v);
   }
}
