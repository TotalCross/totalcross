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



#define FEATURE             1
#define SAVED_PREFERENCES   2
#define UNSAVED_PREFERENCES 3

static void privateGetInt(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   UInt32 crid = *((UInt32*)key),ret;
   crid = SWAP32_FORCED(crid);
   int32 err=0,num;
   num = str2int(value,&err);
   if (err)
      throwException(p->currentContext, ExceptionClass, "Invalid number: %s",value);
   else
   {
      if (hk == FEATURE)
         err = FtrGet(crid, num, &ret);
      else
         err = PrefGetAppPreferences(crid, num, &ret, null, hk == SAVED_PREFERENCES);
      if (err == noPreferenceFound)
         throwException(p->currentContext, ElementNotFoundException, "appid: %s, number: %s",key,value);
      else
         p->retI = ret;
   }
}

static void privateGetString(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   UInt32 crid = *((UInt32*)key);
   crid = SWAP32_FORCED(crid);
   int32 err=0,num;
   UInt16 size=0;
   num = str2int(value,&err);

   if (hk == FEATURE)
      throwException(p->currentContext, ExceptionClass, "You can only read integer values from the Feature manager.");
   else
   if (err)
      throwException(p->currentContext, ExceptionClass, "Invalid number: %s",value);
   else
   {
      err = PrefGetAppPreferences(crid, num, null, &size, hk == SAVED_PREFERENCES);
      if (err == noPreferenceFound)
         throwException(p->currentContext, ElementNotFoundException, "appid: %s, number: %s",key,value);
      else
      {
         uint8* buf = xmalloc(size+1);
         if (buf == null)
            throwException(p->currentContext, OutOfMemoryError, "Can't allocate char buffer");
         else
         {
            PrefGetAppPreferences(crid, num, buf, &size, hk == SAVED_PREFERENCES);
            p->retO = createStringObjectFromCharP(p->currentContext, buf, size);
            if (p->retO)
               setObjectLock(p->retO, UNLOCKED);
            xfree(buf);
         }
      }
   }
}

static void privateGetBlob(NMParams p, int32 hk, TCHARP key, TCHARP value)
{
   UInt32 crid = *((UInt32*)key);
   crid = SWAP32_FORCED(crid);
   int32 err=0,num;
   UInt16 size=0;
   num = str2int(value,&err);

   if (hk == FEATURE)
      throwException(p->currentContext, ExceptionClass, "You can only read integer values from the Feature manager.");
   else
   if (err)
      throwException(p->currentContext, ExceptionClass, "Invalid number: %s",value);
   else
   {
      err = PrefGetAppPreferences(crid, num, null, &size, hk == SAVED_PREFERENCES);
      if (err == noPreferenceFound)
         throwException(p->currentContext, ElementNotFoundException, "appid: %s, number: %s",key,value);
      else
      if ((p->retO = createByteArray(p->currentContext, size)) != null)
      {
         PrefGetAppPreferences(crid, num, ARRAYOBJ_START(p->retO), null, hk == SAVED_PREFERENCES);
         setObjectLock(p->retO, UNLOCKED);
      }
   }
}

static void set(Context c, int32 hk, TCHARP key, TCHARP value, uint8* data, int32 len)
{
   uint32 crid = *((uint32*)key);
   crid = SWAP32_FORCED(crid);
   uint32 err=0,num;

   num = str2int(value,&err);
   if (hk == FEATURE)
      throwException(c, ExceptionClass, "Features are read-only.");
   else
   if (err)
      throwException(c, ExceptionClass, "Invalid number: %s",value);
   else
   {
      if (hk == FEATURE)
         err = FtrSet(crid, num, *((UInt32*)data));
      else
         PrefSetAppPreferences(crid, num, 1, data, len, hk == SAVED_PREFERENCES);
      if (err != errNone)
         throwException(c, ExceptionClass, "Error %d",err);
   }
}

static void privateSetInt(Context c, int32 hk, TCHARP key, TCHARP value, int32 data)
{
   set(c, hk, key, value, (uint8*)&data, 4);
}

static void privateSetString(Context c, int32 hk, TCHARP key, TCHARP value, CharP data)
{
   set(c, hk, key, value, data, xstrlen(data));
}

static void privateSetBlob(Context c, int32 hk, TCHARP key, TCHARP value, uint8* data, int32 len)
{
   set(c, hk, key, value, data, len);
}

static bool privateDelete(int32 hk, TCHARP key, TCHARP value)
{
   uint32 crid = *((uint32*)key);
   crid = SWAP32_FORCED(crid);
   uint32 err=0,num;

   num = str2int(value ? value : "*",&err); // force error if null value
   if (err)
      return false;
   else
   {
      if (hk == FEATURE)
         err = FtrUnregister(crid, num);
      else
         PrefSetAppPreferences(crid, num, 1, null, 0, hk == SAVED_PREFERENCES);
      return err == errNone;
   }
}

static Object privateList(Context currentContext, int32 hk, TCHARP key)
{
   UNUSED(currentContext);
   UNUSED(hk);
   UNUSED(key);
   return null;
}
