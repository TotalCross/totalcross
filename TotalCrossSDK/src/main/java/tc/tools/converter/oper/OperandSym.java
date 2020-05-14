// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandSym extends Operand {
  public int index;

  public OperandSym(int kind, int index) {
    super(kind);
    this.index = index;
  }

  @Override
  public boolean isSym() {
    return true;
  }
}
