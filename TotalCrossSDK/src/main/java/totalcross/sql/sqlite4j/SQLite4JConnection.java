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
import totalcross.sql.Connection;
import totalcross.sql.DatabaseMetaData;
import totalcross.sql.PreparedStatement;
import totalcross.sql.Statement;

public class SQLite4JConnection implements Connection {
  public org.sqlite.SQLiteConnection con;

  public SQLite4JConnection(org.sqlite.SQLiteConnection con) {
    this.con = con;
  }

  @Override
  public Statement createStatement() throws SQLException {
    return new SQLite4JStatement(con.createStatement());
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return new SQLite4JPreparedStatement(con.prepareStatement(sql));
  }

  @Override
  public String nativeSQL(String sql) {
    return con.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    con.setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return con.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    con.commit();
  }

  @Override
  public void rollback() throws SQLException {
    con.rollback();
  }

  @Override
  public void close() throws SQLException {
    con.close();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return con.isClosed();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    con.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return con.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    con.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return con.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    con.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return con.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return con.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    con.clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    return new SQLite4JStatement(con.createStatement(resultSetType, resultSetConcurrency));
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return new SQLite4JPreparedStatement(con.prepareStatement(sql, resultSetType, resultSetConcurrency));
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return new SQLite4JDatabaseMetaData(this, con.getMetaData());
  }
}
