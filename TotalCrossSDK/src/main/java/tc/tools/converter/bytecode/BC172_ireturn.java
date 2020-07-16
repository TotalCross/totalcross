// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC172_ireturn extends Return {
  public BC172_ireturn() {
    super(1, -1, INT);
  }

  @Override
  public void exec() {
    returnValue.asInt = stack[stackPtr - 1].asInt;
    returnValue.type = INT;
  }
}
