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

// $Id: BC025_042to045.java,v 1.9 2011-01-04 13:19:03 guich Exp $

package tc.test.converter.testfiles;

public class BC025_042to045
{
   public BC025_042to045()
   {
      Object o1 = null, o2 = null, o3 = null, o4 = null, o;

      o = o1;
      o = o2;
      o = o3;
      o = o4;

      if (false) o1 = o; // remove warnings
   }
}
