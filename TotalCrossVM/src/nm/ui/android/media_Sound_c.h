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


static void soundPlay(CharP filename)
{
   JNIEnv *env = getJNIEnv();
   if (env)                      
   {
      JChar buf[MAX_PATHNAME];
      jstring s;
      int32 len = xstrlen(filename);
      CharP2JCharPBuf(filename,len,buf,true);
      s = (*env)->NewString(env,buf,len);
      (*env)->CallStaticVoidMethod(env, applicationClass, jsoundPlay, s); 
      (*env)->DeleteLocalRef(env, s);
   }
}

static void soundSetEnabled(bool b)
{
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticVoidMethod(env, applicationClass, jsoundEnable, b);
}

static void soundBeep(void)
{
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticVoidMethod(env, applicationClass, jtone, 99999,0);
}

bool soundTone(int32 freq, uint16 duration)
{
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticVoidMethod(env, applicationClass, jtone, freq, duration);
}
