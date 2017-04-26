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

public class Reg_ar extends SingleInstruction
{
   public int base;
   public int idx;
   public int reg;

   public Reg_ar(int op, int line, int b, int i, int r)
   {
      super(op, line);
      base = b;
      idx  = i;
      reg  = r;
   }

   public Reg_ar(int op, int line)
   {
      super(op, line);
   }

   public void set(int b, int i, int r)
   {
      base = b;
      idx  = i;
      reg  = r;
   }

   public String toString()
   {
      String print;
      print = TCConstants.bcTClassNames[opcode] + " " + base + ", " + idx + ", " + reg;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(opcode, line);
      tc.reg_ar__reg(reg);
      tc.reg_ar__base(base);
      tc.reg_ar__idx(idx);
      vcode.addElement(tc);
   }
}
