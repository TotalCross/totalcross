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
 * Tests aggregation functions.
 */
public class TestSelectClause_AggFunctions extends TestCase
{
   /** 
    * The mais test method.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); 

      try
      {
         driver.executeUpdate("drop table PERSON");
      } 
      catch (DriverException exception) {} // Table not found.

      // Rows to insert
      String [] insertRows =
      {
         "insert into PERSON values ('getulio', 'Sorocaba', 'sp', 2778.11, 42)",
         "insert into PERSON values ('guilherme', 'Rio de Janeiro', 'rj', 4400.50, 35)",
         "insert into PERSON values ('roberto', 'Rio de Janeiro', 'rj', 3900.50, 72)",
         "insert into PERSON values ('joao', 'Petropolis', 'rj', 5004.26, 44)",
         "insert into PERSON values ('ricardo', 'Sao Paulo', 'sp', 2876.22, 23)",
         "insert into PERSON values ('janos', 'Ribeirao Preto', 'sp', 10082.12, 65)",
         "insert into PERSON values ('socrates', 'Sao Paulo', 'sp', 5100.12, 48)",
         "insert into PERSON values ('dudamel', 'Ribeirao Preto', 'sp', 3999.34, 34)",
         "insert into PERSON values ('felipe', 'Ribeirao Preto', 'sp', 2778.11, 42)",
         "insert into PERSON values ('carmelo', 'Porto Alegre', 'rs', 6789.66, 36)",
         "insert into PERSON values ('breno', 'Uruguaiana', 'rs', 4441.88, 26)",
         "insert into PERSON values ('mauricio', 'Rio de Janeiro', 'rj', 7777.88, 38)",
         "insert into PERSON values ('maria', 'Sorocaba', 'sp', 3456.22, 28)",
         "insert into PERSON values ('marta', 'Petropolis', 'rj', 8788.22, 19)",
         "insert into PERSON values ('renato', 'Rio de Janeiro', 'rj', 5555.33, 44)",
         "insert into PERSON values ('nadia', 'Porto Alegre', 'rs', 4006.32, 62)"
      };

      int numRows = insertRows.length;

      // Creates the table.
      driver.execute("create table PERSON (FIRST_NAME CHAR(30), CITY CHAR(30), STATE CHAR(2), SALARY FLOAT, AGE SHORT)");

      // Inserts rows.
      int i = -1;
      while (++i < numRows)
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      // Runs SQL queries to test the where clause parsing and evaluation.

      // No where clause and select with wildcard.
      assertEquals(numRows, executeQuery(driver, "select * from PERSON"));
      
      // Simple where clause.
      assertEquals(7, executeQuery(driver, "select first_name, age, CITY from PERSON where state ='sp'"));
      
      // Simple where clause with aliases.
      assertEquals(7, executeQuery(driver, "select first_name as nome, age as idade, CITY from PERSON where state ='sp'"));
      
      // Simple COUNT(*)
      ResultSet resultSet = driver.executeQuery("select COUNT(*) as total_records from PERSON");
      assertTrue(resultSet.next());
      
      assertEquals(numRows, resultSet.getInt("total_records")); // Asserts that query worked properly.
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests MAX() and MIN() for string types. 
      assertTrue((resultSet = driver.executeQuery("select max(FIRST_NAME) as max_name, min(FIRST_NAME) as min_name, MAX(city) as max_city, " 
                                                                                    + "MIN(city) as min_city from PERSON")).next());
      assertEquals("socrates", resultSet.getString("max_name"));
      assertEquals("breno", resultSet.getString("min_name"));
      assertEquals("Petropolis", resultSet.getString("min_city"));
      assertEquals("Uruguaiana", resultSet.getString("max_city"));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests MAX() and MIN() for string types with having. 
      assertTrue((resultSet = driver.executeQuery("select max(FIRST_NAME) as max_name, count(*) as total, state from PERSON group by state " +
                                                                                                         "having max_name >= 'nadia'")).next()); 
      assertEquals(3, resultSet.getRowCount());
      assertEquals("roberto", resultSet.getString(1));
      assertEquals(6, resultSet.getInt(2));
      assertEquals("rj", resultSet.getString(3));
      assertTrue(resultSet.next());
      assertEquals("nadia", resultSet.getString(1));
      assertEquals(3, resultSet.getInt(2));
      assertEquals("rs", resultSet.getString(3));
      assertTrue(resultSet.next());
      assertEquals("socrates", resultSet.getString(1));
      assertEquals(7, resultSet.getInt(2));
      assertEquals("sp", resultSet.getString(3));
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertTrue((resultSet = driver.executeQuery("select min(FIRST_NAME) as min_name, count(*) as total, state from PERSON group by state " + 
                                                                                                         "having min_name <= 'guilherme'")).next());
      assertEquals(3, resultSet.getRowCount());
      assertEquals("guilherme", resultSet.getString(1));
      assertEquals(6, resultSet.getInt(2));
      assertEquals("rj", resultSet.getString(3));
      assertTrue(resultSet.next());
      assertEquals("breno", resultSet.getString(1));
      assertEquals(3, resultSet.getInt(2));
      assertEquals("rs", resultSet.getString(3));
      assertTrue(resultSet.next());
      assertEquals("dudamel", resultSet.getString(1));
      assertEquals(7, resultSet.getInt(2));
      assertEquals("sp", resultSet.getString(3));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Simple COUNT(*) with where clause.
      assertTrue((resultSet = driver.executeQuery("select COUNT(*) as total_records from PERSON where state = 'sp'")).next());
      assertEquals(7, resultSet.getInt("total_records")); // Asserts that query worked properly.
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Aggregated functions without where clause or group by function.
      assertTrue((resultSet = driver.executeQuery("select COUNT(*) as total, min(age) as min_age, max(age) as max_age, sum(salary) as sum_salary, " 
                                                                                               + "avg(salary) as avg_salary from PERSON")).next());
      
      // Asserts that query worked properly.
      assertEquals(16, resultSet.getInt("total"));
      assertEquals(19, resultSet.getShort("min_age"));
      assertEquals(72, resultSet.getShort("max_age"));
      assertEquals(81734.79, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(5108.42, resultSet.getDouble("avg_salary"), 1e-2);
      assertFalse(resultSet.next()); 
      resultSet.close();
      
      // Where clause with aggregated functions and no group by clause.
      assertTrue((resultSet = driver.executeQuery("select COUNT(*) as total, min(age) as min_age, max(age) as max_age, sum(salary) as sum_salary, " 
                                                                            + "avg(salary) as avg_salary from PERSON where state ='sp'")).next());

      // Asserts that query worked.
      assertEquals(7, resultSet.getInt("total"));
      assertEquals(23, resultSet.getShort("min_age"));
      assertEquals(65, resultSet.getShort("max_age"));
      assertEquals(31070.25, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(4438.60, resultSet.getDouble("avg_salary"), 1e-2);
      assertFalse(resultSet.next()); 
      resultSet.close();
      
      // Select with aggregated functions and simple columns with group by clause.
      assertTrue((resultSet = driver.executeQuery("select CITY, state, COUNT(*) as total, min(age) as min_age, avg(age) as avg_age, "+ 
                                                  "sum(salary) as sum_salary, avg(salary) as avg_salary from PERSON group by state,CITY")).next());

      // Asserts that the query worked by checking the values of each record.
      assertEquals(7, resultSet.getRowCount());

      // First record: Petropolis RJ 2 19 31.5 13792.48 6896.2399999999998.
      assertEquals(2, resultSet.getInt("total"));
      assertEquals(19, resultSet.getShort("min_age"));
      assertEquals("rj", resultSet.getString("state"));
      assertEquals("Petropolis", resultSet.getString("CITY"));
      assertEquals(31.501, resultSet.getDouble("avg_age"), 1e-2);
      assertEquals(13792.48, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(6896.24, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());
      
      // Second record: Rio de Janeiro RJ 4 35 47.25 21634.209999999999 5408.5524999999998.
      assertEquals(4, resultSet.getInt("total"));
      assertEquals(35, resultSet.getShort("min_age"));
      assertEquals("rj", resultSet.getString("state"));
      assertEquals("Rio de Janeiro", resultSet.getString("CITY"));
      assertEquals(47.251, resultSet.getDouble("avg_age"), 1e-2);
      assertEquals(21634.21, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(5408.55, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());
      
      // Third record: Porto Alegre RS 2 36 49.0 10795.98 5397.9899999999998.
      assertEquals(2, resultSet.getInt("total"));
      assertEquals(36, resultSet.getShort("min_age"));
      assertEquals("rs", resultSet.getString("state"));
      assertEquals("Porto Alegre", resultSet.getString("CITY"));
      assertEquals(49.01, resultSet.getDouble("avg_age"), 1e-2);
      assertEquals(10795.98, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(5397.99, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());

      // Fourth record: Uruguaiana RS 1 26 26.0 4441.8800000000001 4441.8800000000001.
      assertEquals(1, resultSet.getInt("total"));
      assertEquals(26, resultSet.getShort("min_age"));
      assertEquals("rs", resultSet.getString("state"));
      assertEquals("Uruguaiana", resultSet.getString("CITY"));
      assertEquals(26.0, resultSet.getDouble("avg_age"), 1e-1);
      assertEquals(4441.88, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(4441.88, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());

      // Fifth record: Ribeirao Preto SP 3 34 47.0 16859.57 5619.8566666666666.
      assertEquals(3, resultSet.getInt("total"));
      assertEquals(34, resultSet.getShort("min_age"));
      assertEquals("sp", resultSet.getString("state"));
      assertEquals("Ribeirao Preto", resultSet.getString("CITY"));
      assertEquals(47.0, resultSet.getDouble("avg_age"), 1e-1);
      assertEquals(16859.57, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(5619.86, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());

      // Sixth record: Sao Paulo SP 2 23 35.5 7976.3400000000001 3988.1700000000001.
      assertEquals(2, resultSet.getInt("total"));
      assertEquals(23, resultSet.getShort("min_age"));
      assertEquals("sp", resultSet.getString("state"));
      assertEquals("Sao Paulo", resultSet.getString("CITY"));
      assertEquals(35.5, resultSet.getDouble("avg_age"), 1e-1);
      assertEquals(7976.35, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(3988.18, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());

      // Seventh record: Sorocaba SP 2 28 35.0 6234.3299999999999 3117.165.
      assertEquals(2, resultSet.getInt("total"));
      assertEquals(28, resultSet.getShort("min_age"));
      assertEquals("sp", resultSet.getString("state"));
      assertEquals("Sorocaba", resultSet.getString("CITY"));
      assertEquals(35.0, resultSet.getDouble("avg_age"), 1e-1);
      assertEquals(6234.33,resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(3117.16, resultSet.getDouble("avg_salary"), 1e-2);
      assertFalse(resultSet.next());
      resultSet.close();

      // Select with order by.
      assertTrue((resultSet = driver.executeQuery("select first_name as name, CITY from PERSON order by age, first_name desc")).next());
      
      // Asserts that the query worked by checking the values of each record.
      assertEquals(16, resultSet.getRowCount());
      assertEquals("marta", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("ricardo", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("breno", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("maria", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("dudamel", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("guilherme", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("carmelo", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("mauricio", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("getulio", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("felipe", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("renato", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("joao", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("socrates", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("nadia", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("janos", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("roberto", resultSet.getString("name"));
      assertFalse(resultSet.next());
      resultSet.close();

      // Select with order by and where clause
      assertTrue((resultSet = driver.executeQuery("select first_name as name, CITY from PERSON where state = 'sp' or state = 'rs' " 
                                                                                     + "order by CITY, first_name desc")).next());
      
      // Asserts that the query worked, by checking the values of each record.
      assertEquals(10, resultSet.getRowCount());
      
      assertEquals("nadia", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("carmelo", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("janos", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("felipe", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("dudamel", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("socrates", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("ricardo", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("maria", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("getulio", resultSet.getString("name"));
      assertTrue(resultSet.next());
      assertEquals("breno", resultSet.getString("name"));
      assertFalse(resultSet.next());
      resultSet.close();

      // Select with where clause and group by.
      assertTrue((resultSet = driver.executeQuery("select COUNT(*) as total, CITY from PERSON where age > 26 and salary > 3000F group by CITY"))
                                                                                                                                         .next());

      // Asserts that the query worked by checking the values of each record.
      assertEquals(6, resultSet.getRowCount());
      assertEquals("Petropolis", resultSet.getString("CITY"));
      assertEquals(1, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Porto Alegre", resultSet.getString("CITY"));
      assertEquals(2, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Ribeirao Preto", resultSet.getString("CITY"));
      assertEquals(2, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Rio de Janeiro",resultSet.getString("CITY"));
      assertEquals(4, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Sao Paulo", resultSet.getString("CITY"));
      assertEquals(1, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Sorocaba", resultSet.getString("CITY"));
      assertEquals(1, resultSet.getInt("total"));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Select with order by, group by, and where clause.
      assertTrue((resultSet = driver.executeQuery("select COUNT(*) as total, CITY from PERSON where state = 'sp' or state = 'rj' " + 
                                                                                                            "group by CITY order by CITY")).next());

      // Asserts that the query worked by checking the values of each record.
      assertEquals(5, resultSet.getRowCount());
      assertEquals("Petropolis", resultSet.getString("CITY"));
      assertEquals(2, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Ribeirao Preto", resultSet.getString("CITY"));
      assertEquals(3, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Rio de Janeiro", resultSet.getString("CITY"));
      assertEquals(4, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Sao Paulo", resultSet.getString("CITY"));
      assertEquals(2, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Sorocaba", resultSet.getString("CITY"));
      assertEquals(2, resultSet.getInt("total"));
      assertFalse(resultSet.next());
      resultSet.close();

      // Select with group by but no aggregated function to simulate the usage of a "distinct" in the select.
      assertTrue((resultSet = driver.executeQuery("select CITY from PERSON where state = 'sp' or state = 'rj' group by CITY")).next());

      // Asserts that the query worked by checking the values of each record.
      assertEquals(5, resultSet.getRowCount());
      assertEquals("Petropolis", resultSet.getString("CITY"));
      assertTrue(resultSet.next());
      assertEquals("Ribeirao Preto", resultSet.getString("CITY"));
      assertTrue(resultSet.next());
      assertEquals("Rio de Janeiro", resultSet.getString("CITY"));
      assertTrue(resultSet.next());
      assertEquals("Sao Paulo", resultSet.getString("CITY"));
      assertTrue(resultSet.next());
      assertEquals("Sorocaba", resultSet.getString("CITY"));
      assertFalse(resultSet.next());
      resultSet.close();

      // Select with where clause, group by, having, and order by.
      assertTrue((resultSet = driver.executeQuery("select CITY, COUNT(*) as total from PERSON where state = 'sp' or state = 'rj' group by CITY " + 
                                                                                                   "having total > 2 order by CITY")).next());

      // Asserts that the query worked by checking the values of each record.
      assertEquals(2, resultSet.getRowCount());
      assertEquals("Ribeirao Preto", resultSet.getString("CITY"));
      assertEquals(3, resultSet.getInt("total"));
      assertTrue(resultSet.next());
      assertEquals("Rio de Janeiro", resultSet.getString("CITY"));
      assertEquals(4, resultSet.getInt("total"));
      assertFalse(resultSet.next());
      resultSet.close();

      // Select with aggregated functions and simple columns with group by and having clauses.
      assertTrue((resultSet = driver.executeQuery("select CITY, state, COUNT(*) as total, min(age) as min_age, avg(age) as avg_age, " + 
        "sum(salary) as sum_salary, avg(salary) as avg_salary from PERSON group by state, CITY having min_age > 25 and sum_salary < 15000")).next());

      // Asserts that the query worked by checking the values of each record.
      assertEquals(3, resultSet.getRowCount());

      // First record: Porto Alegre RS 2 36 49.0 10795.98 5397.9899999999998.
      assertEquals(2, resultSet.getInt("total"));
      assertEquals(36, resultSet.getShort("min_age"));
      assertEquals("rs", resultSet.getString("state"));
      assertEquals("Porto Alegre", resultSet.getString("CITY"));
      assertEquals(49.01, resultSet.getDouble("avg_age"), 1e-2);
      assertEquals(10795.98, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(5397.99, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());
      
      // Second record: Uruguaiana RS 1 26 26.0 4441.8800000000001 4441.8800000000001.
      assertEquals(1, resultSet.getInt("total"));
      assertEquals(26, resultSet.getShort("min_age"));
      assertEquals("rs", resultSet.getString("state"));
      assertEquals("Uruguaiana", resultSet.getString("CITY"));
      assertEquals(26.0, resultSet.getDouble("avg_age"), 1e-1);
      assertEquals(4441.88, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(4441.88, resultSet.getDouble("avg_salary"), 1e-2);
      assertTrue(resultSet.next());
      
      // Third record: Sorocaba SP 2 28 35.0 6234.3299999999999 3117.165.
      assertEquals(2, resultSet.getInt("total"));
      assertEquals(28, resultSet.getShort("min_age"));
      assertEquals("sp", resultSet.getString("state"));
      assertEquals("Sorocaba", resultSet.getString("CITY"));
      assertEquals(35.0, resultSet.getDouble("avg_age"), 1e-1);
      assertEquals(6234.33, resultSet.getDouble("sum_salary"), 1e-2);
      assertEquals(3117.16, resultSet.getDouble("avg_salary"), 1e-2);
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests a max in a table with many columns.
      if (driver.exists("cliente"))
         driver.executeUpdate("drop table cliente");
      driver.execute("create table cliente(clienteEmpresaId int not null, clienteId int not null, cpfCnpj varchar(14), tipoPessoa varchar(1), " + 
            "nome varchar(60), fantasia varchar(40), contato varchar(60), inscricaoRg varchar(18), consFinal varchar(1), limCredito double, " + 
            "endereco varchar(70), bairro varchar(40), cidadeId varchar(5), cep varchar(8), telefone varchar(14), email varchar(100), ramoId int, " + 
            "usuarioId int, usuarioEmpresaId int, sincronizado varchar(1), duplicatas double, prestadorId int, prestadorEmpresaId int, " + 
            "condPgtoEmpresaId int, condPgtoId int, numero varchar(10), descontoMax double)");
      assertFalse((resultSet = driver.prepareStatement(" SELECT max(clienteId) as id FROM cliente ").executeQuery()).first());
      resultSet.close();
      driver.closeAll();
      driver = AllTests.getInstance("Test"); 
      assertFalse((resultSet = driver.prepareStatement(" SELECT max(clienteId) as id FROM cliente ").executeQuery()).first());
      resultSet.close();
      driver.closeAll();
   }

   /**
    *  Executes a query and returns the total number of record.
    *  
    * @param driver The connection with Litebase.
    * @param sql The query.
    * @return The number of records of the query.
    */
   private int executeQuery(LitebaseConnection driver, String sql)
   {
      int count = 0;
      output('\n' + sql);
      ResultSet rs = driver.executeQuery(sql);
      while (rs.next())
         count++;     
      rs.close();
      return count;
   }
}
