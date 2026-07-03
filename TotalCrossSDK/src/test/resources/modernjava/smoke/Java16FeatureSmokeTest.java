// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java16FeatureSmokeTest extends FeatureSmokeTest {
  public Java16FeatureSmokeTest() {
    super("Java 16");
  }

  @Override
  public void initUI() {
    testRecordCarrier();
    testInstanceofPatternMatching();
    finish();
  }

  private void testRecordCarrier() {
    Release release = new Release("java", 16);
    checkEquals("java16", release.label(), "record carrier");
    checkEquals(Integer.valueOf(16), Integer.valueOf(release.version()), "record accessor");
  }

  private void testInstanceofPatternMatching() {
    Object value = "java16";
    if (value instanceof String text) {
      checkEquals("java16", text, "instanceof pattern matching");
      return;
    }
    throw new RuntimeException("Java 16 smoke failed: instanceof pattern matching");
  }

  record Release(String name, int version) {
    String label() {
      return name + version;
    }
  }
}
