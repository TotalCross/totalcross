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

public class BC090_dup_x1 extends StackManipulation
{
   public BC090_dup_x1()
   {
      super(1,false);
   }
   public void exec()
   {
      stack[stackPtr  ].copyFrom(stack[stackPtr-1]);
      stack[stackPtr-1].copyFrom(stack[stackPtr-2]);
      stack[stackPtr-2].copyFrom(stack[stackPtr  ]);
   }
}
