// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC200_goto_w extends Branch {
  public BC200_goto_w() {
    super(0, readInt32(pc + 1));
    pcInc = 5;
  }

  @Override
  public void exec() {
    pcInc = jumpTo;
  }
}
