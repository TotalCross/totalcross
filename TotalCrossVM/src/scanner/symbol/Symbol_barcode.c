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

//----------------------------------------------------------------------------
// Symbol Scanner Support
//
// This file includes all the native functions required to support the
// SuperWaba Scanner class.
//
// This dll starts up a thread to monitor the scanner, and uses
// SCAN_ReadLabelEvent to be notified of scans.
//
// It will also stop scanning for a period of time when the unit is
// powered up - see readme-ce.txt for more information.
//
// The power-up Event name is taken from registry setting
// HKCU\Software\SuperWaba\Scanner\pwrEvent, and defaults to the definition
// POWER_ON_EVENT if the registry key does not exist.
//----------------------------------------------------------------------------

//#define appErrorClass 0 // required to compile in Windows
#define TC_privateXfree                privateXfree
#define TC_privateXmalloc              privateXmalloc
#define TC_privateXrealloc             privateXrealloc
#define TC_createArrayObject           createArrayObject
#include "..\barcode.h"

// now Scanner specific includes
#include <Strucinf.h>
#include <ScanCAPI.H>

//----------------------------------------------------------------------------
// Definitions
//----------------------------------------------------------------------------
#define BUFFER_SIZE 2710

// name of the default event which the pwrdll.dll sets when the unit it powered on
// This program will first look in registry setting HKCU\Software\SuperWaba\Scanner\pwrEvent
#define POWER_ON_EVENT TEXT("SW_PowerOnEventName")

// message number to use by this application
// TODO: see how we are supposed to get this to avoid clashes!
enum tagUSERMSGS
{
   UM_SCAN = WM_USER + 0x100
      , UM_SCAN_ERROR
};

//----------------------------------------------------------------------------
// function prototypes
//----------------------------------------------------------------------------


// other internal functions
static BOOL TranslateDecoder(int32 decoder, LPDECODER translated);
static BOOL ScanStartMonitorThread();
LRESULT ScanMonitorThread() ;
static HANDLE CreatePowerUpEvent();
static BOOL ScanOpen();
static BOOL ScanSetup();
static void ScanCancelReads();
static void ScanClose();
static BOOL PostErrorMessage(LPTSTR szInMsg, DWORD dwError);

//----------------------------------------------------------------------------
// Global variables
//----------------------------------------------------------------------------

// Scanner variables
static HANDLE hScanner = NULL; // handle of the scanner
static LPSCAN_BUFFER lpScanBuffer = NULL;  // pointer to the scan buffer
static HANDLE hThread = NULL;  // handle of ScanMonitorThread
// Handles for some of the events we are waiting for
static HANDLE hActivateScanner  = NULL;  // request to activate the scanner
static HANDLE hDeactivateScanner = NULL;  // request to deactivate the scanner
static HANDLE hAbortEvent = NULL;      // abort the thread which is monitoring the scanner
static int32 scannerWaitingTime = INFINITE; // guich@580_22
static Context scannerContext;

#include "RcmCAPI.h"
Method onEventMethod;

debugFunc procDebug;

/*static void setRomSerialNumber() // guich@567_9
{
   TCSettings tcSettings = getSettingsPtr();
   UNITID var;
   xmemzero(var, sizeof(var));

   if ((RCM_GetUniqueUnitId(&var) == 0))
   {
      int i;
      char serial[20];
      Class settingsClass;
      char* s = serial;
      *s++ = '0';
      *s++ = 'x';
      for (i = 0; i < 8; i++, s+=2)
         int2hex(var[i],2,s);
      *s = 0;
      settingsClass = loadClass(scannerContext, "totalcross.sys.Settings", true);
      setObjectLock(*getStaticFieldObject(null,settingsClass, "romSerialNumber") = createStringObjectFromCharP(scannerContext, serial, -1), UNLOCKED);
   }
}*/

static bool createScanner()
{
   if (!hThread)
   {
      if (!ScanOpen())
         return false;

      ScanStartMonitorThread();
   }
   return true;
}

static void destroyScanner()
{
   if (hThread)  // if we managed to open the scanner
   {
      SetEvent(hAbortEvent);
      // wait for the ScanMonitorThread to complete before we return
      WaitForSingleObject(hThread, INFINITE);
      CloseHandle(hThread);
      hThread = null;
   }
}

