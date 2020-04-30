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

public class Call extends MultiInstruction {
  public int sym;
  public int _this;
  public int retOrParam;
  public boolean isVoid;
  public boolean isStatic;
  public int retOrParamType = type_Void;

  public Call(int op, int line, int s, int _t, int ret, int type) {
    super(op, line);
    sym = s;
    _this = _t;
    retOrParam = ret;
    retOrParamType = type;
  }

  public Call(int op, int line) {
    super(op, line);
  }

  public void set(int s, int _t, int ret, int type) {
    sym = s;
    _this = _t;
    retOrParam = ret;
    retOrParamType = type;
  }

  @Override
  public String toString() {
    String print = TCConstants.bcTClassNames[opcode] + " " + sym + ", " + _this + ", " + retOrParam;
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        print += "\n" + params[i];
      }
    }
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.len = len;
    tc.mtd__sym(sym);
    tc.mtd__this(_this);
    tc.mtd__retOrParam(retOrParam);
    vcode.addElement(tc);
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        params[i].toTCCode(vcode);
      }
    }
  }
}
