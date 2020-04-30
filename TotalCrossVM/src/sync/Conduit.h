// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef CONDUIT_H
#define CONDUIT_H

#if defined(WIN32) || defined(WINCE)
 #ifdef SYNC_EXPORTS
  #define SYNC_API __declspec(dllexport)
 #else
  #define SYNC_API __declspec(dllimport)
 #endif
#else
 #define SYNC_API extern
#endif

#if defined(WIN32)
 #define INCL_WINSOCK_API_PROTOTYPES 0
 #define INCL_WINSOCK_API_TYPEDEFS 1 
 #include <winsock2.h> // flsobral@tc114: we must include winsock2 before windows to avoid conflict with winsock.
 #include <windows.h>
 #include <winnt.h>
#endif

// PalmOS - Palm Desktop
#include "condmgr.h"
#include "syncmgr.h"

#ifdef __cplusplus
extern "C" {
 #include "tcvm.h" // defined after syncmgr and before condapi to avoid type redefinition
}
#endif

#include "condapi.h"
#include "hsapi.h"
#include "hslog.h"

// Windows CE - ActiveSync
#include <rapi.h>
#if !defined CERAPI_E_ALREADYINITIALIZED
 #define CERAPI_E_ALREADYINITIALIZED     0x80041001
#endif
#include "shlobj.h"

#include <stdio.h>
#include <tlhelp32.h>

#ifdef __cplusplus
extern "C" {
 typedef HRESULT (__stdcall *CeRapiInitProc)(void);
 typedef HRESULT (__stdcall *CeRapiUninitProc)(void);
 typedef DWORD (__stdcall *CeGetFileAttributesProc)( LPCWSTR );
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
 typedef BOOL (__stdcall *CeRemoveDirectoryProc)(LPCWSTR lpPathName);
 typedef BOOL (__stdcall *CeSetFileAttributesProc)(LPCWSTR lpFileName, DWORD dwFileAttributes);
 typedef BOOL (__stdcall *CeFindAllFilesProc)(LPCWSTR szPath, DWORD dwFlags, LPDWORD lpdwFoundCount, LPLPCE_FIND_DATA ppFindDataArray);

 typedef BOOL (__stdcall *CeCreateProcessProc)
 (
   LPCWSTR lpApplicationName, 
   LPCWSTR lpCommandLine, 
   LPSECURITY_ATTRIBUTES lpProcessAttributes, 
   LPSECURITY_ATTRIBUTES lpThreadAttributes, 
   BOOL bInheritHandles, 
   DWORD dwCreationFlags, 
   LPVOID lpEnvironment, 
   LPWSTR lpCurrentDirectory, 
   LPSTARTUPINFO lpStartupInfo, 
   LPPROCESS_INFORMATION lpProcessInformation 
 );
 typedef HRESULT (__stdcall *CeRapiGetErrorProc)(void);

 extern CeRapiInitProc procCeRapiInit;
 extern CeRapiUninitProc procCeRapiUninit;
 extern CeGetFileAttributesProc procCeGetFileAttributes;
 extern CeFindCloseProc procCeFindClose;
 extern CeFindNextFileProc procCeFindNextFile;
 extern CeFindFirstFileProc procCeFindFirstFile;
 extern CeGetLastErrorProc procCeGetLastError;
 extern CeSetFilePointerProc procCeSetFilePointer;
 extern CeReadFileProc procCeReadFile;
 extern CeWriteFileProc procCeWriteFile;
 extern CeSetEndOfFileProc procCeSetEndOfFile;
 extern CeCloseHandleProc procCeCloseHandle;
 extern CeDeleteFileProc procCeDeleteFile;
 extern CeCreateFileProc procCeCreateFile;
 extern CeGetFileSizeProc procCeGetFileSize;
 extern CeMoveFileProc procCeMoveFile;
 extern CeRemoveDirectoryProc procCeRemoveDirectory;
 extern CeSetFileAttributesProc procCeSetFileAttributes;
 extern CeFindAllFilesProc procCeFindAllFiles;

 extern CeCreateProcessProc procCeCreateProcess;
 extern CeRapiGetErrorProc procCeRapiGetError;

 #include "../palmdb/palmdb.h"
 typedef HANDLE PDBFileRef;
 extern bool PDBCreateFile(TCHARP fullPath, bool createIt, bool readOnly, PDBFileRef* fileRef);
 extern bool PDBRead(PDBFileRef fileRef, VoidP buf, int32 size, int32* read);
 extern bool PDBCloseFile(PDBFileRef fileRef);

 enum ActionCode
 {
    LIST_VOLUMES  = 1,
    LIST_FILES    = 2,
    DELETE_FILE   = 3,
    READ_FILE     = 4,
    WRITE_FILE    = 5,
 };

}
#endif

#endif