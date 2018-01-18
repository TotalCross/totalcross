package tc.test.totalcross.lang.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import totalcross.unit.AssertionFailedError;
import totalcross.unit.TestCase;

// svn.apache.org/repos/asf/harmony/enhanced/java/trunk/classlib/modules/luni/src/test/api/common/org/apache/harmony/luni/tests/java/lang/reflect/ClassTest.java

@SuppressWarnings("rawtypes")
public class ClassTest extends TestCase {
  public ClassTest() {
  }

  public ClassTest(String s) {
  }

  public static final String FILENAME = "ClassTest/test#.properties";

  static class StaticMember$Class {
    class Member2$A {
    }
  }

  class Member$Class {
    class Member3$B {
    }
  }

  @SuppressWarnings("unused")
  public static class TestClass {
    private int privField = 1;

    public int pubField = 2;

    private Object cValue = null;

    public Object ack = new Object();

    private int privMethod() {
      return 1;
    }

    public int pubMethod() {
      return 2;
    }

    public Object cValue() {
      return cValue;
    }

    public TestClass() {
    }

    private TestClass(Object o) {
    }
  }

  public static class SubTestClass extends TestClass {
  }

  /**
   * @tests java.lang.Class#forName(java.lang.String)
   */
  public void test_forNameLjava_lang_String() throws Exception {
    assertEquals(Object.class, Class.forName("java.lang.Object"));
    assertEquals(Object[][].class, Class.forName("[[Ljava.lang.Object;"));

    assertEquals(int[].class, Class.forName("[I"));

    try {
      Class.forName("int");
      fail("");
    } catch (ClassNotFoundException e) {
    }

    try {
      Class.forName("byte");
      fail("");
    } catch (ClassNotFoundException e) {
    }
    try {
      Class.forName("char");
      fail("");
    } catch (ClassNotFoundException e) {
    }

    try {
      Class.forName("void");
      fail("");
    } catch (ClassNotFoundException e) {
    }

    try {
      Class.forName("short");
      fail("");
    } catch (ClassNotFoundException e) {
    }
    try {
      Class.forName("long");
      fail("");
    } catch (ClassNotFoundException e) {
    }

    try {
      Class.forName("boolean");
      fail("");
    } catch (ClassNotFoundException e) {
    }
    try {
      Class.forName("float");
      fail("");
    } catch (ClassNotFoundException e) {
    }
    try {
      Class.forName("double");
      fail("");
    } catch (ClassNotFoundException e) {
    }

    // regression test for JIRA 2162
    try {
      Class.forName("%");
      fail("should throw ClassNotFoundException.");
    } catch (ClassNotFoundException e) {
    }
  }

  /**
   * @tests java.lang.Class#getComponentType()
   */
  public void test_getComponentType() {
    assertEquals(int.class, int[].class.getComponentType());
    assertEquals(Object.class, Object[].class.getComponentType());
    assertNull(Object.class.getComponentType());
  }

  /**
   * @tests java.lang.Class#getConstructor(java.lang.Class[])
   */
  public void test_getConstructor$Ljava_lang_Class() throws NoSuchMethodException {
    TestClass.class.getConstructor(new Class[0]);
    try {
      TestClass.class.getConstructor(new Class[] { Object.class });
      fail("Found private constructor");
    } catch (NoSuchMethodException e) {
      // Correct - constructor with obj is private
    }
  }

  /**
   * @tests java.lang.Class#getConstructors()
   */
  public void test_getConstructors() throws Exception {
    Constructor[] c = TestClass.class.getConstructors();
    assertEquals(1, c.length);
  }

  /**
   * @tests java.lang.Class#getDeclaredConstructor(java.lang.Class[])
   */
  public void test_getDeclaredConstructor$Ljava_lang_Class() throws Exception {
    Constructor c = TestClass.class.getDeclaredConstructor(new Class[0]);
    assertNull(((TestClass) c.newInstance((Object) new Class[0])).cValue());
    c = TestClass.class.getDeclaredConstructor(new Class[] { Object.class });
  }

  /**
   * @tests java.lang.Class#getDeclaredConstructors()
   */
  public void test_getDeclaredConstructors() throws Exception {
    Constructor[] c = TestClass.class.getDeclaredConstructors();
    assertEquals(2, c.length);
  }

  /**
   * @tests java.lang.Class#getDeclaredField(java.lang.String)
   */
  public void test_getDeclaredFieldLjava_lang_String() throws Exception {
    Field f = TestClass.class.getDeclaredField("pubField");
    assertEquals(2, f.getInt(new TestClass()));
  }

  /**
   * @tests java.lang.Class#getDeclaredFields()
   */
  public void test_getDeclaredFields() throws Exception {
    Field[] f = TestClass.class.getDeclaredFields();
    assertEquals(4, f.length);
    f = SubTestClass.class.getDeclaredFields();
    // Declared fields do not include inherited
    assertEquals(0, f.length);
  }

