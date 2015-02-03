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



package tc.test.converter.testfiles;

public class BC120to125
{
   public BC120to125()
   {
      int    i1=0,    i2=2,    i3=1;    // regs (32)--> 1 2 3
      long   l1=0L,   l2=2L,   l3=1L;   // regs (64)--> 1 2 3

      i1 = i2 << i3;    // SHL_regI_regI_regI
      i1 = i2 << 2047;  // SHL_regI_regI_s12
      i1 = i2 << -2048; // SHL_regI_regI_s12
      i1 = i2 << 2048;  // SHL_regI_regI_regI

      l1 = l2 << i1;    // SHL_regL_regL_regL
      l1 = l2 << 2047;  // SHL_regL_regL_regL

      i1 = i2 >> i3;    // SHR_regI_regI_regI
      i1 = i2 >> 2047;  // SHR_regI_regI_s12
      i1 = i2 >> -2048; // SHR_regI_regI_s12
      i1 = i2 >> 2048;  // SHR_regI_regI_regI

      l1 = l2 >> i1;    // SHR_regL_regL_regL
      l1 = l2 >> 2047;  // SHR_regL_regL_regL

      i1 = i2 >>> i3;    // USHR_regI_regI_regI
      i1 = i2 >>> 2047;  // USHR_regI_regI_s12
      i1 = i2 >>> -2048; // USHR_regI_regI_s12
      i1 = i2 >>> 2048;  // USHR_regI_regI_regI

      l1 = l2 >>> i1;    // USHR_regL_regL_regL
      l1 = l2 >>> 2047;  // USHR_regL_regL_regL

      if (false) {i1+=0; l1+=l3;} // remove warnings
   }
}
