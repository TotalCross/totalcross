// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



/****** IMPORTANT: IF YOU GET +30 LINK ERRORS WHEN BUILDING THIS FILE, YOU MUST FORCE THE REBUILD ALL. THIS IS A BUG OF VC6 *******/

#include "Conduit.h"

#define UNDEFINED_ERROR 16394 // Palm error code that means we have no idea of what went wrong.

//#define ENABLE_TRACE
#ifdef ENABLE_TRACE
#define TRACE(x) alert(x);
#else
#define TRACE(x)
#endif

/*******************

PREMISSES:
1. ADD C:\TOTALCROSS and c:\PALM to path

  To build the project, you need to setup eMbedded Visual Tools (or PocketPC SDK),
and setup directories in Visual C++ 6.0. Add ActiveSync inc\ and lib\ folders.
Use menu Tools -> Options -> directories tab. In my case,
I added C:\Windows CE Tools\wce300\MS Pocket PC\support\ActiveSync\inc (and \lib) folders.
********************/

extern "C"
{
   SYNC_API void tisC_initSync_i(NMParams p);
   SYNC_API void tisC_finishSync(NMParams p);
   SYNC_API void tisC_log_s(NMParams p);
   SYNC_API void tisC_yield(NMParams p);
   SYNC_API void tisC_register_sii(NMParams p);
   SYNC_API void tisC_unregister_si(NMParams p);

   SYNC_API void tisRF_delete_s(NMParams p);
   SYNC_API void tisRF_listFiles_s(NMParams p);
   SYNC_API void tisRF_copyFromRemote_ss(NMParams p);
   SYNC_API void tisRF_copyToRemote_ss(NMParams p);
   SYNC_API void tisRF_exec_ssib(NMParams p);

   SYNC_API void tisRPDBF_create(NMParams p);
   SYNC_API void tisRPDBF_close(NMParams p);
   SYNC_API void tisRPDBF_delete(NMParams p);
   SYNC_API void tisRPDBF_getRecordCount(NMParams p);
   SYNC_API void tisRPDBF_rwRecord_ibb(NMParams p);
   SYNC_API void tisRPDBF_deleteRecord_i(NMParams p);
   SYNC_API void tisRPDBF_listPDBs_ii(NMParams p);
   SYNC_API void tisRPDBF_getNextModifiedRecordIn(NMParams p);

   int32 listDatabases(TCHARP searchPath, HandlePDBSearchProcType proc, void *userVars, byte recursive);
}

///////////////////////////////////////////////////////////////////////////////
// Used only by ActiveSync
HMODULE rapiModule;

CeRapiInitProc          procCeRapiInit;
CeRapiUninitProc        procCeRapiUninit;
CeGetFileAttributesProc procCeGetFileAttributes;
CeFindCloseProc         procCeFindClose;
CeFindNextFileProc      procCeFindNextFile;
CeFindFirstFileProc     procCeFindFirstFile;
CeGetLastErrorProc      procCeGetLastError;
CeSetFilePointerProc    procCeSetFilePointer;
CeReadFileProc          procCeReadFile;
CeWriteFileProc         procCeWriteFile;
CeSetEndOfFileProc      procCeSetEndOfFile;
CeCloseHandleProc       procCeCloseHandle;
CeDeleteFileProc        procCeDeleteFile;
CeCreateFileProc        procCeCreateFile;
CeGetFileSizeProc       procCeGetFileSize;
CeMoveFileProc          procCeMoveFile;
CeRemoveDirectoryProc   procCeRemoveDirectory;
CeSetFileAttributesProc procCeSetFileAttributes;
CeFindAllFilesProc      procCeFindAllFiles;

CeCreateProcessProc     procCeCreateProcess;
CeRapiGetErrorProc      procCeRapiGetError;
///////////////////////////////////////////////////////////////////////////////
// Used only to configure HotSync in register, unregister and CfgConduit
HMODULE hsApiModule;
typedef long (__stdcall *HsRefreshConduitInfoProc) (void);
HsRefreshConduitInfoProc   procHsRefreshConduitInfo;

HMODULE condMgrModule;
typedef int (__stdcall *CmSetCreatorFileProc)            (const char* pCreatorID, const TCHAR* pFile);
typedef int (__stdcall *CmGetCreatorFileProc)            (const char* pCreatorID, TCHAR* pFile, int *piSize);
typedef int (__stdcall *CmConvertCreatorIDToStringProc)  (DWORD dwID, TCHAR* pString, int *piSize);
typedef int (__stdcall *CmSetCreatorTitleProc)           (const char* pCreatorID, const TCHAR* pTitle);
typedef int (__stdcall *CmSetCreatorPriorityProc)        (const char* pCreatorID, DWORD dwPriority);
typedef int (__stdcall *CmSetCreatorDirectoryProc)       (const char* pCreatorID, const TCHAR* pDirectory);
typedef int (__stdcall *CmGetCreatorDirectoryProc)       (const char* pCreatorID, TCHAR* pFile, int *piSize);
typedef int (__stdcall *CmSetCreatorNameProc)            (const char* pCreatorID, const TCHAR* pConduitName);
typedef int (__stdcall *CmInstallCreatorProc)            (const char* pCreator, int iType);
typedef int (__stdcall *CmRemoveConduitByCreatorIDProc)  (const char* pCreatorID);

CmSetCreatorFileProc             procCmSetCreatorFile;
CmGetCreatorFileProc             procCmGetCreatorFile;
CmConvertCreatorIDToStringProc   procCmConvertCreatorIDToString;
CmSetCreatorTitleProc            procCmSetCreatorTitle;
CmSetCreatorPriorityProc         procCmSetCreatorPriority;
CmSetCreatorDirectoryProc        procCmSetCreatorDirectory;
CmGetCreatorDirectoryProc        procCmGetCreatorDirectory;
CmSetCreatorNameProc             procCmSetCreatorName;
CmInstallCreatorProc             procCmInstallCreator;
CmRemoveConduitByCreatorIDProc   procCmRemoveConduitByCreatorID;
///////////////////////////////////////////////////////////////////////////////
// HotSync log
HMODULE hsLog20Module;
typedef long (*LogAddEntryProc)  (LPCTSTR pszEntry, Activity act, BOOL bTimeStamp);

LogAddEntryProc   procLogAddEntry;
///////////////////////////////////////////////////////////////////////////////
// Synchronization with HotSync
HMODULE sync20Module;
typedef SInt32 (*SyncUnRegisterConduitProc)     (CONDHANDLE handle);
typedef SInt32 (*SyncYieldCyclesProc)           (UInt16 wMaxMiliSecs);
typedef SInt32 (*SyncReadUserIDProc)            (CUserIDInfo &rInfo);
typedef UInt16 (*SyncGetHHOSVersionProc)        (UInt16* pwRomVMinor);
typedef SInt32 (*SyncRegisterConduitProc)       (CONDHANDLE &rHandle) ;
typedef SInt32 (*SyncCloseDBProc)               (HSByte fHandle);
typedef SInt32 (*SyncReadOpenDbInfoProc)        (SyncReadOpenDbInfoParams& rParam, SyncDatabaseInfoType& rInfo);
typedef SInt32 (*SyncOpenDBProc)                (const char* pName, SInt32 nCardNum, HSByte& rHandle, HSByte  openMode);
typedef SInt32 (*SyncCreateDBProc)              (CDbCreateDB& rDbStats);
typedef SInt32 (*SyncResetSyncFlagsProc)        (HSByte fHandle);
typedef SInt32 (*SyncDeleteDBProc)              (const char* pName, SInt32 nCardNum);
typedef SInt32 (*SyncGetDBRecordCountProc)      (HSByte fHandle, UInt16 &rNumRecs);
typedef SInt32 (*SyncReadNextModifiedRecProc)   (CRawRecordInfo& rInfo);
typedef SInt32 (*SyncWriteRecProc)              (CRawRecordInfo& rInfo);
typedef SInt32 (*SyncReadRecordByIndexProc)     (CRawRecordInfo& rInfo);
typedef SInt32 (*SyncDeleteRecProc)             (CRawRecordInfo& rInfo);
typedef SInt32 (*SyncFindDbByTypeCreatorProc)   (SyncFindDbByTypeCreatorParams& rParam, SyncDatabaseInfoType& rInfo);
typedef SInt32 (*SyncCallRemoteModuleProc)      (CCallModuleParams* pParams);

SyncUnRegisterConduitProc     procSyncUnRegisterConduit;
SyncYieldCyclesProc           procSyncYieldCycles;
SyncReadUserIDProc            procSyncReadUserID;
SyncGetHHOSVersionProc        procSyncGetHHOSVersion;
SyncRegisterConduitProc       procSyncRegisterConduit;
SyncCloseDBProc               procSyncCloseDB;
SyncReadOpenDbInfoProc        procSyncReadOpenDbInfo;
SyncOpenDBProc                procSyncOpenDB;
SyncCreateDBProc              procSyncCreateDB;
SyncResetSyncFlagsProc        procSyncResetSyncFlags;
SyncDeleteDBProc              procSyncDeleteDB;
SyncGetDBRecordCountProc      procSyncGetDBRecordCount;
SyncReadNextModifiedRecProc   procSyncReadNextModifiedRec;
SyncWriteRecProc              procSyncWriteRec;
SyncReadRecordByIndexProc     procSyncReadRecordByIndex;
SyncDeleteRecProc             procSyncDeleteRec;
SyncFindDbByTypeCreatorProc   procSyncFindDbByTypeCreator;
SyncCallRemoteModuleProc      procSyncCallRemoteModule;
///////////////////////////////////////////////////////////////////////////////

