// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined (darwin)
#ifdef __cplusplus
extern "C" {
#endif
    
    char* iphone_readBarcode(char* mode);
    
#ifdef __cplusplus
};
#endif
#endif // darwin

//////////////////////////////////////////////////////////////////////////
TC_API void tidsS_readBarcode_s(NMParams p) // totalcross/io/device/scanner/Scanner native public static String readBarcode(String mode);
{
#ifdef ANDROID
   JNIEnv* env = getJNIEnv();         
   TCObject mode = p->obj[0];
   TCObject o = null;
   
   jmethodID method = (*env)->GetStaticMethodID(env, applicationClass, "requestCameraPermission", "()I");
   jint result = (*env)->CallStaticIntMethod(env, applicationClass, method);
   if (result > 0) {
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
   }
   p->retO = o;
#elif defined darwin
    TCObject mode = p->obj[0];
    char* cmode = String2CharP(mode);
    char* ret = iphone_readBarcode(cmode);
    p->retO = createStringObjectFromCharP(p->currentContext, ret, -1);
    xfree(cmode);
#else
   p->retO = 0;
#endif	
}
