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

// $Id: BC145_i2b.java,v 1.9 2011-01-04 13:18:56 guich Exp $

package tc.tools.converter.bytecode;

public class BC145_i2b extends Conversion // this conversion should be ignored
{
   public BC145_i2b()
   {
      super(-1,-1, INT, BYTE);
   }
   public void exec()
   {
      stack[stackPtr-1].asInt = (byte)stack[stackPtr-1].asInt;
   }
}
