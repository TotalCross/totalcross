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

public class OperandArrayAccess extends Operand
{
   public OperandReg base;
   public OperandReg index;

   public OperandArrayAccess(int kind, OperandReg base, OperandReg index)
   {
      super(kind);
      this.base  = base;
      this.index = index;
      nWords = base.nWords;
   }
}
