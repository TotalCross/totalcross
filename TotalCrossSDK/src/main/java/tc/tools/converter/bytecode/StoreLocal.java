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

public class StoreLocal extends ByteCode {
  /** Index in the local array */
  public int targetIdx, srcStack;

  public StoreLocal(int targetIdx, int srcStack, int type) {
    this.targetIdx = targetIdx;
    this.srcStack = stackInc = srcStack;
    this.targetType = type;
  }

  @Override
  public void exec() {
    local[targetIdx].copyFrom(stack[stackPtr + srcStack]);
  }
}
