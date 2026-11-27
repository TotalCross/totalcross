// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#define _GNU_SOURCE

#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#if defined(__linux__) && !defined(linux)
#define linux 1
#endif

#ifndef MAX_PATHNAME
#if defined(__APPLE__)
#define MAX_PATHNAME FILENAME_MAX
#else
#define MAX_PATHNAME 1024
#endif
#endif

#ifndef _FORTIFY_SOURCE
#define _FORTIFY_SOURCE 0
#endif

#define null NULL

static char *xstrncpy(char *target, const char *source, size_t length)
{
   char *start = target;

   while (length-- > 0 && *source)
      *target++ = *source++;
   *target = 0;
   return start;
}

#include "../init/posix/startup_c.h"

#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <signal.h>
#include <sys/resource.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

static int fail(const char *message)
{
   fprintf(stderr, "copyResolvedPath test failed: %s\n", message);
   return 1;
}

static bool runChildAndCheck(bool expectAbort)
{
#if defined(__unix__) || defined(__APPLE__)
   pid_t child = fork();
   int status;

   if (child < 0)
      return false;
   if (child == 0)
   {
      char output[64];
      struct rlimit coreLimit = {0, 0};

      setrlimit(RLIMIT_CORE, &coreLimit);
      _exit(copyResolvedPath("/", output, sizeof(output)) ? 0 : 1);
   }
   if (waitpid(child, &status, 0) != child)
      return false;

#if defined(__linux__)
   if (expectAbort)
      return WIFSIGNALED(status) && WTERMSIG(status) == SIGABRT;
#else
   (void)expectAbort;
#endif
   return WIFEXITED(status) && WEXITSTATUS(status) == 0;
#else
   (void)expectAbort;
   return true;
#endif
}

static bool writeFile(const char *path)
{
   FILE *file = fopen(path, "wb");

   if (!file)
      return false;
   fputs("path helper fixture\n", file);
   return fclose(file) == 0;
}

static bool checkPath(const char *input, const char *expected)
{
   char output[PATH_MAX];

   memset(output, 0x7f, sizeof(output));
   return copyResolvedPath(input, output, sizeof(output)) &&
          strcmp(output, expected) == 0 && output[sizeof(output) - 1] == 0;
}

