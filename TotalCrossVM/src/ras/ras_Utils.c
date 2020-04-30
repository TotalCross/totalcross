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
#include "../axtls/crypto.h"

#if defined (WINCE)
 #include "../nm/io/device/RadioDevice.h"
 #include "../nm/io/device/win/RadioDevice_c.h"
#endif

static TCObject createInfo(Context currentContext)
{
   TCObject info = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.util.Hashtable");
   executeMethod(currentContext, getMethod(OBJ_CLASS(info), true, CONSTRUCTOR_NAME, 1, J_INT), info, 10);
   return info;
}

static bool putInfo(Context currentContext, TCObject info, CharP key, CharP val)
{
   TCObject infoKey;
   TCObject infoVal;

   if (info == null || key == null || val == null)
      throwException(currentContext, NullPointerException, "putInfo received a null pointer!");
   else
   {
      infoKey = createStringObjectFromCharP(currentContext, key, -1);
      infoVal = createStringObjectFromCharP(currentContext, val, -1);

      if (currentContext->thrownException == null)
      {
         executeMethod(currentContext, getMethod(OBJ_CLASS(info), true, "put", 2, "java.lang.Object", "java.lang.Object"), info, infoKey, infoVal);
         setObjectLock(infoKey, UNLOCKED);
         setObjectLock(infoVal, UNLOCKED);
      }
   }
   
   return currentContext->thrownException == null;
}

static bool putInfoObj(Context currentContext, TCObject info, CharP key, TCObject infoVal)
{
   TCObject infoKey;
   
   if (info == null || key == null || infoVal == null)
      throwException(currentContext, NullPointerException, "putInfoObj received a null pointer!");
   else
   {
      infoKey = createStringObjectFromCharP(currentContext, key, -1);
      if (currentContext->thrownException == null)
      {
         executeMethod(currentContext, getMethod(OBJ_CLASS(info), true, "put", 2, "java.lang.Object", "java.lang.Object"), info, infoKey, infoVal);
         setObjectLock(infoKey, UNLOCKED);         
      }
   }
   
   return currentContext->thrownException == null;
}

static void getArtificialHash(char *out)
{
   int32 v=0;
#ifdef WINCE
   HKEY handle;
   DWORD type = REG_DWORD;
   int32 size = 4;

   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("Software\\Drivers"), 0, KEY_READ, &handle) == NO_ERROR)
   {
      if (RegQueryValueEx(handle, TEXT("MonkeyArea"), NULL, &type, (byte*) &v, (LPDWORD) &size) != NO_ERROR) // never created?
      {
         // generate a new key - just the current timestamp
         v = (int32)GetTickCount();
         if (RegSetValueEx(handle, TEXT("MonkeyArea"), 0, REG_DWORD, (byte*) &v, (DWORD) 4) != NO_ERROR)
            v = 0;
      }
      RegCloseKey(handle);
   }
#endif
   if (v != 0)
      xstrprintf(out, "%lX", (long)v);
}

enum
{
   GDHERR_IMEI             = 1,
   GDHERR_ARTIFICIAL_HASH  = 2,
   GDHERR_OOM              = 3,
   GDHERR_EXCEPTION        = 4,
};

static int32 getDeviceHash(Context currentContext, CharP* deviceHash)
{
   MD5_CTX ctx;
   char serial[128],imei[32],deviceId[64];
   int32 notFound = 0;
   TCObject res, out;
   getDeviceId(deviceId); //flsobral@tc126: added "MOTOROLA MC55" and "Intermec CN3"
   
   MD5_Init(&ctx);
   getImei(imei);
#ifdef ANDROID
   if (__system_property_get("ro.serialno",serial) <= 0)
      serial[0] = 0;
   if (serial[0] == 0) // no serial? try the java way.
      getRomSerialNumber(serial);
   if (strEq(deviceId, "LGE LG-P698f") || strEq(deviceId, "unknown generic")) // android dual sim phone
   {
      if (serial[0] != 0) // do not use IMEI if the serial is available.
         imei[0] = 0;
   }
#else
   getRomSerialNumber(serial);
#endif

   if (*serial)
   {
#if defined (WIN32) && !defined (WINCE)
      int32 serialLen = xstrlen(serial);
      if (serialLen > 12) // use only the first mac address to keep the hash the same as the previous versions.
         MD5_Update(&ctx, serial, 12);
      else
         MD5_Update(&ctx, serial, serialLen);
#else
      MD5_Update(&ctx, (uint8*)serial, xstrlen(serial));
#endif
   }
   else notFound++;

#if defined (WINCE)
   CharPToUpper(deviceId);
   if (!strEq(deviceId, "PALM TREO 750") && !strEqn(deviceId, "MOTOROLA MC", 11) && !strEqn(deviceId, "SYMBOL MC", 9) && !strEq(deviceId, "INTERMEC CN3")) // flsobral@tc122: never use IMEI on these device because it is not available when the device is on airplane mode. (phone off) - guich@tc136: skip all Motorola scanners, changed || to &&
#endif
   {
      if (*imei)
         MD5_Update(&ctx, (uint8*)imei, xstrlen(imei));
#if defined (WINCE)
      else if (RdIsSupported(PHONE))
         return GDHERR_IMEI;
#endif
      else notFound++;
   }

   if (notFound == 2) // no information? use an artificial hash
   {  
      serial[0] = 0;
      getArtificialHash(serial);
      if (*serial)
         MD5_Update(&ctx, (uint8*)serial, xstrlen(serial));
      else
         return GDHERR_ARTIFICIAL_HASH;
   }

   if ((out = createByteArray(currentContext, MD5_SIZE)) == null)
      return GDHERR_OOM;

   MD5_Final(ARRAYOBJ_START(out), &ctx);

   res = executeMethod(currentContext, getMethod(loadClass(currentContext, "totalcross.sys.Convert", true), true, "bytesToHexString", 1, BYTE_ARRAY), out).asObj;
   setObjectLock(out, UNLOCKED);
   if (currentContext->thrownException != null)
      return GDHERR_EXCEPTION;

   *deviceHash = JCharP2CharP(String_charsStart(res), String_charsLen(res));
   return NO_ERROR;
}

