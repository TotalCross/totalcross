// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"
#include "guid.h"

#if defined (WP8)

#elif defined (WIN32) || defined (WINCE)
 #include "win/SerialPortServer_c.h"
#elif defined ANDROID
 #include "android/SerialPortServer_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPS_createSerialPortServer_s(NMParams p) // totalcross/io/device/bluetooth/SerialPortServer native private void createSerialPortServer(String uuid, String []params) throws totalcross.io.IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortServerObj = p->obj[0];
   TCObject uuidObj = p->obj[1];
   TCObject paramsArray = p->obj[2];
   TCObject nativeHandleObj;
   NATIVE_HANDLE* nativeHandle;
   GUID guid;
   Err err;

   if (!String2GUID(uuidObj, &guid))
      throwException(p->currentContext, IllegalArgumentException, "Invalid UUID.");
   else if ((nativeHandleObj = createByteArray(p->currentContext, sizeof(NATIVE_HANDLE))) != null)
   {
      nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
      if ((err = btsppServerCreate(nativeHandle, guid)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else
         SerialPortServer_nativeHandle(serialPortServerObj) = nativeHandleObj;
      setObjectLock(nativeHandleObj, UNLOCKED);
   }
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPS_accept(NMParams p) // totalcross/io/device/bluetooth/SerialPortServer native public totalcross.io.Stream accept() throws totalcross.io.IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortServerObj = p->obj[0];
   TCObject nativeHandleObj = SerialPortServer_nativeHandle(serialPortServerObj);
   NATIVE_HANDLE* nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
   NATIVE_HANDLE* clientHandle;
   TCObject serialPortClientObj;
   TCObject clientHandleObj;
   Err err;

   if ((serialPortClientObj = createObject(p->currentContext, "totalcross.io.device.bluetooth.SerialPortClient")) != null
    && (clientHandleObj = createByteArray(p->currentContext, sizeof(NATIVE_HANDLE))) != null)
   {
      clientHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(clientHandleObj);
      if ((err = btsppServerAccept(nativeHandle, clientHandle)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      else if (*clientHandle == null)
      {
         // the handle is null, that means the socket was closed during this IO blocking operation. we'll throw a different exception in this case.
         throwExceptionWithCode(p->currentContext, SocketTimeoutException, err);
      }
      else
      {
         SerialPortClient_nativeHandle(serialPortClientObj) = clientHandleObj;
         p->retO = serialPortClientObj;
      }
      setObjectLock(clientHandleObj, UNLOCKED); // it will be unlocked by the client's close.
   }
   setObjectLock(serialPortClientObj, UNLOCKED);
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPS_close(NMParams p) // totalcross/io/device/bluetooth/SerialPortServer native public void close() throws throws totalcross.io.IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortServerObj = p->obj[0];
   TCObject nativeHandleObj = SerialPortServer_nativeHandle(serialPortServerObj);
   NATIVE_HANDLE* nativeHandle;
   Err err;

   if (nativeHandleObj == null)
      throwException(p->currentContext, IOException, "Invalid object");
   else
   {
      nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
      if ((err = btsppServerClose(nativeHandle)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      SerialPortServer_nativeHandle(serialPortServerObj) = null;
   }
#else
   p = 0;
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "SerialPortServer_c.h"
#endif
