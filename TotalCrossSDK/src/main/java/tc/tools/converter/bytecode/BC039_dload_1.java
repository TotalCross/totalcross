// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC039_dload_1 extends LoadLocal {
  public BC039_dload_1() {
    super(1, DOUBLE);
    stackInc = 2;
  }
}
