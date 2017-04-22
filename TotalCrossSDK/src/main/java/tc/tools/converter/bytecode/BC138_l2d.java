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

public class BC138_l2d extends Conversion
{
   public BC138_l2d()
   {
      super(-1,-1, LONG, DOUBLE);
   }
   public void exec()
   {
      stack[stackPtr-1].asDouble = (double)stack[stackPtr-1].asLong;
   }
}
