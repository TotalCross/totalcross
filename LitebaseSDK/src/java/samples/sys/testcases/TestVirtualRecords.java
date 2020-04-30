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
 * This tests if the load factor growth is being stored correctly.
 */
public class TestVirtualRecords extends TestCase
{
   /** 
    * Tests if the select results are as expected.
    *
    * @param driver The connection with Litebase.
    * @param countAssert The expected number of returned rows.
    */
   private void doSelect(LitebaseConnection driver, int countAssert)
   {
      ResultSet resultSet = driver.executeQuery(" select name from virtrecs");
      int       count = 0;

      resultSet.afterLast();
      while (resultSet.prev())
         if (count++ % 10 == 0)
            assertEquals("Name" + (countAssert - count), new String(resultSet.getChars(1)));
      assertEquals(countAssert, resultSet.getRowCount());
      resultSet.close();
      assertEquals(countAssert, count);
   }

   /** 
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");

      try
      {
         driver.executeUpdate(" drop table virtrecs");
      } 
      catch (DriverException de) {}

      try
      {
         // Huge records!
         driver.execute(" CREATE TABLE virtrecs (NAME CHAR(500) NOCASE, ADDRESS char(700))");
         driver.execute(" create index idxname on virtrecs(name)");
      } 
      catch (AlreadyCreatedException ace) {}

      try // There can't be two indices for the same column.
      {
         driver.execute(" create index idxname on virtrecs(name)");
         fail();
      } 
      catch (AlreadyCreatedException ace) {}

      // Tests the select on the empty table.
      ResultSet resultSet = driver.executeQuery("select name from virtrecs");
      assertEquals(0, resultSet.getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();

      // Populates the table.
      driver.setRowInc("virtrecs", 100);
      int i = -1;
      while (++ i < 100)
         assertEquals(1, driver.executeUpdate(" INSERT INTO virtrecs VALUES ('Name" + i + "','Addr" + i + "')"));
      driver.setRowInc("virtrecs", -1);

      // Does a query that returns no results.
      assertEquals(0, (resultSet = driver.executeQuery("select name from virtrecs where name = 'xxx'")).getRowCount());

      // The result set is kept open.
      doSelect(driver, 100);
      driver.closeAll();

      // Tries again.
      driver = AllTests.getInstance("Test");
      doSelect(driver, 100);

      // Tries to insert data after closeAll();
      assertEquals(1, driver.executeUpdate("INSERT INTO virtrecs VALUES ('Name100', 'Addr100')"));
      doSelect(driver, 101);
      driver.closeAll();

      // Tries again.
      driver = AllTests.getInstance("Test");
      doSelect(driver, 101);
      
      // Closes every thing.
      resultSet.close();
      
      // Very big identifiers.
      driver.executeUpdate("drop table virtrecs");
      driver.execute("create table virtrecs (namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamename char(10), id double)");
      driver.executeUpdate("insert into virtrecs values ('12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890', 0)");
      try
      {
         driver.executeUpdate("update virtrecs set id = 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
      }
      catch (SQLParseException exception) {}
      driver.closeAll();
   }
}
