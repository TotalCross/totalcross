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

public class BC096to132
{
   public BC096to132()
   {
      int i1, i2, i3;

      i2 = 20;
      i3 = 10;

      i1 = i2 + i3; // ADD_regI_regI_regI
      i1 = i2 - i3; // SUB_regI_regI_regI
      i1 = i2 * i3; // MUL_regI_regI_regI
      i1 = i2 / i3; // DIV_regI_regI_regI

      i1 = i2 + 2047; // ADD_regI_s12_regI
      i1 = 2047 + i2; // ADD_regI_s12_regI
      i1 = i2 + 2048; // ADD_regI_regI_sym
      i1 = 2048 + i2; // ADD_regI_regI_sym

      i1 = i2 - 2047;          // ADD_regI_s12_regI
      i1 = i2 - 2048;          // ADD_regI_s12_regI
      i1 = i2 - 2049;          // ADD_regI_regI_sym
      i1 = i2 - 2147483647;    // ADD_regI_regI_sym
      i1 = i2 - (-2147483648); // SUB_regI_regI_regI
      i1 = 2047 - i2;          // SUB_regI_s12_regI
      i1 = -2048 - i2;         // SUB_regI_s12_regI

      i1 = 10 + 20;      // add in compile-time -- MOV_regI_s18

      i1 = i1 + i2 + i3; // ADD_regI_regI_regI ; ADD_regI_regI_regI

      i1 = i2 * 2047; // MUL_regI_s12_regI
      i1 = 2047 * i2; // MUL_regI_s12_regI
      i1 = i2 * 2048; // MUL_regI_regI_regI
      i1 = 2048 * i2; // MUL_regI_regI_regI

      i1 = i2 / 2047; // DIV_regI_regI_s12
      i1 = 2047 / i2; // DIV_regI_regI_regI
      i1 = i2 / 2048; // DIV_regI_regI_regI
      i1 = 2048 / i2; // DIV_regI_regI_regI

      i1 = i2 % i3;   // MOD_regI_regI_regI
      i1 = i2 % 2047; // MOD_regI_regI_s12
      i1 = 2047 % i2; // MOD_regI_regI_regI
      i1 = i2 % 2048; // MOD_regI_regI_regI

      i1 = -i2; // SUB_regI_s12_regI

      i1 = i2 << i3;   // SHL_regI_regI_regI
      i1 = i2 << 2047; // SHL_regI_regI_s12
      i1 = 2047 << i2; // SHL_regI_regI_regI
      i1 = i2 << 2048; // SHL_regI_regI_regI

      i1 = i2 >> i3;   // SHR_regI_regI_regI
      i1 = i2 >> 2047; // SHR_regI_regI_s12
      i1 = 2047 >> i2; // SHR_regI_regI_regI
      i1 = i2 >> 2048; // SHR_regI_regI_regI

      i1 = i2 >>> i3;   // USHR_regI_regI_regI
      i1 = i2 >>> 2047; // USHR_regI_regI_s12
      i1 = 2047 >>> i2; // USHR_regI_regI_regI
      i1 = i2 >>> 2048; // USHR_regI_regI_regI

      i1 = i2 & i3;   // AND_regI_regI_regI
      i1 = i2 & 2047; // AND_regI_regI_s12
      i1 = 2047 & i2; // AND_regI_regI_s12
      i1 = i2 & 2048; // AND_regI_regI_regI

      i1 = i2 | i3;   // OR_regI_regI_regI
      i1 = i2 | 2047; // OR_regI_regI_s12
      i1 = 2047 | i2; // OR_regI_regI_s12
      i1 = i2 | 2048; // OR_regI_regI_regI

      i1 = i2 ^ i3;   // XOR_regI_regI_regI
      i1 = i2 ^ 2047; // XOR_regI_regI_s12
      i1 = 2047 ^ i2; // XOR_regI_regI_s12
      i1 = i2 ^ 2048; // XOR_regI_regI_regI

      // incompleto ...
   }
}
