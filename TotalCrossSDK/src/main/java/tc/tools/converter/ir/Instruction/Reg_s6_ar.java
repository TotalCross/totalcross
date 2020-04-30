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

public class Reg_s6_ar extends SingleInstruction {
  public int reg;
  public int base;
  public int idx;
  public int s6;

  public Reg_s6_ar(int op, int line, int r, int b, int i, int s) {
    super(op, line);
    reg = r;
    base = b;
    idx = i;
    s6 = s;
  }

  public Reg_s6_ar(int op, int line) {
    super(op, line);
  }

  public void set(int r, int b, int i, int s) {
    reg = r;
    base = b;
    idx = i;
    s6 = s;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + base + ", " + idx + ", " + s6;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_s6_ar__reg(reg);
    tc.reg_s6_ar__s6(s6);
    tc.reg_s6_ar__base(base);
    tc.reg_s6_ar__idx(idx);
    vcode.addElement(tc);
  }
}
