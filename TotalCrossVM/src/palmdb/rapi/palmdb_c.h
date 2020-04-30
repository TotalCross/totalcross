// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if UNDER_CE >= 300 && !defined(WIN32_PLATFORM_HPC2000)
 #include <stddef.h>
#endif

#include <rapi.h>

typedef BOOL (__stdcall *CeFindCloseProc)(HANDLE hFindFile);
typedef BOOL (__stdcall *CeFindNextFileProc)(HANDLE hFindFile, LPCE_FIND_DATA lpFindFileData);
typedef HANDLE (__stdcall *CeFindFirstFileProc)(LPCWSTR lpFileName, LPCE_FIND_DATA lpFindFileData);
typedef DWORD (__stdcall *CeGetLastErrorProc)(void);
typedef DWORD (__stdcall *CeSetFilePointerProc)(HANDLE hFile, LONG lDistanceToMove, PLONG lpDistanceToMoveHigh, DWORD dwMoveMethod);
typedef BOOL (__stdcall *CeReadFileProc)(HANDLE hFile, LPVOID lpBuffer, DWORD nNumberOfBytesToRead, LPDWORD lpNumberOfBytesRead, LPOVERLAPPED lpOverlapped);
typedef BOOL (__stdcall *CeWriteFileProc)(HANDLE hFile, LPCVOID lpBuffer, DWORD nNumberOfBytesToWrite, LPDWORD lpNumberOfBytesWritten, LPOVERLAPPED lpOverlapped);
typedef BOOL (__stdcall *CeSetEndOfFileProc)(HANDLE hFile);
typedef BOOL (__stdcall *CeCloseHandleProc)(HANDLE hObject);
typedef BOOL (__stdcall *CeDeleteFileProc)(LPCWSTR lpFileName);
typedef HANDLE (__stdcall *CeCreateFileProc)(LPCWSTR lpFileName, DWORD dwDesiredAccess, DWORD dwShareMode, LPSECURITY_ATTRIBUTES lpSecurityAttributes, DWORD dwCreationDisposition, DWORD dwFlagsAndAttributes, HANDLE hTemplateFile);
typedef DWORD (__stdcall *CeGetFileSizeProc)(HANDLE hFile, LPDWORD lpFileSizeHigh);
typedef BOOL (__stdcall *CeMoveFileProc)(LPCWSTR lpExistingFileName, LPCWSTR lpNewFileName);

CeFindCloseProc procCeFindClose;
CeFindNextFileProc procCeFindNextFile;
CeFindFirstFileProc procCeFindFirstFile;
CeGetLastErrorProc procCeGetLastError;
CeSetFilePointerProc procCeSetFilePointer;
CeReadFileProc procCeReadFile;
CeWriteFileProc procCeWriteFile;
CeSetEndOfFileProc procCeSetEndOfFile;
CeCloseHandleProc procCeCloseHandle;
CeDeleteFileProc procCeDeleteFile;
CeCreateFileProc procCeCreateFile;
CeGetFileSizeProc procCeGetFileSize;
CeMoveFileProc procCeMoveFile;


