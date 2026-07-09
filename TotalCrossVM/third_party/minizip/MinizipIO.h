// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#ifndef TOTALCROSS_MINIZIP_IO_H
#define TOTALCROSS_MINIZIP_IO_H

#include "tcvm.h"
#include <ioapi.h>

typedef struct TCMinizipNative {
   void* zipFile;
   zlib_filefunc_def filefunc;
   bool isFirstFile;
   int32 method;

   Method streamRead;
   Method streamWrite;
   Method streamTell;
   Method streamSeek;
   Method streamClose;

   TCObject readBuf;
   TCObject writeBuf;

   Context context;
} TCMinizipNative, *TCMinizipNativeP;

bool tcMinizipInitialize(Context currentContext, TCMinizipNativeP minizipNativeP, TCObject streamObj);

#endif
