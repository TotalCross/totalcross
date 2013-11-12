package tc.test.totalcross.lang.reflect;

import totalcross.sys.*;
import totalcross.unit.*;
import totalcross.util.*;

import java.lang.reflect.*;

// svn.apache.org/repos/asf/harmony/enhanced/java/trunk/classlib/modules/luni/src/test/api/common/org/apache/harmony/luni/tests/java/lang/reflect/ConstructorTest.java

public class ConstructorTest extends TestCase
{
   static class ConstructorTestHelper extends Object
   {
      int cval;

      public ConstructorTestHelper() throws IndexOutOfBoundsException
      {
         cval = 99;
      }

      public ConstructorTestHelper(Object x)
      {
      }

      private ConstructorTestHelper(int a)
      {
      }

      protected ConstructorTestHelper(long a)
      {
      }

      public int check()
      {
         return cval;
      }
   }

   /**
    * @tests java.lang.reflect.Constructor#equals(java.lang.Object)
    */
   public void test_equalsLjava_lang_Object() throws Exception
   {
      Constructor ctor1 = null, ctor2 = null;
      ctor1 = new ConstructorTestHelper().getClass().getConstructor(new Class[0]);

      Class[] parms = null;
      parms = new Class[1];
      parms[0] = new Object().getClass();
      ctor2 = new ConstructorTestHelper().getClass().getConstructor(parms);

      assertNotEquals(ctor1,ctor2);
   }

   /**
    * @tests java.lang.reflect.Constructor#getDeclaringClass()
    */
   public void test_getDeclaringClass() throws Exception
   {
      // Test for method java.lang.Class
      // java.lang.reflect.Constructor.getDeclaringClass()
      Class pclass = new ConstructorTestHelper().getClass();
      Constructor ctor = pclass.getConstructor(new Class[0]);
      assertEquals(ctor.getDeclaringClass(),pclass);
   }

   /**
    * @tests java.lang.reflect.Constructor#getExceptionTypes()
    */
   public void test_getExceptionTypes() throws Exception
   {
      Class[] exceptions = null;
      Class ex = null;
      Constructor ctor = new ConstructorTestHelper().getClass().getConstructor(new Class[0]);
      exceptions = ctor.getExceptionTypes();
      ex = new IndexOutOfBoundsException().getClass();

      assertEquals(1, exceptions.length);
      assertTrue(exceptions[0].equals(ex));
   }

   /**
    * @tests java.lang.reflect.Constructor#getModifiers()
    */
   public void test_getModifiers()
   {
      int mod = 0;
      try
      {
         Constructor ctor = new ConstructorTestHelper().getClass().getConstructor(new Class[0]);
         mod = ctor.getModifiers();
         assertTrue(((mod & Modifier.PUBLIC) == Modifier.PUBLIC) && ((mod & Modifier.PRIVATE) == 0));
      }
      catch (NoSuchMethodException e)
      {
         fail("Exception during test : " + e.getMessage());
      }
      try
      {
         Class[] cl = { int.class };
         Constructor ctor = new ConstructorTestHelper().getClass().getDeclaredConstructor(cl);
         mod = ctor.getModifiers();
         assertTrue(((mod & Modifier.PRIVATE) == Modifier.PRIVATE) && ((mod & Modifier.PUBLIC) == 0));
      }
      catch (NoSuchMethodException e)
      {
         fail("Exception during test : " + e.getMessage());
      }
      try
      {
         Class[] cl = { long.class };
         Constructor ctor = new ConstructorTestHelper().getClass().getDeclaredConstructor(cl);
         mod = ctor.getModifiers();
         assertTrue(((mod & Modifier.PROTECTED) == Modifier.PROTECTED) && ((mod & Modifier.PUBLIC) == 0));
      }
      catch (NoSuchMethodException e)
      {
         fail("NoSuchMethodException during test : " + e.getMessage());
      }
   }

   /**
    * @tests java.lang.reflect.Constructor#getName()
    */
   public void test_getName() throws Exception
   {
      Constructor ctor = new ConstructorTestHelper().getClass().getConstructor(new Class[0]);
      assertEquals(ctor.getName(),"tc.test.totalcross.lang.reflect.ConstructorTest$ConstructorTestHelper");
   }

   /**
    * @tests java.lang.reflect.Constructor#getParameterTypes()
    */
   public void test_getParameterTypes() throws Exception
   {
      Class[] types = null;
      Constructor ctor = new ConstructorTestHelper().getClass().getConstructor(new Class[0]);
      types = ctor.getParameterTypes();

      assertEquals(0, types.length);

      Class[] parms = null;
      parms = new Class[1];
      parms[0] = new Object().getClass();
      ctor = new ConstructorTestHelper().getClass().getConstructor(parms);
      types = ctor.getParameterTypes();

      assertEquals(types[0],parms[0]);
   }

   /**
    * @tests java.lang.reflect.Constructor#newInstance(java.lang.Object[])
    */
   public void test_newInstance$Ljava_lang_Object() throws Exception
   {
      ConstructorTestHelper test = null;
      Constructor ctor = new ConstructorTestHelper().getClass().getConstructor(new Class[0]);
      test = (ConstructorTestHelper) ctor.newInstance((Object[]) null);

      assertEquals(99, test.check());
   }

   /**
    * @tests java.lang.reflect.Constructor#newInstance(java.lang.Object[])
    */
   public void test_newInstance_IAE() throws Exception
   {
      Constructor constructor = Vector.class.getConstructor(new Class[] { Integer.TYPE });

      try
      {
         constructor.newInstance(new Object[] { null });
         fail("should throw IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         // Expected
      }
   }

   public void test_newInstance_InvocationTargetException() throws Exception
   {
      Constructor constructor = MockObject.class.getConstructor(new Class[] { Class.class });

      try
      {
         constructor.newInstance(new Class[] { InvocationTargetException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         constructor.newInstance(new Class[] { IllegalAccessException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         constructor.newInstance(new Class[] { IllegalArgumentException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         constructor.newInstance(new Class[] { InvocationTargetException.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }

      try
      {
         constructor.newInstance(new Class[] { Throwable.class });
         fail("should throw InvocationTargetException");
      }
      catch (InvocationTargetException e)
      {
         // Expected
      }
   }

   static class MockObject
   {

      public MockObject(Class clazz) throws Exception
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
    * @tests java.lang.reflect.Constructor#toString()
    */
   public void test_toString() throws Exception
   {
      Class[] parms = null;
      Constructor ctor = null;
      parms = new Class[1];
      parms[0] = new Object().getClass();
      ctor = new ConstructorTestHelper().getClass().getConstructor(parms);

      assertEquals(ctor.toString(), "public tc.test.totalcross.lang.reflect.ConstructorTest$ConstructorTestHelper(java.lang.Object)");
   }

   public void testRun()
   {
      try
      {
/*         test_equalsLjava_lang_Object();
         test_getDeclaringClass();
         if (false) test_getExceptionTypes();
         test_getModifiers();
         test_getName();
         test_getParameterTypes();*/
         //test_newInstance$Ljava_lang_Object();
         test_newInstance_IAE();
         test_newInstance_InvocationTargetException();
         test_toString();
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         String s = Vm.getStackTrace(e);
         throw new AssertionFailedError(getClass().getName()+" - "+e.getMessage()+" - "+s);
      }
   }
}
