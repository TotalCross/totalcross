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

// $Id: BC168_jsr.java,v 1.10 2011-01-04 13:18:54 guich Exp $

package tc.tools.converter.bytecode;

public class BC168_jsr extends Branch
{
   public BC168_jsr()
   {
      super(1,readInt16(pc+1));
      pcInc = 3;
   }
   public void exec()
   {
      stack[stackPtr].asInt = pc+3;
      pcInc = jumpTo;
   }
}
