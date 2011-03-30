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

// $Id: OperandExternal.java,v 1.9 2011-01-04 13:19:18 guich Exp $

package tc.tools.converter.oper;

public class OperandExternal extends Operand
{
   public OperandReg regO;
   public OperandSym sym;

   public OperandExternal(OperandReg reg, OperandSym sym)
   {
      super(sym.kind);
      regO = reg;
      this.sym = sym;
      nWords = sym.nWords;
   }
}
