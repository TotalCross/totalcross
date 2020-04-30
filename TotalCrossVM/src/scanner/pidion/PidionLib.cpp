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

#include <tcvm.h>
#include "..\barcode.h"

bool CameraLibOpen(OpenParams params);
bool PrinterLibOpen(OpenParams params);
void CameraLibClose();
void PrinterLibClose();

HMODULE apidll;
Context currentContext;

#ifdef __cplusplus
extern "C" {
#endif
SCAN_API int32 LibOpen(OpenParams params)
{
   currentContext = params->currentContext;
   // load the dll
   if ((apidll = LoadLibrary(TEXT("bbappapi.dll"))) == null)
      return false;
   bool b1 = CameraLibOpen(params);
   bool b2 = PrinterLibOpen(params);
   return b1 && b2;
}

SCAN_API void LibClose()
{
   CameraLibClose();
   PrinterLibClose();
   // free the dll
   if (apidll != null)
   {
      FreeLibrary(apidll);
      apidll = null;
   }
}
#ifdef __cplusplus
}
#endif
