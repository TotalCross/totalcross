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

static bool ensureCapacity(Context currentContext, TCObject obj, int32 minimumCapacity)
{
   TCObject charArrayObjSrc, charArrayObjDest;
   JCharP srcPtr,destPtr;
   int32 len, maxCapacity, newCapacity;

   charArrayObjSrc = StringBuffer_chars(obj);
   if (!charArrayObjSrc)
      return false;
   len = StringBuffer_count(obj);
   maxCapacity = ARRAYOBJ_LEN(charArrayObjSrc);
   if (max32(minimumCapacity,len) <= maxCapacity)
      return true;

   newCapacity = (maxCapacity + 1) * 3 / 2; // grow at 50%
   if (minimumCapacity > newCapacity)
      newCapacity = minimumCapacity;

   // create a new object to store the desired capacity
   charArrayObjDest = createCharArray(currentContext, newCapacity);
   if (charArrayObjDest == null)
      return false;
   // copy the current contents
   destPtr = (JCharP)ARRAYOBJ_START(charArrayObjDest);
   srcPtr  = (JCharP)ARRAYOBJ_START(charArrayObjSrc);
   xmemmove(destPtr, srcPtr, len << 1);
   StringBuffer_chars(obj) = charArrayObjDest; // replace original one
   setObjectLock(charArrayObjDest, UNLOCKED);
   return true;
}

TC_API TCObject appendJCharP(Context currentContext, TCObject obj, JCharP srcPtr, int32 len)
{
   int32 count, bufferLen;
   JCharP destPtr;

   count = StringBuffer_count(obj);
   bufferLen = ARRAYOBJ_LEN(StringBuffer_chars(obj));

   if ((count + len) > bufferLen && !ensureCapacity(currentContext, obj, count + len)) // need to increase buffer?
      return null;

   // append
   destPtr = ((JCharP)StringBuffer_charsStart(obj)) + count; // don't cache bc the array may change in ensureCapacity
   StringBuffer_count(obj) += len;
   xmemmove(destPtr, srcPtr, len << 1);
   return obj;
}

TC_API TCObject appendCharP(Context currentContext, TCObject obj, CharP srcPtr)
{
   int32 count, bufferLen, len;
   JCharP destPtr;
   unsigned char *usrcPtr = (unsigned char*)srcPtr; // get rid of the sign

   len = xstrlen(srcPtr);
   count = StringBuffer_count(obj);
   bufferLen = ARRAYOBJ_LEN(StringBuffer_chars(obj));

   if ((count + len) > bufferLen && !ensureCapacity(currentContext, obj, count + len)) // need to increase buffer?
      return null;

   // append
   destPtr = ((JCharP)StringBuffer_charsStart(obj)) + count; // don't cache bc the array may change in ensureCapacity
   StringBuffer_count(obj) += len;
   while (--len >= 0)
      *destPtr++ = *usrcPtr++;
   return obj;
}

void SB_delete(TCObject obj, int32 start, int32 end)
{
   int32 count = StringBuffer_count(obj);

	if (start < 0)
	   start = 0;
  	if (end > count)
	   end = count;
	if (start > end)
	   count = 0;
	else
   {
      int32 len = end - start;
      if (len > 0)
      {
         JCharP ptr = StringBuffer_charsStart(obj);
         xmemmove(ptr + start, ptr + (start+len), (count-end) << 1);
         StringBuffer_count(obj) -= len;
      }
   }
}
void SB_insert(Context currentContext, TCObject obj, JCharP what, int32 pos, int32 wlen)
{
   int32 remain = StringBuffer_count(obj) - pos;
   // first we append it
   obj = appendJCharP(currentContext, obj, what, wlen);
   if (obj != null && remain > 0)
   {
      // now we insert the value, shifting the buffer at the insert position
      JCharP buf = StringBuffer_charsStart(obj);
      xmemmove(buf+pos+wlen, buf+pos, remain*2);
      xmemmove(buf+pos, what, wlen*2);
   }
}

