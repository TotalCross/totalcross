// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <Winbase.h>
#if defined (WINCE) && _WIN32_WCE >= 300 && defined(WIN32_PLATFORM_PSPC)
 #include <Projects.h>
#endif

#ifdef UNICODE
   #define IS_DEBUG_CONSOLE(path) (JCharPIndexOfJCharP(path,TEXT("DebugConsole"),0,-1,12) >= 0)
#else
   #define IS_DEBUG_CONSOLE(path) (strstr(path,"DebugConsole") != null)
#endif

/*
 *
 * Always return true on WinCE and Win32.
 *
 *************************************/

#ifdef WP8
static bool fileIsCardInserted(int32 slot)
{
   return fileIsCardInsertedCPP();
}
#else
static bool fileIsCardInserted(int32 slot)
{
   return true;
}
#endif

/*
 *
 * CreateFile
 * GetLastError
 * GetFileAttributes
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileCreate(NATIVE_FILE* fref, TCHARP path, int32 mode, int32* slot)
{
   HANDLE hFile;
   DWORD dwDesiredAccess;
   DWORD dwShareMode = FILE_SHARE_READ; // file is read-only for other applications while in use.
   LPSECURITY_ATTRIBUTES lpSecurityAttributes = null; // Not used in WinCE, may be null in Win32 since we won't be using it.
   DWORD dwCreationDisposition;
   DWORD dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL; // file attributes are ignored while opening an existing obj, it is set to FILE_ATTRIBUTE_NORMAL for obj creation.
   HANDLE hTemplateFile = null; // Not used in WinCE, may be null in Win32 since we won't be using it.

   switch (mode)
   {
      case READ_WRITE:     dwDesiredAccess = GENERIC_READ | GENERIC_WRITE; dwCreationDisposition = OPEN_EXISTING;  break;
      case CREATE:         dwDesiredAccess = GENERIC_READ | GENERIC_WRITE; dwCreationDisposition = CREATE_NEW;     break;
      case CREATE_EMPTY:   dwDesiredAccess = GENERIC_READ | GENERIC_WRITE; dwCreationDisposition = CREATE_ALWAYS;  break;
      case READ_ONLY:      dwDesiredAccess = GENERIC_READ;                 dwCreationDisposition = OPEN_EXISTING;  dwShareMode |= FILE_SHARE_WRITE; break;
   }

   hFile = CreateFile(path, dwDesiredAccess, dwShareMode, lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
   if (hFile == INVALID_HANDLE_VALUE && mode == CREATE)
      hFile = CreateFile(path, dwDesiredAccess, dwShareMode, lpSecurityAttributes, OPEN_EXISTING, dwFlagsAndAttributes, hTemplateFile);
   if (hFile == INVALID_HANDLE_VALUE)
      return GetLastError();

   fref->handle = hFile;
   tcscpy(fref->path, path);
   return NO_ERROR;
}

/*
 *
 * CloseHandle
 * GetLastError
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileClose(NATIVE_FILE* fref)
{
   if (!CloseHandle(fref->handle)) //It's an archive and it will be closed.
      return GetLastError();
   return NO_ERROR;
}

/*
 *
 * CreateDirectory
 * GetFileAttributes
 * GetLastError
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileCreateDir(TCHARP path, int32 slot)
{
   TCHARP c;

   if (!CreateDirectory(path, null))
   {
      if (path[0] == '/')
         c = path + 1;
      else
         c = path;
      while (*c != 0)
      {
         if (*c == '/')
         {
            WIN32_FILE_ATTRIBUTE_DATA f_attr_ex;
            int res = 0;
            f_attr_ex.dwFileAttributes = INVALID_ATTR_VALUE;

            *c = 0;
            res = GetFileAttributesEx(path, GetFileExInfoStandard, &f_attr_ex);
            if ((res == 0) || (f_attr_ex.dwFileAttributes == INVALID_ATTR_VALUE))
            {
               if (!CreateDirectory(path, null))
                  goto error;
            }
            *c = '/';
         }
         c++;
      }
      if (!CreateDirectory(path, null))
         goto error;
   }
   return NO_ERROR;

error:
   return GetLastError();
}

/*
 *
 * GetFileAttributes
 * RemoveDirectory
 * CloseHandle
 * DeleteFile
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileDelete(NATIVE_FILE* fref, TCHARP path, int32 slot, bool isOpen)
{
   DWORD fileAttributes;
   int len = lstrlen(path);
   if (path[len-1] == '/') // remove leading slash
      path[len-1] = 0;

   {
	   WIN32_FILE_ATTRIBUTE_DATA f_attr_ex;
	   f_attr_ex.dwFileAttributes = INVALID_ATTR_VALUE;
	   GetFileAttributesEx(path, GetFileExInfoStandard, &f_attr_ex);
	   fileAttributes = f_attr_ex.dwFileAttributes;
   }

   if (fileAttributes == INVALID_ATTR_VALUE) //Checks if file exists.
      return ERROR_FILE_NOT_FOUND;
   if (fileAttributes & FILE_ATTRIBUTE_DIRECTORY) //It's a directory.
      return RemoveDirectory(path) ? NO_ERROR : GetLastError();

   //It's an archive.
   if (isOpen)
      CloseHandle(fref->handle);
   return (DeleteFile(path) ? NO_ERROR : GetLastError());
}

/*
 *
 * GetFileAttributes
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static bool fileExists(TCHARP path, int32 slot)
#if defined WINCE
{
	return (GetFileAttributes(path) != INVALID_ATTR_VALUE);
#elif defined WP8
{
	bool result;
	WIN32_FILE_ATTRIBUTE_DATA f_attr_ex;
	f_attr_ex.dwFileAttributes = INVALID_ATTR_VALUE;
	GetFileAttributesEx(path, GetFileExInfoStandard, &f_attr_ex);
	result = (f_attr_ex.dwFileAttributes != INVALID_ATTR_VALUE);
	return result;
#else
{
	bool result;
	SetErrorMode(SEM_FAILCRITICALERRORS); // flsobral@tc115_25: no longer displays error message to user when the removable disk is not available.
	result = (GetFileAttributes(path) != INVALID_ATTR_VALUE);
	SetErrorMode(0);
	return result;
#endif
}

/*
 *
 * ULARGE_INTEGER
 *
 * GetDiskFreeSpaceEx
 *
 * OS Versions: Windows CE 2.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileGetFreeSpace(TCHAR* path, int32* freeSpace, int32 slot)
{
   ULARGE_INTEGER freeSize = { 0 };

   if (GetDiskFreeSpaceEx(path, null, null, &freeSize))
   {
      if (freeSize.HighPart > 0 || freeSize.LowPart > 2147483647) //flsobral@tc122_25: the low part may be 64 bits in some systems for no particular reason.
         *freeSpace = 2147483647; // If there is more than 2GB available, returns 2GB.
      else
         *freeSpace = freeSize.LowPart;
      return NO_ERROR;
   }
   return GetLastError();
}

/*
 *
 * GetFileSize
 *
 * OS Versions: Windows CE 2.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileGetSize(NATIVE_FILE fref, TCHARP szPath, int32* size)
{
#ifndef WP8
   return (((*size = GetFileSize(fref.handle, null)) != 0xFFFFFFFF) ? NO_ERROR : GetLastError());
#else
   FILE_STANDARD_INFO finfo = { 0 };
   *size = 0xFFFFFFFF;
   if (GetFileInformationByHandleEx(fref.handle, FileStandardInfo, &finfo, sizeof(finfo)) == 0)
   {
      return GetLastError();
   }

   // Size cannot exceed 32 bits
   *size = (int32) finfo.EndOfFile.LowPart;
   return NO_ERROR;
#endif
}

/*
 *
 * GetFileAttributes
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static bool fileIsDir(TCHARP path, int32 slot)
{
#ifndef WP8
   DWORD fileAttributes;
   fileAttributes = GetFileAttributes(path);
#else
   DWORD fileAttributes;
   WIN32_FILE_ATTRIBUTE_DATA f_attr_ex;
   int res = 0;
   f_attr_ex.dwFileAttributes = INVALID_ATTR_VALUE;

   res = GetFileAttributesEx(path, GetFileExInfoStandard, &f_attr_ex);
   fileAttributes = f_attr_ex.dwFileAttributes;
#endif

   return ((fileAttributes != INVALID_ATTR_VALUE)&&(fileAttributes & FILE_ATTRIBUTE_DIRECTORY));
}

static Err fileIsEmpty(NATIVE_FILE* fref, TCHARP path, int32 slot, int32* isEmpty)
{
   Err err = NO_ERROR;
#ifndef WP8
   DWORD fileAttributes = GetFileAttributes(path);
#else
   DWORD fileAttributes;
   WIN32_FILE_ATTRIBUTE_DATA f_attr_ex;
   int res = 0;

   res = GetFileAttributesEx(path, GetFileExInfoStandard, &f_attr_ex);
   fileAttributes = f_attr_ex.dwFileAttributes;
#endif

   if (fref != INVALID_HANDLE_VALUE)
      return fileGetSize(*fref, path, isEmpty);

   if ((fileAttributes != INVALID_ATTR_VALUE)&&(fileAttributes & FILE_ATTRIBUTE_DIRECTORY))
   {
      WIN32_FIND_DATA findData;
      HANDLE hFind;
      TCHAR searchPath[MAX_PATH];
   
      tcscpy(searchPath, path);
      if (path[tcslen(path)-1] != '/')
         tcscat(searchPath, TEXT("/*.*"));
      else
         tcscat(searchPath, TEXT("*.*"));
   
      *isEmpty = true;
      if ((hFind = FindFirstFile(searchPath, &findData)) != INVALID_HANDLE_VALUE)
      {
         do
         {
            #if defined (WIN32) && !defined (WINCE)
            if (findData.cFileName[0] != '.' || (tcscmp(findData.cFileName, TEXT(".")) && tcscmp(findData.cFileName, TEXT("..")))) // first check is just for speedup
            #endif
            {     
               *isEmpty = false;
               break;                                        
            }
         }
         while (FindNextFile(hFind, &findData));
         FindClose(hFind);
      }
   }
   else if ((err = fileCreate(fref, path, READ_ONLY, &slot)) == NO_ERROR)
   {
      err = fileGetSize(*fref, path, isEmpty);
      fileClose(fref);
   }
   return err;
}

/*
 *
 * ReadFile
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileReadBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesRead)
{
   return (ReadFile(fref.handle, (bytes+offset), length, bytesRead, null) ? NO_ERROR : GetLastError());
}

/*
 *
 * CloseHandle
 * MoveFile
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileRename(NATIVE_FILE fref, int32 slot, TCHARP currPath, TCHARP newPath, bool isOpen)
{
   if (isOpen)
      CloseHandle(fref.handle);
#if defined (WP8)
   return (MoveFileEx(currPath, newPath, 0) ? NO_ERROR : GetLastError());
#else
   return (MoveFile(currPath, newPath) ? NO_ERROR : GetLastError());
#endif
}

/*
 *
 * SetFilePointer
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileSetPos(NATIVE_FILE fref, int32 position)
{
   Err err;
   // Must use SetFilePointerEx when running on the WP8 emulator, but not on device
#if defined WP8
   LARGE_INTEGER off = { 0 },cur;
   off.LowPart = position;
   return ((SetFilePointerEx(fref.handle, off, &cur, FILE_BEGIN) != 0) ?
               NO_ERROR : (((err = GetLastError()) == NO_ERROR) ? NO_ERROR : err));
#else
   return ((SetFilePointer(fref.handle, position, null, FILE_BEGIN) != INVALID_FILEPTR_VALUE) ?
               NO_ERROR : (((err = GetLastError()) == NO_ERROR) ? NO_ERROR : err));
#endif
}

/*
 *
 * WriteFile
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileWriteBytes(NATIVE_FILE fref, CharP bytes, int32 offset, int32 length, int32* bytesWritten)
{
   return (WriteFile(fref.handle, (bytes+offset), length, bytesWritten, null) ? NO_ERROR : GetLastError());
}

/*
 *
 * SetFileAttributes
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileSetAttributes(NATIVE_FILE fref, TCHARP path, int32 tcAttributes)
{
   DWORD fileAttributes = 0;

   if (tcAttributes == ATTR_NORMAL)
      fileAttributes = FILE_ATTRIBUTE_NORMAL;
   else
   {
      if (tcAttributes & ATTR_ARCHIVE)
         fileAttributes |= FILE_ATTRIBUTE_ARCHIVE;
      if (tcAttributes & ATTR_HIDDEN)
         fileAttributes |= FILE_ATTRIBUTE_HIDDEN;
      if (tcAttributes & ATTR_READ_ONLY)
         fileAttributes |= FILE_ATTRIBUTE_READONLY;
      if (tcAttributes & ATTR_SYSTEM)
         fileAttributes |= FILE_ATTRIBUTE_SYSTEM;
   }

   return (SetFileAttributes(path, fileAttributes) ? NO_ERROR : GetLastError());
}

/*
 *
 * GetFileAttributes
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileGetAttributes(NATIVE_FILE fref, TCHARP path, int32* attributes)
{
   DWORD fileAttributes = 0;
#ifndef WP8
   if ((fileAttributes = GetFileAttributes(path)) == INVALID_ATTR_VALUE)
      return GetLastError();
#else
   WIN32_FILE_ATTRIBUTE_DATA f_attr_ex;
   int res = 0;
   f_attr_ex.dwFileAttributes = INVALID_ATTR_VALUE;

   res = GetFileAttributesEx(path, GetFileExInfoStandard, &f_attr_ex);
   fileAttributes = f_attr_ex.dwFileAttributes;
   if ((res == 0) || (fileAttributes == INVALID_ATTR_VALUE))
      return GetLastError();
#endif
   *attributes = 0;

   if (fileAttributes & FILE_ATTRIBUTE_ARCHIVE)
      *attributes = *attributes | ATTR_ARCHIVE;
   if (fileAttributes & FILE_ATTRIBUTE_HIDDEN)
      *attributes = *attributes | ATTR_HIDDEN;
   if (fileAttributes & FILE_ATTRIBUTE_READONLY)
      *attributes = *attributes | ATTR_READ_ONLY;
   if (fileAttributes & FILE_ATTRIBUTE_SYSTEM)
      *attributes = *attributes | ATTR_SYSTEM;

   return NO_ERROR;
}

/*
 *
 * FILETIME
 * SYSTEMTIME
 *
 * SystemTimeToFileTime
 * LocalFileTimeToFileTime
 * SetFileTime
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 ************************************/

