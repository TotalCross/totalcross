/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: string_posix.c,v 1.7 2011-01-04 13:31:15 guich Exp $

#if HAVE_CONFIG_H
#include "config.h"
#endif

#include "stdio.h"
#include "string.h"

#if defined(NO_MEMFUNC_MACROS)

void *memset (void *s, int c, size_t n)
{
   MemSet(s, n, c);
   return s;
}

void *memzero (void *s, size_t n)
{
   MemSet(s, n, 0);
   return s;
}

void *memmove (void *dst, const void *src, size_t n)
{
    MemMove(dst, src, n);
    return dst;
}

#endif // NO_MEMFUNC_MACROS

void* memcpy(void *dst, const void *src, size_t n)
{
    MemMove(dst, src, n);
    return dst;
}

void *memchr(const void *s, int c, size_t n)
{
   if (n != 0)
   {
      const unsigned char *p = s;
      do
      {
         if (*p++ == (unsigned char)c)
            return ((void *)(p - 1));
      }
      while (--n != 0);
   }
   return NULL;
}
