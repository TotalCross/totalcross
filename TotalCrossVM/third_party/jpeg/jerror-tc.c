// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

/*
 * This file is derived from libjpeg's jerror.c, which was part of the
 * Independent JPEG Group's software:
 * Copyright (C) 1991-1998, Thomas G. Lane.
 * Modified 2012-2025 by Guido Vollbeding.
 * For conditions of distribution and use, see the accompanying README file.
 */

#include <stdio.h>

#include "jerror-tc.h"
#include "jerror.h"

#ifndef JCOPYRIGHT_SHORT
#define JCOPYRIGHT_SHORT "Independent JPEG Group's software"
#endif

#ifndef JVERSION
#define JVERSION "libjpeg-compatible"
#endif

#define JMESSAGE(code, string) string,

static const char *const tc_jpeg_std_message_table[] = {
#include "jerror.h"
   NULL
};

#undef JMESSAGE

static void error_exit(j_common_ptr cinfo)
{
   TCJpegErrorManager *err = (TCJpegErrorManager *)cinfo->err;

   (*cinfo->err->output_message)(cinfo);
   HEAP_ERROR(err->heap, err->pub.msg_code);
}

static void output_message(j_common_ptr cinfo)
{
   char buffer[JMSG_LENGTH_MAX];

   (*cinfo->err->format_message)(cinfo, buffer);
   debug("%s", buffer);
}

static void emit_message(j_common_ptr cinfo, int msg_level)
{
   struct jpeg_error_mgr *err = cinfo->err;

   if (msg_level < 0)
   {
      if (err->num_warnings == 0 || err->trace_level >= 3)
         (*err->output_message)(cinfo);
      err->num_warnings++;
   }
   else if (err->trace_level >= msg_level)
   {
      (*err->output_message)(cinfo);
   }
}

static void format_message(j_common_ptr cinfo, char *buffer)
{
   struct jpeg_error_mgr *err = cinfo->err;
   int msg_code = err->msg_code;
   const char *msgtext = NULL;
   const char *msgptr;
   char ch;
   boolean isstring;

   if (msg_code > 0 && msg_code <= err->last_jpeg_message)
      msgtext = err->jpeg_message_table[msg_code];
   else if (err->addon_message_table != NULL &&
            msg_code >= err->first_addon_message &&
            msg_code <= err->last_addon_message)
      msgtext = err->addon_message_table[msg_code - err->first_addon_message];

   if (msgtext == NULL)
   {
      err->msg_parm.i[0] = msg_code;
      msgtext = err->jpeg_message_table[0];
   }

   isstring = FALSE;
   msgptr = msgtext;
   while ((ch = *msgptr++) != '\0')
   {
      if (ch == '%')
      {
         if (*msgptr == 's')
            isstring = TRUE;
         break;
      }
   }

   if (isstring)
      sprintf(buffer, msgtext, err->msg_parm.s);
   else
      sprintf(buffer, msgtext,
              err->msg_parm.i[0], err->msg_parm.i[1],
              err->msg_parm.i[2], err->msg_parm.i[3],
              err->msg_parm.i[4], err->msg_parm.i[5],
              err->msg_parm.i[6], err->msg_parm.i[7]);
}

static void reset_error_mgr(j_common_ptr cinfo)
{
   cinfo->err->num_warnings = 0;
   cinfo->err->msg_code = 0;
}

struct jpeg_error_mgr *tc_jpeg_std_error(TCJpegErrorManager *err, Heap heap)
{
   err->pub.error_exit = error_exit;
   err->pub.emit_message = emit_message;
   err->pub.output_message = output_message;
   err->pub.format_message = format_message;
   err->pub.reset_error_mgr = reset_error_mgr;

   err->pub.msg_code = 0;
   err->pub.msg_parm.i[0] = 0;
   err->pub.msg_parm.i[1] = 0;
   err->pub.msg_parm.i[2] = 0;
   err->pub.msg_parm.i[3] = 0;
   err->pub.msg_parm.i[4] = 0;
   err->pub.msg_parm.i[5] = 0;
   err->pub.msg_parm.i[6] = 0;
   err->pub.msg_parm.i[7] = 0;

   err->pub.trace_level = 0;
   err->pub.num_warnings = 0;
   err->pub.jpeg_message_table = tc_jpeg_std_message_table;
   err->pub.last_jpeg_message = (int)JMSG_LASTMSGCODE - 1;
   err->pub.addon_message_table = NULL;
   err->pub.first_addon_message = 0;
   err->pub.last_addon_message = 0;
   err->heap = heap;

   return &err->pub;
}
