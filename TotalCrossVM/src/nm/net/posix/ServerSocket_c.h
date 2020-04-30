// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#include <netdb.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#ifdef darwin
#include <sys/select.h>
#include <sys/time.h>
#endif

typedef int SOCKET;
typedef int SERVER_SOCKET;
#define INVALID_SOCKET 0

static Err nativeClose(SERVER_SOCKET serverSocketHandle)
{
   if (close(serverSocketHandle) < 0)
      return errno;

   return NO_ERROR;
}

static Err serverSocketCreate(SERVER_SOCKET* serverSocketHandle, int32 port, int32 backlog, CharP addr)
{
   SERVER_SOCKET serverSocketRef;
   struct sockaddr_in internetAddr;
   struct hostent *phostent;
   long arg;
   Err err;
   int yes = 1;

   if ((serverSocketRef = socket(AF_INET, SOCK_STREAM, 0)) < 0)
      goto Error;

   if (setsockopt(serverSocketRef, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(int)) == -1)
      goto Error;

   // Fill out the server socket's address information.
   internetAddr.sin_family = AF_INET;
   if (addr == null)
      internetAddr.sin_addr.s_addr = INADDR_ANY;
   else
   if ((internetAddr.sin_addr.s_addr = inet_addr(addr)) == -1)
   {
      if ((phostent = gethostbyname(addr)) == null)
         goto Error;
      else
      {
         internetAddr.sin_family = phostent->h_addrtype;
         xmemmove(&(internetAddr.sin_addr.s_addr), phostent->h_addr, phostent->h_length);
      }
   }
   internetAddr.sin_port = htons((uint16) port);

   // Set non-blocking
   arg = fcntl(serverSocketRef, F_GETFL, NULL);
   arg |= O_NONBLOCK;
   fcntl(serverSocketRef, F_SETFL, arg);

   if (bind(serverSocketRef, (struct sockaddr *)&internetAddr, sizeof(internetAddr)) < 0)
      goto Error;

   if (listen(serverSocketRef, backlog) < 0)
      goto Error;

   *serverSocketHandle = serverSocketRef;
   return NO_ERROR;

Error: // Close the socket.
   err = errno;
   *serverSocketHandle = INVALID_SOCKET;
   nativeClose(serverSocketRef);
   return err;
}

static Err serverSocketAccept(SERVER_SOCKET serverSocketHandle, SOCKET* socketHandle, int32 timeoutMillis)
{
   fd_set fdReadSet;
   Err err;
   struct timeval timeout;

   FD_ZERO(&fdReadSet);
   FD_SET(serverSocketHandle, &fdReadSet);

   timeout.tv_sec  = (timeoutMillis >= 1000 ? timeoutMillis/1000 : 0);
   timeout.tv_usec = (timeoutMillis < 1000  ? timeoutMillis : timeoutMillis%1000)*1000;

   if (select(serverSocketHandle+1, &fdReadSet, null, null, &timeout) < 0)
      return errno;

   if (FD_ISSET(serverSocketHandle, &fdReadSet))
   {
      Sleep(100);
      if ((*socketHandle = accept(serverSocketHandle, null, null)) < 0)
         return ((err = errno) == EWOULDBLOCK ? NO_ERROR : err);
   }
   return NO_ERROR;
}


CharP GetIP(CharP strIp)
{
   char strHostName[81];
   struct hostent *pHost = null;
   struct in_addr **ppip;
   struct in_addr ip;

   if (gethostname(strHostName, 80)==0)
   {
      pHost = gethostbyname(strHostName);
      if (pHost->h_addrtype == AF_INET)
      {
         ppip=(struct in_addr**)pHost->h_addr_list;

         //Enumarate all addresses
         while (*ppip)
         {
            ip=**ppip;
            xstrcpy(strIp, inet_ntoa(ip));
            ppip++;
            if (*strIp)
               break;
         }
      }
   }

   return strIp;
}
