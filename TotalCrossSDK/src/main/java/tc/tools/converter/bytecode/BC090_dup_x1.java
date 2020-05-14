// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC090_dup_x1 extends StackManipulation {
  public BC090_dup_x1() {
    super(1, false);
  }

  @Override
  public void exec() {
    stack[stackPtr].copyFrom(stack[stackPtr - 1]);
    stack[stackPtr - 1].copyFrom(stack[stackPtr - 2]);
    stack[stackPtr - 2].copyFrom(stack[stackPtr]);
  }
}
