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

// $Id: BC020_ldc2_w.java,v 1.8 2011-01-04 13:18:55 guich Exp $

package tc.tools.converter.bytecode;

public class BC020_ldc2_w extends BC018_ldc
{
   public BC020_ldc2_w()
   {
      super(readUInt16(pc+1),3);
      stackInc = 2;
   }
}
