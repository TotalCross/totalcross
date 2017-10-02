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

public class Reg_arl_s12 extends SingleInstruction {
  public int regI;
  public int base;
  public int desloc;

  public Reg_arl_s12(int op, int line, int r, int b, int d) {
    super(op, line);
    regI = r;
    base = b;
    desloc = d;
  }

  public Reg_arl_s12(int op, int line) {
    super(op, line);
  }

  public void set(int r, int b, int d) {
    regI = r;
    base = b;
    desloc = d;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + regI + ", " + base + ", " + desloc;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_arl_s12__base(base);
    tc.reg_arl_s12__regI(regI);
    tc.reg_arl_s12__desloc(desloc);
    vcode.addElement(tc);
  }
}