SCAN_API int32 LibOpen(OpenParams params)
{
   HMODULE tcvmModule = GetModuleHandle(TEXT("tcvm.dll"));
   TCClass scannerClass;

   procDebug = (debugFunc) GetProcAddress(tcvmModule, L"debug");

   scannerClass = loadClass(params->currentContext, "totalcross.io.device.scanner.Scanner", false);
   onEventMethod = getMethod(scannerClass, false, "_onEvent", 1, J_INT);

   scannerContext = params->currentContext;
   //setRomSerialNumber();

   return createScanner();
}

SCAN_API bool HandleEvent(VoidP eventP)
{
   WinEvent* we = (WinEvent*) eventP;
   char msg[128];

   switch (we->msg)
   {
      case UM_SCAN:
      {
         executeMethod(scannerContext, onEventMethod, 1);
         return true;
      } break;
      case UM_SCAN_ERROR:
      {
         xstrprintf(msg, "Scanner error:\n%d\nPlease reboot this unit", we->lParam);
         throwException(scannerContext, RuntimeException, msg);
         return true;
      } break;
   }
   return false;
}

SCAN_API void LibClose()
{
   destroyScanner();
}


//----------------------------------------------------------------------------
// superwaba/ext/xplat/NativeMethods static boolean scannerSetBarcodeParam(int barcodeType, boolean enable)
//
// Enable/disable a decoder
//----------------------------------------------------------------------------
DWORD ScanToggleDecoder(LPDECODER lpDecoder, bool enable)
{
	DECODER_LIST DecoderList;
	DWORD dwResult;

	// Mark entire buffer allocated and not used
	SI_INIT(&DecoderList);
	// Get enabled decoder list
	dwResult = SCAN_GetEnabledDecoders(hScanner, &DecoderList);
	if (dwResult == E_SCN_SUCCESS)
	{
		DWORD i;
		DWORD decoderCount = DecoderList.Decoders.dwDecoderCount;
		for (i = 0 ; i < DecoderList.Decoders.dwDecoderCount ; i++)
		{
			if (DecoderList.Decoders.byList[i] == (BYTE)*lpDecoder)
			{
            if (enable)
               return(E_SCN_SUCCESS);
				memcpy(DecoderList.Decoders.byList+i, DecoderList.Decoders.byList+i+1, decoderCount-i-1);
				DecoderList.Decoders.dwDecoderCount--;
			}
		}
      if (enable)
      {
		   SI_SET_FIELD(&DecoderList, Decoders.byList[DecoderList.Decoders.dwDecoderCount], (BYTE)*lpDecoder);
		   DecoderList.Decoders.dwDecoderCount++;
		   dwResult = SCAN_SetEnabledDecoders(hScanner, &DecoderList);
      }
      else if (decoderCount > DecoderList.Decoders.dwDecoderCount) // something is disabled
			dwResult = SCAN_SetEnabledDecoders(hScanner, &DecoderList);
	}
	return dwResult;
}

//----------------------------------------------------------------------------
// superwaba/ext/xplat/io/scanner/NativeMethods boolean setBarcodeLength(int barcodeType, int lengthType, int length1, int length2 )
//----------------------------------------------------------------------------
DWORD ScanSetDecoderLengths(LPDECODER lpDecoder,
							DWORD dwMinLength,
							DWORD dwMaxLength)
{
	DECODER_PARAMS decoder_params;
	DWORD dwResult;

	// Initialize the structure to all zeroes
	xmemzero(&decoder_params,sizeof(decoder_params));

	// Mark entire buffer allocated and not used
	SI_INIT(&decoder_params);

	// Get current decoder parameters
	dwResult = SCAN_GetDecoderParams(hScanner,
									 lpDecoder,
									 &decoder_params);

	// If there was an error getting the parameters
	if ( dwResult != E_SCN_SUCCESS )
	{
		// Return the error
		return(dwResult);
	}

	// Set all parameters into structure if changed
	SI_SET_IF_CHANGED(&decoder_params,dwMinLength,dwMinLength);
	SI_SET_IF_CHANGED(&decoder_params,dwMaxLength,dwMaxLength);

	// Return the result of setting parameters
	return(SCAN_SetDecoderParams(hScanner,
								 lpDecoder,
								 &decoder_params));
}

