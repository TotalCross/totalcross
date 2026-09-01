// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only



static void windowBackendSetDeviceTitle(TCObject titleObj)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setDeviceTitle", "(Ljava/lang/String;)V");
   jstring s = (*env)->NewString(env, String_charsStart(titleObj), String_charsLen(titleObj));
   (*env)->CallStaticVoidMethod(env, applicationClass, m, s);
   (*env)->DeleteLocalRef(env, s);
}
