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

public class BC055_063to066
{
  public BC055_063to066()
  {
    long a, b, c, d, e;

    a = -131072;               // integer minimum of 18 bits -- MOV_regL_s18
    b = 131071;                // integer maximum of 18 bits -- MOV_regL_s18
    c = -2147483648;           // integer minimum of 32 bits -- MOV_regI_sym
    d = 2147483647;            // integer maximum of 32 bits -- MOV_regI_sym
    e = -9223372036854775808L; // long minimum of 64 bits    -- MOV_regL_sym
    e = 9223372036854775807L;  // long maximum of 64 bits    -- MOV_regL_sym

    a = b; // MOV_reg64_reg64
    d = a; // MOV_reg64_reg64
    e = d; // MOV_reg64_reg64

    // incompleto ...
    c += e;
  }
}
