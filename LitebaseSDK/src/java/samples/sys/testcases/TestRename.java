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
import totalcross.unit.*;

/**
 * This test verifies if the "rename table" and "rename column" are working properly.
 */
public class TestRename extends TestCase
{
   LitebaseConnection driver;
   
   /**
    * The main test method.
    */
   public void testRun()
   {
      driver = AllTests.getInstance("Test"); 
      
      // Drops the tables if they exist.
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      if (driver.exists("person2"))
         driver.executeUpdate("drop table person2");
      
      // Creates and populates the table with one row.
      driver.execute("create table person(name char(16) , age int, primary key(name, age))");
      driver.execute("CREATE index ageindex on person(age)");
      driver.executeUpdate("insert into person values ('renato novais', 12)");
      driver.executeUpdate("insert into person values ('fabio novais', 12)");
      
      driver.closeAll();
      driver = AllTests.getInstance("Test"); 

      assertEquals(2, selectOldTable()); // Does a select.
      
      try // The index already exists.
      {
         output("Creating index on old table");
         driver.execute("CREATE index ageindex on person(age)");
         fail("1");
      }
      catch (AlreadyCreatedException e) {}
      
      try // Tries to rename unknown table.
      {
         driver.executeUpdate("ALTER TABLE peRson2 RENAME to person ");       
         fail("2");
      }
      catch (DriverException exception) {}
      
      try // Tries to add a primary key to an unknown table.
      {
         driver.executeUpdate("ALTER TABLE peRson2 add primary key (age) ");       
         fail("3");
      }
      catch (DriverException exception) {}
      
      try // Tries to drop a primary key from an unknown table.
      {
         driver.executeUpdate("ALTER TABLE person2 drop primary key ");       
         fail("4");
      }
      catch (DriverException exception) {}
      
      assertEquals(0, driver.executeUpdate("ALTER TABLE person RENAME TO Person2")); // Renames the table.
      
      try // Tries to rename an unknown table.
      {
         driver.executeUpdate("ALTER TABLE peRson RENAME to person2 ");       
         fail("5");
      }
      catch (DriverException exception) {}
      
      try // Tries to add a primary key to an unknown table.
      {
         driver.executeUpdate("ALTER TABLE peRson add primary key (age) ");       
         fail("6");
      }
      catch (DriverException exception) {}
      
      try // Tries to drop a primary key from an unknown table.
      {
         driver.executeUpdate("ALTER TABLE person drop primary key ");       
         fail("7");
      }
      catch (DriverException exception) {}
      
      assertEquals(-1, selectOldTable()); // The old table does not exist anymore, so the select will fail.
      
      // Does a select on the new table.
      output("Select on new table");
      ResultSet rs = driver.executeQuery("Select name, age from person2 ");
      assertEquals(2, rs.getRowCount());
      rs.close();
      
      assertEquals(-1, createIndexNewTable()); // The index already exists from the old table.
      
      // But to drop the index the new table must be used instead.
      try
      {
         driver.executeUpdate("Drop index age on person");
         fail("8");
      }
      catch (DriverException exception) {}
      assertEquals(1, driver.executeUpdate("Drop index age on person2"));
      
      assertEquals(1, createIndexNewTable()); // Creates the index again.     
      assertEquals(0, driver.executeUpdate("alter table person2 drop primary key")); // Drops the primary key.
      
      try // Tries to rename an unknown column.
      {
         driver.executeUpdate("ALTER TABLE peRson2 RENAME years TO age ");       
         fail("9");
      }
      catch (DriverException exception) {}
      
      try // Tries to add a primary key to an unknown column.
      {
         driver.executeUpdate("ALTER TABLE peRson2 add primary key (years) ");       
         fail("10");
      }
      catch (DriverException exception) {}

      assertEquals(0, driver.executeUpdate("ALTER TABLE peRson2 RENAME age TO years ")); // Renames column.
      
      try // Tries to rename unknown column.
      {
         driver.executeUpdate("ALTER TABLE peRson2 RENAME age TO years ");       
         fail("11");
      }
      catch (DriverException exception) {}
      
      try // Tries to add a primary key to an unknown column.
      {
         driver.executeUpdate("ALTER TABLE peRson2 add primary key (age) ");       
         fail("12");
      }
      catch (DriverException exception) {} 
      
      try // Tries select an unknown column.
      {
         driver.executeQuery("select age from person2");       
         fail("13");
      }
      catch (SQLParseException exception) {} 
      
      try // Tries to use an unknown column in a where clause.
      {
         driver.executeQuery("select * from person2 where age = 1");       
         fail("14");
      }
      catch (SQLParseException exception) {}
      
      try // Tries select an unknown column in an aggregation.
      {
         driver.executeQuery("select max(age) as maximum from person2");       
         fail("15");
      }
      catch (SQLParseException exception) {}
      
      try // Tries select an unknown column in a data function.
      {
         driver.executeQuery("select abs(age) as maximum from person2");       
         fail("16");
      }
      catch (SQLParseException exception) {}
      
      try // Tries select use an unknown column in an order by.
      {
         driver.executeQuery("select * from person2 order by age");       
         fail("17");
      }
      catch (SQLParseException exception) {}
      
      try // Tries select use an unknown column in a group by.
      {
         driver.executeQuery("select * from person2 group by age");       
         fail("18");
      }
      catch (SQLParseException exception) {}
      
      assertEquals(-1, createIndexNewColumnTable()); // Since the new table already has an index, the index can't be created.
      assertEquals(1, driver.executeUpdate("Drop index years on person2")); // First, it must be dropped.
      assertEquals(1, createIndexNewColumnTable()); // Then, re-created.
      assertEquals(1, driver.executeUpdate("insert into persOn2(name,years) values ('indira gomes',14)")); // Inserts a new row.
      
      // Does a select using the new column.
      assertEquals(3, (rs = driver.executeQuery("Select name, years from Person2 ")).getRowCount());
      rs.close();
      
      assertEquals(1, driver.prepareStatement("Drop index years on person2").executeUpdate()); // Drops the index again.

      // Renames a table and renames it back without using close all between the operations.
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      if (driver.exists("person2"))
         driver.executeUpdate("drop table person2");
      driver.execute("create table person(name char(16) primary key, age int)");
      driver.prepareStatement("alter table person rename to person2").executeUpdate();
      driver.prepareStatement("alter table person2 rename to person").executeUpdate();
      driver.prepareStatement("alter table person rename to person2").executeUpdate();
      driver.prepareStatement("alter table person2 drop primary key").executeUpdate();
      driver.prepareStatement("CREATE index ageindex on person2(age)").executeUpdate();
      
      // Renames a table and renames it back using close all between the operations.
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      if (driver.exists("person2"))
         driver.executeUpdate("drop table person2");
      driver.execute("create table person(name char(16) primary key, age int)");
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      driver.executeUpdate("alter table person rename to person2");
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      driver.executeUpdate("alter table person2 rename to person");
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      driver.executeUpdate("alter table person rename to person2");
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      driver.executeUpdate("alter table person2 drop primary key");
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      driver.execute("CREATE index ageindex on person2(age)");
      
      String path = driver.getSourcePath();
      driver.closeAll();
      
      try // Checks if the files of the old table does not exist and that the files of the new table exist.
      {
         assertFalse(new File(path + "Test-person.db", File.DONT_OPEN).exists());
         assertTrue(new File(path + "Test-person2.db", File.DONT_OPEN).exists());
         assertFalse(new File(path + "Test-person.dbo", File.DONT_OPEN).exists());
         assertTrue(new File(path + "Test-person2.dbo", File.DONT_OPEN).exists());
         assertFalse(new File(path + "Test-person$2.idk", File.DONT_OPEN).exists());
         assertTrue(new File(path + "Test-person2$2.idk", File.DONT_OPEN).exists());
      }
      catch (IOException exception)
      {
         fail("19");
      }
 
   }

   /**
    * Does a select using the old table.
    * 
    * @return The number of rows or -1 if an exception occurs.
    */
   private int selectOldTable()
   {
      try
      {
         ResultSet rs = driver.executeQuery("Select name, age from person ");
         int ret = rs.getRowCount();
         rs.close();
         return ret;
     }
     catch (DriverException exception)
     {
        return -1;
     }
   }

   /**
    * Creates an index in the new table.
    * 
    * @return 1 If the index was successully created or -1 if an exception was thrown.
    */
   private int createIndexNewTable()
   {
      try
      {
         output("Creating index on new table");
         driver.execute("CREATE index ageindex on person2(age)");
         return 1;
      }
      catch (AlreadyCreatedException exception)
      {
        return -1;
      }
   }

   /**
    * Creates an index in the new column.
    * 
    * @return If the index was successully created or -1 if an exception was thrown.
    */
   private int createIndexNewColumnTable()
   {
      try
      {
         driver.execute("CREATE index ageindex on person2(years)");
         return 1;
      }
      catch (AlreadyCreatedException exception)
      {
         return -1;
      }
   }
}
