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
package tc.tools.converter.oper;

public class OperandRegD64 extends OperandRegD {
  public OperandRegD64() {
    super();
    this.nWords = 2;
  }

  public OperandRegD64(int framePosition) {
    super(framePosition);
    this.nWords = 2;
  }

  public OperandRegD64(String wordIndex, int index) {
    super(wordIndex, index);
    nWords = 2;
  }
}