//----------------------------------------------------------------------------
// Translate the decoder from the Palm format used by SuperWaba to the format
// used by the CE scanner API.  Symbol's CE & Palm API decoder values don't
// match.  Don't you just love consistent APIs
//----------------------------------------------------------------------------
static BOOL TranslateDecoder(int32 decoder, LPDECODER translated)
{
   switch (decoder)
   {
      case 0: strcpy(translated, DECODER_CODE39); break;
      case 1: strcpy(translated, DECODER_UPCA); break;
      case 2: strcpy(translated, DECODER_UPCE0); break;
      case 3: strcpy(translated, DECODER_EAN13); break;
      case 4: strcpy(translated, DECODER_EAN8); break;
      case 5: strcpy(translated, DECODER_D2OF5); break;
      case 6: strcpy(translated, DECODER_I2OF5); break;
      case 7: strcpy(translated, DECODER_CODABAR); break;
      case 8: strcpy(translated, DECODER_CODE128); break;
      case 9: strcpy(translated, DECODER_CODE93); break;
      case 11: strcpy(translated, DECODER_MSI); break;
      case 12: strcpy(translated, DECODER_UPCE1); break;
      case 13: strcpy(translated, DECODER_TRIOPTIC39); break;
      case 15: strcpy(translated, DECODER_CODE11); break;
   // the following decoders aren't available on PPT2700 & 2800
   // case 14: strcpy(translated, DECODER_EAN128); break;
   // case 83: strcpy(translated, DECODER_BROOKLAND); break;
   // case 84: strcpy(translated, DECODER_ISBT128); break;
   // case 85: strcpy(translated, DECODER_COUPON); break;
      default: return FALSE; // unknown decoder
   }
   return TRUE;
}

//----------------------------------------------------------------------------
// Start up a seperate thread to monitor the scanner
//----------------------------------------------------------------------------
static BOOL ScanStartMonitorThread()
{
   DWORD dwThreadId;

   hAbortEvent = CreateEvent( NULL, TRUE, FALSE, NULL );
   hActivateScanner  = CreateEvent( NULL, TRUE, FALSE, NULL );
   hDeactivateScanner = CreateEvent( NULL, TRUE, FALSE, NULL );

   hThread = CreateThread( NULL, 0, (LPTHREAD_START_ROUTINE) ScanMonitorThread, NULL, 0,  &dwThreadId );
   if (hThread == NULL)
   {
      CloseHandle(hAbortEvent);
      CloseHandle(hActivateScanner);
      CloseHandle(hDeactivateScanner);
      hAbortEvent = NULL;
      hActivateScanner = NULL;
      hDeactivateScanner = NULL;
      return FALSE;
   }
   else
   {
      SetThreadPriority (hThread, THREAD_PRIORITY_ABOVE_NORMAL );
      return TRUE;
   }
}



