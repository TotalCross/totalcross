// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#if defined WINCE
#include "..\barcode.h"
#include "itcscan.h"

#define MAX_MESSAGE_LENGTH 4096

typedef HRESULT (__stdcall *procITCSCAN_Open)(INT32 *pHandle,LPCTSTR pszDeviceName);
typedef HRESULT (__stdcall *procITCSCAN_Close)(INT32 pHandle);
typedef HRESULT (__stdcall *procITCSCAN_SyncRead)(INT32 pHandle, READ_DATA_STRUCT * pReadDataBlock);
typedef HRESULT (__stdcall *procITCSCAN_CancelRead) (INT32 pHandle, BOOL FlushBufferedData,DWORD *pdwTotalDiscardedMessages,DWORD *pdwTotalDiscardedBytes);
typedef void    (__stdcall *procITCSCAN_GetErrorString) (HRESULT errorCode, TCHAR *pErrorString);
typedef LONG    (__stdcall *procITCSCAN_GetDefaultDevice)(LPTSTR lpDeviceNameBuffer, DWORD * pBufferLength);
typedef HRESULT (__stdcall *procITCSCAN_SetScannerEnable)(INT32 pHandle,INT32 OnOff);

// Globals

int scanError;
INT32   g_handle = 0;

HANDLE decoderDll;

procITCSCAN_Open           SCAN_Open;
procITCSCAN_Close          SCAN_Close;
procITCSCAN_SyncRead       SCAN_SyncRead;
procITCSCAN_CancelRead     SCAN_CancelRead;
procITCSCAN_GetErrorString SCAN_GetErrorString;
procITCSCAN_GetDefaultDevice SCAN_GetDefaultDevice;
procITCSCAN_SetScannerEnable SCAN_Enable;

enum tagUSERMSGS
{
   UM_SCAN = WM_USER + 0x100
};

// Scanner variables
static HANDLE hThread = NULL;  // handle of ScanMonitorThread
LRESULT ScanMonitorThread();
char readBarcode[MAX_MESSAGE_LENGTH];

#define RESULT_SUCCESS 0
Method onEventMethod;
alertFunc alertf;
static bool running;

LRESULT ScanMonitorThread(LPVOID * pHandle)
{
	HRESULT	ReadStatus = 0;
	READ_DATA_STRUCT readDataBlock;
   BYTE rgbDataBuffer[MAX_MESSAGE_LENGTH];

   running = true;
	while (running)
   {
      Sleep(200);
      if (!running)
         break;
		memset (rgbDataBuffer, 0, sizeof(rgbDataBuffer));
		memset (&readDataBlock, 0, sizeof (readDataBlock));
		readDataBlock.rgbDataBuffer = rgbDataBuffer;
		readDataBlock.dwDataBufferSize = sizeof(rgbDataBuffer);
		readDataBlock.dwTimeout = 1000; //5000; //wait 5 seconds for data
		readDataBlock.dwBytesReturned = 0;

		ReadStatus = SCAN_SyncRead((INT32) *pHandle, &readDataBlock);
	
		if (SUCCEEDED(ReadStatus))
		{
			//Get data scanned and add it to the list box			
			rgbDataBuffer[readDataBlock.dwBytesReturned] = 0;
         PostMessage(getMainWindowHandle(), UM_SCAN, ReadStatus, (long) rgbDataBuffer);
         ReadStatus = RESULT_SUCCESS;
		}
//		else
//		if (ReadStatus == E_ITCADC_OPERATION_TIMED_OUT || ReadStatus == E_ITCADC_READ_CANCELLED)
//       ReadStatus = RESULT_SUCCESS;
	}
   return 0;
}

void onClose()
{
   if (hThread != null)  // if we managed to open the scanner
   {
      running = false;
      Sleep(2000);
//      TerminateThread(hThread, 0);
      CloseHandle(hThread);
      hThread = null;
   }
}

SCAN_API int32 LibOpen(OpenParams params)
{
   TCClass scannerClass;
   alertf = params->alert;

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);

   if ((decoderDll = LoadLibrary(TEXT("itcscan.dll"))) != null || (decoderDll = LoadLibrary(TEXT("\\TotalCross\\itcscan.dll"))) != null)
   {
      SCAN_Open            = (procITCSCAN_Open            ) GetProcAddress(decoderDll, TEXT("ITCSCAN_Open"));
      SCAN_Close           = (procITCSCAN_Close           ) GetProcAddress(decoderDll, TEXT("ITCSCAN_Close"));
      SCAN_SyncRead        = (procITCSCAN_SyncRead        ) GetProcAddress(decoderDll, TEXT("ITCSCAN_SyncRead"));
      SCAN_CancelRead      = (procITCSCAN_CancelRead      ) GetProcAddress(decoderDll, TEXT("ITCSCAN_CancelRead"));
      SCAN_GetErrorString  = (procITCSCAN_GetErrorString  ) GetProcAddress(decoderDll, TEXT("ITCSCAN_GetErrorString"));
      SCAN_GetDefaultDevice= (procITCSCAN_GetDefaultDevice) GetProcAddress(decoderDll, TEXT("ITCSCAN_GetDefaultDevice"));
      SCAN_Enable          = (procITCSCAN_SetScannerEnable) GetProcAddress(decoderDll, TEXT("ITCSCAN_SetScannerEnable"));
   }
   return SCAN_Open && SCAN_Close && SCAN_SyncRead && SCAN_CancelRead && SCAN_GetErrorString && SCAN_GetDefaultDevice && SCAN_Enable;
}

