// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#if !defined(WINCE) && !defined(WP8)
#include <tlhelp32.h>
#endif

static void vmSetTime(TCObject time)
{
   SYSTEMTIME newTime;

   newTime.wYear         = Time_year(time);
   newTime.wMonth        = Time_month(time);
   newTime.wDay          = Time_day(time);
   newTime.wHour         = Time_hour(time);
   newTime.wMinute       = Time_minute(time);
   newTime.wSecond       = Time_second(time);
#ifndef WINCE
   newTime.wMilliseconds = Time_millis(time);
#endif

#ifndef WP8
   SetLocalTime(&newTime);
   SetLocalTime(&newTime);
#endif
}
//XXX como carregar a dll do TC
#define IOCTL_HAL_REBOOT 0x101003C
void rebootDevice()
{
#ifdef WINCE
   typedef BOOL (__stdcall *KernelIoControlProc)( DWORD, LPVOID, DWORD, LPVOID, DWORD, LPDWORD );
   KernelIoControlProc procKernelIoControl = (KernelIoControlProc)GetProcAddress(coreDll, TEXT("KernelIoControl"));
   if (procKernelIoControl != null)
      procKernelIoControl(IOCTL_HAL_REBOOT, NULL, 0, NULL, 0, NULL);
#elif !defined WP8
   ExitWindowsEx(EWX_REBOOT,0);
#else
   /*HMODULE dll = LoadLibrary(TEXT("coredll.dll"));
   SetSystemPowerStateProc SetSystemPowerState = (SetSystemPowerStateProc)GetProcAddress(dll, TEXT("SetSystemPowerState"));
   SetSystemPowerState(null, 0x00800000, 4096);*/
#endif
}


typedef HANDLE (__stdcall *RegisterServiceProc)(LPCWSTR lpszType,  DWORD dwIndex,  LPCWSTR lpszLib,  DWORD dwInfo);
typedef BOOL (__stdcall *DeregisterServiceProc)(HANDLE hDevice);
typedef HANDLE (__stdcall *GetServiceHandleProc)(LPWSTR szPrefix, LPWSTR szDllName, DWORD pdwDllBuf);

#ifndef WP8 // defined in cppwrapper
static int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   VoidP startInfo = null;
   int32 ret=-1;

#if !defined(WP8)
   TCHAR pathargs[1024];
   BOOL ok;
   DWORD err;
   PROCESS_INFORMATION processInfo;
#ifndef WINCE
   STARTUPINFO si;
   xmemzero(&si, sizeof(si));
   startInfo = &si;
#endif
#ifdef WINCE
   if (lstrcmp(szCommand,TEXT("unregister service"))==0)
   {
      HANDLE dll = LoadLibrary(TEXT("coredll.dll")),srv;
      DeregisterServiceProc deregisterService = (DeregisterServiceProc)GetProcAddress(dll, TEXT("DeregisterService"));
      GetServiceHandleProc getServiceHandle = (GetServiceHandleProc)GetProcAddress(dll, TEXT("GetServiceHandle"));
      ret = 0;
      srv = getServiceHandle(L"TSV0:",0,0);
      if (srv != 0)
         ret = deregisterService(srv) != 0;
      FreeLibrary(dll);
      return ret;
   }
   if (lstrcmp(szCommand,TEXT("register service"))==0)
   {
      HANDLE dll = LoadLibrary(TEXT("coredll.dll"));
      RegisterServiceProc registerService = (RegisterServiceProc)GetProcAddress(dll, TEXT("RegisterService"));
      char dllpath[255];
      HANDLE srv;
      xstrcpy(dllpath,vmPath);
      xstrcat(dllpath,"\\tcvm.dll");
      replaceChar(dllpath,'/','\\');
      CharP2TCHARPBuf(dllpath,pathargs);
      srv = registerService(L"TSV",0,pathargs,0);
      FreeLibrary(dll);
      return srv != 0;
   }
#endif
#endif
   //XXX all below should be reworked
#if !defined WP8
#ifndef WINCE
   if (strEq(szCommand,"running process"))
   {
    bool exists = false;
    PROCESSENTRY32 entry;
	HANDLE snapshot;
    entry.dwSize = sizeof(PROCESSENTRY32);
    snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (Process32First(snapshot, &entry))
       while (!exists && Process32Next(snapshot, &entry))
          if (lstrcmpi(entry.szExeFile, szArgs) == 0)
             exists = true;
    CloseHandle(snapshot);
    return exists;
   }
   else
   if (strEq(szCommand,"viewer") || strEq(szCommand,"url"))
   {
	  ShellExecute(NULL, "open", szArgs, NULL, NULL, SW_SHOWNORMAL);
	  return 0;
   }
