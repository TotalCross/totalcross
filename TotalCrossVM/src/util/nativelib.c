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

// $Id: nativelib.c,v 1.23 2011-01-04 13:31:09 guich Exp $

#include "tcvm.h"

#if defined(WINCE) || defined(WIN32)
 #include "win/nativelib_c.h"
#elif defined(PALMOS)
 #include "palm/nativelib_c.h"
#elif defined(__SYMBIAN32__)
 #include "symbian/nativelib_c.h"
#else
 #include "posix/nativelib_c.h"
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

bool attachNativeLib(Context currentContext, CharP name)
{
   VoidP h;
   NativeLib lib;
   TOpenParams params;
   bool ok = false;

   h = loadLibrary(name);
   if (h != null && (lib = newX(NativeLib)) != null)
   {
#ifdef PALMOS
      uint32 *got_ptr = h; // first uint32 field of PealModule is the GOT
      lib->ref = SWAP32_FORCED(*got_ptr);
      params.ref = lib->ref;
#elif defined(WIN32)
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
         EnterLibrary(lib->ref)
         ok = lib->LibOpen(&params);
         ExitLibrary()
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
         EnterLibrary(lib->ref)
         ok = lib->HandleEvent(event);
         ExitLibrary()
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
      {
         EnterLibrary(lib->ref)
         lib->LibClose();
         ExitLibrary()
      }
      unloadLibrary(lib->handle);
      xfree(lib);
      list = list->next;
   } while (head != list);
   VoidPsDestroy(openNativeLibs, null);
}
