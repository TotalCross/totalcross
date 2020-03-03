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
