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
 * Tests simple where clauses without indices.
 */
public class TestWhereClause_Basic extends TestCase
{
   /** 
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      try
      {
         driver.executeUpdate("drop table PERSON");
      } 
      catch (DriverException exception) {} // Table not found.

      String[] insertRows =  // Rows to insert.
      {
         "insert into PERSON values ('guilherme', 'hazan', 4400.50, 3400.80, 10, 6)",
         "insert into PERSON values ('raimundo', 'correa', 3400.50, 3400.50, 11, 26)",
         "insert into PERSON values ('ricardo', 'zorba', 10400.50, 5000.20, 23, 23)",
         "insert into PERSON values ('cher', 'cher', 1000.50, 3400.50, 4, 3)",
         "insert into PERSON values ('lero', 'lero', 2001.34, 1000.35, 2, 6)",
         "insert into PERSON values ('zico', 'mengao', 1000.51, 1000.51, 12, 0)",
         "insert into PERSON values ('roberto', 'dinamite', 2222.51, 1.21, 10, 10)",
         "insert into PERSON values ('socrates', 'sampaio', 1111.50, 1111.50, 7, 11)",
         "insert into PERSON values ('paulo', 'falcao', 2800.04, 1.21, 15, 12)",

         // with empty and negative values
         "insert into PERSON values ('leo', '', 2.50, 3400.50, -4, 5)" };
      int numRows = insertRows.length;

      // Creates table.
      driver.execute("create table PERSON (FIRST_NAME CHAR(30), LAST_NAME CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, YEARS_EXP_JAVA INT, " 
                                                                                                                        + "YEARS_EXP_C INT )");

      // Inserts rows.
      int i = -1;
      while (++i < numRows)
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      // Runs SQL query to test the where clause parsing and evaluation.
      // No where clause
      assertEquals(numRows, executeQuery(driver, "select * from PERSON"));

      // Empty strings
      assertEquals(1, executeQuery(driver, "select * from PERSON where last_name= ''"));

      // Parameter order test.
      ResultSet resultSet = driver.executeQuery("select first_name, salary_cur from person where rowid = 2");
      assertTrue(resultSet.first());
      assertEquals("raimundo", resultSet.getString(1));
      resultSet.close();
      assertTrue((resultSet = driver.executeQuery("select salary_cur, first_name from person where rowid = 2")).first());
      assertEquals("raimundo", resultSet.getString(2));
      resultSet.close();
      assertTrue((resultSet = driver.executeQuery("select * from person where rowid = 2")).first());

      // guich@503_10: asserts a bug just corrected.
      assertEquals("raimundo", resultSet.getString(1));

      // Integer comparison
      int greatThanCount = 4;
      int equalCount = 2;

      // Simple query, with integer comparison >=.
      assertEquals(greatThanCount + equalCount, executeQuery(driver, "select * from PERSON where years_exp_java >= 10"));

      // Simple query, with integer comparison >.
      assertEquals(greatThanCount, executeQuery(driver, "select * from PERSON where years_exp_java > 10"));

      // Simple query, with integer comparison =.
      assertEquals(equalCount, executeQuery(driver, "select * from PERSON where years_exp_java = 10"));

      // Simple query, with imediate negative numbers.
      assertEquals(1, executeQuery(driver, "select * from PERSON where years_exp_java = -4"));

      // Simple query, with imediate positive numbers.
      assertEquals(equalCount, executeQuery(driver, "select * from PERSON where years_exp_java = +10"));

      // Simple query, with integer comparison <>.
      assertEquals(numRows - equalCount, executeQuery(driver, "select * from PERSON where years_exp_java <> 10"));

      // Simple query, with integer comparison !=.
      assertEquals(numRows - equalCount, executeQuery(driver, "select * from PERSON where years_exp_java != 10"));
      
      // Simple query, with integer comparison <.
      assertEquals(numRows - equalCount - greatThanCount, executeQuery(driver, "select * from PERSON where years_exp_java < 10"));

      // Simple query, with integer comparison <=.
      assertEquals(numRows - greatThanCount, executeQuery(driver, "select * from PERSON where years_exp_java <= 10"));

      // Double comparison.
      greatThanCount = 2;
      equalCount = 3;

      // Simple query, with double comparison >=.
      assertEquals(greatThanCount + equalCount, executeQuery(driver, "select * from PERSON where salary_prev >= 3400.50"));

      // Simple query, with double comparison >.
      assertEquals(greatThanCount, executeQuery(driver, "select * from PERSON where salary_prev > 3400.50"));

      // Simple query, with double comparison =.
      assertEquals(equalCount, executeQuery(driver, "select * from PERSON where salary_prev = 3400.50"));

      // Simple query, with double comparison <>.
      assertEquals(numRows - equalCount, executeQuery(driver, "select * from PERSON where salary_prev <> 3400.50"));

      // Simple query, with double comparison !=.
      assertEquals(numRows - equalCount, executeQuery(driver, "select * from PERSON where salary_prev != 3400.50"));

      // Simple query, with double comparison <.
      assertEquals(numRows - equalCount - greatThanCount, executeQuery(driver, "select * from PERSON where salary_prev < 3400.50"));

      // Simple query, with double comparison <=.
      assertEquals(numRows - greatThanCount, executeQuery(driver, "select * from PERSON where salary_prev <= 3400.50"));

      // String comparison.
      greatThanCount = 4;
      equalCount = 1;

      // Simple query, with string comparison >=.
      assertEquals(greatThanCount + equalCount, executeQuery(driver, "select * from PERSON where FIRST_NAME >= 'raimundo'"));

      // Simple query, with string comparison >.
      assertEquals(greatThanCount, executeQuery(driver, "select * from PERSON where FIRST_NAME > 'raimundo'"));

      // Simple query, with string comparison =.
      assertEquals(equalCount, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'raimundo'"));

      // Simple query, with string comparison <>.
      assertEquals(numRows - equalCount, executeQuery(driver, "select * from PERSON where FIRST_NAME <> 'raimundo'"));

      // Simple query, with string comparison !=.
      assertEquals(numRows - equalCount, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'raimundo'"));

      // Simple query, with string comparison <.
      assertEquals(numRows - equalCount - greatThanCount, executeQuery(driver, "select * from PERSON where FIRST_NAME < 'raimundo'"));

      // Simple query, with string comparison <=.
      assertEquals(numRows - greatThanCount, executeQuery(driver, "select * from PERSON where FIRST_NAME <= 'raimundo'"));

      // String matching.
      assertEquals(3, executeQuery(driver, "select * from PERSON where FIRST_NAME like 'r%'")); // Starts with.
      assertEquals(2, executeQuery(driver, "select * from PERSON where last_name like '%ao'")); // Ends with.
      assertEquals(2, executeQuery(driver, "select * from PERSON where last_name like '%or%'")); // Contains.
      assertEquals(4, executeQuery(driver, "select * from PERSON where last_name like '%r%'")); // Contains.
      assertEquals(8, executeQuery(driver, "select * from PERSON where last_name not like '%or%'")); // Not match.
      assertEquals(4, executeQuery(driver, "select * from PERSON where last_name not like '%o%'")); // Not match.
      assertEquals(1, executeQuery(driver, "select * from PERSON where last_name like 'din%ite'")); // Starts and ends with.
      assertEquals(1, executeQuery(driver, "select * from PERSON where last_name like 'sampaio'")); // LIKE as equals.

      // Field comparison.
      // Simple query, with integer field comparison >=.
      assertEquals(6, executeQuery(driver, "select * from PERSON where years_exp_java >= years_exp_c"));

      // Simple query, with integer field comparison >.
      assertEquals(4, executeQuery(driver, "select * from PERSON where years_exp_java > years_exp_c"));

      // Simple query, with string field comparison =.
      assertEquals(2, executeQuery(driver, "select * from PERSON where FIRST_NAME = last_name"));

      // Simple query, with string field comparison <>.
      assertEquals(8, executeQuery(driver, "select * from PERSON where FIRST_NAME <> last_name"));

      // Simple query, with string field comparison !=.
      assertEquals(8, executeQuery(driver, "select * from PERSON where FIRST_NAME != last_name"));

      // Simple query, with double field comparison <.
      assertEquals(5, executeQuery(driver, "select * from PERSON where salary_prev < salary_cur"));

      // Simple query, with double field comparison <=.
      assertEquals(8, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur"));

      // Complex queries.
      assertEquals(2, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur and last_name like '%or%'"));
      assertEquals(8, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur or last_name like '%or%'"));
      assertEquals(4, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur and not years_exp_c > 10"));
      assertEquals(6, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 and years_exp_java >= 10) or FIRST_NAME > 'r'"));
      assertEquals(4, executeQuery(driver, "select * from PERSON where salary_prev < 3000 and (years_exp_java >= 10 or FIRST_NAME > 'r')"));
      assertEquals(2, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 or not years_exp_c >= years_exp_java) "
                                                                                           + "and not (years_exp_java >= 10 or FIRST_NAME > 'r')"));
      assertEquals(8, executeQuery(driver, "select * from PERSON where salary_prev < 3000 or ((not years_exp_c >= years_exp_java) "
                                                                                          + "and not years_exp_java >= 10) or FIRST_NAME > 'r'"));

      // Complex queries with update.
      assertEquals(8, driver.executeUpdate("update PERSON set FIRST_NAME = 'x' where salary_prev < 3000 or ((not years_exp_c >= years_exp_java) "
                                                                                  + "and not years_exp_java >= 10) or FIRST_NAME > 'r'"));
      assertEquals(8, driver.executeUpdate("delete PERSON where salary_prev < 3000 or ((not years_exp_c >= years_exp_java) " 
                                                                                     + "and not years_exp_java >= 10) or FIRST_NAME > 'r'"));

      // Updates the whole table.
      assertEquals(2, driver.executeUpdate("update PERSON set FIRST_NAME = 'Michelle'"));
      driver.closeAll();
   }

   /** 
    * Tests a query.
    *
    * @param driver The connection with Litebase.
    * @param sql The query to be executed.
    * @return The number of rows returned by the query.
    */
   private int executeQuery(LitebaseConnection driver, String sql)
   {
      int count = 0;
      ResultSet resultSet = driver.executeQuery(sql);
      while (resultSet.next())
         count++;
      resultSet.close();
      return count;
   }
}
