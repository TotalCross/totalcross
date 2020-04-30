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
TC_API void ttSM_registerSmsReceiver_si(NMParams p) // totalcross/telephony/SmsManager public native void registerSmsReceiver(totalcross.telephony.SmsReceiver receiver, int port);
{
   TCObject smsManager = p->obj[0];
   TCObject smsReceiver = p->obj[1];
   int32 port = p->i32[0];
   SmsManager_smsReceiver(smsManager) = smsReceiver;
   
#if defined (ANDROID)   
   registerSmsReceiver(smsReceiver, port);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void ttSM_sendDataMessage_ssiB(NMParams p) // totalcross/telephony/SmsManager public native void sendDataMessage(String destinationAddress, String scAddress, int port, byte[] data);
{
	TCObject smsManager = p->obj[0];
	TCObject destinationAddress = p->obj[1];
	TCObject scAddress = p->obj[2];
	int32 port = p->i32[0];
	TCObject data = p->obj[3];
	
	if (destinationAddress == null) {
		throwNullArgumentException(p->currentContext, "destinationAddress");
	} else if (data == null) {
		throwNullArgumentException(p->currentContext, "data");
	} else if (String_charsLen(destinationAddress) == 0) {
		throwIllegalArgumentException(p->currentContext, "Argument destinationAddress cannot be empty");
	} else if (ARRAYOBJ_LEN(data) == 0) {
		throwIllegalArgumentException(p->currentContext, "Argument data cannot be empty");
	} else {
#if defined (ANDROID)
		sendDataMessage(destinationAddress, scAddress, port, data);
#endif
	}
}
#ifdef ENABLE_TEST_SUITE
//#include "SmsManager_test.h"
#endif
