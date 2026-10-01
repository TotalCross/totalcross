// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Integer4DTest {
  private static final int SAMPLE_COUNT = 4096;

  @Test
  void formatsCanonicalValues() {
    assertEquals("0", Integer4D.toHexString(0));
    assertEquals("1", Integer4D.toHexString(1));
    assertEquals("1234", Integer4D.toHexString(0x1234));
    assertEquals("12345678", Integer4D.toHexString(0x12345678));
    assertEquals("ffffff", Integer4D.toHexString(0x00FFFFFF));
    assertEquals("7fffffff", Integer4D.toHexString(Integer.MAX_VALUE));
    assertEquals("80000000", Integer4D.toHexString(Integer.MIN_VALUE));
    assertEquals("ffffffff", Integer4D.toHexString(-1));
  }

  @Test
  void matchesHostIntegerForBitPatterns() {
    for (int bit = 0; bit < 32; bit++) {
      assertMatchesHost(1 << bit);
    }
    for (int bits = 4; bits <= 28; bits += 4) {
      int boundary = 1 << bits;
      assertMatchesHost(boundary - 1);
      assertMatchesHost(boundary);
      assertMatchesHost(boundary + 1);
    }
    assertMatchesHost(0x80000001);
    assertMatchesHost(0xABCDEF01);
    assertMatchesHost(0xDEADBEEF);
    assertMatchesHost(0xF0F0F0F0);
    assertMatchesHost(0xFF00FF00);
    assertMatchesHost(~0x12345678);
    assertMatchesHost(Integer.MIN_VALUE + 1);
    assertMatchesHost(-2);

    int sample = 0x9E3779B9;
    for (int i = 0; i < SAMPLE_COUNT; i++) {
      assertMatchesHost(sample);
      sample = sample * 1664525 + 1013904223;
    }
  }

  private static void assertMatchesHost(int value) {
    String actual = Integer4D.toHexString(value);
    assertEquals(Integer.toHexString(value), actual);
    assertHexShape(value, actual);
  }

  private static void assertHexShape(int value, String actual) {
    assertTrue(actual.length() >= 1 && actual.length() <= 8);
    for (int i = 0; i < actual.length(); i++) {
      char ch = actual.charAt(i);
      assertTrue((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'));
    }
    if (actual.length() > 1) {
      assertFalse(actual.charAt(0) == '0');
    }
    if (value < 0) {
      assertEquals(8, actual.length());
      assertEquals(-1, actual.indexOf('-'));
    }
  }
}
