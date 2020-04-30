// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

///////////////////////////////////////////////////////////////////////////
//                                 Math                                  //
///////////////////////////////////////////////////////////////////////////

TESTCASE(_str2int64)
{
   int64 i;

   i = str2long("-1234567890",null);
   ASSERT2_EQUALS(I64, i, I64_CONST(0xffffffffb669fd2e));

   i = str2long("8756342109",null);
   ASSERT2_EQUALS(I64, i, I64_CONST(0x0000000209eb2d5d));

   i = str2long("6787653557873139543",null);
   ASSERT2_EQUALS(I64, i, I64_CONST(0x5e3296f2c771df57));

finish: ;
}

/**
 * To fill in and check IEEE 754 floating point values, you may use
 * this applet: http://www.randelshofer.ch/fhw/gri/doubleapplet.html
 */
TESTCASE(_str2double)
{
   bool err;
   double d;
   int64 *di = (int64*)&d;
   DoubleBuf dblBuffer; // str2double requires a r/w buffer so we can't use constant strings

   xstrcpy(dblBuffer, "-3.5");
   d = str2double(dblBuffer,&err);
   ASSERT2_EQUALS(I64, *di, IEEE_I64_VALUE(I64_CONST(0xC00C000000000000)));
   ASSERT1_EQUALS(False, err);

   xstrcpy(dblBuffer, "-87654");
   d = str2double(dblBuffer,&err);
   ASSERT2_EQUALS(I64, *di, IEEE_I64_VALUE(I64_CONST(0xc0f5666000000000)));
   ASSERT1_EQUALS(False, err);

   xstrcpy(dblBuffer, "5.0025");
   d = str2double(dblBuffer,&err);
   ASSERT2_EQUALS(I64, *di, IEEE_I64_VALUE(I64_CONST(0x4014028f5c28f5c3)));
   ASSERT1_EQUALS(False, err);

   xstrcpy(dblBuffer, "+36.123456789012");
   d = str2double(dblBuffer,&err);
   ASSERT2_EQUALS(I64, *di, IEEE_I64_VALUE(I64_CONST(0x40420fcd6e9ba34b)));
   ASSERT1_EQUALS(False, err);

   xstrcpy(dblBuffer, "-12.25e3");
   d = str2double(dblBuffer,&err);
   ASSERT2_EQUALS(I64, *di, IEEE_I64_VALUE(I64_CONST(0xc0c7ed0000000000)));
   ASSERT1_EQUALS(False, err);

   xstrcpy(dblBuffer, "1.625E-2");
   d = str2double(dblBuffer,&err);
   ASSERT2_EQUALS(I64, *di, IEEE_I64_VALUE(I64_CONST(0x3f90a3d70a3d70a4)));
   ASSERT1_EQUALS(False, err);

finish: ;
}

TESTCASE(_doubleToStr)
{
   double d;
   DoubleBuf db;
   char *dblBuffer;

   d = -1.5;
   dblBuffer = double2str(d,-1,db);
   ASSERT2_EQUALS(Sz, dblBuffer, "-1.5");

   d =0.31415926e001;
   dblBuffer = double2str(d,-1,db);
   ASSERT2_EQUALS(Sz, dblBuffer, "3.1415926");

finish: ;
}
