// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.bytecode;

public class ConditionalBranch extends Branch {
  public int left, right, jumpIfTrue, jumpIfFalse;

  public ConditionalBranch(int stackInc, int left, int right, int jumpIfTrue, int type) {
    super(stackInc, jumpIfTrue);
    this.left = left;
    this.right = right;
    this.jumpIfTrue = jumpIfTrue + pcInMethod;
    this.pcInc = this.jumpIfFalse = 3;
    this.targetType = type;
  }
}