extern "C"
{
// Entry point for ActiveSync
SYNC_API bool LibOpen(OpenParams params)
{
   HMODULE tcvmModule = GetModuleHandle("tcvm.dll");
   TRACE("LibOpen");

   if ((rapiModule = LoadLibrary("rapi.dll")) == null)
   {
      if (params->commandLine[0] == 0 || params->commandLine[2] == 'p' || params->commandLine[2] == 'P') // if no command line passed or targetting palm, we don't need ActiveSync
         return true;
      throwException(params->currentContext, RuntimeException, "rapi.dll was not found. Was ActiveSync installed?");
      return false;
   }

   procCeRapiInit = (CeRapiInitProc) GetProcAddress(rapiModule, "CeRapiInit");
   procCeRapiUninit = (CeRapiUninitProc) GetProcAddress(rapiModule, "CeRapiUninit");
   procCeGetFileAttributes = (CeGetFileAttributesProc) GetProcAddress(rapiModule, "CeGetFileAttributes");
   procCeFindClose = (CeFindCloseProc) GetProcAddress(rapiModule, "CeFindClose");
   procCeFindNextFile = (CeFindNextFileProc) GetProcAddress(rapiModule, "CeFindNextFile");
   procCeFindFirstFile = (CeFindFirstFileProc) GetProcAddress(rapiModule, "CeFindFirstFile");
   procCeGetLastError = (CeGetLastErrorProc) GetProcAddress(rapiModule, "CeGetLastError");
   procCeSetFilePointer = (CeSetFilePointerProc) GetProcAddress(rapiModule, "CeSetFilePointer");
   procCeReadFile = (CeReadFileProc) GetProcAddress(rapiModule, "CeReadFile");
   procCeWriteFile = (CeWriteFileProc) GetProcAddress(rapiModule, "CeWriteFile");
   procCeSetEndOfFile = (CeSetEndOfFileProc) GetProcAddress(rapiModule, "CeSetEndOfFile");
   procCeCloseHandle = (CeCloseHandleProc) GetProcAddress(rapiModule, "CeCloseHandle");
   procCeDeleteFile = (CeDeleteFileProc) GetProcAddress(rapiModule, "CeDeleteFile");
   procCeCreateFile = (CeCreateFileProc) GetProcAddress(rapiModule, "CeCreateFile");
   procCeGetFileSize = (CeGetFileSizeProc) GetProcAddress(rapiModule, "CeGetFileSize");
   procCeMoveFile = (CeMoveFileProc) GetProcAddress(rapiModule, "CeMoveFile");
   procCeRemoveDirectory = (CeRemoveDirectoryProc) GetProcAddress(rapiModule, "CeRemoveDirectory");
   procCeSetFileAttributes = (CeSetFileAttributesProc) GetProcAddress(rapiModule, "CeSetFileAttributes");
   procCeFindAllFiles = (CeFindAllFilesProc) GetProcAddress(rapiModule, "CeFindAllFiles");

   procCeCreateProcess = (CeCreateProcessProc) GetProcAddress(rapiModule, "CeCreateProcess");
   procCeRapiGetError = (CeRapiGetErrorProc) GetProcAddress(rapiModule, "CeRapiGetError");

   if (!procCeRapiInit        || !procCeRapiUninit    || !procCeGetFileAttributes   || !procCeFindClose        ||
       !procCeFindNextFile    || !procCeFindFirstFile || !procCeGetLastError        || !procCeSetFilePointer   ||
       !procCeReadFile        || !procCeWriteFile     || !procCeSetEndOfFile        || !procCeCloseHandle      ||
       !procCeDeleteFile      || !procCeCreateFile    || !procCeGetFileSize         || !procCeMoveFile         ||
       !procCeRemoveDirectory || !procCeFindAllFiles  || !procCeSetFileAttributes   || !procCeCreateProcess    || !procCeRapiGetError)
   {
      FreeLibrary(rapiModule);
      throwException(params->currentContext, RuntimeException, "One or more rapi.dll functions were missing. Please be sure to be using the correct ActiveSync version.");
      return false;
   }
   return true;
}

static HMODULE loadPalmDll(CharP name)
{
   HKEY handle;
   uint32 ret;
   DWORD size;
   HMODULE mod;
   char path[MAX_PATHNAME];
   TRACE("loadPalmDll");
   ret = RegOpenKeyEx(HKEY_CURRENT_USER,"Software\\U.S. Robotics\\Pilot Desktop\\Core",0,KEY_READ,&handle);
   if (ret == NO_ERROR)
   {
      tzero(path);
      size = MAX_PATHNAME-1;
      ret = RegQueryValueEx(handle,"DesktopPath",NULL,NULL,(uint8*)path,&size);
      if (ret == NO_ERROR)
         xstrcat(path,"\\");
      RegCloseKey(handle);
   }
   xstrcat(path, name);
   mod = LoadLibrary(path);
   if (mod == null)
      mod = LoadLibrary(name);
   return mod;
}

static bool loadHotSyncCfgLibraries()
{
   TRACE("loadHotSyncCfgLibraries");
   // load all libraries
   hsApiModule = loadPalmDll("hsapi.dll");
   condMgrModule = loadPalmDll("condmgr.dll");

   if (!hsApiModule || !condMgrModule)
      goto error;

   // now get the procs
   procHsRefreshConduitInfo = (HsRefreshConduitInfoProc) GetProcAddress(hsApiModule, "HsRefreshConduitInfo");
   procCmSetCreatorFile = (CmSetCreatorFileProc) GetProcAddress(condMgrModule, "CmSetCreatorFile");
   procCmGetCreatorFile = (CmGetCreatorFileProc) GetProcAddress(condMgrModule, "CmGetCreatorFile");
   procCmConvertCreatorIDToString = (CmConvertCreatorIDToStringProc) GetProcAddress(condMgrModule, "CmConvertCreatorIDToString");
   procCmSetCreatorTitle = (CmSetCreatorTitleProc) GetProcAddress(condMgrModule, "CmSetCreatorTitle");
   procCmSetCreatorPriority = (CmSetCreatorPriorityProc) GetProcAddress(condMgrModule, "CmSetCreatorPriority");
   procCmSetCreatorDirectory = (CmSetCreatorDirectoryProc) GetProcAddress(condMgrModule, "CmSetCreatorDirectory");
   procCmGetCreatorDirectory = (CmGetCreatorDirectoryProc) GetProcAddress(condMgrModule, "CmGetCreatorDirectory");
   procCmSetCreatorName = (CmSetCreatorNameProc) GetProcAddress(condMgrModule, "CmSetCreatorName");
   procCmInstallCreator = (CmInstallCreatorProc) GetProcAddress(condMgrModule, "CmInstallCreator");
   procCmRemoveConduitByCreatorID = (CmRemoveConduitByCreatorIDProc) GetProcAddress(condMgrModule, "CmRemoveConduitByCreatorID");

   if (!procHsRefreshConduitInfo       || !procCmSetCreatorFile   || !procCmGetCreatorFile      ||
       !procCmConvertCreatorIDToString || !procCmSetCreatorTitle  || !procCmSetCreatorPriority  ||
       !procCmSetCreatorDirectory      || !procCmSetCreatorName   || !procCmInstallCreator      ||
       !procCmGetCreatorDirectory      || !procCmRemoveConduitByCreatorID)
   {
      if (procHsRefreshConduitInfo == null)
         MessageBox(null, "Could not load a required function from hsapid.dll.\nPalm Desktop 4.14 / HotSync 6.0.1 or newer are required.", "TotalCross Sync", 0);
      else
         MessageBox(null, "Could not load a required function from condmgr.dll.\nPalm Desktop 4.14 / HotSync 6.0.1 or newer are required.", "TotalCross Sync", 0);
      goto error;
   }
   return true;

error:
   if (hsApiModule) FreeLibrary(hsApiModule);
   if (condMgrModule) FreeLibrary(condMgrModule);
   return false;
}

static void unloadHotSyncCfgLibraries()
{
   TRACE("unloadHotSyncCfgLibraries");
   FreeLibrary(hsApiModule);
   FreeLibrary(condMgrModule);
}

}
//////////////////////////////////////////////////////////////////////////
//                       Shared data segment                            //
//////////////////////////////////////////////////////////////////////////

#define RECEIVE_BUFSIZE 65400 // making it larger will also make the dll larger - CANNOT BE LARGER THAN 65535 !

// what are we synchronizing? Palm or CE?
#define TARGETING_ACTIVESYNC   1
#define TARGETING_PALMDESKTOP  2

TCSettings tcSettingsP = getSettingsPtr();

//////////////////////////////////////////////////////////////////////////
//                       Auxiliary Functions                            //
//////////////////////////////////////////////////////////////////////////

static char* replaceSlashes(char *str)
{
   char *c;
   while ((c = xstrstr(str, "\\")))
      *c = '/';
   return str;
}

void swapDatabaseHeader2(DatabaseHeader *src, DatabaseHeader *dst)
{
   TRACE("swapDatabaseHeader2");
   xmemmove(dst->name, src->name, DB_NAME_LENGTH);
   dst->attributes          = SWAP16_FORCED(src->attributes);
   dst->version             = SWAP16_FORCED(src->version);
   dst->creationDate        = SWAP32_FORCED(src->creationDate);
   dst->modificationDate    = SWAP32_FORCED(src->modificationDate);
   dst->lastBackupDate      = SWAP32_FORCED(src->lastBackupDate);
   dst->modificationNumber  = SWAP32_FORCED(src->modificationNumber);
   dst->appInfoOffset       = SWAP32_FORCED(src->appInfoOffset);
   dst->sortInfoOffset      = SWAP32_FORCED(src->sortInfoOffset);
   dst->type                = SWAP32_FORCED(src->type);
   dst->creator             = SWAP32_FORCED(src->creator);
   dst->uniqueIDSeed        = SWAP32_FORCED(src->uniqueIDSeed);
   dst->nextRecordListID    = SWAP32_FORCED(src->nextRecordListID);
   dst->numRecords          = SWAP16_FORCED(src->numRecords);
}

typedef struct
{
   TCHAR uniName[32];
   char  fullName[50]; // 32 + 5 + 5 is enough
   char  crtrStr[5];
   char  typeStr[5];
   uint32 type;
   uint32 crtr;
   char* stringsP;
   int32 stringsCount;
} CLC_Vars;

static bool clcAddFile(TCHAR* fileName, void *userVars)
{
   CLC_Vars *vars = (CLC_Vars*) userVars;
   DatabaseHeader dbh;
   int32 bytesRW;
   HANDLE fileRef;
   TRACE("clcAddFile");

   if (!PDBCreateFile(fileName, false, true, &fileRef)) // createIt - false, readOnly - true
      goto finish;
   if (!PDBRead(fileRef, &dbh, 78, &bytesRW))
      goto finish;
   swapDatabaseHeader2(&dbh, &dbh);

   if (dbh.creator == 0 || dbh.type == 0)
      goto finish;
   if (vars->type != 0 && vars->type != dbh.type)
      goto finish;
   if (vars->crtr != 0 && vars->crtr != dbh.creator)
      goto finish;

   tcscpy(vars->uniName, tcsrchr(fileName, '/')+1);
   vars->uniName[(int32) tcslen(vars->uniName)-4] = 0; // remove the .pdb

   // append the crtr and the type to the name
   int2CRID((int32) dbh.creator, vars->crtrStr);
   int2CRID((int32) dbh.type, vars->typeStr);
   xstrprintf(vars->stringsP, "%s.%s.%s",vars->uniName,vars->crtrStr,vars->typeStr);
   vars->stringsCount++;
   vars->stringsP += xstrlen(vars->stringsP) + 1;

finish:
   PDBCloseFile(fileRef);
   return 0;
}

