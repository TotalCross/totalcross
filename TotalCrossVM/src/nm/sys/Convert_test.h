// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#define CDILU 199 // �
#define CDILL 231 // �

extern TCObject testfont;

TESTCASE(tsC_toInt_s) // totalcross/sys/Convert native public static int toInt(String s);
{
   TNMParams p;
   TCObject params[1];

   p.currentContext = currentContext;
   p.obj = params;

   p.obj[0] = createStringObjectFromCharP(currentContext, "12345678", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 12345678L, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "-12345678", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, -12345678L, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "0x12345678", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "abcd", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "-abcd", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "+999", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "-999", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, -999, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "-999.023", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "12345678901234567890", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   p.obj[0] = createStringObjectFromCharP(currentContext, "", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toInt_s(&p);
   ASSERT2_EQUALS(I32, 0, p.retI);

   finish: ;
}
TESTCASE(tsC_toString_c) // totalcross/sys/Convert native public static String toString(char c);
{
   TNMParams p;
   char buffer[20];
   char param[2];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.i32 = i32buf;

   xstrcpy(param, "A");
   CharP2JCharPBuf(param, -1, (JCharP) i32buf, true);
   tsC_toString_c(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, param, buffer);

   param[0] = (char)CDILU;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, (JCharP) i32buf, true);
   tsC_toString_c(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, param, buffer);

   finish: ;
}
TESTCASE(tsC_doubleToIntBits_d) // totalcross/sys/Convert native public static int doubleToIntBits(double d);
{
   TNMParams p;
   double doubleBuf[1];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.dbl = doubleBuf;
   p.i32 = i32buf;

   p.dbl[0] = 123.123;
   tsC_doubleToIntBits_d(&p);

   p.i32[0] = p.retI;
   tsC_intBitsToDouble_i(&p);
   ASSERT_BETWEEN(Dbl, 123.122, (float) p.retD, 123.124);

   p.dbl[0] = -123.123;
   tsC_doubleToIntBits_d(&p);

   p.i32[0] = p.retI;
   tsC_intBitsToDouble_i(&p);
   ASSERT_BETWEEN(Dbl, -123.124, (float) p.retD, 123.122);

   p.dbl[0] = 123.1238;
   tsC_doubleToIntBits_d(&p);

   p.i32[0] = p.retI;
   tsC_intBitsToDouble_i(&p);
   ASSERT_BETWEEN(Dbl, 123.123, (float) p.retD, 123.125);

   finish: ;
}
TESTCASE(tsC_intBitsToDouble_i) // totalcross/sys/Convert native public static double intBitsToDouble(int i);
{
   TNMParams p;
   double doubleBuf[1];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.dbl = doubleBuf;
   p.i32 = i32buf;

   p.dbl[0] = 123.123;
   tsC_doubleToIntBits_d(&p);

   p.i32[0] = p.retI;
   tsC_intBitsToDouble_i(&p);
   ASSERT_BETWEEN(Dbl, 123.122, (float) p.retD, 123.124);

   p.dbl[0] = -123.123;
   tsC_doubleToIntBits_d(&p);

   p.i32[0] = p.retI;
   tsC_intBitsToDouble_i(&p);
   ASSERT_BETWEEN(Dbl, -123.124, (float) p.retD, 123.122);

   p.dbl[0] = 123.1238;
   tsC_doubleToIntBits_d(&p);

   p.i32[0] = p.retI;
   tsC_intBitsToDouble_i(&p);
   ASSERT_BETWEEN(Dbl, 123.123, (float) p.retD, 123.125);

   finish: ;
}

TESTCASE(tsC_toString_i) // totalcross/sys/Convert native public static String toString(int i);
{
   TNMParams p;
   char buffer[20];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.i32 = i32buf;

   p.i32[0] = 0;
   tsC_toString_i(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "0", buffer);

   p.i32[0] = 1234567890;
   tsC_toString_i(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "1234567890", buffer);

   p.i32[0] = -1234567890;
   tsC_toString_i(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "-1234567890", buffer);

   finish: ;
}
TESTCASE(tsC_toString_di) // totalcross/sys/Convert native public static String toString(double d, int precision);
{
   TNMParams p;
   char buffer[20];
   double doubleBuf[1];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.dbl = doubleBuf;
   p.i32 = i32buf;

   p.dbl[0] = 1234.12345;
   p.i32[0] = 2;
   tsC_toString_di(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "1234.12", buffer);

   p.dbl[0] = -1234.45670;
   p.i32[0] = 2;
   tsC_toString_di(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "-1234.46", buffer);

   p.dbl[0] = 1234.890007;
   p.i32[0] = 3;
   tsC_toString_di(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "1234.890", buffer);

   finish: ;
}
TESTCASE(tsC_toDouble_s) // totalcross/sys/Convert native public static double toDouble(String s);
{
   TNMParams p;
   TCObject params[5];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.i32 = i32buf;

   p.obj = params;

   p.i32[0] = 2;
   p.obj[0] = createStringObjectFromCharP(currentContext, "1234.1234", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toDouble_s(&p);
   ASSERT2_EQUALS(Dbl, 1234.1234, p.retD);

   p.i32[0] = 4;
   p.obj[0] = createStringObjectFromCharP(currentContext, "1234.12", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toDouble_s(&p);
   ASSERT2_EQUALS(Dbl, 1234.1200, p.retD);

   p.i32[0] = 2;
   p.obj[0] = createStringObjectFromCharP(currentContext, "1234.129", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toDouble_s(&p);
   ASSERT_BETWEEN(Dbl, 1234.12, p.retD, 1234.14);

   finish: ;
}
TESTCASE(tsC_toString_si) // totalcross/sys/Convert native public static String toString(String doubleValue, int n);
{
   TNMParams p;
   TCObject params[1];
   char buffer[20];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.i32 = i32buf;
   p.obj = params;

   p.i32[0] = 2;
   p.obj[0] = createStringObjectFromCharP(currentContext, "1234.1234", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toString_si(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "1234.12", buffer);

   p.i32[0] = 4;
   p.obj[0] = createStringObjectFromCharP(currentContext, "-1234.87658", -1);
   setObjectLock(p.obj[0], UNLOCKED);
   tsC_toString_si(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "-1234.8766", buffer);

   finish: ;
}
TESTCASE(tsC_doubleToLongBits_d) // totalcross/sys/Convert native public static long doubleToLongBits(double value);
{
   TNMParams p;
   double doubleBuf[1];
   int64 longBuf[1];

   p.currentContext = currentContext;
   p.dbl = doubleBuf;
   p.dbl[0] = 1234567.1234567;
   tsC_doubleToLongBits_d(&p);
   longBuf[0] = p.retL;

   p.i64 = longBuf;
   tsC_longBitsToDouble_l(&p);
   ASSERT_BETWEEN(Dbl, 1234567.1234566, p.retD, 1234567.1234568);

   p.dbl = doubleBuf;
   p.dbl[0] = -1234567.1234567;
   tsC_doubleToLongBits_d(&p);
   longBuf[0] = p.retL;

   p.i64 = longBuf;
   tsC_longBitsToDouble_l(&p);
   ASSERT_BETWEEN(Dbl, -1234567.1234568, p.retD, -1234567.1234566);

   p.dbl = doubleBuf;
   p.dbl[0] = 1234567.123456789;
   tsC_doubleToLongBits_d(&p);
   longBuf[0] = p.retL;

   p.i64 = longBuf;
   tsC_longBitsToDouble_l(&p);
   ASSERT_BETWEEN(Dbl, 1234567.1234567, p.retD, 1234567.1234569);

   finish: ;
}
TESTCASE(tsC_longBitsToDouble_l) // totalcross/sys/Convert native public static double longBitsToDouble(long bits);
{
   TNMParams p;
   double doubleBuf[1];
   int64 longBuf[1];

   p.currentContext = currentContext;
   p.dbl = doubleBuf;
   p.dbl[0] = 1234567.1234567;
   tsC_doubleToLongBits_d(&p);
   longBuf[0] = p.retL;

   p.i64 = longBuf;
   tsC_longBitsToDouble_l(&p);
   ASSERT_BETWEEN(Dbl, 1234567.1234566, p.retD, 1234567.1234568);

   p.dbl = doubleBuf;
   p.dbl[0] = -1234567.1234567;
   tsC_doubleToLongBits_d(&p);
   longBuf[0] = p.retL;

   p.i64 = longBuf;
   tsC_longBitsToDouble_l(&p);
   ASSERT_BETWEEN(Dbl, -1234567.1234568, p.retD, -1234567.1234566);

   p.dbl = doubleBuf;
   p.dbl[0] = 1234567.123456789;
   tsC_doubleToLongBits_d(&p);
   longBuf[0] = p.retL;

   p.i64 = longBuf;
   tsC_longBitsToDouble_l(&p);
   ASSERT_BETWEEN(Dbl, 1234567.1234567, p.retD, 1234567.1234569);

   finish: ;
}
TESTCASE(tsC_toLowerCase_c) // totalcross/sys/Convert native public static char toLowerCase(char c);
{
   TNMParams p;
   char param[2];
   JChar buffer[20];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.i32 = i32buf;

   xstrcpy(param, "A");
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   ASSERT2_EQUALS(I16, 'a', (int16) p.retI);

   xstrcpy(param, "a");
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   ASSERT2_EQUALS(I16, 'a', (int16) p.retI);

   xstrcpy(param, "Z");
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   ASSERT2_EQUALS(I16, 'z', (int16) p.retI);

   param[0] = (char)CDILL;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   param[0] = (char)CDILL;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   ASSERT2_EQUALS(I16, buffer[0], (int16)p.retI);

   param[0] = (char)CDILU;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   param[0] = (char)CDILL;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   ASSERT2_EQUALS(I16, buffer[0], (int16)p.retI);

   param[0] = 0;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   param[0] = 0;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   ASSERT2_EQUALS(I16, buffer[0], (int16)p.retI);

   *buffer = (int16)1000000;
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   *buffer = (int16)1000000;
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   *buffer = 0x1E00;
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   *buffer = 0x1E01;
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   *buffer = 0x1E01;
   p.i32[0] = buffer[0];
   tsC_toLowerCase_c(&p);
   *buffer = 0x1E01;
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   finish: ;
}
TESTCASE(tsC_toUpperCase_c) // totalcross/sys/Convert native public static char toUpperCase(char c);
{
   TNMParams p;
   char param[2];
   JChar buffer[20];
   int32 i32buf[1];

   p.currentContext = currentContext;
   p.i32 = i32buf;

   xstrcpy(param, "a");
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   ASSERT2_EQUALS(I16, 'A', (int16) p.retI);

   xstrcpy(param, "A");
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   ASSERT2_EQUALS(I16, 'A', (int16) p.retI);

   xstrcpy(param, "z");
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   ASSERT2_EQUALS(I16, 'Z', (int16) p.retI);

   param[0] = (char)CDILU;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   param[0] = (char)CDILU;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   param[0] = (char)CDILL;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   param[0] = (char)CDILU;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   param[0] = 0;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   param[0] = 0;
   param[1] = 0;
   CharP2JCharPBuf(param, -1, buffer, true);
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   *buffer = (int16)1000000;
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   *buffer = (int16)1000000;
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   *buffer = 0x1E01;
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   *buffer = 0x1E00;
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   *buffer = 0x1E00;
   p.i32[0] = buffer[0];
   tsC_toUpperCase_c(&p);
   *buffer = 0x1E00;
   ASSERT2_EQUALS(I16, buffer[0], (int16) p.retI);

   finish: ;
}
TESTCASE(tsC_unsigned2hex_ii) // totalcross/sys/Convert native public static String unsigned2hex(int b, int places);
{
   TNMParams p;
   char buffer[20];
   int32 i32buf[2];

   p.currentContext = currentContext;
   p.i32 = i32buf;

   p.i32[0] = 0xFFEEBBAA; // signed value
   p.i32[1] = 4;
   tsC_unsigned2hex_ii(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "BBAA", buffer);

   p.i32[0] = 0xFFEEBBAA; // signed value
   p.i32[1] = 8;
   tsC_unsigned2hex_ii(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "FFEEBBAA", buffer); // the number was signed, so this result is correct

   p.i32[0] = 0xFEEBBAA; // unsigned value
   p.i32[1] = 8;
   tsC_unsigned2hex_ii(&p);
   String2CharPBuf(p.retO, buffer);
   ASSERT2_EQUALS(Sz, "0FEEBBAA", buffer);

   finish: ;
}
TESTCASE(tsC_hashCode_s) // totalcross/sys/Convert native public static int hashCode(StringBuffer sb);
{
   TNMParams p;
   TCObject params[1];
   TCObject sb;
   TCObject charArray;
   char buf[10] = "HASHCODE";
   JChar jbuf[10];

   CharP2JCharPBuf(buf, xstrlen(buf), jbuf, true);
   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);

   p.currentContext = currentContext;
   p.obj = params;
   p.obj[0] = sb;

   charArray = createCharArray(currentContext, 10);
   setObjectLock(charArray, UNLOCKED);
   StringBuffer_chars(sb) = charArray;
   StringBuffer_charsLen(sb) = 10;
   StringBuffer_count(sb) = xstrlen(buf);
   xmemmove(ARRAYOBJ_START(charArray), jbuf, xstrlen(buf)*sizeof(JChar));

   tsC_hashCode_s(&p);
   ASSERT2_EQUALS(I32, p.retI, JCharPHashCode(jbuf, xstrlen(buf)));

   finish: ;
}

extern TCObject testfm;

TESTCASE(tsC_getBreakPos_fsiib) // totalcross/sys/Convert native public static int getBreakPos(totalcross.ui.font.FontMetrics fm, StringBuffer sb, int start, int width, boolean doWordWrap); #DEPENDS(tufFM_fontMetricsCreate)
{
   TNMParams p;
   TCObject params[2];
   int32 i32buf[3];
   TCObject sb;
   TCObject charArray;
   CharP buf = "A SuperWaba � uma plataforma para desenvolvimento de aplica��es para PDA(Personal Digital Assistants) e Smartphones.";
   JCharP jbuf;
   int32* start;
   int32* width;
   int32* doWordWrap;

   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);
   ASSERT1_EQUALS(NotNull, testfm);

   jbuf = CharP2JCharP(buf, xstrlen(buf));

   p.currentContext = currentContext;
   p.i32 = i32buf;
   start = &p.i32[0];
   width = &p.i32[1];
   doWordWrap = &p.i32[2];
   p.obj = params;
   p.obj[0] = testfm;
   p.obj[1] = sb;

   charArray = createCharArray(currentContext, xstrlen(buf) + 15);
   setObjectLock(charArray, UNLOCKED);
   StringBuffer_chars(sb) = charArray;
   StringBuffer_charsLen(sb) = xstrlen(buf) + 15;
   StringBuffer_count(sb) = xstrlen(buf);
   xmemmove(ARRAYOBJ_START(charArray), jbuf, xstrlen(buf)*sizeof(JChar));

   *start = 0;
   *width = 160;
   *doWordWrap = (bool)true;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 18);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 34);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 64);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 90);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 116);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 116);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 116);

   *start = 0;
   *width = 160;
   *doWordWrap = (bool)false;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 27);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 57);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 86);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 116);

   *start = p.retI;
   tsC_getBreakPos_fsiib(&p);
   ASSERT2_EQUALS(I32, p.retI, 116);

   xfree(jbuf);

   finish: ;
}
TESTCASE(tsC_insertAt_sic) // totalcross/sys/Convert native public static void insertAt(StringBuffer sb, int pos, char c);
{
   // note that "insert" is not the same of "replace"
   TNMParams p;
   TCObject params[1];
   int32 i32buf[2];
   TCObject sb;
   TCObject charArray;
   char buf[10] = "CARRO";
   JChar jbuf[10];
   JCharP jresP;
   CharP resP=null;
   int32 resSize;

   CharP2JCharPBuf(buf, xstrlen(buf), jbuf, true);
   sb = createObject(currentContext, "java.lang.StringBuffer");
   ASSERT1_EQUALS(NotNull, sb);

   p.currentContext = currentContext;
   p.i32 = i32buf;
   p.obj = params;
   p.obj[0] = sb;

   p.i32[0] = 1;
   p.i32[1] = (int32)'O';
   charArray = createCharArray(currentContext, 10);
   setObjectLock(charArray, UNLOCKED);
   StringBuffer_chars(sb) = charArray;
   StringBuffer_charsLen(sb) = 10;
   StringBuffer_count(sb) = xstrlen(buf);
   xmemmove(ARRAYOBJ_START(charArray), jbuf, xstrlen(buf)*sizeof(JChar));

   tsC_insertAt_sic(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   jresP = StringBuffer_charsStart(p.obj[0]);
   resSize = StringBuffer_count(p.obj[0]);
   resP = JCharP2CharP(jresP, resSize);
   ASSERT2_EQUALS(Sz, resP, "COARRO");
   xfree(resP);

   p.i32[0] = 4;
   p.i32[1] = (int32)'A';

   tsC_insertAt_sic(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   jresP = StringBuffer_charsStart(p.obj[0]);
   resSize = StringBuffer_count(p.obj[0]);
   resP = JCharP2CharP(jresP, resSize);
   ASSERT2_EQUALS(Sz, resP, "COARARO");
   xfree(resP);

   p.i32[0] = 9;
   p.i32[1] = (int32)'X';

   tsC_insertAt_sic(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   jresP = StringBuffer_charsStart(p.obj[0]);
   resSize = StringBuffer_count(p.obj[0]);
   resP = JCharP2CharP(jresP, resSize);
   ASSERT2_EQUALS(Sz, resP, "COARARO");
   xfree(resP);

   p.i32[0] = 12;
   p.i32[1] = (int32)'X';

   tsC_insertAt_sic(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   jresP = StringBuffer_charsStart(p.obj[0]);
   resSize = StringBuffer_count(p.obj[0]);
   resP = JCharP2CharP(jresP, resSize);
   ASSERT2_EQUALS(Sz, resP, "COARARO");
   xfree(resP);

   p.i32[0] = -5;
   p.i32[1] = (int32)'X';

   tsC_insertAt_sic(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   jresP = StringBuffer_charsStart(p.obj[0]);
   resSize = StringBuffer_count(p.obj[0]);
   resP = JCharP2CharP(jresP, resSize);
   ASSERT2_EQUALS(Sz, resP, "COARARO");
   xfree(resP);

   p.i32[0] = 7;
   p.i32[1] = (int32)'T';

   tsC_insertAt_sic(&p);

   jresP = StringBuffer_charsStart(p.obj[0]);
   resSize = StringBuffer_count(p.obj[0]);
   resP = JCharP2CharP(jresP, resSize);
   ASSERT2_EQUALS(Sz, resP, "COARAROT");
   xfree(resP);

finish:
   if (resP)
      xfree(resP);
   currentContext->thrownException = null;
}

static CharP toStringLong(Context currentContext, CharP buf, int64 i)
{
   TNMParams p;

   *buf = 0;
   tzero(p);
   p.currentContext = currentContext;
   p.i64 = &i;
   tsC_toString_l(&p);
   if (p.retO == null)
      return null;
   String2CharPBuf(p.retO, buf);
   return buf;
}

TESTCASE(tsC_toString_l) // totalcross/sys/Convert native public static String toString(long l);
{
   char buf[30];
   ASSERT2_EQUALS(Sz, "-1",toStringLong(currentContext, buf,I64_CONST(-1)));
   ASSERT2_EQUALS(Sz, "0",toStringLong(currentContext, buf,I64_CONST(0)));
   ASSERT2_EQUALS(Sz, "999999999999999999",toStringLong(currentContext, buf,I64_CONST(999999999999999999)));
   ASSERT2_EQUALS(Sz, "-999999999999999999",toStringLong(currentContext, buf,I64_CONST(-999999999999999999)));
   ASSERT2_EQUALS(Sz, "9223372036854775807",toStringLong(currentContext, buf,I64_CONST(0x7FFFFFFFFFFFFFFF)));
   ASSERT2_EQUALS(Sz, "9223372036854775806",toStringLong(currentContext, buf,I64_CONST(9223372036854775806)));
   ASSERT2_EQUALS(Sz, "-9223372036854775808",toStringLong(currentContext, buf,I64_CONST(0x8000000000000000)));
   ASSERT2_EQUALS(Sz, "-9223372036854775807",toStringLong(currentContext, buf,I64_CONST(-9223372036854775807)));
   ASSERT2_EQUALS(Sz, "-2147483648",toStringLong(currentContext, buf,I64_CONST(0xFFFFFFFF80000000)));
   ASSERT2_EQUALS(Sz, "-2147483647",toStringLong(currentContext, buf,I64_CONST(-2147483647)));
   ASSERT2_EQUALS(Sz, "2147483647",toStringLong(currentContext, buf,I64_CONST(2147483647)));
   ASSERT2_EQUALS(Sz, "2147483646",toStringLong(currentContext, buf,I64_CONST(2147483646)));
   finish: ;
}

static int64 toLongString(Context currentContext, CharP buf)
{
   TNMParams p;
   TCObject obj;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;
   obj = createStringObjectFromCharP(currentContext, buf, -1);
   setObjectLock(obj, UNLOCKED);
   if (!obj)
      return 0;
   tsC_toLong_s(&p);
   return p.retL;
}

TESTCASE(tsC_toLong_s) // totalcross/sys/Convert native public static long toLong(String s);
{
   ASSERT2_EQUALS(I64, I64_CONST(0),toLongString(currentContext, ""));
   ASSERT2_EQUALS(I64, I64_CONST(0),toLongString(currentContext, "-"));
   ASSERT2_EQUALS(I64, I64_CONST(0),toLongString(currentContext, "1A"));
   ASSERT2_EQUALS(I64, I64_CONST(-1),toLongString(currentContext, "-1"));
   ASSERT2_EQUALS(I64, I64_CONST(0),toLongString(currentContext, "0"));
   ASSERT2_EQUALS(I64, I64_CONST(999999999999999999),toLongString(currentContext, "999999999999999999"));
   ASSERT2_EQUALS(I64, I64_CONST(-999999999999999999),toLongString(currentContext, "-999999999999999999"));
   ASSERT2_EQUALS(I64, I64_CONST(0x7FFFFFFFFFFFFFFF),toLongString(currentContext, "9223372036854775807"));
   ASSERT2_EQUALS(I64, I64_CONST(9223372036854775806),toLongString(currentContext, "9223372036854775806"));
   ASSERT2_EQUALS(I64, I64_CONST(0x8000000000000000),toLongString(currentContext, "-9223372036854775808"));
   ASSERT2_EQUALS(I64, I64_CONST(-9223372036854775807),toLongString(currentContext, "-9223372036854775807"));
   ASSERT2_EQUALS(I64, I64_CONST(0xFFFFFFFF80000000),toLongString(currentContext, "-2147483648"));
   ASSERT2_EQUALS(I64, I64_CONST(-2147483647),toLongString(currentContext, "-2147483647"));
   ASSERT2_EQUALS(I64, I64_CONST(2147483647),toLongString(currentContext, "2147483647"));
   ASSERT2_EQUALS(I64, I64_CONST(2147483646),toLongString(currentContext, "2147483646"));
   finish: ;
}
