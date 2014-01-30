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



#include "tcvm.h"

static void throwDialException(CharP msg, uint32 param);
static Context currentContext;
static void statusChange(CharP msg);

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

static void throwDialException(CharP msg, uint32 param)
{
   throwException(currentContext, IOException, msg, param);
}

Object* listener;
Method dialStatusChange;
Object lastListener;

static void statusChange(CharP msg)
{
   Object msgObj;
   if (listener == null || *listener != lastListener)
   {
      TCClass dial = loadClass(currentContext, "totalcross.phone.Dial", true); //flsobral@tc114_75: fixed Dial's full qualified name.
      if (dial == null) // exception already thrown
         return;
      listener = getStaticFieldObject(dial, "listener");
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

//////////////////////////////////////////////////////////////////////////
TC_API void tpD_number_s(NMParams p) // totalcross/phone/Dial native public static void number(String number);
{
#if defined(WINCE) || defined(ANDROID) || defined(darwin)
   Object numberObj = p->obj[0];
   currentContext = p->currentContext;
   if (numberObj == null)
      throwNullArgumentException(p->currentContext, "number");
#if defined (WINCE)
   else if (RdGetState(PHONE) == RADIO_STATE_DISABLED)
      throwException(p->currentContext, IOException, "Phone is disabled");
#endif
   else
   {
      char number[100];
      JCharP2CharPBuf(String_charsStart(numberObj), min32(String_charsLen(numberObj),sizeof(number)-1),number);
      dialNumber(number);
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
