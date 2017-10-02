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

public class BC126to131 {
  public BC126to131() {
    int i1 = 0, i2 = 2, i3 = 1; // regs (32)--> 1 2 3
    long l1 = 0L, l2 = 2L, l3 = 1L; // regs (64)--> 1 2 3

    i1 = i2 & i3; // AND_regI_regI_regI
    i1 = i2 & 2047; // AND_regI_regI_s12
    i1 = i2 & -2048; // AND_regI_regI_s12
    i1 = i2 & 2048; // AND_regI_regI_regI

    l1 = l2 & l3; // AND_regL_regL_regL
    l1 = l2 & 2047; // AND_regL_regL_regL

    i1 = i2 | i3; // OR_regI_regI_regI
    i1 = i2 | 2047; // OR_regI_regI_s12
    i1 = i2 | -2048; // OR_regI_regI_s12
    i1 = i2 | 2048; // OR_regI_regI_regI

    l1 = l2 | l3; // OR_regL_regL_regL
    l1 = l2 | 2047; // OR_regL_regL_regL

    i1 = i2 ^ i3; // XOR_regI_regI_regI
    i1 = i2 ^ 2047; // XOR_regI_regI_s12
    i1 = i2 ^ -2048; // XOR_regI_regI_s12
    i1 = i2 ^ 2048; // XOR_regI_regI_regI

    l1 = l2 ^ l3; // XOR_regL_regL_regL
    l1 = l2 ^ 2047; // XOR_regL_regL_regL

    if (false) {
      i1 += 0;
      l1 += 0;
    } // remove warnings
  }
}
