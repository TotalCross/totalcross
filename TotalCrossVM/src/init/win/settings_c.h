// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "../../nm/ui/media_Sound.h"
#if !defined WP8
 #include <Tapi.h>
#endif

#if defined (WINCE)
 #include "win/aygshellLib.h"
 #include "../nm/io/device/RadioDevice.h"
 #include "../nm/io/device/win/RadioDevice_c.h"
 
 bool emptyImei; // guich@tc138: in some devices, the phone api is there but no imei is retrieved at all
#endif

#if !defined WINCE

 // COBJMACROS must be defined to include the macros from wbemcli.h that are used
 // to invoke methods of WBEM objects when the code is in C.
 #define COBJMACROS
#if !defined WP8
 #include <wbemcli.h>
#endif
 
 // Define the CLSID_WbemLocator. It is defined in wbemcli.h only for C++ programs.
 GUID CLSID_WbemLocator2 =  { 0x4590f811, 0x1d3a, 0x11d0, { 0x89, 0x1f, 0x00, 0xaa, 0x00, 0x4b, 0x2e, 0x24 } };
 // Define the IID_IWbemLocator. It is defined in wbemcli.h only for C++ programs.
 GUID IID_IWbemLocator2 =   { 0xdc12a687, 0x737f, 0x11cf, { 0x88, 0x4d, 0x00, 0xaa, 0x00, 0x4b, 0x2e, 0x24 } };
 // Define the IID_IWbemClassObject. It is defined in wbemcli.h only for C++ programs.
 GUID IID_IWbemClassObject2 =   { 0xdc12a681, 0x737f, 0x11cf, { 0x88, 0x4d, 0x00, 0xaa, 0x00, 0x4b, 0x2e, 0x24 } };

#endif
#if defined WINCE && _WIN32_WCE >= 300
 #ifndef _ExTAPI_H_
   /* From Extapi.h */
   typedef struct linegeneralinfo_tag {
       DWORD dwTotalSize;
       DWORD dwNeededSize;
       DWORD dwUsedSize;
       DWORD dwManufacturerSize;
       DWORD dwManufacturerOffset;
       DWORD dwModelSize;
       DWORD dwModelOffset;
       DWORD dwRevisionSize;
       DWORD dwRevisionOffset;
       DWORD dwSerialNumberSize;
       DWORD dwSerialNumberOffset;
       DWORD dwSubscriberNumberSize;
       DWORD dwSubscriberNumberOffset;
   } LINEGENERALINFO, *LPLINEGENERALINFO;
 #endif
#endif

// the crid replaces the last 4 characters of the given string
// @pre buf contains a string of at least 4 chars
static TCHAR *createRegistryKey(TCHAR *buf, uint32 crid)
{
   TCHAR *cur = buf + lstrlen(buf) - 4;

   // set the key name to the creator id of the application
   *cur++ = (TCHAR)((crid >> 24) & 0xFF);
   *cur++ = (TCHAR)((crid >> 16) & 0xFF);
   *cur++ = (TCHAR)((crid >> 8)  & 0xFF);
   *cur++ = (TCHAR)(crid & 0xFF);

   return buf;
}

#define STR_DEFAULT_KEY TEXT("Software\\TotalCross\\appSettings\\1234")
#define BIN_DEFAULT_KEY TEXT("Software\\TotalCross\\appSettings\\B1234") // guich@573_16
static void deleteAppSettingsTry(uint32 crid, bool bin, bool isHKLM) // guich@573_16: added bin option to the three methods below - guich@580_21: use hklm if for secret key
{
#if !defined WP8
   TCHAR buf[40];
   tcscpy(buf, bin ? BIN_DEFAULT_KEY : STR_DEFAULT_KEY);
#ifdef WINCE
   if (RegDeleteKey(isHKLM ? HKEY_LOCAL_MACHINE : HKEY_CURRENT_USER, createRegistryKey(buf,crid)) != NO_ERROR) // guich@580_21: if the user don't have enough priviledges, then use the HKCU.
#endif
      RegDeleteKey(HKEY_CURRENT_USER,createRegistryKey(buf,crid));
#else
	//WP8 should not use registry
#endif
}

static void getSettingsFile(uint32 crid, bool bin, bool isHKLM, CharP out)
{
   char src[MAX_PATHNAME];
   char c1 = (TCHAR)((crid >> 24) & 0xFF);
   char c2 = (TCHAR)((crid >> 16) & 0xFF);
   char c3 = (TCHAR)((crid >> 8)  & 0xFF);
   char c4 = (TCHAR)(crid & 0xFF);
   int32 type;
   if (isHKLM)
   {
      c1 -= 64;
      c2 -= 64;
      c3 -= 64;
      c4 -= 64;
   }
   type = bin ? 1 : isHKLM ? 2 : 3;
   xstrprintf(src, "%%ALLUSERSPROFILE%%\\app%c%c%c%c.dt%d",c1,c2,c3,c4,type);
   ExpandEnvironmentStrings(src, out, sizeof(src));
}

static void deleteAppSettings(uint32 crid, bool bin, bool isHKLM) // guich@573_16: added bin option to the three methods below - guich@580_21: use hklm if for secret key
{
#if defined(WIN32) && !defined(WINCE) && !defined(WP8)
   char dest[MAX_PATHNAME];
   FILE* f;
   getSettingsFile(crid, bin, isHKLM, dest);
   f = fopen(dest, "wb"); // just truncate the file
   if (f) fclose(f);
#else
   deleteAppSettingsTry(crid, bin, isHKLM);
#endif
}

static void setAppSettings(uint32 crid, TCObject ptr, bool bin, bool isHKLM) // guich@580_21: use hklm if for secret key
{
	//WP8 app should not use registry
#if defined(WIN32) && !defined(WINCE) && !defined(WP8)
   char dest[MAX_PATHNAME];
   FILE* f;
   getSettingsFile(crid, bin, isHKLM, dest);
   f = fopen(dest, "wb");
   if (f)
   {
      uint8* data;
      uint32 len;
      TCObject obj = (TCObject)ptr;
      if (!bin)
         obj = String_chars(obj);
      len = ARRAYOBJ_LEN(obj);
      data = (uint8*)ARRAYOBJ_START(obj);
      if (!bin)
         len *= 2;
      fwrite(data,len,1,f);
      fclose(f);
   }
#elif !defined WP8
   HKEY handle;
   DWORD disp;
   long ret;
   TCHAR buf[40];

   tcscpy(buf, bin ? BIN_DEFAULT_KEY : STR_DEFAULT_KEY);

#ifndef WINCE
   ret = RegCreateKeyEx(isHKLM ? HKEY_LOCAL_MACHINE : HKEY_CURRENT_USER,createRegistryKey(buf, crid),0,NULL,0,KEY_ALL_ACCESS,NULL,&handle,&disp);
   if (isHKLM && (ret || RegOpenKeyEx(HKEY_LOCAL_MACHINE, buf, 0, KEY_READ, &handle))) // guich@580_21: if the user don't have enough priviledges, then use the HKCU.
#endif
      ret = RegCreateKeyEx(HKEY_CURRENT_USER,createRegistryKey(buf, crid),0,NULL,0,KEY_ALL_ACCESS,NULL,&handle,&disp);
   if (ret == NO_ERROR)
   {
      uint8* data;
      uint32 len;
      TCObject obj = (TCObject)ptr;
      if (!bin)
         obj = String_chars(obj);
      len = ARRAYOBJ_LEN(obj);
      data = (uint8*)ARRAYOBJ_START(obj);
      if (!bin)
         len *= 2;
      ret = RegSetValueEx(handle,TEXT("Value"),0,REG_BINARY,data,len); // store the data
      RegCloseKey(handle);
   }
#endif
}

