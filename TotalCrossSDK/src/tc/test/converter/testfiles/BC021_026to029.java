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

// $Id: BC021_026to029.java,v 1.9 2011-01-04 13:19:03 guich Exp $

package tc.test.converter.testfiles;

public class BC021_026to029
{
   public BC021_026to029()
   {
      int i1=1, i2=2, i3=3, i4=4, i;
      i = i1;
      i = i2;
      i = i3;
      i = i4;
      if (false) i+=0; // remove warnings
   }
}
