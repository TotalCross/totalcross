// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java14FeatureSmokeTest extends FeatureSmokeTest {
  public Java14FeatureSmokeTest() {
    super("Java 14");
  }

  @Override
  public void initUI() {
    testSwitchExpression();
    finish();
  }

  private void testSwitchExpression() {
    String release = "lts";
    int version = switch (release) {
    case "lts" -> 17;
    case "current" -> 14;
    default -> 0;
    };
    checkEquals(Integer.valueOf(17), Integer.valueOf(version), "switch expression");
  }
}
