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

#ifdef WINCE
static void throwDialException(CharP msg, uint32 param);
static Context currentContext;
static void statusChange(CharP msg);
#endif

#if defined WP8

#elif defined WINCE || defined WIN32
 #include "win/Dial_c.h"
#if defined WINCE
 #include "../io/device/win/RadioDevice_c.h"
#endif
#elif defined(ANDROID)
 #include "android/Dial_c.h"
#else
 #include "posix/Dial_c.h"
#endif

#ifdef WINCE
static void throwDialException(CharP msg, uint32 param)
{
   throwException(currentContext, IOException, msg, param);
}

TCObject* listener;
Method dialStatusChange;
TCObject lastListener;

static void statusChange(CharP msg)
{
   TCObject msgObj;
   if (listener == null || *listener != lastListener)
   {
      TCClass dial = loadClass(currentContext, "totalcross.phone.Dial", true); //flsobral@tc114_75: fixed Dial's full qualified name.
      if (dial == null) // exception already thrown
         return;
      listener = getStaticFieldObject(null, dial, "listener");
      if (listener == null)
      {
         throwException(currentContext, NoSuchFieldError, "Can't find Dial.listener static field.");
         return;
      }
      dialStatusChange = getMethod(OBJ_CLASS(*listener), true, "dialStatusChange", 1, "java.lang.String");
      if (dialStatusChange == null)
      {
         throwException(currentContext, NoSuchMethodError, "Dial.Listener.dialStatusChange(String)");
         return;
      }
      lastListener = *listener;
   }
   msgObj = createStringObjectFromCharP(currentContext, msg, -1);
   if (msgObj != null)
   {
      executeMethod(currentContext, dialStatusChange, *listener, msgObj);
      setObjectLock(msgObj, UNLOCKED);
   }
}
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tpD_number_s(NMParams p) // totalcross/phone/Dial native public static void number(String number);
{
   TCObject numberObj = p->obj[0];
   if (numberObj == null)
   {
      throwNullArgumentException(p->currentContext, "number");
      return;
   }
#if defined (WINCE)
   currentContext = p->currentContext;
   if (RdGetState(PHONE) == RADIO_STATE_DISABLED)
      throwException(currentContext, IOException, "Phone is disabled");
#endif
#if defined(WINCE) || defined(ANDROID) || defined(darwin) || defined(WP8)
   else
   {
      
#if !defined WP8
      char number[100];
      JCharP2CharPBuf(String_charsStart(numberObj), min32(String_charsLen(numberObj),sizeof(number)-1),number);
      dialNumber(number); 
#else
      JChar number[100];
      JCharPDupBuf(String_charsStart(numberObj), min32(String_charsLen(numberObj), sizeof(number) - 1), number);
      dialNumberCPP(number);
#endif
   }
#else
   UNUSED(p);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpD_hangup(NMParams p) // totalcross/phone/Dial native public static void hangup();
{
#if defined(WINCE)
   currentContext = p->currentContext;
   hangup();
#else
   UNUSED(p);
#endif
}
