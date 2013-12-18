/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB__open_si(NMParams p) // totalcross/db/sqlite/NativeDB protected native void _open(String file, int openFlags) throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB__close(NMParams p) // totalcross/db/sqlite/NativeDB protected native void _close() throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB__exec_s(NMParams p) // totalcross/db/sqlite/NativeDB protected native int _exec(String sql) throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_shared_cache_b(NMParams p) // totalcross/db/sqlite/NativeDB native int shared_cache(boolean enable);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_enable_load_extension_b(NMParams p) // totalcross/db/sqlite/NativeDB native int enable_load_extension(boolean enable);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_interrupt(NMParams p) // totalcross/db/sqlite/NativeDB native void interrupt();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_busy_timeout_i(NMParams p) // totalcross/db/sqlite/NativeDB native void busy_timeout(int ms);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_exec_s(NMParams p) // totalcross/db/sqlite/NativeDB native void exec(String sql) throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_prepare_s(NMParams p) // totalcross/db/sqlite/NativeDB protected native long prepare(String sql) throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_errmsg(NMParams p) // totalcross/db/sqlite/NativeDB native String errmsg();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_libversion(NMParams p) // totalcross/db/sqlite/NativeDB native String libversion();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_changes(NMParams p) // totalcross/db/sqlite/NativeDB native int changes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_total_changes(NMParams p) // totalcross/db/sqlite/NativeDB native int total_changes();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_finalize_l(NMParams p) // totalcross/db/sqlite/NativeDB protected native int finalize(long stmt);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_step_l(NMParams p) // totalcross/db/sqlite/NativeDB protected native int step(long stmt);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_reset_l(NMParams p) // totalcross/db/sqlite/NativeDB protected native int reset(long stmt);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_clear_bindings_l(NMParams p) // totalcross/db/sqlite/NativeDB native int clear_bindings(long stmt);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_parameter_count_l(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_parameter_count(long stmt);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_count_l(NMParams p) // totalcross/db/sqlite/NativeDB native int column_count(long stmt);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_type_li(NMParams p) // totalcross/db/sqlite/NativeDB native int column_type(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_decltype_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_decltype(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_table_name_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_table_name(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_name_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_name(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_text_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_text(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_blob_li(NMParams p) // totalcross/db/sqlite/NativeDB native byte[] column_blob(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_double_li(NMParams p) // totalcross/db/sqlite/NativeDB native double column_double(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_long_li(NMParams p) // totalcross/db/sqlite/NativeDB native long column_long(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_int_li(NMParams p) // totalcross/db/sqlite/NativeDB native int column_int(long stmt, int col);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_null_li(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_null(long stmt, int pos);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_int_lii(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_int(long stmt, int pos, int v);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_long_lil(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_long(long stmt, int pos, long v);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_double_lid(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_double(long stmt, int pos, double v);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_text_lis(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_text(long stmt, int pos, String v);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_bind_blob_liB(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_blob(long stmt, int pos, byte []v);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_null_l(NMParams p) // totalcross/db/sqlite/NativeDB native void result_null(long context);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_text_ls(NMParams p) // totalcross/db/sqlite/NativeDB native void result_text(long context, String val);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_blob_lB(NMParams p) // totalcross/db/sqlite/NativeDB native void result_blob(long context, byte []val);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_double_ld(NMParams p) // totalcross/db/sqlite/NativeDB native void result_double(long context, double val);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_long_ll(NMParams p) // totalcross/db/sqlite/NativeDB native void result_long(long context, long val);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_int_li(NMParams p) // totalcross/db/sqlite/NativeDB native void result_int(long context, int val);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_result_error_ls(NMParams p) // totalcross/db/sqlite/NativeDB native void result_error(long context, String err);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_bytes_fi(NMParams p) // totalcross/db/sqlite/NativeDB native int value_bytes(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_text_fi(NMParams p) // totalcross/db/sqlite/NativeDB native String value_text(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_blob_fi(NMParams p) // totalcross/db/sqlite/NativeDB native byte[] value_blob(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_double_fi(NMParams p) // totalcross/db/sqlite/NativeDB native double value_double(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_long_fi(NMParams p) // totalcross/db/sqlite/NativeDB native long value_long(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_int_fi(NMParams p) // totalcross/db/sqlite/NativeDB native int value_int(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_value_type_fi(NMParams p) // totalcross/db/sqlite/NativeDB native int value_type(totalcross.db.sqlite.Function f, int arg);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_create_function_sf(NMParams p) // totalcross/db/sqlite/NativeDB native int create_function(String name, totalcross.db.sqlite.Function func);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_destroy_function_s(NMParams p) // totalcross/db/sqlite/NativeDB native int destroy_function(String name);
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_free_functions(NMParams p) // totalcross/db/sqlite/NativeDB native void free_functions();
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_backup_ssp(NMParams p) // totalcross/db/sqlite/NativeDB native int backup(String dbName, String destFileName, totalcross.db.sqlite.DB.ProgressObserver observer) throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_restore_ssp(NMParams p) // totalcross/db/sqlite/NativeDB native int restore(String dbName, String sourceFileName, totalcross.db.sqlite.DB.ProgressObserver observer) throws SQLException;
{
}
//////////////////////////////////////////////////////////////////////////
TC_API void tdsNDB_column_metadata_l(NMParams p) // totalcross/db/sqlite/NativeDB native boolean[][] column_metadata(long stmt);
{
}
