// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef JPEG_LOADER_H
#define JPEG_LOADER_H

#include <stddef.h>

#include "tcvm.h"
#include "jpeglib.h"

typedef struct TJPEGFILE
{
   TCZFile tcz; // if filled, we're reading from a tcz file, otherwise, from a totalcross.io.Stream

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
   const char *first4;
   const char *mapped;
   int32 size;
   int32 cursor;
   Context currentContext;
} JPEGFILE;

typedef size_t (*TCJpegReadFunc)(void *opaque, void *buffer, size_t count);
typedef size_t (*TCJpegWriteFunc)(void *opaque, const void *buffer, size_t count);

typedef struct TCJpegIOContext
{
   void *opaque;
   TCJpegReadFunc read;
   TCJpegWriteFunc write;
} TCJpegIOContext;

void jpegLoad(Context currentContext, TCObject imageObj, TCObject inputStreamObj, TCObject bufObj, TCZFile tcz,
              const char *first4, int32 size, int32 targetWidthOrScaleNum, int32 targetHeightOrScaleDenom);
bool image2jpeg(Context currentContext, TCObject srcImageObj, TCObject dstStreamObj, int32 quality);

void jpeg_tc_src(j_decompress_ptr cinfo);
void jpeg_tc_dest(j_compress_ptr cinfo);

#endif
