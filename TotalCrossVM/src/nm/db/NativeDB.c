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

#include "tcvm.h"
#include "../../sqlite/sqlite3.h"

//TC_API void tdsNDB_exec_s(NMParams p) // totalcross/db/sqlite/NativeDB native void exec(String sql) throws SQLException;
//TC_API void tdsNDB_result_error_ls(NMParams p) // totalcross/db/sqlite/NativeDB native void result_error(long context, String err);

// NativeDB extends DB

#define DB_begin(o)              FIELD_I64(o, OBJ_CLASS(o), 0)
#define DB_commit(o)             FIELD_I64(o, OBJ_CLASS(o), 1)

#define NativeDB_pointer(o)      FIELD_I64(o, OBJ_CLASS(o), 2)

static TCClass dbclass;
static TCClass fclass;
static TCClass aclass;
static TCClass pclass;

#define TRACE(x)
//#define TRACE(x) bool xxx = debug("%s: i64: %X, obj: %X",x,(int)p->i64[0],(int)p->obj[0]);

/* Definicoes */
int32 utf8len(JCharP chars, int32 length);
void utf8chars2bytesBuf(JCharP chars, int32 length, uint8* bytes);
TCObject utf8bytes2chars(Context currentContext, JCharP chars, int32 length);
// CharacterConverter.c


static void * toref(int64 value)
{
    return (void *) value;
}

static int64 fromref(void * value)
{
    return (int64)value;
}

static void throw_errorcode(Context currentContext, TCObject this_, int32 errorCode)
{
    static Method mth_throwex = 0;
    if (!mth_throwex)
       mth_throwex = getMethod(dbclass, true, "throwex", 1, J_INT);
    executeMethod(currentContext, mth_throwex, this_, errorCode);
}

static void throwexmsg(Context currentContext, TCObject this_, char *str)
{
    static Method mth_throwex = 0;
    TCObject o = createStringObjectFromCharP(currentContext, str, -1);
    if (!mth_throwex)
       mth_throwex = getMethod(dbclass, true, "throwex", 1, "java.lang.String"); // method is static!
    executeMethod(currentContext, mth_throwex, o);
    setObjectLock(o,UNLOCKED);
}

static sqlite3 * gethandle(Context currentContext, TCObject this_)
{
   return (sqlite3 *)NativeDB_pointer(this_);
}

static void sethandle(Context currentContext, TCObject this_, sqlite3 * ref)
{
   NativeDB_pointer(this_) = (int64)ref;
}

/**

IMPORTANT! TO AVOID DEADLOCK, UNLOCKDB MUST BE CALLED BEFORE ANY TCOBJECT ALLOCATIONS!

**/
#define LOCKDB int32 dumbmutex = lockdb(p->obj[0]);
#define UNLOCKDB unlockdb(p->obj[0],dumbmutex);

int32 lockSqlite3(void* handle);
void unlockSqlite3(void* handle);
void initSqlite3Mutex(void* db_);
void destroySqlite3Mutex(void* db_);

static int32 lockdb(TCObject this_)
{
   return lockSqlite3(gethandle(null,this_));
}

static void unlockdb(TCObject this_, int32 dumb)
{
   unlockSqlite3(gethandle(null,this_));
}

// INITIALISATION ///////////////////////////////////////////////////

TC_API void tdsNDB_load(NMParams p) // totalcross/db/sqlite/NativeDB native static void load() throws Exception;
{
   TRACE("tdsNDB_load")
   if (dbclass == null)
   {
      dbclass = loadClass(p->currentContext, "totalcross.db.sqlite.NativeDB", false);
      fclass  = loadClass(p->currentContext, "totalcross.db.sqlite.Function", false);
      aclass  = loadClass(p->currentContext, "totalcross.db.sqlite.Function$Aggregate", false);
      pclass  = loadClass(p->currentContext, "totalcross.db.sqlite.DB$ProgressObserver", false);
   }
}

// WRAPPERS for sqlite_* functions //////////////////////////////////

