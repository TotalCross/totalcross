// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "../Window.h"

static void windowPlatformSetSIP(
   Context currentContext,
   int32 sipOption,
   TCObject control,
   bool numeric)
{
   UNUSED(currentContext)
   UNUSED(control)
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setSIP", "(IZ)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, m, (jint) sipOption, numeric);
}

static bool windowPlatformIsSIPShown(void)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "getSIP", "()Z");
   return (*env)->CallStaticBooleanMethod(env, applicationClass, m);
}

static void windowPlatformSetOrientation(int32 orientation)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setOrientation", "(I)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, m, orientation);
}

static void windowPlatformGetSafeAreaInsets(
   int32 *top,
   int32 *left,
   int32 *bottom,
   int32 *right)
{
   JNIEnv *env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "getSafeAreaInsets", "()[I");
   jintArray array = (jintArray) (*env)->CallStaticObjectMethod(env, applicationClass, m);
   if (array == null)
      return;

   jint *values = (*env)->GetIntArrayElements(env, array, NULL);
   *top = values[0];
   *left = values[1];
   *bottom = values[2];
   *right = values[3];
   (*env)->ReleaseIntArrayElements(env, array, values, JNI_ABORT);
   (*env)->DeleteLocalRef(env, array);
}
