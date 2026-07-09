// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "MinizipIO.h"

static void* ZCALLBACK tcMinizipOpen(void* opaque, const char* filename, int mode)
{
   UNUSED(opaque)
   UNUSED(mode)
   return (void*)filename;
}

static unsigned long ZCALLBACK tcMinizipRead(void* opaque, void* stream, void* buffer, unsigned long size)
{
   TCMinizipNativeP minizipNativeP = (TCMinizipNativeP)opaque;
   TCObject streamObj = (TCObject)stream;
   int32 result;

   if (minizipNativeP->readBuf == null || ARRAYOBJ_LEN(minizipNativeP->readBuf) < (int32)size)
   {
      if (minizipNativeP->readBuf != null)
         setObjectLock(minizipNativeP->readBuf, UNLOCKED);
      minizipNativeP->readBuf = createByteArray(minizipNativeP->context, (int32)size);
      if (minizipNativeP->readBuf == null)
         return 0;
   }

   result = executeMethod(minizipNativeP->context, minizipNativeP->streamRead, streamObj,
      minizipNativeP->readBuf, 0, (int32)size).asInt32;
   if (minizipNativeP->context->thrownException != null || result <= 0)
      return 0;

   xmemmove(buffer, ARRAYOBJ_START(minizipNativeP->readBuf), result);
   return (unsigned long)result;
}

static unsigned long ZCALLBACK tcMinizipWrite(void* opaque, void* stream, const void* buffer, unsigned long size)
{
   TCMinizipNativeP minizipNativeP = (TCMinizipNativeP)opaque;
   TCObject streamObj = (TCObject)stream;
   int32 result;

   if (minizipNativeP->writeBuf == null || ARRAYOBJ_LEN(minizipNativeP->writeBuf) < (int32)size)
   {
      if (minizipNativeP->writeBuf != null)
         setObjectLock(minizipNativeP->writeBuf, UNLOCKED);
      minizipNativeP->writeBuf = createByteArray(minizipNativeP->context, (int32)size);
      if (minizipNativeP->writeBuf == null)
         return 0;
   }

   xmemmove(ARRAYOBJ_START(minizipNativeP->writeBuf), buffer, size);
   result = executeMethod(minizipNativeP->context, minizipNativeP->streamWrite, streamObj,
      minizipNativeP->writeBuf, 0, (int32)size).asInt32;
   if (minizipNativeP->context->thrownException != null || result <= 0)
      return 0;

   return (unsigned long)result;
}

static long ZCALLBACK tcMinizipTell(void* opaque, void* stream)
{
   TCMinizipNativeP minizipNativeP = (TCMinizipNativeP)opaque;
   int32 result = executeMethod(minizipNativeP->context, minizipNativeP->streamTell, (TCObject)stream).asInt32;

   return minizipNativeP->context->thrownException == null ? result : -1;
}

static long ZCALLBACK tcMinizipSeek(void* opaque, void* stream, unsigned long offset, int origin)
{
   TCMinizipNativeP minizipNativeP = (TCMinizipNativeP)opaque;

   if (origin != ZLIB_FILEFUNC_SEEK_SET && origin != ZLIB_FILEFUNC_SEEK_CUR && origin != ZLIB_FILEFUNC_SEEK_END)
      return -1;

   executeMethod(minizipNativeP->context, minizipNativeP->streamSeek, (TCObject)stream, (int32)offset, origin);
   return minizipNativeP->context->thrownException == null ? 0 : -1;
}

static int ZCALLBACK tcMinizipClose(void* opaque, void* stream)
{
   UNUSED(opaque)
   UNUSED(stream)
   return 0;
}

static int ZCALLBACK tcMinizipError(void* opaque, void* stream)
{
   TCMinizipNativeP minizipNativeP = (TCMinizipNativeP)opaque;
   UNUSED(stream)
   return minizipNativeP->context->thrownException != null;
}

bool tcMinizipInitialize(Context currentContext, TCMinizipNativeP minizipNativeP, TCObject streamObj)
{
   minizipNativeP->streamRead = getMethod(OBJ_CLASS(streamObj), true, "readBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   minizipNativeP->streamWrite = getMethod(OBJ_CLASS(streamObj), true, "writeBytes", 3, BYTE_ARRAY, J_INT, J_INT);
   minizipNativeP->streamTell = getMethod(OBJ_CLASS(streamObj), true, "getPos", 0);
   minizipNativeP->streamSeek = getMethod(OBJ_CLASS(streamObj), true, "setPos", 2, J_INT, J_INT);
   minizipNativeP->streamClose = getMethod(OBJ_CLASS(streamObj), true, "close", 0);

   if (minizipNativeP->streamRead == null || minizipNativeP->streamWrite == null ||
      minizipNativeP->streamTell == null || minizipNativeP->streamSeek == null ||
      minizipNativeP->streamClose == null)
      return false;

   minizipNativeP->context = currentContext;
   minizipNativeP->isFirstFile = true;
   minizipNativeP->filefunc.zopen_file = tcMinizipOpen;
   minizipNativeP->filefunc.zread_file = tcMinizipRead;
   minizipNativeP->filefunc.zwrite_file = tcMinizipWrite;
   minizipNativeP->filefunc.ztell_file = tcMinizipTell;
   minizipNativeP->filefunc.zseek_file = tcMinizipSeek;
   minizipNativeP->filefunc.zclose_file = tcMinizipClose;
   minizipNativeP->filefunc.zerror_file = tcMinizipError;
   minizipNativeP->filefunc.opaque = minizipNativeP;
   return true;
}
