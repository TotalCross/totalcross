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



static int32 privateGetFreeMemory(bool maxblock)
{
   int32 result=1;
   MEMORYSTATUS ms;  // works for most cases
   GlobalMemoryStatus(&ms);
#ifdef WINCE
   result = maxblock ? ms.dwTotalVirtual : ms.dwAvailVirtual; // guich@tc115_3: now using dwTotalVirtual instead of dwAvailPhys
#else
   result = maxblock ? ms.dwTotalPhys : ms.dwAvailPhys;
#endif
   return result;
}

static int32 privateGetTimeStamp()
{
   return GetTickCount() & 0x3FFFFFFF;
}

static Err privateListFiles(TCHARP path, int32 slot, TCHARPs** list, int32* count, Heap h, int32 options)
{
   WIN32_FIND_DATA findData;
   HANDLE hFind;
   TCHAR searchPath[MAX_PATH];
   TCHARP fileName;
   int32 fileNameSize,pathlen=0;
   int32 errCode;
   bool isDir;
   bool recursive = options & LF_RECURSIVE;
#if WIN32_WCE <= 400
   int32 errCodeBug;
#endif

   tcscpy(searchPath, path);
   if (path[tcslen(path)-1] != '/')
      tcscat(searchPath, TEXT("/*.*"));
   else
      tcscat(searchPath, TEXT("*.*"));

   if (recursive)
      pathlen = tcslen(path) + 1;

   if ((hFind = FindFirstFile(searchPath, &findData)) == INVALID_HANDLE_VALUE)
   {
      errCode = GetLastError();
      return errCode != ERROR_NO_MORE_FILES && errCode != ERROR_FILE_NOT_FOUND ? errCode : NO_ERROR; // guich@tc113_17: "file not found" is thrown in empty folders
   }

#if WIN32_WCE <= 400
   errCodeBug = GetLastError(); // some old implementations return a strange error code in SUCCESS situations. So we store it here to compare with it again below.
#endif

   do
   {
#if defined (WIN32) && !defined (WINCE)
      if (findData.cFileName[0] != '.' || (!strEq(findData.cFileName, ".") && !strEq(findData.cFileName, ".."))) // first check is just for speedup
#endif
      {                                             
         fileNameSize = tcslen(findData.cFileName)+2; //One for null and one extra in case it is a directory.
         fileName = (TCHARP)heapAlloc(h, sizeof(TCHAR)*(fileNameSize+pathlen) ); // note: if an heap error occurs, the FindClose will never be called; TODO change the implementation to allow that.

         if (recursive)
         {
            tcscpy(fileName, path);
            if (path[pathlen-2] != '/')
               tcscat(fileName, TEXT("/"));
         }
         tcscat(fileName, findData.cFileName);

         isDir = findData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY;
         if (isDir)
            tcscat(fileName, TEXT("/"));

         *list = TCHARPsAdd(*list, fileName ,h); // add entry to list
         (*count)++;

         if (isDir && recursive)
         {
            Err err = privateListFiles(fileName, slot, list, count, h, recursive);
            if (err != 0)
            {
               FindClose(hFind);
               return err;
            }
         }
      }
   }
   while (FindNextFile(hFind, &findData));
   // Free resources used to get the files' names.
   FindClose(hFind);
   errCode = GetLastError();
#if WIN32_WCE <= 400
   if (errCodeBug == errCode)
      return NO_ERROR;
#endif
   return (errCode != ERROR_NO_MORE_FILES) ? errCode : NO_ERROR;
}

/*****   timeUpdate   *****
 *
 * typedef struct _SYSTEMTIME {
 *    WORD wYear;
 *    WORD wMonth;
 *    WORD wDayOfWeek;
 *    WORD wDay;
 *    WORD wHour;
 *    WORD wMinute;
 *    WORD wSecond;
 *    WORD wMilliseconds;
 * } SYSTEMTIME;
 *
 * GetLocalTime
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *******************************/

static void privateGetDateTime(int32* year, int32* month, int32* day, int32* hour, int32* minute, int32* second, int32* millis)
{
   /*
    * "The wMilliseconds value in the SYSTEMTIME structure is always zero (0) when you use
    * the GetSystemTime function on a x86 Windows CE PC-based (CEPC) reference platform."
    *
    * http://support.microsoft.com/default.aspx?scid=KB;en-us;260419&
    *
    * Article ID: 260419
    *
    */
   SYSTEMTIME tLocalTime;
   GetLocalTime(&tLocalTime);

   *year   = tLocalTime.wYear;
   *month  = tLocalTime.wMonth;
   *day    = tLocalTime.wDay;
   *hour   = tLocalTime.wHour;
   *minute = tLocalTime.wMinute;
   *second = tLocalTime.wSecond;
   *millis = tLocalTime.wMilliseconds;
}
