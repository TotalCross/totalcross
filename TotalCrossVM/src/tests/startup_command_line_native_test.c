// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Keep the real desktop parser in this test translation unit while avoiding
 * duplicate VM entry points from the linked tcvm library. */
#define exitProgram tcStartupCommandLineTest_exitProgram
#define startProgram tcStartupCommandLineTest_startProgram
#define startVM tcStartupCommandLineTest_startVM
#define executeProgram tcStartupCommandLineTest_executeProgram
#define wokeUp tcStartupCommandLineTest_wokeUp
#include "../init/startup.c"
#undef exitProgram
#undef startProgram
#undef startVM
#undef executeProgram
#undef wokeUp

static int failures;

static void failCase(const char *name, const char *reason)
{
   fprintf(stderr, "FAIL case=%s reason=%s\n", name, reason);
   failures++;
}

static void runCase(const char *name, const char *commandLine,
   const char *expectedVMCommandLine, const char *expectedApplicationCommandLine,
   bool expectTrace, bool expectPath, bool expectTestSuite,
   bool expectScreen, int32 expectedScreenX, int32 expectedScreenY,
   int32 expectedScreenWidth, int32 expectedScreenHeight,
   TCInitialWindowState expectedInitialState)
{
   CharP vmCommandLine = null;
   CharP applicationCommandLine = null;
   DesktopCommandLineOptions commandOptions;
   size_t commandLineSize = strlen(commandLine) + 1;

   vmCommandLine = (CharP)malloc(commandLineSize);
   applicationCommandLine = (CharP)malloc(commandLineSize);
   if (vmCommandLine == null || applicationCommandLine == null)
   {
      failCase(name, "could not allocate command-line buffers");
      goto cleanup;
   }
   memcpy(vmCommandLine, commandLine, commandLineSize);
   applicationCommandLine[0] = '\0';
   desktopWindowStartupOptions.initialState = TC_INITIAL_WINDOW_NORMAL;
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      (int32)commandLineSize, &commandOptions))
   {
      failCase(name, "startup command-line parsing failed");
      goto cleanup;
   }
   if (strcmp(vmCommandLine, expectedVMCommandLine) != 0
      || strcmp(applicationCommandLine, expectedApplicationCommandLine) != 0)
   {
      failCase(name, "command-line output differed");
      goto cleanup;
   }
   if (commandOptions.traceRequested != expectTrace
      || commandOptions.pathRequested != expectPath
      || commandOptions.testSuiteRequested != expectTestSuite
      || desktopWindowStartupOptions.initialState != expectedInitialState)
   {
      failCase(name, "startup options were not preserved");
      goto cleanup;
   }
   if (expectPath && strcmp(commandOptions.path, "/tmp/app") != 0)
   {
      failCase(name, "-p payload was not consumed correctly");
      goto cleanup;
   }
   if (expectScreen && (!desktopWindowStartupOptions.screenSpecified
      || desktopWindowStartupOptions.x != expectedScreenX
      || desktopWindowStartupOptions.y != expectedScreenY
      || desktopWindowStartupOptions.width != expectedScreenWidth
      || desktopWindowStartupOptions.height != expectedScreenHeight))
   {
      failCase(name, "/scr payload was not consumed correctly");
      goto cleanup;
   }

cleanup:
   if (applicationCommandLine != null)
      free(applicationCommandLine);
   if (vmCommandLine != null)
      free(vmCommandLine);
}

static void runLongCase(void)
{
   const char *inputPrefix =
      "App.tcz /scr -2,-2,800,600 -p /tmp/app -t /cmd ";
   const char *expectedPrefix = "App.tcz /cmd ";
   const size_t applicationLength = 700;
   const size_t inputPrefixLength = strlen(inputPrefix);
   const size_t expectedPrefixLength = strlen(expectedPrefix);
   const size_t applicationSize = applicationLength + 1;
   const size_t inputSize = inputPrefixLength + applicationLength + 1;
   const size_t expectedVMSize = expectedPrefixLength + applicationLength + 1;
   CharP commandLine = null;
   CharP expectedVMCommandLine = null;
   CharP expectedApplicationCommandLine = null;

   commandLine = (CharP)malloc(inputSize);
   expectedVMCommandLine = (CharP)malloc(expectedVMSize);
   expectedApplicationCommandLine = (CharP)malloc(applicationSize);
   if (commandLine == null || expectedVMCommandLine == null
      || expectedApplicationCommandLine == null)
   {
      failCase("long-command-line", "could not allocate long command-line buffers");
      goto cleanup;
   }

   memcpy(commandLine, inputPrefix, inputPrefixLength);
   memset(commandLine + inputPrefixLength, 'x', applicationLength);
   commandLine[inputSize - 1] = '\0';
   memcpy(expectedVMCommandLine, expectedPrefix, expectedPrefixLength);
   memset(expectedVMCommandLine + expectedPrefixLength, 'x', applicationLength);
   expectedVMCommandLine[expectedVMSize - 1] = '\0';
   memset(expectedApplicationCommandLine, 'x', applicationLength);
   expectedApplicationCommandLine[applicationLength] = '\0';

   runCase("long-command-line", commandLine, expectedVMCommandLine,
      expectedApplicationCommandLine, true, true, false, true,
      -2, -2, 800, 600, TC_INITIAL_WINDOW_NORMAL);

cleanup:
   if (expectedApplicationCommandLine != null)
      free(expectedApplicationCommandLine);
   if (expectedVMCommandLine != null)
      free(expectedVMCommandLine);
   if (commandLine != null)
      free(commandLine);
}

int main(void)
{
   runCase("short-command-line",
      "App.tcz -t /cmdlike /scr -2,-2,800,600 /cmd foo /fullscreen bar "
      "-p /tmp/app baz -testsuite qux /sdlPixelFormat auto "
      "/scrSomething /cmdlike -testsuitelike",
      "App.tcz /cmdlike /cmd foo bar baz qux /scrSomething /cmdlike "
      "-testsuitelike",
      "foo bar baz qux /scrSomething /cmdlike -testsuitelike", true, true,
      true, true, -2, -2, 800, 600, TC_INITIAL_WINDOW_FULLSCREEN);
   runCase("screen-payload",
      "App.tcz /cmd /admin W DEBUG /scr -2, -2, 480, 720",
      "App.tcz /cmd /admin W DEBUG", "/admin W DEBUG", false, false, false,
      true, -2, -2, 480, 720, TC_INITIAL_WINDOW_NORMAL);
   runLongCase();

   if (failures != 0)
   {
      fprintf(stderr, "startup_command_line_failures=%d\n", failures);
      return 1;
   }
   puts("Startup command-line tests passed");
   return 0;
}
