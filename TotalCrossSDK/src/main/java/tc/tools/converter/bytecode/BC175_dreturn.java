// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC175_dreturn extends Return {
  public BC175_dreturn() {
    super(1, -1, DOUBLE);
  }

  @Override
  public void exec() {
    returnValue.asDouble = stack[stackPtr - 1].asDouble;
    returnValue.type = DOUBLE;
  }
}
