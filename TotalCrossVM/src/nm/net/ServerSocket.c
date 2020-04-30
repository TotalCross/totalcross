// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "Net.h"

#if defined WINCE || defined WIN32
 #include "win/ServerSocket_c.h"
#else
 #include "posix/ServerSocket_c.h"
#endif

static void invalidate(TCObject obj)
{
   if (ServerSocket_serverRef(obj) != null)
   {
      setObjectLock(ServerSocket_serverRef(obj), UNLOCKED);
      ServerSocket_serverRef(obj) = null;
   }
   ServerSocket_dontFinalize(obj) = true;
}

static void invalidateSocket(TCObject* obj)
{
   if (Socket_socketRef(*obj) != null)
   {
      setObjectLock(Socket_socketRef(*obj), UNLOCKED);
      Socket_socketRef(*obj) = null;
   }
   Socket_dontFinalize(*obj) = true;
   *obj = null;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tnSS_serversocketCreate_iiis(NMParams p) // totalcross/net/ServerSocket native void serversocketCreate(int port, int backlog, String host) throws totalcross.io.IOException;
{
   TCObject serverSocket = p->obj[0];
   TCObject serverSocketRef;
   SERVER_SOCKET* serverSocketHandle;
   TCObject address = p->obj[1];
   int32 port = p->i32[0];
   int32 backlog = p->i32[1];
   CharP szAddress = null;
   Err err;

   if (address)
      szAddress = String2CharP(address);

   if ((serverSocketRef = createByteArray(p->currentContext, sizeof(SERVER_SOCKET))) != null)
   {
      ServerSocket_serverRef(serverSocket) = serverSocketRef;
      serverSocketHandle = (SERVER_SOCKET*) ARRAYOBJ_START(serverSocketRef);
      if ((err = serverSocketCreate(serverSocketHandle, port, backlog, szAddress)) != NO_ERROR)
      {
         throwExceptionWithCode(p->currentContext, IOException, err);
         invalidate(serverSocket);
      }
   }
   xfree(szAddress);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnSS_nativeAccept(NMParams p) // totalcross/net/ServerSocket native public void nativeAccept() throws totalcross.io.IOException;
{
   TCObject serverSocket = p->obj[0];
   TCObject serverSocketRef = ServerSocket_serverRef(serverSocket);
   SERVER_SOCKET* serverSocketHandle;
   TCObject socketRef;
   SOCKET* socketHandle;
   TCObject newSocket = null;
   int32 timeout = ServerSocket_timeout(serverSocket);
   Err err;

   newSocket = createObject(p->currentContext, "totalcross.net.Socket");
   if (newSocket != null && (socketRef = createByteArray(p->currentContext, sizeof(SOCKET))) != null)
   {
      Socket_socketRef(newSocket) = socketRef;
      serverSocketHandle = (SERVER_SOCKET*) ARRAYOBJ_START(serverSocketRef);
      socketHandle = (SOCKET*) ARRAYOBJ_START(socketRef);
      *socketHandle = INVALID_SOCKET;

      if ((err = serverSocketAccept(*serverSocketHandle, socketHandle, timeout)) != NO_ERROR)
      {
         throwExceptionWithCode(p->currentContext, IOException, err);
         invalidateSocket(&newSocket);
      }
      else
      if (*socketHandle == INVALID_SOCKET)
         invalidateSocket(&newSocket);
   }
   if (newSocket)
   {
      p->retO = newSocket;
      setObjectLock(p->retO, UNLOCKED);
   }
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnSS_nativeClose(NMParams p) // totalcross/net/ServerSocket native private void nativeClose() throws totalcross.io.IOException;
{
   TCObject serverSocket = p->obj[0];
   TCObject serverSocketRef = ServerSocket_serverRef(serverSocket);
   SERVER_SOCKET* serverSocketHandle;
   Err err;

   serverSocketHandle = (SERVER_SOCKET*) ARRAYOBJ_START(serverSocketRef);
   if ((err = nativeClose(*serverSocketHandle)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   invalidate(serverSocket);
}


#ifdef ENABLE_TEST_SUITE
#include "ServerSocket_test.h"
#endif
