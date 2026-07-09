// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

/*
 * This file is derived from libjpeg-turbo's jdatasrc.c, which was part of the
 * Independent JPEG Group's software:
 * Copyright (C) 1994-1996, Thomas G. Lane.
 * Modified 2009-2011 by Guido Vollbeding.
 * libjpeg-turbo Modifications:
 * Copyright (C) 2013, 2016, 2022, D. R. Commander.
 * For conditions of distribution and use, see the accompanying README.ijg
 * file.
 */

#include "JpegLoader.h"
#include "jerror.h"

typedef struct
{
   struct jpeg_source_mgr pub;
   JOCTET *buffer;
   boolean start_of_file;
} TCJpegSourceManager;

typedef TCJpegSourceManager *TCJpegSourcePtr;

#define INPUT_BUF_SIZE 4096

static void init_source(j_decompress_ptr cinfo)
{
   TCJpegSourcePtr src = (TCJpegSourcePtr)cinfo->src;

   src->start_of_file = TRUE;
}

static boolean fill_input_buffer(j_decompress_ptr cinfo)
{
   TCJpegSourcePtr src = (TCJpegSourcePtr)cinfo->src;
   TCJpegIOContext *io = (TCJpegIOContext *)cinfo->client_data;
   size_t nbytes;

   if (io == NULL || io->read == NULL)
      ERREXIT(cinfo, JERR_INPUT_EMPTY);

   nbytes = io->read(io->opaque, src->buffer, INPUT_BUF_SIZE);

   if (nbytes <= 0)
   {
      if (src->start_of_file)
         ERREXIT(cinfo, JERR_INPUT_EMPTY);

      WARNMS(cinfo, JWRN_JPEG_EOF);
      src->buffer[0] = (JOCTET)0xFF;
      src->buffer[1] = (JOCTET)JPEG_EOI;
      nbytes = 2;
   }

   src->pub.next_input_byte = src->buffer;
   src->pub.bytes_in_buffer = nbytes;
   src->start_of_file = FALSE;

   return TRUE;
}

static void skip_input_data(j_decompress_ptr cinfo, long num_bytes)
{
   struct jpeg_source_mgr *src = cinfo->src;

   if (num_bytes > 0)
   {
      while (num_bytes > (long)src->bytes_in_buffer)
      {
         num_bytes -= (long)src->bytes_in_buffer;
         (void)(*src->fill_input_buffer)(cinfo);
      }
      src->next_input_byte += (size_t)num_bytes;
      src->bytes_in_buffer -= (size_t)num_bytes;
   }
}

static void term_source(j_decompress_ptr cinfo)
{
   UNUSED(cinfo);
}

void jpeg_tc_src(j_decompress_ptr cinfo)
{
   TCJpegSourcePtr src;
   TCJpegIOContext *io = (TCJpegIOContext *)cinfo->client_data;

   if (io == NULL || io->read == NULL)
      ERREXIT(cinfo, JERR_INPUT_EMPTY);

   if (cinfo->src == NULL)
   {
      cinfo->src = (struct jpeg_source_mgr *)(*cinfo->mem->alloc_small)((j_common_ptr)cinfo, JPOOL_PERMANENT,
                                                                        sizeof(TCJpegSourceManager));
      src = (TCJpegSourcePtr)cinfo->src;
      src->buffer = (JOCTET *)(*cinfo->mem->alloc_small)((j_common_ptr)cinfo, JPOOL_PERMANENT,
                                                         INPUT_BUF_SIZE * sizeof(JOCTET));
   }
   else if (cinfo->src->init_source != init_source)
   {
      ERREXIT(cinfo, JERR_BUFFER_SIZE);
   }

   src = (TCJpegSourcePtr)cinfo->src;
   src->pub.init_source = init_source;
   src->pub.fill_input_buffer = fill_input_buffer;
   src->pub.skip_input_data = skip_input_data;
   src->pub.resync_to_restart = jpeg_resync_to_restart;
   src->pub.term_source = term_source;
   src->pub.bytes_in_buffer = 0;
   src->pub.next_input_byte = NULL;
}
