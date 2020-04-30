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
#pragma pack(8)
#include <ws2bth.h>
#pragma pack()
#if defined (WINCE)
// #include <Bthapi.h>
 #include <Bt_sdp.h>

#define BTH_ADDR BT_ADDR

#else
 #include <BluetoothAPIs.h>
#endif

#define NATIVE_HANDLE SOCKET

static Err btsppClientCreate(NATIVE_HANDLE* nativeHandle, CharP address, int32 channel)
{
   WSADATA wsaData;
   SOCKADDR_BTH btSockAddr;
   Err err;
   *nativeHandle = INVALID_SOCKET;

   if ((err = WSAStartup(MAKEWORD(2, 2), &wsaData)) != 0)
      return err;

   if ((*nativeHandle = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM)) == INVALID_SOCKET)
      goto Error;

   xmemzero(&btSockAddr, sizeof(SOCKADDR_BTH));
   btSockAddr.addressFamily = AF_BTH;
   radix2long(address, 16, &(btSockAddr.btAddr));
   btSockAddr.port = channel;
   
   if (connect (*nativeHandle, (struct sockaddr*) &btSockAddr, sizeof(btSockAddr)) != NO_ERROR)
      goto Error;

   return NO_ERROR;

Error:
   err = WSAGetLastError();
   if (*nativeHandle != INVALID_SOCKET)
      closesocket(*nativeHandle);
   WSACleanup();
   return err;
}

static Err btsppClientRead(NATIVE_HANDLE* nativeHandle, uint8* byteArrayP, int32 offset, int32 count, int32* bytesRead)
{
   if ((*bytesRead = recv(*nativeHandle, byteArrayP + offset, count, 0)) == SOCKET_ERROR)
      return WSAGetLastError();
   return NO_ERROR;
}

static Err btsppClientWrite(NATIVE_HANDLE* nativeHandle, uint8* byteArrayP, int32 offset, int32 count, int32* bytesWritten)
{
   if ((*bytesWritten = send(*nativeHandle, byteArrayP + offset, count, 0)) == SOCKET_ERROR)
      return WSAGetLastError();
   return NO_ERROR;
}

static Err btsppClientClose(NATIVE_HANDLE* nativeHandle)
{
   Err err = NO_ERROR;

   if (closesocket(*nativeHandle) != 0)
      err = WSAGetLastError();
   WSACleanup();
   
   return err;
}
