// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#if _WIN32_WCE >= 300
 #include <Aygshell.h>
 #include "win/aygshellLib.h"
#endif


void markWholeScreenDirty(Context currentContext);
void screenChange(Context currentContext, int32 newWidth, int32 newHeight, int hRes, int vRes, bool nothingChanged);
void getScreenSize(int32 *w, int32* h); // ui/win/gfx_Graphics_c.h

BOOL APIENTRY DllMain(HANDLE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved)
{
   switch (ul_reason_for_call)
	{
		case DLL_PROCESS_ATTACH:
			hModuleTCVM = hModule;
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
		case DLL_PROCESS_DETACH:
			break;
   }
   return TRUE;
}

static void hideWinCEStuff()
{
#ifdef __AYGSHELL_H__
   if (*tcSettings.isFullScreenPtr)
   {
      // now make sure task bar/start icon/sip are lower down the z-order so they seem to be removed
      SetForegroundWindow(mainHWnd);
      if (_SHFullScreen != null) {
    	  _SHFullScreen(mainHWnd, SHFS_HIDESTARTICON | SHFS_HIDETASKBAR);
      }
      if (!isWindowsMobile)
      {
         RECT rect;
         int32 width,height;
         HWND hWndTaskBar = FindWindow(TEXT("HHTaskBar"), TEXT(""));
         ShowWindow(hWndTaskBar, SW_HIDE);
         getScreenSize(&width, &height);
         rect.top = 0;
         rect.left = 0;
         rect.bottom = height;
         rect.right = width;
         SystemParametersInfo(SPI_SETWORKAREA, 0, &rect, SPIF_SENDCHANGE);
      }
   }
   switch (*tcSettings.closeButtonTypePtr)
   {
      case CLOSE_BUTTON:
    	  if (_SHDoneButton != null) {
    		  _SHDoneButton(mainHWnd, SHDB_SHOW);
    	  }
         break;
      case NO_BUTTON: // guich@tc111_3
      {
      	DWORD dwStyle = GetWindowLong(mainHWnd, GWL_STYLE);
      	if ((dwStyle & WS_MINIMIZEBOX) == 0)
         {
      		SetWindowLong(mainHWnd, GWL_STYLE, dwStyle | WS_MINIMIZEBOX);
      		if (_SHDoneButton != null) {
      			_SHDoneButton(mainHWnd, SHDB_SHOW); // force caption bar redraw
            	_SHDoneButton(mainHWnd, SHDB_HIDE);
      		}
         }
         break;
      }
      default: break;
   }
   if (_SHFullScreen != null) {
	   _SHFullScreen(mainHWnd, SHFS_HIDESIPBUTTON);
   }
#endif
}

static bool keysMatch(int32 tcK, int32 sysK) // verifies if the given user key matches the system key
{
   // map the TotalCross keys into the device-specific keys
   // Note that more than one device key may be mapped to a single tc key
   int32 k = keyPortable2Device(sysK);
   return k == tcK;
}

extern bool showKeyCodes;
static int32 lastW, lastH,lastPenX=-1,lastPenY=-1;
static uint8 keyIsDown[256];
static int32 actionStart;
static bool dontPostOnChar;
static bool minimized;

