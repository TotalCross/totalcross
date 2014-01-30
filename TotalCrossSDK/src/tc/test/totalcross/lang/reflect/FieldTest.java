package tc.test.totalcross.lang.reflect;

import tc.test.totalcross.lang.*;

import totalcross.sys.*;
import totalcross.unit.*;

import java.lang.reflect.*;

// svn.apache.org/repos/asf/harmony/enhanced/java/trunk/classlib/modules/luni/src/test/api/common/org/apache/harmony/luni/tests/java/lang/reflect/FieldTest.java

public class FieldTest extends TestCase
{
   static class TestField
   {
      public static int pubfield1;
      protected static double doubleSField = Double.MAX_VALUE;
      private static int privfield1 = 123;
      protected int intField = Integer.MAX_VALUE;
      protected short shortField = Short.MAX_VALUE;
      protected boolean booleanField = true;
      protected byte byteField = Byte.MAX_VALUE;
      protected long longField = Long.MAX_VALUE;
      protected double doubleField = Double.MAX_VALUE;
      protected float floatField = Float.MAX_VALUE;
      protected char charField = 'T';
      protected final int intFField = Integer.MAX_VALUE;
      protected final short shortFField = Short.MAX_VALUE;
      protected final boolean booleanFField = true;
      protected final byte byteFField = Byte.MAX_VALUE;
      protected final long longFField = Long.MAX_VALUE;
      protected final double doubleFField = Double.MAX_VALUE;
      protected final float floatFField = Float.MAX_VALUE;
      protected final char charFField = 'T';
      private static final int x = 1;
      public volatile transient int y = 0;
      protected static transient volatile int prsttrvol = 99;
   }

   public class TestFieldSub1 extends TestField
   {
   }

   public class TestFieldSub2 extends TestField
   {
   }

   static class A
   {
      protected short shortField = Short.MAX_VALUE;
   }

   /**
    * @tests java.lang.reflect.Field#equals(java.lang.Object)
    */
   public void test_equalsLjava_lang_Object() throws Exception
   {
      // Test for method boolean
      // java.lang.reflect.Field.equals(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      f = x.getClass().getDeclaredField("shortField");

      assertTrue(f.equals(f));
      assertTrue(f.equals(x.getClass().getDeclaredField("shortField")));
      assertTrue(!f.equals(A.class.getDeclaredField("shortField")));
   }

   /**
    * @tests java.lang.reflect.Field#get(java.lang.Object)
    */
   public void test_getLjava_lang_Object() throws Throwable
   {
      // Test for method java.lang.Object
      // java.lang.reflect.Field.get(java.lang.Object)
      TestField x = new TestField();
      Field f = x.getClass().getDeclaredField("doubleField");
      Double val = (Double) f.get(x);

      assertTrue(val.doubleValue() == Double.MAX_VALUE);
      // Test getting a static field;
      f = x.getClass().getDeclaredField("doubleSField");
      f.set(x, new Double(1.0));
      val = (Double) f.get(x);
      assertEquals(1.0, val.doubleValue(),0);

      // Try a get on a private field
      try
      {
         f = TestAccess.class.getDeclaredField("xxx");
         assertNotNull(f);
         f.get(null);
         fail("No expected IllegalAccessException");
      }
      catch (IllegalAccessException ok)
      {
      }

      // Try a get on a private field in nested member
      // temporarily commented because it breaks J9 VM
      // Regression for HARMONY-1309
      try
      {
         f = x.getClass().getDeclaredField("privfield1");
         assertEquals(x.privfield1, ((Integer) f.get(x)).intValue());
         fail("could get field access!");
      }
      catch (IllegalAccessException ok)
      {
      }

      // Try a get using an invalid class.
      try
      {
         f = x.getClass().getDeclaredField("doubleField");
         f.get(new String());
         fail("No expected IllegalArgumentException");
      }
      catch (IllegalArgumentException exc)
      {
         // Correct - Passed an Object that does not declare or inherit f
      }
   }

   class SupportSubClass extends Support_Field
   {