#define ggetUInt32(b) (uint32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )

#define sizeofPDBHandle(syncTarget)    (syncTarget == TARGETING_PALMDESKTOP ? sizeof(HSByte) : sizeof(TPDBFile))
#define pdbHandle4HotSync(h)           (*((HSByte*) h))
#define pdbHandle4ActiveSync(h)        ((DmOpenRef) h)

enum
{
   PDBFILE_READ_WRITE     = 3,
   PDBFILE_CREATE         = 4,
   PDBFILE_CREATE_EMPTY   = 5
};

static CCallModuleParams* prepareParams(int32 code, void* inBuf, int32 inBufSize)
{
   static CCallModuleParams params;
   static byte outBuffer[RECEIVE_BUFSIZE];
   TRACE("prepareParams");

   xmemzero(&params, sizeof(params));
   // send
   params.m_dwCreatorID = 'LBcn';
   params.m_dwTypeID = 'appl';
   params.m_wActionCode = code;
   params.m_pParam = inBuf;
   params.m_dwParamSize = (UInt32) inBufSize;
   // receive
   params.m_pResultBuf = outBuffer;
   params.m_dwResultBufSize = RECEIVE_BUFSIZE;
   return &params;
}

static bool queryRegistry(HKEY key, TCHAR *subkey, TCHAR *name, char *buf, uint32 size);

static int execTC(char* param, char* cmdLine, char* className)
{
   HMODULE tcvmModule;
   char buf[256];
   char scr[100];
   TRACE("execTC");
   if (!queryRegistry(HKEY_CURRENT_USER, (cmdLine[1]=='c')?"Software\\TotalCross\\ConduitCfgRect":"Software\\TotalCross\\ConduitSyncRect", className, scr, 100)) // guich@tc110_27
      xstrcpy(scr,"-2,-2,-1,-1"); // center,center,default,default
   xstrprintf(buf, "%s.tcz /cmd %s /scr %s", param, cmdLine, scr);
   if ((tcvmModule = LoadLibrary("tcvm.dll")) != null)
   {
      typedef int32 (*executeProgramProc)(CharP argsOriginal);
      executeProgramProc procExecuteProgram;
      if ((procExecuteProgram = (executeProgramProc) GetProcAddress(tcvmModule, "executeProgram")) != null)
      {
         int ret = (int) procExecuteProgram(buf);
         FreeLibrary(tcvmModule);
         return ret;
      }
   }
   return UNDEFINED_ERROR;
}

static bool remoteQueryRegistry(HKEY key, LPCWSTR subkey, LPCWSTR name, WCHAR* buf, uint32 size)
{
   HKEY handle;
   DWORD type = REG_DWORD;
   TRACE("remoteQueryRegistry");

   return CeRegOpenKeyEx(key, subkey, 0, KEY_READ, &handle) == NO_ERROR &&
          CeRegQueryValueEx(handle, name, null, &type, (LPBYTE) buf, (LPDWORD) &size) == NO_ERROR &&
          CeRegCloseKey(handle) == NO_ERROR;
}

static bool queryRegistry(HKEY key, TCHAR *subkey, TCHAR *name, char *buf, uint32 size)
{
   HKEY handle;
   DWORD type = REG_DWORD;
   TRACE("queryRegistry");

   return RegOpenKeyEx(key, subkey, 0, KEY_READ, &handle) == NO_ERROR &&
          RegQueryValueEx(handle, name, NULL, &type, (byte*) buf, (LPDWORD) &size) == NO_ERROR &&
          RegCloseKey(handle) == NO_ERROR;
}

static bool setRegistry(HKEY key, TCHAR *subkey, TCHAR *name, char *buf)
{
   HKEY handle;
   TRACE("setRegistry");

   return RegOpenKeyEx(key,subkey,0,KEY_ALL_ACCESS,&handle) == NO_ERROR &&
          RegSetValueEx(handle, name, 0, REG_SZ, (byte*) buf, (DWORD) (xstrlen(buf)+1)) == NO_ERROR && // store the length
          RegCloseKey(handle) == NO_ERROR;
}

static bool deleteRegistry(HKEY key, TCHAR *subkey, TCHAR *name)
{
   HKEY handle;
   TRACE("deleteRegistry");

   return RegOpenKeyEx(key,subkey,0,KEY_ALL_ACCESS,&handle) == NO_ERROR &&
          RegDeleteValue(handle,name) == NO_ERROR &&
          RegCloseKey(handle) == NO_ERROR;
}

static bool setEnviromentVariable(HKEY key, TCHAR* subkey)
{
   HKEY tcKey;
   CharP vmPathP;
   char pathVar[16384]; // 16KB buffer
   char pathLower[16384],vmPathLower[MAX_PATHNAME];
   DWORD pathVarSize = sizeof(pathVar);
   bool ok = false;
   char szEnv[12] = "Environment";
   Err err = 0;
   TRACE("setEnviromentVariable");

   vmPathP = getVMPath();
   if (RegOpenKeyEx(key, subkey, 0, KEY_ALL_ACCESS, &tcKey) == NO_ERROR)
   {
      if ((err = RegQueryValueEx(tcKey, "PATH", 0, null, (LPBYTE) pathVar, &pathVarSize)) == NO_ERROR)
      {
         // check if the path already contains the vm path. case insensitive
         xstrcpy(pathLower, pathVar);
         CharPToLower(pathLower);
         xstrcpy(vmPathLower, vmPathP);
         CharPToLower(vmPathLower);
         ok = xstrstr(pathLower, vmPathLower) != null || xstrstr(pathLower,"totalcross_home") != null;
      }

      if (!ok)
      {
         if (err == ERROR_FILE_NOT_FOUND)
            *pathVar = 0;
         else
         if (err == NO_ERROR)
            xstrcat(pathVar, ";");

         xstrcat(pathVar, vmPathP);
         if (RegSetValueEx(tcKey, "PATH", 0, REG_EXPAND_SZ, (BYTE*) pathVar, xstrlen(pathVar)) == NO_ERROR)
            ok = true;
      }
      SendMessage(HWND_BROADCAST, WM_WININICHANGE, 0, (LPARAM) &szEnv); //flsobral@tc114_14: Wait until all applications are notified about the registry update.
   }
   RegCloseKey(tcKey);
   return ok;
}

HRESULT CreateLink(char *path, char *args, char *workingDir, char *linkPath)
{
   HRESULT hres;
   IShellLink* psl;
   TRACE("CreateLink");

   if (SUCCEEDED(CoInitialize(NULL)))
   {
      hres = CoCreateInstance(CLSID_ShellLink, NULL, CLSCTX_INPROC_SERVER, IID_IShellLink, (void**) &psl);
      // Get a pointer to the IShellLink interface.
      if (SUCCEEDED(hres))
      {
         IPersistFile* ppf;

         // Set the path to the shortcut target
         psl->SetPath(path);
         psl->SetArguments(args);
         psl->SetWorkingDirectory(workingDir);

         // Query IShellLink for the IPersistFile interface for saving the shortcut in persistent storage.
         hres = psl->QueryInterface(IID_IPersistFile, (void**) &ppf);

         if (SUCCEEDED(hres))
         {
            WCHAR wsz[MAX_PATH];

            // Ensure that the string is ANSI.
            MultiByteToWideChar(CP_ACP, 0, linkPath, -1, wsz, MAX_PATH);

            // Save the link by calling IPersistFile::Save.
            hres = ppf->Save(wsz, TRUE);
            ppf->Release();
         }
         psl->Release();
      }
      CoUninitialize();
   }
   return hres;
}

static bool isSyncingEnabled(CharP className)
{
   DWORD status=0;
   TRACE("isSyncingEnabled");
   queryRegistry(HKEY_CURRENT_USER, "Software\\TotalCross\\EnableSync",className, (char*)&status, 4);
   return status == 1;
}

static Err getClassName(uint32 ucrid, char* className, char* path, CfgConduitInfoType* info)
{
   TRACE("getClassName");
   if (loadHotSyncCfgLibraries())
   {
      Err err;
      char crid[CM_CREATOR_ID_SIZE];
      int size = CM_CREATOR_ID_SIZE;
      *className = 0;

      if (info) TCHARP2CharPBuf(info->szUser, getUserName());
      // get the File0 from the registry
      procCmConvertCreatorIDToString(ucrid, crid, &size);
      size = 255;
      err = procCmGetCreatorFile(crid, className, &size);
      if (path)
      {
         size = 255;
         procCmGetCreatorDirectory(crid, path, &size);
      }
      unloadHotSyncCfgLibraries();
      return err;
   }
   return UNDEFINED_ERROR;
}

//////////////////////////////////////////////////////////////////////////
//                        Conduit Entry Points                          //
//////////////////////////////////////////////////////////////////////////
__declspec(dllexport) long GetConduitName(char *name, WORD maxLen)
{
   TRACE("GetConduitName");
   xmemzero(name, maxLen);
	strcpy(name, "TCSync");
	return CONDERR_NONE;
}

__declspec(dllexport) DWORD GetConduitVersion()
{
   TRACE("GetConduitVersion");
	return 1;
}

