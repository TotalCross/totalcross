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
 #include "win/Socket_c.h"
#else
 #include "posix/Socket_c.h"
#endif

static void invalidate(TCObject obj)
{
   if (Socket_socketRef(obj) != null)
      Socket_socketRef(obj) = null;
   Socket_dontFinalize(obj) = true;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tnS_socketCreate_siib(NMParams p) // totalcross/net/Socket native void socketCreate(final String host, final int port, final int timeout, final boolean noLinger);
{
   TCObject socket = p->obj[0];
   TCObject socketRef;
   SOCKET* socketHandle;
   TCObject host = p->obj[1];
   int32 port = p->i32[0];
   int32 timeout = p->i32[1];
   bool noLinger = p->i32[2];
   CharP szHost;
   bool isUnknownHost = false;
   bool timedOut = false;
   Err err;

   if (host == null)
   {
      szHost = (CharP) xmalloc(10);
      if (szHost != null)
         xstrcpy(szHost, "localhost");
   }
   else szHost = String2CharP(host);
   if (szHost == null)
   {
      throwException(p->currentContext, OutOfMemoryError, null);
      return;
   }

   if ((socketRef = createByteArray(p->currentContext, sizeof(SOCKET))) != null)
   {
      Socket_socketRef(socket) = socketRef;
      setObjectLock(socketRef, UNLOCKED);
      socketHandle = (SOCKET*) ARRAYOBJ_START(socketRef);
      if ((err = socketCreate(socketHandle, szHost, port, timeout, noLinger, &isUnknownHost, &timedOut)) != NO_ERROR)
      {
         if (isUnknownHost)
            throwException(p->currentContext, UnknownHostException, szHost);
         else if (timedOut)
            throwException(p->currentContext, IOException, "Socket creation timed out.");
         else
            throwExceptionWithCode(p->currentContext, IOException, err);
         invalidate(socket);
      }
   }
   xfree(szHost);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnS_nativeClose(NMParams p) // totalcross/net/Socket native private void nativeClose() throws totalcross.io.IOException;
{
   TCObject socket = p->obj[0];
   TCObject socketRef = Socket_socketRef(socket);
   SOCKET* socketHandle = (SOCKET*) ARRAYOBJ_START(socketRef);
   Err err;

   if ((err = socketClose(socketHandle)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   invalidate(socket);
}
//////////////////////////////////////////////////////////////////////////
TC_API void tnS_readWriteBytes_Biib(NMParams p) // totalcross/net/Socket native private int readWriteBytes(byte []buf, int start, int count, boolean isRead) throws totalcross.io.IOException;
{
   TCObject socket = p->obj[0];
   TCObject socketRef = Socket_socketRef(socket);
   SOCKET* socketHandle;
   TCObject buf = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   bool isRead = p->i32[2];
   int32 timeout;
   int32 retCount;
   Err err;

   if (isRead)
      timeout = Socket_readTimeout(socket);
   else
      timeout = Socket_writeTimeout(socket);

   socketHandle = (SOCKET*) ARRAYOBJ_START(socketRef);
   if ((err = socketReadWriteBytes(*socketHandle, timeout, (CharP)ARRAYOBJ_START(buf), start, count, &retCount, isRead)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   else if (retCount == -2) // timeout!
      throwException(p->currentContext, SocketTimeoutException, "Operation timed out");
   else if (isRead && retCount == 0 && count > 0)
      p->retI = -1;
   else
      p->retI = retCount;
}
//////////////////////////////////////////////////////////////////////////
// Used by axTLS as socket I/O function.
int tcSocketReadWrite(int fd, CharP buf, int32 count, bool isRead)
{
   int32 written = 0;
   int32 retCount;
   TCObject socket;
   int32 timeout;
   Err err;

   LOCKVAR(htSSL);
   socket = (TCObject)htGetPtr(&htSSLSocket, fd);
   UNLOCKVAR(htSSL);

   if (!socket) // guich@tc113_14
      return -1;

   if (isRead)
      timeout = Socket_readTimeout(socket);
   else
      timeout = Socket_writeTimeout(socket);

   do
   {
      err = socketReadWriteBytes(fd, timeout, buf, 0 + written, count - written, &retCount, isRead);
      if (retCount == 0) // Gracefully closed by the remote, just break the loop.
         break;
      if (retCount > 0)
         written += retCount;
   }
   while (written < count && err == NO_ERROR);
   return (err == NO_ERROR) ? written : -1;
}

#ifdef ENABLE_TEST_SUITE
#include "Socket_test.h"
#endif
