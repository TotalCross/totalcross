// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <android/log.h>

static CharP stripUnicode(CharP s)
{
   CharP s0 = s;
   for (; *s != 0; s++)
      if ((*s & 0xFF) > 127)
         *s = '?';
   return s0;
}

static bool privateInitDebug()
{
   return true;
}

static void privateDestroyDebug()
{
   legacyDebugConsoleDestroy("===============\n");
}

static bool privateDebug(char* str)
{
   if (strEq(str,ALTERNATIVE_DEBUG)) // is the user asking to change the mode?
      return true;
   else
   {
      bool err = true;
      if (strEq(str,ERASE_DEBUG_STR))
         legacyDebugConsoleErase();
      else
      {
         err = legacyDebugConsoleDebugLine(str, "\n", true);
         if (!legacyDebugConsoleIsOpen())
            err = true;
      }
      return err;
   }
}

// Alert
static void privateAlert(CharP str)
{
   JNIEnv *env = getJNIEnv();
   jstring jstr = (*env)->NewStringUTF(env, stripUnicode(str));
   // must check if the alert is already being show (maybe by the system itself) prior to calling another alert
   while ((*env)->GetStaticBooleanField(env, applicationClass, jshowingAlert))
      Sleep(200);
   (*env)->CallStaticVoidMethod(env, applicationClass, jalert, jstr);
   while ((*env)->GetStaticBooleanField(env, applicationClass, jshowingAlert))
      Sleep(200);
   (*env)->DeleteLocalRef(env, jstr);
}