__declspec(dllexport) long OpenConduit(PROGRESSFN progress, CSyncProperties &sync)
{
   char className[256];
   char classDir[256];
   char classPath[512];
   char msg[128];
   Err err;
   TRACE("OpenConduit");

   // load all libraries now, no lazy loading.
   hsLog20Module = loadPalmDll("hslog20.dll");
   sync20Module = loadPalmDll("sync20.dll");
   if (!hsLog20Module || !sync20Module)
   {
      // exit and report
      MessageBoxA(null, "Could not load a required dll:\nhslog20.dll and/or sync20.dll", null, 0);
      return UNDEFINED_ERROR;
   }

   // now get procs
   procLogAddEntry = (LogAddEntryProc) GetProcAddress(hsLog20Module, "LogAddEntry");
   procSyncUnRegisterConduit = (SyncUnRegisterConduitProc) GetProcAddress(sync20Module, "SyncUnRegisterConduit");
   procSyncYieldCycles = (SyncYieldCyclesProc) GetProcAddress(sync20Module, "SyncYieldCycles");
   procSyncReadUserID = (SyncReadUserIDProc) GetProcAddress(sync20Module, "SyncReadUserID");
   procSyncGetHHOSVersion = (SyncGetHHOSVersionProc) GetProcAddress(sync20Module, "SyncGetHHOSVersion");
   procSyncRegisterConduit = (SyncRegisterConduitProc) GetProcAddress(sync20Module, "SyncRegisterConduit");
   procSyncCloseDB = (SyncCloseDBProc) GetProcAddress(sync20Module, "SyncCloseDB");
   procSyncReadOpenDbInfo = (SyncReadOpenDbInfoProc) GetProcAddress(sync20Module, "SyncReadOpenDbInfo");
   procSyncOpenDB = (SyncOpenDBProc) GetProcAddress(sync20Module, "SyncOpenDB");
   procSyncCreateDB = (SyncCreateDBProc) GetProcAddress(sync20Module, "SyncCreateDB");
   procSyncResetSyncFlags = (SyncResetSyncFlagsProc) GetProcAddress(sync20Module, "SyncResetSyncFlags");
   procSyncDeleteDB = (SyncDeleteDBProc) GetProcAddress(sync20Module, "SyncDeleteDB");
   procSyncGetDBRecordCount = (SyncGetDBRecordCountProc) GetProcAddress(sync20Module, "SyncGetDBRecordCount");
   procSyncReadNextModifiedRec = (SyncReadNextModifiedRecProc) GetProcAddress(sync20Module, "SyncReadNextModifiedRec");
   procSyncWriteRec = (SyncWriteRecProc) GetProcAddress(sync20Module, "SyncWriteRec");
   procSyncReadRecordByIndex = (SyncReadRecordByIndexProc) GetProcAddress(sync20Module, "SyncReadRecordByIndex");
   procSyncDeleteRec = (SyncDeleteRecProc) GetProcAddress(sync20Module, "SyncDeleteRec");
   procSyncFindDbByTypeCreator = (SyncFindDbByTypeCreatorProc) GetProcAddress(sync20Module, "SyncFindDbByTypeCreator");
   procSyncCallRemoteModule = (SyncCallRemoteModuleProc) GetProcAddress(sync20Module, "SyncCallRemoteModule");

   if (!procLogAddEntry || !procSyncUnRegisterConduit || !procSyncYieldCycles ||
       !procSyncReadUserID || !procSyncGetHHOSVersion || !procSyncRegisterConduit || !procSyncCloseDB ||
       !procSyncReadOpenDbInfo || !procSyncOpenDB || !procSyncCreateDB || !procSyncResetSyncFlags ||
       !procSyncDeleteDB || !procSyncGetDBRecordCount || !procSyncReadNextModifiedRec || !procSyncWriteRec ||
       !procSyncReadRecordByIndex || !procSyncDeleteRec || !procSyncFindDbByTypeCreator || !procSyncCallRemoteModule)
   {
      if (procLogAddEntry == null)
         MessageBox(null, "Could not load a required function from hslog20.dll.\nPalm Desktop 4.14 / HotSync 6.0.1 or newer are required.", "TotalCross Sync", 0);
      else
         MessageBox(null, "Could not load a required function FROM sync20.dll.\nPalm Desktop 4.14 / HotSync 6.0.1 or newer are required.", "TotalCross Sync", 0);
      return UNDEFINED_ERROR;
   }

   progress("Launching Conduit...");
   if (queryRegistry(sync.m_hKey, sync.m_Registry, "File0", className, sizeof(className))
      && queryRegistry(sync.m_hKey, sync.m_Registry, "Directory", classDir, sizeof(classDir)))
   {
      xstrcpy(classPath, classDir);
      xstrcat(classPath, "/");
      xstrcat(classPath, className);
      if (isSyncingEnabled(className) && (err = execTC(classPath, "/sp", className)) != 0)
      {
         xstrprintf(msg, "%s returned error code %d", className, err);
         procLogAddEntry(msg, slError, false);
         return UNDEFINED_ERROR;
      }
      return CONDERR_NONE;
   }
   xstrprintf(msg, "Conduit not properly registered, registering the conduit again will probably fix the problem");
   procLogAddEntry(msg, slSyncSessionCancelled, false);
   return UNDEFINED_ERROR;
}

__declspec(dllexport) long ConfigureConduit(CSyncPreference& pref)
{
   char className[256];
   char classDir[256];
   char fullPath[512];
   Err err;
   TRACE("ConfigureConduit");

   if (queryRegistry(pref.m_hKey, pref.m_Registry, "File0", className, sizeof(className))
      && queryRegistry(pref.m_hKey, pref.m_Registry, "Directory", classDir, sizeof(classDir))) // guich@tc110_25: now using the directory
   {
      xstrprintf(fullPath, "%s/%s",classDir,className);
      if ((err = execTC(fullPath, "/cp", className)) == 0)
         return CONDERR_NONE;
   }
   return UNDEFINED_ERROR;
}

// guich@555_6: implemented missing entrypoints
__declspec(dllexport) long CfgConduit(ConduitCfgEnum cfgType, void *pArgs, DWORD *pdwArgsSize)
{
   Err err = CONDERR_NONE;
   TRACE("CfgConduit");
   if (cfgType != eConfig1)
      err = CONDERR_UNSUPPORTED_CFGCONDUIT_ENUM;
   else
   {
      Err execErr;
      char className[256];
      char classDir[256];
      char fullPath[512];
      CfgConduitInfoType* info = (CfgConduitInfoType*)pArgs;
      if ((err = getClassName(info->dwCreatorId, className, classDir, info)) == CONDERR_NONE)
      {
         xstrprintf(fullPath, "%s/%s",classDir,className); // guich@tc110_25: now using the classDir
         if ((execErr = execTC(fullPath, "/cp", className)) != 0)
            err = UNDEFINED_ERROR;
      }
      return err;
   }
   return UNDEFINED_ERROR;
}

__declspec(dllexport) long GetConduitInfo(ConduitInfoEnum infoType, void *pInArgs, void *pOut, DWORD *pdwOutSize)
{
   TRACE("GetConduitInfo");
    //{char buf[100]; sprintf(buf, "ConduitInfoEnum: %d",infoType); MessageBox(null,buf,"alert",0);}
    if (!pOut)
        return CONDERR_INVALID_PTR;
    if (!pdwOutSize)
        return CONDERR_INVALID_OUTSIZE_PTR;

    switch (infoType)
    {
       case eConduitName:
          return GetConduitName((char*) pOut, (WORD) *pdwOutSize);
          break;

       case eDefaultAction:
       {
          Err err;
          char className[256];
          if (*pdwOutSize != sizeof(eSyncTypes))
             return CONDERR_INVALID_BUFFER_SIZE;
          if ((err=getClassName(((ConduitRequestInfoType*)pInArgs)->dwCreatorId, className, null, null)) == CONDERR_NONE)
             (*(eSyncTypes*) pOut) = isSyncingEnabled(className) ? eFast : eDoNothing;
          else
             return err;
          break;
       }
       case eMfcVersion:
          if (*pdwOutSize != sizeof(DWORD))
             return CONDERR_INVALID_BUFFER_SIZE;
          (*(DWORD*) pOut) = MFC_NOT_USED;
          break;

       default:
           return CONDERR_UNSUPPORTED_CONDUITINFO_ENUM;
    }
    return CONDERR_NONE;
}

