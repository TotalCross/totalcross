package totalcross.sql;

import totalcross.sql.sqlite4j.*;
import totalcross.util.*;
import java.sql.SQLException;

public class DriverManager
{
   public static Logger getLogWriter()
   {
      return null;
   }
   public static void setLogWriter(Logger out)
   {
   }
   public static Connection getConnection(String url, Properties info) throws SQLException
   {
      return null;
   }
   public static Connection getConnection(String url, String user, String password) throws SQLException
   {
      return null;
   }
   public static Connection getConnection(String url) throws SQLException
   {
      if (url.startsWith("jdbc:sqlite")) // :sample.db
      {
         String dbname = "temp.db";
         int l = url.length();
         if (l > 11 && url.endsWith(".db"))
            dbname = url.substring(12);
         try
         {
            return new SQLite4JConnection(new org.sqlite.SQLiteConnection(url, dbname));
         }
         catch (java.sql.SQLException e)
         {
            throw new SQLException(e);
         }
      }
      return null;
   }
   public static Driver getDriver(String url) throws SQLException
   {
      return null;
   }
   public static void registerDriver(Driver driver) throws SQLException
   {
   }
   public static void deregisterDriver(Driver driver) throws SQLException
   {
   }
   public static Driver[] getDrivers()
   {
      return null;
   }
   public static void setLoginTimeout(int seconds)
   {
   }
   public static int getLoginTimeout()
   {
      return 0;
   }
   public static void println(String message)
   {
   }
}
