// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "File.h"

#if defined WINCE || defined WIN32
 #include "win/File_c.h"
#else
 #include "posix/File_c.h"
#endif

TC_API bool validatePath(TCHARP path)
{
   TCHARP auxP;
   int32 pathLen, i;
   
   if (path == null)
      return false;
   pathLen = tcslen(path);
   if (pathLen == 0)
      return true;
   for (i = pathLen - 1 ; i >= 0 ; i--)
      if (path[i] == '\\')
         path[i] = '/';
   if (path[0] == '.')
   {
      if (pathLen == 1 || (pathLen == 2 && path[1] == '.'))
         return false;
      if (pathLen > 1 && path[1] == '/')
         return false;
      if (pathLen > 2 && path[1] == '.' && path[2] == '/')
         return false;
   }
   auxP = tcsstr(path, TEXT("/."));
   if (auxP && (auxP[2] == 0 || auxP[2] == '/' || (auxP[2] == '.' && (auxP[3] == 0  || auxP[3] == '/'))))
      return false;
   return true;
}

static void invalidate(TCObject file)
{
   if (File_fileRef(file) != null)
   {
      setObjectLock(File_fileRef(file), UNLOCKED);
      File_fileRef(file) = null;
   }
   File_mode(file) = INVALID;
   File_dontFinalize(file) = true;
}

static void mark_closed(TCObject file)
{
   if (File_fileRef(file) != null)
   {
      setObjectLock(File_fileRef(file), UNLOCKED);
      File_fileRef(file) = null;
   }
   File_mode(file) = CLOSED;
   File_dontFinalize(file) = true;
}

