// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

public final class JavaConstantInfo {
  public int index1, index2;
  public int type;

  public JavaConstantInfo(int type, int index1) {
    this.index1 = index1;
    this.type = type;
  }

  public JavaConstantInfo(int type, int index1, int index2) {
    this.index1 = index1;
    this.index2 = index2;
    this.type = type;
  }
}
