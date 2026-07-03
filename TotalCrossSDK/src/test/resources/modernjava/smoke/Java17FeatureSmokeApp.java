// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import totalcross.ui.Label;
import totalcross.ui.MainWindow;

public class Java17FeatureSmokeApp extends MainWindow {
  @Override
  public void initUI() {
    testRecordCarrier();
    testInstanceofPatternMatching();
    testSwitchExpression();
    testTextBlock();
    testSealedClassMetadata();

    add(new Label("Java 17 smoke OK"), LEFT + 8, TOP + 8);
  }

  private void testRecordCarrier() {
    Release release = new Release("java", 17);
    checkEquals("java17", release.label(), "record carrier");
    checkEquals(Integer.valueOf(17), Integer.valueOf(release.version()), "record accessor");
  }

  private void testInstanceofPatternMatching() {
    Object value = "java17";
    if (value instanceof String text) {
      checkEquals("java17", text, "instanceof pattern matching");
      return;
    }
    throw new RuntimeException("Java 17 smoke failed: instanceof pattern matching");
  }

  private void testSwitchExpression() {
    String release = "lts";
    int version = switch (release) {
    case "lts" -> 17;
    case "current" -> 26;
    default -> 0;
    };
    checkEquals(Integer.valueOf(17), Integer.valueOf(version), "switch expression");
  }

  private void testTextBlock() {
    String value = """
        java
        17
        """;
    checkEquals("java\n17\n", value, "text block");
  }

  private void testSealedClassMetadata() {
    JavaRelease release = new LtsRelease(17);
    checkEquals(Integer.valueOf(17), Integer.valueOf(release.version()), "sealed class metadata");
  }

  private static void checkEquals(Object expected, Object actual, String feature) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new RuntimeException("Java 17 smoke failed: " + feature);
    }
  }

  record Release(String name, int version) {
    String label() {
      return name + version;
    }
  }

  sealed interface JavaRelease permits LtsRelease {
    int version();
  }

  static final class LtsRelease implements JavaRelease {
    private final int version;

    LtsRelease(int version) {
      this.version = version;
    }

    @Override
    public int version() {
      return version;
    }
  }
}
