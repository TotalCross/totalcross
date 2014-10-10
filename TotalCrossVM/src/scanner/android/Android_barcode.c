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
TC_API void tidsS_getData(NMParams p) // totalcross/io/device/scanner/Scanner native public static String getData();
{
   JNIEnv* env = getJNIEnv();
   jclass applicationClass = androidFindClass(env, "totalcross/android/Scanner4A");
   jstring string = (*env)->CallStaticObjectMethod(env, applicationClass, 
                                                   (*env)->GetStaticMethodID(env, applicationClass, "getData", "()Ljava/lang/String;"));
   TCObject ret = null;

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

