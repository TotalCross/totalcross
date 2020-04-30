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

package totalcross.sql;

import java.sql.SQLException;
import totalcross.sys.Time;
import totalcross.util.BigDecimal;
import totalcross.util.Date;

public interface PreparedStatement extends Statement {
  public ResultSet executeQuery() throws SQLException;

  public int executeUpdate() throws SQLException;

  public void setNull(int parameterIndex, int sqlType) throws SQLException;

  public void setBoolean(int parameterIndex, boolean x) throws SQLException;

  public void setByte(int parameterIndex, byte x) throws SQLException;

  public void setShort(int parameterIndex, short x) throws SQLException;

  public void setInt(int parameterIndex, int x) throws SQLException;

  public void setLong(int parameterIndex, long x) throws SQLException;

  public void setDouble(int parameterIndex, double x) throws SQLException;

  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException;

  public void setString(int parameterIndex, String x) throws SQLException;

  public void setBytes(int parameterIndex, byte[] x) throws SQLException;

  public void setDate(int parameterIndex, Date x) throws SQLException;

  public void setTime(int parameterIndex, Time x) throws SQLException;

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException;

  public void clearParameters() throws SQLException;

  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException;

  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException;

  public void setObject(int parameterIndex, Object x) throws SQLException;

  public boolean execute() throws SQLException;

  public void addBatch() throws SQLException;

  public ResultSetMetaData getMetaData() throws SQLException;

  public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException;
}
