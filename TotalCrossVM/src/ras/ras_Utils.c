/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include "tcvm.h"
#include "../axtls/crypto.h"

static Object createInfo(Context currentContext)
{
   Object info = createObjectWithoutCallingDefaultConstructor(currentContext, "totalcross.util.Hashtable");
   executeMethod(currentContext, getMethod(OBJ_CLASS(info), true, CONSTRUCTOR_NAME, 1, J_INT), info, 10);
   return info;
}

static bool putInfo(Context currentContext, Object info, CharP key, CharP val)
{
   Object infoKey;
   Object infoVal;

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

static bool putInfoObj(Context currentContext, Object info, CharP key, Object infoVal)
{
   Object infoKey;
   
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
#elif defined(PALMOS)
   uint16 size = 4;
   if (PrefGetAppPreferences('POWR', 1, &v, &size, true) == noPreferenceFound)
   {
      v = (int32)TimGetTicks();
      PrefSetAppPreferences('POWR', 1, 1, &v, 4, true); // returns void
   }
#endif
   if (v != 0)
      xstrprintf(out, "%lX", (long)v);
}

static CharP getDeviceHash(Context currentContext)
{
   MD5_CTX ctx;
   char buf[128];
   int32 no = 0;
   Object res, out;

   MD5Init(&ctx);

   getRomSerialNumber(buf);
   if (*buf)
      MD5Update(&ctx, buf, xstrlen(buf));
   else no++;

   getDeviceId(buf); //flsobral@tc126: added "MOTOROLA MC55" and "Intermec CN3"
   if (!strEq(buf, "Palm Treo 750") || !strEq(buf, "MOTOROLA MC55") || !strEq(buf, "Intermec CN3")) // flsobral@tc122: never use IMEI on these device because it is not available when the device is on airplane mode. (phone off)
   {
      getImei(buf);
      if (*buf)
         MD5Update(&ctx, buf, xstrlen(buf));
      else no++;
   }

   if (no == 2) // no information? use an artificial hash
   {
      getArtificialHash(buf);
      if (*buf)
         MD5Update(&ctx, buf, xstrlen(buf));
      else
         return null;
   }

   out = createByteArray(currentContext, MD5_SIZE);

   MD5Final(&ctx, ARRAYOBJ_START(out));

   res = executeMethod(currentContext, getMethod(loadClass(currentContext, "totalcross.sys.Convert", true), true, "bytesToHexString", 1, BYTE_ARRAY), out).asObj;
   setObjectLock(out, UNLOCKED);
   if (currentContext->thrownException != null)
      return null;

   return JCharP2CharP(String_charsStart(res), String_charsLen(res));
}

//////////////////////////////////////////////////////////////////////////
TC_API void rU_getConfigInfo(NMParams p) // ras/Utils native public static totalcross.util.Hashtable getConfigInfo();
{
//   Object obj = p->obj[0];
   Object info = createInfo(p->currentContext);

   putInfo(p->currentContext, info, "SERVER_HOST", "www.superwaba.net");
   putInfo(p->currentContext, info, "SERVER_PORT", "6666");
   putInfo(p->currentContext, info, "SERVER_SOCK_PARAMS", "");

   p->retO = info;
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void rU_getProductInfo(NMParams p) // ras/Utils native public static totalcross.util.Hashtable getProductInfo();
{
//   Object obj = p->obj[0];
   Object info = createInfo(p->currentContext);
   IntBuf buf;
   char buffer[64];
   Object strObj;

   putInfo(p->currentContext, info, "FULL_ID", "TC??");
   putInfo(p->currentContext, info, "COMPILATION_DATE", int2str(getCompilationDate() ^ COMPILATION_MASK, buf));

   //flsobral@tc125: added more info on v2
   if ((strObj = *getStaticFieldObject(settingsClass, "versionStr")) != null)
      putInfo(p->currentContext, info, "VERSAO_VM", String2CharPBuf(strObj, buffer));

   p->retO = info;
   setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void rU_getDeviceInfo(NMParams p) // ras/Utils native public static totalcross.util.Hashtable getDeviceInfo();
{
//   Object obj = p->obj[0];
   Object info;
   char deviceId[128];
   CharP deviceHash;
   IntBuf buf;
   char buffer[64];
   Object strObj;
   TCSettings settings = getSettingsPtr();

   getDeviceId(deviceId);
   deviceHash = getDeviceHash(p->currentContext);
   if (deviceHash == null)
      throwException(p->currentContext, RuntimeException, "Unable to get unique device information. Activation cannot continue.");

   info = createInfo(p->currentContext);
   if (putInfoObj(p->currentContext, info, "PLATFORM", *getStaticFieldObject(settingsClass, "platform")) &&
       putInfo(p->currentContext, info, "ID", deviceId) &&
       putInfo(p->currentContext, info, "HASH", deviceHash) &&
       putInfo(p->currentContext, info, "VERSAO_ROM", int2str(*settings->romVersionPtr, buf))) //flsobral@tc125: added more info on v2
   {
      if ((strObj = *getStaticFieldObject(settingsClass, "activationId")) != null)
         putInfoObj(p->currentContext, info, "COD_ATIVACAO" , strObj);
   }
   xfree(deviceHash);

   p->retO = info;
   setObjectLock(p->retO, UNLOCKED);
}
