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

// FROM extlibs/D7600/Include/Armv4i/Decoder.h

#define MAX_MESSAGE_LENGTH 4096

#define SYMID_EAN8   'D'
#define SYMID_EAN13  'd'

typedef enum
{
   RESULT_INITIALIZE = -1,
   RESULT_SUCCESS  = 0,                 // Operation was successful
   RESULT_ERR_BADREGION,                // An image was requested using an invalid image region
   RESULT_ERR_DRIVER,                   // Error detected in image engine driver
   RESULT_ERR_ENGINEBUSY,               // Image engine driver reported busy
   RESULT_ERR_MEMORY,                   // Memory allocation failed
   RESULT_ERR_NODECODE,                 // Image engine unable to decode a symbology
   RESULT_ERR_NOIMAGE,                  // No image available
   RESULT_ERR_NORESPONSE,               // Could not communicate with imager
   RESULT_ERR_NOTCONNECTED,             // Not connected to image engine
   RESULT_ERR_PARAMETER,                // One of the function parameters was invalid
   RESULT_ERR_UNSUPPORTED,              // The operation was not supported by the engine
   RESULT_ERR_NOTRIGGER,                // Trigger state is false
   RESULT_ERR_BADSMARTIMAGE,            // IQ image fail
   RESULT_ERR_SMARTIMAGETOOLARGE,       // Requested IQ image too large
   RESULT_ERR_TOO_MUCH_INTERPOLATION,   // IQ image fail
   RESULT_ERR_WRONGRESULTSTRUCT,        // Invalid structure size
   RESULT_ERR_THREAD,                   // Could not create async decode thread
   RESULT_ERR_CANCEL,                   // Asynchronous decode was canceled
   RESULT_ERR_EXCEPTION,                // An exception was detected in the decoder
   RESULT_ERR_UNSUPPORTED_IQ_BARCODE    // Scanned barcode is not a valid IQ host barcode
} Result_t;

typedef Result_t (__stdcall *procDecWaitForDecode)(DWORD dwTime, BYTE* pchMessage, BYTE* pchCodeID, BYTE* pchSymLetter, BYTE* pchSymModifier, WORD* pnLength, BOOL (*fpCallBack)(void));
typedef Result_t (__stdcall *procDecConnect)(void);
typedef Result_t (__stdcall *procDecDisconnect)(void);
typedef Result_t (__stdcall *procDecGetDecoderRevision)(TCHAR* pszRev);
typedef Result_t (__stdcall *procDecEnableDisableSymbology)(int nSymId, BOOL bEnable);

// Globals

int scanError;
Method onEventMethod;

HANDLE decoderDll;
procDecWaitForDecode          decWaitForDecodeProc;
procDecConnect                decConnectProc;
procDecDisconnect             decDisconnectProc;
procDecGetDecoderRevision     decGetDecoderRevisionProc;
procDecEnableDisableSymbology decEnableDisableSymbologyProc;

/* For exception support.
static CharP getDolphinMessage(Result_t code)
{
   switch (code)
   {
      case -1 : return "";
      case  0 : return "Operation was successful";
      case  1 : return "An image was requested using an invalid image region";
      case  2 : return "Error detected in image engine driver";
      case  3 : return "Image engine driver reported busy";
      case  4 : return "Memory allocation failed";
      case  5 : return "Image engine unable to decode a symbology";
      case  6 : return "No image available";
      case  7 : return "Could not communicate with imager";
      case  8 : return "Not connected to image engine";
      case  9 : return "One of the function parameters was invalid";
      case 10 : return "The operation was not supported by the engine";
      case 11 : return "Trigger state is false";
      case 12 : return "IQ image fail";
      case 13 : return "Requested IQ image too large";
      case 14 : return "IQ image fail";
      case 15 : return "Invalid structure size";
      case 16 : return "Could not create async decode thread";
      case 17 : return "Asynchronous decode was canceled";
      case 18 : return "An exception was detected in the decoder";
      case 19 : return "Scanned barcode is not a valid IQ host barcode";
      default : return "Unknown error";
   }
}
*/

enum tagUSERMSGS
{
   UM_SCAN = WM_USER + 0x100
};

// Scanner variables
static HANDLE hThread = NULL;  // handle of ScanMonitorThread
LRESULT ScanMonitorThread();
char readBarcode[MAX_MESSAGE_LENGTH];
static bool running;

#define SCAN_KEY        0x2a
int scanning = 0;
BOOL CheckOnSCAN(void)
{
   return scanning;
}
static bool isScanKeyDown()
{
   return GetAsyncKeyState(SCAN_KEY) < 0 || GetAsyncKeyState(193) < 0 || GetAsyncKeyState(195) < 0 || GetAsyncKeyState(194) < 0; // last three are for dolphin 6510
}

