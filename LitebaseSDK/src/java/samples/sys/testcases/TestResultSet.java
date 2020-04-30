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

import totalcross.unit.TestCase;
import litebase.*;

/**
 * Tests many <code>ResultSet</code> public methods that are not tested in the previous test cases,
 */
public class TestResultSet extends TestCase
{
   /**
    * Main test method.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      
      // Creates and populates the test table.
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      driver.execute("create table person(name char(16) , age int, birth Date, years DateTime)");
      driver.execute("create index idx on person(years)");
      driver.executeUpdate("insert into person values ('Renato Novais',12, '2005/9-12 ', ' 2006/08-21 12:08:01:234 ')");
      driver.executeUpdate("insert into person values ('Indira Gomes',13,'2005/7/8 ', '2006/08-21 0:08')");
      driver.executeUpdate("insert into person values ('Lucas Novais',20,'2005/7/8', ' 2008/06/06 13:45 ')");
      driver.executeUpdate("insert into person values ('Zenes Lima',15, '2005/9/12', '2006/08-21 0:08')");
      
      testResultSet(driver.executeQuery("select * from person where age > 0 order by name")); // Select with temporary table.
      testResultSet(driver.executeQuery("select * from person where age > 0")); // Select without temporary table.
      testResultSet(driver.executeQuery("select * from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, name, age, birth, years from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, name, age, birth, years as anos from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, upper(name) as nameAux, age, birth, years as anos from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, lower(name) as nameAux, age, birth, years as anos from person")); // Select without temporary table.
      
      // Drops some records and adds them again so that there are enpty spaces in the select base table.
      driver.executeUpdate("delete from person where age = 12");
      driver.executeUpdate("insert into person values ('Renato Novais',12, '2005/9-12 ', ' 2006/08-21 12:08:01:234 ')");
      driver.executeUpdate("delete from person where age = 20");
      driver.executeUpdate("insert into person values ('Lucas Novais',20,'2005/7/8', ' 2008/06/06 13:45 ')");
      driver.executeUpdate("delete from person where age = 15");
      driver.executeUpdate("insert into person values ('Zenes Lima',15, '2005/9/12', '2006/08-21 0:08')");
      driver.executeUpdate("insert into person values ('Juliana Imperial',29, '1979/06/26', '2009/04-14 0:08')");
      driver.executeUpdate("delete from person where age = 29");
      
      testResultSet(driver.executeQuery("select * from person where age > 0 order by name")); // Select with temporary table.
      testResultSet(driver.executeQuery("select * from person where age > 0")); // Select without temporary table.
      testResultSet(driver.executeQuery("select * from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, name, age, birth, years from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, name, age, birth, years as anos from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, upper(name) as nameAux, age, birth, years as anos from person")); // Select without temporary table.
      testResultSet(driver.executeQuery("select rowid, lower(name) as nameAux, age, birth, years as anos from person")); // Select without temporary table.
      
      driver.closeAll(); 
   }
   
   /**
    * Tests the result set public methods.
    * 
    * @param resultSet The result set to be tested.
    */
   private void testResultSet(ResultSet resultSet)
   {
      String[][] matrix;
      String nameAux;
      
      assertNotNull(resultSet);
      assertEquals(4, resultSet.getRowCount());
      
      try // Invalid result set position.
      {
         resultSet.getStrings();
         fail("1");
      }
      catch (DriverException exception) {}
      
      while (resultSet.next())
      {
         try // Checks that the functions lower and upper worked.
         {
            nameAux = resultSet.getString("nameAux");
            assertTrue(nameAux.equals(nameAux.toLowerCase()) || nameAux.equals(nameAux.toUpperCase()));
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         
         // Invalid index.
         try
         {
            resultSet.getInt(0);
            fail("2");
         }
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getDateTime(0);
            fail("3");
         }
         catch (IllegalArgumentException exception) {}
         catch (DriverException exception) {}
         try
         {
            resultSet.isNull(0);
            fail("4");
         }
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getInt("boboca");
            fail("5");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getDateTime("boboca");
            fail("6");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.isNull("boboca");
            fail("7");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getInt(6);
            fail("8");
         }
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getDateTime(6);
            fail("9");
         }
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.isNull(6);
            fail("10");
         }
         catch (IllegalArgumentException exception) {}
         
         // Invalid type or column.
         try
         {
            resultSet.getChars("rowid");
            fail("11");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getChars("years");
            fail("12");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getChars("anos");
            fail("13");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
         try
         {
            resultSet.getInt("name");
            fail("14");
         }
         catch (DriverException exception) {}
         catch (IllegalArgumentException exception) {}
      }
      
      // Tests first() and next().
      resultSet.first();
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.next();
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.next();
      resultSet.next();
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      resultSet.next(); // next() always returns the last result if it has passed the last position.
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests beforeFirst() and next().
      resultSet.beforeFirst();
      resultSet.next();
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.next();
      resultSet.next();
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      resultSet.next();
      resultSet.next(); // next() always return the last result if it has passed the last position.
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests last() and prev().
      resultSet.last();
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.prev();
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.prev();
      resultSet.prev();
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev(); // prev() always return the first result if it has passed the first position.
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests afterLast() and prev().
      resultSet.afterLast();
      resultSet.prev();
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.prev();
      resultSet.prev();
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev();
      resultSet.prev(); // prev() always return the first result if it has passed the first position. 
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests absolute.
      resultSet.absolute(0);
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(1);
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(2);
      assertEquals(2,(matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(3);
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests ascending relative with beforeFirst().
      resultSet.beforeFirst();
      resultSet.relative(0); // relative() always set the position to the first or last record if it is in an invalid position.
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.relative(1);
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.relative(2);
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.relative(3);
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.relative(4);
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.beforeFirst();
      resultSet.relative(5); // relative() always set the position to the first or last record if it is in an invalid position.
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests ascending relative with first().
      resultSet.first();
      resultSet.relative(0);
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.relative(1);
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.relative(2);
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.relative(3);
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.first();
      resultSet.relative(4); // relative() always set the position to the first or last record if it is in an invalid position.
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests descending relative with afterLast().
      resultSet.afterLast();
      resultSet.relative(0); // relative() always set the position to the first or last record if it is in an invalid position.
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.relative(-1);
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.relative(-2);
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.relative(-3);
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.relative(-4);
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.afterLast();
      resultSet.relative(-5); // relative() always set the position to the first or last record if it is in an invalid position.
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // Tests descending relative with last().
      resultSet.last();
      resultSet.relative(0);
      assertEquals(1, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.relative(-1);
      assertEquals(2, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.relative(-2);
      assertEquals(3, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.relative(-3);
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.last();
      resultSet.relative(-4); // relative() always set the position to the first or last record if it is in an invalid position.
      assertEquals(4, (matrix = resultSet.getStrings()).length);
      assertBetween(4, matrix[0].length, 5);
      
      // getStrings() with parameter beginning in the first record. 
      resultSet.absolute(0);
      assertEquals(0, resultSet.getRow());
      assertEquals(0, (matrix = resultSet.getStrings(0)).length);
      resultSet.absolute(0);
      assertEquals(0, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(1)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(0);
      assertEquals(0, resultSet.getRow());
      assertEquals(2, (matrix = resultSet.getStrings(2)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(0);
      assertEquals(0, resultSet.getRow());
      assertEquals(3, (matrix = resultSet.getStrings(3)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(0);
      assertEquals(0, resultSet.getRow());
      assertEquals(4, (matrix = resultSet.getStrings(4)).length);
      assertBetween(4, matrix[0].length, 5);
      
      // getStrings() with parameter beginning in the second record. 
      resultSet.absolute(1);
      assertEquals(1, resultSet.getRow());
      assertEquals(0, (matrix = resultSet.getStrings(0)).length);
      resultSet.absolute(1);
      assertEquals(1, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(1)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(1);
      assertEquals(1, resultSet.getRow());
      assertEquals(2, (matrix = resultSet.getStrings(2)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(1);
      assertEquals(1, resultSet.getRow());
      assertEquals(3, (matrix = resultSet.getStrings(3)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(1);
      assertEquals(1, resultSet.getRow());
      assertEquals(3, (matrix = resultSet.getStrings(4)).length);
      assertBetween(4, matrix[0].length, 5);
      
      // getStrings() with parameter beginning in the third record. 
      resultSet.absolute(2);
      assertEquals(2, resultSet.getRow());
      assertEquals(0, (matrix = resultSet.getStrings(0)).length);
      resultSet.absolute(2);
      assertEquals(2, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(1)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(2);
      assertEquals(2, resultSet.getRow());
      assertEquals(2, (matrix = resultSet.getStrings(2)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(2);
      assertEquals(2, resultSet.getRow());
      assertEquals(2, (matrix = resultSet.getStrings(3)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(2);
      assertEquals(2, resultSet.getRow());
      assertEquals(2, (matrix = resultSet.getStrings(4)).length);
      assertBetween(4, matrix[0].length, 5);
      
      // getStrings() with parameter beginning in the fourth record. 
      resultSet.absolute(3);
      assertEquals(3, resultSet.getRow());
      assertEquals(0, (matrix = resultSet.getStrings(0)).length);
      resultSet.absolute(3);
      assertEquals(3, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(1)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(3);
      assertEquals(3, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(2)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(3);
      assertEquals(3, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(3)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(3);
      assertEquals(3, resultSet.getRow());
      assertEquals(1, (matrix = resultSet.getStrings(4)).length);
      assertBetween(4, matrix[0].length, 5);
      
      // getStrings() with parameter beginning in the fourth record. 
      resultSet.absolute(4);
      assertEquals(0, (matrix = resultSet.getStrings(0)).length);
      resultSet.absolute(4);
      assertEquals(1, (matrix = resultSet.getStrings(1)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(4);
      assertEquals(1, (matrix = resultSet.getStrings(2)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(4);
      assertEquals(1, (matrix = resultSet.getStrings(3)).length);
      assertBetween(4, matrix[0].length, 5);
      resultSet.absolute(4);
      assertEquals(1, (matrix = resultSet.getStrings(4)).length);
      assertBetween(4, matrix[0].length, 5);
      
      try // It is not possible use getStrings() with negative numbers other than -1.
      {
         resultSet.getStrings(-2);
         fail("15");
      }
      catch (IllegalArgumentException exception) {}
      
      resultSet.close(); // Closes the result set. All result set methods must throw an excetion on an atempt to use it,
      try
      {
         resultSet.absolute(1);
         fail("16");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.afterLast();
         fail("17");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.close();
         fail("18");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.first();
         fail("19");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getString(1);
         fail("20");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getString("name");
         fail("21");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getResultSetMetaData();
         fail("22");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getRow();
         fail("23");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getRowCount();
         fail("24");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getStrings();
         fail("25");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.isNull(1);
         fail("26");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.isNull("name");
         fail("27");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.last();
         fail("28");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.next();
         fail("29");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.prev();
         fail("30");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.relative(1);
         fail("31");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.setDecimalPlaces(1, 1);
         fail("32");
      } 
      catch (IllegalStateException exception) {} 
   } 
}
