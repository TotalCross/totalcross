// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java4FeatureSmokeTest extends FeatureSmokeTest {
  public Java4FeatureSmokeTest() {
    super("Java 1.4");
  }

  @Override
  public void initUI() {
    testAssertStatement();
    finish();
  }

  private void testAssertStatement() {
    boolean value = true;
    assert value;
    check(value, "assert statement");
  }
}
