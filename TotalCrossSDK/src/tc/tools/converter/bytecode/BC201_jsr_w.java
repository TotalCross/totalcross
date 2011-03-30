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

// $Id: BC201_jsr_w.java,v 1.9 2011-01-04 13:18:57 guich Exp $

package tc.tools.converter.bytecode;

public class BC201_jsr_w extends Branch
{
   public BC201_jsr_w()
   {
      super(1,readInt32(pc+1));
      pcInc = 5;
   }
   public void exec()
   {
      stack[stackPtr].asInt = pc+5;
      pcInc = jumpTo;
   }
}
