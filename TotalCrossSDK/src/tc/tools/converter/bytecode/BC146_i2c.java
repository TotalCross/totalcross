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

public class BC146_i2c extends Conversion // this conversion should be ignored
{
   public BC146_i2c()
   {
      super(-1,-1, INT, CHAR);
   }
   public void exec()
   {
      stack[stackPtr-1].asInt = (char)stack[stackPtr-1].asInt;
   }
}
