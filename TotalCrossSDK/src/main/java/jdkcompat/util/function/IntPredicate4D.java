// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.function;

import java.util.function.IntPredicate;

public interface IntPredicate4D {
  boolean test(int value);

  default IntPredicate and(IntPredicate other) {
    requireNonNull(other);
    return new IntPredicate() {
      @Override
      public boolean test(int value) {
        return IntPredicate4D.this.test(value) && other.test(value);
      }
    };
  }

  default IntPredicate negate() {
    return new IntPredicate() {
      @Override
      public boolean test(int value) {
        return !IntPredicate4D.this.test(value);
      }
    };
  }

  default IntPredicate or(IntPredicate other) {
    requireNonNull(other);
    return new IntPredicate() {
      @Override
      public boolean test(int value) {
        return IntPredicate4D.this.test(value) || other.test(value);
      }
    };
  }

  static void requireNonNull(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }
}
