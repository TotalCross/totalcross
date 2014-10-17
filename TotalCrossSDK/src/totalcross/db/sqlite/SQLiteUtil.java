package totalcross.db.sqlite;

import java.sql.SQLException;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.util.*;

/** Utility class to make convertion from Litebase to SQLite easier. 
 */

public class SQLiteUtil
{
   private Connection con;
   public int vectorInitialSize = 50;
   private String fullPath;

   /** Open a connection at the given table */
   public SQLiteUtil(String table) throws SQLException
   {
      this.fullPath = table;
   }
   
   /** Open a connection at the given path and table */
   public SQLiteUtil(String path, String table) throws SQLException
   {
      this(Convert.appendPath(path, table));
   }
   
   /** Open a connection at memory */
   public SQLiteUtil() throws SQLException
   {
      this("");
   }
   
   /** Returns the connecton with the parameters passed in the constructor */
   public Connection con() throws SQLException
   {
      if (con == null || con.isClosed())
         con = DriverManager.getConnection("jdbc:sqlite:"+fullPath);
      return con;
   }
   
   public void close()
   {
      try
      {
         if (con != null) con.close();
      }
      catch (Exception e) {if (Settings.onJavaSE) e.printStackTrace();}
      con = null;
   }
   
   public boolean tableExists(String tab) throws SQLException
   {
      return isNotEmpty("SELECT name FROM sqlite_master WHERE type='table' AND lower(name)='"+tab.toLowerCase()+"'");
   }
   
   public boolean isNotEmpty(String sql) 
   {
      try
      {
         Statement st = con().createStatement();
         ResultSet rs = st.executeQuery(sql);
         boolean exists = rs.next();
         rs.close();
         return exists;
      }
      catch (SQLException e)
      {
         if (e.getErrorCode() != 1 && Settings.onJavaSE) e.printStackTrace();
      }
      return false;
   }

   public int getColCount(ResultSet rs) throws SQLException
   {
      return rs.getMetaData().getColumnCount();
   }

   public ResultSet executeQuery(String s) throws SQLException
   {
      return con().createStatement().executeQuery(s);
   }
   public void close(ResultSet rs)
   {
      try
      {
         rs.getStatement().close();
         rs.close();
      }
      catch (Exception e) {}
   }   

   public String[] getStrings1(String sql) 
   {
      try
      {
         ResultSet rs = executeQuery(sql);
         Vector out = new Vector(vectorInitialSize);
         while (rs.next())
            out.addElement(rs.getString(1));
         close(rs);
         return (String[])out.toObjectArray();
      }
      catch (Exception e)
      {
         if (Settings.onJavaSE) e.printStackTrace();
         return null;
      }
   }

   public String[][] getStrings(ResultSet rs, Vector v) throws SQLException
   {
      int cols = getColCount(rs);
      while (rs.next())
      {
         String[] linha = new String[cols];
         for (int i = 0; i < cols; i++)
            linha[i] = rs.getString(i+1);
         v.addElement(linha);
      }
      String[][] ss = new String[v.size()][];
      v.copyInto(ss);
      return ss;
   }

   public String[][] getStrings(ResultSet rs) throws SQLException
   {
      return getStrings(rs, new Vector(vectorInitialSize));
   }
   
   public String[][] getStrings(String sql) throws SQLException
   {
      ResultSet rs = executeQuery(sql);
      String[][] ret = getStrings(rs, new Vector(vectorInitialSize));
      rs.close();
      return ret;
   }
   
   public String getString(String sql) throws SQLException
   {
      ResultSet rs = executeQuery(sql);
      String ret = rs.next() ? rs.getString(1) : null;
      close(rs);
      return ret;
   }

   public int getInt(String sql) throws SQLException
   {
      ResultSet rs = executeQuery(sql);
      int ret = rs.next() ? rs.getInt(1) : 0;
      close(rs);
      return ret;
   }

   public int getShort(String sql) throws SQLException
   {
      ResultSet rs = executeQuery(sql);
      int ret = rs.next() ? rs.getShort(1) : 0;
      close(rs);
      return ret;
   }

   public int getRowCount(String table)
   {
      try
      {
         return Math.max(0,getInt("select count(*) from "+table));
      }
      catch (Exception e)
      {
         return 0;
      }
   }

   public void startTransaction() throws SQLException
   {
      con().setAutoCommit(false);
   }
   
   public void finishTransaction() throws SQLException
   {
      con().commit();
      con().setAutoCommit(true);
   }
   
   public void rollback() throws SQLException
   {
      con().rollback();
      con().setAutoCommit(true);
   }

   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      return con().prepareStatement(sql);
   }

   public String[] listAllTables() 
   {
      return getStrings1("SELECT name FROM sqlite_master WHERE type = 'table' AND name != 'android_metadata' AND name != 'sqlite_sequence';");
   }
   
   /** Handles single quote when inserting or retrieving data from Sqlite.
    * Example:
    * <pre>
    * String s = SQLiteUtil.fixQuote("'",true); // returns ''
    * String s = SQLiteUtil.fixQuote("''",false); // returns '
    * </pre>
    */
   public static String fixQuote(String s, boolean toSqlite)
   {
      return toSqlite ? Convert.replace(s,"'","''") : Convert.replace(s,"''","'");
   }

   /** Changes a date in format 2014-02-19 00:00:00:000 to a totalcross.util.Date. 
    */
   public static Date fromSqlDate(String sqldate) throws InvalidDateException
   {
      int sp = sqldate.indexOf(' ');
      return new Date(sp == -1 ? sqldate : sqldate.substring(0,sp), Settings.DATE_YMD);
   }
}
