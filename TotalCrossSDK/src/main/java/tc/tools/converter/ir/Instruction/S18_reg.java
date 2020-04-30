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

public class S18_reg extends SingleInstruction {
  public int s18;
  public int reg;

  public S18_reg(int op, int line, int s, int r) {
    super(op, line);
    s18 = s;
    reg = r;
  }

  public S18_reg(int op, int line) {
    super(op, line);
  }

  public void set(int s, int r) {
    s18 = s;
    reg = r;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + "  " + reg + ", " + s18;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.s18_reg__reg(reg);
    tc.s18_reg__s18(s18);
    vcode.addElement(tc);
  }
}
