// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.ir.Instruction;

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Reg_reg_reg extends SingleInstruction {
  public int r0;
  public int r1;
  public int r2;

  public Reg_reg_reg(int op, int line, int r0, int r1, int r2) {
    super(op, line);
    this.r0 = r0;
    this.r1 = r1;
    this.r2 = r2;
  }

  public Reg_reg_reg(int op, int line) {
    super(op, line);
  }

  public void set(int r0, int r1, int r2) {
    this.r0 = r0;
    this.r1 = r1;
    this.r2 = r2;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + r0 + ", " + r1 + ", " + r2;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_reg_reg__reg0(r0);
    tc.reg_reg_reg__reg1(r1);
    tc.reg_reg_reg__reg2(r2);
    vcode.addElement(tc);
  }
}
