// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandRegO extends OperandReg {
  public OperandRegO() {
    super(opr_regO);
  }

  public OperandRegO(int framePosition) {
    super(opr_regO, framePosition);
  }

  public OperandRegO(String wordIndex, int index) {
    super(opr_regO, wordIndex, index);
  }
}
