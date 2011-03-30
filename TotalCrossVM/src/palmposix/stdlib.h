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



#ifndef __STDLIB_H__
#define __STDLIB_H__

#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#endif

#define malloc disabled_malloc
#define free disabled_free
#define realloc disabled_realloc

#include_next <stdlib.h>

#undef malloc
#undef free
#undef realloc

char *getenv(const char *name);

#define calloc(n,s) malloc(n*s)
#define srand(s)
#define rand()    ((UInt32)TimGetTicks())

#define abort()  exit(0)
//#define exit(e) { FrmCloseAllForms(); return 0; }

void    *malloc     (UInt32 size);
void     free       (void *p);
void    *realloc    (void *p, UInt32 newSize);

UInt32   freeMemInfo();

void exit (int status);

#define  atoi(x)     StrAToI(x)
#define  atol(x)     StrAToI(x)

#endif //  __STDLIB_H__