TC_API void tdsNDB_shared_cache_b(NMParams p) // totalcross/db/sqlite/NativeDB native int shared_cache(boolean enable);
{
   TRACE("tdsNDB_shared_cache_b")
   bool enable = p->i32[0];
   LOCKDB
   p->retI = sqlite3_enable_shared_cache(enable);
   UNLOCKDB
}
TC_API void tdsNDB_enable_load_extension_b(NMParams p) // totalcross/db/sqlite/NativeDB native int enable_load_extension(boolean enable);
{
   TRACE("tdsNDB_enable_load_extension_b")
   TCObject this_ = p->obj[0];
   bool enable = p->i32[0];
   LOCKDB
	p->retI = sqlite3_enable_load_extension(gethandle(p->currentContext, this_), enable);
   UNLOCKDB
}

static CharP newSafeString(TCObject strObj)
{
   JCharP js = String_charsStart(strObj);
   int32 len = String_charsLen(strObj);
   CharP ret;
   
   if (OBJ_CLASS(*charConverterPtr) == UTF8CharacterConverter)
   {
      int32 utflen = utf8len(js, len);
      ret = (CharP)xmalloc(utflen+16); // put 8 bytes before and 8 after the string
      ret += 8;
      utf8chars2bytesBuf(js, len, ret);
   }
   else
   {
      ret = (CharP)xmalloc(len+16); // put 8 bytes before and 8 after the string
      ret += 8;
      JCharP2CharPBuf(js, len, ret);      
   }
   return ret;
}

static freeSafeString(CharP buf)
{
   buf -= 8;
   xfree(buf);
}

TC_API void tdsNDB__open_si(NMParams p) // totalcross/db/sqlite/NativeDB protected native void _open(String file, int openFlags) throws SQLException;
{
   TRACE("tdsNDB__open_si")
    int32 ret;
    int32 flags = p->i32[0];
    TCObject this_ = p->obj[0];
    TCObject file = p->obj[1];
    sqlite3 *db = gethandle(p->currentContext, this_);
    CharP str;

    if (db)
    {
        throwexmsg(p->currentContext, this_, "DB already open");
        sqlite3_close(db);
        return;
    }

    str = newSafeString(file);
    ret = sqlite3_open_v2(str, &db, flags, NULL);
    if (ret) 
    {
        throw_errorcode(p->currentContext, this_, ret);
        sqlite3_close(db);
        return;
    }
    freeSafeString(str);

    sethandle(p->currentContext, this_, db);
    initSqlite3Mutex(db);
}

TC_API void tdsNDB__close(NMParams p) // totalcross/db/sqlite/NativeDB protected native void _close() throws SQLException;
{
   TRACE("!!!!!! tdsNDB__close")
   TCObject this_ = p->obj[0];
   sqlite3* db = gethandle(p->currentContext, this_);
   if (db)
   {
      destroySqlite3Mutex(db);
      if (sqlite3_close(db) == SQLITE_OK)
         sethandle(p->currentContext, this_, 0);
   }
}

TC_API void tdsNDB_interrupt(NMParams p) // totalcross/db/sqlite/NativeDB native void interrupt();
{
   // NOT SYNCHRONIZED!
   TRACE("tdsNDB_interrupt")
   TCObject this_ = p->obj[0];
   sqlite3_interrupt(gethandle(p->currentContext, this_));
}

TC_API void tdsNDB_busy_timeout_i(NMParams p) // totalcross/db/sqlite/NativeDB native void busy_timeout(int ms);
{                                                                
   TRACE("tdsNDB_busy_timeout_i")
   TCObject this_ = p->obj[0];
   int32 ms = p->i32[0];
   LOCKDB
   sqlite3_busy_timeout(gethandle(p->currentContext, this_), ms);
   UNLOCKDB
}

TC_API void tdsNDB_prepare_s(NMParams p) // totalcross/db/sqlite/NativeDB protected native long prepare(String sql) throws SQLException;
{
   TRACE("tdsNDB_prepare_s")
   TCObject this_ = p->obj[0];
   TCObject sql = p->obj[1];
    sqlite3* db = gethandle(p->currentContext, this_);
    sqlite3_stmt* stmt;

    char *strsql = newSafeString(sql);
   LOCKDB
    int32 status = sqlite3_prepare_v2(db, strsql, -1, &stmt, 0);
   UNLOCKDB
    freeSafeString(strsql);

    if (status != SQLITE_OK) 
    {
        throw_errorcode(p->currentContext, this_, status);
        p->retL = 0;
    }
    else p->retL = fromref(stmt);
}

