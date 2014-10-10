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



static VoidP tryAt(CharP prefix, CharP prefix2, CharP lib, CharP suffix)
{
   char fullpath[MAX_PATH];
   TCHAR szLibName[MAX_PATH];
   xstrprintf(fullpath, "%s%s%s%s",prefix, prefix2, lib, suffix);
   CharP2TCHARPBuf(fullpath, szLibName);

   return LoadLibrary(szLibName); // now, at the parent folder
}

VoidP privateLoadLibrary(CharP libName)
{
   HINSTANCE library;

#if !defined (WINCE)
   CharPToLower(libName); // are we loading ourselves?
#endif

   library = tryAt("","",libName,".dll");
   if (library == null)
      library = tryAt("../","",libName,".dll");
   if (library == null)
      library = tryAt(vmPath,"/",libName,".dll");
#if defined(WIN32) && !(defined(WINCE) || defined(WP8))
   if (library == null && strEq(libName,"litebase"))
   {
      TCHAR litebasePath[MAX_PATHNAME];
      if (GetEnvironmentVariable(TEXT("TOTALCROSS3_HOME"), litebasePath, MAX_PATHNAME) != 0)
      {
         tcscat(litebasePath, TEXT("/dist/vm/win32")); //flsobral@tc120_18: fixed path of LitebaseLib.tcz on Win32. Applications should now able to run from anywhere, as long as the Litebase and TotalCross home paths are set.
         library = tryAt(litebasePath,"/",libName,".dll");
      }
   }
#endif

// - guich: this freezes the program in Intermec
//   if (library == null && isWindowsMobile) // flsobral@tc113_27: WindowsCE doesn't seem to understand paths starting with two slashes.
//      library = tryAt("//TotalCross/","",libName,".dll");
   return library;
}

void privateUnloadLibrary(VoidP libPtr)
{
#if !defined (WINCE) && !defined (WP8) //XXX
   // do not free the synchronization dll please
   TCHAR libPath[MAX_PATH];
   char libName[MAX_PATH];
   GetModuleFileName(libPtr, libPath, MAX_PATH);
   TCHARP2CharPBuf(tcsrchr(libPath, '\\')+1, libName);
   CharPToLower(libName);
#endif
   FreeLibrary(libPtr);
}

VoidP privateGetProcAddress(const VoidP module, const CharP funcName)
{
   return GetProcAddress(!module ? hModuleTCVM : module, funcName);
}
