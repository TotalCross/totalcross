// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#define waitUntilStarted()

bool wokeUp()
{
   return false;
}

static void registerWake(bool set)
{
   UNUSED(set);
}

#if defined(linux) || defined(darwin) || defined(__APPLE__)
#if !defined(__APPLE__)
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif
#endif
#include <dlfcn.h>
#include <stdlib.h>
#endif

#if defined(__APPLE__)
#include <mach-o/dyld.h>
#endif

#if defined(linux) || defined(__APPLE__)
#include <unistd.h>

static bool copyResolvedPath(const char *path, char *out, size_t outSize)
{
   char resolved[MAX_PATHNAME];
   const char *resolvedPath;

   if (outSize == 0)
      return false;

   out[0] = 0;

   if (path == null || *path == 0)
      return false;

   resolvedPath = realpath(path, resolved) ? resolved : path;
   xstrncpy(out, resolvedPath, outSize - 1);
   out[outSize - 1] = 0;
   return true;
}

static bool stripFileName(char *path)
{
   char *slash = xstrrchr(path, '/');

   if (slash == null)
      return false;

   if (slash == path)
      slash[1] = 0;
   else
      *slash = 0;

   return true;
}

static bool copyFileName(const char *path, char *out, size_t outSize)
{
   const char *slash;

   if (outSize == 0)
      return false;

   out[0] = 0;

   if (path == null || *path == 0)
      return false;

   slash = xstrrchr(path, '/');
   xstrncpy(out, slash ? slash + 1 : path, outSize - 1);
   out[outSize - 1] = 0;
   return true;
}

static bool getMainExecutablePath(char *out, size_t outSize)
{
#if defined(__APPLE__)
   char path[MAX_PATHNAME];
   uint32_t len = sizeof(path);

   if (_NSGetExecutablePath(path, &len) != 0)
      return false;

   return copyResolvedPath(path, out, outSize);
#elif defined(linux)
   char path[MAX_PATHNAME];
   int len = readlink("/proc/self/exe", path, sizeof(path) - 1);

   if (len <= 0)
      return false;

   path[len] = 0;
   return copyResolvedPath(path, out, outSize);
#else
   UNUSED(out);
   UNUSED(outSize);
   return false;
#endif
}

static bool getMainExecutableDir(char *out, size_t outSize)
{
   if (!getMainExecutablePath(out, outSize))
      return false;

   return stripFileName(out);
}

static bool getMainExecutableName(char *out, size_t outSize)
{
   char path[MAX_PATHNAME];

   if (!getMainExecutablePath(path, sizeof(path)))
      return false;

   return copyFileName(path, out, outSize);
}

static bool getThisLibraryDir(char *out, size_t outSize)
{
   Dl_info info;

   if (!dladdr((const void *)&getThisLibraryDir, &info) || !info.dli_fname)
      return false;

   if (!copyResolvedPath(info.dli_fname, out, outSize))
      return false;

   return stripFileName(out);
}
#endif

#if defined(darwin)
void setFullScreen();
void privateGetWorkingDir(CharP vmPath, CharP appPath);

static void getWorkingDir()
{
   privateGetWorkingDir(vmPath, appPath);
}

#else

void setFullScreen()
{
}

static void getWorkingDir()
{
   char path[MAX_PATHNAME];

   if (getThisLibraryDir(path, sizeof(path)))
      TCHARP2CharPBuf(path, vmPath);
   else
      TCHARP2CharPBuf("/usr/lib/totalcross", vmPath);

   if (getMainExecutableDir(path, sizeof(path)))
      TCHARP2CharPBuf(path, appPath);
   else
      TCHARP2CharPBuf(".", appPath);

   if (getMainExecutableName(path, sizeof(path)))
      TCHARP2CharPBuf(path, exeName);
}
#endif
