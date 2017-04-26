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

public class BC057_071to074
{
   public BC057_071to074()
   {
      double a, b, c, d, e;

      a = -131072;   // integer minimum of 18 bits                 -- MOV_regD_s18
      b = 131071;    // integer maximum of 18 bits                 -- MOV_regD_s18
      c = -0.5;      // a double                                   -- MOV_regD_sym
      d = 1.5;       // a double                                   -- MOV_regD_sym
      e = -131072.0; // a double that can be an integer of 18 bits -- MOV_regD_s18 (an optmization)
      e = 131071.0;  // a double that can be an integer of 18 bits -- MOV_regD_s18 (an optmization)

      a = b; // MOV_reg64_reg64
      d = a; // MOV_reg64_reg64
      e = d; // MOV_reg64_reg64

      // incompleto ...
      c += e;  // remove warnings
   }

}
