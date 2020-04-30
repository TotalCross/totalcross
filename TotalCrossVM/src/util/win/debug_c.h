// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

// Debug

static FILE* fdebug;
static char debugPath[MAX_PATHNAME];

static bool privateInitDebug()
{
   return true;
}

void closeDebug()
{
   fclose(fdebug);
   fdebug = null;
}

static void privateDestroyDebug()
{
   if (fdebug)
   {
      fputs("===============\r\n",fdebug);
      closeDebug();
   }
}

static bool privateDebug(char* str)
{
#if defined(ENABLE_CONSOLE) && defined(WIN32) && !defined(WINCE) && !defined(WP8)
   if (!consoleAllocated)
   {
      consoleAllocated = true;
      AllocConsole();
      freopen("conin$","r",stdin);
      freopen("conout$","w",stdout);
      freopen("conout$","w",stderr);
   }
   printf(str);
   printf("\n");
   fflush(stdout);
   return true;
#else
   bool err = true;
   if (!fdebug)
   {
      xstrprintf(debugPath, "%s\\DebugConsole.txt",appPath);
      fdebug = fopen(debugPath,"ab+");
   }
   if (fdebug)
   {
      if (strEq(str,ERASE_DEBUG_STR))
      {
         TCHAR debugPath2[MAX_PATHNAME];
         CharP2TCHARPBuf(debugPath, debugPath2);
         closeDebug();
         DeleteFile(debugPath2);
      }
      else
      {
         fprintf(fdebug, "%s\r\n", str);
         fflush(fdebug);
      }
   }
#if defined(WINCE) && defined(_DEBUG)
{
   TCHARP tstr = CharP2TCHARP(str);
   if (tstr) OutputDebugStringW(tstr);
   xfree(tstr);
   OutputDebugStringW(TEXT("\n"));
}
#elif (defined(WP8) && defined(DEBUG)) || (defined(WIN32) && defined(_DEBUG))
   OutputDebugStringA(str);
   OutputDebugStringA("\n");
#endif
   return err;
#endif
}

// Alert

#ifdef WINCE

static BOOL CALLBACK SearchWindowProc(HWND hwnd, LPARAM lParam)
{
   TCHAR title[256];
   bool* stop = (bool*)lParam;
   GetWindowText(hwnd, title, 255);
   if (lstrcmpi(title,L"ALERT") == 0)
   {
      GetWindowText(GetForegroundWindow(), title, 255);
      if (lstrcmpi(title,L"ALERT") != 0) // if there's an alert and its not the active window, close it
      {
         Sleep(2000); // give a time before closing
         DestroyWindow(hwnd);
         *stop = true;
      }
      return false;
   }
   return true; // continue enumeration
}

DWORD WINAPI CheckMessageBox(VoidP argP) // guich@tc114_88
{
   bool stop = false;
   while (!stop)
   {
      Sleep(500);
      EnumWindows(SearchWindowProc, (long)&stop);
   }
   return 0;
}
#endif

static void privateAlert(CharP str)
{
#ifdef WINCE
   HANDLE h = null;
   int32 id;
   JChar buf[2048]; // JCharP = TCHAR*
   CharP2JCharPBuf(str, min32(xstrlen(str),sizeof(buf)-1), buf, true);
   h = CreateThread(NULL, 0, CheckMessageBox, null, 0, &id);
   MessageBox(mainHWnd,buf,TEXT("ALERT"),MB_OK|MB_ICONEXCLAMATION|MB_TOPMOST|MB_SETFOREGROUND);
   TerminateThread(h, 0);
   CloseHandle(h);
   SetForegroundWindow(mainHWnd);
   SetActiveWindow(mainHWnd);
#elif !defined(WP8) 
   MessageBox(mainHWnd,str,TEXT("ALERT"),MB_OK|MB_TOPMOST|MB_SETFOREGROUND);
#else
   JChar buf[2048]; // JCharP = TCHARP
   CharP2JCharPBuf(str, min32(xstrlen(str), sizeof(buf)-1), buf, true);
   alertCPP(buf);
#endif
}
