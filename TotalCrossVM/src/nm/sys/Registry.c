// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

#if defined (WP8)

#elif defined(WINCE) || defined(WIN32)
 #include "win/Registry_c.h"
#else
 #include "posix/Registry_c.h"
#endif

#ifndef darwin
static bool getKeyValue(NMParams p, TCHARP* key, TCHARP* value) // key and value are always in the same position
{
   bool ret = false;
   TCObject keyObj, valueObj;
   keyObj = p->obj[0];
   valueObj = p->obj[1];

   if (keyObj == null)
      throwNullArgumentException(p->currentContext, "key");
   else
   if (valueObj == null)
      throwNullArgumentException(p->currentContext, "value");
   else
   {
      *key = String2TCHARP(keyObj);
      *value = String2TCHARP(valueObj);
      if (*key == null || *value == null)
         throwException(p->currentContext, OutOfMemoryError, "Not enough memory to convert strings");
      else
         ret = true;
      // strings must be free by the caller
   }
   return ret;
}
#endif
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_getInt_iss(NMParams p) // totalcross/sys/Registry native public static int getInt(int hk, String key, String value) throws totalcross.util.ElementNotFoundException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null, value=null;
   if (getKeyValue(p, &key, &value))
      privateGetInt(p, p->i32[0], key, value);
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_getString_iss(NMParams p) // totalcross/sys/Registry native public static String getString(int hk, String key, String value) throws totalcross.util.ElementNotFoundException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null, value=null;
   if (getKeyValue(p, &key, &value))
      privateGetString(p, p->i32[0], key, value);
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_getBlob_iss(NMParams p) // totalcross/sys/Registry native public static byte[] getBlob(int hk, String key, String value) throws totalcross.util.ElementNotFoundException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null, value=null;
   if (getKeyValue(p, &key, &value))
      privateGetBlob(p, p->i32[0], key, value);
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_set_issi(NMParams p) // totalcross/sys/Registry native public static void set(int hk, String key, String value, int data);
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null, value=null;
   if (getKeyValue(p, &key, &value))
      privateSetInt(p->currentContext, p->i32[0], key, value, p->i32[1]);
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_set_isss(NMParams p) // totalcross/sys/Registry native public static void set(int hk, String key, String value, String data);
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null, value=null;
   CharP data=null;
   if (getKeyValue(p, &key, &value))
   {
      TCObject dataObj = p->obj[2];
      if (dataObj == null)
         throwNullArgumentException(p->currentContext, "data");
      else
      {
         data = String2CharP(dataObj);
         if (data == null)
            throwException(p->currentContext, OutOfMemoryError, "Not enough memory to convert data");
         else
            privateSetString(p->currentContext, p->i32[0], key, value, data);
      }
   }
   xfree(data);
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_set_issB(NMParams p) // totalcross/sys/Registry native public static void set(int hk, String key, String value, byte []data);
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null, value=null;
   if (getKeyValue(p, &key, &value))
   {
      TCObject dataObj = p->obj[2];
      if (dataObj == null)
         throwNullArgumentException(p->currentContext, "data");
      else
         privateSetBlob(p->currentContext, p->i32[0], key, value, ARRAYOBJ_START(dataObj), ARRAYOBJ_LEN(dataObj));
   }
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_delete_iss(NMParams p) // totalcross/sys/Registry native public static boolean delete(int hk, String key, String value);
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   // value can be null
   TCHARP key=null, value=null;
   TCObject keyObj, valueObj;
   keyObj = p->obj[0];
   valueObj = p->obj[1];

   if (keyObj == null)
      throwNullArgumentException(p->currentContext, "key");
   else
   {
      key = String2TCHARP(keyObj);
      if (key == null)
      {
         throwException(p->currentContext, OutOfMemoryError, "Not enough memory to convert strings");
         return;
      }
      if (valueObj)
      {
         value = String2TCHARP(valueObj);
         if (value == null)
         {
            throwException(p->currentContext, OutOfMemoryError, "Not enough memory to convert strings");
            goto end;
         }
      }
      p->retI = privateDelete(p->i32[0], key, value);
   }
end:
   xfree(key);
   xfree(value);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsR_list_is(NMParams p) // totalcross/sys/Registry native public static String[] list(int hk, String key);
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE))
   TCHARP key=null;
   TCObject keyObj;
   keyObj = p->obj[0];

   key = String2TCHARP(keyObj);
   if (key == null)
      throwException(p->currentContext, OutOfMemoryError, "Not enough memory to convert strings");
   else
   {
      p->retO = privateList(p->currentContext, p->i32[0], key);
      xfree(key);
   }
#endif
}
