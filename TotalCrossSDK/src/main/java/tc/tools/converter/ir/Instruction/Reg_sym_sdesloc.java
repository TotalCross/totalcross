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

public class Reg_sym_sdesloc extends SingleInstruction {
  public int reg;
  public int sym;
  public int desloc;

  public Reg_sym_sdesloc(int op, int line, int r, int s, int d) {
    super(op, line);
    reg = r;
    sym = s;
    desloc = d;
  }

  public Reg_sym_sdesloc(int op, int line) {
    super(op, line);
  }

  public void set(int r, int s, int d) {
    reg = r;
    sym = s;
    desloc = d;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + reg + ", " + sym + ", " + desloc;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.reg_sym_sdesloc__desloc(desloc);
    tc.reg_sym_sdesloc__reg(reg);
    tc.reg_sym_sdesloc__sym(sym);
    vcode.addElement(tc);
  }
}
