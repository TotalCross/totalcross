// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "ril.h"

static HANDLE rilHandle;
static HINSTANCE rilDll;
static RIL_InitializeProc            _RIL_Initialize;
static RIL_DeinitializeProc          _RIL_Deinitialize;
static RIL_GetRegistrationStatusProc _RIL_GetRegistrationStatus;
static RIL_GetCellTowerInfoProc      _RIL_GetCellTowerInfo;
static RIL_GetSignalQualityProc      _RIL_GetSignalQuality;

static Hashtable htCallbackResults;

typedef struct _callbackResult
{
   HRESULT hrCmdID;
   HANDLE mutex;
   VoidP* lpData;
   DWORD dwCode;
   DWORD cbData;
   DWORD dwParam;
} CallbackResult, *CallbackResultP;

DWORD RilCallbackResult(HRESULT hrCmdID, VoidP* lpData, DWORD* cbData, DWORD* dwParam);
CALLBACK RilCallback(DWORD dwCode, HRESULT hrCmdID, const void*lpData, DWORD cbdata, DWORD dwParam);


static void CellInfoLoadResources(Context currentContext)
{
   if (*tcSettings.romVersionPtr >= 420)
   {
      rilDll = LoadLibrary(TEXT("ril.dll"));

      if (!rilDll )
         throwException(currentContext, RuntimeException, "Could not load library for CellInfo");
      else
      {
         _RIL_Initialize            = (RIL_InitializeProc)           GetProcAddress(rilDll, TEXT("RIL_Initialize"));
         _RIL_Deinitialize          = (RIL_DeinitializeProc)         GetProcAddress(rilDll, TEXT("RIL_Deinitialize"));
         _RIL_GetRegistrationStatus = (RIL_GetRegistrationStatusProc)GetProcAddress(rilDll, TEXT("RIL_GetRegistrationStatus"));
         _RIL_GetCellTowerInfo      = (RIL_GetCellTowerInfoProc)     GetProcAddress(rilDll, TEXT("RIL_GetCellTowerInfo"));
         _RIL_GetSignalQuality      = (RIL_GetSignalQualityProc)     GetProcAddress(rilDll, TEXT("RIL_GetSignalQuality"));

         if (!_RIL_Initialize || !_RIL_Deinitialize || !_RIL_GetRegistrationStatus || !_RIL_GetCellTowerInfo || !_RIL_GetSignalQuality)
            throwException(currentContext, RuntimeException, "Could not load the procedures required for CellInfo");
         else
         {
            HRESULT err = S_FALSE;
            int32 i;

            for (i = 0 ; i < 5  && err == S_FALSE ; i++)
               err = _RIL_Initialize(1, RilCallback, null, 0, 0, &rilHandle);
            if (err != S_OK)
               throwException(currentContext, RuntimeException, "RIL_Initialize falhou, o que fazer aqui?");
            else
            {
               htCallbackResults = htNew(10, null);
            }
         }
      }
   }
}

static void CellInfoReleaseResources()
{
   if (rilHandle)
      _RIL_Deinitialize(rilHandle);
   if (rilDll)
      FreeLibrary(rilDll);
   rilHandle = rilDll = null;
   htFree(&htCallbackResults, null);
}

static void CellInfoUpdate(int32* mcc, int32* mnc, int32* lac, int32* cellid, int32* signal)
{
   HRESULT hrCmdID;
   VoidP data;
   DWORD cbData;
   DWORD dwParam;

   hrCmdID = _RIL_GetSignalQuality(rilHandle);
   RilCallbackResult(hrCmdID, &data, &cbData, &dwParam);
   if (data != null)
   {
      RILSIGNALQUALITY* signalQualityP = (RILSIGNALQUALITY*) data;
      *signal = signalQualityP->nSignalStrength;
      xfree(data);
   }

   hrCmdID = _RIL_GetCellTowerInfo(rilHandle);
   RilCallbackResult(hrCmdID, &data, &cbData, &dwParam);
   if (data != null)
   {
      RILCELLTOWERINFO* info = (RILCELLTOWERINFO*)data;
      *mcc = info->dwMobileCountryCode;
      *mnc = info->dwMobileNetworkCode;
      *lac = info->dwLocationAreaCode;
      *cellid = info->dwCellID;
      xfree(data);
   }
}

DWORD RilCallbackResult(HRESULT hrCmdID, VoidP* lpData, DWORD* cbData, DWORD* dwParam)
{
   CallbackResult myCallbackResult;

   myCallbackResult.lpData = lpData;
   myCallbackResult.mutex = CreateEvent(null, true, false, null);
   htPutPtr(&htCallbackResults, hrCmdID, &myCallbackResult);
   
   WaitForSingleObject(myCallbackResult.mutex, INFINITE);
   CloseHandle(myCallbackResult.mutex);
   htRemove(&htCallbackResults, hrCmdID);

   *cbData = myCallbackResult.cbData;
   *dwParam = myCallbackResult.dwParam;
   return myCallbackResult.dwCode;
}

CALLBACK RilCallback(DWORD dwCode, HRESULT hrCmdID, const void* lpData, DWORD cbData, DWORD dwParam)
{
   CallbackResultP callbackResultP = null;
   
   RILSIGNALQUALITY* aux = (RILSIGNALQUALITY*) lpData;

   while (callbackResultP == null)
      callbackResultP = (CallbackResultP) htGetPtr(&htCallbackResults, hrCmdID);

   if (lpData != null && (*(callbackResultP->lpData) = xmalloc(cbData)) != null)
      xmemmove(*(callbackResultP->lpData), lpData, cbData);
   callbackResultP->cbData = cbData;
   callbackResultP->dwCode = dwCode;
   callbackResultP->dwParam = dwParam;
   PulseEvent(callbackResultP->mutex);
}
