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
import java.sql.SQLWarning;
import totalcross.sql.ResultSet;
import totalcross.sql.ResultSetMetaData;
import totalcross.sql.Statement;
import totalcross.sql.Timestamp;
import totalcross.sys.Time;
import totalcross.util.BigDecimal;
import totalcross.util.Date;

public class SQLite4JResultSet implements ResultSet {
  java.sql.ResultSet rs;

  public SQLite4JResultSet(java.sql.ResultSet rs) {
    this.rs = rs;
  }

  @Override
  public boolean next() throws SQLException {
    return rs.next();
  }

  @Override
  public void close() throws SQLException {
    rs.close();
  }

  @Override
  public boolean wasNull() throws SQLException {
    return rs.wasNull();
  }

  @Override
  public String getString(int columnIndex) throws SQLException {
    return rs.getString(columnIndex);
  }

  @Override
  public boolean getBoolean(int columnIndex) throws SQLException {
    return rs.getBoolean(columnIndex);
  }

  @Override
  public byte getByte(int columnIndex) throws SQLException {
    return rs.getByte(columnIndex);
  }

  @Override
  public short getShort(int columnIndex) throws SQLException {
    return rs.getShort(columnIndex);
  }

  @Override
  public int getInt(int columnIndex) throws SQLException {
    return rs.getInt(columnIndex);
  }

  @Override
  public long getLong(int columnIndex) throws SQLException {
    return rs.getLong(columnIndex);
  }

  public double getFloat(int columnIndex) throws SQLException {
    return rs.getFloat(columnIndex);
  }

  @Override
  public double getDouble(int columnIndex) throws SQLException {
    return rs.getDouble(columnIndex);
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    return SQLConvert.bigdecimal(rs.getBigDecimal(columnIndex));
  }

  @Override
  public byte[] getBytes(int columnIndex) throws SQLException {
    return rs.getBytes(columnIndex);
  }

  @Override
  public Date getDate(int columnIndex) throws SQLException {
    java.sql.Date dd = rs.getDate(columnIndex);
    return SQLConvert.date(dd);
  }

  @Override
  public Time getTime(int columnIndex) throws SQLException {
    return SQLConvert.time(rs.getString(columnIndex));
  }

  @Override
  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    return SQLConvert.timestamp(rs.getTimestamp(columnIndex));
  }

  @Override
  public String getString(String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  @Override
  public boolean getBoolean(String columnName) throws SQLException {
    return rs.getBoolean(columnName);
  }

  @Override
  public byte getByte(String columnName) throws SQLException {
    return rs.getByte(columnName);
  }

  @Override
  public short getShort(String columnName) throws SQLException {
    return rs.getShort(columnName);
  }

  @Override
  public int getInt(String columnName) throws SQLException {
    return rs.getInt(columnName);
  }

  @Override
  public long getLong(String columnName) throws SQLException {
    return rs.getLong(columnName);
  }

  public double getFloat(String columnName) throws SQLException {
    return rs.getFloat(columnName);
  }

  @Override
  public double getDouble(String columnName) throws SQLException {
    return rs.getDouble(columnName);
  }

  @Override
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
    return SQLConvert.bigdecimal(rs.getBigDecimal(columnName));
  }

  @Override
  public byte[] getBytes(String columnName) throws SQLException {
    return rs.getBytes(columnName);
  }

  @Override
  public Date getDate(String columnName) throws SQLException {
    return SQLConvert.date(rs.getDate(columnName));
  }

  @Override
  public Time getTime(String columnName) throws SQLException {
    return SQLConvert.time(rs.getString(columnName));
  }

  @Override
  public Timestamp getTimestamp(String columnName) throws SQLException {
    return SQLConvert.timestamp(rs.getTimestamp(columnName));
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return rs.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    rs.clearWarnings();
  }

  @Override
  public String getCursorName() throws SQLException {
    return rs.getCursorName();
  }

  SQLite4JResultSetMetaData rsmd;

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return rsmd == null ? rsmd = new SQLite4JResultSetMetaData(rs.getMetaData()) : rsmd;
  }

  @Override
  public Object getObject(int columnIndex) throws SQLException {
    return rs.getObject(columnIndex);
  }

  @Override
  public Object getObject(String columnName) throws SQLException {
    return rs.getObject(columnName);
  }

  @Override
  public int findColumn(String columnName) throws SQLException {
    return rs.findColumn(columnName);
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    return SQLConvert.bigdecimal(rs.getBigDecimal(columnIndex));
  }

  @Override
  public BigDecimal getBigDecimal(String columnName) throws SQLException {
    return SQLConvert.bigdecimal(rs.getBigDecimal(columnName));
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    return rs.isBeforeFirst();
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    return rs.isAfterLast();
  }

  @Override
  public boolean isFirst() throws SQLException {
    return rs.isFirst();
  }

  @Override
  public boolean isLast() throws SQLException {
    return rs.isLast();
  }

  @Override
  public void beforeFirst() throws SQLException {
    rs.beforeFirst();
  }

  @Override
  public void afterLast() throws SQLException {
    rs.afterLast();
  }

  @Override
  public boolean first() throws SQLException {
    return rs.first();
  }

  @Override
  public boolean last() throws SQLException {
    return rs.last();
  }

  @Override
  public int getRow() throws SQLException {
    return rs.getRow();
  }

  @Override
  public boolean absolute(int row) throws SQLException {
    return rs.absolute(row);
  }

  @Override
  public boolean relative(int rows) throws SQLException {
    return rs.relative(rows);
  }

  @Override
  public boolean previous() throws SQLException {
    return rs.previous();
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    rs.setFetchDirection(direction);
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return rs.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    rs.setFetchSize(rows);
  }

  @Override
  public int getFetchSize() throws SQLException {
    return rs.getFetchSize();
  }

  @Override
  public int getType() throws SQLException {
    return rs.getType();
  }

  @Override
  public int getConcurrency() throws SQLException {
    return rs.getConcurrency();
  }

  SQLite4JStatement tcstat;

  @Override
  public Statement getStatement() throws SQLException {
    return tcstat != null ? tcstat : (tcstat = new SQLite4JStatement(rs.getStatement()));
  }
}
