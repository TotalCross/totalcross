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

public class ConditionalBranch extends Branch
{
   public int left, right, jumpIfTrue, jumpIfFalse;

   public ConditionalBranch(int stackInc, int left, int right, int jumpIfTrue, int type)
   {
      super(stackInc, jumpIfTrue);
      this.left = left;
      this.right = right;
      this.jumpIfTrue = jumpIfTrue + pcInMethod;
      this.pcInc = this.jumpIfFalse = 3;
      this.targetType = type;
   }
}
