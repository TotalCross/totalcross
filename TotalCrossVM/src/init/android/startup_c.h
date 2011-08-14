/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#if defined(linux)
#include <unistd.h>
#endif

#define waitUntilStarted()

bool wokeUp()
{
   return false;
}

static void registerWake(bool set)
{
   UNUSED(set);
}

static void setFullScreen()
{
}                     

static char targetPackage[8]; // totalcross.android -> totalcross.uigadge
static char tcabuf[64];
static bool isSingleAPK;
static char tczname[32];
/*
 * Class:     totalcross_Launcher4A
 * Method:    initializeVM
 * Signature: (Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
void JNICALL Java_totalcross_Launcher4A_initializeVM(JNIEnv *env, jobject appObj, jobject appContext, jstring jtczname, jstring jappPath, jstring jvmPath)
{
   #define MAKE_GLOBAL_REFERENCE(x,c) x = (c*) (*env)->NewGlobalRef(env, x) // the android vm limits to 16 references!

   (*env)->GetJavaVM(env, &androidJVM);
   applicationObj     = appObj;
   applicationContext = appContext;

   MAKE_GLOBAL_REFERENCE(applicationObj, jobject);
   MAKE_GLOBAL_REFERENCE(applicationContext, jobject);

   applicationClass   = JOBJ_CLASS(applicationObj);
   MAKE_GLOBAL_REFERENCE(applicationClass, jclass);

   jeventIsAvailable = (*env)->GetStaticMethodID(env, applicationClass, "eventIsAvailable", "()Z");
   jpumpEvents       = (*env)->GetStaticMethodID(env, applicationClass, "pumpEvents", "()V");
   jupdateScreen     = (*env)->GetStaticMethodID(env, applicationClass, "updateScreen", "(IIIII)V");
   jalert            = (*env)->GetStaticMethodID(env, applicationClass, "alert", "(Ljava/lang/String;)V");
   jsetElapsed       = (*env)->GetStaticMethodID(env, applicationClass, "setElapsed", "(I)I");
   jvmFuncI          = (*env)->GetStaticMethodID(env, applicationClass, "vmFuncI", "(II)I");
   jvmExec           = (*env)->GetStaticMethodID(env, applicationClass, "vmExec", "(Ljava/lang/String;Ljava/lang/String;IZ)I");
   jgetSDCardPath    = (*env)->GetStaticMethodID(env, applicationClass, "getSDCardPath", "()Ljava/lang/String;");
   jshowCamera       = (*env)->GetStaticMethodID(env, applicationClass, "showCamera", "(Ljava/lang/String;)V");
   jdial             = (*env)->GetStaticMethodID(env, applicationClass, "dial", "(Ljava/lang/String;)V");
   jgpsFunc          = (*env)->GetStaticMethodID(env, applicationClass, "gpsFunc", "(I)Ljava/lang/String;");
   jtone             = (*env)->GetStaticMethodID(env, applicationClass, "tone", "(II)V");
   jsoundEnable      = (*env)->GetStaticMethodID(env, applicationClass, "soundEnable", "(Z)V");
   jcellinfoUpdate   = (*env)->GetStaticMethodID(env, applicationClass, "cellinfoUpdate", "()[I");
   jshowingAlert     = (*env)->GetStaticFieldID (env, applicationClass, "showingAlert", "Z");
   jhardwareKeyboardIsVisible = (*env)->GetStaticFieldID (env, applicationClass, "hardwareKeyboardIsVisible", "Z");
                                                                                           
#ifndef ENABLE_TEST_SUITE
   jstring2CharP(jtczname, tczname);
#endif
   jstring2CharP(jappPath, appPath);
   jstring2CharP(jvmPath, vmPath);
   
   isSingleAPK = strEq(appPath, vmPath);
   if (isSingleAPK)
   {
   	  int32 len = xstrlen(tczname);
      xstrncpy(targetPackage, tczname, min32(7,len));
      for (; len < 7; len++) // fill with _
      	 targetPackage[len] = '_';            
   }   	
}

char* getTotalCrossAndroidClass(char* className)
{  
	 char* an;
	 if (!isSingleAPK)
	 	  return className;
	 xstrcpy(tcabuf, className); // totalcross.android
	 an = xstrstr(tcabuf,"android");
	 xmemmove(an, targetPackage, 7);
	 return tcabuf;
}

JNIEnv* getJNIEnv()
{
   JNIEnv *env;
   (*androidJVM)->GetEnv(androidJVM, (void**)&env, JNI_VERSION_1_2);
   return env;
}

void callExecuteProgram() // called from android/gfz_Graphics_c.h
{
   executeProgram(tczname[0] ? tczname : NULL);
}

void privateExit(int32 code)
{
   JNIEnv* env = getJNIEnv();
   jmethodID jexit = (*env)->GetStaticMethodID(env, applicationClass, "exit", "(I)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, jexit, code);
}
