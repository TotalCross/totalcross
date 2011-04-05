/*********************************************************************************
 *  Copyright (C) 2002 Guilherme Campos Hazan <guich@superwaba.org.>             *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library is NOT free software.                                           *
 *  Once you paid for it, you can use it in how many devices you want.           *
 *                                                                               *
 *********************************************************************************/

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
