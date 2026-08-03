// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package lambda.repro;

import totalcross.ui.MainWindow;

public class LambdaReturnDiscardRepro extends MainWindow {
  private static final int TOTAL_CASES = 11;
  private final int[] invocationCounts = new int[TOTAL_CASES];
  private int passed;
  private int failed;

  @Override
  public void initUI() {
    runCase("void", this::returnVoid, 0);
    runCase("boolean", this::returnBoolean, 1);
    runCase("byte", this::returnByte, 2);
    runCase("char", this::returnChar, 3);
    runCase("short", this::returnShort, 4);
    runCase("int", this::returnInt, 5);
    runCase("long", this::returnLong, 6);
    runCase("float", this::returnFloat, 7);
    runCase("double", this::returnDouble, 8);
    runCase("object", this::returnObject, 9);
    runCase("array", this::returnArray, 10);

    System.out.println("[SUMMARY] Lambda return discard - total=" + TOTAL_CASES + ", passed=" + passed
        + ", failed=" + failed);
    exit(failed == 0 ? 0 : 1);
  }

  private void runCase(String name, ReturnDiscardAction action, int index) {
    System.out.println("[CASE] Lambda return discard - " + name);
    try {
      action.execute();
      if (invocationCounts[index] != 1) {
        throw new IllegalStateException("expected one invocation, got " + invocationCounts[index]);
      }
      passed++;
      System.out.println("[PASS] Lambda return discard - " + name);
    } catch (Throwable failure) {
      failed++;
      System.out.println("[FAIL] Lambda return discard - " + name + " - " + failure.getClass().getName() + ": "
          + failure.getMessage());
      failure.printStackTrace();
    }
  }

  private void record(int index) {
    invocationCounts[index]++;
  }

  public void returnVoid() {
    record(0);
  }

  public boolean returnBoolean() {
    record(1);
    return true;
  }

  public byte returnByte() {
    record(2);
    return 7;
  }

  public char returnChar() {
    record(3);
    return 'c';
  }

  public short returnShort() {
    record(4);
    return 8;
  }

  public int returnInt() {
    record(5);
    return 9;
  }

  public long returnLong() {
    record(6);
    return 10L;
  }

  public float returnFloat() {
    record(7);
    return 1.5F;
  }

  public double returnDouble() {
    record(8);
    return 2.5D;
  }

  public String returnObject() {
    record(9);
    return "return-object";
  }

  public int[] returnArray() {
    record(10);
    return new int[] { 11 };
  }
}
