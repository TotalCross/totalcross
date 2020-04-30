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

public class Field_reg extends SingleInstruction {
  public int sym;
  public int _this;
  public int reg;

  public Field_reg(int op, int line, int s, int _t, int r) {
    super(op, line);
    sym = s;
    _this = _t;
    reg = r;
  }

  public Field_reg(int op, int line) {
    super(op, line);
  }

  public void set(int s, int _t, int r) {
    sym = s;
    _this = _t;
    reg = r;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + sym + ", " + _this + ", " + reg;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.field_reg__reg(reg);
    tc.field_reg__sym(sym);
    tc.field_reg__this(_this);
    vcode.addElement(tc);
  }
}
