/*
 * jinclude.h
 *
 * Copyright (C) 1991-1994, Thomas G. Lane.
 * This file is part of the Independent JPEG Group's software.
 * For conditions of distribution and use, see the accompanying README file.
 *
 * This file exists to provide a single place to fix any problems with
 * including the wrong system include files.  (Common problems are taken
 * care of by the standard jconfig symbols, but on really weird systems
 * you may have to edit this file.)
 *
 * NOTE: this file is NOT intended to be included by applications using the
 * JPEG library.  Most applications need only include jpeglib.h.
 */

/////////    IMPORTANT: THIS FILE HAS BEEN CHANGED BY GUICH TO SUPPORT TOTALCROSS AND SIMPLIFY SOME THINGS.
/////////    IT IS **NOT** THE ORIGINAL JINCLUDE.H file.

#ifndef JINCLUDE_H
#define JINCLUDE_H

#include "tcvm.h"

/* Include auto-config file to find out which system include files we need. */

#include "jconfig.h"    /* auto configuration options */
#define JCONFIG_INCLUDED   /* so that jpeglib.h doesn't do it again */

#ifdef WINCE
 #undef FILE
 #define FILE FORGET_IT_BILL
 #include <stdlib.h>
 #undef FILE
#endif

#include <string.h>
#define MEMZERO(target,size)  memset((void *)(target), 0, (size_t)(size))
#define MEMCOPY(dest,src,size)   memcpy((void *)(dest), (const void *)(src), (size_t)(size))

//////////////////////////////////////
/// start of changes by guich

// if a warning is given in the line below, you must add WINCE to the C/C++ Preprocessor definitions
struct TJPEGFILE
{
   TCZFile tcz; // if filled, we're reading from a tcz file, otherwise, from a totalcross.io.Stream
   // for fetching data

   union
   {
      TCObject inputStreamObj;
      TCObject outputStreamObj;
   };
   union
   {
      Method readBytesMethod;
      Method writeBytesMethod;
   };

   TCObject bufObj;
   TValue params[4];
   // the first 4 bytes
   char *first4;
   Context currentContext;
};

typedef struct TJPEGFILE JPEGFILE;

extern int jpegRead (void * buf, int size, JPEGFILE * in);

extern int jpegWrite(void *buff, int count, JPEGFILE *in);

/// end of changes by guich
//////////////////////////////////////

#ifdef WIN32
#define HAVE_BOOLEAN
typedef unsigned char boolean;
#endif
/*
 * In ANSI C, and indeed any rational implementation, size_t is also the
 * type returned by sizeof().  However, it seems there are some irrational
 * implementations out there, in which sizeof() returns an int even though
 * size_t is defined as long or unsigned long.  To ensure consistent results
 * we always use this SIZEOF() macro in place of using sizeof() directly.
 */

#define SIZEOF(object)  ((size_t) sizeof(object))

/*
 * The modules that use fread() and fwrite() always invoke them through
 * these macros.  On some systems you may need to twiddle the argument casts.
 * CAUTION: argument order is different from underlying functions!
 */

// GUICH - IMPORTANT: I CHANGED THE NAME OF FILE TO JPEGFILE in jpeglib.h and jdatasrc.c so it doesn't clashes with FILE from stdlib
// I ALSO CHANGED fread to jpegRead below
#define JFREAD(file,buf,sizeofbuf)  jpegRead ((void *) (buf), (int) (sizeofbuf), (file))

#define JFWRITE(file,buf,sizeofbuf) jpegWrite((void *) (buf), (int) (sizeofbuf), (file))

#endif
