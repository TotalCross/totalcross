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



#include "tcvm.h"

#define MAXIMUM_TIME (80*3600) // 80 hours

static int current,last;

#if defined WIN32
static TCHAR* getKey()
{
   static TCHAR buf[50],*t;
   if (!buf[0])
   {                // Software\Microsoft\Voice
      TCHAR* f = TEXT("Tpguxbsf]Njdsptpgu]Wpjdf");
      for (t=buf; *f; t++,f++)
         *t = *f-1;
      *t = 0;
   }
   return buf;
}
#endif

#if defined(darwin)
// implemented in demo_c.m
bool getElapsed(int32 *value);
bool setElapsed(int32 value);
#else

#ifdef WP8
char demoFN[MAX_PATH];
CharP getDemoFileName()
{
   if (demoFN[0] == 0)
   {
      char* c;
      xstrcpy(demoFN, appPath);
      xstrcat(demoFN, "\\vm.par");
      while ((c = xstrchr(demoFN, '/')) != null)
         *c = '\\';
   }
   return demoFN;
}
#endif

bool getElapsed(int32 *value)
{
   int32 err = 0;

#ifdef WP8
   FILE* f = fopen(getDemoFileName(), "rb");
   if (f)
   {
      int n = fread(value, 1, 4, f);
      if (n < 4)
         *value = 0;
      fclose(f);
   }
#elif defined WIN32
   uint32 size=4;
   HKEY handle=(HKEY)0;
   err = RegOpenKeyEx(HKEY_CURRENT_USER, getKey(), 0, KEY_READ, &handle);
   if (err == 0)
      err = RegQueryValueEx(handle,TEXT("tone"),NULL,NULL,(uint8 *)value,&size);
   RegCloseKey(handle);
#elif defined ANDROID
   JNIEnv* env = getJNIEnv();
   *value = (*env)->CallStaticIntMethod(env, applicationClass, jsetElapsed, 0);
#endif
   return err == 0;
}

bool setElapsed(int32 value)
{
   int32 err = 0;

#ifdef WP8
   FILE* f = fopen(getDemoFileName(), "wb");
   if (f)
   {
      fwrite(&value, 4, 1, f);
      fclose(f);
   }
#elif defined WIN32
   uint32 size=4;
   HKEY handle=(HKEY)0;
   err = RegCreateKeyEx(HKEY_CURRENT_USER, getKey(), 0, 0, 0, KEY_WRITE, 0, &handle, &size);
   if (err == 0)
      err = RegSetValueEx(handle,TEXT("tone"),0,REG_DWORD,(uint8*)&value,4);
   RegCloseKey(handle);
#elif defined ANDROID
   JNIEnv* env = getJNIEnv();
   (*env)->CallStaticIntMethod(env, applicationClass, jsetElapsed, value);
#endif
   return err == 0;
}
#endif

int32 checkDemo()
{
#ifdef ENABLE_DEMO
   int32 value=0,secs,hours,mins;
#if defined(WIN32) && !defined(WINCE)
   if (!isMainWindow) // don't show the copyright in window-less applications on windows (to allow CreateInstallAPK work)
      return -1;
#endif
   if (!getElapsed(&value) && !setElapsed(0)) // never set and cannot reset?
      return 0; // quit the program
   current = value;
   last = getTimeStamp();
   
   secs = MAXIMUM_TIME - current;
   if (secs < 0) secs = 0;
   hours = secs / 3600;
   mins = (secs - hours * 3600) / 60;
   
   return hours*100 + mins;
#else
   return -1;
#endif
}

bool updateDemoTime()
{
   int32 time = getTimeStamp();
   current += (time - last)/1000;
   last = time;
   return setElapsed(current) && current < MAXIMUM_TIME;
}

#include "compilation.date"

int32 getCompilationDate()
{
   return COMPILATION_MASK ^ COMPILATION_DATE;
}