// Defined in Imm.h
#define IME_ESC_PRIVATE_FIRST              0x0800
// Defined in Tpcshell.h
#define IME_ESC_SET_MODE                   (IME_ESC_PRIVATE_FIRST)
#define IME_ESC_GET_MODE                   (IME_ESC_PRIVATE_FIRST + 1)
#define IME_ESC_SEND_BACK_TO_FOCUS_WINDOW  (IME_ESC_PRIVATE_FIRST + 2)
#define IME_ESC_SET_CUSTOM_SYMBOLS         (IME_ESC_PRIVATE_FIRST + 3)
#define IME_ESC_RETAIN_MODE_ICON           (IME_ESC_PRIVATE_FIRST + 4)
#define IME_ESC_SAVE_USER_WORDS            (IME_ESC_PRIVATE_FIRST + 5)
#define IME_ESC_CLEAR_ON_BACK_PRESS_HOLD   (IME_ESC_PRIVATE_FIRST + 6)
#define IME_ESC_SKIP_AMBIG_MODE            (IME_ESC_PRIVATE_FIRST + 7)
// Defined in WinuserM.h
// wParam of  WM_IME_REQUEST. Extension for Mobile.
#define IMR_ISIMEAWARE                     0x1000  // Is window IME aware?
// Return values of IMR_ISIMEAWARE
#define IMEAF_AWARE                        0x00000001 // Window is IME aware
// IME Input Modes
#define IM_SPELL                           0

void adjustWindowSizeWithBorders(int32 resizableWindow, int32* w, int32* h);
void applyPalette();