static void char8tochar16(CharP value, int32 i)
{
	CharP f = value + i;
	CharP t = value + (i << 1);
	for (; --i >= 0; f--,t-=2)
	{
		*t = *f;
		*f = 0;
	}
}

// don't forget to free the allocated buffer
static TCObject getAppSettingsTry(Context currentContext, uint32 crid, bool bin, bool isHKLM) // guich@580_21: use hklm if for secret key
{
	// WP8 app should not use registry
#if !defined WP8
   HKEY handle;
   long ret;
   DWORD len,type;
   TCHAR buf[40];
   TCObject temp = null, target = null;

   tcscpy(buf, bin ? BIN_DEFAULT_KEY : STR_DEFAULT_KEY);

#ifndef WINCE
   ret = RegOpenKeyEx(isHKLM ? HKEY_LOCAL_MACHINE : HKEY_CURRENT_USER,createRegistryKey(buf, crid),0,KEY_READ,&handle);
   if (ret != NO_ERROR) // guich@580_21: if the user don't have enough priviledges, then use the HKCU.
#endif
      ret = RegOpenKeyEx(HKEY_CURRENT_USER,createRegistryKey(buf, crid),0,KEY_READ,&handle);
   if (ret == NO_ERROR)     
   {
      len = 0;
      ret = RegQueryValueEx(handle,TEXT("Value"),NULL,NULL,NULL,&len);
      target = temp = bin ? createByteArray(currentContext, len) : createCharArray(currentContext, len/2); // guich@tc113_10: sw uses len, not len/2
      if (temp)
      {
         type = REG_BINARY;
         ret = RegQueryValueEx(handle,TEXT("Value"),NULL,&type,ARRAYOBJ_START(temp), &len);
      }
      if (!bin) // if not binary, create a string and set the chars to our created buffer
      {
         if ((target = createObject(currentContext, "java.lang.String")) != null)
            String_chars(target) = temp;
         setObjectLock(temp, UNLOCKED);
      }
      RegCloseKey(handle);
   }
   return target;
#endif
   return 0;
}

static TCObject getAppSettings(Context currentContext, uint32 crid, bool bin, bool isHKLM) // guich@580_21: use hklm if for secret key
{
   TCObject o = null;
#if !defined WP8
#ifndef WINCE
   // guich@tc310: now we first look at the file, then at the registry
   char dest[MAX_PATHNAME];
   FILE* f;
   getSettingsFile(crid, bin, isHKLM, dest);
   f = fopen(dest, "rb");
   if (f)
   {
      TCObject temp = null, target = null;
      int32 len;

      fseek(f, 0, SEEK_END);
      len=ftell(f);
      fseek(f, 0, SEEK_SET);
      if (len == 0) // deleted?
         goto end;

      target = temp = bin ? createByteArray(currentContext, len) : createCharArray(currentContext, len/2); // guich@tc113_10: sw uses len, not len/2
      if (temp)
         fread(ARRAYOBJ_START(temp), len, 1, f);
      if (!bin) // if not binary, create a string and set the chars to our created buffer
      {
         if ((target = createObject(currentContext, "java.lang.String")) != null)
            String_chars(target) = temp;
         setObjectLock(temp, UNLOCKED);
      }
      o = target;
end:
      fclose(f);
   }
   if (o == null)
#endif // wince
      o = getAppSettingsTry(currentContext, crid, bin, isHKLM); // first test at TC
#endif
   return o;
}

static bool queryRegistry(HKEY key, TCHAR *subkey, TCHAR *name, char *buf, uint32 size)
{
	// WP8 app should not use registry
#if !defined WP8
   HKEY handle;
   long ret;
   DWORD type;

   ret = RegOpenKeyEx(key,subkey,0,KEY_READ,&handle);
   if (ret == NO_ERROR) // error or success? :-P
   {
      type = REG_DWORD;
      RegQueryValueEx(handle,name,NULL,&type,(uint8*)buf,(LPDWORD) &size);
      RegCloseKey(handle);
      return true;
   }
#endif
   return false;
}

#ifdef WINCE
typedef struct _DEVICE_ID {
  DWORD dwSize;
  DWORD dwPresetIDOffset;
  DWORD dwPresetIDBytes;
  DWORD dwPlatformIDOffset;
  DWORD dwPlatformIDBytes;
} DEVICE_ID, *PDEVICE_ID;
#include <winioctl.h>
extern BOOL KernelIoControl(DWORD dwIoControlCode, LPVOID lpInBuf, DWORD nInBufSize, LPVOID lpOutBuf, DWORD nOutBufSize, LPDWORD lpBytesReturned);
#define IOCTL_HAL_REBOOT CTL_CODE(FILE_DEVICE_HAL, 15, METHOD_BUFFERED, FILE_ANY_ACCESS)
#define IOCTL_HAL_GET_DEVICEID CTL_CODE(FILE_DEVICE_HAL, 21, METHOD_BUFFERED, FILE_ANY_ACCESS)
#if !(defined(WIN32_PLATFORM_HPCPRO) && _WIN32_WCE == 211)
  #define HAS_SIP
  #include <sip.h>
#endif
#if _WIN32_WCE >= 300 && !defined(WIN32_PLATFORM_HPC2000)
  #include <Aygshell.h>
  #pragma comment( lib, "aygshell" )   // Link Pocket PC lib for menubar
#endif

