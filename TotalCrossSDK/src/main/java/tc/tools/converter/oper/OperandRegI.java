// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandRegI extends OperandReg {
  public OperandRegI() {
    super(opr_regI);
  }

  public OperandRegI(int framePosition) {
    super(opr_regI, framePosition);
  }

  public OperandRegI(int kind, int framePosition) // kind: regIb | regIs | regIc
  {
    super(kind, framePosition);
  }

  public OperandRegI(String wordIndex, int index) {
    super(opr_regI, wordIndex, index);
  }
}
