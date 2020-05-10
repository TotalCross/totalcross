// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class Branch extends ByteCode {
  public int jumpTo;

  public Branch(int stackInc, int jumpTo) {
    super.stackInc = stackInc;
    this.jumpTo = jumpTo + pcInMethod;
  }

  @Override
  public String toString() {
    return super.toString() + "->" + jumpTo;
  }
}
