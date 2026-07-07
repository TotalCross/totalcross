// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

public class Java9FeatureSmokeTest extends FeatureSmokeTest {
  private int javaVersion = 9;

  public Java9FeatureSmokeTest() {
    super("Java 9");
  }

  @Override
  public void initUI() {
    testStringConcatFactory();
    testPrivateInterfaceMethods();
    testPrivateStaticInterfaceMethods();
    testDiamondAnonymousClass();
    testTryWithResourcesEffectivelyFinal();
    testPrivateSafeVarargs();
    finish();
  }

  private void testStringConcatFactory() {
    String value = "java" + javaVersion;
    checkEquals("java9", value, "StringConcatFactory string concat");
  }

  private void testPrivateInterfaceMethods() {
    Greeter greeter = new DefaultGreeter();
    checkEquals("hello java9", greeter::greet, "java9", "private interface method");
  }

  private void testPrivateStaticInterfaceMethods() {
    checkEquals("static java9", Greeter::staticGreet, "java9", "private static interface method");
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

  interface Greeter {
    default String greet(String value) {
      return prefix() + value;
    }

    static String staticGreet(String value) {
      return staticPrefix() + value;
    }

    private String prefix() {
      return "hello ";
    }

    private static String staticPrefix() {
      return "static ";
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
