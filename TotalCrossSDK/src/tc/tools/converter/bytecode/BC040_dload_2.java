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

// $Id: BC040_dload_2.java,v 1.9 2011-01-04 13:18:55 guich Exp $

package tc.tools.converter.bytecode;

public class BC040_dload_2 extends LoadLocal
{
   public BC040_dload_2()
   {
      super(2,DOUBLE);
      stackInc = 2;
   }
}
