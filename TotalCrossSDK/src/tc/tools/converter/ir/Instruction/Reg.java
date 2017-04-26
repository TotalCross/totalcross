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



package tc.tools.converter.ir.Instruction;

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Reg extends SingleInstruction
{
   public int reg;

   public Reg(int op, int line, int r)
   {
      super(op, line);
      reg = r;
   }

   public Reg(int op, int line)
   {
      super(op, line);
   }

   public void set(int r)
   {
      reg = r;
   }

   public String toString()
   {
      String print;
      print = TCConstants.bcTClassNames[opcode] + " " + reg;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(opcode, line);
      tc.reg__reg(reg);
      vcode.addElement(tc);
   }
}
