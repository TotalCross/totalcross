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
 #include "win/SerialPortClient_c.h"
#elif defined ANDROID
 #include "android/SerialPortClient_c.h"
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPC_createSerialPortClient_s(NMParams p) // totalcross/io/device/bluetooth/SerialPortClient native private void createSerialPortClient(String address, int port, String []params) throws totalcross.io.IOException;  
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortClientObj = p->obj[0];
   TCObject addressObj = p->obj[1];
   TCObject paramsArray = p->obj[2];
   int32 channel = p->i32[0];
   TCObject nativeHandleObj;
   NATIVE_HANDLE* nativeHandle; 
   char address[13];
   Err err;

   String2CharPBuf(addressObj, address);

   if ((nativeHandleObj = createByteArray(p->currentContext, sizeof(NATIVE_HANDLE))) != null)
   {
      nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
      if ((err = btsppClientCreate(nativeHandle, address, channel)) != NO_ERROR)
      {
#ifdef ANDROID
         throwException(p->currentContext, IOException, err == BT_INVALID_PASSWORD ? "Invalid password" : "Error connecting to device");
#else
         throwExceptionWithCode(p->currentContext, IOException, err);
#endif         
      }
      else
         SerialPortClient_nativeHandle(serialPortClientObj) = nativeHandleObj;
      setObjectLock(nativeHandleObj, UNLOCKED);
   }
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPC_readBytes_Bii(NMParams p) // totalcross/io/device/bluetooth/SerialPortClient native public int readBytes(byte []b, int offset, int count) throws totalcross.io.IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortClientObj = p->obj[0];
   TCObject byteArrayObj = p->obj[1];
   int32 offset = p->i32[0];
   int32 count = p->i32[1];
   TCObject nativeHandleObj = SerialPortClient_nativeHandle(serialPortClientObj);
   NATIVE_HANDLE* nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
   uint8* byteArrayP = (uint8*) ARRAYOBJ_START(byteArrayObj);
   int32 bytesRead;
   Err err;

   if ((err = btsppClientRead(nativeHandle, byteArrayP, offset, count, &bytesRead)) != NO_ERROR)
      throwExceptionWithCode(p->currentContext, IOException, err);
   p->retI = bytesRead;
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPC_writeBytes_Bii(NMParams p) // totalcross/io/device/bluetooth/SerialPortClient native public int writeBytes(byte []b, int offset, int count) throws totalcross.io.IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortClientObj = p->obj[0];
   TCObject byteArrayObj = p->obj[1];
   int32 offset = p->i32[0];
   int32 count = p->i32[1];
   TCObject nativeHandleObj = SerialPortClient_nativeHandle(serialPortClientObj);
   NATIVE_HANDLE* nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
   uint8* byteArrayP = (uint8*) ARRAYOBJ_START(byteArrayObj);
   int32 bytesWritten;
   Err err;

   if ((err = btsppClientWrite(nativeHandle, byteArrayP, offset, count, &bytesWritten)) != NO_ERROR)
#ifdef ANDROID
      throwException(p->currentContext, IOException, "Error connecting to device");
#else
      throwExceptionWithCode(p->currentContext, IOException, err);
#endif         
   p->retI = bytesWritten;
#else
   p = 0;
#endif
}
//////////////////////////////////////////////////////////////////////////
TC_API void tidbSPC_close(NMParams p) // totalcross/io/device/bluetooth/SerialPortClient native public void close() throws totalcross.io.IOException;
{
#if !defined WP8 && (defined (WIN32) || defined (WINCE) || defined (ANDROID))
   TCObject serialPortClientObj = p->obj[0];
   TCObject nativeHandleObj = SerialPortClient_nativeHandle(serialPortClientObj);
   NATIVE_HANDLE* nativeHandle;
   Err err;

   if (nativeHandleObj == null)
      throwException(p->currentContext, IOException, "Invalid object");
   else
   {
      nativeHandle = (NATIVE_HANDLE*) ARRAYOBJ_START(nativeHandleObj);
      if ((err = btsppClientClose(nativeHandle)) != NO_ERROR)
         throwExceptionWithCode(p->currentContext, IOException, err);
      SerialPortClient_nativeHandle(serialPortClientObj) = null;
   }
#else
   p = 0;
#endif
}

#ifdef ENABLE_TEST_SUITE
//#include "SerialPortClient_c.h"
#endif
