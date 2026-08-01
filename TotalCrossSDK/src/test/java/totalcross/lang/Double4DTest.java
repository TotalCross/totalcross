// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.lang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Double4DTest {
  @Test
  void recognizesFiniteValues() {
    assertTrue(Double4D.isFinite(0));
    assertTrue(Double4D.isFinite(-Double4D.MIN_VALUE));
    assertTrue(Double4D.isFinite(Double4D.MAX_VALUE));
    assertFalse(Double4D.isFinite(Double4D.NaN));
    assertFalse(Double4D.isFinite(Double4D.POSITIVE_INFINITY));
    assertFalse(Double4D.isFinite(Double4D.NEGATIVE_INFINITY));
  }
}
