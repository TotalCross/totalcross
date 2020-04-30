// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



typedef HANDLE PortHandle;

static Err queryRegistry(HKEY key, TCHARP subkey, TCHARP name, CharP buf, int32 size)
{
   HKEY handle;
   int32 type;
   Err err = NO_ERROR;

   if ((err = RegOpenKeyEx(key, subkey, 0, KEY_READ, &handle)) != NO_ERROR)
      return err;

   type = REG_DWORD;
   if ((err = RegQueryValueEx(handle, name, null, &type, buf, &size)) != NO_ERROR)
   {
      RegCloseKey(handle);
      return err;
   }
   err = RegCloseKey(handle);

   return err;
}

#ifdef WINCE
#define xprintf wsprintf
#else
#define xprintf sprintf
#endif

static Err portConnectorCreate(PortHandle* portConnectorRef, VoidP receiveBuffer, int32 number, int32 baudRate, int32 bits, int32 parity, int32 stopBits, int32 writeTimeOutValue)
{
   TCHAR buf[50],buf2[50];
   char buf8[50];
   HANDLE h;
   COMMTIMEOUTS timeout;
   DCB dcb;
   int32 i32,i;
   bool isIR = true; // guich@570_104
   Err err;

   buf[0] = 0;

   switch (number) // guich@320_42
   {
      case SERIAL_IRCOMM:
         isIR = 1;
         if ((err = queryRegistry(HKEY_LOCAL_MACHINE, TEXT("Drivers\\BuiltIn\\IrCOMM"), TEXT("Index"), (CharP) &i32, 4)) == NO_ERROR)
            number = i32;
         else
            return err; //number = 3; // maybe an old device?
         break;
      case SERIAL_SIR:
         isIR = 1;
         if ((err = queryRegistry(HKEY_LOCAL_MACHINE, TEXT("ExtModems\\GenericInfraredModem"), TEXT("Port"), (CharP) buf, 32)) != NO_ERROR)
            return err;
         // fabio: Couldn't test if the above also works for Qtek, so I'm leaving the alternative to test that later.
         //if ((err = queryRegistry(HKEY_LOCAL_MACHINE, TEXT("Drivers\\BuiltIn\\SerialIR"), TEXT("Index"), (CharP) &i32, 4)) == NO_ERROR) // guich@570_105: in qtek its SerialIR
         //   number = i32;
         break;
      case SERIAL_USB: // fabio: as far as we know this type of connection is no longer used on pdas, so I'll leave the implementation as it is.
         if ((err = queryRegistry(HKEY_LOCAL_MACHINE, TEXT("Drivers\\BuiltIn\\SerialUSB"), TEXT("Index"), (CharP) &i32, 4)) == NO_ERROR)
            number = i32;
         else
            return err;
         break;
      case SERIAL_BLUETOOTH:
         for (i = 10; i >= 0; i--) // guich@tc112_4: loop through the serial ports to find the correct bluetooth one - wierd: there are 2 bluetooth ports, but only one works (usually com6)
         {
            xprintf(buf, TEXT("Drivers\\BuiltIn\\Serial%d"), i);
            if ((err = queryRegistry(HKEY_LOCAL_MACHINE, buf, TEXT("FriendlyName"), (CharP) buf2, sizeof(buf2))) == NO_ERROR)
            {
               TCHARP2CharPBuf(buf2, buf8);
               CharPToLower(buf8);
               if (xstrstr(buf8,"bluetooth") && (err = queryRegistry(HKEY_LOCAL_MACHINE, buf, TEXT("Index"), (CharP) &i32, 4)) == NO_ERROR)
               {
                  number = i32;
                  buf[0] = 0;
                  break;
               }
            }
         }
         if (i == -1)
            return -2;
         break;
      case SERIAL_DEFAULT:
         if ((err = queryRegistry(HKEY_LOCAL_MACHINE, TEXT("Drivers\\BuiltIn\\Serial"), TEXT("Index"), (CharP) &i32, 4)) == NO_ERROR)
            number = i32;
         else
            return err; //number = 1;
         break;
      default: // guich@570_106: the user can now pass something like TCP1 in the port number
         xprintf(buf, TEXT("%c%c%c%c:"), (TCHAR) ((number>>24)&0xFF), (TCHAR) ((number>>16)&0xFF), (TCHAR) ((number>>8)&0xFF), (TCHAR) (number&0xFF));
   }

   if (buf[0] == 0) // not yet set on default above?
   {
      // HOWTO: Specify Serial Ports Larger than COM9 - http://support.microsoft.com/kb/115831/en-us
      if (number < 10)
         xprintf(buf, TEXT("COM%d:"), number);
      else
         xprintf(buf, TEXT("\\\\.\\COM%d"), number);
   }

   if ((h = CreateFile(buf, GENERIC_READ|GENERIC_WRITE, 0, null, OPEN_EXISTING, FILE_ATTRIBUTE_SYSTEM|FILE_FLAG_WRITE_THROUGH, null)) == INVALID_HANDLE_VALUE)
      return GetLastError();

#ifdef WINCE
   if (isIR)
      EscapeCommFunction(h, SETIR);
#endif

   // set serial timeouts
   GetCommTimeouts(h, &timeout);
   timeout.ReadIntervalTimeout = 0;
   timeout.ReadTotalTimeoutMultiplier = 5;
   timeout.ReadTotalTimeoutConstant = 100;
   timeout.WriteTotalTimeoutMultiplier = 5;
   timeout.WriteTotalTimeoutConstant = 1000;
   SetCommTimeouts(h, &timeout);

   GetCommState(h, &dcb);
   if (baudRate == 0)
      baudRate = 9600;
   dcb.BaudRate = baudRate;
   dcb.ByteSize = (uint8) bits;
	dcb.Parity = (parity == SERIAL_PARITY_NONE) ? NOPARITY:(parity == SERIAL_PARITY_EVEN) ? EVENPARITY:ODDPARITY; // guich@450_29: added oddparity
   if (stopBits == 1)
      dcb.StopBits = ONESTOPBIT;
   else
   if (stopBits == 2)
      dcb.StopBits = TWOSTOPBITS;

   // NOTE: Handshaking is off by default for WindowsCE and on by
   // default for all other Win32 platforms
   #ifdef WINCE
      dcb.fOutxCtsFlow = 0;
      dcb.fRtsControl = RTS_CONTROL_ENABLE;
   #else
      dcb.fOutxCtsFlow = 1;
      dcb.fRtsControl = RTS_CONTROL_HANDSHAKE;
   #endif
   dcb.fOutxDsrFlow = 0;
   dcb.fDtrControl = DTR_CONTROL_DISABLE;
   dcb.fDsrSensitivity = 0;
   dcb.fTXContinueOnXoff = 0;
   dcb.fOutX = 0;
   dcb.fInX = 0;
   dcb.fNull = 0;
   dcb.fAbortOnError = 0;
   if (SetCommState(h, &dcb) == 0)
   {
      CloseHandle(h);
      return GetLastError();
   }
   *portConnectorRef = h;
   return NO_ERROR;
}

