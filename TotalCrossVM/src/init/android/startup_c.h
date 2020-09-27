// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

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

static char targetPackage[8]; // totalcross.android -> totalcross.appapid
static char tcabuf[128];
static bool isSingleAPK;
static char tczname[32];                                                                                           
/*
 * Class:     totalcross_Launcher4A
 * Method:    initializeVM
 * Signature: (Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
void JNICALL Java_totalcross_Launcher4A_initializeVM(JNIEnv *env, jobject appObj, jobject appContext, jstring jtczname, jstring jappPath, jstring jvmPath, jstring jcmdline)
{
   #define MAKE_GLOBAL_REFERENCE(x,c) x = (c*) (*env)->NewGlobalRef(env, x) // the android vm limits to 16 references!

   (*env)->GetJavaVM(env, &androidJVM);
   applicationObj     = appObj;
   applicationContext = appContext;

   MAKE_GLOBAL_REFERENCE(applicationObj, jobject);
   MAKE_GLOBAL_REFERENCE(applicationContext, jobject);

   applicationClass   = JOBJ_CLASS(applicationObj);
   MAKE_GLOBAL_REFERENCE(applicationClass, jclass);

#ifndef ENABLE_TEST_SUITE
   jstring2CharP(jtczname, tczname);
   if (jcmdline != null)
   {
      char cmdline[100];
      jstring2CharP(jcmdline, cmdline);
      xstrcat(tczname," /cmd ");
      xstrcat(tczname,cmdline);
   }
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
   jplayYoutube = (*env)->GetStaticMethodID(env, applicationClass, "playVideo", "(Ljava/lang/String;ZII)V");
   jshowRoute        = (*env)->GetStaticMethodID(env, applicationClass, "showRoute", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)I");
   jshowGoogleMaps   = (*env)->GetStaticMethodID(env, applicationClass, "showGoogleMaps", "(Ljava/lang/String;Z)Z");
   jeventIsAvailable = (*env)->GetStaticMethodID(env, applicationClass, "eventIsAvailable", "()Z");
   jpumpEvents       = (*env)->GetStaticMethodID(env, applicationClass, "pumpEvents", "()V");
   jalert            = (*env)->GetStaticMethodID(env, applicationClass, "alert", "(Ljava/lang/String;)V");
   jsetElapsed       = (*env)->GetStaticMethodID(env, applicationClass, "setElapsed", "(I)I");
   jvmFuncI          = (*env)->GetStaticMethodID(env, applicationClass, "vmFuncI", "(II)I");
   jvmExec           = (*env)->GetStaticMethodID(env, applicationClass, "vmExec", "(Ljava/lang/String;Ljava/lang/String;IZ)I");
   jgetSDCardPath    = (*env)->GetStaticMethodID(env, applicationClass, "getSDCardPath", "(I)Ljava/lang/String;");
   jshowCamera       = (*env)->GetStaticMethodID(env, applicationClass, "showCamera", "(Ljava/lang/String;IIIZI)V");
   jgetNativeResolutions= (*env)->GetStaticMethodID(env, applicationClass, "getNativeResolutions", "()Ljava/lang/String;");
   jgetDefaultToString = (*env)->GetStaticMethodID(env, applicationClass, "getDefaultToString", "()Ljava/lang/String;");
   jdial             = (*env)->GetStaticMethodID(env, applicationClass, "dial", "(Ljava/lang/String;)V");
   jgpsFunc          = (*env)->GetStaticMethodID(env, applicationClass, "gpsFunc", "(II)Ljava/lang/String;");
   jtone             = (*env)->GetStaticMethodID(env, applicationClass, "tone", "(II)V");
   jsoundPlay        = (*env)->GetStaticMethodID(env, applicationClass, "soundPlay", "(Ljava/lang/String;)V");
   jsoundEnable      = (*env)->GetStaticMethodID(env, applicationClass, "soundEnable", "(Z)V");
   jcellinfoUpdate   = (*env)->GetStaticMethodID(env, applicationClass, "cellinfoUpdate", "()[I");
   jshowingAlert     = (*env)->GetStaticFieldID (env, applicationClass, "showingAlert", "Z");
   jgetHeight        = (*env)->GetStaticMethodID(env, applicationClass, "getAppHeight", "()I");
   jsipVisible       = (*env)->GetStaticFieldID (env, applicationClass, "sipVisible", "Z");
   jappTitleH        = (*env)->GetStaticFieldID (env, applicationClass, "appTitleH", "I");
   jclipboard        = (*env)->GetStaticMethodID(env, applicationClass, "clipboard", "(Ljava/lang/String;)Ljava/lang/String;");
   jhardwareKeyboardIsVisible = (*env)->GetStaticFieldID (env, applicationClass, "hardwareKeyboardIsVisible", "Z");
   jfindTCZ          = (*env)->GetStaticMethodID(env, applicationClass, "findTCZ", "(Ljava/lang/String;)I");
   jreadTCZ          = (*env)->GetStaticMethodID(env, applicationClass, "readTCZ", "(II[B)I");
   jlistTCZs         = (*env)->GetStaticMethodID(env, applicationClass, "listTCZs", "()Ljava/lang/String;");
   jgetFreeMemory    = (*env)->GetStaticMethodID(env, applicationClass, "getFreeMemory", "()I");
   // guich@tc135: load classes at startup since it will fail if loading from a thread
   jRadioDevice4A       = androidFindClass(env, "totalcross/android/RadioDevice4A");
   jBluetooth4A         = androidFindClass(env, "totalcross/android/Bluetooth4A");
   jConnectionManager4A = androidFindClass(env, "totalcross/android/ConnectionManager4A");
   jSmsManager4A        = androidFindClass(env, "totalcross/android/SmsManager4A");
   jNotificationManager4A	= androidFindClass(env, "totalcross/android/NotificationManager4A");
   jFirebaseInstanceId  = androidFindClass(env, "com/google/firebase/iid/FirebaseInstanceId");
   jTcFirebaseUtils     = androidFindClass(env, "totalcross/android/firebase/FirebaseUtils");
   jCieloPrinterManager4A   = androidFindClass(env, "totalcross/android/cielo/PrinterManager4A");
   jzxing            = (*env)->GetStaticMethodID(env, applicationClass, "zxing", "(Ljava/lang/String;)Ljava/lang/String;");
   jsoundToText      = (*env)->GetStaticMethodID(env, applicationClass, "soundToText", "(Ljava/lang/String;)Ljava/lang/String;");
   jsoundFromText    = (*env)->GetStaticMethodID(env, applicationClass, "soundFromText", "(Ljava/lang/String;)V");
   jadsFunc          = (*env)->GetStaticMethodID(env, applicationClass, "adsFunc", "(IILjava/lang/String;)I");
}

char* getTotalCrossAndroidClass(CharP className)
{  
	 char* an;
	 if (!isSingleAPK)
	 	  return className;
	 xstrcpy(tcabuf, className); // totalcross.android
	 an = xstrstr(tcabuf,"android");
	 if (an)
   	 xmemmove(an, targetPackage, 7);
	 return tcabuf;
}

jclass androidFindClass(JNIEnv* env, CharP className)
{   
   jclass c = (*env)->FindClass(env, getTotalCrossAndroidClass(className));
   (*env)->ExceptionClear(env);
   if (c)
      MAKE_GLOBAL_REFERENCE(c,jclass);
   return c;
}

JNIEnv* getJNIEnv()
{
   JNIEnv *env;
   (*androidJVM)->GetEnv(androidJVM, (void**)&env, JNI_VERSION_1_2);
   return env;
}

void callExecuteProgram() // called from android/gfx_Graphics_c.h
{
   executeProgram(tczname[0] ? tczname : NULL);
}

void privateExit(int32 code)
{
   JNIEnv* env = getJNIEnv();
   jmethodID jexit = (*env)->GetStaticMethodID(env, applicationClass, "exit", "(I)V");
   (*env)->CallStaticVoidMethod(env, applicationClass, jexit, code);
}

Err fileCreateDir(TCHARP path, int32 slot);

JNIEXPORT void JNICALL Java_totalcross_AndroidUtils_nativeCreateFile(JNIEnv *env, jclass _this, jstring jpath)
{
   char path[100];
   int len;
   jstring2CharPEnv(jpath, path, env);
   len = xstrlen(path);
   if (path[len-1] == '/')
   {
      path[len-1] = 0;
      fileCreateDir(path,0);
   }
   else
   {
      FILE* f = fopen(path,"wb");
      if (f) fclose(f);
   }
}
