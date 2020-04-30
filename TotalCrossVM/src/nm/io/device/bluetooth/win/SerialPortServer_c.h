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
#if !defined WP8
#pragma pack(8)
#include <ws2bth.h>
#pragma pack()
#if defined (WINCE)
 #include <Bt_sdp.h>

#define BTH_ADDR BT_ADDR

#else
 #include <BluetoothAPIs.h>
#endif
#endif

#define NATIVE_HANDLE SOCKET

static GUID _SerialPortServiceClassID_UUID16 = {0x00001101, 0x0000, 0x1000, { 0x80, 0x00, 0x00, 0x80, 0x5F, 0x9B, 0x34, 0xFB } };

static Err PublishRecord(PBYTE pSDPRec, GUID* pguid2, int32 nRecSize, ULONG *pRecord, BTHNS_SETBLOB* pSetBlob)
{
   ULONG ulSdpVersion = BTH_SDP_VERSION;
   BLOB blob;
   WSAQUERYSET Service;

   // Zero out the record handle that will be returned by the call
   *pRecord = 0;
   
#if defined (WINCE)
   pSetBlob->pRecordHandle = pRecord;
   pSetBlob->fSecurity = 0;
   pSetBlob->fOptions = 0;
#else
   pSetBlob->pRecordHandle = (HANDLE*) pRecord;
#endif
   pSetBlob->pSdpVersion = &ulSdpVersion;
   pSetBlob->ulRecordLength = nRecSize;
   xmemmove(pSetBlob->pRecord, pSDPRec, nRecSize);

   // Init the container blob
   blob.cbSize = sizeof(BTHNS_SETBLOB) + nRecSize - 1;
   blob.pBlobData = (PBYTE) pSetBlob;

   // Init the WSAQuerySet struct
   xmemzero(&Service, sizeof(Service));
   Service.dwSize = sizeof(Service);
   Service.lpBlob = &blob;
   Service.dwNameSpace = NS_BTH;
   Service.lpServiceClassId = pguid2;

   // Publish the service
   return WSASetService(&Service, RNRSERVICE_REGISTER, 0);
}

// auxiliary functions
static Err RegisterBtService(GUID* pguid, GUID* pguid2, byte bChannel, ULONG *pRecord)
{
   // SDP dummy record
   // GUID goes at offset 8
   // Channel goes in last byte of record.
   static BYTE bSDPRecord[] = {
   0x35, 0x27, 0x09, 0x00, 0x01, 0x35, 0x11, 0x1C, 0x00, 0x00, 0x00,
   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
   0x00, 0x00, 0x09, 0x00, 0x04, 0x35, 0x0C, 0x35, 0x03, 0x19, 0x01,
   0x00, 0x35, 0x05, 0x19, 0x00, 0x03, 0x08, 0x00};

   // BTHNS_SETBLOB structure passed to PublishRecord.
   uint8 setBlob[sizeof(BTHNS_SETBLOB) + sizeof(bSDPRecord) - 1];

   // Update the SDP record
   // Translate guid into net byte order for SDP record
   GUID *p = (GUID*) &bSDPRecord[8];
   p->Data1 = htonl(pguid->Data1);
   p->Data2 = htons(pguid->Data2);
   p->Data3 = htons(pguid->Data3);
   xmemmove(p->Data4, pguid->Data4, sizeof(pguid->Data4));

   // Copy channel value into record
   bSDPRecord[sizeof(bSDPRecord) - 1] = bChannel;

   return PublishRecord(bSDPRecord, pguid2, sizeof(bSDPRecord), pRecord, (BTHNS_SETBLOB*) &setBlob);
}

// interface functions
static Err btsppServerCreate(NATIVE_HANDLE* nativeHandle, GUID guid)
{
   WSADATA wsaData;
   SOCKADDR_BTH sockAddrBth;
   struct sockaddr *sockAddrP;

   int32 sockAddrLen;
   WSAQUERYSET wsaQuerySet = {0};
   ULONG recordHandle;
   Err err;

   *nativeHandle = INVALID_SOCKET;
   
   if ((err = WSAStartup(MAKEWORD(2, 2), &wsaData)) != NO_ERROR)
      return err;

   if ((*nativeHandle = socket(AF_BTH, SOCK_STREAM, BTHPROTO_RFCOMM)) == INVALID_SOCKET)
      goto Error;

   xmemzero(&sockAddrBth, sizeof(sockAddrBth));
   sockAddrBth.addressFamily = AF_BTH; // Must be AF_BTH for Bluetooth.
   sockAddrBth.port = 0; // Let the system choose an available port.
   sockAddrP = (struct sockaddr*) &sockAddrBth; // sockaddr* to the bluetooth struct to use with winsock functions.
   
   if (bind(*nativeHandle, sockAddrP, sizeof(SOCKADDR_BTH)) == SOCKET_ERROR)
      goto Error;

   sockAddrLen = sizeof(SOCKADDR_BTH);
   if (getsockname(*nativeHandle, sockAddrP, &sockAddrLen) == SOCKET_ERROR)
      goto Error;

   //
   // As long as we use a blocking accept(), we will have a race
   // between advertising the service and actually being ready to
   // accept connections.  If we use non-blocking accept, advertise
   // the service after accept has been called.
   //
   if ((err = RegisterBtService(&_SerialPortServiceClassID_UUID16, &guid, (byte) sockAddrBth.port, &recordHandle)) != NO_ERROR)
      goto Error;

   //
   // listen() call indicates winsock2 to listen on a given socket for any incoming connection.
   //
   if (listen(*nativeHandle, 4) != NO_ERROR)
      goto Error;

   return NO_ERROR;

Error:
   err = WSAGetLastError();
   if (*nativeHandle != INVALID_SOCKET)
      closesocket(*nativeHandle);
   WSACleanup();

   return err;
}

static Err btsppServerAccept(NATIVE_HANDLE* nativeHandle, NATIVE_HANDLE* clientHandle)
{
   WSADATA wsaData;
   Err err;
   //
   // accept() call indicates winsock2 to wait for any  
   // incoming connection request from a remote socket.
   // If there are already some connection requests on the queue,
   // then accept() extracts the first request and creates a new socket and
   // returns the handle to this newly created socket. This newly created
   // socket represents the actual connection that connects the two sockets.
   //
   if ((*clientHandle = accept(*nativeHandle, null, null)) == INVALID_SOCKET)
   {       
      err = WSAGetLastError();
      if (err == WSAEINTR)
      {
         // WSAEINTR is returned when the socket is closed during a blocking IO operation. In this case, we'll set the handle to null to notify the caller.
         *clientHandle = null;
         return NO_ERROR;
      }
      return err;
   }
   
   if ((err = WSAStartup(MAKEWORD(2, 2), &wsaData)) != NO_ERROR) // we must call WSAStartup again for each client, because they will call WSACleanup on close.
      return err;   
   return NO_ERROR;
}

static Err btsppServerClose(NATIVE_HANDLE* nativeHandle)
{
   Err err = NO_ERROR;

   if (closesocket(*nativeHandle) != 0)
      err = WSAGetLastError();
   WSACleanup();

   return err;
}

