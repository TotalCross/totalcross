// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandExternal extends Operand {
  public OperandReg regO;
  public OperandSym sym;

  public OperandExternal(OperandReg reg, OperandSym sym) {
    super(sym.kind);
    regO = reg;
    this.sym = sym;
    nWords = sym.nWords;
  }
}
