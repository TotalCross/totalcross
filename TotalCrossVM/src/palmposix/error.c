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

// $Id: error.c,v 1.8 2011-01-04 13:31:15 guich Exp $

#if HAVE_CONFIG_H
#include "config.h"
#endif

#include "error.h"

int *__errno()
{
   static int _errno;
   return &_errno;
}

#if ERROR_CHECK_LEVEL >= ERROR_CHECK_PARTIAL

void ErrDisplayF(const char *buffer, const char *format, ...)
{
#ifdef HAVE_STDARG_H
   va_list va;
   va_start(va, format);
   StrVPrintF((char*)buffer, format, va);
   va_end(va);
   ErrDisplay(buffer);
#endif
}

void ErrDisplay256F(const char *format, ...)
{
#ifdef HAVE_STDARG_H
   char buffer[256];
   va_list va;
   va_start(va, format);
   StrVPrintF(buffer, format, va);
   va_end(va);
   ErrDisplay(buffer);
#endif
}

#endif // ERROR_CHECK_LEVEL