SCAN_API void LibClose()
{
   onClose();
}

SCAN_API bool HandleEvent(VoidP eventP)
{
   WinEvent* we = (WinEvent*) eventP;

   switch (we->msg)
   {
      case UM_SCAN:
      {
         xstrcpy(readBarcode, (char*) we->lParam);
         scanError = we->wParam;
         executeMethod(we->currentContext, onEventMethod, 1); // 1 - SCANNED event number
         return true;
      }
   }
   return false;
}

//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   Err err;
   if ((hThread = CreateThread(null, 0, (LPTHREAD_START_ROUTINE) ScanMonitorThread, &g_handle, 0, null)) == null)
      p->retI = false;  // throw exception on error?
   else if ((err = SCAN_Open(&g_handle, L"default")) != RESULT_SUCCESS)
   {
      onClose();
      p->retI = false;  // throw exception on error?
   }
   else
   {
      SCAN_Enable(g_handle, 1);
      p->retI = true;
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   p->retI = false;  // throw exception on error?
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
   if (scanError == RESULT_SUCCESS)
   {
      p->retO = createStringObjectFromCharP(p->currentContext, readBarcode, -1);
      if (p->retO)
         setObjectLock(p->retO, UNLOCKED);
   }
   else
   {
      TCHAR szMessageString[ITCSCAN_MAX_ERROR_STRING_LENGTH];
      TCHAR buf[ITCSCAN_MAX_ERROR_STRING_LENGTH+30];

		SCAN_GetErrorString (scanError, szMessageString);
      wsprintf(buf, TEXT("Error: %d (%S)"), scanError, szMessageString);
      p->retO = createStringObjectFromTCHAR(p->currentContext, buf, -1);
      if (p->retO)
         setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   if ((p->retO = createStringObjectFromCharP(p->currentContext, "1.0", 3)) != null)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   TCHAR name[128];
   DWORD len = sizeof(name);
   if (!SUCCEEDED(SCAN_GetDefaultDevice(name,&len)))
      name[0] = 0;
   p->retO = createStringObjectFromTCHAR(p->currentContext, name, -1);
   if (p->retO != null)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   onClose(); // destroy the thread
   if (g_handle)
   {
	   DWORD	dwTotalDiscardedMessages = 0;
	   DWORD	dwTotalDiscardedBytes = 0;
	   ///HRESULT    status;
      //status = SCAN_CancelRead ((INT32) g_handle, true, &dwTotalDiscardedMessages, &dwTotalDiscardedBytes);
	   //Sleep(500);
      SCAN_Enable(g_handle, 0);
	   SCAN_Close (g_handle);		
      g_handle = 0;
      p->retI = true;
   }
   else p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}

#elif defined (ANDROID)
#include "barcode.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   p->retI = callBoolMethodWithoutParams("scannerActivate");
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{  
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   p->retI = (*env)->CallStaticBooleanMethod(env, applicationClass, (*env)->GetStaticMethodID(env, applicationClass, "setBarcodeParam", "(IZ)Z"), 
                                                                                              (jint)p->i32[0], (jboolean)p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   jstring string = (*env)->CallStaticObjectMethod(env, applicationClass, 
                                                   (*env)->GetStaticMethodID(env, applicationClass, "getData", "()Ljava/lang/String;"));
   Object ret = null;

   if (string)
   {
      const CharP charP = (*env)->GetStringUTFChars(env, string, 0);
      
      if (charP) 
      {
         ret = createStringObjectFromCharP(p->currentContext, charP , -1);        
         (*env)->ReleaseStringUTFChars(env, string, charP);
      }
      (*env)->DeleteLocalRef(env, string); 
   }

   setObjectLock(p->retO = ret, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   if ((p->retO = createStringObjectFromCharP(p->currentContext, "1.0", 3)))
      setObjectLock(p->retO, UNLOCKED);
   else
      throwExceptionNamed(p->currentContext, "java.lang.OutOfMemoryError", null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   if ((p->retO = createStringObjectFromCharP(p->currentContext, "UNKNOWN", 7)))
      setObjectLock(p->retO, UNLOCKED);
   else
      throwExceptionNamed(p->currentContext, "java.lang.OutOfMemoryError", null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   p->retI = callBoolMethodWithoutParams("deactivate");
}

bool callBoolMethodWithoutParams(CharP name)
{
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   return (*env)->CallStaticBooleanMethod(env, applicationClass, (*env)->GetStaticMethodID(env, applicationClass, name, "()Z"));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}
#endif
