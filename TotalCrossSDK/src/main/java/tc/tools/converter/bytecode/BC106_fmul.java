// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC106_fmul extends Arithmetic {
  public BC106_fmul() {
    super(-1, -2, -1, FLOAT);
  }

  @Override
  public void exec() {
    stack[stackPtr - 2].asDouble *= stack[stackPtr - 1].asDouble;
  }
}
