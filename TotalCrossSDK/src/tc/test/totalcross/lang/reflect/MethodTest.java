package tc.test.totalcross.lang.reflect;

import totalcross.sys.*;
import totalcross.unit.*;

import java.lang.reflect.*;

// svn.apache.org/repos/asf/harmony/enhanced/java/trunk/classlib/modules/luni/src/test/api/common/org/apache/harmony/luni/tests/java/lang/reflect/MethodTest.java

public class MethodTest extends TestCase
{
   static class TestMethod
   {
      public TestMethod()
      {
      }

      public void voidMethod() throws IllegalArgumentException
      {
      }

      public void parmTest(int x, short y, String s, boolean bool, Object o, long l, byte b, char c, double d)
      {
      }

      public int intMethod()
      {
         return 1;
      }

      public static final void printTest(int x, short y, String s, boolean bool, Object o, long l, byte b, char c, double d)
      {
      }

      public double doubleMethod()
      {
         return 1.0;
      }

      public short shortMethod()
      {
         return (short) 1;
      }

      public byte byteMethod()
      {
         return (byte) 1;
      }

      public float floatMethod()
      {
         return 1.0f;
      }

      public long longMethod()
      {
         return 1l;
      }

      public char charMethod()
      {
         return 'T';
      }

      public Object objectMethod()
      {
         return new Object();
      }

      private static void prstatic()
      {
      }

      public static void pustatic()
      {
      }

      public static synchronized void pustatsynch()
      {
      }

      public static int invokeStaticTest()
      {
         return 1;
      }

      public int invokeInstanceTest()
      {
         return 1;
      }

      private int privateInvokeTest()
      {
         return 1;
      }

      public int invokeExceptionTest() throws NullPointerException
      {
         throw new NullPointerException();
      }

      public static synchronized native void pustatsynchnat();

      public void invokeCastTest1(byte param)
      {
      }

      public void invokeCastTest1(short param)
      {
      }

      public void invokeCastTest1(int param)
      {
      }

      public void invokeCastTest1(long param)
      {
      }

      public void invokeCastTest1(float param)
      {
      }

      public void invokeCastTest1(double param)
      {
      }

      public void invokeCastTest1(char param)
      {
      }

      public void invokeCastTest1(boolean param)
      {
      }
   }

   abstract class AbstractTestMethod
   {
      public abstract void puabs();
   }

   class TestMethodSub extends TestMethod
   {
      public int invokeInstanceTest()
      {
         return 0;
      }
   }

   /**
    * @tests java.lang.reflect.Method#equals(java.lang.Object)
    */
   public void test_equalsLjava_lang_Object() throws Exception
   {
      // Test for method boolean
      // java.lang.reflect.Method.equals(java.lang.Object)

      Method m1 = null, m2 = null;
      m1 = TestMethod.class.getMethod("invokeInstanceTest", new Class[0]);
      m2 = TestMethodSub.class.getMethod("invokeInstanceTest", new Class[0]);

      assertNotEquals(m1,m2);
      assertEquals(m1,m1);
      m1 = TestMethod.class.getMethod("invokeStaticTest", new Class[0]);
      m2 = TestMethodSub.class.getMethod("invokeStaticTest", new Class[0]);

      assertEquals(m1,m2);
   }

   /**
    * @tests java.lang.reflect.Method#getDeclaringClass()
    */
   public void test_getDeclaringClass()
   {
      // Test for method java.lang.Class
      // java.lang.reflect.Method.getDeclaringClass()

      Method[] mths;

      mths = TestMethod.class.getDeclaredMethods();
      assertEquals(mths[0].getDeclaringClass(),TestMethod.class);
   }

   /**
    * @tests java.lang.reflect.Method#getExceptionTypes()
    */
   public void test_getExceptionTypes() throws Exception
   {
      // Test for method java.lang.Class []
      // java.lang.reflect.Method.getExceptionTypes()

      Method mth = TestMethod.class.getMethod("voidMethod", new Class[0]);
      Class[] ex = mth.getExceptionTypes();
      assertEquals(1, ex.length);
      assertEquals(ex[0], IllegalArgumentException.class);
      mth = TestMethod.class.getMethod("intMethod", new Class[0]);
      ex = mth.getExceptionTypes();
      assertEquals(0, ex.length);
   }

