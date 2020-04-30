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

public class Comparison extends ByteCode {
  public int result, left, right, srcType;

  public Comparison(int stackInc, int result, int left, int right, int srcType) {
    this.stackInc = stackInc;
    this.result = result;
    this.left = left;
    this.right = right;
    this.srcType = srcType;
    targetType = BOOLEAN;
  }
}
