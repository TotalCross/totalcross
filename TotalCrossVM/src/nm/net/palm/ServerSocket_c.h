/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: ServerSocket_c.h,v 1.26 2011-01-04 13:31:02 guich Exp $

#define INVALID_SOCKET 0
typedef int SERVER_SOCKET;
typedef int SOCKET;

static Err nativeClose(SERVER_SOCKET sRef)
{
   Err err = errNone;
   char buf[64];
   int32 n;

   // First, invoke NetLibShutdown. This informs both sides of the connection that the end of their connection is near.
   // Then invoke NetLibReceive until you have received all pending data. This ensures that there are no packets waiting
   // to be acknowledged. Finally, call NetLibSocketClose to close the socket.
   //
   // reference: http://www.oreilly.com/catalog/palmosnetpro/chapter/ch05.html

   NetLibSocketShutdown(sRef, netSocketDirOutput, -1, &err);
   if (err != errNone)
      return err;

   while ((n = NetLibReceive(sRef, buf, 64, 0, null, 0, -1, &err)) > 0); // clear pending data

   NetLibSocketShutdown(sRef, netSocketDirInput, -1, &err);
   if (err != errNone)
      return err;

   NetLibSocketClose(sRef, -1, &err); // close socket
   return err;
}

static Err serverSocketAccept(SERVER_SOCKET serverSock, SOCKET* newSock, int32 timeoutMillis)
{
   Err err = errNone;
   Int32 timeout = millisToTicks(timeoutMillis);
   NetSocketAddrINType sockAddr;
   Int16 addrLen = sizeof(sockAddr);
   bool ok = false;

   ok = gNETLink != null;
   if (ok) // library is loaded
   {
      if ((*newSock = NetLibSocketAccept(serverSock, (NetSocketAddrType*) &sockAddr, &addrLen, timeout, &err)) == -1)
      {
         *newSock = INVALID_SOCKET;
         if (err != netErrTimeout)
            return err;
      }
      return errNone;
   }
   return netErrNotOpen;
}

Err loadNetLib();

static Err serverSocketCreate(SERVER_SOCKET* serverSocket, int32 port, int32 backlog, CharP addr)
{
   Err err = errNone;
   NetSocketRef socketRef = 0;
   NetSocketAddrINType addrIN;
   NetIPAddr               address = 0;
   NetHostInfoBufType      hostInfo;
   NetHostInfoPtr          hostInfoP;
   Int16 Value = 1;

   UNUSED(addr);

   if (gNETLink == null) // load library
      err = loadNetLib();

   if(err == errNone)
   {
      addrIN.family = netSocketAddrINET;
      addrIN.port = NetHToNS((UInt16) port);

      if (addr != NULL)
      {
         if ((address = NetLibAddrAToIN(addr)) == (NetIPAddr)-1) // cannot parse host address, maybe it's a domain name
         {
            hostInfoP = NetLibGetHostByName(addr, &hostInfo, 1000, &err);
            if (hostInfoP != null) // resolved
               address = *((int32*) hostInfoP->addrListP[0]); // get first ip address
         }
      }
      addrIN.addr = address;

      socketRef = NetLibSocketOpen(netSocketAddrINET, netSocketTypeStream, 0, -1, &err);
      if (err != errNone)
         return err;

      NetLibSocketBind(socketRef, (NetSocketAddrType*) &addrIN, sizeof(addrIN), -1, &err);
      if (err != errNone)
         return err;

      NetLibSocketOptionSet(socketRef, netSocketOptLevelSocket, netSocketOptSockNonBlocking, &Value, sizeof(Value), -1, &err);
      if (err != errNone)
         return err;

      NetLibSocketOptionSet(socketRef, netSocketOptLevelTCP, netSocketOptTCPNoDelay, &Value, sizeof(Value), -1, &err);
      if (err != errNone)
         return err;

      NetLibSocketListen(socketRef, (UInt16) backlog, -1, &err);
      if (err != errNone)
         return err;

      if (socketRef != INVALID_SOCKET && err == errNone) // socket opened
         *serverSocket = socketRef;
   }
   return err;
}