#ifdef ANDROID
bool replacePath(NMParams p, char* szPath, bool throwEx)
{
   if (xstrncmp(szPath,"/sdcard",7) == 0)
   {
      char path2[MAX_PATHNAME];
      char n = szPath[7] - '0';
      if (!getSDCardPath(path2,n))
      {                                                
         if (throwEx)
            throwException(p->currentContext, IOException, "Card not inserted.");
         return false;
      }   
      xstrcat(path2, &szPath[0 <= n && n <= 9 ? 8 : 7]);
      xstrcpy(szPath, path2);
   }
   return true;
}
#else
#define replacePath(x,y,z) true
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tiF_getDeviceAlias(NMParams p) // totalcross/io/File native private static String getDeviceAlias();
{
#if defined (darwin)
   p->retO = createStringObjectFromCharP(p->currentContext, appPath, -1);
   setObjectLock(p->retO, UNLOCKED);
#else
   p->retO = null;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_isCardInserted_i(NMParams p) // totalcross/io/File native public static boolean isCardInserted(int slot);
{
   int32 slot = p->i32[0];

   IntBuf intBuf;

   if (slot < -1)
      throwIllegalArgumentIOException(p->currentContext, "slot", int2str(slot, intBuf));
   else
      p->retI = fileIsCardInserted(slot);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_create_sii(NMParams p) // totalcross/io/File native private void create(String path, int mode, int slot) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject path = p->obj[1];
   int32 mode  = p->i32[0];
   TCObject fileRef = null;
   NATIVE_FILE* natFile = null;
   TCHAR szPath[MAX_PATHNAME];
   Err err;

#ifdef ENABLE_TEST_SUITE
   File_slot(file) = slot;
   File_path(file) = path;
   File_mode(file) = mode;
#endif

   if (mode != DONT_OPEN)
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (IS_DEBUG_CONSOLE(szPath))
         mode = READ_ONLY;
      if (!replacePath(p,szPath,true))
      {
         invalidate(file);
         return;
      }
      if (mode != CREATE && mode != CREATE_EMPTY && !fileExists(szPath, File_slot(file)))
         throwFileNotFoundException(p->currentContext, szPath);
      else
      if ((fileRef = createByteArray(p->currentContext, sizeof(NATIVE_FILE))) != null) // created fileRef will be unlocked only in native close
      {
         File_fileRef(file) = fileRef;
         natFile = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
         if ((err = fileCreate(natFile, szPath, mode, &File_slot(file))) != NO_ERROR)
         {
            throwExceptionWithCode(p->currentContext, IOException, err);
            invalidate(file);
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_close(NMParams p) // totalcross/io/File native public void close() throws totalcross.io.IOException;
{
   TCObject file, fileRef;
   NATIVE_FILE* fref;
   Err err;
   int32 mode;

   file = p->obj[0];
   fileRef = File_fileRef(file);
   mode = File_mode(file);
   if (mode == CLOSED || mode == DONT_OPEN)
   {
      return;
   }
   if (mode == INVALID)
   {
      throwException(p->currentContext, IOException, "Invalid file object.");
   }
   if (fileRef) // don't remove this!
   {
      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileClose(fref)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      mark_closed(file);
   }
}
//////////////////////////////////////////////////////////////////////////
#ifdef WIN32
#define FILE_EXISTS 183
#else
#define FILE_EXISTS 17
#endif
int createDirRec(NMParams p, TCHARP szPath, int stringSize, int slot)
{
   TCHARP c;
   int nStringSize;
   Err err;

   if (fileExists(szPath, slot))
      return 0;

   for (nStringSize = stringSize, c = szPath + stringSize - 1; c >= szPath; c--, nStringSize--)
      if (*c == '/')
      {
          *c = 0;
          if (!createDirRec(p, szPath, nStringSize, slot))
          {
             *c = '/';
             if ((err = fileCreateDir(szPath, slot)) != NO_ERROR && err != FILE_EXISTS) // ignore if EEXIST
             {
                throwExceptionWithCode(p->currentContext, IOException, err);
                return 1;
             }
             else
                return 0;
          }
          return 1;
      }
  
   if ((err = fileCreateDir(szPath, slot)) != NO_ERROR && err != FILE_EXISTS)
   {
      throwExceptionWithCode(p->currentContext, IOException, err);
      return 1;
   }
   else
      return 0;
}

TC_API void tiF_createDir(NMParams p) // totalcross/io/File native public void createDir() throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   int32 mode = File_mode(file);
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode != DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation can ONLY be used in mode DONT_OPEN.");
   else
   {
      int stringSize = String_charsLen(path);
      JCharP2TCHARPBuf(String_charsStart(path), stringSize, szPath);
      if (!replacePath(p,szPath,true))
         return;
      if (fileExists(szPath, slot))
         throwException(p->currentContext, IOException, "Directory already exists.");
      else
         createDirRec(p, szPath, stringSize, slot); // this recursion will throw the exception
   }
}


//////////////////////////////////////////////////////////////////////////
void closeDebug();

TC_API void tiF_delete(NMParams p) // totalcross/io/File native public void delete() throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;
      if (!fileExists(szPath, slot))
         throwFileNotFoundException(p->currentContext, szPath);
      else
      {
         if (IS_DEBUG_CONSOLE(szPath)) // guich@tc100b5_39: close the debug console if we're trying to delete it
            closeDebug();
         fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
         if ((err = fileDelete(fref, szPath, slot, mode != DONT_OPEN)) != NO_ERROR)
            throwExceptionWithCode(p->currentContext, IOException, err);
      }
      mark_closed(file);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_exists(NMParams p) // totalcross/io/File native public boolean exists();
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   int32 mode = File_mode(file);
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,false))
         p->retI = false;
      else
         p->retI = fileExists(szPath, slot);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_getSize(NMParams p) // totalcross/io/File native public int getSize() throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];
   int32 size;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   {
      int32 len;
      JCharP2TCHARPBuf(String_charsStart(path), len=String_charsLen(path), szPath);
      if (len > 0 && szPath[len-1] == '/')
      {
         if ((err = fileGetFreeSpace(szPath, &size, slot)) != NO_ERROR)
            throwExceptionWithCode(p->currentContext, IOException, err);
         else
            p->retI = size;
      }
      else
      if (mode == DONT_OPEN)
         throwException(p->currentContext, IOException, "The file can't be open in the DONT_OPEN mode to get its size.");
      else
      {
         fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);

         if ((err = fileGetSize(*fref, szPath, &size)) != NO_ERROR)
            throwExceptionWithCode(p->currentContext, IOException, err);
         else
            p->retI = size;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_isDir(NMParams p) // totalcross/io/File native public boolean isDir();
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   int32 mode = File_mode(file);
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode != DONT_OPEN)
      p->retI = false;
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;
      if (fileExists(szPath, slot))
         p->retI = fileIsDir(szPath, slot);
      else
         p->retI = false;

// Used to throw FileNotFoundException
//      if (!fileExists(szPath, *slot))
//         throwFileNotFoundException(p->currentContext, szPath);
//      else
//         p->retI = fileIsDir(szPath, *slot);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_listFiles(NMParams p) // totalcross/io/File native public String []listFiles() throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   int32 mode = File_mode(file);
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];
   TCHARPs* list=null;
   int32 count=0;
   volatile TCObject arrayObj = null;
   TCObjectArray start, end;
   Err err;
   volatile Heap h;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode != DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation can ONLY be used in mode DONT_OPEN.");
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;
#if defined(WIN32) && !defined(WINCE)
      if (tcsstr(szPath,TEXT("System Volume Information"))) // guich@tc110_24
         return;