#endif
   ok = CreateProcess(szCommand, szArgs, null, null, false, 0, null, null, startInfo, &processInfo); // guich@tc100b5_16: iexplore requires this mode.
   err = GetLastError();

   if (!ok)
   {
      lstrcpy(pathargs, szCommand); // concats the parameters. not sure why this works on most features but does not work when passing the parameters separately
      if (szArgs && szArgs[0] != 0) // guich@tc100b5_15
      {
         lstrcat(pathargs, TEXT(" "));
         lstrcat(pathargs, szArgs);
      }
      ok = CreateProcess(null, pathargs, null, null, false, 0, null, null, startInfo, &processInfo);
      err = GetLastError();
   }

#ifndef WINCE
   if (!ok && xstrchr(szCommand,'/') == null && xstrchr(szCommand,'\\') == null) // if the user didn't specified a path
   {
      // 1. search in all folders in the path environment variable
      char pathEnv[4097],*p=pathEnv,*p2;
      char fullname[MAX_PATHNAME],*sep,*sep0;
      if (GetEnvironmentVariable("path",pathEnv, 4096) != 0)
      {
         strcat(pathEnv, ";"); // make sure it ends with ;
         while ((sep=xstrchr(p,';')) != null)
         {
            sep0 = sep;
            if (*p == '"') p++;
            if (*(sep-1) == '"') sep--;
            *sep = 0;
            xstrcpy(fullname, p);
            if (*(sep-1) != '/' && *(sep-1) != '\\')
               strcat(fullname, "\\");
            strcat(fullname,szCommand);

            xstrprintf(pathargs,"%s %s",fullname, szArgs);
            ok = CreateProcess(null, pathargs, null, null, false, 0, null, null, startInfo, &processInfo);
            err = GetLastError();
            if (ok != null)
               break;
            p = sep0+1;
         }
      }
      if (!ok) // 2. check in the HKEY_CLASSES_ROOT\Applications\APPLICATION.EXE\shell\open\command
      {
         uint32 size=sizeof(fullname);
         HKEY handle=(HKEY)0;
         DWORD err;
         xstrprintf(fullname,"Applications\\%s\\shell\\open\\command",szCommand);
         err = RegOpenKeyEx(HKEY_CLASSES_ROOT, fullname, 0, KEY_READ, &handle);
         if (err != 0)
         {
            xstrprintf(fullname,"Applications\\%s\\shell\\new\\command",szCommand);
            err = RegOpenKeyEx(HKEY_CLASSES_ROOT, fullname, 0, KEY_READ, &handle);
         }
         if (err == 0)
         {
            err = RegQueryValueEx(handle,null,null,null,(uint8 *)fullname,&size);
            if (err == 0)
            {
               // returns something like "%ProgramFiles%\Windows NT\Acess�rios\WORDPAD.EXE" "%1"
               // get the first part
               char* f = fullname;
               if (*f == '"')
                  f++;
               p = xstrchr(f,'"');
               if (p)
                  *p = 0;
               xstrcpy(fullname, f);
               RegCloseKey(handle);
               // now expand %ProgramFiles% and other envs
               while ((p = xstrchr(fullname,'%')) != null)
               {
                  p2 = xstrchr(p+1,'%');
                  if (!p2)
                     break; // didn't found the ending % ?
                  *p2 = 0;
                  if (GetEnvironmentVariable(p+1, pathEnv, 4096) <= 0)
                     break;
                  xstrcat(pathEnv, p2+1);
                  xstrcpy(fullname,pathEnv); // copy back to fullname
               }
               xstrprintf(pathargs,"%s %s",fullname, szArgs);
               ok = CreateProcess(null, pathargs, null, null, false, 0, null, null, startInfo, &processInfo);
               err = GetLastError();
            }
         }
      }
   }
#endif
   if (ok)
   {
      ret = 0;
      if (wait)
      {
         WaitForSingleObject(processInfo.hProcess, INFINITE);
         GetExitCodeProcess(processInfo.hProcess, &ret);
      }
      CloseHandle(processInfo.hThread);
      CloseHandle(processInfo.hProcess);
   }
   else ret = err;
#endif
   return ret;
}
#endif

