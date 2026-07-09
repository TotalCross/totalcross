// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

import java.util.function.Predicate;

public class Java11FeatureSmokeTest extends FeatureSmokeTest {
  private int javaVersion = 11;

  public Java11FeatureSmokeTest() {
    super("Java 11");
  }

  @Override
  public void initUI() {
    testStringConcatFactory();
    testNestmatePrivateAccess();
    testLambdaVarParameters();
    testPredicateNot();
    testStringHelpers();
    finish();
  }

  private void testStringConcatFactory() {
    String value = "java" + javaVersion;
    checkEquals("java11", value, "StringConcatFactory string concat");
  }

  private void testNestmatePrivateAccess() {
    SecretReader reader = new SecretReader();
    checkEquals("java11", reader.read(), "nestmate private access");
  }

  private void testLambdaVarParameters() {
    Mapper mapper = (var value) -> value + javaVersion;
    checkEquals("java11", mapper.map("java"), "lambda var parameters");
  }

  private void testPredicateNot() {
    Predicate<String> blank = String::isBlank;
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(Predicate.not(blank).test("java11")), "Predicate.not");
  }

  private void testStringHelpers() {
    checkEquals(Boolean.TRUE, () -> Boolean.valueOf(" \t\n".isBlank()), "String.isBlank");
    checkEquals("java11", "  java11 \t".strip(), "String.strip");
    checkEquals("java11 ", "\t java11 ".stripLeading(), "String.stripLeading");
    checkEquals(" java11", " java11 \n".stripTrailing(), "String.stripTrailing");
    checkEquals("javajavajava", "java".repeat(3), "String.repeat");
  }

  private String secret() {
    return "java" + javaVersion;
  }

  interface Mapper {
    String map(String value);
  }

  final class SecretReader {
    String read() {
      return secret();
    }
  }

}
