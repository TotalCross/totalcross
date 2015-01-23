/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.converter.bytecode;

public class BC166_if_acmpne extends ConditionalBranch
{
   public BC166_if_acmpne()
   {
      super(-2,-2,-1,readInt16(pc+1),OBJECT);
   }
   public void exec()
   {
      if (stack[stackPtr-2].asObj != stack[stackPtr-1].asObj)
         pcInc = jumpIfTrue;
      else
         pcInc = jumpIfFalse;
   }
}
