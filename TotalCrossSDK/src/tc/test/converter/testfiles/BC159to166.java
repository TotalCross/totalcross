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

// $Id: BC159to166.java,v 1.9 2011-01-04 13:19:03 guich Exp $

package tc.test.converter.testfiles;

public class BC159to166
{
   public BC159to166()
   {
      int a = 1, b = 1, c = 10;

      if (a == b)
      {
         a = a + b;
      }
      else
      {
         b = a - b;
      }

      a = a * b;


      while (a < c)
      {
         a = a + 1;
      }
   }
}
