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

#ifndef GUID_H
#define GUID_H

#include "tcvm.h"

#ifndef GUID_DEFINED
 #define GUID_DEFINED
   typedef struct _GUID
   {
      uint32   Data1;
      uint16   Data2;
      uint16   Data3;
      uint8    Data4[8];
   } GUID;
#endif // !defined(GUID_DEFINED)

#ifndef MAX_GUID_STRING_LEN
 #define MAX_GUID_STRING_LEN 39
#endif

#define GuidZeroA "{00000000-0000-0000-0000-000000000000}"
#define GuidZeroW TEXT(GuidZeroA)
#ifdef UNICODE
 #define GuidZero GuidZeroW
#else
 #define GuidZero GuidZeroA
#endif 

TC_API void GUID2TCHARP(GUID guid, TCHARP szGuid);
typedef void (*GUID2TCHARPFunc)(GUID guid, TCHARP szGuid);
TC_API bool TCHARP2GUID(TCHARP szGuid, GUID* guid);
typedef bool (*TCHARP2GUIDFunc)(TCHARP szGuid, GUID* guid);
TC_API bool String2GUID(TCObject string, GUID* guid);
typedef bool (*String2GUIDFunc)(TCObject string, GUID* guid);

#endif
