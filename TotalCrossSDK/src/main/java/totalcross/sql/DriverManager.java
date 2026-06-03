package totalcross.sql;

import java.sql.SQLException;

import totalcross.sql.sqlite4j.SQLite4JConnection;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.util.Hashtable;
import totalcross.util.Logger;
import totalcross.util.Properties;
import totalcross.util.Vector;

public class DriverManager {
  public static Logger getLogWriter() {
    return null;
  }

  public static void setLogWriter(Logger out) {
  }

  public static Connection getConnection(String url, Properties info) throws SQLException {
      if (url.startsWith("jdbc:sqlite:")) // :sample.db
      {
        int l = url.length();
        String dbname = l == 12 ? "temp.db" : url.substring(12);
        if ((Settings.isIOS() || Settings.platform.equals(Settings.ANDROID)) && dbname.indexOf("/") == -1
            && dbname.indexOf(":memory:") == -1 && dbname.indexOf("mode=memory") == -1) // dont use this for memory databases
        {
          // in ios and android its required that the user specify a valid path. if he don't, we put the database in the app path
          boolean isfile = dbname.startsWith("file:");
          if (isfile) {
            dbname = dbname.substring(5);
          }
          dbname = Convert.appendPath(Settings.appPath, dbname);
          if (isfile) {
            dbname = "file:".concat(dbname);
          }
          Vm.debug("changing dbname to " + dbname);
        }
        try {
          return newConnection(url, dbname, info);
        } catch (java.sql.SQLException e) {
          throw new SQLException("Can't get connection: " + url + initCause(e));
        }
      }
      return null;
    }

  public static Connection getConnection(String url, String user, String password) throws SQLException {
    return null;
  }

  static String initCause(Throwable e) {
    return " Cause: " + e.getMessage() + "\n trace: " + Vm.getStackTrace(e);
  }

  public static Connection getConnection(String url) throws SQLException {
      return getConnection(url, null);
  }

    static Connection newConnection(String url, String dbname, Properties props) throws SQLException {
        java.util.Properties props2 = new java.util.Properties();
        if (props != null) {
            Vector keys = props.getKeys();
            for (int i = keys.size() - 1; i >= 0; i--) {
                String key = (String) keys.items[i];
                props2.put(key, props.get(key).toString());
            }
        }
        props2.put("date_string_format", "yyyy-MM-dd");
        return new SQLite4JConnection(new org.sqlite.jdbc4.JDBC4Connection(url, dbname, props2));
    }

    static Connection newConnection4D(String url, String dbname, Properties props) throws SQLException {
        Hashtable props2 = new Hashtable(10);
        if (props != null) {
            Vector keys = props.getKeys();
            for (int i = keys.size() - 1; i >= 0; i--) {
                String key = (String) keys.items[i];
                props2.put(key, props.get(key).toString());
            }
        }
        return new totalcross.db.sqlite.SQLiteConnection(url, dbname, props2);
    }

    static Connection newConnection(String url, String dbname) throws SQLException {
        return newConnection(url, dbname, null);
    }

    static Connection newConnection4D(String url, String dbname) throws SQLException {
        return newConnection4D(url, dbname, null);
    }

  public static Driver getDriver(String url) throws SQLException {
    return null;
  }

  public static void registerDriver(Driver driver) throws SQLException {
  }

  public static void deregisterDriver(Driver driver) throws SQLException {
  }

  public static Driver[] getDrivers() {
    return null;
  }

  public static void setLoginTimeout(int seconds) {
  }

  public static int getLoginTimeout() {
    return 0;
  }

  public static void println(String message) {
  }
}
