package totalcross.lang;

import totalcross.sys.*;

public class Long4D
{
   public static final Class<Long> TYPE = Long.class;
   long v;
   
   public Long4D(long v)
   {
      this.v = v;
   }
   public Long4D(String s) throws NumberFormatException
   {
      v = parseLong(s);
   }
   public long longValue()
   {
      return v;
   }
   public boolean equals(Object o)
   {
      return o != null && o instanceof Long4D && ((Long4D)o).v == this.v; 
   }
   public int hashCode()
   {
      return (int)(v ^ (v >>> 32));
   }
   public String toString(long l)
   {
      return Convert.toString(l);
   }
   public String toString()
   {
      return String.valueOf(v);
   }
   public Long4D valueOf(long l)
   {
      return new Long4D(l);
   }
   public static Long4D valueOf(String s) throws NumberFormatException
   {
      try
      {
         return new Long4D(Convert.toLong(s));
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
   public static long parseLong(String s) throws NumberFormatException
   {
      try
      {
         return Convert.toLong(s);
      }
      catch (InvalidNumberException ine)
      {
         throw new NumberFormatException(ine.getMessage());
      }
   }
   public int intValue()
   {
     return (int) v;
   }
}