//////////////////////////////////////////////////////////////////////////
//                      TotalCross Native Methods                       //
//////////////////////////////////////////////////////////////////////////
extern "C"
{
static void invalidateRemotePDBFile(Context currentContext, TCObject obj)
{
   TRACE("invalidateRemotePDBFile");
   RemotePDBFile_open(obj) = false;
   RemotePDBFile_dontFinalize(obj) = true;
   if (RemotePDBFile_pdbHandle(obj) != null)
   {
      setObjectLock(RemotePDBFile_pdbHandle(obj), UNLOCKED);
      RemotePDBFile_pdbHandle(obj) = null;
   }
   *getStaticFieldInt(loadClass(currentContext, "totalcross.io.sync.RemotePDBFile", true), "idle") = true; // guich@tc114_95: must set to idle
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisC_finishSync(NMParams p) // totalcross/io/sync/Conduit native boolean finishSync();
{
   TCObject conduit = p->obj[0];
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TRACE("tisC_finishSync");

   if (syncTarget == TARGETING_PALMDESKTOP)
   {
      TCObject* conduitHandleObject = Conduit_conduitHandle(conduit);
      CONDHANDLE* conduitHandle = (CONDHANDLE*) ARRAYOBJ_START(*conduitHandleObject);
      procSyncUnRegisterConduit(*conduitHandle);
      FreeLibrary(hsLog20Module);
      FreeLibrary(sync20Module);
   }
   else // syncTarget == TARGETING_ACTIVESYNC
   {
      procCeRapiUninit();
      FreeLibrary(rapiModule);
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisC_log_s(NMParams p) // totalcross/io/sync/Conduit native static public void log(String text);
{
   TCObject text = p->obj[0];
   int32 syncTarget = *Conduit_syncTarget(getMainClass());
   TRACE("tisC_log_s");

   if (text == null)
      throwNullArgumentException(p->currentContext, "text");
   else
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         TCHAR szText[256];
         JCharP2TCHARPBuf(String_charsStart(text), String_charsLen(text), szText);
         procLogAddEntry(szText, slText, false);
      }
      else // syncTarget == TARGETING_ACTIVESYNC
      {
         char szText[256];
         JCharP2CharPBuf(String_charsStart(text), String_charsLen(text), szText);
         debug(szText);
      }
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisC_yield(NMParams p) // totalcross/io/sync/Conduit native static public void yield();
{
   int32 syncTarget = *Conduit_syncTarget(getMainClass());

   UNUSED(p)
   if (syncTarget == TARGETING_PALMDESKTOP)
      procSyncYieldCycles(1);
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisC_register_i(NMParams p) // totalcross/io/sync/Conduit native boolean register(int priority);
{
   TCObject conduit = p->obj[0];
   int32 priority = p->i32[0];
   int32 syncTarget = *Conduit_syncTarget(conduit);
   bool ok = false;

   TCObject targetApplicationId = Conduit_targetApplicationId(conduit);
   TCObject conduitName = Conduit_conduitName(conduit);

   CharP appPathP = getAppPath();
   TRACE("tisC_register_i");

   if (syncTarget & TARGETING_PALMDESKTOP)
   {
      TCHAR szConduitNameT[MAX_PATH];
      char szCreatorA[5];
      JCharP2TCHARPBuf(String_charsStart(conduitName), String_charsLen(conduitName), szConduitNameT);
      JCharP2CharPBuf(String_charsStart(targetApplicationId), String_charsLen(targetApplicationId), szCreatorA);

      ok = setEnviromentVariable(HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Control\\Session Manager\\Environment");
      if (!ok)
         ok = setEnviromentVariable(HKEY_CURRENT_USER, "Environment");

      if (ok && loadHotSyncCfgLibraries())
      {
         // registers the conduit
         if (procCmInstallCreator(szCreatorA, CONDUIT_APPLICATION) == -1005)
            throwException(p->currentContext, RuntimeException, "Could not install the conduit, the creator id specified is already installed");
         else
         {
            TCHAR conduitDllPath[MAX_PATH];
            CharP2TCHARPBuf(getVMPath(), conduitDllPath);
            tcscat(conduitDllPath, "/TCSync.dll");
            TCHAR conduitAppDir[MAX_PATH];
            CharP2TCHARPBuf(appPathP, conduitAppDir);

            ok = procCmSetCreatorName(szCreatorA, conduitDllPath) == SYNCERR_NONE &&
                 procCmSetCreatorDirectory(szCreatorA, conduitAppDir) == SYNCERR_NONE &&
                 procCmSetCreatorFile(szCreatorA, szConduitNameT) == SYNCERR_NONE &&
                 procCmSetCreatorPriority(szCreatorA, priority) == SYNCERR_NONE &&
                 procCmSetCreatorTitle(szCreatorA, szConduitNameT) == SYNCERR_NONE;
            procHsRefreshConduitInfo(); // just refresh, no restart is needed. - guich@511_4: removed this from comparision.
         }
         unloadHotSyncCfgLibraries();
      }
   }

   if (syncTarget & TARGETING_ACTIVESYNC)
   {
      // create the link that will be called to run the conduit
      char shortcutPath[MAX_PATHNAME], appPath[MAX_PATHNAME];
      char szConduitNameA[MAX_PATH];
      TCHAR szCreatorT[5];
      JCharP2CharPBuf(String_charsStart(conduitName), String_charsLen(conduitName), szConduitNameA);
      JCharP2TCHARPBuf(String_charsStart(targetApplicationId), String_charsLen(targetApplicationId), szCreatorT);

      xstrprintf(shortcutPath, "%s/Run%s.lnk", appPathP, szConduitNameA);
      xstrprintf(appPath, "%s/%s.exe", appPathP, szConduitNameA);
      if (CreateLink(appPath, "/sw", "", shortcutPath) == S_OK)
         ok = setRegistry(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows CE Services\\AutoStartOnConnect", szCreatorT, shortcutPath);
   }

   p->retI = ok;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisC_unregister(NMParams p) // totalcross/io/sync/Conduit native boolean unregister();
{
   TCObject conduit = p->obj[0];
   int32 syncTarget = *Conduit_syncTarget(conduit);
   int32 ret = 1;

   TCObject targetApplicationId = Conduit_targetApplicationId(conduit);
   TCObject conduitName = Conduit_conduitName(conduit);
   TRACE("tisC_unregister");

   if (syncTarget & TARGETING_PALMDESKTOP)
   {
      if (loadHotSyncCfgLibraries())
      {
         char szCreatorA[5];
         JCharP2CharPBuf(String_charsStart(targetApplicationId), String_charsLen(targetApplicationId), szCreatorA);
         ret = procCmRemoveConduitByCreatorID(szCreatorA);
         procHsRefreshConduitInfo(); // just refresh, no restart is needed.
         unloadHotSyncCfgLibraries();
      }
   }

   if (syncTarget & TARGETING_ACTIVESYNC)
   {
      TCHAR szCreatorT[5];
      JCharP2TCHARPBuf(String_charsStart(targetApplicationId), String_charsLen(targetApplicationId), szCreatorT);
      ret = (ret == 0) ? ret : deleteRegistry(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows CE Services\\AutoStartOnConnect", szCreatorT);
   }

   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisC_initSync(NMParams p) // totalcross/io/sync/Conduit native private boolean initSync();
{
   TCObject conduit = p->obj[0];
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject conduitHandleObject = null;
   TCObject targetAppPath = *Conduit_targetAppPath(conduit);

   int32 r = 0;
   int32 ret = 0;

   CharP userNameP;
   WCHAR remoteAppPath[MAX_PATH];
   int32 targetAppPathLen = String_charsLen(targetAppPath);
   TRACE("tisC_initSync");

   xmemmove(remoteAppPath, String_charsStart(targetAppPath), targetAppPathLen*sizeof(WCHAR));
   *(remoteAppPath + targetAppPathLen) = 0;

   conduitHandleObject = createByteArray(p->currentContext, sizeof(CONDHANDLE));
   CONDHANDLE* conduitHandle = (CONDHANDLE*) ARRAYOBJ_START(conduitHandleObject);

   if (syncTarget == TARGETING_PALMDESKTOP)
   {
      if (procSyncRegisterConduit(*conduitHandle) != SYNCERR_NONE)
      {
         procLogAddEntry("Conduit was not allowed to run by the HotSync. Make sure the application this conduit is registered for is installed on the target device", slError, false);
         *conduitHandle = 0;
      }
      else
      {
         // get the hh rom version number
         WORD mi, ma;
         ma = procSyncGetHHOSVersion(&mi);
         *tcSettingsP->romVersionPtr = (((int32) ma<<8) | mi)<<16;
         // get the user
         CUserIDInfo info;
         xmemzero(&info, sizeof(info));
         if (procSyncReadUserID(info) == SYNCERR_NONE)
         {
            userNameP = getUserName();
            if (info.m_pName)
               xstrcpy(userNameP, info.m_pName);
            else
               *userNameP = 0;
         }
      }
   }
   else
   {
      WCHAR currPath[MAX_PATH];
      *conduitHandle = (CONDHANDLE) ((procCeRapiInit() == NO_ERROR) ? 1 : 0);

      // check if the target path exists
      lstrcpyW(currPath, remoteAppPath);
      DWORD fileAttributes = procCeGetFileAttributes(currPath);
      if (fileAttributes == 0xFFFFFFFF || !(fileAttributes & FILE_ATTRIBUTE_DIRECTORY))
      {
         wsprintfW(currPath, L"TotalCross/%s", remoteAppPath);
         fileAttributes = procCeGetFileAttributes(currPath);
         if (fileAttributes == 0xFFFFFFFF || !(fileAttributes & FILE_ATTRIBUTE_DIRECTORY))
         {
            p->retI = 0; //flsobral@tc114_45: No longer throws an exception if the application is not found at the device, preventing the conduit to display errors when connecting to devices without the target application installed.
            return;
         }
      }
      lstrcpyW(remoteAppPath, currPath);
   }
   //lstrcatW(remoteAppPath, L"/");

   ret = *conduitHandle != 0;
   *Conduit_conduitHandle(conduit) = conduitHandleObject;
   *Conduit_targetAppPath(conduit) = createStringObjectFromJCharP(p->currentContext, (JCharP) remoteAppPath, -1);

   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_create(NMParams p) // totalcross/io/sync/RemotePDBFile native void create() throws totalcross.io.IllegalArgumentIOException, totalcross.io.FileNotFoundException, totalcross.io.IOException;
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject targetAppPath = *Conduit_targetAppPath(conduit);
   WCHAR remoteAppPath[MAX_PATH];
   int32 targetAppPathLen = String_charsLen(targetAppPath);
   TRACE("tisRPDBF_create");
   xmemmove(remoteAppPath, String_charsStart(targetAppPath), targetAppPathLen*sizeof(WCHAR));
   *(remoteAppPath + targetAppPathLen) = 0;

   TCObject pdbFile = p->obj[0];
   TCObject pdbName = RemotePDBFile_name(pdbFile);
   int32 mode = RemotePDBFile_mode(pdbFile) & 7;
   int32 flags = mode & ~7;
   int32 pdbNameLen = String_charsLen(pdbName);
   char szPdbNameA[42];
   TCHAR szPdbNameT[42];
   JCharP2CharPBuf(String_charsStart(pdbName), pdbNameLen, szPdbNameA);
   JCharP2TCHARPBuf(String_charsStart(pdbName), pdbNameLen, szPdbNameT);
   uint32 creator = ggetUInt32((uint8*) &szPdbNameA[pdbNameLen - 9]);
   uint32 type = ggetUInt32((uint8*) &szPdbNameA[pdbNameLen - 4]);

   int32 ret = 0;

   szPdbNameA[pdbNameLen - 10] = 0;

   TCObject pdbHandleObject = createByteArray(p->currentContext, sizeofPDBHandle(syncTarget));
   if (!pdbHandleObject)
      return;
   RemotePDBFile_pdbHandle(pdbFile) = pdbHandleObject;
   uint8* pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   // if create, try to create it; if it already exists, open in read_write mode.
   if (mode == PDBFILE_CREATE)
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         CDbCreateDB  createInfo;

         createInfo.m_Creator     = creator;
         createInfo.m_Type        = type;
         createInfo.m_Flags       = (eDbFlags) flags; // guich@581_3
         createInfo.m_CardNo      = 0;
         xstrcpy(createInfo.m_Name, szPdbNameA);
         createInfo.m_Version     = 1;
         createInfo.m_FileHandle  = 0;

         switch (procSyncCreateDB(createInfo))
         {
            case SYNCERR_FILE_ALREADY_EXIST:
               mode = PDBFILE_READ_WRITE;
               break;
            case SYNCERR_NONE:
               pdbHandle4HotSync(pdbHandle) = createInfo.m_FileHandle;
               break;
         }
      }
      else
      {
         TCHAR szFileName[MAX_PATHNAME]; //flsobral@tc115_11: ensure myDmCreateDatabase uses the targetAppPath to create the file. Otherwise it may use the Conduit's application path and fail.

         String2TCHARPBuf(targetAppPath, szFileName);
         if (szFileName[targetAppPathLen - 1] != '/' && szFileName[targetAppPathLen - 1] != '\\')
            tcscat(szFileName, TEXT("/"));
         szPdbNameT[pdbNameLen - 10] = 0;
         tcscat(szFileName, szPdbNameT);
         szPdbNameT[pdbNameLen - 10] = '.';
         if (myDmCreateDatabase(szFileName, creator, type, false) == 0)
            mode = PDBFILE_READ_WRITE;
      }
   }
   if (mode != PDBFILE_CREATE)
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         int32 openMode = eDbShowSecret;
         if (mode == PDBFILE_READ_WRITE)
            openMode |= eDbWrite | eDbRead;
         if (procSyncOpenDB(szPdbNameA, 0, pdbHandle4HotSync(pdbHandle), openMode) != SYNCERR_NONE)
            pdbHandle4HotSync(pdbHandle) = 0;
         else
         {
            // retrieve information about the database to confirm that it is the
            // one with creator and type specified
            SyncReadOpenDbInfoParams params;
            SyncDatabaseInfoType info;
            params.bDbHandle = pdbHandle4HotSync(pdbHandle);
            params.bOptFlags = SYNC_DB_INFO_OPT_GET_ATTRIBUTES;
            if (procSyncReadOpenDbInfo(params, info) != SYNCERR_NONE || info.baseInfo.m_Creator != creator || info.baseInfo.m_DbType != type)
            {
               procSyncCloseDB(pdbHandle4HotSync(pdbHandle));
               *pdbHandle = 0;
            }
         }
      }
      else // syncTarget == TARGETING_ACTIVESYNC
      {
         TCHAR pdbPath[MAX_PATH];
         JCharP2TCHARPBuf((JCharP) remoteAppPath, targetAppPathLen, pdbPath);
         if (*(remoteAppPath + targetAppPathLen - 1) != '/')
            tcscat(pdbPath, "/");
         tcscat(pdbPath, szPdbNameT);
         pdbPath[(int32) tcslen(pdbPath)-10] = 0;
         VoidP dbId = null;

         if (pdbHandleObject)
         {
            dbId = pdbHandle;
            dbId = myDmFindDatabase(p->currentContext, pdbPath, (PDBFile) dbId);

            if (dbId) // check if the creator id and type are those expected
            {
               uint32 creatorId, typeId;
               Err err;

               err = myDmDatabaseInfo(dbId, null, null, null, null, null, null, null, null, null, &typeId, &creatorId);
               if (err == errNone && creator == creatorId && type == typeId)
                  pdbHandle = (uint8*) DmOpenDatabase(dbId, dmModeReadWrite);
            }
         }
      }
   }

   if (pdbHandle != null && *pdbHandle != 0)
      RemotePDBFile_open(pdbFile) = 1;
   else
      invalidateRemotePDBFile(p->currentContext, pdbFile);
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_close(NMParams p) // totalcross/io/sync/RemotePDBFile native public void close() throws totalcross.io.IOException;
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);

   TCObject pdbHandleObject;
   uint8* pdbHandle;
   Err err = NO_ERROR;
   TCObject pdbFile = p->obj[0];
   TRACE("tisRPDBF_close");

   pdbHandleObject = RemotePDBFile_pdbHandle(pdbFile);
   if (!RemotePDBFile_open(pdbFile) || pdbHandleObject == null)
   {
      err = 9999;
      goto error;
   }
   pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   if (*pdbHandle)
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         procSyncResetSyncFlags(pdbHandle4HotSync(pdbHandle)); // guich@570_14: try through here too
         err = procSyncCloseDB(pdbHandle4HotSync(pdbHandle));
      }
      else // syncTarget == TARGETING_ACTIVESYNC
         err = myDmCloseDatabase(pdbHandle4ActiveSync(pdbHandle));
      *pdbHandle = 0;
   }

error:
   if (err != NO_ERROR)
      debug("Close error - %d", err);
   invalidateRemotePDBFile(p->currentContext, pdbFile);
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_delete(NMParams p) // totalcross/io/sync/RemotePDBFile native public void delete() throws totalcross.io.IOException;
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);

   TCObject pdbFile = p->obj[0];
   Err err;
   TCObject pdbHandleObject=null;
   uint8* pdbHandle=null;
   TCObject remotePDB;
   TRACE("tisRPDBF_delete");
   pdbHandleObject = RemotePDBFile_pdbHandle(pdbFile);
   if (!RemotePDBFile_open(pdbFile) || pdbHandleObject == null)
   {
      err = 9999;
      goto error;
   }
   pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   remotePDB = p->obj[0];

   if (remotePDB != null)
   {
      if (*pdbHandle)
      {
         if (syncTarget == TARGETING_PALMDESKTOP)
         {
            if ((err = procSyncCloseDB(pdbHandle4HotSync(pdbHandle))) == NO_ERROR)
            {
               TCObject pdbName = RemotePDBFile_name(remotePDB);
               char szPdbName[42];
               char* dot;
               JCharP2CharPBuf(String_charsStart(pdbName), String_charsLen(pdbName), szPdbName);
               dot = strchr(szPdbName, '.');
               if (dot) *dot = 0; // cut off the creator and type.
               procSyncDeleteDB(szPdbName, 0);
            }
         }
         else // syncTarget == TARGETING_ACTIVESYNC
         {
            err = myDmDeleteDatabase(pdbHandle4ActiveSync(pdbHandle));
         }
         //*pdbHandle = 0;
      }

      //RemotePDBFile_open(remotePDB) = 0;
error:
      if (err != NO_ERROR)
         debug("tisRPDBF_delete error - %d", err);
      invalidateRemotePDBFile(p->currentContext, pdbFile);
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_getRecordCount(NMParams p) // totalcross/io/sync/RemotePDBFile native public int getRecordCount() throws totalcross.io.IOException;
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   Err err = NO_ERROR;
   TCObject pdbFile = p->obj[0];
   UInt16 recordCount;
   TCObject pdbHandleObject;
   uint8* pdbHandle;
   TRACE("tisRPDBF_getRecordCount");

   pdbHandleObject = RemotePDBFile_pdbHandle(pdbFile);
   if (!RemotePDBFile_open(pdbFile) || pdbHandleObject == null)
   {
      err = 9999;
      goto error;
   }
   pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   if (*pdbHandle)
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
         err = procSyncGetDBRecordCount(pdbHandle4HotSync(pdbHandle), recordCount);
      else // syncTarget == TARGETING_ACTIVESYNC
         recordCount = myDmNumRecords(pdbHandle4ActiveSync(pdbHandle));
   }
error:
   if (err != NO_ERROR)
   {
      p->retI = -1;
      debug("SyncGetDBRecordCount returned %d", err);
   }
   else
      p->retI = (int32) recordCount;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_getNextModifiedRecordIn(NMParams p) // totalcross/io/sync/RemotePDBFile native public int getNextModifiedRecordIndex();
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);

   TCObject pdbFile = p->obj[0];
   int32 lastSearchedRec;
   TCObject pdbHandleObject;
   uint8* pdbHandle;
   int32 ret = -1;
   TRACE("tisRPDBF_getNextModifiedRecordIn");
   pdbHandleObject = RemotePDBFile_pdbHandle(pdbFile);
   if (!RemotePDBFile_open(pdbFile) || pdbHandleObject == null)
   {
      p->retI = -1;
      return;
   }
   lastSearchedRec = RemotePDBFile_lastSearchedRec(pdbFile);
   pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   if (*pdbHandle)
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         CRawRecordInfo info;
         xmemzero(&info, sizeof(info));
         info.m_FileHandle = pdbHandle4HotSync(pdbHandle);
         if (procSyncReadNextModifiedRec(info) == SYNCERR_NONE)
            ret = info.m_RecIndex;
      }
      else // syncTarget == TARGETING_ACTIVESYNC
      {
         int32 recordCount = myDmNumRecords(pdbHandle4ActiveSync(pdbHandle));
         uint16 attr;
         while (++lastSearchedRec < recordCount)
         {
            if (myDmRecordInfo(pdbHandle4ActiveSync(pdbHandle), lastSearchedRec, &attr) != NO_ERROR)
            {
               ret = -1;
               break;
            }
            if (attr & dmRecAttrDirty)
            {
               ret = lastSearchedRec;
               break;
            }
         }
         RemotePDBFile_lastSearchedRec(pdbFile) = lastSearchedRec;
      }
   }

   // set it available to handle next command
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_rwRecord_ibb(NMParams p) // totalcross/io/sync/RemotePDBFile native int rwRecord(int idx, totalcross.io.ByteArrayStream bas, boolean read);
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject pdbFile = p->obj[0];
   TCObject pdbHandleObject = RemotePDBFile_pdbHandle(pdbFile);
   TRACE("tisRPDBF_rwRecord_ibb");

   if (!RemotePDBFile_open(pdbFile) || pdbHandleObject == null)
   {
      p->retI = -1;
      return;
   }
   uint8* pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   int32 idx = p->i32[0];
   TCObject bas = p->obj[1];
   int32 isRead = p->i32[1];
   TCObject basBuf;
   uint8* buf;
   int32 pos;

   int32 ret = -1;

   if (bas == null)
      throwNullArgumentException(p->currentContext, "bas");
   else
   {
      basBuf = ByteArrayStream_buffer(bas);
      buf = ARRAYOBJ_START(basBuf);
      pos = ByteArrayStream_pos(bas);

      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         CRawRecordInfo info;
         xmemzero(&info, sizeof(info));
         info.m_FileHandle = pdbHandle4HotSync(pdbHandle);
         info.m_RecIndex = idx;

         info.m_pBytes = (byte*) buf;
         if (isRead)
         {
            info.m_TotalBytes = ARRAYOBJ_LEN(basBuf);
            if (procSyncReadRecordByIndex(info) == SYNCERR_NONE)
               ret = info.m_RecSize;
         }
         else
         {
            if (idx < 0 || (idx >= 0 && procSyncReadRecordByIndex(info) == SYNCERR_NONE)) // appending or overwriting. if overwriting, get the record id
            {
               info.m_RecSize = pos;
               info.m_Attribs = 0;//&= ~eRecAttrDirty; // guich@570_14: reset the dirty flag
               if (procSyncWriteRec(info) == SYNCERR_NONE)
                  ret = info.m_RecSize;
            }
         }
      }
      else // syncTarget == TARGETING_ACTIVESYNC
      {
         if (isRead)
         {
            MemHandle mh = myDmQueryRecord(pdbHandle4ActiveSync(pdbHandle), idx);
            uint32 size = myMemHandleSize(mh);
            if (mh && size > 0)
            {
               CharP recPtr = (CharP) MemHandleLock(mh);
               xmemmove(buf, recPtr, size);
               MemHandleUnlock(mh);
               ret = size;
            }
         }
         else
         {
            uint16 uidx = (uint16) idx;
            MemHandle recH = myDmNewRecord(pdbHandle4ActiveSync(pdbHandle), &uidx, pos);
            if (recH != NULL)
            {
               boolean ok = false;
               uint16 attr;
               CharP recPtr = (CharP) MemHandleLock(recH);
               if (myDmWrite(recPtr, 0, buf, pos) == 0)
               {
                  ok = true;
                  ret = pos;
               }
               MemHandleUnlock(recH);
               myDmReleaseRecord(pdbHandle4ActiveSync(pdbHandle), uidx, 1);
               if (myDmRecordInfo(pdbHandle4ActiveSync(pdbHandle), uidx, &attr) == 0)
                  myDmSetRecordInfo(pdbHandle4ActiveSync(pdbHandle), uidx, attr & ~0X40); // guich@570_14: reset dirty attribute

            }
         }
      }

      if (ret > 0 && isRead)
         ByteArrayStream_pos(bas) = 0; // this is how we inform
      p->retI = ret;
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_deleteRecord_i(NMParams p) // totalcross/io/sync/RemotePDBFile native public void deleteRecord(int index) throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException;
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);

   TCObject pdbFile = p->obj[0];
   Err err = NO_ERROR;
   int32 index = p->i32[0];
   TCObject pdbHandleObject;
   uint8* pdbHandle;
   TRACE("tisRPDBF_deleteRecord_i");
   pdbHandleObject = RemotePDBFile_pdbHandle(pdbFile);
   if (!RemotePDBFile_open(pdbFile) || pdbHandleObject == null)
   {
      err = 9999;
      goto error;
   }
   pdbHandle = ARRAYOBJ_START(pdbHandleObject);

   if (syncTarget == TARGETING_PALMDESKTOP)
   {
      CRawRecordInfo info;
      xmemzero(&info, sizeof(info));
      info.m_FileHandle = pdbHandle4HotSync(pdbHandle);
      info.m_RecIndex = index;
      if ((err = procSyncReadRecordByIndex(info)) != NO_ERROR) // get record id
         err = procSyncDeleteRec(info); // delete it.
   }
   else
      err = myDmRemoveRecord(pdbHandle4ActiveSync(pdbHandle), index);

