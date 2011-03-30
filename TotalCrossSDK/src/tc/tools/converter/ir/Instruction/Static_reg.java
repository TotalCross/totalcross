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

// $Id: Static_reg.java,v 1.9 2011-01-04 13:19:06 guich Exp $

package tc.tools.converter.ir.Instruction;

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Static_reg extends SingleInstruction
{
   public int reg;
   public int sym;

   public Static_reg(int op, int line, int r, int s)
   {
      super(op, line);
      reg = r;
      sym = s;
   }

   public Static_reg(int op, int line)
   {
      super(op, line);
   }

   public void set(int r, int s)
   {
      reg = r;
      sym = s;
   }

   public String toString()
   {
      String print;
      print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + sym;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(opcode, line);
      tc.static_reg__reg(reg);
      tc.static_reg__sym(sym);
      vcode.addElement(tc);
   }
}
