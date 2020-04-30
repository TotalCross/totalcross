// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

TESTCASE(Socket)
{
#if defined (WIN32) || defined(ANDROID)
   TNMParams p;
   TCObject objArray[2];
   int32 i32Array[3];
   CharP buf;
   CharP msg;
   TCObject* jmsg;
   p.obj = objArray;
   p.i32 = i32Array;
   p.currentContext = currentContext;

   p.obj[0] = createObject(currentContext, "totalcross.net.Socket");
   p.obj[1] = createStringObjectFromCharP(currentContext, "www.superwaba.com.br", -1);
   p.i32[0] = 80;
   p.i32[1] = 30000;
   p.i32[2] = true;

   tnS_socketCreate_siib(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // socket is open.
   ASSERT1_EQUALS(True, Socket_socketRef(p.obj[0]) != null);

   buf = "GET / HTTP/1.0\n\n";
   p.i32[0] = 0;
   p.i32[1] = xstrlen(buf);
   p.obj[1] = createByteArray(currentContext, 1024);
   xmemmove(ARRAYOBJ_START(p.obj[1]), buf, p.i32[1]);

   p.retI = 0;
   Socket_writeTimeout(p.obj[0]) = 3000;
   p.i32[2] = false; // writeBytes!
   tnS_readWriteBytes_Biib(&p);
   if (currentContext->thrownException != null)
   {
      alert("%s", OBJ_CLASS(currentContext->thrownException)->name);
      jmsg = Throwable_msg(currentContext->thrownException);
      msg = String2CharP(*jmsg);
      alert("%s", msg);
      xfree(msg);
   }
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT2_EQUALS(I32, p.i32[1], p.retI);

   p.retI = 0;
   Socket_readTimeout(p.obj[0]) = 3000;
   p.i32[1] = 1024;
   p.i32[2] = true; // readBytes!
   tnS_readWriteBytes_Biib(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   ASSERT1_EQUALS(True, p.retI > 0);
   //ARRAYOBJ_START(p.obj[1])[p.retI] = 0;
   //alert("%s", ARRAYOBJ_START(p.obj[1]));

   tnS_nativeClose(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   // socket was closed.
   ASSERT1_EQUALS(False, Socket_socketRef(p.obj[0]) != null);
#else
   TEST_SKIP;
#endif

   finish: ;
}
