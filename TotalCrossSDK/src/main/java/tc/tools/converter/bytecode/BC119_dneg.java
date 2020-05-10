// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC119_dneg extends Arithmetic {
  public BC119_dneg() {
    super(0, -1, -1, DOUBLE);
  }

  @Override
  public void exec() {
    stack[stackPtr - 1].asDouble = -stack[stackPtr - 1].asDouble;
  }
}
