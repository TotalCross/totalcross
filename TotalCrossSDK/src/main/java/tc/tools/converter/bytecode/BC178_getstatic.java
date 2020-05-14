// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC178_getstatic extends LoadStoreField {
  public BC178_getstatic() {
    super(readUInt16(pc + 1));
    stackInc = 1;
  }

  @Override
  public void exec() {
    stack[stackPtr].asObj = fieldName;
  }
}
