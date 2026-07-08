// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.function;

import java.util.function.LongPredicate;

public interface LongPredicate4D {
  boolean test(long value);

  default LongPredicate and(LongPredicate other) {
    requireNonNull(other);
    return new LongPredicate() {
      @Override
      public boolean test(long value) {
        return LongPredicate4D.this.test(value) && other.test(value);
      }
    };
  }

  default LongPredicate negate() {
    return new LongPredicate() {
      @Override
      public boolean test(long value) {
        return !LongPredicate4D.this.test(value);
      }
    };
  }

  default LongPredicate or(LongPredicate other) {
    requireNonNull(other);
    return new LongPredicate() {
      @Override
      public boolean test(long value) {
        return LongPredicate4D.this.test(value) || other.test(value);
      }
    };
  }

  static void requireNonNull(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }
}
