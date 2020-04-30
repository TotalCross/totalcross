// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "winsockLib.h"

#include "../RadioDevice.h"

static boolean RdGetStateWIFI();

#ifdef WINCE
extern bool emptyImei; // settings_c.h
#endif

static bool RdIsSupported(int32 type)
{
#if defined (WINCE)
   if (type == PHONE)
      return cellcoreDll != null && !emptyImei;
#endif
   return false;
}

static void RdSetState(int32 type, int32 state)
{
#if defined (WINCE)
   RDD* pDevice = null;
   RDD* pTD;

   // tc constants to wince constants
   if (type == WIFI)
      type = RADIODEVICES_MANAGED;
   else if (type == PHONE)
      type = RADIODEVICES_PHONE;
   else if (type == BLUETOOTH)
      type = RADIODEVICES_BLUETOOTH;
   else
      return;

   if (_GetWirelessDevices(&pDevice, 0) != S_OK || pDevice == null)
      return;

   // loop through the linked list of devices
   for (pTD = pDevice ; pTD != null ; pTD = pTD->pNext)
   {
      if (pTD->DeviceType == type)
         _ChangeRadioState(pTD, state, RADIODEVICES_PRE_SAVE);
      pTD->pszDeviceName = LocalFree(pTD->pszDeviceName);
      pTD->pszDisplayName = LocalFree(pTD->pszDisplayName);
   }
   _FreeDeviceList(pDevice);
#endif
}

static int32 RdGetState(int32 type)
{
#if defined (WINCE)
   if (type == WIFI && *tcSettings.romVersionPtr < 420)
      return RdGetStateWIFI() ? RADIO_STATE_ENABLED : RADIO_STATE_DISABLED;
   else
   {
      RDD* pDevice = null;
      RDD* pTD;
      int32 state = 0; // Default is disabled.

      // tc constants to wince constants
      if (type == WIFI)
         type = RADIODEVICES_MANAGED;
      else if (type == PHONE)
         type = RADIODEVICES_PHONE;
      else if (type == BLUETOOTH)
         type = RADIODEVICES_BLUETOOTH;
      else
         return 0;
      
      if (_GetWirelessDevices(&pDevice, 0) != S_OK || pDevice == null)
         return 0;
      
      // loop through the linked list of devices
      for (pTD = pDevice ; pTD != null ; pTD = pTD->pNext)
      {
         if (pTD->DeviceType == type)
            state = pTD->dwState;
         pTD->pszDeviceName = LocalFree(pTD->pszDeviceName);
         pTD->pszDisplayName = LocalFree(pTD->pszDisplayName);
      }
      _FreeDeviceList(pDevice);
      
      return state;
   }
#endif
}

boolean RdGetStateWIFI()
{
#if defined (WINCE)
   HKEY regKey = null;
   TCHAR adapterPath[MAX_PATHNAME] = TEXT("Comm\\");
   int32 nameLen = MAX_PATHNAME - 5;
   DWORD valueType;
   Err err;

   if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("Software\\Microsoft\\WZCSVC\\Parameters\\Interfaces"), 0, 0, &regKey)) != NO_ERROR)
      goto returnFalse;
   if ((err = RegEnumKeyEx(regKey, 0, adapterPath + 5, &nameLen, null, null, null, null)) != NO_ERROR)
      goto returnFalse;
   RegCloseKey(regKey);
   regKey = null;

   tcscat(adapterPath, TEXT("\\Parms\\TcpIp"));
   if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, adapterPath, 0, 0, &regKey)) != NO_ERROR)
      goto returnFalse;
   if ((err = RegQueryValueEx(regKey, TEXT("DhcpDefaultGateway"), null, &valueType, null, null)) != NO_ERROR)
      goto returnFalse;
   RegCloseKey(regKey);
   
   return true;

returnFalse:
   if (regKey != null)
      RegCloseKey(regKey);
#endif

   return false;
}