static void GetSerialNumberPocketPC2002(CharP buf) // see http://msdn.microsoft.com/library/default.asp?url=/library/en-us/dnnetcomp/html/retrievedeviceid.asp - guich@567_20: fixed this routine
{
    char OutputBuffer[256];
    int32 BytesReturned=0;
    int32 PresetIDOffset;
    int32 PlatformIDOffset;
    int32 PlatformIDSize;
    char *c=buf;
    int i;
	int32 outSize;
    *buf = 0;

    xmemzero(OutputBuffer, 256);
    ((DEVICE_ID*)OutputBuffer)->dwSize = 256;
    if (!KernelIoControl(IOCTL_HAL_GET_DEVICEID, null, 0, OutputBuffer, 256, &outSize)) //flsobral@tc115_73: bug fix for Symbol/Motorola MC3090 - this function call fails if the last parameter (WHICH IS OPTIONAL!) is set to null.
       return;

    // Examine the OutputBuffer byte array to find the start of the
    // Preset ID and Platform ID, as well as the size of the
    // PlatformID.
    // PresetIDOffset - The number of bytes the preset ID is offset
    //                  from the beginning of the structure
    // PlatformIDOffset - The number of bytes the platform ID is
    //                    offset from the beginning of the structure
    // PlatformIDSize - The number of bytes used to store the
    //                  platform ID
    // Use BitConverter.ToInt32() to convert from byte[] to int
    PresetIDOffset = ((DEVICE_ID*)OutputBuffer)->dwPresetIDOffset;
    PlatformIDOffset = ((DEVICE_ID*)OutputBuffer)->dwPlatformIDOffset;
    PlatformIDSize = ((DEVICE_ID*)OutputBuffer)->dwPlatformIDBytes;
    if (PresetIDOffset < 0 || PlatformIDOffset < 0 || PlatformIDSize < 0)
       return; // sometimes the KernelIoControl function does not get valid results

    // Convert the Preset ID segments into a string so they can be
    // displayed easily.
    {
       unsigned char *p = &OutputBuffer[0]+PresetIDOffset;
       xstrprintf(buf,"%02X%02X%02X%02X-%02X%02X-%02X%02X-%02X%02X-", *(p+0),*(p+1),*(p+2),*(p+3),*(p+4),*(p+5),*(p+6),*(p+7),*(p+8),*(p+9)); // guich@tc100b5_13: refactored to use the values directly
    }

    // Break the Platform ID down into 2-digit hexadecimal numbers
    // and append them to the Preset ID. This will result in a
    // string-formatted Device ID
    if ((PlatformIDSize*2 + 24) < 128) // buffer length is 128
    {
       c += 24;
       for (i = PlatformIDOffset; i < (PlatformIDOffset + PlatformIDSize); i++, c += 2)
           xstrprintf(c,"%02X", (int32)(OutputBuffer[i] & 0xFF));
       *c = 0;
    }
    else
    {
       int32 charLen = xstrlen(&OutputBuffer[PlatformIDOffset]);
       int32 wcharLen = JCharPLen(&OutputBuffer[PlatformIDOffset]);
       if (charLen == 1 && wcharLen > 1 && wcharLen < 100)
          JCharP2CharPBuf((JCharP) &OutputBuffer[PlatformIDOffset], wcharLen, buf + 24);
       else if (charLen > 1 && charLen < 100)
          xstrcpy(buf + 24, &OutputBuffer[PlatformIDOffset]);
       else
          buf[23] = 0; // ignore PlatformID if it's too large.
    }
}

static void GetSerialNumberPocketPC2000(CharP out) // see http://www.pocketpcdn.com/articles/serial_number.html
{
   // Start CreateAssetFile.exe
   HANDLE hInFile;
   DWORD dwBytesRead;
   PROCESS_INFORMATION pi;
   TCHAR strSN[65];

   // Read data from cpqAssetData.dat file
   hInFile = CreateFile(TEXT("\\windows\\cpqAssetData.dat"), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0); // guich@567_20: changed OPEN_ALWAYS to OPEN_EXISTING, otherwise a 0-length file will be created and the correct file will never be created
   if (hInFile == INVALID_HANDLE_VALUE) // still does not exist?
   {
      if (!CreateProcess(TEXT("\\windows\\CreateAssetFile.exe"),
         NULL, NULL, NULL, FALSE, 0, NULL, NULL, NULL, &pi))
         return;

      // Wait until CreateAssetFile.exe will be finished
      WaitForSingleObject(pi.hProcess, INFINITE);
      hInFile = CreateFile(TEXT("\\windows\\cpqAssetData.dat"), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
      if (hInFile == INVALID_HANDLE_VALUE) // not created? fail
         return;
   }

   SetFilePointer(hInFile, 976, NULL, FILE_BEGIN);
   memset(strSN, 0, 64 * sizeof(TCHAR));
   ReadFile(hInFile, &strSN, 64, &dwBytesRead, NULL);
   CloseHandle(hInFile);
   JCharP2CharPBuf(strSN, -1, out);
}

static void GetSerialNumberSymbol(CharP out)
{
   HINSTANCE dll;
   typedef BYTE UNITID[8];
   typedef UNITID FAR * LPUNITID;
   typedef DWORD (__stdcall *RCM_GetUniqueUnitIdProc)(LPUNITID lpUnitId);
   RCM_GetUniqueUnitIdProc RCM_GetUniqueUnitId;
   UNITID var;
   xmemzero(&var,sizeof(var));

   if ((dll = LoadLibrary(TEXT("RcmAPI32.dll"))) == null)
      return;

   if ((RCM_GetUniqueUnitId = (RCM_GetUniqueUnitIdProc) GetProcAddress(dll, TEXT("RCM_GetUniqueUnitId"))) != null)
   {
      int err = RCM_GetUniqueUnitId(&var);
      if (err == 0)
      {
         int i;
         char* s = out;
         *s++ = '0';
         *s++ = 'x';
         for (i = 0; i < 8; i++, s+=2)
            int2hex(var[i],2,s);
         *s = 0;
      }
   }
   FreeLibrary(dll);
}

#endif

bool checkWindowsMobile()
{
#ifdef WINCE
   HKEY key;
   TCHAR wcbuf[MAX_PATH+1];
   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("Software\\Microsoft\\Today"), 0, 0, &key) == NO_ERROR)
   {
      RegCloseKey(key);
      return true; // flsobral@tc110_37: PDA running Windows Mobile
   }
   if (SystemParametersInfo(SPI_GETPLATFORMTYPE, MAX_PATH, wcbuf, 0) && lstrcmp(wcbuf, TEXT("SmartPhone")) == 0)
      return true; // flsobral@tc110_37: Smartphone running Windows Mobile
   return false;
#else
   return false;
#endif
}

bool hasVirtualKeyboard()
{
#if defined (WP8)
	return true;
#else
 #if defined (WINCE) && _WIN32_WCE >= 300
   if (SipStatus() == SIP_STATUS_AVAILABLE)
      return true;
 #endif
   return false;
#endif
}

