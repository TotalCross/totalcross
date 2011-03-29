/********************************************************************************* 
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: TestPalmMemoryCard.java,v 1.9.4.1.2.1.4.26 2011-01-03 20:20:35 juliana Exp $
package samples.sys.testcases;

import litebase.*;
import totalcross.unit.*;
import totalcross.sys.*;
import totalcross.io.*;

/**
 * Almost like the TestWhereClause_Indexes, but targetted for the memory card.
 */
public class TestPalmMemoryCard extends TestCase
{
   /**
    * This test is only for Palm OS.
    */
   public void testRun()
   {
      testFolder("Tesa", "2:"); // The memory card root.
      testFolder("Tesb", "2:\\dba"); // A memory card folder.
      testFolder("Tesc", "-1:\\"); // The memory card root.
      testFolder("Tesd", "-1:\\dbb\\"); // A memory card folder.
   }

   /**
    * Does the test on an especific folder of the memory card.
    * 
    * @param crid The creator id for the tables.
    * @param dataPath The folder for the tables.
    */
   private void testFolder(String crid, String dataPath)
   {
      try
      {
         // Cheks to see if the platform is Palm and if the memory card is inserted.
         if (!Settings.platform.startsWith(Settings.PALMOS) || !File.isCardInserted(-1))
         {
            output("Skipping test...");
            output("The platform is not Palm, Palm Card is not available, or not inserted");
            return;
         }
      }
      catch (IllegalArgumentIOException exception)
      {
         fail("Error when checking the device: " + exception.getMessage());
      }

      output(crid + " " + dataPath);
      LitebaseConnection driver = AllTests.getInstance(crid, dataPath);
      
      assertGreater(driver.getSlot(), 1); // Checks that the card is being used.
      
      // Drops the table.
      int ini = Vm.getTimeStamp();
      try
      {
         driver.executeUpdate("drop table PERSON");
      }
      catch (DriverException exception)
      {
         if (driver.exists("person")) // If the table exists, it must have been dropped. 
            fail("Error when dropping table: " + exception.getMessage());
      }
      output("drop. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // The renamed table must also be erased.
      if (driver.exists("person2"))
         driver.executeUpdate("drop table PERSON2");
      
      // Rows to insert.
      String[] insertRows = { "insert into PERSON values ('guilherme', 'Rio de Janeiro', 4400.50, 3400.80, 10, 6)",
                              "insert into PERSON values ('raimundo', 'Fortaleza', 3400.50, 3400.50, 11, 26)",
                              "insert into PERSON values ('ricardo', 'Natal', 10400.50, 5000.20, 23, 23)",
                              "insert into PERSON values ('cher', 'Fortaleza', 1000.50, 3400.50, 4, 3)", 
                              "insert into PERSON values ('maria', 'Paraty', 2001.34, 1000.35, 2, 6)",
                              "insert into PERSON values ('zico', 'Ouro Preto', 1000.51, 1000.51, 12, 0)",
                              "insert into PERSON values ('roberto', 'Rio de Janeiro', 2222.51, 1.21, 10, 10)",
                              "insert into PERSON values ('socrates', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
                              "insert into PERSON values ('paulo', 'Rio de Janeiro', 2800.04, 1.21, 15, 12)",
                              "insert into PERSON values ('leo', 'Natal', 2.50, 3400.50, 4, 5)",
                              "insert into PERSON values ('maria', 'Foz do Iguaçu', 4400.50, 3400.80, 10, 6)",
                              "insert into PERSON values ('guilherme', 'Porto Seguro', 3400.50, 3400.50, 11, 26)",
                              "insert into PERSON values ('zanata', 'Florianopolis', 10400.50, 5000.20, 23, 23)",
                              "insert into PERSON values ('roberto', 'Natal', 1000.50, 3400.50, 4, 3)",
                              "insert into PERSON values ('maria', 'Fortaleza', 2001.34, 1000.35, 2, 6)",
                              "insert into PERSON values ('maria', 'Porto Seguro', 1000.51, 1000.51, 12, 0)",
                              "insert into PERSON values ('roberto', 'Ouro Preto', 2222.51, 1.21, 10, 10)",
                              "insert into PERSON values ('paulo', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
                              "insert into PERSON values ('cher', 'Rio de Janeiro', 2800.04, 1.21, 15, 12)",
                              "insert into PERSON values ('maria', 'Rio de Janeiro', 2.50, 3400.50, 4, 5)" };

      int numRows = insertRows.length;

      // Creates the table.
      ini = Vm.getTimeStamp();
      try
      {
         driver.execute(
         "create table PERSON (FIRST_NAME CHAR(30), CITY CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, YEARS_EXP_JAVA INT, YEARS_EXP_C INT )");
      }
      catch (DriverException exception)
      {
         fail("Error when creating table: " + exception.getMessage());
      }
      output("create table. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // Inserts rows.
      ini = Vm.getTimeStamp();
      driver.setRowInc("person", numRows);
      while (--numRows >= 0)
         assertEquals(1, driver.executeUpdate(insertRows[numRows]));
      driver.setRowInc("person", -1);
      output("inserts. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // Runs SQL queries to test the WHERE clause evaluation using indexes usage.
      // Queries without index.
      // Exact key search
      assertEquals(2, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'guilherme'"));
      assertEquals(18, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'guilherme'"));

      // Exact search with OR.
      assertEquals(5, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'guilherme' or FIRST_NAME = 'roberto'"));

      // Exact search with AND.
      assertEquals(7, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'roberto' or city = 'Porto Seguro'"));
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and city = 'Paraty'"));
      assertEquals(3, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and YEARS_EXP_C = 6"));
      assertEquals(2, executeQuery(driver, "select * from PERSON where (salary_prev < 3000.0d or years_exp_C = 10) and "
                                                                     + "city = 'Porto Seguro' and years_exp_C > 2"));

      // Queries with index.
      // Indices creation.
      ini = Vm.getTimeStamp();
      driver.execute("create index idx_name on PERSON(FIRST_NAME)");
      driver.execute("create index idx_city on PERSON(city)");
      driver.execute("create index idx_exp_C on PERSON(years_exp_C)");
      output("index creation. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // Repeats the queries.
      // Exact key search.
      assertEquals(2, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'guilherme'"));
      assertEquals(18, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'guilherme'"));

      // Exact search with OR.
      assertEquals(5, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'guilherme' or FIRST_NAME = 'roberto'"));
      assertEquals(7, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'roberto' or city = 'Porto Seguro'"));

      // Exact search with AND.
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and city = 'Paraty'"));
      assertEquals(3, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and YEARS_EXP_C = 6"));

      // Update with indexes.
      ini = Vm.getTimeStamp();
      assertEquals(8, driver.executeUpdate("update PERSON set FIRST_NAME = 'x' where FIRST_NAME = 'maria' or (SALARY_CUR > 3000.D "
            + "and years_exp_C = 6) or city = 'Fortaleza'"));
      output("update. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // Delete with indices.
      ini = Vm.getTimeStamp();
      assertEquals(5, driver.executeUpdate("delete PERSON where city = 'Rio de Janeiro' and (years_exp_Java > 10 or years_exp_C > 3)"));
      output("delete. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // Tests rename.
      ini = Vm.getTimeStamp();
      assertEquals(0, driver.executeUpdate("ALTER TABLE person RENAME TO Person2"));
      output("rename. elapsed " + (Vm.getTimeStamp() - ini) + "ms");

      // Tests table recovering in the palm memory card.
      driver.closeAll();
      driver = AllTests.getInstance(crid, dataPath);
      try
      {
         File dbFile = new File(Convert.appendPath(driver.getSourcePath(), crid + "-person2.db"), File.READ_WRITE, driver.getSlot());
         byte[] oneByte = new byte[1];
         
         // Pretends that the table was not closed correctly.   
         dbFile.setPos(6);
         dbFile.readBytes(oneByte, 0, 1);
         oneByte[0] = (byte)(oneByte[0] & 2);
         dbFile.setPos(6);
         dbFile.writeBytes(oneByte, 0, 1);
         dbFile.close();
         assertFalse(driver.recoverTable("person2"));
      }
      catch (IOException excetion)
      {
         fail("1");
      }
      
      try // Convert will fail because the table version is the current one.
      {
         driver.convert("person2");
         fail("2");
      }
      catch (DriverException excetion) {}
      
      // Tests close all.
      ini = Vm.getTimeStamp();
      driver.closeAll();
      output("closeall. elapsed " + (Vm.getTimeStamp() - ini) + "ms");
   }

   /**
    * Execute a query and returns the total numbers returned.
    * 
    * @param driver The connection with Litebase.
    * @param sql The query to be executed.
    * @return The number of rows returned by the query.
    */
   private int executeQuery(LitebaseConnection driver, String sql)
   {
      int count = 0;
      output('\n' + sql);

      int ini = Vm.getTimeStamp();
      ResultSet rs = driver.executeQuery(sql);

      while (rs.next()) // Counts the results.
      {
         output(rs.getString("FIRST_NAME") + ',' + rs.getString("city"));
         count++;
      }

      output("elapsed " + (Vm.getTimeStamp() - ini) + "ms");
      rs.close();
      return count;
   }
}
