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

// $Id: BC144_d2f.java,v 1.9 2011-01-04 13:18:55 guich Exp $

package tc.tools.converter.bytecode;

public class BC144_d2f extends Conversion // this conversion should be ignored
{
   public BC144_d2f()
   {
      super(-1,-1, DOUBLE, FLOAT);
   }
   public void exec()
   {
      //stack[stackPtr-1].asDouble = (double)stack[stackPtr-1].asDouble;
   }
}
