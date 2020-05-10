// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC180_getfield extends LoadStoreField {
  public BC180_getfield() {
    super(readUInt16(pc + 1));
    stackInc = 0;
  }

  @Override
  public void exec() {
    classInstance = (String) stack[stackPtr].asObj;
    stack[stackPtr].asObj = fieldName;
  }
}