void CALLBACK lineCallbackFunc(DWORD dwDevice, DWORD dwMsg, DWORD dwCallbackInstance, DWORD dwParam1, DWORD dwParam2, DWORD dwParam3)
{
}

static void fillIMEI()
{
#if defined (WINCE) && _WIN32_WCE >= 300
   typedef LONG (__stdcall *lineGetGeneralInfoProc)( HLINE, LPLINEGENERALINFO );
   lineGetGeneralInfoProc procLineGetGeneralInfo;

   DWORD dwNumDevs;
   DWORD dwAPIVersion;
   HLINEAPP hLineApp;
   HLINE hLine;
   LINEEXTENSIONID lineExtensionID;
   LPLINEGENERALINFO lineGeneralInfoP;
   int8 lineGeneralInfo[1024];
   TCHAR imeiT[20];
   boolean disablePhone = false;
   int32 retryCount;

   if ((procLineGetGeneralInfo = (lineGetGeneralInfoProc) GetProcAddress(cellcoreDll, _T("lineGetGeneralInfo"))) == null)
      return;

   if (RdIsSupported(PHONE) && RdGetState(PHONE) == RADIO_STATE_DISABLED)
   {
      RdSetState(PHONE, RADIO_STATE_ENABLED);
      disablePhone = true;
   }

   if (!lineInitialize(&hLineApp, 0, lineCallbackFunc, null, &dwNumDevs))
   {
      if(!lineNegotiateAPIVersion(hLineApp, 0, 0x10004, 0x20000, &dwAPIVersion, &lineExtensionID))
      {
         if (!lineOpen(hLineApp, 0, &hLine, dwAPIVersion, 0, null, LINECALLPRIVILEGE_MONITOR , 0, null))
         {
            tzero(lineGeneralInfo);
            lineGeneralInfoP = (LPLINEGENERALINFO) lineGeneralInfo;

            for (retryCount = 0 ; retryCount < 5 ; retryCount++)
            {
               lineGeneralInfoP->dwTotalSize = sizeof(lineGeneralInfo);
               if (!procLineGetGeneralInfo(hLine, lineGeneralInfoP) && lineGeneralInfoP->dwSerialNumberSize > 0)
               {
                  xmemmove(imeiT, ((unsigned short *)(lineGeneralInfoP) + lineGeneralInfoP->dwSerialNumberOffset/2), lineGeneralInfoP->dwSerialNumberSize);
                  imeiT[lineGeneralInfoP->dwSerialNumberSize/2] = 0;
                  TCHARP2CharPBuf(imeiT, imei);
                  break;
               }
            }
            if (!*imei)
               emptyImei = true;
            lineClose(hLine);
         }
      }
      lineShutdown(hLineApp);
   }

   if (disablePhone)
      RdSetState(PHONE, RADIO_STATE_DISABLED);
#endif //WINCE
}

static void fillICCID() // guich@tc126_75
{
#if defined (WINCE) && _WIN32_WCE >= 300
   typedef HANDLE HSIM, *LPHSIM;
   typedef HRESULT (*SimInitializeProc) (DWORD dwFlags, void* lpfnCallBack, DWORD dwParam, LPHSIM lphSim);
   typedef HRESULT (*SimDeinitializeProc) (HSIM hSim);
   typedef HRESULT (*SimReadRecordProc) (HSIM hSim, DWORD dwAddress, DWORD dwRecordType, DWORD dwIndex, LPBYTE lpData, DWORD dwBufferSize, LPDWORD lpdwBytesRead);
   HSIM hsim;
   uint8 buf[15];
   int len;            

   SimInitializeProc SimInitialize;
   SimDeinitializeProc SimDeinitialize;
   SimReadRecordProc SimReadRecord;

   #define EF_ICCID 0x2FE2

   SimInitialize = (SimInitializeProc)GetProcAddress(cellcoreDll, TEXT("SimInitialize"));
   SimDeinitialize = (SimDeinitializeProc)GetProcAddress(cellcoreDll, TEXT("SimDeinitialize"));
   SimReadRecord = (SimReadRecordProc)GetProcAddress(cellcoreDll, TEXT("SimReadRecord"));

   if (SimInitialize != null && SimDeinitialize != null && SimReadRecord != null)
      if (SimInitialize(0,NULL,0,&hsim) == S_OK)
      {
         if (SimReadRecord(hsim,EF_ICCID,1,0,buf,sizeof(buf),&len) == S_OK)
         {
            int index = 0,i;
            for(i=0;i<len;i++)
            {
               iccid[index++] = ((buf[i] ) & 0xF) +'0';
               iccid[index++] = ((buf[i]>>4) & 0xF) +'0';
            }
            iccid[index] = 0;
         }
         SimDeinitialize(hsim);
      }
#endif //WINCE
}

static bool hasKeyboard()
{
	// jeffque: WP8 never has a keyboard.
#if !defined WP8
   int32 ret;
   return queryRegistry(HKEY_CURRENT_USER, TEXT("Software\\Microsoft\\Shell"), TEXT("HasKeyboard"), (char*)&ret, sizeof(ret)) && ret == 1;
#else
	return false;
#endif
}

#ifndef WINCE
void GetMacAddress(char* serialBuf) // guich@tc110_96
{
   HINSTANCE dll;
   typedef struct {unsigned long Data1;  unsigned short Data2;  unsigned short Data3;  unsigned char Data4[8];} UUID;
   typedef void (__stdcall *UuidCreateSequentialProc)(UUID*);
   UuidCreateSequentialProc UuidCreateSequential;
   UUID uid;

   if ((dll = LoadLibrary(TEXT("rpcrt4.dll"))) == null)
      return;

   if ((UuidCreateSequential = (UuidCreateSequentialProc) GetProcAddress(dll, "UuidCreateSequential")) != null)
   {
      UuidCreateSequential(&uid); // mac address is the last 6 bytes of Data4, but we use also the other ones to make the key bigger
      xstrprintf(serialBuf, "%02X%02X%02X%02X%02X%02X",(int)uid.Data4[2],(int)uid.Data4[3],(int)uid.Data4[4],(int)uid.Data4[5],(int)uid.Data4[6],(int)uid.Data4[7]); // guich@tc120_24: use only the last 6 digits
   }
   FreeLibrary(dll);
}

