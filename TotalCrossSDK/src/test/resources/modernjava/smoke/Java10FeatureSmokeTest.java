// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java10FeatureSmokeTest extends FeatureSmokeTest {
  public Java10FeatureSmokeTest() {
    super("Java 10");
  }

  @Override
  public void initUI() {
    testLocalVariableTypeInference();
    finish();
  }

  private void testLocalVariableTypeInference() {
    var holder = new Holder<String>("java");
    var version = 10;
    checkEquals("java10", holder.get() + version, "local variable type inference");
  }

  static final class Holder<T> {
    private final T value;

    Holder(T value) {
      this.value = value;
    }

    T get() {
      return value;
    }
  }
}
