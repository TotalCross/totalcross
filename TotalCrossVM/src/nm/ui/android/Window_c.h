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



static void windowSetSIP(int32 sipOption)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setSIP", "(I)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, m, (jint) sipOption);
}

static void windowSetDeviceTitle(TCObject titleObj)
{
   JNIEnv* env = getJNIEnv();
   jmethodID m = (*env)->GetStaticMethodID(env, applicationClass, "setDeviceTitle", "(Ljava/lang/String;)V");
   jstring s = (*env)->NewString(env, String_charsStart(titleObj), String_charsLen(titleObj));
   (*env)->CallStaticVoidMethod(env, applicationClass, m, s);
   (*env)->DeleteLocalRef(env, s);
}
