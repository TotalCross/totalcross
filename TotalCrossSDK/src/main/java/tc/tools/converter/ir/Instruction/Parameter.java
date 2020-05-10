// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.ir.Instruction;

import totalcross.util.Vector;

public class Parameter {
  int line;

  public Parameter(int line) {
    this.line = line;
  }

  @Override
  public String toString() {
    return " ";
  }

  public void toTCCode(Vector vcode) {
  }
}
