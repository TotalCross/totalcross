/* ioapi.c -- IO base function header for compress/uncompress .zip
   files using zlib + zip or unzip API

   Version 1.01e, February 12th, 2005

   Copyright (C) 1998-2005 Gilles Vollant
*/


#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "zlib.h"
#include "ioapi.h"



/* I've found an old Unix (a SunOS 4.1.3_U1) without all SEEK_* defined.... */

#ifndef SEEK_CUR
#define SEEK_CUR    1
#endif

#ifndef SEEK_END
#define SEEK_END    2
#endif

#ifndef SEEK_SET
#define SEEK_SET    0
#endif

voidpf ZCALLBACK fopen_file_func OF((
   voidpf opaque,
   const char* filename,
   int mode));

uLong ZCALLBACK fread_file_func OF((
   voidpf opaque,
   voidpf stream,
   void* buf,
   uLong size));

uLong ZCALLBACK fwrite_file_func OF((
   voidpf opaque,
   voidpf stream,
   const void* buf,
   uLong size));

long ZCALLBACK ftell_file_func OF((
   voidpf opaque,
   voidpf stream));

long ZCALLBACK fseek_file_func OF((
   voidpf opaque,
   voidpf stream,
   uLong offset,
   int origin));

int ZCALLBACK fclose_file_func OF((
   voidpf opaque,
   voidpf stream));

int ZCALLBACK ferror_file_func OF((
   voidpf opaque,
   voidpf stream));

#if defined TOTALCROSS

voidpf ZCALLBACK fopen_file_func (opaque, filename, mode)
   voidpf opaque;
   const char* filename;
   int mode;
{
   TCObject fileObj = (TCObject) filename;
   return fileObj; //file;
}

uLong ZCALLBACK fread_file_func (opaque, stream, buf, size)
   voidpf opaque;
   voidpf stream;
   void* buf;
   uLong size;
{
   long ret;
   uLong bufSize = 0;
   TCObject streamObj = (TCObject) stream;
   ZipNativeP zipNativeP = (ZipNativeP) opaque;

   if (zipNativeP->readBuf != null)
      bufSize = ARRAYOBJ_LEN(zipNativeP->readBuf);

   if (bufSize < size)
   {
      setObjectLock(zipNativeP->readBuf, UNLOCKED);
      zipNativeP->readBuf = createByteArray(zipNativeP->context, size);
   }

   ret = executeMethod(zipNativeP->context, zipNativeP->streamRead, streamObj,  zipNativeP->readBuf, 0, size).asInt32;
   xmemmove(buf, ARRAYOBJ_START(zipNativeP->readBuf), size);

   return ret;
}

uLong ZCALLBACK fwrite_file_func (opaque, stream, buf, size)
   voidpf opaque;
   voidpf stream;
   const void* buf;
   uLong size;
{
   long ret;
   uLong bufSize = 0;
   TCObject streamObj = (TCObject) stream;
   ZipNativeP zipNativeP = (ZipNativeP) opaque;

   if (zipNativeP->writeBuf != null)
      bufSize = ARRAYOBJ_LEN(zipNativeP->writeBuf);

   if (bufSize < size)
   {
      setObjectLock(zipNativeP->writeBuf, UNLOCKED);
      zipNativeP->writeBuf = createByteArray(zipNativeP->context, size);
   }

   xmemmove(ARRAYOBJ_START(zipNativeP->writeBuf), buf, size);
   ret = executeMethod(zipNativeP->context, zipNativeP->streamWrite, streamObj,  zipNativeP->writeBuf, 0, size).asInt32;

   return ret;
}

long ZCALLBACK ftell_file_func (opaque, stream)
   voidpf opaque;
   voidpf stream;
{
   long ret;
   TCObject streamObj = (TCObject) stream;
   ZipNativeP zipNativeP = (ZipNativeP) opaque;

   ret = executeMethod(zipNativeP->context, zipNativeP->streamTell, streamObj).asInt32;

   return ret;
}

long ZCALLBACK fseek_file_func (opaque, stream, offset, origin)
   voidpf opaque;
   voidpf stream;
   uLong offset;
   int origin;
{
   TCObject streamObj = (TCObject) stream;
   ZipNativeP zipNativeP = (ZipNativeP) opaque;

   int fseek_origin=0;
   long ret;
   switch (origin)
   {
      case ZLIB_FILEFUNC_SEEK_CUR :
         fseek_origin = 1; //SEEK_CUR;
         break;
      case ZLIB_FILEFUNC_SEEK_END :
         fseek_origin = 2; //SEEK_END;
         break;
      case ZLIB_FILEFUNC_SEEK_SET :
         fseek_origin = 0; //SEEK_SET;
         break;
      default: return -1;
   }
   ret = 0;

   executeMethod(zipNativeP->context, zipNativeP->streamSeek, streamObj, offset, fseek_origin);

   return ret;
}

