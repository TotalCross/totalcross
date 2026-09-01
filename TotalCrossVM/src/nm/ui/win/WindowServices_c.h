// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../Window.h"
#include "../GraphicsPrimitives.h"

#if TC_OS_WINCE && _WIN32_WCE >= 300
 #include <Sipapi.h>
 #include "win/aygshellLib.h"
#endif

#if TC_OS_WINCE && _WIN32_WCE >= 300

static SHIME_MODE oldMode = SHIME_MODE_NONE;

#define IM_NUMBERS                    2
#define IME_ESC_PRIVATE_FIRST         0x0800
#define IME_ESC_SET_MODE              (IME_ESC_PRIVATE_FIRST)
#define EM_SETINPUTMODE               0x00DE

#endif

static bool isShown;

static bool windowPlatformIsSIPShown(void)
{
#if TC_OS_WINCE && _WIN32_WCE >= 300
   if (!*tcSettings.virtualKeyboardPtr)
      return false;
#endif
   return isShown;
}

static void windowPlatformSetSIP(
   Context currentContext,
   int32 sipOption,
   TCObject control,
   bool numeric)
{
   UNUSED(currentContext)
   UNUSED(control)

#if TC_OS_WINCE && _WIN32_WCE >= 300
   if (!*tcSettings.virtualKeyboardPtr)
      return;
#endif

   isShown = sipOption != SIP_HIDE && sipOption != SIP_DISABLE_NUMERICPAD;
   if (numeric)
      sipOption = sipOption == SIP_HIDE ? SIP_DISABLE_NUMERICPAD : SIP_ENABLE_NUMERICPAD;

#if TC_OS_WINDOWS
   switch (sipOption)
   {
      case SIP_HIDE:
      {
         HWND iHandle = FindWindow("IPTIP_Main_Window", "");
         if (iHandle > 0)
            SendMessage(iHandle, WM_SYSCOMMAND, SC_CLOSE, 0);
         break;
      }
      default:
         ShellExecute(NULL, "open", "C:\\Program Files\\Common Files\\Microsoft Shared\\ink\\TabTip.exe", NULL, NULL, SW_SHOWNORMAL);
         break;
   }
#elif TC_OS_WINCE && _WIN32_WCE >= 300
   CLSID Clsid;
   RECT sipRect;
   int32 scrW = GetSystemMetrics(SM_CXSCREEN);
   int32 scrH = GetSystemMetrics(SM_CYSCREEN);

   if (vkSettings.bottom > scrH || vkSettings.right > scrW)
   {
      int32 w = min32(scrW, scrH);
      int32 h = scrH - scrW;
      vkSettings.left = (scrW - w) / 2;
      vkSettings.right = vkSettings.left + w;
      vkSettings.bottom += h;
      vkSettings.top += h;
   }

   switch (sipOption)
   {
#ifndef WIN32_PLATFORM_HPC2000
      case SIP_ENABLE_NUMERICPAD:
         if (_SHGetImeMode != null && _SHSetImeMode != null)
         {
            _SHGetImeMode(mainHWnd, &oldMode);
            _SHSetImeMode(mainHWnd, SHIME_MODE_NUMBERS);
         }
         else
         {
            HRESULT hC = ImmGetContext(mainHWnd);
            ImmSetOpenStatus(hC, TRUE);
            ImmEscape(0, hC, IME_ESC_SET_MODE, (LPVOID)IM_NUMBERS);
            SendMessage(null, EM_SETINPUTMODE, 0, IM_NUMBERS);
         }
         break;
      case SIP_DISABLE_NUMERICPAD:
         if (_SHSetImeMode != null)
            _SHSetImeMode(mainHWnd, oldMode);
         break;
#endif
      case SIP_HIDE:
#ifndef WIN32_PLATFORM_HPC2000
         if (_SHFullScreen != null)
            _SHFullScreen(mainHWnd, SHFS_HIDESIPBUTTON);
#endif
         SipShowIM(SIPF_OFF);
         {
            HWND hsipbtn;
            bSipUp = false;
            hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_BOTTOM, 0, 0, 0, 0,
               SWP_NOMOVE | SWP_NOSIZE | SWP_HIDEWINDOW);
         }
         break;
      case SIP_TOP:
      case SIP_BOTTOM:
         if (sipOption == SIP_TOP)
         {
            sipRect.bottom = screen.screenY + (vkSettings.bottom - vkSettings.top) + vkSettings.topGap;
            sipRect.top = screen.screenY + vkSettings.topGap;
         }
         else if (!isWindowsMobile)
         {
            sipRect.bottom = screen.screenY + screen.screenH;
            sipRect.top = sipRect.bottom - (vkSettings.bottom - vkSettings.top);
         }
         else
         {
            sipRect.bottom = vkSettings.bottom;
            sipRect.top = vkSettings.top;
         }
         sipRect.left = vkSettings.left;
         sipRect.right = vkSettings.right;
         vkSettings.changed = true;

#ifndef WIN32_PLATFORM_HPC2000
         if (_SHFullScreen != null)
            _SHFullScreen(mainHWnd, SHFS_SHOWSIPBUTTON);
#endif
         SipSetDefaultRect(&sipRect);
         SipGetCurrentIM(&Clsid);
         SipSetCurrentIM(&Clsid);
         SipShowIM(SIPF_ON);
         {
            HWND hsipbtn;
            bSipUp = true;
            hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_TOP, 0, 0, 0, 0,
               SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW);
         }
         break;
      case SIP_SHOW:
#ifndef WIN32_PLATFORM_HPC2000
         if (_SHFullScreen != null)
            _SHFullScreen(mainHWnd, SHFS_SHOWSIPBUTTON);
#endif
         SipShowIM(SIPF_ON);
         {
            HWND hsipbtn;
            bSipUp = true;
            hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_TOP, 0, 0, 0, 0,
               SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW);
         }
         break;
   }
#endif
}

static void windowPlatformSetOrientation(int32 orientation)
{
   UNUSED(orientation)
}

static void windowPlatformGetSafeAreaInsets(
   int32 *top,
   int32 *left,
   int32 *bottom,
   int32 *right)
{
   *top = *left = *bottom = *right = 0;
}
