// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "..\barcode.h"
// now Scanner specific includes
//DATALOGIC SCANNER Interface
#include "dl_sedapi.h"
//DATALOGIC SCANNER PARAMETERS
#include "se_parameters.h"


DLScanner scanner;
DLScannerSetup scannerSetup;

enum
{
   WM_BARCODE_LABEL = WM_USER + 10,
};

Method onEventMethod;

SCAN_API int32 LibOpen(OpenParams params)
{
   TCClass scannerClass;

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);
   return 1;
}

SCAN_API bool HandleEvent(VoidP eventP)
{
   WinEvent* we = (WinEvent*) eventP;

   switch (we->msg)
   {
      case WM_BARCODE_LABEL:
      {
         executeMethod(we->currentContext, onEventMethod, 1); // 1 - SCANNED event number

         return true;
      } break;
   }
   return false;
}

SCAN_API void LibClose()
{
}

//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   if (scanner.init())
   {
      scanner.scanEnable();
	   scanner.registerLabelMessage(getMainWindowHandle(), WM_BARCODE_LABEL);
      p->retI = 1;
   }
   else
      p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   int32 type = p->i32[0];
//   int32 barcodeType = p->i32[1]; not used
   int32 value = p->i32[2];

   scannerSetup.setParameter(type, value);
   p->retI = (scannerSetup.getStatus() == scannerSetup.s_nOK);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = 0;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   scannerSetup.update();
   p->retI = (scannerSetup.getStatus() == scannerSetup.s_nOK);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
	int32 len = scanner.getLabelTextLength();
   TCHARP data;

	if (len <= 0 || (data = new TCHAR [len+1]) == 0)
      p->retI = 0;

   scanner.getLabelText(data, len);
   data[len] = _T('\0');
   p->retO = createStringObjectFromTCHARP(p->currentContext, data, len);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   char buf[20];

   xstrprintf(buf, "%d.%d.%d", (int) scanner.getApiMajorVersion(), (int) scanner.getApiMinorVersion(), (int) scanner.getApiBuildVersion());
   p->retO = createStringObjectFromCharP(p->currentContext, buf, -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   char buf[20];

   xstrprintf(buf, "%d.%d.%d", (int) scanner.getDriverMajorVersion(), (int) scanner.getDriverMinorVersion(), (int) scanner.getDriverBuildVersion());
   p->retO = createStringObjectFromCharP(p->currentContext, buf, -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   if (!scanner.isScanEnabled())
      p->retI = 0;

   scanner.scanDisable();
   scanner.deinit();
   p->retI = 1;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}