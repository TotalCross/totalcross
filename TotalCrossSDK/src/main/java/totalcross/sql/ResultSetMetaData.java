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

public interface ResultSetMetaData {
  public static final int columnNoNulls = 0;
  public static final int columnNullable = 1;
  public static final int columnNullableUnknown = 2;

  public int getColumnCount() throws SQLException;

  public boolean isAutoIncrement(int column) throws SQLException;

  public boolean isCaseSensitive(int column) throws SQLException;

  public boolean isSearchable(int column) throws SQLException;

  public boolean isCurrency(int column) throws SQLException;

  public int isNullable(int column) throws SQLException;

  public boolean isSigned(int column) throws SQLException;

  public int getColumnDisplaySize(int column) throws SQLException;

  public String getColumnLabel(int column) throws SQLException;

  public String getColumnName(int column) throws SQLException;

  public String getSchemaName(int column) throws SQLException;

  public int getPrecision(int column) throws SQLException;

  public int getScale(int column) throws SQLException;

  public String getTableName(int column) throws SQLException;

  public String getCatalogName(int column) throws SQLException;

  public int getColumnType(int column) throws SQLException;

  public String getColumnTypeName(int column) throws SQLException;

  public boolean isReadOnly(int column) throws SQLException;

  public boolean isWritable(int column) throws SQLException;

  public boolean isDefinitelyWritable(int column) throws SQLException;

  public String getColumnClassName(int column) throws SQLException;
}
