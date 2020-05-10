// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandReg16 extends OperandRegI {
  public OperandReg16() {
    super();
    kind = opr_reg16;
  }

  public OperandReg16(int framePosition) {
    super(opr_reg16, framePosition);
  }
}
