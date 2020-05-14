// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC110_fdiv extends Arithmetic {
  public BC110_fdiv() {
    super(-1, -2, -1, FLOAT);
  }

  @Override
  public void exec() {
    stack[stackPtr - 2].asDouble /= stack[stackPtr - 1].asDouble;
  }
}
