// Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef JPEGLIB_TC_H
#define JPEGLIB_TC_H

// usually jinclude.h handles these
// #ifdef HAVE_STDDEF_H
#include <stddef.h>
// #endif
#include <stdio.h>

#include "jpeglib.h"
#include "tcz.h"
#include "tcclass.h"
#include "tcvm.h"

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

#undef JFREAD
#define JFREAD(file,buf,sizeofbuf)  jpegRead ((void *) (buf), (int) (sizeofbuf), (file))

#undef JFWRITE
#define JFWRITE(file,buf,sizeofbuf) jpegWrite((void *) (buf), (int) (sizeofbuf), (file))


EXTERN(void) jpeg_tiF_dest JPP((j_compress_ptr cinfo, JPEGFILE * outfile));
EXTERN(void) jpeg_tiF_src JPP((j_decompress_ptr cinfo, JPEGFILE * infile));

#endif