// header wbemcli.h not defined in WP8, looks like that
#if !defined WP8
int GetMacAddressWMI(char* serialBuf)
{
   HRESULT hres;
   IWbemLocator *pLoc = NULL;
   IWbemServices *pCIMV2 = NULL;
   IWbemServices *pWMI = NULL;
   IEnumWbemClassObject* pEnumAdapters = NULL;
   IEnumWbemClassObject* pEnumAddresses = NULL;
   IWbemClassObject *pAdapterClasses = NULL;
   IWbemClassObject *pAddressClasses = NULL;
   ULONG ulFound = 0;
   // Namespaces are passed to COM in BSTRs.
   BSTR namespaceCIMV2 = SysAllocString(L"ROOT\\CIMV2");
   BSTR namespaceWMI = SysAllocString(L"ROOT\\WMI");
   BSTR bstrQueryLanguage = SysAllocString(L"WQL");
   BSTR bstrQueryAdapters = SysAllocString(L"select * from Win32_NetworkAdapter WHERE Manufacturer!='Microsoft' and ServiceName!='VMnetAdapter' and ProductName != 'RAS Async Adapter' and NOT Productname LIKE '%Bluetooth%'");
   BSTR bstrPropIndex = SysAllocString(L"Index");
   BSTR bstrPropAdapterName = SysAllocString(L"Name");
   BSTR bstrPropPermanentAddress = SysAllocString(L"NdisPermanentAddress");
   BSTR bstrPropAddress = SysAllocString(L"Address");
   VARIANT propertyIndex;
   VARIANT propertyAdapterName;
   VARIANT propertyPermanentAddress;
   VARIANT propertyAddress;

   // Step 1: Initialize COM.
   if (FAILED(hres = CoInitializeEx(0, COINIT_MULTITHREADED)))
      return hres; //Failed to initialize COM library.

   // Step 2: Set general COM security levels
   // Note: If you are using Windows 2000, you need to specify the default authentication credentials for a 
   // user by using a SOLE_AUTHENTICATION_LIST structure in the pAuthList parameter of CoInitializeSecurity
   if (FAILED(hres = CoInitializeSecurity( 
                        NULL,                        // Access permission
                        -1,                          // COM authentication 
                        NULL,                        // Authentication services 
                        NULL,                        // Reserved 
                        RPC_C_AUTHN_LEVEL_DEFAULT,   // Default authentication  
                        RPC_C_IMP_LEVEL_IMPERSONATE, // Default Impersonation   
                        NULL,                        // Authentication info 
                        EOAC_NONE,                   // Additional capabilities  
                        NULL                         // Reserved 
      )))
      goto cleanup; //Failed to initialize security.

   // Step 3: Obtain the initial locator to WMI
   if (FAILED(hres = CoCreateInstance( 
                        &CLSID_WbemLocator2,              
                        0,  
                        CLSCTX_INPROC_SERVER,  
                        &IID_IWbemLocator2, 
                        (LPVOID *) &pLoc
      )))
      goto cleanup; //Failed to create IWbemLocator object.
 
   // Step 4: Connect to WMI through the IWbemLocator::ConnectServer method
   // Connect to the root\cimv2 namespace with the current user and obtain pointer pCIMV2 to make IWbemServices calls.
   if (FAILED(hres = IWbemLocator_ConnectServer(
                        pLoc,
                        namespaceCIMV2 ,                                             // Object path of WMI namespace
                        NULL,    // NULL means current account, for simplicity.      // User name. NULL = current user
                        NULL,    // NULL means current password, for simplicity.     // User password. NULL = current
                        0L,      // locale                                           // Locale. NULL indicates current
                        0L,      // securityFlags                                    // Security flags.
                        NULL,    // authority (domain for NTLM)                      // Authority (e.g. Kerberos)
                        NULL,    // context                                          // Context object
                        &pCIMV2  // Returned IWbemServices.                          // pointer to IWbemServices proxy
      )))
      goto cleanup; //Could not connect.
   // Connect to the root\wminamespace with the current user and obtain pointer pWMI to make IWbemServices calls.
   if (FAILED(hres = IWbemLocator_ConnectServer(
                        pLoc,
                        namespaceWMI,                                                // Object path of WMI namespace
                        NULL,    // NULL means current account, for simplicity.      // User name. NULL = current user
                        NULL,    // NULL means current password, for simplicity.     // User password. NULL = current
                        0L,      // locale                                           // Locale. NULL indicates current
                        0L,      // securityFlags                                    // Security flags.
                        NULL,    // authority (domain for NTLM)                      // Authority (e.g. Kerberos)
                        NULL,    // context                                          // Context object
                        &pWMI    // Returned IWbemServices.                          // pointer to IWbemServices proxy
      )))
      goto cleanup; //Could not connect.

   // Step 5: Set security levels on the proxy
   if (FAILED(hres = CoSetProxyBlanket( 
                        (IUnknown *)pCIMV2,          // Indicates the proxy to set
                        RPC_C_AUTHN_WINNT,           // RPC_C_AUTHN_xxx
                        RPC_C_AUTHZ_NONE,            // RPC_C_AUTHZ_xxx
                        NULL,                        // Server principal name
                        RPC_C_AUTHN_LEVEL_CALL,      // RPC_C_AUTHN_LEVEL_xxx
                        RPC_C_IMP_LEVEL_IMPERSONATE, // RPC_C_IMP_LEVEL_xxx
                        NULL,                        // client identity
                        EOAC_NONE                    // proxy capabilities
      )))
      goto cleanup; //Could not set proxy blanket.
   if (FAILED(hres = CoSetProxyBlanket(
                        (IUnknown *)pWMI,            // Indicates the proxy to set
                        RPC_C_AUTHN_WINNT,           // RPC_C_AUTHN_xxx
                        RPC_C_AUTHZ_NONE,            // RPC_C_AUTHZ_xxx
                        NULL,                        // Server principal name
                        RPC_C_AUTHN_LEVEL_CALL,      // RPC_C_AUTHN_LEVEL_xxx
                        RPC_C_IMP_LEVEL_IMPERSONATE, // RPC_C_IMP_LEVEL_xxx
                        NULL,                        // client identity
                        EOAC_NONE                    // proxy capabilities
      )))
      goto cleanup; //Could not set proxy blanket.

   // Step 6: Use the IWbemServices pointer to make requests of WMI
   if (FAILED(hres = IWbemServices_ExecQuery(
                        pCIMV2,
                        bstrQueryLanguage,  
                        bstrQueryAdapters,
                        WBEM_FLAG_FORWARD_ONLY | WBEM_FLAG_RETURN_IMMEDIATELY,  
                        NULL, 
                        &pEnumAdapters
      )))
      goto cleanup; //Query for operating system name failed.
    
   // Step 7: Get the data from the query in step 6
   while ((hres = IEnumWbemClassObject_Next(
                     pEnumAdapters,
                     30000,     // flsobral@1.29.1: increased timeout to thirty seconds for netbooks with Windows 7 starter.
                     1,         // return just one class.
                     &pAdapterClasses,  // pointer to class.
                     &ulFound   // Number of classes returned.
                     )) == WBEM_S_NO_ERROR && ulFound == 1)
   {
      VariantClear(&propertyIndex);
      VariantInit(&propertyIndex);
      propertyIndex.bstrVal = null;

      hres = IWbemClassObject_Get(
               pAdapterClasses,
               bstrPropIndex,       // property name 
               0L,                  // Reserved, must be zero.
               &propertyIndex,      // property value(class name) returned.
               NULL,                // CIM type not needed.
               NULL);               // Flavor not needed.

      if (hres == WBEM_S_NO_ERROR && propertyIndex.bstrVal != null)
      {
         VariantClear(&propertyAdapterName);
         VariantInit(&propertyAdapterName);
         propertyAdapterName.bstrVal = null;

         hres = IWbemClassObject_Get(
                  pAdapterClasses,
                  bstrPropAdapterName,    // property name
                  0L,                     // Reserved, must be zero.
                  &propertyAdapterName,   // property value(class name) returned.
                  NULL,                   // CIM type not needed.
                  NULL);                  // Flavor not needed.

         if (hres == WBEM_S_NO_ERROR && propertyAdapterName.bstrVal != null)
         {
            OLECHAR aux[256];
            BSTR bstrQueryAddresses;

            wsprintfW(aux, L"select * from MSNdis_EthernetPermanentAddress where InstanceName='%s'", propertyAdapterName.bstrVal);
            bstrQueryAddresses = SysAllocString(aux);
            if (!FAILED(hres = IWbemServices_ExecQuery(
                                 pWMI,
                                 bstrQueryLanguage,
                                 bstrQueryAddresses,
                                 WBEM_FLAG_FORWARD_ONLY | WBEM_FLAG_RETURN_IMMEDIATELY,
                                 NULL,
                                 &pEnumAddresses)))
            {
               if ((hres = IEnumWbemClassObject_Next(pEnumAddresses, 30000, 1, &pAddressClasses, &ulFound)) == WBEM_S_NO_ERROR && ulFound == 1)
               {
                  VariantClear(&propertyPermanentAddress);
                  VariantInit(&propertyPermanentAddress);
                  propertyPermanentAddress.bstrVal = null;

                  if ((hres = IWbemClassObject_Get(pAddressClasses, bstrPropPermanentAddress, 0L, &propertyPermanentAddress, NULL, NULL)) == WBEM_S_NO_ERROR)
                  {
                     IWbemClassObject *pNetworkAddress = NULL;
                     char permAddress[6];
                     long index;
                     IID riid = IID_IWbemClassObject2;

                     if ((hres = IWbemLocator_QueryInterface(propertyPermanentAddress.punkVal, &riid, &pNetworkAddress)) == WBEM_S_NO_ERROR)
                     {
                        VariantClear(&propertyAddress);
                        VariantInit(&propertyAddress);
                        propertyAddress.bstrVal = null;

                        if ((hres = IWbemClassObject_Get(pNetworkAddress, bstrPropAddress, 0L, &propertyAddress, NULL, NULL)) == WBEM_S_NO_ERROR)
                        {
                           for (index = 0; index < 6; index++)
                              SafeArrayGetElement(propertyAddress.parray, &index, &permAddress[index]);
                           if (*serialBuf != 0)
                              xstrcat(serialBuf, "-");
                           xstrprintf(serialBuf + xstrlen(serialBuf), "%02X%02X%02X%02X%02X%02X",(int)(permAddress[0] & 0xFF),(int)(permAddress[1] & 0xFF),(int)(permAddress[2] & 0xFF),(int)(permAddress[3] & 0xFF),(int)(permAddress[4] & 0xFF),(int)(permAddress[5] & 0xFF));
                        }
                        IWbemClassObject_Release(pNetworkAddress);
                     }
                  }
                  IWbemClassObject_Release(pAddressClasses);
               }
               IEnumWbemClassObject_Release(pEnumAddresses);
            }
            SysFreeString(bstrQueryAddresses);
         }
      }
   }
   if (xstrlen(serialBuf) > 0)
      hres = NO_ERROR;
cleanup:
   if (pEnumAdapters != null)
      IEnumWbemClassObject_Release(pEnumAdapters);
   if (pAdapterClasses != null)
      IWbemClassObject_Release(pAdapterClasses);
   if (pWMI != null)
      IWbemServices_Release(pCIMV2);
   if (pCIMV2 != null)
      IWbemServices_Release(pWMI);
   if (pLoc != null)
      IWbemLocator_Release(pLoc);
   CoUninitialize();

   SysFreeString(namespaceCIMV2);
   SysFreeString(namespaceWMI);
   SysFreeString(bstrQueryLanguage);
   SysFreeString(bstrQueryAdapters);
   SysFreeString(bstrPropIndex);
   SysFreeString(bstrPropAdapterName);
   SysFreeString(bstrPropPermanentAddress);
   SysFreeString(bstrPropAddress);

   return hres;
}
#endif
#endif

