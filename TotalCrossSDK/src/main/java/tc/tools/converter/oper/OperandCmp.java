// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandCmp extends Operand {
  public Operand v1, v2;

  public OperandCmp(Operand v1, Operand v2) {
    super(opr_cmp);
    this.v1 = v1;
    this.v2 = v2;
  }
}
