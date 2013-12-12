/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#if defined (WINCE) && _WIN32_WCE >= 300
 #include <Sipapi.h>
 #ifndef WIN32_PLATFORM_HPC2000
  #include <aygshell.h>
 #endif
#endif
#include "../GraphicsPrimitives.h"

/*****   windowSetSIP   *****
 *
 * typedef struct _RECT {
 *  LONG left;
 *  LONG top;
 *  LONG right;
 *  LONG bottom;
 * } RECT;
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Windef.h.
 *
 * SipShowIM
 * SipSetDefaultRect
 * SipGetCurrentIM
 * SipSetCurrentIM
 *
 * OS Versions: Windows CE 2.10 and later.
 * Header: Sipapi.h.
 * Link Library: Coredll.lib.
 *
 *******************************/

#if defined (WINCE) && _WIN32_WCE >= 300
typedef enum tagSHIME_MODE
{
    SHIME_MODE_NONE                = 0,
    SHIME_MODE_SPELL               = 1,
    SHIME_MODE_SPELL_CAPS          = 2,
    SHIME_MODE_SPELL_CAPS_LOCK     = 3,
    SHIME_MODE_AMBIGUOUS           = 4,
    SHIME_MODE_AMBIGUOUS_CAPS      = 5,
    SHIME_MODE_AMBIGUOUS_CAPS_LOCK = 6,
    SHIME_MODE_NUMBERS             = 7,
    SHIME_MODE_CUSTOM              = 8,
} SHIME_MODE;

typedef HRESULT (__stdcall *SHSetImeModeProc)(HWND hWnd, SHIME_MODE nMode);
typedef HRESULT (__stdcall *SHGetImeModeProc)(HWND hWnd, SHIME_MODE* pnMode);

SHSetImeModeProc SHSetImeMode;
SHGetImeModeProc SHGetImeMode;
static SHIME_MODE oldMode = SHIME_MODE_NONE;

static bool loadSip6()
{
   if (aygshellDll && !SHSetImeMode)
   {
      SHSetImeMode = (SHSetImeModeProc) GetProcAddress(aygshellDll, TEXT("SHSetImeMode"));
      SHGetImeMode = (SHGetImeModeProc) GetProcAddress(aygshellDll, TEXT("SHGetImeMode"));
   }
   return SHSetImeMode != null && SHGetImeMode != null;
}


#define IM_NUMBERS         2
#define IME_ESC_PRIVATE_FIRST           0x0800
#define IME_ESC_SET_MODE                   (IME_ESC_PRIVATE_FIRST)
#define EM_SETINPUTMODE       0x00DE    // Sets default input mode when control gets focus. lParam should be be EIM_*|EIMMF_*.

#endif

static void windowSetSIP(int32 sipOption)
{
#if defined (WP8)
	setKeyboard(true);
#elif defined (WINCE) && _WIN32_WCE >= 300
   CLSID Clsid;
   RECT sipRect;
   int32 scrW = GetSystemMetrics(SM_CXSCREEN);
   int32 scrH = GetSystemMetrics(SM_CYSCREEN);

   if (vkSettings.bottom > scrH || vkSettings.right > scrW) // guich@tc110_99
   {
      int32 w = min32(scrW,scrH); // if the screen has been rotated, then the width is surely the width in portrait (mininum value)
      int32 h = scrH - scrW;
      vkSettings.left = (scrW - w) / 2; // center
      vkSettings.right = vkSettings.left + w;
      vkSettings.bottom += h;
      vkSettings.top += h;
   }

   switch (sipOption)
   {
#ifndef WIN32_PLATFORM_HPC2000
      case SIP_ENABLE_NUMERICPAD:
         if (loadSip6())
         {
            SHGetImeMode(mainHWnd, &oldMode);
            SHSetImeMode(mainHWnd, SHIME_MODE_NUMBERS);
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
         if (loadSip6())
            SHSetImeMode(mainHWnd, oldMode);
         break;
#endif
      case SIP_HIDE:
#ifndef WIN32_PLATFORM_HPC2000
         SHFullScreen(mainHWnd, SHFS_HIDESIPBUTTON);
#endif
         SipShowIM(SIPF_OFF);
         {  //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
            HWND hsipbtn;
            bSipUp = false;
            hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_BOTTOM, 0,0, 0,0, SWP_NOMOVE | SWP_NOSIZE | SWP_HIDEWINDOW);
         }
         break;
      case SIP_TOP:
      case SIP_BOTTOM:
         if (sipOption == SIP_TOP)
         {
            sipRect.bottom = screen.screenY + (vkSettings.bottom - vkSettings.top) + vkSettings.topGap;
            sipRect.top = screen.screenY + vkSettings.topGap;
         }
         else
         {
            if (!isWindowsMobile)
            {
               // flsobral@tc113_24: SIP bottom position is always relative to the screen, not to its original position.
               sipRect.bottom = screen.screenY + screen.screenH;
               sipRect.top = sipRect.bottom - (vkSettings.bottom - vkSettings.top);
            }
            else
            {
               // flsobral@tc113_42: SIP is relative to the screen only on WindowsCE, WM devices use the original SIP position.
               sipRect.bottom = vkSettings.bottom;
               sipRect.top = vkSettings.top;
            }
         }
         sipRect.left = vkSettings.left;
         sipRect.right = vkSettings.right;
         vkSettings.changed = true;

#ifndef WIN32_PLATFORM_HPC2000
         SHFullScreen(mainHWnd, SHFS_SHOWSIPBUTTON);
#endif
         SipSetDefaultRect(&sipRect);
         SipGetCurrentIM(&Clsid);
         SipSetCurrentIM(&Clsid);
         SipShowIM(SIPF_ON);
         {  //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
            HWND hsipbtn;
            bSipUp = true;
            hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_TOP, 0,0, 0,0, SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW);
         }
         break;
      case SIP_SHOW:
#ifndef WIN32_PLATFORM_HPC2000
         SHFullScreen(mainHWnd, SHFS_SHOWSIPBUTTON);
#endif
         SipShowIM(SIPF_ON);
         {  //flsobral@tc114_50: fixed the SIP keyboard button not being properly displayed on some WinCE devices.
            HWND hsipbtn;
            bSipUp = true;
            hsipbtn = FindWindow(_T("MS_SIPBUTTON"), _T("MS_SIPBUTTON"));
            SetWindowPos(hsipbtn, HWND_TOP, 0,0, 0,0, SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW); // sets z-order to top but doesn't reposition or resize
         }
         break;
   }
#endif
}

static void windowSetDeviceTitle(Object titleObj)
{
#ifndef WP8
   TCHAR buf[30];
   JCharP2TCHARPBuf(String_charsStart(titleObj), min32(String_charsLen(titleObj),29), buf); // guich@tc113_32: limit to buf's size (and reduced to 30 chars)
   SetWindowText(mainHWnd, buf);
#endif
}
