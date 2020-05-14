// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC176_areturn extends Return {
  public BC176_areturn() {
    super(1, -1, OBJECT);
  }

  @Override
  public void exec() {
    returnValue.asObj = stack[stackPtr - 1].asObj;
    returnValue.type = OBJECT;
  }
}
