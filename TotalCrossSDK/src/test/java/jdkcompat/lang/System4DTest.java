// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang;

import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class System4DTest {
  @Test
  void createsIndependentOutputAndErrorStreams() {
    assertNotSame(System4D.out, System4D.err);
  }
}
