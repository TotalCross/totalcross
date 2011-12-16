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



#if HAVE_CONFIG_H
#include "config.h"
#endif

#include "stdio.h"
#include "stdlib.h"
#include "error.h"
#include "palm_posix.h"

#if HAVE_STDARG_H
#include <stdarg.h>
#endif

#if HAVE_STRING_H
#include "string.h"
#endif

FILE *openFiles;
size_t errno;

// use prc-tools gcc flag __OWNGP__ to support or not global variables ?
//FILE *fstdin  = (FILE *)0;
//FILE *fstdout = (FILE *)1;
//FILE *fstderr = (FILE *)2;

FILE *_stdin()
{
   return (FILE *)0;
}

FILE *_stdout()
{
   return (FILE *)1;
}

FILE *_stderr()
{
   return (FILE *)2;
}

FILE *fopen(const char *path, const char *mode)
{
   FileHand fh;
   Err error;
   UInt32 openMode=0;
   FILE *newFILE;
   MemHandle mh = 0;

   errno = 0;
   if (path == 0) return 0;

   if (strncmp(mode, "r+", 2) == 0)
      openMode=fileModeUpdate;
   else
   if (*mode == 'r')
      openMode=fileModeReadOnly;
   else
   if (*mode == 'w')
      openMode=fileModeReadWrite;
   else
   if (*mode == 'a')
      openMode=fileModeAppend;
   else
   {
      if (alert_cb) alert_cb("'%s' unsupported mode", mode);
      return 0;
   }

   fh = FileOpen(path, 'DATA', appId_cb(), openMode, &error);
   if (error == fileErrNotStream) // maybe trying to open Litebase.prc instead of Litebase.pdb? Explicitly search for a tcz file
   {
      DmSearchStateType state;
      error = DmGetNextDatabaseByTypeCreator(true, &state, 'TCZF', 0, true, &mh);
      if (error == errNone)
         error = fileErrTypeCreatorMismatch; // will be handled below
   }
   if (error == fileErrNotFound) // errNotStream: opening a prc instead of a pdb
      return 0;
   if (error == fileErrTypeCreatorMismatch && (mh != 0 || (mh = DmFindDatabase(path)) != 0))
   {
      UInt32 crid,type;
      DmDatabaseInfo(mh, 0,0,0,0,0,0,0,0,0,&type,&crid);
      fh = FileOpen (path, type, crid, openMode, &error); // try again, this time using the wildcards
      if (error == fileErrNotFound)
      {
         errno = error;
         return 0;
      }
   }
   if (error != 0)
   {
      if (alert_cb) alert_cb("error opening %s: %d",path, error);
      return 0;
   }
   if (fh == 0)
   {
      if (alert_cb) alert_cb("null filehandle for %s", path);
      return 0;
   }

   newFILE = (FILE *)malloc(sizeof(FILE));
   if (newFILE == 0)
   {
      FileClose(fh);
      if (alert_cb) alert_cb("out of memory when creating FILE\nfor %s",path);
      return 0;
   }

   newFILE->fh = fh;
   newFILE->next = openFiles;
   openFiles = newFILE;
   return newFILE;
}

int feof (FILE *stream)
{
   return (stream->fh != 0) ? FileEOF(stream->fh) : 0;
}

int fgetc (FILE *stream)
{
   char c;
   size_t sz = fread(&c, 1, 1, stream);
   return (sz == 1) ? c : EOF;
}

char *fgets (char *s, int size, FILE *stream)
{
   char *ss = s;
   while (--size > 0)
   {
      int c = fgetc(stream);
      if (c != EOF)
      {
         *s++ = c;
      }
      if (c == EOF || c == '\n') break;
   }
   *s = '\0';
   return ss;
}

int fprintf(FILE *stream, const char *format, ...)
{
#if HAVE_STDARG_H
   char msg[256];
   ErrFatalDisplayIf(strlen(format) > sizeof(msg), "buffer overrun risk");

   if (stream == stderr)
   {
      va_list va;
      va_start(va,format);
      vsprintf(msg,format,va);
      va_end(va);

      ErrDisplay(msg);
   }
#endif
   return 0;
}

size_t fread(void *ptr, size_t size, size_t nmemb, FILE *stream)
{
   Err error;
   errno = 0;

   if (stream->fh != 0)
   {
      int num = FileRead(stream->fh, ptr, size, nmemb, &error);
      if (error != 0 && error != fileErrEOF)
         return -1;
      return num;
   }
   return 0;
}

size_t fwrite(const void *ptr, size_t size, size_t nmemb, FILE *stream)
{
   Err error;
   errno = 0;

   if (stream->fh != 0)
   {
      int num = FileWrite(stream->fh, ptr, size, nmemb, &error);
      errno = error;
      if (error != 0)
         return -1;
      return num;
   }
   return 0;
}

long ftell(FILE *stream)
{
   Err error;
   errno = 0;

   if (stream->fh != 0)
   {
      Int32 p = FileTell(stream->fh, 0, &error);
      errno = error;
      if (error != 0)
         return -1;
      return p;
   }
   return 0;
}

int fseek(FILE *stream, long offset, int whence)
{
   Err error;
   FileOriginEnum origin;

   if (stream->fh != 0)
   {
      if (whence == SEEK_SET) origin = fileOriginBeginning;
      else
      if (whence == SEEK_END) origin = fileOriginEnd;
      else
      if (whence == SEEK_CUR) origin = fileOriginCurrent;
      else
         return -1;

      error = FileSeek(stream->fh, offset, origin);
      errno = error;
      if (error != 0)
         return -1;
   }
   return 0;
}

int fclose(FILE *stream)
{
   if (stream && stream->fh != 0)
   {
      FileClose(stream->fh);
      stream->fh = 0;
   }
   return 0;
}

int ferror(FILE *stream)
{
   stream = stream; // unused param
   return 0;
}

void closeAllFiles()
{
   while (openFiles)
   {
      FILE *next = openFiles->next;
      if (openFiles->fh)
         fclose(openFiles);
      free(openFiles);
      openFiles = next;
   }
}
