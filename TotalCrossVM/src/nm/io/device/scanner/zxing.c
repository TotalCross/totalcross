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
TC_API void tidsS_readBarcode_s(NMParams p) // totalcross/io/device/scanner/Scanner native public static String readBarcode(String mode);
{
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();         
   TCObject mode = p->obj[0];
   TCObject o = null;
   jstring jmode = mode == null ? null : (*env)->NewString(env, (jchar*) String_charsStart(mode), String_charsLen(mode));
   jstring result = (*env)->CallStaticObjectMethod(env, applicationClass, jzxing, jmode);
   (*env)->DeleteLocalRef(env, jmode);
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
