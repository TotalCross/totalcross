// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.ir.Instruction;

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Reg_reg_sym extends SingleInstruction {
  public int r0;
  public int r1;
  public int sym;

  public Reg_reg_sym(int op, int line, int r0, int r1, int s) {
    super(op, line);
    this.r0 = r0;
    this.r1 = r1;
    sym = s;
  }

  public Reg_reg_sym(int op, int line) {
    super(op, line);
  }

  public void set(int r0, int r1, int s) {
    this.r0 = r0;
    this.r1 = r1;
    sym = s;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + r0 + ", " + r1 + ", " + sym;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_reg_sym__reg0(r0);
    tc.reg_reg_sym__reg1(r1);
    tc.reg_reg_sym__sym(sym);
    vcode.addElement(tc);
  }
}
