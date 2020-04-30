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

typedef DWORD SMS_HANDLE;
typedef DWORD SMS_MESSAGE_ID;

#define SMS_MSGTYPE_TEXT TEXT("Microsoft Text SMS Protocol")

// dwMessageModes for SmsOpen
#define SMS_MODE_RECEIVE              (0x00000001)
#define SMS_MODE_SEND                 (0x00000002)

typedef enum  
{
   SMSAT_UNKNOWN=0,
   SMSAT_INTERNATIONAL,
   SMSAT_NATIONAL,
   SMSAT_NETWORKSPECIFIC,
   SMSAT_SUBSCRIBER,
   SMSAT_ALPHANUMERIC,
   SMSAT_ABBREVIATED
} SMS_ADDRESS_TYPE;
typedef enum 
{
   SMSDE_OPTIMAL=0,
   SMSDE_GSM,
   SMSDE_UCS2,
} SMS_DATA_ENCODING;
typedef enum  
{
   PS_MESSAGE_CLASS0 = 0,
   PS_MESSAGE_CLASS1,
   PS_MESSAGE_CLASS2,
   PS_MESSAGE_CLASS3,
   PS_MESSAGE_CLASSUNSPECIFIED,
} PROVIDER_SPECIFIC_MESSAGE_CLASS;
typedef enum  
{
   PSRO_NONE = 0,
   PSRO_REPLACE_TYPE1,
   PSRO_REPLACE_TYPE2,
   PSRO_REPLACE_TYPE3,
   PSRO_REPLACE_TYPE4,
   PSRO_REPLACE_TYPE5,
   PSRO_REPLACE_TYPE6,
   PSRO_REPLACE_TYPE7,
   PSRO_RETURN_CALL,
   PSRO_DEPERSONALIZATION,
} PROVIDER_SPECIFIC_REPLACE_OPTION;

typedef enum  
{
   TEXTPSPRI_NONE = 0,    
   TEXTPSPRI_NORMAL = 1,
   TEXTPSPRI_INTERACTIVE,
   TEXTPSPRI_URGENT,
   TEXTPSPRI_EMERGENCY
} TEXT_PROVIDER_SPECIFIC_PRIORITY_TYPE;

#define SMS_MAX_ADDRESS_LENGTH       (256)
#define SMS_DATAGRAM_SIZE            (140)

// SMS addressing information
typedef struct sms_address_tag {
    SMS_ADDRESS_TYPE smsatAddressType;
    TCHAR ptsAddress[SMS_MAX_ADDRESS_LENGTH];
} SMS_ADDRESS, *LPSMS_ADDRESS;

typedef HRESULT (__stdcall *SmsOpenProc)
(
   const LPCTSTR ptsMessageProtocol,
   const DWORD dwMessageModes,
   SMS_HANDLE* const psmshHandle,
   HANDLE* const phMessageAvailableEvent
);

typedef HRESULT (__stdcall *SmsCloseProc)
(
  const SMS_HANDLE smshHandle
);

typedef HRESULT (__stdcall *SmsSendMessageProc)
(
   const SMS_HANDLE smshHandle,
   const SMS_ADDRESS * const psmsaSMSCAddress,
   const SMS_ADDRESS * const psmsaDestinationAddress,
   const SYSTEMTIME * const pstValidityPeriod,
   const BYTE * const pbData,
   const DWORD dwDataSize,
   const BYTE * const pbProviderSpecificData,
   const DWORD dwProviderSpecificDataSize,
   const SMS_DATA_ENCODING smsdeDataEncoding,
   const DWORD dwOptions,
   SMS_MESSAGE_ID * psmsmidMessageID
);

typedef HRESULT (__stdcall *SmsGetMessageSizeProc) 
(
   const SMS_HANDLE smshHandle,
   DWORD* const pdwDataSize
);

