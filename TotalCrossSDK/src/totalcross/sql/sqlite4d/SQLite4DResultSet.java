package totalcross.sql.sqlite4d;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.util.*;
import totalcross.util.Date;
import java.sql.SQLException;

public class SQLite4DResultSet implements ResultSet
{
   public SQLite4DResultSet()
   {
      nativeCreate();
   }
   
   native void nativeCreate();
   native public boolean next() throws SQLException;
   native public void close() throws SQLException;
   native public boolean wasNull() throws SQLException;
   native public String getString(int columnIndex) throws SQLException;
   native public boolean getBoolean(int columnIndex) throws SQLException;
   native public byte getByte(int columnIndex) throws SQLException;
   native public short getShort(int columnIndex) throws SQLException;
   native public int getInt(int columnIndex) throws SQLException;
   native public long getLong(int columnIndex) throws SQLException;
   native public double getFloat(int columnIndex) throws SQLException;
   native public double getDouble(int columnIndex) throws SQLException;
   native public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException;
   native public byte[] getBytes(int columnIndex) throws SQLException;
   native public Date getDate(int columnIndex) throws SQLException;
   native public Time getTime(int columnIndex) throws SQLException;
   native public Timestamp getTimestamp(int columnIndex) throws SQLException;
   native public String getString(String columnName) throws SQLException;
   native public boolean getBoolean(String columnName) throws SQLException;
   native public byte getByte(String columnName) throws SQLException;
   native public short getShort(String columnName) throws SQLException;
   native public int getInt(String columnName) throws SQLException;
   native public long getLong(String columnName) throws SQLException;
   native public double getFloat(String columnName) throws SQLException;
   native public double getDouble(String columnName) throws SQLException;
   native public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException;
   native public byte[] getBytes(String columnName) throws SQLException;
   native public Date getDate(String columnName) throws SQLException;
   native public Time getTime(String columnName) throws SQLException;
   native public Timestamp getTimestamp(String columnName) throws SQLException;
   native public SQLException getWarnings() throws SQLException;
   native public void clearWarnings() throws SQLException;
   native public String getCursorName() throws SQLException;
   native public Object getObject(int columnIndex) throws SQLException;
   native public Object getObject(String columnName) throws SQLException;
   native public int findColumn(String columnName) throws SQLException;
   native public BigDecimal getBigDecimal(int columnIndex) throws SQLException;
   native public BigDecimal getBigDecimal(String columnName) throws SQLException;
   native public boolean isBeforeFirst() throws SQLException;
   native public boolean isAfterLast() throws SQLException;
   native public boolean isFirst() throws SQLException;
   native public boolean isLast() throws SQLException;
   native public void beforeFirst() throws SQLException;
   native public void afterLast() throws SQLException;
   native public boolean first() throws SQLException;
   native public boolean last() throws SQLException;
   native public int getRow() throws SQLException;
   native public boolean absolute(int row) throws SQLException;
   native public boolean relative(int rows) throws SQLException;
   native public boolean previous() throws SQLException;
   native public void setFetchDirection(int direction) throws SQLException;
   native public int getFetchDirection() throws SQLException;
   native public void setFetchSize(int rows) throws SQLException;
   native public int getFetchSize() throws SQLException;
   native public int getType() throws SQLException;
   native public int getConcurrency() throws SQLException;

   SQLite4DResultSetMetaData rsmd;
   SQLite4DStatement tcstat;

   public ResultSetMetaData getMetaData() throws SQLException
   {
      return rsmd == null ? rsmd = new SQLite4DResultSetMetaData() : rsmd;
   }
   public Statement getStatement() throws SQLException
   {
      return tcstat != null ? tcstat : (tcstat=new SQLite4DStatement());
   }
}
