// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC182_invokevirtual extends MethodCall {
  public BC182_invokevirtual() {
    super(readUInt16(pc + 1));
  }

  @Override
  public void exec() {
  }
}
