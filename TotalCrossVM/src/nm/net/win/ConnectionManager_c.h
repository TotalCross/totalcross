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
#include "win/aygshellLib.h"
#if !defined WP8
#include <Ras.h>
#endif
#if defined (WINCE)
 #include "connmgr_defines.h"
#endif
#include "guid.h"

#define NATIVE_CONNECTION HANDLE

#include "../../io/device/win/RadioDevice_c.h"

TCHARP parseArgs(TCHARP line, TCHARP argument, TCHARP argValue)
{
   TCHARP p1, p2;

   p1 = tcsstr(line, argument);
   if (p1 == null)
      *argValue = 0;
   else
   {
      p1 += tcslen(argument) + 1;
      p2 = tcschr(p1, ';');
      if (p2 == null)
         tcscpy(argValue, p1);
      else
      {
         *p2 = 0;
         tcscpy(argValue, p1);
         *p2 = ';';
      }
   }
   return argValue;
}

#if defined (WINCE)
static ConnMgrEstablishConnectionSyncProc _ConnMgrEstablishConnectionSync = null;
static ConnMgrMapURLProc _ConnMgrMapURL = null;
static ConnMgrReleaseConnectionProc _ConnMgrReleaseConnection = null;
static ConnMgrMapConRefProc _ConnMgrMapConRef = null;

boolean isWifiActive();
bool RasLookup(RASCONNSTATE state, TCHARP szDeviceType, RASCONN* rasConn);
#endif

static Err CmGprsConfigure(Context currentContext, TCHARP szConnCfg)
{
#if defined (WINCE)
   TCHAR xml[1024];
   LPWSTR out;
   Err err = NO_ERROR;
   TCHAR apn[32];
   TCHAR username[32];
   TCHAR password[32];
   TCHAR domain[32];

   if (!isWindowsMobile || *tcSettings.romVersionPtr < 300)
      throwException(currentContext, RuntimeException, "Device not recognized as Windows Mobile");
   else if (!_DMProcessConfigXML)
      throwException(currentContext, RuntimeException, "Operation not supported by the device.");
   else
   {
      if (szConnCfg != null)
      {
         parseArgs(szConnCfg, TEXT("apn"), apn);
         parseArgs(szConnCfg, TEXT("username"), username);
         parseArgs(szConnCfg, TEXT("password"), password);
         parseArgs(szConnCfg, TEXT("domain"), domain);
      }
      if (!(*apn == *username == *password == *domain == 0))
      {
         _stprintf(xml, TEXT("<wap-provisioningdoc><characteristic type=\"CM_GPRSEntries\"><characteristic type=\"%s\"><parm name=\"DestId\" value=\"{436EF144-B4FB-4863-A041-8F905A62C572}\"/><parm name=\"UserName\" value=\"%s\"/><parm name=\"Password\" value=\"%s\"/><parm name=\"Domain\" value=\"%s\"/><characteristic type=\"DevSpecificCellular\"><parm name=\"GPRSInfoValid\" value=\"1\"/><parm name=\"GPRSInfoAccessPointName\" value=\"%s\"/></characteristic></characteristic></characteristic></wap-provisioningdoc>"), TEXT("TotalCrossGPRS"), username, password, domain, apn);

         if ((err = _DMProcessConfigXML((LPCWSTR) xml, 0x0001, &out)) == S_OK &&
             (err = _DMProcessConfigXML((LPCWSTR) TEXT("<wap-provisioningdoc><characteristic type=\"CM_Planner\"><characteristic type=\"PreferredConnections\"><parm name=\"{436EF144-B4FB-4863-A041-8F905A62C572}\" value=\"TotalCrossGPRS\"/></characteristic></characteristic></wap-provisioningdoc>"), 0x0001, &out)) == S_OK &&
             (err = _DMProcessConfigXML((LPCWSTR) TEXT("<wap-provisioningdoc><characteristic type=\"CM_ProxyEntries\"><characteristic type=\"HTTP-{ADB0B001-10B5-3F39-27C6-9742E785FCD4}\"><parm name=\"SrcId\" value=\"{ADB0B001-10B5-3F39-27C6-9742E785FCD4}\"/><parm name=\"DestId\" value=\"{436EF144-B4FB-4863-A041-8F905A62C572}\"/><parm name=\"Enable\" value=\"1\"/><parm name=\"Proxy\" value=\"new-inet:1159\"/><parm name=\"Type\" value=\"0\"/></characteristic></characteristic></wap-provisioningdoc>"), 0x0001, &out)) == S_OK)
             err = NO_ERROR;
      }
   }

   return err;
#else
   return -1;
#endif
}