//----------------------------------------------------------------------------
// Thread function which monitors the scanner.
//----------------------------------------------------------------------------
LRESULT ScanMonitorThread()
{
   HANDLE hEventPowerUp;
   HANDLE hScannerEvent;
   DWORD dwRequestId = 0;

   BOOL bAbort = FALSE;
   BOOL bIsScannerActive = FALSE;

   HANDLE h[ 5 ] ;
   int eventCount = 0;

   hScannerEvent = CreateEvent( NULL, TRUE, FALSE, NULL );
   hEventPowerUp = CreatePowerUpEvent();

   //  Setup the events that we want to wait for
   h[ eventCount++ ] = hScannerEvent ;                 // WAIT_OBJECT_0
   h[ eventCount++ ] = hAbortEvent ;               // WAIT_OBJECT_0+1
   h[ eventCount++ ] = hDeactivateScanner ;               // WAIT_OBJECT_0+2
   h[ eventCount++ ] = hActivateScanner ;                // WAIT_OBJECT_0+3
   if ( hEventPowerUp )
      h[ eventCount++ ] = hEventPowerUp ;          // WAIT_OBJECT_0+4

   while( !bAbort )
   {
      DWORD dwResult = 0;
      ScanCancelReads();

      // If we are 'active' then put a read into the scanner otherwise we
      // are inactive, so do nothing
      if ( bIsScannerActive )
      {
         if ( hScannerEvent )
            CloseHandle( hScannerEvent );
         hScannerEvent = CreateEvent( NULL, TRUE, FALSE, NULL );
         h[ 0 ] = hScannerEvent ;                 // WAIT_OBJECT_0
         dwResult = SCAN_ReadLabelEvent( hScanner, lpScanBuffer, hScannerEvent, 8460000, &dwRequestId );
         if ( dwResult != E_SCN_SUCCESS )
            PostErrorMessage(TEXT("SCAN_ReadLabelEvent"), dwResult);
      }

      dwResult = WaitForMultipleObjects( eventCount, h, FALSE, scannerWaitingTime);

      switch ( dwResult )
      {
         case WAIT_OBJECT_0 :           // scanner woke us up
         {
#if _WIN32_WCE >= 300
            // make sure the device does not go into suspended state.  The device does
            // not consider a scan to be a user activity, so may power off if we
            // don't call SystemIdleTimerReset.
            SystemIdleTimerReset();
#endif
            ResetEvent( hScannerEvent );
            if ( lpScanBuffer->dwStatus == E_SCN_SUCCESS )
            {
               bIsScannerActive = FALSE;
               // post a message back to the main thread that a barcode
               // has been scanned.  Since SuperWaba isn't multithreaded we
               // cannot post an event into the VM from this thread.
               PostMessage(getMainWindowHandle(), UM_SCAN, 0, 0);
               continue;
             }
             else
             if ( lpScanBuffer->dwStatus == E_SCN_DEVICEFAILURE )
             {
                // problem with the device - try closing & re-opening
                ScanClose ();
                // if we can't reopen the scanner then abort
                if (!ScanOpen())
                   bAbort = TRUE;
             }
             break;
         }
      case (WAIT_OBJECT_0+1) :       // abort/exit event woke us up
         bAbort = TRUE ;
         continue;
      case (WAIT_OBJECT_0+2) :       // deactivate scanner - do not allow reads until enabled
         ResetEvent( hDeactivateScanner );
         bIsScannerActive = FALSE ;
         ResetEvent( hScannerEvent );
         break;
      case (WAIT_OBJECT_0+3) :       // activate scanner i.e. allow reads
         ResetEvent( hActivateScanner );
         bIsScannerActive = TRUE ;
         break;
      case (WAIT_OBJECT_0+4) :
         // power on event woke us up so wait to give time for the scanner & RF card to power up,
         // but still abort if abort event
         {
            const DWORD dwDelay = 7000;
            DWORD StartTime = GetTickCount();

            ScanCancelReads();

            do
            {
               DWORD dwResult;
               DWORD dwT = dwDelay - (GetTickCount()-StartTime) ;
               if ( dwT <= 0 || dwT > dwDelay )
                  break ;
               dwResult = WaitForMultipleObjects( eventCount, h, FALSE, dwT );
               switch ( dwResult )
               {
                  case WAIT_OBJECT_0 :           // scanner woke us up - ignore the read
                     ResetEvent( hScannerEvent );
                     break;
                  case (WAIT_OBJECT_0+1) :           // abort event
                     bAbort = TRUE;
                     StartTime = 0;
                     break;
                  case (WAIT_OBJECT_0+2) :           // deactivate scanner
                     ResetEvent( hDeactivateScanner );
                     ResetEvent( hScannerEvent );
                     bIsScannerActive = FALSE;
                     break;
                  case (WAIT_OBJECT_0+3) :           // activate scanner
                     ResetEvent( hActivateScanner );
                     if ( hScannerEvent )
                        ResetEvent( hScannerEvent );
                     bIsScannerActive = TRUE;
                     break;
                  case (WAIT_OBJECT_0+4) :       // power on event not reset so yield
                     Sleep(100);
                     break;
                  default :
                     break;
               }
            } while ( (GetTickCount()-StartTime) < dwDelay );
            break;
         }
         default :
            break;
      }
   }

   ScanClose();
   if ( hEventPowerUp )
      CloseHandle( hEventPowerUp );
   CloseHandle( hActivateScanner  );
   CloseHandle( hDeactivateScanner );
   CloseHandle( hScannerEvent );
   CloseHandle( hAbortEvent );
   return 0;
}

