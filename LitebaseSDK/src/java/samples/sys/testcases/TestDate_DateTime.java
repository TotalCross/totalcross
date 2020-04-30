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
import totalcross.util.*;

/**
 * This test verifies if the DATE and DATETIME Litebase data fields are working properly.
 */
public class TestDate_DateTime extends TestCase
{
   LitebaseConnection driver;
   
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      driver = AllTests.getInstance("Test"); 

      // Inserts the items into a new table and executes the selects.
      createTable();
      output("Do selects 0");
      doSelects();
      createTablePreparedStatement();
      output("Do selects 1");
      doSelects();
      
      // Tests with prepared Statemet.
      testPreparedStatement();
      testPreparedStatement2();
      createTablePrimarykeyDateTime();
      testSetDateTime();
      
      // Tests like without indices.
      driver.executeUpdate("alter table person drop primary key");
      testLikeStartsWithEqualsAnythingDateDateTime();
      driver.execute("create index idx on person(years)");
      driver.execute("create index idx on person(birth)");
      testLikeStartsWithEqualsAnythingDateDateTime();
      driver.closeAll();
   }

   /**
    * Executes some selects in the table previously created.
    */
   private void doSelects()
   {
      // Where clause with equalities.
      ResultSet rs = driver.executeQuery("Select name, age,birth , years from person where birth = '2001/06/06'");
      assertNotNull(rs);
      assertEquals(0, rs.getRowCount());
      rs.close();
      assertNotNull(rs = driver.executeQuery("Select name, age,birth , years from person where years = '2008/06/06 13:45'"));
      assertEquals(1, rs.getRowCount());
      rs.close();
      
      // Where clause with inequalities.
      assertNotNull(rs = driver.executeQuery("Select name, age,birth , years from person where birth <= '2005-7/8'"));
      assertEquals(2, rs.getRowCount());
      rs.close();
      assertNotNull(rs = driver.executeQuery("Select name, age,birth , years from person where years < '2007/06/06  0.0'"));
      assertEquals(3, rs.getRowCount());
      rs.close();
      assertNotNull(rs = driver
                       .executeQuery("Select name, age,birth , years from person where birth > '2003/01/02' and years < '2030/06/06  1.59:40'"));
      assertEquals(4, rs.getRowCount());
      rs.close();

      // Aggregation and group by.
      assertNotNull(rs = driver.executeQuery("Select years as aName, sum(age) as abirth from person group by years"));
      assertTrue(rs.next());
      assertEquals("28.0", rs.getString("aBirth"));
      assertTrue(rs.next());
      assertEquals("12.0", rs.getString("aBirth"));
      assertTrue(rs.next());
      assertEquals("20.0", rs.getString("aBirth"));
      assertEquals(3, rs.getRowCount());
      rs.close();
      
      // Gets all the years of the table.
      assertNotNull(rs = driver.executeQuery("Select year(birth) as years from person"));
      assertTrue(rs.next());
      assertEquals("2005", rs.getString(1));
      assertTrue(rs.next());
      assertEquals("2005", rs.getString(1));
      assertTrue(rs.next());
      assertEquals("2005", rs.getString(1));
      assertTrue(rs.next());
      assertEquals("2005", rs.getString(1));
      assertFalse(rs.next());
      rs.close();
      
      // Invalid date and datetime.
      try
      {
         driver.executeQuery("Select * from person where birth = ''");
         fail("1");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("Select * from person where years = ''");
         fail("2");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("Select * from person where birth = ' '");
         fail("3");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("Select * from person where years = ' '");
         fail("4");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("Select * from person where years = '3000/12/31'");
         fail("5");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("Select * from person where years = '2010/02/29'");
         fail("6");
      }
      catch (SQLParseException exception) {}
   }

   /**
    * Creates a table and populates it with normal selects.
    */
   private void createTable()
   {
      output("Create simple table and insert.");
      
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      driver.execute("create table person(name char(16) , age int, birth Date, years DateTime)");
      driver.execute("create index idx on person(years)");
      driver.executeUpdate("insert into person values ('renato novais',12, '  2005/9-12  ', ' 2006/08-21    12:08:01:234 ')");
      driver.executeUpdate("insert into person values ('indira gomes',13, '2005/7/8 ', '2006/08-21 0:08')");
      driver.executeUpdate("insert into person values ('Lucas Novais',20, '2005/7/8', ' 2008/06/06  13:45 ')");
      driver.executeUpdate("insert into person values ('Zenes Lima',15, '2005/9/12 ', '2006/08-21 0:08')");
      
      // Invalid date and datetime,
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, '', '1979/06-26 2:00')");
         fail("7");
      } 
      catch (SQLParseException exception) 
      {
         exception.printStackTrace();
      }
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, '1979/06/26', '')");
         fail("8");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, ' ', '1979/06-26 2:00')");
         fail("9");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, '1979/06/26', ' ')");
         fail("10");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, '1979/02/29', '1979/06-26 2:00')");
         fail("11");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, '3000/26/06', '1979/06-26 2:00')");
         fail("12");
      } 
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into person values ('Juliana Imperial', 29, '1979/26/06', '1979/06-26 2:00:00.000.0')");
         fail("13");
      } 
      catch (SQLParseException exception) {}
   }

   /**
    * Does a simple select and uses it to get the number of rows of a table.
    * 
    * @param tableName The table whose rows are to be counted.
    * @return The table number of rows.
    */
   private int getRowCount(String tableName) // rnovais@_570_56
   {
      ResultSet rs = driver.executeQuery("Select * from " + tableName);
      int ret = rs.getRowCount();
      rs.close();
      return ret;
   }

   /**
    * Creates and populates a table using prepared statement.
    */
   private void createTablePreparedStatement()
   {
      output("Create simple table and insert with prepared statement.");
      
      driver.prepareStatement("drop table person").executeUpdate();
      driver.prepareStatement("create table person(name char(16) , age int, birth Date, years DateTime)").executeUpdate();
      
      PreparedStatement ps = driver.prepareStatement("insert into person values(?,?,?,?)");
      
      // Inserts the first.
      ps.setString(0, "renato novais");
      ps.setInt(1, 12);
      ps.setString(2, "  2005/9-12  ");
      ps.setString(3, " 2006/08-21    12:08:01:0 ");
      ps.executeUpdate();
      
      // Inserts the second.
      ps.setString(0, "indira gomes");
      ps.setInt(1, 13);
      ps.setString(2, "2005/7/8 ");
      ps.setString(3, "2006/08-21 0:08");
      ps.executeUpdate();
      
      // Inserts the third.
      ps.setString(0, "Lucas Novais");
      ps.setInt(1, 20);
      ps.setString(2, "05/7/8 ");
      ps.setString(3, " 2008/06/06  13:45");
      ps.executeUpdate();
      
      // Inserts the fourth.
      ps.setString(0, "Zenes Lima");
      ps.setInt(1, 15);
      ps.setString(2, " 2005/9/12");
      ps.setString(3, "2006/08-21 0:08");
      ps.executeUpdate();
      
      assertEquals(4, getRowCount("person"));
      
      // Invalid date.
      ps.setString(0, "Juliana Imperial");
      ps.setInt(1, 29);
      ps.setString(2, "");
      ps.setString(3, " 1979/06-26    00:02:00:0 ");
      try
      {
         ps.executeUpdate();
         fail("14");
      }
      catch (SQLParseException exception) {}
      
      // Invalid datetime.
      ps.setString(0, "Juliana Imperial");
      ps.setInt(1, 29);
      ps.setString(2, " 1979/06/26");
      ps.setString(3, "");
      try
      {
         ps.executeUpdate();
         fail("15");
      }
      catch (SQLParseException exception) {}
      
      // Invalid date.
      ps.setString(0, "Juliana Imperial");
      ps.setInt(1, 29);
      ps.setString(2, " ");
      ps.setString(3, " 1979/06-26    00:02:00:0 ");
      try
      {
         ps.executeUpdate();
         fail("16");
      }
      catch (SQLParseException exception) {}
      
      // Invalid datetime.
      ps.setString(0, "Juliana Imperial");
      ps.setInt(1,29);
      ps.setString(2, " 1979/06/26");
      ps.setString(3, " ");
      try
      {
         ps.executeUpdate();
         fail("17");
      }
      catch (SQLParseException exception) {}
      
      // Inserts and deletes a valid value.
      try 
      {
         ps.setString(0, "Juliana Imperial");
         ps.setInt(1, 29);
         ps.setDate(2, new Date("26/06/1979", Settings.DATE_DMY));
         ps.setDateTime(3, new Date("09/02/2009", Settings.DATE_DMY));
         ps.executeUpdate();
      }
      catch (SQLParseException exception) 
      {
         fail("18");
      }
      catch (InvalidDateException exception) 
      {
         fail(exception.getMessage());
      }
      driver.executeUpdate("delete from person where years = '2009/02/09'");
   }
   
   /**
    * Does some tests using update and delete prepared statements.
    */
   private void testPreparedStatement() // rnovais@_570_56
   {
      output("Test Prepared Statement.");
      if (driver.exists("person2"))
         driver.prepareStatement(" drop table person2 ").executeUpdate();

      driver.prepareStatement(" create table person2(name char(16) , age int, birth Date, years DateTime) ").executeUpdate();
      driver.prepareStatement(" create index idx on person2(years) ").executeUpdate();
      driver.prepareStatement(" create index idx on person2(birth) ").executeUpdate();
      
      PreparedStatement ps = driver.prepareStatement(" insert into person2 values(?,?,?,?) ");
      
      // Inserts the first.
      ps.setString(0, "renato novais");
      ps.setInt(1, 12);
      ps.setString(2, "  2005/9-12  ");
      ps.setString(3, " 2006/08-21    12:08:01:0 ");
      ps.executeUpdate();
      
      // Inserts the second.
      ps.setString(0, "indira gomes");
      ps.setInt(1, 13);
      ps.setString(2, "2005/7/8 ");
      ps.setString(3, "2006/08-21 0:08");
      ps.executeUpdate();
      
      // Inserts the third.
      ps.setString(0, "Lucas Novais");
      ps.setInt(1, 20);
      ps.setString(2, "05/7/8 ");
      ps.setString(3, " 2008/06/06  13:45");
      ps.executeUpdate();
      
      // Inserts the fourth.
      ps.setString(0, "Zenes Lima");
      ps.setInt(1, 15);
      ps.setString(2, " 2005/9/12");
      ps.setString(3, "2006/08-21 0:08");
      ps.executeUpdate();
      assertEquals(4, getRowCount("person2"));
      
      // juliana@220_6: setDate() and setDateTime() must accept null values.
      ps.setDate(2, null);
      ps.setDateTime(3, (Date)null);
      ps.setDateTime(3, (Time)null);
      
      try 
      {
         // Updates // rnovais@570_56
         (ps = driver.prepareStatement("update person2 set name = ?  where birth = ?")).setString(0, "danilo");
         ps.setDate(1, new Date("8/7/2005", Settings.DATE_DMY));
         assertEquals(2, ps.executeUpdate());
         assertEquals(4, getRowCount("person2"));

         (ps = driver.prepareStatement("update person2 set name = ?  where years = ?")).setString(0, "carol");
         ps.setDateTime(1, new Time("20060821T12:08:01"));
         assertEquals(1, ps.executeUpdate());
         assertEquals(4, getRowCount("person2"));

         // Deletes // rnovais@570_56
         (ps = driver.prepareStatement("delete from person2 where birth = ?")).setDate(0, new Date("12/09/2005", Settings.DATE_DMY));
         assertEquals(2, ps.executeUpdate());
         assertEquals(2, getRowCount("person2"));

         (ps = driver.prepareStatement("delete from person2 where years = ?")).setDateTime(0, new Time("20060821T00:08:00"));
         assertEquals(1, ps.executeUpdate());
         assertEquals(1, getRowCount("person2"));   
      }
      catch (InvalidNumberException exception) 
      {
         fail(exception.getMessage());
      }
      catch (InvalidDateException exception) 
      {
         fail(exception.getMessage());
      }
   }

   /**
    * Does some tests using select prepared statements with indices and functions.
    */
   private void testPreparedStatement2() // rnovais@570_108
   {
      output("Test Prepared Statement 2.");
      
      driver.prepareStatement(" drop index * on person2 ").executeUpdate();
      driver.prepareStatement(" drop table person2 ").executeUpdate();

      driver.prepareStatement("create table person2(name char(16) , age int, birth Date, years DateTime)").executeUpdate();
      driver.prepareStatement("create index idx on person2(years)").executeUpdate();
      PreparedStatement ps = driver.prepareStatement("insert into person2 values(?,?,?,?)");
      
      // Inserts the first.
      ps.setString(0, "renato novais");
      ps.setInt(1, 12);
      ps.setString(2, "  2005/9-12  ");
      ps.setString(3, " 2006/08-21    12:08:01:0 ");
      ps.executeUpdate();
      driver.prepareStatement("insert into person2 values ('jo�o pedro',13, '2006/7/8 ', '2006/08-21 0:08')").executeUpdate();
      driver.prepareStatement("insert into person2 values ('danilo novais',20, '2008/4/6', ' 2008/06/06  13:45 ')").executeUpdate();
      driver.prepareStatement("insert into person2 values ('carol nOVAIS',15, '2005/9/12 ', '2005/01-4 1:50')").executeUpdate();

      try
      {
         (ps = driver.prepareStatement("select * from person2 where birth = ?")).setDate(0, new Date("8/7/2006", Settings.DATE_DMY));
         ResultSet rs = ps.executeQuery();
         assertTrue(rs.next());
         assertEquals("jo�o pedro", rs.getString(1));
         rs.close();

         (ps = driver.prepareStatement("select name from person2 where month(birth) = ? and day(birth) = ? and minute(years) = 50 and millis(years) != ?")).setShort(0, (short)9);
         ps.setShort(1, (short)12);
         ps.setShort(2, (short)1);
         assertTrue((rs = ps.executeQuery()).next());
         assertEquals("carol nOVAIS", rs.getString(1));
         rs.close();

         (ps = driver.prepareStatement("select name from person2 where month(birth) != ? and (day(birth) = ? or minute(years) = ?)")).setShort(0, (short)7);
         ps.setShort(1, (short)6);
         ps.setShort(2, (short)8);
         assertEquals(2, (rs = ps.executeQuery()).getRowCount());
         assertTrue(rs.next());
         assertEquals("renato novais", rs.getString(1));
         assertTrue(rs.next());
         assertEquals("danilo novais", rs.getString(1));
         rs.close();

         (ps = driver.prepareStatement("select name, birth, years from person2 where (day(birth) = ? and birth != ?) or minute(years) = ?")).setShort(0, (short)6);
         ps.setDate(1, new Date("8/7/2006", Settings.DATE_DMY));
         ps.setShort(2, (short)8);
         assertEquals(3, (rs = ps.executeQuery()).getRowCount());
         assertTrue(rs.next());
         assertEquals("renato novais", rs.getString(1));
         
         int settings = Settings.dateFormat;
         boolean is24Hour = Settings.is24Hour;
         
         Settings.dateFormat = Settings.DATE_DMY;
         Settings.is24Hour = true; 
         
         assertEquals("12/09/2005", rs.getString(2));
         assertEquals("21/08/2006 12:08:01:000", rs.getString(3));
         assertTrue(rs.next());
         assertEquals("jo�o pedro", rs.getString(1));
         assertEquals("08/07/2006", rs.getString(2));
         assertEquals("21/08/2006 00:08:00:000", rs.getString(3));
         assertTrue(rs.next());
         assertEquals("danilo novais", rs.getString(1));
         assertEquals("06/04/2008", rs.getString(2));
         assertEquals("06/06/2008 13:45:00:000", rs.getString(3));
         
         Settings.dateFormat = (byte)settings;
         Settings.is24Hour = is24Hour; 
         rs.close();
      }
      catch (InvalidDateException exception) 
      {
         fail(exception.getMessage()); 
      }
   }
   
   /**
    * Tests duplicates insertions with primary key.
    */
   private void createTablePrimarykeyDateTime()
   {
      driver.executeUpdate("drop table person");
      
      // Primary key on a date field.
      driver.execute("create table person(name char(16), age int, birth date primary key, years DateTime)");
      
      // Duplicated date.
      assertEquals(1, driver.executeUpdate("insert into person values ('renato novais',12,'2005/9-12', '2006/08-21 12:08')"));
      try
      {
         driver.executeUpdate("insert into person values ('indira gomes',13,'2005/9-12', '2007/09/20')");
         fail("1. PrimaryKeyViolationException not thrown");
      }
      catch (PrimaryKeyViolationException exception) {}
      
      // Primary key on a datetime field.
      driver.executeUpdate("alter table person drop primary key");
      driver.executeUpdate("alter table person add primary key (years)");
      driver.executeUpdate("delete from person");
   
      // Duplicated datetime.
      assertEquals(1, driver.executeUpdate("insert into person values ('renato novais',12,'2005/9-12', '2006/08-21 12:08')"));
      try
      {
         driver.executeUpdate("insert into person values ('indira gomes',13,'2007/06/15', '2006/08-21 12:08')");
         fail("2. PrimaryKeyViolationException not thrown");
      }
      catch (PrimaryKeyViolationException exception) {}
       
   }
   
   /** 
    * Tests that setDateTime inserts the correct value of a DATETIME using a <code>time</code> object.
    */
   private void testSetDateTime()
   {
      if (driver.exists("time"))
         driver.executeUpdate("drop table time");
      driver.execute("CREATE TABLE time (campo DATETIME primary key, data date default '1979/06/26')");
      PreparedStatement pstmt = driver.prepareStatement("INSERT INTO time (campo) VALUES(?)");
      Time now = new Time();
      pstmt.setDateTime(0, now);
      pstmt.executeUpdate();
      ResultSet rs = driver.executeQuery("SELECT * FROM time");
      rs.next();
      assertEquals(now.toIso8601(), rs.getDateTime(1).toIso8601());
      try
      {
         assertEquals(new Date(19790626), rs.getDate(2));
      }
      catch (InvalidDateException exception) {}
      rs.close();
   }
   
   /**
    * Tests DATE and DATETIME with like using the pattern mathes EQUALS, STARTS_WITH, and ANYTHING.
    */
   private void testLikeStartsWithEqualsAnythingDateDateTime()
   {
      ResultSet resultSet = driver.executeQuery("select * from person where birth like '2005/09/12'");
      assertEquals(1, resultSet.getRowCount());
      resultSet.close();  
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '20%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '200%'")).getRowCount());
      resultSet.close(); 
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005%'")).getRowCount());
      resultSet.close(); 
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005/%'")).getRowCount());
      resultSet.close(); 
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005/0%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005/09%'")).getRowCount());
      resultSet.close(); 
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005/09/%'")).getRowCount());
      resultSet.close(); 
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005/09/1%'")).getRowCount());
      resultSet.close(); 
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where birth like '2005/09/12%'")).getRowCount());
      resultSet.close();
      
      assertEquals(0, (resultSet = driver.executeQuery("select * from person where birth like '2005/09/12 %'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:00:000%'")).getRowCount());
      resultSet.close();

      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '20%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '200%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006%'")).getRowCount());
      resultSet.close();

      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/0%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/2%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 %'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 1%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:0%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:0%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:00%'")).getRowCount());
      resultSet.close();

      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:00:%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:00:0%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:00:00%'")).getRowCount());
      resultSet.close();
      
      assertEquals(1, (resultSet = driver.executeQuery("select * from person where years like '2006/08/21 12:08:00:000%'")).getRowCount());
      resultSet.close();
   }
   
}
