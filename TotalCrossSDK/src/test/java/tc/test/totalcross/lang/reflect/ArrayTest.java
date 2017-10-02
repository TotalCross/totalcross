package tc.test.totalcross.lang.reflect;

import java.lang.reflect.Array;

import totalcross.unit.TestCase;

// svn.apache.org/repos/asf/harmony/enhanced/java/trunk/classlib/modules/luni/src/test/api/common/org/apache/harmony/luni/tests/java/lang/reflect/ArrayTest.java

public class ArrayTest extends TestCase {
  /**
   * @tests java.lang.reflect.Array#get(java.lang.Object, int)
   */
  private void test_getLjava_lang_ObjectI() {
    // Test for method java.lang.Object
    // java.lang.reflect.Array.get(java.lang.Object, int)

    int[] x = { 1 };
    Object ret = null;
    boolean thrown = false;
    ret = Array.get(x, 0);
    assertEquals(1, ((Integer) ret).intValue());
    try {
      ret = Array.get(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.get(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getBoolean(java.lang.Object, int)
   */
  private void test_getBooleanLjava_lang_ObjectI() {
    // Test for method boolean
    // java.lang.reflect.Array.getBoolean(java.lang.Object, int)
    boolean[] x = { true };
    boolean ret = false;
    boolean thrown = false;
    ret = Array.getBoolean(x, 0);

    assertTrue(ret);
    try {
      ret = Array.getBoolean(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getBoolean(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getByte(java.lang.Object, int)
   */
  private void test_getByteLjava_lang_ObjectI() {
    // Test for method byte
    // java.lang.reflect.Array.getByte(java.lang.Object, int)
    byte[] x = { 1 };
    byte ret = 0;
    boolean thrown = false;
    ret = Array.getByte(x, 0);

    assertEquals(1, ret);
    try {
      ret = Array.getByte(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getByte(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getChar(java.lang.Object, int)
   */
  private void test_getCharLjava_lang_ObjectI() {
    // Test for method char
    // java.lang.reflect.Array.getChar(java.lang.Object, int)
    char[] x = { 1 };
    char ret = 0;
    boolean thrown = false;
    ret = Array.getChar(x, 0);

    assertEquals(1, ret);
    try {
      ret = Array.getChar(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getChar(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getDouble(java.lang.Object, int)
   */
  private void test_getDoubleLjava_lang_ObjectI() {
    // Test for method double
    // java.lang.reflect.Array.getDouble(java.lang.Object, int)
    double[] x = { 1 };
    double ret = 0;
    boolean thrown = false;
    ret = Array.getDouble(x, 0);

    assertEquals(1d, ret, 0);
    try {
      ret = Array.getDouble(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }

    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getDouble(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getFloat(java.lang.Object, int)
   */
  private void test_getFloatLjava_lang_ObjectI() {
    // Test for method float
    // java.lang.reflect.Array.getFloat(java.lang.Object, int)
    float[] x = { 1 };
    float ret = 0;
    boolean thrown = false;
    ret = Array.getFloat(x, 0);

    assertEquals(1, ret, 0);
    try {
      ret = Array.getFloat(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getFloat(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getInt(java.lang.Object, int)
   */
  private void test_getIntLjava_lang_ObjectI() {
    // Test for method int java.lang.reflect.Array.getInt(java.lang.Object,
    // int)
    int[] x = { 1 };
    int ret = 0;
    boolean thrown = false;
    ret = Array.getInt(x, 0);

    assertEquals(1, ret);
    try {
      ret = Array.getInt(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getInt(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getLength(java.lang.Object)
   */
  private void test_getLengthLjava_lang_Object() {
    // Test for method int
    // java.lang.reflect.Array.getLength(java.lang.Object)
    long[] x = { 1 };

    assertEquals(1, Array.getLength(x));
    assertEquals(10000, Array.getLength(new Object[10000]));
    try {
      Array.getLength(new Object());
    } catch (IllegalArgumentException e) {
      // Correct
      return;
    }
    fail("Failed to throw exception when passed non-array");
  }

  /**
   * @tests java.lang.reflect.Array#getLong(java.lang.Object, int)
   */
  private void test_getLongLjava_lang_ObjectI() {
    // Test for method long
    // java.lang.reflect.Array.getLong(java.lang.Object, int)
    long[] x = { 1 };
    long ret = 0;
    boolean thrown = false;
    ret = Array.getLong(x, 0);

    assertEquals(1, ret);
    try {
      ret = Array.getLong(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getLong(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#getShort(java.lang.Object, int)
   */
  private void test_getShortLjava_lang_ObjectI() {
    // Test for method short
    // java.lang.reflect.Array.getShort(java.lang.Object, int)
    short[] x = { 1 };
    short ret = 0;
    boolean thrown = false;
    ret = Array.getShort(x, 0);

    assertEquals(1, ret);
    try {
      ret = Array.getShort(new Object(), 0);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      ret = Array.getShort(x, 4);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#newInstance(java.lang.Class, int[])
   */
  private void test_newInstanceLjava_lang_ClassII() {
    // Test for method java.lang.Object
    // java.lang.reflect.Array.newInstance(java.lang.Class, int [])
    int[][] x;
    int[] y = { 2 };

    x = (int[][]) Array.newInstance(int[].class, y);
    assertEquals(2, x.length);

  }

  /**
   * @tests java.lang.reflect.Array#newInstance(java.lang.Class, int)
   */
  private void test_newInstanceLjava_lang_ClassI() {
    // Test for method java.lang.Object
    // java.lang.reflect.Array.newInstance(java.lang.Class, int)
    int[] x;

    x = (int[]) Array.newInstance(int.class, 100);
    assertEquals(100, x.length);
  }

  /**
   * @tests java.lang.reflect.Array#set(java.lang.Object, int, java.lang.Object)
   */
  private void test_setLjava_lang_ObjectILjava_lang_Object() {
    // Test for method void java.lang.reflect.Array.set(java.lang.Object,
    // int, java.lang.Object)
    int[] x = { 0 };
    boolean thrown = false;
    Array.set(x, 0, new Integer(1));

    assertEquals(1, ((Integer) Array.get(x, 0)).intValue());
    try {
      Array.set(new Object(), 0, new Object());
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.set(x, 4, new Integer(1));
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }

    // trying to put null in a primitive array causes
    // a IllegalArgumentException in 5.0
    boolean exception = false;
    try {
      Array.set(new int[1], 0, null);
    } catch (IllegalArgumentException e) {
      exception = true;
    }
    assertTrue(exception);
  }

  /**
   * @tests java.lang.reflect.Array#setBoolean(java.lang.Object, int, boolean)
   */
  private void test_setBooleanLjava_lang_ObjectIZ() {
    // Test for method void
    // java.lang.reflect.Array.setBoolean(java.lang.Object, int, boolean)
    boolean[] x = { false };
    boolean thrown = false;
    Array.setBoolean(x, 0, true);

    assertTrue(Array.getBoolean(x, 0));
    try {
      Array.setBoolean(new Object(), 0, false);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setBoolean(x, 4, false);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setByte(java.lang.Object, int, byte)
   */
  private void test_setByteLjava_lang_ObjectIB() {
    // Test for method void
    // java.lang.reflect.Array.setByte(java.lang.Object, int, byte)
    byte[] x = { 0 };
    boolean thrown = false;
    Array.setByte(x, 0, (byte) 1);

    assertEquals(1, Array.getByte(x, 0));
    try {
      Array.setByte(new Object(), 0, (byte) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setByte(x, 4, (byte) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setChar(java.lang.Object, int, char)
   */
  private void test_setCharLjava_lang_ObjectIC() {
    // Test for method void
    // java.lang.reflect.Array.setChar(java.lang.Object, int, char)
    char[] x = { 0 };
    boolean thrown = false;
    Array.setChar(x, 0, (char) 1);

    assertEquals(1, Array.getChar(x, 0));
    try {
      Array.setChar(new Object(), 0, (char) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setChar(x, 4, (char) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setDouble(java.lang.Object, int, double)
   */
  private void test_setDoubleLjava_lang_ObjectID() {
    // Test for method void
    // java.lang.reflect.Array.setDouble(java.lang.Object, int, double)
    double[] x = { 0 };
    boolean thrown = false;
    Array.setDouble(x, 0, (double) 1);

    assertEquals(1, Array.getDouble(x, 0), 0);
    try {
      Array.setDouble(new Object(), 0, (double) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setDouble(x, 4, (double) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setFloat(java.lang.Object, int, float)
   */
  private void test_setFloatLjava_lang_ObjectIF() {
    // Test for method void
    // java.lang.reflect.Array.setFloat(java.lang.Object, int, float)
    float[] x = { 0.0f };
    boolean thrown = false;
    Array.setFloat(x, 0, (float) 1);

    assertEquals(1, Array.getFloat(x, 0), 0);
    try {
      Array.setFloat(new Object(), 0, (float) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setFloat(x, 4, (float) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setInt(java.lang.Object, int, int)
   */
  private void test_setIntLjava_lang_ObjectII() {
    // Test for method void java.lang.reflect.Array.setInt(java.lang.Object,
    // int, int)
    int[] x = { 0 };
    boolean thrown = false;
    Array.setInt(x, 0, (int) 1);

    assertEquals(1, Array.getInt(x, 0));
    try {
      Array.setInt(new Object(), 0, (int) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setInt(x, 4, (int) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setLong(java.lang.Object, int, long)
   */
  private void test_setLongLjava_lang_ObjectIJ() {
    // Test for method void
    // java.lang.reflect.Array.setLong(java.lang.Object, int, long)
    long[] x = { 0 };
    boolean thrown = false;
    Array.setLong(x, 0, (long) 1);

    assertEquals(1, Array.getLong(x, 0));
    try {
      Array.setLong(new Object(), 0, (long) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setLong(x, 4, (long) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  /**
   * @tests java.lang.reflect.Array#setShort(java.lang.Object, int, short)
   */
  private void test_setShortLjava_lang_ObjectIS() {
    // Test for method void
    // java.lang.reflect.Array.setShort(java.lang.Object, int, short)
    short[] x = { 0 };
    boolean thrown = false;
    Array.setShort(x, 0, (short) 1);

    assertEquals(1, Array.getShort(x, 0));
    try {
      Array.setShort(new Object(), 0, (short) 9);
    } catch (IllegalArgumentException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Passing non-array failed to throw exception");
    }
    thrown = false;
    try {
      Array.setShort(x, 4, (short) 9);
    } catch (ArrayIndexOutOfBoundsException e) {
      // Correct behaviour
      thrown = true;
    }
    if (!thrown) {
      fail("Invalid index failed to throw exception");
    }
  }

  @Override
  public void testRun() {
    test_getLjava_lang_ObjectI();
    test_getBooleanLjava_lang_ObjectI();
    test_getByteLjava_lang_ObjectI();
    test_getCharLjava_lang_ObjectI();
    test_getDoubleLjava_lang_ObjectI();
    test_getFloatLjava_lang_ObjectI();
    test_getIntLjava_lang_ObjectI();
    test_getLengthLjava_lang_Object();
    test_getLongLjava_lang_ObjectI();
    test_getShortLjava_lang_ObjectI();
    test_newInstanceLjava_lang_ClassI();
    test_newInstanceLjava_lang_ClassII();
    test_setLjava_lang_ObjectILjava_lang_Object();
    test_setBooleanLjava_lang_ObjectIZ();
    test_setByteLjava_lang_ObjectIB();
    test_setCharLjava_lang_ObjectIC();
    test_setDoubleLjava_lang_ObjectID();
    test_setFloatLjava_lang_ObjectIF();
    test_setIntLjava_lang_ObjectII();
    test_setLongLjava_lang_ObjectIJ();
    test_setShortLjava_lang_ObjectIS();
  }

}