static Err CmGprsOpen(Context currentContext, NATIVE_CONNECTION* connHandle, int32 timeout, bool* wasSuccessful)
{
#if defined (WINCE)
   CONNMGR_CONNECTIONINFO connectionInfo;
   GUID guid;
   DWORD index = 0;
   DWORD status = 0;

   HKEY regKey = null;
   HKEY subKey = null;
   DWORD value = 0;
   DWORD valueSize = sizeof(DWORD);
   Err err;
   int32 i;
   TCHAR subkeyName[128];
   int32 subkeyNameLen;
   int32 szGuidLen;
   TCHAR destid[64];
   DWORD destidSize = sizeof(TCHAR) * 64;

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300)
   {
      xmemzero (&connectionInfo, sizeof (connectionInfo));
      connectionInfo.cbSize = sizeof (connectionInfo);

      connectionInfo.dwParams = CONNMGR_PARAM_GUIDDESTNET;
      connectionInfo.dwFlags = CONNMGR_FLAG_PROXY_HTTP;
      connectionInfo.dwPriority = CONNMGR_PRIORITY_USERINTERACTIVE;
      *wasSuccessful = false;

      // search registry for preferred connection
      if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("Comm\\ConnMgr\\Providers\\{7C4B7A38-5FF7-4bc1-80F6-5DA7870BB1AA}\\Connections"), 0, 0, &regKey)) != NO_ERROR)
         if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\Microsoft\\ConnMgr\\Providers\\{7C4B7A38-5FF7-4bc1-80F6-5DA7870BB1AA}\\Connections"), 0, 0, &regKey)) != NO_ERROR)
            goto regError;

      for (i = 0 ; err == NO_ERROR ; i++)
      {
         subkeyNameLen = szGuidLen = 128;
         if ((err = RegEnumKeyEx(regKey, i, subkeyName, &subkeyNameLen, null, null, null, null)) == NO_ERROR)
         {
            if (((err = RegOpenKeyEx(regKey, subkeyName, 0, 0, &subKey)) == NO_ERROR)
             && ((err = RegQueryValueEx(subKey, TEXT("Enabled"), null, null, (LPBYTE) &value, &valueSize)) == NO_ERROR)
             && ((err = RegQueryValueEx(subKey, TEXT("DestId"), null, null, (LPBYTE) &destid, &destidSize)) == NO_ERROR)
              && (value == 1))
            {
               //flsobral: also check the DestId to make sure we are getting an Internet connection. Needed for brazillian carrier "Claro", which doesn't make any other distinction between GPRS, MMS or Video connections.   
               if (tcscmp(destid, TEXT("{0DAEA92E-2917-4C6C-9E23-F2BCAA13DA07}")) != 0 || (_ConnMgrMapConRef && _ConnMgrMapConRef(ConRefType_NAP, subkeyName, &guid) != S_OK))
                  value = 0;
            }
            if (subKey != null)
            {
               RegCloseKey(subKey);
               subKey = null;

               if (value == 1)
                  break;
            }
         }
      }

      if (regKey != null)
         RegCloseKey(regKey);

      // finished registry crap.

      connectionInfo.guidDestNet = guid; //IID_DestNetInternet;
      *wasSuccessful = _ConnMgrEstablishConnectionSync(&connectionInfo, connHandle, timeout, &status) == S_OK;
      if (!*wasSuccessful)
      {
         if (_ConnMgrMapURL(TEXT("http://www.superwaba.com/"), &guid, &index) != S_OK)
            xmemzero(&guid, sizeof(guid));
         connectionInfo.guidDestNet = guid;
         *wasSuccessful = _ConnMgrEstablishConnectionSync(&connectionInfo, connHandle, timeout, &status) == S_OK;
      }
      if (*wasSuccessful) //flsobral@tc115_51: check the connection a second time before returning.
         return NO_ERROR;
   }
   return -1;   

regError:
   if (regKey != null)
      RegCloseKey(regKey);
   return -1;
#else
   return -1;
#endif
}