   /**
    * @tests java.lang.reflect.Method#getModifiers()
    */
   public void test_getModifiers() throws Exception
   {
      // Test for method int java.lang.reflect.Method.getModifiers()

      Class cl = TestMethod.class;
      int mods = 0;
      Method mth = null;
      int mask = 0;
      mth = cl.getMethod("pustatic", new Class[0]);
      mods = mth.getModifiers();

      mask = Modifier.PUBLIC | Modifier.STATIC;
      assertEquals((mods | mask), mask);
      mth = cl.getDeclaredMethod("prstatic", new Class[0]);
      mods = mth.getModifiers();

      mask = Modifier.PRIVATE | Modifier.STATIC;
      assertEquals((mods | mask), mask);
      mth = cl.getDeclaredMethod("pustatsynch", new Class[0]);
      mods = mth.getModifiers();

      mask = (Modifier.PUBLIC | Modifier.STATIC) | Modifier.SYNCHRONIZED;
      assertEquals((mods | mask), mask);
      mth = cl.getDeclaredMethod("pustatsynchnat", new Class[0]);
      mods = mth.getModifiers();

      mask = ((Modifier.PUBLIC | Modifier.STATIC) | Modifier.SYNCHRONIZED) | Modifier.NATIVE;
      assertEquals((mods | mask), mask);
      cl = AbstractTestMethod.class;
      mth = cl.getDeclaredMethod("puabs", new Class[0]);
      mods = mth.getModifiers();

      mask = Modifier.PUBLIC | Modifier.ABSTRACT;
      assertEquals((mods | mask), mask);
   }

   /**
    * @tests java.lang.reflect.Method#getName()
    */
   public void test_getName() throws Exception
   {
      // Test for method java.lang.String java.lang.reflect.Method.getName()
      Method mth = null;
      mth = TestMethod.class.getMethod("voidMethod", new Class[0]);

      assertEquals("voidMethod", mth.getName());
   }

   /**
    * @tests java.lang.reflect.Method#getParameterTypes()
    */
   public void test_getParameterTypes() throws Exception
   {
      // Test for method java.lang.Class []
      // java.lang.reflect.Method.getParameterTypes()
      Class cl = TestMethod.class;
      Method mth = null;
      Class[] parms = null;
      Method[] methods = null;
      Class[] plist = { int.class, short.class, String.class, boolean.class, Object.class, long.class, byte.class, char.class, double.class };
      mth = cl.getMethod("voidMethod", new Class[0]);
      parms = mth.getParameterTypes();

      assertEquals(0, parms.length);
      mth = cl.getMethod("parmTest", plist);
      parms = mth.getParameterTypes();

      assertEquals(plist.length, parms.length);
      for (int i = 0; i < plist.length; i++)
         assertEquals(plist[i],parms[i]);

      // Test same method. but this time pull it from the list of methods
      // rather than asking for it explicitly
      methods = cl.getDeclaredMethods();

      int i;
      for (i = 0; i < methods.length; i++)
         if (methods[i].getName().equals("parmTest"))
         {
            mth = methods[i];
            i = methods.length + 1;
         }
      if (i < methods.length)
      {
         parms = mth.getParameterTypes();
         assertEquals(parms.length, plist.length);
         for (i = 0; i < plist.length; i++)
            assertEquals(plist[i], parms[i]);
      }
   }