void updateDaylightSavings(Context currentContext)
{
   DWORD ret;
   TIME_ZONE_INFORMATION tzi;
   char timeZone[128];

   ret = GetTimeZoneInformation(&tzi); // even if TIME_ZONE_ID_UNKNOWN is returned, the fields are filled correctly
   (*tcSettings.timeZoneMinutesPtr) = -tzi.Bias; // for gmt-3 it returns 180
   (*tcSettings.timeZonePtr) = *tcSettings.timeZoneMinutesPtr / 60; // divide by 60 to get -3.
   (*tcSettings.daylightSavingsPtr) = ret == TIME_ZONE_ID_DAYLIGHT; // guich@tc100b5_3
   if (*tcSettings.daylightSavingsPtr)
      (*tcSettings.daylightSavingsMinutesPtr) = -tzi.DaylightBias;

   if (ret == TIME_ZONE_ID_STANDARD) //flsobral@tc115_54: added field Settings.timeZoneStr
      JCharP2CharPBuf(tzi.StandardName, JCharPLen(tzi.StandardName), timeZone);
   else
      JCharP2CharPBuf(tzi.DaylightName, JCharPLen(tzi.DaylightName), timeZone);
   setObjectLock(*getStaticFieldObject(currentContext, settingsClass, "timeZoneStr") = createStringObjectFromCharP(currentContext, timeZone, -1), UNLOCKED);
}

