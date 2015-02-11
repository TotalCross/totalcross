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
   static totalcross.sys.Time time(String x) // 2015-01-14 17:28:09.708
   {
      try
      {
         return x == null ? null : new Time(x, true,true,true,true,true,true, Settings.DATE_YMD);
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE) e.printStackTrace();
         return null;
      }
   }

   static java.sql.Date date(Date x)
   {
      return x == null ? null : new java.sql.Date(x.getTime());
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