//----------------------------------------------------------------------------
// Return a handle to the power-up event, or null if the event cannot be
// found.  The power-up event must have been created by another application.
//----------------------------------------------------------------------------
static HANDLE CreatePowerUpEvent()
{
   HKEY hKey;
   TCHAR szEventName[ 256 ] = POWER_ON_EVENT; // default event name
   DWORD dwBufLen = 256;
   LONG rc=0;
   HANDLE hEventPowerUp;

   // look in the registry for the event name
   rc = RegOpenKeyEx( HKEY_CURRENT_USER, TEXT("Software\\SuperWaba\\Scanner"), 0, KEY_ALL_ACCESS, &hKey);
   if (rc == ERROR_SUCCESS )
   {
      DWORD dwType = REG_SZ;
      RegQueryValueEx( hKey, TEXT("pwrEvent"), 0, &dwType, (LPBYTE)szEventName, &dwBufLen );
       RegCloseKey( hKey );
   }

   hEventPowerUp = CreateEvent( NULL, TRUE, FALSE, szEventName );
   // If we the power-up Event doesn't already exist then we can't detect power on events
   if ( GetLastError() != ERROR_ALREADY_EXISTS )
   {
      CloseHandle( hEventPowerUp );
      hEventPowerUp = NULL;
   }
   return hEventPowerUp;
}

//----------------------------------------------------------------------------
// Open & enable the scanner, and create the scan buffer.
// Return TRUE if ok, FALSE otherwise
//----------------------------------------------------------------------------
static BOOL ScanOpen()
{
   DWORD dwResult;

   dwResult = SCAN_Open(TEXT("SCN1:"), &hScanner);
   if (dwResult != E_SCN_SUCCESS)
   {
      hScanner = NULL;
      PostErrorMessage(TEXT("SCAN_Open"), dwResult);
        return FALSE;
   }

   dwResult = SCAN_Enable(hScanner) ;
   if (dwResult != E_SCN_SUCCESS)
   {
      PostErrorMessage(TEXT("SCAN_Enable"), dwResult);
      SCAN_Close(hScanner);
      hScanner = NULL;
        return FALSE;
   }

   lpScanBuffer = SCAN_AllocateBuffer(TRUE, BUFFER_SIZE);
   if (lpScanBuffer == NULL)
   {
      PostErrorMessage(TEXT("SCAN_AllocateBuffer"), GetLastError());
      ScanClose();
      return FALSE;
   }

   if (!ScanSetup())
   {
      ScanClose();
      return FALSE;
   }

   return TRUE;
}

//----------------------------------------------------------------------------
// Set default parameters for the Scanner.
// Return TRUE if ok, FALSE otherwise
//----------------------------------------------------------------------------
static BOOL ScanSetup()
{
   DWORD dwResult;
   //
   //  Set the scanner parameters
   //
   {
      SCAN_PARAMS scan_params;
      memset(&scan_params,0, sizeof(scan_params));
      SI_ALLOC_ALL(&scan_params);
      scan_params.StructInfo.dwUsed = 0;
      dwResult = SCAN_GetScanParameters(hScanner,&scan_params);
      if ( dwResult != E_SCN_SUCCESS ) {
         PostErrorMessage(TEXT("SCAN_GetScanParameters"), dwResult);
         return FALSE;
      }

      scan_params.dwCodeIdType = CODE_ID_TYPE_NONE ;
      scan_params.dwScanType = SCAN_TYPE_FOREGROUND;
      scan_params.bLocalFeedback = TRUE;
      scan_params.dwDecodeBeepTime = 500;
      scan_params.dwDecodeBeepFrequency = 3000;
      scan_params.dwDecodeLedTime = 1000;
      memset( scan_params.szWaveFile, 0, sizeof(scan_params.szWaveFile) );
      dwResult = SCAN_SetScanParameters( hScanner, &scan_params ) ;
      if ( dwResult != E_SCN_SUCCESS ) {
         PostErrorMessage(TEXT("SCAN_SetScanParameters"), dwResult);
         return FALSE;
      }
   }
   //
   //  Set The reader paramers
   //
   {
      READER_PARAMS reader_params;
      memset(&reader_params,0, sizeof(reader_params));
      SI_ALLOC_ALL(&reader_params);
      reader_params.StructInfo.dwUsed = 0;
      dwResult = SCAN_GetReaderParams(hScanner,&reader_params);
      if ( dwResult != E_SCN_SUCCESS )
      {
         PostErrorMessage(TEXT("SCAN_GetReaderParams"), dwResult);
         return FALSE;
      }

      reader_params.dwReaderType = READER_TYPE_LASER ;
      reader_params.ReaderSpecific.laser_specific.dwAimType = AIM_TYPE_TRIGGER ;
      reader_params.ReaderSpecific.laser_specific.dwAimDuration = 0;
      reader_params.ReaderSpecific.laser_specific.dwAimMode = AIM_MODE_SLAB;
      reader_params.ReaderSpecific.laser_specific.bNarrowBeam = FALSE;
      reader_params.ReaderSpecific.laser_specific.dwRasterMode = RASTER_MODE_SMART;
      reader_params.ReaderSpecific.laser_specific.dwBeamTimer = 10000;
      reader_params.ReaderSpecific.laser_specific.bControlScanLed = TRUE;

      reader_params.ReaderSpecific.laser_specific.bScanLedLogicLevel = FALSE;
      reader_params.ReaderSpecific.laser_specific.bKlasseEinsEnable = TRUE;
      reader_params.ReaderSpecific.laser_specific.bBidirRedundancy = FALSE;

      reader_params.ReaderSpecific.laser_specific.dwLinearSecurityLevel = SECURITY_ALL_THRICE;
      reader_params.ReaderSpecific.laser_specific.dwPointerTimer = 10000;
      dwResult = SCAN_SetReaderParams(hScanner,&reader_params);
      if ( dwResult != E_SCN_SUCCESS )
      {
         //PostErrorMessage(TEXT("SCAN_SetReaderParams"), dwResult);
         return TRUE; // not all scanners need this
      }
   }
   return TRUE;
}

