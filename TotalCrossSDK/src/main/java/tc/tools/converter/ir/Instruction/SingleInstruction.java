// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.ir.Instruction;

import totalcross.util.Vector;

public class SingleInstruction extends Instruction {
  public SingleInstruction(int op, int line) {
    super(op, line);
  }

  @Override
  public void toTCCode(Vector vcode) {
  }
}
