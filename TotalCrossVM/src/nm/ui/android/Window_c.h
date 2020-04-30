// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



static void windowSetSIP(int32 sipOption, bool numeric)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setSIP", "(IZ)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, m, (jint) sipOption, numeric);
}

static bool windowGetSIP()
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "getSIP", "()Z");
   return (*env)->CallStaticBooleanMethod(env, applicationClass, m);
}

static void windowSetDeviceTitle(TCObject titleObj)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setDeviceTitle", "(Ljava/lang/String;)V");
   jstring s = (*env)->NewString(env, String_charsStart(titleObj), String_charsLen(titleObj));
   (*env)->CallStaticVoidMethod(env, applicationClass, m, s);
   (*env)->DeleteLocalRef(env, s);
}

static void windowSetOrientation(int32 o)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setOrientation", "(I)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, m, o);
}