  /**
   * @tests java.lang.Class#getDeclaredMethod(java.lang.String, java.lang.Class[])
   */
  public void test_getDeclaredMethodLjava_lang_String$Ljava_lang_Class() throws Exception {
    Method m = TestClass.class.getDeclaredMethod("pubMethod", new Class[0]);
    assertEquals(2, ((Integer) (m.invoke((Object) new TestClass(), (Object) new Class[0]))).intValue());
    m = TestClass.class.getDeclaredMethod("privMethod", new Class[0]);
  }

  /**
   * @tests java.lang.Class#getDeclaredMethods()
   */
  public void test_getDeclaredMethods() throws Exception {
    Method[] m = TestClass.class.getDeclaredMethods();
    assertEquals(3, m.length);
    m = SubTestClass.class.getDeclaredMethods();
    assertEquals(0, m.length);
  }

  /**
   * @tests java.lang.Class#getField(java.lang.String)
   */
  public void test_getFieldLjava_lang_String() throws Exception {
    Field f = TestClass.class.getField("pubField");
    assertEquals(2, f.getInt(new TestClass()));
    try {
      f = TestClass.class.getField("privField");
      fail("Private field access failed to throw exception");
    } catch (NoSuchFieldException e) {
      // Correct
    }
  }

  /**
   * @tests java.lang.Class#getFields()
   */
  public void test_getFields() throws Exception {
    Field[] f = TestClass.class.getFields();
    assertEquals(2, f.length);
    f = SubTestClass.class.getFields();
    // Check inheritance of pub fields
    assertEquals(2, f.length);
  }

  interface I1 {
  }

  interface I2 {
  }

  class TestInterf implements I1, I2 {
  }

  /**
   * @tests java.lang.Class#getInterfaces()
   */
  public void test_getInterfaces() {
    Class[] interfaces;
    interfaces = Object.class.getInterfaces();
    assertEquals(0, interfaces.length);
    assertEquals(2, TestInterf.class.getInterfaces().length);
  }

  /**
   * @tests java.lang.Class#getMethod(java.lang.String, java.lang.Class[])
   */
  public void test_getMethodLjava_lang_String$Ljava_lang_Class() throws Exception {
    Method m = TestClass.class.getMethod("pubMethod", new Class[0]);
    assertEquals(2, ((Integer) (m.invoke((Object) new TestClass(), (Object) new Class[0]))).intValue());
    try {
      m = TestClass.class.getMethod("privMethod", new Class[0]);
      fail("Failed to throw exception accessing private method");
    } catch (NoSuchMethodException e) {
      // Correct
      return;
    }
  }

  /**
   * @tests java.lang.Class#getMethods()
   */
  public void test_getMethods() throws Exception {
    Method[] m = TestClass.class.getMethods();
    assertEquals(2 + Object.class.getMethods().length, m.length);
    m = SubTestClass.class.getMethods();
    assertEquals(2 + Object.class.getMethods().length, m.length);
    // Number of inherited methods
  }

  private static final class PrivateClass {
  }

  /**
   * @tests java.lang.Class#getModifiers()
   */
  public void test_getModifiers() {
    int dcm = PrivateClass.class.getModifiers();
    assertFalse(Modifier.isPublic(dcm));
    assertFalse(Modifier.isProtected(dcm));
    // assertTrue("default class is not private", Modifier.isPrivate(dcm));

    int ocm = Object.class.getModifiers();
    assertTrue(Modifier.isPublic(ocm));
    assertFalse(Modifier.isProtected(ocm));
    assertFalse(Modifier.isPrivate(ocm));
  }

  /**
   * @tests java.lang.Class#getName()
   */
  public void test_getName() throws Exception {
    String className = Class.forName("java.lang.Object").getName();
    assertNotNull(className);

    assertEquals("java.lang.Object", className);
    assertEquals("int", int.class.getName());
    className = Class.forName("[I").getName();
    assertNotNull(className);
    assertEquals("[I", className);

    className = Class.forName("[Ljava.lang.Object;").getName();
    assertNotNull(className);

    // assertEquals("Class getName printed wrong value", "[Ljava.lang.Object;", className);
  }

  /**
   * @tests java.lang.Class#getSuperclass()
   */
  public void test_getSuperclass() {
    assertNull(Object.class.getSuperclass());
    assertEquals(Object.class, totalcross.io.File[].class.getSuperclass());
    assertNull(int.class.getSuperclass());
  }

  /**
   * @tests java.lang.Class#isArray()
   */
  public void test_isArray() throws ClassNotFoundException {
    assertTrue(!int.class.isArray());
    Class clazz = null;
    clazz = Class.forName("[I");
    assertTrue(clazz.isArray());

    clazz = Class.forName("[Ljava.lang.Object;");
    assertTrue(clazz.isArray());

    clazz = Class.forName("java.lang.Object");
    assertTrue(!clazz.isArray());
  }

