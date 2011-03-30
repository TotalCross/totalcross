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

// $Id: BC023_034to037.java,v 1.9 2011-01-04 13:19:02 guich Exp $

package tc.test.converter.testfiles;

public class BC023_034to037
{
   public BC023_034to037()
   {
      float f1=1.0f, f2=2.0f, f3=3.0f, f4=4.0f, f;
      f = f1;
      f = f2;
      f = f3;
      f = f4;
      if (false) f+=0; // remove warnings
   }
}
