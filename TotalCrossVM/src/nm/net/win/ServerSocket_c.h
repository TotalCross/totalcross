// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "winsockLib.h"

typedef SOCKET SERVER_SOCKET;

static Err nativeClose(SERVER_SOCKET serverSocketHandle)
{
   if (closesocket(serverSocketHandle) == SOCKET_ERROR)
      return WSAGetLastError();
   if (WSACleanup() == SOCKET_ERROR)
      return WSAGetLastError();
   return NO_ERROR;
}

static Err serverSocketCreate(SERVER_SOCKET* serverSocketHandle, int32 port, int32 backlog, CharP addr)
{
   SERVER_SOCKET serverSocketRef = 0;
   SOCKADDR_IN internetAddr;
   WSADATA WSAData;
   PHOSTENT phostent;
   ULONG NonBlock;
   Err err;

   // Initialize Winsocket.
   if (WSAStartup(MAKEWORD(1,1), &WSAData) != 0)
      goto Error;

   if ((serverSocketRef = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
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

   NonBlock = 1;
   if (ioctlsocket(serverSocketRef, FIONBIO, &NonBlock) == SOCKET_ERROR)
      goto Error;

   if (bind(serverSocketRef, (SOCKADDR *) &internetAddr, sizeof(internetAddr)) == SOCKET_ERROR)
      goto Error;

   if (listen(serverSocketRef, backlog) == SOCKET_ERROR)
      goto Error;

   *serverSocketHandle = serverSocketRef;
   return NO_ERROR;

Error: // Close the socket.
   err = WSAGetLastError();
   *serverSocketHandle = INVALID_SOCKET;
   nativeClose(serverSocketRef);
   return err;
}

static Err serverSocketAccept(SERVER_SOCKET serverSocketHandle, SOCKET* socketHandle, int32 timeoutMillis)
{
   FD_SET fdReadSet;
   WSADATA WSAData;
   Err err;
   TIMEVAL timeout;

   FD_ZERO(&fdReadSet);
   FD_SET(serverSocketHandle, &fdReadSet);

   timeout.tv_sec  = (timeoutMillis >= 1000 ? timeoutMillis/1000 : 0);
   timeout.tv_usec = (timeoutMillis < 1000  ? timeoutMillis : timeoutMillis%1000)*1000;

   if (select(0, &fdReadSet, null, null, &timeout) == SOCKET_ERROR)
      return WSAGetLastError();

   if (FD_ISSET(serverSocketHandle, &fdReadSet))
   {
      Sleep(100);
      if ((*socketHandle = accept(serverSocketHandle, null, null)) != INVALID_SOCKET)
         WSAStartup(MAKEWORD(1,1), &WSAData);
      else
         return ((err = WSAGetLastError()) == WSAEWOULDBLOCK ? NO_ERROR : err);
   }
   return NO_ERROR;
}


CharP GetIP(CharP strIp)
{
   char strHostName[81];
   HOSTENT *pHost = null;
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
				if (strIp!="") {
					break;
				}
			}
		}
	}

	return strIp;
}