int ZCALLBACK fclose_file_func (opaque, stream)
   voidpf opaque;
   voidpf stream;
{
   TCObject streamObj = (TCObject) stream;
   ZipNativeP zipNativeP = (ZipNativeP) opaque;

   //executeMethod(zipNativeP->context, zipNativeP->streamClose, streamObj);
   return 0;
}

int ZCALLBACK ferror_file_func (opaque, stream)
   voidpf opaque;
   voidpf stream;
{
    return false;
}

void fill_fopen_filefunc (pzlib_filefunc_def)
  zlib_filefunc_def* pzlib_filefunc_def;
{
    pzlib_filefunc_def->zopen_file = fopen_file_func;
    pzlib_filefunc_def->zread_file = fread_file_func;
    pzlib_filefunc_def->zwrite_file = fwrite_file_func;
    pzlib_filefunc_def->ztell_file = ftell_file_func;
    pzlib_filefunc_def->zseek_file = fseek_file_func;
    pzlib_filefunc_def->zclose_file = fclose_file_func;
    /*
    pzlib_filefunc_def->zerror_file = ferror_file_func;
    pzlib_filefunc_def->opaque = NULL;
    */
}
#else
voidpf ZCALLBACK fopen_file_func (opaque, filename, mode)
   voidpf opaque;
   const char* filename;
   int mode;
{
    FILE* file = NULL;
    const char* mode_fopen = NULL;
    if ((mode & ZLIB_FILEFUNC_MODE_READWRITEFILTER)==ZLIB_FILEFUNC_MODE_READ)
        mode_fopen = "rb";
    else
    if (mode & ZLIB_FILEFUNC_MODE_EXISTING)
        mode_fopen = "r+b";
    else
    if (mode & ZLIB_FILEFUNC_MODE_CREATE)
        mode_fopen = "wb";

    if ((filename!=NULL) && (mode_fopen != NULL))
        file = fopen(filename, mode_fopen);
    return file;
}

uLong ZCALLBACK fread_file_func (opaque, stream, buf, size)
   voidpf opaque;
   voidpf stream;
   void* buf;
   uLong size;
{
    uLong ret;
    ret = (uLong)fread(buf, 1, (size_t)size, (FILE *)stream);
    return ret;
}

uLong ZCALLBACK fwrite_file_func (opaque, stream, buf, size)
   voidpf opaque;
   voidpf stream;
   const void* buf;
   uLong size;
{
    uLong ret;
    ret = (uLong)fwrite(buf, 1, (size_t)size, (FILE *)stream);
    return ret;
}

long ZCALLBACK ftell_file_func (opaque, stream)
   voidpf opaque;
   voidpf stream;
{
    long ret;
    ret = ftell((FILE *)stream);
    return ret;
}

long ZCALLBACK fseek_file_func (opaque, stream, offset, origin)
   voidpf opaque;
   voidpf stream;
   uLong offset;
   int origin;
{
    int fseek_origin=0;
    long ret;
    switch (origin)
    {
    case ZLIB_FILEFUNC_SEEK_CUR :
        fseek_origin = SEEK_CUR;
        break;
    case ZLIB_FILEFUNC_SEEK_END :
        fseek_origin = SEEK_END;
        break;
    case ZLIB_FILEFUNC_SEEK_SET :
        fseek_origin = SEEK_SET;
        break;
    default: return -1;
    }
    ret = 0;
    fseek((FILE *)stream, offset, fseek_origin);
    return ret;
}

int ZCALLBACK fclose_file_func (opaque, stream)
   voidpf opaque;
   voidpf stream;
{
    int ret;
    ret = fclose((FILE *)stream);
    return ret;
}

int ZCALLBACK ferror_file_func (opaque, stream)
   voidpf opaque;
   voidpf stream;
{
    int ret;
    ret = ferror((FILE *)stream);
    return ret;
}
#endif

#if !defined TOTALCROSS
void fill_fopen_filefunc (pzlib_filefunc_def)
  zlib_filefunc_def* pzlib_filefunc_def;
{
    pzlib_filefunc_def->zopen_file = fopen_file_func;
    pzlib_filefunc_def->zread_file = fread_file_func;
    pzlib_filefunc_def->zwrite_file = fwrite_file_func;
    pzlib_filefunc_def->ztell_file = ftell_file_func;
    pzlib_filefunc_def->zseek_file = fseek_file_func;
    pzlib_filefunc_def->zclose_file = fclose_file_func;
    pzlib_filefunc_def->zerror_file = ferror_file_func;
    pzlib_filefunc_def->opaque = NULL;
}
#endif
