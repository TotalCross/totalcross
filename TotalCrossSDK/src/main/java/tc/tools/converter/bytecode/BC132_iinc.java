// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class BC132_iinc extends Arithmetic {
  public BC132_iinc() {
    super(0, readUInt8(pc + 1), code[pc + 2], INT);
    pcInc = 3;
  }

  public BC132_iinc(boolean wide) {
    super(0, readUInt16(pc + 2), readInt16(pc + 4), INT); // note: pc+1 stores the opcode
    pcInc = 5;
    bc = IINC;
  }

  @Override
  public void exec() {
    local[result].asInt += operand;
  }
}
