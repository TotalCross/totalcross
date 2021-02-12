#include "tcvm.h"
#undef MIN
#undef MAX
#define SQLITE_ENABLE_COLUMN_METADATA 1
#define SQLITE_ENABLE_FTS3 1
#define SQLITE_ENABLE_LOAD_EXTENSION 1 
#define SQLITE_ENABLE_UPDATE_DELETE_LIMIT 1
#define SQLITE_ENABLE_FTS3_PARENTHESIS 1
#define SQLITE_ENABLE_RTREE 1
#define SQLITE_ENABLE_STAT2 1
#define SQLITE_ENABLE_UNLOCK_NOTIFY 1
#define SQLITE_THREADSAFE 0
#define SQLITE_THREAD_OVERRIDE_LOCK 0
#define SQLITE_WITHOUT_MSIZE 1
#define SQLITE_MUTEX_OMIT
#ifdef WP8
#define SQLITE_WIN32_FILEMAPPING_API 1
#define SQLITE_OMIT_WAL 1
#define SQLITE_OS_WINRT 1
#endif

#if defined TOTALCROSS && defined POSIX && !defined __APPLE__ && !defined ANDROID
#include <linux/mman.h>
#endif

#if !defined(FORCE_LIBC_ALLOC)
#undef malloc
#undef free
#undef realloc
#define malloc xmalloc
#define free xfree
#define realloc xrealloc
#endif

/* other changes to work with totalcross:

0. On top of sqlite3.c: 

#include "sqlite3_tc.h"

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
struct sqlite3 {
...
 MUTEX_TYPE tcmutex;
 bool validMutex;
};

int32 lockSqlite3(void* handle)
{
   if (handle)
   {
      sqlite3* db = (sqlite3*)handle;
      if (db->validMutex)
      {RESERVE_MUTEX_VAR(db->tcmutex);}
   }
   return 0;
}

void unlockSqlite3(void* handle)
{
   if (handle)
   {
      sqlite3* db = (sqlite3*)handle;
      if (db->validMutex)
      {RELEASE_MUTEX_VAR(db->tcmutex);}
   }
}

void initSqlite3Mutex(void* db_)
{
   sqlite3* db = (sqlite3*)db_;
   SETUP_MUTEX;
   INIT_MUTEX_VAR(db->tcmutex);
   db->validMutex = true;
}

void destroySqlite3Mutex(void* db_)
{
   sqlite3* db = (sqlite3*)db_;
   DESTROY_MUTEX_VAR(db->tcmutex);
   db->validMutex = false;
}
///////////////////////////////////////////////////////////////////////////////////////


*/