void vmSetAutoOff(bool enable)
{
#ifdef WINCE
   if (!enable && oldAutoOffValue == 0)
   {
      SystemParametersInfo(SPI_GETBATTERYIDLETIMEOUT, 0, &oldAutoOffValue, 0); // get the previous value
      if (oldAutoOffValue == 0) 
         oldAutoOffValue = -1; // guich@tc113_22: some systems returns 0, so we change it to -1
      else
         SystemParametersInfo(SPI_SETBATTERYIDLETIMEOUT, 0, null, 0); // set to 0 (2nd parameter)
   }
   else
   if (enable && oldAutoOffValue != 0)
   {
      if (oldAutoOffValue != -1) // guich@tc113_22
         SystemParametersInfo(SPI_SETBATTERYIDLETIMEOUT, oldAutoOffValue, null, 0);
      oldAutoOffValue = 0;
   }
#elif defined WP8
   vmSetAutoOffCPP(enable);
#endif
}

//////////// START OF KEY INTERCEPTION FUNCTIONS
//XXX: O que s�o hot keys? N�o fa�o a menor id�ia do que fazer aqui e acho que nem faz sentido
void registerHotkeys(Int32Array keys, bool isRegister)
{
   if (mainHWnd != null)
   {
      #ifdef WINCE
      int32 n;
      typedef BOOL (__stdcall *UnregisterFunc1Proc)( UINT, UINT );
      HINSTANCE hCoreDll;
      if ((hCoreDll = LoadLibrary(TEXT("coredll.dll"))) != null)
      {
         UnregisterFunc1Proc procUnregisterFunc = (UnregisterFunc1Proc)GetProcAddress(hCoreDll, _T("UnregisterFunc1"));
         if (procUnregisterFunc != null)
            for (n = ARRAYLEN(keys); n-- > 0; keys++)
            {
               procUnregisterFunc(MOD_WIN, *keys);
               if (isRegister)
                  RegisterHotKey(mainHWnd, *keys, MOD_WIN | MOD_KEYUP, *keys);
               else
                  UnregisterHotKey(mainHWnd, *keys);
            }
         if (hCoreDll) FreeLibrary(hCoreDll);
         return;
      }
      //throwException(currentContext, RuntimeException, "Could not find entry point for hotkeys registration");
      #elif !defined WP8
      int32 n;
      for (n = ARRAYLEN(keys); n-- > 0; keys++)
         if (isRegister)
            RegisterHotKey(mainHWnd, *keys, MOD_WIN, *keys);
         else
            UnregisterHotKey(mainHWnd, *keys);
      #endif
   }
}

static void vmShowKeyCodes(bool show)
{
   int32 i;
   Int32Array ia = newPtrArrayOf(Int32, 255,null);
   if (ia == null)
      return;
   for (i = 1; i <= 0xFF; i++)
      ia[i-1] = i;
   registerHotkeys(ia, show);
   freeArray(ia);

   if (!show && interceptedSpecialKeys != null) // recover old registered keys
      registerHotkeys(interceptedSpecialKeys, true);
}

static void vmInterceptSpecialKeys(int32* keys, int32 len)
{
   if (interceptedSpecialKeys != null)
   {
      registerHotkeys(interceptedSpecialKeys, false);
      freeArray(interceptedSpecialKeys);
   }
   if (len == 0)
      interceptedSpecialKeys = null;
   else
   {
      int32 *dk;
      dk = interceptedSpecialKeys = newPtrArrayOf(Int32, len, null);
      if (interceptedSpecialKeys != null)
      {
         // map the TotalCross keys into the device-specific keys
         for (; len-- > 0; keys++, dk++)
            *dk = keyPortable2Device(*keys);
         registerHotkeys(interceptedSpecialKeys, true);
      }
   }
}
//////////// END OF KEY INTERCEPTION FUNCTIONS

#ifdef WINCE
 #define CF_TCHARTEXT               CF_UNICODETEXT
 #define ClipboardAlloc(len)        LocalAlloc(LPTR, sizeof(TCHAR)*(len + 1))
 #define ClipboardFree(clipData)    LocalFree(clipData)
 #define ClipboardLock(clipData)    (TCHARP) clipData
 #define ClipboardUnlock(clipData)  clipData = clipData
