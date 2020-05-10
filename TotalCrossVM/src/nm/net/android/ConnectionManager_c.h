// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

static jclass gConnMgrClass;

static Err CmGetHostAddress(CharP hostName, CharP hostAddress)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getHostAddressMethod = (*env)->GetStaticMethodID(env, jConnectionManager4A, "getHostAddress", "(Ljava/lang/String;)Ljava/lang/String;");
   JCharP jhostName = CharP2JCharP(hostName,-1);
   jstring jHostName = (*env)->NewString(env, (jchar*) jhostName, xstrlen(hostName));
   jstring jString = (jstring) (*env)->CallStaticObjectMethod(env, jConnectionManager4A, getHostAddressMethod, jHostName);
   xfree(jhostName);
   if (jString != null)
   {
      jstring2CharP(jString, hostAddress);
      (*env)->DeleteLocalRef(env, jString); // guich@tc125_1
   }
   (*env)->DeleteLocalRef(env, jHostName);
   return NO_ERROR;
}

static Err CmGetHostName(CharP hostAddress, CharP hostName)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getHostNameMethod = (*env)->GetStaticMethodID(env, jConnectionManager4A, "getHostName", "(Ljava/lang/String;)Ljava/lang/String;");
   JCharP jhostAddress = CharP2JCharP(hostAddress,-1);
   jstring jHostAddress = (*env)->NewString(env, (jchar*) jhostAddress, xstrlen(hostAddress));
   jstring jString = (jstring) (*env)->CallStaticObjectMethod(env, jConnectionManager4A, getHostNameMethod, jHostAddress);
   xfree(jhostAddress);
   if (jString != null)              
   {
      jstring2CharP(jString, hostName);
      (*env)->DeleteLocalRef(env, jString); // guich@tc125_1
   }
   (*env)->DeleteLocalRef(env, jHostAddress);
   return NO_ERROR;
}

static Err CmGetLocalHost(CharP address)
{
   JNIEnv* env = getJNIEnv();
   jmethodID getLocalHostMethod = (*env)->GetStaticMethodID(env, jConnectionManager4A, "getLocalHost", "()Ljava/lang/String;");
   jstring jString = (jstring) (*env)->CallStaticObjectMethod(env, jConnectionManager4A, getLocalHostMethod);
   if (jString != null)
   {
      jstring2CharP(jString, address);
      (*env)->DeleteLocalRef(env, jString); // guich@tc125_1
   }

   return NO_ERROR;
}
