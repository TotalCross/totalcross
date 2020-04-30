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

/**
 * This test verifies the joins on Litebase.
 */
public class TestJoin extends TestCase
{
   LitebaseConnection driver;
   
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      driver = AllTests.getInstance("Test"); // First creates the connection with Litebase.
      testJoinSintax(); // Tests the join sintax.
      testSimpleJoins(); // Tests simple joins.
      testComparisons(); // Tests comparison between fields, including null values and functions.
      test3TablesJoin(); // Tests joins with 3 tables.
      testRSColName(); // Tests ResultSet getting fields by column name.
      testJoinWithIndex(); // Tests join with index.
      testComparisonsWithIndices(); // Tests comparison between fields, using index.
      testPrimaryKeyAndOrdering(); // Tests join with primary key, clause and table orders.
      testComparisonInTheSameTable(); // Tests comparison of columns of the same table.
      testOrWithFalseConstantComparison(); // Tests join with or and false comparisons with constants.
      testOrderGroupBy(); // Tests join with order and group by.
      driver.closeAll();
   }

   /**
    * Drops an especific table if it exists
    * 
    * @param name The table name.
    */
   private void dropTableIfExist(String name)
   {
      if (driver.exists(name))
         driver.executeUpdate("drop table " + name);
   }

   /**
    * Runs Tests queries that have parser errors.
    * 
    * @param sql The sql command with parser errors.
    */
   private void runSelectWithParserError(String sql)
   {
      try
      {
         driver.executeQuery(sql);
         fail();
      }
      catch (SQLParseException exception) {}

   }
   
   /**
    * Tests the join sintax. All sql commands with parser error must throw an exception.
    */
   private void testJoinSintax()
   {
      // Drop tables.
      dropTableIfExist("person");
      dropTableIfExist("course");

      // Create tables.
      driver.execute("create table person(person_id int primary key, name char(20) default 'Est� sem nome: �^~' not null, age int default 10, " 
                                                                  + "birth Date default '1981/06/06', years DateTime not null)");
      driver.execute("create table course (name char(20) primary key, person_id int, address char(20))");

      runSelectWithParserError("select * from person p where person.person_id = 1"); // The alias must be used in the where clause.
      runSelectWithParserError("select p.name, person.age from person p"); // The alias must be used if the table has it.
      
      // The alias must be used in the order by clause.
      runSelectWithParserError("select max(t.person_id) as maxname from course t where t.name = '1' order by course.name "); 
      
      // The alias must be used in the group by clause.
      runSelectWithParserError("select max(t.person_id) as maxname from course t where t.name = '1' group by course.name "); 
      
      // An alias can't be used in the where clause.
      runSelectWithParserError("select max(t.person_id) as maxname, t.name as tn from course t where tn = '1' group by t.name " 
                                                                                            + "having maxname = 'sem nome'"); 
      
      runSelectWithParserError("select p.name, p.age from person p where p.xxxx = 1"); // Unknown column xxxx.
      runSelectWithParserError("select name from person, course"); // Ambigous column name.
      runSelectWithParserError("select name as v, person_id as v from person, course"); // Repeated alias.
      runSelectWithParserError("SELECT * FROM person course, course"); // person can't use course as alias without giving an alias to course.
      runSelectWithParserError("SELECT course.address FROM person course, course person"); // Course is an alias for person.
      
      // person is an alias for course.
      runSelectWithParserError("SELECT * FROM person course, course person where person.address = '' and person.age = 9"); 
      
      runSelectWithParserError("SELECT * FROM course, person order by year"); // order by field doesn't exist.

      // c.name is not in the select clause.
      runSelectWithParserError("select max(c.person_id) as maxname, p.name, p.birth, c.address, p.person_id from course c, person p " 
                                                                                             + "where c.name = '1' order by c.name");
      
      try // Select sql without sintax errors.
      {
         runSelectWithParserError("SELECT distinct * FROM person course, course person where course.age = 10 and person.address is not null");
         fail();
      } 
      catch (AssertionFailedError error) {}
   }

   /**
    * Tests simple joins.
    */
   private void testSimpleJoins() 
   {
      // Drops tables.
      driver.executeUpdate("drop table perSon");
      dropTableIfExist("peRson2");
      
      // Creates tables.
      driver.execute("create table person(name char(5), age int, years int)");
      driver.execute("create table person2(name2 char(5), age2 int)");
     
      // Populates tables.
      driver.executeUpdate("Insert into person values ('RLN', 20, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 20, 2)");
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");
      
      ResultSet rs = driver.executeQuery("select name, name2, age, age2 from person, person2 where 'RLN' = person.name order by name2");
      assertNotNull(rs);
      assertEquals(2, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person, person2  where age2 = 40 and name = 'RLN'"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person, person2  where name = 'RLN' and person2.age2 = 40 "));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person, person2  where person.name = 'RLN' or person2.age2 = 40 "));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person, person2  where age2 = 40 or person.name = 'RLN'"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();
      
      assertNotNull(rs = driver.executeQuery("select name, name2 from person, person2 where person.name = person2.name2 and (person.name = 'RLN' or person2.name2 = 'RLN')"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      rs.close();
      
       // Tests the boolean operator NOT.
      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person, person2  where not age2 <> 40 or not person.name <> 'RLN'"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();

      // Tests the boolean operator NOT.
      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from  person,person2 where not years = 1 or not age2 = 30 " 
                                                                                                                     + "order by age2"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();

      // Tests comparison between fields.
      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person2,person where name = person2.name2"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      rs.close();
   }

   /**
    * Tests comparison between fields, including null values and functions.
    */
   private void testComparisons()
   {
      // Drop tables.       
      driver.executeUpdate("drop table perSon");
      driver.executeUpdate("drop table perSon2");

      // Create tables.
      driver.execute("create table person(name char(5), age int, years int)");
      driver.execute("create table person2(name2 char(5), age2 int)");
      
      // Creates the index.
      driver.execute("create index idx on person(name)");
      
      // Populates the tables.
      driver.executeUpdate("Insert into person values ('RLN', 20, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 30, 2)");
      driver.executeUpdate("Insert INTO person(name) values ('IND')");
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");
      driver.executeUpdate("Insert INTO person2 values ('rln', 40)");
      driver.executeUpdate("Insert INTO person2(name2) values ('IND2')");

      ResultSet rs  = driver.executeQuery("select name, name2, age, age2 from person2, person where lower(person.name) = person2.name2"); 
      assertNotNull(rs);
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person2, person where person.age = person2.age2"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      rs.close();

      driver.executeUpdate("update person2 p set p.age2 = 30 where name2 = 'rln'");
      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person2, person where person.age = person2.age2 or name = name2"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, name2, age, age2 from person2, person where person.age = person2.age2 and name = name2"));
      assertEquals(0, rs.getRowCount());
      rs.close();
      
      // Empties tables.
      driver.executeUpdate("delete from person");
      driver.executeUpdate("delete from person2");
      
      // Populates person.
      driver.executeUpdate("Insert into person values ('RLN', 40, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 20, 2)");
      driver.executeUpdate("Insert INTO person(name) values ('IND')");
      driver.executeUpdate("Insert INTO person values ('RLN', 10, 4)");
      driver.executeUpdate("Insert INTO person values ('IND', 80, null)");
      driver.executeUpdate("Insert INTO person values ('IND', 100, 5)");
      
      // Populates person2.
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");
      driver.executeUpdate("Insert INTO person2(name2) values ('IND2')");

      assertNotNull(rs = driver.executeQuery("select * from person2, person"));
      assertEquals(18, rs.getRowCount());
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person2, person where age is null and years is null and age2 is null"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("IND", rs.getString("name"));
      assertEquals("IND2", rs.getString("name2"));
      assertNull(rs.getString("years"));
      assertNull(rs.getString("age"));
      assertNull(rs.getString("age2"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select count(*) as tt from person, person2"));
      assertTrue(rs.next());
      assertEquals(18, rs.getInt(1));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select count(*) as tt from person, person2 where name = 'IND'"));
      assertTrue(rs.next());
      assertEquals(9, rs.getInt("tt"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, count(*) as tt from person, person2 group by name"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("IND", rs.getString("name"));
      assertEquals(9, rs.getInt("tt"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals(3, rs.getInt("tt"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals(6, rs.getInt("tt"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select name, count(*) as tt from person, person2 group by name having tt > 3"));
      assertEquals(2, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("IND", rs.getString("name"));
      assertEquals(9, rs.getInt("tt"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals(6, rs.getInt("tt"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select age2, lower(name) as tt, age, upper(name2) as rr from person, person2 where name = name2 " 
                                                                                                                        + "order by age"));
      assertEquals(2, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("rln", rs.getString("tt"));
      assertEquals("RLN", rs.getString("rr"));
      assertEquals(10, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertTrue(rs.next());
      assertEquals("rln", rs.getString("tt"));
      assertEquals("RLN", rs.getString("rr"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      rs.close();
   }

   /**
    * Tests joins with 3 tables.
    */
   private void test3TablesJoin() 
   {
      // Drops Tables.
      driver.executeUpdate("drop table perSon");
      driver.executeUpdate("drop table perSon2");
      dropTableIfExist("peRson3");

      // Creates tables.
      driver.execute("create table person(name char(5), age int, years int)");
      driver.execute("create table person2(name2 char(5), age2 int)");
      driver.execute("create table person3(name3 char(5), age3 int, years3 int)");
      
      // Creates indices.
      driver.execute("create index idx on person(name)");
      driver.execute("create index idx on person2(age2)");
      driver.execute("create index idx on person3(years3)");
      
      // Populates person.
      driver.executeUpdate("Insert into person values ('RLN', 40, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 20, 2)");
      driver.executeUpdate("Insert INTO person(name) values ('IND')");
      driver.executeUpdate("Insert INTO person values ('RLN', 10, 4)");
      driver.executeUpdate("Insert INTO person values ('IND', 80, null)");
      driver.executeUpdate("Insert INTO person values ('IND', 100, 5)");
      
      // Populates person 2.
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");
      driver.executeUpdate("Insert INTO person2(name2) values ('IND2')");
      
      // Populates person 3.
      driver.executeUpdate("INSERT into person3 values ('ZN', 60, 25)");
      driver.executeUpdate("Insert INTO person3 values ('JN', 25, 60)");
      driver.executeUpdate("INSERT into person3 values ('IND', 20, 1)");
      driver.executeUpdate("Insert INTO person3 values ('IND', 30, 2)");
      driver.executeUpdate("INSERT INTO person3(name3) values ('RLN')");
      driver.executeUpdate("Insert INTO person3(years3, name3) values (50,'RLN')");
      driver.executeUpdate("INSERT into person3(name3, years3) values ('ZN', 60)");
      driver.executeUpdate("INSERT INTO person3(name3, age3) values ('JN', 90)");
      driver.executeUpdate("INSERT into person3 values ('ZN', 30, 25)");
      driver.executeUpdate("INSERT INTO person3 values ('JN', 2, 60)");
      driver.executeUpdate("INSERT into person3 values ('RLN', 1, 100)");
      driver.executeUpdate("Insert INTO person3 values ('IOG', 40, 2)");
      driver.executeUpdate("Insert INTO person3(name3) values ('IOG')");
      driver.executeUpdate("Insert INTO person3(years3, name3) values (1,'RLN')");
      driver.executeUpdate("Insert into person3(name3, years3) values ('ZN', 50)");
      driver.executeUpdate("Insert INTO person3(name3, age3) values ('JN', 75)");

      ResultSet rs = driver.executeQuery("select * from person, person2, person3");
      assertNotNull(rs);
      assertEquals(288, rs.getRowCount());
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2, person3 where name = name2"));
      assertEquals(32, rs.getRowCount());
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2, person3 where name = name2 and person3.age3 is null"));
      assertEquals(12, rs.getRowCount());
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2, person3 where name = name2 and person3.age3 is null and name3 = 'IOG' " 
                                                                                                                              + "and age = 10"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals("IOG", rs.getString("name3"));
      assertEquals(10, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertNull(rs.getString("age3"));
      assertEquals(4, rs.getInt("years"));
      assertNull(rs.getString("years3"));
      rs.close();
   }

   /**
    * Tests ResultSet getting fields by column name.
    */
   private void testRSColName()
   {
      // Drop tables.
      driver.executeUpdate("drop table perSon");
      driver.executeUpdate("drop table perSon2");

      // Create tables.
      driver.execute("create table person(name char(5), age int, years int)");
      driver.execute("create table person2(name char(5), age2 int)");
      
      // Populates table person.
      driver.executeUpdate("Insert into person values ('RLN', 20, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 30, 2)");
      
      // Populates table person2.
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");

      ResultSet rs = driver.executeQuery("select * from person, person2");
      assertEquals(4, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString(1));
      assertEquals("20", rs.getString(2));
      assertEquals("1", rs.getString(3));
      assertEquals("RLN", rs.getString(4));
      assertEquals("30", rs.getString(5));
      assertEquals("RLN", rs.getString("person.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("20", rs.getString("person.age"));
      assertEquals("20", rs.getString("age"));
      assertEquals("1", rs.getString("person.years"));
      assertEquals("1", rs.getString("years"));
      assertEquals("RLN", rs.getString("person2.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("30", rs.getString("person2.age2"));
      assertEquals("30", rs.getString("age2"));

      assertTrue(rs.next());
      assertEquals("RLN", rs.getString(1));
      assertEquals("20", rs.getString(2));
      assertEquals("1", rs.getString(3));
      assertEquals("IAPU", rs.getString(4));
      assertEquals("40", rs.getString(5));
      assertEquals("RLN", rs.getString("person.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("20", rs.getString("person.age"));
      assertEquals("20", rs.getString("age"));
      assertEquals("1", rs.getString("person.years"));
      assertEquals("1", rs.getString("years"));
      assertEquals("IAPU", rs.getString("person2.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("40", rs.getString("person2.age2"));
      assertEquals("40", rs.getString("age2"));

      assertTrue(rs.next());
      assertEquals("IOG", rs.getString(1));
      assertEquals("30", rs.getString(2));
      assertEquals("2", rs.getString(3));
      assertEquals("RLN", rs.getString(4));
      assertEquals("30", rs.getString(5));
      assertEquals("IOG", rs.getString("person.name"));
      assertEquals("IOG", rs.getString("name"));
      assertEquals("30", rs.getString("person.age"));
      assertEquals("30", rs.getString("age"));
      assertEquals("2", rs.getString("person.years"));
      assertEquals("2", rs.getString("years"));
      assertEquals("RLN", rs.getString("person2.name"));
      assertEquals("IOG", rs.getString("name"));
      assertEquals("30", rs.getString("person2.age2"));
      assertEquals("30", rs.getString("age2"));

      assertTrue(rs.next());
      assertEquals("IOG", rs.getString(1));
      assertEquals("30", rs.getString(2));
      assertEquals("2", rs.getString(3));
      assertEquals("IAPU", rs.getString(4));
      assertEquals("40", rs.getString(5));
      assertEquals("IOG", rs.getString("person.name"));
      assertEquals("IOG", rs.getString("name"));
      assertEquals("30", rs.getString("person.age"));
      assertEquals("30", rs.getString("age"));
      assertEquals("2", rs.getString("person.years"));
      assertEquals("2", rs.getString("years"));
      assertEquals("IAPU", rs.getString("person2.name"));
      assertEquals("IOG", rs.getString("name"));
      assertEquals("40", rs.getString("person2.age2"));
      assertEquals("40", rs.getString("age2"));
      rs.close();

      assertEquals(4, (rs = driver.executeQuery("select person.name as tt, age, years as bb, person2.name, person2.age2 from person, person2"))
                                  .getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString(1));
      assertEquals("20", rs.getString(2));
      assertEquals("1", rs.getString(3));
      assertEquals("RLN", rs.getString(4));
      assertEquals("30", rs.getString(5));
      assertEquals("RLN", rs.getString("person.tt"));
      assertEquals("RLN", rs.getString("tt"));
      assertEquals("20", rs.getString("person.age"));
      assertEquals("20", rs.getString("age"));
      assertEquals("1", rs.getString("person.bb"));
      assertEquals("1", rs.getString("bb"));
      assertEquals("RLN", rs.getString("person2.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("30", rs.getString("person2.age2"));
      assertEquals("30", rs.getString("age2"));

      assertTrue(rs.next());
      assertEquals("RLN", rs.getString(1));
      assertEquals("20", rs.getString(2));
      assertEquals("1", rs.getString(3));
      assertEquals("IAPU", rs.getString(4));
      assertEquals("40", rs.getString(5));
      assertEquals("RLN", rs.getString("person.tt"));
      assertEquals("RLN", rs.getString("tt"));
      assertEquals("20", rs.getString("person.age"));
      assertEquals("20", rs.getString("age"));
      assertEquals("1", rs.getString("person.bb"));
      assertEquals("1", rs.getString("bb"));
      assertEquals("IAPU", rs.getString("person2.name"));
      assertEquals("IAPU", rs.getString("name"));
      assertEquals("40", rs.getString("person2.age2"));
      assertEquals("40", rs.getString("age2"));

      assertTrue(rs.next());
      assertEquals("IOG", rs.getString(1));
      assertEquals("30", rs.getString(2));
      assertEquals("2", rs.getString(3));
      assertEquals("RLN", rs.getString(4));
      assertEquals("30", rs.getString(5));
      assertEquals("IOG", rs.getString("person.tt"));
      assertEquals("IOG", rs.getString("tt"));
      assertEquals("30", rs.getString("person.age"));
      assertEquals("30", rs.getString("age"));
      assertEquals("2", rs.getString("person.bb"));
      assertEquals("2", rs.getString("bb"));
      assertEquals("RLN", rs.getString("person2.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("30", rs.getString("person2.age2"));
      assertEquals("30", rs.getString("age2"));

      assertTrue(rs.next());
      assertEquals("IOG", rs.getString(1));
      assertEquals("30", rs.getString(2));
      assertEquals("2", rs.getString(3));
      assertEquals("IAPU", rs.getString(4));
      assertEquals("40", rs.getString(5));
      assertEquals("IOG", rs.getString("person.tt"));
      assertEquals("IOG", rs.getString("tt"));
      assertEquals("30", rs.getString("person.age"));
      assertEquals("30", rs.getString("age"));
      assertEquals("2", rs.getString("person.bb"));
      assertEquals("2", rs.getString("bb"));
      assertEquals("IAPU", rs.getString("person2.name"));
      assertEquals("IAPU", rs.getString("name"));
      assertEquals("40", rs.getString("person2.age2"));
      assertEquals("40", rs.getString("age2"));
      rs.close();

      assertEquals((rs = driver.executeQuery("select person.name, person2.name from person, person2")).getRowCount(),4);
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString(1));
      assertEquals("RLN", rs.getString(2));
      assertEquals("RLN", rs.getString("person.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("person2.name"));
      assertEquals("RLN", rs.getString("name"));

      assertTrue(rs.next());
      assertEquals("RLN", rs.getString(1));
      assertEquals("IAPU", rs.getString(2));
      assertEquals("RLN", rs.getString("person.name"));
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("person2.name"));
      assertEquals("RLN", rs.getString("name"));

      assertTrue(rs.next());
      assertEquals("IOG", rs.getString(1));
      assertEquals("RLN", rs.getString(2));
      assertEquals("IOG", rs.getString("person.name"));
      assertEquals("IOG", rs.getString("name"));
      assertEquals("RLN", rs.getString("person2.name"));
      assertEquals("IOG", rs.getString("name"));

      assertTrue(rs.next());
      assertEquals("IOG", rs.getString(1));
      assertEquals("IAPU", rs.getString(2));
      assertEquals("IOG", rs.getString("person.name"));
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IAPU", rs.getString("person2.name"));
      assertEquals("IOG", rs.getString("name"));
      rs.close();
   }

   /**
    * Tests join with index.
    */
   private void testJoinWithIndex() 
   {
      // Drops tables.
      driver.executeUpdate("drop table perSon");
      driver.executeUpdate("drop table perSon2");

      // Creates tables.
      driver.execute("create table person(name char(5), age int, years int)");
      driver.execute("create table person2(name2 char(5), age2 int)");
      
      driver.execute("create index idx on person(name)"); // Creates index for column person.name.
      
      testIndexOnAge(); // Tests without index on person.age.
      driver.execute("create index idx on person(age)"); // Creates the index.
      testIndexOnAge(); // Tests with index on person.age.

      testIndexOnName2(); // Tests without index on person2.name,
      driver.execute("create index idx on person2(name2)"); // Creates the index.
      testIndexOnName2(); // Tests with index on pers
   }

   /**
    * Tests joins with and without index on person.age.
    */
   private void testIndexOnAge()
   {
      // Empties tables.
      driver.executeUpdate("delete from person");
      driver.executeUpdate("delete from person2");
 
      // Populates table person.
      driver.executeUpdate("Insert into person values ('RLN', 20, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 30, 2)");
      driver.executeUpdate("Insert into person values ('RLN2', 40, 3)");

      // Populates table person2.
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");
      driver.executeUpdate("Insert INTO person2 values ('rln', 30)");

      ResultSet rs = driver.executeQuery("select * from person,person2 where name = 'RLN'");
      assertNotNull(rs);
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      rs.close();

      driver.executeUpdate("update person set name = 'RLN' where age = 40");
      assertNotNull(rs = driver.executeQuery("select * from person,person2 where name = 'RLN' and age = 40"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();

      driver.executeUpdate("update person set name = 'RLN2' where age = 40");
      assertNotNull(rs = driver.executeQuery("select * from person,person2 where name = 'RLN' or age = 40"));
      assertEquals(6, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person,person2 where name = 'RLN2' and age = 40"));
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();
   }
   
   /**
    * Tests joins with and without index on person2.name2.
    */
   private void testIndexOnName2()
   {
      ResultSet rs = driver.executeQuery("select * from person, person2 where name = 'RLN' and name2 = 'rln'"); 
      assertNotNull(rs);
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      rs.close();
      
      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name2 = 'rln' and name = 'RLN'"));
      assertEquals(rs.getRowCount(),1);
      assertTrue(rs.next());
      assertEquals(rs.getString("name"),"RLN");
      assertEquals(rs.getString("name2"),"rln");
      assertEquals(rs.getInt("age"),20);
      assertEquals(rs.getInt("age2"),30);
      assertEquals(rs.getInt("years"),1);
      rs.close();
      
      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name = 'RLN' or name2 = 'rln'"));
      assertEquals(5, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(2, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();
      
      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name2 = 'rln' or name = 'RLN'"));
      assertEquals(5, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(2, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2 where (name = 'RLN' or name2 = 'rln') or name = 'RLN2'"));
      assertEquals(7, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(2, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("IAPU", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(40, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2 where (name = 'RLN' or name2 = 'rln') and name = 'RLN2'"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name = 'RLN2' and (name = 'RLN' or name2 = 'rln')" ));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN2", rs.getString("name"));
      assertEquals("rln", rs.getString("name2"));
      assertEquals(40, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(3, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name = name2"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      rs.close();
   }

   /**
    * Tests comparison between fields, using index.
    */
   private void testComparisonsWithIndices()
   {
      // Drops tables.
      driver.executeUpdate("drop table perSon");
      driver.executeUpdate("drop table perSon2");

      // Creates tables.
      driver.execute("create table peRson(name char(5), age int, years int)");
      driver.execute("create table pErson2(name2 char(5), age2 int)");
      
      // Populates person.
      driver.executeUpdate("Insert into person values ('RLN', 20, 1)");
      driver.executeUpdate("Insert INTO person values ('IOG', 30, 2)");
      driver.executeUpdate("Insert into person values ('RLN2', 40, 3)");

      // Populates person2.
      driver.executeUpdate("Insert into person2 values ('RLN', 30)");
      driver.executeUpdate("Insert INTO person2 values ('IAPU', 40)");
      driver.executeUpdate("Insert INTO person2 values ('rln', 30)");
      driver.executeUpdate("Insert INTO person2 values ('RLN', 60)");
      driver.executeUpdate("Insert INTO person2 values ('IOG', 70)");

      // Creates the indices.
      driver.execute("create index idx on person(name)");
      driver.execute("create index idx on person2(name2)");
      driver.execute("create index idx on person(age)");
      driver.execute("create index idx on person2(age2)");

      ResultSet rs = driver.executeQuery("select * from person, person2 where name = name2");
      assertNotNull(rs);
      assertEquals(3, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(60, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IOG", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(70, rs.getInt("age2"));
      assertEquals(2, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name = name2 and name = 'IOG'"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("IOG", rs.getString("name"));
      assertEquals("IOG", rs.getString("name2"));
      assertEquals(30, rs.getInt("age"));
      assertEquals(70, rs.getInt("age2"));
      assertEquals(2, rs.getInt("years"));
      rs.close();

      driver.executeUpdate("update person2 set age2 = 20 where age2 = 60");
      assertNotNull(rs = driver.executeQuery("select * from person, person2 where name = name2 and age = age2"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(20, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      rs.close();

      assertNotNull(rs = driver.executeQuery("select * from person, person2 where age = age2 and name = name2"));
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(20, rs.getInt("age2"));
      assertEquals(1, rs.getInt("years"));
      rs.close();

      // Creates and populates person3.
      driver.executeUpdate("drop table perSon3");
      driver.execute("create table person3(name3 char(5), age3 int, years3 int)");
      driver.executeUpdate("INSERT into person3 values ('ZN', 60, 25)");
      driver.executeUpdate("Insert INTO person3 values ('JN', 25, 60)");
      driver.executeUpdate("INSERT into person3 values ('IND', 20, 1)");
      driver.executeUpdate("INSERT into person3 values ('RLN', 1, 100)");
      driver.executeUpdate("Insert INTO person3 values ('IND', 30, 2)");
      driver.executeUpdate("Insert INTO person3 values ('RLN2', 30, 2)");
      driver.execute("create index idx on person3(name3)");

      // Tests the same join bu with different table ordering.
      testTableOrder("select * from person, person2, person3 where name = name2 and name = name3");
      testTableOrder("select * from person, person3, person2 where name = name2 and name = name3");
      testTableOrder("select * from person2, person3, person where name = name2 and name = name3");
      testTableOrder("select * from person2, person, person3 where name = name2 and name = name3");
      testTableOrder("select * from person3, person2, person where name = name2 and name = name3");
      testTableOrder("select * from person3, person, person2 where name = name2 and name = name3");
   }

   /**
    * Tests the same join bu with different table ordering.
    * 
    * @param sql The sql select command of the join.
    */
   private void testTableOrder(String sql)
   {
      ResultSet rs = driver.executeQuery(sql);
      assertNotNull(rs);
      assertEquals(2, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals("RLN", rs.getString("name3"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(30, rs.getInt("age2"));
      assertEquals(1, rs.getInt("age3"));
      assertEquals(1, rs.getInt("years"));
      assertEquals(100, rs.getInt("years3"));
      assertTrue(rs.next());
      assertEquals("RLN", rs.getString("name"));
      assertEquals("RLN", rs.getString("name2"));
      assertEquals("RLN", rs.getString("name3"));
      assertEquals(20, rs.getInt("age"));
      assertEquals(20, rs.getInt("age2"));
      assertEquals(1, rs.getInt("age3"));
      assertEquals(1, rs.getInt("years"));
      assertEquals(100, rs.getInt("years3"));
      rs.close();
   }
   
   /**
    * Tests join with primary key, clause and table orders.
    */
   private void testPrimaryKeyAndOrdering()
   {
      // Drop tables.
      driver.executeUpdate("drop table perSon");
      driver.executeUpdate("drop table Course");
      
      // Creates tables.
      driver.execute("create table person (id long primary key, lastname char(20), firstname char(20))");
      driver.execute("create table course (id long primary key, personid long, cname char(20))");
      
      // Populates the tables
      PreparedStatement ptInsert = driver.prepareStatement("insert into person (id,lastname,firstname) values (?,?,?)");
      PreparedStatement acctInsert = driver.prepareStatement("insert into course (id,personid,cname) values (?,?,?)");
      int i = 101;
      while (--i > 0)
      {
          ptInsert.setLong(0, i);
          ptInsert.setString(1, "Last" + i);
          ptInsert.setString(2, "First" + i);
          ptInsert.executeUpdate();

          acctInsert.setLong(0, i + 100);
          acctInsert.setLong(1, i);
          acctInsert.setString(2, "Course" + i);
          acctInsert.executeUpdate();
      }
      
      // There is only one person with id 50.
      ResultSet rs = driver.executeQuery("select person.id from person where person.id = 50");
      assertEquals(1, rs.getRowCount());
      rs.close();
      
      // All the joins must return only one record because only one person has id 50.
      assertEquals(1, (rs = driver.executeQuery("select course.id from course where course.personid = 50")).getRowCount());
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from person,course where person.id = course.personid and person.id = 50")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from course, person where person.id = course.personid and person.id = 50")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from person,course where course.personid = person.id and person.id = 50")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from course, person where course.personid = person.id and person.id = 50")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from person,course where person.id = 50 and course.personid = person.id")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from course, person where person.id = 50 and course.personid = person.id")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from person,course where person.id = 50 and course.personid = person.id")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
      
      assertEquals(1, (rs = driver.executeQuery("select person.id, course.id from course, person where person.id = 50 and course.personid = person.id")).getRowCount());
      rs.next();
      assertEquals(50, rs.getLong(1));
      assertEquals(150, rs.getLong(2));
      rs.close();
   }
   
   /**
    * Tests comparison of columns of the same table.
    */
   void testComparisonInTheSameTable()
   {
      LitebaseConnection driverAux = driver;
      
      if (driverAux.exists("M_META_GRUPO_MATERIAL"))
         driverAux.executeUpdate("drop table M_META_GRUPO_MATERIAL");
      driverAux.execute("create table M_META_GRUPO_MATERIAL (GMF_CODIGO long NOT NULL, QTD_META double, QTD_CONSUMIDA double)");
      driverAux.executeUpdate("insert into M_META_GRUPO_MATERIAL values (1, 500.6578844, 0)");
      driverAux.executeUpdate("insert into M_META_GRUPO_MATERIAL values (1, 350.50, 350.50)");
      
      ResultSet resultSet = driver.executeQuery("select * from M_META_GRUPO_MATERIAL MGM where MGM.QTD_CONSUMIDA = MGM.QTD_META");
      assertEquals(1, resultSet.getRowCount());
      resultSet.close();
      
      if (driverAux.exists("M_MATERIAL"))
         driverAux.executeUpdate("drop table M_MATERIAL");
      driverAux.execute("create table M_MATERIAL (CODIGO_MATERIAL int NOT NULL, GMF_CODIGO long, primary key(CODIGO_MATERIAL))");
      
      driverAux.executeUpdate("insert into M_MATERIAL values (1, 1)");
      driverAux.executeUpdate("insert into M_MATERIAL values (2, 1)");
      
      assertEquals(2, (resultSet = driverAux.executeQuery("select * from M_MATERIAL MM, M_META_GRUPO_MATERIAL MGM where MM.GMF_CODIGO=MGM.GMF_CODIGO and MM.CODIGO_MATERIAL=1")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driverAux.executeQuery("select * from M_MATERIAL MM, M_META_GRUPO_MATERIAL MGM where MM.GMF_CODIGO=MGM.GMF_CODIGO and MM.CODIGO_MATERIAL=1 and MGM.QTD_CONSUMIDA = MGM.QTD_META")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driverAux.executeQuery("select * from M_MATERIAL MM, M_META_GRUPO_MATERIAL MGM where MM.GMF_CODIGO=MGM.GMF_CODIGO and MM.CODIGO_MATERIAL=1 and MGM.QTD_CONSUMIDA = 350.5")).getRowCount());
      resultSet.close();
   }
   
   /**
    * Tests join with or and false comparisons with constants.
    */
   private void testOrWithFalseConstantComparison()
   {
      LitebaseConnection driverAux = driver;
      
      // Drops existing tables.
      if (driverAux.exists("tbpedido"))
         driverAux.executeUpdate("drop table tbpedido");
      if (driverAux.exists("tbcliente"))
         driverAux.executeUpdate("drop table tbcliente");
      
      // Creates the tables.
      driverAux.execute("create table tbpedido (cdpedido short, totalpedido double, cdcliente short)");
      driverAux.execute("create table tbcliente (cdcliente short, nmcliente char(10), flativo char(1))");
      
      // Populates the tables.
      driverAux.executeUpdate("insert into tbpedido values (1, 11.30, 1)");
      driverAux.executeUpdate("insert into tbpedido values (2, 19.30, 1)");  
      driverAux.executeUpdate("insert into tbpedido values (3, 17.20, 2)"); 
      driverAux.executeUpdate("insert into tbpedido values (4, 50.00, 2)");  
      driverAux.executeUpdate("insert into tbpedido values (5, 45.70, 3)");  
      driverAux.executeUpdate("insert into tbpedido values (6, 78.00, 4)");  
      driverAux.executeUpdate("insert into tbpedido values (7, 79.10, 5)");  
      driverAux.executeUpdate("insert into tbpedido values (8, 80.00, 3)");  
      driverAux.executeUpdate("insert into tbpedido values (9, 2.50, 4)");
      
      driverAux.executeUpdate("insert into tbcliente values (1, 'Jos�', 'S')");  
      driverAux.executeUpdate("insert into tbcliente values (2, 'Maria', 'S')");  
      driverAux.executeUpdate("insert into tbcliente values (3, 'Jo�o', 'S')");  
      driverAux.executeUpdate("insert into tbcliente values (4, 'Antonio', 'S')");  
      driverAux.executeUpdate("insert into tbcliente values (5, 'Lucia', 'S')");

      // Normal select.
      ResultSet resultSet 
              = driverAux.executeQuery("select * from tbpedido ped, tbcliente cli where cli.cdcliente = ped.cdcliente and (ped.cdcliente = 1)");
      assertEquals(2, resultSet.getRowCount());
      resultSet.close();
      
      // Selects with false constant comparisons.
      assertEquals(2, (resultSet = driverAux.executeQuery("select * from tbpedido ped, tbcliente cli where cli.cdcliente = ped.cdcliente and " 
                                                        + "(ped.cdcliente = 1 or '1' = '0')")).getRowCount());
      resultSet.close();
      
      assertEquals(2, (resultSet = driverAux.executeQuery("select * from tbpedido ped, tbcliente cli where cli.cdcliente = ped.cdcliente and " 
                                                        + "('1' = '0' or ped.cdcliente = 1)")).getRowCount());
      resultSet.close();
      
      assertEquals(2, (resultSet = driverAux.executeQuery("select * from tbpedido ped, tbcliente cli where (ped.cdcliente = 1 or '1' = '0') and " 
                                                        + "cli.cdcliente = ped.cdcliente")).getRowCount());
      resultSet.close();

      assertEquals(2, (resultSet = driverAux.executeQuery("select * from tbpedido ped, tbcliente cli where ('1' = '0' or ped.cdcliente = 1) and " 
                                                        + "cli.cdcliente = ped.cdcliente")).getRowCount());
      resultSet.close();
   }
   
   /**
    * Tests join with order and group by.
    */
   void testOrderGroupBy()
   {
      LitebaseConnection driverAux = driver;
      
      // Drops existing tables.
      if (driverAux.exists("PERGUNTA"))
         driverAux.executeUpdate("drop table PERGUNTA");
      if (driverAux.exists("ASSGRUPOPERGUNTA"))
         driverAux.executeUpdate("drop table ASSGRUPOPERGUNTA");
      if (driverAux.exists("ASSGRPPERGTITULOCHK"))
         driverAux.executeUpdate("drop table ASSGRPPERGTITULOCHK");
      
      // Creates the tables.
      driverAux.execute("CREATE TABLE PERGUNTA (IDPERGUNTA LONG PRIMARY KEY, DESCRICAO VARCHAR(115) NOT NULL, SITUACAO INT NOT NULL, " 
                                             + "FOTO INT NOT NULL)");
      driverAux.execute("CREATE TABLE ASSGRUPOPERGUNTA(IDASSGRUPOPERGUNTA LONG PRIMARY KEY, IDPERGUNTA LONG NOT NULL, " + 
                                                      "IDGRUPOPERGUNTA LONG NOT NULL, ORDEM INT NOT NULL, SITUACAO INT NOT NULL)");
      driverAux.execute("CREATE TABLE ASSGRPPERGTITULOCHK(IDASSGRPPERGTITULOCHK LONG PRIMARY KEY, IDFORMULARIO LONG NOT NULL, " 
                                                       + "IDASSGRUPOPERGUNTA LONG NOT NULL, SITUACAO INT NOT NULL)");
      
      // Populates the tables.
      driverAux.executeUpdate("insert into pergunta values (57, 'Desligar todas as fontes de tensao', 1, 0)");
      driverAux.executeUpdate("insert into ASSGRUPOPERGUNTA values (262, 57, 14, 1, 1)");
      driverAux.executeUpdate("insert into ASSGRPPERGTITULOCHK values (441, 4, 262, 1)");
      
      // The joins.
      ResultSet resultSet = driverAux.executeQuery("select ASS.IDASSGRUPOPERGUNTA, P.IDPERGUNTA, P.DESCRICAO, P.FOTO from PERGUNTA P, " 
+ "ASSGRUPOPERGUNTA ASS, ASSGRPPERGTITULOCHK ASS1 where ASS.IDGRUPOPERGUNTA=14 AND ASS1.IDFORMULARIO = 4 AND ASS.SITUACAO = 1 AND ASS1.SITUACAO = 1 " 
+ "AND P.SITUACAO = 1 AND ASS.IDPERGUNTA = P.IDPERGUNTA AND ASS1.IDASSGRUPOPERGUNTA = ASS.IDASSGRUPOPERGUNTA order by ASS.ORDEM");
      assertEquals(1, resultSet.getRowCount());
      resultSet.close();
     
      assertEquals(1, (resultSet = driverAux.executeQuery("select ASS.IDASSGRUPOPERGUNTA, P.IDPERGUNTA, P.DESCRICAO, P.FOTO from PERGUNTA P, " 
+ "ASSGRUPOPERGUNTA ASS, ASSGRPPERGTITULOCHK ASS1 where ASS.IDGRUPOPERGUNTA=14 AND ASS1.IDFORMULARIO = 4 AND ASS.SITUACAO = 1 AND ASS1.SITUACAO = 1 " 
+ "AND P.SITUACAO = 1 AND ASS.IDPERGUNTA = P.IDPERGUNTA AND ASS1.IDASSGRUPOPERGUNTA = ASS.IDASSGRUPOPERGUNTA group by ASS.IDASSGRUPOPERGUNTA, P.IDPERGUNTA, P.DESCRICAO, P.FOTO")).getRowCount());
      resultSet.close();
   }
}