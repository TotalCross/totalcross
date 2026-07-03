// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java15FeatureSmokeTest extends FeatureSmokeTest {
  public Java15FeatureSmokeTest() {
    super("Java 15");
  }

  @Override
  public void initUI() {
    testTextBlock();
    finish();
  }

  private void testTextBlock() {
    String value = """
        java
        15
        """;
    checkEquals("java\n15\n", value, "text block");
  }
}
