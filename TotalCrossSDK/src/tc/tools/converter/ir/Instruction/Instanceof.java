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

public class Instanceof extends SingleInstruction
{
   public int sym;
   public int regO;
   public int regI;

   public Instanceof(int op, int line, int s, int ro, int ri)
   {
      super(op, line);
      sym  = s;
      regO = ro;
      regI = ri;
   }

   public Instanceof(int op, int line)
   {
      super(op, line);
   }

   public void set(int s, int ro, int ri)
   {
      sym  = s;
      regO = ro;
      regI = ri;
   }

   public String toString()
   {
      String print;
      print = TCConstants.bcTClassNames[opcode] + " " + sym + ", " + regO + ", " + regI;
      return print;
   }

   public void toTCCode(Vector vcode)
   {
      TCCode tc = new TCCode(opcode, line);
      tc.instanceof__regI(regI);
      tc.instanceof__regO(regO);
      tc.instanceof__sym(sym);
      vcode.addElement(tc);
   }
}
