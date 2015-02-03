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

import tc.tools.converter.*;

public class BC190_arraylength extends ByteCode
{
   public int resultPos, objPos;

   public BC190_arraylength()
   {
      objPos = resultPos = -1;
      stackInc = 0;
   }
   public void exec()
   {
      TCValue v = stack[stackPtr-1];
      if (!"array".equals(v.asObj))
         System.out.println("Not an array!");
      stack[stackPtr-1].asInt = v.asInt;
   }
}
