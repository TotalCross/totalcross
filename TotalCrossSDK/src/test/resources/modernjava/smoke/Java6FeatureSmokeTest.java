// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java6FeatureSmokeTest extends FeatureSmokeTest {
  public Java6FeatureSmokeTest() {
    super("Java 6");
  }

  @Override
  public void initUI() {
    testInterfaceOverrideAnnotation();
    finish();
  }

  private void testInterfaceOverrideAnnotation() {
    checkEquals("java6", new InterfaceImplementation().name(), "@Override on interface method");
  }

  interface Named {
    String name();
  }

  static final class InterfaceImplementation implements Named {
    @Override
    public String name() {
      return "java6";
    }
  }
}
