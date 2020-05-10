// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



static void dialNumber(CharP number)
{
   JNIEnv *env = getJNIEnv();
   if (env)                      
   {
      JChar buf[50];
      jstring s;
      int32 len = xstrlen(number);
      CharP2JCharPBuf(number,len,buf,true);
      s = (*env)->NewString(env,buf,len);
      (*env)->CallStaticVoidMethod(env, applicationClass, jdial, s); 
      (*env)->DeleteLocalRef(env, s);
   }
}

static void hangup()
{
}
