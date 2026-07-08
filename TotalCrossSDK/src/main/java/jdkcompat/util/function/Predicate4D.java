// Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
// Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.util.function;

import java.util.function.Predicate;

public interface Predicate4D<T> {
  boolean test(T t);

  default Predicate<T> and(Predicate<? super T> other) {
    requireNonNull(other);
    return new Predicate<T>() {
      @Override
      public boolean test(T value) {
        return Predicate4D.this.test(value) && other.test(value);
      }
    };
  }

  default Predicate<T> negate() {
    return new Predicate<T>() {
      @Override
      public boolean test(T value) {
        return !Predicate4D.this.test(value);
      }
    };
  }

  default Predicate<T> or(Predicate<? super T> other) {
    requireNonNull(other);
    return new Predicate<T>() {
      @Override
      public boolean test(T value) {
        return Predicate4D.this.test(value) || other.test(value);
      }
    };
  }

  static <T> Predicate<T> isEqual(Object targetRef) {
    return new Predicate<T>() {
      @Override
      public boolean test(T value) {
        return targetRef == null ? value == null : targetRef.equals(value);
      }
    };
  }

  static <T> Predicate<T> not(Predicate<? super T> target) {
    requireNonNull(target);
    return new Predicate<T>() {
      @Override
      public boolean test(T value) {
        return !target.test(value);
      }
    };
  }

  static void requireNonNull(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }
}
