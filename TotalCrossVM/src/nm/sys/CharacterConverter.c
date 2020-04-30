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

TCObject iso88591bytes2chars(Context currentContext, uint8* bytes, int32 length)
{
   JCharP chars;
   TCObject charArray = createCharArray(currentContext, length);
   if (charArray)
   {
      chars = (JCharP)ARRAYOBJ_START(charArray);
      while (length-- > 0)
         *chars++ = *bytes++;
      setObjectLock(charArray, UNLOCKED);
   }
   return charArray;
}

TCObject iso88591chars2bytes(Context currentContext, JCharP chars, int32 length)
{
   TCObject byteArray = createByteArray(currentContext, length);
   if (byteArray != null)
   {
      JChar* end = chars+length-1;
      int32 skip = 0;
      uint8* bytes = ARRAYOBJ_START(byteArray);
      while (chars <= end)
      {
         JChar c = *chars++;
         if (c <= 255) // guich@tc123_42: '\377' (octal) is 255 (decimal)
            *bytes++ = (uint8)c;
         else
         {
            if (0xD800 <= c && c <= 0xDBFF && chars < end) // invalid range? skip one byte
            {
               skip++;
               chars++;
            }
            *bytes++ = '?';
         }
      }
      if (skip != 0) // just adjust the final length - this will waste memory but will prevent fragmentation
         ARRAYOBJ_LEN(byteArray) -= skip;
      setObjectLock(byteArray, UNLOCKED);
   }
   return byteArray;
}

TCObject utf8bytes2chars(Context currentContext, uint8* bytes, int32 length)
{
   TCObject charArray = createCharArray(currentContext, length); // upper bound
   if (charArray)
   {
      JCharP chars = (JCharP)ARRAYOBJ_START(charArray), chars0 = chars;
      int32 start = 0, end = length, resultingLength;
      int32 c0, c, r;

      while (start < end)
      {
         c0 = bytes[start++] & 0xFF;
         if (c0 < 0x80)                          // if a 1 byte sequence,
         {
            *chars++ = (JChar)c0;                // set the value
            continue;                            // success.
         }
         if (start >= end)                       // If no byte follows,
         {
            *chars++ = '?';                      // set MCS
            break;                               // done
         }
         c = (bytes[start++] & 0xFF) ^ 0x80; // 2nd byte
         if ((c & 0xC0) != 0)                    // starts new sequence?
         {
            --start;                             // Yes, backup
            *chars++ = '?';                      // set MCS
            continue;                            // pursue
         }
         r = (c0 << 6) | c;                  // Get encoded value
         if ((c0 & 0xE0) == 0xC0)                // 2 bytes sequence?
         {
            *chars++ = (JChar)(r & 0x7FF);       // Yes.  Cut noise
            continue;                            // pursue
         }
         if (start >= end)                       // If no byte follows,
         {
            *chars++ = '?';                      // set MCS
            break;                               // done
         }
         c = (bytes[start++] & 0xFF) ^ 0x80;     // 3rd byte
         if ((c & 0xC0) != 0)                    // starts new sequence?
         {
            --start;                             // Yes, backup
            *chars++ = '?';                      // set MCS
            continue;                            // pursue
         }
         *chars++ = (JChar)((r << 6) | c); // Get encoded value
      }
      resultingLength = (int32)(chars - chars0);
      if (resultingLength != length) // just adjust the final length - this will waste memory but will prevent fragmentation
         ARRAYOBJ_LEN(charArray) = resultingLength;
      setObjectLock(charArray, UNLOCKED);
   }
   return charArray;
}

int32 utf8len(JCharP chars, int32 length)
{
   int r = 0;
   while (length-- > 0)
   {
      JChar c = *chars++;
      if (c < 0x80)
         r++;
      else
      if (c < 0x800)
         r += 2;
      else
         r += 3;
   }
   return r;
}

