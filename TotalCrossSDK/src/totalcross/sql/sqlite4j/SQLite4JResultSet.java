package totalcross.sql.sqlite4j;

import totalcross.sql.ResultSet;
import totalcross.sql.ResultSetMetaData;
import totalcross.sql.Statement;
import totalcross.sql.Timestamp;
import totalcross.sys.Time;
import totalcross.util.*;
import totalcross.util.Date;

import java.sql.SQLException;
import java.sql.SQLWarning;

public class SQLite4JResultSet implements ResultSet
{
   java.sql.ResultSet rs;
   
   public SQLite4JResultSet(java.sql.ResultSet rs)
   {
      this.rs = rs;
   }

   public boolean next() throws SQLException
   {
      return rs.next();
   }

   public void close() throws SQLException
   {
      rs.close();
   }

   public boolean wasNull() throws SQLException
   {
      return rs.wasNull();
   }

   public String getString(int columnIndex) throws SQLException
   {
      return rs.getString(columnIndex);
   }

   public boolean getBoolean(int columnIndex) throws SQLException
   {
      return rs.getBoolean(columnIndex);
   }

   public byte getByte(int columnIndex) throws SQLException
   {
      return rs.getByte(columnIndex);
   }

   public short getShort(int columnIndex) throws SQLException
   {
      return rs.getShort(columnIndex);
   }

   public int getInt(int columnIndex) throws SQLException
   {
      return rs.getInt(columnIndex);
   }

   public long getLong(int columnIndex) throws SQLException
   {
      return rs.getLong(columnIndex);
   }

   public double getFloat(int columnIndex) throws SQLException
   {
      return rs.getFloat(columnIndex);
   }

   public double getDouble(int columnIndex) throws SQLException
   {
      return rs.getDouble(columnIndex);
   }

   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
   {
      return SQLConvert.bigdecimal(rs.getBigDecimal(columnIndex));
   }

   public byte[] getBytes(int columnIndex) throws SQLException
   {
      return rs.getBytes(columnIndex);
   }

   public Date getDate(int columnIndex) throws SQLException
   {
      java.sql.Date dd = rs.getDate(columnIndex);
      return SQLConvert.date(dd);
   }

   public Time getTime(int columnIndex) throws SQLException
   {
      return SQLConvert.time(rs.getString(columnIndex));
   }

   public Timestamp getTimestamp(int columnIndex) throws SQLException
   {
      return SQLConvert.timestamp(rs.getTimestamp(columnIndex));
   }

   public String getString(String columnName) throws SQLException
   {
      return rs.getString(columnName);
   }

   public boolean getBoolean(String columnName) throws SQLException
   {
      return rs.getBoolean(columnName);
   }

   public byte getByte(String columnName) throws SQLException
   {
      return rs.getByte(columnName);
   }

   public short getShort(String columnName) throws SQLException
   {
      return rs.getShort(columnName);
   }

   public int getInt(String columnName) throws SQLException
   {
      return rs.getInt(columnName);
   }

   public long getLong(String columnName) throws SQLException
   {
      return rs.getLong(columnName);
   }

   public double getFloat(String columnName) throws SQLException
   {
      return rs.getFloat(columnName);
   }

   public double getDouble(String columnName) throws SQLException
   {
      return rs.getDouble(columnName);
   }

   public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
   {
      return SQLConvert.bigdecimal(rs.getBigDecimal(columnName));
   }

   public byte[] getBytes(String columnName) throws SQLException
   {
      return rs.getBytes(columnName);
   }

   public Date getDate(String columnName) throws SQLException
   {
      return SQLConvert.date(rs.getDate(columnName));
   }

   public Time getTime(String columnName) throws SQLException
   {
      return SQLConvert.time(rs.getString(columnName));
   }

   public Timestamp getTimestamp(String columnName) throws SQLException
   {
      return SQLConvert.timestamp(rs.getTimestamp(columnName));
   }

   public SQLWarning getWarnings() throws SQLException
   {
      return rs.getWarnings();
   }

   public void clearWarnings() throws SQLException
   {
      rs.clearWarnings();
   }

   public String getCursorName() throws SQLException
   {
      return rs.getCursorName();
   }

   SQLite4JResultSetMetaData rsmd;
   public ResultSetMetaData getMetaData() throws SQLException
   {
      return rsmd == null ? rsmd = new SQLite4JResultSetMetaData(rs.getMetaData()) : rsmd;
   }

   public Object getObject(int columnIndex) throws SQLException
   {
      return rs.getObject(columnIndex);
   }

   public Object getObject(String columnName) throws SQLException
   {
      return rs.getObject(columnName);
   }

   public int findColumn(String columnName) throws SQLException
   {
      return rs.findColumn(columnName);
   }

   public BigDecimal getBigDecimal(int columnIndex) throws SQLException
   {
      return SQLConvert.bigdecimal(rs.getBigDecimal(columnIndex));
   }

   public BigDecimal getBigDecimal(String columnName) throws SQLException
   {
      return SQLConvert.bigdecimal(rs.getBigDecimal(columnName));
   }

   public boolean isBeforeFirst() throws SQLException
   {
      return rs.isBeforeFirst();
   }

   public boolean isAfterLast() throws SQLException
   {
      return rs.isAfterLast();
   }

   public boolean isFirst() throws SQLException
   {
      return rs.isFirst();
   }

   public boolean isLast() throws SQLException
   {
      return rs.isLast();
   }

   public void beforeFirst() throws SQLException
   {
      rs.beforeFirst();
   }

   public void afterLast() throws SQLException
   {
      rs.afterLast();
   }

   public boolean first() throws SQLException
   {
      return rs.first();
   }

   public boolean last() throws SQLException
   {
      return rs.last();
   }

   public int getRow() throws SQLException
   {
      return rs.getRow();
   }

   public boolean absolute(int row) throws SQLException
   {
      return rs.absolute(row);
   }

   public boolean relative(int rows) throws SQLException
   {
      return rs.relative(rows);
   }

   public boolean previous() throws SQLException
   {
      return rs.previous();
   }

   public void setFetchDirection(int direction) throws SQLException
   {
      rs.setFetchDirection(direction);
   }

   public int getFetchDirection() throws SQLException
   {
      return rs.getFetchDirection();
   }

   public void setFetchSize(int rows) throws SQLException
   {
      rs.setFetchSize(rows);
   }

   public int getFetchSize() throws SQLException
   {
      return rs.getFetchSize();
   }

   public int getType() throws SQLException
   {
      return rs.getType();
   }

   public int getConcurrency() throws SQLException
   {
      return rs.getConcurrency();
   }

   SQLite4JStatement tcstat;
   public Statement getStatement() throws SQLException
   {
      return tcstat != null ? tcstat : (tcstat=new SQLite4JStatement(rs.getStatement()));
   }
}