static Err fileSetTime(NATIVE_FILE fref, TCHARP path, char whichTime, TCObject time)
{
   FILETIME systemFileTime, localFileTime;
   FILETIME *creationTime, *lastAccessTime, *lastWriteTime;
   SYSTEMTIME fileTime;

   fileTime.wYear          = Time_year(time);
   fileTime.wMonth         = Time_month(time);
   fileTime.wDay           = Time_day(time);
   fileTime.wHour          = Time_hour(time);
   fileTime.wMinute        = Time_minute(time);
   fileTime.wSecond        = Time_second(time);
   fileTime.wMilliseconds  = Time_millis(time);

   SystemTimeToFileTime(&fileTime, &localFileTime);
#ifdef WP8
   systemFileTime = localFileTime; 
#else
   LocalFileTimeToFileTime(&localFileTime, &systemFileTime);
#endif

   creationTime = lastAccessTime = lastWriteTime = null;

   if (whichTime & TIME_CREATED)
      creationTime = &systemFileTime;
   if (whichTime & TIME_MODIFIED)
      lastWriteTime = &systemFileTime;
   if (whichTime & TIME_ACCESSED)
      lastAccessTime = &systemFileTime;

   /*
    * SetFileTime( HANDLE, lpCreationTime, lpLastAccessTime, lpLastWriteTime )
    * The Windows CE object store returns the same creation time value for all
    * three parameters. In general, obj system drivers will vary how they
    * support this function.
    */
#ifndef WP8
   return (SetFileTime(fref.handle, creationTime, lastAccessTime, lastWriteTime) ? NO_ERROR : GetLastError());
#else
   {
      char buff[40];
      FILE_BASIC_INFO *finfo = (FILE_BASIC_INFO*) buff;

      if (!GetFileInformationByHandleEx(fref.handle, FileBasicInfo, finfo, sizeof(buff)))
         return GetLastError();

      if (creationTime != NULL)
         finfo->CreationTime = *(LARGE_INTEGER*)creationTime;
      if (lastAccessTime != NULL)
         finfo->LastAccessTime = *(LARGE_INTEGER*)lastAccessTime;
      if (lastWriteTime != NULL)
         finfo->LastWriteTime = *(LARGE_INTEGER*)lastWriteTime;

      return SetFileInformationByHandle(fref.handle, FileBasicInfo, finfo, sizeof(buff)) ? NO_ERROR : GetLastError();
   }
#endif
}

