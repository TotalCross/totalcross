// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "device_PortConnector.h"

#if defined (WP8)

#elif defined (WINCE) || defined (WIN32)
 #include "win/device_PortConnector_c.h"
#elif defined SYMBIAN
 #include "symbian/device_PortConnector_c.h"
#elif defined MACOS
 #include "mac/device_PortConnector_c.h"
#elif defined linux || defined ANDROID
 #include "linux/device_PortConnector_c.h"
#endif

static void invalidate(TCObject obj)
{
   if (PortConnector_portConnector(obj) != null)
   {
      setObjectLock(PortConnector_portConnector(obj), UNLOCKED);
      PortConnector_portConnector(obj) = null;
   }
   if (PortConnector_receiveBuffer(obj) != null)
   {
      setObjectLock(PortConnector_receiveBuffer(obj), UNLOCKED);
      PortConnector_receiveBuffer(obj) = null;
   }
   PortConnector_dontFinalize(obj) = true;
}

//////////////////////////////////////////////////////////////////////////
TC_API void tidPC_create_iiiii(NMParams p) // totalcross/io/device/PortConnector native void create(int number, int baudRate, int bits, int parity, int stopBits);
{
#if !defined WP8
   TCObject portConnector = p->obj[0];
   int32 number = p->i32[0];
   int32 baudRate = p->i32[1];
   int32 bits = p->i32[2];
   int32 parity = p->i32[3];
   int32 stopBits = p->i32[4];

   TCObject portConnectorRef;
   PortHandle* portConnectorHandle;
   TCObject receiveBufferObj;
   VoidP* receiveBuffer;
   Err err;

   int32 timeout = 200;

   portConnectorRef = createByteArray(p->currentContext, sizeof(PortHandle));
   receiveBufferObj = createByteArray(p->currentContext, sizeof(VoidP));
   if (portConnectorRef != null && receiveBufferObj != null)
   {
      PortConnector_portConnector(portConnector) = portConnectorRef;
      PortConnector_receiveBuffer(portConnector) = receiveBufferObj;
      portConnectorHandle = (PortHandle*) ARRAYOBJ_START(portConnectorRef);
      receiveBuffer = (VoidP*) ARRAYOBJ_START(receiveBufferObj);
      if ((err = portConnectorCreate(portConnectorHandle, *receiveBuffer, number, baudRate, bits, parity, stopBits, timeout)) != NO_ERROR)
      {
         throwExceptionWithCode(p->currentContext, IOException, err);
         invalidate(portConnector);
      }
   }
   else invalidate(portConnector);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidPC_nativeClose(NMParams p) // totalcross/io/device/PortConnector native private void nativeClose();
{
#if !defined WP8
   TCObject portConnector = p->obj[0];

   TCObject portConnectorRef = PortConnector_portConnector(portConnector);
   PortHandle* portConnectorHandle = (PortHandle*) ARRAYOBJ_START(portConnectorRef);

   TCObject receiveBufferObj = PortConnector_receiveBuffer(portConnector);
   VoidP* receiveBuffer = (VoidP*) ARRAYOBJ_START(receiveBufferObj);
   int32 portNumber = PortConnector_portNumber(portConnector);
   Err err;

   if ((err = portConnectorClose(*portConnectorHandle, *receiveBuffer, portNumber)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   invalidate(portConnector);
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidPC_setFlowControl_b(NMParams p) // totalcross/io/device/PortConnector native public void setFlowControl(boolean on);
{
#if !defined WP8
   TCObject portConnector = p->obj[0];
   bool flowOn = p->i32[0]; // note: this is also used in Palm OS!

   TCObject portConnectorRef = PortConnector_portConnector(portConnector);
   PortHandle* portConnectorHandle;

   Err err;

   if (portConnectorRef == null)
      throwException(p->currentContext, IOException, "The port is not open.");
   else
   {
      portConnectorHandle = (PortHandle*) ARRAYOBJ_START(portConnectorRef);
      if ((err = portConnectorSetFlowControl(*portConnectorHandle, flowOn)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
   }
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidPC_readWriteBytes_Biib(NMParams p)
{
#if !defined WP8
   TCObject portConnector = p->obj[0];
   TCObject byteArray = p->obj[1];
   int32 start = p->i32[0];
   int32 count = p->i32[1];
   bool isRead = p->i32[2];

   TCObject portConnectorRef = PortConnector_portConnector(portConnector);
   PortHandle* portConnectorHandle = (PortHandle*) ARRAYOBJ_START(portConnectorRef);

   int32 portNumber = PortConnector_portNumber(portConnector);
   bool stopWriteCheckOnTimeout = PortConnector_stopWriteCheckOnTimeout(portConnector);
   int32 timeout;
   int32 retCount;
   Err err;

   if (isRead)
      timeout = PortConnector_readTimeout(portConnector);
   else
      timeout = PortConnector_writeTimeout(portConnector);

   if ((err = portConnectorReadWriteBytes(*portConnectorHandle, portNumber, stopWriteCheckOnTimeout, timeout, ARRAYOBJ_START(byteArray), start, count, &retCount, isRead)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   else
      p->retI = retCount;
#endif
}

//////////////////////////////////////////////////////////////////////////
TC_API void tidPC_readCheck(NMParams p) // totalcross/io/device/PortConnector native public int readCheck();
{
#if !defined WP8
   TCObject portConnector = p->obj[0];

   TCObject portConnectorRef = PortConnector_portConnector(portConnector);
   PortHandle* portConnectorHandle;

   int32 inQueue;
   Err err;

   if (portConnectorRef == null)
      throwException(p->currentContext, IOException, "The port is not open.");
   else
   {
      portConnectorHandle = (PortHandle*) ARRAYOBJ_START(portConnectorRef);
      if ((err = portConnectorReadCheck(*portConnectorHandle, &inQueue)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         p->retI = inQueue;
   }
#endif
}

#ifdef ENABLE_TEST_SUITE
#include "device_PortConnector_test.h"
#endif
