// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import java.util.function.Function;
import java.util.function.Supplier;

import totalcross.ui.Label;
import totalcross.ui.MainWindow;

public class Java8FeatureSmokeApp extends MainWindow {
  private int counter;

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

    add(new Label("Java 8 smoke OK"), LEFT + 8, TOP + 8);
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
    Supplier<String> supplier = Java8FeatureSmokeApp::staticText;
    checkEquals("text", supplier.get(), "static method reference");
  }

  private void testBoundMethodReference() {
    Supplier<String> supplier = this::instanceText;
    checkEquals("instance", supplier.get(), "bound method reference");
  }

  private void testUnboundMethodReference() {
    TextReader reader = Java8FeatureSmokeApp::instanceText;
    checkEquals("instance", reader.read(this), "unbound method reference");
  }

  private void testConstructorReference() {
    BoxFactory factory = Box::new;
    checkEquals("box", factory.create("box").value, "constructor reference");
  }

  private void testAltMetafactoryMarker() {
    Supplier<String> supplier = (Supplier<String> & Marker) Java8FeatureSmokeApp::staticText;
    check(supplier instanceof Marker, "altMetafactory marker");
    checkEquals("text", supplier.get(), "altMetafactory marker value");
  }

  private void testAltMetafactoryBridge() {
    StringFactory factory = (StringFactory & ObjectFactory) Java8FeatureSmokeApp::staticText;
    checkEquals("text", factory.get(), "altMetafactory bridge string value");
    checkEquals("text", ((ObjectFactory) factory).get(), "altMetafactory bridge object value");
  }

  private void testReferenceReturnAdaptation() {
    ObjectFactory factory = Java8FeatureSmokeApp::staticText;
    checkEquals("text", factory.get(), "reference return adaptation");
  }

  private void testReferenceArgumentAdaptation() {
    Mapper<String, String> mapper = Java8FeatureSmokeApp::trim;
    checkEquals("java8", mapper.map(" java8 "), "reference argument adaptation");
  }

  private void testPrimitiveAdaptation() {
    Function<String, Integer> length = String::length;
    checkEquals(Integer.valueOf(5), length.apply("java8"), "primitive return boxing");

    Function<Integer, Integer> twice = Java8FeatureSmokeApp::twice;
    checkEquals(Integer.valueOf(16), twice.apply(Integer.valueOf(8)), "primitive argument unboxing");
  }

  private void testDefaultAndStaticInterfaceMethods() {
    DefaultGreeting greeting = new DefaultGreetingImpl();
    checkEquals("default", greeting.defaultText(), "default interface method");
    checkEquals("static", DefaultGreeting.staticText(), "static interface method");
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

  private static void check(boolean condition, String feature) {
    if (!condition) {
      throw new RuntimeException("Java 8 smoke failed: " + feature);
    }
  }

  private static void checkEquals(Object expected, Object actual, String feature) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new RuntimeException("Java 8 smoke failed: " + feature);
    }
  }

  interface TextReader {
    String read(Java8FeatureSmokeApp source);
  }

  interface BoxFactory {
    Java8FeatureSmokeApp.Box create(String value);
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
}
