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

#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_readBarcode_s(NMParams p) // totalcross/io/device/scanner/Scanner native public static String readBarcode(String fileName);
{
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();         
   Object fname = p->obj[0];
   Object o = null;
   jstring jfname = (*env)->NewString(env, (jchar*) String_charsStart(fname), String_charsLen(fname));
   jstring result = (*env)->CallStaticObjectMethod(env, applicationClass, jzxing, jfname);
   (*env)->DeleteLocalRef(env, jfname);
   if (result != null)
   {
      const jchar *str = (*env)->GetStringChars(env, result, 0);
      if (str)
         o = createStringObjectFromJCharP(p->currentContext, (JCharP)str, (*env)->GetStringLength(env, result));
      (*env)->ReleaseStringChars(env, result, str);
   }
   (*env)->DeleteLocalRef(env, result); // guich@tc125_1
   p->retO = o;
#else
   p->retO = 0;
#endif	
}
