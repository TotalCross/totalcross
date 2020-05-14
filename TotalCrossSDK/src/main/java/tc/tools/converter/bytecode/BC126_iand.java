// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC126_iand extends Logical {
  public BC126_iand() {
    super(-1, -2, -1, INT);
  }

  @Override
  public void exec() {
    stack[-2].asInt &= stack[-1].asInt;
  }
}
