// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC108_idiv extends Arithmetic {
  public BC108_idiv() {
    super(-1, -2, -1, INT);
  }

  @Override
  public void exec() {
    stack[stackPtr - 2].asInt /= stack[stackPtr - 1].asInt;
  }
}
