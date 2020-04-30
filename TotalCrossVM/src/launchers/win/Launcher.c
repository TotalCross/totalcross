// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  

   IMPORTANT: RUN "p:\gitrepo\TotalCross\TotalCrossVM\builders\vc2008\fixwin32exe.bat"
   
              OTHERWISE, THE PROGRAM WILL NOT HAVE MEMORY TO RUN

 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

#include <windows.h>

char *args = "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

typedef int (*ExecuteProgramFunc)(char* args);

static int executeProgram(char* cmdline)
{
   int ret;
   ExecuteProgramFunc fExecuteProgram = NULL;
   HINSTANCE tcvm;

   tcvm = LoadLibrary(TEXT("tcvm.dll"));               // load in current folder - otherwise, we'll not be able to debug
   if (!tcvm)
   {
      TCHAR dir[MAX_PATH],*c; // LoadLibrary does not accept .. in the name, so we have to get the current path and build up the full path
      int n=2;
      GetModuleFileName(GetModuleHandle(0), dir, MAX_PATH); // get the path to the exe
      for (c = dir + lstrlen(dir)-1; c > dir; c--)
         if (*c == '\\' && --n == 0)
            break;
      if (*c == '\\' && n == 0)
      {
         lstrcpy(c+1, TEXT("tcvm.dll"));
         tcvm = LoadLibrary(dir);    // load in parent folder
      }
   }
   if (!tcvm)
      tcvm = LoadLibrary(TEXT("\\TotalCross\\tcvm.dll")); // load in most common absolute path
   if (!tcvm)
   {
      MessageBox(0,TEXT("TCVM.dll not found."), TEXT("Fatal Error"), MB_OK|MB_TOPMOST);
      return 10000;
   }

   fExecuteProgram = (ExecuteProgramFunc)GetProcAddress(tcvm, TEXT("executeProgram"));
   if (!fExecuteProgram)
      return 10001;

   ret = fExecuteProgram(cmdline); // call the function now
   FreeLibrary(tcvm); // free the library
   return ret;
}

char* xstrcat(char* dst, TCHAR* tsrc, char* src)
{
   while (*dst) dst++; // go to end
   if (tsrc) while (*tsrc) *dst++ = (char)*tsrc++;
   if (src)  while (*src) *dst++ = (char)*src++;
   *dst = 0;
   return dst;
}

static void getTCZName(char *c)
{
   TCHAR moduleT[256];
   GetModuleFileName(0, moduleT, sizeof(moduleT));
   c = xstrcat(c, moduleT, 0);
   // replace the .exe by the .tcz
   c[-3] = 't';
   c[-2] = 'c';
   c[-1] = 'z';
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine, int nCmdShow)
{
   char cmdline[512];
   memset(cmdline,0,sizeof(cmdline));
   getTCZName(cmdline);
   if (*lpCmdLine || (*args && *args != '1')) // if there's a commandline passed by the system or one passed by the user
   {
      xstrcat(cmdline, 0, " /cmd ");
      if (args[0] != '1')
         xstrcat(cmdline, 0, args);
      if (*lpCmdLine)
         xstrcat(cmdline, lpCmdLine, 0);
   }
   return executeProgram(cmdline); // in tcvm\startup.c
}
