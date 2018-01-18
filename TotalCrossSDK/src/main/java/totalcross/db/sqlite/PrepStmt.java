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

import java.sql.SQLException;
import totalcross.io.CharStream;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sql.ParameterMetaData;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.ResultSetMetaData;
import totalcross.sql.Statement;
import totalcross.sql.Timestamp;
import totalcross.sql.Types;
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.util.BigDecimal;
import totalcross.util.Date;

final class PrepStmt extends Stmt implements PreparedStatement, ParameterMetaData, Codes {
  private int columnCount;
  private int paramCount;

  /**
   * Constructs a prepared statement on a provided connection.
   * @param conn Connection on which to create the prepared statement.
   * @param sql The SQL script to prepare.
   * @throws SQLException
   */
  PrepStmt(SQLiteConnection conn, String sql) throws SQLException {
    super(conn);

    this.sql = sql;
    db.prepare(this);
    rs.colsMeta = db.column_names(pointer);
    columnCount = db.column_count(pointer);
    paramCount = db.bind_parameter_count(pointer);
    batch = null;
    batchPos = 0;
  }

  /**
   * @see java.sql.PreparedStatement#clearParameters()
   */
  @Override
  public void clearParameters() throws SQLException {
    checkOpen();
    db.clear_bindings(pointer);
    batch = null;
  }

  /**
   * @see totalcross.db.sqlite.Stmt#finalize()
   */

  @Override
  protected void finalize() throws SQLException {
    close();
  }

  /**
   * Checks if values are bound to statement parameters.
   * @throws SQLException
   */
  private void checkParameters() throws SQLException {
    if (batch == null && paramCount > 0) {
      throw new SQLException("Values not bound to statement");
    }
  }

  /**
   * @see java.sql.PreparedStatement#execute()
   */
  @Override
  public boolean execute() throws SQLException {
    checkOpen();
    rs.close();
    db.reset(pointer);
    checkParameters();

    resultsWaiting = db.execute(this, batch);
    return columnCount != 0;
  }

  /**
   * @see java.sql.PreparedStatement#executeQuery()
   */
  @Override
  public ResultSet executeQuery() throws SQLException {
    checkOpen();

    if (columnCount == 0) {
      throw new SQLException("Query does not return results");
    }

    rs.close();
    db.reset(pointer);
    checkParameters();

    resultsWaiting = db.execute(this, batch);
    return getResultSet();
  }

  /**
   * @see java.sql.PreparedStatement#executeUpdate()
   */
  @Override
  public int executeUpdate() throws SQLException {
    checkOpen();

    if (columnCount != 0) {
      throw new SQLException("Query returns results");
    }

    rs.close();
    db.reset(pointer);
    checkParameters();

    return db.executeUpdate(this, batch);
  }

  /**
   * @see totalcross.db.sqlite.Stmt#executeBatch()
   */

  @Override
  public int[] executeBatch() throws SQLException {
    if (batchPos == 0) {
      return new int[] {};
    }

    checkParameters();

    try {
      return db.executeBatch(pointer, batchPos / paramCount, batch);
    } finally {
      clearBatch();
    }
  }

  /**
   * @see totalcross.db.sqlite.Stmt#getUpdateCount()
   */

  @Override
  public int getUpdateCount() throws SQLException {
    if (pointer == 0 || resultsWaiting || rs.isOpen()) {
      return -1;
    }

    return db.changes();
  }

  /**
   * @see java.sql.PreparedStatement#addBatch()
   */
  @Override
  public void addBatch() throws SQLException {
    checkOpen();
    batchPos += paramCount;
    if (batchPos + paramCount > batch.length) {
      Object[] nb = new Object[batch.length * 2];
      Vm.arrayCopy(batch, 0, nb, 0, batch.length);
      batch = nb;
    }
    Vm.arrayCopy(batch, batchPos - paramCount, batch, batchPos, paramCount);
  }

  // ParameterMetaData FUNCTIONS //////////////////////////////////

  /**
   * @see java.sql.PreparedStatement#getParameterMetaData()
   */
  public ParameterMetaData getParameterMetaData() {
    return this;
  }

  /**
   * @see java.sql.ParameterMetaData#getParameterCount()
   */
  @Override
  public int getParameterCount() throws SQLException {
    checkOpen();
    return paramCount;
  }

