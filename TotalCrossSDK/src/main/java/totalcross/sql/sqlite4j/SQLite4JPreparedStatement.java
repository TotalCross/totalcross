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

package totalcross.sql.sqlite4j;

import java.sql.SQLException;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.ResultSetMetaData;
import totalcross.sql.Timestamp;
import totalcross.sys.Time;
import totalcross.util.BigDecimal;
import totalcross.util.Date;

public class SQLite4JPreparedStatement extends SQLite4JStatement implements PreparedStatement {
  java.sql.PreparedStatement ps;

  public SQLite4JPreparedStatement(java.sql.PreparedStatement ps) {
    super(ps);
    this.ps = ps;
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    return new SQLite4JResultSet(ps.executeQuery());
  }

  @Override
  public int executeUpdate() throws SQLException {
    return ps.executeUpdate();
  }

  @Override
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    ps.setNull(parameterIndex, sqlType);
  }

  @Override
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    ps.setBoolean(parameterIndex, x);
  }

  @Override
  public void setByte(int parameterIndex, byte x) throws SQLException {
    ps.setByte(parameterIndex, x);
  }

  @Override
  public void setShort(int parameterIndex, short x) throws SQLException {
    ps.setShort(parameterIndex, x);
  }

  @Override
  public void setInt(int parameterIndex, int x) throws SQLException {
    ps.setInt(parameterIndex, x);
  }

  @Override
  public void setLong(int parameterIndex, long x) throws SQLException {
    ps.setLong(parameterIndex, x);
  }

  @Override
  public void setDouble(int parameterIndex, double x) throws SQLException {
    ps.setDouble(parameterIndex, x);
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    ps.setBigDecimal(parameterIndex, SQLConvert.bigdecimal(x));
  }

  @Override
  public void setString(int parameterIndex, String x) throws SQLException {
    ps.setString(parameterIndex, x);
  }

  @Override
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    ps.setBytes(parameterIndex, x);
  }

  @Override
  public void setDate(int parameterIndex, Date x) throws SQLException {
    ps.setString(parameterIndex, x == null ? null : x.getSQLString()); // ps.setDate(parameterIndex,SQLConvert.date(x));
  }

  @Override
  public void setTime(int parameterIndex, Time x) throws SQLException {
    ps.setString(parameterIndex, x == null ? null : x.getSQLString()); // ps.setTime(parameterIndex,SQLConvert.time(x));
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    ps.setTimestamp(parameterIndex, SQLConvert.timestamp(x));
  }

  @Override
  public void clearParameters() throws SQLException {
    ps.clearParameters();
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
    ps.setObject(parameterIndex, x, targetSqlType, scale);
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    ps.setObject(parameterIndex, x, targetSqlType);
  }

  @Override
  public void setObject(int parameterIndex, Object x) throws SQLException {
    ps.setObject(parameterIndex, x);
  }

  @Override
  public boolean execute() throws SQLException {
    return ps.execute();
  }

  @Override
  public void addBatch() throws SQLException {
    ps.addBatch();
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return new SQLite4JResultSetMetaData(ps.getMetaData());
  }

  @Override
  public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
    ps.setNull(paramIndex, sqlType, typeName);
  }
}
