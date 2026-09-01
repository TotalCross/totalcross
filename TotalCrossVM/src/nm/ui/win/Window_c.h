// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../GraphicsPrimitives.h"

static void windowBackendSetDeviceTitle(TCObject titleObj)
{
   TCHAR buf[30];
   JCharP2TCHARPBuf(String_charsStart(titleObj), min32(String_charsLen(titleObj), 29), buf);
   SetWindowText(mainHWnd, buf);
}
