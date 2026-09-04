// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

TCObject testfont;
TESTCASE(tufF_fontCreate_f) // totalcross/ui/font/Font native static void fontCreate(Font obj);
{
   TNMParams p;
   int32 *style, *size, *skiaIndex, *maxfs, *minfs, *normal, *tabSize;
   TCObject *name, *hvUserFont;
   TCClass c;
   TCObject font;
#if TC_RENDERER_SKIA
   CharP resourceName;
   CharP missingName;
   TCObject customFont;
   TCObject missingFont;
   char longName[160];
   int32 i;
#endif
   font = createObject(currentContext, "totalcross.ui.font.Font");
   setObjectLock(font, UNLOCKED);
   ASSERT1_EQUALS(NotNull, font);
   c = OBJ_CLASS(font);
   // check if the fields are in the right order

   // style
   style = getInstanceFieldInt(font, "style", "totalcross.ui.font.Font");
   ASSERT1_EQUALS(NotNull, style);
   ASSERT2_EQUALS(Ptr, style, &Font_style(font));

   size = getInstanceFieldInt(font, "size", "totalcross.ui.font.Font");
   ASSERT1_EQUALS(NotNull, size);
   ASSERT2_EQUALS(Ptr, size, &Font_size(font));

   skiaIndex = getInstanceFieldInt(font, "skiaIndex", "totalcross.ui.font.Font");
   ASSERT1_EQUALS(NotNull, skiaIndex);
   ASSERT2_EQUALS(Ptr, skiaIndex, &Font_skiaIndex(font));

   name = getInstanceFieldObject(font, "name", "totalcross.ui.font.Font");
   ASSERT1_EQUALS(NotNull, name);
   ASSERT2_EQUALS(Ptr, name, &Font_name(font));

   hvUserFont = getInstanceFieldObject(font, "hv_UserFont", "totalcross.ui.font.Font");
   ASSERT1_EQUALS(NotNull, hvUserFont);

   maxfs   = getStaticFieldInt(c, "MAX_FONT_SIZE");
   minfs   = getStaticFieldInt(c, "MIN_FONT_SIZE");
   normal  = getStaticFieldInt(c, "NORMAL_SIZE");
   tabSize = getStaticFieldInt(c, "TAB_SIZE");
   ASSERT1_EQUALS(NotNull, maxfs);
   ASSERT1_EQUALS(NotNull, minfs);
   ASSERT1_EQUALS(NotNull, normal);
   ASSERT1_EQUALS(NotNull, tabSize);
   ASSERT1_EQUALS(True, *maxfs > 0);
   ASSERT1_EQUALS(True, *minfs > 0);
   ASSERT1_EQUALS(True, *normal > 0);
   ASSERT1_EQUALS(True, *tabSize > 0);

#if TC_RENDERER_SKIA
   resourceName = createSkiaResourceName("a");
   ASSERT2_EQUALS(Sz, resourceName, "a.ttf");
   xfree(resourceName);
   resourceName = createSkiaResourceName("a.ttf");
   ASSERT2_EQUALS(Sz, resourceName, "a.ttf");
   xfree(resourceName);
   resourceName = createSkiaResourceName("a.TTF");
   ASSERT2_EQUALS(Sz, resourceName, "a.TTF");
   xfree(resourceName);
   resourceName = createSkiaResourceName("a.TtF");
   ASSERT2_EQUALS(Sz, resourceName, "a.TtF");
   xfree(resourceName);
   resourceName = createSkiaResourceName("TCFont");
   ASSERT2_EQUALS(Sz, resourceName, "Roboto Regular.ttf");
   xfree(resourceName);
   for (i = 0; i < 159; i++)
      longName[i] = 'x';
   longName[159] = 0;
   resourceName = createSkiaResourceName(longName);
   ASSERT2_EQUALS(I32, xstrlen(resourceName), 163);
   xfree(resourceName);
#endif

#if !TC_RENDERER_SKIA
   ASSERT1_EQUALS(NotNull, defaultFont);
#endif

   // fill in a font and test if it loads
   Font_name(font) = createStringObjectFromCharP(currentContext, "TCFont",-1);
   setObjectLock(Font_name(font), UNLOCKED);
   Font_size(font) = 9;  // do NOT change this value, or the fontmetrics tests will fail
   Font_style(font) = 1; // BOLD
   p.currentContext = currentContext;
   p.obj = &font;
   tufF_fontCreate(&p);
#if TC_RENDERER_SKIA
   ASSERT1_EQUALS(Null, *hvUserFont);

   if (skia_getTypefaceIndex("Roboto Regular.ttf") >= 0)
   {
      customFont = createObject(currentContext, "totalcross.ui.font.Font");
      ASSERT1_EQUALS(NotNull, customFont);
      setObjectLock(customFont, UNLOCKED);
      Font_name(customFont) = createStringObjectFromCharP(currentContext, "Roboto Regular", -1);
      setObjectLock(Font_name(customFont), UNLOCKED);
      Font_size(customFont) = 9;
      p.obj = &customFont;
      tufF_fontCreate(&p);
      ASSERT1_EQUALS(True, Font_skiaIndex(customFont) >= 0);
      missingName = String2CharP(Font_name(customFont));
      ASSERT2_EQUALS(Sz, missingName, "Roboto Regular");
      xfree(missingName);
   }

   missingFont = createObject(currentContext, "totalcross.ui.font.Font");
   ASSERT1_EQUALS(NotNull, missingFont);
   setObjectLock(missingFont, UNLOCKED);
   Font_name(missingFont) = createStringObjectFromCharP(currentContext, "Missing", -1);
   setObjectLock(Font_name(missingFont), UNLOCKED);
   Font_size(missingFont) = 9;
   p.obj = &missingFont;
   tufF_fontCreate(&p);
   ASSERT1_EQUALS(Null, Font_hvUserFont(missingFont));
   missingName = String2CharP(Font_name(missingFont));
   ASSERT2_EQUALS(Sz, missingName, "TCFont");
   xfree(missingName);
#else
   ASSERT1_EQUALS(NotNull, *hvUserFont);
#endif

   testfont = font; // will be used in the fontmetrics tests
   finish: ;
}

TESTCASE(tufF_FontTestCleanup_f) // just do cleanups on the font and fontmetrics tests  #DEPENDS(Graphics)
{
   UNUSED(tc);
   fontDestroy();
}
