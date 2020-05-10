// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC144_d2f extends Conversion // this conversion should be ignored
{
  public BC144_d2f() {
    super(-1, -1, DOUBLE, FLOAT);
  }

  @Override
  public void exec() {
    //stack[stackPtr-1].asDouble = (double)stack[stackPtr-1].asDouble;
  }
}
