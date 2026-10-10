// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.sys;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConvertRadixTest {
  private static final int[] RADICES = { 2, 8, 10, 16 };
  private static final int[] INT_VALUES = {
      0, 1, -1, 42, -42, 0x1234abcd, -0x1234abcd, Integer.MIN_VALUE, Integer.MAX_VALUE
  };
  private static final long[] LONG_VALUES = {
      0L, 1L, -1L, 42L, -42L, 0x123456789abcdefL, -0x123456789abcdefL,
      Long.MIN_VALUE, Long.MAX_VALUE
  };

  @Test
  void intFormattingMatchesJdkAcrossSupportedRadices() {
    for (int radix : RADICES) {
      for (int value : INT_VALUES) {
        assertEquals(expectedInt(value, radix), Convert.toString(value, radix),
            "int value=" + value + ", radix=" + radix);
      }
    }
  }

  @Test
  void longFormattingMatchesJdkAcrossSupportedRadices() {
    for (int radix : RADICES) {
      for (long value : LONG_VALUES) {
        assertEquals(expectedLong(value, radix), Convert.toString(value, radix),
            "long value=" + value + ", radix=" + radix);
      }
    }
  }

  @Test
  void longDecimalOverloadUsesTheJavaSERadixImplementation() {
    for (long value : LONG_VALUES) {
      assertEquals(Long.toString(value), Convert.toString(value), "long value=" + value);
    }
  }

  private static String expectedInt(int value, int radix) {
    if (radix == 10 || value >= 0) return Integer.toString(value, radix);
    if (value == Integer.MIN_VALUE) return Integer.toUnsignedString(value, radix);
    return Integer.toString(-value, radix);
  }

  private static String expectedLong(long value, int radix) {
    if (radix == 10 || value >= 0) return Long.toString(value, radix);
    if (value == Long.MIN_VALUE) return Long.toUnsignedString(value, radix);
    return Long.toString(-value, radix);
  }
}
