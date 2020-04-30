// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.unit;

import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;

/** This class represents a testcase. Just extend this class and implement the testRun method,
 * calling then the assert methods. The testcase must be added to a TestSuite class using the
 * addTestCase method. See the Litebase/alltests sample.
 */

public abstract class TestCase {
  // Called to run the test
  public abstract void testRun();

  protected static int assertionCounter;

  /** Set this to true when your code is <i>learning</i> the right answers. It will not halt
   * at each assertion failure, allowing you to know all the failures at once.
   * When the test ends, it will display "success" even if there were failures.
   */
  protected static boolean learning;

  protected void run() // guich@565_6
  {
    try {
      TestSuite.assertionFailed = false;
      assertionCounter = 0;
      testRun();
    } catch (AssertionFailedError e) // this error must be catched here, otherwise, the vm will issue an "Uncaugh Exception" and then halt.
    {
      TestSuite.assertionFailed = true;
      TestSuite.showException(e);
    }
  }

  /** Shows the text, in the output listbox, after all tests were finished. */
  protected static void output(String s) {
    TestSuite.output(s, TestSuite.OUTPUT_MSG);
  }

  /** Shows the text, in a limited (half screen width) region of the TestSuite screen. */
  protected static void status(String s) {
    TestSuite.status(s);
  }

  /** Updates the label with the available memory. */
  protected static void updateMem() {
    TestSuite.updateMem();
  }

  protected static void fail(Throwable e) {
    fail("\nOriginal exception: " + e.getClass().getName() + " - " + e.getMessage() + "\nOriginal stackTrace:\n"
        + Vm.getStackTrace(e));
  }

  protected static void fail(String msg) {
    if (learning) {
      msg = "LEARN #" + assertionCounter + ": " + msg;
      if (Settings.onJavaSE) {
        try {
          throw new AssertionFailedError(msg);
        } catch (AssertionFailedError afe) {
          afe.printStackTrace();
        }
      } else {
        Vm.debug(msg); // when learning, always dump to console.
      }
      output(msg);
    } else {
      throw new AssertionFailedError(msg);
    }
  }

  protected static void fail() {
    fail("Failed");
  }

  private static void assertFailed(String oper) {
    fail("assert " + oper + " failed");
  }

  // methods used to assert

  protected static void assertFalse(boolean condition) {
    assertionCounter++;
    boolean ok = !condition;
    if (!ok) {
      assertFailed("False");
    }
  }

  protected static void assertNotNull(java.lang.Object object) {
    assertionCounter++;
    boolean ok = object != null;
    if (!ok) {
      assertFailed("!null");
    }
  }

  protected static void assertNotSame(java.lang.Object expected, java.lang.Object actual) {
    assertionCounter++;
    boolean ok = (expected == null && actual != null) || (expected != null && actual == null)
        || (actual != null && !actual.equals(expected));
    if (!ok) {
      assertFailed(expected + " not same " + actual);
    }
  }

  protected static void assertSame(java.lang.Object expected, java.lang.Object actual) {
    assertionCounter++;
    boolean ok = (expected == null && actual == null) || (expected != null && expected.equals(actual));
    if (!ok) {
      assertFailed(expected + " same " + actual);
    }
  }

  protected static void assertNull(java.lang.Object object) {
    assertionCounter++;
    boolean ok = object == null;
    if (!ok) {
      assertFailed(object + " null");
    }
  }

  protected static void assertTrue(boolean actual) {
    assertionCounter++;
    boolean ok = actual;
    if (!ok) {
      assertFailed("true");
    }
  }

