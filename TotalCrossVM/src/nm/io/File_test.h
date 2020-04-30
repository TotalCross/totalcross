// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if defined(WIN32) || defined (WINCE)
 const TCHARP TEMP_PATH = TEXT("TC_TEST");
#elif defined(linux)
 const TCHARP TEMP_PATH = TEXT("/tmp");
#elif defined(ANDROID)
 const TCHARP TEMP_PATH = TEXT("/data/data/totalcross.android");
#endif

/*
 * Creates a file Object.
 */
TCHAR buf[128];
TCObject createFile(Context currentContext, const TCHARP dir, const TCHARP fileName, TCObject* outPath)
{
   TCObject obj;
   obj = createObject(currentContext,"totalcross.io.File");
   setObjectLock(obj, UNLOCKED);
   if (obj != null)
   {
      tcscpy(buf, dir);
      if (fileName != null)
      {
         if (dir[tcslen(dir) - 1] != '/' && fileName[0] != '/')
            tcscat(buf, TEXT("/"));
         tcscat(buf, fileName);
      }

      *outPath = File_path(obj) = createStringObjectFromTCHAR(currentContext, buf, -1);
      //setObjectLock(File_path(obj), UNLOCKED);
   }
   else currentContext->thrownException = null;
   return obj;
}

/*
 * Create a time Object.
 */
TCObject createTime(Context currentContext, int32 year, int32 month, int32 day, int32 hour, int32 minute, int32 second, int32 millisecond)
{
   TCObject obj = createObject(currentContext, "totalcross.sys.Time");
   setObjectLock(obj, UNLOCKED);
   if (obj != null)
   {
      Time_year(obj) = year;
      Time_month(obj) = month;
      Time_day(obj) = day;
      Time_hour(obj) = hour;
      Time_minute(obj) = minute;
      Time_second(obj) = second;
      Time_millis(obj) = millisecond;
   }

   return obj;
}

TCObject CharP2Buf(Context currentContext, CharP bytes, int len)
{
   TCObject arrayObj;
   CharP array;

   if (len <= 0 && bytes != null)
      len = xstrlen(bytes);
   if (len <= 0)
      return null;

   arrayObj = createByteArray(currentContext, len);
   setObjectLock(arrayObj, UNLOCKED);
   if (arrayObj != null)
   {
      array = (CharP) ARRAYOBJ_START(arrayObj);
      if (bytes != null)
         xmemmove(array, bytes, len); // copy bytes
   }

   return arrayObj;
}

