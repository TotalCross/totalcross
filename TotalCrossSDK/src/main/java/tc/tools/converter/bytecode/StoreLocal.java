/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

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
