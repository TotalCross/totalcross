// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
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
