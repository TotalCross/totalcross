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
