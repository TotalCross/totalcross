// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC031_lload_1 extends LoadLocal {
  public BC031_lload_1() {
    super(1, LONG);
    stackInc = 2;
  }
}
