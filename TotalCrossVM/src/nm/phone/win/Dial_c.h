// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <tapi.h>

#define TAPI_VERSION_1_0      0x00010003
#define TAPI_VERSION_2_0      0x00020000

static HLINEAPP hLineApp;      // Application's use handle for TAPI(lineInitialize)
static HCALL hCall;            // Handle to the open line device on which the call is to be originated(lineMakeCall)
static LONG MakeCallRequestID;        // Request identifier returned by lineMakeCall

static DWORD dwNumDevs = 0;          // Number of line devices available
static DWORD dwCurrentLineID = -1;   // Current line device identifier

typedef struct
{
  HLINE hLine;              // Line handle returned by lineOpen
  DWORD dwAPIVersion;       // API version that the line supports
} LINEINFO, *LPLINEINFO;

static LINEINFO VoiceLineInfo;     // Contains the current line information

// This function closes the opened line device.
static void CurrentLineClose()
{
   LONG DropCallRequestID;
   uint32 ret;
   // If lineMakeCall succeeded, then drop the call.
   if (hCall)
   {
      ret = DropCallRequestID = lineDrop(hCall, NULL, 0);
      Sleep(3000);
      //alert("ret1: %X",ret);
      ret = lineDeallocateCall(hCall); // Deallocate call handle.
      //alert("ret2: %X",ret);
   }

   // Close the current line.
   if (VoiceLineInfo.hLine)
   {
      ret = lineClose(VoiceLineInfo.hLine);
      //alert("ret3: %X",ret);
   }
   ret=ret;

   // Reinitialize the variables.
   VoiceLineInfo.hLine = null;
   hCall = null;
}

typedef struct linedevcaps_tag_v3 // we're targetting windows mobile 5 and beyond
{
    DWORD       dwTotalSize;
    DWORD       dwNeededSize;
    DWORD       dwUsedSize;
    DWORD       dwProviderInfoSize;
    DWORD       dwProviderInfoOffset;
    DWORD       dwSwitchInfoSize;
    DWORD       dwSwitchInfoOffset;
    DWORD       dwPermanentLineID;
    DWORD       dwLineNameSize;
    DWORD       dwLineNameOffset;
    DWORD       dwStringFormat;
    DWORD       dwAddressModes;
    DWORD       dwNumAddresses;
    DWORD       dwBearerModes;
    DWORD       dwMaxRate;
    DWORD       dwMediaModes;
    DWORD       dwGenerateToneModes;
    DWORD       dwGenerateToneMaxNumFreq;
    DWORD       dwGenerateDigitModes;
    DWORD       dwMonitorToneMaxNumFreq;
    DWORD       dwMonitorToneMaxNumEntries;
    DWORD       dwMonitorDigitModes;
    DWORD       dwGatherDigitsMinTimeout;
    DWORD       dwGatherDigitsMaxTimeout;
    DWORD       dwMedCtlDigitMaxListSize;
    DWORD       dwMedCtlMediaMaxListSize;
    DWORD       dwMedCtlToneMaxListSize;
    DWORD       dwMedCtlCallStateMaxListSize;
    DWORD       dwDevCapFlags;
    DWORD       dwMaxNumActiveCalls;
    DWORD       dwAnswerMode;
    DWORD       dwRingModes;
    DWORD       dwLineStates;
    DWORD       dwUUIAcceptSize;
    DWORD       dwUUIAnswerSize;
    DWORD       dwUUIMakeCallSize;
    DWORD       dwUUIDropSize;
    DWORD       dwUUISendUserUserInfoSize;
    DWORD       dwUUICallInfoSize;
    LINEDIALPARAMS  MinDialParams;
    LINEDIALPARAMS  MaxDialParams;
    LINEDIALPARAMS  DefaultDialParams;
    DWORD       dwNumTerminals;
    DWORD       dwTerminalCapsSize;
    DWORD       dwTerminalCapsOffset;
    DWORD       dwTerminalTextEntrySize;
    DWORD       dwTerminalTextSize;
    DWORD       dwTerminalTextOffset;
    DWORD       dwDevSpecificSize;
    DWORD       dwDevSpecificOffset;                            // TAPI v1.0 til here

    DWORD       dwLineFeatures;                                 // TAPI v1.4
    DWORD       dwSettableDevStatus;                            // TAPI v2.0
    DWORD       dwDeviceClassesSize;                            // TAPI v2.0
    DWORD       dwDeviceClassesOffset;                          // TAPI v2.0
    GUID        PermanentLineGuid;                              // TAPI v2.2
    DWORD       dwAddressTypes;                                 // TAPI v3.0
    GUID        ProtocolGuid;                                   // TAPI v3.0
    DWORD       dwAvailableTracking;                            // TAPI v3.0
} LINEDEVCAPS_v3, FAR *LPLINEDEVCAPS_v3;

