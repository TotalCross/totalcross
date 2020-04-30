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

#ifndef UTILS_H
#define UTILS_H
#ifdef __cplusplus
 extern "C" {
#endif

TC_API void getDateTime(int32* year, int32* month, int32* day, int32* hour, int32* minute, int32* second, int32* millis);
typedef void (*getDateTimeFunc)(int32* year, int32* month, int32* day, int32* hour, int32* minute, int32* second, int32* millis);

double dmod(double c1, double c2);
int32 toBaseAsDecimal(int32 num, int32 baseFrom, int32 baseTo); // converts a number to octal

TC_API int32 hashCode(CharP s);
typedef int32 (*hashCodeFunc)(CharP s);
int32 hashCodeSlash2Dot(CharP s);

TC_API int32 hashCodeFmt(CharP fmt, ...); // Computes the hashcode of the given variable number of types. Allowed types passed in fmt: s (CharP) S (JCharP) x (pointer) i (int32) l (int64), v (CharP converting to lowercase). Ex: int32 hash = hashCodeFmt("sii", "Barbara", 30, anInt32variable);
typedef int32 (*hashCodeFmtFunc)(CharP fmt, ...);

TC_API void int2hex(int32 b, int32 places, CharP outBuf);
typedef void (*int2hexFunc)(int32 b, int32 places, CharP outBuf);
void long2hex(int64 b, int32 places, CharP outBuf);
bool radix2int(CharP hex, int32 radix, int32* result);
bool radix2long(CharP str, int32 radix, int64* result);
extern int32 min32(int32 i1, int32 i2);
extern int32 max32(int32 i1, int32 i2);

/// simple case convertions: only supports a-z/A-Z
TC_API char toLower(char c);
typedef char (*toLowerFunc)(char c);
TC_API char toUpper(char c);
typedef char (*toUpperFunc)(char c);
TC_API void CharPToUpper(CharP c); // replace the chars in the buffer
typedef void (*CharPToUpperFunc)(CharP c); // replace the chars in the buffer
TC_API void CharPToLower(CharP c);
typedef void (*CharPToLowerFunc)(CharP c);

/// Read a LE int32 at the current file pointer
int32 fread32(FILE* f);
/// Read a LE int16 at the current file pointer
int16 fread16(FILE* f);
/// Search and find a file in the most common paths. Name should be the filename only, should not contain absolute paths. If pathOut is not null, it should point to a buffer where the full path will be stored
FILE* findFile(CharP name, CharP pathOut);

#define LF_NONE      0
#define LF_RECURSIVE 1
/// List all files on the given folder. In Palm OS, if slot is 0, searches in main memory, otherwise NVFS is used.
/// If (options & LF_RECURSIVE) is true, all folders from the given one are searched, and the path is stored with the file's name; otherwise, only the
/// file name is stored in the list.
/// <b>Important: count and list must be initialized to 0 before this function is called!</b>
/// options can be a combination of LF_RECURSIVE
TC_API Err listFiles(TCHARP path, int32 slot, TCHARPs** list, int32* count, Heap h, int32 options);
typedef Err (*listFilesFunc)(TCHARP path, int32 slot, TCHARPs** list, int32* count, Heap h, int32 options);

/// Convert a CharP to a primitive type. 'err': if not null, returns true if an error occurs (must be set to false before calling)
TC_API int32 str2int(CharP str, bool *err);
typedef int32 (*str2intFunc)(CharP str, bool *err);
/// Convert a CharP to a primitive type. 'err': if not null, returns true if an error occurs (must be set to false before calling)
TC_API double str2double(CharP str, bool *err);
typedef double (*str2doubleFunc)(CharP str, bool *err);
/// Convert a CharP to a primitive type. 'err': if not null, returns true if an error occurs (must be set to false before calling)
TC_API int64 str2long(CharP str, bool *err);
typedef int64 (*str2longFunc)(CharP str, bool *err);

/// Convert an int32 to a CRID
TC_API CharP int2CRID(int32 i, CharP crid);
typedef CharP (*int2CRIDFunc)(int32 i, CharP crid);

/// duplicates a string allocating from a Heap
TC_API CharP hstrdup(CharP s, Heap h);
typedef CharP (*hstrdupFunc)(CharP s, Heap h);

/// Replaces the 'from' char to the 'to' char in the buffer.
void replaceChar(CharP s, char from, char to);

// Convert a primitive type to a CharP
// The result is stored in a static buffer inside these functions.
// Calling it twice will destroy the convertion resulted from first call.
// You must declare a local variable to store the resulting buffer, according
// with the type.
// The result is always stored inside buf; but the returned pointer may
// be INSIDE the buffer (the convertion moves to the end of the buffer and
// goes backwards
typedef char IntBuf[12];
TC_API CharP int2str(int32 i, IntBuf buf);
typedef CharP (*int2strFunc)(int32 i, IntBuf buf);
/**
 * Length of the longest printable double:
 * int max_digits = 3 + DBL_MANT_DIG - DBL_MIN_EXP
 * For a 64-bit IEEE double, we have
 * DBL_MANT_DIG = 53
 * DBL_MIN_EXP = -1023
 * max_digits = 3 + 53 - (-1023) = 1079
 * 
 * We should set DoubleBuf to 1079 to support ANY double value.
 */
typedef char DoubleBuf[1080];
TC_API CharP double2str(double val, int32 places, DoubleBuf buf);
typedef CharP (*double2strFunc)(double val, int32 places, DoubleBuf buf);
typedef char LongBuf[24];
TC_API CharP long2str(int64 i, LongBuf buf);
typedef CharP (*long2strFunc)(int64 i, LongBuf buf);

/// Pauses the program for the given number of milliseconds
#ifndef WIN32
TC_API void Sleep(uint32 ms);
typedef void (*SleepFunc)(uint32 ms);
#endif
/// Returns the amount of free memory. Maximum is 2GB. If maxblock is true, it return the biggest contiguous block size available
TC_API int32 getFreeMemory(bool maxblock);
typedef int32 (*getFreeMemoryFunc)(bool maxblock);
/// Returns a time stamp
TC_API int32 getTimeStamp();
typedef int32 (*getTimeStampFunc)();
/// Normalizes the path, replacing backslahes with slashes.
TC_API void normalizePath(TCHARP path);
typedef void (*normalizePathFunc)(TCHARP path);

// Converts a CharP to a TCHAR* and vice-versa
// In WinCE, an internal buffer of 255 TCHAR is used to convert the value.
// Be careful not to hold the result for a long period, since calling this function
// again will destroy the first result.
// In all other platforms, no buffer is used and the function just returns "from",
// since no convertion is needed (TCHAR = char)
TC_API TCHARP CharP2TCHARP(CharP from);
typedef TCHARP (*CharP2TCHARPFunc)(CharP from);
TC_API TCHARP CharP2TCHARPBuf(CharP from, TCHARP to);
typedef TCHARP (*CharP2TCHARPBufFunc)(CharP from, TCHARP to);
TC_API CharP TCHARP2CharPBuf(TCHARP from, CharP to);
typedef CharP (*TCHARP2CharPBufFunc)(TCHARP js, CharP buffer);

/// Handy functions to convert a Java String into a char* and TCHAR*
#define String2CharP(strObj) JCharP2CharP(String_charsStart(strObj), String_charsLen(strObj))
#define String2CharPBuf(strObj, buf) JCharP2CharPBuf(String_charsStart(strObj), String_charsLen(strObj), buf)
#define String2TCHARP(strObj) JCharP2TCHARP(String_charsStart(strObj), String_charsLen(strObj))
#define String2TCHARPBuf(strObj, buf) JCharP2TCHARPBuf(String_charsStart(strObj), String_charsLen(strObj), buf)

#ifdef WINCE
   #define strcpyTCHAR lstrcpy
   #define strcatTCHAR lstrcat
   #define strlenTCHAR lstrlen
#else
   #define strcpyTCHAR xstrcpy
   #define strcatTCHAR xstrcat
   #define strlenTCHAR xstrlen
#endif

#ifdef ANDROID
void jstring2CharP(jstring src, char* dest);
void jstring2CharPEnv(jstring src, char* dest, JNIEnv* env);
#endif

#ifdef __cplusplus
 } // __cplusplus
#endif

#endif