TC_API void tdsNDB__exec_s(NMParams p) // totalcross/db/sqlite/NativeDB protected native int _exec(String sql) throws SQLException;
{
   TRACE("tdsNDB__exec_s")
   TCObject this_ = p->obj[0];
   TCObject sql = p->obj[1];
   sqlite3* db = gethandle(p->currentContext, this_);
   char* errorMsg;

	if(!db)
	{
		throw_errorcode(p->currentContext, this_, 21);
		p->retI = 21;
	}
   else
   {
    char *strsql = newSafeString(sql);
    LOCKDB
    int status = sqlite3_exec(db, strsql, 0, 0, &errorMsg);
    UNLOCKDB
    freeSafeString(strsql);

    if (status != SQLITE_OK) 
    {
        throwexmsg(p->currentContext, this_, errorMsg);
        sqlite3_free(errorMsg);
    }
    p->retI = status;
   }
}

TC_API void tdsNDB_errmsg(NMParams p) // totalcross/db/sqlite/NativeDB native String errmsg();
{
   TRACE("tdsNDB_errmsg")
   TCObject this_ = p->obj[0];
   CharP msg;
   LOCKDB
   msg = (char*)sqlite3_errmsg(gethandle(p->currentContext, this_));
   UNLOCKDB
   setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, msg,-1), UNLOCKED);
}

TC_API void tdsNDB_libversion(NMParams p) // totalcross/db/sqlite/NativeDB native String libversion();
{
   TRACE("tdsNDB_libversion")
   CharP msg;
   LOCKDB
   msg = (char*)sqlite3_libversion();
   UNLOCKDB
   setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, msg, -1), UNLOCKED);
}

TC_API void tdsNDB_changes(NMParams p) // totalcross/db/sqlite/NativeDB native int changes();
{
   TRACE("tdsNDB_changes")
   TCObject this_ = p->obj[0];
   LOCKDB
    p->retI = sqlite3_changes(gethandle(p->currentContext, this_));
   UNLOCKDB
}

TC_API void tdsNDB_total_changes(NMParams p) // totalcross/db/sqlite/NativeDB native int total_changes();
{
   TRACE("tdsNDB_total_changes")
   TCObject this_ = p->obj[0];
   LOCKDB
    p->retI = sqlite3_total_changes(gethandle(p->currentContext, this_));
   UNLOCKDB
}

TC_API void tdsNDB_finalize_l(NMParams p) // totalcross/db/sqlite/NativeDB protected native int finalize(long stmt);
{
   TRACE("@@@@ tdsNDB_finalize_l")
   int64 stmt = p->i64[0];
   LOCKDB
   p->retI = sqlite3_finalize(toref(stmt));
   UNLOCKDB
}

TC_API void tdsNDB_step_l(NMParams p) // totalcross/db/sqlite/NativeDB protected native int step(long stmt);
{
   TRACE("tdsNDB_step_l")
   int64 stmt = p->i64[0];
   LOCKDB
   p->retI = sqlite3_step(toref(stmt));
   UNLOCKDB
}

TC_API void tdsNDB_reset_l(NMParams p) // totalcross/db/sqlite/NativeDB protected native int reset(long stmt);
{
   TRACE("tdsNDB_reset_l")
   int64 stmt = p->i64[0];
   LOCKDB
    p->retI = sqlite3_reset(toref(stmt));
   UNLOCKDB
}

TC_API void tdsNDB_clear_bindings_l(NMParams p) // totalcross/db/sqlite/NativeDB native int clear_bindings(long stmt);
{
   TRACE("tdsNDB_clear_bindings_l")
   int64 stmt = p->i64[0];
   LOCKDB
    p->retI = sqlite3_clear_bindings(toref(stmt));
   UNLOCKDB
}

