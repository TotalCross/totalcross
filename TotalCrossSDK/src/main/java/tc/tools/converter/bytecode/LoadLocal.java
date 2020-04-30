// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class LoadLocal extends ByteCode {
  /** Index in the local array */
  public int localIdx;

  public LoadLocal(int idx, int type) {
    this.localIdx = idx;
    this.targetType = type;
  }

  @Override
  public void exec() {
    stack[stackPtr].copyFrom(local[localIdx]);
  }
}
