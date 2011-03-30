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

// $Id: BC017_sipush.java,v 1.12 2011-01-04 13:18:55 guich Exp $

package tc.tools.converter.bytecode;

public class BC017_sipush extends LoadLocal
{
   public BC017_sipush()
   {
      super(readInt16(pc+1),INT);
      pcInc = 3;
   }
   public void exec()
   {
      stack[stackPtr].asInt = localIdx;
   }
}
