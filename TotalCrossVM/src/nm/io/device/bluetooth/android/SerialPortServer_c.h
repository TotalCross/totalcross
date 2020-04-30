// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

typedef char NATIVE_HANDLE[MAX_GUID_STRING_LEN]; // uuid
#define BT_ERROR -999
static jmethodID jserverAccept, jserverClose;

static void loadFunctions(JNIEnv* env)
{
   jserverAccept = (*env)->GetStaticMethodID(env, jBluetooth4A, "serverAccept", "(Ljava/lang/String;)I");
   jserverClose  = (*env)->GetStaticMethodID(env, jBluetooth4A, "serverClose",  "()V");
}

static void guid2handle(NATIVE_HANDLE* nativeHandle, GUID guid)
{  
   TCHAR guidstr[MAX_GUID_STRING_LEN];
   GUID2TCHARP(guid,guidstr);
   TCHARP2CharPBuf(guidstr, *nativeHandle);
}

static Err btsppServerCreate(NATIVE_HANDLE* nativeHandle, GUID guid)
{                                      
   guid2handle(nativeHandle, guid);
   return NO_ERROR;
}

static Err btsppServerAccept(NATIVE_HANDLE* nativeHandle, NATIVE_HANDLE* clientHandle)
{
   JNIEnv* env = getJNIEnv();
   jstring jaddress;
   int ret;
   
   if (jserverAccept == 0) 
      loadFunctions(env);
      
   jaddress = (*env)->NewStringUTF(env, (const char*)nativeHandle);
   ret = (*env)->CallStaticIntMethod(env, jBluetooth4A, jserverAccept, jaddress);
   (*env)->DeleteLocalRef(env, jaddress);
   if (ret == NO_ERROR)         
      xmemmove(clientHandle, nativeHandle, MAX_GUID_STRING_LEN);
   return ret;
}

static Err btsppServerClose(NATIVE_HANDLE* nativeHandle)
{         
   if (jserverAccept == 0)
      return BT_ERROR;
   JNIEnv* env = getJNIEnv();
   jstring jaddress = (*env)->NewStringUTF(env, (const char*)nativeHandle);
   (*env)->CallStaticVoidMethod(env, jBluetooth4A, jserverClose, jaddress);
   (*env)->DeleteLocalRef(env, jaddress);
   return NO_ERROR;
}