// This is a callback function invoked to determine status and events on the line device, addresses, or calls.
static void CALLBACK lineCallbackFunc(DWORD hDevice, DWORD dwMsg, DWORD dwCallbackInstance, DWORD dwParam1, DWORD dwParam2, DWORD dwParam3)
{
#ifdef WINCE
   CharP status = null;

   switch (dwMsg)
   {
      case LINE_CALLSTATE:  // Sent after change of call state
         // dwParam1 is the specific CALLSTATE change that is occurring.
         switch (dwParam1)
         {
            case LINECALLSTATE_DIALTONE:    status = "Dial tone";                                                     break;
            case LINECALLSTATE_DIALING:     status = "Dialing...";                                                    break;
            case LINECALLSTATE_PROCEEDING:  status = "Dialing completed, call proceeding";                            break;
            case LINECALLSTATE_RINGBACK:    status = "Ring back";                                                     break;
            case LINECALLSTATE_CONNECTED:   status = "Connected";                                                     break;
            case LINECALLSTATE_BUSY:        status = "Line busy"; CurrentLineClose();                                 break;
            case LINECALLSTATE_IDLE:        status = "Line is idle";                                                  break;
            case LINECALLSTATE_SPECIALINFO: status = "Special Information, couldn't dial number"; CurrentLineClose(); break;
            case LINECALLSTATE_DISCONNECTED:
            {
               switch (dwParam2)
               {
                  case LINEDISCONNECTMODE_NORMAL:       status = "Disconnected by remote party";        break;
                  case LINEDISCONNECTMODE_REJECT:       status = "Remote Party rejected call";          break;
                  case LINEDISCONNECTMODE_PICKUP:       status = "Disconnected: Local phone picked up"; break;
                  case LINEDISCONNECTMODE_FORWARDED:    status = "Disconnected: Forwarded";             break;
                  case LINEDISCONNECTMODE_BUSY:         status = "Disconnected: Busy";                  break;
                  case LINEDISCONNECTMODE_NOANSWER:     status = "Disconnected: No Answer";             break;
                  case LINEDISCONNECTMODE_BADADDRESS:   status = "Disconnected: Bad address";           break;
                  case LINEDISCONNECTMODE_UNREACHABLE:  status = "Disconnected: Unreachable";           break;
                  case LINEDISCONNECTMODE_CONGESTION:   status = "Disconnected: Congestion";            break;
                  case LINEDISCONNECTMODE_INCOMPATIBLE: status = "Disconnected: Incompatible";          break;
                  case LINEDISCONNECTMODE_UNAVAIL:      status = "Disconnected: Unavailable";           break;
                  case LINEDISCONNECTMODE_NODIALTONE:   status = "Disconnected: No dial tone";          break;
                  default:                              status = "Disconnected";                        break;
               }
               CurrentLineClose();
               break;
            }
         }
         break;
      case LINE_LINEDEVSTATE:
         switch (dwParam1)
         {
            case LINEDEVSTATE_RINGING:      status = "Ringing";                                                      break;
            case LINEDEVSTATE_OUTOFSERVICE: status = "The line selected is out of service";      CurrentLineClose(); break;
            case LINEDEVSTATE_DISCONNECTED: status = "The line selected is disconnected";        CurrentLineClose(); break;
            case LINEDEVSTATE_MAINTENANCE:  status = "The line selected is out for maintenance"; CurrentLineClose(); break;
            case LINEDEVSTATE_REMOVED:      status = "The Line device has been removed; no action taken";            break;
            case LINEDEVSTATE_REINIT:
            {
                // This usually means that a service provider has changed in such a way that requires TAPI to REINIT. Note that there
                // are both soft REINITs and hard REINITs. Soft REINITs do not require a full shutdown but an informational change that
                // historically required a REINIT to force the application to deal with.  TAPI API Version 1.3 applications require a
                // full REINIT for both hard and soft REINITs.
                switch(dwParam2)
                {
                   // This is the hard REINIT. TAPI is waiting for everyone to shut down. Our response is
                   // to immediately shut down any  calls, shut down our use of TAPI and notify the user.
                   case 0: throwDialException("Tapi line configuration has been changed.",0); lineShutdown(hLineApp); hLineApp = null; break;
                   case LINE_CREATE:
                   case LINE_LINEDEVSTATE: lineCallbackFunc(hDevice, dwParam2, dwCallbackInstance, dwParam3, 0, 0); break;
                }
            }
         }
         break;
      case LINE_REPLY:
         // Reply from the lineMakeCall function.
         if ((LONG)dwParam1 == MakeCallRequestID)
         {
            // If an error occurred on making the call.
            if (dwParam2 != 0)
            {
               if (dwParam2 == LINEERR_CALLUNAVAIL)
                  status = "The line is not available.";
               else
                  status = "Closing line";
               CurrentLineClose();
            }
         }
         break;
      case LINE_CREATE:
         // dwParam1 is the device identifier of the new line.
/*         if (dwParam1 >= dwNumDevs)
         {
            DWORD dwLineID;
            LINEINFO *lpLineInfo;

            dwNumDevs = dwParam1 + 1;

            // Allocate a buffer for storing LINEINFO for all the lines.
            if (!(lpLineInfo =(LPLINEINFO) LocalAlloc(LPTR, sizeof(LINEINFO) * dwNumDevs)))
               break;

            // Assume we just add a new line, the lines are sequential and
            // the new line is the last one.
            for (dwLineID = 0; dwLineID < dwParam1; ++dwLineID)
               lpLineInfo[dwLineID] = lpLineInfo[dwLineID];

            // Get the new line information.
            Get LineInfo(dwParam1, &lpLineInfo[dwParam1]);

            LocalFree(lpLineInfo);
            lpLineInfo = lpLineInfo;
         }*/
         break;
      case LINE_CLOSE:
         if (VoiceLineInfo.hLine ==(HLINE) hDevice)
         {
            status = "Closing line";
            CurrentLineClose();
         }
         break;
/*      case LINE_ADDRESSSTATE:
      case LINE_CALLINFO:
      case LINE_DEVSPECIFIC:
      case LINE_DEVSPECIFICFEATURE:
      case LINE_GATHERDIGITS:
      case LINE_GENERATE:
      case LINE_MONITORDIGITS:
      case LINE_MONITORMEDIA:
      case LINE_MONITORTONE:
      case LINE_REMOVE:
      case LINE_REQUEST:
         break;*/
   }
   if (status != null)
      statusChange(status);
#endif
}

