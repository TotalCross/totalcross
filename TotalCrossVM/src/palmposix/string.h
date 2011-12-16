/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#ifndef __STRING_H__
#define __STRING_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

#define strlen disabled_strlen
#define strcmp disabled_strcmp
#define strncmp disabled_strncmp
#define strcaselesscmp disabled_strcaselesscmp
#define strcpy disabled_strcpy
#define strncpy disabled_strncpy
#define strcat disabled_strcat
#define strncat disabled_strncat
#define memset disabled_memset
#define memzero disabled_memzero
#define memcpy disabled_memcpy
#define memcmp disabled_memcmp
#define memmove disabled_memmove
#define strchr disabled_strchr
#define strstr disabled_strstr
#define bzero disabled_bzero

#include_next <string.h>

#undef strlen
#undef strcmp
#undef strncmp
#undef strcaselesscmp
#undef strcpy
#undef strncpy
#undef strcat
#undef strncat
#undef memset
#undef memzero
#undef memcpy
#undef memcmp
#undef memmove
#undef strchr
#undef strstr
#undef bzero

#define strlen(s) StrLen(s)
#define strcmp(s1, s2) StrCompareAscii(s1, s2)
#define strncmp(s1, s2, n) StrNCompareAscii(s1, s2, n)
#define strcaselesscmp StrCaselessCompare
#define strcpy StrCopy
#define strncpy(dst, src, n) StrNCopy(dst, src, n)
#define strcat(dst, src) StrCat(dst, src)
#define strncat(dst, src, n) StrNCat(dst, src, n)
#define memcmp(s1, s2, n) MemCmp(s1, s2, n)
#define strchr(s, c) StrChr(s, c)
#define strstr(s1, s2) StrStr(s1, s2)
#define bzero(s, n) MemSet(s, n, 0)

/**
 * The matching PalmOS functions below don't have the same return value, thus
 * if your app uses the return value of one of this call, you have to use the
 * function version rather than the macro version. (just define NO_MEMFUNC_MACROS).
 */
#if !defined(NO_MEMFUNC_MACROS)
#define memset(mem, what, n) MemSet(mem, n, what)
#define memzero(mem, n) MemSet(mem, n, 0)
#define memmove(dst, src, n) MemMove(dst, src, n)
#else
extern void *memset (void *s, int c, size_t n);
extern void *memzero (void *s, size_t n);
extern void *memmove (void *dst, const void *src, size_t n);
#endif

extern void *memcpy(void *dst, const void *src, size_t n);
extern void *memchr(const void *s, int c, size_t n);

#endif //  __STRING_H__
