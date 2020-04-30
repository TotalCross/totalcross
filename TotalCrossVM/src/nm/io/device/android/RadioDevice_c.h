// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

static jmethodID ggetStateMethod;

static bool RdIsSupported(Context currentContext, int32 type)
{
   JNIEnv* env = getJNIEnv();
   jmethodID isSupportedMethod = (*env)->GetStaticMethodID(env, jRadioDevice4A, "isSupported", "(I)Z");
   jboolean result = (*env)->CallStaticBooleanMethod(env, jRadioDevice4A, isSupportedMethod, (jint) type);
   return result != 0;
}

static int32 RdGetState(Context currentContext, int32 type)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getStateMethod = ggetStateMethod ? ggetStateMethod : (ggetStateMethod = (*env)->GetStaticMethodID(env, jRadioDevice4A, "getState", "(I)I"));
   jint result = (*env)->CallStaticIntMethod(env, jRadioDevice4A, getStateMethod, (jint) type);
   return (int32) result;
}

static void RdSetState(Context currentContext, int32 type, int32 state)
{
   JNIEnv* env = getJNIEnv();
   jmethodID setStateMethod = (*env)->GetStaticMethodID(env, jRadioDevice4A, "setState", "(II)V");
   (*env)->CallStaticVoidMethod(env, jRadioDevice4A, setStateMethod, (jint) type, (jint) state);
}
