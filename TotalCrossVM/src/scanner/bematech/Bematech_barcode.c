// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "barcode.h"

// now Scanner specific includes
#include "ZBCRLib.h"

HMODULE bematechScanDll = null;
typedef BOOL (__stdcall *ZBCRGetLastNotifyEventProc)(PDWORD lpNotifyEvent);
typedef BOOL (__stdcall *ZBCRSetPowerProc)(BOOL dwState);
typedef BOOL (__stdcall *ZBCRGetLastBarcodeProc)(LPTSTR lpszBarcode);

ZBCRGetLastNotifyEventProc ZBCRGetLastNotifyEventP;
ZBCRSetPowerProc ZBCRSetPowerP;
ZBCRGetLastBarcodeProc ZBCRGetLastBarcodeP;
Method onEventMethod;

SCAN_API int32 LibOpen(OpenParams params)
{
   TCClass scannerClass;

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);

   if ((bematechScanDll = LoadLibrary(TEXT("zbcrlib.dll"))) == null)
      return 0;

   ZBCRGetLastNotifyEventP = (ZBCRGetLastNotifyEventProc) GetProcAddress(bematechScanDll, TEXT("ZBCRGetLastNotifyEvent"));
   ZBCRSetPowerP = (ZBCRSetPowerProc) GetProcAddress(bematechScanDll, TEXT("ZBCRSetPower"));
   ZBCRGetLastBarcodeP = (ZBCRGetLastBarcodeProc) GetProcAddress(bematechScanDll, TEXT("ZBCRGetLastBarcode"));

   if (!ZBCRGetLastNotifyEventP || !ZBCRSetPowerP || !ZBCRGetLastBarcodeP)
   {
      FreeLibrary(bematechScanDll);
      return 0;
   }

   return 1;
}


//----------------------------------------------------------------------------
// Called by the VM to let the native libraries handle particular events.
// Must return true if the event is handled, false otherwise.
//
// Handles scanning events and scan errors.
//----------------------------------------------------------------------------

SCAN_API bool HandleEvent(VoidP eventP)
{
   WinEvent* we = (WinEvent*) eventP;

   switch (we->msg)
   {
      case WM_BCR_NOTIFY:
      {
		  if (we->wParam == BCR_NOTIFY_START_SCAN)
			  executeMethod(we->currentContext, onEventMethod, 0);
		  else if (we->wParam == BCR_NOTIFY_RECEIVE_BARCODE)
			  executeMethod(we->currentContext, onEventMethod, 1);
      } break;
      default:
         return false;
   }

   return true;
}

SCAN_API void LibClose()
{
   if (bematechScanDll != null)
      FreeLibrary(bematechScanDll);
}

//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
	p->retI = ZBCRSetPowerP(TRUE);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
	TCHAR lastBarcode[512]; // arbitrary size of 512, hopefully nobody will ever read a barcode with more than 512 characters
	if (!ZBCRGetLastBarcodeP(lastBarcode))
		p->retO = null;
	else
	{
	   p->retO = createStringObjectFromTCHAR(p->currentContext, lastBarcode, -1);
	   setObjectLock(p->retO, UNLOCKED);
	}   
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   p->retO = createStringObjectFromCharP(p->currentContext, "ZBCRLib", -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   p->retO = createStringObjectFromCharP(p->currentContext, "1.0.0", -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
	p->retI = ZBCRSetPowerP(FALSE);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}