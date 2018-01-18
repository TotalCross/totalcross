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

public class Reg_reg extends SingleInstruction {
  public int r0;
  public int r1;

  public Reg_reg(int op, int line, int r0, int r1) {
    super(op, line);
    this.r0 = r0;
    this.r1 = r1;
  }

  public Reg_reg(int op, int line) {
    super(op, line);
  }

  public void set(int r0, int r1) {
    this.r0 = r0;
    this.r1 = r1;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + r0 + ", " + r1;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_reg__reg0(r0);
    tc.reg_reg__reg1(r1);
    vcode.addElement(tc);
  }
}
