package totalcross.sql.sqlite4j;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.util.*;

class Convert
{
   private Convert()
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
      return new totalcross.sys.Time(x.getTime());
   }

   static java.sql.Date date(Date x)
   {
      return new java.sql.Date(x.getTime());
   }
   static Date date(java.sql.Date x)
   {
      try
      {
         return new Date(x.getDay(), x.getMonth(), x.getYear());
      }
      catch (InvalidDateException e)
      {
         return null;
      }
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
