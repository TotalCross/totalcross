// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandRegL extends OperandReg {
  public OperandRegL() {
    super(opr_regL);
    nWords = 2;
  }

  public OperandRegL(int framePosition) {
    super(opr_regL, framePosition);
    nWords = 2;
  }

  public OperandRegL(String wordIndex, int index) {
    super(opr_regL, wordIndex, index);
    nWords = 2;
  }
}
