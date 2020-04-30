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

#ifndef JCHAR_H
#define JCHAR_H

#include "../tcvm/tcapi.h"
#include "xtypes.h"

/// In the functions below, if len is < 0, it is computed from the given input (J)CharP.
/// If (J)CharP is NOT terminated with 0, then you MUST provide its length

/// Important: some functions allocate a buffer to place the returned value using xmalloc,
/// to you must use xfree to free that buffer (and NOT freeArray).

/// convert a CharP to a JCharP, allocating the target buffer. Resulting string is 0 terminated.
TC_API JCharP CharP2JCharP(CharP s, int32 len);
typedef JCharP (*CharP2JCharPFunc)(CharP s, int32 len);
/// convert a JCharP to a CharP, allocating the target buffer. Resulting string is 0 terminated.
TC_API CharP JCharP2CharP(JCharP js, int32 len);
typedef CharP (*JCharP2CharPFunc)(JCharP js, int32 len);
/// same as above, but the buffer is given; optionally, do not end the buffer with 0 (used when filling a java.lang.String object)
TC_API JCharP CharP2JCharPBuf(CharP s, int32 len, JCharP buffer, bool endWithZero);
typedef JCharP (*CharP2JCharPBufFunc)(CharP s, int32 len, JCharP buffer, bool endWithZero);
/// same as above, but the buffer is given
TC_API CharP JCharP2CharPBuf(JCharP js, int32 len, CharP buffer);
typedef CharP (*JCharP2CharPBufFunc)(JCharP js, int32 len, CharP buffer);
TC_API int32 JCharPLen(JCharP s);
typedef int32 (*JCharPLenFunc)(JCharP s);
TC_API JChar JCharToLower(JChar c);
typedef JChar (*JCharToLowerFunc)(JChar c);
TC_API JChar JCharToUpper(JChar c);
typedef JChar (*JCharToUpperFunc)(JChar c);
TC_API int32 JCharPIndexOfJCharP(JCharP me, JCharP other, int32 start, int32 meLen, int32 otherLen);
typedef int32 (*JCharPIndexOfJCharPFunc)(JCharP me, JCharP other, int32 start, int32 meLen, int32 otherLen);
TC_API int32 JCharPLastIndexOfJCharP(JCharP me, JCharP other, int32 start, int32 meLen, int32 otherLen);
typedef int32 (*JCharPLastIndexOfJCharPFunc)(JCharP me, JCharP other, int32 start, int32 meLen, int32 otherLen);
TC_API int32 JCharPIndexOfJChar(JCharP me, JChar what, int32 start, int32 meLen);
typedef int32 (*JCharPIndexOfJCharFunc)(JCharP me, JChar what, int32 start, int32 meLen);
/// if meLen or otherLen is < 0, it calls JCharLength to get the length.
TC_API bool JCharPEqualsJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen);
typedef bool (*JCharPEqualsJCharPFunc)(JCharP me, JCharP other, int32 meLen, int32 otherLen);
TC_API int32 JCharPCompareToJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen);
typedef int32 (*JCharPCompareToJCharPFunc)(JCharP me, JCharP other, int32 meLen, int32 otherLen);
TC_API int32 JCharPHashCode(JCharP s, int32 len);
typedef int32 (*JCharPHashCodeFunc)(JCharP s, int32 len);
TC_API bool JCharPStartsWithJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen, int32 from);
typedef bool (*JCharPStartsWithJCharPFunc)(JCharP me, JCharP other, int32 meLen, int32 otherLen, int32 from);
TC_API bool JCharPEndsWithJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen);
typedef bool (*JCharPEndsWithJCharPFunc)(JCharP me, JCharP other, int32 meLen, int32 otherLen);
/// if meLen or otherLen is < 0, it calls JCharLength to get the length.
TC_API bool JCharPEqualsIgnoreCaseJCharP(JCharP me, JCharP other, int32 meLen, int32 otherLen);
typedef bool (*JCharPEqualsIgnoreCaseJCharPFunc)(JCharP me, JCharP other, int32 meLen, int32 otherLen);
TC_API int32 JCharPLastIndexOfJChar(JCharP me, int32 meLen, JChar c, int32 startIndex);
typedef int32 (*JCharPLastIndexOfJCharFunc)(JCharP me, int32 meLen, JChar c, int32 startIndex);
TC_API void JCharPDupBuf(JCharP original, int32 length, JCharP buffer);
typedef void(*JCharPDupBufFunc)(JCharP original, int32 length, JCharP buffer);
TC_API JCharP JCharPDup(JCharP original, int32 length);
typedef JCharP(*JCharPDupFunc)(JCharP original, int32 length);

/// In WinCE, returns "from". In other platforms, calls JCharP2CharPBuf, using an internal buffer of size 255
TC_API TCHARP JCharP2TCHARP(JCharP from, int32 len);
typedef TCHARP (*JCharP2TCHARPFunc)(JCharP from, int32 len);
/// In WinCE, returns "from". In other platforms, calls JCharP2CharPBuf, using the buffer provided
TC_API TCHARP JCharP2TCHARPBuf(JCharP from, int32 len, TCHAR* buf);
typedef TCHARP (*JCharP2TCHARPBufFunc)(JCharP from, int32 len, TCHAR* buf);
/// In WinCE, returns "from". In other platforms, calls CharP2JCharPBuf, using an internal buffer of size 255 and ending with 0.
TC_API JCharP TCHARP2JCharP(TCHAR* from, JCharP to, int32 len);
typedef JCharP (*TCHARP2JCharPFunc)(TCHAR* from, JCharP to, int32 len);

#endif
