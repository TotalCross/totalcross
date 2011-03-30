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



#include <SerialMgr.h>

typedef int32 PortHandle;
#define INVALID_HANDLE_VALUE 0

#define RECEIVE_BUFFER_SIZE 50000

static Err portConnectorClose(PortHandle portConnectorRef, VoidP receiveBuffer, int32 portNumber)
{
   Err err;
   UInt16 refNum = portConnectorRef;

   SrmSendWait(refNum); // new Serial Manager
   if (portNumber == SERIAL_SIR)
      SrmControl(refNum, srmCtlIrDADisable, 0, 0);

   SrmSetReceiveBuffer(refNum, null, 0);
   err = SrmClose(refNum);

   if (receiveBuffer != null)
   {
      MemPtrFree(receiveBuffer);
      receiveBuffer = null;
   }

   return err;
}

static Err portConnectorCreate(PortHandle* portConnectorRef, VoidP receiveBuffer, int32 number, int32 baudRate, int32 bits, int32 parity, int32 stopBits, int32 writeTimeOutValue)
{
   Err err;
   UInt32 port;
   UInt16 refNum = INVALID_HANDLE_VALUE;
   UInt16 sizeP;
   Int32 flags;

   switch (number)
   {
      case SERIAL_DEFAULT:    port = serPortCradlePort;  break;
      case SERIAL_IRCOMM:     port = 'ircm';             break;
      case SERIAL_SIR:        port = serPortIrPort;      break;
      case SERIAL_USB:        port = 'HsUd';             break;
      case SERIAL_BLUETOOTH:  port = 'rfcm';             break;
      default:                port = (UInt32) number;    break;
   }

   if ((err = SrmOpen(port, (UInt32) baudRate, &refNum)) == errNone)
   {
      if (number != SERIAL_SIR || (err = SrmControl(refNum, srmCtlIrDAEnable, 0, 0) == errNone))
      {
         sizeP = sizeof(Int32);
         if ((err = SrmControl(refNum, srmCtlSetBaudRate, &baudRate, &sizeP)) == errNone) // set baudrate
         {
            flags = 0;
            if (number != SERIAL_SIR) // no handshake via SIR
               flags |= srmSettingsFlagRTSAutoM | srmSettingsFlagCTSAutoM;
            if (bits == 8)
               flags |= srmSettingsFlagBitsPerChar8;
            else
            if (bits == 7)
               flags |= srmSettingsFlagBitsPerChar7;
            else
            if (bits == 6)
               flags |= srmSettingsFlagBitsPerChar6;
            else
            if (bits == 5)
               flags |= srmSettingsFlagBitsPerChar5;

            if (parity != 0)
            {
               flags |= srmSettingsFlagParityOnM;
               flags |= (parity == 1) ? srmSettingsFlagParityEvenM : srmSettingsFlagXonXoffM;
            }
            if (stopBits == 1)
               flags |= srmSettingsFlagStopBits1;
            else
            if (stopBits == 2)
               flags |= srmSettingsFlagStopBits2;

            sizeP = sizeof(Int32);
            if ((err = SrmControl(refNum, srmCtlSetFlags, &flags, &sizeP)) == errNone) // set flags
            {
               if ((err = SrmControl(refNum, srmCtlSetCtsTimeout, &writeTimeOutValue, &sizeP)) == errNone) // set write timeout
               {
                  if (receiveBuffer == null) // create receive buffer
                     receiveBuffer = MemPtrNew(RECEIVE_BUFFER_SIZE);
                  if (receiveBuffer != null)
                     err = SrmSetReceiveBuffer(refNum, receiveBuffer, RECEIVE_BUFFER_SIZE);
                  else
                  {
                     portConnectorClose(*portConnectorRef, receiveBuffer, number);
                     return -1;
                  }
               }
            }
         }
      }
      else
      {
		   err = SrmClose(refNum); // error: close port before returning
      }
   }

   *portConnectorRef = (err == errNone ? refNum : INVALID_HANDLE_VALUE);
   return err;
}

static Err portConnectorSetFlowControl(PortHandle portConnectorRef, bool flowOn)
{
   Err err;
   UInt16 refNum = portConnectorRef;
   UInt16 sizeP;
   Int32 flags;

   sizeP = sizeof(Int32);
   if ((err = SrmControl(refNum, srmCtlGetFlags, &flags, &sizeP)) == errNone) // Get current flags
   {
      if (flowOn)
         flags |= srmSettingsFlagRTSAutoM | srmSettingsFlagCTSAutoM;
      else
         flags &= ~(srmSettingsFlagRTSAutoM | srmSettingsFlagCTSAutoM);

      err = SrmControl(refNum, srmCtlSetFlags, &flags, &sizeP);
   }
   return err;
}

static Err portConnectorReadCheck(PortHandle portConnectorRef, int32* inQueue)
{
   Err err;
   UInt16 refNum = portConnectorRef;
   ULong available;

   err = SrmReceiveCheck(refNum, &available);

   *inQueue = (int32) available;
   return err;
}

static Err portConnectorReadWriteBytes(PortHandle portConnectorRef, int32 portNumber, bool stopWriteCheckOnTimeout, int32 timeout, uint8 *bytes, int32 start, int32 count, int32 *retCount, bool isRead)
{
   Err err;
   UInt16 refNum = portConnectorRef;
   ULong n;
   Int32 timeoutTicks = millisToTicks(timeout);

   if (isRead)
   {
      n = SrmReceive(refNum, (VoidP) (bytes + start), (ULong) count, timeoutTicks, &err);
      if (err == serErrTimeOut)
         err = errNone;
   }
   else
   {
      UInt16 sizeP = sizeof(Int32);

      if (portNumber == SERIAL_SIR)
         SrmControl(refNum, srmCtlRxDisable, 0, 0); // disable Rx

      SrmControl(refNum, srmCtlSetCtsTimeout, &timeoutTicks, &sizeP);

      n = SrmSend(refNum, (VoidP) (bytes + start), (ULong) count, &err);
      SrmSendFlush(refNum);

      // Write check
      if (err == errNone)
      {
         do
         {
            err = SrmSendWait(refNum);
            if (stopWriteCheckOnTimeout && err == serErrTimeOut)
               break;
         } while (err != errNone);

         if (portNumber == SERIAL_SIR)
         {
            SrmControl(refNum, srmCtlIrDAEnable, 0, 0); // re-enable IrDA
            SrmControl(refNum, srmCtlRxEnable, 0, 0); // enable Rx
         }
      }
   }

   *retCount = n;
   return err;
}
