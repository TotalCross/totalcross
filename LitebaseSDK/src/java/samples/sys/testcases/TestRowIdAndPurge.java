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
 * Tests the relation between rowid, delete and purge.
 */
public class TestRowIdAndPurge extends TestCase
{
   /** 
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      int i = -1;
      String[] insertRows = // Rows to insert.
      {
         "insert into PERSON3 values ('guilherme', 'hazan', 4400.50, 3400.80, 10, 6)",
         "insert into PERSON3 values ('raimundo', 'correa', 3400.50, 3400.50, 11, 26)",
         "insert into PERSON3 values ('ricardo', 'zorba', 10400.50, 5000.20, 23, 23)",
         "insert into PERSON3 values ('cher', 'cher', 1000.50, 3400.50, 4, 3)", "insert into PERSON3 values ('lero', 'lero', 2001.34, 1000.35, 2, 6)",
         "insert into PERSON3 values ('zico', 'mengao', 1000.51, 1000.51, 12, 0)",
         "insert into PERSON3 values ('roberto', 'dinamite', 2222.51, 1.21, 10, 10)",
         "insert into PERSON3 values ('socrates', 'sampaio', 1111.50, 1111.50, 7, 11)",
         "insert into PERSON3 values ('paulo', 'falcao', 2800.04, 1.21, 15, 12)", "insert into PERSON3 values ('leo', 'junior', 2.50, 3400.50, 4, 5)" };

      int numRows = insertRows.length;

      try
      {
         driver.executeUpdate("drop table PERSON3");
      }
      catch (DriverException de) // Table not found.
      {
         output(de.getMessage());
      }

      // Creates table and index.
      driver.execute("create table PERSON3 (FIRST_NAME CHAR(30) NOCASE, LAST_NAME CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, "
                                                                                         + "YEARS_EXP_JAVA INT, YEARS_EXP_C INT )");
      driver.execute("create index idx_name on person3(First_Name)");

      // Inserts rows.
      while (++i < numRows)
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      assertEquals(11, driver.getCurrentRowId("person3"));
      
      // Verifies if the table is ok.
      ResultSet rs = driver.executeQuery("select rowid, first_name from person3 where first_name > 'A'");
      i = 0;
      while (++i <= numRows)
      {
         assertTrue(rs.next());
         assertEquals(i, rs.getInt("rowid"));
         assertEquals(i + "", rs.getString("rowid"));
      }
      assertEquals(numRows, rs.getRowCount());
      rs.close();

      // Tests getRecordCount().
      assertEquals(numRows, driver.getRowCountDeleted("PERSON3") + driver.getRowCount("person3"));
      assertEquals(numRows, driver.getRowCount("person3"));

      // Does some selects using like and "%".
      assertEquals(1, (rs = driver.executeQuery("select * from person3 where first_name like 'leo%'")).getRowCount());
      rs.close();
      assertEquals(1, (rs = driver.executeQuery("select * from person3 where last_name like '%orr%'")).getRowCount());
      rs.close();
      
      // Deletes some records.
      assertEquals(2, driver.executeUpdate("delete person3 where first_name like 'l%'"));

      // Tests getRecordCount() again.
      assertEquals(numRows, driver.getRowCountDeleted("person3") + driver.getRowCount("person3"));
      assertEquals(numRows - 2, driver.getRowCount("person3"));

      // Purges the table.
      assertEquals(2, driver.purge("PERSON3"));

      // Tests getRecordCount().
      assertEquals(numRows -= 2, driver.getRowCountDeleted("person3") + driver.getRowCount("person3"));
      assertEquals(numRows, driver.getRowCount("person3"));

      // Verifies if the table is ok.
      rs = driver.executeQuery("select rowid, first_name from person3 where first_name > 'A'");
      i = 0;
      while (++i <= numRows)
      {
         if (i == 5) 
            continue;
         assertTrue(rs.next());
         assertEquals(i, rs.getInt("rowid"));
      }
      assertEquals(numRows, rs.getRowCount());
      rs.close();
      driver.closeAll();
      
      driver = AllTests.getInstance("Test");
      assertEquals(11, driver.getCurrentRowId("person3"));
      
      // Inserts rows.
      i = -1;
      while (++i < numRows)
         assertEquals(1, driver.executeUpdate(insertRows[i]));
      
      numRows <<= 1;
      
      assertEquals(19, driver.getCurrentRowId("person3")); // 2 * 10 - 2 + 1 
      
      // Verifies if the table is ok.
      rs = driver.executeQuery("select rowid, first_name from person3 where first_name > 'A'");
      i = 0;
      while (++i <= numRows)
      {
         if (i == 5 || i == 10) 
            continue;
         assertTrue(rs.next());
         assertEquals(i, rs.getInt("rowid"));
      }
      rs.close();
      driver.closeAll();
   }
}
