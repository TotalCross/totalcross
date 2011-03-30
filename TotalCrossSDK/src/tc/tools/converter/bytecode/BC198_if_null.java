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

// $Id: BC198_if_null.java,v 1.8 2011-01-04 13:18:57 guich Exp $

package tc.tools.converter.bytecode;

public class BC198_if_null extends ConditionalBranch
{
   public BC198_if_null()
   {
      super(-1,-1,0,readInt16(pc+1),OBJECT);
   }
   public void exec()
   {
      if (stack[stackPtr-1].asObj == null)
         pcInc = jumpIfTrue;
      else
         pcInc = jumpIfFalse;
   }
}