/*
 *
 * FILETIME
 * SYSTEMTIME
 *
 * GetFileTime
 * FileTimeToLocalFileTime
 * FileTimeToSystemTime
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/

static Err fileGetTime(Context currentContext, NATIVE_FILE fref, TCHARP path, char whichTime, TCObject* time)
{
   FILETIME systemFileTime, localFileTime;
   SYSTEMTIME fileTime;

#ifdef WP8
   char buff[40];
   FILE_BASIC_INFO *finfo = (FILE_BASIC_INFO *)buff;

   if (!GetFileInformationByHandleEx(fref.handle, FileBasicInfo, finfo, sizeof(buff)))
      return GetLastError();

   if (whichTime & TIME_CREATED)
      systemFileTime = *(FILETIME*)&finfo->CreationTime;
   else if (whichTime & TIME_MODIFIED)
      systemFileTime = *(FILETIME*)&finfo->LastAccessTime;
   else if (whichTime & TIME_ACCESSED)
      systemFileTime = *(FILETIME*)&finfo->LastWriteTime;
   
   localFileTime = systemFileTime;
#else
   FILETIME *creationTime = null, *lastAccessTime = null, *lastWriteTime = null;
   if (whichTime & TIME_CREATED)
      creationTime = &systemFileTime;
   else
   if (whichTime & TIME_MODIFIED)
      lastWriteTime = &systemFileTime;
   else
   if (whichTime & TIME_ACCESSED)
      lastAccessTime = &systemFileTime;

   /*
    * GetFileTime(HANDLE, lpCreationTime, lpLastAccessTime, lpLastWriteTime)
    * The Windows CE object store returns the same creation time value for all
    * three parameters. In general, obj system drivers will vary how they
    * support this function.
    */
   if (!GetFileTime(fref.handle, creationTime, lastAccessTime, lastWriteTime))
      return GetLastError();

   FileTimeToLocalFileTime(&systemFileTime, &localFileTime);
