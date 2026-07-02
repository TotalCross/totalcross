// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.converter.java;

public final class JavaBootstrapMethod {
  public final int bootstrapMethodRef;
  public final int[] bootstrapArguments;

  public JavaBootstrapMethod(int bootstrapMethodRef, int[] bootstrapArguments) {
    this.bootstrapMethodRef = bootstrapMethodRef;
    this.bootstrapArguments = bootstrapArguments;
  }
}
