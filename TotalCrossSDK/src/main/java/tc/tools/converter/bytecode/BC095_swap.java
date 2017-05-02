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

public class BC095_swap extends StackManipulation
{
   private static TCValue vtemp = new TCValue();
   public BC095_swap()
   {
      super(0,false);
   }
   public void exec()
   {
      vtemp.copyFrom(stack[stackPtr-2]);
      stack[stackPtr-2].copyFrom(stack[stackPtr-1]);
      stack[stackPtr-1].copyFrom(vtemp);
   }
}
