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

package samples.sys.testcases;

import litebase.*;
import totalcross.io.*;
import totalcross.sys.*;
import totalcross.unit.TestCase;

/**
 * Does tests with add column.
 */
public class TestAddColumn extends TestCase
{
   /**
    * The connection with Litebase.
    */
   LitebaseConnection driver = AllTests.getInstance("Test");
   
   /**
    * A time object.
    */
   Time time = new Time();
         
   /**
    * Main test method.
    */
   public void testRun()
   {
      wrongSyntax(); // Tests wrong syntax using add column.
      addAfterInsert(); // Tests add column after some inserts.
      addAfterCreate(); // Tests add column just after creating the table.
      driver.closeAll();
   }
   
   /**
    * Drops and creates the test table.
    */
   private void create()
   {
      try
      {
         driver.executeUpdate("drop table person");
      }
      catch (DriverException exception) {}
      
      driver.execute("create table person (name char(10))");
   }
   
   /**
    * Tests wrong syntax.
    */
   private void wrongSyntax()
   {
      create();
      try // No type.
      {
         driver.executeUpdate("alter table person add column");
         fail("1");
      }
      catch (SQLParseException exception) {}
      try // Invalid type.
      {
         driver.executeUpdate("alter table person add column x");
         fail("2");
      }
      catch (SQLParseException exception) {}
      try // No type.
      {
         driver.executeUpdate("alter table person add x");
         fail("3");
      }
      catch (SQLParseException exception) {}
      try // An added column can't be declared as primary key.
      {
         driver.executeUpdate("alter table person add x char(5) primary key");
         fail("4");
      }
      catch (SQLParseException exception) {}
      try // An added column can't be declared as primary key.
      {
         driver.executeUpdate("alter table person add x char(5) default 'x' primary key");
         fail("5");
      }
      catch (SQLParseException exception) {}
      try // An added column declared as not null must have a not null default value.
      {
         driver.executeUpdate("alter table person add x char(5) not null");
         fail("6");
      }
      catch (SQLParseException exception) {}
      try // An added column declared as not null must have a not null default value.
      {
         driver.executeUpdate("alter table person add x char(5) not null default null");
         fail("7");
      }
      catch (SQLParseException exception) {}
      try // Totally wrong SQL. 
      {
         driver.executeUpdate("alter table person add primary key (name) x char(5)");
         fail("8");
      }
      catch (SQLParseException exception) {}
      try // Repeated column name.
      {
         driver.executeUpdate("alter table person add name char(5)");
         fail("9");
      }
      catch (SQLParseException exception) {}
      try // Repeated column name.
      {
         driver.executeUpdate("alter table person add rowid int");
         fail("10");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Adds new columns after doing inserts.
    */
   private void addAfterInsert()
   {
      create();
      int i = 100;
      PreparedStatement prepStmt = driver.prepareStatement("insert into person values (?)");
      
      time.year = 2012;
      time.month = 3;
      time.day = 20;
      time.hour = 16;
      time.minute = 53;
      time.second = 42;
      time.millis = 0;

      while (--i >= 0)
      {
         prepStmt.setString(0, "name" + i);
         prepStmt.executeUpdate();
      }
      
      driver.executeUpdate("alter table person add a char(10) default null");
      driver.executeUpdate("alter table person add b short default 5 not null");
      driver.executeUpdate("alter table person add c int");
      driver.executeUpdate("alter table person add d long");
      driver.executeUpdate("alter table person add e double");
      driver.executeUpdate("alter table person add f datetime default '2012/03/20 16:53:42'");
      driver.executeUpdate("alter table person add g blob(1 K)");
      
      ResultSet resultSet = driver.executeQuery("select * from person");
      assertEquals(100, resultSet.getRowCount());
      
      i = 100;
      while (resultSet.next())
      {
         assertEquals("name" + (--i), resultSet.getString(1));
         assertNull(resultSet.getString("a"));
         assertTrue(resultSet.isNull("a"));
         assertEquals(5, resultSet.getShort(3));
         assertFalse(resultSet.isNull(3));
         assertNull(resultSet.getString("c"));
         assertTrue(resultSet.isNull("c"));
         assertNull(resultSet.getString(5));
         assertTrue(resultSet.isNull(5));
         assertNull(resultSet.getString("e"));
         assertTrue(resultSet.isNull("e"));
         assertEquals(time, resultSet.getDateTime(7));
         assertFalse(resultSet.isNull(7));
         assertNull(resultSet.getString("g"));
         assertTrue(resultSet.isNull("g"));
      }
      resultSet.close();
      
      time.day = 26;
      time.month = 6;
      time.year = 1979;
      time.millis = time.second = time.minute = time.hour = 0;
      
      driver.executeUpdate("update person set a = 'a', b = 1, c = null, d = 2, e = 3.5, f = '1979/06/26'");
      assertEquals(i = 100, (resultSet = driver.executeQuery("select * from person")).getRowCount());
      while (resultSet.next())
      {
         assertEquals("name" + (--i), resultSet.getString(1));
         assertEquals("a", resultSet.getString("a"));
         assertFalse(resultSet.isNull("a"));
         assertEquals(1, resultSet.getShort(3));
         assertFalse(resultSet.isNull(3));
         assertNull(resultSet.getString("c"));
         assertTrue(resultSet.isNull("c"));
         assertEquals(2, resultSet.getLong(5));
         assertFalse(resultSet.isNull(5));
         assertEquals(3.5, resultSet.getDouble("e"), 0.001);
         assertFalse(resultSet.isNull("e"));
         assertEquals(time, resultSet.getDateTime(7));
         assertFalse(resultSet.isNull(7));
         assertNull(resultSet.getString("g"));
         assertTrue(resultSet.isNull("g"));
         
      }
      resultSet.close();
      
      driver.executeUpdate("delete from person");
      
      coreTest();
   }
   
   /**
    * Adds new columns after creating the table.
    */
   private void addAfterCreate()
   {
      create();
      driver.executeUpdate("alter table person add a char(10) default null");
      driver.executeUpdate("alter table person add b short default 5 not null");
      driver.executeUpdate("alter table person add c int");
      driver.executeUpdate("alter table person add d long");
      driver.executeUpdate("alter table person add e double");
      driver.executeUpdate("alter table person add f datetime default '2012/03/20 16:53:42'");
      driver.executeUpdate("alter table person add g blob(1 K)");
      
      coreTest();
   }
   
   /**
    * Executes the main test.
    */
   private void coreTest()
   {
      doInserts();
      doSelect();
      driver.purge("person");
      doSelect();
      addAndMetaData();   
      addAndRecover();
      addAndRowIterator();
      addAndIndices();
      addTooManyColumns();
   }
   
   /**
    * Tests result set meta data with add column.
    */
   private void addAndMetaData()
   {
      ResultSet resultSet = driver.executeQuery("select * from person");
      ResultSetMetaData metaData = resultSet.getResultSetMetaData();
      
      time.year = 2012;
      time.month = 3;
      time.day = 20;
      time.hour = 16;
      time.minute = 53;
      time.second = 42;
      time.millis = 0;
      
      assertEquals(8, metaData.getColumnCount());
      
      assertEquals(10, metaData.getColumnDisplaySize(1));
      assertEquals("name", metaData.getColumnLabel(1));
      assertEquals(ResultSetMetaData.CHAR_TYPE, metaData.getColumnType(1));
      assertEquals("chars", metaData.getColumnTypeName(1));
      assertEquals("person", metaData.getColumnTableName(1));
      assertEquals("person", metaData.getColumnTableName("name"));
      assertFalse(metaData.hasDefaultValue(1));
      assertFalse(metaData.hasDefaultValue("name"));
      assertFalse(metaData.isNotNull(1));
      assertFalse(metaData.isNotNull("name"));
      assertNull(metaData.getDefaultValue(1));
      assertNull(metaData.getDefaultValue("name"));
      
      assertEquals(10, metaData.getColumnDisplaySize(2));
      assertEquals("a", metaData.getColumnLabel(2));
      assertEquals(ResultSetMetaData.CHAR_TYPE, metaData.getColumnType(2));
      assertEquals("chars", metaData.getColumnTypeName(2));
      assertEquals("person", metaData.getColumnTableName(2));
      assertEquals("person", metaData.getColumnTableName("a"));
      assertFalse(metaData.hasDefaultValue(2));
      assertFalse(metaData.hasDefaultValue("a"));
      assertFalse(metaData.isNotNull(2));
      assertFalse(metaData.isNotNull("a"));
      assertNull(metaData.getDefaultValue(2));
      assertNull(metaData.getDefaultValue("a"));
      
      assertEquals(6, metaData.getColumnDisplaySize(3));
      assertEquals("b", metaData.getColumnLabel(3));
      assertEquals(ResultSetMetaData.SHORT_TYPE, metaData.getColumnType(3));
      assertEquals("short", metaData.getColumnTypeName(3));
      assertEquals("person", metaData.getColumnTableName(3));
      assertEquals("person", metaData.getColumnTableName("b"));
      assertTrue(metaData.hasDefaultValue(3));
      assertTrue(metaData.hasDefaultValue("b"));
      assertTrue(metaData.isNotNull(3));
      assertTrue(metaData.isNotNull("b"));
      assertEquals("5", metaData.getDefaultValue(3));
      assertEquals("5", metaData.getDefaultValue("b"));
      
      assertEquals(11, metaData.getColumnDisplaySize(4));
      assertEquals("c", metaData.getColumnLabel(4));
      assertEquals(ResultSetMetaData.INT_TYPE, metaData.getColumnType(4));
      assertEquals("int", metaData.getColumnTypeName(4));
      assertEquals("person", metaData.getColumnTableName(4));
      assertEquals("person", metaData.getColumnTableName("c"));
      assertFalse(metaData.hasDefaultValue(4));
      assertFalse(metaData.hasDefaultValue("c"));
      assertFalse(metaData.isNotNull(4));
      assertFalse(metaData.isNotNull("c"));
      assertNull(metaData.getDefaultValue(4));
      assertNull(metaData.getDefaultValue("c"));
      
      assertEquals(20, metaData.getColumnDisplaySize(5));
      assertEquals("d", metaData.getColumnLabel(5));
      assertEquals(ResultSetMetaData.LONG_TYPE, metaData.getColumnType(5));
      assertEquals("long", metaData.getColumnTypeName(5));
      assertEquals("person", metaData.getColumnTableName(5));
      assertEquals("person", metaData.getColumnTableName("d"));
      assertFalse(metaData.hasDefaultValue(5));
      assertFalse(metaData.hasDefaultValue("d"));
      assertFalse(metaData.isNotNull(5));
      assertFalse(metaData.isNotNull("d"));
      assertNull(metaData.getDefaultValue(5));
      assertNull(metaData.getDefaultValue("d"));
      
      assertEquals(21, metaData.getColumnDisplaySize(6));
      assertEquals("e", metaData.getColumnLabel(6));
      assertEquals(ResultSetMetaData.DOUBLE_TYPE, metaData.getColumnType(6));
      assertEquals("double", metaData.getColumnTypeName(6));
      assertEquals("person", metaData.getColumnTableName(6));
      assertEquals("person", metaData.getColumnTableName("e"));
      assertFalse(metaData.hasDefaultValue(6));
      assertFalse(metaData.hasDefaultValue("e"));
      assertFalse(metaData.isNotNull(6));
      assertFalse(metaData.isNotNull("e"));
      assertNull(metaData.getDefaultValue(6));
      assertNull(metaData.getDefaultValue("e"));
      
      assertEquals(31, metaData.getColumnDisplaySize(7));
      assertEquals("f", metaData.getColumnLabel(7));
      assertEquals(ResultSetMetaData.DATETIME_TYPE, metaData.getColumnType(7));
      assertEquals("datetime", metaData.getColumnTypeName(7));
      assertEquals("person", metaData.getColumnTableName(7));
      assertEquals("person", metaData.getColumnTableName("f"));
      assertTrue(metaData.hasDefaultValue(7));
      assertTrue(metaData.hasDefaultValue("f"));
      assertFalse(metaData.isNotNull(7));
      assertFalse(metaData.isNotNull("f"));
      assertEquals("2012/03/20 16:53:42:000", metaData.getDefaultValue(7));
      assertEquals("2012/03/20 16:53:42:000", metaData.getDefaultValue("f"));
      
      assertEquals(-1, metaData.getColumnDisplaySize(8));
      assertEquals("g", metaData.getColumnLabel(8));
      assertEquals(ResultSetMetaData.BLOB_TYPE, metaData.getColumnType(8));
      assertEquals("blob", metaData.getColumnTypeName(8));
      assertEquals("person", metaData.getColumnTableName(8));
      assertEquals("person", metaData.getColumnTableName("g"));
      assertFalse(metaData.hasDefaultValue(8));
      assertFalse(metaData.hasDefaultValue("g"));
      assertFalse(metaData.isNotNull(8));
      assertFalse(metaData.isNotNull("g"));
      assertNull(metaData.getDefaultValue(8));
      assertNull(metaData.getDefaultValue("g"));
      
      resultSet.close();
   }
   
   /**
    * Does the inserts for the tests.
    */
   private void doInserts()
   {
      int i = 100;
      PreparedStatement prepStmt = driver.prepareStatement("insert into person values (?, ?, ?, ?, ?, ?, ?, ?)");
      
      time.year = 2012;
      time.month = 3;
      time.day = 20;
      time.hour = 16;
      time.minute = 53;
      time.second = 42;
      time.millis = 0;
      
      while (--i >= 0)
      {
         prepStmt.setString(0, "name" + i);
         prepStmt.setString(1, "a" + i);
         prepStmt.setShort(2, (short)i);
         prepStmt.setInt(3, i);
         prepStmt.setLong(4, i);
         prepStmt.setDouble(5, i);
         prepStmt.setDateTime(6, time);
         prepStmt.setBlob(7, ("name" + i).getBytes());
         prepStmt.executeUpdate();
      }
   }
   
   /**
    * Does a select for the tests.
    */
   private void doSelect()
   {
      ResultSet resultSet = driver.executeQuery("select * from person");
      int i = 100;
      assertEquals(100, resultSet.getRowCount());
      while (resultSet.next())
      {
         assertEquals("name" + (--i), resultSet.getString("name"));
         assertEquals("a" + i, resultSet.getString(2));
         assertEquals(i, resultSet.getShort("b"));
         assertEquals(i, resultSet.getInt(4));
         assertEquals(i, resultSet.getLong("d"));
         assertEquals(i, resultSet.getDouble(6), 0.001);
         assertEquals(time, resultSet.getDateTime("f"));
         assertEquals(("name" + i).getBytes(), resultSet.getBlob(8));
      }
      resultSet.close();
   }
   
   /**
    * Tests table recovering after adding a column.
    */
   private void addAndRecover()
   {
      try
      {
         String path = driver.getSourcePath() + "Test-person.db";
         
         driver.closeAll();
         
         File dbFile = new File(path, File.READ_WRITE); // The table is closed after recovering it.
         byte[] oneByte = new byte[1];
         
         // Pretends that the table was not closed correctly.   
         dbFile.setPos(6);
         dbFile.readBytes(oneByte, 0, 1);
         
         if (AllTests.useCrypto)
            oneByte[0] ^= 0xAA;
         oneByte[0] = (byte)(oneByte[0] & 2);
         if (AllTests.useCrypto)
            oneByte[0] ^= 0xAA;
         dbFile.setPos(6);
         dbFile.writeBytes(oneByte, 0, 1);
         dbFile.close();
      }
      catch (IOException exception)
      {
         fail("11");
      }
      driver = AllTests.getInstance("Test");
      
      try
      {
         doSelect();
         fail("12");
      }
      catch (TableNotClosedException exception)
      {
         driver.recoverTable("person");
         doSelect();
      }
   }
   
   /**
    * Tests add column with row iterator.
    */
   private void addAndRowIterator()
   {
      RowIterator it = driver.getRowIterator("PERSON"); // Gets the row iterator.
      int i = 100;
      
      while (it.next())
      {
         // Confirms the row id and that the NEW attribute is set.
         assertTrue(101 - i == it.rowid || 201 - i == it.rowid);
         assertEquals(RowIterator.ROW_ATTR_NEW, it.attr);

         // Checks the values of the iterator.
         assertEquals("name" + (--i), it.getString(1));
         assertFalse(it.isNull(1));
         assertEquals("a" + i, it.getString(2));
         assertFalse(it.isNull(2));
         assertEquals(i, it.getShort(3));
         assertFalse(it.isNull(3));
         assertEquals(i, it.getInt(4));
         assertFalse(it.isNull(4));
         assertEquals(i, it.getLong(5));
         assertFalse(it.isNull(5));
         assertEquals(i, it.getDouble(6), 0.001);
         assertFalse(it.isNull(6));   
         assertEquals(time, it.getDateTime(7));  
         assertFalse(it.isNull(7));
         assertEquals(("name" + i).getBytes(), it.getBlob(8));  
         assertFalse(it.isNull(8));
      }
      
      it.close();
   }
   
   /**
    * Creates indices and tests added columns.
    */
   private void addAndIndices()
   {
      driver.execute("create index idx on person(name)");
      driver.execute("create index idx on person(a)");
      driver.execute("create index idx on person(b)");
      driver.execute("create index idx on person(c)");
      driver.execute("create index idx on person(d)");
      driver.execute("create index idx on person(e)");
      driver.execute("create index idx on person(f)");      
      testSelectWithIndices();
      
      driver.executeUpdate("drop index * on person");
      driver.execute("create index idx on person (name, a, b, c, d, e, f)");
      testSelectWithIndices();
   }
   
   /**
    * Tests a select with the created indices above.
    */
   private void testSelectWithIndices()
   {
      ResultSet resultSet;
      PreparedStatement psSelect = driver.prepareStatement("select * from person where name = ? and a = ? and b = ? and c = ? and d = ? and e = ? " 
                                                                                                                 + "and f = '2012/03/20 16:53:42'");
      
      int i = 100;
      while (--i >= 0)
      {
         psSelect.setString(0, "name" + i);
         psSelect.setString(1, "a" + i);
         psSelect.setShort(2, (short)i);
         psSelect.setInt(3, i);
         psSelect.setLong(4, i);
         psSelect.setDouble(5, i);
         
         assertEquals(1, (resultSet = psSelect.executeQuery()).getRowCount());
         resultSet.first();
         
         assertEquals("name" + i, resultSet.getString("name"));
         assertEquals("a" + i, resultSet.getString(2));
         assertEquals(i, resultSet.getShort("b"));
         assertEquals(i, resultSet.getInt(4));
         assertEquals(i, resultSet.getLong("d"));
         assertEquals(i, resultSet.getDouble(6), 0.001);
         assertEquals(time, resultSet.getDateTime("f"));
         assertEquals(("name" + i).getBytes(), resultSet.getBlob(8));
         
         assertFalse(resultSet.next());
         resultSet.close();
      }
   }
   
   /**
    * Adds many columns till exceeding the maximum number of columns.
    */
   private void addTooManyColumns()
   {
      try
      {
         int i = 246;
         StringBuffer sBuffer = new StringBuffer(50);
         
         sBuffer.append("alter table person add a");         
         while (--i >= 0)
         {
            sBuffer.setLength(24);            
            sBuffer.append(i);
            sBuffer.append(" int");
            driver.executeUpdate(sBuffer.toString());
         }
         
         fail("13");
      }
      catch (SQLParseException exception) {}
   }
}
