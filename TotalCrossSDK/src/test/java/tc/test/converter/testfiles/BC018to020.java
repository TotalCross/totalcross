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

public class BC018to020
{
   public BC018to020()
   {
      int i;
      i = - 2147483648;
      i = 2147483647;

      float f;
      f = 0.5f;
      f = -0.5f;

      long l;
      l = -9223372036854775808L;
      l = 9223372036854775807L;

      double d;
      d = 0.5;
      d = -0.5;

      String s = "testing java opcode";

      if (false) {i+=0; f+=0; l+=0; d+=0; s+=0;} // remove warnings
   }
}
