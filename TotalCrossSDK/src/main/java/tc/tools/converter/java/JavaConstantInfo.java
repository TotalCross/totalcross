// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

public final class JavaConstantInfo {
  public int index1, index2;
  public byte type;

  public JavaConstantInfo(byte type, int index1) {
    this.index1 = index1;
    this.type = type;
  }

  public JavaConstantInfo(byte type, int index1, int index2) {
    this.index1 = index1;
    this.index2 = index2;
    this.type = type;
  }
}
