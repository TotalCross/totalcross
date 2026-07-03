// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java7FeatureSmokeTest extends FeatureSmokeTest {
  private final int javaVersion = 7;

  public Java7FeatureSmokeTest() {
    super("Java 7");
  }

  @Override
  public void initUI() {
    testDiamond();
    testStringSwitch();
    testTryWithResources();
    testMultiCatch();
    testBinaryLiteralsAndNumericSeparators();
    finish();
  }

  private void testDiamond() {
    Holder<String> holder = new Holder<>("java7");
    checkEquals("java7", holder.get(), "diamond operator");
  }

  private void testStringSwitch() {
    String value = "java";
    int version;
    switch (value) {
    case "java":
      version = javaVersion;
      break;
    default:
      version = 0;
      break;
    }
    checkEquals(Integer.valueOf(7), Integer.valueOf(version), "string switch");
  }

  private void testTryWithResources() {
    SmokeResource resource = new SmokeResource();
    try (SmokeResource closeable = resource) {
      closeable.touch();
    }
    check(resource.touched && resource.closed, "try-with-resources");
  }

  private void testMultiCatch() {
    try {
      throwChecked("state");
    } catch (IllegalArgumentException | IllegalStateException e) {
      checkEquals("state", e.getMessage(), "multi-catch");
      return;
    }
    throw new RuntimeException("Java 7 smoke failed: multi-catch");
  }

  private void testBinaryLiteralsAndNumericSeparators() {
    int value = 0b111 + 1_000;
    checkEquals(Integer.valueOf(1007), Integer.valueOf(value), "binary literals and numeric separators");
  }

  private static void throwChecked(String message) {
    throw new IllegalStateException(message);
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
