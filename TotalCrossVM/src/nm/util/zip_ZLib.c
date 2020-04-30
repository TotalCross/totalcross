// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

#include "../../zlib/zlib.h"
#include "../../zlib/zutil.h"

#define BUFSIZE   0x400

enum
{
   UNCOMPRESS = -1,
   COMPRESS_ZLIB = 0,
   COMPRESS_GZIP = 16
};


static voidpf zalloc(voidpf opaque, uInt items, uInt size)
{
   UNUSED(opaque)
	return xmalloc(items*size);
}

static void zfree(voidpf opaque, voidpf address)
{
   UNUSED(opaque)
	xfree(address);
}

static int32 commonDeflateInflate(Context currentContext, int32 compress, int32 buffersize, int32 levelOrSizeIn, int32 strategy, bool noWrap, TCObject in, TCObject out)
{
   TCObject inByteArray = null;
   TCObject outByteArray = null;
	CharP inArray, outArray;
	Method readMethod, writeMethod;
	int32 err = Z_OK;
	int32 count;
	bool endloop;
   int32 wrote;

	z_stream c_stream; // compression stream
	c_stream.zalloc = zalloc;
	c_stream.zfree = zfree;
	c_stream.opaque = (voidpf) 0;

   if (compress == UNCOMPRESS)
      err = inflateInit2(&c_stream, 32 + MAX_WBITS); // windowBits can also be greater than 15 for optional gzip decoding. Add 32 to windowBits to enable zlib and gzip decoding with automatic header detection, or add 16 to decode only the gzip format(the zlib format will return a Z_DATA_ERROR).
   else
   {
      int32 bits = compress == COMPRESS_GZIP ? (compress + MAX_WBITS) : (noWrap ? -MAX_WBITS : MAX_WBITS); // flsobral@tc123b_68: GZIP negative window bits is for "no wrap", GZIP uses its own value.
      err = deflateInit2(&c_stream, levelOrSizeIn, Z_DEFLATED, bits, DEF_MEM_LEVEL, strategy);
   }

   if (err != Z_OK)
   {
      if (err == Z_MEM_ERROR)
         throwException(currentContext, OutOfMemoryError, null);
      else
         throwException(currentContext, IOException, c_stream.msg);
      return -1;
   }
	if ((inByteArray = createByteArray(currentContext, buffersize)) == null)
      goto error;
   inArray = (CharP) ARRAYOBJ_START(inByteArray);

   if ((outByteArray = createByteArray(currentContext, buffersize)) == null)
      goto error;
   outArray = (CharP) ARRAYOBJ_START(outByteArray);

   if (compress != UNCOMPRESS) levelOrSizeIn = -1; // if compressing, level was already used, so we reset it to avoid confusing with sizeIn

   readMethod  = getMethod((TCClass) OBJ_CLASS(in), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   writeMethod = getMethod((TCClass) OBJ_CLASS(out), true, "writeBytes", 3, BYTE_ARRAY, J_INT, J_INT);

   c_stream.avail_out = buffersize;
   c_stream.next_out = (Bytef*)outArray;
   c_stream.avail_in = 0;

   do
   {
      if (c_stream.avail_out == 0)
      {
         wrote = 0;
         // no more output bytes available, write to the stream
         count = buffersize;
         
         do
         {
            wrote += executeMethod(currentContext, writeMethod, out, outByteArray, wrote, count - wrote).asInt32;
         } while (wrote != count && currentContext->thrownException == null);
         if (currentContext->thrownException != null)
            goto error;
         c_stream.next_out = (Bytef*)outArray;
         c_stream.avail_out = buffersize;
      }

      if (c_stream.avail_in == 0)
      {
         int32 tor = levelOrSizeIn == -1 ? buffersize : min32(buffersize, levelOrSizeIn);
         // no more bytes in the input buffer, read new bytes from the stream
         count = executeMethod(currentContext, readMethod, in, inByteArray, 0, tor).asInt32;
         if (currentContext->thrownException != null)
            goto error;
         if (count <= 0) break;
         c_stream.next_in = (Bytef*)inArray;
         c_stream.avail_in = count;
         if (levelOrSizeIn > 0)
            levelOrSizeIn -= count;
      }

      err = compress != UNCOMPRESS ? deflate(&c_stream, Z_NO_FLUSH) : inflate(&c_stream, Z_NO_FLUSH);
      if (err != Z_OK && err != Z_STREAM_END)
      {
         if (err == Z_MEM_ERROR)
            throwException(currentContext, OutOfMemoryError, null);
         else if (err == Z_DATA_ERROR)
            throwException(currentContext, ZipException, c_stream.msg);
         else
            throwException(currentContext, IOException, c_stream.msg);
         goto error;
      }
   }
   while (err == Z_OK);

   endloop = false;
   while (!endloop)
   {
      err = compress != UNCOMPRESS ? deflate(&c_stream, Z_FINISH) : inflate(&c_stream, Z_FINISH);
      if (err != Z_OK && err != Z_STREAM_END)
      {
         if (err == Z_MEM_ERROR)
            throwException(currentContext, OutOfMemoryError, null);
         else if (err == Z_DATA_ERROR)
            throwException(currentContext, ZipException, c_stream.msg);
         else
            throwException(currentContext, IOException, c_stream.msg);
         goto error;
      }
      if (c_stream.avail_out != 0) endloop = true;

      if ((int32) c_stream.avail_out < buffersize)
      {
         wrote = 0;
         // no more output bytes available, write to the stream
         count = buffersize - c_stream.avail_out;
         
         do
         {
            wrote += executeMethod(currentContext, writeMethod, out, outByteArray, wrote, count - wrote).asInt32;
         } while (wrote != count && currentContext->thrownException == null);
         if (currentContext->thrownException != null)
            goto error;
         c_stream.next_out = (Bytef*)outArray;
         c_stream.avail_out = buffersize;
      }
   }

error:
   err = compress != UNCOMPRESS ? deflateEnd(&c_stream) : inflateEnd(&c_stream);
   if (err != Z_OK && currentContext->thrownException == null)
   {
      if (err == Z_MEM_ERROR)
         throwException(currentContext, OutOfMemoryError, null);
      else if (err == Z_DATA_ERROR)
         throwException(currentContext, ZipException, c_stream.msg);
      else
         throwException(currentContext, IOException, c_stream.msg);
   }
   if (outByteArray != null) setObjectLock(outByteArray, UNLOCKED);
   if (inByteArray  != null) setObjectLock(inByteArray,  UNLOCKED);

   return currentContext->thrownException == null ? (int32)c_stream.total_out : -1;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tuzZL_deflate_ssiib(NMParams p) // totalcross/util/zip/ZLib native public static int deflate(totalcross.io.Stream in, totalcross.io.Stream out, int compressionLevel, int strategy, boolean noWrap) throws IOException;
{
   TCObject streamIn = p->obj[0];
   TCObject streamOut = p->obj[1];
   int32 level = p->i32[0];
   int32 strategy = p->i32[1];
   bool noWrap = p->i32[2];
   int32 compressionType = COMPRESS_ZLIB;

   if (!streamIn)
      throwNullArgumentException(p->currentContext, "in");
   else
   if (!streamOut)
      throwNullArgumentException(p->currentContext, "out");
   else
   {
      if (level == 15) //flsobral@tc114_82: if level is 15, we want gzip compression instead of zlib.
      {
         compressionType = COMPRESS_GZIP;
         level = Z_DEFAULT_COMPRESSION;
      }
      
      if (level < -1 || level > 9) //flsobraltc123a_64: removed else that caused GZip to jump over the deflate method.
         throwIllegalArgumentExceptionI(p->currentContext, "compressionLevel", level);
      else
         p->retI = commonDeflateInflate(p->currentContext, compressionType, BUFSIZE, level, strategy, noWrap, streamIn, streamOut);
   }   
}
//////////////////////////////////////////////////////////////////////////
TC_API void tuzZL_inflate_ssib(NMParams p) // totalcross/util/zip/ZLib native public static int inflate(totalcross.io.Stream in, totalcross.io.Stream out, int sizeIn, boolean noWrap) throws IOException, ZipException;
{
   TCObject streamIn = p->obj[0];
   TCObject streamOut = p->obj[1];
   int32 sizeIn = p->i32[0];
   bool noWrap = p->i32[1];

   if (!streamIn)
      throwNullArgumentException(p->currentContext, "in");
   else
   if (!streamOut)
      throwNullArgumentException(p->currentContext, "out");
   else
   if (sizeIn < -1)
      throwIllegalArgumentExceptionI(p->currentContext, "sizeIn", sizeIn);
   else
   if (sizeIn == 0)
      p->retI = 0;
   else
      p->retI = commonDeflateInflate(p->currentContext, UNCOMPRESS, BUFSIZE, sizeIn, 0, noWrap, streamIn, streamOut);
}

#ifdef ENABLE_TEST_SUITE
#include "zip_ZLib_test.h"
#endif