#endif

   *time = createObject(currentContext, "totalcross.sys.Time");
   if (*time == null)
      return NO_ERROR; // OOME was already threw

   FileTimeToSystemTime(&localFileTime, &fileTime);

   Time_year(*time)   = fileTime.wYear;
   Time_month(*time)  = fileTime.wMonth;
   Time_day(*time)    = fileTime.wDay;
   Time_hour(*time)   = fileTime.wHour;
   Time_minute(*time) = fileTime.wMinute;
   Time_second(*time) = fileTime.wSecond;
   Time_millis(*time) = fileTime.wMilliseconds;

   return NO_ERROR;
}

/*
 *
 * GetFileSize
 * SetFilePointer
 * SetEndOfFile
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winbase.h.
 * Link Library: Coredll.lib.
 *
 *************************************/
static Err fileSetSize(NATIVE_FILE* fref, int32 newSize)
{
	DWORD posHigh = 0;
	DWORD fileSize;

#ifndef WP8
   if ((fileSize = GetFileSize(fref->handle, null)) == 0xFFFFFFFF)
	   return GetLastError();
#else
   FILE_STANDARD_INFO finfo = { 0 };
   fileSize = 0xFFFFFFFF;
   
   if (GetFileInformationByHandleEx(fref->handle, FileStandardInfo, &finfo, sizeof(finfo)) == 0)
   {
      return GetLastError();
   }

   // Size cannot exceed 32 bits
   fileSize = finfo.EndOfFile.LowPart;
#endif
	if (fileSize == newSize)
		return NO_ERROR;

   // Must use SetFilePointerEx when running on the WP8 emulator, but not on device
#if defined WP8
   {
      LARGE_INTEGER off = { 0 }, cur;
      off.LowPart = newSize;

      if (SetFilePointerEx(fref->handle, off, &cur, FILE_BEGIN) == false)
         return GetLastError();
   }
#else
   if (SetFilePointer(fref->handle, newSize, &posHigh, FILE_BEGIN) == INVALID_FILEPTR_VALUE)
		return GetLastError();
#endif
   if (SetEndOfFile(fref->handle) == 0)
   {
      Err error = GetLastError();
      if (error == ERROR_INVALID_PARAMETER && newSize == 0)
      {
         CloseHandle(fref->handle);
         fref->handle = CreateFile(fref->path, GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ, null, TRUNCATE_EXISTING, FILE_ATTRIBUTE_NORMAL, null);
         if (!fileIsValid(*fref))
		      return GetLastError();
         return NO_ERROR;
      }
      return error;
   }

   return NO_ERROR;
}