static int runContractCases(void)
{
   char originalDirectory[PATH_MAX];
   char temporaryDirectory[] = "/tmp/tc-realpath-test.XXXXXX";
   char subdirectory[PATH_MAX];
   char target[PATH_MAX];
   char alias[PATH_MAX];
   char expected[PATH_MAX];
   char deepDirectory[PATH_MAX];
   char deepDirectories[4][PATH_MAX];
   char deepPath[PATH_MAX];
   char output[PATH_MAX];
   char small[8];
   char oneByte = 'x';
   char zeroCapacity = 'x';
   char missingInput[] = "missing.txt";
   int depth;
   int index;

   if (!getcwd(originalDirectory, sizeof(originalDirectory)))
      return fail("getcwd failed");
   if (!mkdtemp(temporaryDirectory))
      return fail("mkdtemp failed");

   snprintf(subdirectory, sizeof(subdirectory), "%s/sub", temporaryDirectory);
   snprintf(target, sizeof(target), "%s/file.txt", temporaryDirectory);
   snprintf(alias, sizeof(alias), "%s/alias.txt", temporaryDirectory);
   if (mkdir(subdirectory, 0700) != 0 || !writeFile(target))
      return fail("could not create absolute-path fixture");
   if (symlink("file.txt", alias) != 0)
      return fail("could not create symlink fixture");
   if (!realpath(target, expected))
      return fail("could not resolve expected fixture path");

   if (!checkPath(target, expected))
      return fail("existing absolute path did not resolve");
   if (chdir(temporaryDirectory) != 0)
      return fail("could not enter fixture directory");
   if (!checkPath("file.txt", expected))
      return fail("existing relative path did not resolve");
   if (!checkPath("sub/../file.txt", expected))
      return fail("relative components were not normalized");
   if (!checkPath("alias.txt", expected))
      return fail("symbolic link did not resolve to its target");

   memset(output, 0x7f, sizeof(output));
   if (!copyResolvedPath(missingInput, output, sizeof(output)) ||
       strcmp(output, missingInput) != 0 || strcmp(missingInput, "missing.txt") != 0)
      return fail("nonexistent input did not preserve its original path");

   memset(output, 0x7f, sizeof(output));
   if (copyResolvedPath(NULL, output, sizeof(output)) || output[0] != 0)
      return fail("null input did not clear output and return false");
   output[0] = 'x';
   if (copyResolvedPath("", output, sizeof(output)) || output[0] != 0)
      return fail("empty input did not clear output and return false");
   if (copyResolvedPath("/", NULL, 0) || zeroCapacity != 'x')
      return fail("zero output capacity did not return false");
   if (copyResolvedPath("/", &zeroCapacity, 0) || zeroCapacity != 'x')
      return fail("zero output capacity modified output");

   if (!copyResolvedPath("/", &oneByte, 1) || oneByte != 0)
      return fail("one-byte output capacity did not terminate");
   index = (int)strlen(expected) + 1;
   if (!copyResolvedPath("file.txt", output, (size_t)index) ||
       strcmp(output, expected) != 0)
      return fail("exact-fit output capacity did not copy the full path");
   memset(small, 0x7f, sizeof(small));
   if (!copyResolvedPath("file.txt", small, sizeof(small)) ||
       small[sizeof(small) - 1] != 0 || strncmp(small, expected, sizeof(small) - 1) != 0)
      return fail("insufficient output capacity did not preserve truncation");

   depth = snprintf(deepDirectory, sizeof(deepDirectory), "%s", temporaryDirectory);
   for (index = 0; index < 4; index++)
   {
      char component[96];
      int componentLength = snprintf(component, sizeof(component),
                                     "segment_%d_%078d", index, 0);

      if (componentLength <= 0 || depth + componentLength + 2 >= (int)sizeof(deepDirectory))
         return fail("deep path fixture exceeded its local buffer");
      deepDirectory[depth++] = '/';
      memcpy(deepDirectory + depth, component, (size_t)componentLength + 1);
      depth += componentLength;
      if (mkdir(deepDirectory, 0700) != 0)
         return fail("could not create deep path fixture");
      snprintf(deepDirectories[index], sizeof(deepDirectories[index]),
               "%s", deepDirectory);
   }
   snprintf(deepPath, sizeof(deepPath), "%s/deep.txt", deepDirectory);
   if (!writeFile(deepPath) || !realpath(deepPath, expected))
      return fail("could not create or resolve deep fixture");
   memset(small, 0x7f, sizeof(small));
   if (!copyResolvedPath(deepPath, small, sizeof(small)) ||
       small[sizeof(small) - 1] != 0 || strncmp(small, expected, sizeof(small) - 1) != 0)
      return fail("resolved path longer than destination did not truncate safely");

   for (index = 0; index < 10000; index++)
   {
      if (!copyResolvedPath("file.txt", output, sizeof(output)) ||
          !copyResolvedPath("missing.txt", output, sizeof(output)))
         return fail("repeated successful or fallback resolution failed");
   }

   if (chdir(originalDirectory) != 0)
      return fail("could not restore working directory");
   unlink(alias);
   unlink(target);
   unlink(deepPath);
   for (index = 3; index >= 0; index--)
   {
      if (rmdir(deepDirectories[index]) != 0)
         return fail("could not remove deep path fixture");
   }
   rmdir(subdirectory);
   rmdir(temporaryDirectory);
   puts("copyResolvedPath contract cases passed");
   return 0;
}

int main(int argc, char **argv)
{
   if (argc != 2)
      return fail("expected --expect-abort, --expect-safe, or --run-cases");
   if (strcmp(argv[1], "--expect-abort") == 0)
   {
#if defined(__linux__)
      if (!runChildAndCheck(true))
         return fail("fixed-buffer helper did not abort in a child process");
      printf("baseline reproduced: signal=SIGABRT buffer=%d PATH_MAX=%d FORTIFY_SOURCE=%d\n",
             MAX_PATHNAME, PATH_MAX, _FORTIFY_SOURCE);
#elif defined(__APPLE__)
      puts("baseline reproduction is Linux/glibc-specific");
#else
      puts("baseline reproduction is Linux/glibc-specific");
#endif
      return 0;
   }
   if (strcmp(argv[1], "--expect-safe") == 0)
   {
      if (!runChildAndCheck(false))
         return fail("copyResolvedPath did not return normally in a child process");
      puts("copyResolvedPath short-path startup completed in a child process");
      return 0;
   }
   if (strcmp(argv[1], "--run-cases") == 0)
      return runContractCases();
   return fail("unknown test mode");
}
