// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package smoke;

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
