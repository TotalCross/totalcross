// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

/*
 * This file is derived from libjpeg-turbo's jdatadst.c, which was part of the
 * Independent JPEG Group's software:
 * Copyright (C) 1994-1996, Thomas G. Lane.
 * Modified 2009-2012 by Guido Vollbeding.
 * libjpeg-turbo Modifications:
 * Copyright (C) 2013, 2016, 2022, D. R. Commander.
 * For conditions of distribution and use, see the accompanying README.ijg
 * file.
 */

#include "JpegLoader.h"
#include "jerror.h"

typedef struct
{
   struct jpeg_destination_mgr pub;
   JOCTET *buffer;
} TCJpegDestinationManager;

typedef TCJpegDestinationManager *TCJpegDestinationPtr;

#define OUTPUT_BUF_SIZE 4096

static void init_destination(j_compress_ptr cinfo)
{
   TCJpegDestinationPtr dest = (TCJpegDestinationPtr)cinfo->dest;

   dest->buffer = (JOCTET *)(*cinfo->mem->alloc_small)((j_common_ptr)cinfo, JPOOL_IMAGE,
                                                       OUTPUT_BUF_SIZE * sizeof(JOCTET));

   dest->pub.next_output_byte = dest->buffer;
   dest->pub.free_in_buffer = OUTPUT_BUF_SIZE;
}

static boolean empty_output_buffer(j_compress_ptr cinfo)
{
   TCJpegDestinationPtr dest = (TCJpegDestinationPtr)cinfo->dest;
   TCJpegIOContext *io = (TCJpegIOContext *)cinfo->client_data;

   if (io == NULL || io->write == NULL || io->write(io->opaque, dest->buffer, OUTPUT_BUF_SIZE) != (size_t)OUTPUT_BUF_SIZE)
      ERREXIT(cinfo, JERR_FILE_WRITE);

   dest->pub.next_output_byte = dest->buffer;
   dest->pub.free_in_buffer = OUTPUT_BUF_SIZE;

   return TRUE;
}

static void term_destination(j_compress_ptr cinfo)
{
   TCJpegDestinationPtr dest = (TCJpegDestinationPtr)cinfo->dest;
   TCJpegIOContext *io = (TCJpegIOContext *)cinfo->client_data;
   size_t datacount = OUTPUT_BUF_SIZE - dest->pub.free_in_buffer;

   if (datacount > 0 &&
       (io == NULL || io->write == NULL || io->write(io->opaque, dest->buffer, datacount) != datacount))
      ERREXIT(cinfo, JERR_FILE_WRITE);
}

void jpeg_tc_dest(j_compress_ptr cinfo)
{
   TCJpegDestinationPtr dest;
   TCJpegIOContext *io = (TCJpegIOContext *)cinfo->client_data;

   if (io == NULL || io->write == NULL)
      ERREXIT(cinfo, JERR_FILE_WRITE);

   if (cinfo->dest == NULL)
      cinfo->dest = (struct jpeg_destination_mgr *)(*cinfo->mem->alloc_small)((j_common_ptr)cinfo, JPOOL_PERMANENT,
                                                                              sizeof(TCJpegDestinationManager));
   else if (cinfo->dest->init_destination != init_destination)
      ERREXIT(cinfo, JERR_BUFFER_SIZE);

   dest = (TCJpegDestinationPtr)cinfo->dest;
   dest->pub.init_destination = init_destination;
   dest->pub.empty_output_buffer = empty_output_buffer;
   dest->pub.term_destination = term_destination;
}
