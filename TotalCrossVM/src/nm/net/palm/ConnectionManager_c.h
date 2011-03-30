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



#define TIMEOUT   SysTicksPerSecond() * 45
Err loadNetLib();

static Err CmClose()
{
   Err err = errNone;

   if (gNETLink != null) // library is loaded
   {
      while ((err = NetLibClose(true)) == netErrStillOpen);
      err = NetLibFinishCloseWait();
      gNETLink = null;
   }
   return err == netErrNotOpen ? errNone : err;
}

static Err CmGetHostAddress(CharP hostName, CharP hostAddress)
{
   NetHostInfoBufType hostInfo;
   NetHostInfoPtr hostInfoP;
   int32 ipAddr;
   Err err = errNone;

   if (gNETLink == null)
      err = loadNetLib();
   if (err != errNone)
      return err;

   hostInfoP = NetLibGetHostByName(hostName, &hostInfo, TIMEOUT, &err);
   if (hostInfoP != null && err == errNone) // resolved
   {
      ipAddr = *((int32*) hostInfoP->addrListP[0]); // get first ip address
      NetLibAddrINToA(ipAddr, hostAddress);
   }
   return err;
}

static Err CmGetHostName(CharP hostAddress, CharP hostName)
{
   NetHostInfoBufType hostInfo;
   int32 ipAddr;
   Err err = errNone;

   if (gNETLink == null)
      err = loadNetLib();
   if (err != errNone)
      return err;

   ipAddr = NetLibAddrAToIN(hostAddress);
   NetLibGetHostByAddr((VoidP) &ipAddr, sizeof(int32), netSocketAddrINET, &hostInfo, TIMEOUT, &err);
   if (err == errNone) // resolved
      xstrcpy(hostName, (CharP) hostInfo.name);
   return err;
}

static Err CmGetLocalHost(CharP address)
{
   UInt32 creator, ip;
   UInt16 instance;
   UInt16 size = sizeof(UInt32);
   int32 index = 0;
   Err err = errNone;

   if (gNETLink == null)
      err = loadNetLib();
   if (err != errNone)
      return err;

   while ((err = NetLibIFGet(index++, &creator, &instance)) != netErrInvalidInterface)
   {
      if (creator == netIFCreatorPPP)
      {
         if ((err = NetLibIFSettingGet(creator, instance, netIFSettingActualIPAddr, &ip, &size)) == errNone)
         {
            NetLibAddrINToA(ip, address);
            return errNone;
         }
      }
   }
   return err;
}
