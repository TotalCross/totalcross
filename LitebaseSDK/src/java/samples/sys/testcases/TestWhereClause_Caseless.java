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
 * Tests where clauses taking string cases into consideration.
 */
public class TestWhereClause_Caseless extends TestCase
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
      catch (DriverException pe) {} // Table not found.
      try
      {
         driver.executeUpdate("drop table PERSON2");
      } 
      catch (DriverException pe) {} // Table not found.

      // Rows to insert.
      String [] insertRows =
      {
         "insert into PERSON values ('guilherme', 'HAZAN', 'Rio de Janeiro', 4400.50, 3400.80, 10, 6)",
         "insert into PERSON values ('raimundo', 'correa', 'Fortaleza', 3400.50, 3400.50, 11, 26)",
         "insert into PERSON values ('ricardo', 'souza', 'NATAL', 10400.50, 5000.20, 23, 23)",
         "insert into PERSON values ('cher', 'cher', 'Fortaleza', 1000.50, 3400.50, 4, 3)",
         "insert into PERSON values ('maria', 'jose', 'Paraty', 2001.34, 1000.35, 2, 6)",
         "insert into PERSON values ('ZICO', 'ZICO', 'Ouro Preto', 1000.51, 1000.51, 12, 0)",
         "insert into PERSON values ('roberto', 'Dinamite', 'Rio de Janeiro', 2222.51, 1.21, 10, 10)",
         "insert into PERSON values ('socrates', 'sampaio', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
         "insert into PERSON values ('paulo', 'dinamite', 'RIO DE JANEIRO', 2800.04, 1.21, 15, 12)",
         "insert into PERSON values ('leo', 'Souza', 'Natal', 2.50, 3400.50, 4, 5)",
         "insert into PERSON values ('maria', 'tatu', 'Foz do Igua�u', 4400.50, 3400.80, 10, 6)",
         "insert into PERSON values ('guilherme', 'renato', 'Porto Seguro', 3400.50, 3400.50, 11, 26)",
         "insert into PERSON values ('zanata', 'dinamite', 'Florianopolis', 10400.50, 5000.20, 23, 23)",
         "insert into PERSON values ('roberto', 'DINAMITE', 'Natal', 1000.50, 3400.50, 4, 3)",
         "insert into PERSON values ('maria', 'MARIA', 'Fortaleza', 2001.34, 1000.35, 2, 6)",
         "insert into PERSON values ('MARIA', 'severina', 'Porto Seguro', 1000.51, 1000.51, 12, 0)",
         "insert into PERSON values ('roberto', 'carlos', 'Ouro PRETO', 2222.51, 1.21, 10, 10)",
         "insert into PERSON values ('paulo', 'JOSE', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
         "insert into PERSON values ('cher', 'cher', 'rio de janeiro', 2800.04, 1.21, 15, 12)",
         "insert into PERSON values ('maria', 'joao', 'Rio de Janeiro', 2.50, 3400.50, 4, 5)"
      };
      int numRows = insertRows.length;
 
      // Caseless strings.
      // Creates table.
      driver.execute("create table PERSON (FIRST_NAME CHAR(30) NOCASE, LAST_NAME CHAR(40) NOCASE, CITY CHAR(30) NOCASE, SALARY_CUR DOUBLE, " 
                                                                    + "SALARY_PREV DOUBLE, YEARS_EXP_JAVA INT, YEARS_EXP_C INT )");

      // Inserts rows.
      int i = numRows;
      while (-- i >= 0)
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      testQueriesCaseless(driver); // Case-insensitive queries without index.

      // Case-insensitive queries with index.
      driver.execute("create index idx_name on PERSON(FIRST_NAME)");
      driver.execute("create index idx_city on PERSON(city)");
      driver.execute("create index idx_exp_C on PERSON(years_exp_C)");

      testQueriesCaseless(driver); // Repeats queries.
      
      // Case sensitive.
      // Creates a new table.
      driver.executeUpdate("drop table person");
      driver.execute("create table PERSON (FIRST_NAME CHAR(30), LAST_NAME CHAR(40), CITY CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, " 
                                                                                                 + "YEARS_EXP_JAVA INT, YEARS_EXP_C INT )");
      // Insert rows
      i = numRows;
      while (-- i >= 0)
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      testQueriesCase(driver); // Case-sensitive queries without index.
      
      // Case-sensitive queries with index.
      driver.execute("create index idx_name on PERSON(FIRST_NAME)");
      driver.execute("create index idx_city on PERSON(city)");
      driver.execute("create index idx_exp_C on PERSON(years_exp_C)");
      
      testQueriesCase(driver); // Repeats queries.
      
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
   
   /**
    * Queries the table with caseless strings.
    * 
    * @param driver The connection with Litebase.
    */
   private void testQueriesCaseless(LitebaseConnection driver)
   {
      // Exact key search.
      assertEquals(5, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria'"));
      assertEquals(15, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'maria'"));

      // Exact search with OR.
      assertEquals(9, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' or LAST_NAME = 'dinamite'"));
      assertEquals(5, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'roberto' or city = 'NATAL'"));

      // Exact search with AND.
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and city = 'Paraty'"));
      assertEquals(3, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and YEARS_EXP_C = 6"));

      // Non-exact search with OR.
      assertEquals(6, executeQuery(driver, "select * from PERSON where city = 'Ouro PRETO' or SALARY_CUR > 10000 or years_exp_C = 12"));
      assertEquals(10, executeQuery(driver, "select * from PERSON where last_name = 'Jose' or years_exp_Java > 10 or years_exp_C > 18"));

      // Non-exact search with AND.
      assertEquals(2, executeQuery(driver, "select * from PERSON where city = 'rio de Janeiro' and years_exp_Java > 10 and years_exp_C > 3"));

      // Complex queries.
      assertEquals(3, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 and years_exp_C = 10) or city = 'ouro preto' " 
                                                                                                              + "or first_name = 'zico'"));
      assertEquals(9, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' or (SALARY_CUR > 3000 and years_exp_C = 6) " 
                                                                                                               + "or city = 'natal'"));
      assertEquals(5, executeQuery(driver, "select * from PERSON where city = 'Rio de Janeiro' and (years_exp_Java > 10 " 
                                                                                            + "or years_exp_C > 3)"));
      assertEquals(8, executeQuery(driver, "select * from PERSON where first_name = last_name or last_name = 'dinamite'"));

      // Group by.
      assertEquals(8, executeQuery(driver, "select city from PERSON group by city"));
   }
   
   /**
    * Queries the table taking the strings case into consideration.
    * 
    * @param driver The connection with Litebase.
    */
   private void testQueriesCase(LitebaseConnection driver)
   {
      // Exact key search.
      assertEquals(4, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria'"));
      assertEquals(16, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'maria'"));

      // Exact search with OR.
      assertEquals(6, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' or LAST_NAME = 'dinamite'"));
      assertEquals(4, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'roberto' or city = 'NATAL'"));

      // Exact search with AND.
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and city = 'Paraty'"));
      assertEquals(3, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and YEARS_EXP_C = 6"));

      // Non-exact search with OR.
      assertEquals(5, executeQuery(driver, "select * from PERSON where city = 'Ouro PRETO' or SALARY_CUR > 10000 or years_exp_C = 12"));
      assertEquals(8, executeQuery(driver, "select * from PERSON where last_name = 'Jose' or years_exp_Java > 10 or years_exp_C > 18"));

      // Non-exact search with AND.
      assertEquals(0, executeQuery(driver, "select * from PERSON where city = 'rio de Janeiro' and years_exp_Java > 10 and years_exp_C > 3"));

      // Complex queries.
      assertEquals(2, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 and years_exp_C = 10) or city = 'ouro preto' " 
                                                                                                               + "or first_name = 'zico'"));
      assertEquals(5, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' or (SALARY_CUR > 3000 and years_exp_C = 6) " 
                                                                                                                 + "or city = 'natal'"));
      assertEquals(3, executeQuery(driver, "select * from PERSON where city = 'Rio de Janeiro' and (years_exp_Java > 10 " 
                                                                                             + "or years_exp_C > 3)"));
      assertEquals(5, executeQuery(driver, "select * from PERSON where first_name = last_name or last_name = 'dinamite'"));

      // Group by.
      assertEquals(12, executeQuery(driver, "select city from PERSON group by city"));
   }
}
