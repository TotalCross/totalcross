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



package tc.tools.converter.oper;

public abstract class OperandRegD extends OperandReg
{
   public OperandRegD()
   {
      super(opr_regD);
   }

   public OperandRegD(int framePosition)
   {
      super(opr_regD, framePosition);
   }

   public OperandRegD(String wordIndex, int index)
   {
      super(opr_regD, wordIndex, index);
   }
}
