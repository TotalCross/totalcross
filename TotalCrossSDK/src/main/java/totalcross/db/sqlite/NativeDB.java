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

/** This class provides a thin JNI layer over the SQLite3 C API. */
final class NativeDB extends DB
{
  /** SQLite connection handle. */
  long pointer;

  /**
   * Loads the SQLite interface backend.
   */
  native static void load() throws Exception; 

  // WRAPPER FUNCTIONS ////////////////////////////////////////////

  /**
   * @see org.sqlite.DB#_open(java.lang.String, int)
   */

  @Override
  protected native void _open(String file, int openFlags) throws SQLException;

  /**
   * @see org.sqlite.DB#_close()
   */

  @Override
  protected native void _close() throws SQLException;

  /**
   * @see org.sqlite.DB#_exec(java.lang.String)
   */

  @Override
  protected native int _exec(String sql) throws SQLException;

  /**
   * @see org.sqlite.DB#shared_cache(boolean)
   */

  @Override
  native int shared_cache(boolean enable);

  /**
   * @see org.sqlite.DB#enable_load_extension(boolean)
   */

  @Override
  native int enable_load_extension(boolean enable);

  /**
   * @see org.sqlite.DB#interrupt()
   */

  @Override
  native void interrupt();

  /**
   * @see org.sqlite.DB#busy_timeout(int)
   */

  @Override
  native void busy_timeout(int ms);

  /**
   * @see org.sqlite.DB#prepare(java.lang.String)
   */
  //native void exec(String sql) throws SQLException;

  @Override
  protected native long prepare(String sql) throws SQLException;

  /**
   * @see org.sqlite.DB#errmsg()
   */

  @Override
  native String errmsg();

  /**
   * @see org.sqlite.DB#libversion()
   */

  @Override
  native String libversion();

  /**
   * @see org.sqlite.DB#changes()
   */

  @Override
  native int changes();

  /**
   * @see org.sqlite.DB#total_changes()
   */

  @Override
  native int total_changes();

  /**
   * @see org.sqlite.DB#finalize(long)
   */

  @Override
  protected native int finalize(long stmt);

  /**
   * @see org.sqlite.DB#step(long)
   */

  @Override
  protected native int step(long stmt);

  /**
   * @see org.sqlite.DB#reset(long)
   */

  @Override
  protected native int reset(long stmt);

  /**
   * @see org.sqlite.DB#clear_bindings(long)
   */

  @Override
  native int clear_bindings(long stmt);

  /**
   * @see org.sqlite.DB#bind_parameter_count(long)
   */

  @Override
  native int bind_parameter_count(long stmt);

  /**
   * @see org.sqlite.DB#column_count(long)
   */

  @Override
  native int column_count(long stmt);

  /**
   * @see org.sqlite.DB#column_type(long, int)
   */

