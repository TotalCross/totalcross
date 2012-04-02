/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package samples.sys.testcases;

import litebase.*;
import totalcross.unit.TestCase;

/**
 * Does tests with add column.
 */
public class TestAddColumn extends TestCase
{
   LitebaseConnection driver = LitebaseConnection.getInstance("test");
         
   /**
    * Main test method.
    */
   public void testRun()
   {
      wrongSyntax();
      addAfterInsert();
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
      try
      {
         driver.executeUpdate("alter table person add column");
         fail("1");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add column x");
         fail("2");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add x");
         fail("3");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add x char(5) primary key");
         fail("4");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add x char(5) default 'x' primary key");
         fail("5");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add x char(5) not null");
         fail("6");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add x char(5) not null default null");
         fail("7");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table person add primary key (name) x char(5)");
         fail("8");
      }
      catch (SQLParseException exception) {}
   }
   
   private void addAfterInsert()
   {
      create();
      int i = 100;
      PreparedStatement prepStmt = driver.prepareStatement("insert into person values (?)");
      
      while (--i >= 0)
      {
         prepStmt.setString(0, "name" + i);
         prepStmt.executeUpdate();
      }
      
      driver.executeUpdate("alter table person add a char(10) default null");
      driver.executeUpdate("alter table person add b short not null default '5'");
      driver.executeUpdate("alter table person add c int");
      driver.executeUpdate("alter table person add d long");
      driver.executeUpdate("alter table person add e double");
      driver.executeUpdate("alter table person add f datetime default '2012/03/20 16:53:42'");
      
   }
   
   private void addAterCreate()
   {
      
   }
   
   private void addAndMetaData()
   {
      
   }
   
   private void addAndPurge()
   {
      
   }
   
   private void addAndRecover()
   {
      
   }
   
   private void addAndRowIterator()
   {
      
   }
   
   private void addAndAdd()
   {
      
   }
   
   private void addAndSimpleIndex()
   {
      
   }
   
   private void addAndComposedIndex()
   {
      
   }
   
   
   
   
}
