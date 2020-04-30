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

// wince is 4.5x faster when using the system io, due to the FILE_FLAG_RANDOM_ACCESS flag
// this is why we have a special version not using POSIX

typedef HANDLE PDBFileRef;

Err PDBGetLastErr()
{
   return GetLastError();
}

bool PDBCreateFile(TCHARP fullPath, bool createIt, bool readOnly, PDBFileRef* fileRef)
{
   return (*fileRef = CreateFile(fullPath,
      readOnly ? GENERIC_READ:(GENERIC_READ|GENERIC_WRITE), // font files must be open in readonly, or two instances will not be able to run
      FILE_SHARE_READ|FILE_SHARE_WRITE,
      null,
      createIt ? OPEN_ALWAYS:OPEN_EXISTING,
      FILE_ATTRIBUTE_NORMAL|FILE_FLAG_RANDOM_ACCESS, // guich@555_9: with FILE_FLAG_WRITE_THROUGH, queries are 50ms faster, but without it, table fillup is 48% faster!
      null)) != INVALID_HANDLE_VALUE;
}

bool PDBCloseFile(PDBFileRef fileRef)
{
   return CloseHandle(fileRef);
}

bool PDBRename(TCHARP oldName, TCHARP newName)
{
#if defined (WP8)
   return MoveFileEx(oldName, newName, 0);
#else
	return MoveFile(oldName, newName);
#endif
}

bool PDBRemove(TCHARP fileName)
{
   return DeleteFile(fileName);
}

bool PDBRead(PDBFileRef fileRef, VoidP buf, int32 size, int32* read)
{
   return ReadFile(fileRef, buf, size, read, null);
}

bool PDBReadAt(PDBFileRef fileRef, VoidP buf, int32 size, int32 offset, int32* read)
{
   // Must use SetFilePointerEx when running on the WP8 emulator, but not on device
#if defined WP8
   LARGE_INTEGER off = { 0 }, cur;
   off.LowPart = offset;
   return (SetFilePointerEx(fileRef, off, &cur, FILE_BEGIN) != 0) ? PDBRead(fileRef, buf, size, read) : false;
#else
   return (SetFilePointer(fileRef, offset, null, FILE_BEGIN) != 0xFFFFFFFFL) ? PDBRead(fileRef, buf, size, read) : false;
#endif
}

bool PDBWrite(PDBFileRef fileRef, VoidP buf, int32 size, int32* written)
{
   return WriteFile(fileRef, buf, size, written, null);
}

bool PDBWriteAt(PDBFileRef fileRef, VoidP buf, int32 size, int32 offset, int32* written)
{
   // Must use SetFilePointerEx when running on the WP8 emulator, but not on device
#if defined WP8
   LARGE_INTEGER off = { 0 }, cur;
   off.LowPart = offset;
   return (SetFilePointerEx(fileRef, off, &cur, FILE_BEGIN) != 0) ? PDBWrite(fileRef, buf, size, written) : false;
#else
   return (SetFilePointer(fileRef, offset, null, FILE_BEGIN) != 0xFFFFFFFFL) ? PDBWrite(fileRef, buf, size, written) : false;
#endif
}

bool PDBGetFileSize (PDBFileRef fileRef, int32* size)
{
#ifndef WP8
   return (*size = GetFileSize(fileRef, null)) != 0xFFFFFFFFL;
#else
   FILE_STANDARD_INFO finfo = { 0 };
   *size = 0xFFFFFFFF;
   if (GetFileInformationByHandleEx(fileRef, FileStandardInfo, &finfo, sizeof(finfo)) == 0)
   {
      return false;
   }

   // Size cannot exceed 32 bits
   *size = finfo.EndOfFile.LowPart;
   return true;
#endif
}

bool PDBGrowFileSize(PDBFileRef fileRef, int32 oldSize, int32 growSize)
{
   // Must use SetFilePointerEx when running on the WP8 emulator, but not on device
#if defined WP8
   LARGE_INTEGER off = { 0 }, cur;
   off.LowPart = oldSize + growSize;
   return (SetFilePointerEx(fileRef, off, &cur, FILE_BEGIN)) ? SetEndOfFile(fileRef) : false;
#else
   return (SetFilePointer(fileRef, oldSize + growSize, null, FILE_BEGIN) != 0xFFFFFFFFL) ? SetEndOfFile(fileRef) : false;
#endif
}

bool PDBListDatabasesIn(TCHARP path, bool recursive, HandlePDBSearchProcType proc, VoidP userVars)
{
   TCHAR searchPath[MAX_PATH];
   HANDLE searchHandle;
   WIN32_FIND_DATA findFileData;
   bool stopSearch = false;
   int32 pathLen = tcslen(path);

   tcscpy(searchPath, path);
   if (path[pathLen-1] != '/')
      tcscat(searchPath, TEXT("/*.*"));
   else
   {
      tcscat(searchPath, TEXT("*.*"));
      pathLen--;
   }

   if ((searchHandle = FindFirstFile(searchPath, &findFileData)) == INVALID_HANDLE_VALUE)
      return false;

   do
   {
      if (findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY && recursive)
      {
#if defined (WIN32) && !defined (WINCE)
         if (tcscmp(findFileData.cFileName, TEXT(".")) && tcscmp(findFileData.cFileName, TEXT("..")))
         {
#endif
            tcscpy(searchPath+pathLen+1, findFileData.cFileName);
            stopSearch = PDBListDatabasesIn(searchPath, recursive, proc, userVars);
#if defined (WIN32) && !defined (WINCE)
         }
#endif
      }
      else if (endsWithPDB(findFileData.cFileName)) // it's a pdb file.
      {
         if (proc)
         {
            tcscpy(searchPath+pathLen+1, findFileData.cFileName);
            stopSearch = (*proc)(searchPath, userVars);
         }
      }
   }
   while (FindNextFile(searchHandle, &findFileData) && !stopSearch);

   FindClose(searchHandle);
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
