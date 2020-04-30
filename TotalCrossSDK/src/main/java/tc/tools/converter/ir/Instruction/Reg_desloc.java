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

public class Reg_desloc extends SingleInstruction {
  public int reg;
  public int desloc;

  public Reg_desloc(int op, int line, int r, int d) {
    super(op, line);
    reg = r;
    desloc = d;
  }

  public Reg_desloc(int op, int line) {
    super(op, line);
  }

  public void set(int r, int d) {
    reg = r;
    desloc = d;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + desloc;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_desloc__reg(reg);
    tc.reg_desloc__desloc(desloc);
    vcode.addElement(tc);
  }
}
