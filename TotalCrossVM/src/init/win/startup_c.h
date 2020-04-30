// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

TC_API DWORD TSV_Close(DWORD dwData) {return 0;}
TC_API DWORD TSV_Deinit(DWORD dwData) {return 0;}
TC_API DWORD TSV_IOControl(DWORD dwData, DWORD dwCode, PBYTE pBufIn, DWORD dwLenIn, PBYTE pBufOut, DWORD dwLenOut, PDWORD pdwActualOut) {return 1;}
TC_API DWORD TSV_Open(DWORD dwData, DWORD dwAccess, DWORD dwShareMode) {return 0;}
TC_API DWORD TSV_Read(DWORD dwData, LPVOID pBuf, DWORD dwLen) {return 0;}
TC_API DWORD TSV_Seek(DWORD dwData, long pos, DWORD type) {return 0;}
TC_API DWORD TSV_Write(DWORD dwData, LPCVOID pInBuf, DWORD dwInLen) {return 0;}

unsigned long __cdecl StartVMFromService(void* nnn) 
{
	// WP8 app should not use the registry
#if !defined WP8
   // get the tcz name from the registry
   HKEY handle=(HKEY)0;
   DWORD err,size;
   TCHAR buf[64];
   char name[64];
   int32 ret;

   err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("\\Services\\TotalCrossSrv"), 0, KEY_ALL_ACCESS, &handle);
   size = sizeof(buf);
   buf[0] = 0;
   RegQueryValueEx(handle,TEXT("TCZ"),null,null,(uint8*)buf,&size);
   RegCloseKey(handle);
   TCHARP2CharPBuf(buf,name);

   ret = executeProgram(name);
   if (ret != 0)
   {
      wsprintf(buf,TEXT("%d"),ret);
      MessageBox(0,buf,TEXT("Service Exit Code"),MB_OK);
   }
#endif
   return 0;
}

TC_API DWORD TSV_Init(DWORD dwData)
{
#ifdef WINCE
	HANDLE hThread = CreateThread( 0, 0, StartVMFromService, 0, 0, 0);
#endif
	return 1;
}
///////////////////////////////

static void getWorkingDir()
{
	char* sl;

#ifndef WP8
   TCHAR d1[MAX_PATH], d2[MAX_PATH];
   // get the path to the vm
   GetModuleFileName(hModuleTCVM, d1, MAX_PATH); // note: passing 0 here returns the path to launcher.exe, not this dll
   TCHARP2CharPBuf(d1, vmPath);
   // get the path to the exe
   GetModuleFileName(GetModuleHandle(null), d2, MAX_PATH); // note: passing 0 here returns the path to launcher.exe, not this dll
   TCHARP2CharPBuf(d2, appPath);


   sl = xstrrchr(vmPath, '\\'); // strip the file name from the path
   if (!sl) sl = vmPath;
   *sl = 0;
   for (sl = vmPath; *sl != 0; sl++) // replace backslashes for slashes
      if (*sl == '\\') *sl = '/';

   sl = xstrrchr(appPath, '\\'); // strip the file name from the path
   if (!sl) sl = appPath;
   *sl = 0;
   for (sl = appPath; *sl != 0; sl++) // replace backslashes by slashes
      if (*sl == '\\') *sl = '/';

   // store the exe name
   GetModuleFileName(GetModuleHandle(null), exeName, MAX_PATHNAME);
#else
   char *_path;
   _path = GetVmPathWP8();
   for (sl = _path; *sl != 0; sl++) // replace backslashes by slashes
      if (*sl == '\\') *sl = '/';
   xstrcpy(vmPath, _path);

   _path = GetAppPathWP8();
   for (sl = _path; *sl != 0; sl++) // replace backslashes by slashes
	   if (*sl == '\\') *sl = '/';
   xstrcpy(appPath, _path);
#endif
}

#if defined(ENABLE_TEST_SUITE) && defined(WINCE)
static void waitUntilStarted() // waits until the window is shown in windows ce so that the graphics test can run correctly
{
   MSG msg;
   int i, ret;

   for (i= *tcSettings.isFullScreenPtr ? 2 : 1; i != 0;) // the first repaint is still with the taskbar; the 2nd repaint already removed the taskbar
   {
      ret = GetMessage(&msg, mainHWnd, 0, 0);
      if (ret != 0 && ret != -1)
      {
         TranslateMessage(&msg);
         DispatchMessage(&msg);
      }
      if (msg.message == WM_PAINT)
      {
         i--;
         PostMessage(mainHWnd, WM_PAINT, 0, 0);
      }
   }
}
#else
#define waitUntilStarted()
#endif

typedef struct
{
   HWND hWnd;
} TWindowBeingSearched, *WindowBeingSearched;

static BOOL CALLBACK SearchWindowProc(HWND hwnd, LPARAM lParam)
{
   TCHAR wclass[256];
   WindowBeingSearched wbs = (WindowBeingSearched)lParam;
   wclass[0] = 0;
   GetClassName(hwnd, wclass, 255);
   if (lstrcmpi(wclass,exeName) == 0)
   {
      wbs->hWnd = hwnd;
      return false;
   }
   return true; // continue enumeration
}

