// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "barcode.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_scannerActivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean scannerActivate();
{
   p->retI = callBoolMethodWithoutParams("scannerActivate");
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setBarcodeParam_ib(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeParam(int barcodeType, boolean enable);
{  
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   p->retI = (*env)->CallStaticBooleanMethod(env, applicationClass, (*env)->GetStaticMethodID(env, applicationClass, "setBarcodeParam", "(IZ)Z"), 
                                                                                              (jint)p->i32[0], (jboolean)p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setParam_iii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setParam(int type, int barcodeType, int value);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_setBarcodeLength_iiii(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean setBarcodeLength(int barcodeType, int lengthType, int min, int max);
{
   p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_commitBarcodeParams(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean commitBarcodeParams();
{
   p->retI = true;
}
//////////////////////////////////////////////////////////////////////////
jmethodID jgetData;
TC_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   TCObject ret = null;
   jstring string;                                                        
   if (jgetData == null)
      jgetData = (*env)->GetStaticMethodID(env, applicationClass, "getData", "()Ljava/lang/String;");
   string = (*env)->CallStaticObjectMethod(env, applicationClass, jgetData);

   if (string)
   {
      const CharP charP = (const CharP)(*env)->GetStringUTFChars(env, string, 0);
      
      if (charP) 
      {
         ret = createStringObjectFromCharP(p->currentContext, charP , -1);        
         (*env)->ReleaseStringUTFChars(env, string, charP);
      }
      (*env)->DeleteLocalRef(env, string); 
   }

   setObjectLock(p->retO = ret, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getScanManagerVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanManagerVersion();
{
   if ((p->retO = createStringObjectFromCharP(p->currentContext, "1.0", 3)))
      setObjectLock(p->retO, UNLOCKED);
   else
      throwExceptionNamed(p->currentContext, "java.lang.OutOfMemoryError", null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_getScanPortDriverVersion(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getScanPortDriverVersion();
{
   if ((p->retO = createStringObjectFromCharP(p->currentContext, "UNKNOWN", 7)))
      setObjectLock(p->retO, UNLOCKED);
   else
      throwExceptionNamed(p->currentContext, "java.lang.OutOfMemoryError", null);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_deactivate(NMParams p) // totalcross/io/device/scanner/Scanner native public static boolean deactivate();
{
   p->retI = callBoolMethodWithoutParams("deactivate");
}

bool callBoolMethodWithoutParams(CharP name)
{
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   return (*env)->CallStaticBooleanMethod(env, applicationClass, (*env)->GetStaticMethodID(env, applicationClass, name, "()Z"));
}
//////////////////////////////////////////////////////////////////////////
static jmethodID jsetParam;
TC_API void tidsS_setParam_ss(NMParams p) // totalcross/io/device/scanner/Scanner native public static void setParam(String what, String value);
{                            
   TCObject owhat = p->obj[0];
   TCObject ovalue = p->obj[1];
   if (owhat == null)
      throwNullArgumentException(p->currentContext, "what");
   else
   if (ovalue == null)
      throwNullArgumentException(p->currentContext, "value");      
   else
   {
      JNIEnv* env = getJNIEnv();
      jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
      jstring jwhat  = (*env)->NewString(env, (jchar*) String_charsStart(owhat),  String_charsLen(owhat));
      jstring jvalue = (*env)->NewString(env, (jchar*) String_charsStart(ovalue), String_charsLen(ovalue));
      if (jsetParam == null)
         jsetParam = (*env)->GetStaticMethodID(env, applicationClass, "setParam", "(Ljava/lang/String;Ljava/lang/String;)V");
      p->retI = (*env)->CallStaticBooleanMethod(env, applicationClass, jsetParam, jwhat, jvalue);
      (*env)->DeleteLocalRef(env, jwhat);
      (*env)->DeleteLocalRef(env, jvalue);
   }
}