bool fillSettings(Context currentContext) // http://msdn.microsoft.com/en-us/windowsmobile/bb794697.aspx
{
   OSVERSIONINFO osvi;
   TCHAR wcbuf[MAX_PATH+1];
#if !defined (WINCE)
   int32 len;
#if !defined(WP8)
   HRESULT hres;
#endif
#endif

#ifdef WP8
   *(tcSettings.romVersionPtr) = getOSVersion();
   getRomSerialNumberCPP(romSerialNumber);
   getDeviceIdCPP(deviceId);
   *(tcSettings.virtualKeyboardPtr) = isVirtualKeyboard();
   platform = "WindowsPhone";
   //   xstrcpy(deviceId, GetDisplayNameWP8());
#else
   // OS version
   osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
   GetVersionEx(&osvi);
   *(tcSettings.romVersionPtr) = osvi.dwMajorVersion * 100 + osvi.dwMinorVersion; // 2 * 100 + 11 = 2.11
#endif

#ifdef WINCE
   romSerialNumber[0] = 0;
   if (*(tcSettings.romVersionPtr) >= 400)
   {
      GetSerialNumberPocketPC2002(romSerialNumber);
      if (romSerialNumber[0] == 0)
         GetSerialNumberSymbol(romSerialNumber);
   }
   else
      GetSerialNumberPocketPC2000(romSerialNumber);

   if (SystemParametersInfo(SPI_GETOEMINFO, MAX_PATH, wcbuf, 0)) // guich@568_2
   {
      isMotoQ = tcscmp(wcbuf,TEXT("MotoQ")) == 0;
      TCHARP2CharPBuf(wcbuf, deviceId);
      *(tcSettings.virtualKeyboardPtr) = !hasKeyboard(); // guich@584_3
   }
   else
      *(tcSettings.virtualKeyboardPtr) = true;

   if (*(tcSettings.romVersionPtr) < 300)
      platform = "WindowsCE";
   else
   {
      #ifdef HAS_SIP
      *(tcSettings.keyboardFocusTraversablePtr) = SipStatus() == SIP_STATUS_UNAVAILABLE; // guich@570_39
      #endif
      if (!isWindowsMobile) //flsobral@tc110_37: Our global was already initialized, so let's use it.
         platform = "PocketPC";
      else
      {      
         platform = "WindowsMobile";
         if (strEq(deviceId, "GT-I8000L") || strEq(deviceId, "GT-B7300B")) //flsobral@tc123_26: Samsung Omnia devices have a buggy touch screen.
            vkSettings.topGap = 20;
         else
            vkSettings.topGap = 0;         
      }
   }

   // get the name entered in the control panel
   if (queryRegistry(HKEY_CURRENT_USER, TEXT("ControlPanel\\Owner"), TEXT("Owner"), (char *)wcbuf, MAX_PATH*sizeof(TCHAR)))
      TCHARP2CharPBuf(wcbuf, userName);
#else
# ifndef WP8
   len = sizeof(deviceId);
   GetComputerName(deviceId,&len); // guich@568_2
   platform = "Win32";
   *(tcSettings.virtualKeyboardPtr) = GetSystemMetrics(SM_TABLETPC);
# endif

#if !defined WP8
   //use the mac address as the serial number
   hres = GetMacAddressWMI(romSerialNumber); // flsobral@tc126: first we try to retrieve the mac address using the WMI
   if (hres == WBEM_S_TIMEDOUT) // flsobral@tc129.1: give up if the operation failed after a timeout.
      debug("Unable to retrieve device registration information, please try again or contact support if the problem persists. (%X)", hres);
   else if (romSerialNumber[0] == 0)
   {
      if (*(tcSettings.romVersionPtr) < 501)
         GetMacAddress(romSerialNumber);
//      if (romSerialNumber[0] == 0)
//         xstrcpy(romSerialNumber, "unknown");
   }
#endif

 
#if !defined WP8 
   if (GetUserName(userName,&len) || // guich@568_3: better use a standard routine
      queryRegistry(HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer", "Logon User Name", userName, sizeof(userName)) || // first, try as a winnt machine
      queryRegistry(HKEY_LOCAL_MACHINE, "Network\\Logon", "Username", userName, sizeof(userName))) // else, try as on windows 98
      ;
#endif
#endif
   {
	   //XXX WP8 does not have GetDC or similar...
#if !defined WP8
      HDC hdc = GetDC(mainHWnd);
      *(tcSettings.deviceFontHeightPtr) = abs(12 * GetDeviceCaps(hdc, LOGPIXELSY) / 72);
      DeleteDC(hdc);
#else
	  *(tcSettings.deviceFontHeightPtr) = (int32) getFontHeightCPP();
#endif
   }
#if defined WP8
#define GetLocaleInfo_COMPAT GetLocaleInfoEx
#define LOCALE_USER_DEFAULT_COMPAT LOCALE_NAME_USER_DEFAULT
#else
#define GetLocaleInfo_COMPAT GetLocaleInfo
#define LOCALE_USER_DEFAULT_COMPAT LOCALE_USER_DEFAULT
#endif
   *(tcSettings.decimalSeparatorPtr)     = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_SDECIMAL,wcbuf,2) ? (char)wcbuf[0] : '.';
   *(tcSettings.thousandsSeparatorPtr)   = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_STHOUSAND,wcbuf,2) ? (char)wcbuf[0] : ',';
   if (*(tcSettings.decimalSeparatorPtr) == *(tcSettings.thousandsSeparatorPtr)) // guich@421_12: make sure they differ
      *(tcSettings.decimalSeparatorPtr)  = *(tcSettings.thousandsSeparatorPtr)=='.' ? ',' : '.';
   *(tcSettings.dateSeparatorPtr)        = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_SDATE,wcbuf,2) ? (char)wcbuf[0] : '/';
   *(tcSettings.timeSeparatorPtr)        = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_STIME,wcbuf,2) ? (char)wcbuf[0] : ':';
   *(tcSettings.weekStartPtr)            = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_IFIRSTDAYOFWEEK,wcbuf,2) ? (((char)wcbuf[0]-'0' + 1) % 7) : 0; // for SW, 0 is sunday; for Win, 6 is sunday
   *(tcSettings.is24HourPtr)             = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_ITIME,wcbuf,2) ? wcbuf[0] == '1' : true;
   *(tcSettings.dateFormatPtr)           = GetLocaleInfo_COMPAT(LOCALE_USER_DEFAULT_COMPAT,LOCALE_IDATE,wcbuf,2) ? ((char)wcbuf[0]-'0'+1) : 1; // MDY, DMY, YMD

#if defined(WP8)
   *(tcSettings.virtualKeyboardPtr)      = true;
   *(tcSettings.fingerTouchPtr)          = true;
   *(tcSettings.unmovableSIP)            = true;
   //*(tcSettings.keyboardFocusTraversablePtr) = true;
#endif

   // guich@340_33: timezone and daylight savings
   updateDaylightSavings(currentContext);

   /*IMEI*/
   imei[0] = 0;
   iccid[0] = 0;
#if defined (WINCE) && _WIN32_WCE >= 300
   if (cellcoreDll != null)
   {
      fillIMEI();
      fillICCID();
   }
#endif //WINCE

   return true;
}

