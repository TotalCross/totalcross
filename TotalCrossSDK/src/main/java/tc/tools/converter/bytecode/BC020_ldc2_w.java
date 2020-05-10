// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC020_ldc2_w extends BC018_ldc {
  public BC020_ldc2_w() {
    super(readUInt16(pc + 1), 3);
    stackInc = 2;
  }
}
