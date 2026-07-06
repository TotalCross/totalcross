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
    checkEquals("Release[name=java, version=16]", release.toString(), "record toString");
    check(new Release("java", 16).equals(release), "record equals");
    check(!new Release("java", 17).equals(release), "record equals mismatch");
    checkEquals(Integer.valueOf(new Release("java", 16).hashCode()), Integer.valueOf(release.hashCode()),
        "record hashCode");
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
