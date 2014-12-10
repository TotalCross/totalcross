package totalcross.sql.sqlite4j;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.util.*;

import java.sql.SQLException;

public class SQLite4JPreparedStatement extends SQLite4JStatement implements PreparedStatement
{
   java.sql.PreparedStatement ps;

   public SQLite4JPreparedStatement(java.sql.PreparedStatement ps)
   {
      super(ps);
      this.ps = ps;
   }

   public ResultSet executeQuery() throws SQLException
   {
      return new SQLite4JResultSet(ps.executeQuery());
   }

   public int executeUpdate() throws SQLException
   {
      return ps.executeUpdate();
   }

   public void setNull(int parameterIndex, int sqlType) throws SQLException
   {
      ps.setNull(parameterIndex, sqlType);
   }

   public void setBoolean(int parameterIndex, boolean x) throws SQLException
   {
      ps.setBoolean(parameterIndex,x);
   }

   public void setByte(int parameterIndex, byte x) throws SQLException
   {
      ps.setByte(parameterIndex,x);
   }

   public void setShort(int parameterIndex, short x) throws SQLException
   {
      ps.setShort(parameterIndex,x);
   }

   public void setInt(int parameterIndex, int x) throws SQLException
   {
      ps.setInt(parameterIndex,x);
   }

   public void setLong(int parameterIndex, long x) throws SQLException
   {
      ps.setLong(parameterIndex,x);
   }

   public void setDouble(int parameterIndex, double x) throws SQLException
   {
      ps.setDouble(parameterIndex,x);
   }

   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
   {
      ps.setBigDecimal(parameterIndex, SQLConvert.bigdecimal(x));
   }

   public void setString(int parameterIndex, String x) throws SQLException
   {
      ps.setString(parameterIndex,x);
   }

   public void setBytes(int parameterIndex, byte[] x) throws SQLException
   {
      ps.setBytes(parameterIndex,x);
   }

   public void setDate(int parameterIndex, Date x) throws SQLException
   {
      ps.setString(parameterIndex, x == null ? null : x.getSQLString()); // ps.setDate(parameterIndex,SQLConvert.date(x));
   }

   public void setTime(int parameterIndex, Time x) throws SQLException
   {
      ps.setString(parameterIndex, x == null ? null : x.getSQLString()); // ps.setTime(parameterIndex,SQLConvert.time(x));
   }

   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
   {
      ps.setTimestamp(parameterIndex,SQLConvert.timestamp(x));
   }

   public void clearParameters() throws SQLException
   {
      ps.clearParameters();
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException
   {
      ps.setObject(parameterIndex,x, targetSqlType, scale);
   }

   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
   {
      ps.setObject(parameterIndex,x, targetSqlType);
   }

   public void setObject(int parameterIndex, Object x) throws SQLException
   {
      ps.setObject(parameterIndex,x);
   }

   public boolean execute() throws SQLException
   {
      return ps.execute();
   }

   public void addBatch() throws SQLException
   {
      ps.addBatch();
   }

   public ResultSetMetaData getMetaData() throws SQLException
   {
      return new SQLite4JResultSetMetaData(ps.getMetaData());
   }

   public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException
   {
      ps.setNull(paramIndex, sqlType, typeName);
   }   
}
