// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "PalmFont.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_fontMetricsCreate(NMParams p) // totalcross/ui/font/FontMetrics native void fontMetricsCreate();
{
   TCObject fm = p->obj[0],font = FontMetrics_font(fm);
   UserFont uf = loadUserFontFromFontObj(p->currentContext, font, ' ');
   if (uf != null)
   {
      FontMetrics_ascent(fm)  = uf->fontP.ascent;
      FontMetrics_descent(fm) = uf->fontP.descent;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_charWidth_c(NMParams p) // totalcross/ui/font/FontMetrics native public int charWidth(char c);
{
   p->retI = getJCharWidth(p->currentContext, FontMetrics_font(p->obj[0]), (JChar)p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_stringWidth_s(NMParams p) // totalcross/ui/font/FontMetrics native public int stringWidth(String s);
{
   TCObject s = p->obj[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
   else
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), String_charsStart(s), String_charsLen(s));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_stringWidth_Cii(NMParams p) // totalcross/ui/font/FontMetrics native public int stringWidth(char []chars, int start, int count);
{
   TCObject charArray = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   if (charArray == null)
      throwNullArgumentException(p->currentContext, "chars");
   else
   if (checkArrayRange(p->currentContext, charArray, start, count))
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), ((JCharP)ARRAYOBJ_START(charArray))+start, count);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_sbWidth_s(NMParams p) // totalcross/ui/font/FontMetrics native public int sbWidth(StringBuffer s);
{
   TCObject s = p->obj[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s"); // throw NPE
   else
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), StringBuffer_charsStart(s), StringBuffer_count(s));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_sbWidth_sii(NMParams p) // totalcross/ui/font/FontMetrics native public int sbWidth(StringBuffer s, int start, int count);
{
   TCObject s = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s"); // throw NPE
   else
   if (checkArrayRange(p->currentContext, StringBuffer_chars(s), start, count))
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), StringBuffer_charsStart(s)+start, count);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_charWidth_si(NMParams p) // totalcross/ui/font/FontMetrics native public int charWidth(StringBuffer s, int i);
{
   TCObject s = p->obj[1];
   int32 i = p->i32[0];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s"); // throw NPE
   else
   if (checkArrayRange(p->currentContext, StringBuffer_chars(s), i, 1)) // check only index "i"
      p->retI = getJCharWidth(p->currentContext, FontMetrics_font(p->obj[0]), StringBuffer_charsStart(s)[i]);
}

#ifdef ENABLE_TEST_SUITE
#include "font_FontMetrics_test.h"
#endif