error:
   if (err != NO_ERROR)
      debug("tisRPDBF_deleteRecord_i error - %d", err);
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRPDBF_listPDBs_ii(NMParams p) // totalcross/io/sync/RemotePDBFile native public static String[] listPDBs(int crtr, int type);
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject targetAppPath = *Conduit_targetAppPath(conduit);
   WCHAR remoteAppPath[MAX_PATH];
   int32 targetAppPathLen = String_charsLen(targetAppPath);
   TRACE("tisRPDBF_listPDBs_ii");
   xmemmove(remoteAppPath, String_charsStart(targetAppPath), targetAppPathLen*sizeof(WCHAR));
   *(remoteAppPath + targetAppPathLen) = 0;

   int32 crtr = p->i32[0];
   int32 type = p->i32[1];
   TCObject stringArray = null;

   char stringsBuf[RECEIVE_BUFSIZE];
   char* stringsP = stringsBuf;
   int32 stringsCount = 0;

   // prepare the parameters
   if (crtr || type) // one of the two must be provided
   {
      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         char fullName[50]; // 32 + 5 + 5 is enough
         char szCrtr[5];
         char szType[5];
         SyncFindDbByTypeCreatorParams rParams;
         SyncDatabaseInfoType rInfo;

         rParams.bOptFlags = SYNC_DB_INFO_OPT_GET_ATTRIBUTES;
         rParams.bSrchFlags = SYNC_DB_SRCH_OPT_NEW_SEARCH; // only set for first search
         rParams.dwCreator = (UInt32) crtr; //ipcParam1;
         rParams.dwType = (UInt32) type; //ipcParam2;
         while (1)
         {
            // find next database of desired creator and type
            rInfo.dwReserved = 0; // must set to zero before calling function, per documentation
            rInfo.baseInfo.m_dwReserved = 0; // must set to zero before calling function, per documentation

            if (procSyncFindDbByTypeCreator(rParams, rInfo) != SYNCERR_NONE)
               break;

            // append the crtr and the type to the name
            int2CRID((int32) rInfo.baseInfo.m_Creator, szCrtr);
            int2CRID((int32) rInfo.baseInfo.m_DbType, szType);
            xstrprintf(stringsP, "%s.%s.%s", rInfo.baseInfo.m_Name, szCrtr, szType);
            xstrprintf(fullName, "%s.%s.%s", rInfo.baseInfo.m_Name, szCrtr, szType);
            stringsCount++;
            stringsP += xstrlen(stringsP) + 1;

            // set flag to zero so we get next database of creator and type
            rParams.bSrchFlags = 0;
         }
      }
      else
      {
         CLC_Vars vars;
         vars.crtr = crtr;
         vars.type = type;
         vars.stringsP = stringsP;
         vars.stringsCount = 0;

         TCHARP searchPath = JCharP2TCHARP((JCharP) remoteAppPath, (int32) lstrlenW(remoteAppPath));
         listDatabases(searchPath, &clcAddFile, &vars, 1);
         stringsCount = vars.stringsCount;
         xfree(searchPath);
      }

      // fill the string array
      if (stringsCount != 0) // no databases found?
      {
         p->retO = stringArray = createStringArray(p->currentContext, stringsCount);
         // we will hold the found objects in the native stack to avoid them being collected
         if (stringArray != NULL)
         {
            TCObject *strings = (TCObject*) ARRAYOBJ_START(stringArray);
            stringsP = stringsBuf;
            while (stringsCount-- > 0)
            {
               int32 len = (int32) xstrlen(stringsP);
               *strings++ = createStringObjectFromCharP(p->currentContext, stringsP, len);
               stringsP += len + 1;
            }
            setObjectLock(p->retO, UNLOCKED);
         }
      }
   }
   p->retO = stringArray;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRF_listFiles_s(NMParams p) // totalcross/io/sync/RemoteFile native public static String[] listFiles(String dir);
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject dir = p->obj[0];
   int32 dirLen;
   Err err = NO_ERROR;
   TCObject stringArray = null;
   TRACE("tisRF_listFiles_s");

   if (dir == null)
      throwNullArgumentException(p->currentContext, "dir");
   else
   {
      dirLen = String_charsLen(dir);
      char szDirA[MAX_PATH];
      JCharP2CharPBuf(String_charsStart(dir), dirLen, szDirA);
      replaceSlashes(szDirA);
      uint32 stringsCount;

      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         // enumerate volumes or list files?
         CCallModuleParams* params = (*szDirA == 0) ? prepareParams(LIST_VOLUMES, null, 0) : prepareParams(LIST_FILES, (void*) szDirA, dirLen+1);
         if ((err = procSyncCallRemoteModule(params)) != errNone)
            ; //debug("tisRF_listFiles_s error - %d, %d", err, params->m_dwResultCode);
         else
         if ((stringsCount = params->m_dwResultCode) > 0 &&    // no databases found?
            (p->retO = stringArray = createStringArray(p->currentContext, stringsCount)) != null)
         {
            // fill the string array
            TCObject* strings = (TCObject*) ARRAYOBJ_START(stringArray);
            char* string = (char*) params->m_pResultBuf; //bufP;
            while (stringsCount-- > 0)
            {
               int32 stringLen = (int32) xstrlen(string);
               *strings++ = createStringObjectFromCharP(p->currentContext, string, stringLen);
               string += stringLen + 1;
            }
            setObjectLock(p->retO, UNLOCKED);
         }
      }
      else if (dirLen > 0)
      {
         //DWORD filesCount;
         LPCE_FIND_DATA fd;
         WCHAR szDirW[MAX_PATH];
         int32 i;

         CharP2JCharPBuf(szDirA, *(szDirA + dirLen - 1) == '/' ? dirLen - 1 : dirLen, (JCharP) szDirW, true);
         lstrcatW(szDirW, L"/*.*");

         // get the files
         if (!procCeFindAllFiles(szDirW, FAF_NAME | FAF_ATTRIBUTES, (LPDWORD) &stringsCount, &fd))
            err = procCeGetLastError();
         else
         if (stringsCount > 0)
         {
            p->retO = stringArray = createStringArray(p->currentContext, stringsCount);
            if (stringArray != null)
            {
               TCObject* strings = (TCObject*) ARRAYOBJ_START(stringArray);
               for (i = 0 ; i < (int32) stringsCount ; i++)
               {
                  TCHAR fileName[MAX_PATH];

                  JCharP2TCHARPBuf((JCharP) fd[i].cFileName, lstrlenW(fd[i].cFileName), fileName);
                  if (fd[i].dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
                     tcscat(fileName, "/");
                  *strings++ = createStringObjectFromTCHAR(p->currentContext, fileName, -1);
               }
               setObjectLock(p->retO, UNLOCKED);
            }
         }
      }
      p->retO = stringArray;
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRF_copyToRemote_ss(NMParams p) // totalcross/io/sync/RemoteFile native public static boolean copyToRemote(String srcFile, String dstFile);
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject srcFile = p->obj[0];
   TCObject dstFile = p->obj[1];
   char szSrcFile[MAX_PATH];
   char szDstFile[MAX_PATH];
   int32 srcFileLen;
   int32 dstFileLen;
   Err err = NO_ERROR;
   int32 result = 0;
   TRACE("tisRF_copyToRemote_ss");

   if (srcFile == null)
      throwNullArgumentException(p->currentContext, "srcFile");
   else
   if (dstFile == null)
      throwNullArgumentException(p->currentContext, "dstFile");
   else
   {
      srcFileLen = String_charsLen(srcFile);
      dstFileLen = String_charsLen(dstFile);
      JCharP2CharPBuf(String_charsStart(srcFile), srcFileLen, szSrcFile);
      JCharP2CharPBuf(String_charsStart(dstFile), dstFileLen, szDstFile);
      replaceSlashes(szSrcFile);
      replaceSlashes(szDstFile);

      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         uint32 ofs = 0;
         char bufn[20];
         size_t read;
         FILE* srcRef;
         byte buffer[RECEIVE_BUFSIZE];
         char* dstFileNameP = (char*) buffer;
         char* offsetP;
         char* dataP;

         if ((srcRef = fopen(szSrcFile, "rb")) == NULL)
         {
            alert("fopen szSrcFile failed");
         }
         else
         {
            xstrcpy(dstFileNameP, szDstFile);
            offsetP = dstFileNameP + dstFileLen + 1;

            xstrcpy(offsetP, _ltoa(ofs, bufn, 10));
            dataP = offsetP + xstrlen(offsetP) + 1;
            CCallModuleParams *params = prepareParams(WRITE_FILE, buffer, (uint32) (dataP - dstFileNameP)); // verify if file exists, return its size and transfer the first bytes

            if ((read = fread(dataP, 1, (RECEIVE_BUFSIZE - (dataP - dstFileNameP)), srcRef)) <= 0)
               result = 1;
            params->m_dwParamSize = (uint32) ((dataP - dstFileNameP) + read);
            if ((err = procSyncCallRemoteModule(params)) != errNone)
            {
               alert("1.SyncCallRemoteModule in copyTo %X, %d", err, params->m_dwResultCode);
               // do something
            }
            else
            {
               ofs += (UInt32) read;
               while (!result)
               {
                  xstrcpy(offsetP, _ltoa(ofs, bufn, 10));
                  dataP = offsetP + xstrlen(offsetP) + 1;
                  params = prepareParams(WRITE_FILE, buffer, (uint32) (dataP - dstFileNameP)); // verify if file exists, return its size and transfer the first bytes
                  if ((read = fread(dataP, 1, (RECEIVE_BUFSIZE - (dataP - dstFileNameP)), srcRef)) <= 0)
                  {
                     result = 1;
                     break;
                  }
                  params->m_dwParamSize = (uint32) ((dataP - dstFileNameP) + read);
                  if ((err = procSyncCallRemoteModule(params)) != errNone)
                  {
                     alert("2.SyncCallRemoteModule in copyTo %X, %d", err, params->m_dwResultCode);
                     break;
                     // do something
                  }
                  ofs += (UInt32) read;
               }
            }
            fclose(srcRef);
         }
      }
      else
      {
	      char buff[5006];
	      HANDLE hSrc, hDest;
	      DWORD dwSz, dwRes;
	      WCHAR dstFileW[MAX_PATH];

         replaceSlashes(szSrcFile);
         replaceSlashes(szDstFile);
         CharP2JCharPBuf(szDstFile, dstFileLen, (JCharP) dstFileW, true);

	      hSrc = CreateFile(szSrcFile, GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, 0);
	      if (hSrc == INVALID_HANDLE_VALUE)
            err = GetLastError();
         else
         {
	         hDest = procCeCreateFile(dstFileW, GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, CREATE_ALWAYS, 0, 0);
	         if (hDest == INVALID_HANDLE_VALUE)
            {
               err = procCeGetLastError();
               CloseHandle(hSrc);
            }
            else
            {
      	      dwSz = 5000;

	            while (1)
               {
		            ReadFile(hSrc, buff, dwSz, &dwRes, NULL);
		            if (dwRes == 0) break;
		            procCeWriteFile(hDest, buff, dwRes, &dwRes, NULL);
		            if (dwRes != dwSz) break;
	            }

	            CloseHandle(hSrc);
	            procCeCloseHandle(hDest);
               result = 1;
            }
         }
      }

      if (err != NO_ERROR)
      {
         result = false;
         debug("tisRF_copyToRemote_ss error - %d", err);
      }

      p->retI = result;
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRF_copyFromRemote_ss(NMParams p) // totalcross/io/sync/RemoteFile native public static boolean copyFromRemote(String srcFile, String dstFile);
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject srcFile = p->obj[0];
   TCObject dstFile = p->obj[1];
   char szSrcFile[MAX_PATH];
   char szDstFile[MAX_PATH];
   int32 srcFileLen;
   int32 dstFileLen;
   Err err = NO_ERROR;
   int32 result = 0;
   TRACE("tisRF_copyFromRemote_ss");

   if (srcFile == null)
      throwNullArgumentException(p->currentContext, "srcFile");
   else
   if (dstFile == null)
      throwNullArgumentException(p->currentContext, "dstFile");
   else
   {
      // Convert and normalize arguments
      srcFileLen = String_charsLen(srcFile);
      dstFileLen = String_charsLen(dstFile);
      JCharP2CharPBuf(String_charsStart(srcFile), srcFileLen, szSrcFile);
      JCharP2CharPBuf(String_charsStart(dstFile), dstFileLen, szDstFile);
      replaceSlashes(szSrcFile);
      replaceSlashes(szDstFile);

      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         int ofs = 0;
         byte buffer[MAX_PATH*2];
         char* srcFileNameP = (char*) buffer;
         char* offsetP;

         // the instruction that will be passed to the remote module is:
         // open file F, position at I, and tranfer the bytes
         // this way, there will be no need to persist data at the device
         // the remote file must answer with the total number of bytes transfered.

         xstrcpy(srcFileNameP, szSrcFile);
         offsetP = srcFileNameP + srcFileLen + 1;
         xstrcpy(offsetP, "0"); // ofs = 0

         CCallModuleParams *params = prepareParams(READ_FILE, buffer, (uint32) (srcFileLen + xstrlen(offsetP) + 2)); // verify if file exists, return its size and transfer the first bytes
         if ((err = procSyncCallRemoteModule(params)) != errNone)
         {
            alert("first SyncCallRemoteModule %d", err);
            // do something
         }
         else
         {
            char bufn[20];
            uint32 size = params->m_dwResultCode;
            int32 read = (int32) params->m_dwActResultSize;
            FILE* dstRef = fopen(szDstFile, "wb");
            void* bytesRead = params->m_pResultBuf;
            while (dstRef != NULL && read > 0) // why boring with another if? :-)
            {
               fwrite(bytesRead, 1, read, dstRef);
               ofs += read;
               size -= read;
               if (size <= 0)
                  break;

               xstrcpy(offsetP, _ltoa(ofs, bufn, 10));
               params = prepareParams(READ_FILE, buffer, (uint32) (srcFileLen + xstrlen(offsetP) + 2)); // open file F at position I
               if ((err = procSyncCallRemoteModule(params)) != errNone)
               {
                  alert("SyncCallRemoteModule %d", err);
                  break;
               }
               read = (int32) params->m_dwActResultSize;
            }
            if (dstRef != null)
               fclose(dstRef);
            result = (size == 0);
         }
      }
      else
      {
	      char buff[5006];
	      HANDLE hSrc,hDest;
	      DWORD dwSz, dwRes;
	      WCHAR srcFileW[MAX_PATH];

         CharP2JCharPBuf(szSrcFile, srcFileLen, (JCharP) srcFileW, true);

	      hSrc = procCeCreateFile(srcFileW, GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, 0);
	      if (hSrc == INVALID_HANDLE_VALUE)
            err = procCeGetLastError();
         else
         {
   	      hDest = CreateFile(szDstFile, GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, CREATE_ALWAYS, 0, 0);
	         if (hDest == INVALID_HANDLE_VALUE)
            {
               err = GetLastError();
               procCeCloseHandle(hSrc);
            }
            else
            {
               dwSz = 5000;

               while (1)
               {
	               procCeReadFile(hSrc, buff, dwSz, &dwRes, NULL);
	               if (dwRes == 0) break;
	               WriteFile(hDest, buff, dwRes, &dwRes, NULL);
	               if (dwRes != dwSz) break;
               }

               procCeCloseHandle(hSrc);
               CloseHandle(hDest);
               result = 1;
            }
         }
      }

      if (err != NO_ERROR)
      {
         result = false;
         debug("tisRF_copyFromRemote_ss error - %d", err);
      }
   }
   p->retI = result;
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRF_delete_s(NMParams p) // totalcross/io/sync/RemoteFile native public static boolean delete(String fileOrFolder);
{
   TCObject conduit = getMainClass();
   int32 syncTarget = *Conduit_syncTarget(conduit);
   TCObject fileOrFolder = p->obj[0];
   int32 fileOrFolderLen;
   char szFileOrFolderA[MAX_PATH];
   Err err = NO_ERROR;
   TRACE("tisRF_delete_s");

   if (fileOrFolder == null)
      throwNullArgumentException(p->currentContext, "fileOrFolder");
   else
   {
      fileOrFolderLen = String_charsLen(fileOrFolder);
      JCharP2CharPBuf(String_charsStart(fileOrFolder), fileOrFolderLen, szFileOrFolderA);
      replaceSlashes(szFileOrFolderA);

      if (syncTarget == TARGETING_PALMDESKTOP)
      {
         CCallModuleParams *params = prepareParams(DELETE_FILE, szFileOrFolderA, (uint32) fileOrFolderLen+1);
         if ((err = procSyncCallRemoteModule(params)) != errNone)
         {
            //alert("SyncCallRemoteModule in delete %d", err);
         }
      }
      else
      {
	      WCHAR szFileOrFolderW[MAX_PATH];
         DWORD attr;
         CharP2JCharPBuf(szFileOrFolderA, fileOrFolderLen, (JCharP) szFileOrFolderW, true);

         // first find the file type
         attr = procCeGetFileAttributes(szFileOrFolderW);
         if ((attr & FILE_ATTRIBUTE_READONLY) != 0) // if the file is readonly, reset the attribute before deleting
            if (!procCeSetFileAttributes(szFileOrFolderW, attr & ~FILE_ATTRIBUTE_READONLY))
               err = procCeGetLastError();

         if (err == NO_ERROR)
         {
            if (((attr & FILE_ATTRIBUTE_DIRECTORY) != 0) && !procCeRemoveDirectory(szFileOrFolderW))
               err = procCeGetLastError();
            else
            if (!procCeDeleteFile(szFileOrFolderW))
               err = procCeGetLastError();
         }
      }
      p->retI = err == NO_ERROR;
      //if (err != NO_ERROR)
         //debug("tisRF_delete_s error - %d", err);
   }
}
//////////////////////////////////////////////////////////////////////////
SYNC_API void tisRF_exec_ssib(NMParams p) // totalcross/io/sync/RemoteFile native public static int exec(String command, String args, int launchCode, boolean wait);
{
   TCObject command = p->obj[0];
   TCObject args = p->obj[1];

   WCHAR applicationName[MAX_PATHNAME];
   WCHAR commandLine[MAX_PATHNAME];
   PROCESS_INFORMATION processInformation;

   Err initRet;
   Err createProcessRet;

   xmemzero(applicationName, sizeof(applicationName));
   xmemmove(applicationName, String_charsStart(command), String_charsLen(command) * sizeof(WCHAR));

   xmemzero(commandLine, sizeof(commandLine));
   xmemmove(commandLine, String_charsStart(args), String_charsLen(args) * sizeof(WCHAR));

   initRet = procCeRapiInit();
   if (initRet == NO_ERROR || initRet == CERAPI_E_ALREADYINITIALIZED)
   {
      if ((createProcessRet = procCeCreateProcess(applicationName, commandLine, null, null, false, 0, null, null, null, &processInformation)) != 0)
         p->retI = NO_ERROR;
      else if ((createProcessRet = procCeGetLastError()) != 0)
         p->retI = createProcessRet;
      else
         p->retI = procCeRapiGetError();

      if (initRet == NO_ERROR)
         procCeRapiUninit();
   }
   else
      p->retI = initRet;
}
} // extern "C"