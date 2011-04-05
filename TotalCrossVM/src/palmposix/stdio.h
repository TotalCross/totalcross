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



#ifndef __STDIO_H__
#define __STDIO_H__

#define sprintf disabled_sprintf
#define vsprintf disabled_vsprintf

//#define EMULATION_LEVEL
#ifdef __arm__
#include <PalmOSARM.h>
#else
#include <PalmOS.h>
#include_next <stdio.h>
#endif

#include_next <stddef.h>

#undef sprintf
#undef vsprintf

#define sprintf StrPrintF
#define vsprintf StrVPrintF

#ifndef _SIZE_T_DEFINED_
#define _SIZE_T_DEFINED_
typedef unsigned long size_t;
#endif

typedef struct FILE
{
   FileHand fh;
   struct FILE *next;
} FILE;

#undef stdin
#undef stdout
#undef stderr

// use prc-tools gcc flag __OWNGP__ to support or not global variables ?

/* Standard streams.  */
#define stdin     _stdin()    /* Standard input stream.  */
#define stdout    _stdout()   /* Standard output stream. */
#define stderr    _stderr()   /* Standard error output stream. */

//extern FILE *stdin;          /* Standard input stream.  */
//extern FILE *stdout;         /* Standard output stream. */
//extern FILE *stderr;         /* Standard error output stream. */

/* End of file character.
   Some things throughout the library rely on this being -1.  */
#ifndef EOF
#define EOF (-1)
#endif

enum
{
  SEEK_SET,    /* Seek from beginning of file.  */
  SEEK_CUR,    /* Seek from current position.  */
  SEEK_END     /* Seek from end of file.  */
};

int feof (FILE *stream);
int fgetc (FILE *stream);
char *fgets (char *s, int size, FILE *stream);
int fprintf(FILE *stream, const char *format, ...);

FILE *fopen(const char *path, const char *mode);
size_t fread(void *ptr, size_t size, size_t nmemb, FILE *stream);
size_t fwrite(const  void  *ptr,  size_t  size,  size_t  nmemb,  FILE *stream);
long ftell(FILE *stream);
int fseek(FILE *stream, long offset, int whence);
int fclose(FILE *stream);
int ferror(FILE *stream);

extern size_t errno;

FILE *_stdin();
FILE *_stdout();
FILE *_stderr();

#endif //  __STDIO_H__
