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
import totalcross.sql.ResultSetMetaData;

public class SQLite4JResultSetMetaData implements ResultSetMetaData {
  java.sql.ResultSetMetaData meta;

  public SQLite4JResultSetMetaData(java.sql.ResultSetMetaData metaData) {
    this.meta = metaData;
  }

  @Override
  public int getColumnCount() throws SQLException {
    return meta.getColumnCount();
  }

  @Override
  public boolean isAutoIncrement(int column) throws SQLException {
    return meta.isAutoIncrement(column);
  }

  @Override
  public boolean isCaseSensitive(int column) throws SQLException {
    return meta.isCaseSensitive(column);
  }

  @Override
  public boolean isSearchable(int column) throws SQLException {
    return meta.isSearchable(column);
  }

  @Override
  public boolean isCurrency(int column) throws SQLException {
    return meta.isCurrency(column);
  }

  @Override
  public int isNullable(int column) throws SQLException {
    return meta.isNullable(column);
  }

  @Override
  public boolean isSigned(int column) throws SQLException {
    return meta.isSigned(column);
  }

  @Override
  public int getColumnDisplaySize(int column) throws SQLException {
    return meta.getColumnDisplaySize(column);
  }

  @Override
  public String getColumnLabel(int column) throws SQLException {
    return meta.getColumnLabel(column);
  }

  @Override
  public String getColumnName(int column) throws SQLException {
    return meta.getColumnName(column);
  }

  @Override
  public String getSchemaName(int column) throws SQLException {
    return meta.getSchemaName(column);
  }

  @Override
  public int getPrecision(int column) throws SQLException {
    return meta.getPrecision(column);
  }

  @Override
  public int getScale(int column) throws SQLException {
    return meta.getScale(column);
  }

  @Override
  public String getTableName(int column) throws SQLException {
    return meta.getTableName(column);
  }

  @Override
  public String getCatalogName(int column) throws SQLException {
    return meta.getCatalogName(column);
  }

  @Override
  public int getColumnType(int column) throws SQLException {
    return meta.getColumnType(column);
  }

  @Override
  public String getColumnTypeName(int column) throws SQLException {
    return meta.getColumnTypeName(column);
  }

  @Override
  public boolean isReadOnly(int column) throws SQLException {
    return meta.isReadOnly(column);
  }

  @Override
  public boolean isWritable(int column) throws SQLException {
    return meta.isWritable(column);
  }

  @Override
  public boolean isDefinitelyWritable(int column) throws SQLException {
    return meta.isDefinitelyWritable(column);
  }

  @Override
  public String getColumnClassName(int column) throws SQLException {
    return meta.getColumnClassName(column);
  }
}
