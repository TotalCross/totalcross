package totalcross.sql.sqlite4j;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.util.*;

class SQLConvert
{
   private SQLConvert()
   {
   }

   static java.sql.Time time(totalcross.sys.Time x)
   {
      try
      {
         return x == null ? null : new java.sql.Time(x.getTime());
      }
      catch (InvalidDateException e)
      {
         return null;
      }
   }
   static totalcross.sys.Time time(java.sql.Time x)
   {
      try
      {
         return x == null ? null : new totalcross.sys.Time(x.getTime(),true);
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE) e.printStackTrace();
         return null;
      }
   }

   static java.sql.Date date(Date x)
   {
      return new java.sql.Date(x.getTime());
   }
   static Date date(java.sql.Date x)
   {
      if (x != null)
         try
         {
            Time t = new totalcross.sys.Time(x.getTime(),true);
            return new Date(t);
         }
         catch (Exception e)
         {
            if (Settings.onJavaSE) e.printStackTrace();
            return null;
         }
      return null;
   }
   
   private static void test(long l)
   {
      try
      {
         Time t = new Time(l); // como vem da nuvem
         long lt = t.getTime(); // desde 1/1/1970
         Time t2 = new Time(lt,true);
         long lf = t2.getTimeLong();
         String s = t2.getSQLString();
         System.out.println(l+" -> "+lf+" ("+s+")");
      }
      catch (InvalidDateException e)
      {
         e.printStackTrace();
      }
   }
   public static void main(String args[])
   {
      Settings.is24Hour = true;
      Settings.dateSeparator = '/';
      Settings.timeZoneMinutes = -240;
      
      totalcross.sys.Time t0 = new totalcross.sys.Time();
      java.sql.Time t1 = time(t0);
      totalcross.sys.Time t2 = time(t1);
      System.out.println(t0.getTimeLong()+" -> "+t2.getTimeLong());
      
      test(20121203000000L);
      test(20130127232922L); // fails if Settings.timeZoneMinutes is considered
      test(20130125125200L);
   }

   static java.sql.Timestamp timestamp(Timestamp x)
   {
      return new java.sql.Timestamp(x.getTime());
   }
   static Timestamp timestamp(java.sql.Timestamp x)
   {
      return new Timestamp(x.getTime());
   }

   public static java.math.BigDecimal bigdecimal(BigDecimal x)
   {
      return new java.math.BigDecimal(x.toPlainString());
   }
   public static BigDecimal bigdecimal(java.math.BigDecimal x)
   {
      try
      {
         return new BigDecimal(x.toPlainString());
      }
      catch (InvalidNumberException e)
      {
         return null;
      }
   }
}
