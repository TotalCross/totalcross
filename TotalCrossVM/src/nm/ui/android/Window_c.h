// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
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

static void windowGetSafeAreaInsets(int32 *top, int32 *left, int32 *bottom, int32 *right) {
    JNIEnv *env = getJNIEnv();
    jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "getSafeAreaInsets", "()[I");

    jintArray array = (jintArray) (*env)->CallStaticObjectMethod(env, applicationClass, m);
    if (array == null) {
        return;
    }

    jint *values = (*env)->GetIntArrayElements(env, array, NULL);

    *top = values[0];
    *left = values[1];
    *bottom = values[2];
    *right = values[3];

    (*env)->ReleaseIntArrayElements(
            env,
            array,
            values,
            JNI_ABORT
    );

    (*env)->DeleteLocalRef(env, array);
}
