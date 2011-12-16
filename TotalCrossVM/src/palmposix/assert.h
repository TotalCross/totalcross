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



#ifndef __ASSERT_H__
#define __ASSERT_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

#define assert(e)    if (!(e)) ErrDisplayFileLineMsg(__FILE__, __LINE__, #e);

#if ERROR_CHECK_LEVEL >= ERROR_CHECK_PARTIAL

void fatal_custom (char *buffer, const char *filename, UInt16 lineNo, const char *format, ...);
void fatal        (const char *filename, UInt16 lineNo, const char *format, ...);

#else

#define fatal_custom(...)
#define fatal(...)

#endif

#endif //__ASSERT_H__
