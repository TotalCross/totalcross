/*
 * Copyright (c) 2007 David Crawshaw <david@zentus.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package totalcross.db.sqlite;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import totalcross.db.sqlite.DB.ProgressObserver;
import totalcross.db.sqlite.ExtendedCommand.SQLExtension;
import totalcross.sql.Connection;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.Vm;

class Stmt extends Unused implements Statement, Codes {
  final SQLiteConnection conn;
  final DB db;
  final RS rs;

  private MetaData metadata;

  long pointer;
  String sql;

  int batchPos;
  Object[] batch;
  boolean resultsWaiting;

  Stmt(SQLiteConnection c) {
    conn = c;
    db = conn.db();
    rs = new RS(this);
  }

  /**
   * @throws SQLException If the database is not opened.
   */
  protected final void checkOpen() throws SQLException {
    if (pointer == 0) {
      throw new SQLException("statement is not executing");
    }
  }

  /**
   * @return True if the database is opened; false otherwise.
   * @throws SQLException
   */
  boolean isOpen() throws SQLException {
    return (pointer != 0);
  }

  /**
   * Calls sqlite3_step() and sets up results. Expects a clean stmt.
   * @return True if the ResultSet has at least one row; false otherwise. 
   * @throws SQLException If the given SQL statement is null or no database is open.
   */
  protected boolean exec() throws SQLException {
    if (sql == null) {
      throw new SQLException("SQLiteJDBC internal error: sql==null");
    }
    if (rs.isOpen()) {
      throw new SQLException("SQLite JDBC internal error: rs.isOpen() on exec.");
    }

    boolean rc = false;
    try {
      rc = db.execute(this, null);
    } finally {
      resultsWaiting = rc;
    }

    return db.column_count(pointer) != 0;
  }

  /**
   * Executes SQL statement and throws SQLExceptions if the given SQL
   * statement is null or no database is open.
   * @param sql SQL statement.
   * @return True if the ResultSet has at least one row; false otherwise. 
   * @throws SQLException If the given SQL statement is null or no database is open.
   */
  protected boolean exec(String sql) throws SQLException {
    if (sql == null) {
      throw new SQLException("SQLiteJDBC internal error: sql==null");
    }
    if (rs.isOpen()) {
      throw new SQLException("SQLite JDBC internal error: rs.isOpen() on exec.");
    }

    boolean rc = false;
    try {
      rc = db.execute(sql);
    } finally {
      resultsWaiting = rc;
    }

    return db.column_count(pointer) != 0;
  }

  protected void internalClose() throws SQLException {
    if (pointer == 0 || db.conn.isClosed()) {
      return; //throw DB.newSQLException(SQLITE_ERROR, "Connection is closed");
    }

    rs.close();
    batch = null;
    batchPos = 0;
    /*int resp = */db.finalize(this);
    pointer = 0;
    //        if (resp != SQLITE_OK && resp != SQLITE_MISUSE)
    //            db.throwex(resp);
  }

  // PUBLIC INTERFACE /////////////////////////////////////////////

  /**
   * @see java.sql.Statement#close()
   */
  @Override
  public void close() throws SQLException {
    if (metadata != null) {
      metadata.refCount--;
      metadata.close();
      metadata = null;
    }
    internalClose();
  }

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws SQLException {
    if (pointer != 0) {
      close();
    }
  }

  /**
   * @see java.sql.Statement#execute(java.lang.String)
   */
  @Override
  public boolean execute(String sql) throws SQLException {
    internalClose();

    SQLExtension ext = ExtendedCommand.parse(sql);
    if (ext != null) {
      ext.execute(db);

      return false;
    }

    this.sql = sql;

    db.prepare(this);
    return exec();
  }

  /**
   * @param closeStmt Whether to close this statement when the resultset is closed.
   * @see java.sql.Statement#executeQuery(java.lang.String)
   */
  ResultSet executeQuery(String sql, boolean closeStmt) throws SQLException {
    rs.closeStmt = closeStmt;

    return executeQuery(sql);
  }

  /**
   * @see java.sql.Statement#executeQuery(java.lang.String)
   */
  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    internalClose();
    this.sql = sql;

    db.prepare(this);

    if (!exec()) {
      internalClose();
      throw new SQLException("query does not return ResultSet", "SQLITE_DONE", SQLITE_DONE);
    }

    return getResultSet();
  }

  static class BackupObserver implements ProgressObserver {
    @Override
    public void progress(int remaining, int pageCount) {
      totalcross.sys.Vm.debug("remaining:" + remaining + ", page count:" + pageCount);
    }
  }

  /**
   * @see java.sql.Statement#executeUpdate(java.lang.String)
   */
  @Override
  public int executeUpdate(String sql) throws SQLException {
    internalClose();
    this.sql = sql;

    int changes = 0;
    SQLExtension ext = ExtendedCommand.parse(sql);
    if (ext != null) {
      // execute extended command 
      ext.execute(db);
    } else {
      try {
        changes = db.total_changes();

        // directly invokes the exec API to support multiple SQL statements 
        int statusCode = db._exec(sql);
        if (statusCode != SQLITE_OK) {
          throw DB.newSQLException(statusCode, "");
        }

        changes = db.total_changes() - changes;
      } finally {
        internalClose();
      }
    }
    return changes;
  }

  /**
   * @see java.sql.Statement#getResultSet()
   */
  @Override
  public ResultSet getResultSet() throws SQLException {
    checkOpen();

    if (rs.isOpen()) {
      throw new SQLException("ResultSet already requested");
    }

    if (db.column_count(pointer) == 0) {
      return null;
    }

    if (rs.colsMeta == null) {
      rs.colsMeta = db.column_names(pointer);
    }

    rs.setCols(rs.colsMeta);
    rs.open = resultsWaiting;
    resultsWaiting = false;

    return rs;
  }

  /*
   * This function has a complex behaviour best understood by carefully
   * reading the JavaDoc for getMoreResults() and considering the test
   * StatementTest.execute().
   * @see java.sql.Statement#getUpdateCount()
   */
  @Override
  public int getUpdateCount() throws SQLException {
    if (pointer != 0 && !rs.isOpen() && !resultsWaiting && db.column_count(pointer) == 0) {
      return db.changes();
    }
    return -1;
  }

  /**
   * @see java.sql.Statement#addBatch(java.lang.String)
   */
  @Override
  public void addBatch(String sql) throws SQLException {
    internalClose();
    if (batch == null || batchPos + 1 >= batch.length) {
      Object[] nb = new Object[Math.max(10, batchPos * 2)];
      if (batch != null) {
        Vm.arrayCopy(batch, 0, nb, 0, batch.length);
      }
      batch = nb;
    }
    batch[batchPos++] = sql;
  }

  /**
   * @see java.sql.Statement#clearBatch()
   */
  @Override
  public void clearBatch() throws SQLException {
    batchPos = 0;
    if (batch != null) {
      for (int i = 0; i < batch.length; i++) {
        batch[i] = null;
      }
    }
  }

  /**
   * @see java.sql.Statement#executeBatch()
   */
  @Override
  public int[] executeBatch() throws SQLException {
    // TODO: optimize
    internalClose();
    if (batch == null || batchPos == 0) {
      return new int[] {};
    }

    int[] changes = new int[batchPos];

    /*synchronized (db.stmtsLock) */ {
      try {
        for (int i = 0; i < changes.length; i++) {
          try {
            this.sql = (String) batch[i];
            db.prepare(this);
            changes[i] = db.executeUpdate(this, null);
          } catch (SQLException e) {
            throw new BatchUpdateException("batch entry " + i + ": " + e.getMessage(), changes);
          } finally {
            db.finalize(this);
          }
        }
      } finally {
        clearBatch();
      }
    }

    return changes;
  }

  /**
   * @see java.sql.Statement#setCursorName(java.lang.String)
   */
  @Override
  public void setCursorName(String name) {
  }

  /**
   * @see java.sql.Statement#getWarnings()
   */
  @Override
  public SQLWarning getWarnings() throws SQLException {
    return null;
  }

  /**
   * @see java.sql.Statement#clearWarnings()
   */
  @Override
  public void clearWarnings() throws SQLException {
  }

  /**
   * @see java.sql.Statement#getConnection()
   */
  @Override
  public Connection getConnection() throws SQLException {
    return conn;
  }

  /**
   * @see java.sql.Statement#cancel()
   */
  @Override
  public void cancel() throws SQLException {
    db.interrupt();
  }

  /**
   * @see java.sql.Statement#getQueryTimeout()
   */
  @Override
  public int getQueryTimeout() throws SQLException {
    return conn.getBusyTimeout();
  }

  /**
   * @see java.sql.Statement#setQueryTimeout(int)
   */
  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    if (seconds < 0) {
      throw new SQLException("query timeout must be >= 0");
    }
    conn.setBusyTimeout(1000 * seconds);
  }

  // TODO: write test
  /**
   * @see java.sql.Statement#getMaxRows()
   */
  @Override
  public int getMaxRows() throws SQLException {
    //checkOpen();
    return rs.maxRows;
  }

  /**
   * @see java.sql.Statement#setMaxRows(int)
   */
  @Override
  public void setMaxRows(int max) throws SQLException {
    //checkOpen();
    if (max < 0) {
      throw new SQLException("max row count must be >= 0");
    }
    rs.maxRows = max;
  }

  /**
   * @see java.sql.Statement#getMaxFieldSize()
   */
  public int getMaxFieldSize() throws SQLException {
    return 0;
  }

  /**
   * @see java.sql.Statement#setMaxFieldSize(int)
   */
  public void setMaxFieldSize(int max) throws SQLException {
    if (max < 0) {
      throw new SQLException("max field size " + max + " cannot be negative");
    }
  }

  /**
   * @see java.sql.Statement#getFetchSize()
   */
  @Override
  public int getFetchSize() throws SQLException {
    return rs.getFetchSize();
  }

  /**
   * @see java.sql.Statement#setFetchSize(int)
   */
  @Override
  public void setFetchSize(int r) throws SQLException {
    rs.setFetchSize(r);
  }

  /**
   * @see java.sql.Statement#getFetchDirection()
   */
  @Override
  public int getFetchDirection() throws SQLException {
    return rs.getFetchDirection();
  }

  /**
   * @see java.sql.Statement#setFetchDirection(int)
   */
  @Override
  public void setFetchDirection(int d) throws SQLException {
    rs.setFetchDirection(d);
  }

  /**
   * As SQLite's last_insert_rowid() function is DB-specific not statement
   * specific, this function introduces a race condition if the same
   * connection is used by two threads and both insert.
   * @see java.sql.Statement#getGeneratedKeys()
   */
  public ResultSet getGeneratedKeys() throws SQLException {
    if (metadata == null) {
      metadata = (MetaData) conn.getMetaData();
      metadata.refCount++;
    }

    return metadata.getGeneratedKeys();
  }

  /**
   * SQLite does not support multiple results from execute().
   * @see java.sql.Statement#getMoreResults()
   */
  @Override
  public boolean getMoreResults() throws SQLException {
    return getMoreResults(0);
  }

  /**
   * @see java.sql.Statement#getMoreResults(int)
   */
  public boolean getMoreResults(int c) throws SQLException {
    checkOpen();
    internalClose(); // as we never have another result, clean up pointer
    return false;
  }

  /**
   * @see java.sql.Statement#getResultSetConcurrency()
   */
  @Override
  public int getResultSetConcurrency() throws SQLException {
    return ResultSet.CONCUR_READ_ONLY;
  }

  /**
   * @see java.sql.Statement#getResultSetHoldability()
   */
  public int getResultSetHoldability() throws SQLException {
    return ResultSet.CLOSE_CURSORS_AT_COMMIT;
  }

  /**
   * @see java.sql.Statement#getResultSetType()
   */
  @Override
  public int getResultSetType() throws SQLException {
    return ResultSet.TYPE_FORWARD_ONLY;
  }

  /**
   * @see java.sql.Statement#setEscapeProcessing(boolean)
   */
  public void setEscapeProcessing(boolean enable) throws SQLException {
    if (enable) {
      throw unused();
    }
  }

}
