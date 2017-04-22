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

public class BC139_f2i extends Conversion
{
   public BC139_f2i()
   {
      super(-1,-1, FLOAT, INT);
   }
   public void exec()
   {
      double f = stack[stackPtr-1].asDouble;
      stack[stackPtr-1].asInt = (f > 2147483647.0)?0x7FFFFFFF:(f < -2147483648.0)?0x80000000:(int)f;
   }
}
