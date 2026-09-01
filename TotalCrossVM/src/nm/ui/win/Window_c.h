// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../Window.h"

void adjustWindowSizeWithBorders(int32 resizableWindow, int32* w, int32* h)
{
#ifndef WINCE // windows ce already does this for us
   *w += GetSystemMetrics(resizableWindow ? SM_CXSIZEFRAME : SM_CXFIXEDFRAME)*2;
   *h += GetSystemMetrics(resizableWindow ? SM_CYSIZEFRAME : SM_CYFIXEDFRAME)*2 + GetSystemMetrics(SM_CYCAPTION);
#else
   UNUSED(resizableWindow)
   UNUSED(w)
   UNUSED(h)
#endif
}

static bool windowBackendSetSizeImpl(int32 width, int32 height)
{
#ifndef WINCE // windows ce already does this for us
   adjustWindowSizeWithBorders(*tcSettings.resizableWindow, &width, &height);
   return SetWindowPos(mainHWnd, 0, 0, 0, width, height, SWP_NOMOVE) != 0;
#else
   UNUSED(width)
   UNUSED(height)
   return true;
#endif
}

static void windowBackendSetDeviceTitle(TCObject titleObj)
{
   TCHAR buf[30];
   JCharP2TCHARPBuf(String_charsStart(titleObj), min32(String_charsLen(titleObj), 29), buf);
   SetWindowText(mainHWnd, buf);
}