// Read an SMS message (the appropriate size of the buffer can be found via a call to SmsGetMessageSize)
typedef HRESULT (__stdcall *SmsReadMessageProc) 
(
   const SMS_HANDLE smshHandle,
   SMS_ADDRESS * const psmsaSMSCAddress,
   SMS_ADDRESS * const psmsaSourceAddress,
   SYSTEMTIME * const pstReceiveTime,
   BYTE * const pbBuffer, DWORD dwBufferSize,
   BYTE * const pbProviderSpecificBuffer,
   DWORD dwProviderSpecificDataBuffer,
   DWORD* pdwBytesRead
);

typedef struct text_provider_specific_data_tag 
{
   DWORD dwMessageOptions;
   PROVIDER_SPECIFIC_MESSAGE_CLASS psMessageClass;
   PROVIDER_SPECIFIC_REPLACE_OPTION psReplaceOption;
   DWORD dwHeaderDataSize;
   BYTE pbHeaderData[SMS_DATAGRAM_SIZE];   // For concatenated messages, only the header from the first segment is returned.
   BOOL fMessageContainsEMSHeaders;        // At least one segment of this message contains EMS headers.  
                                           // Only set if EMS handler installed.
   DWORD dwProtocolID;                     // PID of incoming message, or desired PID of outgoing message.  
                                           // Applies only to GSM.  Set to SMS_MSGPROTOCOL_UNKNOWN if psReplaceOption 
                                           // is not PSRO_NONE.
   DWORD dwExtParams;                                // Bitfield of valid additional structure parameters (all structure
                                                     // values above are considered always valid).
   TEXT_PROVIDER_SPECIFIC_PRIORITY_TYPE tpsPriority; // Applies only to CDMA IS637. Priority indicator.
   SMS_ADDRESS smsaCallback;                         // Applies only to CDMA IS637. Callback number
} TEXT_PROVIDER_SPECIFIC_DATA;

#define PS_MESSAGE_OPTION_NONE          (0x00000000)
#define PS_MESSAGE_OPTION_REPLYPATH     (0x00000001)
#define PS_MESSAGE_OPTION_STATUSREPORT  (0x00000002)
#define PS_MESSAGE_OPTION_DISCARD       (0x00000004)

#define SMS_OPTION_DELIVERY_NONE      (0x00000000)
#define SMS_OPTION_DELIVERY_NO_RETRY  (0x00000001)

static SMS_HANDLE smsHandle;
static HANDLE smsEvent;
static HINSTANCE smsDll;
static SmsOpenProc SmsOpen;
static SmsCloseProc SmsClose;
static SmsSendMessageProc SmsSendMessage;
static SmsGetMessageSizeProc SmsGetMessageSize;
static SmsReadMessageProc SmsReadMessage;

static bool initSMS(Context currentContext, const DWORD mode)
{
   smsDll = LoadLibrary(TEXT("sms.dll"));
   if (!smsDll)
      throwException(currentContext, RuntimeException, "Could not load the SMS library");
   else
   {
      SmsOpen = (SmsOpenProc)GetProcAddress(smsDll, TEXT("SmsOpen"));
      SmsSendMessage = (SmsSendMessageProc)GetProcAddress(smsDll, TEXT("SmsSendMessage"));
      SmsClose = (SmsCloseProc)GetProcAddress(smsDll, TEXT("SmsClose"));
      SmsGetMessageSize = (SmsGetMessageSizeProc)GetProcAddress(smsDll, TEXT("SmsGetMessageSize"));
      SmsReadMessage = (SmsReadMessageProc)GetProcAddress(smsDll, TEXT("SmsReadMessage"));

      if (!SmsOpen || !SmsSendMessage || !SmsClose || !SmsReadMessage || !SmsGetMessageSize)
         throwException(currentContext, IOException, "Could not load the required procedures from the SMS library");
      else
      {
         Err err = SmsOpen(SMS_MSGTYPE_TEXT, mode, &smsHandle, &smsEvent);
         if (err != NO_ERROR)
            throwExceptionWithCode(currentContext, IOException, err);
      }
   }
   return currentContext->thrownException == null;
}

