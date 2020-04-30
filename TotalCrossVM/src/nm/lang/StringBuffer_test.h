// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TESTCASE(jlSB_aensureCapacity_i) // java/lang/StringBuffer native public void ensureCapacity(int minimumCapacity);
{
   TNMParams p;
   TCObject sb;
   int32 i;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &sb;
   p.i32 = &i;

   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);
   i = 500;
   jlSB_ensureCapacity_i(&p);
   ASSERT1_EQUALS(NotNull, StringBuffer_chars(sb));
   ASSERT2_EQUALS(I32, StringBuffer_charsLen(sb), 500);

   finish: ;
}
TESTCASE(jlSB_setLength_i) // java/lang/StringBuffer native public void setLength(int newLength);
{
   TNMParams p;
   TCObject sb;
   int32 i,i1;
   int32 *STARTING_SIZE;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &sb;
   p.i32 = &i;

   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);

   STARTING_SIZE = getStaticFieldInt(OBJ_CLASS(sb), "STARTING_SIZE");
   ASSERT1_EQUALS(NotNull, STARTING_SIZE);
   ASSERT_BETWEEN(I32, 1, *STARTING_SIZE, 5000);

   ASSERT1_EQUALS(NotNull, StringBuffer_chars(sb));

   i1 = i = *STARTING_SIZE + 220;
   jlSB_setLength_i(&p);
   ASSERT1_EQUALS(NotNull, StringBuffer_chars(sb));
   ASSERT2_EQUALS(I32, StringBuffer_charsLen(sb), i);
   ASSERT2_EQUALS(I32, StringBuffer_count(sb), i);

   i = *STARTING_SIZE;
   jlSB_setLength_i(&p);
   ASSERT1_EQUALS(NotNull, StringBuffer_chars(sb));
   ASSERT2_EQUALS(I32, StringBuffer_charsLen(sb), i1);
   ASSERT2_EQUALS(I32, StringBuffer_count(sb), i);

   finish: ;
}


