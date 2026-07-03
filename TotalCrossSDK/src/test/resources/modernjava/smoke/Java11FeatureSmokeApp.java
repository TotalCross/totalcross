// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import totalcross.ui.Label;
import totalcross.ui.MainWindow;

public class Java11FeatureSmokeApp extends MainWindow {
  private int javaVersion = 11;

  @Override
  public void initUI() {
    testStringConcatFactory();
    testNestmatePrivateAccess();
    testLocalVariableTypeInference();
    testLambdaVarParameters();

    add(new Label("Java 11 smoke OK"), LEFT + 8, TOP + 8);
  }

  private void testStringConcatFactory() {
    String value = "java" + javaVersion;
    checkEquals("java11", value, "StringConcatFactory string concat");
  }

  private void testNestmatePrivateAccess() {
    SecretReader reader = new SecretReader();
    checkEquals("java11", reader.read(), "nestmate private access");
  }

  private void testLocalVariableTypeInference() {
    var holder = new Holder<String>("java");
    checkEquals("java11", holder.value() + javaVersion, "local variable type inference");
  }

  private void testLambdaVarParameters() {
    Mapper mapper = (var value) -> value + javaVersion;
    checkEquals("java11", mapper.map("java"), "lambda var parameters");
  }

  private String secret() {
    return "java" + javaVersion;
  }

  private static void checkEquals(Object expected, Object actual, String feature) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new RuntimeException("Java 11 smoke failed: " + feature);
    }
  }

  interface Mapper {
    String map(String value);
  }

  final class SecretReader {
    String read() {
      return secret();
    }
  }

  static final class Holder<T> {
    private final T value;

    Holder(T value) {
      this.value = value;
    }

    T value() {
      return value;
    }
  }
}
