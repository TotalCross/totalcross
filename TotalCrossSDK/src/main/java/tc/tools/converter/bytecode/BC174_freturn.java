// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC174_freturn extends Return {
  public BC174_freturn() {
    super(1, -1, FLOAT);
  }

  @Override
  public void exec() {
    returnValue.asDouble = stack[stackPtr - 1].asDouble;
    returnValue.type = FLOAT;
  }
}
