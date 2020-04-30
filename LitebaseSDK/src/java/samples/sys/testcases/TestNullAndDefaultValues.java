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
import totalcross.unit.*;
import totalcross.sys.*;
import totalcross.util.Date;

/**
 * This test verifies default and null values for Litebase
 */
public class TestNullAndDefaultValues extends TestCase
{
   LitebaseConnection driver;
   
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      driver = AllTests.getInstance("Test"); 
      testSimpleInserts(); // Tests simple inserts with null and defaults.
      testGettingNulls(); // Tests getting null values.
      testUpdatingNulls(); // Tests updating null values.
      testUpdatingWithBool(); // Tests updating with AND and OR boolean connectors.
      testSelects(); // Tests selects.
      testDefaultNull(); // Tests default null.
      testPrimaryKeyNull(); // Tests primary key with null.
      testOrderBy(); // Tests order by ordering.
      testAggregationFunctions(); // Tests aggregation functions.
      testFunctionsAndNulls(); // Tests functions and null values.
      testPreparedStatements(); // Tests prepared tatements.
      testLikeWithNull(); // Tests like with null in the table.
      driver.closeAll();
   }

   /**
    * Tests simple inserts with null and defaults.
    */
   private void testSimpleInserts()
   {
      if (driver.exists("person")) // Drops table if it exists.
         driver.executeUpdate("drop table person");
      
      // Invalid default empty date and empty datetime, and invalid date and datetime.
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, birth Date default '', " 
                                                                                                                     + "years DateTime not null)");
         fail("1");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, " 
                                          + "birth Date default '1981/06/06', years DateTime default '')");
         fail("2");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, birth Date default ' ', " 
                                                                                                                    + "years DateTime not null)");
         fail("3");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, " 
                                          + "birth Date default '1981/06/06', years DateTime default ' ')");
         fail("4");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, birth Date default '2010/02/29', " 
                                                                                                                    + "years DateTime not null)");
         fail("5");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, " 
                                          + "birth Date default '1981/06/06', years DateTime default '1981/06/06 24:08:01.234')");
         fail("6");
      }
      catch (SQLParseException exception) {}

      // Tests default string bigger than the definition.
      try
      {
         driver.execute("create table person(name char(6) primary key default 'rnovais' not null, age int default 10, " 
                                                                           + "birth Date default '1981/06/06', years DateTime not null)");
         fail("7");
      }
      catch (SQLParseException exception) {}
      
      // Tests short values out of range.
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age short default 32768, " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("8");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age short default -32769, " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("9");
      }
      catch (SQLParseException exception) {}
      
      // Tests int values out of range.
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 2147483648, " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("10");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default -2147483649, " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("11");
      }
      catch (SQLParseException exception) {}
      
      // Tests long values out of range.
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age long default 9223372036854775808, " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("12");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age long default -9223372036854775809, " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("13");
      }
      catch (SQLParseException exception) {}
      
      // Wrong default value types;
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 'rnovais', " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("14");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age float default 'rnovais', " 
                                                                             + "birth Date default '1981/06/06', years DateTime not null)");
         fail("15");
      }
      catch (SQLParseException exception) {}
      
      // Tests default string with its strict size.
      driver.execute("create table person(name char(7) primary key default 'rnovais' not null, age int default 10, " 
                                                                                  + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person");   
      
      // Tests short values in the limit of the range.
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age short default +32767, " 
                                                                                   + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person");
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age short default -32768, " 
                                                                                   + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person");
      
      // Tests int values in the limit of the range.
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default +2147483647, " 
                                                                                   + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person");
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default -2147483648, " 
                                                                                   + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person");   
      
      // Tests long values in the limit of the range.
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age long default +9223372036854775807, " 
                                                                                   + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person"); 
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age long default -9223372036854775808, " 
                                                                                   + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("drop table person");   
      
      // Creates and populates the table.
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int default 10, " 
                                       + "birth Date default '1981/06/06', years DateTime not null)");
      driver.executeUpdate("insert into person(name, birth, years) values ('renato novais', '1985/02/28',' 2006/08-21    12:08:01:234 ')");
      driver.executeUpdate("insert into person(birth, years) values ( '1985/02/28',' 2006/08-21    12:08:01:234 ')"); // Not null with default.
      driver.executeUpdate("insert into person(name,age, years) values ('indira gomes', 14,' 2006/08-21    12:08:01:234 ')");
      driver.executeUpdate("insert into person(name, years) values ('caio novais', ' 2006/08-21    12:08:01:234 ')");
      
      try // It is not possible not to insert a value in a column with a non-null restriction and no default value. 
      {
         driver.executeUpdate("insert into person(name) values ('caio novais')");
         fail("16");
      }
      catch (DriverException exception) {}

      // Queries the table.
      ResultSet rs = driver.executeQuery("Select * from person");
      assertEquals(4, rs.getRowCount());
      String expectedTime = (new Time(2006, 8, 21, 12, 8, 1, 234)).toString();
      String expectedDate = Date.formatDate(28, 02, 1985, Settings.DATE_DMY);

      // Checks the results.
      assertTrue(rs.next());
      assertEquals("renato novais", rs.getString("name"));
      assertEquals(10, rs.getInt("age"));
      assertEquals(expectedDate, rs.getDate(3).toString(Settings.DATE_DMY));
      assertEquals(expectedTime, rs.getDateTime(4).toString());

      assertTrue(rs.next());
      assertEquals("rnovais", rs.getString("name"));
      assertEquals(10, rs.getInt("age"));
      assertEquals(expectedDate, rs.getDate(3).toString(Settings.DATE_DMY));
      assertEquals(expectedTime, rs.getDateTime(4).toString());

      assertTrue(rs.next());
      assertEquals("indira gomes", rs.getString("name"));
      assertEquals(14, rs.getInt("age"));
      assertEquals(expectedDate = Date.formatDate(06, 06, 1981, Settings.DATE_DMY), rs.getDate(3).toString(Settings.DATE_DMY));
      assertEquals(expectedTime, rs.getDateTime(4).toString());

      assertTrue(rs.next());
      assertEquals("caio novais", rs.getString("name"));
      assertEquals(10, rs.getInt("age"));
      assertEquals(expectedDate, rs.getDate(3).toString(Settings.DATE_DMY));
      assertEquals(expectedTime, rs.getDateTime(4).toString());
      rs.close(); 
      
      // juliana@202_12: The default values must have been correctly saved on the table.
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      assertEquals(4, (rs = driver.executeQuery("Select * from person")).getRowCount());
      rs.close(); 
   }

   /**
    * Tests getting null values.
    */
   private void testGettingNulls() 
   {
      driver.executeUpdate("drop table person"); // Drops the table.

      // Creates and populates the table.
      driver.execute("create table person(field0 char(20),field1 short, field2 int, field3 long, field4 float, field5 double, field6 date, " 
                                                                                                                           + "field7 DateTime)");
      driver.executeUpdate("insert into person values (null,null,null,null,null,null,null,null)");
      driver.executeUpdate("insert into person values (null,null,null,null,null,null,null,null)");
      driver.executeUpdate("delete person where rowid = 2");
      driver.purge("person");
      
      
      ResultSet rs = driver.executeQuery("Select * from person");
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertNull(rs.getString("field0"));
      assertEquals(0, rs.getShort("field1"));
      assertEquals(0, rs.getInt("field2"));
      assertEquals(0, rs.getLong("field3"));
      assertEquals(0, rs.getFloat("field4"), 1e-2);
      assertEquals(0, rs.getDouble("field5"), 1e-2);
      assertNull(rs.getDate("field6"));
      assertNull(rs.getDateTime("field7"));
      
      // All the fields must be null.
      assertTrue(rs.isNull("field0"));
      assertTrue(rs.isNull("field1"));
      assertTrue(rs.isNull("field2"));
      assertTrue(rs.isNull("field3"));
      assertTrue(rs.isNull("field4"));
      assertTrue(rs.isNull("field5"));
      assertTrue(rs.isNull("field6"));
      assertTrue(rs.isNull("field7"));
      assertTrue(rs.isNull(1));
      assertTrue(rs.isNull(2));
      assertTrue(rs.isNull(3));
      assertTrue(rs.isNull(4));
      assertTrue(rs.isNull(5));
      assertTrue(rs.isNull(6));
      assertTrue(rs.isNull(7));
      assertTrue(rs.isNull(8));
      
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("Select * from person where field0 is null and field1 is null and field2 is null and field3 is null " 
                                              + "and field4 is null and field5 is null and field6 is null")).getRowCount());
      assertTrue(rs.next());
      assertNull(rs.getString("field0"));
      assertEquals(0, rs.getShort("field1"));
      assertEquals(0, rs.getInt("field2"));
      assertEquals(0, rs.getLong("field3"));
      assertEquals(0, rs.getFloat("field4"), 1e-2);
      assertEquals(0, rs.getDouble("field5"), 1e-2);
      assertNull(rs.getDate("field6"));
      assertNull(rs.getDateTime("field7"));
      
      // All the fields must be null.
      assertTrue(rs.isNull("field0"));
      assertTrue(rs.isNull("field1"));
      assertTrue(rs.isNull("field2"));
      assertTrue(rs.isNull("field3"));
      assertTrue(rs.isNull("field4"));
      assertTrue(rs.isNull("field5"));
      assertTrue(rs.isNull("field6"));
      assertTrue(rs.isNull("field7"));
      assertTrue(rs.isNull(1));
      assertTrue(rs.isNull(2));
      assertTrue(rs.isNull(3));
      assertTrue(rs.isNull(4));
      assertTrue(rs.isNull(5));
      assertTrue(rs.isNull(6));
      assertTrue(rs.isNull(7));
      assertTrue(rs.isNull(8));
      
      rs.close();
   }

   /**
    * Tests updating null values.
    */
   private void testUpdatingNulls() 
   {
      driver.executeUpdate("drop table person"); // Drops the table.

      // Creates and populates the table.
      driver.execute("create table person(name char(20) default '', age int default 10)");
      driver.execute("create index idx on person(name)");
      driver.executeUpdate("insert into person( name, age) values ('InDiRa GoMeS', null)");
      
      // Closes and reopens Litebase to test that the default values data will be saved correctly.
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      
      ResultSet rs = driver.executeQuery("Select age, name from person where name = 'InDiRa GoMeS'");
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("InDiRa GoMeS", rs.getString("name"));
      assertEquals(0, rs.getInt("age"));
      rs.close();

      // Tests an update.
      driver.executeUpdate("update person set age = 21, name = null");
      rs = driver.executeQuery("Select age, name from person");
      assertEquals(1, (rs = driver.executeQuery("Select age, name from person")).getRowCount());
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals(21, rs.getInt("age"));
      rs.close();
      
      // Updates the table with a not null value.
      driver.executeUpdate("update person set name = 'juliana', age = 32 where name is null");
      rs = driver.executeQuery("Select age, name from person");
      assertEquals(1, (rs = driver.executeQuery("Select age, name from person")).getRowCount());
      assertTrue(rs.next());
      assertNotNull(rs.getString("name"));
      assertEquals(32, rs.getInt("age"));
      rs.close();
      
      // Tests empty default value.
      driver.executeUpdate("insert into person(age) values (null)");
      assertEquals(2, (rs = driver.executeQuery("Select age, name from person where name = 'juliana' or name = ''")).getRowCount());
      assertTrue(rs.next());
      assertEquals("juliana", rs.getString("name"));
      assertEquals(32, rs.getInt("age"));
      assertTrue(rs.next());
      assertEquals("", rs.getString("name"));
      assertEquals(0, rs.getInt("age"));
      rs.close();
   }

   /**
    * Tests updating with AND and OR boolean connectors.
    */
   private void testUpdatingWithBool() 
   {
      driver.executeUpdate("drop table person"); // Drops the table.
      
      // Creates, populates the table.
      driver.execute("create table person(name char(20) default 'ZeneS', age int)");
      driver.execute("create index idx on person (name, age)");
      driver.executeUpdate("insert into person( age, name) values (21, 'renato')");
      driver.executeUpdate("insert into person( age, name) values (null, 'maria')");
      
      // Updates the table.
      driver.executeUpdate("update person set age = 12, name = null where age is not null and name is null");
      driver.executeUpdate("update person set age = 12, name = null where age is not null and name is not null");
      driver.executeUpdate("update person set age = 12, name = null where name = 'joao' or age is not null");
      driver.executeUpdate("update person set age = null, name = 'zENEs' where name is null and age = 12");

      ResultSet rs = driver.executeQuery("Select age, name from person");
      assertEquals(2, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("zENEs", rs.getString("name"));
      assertEquals(0, rs.getInt("age"));
      assertTrue(rs.next());
      assertEquals("maria", rs.getString("name"));
      assertEquals(0, rs.getInt("age"));
      rs.close();
      
      assertEquals(0, (rs = driver.executeQuery("Select name, age from person where name = 'zENEs' and age = 0")).getRowCount());
      rs.close();
      assertEquals(0, (rs = driver.executeQuery("Select name, age from person where name = 'maria' and age = 0")).getRowCount());
      rs.close();
      
      driver.executeUpdate("delete from person"); // Empties the table.
      
      // Re-populates the table.
      driver.executeUpdate("insert into person( age, name) values (21, 'renato')");
      driver.executeUpdate("insert into person( age, name) values (null, 'jener')");

      // Updates the table.
      driver.executeUpdate("update person set age = null, name = 'indira' where age is null");
      driver.executeUpdate("update person set name = 'TI�oZ�o' where age is not null");

      assertEquals(2, (rs = driver.executeQuery("Select name, age from person")).getRowCount());
      assertTrue(rs.next());
      assertEquals("TI�oZ�o", rs.getString("name"));
      assertEquals(21, rs.getInt("age"));
      assertTrue(rs.next());
      assertEquals("indira", rs.getString("name"));
      assertEquals(0, rs.getInt("age"));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("Select name, age from person where name = 'TI�oZ�o' and age = 21")).getRowCount());
      assertTrue(rs.next());
      assertEquals("TI�oZ�o", rs.getString("name"));
      assertEquals(21, rs.getInt("age"));
      rs.close();
      assertEquals(0, (rs = driver.executeQuery("Select name, age from person where name = 'indira' and age = 0")).getRowCount());
      rs.close();
   }

   /**
    * Tests selects.
    */
   private void testSelects()
   {
      driver.executeUpdate("drop table person"); // Drops the table.
      
      // Creates and populates the table.
      driver.execute("create table person(name char(20) primary key default 'rnovais' not null, age int, birth DATE default '1994/05/06')");
      driver.executeUpdate("insert into person( age, name) values (21, 'maria')");
      driver.executeUpdate("insert into person( name) values ('parazita')");
      driver.executeUpdate("insert into person( age, birth) values (30,null)");
      driver.executeUpdate("insert into person( name, age) values ('VUlcao',5000)");
      driver.executeUpdate("insert into person( name) values ('Plinio')");
      
      try // Primary key violation.
      {
         
         driver.executeUpdate("insert into person( age, birth) values (17,'2005/03/12')");
         fail("17");
      }
      catch(PrimaryKeyViolationException exception) {}
      
      // More inserts.
      driver.executeUpdate("insert into person( name, age, birth) values ('Maria Beatriz',1,null)");
      driver.executeUpdate("insert into person( name, age, birth) values ('Caio', null, null)");
      driver.executeUpdate("insert into person( name, age, birth) values ('Lucas', null, '2020/11/30')");
      driver.executeUpdate("insert into person( birth, name) values ('1998/12/23', 'celia')");
      driver.executeUpdate("insert into person values ('Marlene',10,'2000/02/04')");
      
      ResultSet rs = driver.executeQuery("Select * from person where name is null");
      assertEquals(0, rs.getRowCount());
      rs.close();

      assertEquals(10, (rs = driver.executeQuery("Select * from person where name is not null")).getRowCount());
      rs.close();

      assertEquals(5, (rs = driver.executeQuery("Select * from person where age is not null")).getRowCount());
      assertTrue(rs.next());

      assertEquals(6, rs.getDate("birth").getDay());
      assertEquals(5, rs.getDate("birth").getMonth());
      assertEquals(1994, rs.getDate("birth").getYear());

      assertEquals(3, (rs = driver.executeQuery("Select * from person where birth is null")).getRowCount());
      assertTrue(rs.next());
      assertEquals("rnovais", rs.getString("name"));
      assertTrue(rs.next());
      assertEquals("Maria Beatriz", rs.getString("name"));
      assertEquals(1, rs.getInt("age"));
      assertNull(rs.getString("birth"));
      assertTrue(rs.next());
      assertEquals("Caio", rs.getString("name"));
      rs.close();

      assertEquals(3, (rs = driver.executeQuery("Select * from person where name is not null and age is not null and birth is not null")).getRowCount());
      assertTrue(rs.next());
      assertEquals("maria", rs.getString("name"));
      assertTrue(rs.next());
      assertEquals("VUlcao", rs.getString("name"));
      assertEquals(5000, rs.getInt("age"));
      assertEquals(6, rs.getDate("birth").getDay());
      assertEquals(5, rs.getDate("birth").getMonth());
      assertEquals(1994, rs.getDate("birth").getYear());
      assertTrue(rs.next());
      assertEquals("Marlene", rs.getString("name"));
      rs.close();  
   }

   /**
    * Tests default null.
    */
   private void testDefaultNull()
   {
      // Drops and re-creates the table.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (name char(20) default null not null, age int default 21)");
      
      try // Can't insert a null when the column can't have a null.
      {
         driver.executeUpdate("insert into person values (null,null)");
         fail("18");
      }
      catch (DriverException exception) {}
      
      driver.executeUpdate("insert into person values ('maria',22)");
      
      try // Can't update to a null when the column can't have a null.
      {
         driver.executeUpdate("update person set name = null, age = 21");
         fail("19");
      }
      catch (DriverException e) {}
   }

   /**
    * Tests primary key with null.
    */
   private void testPrimaryKeyNull() 
   {
      // Drops and re-creates the table.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (name char(20) primary key , age int default 21)");
      
      try // A primary key can't be null.
      {
         driver.executeUpdate("insert into person values (null,null)");
         fail("20");
      }
      catch (DriverException exception) {}
      try // A primary key can't be null.
      {
         driver.executeUpdate("insert into person(age) values (null)");
         fail("21");
      }
      catch (DriverException exception) {}
      try // A primary key can't be update to null.
      {
         driver.executeUpdate("update person set name = null, age = 21");
         fail("22");
      }
      catch (DriverException exception) {}

      // Drops the primary key and inserts a null.
      driver.executeUpdate("alter table person drop primary key");
      driver.executeUpdate("insert into person values (null, null)");
      
      try // Now, the primary key can't be created because of a null in it.
      {
         driver.executeUpdate("alter table person add primary key(name)");
         fail("23");
      } 
      catch (DriverException exception) {}
   }

   /**
    * Tests order by ordering.
    */
   private void testOrderBy()
   {
      driver.executeUpdate("drop table person"); // Drops the table.

      // Creates and populates the table.
      driver.execute("create table PERSON (name char(20) , age1 int, age2 int)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Joao Pedro',10, 20)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Caio',null, 30)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Lucas',8, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Maria',null, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('carol',23, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('null',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('danilo',null, 27)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('felipe',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,23, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('indira',null, 29)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('felipe',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,23, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('caio',null, 25)");

      ResultSet rs = driver.executeQuery("Select name, age1, age2 from person order by name");
      assertTrue(rs.next());
      assertEquals("Caio", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("30", rs.getString("age2"));
      assertFalse(rs.isNull("name"));
      assertTrue(rs.isNull("age1"));
      assertFalse(rs.isNull("age2"));
      assertTrue(rs.next());
      assertEquals("Joao Pedro", rs.getString("name"));
      assertEquals("10", rs.getString("age1"));
      assertEquals("20", rs.getString("age2"));
      assertFalse(rs.isNull("name"));
      assertFalse(rs.isNull("age1"));
      assertFalse(rs.isNull("age2"));
      assertTrue(rs.next());
      assertEquals("Lucas", rs.getString("name"));
      assertEquals("8", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertFalse(rs.isNull("name"));
      assertFalse(rs.isNull("age1"));
      assertTrue(rs.isNull("age2"));
      assertTrue(rs.next());
      assertEquals("Maria", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertFalse(rs.isNull("name"));
      assertTrue(rs.isNull("age1"));
      assertTrue(rs.isNull("age2"));
      assertTrue(rs.next());
      assertEquals("caio", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("25", rs.getString("age2"));
      assertFalse(rs.isNull("name"));
      assertTrue(rs.isNull("age1"));
      assertFalse(rs.isNull("age2"));
      assertTrue(rs.next());
      assertEquals("carol", rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertFalse(rs.isNull("name"));
      assertFalse(rs.isNull("age1"));
      assertTrue(rs.isNull("age2"));
      rs.close();

      assertTrue((rs = driver.executeQuery("Select name, age1, age2 from person order by name desc, age1, age2")).next());
      assertNull(rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertTrue(rs.isNull(1));
      assertFalse(rs.isNull(2));
      assertTrue(rs.isNull(3));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertTrue(rs.isNull(1));
      assertFalse(rs.isNull(2));
      assertTrue(rs.isNull(3));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("12", rs.getString("age2"));
      assertTrue(rs.isNull(1));
      assertTrue(rs.isNull(2));
      assertFalse(rs.isNull(3));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("12", rs.getString("age2"));
      assertFalse(rs.isNull(1));
      assertTrue(rs.isNull(2));
      assertFalse(rs.isNull(3));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("12", rs.getString("age2"));
      assertFalse(rs.isNull(1));
      assertTrue(rs.isNull(2));
      assertFalse(rs.isNull(3));
      assertTrue(rs.next());
      assertEquals("null", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("12", rs.getString("age2"));
      assertFalse(rs.isNull(1));
      assertTrue(rs.isNull(2));
      assertFalse(rs.isNull(3));
      rs.close();
   }

   /**
    * Tests aggregation functions.
    */
   private void testAggregationFunctions() 
   {
      driver.executeUpdate("drop table person"); // Drops the table.

      // Creates and populates the table.
      driver.execute("create table PERSON (name char(20) , age1 int, age2 int)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('maria',10, 20)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('maria',null, 30)");

      ResultSet rs = driver.executeQuery("Select name, avg(age1) as a1, avg(age2) as a2 from person group by name");
      assertTrue(rs.next());
      assertEquals("10.0", rs.getString("a1"));
      assertEquals("25.0", rs.getString("a2"));
      rs.close();

      driver.executeUpdate("delete from person where age1 is null");
      driver.executeUpdate("insert into person(name, age1, age2) values ('maria',8, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('maria',null, null)");

      assertTrue((rs = driver.executeQuery("Select name, avg(age1) as a1, avg(age2) as a2 from person group by name")).next());
      assertEquals("9.0", rs.getString("a1"));
      assertEquals("20.0", rs.getString("a2"));
      rs.close();

      // Empties and re-populates the table again.
      driver.executeUpdate("delete from person");
      driver.executeUpdate("insert into person(name, age1, age2) values ('maria',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('maria',23, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato',null, 24)");

      assertTrue((rs = driver.executeQuery("Select name, avg(age1) as a1, avg(age2) as a2 from person group by name order by name")).next());
      assertEquals("23.0", rs.getString("a1"));
      assertEquals("12.0", rs.getString("a2"));
      assertTrue(rs.next());
      assertNull(rs.getString("a1"));
      assertEquals("18.0", rs.getString("a2"));
      rs.close();
      
      assertTrue((rs = driver.executeQuery("Select count(*) as av from person")).next());
      assertEquals("4", rs.getString("av"));
      rs.close();

      driver.executeUpdate("update person set age1 = -23 where age2 is null");
      assertTrue((rs = driver.executeQuery(("Select name, count(*) as a,  max(age1) as a1, min(age2) as a2 from person group by name"))).next());
      assertEquals("maria", rs.getString("name"));
      assertEquals("2", rs.getString("a"));
      assertEquals("-23", rs.getString("a1"));
      assertEquals("12", rs.getString("a2"));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertEquals("2", rs.getString("a"));
      assertNull(rs.getString("a1"));
      assertEquals("12", rs.getString("a2"));
      rs.close();

      // Re-creates the table and populates it.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (name char(20) , age1 char(20), age2 int)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Joao Pedro',null, 20)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,'23', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Joao Pedro',null, 30)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,'23', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Joao Pedro',null, 50)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,'23', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,'23', null)");

      assertTrue((rs = driver.executeQuery("Select name, age1, count(*) as cnt  from person group by name, age1")).next());
      assertEquals("Joao Pedro", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals(3, rs.getInt("cnt"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertEquals(4, rs.getInt("cnt"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      rs.close();

      driver.executeUpdate("delete from person where name is null or name is not null"); // Deletes all

      // Re-populates the table.
      driver.executeUpdate("insert into person(name, age1, age2) values ('Joao Pedro',null, 20)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('caio',null, 30)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Lucas','8', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('Maria',null, null)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('carol','23', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('null',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('danilo',null, 28)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('felipe',null, 16)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,'23', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato','85', 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato',null, 84)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('indira',null, 05)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('felipe',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('null',null, 10)");
      driver.executeUpdate("insert into person(name, age1, age2) values (null,'23', null)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('renato',null, 12)");
      driver.executeUpdate("insert into person(name, age1, age2) values ('caio',null, 24)");

      assertTrue((rs = driver.executeQuery("Select name, age1, count(*) as cnt  from person group by name, age1")).next());
      assertEquals("Joao Pedro", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals(1, rs.getInt("cnt"));
      assertTrue(rs.next());
      assertEquals("Lucas", rs.getString("name"));
      assertEquals("8", rs.getString("age1"));
      assertEquals(1, rs.getInt("cnt"));
      assertTrue(rs.next());
      assertEquals("Maria", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("caio", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("carol", rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("danilo", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("felipe", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("indira", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("null", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertEquals("85", rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      rs.close();

      // having
      assertTrue((rs = driver.executeQuery("Select name, age1, avg(age2) as cnt  from person group by name, age1 having cnt is not null")).next());
      assertEquals("Joao Pedro", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("20.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("caio", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("27.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("danilo", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("28.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("felipe", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("14.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("indira", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("5.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("null", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("11.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertEquals("85", rs.getString("age1"));
      assertEquals("12.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("48.0", rs.getString("cnt"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("12.0", rs.getString("cnt"));
      rs.close();

      assertTrue((rs = driver.executeQuery("Select name, age1, avg(age2) as cnt  from person group by name, age1 having cnt is null")).next());
      assertEquals("Lucas", rs.getString("name"));
      assertEquals("8", rs.getString("age1"));
      assertNull(rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("Maria", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertNull(rs.getString("cnt"));
      assertTrue(rs.next());
      assertEquals("carol", rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertNull(rs.getString("cnt"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertNull(rs.getString("cnt"));
      rs.close();
   }

   /**
    * Tests functions and null values.
    */
   private void testFunctionsAndNulls() 
   {
      driver.executeUpdate("drop table person"); // Drops the table.

      // Creates and populates the table.
      driver.execute("create table PERSON (name char(20) , age1 int, age2 int)");
      driver.executeUpdate("insert into person values ('Joao Pedro',null, 20)");
      driver.executeUpdate("insert into person values ('caio',null, 30)");
      driver.executeUpdate("insert into person values ('Lucas',8, null)");
      driver.executeUpdate("insert into person values ('Maria',null, null)");
      driver.executeUpdate("insert into person values (null,null, 12)");
      driver.executeUpdate("insert into person values ('carol','23', null)");
      driver.executeUpdate("insert into person values ('null',null, 12)");
      driver.executeUpdate("insert into person values ('danilo',null, 28)");
      driver.executeUpdate("insert into person values ('felipe',null, 16)");
      driver.executeUpdate("insert into person values (null,23, null)");
      driver.executeUpdate("insert into person values ('renato',85, 12)");
      driver.executeUpdate("insert into person values ('renato',null, 84)");
      driver.executeUpdate("insert into person values ('indira',null, 05)");
      driver.executeUpdate("insert into person values ('felipe',null, 12)");
      driver.executeUpdate("insert into person values ('null',null, 10)");
      driver.executeUpdate("insert into person values (null,23, null)");
      driver.executeUpdate("insert into person values ('renato',null, 12)");
      driver.executeUpdate("insert into person values ('caio',null, 24)");

      ResultSet rs = driver.executeQuery("Select name, age1, count(*) as cnt  from person group by name, age1");
      assertTrue(rs.next());
      assertEquals("Joao Pedro", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals(1, rs.getInt("cnt"));
      assertEquals("Joao Pedro\t\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("Lucas", rs.getString("name"));
      assertEquals("8", rs.getString("age1"));
      assertEquals(1, rs.getInt("cnt"));
      assertEquals("Lucas\t8\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("Maria", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertEquals("Maria\t\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("caio", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertEquals("caio\t\t2", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("carol", rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertEquals("carol\t23\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("danilo", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertEquals("danilo\t\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("felipe", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertEquals("felipe\t\t2", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("indira", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertEquals("indira\t\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("null", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertEquals("null\t\t2", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertEquals("85", rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertEquals("renato\t85\t1", rs.rowToString());
      assertTrue(rs.next());
      assertEquals("renato", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertEquals("renato\t\t2", rs.rowToString());
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("23", rs.getString("age1"));
      assertEquals("2", rs.getString("cnt"));
      assertEquals("\t23\t2", rs.rowToString());
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("1", rs.getString("cnt"));
      assertEquals("\t\t1", rs.rowToString());
      rs.close();
   }

   /**
    * Tests prepared statements.
    */
   private void testPreparedStatements() 
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); // Gets another connection.
      
      // Re-creates the table.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (name char(20) , age1 int, age2 int)");

      // Populates the table.
      PreparedStatement ps = driver.prepareStatement("insert into person(age1, age2, name) values (?,?,?)");
      ps.setNull(0);
      ps.setString(1, null);
      ps.setString(2, "Renato");
      ps.executeUpdate();
      ps.setInt(0, 12);
      ps.setString(1, null);
      ps.setNull(2);
      ps.executeUpdate();
      ps.setInt(0, 15);
      ps.setString(1, null);
      ps.setString(2, "Null");
      ps.executeUpdate();

      ResultSet rs = driver.executeQuery("Select name, age1, age2 from person order by name");
      assertTrue(rs.next());
      assertEquals("Null", rs.getString("name"));
      assertEquals("15", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertTrue(rs.next());
      assertEquals("Renato", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("12", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      rs.close();

      // Re-creates the table.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (name char(20) , age1 int , age2 int default 50, age3 int not null)");
      
      // Populates the table.
      (ps = driver.prepareStatement("insert into person( age1, age3, age2, name) values (?,?,?,?)")).setString(3,"Jo�o Pedro");
      ps.setString(0, "21");
      ps.setString(1, "156");
      ps.executeUpdate();
      ps.clearParameters();
      ps.setString(3, "Renato Novais");
      ps.setString(0, "25");
      ps.setString(2, null);
      ps.setString(1, "11");
      ps.executeUpdate();
      ps.clearParameters();
      ps.setString(3, "Indira");
      ps.setString(2, "12");
      ps.setString(1, "10");
      ps.executeUpdate();

      try // age3 can't be null.
      {
         ps.clearParameters();
         ps.setString(3, "caio");
         ps.setString(2, "100");
         ps.setString(1, null);
         ps.executeUpdate();
         fail("24");
      }
      catch (DriverException exception) {}

      try // age3 can't be null.
      {
         ps.clearParameters();
         ps.setString(3, "caio");
         ps.setString(2, "100");
         ps.executeUpdate();
         fail("25");
      }
      catch (DriverException exception) {}

      assertTrue((rs = driver.executeQuery("Select name, age1, age2, age3 from person")).next());
      assertEquals("Jo�o Pedro", rs.getString("name"));
      assertEquals("21", rs.getString("age1"));
      assertEquals("50", rs.getString("age2"));
      assertEquals("156", rs.getString("age3"));
      assertTrue(rs.next());
      assertEquals("Renato Novais", rs.getString("name"));
      assertEquals("25", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertEquals("11", rs.getString("age3"));
      assertTrue(rs.next());
      assertEquals("Indira", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("12", rs.getString("age2"));
      assertEquals("10", rs.getString("age3"));
      rs.close();

      (ps = driver.prepareStatement("update person set age1 = ?, name = ?, age2=?  where name = ? ")).clearParameters();
      ps.setString(1, "Caio");
      ps.setString(0, "26"); 
      ps.setString(2, null); 
      ps.setString(3, "Renato Novais"); 
      ps.executeUpdate();

      (ps = driver.prepareStatement("update person set name = ?, age1 = ? where name = ? ")).clearParameters();
      ps.setString(0, "Carolina");
      ps.setString(1, null);
      ps.setString(2, "Jo�o Pedro");
      ps.executeUpdate();

      (ps = driver.prepareStatement("update person set age1 = ?, age3 = ?, name = ?, age2 = ? where name = ? ")).clearParameters();
      ps.setString(0, "21");
      ps.setString(1, "23");
      ps.setNull(2);
      ps.setString(3, "22");
      ps.setString(4, "Indira");
      ps.executeUpdate();

      try // age3 can't be null.
      {
         ps.clearParameters();
         ps.setString(0, "21");
         ps.setString(1, null);
         ps.setString(2, null);
         ps.setString(3, "22");
         ps.setString(4, "Indira");
         ps.executeUpdate();
         fail("26");
      }
      catch (DriverException exception) {}

      assertTrue((rs = driver.executeQuery("Select name, age1, age2, age3 from person")).next());
      assertEquals("Carolina", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("50", rs.getString("age2"));
      assertEquals("156", rs.getString("age3"));
      assertTrue(rs.next());
      assertEquals("Caio", rs.getString("name"));
      assertEquals("26", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertEquals("11", rs.getString("age3"));
      assertTrue(rs.next());
      assertNull(rs.getString("name"));
      assertEquals("21", rs.getString("age1"));
      assertEquals("22", rs.getString("age2"));
      assertEquals("23", rs.getString("age3"));
      rs.close();

      (ps = driver.prepareStatement("update person set name = ? where name is null")).setString(0,"Danilo");
      ps.executeUpdate();
      ps.setString(0, "Felipe"); // No action, since name is not null.
      ps.executeUpdate();

      assertTrue((rs = driver.executeQuery("Select name, age1, age2, age3 from person")).next());
      assertEquals("Carolina", rs.getString("name"));
      assertNull(rs.getString("age1"));
      assertEquals("50", rs.getString("age2"));
      assertEquals("156", rs.getString("age3"));
      assertTrue(rs.next());
      assertEquals("Caio", rs.getString("name"));
      assertEquals("26", rs.getString("age1"));
      assertNull(rs.getString("age2"));
      assertEquals("11", rs.getString("age3"));
      assertTrue(rs.next());
      assertEquals("Danilo", rs.getString("name"));
      assertEquals("21", rs.getString("age1"));
      assertEquals("22", rs.getString("age2"));
      assertEquals("23", rs.getString("age3"));
      rs.close();
   }
   
   /**
    * Tests like with null in the table.
    */
   private void testLikeWithNull()
   {
      // Recreates the table.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (birth datetime default '1981/06/06')");
      
      // Populates the table with a null.
      PreparedStatement ps = driver.prepareStatement("INSERT INTO PERSON (birth) VALUES (?)");
      assertEquals(1, ps.executeUpdate());
      ps.setString(0, "1980/06/06");
      assertEquals(1, ps.executeUpdate());
      ps.setNull(0);
      assertEquals(1, ps.executeUpdate());
      
      // There can't be any rows in the result set.
      ResultSet rs = driver.executeQuery("select * from person where birth like '1981/06/06 %'");
      assertEquals(1, rs.getRowCount());
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select * from person where birth like '1980/06/06 %'")).getRowCount());
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select * from person where birth is null")).getRowCount());
      rs.close();
      
      assertEquals(2, (rs = driver.executeQuery("select * from person where birth is not null")).getRowCount());
      rs.close();
      
      // Repeats the test using an index.
      driver.execute("create index idx on person(birth)");
      assertEquals(1, (rs = driver.executeQuery("select * from person where birth like '1980/06/06 %'")).getRowCount());
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select * from person where birth is null")).getRowCount());
      rs.close();
      
      assertEquals(2, (rs = driver.executeQuery("select * from person where birth is not null")).getRowCount());
      rs.close();
      
      // Can't accept = null or != null.
      try
      {
         driver.executeQuery("select * from person where birth != null");
         fail("27");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from person where birth = null");
         fail("28");
      }
      catch (SQLParseException exception) {}
   }

}
