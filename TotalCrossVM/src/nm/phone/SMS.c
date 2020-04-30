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

#if defined WP8

#elif defined (WIN32) || defined (WINCE)
 #include "win/SMS_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tpSMS_send_ss(NMParams p) // totalcross/phone/SMS native public static void send(String destination, String message) throws totalcross.io.IOException;
{
#if defined (WINCE) || defined (WP8)
   TCObject destination = p->obj[0];
   TCObject message = p->obj[1];
   if (destination == null)
      throwNullArgumentException(p->currentContext, "destination");
   else
   if (message == null)
      throwNullArgumentException(p->currentContext, "message");
   else
   {
#ifdef WINCE
      TCHARP szMessage, szDestination;

      szMessage = String2TCHARP(message);
      szDestination = String2TCHARP(destination);
      if (!szMessage || !szDestination)
         throwException(p->currentContext, OutOfMemoryError, !szMessage?"When allocating 'message'":"'When allocating 'destination'");
      else
         SmsSend(p->currentContext, szMessage, szDestination);
#elif defined (WP8)
      JCharP szMessage = JCharPDup(String_charsStart(message), String_charsLen(message));
      JCharP szDestination = JCharPDup(String_charsStart(destination), String_charsLen(destination));

      if (!szMessage || !szDestination)
         throwException(p->currentContext, OutOfMemoryError, !szMessage ? "When allocating 'message'" : "'When allocating 'destination'");
      else
         smsSendCPP(szMessage, szDestination);
         
#endif
      xfree(szMessage);
      xfree(szDestination);
   }
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpSMS_receive(NMParams p) // totalcross/phone/SMS native public static String[] receive() throws totalcross.io.IOException;
{
#if defined (WINCE)
   SmsReceive(p->currentContext, &p->retO);
#else
   p->retO = NULL;
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "SMS_test.h"
#endif
