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

public interface Statement extends AutoCloseable {
  public ResultSet executeQuery(String sql) throws SQLException;

  public int executeUpdate(String sql) throws SQLException;

  @Override
  public void close() throws SQLException;

  public int getMaxRows() throws SQLException;

  public void setMaxRows(int max) throws SQLException;

  public int getQueryTimeout() throws SQLException;

  public void setQueryTimeout(int seconds) throws SQLException;

  public void cancel() throws SQLException;

  public SQLWarning getWarnings() throws SQLException;

  public void clearWarnings() throws SQLException;

  public void setCursorName(String name) throws SQLException;

  public boolean execute(String sql) throws SQLException;

  public ResultSet getResultSet() throws SQLException;

  public int getUpdateCount() throws SQLException;

  public boolean getMoreResults() throws SQLException;

  public void setFetchDirection(int direction) throws SQLException;

  public int getFetchDirection() throws SQLException;

  public void setFetchSize(int rows) throws SQLException;

  public int getFetchSize() throws SQLException;

  public int getResultSetConcurrency() throws SQLException;

  public int getResultSetType() throws SQLException;

  public void addBatch(String sql) throws SQLException;

  public void clearBatch() throws SQLException;

  public int[] executeBatch() throws SQLException;

  public Connection getConnection() throws SQLException;
}
