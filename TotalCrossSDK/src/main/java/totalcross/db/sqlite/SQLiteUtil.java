// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.db.sqlite;

import java.sql.SQLException;
import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.ResultSetMetaData;
import totalcross.sql.Statement;
import totalcross.sql.Types;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Time;
import totalcross.util.Date;
import totalcross.util.InvalidDateException;
import totalcross.util.Vector;

/** Utility class to make convertion from Litebase to SQLite easier. 
 * 
 * Important rules about date and time in SQLite.
 * 
 * <ul>
 * <li> DATE type: must be in the form: YYYY-MM-DD
 * <li> TIME type: must be in the form: YYYY-MM-DD HH:MM:SS.MMM
 * </ul>
 * 
 * When using 'between' or any other date/time comparation, the arguments MUST MATCH the type form.
 * So, in "select borndate from test where borndate between '2014-12-10' and '2014-12-14'":
 * <ul>
 * <li> If borndate is a DATE, the comparison will succeed.
 * <li> If borndate is a TIME, the comparison will fail. To make it work, use 
 * "select borndate from test where borndate between '2014-12-10 00:00:00.000' and '2014-12-14 00:00:00.000'".
 * </ul>
 * @see totalcross.util.Date#getSQLString()
 * @see totalcross.sys.Time#getSQLString()
 */

public class SQLiteUtil {
  private Connection con;
  public int vectorInitialSize = 50;
  public String fullPath;

  /** Open a connection at the given table */
  public SQLiteUtil(String table) throws SQLException {
    this.fullPath = table;
  }

  /** Open a connection at the given path and table */
  public SQLiteUtil(String path, String table) throws SQLException {
    this(Convert.appendPath(path, table));
  }

  /** Open a connection at memory */
  public SQLiteUtil() throws SQLException {
    this("");
  }

  /** Use the given connection in all operations */
  public SQLiteUtil(Connection con) {
    this.con = con;
  }

  /** Returns the connecton with the parameters passed in the constructor */
  public Connection con() throws SQLException {
    if (con == null || con.isClosed()) {
      con = DriverManager.getConnection("jdbc:sqlite:" + fullPath);
    }
    return con;
  }

  public void close() {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
    con = null;
  }

  public boolean tableExists(String tab) throws SQLException {
    return isNotEmpty("SELECT name FROM sqlite_master WHERE type='table' AND lower(name)='" + tab.toLowerCase() + "'");
  }

