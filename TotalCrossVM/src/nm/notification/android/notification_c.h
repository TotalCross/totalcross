// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

static Err NmNotify(TCObject title, TCObject text)
{
   JNIEnv* env = getJNIEnv();
   jmethodID notifyMethod = (*env)->GetStaticMethodID(env, jNotificationManager4A, "notify", "(Ljava/lang/String;Ljava/lang/String;)V");
   jstring jTitle = title == null ? null : (*env)->NewString(env, String_charsStart(title), String_charsLen(title));
   jstring jText = text == null ? null : (*env)->NewString(env, String_charsStart(text), String_charsLen(text));
   (*env)->CallStaticObjectMethod(env, jNotificationManager4A, notifyMethod, jTitle, jText);
   
   (*env)->DeleteLocalRef(env, jTitle);
   (*env)->DeleteLocalRef(env, jText);
   
   return NO_ERROR;
}