  protected static void assertEquals(int expected, int actual) {
    assertionCounter++;
    boolean ok = expected == actual;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(boolean expected, boolean actual) {
    assertionCounter++;
    boolean ok = expected == actual;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(java.lang.Object expected, java.lang.Object actual) {
    assertionCounter++;
    boolean ok = (expected == null && actual == null) || (expected != null && expected.equals(actual));
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(byte[] b1, byte[] b2) {
    assertNotNull(b1);
    assertNotNull(b2);
    if (b1.length != b2.length) {
      assertFailed("Array length mismatch: " + b1.length + " != " + b2.length);
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) {
        assertFailed(
            "Mismatch at " + i + ": " + Convert.unsigned2hex(b1[i], 2) + " != " + Convert.unsigned2hex(b2[i], 2));
      }
    }
  }

  protected static void assertEquals(int[] b1, int[] b2) {
    assertNotNull(b1);
    assertNotNull(b2);
    if (b1.length != b2.length) {
      assertFailed("Array length mismatch: " + b1.length + " != " + b2.length);
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) {
        assertFailed(
            "Mismatch at " + i + ": " + Convert.unsigned2hex(b1[i], 2) + " != " + Convert.unsigned2hex(b2[i], 2));
      }
    }
  }

  protected static void assertEquals(byte expected, byte actual) {
    assertionCounter++;
    boolean ok = expected == actual;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(char expected, char actual) {
    assertionCounter++;
    boolean ok = expected == actual;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(double expected, double actual, double delta) {
    assertionCounter++;
    double diff = Math.abs(expected - actual); // guich@552_33
    boolean ok = diff <= delta;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(long expected, long actual) {
    assertionCounter++;
    boolean ok = expected == actual;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertEquals(short expected, short actual) {
    assertionCounter++;
    boolean ok = expected == actual;
    if (!ok) {
      assertFailed(expected + " == " + actual);
    }
  }

  protected static void assertGreater(char left, char right) // guich@565_5: added Greater/GreaterOrEqual/Lower/LowerOrEqual
  {
    assertionCounter++;
    boolean ok = left > right;
    if (!ok) {
      assertFailed(left + " > " + right);
    }
  }

  protected static void assertGreater(byte left, byte right) {
    assertionCounter++;
    boolean ok = left > right;
    if (!ok) {
      assertFailed(left + " > " + right);
    }
  }

  protected static void assertGreater(short left, short right) {
    assertionCounter++;
    boolean ok = left > right;
    if (!ok) {
      assertFailed(left + " > " + right);
    }
  }

  protected static void assertGreater(int left, int right) {
    assertionCounter++;
    boolean ok = left > right;
    if (!ok) {
      assertFailed(left + " > " + right);
    }
  }

  protected static void assertGreater(long left, long right) {
    assertionCounter++;
    boolean ok = left > right;
    if (!ok) {
      assertFailed(left + " > " + right);
    }
  }

  protected static void assertGreater(double left, double right) {
    assertionCounter++;
    boolean ok = left > right;
    if (!ok) {
      assertFailed(left + " > " + right);
    }
  }

  protected static void assertGreaterOrEqual(char left, char right) {
    assertionCounter++;
    boolean ok = left >= right;
    if (!ok) {
      assertFailed(left + " >= " + right);
    }
  }

  protected static void assertGreaterOrEqual(byte left, byte right) {
    assertionCounter++;
    boolean ok = left >= right;
    if (!ok) {
      assertFailed(left + " >= " + right);
    }
  }

  protected static void assertGreaterOrEqual(short left, short right) {
    assertionCounter++;
    boolean ok = left >= right;
    if (!ok) {
      assertFailed(left + " >= " + right);
    }
  }

  protected static void assertGreaterOrEqual(int left, int right) {
    assertionCounter++;
    boolean ok = left >= right;
    if (!ok) {
      assertFailed(left + " >= " + right);
    }
  }

  protected static void assertGreaterOrEqual(long left, long right) {
    assertionCounter++;
    boolean ok = left >= right;
    if (!ok) {
      assertFailed(left + " >= " + right);
    }
  }

  protected static void assertGreaterOrEqual(double left, double right) {
    assertionCounter++;
    boolean ok = left >= right;
    if (!ok) {
      assertFailed(left + " >= " + right);
    }
  }

  protected static void assertLower(char left, char right) {
    assertionCounter++;
    boolean ok = left < right;
    if (!ok) {
      assertFailed(left + " < " + right);
    }
  }

  protected static void assertLower(byte left, byte right) {
    assertionCounter++;
    boolean ok = left < right;
    if (!ok) {
      assertFailed(left + " < " + right);
    }
  }

  protected static void assertLower(short left, short right) {
    assertionCounter++;
    boolean ok = left < right;
    if (!ok) {
      assertFailed(left + " < " + right);
    }
  }

  protected static void assertLower(int left, int right) {
    assertionCounter++;
    boolean ok = left < right;
    if (!ok) {
      assertFailed(left + " < " + right);
    }
  }

  protected static void assertLower(long left, long right) {
    assertionCounter++;
    boolean ok = left < right;
    if (!ok) {
      assertFailed(left + " < " + right);
    }
  }

  protected static void assertLower(double left, double right) {
    assertionCounter++;
    boolean ok = left < right;
    if (!ok) {
      assertFailed(left + " < " + right);
    }
  }

  protected static void assertLowerOrEqual(char left, char right) {
    assertionCounter++;
    boolean ok = left <= right;
    if (!ok) {
      assertFailed(left + " <= " + right);
    }
  }

  protected static void assertLowerOrEqual(byte left, byte right) {
    assertionCounter++;
    boolean ok = left <= right;
    if (!ok) {
      assertFailed(left + " <= " + right);
    }
  }

  protected static void assertLowerOrEqual(short left, short right) {
    assertionCounter++;
    boolean ok = left <= right;
    if (!ok) {
      assertFailed(left + " <= " + right);
    }
  }

  protected static void assertLowerOrEqual(int left, int right) {
    assertionCounter++;
    boolean ok = left <= right;
    if (!ok) {
      assertFailed(left + " <= " + right);
    }
  }

  protected static void assertLowerOrEqual(long left, long right) {
    assertionCounter++;
    boolean ok = left <= right;
    if (!ok) {
      assertFailed(left + " <= " + right);
    }
  }

  protected static void assertLowerOrEqual(double left, double right) {
    assertionCounter++;
    boolean ok = left <= right;
    if (!ok) {
      assertFailed(left + " <= " + right);
    }
  }

  protected static void assertBetween(char left, char mid, char right) // guich@566_6
  {
    assertionCounter++;
    boolean ok = left <= mid && mid <= right;
    if (!ok) {
      assertFailed(left + " <= " + mid + " <= " + right);
    }
  }

  protected static void assertBetween(byte left, byte mid, byte right) {
    assertionCounter++;
    boolean ok = left <= mid && mid <= right;
    if (!ok) {
      assertFailed(left + " <= " + mid + " <= " + right);
    }
  }

  protected static void assertBetween(short left, short mid, short right) {
    assertionCounter++;
    boolean ok = left <= mid && mid <= right;
    if (!ok) {
      assertFailed(left + " <= " + mid + " <= " + right);
    }
  }

  protected static void assertBetween(int left, int mid, int right) {
    assertionCounter++;
    boolean ok = left <= mid && mid <= right;
    if (!ok) {
      assertFailed(left + " <= " + mid + " <= " + right);
    }
  }

  protected static void assertBetween(long left, long mid, long right) {
    assertionCounter++;
    boolean ok = left <= mid && mid <= right;
    if (!ok) {
      assertFailed(left + " <= " + mid + " <= " + right);
    }
  }

  protected static void assertBetween(double left, double mid, double right) {
    assertionCounter++;
    boolean ok = left <= mid && mid <= right;
    if (!ok) {
      assertFailed(left + " <= " + mid + " <= " + right);
    }
  }

  protected static void assertNotEquals(Object expected, Object actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  protected static void assertNotEquals(int expected, int actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  protected static void assertNotEquals(boolean expected, boolean actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  protected static void assertNotEquals(byte expected, byte actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  //jeffque@tc200: byte array not equals comparison
  protected static void assertNotEquals(byte[] b1, byte[] b2) {
    assertionCounter++;
    if (b1 == null) {
      if (b2 == null) {
        assertFailed("");
      } else {
        return;
      }
    } else if (b2 == null) {
      return;
    }

    if (b1.length == b2.length) {
      for (int i = 0; i < b1.length; i++) {
        if (b1[i] != b2[i]) {
          return;
        }
      }
      assertFailed("Arrays are equal");
    }
  }

  protected static void assertNotEquals(char expected, char actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  protected static void assertNotEquals(double expected, double actual, double delta) {
    assertionCounter++;
    double diff = Math.abs(expected - actual); // guich@552_33
    boolean ok = diff > delta;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  protected static void assertNotEquals(long expected, long actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }

  protected static void assertNotEquals(short expected, short actual) {
    assertionCounter++;
    boolean ok = expected != actual;
    if (!ok) {
      assertFailed(expected + " != " + actual);
    }
  }
}