/*
*
* FindClose
* CreateFile
* DeviceIoControl
* CloseHandle
*
* OS Versions: Windows CE 1.0 and later.
* Header: Winbase.h.
* Link Library: Coredll.lib.
*
* FindFirstFlashCard
* FindNextFlashCard
*
* Pocket PC Platforms: Pocket PC 2000 and later
* OS Versions: Windows CE 3.0 and later
* Header: projects.h
* Library: note_prj.lib
*
* IOCTL_DISK_GET_STORAGEID
*
* STORAGE_IDENTIFICATION
*
*
*************************************/
static Err fileGetCardSerialNumber(int32 slot, CharP serialNumber)
{
#if defined (WINCE) && _WIN32_WCE >= 300 && defined(WIN32_PLATFORM_PSPC)
   typedef HANDLE (__stdcall *procFindFirstFlashCard)(LPWIN32_FIND_DATA lpFindFlashData);
   typedef BOOL (__stdcall *procFindNextFlashCard)(HANDLE hFlashCard, LPWIN32_FIND_DATA lpFindFlashData);
   WIN32_FIND_DATA findFlashData;
   HANDLE hFlashCard, hFoundCard;
   TCHAR buf[MAX_PATH];
   STORAGE_IDENTIFICATION *storageIDP;
   char storageID[256];
   DWORD dwNumReturned;
   Err err = NO_ERROR;
   int32 i;
   
   HANDLE note_prjDll;
   procFindFirstFlashCard FindFirstFlashCardProc;
   procFindNextFlashCard FindNextFlashCardProc;
   
   note_prjDll = LoadLibrary(TEXT("note_prj.dll"));
   FindFirstFlashCardProc = (procFindFirstFlashCard) GetProcAddress(note_prjDll, TEXT("FindFirstFlashCard"));
   FindNextFlashCardProc = (procFindNextFlashCard) GetProcAddress(note_prjDll, TEXT("FindNextFlashCard"));

   if ((hFlashCard = FindFirstFlashCardProc(&findFlashData)) == INVALID_HANDLE_VALUE)
      goto error;

   for (i = 1 ; i <= slot ; i++)
   {
      if (!FindNextFlashCardProc(hFlashCard, &findFlashData))
      {
         FindClose(hFlashCard);
         err = ERROR_INVALID_DRIVE;
         goto finish;
      }
   }

   tcscpy(buf, TEXT("\\"));
   tcscat(buf, findFlashData.cFileName);
   tcscat(buf, TEXT("\\Vol:"));

   if (!FindClose(hFlashCard))
      goto error;
   if ((hFoundCard = CreateFile(buf, 0, FILE_SHARE_READ | FILE_SHARE_WRITE, null, OPEN_EXISTING, 0, null)) == INVALID_HANDLE_VALUE)
      goto error;

   storageIDP = (STORAGE_IDENTIFICATION*) storageID;
   storageIDP->dwSize = 256;
   if(!DeviceIoControl(hFoundCard, IOCTL_DISK_GET_STORAGEID, null, 0, storageIDP, 256, &dwNumReturned, null))
   {
      CloseHandle(hFoundCard);
      goto error;
   }

   xstrncpy(serialNumber, (((CharP)storageIDP) + storageIDP->dwSerialNumOffset), dwNumReturned  - storageIDP->dwSerialNumOffset);
   if (!CloseHandle(hFoundCard))
      goto error;

   goto finish;
error:
   err = GetLastError();
finish:
   FreeLibrary(note_prjDll);
   return err;
#else
	serialNumber[0] = 0;

	return NO_ERROR;
#endif
}