//----------------------------------------------------------------------------
// Cancel any outstanding reads on the scanner
//----------------------------------------------------------------------------
static void ScanCancelReads()
{
   if (lpScanBuffer != NULL)
   {
      if ( SCNBUF_GETSTAT(lpScanBuffer) == E_SCN_READPENDING )     // if an outstanding read then cancel read
      {
         //dwResult = SCAN_CancelRead( hScanner, SCNBUF_GETREQID(lpScanBuffer) );
         DWORD dwResult = SCAN_Flush( hScanner );
         if (dwResult == E_SCN_SUCCESS)
         {
            // the read may or may not have cancelled before flush returns, so
            // make sure the read is actually cancelled before continuing
            while ( SCNBUF_GETSTAT(lpScanBuffer) == E_SCN_READPENDING )
            {
               Sleep(100);
            }
         }
      }
   }
}

//----------------------------------------------------------------------------
// Close the Scanner, and cancel any reads pending
//----------------------------------------------------------------------------
static void ScanClose()
{
   if (hScanner)
   {
      ScanCancelReads();
      SCAN_Disable(hScanner);
      if (lpScanBuffer)
      {
         SCAN_DeallocateBuffer( lpScanBuffer );
         lpScanBuffer = NULL;
      }
      SCAN_Close(hScanner);
      hScanner = NULL;
   }
}

//----------------------------------------------------------------------------
// Send an error message to the main window - due to fatal error
//
// return true if success, false otherwise
//----------------------------------------------------------------------------
static BOOL PostErrorMessage(LPTSTR szInMsg, DWORD dwError )
{
   LPTSTR szMsg;
   bool ret;

   // take a copy of the message - main thread must delete the memory
   szMsg = (TCHARP) xmalloc((tcslen(szInMsg) + 64) * sizeof(TCHAR));
   if (!szMsg)
      return FALSE;

   wsprintf(szMsg, TEXT("%S (error code %x)"), szInMsg, dwError);
   // send the message to the main window - if we can't post the message then
   // make sure we free memory.
   ret = PostMessage(getMainWindowHandle(), UM_SCAN_ERROR, 0, (LPARAM)szMsg) != 0; // Zero means failure.
   xfree(szMsg);

   return ret;
}


