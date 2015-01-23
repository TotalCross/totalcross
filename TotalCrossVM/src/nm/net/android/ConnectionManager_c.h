/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#if 0
static Err CmGprsConfigure(Context currentContext, TCHARP szConnCfg)
{
#if defined (WINCE)
   TCHAR xml[1024];
   LPWSTR out;
   Err err = 0;
   TCHAR apn[32];
   TCHAR username[32];
   TCHAR password[32];
   TCHAR domain[32];

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300)
   {
      if (szConnCfg != null)
      {
         parseArgs(szConnCfg, TEXT("apn"), apn);
         parseArgs(szConnCfg, TEXT("username"), username);
         parseArgs(szConnCfg, TEXT("password"), password);
         parseArgs(szConnCfg, TEXT("domain"), domain);
      }
      if (*apn == *username == *password == *domain == 0)
         return NO_ERROR;
      _stprintf(xml, TEXT("<wap-provisioningdoc><characteristic type=\"CM_GPRSEntries\"><characteristic type=\"%s\"><parm name=\"DestId\" value=\"{436EF144-B4FB-4863-A041-8F905A62C572}\"/><parm name=\"UserName\" value=\"%s\"/><parm name=\"Password\" value=\"%s\"/><parm name=\"Domain\" value=\"%s\"/><characteristic type=\"DevSpecificCellular\"><parm name=\"GPRSInfoValid\" value=\"1\"/><parm name=\"GPRSInfoAccessPointName\" value=\"%s\"/></characteristic></characteristic></characteristic></wap-provisioningdoc>"), TEXT("TotalCrossGPRS"), username, password, domain, apn);

      if ((err = _DMProcessConfigXML((LPCWSTR) xml, 0x0001, &out)) == S_OK &&
          (err = _DMProcessConfigXML((LPCWSTR) TEXT("<wap-provisioningdoc><characteristic type=\"CM_Planner\"><characteristic type=\"PreferredConnections\"><parm name=\"{436EF144-B4FB-4863-A041-8F905A62C572}\" value=\"TotalCrossGPRS\"/></characteristic></characteristic></wap-provisioningdoc>"), 0x0001, &out)) == S_OK &&
          (err = _DMProcessConfigXML((LPCWSTR) TEXT("<wap-provisioningdoc><characteristic type=\"CM_ProxyEntries\"><characteristic type=\"HTTP-{ADB0B001-10B5-3F39-27C6-9742E785FCD4}\"><parm name=\"SrcId\" value=\"{ADB0B001-10B5-3F39-27C6-9742E785FCD4}\"/><parm name=\"DestId\" value=\"{436EF144-B4FB-4863-A041-8F905A62C572}\"/><parm name=\"Enable\" value=\"1\"/><parm name=\"Proxy\" value=\"new-inet:1159\"/><parm name=\"Type\" value=\"0\"/></characteristic></characteristic></wap-provisioningdoc>"), 0x0001, &out)) == S_OK)
          err = NO_ERROR;
      return err;
   }
   throwException(currentContext, RuntimeException, "Device not recognized as Windows Mobile");
   return NO_ERROR; // Avoid throwing a second exception.
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

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 300)
   {
      xmemzero (&connectionInfo, sizeof (connectionInfo));
      connectionInfo.cbSize = sizeof (connectionInfo);

      connectionInfo.dwParams = CONNMGR_PARAM_GUIDDESTNET;
      connectionInfo.dwFlags = CONNMGR_FLAG_PROXY_HTTP;
      connectionInfo.dwPriority = CONNMGR_PRIORITY_USERINTERACTIVE;
      *wasSuccessful = false;

      connectionInfo.guidDestNet = IID_DestNetInternet;
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
   HINSTANCE hInstanceDll = null;
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
#endif
static jclass gConnMgrClass;

static Err CmGetHostAddress(CharP hostName, CharP hostAddress)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getHostAddressMethod = (*env)->GetStaticMethodID(env, jConnectionManager4A, "getHostAddress", "(Ljava/lang/String;)Ljava/lang/String;");
   JCharP jhostName = CharP2JCharP(hostName,-1);
   jstring jHostName = (*env)->NewString(env, (jchar*) jhostName, xstrlen(hostName));
   jstring jString = (jstring) (*env)->CallStaticObjectMethod(env, jConnectionManager4A, getHostAddressMethod, jHostName);
   xfree(jhostName);
   if (jString != null)
   {
      jstring2CharP(jString, hostAddress);
      (*env)->DeleteLocalRef(env, jString); // guich@tc125_1
   }
   (*env)->DeleteLocalRef(env, jHostName);
   return NO_ERROR;
}

static Err CmGetHostName(CharP hostAddress, CharP hostName)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getHostNameMethod = (*env)->GetStaticMethodID(env, jConnectionManager4A, "getHostName", "(Ljava/lang/String;)Ljava/lang/String;");
   JCharP jhostAddress = CharP2JCharP(hostAddress,-1);
   jstring jHostAddress = (*env)->NewString(env, (jchar*) jhostAddress, xstrlen(hostAddress));
   jstring jString = (jstring) (*env)->CallStaticObjectMethod(env, jConnectionManager4A, getHostNameMethod, jHostAddress);
   xfree(jhostAddress);
   if (jString != null)              
   {
      jstring2CharP(jString, hostName);
      (*env)->DeleteLocalRef(env, jString); // guich@tc125_1
   }
   (*env)->DeleteLocalRef(env, jHostAddress);
   return NO_ERROR;
}

static Err CmGetLocalHost(CharP address)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getLocalHostMethod = (*env)->GetStaticMethodID(env, jConnectionManager4A, "getLocalHost", "()Ljava/lang/String;");
   jstring jString = (jstring) (*env)->CallStaticObjectMethod(env, jConnectionManager4A, getLocalHostMethod);
   if (jString != null)
   {
      jstring2CharP(jString, address);
      (*env)->DeleteLocalRef(env, jString); // guich@tc125_1
   }

   return NO_ERROR;
}

#if 0
static boolean CmIsAvailable(int type)
{
   switch (type)
   {
      case 1: return isWifiActive();
      default: return true; // flsobral@115: always return true, let the user try to open the gprs connection.
   }
}
#endif