// Initialize the application's use of Tapi.dll.
static bool InitializeTAPI()
{
#ifdef WINCE
   DWORD dwLineID, dwReturn, dwTimeCount = GetTickCount();
   bool found = false;

   // Initialize the application's use of Tapi.dll. Keep trying until the
   // user cancels or stops getting LINEERR_REINIT.
   while ((dwReturn = lineInitialize(&hLineApp, hModuleTCVM, (LINECALLBACK)lineCallbackFunc, TEXT("TotalCross"), &dwNumDevs)) == LINEERR_REINIT)
   {
      Sleep(50);
      // Bring up the message box if 5 seconds have passed.
      if (GetTickCount() > 5000 + dwTimeCount)
      {
         throwDialException("Cannot initialize tapi.dll. Please quit all other phone programs and try again.", 0);
         return false;
      }
   }

   // If function "lineInitialize" fails, then return.
   if (dwReturn)
   {
      throwDialException("Error in lineInitialize: %X", dwReturn);
      return false;
   }

   // find a voice line
   for (dwLineID = 0; dwLineID < dwNumDevs; ++dwLineID)
   {
      DWORD dwReturn;
      LINEDEVCAPS_v3 LineDevCaps;

      // Negotiate the API version number. If it fails, return to dwReturn.
      // TAPI registration handle, Line device to be queried, Least recent API version, Most recent API version,
      // Negotiated API version, Negotiated API versionMust be NULL; the provider-specific extension is not supported on Windows CE
      dwReturn = lineNegotiateAPIVersion(hLineApp, dwLineID, TAPI_VERSION_1_0, TAPI_VERSION_2_0, &VoiceLineInfo.dwAPIVersion, NULL);
      if (dwReturn == 0)
      {
         tzero(LineDevCaps);
         LineDevCaps.dwTotalSize = sizeof(LINEDEVCAPS_v3);
         dwReturn = lineGetDevCaps(hLineApp, dwLineID, VoiceLineInfo.dwAPIVersion, 0, (LPLINEDEVCAPS)&LineDevCaps);
         if (dwReturn == 0 && LineDevCaps.dwMediaModes & LINEMEDIAMODE_INTERACTIVEVOICE)
         {
            dwCurrentLineID = dwLineID;
            found = true;
            break;
         }
      }
   }
   
   if (!found)
   {
      throwDialException("There are no voice lines available", 0);
      return false;
   }
   else
#endif
      return true;
}

