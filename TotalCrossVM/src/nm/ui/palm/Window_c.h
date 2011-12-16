/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



bool isTreo();
bool loadTreoLib();

Err HsGrfGetStateExt(Boolean* capsLockP, Boolean* numLockP, Boolean* optLockP, UInt16* tempShiftP, Boolean* autoShiftedP);
Err HsGrfSetStateExt(Boolean capsLock,Boolean numLock, Boolean optLock, Boolean upperShift, Boolean optShift, Boolean autoShift);

static void windowSetSIP(int32 sipOption) // guich@tc110_55
{
   if (isTreo() && loadTreoLib() && (sipOption == SIP_ENABLE_NUMERICPAD || sipOption == SIP_DISABLE_NUMERICPAD))
   {
      Boolean caps,num,opt,autos;
      UInt16 temp;
      Err err;
      err = HsGrfGetStateExt(&caps, &num, &opt, &temp, &autos);
      HsGrfSetStateExt(caps, num, sipOption == SIP_ENABLE_NUMERICPAD, false, false, autos);
   }
}
