// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandArrayAccess extends Operand {
  public OperandReg base;
  public OperandReg index;

  public OperandArrayAccess(int kind, OperandReg base, OperandReg index) {
    super(kind);
    this.base = base;
    this.index = index;
    nWords = base.nWords;
  }
}