#if defined (WP8)
static long FAR PASCAL handleWin32Event(HWND hWnd, UINT msg, WPARAM wParam, LONG lParam)
{
	return 0L;
}
#else
static long FAR PASCAL handleWin32Event(HWND hWnd, UINT msg, WPARAM wParam, LONG lParam)
{
   bool isHotKey = false;
   int32 key, scan;
   WinEvent we;
#ifdef WINCE
   HIMC hC;
#endif

   we.hWnd = hWnd;
   we.msg = msg;
   we.wParam = wParam;
   we.lParam = lParam;
   we.currentContext = mainContext;
   if (handleEvent(&we)) // let the attached native libs handle this event
      return 0L;

   //debug("msg: %X (%d), wParam: %d, lParam: %X", (int)msg, (int)msg, (int)wParam, (int)lParam);
   switch(msg)
   {
#if !defined WINCE && !defined WP8
      case WM_GETMINMAXINFO:
         if (screen.pixels && *tcSettings.resizableWindow)
         {
            MINMAXINFO* mmi = (MINMAXINFO*)lParam;
            int32 w=0,h=0;
            bool landscape = screen.screenW > screen.screenH;
            adjustWindowSizeWithBorders(*tcSettings.resizableWindow, &w, &h);
            mmi->ptMinTrackSize.x = max32(landscape ? 320 : 240,screen.minScreenW/2)+w;
            mmi->ptMinTrackSize.y = max32(landscape ? 240 : 320,screen.minScreenH/2)+h;
            mmi->ptMaxTrackSize.x = GetSystemMetrics(SM_CXFULLSCREEN);
            mmi->ptMaxTrackSize.y = GetSystemMetrics(SM_CYFULLSCREEN);
         }
         break;
      case WM_SIZE:
      {
         if (screen.pixels && *tcSettings.resizableWindow)
         {
            int32 w = lParam & 0xFFFF,h = lParam >> 16;
            if (w != 0 && h != 0 && (lastW != w || lastH != h))
               screenChange(mainContext, lastW = w, lastH = h, screen.hRes, screen.vRes, false);
         }
         break;
      }
#endif
      case WM_ACTIVATE:
      {
         applyPalette();
#if defined (WIN32)
         if (HIWORD(wParam)) // HIWORD(wParam) == 0 means the app is not minimized
         {
            if (!minimized)
               postOnMinimizeOrRestore(minimized = true);
         }
         else if (minimized)
            postOnMinimizeOrRestore(minimized = false);
#endif
#if defined (WINCE)
         if (wParam == 0)
            restoreVKSettings();
#endif
         hideWinCEStuff();
         break;
      }
#if defined (WINCE)
      case WM_WINDOWPOSCHANGED:
      {
         if (((PWINDOWPOS)lParam)->hwndInsertAfter) // hwndInsertAfter == 0 means the app window is at the top
         {
            if (!minimized)
               postOnMinimizeOrRestore(minimized = true);
         }
         else if (minimized)
            postOnMinimizeOrRestore(minimized = false);
         break;
      }
#endif
      case WM_SETTINGCHANGE:
      {
         if (!bSipUp) //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
         {
            HWND hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_BOTTOM, 0,0,0,0, SWP_NOMOVE | SWP_NOSIZE | SWP_HIDEWINDOW); // sets z-order to top but doesn't reposition or resize.
         }
         break;
      }
      case WM_KILLFOCUS:
      {
#if defined (WINCE)
         if (*tcSettings.romVersionPtr < 400)
            break;
			hC = ImmGetContext(hWnd);
			if ((SendMessage((HWND) wParam, WM_IME_REQUEST, IMR_ISIMEAWARE, 0) & IMEAF_AWARE) == IMEAF_AWARE)
				ImmEscape(null, hC, IME_ESC_RETAIN_MODE_ICON, (LPVOID) true);
			ImmSetOpenStatus(hC, false);
#endif
         break;
      }
      case WM_SETFOCUS:
      {
#if defined (WINCE)
         if (*tcSettings.romVersionPtr < 400)
            break;
         hC = ImmGetContext(hWnd);
         ImmSetOpenStatus(hC, true);
         ImmEscape(null, hC, IME_ESC_SET_MODE, (LPVOID) IM_SPELL);
#endif
         break;
      }
#if defined (WINCE)
      case WM_IME_REQUEST:
      {
         if (wParam == IMR_ISIMEAWARE)
            return IMEAF_AWARE;
         break;
      }
#endif
      case WM_PAINT:
      {
#if !defined WP8
         PAINTSTRUCT ps;
         HDC hDC;
         int32 w,h;
         hideWinCEStuff(); // in fullscreen mode, if the window looses focus, we have to hide the stuff again when we get the focus back. The problem is that there's no event telling that we recover the focus.
         if (!eventsInitialized)
         {
            eventsInitialized = true;
            getScreenSize(&w, &h);
            lastW = w;
            lastH = h;
         }
         else
         {
            getScreenSize(&w, &h);
            if (lastW != w || lastH != h)
               screenChange(mainContext, lastW = w, lastH = h, screen.hRes, screen.vRes, false);
         }
         hDC = BeginPaint(hWnd, &ps);
         markWholeScreenDirty(mainContext);
         updateScreen(mainContext);
         EndPaint(hWnd, &ps);
#endif
         break;
      }
#if defined (WINCE)
      case WM_ERASEBKGND:
      {
         if (eventsInitialized)
            restoreVKSettings();
         break;
      }
#endif
      case WM_LBUTTONDOWN:
      {
         int xx = (int32)((int16)LOWORD(lParam));
         int yy = (int32)((int16)HIWORD(lParam));
         if (yy >= 0 && keepRunning)
         {
            lastPenX = xx;
            lastPenY = yy;
            SetCapture(hWnd);
            postEvent(mainContext, PENEVENT_PEN_DOWN, 0, lastPenX, max32(lastPenY,0),-1);
            isDragging = true;
         }
         break;
      }
      case WM_LBUTTONUP:
      {
         int xx = (int32)((int16)LOWORD(lParam));
         int yy = (int32)((int16)HIWORD(lParam));
         if (yy >= 0 && keepRunning)
         {
            ReleaseCapture();
            isDragging = false;
            lastPenX = lastPenY = -1;
            postEvent(mainContext, PENEVENT_PEN_UP, 0, xx, max32(yy,0),-1);
         }
         break;
      }
#ifndef WINCE
      case WM_MOUSEHWHEEL:
      case WM_MOUSEWHEEL:
      {
         int32 x = lastPenX;//(int32)((int16)LOWORD(lParam)); using the last position because the ones that comes in lParam are relative to window's origin!
         int32 y = lastPenY;//(int32)((int16)HIWORD(lParam));
         int32 a = (int32)((int16)HIWORD(wParam));
         if (y >= 0 && x >= 0 && keepRunning)
            postEvent(mainContext, MOUSEEVENT_MOUSE_WHEEL, a > 0 ? (msg==WM_MOUSEHWHEEL ? WHEEL_RIGHT : WHEEL_UP) : (msg==WM_MOUSEHWHEEL ? WHEEL_LEFT : WHEEL_DOWN), x, max32(y,0),-1);
         break;
      }
#endif
      case WM_MOUSEMOVE:
      {
         int32 x = (int32)((int16)LOWORD(lParam));
         int32 y = (int32)((int16)HIWORD(lParam));
         if (keepRunning && (x != lastPenX || y != lastPenY))
            postEvent(mainContext, isDragging ? PENEVENT_PEN_DRAG : MOUSEEVENT_MOUSE_MOVE, 0, lastPenX = x, lastPenY = max32(y,0),-1);
         break;
      }
      case WM_KEYUP:
         scan = (lParam>>16) & 0xFF;
         if (wParam == 134) return 1; // 134 is always followed by an ENTER key
         if (!keyIsDown[scan]) // some key codes are sent only on key up, so if a key generated no keydown, we assume it is a hotkey
            isHotKey = true;
         keyIsDown[scan] = false;
         if (wParam <= 255)
         {
            int32 sk_event = -1;
            switch (wParam)
            {
               case 13: sk_event = ((getTimeStamp()-actionStart) >= 800) ? SK_MENU : SK_ACTION; break;
               case VK_F1: if (!isWindowsMobile) return 0L; sk_event = SK_MENU; break;   // guich@tc114_86
               case VK_F2: if (!isWindowsMobile) return 0L; sk_event = SK_ESCAPE; break; // guich@tc114_86
            }
            if (sk_event != -1)
            {
               postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, sk_event, 0,0,-1);
               actionStart = 0;
               break;
            }
            actionStart = 0;
            goto cont;
         }
         break;
      case WM_HOTKEY:
         scan = (lParam>>16) & 0xFF;
         if ((lParam & 0x1000) != 0) // ignore event when key is up (process only when it is down)
            break;
         if (wParam == 91 && showKeyCodes) // 91 is sent before every hotkey, so we ignore it
            break;
         isHotKey = true;
      case WM_KEYDOWN:
      {
         if (wParam == 134) return 1; // 134 is always followed by an ENTER key
         scan = (lParam>>16) & 0xFF;
         keyIsDown[scan] = true;
         dontPostOnChar = false;
         if (wParam == 13) // enter/action will show up only when released
         {
            actionStart = getTimeStamp();
            dontPostOnChar = true;
            break;
         }
cont:
         scan = (lParam>>16) & 0xFF;
         key = wParam;
         if (showKeyCodes) // debug keys?
         {
            alert("Key code: %d (dev: %d)\nModifier: %X\nEvent: %s",(int)keyDevice2Portable(key), (int)key, (int)keyGetPortableModifiers(-1), msg==WM_HOTKEY?"WM_HOTKEY":msg==WM_KEYUP?"WM_KEYUP":"WM_KEYDOWN");
            break;
         }
         if (isHotKey && interceptedSpecialKeys != null)
         {
            Int32Array keys = interceptedSpecialKeys; // can store special keys (> 0) or totalcross keys (< 0)
            int32 len = ARRAYLEN(keys);
            for (; len-- > 0; keys++)
               if (keysMatch(*keys, key))
               {
                  key = keyDevice2Portable(*keys);
                  if (key == SK_SCREEN_CHANGE)
                  {
                     if (*tcSettings.screenWidthPtr != *tcSettings.screenHeightPtr)
                        screenChange(mainContext, *tcSettings.screenHeightPtr, *tcSettings.screenWidthPtr, *tcSettings.screenHeightInDPIPtr, *tcSettings.screenWidthInDPIPtr, false);
                  }
                  else
                  {
                     postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, key, 0,0,-1);
                     dontPostOnChar = true;
                  }
                  break;
               }
         }
         else
         {
            int32 pkey = keyDevice2Portable(key);
            if (pkey != key && keyIsDown[scan])
            {
               dontPostOnChar = true;
               if (pkey != SK_SCREEN_CHANGE)
                  postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, pkey, 0,0,-1);
               else
               if (*tcSettings.screenWidthPtr != *tcSettings.screenHeightPtr)
               {
                  int t = screen.minScreenW;
                  screen.minScreenW = screen.minScreenH;
                  screen.minScreenH = t;
                  screenChange(mainContext, *tcSettings.screenHeightPtr, *tcSettings.screenWidthPtr, *tcSettings.screenHeightInDPIPtr, *tcSettings.screenWidthInDPIPtr, false);
               }
            }
         }
         break;
      }
      case WM_CHAR:
         scan = (lParam>>16) & 0xFFFF;
         key = wParam;
         if (showKeyCodes) // debug keys?
         {
            alert("Key code: %d (dev: %d)\nModifier: %X\nEvent: %s",(int)keyDevice2Portable(key), (int)key, (int)keyGetPortableModifiers(-1), "WM_CHAR");
            break;
         }
         if (!dontPostOnChar) // ignore keys that are sent with KEYEVENT_SPECIALKEY_PRESS
            postEvent(mainContext, KEYEVENT_KEY_PRESS, key, 0,0,-1); // must pass control chars, otherwise clipboard functions won't work
         break;
      case WM_COMMAND: // guich@320_48
         if (wParam != 0x10000001) // done button?
            goto def;
      case WM_CLOSE:
