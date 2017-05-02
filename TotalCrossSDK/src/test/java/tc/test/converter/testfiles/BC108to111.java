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

public class BC108to111
{
   public BC108to111()
   {
      int    i1=0,    i2=2,    i3=1;    // regs (32)--> 1 2 3
      long   l1=0L,   l2=2L,   l3=1L;   // regs (64)--> 1 2 3
      float  f1=0.0f, f2=2.0f, f3=1.0f; // regs (64)--> 4 5 6
      double d1=0.0,  d2=2.0,  d3=1.0;  // regs (64)--> 7 8 9

      i1 = i2 / i3;      // DIV_regI_regI_regI
      i1 = i2 / 2047;    // DIV_regI_regI_s12
      i1 = 2047 / i2;    // DIV_regI_regI_regI
      i1 = i2 / 2048;    // DIV_regI_regI_regI
      i1 = 2048 / i2;    // DIV_regI_regI_regI
      i1 = 10 / 20;      // MOV_regI_s18 (DIV is done in compile*time)
      i1 = i1 / i2 / i3; // DIV_regI_regI_regI ; DIV_regI_regI_regI

      l1 = l2 / l3;      // DIV_regL_regL_regL
      l1 = l2 / 2047;    // DIV_regL_regL_regL
      l1 = 2047 / l2;    // DIV_regL_regL_regL
      l1 = l2 / 2048;    // DIV_regL_regL_regL
      l1 = 2048 / l2;    // DIV_regL_regL_regL
      l1 = 10L / 20L;    // MOV_regL_s18 (DIV is done in compile*time)
      l1 = l1 / l2 / l3; // DIV_regL_regL_regL ; DIV_regL_regL_regL

      f1 = f2 / f3;        // DIV_regD_regD_regD
      f1 = f2 / 2047;      // DIV_regD_regD_regD
      f1 = 2047 / f2;      // DIV_regD_regD_regD
      f1 = f2 / 2048;      // DIV_regD_regD_regD
      f1 = 2048 / f2;      // DIV_regD_regD_regD
      f1 = 10.0f / 20.0f;  // MOV_regD_s18 (DIV is done in compile*time)
      f1 = f1 / f2 / f3;   // DIV_regD_regD_regD ; DIV_regD_regD_regD

      d1 = d2 / d3;        // DIV_regD_regD_regD
      d1 = d2 / 2047;      // DIV_regD_regD_regD
      d1 = 2047 / d2;      // DIV_regD_regD_regD
      d1 = d2 / 2048;      // DIV_regD_regD_regD
      d1 = 2048 / d2;      // DIV_regD_regD_regD
      d1 = 10.0 / 20.0;    // MOV_regD_s18 (DIV is done in compile*time)
      d1 = d1 / d2 / d3;   // DIV_regD_regD_regD ; DIV_regD_regD_regD
   }
}