static Err CmOpen(Context currentContext, NATIVE_CONNECTION* connHandle, int32 timeout, bool* wasSuccessful)
{
#if defined (WINCE)
   RASCONN rasConn;

   //alert("test for activesync");

   // ActiveSync?
   if (RasLookup(RASCS_Connected, TEXT("direct"), &rasConn))
      return NO_ERROR;

   //alert("test for wifi");

   // WiFi?
   if (isWifiActive())
      return NO_ERROR;
   
   //alert("test for gprs");

   // GPRS?
   if (RasLookup(RASCS_Connected, TEXT("modem"), &rasConn))
      return NO_ERROR;

   //alert("open gprs");

   // use preferred GPRS connection
   return CmGprsOpen(currentContext, connHandle, timeout, wasSuccessful);
#else
   return -1;
#endif
}

/************   CmClose   ***********
*
* RASCONN
*
* RasEnumConnections
* RasHangUp
*
* OS Versions: Windows CE 1.0 and later.
* Header: Ras.h.
* Link Library: Coredll.lib. (Rasapi32.lib on WIN32)
*
*************************************/

static Err CmClose(Context currentContext, NATIVE_CONNECTION* hConnection)
{
#if defined (WINCE)
   RASCONN ras[20];
   RASCONNSTATUS rasStatus;
   DWORD dSize, dNumber;
   int32 i;
   Err err;

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300)
      _ConnMgrReleaseConnection(hConnection, 0);
   
   // RAS HANGUP
   ras[0].dwSize = sizeof(RASCONN);
   dSize = sizeof(ras);

   // Get active RAS - Connections
   if ((err = RasEnumConnections(ras, &dSize, &dNumber)) != 0)
      return err;

   for (i = dNumber ; i >= 0 ; i--) //flsobral@tc115_52: fixed to properly search for an active modem connection and close it.
      if (RasGetConnectStatus(ras[i].hrasconn, &rasStatus) == 0 //RasGetConnectStatus was successful... 
       && rasStatus.rasconnstate == RASCS_Connected             //the connection is currently active...
       && tcscmp(rasStatus.szDeviceType, TEXT("modem")) == 0)   //and it's a modem connection!
      {
         RasHangUp(ras[i].hrasconn);
         break;
      }

  return NO_ERROR;
#else
   return -1;
#endif
}

/*
static Err CmIsOpen()
{
   ConnMgrConnectionStatusProc procConnMgrConnectionStatus = null;
   DWORD status;
   Err err = 0;

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300)
   {
      if (cellcoreDll == null)
         throwException(currentContext, IOException, "Could not load the library Cellcore.dll");
      else
      {
         if ((procConnMgrConnectionStatus = (ConnMgrConnectionStatusProc) GetProcAddress(cellcoreDll, _T("ConnMgrConnectionStatus"))) == null)
            throwException(currentContext, IOException, "Could not load ConnMgrConnectionStatus");
         else
         {
            if ((err = procConnMgrConnectionStatus(hConnection, &status)) != 0)
               err = CmOpenConnection(currentContext, &hConnection, -1, &wasSuccessful);
         }
      }
      return err;
   }
   return -1;
}
*/

static Err CmGetHostAddress(CharP hostName, CharP hostAddress)
{
   WSADATA WSAData;
   HOSTENT *pHost = null;
   struct in_addr **ppip;
   struct in_addr ip;
   Err err;

   if ((err = WSAStartup(MAKEWORD(1,1), &WSAData)) != 0)
      return err;

   pHost = gethostbyname(hostName);
   if (pHost && pHost->h_addrtype == AF_INET)
   {
      ppip = (struct in_addr**) pHost->h_addr_list;
      //Enumerate all addresses
      while (*ppip)
      {
         ip = **ppip;
         xstrcpy(hostAddress, inet_ntoa(ip));
         ppip++;
         if (hostAddress != "")
            break;
      }
   }
   if (WSACleanup() == SOCKET_ERROR)
      goto Error;
   return NO_ERROR;
Error:
   return WSAGetLastError();
}

static Err CmGetHostName(CharP hostAddress, CharP hostName)
{
   WSADATA WSAData;
   HOSTENT *pHost = null;
   struct in_addr hostAddr;
   Err err;

   if ((err = WSAStartup(MAKEWORD(1,1), &WSAData)) != 0)
      return err;

   hostAddr.S_un.S_addr = inet_addr(hostAddress);
   if ((pHost = gethostbyaddr((CharP) &hostAddr, sizeof(hostAddr), AF_INET)) == null)
      goto Error;

   xstrcpy(hostName, pHost->h_name);

   if (WSACleanup() == SOCKET_ERROR)
      goto Error;

   return NO_ERROR;
Error:
   return WSAGetLastError();
}