typedef struct linecallparams_tag_v3               // Defaults:
{
    DWORD       dwTotalSize;                    // ---------
    DWORD       dwBearerMode;                   // voice
    DWORD       dwMinRate;                      // (3.1kHz)
    DWORD       dwMaxRate;                      // (3.1kHz)
    DWORD       dwMediaMode;                    // interactiveVoice
    DWORD       dwCallParamFlags;               // 0
    DWORD       dwAddressMode;                  // addressID
    DWORD       dwAddressID;                    // (any available)
    LINEDIALPARAMS  DialParams;                 // (0, 0, 0, 0)
    DWORD       dwOrigAddressSize;              // 0
    DWORD       dwOrigAddressOffset;
    DWORD       dwDisplayableAddressSize;
    DWORD       dwDisplayableAddressOffset;
    DWORD       dwCalledPartySize;              // 0
    DWORD       dwCalledPartyOffset;
    DWORD       dwCommentSize;                  // 0
    DWORD       dwCommentOffset;
    DWORD       dwUserUserInfoSize;             // 0
    DWORD       dwUserUserInfoOffset;
    DWORD       dwHighLevelCompSize;            // 0
    DWORD       dwHighLevelCompOffset;
    DWORD       dwLowLevelCompSize;             // 0
    DWORD       dwLowLevelCompOffset;
    DWORD       dwDevSpecificSize;              // 0
    DWORD       dwDevSpecificOffset;
    DWORD       dwPredictiveAutoTransferStates;                 // TAPI v2.0
    DWORD       dwTargetAddressSize;                            // TAPI v2.0
    DWORD       dwTargetAddressOffset;                          // TAPI v2.0
    DWORD       dwSendingFlowspecSize;                          // TAPI v2.0
    DWORD       dwSendingFlowspecOffset;                        // TAPI v2.0
    DWORD       dwReceivingFlowspecSize;                        // TAPI v2.0
    DWORD       dwReceivingFlowspecOffset;                      // TAPI v2.0
    DWORD       dwDeviceClassSize;                              // TAPI v2.0
    DWORD       dwDeviceClassOffset;                            // TAPI v2.0
    DWORD       dwDeviceConfigSize;                             // TAPI v2.0
    DWORD       dwDeviceConfigOffset;                           // TAPI v2.0
    DWORD       dwCallDataSize;                                 // TAPI v2.0
    DWORD       dwCallDataOffset;                               // TAPI v2.0
    DWORD       dwNoAnswerTimeout;                              // TAPI v2.0
    DWORD       dwCallingPartyIDSize;                           // TAPI v2.0
    DWORD       dwCallingPartyIDOffset;                         // TAPI v2.0
    DWORD       dwAddressType;                                  // TAPI v3.0
} LINECALLPARAMS_v3, FAR *LPLINECALLPARAMS_v3;

