// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public abstract class OperandRegD extends OperandReg {
  public OperandRegD() {
    super(opr_regD);
  }

  public OperandRegD(int framePosition) {
    super(opr_regD, framePosition);
  }

  public OperandRegD(String wordIndex, int index) {
    super(opr_regD, wordIndex, index);
  }
}
