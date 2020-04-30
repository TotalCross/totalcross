// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TCObject testfont;
TESTCASE(tufF_fontCreate_f) // totalcross/ui/font/Font native static void fontCreate(Font obj);
{
   TNMParams p;
   int32 *style, *size, *maxfs, *minfs, *normal, *tabSize;
   TCObject *name, *hvUserFont;
   TCClass c;
   TCObject font;
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

   ASSERT1_EQUALS(NotNull, defaultFont);

   // fill in a font and test if it loads
   Font_name(font) = createStringObjectFromCharP(currentContext, "TCFont",-1);
   setObjectLock(Font_name(font), UNLOCKED);
   Font_size(font) = 9;  // do NOT change this value, or the fontmetrics tests will fail
   Font_style(font) = 1; // BOLD
   p.currentContext = currentContext;
   p.obj = &font;
   tufF_fontCreate(&p);
   ASSERT1_EQUALS(NotNull, *hvUserFont);

   testfont = font; // will be used in the fontmetrics tests
   finish: ;
}

TESTCASE(tufF_FontTestCleanup_f) // just do cleanups on the font and fontmetrics tests  #DEPENDS(Graphics)
{
   UNUSED(tc);
   fontDestroy();
}
