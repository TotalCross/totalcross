// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

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

static Err registerSmsReceiver (TCObject smsReceiver, int32 port) {
   JNIEnv* env = getJNIEnv();
   jmethodID enableSmsReceiverMethod = (*env)->GetStaticMethodID(env, jSmsManager4A, "enableSmsReceiver", "(ZI)V");
   
   (*env)->CallStaticObjectMethod(env, jSmsManager4A, enableSmsReceiverMethod, (jboolean) (smsReceiver != null), (jint) port);
   return NO_ERROR;
}

void JNICALL Java_totalcross_Launcher4A_nativeSmsReceived(JNIEnv *env, jclass _class, jstring jDisplayOriginatingAddress, jstring jDisplayMessageBody, jbyteArray jUserData)
{
   CharP szDisplayOriginatingAddress = (*env)->GetStringUTFChars(env, jDisplayOriginatingAddress, 0);
   CharP szDisplayMessageBody = (*env)->GetStringUTFChars(env, jDisplayMessageBody, 0);
   TCObject displayOriginatingAddress = createStringObjectFromCharP(mainContext, szDisplayOriginatingAddress, -1);
   TCObject displayMessageBody = szDisplayMessageBody == null ? null : createStringObjectFromCharP(mainContext, szDisplayMessageBody, -1);
   TCObject userData = null;
   (*env)->ReleaseStringUTFChars(env, jDisplayOriginatingAddress, szDisplayOriginatingAddress);
   (*env)->ReleaseStringUTFChars(env, jDisplayMessageBody, szDisplayMessageBody);
   
   if (jUserData != null && (*env)->GetArrayLength(env, jUserData) > 0) {
	   userData = createByteArray(mainContext, (*env)->GetArrayLength(env, jUserData));
	   if (userData != null) {
		   jbyte* jbytes = (*env)->GetByteArrayElements(env, jUserData, 0);
		   xmemmove(ARRAYOBJ_START(userData), jbytes, ARRAYOBJ_LEN(userData));
		   (*env)->ReleaseByteArrayElements(env, jUserData, jbytes, 0);
	   }
   }
   
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
               SmsMessage_userData(smsMessage) = userData;
               
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
   if (userData != null) {
	   setObjectLock(userData, UNLOCKED);
   }
}

static Err sendDataMessage (TCObject destinationAddress, TCObject scAddress, int32 port, TCObject data) {
   JNIEnv* env = getJNIEnv();
   jmethodID sendDataMessageMethod = (*env)->GetStaticMethodID(env, jSmsManager4A, "sendDataMessage", "(Ljava/lang/String;Ljava/lang/String;I[B)V");
   jstring jDestinationAddress = (*env)->NewString(env, String_charsStart(destinationAddress), String_charsLen(destinationAddress));
   jstring jScAddress = (scAddress != null) ? (*env)->NewString(env, String_charsStart(scAddress), String_charsLen(scAddress)) : null;
   jbyteArray jData = null;
   jbyte* jbytes = null;
   
   if (data != null) {
	   jData = (*env)->NewByteArray(env, ARRAYOBJ_LEN(data));
	   if (jData != null) {
		   jbytes = (*env)->GetByteArrayElements(env, jData, 0);
		   xmemmove(jbytes, ARRAYOBJ_START(data), ARRAYOBJ_LEN(data));
		   (*env)->SetByteArrayRegion(env, jData, 0, ARRAYOBJ_LEN(data), jbytes);
	   }
   }
   
   (*env)->CallStaticObjectMethod(env, jSmsManager4A, sendDataMessageMethod, jDestinationAddress, jScAddress, port, jData);
   (*env)->DeleteLocalRef(env, jDestinationAddress);
   if (scAddress != null) {
      (*env)->DeleteLocalRef(env, jScAddress);
   }
   if (jbytes != null) {
	   (*env)->ReleaseByteArrayElements(env, jData, jbytes, 0);
   }
   if (jData != null) {
	   (*env)->DeleteLocalRef(env, jData);
   }
   return NO_ERROR;
}