#endif
      h = heapCreate();
      IF_HEAP_ERROR(h)
      {
         heapDestroy(h);
         throwException(p->currentContext, OutOfMemoryError, null);
         return;
      }

      if (!fileExists(szPath, slot)) //flsobral@tc125_27: should now throw a FileNotFoundException if the given path is not found.
      	throwFileNotFoundException(p->currentContext, szPath);
      else		
      if ((err = listFiles(szPath, slot, &list, &count, h, LF_NONE)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
      if (list != null)
      {
         if ((p->retO = arrayObj = createStringArray(p->currentContext, count)) != null)
         {
            start = (TCObjectArray) ARRAYOBJ_START(arrayObj);
            end = start + ARRAYOBJ_LEN(arrayObj);
            for (; start < end; start++, list = list->next) // stop also if OutOfMemoryError
            {
               *start = createStringObjectFromTCHAR(p->currentContext, list->value, -1);
               if (*start)
                  setObjectLock(*start, UNLOCKED);
               else
                  break;
            }
            setObjectLock(p->retO, UNLOCKED);
         }
      }
      heapDestroy(h);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_readBytes_Bii(NMParams p) // totalcross/io/File native public int readBytes(byte []b, int off, int len) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject bytes = p->obj[1];
   int32 off = p->i32[0];
   int32 len = p->i32[1];
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   int32 bytesRead = 0;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (!bytes)
      throwNullArgumentException(p->currentContext, "b");
   else
   if (len == 0)
      p->retI = 0; // flsobral@tc113_43: return 0 if asked to read 0.
   else
   if (!checkArrayRange(p->currentContext, bytes, off, len)) // off < 0 || len < 0 || off + len > (int32) ARRAYOBJ_LEN(bytes))
      ;
   else
   {
      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileReadBytes(*fref, (char*)ARRAYOBJ_START(bytes), off, len, &bytesRead)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
      {
         File_pos(file) += bytesRead; //flsobral@tc120: update stream pos.
         p->retI = bytesRead == 0 ? -1 : bytesRead; // flsobral@tc110_1: return -1 on EOF.
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_rename_s(NMParams p) // totalcross/io/File native public boolean rename(String path) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject currPath = File_path(file);
   TCObject newPath = p->obj[1];
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   NATIVE_FILE frefDummy;
   int32 slot = File_slot(file);
   int32 newPathLen;
   TCHAR szCurrPath[MAX_PATHNAME], szNewPath[MAX_PATHNAME];
   TCHARP c;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   if (!newPath)
      throwNullArgumentException(p->currentContext, "path");
   else
   {
      newPathLen = String_charsLen(newPath);
      if (newPathLen > MAX_PATHNAME || newPathLen == 0)
         throwIllegalArgumentIOException(p->currentContext, "path", null);
      else
      {
         JCharP2TCHARPBuf(String_charsStart(currPath), String_charsLen(currPath), szCurrPath);

         if (!fileExists(szCurrPath, slot))
            throwFileNotFoundException(p->currentContext, szCurrPath);
         else
         {
            JCharP2TCHARPBuf(String_charsStart(newPath), newPathLen, szNewPath);
            c = szNewPath;
            while (*c != 0)
            {
               if (*c == '\\')
                  *c = '/';
               c++;
            }
            if (mode != DONT_OPEN)
               fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
            else
               fref = &frefDummy;

            if ((err = fileRename(*fref, slot, szCurrPath, szNewPath, mode != DONT_OPEN)) != NO_ERROR)
               throwExceptionWithCode(p->currentContext, IOException, err);
            mark_closed(file);
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_setPos_i(NMParams p) // totalcross/io/File native public void setPos(int pos) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   int32 pos = p->i32[0];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   TCHAR szPath[MAX_PATHNAME];
   int32 size;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (pos < 0)
      throwException(p->currentContext, IOException, "Argument 'pos' cannot be negative");
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;
      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);

      if ((err = fileGetSize(*fref, szPath, &size)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
      if (pos > size && (err = fileSetSize(fref, pos+1)) != NO_ERROR) // guich@tc125_5: grow the file instead of throwing an exception
         throwExceptionWithCode(p->currentContext, IOException, err);
      else   
      if ((err = fileSetPos(*fref, pos)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         File_pos(file) = pos; //flsobral@tc120: update stream pos.
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_writeBytes_Bii(NMParams p) // totalcross/io/File native public int writeBytes(byte []b, int off, int len) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   TCObject bytes = p->obj[1];
   int32 off = p->i32[0];
   int32 len = p->i32[1];
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   int32 bytesWritten;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   if (!bytes)
      throwNullArgumentException(p->currentContext, "b");
   else
   if (len == 0)
      p->retI = 0;
   else
   if (!checkArrayRange(p->currentContext, bytes, off, len)) // off < 0 || len < 0 || off + len > (int32) ARRAYOBJ_LEN(bytes))
      ;
   else
   {
      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileWriteBytes(*fref, (char*)ARRAYOBJ_START(bytes), off, len, &bytesWritten)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
      {
         File_pos(file) += bytesWritten; //flsobral@tc120: update stream pos.
         p->retI = bytesWritten;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_setAttributes_i(NMParams p) // totalcross/io/File native public void setAttributes(int attr) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   int32 attr = p->i32[0];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   TCHAR szPath[MAX_PATHNAME];
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   if (attr < 0 || attr > 15) // the user may reset all attributes, so 0 is a valid number
      throwIllegalArgumentIOException(p->currentContext, "attr", null);
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;

      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileSetAttributes(*fref, szPath, attr)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_getAttributes(NMParams p) // totalcross/io/File native public int getAttributes() throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   int32 attr = p->i32[0];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   TCHAR szPath[MAX_PATHNAME];
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;

      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileGetAttributes(*fref, szPath, &attr)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         p->retI = attr;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_setTime_bt(NMParams p) // totalcross/io/File native public void setTime(byte whichTime, totalcross.sys.Time time) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   char whichTime = (char) p->i32[0];
   TCObject time = p->obj[1];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   TCHAR szPath[MAX_PATHNAME];
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   if (time == null)
      throwNullArgumentException(p->currentContext, "time");
   else
   if (whichTime != 0x1 && whichTime != 0x2 && whichTime != 0x4 && whichTime != 0xF)
      throwIllegalArgumentIOException(p->currentContext, "whichTime", null);
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;

      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileSetTime(*fref, szPath, whichTime, time)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_getTime_b(NMParams p) // totalcross/io/File native public totalcross.sys.Time getTime(byte whichTime) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   char whichTime = (char) p->i32[0];
   TCObject path = File_path(file);
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   TCHAR szPath[MAX_PATHNAME];
   TCObject time;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (whichTime != 0x1 && whichTime != 0x2 && whichTime != 0x4)
      throwIllegalArgumentIOException(p->currentContext, "whichTime", null);
   else
   {
      JCharP2TCHARPBuf(String_charsStart(path), String_charsLen(path), szPath);
      if (!replacePath(p,szPath,true))
         return;

      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileGetTime(p->currentContext, *fref, szPath, whichTime, &time)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
      {
         p->retO = time;
         setObjectLock(time, UNLOCKED);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_setSize_i(NMParams p) // totalcross/io/File native public void setSize(int newSize) throws totalcross.io.IOException;
{
   TCObject file = p->obj[0];
   int32 newSize = p->i32[0];
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   {
      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileSetSize(fref, newSize)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_getCardSerialNumber_i(NMParams p) // totalcross/io/File native public static String getCardSerialNumber(int slot) throws totalcross.io.IOException;
{
   int32 slot = p->i32[0];
   char serialNumber[10];
   Err err;

   IntBuf intBuf;

   if (slot < -1)
      throwIllegalArgumentIOException(p->currentContext, "slot", int2str(slot, intBuf));
   else
   if ((err = fileGetCardSerialNumber(slot, serialNumber)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   else
   if (*serialNumber != 0)
   {
      p->retO = createStringObjectFromCharP(p->currentContext, serialNumber, -1);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_flush(NMParams p)
{
   TCObject file = p->obj[0];
   TCObject fileRef = File_fileRef(file);
   int32 mode = File_mode(file);
   NATIVE_FILE* fref;
   Err err;

   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   if (mode == DONT_OPEN)
      throwException(p->currentContext, IOException, "Operation cannot be used in DONT_OPEN mode");
   else
   if (mode == READ_ONLY)
      throwException(p->currentContext, IOException, "Operation cannot be used in READ_ONLY mode");
   else
   {
      fref = (NATIVE_FILE*) ARRAYOBJ_START(fileRef);
      if ((err = fileFlush(*fref)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_listRoots(NMParams p) // totalcross/io/File native public static String []listRoots()
{
#if defined (WIN32)
   TCHARPs* list=null;
   int32 count = 0;
   volatile TCObject arrayObj = null;
   TCObjectArray start, end;
   Err err;
   volatile Heap h;

   h = heapCreate();
   IF_HEAP_ERROR(h)
   {
      heapDestroy(h);
      throwException(p->currentContext, OutOfMemoryError, null);
      return;
   }

   if ((err = fileListRoots(&list, &count, h)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   else if (count == -1) // if count is -1, throw out of memory error.
      throwException(p->currentContext, OutOfMemoryError, null);
   else
   if (list != null)
   {
      if ((p->retO = arrayObj = createStringArray(p->currentContext, count)) != null)
      {
         start = (TCObjectArray) ARRAYOBJ_START(arrayObj);
         end = start + ARRAYOBJ_LEN(arrayObj);
         for (; start < end; start++, list = list->next) // stop also if OutOfMemoryError
         {
            *start = createStringObjectFromTCHAR(p->currentContext, list->value, -1);
            if (*start)
               setObjectLock(*start, UNLOCKED);
            else
               break;
         }
         setObjectLock(p->retO, UNLOCKED);
      }
   }
   heapDestroy(h);
#endif   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_isEmpty(NMParams p) // totalcross/io/File native public boolean isEmpty() throws IOException
{
   TCObject file = p->obj[0];
   TCObject path = File_path(file);
   int32 mode = File_mode(file);
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];
   NATIVE_FILE* fref = INVALID_HANDLE_VALUE;
   int32 isEmpty = true;
   Err err;
   p->retI = isEmpty;
   
   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   {
      String2TCHARPBuf(path, szPath);
      if (!replacePath(p, szPath, true))
         return;
      if (!fileExists(szPath, slot))
         return;
      if (mode != DONT_OPEN)
         fref = (NATIVE_FILE*) ARRAYOBJ_START(file);
      if ((err = fileIsEmpty(fref, szPath, slot, &isEmpty)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         p->retI = isEmpty;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tiF_chmod_i(NMParams p) // totalcross/io/File native public int chmod(int mod) throws IOException
{
   TCObject file = p->obj[0];
   int32 mod = p->i32[0];
   TCObject path = File_path(file);
   int32 mode = File_mode(file);
   int32 slot = File_slot(file);
   TCHAR szPath[MAX_PATHNAME];
   NATIVE_FILE* fref = INVALID_HANDLE_VALUE;
   Err err;
   p->retI = -1;
   
   if (mode == INVALID || mode == CLOSED)
      throwException(p->currentContext, IOException, "Invalid file object.");
   else
   {
      String2TCHARPBuf(path, szPath);
      if (!replacePath(p, szPath, true))
         return;
      if (fileExists(szPath, slot))
         throwFileNotFoundException(p->currentContext, szPath); //flsobral@tc126: throw exception if the file does not exist.
      else
      {        
         if (mode != DONT_OPEN)
            fref = (NATIVE_FILE*) ARRAYOBJ_START(file);
         if ((err = fileChmod(fref, szPath, slot, &mod)) != NO_ERROR)
            throwExceptionWithCode(p->currentContext, IOException, err);
         else
            p->retI = mod;
      }
   }
}
//////////////////////////////////////////////////////////////////////////

#ifdef ENABLE_TEST_SUITE
 #include "File_test.h"
#endif