static void SmsSend(Context currentContext, TCHARP szMessage, TCHARP szDestination)
{
   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300 && initSMS(currentContext,SMS_MODE_SEND))
   {
      Err err;
      // wait for SMS Event to become signaled
      DWORD dwReturn = WaitForSingleObject( smsEvent,  INFINITE ); // never fails

      if (dwReturn != WAIT_ABANDONED && dwReturn != WAIT_TIMEOUT)
      {
         TEXT_PROVIDER_SPECIFIC_DATA tpsd;
         SMS_MESSAGE_ID smsmidMessageID;
         SMS_ADDRESS destination;
         // Set up provider specific data
         memset(&tpsd, 0, sizeof(TEXT_PROVIDER_SPECIFIC_DATA));
         tpsd.dwMessageOptions = PS_MESSAGE_OPTION_NONE;
         tpsd.psMessageClass = PS_MESSAGE_CLASS1; // I also tried PS_MESSAGE_CLASS1
         tpsd.psReplaceOption = PSRO_NONE;
         lstrcpy(destination.ptsAddress, szDestination);
         destination.smsatAddressType = SMSAT_INTERNATIONAL;
         // send the message
         err = SmsSendMessage(smsHandle,NULL,&destination,NULL,(PBYTE)szMessage, lstrlen(szMessage)*2, (PBYTE) &tpsd, sizeof(TEXT_PROVIDER_SPECIFIC_DATA), SMSDE_OPTIMAL, SMS_OPTION_DELIVERY_NONE, &smsmidMessageID );
         if (err != NO_ERROR)
            throwExceptionWithCode(currentContext, IOException, err);
      }
      else throwException(currentContext, IOException, "SMS event was not activated.");
      err = SmsClose(smsHandle);
      if (err != NO_ERROR)
         throwExceptionWithCode(currentContext, IOException, err);
      FreeLibrary(smsDll);
   }
}

static void SmsReceive(Context currentContext, TCObject* out)
{
   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300 && initSMS(currentContext,SMS_MODE_RECEIVE))
   {
      Err err;
      SMS_ADDRESS smsaDestination;
      TEXT_PROVIDER_SPECIFIC_DATA tpsd;

      // Wait for message to come in.
      DWORD dwReturn = WaitForSingleObject (smsEvent, INFINITE);
      if (dwReturn != WAIT_ABANDONED && dwReturn != WAIT_TIMEOUT)
      {
         DWORD dwSize, dwRead = 0;
         memset (&smsaDestination, 0, sizeof(smsaDestination));

         err = SmsGetMessageSize(smsHandle, &dwSize);
         if (err != NO_ERROR) 
            throwExceptionWithCode(currentContext, IOException, err);
         else
         {
            TCObject msg = createStringObjectWithLen(currentContext, dwSize+1);
            if (msg != null)
            {
               char* pMessage = xmalloc(dwSize+1);
               if (pMessage == null)
                  throwException(currentContext, OutOfMemoryError, "To allocate the SMS message");
               else
               {
                  memset (&tpsd, 0, sizeof (tpsd));
                  err = SmsReadMessage(smsHandle, NULL, &smsaDestination, NULL, (PBYTE)pMessage, dwSize, (PBYTE)&tpsd, sizeof(TEXT_PROVIDER_SPECIFIC_DATA), &dwRead);
                  if (err != NO_ERROR) 
                     throwExceptionWithCode(currentContext, IOException, err);
                  else
                  {
                     *out = createArrayObject(currentContext, "[java.lang.String", 2);
                     if (*out != null)
                     {
                        TCObject* nm = (TCObject*)ARRAYOBJ_START(*out);
                        nm[0] = createStringObjectFromTCHARP(currentContext, smsaDestination.ptsAddress, -1);
                        nm[1] = createStringObjectFromCharP(currentContext, pMessage, dwSize);
                        setObjectLock(nm[0], UNLOCKED);
                        setObjectLock(nm[1], UNLOCKED);
                        setObjectLock(*out, UNLOCKED);
                     }
                  } 
                  xfree(pMessage);
               }
            }
         }
      }
      err = SmsClose(smsHandle);
      if (err != NO_ERROR)
         throwExceptionWithCode(currentContext, IOException, err);
      FreeLibrary(smsDll);
   }
}