TC_API void tdsNDB_bind_parameter_count_l(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_parameter_count(long stmt);
{
   TRACE("tdsNDB_bind_parameter_count_l")
   int64 stmt = p->i64[0];
   LOCKDB
    p->retI = sqlite3_bind_parameter_count(toref(stmt));
   UNLOCKDB
}

TC_API void tdsNDB_column_count_l(NMParams p) // totalcross/db/sqlite/NativeDB native int column_count(long stmt);
{
   TRACE("tdsNDB_column_count_l")
   int64 stmt = p->i64[0];
   LOCKDB
    p->retI = sqlite3_column_count(toref(stmt));
   UNLOCKDB
}

TC_API void tdsNDB_column_type_li(NMParams p) // totalcross/db/sqlite/NativeDB native int column_type(long stmt, int col);
{
   TRACE("tdsNDB_column_type_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
    p->retI = sqlite3_column_type(toref(stmt), col);
   UNLOCKDB
}

TC_API void tdsNDB_column_decltype_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_decltype(long stmt, int col);
{
   TRACE("tdsNDB_column_decltype_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
   char *str = (char*)sqlite3_column_decltype(toref(stmt), col);
   UNLOCKDB
   setObjectLock(p->retO = str ? createStringObjectFromCharP(p->currentContext, str,-1) : null, UNLOCKED);
}

TC_API void tdsNDB_column_table_name_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_table_name(long stmt, int col);
{
   TRACE("tdsNDB_column_table_name_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
   JChar *str = (JChar*)sqlite3_column_table_name16(toref(stmt), col);
   UNLOCKDB
   setObjectLock(p->retO = str ? createStringObjectFromJCharP(p->currentContext, str,-1) : null, UNLOCKED);
}

TC_API void tdsNDB_column_name_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_name(long stmt, int col);
{
   TRACE("tdsNDB_column_name_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
   JChar *str = (JChar*)sqlite3_column_name16(toref(stmt), col);
   UNLOCKDB
   setObjectLock(p->retO = str ? createStringObjectFromJCharP(p->currentContext, str,-1) : null, UNLOCKED);
}

TC_API void tdsNDB_column_text_li(NMParams p) // totalcross/db/sqlite/NativeDB native String column_text(long stmt, int col);
{                                                
   TRACE("tdsNDB_column_text_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
   char* str = (char*)sqlite3_column_text(toref(stmt), col);
   UNLOCKDB
   if (!str)
      p->retO = null;
   else
   if (OBJ_CLASS(*charConverterPtr) != UTF8CharacterConverter)
      setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, str,-1), UNLOCKED);
   else
   {
      TCObject charArray = utf8bytes2chars(p->currentContext, str, xstrlen(str)); // unlocked on return
      JCharP chars = (JCharP)ARRAYOBJ_START(charArray);
      int32 len = ARRAYOBJ_LEN(charArray);
      setObjectLock(p->retO = createStringObjectFromJCharP(p->currentContext, chars, len), UNLOCKED);
   }
}

TC_API void tdsNDB_column_blob_li(NMParams p) // totalcross/db/sqlite/NativeDB native byte[] column_blob(long stmt, int col);
{
   TRACE("tdsNDB_column_blob_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
   int32 length = sqlite3_column_bytes(toref(stmt), col);
   UNLOCKDB
   if (length > 0) 
   {
      TCObject jBlob = createByteArray(p->currentContext, length);
      uint8 *a = ARRAYOBJ_START(jBlob);
      LOCKDB
      void *blob = (void*)sqlite3_column_blob(toref(stmt), col);
      memcpy(a, blob, length);
      UNLOCKDB
      setObjectLock(p->retO = jBlob, UNLOCKED);
   }
}

TC_API void tdsNDB_column_double_li(NMParams p) // totalcross/db/sqlite/NativeDB native double column_double(long stmt, int col);
{
   TRACE("tdsNDB_column_double_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
   p->retD = sqlite3_column_double(toref(stmt), col);
   UNLOCKDB
}

TC_API void tdsNDB_column_long_li(NMParams p) // totalcross/db/sqlite/NativeDB native long column_long(long stmt, int col);
{
   TRACE("tdsNDB_column_long_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0]; 
   LOCKDB
   p->retL = sqlite3_column_int64(toref(stmt), col);
   UNLOCKDB
}

TC_API void tdsNDB_column_int_li(NMParams p) // totalcross/db/sqlite/NativeDB native int column_int(long stmt, int col);
{
   TRACE("tdsNDB_column_int_li")
   int64 stmt = p->i64[0];
   int32 col = p->i32[0];
   LOCKDB
    p->retI = sqlite3_column_int(toref(stmt), col);
   UNLOCKDB
}

TC_API void tdsNDB_bind_null_li(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_null(long stmt, int pos);
{
   TRACE("tdsNDB_bind_null_li")
   int64 stmt = p->i64[0];
   int32 pos = p->i32[0];
   LOCKDB
    p->retI = sqlite3_bind_null(toref(stmt), pos);
   UNLOCKDB
}

TC_API void tdsNDB_bind_int_lii(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_int(long stmt, int pos, int v);
{
   TRACE("tdsNDB_bind_int_lii")
   int64 stmt = p->i64[0];
   int32 pos = p->i32[0];
   int32 v = p->i32[1];
   LOCKDB
    p->retI = sqlite3_bind_int(toref(stmt), pos, v);
   UNLOCKDB
}

TC_API void tdsNDB_bind_long_lil(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_long(long stmt, int pos, long v);
{
   TRACE("tdsNDB_bind_long_lil")
   int64 stmt = p->i64[0];
   int32 pos = p->i32[0];
   int64 v = p->i64[1];
   LOCKDB
    p->retI = sqlite3_bind_int64(toref(stmt), pos, v);
   UNLOCKDB
}

TC_API void tdsNDB_bind_double_lid(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_double(long stmt, int pos, double v);
{
   TRACE("tdsNDB_bind_double_lid")
   int64 stmt = p->i64[0];
   int32 pos = p->i32[0];
   double v = p->dbl[1];
   LOCKDB
   p->retI = sqlite3_bind_double(toref(stmt), pos, v);
   UNLOCKDB
}

TC_API void tdsNDB_bind_text_lis(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_text(long stmt, int pos, String v);
{
   TRACE("tdsNDB_bind_text_lis")
   int64 stmt = p->i64[0];
   int32 pos = p->i32[0];
   TCObject v = p->obj[1];
   char *chars = newSafeString(v);
   LOCKDB
   int rc = sqlite3_bind_text(toref(stmt), pos, chars, String_charsLen(v), SQLITE_TRANSIENT);
   UNLOCKDB
   freeSafeString(chars);
   p->retI = rc;
}

TC_API void tdsNDB_bind_blob_liB(NMParams p) // totalcross/db/sqlite/NativeDB native int bind_blob(long stmt, int pos, byte []v);
{
   TRACE("tdsNDB_bind_blob_liB")
   int64 stmt = p->i64[0];
   int32 pos = p->i32[0];
   TCObject v = p->obj[1];
   int32 size = ARRAYOBJ_LEN(v);
   void *a = ARRAYOBJ_START(v);
   LOCKDB
   p->retI = sqlite3_bind_blob(toref(stmt), pos, a, size, SQLITE_TRANSIENT);
   UNLOCKDB
}

TC_API void tdsNDB_result_null_l(NMParams p) // totalcross/db/sqlite/NativeDB native void result_null(long context);
{
   TRACE("tdsNDB_result_null_l")
    int64 context = p->i64[0];
   LOCKDB
    sqlite3_result_null(toref(context));
   UNLOCKDB
}

TC_API void tdsNDB_result_text_ls(NMParams p) // totalcross/db/sqlite/NativeDB native void result_text(long context, String val);
{
   TRACE("tdsNDB_result_text_ls")
    JChar *str;
    int32 size;
    int64 context = p->i64[0];
    TCObject value = p->obj[1];
   LOCKDB

    if (value == NULL) 
       sqlite3_result_null(toref(context)); 
    else
    {
       size = String_charsLen(value);
       str = String_charsStart(value);
       sqlite3_result_text16(toref(context), str, size, SQLITE_TRANSIENT);
    }
   UNLOCKDB
}

TC_API void tdsNDB_result_blob_lB(NMParams p) // totalcross/db/sqlite/NativeDB native void result_blob(long context, byte []val);
{
   TRACE("tdsNDB_result_blob_lB")
    int8 *bytes;
    int32 size;
    int64 context = p->i64[0];
    TCObject value = p->obj[1];
    LOCKDB

    if (value == NULL) 
       sqlite3_result_null(toref(context)); 
    else
    {
       size = ARRAYOBJ_LEN(value);
       bytes = (int8*)ARRAYOBJ_START(value);
       sqlite3_result_blob(toref(context), bytes, size, SQLITE_TRANSIENT);
    }
   UNLOCKDB
}

TC_API void tdsNDB_result_double_ld(NMParams p) // totalcross/db/sqlite/NativeDB native void result_double(long context, double val);
{
   TRACE("tdsNDB_result_double_ld")
    int64 context = p->i64[0];
    double value = p->dbl[0];
   LOCKDB
    sqlite3_result_double(toref(context), value);
   UNLOCKDB
}

TC_API void tdsNDB_result_long_ll(NMParams p) // totalcross/db/sqlite/NativeDB native void result_long(long context, long val);
{
   TRACE("tdsNDB_result_long_ll")
    int64 context = p->i64[0];
    int64 value = p->i64[1];
   LOCKDB
    sqlite3_result_int64(toref(context), value);
   UNLOCKDB
}

TC_API void tdsNDB_result_int_li(NMParams p) // totalcross/db/sqlite/NativeDB native void result_int(long context, int val);
{
   TRACE("tdsNDB_result_int_li")
    int64 context = p->i64[0];
    int32 value = p->i32[0];
   LOCKDB
    sqlite3_result_int(toref(context), value);
   UNLOCKDB
}

// COMPOUND FUNCTIONS ///////////////////////////////////////////////

TC_API void tdsNDB_column_metadata_l(NMParams p) // totalcross/db/sqlite/NativeDB native boolean[][] column_metadata(long stmt);
{
   TRACE("tdsNDB_column_metadata_l")
   TCObject this_ = p->obj[0];
   int64 stmt = p->i64[0];
    char *zTableName, *zColumnName;
    int32 pNotNull, pPrimaryKey, pAutoinc, i, colCount;
    TCObject boolArray; // ObjectArray
    TCObject colData; // jbooleanArray
    TCObject *oa;
    sqlite3 *db;
    sqlite3_stmt *dbstmt;
    int8* ab;
    LOCKDB
    db = gethandle(p->currentContext, this_);
    dbstmt = toref(stmt);
    colCount = sqlite3_column_count(dbstmt);
    UNLOCKDB

    boolArray = createArrayObject(p->currentContext, BOOLEAN_MATRIX, colCount) ;
    oa = (TCObject*)ARRAYOBJ_START(boolArray);

    for (i = 0; i < colCount; i++) 
    {
        LOCKDB
        pNotNull = pPrimaryKey = pAutoinc = 0;
        // load passed column name and table name
        zColumnName = (char*)sqlite3_column_name(dbstmt, i);
        zTableName  = (char*)sqlite3_column_table_name(dbstmt, i);
        // request metadata for column and load into output variables
        if (zTableName && zColumnName) 
            sqlite3_table_column_metadata(db, 0, zTableName, zColumnName,0, 0, &pNotNull, &pPrimaryKey, &pAutoinc);
        UNLOCKDB

        colData = createArrayObject(p->currentContext, BOOLEAN_ARRAY, 3);
        ab = (int8*)ARRAYOBJ_START(colData);

        // load relevant metadata into 2nd dimension of return results
        ab[0] = pNotNull;
        ab[1] = pPrimaryKey;
        ab[2] = pAutoinc;

        *oa++ = colData;
        setObjectLock(colData, UNLOCKED);
    }

    setObjectLock(p->retO = boolArray, UNLOCKED);
}

// backup function

void reportProgress(Context currentContext, TCObject func, int remaining, int pageCount) {
  static Method mth = 0;
  if (!mth) 
      mth = getMethod(pclass, true, "progress", 2, J_INT, J_INT);

  if (func)
     executeMethod(currentContext, mth, func, remaining, pageCount);
}


/*
** Perform an online backup of database pDb to the database file named
** by zFilename. This function copies 5 database pages from pDb to
** zFilename, then unlocks pDb and sleeps for 250 ms, then repeats the
** process until the entire database is backed up.
**
** The third argument passed to this function must be a pointer to a progress
** function. After each set of 5 pages is backed up, the progress function
** is invoked with two integer parameters: the number of pages left to
** copy, and the total number of pages in the source file. This information
** may be used, for example, to update a GUI progress bar.
**
** While this function is running, another thread may use the database pDb, or
** another process may access the underlying database file via a separate
** connection.
**
** If the backup process is successfully completed, SQLITE_OK is returned.
** Otherwise, if an error occurs, an SQLite error code is returned.
*/

TC_API void tdsNDB_backup_ssp(NMParams p) // totalcross/db/sqlite/NativeDB native int backup(String dbName, String destFileName, totalcross.db.sqlite.DB.ProgressObserver observer) throws SQLException;
{
   TRACE("tdsNDB_backup_ssp")
   TCObject this_ = p->obj[0];
   TCObject dbName = p->obj[1];
   TCObject destFileName = p->obj[2];
   TCObject observer = p->obj[3];
  int rc;                     /* Function return code */
  sqlite3* pDb;               /* Database to back up */
  sqlite3* pFile;             /* Database connection opened on zFilename */
  sqlite3_backup *pBackup;    /* Backup handle used to copy data */
  char *dFileName;
  char *dDBName;
   LOCKDB

  pDb = gethandle(p->currentContext, this_);

  dFileName = newSafeString(destFileName);
  dDBName = newSafeString(dbName);

  /* Open the database file identified by dFileName. */
  rc = sqlite3_open(dFileName, &pFile);
  if( rc==SQLITE_OK )
  {
    /* Open the sqlite3_backup object used to accomplish the transfer */
    pBackup = sqlite3_backup_init(pFile, "main", pDb, dDBName);
    if( pBackup )
    {
	   while((rc = sqlite3_backup_step(pBackup,100))==SQLITE_OK ) 
      {
         reportProgress(p->currentContext, observer, sqlite3_backup_remaining(pBackup), sqlite3_backup_pagecount(pBackup));
      }
      /* Release resources allocated by backup_init(). */
      reportProgress(p->currentContext, observer, sqlite3_backup_remaining(pBackup), sqlite3_backup_pagecount(pBackup));
      (void)sqlite3_backup_finish(pBackup);
    }
    rc = sqlite3_errcode(pFile);
  }
  /* Close the database connection opened on database file zFilename
  ** and return the result of this function. */
  (void)sqlite3_close(pFile);
  freeSafeString(dFileName);
  freeSafeString(dDBName);

  p->retI = rc;
   UNLOCKDB
}

TC_API void tdsNDB_restore_ssp(NMParams p) // totalcross/db/sqlite/NativeDB native int restore(String dbName, String sourceFileName, totalcross.db.sqlite.DB.ProgressObserver observer) throws SQLException;
{
   TRACE("tdsNDB_restore_ssp")
   TCObject this_ = p->obj[0];
   TCObject dbName = p->obj[1];
   TCObject sourceFileName = p->obj[2];
   TCObject observer = p->obj[3];
  int32 rc;                     /* Function return code */
  sqlite3* pDb;               /* Database to back up */
  sqlite3* pFile;             /* Database connection opened on zFilename */
  sqlite3_backup *pBackup;    /* Backup handle used to copy data */
  char *dFileName;
  char *dDBName;
  int32 nTimeout = 0;
   LOCKDB

  pDb = gethandle(p->currentContext, this_);

  dFileName = newSafeString(sourceFileName);
  dDBName = newSafeString(dbName);

  /* Open the database file identified by dFileName. */
  rc = sqlite3_open(dFileName, &pFile);
  if( rc==SQLITE_OK )
  {
    /* Open the sqlite3_backup object used to accomplish the transfer */
    pBackup = sqlite3_backup_init(pDb, dDBName, pFile, "main");
    if( pBackup )
    {
	    while( (rc = sqlite3_backup_step(pBackup,100))==SQLITE_OK || rc==SQLITE_BUSY  )
       {
         reportProgress(p->currentContext, observer, sqlite3_backup_remaining(pBackup), sqlite3_backup_pagecount(pBackup));
     	 	if( rc==SQLITE_BUSY )
         {
        		if( nTimeout++ >= 3 ) break;
	        	sqlite3_sleep(100);
    		}
	    }
      /* Release resources allocated by backup_init(). */
      reportProgress(p->currentContext, observer, sqlite3_backup_remaining(pBackup), sqlite3_backup_pagecount(pBackup));
      (void)sqlite3_backup_finish(pBackup);
    }
    rc = sqlite3_errcode(pFile);
  }

  /* Close the database connection opened on database file zFilename
  ** and return the result of this function. */
  (void)sqlite3_close(pFile);
  freeSafeString(dFileName);
  freeSafeString(dDBName);
  p->retI = rc;
   UNLOCKDB
}

