package totalcross.sql;

import java.sql.SQLException;

import totalcross.sql.sqlite4j.*;
import totalcross.sys.*;
import totalcross.util.*;

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
   static String initCause(Throwable e)
   {
      return " Cause: "+e.getMessage()+"\n trace: "+Vm.getStackTrace(e);
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
            return newConnection(url, dbname);
         }
         catch (java.sql.SQLException e)
         {
            throw new SQLException("Can't get connection: "+url+initCause(e));
         }
      }
      return null;
   }
   static Connection newConnection(String url, String dbname) throws SQLException
   {
      return new SQLite4JConnection(new org.sqlite.SQLiteConnection(url, dbname));
   }
   static Connection newConnection4D(String url, String dbname) throws SQLException
   {
      return new totalcross.db.sqlite.SQLiteConnection(url, dbname);
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
