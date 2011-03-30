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

// $Id: BC016_bipush.java,v 1.12 2011-01-04 13:18:57 guich Exp $

package tc.tools.converter.bytecode;

public class BC016_bipush extends LoadLocal
{
   public BC016_bipush()
   {
      super(code[pc+1],INT);
      pcInc = 2;
   }
   public void exec()
   {
      stack[stackPtr].asInt = localIdx;
   }
}