static bool checkIfRunning()
{
   TWindowBeingSearched wbs;

   tzero(wbs);
   EnumWindows(SearchWindowProc, (long)&wbs); // both window text and class must be tested, otherwise an Explorer window browsing the Painter folder will be incorrectly recognized as a Painter application
   if (wbs.hWnd != null)
      SetForegroundWindow(wbs.hWnd);
   return wbs.hWnd != null;
}

bool wokeUp()
{
#if defined(WINCE) && _WIN32_WCE >= 300
   static int last;
   int cur;
   DWORD v=0,res;
   HKEY h;
   cur = getTimeStamp();
   if ((cur-last) < 250) // don't poll too much
      return 0;
   last = cur;
   if (RegCreateKeyEx(HKEY_CURRENT_USER,TEXT("Software\\TotalCross"),0,NULL,0,KEY_ALL_ACCESS,NULL,&h,&res) == 0)
   {
      res = 4;
      if (RegQueryValueEx(h, TEXT("PowerOn"), 0, NULL, (uint8*)&v, &res) == 0 && v == 1)
      {
         res = 0;
         RegSetValueEx(h,TEXT("PowerOn"),0,REG_DWORD,(byte*)&res,4); // reset to 0
      }
      RegCloseKey(h);
      return v == 1;
   }
#endif
   return 0;
}

static void registerWake(bool reg)
{
#if defined(WINCE) && _WIN32_WCE >= 300
   TCHAR szExeName[MAX_PATH];
   GetModuleFileName(GetModuleHandle(0), szExeName, MAX_PATH);
   CeRunAppAtEvent(szExeName, reg ? NOTIFICATION_EVENT_WAKEUP : NOTIFICATION_EVENT_NONE);
#endif
}

static bool isWakeUpCall(CharP args)
{
#if defined(WINCE) && _WIN32_WCE >= 300
   if (xstrstr(args, "AppRunAfterWakeup") != 0)
   {
      HKEY h=0;
      DWORD res=0;
      if (RegCreateKeyEx(HKEY_CURRENT_USER,TEXT("Software\\TotalCross"),0,NULL,0,KEY_ALL_ACCESS,NULL,&h,&res) == 0)
      {
         DWORD v = 1;
         RegSetValueEx(h,TEXT("PowerOn"),0,REG_DWORD,(byte*)&v,4); // set to 1
         RegCloseKey(h);
      } //else {TCHAR buf[100]; xstrprintf(buf,"%d",(int)GetLastError()); MessageBox(0, buf,L"Erro na abertura",MB_OK);}
      return true;
   }
#endif
   return false;
}

#if !defined(WINCE)
int32 defScrX=-1,defScrY=-1,defScrW=-1,defScrH=-1;
static CharP parseScreenBounds(CharP cmd, int32 *xx, int32 *yy, int32 *ww, int32 *hh)
{
   CharP scr = xstrstr(cmd, "/scr"),s2;
   int n;
   if (!scr) return null;
   n = sscanf(scr + 5,"%d,%d,%d,%d", xx, yy, ww, hh);
   if (n != 4)
   {
      alert("Format: <other arguments> /scr x,y,width,height\nPass -1 to use the default and -2 to center on screen.\nEx: \"/scr -2,100,320,-1\"\nwill open a window horizontally centered at\ny=100, w=320, h=320 (default is 0,0,240,320).");
      return null;
   }
   *scr = 0; // cut at the scr
   while (scr > cmd && *(scr-1) == ' ') // trim
      *(scr-1) = 0;
   s2 = xstrstr(cmd, " /cmd"); // if only /cmd remained at end, remove it.
   if (s2 == scr-6)
      *s2 = 0;
   return cmd;
}
#endif

void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int32 hRes, int32 vRes, bool nothingChanged); // GraphicsPrimitives_c.h

void appSetFullScreen();

static void setFullScreen()
{
#ifndef WP8
   int32 width = GetSystemMetrics(SM_CXSCREEN);
   int32 height = GetSystemMetrics(SM_CYSCREEN);
#if !defined (WINCE) //flsobral@tc114_60: fixed fullscreen display on win32.
   int32 style = GetWindowLong(mainHWnd, GWL_STYLE);

#ifdef DESIRED_SCREEN_WIDTH          // tweak to work in IBGE's NETBOOK
   width = DESIRED_SCREEN_WIDTH;
#endif
#ifdef DESIRED_SCREEN_HEIGHT
   height = DESIRED_SCREEN_HEIGHT;
#endif

   style &= ~(WS_BORDER | WS_CAPTION | WS_THICKFRAME);
   SetWindowLong(mainHWnd, GWL_STYLE, style);
   SetWindowPos(mainHWnd, NULL, 0, 0, width, height, SWP_NOZORDER | SWP_NOACTIVATE | SWP_FRAMECHANGED);
#else
   MoveWindow(mainHWnd, 0, 0, width, height, TRUE);
#endif
   // now make sure task bar/start icon/sip are lower down the z-order so they seem to be removed
   SetForegroundWindow(mainHWnd);
   screen.screenY = screen.screenX = 0;
   screenChange(mainContext, width, height, screen.hRes, screen.vRes, false);
#else
   appSetFullScreen();
#endif
}
