// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only



#ifndef __APPLE__
#include <features.h>
#endif
#include <stdio.h>   /* Standard input/output definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */

typedef UInt16 PortHandle;

#ifdef ANDROID
 #define cfsetspeed cfsetospeed
#endif

static Err portConnectorCreate(PortHandle* portConnectorRef, VoidP receiveBuffer, int32 number, int32 baudRate, int32 bits, int32 parity, int32 stopBits, int32 writeTimeOutValue)
{
   char dev[16];
   struct termios options;
   int fd;

   // fdie@500 Currently only Linux serial ports are supported
   if (number == SERIAL_DEFAULT)
      sprintf(dev, "/dev/ttyS%d", number);
   else if(number == SERIAL_IRCOMM)
      sprintf(dev, "/dev/ircomm%d", number - SERIAL_IRCOMM);
   else if(number == SERIAL_USB)
      sprintf(dev, "/dev/ttyUSB%d", number - SERIAL_USB);
   else if(number == SERIAL_BLUETOOTH)
      sprintf(dev, "/dev/rfcomm%d", number - SERIAL_BLUETOOTH);
   else
      return EPERM;

   fd = open(dev, O_RDWR | O_NOCTTY | O_EXCL/* | O_NDELAY*/);
   if (fd < 0)
   {
      perror(dev);
	  return EBADF;
   }

   // Get the current options for the port...
   if (tcgetattr(fd, &options) < 0)
      return errno;

#define CASE(x)   case x: baudRate = B##x; break;
   switch (baudRate)
   {
      CASE(50)    CASE(75)    CASE(110)   CASE(134)   CASE(150)   CASE(200)
      CASE(300)   CASE(600)   CASE(1200)  CASE(1800)  CASE(2400)  CASE(4800)
      CASE(9600)  CASE(19200) CASE(38400) CASE(57600) CASE(115200)
      default: baudRate = B0; /* hang up */
   }
   // Set the in/out baud rates
   if (cfsetspeed(&options, baudRate) < 0)
      return errno;

   options.c_lflag &= ~ICANON;
   options.c_cc[VMIN] = 0;
   options.c_cc[VTIME] = writeTimeOutValue;

   /* Set character size.  csize variable is an integer holding the desired character size. */
   options.c_cflag &= ~(CSIZE);  /* clear bits used for char size */
   switch (bits)
   {
      case 8:
         options.c_cflag |= CS8;
         break;
      case 7:
         options.c_cflag |= CS7;
         break;
      case 6:
         options.c_cflag |= CS6;
         break;
      case 5:
         options.c_cflag |= CS5;
         break;
      default:
         fprintf(stderr, "wrong number of bits: %d\n", bits);
         return EINVAL;
   }

   /* set parity -- parity variable can be an enum type */
   switch (parity)
   {
      case SERIAL_PARITY_NONE:
         options.c_cflag &= ~(PARENB);  /* clear parity enable */
         options.c_iflag &= ~(INPCK);   /* disable input parity checking */
         break;
      case SERIAL_PARITY_ODD:
         options.c_cflag |= (PARODD | PARENB);  /* enable parity, set to ODD */
         options.c_iflag |= INPCK;              /* enable input parity checking */
         break;
      case SERIAL_PARITY_EVEN:
         options.c_cflag |= PARENB;     /* enable parity, default is EVEN */
         options.c_cflag &= ~(PARODD);  /* ensure PARODD is clear */
         options.c_iflag |= INPCK;      /* enable input parity checking */
         break;
   }

   if (stopBits == 2)
      options.c_cflag |= CSTOPB;
   else
      options.c_cflag &= ~CSTOPB; // default is 1 stop bit

   // Set the new options for the port...
   if (tcsetattr(fd, TCSADRAIN, &options) < 0)
      return errno;

   if (portConnectorRef)
      *portConnectorRef = fd;

   return NO_ERROR;
}

static inline Err portConnectorClose(PortHandle portConnectorRef, VoidP receiveBuffer, int32 portNumber)
{
#ifndef ANDROID
   if (tcdrain(portConnectorRef) < 0) // wait until output buffer is empty
      return errno;
#endif
   return (close(portConnectorRef) == 0) ? NO_ERROR : errno;
}

static Err portConnectorSetFlowControl(PortHandle portConnectorRef, bool flowOn)
{
   struct termios options;

   // Get the current options for the port...
   if (tcgetattr(portConnectorRef, &options) < 0)
      return errno;

   options.c_lflag &= ~(ICANON|ECHO|ISIG);
   options.c_cflag &= ~CRTSCTS;
   if (flowOn)
      options.c_cflag |= CRTSCTS;

   // Set the new options for the port...
   if (tcsetattr(portConnectorRef, TCSADRAIN, &options) < 0)
      return errno;

   return NO_ERROR;
}

static Err portConnectorReadWriteBytes(PortHandle portConnectorRef, int32 portNumber, bool stopWriteCheckOnTimeout, int32 timeout, uint8 *bytes, int32 start, int32 count, int32 *retCount, bool isRead)
{
   struct termios options;
   int num;

   // Get the current options for the port...
   if (tcgetattr(portConnectorRef, &options) < 0)
      return errno;

   //options.c_cc[VMIN] = 0; already set
   options.c_cc[VTIME] = timeout/100; // convert millis to tenth of seconds

   // Set the new options for the port...
   if (tcsetattr(portConnectorRef, TCSADRAIN, &options) < 0)
      return errno;

   if (isRead)
   {
      if ((num = (int)read(portConnectorRef, bytes + start, count)) < 0)
         return errno;
   }
   else
   {
      if ((num = (int)write(portConnectorRef, bytes + start, count)) < 0)
         return errno;
   }

   if (retCount)
      *retCount = num;

   return NO_ERROR;
}

static Err portConnectorReadCheck(PortHandle portConnectorRef, int32* inQueue)
{
   return ENOSYS; // not implemented
}
