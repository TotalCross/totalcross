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

// $Id: BC198to199.java,v 1.7 2011-01-04 13:19:02 guich Exp $

package tc.test.converter.testfiles;

public class BC198to199
{
   public BC198to199()
   {
      Object o1 = new Object(), o2;

      if (o1 == null)
      {
         o1 = null;
      }

      if (o1 != null)
         o2 = null;

      if (false) o1 = o2; // remove warnings
   }
}
