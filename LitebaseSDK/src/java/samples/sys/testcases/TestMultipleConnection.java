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

/**
 * Tests Litebase with multiple connections, to access different databases at the same time.
 */
public class TestMultipleConnection extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      String tempPath = Convert.appendPath(Settings.appPath, "temp");
      
      // Tests different ways of writing paths.
      TestConnections(tempPath + "/a");
      TestConnections(Convert.appendPath(tempPath, "\\b"));
   }

   /**
    * Tests 2 different connections at the same time.
    * 
    * @param tempPath The path of one of the connections used.
    */
   private void TestConnections(String tempPath)
   {
   	String dataPath = Settings.appPath;

      LitebaseConnection driver1 = AllTests.getInstance("Test", dataPath); // The first connection is on datapath.
      if (driver1.exists("person")) // Drops the table of the first connection.
         driver1.executeUpdate("drop table person");

      if (Settings.nvfsVolume != -1) // Appends the volume number to the path.
      	tempPath = Settings.nvfsVolume + (':' + tempPath);
      LitebaseConnection driver2 = AllTests.getInstance("Test", tempPath); // The second connection is on tempPath.
      if (driver2.exists("person")) // Drops the table of the second connection.
         driver2.executeUpdate("drop table person");

      // Both tables can't exist yet.
      assertFalse(driver1.exists("person"));
      assertFalse(driver2.exists("person"));

      // After creating the table in the first connection, only this one can exists.
      driver1.execute("create table person (id int, name char(10))");
      assertTrue(driver1.exists("person"));
      assertFalse(driver2.exists("person"));

      // After creating the table in the second connection, both must exist.
      driver2.execute("create table person (id int, name char(10), age int)");
      assertTrue(driver1.exists("person"));
      assertTrue(driver2.exists("person"));

      // Both rows must be inserted successfully.
      assertEquals(1, driver1.executeUpdate("insert into person values (0, '0')"));
      assertEquals(1, driver2.executeUpdate("insert into person values (1, '1', 1)"));
      
      // Both rows must be found in the queries.
      ResultSet rs1 = driver1.executeQuery("select * from person");
      ResultSet rs2 = driver2.executeQuery("select * from person");
      assertEquals(1, rs1.getRowCount());
      assertEquals(1, rs2.getRowCount());
      assertTrue(rs1.next());
      assertEquals("0", rs1.getString("name"));
      assertTrue(rs2.next());
      assertEquals("1", rs2.getString("name"));
      rs2.close();

      // Inserts the row of the table of the first connection in the one of the second connection.
      assertEquals(1, driver2.executeUpdate("insert into person values('" + rs1.getString("id") + "','" + rs1.getString("name") + "', 2)"));

      // A query on the table of the second connection must return all its rows.
      assertTrue((rs2 = driver2.executeQuery("select * from person")).next());
      assertEquals(1, rs2.getInt("age"));
      assertTrue(rs2.next());
      assertEquals(2, rs2.getInt("age"));
      rs2.close();
      
//      assertEquals(-1, driver1.getSlot());
//      assertEquals(-1, driver2.getSlot());
      
      // Closes both connections.
      driver1.closeAll();
      driver2.closeAll();
   }
}