   /**
    * @tests java.lang.reflect.Method#getReturnType()
    */
   public void test_getReturnType() throws Exception
   {
      // Test for method java.lang.Class
      // java.lang.reflect.Method.getReturnType()
      Class cl = TestMethod.class;
      Method mth = null;
      /*
       * mth = cl.getMethod("charMethod", new Class[0]); assertTrue("Gave incorrect returne type, wanted char", mth
       * .getReturnType().equals(char.class)); mth = cl.getMethod("longMethod", new Class[0]);
       * assertTrue("Gave incorrect returne type, wanted long", mth .getReturnType().equals(long.class)); mth =
       * cl.getMethod("shortMethod", new Class[0]); assertTrue("Gave incorrect returne type, wanted short", mth
       * .getReturnType().equals(short.class)); mth = cl.getMethod("intMethod", new Class[0]);
       * assertTrue("Gave incorrect returne type, wanted int: " + mth.getReturnType(),
       * mth.getReturnType().equals(int.class)); mth = cl.getMethod("doubleMethod", new Class[0]);
       * assertTrue("Gave incorrect returne type, wanted double", mth .getReturnType().equals(double.class)); mth =
       * cl.getMethod("byteMethod", new Class[0]); assertTrue("Gave incorrect returne type, wanted byte", mth
       * .getReturnType().equals(byte.class)); mth = cl.getMethod("byteMethod", new Class[0]);
       * assertTrue("Gave incorrect returne type, wanted byte", mth .getReturnType().equals(byte.class)); mth =
       * cl.getMethod("objectMethod", new Class[0]); assertTrue("Gave incorrect returne type, wanted Object", mth
       * .getReturnType().equals(Object.class));
       */
      mth = cl.getMethod("voidMethod", new Class[0]);

      assertEquals(mth.getReturnType(), void.class);
   }

   /**
    * @tests java.lang.reflect.Method#invoke(java.lang.Object, java.lang.Object[])
    */
   public void test_invokeLjava_lang_Object$Ljava_lang_Object() throws Exception
   {
      // Test for method java.lang.Object
      // java.lang.reflect.Method.invoke(java.lang.Object, java.lang.Object
      // [])

      Class cl = TestMethod.class;
      Method mth = null;
      Object ret = null;
      Class[] dcl = new Class[0];

      // Get and invoke a static method
      mth = cl.getDeclaredMethod("invokeStaticTest", dcl);
      ret = mth.invoke(null, new Object[0]);

      assertEquals(1, ((Integer) ret).intValue());

      // Get and invoke an instance method
      mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);
      ret = mth.invoke(new TestMethod(), new Object[0]);

      assertEquals(1, ((Integer) ret).intValue());

      // Get and attempt to invoke a private method
      mth = cl.getDeclaredMethod("privateInvokeTest", dcl);

      try
      {
         ret = mth.invoke(new TestMethod(), new Object[0]);
      }
      catch (IllegalAccessException e)
      {
         // Correct behaviour
      }

