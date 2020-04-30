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
import totalcross.sql.Connection;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;

public class SQLite4JStatement implements Statement {
  java.sql.Statement stat;

  public SQLite4JStatement(java.sql.Statement stat) {
    this.stat = stat;
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    return new SQLite4JResultSet(stat.executeQuery(sql));
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    return stat.executeUpdate(sql);
  }

  @Override
  public void close() throws SQLException {
    stat.close();
  }

  @Override
  public int getMaxRows() throws SQLException {
    return stat.getMaxRows();
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    stat.setMaxRows(max);
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return stat.getQueryTimeout();
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    stat.setQueryTimeout(seconds);
  }

  @Override
  public void cancel() throws SQLException {
    stat.cancel();
  }

  @Override
  public java.sql.SQLWarning getWarnings() throws SQLException {
    return stat.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    stat.clearWarnings();
  }

  @Override
  public void setCursorName(String name) throws SQLException {
    stat.setCursorName(name);
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    return stat.execute(sql);
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    java.sql.ResultSet rs = stat.getResultSet();
    return rs == null ? null : new SQLite4JResultSet(rs);
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return stat.getUpdateCount();
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return stat.getMoreResults();
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    stat.setFetchDirection(direction);
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return stat.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    stat.setFetchSize(rows);
  }

  @Override
  public int getFetchSize() throws SQLException {
    return stat.getFetchSize();
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return stat.getResultSetConcurrency();
  }

  @Override
  public int getResultSetType() throws SQLException {
    return stat.getResultSetType();
  }

  @Override
  public void addBatch(String sql) throws SQLException {
    stat.addBatch(sql);
  }

  @Override
  public void clearBatch() throws SQLException {
    stat.clearBatch();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    return stat.executeBatch();
  }

  @Override
  public Connection getConnection() throws SQLException {
    return new SQLite4JConnection((org.sqlite.SQLiteConnection) stat.getConnection());
  }
}
