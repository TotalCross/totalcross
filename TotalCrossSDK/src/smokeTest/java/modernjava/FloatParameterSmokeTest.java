// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package modernjava;

public class FloatParameterSmokeTest extends FeatureSmokeTest {
  public FloatParameterSmokeTest() {
    super("float parameters");
  }

  @Override
  public void initUI() {
    Object marker = new Object();
    checkEquals(Integer.valueOf(17), Integer.valueOf(staticInt(1.25F, 17)), "static int after float");
    checkEquals(Integer.valueOf(29), Integer.valueOf(staticMiddle(3, 2.5F, 29)), "static float in middle");
    check(staticObject(3.5F, marker) == marker, "static reference after float");
    checkEquals(Long.valueOf(41L), Long.valueOf(staticLong(4.5F, 41L)), "static long after float");
    checkEquals(Double.valueOf(53.5D), Double.valueOf(staticDouble(5.5F, 53.5D)),
        "static double after float");
    checkEquals(Integer.valueOf(67), Integer.valueOf(instanceInt(6.5F, 67)), "instance int after float");
    check(instanceObject(7.5F, marker) == marker, "instance reference after float");
    checkEquals(Long.valueOf(79L), Long.valueOf(instanceLong(8.5F, 79L)), "instance long after float");
    checkEquals(Double.valueOf(83.5D), Double.valueOf(instanceDouble(9.5F, 83.5D)),
        "instance double after float");
    finish();
  }

  static int staticInt(float ignored, int value) {
    return value;
  }

  static int staticMiddle(int left, float ignored, int right) {
    return right;
  }

  static Object staticObject(float ignored, Object value) {
    return value;
  }

  static long staticLong(float ignored, long value) {
    return value;
  }

  static double staticDouble(float ignored, double value) {
    return value;
  }

  int instanceInt(float ignored, int value) {
    return value;
  }

  Object instanceObject(float ignored, Object value) {
    return value;
  }

  long instanceLong(float ignored, long value) {
    return value;
  }

  double instanceDouble(float ignored, double value) {
    return value;
  }
}
