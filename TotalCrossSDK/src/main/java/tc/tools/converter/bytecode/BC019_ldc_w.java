// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC019_ldc_w extends BC018_ldc {
  public BC019_ldc_w() {
    super(readUInt16(pc + 1), 3);
  }
}
