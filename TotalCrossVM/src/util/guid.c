// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "guid.h"

TC_API void GUID2TCHARP(GUID guid, TCHARP szGuid)
{
   int32 i;
   char guidBuf[MAX_GUID_STRING_LEN];

   int2hex(guid.Data1, 8, guidBuf+1);
   int2hex(guid.Data2, 4, guidBuf+10);
   int2hex(guid.Data3, 4, guidBuf+15);
   int2hex((int32) guid.Data4[0], 2, guidBuf+20);
   int2hex((int32) guid.Data4[1], 2, guidBuf+22);
   for (i = 2 ; i < 8 ; i++)
      int2hex((int32) guid.Data4[i], 2, guidBuf+21+(i*2));

   *guidBuf = '{';
   *(guidBuf+9) = *(guidBuf+14) = *(guidBuf+19) = *(guidBuf+24) = '-';
   *(guidBuf+37) = '}';
   *(guidBuf+38) = 0;
   CharP2TCHARPBuf(guidBuf, szGuid);
}

TC_API bool TCHARP2GUID(TCHARP szGuid, GUID* guid)
{
   int32 i, offset, longHex;
   char guidBuf[MAX_GUID_STRING_LEN];

   TCHARP2CharPBuf(szGuid, guidBuf);
   *(guidBuf+9) = *(guidBuf+14) = *(guidBuf+19) = *(guidBuf+24) = *(guidBuf+37) = 0;

   if (!radix2int(guidBuf+1, 16, &longHex))
      return false;
   guid->Data1 = (uint32) longHex;
   if (!radix2int(guidBuf+10, 16, &longHex))
      return false;
   guid->Data2 = (uint16) longHex;
   if (!radix2int(guidBuf+15, 16, &longHex))
      return false;
   guid->Data3 = (uint16) longHex;

   for (i = 1 ; i >= 0 ; i--)
   {
      offset = 20 + (2*i);
      if (!radix2int(guidBuf+offset, 16, &longHex))
         return false;
      guid->Data4[i] = (uint8) longHex;
      *(guidBuf+offset) = 0;
   }

   for (i = 7 ; i >= 2 ; i--)
   {
      offset = 21 + (2*i);
      if (!radix2int(guidBuf+offset, 16, &longHex))
         return false;
      guid->Data4[i] = (uint8) longHex;
      *(guidBuf+offset) = 0;
   }
   return true;
}

TC_API bool String2GUID(TCObject string, GUID* guid)
{
   int32 stringLen = String_charsLen(string);
   TCHAR szGuid[MAX_GUID_STRING_LEN] = GuidZero;

   if (stringLen == 32)
   {
      JCharP2TCHARPBuf(String_charsStart(string)      ,  8, szGuid +  1);
      JCharP2TCHARPBuf(String_charsStart(string) + 8  ,  4, szGuid + 10);
      JCharP2TCHARPBuf(String_charsStart(string) + 12 ,  4, szGuid + 15);
      JCharP2TCHARPBuf(String_charsStart(string) + 16 ,  4, szGuid + 20);
      JCharP2TCHARPBuf(String_charsStart(string) + 20 , 12, szGuid + 25);
      *(szGuid+9) = *(szGuid+14) = *(szGuid+19) = *(szGuid+24) = '-';
      *(szGuid+37) = '}';
   }
   else if (stringLen == 39)
      String2TCHARPBuf(string, szGuid);
   else
      return false;

   return TCHARP2GUID(szGuid, guid);
}
