// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



// Note: to download the Scanner API, go to:
// http://www.symbol.com/services/downloads/ppt2700_pocket_pc_sdk.html

#include "barcode.h"

#include <winioctl.h>
// now Scanner specific includes
#include "BCRService.h"

#define ServiceName TEXT("BCR0:")
static HANDLE hService = INVALID_HANDLE_VALUE;
static sBCR_Status status;
static int isOk;
static TCHAR szScanEvent[32];
static HANDLE hScanEvent = INVALID_HANDLE_VALUE;
static Context scannerContext;

alertFunc procAlert;

Method onEventMethod;

SCAN_API int32 LibOpen(OpenParams params)
{
   TCClass scannerClass;

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);

   scannerContext = params->currentContext;
   procAlert = params->alert;
   
   // get the handle to the scanner and the scanner event
   isOk = (hService = CreateFile(ServiceName, GENERIC_READ | GENERIC_WRITE, 0, null, OPEN_EXISTING, 0, 0)) != INVALID_HANDLE_VALUE &&
      RegisterHotKey(getMainWindowHandle(), OEM_VK_BCR_TRIGGER, MOD_KEYUP, OEM_VK_BCR_TRIGGER);
   status.dwSize = sizeof(status);

   return 1;
}

static char barcode[4096];

DWORD WINAPI ScanThread( LPVOID lpParameter )
{
	while (1)
	{
		if (WaitForSingleObject(hScanEvent, INFINITE) == WAIT_OBJECT_0)
      {
			DWORD read = 0, totalRead = 0;
         char* ptr = barcode;
			ResetEvent(hScanEvent);
         // read the barcode
			while (1)
			{
				ReadFile(hService, ptr, 256, &read, NULL);
				ptr += read;
				totalRead += read;
				if (read < 256)
					break;
				Sleep(50);
			}
			while (barcode[--totalRead] < ' ')
            barcode[totalRead] = 0; // trim the string
         executeMethod(scannerContext, onEventMethod, 1); // 1 - SCANNED event number
		}
		else break;
	}
	return 0;
}

static bool disableKeyboardHook()
{
   DWORD Result;
	status.dwMask = MASK_KEYHOOK_EMABLE;
	status.dwKeyboardHook = BCRSTATUS_HOOK_OFF;
	DeviceIoControl(hService, BCR_IOCTL_UPDATE_SETTING, &status, sizeof(status), &Result, sizeof(DWORD), NULL, NULL);
   return Result == BCR_SUCCESS;
}

static bool scannerActivate(bool activate)
{
	if (isOk)
   {
      DWORD Result;
		status.dwMask = MASK_MODULE_ENABLE;
      status.dwModuleEnable = activate ? BCRSTATUS_MOD_ENABLE : BCRSTATUS_MOD_DISABLE;
		DeviceIoControl(hService, BCR_IOCTL_UPDATE_SETTING, &status, sizeof(status), &Result, sizeof(DWORD), NULL, NULL);
      if (Result == BCR_SUCCESS && hScanEvent == INVALID_HANDLE_VALUE) // get the event name
      {
         if (DeviceIoControl(hService, BCR_IOCTL_GET_EVENTNAME, NULL, 0, szScanEvent, sizeof(szScanEvent), &Result, NULL) &&
            (hScanEvent = CreateEvent(NULL, TRUE, FALSE, szScanEvent)) != NULL &&
            disableKeyboardHook()) // create the thread
         {
	         DWORD dwThreadId;
            HANDLE hReadGoodThread;
	         hReadGoodThread = CreateThread( NULL, 0, &ScanThread, NULL, 0, &dwThreadId);
	         CloseHandle(hReadGoodThread);
         }
      }
		return Result == BCR_SUCCESS;
	}
   return false;
}

SCAN_API void LibClose()
{
	if (isOk)
   {
      CloseHandle(hService);
      CloseHandle(hScanEvent);
      hService = hScanEvent = INVALID_HANDLE_VALUE;
      isOk = false;
   }
}

SCAN_API bool HandleEvent(VoidP eventP)
{
   WinEvent* we = (WinEvent*) eventP;

   if (we->msg == WM_HOTKEY && we->wParam == OEM_VK_BCR_TRIGGER)
   {
	   SHORT nState;
	   DWORD BCR_PRESS = BCR_TRIG_PRESS;
	   DWORD BCR_RELEASE = BCR_TRIG_RELEASE;
      DWORD Result, Result1;
      nState = GetAsyncKeyState(OEM_VK_BCR_TRIGGER);
      DeviceIoControl(hService, BCR_IOCTL_TRIGGER, HIBYTE(nState) ? &BCR_PRESS : &BCR_RELEASE, sizeof(DWORD), &Result, sizeof(DWORD), &Result1, NULL);
   }
   return false;
}

//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   p->retI = isOk && scannerActivate(true);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   int32 barcodeType = p->i32[0];
   int32 value = p->i32[1];
   DWORD result;

   status.dwMask = MASK_BCTYPE;
   status.dwBCType = barcodeType;

   DeviceIoControl(hService, BCR_IOCTL_UPDATE_SETTING, &status, sizeof(status), &result, sizeof(DWORD), NULL, NULL);
   p->retI = (result == BCR_SUCCESS);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   p->retO = createStringObjectFromCharP(p->currentContext, barcode, -1);
   setObjectLock(p->retO, UNLOCKED);
   *barcode = 0;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   if (isOk)
   {
      DeviceIoControl(hService, BCR_IOCTL_GET_STATUS, null, 0, &status, sizeof(status), null, null);
      p->retO = createStringObjectFromCharP(p->currentContext, status.szModuleVersion, -1);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   p->retO = createStringObjectFromCharP(p->currentContext, "", -1);
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   p->retI = isOk && scannerActivate(false);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}