      // Generate an IllegalArgumentException
      mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);

      try
      {
         Object[] args = { Object.class };
         ret = mth.invoke(new TestMethod(), args);
      }
      catch (IllegalArgumentException e)
      {
         // Correct behaviour
      }

      // Generate a NullPointerException
      mth = cl.getDeclaredMethod("invokeInstanceTest", dcl);

      try
      {
         ret = mth.invoke(null, new Object[0]);
      }
      catch (NullPointerException e)
      {
         // Correct behaviour
      }

      // Generate an InvocationTargetException
      mth = cl.getDeclaredMethod("invokeExceptionTest", dcl);
      try
      {
         ret = mth.invoke(new TestMethod(), new Object[0]);
      }
      catch (InvocationTargetException e)
      {
         // Correct behaviour
      }
      catch (NullPointerException npe)
      {
         fail("should throw InvocationTargetException");
      }

      mth = String.class.getMethod("valueOf", new Class[] { Integer.TYPE });
      try
      {
         mth.invoke(String.class, new Object[] { null });
         fail("should throw IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         // Expected
      }

      TestMethod testMethod = new TestMethod();
      Method methods[] = cl.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].getName().startsWith("invokeCastTest1"))
         {
            Class param = methods[i].getParameterTypes()[0];

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Byte((byte) 1) });
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Integer.TYPE || param == Long.TYPE
                     || param == Float.TYPE || param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Boolean.TYPE || param == Character.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Short((short) 1) });
               assertTrue(param == Short.TYPE || param == Integer.TYPE || param == Long.TYPE || param == Float.TYPE
                     || param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Boolean.TYPE || param == Character.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Integer(1) });
               assertTrue(param == Integer.TYPE || param == Long.TYPE || param == Float.TYPE
                     || param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Boolean.TYPE
                     || param == Character.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Long(1) });
               assertTrue(param == Long.TYPE || param == Float.TYPE || param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Integer.TYPE || param == Boolean.TYPE
                     || param == Character.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Character('a') });
               assertTrue(param == Character.TYPE || param == Integer.TYPE || param == Long.TYPE
                     || param == Float.TYPE || param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Boolean.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Float(1) });
               assertTrue(param == Float.TYPE || param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Integer.TYPE || param == Long.TYPE
                     || param == Boolean.TYPE || param == Character.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Double(1) });
               assertTrue(param == Double.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Integer.TYPE || param == Long.TYPE
                     || param == Boolean.TYPE || param == Character.TYPE || param == Float.TYPE);
            }

            try
            {
               methods[i].invoke(testMethod, new Object[] { new Boolean(true) });
               assertTrue(param == Boolean.TYPE);
            }
            catch (Exception e)
            {
               assertTrue(e instanceof IllegalArgumentException);
               assertTrue(param == Byte.TYPE || param == Short.TYPE || param == Integer.TYPE || param == Long.TYPE
                     || param == Character.TYPE || param == Float.TYPE || param == Double.TYPE);
            }
         }
      }
   }

   public void test_invoke_InvocationTargetException() throws Exception
   {
      Method method = MockObject.class.getDeclaredMethod("mockMethod", new Class[] { Class.class });
      MockObject mockObject = new MockObject();

      try
      {
         method.invoke(mockObject, new Class[] { InvocationTargetException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         method.invoke(mockObject, new Class[] { IllegalAccessException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         method.invoke(mockObject, new Class[] { IllegalArgumentException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         method.invoke(mockObject, new Class[] { InvocationTargetException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         method.invoke(mockObject, new Class[] { Throwable.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }
   }

   static class MockObject
   {

      public void mockMethod(Class clazz) throws Exception
      {
         if (clazz == InstantiationException.class)
         {
            throw new InstantiationException();
         }
         else
            if (clazz == IllegalAccessException.class)
            {
               throw new IllegalAccessException();
            }
            else
               if (clazz == IllegalArgumentException.class)
               {
                  throw new IllegalArgumentException();
               }
               else
                  if (clazz == InvocationTargetException.class)
                  {
                     throw new InvocationTargetException(new Throwable());
                  }
                  else
                  {
                     throw new Exception();
                  }
      }
   }

   /**
    * @tests java.lang.reflect.Method#toString()
    */
   public void test_toString() throws Exception
   {
      Method mth = null;
      Class[] parms = { int.class, short.class, String.class, boolean.class, Object.class, long.class, byte.class, char.class, double.class };
      mth = TestMethod.class.getDeclaredMethod("printTest", parms);

      assertEquals(mth.toString(), "public static final void tc.test.totalcross.lang.reflect.MethodTest$TestMethod.printTest(int,short,java.lang.String,boolean,java.lang.Object,long,byte,char,double)");
   }

   public void testRun()
   {
      try
      {
         test_equalsLjava_lang_Object();
         test_getDeclaringClass();
         if (false)
            test_getExceptionTypes();
         test_getModifiers();
         test_getName();
         test_getParameterTypes();
         test_getReturnType();
         test_invokeLjava_lang_Object$Ljava_lang_Object();
         test_invoke_InvocationTargetException();
         test_toString();
      }
      catch (Throwable e)
      {
         String s = Vm.getStackTrace(e);
         throw new AssertionFailedError(getClass().getName()+" - "+e.getMessage()+" - "+s);
      }
   }
}
