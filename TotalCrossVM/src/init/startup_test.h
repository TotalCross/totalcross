// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(startup_filterApplicationCommandLine)
{
#if TC_OS_DESKTOP && TC_WINDOWING_SDL
   char vmCommandLine[512] =
      "App.tcz -t /cmdlike /scr -2,-2,800,600 /cmd foo /fullscreen bar "
      "-p /tmp/app baz -testsuite qux /sdlPixelFormat auto "
      "/scrSomething /cmdlike -testsuitelike";
   char applicationCommandLine[256];
   char oldAppPath[MAX_PATHNAME];
   int32 oldDefScrX = defScrX;
   int32 oldDefScrY = defScrY;
   int32 oldDefScrW = defScrW;
   int32 oldDefScrH = defScrH;
   bool oldDefScrSpecified = defScrSpecified;
   TCInitialWindowState oldWindowState = initialWindowState;
   DesktopCommandLineOptions desktopCommandLineOptions;

   xstrcpy(oldAppPath, appPath);
   initialWindowState = TC_INITIAL_WINDOW_NORMAL;
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      sizeof(applicationCommandLine), &desktopCommandLineOptions))
   {
      TEST_FAIL(tc, "Could not prepare the desktop command line");
      goto cleanup;
   }
   if (!desktopCommandLineOptions.testSuiteRequested
      || !desktopCommandLineOptions.traceRequested
      || !desktopCommandLineOptions.pathRequested
      || xstrcmp(desktopCommandLineOptions.path, "/tmp/app") != 0
      || defScrX != -2 || defScrY != -2
      || defScrW != 800 || defScrH != 600
      || !defScrSpecified
      || initialWindowState != TC_INITIAL_WINDOW_FULLSCREEN)
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
   initialWindowState = TC_INITIAL_WINDOW_NORMAL;
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      sizeof(applicationCommandLine), &desktopCommandLineOptions)
      || defScrX != -2 || defScrY != -2
      || defScrW != 480 || defScrH != 720
      || !defScrSpecified
      || xstrcmp(vmCommandLine, "App.tcz /cmd /admin W DEBUG") != 0
      || xstrcmp(applicationCommandLine, "/admin W DEBUG") != 0)
   {
      TEST_FAIL(tc, "Screen bounds payload was not fully filtered");
      goto cleanup;
   }

cleanup:
   xstrcpy(appPath, oldAppPath);
   defScrX = oldDefScrX;
   defScrY = oldDefScrY;
   defScrW = oldDefScrW;
   defScrH = oldDefScrH;
   defScrSpecified = oldDefScrSpecified;
   initialWindowState = oldWindowState;
#else
   TEST_SKIP;
#endif
   finish: ;
}