/*
*
* FlushFileBuffers
*
* OS Versions: Windows CE 1.0 and later.
* Header: Winbase.h.
* Link Library: Coredll.lib.
*
*************************************/
static Err fileFlush(NATIVE_FILE fref)
{
   return (FlushFileBuffers(fref.handle) == 0) ? GetLastError() : NO_ERROR;
}


static Err fileListRoots(TCHARPs** list, int32* count, Heap h)
{
#if defined (WINCE) //flsobral@tc126_11: implemented File.listRoots for WinCE.
   WIN32_FIND_DATA findData;
   HANDLE hFind;
   TCHAR searchPath[10] = TEXT("\\*.*");
   TCHARP fileName;
   int32 fileNameSize,pathlen=0;
   int32 errCode;
#if WIN32_WCE <= 400
   int32 errCodeBug;
#endif
   TCHARP logicalDrives;

   if ((logicalDrives = heapAlloc(h, 8 * sizeof(TCHAR))) == null) /* "device/".length = 7 */
   {
      (*count) = -1;
      return NO_ERROR;
   }

   tcscpy(logicalDrives, TEXT("device/"));
   *list = TCHARPsAdd(*list, logicalDrives, h); // add entry to list
   (*count)++;
   
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
      if ((findData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) && (findData.dwFileAttributes & FILE_ATTRIBUTE_TEMPORARY) && lstrcmpi(findData.cFileName, TEXT("NETWORK")))
      {                                             
         fileNameSize = tcslen(findData.cFileName)+2; //One for null and one extra in case it is a directory.
         fileName = (TCHARP)heapAlloc(h, sizeof(TCHAR)*(fileNameSize+pathlen) ); // note: if an heap error occurs, the FindClose will never be called; TODO change the implementation to allow that.
         tcscat(fileName, TEXT("/"));
         *list = TCHARPsAdd(*list, fileName ,h); // add entry to list
         (*count)++;
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
#else
   TCHARP logicalDrives;
   TCHARP nameIdx;
   int32 logicalDrivesLen;
   int32 nameIdxLen;

   if ((logicalDrivesLen = GetLogicalDriveStrings(0, null)) == 0)
      return GetLastError();

   if ((logicalDrives = heapAlloc(h, (logicalDrivesLen++) * sizeof(TCHAR))) == null)
   {
      (*count) = -1;
      return NO_ERROR;
   }
   
   if ((logicalDrivesLen = GetLogicalDriveStrings(logicalDrivesLen, logicalDrives)) == 0)
      return GetLastError();

   for (nameIdx = logicalDrives ; nameIdx[0] != 0 ; nameIdx += nameIdxLen) 
   {
      nameIdxLen = tcslen(nameIdx) + 1;
      *list = TCHARPsAdd(*list, nameIdx, h); // add entry to list
      (*count)++;
   }

   return NO_ERROR;
#endif
}

static Err fileChmod(NATIVE_FILE* fref, TCHARP path, int32 slot, int32* mod)
{
   *mod = -1;
   return NO_ERROR;
}