void saveSoundSettings()
{
#if defined (WINCE)
   WAVEOUTCAPS tWaveoutCaps;
   DWORD volume = 0;
   SNDFILEINFO sfi;

   if (SndGetSoundWM5(SND_EVENT_ALL, &sfi))
   {
      switch (sfi.sstType)
      {
         case SND_SOUNDTYPE_ON: soundSettings.volumeState = 1; break;
         case SND_SOUNDTYPE_VIBRATE: soundSettings.volumeState = -1; break;
         case SND_SOUNDTYPE_NONE: soundSettings.volumeState = 0; break;
      }
      if (sfi.sstType != SND_SOUNDTYPE_ON)
      {
         sfi.sstType = SND_SOUNDTYPE_ON;
         SndSetSoundWM5(SND_EVENT_ALL, &sfi);
      }
   }

   if (waveOutGetNumDevs() > 0
      && waveOutGetDevCaps(WAVE_MAPPER, &tWaveoutCaps, sizeof(tWaveoutCaps)) == MMSYSERR_NOERROR
      && tWaveoutCaps.dwSupport & WAVECAPS_VOLUME)
   {
      waveOutGetVolume((HWAVEOUT) WAVE_MAPPER, &volume);
      soundSettings.volume = volume;
   }

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 500)
   {
      if (soundSettings.volumeState < 1)
      {
         switch (soundSettings.volumeState)
         {
            case 0: sfi.sstType = SND_SOUNDTYPE_NONE; break;
            case -1: sfi.sstType = SND_SOUNDTYPE_VIBRATE; break;
         }
         SndSetSoundWM5(SND_EVENT_ALL, &sfi);
      }
   }
   else
      soundSettings.volumeState = volume > 0 ? 1 : 0;

   soundSettings.isSoundEnabled = (soundSettings.volumeState == 1);
   soundSettings.ringer = -1;
   soundSettings.ringerState = 0;
#endif //WINCE
}

void restoreSoundSettings()
{
#if defined (WINCE)
   WAVEOUTCAPS tWaveoutCaps;
   SNDFILEINFO sfi;

   if (SndGetSoundWM5(SND_EVENT_ALL, &sfi) && sfi.sstType != SND_SOUNDTYPE_ON)
   {
      sfi.sstType = SND_SOUNDTYPE_ON;
      SndSetSoundWM5(SND_EVENT_ALL, &sfi);
   }

   if (waveOutGetNumDevs() > 0
      && waveOutGetDevCaps(WAVE_MAPPER, &tWaveoutCaps, sizeof(tWaveoutCaps)) == MMSYSERR_NOERROR
      && (tWaveoutCaps.dwSupport & WAVECAPS_VOLUME) != 0)
   {
      waveOutSetVolume((HWAVEOUT) WAVE_MAPPER, soundSettings.volume);
   }

   if (isWindowsMobile && *tcSettings.romVersionPtr >= 500 && soundSettings.volumeState != 1)
   {
      switch (soundSettings.volumeState)
      {
         case 0: sfi.sstType = SND_SOUNDTYPE_NONE; break;
         case -1: sfi.sstType = SND_SOUNDTYPE_VIBRATE; break;
      }
      SndSetSoundWM5(SND_EVENT_ALL, &sfi);
   }
#endif //WINCE
}

#if defined (WINCE) && _WIN32_WCE >= 300
CLSID keybSip;
int mustChangeSip;
static int verifyCurrentSip(IMENUMINFO *info)
{
   CLSID current;
   SipGetCurrentIM(&current);
   if (keybSip.Data1 == 0 && lstrcmp(info->szName,TEXT("Keyboard")) == 0)   // store the first sip
      keybSip = info->clsid;
   if (info->clsid.Data1 == current.Data1) // is this the current one?
   {
      if (lstrcmp(info->szName,TEXT("Transcriber")) == 0) // is it the transcriber?
         mustChangeSip = 1;
   }
   return 1;
}
static void initSipRect()
{
   // store the default rect
   SIPINFO si;
   int isNotDocked;
   xmemzero(&si, sizeof(SIPINFO));
   si.cbSize = sizeof (SIPINFO);
   SipGetInfo(&si); // must be before sip be hidden
   isNotDocked = (si.fdwFlags & SIPF_DOCKED) == 0; // guich@550_42: fixed sip getting bigger when it was visible on the sw app start

   SipShowIM(SIPF_OFF);
#ifdef SHFS_HIDESIPBUTTON
   if (_SHFullScreen != null) {
	_SHFullScreen(mainHWnd, SHFS_HIDESIPBUTTON); // only supported in 3.0
   }
#endif
   SipEnumIM(&verifyCurrentSip);
   if (mustChangeSip && keybSip.Data1 != 0)
   {
      SipSetCurrentIM(&keybSip); // go to the keyboard IM
      SipShowIM(SIPF_OFF);
      SipGetInfo(&si); // get info again
   }

   vkSettings.left   = si.rcSipRect.left;
   vkSettings.right  = si.rcSipRect.right;
   vkSettings.top    = si.rcSipRect.top;
   vkSettings.bottom = si.rcSipRect.bottom;
   // a floating sip has a caption area and a thick border, so we add it to sipRect.
   // a docked sip does not contain such area.
   if (isWindowsMobile && isNotDocked) // flsobral@tc113_24: Now also check if the device is Windows Mobile, because this was making the SIP get bigger than it should be on WinCE.
   { 
      vkSettings.bottom += GetSystemMetrics(SM_CYCAPTION);
      vkSettings.right -= vkSettings.left;
      vkSettings.left = 0;
   }
   //alert("left: %d, top: %d, right: %d, bottom: %d", vkSettings.left, vkSettings.top, vkSettings.right, vkSettings.bottom);
}
#endif

void saveVKSettings()
{
#if defined (WINCE) && _WIN32_WCE >= 300 && defined(WIN32_PLATFORM_PSPC)
   if (*tcSettings.virtualKeyboardPtr)
      initSipRect();
#endif //WINCE
}

void restoreVKSettings()
{
#if defined (WINCE) && _WIN32_WCE >= 300 && defined(WIN32_PLATFORM_PSPC)
   if (*tcSettings.virtualKeyboardPtr && vkSettings.changed)
   {
      CLSID Clsid;
      RECT sipRect;

      SipShowIM(SIPF_OFF);

      sipRect.bottom = vkSettings.bottom;
      sipRect.top    = vkSettings.top;
      sipRect.left   = vkSettings.left;
      sipRect.right  = vkSettings.right;

      SipSetDefaultRect(&sipRect);
      SipGetCurrentIM(&Clsid);
      SipSetCurrentIM(&Clsid);
      vkSettings.changed = false;
   }
#endif //WINCE
}
