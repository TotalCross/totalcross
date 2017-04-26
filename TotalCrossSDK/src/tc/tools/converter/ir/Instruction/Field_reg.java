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

public class Field_reg extends SingleInstruction
{
   public int sym;
   public int _this;
   public int reg;

   public Field_reg(int op, int line, int s, int _t, int r)
   {
      super(op, line);
      sym   = s;
      _this = _t;
      reg   = r;
   }

   public Field_reg(int op, int line)
   {
      super(op, line);
   }

   public void set(int s, int _t, int r)
   {
      sym   = s;
      _this = _t;
      reg   = r;
   }

   public String toString()
   {
      String print;
      print = TCConstants.bcTClassNames[opcode] + " " + sym + ", " + _this + ", " + reg;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(opcode, line);
      tc.field_reg__reg(reg);
      tc.field_reg__sym(sym);
      tc.field_reg__this(_this);
      vcode.addElement(tc);
   }
}
