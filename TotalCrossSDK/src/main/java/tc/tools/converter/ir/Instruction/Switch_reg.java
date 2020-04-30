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

public class Switch_reg extends MultiInstruction {
  public int key;
  public int n;

  public Switch_reg(int op, int line, int k, int n) {
    super(op, line);
    key = k;
    this.n = n;
  }

  public Switch_reg(int op, int line) {
    super(op, line);
  }

  public void set(int k, int n) {
    key = k;
    this.n = n;
  }

  @Override
  public String toString() {
    String print = TCConstants.bcTClassNames[opcode] + " " + key + ", " + n;
    for (int i = 0; i < params.length; i++) {
      Parameter p = params[i];
      print += "\n" + p.toString();
    }
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.len = len;
    tc.switch_reg__key(key);
    tc.switch_reg__n(n);
    vcode.addElement(tc);
    for (int i = 0; i < params.length; i++) {
      Parameter p = params[i];
      p.toTCCode(vcode);
    }
  }
}
