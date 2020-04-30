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

import tc.tools.converter.TCValue;

public class BC095_swap extends StackManipulation {
  private static TCValue vtemp = new TCValue();

  public BC095_swap() {
    super(0, false);
  }

  @Override
  public void exec() {
    vtemp.copyFrom(stack[stackPtr - 2]);
    stack[stackPtr - 2].copyFrom(stack[stackPtr - 1]);
    stack[stackPtr - 1].copyFrom(vtemp);
  }
}