TESTCASE(jlSB_append_s) // java/lang/StringBuffer native public StringBuffer append(String str);
{
   TNMParams p;
   TCObject objs[2];
   char buf[100];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;

   objs[0] = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(objs[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);

   objs[1] = createStringObjectFromCharP(currentContext, "Barbara", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlSB_append_s(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara", buf);

   objs[1] = createStringObjectFromCharP(currentContext, " Michelle", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlSB_append_s(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara Michelle", buf);

   objs[1] = createStringObjectFromCharP(currentContext, " Verinha", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlSB_append_s(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara Michelle Verinha", buf);

   objs[1] = createStringObjectFromCharP(currentContext, "(saudades)", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   jlSB_append_s(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara Michelle Verinha(saudades)", buf);

   finish: ;
}
TESTCASE(jlSB_append_C) // java/lang/StringBuffer native public StringBuffer append(char []str);
{
   TNMParams p;
   TCObject objs[2];
   char buf[50];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;

   objs[0] = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(objs[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);

   objs[1] = createStringObjectFromCharP(currentContext, "Barbara", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   objs[1] = String_chars(objs[1]);
   jlSB_append_C(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara", buf);

   objs[1] = createStringObjectFromCharP(currentContext, " Michelle", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   objs[1] = String_chars(objs[1]);
   jlSB_append_C(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara Michelle", buf);

   objs[1] = createStringObjectFromCharP(currentContext, " Verinha", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   objs[1] = String_chars(objs[1]);
   jlSB_append_C(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara Michelle Verinha", buf);

   objs[1] = createStringObjectFromCharP(currentContext, "(saudades)", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   objs[1] = String_chars(objs[1]);
   jlSB_append_C(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara Michelle Verinha(saudades)", buf);

   finish: ;
}
TESTCASE(jlSB_append_Cii) // java/lang/StringBuffer native public StringBuffer append(char []str, int offset, int len);
{
   TNMParams p;
   TCObject objs[2];
   int32 is[2];
   char buf[50];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = objs;
   p.i32 = is;

   objs[0] = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(objs[0], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[0]);

   objs[1] = createStringObjectFromCharP(currentContext, "Barbara", -1);
   setObjectLock(objs[1], UNLOCKED);
   ASSERT1_EQUALS(NotNull, objs[1]);
   objs[1] = String_chars(objs[1]);

   is[0] = 0; is[1] = 7;
   jlSB_append_Cii(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbara", buf);

   is[0] = 3; is[1] = 4;
   jlSB_append_Cii(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbarabara", buf);

   is[0] = 6; is[1] = 1;
   jlSB_append_Cii(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbarabaraa", buf);

   is[0] = 0; is[1] = 8; // ArrayIndexOutOfBoundsException
   jlSB_append_Cii(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "Barbarabaraa", buf); // unchanged
   currentContext->thrownException = null;

   is[0] = 0; is[1] = 1;
   jlSB_append_Cii(&p);
   JCharP2CharPBuf(StringBuffer_charsStart(objs[0]), StringBuffer_charsLen(objs[0]), buf);
   ASSERT2_EQUALS(Sz, "BarbarabaraaB", buf);

   finish: ;
}
TESTCASE(jlSB_append_c) // java/lang/StringBuffer native public StringBuffer append(char c);
{
   TNMParams p;
   TCObject sb;
   int32 i,c,n;
   CharP text = "A Barbara e' a cara do pai";
   JChar jtext[30];

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &sb;
   p.i32 = &c;

   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);

   CharP2JCharPBuf(text, -1, jtext, false);
   ASSERT2_EQUALS(I32, (int32)text[0], (int32)jtext[0]);

   for (i=0, n=xstrlen(text); i < n; i++)
   {
      c = (int32)text[i];
      jlSB_append_c(&p);
      ASSERT3_EQUALS(Block, jtext, StringBuffer_charsStart(sb), i*2);
   }
   ASSERT2_EQUALS(I32, StringBuffer_count(sb), n);

   finish: ;
}

static CharP sbAppend(Context currentContext, CharP buf, TCObject sb, TValue v, RegType t)
{
   TNMParams p;

   tzero(p);
   p.currentContext = currentContext;
   p.obj = &sb;
   switch (t) // (regO never used)
   {
      case RegD: p.dbl = &v.asDouble; jlSB_append_d(&p); break;
      case RegL: p.i64 = &v.asInt64;  jlSB_append_l(&p); break;
      default:   p.i32 = &v.asInt32;  jlSB_append_i(&p); break; // regI
   }
   JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf);
   return buf;
}

TESTCASE(jlSB_append_i) // java/lang/StringBuffer native public StringBuffer append(int i);
{
   char buf[50];
   TCObject sb;
   TValue v;

   xmemzero(buf, sizeof(buf));
   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);

   v.asInt32 = 0        ; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegI), "0");
   v.asInt32 = 1234     ; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegI), "01234");
   v.asInt32 = -5321    ; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegI), "01234-5321");
   v.asInt32 = 0xFFFFFFF; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegI), "01234-5321268435455");

   finish: ;
}
TESTCASE(jlSB_append_l) // java/lang/StringBuffer native public StringBuffer append(long l)
{
   char buf[50];
   TCObject sb;
   TValue v;

   xmemzero(buf, sizeof(buf));
   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);

   v.asInt64 = 0        ; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegL), "0");
   v.asInt64 = 1234     ; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegL), "01234");
   v.asInt64 = -5321    ; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegL), "01234-5321");
   v.asInt64 = 0xFFFFFFF; ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegL), "01234-5321268435455");
   v.asInt64 = I64_CONST(0xFFFFFFFFFFFF); ASSERT2_EQUALS(Sz, sbAppend(currentContext, buf, sb, v, RegL), "01234-5321268435455281474976710655");

   finish: ;
}
TESTCASE(jlSB_append_d) // java/lang/StringBuffer native public StringBuffer append(double d);
{
   char buf[128];
   TCObject sb;
   TValue v;

   xmemzero(buf, sizeof(buf));
   sb = createObject(currentContext, "java.lang.StringBuffer");
   setObjectLock(sb, UNLOCKED);
   ASSERT1_EQUALS(NotNull, sb);

   v.asDouble = 0;
   sbAppend(currentContext, buf, sb, v, RegD);
   StringBuffer_count(sb) = 3;
   ASSERT2_EQUALS(Sz, JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf), "0.0");

   v.asDouble = 1234;
   sbAppend(currentContext, buf, sb, v, RegD);
   StringBuffer_count(sb) = 9;
   ASSERT2_EQUALS(Sz, JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf), "0.01234.0");

   v.asDouble = -5321;
   sbAppend(currentContext, buf, sb, v, RegD);
   StringBuffer_count(sb) = 16;
   ASSERT2_EQUALS(Sz, JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf), "0.01234.0-5321.0");

   v.asDouble = 0.5432424234;
   sbAppend(currentContext, buf, sb, v, RegD);
   StringBuffer_count(sb) = 28;
   ASSERT2_EQUALS(Sz, JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf), "0.01234.0-5321.00.5432424234");
   
   v.asDouble = -13132.5153513;
   sbAppend(currentContext, buf, sb, v, RegD);
   StringBuffer_count(sb) = 42;
   ASSERT2_EQUALS(Sz, JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf), "0.01234.0-5321.00.5432424234-13132.5153513");

   v.asDouble = 1E300;
   sbAppend(currentContext, buf, sb, v, RegD);
   StringBuffer_count(sb) = 49;
   ASSERT2_EQUALS(Sz, JCharP2CharPBuf(StringBuffer_charsStart(sb), StringBuffer_count(sb), buf), "0.01234.0-5321.00.5432424234-13132.51535131.0E300");

   finish: ;
}