      Object getField(char primitiveType, Object o, Field f, Class expectedException)
      {
         Object res = null;
         try
         {
            primitiveType = Convert.toUpperCase(primitiveType);
            switch (primitiveType)
            {
               case 'I': // int
                  res = new Integer(f.getInt(o));
                  break;
               case 'J': // long
                  res = new Long(f.getLong(o));
                  break;
               case 'Z': // boolean
                  res = new Boolean(f.getBoolean(o));
                  break;
               case 'S': // short
                  res = new Short(f.getShort(o));
                  break;
               case 'B': // byte
                  res = new Byte(f.getByte(o));
                  break;
               case 'C': // char
                  res = new Character(f.getChar(o));
                  break;
               case 'D': // double
                  res = new Double(f.getDouble(o));
                  break;
               case 'F': // float
                  res = new Float(f.getFloat(o));
                  break;
               default:
                  res = f.get(o);
            }
            if (expectedException != null)
            {
               fail("expected exception " + expectedException.getName());
            }
         }
         catch (Exception e)
         {
            if (expectedException == null)
            {
               fail("unexpected exception " + e);
            }
            else
            {
               test(e.getClass(), expectedException);
            }
         }
         return res;
      }

      void setField(char primitiveType, Object o, Field f, Class expectedException, Object value)
      {
         try
         {
            primitiveType = Convert.toUpperCase(primitiveType);
            switch (primitiveType)
            {
               case 'I': // int
                  f.setInt(o, ((Integer) value).intValue());
                  break;
               case 'J': // long
                  f.setLong(o, ((Long) value).longValue());
                  break;
               case 'Z': // boolean
                  f.setBoolean(o, ((Boolean) value).booleanValue());
                  break;
               case 'S': // short
                  f.setShort(o, ((Short) value).shortValue());
                  break;
               case 'B': // byte
                  f.setByte(o, ((Byte) value).byteValue());
                  break;
               case 'C': // char
                  f.setChar(o, ((Character) value).charValue());
                  break;
               case 'D': // double
                  f.setDouble(o, ((Double) value).doubleValue());
                  break;
               case 'F': // float
                  f.setFloat(o, ((Float) value).floatValue());
                  break;
               default:
                  f.set(o, value);
            }
            if (expectedException != null)
            {
               fail("expected exception " + expectedException.getName());
            }
         }
         catch (Exception e)
         {
            if (expectedException == null)
            {
               fail("unexpected exception " + e);
            }
            else
            {
               test(e.getClass(),expectedException);
            }
         }
      }
   }
   
   private void test(Object i, Object j)
   {
      assertEquals(i,j);
   }