// Demonstrates the use of lineOpen, lineTranslateAddress, lineMakeCall.
static VOID MakePhoneCall(LPCTSTR lpszPhoneNum)
{
#ifdef WINCE
   DWORD dwReturn,
   dwSizeOfTransOut = sizeof(LINETRANSLATEOUTPUT),
   dwSizeOfCallParams = sizeof(LINECALLPARAMS_v3);

   LPLINECALLPARAMS_v3 lpCallParams = NULL;
   LPLINETRANSLATEOUTPUT lpTransOutput = NULL;

   TCHAR szDialablePhoneNum[TAPIMAXDESTADDRESSSIZE + 1] = {'\0'};
   int err = 0;

   // Initialize MakeCallRequestID.
   MakeCallRequestID = 0;

   // Open the current line.
   // Usage handle for TAPI, Cannot use the LINEMAPPER value, Line handle, Must set to zero for Windows CE,
   // No data passed back, Can only make an outgoing call, Media mode, Must set to NULL for Windows CE
   if (dwReturn = lineOpen(hLineApp, dwCurrentLineID, &VoiceLineInfo.hLine, VoiceLineInfo.dwAPIVersion, 0, 0, LINECALLPRIVILEGE_NONE, 0, NULL))
      goto exit;

   // Call translate address before dialing.
   while (true)
   {
      // Allocate memory for lpTransOutput.
      if (!(lpTransOutput =(LPLINETRANSLATEOUTPUT) LocalAlloc(LPTR,  dwSizeOfTransOut)))
         goto exit;

      lpTransOutput->dwTotalSize = dwSizeOfTransOut;
      // Usage handle for TAPI, Line device identifier, Address to be translated, Must be 0 for Windows CE, No associated operations, Result of the address translation
      if (dwReturn = lineTranslateAddress(hLineApp, dwCurrentLineID, VoiceLineInfo.dwAPIVersion, lpszPhoneNum, 0, 0, lpTransOutput))
         goto exit;

      if (lpTransOutput->dwNeededSize <= lpTransOutput->dwTotalSize)
         break;
      else
      {
         dwSizeOfTransOut = lpTransOutput->dwNeededSize;
         LocalFree(lpTransOutput);
         lpTransOutput = NULL;
      }
   }

   dwSizeOfCallParams += lpTransOutput->dwDisplayableStringSize;

   if (!(lpCallParams =(LPLINECALLPARAMS_v3) LocalAlloc(LPTR, dwSizeOfCallParams)))
      goto exit;

   xmemzero(lpCallParams, dwSizeOfCallParams);

   // Set the call parameters.
   lpCallParams->dwTotalSize      = dwSizeOfCallParams;
   lpCallParams->dwBearerMode     = LINEBEARERMODE_VOICE;
   lpCallParams->dwMediaMode      = LINEMEDIAMODE_INTERACTIVEVOICE;
   lpCallParams->dwCallParamFlags = LINECALLPARAMFLAGS_IDLE;
   lpCallParams->dwAddressMode    = LINEADDRESSMODE_DIALABLEADDR;
   lpCallParams->dwDisplayableAddressSize = lpTransOutput->dwDisplayableStringSize;
   lpCallParams->dwDisplayableAddressOffset = sizeof(LINECALLPARAMS_v3);

   // Save the translated phone number for dialing.
   lstrcpy(szDialablePhoneNum, (LPTSTR)((LPBYTE) lpTransOutput + lpTransOutput->dwDialableStringOffset));
   memcpy((LPBYTE) lpCallParams + lpCallParams->dwDisplayableAddressOffset, (LPBYTE) lpTransOutput + lpTransOutput->dwDisplayableStringOffset, lpTransOutput->dwDisplayableStringSize);

   // Make the phone call. lpCallParams should be NULL if the default
   // call setup parameters are requested.
   MakeCallRequestID = lineMakeCall(VoiceLineInfo.hLine, &hCall, szDialablePhoneNum, 0, (LPLINECALLPARAMS)lpCallParams);

   if (MakeCallRequestID <= 0)
   {
      throwDialException("Failed in making the call. Error: %X",MakeCallRequestID);
      CurrentLineClose();
   }
exit:

   if (lpCallParams)
      LocalFree(lpCallParams);

   if (lpTransOutput)
      LocalFree(lpTransOutput);

   // If the make call did not succeed but the line was opened,
   // then close it.
   if (MakeCallRequestID <= 0 && VoiceLineInfo.hLine)
      CurrentLineClose();
#endif
}

static void dialNumber(CharP number)
{
   TCHAR number16[100];
   CharP2TCHARPBuf(number, number16);
   
   if (InitializeTAPI())
      MakePhoneCall(number16);
}

static void hangup()
{
   CurrentLineClose();
}
