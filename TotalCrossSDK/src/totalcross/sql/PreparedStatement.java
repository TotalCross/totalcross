package totalcross.sql;

import totalcross.sys.*;
import totalcross.util.*;

public interface PreparedStatement extends Statement
{
   public ResultSet executeQuery() throws SQLWarning;

   public int executeUpdate() throws SQLWarning;

   public void setNull(int parameterIndex, int sqlType) throws SQLWarning;

   public void setBoolean(int parameterIndex, boolean x) throws SQLWarning;

   public void setByte(int parameterIndex, byte x) throws SQLWarning;

   public void setShort(int parameterIndex, short x) throws SQLWarning;

   public void setInt(int parameterIndex, int x) throws SQLWarning;

   public void setLong(int parameterIndex, long x) throws SQLWarning;

   public void setFloat(int parameterIndex, double x) throws SQLWarning;

   public void setDouble(int parameterIndex, double x) throws SQLWarning;

   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLWarning;

   public void setString(int parameterIndex, String x) throws SQLWarning;

   public void setBytes(int parameterIndex, byte[] x) throws SQLWarning;

   public void setDate(int parameterIndex, Date x) throws SQLWarning;

   public void setTime(int parameterIndex, Time x) throws SQLWarning;

   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLWarning;

   public void clearParameters() throws SQLWarning;

   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLWarning;

   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLWarning;

   public void setObject(int parameterIndex, Object x) throws SQLWarning;

   public boolean execute() throws SQLWarning;

   public void addBatch() throws SQLWarning;

   public void setBlob(int i, Blob x) throws SQLWarning;

   public ResultSetMetaData getMetaData() throws SQLWarning;

   public void setNull(int paramIndex, int sqlType, String typeName) throws SQLWarning;
}
