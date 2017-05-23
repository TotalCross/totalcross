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

static Err sendTextMessage (TCObject destinationAddress, TCObject scAddress, TCObject text) {
   JNIEnv* env = getJNIEnv();
   jmethodID sendTextMessageMethod = (*env)->GetStaticMethodID(env, jSmsManager4A, "sendTextMessage", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
   jstring jDestinationAddress = (*env)->NewString(env, String_charsStart(destinationAddress), String_charsLen(destinationAddress));
   jstring jScAddress = (scAddress != null) ? (*env)->NewString(env, String_charsStart(scAddress), String_charsLen(scAddress)) : null;
   jstring jText = (*env)->NewString(env, String_charsStart(text), String_charsLen(text));
   
   (*env)->CallStaticObjectMethod(env, jSmsManager4A, sendTextMessageMethod, jDestinationAddress, jScAddress, jText);
   (*env)->DeleteLocalRef(env, jDestinationAddress);
   if (scAddress != null) {
      (*env)->DeleteLocalRef(env, jScAddress);
   }
   (*env)->DeleteLocalRef(env, jText);
   return NO_ERROR;
}

static Err registerSmsReceiver (TCObject smsReceiver) {
   JNIEnv* env = getJNIEnv();
   jmethodID enableSmsReceiverMethod = (*env)->GetStaticMethodID(env, jSmsManager4A, "enableSmsReceiver", "(Z)V");
   
   (*env)->CallStaticObjectMethod(env, jSmsManager4A, enableSmsReceiverMethod, (jboolean) (smsReceiver != null));
   return NO_ERROR;
}

void JNICALL Java_totalcross_Launcher4A_nativeSmsReceived(JNIEnv *env, jclass _class, jstring jDisplayOriginatingAddress, jstring jDisplayMessageBody)
{
   CharP szDisplayOriginatingAddress = (*env)->GetStringUTFChars(env, jDisplayOriginatingAddress, 0);
   CharP szDisplayMessageBody = (*env)->GetStringUTFChars(env, jDisplayMessageBody, 0);
   TCObject displayOriginatingAddress = createStringObjectFromCharP(mainContext, szDisplayOriginatingAddress, -1);
   TCObject displayMessageBody = createStringObjectFromCharP(mainContext, szDisplayMessageBody, -1);
   (*env)->ReleaseStringUTFChars(env, jDisplayOriginatingAddress, szDisplayOriginatingAddress);
   (*env)->ReleaseStringUTFChars(env, jDisplayMessageBody, szDisplayMessageBody);
   
   Method getDefaultMethod = getMethod(loadClass(mainContext,"totalcross.telephony.SmsManager",false),false,"getDefault",0);
   if (getDefaultMethod != null) {
      TCObject smsManager = executeMethod(mainContext, getDefaultMethod, null).asObj;
      if (smsManager != null) {
         TCObject smsReceiver = SmsManager_smsReceiver(smsManager);
         if (smsReceiver != null) {
            TCObject smsMessage = createObject(mainContext, "totalcross.telephony.SmsMessage");
            if (smsMessage != null) {
               SmsMessage_displayOriginatingAddress(smsMessage) = displayOriginatingAddress;
               SmsMessage_displayMessageBody(smsMessage) = displayMessageBody;
               
               Method onReceiveMethod = getMethod(OBJ_CLASS(smsReceiver), true, "onReceive", 1, "totalcross.telephony.SmsMessage");
               if (onReceiveMethod != null) {
                  executeMethod(mainContext, onReceiveMethod, smsReceiver, smsMessage);
               }
               setObjectLock(smsMessage, UNLOCKED);
            }
         }
      }
   }
   setObjectLock(displayOriginatingAddress, UNLOCKED);
   setObjectLock(displayMessageBody, UNLOCKED);
}


