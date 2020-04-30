// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TC_API void tiF_create_sii(NMParams p);
TC_API void tiF_close(NMParams p);

#define TEST_ZLIB false

TESTCASE(ZLib) // totalcross/util/zip/ZLib
{
#if TEST_ZLIB
   TNMParams tmnpZlib, tmnpFileIn, tmnpFileOut;
   int32 i32Zlib[3], i32FileIn[2], i32FileOut[2];
   TCObject objZlib[2];
   TCObject objFileIn[2];
   TCObject objFileOut[2];
   char srcFileName[MAX_PATHNAME];
   char zipFileName[MAX_PATHNAME];
   char dstFileName[MAX_PATHNAME];
   CharP appPath;

   appPath = getAppPath();
   xstrcpy(srcFileName, appPath);
   xstrcat(srcFileName, "/barbara.jpg");
   xstrcpy(zipFileName, appPath);
   xstrcat(zipFileName, "/barbaraZip.txt");
   xstrcpy(dstFileName, appPath);
   xstrcat(dstFileName, "/barbara2.jpg");

   tmnpZlib.currentContext = currentContext;
   tmnpFileIn.currentContext = currentContext;
   tmnpFileOut.currentContext = currentContext;
   tmnpZlib.i32 = i32Zlib;
   tmnpZlib.obj = objZlib;
   tmnpFileIn.obj = objFileIn;
   tmnpFileIn.i32 = i32FileIn;
   tmnpFileOut.obj = objFileOut;
   tmnpFileOut.i32 = i32FileOut;

   tmnpFileIn.obj[0] = createObject(currentContext, "totalcross.io.File");
   ASSERT1_EQUALS(NotNull, tmnpFileIn.obj[0]);
   File_path(tmnpFileIn.obj[0]) = createStringObjectFromCharP(currentContext, srcFileName, -1);
   tmnpFileIn.obj[1] = File_path(tmnpFileIn.obj[0]);
   ASSERT1_EQUALS(NotNull, tmnpFileIn.obj[1]);
   tmnpFileIn.i32[0] = 5; // CREATE_EMPTY
   tmnpFileIn.i32[1] = -1;
   tiF_create_sii(&tmnpFileIn); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tmnpFileOut.obj[0] = createObject(currentContext, "totalcross.io.File");
   ASSERT1_EQUALS(NotNull, tmnpFileOut.obj[0]);
   File_path(tmnpFileOut.obj[0]) = createStringObjectFromCharP(currentContext, zipFileName, -1);
   tmnpFileOut.obj[1] = File_path(tmnpFileOut.obj[0]);
   ASSERT1_EQUALS(NotNull, tmnpFileOut.obj[1]);
   tmnpFileOut.i32[0] = 5; // CREATE_EMPTY
   tmnpFileOut.i32[1] = -1;
   tiF_create_sii(&tmnpFileOut); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tmnpZlib.i32[0] = 9; // Max compression
   tmnpZlib.i32[1] = 0; // Default strategy.
   tmnpZlib.i32[2] = false; // No wrap.
   tmnpZlib.obj[0] = tmnpFileIn.obj[0];
   tmnpZlib.obj[1] = tmnpFileOut.obj[0];

   tuzZL_deflate_ssiib(&tmnpZlib);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_close(&tmnpFileIn);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_close(&tmnpFileOut);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tmnpFileIn.obj[0] = createObject(currentContext, "totalcross.io.File");
   ASSERT1_EQUALS(NotNull, tmnpFileIn.obj[0]);
   File_path(tmnpFileIn.obj[0]) = createStringObjectFromCharP(currentContext, zipFileName, -1);
   tmnpFileIn.obj[1] = File_path(tmnpFileIn.obj[0]);
   ASSERT1_EQUALS(NotNull, tmnpFileIn.obj[1]);
   tmnpFileIn.i32[0] = 2; // READ_WRITE
   tmnpFileIn.i32[1] = -1;
   tiF_create_sii(&tmnpFileIn); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tmnpFileOut.obj[0] = createObject(currentContext, "totalcross.io.File");
   ASSERT1_EQUALS(NotNull, tmnpFileOut.obj[0]);
   File_path(tmnpFileOut.obj[0]) = createStringObjectFromCharP(currentContext, dstFileName, -1);
   tmnpFileOut.obj[1] = File_path(tmnpFileOut.obj[0]);
   ASSERT1_EQUALS(NotNull, tmnpFileOut.obj[1]);
   tmnpFileOut.i32[0] = 5; // CREATE_EMPTY
   tmnpFileOut.i32[1] = -1;
   tiF_create_sii(&tmnpFileOut); // create file
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tmnpZlib.i32[0] = -1;
   tmnpZlib.i32[1] = false;
   tmnpZlib.obj[0] = tmnpFileIn.obj[0];
   tmnpZlib.obj[1] = tmnpFileOut.obj[0];

   tuzZL_inflate_ssib(&tmnpZlib);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_close(&tmnpFileIn);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   tiF_close(&tmnpFileOut);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
#else
   TEST_SKIP;
#endif

   finish:
      currentContext->thrownException = null;
}
