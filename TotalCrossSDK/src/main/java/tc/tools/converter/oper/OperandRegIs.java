// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.oper;

public class OperandRegIs extends OperandRegI {
  public OperandRegIs() {
    super();
    kind = opr_regIs;
  }

  public OperandRegIs(int framePosition) {
    super(opr_regIs, framePosition);
  }
}