#elif !defined WP8
 #define CF_TCHARTEXT               CF_TEXT
 #define ClipboardAlloc(len)        GlobalAlloc(GPTR, sizeof(TCHAR)*(len + 1))
 #define ClipboardFree(clipData)    GlobalFree(clipData)
 #define ClipboardLock(clipData)    GlobalLock(clipData)
 #define ClipboardUnlock(clipData)  GlobalUnlock(clipData)
#else
#define CF_TCHARTEXT               CF_TEXT
#define ClipboardAlloc(len)        GlobalAlloc(GPTR, sizeof(TCHAR)*(len + 1))
#define ClipboardFree(clipData)    GlobalFree(clipData)
#define ClipboardLock(clipData)    GlobalLock(clipData)
#define ClipboardUnlock(clipData)  GlobalUnlock(clipData)
#endif

static void vmClipboardCopy(CharP string, int32 stringLen)
{
	//XXX
#if !defined WP8
   HANDLE hClipData;
   TCHARP lpClipData;

   if (stringLen == 0) // Empty string.
      return;

   if (!(hClipData = ClipboardAlloc(stringLen)))
      return;

   if (!(lpClipData = ClipboardLock(hClipData)))
   {
      ClipboardFree(hClipData);
      return;
   }

   CharP2TCHARPBuf(string, lpClipData);
   ClipboardUnlock (hClipData);

   if (!OpenClipboard(mainHWnd))
   {
      ClipboardFree(hClipData);
      return;
   }

   if (!EmptyClipboard())
   {
      ClipboardFree(hClipData);
      return;
   }

   if (!SetClipboardData(CF_TCHARTEXT, lpClipData))
      ClipboardFree(hClipData);
   CloseClipboard();
#endif
}

static TCObject vmClipboardPaste(Context currentContext)
{
	//XXX
#if !defined WP8
   HANDLE hClipData;
   TCHARP lpClipData;
   TCObject o;

   if (!OpenClipboard(mainHWnd))
      o = createStringObjectFromCharP(currentContext, "", 0);
   else
   if ((hClipData = GetClipboardData(CF_TCHARTEXT)) == null || (lpClipData = ClipboardLock(hClipData)) == null)
   {
      CloseClipboard();
      o = createStringObjectFromCharP(currentContext, "", 0);
   }
   else
   {
      o = createStringObjectFromTCHAR(currentContext, lpClipData, tcslen(lpClipData));
      ClipboardUnlock(hClipData);
      CloseClipboard();
   }
   return o;
#else
	return null;
#endif
}

static bool vmIsKeyDown(int32 key)
{
#if !defined(WP8)
   key = keyPortable2Device(key);
   return (GetAsyncKeyState(key) & 0x8000) != 0;
#else
   return false;
#endif
}

static int32 vmGetRemainingBattery()
{
#ifdef WINCE
   SYSTEM_POWER_STATUS_EX p;
   int32 ret=100;

   if (GetSystemPowerStatusEx(&p, true))
   {
      if (p.BatteryLifePercent <= 100)
         ret = p.BatteryLifePercent;
      if (p.BackupBatteryLifePercent < 100) // even if not present, it returns 100, so we will only consider values less than 100
      {
         if (p.BatteryLifePercent == 0xFF) // sometimes, right after the device is undocked, the main battery is at an unknown state
            ret = 0;
         ret += p.BackupBatteryLifePercent;
      }
   }
   return (ret > 100 ? 100 : ret);
#elif !defined WP8 // guich@tc115_31
   SYSTEM_POWER_STATUS p;
   int32 ret=100;

   if (GetSystemPowerStatus(&p))
   {
      if (p.BatteryLifePercent <= 100)
         ret = p.BatteryLifePercent;
   }
   return (ret > 100 ? 100 : ret);
#else
   return getRemainingBatery();
#endif
}

/* The following structures are used to access the power state */
// GDI Escapes for ExtEscape()
#define QUERYESCSUPPORT    8
 
// The following are unique to CE
#define GETVFRAMEPHYSICAL   6144
#define GETVFRAMELEN    6145
#define DBGDRIVERSTAT    6146
#define SETPOWERMANAGEMENT   6147
#define GETPOWERMANAGEMENT   6148
 
/* These values must conform with those in ScreenPower.java */

#include "win/aygshellLib.h"


typedef enum _VIDEO_POWER_STATE 
{
    VideoPowerOn = 1,
    VideoPowerStandBy,
    VideoPowerSuspend,
    VideoPowerOff
} VIDEO_POWER_STATE, *PVIDEO_POWER_STATE;
 
