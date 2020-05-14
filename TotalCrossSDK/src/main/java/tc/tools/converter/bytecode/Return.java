// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

import tc.tools.converter.TCValue;

public class Return extends ByteCode {
  public int returnValueCount;
  public int answer;
  public TCValue returnValue = new TCValue();

  public Return(int returnValueCount, int answer, int type) {
    this.returnValueCount = returnValueCount;
    this.targetType = type;
    this.answer = answer;
    stackInc = 0;
  }
}
