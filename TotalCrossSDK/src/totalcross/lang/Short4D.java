package totalcross.lang;

import totalcross.sys.*;

public class Short4D
{
   public static final Class<Short> TYPE = Short.class;
   short v;
   
   public Short4D(short v)
   {
      this.v = v;
   }
   public short shortValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Short4D && ((Short4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return v;
   }
   public String toString()
   {
      return String.valueOf(v);
   }
   public static Short4D valueOf(short s)
   {
      return new Short4D(s);
   }
   public static Short4D valueOf(String s) throws NumberFormatException
   {
      try
      {
         return new Short4D((short)Convert.toInt(s));
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
}
