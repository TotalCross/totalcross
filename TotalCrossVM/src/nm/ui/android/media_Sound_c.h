// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only


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
static void soundToText(NMParams p)
{
   JNIEnv* env = getJNIEnv();         
   TCObject o = null;
   TCObject params = p->obj[0];
   jstring jparams = params == null ? null : (*env)->NewString(env, (jchar*) String_charsStart(params), String_charsLen(params));
   jstring result = (*env)->CallStaticObjectMethod(env, applicationClass, jsoundToText, jparams);
   if (jparams != null) (*env)->DeleteLocalRef(env, jparams);
   if (result != null)
   {
      const jchar *str = (*env)->GetStringChars(env, result, 0);
      if (str)
         o = createStringObjectFromJCharP(p->currentContext, (JCharP)str, (*env)->GetStringLength(env, result));
      (*env)->ReleaseStringChars(env, result, str);
   }
   (*env)->DeleteLocalRef(env, result);
   p->retO = o;
   
}
static void soundFromText(NMParams p)
{
   JNIEnv* env = getJNIEnv();         
   TCObject params = p->obj[0];
   jstring jparams = params == null ? null : (*env)->NewString(env, (jchar*) String_charsStart(params), String_charsLen(params));
   (*env)->CallStaticVoidMethod(env, applicationClass, jsoundFromText, jparams);
   if (jparams != null) (*env)->DeleteLocalRef(env, jparams);
}