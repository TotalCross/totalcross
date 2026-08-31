// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(startup_filterApplicationCommandLine)
{
#if TC_OS_DESKTOP && TC_WINDOWING_SDL
   char vmCommandLine[512] =
      "App.tcz /scr -2,-2,800,600 /cmd foo -t /fullscreen bar "
      "-p /tmp/app baz -testsuite qux /sdlPixelFormat auto "
      "/scrSomething /cmdlike -testsuitelike";
   char applicationCommandLine[256];
   char filteredApplicationCommandLine[256];
   char oldAppPath[MAX_PATHNAME];
   int32 oldDefScrX = defScrX;
   int32 oldDefScrY = defScrY;
   int32 oldDefScrW = defScrW;
   int32 oldDefScrH = defScrH;
   TCInitialWindowState oldWindowState = initialWindowState;
   bool testSuiteRequested;

   xstrcpy(oldAppPath, appPath);
   initialWindowState = TC_INITIAL_WINDOW_NORMAL;
   if (!prepareDesktopCommandLines(vmCommandLine, applicationCommandLine,
      sizeof(applicationCommandLine)))
   {
      TEST_FAIL(tc, "Could not prepare the desktop command line");
      goto cleanup;
   }
   if (!filterApplicationCommandLine(applicationCommandLine,
      filteredApplicationCommandLine, sizeof(filteredApplicationCommandLine),
      &testSuiteRequested))
   {
      TEST_FAIL(tc, "Could not filter the application command line");
      goto cleanup;
   }
   if (!testSuiteRequested || defScrX != -2 || defScrY != -2
      || defScrW != 800 || defScrH != 600
      || initialWindowState != TC_INITIAL_WINDOW_FULLSCREEN
      || xstrstr(filteredApplicationCommandLine, "foo") == null
      || xstrstr(filteredApplicationCommandLine, "bar") == null
      || xstrstr(filteredApplicationCommandLine, "baz") == null
      || xstrstr(filteredApplicationCommandLine, "qux") == null
      || xstrstr(filteredApplicationCommandLine, "/scrSomething") == null
      || xstrstr(filteredApplicationCommandLine, "/cmdlike") == null
      || xstrstr(filteredApplicationCommandLine, "-testsuitelike") == null
      || xstrstr(filteredApplicationCommandLine, "/cmd") != null
      || xstrstr(filteredApplicationCommandLine, " -t ") != null
      || xstrstr(filteredApplicationCommandLine, " -p ") != null
      || xstrstr(filteredApplicationCommandLine, " -testsuite ") != null
      || xstrstr(filteredApplicationCommandLine, "/scr ") != null
      || xstrstr(filteredApplicationCommandLine, "/fullscreen ") != null
      || xstrstr(filteredApplicationCommandLine, "/sdlPixelFormat ") != null)
      TEST_FAIL(tc, "Desktop VM options were not filtered correctly");

cleanup:
   xstrcpy(appPath, oldAppPath);
   defScrX = oldDefScrX;
   defScrY = oldDefScrY;
   defScrW = oldDefScrW;
   defScrH = oldDefScrH;
   initialWindowState = oldWindowState;
#else
   TEST_SKIP;
#endif
   finish: ;
}
