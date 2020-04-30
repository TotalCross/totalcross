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

static Err socketClose(SOCKET* socketHandle);

/*****   socketCreate   *****
 *
 * SOCKET
 * SOCKADDR_IN
 * PHOSTENT
 * WSADATA
 * FD_SET
 * TIMEVAL
 *
 * FD_ZERO
 * FD_SET
 * FD_ISSET
 *
 * WSAStartup
 * socket
 * gethostbyname
 * htons
 * ioctlsocket
 * connect
 * select
 * WSAGetLastError
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winsock2.h.
 * Link Library: Ws2.lib. (Ws2_32.lib on WIN32)
 *
 *************************************/
static Err socketCreate(SOCKET* socketHandle, CharP hostname, int32 port, int32 timeout, bool noLinger, bool *isUnknownHost, bool *timedOut)
{
   SOCKET hostSocket = INVALID_SOCKET; // Socket bound to the server
   SOCKADDR_IN destination_sin;        // Server socket address
   PHOSTENT phostent = null;           // Points to the HOSTENT structure of the server
   WSADATA WSAData;                    // Contains details of the Winsocket implementation
   uint32 NonBlock = 1;
   FD_SET fdWriteSet;
   TIMEVAL timeout_val;
   Err err = NO_ERROR;

   // Initialize Winsocket.
   if (WSAStartup(MAKEWORD(1,1), &WSAData) != 0)
      goto Error;

   // Create a TCP/IP socket that is bound to the server.
   if ((hostSocket = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
      goto Error;

   // Fill out the server socket's address information.
   destination_sin.sin_family = AF_INET;
   //destination_sin.sin_addr.s_addr = htonl(INADDR_ANY);
   if ((destination_sin.sin_addr.s_addr = inet_addr(hostname)) == INADDR_NONE)
   {
      if ((phostent = gethostbyname(hostname)) == null)
      {
         if ((err = WSAGetLastError()) == WSAHOST_NOT_FOUND)
            *isUnknownHost = true;
         goto Finish;
      }
      else
      {
         destination_sin.sin_family = phostent->h_addrtype;
         xmemmove(&(destination_sin.sin_addr.s_addr), phostent->h_addr, phostent->h_length);
      }
   }
   // Convert to network ordering.
   destination_sin.sin_port = htons((uint16) port);

   if (ioctlsocket(hostSocket, FIONBIO, &NonBlock) == SOCKET_ERROR)
      goto Error;

   FD_ZERO(&fdWriteSet);
   FD_SET(hostSocket, &fdWriteSet);

   timeout_val.tv_sec = timeout / 1000;
   timeout_val.tv_usec = (timeout % 1000) * 1000;

   if (connect(hostSocket, (PSOCKADDR)&destination_sin, sizeof(destination_sin)) != 0)
   {
      if ((err = WSAGetLastError()) != WSAEWOULDBLOCK)
         goto Finish;
      if (select(0, null, &fdWriteSet, null, &timeout_val) == SOCKET_ERROR)
         goto Error;
      if (!FD_ISSET(hostSocket, &fdWriteSet))
      {
         err = WSAETIMEDOUT; // flsobral@tc100b5_7: select sets the last error to S_OK, even when the connection times out.
         *timedOut = true;
         goto Finish;
      }
   }

   *socketHandle = hostSocket;
   err = NO_ERROR;
   return err;

Error:
   err = WSAGetLastError();
Finish: // Close the socket.
   socketClose(&hostSocket);
   return err;
}

/*****   socketClose   *****
 *
 * shutdown
 * closesocket
 * WSACleanup
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winsock2.h.
 * Link Library: Ws2.lib. (Ws2_32.lib on WIN32)
 *
 *************************************/
static Err socketClose(SOCKET* socketHandle)
{
   if (shutdown(*socketHandle, SD_SEND) == SOCKET_ERROR)
      goto error;
   if (closesocket(*socketHandle) == SOCKET_ERROR)
      goto error;
   if (WSACleanup() == SOCKET_ERROR)
      goto error;

   *socketHandle = INVALID_SOCKET;
   return NO_ERROR;
error:
   return WSAGetLastError();
}

/*****   socketReadWriteBytes   *****
 *
 * FD_SET
 * TIMEVAL
 *
 * FD_ZERO
 * FD_SET
 *
 * select
 * recv
 * send
 * WSAGetLastError
 *
 * OS Versions: Windows CE 1.0 and later.
 * Header: Winsock2.h.
 * Link Library: Ws2.lib. (Ws2_32.lib on WIN32)
 *
 *************************************/

/**
 * This function implements a timeout for reading and writing bytes using timestamp because
 * this timeout for these operations is only supported by WindowsCE .NET, Windows 2000 and
 * later versions.
 * The function should be improved for these devices, and use winsock2 instead of winsock
 * when available.
 * For now, this will work for all Windows platforms.
 */
static Err socketReadWriteBytes(SOCKET socketHandle, int32 timeoutMillis, CharP buf, int32 start, int32 count, int32* retCount, bool isRead)
{
   FD_SET fdSet;
   TIMEVAL timeout;
   int32 result;
   int32 timestamp;
   Err err;
   *retCount = 0; // clear bytes count

   timestamp = getTimeStamp();

   FD_ZERO(&fdSet);
   FD_SET(socketHandle, &fdSet);

   timeout.tv_sec  = timeoutMillis / 1000;
   timeout.tv_usec = (timeoutMillis % 1000) * 1000;

   if (isRead)
      result = select(0, &fdSet, null, null, &timeout); //Read
   else
      result = select(0, null, &fdSet, null, &timeout); //Write

   if (result == 0)  // select timed out, update retCount and return
   {
      *retCount = -2; // indicating it was a timeout.
      return NO_ERROR; // do not display platform specific error.
   }
   else if (result == SOCKET_ERROR)          // select failed, return the error code.
      goto Error;
   else if (!FD_ISSET(socketHandle, &fdSet)) // select returned but socket is not set? return the error code.
      goto Error;

   do // flsobral@tc113_33: loop back only on WSAEWOULDBLOCK, respecting the timeout.
   {
      if (isRead)
      {
         result = recv(socketHandle, buf + start + *retCount, count - *retCount, 0); //Read
         if (result == 0) // flsobral@tc110_2: if result is 0, the connection was gracefully closed by the remote host.
            return NO_ERROR;
      }
      else
         result = send(socketHandle, buf + start + *retCount, count - *retCount, 0); //Write
      *retCount += result; // update the number of bytes write/read
      Sleep(1); // guich@tc122_5: tell the system that we're alive
   } while (result == SOCKET_ERROR && (err = WSAGetLastError()) == WSAEWOULDBLOCK && (getTimeStamp() - timestamp < timeoutMillis));
   
   if (result == SOCKET_ERROR && err != WSAEWOULDBLOCK)
      return err;

   return NO_ERROR;
Error:
   return WSAGetLastError();
}
