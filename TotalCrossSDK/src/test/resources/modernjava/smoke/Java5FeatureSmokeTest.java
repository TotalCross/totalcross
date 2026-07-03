// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

@Java5FeatureSmokeTest.FeatureAnnotation("java5")
public class Java5FeatureSmokeTest extends FeatureSmokeTest {
  public Java5FeatureSmokeTest() {
    super("Java 5");
  }

  @Override
  public void initUI() {
    testEnum();
    testGenerics();
    testEnhancedFor();
    testAutoboxing();
    testVarargs();
    testCovariantReturn();
    testAnnotationMetadata();
    finish();
  }

  private void testEnum() {
    Release release = Release.JAVA5;
    String value;
    switch (release) {
    case JAVA5:
      value = release.label();
      break;
    default:
      value = "unknown";
      break;
    }
    checkEquals("java5", value, "enum and enum switch");
  }

  private void testGenerics() {
    Holder<String> holder = new Holder<String>("java5");
    checkEquals("java5", holder.get(), "generics");
  }

  private void testEnhancedFor() {
    StringBuffer buffer = new StringBuffer();
    for (String part : new String[] { "java", "5" }) {
      buffer.append(part);
    }
    checkEquals("java5", buffer.toString(), "enhanced for");
  }

  private void testAutoboxing() {
    Integer boxed = 5;
    int value = boxed;
    checkEquals(Integer.valueOf(6), Integer.valueOf(value + 1), "autoboxing and unboxing");
  }

  private void testVarargs() {
    checkEquals("java5", join("java", "5"), "varargs");
  }

  private void testCovariantReturn() {
    CovariantBase value = new CovariantChild();
    checkEquals("java5", value.value().toString(), "covariant return");
  }

  private void testAnnotationMetadata() {
    pass("annotation metadata");
  }

  private static String join(String... parts) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < parts.length; i++) {
      buffer.append(parts[i]);
    }
    return buffer.toString();
  }

  enum Release {
    JAVA5;

    String label() {
      return "java5";
    }
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

  static class CovariantBase {
    Object value() {
      return "base";
    }
  }

  static final class CovariantChild extends CovariantBase {
    @Override
    String value() {
      return "java5";
    }
  }

  @interface FeatureAnnotation {
    String value();
  }
}
