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
      if (url.startsWith("jdbc:sqlite:")) // :sample.db
      {
         int l = url.length();
         String dbname = l == 12 ? "temp.db" : url.substring(12);
         if ((Settings.isIOS() || Settings.platform.equals(Settings.ANDROID)) && dbname.indexOf("/") == -1 && dbname.indexOf(":memory:") == -1 && dbname.indexOf("mode=memory") == -1) // dont use this for memory databases
         {
            // in ios and android its required that the user specify a valid path. if he don't, we put the database in the app path
            boolean isfile = dbname.startsWith("file:");
            if (isfile) dbname = dbname.substring(5);
            dbname = Convert.appendPath(Settings.appPath,dbname);
            if (isfile) dbname = "file:".concat(dbname);
            Vm.debug("changing dbname to "+dbname);
         }
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
      java.util.Properties props = new java.util.Properties();
      props.put("date_string_format", "yyyy-MM-dd");
      return new SQLite4JConnection(new org.sqlite.SQLiteConnection(url, dbname, props));
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
