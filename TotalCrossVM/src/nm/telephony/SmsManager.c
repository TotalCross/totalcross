/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2008 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

#include "tcvm.h"

#if defined(ANDROID)
#include "android/SmsManager_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void ttSM_sendTextMessage_sss(NMParams p) // totalcross/telephony/SmsManager public native void sendTextMessage(String destinationAddress, String scAddress, String text);
{
   TCObject smsManager = p->obj[0];
   TCObject destinationAddress = p->obj[1];
   TCObject scAddress = p->obj[2];
   TCObject text = p->obj[3];
   
   if (destinationAddress == null) {
      throwNullArgumentException(p->currentContext, "destinationAddress");
   } else if (text == null) {
      throwNullArgumentException(p->currentContext, "text");
   } else if (String_charsLen(destinationAddress) == 0) {
      throwIllegalArgumentException(p->currentContext, "Argument destinationAddress cannot be empty");
   } else if (String_charsLen(text) == 0) {
      throwIllegalArgumentException(p->currentContext, "Argument text cannot be empty");
   } else {
#if defined (ANDROID)
      sendTextMessage(destinationAddress, scAddress, text);
#endif
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void ttSM_registerSmsReceiver_s(NMParams p) // totalcross/telephony/SmsManager public native void registerSmsReceiver(totalcross.telephony.SmsReceiver receiver);
{
   TCObject smsManager = p->obj[0];
   TCObject smsReceiver = p->obj[1];
   SmsManager_smsReceiver(smsManager) = smsReceiver;
   
#if defined (ANDROID)   
   registerSmsReceiver(smsReceiver);
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "SmsManager_test.h"
#endif
