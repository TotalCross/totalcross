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

public class BC054_059to062 {
  public BC054_059to062() {
    int a, b, d, e;

    a = -1;
    b = 0;
    d = 31; // integer maximum of 6 bits  -- MOV_regI_s18
    e = -2048; // integer minimum of 12 bits -- MOV_regI_s18
    e = 2047; // integer maximum of 12 bits -- MOV_regI_s18
    e = -32768; // integer minimum of 16 bits -- MOV_regI_s18
    e = 32767; // integer maximum of 16 bits -- MOV_regI_s18
    e = -131072; // integer minimum of 18 bits -- MOV_regI_s18
    e = 131071; // integer maximum of 18 bits -- MOV_regI_s18
    e = -8388608; // integer minimum of 24 bits -- MOV_regI_sym
    e = 8388607; // integer maximum of 24 bits -- MOV_regI_sym
    e = -2147483648; // integer minimum of 32 bits -- MOV_regI_sym
    e = 2147483647; // integer maximum of 32 bits -- MOV_regI_sym

    a = b; // MOV_regI_regI
    d = a; // MOV_regI_regI
    e = d; // MOV_regI_regI

    e = a + b; // MOV_regI_regI
  }
}
