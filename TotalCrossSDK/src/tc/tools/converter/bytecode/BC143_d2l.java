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

public class BC143_d2l extends Conversion
{
   public BC143_d2l()
   {
      super(-1,-1, DOUBLE, LONG);
   }
   public void exec()
   {
      stack[stackPtr-1].asLong = (long)stack[stackPtr-1].asDouble;
   }
}
