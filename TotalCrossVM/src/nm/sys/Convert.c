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
#include "../ui/PalmFont.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tsC_equals_BB(NMParams p) // totalcross/sys/Convert native public static boolean equals(byte []b1, byte []b2);
{
   TCObject b1 = p->obj[0];
   TCObject b2 = p->obj[1];
   bool ret = true;
   if (b1 != null && b2 != null && ARRAYOBJ_LEN(b1) == ARRAYOBJ_LEN(b2))
   {
      uint8* a1 = (uint8*)ARRAYOBJ_START(b1);
      uint8* a2 = (uint8*)ARRAYOBJ_START(b2);
      int32 len = ARRAYOBJ_LEN(b1);
      while (--len >= 0)
         if (*a1++ != *a2++)
         {
            ret = false;
            break;
         }
   } else ret = b1 == b2;
   p->retI = ret;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toInt_s(NMParams p) // totalcross/sys/Convert native public static int toInt(String s);
{
   TCObject string = p->obj[0];
   IntBuf buffer;

   if (!string)
      throwNullArgumentException(p->currentContext, "s");
   else
   if (String_charsLen(string) >= sizeof(buffer)) // guich@tc123_2: check if argument fits in buffer
      throwException(p->currentContext, InvalidNumberException, "Error: %s is not a valid integer value.", buffer); // guich@tc123_9
   else
   {
      bool err = false;
      String2CharPBuf(string, buffer);
      p->retI = str2int(buffer, &err);
      if (err || !buffer[0])
         throwException(p->currentContext, InvalidNumberException, "Error: %s is not a valid integer value.", buffer);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toString_c(NMParams p) // totalcross/sys/Convert native public static String toString(char c);
{
   p->retO = createStringObjectFromJCharP(p->currentContext, (JCharP) p->i32, 1);
   if (p->retO)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_doubleToIntBits_d(NMParams p) // totalcross/sys/Convert native public static int doubleToIntBits(double d);
{
   float f = (float) p->dbl[0];
   p->retI = *((int32*) &f);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_intBitsToDouble_i(NMParams p) // totalcross/sys/Convert native public static double intBitsToDouble(int i);
{
   float f = *((float*) (&p->i32[0]));
   p->retD = (double) f;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toString_i(NMParams p) // totalcross/sys/Convert native public static String toString(int i);
{
   CharP buffer;
   IntBuf ib;

   buffer = int2str(p->i32[0], ib);
   p->retO = createStringObjectFromCharP(p->currentContext, buffer, -1);
   if (p->retO)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toString_di(NMParams p) // totalcross/sys/Convert native public static String toString(double d, int precision);
{
   int32 precision = p->i32[0];
   CharP buffer;
   DoubleBuf db;

   if (precision < -1)
      throwIllegalArgumentExceptionI(p->currentContext, "precision",precision);
   else
   {
      buffer = double2str(p->dbl[0], precision, db);
      p->retO = createStringObjectFromCharP(p->currentContext, buffer, -1);
      if (p->retO)
         setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toDouble_s(NMParams p) // totalcross/sys/Convert native public static double toDouble(String s);
{
   TCObject string = p->obj[0];
   DoubleBuf buffer;

   if (!string)
      throwNullArgumentException(p->currentContext, "s");
   else
   if (String_charsLen(string) == 0)
      throwException(p->currentContext, InvalidNumberException, "Error: argument s cannot an empty value.");
   else
   if (String_charsLen(string) >= sizeof(buffer)) // guich@tc123_2: check if argument fits in buffer
      throwException(p->currentContext, InvalidNumberException, "Error (1): %s is not a valid double value (%d > %d).", buffer, String_charsLen(string), sizeof(buffer)); // guich@tc123_9
   else
   {
      bool err = false;
      String2CharPBuf(string, buffer);
      p->retD = str2double(buffer, &err);
      if (err)
      {
#ifndef WINCE
         double result;
         CharP endPtr=null;
         String2CharPBuf(string, buffer);
         p->retD = result = strtod(buffer, &endPtr);
         if (*endPtr != 0 || result == HUGE_VAL || result == -HUGE_VAL) // guich: testing for endPtr to see if it stopped in an invalid character
            throwException(p->currentContext, InvalidNumberException, "Error (2): %s is not a valid double value (%s / %f / %f).", buffer, endPtr, result, HUGE_VAL);
#else
            throwException(p->currentContext, InvalidNumberException, "Error (2): %s is not a valid double value.", buffer);
#endif
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toString_si(NMParams p) // totalcross/sys/Convert native public static String toString(String doubleValue, int n);
{
   TCObject doubleValue = p->obj[0];
   int32 n = p->i32[0];
   CharP buffer;
   double d;
   DoubleBuf db;

   if (!doubleValue)
      throwNullArgumentException(p->currentContext, "doubleValue");
   else
   if (n < -1)
      throwIllegalArgumentExceptionI(p->currentContext, "n",n);
   else
   if ((buffer = String2CharP(doubleValue)) != null)
   {
      bool err = false;
      d = str2double(buffer, &err);
      if (err || !buffer[0])
         throwException(p->currentContext, InvalidNumberException, "Error: %s is not a valid double value.", buffer);
      else
      {
         CharP str = double2str(d, n, db);
         p->retO = createStringObjectFromCharP(p->currentContext, str, -1);
         setObjectLock(p->retO, UNLOCKED);
      }
      xfree(buffer); // guich@tc110_33: moved to here
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_doubleToLongBits_d(NMParams p) // totalcross/sys/Convert native public static long doubleToLongBits(double value);
{
   p->retL = p->i64[0];
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_longBitsToDouble_l(NMParams p) // totalcross/sys/Convert native public static double longBitsToDouble(long bits);
{
   tsC_doubleToLongBits_d(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toLowerCase_c(NMParams p) // totalcross/sys/Convert native public static char toLowerCase(char c);
{
   p->retI = JCharToLower((JChar) p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toUpperCase_c(NMParams p) // totalcross/sys/Convert native public static char toUpperCase(char c);
{
   p->retI = JCharToUpper((JChar) p->i32[0]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_unsigned2hex_ii(NMParams p) // totalcross/sys/Convert native public static String unsigned2hex(int b, int places);
{
   int32 places = p->i32[1];
   char buffer[9];

   if (places < 0 || places > 8)
      throwIllegalArgumentExceptionI(p->currentContext, "places",places);
   else
   {
      int2hex(p->i32[0], places, buffer);
      p->retO = createStringObjectFromCharP(p->currentContext, buffer, places);
      if (p->retO)
         setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_hashCode_s(NMParams p) // totalcross/sys/Convert native public static int hashCode(StringBuffer sb);
{
   TCObject stringBuffer = p->obj[0];

   if (!stringBuffer)
      throwNullArgumentException(p->currentContext, "sb");
   else
      p->retI = JCharPHashCode(StringBuffer_charsStart(stringBuffer), StringBuffer_count(stringBuffer));
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_getBreakPos_fsiib(NMParams p) // totalcross/sys/Convert native public static int getBreakPos(totalcross.ui.font.FontMetrics fm, StringBuffer sb, int start, int width, boolean doWordWrap);
{
   TCObject fm = p->obj[0];
   TCObject sb = p->obj[1];
   int32 start = p->i32[0];
   int32 width = p->i32[1];
   bool doWordWrap = p->i32[2];
   int32 n = StringBuffer_count(sb) - start;
   JCharP buf = StringBuffer_charsStart(sb);
   int32 lastSpace = -1;
   JChar c;
   TCObject font = FontMetrics_font(fm);
   int32 oldStart = start;

   if (!fm)
      throwNullArgumentException(p->currentContext, "fm");
   else
   if (!sb)
      throwNullArgumentException(p->currentContext, "sb");
   else
   if (start < 0)
      throwIllegalArgumentExceptionI(p->currentContext, "start",start);
   else
   if (width < 0)
      throwIllegalArgumentExceptionI(p->currentContext, "width",width);
   else
   if (doWordWrap)
   {
      for (; n-- > 0 && width > 0; start++)
      {
         c = *(buf+start);
         if (c == '\n')
            goto finish;
         if (c == ' ')
            lastSpace = start;
         width -= getJCharWidth(p->currentContext, font, c);
      }
      start--; // stop at the previous letter
      if (((n > 0 || width < 0) || (width == 0 && *(buf+start) != ' ')) && lastSpace >= 0) // if the previous space is near, break at it - guich@tc114_79: also if broke at a non-letter at the end of the string
         start = lastSpace <= start ? lastSpace : (lastSpace+1); // the space is "included" in the previous word
      else
      if (width < 0) // beyond limit?
         start = max32(oldStart, start-1);
      start++;
   }
   else
   {
      for (; n-- > 0 && width > 0; start++)
         width -= getJCharWidth(p->currentContext, font, *(buf+start));
      if (width < 0) // beyond limit?
         start--;
   }

finish:
   p->retI = start;
}
//////////////////////////////////////////////////////////////////////////
void SB_insertAt_sic(NMParams p); // implemented in file nm/lang/StringBuffer.c
TC_API void tsC_insertAt_sic(NMParams p) // totalcross/sys/Convert native public static StringBuffer insertAt(StringBuffer sb, int pos, char c)
{
   SB_insertAt_sic(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toString_l(NMParams p) // totalcross/sys/Convert native public static String toString(long l);
{
   CharP buffer;
   LongBuf lb;

   buffer = long2str(p->i64[0], lb);
   p->retO = createStringObjectFromCharP(p->currentContext, buffer, -1);
   if (p->retO)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_toLong_s(NMParams p) // totalcross/sys/Convert native public static long toLong(String s);
{
   LongBuf buffer;
   TCObject string = p->obj[0];

   if (!string)
      throwNullArgumentException(p->currentContext, "s");
   else
   if (String_charsLen(string) >= sizeof(buffer)) // guich@tc123_2: check if argument fits in buffer
      throwException(p->currentContext, InvalidNumberException, "Error: %s is not a valid long value.", buffer); // guich@tc123_9
   else
   {
      bool err = false;
      String2CharPBuf(string, buffer);
      p->retL = str2long(buffer, &err);
      if (err || !buffer[0])
         throwException(p->currentContext, InvalidNumberException, "Error: %s is not a valid long value.", buffer);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Ciic(NMParams p) // totalcross/sys/Convert native public static void fill(char []a, int from, int to, char value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   int32 value = p->i32[2];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      JChar* a = (JChar*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Biib(NMParams p) // totalcross/sys/Convert native public static void fill(boolean []a, int from, int to, boolean value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   int32 value = p->i32[2];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      uint8* a = (uint8*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Iiii(NMParams p) // totalcross/sys/Convert native public static void fill(int []a, int from, int to, int value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   int32 value = p->i32[2];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      int32* a = (int32*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Diid(NMParams p) // totalcross/sys/Convert native public static void fill(double []a, int from, int to, double value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   double value = p->dbl[0];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      double* a = (double*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Siii(NMParams p) // totalcross/sys/Convert native public static void fill(short []a, int from, int to, int value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   int16 value = (int16)p->i32[2];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      int16* a = (int16*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Biii(NMParams p) // totalcross/sys/Convert native public static void fill(byte []a, int from, int to, int value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   int8 value = (int8)p->i32[2];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      int8* a = (int8*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Liil(NMParams p) // totalcross/sys/Convert native public static void fill(long []a, int from, int to, long value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   int64 value = p->i64[0];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      int64* a = (int64*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_fill_Oiio(NMParams p) // totalcross/sys/Convert native public static void fill(Object []a, int from, int to, Object value);
{
   TCObject ao = p->obj[0];
   int32 from = p->i32[0];
   int32 to = p->i32[1];
   TCObject value = p->obj[1];
   if (checkArrayRange(p->currentContext, ao, from, to-from))
   {
      TCObject* a = (TCObject*)ARRAYOBJ_START(ao);
      for (a += from; from < to; from++)
         *a++ = value;
   }
}
//////////////////////////////////////////////////////////////////////////
void SB_delete(TCObject sb, int32 start, int32 end);
void SB_insert(Context currentContext, TCObject obj, JCharP what, int32 pos, int32 wlen);
TCObject S_replace(Context currentContext, TCObject me, JChar oldChar, JChar newChar);

TC_API void tsC_replace_sss(NMParams p) // totalcross/sys/Convert native public static String replace(String source, String from, String to);
{
   // guich@tc123_10: pre-compute the number of changes and create a single string to store the result. 
   // The pattern is searched inside the String and replaced inside the target string
   TCObject source = p->obj[0], from = p->obj[1], to = p->obj[2];
   if (source == null)
      throwNullArgumentException(p->currentContext, "source");
   else
   if (from == null)
      throwNullArgumentException(p->currentContext, "from");
   else
   if (to == null)
      throwNullArgumentException(p->currentContext, "to");
   else
   {
      int32 f = String_charsLen(from), t = String_charsLen(to), s = String_charsLen(source), last=0, tlast=0;
      JCharP toc = String_charsStart(to);
      JCharP fromc = String_charsStart(from);

      // if user is trying to replace a single char, use a faster routine
      if (f == 1 && t == 1)
         p->retO = S_replace(p->currentContext, source, fromc[0], toc[0]);
      else
      {
         JCharP sourcec = String_charsStart(source), targetc;
         int32 count = 0;
         // count how many times the string appears
         while (last >= 0)
         {
            last = JCharPIndexOfJCharP(sourcec, fromc, last, s, f); // source.indexOf(from,last);
            if (last == -1) 
               break;
            count++;
            last += f;
         }
         if (count == 0) // if the string doesn't appear, return itself
            p->retO = source;
         else
         {
            TCObject target = createStringObjectWithLen(p->currentContext, s + (t-f) * count); // create a string with the new length
            if (target == null)
               return;
            targetc = String_charsStart(target);
            last = 0;

            while (last >= 0)
            {
               int32 now = JCharPIndexOfJCharP(sourcec, fromc, last, s, f); // source.indexOf(from,last);
               if (now == -1) 
               {
                  // copy to the end of the string
                  if (s != last)
                     xmemmove(targetc + tlast, sourcec + last, (s-last)*2);
                  break;
               }
               xmemmove(targetc + tlast, sourcec + last, (now-last)*2); // copy from old position into the new one
               tlast += now - last;
               last = now;
               xmemmove(targetc + tlast, toc, t*2); 
               last += f;
               tlast += t;
            }
            setObjectLock(p->retO = target, UNLOCKED);
         }
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TCObject chars2bytes(Context currentContext, JCharP chars, int32 length); // CharacterConverter.c
TC_API void tsC_getBytes_s(NMParams p) // totalcross/sys/Convert native public static byte[] getBytes(StringBuffer sb);
{
   TCObject sb = p->obj[0];
   if (sb == null)
      throwNullArgumentException(p->currentContext, "sb");
   else
   {
      JChar* chars = StringBuffer_charsStart(sb);
      int32 length = StringBuffer_count(sb);
      p->retO = chars2bytes(p->currentContext, chars, length);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_numberOf_sc(NMParams p) // totalcross/sys/Convert native public static int numberOf(String s, char c);
{ 
   TCObject s = p->obj[0];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
   else
   {
      JChar c = (JChar)p->i32[0];
      int32 count = 0;
      int32 len = String_charsLen(s);
      JCharP chars = String_charsStart(s);
      while (--len >= 0)
         if (*chars++ == c)
            count++;
      p->retI = count;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_zeroPad_si(NMParams p) // totalcross/sys/Convert native public static String zeroPad(String s, int size);
{
   int32 size = p->i32[0];
   TCObject s = p->obj[0];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
   else
   {
      int32 len = String_charsLen(s);
      int32 n = size - len;
      if (n > 0)
      {  
         JCharP chars,source = String_charsStart(s);
         TCObject target = createStringObjectWithLen(p->currentContext, size);
         if (target == null)
            return;
         chars = String_charsStart(target);
         while (--n >= 0)
            *chars++ = '0';
         while (--len >= 0)
            *chars++ = *source++;
         setObjectLock(p->retO = target, UNLOCKED);
      }
      else p->retO = s;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_zeroPad_ii(NMParams p) // totalcross/sys/Convert native public static String zeroPad(int s, int size);
{
   CharP buffer;
   IntBuf ib;
   int32 size = p->i32[1], len, n;
   buffer = int2str(p->i32[0], ib);
   len = xstrlen(buffer);
   n = size - len;
   if (n > 0)
   {  
      JCharP chars;
      TCObject target = createStringObjectWithLen(p->currentContext, size);
      if (target == null)
         return;
      chars = String_charsStart(target);
      while (--n >= 0)
         *chars++ = '0';
      while (--len >= 0)
         *chars++ = *buffer++;
      setObjectLock(p->retO = target, UNLOCKED);
   }
   else setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, buffer, len), UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_numberPad_si(NMParams p) // totalcross/sys/Convert native public static String numberPad(String s, int size);
{
   int32 size = p->i32[0];
   TCObject s = p->obj[0];
   if (s == null)
      throwNullArgumentException(p->currentContext, "s");
   else
   {
      int32 len = String_charsLen(s);
      int32 n = size - len;
      if (n > 0)
      {  
         JCharP chars,source = String_charsStart(s);
         TCObject target = createStringObjectWithLen(p->currentContext, size);
         if (target == null)
            return;
         chars = String_charsStart(target);
         while (--n >= 0)
            *chars++ = (char)160;
         while (--len >= 0)
            *chars++ = *source++;
         setObjectLock(p->retO = target, UNLOCKED);
      }
      else p->retO = s;
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_numberPad_ii(NMParams p) // totalcross/sys/Convert native public static String numberPad(int s, int size);
{
   CharP buffer;
   IntBuf ib;
   int32 size = p->i32[1], len, n;
   buffer = int2str(p->i32[0], ib);
   len = xstrlen(buffer);
   n = size - len;
   if (n > 0)
   {  
      JCharP chars;
      TCObject target = createStringObjectWithLen(p->currentContext, size);
      if (target == null)
         return;
      chars = String_charsStart(target);
      while (--n >= 0)
         *chars++ = (char)160;
      while (--len >= 0)
         *chars++ = *buffer++;
      setObjectLock(p->retO = target, UNLOCKED);
   }
   else setObjectLock(p->retO = createStringObjectFromCharP(p->currentContext, buffer, len), UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_dup_ci(NMParams p) // totalcross/sys/Convert native public static String dup(char c, int count);
{
   JChar c = (JChar)p->i32[0], *chars;
   int32 count = p->i32[1];
   TCObject target;
   if (count < 0)
      throwIllegalArgumentExceptionI(p->currentContext, "count",count);
   else
   {
      target = createStringObjectWithLen(p->currentContext, count);
      if (target == null)
         return;
      chars = String_charsStart(target);
      while (--count >= 0)
         *chars++ = c;
      setObjectLock(p->retO = target, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_spacePad_sib(NMParams p) // totalcross/sys/Convert native public static String spacePad(String what, int size, boolean before);
{
   int32 size = p->i32[0];
   bool before = p->i32[1];
   TCObject s = p->obj[0];
   if (s == null)
      throwNullArgumentException(p->currentContext, "what");
   else
   {
      int32 len = String_charsLen(s);
      int32 n = size - len;
      if (n > 0)
      {  
         JCharP chars,source = String_charsStart(s);
         TCObject target = createStringObjectWithLen(p->currentContext, size);
         if (target == null)
            return;
         chars = String_charsStart(target);
         if (before)
            while (--n >= 0)
               *chars++ = ' ';
         while (--len >= 0)
            *chars++ = *source++;
         if (!before)
            while (--n >= 0)
               *chars++ = ' ';
         setObjectLock(p->retO = target, UNLOCKED);
      }
      else p->retO = s;
   }
}

#ifdef ENABLE_TEST_SUITE
#include "Convert_test.h"
#endif
