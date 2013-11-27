package totalcross.sql.sqlite4d;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.util.*;

import java.sql.SQLException;

public class SQLite4DPreparedStatement extends SQLite4DStatement implements PreparedStatement
{
   public SQLite4DPreparedStatement()
   {
      nativeCreate();
   }
   
   native void nativeCreate();
   native public ResultSet executeQuery() throws SQLException;
   native public int executeUpdate() throws SQLException;
   native public void setNull(int parameterIndex, int sqlType) throws SQLException;
   native public void setBoolean(int parameterIndex, boolean x) throws SQLException;
   native public void setByte(int parameterIndex, byte x) throws SQLException;
   native public void setShort(int parameterIndex, short x) throws SQLException;
   native public void setInt(int parameterIndex, int x) throws SQLException;
   native public void setLong(int parameterIndex, long x) throws SQLException;
   native public void setFloat(int parameterIndex, double x) throws SQLException;
   native public void setDouble(int parameterIndex, double x) throws SQLException;
   native public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException;
   native public void setString(int parameterIndex, String x) throws SQLException;
   native public void setBytes(int parameterIndex, byte[] x) throws SQLException;
   native public void setDate(int parameterIndex, Date x) throws SQLException;
   native public void setTime(int parameterIndex, Time x) throws SQLException;
   native public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException;
   native public void clearParameters() throws SQLException;
   native public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException;
   native public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException;
   native public void setObject(int parameterIndex, Object x) throws SQLException;
   native public boolean execute() throws SQLException;
   native public void addBatch() throws SQLException;
   native public ResultSetMetaData getMetaData() throws SQLException;
   native public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException;
}