  /**
   * @see java.sql.ParameterMetaData#getParameterClassName(int)
   */
  @Override
  public String getParameterClassName(int param) throws SQLException {
    checkOpen();
    return "java.lang.String";
  }

  /**
   * @see java.sql.ParameterMetaData#getParameterTypeName(int)
   */
  @Override
  public String getParameterTypeName(int pos) {
    return "VARCHAR";
  }

  /**
   * @see java.sql.ParameterMetaData#getParameterType(int)
   */
  @Override
  public int getParameterType(int pos) {
    return Types.VARCHAR;
  }

  /**
   * @see java.sql.ParameterMetaData#getParameterMode(int)
   */
  @Override
  public int getParameterMode(int pos) {
    return parameterModeIn;
  }

  /**
   * @see java.sql.ParameterMetaData#getPrecision(int)
   */
  @Override
  public int getPrecision(int pos) {
    return 0;
  }

  /**
   * @see java.sql.ParameterMetaData#getScale(int)
   */
  @Override
  public int getScale(int pos) {
    return 0;
  }

  /**
   * @see java.sql.ParameterMetaData#isNullable(int)
   */
  @Override
  public int isNullable(int pos) {
    return parameterNullable;
  }

  /**
   * @see java.sql.ParameterMetaData#isSigned(int)
   */
  @Override
  public boolean isSigned(int pos) {
    return true;
  }

  public Statement getStatement() {
    return this;
  }

  // PARAMETER FUNCTIONS //////////////////////////////////////////

  /**
   * Assigns the object value to the element at the specific position of array
   * batch.
   * @param pos
   * @param value
   * @throws SQLException
   */
  private void batch(int pos, Object value) throws SQLException {
    checkOpen();
    if (batch == null) {
      batch = new Object[paramCount];
    }
    batch[batchPos + pos - 1] = value;
  }

  /**
   * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
   */
  @Override
  public void setBigDecimal(int pos, BigDecimal value) throws SQLException {
    batch(pos, value == null ? null : value.toString());
  }

  /**
   * Reads given number of bytes from an input stream.
   * @param istream The input stream.
   * @param length The number of bytes to read.
   * @return byte array.
   * @throws SQLException
   */
  private byte[] readBytes(Stream istream, int length) throws SQLException {
    if (length < 0) {
      SQLException exception = new SQLException("Error reading stream. Length should be non-negative");

      throw exception;
    }

    byte[] bytes = new byte[length];

    try {
      istream.readBytes(bytes, 0, bytes.length);

      return bytes;
    } catch (IOException cause) {
      throw new SQLException("Error reading stream.", cause);
    }
  }

  /**
   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
   */
  public void setBinaryStream(int pos, Stream istream, int length) throws SQLException {
    if (istream == null && length == 0) {
      setBytes(pos, null);
    }

    setBytes(pos, readBytes(istream, length));
  }

  /**
   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
   */
  public void setAsciiStream(int pos, Stream istream, int length) throws SQLException {
    setUnicodeStream(pos, istream, length);
  }

  /**
   */
  public void setUnicodeStream(int pos, Stream istream, int length) throws SQLException {
    if (istream == null && length == 0) {
      setString(pos, null);
    }

    setString(pos, new String(readBytes(istream, length)));
  }

  /**
   * @see java.sql.PreparedStatement#setBoolean(int, boolean)
   */
  @Override
  public void setBoolean(int pos, boolean value) throws SQLException {
    setInt(pos, value ? 1 : 0);
  }

  /**
   * @see java.sql.PreparedStatement#setByte(int, byte)
   */
  @Override
  public void setByte(int pos, byte value) throws SQLException {
    setInt(pos, value);
  }

  /**
   * @see java.sql.PreparedStatement#setBytes(int, byte[])
   */
  @Override
  public void setBytes(int pos, byte[] value) throws SQLException {
    batch(pos, value);
  }

  /**
   * @see java.sql.PreparedStatement#setDouble(int, double)
   */
  @Override
  public void setDouble(int pos, double value) throws SQLException {
    batch(pos, new Double(value));
  }

  /**
   * @see java.sql.PreparedStatement#setInt(int, int)
   */
  @Override
  public void setInt(int pos, int value) throws SQLException {
    batch(pos, new Integer(value));
  }

