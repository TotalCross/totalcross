/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.   
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

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