static Err CmGetLocalHost(CharP address)
{
   char strHostName[81];
   WSADATA WSAData;
   HOSTENT *pHost = null;
   struct in_addr **ppip;
   struct in_addr ip;
   Err err;

   if ((err = WSAStartup(MAKEWORD(1,1), &WSAData)) != 0)
      return err;
   if (gethostname(strHostName, 80) == SOCKET_ERROR)
      goto Error;

   pHost = gethostbyname(strHostName);
   if (pHost == null)
      xstrcpy(address, "127.0.0.1");
   else if (pHost->h_addrtype == AF_INET)
   {
      ppip = (struct in_addr**) pHost->h_addr_list;
      //Enumerate all addresses
      while (*ppip)
      {
         ip = **ppip;
         xstrcpy(address, inet_ntoa(ip));
         ppip++;
         if (address != "")
            break;
      }
   }
   if (WSACleanup() == SOCKET_ERROR)
      goto Error;
   return NO_ERROR;
Error:
   return WSAGetLastError();
}

static void CmLoadResources(Context currentContext)
{
#if defined (WINCE)
   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300)
   {
      if (!cellcoreDll )
         throwException(currentContext, RuntimeException, "Could not load the required libraries for the ConnectionManager");
      else
      {
         _ConnMgrEstablishConnectionSync = (ConnMgrEstablishConnectionSyncProc) GetProcAddress(cellcoreDll, TEXT("ConnMgrEstablishConnectionSync"));
         _ConnMgrMapURL = (ConnMgrMapURLProc) GetProcAddress(cellcoreDll, TEXT("ConnMgrMapURL"));
         _ConnMgrReleaseConnection = (ConnMgrReleaseConnectionProc) GetProcAddress(cellcoreDll, TEXT("ConnMgrReleaseConnection"));
         _ConnMgrMapConRef = (ConnMgrMapConRefProc) GetProcAddress(cellcoreDll, TEXT("ConnMgrMapConRef"));

         if (!_ConnMgrEstablishConnectionSync || !_ConnMgrMapURL || !_ConnMgrReleaseConnection)
            throwException(currentContext, RuntimeException, "Could not load the required procedures for the ConnectionManager");
      }
   }
#endif
}

static void CmReleaseResources()
{
}

static boolean CmIsAvailable(int type)
{
#if defined (WINCE)
   switch (type)
   {
      case CM_CRADLE:
      {
         RASCONN rasConn;
         if (isWindowsMobile) // first check the registry for windows mobile
         {
            HKEY regKey = null;
            DWORD value = 0;
            DWORD valueSize = sizeof(DWORD);
            boolean result = false;
            Err err;

            if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("System\\State\\Hardware"), 0, 0, &regKey)) != NO_ERROR)
               goto finish;
            if ((err = RegQueryValueEx(regKey, TEXT("Cradled"), null, null, (LPBYTE) &value, &valueSize)) != NO_ERROR)
               goto finish;

            result = (value == 1);
            finish:
            if (regKey != null)
               RegCloseKey(regKey);

            return result;
         }
         return RasLookup(RASCS_Connected, TEXT("direct"), &rasConn);
      } break;
      case CM_WIFI: return isWifiActive();
      case CM_CELLULAR: return (RdGetState(1) == 1);

      default: return false; // flsobral@120: default now is false.
   }
#else
	return false;
#endif
}

