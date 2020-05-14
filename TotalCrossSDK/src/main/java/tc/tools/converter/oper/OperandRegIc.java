// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandRegIc extends OperandRegI {
  public OperandRegIc() {
    super();
    kind = opr_regIc;
  }

  public OperandRegIc(int framePosition) {
    super(opr_regIc, framePosition);
  }
}
