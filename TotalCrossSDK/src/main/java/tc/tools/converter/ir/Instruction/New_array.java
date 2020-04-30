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

public class New_array extends MultiInstruction {
  public int sym;
  public int regO;
  public int arrayLen;

  public New_array(int op, int line, int s, int r, int length) {
    super(op, line);
    sym = s;
    regO = r;
    arrayLen = length;
  }

  public New_array(int op, int line) {
    super(op, line);
  }

  public void set(int s, int r, int length) {
    sym = s;
    regO = r;
    arrayLen = length;
  }

  @Override
  public String toString() {
    String print;
    print = TCConstants.bcTClassNames[opcode] + " " + sym + ", " + regO + ", " + arrayLen;
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        Parameter p = params[i];
        print += "\n" + p.toString();
      }
    }
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(opcode, line);
    tc.len = len;
    tc.newarray__regO(regO);
    tc.newarray__sym(sym);
    tc.newarray__lenOrRegIOrDims(arrayLen);
    vcode.addElement(tc);
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        Parameter p = params[i];
        p.toTCCode(vcode);
      }
    }
  }
}
