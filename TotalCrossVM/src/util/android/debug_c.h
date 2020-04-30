// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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

static FILE* fdebug;

static bool privateInitDebug()
{
   return true;
}

void closeDebug()
{
   if (fdebug != NULL)
      fclose(fdebug);
   fdebug = NULL;
}

static void privateDestroyDebug()
{
   if (fdebug)
   {
      fputs("===============\n",fdebug);
      closeDebug();
   }
}

static bool privateDebug(char* str)
{
   if (strEq(str,ALTERNATIVE_DEBUG)) // is the user asking to change the mode?
      return true;
   else
   {
      static char debugPath[MAX_PATHNAME];
      bool err = true;
      if (!fdebug)
      {
         xstrprintf(debugPath, "%s/DebugConsole.txt", appPath);
         fdebug = fopen(debugPath, "ab+"); //flsobral@tc110: replaced mode "wb" with "ab+".
      }
      if (fdebug)
      {
         if (strEq(str,ERASE_DEBUG_STR))
         {
            closeDebug();
            remove(debugPath);
         }
         else
         {
            fputs(str,fdebug);
            err = (fputs("\n",fdebug) >= 0);
            fflush(fdebug);
            fsync(fileno(fdebug));
         }
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
