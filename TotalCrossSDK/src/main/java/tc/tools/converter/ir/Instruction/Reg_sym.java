// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.ir.Instruction;

import tc.tools.converter.TCConstants;
import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class Reg_sym extends SingleInstruction {
  public int reg;
  public int sym;

  public Reg_sym(int op, int line, int r, int s) {
    super(op, line);
    reg = r;
    sym = s;
  }

  public Reg_sym(int op, int line) {
    super(op, line);
  }

  public void set(int r, int s) {
    reg = r;
    sym = s;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + "  " + reg + ", " + sym;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_sym__reg(reg);
    tc.reg_sym__sym(sym);
    vcode.addElement(tc);
  }
}