#ifdef WINCE // with this postQuitMessage, any MessageBox issued after it will be ignored in win32
         PostQuitMessage(0);
#else
         if (*tcSettings.closeButtonTypePtr == NO_BUTTON)
         {        
            postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS, SK_MENU, 0,0,-1); // must pass control chars, otherwise clipboard functions won't work
            break;
         }
#endif
         printf("WM_CHAR\n");
         keepRunning = false;
         break;
def:
      default:
         return DefWindowProc(hWnd, msg, wParam, lParam);
   }
   return 0L;
}
#endif
bool privateInitEvent()
{
#if !defined WP8

   WNDCLASS wc;
   xmemzero(&wc, sizeof(wc));
   wc.hInstance = GetModuleHandle(0);
   wc.lpfnWndProc = handleWin32Event;
   wc.hCursor = LoadCursor(NULL, IDC_ARROW);
   wc.lpszClassName = exeName;

   if (!RegisterClass(&wc) && GetLastError() != ERROR_CLASS_ALREADY_EXISTS)
      return false;
   return true;
#else
	return true;
#endif
}

bool privateIsEventAvailable()
{
#if defined WP8
   bool ret;
   ret = !eventQueueEmpty();
   return ret;
#else
	MSG msg;
	return PeekMessage(&msg, mainHWnd, 0, 0, PM_NOREMOVE);
#endif
}

void privatePumpEvent(Context currentContext)
{
#if defined(WP8)
	struct eventQueueMember q_member = eventQueuePop();
   //debug("%X - %d.event pop: %d", GetCurrentThreadId(), q_member.count, q_member.type);
   if (q_member.type != 0)
	   postEvent(mainContext, q_member.type, q_member.key, q_member.x, q_member.y, q_member.modifiers);
#else
   MSG msg;
#ifdef WINCE
   if (oldAutoOffValue != 0) // guich@450_33: since the autooff timer function don't work on wince, we must keep resetting the idle timer so that the device will never go sleep - guich@554_7: reimplemented this feature
      SystemIdleTimerReset();
#endif
   if (GetMessage(&msg, mainHWnd, 0, 0))
   {
      TranslateMessage(&msg);
      DispatchMessage(&msg);
   }
   else
   if (msg.message == WM_QUIT) {
      printf("privatePumpEvent\n");
      keepRunning = false;
   }
      
#endif
}

void privateDestroyEvent()
{
}
