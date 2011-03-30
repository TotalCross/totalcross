/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: OperandArrayAccess.java,v 1.10 2011-01-04 13:19:18 guich Exp $

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
