// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools.converter.java;

public final class JavaVerificationType {
  public enum Kind {
    TOP, INTEGER, FLOAT, DOUBLE, LONG, NULL, UNINITIALIZED_THIS, OBJECT, UNINITIALIZED
  }

  public final Kind kind;
  public final String className;
  public final int newInstructionOffset;

  JavaVerificationType(Kind kind, String className, int newInstructionOffset) {
    this.kind = kind;
    this.className = className;
    this.newInstructionOffset = newInstructionOffset;
  }

  static JavaVerificationType simple(Kind kind) {
    return new JavaVerificationType(kind, null, -1);
  }

  static JavaVerificationType object(String className) {
    return new JavaVerificationType(Kind.OBJECT, className, -1);
  }

  static JavaVerificationType uninitialized(int offset) {
    return new JavaVerificationType(Kind.UNINITIALIZED, null, offset);
  }
}
