#include "tcvm.h"
#undef MIN
#undef MAX
#define SQLITE_ENABLE_COLUMN_METADATA
#define SQLITE_ENABLE_FTS3
#define SQLITE_ENABLE_LOAD_EXTENSION 1 
#define SQLITE_ENABLE_UPDATE_DELETE_LIMIT 
#define SQLITE_ENABLE_FTS3_PARENTHESIS 
#define SQLITE_ENABLE_RTREE 
#define SQLITE_ENABLE_STAT2 
#define SQLITE_ENABLE_UNLOCK_NOTIFY
#define SQLITE_THREADSAFE 1
#define SQLITE_THREAD_OVERRIDE_LOCK 0
#ifdef WP8
#define SQLITE_OMIT_WAL 1
#define SQLITE_OS_WINRT 1
#endif

#if defined TOTALCROSS && defined POSIX && !defined darwin && !defined android
#include <linux/mman.h>
#endif

/* other changes to work with totalcross:

///////////////////////////////////////////////////////////////////////////////////////
1.

static const char *unixTempFileDir(void){
#if defined(ANDROID) || defined(darwin) || defined(POSIX) // guich: use the app path
   return appPath;
#else
...
#endif

///////////////////////////////////////////////////////////////////////////////////////
2.

SQLITE_API int sqlite3_reset(sqlite3_stmt *pStmt){
  int rc;
  if( pStmt==0 ){
    rc = SQLITE_OK;
  }else{
    Vdbe *v = (Vdbe*)pStmt;
    if (v->db==0) return SQLITE_OK; // guich: db may be closed
...

///////////////////////////////////////////////////////////////////////////////////////
3.
struct sqlite3 {
...
 MUTEX_TYPE tcmutex;
};

int32 lockSqlite3(void* handle)
{
   if (handle)
   {
      sqlite3* db = (sqlite3*)handle;
      RESERVE_MUTEX_VAR(db->tcmutex);
   }
   return 0;
}

void unlockSqlite3(void* handle)
{
   if (handle)
   {
      sqlite3* db = (sqlite3*)handle;
      RELEASE_MUTEX_VAR(db->tcmutex);
   }
}

void initSqlite3Mutex(void* db_)
{
   sqlite3* db = (sqlite3*)db_;
   SETUP_MUTEX;
   INIT_MUTEX_VAR(db->tcmutex);
}

void destroySqlite3Mutex(void* db_)
{
   sqlite3* db = (sqlite3*)db_;
   DESTROY_MUTEX_VAR(db->tcmutex);
}
///////////////////////////////////////////////////////////////////////////////////////


*/