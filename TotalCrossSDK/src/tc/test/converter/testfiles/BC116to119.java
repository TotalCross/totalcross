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

public class BC116to119
{
   public BC116to119()
   {
      int    i1=0,    i2=2,    i3=1;    // regs (32)--> 1 2 3
      long   l1=0L,   l2=2L,   l3=1L;   // regs (64)--> 1 2 3
      float  f1=0.0f, f2=2.0f, f3=1.0f; // regs (64)--> 4 5 6
      double d1=0.0,  d2=2.0,  d3=1.0;  // regs (64)--> 7 8 9

      i1 = -i2;
      i1 = -(i2+i3);

      l1 = -l2;
      l1 = -(l2+l3);

      f1 = -f2;
      f1 = -(f2+f3);

      d1 = -d2;
      d1 = -(d2+d3);

      if (false) {i1+=0; l1+=0; f1+=0; d1+=0;} // remove warnings
   }
}
