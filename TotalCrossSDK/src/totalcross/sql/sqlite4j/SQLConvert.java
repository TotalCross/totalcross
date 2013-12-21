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
         return new java.sql.Time(new Date(x).getTime() + x.hour * 60*60*1000 + x.minute*60*1000+ x.second*1000);
      }
      catch (InvalidDateException e)
      {
         return null;
      }
   }
   static totalcross.sys.Time time(java.sql.Time x)
   {
      return x == null ? null : new totalcross.sys.Time(x.getTime());
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
            int d = x.getDate();
            int m = x.getMonth()+1;
            int y = x.getYear()+1900;
            return new Date(d, m, y);
         }
         catch (Exception e)
         {
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
