// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TC_API void tsT_update(NMParams p);

#define DEBUG_PORTCONNECTOR false // MUST CONVERT IT TO THE NEW OBJECT ALLOCATION FORMAT!!!!

TESTCASE(tidPC_create_iiiii) // totalcross/io/device/PortConnector native void create(int number, int baudRate, int bits, int parity, int stopBits);
{
#if DEBUG_PORTCONNECTOR
//#ifdef WIN32
   TNMParams p, pTime;
   int32 i32buf[5];
   TCObject objBuf[2];
   TCObject pTimeObj[1];
   CharP buf;
   CharP buf2;
   int32 len;

   p.i32 = i32buf;
   p.obj= objBuf;
   p.currentContext = currentContext;

   p.obj[0] = createObject("totalcross.io.device.PortConnector");
#if defined (WIN32)
   p.i32[0] = 7; //SERIAL_IRCOMM (with bauds 9600)
#else
   p.i32[0] = SERIAL_BLUETOOTH; //0x1003;
#endif
   p.i32[1] = 115200; //9600;
   p.i32[2] = 8;
   p.i32[3] = (int32) false;
   p.i32[4] = 1;

   tidPC_create_iiiii(&p);
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   tzero(pTime);
   pTime.obj = pTimeObj;
   pTime.obj[0] = createObject("totalcross.sys.Time");
   tsT_update(&pTime);

   if (Time_year(pTime.obj[0]) > 2000)
   {
      while (1)
      {
         buf = "ISTO EH UM TESTE!";
         p.i32[0] = 0;
         p.i32[1] = 35;
         p.obj[1] = createByteArray(p.i32[1]);
         xstrcpy(ARRAYOBJ_START(p.obj[1]), buf);
         buf2 = ARRAYOBJ_START(p.obj[1]);

         tidPC_writeBytes_Bii(&p);
         ASSERT1_EQUALS(Null, currentContext->thrownException);
      }
   }
   else
   {
      p.i32[0] = 0;
      p.i32[1] = 30;
      p.obj[1] = createByteArray(p.i32[1]*sizeof(TCHAR));
      while (1)
      {
         tidPC_readBytes_Bii(&p);
         ASSERT1_EQUALS(Null, currentContext->thrownException);

         if (p.retI > 0)
         {
            buf2 = ARRAYOBJ_START(p.obj[1]);
            len = ARRAYOBJ_LEN(p.obj[1]);
            buf2[len] = 0;
            alert("%s", buf2);
         }
      }
   }

/*
   do
   {
      tidPC_readCheck(&p);
   }
   while(p.retI == 0);
*/

   //xmemmove(ARRAYOBJ_START(p.obj[1]), buf, p.i32[1]*sizeof(TCHAR));
   //buf2 = ARRAYOBJ_START(p.obj[1]);
//   tidPC_readBytes_Bii(&p);

/*
   tidPC_readBytes_Bii(&p);
   buf2 = ARRAYOBJ_START(p.obj[1]);
   len = ARRAYOBJ_LEN(p.obj[1]);
   buf2[len] = 0;
*/
//   alert("%s", buf2);

   /*
   while (1)
   {

      if (p.retI > 0)
      {
         buf2 = ARRAYOBJ_START(p.obj[1]);
         len = ARRAYOBJ_LEN(p.obj[1]);
         buf2[len] = 0;
         alert("%s", buf2);
      }
      Sleep(2500);
   }
   */
   /*

   SerialPort port = new SerialPort(0, 9600);
   if (!port.isOpen())
     return;
   byte buf[] = new byte[10];
   buf[0] = 3;
   buf[1] = 7;
   port.writeBytes(buf, 0, 2);
   int count = port.readBytes(buf, 0, 10);
   if (count == 10)
     ...
   port.close();
   */
//#endif
#endif
   TEST_CANNOT_RUN;
   finish: ;
}
TESTCASE(tidPC_close) // totalcross/io/device/PortConnector native public void close();
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tidPC_isOpen) // totalcross/io/device/PortConnector native public boolean isOpen();
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tidPC_setFlowControl_b) // totalcross/io/device/PortConnector native public boolean setFlowControl(boolean on);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tidPC_readBytes_Bii) // totalcross/io/device/PortConnector native public int readBytes(byte []buf, int start, int count);
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tidPC_readCheck) // totalcross/io/device/PortConnector native public int readCheck();
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tidPC_writeBytes_Bii) // totalcross/io/device/PortConnector native public int writeBytes(byte []buf, int start, int count);
{
   TEST_SKIP;
   finish: ;
}
