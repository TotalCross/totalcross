// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "PalmFont.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_fontMetricsCreate(NMParams p) // totalcross/ui/font/FontMetrics native void fontMetricsCreate();
{
   TCObject fm = p->obj[0],font = FontMetrics_font(fm);
#if TC_RENDERER_SKIA
   double ascent, descent, leading;
   skia_fontMetrics(Font_skiaIndex(font), Font_size(font), &ascent, &descent, &leading);
   FontMetrics_ascentD(fm) = ascent;
   FontMetrics_descentD(fm) = descent;
   FontMetrics_leadingD(fm) = leading;
   FontMetrics_heightD(fm) = ascent + descent + leading;
   FontMetrics_ascent(fm) = (int32)ceil(ascent);
   FontMetrics_descent(fm) = (int32)ceil(descent + leading);
#else
   UserFont uf = loadUserFontFromFontObj(p->currentContext, font, ' ');
   if (uf != null)
   {
      FontMetrics_ascent(fm)  = uf->fontP.ascent;
      FontMetrics_descent(fm) = uf->fontP.descent;
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_charWidth_c(NMParams p) // totalcross/ui/font/FontMetrics native public int charWidth(char c);
{
#if TC_RENDERER_SKIA
   TCObject font = FontMetrics_font(p->obj[0]);
   JChar ch = (JChar)p->i32[0];
   p->retI = skia_stringWidth(&ch, sizeof(JChar), Font_skiaIndex(font), Font_size(font));
#else
   p->retI = getJCharWidth(p->currentContext, FontMetrics_font(p->obj[0]), (JChar)p->i32[0]);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_charWidthD_c(NMParams p) // totalcross/ui/font/FontMetrics native public double charWidthD(char c);
{
#if TC_RENDERER_SKIA
   JChar ch = (JChar)p->i32[0];
   p->retD = skia_stringWidthD(&ch, sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
   p->retD = getJCharWidth(p->currentContext, FontMetrics_font(p->obj[0]), (JChar)p->i32[0]);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_stringWidth_s(NMParams p) // totalcross/ui/font/FontMetrics native public int stringWidth(String s);
{
   TCObject s = p->obj[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
#if TC_RENDERER_SKIA
   else
      p->retI = skia_stringWidth(String_charsStart(s), String_charsLen(s) * sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
   else
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), String_charsStart(s), String_charsLen(s));
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_stringWidthD_s(NMParams p) // totalcross/ui/font/FontMetrics native public double stringWidthD(String s);
{
   TCObject s = p->obj[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
#if TC_RENDERER_SKIA
   else
      p->retD = skia_stringWidthD(String_charsStart(s), String_charsLen(s) * sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
   else
      p->retD = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), String_charsStart(s), String_charsLen(s));
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_stringWidthAtSizeD_sd(NMParams p) // totalcross/ui/font/FontMetrics native public double stringWidthAtSizeD(String s, double fontSize);
{
   TCObject s = p->obj[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
#if TC_RENDERER_SKIA
   else
      p->retD = skia_stringWidthD(String_charsStart(s), String_charsLen(s) * sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), p->dbl[0]);
#else
   else
      p->retD = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), String_charsStart(s), String_charsLen(s)) * p->dbl[0] / Font_size(FontMetrics_font(p->obj[0]));
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_lineHeightAtSizeD_d(NMParams p) // totalcross/ui/font/FontMetrics native public double lineHeightAtSizeD(double fontSize);
{
#if TC_RENDERER_SKIA
   double ascent, descent, leading;
   skia_fontMetrics(Font_skiaIndex(FontMetrics_font(p->obj[0])), p->dbl[0], &ascent, &descent, &leading);
   p->retD = ascent + descent + leading;
#else
   p->retD = (FontMetrics_heightD(p->obj[0]) ? FontMetrics_heightD(p->obj[0]) : FontMetrics_ascent(p->obj[0]) + FontMetrics_descent(p->obj[0])) * p->dbl[0] / Font_size(FontMetrics_font(p->obj[0]));
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_descentAtSizeD_d(NMParams p) // totalcross/ui/font/FontMetrics native public double descentAtSizeD(double fontSize);
{
#if TC_RENDERER_SKIA
   double ascent, descent, leading;
   skia_fontMetrics(Font_skiaIndex(FontMetrics_font(p->obj[0])), p->dbl[0], &ascent, &descent, &leading);
   p->retD = descent;
#else
   p->retD = FontMetrics_descentD(p->obj[0]) * p->dbl[0] / Font_size(FontMetrics_font(p->obj[0]));
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_ascentAtSizeD_d(NMParams p) // totalcross/ui/font/FontMetrics native public double ascentAtSizeD(double fontSize);
{
#if TC_RENDERER_SKIA
   double ascent, descent, leading;
   skia_fontMetrics(Font_skiaIndex(FontMetrics_font(p->obj[0])), p->dbl[0], &ascent, &descent, &leading);
   p->retD = ascent;
#else
   p->retD = FontMetrics_ascentD(p->obj[0]) * p->dbl[0] / Font_size(FontMetrics_font(p->obj[0]));
#endif
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
#if TC_RENDERER_SKIA
      p->retI = skia_stringWidth(((JCharP)ARRAYOBJ_START(charArray))+start, count * sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), ((JCharP)ARRAYOBJ_START(charArray))+start, count);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tufFM_sbWidth_s(NMParams p) // totalcross/ui/font/FontMetrics native public int sbWidth(StringBuffer s);
{
   TCObject s = p->obj[1];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s"); // throw NPE
#if TC_RENDERER_SKIA
   else
      p->retI = skia_stringWidth(StringBuffer_charsStart(s), StringBuffer_count(s) * sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
   else
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), StringBuffer_charsStart(s), StringBuffer_count(s));
#endif
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
#if TC_RENDERER_SKIA
      p->retI = skia_stringWidth(StringBuffer_charsStart(s)+start, count * sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
      p->retI = getJCharPWidth(p->currentContext, FontMetrics_font(p->obj[0]), StringBuffer_charsStart(s)+start, count);
#endif
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
#if TC_RENDERER_SKIA
      p->retI = skia_stringWidth(StringBuffer_charsStart(s)+i, sizeof(JChar), Font_skiaIndex(FontMetrics_font(p->obj[0])), Font_size(FontMetrics_font(p->obj[0])));
#else
      p->retI = getJCharWidth(p->currentContext, FontMetrics_font(p->obj[0]), StringBuffer_charsStart(s)[i]);
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "font_FontMetrics_test.h"
#endif
