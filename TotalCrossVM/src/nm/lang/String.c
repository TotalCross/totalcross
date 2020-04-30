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
#include "NativeMethods.h"

//////////////////////////////////////////////////////////////////////////
TC_API void jlS_valueOf_d(NMParams p) // java/lang/String native public static String valueOf(double d);
{
   double d = p->dbl[0];
   DoubleBuf db;
   CharP res = double2str(d, -1, db);
   p->retO = createStringObjectFromCharP(p->currentContext, res,-1);
   if (p->retO)
      setObjectLock(p->retO, UNLOCKED);
}
//////////////////////////////////////////////////////////////////////////
void tsC_toString_c(NMParams p);
void tsC_toString_i(NMParams p);
void tsC_toString_l(NMParams p);

TC_API void jlS_valueOf_c(NMParams p) // java/lang/String native public static String valueOf(char c);
{
   tsC_toString_c(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_valueOf_i(NMParams p) // java/lang/String native public static String valueOf(int i);
{
   tsC_toString_i(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_copyChars_CiCii(NMParams p) // java/lang/String native static boolean copyChars(char []srcArray, int srcStart, char []dstArray, int dstStart, int length);
{
   TCObject srcArray, dstArray;
   int32 srcStart, dstStart, len;
   JCharP srcPtr, dstPtr;

   srcArray = p->obj[0];
   dstArray = p->obj[1];
   srcStart = p->i32[0];
   dstStart = p->i32[1];
   len = p->i32[2];

   if (checkArrayRange(p->currentContext, srcArray, srcStart, len) && checkArrayRange(p->currentContext, dstArray, dstStart, len))
   {
      srcPtr = (JCharP)ARRAYOBJ_START(srcArray);
      dstPtr = (JCharP)ARRAYOBJ_START(dstArray);
      xmemmove(dstPtr + dstStart, srcPtr + srcStart, len << 1);
      p->retI = true;
   }
   else p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_toUpperCase(NMParams p) // java/lang/String native public String toUpperCase();
{
   TCObject fromObj, toObj;
   JCharP from,to;
   int32 len;

   fromObj = p->obj[0];
   from   = String_charsStart(fromObj);
   len    = String_charsLen(fromObj);

   p->retO = toObj = createStringObjectWithLen(p->currentContext, len);
   if (toObj != null)
   {
      to = String_charsStart(toObj);
      // do the convertion
      while (len--)
         *to++ = JCharToUpper(*from++);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_toLowerCase(NMParams p) // java/lang/String native public String toLowerCase();
{
   TCObject fromObj, toObj;
   JCharP from,to;
   int32 len;

   fromObj = p->obj[0];
   from   = String_charsStart(fromObj);
   len    = String_charsLen(fromObj);

   p->retO = toObj = createStringObjectWithLen(p->currentContext, len);
   if (toObj != null)
   {
      to = String_charsStart(toObj);
      // do the convertion
      while (len--)
         *to++ = JCharToLower(*from++);
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_equals_o(NMParams p) // java/lang/String native public boolean equals(Object obj);
{
   TCObject me, other;
   me = p->obj[0];
   other = p->obj[1];

   if (me == other) // same object?
      p->retI = true;
   else
   if (other != null && OBJ_CLASS(me) == OBJ_CLASS(other))
      p->retI = JCharPEqualsJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(other));
   else
      p->retI = false;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_compareTo_s(NMParams p) // java/lang/String native public int compareTo(String s);
{
   TCObject me, other;
   me = p->obj[0];
   other = p->obj[1];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
   if (me == other) // same object?
      p->retI = 0; // match
   else
      p->retI = JCharPCompareToJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(other));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_indexOf_i(NMParams p) // java/lang/String native public int indexOf(int c);
{
   TCObject me = p->obj[0];
   p->retI = JCharPIndexOfJChar(String_charsStart(me), (JChar)p->i32[0], 0, String_charsLen(me));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_indexOf_ii(NMParams p) // java/lang/String native public int indexOf(int c, int startIndex);
{
   TCObject me = p->obj[0];
   p->retI = JCharPIndexOfJChar(String_charsStart(me), (JChar)p->i32[0], p->i32[1], String_charsLen(me));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_indexOf_s(NMParams p) // java/lang/String native public int indexOf(String c);
{
   TCObject me,other;
   me = p->obj[0];
   other = p->obj[1];
   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPIndexOfJCharP(String_charsStart(me), String_charsStart(other), 0, String_charsLen(me), String_charsLen(other));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_indexOf_si(NMParams p) // java/lang/String native public int indexOf(String c, int startIndex);
{
   TCObject me,other;
   me = p->obj[0];
   other = p->obj[1];
   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPIndexOfJCharP(String_charsStart(me), String_charsStart(other), p->i32[0], String_charsLen(me), String_charsLen(other));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_hashCode(NMParams p) // java/lang/String native public int hashCode();
{
   TCObject me = p->obj[0];
   p->retI = JCharPHashCode(String_charsStart(me), String_charsLen(me));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_startsWith_si(NMParams p) // java/lang/String native public boolean startsWith(String prefix, int from);
{
   TCObject me, other;
   int32 from;

   me = p->obj[0];
   other = p->obj[1];
   from = p->i32[0];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPStartsWithJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(other), from);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_startsWith_s(NMParams p) // java/lang/String native public boolean startsWith(String prefix);
{
   TCObject me, other;

   me = p->obj[0];
   other = p->obj[1];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPStartsWithJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(other), 0);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_endsWith_s(NMParams p) // java/lang/String native public boolean endsWith(String suffix);
{
   TCObject me, other;

   me = p->obj[0];
   other = p->obj[1];

   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPEndsWithJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(other));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_equalsIgnoreCase_s(NMParams p) // java/lang/String native public boolean equalsIgnoreCase(String s);
{
   TCObject me, other;

   me = p->obj[0];
   other = p->obj[1];

   p->retI = other == null ? false : JCharPEqualsIgnoreCaseJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(other)); // guich@tc120_45: return false if null
}
//////////////////////////////////////////////////////////////////////////
TCObject S_replace(Context currentContext, TCObject me, JChar oldChar, JChar newChar)
{
   TCObject other, retO = null;
   int32 n;
   JCharP jme, jother;
   bool found = false;
                                     
   // guich@tc115_55: search before replacing it
   n = String_charsLen(me);
   jme = String_charsStart(me);
   while (--n >= 0)
      if (*jme++ == oldChar)
      {
         found = true;
         break;
      }
   if (!found) // if not found, return "this"
      retO = me;
   else
   {
      n = String_charsLen(me);
      retO = other = createStringObjectWithLen(currentContext, n);
      if (other)
      {
         jme = String_charsStart(me);
         jother = String_charsStart(other);
         for (; n-- > 0; jother++, jme++)
            *jother = (*jme == oldChar) ? newChar : *jme;
         setObjectLock(retO, UNLOCKED);
      }
   }
   return retO;
}

TC_API void jlS_replace_cc(NMParams p) // java/lang/String native public String replace(char oldChar, char newChar);
{
   p->retO = S_replace(p->currentContext, p->obj[0], (JChar)p->i32[0], (JChar)p->i32[1]);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_lastIndexOf_ii(NMParams p) // java/lang/String native public int lastIndexOf(int c, int startIndex);
{
   TCObject me;
   int32 c,startIndex;

   me = p->obj[0];
   c = p->i32[0];
   startIndex = p->i32[1];

   p->retI = JCharPLastIndexOfJChar(String_charsStart(me), String_charsLen(me), (JChar)c, startIndex);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_lastIndexOf_i(NMParams p) // java/lang/String native public int lastIndexOf(int c);
{
   TCObject me;
   int32 c,startIndex;

   me = p->obj[0];
   c = p->i32[0];
   startIndex = p->i32[1];

   p->retI = JCharPLastIndexOfJChar(String_charsStart(me), String_charsLen(me), (JChar)c, String_charsLen(me)-1);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_trim(NMParams p) // java/lang/String native public String trim();
{
   TCObject me;
   int32 end,len,st=0;
   JCharP chars;

   me = p->obj[0];
   len = String_charsLen(me);
   end = len-1;

   chars = String_charsStart(me);
   while (st <= end && chars[st] <= ' ')
      st++;
   while (end >= 0 && chars[end] <= ' ')
      end--;

   if (st <= 0 && end >= (len-1))
      p->retO = me;
   else
   {
      if (st > end)
         p->retO = createStringObjectWithLen(p->currentContext, 0);
      else
      {
         p->retO = createStringObjectWithLen(p->currentContext, end-st+1);
         if (p->retO)
            xmemmove(String_charsStart(p->retO), chars+st, (end-st+1)*2);
      }
      if (p->retO)
         setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_valueOf_l(NMParams p) // java/lang/String native public static String valueOf(long l);
{
   tsC_toString_l(p);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_lastIndexOf_s(NMParams p) // java/lang/String native public int lastIndexOf(String s);
{
   TCObject me,other;
   me = p->obj[0];
   other = p->obj[1];
   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPLastIndexOfJCharP(String_charsStart(me), String_charsStart(other), String_charsLen(me), String_charsLen(me), String_charsLen(other));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlS_lastIndexOf_si(NMParams p) // java/lang/String native public int lastIndexOf(String s, int startIndex);
{
   TCObject me,other;
   me = p->obj[0];
   other = p->obj[1];
   if (other == null)
      throwException(p->currentContext, NullPointerException,null);
   else
      p->retI = JCharPLastIndexOfJCharP(String_charsStart(me), String_charsStart(other), p->i32[0], String_charsLen(me), String_charsLen(other));
}
//////////////////////////////////////////////////////////////////////////

TCObject chars2bytes(Context currentContext, JCharP chars, int32 length); // CharacterConverter.c

TC_API void jlS_getBytes(NMParams p) // java/lang/String native public byte []getBytes();
{
   TCObject obj = p->obj[0];
   JChar* chars = String_charsStart(obj);
   int32 length = String_charsLen(obj);
   p->retO = chars2bytes(p->currentContext, chars, length);
}

#ifdef ENABLE_TEST_SUITE
#include "String_test.h"
#endif
