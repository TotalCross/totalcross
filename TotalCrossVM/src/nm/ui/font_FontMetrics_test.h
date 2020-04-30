// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



extern TCObject testfont;
TCObject testfm;

TESTCASE(tufFM_fontMetricsCreate) // totalcross/ui/font/FontMetrics native void fontMetricsCreate(); #DEPENDS(tufF_fontCreate_f)
{
   TNMParams p;
   int32 *ascent,*descent;
   TCObject fm = createObject(currentContext, "totalcross.ui.font.FontMetrics");
   setObjectLock(fm, UNLOCKED);
   tzero(p);
   p.currentContext = currentContext;
   ASSERT1_EQUALS(NotNull, fm);
   ASSERT1_EQUALS(NotNull, testfont);
   // set the font field of the object
   FontMetrics_font(fm) = testfont;
   // check that fields exist
   ascent = getInstanceFieldInt(fm, "ascent", "totalcross.ui.font.FontMetrics");
   descent= getInstanceFieldInt(fm, "descent", "totalcross.ui.font.FontMetrics");
   ASSERT1_EQUALS(NotNull, ascent);
   ASSERT1_EQUALS(NotNull, descent);
   // call the create function
   p.obj = &fm; // this
   tufFM_fontMetricsCreate(&p);
   ASSERT2_EQUALS(I32, *ascent, 7);
   ASSERT2_EQUALS(I32, *descent, 2);

   testfm = fm;
   finish: ;
}
TESTCASE(tufFM_charWidth_c) // totalcross/ui/font/FontMetrics native public int charWidth(char c); #DEPENDS(tufFM_fontMetricsCreate)
{
   TNMParams p;
   int32 c;
   tzero(p);
   p.currentContext = currentContext;
   p.obj = &testfm; // this
   p.i32 = &c;
   ASSERT1_EQUALS(NotNull, testfm); // make sure fontMetricsCreate didn't failed

   c = ' ';
   tufFM_charWidth_c(&p);
   ASSERT2_EQUALS(I32, p.retI, 2);

   c = 'b';
   tufFM_charWidth_c(&p);
   ASSERT2_EQUALS(I32, p.retI, 5);

   c = 0x1234; // invalid char
   tufFM_charWidth_c(&p);
   ASSERT2_EQUALS(I32, p.retI, 2); // same of space

   finish: ;
}TESTCASE(tufFM_stringWidth_Cii) // totalcross/ui/font/FontMetrics native public int stringWidth(char []chars, int start, int count);
{
   TNMParams p;
   int32 sc[2];
   TCObject obj[2];
   TCObject s;
   ASSERT1_EQUALS(NotNull, testfm); // make sure fontMetricsCreate didn't failed
   s = createStringObjectFromCharP(currentContext, "123Barbara",-1);
   setObjectLock(s, UNLOCKED);
   ASSERT1_EQUALS(NotNull, s);
   tzero(p);
   p.currentContext = currentContext;
   p.obj = obj;
   p.i32 = sc;
   obj[0] = testfm;
   obj[1] = String_chars(s); // get the char array created for the string object
   sc[0] = 3; // start
   sc[1] = 7; // count
   tufFM_stringWidth_Cii(&p);
   ASSERT2_EQUALS(I32, p.retI, 34);

   finish: ;
}
