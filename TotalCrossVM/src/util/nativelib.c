// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcvm.h"

#if defined(WINCE) || defined(WIN32)
 #include "win/nativelib_c.h"
#else
 #include "posix/nativelib_c.h"
#if __APPLE__
#undef bool
#define bool int
#endif
#endif

VoidP loadLibrary(const char* libName)
{
   return privateLoadLibrary((char*)libName);
}

void unloadLibrary(VoidP libPtr)
{
   privateUnloadLibrary(libPtr);
}

VoidP getProcAddress(const VoidP module, const char* funcName)
{
   return privateGetProcAddress(module, (char*)funcName);
}

#if (defined (darwin) && !defined (THEOS)) || defined(WP8)  || defined(ANDROID)
bool LibOpen(OpenParams params);
void LibClose();

bool attachNativeLib(Context currentContext, CharP name)
{
    NativeLib lib;
    TOpenParams params;
    
    if (!strEq(name, "Litebase"))
        return false;
    if ((lib = newX(NativeLib)) != null)
    {
        params.commandLine = commandLine;
        params.alert = alert;
        params.getProcAddress = getProcAddress;
        params.currentContext = currentContext;
        lib->LibOpen     = LibOpen;
        lib->LibClose    = LibClose;
        if (lib->LibOpen(&params))
        {
            openNativeLibs = VoidPsAdd(openNativeLibs, lib, null);
            return true;
        }
        else xfree(lib);
    }
    return false;
}
#else
bool attachNativeLib(Context currentContext, CharP name)
{
   VoidP h;
   NativeLib lib;
   TOpenParams params;
   bool ok = false;

   h = loadLibrary(name);
   if (h != null && (lib = newX(NativeLib)) != null)
   {
#ifdef WIN32
      params.mainHWnd = mainHWnd;      
#endif
      params.commandLine = commandLine;
      params.alert = alert;
      params.getProcAddress = getProcAddress;
      params.currentContext = currentContext;
      lib->handle = h;
      lib->LibOpen     = (NativeLibOpenFunc)    getProcAddress(h,"LibOpen");
      lib->LibClose    = (NativeLibCloseFunc)   getProcAddress(h,"LibClose");
      lib->HandleEvent = (NativeLibHandleEventFunc)getProcAddress(h,"HandleEvent");
      if (lib->LibOpen)
      {
         ok = lib->LibOpen(&params);
      }
      else throwException(currentContext, RuntimeException, "Native library %s does not implement the 'LibOpen' function.",name);
      if (ok)
      {
         openNativeLibs = VoidPsAdd(openNativeLibs, lib, null);
         return true;
      }
      else xfree(lib);
   }
   return false;
}
#endif

bool handleEvent(VoidP event)
{
   bool ok = false;
   VoidPs *list, *head;
   head = list = openNativeLibs;
   if (list != null)
   do
   {
      NativeLib lib = (NativeLib)list->value;
      if (lib->HandleEvent != null)
      {
         ok = lib->HandleEvent(event);
         if (ok)
            return true;
      }
      list = list->next;
   } while (head != list);
   return false;
}

VoidP findProcAddress(CharP funcName, uint32* ref)
{
   VoidPs *list, *head;
   head = list = openNativeLibs;
   if (list != null)
   do
   {
      NativeLib lib = (NativeLib)list->value;
      VoidP addr = getProcAddress(lib->handle, funcName);
      if (addr != null)
      {
         *ref = lib->ref;
         return addr;
      }
      list = list->next;
   } while (head != list);
   return null;
}

void destroyNativeLib() // no thread are running here
{
   VoidPs* list = openNativeLibs;
   VoidPs* head = list;
   if (list != null)
   do
   {
      NativeLib lib = (NativeLib)list->value;
      if (lib->LibClose)
         lib->LibClose();
      unloadLibrary(lib->handle);
      xfree(lib);
      list = list->next;
   } while (head != list);
   VoidPsDestroy(openNativeLibs, null);
}
