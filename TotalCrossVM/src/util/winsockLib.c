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

#include "tcvm.h"
#include "winsockLib.h"


static HMODULE wsLib = null;
#if defined (WINCE)
static HMODULE ossvcsLib = null;
#endif

bool initWinsock()
{
#ifndef WP8
   if (wsLib == null)
   {
#if defined (WINCE)
      OSVERSIONINFO osvi;
      // OS version
      osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
      GetVersionEx(&osvi);
      if ((osvi.dwMajorVersion * 100 + osvi.dwMinorVersion) >= 420) // try to load ws2.dll
         wsLib = LoadLibrary(TEXT("ws2.dll"));
      if (wsLib == null)
         wsLib = LoadLibrary(TEXT("winsock.dll"));
      ossvcsLib = LoadLibrary(TEXT("ossvcs.dll"));
#else
      wsLib = LoadLibrary(TEXT("ws2_32.dll"));
#endif
   }
   if (wsLib != null) // successfully loaded
   {
      WSAStartupProc       = (LPFN_WSASTARTUP     ) GetProcAddress(wsLib, TEXT("WSAStartup"));
      socketProc           = (LPFN_SOCKET         ) GetProcAddress(wsLib, TEXT("socket"));
      inet_addrProc        = (LPFN_INET_ADDR      ) GetProcAddress(wsLib, TEXT("inet_addr"));
      gethostbynameProc    = (LPFN_GETHOSTBYNAME  ) GetProcAddress(wsLib, TEXT("gethostbyname"));
      WSAGetLastErrorProc  = (LPFN_WSAGETLASTERROR) GetProcAddress(wsLib, TEXT("WSAGetLastError"));
      htonsProc            = (LPFN_HTONS          ) GetProcAddress(wsLib, TEXT("htons"));
      ioctlsocketProc      = (LPFN_IOCTLSOCKET    ) GetProcAddress(wsLib, TEXT("ioctlsocket"));
      connectProc          = (LPFN_CONNECT        ) GetProcAddress(wsLib, TEXT("connect"));
      selectProc           = (LPFN_SELECT         ) GetProcAddress(wsLib, TEXT("select"));
      shutdownProc         = (LPFN_SHUTDOWN       ) GetProcAddress(wsLib, TEXT("shutdown"));
      closesocketProc      = (LPFN_CLOSESOCKET    ) GetProcAddress(wsLib, TEXT("closesocket"));
      WSACleanupProc       = (LPFN_WSACLEANUP     ) GetProcAddress(wsLib, TEXT("WSACleanup"));
      recvProc             = (LPFN_RECV           ) GetProcAddress(wsLib, TEXT("recv"));
      sendProc             = (LPFN_SEND           ) GetProcAddress(wsLib, TEXT("send"));
      bindProc             = (LPFN_BIND           ) GetProcAddress(wsLib, TEXT("bind"));
      inet_ntoaProc        = (LPFN_INET_NTOA      ) GetProcAddress(wsLib, TEXT("inet_ntoa"));
      gethostbyaddrProc    = (LPFN_GETHOSTBYADDR  ) GetProcAddress(wsLib, TEXT("gethostbyaddr"));
      gethostnameProc      = (LPFN_GETHOSTNAME    ) GetProcAddress(wsLib, TEXT("gethostname"));
      listenProc           = (LPFN_LISTEN         ) GetProcAddress(wsLib, TEXT("listen"));
      acceptProc           = (LPFN_ACCEPT         ) GetProcAddress(wsLib, TEXT("accept"));
      ntohlProc            = (LPFN_NTOHL          ) GetProcAddress(wsLib, TEXT("ntohl"));
      htonlProc            = (LPFN_HTONL          ) GetProcAddress(wsLib, TEXT("htonl"));
      getsocknameProc      = (LPFN_GETSOCKNAME    ) GetProcAddress(wsLib, TEXT("getsockname"));

      WSALookupServiceBeginProc  = (LPFN_WSALOOKUPSERVICEBEGIN ) GetProcAddress(wsLib, _WSALookupServiceBegin_);
      WSALookupServiceNextProc   = (LPFN_WSALOOKUPSERVICENEXT  ) GetProcAddress(wsLib, _WSALookupServiceNext_);
      WSALookupServiceEndProc    = (LPFN_WSALOOKUPSERVICEEND   ) GetProcAddress(wsLib, TEXT("WSALookupServiceEnd"));
      WSASetServiceProc          = (LPFN_WSASETSERVICE         ) GetProcAddress(wsLib, _WSASetService_);
   }
#if defined (WINCE)
   if (ossvcsLib != null)
   {
      pGetWirelessDevices  = (GetWirelessDevicesProc) GetProcAddress(ossvcsLib, MAKEINTRESOURCE(GetWirelessDevice_ORDINAL));
      pChangeRadioState    = (ChangeRadioStateProc)   GetProcAddress(ossvcsLib, MAKEINTRESOURCE(ChangeRadioState_ORDINAL));
      pFreeDeviceList      = (FreeDeviceListProc)     GetProcAddress(ossvcsLib, MAKEINTRESOURCE(FreeDeviceList_ORDINAL));
   }
#endif
#endif
   return true;
}

void closeWinsock()
{
   if (wsLib != null)
   {
      FreeLibrary(wsLib);
      wsLib = null;
   }
#if defined (WINCE)
   if (ossvcsLib != null)
   {
      FreeLibrary(ossvcsLib);
      ossvcsLib = null;
   }
#endif
}
