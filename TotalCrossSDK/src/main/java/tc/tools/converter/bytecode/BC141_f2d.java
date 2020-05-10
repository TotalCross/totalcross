// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC141_f2d extends Conversion // this conversion should be ignored
{
  public BC141_f2d() {
    super(-1, -1, FLOAT, DOUBLE);
  }

  @Override
  public void exec() {
    //stack[stackPtr-1].asDouble = stack[stackPtr-1].asDouble;
  }
}
