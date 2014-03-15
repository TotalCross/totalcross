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

static void windowSetSIP(int32 sipOption)
{
   switch (sipOption)
   {
      case SIP_ENABLE_NUMERICPAD:
      case SIP_DISABLE_NUMERICPAD:
      case SIP_HIDE:
      case SIP_TOP:
      case SIP_BOTTOM:
      case SIP_SHOW:
		  break;
   }
}

static void windowSetDeviceTitle(TCObject titleObj)
{
   TCHAR buf[30];
   JCharP2TCHARPBuf(String_charsStart(titleObj), min32(String_charsLen(titleObj),29), buf); // guich@tc113_32: limit to buf's size (and reduced to 30 chars)
   SetWindowText(mainHWnd, buf);
}
