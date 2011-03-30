/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: BC132.java,v 1.8 2011-01-04 13:19:03 guich Exp $

package tc.test.converter.testfiles;

public class BC132
{
   public BC132()
   {
      int x=0, y=0, z=0; // regs(32) 1 2 3

      x++; // ADD_regI_s12_regI
      y--; // ADD_regI_s12_regI
      --z; // ADD_regI_s12_regI
      ++y; // ADD_regI_s12_regI
   }
}
