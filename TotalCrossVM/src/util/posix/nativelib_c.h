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

#if defined(darwin)
 #include <dlfcn.h>
 #include "sys/syslimits.h"
 #define SHLIB_SUFFIX "dylib"
 #define VM_PATH   "/Applications/TotalCross.app/libtcvm." SHLIB_SUFFIX
#elif defined(linux) || defined(ANDROID)
 #include <dlfcn.h> 
#include <limits.h>
#ifndef PATH_MAX
#define PATH_MAX 4096
#endif
 #define SHLIB_SUFFIX "so"
 #ifdef ANDROID
  #define VM_PATH   "/data/data/totalcross/android/libtcvm." SHLIB_SUFFIX
 #else
  #define VM_PATH   "/usr/lib/totalcross/libtcvm." SHLIB_SUFFIX
 #endif
#else
 #error "Undefined VM installation PATH"
#endif

static VoidP tryAt(CharP prefix, CharP prefix2, CharP lib)
{     
   char fullpath[PATH_MAX];
   TCHAR szLibName[PATH_MAX];

   xstrprintf(fullpath, "%s%slib%s.%s",prefix, prefix2, lib, SHLIB_SUFFIX);
   CharP2TCHARPBuf(fullpath, szLibName);
   return dlopen(szLibName, RTLD_LAZY);
}

VoidP privateLoadLibrary(CharP libName)
{
   VoidP library = null;
#ifdef ANDROID
   CharPToLower(libName);
#endif
#ifdef ANDROID
   if (library == null)
      library = tryAt(vmPath,"/lib/",libName); // needed for single apk applications
#endif      
   if (library == null)
      library = tryAt("","",libName);
   if (library == null)
      library = tryAt("../","",libName);
   if (library == null)
      library = tryAt(vmPath,"/",libName);
   return library;
}

void privateUnloadLibrary(VoidP libPtr)
{
#ifndef ANDROID   
   dlclose(libPtr);
#endif   
}

VoidP privateGetProcAddress(const VoidP module, const CharP funcName)
{
#if defined darwin || defined ANDROID || defined HEADLESS
    return (NativeMethod)htGetPtr(&htNativeProcAddresses, hashCode(funcName));
#else
   void *tcvm = module ? module : dlopen(TEXT(VM_PATH), RTLD_LAZY);
   if (tcvm)
      return dlsym(tcvm, funcName);
#endif
   return NULL;
}
