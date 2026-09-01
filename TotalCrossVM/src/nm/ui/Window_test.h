// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(windowResolveStartupSize)
{
#if TC_OS_DESKTOP
   struct StartupSizeCase
   {
      int16 appTczAttr;
      bool commandLineSizeProvided;
      bool fullscreenDefault;
      int32 commandLineWidth;
      int32 commandLineHeight;
      int32 environmentWidth;
      int32 environmentHeight;
      int32 expectedWidth;
      int32 expectedHeight;
      bool expectedTczSizeApplied;
   } cases[] = {
      { ATTR_WINDOWSIZE_320X480, true, false, 700, 500, 600, 400,
        700, 500, false },
      { ATTR_WINDOWSIZE_320X480, false, false, -1, -1, 600, 400,
        600, 400, false },
      { ATTR_WINDOWSIZE_320X480, false, false, -1, -1, -1, -1,
        320, 480, true },
      { ATTR_WINDOWSIZE_480X640, false, false, -1, -1, -1, -1,
        480, 640, true },
      { ATTR_WINDOWSIZE_600X800, false, false, -1, -1, -1, -1,
        600, 700, true },
      { 0, false, false, -1, -1, -1, -1, 800, 500, false },
      { ATTR_WINDOWSIZE_480X640, true, false, -1, -1, 600, 400,
        800, 500, false },
      { 0, true, true, -1, -1, -1, -1, 800, 500, false },
      { 0, false, true, -1, -1, -1, -1, 1600, 1000, false },
   };
   int32 width;
   int32 height;
   bool tczSizeApplied;
   int32 i;

   for (i = 0; i < (int32)(sizeof(cases) / sizeof(cases[0])); i++)
   {
      if (!windowResolveStartupSize(
         cases[i].appTczAttr,
         cases[i].commandLineSizeProvided,
         cases[i].fullscreenDefault,
         1600,
         1000,
         700,
         cases[i].commandLineWidth,
         cases[i].commandLineHeight,
         cases[i].environmentWidth,
         cases[i].environmentHeight,
         &width,
         &height,
         &tczSizeApplied)
         || width != cases[i].expectedWidth
         || height != cases[i].expectedHeight
         || tczSizeApplied != cases[i].expectedTczSizeApplied)
      {
         TEST_FAIL(tc, "Startup window size policy case failed");
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
