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

public class Reg_sym_sdesloc extends SingleInstruction
{
   public int reg;
   public int sym;
   public int desloc;

   public Reg_sym_sdesloc(int op, int line, int r, int s, int d)
   {
      super(op, line);
      reg    = r;
      sym    = s;
      desloc = d;
   }

   public Reg_sym_sdesloc(int op, int line)
   {
      super(op, line);
   }

   public void set(int r, int s, int d)
   {
      reg    = r;
      sym    = s;
      desloc = d;
   }

   public String toString()
   {
      String print;
      print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + sym + ", " + desloc;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(opcode, line);
      tc.reg_sym_sdesloc__desloc(desloc);
      tc.reg_sym_sdesloc__reg(reg);
      tc.reg_sym_sdesloc__sym(sym);
      vcode.addElement(tc);
   }
}
