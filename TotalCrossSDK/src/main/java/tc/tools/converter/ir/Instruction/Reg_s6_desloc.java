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

public class Reg_s6_desloc extends SingleInstruction {
  public int reg;
  public int s6;
  public int desloc;

  public Reg_s6_desloc(int op, int line, int r, int s, int d) {
    super(op, line);
    reg = r;
    s6 = s;
    desloc = d;
  }

  public Reg_s6_desloc(int op, int line) {
    super(op, line);
  }

  public void set(int r, int s, int d) {
    reg = r;
    s6 = s;
    desloc = d;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + s6 + ", " + desloc;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_s6_desloc__desloc(desloc);
    tc.reg_s6_desloc__s6(s6);
    tc.reg_s6_desloc__reg(reg);
    vcode.addElement(tc);
  }
}
