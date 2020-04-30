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
 * Tests index on rowid after table creation.
 */
public class TestReIndex2rowId extends TestCase
{
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");

      /**
       * The main test method.
       */
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      
      // Creates and populates the table.
      driver.execute("create table person(name char(20), age int)");
      driver.executeUpdate("insert into person(name, age) values ('RLN_0', 0)");
      driver.executeUpdate("insert into person(name, age) values ('RLN_1', 1)");
      driver.executeUpdate("insert into person(name, age) values ('RLN_2', 2)");
      driver.executeUpdate("insert into person(name, age) values ('RLN_3', 3)");
      driver.executeUpdate("insert into person(name, age) values ('RLN_4', 4)");

      driver.execute("create index idxId on person(rowId)"); // Creates an index on rowid after some inserts.

      // Do selects.
      ResultSet rs = driver.executeQuery("Select  rowid, name, age from person where rowid = 3");
      assertEquals(1, rs.getRowCount());
      assertTrue(rs.next());
      assertEquals(3, rs.getInt("rowid"));
      assertEquals("RLN_2", rs.getString("name"));
      assertEquals(2, rs.getInt("age"));
      rs.close();

      assertEquals(3, (rs = driver.executeQuery("Select  rowid, name, age from person where rowid > 2")).getRowCount());
      assertTrue(rs.next());
      assertEquals(3, rs.getInt("rowid"));
      assertEquals("RLN_2", rs.getString("name"));
      assertEquals(2, rs.getInt("age"));
      assertTrue(rs.next());
      assertEquals(4, rs.getInt("rowid"));
      assertEquals("RLN_3", rs.getString("name"));
      assertEquals(3, rs.getInt("age"));
      assertTrue(rs.next());
      assertEquals(5, rs.getInt("rowid"));
      assertEquals("RLN_4", rs.getString("name"));
      assertEquals(4, rs.getInt("age"));
      rs.close();

      assertEquals(6, driver.getCurrentRowId("person"));
      driver.closeAll();
   }
}
