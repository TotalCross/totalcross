// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

public final class JavaStackMapFrame {
  public final int bytecodeOffset;
  public final JavaVerificationType[] locals;
  public final JavaVerificationType[] stack;

  JavaStackMapFrame(int bytecodeOffset, JavaVerificationType[] locals, JavaVerificationType[] stack) {
    this.bytecodeOffset = bytecodeOffset;
    this.locals = locals;
    this.stack = stack;
  }
}