  public boolean isNotEmpty(String sql) {
    try {
      return isNotEmpty(executeQuery(sql + " limit 1"));
    } catch (SQLException e) {
      if (e.getErrorCode() != 1 && Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public boolean isNotEmpty(ResultSet rs) {
    try {
      boolean exists = rs.next();
      close(rs);
      return exists;
    } catch (SQLException e) {
      if (e.getErrorCode() != 1 && Settings.onJavaSE) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public int getColCount(ResultSet rs) throws SQLException {
    return rs.getMetaData().getColumnCount();
  }

  public ResultSet executeQuery(String s) throws SQLException {
    return con().createStatement().executeQuery(s);
  }

  public void close(ResultSet rs) {
    try {
      rs.getStatement().close();
      rs.close();
    } catch (Exception e) {
    }
  }

  public String[] getStrings1(String sql) {
    try {
      return getStrings1(executeQuery(sql));
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
      return null;
    }
  }

  public String[] getStrings1(ResultSet rs) {
    try {
      Vector out = new Vector(vectorInitialSize);
      while (rs.next()) {
        out.addElement(rs.getString(1));
      }
      close(rs);
      return (String[]) out.toObjectArray();
    } catch (Exception e) {
      if (Settings.onJavaSE) {
        e.printStackTrace();
      }
      return null;
    }
  }

  /** SQLite has a problem (not sure if its a bug) where it returns DATE for both 
   * DATE and DATETIME types, so this method returns the correct correspondence:
   * DATE for date and TIME for DATETIME (remember that in TotalCross, a Time object also
   * contains the date).
   * @param md The ResultSetMetaData obtained with ResultSet.getMetaData.
   * @param col The column, starting from 1.
   */
  public int getColumnType(ResultSetMetaData md, int col) throws SQLException {
    String s = md.getColumnTypeName(col);
    return s.equals("DATE") ? Types.DATE : s.equals("DATETIME") ? Types.TIME : md.getColumnType(col);
  }

  public String[][] getStrings(ResultSet rs, Vector v) throws SQLException {
    return getStrings(rs, v, null);
  }

  public String[][] getStrings(ResultSet rs, Vector v, int[] decimalPlaces) throws SQLException {
    int cols = getColCount(rs);
    int[] types = new int[cols];
    ResultSetMetaData md = rs.getMetaData();
    for (int i = types.length; --i >= 0;) {
      types[i] = getColumnType(md, i + 1);
    }
    while (rs.next()) {
      String[] linha = new String[cols];
      for (int i = 0; i < cols; i++) {
        switch (types[i]) {
        case Types.DATE:
          Date dt = rs.getDate(i + 1);
          linha[i] = dt == null ? "" : dt.toString();
          break;
        case Types.TIME:
          Time tm = rs.getTime(i + 1);
          linha[i] = tm == null ? "" : tm.toString();
          break;
        case Types.DOUBLE:
          linha[i] = Convert.toString(rs.getDouble(i + 1), decimalPlaces == null ? -1 : decimalPlaces[i]);
          break;
        default:
          linha[i] = rs.getString(i + 1);
        }
      }
      v.addElement(linha);
    }
    String[][] ss = new String[v.size()][];
    v.copyInto(ss);
    close(rs);
    return ss;
  }

  public String[][] getStrings(ResultSet rs) throws SQLException {
    return getStrings(rs, new Vector(vectorInitialSize));
  }

  public String[][] getStrings(String sql) throws SQLException {
    return getStrings(executeQuery(sql), new Vector(vectorInitialSize));
  }

  public String getString(String sql) throws SQLException {
    ResultSet rs = executeQuery(sql);
    String ret = rs.next() ? rs.getString(1) : null;
    close(rs);
    return ret;
  }

  public int getInt(String sql) throws SQLException {
    return getInt(executeQuery(sql));
  }

  public int getInt(ResultSet rs) throws SQLException {
    int ret = rs.next() ? rs.getInt(1) : 0;
    close(rs);
    return ret;
  }

  public int getShort(String sql) throws SQLException {
    return getShort(executeQuery(sql));
  }

  public int getShort(ResultSet rs) throws SQLException {
    int ret = rs.next() ? rs.getShort(1) : 0;
    close(rs);
    return ret;
  }

  public int getRowCount(String table) {
    try {
      return Math.max(0, getInt("select count(*) from " + table));
    } catch (Exception e) {
      return 0;
    }
  }

  public void startTransaction() throws SQLException {
    con().setAutoCommit(false);
  }

  public void finishTransaction() throws SQLException {
    con().commit();
    con().setAutoCommit(true);
  }

  public void rollback() throws SQLException {
    con().rollback();
    con().setAutoCommit(true);
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return con().prepareStatement(sql);
  }

  public String[] listAllTables() {
    return getStrings1(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name != 'android_metadata' AND name != 'sqlite_sequence';");
  }

  /** Handles single quote when inserting or retrieving data from Sqlite.
   * Example:
   * <pre>
   * String s = SQLiteUtil.fixQuote("'",true); // returns ''
   * String s = SQLiteUtil.fixQuote("''",false); // returns '
   * </pre>
   */
  public static String fixQuote(String s, boolean toSqlite) {
    return toSqlite ? Convert.replace(s, "'", "''") : Convert.replace(s, "''", "'");
  }

  /** Changes a date in format 2014-02-19 00:00:00:000 to a totalcross.util.Date. 
   */
  public static Date fromSqlDate(String sqldate) throws InvalidDateException {
    int sp = sqldate.indexOf(' ');
    return new Date(sp == -1 ? sqldate : sqldate.substring(0, sp), Settings.DATE_YMD);
  }

  /** Rebuild and shrink the entire database, like the old Litebase's <code>purge</code> method,
   * but in this case it applies to all tables. 
   */
  public void shrinkDB() throws SQLException {
    Statement st = con().createStatement();
    st.execute("VACUUM;");
    st.close();
  }

  /**
   * Check if the index with the name exists in this table
   * 
   * @param tableName
   *        to check
   * @param indexName
   *        to check
   * @param connection
   *        with the database
   * @return true if the index exists, otherwise false;
   * @throws SQLException
   */
  public static final boolean existsIndex(String tableName, String indexName, Connection connection)
      throws SQLException {
    PreparedStatement prepareStatement = null;
    ResultSet rs = null;
    try {
      prepareStatement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='index' AND name='"
          + indexName + "' AND tbl_name='" + tableName + "';");
      rs = prepareStatement.executeQuery();
      return rs.next();
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (prepareStatement != null) {
        prepareStatement.close();
      }
    }
  }
}
