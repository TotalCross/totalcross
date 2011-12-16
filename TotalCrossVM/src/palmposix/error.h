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



#ifndef __ERROR_H__
#define __ERROR_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

#ifdef HAVE_STDARG_H
#include <stdarg.h>
#endif

int *__errno();

#if ERROR_CHECK_LEVEL >= ERROR_CHECK_PARTIAL

void ErrDisplayF     (const char *buffer, const char *format, ...);
void ErrDisplay256F  (const char *format, ...);

#else

#define ErrDisplayF(...)
#define ErrDisplay256F(...)

#endif

#endif //__ERROR_H__
