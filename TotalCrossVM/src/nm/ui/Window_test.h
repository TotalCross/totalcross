// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(windowResolveStartupConfiguration)
{
#if TC_OS_DESKTOP
   struct StartupConfigurationCase
   {
      int16 appTczAttr;
      bool screenSpecified;
      int32 x;
      int32 y;
      int32 width;
      int32 height;
      int32 environmentWidth;
      int32 environmentHeight;
      TCInitialWindowState initialState;
      bool legacyFullscreen;
      int32 expectedWidth;
      int32 expectedHeight;
      TCWindowPositionMode expectedXMode;
      TCWindowPositionMode expectedYMode;
      int32 expectedX;
      int32 expectedY;
      bool expectedFullscreen;
      bool expectedMaximized;
      bool expectedResizable;
   } cases[] = {
      { ATTR_WINDOWSIZE_320X480 | ATTR_RESIZABLE_WINDOW, true, 40, 50, 700, 500,
        600, 400, TC_INITIAL_WINDOW_NORMAL, false, 700, 500,
        TC_WINDOW_POSITION_EXPLICIT, TC_WINDOW_POSITION_EXPLICIT, 40, 50,
        false, false, true },
      { ATTR_WINDOWSIZE_320X480, true, -2, -1, -1, -1, 600, 400,
        TC_INITIAL_WINDOW_NORMAL, false, 800, 500,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_DEFAULT, -2, -1,
        false, false, false },
      { ATTR_WINDOWSIZE_320X480, false, -1, -1, -1, -1, 600, 400,
        TC_INITIAL_WINDOW_NORMAL, false, 600, 400,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        false, false, false },
      { ATTR_WINDOWSIZE_320X480, false, -1, -1, -1, -1, 600, -1,
        TC_INITIAL_WINDOW_NORMAL, false, 600, 480,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_CENTER, 0, 0,
        false, false, false },
      { ATTR_WINDOWSIZE_320X480, false, -1, -1, -1, -1, -1, 400,
        TC_INITIAL_WINDOW_NORMAL, false, 320, 400,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_CENTER, 0, 0,
        false, false, false },
      { ATTR_WINDOWSIZE_320X480, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_NORMAL, false, 320, 480,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_CENTER, 0, 0,
        false, false, false },
      { ATTR_WINDOWSIZE_480X640, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_NORMAL, false, 480, 640,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_CENTER, 0, 0,
        false, false, false },
      { ATTR_WINDOWSIZE_600X800, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_NORMAL, false, 600, 700,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_CENTER, 0, 0,
        false, false, false },
      { 0, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_NORMAL, false, 800, 500,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        false, false, false },
      { 0, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_FULLSCREEN, false, 1600, 1000,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        true, false, false },
      { ATTR_WINDOWSIZE_320X480, true, 10, -2, 480, -1, 600, 400,
        TC_INITIAL_WINDOW_FULLSCREEN, false, 480, 500,
        TC_WINDOW_POSITION_EXPLICIT, TC_WINDOW_POSITION_CENTER, 10, -2,
        true, false, false },
      { ATTR_WINDOWSIZE_320X480, false, -1, -1, -1, -1, 600, 400,
        TC_INITIAL_WINDOW_FULLSCREEN, false, 600, 400,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        true, false, false },
      { ATTR_WINDOWSIZE_480X640, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_FULLSCREEN, false, 480, 640,
        TC_WINDOW_POSITION_CENTER, TC_WINDOW_POSITION_CENTER, 0, 0,
        true, false, false },
      { ATTR_RESIZABLE_WINDOW, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_NORMAL, false, 800, 500,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        false, false, true },
      { ATTR_RESIZABLE_WINDOW, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_FULLSCREEN, false, 1600, 1000,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        true, false, false },
      { ATTR_RESIZABLE_WINDOW, false, -1, -1, -1, -1, 600, -1,
        TC_INITIAL_WINDOW_NORMAL, true, 600, 500,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        true, false, false },
      { ATTR_RESIZABLE_WINDOW, false, -1, -1, -1, -1, -1, -1,
        TC_INITIAL_WINDOW_MAXIMIZED, false, 800, 500,
        TC_WINDOW_POSITION_DEFAULT, TC_WINDOW_POSITION_DEFAULT, 0, 0,
        false, true, true },
   };
   TCDisplayMetrics display = { 1600, 1000, 1600, 700 };
   TCWindowStartupOptions options;
   TCWindowStartupConfiguration configuration;
   int32 i;

   for (i = 0; i < (int32)(sizeof(cases) / sizeof(cases[0])); i++)
   {
      xmemzero(&options, sizeof(options));
      options.screenSpecified = cases[i].screenSpecified;
      options.x = cases[i].x;
      options.y = cases[i].y;
      options.width = cases[i].width;
      options.height = cases[i].height;
      options.environmentWidth = cases[i].environmentWidth;
      options.environmentHeight = cases[i].environmentHeight;
      options.initialState = cases[i].initialState;
      options.legacyFullscreen = cases[i].legacyFullscreen;
      options.appTczAttr = cases[i].appTczAttr;
      if (!windowResolveStartupConfiguration(&options, &display, &configuration)
         || configuration.width != cases[i].expectedWidth
         || configuration.height != cases[i].expectedHeight
         || configuration.xMode != cases[i].expectedXMode
         || configuration.yMode != cases[i].expectedYMode
         || configuration.x != cases[i].expectedX
         || configuration.y != cases[i].expectedY
         || configuration.fullscreen != cases[i].expectedFullscreen
         || configuration.maximized != cases[i].expectedMaximized
         || configuration.resizable != cases[i].expectedResizable)
      {
         TEST_FAIL(tc, "Startup window configuration policy case failed");
         goto finish;
      }
   }
#else
   TEST_SKIP;
#endif
   finish: ;
}

TESTCASE(tuW_pumpEvents) // totalcross/ui/Window native public static void pumpEvents();
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tuW_setSIP_icb) // totalcross/ui/Window native public void setSIP(int sipOption, totalcross.ui.Control control, boolean secret);
{
   TEST_SKIP;
   finish: ;
}
