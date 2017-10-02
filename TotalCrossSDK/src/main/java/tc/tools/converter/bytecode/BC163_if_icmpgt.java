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

public class BC163_if_icmpgt extends ConditionalBranch {
  public BC163_if_icmpgt() {
    super(-2, -2, -1, readInt16(pc + 1), INT);
  }

  @Override
  public void exec() {
    if (stack[stackPtr - 2].asInt > stack[stackPtr - 1].asInt) {
      pcInc = jumpIfTrue;
    } else {
      pcInc = jumpIfFalse;
    }
  }
}
