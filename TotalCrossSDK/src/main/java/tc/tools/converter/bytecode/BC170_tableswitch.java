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

public class BC170_tableswitch extends Switch {
  public int low, high;

  public BC170_tableswitch() {
    super(-1);
    int npc = pc + 1;
    npc += ((4 - ((npc /* -pc0 */) & 3)) & 3); // padding
    def = readInt32(npc) + pc;
    low = readInt32(npc + 4);
    high = readInt32(npc + 8);
    npc += 12;
    jumps = new int[high - low + 1];
    for (int i = 0; i < jumps.length; i++, npc += 4) {
      jumps[i] = readInt32(npc) + pc;
    }
    pcInc = npc - pc;
  }

  @Override
  public void exec() {
    int key = stack[stackPtr - 1].asInt;
    pcInc = (key < low || key > high) ? def : jumps[key - low];
  }
}
