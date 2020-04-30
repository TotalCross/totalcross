// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TESTCASE(jlS_toUpperCase) // java/lang/String native public String toUpperCase();
{
   TNMParams p;
   TCObject obj;
   CharP res8=null;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;
   obj = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_toUpperCase(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(obj), String_charsLen(p.retO));
   res8 = String2CharP(p.retO);
   ASSERT2_EQUALS(Sz, res8, "ERA UMA CASA T�O ENGRA�ADA, N�O TINHA MESA, N�O TINHA NADA.");
   finish:
   xfree(res8);
}
TESTCASE(jlS_toLowerCase) // java/lang/String native public String toLowerCase();
{
   TNMParams p;
   TCObject obj;
   CharP res8=null;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;
   obj = createStringObjectFromCharP(currentContext, "eRA UMA CASA T�O ENGRA�ADA, N�O TINHA MESA, N�O TINHA NADA.",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_toLowerCase(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(obj), String_charsLen(p.retO));
   res8 = String2CharP(p.retO);
   ASSERT2_EQUALS(Sz, res8, "era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.");
   finish:
   xfree(res8);
}

static CharP valueOfDouble(Context currentContext, CharP buf, double d, int32 size)
{
   TNMParams p;

   *buf = 0;
   tzero(p);
   p.currentContext = currentContext;
   p.dbl = &d;
   jlS_valueOf_d(&p);
   if (p.retO == null)
      return null;
   String2CharPBuf(p.retO, buf);
   buf[size] = 0;
   return buf;
}

TESTCASE(jlS_valueOf_d) // java/lang/String native public static String valueOf(double d);
{
   char buf[25];

   ASSERT2_EQUALS(Sz, "10.0", valueOfDouble(currentContext, buf,10, 4));
   ASSERT2_EQUALS(Sz, "0.0", valueOfDouble(currentContext, buf,0, 3));
   ASSERT2_EQUALS(Sz, "0.5", valueOfDouble(currentContext, buf,0.5, 3));
   ASSERT2_EQUALS(Sz, "0.55555555555", valueOfDouble(currentContext, buf,0.55555555555, 13));
   ASSERT2_EQUALS(Sz, "0.0055555555555", valueOfDouble(currentContext, buf,0.0055555555555, 15));
   ASSERT2_EQUALS(Sz, "0.000055555555555", valueOfDouble(currentContext, buf,0.000055555555555, 17));
   ASSERT2_EQUALS(Sz, "0.000000555555556", valueOfDouble(currentContext, buf, 0.00000055555555555, 17));
   ASSERT2_EQUALS(Sz, "0.000000005555556", valueOfDouble(currentContext, buf, 0.0000000055555555555, 17));
   ASSERT2_EQUALS(Sz, "0.000000005555556", valueOfDouble(currentContext, buf, 0.000000005555555555555, 17));
   ASSERT2_EQUALS(Sz, "0.000000000055556", valueOfDouble(currentContext, buf, 0.00000000005555555555555, 17));
   ASSERT2_EQUALS(Sz, "0.000000000000556", valueOfDouble(currentContext, buf, 0.000000000000555555555555555, 17));
   ASSERT2_EQUALS(Sz, "0.000000000000006", valueOfDouble(currentContext, buf, 0.00000000000000555555555555555, 17));
   ASSERT2_EQUALS(Sz, "5.55555555555555E-17", valueOfDouble(currentContext, buf,0.0000000000000000555555555555555, 20));
   ASSERT2_EQUALS(Sz, "10000.0", valueOfDouble(currentContext, buf,10000.0, 7));
   ASSERT2_EQUALS(Sz, "1000.543216", valueOfDouble(currentContext, buf, 1000.543216, 11));
   ASSERT2_EQUALS(Sz, "-100.54", valueOfDouble(currentContext, buf, -100.54, 7));
   ASSERT2_EQUALS(Sz, "1.0E-40", valueOfDouble(currentContext, buf, 1e-40, 7));
   ASSERT2_EQUALS(Sz, "-1.0E-40", valueOfDouble(currentContext, buf, -1e-40, 8));
   ASSERT2_EQUALS(Sz, "-1.0E40", valueOfDouble(currentContext, buf, -1e40, 7));
   ASSERT2_EQUALS(Sz, "1000000.0", valueOfDouble(currentContext, buf,1000000.0, 10));
   ASSERT2_EQUALS(Sz, "9999999.0", valueOfDouble(currentContext, buf,9999999.0, 10));
   ASSERT2_EQUALS(Sz, "0.99999999999", valueOfDouble(currentContext, buf,0.99999999999, 13));
   ASSERT2_EQUALS(Sz, "1.99999999999", valueOfDouble(currentContext, buf,1.99999999999, 13));
   ASSERT2_EQUALS(Sz, "-1.0E308", valueOfDouble(currentContext, buf,-1e308, 8));
   ASSERT2_EQUALS(Sz, "-1.432132689765E140", valueOfDouble(currentContext, buf,-1.432132689765E140, 19));
   ASSERT2_EQUALS(Sz, "-9.454938759257E240", valueOfDouble(currentContext, buf,-9.454938759257E240, 19));
   ASSERT2_EQUALS(Sz, "1.432132689765E140", valueOfDouble(currentContext, buf,1.432132689765E140, 18));
   ASSERT2_EQUALS(Sz, "9.454938759257E240", valueOfDouble(currentContext, buf,9.454938759257E240, 18));
   ASSERT2_EQUALS(Sz, "1.432132689765E-140", valueOfDouble(currentContext, buf,1.432132689765E-140, 19));
   ASSERT2_EQUALS(Sz, "9.454938759257E-240", valueOfDouble(currentContext, buf,9.454938759257E-240, 19));
   //ASSERT2_EQUALS(Sz, "+Inf", valueOfDouble(currentContext, buf,1.0/0.0));
   //ASSERT2_EQUALS(Sz, "-Inf", valueOfDouble(currentContext, buf,-1.0/0.0));
   finish: ;
}

static JChar valueOfChar(Context currentContext, JChar c)
{
   TNMParams p;
   int32 i;

   tzero(p);
   p.currentContext = currentContext;
   p.i32 = &i;
   i = (int32)c;
   jlS_valueOf_c(&p);
   if (p.retO == null || String_charsLen(p.retO) != 1)
      return null;
   return (JChar)String_charsStart(p.retO)[0];
}
TESTCASE(jlS_valueOf_c) // java/lang/String native public static String valueOf(char c);
{
   ASSERT2_EQUALS(U16, 'a',valueOfChar(currentContext, 'a'));
   ASSERT2_EQUALS(U16, '�',valueOfChar(currentContext, '�'));
   ASSERT2_EQUALS(U16, 0x1E00,valueOfChar(currentContext, (JChar)0x1E00));
   finish: ;
}

static CharP valueOfInt(Context currentContext, CharP buf, int32 i)
{
   TNMParams p;

   *buf = 0;
   tzero(p);
   p.currentContext = currentContext;
   p.i32 = &i;
   jlS_valueOf_i(&p);
   if (p.retO == null)
      return null;
   String2CharPBuf(p.retO, buf);
   return buf;
}

TESTCASE(jlS_valueOf_i) // java/lang/String native public static String valueOf(int i);
{
   char buf[15];
   ASSERT2_EQUALS(Sz, "2147483647",valueOfInt(currentContext, buf, 2147483647));
   ASSERT2_EQUALS(Sz, "12345678", valueOfInt(currentContext, buf, 12345678));
   ASSERT2_EQUALS(Sz, "-12345678", valueOfInt(currentContext, buf, -12345678));
   ASSERT2_EQUALS(Sz, "305419896", valueOfInt(currentContext, buf, (int)0x12345678));
   ASSERT2_EQUALS(Sz, "999", valueOfInt(currentContext, buf, +999));
   ASSERT2_EQUALS(Sz, "-999", valueOfInt(currentContext, buf, -999));
   ASSERT2_EQUALS(Sz, "1", valueOfInt(currentContext, buf, 1));
   ASSERT2_EQUALS(Sz, "-1", valueOfInt(currentContext, buf, -1));
   ASSERT2_EQUALS(Sz, "0", valueOfInt(currentContext, buf, 0));
   finish: ;
}
TESTCASE(jlS_indexOf_i) // java/lang/String native public int indexOf(int c);
{
   TNMParams p;
   TCObject obj;
   int32 i;
   JCharP j;

   tzero(p);
   p.currentContext = currentContext;
   obj = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   p.obj = &obj;
   p.i32 = &i;
   j = String_charsStart(obj);

   i = (int32)'E';
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   i = 231/*'�'*/;
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 22);

   i = (int32)'z';
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   i = (int32)'.';
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 58);

   i = 227/*'�'*/;
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 14);

   i = (int32)',';
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 26);

   i = (int32)' ';
   jlS_indexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 3);

   finish: ;
}
TESTCASE(jlS_indexOf_ii) // java/lang/String native public int indexOf(int c, int startIndex);
{
   TNMParams p;
   TCObject obj;
   int32 is[2];
   JCharP j;

   tzero(p);
   p.currentContext = currentContext;
   obj = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   p.obj = &obj;
   p.i32 = is;
   j = String_charsStart(obj);

   is[0] = (int32)'E'; is[1] = 0;
   jlS_indexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   is[0] = (int32)'E'; is[1] = 2;
   jlS_indexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   is[0] = 231/*'�'*/; is[1] = 0;
   jlS_indexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 22);

   is[0] = 231/*'�'*/; is[1] = 22;
   jlS_indexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 22);

   is[0] = 231/*'�'*/; is[1] = 23;
   jlS_indexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   finish: ;
}
TESTCASE(jlS_equals_o) // java/lang/String native public boolean equals(Object obj);
{
   TNMParams p;
   TCObject objs[2];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;
   p.retI = 999; // 0 will be the first test result

   objs[0] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   objs[1] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada!",-1);
   setObjectLock(objs[0], UNLOCKED);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_equals_o(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   objs[1] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_equals_o(&p);
   ASSERT2_EQUALS(I32, p.retI, 1);

   objs[1] = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_equals_o(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);
   finish: ;
}
TESTCASE(jlS_compareTo_s) // java/lang/String native public int compareTo(String s);
{
   TNMParams p;
   TCObject objs[2];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;

   objs[0] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   objs[1] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada!",-1);
   setObjectLock(objs[0], UNLOCKED);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   ASSERT1_EQUALS(NotNull, objs[1]);

   jlS_compareTo_s(&p);
   ASSERT2_EQUALS(I32, p.retI, '.'-'!');

   objs[1] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_compareTo_s(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   finish: ;
}
TESTCASE(jlS_copyChars_CiCii) // java/lang/String native static boolean copyChars(char []srcArray, int srcStart, char []dstArray, int dstStart, int length);
{
   TNMParams p;
   TCObject objs[2],res;
   int32 i[3];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;
   p.i32 = i;

   objs[0] = createStringObjectFromCharP(currentContext, "n�o tinha mesa",-1);
   objs[1] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada!",-1);
   res     = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha mesa!",-1);
   setObjectLock(objs[0], UNLOCKED);
   setObjectLock(objs[1], UNLOCKED);
   setObjectLock(res, UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   ASSERT1_EQUALS(NotNull, objs[1]);
   ASSERT1_EQUALS(NotNull, res);
   objs[0] = String_chars(objs[0]);
   objs[1] = String_chars(objs[1]);

   i[0] = 0; i[1] = 44; i[2] = 14;
   jlS_copyChars_CiCii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI);
   ASSERT3_EQUALS(Block, ARRAYOBJ_START(objs[1]), String_charsStart(res), 2*String_charsLen(res));

   i[0] = 0; i[1] = 44; i[2] = 16;
   jlS_copyChars_CiCii(&p);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   ASSERT1_EQUALS(False, p.retI);
   currentContext->thrownException = null;

   finish: ;
}
TESTCASE(jlS_indexOf_si) // java/lang/String native public int indexOf(String c, int startIndex);
{
   TNMParams p;
   TCObject objs[2];
   int32 s;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;
   p.i32 = &s;

   objs[0] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   objs[1] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada!",-1);
   setObjectLock(objs[0], UNLOCKED);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   ASSERT1_EQUALS(NotNull, objs[1]);

   s = 0;
   jlS_indexOf_si(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   s = -1;
   jlS_indexOf_si(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   objs[1] = createStringObjectFromCharP(currentContext, "n�o tinha nada.",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);

   s = 0;
   jlS_indexOf_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 44);

   s = 43;
   jlS_indexOf_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 44);

   s = 44;
   jlS_indexOf_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 44);

   s = 45;
   jlS_indexOf_si(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   finish: ;
}
TESTCASE(jlS_hashCode) // java/lang/String native public int hashCode();
{
   TNMParams p;
   TCObject obj;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;

   obj = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 313238293);

   obj = createStringObjectFromCharP(currentContext, "",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   obj = createStringObjectFromCharP(currentContext, "1",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 49);

   obj = createStringObjectFromCharP(currentContext, "123",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 48690);

   obj = createStringObjectFromCharP(currentContext, "123456",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 1450575459);

   obj = createStringObjectFromCharP(currentContext, "123456789",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, -1867378635);

   obj = createStringObjectFromCharP(currentContext, "123456789012",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 1634517500);

   obj = createStringObjectFromCharP(currentContext, "123456789012345",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, 1866658424);

   obj = createStringObjectFromCharP(currentContext, "123456789012345678",-1);
   setObjectLock(obj, UNLOCKED);
   jlS_hashCode(&p);
   ASSERT2_EQUALS(I32, p.retI, -1615385569);

   finish: ;
}
TESTCASE(jlS_startsWith_si) // java/lang/String native public boolean startsWith(String prefix, int from);
{
   TNMParams p;
   TCObject objs[2];
   int32 from;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;
   p.i32 = &from;

   objs[0] = createStringObjectFromCharP(currentContext, "Barbara Hazan",-1);
   setObjectLock(objs[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   objs[1] = createStringObjectFromCharP(currentContext, "Barbara",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);

   from = 0;
   jlS_startsWith_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 1);

   from = 1;
   jlS_startsWith_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   from = -1;
   jlS_startsWith_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   objs[1] = createStringObjectFromCharP(currentContext, "Barbara Hazan2",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   from = 0;
   jlS_startsWith_si(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   finish: ;
}
TESTCASE(jlS_endsWith_s) // java/lang/String native public boolean endsWith(String suffix);
{
   TNMParams p;
   TCObject objs[2];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;

   objs[0] = createStringObjectFromCharP(currentContext, "Barbara Hazan",-1);
   setObjectLock(objs[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   objs[1] = createStringObjectFromCharP(currentContext, "Hazan",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);

   jlS_endsWith_s(&p);
   ASSERT2_EQUALS(I32, p.retI, 1);

   objs[1] = createStringObjectFromCharP(currentContext, "Hazan ",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_endsWith_s(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   objs[1] = createStringObjectFromCharP(currentContext, "Barbara Hazan ",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_endsWith_s(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   finish: ;
}
TESTCASE(jlS_equalsIgnoreCase_s) // java/lang/String native public boolean equalsIgnoreCase(String s);
{
   TNMParams p;
   TCObject objs[2];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;
   p.retI = 999; // 0 will be the first test result

   objs[0] = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   objs[1] = createStringObjectFromCharP(currentContext, "eRA UMA CASA T�O ENGRA�ADA, N�O TINHA MESA, N�O TINHA NADA.",-1);
   setObjectLock(objs[0], UNLOCKED);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_equalsIgnoreCase_s(&p);
   ASSERT2_EQUALS(I32, p.retI, 1);

   objs[1] = createStringObjectFromCharP(currentContext, "eRA UMA CASA T�O ENGRA�ADA, N�O TINHA MESA, N�O TINHA NADA!",-1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlS_equalsIgnoreCase_s(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);
   finish: ;
}
TESTCASE(jlS_replace_cc) // java/lang/String native public String replace(char oldChar, char newChar);
{
   TNMParams p;
   TCObject obj,obj2;
   int32 is[2];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;
   p.i32 = is;

   // replace a by o
   obj = createStringObjectFromCharP(currentContext, "Barbara Hazan",-1);
   obj2= createStringObjectFromCharP(currentContext, "Borboro Hozon",-1);
   setObjectLock(obj, UNLOCKED);
   setObjectLock(obj2, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   ASSERT1_EQUALS(NotNull, obj2);
   is[0] = 'a'; // old
   is[1] = 'o'; // new
   jlS_replace_cc(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(obj2), String_charsLen(p.retO)); // compare with the target's length
   ASSERT3_EQUALS(Block, String_charsStart(obj2), String_charsStart(p.retO), 13*2);

   // now go back to the original
   is[0] = 'o'; // old
   is[1] = 'a'; // new
   p.obj = &obj2;
   jlS_replace_cc(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(obj), String_charsLen(p.retO)); // compare with the target's length
   ASSERT3_EQUALS(Block, String_charsStart(obj), String_charsStart(p.retO), 13*2);

   finish: ;
}
TESTCASE(jlS_lastIndexOf_ii) // java/lang/String native public int lastIndexOf(int c, int startIndex);
{
   TNMParams p;
   TCObject obj;
   int32 is[2];
   JCharP j;

   tzero(p);
   p.currentContext = currentContext;
   obj = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   p.obj = &obj;
   p.i32 = is;
   j = String_charsStart(obj);

   is[0] = (int32)'E'; is[1] = 0;
   jlS_lastIndexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   is[0] = (int32)'E'; is[1] = 2;
   jlS_lastIndexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   is[0] = 227; is[1] = 13; // �
   jlS_lastIndexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   is[0] = 227; is[1] = 14;
   jlS_lastIndexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 14);

   is[0] = 227; is[1] = 50;
   jlS_lastIndexOf_ii(&p);
   ASSERT2_EQUALS(I32, p.retI, 45);

   finish: ;
}
TESTCASE(jlS_lastIndexOf_i) // java/lang/String native public int lastIndexOf(int c);
{
   TNMParams p;
   TCObject obj;
   int32 i;
   JCharP j;

   tzero(p);
   p.currentContext = currentContext;
   obj = createStringObjectFromCharP(currentContext, "Era uma casa t�o engra�ada, n�o tinha mesa, n�o tinha nada.",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   p.obj = &obj;
   p.i32 = &i;
   j = String_charsStart(obj);

   i = (int32)'E';
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 0);

   i = 231/*'�'*/;
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 22);

   i = (int32)'z';
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, -1);

   i = (int32)'.';
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 58);

   i = 227/*'�'*/;
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 45);

   i = (int32)',';
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 42);

   i = (int32)' ';
   jlS_lastIndexOf_i(&p);
   ASSERT2_EQUALS(I32, p.retI, 53);

   finish: ;
}
TESTCASE(jlS_trim) // java/lang/String native public String trim();
{
   TNMParams p;
   TCObject obj;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &obj;

   // something will be trimmed
   obj = createStringObjectFromCharP(currentContext, "  Vera Nardelli   ",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 13); // compare with the target's length
   ASSERT3_EQUALS(Block, String_charsStart(obj)+2, String_charsStart(p.retO), 13*2);

   // nothing to be trimmed
   obj = createStringObjectFromCharP(currentContext, "Vera Nardelli",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT2_EQUALS(Ptr, obj, p.retO); // the original object must be returned

   obj = createStringObjectFromCharP(currentContext, " ",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 0);

   obj = createStringObjectFromCharP(currentContext, "  ",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 0);

   obj = createStringObjectFromCharP(currentContext, "",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 0);

   obj = createStringObjectFromCharP(currentContext, " t ",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 1);

   obj = createStringObjectFromCharP(currentContext, "  t",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 1);

   obj = createStringObjectFromCharP(currentContext, "t  ",-1);
   setObjectLock(obj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, obj);
   jlS_trim(&p);
   ASSERT1_EQUALS(NotNull, p.retO);
   ASSERT2_EQUALS(I32, String_charsLen(p.retO), 1);

   finish: ;
}
