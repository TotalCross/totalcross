// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#if !defined(WINCE)
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

   SetLocalTime(&newTime);
   SetLocalTime(&newTime);
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
#else
   ExitWindowsEx(EWX_REBOOT,0);
#endif
}


typedef HANDLE (__stdcall *RegisterServiceProc)(LPCWSTR lpszType,  DWORD dwIndex,  LPCWSTR lpszLib,  DWORD dwInfo);
typedef BOOL (__stdcall *DeregisterServiceProc)(HANDLE hDevice);
typedef HANDLE (__stdcall *GetServiceHandleProc)(LPWSTR szPrefix, LPWSTR szDllName, DWORD pdwDllBuf);

static int32 vmExec(TCHARP szCommand, TCHARP szArgs, int32 launchCode, bool wait)
{
   VoidP startInfo = null;
   int32 ret=-1;

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

   return ret;
}

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
#endif
}

//////////// START OF KEY INTERCEPTION FUNCTIONS
//XXX: O que s�o hot keys? N�o fa�o a menor id�ia do que fazer aqui e acho que nem faz sentido
static int32 vmPortableKeyToWin32(PortableSpecialKeys key)
{
   switch (key)
   {
      case SK_PAGE_UP       : return VK_PRIOR;
      case SK_PAGE_DOWN     : return VK_NEXT;
      case SK_HOME          : return VK_HOME;
      case SK_END           : return VK_END;
      case SK_UP            : return VK_UP;
      case SK_DOWN          : return VK_DOWN;
      case SK_LEFT          : return VK_LEFT;
      case SK_RIGHT         : return VK_RIGHT;
      case SK_INSERT        : return VK_INSERT;
      case SK_ENTER         : return VK_RETURN;
      case SK_TAB           : return VK_TAB;
      case SK_BACKSPACE     : return VK_BACK;
      case SK_ESCAPE        : return VK_ESCAPE;
      case SK_DELETE        : return VK_DELETE;
      case SK_MENU          : return VK_F6;
      case SK_KEYBOARD_ABC  : return VK_F11;
      case SK_HARD1         : return VK_F1;
      case SK_HARD2         : return VK_F2;
      case SK_HARD3         : return VK_F3;
      case SK_HARD4         : return VK_F4;
      case SK_CALC          : return VK_F7;
      case SK_FIND          : return VK_F8;
      case SK_ACTION        : return VK_F12;
      case SK_SCREEN_CHANGE : return VK_F9;
      case SK_F1            : return VK_F1;
      case SK_F2            : return VK_F2;
      case SK_F3            : return VK_F3;
      case SK_F4            : return VK_F4;
      case SK_F5            : return VK_F5;
      case SK_F6            : return VK_F6;
      case SK_F7            : return VK_F7;
      case SK_F8            : return VK_F8;
      case SK_F9            : return VK_F9;
      case SK_F10           : return VK_F10;
      case SK_F11           : return VK_F11;
      case SK_F12           : return VK_F12;
      case SK_F13           : return VK_F13;
      case SK_F14           : return VK_F14;
      case SK_F15           : return VK_F15;
      case SK_F16           : return VK_F16;
      case SK_F17           : return VK_F17;
      case SK_F18           : return VK_F18;
      case SK_F19           : return VK_F19;
      case SK_F20           : return VK_F20;
      case SK_F21           : return VK_F21;
      case SK_F22           : return VK_F22;
      case SK_F23           : return VK_F23;
      case SK_F24           : return VK_F24;
      default:
         break;
   }
   return key < 0 ? -key : key;
}

PortableSpecialKeys vmWin32KeyToPortable(int32 key)
{
   switch (key)
   {
      case VK_PRIOR : return SK_PAGE_UP;
      case VK_NEXT  : return SK_PAGE_DOWN;
      case VK_HOME  : return SK_HOME;
      case VK_END   : return SK_END;
      case VK_UP    : return SK_UP;
      case VK_DOWN  : return SK_DOWN;
      case VK_LEFT  : return SK_LEFT;
      case VK_RIGHT : return SK_RIGHT;
      case VK_INSERT: return SK_INSERT;
      case VK_RETURN: return SK_ENTER;
      case VK_TAB   : return SK_TAB;
      case VK_BACK  : return SK_BACKSPACE;
      case VK_ESCAPE: return SK_ESCAPE;
      case VK_DELETE: return SK_DELETE;
      case VK_F1    : return SK_HARD1;
      case VK_F2    : return SK_HARD2;
      case VK_F3    : return SK_HARD3;
      case VK_F4    : return SK_HARD4;
      case VK_F5    : return SK_F5;
      case VK_F6    : return SK_MENU;
      case VK_F7    : return SK_CALC;
      case VK_F8    : return SK_FIND;
      case VK_F9    : return SK_SCREEN_CHANGE;
      case VK_F10   : return SK_HOME;
      case VK_F11   : return SK_KEYBOARD_ABC;
      case VK_F12   : return SK_ACTION;
      case VK_F13   : return SK_F13;
      case VK_F14   : return SK_F14;
      case VK_F15   : return SK_F15;
      case VK_F16   : return SK_F16;
      case VK_F17   : return SK_F17;
      case VK_F18   : return SK_F18;
      case VK_F19   : return SK_F19;
      case VK_F20   : return SK_F20;
      case VK_F21   : return SK_F21;
      case VK_F22   : return SK_F22;
      case VK_F23   : return SK_F23;
      case VK_F24   : return SK_F24;
      default:
         break;
   }
   return (PortableSpecialKeys)key;
}

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
      #else
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
            *dk = vmPortableKeyToWin32(*keys);
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
#else
 #define CF_TCHARTEXT               CF_TEXT
 #define ClipboardAlloc(len)        GlobalAlloc(GPTR, sizeof(TCHAR)*(len + 1))
 #define ClipboardFree(clipData)    GlobalFree(clipData)
 #define ClipboardLock(clipData)    GlobalLock(clipData)
 #define ClipboardUnlock(clipData)  GlobalUnlock(clipData)
#endif

static void vmClipboardCopy(CharP string, int32 stringLen)
{
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
}

static TCObject vmClipboardPaste(Context currentContext)
{
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
}

static bool vmIsKeyDown(int32 key)
{
   key = vmPortableKeyToWin32(key);
   return (GetAsyncKeyState(key) & 0x8000) != 0;
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
#else
   SYSTEM_POWER_STATUS p;
   int32 ret=100;

   if (GetSystemPowerStatus(&p))
   {
      if (p.BatteryLifePercent <= 100)
         ret = p.BatteryLifePercent;
   }
   return (ret > 100 ? 100 : ret);
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
#endif
}
