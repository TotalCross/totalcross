// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import totalcross.ui.Label;
import totalcross.ui.MainWindow;

public class Java9FeatureSmokeApp extends MainWindow {
  private int javaVersion = 9;

  @Override
  public void initUI() {
    testStringConcatFactory();
    testPrivateInterfaceMethods();
    testDiamondAnonymousClass();
    testTryWithResourcesEffectivelyFinal();
    testPrivateSafeVarargs();

    add(new Label("Java 9 smoke OK"), LEFT + 8, TOP + 8);
  }

  private void testStringConcatFactory() {
    String value = "java" + javaVersion;
    checkEquals("java9", value, "StringConcatFactory string concat");
  }

  private void testPrivateInterfaceMethods() {
    Greeter greeter = new DefaultGreeter();
    checkEquals("hello java9", greeter.greet("java9"), "private interface method");
  }

  private void testDiamondAnonymousClass() {
    Holder<String> holder = new Holder<>("java") {
      @Override
      String value() {
        return super.value() + "9";
      }
    };
    checkEquals("java9", holder.value(), "diamond anonymous class");
  }

  private void testTryWithResourcesEffectivelyFinal() {
    SmokeResource resource = new SmokeResource();
    try (resource) {
      resource.touch();
    }
    check(resource.touched && resource.closed, "try-with-resources effectively-final variable");
  }

  @SafeVarargs
  private String joinPieces(String... parts) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < parts.length; i++) {
      buffer.append(parts[i]);
    }
    return buffer.toString();
  }

  private void testPrivateSafeVarargs() {
    checkEquals("java9", joinPieces("java", "9"), "@SafeVarargs private method");
  }

  private static void check(boolean condition, String feature) {
    if (!condition) {
      throw new RuntimeException("Java 9 smoke failed: " + feature);
    }
  }

  private static void checkEquals(Object expected, Object actual, String feature) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new RuntimeException("Java 9 smoke failed: " + feature);
    }
  }

  interface Greeter {
    default String greet(String value) {
      return prefix() + value;
    }

    private String prefix() {
      return "hello ";
    }
  }

  static final class DefaultGreeter implements Greeter {
  }

  static class Holder<T> {
    private final T value;

    Holder(T value) {
      this.value = value;
    }

    String value() {
      return String.valueOf(value);
    }
  }

  static final class SmokeResource implements AutoCloseable {
    boolean closed;
    boolean touched;

    void touch() {
      touched = true;
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