  @Override
  native int column_type(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_decltype(long, int)
   */

  @Override
  native String column_decltype(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_table_name(long, int)
   */

  @Override
  native String column_table_name(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_name(long, int)
   */

  @Override
  native String column_name(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_text(long, int)
   */

  @Override
  native String column_text(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_blob(long, int)
   */

  @Override
  native byte[] column_blob(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_double(long, int)
   */

  @Override
  native double column_double(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_long(long, int)
   */

  @Override
  native long column_long(long stmt, int col);

  /**
   * @see org.sqlite.DB#column_int(long, int)
   */

  @Override
  native int column_int(long stmt, int col);

  /**
   * @see org.sqlite.DB#bind_null(long, int)
   */

  @Override
  native int bind_null(long stmt, int pos);

  /**
   * @see org.sqlite.DB#bind_int(long, int, int)
   */

  @Override
  native int bind_int(long stmt, int pos, int v);

  /**
   * @see org.sqlite.DB#bind_long(long, int, long)
   */

  @Override
  native int bind_long(long stmt, int pos, long v);

  /**
   * @see org.sqlite.DB#bind_double(long, int, double)
   */

  @Override
  native int bind_double(long stmt, int pos, double v);

  /**
   * @see org.sqlite.DB#bind_text(long, int, java.lang.String)
   */

  @Override
  native int bind_text(long stmt, int pos, String v);

  /**
   * @see org.sqlite.DB#bind_blob(long, int, byte[])
   */

  @Override
  native int bind_blob(long stmt, int pos, byte[] v);

  /**
   * @see org.sqlite.DB#result_null(long)
   */

  @Override
  native void result_null(long context);

  /**
   * @see org.sqlite.DB#result_text(long, java.lang.String)
   */

  @Override
  native void result_text(long context, String val);

  /**
   * @see org.sqlite.DB#result_blob(long, byte[])
   */

  @Override
  native void result_blob(long context, byte[] val);

  /**
   * @see org.sqlite.DB#result_double(long, double)
   */

  @Override
  native void result_double(long context, double val);

  /**
   * @see org.sqlite.DB#result_long(long, long)
   */

  @Override
  native void result_long(long context, long val);

  /**
   * @see org.sqlite.DB#result_int(long, int)
   */

  @Override
  native void result_int(long context, int val);

  /**
   * @see org.sqlite.DB#result_error(long, java.lang.String)
   */

  @Override
  native void result_error(long context, String err);

  /**
   * @see org.sqlite.DB#value_bytes(org.sqlite.Function, int)
   */

  //native int value_bytes(Function f, int arg);

  /**
   * @see org.sqlite.DB#value_text(org.sqlite.Function, int)
   */

  //native String value_text(Function f, int arg);

  /**
   * @see org.sqlite.DB#value_blob(org.sqlite.Function, int)
   */

  //native byte[] value_blob(Function f, int arg);

  /**
   * @see org.sqlite.DB#value_double(org.sqlite.Function, int)
   */

  //native double value_double(Function f, int arg);

  /**
   * @see org.sqlite.DB#value_long(org.sqlite.Function, int)
   */

  //native long value_long(Function f, int arg);

  /**
   * @see org.sqlite.DB#value_int(org.sqlite.Function, int)
   */

  //native int value_int(Function f, int arg);

  /**
   * @see org.sqlite.DB#value_type(org.sqlite.Function, int)
   */

  //native int value_type(Function f, int arg);

  /**
   * @see org.sqlite.DB#create_function(java.lang.String, org.sqlite.Function)
   */

  //native int create_function(String name, Function func);

  /**
   * @see org.sqlite.DB#destroy_function(java.lang.String)
   */

  //native int destroy_function(String name);

  /**
   * @see org.sqlite.DB#free_functions()
   */

  //native void free_functions();

  /**
   * @see org.sqlite.DB#backup(java.lang.String, java.lang.String, org.sqlite.DB.ProgressObserver)
   */

  @Override
  native int backup(String dbName, String destFileName, totalcross.db.sqlite.DB.ProgressObserver observer) throws SQLException;

  /**
   * @see org.sqlite.DB#restore(java.lang.String, java.lang.String,
   *      org.sqlite.DB.ProgressObserver)
   */

  @Override
  native int restore(String dbName, String sourceFileName, totalcross.db.sqlite.DB.ProgressObserver observer) throws SQLException;

  // COMPOUND FUNCTIONS (for optimisation) /////////////////////////

  /**
   * Provides metadata for table columns.
   * @returns For each column returns: <br/>
   * res[col][0] = true if column constrained NOT NULL<br/>
   * res[col][1] = true if column is part of the primary key<br/>
   * res[col][2] = true if column is auto-increment.
   * @see org.sqlite.DB#column_metadata(long)
   */

  @Override
  native boolean[][] column_metadata(long stmt);

  /**
   * Throws an SQLException
   * @param msg Message for the SQLException.
   * @throws SQLException
   */
  static void throwex(String msg) throws SQLException 
  {
    throw new SQLException(msg);
  }
}
