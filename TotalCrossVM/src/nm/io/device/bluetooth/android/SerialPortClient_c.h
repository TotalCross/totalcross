// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#define BT_ERROR -999
#define BT_INVALID_PASSWORD -998

typedef char NATIVE_HANDLE[MAX_GUID_STRING_LEN]; // ip address or uuid
static jmethodID jconnectTo, jread, jwrite, jclose;

static void loadFunctions()
{
   JNIEnv* env = getJNIEnv();
   jconnectTo = (*env)->GetStaticMethodID(env, jBluetooth4A, "connectTo", "(Ljava/lang/String;)I");
   jclose     = (*env)->GetStaticMethodID(env, jBluetooth4A, "close",     "(Ljava/lang/String;)V");
   jread      = (*env)->GetStaticMethodID(env, jBluetooth4A, "read",      "(Ljava/lang/String;[BII)I");
   jwrite     = (*env)->GetStaticMethodID(env, jBluetooth4A, "write",     "(Ljava/lang/String;[BII)I");
}

static Err btsppClientCreate(NATIVE_HANDLE* nativeHandle, CharP address, int32 channel)
{
   JNIEnv* env = getJNIEnv();
   jint ret;
   jstring jaddress;
   
   if (jconnectTo == 0) loadFunctions();
      
   jaddress = (*env)->NewStringUTF(env, address);
   ret = (*env)->CallStaticIntMethod(env, jBluetooth4A, jconnectTo, jaddress);
   (*env)->DeleteLocalRef(env, jaddress);
   if (ret == NO_ERROR)
      xstrcpy((char*)nativeHandle, address);
   return ret;
}

static Err btsppClientReadWrite(bool isRead, NATIVE_HANDLE* nativeHandle, uint8* byteArrayP, int32 offset, int32 count, int32* bytesRead)
{
   JNIEnv* env = getJNIEnv();
   jstring jaddress = (*env)->NewStringUTF(env, (const char*)nativeHandle);
   //int i1 = debug("r/w: %d, %s",isRead,nativeHandle);
   jbyteArray jbytesP = (*env)->NewByteArray(env, count-offset); // !!! temporary byte array has length: count-offset
   jbyte* jbytes;
   int32 ret;

   if (jconnectTo == 0) loadFunctions(); // create may not be called if this is a bt server socket!
                                                                 
   if (!isRead)
   {
      jbytes = (*env)->GetByteArrayElements(env, jbytesP, 0);
      xmemmove(jbytes, byteArrayP+offset, count);            
      (*env)->ReleaseByteArrayElements(env, jbytesP, jbytes, 0);
   }
   
   ret = (*env)->CallStaticIntMethod(env, jBluetooth4A, isRead ? jread : jwrite, jaddress, jbytesP, 0, (jint)count);
   
   if (isRead && ret > 0)
   {
      jbytes = (*env)->GetByteArrayElements(env, jbytesP, 0);
      xmemmove(byteArrayP+offset, jbytes, ret);
      (*env)->ReleaseByteArrayElements(env, jbytesP, jbytes, 0);
   }
   
   (*env)->DeleteLocalRef(env, jaddress);
   (*env)->DeleteLocalRef(env, jbytesP);
   if (ret >= -1)
   {
      *bytesRead = ret;
      return NO_ERROR;
   }
   return ret;
}

static Err btsppClientRead(NATIVE_HANDLE* nativeHandle, uint8* byteArrayP, int32 offset, int32 count, int32* bytesRead)
{
   return btsppClientReadWrite(true, nativeHandle, byteArrayP, offset, count, bytesRead);
}

static Err btsppClientWrite(NATIVE_HANDLE* nativeHandle, uint8* byteArrayP, int32 offset, int32 count, int32* bytesWritten)
{
   return btsppClientReadWrite(false, nativeHandle, byteArrayP, offset, count, bytesWritten);
}

static Err btsppClientClose(NATIVE_HANDLE* nativeHandle)
{
   JNIEnv* env = getJNIEnv();
   jstring jaddress = (*env)->NewStringUTF(env, (const char*)nativeHandle);
   if (jconnectTo == 0) loadFunctions();
   (*env)->CallStaticVoidMethod(env, jBluetooth4A, jclose, jaddress);
   (*env)->DeleteLocalRef(env, jaddress);
   return NO_ERROR;
}
