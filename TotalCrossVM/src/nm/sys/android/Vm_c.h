// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <time.h>
#include <sys/time.h>
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>

#define REBOOT_DEVICE         1                                                     
#define SET_AUTO_OFF          2                                                                     
#define SHOW_KEY_CODES        3
#define GET_REMAINING_BATTERY 4
#define IS_KEY_DOWN           5
#define TURN_SCREEN_ON        6
#define VIBRATE               12

static void vmSetTime(TCObject time)   // NOT IMPLEMENTED!
{
/*   tm.tm_year     = Time_year(time);
   tm.tm_mon      = Time_month(time);
   tm.tm_mday     = Time_day(time);
   tm.tm_hour     = Time_hour(time);
   tm.tm_min      = Time_minute(time);
   tm.tm_sec      = Time_second(time);*/
}

void rebootDevice()
{
   JNIEnv *env = getJNIEnv();                                      
   (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, REBOOT_DEVICE, 0);
}

static int32 vmExec(TCObject command, TCObject args, int32 launchCode, bool wait)
{                                                                                
   JNIEnv *env = getJNIEnv();                                      
   jstring jcommand = (*env)->NewString(env, (jchar*) String_charsStart(command), String_charsLen(command));
   jstring jargs = !args ? null : (*env)->NewString(env, (jchar*) String_charsStart(args), String_charsLen(args));
   int32 ret = (*env)->CallStaticIntMethod(env, applicationClass, jvmExec, jcommand, jargs, launchCode, wait);
   (*env)->DeleteLocalRef(env, jcommand);
   if (jargs) (*env)->DeleteLocalRef(env, jargs);
   return ret;
}

void vmSetAutoOff(bool enable)
{
   JNIEnv *env = getJNIEnv();                                      
   (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, SET_AUTO_OFF, (int32)enable);
}

//////////// START OF KEY INTERCEPTION FUNCTIONS

static void vmShowKeyCodes(bool show)
{
   JNIEnv *env = getJNIEnv();                                      
   (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, SHOW_KEY_CODES, (int32)show);
}

static void vmInterceptSpecialKeys(int32* keys, int32 len)
{
   if (interceptedSpecialKeys != null)
      freeArray(interceptedSpecialKeys);
   if (len == 0)
      interceptedSpecialKeys = null;
   else
   {
      int32 *dk;
      dk = interceptedSpecialKeys = newPtrArrayOf(Int32, len, null);
      if (interceptedSpecialKeys != null)
      {
         // map the TotalCross keys into the device-specific keys
         for (; len-- > 0; keys++, dk++)
            *dk = keyPortable2Device(*keys);
      }
   }
}
//////////// END OF KEY INTERCEPTION FUNCTIONS

static void vmClipboardCopy(JCharP string, int32 sLen)
{
   JNIEnv* env = getJNIEnv();
   jstring jstr = (*env)->NewString(env, (jchar*) string, sLen);
   (*env)->CallStaticObjectMethod(env, applicationClass, jclipboard, jstr);
   (*env)->DeleteLocalRef(env, jstr);
}

static TCObject vmClipboardPaste(Context currentContext)   // NOT IMPLEMENTED!
{
   JNIEnv* env = getJNIEnv();
   jstring src = (jstring) (*env)->CallStaticObjectMethod(env, applicationClass, jclipboard, 0);
   TCObject o = null;
   if (src != null)
   {
      const jchar *str = (*env)->GetStringChars(env, src, 0);
      if (str)
         o = createStringObjectFromJCharP(currentContext, (JCharP)str, (*env)->GetStringLength(env, src));
      (*env)->ReleaseStringChars(env, src, str);
   }
   (*env)->DeleteLocalRef(env, src); // guich@tc125_1
   if (o == null)
      o = createStringObjectFromCharP(currentContext, "", 0);
   return o;
}

static bool vmIsKeyDown(int32 key)
{
   int devkey = (int)keyPortable2Device(key);
   JNIEnv *env = getJNIEnv();
   return (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, IS_KEY_DOWN, devkey) == 1;
}

static int32 vmGetRemainingBattery()
{
   JNIEnv *env = getJNIEnv();                                      
   return (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, GET_REMAINING_BATTERY, 0);
}


#if 0
static void *loadLib(UtfString lname, int libPrefix/*, int syspath*/)   // NOT IMPLEMENTED!
{
   void * h;

   TCHAR path[MAX_PATH];

   // append the user path and ".dll/.so" to the library name
   int l = (*vmGlobals->ascii2unicode)(path, vmGlobals->appPath, MAX_PATH);

   // try to prefix with "lib" on Linux
   int l2 = libPrefix ? (*vmGlobals->ascii2unicode)(path+l, "lib", MAX_PATH) : 0;
   int l3 = (*vmGlobals->ascii2unicode)(path+l+l2, lname.str, MAX_PATH);
   (*vmGlobals->ascii2unicode)(path+l+l2+l3, DLL_EXT, MAX_PATH);

   // first search in the user path
   _LOG_INFO((LOG_CAT_NLIB, "LoadLibrary(%s)", path));
   h = LoadLibrary(path);
   if (h == NULL) // not found in user path?
   {
      _LOG_INFO((LOG_CAT_NLIB, "LoadLibrary2(%s)", path+l));
      h = LoadLibrary(path+l); // skip the user path
      if (h == NULL)
      {
         _LOG_ERROR((LOG_CAT_NLIB, "LoadLibrary(%s) failed", lname.str));
      }
   }
   return h;
}
#endif


static bool vmTurnScreenOn(bool on)
{                 
   JNIEnv *env = getJNIEnv();                                      
   return !env ? false : (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, TURN_SCREEN_ON, (int32)on) == 1;
}

void vmVibrate(int32 ms)
{
   JNIEnv *env = getJNIEnv();                                      
   if (env != null)
      (*env)->CallStaticIntMethod(env, applicationClass, jvmFuncI, VIBRATE, ms);
}
