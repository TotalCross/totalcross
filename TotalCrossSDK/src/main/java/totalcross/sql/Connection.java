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

public interface Connection extends AutoCloseable {
  public static final int TRANSACTION_NONE = 0;
  public static final int TRANSACTION_READ_UNCOMMITTED = 1;
  public static final int TRANSACTION_READ_COMMITTED = 2;
  public static final int TRANSACTION_REPEATABLE_READ = 4;
  public static final int TRANSACTION_SERIALIZABLE = 8;

  public Statement createStatement() throws SQLException;

  public PreparedStatement prepareStatement(String sql) throws SQLException;

  public String nativeSQL(String sql);

  public void setAutoCommit(boolean autoCommit) throws SQLException;

  public boolean getAutoCommit() throws SQLException;

  public void commit() throws SQLException;

  public void rollback() throws SQLException;

  public void close() throws SQLException;

  public boolean isClosed() throws SQLException;

  public void setReadOnly(boolean readOnly) throws SQLException;

  public boolean isReadOnly() throws SQLException;

  public void setCatalog(String catalog) throws SQLException;

  public String getCatalog() throws SQLException;

  public void setTransactionIsolation(int level) throws SQLException;

  public int getTransactionIsolation() throws SQLException;

  public SQLWarning getWarnings() throws SQLException;

  public void clearWarnings() throws SQLException;

  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException;

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException;

  public DatabaseMetaData getMetaData() throws SQLException;
}
