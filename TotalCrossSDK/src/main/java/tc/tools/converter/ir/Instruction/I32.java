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

import tc.tools.converter.tclass.TCCode;
import totalcross.util.Vector;

public class I32 extends Parameter {
  public int i32;

  public I32(int line, int v) {
    super(line);
    i32 = v;
  }

  public I32(int line) {
    super(line);
  }

  public void set(int v) {
    i32 = v;
  }

  @Override
  public String toString() {
    String print;
    print = "" + i32;
    return print;
  }

  @Override
  public void toTCCode(Vector vcode) {
    TCCode tc = new TCCode(line);
    tc.len = 0;
    tc.i32__i32(i32);
    vcode.addElement(tc);
  }
}
