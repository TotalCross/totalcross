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



#if defined(darwin)
 #include <dlfcn.h>
 #include "sys/syslimits.h"
 #define SHLIB_SUFFIX "dylib"
 #define VM_PATH   "/Applications/TotalCross.app/libtcvm." SHLIB_SUFFIX
#elif defined(linux) || defined(ANDROID)
 #include <dlfcn.h>
 #include <limits.h>
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
#if defined darwin || defined ANDROID
    return (NativeMethod)htGetPtr(&htNativeProcAddresses, hashCode(funcName));
#else
   void *tcvm = module ? module : dlopen(TEXT(VM_PATH), RTLD_LAZY);
   if (tcvm)
      return dlsym(tcvm, funcName);
#endif
   return NULL;
}