LRESULT ScanMonitorThread()
{
   char cCodeID, cSymLetter, cSymModifier;
   char barcode[MAX_MESSAGE_LENGTH];
   char barcode2[MAX_MESSAGE_LENGTH];
   unsigned short uBarcodeLen;
   Result_t nResult;
   int32 checkDigit = 0;
   int32 i;
   barcode[0] = 0;

   running = true;
   while (running)
   {
      Sleep(50);
      if (!running)
         break;
	  if (!scanning && isScanKeyDown())
      {
         scanning = 1;
		 checkDigit = 0;
		 xmemzero(barcode, MAX_MESSAGE_LENGTH);
         nResult = decWaitForDecodeProc(500, (BYTE*) barcode, (BYTE*) &cCodeID,(BYTE*) &cSymLetter, (BYTE*) &cSymModifier, &uBarcodeLen, CheckOnSCAN);
         if (nResult == RESULT_SUCCESS)
         {
            if (cCodeID == SYMID_EAN13 || cCodeID == SYMID_EAN8)
            {
               for (i = uBarcodeLen - 1 ; i >= 0 ; i--)
                  checkDigit += (barcode[i] - '0') * ((i % 2 == 0) ? 1 : 3);
               checkDigit %= 10;
               if (checkDigit == 0) //flsobral@tc114_24: fixed check digit generation for barcodes which weighted value mod is 0.
                  barcode[uBarcodeLen] = '0';
               else
               {
                  checkDigit = 10 - checkDigit;
                  barcode[uBarcodeLen] = '0' + checkDigit;
               }
               barcode[uBarcodeLen + 1] = 0;
            }
            PostMessage(getMainWindowHandle(), UM_SCAN, nResult, (long) barcode);
            // clear read-ahead - guich@tc330 - removed this because the scanner will keep on while the user is looking at a barcode
            // while (decWaitForDecodeProc(50, (BYTE*) barcode2, (BYTE*) &cCodeID, (BYTE*) &cSymLetter, (BYTE*) &cSymModifier, &uBarcodeLen, 0) == RESULT_SUCCESS);

			// wait until the user release the key
			while (isScanKeyDown())
				Sleep(50);
         }
         scanning = 0;
      }
   }
   return 0;
}

void onClose()
{
   if (hThread != null)  // if we managed to open the scanner
   {
      running = false;
      Sleep(500);
//      TerminateThread(hThread, 0);
      CloseHandle(hThread);
      hThread = null;
   }
}

SCAN_API int32 LibOpen(OpenParams params)
{
   TCClass scannerClass;

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);

   if ((decoderDll = LoadLibrary(TEXT("decoder.dll"))) != null)
   {
      decWaitForDecodeProc          = (procDecWaitForDecode)   GetProcAddress(decoderDll, TEXT("decWaitForDecode"));
      decConnectProc                = (procDecConnect)         GetProcAddress(decoderDll, TEXT("decConnect"));
      decDisconnectProc             = (procDecDisconnect)      GetProcAddress(decoderDll, TEXT("decDisconnect"));
      decGetDecoderRevisionProc     = (procDecGetDecoderRevision) GetProcAddress(decoderDll, TEXT("decGetDecoderRevision"));
      decEnableDisableSymbologyProc = (procDecEnableDisableSymbology) GetProcAddress(decoderDll, TEXT("decEnableDisableSymbology"));
   }
   return decWaitForDecodeProc && decConnectProc && decDisconnectProc && decGetDecoderRevisionProc && decEnableDisableSymbologyProc;
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
   if ((hThread = CreateThread(null, 0, (LPTHREAD_START_ROUTINE) ScanMonitorThread, null, 0, null)) == null)
      p->retI = false;  // throw exception on error?
   else if ((err = decConnectProc()) != RESULT_SUCCESS)
   {
      onClose();
      p->retI = false;  // throw exception on error?
   }
   else
      p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   int32 barcodeType = p->i32[0];
   int32 enable = p->i32[1];
   Err err;

   if ((err = decEnableDisableSymbologyProc(barcodeType, enable)) != RESULT_SUCCESS)
      p->retI = false;  // throw exception on error?
   else
      p->retI = true;
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

/*
   if (nResult != RESULT_ERR_NOTRIGGER && nResult != RESULT_ERR_NODECODE)
   {
      TCHAR buf[50];
      wsprintf(buf, TEXT("Error: %d"), nResult);
      p->retO = createStringObjectFromTCHAR(buf, -1);
   }
 */
   p->retO = null;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   TCHAR buf[MAX_PATHNAME];
   Err err;

   if ((err = decGetDecoderRevisionProc(buf)) != RESULT_SUCCESS)
      p->retO = null;   // throw exception on error?
   else if ((p->retO = createStringObjectFromTCHAR(p->currentContext, buf, -1)) != null)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   TCHAR buf[MAX_PATHNAME];
   Err err;

   if ((err = decGetDecoderRevisionProc(buf)) != RESULT_SUCCESS)
      p->retO = null;   // throw exception on error?
   else if ((p->retO = createStringObjectFromTCHAR(p->currentContext, buf, -1)) != null)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   Err err;
   
   onClose(); // destroy the thread
   if ((err = decDisconnectProc()) != RESULT_SUCCESS)
      p->retI = false;  // throw exception on error?
   else
      p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}