//////////////////////////////////////////////////////////////////////////
TC_API void rU_getConfigInfo(NMParams p) // ras/Utils native public static totalcross.util.Hashtable getConfigInfo();
{
//   TCObject obj = p->obj[0];
   TCObject info = createInfo(p->currentContext);

   putInfo(p->currentContext, info, "SERVER_HOST", "www.superwaba.net");
   putInfo(p->currentContext, info, "SERVER_PORT", "6666");
   putInfo(p->currentContext, info, "SERVER_SOCK_PARAMS", "");

   p->retO = info;
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void rU_getProductInfo(NMParams p) // ras/Utils native public static totalcross.util.Hashtable getProductInfo();
{
//   TCObject obj = p->obj[0];
   TCObject info = createInfo(p->currentContext);
   IntBuf buf;
   char buffer[64];
   TCObject strObj;

   putInfo(p->currentContext, info, "COMPILATION_DATE", int2str(getCompilationDate() ^ COMPILATION_MASK, buf));

   //flsobral@tc125: added more info on v2
   if ((strObj = *getStaticFieldObject(p->currentContext, settingsClass, "versionStr")) != null)
      putInfo(p->currentContext, info, "VERSAO_VM", String2CharPBuf(strObj, buffer));

   p->retO = info;
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void rU_getDeviceInfo(NMParams p) // ras/Utils native public static totalcross.util.Hashtable getDeviceInfo() throws ActivationException;
{
//   TCObject obj = p->obj[0];
   TCObject info;
   char serial[128];
   char imei[32];
   char deviceId[128];
   CharP deviceHash;
   IntBuf buf;
   TCObject strObj;
   TCSettings settings = getSettingsPtr();

   getDeviceId(deviceId);
   getRomSerialNumber(serial);
   getImei(imei);
   switch (getDeviceHash(p->currentContext, &deviceHash))
   {
      case GDHERR_IMEI:
         throwExceptionNamed(p->currentContext, "ras.ActivationException", "Unable to get unique device information; turn on WIFI and/or phone, if available. Try again or contact us if the problem persists."); break;
      case GDHERR_ARTIFICIAL_HASH:
         throwExceptionNamed(p->currentContext, "ras.ActivationException", "Unable to get unique device information; turn on WIFI and/or phone, if available."); break;
      case GDHERR_OOM:
      case GDHERR_EXCEPTION: break; // an exception was already thrown
      case NO_ERROR:
      {
         info = createInfo(p->currentContext);
         if (putInfoObj(p->currentContext, info, "PLATFORM", *getStaticFieldObject(p->currentContext, settingsClass, "platform")) &&
             putInfo(p->currentContext, info, "ID", deviceId) &&
             putInfo(p->currentContext, info, "HASH", deviceHash) &&
             putInfo(p->currentContext, info, "VERSAO_ROM", int2str(*settings->romVersionPtr, buf))) //flsobral@tc125: added more info on v2
         {
            //flsobral@tc138: v3 info
            if (putInfo(p->currentContext, info, "IMEI", imei) && putInfo(p->currentContext, info, "SERIAL", serial))
            {
               if ((strObj = *getStaticFieldObject(p->currentContext,settingsClass, "activationId")) != null)
                  putInfoObj(p->currentContext, info, "COD_ATIVACAO" , strObj);
            }
         }
         xfree(deviceHash);

         p->retO = info;
         setObjectLock(p->retO, UNLOCKED);
      } break;
   }
}
