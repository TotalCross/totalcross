// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.function;

import java.util.function.DoublePredicate;

public interface DoublePredicate4D {
  boolean test(double value);

  default DoublePredicate and(DoublePredicate other) {
    requireNonNull(other);
    return new DoublePredicate() {
      @Override
      public boolean test(double value) {
        return DoublePredicate4D.this.test(value) && other.test(value);
      }
    };
  }

  default DoublePredicate negate() {
    return new DoublePredicate() {
      @Override
      public boolean test(double value) {
        return !DoublePredicate4D.this.test(value);
      }
    };
  }

  default DoublePredicate or(DoublePredicate other) {
    requireNonNull(other);
    return new DoublePredicate() {
      @Override
      public boolean test(double value) {
        return DoublePredicate4D.this.test(value) || other.test(value);
      }
    };
  }

  static void requireNonNull(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }
}
