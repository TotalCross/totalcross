// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  

   IMPORTANT: RUN "p:\gitrepo\TotalCross\TotalCrossVM\builders\vc2008\fixwin32exe.bat"
   
              OTHERWISE, THE PROGRAM WILL NOT HAVE MEMORY TO RUN

 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/

#include <windows.h>
#include <stdlib.h>

#define LAUNCHER_ERROR_MODULE_PATH 10002
#define LAUNCHER_ERROR_ALLOCATION 10003
#define LAUNCHER_ERROR_COMMAND_LINE 10004
#define MAX_MODULE_PATH_CAPACITY (16U * 1024U * 1024U)

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

static size_t stringLength(const char *source)
{
   size_t length = 0;
   while (source[length])
      length++;
   return length;
}

static size_t tcharStringLength(const TCHAR *source)
{
   size_t length = 0;
   while (source[length])
      length++;
   return length;
}

static int addSize(size_t *length, size_t additional)
{
   if (additional > (size_t)-1 - *length)
      return 0;
   *length += additional;
   return 1;
}

static int appendString(char *destination, size_t capacity, size_t *position,
                        const char *source)
{
   size_t sourceLength = stringLength(source);
   size_t i;

   if (*position >= capacity || sourceLength > capacity - *position - 1)
      return 0;
   for (i = 0; i < sourceLength; i++)
      destination[*position + i] = source[i];
   *position += sourceLength;
   destination[*position] = 0;
   return 1;
}

static int appendTcharString(char *destination, size_t capacity,
                             size_t *position, const TCHAR *source)
{
   size_t sourceLength = tcharStringLength(source);
   size_t i;

   if (*position >= capacity || sourceLength > capacity - *position - 1)
      return 0;
   for (i = 0; i < sourceLength; i++)
      destination[*position + i] = (char)source[i];
   *position += sourceLength;
   destination[*position] = 0;
   return 1;
}

static TCHAR *getModulePath(size_t *lengthOut)
{
   DWORD capacity = MAX_PATH;
   DWORD length;
   TCHAR *modulePath;

   for (;;)
   {
      if ((size_t)capacity > (size_t)-1 / sizeof(TCHAR))
         return NULL;
      modulePath = (TCHAR *)malloc((size_t)capacity * sizeof(TCHAR));
      if (!modulePath)
         return NULL;

      // A return value equal to the capacity indicates truncation. The
      // terminator check also handles APIs that report capacity - 1.
      modulePath[capacity - 1] = (TCHAR)0xffff;
      length = GetModuleFileName(GetModuleHandle(0), modulePath, capacity);
      if (length != 0 && length < capacity && modulePath[length] == 0)
      {
         *lengthOut = (size_t)length;
         return modulePath;
      }

      free(modulePath);
      if (capacity > MAX_MODULE_PATH_CAPACITY / 2)
         return NULL;
      capacity *= 2;
   }
}

static int getTCZName(char *destination, size_t capacity, size_t *position,
                      const TCHAR *modulePath, size_t modulePathLength)
{
   if (modulePathLength < 3 ||
       !appendTcharString(destination, capacity, position, modulePath))
      return 0;
   // replace the .exe by the .tcz
   destination[*position - 3] = 't';
   destination[*position - 2] = 'c';
   destination[*position - 1] = 'z';
   return 1;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine, int nCmdShow)
{
   const char *cmdPrefix = " /cmd ";
   TCHAR *modulePath = NULL;
   size_t modulePathLength = 0;
   size_t cmdlineLength = 0;
   size_t position = 0;
   char *cmdline = NULL;
   int hasCommandLine;
   int ret = LAUNCHER_ERROR_COMMAND_LINE;

   modulePath = getModulePath(&modulePathLength);
   if (!modulePath || modulePathLength < 3)
   {
      ret = LAUNCHER_ERROR_MODULE_PATH;
      goto cleanup;
   }

   hasCommandLine = *lpCmdLine || (*args && *args != '1');
   if (!addSize(&cmdlineLength, modulePathLength) ||
       !addSize(&cmdlineLength, 1))
      goto cleanup;
   if (hasCommandLine)
   {
      if (!addSize(&cmdlineLength, stringLength(cmdPrefix)))
         goto cleanup;
      if (args[0] != '1' && !addSize(&cmdlineLength, stringLength(args)))
         goto cleanup;
      if (*lpCmdLine &&
          !addSize(&cmdlineLength, tcharStringLength(lpCmdLine)))
         goto cleanup;
   }

   cmdline = (char *)malloc(cmdlineLength);
   if (!cmdline)
   {
      ret = LAUNCHER_ERROR_ALLOCATION;
      goto cleanup;
   }

   if (!getTCZName(cmdline, cmdlineLength, &position, modulePath,
                   modulePathLength))
      goto cleanup;
   if (hasCommandLine)
   {
      if (!appendString(cmdline, cmdlineLength, &position, cmdPrefix))
         goto cleanup;
      if (args[0] != '1' &&
          !appendString(cmdline, cmdlineLength, &position, args))
         goto cleanup;
      if (*lpCmdLine &&
          !appendTcharString(cmdline, cmdlineLength, &position, lpCmdLine))
         goto cleanup;
   }

   ret = executeProgram(cmdline); // in tcvm\startup.c

cleanup:
   free(cmdline);
   free(modulePath);
   return ret;
}
