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



#if HAVE_CONFIG_H
#include "config.h"
#endif

#ifdef HAVE_STDARG_H
#include <stdarg.h>
#endif

#include <assert.h>
#include <limits.h>

#if ERROR_CHECK_LEVEL >= ERROR_CHECK_PARTIAL

void fatal_custom(char *buffer, const char *filename, UInt16 lineNo, const char *format, ...)
{
#ifdef HAVE_STDARG_H
   va_list va;
   va_start(va, format);
   StrVPrintF(buffer, format, va);
   va_end(va);
#else
   StrPrintF(buffer, "Unknown error at %s:%d", filename, lineNo);
#endif
   ErrDisplayFileLineMsg(filename, lineNo, buffer);
}

void fatal(const char *filename, UInt16 lineNo, const char *format, ...)
{
   char buffer[CONSOLE_MAXLINE];
#ifdef HAVE_STDARG_H
   va_list va;
   va_start(va, format);
   StrVPrintF(buffer, format, va);
   va_end(va);
#else
   StrPrintF(buffer, "Unknown error at %s:%d", filename, lineNo);
#endif
   ErrDisplayFileLineMsg(filename, lineNo, buffer);
}

#endif
