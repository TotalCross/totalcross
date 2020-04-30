// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC094_dup2_x2 extends StackManipulation {
  public BC094_dup2_x2() {
    super(2, true);
  }

  @Override
  public void exec() {
    stack[stackPtr + 1].copyFrom(stack[stackPtr - 1]);
    stack[stackPtr].copyFrom(stack[stackPtr - 2]);
    stack[stackPtr - 1].copyFrom(stack[stackPtr - 3]);
    stack[stackPtr - 2].copyFrom(stack[stackPtr - 4]);
    stack[stackPtr - 3].copyFrom(stack[stackPtr + 1]);
    stack[stackPtr - 4].copyFrom(stack[stackPtr]);
  }
}
