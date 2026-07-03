// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import totalcross.ui.Container;
import totalcross.ui.Label;

abstract class FeatureSmokeTest extends Container {
  private final String suiteName;
  private int passed;

  FeatureSmokeTest(String suiteName) {
    this.suiteName = suiteName;
  }

  protected void pass(String feature) {
    passed++;
    System.out.println("[PASS] " + suiteName + " - " + feature);
  }

  protected void check(boolean condition, String feature) {
    if (!condition) {
      fail(feature, "condition was false");
    }
    pass(feature);
  }

  protected void checkEquals(Object expected, Object actual, String feature) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      fail(feature, "expected <" + expected + "> but was <" + actual + ">");
    }
    pass(feature);
  }

  protected void finish() {
    String result = suiteName + " smoke OK (" + passed + " tests)";
    System.out.println("[PASS] " + result);
    add(new Label(result), LEFT + 8, TOP + 4);
  }

  private void fail(String feature, String reason) {
    String message = suiteName + " smoke failed: " + feature + " - " + reason;
    System.out.println("[FAIL] " + message);
    throw new RuntimeException(message);
  }
}