#ifdef __cplusplus
extern "C" {
#endif

// wince is 4.5x faster when using the system io, due to the FILE_FLAG_RANDOM_ACCESS flag
// this is why we have a special version not using POSIX

typedef HANDLE PDBFileRef;

static WCHAR *toWChar(TCHAR *c)
{
   static WCHAR buf[MAX_PATH];
   WCHAR *f = buf;
   while (*c)
      *f++ = *c++;
   *f = 0;
   return buf;
}

static WCHAR *toWChar2(TCHAR *c)
{
   static WCHAR buf[MAX_PATH];
   WCHAR *f = buf;
   while (*c)
      *f++ = *c++;
   *f = 0;
   return buf;
}

static TCHARP toTCHAR(WCHAR* c)
{
   static TCHAR buf[MAX_PATH];
   TCHARP f = buf;

   while (*c)
      *f++ = (TCHAR) *c++;
   *f = 0;
   return buf;
}

Err inline PDBGetLastErr()
{
   return procCeGetLastError();
}

bool PDBCreateFile(TCHARP fullPath, bool createIt, bool readOnly, PDBFileRef* fileRef)
{
   return (*fileRef = procCeCreateFile(toWChar(fullPath),
      readOnly ? GENERIC_READ:(GENERIC_READ|GENERIC_WRITE), // font files must be open in readonly, or two instances will not be able to run
      FILE_SHARE_READ,
      null,
      createIt ? OPEN_ALWAYS:OPEN_EXISTING,
      /*FILE_ATTRIBUTE_NORMAL|FILE_FLAG_RANDOM_ACCESS|FILE_FLAG_WRITE_THROUGH, TEST IT! */ FILE_ATTRIBUTE_NORMAL,
      // Line below is from win32/wince version
      //FILE_ATTRIBUTE_NORMAL|FILE_FLAG_RANDOM_ACCESS, // guich@555_9: with FILE_FLAG_WRITE_THROUGH, queries are 50ms faster, but without it, table fillup is 48% faster!
      null)) != INVALID_HANDLE_VALUE;
}

bool PDBCloseFile(PDBFileRef fileRef)
{
   return procCeCloseHandle(fileRef) == TRUE;
}

bool inline PDBRename(TCHARP oldName, TCHARP newName)
{
   return procCeMoveFile(toWChar(oldName), toWChar2(newName)) == TRUE;
}

bool inline PDBRemove(TCHARP fileName)
{
   return procCeDeleteFile(toWChar(fileName)) == TRUE;
}

bool PDBRead(PDBFileRef fileRef, VoidP buf, int32 size, int32* read)
{
   return procCeReadFile(fileRef, buf, size, (LPDWORD) read, null) == TRUE;
}

bool inline PDBReadAt(PDBFileRef fileRef, VoidP buf, int32 size, int32 offset, int32* read)
{
   return (procCeSetFilePointer(fileRef, offset, null, FILE_BEGIN) != 0xFFFFFFFFL) ? PDBRead(fileRef, buf, size, read) : false;
}

bool inline PDBWrite(PDBFileRef fileRef, VoidP buf, int32 size, int32* written)
{
   return procCeWriteFile(fileRef, buf, size, (LPDWORD) written, null) == TRUE;
}

bool inline PDBWriteAt(PDBFileRef fileRef, VoidP buf, int32 size, int32 offset, int32* written)
{
   return (procCeSetFilePointer(fileRef, offset, null, FILE_BEGIN) != 0xFFFFFFFFL) ? PDBWrite(fileRef, buf, size, written) : false;
}

bool inline PDBGetFileSize (PDBFileRef fileRef, int32* size)
{
   return (*size = procCeGetFileSize(fileRef, null)) != 0xFFFFFFFFL;
}

bool inline PDBGrowFileSize(PDBFileRef fileRef, int32 oldSize, int32 growSize)
{
   return (procCeSetFilePointer(fileRef, oldSize + growSize, null, FILE_BEGIN) != 0xFFFFFFFFL) ? (procCeSetEndOfFile(fileRef) == TRUE) : 0;
}

bool PDBListDatabasesIn(TCHARP path, bool recursive, HandlePDBSearchProcType proc, VoidP userVars)
{
   TCHAR searchPath[MAX_PATH];
   HANDLE searchHandle;
   CE_FIND_DATA findFileData;
   bool stopSearch = false;
   int32 pathLen = (int32) tcslen(path);

   tcscpy(searchPath, path);
   if (path[pathLen-1] != '/')
      tcscat(searchPath, TEXT("/*.*"));
   else
   {
      tcscat(searchPath, TEXT("*.*"));
      pathLen--;
   }

   if ((searchHandle = procCeFindFirstFile(toWChar(searchPath), &findFileData)) == INVALID_HANDLE_VALUE)
      return false;

   do
   {
      if (findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY && recursive)
      {
#if defined (WIN32) && !defined (WINCE)
         if (lstrcmpW(findFileData.cFileName, L".") && lstrcmpW(findFileData.cFileName, L".."))
         {
#endif
            JCharP2TCHARPBuf((JCharP) findFileData.cFileName, lstrlenW(findFileData.cFileName), searchPath+pathLen+1);
            //tcscpy(searchPath+pathLen+1, findFileData.cFileName);
            stopSearch = PDBListDatabasesIn(searchPath, recursive, proc, userVars);
#if defined (WIN32) && !defined (WINCE)
         }
#endif
      }
      else if (endsWithPDB(toTCHAR(findFileData.cFileName))) // it's a pdb file.
      {
         if (proc)
         {
            JCharP2TCHARPBuf((JCharP) findFileData.cFileName, lstrlenW(findFileData.cFileName), searchPath+pathLen+1);
            //tcscpy(searchPath+pathLen+1, findFileData.cFileName);
            stopSearch = (*proc)(searchPath, userVars);
         }
      }
   }
   while (procCeFindNextFile(searchHandle, &findFileData) && !stopSearch);

   procCeFindClose(searchHandle);
   return stopSearch; // guich@tc110_93: return stopSearch
}

// date and time functions

int32 leapYear(int32 y)
{
   if (y % 400 == 0) return 1;
   if (y % 100 == 0) return 0;
   if (y % 4 == 0)   return 1;
   return 0;
}

int32 daysAccrued(int32 m, int32 y)
{
   static int32 daysAc[] = {0,31,59,90,120,151,181,212,243,273,304,334,365}; // days accrued in a year
   if (leapYear(y) && m >=2)
      return daysAc[m]+1;
   return daysAc[m];
}

int32 daysInYear(int32 y)
{
   return 365 + leapYear(y);
}

int32 date2days(int32 y, int32 m, int32 d)
{
   int32 total = d; // start the count with the current day
   int32 i;
   if (m > 1)
      total += daysAccrued(m-1,y); // it is not necessary count current month neither january
   // count since last year until base year how many days have gone
   for (i = y-1; i >= 1970; i--)
      total += daysInYear(i);
   return total-1;
}

uint32 PDBGetNow()
{
   // PALMOS 5.0 date is based in # of seconds since 1970
   SYSTEMTIME st;
   uint32 timeX;

   GetSystemTime(&st);
   timeX = date2days(st.wYear, st.wMonth, st.wDay) * 86400; // convert to seconds
   timeX += st.wHour * 3600 + st.wMinute * 60 + st.wSecond; // add the number of seconds in today
   //timeX = 2082844800;

   return timeX;
}

#ifdef __cplusplus
}
#endif
