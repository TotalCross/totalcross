// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.function;

import java.util.function.BiPredicate;

public interface BiPredicate4D<T, U> {
  boolean test(T t, U u);

  default BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
    requireNonNull(other);
    return new BiPredicate<T, U>() {
      @Override
      public boolean test(T left, U right) {
        return BiPredicate4D.this.test(left, right) && other.test(left, right);
      }
    };
  }

  default BiPredicate<T, U> negate() {
    return new BiPredicate<T, U>() {
      @Override
      public boolean test(T left, U right) {
        return !BiPredicate4D.this.test(left, right);
      }
    };
  }

  default BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
    requireNonNull(other);
    return new BiPredicate<T, U>() {
      @Override
      public boolean test(T left, U right) {
        return BiPredicate4D.this.test(left, right) || other.test(left, right);
      }
    };
  }

  static void requireNonNull(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }
}
