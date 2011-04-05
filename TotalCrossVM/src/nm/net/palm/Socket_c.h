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



typedef int SOCKET;
#define INVALID_SOCKET 0

Err loadNetLib()
{
   Err err;
   UInt16 count, netIFErr;
   UInt8 allUp;
   if ((err = LinkModule(netLibType, netLibCreator, &gNETLink, NULL)) == errNone)
   {
      if ((err = NetLibOpenCount(&count)) == errNone)
      {
         if (count == 0) // lib is not opened
         {
            err = NetLibOpen(&netIFErr);
            allUp = netIFErr == 0;
         }
         else
            err = NetLibConnectionRefresh(true, &allUp, &netIFErr);

         if (!allUp) // could not bring all connections up
            err = netErrInterfaceDown;
      }
   }
   if (err != errNone) // cannot open library
      gNETLink = null;
   return err;
}

static Err socketCreate(SOCKET* socketHandle, CharP host, int32 port, int32 timeout, bool noLinger, bool *isUnknownHost)
{
   Err err = errNone;
   NetSocketRef sRef = 0;
   int32 ipAddr;
   NetSocketAddrINType addrIN;
   NetHostInfoBufType hostInfo;
   NetHostInfoPtr hostInfoP;
   NetSocketLingerType linger;

   linger.onOff = noLinger;
   linger.time = 0;
   timeout = timeout >= 0 ? millisToTicks(timeout) : -1;

   if (gNETLink == null) // load library
      err = loadNetLib();

   if (err == errNone)
   {
      ipAddr = NetLibAddrAToIN(host);
      if (ipAddr == -1) // cannot parse host address, maybe it's a domain name
      {
         hostInfoP = NetLibGetHostByName(host, &hostInfo, timeout, &err);
         if (hostInfoP != null) // resolved
            ipAddr = *((int32*) hostInfoP->addrListP[0]); // get first ip address
         else
         {
            if (err == netErrDNSBadName || err == netErrDNSBadName || err == netErrDNSNonexistantName)
               *isUnknownHost = true;
         }
      }

      if (ipAddr != -1)
      {
         addrIN.family = netSocketAddrINET;
         addrIN.addr = (NetIPAddr) ipAddr;
         addrIN.port = NetHToNS((UInt16) port);

         sRef = NetLibSocketOpen(netSocketAddrINET, netSocketTypeStream, netSocketProtoIPTCP, timeout, &err);
         if (sRef != -1) // socket opened
         {
            if (noLinger)
               NetLibSocketOptionSet(sRef, netSocketOptLevelSocket, netSocketOptSockLinger, &linger, sizeof(linger), -1, &err);
            NetLibSocketConnect(sRef, (NetSocketAddrType*) &addrIN, sizeof(addrIN), timeout, &err); // connect
         }
      }
   }

   if (sRef != 0 && err == errNone) // socket opened and connected
      *socketHandle = sRef;

   return err;
}

static Err socketClose(SOCKET* socketHandle)
{
   Err err = errNone;
   NetSocketRef sRef;
   char buf[64];
   int32 n, timeout;

   sRef = (NetSocketRef) *socketHandle;
   timeout = millisToTicks(5000); // FIX ME: Decide what timeout to use!

   // First, invoke NetLibShutdown. This informs both sides of the connection that the end of their connection is near.
   // Then invoke NetLibReceive until you have received all pending data. This ensures that there are no packets waiting
   // to be acknowledged. Finally, call NetLibSocketClose to close the socket.
   //
   // reference: http://www.oreilly.com/catalog/palmosnetpro/chapter/ch05.html

   NetLibSocketShutdown(sRef, netSocketDirOutput, timeout, &err);
   while ((n = NetLibReceive(sRef, buf, 64, 0, null, 0, timeout, &err)) > 0); // clear pending data
   NetLibSocketShutdown(sRef, netSocketDirInput, timeout, &err);

   NetLibSocketClose(sRef, timeout, &err); // close socket

   if (err == errNone)
      *socketHandle = 0;

   return err;
}

static Err socketReadWriteBytes(SOCKET socketHandle, int32 timeoutMillis, CharP buf, int32 off, int32 len, int32* retCount, bool isRead)
{
   NetSocketRef sRef = (NetSocketRef) socketHandle;
   Err err = errNone;
   int32 timeout;
   int32 result;
   int32 timeoutLeft = timeoutMillis >= 0 ? timeoutMillis : 0; // we should enter the read/write loop at least once, even if timeout <= 0
   *retCount = 0; // clear bytes count

   timeout = millisToTicks(timeoutLeft);

   if (isRead)
   {
      result = NetLibReceive  (sRef, buf + off + *retCount, len - *retCount, 0, null, 0, timeout, &err); //Read
      if (result == 0) // flsobral@tc110_2: if result is 0, the connection was gracefully closed by the remote host.
         return NO_ERROR;
   }
   else
   {
      result = NetLibSend     (sRef, buf + off + *retCount, len - *retCount, 0, null, 0, timeout, &err); //Write
      if (result == 0) // flsobral@tc110_2: if the result is 0, the connection was gracefully closed by the remote host. However, in this case this is not expected by the user, unlike th read operation.
         return netErrSocketClosedByRemote;
   }

   if (result == -1)
   {
      if (err == netErrTimeout)  // operation timed out, update retCount and return
      {
         *retCount = -2; // indicating it was a timeout.
         return NO_ERROR; // do not display platform specific error.
      }
      if (err != errNone)   // something went wrong, return the error code.
         return err;

      // according to the documentation, these functions may fail and report that no error occurred.
      // if this is the case, we'll just ignore and set result to 0.
      result = 0;
   }

   *retCount += result; // update the number of bytes write/read

   return errNone;
}

