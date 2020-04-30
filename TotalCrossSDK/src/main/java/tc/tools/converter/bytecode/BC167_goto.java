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

public class BC167_goto extends Branch {
  public BC167_goto() {
    super(0, readInt16(pc + 1));
    this.pcInc = 3; // this is the instruction length
  }

  @Override
  public void exec() {
    pcInc = jumpTo - pcInMethod; // this is the target address
  }
}