   /**
    * @tests java.lang.reflect.Field#get(java.lang.Object)
    * @tests java.lang.reflect.Field#getByte(java.lang.Object)
    * @tests java.lang.reflect.Field#getBoolean(java.lang.Object)
    * @tests java.lang.reflect.Field#getShort(java.lang.Object)
    * @tests java.lang.reflect.Field#getInt(java.lang.Object)
    * @tests java.lang.reflect.Field#getLong(java.lang.Object)
    * @tests java.lang.reflect.Field#getFloat(java.lang.Object)
    * @tests java.lang.reflect.Field#getDouble(java.lang.Object)
    * @tests java.lang.reflect.Field#getChar(java.lang.Object)
    * @tests java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
    * @tests java.lang.reflect.Field#setByte(java.lang.Object, byte)
    * @tests java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
    * @tests java.lang.reflect.Field#setShort(java.lang.Object, short)
    * @tests java.lang.reflect.Field#setInt(java.lang.Object, int)
    * @tests java.lang.reflect.Field#setLong(java.lang.Object, long)
    * @tests java.lang.reflect.Field#setFloat(java.lang.Object, float)
    * @tests java.lang.reflect.Field#setDouble(java.lang.Object, double)
    * @tests java.lang.reflect.Field#setChar(java.lang.Object, char)
    */
   public void testProtectedFieldAccess()
   {
      Class fieldClass = new Support_Field().getClass();
      String fieldName = null;
      Field objectField = null;
      Field booleanField = null;
      Field byteField = null;
      Field charField = null;
      Field shortField = null;
      Field intField = null;
      Field longField = null;
      Field floatField = null;
      Field doubleField = null;
      try
      {
         fieldName = "objectField";
         objectField = fieldClass.getDeclaredField(fieldName);

         fieldName = "booleanField";
         booleanField = fieldClass.getDeclaredField(fieldName);

         fieldName = "byteField";
         byteField = fieldClass.getDeclaredField(fieldName);

         fieldName = "charField";
         charField = fieldClass.getDeclaredField(fieldName);

         fieldName = "shortField";
         shortField = fieldClass.getDeclaredField(fieldName);

         fieldName = "intField";
         intField = fieldClass.getDeclaredField(fieldName);

         fieldName = "longField";
         longField = fieldClass.getDeclaredField(fieldName);

         fieldName = "floatField";
         floatField = fieldClass.getDeclaredField(fieldName);

         fieldName = "doubleField";
         doubleField = fieldClass.getDeclaredField(fieldName);
      }
      catch (NoSuchFieldException e)
      {
         fail("missing field " + fieldName + " in test support class " + fieldClass.getName());
      }

      // create the various objects that might or might not have an instance
      // of the field
      Support_Field parentClass = new Support_Field();
      SupportSubClass subclass = new SupportSubClass();
      SupportSubClass otherSubclass = new SupportSubClass();
      Object plainObject = new Object();

      Class illegalArgumentExceptionClass = new IllegalArgumentException().getClass();

      // The test will attempt to use pass an object to set for object, byte,
      // short, ..., float and double fields
      // and pass a byte to setByte for byte, short, ..., float and double
      // fields and so on.
      // It will also test if IllegalArgumentException is thrown when the
      // field does not exist in the given object and that
      // IllegalAccessException is thrown when trying to access an
      // inaccessible protected field.
      // The test will also check that IllegalArgumentException is thrown for
      // all other attempts.

      // Ordered by widening conversion, except for 'L' at the beg (which
      // stands for Object).
      // If the object provided to set can be unwrapped to a primitive, then
      // the set method can set
      // primitive fields.
      char types[] = { 'L', 'B', 'S', 'C', 'I', 'J', 'F', 'D' };
      Field fields[] = { objectField, byteField, shortField, charField, intField, longField, floatField, doubleField };
      Object values[] = { new Byte((byte) 1), new Byte((byte) 1), new Short((short) 1), new Character((char) 1), new Integer(1), new Long(1), new Float(1),
            new Double(1) };

      // test set methods
      for (int i = 0; i < types.length; i++)
      {
         char type = types[i];
         Object value = values[i];
         for (int j = i; j < fields.length; j++)
         {
            Field field = fields[j];
            fieldName = field.getName();
            if (field == charField && type != 'C')
            {
               // the exception is that bytes and shorts CANNOT be
               // converted into chars even though chars CAN be
               // converted into ints, longs, floats and doubles
               subclass.setField(type, subclass, field, illegalArgumentExceptionClass, value);
            }
            else
            {
               // setting type into field);
               subclass.setField(type, subclass, field, null, value);
               subclass.setField(type, otherSubclass, field, null, value);
               subclass.setField(type, parentClass, field, /* illegalAccessExceptionClass guich@javase */null, value);
               subclass.setField(type, plainObject, field, illegalArgumentExceptionClass, value);
            }
         }
         for (int j = 0; j < i; j++)
         {
            Field field = fields[j];
            fieldName = field.getName();
            // not setting type into field);
            subclass.setField(type, subclass, field, illegalArgumentExceptionClass, value);
         }
      }

      // test setBoolean
      Boolean booleanValue = Boolean.TRUE;
      subclass.setField('Z', subclass, booleanField, null, booleanValue);
      subclass.setField('Z', otherSubclass, booleanField, null, booleanValue);
      subclass.setField('Z', parentClass, booleanField, /* illegalAccessExceptionClass guich@javase */null, booleanValue);
      subclass.setField('Z', plainObject, booleanField, illegalArgumentExceptionClass, booleanValue);
      for (int j = 0; j < fields.length; j++)
      {
         Field listedField = fields[j];
         fieldName = listedField.getName();
         // not setting boolean into listedField
         subclass.setField('Z', subclass, listedField, illegalArgumentExceptionClass, booleanValue);
      }
      for (int i = 0; i < types.length; i++)
      {
         char type = types[i];
         Object value = values[i];
         subclass.setField(type, subclass, booleanField, illegalArgumentExceptionClass, value);
      }

      // We perform the analagous test on the get methods.

      // ordered by widening conversion, except for 'L' at the end (which
      // stands for Object), to which all primitives can be converted by
      // wrapping
      char newTypes[] = new char[] { 'B', 'S', 'C', 'I', 'J', 'F', 'D', 'L' };
      Field newFields[] = { byteField, shortField, charField, intField, longField, floatField, doubleField, objectField };
      fields = newFields;
      types = newTypes;
      // test get methods
      for (int i = 0; i < types.length; i++)
      {
         char type = types[i];
         for (int j = 0; j <= i; j++)
         {
            Field field = fields[j];
            fieldName = field.getName();
            if (type == 'C' && field != charField)
            {
               // the exception is that bytes and shorts CANNOT be
               // converted into chars even though chars CAN be
               // converted into ints, longs, floats and doubles
               subclass.getField(type, subclass, field, illegalArgumentExceptionClass);
            }
            else
            {
               // getting type from field
               subclass.getField(type, subclass, field, null);
               subclass.getField(type, otherSubclass, field, null);
               subclass.getField(type, parentClass, field, /* illegalAccessExceptionClass guich@javase */null);
               subclass.getField(type, plainObject, field, illegalArgumentExceptionClass);
            }
         }
         for (int j = i + 1; j < fields.length; j++)
         {
            Field field = fields[j];
            fieldName = field.getName();
            subclass.getField(type, subclass, field, illegalArgumentExceptionClass);
         }
      }

      // test getBoolean
      subclass.getField('Z', subclass, booleanField, null);
      subclass.getField('Z', otherSubclass, booleanField, null);
      subclass.getField('Z', parentClass, booleanField, null /* illegalAccessExceptionClass guich@javase */);
      subclass.getField('Z', plainObject, booleanField, illegalArgumentExceptionClass);
      for (int j = 0; j < fields.length; j++)
      {
         Field listedField = fields[j];
         fieldName = listedField.getName();
         // not getting boolean from listedField
         subclass.getField('Z', subclass, listedField, illegalArgumentExceptionClass);
      }
      for (int i = 0; i < types.length - 1; i++)
      {
         char type = types[i];
         subclass.getField(type, subclass, booleanField, illegalArgumentExceptionClass);
      }
      Object res = subclass.getField('L', subclass, booleanField, null);
      assertTrue(res instanceof Boolean);
   }

