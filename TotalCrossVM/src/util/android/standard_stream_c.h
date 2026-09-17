// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef STANDARD_STREAM_ANDROID_C_H
#define STANDARD_STREAM_ANDROID_C_H

#include <android/log.h>
#include <string.h>

#define STANDARD_STREAM_LINE_CAPACITY 4096

JNIEnv *getJNIEnv();

/* The package name is the normal tag; TotalCross is only the defensive fallback. */
static char standardAndroidTag[128] = "TotalCross";
static char standardAndroidLines[2][STANDARD_STREAM_LINE_CAPACITY];
static int32 standardAndroidLineLengths[2];

static void standardAndroidResolveTag()
{
   JNIEnv *env;
   jclass contextClass;
   jmethodID getPackageName;
   jstring packageName;
   const char *utfName;
   size_t length;

   if (!applicationContext || (env = getJNIEnv()) == NULL)
      return;
   contextClass = (*env)->GetObjectClass(env, applicationContext);
   if (!contextClass)
      return;
   getPackageName = (*env)->GetMethodID(env, contextClass, "getPackageName", "()Ljava/lang/String;");
   if (!getPackageName)
   {
      (*env)->ExceptionClear(env);
      (*env)->DeleteLocalRef(env, contextClass);
      return;
   }
   packageName = (jstring)(*env)->CallObjectMethod(env, applicationContext, getPackageName);
   if ((*env)->ExceptionCheck(env) || !packageName)
   {
      (*env)->ExceptionClear(env);
      (*env)->DeleteLocalRef(env, contextClass);
      return;
   }
   utfName = (*env)->GetStringUTFChars(env, packageName, NULL);
   if (utfName)
   {
      length = strlen(utfName);
      if (length >= sizeof(standardAndroidTag))
         length = sizeof(standardAndroidTag) - 1;
      memcpy(standardAndroidTag, utfName, length);
      standardAndroidTag[length] = 0;
      (*env)->ReleaseStringUTFChars(env, packageName, utfName);
   }
   (*env)->DeleteLocalRef(env, packageName);
   (*env)->DeleteLocalRef(env, contextClass);
}

static void standardAndroidEmit(int32 stream)
{
   int priority = stream == STANDARD_STREAM_OUT ? ANDROID_LOG_INFO : ANDROID_LOG_WARN;
   standardAndroidLines[stream][standardAndroidLineLengths[stream]] = 0;
   __android_log_write(priority, standardAndroidTag, standardAndroidLines[stream]);
   standardAndroidLineLengths[stream] = 0;
}

static bool standardPlatformInit()
{
   standardAndroidLineLengths[STANDARD_STREAM_OUT] = 0;
   standardAndroidLineLengths[STANDARD_STREAM_ERR] = 0;
   standardAndroidResolveTag();
   return true;
}

static bool standardPlatformWrite(int32 stream, const uint8 *bytes, int32 length)
{
   while (length-- > 0)
   {
      uint8 byte = *bytes++;
      if (byte == '\n')
         standardAndroidEmit(stream);
      else
      {
         if (standardAndroidLineLengths[stream] == STANDARD_STREAM_LINE_CAPACITY - 1)
            standardAndroidEmit(stream);
         standardAndroidLines[stream][standardAndroidLineLengths[stream]++] = (char)byte;
      }
   }
   return true;
}

static bool standardPlatformFlush(int32 stream, bool durable)
{
   UNUSED(durable);
   if (standardAndroidLineLengths[stream] > 0)
      standardAndroidEmit(stream);
   return true;
}

static bool standardPlatformClose(int32 stream)
{
   return standardPlatformFlush(stream, false);
}

static void standardPlatformDestroy()
{
   standardPlatformFlush(STANDARD_STREAM_OUT, false);
   standardPlatformFlush(STANDARD_STREAM_ERR, false);
}

#endif
