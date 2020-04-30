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

public class Inc extends SingleInstruction {
  public int reg;
  public int s16;

  public Inc(int op, int line, int r, int v) {
    super(op, line);
    reg = r;
    s16 = v;
  }

  public Inc(int op, int line) {
    super(op, line);
  }

  public void set(int r, int v) {
    reg = r;
    s16 = v;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + s16;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.inc__reg(reg);
    tc.inc__s16(s16);
    vcode.addElement(tc);
  }
}
