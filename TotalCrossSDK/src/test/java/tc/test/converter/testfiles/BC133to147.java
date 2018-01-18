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

public class BC133to147 {
  public BC133to147() {
    byte b1 = 0, b2 = 2; // regs (32)--> 1 2
    char c1 = 0, c2 = 2; // regs (32)--> 3 4
    short s1 = 0, s2 = 2; // regs (32)--> 5 6
    int i1 = 0, i2 = 2; // regs (32)--> 7 8
    long l1 = 0L, l2 = 2L; // regs (64)--> 1 2
    float f1 = 0.0f, f2 = 2.0f; // regs (64)--> 3 4
    double d1 = 0.0, d2 = 2.0; // regs (64)--> 5 6

    b1 = (byte) c1; // CONV_regIb_regI
    b1 = (byte) s1; // CONV_regIb_regI
    b1 = (byte) i1; // CONV_regIb_regI
    b1 = (byte) l1; // CONV_regI_regL ; CONV_regIb_regI
    b1 = (byte) f1; // CONV_regI_regD ; CONV_regIb_regI
    b1 = (byte) d1; // CONV_regI_regD ; CONV_regIb_regI

    c1 = (char) b1; // CONV_regIc_regI
    c1 = (char) s1; // CONV_regIc_regI
    c1 = (char) i1; // CONV_regIc_regI
    c1 = (char) l1; // CONV_regI_regL ; CONV_regIc_regI
    c1 = (char) f1; // CONV_regI_regD ; CONV_regIc_regI
    c1 = (char) d1; // CONV_regI_regD ; CONV_regIc_regI

    s1 = (short) b1; // CONV_regIs_regI
    s1 = (short) c1; // CONV_regIs_regI
    s1 = (short) i1; // CONV_regIs_regI
    s1 = (short) l1; // CONV_regI_regL ; CONV_regIs_regI
    s1 = (short) f1; // CONV_regI_regD ; CONV_regIs_regI
    s1 = (short) d1; // CONV_regI_regD ; CONV_regIs_regI

    i1 = (int) b1; // MOV_regI_regI
    i1 = (int) c1; // MOV_regI_regI
    i1 = (int) s1; // MOV_regI_regI
    i1 = (int) l1; // CONV_regI_regL
    i1 = (int) f1; // CONV_regI_regD
    i1 = (int) d1; // CONV_regI_regD

    l1 = (long) b1; // CONV_regL_regI
    l1 = (long) c1; // CONV_regL_regI
    l1 = (long) s1; // CONV_regL_regI
    l1 = (long) i1; // CONV_regL_regI
    l1 = (long) f1; // CONV_regL_regD
    l1 = (long) d1; // CONV_regL_regD

    f1 = (float) b1; // CONV_regD_regI
    f1 = (float) c1; // CONV_regD_regI
    f1 = (float) s1; // CONV_regD_regI
    f1 = (float) i1; // CONV_regD_regI
    f1 = (float) l1; // CONV_regD_regD
    f1 = (float) d1; // MOV_reg64_reg64

    d1 = (double) b1; // CONV_regD_regI
    d1 = (double) c1; // CONV_regD_regI
    d1 = (double) s1; // CONV_regD_regI
    d1 = (double) i1; // CONV_regD_regI
    d1 = (double) l1; // CONV_regD_regL
    d1 = (double) f1; // MOV_reg64_reg64

    if (false) {
      b1 += b2;
      c1 += c2;
      s1 += s2;
      i1 += i2;
      l1 += l2;
      f1 += f2;
      d1 += d2;
    } // remove warnings
  }
}
