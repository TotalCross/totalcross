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

public class BC142_d2i extends Conversion // this conversion should be ignored
{
   public BC142_d2i()
   {
      super(-1,-1, DOUBLE, INT);
   }
   public void exec()
   {
      stack[stackPtr-1].asInt = (int)stack[stackPtr-1].asDouble;
   }
}
