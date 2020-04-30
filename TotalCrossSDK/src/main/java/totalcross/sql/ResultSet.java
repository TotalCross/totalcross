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
import java.sql.SQLWarning;
import totalcross.sys.Time;
import totalcross.util.BigDecimal;
import totalcross.util.Date;

public interface ResultSet extends AutoCloseable {
  public static final int FETCH_FORWARD = 1000;
  public static final int FETCH_REVERSE = 1001;
  public static final int FETCH_UNKNOWN = 1002;
  public static final int TYPE_FORWARD_ONLY = 1003;
  public static final int TYPE_SCROLL_INSENSITIVE = 1004;
  public static final int TYPE_SCROLL_SENSITIVE = 1005;
  public static final int CONCUR_READ_ONLY = 1007;
  public static final int CONCUR_UPDATABLE = 1008;
  public static final int HOLD_CURSORS_OVER_COMMIT = 1;
  public static final int CLOSE_CURSORS_AT_COMMIT = 2;

  public boolean next() throws SQLException;

  @Override
  public void close() throws SQLException;

  public boolean wasNull() throws SQLException;

  public String getString(int columnIndex) throws SQLException;

  public boolean getBoolean(int columnIndex) throws SQLException;

  public byte getByte(int columnIndex) throws SQLException;

  public short getShort(int columnIndex) throws SQLException;

  public int getInt(int columnIndex) throws SQLException;

  public long getLong(int columnIndex) throws SQLException;

  public double getDouble(int columnIndex) throws SQLException;

  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException;

  public byte[] getBytes(int columnIndex) throws SQLException;

  public Date getDate(int columnIndex) throws SQLException;

  public Time getTime(int columnIndex) throws SQLException;

  public Timestamp getTimestamp(int columnIndex) throws SQLException;

  public String getString(String columnName) throws SQLException;

  public boolean getBoolean(String columnName) throws SQLException;

  public byte getByte(String columnName) throws SQLException;

  public short getShort(String columnName) throws SQLException;

  public int getInt(String columnName) throws SQLException;

  public long getLong(String columnName) throws SQLException;

  public double getDouble(String columnName) throws SQLException;

  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException;

  public byte[] getBytes(String columnName) throws SQLException;

  public Date getDate(String columnName) throws SQLException;

  public Time getTime(String columnName) throws SQLException;

  public Timestamp getTimestamp(String columnName) throws SQLException;

  public SQLWarning getWarnings() throws SQLException;

  public void clearWarnings() throws SQLException;

  public String getCursorName() throws SQLException;

  public ResultSetMetaData getMetaData() throws SQLException;

  public Object getObject(int columnIndex) throws SQLException;

  public Object getObject(String columnName) throws SQLException;

  public int findColumn(String columnName) throws SQLException;

  public BigDecimal getBigDecimal(int columnIndex) throws SQLException;

  public BigDecimal getBigDecimal(String columnName) throws SQLException;

  public boolean isBeforeFirst() throws SQLException;

  public boolean isAfterLast() throws SQLException;

  public boolean isFirst() throws SQLException;

  public boolean isLast() throws SQLException;

  public void beforeFirst() throws SQLException;

  public void afterLast() throws SQLException;

  public boolean first() throws SQLException;

  public boolean last() throws SQLException;

  public int getRow() throws SQLException;

  public boolean absolute(int row) throws SQLException;

  public boolean relative(int rows) throws SQLException;

  public boolean previous() throws SQLException;

  public void setFetchDirection(int direction) throws SQLException;

  public int getFetchDirection() throws SQLException;

  public void setFetchSize(int rows) throws SQLException;

  public int getFetchSize() throws SQLException;

  public int getType() throws SQLException;

  public int getConcurrency() throws SQLException;

  public Statement getStatement() throws SQLException;
}