typedef struct _VIDEO_POWER_MANAGEMENT 
{
    ULONG Length;
    ULONG DPMSVersion;
    ULONG PowerState;
} VIDEO_POWER_MANAGEMENT, *PVIDEO_POWER_MANAGEMENT;

static bool vmTurnScreenOn(bool on)
{
#if defined WP8
	return true;
#else
	HDC gdc;
	int iESC;
	bool ret;

	// implementation
	iESC=SETPOWERMANAGEMENT;

	gdc = GetDC(NULL);
	if (ExtEscape(gdc, QUERYESCSUPPORT, sizeof(int), (LPCSTR)&iESC, 0, NULL)==0)		
      ret = false;
	else
	{
		VIDEO_POWER_MANAGEMENT vpm;
		vpm.Length = sizeof(VIDEO_POWER_MANAGEMENT);
		vpm.DPMSVersion = 0x0001;
      vpm.PowerState = on ? VideoPowerOn : VideoPowerOff;
		// Change the power state of the display
		ExtEscape(gdc, SETPOWERMANAGEMENT, vpm.Length, (LPCSTR) &vpm, 0, NULL);
		ExtEscape(gdc, GETPOWERMANAGEMENT, 0, NULL, vpm.Length, (LPSTR) &vpm); 
      ret = on ? (vpm.PowerState == VideoPowerOn) : (vpm.PowerState == VideoPowerOff);
		ReleaseDC(NULL, gdc);
	}	
	return ret;
#endif
}

////////////////////// guich@tc122_52: added VIBRATION

#ifdef WINCE
// for Windows Mobile
const int NLED_COUNT_INFO_ID = 0;
const int NLED_SETTINGS_INFO_ID = 2;

struct NLED_SETTINGS_INFO 
{
  UINT LedNum;
  INT OffOnBlink;
  LONG TotalCycleTime;
  LONG OnTime;
  LONG OffTime;
  INT MetaCycleOn;
  INT MetaCycleOff; 
};

typedef BOOL (__stdcall *NLedSetDeviceProc)(int nID, void* pOutput);
typedef BOOL (__stdcall *NLedGetDeviceInfoProc)(int nInfoId,  void* pOutput);
NLedSetDeviceProc NLedSetDevice;
NLedGetDeviceInfoProc NLedGetDeviceInfo;

static int vibtype = VIB_NONE;
static int vibIndex;
static HANDLE vibThread;

LRESULT VibrateThread(int32 *ms_)
{
   int32 ms = (int32)ms_;
   switch (vibtype)
   {
      case VIB_AYG:
      {
    	  if (_Vibrate != null && _VibrateStop != null) {
	         _Vibrate(0,NULL,TRUE,INFINITE);
	         Sleep(ms);
	         _VibrateStop();
    	  }
         break;
      }
      case VIB_CORE:
      {
         struct NLED_SETTINGS_INFO settings;
         settings.LedNum= vibIndex;
         settings.OffOnBlink= 1;
         NLedSetDevice(NLED_SETTINGS_INFO_ID, &settings);
         Sleep(ms);
         settings.OffOnBlink= 0;
         NLedSetDevice(NLED_SETTINGS_INFO_ID, &settings);
         break;
      }
   }
   vibThread = null;
   return 0;
}
#endif // WINCE

void vmVibrate(int32 ms)
{
#ifdef WINCE
   if (vibtype == VIB_NONE)
   {
      if (coreDll)
      {
         NLedSetDevice = (NLedSetDeviceProc) GetProcAddress(coreDll, TEXT("NLedSetDevice"));
         NLedGetDeviceInfo = (NLedGetDeviceInfoProc) GetProcAddress(coreDll, TEXT("NLedGetDeviceInfo"));
         if (NLedSetDevice && NLedGetDeviceInfo)
         {
            vibtype = VIB_CORE;
            NLedGetDeviceInfo(NLED_COUNT_INFO_ID, &vibIndex);
            vibIndex--;
         }
      }
      if (vibtype == VIB_NONE && _Vibrate && _VibrateStop)
      {
        vibtype = VIB_AYG;
      }
      if (vibtype == VIB_NONE)
         vibtype = VIB_NOTAVAILABLE;
   }
   if (vibThread == null && (vibtype == VIB_AYG || vibtype == VIB_CORE))
      vibThread = CreateThread(null, 0, (LPTHREAD_START_ROUTINE) VibrateThread, (int*)ms, 0, null);

#elif defined WP8
   vibrate(ms);
#endif
}
