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
package tc.tools.converter.ir.web;

import tc.tools.converter.ir.Instruction.Instruction;
import totalcross.util.Vector;

public class Web {
  public int type; // opr_regI / opr_regO / opr_regD / opr_regL
  public int number;
  public Vector dChain = new Vector(32);
  public Vector uChain = new Vector(32);

  public Web(int t, int n) {
    type = t;
    number = n;
  }

  public void addDefinition(Instruction i) {
    dChain.addElement(i);
  }

  public void addUse(Instruction i) {
    uChain.addElement(i);
  }
}
