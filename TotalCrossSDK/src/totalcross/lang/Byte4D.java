package totalcross.lang;

import totalcross.sys.*;

public class Byte4D
{
   public static final Class<Byte> TYPE = Byte.class;
   byte v;
   
   public Byte4D(byte v)
   {
      this.v = v;
   }
   public byte byteValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Byte4D && ((Byte4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return v;
   }
   public String toString()
   {
      return String.valueOf(v);
   }
   public static Byte4D valueOf(byte b)
   {
      return new Byte4D(b);
   }
   public static Byte4D valueOf(String s) throws NumberFormatException
   {
      try
      {
         return new Byte4D((byte)Convert.toInt(s));
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
}