void utf8chars2bytesBuf(JCharP chars, int32 length, uint8* bytes)
{
   JCharP end = chars + length;
   while (chars < end)
   {
      int32 r = *chars++;
      if (r < 0x80)                       // 1 byte sequence
         *bytes++ = (uint8)r;
      else
      if (r < 0x800)                      // 2 bytes sequence?
      {
         *bytes++ = (uint8)(0xC0 | (r >> 6));
         *bytes++ = (uint8)(0x80 | (r & 0x3F));
      }
      else                                     // 3 bytes sequence.
      {
         *bytes++ = (uint8)(0xE0 | (r >> 12));
         *bytes++ = (uint8)(0x80 | ((r >> 6) & 0x3F));
         *bytes++ = (uint8)(0x80 | (r & 0x3F));
      }
   }
}
TCObject utf8chars2bytes(Context currentContext, JCharP chars, int32 length)
{
   TCObject byteArray = createByteArray(currentContext, utf8len(chars, length)); // find the exact length, since it can be max 3*length
   if (byteArray != null)
   {
      uint8* bytes = ARRAYOBJ_START(byteArray);
      utf8chars2bytesBuf(chars, length, bytes);      
      setObjectLock(byteArray, UNLOCKED);
   }
   return byteArray;
}

///////////////
static Method chars2bytesMtd;
static TCObject lastCharConverter;

TCObject chars2bytes(Context currentContext, JCharP chars, int32 length)
{
   if (OBJ_CLASS(*charConverterPtr) == ISO88591CharacterConverter)
      return iso88591chars2bytes(currentContext, chars, length);
   if (OBJ_CLASS(*charConverterPtr) == UTF8CharacterConverter)
      return utf8chars2bytes(currentContext, chars, length);
   if (*charConverterPtr != lastCharConverter)
   {
      chars2bytesMtd = getMethod(OBJ_CLASS(*charConverterPtr), true, "chars2bytes", 3, CHAR_ARRAY, J_INT, J_INT);
      if (chars2bytesMtd == null)
         return null;
      lastCharConverter = *charConverterPtr;
   }
   return executeMethod(currentContext, chars2bytesMtd, *charConverterPtr, chars, 0, length).asObj;
}


//////////////////////////////////////////////////////////////////////////
TC_API void tsCC_bytes2chars_Bii(NMParams p) // totalcross/sys/CharacterConverter native public char[]bytes2chars(byte []bytes, int offset, int length);
{
   TCObject bytes = p->obj[1];
   int32 offset = p->i32[0];
   int32 len = p->i32[1];
   if (checkArrayRange(p->currentContext, bytes, offset, len))
      p->retO = iso88591bytes2chars(p->currentContext, ((uint8*)ARRAYOBJ_START(bytes))+offset, len);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsCC_chars2bytes_Cii(NMParams p) // totalcross/sys/CharacterConverter native public byte[] chars2bytes(char []chars, int offset, int length);
{
   TCObject chars = p->obj[1];
   int32 offset = p->i32[0];
   int32 len = p->i32[1];
   if (checkArrayRange(p->currentContext, chars, offset, len))
      p->retO = iso88591chars2bytes(p->currentContext, ((JCharP)ARRAYOBJ_START(chars))+offset, len);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsUTF8CC_bytes2chars_Bii(NMParams p) // totalcross/sys/UTF8CharacterConverter native public char[]bytes2chars(byte []bytes, int offset, int length);
{
   TCObject bytes = p->obj[1];
   int32 offset = p->i32[0];
   int32 len = p->i32[1];
   if (checkArrayRange(p->currentContext, bytes, offset, len))
      p->retO = utf8bytes2chars(p->currentContext, ((uint8*)ARRAYOBJ_START(bytes))+offset, len);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tsUTF8CC_chars2bytes_Cii(NMParams p) // totalcross/sys/UTF8CharacterConverter native public byte[] chars2bytes(char []chars, int offset, int length);
{
   TCObject chars = p->obj[1];
   int32 offset = p->i32[0];
   int32 len = p->i32[1];
   if (checkArrayRange(p->currentContext, chars, offset, len))
      p->retO = utf8chars2bytes(p->currentContext, ((JCharP)ARRAYOBJ_START(chars))+offset, len);
}