  /**
   * @tests java.lang.Class#isAssignableFrom(java.lang.Class)
   */
  public void test_isAssignableFromLjava_lang_Class() {
    Class<?> clazz1 = null;
    Class<?> clazz2 = null;

    clazz1 = Object.class;
    clazz2 = Class.class;
    assertTrue(clazz1.isAssignableFrom(clazz2));

    clazz1 = TestClass.class;
    assertTrue(clazz1.isAssignableFrom(clazz1));

    clazz1 = Runnable.class;
    clazz2 = Thread.class;
    assertTrue(clazz1.isAssignableFrom(clazz2));
  }

  /**
   * @tests java.lang.Class#isInterface()
   */
  public void test_isInterface() throws ClassNotFoundException {
    assertTrue(!int.class.isInterface());
    Class clazz = null;
    clazz = Class.forName("[I");
    assertTrue(!clazz.isInterface());

    clazz = Class.forName("java.lang.Runnable");
    assertTrue(clazz.isInterface());
    clazz = Class.forName("java.lang.Object");
    assertTrue(!clazz.isInterface());

    clazz = Class.forName("[Ljava.lang.Object;");
    assertTrue(!clazz.isInterface());
  }

  /**
   * @tests java.lang.Class#isPrimitive()
   */
  public void test_isPrimitive() {
    assertFalse(Runnable.class.isPrimitive());
    assertFalse(Object.class.isPrimitive());
    assertFalse(int[].class.isPrimitive());
    assertFalse(Object[].class.isPrimitive());
    assertTrue(int.class.isPrimitive());
    assertFalse(Object.class.isPrimitive());
  }

  /**
   * @tests java.lang.Class#newInstance()
   */
  public void test_newInstance() throws Exception {
    Class clazz = null;
    clazz = Class.forName("java.lang.Object");
    assertNotNull(clazz.newInstance());

    clazz = Class.forName("java.lang.Throwable");
    assertEquals(clazz, clazz.newInstance().getClass());

    clazz = Class.forName("java.lang.Integer");
    try {
      clazz.newInstance();
      fail("Exception for instantiating a newInstance with no default constructor is not thrown");
    } catch (InstantiationException e) {
      // expected
    }
  }

  /**
   * @tests java.lang.Class#toString()
   */
  public void test_toString() throws ClassNotFoundException {
    assertEquals("int", int.class.toString());
    Class clazz = null;
    clazz = Class.forName("[I");
    assertEquals("class [I", clazz.toString());

    clazz = Class.forName("java.lang.Object");
    assertEquals("class java.lang.Object", clazz.toString());

    clazz = Class.forName("[Ljava.lang.Object;");
    // assertEquals("Class toString printed wrong value","class [Ljava.lang.Object;", clazz.toString());
  }

  /*
   * Regression test for HARMONY-2644: Load system and non-system array classes via Class.forName()
   */
  public void test_forName_arrays() throws Exception {
    Class<? extends ClassTest> c1 = getClass();
    String s = c1.getName();
    Class a1 = Class.forName("[L" + s + ";");
    Class a2 = Class.forName("[[L" + s + ";");
    assertEquals(c1, a1.getComponentType());
    assertEquals(a1, a2.getComponentType());
    Class l4 = Class.forName("[[[[[J");
    assertEquals(long[][][][][].class, l4);

    try {
      output("" + Class.forName("[;"));
      fail("1");
    } catch (ClassNotFoundException ok) {
    }
    try {
      output("" + Class.forName("[["));
      fail("2");
    } catch (ClassNotFoundException ok) {
    }
    try {
      output("" + Class.forName("[L"));
      fail("3");
    } catch (ClassNotFoundException ok) {
    }
    try {
      output("" + Class.forName("[L;"));
      fail("4");
    } catch (ClassNotFoundException ok) {
    }
    try {
      output("" + Class.forName(";"));
      fail("5");
    } catch (ClassNotFoundException ok) {
    }
    try {
      output("" + Class.forName(""));
      fail("6");
    } catch (ClassNotFoundException ok) {
    }
  }

  @Override
  public void testRun() {
    try {
      test_forNameLjava_lang_String();
      test_getComponentType();
      test_getConstructor$Ljava_lang_Class();
      test_getConstructors();
      test_getDeclaredConstructor$Ljava_lang_Class();
      test_getDeclaredConstructors();
      test_getDeclaredFieldLjava_lang_String();
      test_getDeclaredFields();
      test_getDeclaredMethodLjava_lang_String$Ljava_lang_Class();
      test_getDeclaredMethods();
      test_getFieldLjava_lang_String();
      test_getFields();
      test_getInterfaces();
      test_getMethodLjava_lang_String$Ljava_lang_Class();
      test_getMethods();
      test_getModifiers();
      test_getName();
      test_getSuperclass();
      test_isArray();
      test_isAssignableFromLjava_lang_Class();
      test_isInterface();
      test_isPrimitive();
      test_newInstance();
      test_toString();
      test_forName_arrays();
    } catch (Throwable e) {
      throw new AssertionFailedError(getClass().getName() + " - " + e.getMessage());
    }
  }
}