//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_ensureCapacity_i(NMParams p) // java/lang/StringBuffer native public void ensureCapacity(int minimumCapacity);
{
   TCObject obj = p->obj[0];
   int32 minimumCapacity = p->i32[0];
   ensureCapacity(p->currentContext, obj, minimumCapacity);
   p->retO = obj;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_setLength_i(NMParams p) // java/lang/StringBuffer native public void setLength(int newLength);
{
   TCObject obj = p->obj[0]; // class object
   int32 newLength = p->i32[0];
   int32 remain = StringBuffer_count(obj);

   if (newLength < 0)
      newLength = 0;

   remain -= newLength; // how much chars remain in the array?
   if (remain >= 0 || ensureCapacity(p->currentContext, obj, newLength))
   {
      // zero the unused memory region
      if (remain > 0)
      {
         JCharP c = StringBuffer_charsStart(obj);
         xmemzero(&c[newLength], remain << 1);
      }
      StringBuffer_count(obj) = newLength;
   }
   p->retO = obj;
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_s(NMParams p) // java/lang/StringBuffer native public StringBuffer append(String str);
{
   TCObject obj = p->obj[0]; // class object
   TCObject str = p->obj[1];
   if (str == null)
      p->retO = appendCharP(p->currentContext, obj, "null");
   else
      p->retO = appendJCharP(p->currentContext, obj, String_charsStart(str), String_charsLen(str));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_C(NMParams p) // java/lang/StringBuffer native public StringBuffer append(char []str);
{
   TCObject obj = p->obj[0]; // class object
   TCObject str = p->obj[1];
   p->retO = appendJCharP(p->currentContext, obj, (JCharP)ARRAYOBJ_START(str), ARRAYOBJ_LEN(str));
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_Cii(NMParams p) // java/lang/StringBuffer native public StringBuffer append(char []str, int offset, int len);
{
   TCObject obj = p->obj[0]; // class object
   TCObject str = p->obj[1];
   int32 offset = p->i32[0];
   int32 len = p->i32[1];
   if (checkArrayRange(p->currentContext, str, offset, len))
   {
      JCharP ptr = (JCharP)ARRAYOBJ_START(str);
      p->retO = appendJCharP(p->currentContext, obj, ptr + offset, len);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_c(NMParams p) // java/lang/StringBuffer native public StringBuffer append(char c);
{
   TCObject obj = p->obj[0]; // class object
   JChar c = (JChar)p->i32[0];
   p->retO = appendJCharP(p->currentContext, obj, &c, 1);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_i(NMParams p) // java/lang/StringBuffer native public StringBuffer append(int i);
{
   TCObject obj = p->obj[0]; // class object
   int32 i = p->i32[0];
   IntBuf ib;
   CharP ret = int2str(i,ib);
   p->retO = appendCharP(p->currentContext, obj, ret);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_d(NMParams p) // java/lang/StringBuffer native public StringBuffer append(double d);
{
   TCObject obj = p->obj[0]; // class object
   double d = p->dbl[0];
   DoubleBuf db;
   CharP ret = double2str(d, -1, db);
   p->retO = appendCharP(p->currentContext, obj, ret);
}
//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_append_l(NMParams p) // java/lang/StringBuffer native public StringBuffer append(long l)
{
   TCObject obj = p->obj[0]; // class object
   int64 i = p->i64[0];
   LongBuf lb;
   CharP ret = long2str(i, lb);
   p->retO = appendCharP(p->currentContext, obj, ret);
}
//////////////////////////////////////////////////////////////////////////
TC_API void SB_insertAt_sic(NMParams p) // totalcross/sys/Convert native public static void insertAt(StringBuffer sb, int pos, char c)
{
   TCObject obj = p->obj[0], sb;
   int32 pos = p->i32[0];
   JChar c = (JChar)p->i32[1];
   if (obj == null)
      throwNullArgumentException(p->currentContext, "sb");
   else
   if (pos > StringBuffer_count(obj))
      throwException(p->currentContext, ArrayIndexOutOfBoundsException, "Cannot insert beyond position: %d > %d",pos, StringBuffer_count(obj));
   else
   if (pos < 0)
      throwException(p->currentContext, ArrayIndexOutOfBoundsException, "Cannot insert below zero: %d",pos);
   else
   {
      int32 remain = StringBuffer_count(obj) - pos;
      // first we append it
      sb = appendJCharP(p->currentContext, obj, &c, 1);
      if (sb != null && remain > 0)
      {
         // now we insert the value, shifting the buffer at the insert position
         JCharP buf = StringBuffer_charsStart(obj);
         xmemmove(buf+pos+1, buf+pos, remain*2);
         buf[pos] = c;
      }
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsC_append_sci(NMParams p) // totalcross/sys/Convert native public static void append(StringBuffer sb, char c, int count);
{
   TCObject obj = p->obj[0];
   int32 count = p->i32[1];
   JChar c = (JChar)p->i32[0];
   if (obj == null)
      throwNullArgumentException(p->currentContext, "sb");
   else
   if (count < 0)
      throwException(p->currentContext, ArrayIndexOutOfBoundsException, "Count must be >= 0: %d",count);
   else
   {
      int32 bufferLen, sbcount;
      JCharP destPtr;

      sbcount = StringBuffer_count(obj);
      bufferLen = ARRAYOBJ_LEN(StringBuffer_chars(obj));

      if ((sbcount + count) > bufferLen && !ensureCapacity(p->currentContext, obj, sbcount + count)) // need to increase buffer?
         return;

      // append
      destPtr = ((JCharP)StringBuffer_charsStart(obj)) + sbcount; // don't cache bc the array may change in ensureCapacity
      StringBuffer_count(obj) += count;
      while (--count >= 0)
         *destPtr++ = c;
   }
}

//////////////////////////////////////////////////////////////////////////
TC_API void jlSB_delete_ii(NMParams p) // java/lang/StringBuffer native public StringBuffer delete(int start, int end);
{
   p->retO = p->obj[0];
   SB_delete(p->obj[0], p->i32[0], p->i32[1]);
}

#ifdef ENABLE_TEST_SUITE
#include "StringBuffer_test.h"
#endif