//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   if (hActivateScanner == null)   // if scanner failed to open
      p->retI = false;
   else
   {
#ifdef MOTOROLA
      createScanner();
#endif
      SetEvent(hActivateScanner);
      p->retI = true;
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{
   int32 barcodeType = p->i32[0];
   int32 enable = p->i32[1];
   DECODER Decoder;
   DWORD dwResult;
   int32 ret = 0;

   if ((dwResult = TranslateDecoder(barcodeType, (LPDECODER) Decoder)))
   {
      dwResult = ScanToggleDecoder(Decoder, enable);
      ret = (dwResult == E_SCN_SUCCESS);
   }
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   int32 type = p->i32[0];
   int32 barcodetype = p->i32[1];
   int32 value = p->i32[2];

   if (type == -999) // guich@580_22
   {
      scannerWaitingTime = value;
      if (scannerWaitingTime <= 0)
         scannerWaitingTime = INFINITE;
      p->retI = true;
   }
   else p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   // values which are passed by the Scanner class
   #define ANY_LENGTH                                  0x00
   #define ONE_DISCRETE_LENGTH                         0x01
   #define TWO_DISCRETE_LENGTHS                        0x02
   #define LENGTH_WITHIN_RANGE                         0x03

   int32 barcodeType = p->i32[0];
   int32 lengthType = p->i32[1];
   int32 min = p->i32[2];
   int32 max = p->i32[3];
   int32 ret = 0;


   DECODER Decoder;
   DWORD dwMinLength = 0, dwMaxLength = 0, dwResult = 0;

   // Allowable decode lengths are specified by dwMinLength and dwMaxLength as follows:

   // Variable length - dwMinLength = dwMaxLength = 0
   // Range (from a to b, including a and b) - dwMinLength = a, dwMaxLength = b
   // Two Discrete Lengths (either a or b, given a<b) - dwMinLength = b, dwMaxLength = a
   // One Discrete Length (only a) - dwMinLength = dwMaxLength = a

   switch (lengthType)
   {
      case ANY_LENGTH:
         dwMinLength = dwMaxLength = 0;
         break;
      case ONE_DISCRETE_LENGTH:
         dwMinLength = dwMaxLength = min;
         break;
      case TWO_DISCRETE_LENGTHS:
      case LENGTH_WITHIN_RANGE:
         dwMinLength = min;
         dwMaxLength = max;
         break;
   }
   if (dwMaxLength < dwMinLength) // guich@580_20 - swap
   {
      DWORD temp  = dwMinLength;
      dwMinLength = dwMaxLength;
      dwMaxLength = temp;
   }

   if (!(dwResult = TranslateDecoder(barcodeType, (LPDECODER) Decoder)))
   {
      // error, do something about it!
   }
   else
   {
      // Return the result of setting parameters
      dwResult = ScanSetDecoderLengths(Decoder, dwMinLength, dwMaxLength);
      ret = (dwResult == E_SCN_SUCCESS);
   }

   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   TCHAR *barcode;
   DWORD dwResult;

   if ((dwResult = SCNBUF_GETSTAT(lpScanBuffer)) != E_SCN_SUCCESS)
   {
      // error, do something about it!
   }
   else
   {
      barcode = (TCHAR*) SCNBUF_GETDATA(lpScanBuffer);
      p->retO = createStringObjectFromTCHAR(p->currentContext, barcode, SCNBUF_GETLEN(lpScanBuffer));
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   SCAN_VERSION_INFO scanVersionInfo;
   DWORD dwResult;
   char buf[20];

   SI_INIT(&scanVersionInfo);
   xmemzero(buf,sizeof(buf));

   if ((dwResult = SCAN_GetVersionInfo(hScanner, &scanVersionInfo)) != E_SCN_SUCCESS)
   {
      // error, do something about it!
   }
   else
   {
      int2hex((int32) scanVersionInfo.dwCAPIVersion, 8, buf);
      p->retO = createStringObjectFromCharP(p->currentContext, buf, -1);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   SCAN_VERSION_INFO scanVersionInfo;
   DWORD dwResult;
   char buf[20];

   SI_INIT(&scanVersionInfo);
   xmemzero(buf, sizeof(buf));

   if ((dwResult = SCAN_GetVersionInfo(hScanner, &scanVersionInfo)) != E_SCN_SUCCESS)
   {
      // error, do something about it!
   }
   else
   {
      int2hex((int32) scanVersionInfo.dwPddVersion, 8, buf);
      p->retO = createStringObjectFromCharP(p->currentContext, buf, -1);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   if (hActivateScanner == null)   // if scanner failed to open
      p->retI = false;
   else
   {
      SetEvent(hDeactivateScanner);
      p->retI = true;
#ifdef MOTOROLA
      destroyScanner();
#endif
   }
}
//////////////////////////////////////////////////////////////////////////
SCAN_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{
}