  /**
   * @see java.sql.PreparedStatement#setLong(int, long)
   */
  @Override
  public void setLong(int pos, long value) throws SQLException {
    batch(pos, new Long(value));
  }

  /**
   * @see java.sql.PreparedStatement#setNull(int, int)
   */
  @Override
  public void setNull(int pos, int u1) throws SQLException {
    setNull(pos, u1, null);
  }

  /**
   * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
   */
  @Override
  public void setNull(int pos, int u1, String u2) throws SQLException {
    batch(pos, null);
  }

  /**
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
   */
  @Override
  public void setObject(int pos, Object value) throws SQLException {
    if (value == null) {
      batch(pos, null);
    } else if (value instanceof Date) {
      batch(pos, ((Date) value).getSQLString());
    } else if (value instanceof Time) {
      batch(pos, ((Time) value).getSQLString());
    } else if (value instanceof Timestamp) {
      long l = ((Timestamp) value).getTime();
      batch(pos, new Long(l));
    } else if (value instanceof Long) {
      batch(pos, value);
    } else if (value instanceof Integer) {
      batch(pos, value);
    } else if (value instanceof Short) {
      batch(pos, new Integer(((Short) value).shortValue()));
    } else if (value instanceof Float) {
      batch(pos, value);
    } else if (value instanceof Double) {
      batch(pos, value);
    } else if (value instanceof Boolean) {
      setBoolean(pos, ((Boolean) value).booleanValue());
    } else if (value instanceof byte[]) {
      batch(pos, value);
    } else if (value instanceof BigDecimal) {
      setBigDecimal(pos, (BigDecimal) value);
    } else {
      batch(pos, value.toString());
    }
  }

  /**
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
   */
  @Override
  public void setObject(int p, Object v, int t) throws SQLException {
    setObject(p, v);
  }

  /**
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
   */
  @Override
  public void setObject(int p, Object v, int t, int s) throws SQLException {
    setObject(p, v);
  }

  /**
   * @see java.sql.PreparedStatement#setShort(int, short)
   */
  @Override
  public void setShort(int pos, short value) throws SQLException {
    setInt(pos, value);
  }

  /**
   * @see java.sql.PreparedStatement#setString(int, java.lang.String)
   */
  @Override
  public void setString(int pos, String value) throws SQLException {
    batch(pos, value);
  }

  /**
   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
   */
  public void setCharacterStream(int pos, CharStream reader, int length) throws SQLException {
    try {
      // copy chars from reader to StringBuffer
      StringBuffer sb = new StringBuffer();
      char[] cbuf = new char[8192];
      int cnt;

      while ((cnt = reader.read(cbuf)) > 0) {
        sb.append(cbuf, 0, cnt);
      }

      // set as string
      setString(pos, sb.toString());
    } catch (IOException e) {
      throw new SQLException("Cannot read from character stream, exception message: " + e.getMessage());
    }
  }

  /**
   * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
   */
  @Override
  public void setDate(int pos, Date x) throws SQLException {
    setObject(pos, x);
  }

  /**
   * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
   */
  @Override
  public void setTime(int pos, Time x) throws SQLException {
    setObject(pos, x);
  }

  /**
   * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
   */
  @Override
  public void setTimestamp(int pos, Timestamp x) throws SQLException {
    setObject(pos, x);
  }

  /**
   * @see java.sql.PreparedStatement#getMetaData()
   */
  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    checkOpen();
    return rs;
  }

  // UNUSED ///////////////////////////////////////////////////////

  /**
   * @see totalcross.db.sqlite.Stmt#execute(java.lang.String)
   */

  @Override
  public boolean execute(String sql) throws SQLException {
    throw unused();
  }

  /**
   * @see totalcross.db.sqlite.Stmt#executeUpdate(java.lang.String)
   */

  @Override
  public int executeUpdate(String sql) throws SQLException {
    throw unused();
  }

  /**
   * @see totalcross.db.sqlite.Stmt#executeQuery(java.lang.String)
   */

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    throw unused();
  }

  /**
   * @see totalcross.db.sqlite.Stmt#addBatch(java.lang.String)
   */

  @Override
  public void addBatch(String sql) throws SQLException {
    throw unused();
  }
}