static inline Err portConnectorClose(PortHandle portConnectorRef, VoidP receiveBuffer, int32 portNumber)
{
   return (CloseHandle(portConnectorRef) ? NO_ERROR : GetLastError());
}

static Err portConnectorSetFlowControl(PortHandle portConnectorRef, bool flowOn)
{
	DCB dcb;

	GetCommState(portConnectorRef, &dcb);
	if (flowOn)
	{
		dcb.fOutxCtsFlow = 1;
		dcb.fRtsControl = RTS_CONTROL_HANDSHAKE;
	}
	else
	{
		dcb.fOutxCtsFlow = 0;
		dcb.fRtsControl = RTS_CONTROL_ENABLE;
	}

   return (SetCommState(portConnectorRef, &dcb) ? NO_ERROR : GetLastError());
}

typedef BOOL (__stdcall *ReadWriteFileFunc)(HANDLE hFile, LPVOID lpBuffer, DWORD nNumberOfBytesToRead, LPDWORD lpNumberOfBytesRead, LPOVERLAPPED lpOverlapped);

static Err portConnectorReadWriteBytes(PortHandle portConnectorRef, int32 portNumber, bool stopWriteCheckOnTimeout, int32 timeout, uint8 *bytes, int32 start, int32 count, int32 *retCount, bool isRead)
{
   ReadWriteFileFunc readWriteFile;
   COMMTIMEOUTS commtimeouts;
   int i;
   Err err = NO_ERROR;

   if (!GetCommTimeouts(portConnectorRef, &commtimeouts))
      goto Error;

   if (isRead)
   {
      commtimeouts.ReadIntervalTimeout = 0;
      commtimeouts.ReadTotalTimeoutMultiplier = 0;
      commtimeouts.ReadTotalTimeoutConstant = timeout; //flsobral@tc122: don't need to set the multiplier, we can just use the constant field.
      if (!SetCommTimeouts(portConnectorRef, &commtimeouts))
         goto Error;
      readWriteFile = ReadFile;
   }
   else
   {
      commtimeouts.ReadIntervalTimeout = 0;
      commtimeouts.WriteTotalTimeoutMultiplier = timeout/count;
      commtimeouts.WriteTotalTimeoutConstant = 0;
      if (!SetCommTimeouts(portConnectorRef, &commtimeouts))
         goto Error;
      readWriteFile = WriteFile;
   }
   
   for (i = 0 ; i < 10 ; i++)
   {
      if (readWriteFile(portConnectorRef, bytes+start, count, retCount, null))
         return NO_ERROR;
      if ((err = GetLastError()) != 21)
         break;
      Sleep(150);
   }
   goto Finish;
   
Error:
   err = GetLastError();
Finish:
   return err != 1359 ? err : NO_ERROR;
}

static Err portConnectorReadCheck(PortHandle portConnectorRef, int32* inQueue)
{
   COMSTAT Stat;
   int32 dwErrors;
/*
   char errors[128] = 0;
*/
   if (!ClearCommError(portConnectorRef, &dwErrors, &Stat))
   {
/*
      if (dwErrors & CE_BREAK)
         xstrcat(errors, "CE_BREAK ");
      if (dwErrors & CE_FRAME)
         xstrcat(errors, "CE_FRAME ");
      if (dwErrors & CE_IOE)
         xstrcat(errors, "CE_IOE ");
      if (dwErrors & CE_MODE)
         xstrcat(errors, "CE_MODE ");
      if (dwErrors & CE_OVERRUN)
         xstrcat(errors, "CE_OVERRUN ");
      if (dwErrors & CE_RXOVER)
         xstrcat(errors, "CE_RXOVER ");
      if (dwErrors & CE_RXPARITY)
         xstrcat(errors, "CE_RXPARITY ");
      if (dwErrors & CE_TXFULL)
         xstrcat(errors, "CE_TXFULL ");

      debug("portConnectorReadCheck - %s", errors);
*/
      return GetLastError();
   }

   *inQueue = Stat.cbInQue;
   return NO_ERROR;
}