   /**
    * @tests java.lang.reflect.Field#getBoolean(java.lang.Object)
    */
   public void test_getBooleanLjava_lang_Object() throws Exception
   {
      // Test for method boolean
      // java.lang.reflect.Field.getBoolean(java.lang.Object)

      TestField x = new TestField();
      Field f = null;
      boolean val = false;
      f = x.getClass().getDeclaredField("booleanField");
      val = f.getBoolean(x);

      assertTrue(val);

      try
      {
         f = x.getClass().getDeclaredField("doubleField");
         f.getBoolean(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since doubleField is not a
         // boolean type
         return;
      }
      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getByte(java.lang.Object)
    */
   public void test_getByteLjava_lang_Object() throws Exception
   {
      // Test for method byte
      // java.lang.reflect.Field.getByte(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      byte val = 0;
      f = x.getClass().getDeclaredField("byteField");
      val = f.getByte(x);

      assertTrue(val == Byte.MAX_VALUE);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getByte(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since byteField is not a
         // boolean type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getChar(java.lang.Object)
    */
   public void test_getCharLjava_lang_Object() throws Exception
   {
      // Test for method char
      // java.lang.reflect.Field.getChar(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      char val = 0;
      f = x.getClass().getDeclaredField("charField");
      val = f.getChar(x);

      assertEquals('T', val);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getChar(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since charField is not a
         // boolean type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getDeclaringClass()
    */
   public void test_getDeclaringClass()
   {
      // Test for method java.lang.Class
      // java.lang.reflect.Field.getDeclaringClass()
      Field[] fields;

      fields = new TestField().getClass().getFields();
      assertTrue(fields[0].getDeclaringClass().equals(new TestField().getClass()));

      // Check the case where the field is inherited to be sure the parent
      // is returned as the declarator
      fields = new TestFieldSub1().getClass().getFields();
      assertTrue(fields[0].getDeclaringClass().equals(new TestField().getClass()));
   }

   /**
    * @tests java.lang.reflect.Field#getDouble(java.lang.Object)
    */
   public void test_getDoubleLjava_lang_Object() throws Exception
   {
      // Test for method double
      // java.lang.reflect.Field.getDouble(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      double val = 0.0;
      f = x.getClass().getDeclaredField("doubleField");
      val = f.getDouble(x);

      assertTrue(val == Double.MAX_VALUE);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getDouble(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since doubleField is not a
         // boolean type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getFloat(java.lang.Object)
    */
   public void test_getFloatLjava_lang_Object() throws Exception
   {
      // Test for method float
      // java.lang.reflect.Field.getFloat(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      float val = 0;
      f = x.getClass().getDeclaredField("floatField");
      val = f.getFloat(x);

      assertTrue(val == Float.MAX_VALUE);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getFloat(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since floatField is not a
         // boolean type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getInt(java.lang.Object)
    */
   public void test_getIntLjava_lang_Object() throws Exception
   {
      // Test for method int java.lang.reflect.Field.getInt(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      int val = 0;
      f = x.getClass().getDeclaredField("intField");
      val = f.getInt(x);

      assertTrue(val == Integer.MAX_VALUE);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getInt(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since IntField is not a
         // boolean type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getLong(java.lang.Object)
    */
   public void test_getLongLjava_lang_Object() throws Exception
   {
      // Test for method long
      // java.lang.reflect.Field.getLong(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      long val = 0;
      f = x.getClass().getDeclaredField("longField");
      val = f.getLong(x);

      assertTrue(val == Long.MAX_VALUE);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getLong(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // long type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getModifiers()
    */
   public void test_getModifiers() throws Exception
   {
      // Test for method int java.lang.reflect.Field.getModifiers()
      TestField x = new TestField();
      Field f = null;
      f = x.getClass().getDeclaredField("prsttrvol");

      int mod = f.getModifiers();
      int mask = (Modifier.PROTECTED | Modifier.STATIC) | (Modifier.TRANSIENT | Modifier.VOLATILE);
      int nmask = (Modifier.PUBLIC | Modifier.NATIVE);
      assertTrue(((mod & mask) == mask) && ((mod & nmask) == 0));
   }

   /**
    * @tests java.lang.reflect.Field#getName()
    */
   public void test_getName() throws Exception
   {
      // Test for method java.lang.String java.lang.reflect.Field.getName()
      TestField x = new TestField();
      Field f = null;
      f = x.getClass().getDeclaredField("shortField");

      assertEquals("shortField", f.getName());
   }

   /**
    * @tests java.lang.reflect.Field#getShort(java.lang.Object)
    */
   public void test_getShortLjava_lang_Object() throws Exception
   {
      // Test for method short
      // java.lang.reflect.Field.getShort(java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      short val = 0;

      f = x.getClass().getDeclaredField("shortField");
      val = f.getShort(x);

      assertTrue(val == Short.MAX_VALUE);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.getShort(x);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // short type
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#getType()
    */
   public void test_getType() throws Exception
   {
      // Test for method java.lang.Class java.lang.reflect.Field.getType()
      TestField x = new TestField();
      Field f = null;
      f = x.getClass().getDeclaredField("shortField");

      assertTrue(f.getType().equals(short.class));
   }

   /**
    * @tests java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
    */
   public void test_setLjava_lang_ObjectLjava_lang_Object() throws Exception
   {
      // Test for method void java.lang.reflect.Field.set(java.lang.Object,
      // java.lang.Object)
      TestField x = new TestField();
      Field f = null;
      double val = 0.0;
      f = x.getClass().getDeclaredField("doubleField");
      f.set(x, new Double(1.0));
      val = f.getDouble(x);

      assertEquals(1.0, val,0);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.set(x, new Double(1.0));
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // double type
         return;
      }
      try
      {
         f = x.getClass().getDeclaredField("doubleFField");
         f.set(x, new Double(1.0));
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since doubleFField is
         // declared as final
         return;
      }
      // Test setting a static field;
      f = x.getClass().getDeclaredField("doubleSField");
      f.set(x, new Double(1.0));
      val = f.getDouble(x);
      assertEquals(1.0, val,0);

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
    */
   public void test_setBooleanLjava_lang_ObjectZ() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setBoolean(java.lang.Object, boolean)
      TestField x = new TestField();
      Field f = null;
      boolean val = false;
      f = x.getClass().getDeclaredField("booleanField");
      f.setBoolean(x, false);
      val = f.getBoolean(x);

      assertTrue(!val);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setBoolean(x, true);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // boolean type
         return;
      }

      try
      {
         f = x.getClass().getDeclaredField("booleanFField");
         f.setBoolean(x, true);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since booleanField is
         // declared as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setByte(java.lang.Object, byte)
    */
   public void test_setByteLjava_lang_ObjectB() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setByte(java.lang.Object, byte)
      TestField x = new TestField();
      Field f = null;
      byte val = 0;
      f = x.getClass().getDeclaredField("byteField");
      f.setByte(x, (byte) 1);
      val = f.getByte(x);

      assertEquals(1, val);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setByte(x, (byte) 1);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // byte type
         return;
      }

      try
      {
         f = x.getClass().getDeclaredField("byteFField");
         f.setByte(x, (byte) 1);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since byteFField is declared
         // as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setChar(java.lang.Object, char)
    */
   public void test_setCharLjava_lang_ObjectC() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setChar(java.lang.Object, char)
      TestField x = new TestField();
      Field f = null;
      char val = 0;
      f = x.getClass().getDeclaredField("charField");
      f.setChar(x, (char) 1);
      val = f.getChar(x);

      assertEquals(1, val);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setChar(x, (char) 1);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // char type
         return;
      }

      try
      {
         f = x.getClass().getDeclaredField("charFField");
         f.setChar(x, (char) 1);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since charFField is declared
         // as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setDouble(java.lang.Object, double)
    */
   public void test_setDoubleLjava_lang_ObjectD() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setDouble(java.lang.Object, double)
      TestField x = new TestField();
      Field f = null;
      double val = 0.0;
      f = x.getClass().getDeclaredField("doubleField");
      f.setDouble(x, 1.0);
      val = f.getDouble(x);

      assertEquals(1.0, val,0);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setDouble(x, 1.0);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // double type
         return;
      }

      try
      {
         f = x.getClass().getDeclaredField("doubleFField");
         f.setDouble(x, 1.0);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since doubleFField is
         // declared as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setFloat(java.lang.Object, float)
    */
   public void test_setFloatLjava_lang_ObjectF() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setFloat(java.lang.Object, float)
      TestField x = new TestField();
      Field f = null;
      float val = 0.0F;
      f = x.getClass().getDeclaredField("floatField");
      f.setFloat(x, (float) 1);
      val = f.getFloat(x);

      assertEquals(1.0, val,0);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setFloat(x, (float) 1);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // float type
         return;
      }
      try
      {
         f = x.getClass().getDeclaredField("floatFField");
         f.setFloat(x, (float) 1);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since floatFField is
         // declared as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setInt(java.lang.Object, int)
    */
   public void test_setIntLjava_lang_ObjectI() throws Exception
   {
      // Test for method void java.lang.reflect.Field.setInt(java.lang.Object,
      // int)
      TestField x = new TestField();
      Field f = null;
      int val = 0;
      f = x.getClass().getDeclaredField("intField");
      f.setInt(x, (int) 1);
      val = f.getInt(x);

      assertEquals(1, val);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setInt(x, (int) 1);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // int type
         return;
      }
      try
      {
         f = x.getClass().getDeclaredField("intFField");
         f.setInt(x, (int) 1);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since intFField is declared
         // as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setLong(java.lang.Object, long)
    */
   public void test_setLongLjava_lang_ObjectJ() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setLong(java.lang.Object, long)
      TestField x = new TestField();
      Field f = null;
      long val = 0L;
      f = x.getClass().getDeclaredField("longField");
      f.setLong(x, (long) 1);
      val = f.getLong(x);

      assertEquals(1, val);

      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setLong(x, (long) 1);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // long type
         return;
      }
      try
      {
         f = x.getClass().getDeclaredField("longFField");
         f.setLong(x, (long) 1);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since longFField is declared
         // as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#setShort(java.lang.Object, short)
    */
   public void test_setShortLjava_lang_ObjectS() throws Exception
   {
      // Test for method void
      // java.lang.reflect.Field.setShort(java.lang.Object, short)
      TestField x = new TestField();
      Field f = null;
      short val = 0;
      f = x.getClass().getDeclaredField("shortField");
      f.setShort(x, (short) 1);
      val = f.getShort(x);

      assertEquals(1, val);
      try
      {
         f = x.getClass().getDeclaredField("booleanField");
         f.setShort(x, (short) 1);
      }
      catch (IllegalArgumentException ex)
      {
         // Good, Exception should be thrown since booleanField is not a
         // short type
         return;
      }
      try
      {
         f = x.getClass().getDeclaredField("shortFField");
         f.setShort(x, (short) 1);
      }
      catch (IllegalAccessException ex)
      {
         // Good, Exception should be thrown since shortFField is
         // declared as final
         return;
      }

      fail("Accessed field of invalid type");
   }

   /**
    * @tests java.lang.reflect.Field#toString()
    */
   public void test_toString() throws Exception
   {
      Field f = null;

      f = TestField.class.getDeclaredField("x");

      assertEquals(f.toString(),"private static final int tc.test.totalcross.lang.reflect.FieldTest$TestField.x");
      Class c = FieldTest.class;
      assertEquals(c.getDeclaredField("t").toString(), "int tc.test.totalcross.lang.reflect.FieldTest.t");
      assertEquals(c.getDeclaredField("it").toString(), "java.lang.Integer tc.test.totalcross.lang.reflect.FieldTest.it");

   }

   // /////////////////////////////////////////////////////////////////////////////////////////////////////////

   int t;
   Integer it;

   public void testRun()
   {
      try
      {
         test_equalsLjava_lang_Object();
         test_getLjava_lang_Object();
         testProtectedFieldAccess();
         test_getBooleanLjava_lang_Object();
         test_getByteLjava_lang_Object();
         test_getCharLjava_lang_Object();
         test_getDeclaringClass();
         test_getDoubleLjava_lang_Object();
         test_getFloatLjava_lang_Object();
         test_getIntLjava_lang_Object();
         test_getLongLjava_lang_Object();
         test_getModifiers();
         test_getName();
         test_getShortLjava_lang_Object();
         test_getType();
         test_setLjava_lang_ObjectLjava_lang_Object();
         test_setBooleanLjava_lang_ObjectZ();
         test_setByteLjava_lang_ObjectB();
         test_setCharLjava_lang_ObjectC();
         test_setDoubleLjava_lang_ObjectD();
         test_setFloatLjava_lang_ObjectF();
         test_setIntLjava_lang_ObjectI();
         test_setLongLjava_lang_ObjectJ();
         test_setShortLjava_lang_ObjectS();
         test_toString();
      }
      catch (Throwable e) 
      {
         String s = Vm.getStackTrace(e);
         throw new AssertionFailedError(getClass().getName()+" - "+e.getMessage()+" - "+s);
      }
   }
}
class TestAccess
{
   private static int xxx;
}
