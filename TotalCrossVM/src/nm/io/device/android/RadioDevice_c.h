/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: RadioDevice_c.h,v 1.2 2011-01-04 13:31:05 guich Exp $

static bool RdIsSupported(int32 type)
{
   JNIEnv* env = getJNIEnv();
   jclass jRadioClass = (*env)->FindClass(env, "totalcross/android/RadioDevice4A");
   jmethodID isSupportedMethod = (*env)->GetStaticMethodID(env, jRadioClass, "isSupported", "(I)Z");
   jboolean result = (*env)->CallStaticBooleanMethod(env, jRadioClass, isSupportedMethod, (jint) type);
   return result != 0;
}

static int32 RdGetState(int32 type)
{
   JNIEnv* env = getJNIEnv();
   jclass jRadioClass = (*env)->FindClass(env, "totalcross/android/RadioDevice4A");
   jmethodID getStateMethod = (*env)->GetStaticMethodID(env, jRadioClass, "getState", "(I)I");
   jint result = (*env)->CallStaticIntMethod(env, jRadioClass, getStateMethod, (jint) type);
   return (int32) result;
}

static void RdSetState(int32 type, int32 state)
{
   JNIEnv* env = getJNIEnv();
   jclass jRadioClass = (*env)->FindClass(env, "totalcross/android/RadioDevice4A");
   jmethodID setStateMethod = (*env)->GetStaticMethodID(env, jRadioClass, "setState", "(I,I)V");
   (*env)->CallStaticVoidMethod(env, jRadioClass, setStateMethod, (jint) type, (jint) state);
}
