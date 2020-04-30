// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#define UNTEST_SERVER_SOCKET

int32 errors[200];
int32 errorsLen = 0;

TESTCASE(tnSS_serversocketCreate_iiis) // totalcross/net/ServerSocket native void serversocketCreate(int port, int backlog, int timeout, String host) throws totalcross.io.IOException;
{
#ifndef UNTEST_SERVER_SOCKET
   char str[1024];
   SERVER_SOCKET* ssh;
   SOCKET* s = INVALID_SOCKET;
   Err err;
   int32 timeout = 1500;
   int32 bytesReceived;

   err = serverSocketCreate(&ssh, 7070, 50, null);

#if defined WIN32
   alert("IP - %s\nPORT - 7070", GetIP(str));
#endif
   do
   {
      err = serverSocketAccept(ssh, &s, timeout);
   }
   while(s == INVALID_SOCKET);
   alert("accepted");
   // READ AND WRITE ON OPEN CLIENT SOCKET.
#if defined WIN32
   while ((bytesReceived = recv(s, str, 1024, 0)) <= 0);
#endif
   str[bytesReceived] = 0;
   alert("%s", str);
   nativeClose(ssh);
#else
   TEST_CANNOT_RUN;
#endif
   finish: ;
}
TESTCASE(tnSS_accept) // totalcross/net/ServerSocket native public void accept() throws totalcross.io.IOException;
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tnSS_nativeClose) // totalcross/net/ServerSocket native private void nativeClose() throws totalcross.io.IOException;
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tnSS_isOpen) // totalcross/net/ServerSocket native public boolean isOpen();
{
   TEST_SKIP;
   finish: ;
}
