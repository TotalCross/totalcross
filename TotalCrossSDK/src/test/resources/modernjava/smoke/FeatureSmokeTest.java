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

  protected void checkEquals(Object expected, Action action, String feature) {
    Object actual;
    try {
      actual = action.execute();

      if (expected == null ? actual != null : !expected.equals(actual)) {
        fail(feature, "expected <" + expected + "> but was <" + actual + ">");
      }
      pass(feature);
    } catch (NoSuchMethodError e) {
      fail(feature, e.getMessage());
    }
  }

  protected void checkEquals(Object expected, ActionString action, String actionArgument, String feature) {
    Object actual;
    try {
      actual = action.execute(actionArgument);

      if (expected == null ? actual != null : !expected.equals(actual)) {
        fail(feature, "expected <" + expected + "> but was <" + actual + ">");
      }
      pass(feature);
    } catch (NoSuchMethodError e) {
      fail(feature, e.getMessage());
    }
  }

  protected void finish() {
    String result = suiteName + " smoke OK (" + passed + " tests)";
    System.out.println("[PASS] " + result);
    add(new Label(result), LEFT + 8, TOP + 4);
  }

  protected void fail(String feature, Exception reason) {
    fail(feature, reason.getMessage());
    reason.printStackTrace();
  }

  private void fail(String feature, String reason) {
    String message = suiteName + " smoke failed: " + feature + " - " + reason;
    System.out.println("[FAIL] " + message);
  }

  @FunctionalInterface
  interface Action {
      Object execute() throws NoSuchMethodError;
  }

  @FunctionalInterface
  interface ActionString {
      Object execute(String value) throws NoSuchMethodError;
  }
}
