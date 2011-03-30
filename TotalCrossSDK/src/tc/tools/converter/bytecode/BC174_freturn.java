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

// $Id: BC174_freturn.java,v 1.9 2011-01-04 13:18:57 guich Exp $

package tc.tools.converter.bytecode;

public class BC174_freturn extends Return
{
   public BC174_freturn()
   {
      super(1,-1,FLOAT);
   }
   public void exec()
   {
      returnValue.asDouble = stack[stackPtr-1].asDouble;
      returnValue.type = FLOAT;
   }
}