#if defined (WINCE)
boolean isWifiActive()
{
   HKEY regKey = null;
   TCHAR adapterName[128] = TEXT("");
   TCHAR adapterPath[MAX_PATHNAME] = TEXT("Comm\\");
   TCHAR adapterGUID[MAX_PATHNAME] = TEXT("{98C5250D-C29A-4985-AE5F-AFE5367E5006}"); // {98C5250D-C29A-4985-AE5F-AFE5367E5006} is the GUID for power-managed NDIS miniports. That usually includes Wi-Fi driver.
   TCHAR adapterState[MAX_PATHNAME];
   int32 nameLen = MAX_PATHNAME - 5;
   DWORD valueType;
   DWORD adapterStateValue;
   DWORD valueSize = sizeof(DWORD);
   int32 adapterGUIDLen = tcslen(adapterGUID);
   int32 i;
   Err err;

   if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("Software\\Microsoft\\WZCSVC\\Parameters\\Interfaces"), 0, 0, &regKey)) == NO_ERROR)
   {
      if ((err = RegEnumKeyEx(regKey, 0, adapterName, &nameLen, null, null, null, null)) != NO_ERROR)
         goto returnFalse;
      RegCloseKey(regKey);
      regKey = null;
   }

   if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("System\\CurrentControlSet\\Control\\Power\\State"), 0, 0, &regKey)) != NO_ERROR)
      goto returnFalse;
   if (tcslen(adapterName) > 0)
      tcscat(adapterState, adapterName);
   else
   {
      nameLen = MAX_PATHNAME - 5;
      for (i = 0 ; (err = RegEnumValue(regKey, i, adapterState, &nameLen, null, null, null, null)) != ERROR_NO_MORE_ITEMS ; i++)
      {
         if (err != NO_ERROR)
            goto returnFalse;
         if (nameLen > 0 && tcsncmp(adapterState, adapterGUID, adapterGUIDLen) == 0)
         {
            tcscpy(adapterName, adapterState + adapterGUIDLen + 1);
            break;
         }
         nameLen = MAX_PATHNAME - 5;
      }
      if (err == ERROR_NO_MORE_ITEMS)
         goto returnFalse;
   }
   if ((err = RegQueryValueEx(regKey, adapterState, null, &valueType, (LPBYTE) &adapterStateValue, &valueSize)) != NO_ERROR)
      goto returnFalse;
   RegCloseKey(regKey);
   regKey = null;

   if (!adapterStateValue) // 0 - off, 1 - on
      return false;

   tcscat(adapterPath, adapterName);
   tcscat(adapterPath, TEXT("\\Parms\\TcpIp"));
   if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, adapterPath, 0, 0, &regKey)) != NO_ERROR)
      goto returnFalse;
   err = RegQueryValueEx(regKey, TEXT("DhcpDefaultGateway"), null, &valueType, null, null);
   RegCloseKey(regKey);
   regKey = null;

   if (err != NO_ERROR && !isWindowsMobile) // flsobral@tc123_27: WinCE specific check.
   {
      TCHAR upperBind[64];
      // if DhcpDefaultGateway is not available, check for UpperBind in Parms
      adapterPath[tcslen(adapterPath) - 5] = 0;
      if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, adapterPath, 0, 0, &regKey)) != NO_ERROR)
         goto returnFalse;
      valueType = REG_MULTI_SZ;
      valueSize = sizeof(TCHAR) * 64;
      if ((err = RegQueryValueEx(regKey, TEXT("UpperBind"), null, &valueType, (LPBYTE) upperBind, &valueSize)) != NO_ERROR)
         goto returnFalse;
      RegCloseKey(regKey);
      regKey = null;

      // if UpperBind is available, check for DhcpIPAddress using %upperBind%_%adapterName%
      adapterPath[5] = 0;
      tcscat(adapterPath, upperBind);
      tcscat(adapterPath, TEXT("_"));
      tcscat(adapterPath, adapterName);
      tcscat(adapterPath, TEXT("\\Parms\\TcpIp"));

      if ((err = RegOpenKeyEx(HKEY_LOCAL_MACHINE, adapterPath, 0, 0, &regKey)) != NO_ERROR)
         goto returnFalse;
      if ((err = RegQueryValueEx(regKey, TEXT("DhcpIPAddress"), null, &valueType, null, null)) != NO_ERROR)
         goto returnFalse;
      RegCloseKey(regKey);
   }
   
   return true;

returnFalse:
   if (regKey != null)
      RegCloseKey(regKey);

   return false;
}

bool RasLookup(RASCONNSTATE state, TCHARP szDeviceType, RASCONN* rasConn)
{
   RASCONN ras[20];
   RASCONNSTATUS rasStatus;
   DWORD dSize, dNumber;
   int32 i;
   Err err;

   ras[0].dwSize = sizeof(RASCONN);
   dSize = sizeof(ras);

   // Get active RAS - Connections
   if ((err = RasEnumConnections(ras, &dSize, &dNumber)) != 0)
      return false;

   for (i = dNumber ; i >= 0 ; i--) //flsobral@tc115_52: fixed to properly search for an active modem connection and close it.
   {
      err = RasGetConnectStatus(ras[i].hrasconn, &rasStatus);

      if ((err /*= RasGetConnectStatus(ras[i].hrasconn, &rasStatus)*/ == 0) //RasGetConnectStatus was successful... 
       && rasStatus.rasconnstate == state             //the connection is currently active...
       && tcscmp(rasStatus.szDeviceType, szDeviceType) == 0)   //and it's a modem connection!
      {
         *rasConn = ras[i];
         return true;
      }
   }
   return false;
}
#endif
