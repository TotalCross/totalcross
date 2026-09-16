// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(startup_filterApplicationCommandLine)
{
#if TC_OS_DESKTOP && TC_WINDOWING_SDL
   const char *initialCommandLine =
      "App.tcz -t /cmdlike /scr -2,-2,800,600 /cmd foo /fullscreen bar "
      "-p /tmp/app baz -testsuite qux /sdlPixelFormat auto "
      "/scrSomething /cmdlike -testsuitelike";
   int32 commandLineSize = xstrlen(initialCommandLine) + 1;
   CharP vmCommandLine = null;
   CharP applicationCommandLine = null;
   char oldAppPath[MAX_PATHNAME];
   TCWindowStartupOptions oldStartupOptions = desktopWindowStartupOptions;
   DesktopCommandLineOptions desktopCommandLineOptions;

   xstrcpy(oldAppPath, appPath);
   vmCommandLine = (CharP)malloc(commandLineSize);
   applicationCommandLine = (CharP)malloc(commandLineSize);
   if (vmCommandLine == null || applicationCommandLine == null)
   {
      TEST_FAIL(tc, "Could not allocate command-line test buffers");
      goto cleanup;
   }
   xstrcpy(vmCommandLine, (CharP)initialCommandLine);
   desktopWindowStartupOptions.initialState = TC_INITIAL_WINDOW_NORMAL;
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      commandLineSize, &desktopCommandLineOptions))
   {
      TEST_FAIL(tc, "Could not prepare the desktop command line");
      goto cleanup;
   }
   if (!desktopCommandLineOptions.testSuiteRequested
      || !desktopCommandLineOptions.traceRequested
      || !desktopCommandLineOptions.pathRequested
      || xstrcmp(desktopCommandLineOptions.path, "/tmp/app") != 0
      || desktopWindowStartupOptions.x != -2
      || desktopWindowStartupOptions.y != -2
      || desktopWindowStartupOptions.width != 800
      || desktopWindowStartupOptions.height != 600
      || !desktopWindowStartupOptions.screenSpecified
      || desktopWindowStartupOptions.initialState != TC_INITIAL_WINDOW_FULLSCREEN)
   {
      TEST_FAIL(tc, "Desktop VM options were not parsed correctly");
      goto cleanup;
   }
   if (xstrcmp(vmCommandLine,
      "App.tcz /cmdlike /cmd foo bar baz qux /scrSomething /cmdlike "
      "-testsuitelike") != 0
      || xstrcmp(applicationCommandLine,
         "foo bar baz qux /scrSomething /cmdlike -testsuitelike") != 0)
   {
      TEST_FAIL(tc, "Desktop command-line compaction was incorrect");
      goto cleanup;
   }

   xstrcpy(vmCommandLine,
      "App.tcz /cmd /admin W DEBUG /scr -2, -2, 480, 720");
   desktopWindowStartupOptions.initialState = TC_INITIAL_WINDOW_NORMAL;
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      commandLineSize, &desktopCommandLineOptions)
      || desktopWindowStartupOptions.x != -2
      || desktopWindowStartupOptions.y != -2
      || desktopWindowStartupOptions.width != 480
      || desktopWindowStartupOptions.height != 720
      || !desktopWindowStartupOptions.screenSpecified
      || xstrcmp(vmCommandLine, "App.tcz /cmd /admin W DEBUG") != 0
      || xstrcmp(applicationCommandLine, "/admin W DEBUG") != 0)
   {
      TEST_FAIL(tc, "Screen bounds payload was not fully filtered");
      goto cleanup;
   }

cleanup:
   xstrcpy(appPath, oldAppPath);
   desktopWindowStartupOptions = oldStartupOptions;
   if (applicationCommandLine != null)
      free(applicationCommandLine);
   if (vmCommandLine != null)
      free(vmCommandLine);
#else
   TEST_SKIP;
#endif
   finish: ;
}