TESTCASE(tiF_isCardInserted_i) // totalcross/io/File native public static boolean isCardInserted(int slot);
{
   TNMParams p;
   int32 i32Array[1];
   p.currentContext = currentContext;
   p.i32 = i32Array;

   p.i32[0] = lastVolume;
   tiF_isCardInserted_i(&p);
   ASSERT1_EQUALS(True, p.retI);

   finish:
      ;
}
TESTCASE(tiF_create_sii) // totalcross/io/File native private void create(String path, int mode, int slot); #DEPENDS(tiF_isCardInserted_i)
{
   TNMParams p, tempDirParams;
   TCObject objArray[2], tempDirObj[2], path;
   int32 i32Array[2], tempDirI32[2];
#if 0//def WIN32
   char msg[128];
#endif

   p.currentContext = currentContext;

   // Creates the temporary directory to hold all test files
   p.currentContext = tempDirParams.currentContext = currentContext;
   tempDirParams.obj = tempDirObj;
   tempDirParams.i32 = tempDirI32;

   tempDirParams.obj[0] = createFile(currentContext, TEMP_PATH, null, &path);
   ASSERT1_EQUALS(NotNull, tempDirParams.obj[0]);

   tempDirParams.obj[1] = File_path(tempDirParams.obj[0]);
   tempDirParams.i32[0] = DONT_OPEN;
   tempDirParams.i32[1] = lastVolume;
   tiF_create_sii(&tempDirParams);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_createDir(&tempDirParams);
   //ASSERT1_EQUALS(Null, currentContext->thrownException); - the folder may already exist.
   // **** //

   p.obj = objArray;
   p.i32 = i32Array;

   // ----- FILE -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_create_sii.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // #1 - Invalid path
   p.obj[1] = createStringObjectFromCharP(currentContext, "/thisfolderdoesnotexists/file.test", -1);
   setObjectLock(p.obj[1], UNLOCKED);
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // #4 - open file that does not exists with mode DONT_OPEN
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_exists(&p);

   // If p.retI is true, then the test has failed, besaucse "create" should not return true
   if (p.retI)
   {
      tiF_delete(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
   }

   currentContext->thrownException = null;

   // #4 - open file that does not exists with mode READ_WRITE
   p.obj[1] = path;
   p.i32[0] = READ_WRITE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[FileNotFoundException]);
   currentContext->thrownException = null;

   // #4 - create file with mode CREATE
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException); // assert file was closed

   // #5 - create file with mode CREATE_EMPTY
   p.obj[1] = path;
   p.i32[0] = CREATE_EMPTY;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file (deleting)
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException); // assert file was closed

   // #7 - open file with mode READ_WRITE
   p.obj[1] = path;
   p.i32[0] = READ_WRITE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException); // assert file was closed

   // #7 - open file with mode DONT_OPEN
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI); // assert file exists

   // ----- DIR -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_create_sii"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   p.obj[1] = path;
   p.i32[0] = DONT_OPEN; // dir can never be opened
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_delete(&p); // delete dir, if already exists

   currentContext->thrownException = null;
   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_create_sii"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN; // dir can never be opened
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_createDir(&p); // create dir
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI); // assert dir exists

   finish:
      ;
}
TESTCASE(tiF_close) // totalcross/io/File native private void close(); #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // ARCHIVE

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_close.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // #1 - Create and close archive.
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException); // assert file was closed

   // #2 - Open archive with READ_WRITE and close.
   p.obj[1] = path;
   p.i32[0] = READ_WRITE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException); // assert file was closed

   // #3 - Open archive with DONT_OPEN and close.
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException); // assert file was closed

   // #4 - Open archive with DONT_OPEN and delete.
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_delete(&p);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #5 - Create and delete the archive.
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // DIRECTORY
   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_nativeCloseDir"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);

   // #6 - Create directory and close file.
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file instance (not physically)
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_delete(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_createDir(&p); // create dir
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #7 - Delete directory and close file.
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(True, p.retI);

   tiF_delete(&p);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   finish:
      ;
}
TESTCASE(tiF_createDir) // totalcross/io/File native public boolean createDir(); #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_createDir"),&path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   if (!p.retI) // directory exists? skip the test, because we would have to recursively delete the directory, what isn't allowed yet
   {
      p.obj[1] = path;
      p.i32[0] = DONT_OPEN;
      p.i32[1] = lastVolume;
      tiF_createDir(&p); // create dir
      ASSERT1_EQUALS(Null, currentContext->thrownException);

      tiF_isDir(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
      ASSERT1_EQUALS(True, p.retI); // assert dir exists
   }
   finish:
      ;
}
TESTCASE(tiF_delete) // totalcross/io/File native public void delete();  #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // ----- FILE -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_delete.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_exists(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;
   tiF_delete(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   // ----- DIR -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_delete"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_createDir(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_delete(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   finish:
      ;
}
TESTCASE(tiF_exists) // totalcross/io/File native public boolean exists();  #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // ----- FILE -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_exists.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);
   tiF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_exists(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_exists.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(False, p.retI);
   tiF_delete(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // ----- DIR -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_exists"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   if (!p.retI)
   {
      tiF_createDir(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
   }

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);
   tiF_delete(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_exists(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_exists.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(False, p.retI);
   tiF_delete(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   finish:
      ;
}
TESTCASE(tiF_getSize) // totalcross/io/File native public int getSize();      #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // ----- FILE -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_getSize.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE_EMPTY;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_getSize(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[1] = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = 0; // offset
   p.i32[1] = ARRAYOBJ_LEN(p.obj[1]); // len
   tiF_writeBytes_Bii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);
   tiF_getSize(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);
   tiF_getSize(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   // ----- ROOT -----
#ifndef WP8
   p.obj[0] = createFile(currentContext, TEXT("/"), null, &path);

   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_getSize(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI >= 0); // assert volume size is greater than zero
#endif
   finish:
      ;
}
TESTCASE(tiF_isDir) // totalcross/io/File native public boolean isDir();     #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // ----- FILE -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_isDir.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_isDir(&p);
   ASSERT1_EQUALS(False, p.retI);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_isDir.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_isDir(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(False, p.retI);
   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // ----- DIR -----

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_isDir"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_exists(&p);
   if (!p.retI)
   {
      tiF_createDir(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
   }
   tiF_isDir(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);

   finish:
      currentContext->thrownException = null;
}
TESTCASE(tiF_listFiles) // totalcross/io/File native public String []listFiles();  #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   TCObject* list;
   int32 count;
   TCHARP s;
   bool found;
   tzero(p);
   p.currentContext = currentContext;
   p.obj = objArray;
   p.i32 = i32Array;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_ListFiles.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   p.obj[0] = createFile(currentContext, TEMP_PATH, null, &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_listFiles(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT1_EQUALS(True, (count = ARRAYOBJ_LEN(p.retO)) > 0);

   list = (TCObject*) ARRAYOBJ_START(p.retO);
   found = false;
   while (--count >= 0 && !found)
   {
      s = JCharP2TCHARP(String_charsStart(list[count]), String_charsLen(list[count]));
      if (tcscmp(s, TEXT("tiF_ListFiles.test")) == 0)
         found = true;
      xfree(s);
   }

   ASSERT1_EQUALS(True, found); // assert "tiF_ListFiles.test" was found

   finish:
      ;
}
TESTCASE(tiF_rename_s) // totalcross/io/File native public boolean rename(String path);  #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   TCObject renPath;

   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   // #1 Try to delete a file left from previous tests.
   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_rename_s.test.renamed"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = renPath = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tiF_delete(&p); // no assert please! the file may not have been left
   currentContext->thrownException = null;

   // #2 Create file to be renamed.
   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_rename_s.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE_EMPTY;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #3 Try to rename the file with a null argument.
   p.obj[0] = p.obj[0];
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = null;
   tiF_rename_s(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[NullPointerException]);
   currentContext->thrownException = null;

   // #4 Rename the file.
   p.obj[0] = p.obj[0];
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = renPath;
   tiF_rename_s(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #6 Open the file using the new name
   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_rename_s.test.renamed"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #7 Confirms file with new name exists.
   tiF_exists(&p);
   ASSERT1_EQUALS(True, p.retI);

   // #8 Open the file using the old name.
   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_rename_s.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = DONT_OPEN;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #9 Confirms file with old name does not exist.
   tiF_exists(&p);
   ASSERT1_EQUALS(False, p.retI);

   finish:
      ;
}
TESTCASE(tiF_writeBytes_Bii) // totalcross/io/File native public int writeBytes(byte []b, int off, int len);   #DEPENDS(tiF_create_sii)
{
   TNMParams rbP, wbP;
   TCObject readBytesObj[2], writeBytesObj[2],path;
   int32 readBytesI32[2], writeBytesI32[2];
   TCObject rbBuf, wbBuf;

   rbP.currentContext = currentContext;
   wbP.currentContext = currentContext;

   //Prepare the params
   rbP.obj = readBytesObj;
   rbP.i32 = readBytesI32;
   wbP.obj = writeBytesObj;
   wbP.i32 = writeBytesI32;

   // #1 Create new empty file.
   wbP.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_writeBytes_Bii.test"), &path);
   ASSERT1_EQUALS(NotNull, wbP.obj[0]);
   wbP.obj[1] = File_path(wbP.obj[0]);
   wbP.i32[0] = CREATE_EMPTY;
   wbP.i32[1] = lastVolume;
   tiF_create_sii(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #2 Check the file size.
   tiF_getSize(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, wbP.retI);

   // #3 Close the file.
   tiF_close(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #4 Try to write on the closed file.
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = 0;
   wbP.i32[1] = 10;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   // #5 Try to check the file size.
   tiF_getSize(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   // #9 Close the file.
   tiF_close(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #10 Create in mode DONT_OPEN.
   wbP.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_writeBytes_Bii.test"), &path);
   ASSERT1_EQUALS(NotNull, wbP.obj[0]);
   wbP.obj[1] = File_path(wbP.obj[0]);
   wbP.i32[0] = DONT_OPEN;
   wbP.i32[1] = lastVolume;
   tiF_create_sii(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #11 Check the file size.
   tiF_getSize(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // #13 Create file in mode CREATE.
   wbP.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_writeBytes_Bii.test"), &path);
   ASSERT1_EQUALS(NotNull, wbP.obj[0]);
   wbP.obj[1] = File_path(wbP.obj[0]);
   wbP.i32[0] = CREATE;
   wbP.i32[1] = lastVolume;
   tiF_create_sii(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #14 Try to write in mode CREATE passing a null array.
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = null;
   wbP.i32[0] = 0;
   wbP.i32[1] = 10;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[NullPointerException]);
   currentContext->thrownException = null;

   // #15 Same file, now trying to write with non-negative offset value.
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = -1;
   wbP.i32[1] = 10;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;

   // #16 Same file, now trying to write with non-positive length value.
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = 0;
   wbP.i32[1] = 0;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, wbP.retI);

   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = 0;
   wbP.i32[1] = -1;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;

   // #17 Same file, now trying to write without respecting the array limits.
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = 2;
   wbP.i32[1] = 12;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;

   // #18 Same file, now trying to write correctly.
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = 2;
   wbP.i32[1] = 6;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 6, wbP.retI);

   // #19 Check the file size.
   tiF_getSize(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 6, wbP.retI);

   // #20 Same file, writing a few more bytes
   wbP.obj[0] = wbP.obj[0];
   wbP.obj[1] = wbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, wbP.obj[1]);
   wbP.i32[0] = 0;
   wbP.i32[1] = 5;
   tiF_writeBytes_Bii(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 5, wbP.retI);

   // #20 Check the file size.
   tiF_getSize(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 11, wbP.retI);

   // #21 Close and reopen the file
   tiF_close(&wbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   rbP.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_writeBytes_Bii.test"), &path);
   ASSERT1_EQUALS(NotNull, rbP.obj[0]);
   rbP.obj[1] = File_path(rbP.obj[0]);
   rbP.i32[0] = READ_WRITE;
   rbP.i32[1] = lastVolume;
   tiF_create_sii(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #22 Check the file size.
   tiF_getSize(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 11, rbP.retI);

   // #23 Try to read passing a null array.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = null;
   rbP.i32[0] = 0;
   rbP.i32[1] = 10;
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[NullPointerException]);
   currentContext->thrownException = null;

   // #24 Same file, now trying to read with non-negative offset value.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = -1;
   rbP.i32[1] = 10;
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;

   // #25 Same file, now trying to read with length <= 0
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 0;
   rbP.i32[1] = 0;
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 0, rbP.retI);
   currentContext->thrownException = null;

   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 0;
   rbP.i32[1] = -1;
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;

   // #26 Same file, now trying to read without respecting the array limits.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, "0123456789", 0);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 2;
   rbP.i32[1] = 12;
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[ArrayIndexOutOfBoundsException]);
   currentContext->thrownException = null;

   // #27 Same file, now trying to read correctly.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, null, 5);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 0;
   rbP.i32[1] = ARRAYOBJ_LEN(rbP.obj[1]);
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 5, rbP.retI);
   ASSERT2_EQUALS(I32, 0, xstrncmp(ARRAYOBJ_START(rbBuf), "23456", 5));

   // #28 Same file, now reading more bytes than it actually has.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, null, 10);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 0;
   rbP.i32[1] = ARRAYOBJ_LEN(rbP.obj[1]);
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, 6, rbP.retI);
   ASSERT2_EQUALS(I32, 0, xstrncmp(ARRAYOBJ_START(rbBuf), "701234", 6));

   // #29 Same file, trying to read at EOF.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, null, 10);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 0;
   rbP.i32[1] = ARRAYOBJ_LEN(rbP.obj[1]);
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, -1, rbP.retI);

   // #30 Try to move the file pointer with negative pos.
   rbP.obj[0] = rbP.obj[0];
   rbP.i32[0] = -1;
   tiF_setPos_i(&rbP);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;

   // #31 Try to move the file pointer beyond the file size.
   rbP.obj[0] = rbP.obj[0];
   rbP.i32[0] = 12;
   tiF_setPos_i(&rbP);
   
#ifndef WP8
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT2_EQUALS(Sz, OBJ_CLASS(currentContext->thrownException)->name, throwableAsCharP[IOException]);
   currentContext->thrownException = null;
#endif

   // #32 Moving the file pointer back to the beggining of the file
   rbP.obj[0] = rbP.obj[0];
   rbP.i32[0] = 0;
   tiF_setPos_i(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // #33 Now read everything.
   rbP.obj[0] = rbP.obj[0];
   rbP.obj[1] = rbBuf = CharP2Buf(currentContext, null, 20);
   ASSERT1_EQUALS(NotNull, rbP.obj[1]);
   rbP.i32[0] = 0;
   rbP.i32[1] = ARRAYOBJ_LEN(rbP.obj[1]);
   tiF_readBytes_Bii(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

#ifndef WP8
   ASSERT2_EQUALS(I32, 11, rbP.retI); 
#endif

   ASSERT2_EQUALS(I32, 0, xstrncmp(ARRAYOBJ_START(rbBuf), "23456701234", 11)); 

   // #34 Close the file.
   tiF_close(&rbP);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   finish:
      ;
}
TESTCASE(tiF_setAttributes_i) // totalcross/io/File native public void setAttributes(int attr); #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_setAttributes_i.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   for (p.i32[0] = 0; p.i32[0] < 8; p.i32[0] ++) // do not test ATTR_SYSTEM (may fail in some platforms)
   {
      tiF_setAttributes_i(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
      tiF_getAttributes(&p);
      ASSERT1_EQUALS(Null, currentContext->thrownException);
#if !defined (WIN32) && !defined (PALMOS) // POSIX does not support HIDDEN and ARCHIVE attribues
      if (p.i32[0] & 1)
    	  p.retI |= 1;
      if (p.i32[0] & 2)
    	  p.retI |= 2;
#endif
      ASSERT2_EQUALS(I32, p.i32[0], p.retI & p.i32[0]);
   }

   p.i32[0] = 0;
   tiF_setAttributes_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   finish:
      ;
}
TESTCASE(tiF_setTime_bt) // totalcross/io/File native public void setTime(byte whichTime, totalcross.sys.Time time);  #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   TCObject time, t1, t2, t3;

   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_setTime_bt.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE_EMPTY;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   p.obj[1] = time = createTime(currentContext, 1984, 10, 16, 12, 35, 0, 0);
   ASSERT1_EQUALS(NotNull, p.obj[1]);
   p.i32[0] = TIME_ALL;
   tiF_setTime_bt(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   p.i32[0] = TIME_CREATED;
   tiF_getTime_b(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, p.retO);
   t1 = p.retO;
   p.i32[0] = TIME_MODIFIED;
   tiF_getTime_b(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, p.retO);
   t2 = p.retO;
   p.i32[0] = TIME_ACCESSED;
   tiF_getTime_b(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(NotNull, p.retO);
   t3 = p.retO;

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

#if defined (WIN32) || defined (PALMOS) // POSIX does not support creation time of files
   // time created
   ASSERT2_EQUALS(I32, Time_year(time), Time_year(t1));
   ASSERT2_EQUALS(I32, Time_month(time), Time_month(t1));
   ASSERT2_EQUALS(I32, Time_day(time), Time_day(t1));
   ASSERT2_EQUALS(I32, Time_hour(time), Time_hour(t1));
   ASSERT2_EQUALS(I32, Time_minute(time), Time_minute(t1));
   ASSERT2_EQUALS(I32, Time_second(time), Time_second(t1));
#endif

   // time modified
   ASSERT2_EQUALS(I32, Time_year(time), Time_year(t2));
   ASSERT2_EQUALS(I32, Time_month(time), Time_month(t2));
   ASSERT2_EQUALS(I32, Time_day(time), Time_day(t2));
   ASSERT2_EQUALS(I32, Time_hour(time), Time_hour(t2));
   ASSERT2_EQUALS(I32, Time_minute(time), Time_minute(t2));
   ASSERT2_EQUALS(I32, Time_second(time), Time_second(t2));

   // time accessed
   ASSERT2_EQUALS(I32, Time_year(time), Time_year(t3));
   ASSERT2_EQUALS(I32, Time_month(time), Time_month(t3));
   ASSERT2_EQUALS(I32, Time_day(time), Time_day(t3));
   
#ifdef WP8   
   ASSERT2_EQUALS(I32, Time_hour(time), Time_hour(t3));
   ASSERT2_EQUALS(I32, Time_minute(time), Time_minute(t3));
   ASSERT2_EQUALS(I32, Time_second(time), Time_second(t3));
#endif

   finish:
      ;
}

TESTCASE(tiF_setSize_i) // totalcross/io/File native public void setSize(int newSize); #DEPENDS(tiF_create_sii)
{
   TNMParams p;
   TCObject objArray[2],path;
   int32 i32Array[2];
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createFile(currentContext, TEMP_PATH, TEXT("tiF_setSize_i.test"), &path);
   ASSERT1_EQUALS(NotNull, p.obj[0]);
   p.obj[1] = path;
   p.i32[0] = CREATE_EMPTY;
   p.i32[1] = lastVolume;
   tiF_create_sii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   p.i32[0] = 10;
   tiF_setSize_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_getSize(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, p.i32[0], p.retI);

   p.i32[0] = 5;
   tiF_setSize_i(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_getSize(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, p.i32[0], p.retI);

   tiF_close(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   finish:
      ;
}
