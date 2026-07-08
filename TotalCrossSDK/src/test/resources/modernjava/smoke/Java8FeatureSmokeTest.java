// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import java.util.function.BiPredicate;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class Java8FeatureSmokeTest extends FeatureSmokeTest {
  private int counter;

  public Java8FeatureSmokeTest() {
    super("Java 8");
  }

  @Override
  public void initUI() {
    testStatelessLambda();
    testCapturedLambda();
    testStaticMethodReference();
    testBoundMethodReference();
    testUnboundMethodReference();
    testConstructorReference();
    testAltMetafactoryMarker();
    testAltMetafactoryBridge();
    testReferenceReturnAdaptation();
    testReferenceArgumentAdaptation();
    testPrimitiveAdaptation();
    testDefaultAndStaticInterfaceMethods();
    testPredicateDefaults();
    testTypeAnnotationMetadata();
    testRepeatableAnnotationMetadata();
    finish();
  }

  private void testStatelessLambda() {
    Runnable runnable = () -> increment();
    runnable.run();
    check(counter > 0, "stateless lambda");
  }

  private void testCapturedLambda() {
    final String value = "captured";
    Supplier<String> supplier = () -> value + "-lambda";
    checkEquals("captured-lambda", supplier.get(), "captured lambda");
  }

  private void testStaticMethodReference() {
    Supplier<String> supplier = Java8FeatureSmokeTest::staticText;
    checkEquals("text", supplier.get(), "static method reference");
  }

  private void testBoundMethodReference() {
    Supplier<String> supplier = this::instanceText;
    checkEquals("instance", supplier.get(), "bound method reference");
  }

  private void testUnboundMethodReference() {
    TextReader reader = Java8FeatureSmokeTest::instanceText;
    checkEquals("instance", reader.read(this), "unbound method reference");
  }

  private void testConstructorReference() {
    BoxFactory factory = Box::new;
    checkEquals("box", factory.create("box").value, "constructor reference");
  }

  private void testAltMetafactoryMarker() {
    Supplier<String> supplier = (Supplier<String> & Marker) Java8FeatureSmokeTest::staticText;
    check(supplier instanceof Marker, "altMetafactory marker");
    checkEquals("text", supplier.get(), "altMetafactory marker value");
  }

  private void testAltMetafactoryBridge() {
    StringFactory factory = (StringFactory & ObjectFactory) Java8FeatureSmokeTest::staticText;
    checkEquals("text", factory.get(), "altMetafactory bridge string value");
    checkEquals("text", ((ObjectFactory) factory).get(), "altMetafactory bridge object value");
  }

  private void testReferenceReturnAdaptation() {
    ObjectFactory factory = Java8FeatureSmokeTest::staticText;
    checkEquals("text", factory.get(), "reference return adaptation");
  }

  private void testReferenceArgumentAdaptation() {
    Mapper<String, String> mapper = Java8FeatureSmokeTest::trim;
    checkEquals("java8", mapper.map(" java8 "), "reference argument adaptation");
  }

  private void testPrimitiveAdaptation() {
    Function<String, Integer> length = String::length;
    checkEquals(Integer.valueOf(5), length.apply("java8"), "primitive return boxing");

    Function<Integer, Integer> twice = Java8FeatureSmokeTest::twice;
    checkEquals(Integer.valueOf(16), twice.apply(Integer.valueOf(8)), "primitive argument unboxing");
  }

  private void testDefaultAndStaticInterfaceMethods() {
    DefaultGreeting greeting = new DefaultGreetingImpl();
    checkEquals("default", greeting::defaultText, "default interface method");
    checkEquals("static", DefaultGreeting::staticText, "static interface method");
  }

  private void testPredicateDefaults() {
    Predicate<String> startsWithJava = value -> value.startsWith("java");
    Predicate<String> endsWithEight = value -> value.endsWith("8");
    Predicate<String> composed = startsWithJava.and(endsWithEight);
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(composed.test("java8")), "predicate default and");
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(startsWithJava.or(endsWithEight).test("totalcross8")),
        "predicate default or");
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(startsWithJava.negate().test("totalcross")),
        "predicate default negate");
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(Predicate.isEqual("java8").test("java8")),
        "predicate static isEqual");

    BiPredicate<String, String> sameLength = (left, right) -> left.length() == right.length();
    BiPredicate<String, String> sameStart = (left, right) -> left.charAt(0) == right.charAt(0);
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(sameLength.and(sameStart).test("java", "jvm!")),
        "biPredicate default and");

    IntPredicate positive = value -> value > 0;
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(positive.and(value -> value < 10).test(8)),
        "intPredicate default and");

    LongPredicate large = value -> value > 100L;
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(large.or(value -> value == 8L).test(8L)),
        "longPredicate default or");

    DoublePredicate whole = value -> value == (long) value;
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(whole.negate().test(8.5D)), "doublePredicate default negate");
  }

  private void testTypeAnnotationMetadata() {
    String value = (@NonEmpty String) "java8";
    checkEquals("java8", value, "type annotation metadata");
  }

  @SmokeTags({ @SmokeTag("lambda"), @SmokeTag("default-method") })
  private void testRepeatableAnnotationMetadata() {
    pass("repeatable annotation metadata");
  }

  private void increment() {
    counter++;
  }

  private String instanceText() {
    return "instance";
  }

  private static String staticText() {
    return "text";
  }

  private static String trim(String value) {
    return value.trim();
  }

  private static int twice(int value) {
    return value * 2;
  }

  interface TextReader {
    String read(Java8FeatureSmokeTest source);
  }

  interface BoxFactory {
    Java8FeatureSmokeTest.Box create(String value);
  }

  interface StringFactory {
    String get();
  }

  interface ObjectFactory {
    Object get();
  }

  interface Marker {
  }

  interface Mapper<T, R> {
    R map(T value);
  }

  interface DefaultGreeting {
    default String defaultText() {
      return "default";
    }

    static String staticText() {
      return "static";
    }
  }

  static final class DefaultGreetingImpl implements DefaultGreeting {
  }

  static final class Box {
    final String value;

    Box(String value) {
      this.value = value;
    }
  }

  @Target(ElementType.TYPE_USE)
  @interface NonEmpty {
  }

  @interface SmokeTags {
    SmokeTag[] value();
  }

  @java.lang.annotation.Repeatable(SmokeTags.class)
  @interface SmokeTag {
    String value();
  }
}
