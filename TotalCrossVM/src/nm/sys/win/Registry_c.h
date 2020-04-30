// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



static void privateGetInt(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   HKEY handle=(HKEY)0;
   long ret;
   uint32 size=4;

   ret = RegOpenKeyEx((HKEY)hk, key, 0, KEY_READ, &handle);
   if (ret != 0)
   {
notFound:
      throwException(p->currentContext, ElementNotFoundException, "Key not found");
   }
   else
   {
      ret = RegQueryValueEx(handle,value,NULL,NULL,(uint8 *)&p->retI,&size);
      RegCloseKey(handle);
      if (ret == ERROR_FILE_NOT_FOUND) // very strange: a key that does not exist does not return an error in RegOpenKeyEx!
         goto notFound;
      if (ret != 0)
         throwException(p->currentContext, ExceptionClass, "Cannot read value. Error: %d",ret);
   }
}

static void privateGetString(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   HKEY handle=(HKEY)0;
   long ret;
   uint32 size=0;

   ret = RegOpenKeyEx((HKEY)hk, key, 0, KEY_READ, &handle);
   if (ret != 0)
   {
notFound:
      throwException(p->currentContext, ElementNotFoundException, "Key not found");
   }
   else
   {
      ret = RegQueryValueEx(handle, value, NULL, NULL, NULL, &size);
      if (ret == 0)
      {
         TCHARP buf = (TCHARP)xmalloc(size);
         if (buf == null)
            throwException(p->currentContext, OutOfMemoryError, "Can't allocate char buffer");
         else
         {
            ret = RegQueryValueEx(handle,value,NULL,NULL,(char*)buf,&size);
            if (ret == 0)
            {
               p->retO = createStringObjectFromTCHARP(p->currentContext, buf, size/sizeof(TCHAR)-1); // guich@tc100b5_37: in wince its a TCHAR. - guich@tc112_16
               if (p->retO)
                  setObjectLock(p->retO, UNLOCKED);
            }
            xfree(buf);
         }
      }
      RegCloseKey(handle);
      if (ret == ERROR_FILE_NOT_FOUND) // very strange: a key that does not exist does not return an error in RegOpenKeyEx!
         goto notFound;
      if (ret != 0)
         throwException(p->currentContext, ExceptionClass, "Cannot read value. Error: %d",ret);
   }
}

static void privateGetBlob(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   HKEY handle=(HKEY)0;
   long ret;
   uint32 size=0;

   ret = RegOpenKeyEx((HKEY)hk, key, 0, KEY_READ, &handle);
   if (ret != 0)
   {
notFound:
      throwException(p->currentContext, ElementNotFoundException, "Key not found");
   }
   else
   {
      ret = RegQueryValueEx(handle, value, NULL, NULL, NULL, &size);
      if (ret == 0)
      {
         if ((p->retO = createByteArray(p->currentContext, size)) != null)
         {
            setObjectLock(p->retO, UNLOCKED);
            ret = RegQueryValueEx(handle,value,NULL,NULL,ARRAYOBJ_START(p->retO),&size);
         }
      }
      RegCloseKey(handle);
      if (ret == ERROR_FILE_NOT_FOUND) // very strange: a key that does not exist does not return an error in RegOpenKeyEx!
         goto notFound;
      if (ret != 0)
         throwException(p->currentContext, ExceptionClass, "Cannot read value. Error: %d",ret);
   }
}

static void set(Context c, int32 hk, TCHARP key, TCHARP value, uint32 type, void* data, int32 len)
{
   HKEY handle=(HKEY)0;
   DWORD disp;
   long ret;

   RegCreateKeyEx((HKEY)hk, key, 0, NULL, 0, KEY_WRITE, NULL, &handle, &disp);
   ret = RegSetValueEx(handle,value,0,type,(uint8*)data,len);
   RegCloseKey(handle);
   if (ret != 0)
      throwException(c, ExceptionClass, "Cannot write value. Error: %d",ret);
}

static void privateSetInt(Context c, int32 hk, TCHARP key, TCHARP value, int32 data)
{
   set(c, hk, key, value, REG_DWORD, &data, 4);
}

static void privateSetString(Context c, int32 hk, TCHARP key, TCHARP value, CharP data)
{
   int32 len = xstrlen(data);
   TCHARP wdata = CharP2TCHARP(data);
   if (wdata)
      set(c, hk, key, value, REG_SZ, wdata, len*2);
   xfree(wdata);
}

static void privateSetBlob(Context c, int32 hk, TCHARP key, TCHARP value, uint8* data, int32 len)
{
   set(c, hk, key, value, REG_BINARY, data, len);
}

static bool privateDelete(int32 hk, TCHARP key, TCHARP value)
{
   HKEY handle;
   if (value == null)
      return RegDeleteKey((HKEY)hk,key) == 0;
   else
      return RegOpenKeyEx((HKEY)hk,key,0,KEY_SET_VALUE,&handle) == NO_ERROR &&
             RegDeleteValue(handle,value) == NO_ERROR &&
             RegCloseKey(handle) == NO_ERROR;
}

static TCObject privateList(Context currentContext, int32 hk, TCHARP key)
{
   HKEY handle;
   LONG ret;
   DWORD count=0,i;
   TCObject arrayObj = null;
   TCObject *array = null;
   TCHAR buf[256];
   DWORD bufSize=255;

   if ((ret=RegOpenKeyEx((HKEY)hk, key,0, KEY_ENUMERATE_SUB_KEYS|KEY_QUERY_VALUE, &handle)) != NO_ERROR)
      return null;
   ret = RegQueryInfoKey(handle, 0,0,0, &count,0,0,0,0,0,0,0);
   if (ret == NO_ERROR && count > 0)
   {
      arrayObj = createArrayObject(currentContext, "[java.lang.String", count);
      if (arrayObj != null)
      {
         array = (TCObject*) ARRAYOBJ_START(arrayObj);
         for (i = 0; i < count; i++, bufSize = 255)
            if (RegEnumKeyEx(handle, i, buf, &bufSize, 0, 0, 0, 0) == NO_ERROR)
            {
               *array = createStringObjectFromTCHAR(currentContext, buf, bufSize);
               setObjectLock(*array, UNLOCKED);
               array++;
            }
         setObjectLock(arrayObj, UNLOCKED);
      }
   }
   RegCloseKey(handle);
   return arrayObj;
}
