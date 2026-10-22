// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#define BT_ERROR -999
#define BT_INVALID_PASSWORD -998

typedef char NATIVE_HANDLE[MAX_GUID_STRING_LEN]; // ip address or uuid
static jmethodID jconnectTo, jread, jwrite, jclose;

static bool loadFunctions()
{
   JNIEnv* env = getJNIEnv();
   if (!env || !jBluetooth4A)
      return false;

   jconnectTo = (*env)->GetStaticMethodID(env, jBluetooth4A, "connectTo", "(Ljava/lang/String;)I");
   if (!jconnectTo || (*env)->ExceptionCheck(env)) goto Error;
   jclose     = (*env)->GetStaticMethodID(env, jBluetooth4A, "close",     "(Ljava/lang/String;)V");
   if (!jclose || (*env)->ExceptionCheck(env)) goto Error;
   jread      = (*env)->GetStaticMethodID(env, jBluetooth4A, "read",      "(Ljava/lang/String;[BII)I");
   if (!jread || (*env)->ExceptionCheck(env)) goto Error;
   jwrite     = (*env)->GetStaticMethodID(env, jBluetooth4A, "write",     "(Ljava/lang/String;[BII)I");
   if (!jwrite || (*env)->ExceptionCheck(env)) goto Error;
   return true;

Error:
   if ((*env)->ExceptionCheck(env))
      (*env)->ExceptionClear(env);
   jconnectTo = jclose = jread = jwrite = 0;
   return false;
}

static bool clearJNIException(JNIEnv* env)
{
   if ((*env)->ExceptionCheck(env))
   {
      (*env)->ExceptionClear(env);
      return true;
   }
   return false;
}

static Err btsppClientCreate(NATIVE_HANDLE* nativeHandle, CharP address, int32 channel)
{
   JNIEnv* env = getJNIEnv();
   jint ret;
   jstring jaddress = NULL;
   
   if (!env || !jBluetooth4A) return BT_ERROR;
   if (!jconnectTo && !loadFunctions()) return BT_ERROR;
   if (!jconnectTo) return BT_ERROR;
      
   jaddress = (*env)->NewStringUTF(env, address);
   if (clearJNIException(env) || !jaddress)
   {
      if (jaddress) (*env)->DeleteLocalRef(env, jaddress);
      return BT_ERROR;
   }
   ret = (*env)->CallStaticIntMethod(env, jBluetooth4A, jconnectTo, jaddress);
   if (clearJNIException(env)) ret = BT_ERROR;
   (*env)->DeleteLocalRef(env, jaddress);
   if (ret == NO_ERROR)
      xstrcpy((char*)nativeHandle, address);
   return ret;
}

static Err btsppClientReadWrite(bool isRead, NATIVE_HANDLE* nativeHandle, uint8* byteArrayP, int32 offset, int32 count, int32* bytesRead)
{
   JNIEnv* env;
   jstring jaddress = NULL;
   jbyteArray jbytesP = NULL;
   jint javaResult = 0;
   Err result = BT_ERROR;

   if (bytesRead) *bytesRead = 0;
   if (!nativeHandle || !bytesRead || offset < 0 || count < 0 || (count > 0 && !byteArrayP))
      return BT_ERROR;
   if (count == 0)
      return NO_ERROR;

   env = getJNIEnv();
   if (!env)
      return BT_ERROR;
   if ((!jconnectTo || !jread || !jwrite || !jclose) && !loadFunctions())
      goto Cleanup;
   if (!jBluetooth4A || !jconnectTo || (isRead ? !jread : !jwrite))
      goto Cleanup;

   jaddress = (*env)->NewStringUTF(env, (const char*)nativeHandle);
   if (clearJNIException(env) || !jaddress)
      goto Cleanup;

   jbytesP = (*env)->NewByteArray(env, (jsize)count);
   if (clearJNIException(env) || !jbytesP)
      goto Cleanup;

   if (!isRead)
   {
      (*env)->SetByteArrayRegion(env, jbytesP, 0, (jsize)count, (const jbyte*)(byteArrayP + offset));
      if (clearJNIException(env))
         goto Cleanup;
   }

   javaResult = (*env)->CallStaticIntMethod(env, jBluetooth4A, isRead ? jread : jwrite,
         jaddress, jbytesP, 0, (jint)count);
   if (clearJNIException(env) || javaResult > count)
      goto Cleanup;

   if (isRead && javaResult > 0)
   {
      (*env)->GetByteArrayRegion(env, jbytesP, 0, javaResult, (jbyte*)(byteArrayP + offset));
      if (clearJNIException(env))
         goto Cleanup;
   }

   if (javaResult >= -1)
   {
      *bytesRead = javaResult;
      result = NO_ERROR;
   }
   else
      result = (Err)javaResult;

Cleanup:
   if (jbytesP) (*env)->DeleteLocalRef(env, jbytesP);
   if (jaddress) (*env)->DeleteLocalRef(env, jaddress);
   return result;
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
   jstring jaddress = NULL;
   Err result = BT_ERROR;

   if (!env || !jBluetooth4A) return BT_ERROR;
   if (!jclose && !loadFunctions()) return BT_ERROR;
   if (!jclose) return BT_ERROR;

   jaddress = (*env)->NewStringUTF(env, (const char*)nativeHandle);
   if (clearJNIException(env) || !jaddress)
      goto Cleanup;

   (*env)->CallStaticVoidMethod(env, jBluetooth4A, jclose, jaddress);
   if (!clearJNIException(env)) result = NO_ERROR;

Cleanup:
   if (jaddress) (*env)->DeleteLocalRef(env, jaddress);
   return result;
}
