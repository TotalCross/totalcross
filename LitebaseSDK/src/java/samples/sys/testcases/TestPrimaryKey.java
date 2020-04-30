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
 * Tests Litebase primary key.
 */
public class TestPrimaryKey extends TestCase
{
	LitebaseConnection driver = AllTests.getInstance("Test");
	String[] insertRows =  // Rows to insert.
   {
      "insert into PERSON_PK values ('guilherme', 'Rio de Janeiro', 4400.50, 3400.80, 10, 6)",
      "insert into PERSON_PK values ('raimundo', 'Fortaleza', 3400.50, 3400.50, 11, 26)",
      "insert into PERSON_PK values ('ricardo', 'Natal', 10400.50, 5000.20, 23, 23)",
      "insert into PERSON_PK values ('cher', 'Fortaleza', 1000.50, 3400.50, 4, 3)",
      "insert into PERSON_PK values ('maria', 'Paraty', 2001.34, 1000.35, 2, 6)",
      "insert into PERSON_PK values ('zico', 'Ouro Preto', 1000.51, 1000.51, 12, 0)",
      "insert into PERSON_PK values ('roberto', 'Rio de Janeiro', 2222.51, 1.21, 10, 10)",
      "insert into PERSON_PK values ('socrates', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
      "insert into PERSON_PK values ('paulo', 'Rio de Janeiro', 2800.04, 1.21, 15, 12)",
      "insert into PERSON_PK values ('leo', 'Natal', 2.50, 3400.50, 4, 5)",
      "insert into PERSON_PK values ('maria', 'Foz do Igua�u', 4400.50, 3400.80, 10, 6)",
      "insert into PERSON_PK values ('guilherme', 'Porto Seguro', 3400.50, 3400.50, 11, 26)",
      "insert into PERSON_PK values ('zanata', 'Florianopolis', 10400.50, 5000.20, 23, 23)",
      "insert into PERSON_PK values ('roberto', 'Natal', 1000.50, 3400.50, 4, 3)",
      "insert into PERSON_PK values ('maria', 'Fortaleza', 2001.34, 1000.35, 2, 6)",
      "insert into PERSON_PK values ('maria', 'Porto Seguro', 1000.51, 1000.51, 12, 0)",
      "insert into PERSON_PK values ('roberto', 'Ouro Preto', 2222.51, 1.21, 10, 10)",
      "insert into PERSON_PK values ('paulo', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
      "insert into PERSON_PK values ('cher', 'Rio de Janeiro', 2800.04, 1.21, 15, 12)",
      "insert into PERSON_PK values ('maria', 'Rio de Janeiro', 2.50, 3400.50, 4, 5)"
   };
   int numRows = insertRows.length;

   /**
    * The main test method.
    */
   public void testRun()
   {
      try
      {
         driver.executeUpdate("drop table PERSON_PK");
      } 
      catch (DriverException exception) {} // Table not found.

      try // Tries to create a table with 2 primary keys.
      {
         driver.execute("create table PERSON_PK (FIRST_NAME CHAR(30) PRIMARY KEY, CITY CHAR(30), SALARY_CUR DOUBLE PRIMARY KEY, SALARY_PREV DOUBLE, " 
                                                                                              + "YEARS_EXP_JAVA INT, YEARS_EXP_C INT )");
         fail("1");
      } 
      catch (SQLParseException exception) {}

      // Creates table with the first name being the primary key.
      test_person_PK("create table PERSON_PK (FIRST_NAME CHAR(30) PRIMARY KEY, CITY CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, " 
                                                                                           + "YEARS_EXP_JAVA INT, YEARS_EXP_C INT )");
      test_person_PK("create table PERSON_PK (FIRST_NAME CHAR(30), CITY CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, YEARS_EXP_JAVA INT, "
                                                                                                  + "YEARS_EXP_C INT, PRIMARY KEY(FIRST_NAME))");

      output("Testing updating the same key");
      if (driver.exists("tabps"))
         driver.executeUpdate("drop table tabps");
      driver.execute("create table tabps (idade int primary key, name char(30))");
      driver.executeUpdate("insert into tabps values (5, 'cacule')");
      driver.executeUpdate("insert into tabps values (6, 'vera')");
      assertEquals(1, driver.executeUpdate("update tabps set idade = 5, name = 'michelle' where idade = 5")); // guich@564_18

      // Tests that prepared statement won't create a duplicate primary key.
      PreparedStatement ps = driver.prepareStatement("insert into tabps values (?, ?)");
      ps.setString(0, "1");
      ps.setString(1, "1");
      ps.executeUpdate();
      ps.setInt(0, 2);
      ps.setString(1, "2");
      ps.executeUpdate();
      ResultSet resultSet = driver.executeQuery("select idade from tabps where name = '1' or name = '2'");
      resultSet.next();
      assertEquals(1, resultSet.getInt(1));
      resultSet.next();
      assertEquals(2, resultSet.getInt(1));
      resultSet.close();
      
      try // Repeated primary key.
      {
         driver.executeUpdate("update tabps set idade = 5, name = 'michelle' where idade = 6"); // guich@564_19
         fail("2");
      } 
      catch (PrimaryKeyViolationException exception) {}
      
      driver.executeUpdate("drop table tabps");
      driver.execute("create table tabps (x int)");
      driver.execute("create index idx on tabps(x)");
      driver.execute("create index idx on tabps(x, rowid)");
      
      // It is not possible to add a primary key to a column that has already an index or columns that already compose an index in the same order of
      // the key columns.      
      try
      {
         driver.executeUpdate("alter table tabps add primary key (x)");
         fail("3");
      }
      catch (AlreadyCreatedException exception) {}
      try
      {
         driver.executeUpdate("alter table tabps add primary key (x, rowid)");
         fail("4");
      }
      catch (AlreadyCreatedException exception) {}
      
      driver.executeUpdate("alter table tabps add primary key (rowid, x)"); // This must work.
      driver.closeAll();
   }

   /** 
    * Tests a query.
    *
    * @param sql The query to be executed
    *
    * @return The number of rows returned by the query.
    */
   private int executeQuery(String sql)
   {
      int count = 0;
      output("\n" + sql);
      ResultSet rs = driver.executeQuery(sql);
      while (rs.next())
      {
         output(rs.getString("FIRST_NAME") + ", " + rs.getString("city"));
         count++;
      }

      rs.close();
      return count;
   }

   /** 
    * Does tests with primary key.
    *
    * @param sql The create table sql string.
    */
   private void test_person_PK(String sql)
   {
   	if (driver.exists("person_pk"))
   		driver.executeUpdate("drop table person_pk");
   	driver.execute(sql);

      // Inserts rows. It should raise the exception in the 11th insert.
      int i = -1;

      try
      {
	      while (++i < numRows)
	         driver.executeUpdate(insertRows[i]);
	      fail("5");
      } 
      catch (PrimaryKeyViolationException exception) {}

      assertEquals(i, 10);

      try  // Tries to add another primary key.
      {
         driver.executeUpdate("alter table PERSON_PK add primary key(city)");
         fail("6");
      } 
      catch (AlreadyCreatedException exception) {}
      
      driver.executeUpdate("alter table PERSON_PK drop primary key"); // Drops the existing primary key.

      i = 9;
      while (++i < numRows) // Inserts the remaining rows.
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      try  // Tries to drop the key again.
      {
         driver.executeUpdate("alter table person_pk drop primary key");
         fail("7");
      } 
      catch (DriverException exception) {}

      try // Tries to add the primary key again. It must raise an exception, since now there are duplicated records.
      {
         driver.executeUpdate("alter table person_pk add primary key(first_name)");
         fail("8");
      } catch (PrimaryKeyViolationException exception) {}

      try // Tries again.
      {
         driver.executeUpdate("alter table person_pk add primary key(first_name)");
         fail("9");
      } 
      catch (PrimaryKeyViolationException exception) {}

      // Tries again after closing the driver.
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      try
      {
         driver.executeUpdate("alter table person_pk add primary key(first_name)");
         fail("10");
      } 
      catch (PrimaryKeyViolationException exception) {}

      // Deletes the duplicated records.
      assertEquals(2, driver.executeUpdate("delete person_pk where first_name = 'guilherme'"));
      assertEquals(5, driver.executeUpdate("delete person_pk where first_name = 'maria'"));
      assertEquals(2, driver.executeUpdate("delete person_pk where first_name = 'paulo'"));
      assertEquals(3, driver.executeUpdate("delete person_pk where first_name = 'roberto'"));
      assertEquals(2, driver.executeUpdate("delete person_pk where first_name = 'cher'"));

      assertEquals(6, executeQuery("select * from person_pk"));
      assertEquals(6, executeQuery("select * from person_pk where 1 = 1"));

      // Tries one more time to add the primary key again. It should work, since there are no duplicated records anymore.
      driver.executeUpdate("alter table person_pk add primary key(first_name)");

      // Checks INSERT statements. The first should pass; the second should fail.
      driver.executeUpdate("insert into person_pk values ('maria', 'Rio de Janeiro', 2.50, 3400.50, 4, 5)");
      try
      {
         driver.executeUpdate("insert into person_pk values ('maria', 'Rio de Janeiro', 2.50, 3400.50, 4, 5)");
         fail("11");
      }
      catch (PrimaryKeyViolationException exception) {}

      // Checks UPDATE statements. The first two should pass; the third should fail.
      assertEquals(1, driver.executeUpdate("update person_pk set first_name = 'madalena' where first_name = 'maria'"));
      assertEquals(1, driver.executeUpdate("update person_pk set city = 'new york' where first_name = 'madalena'"));
      try
      {
         driver.executeUpdate("update person_pk set first_name = 'zanata' where first_name = 'madalena'");
         fail("12");
      } 
      catch (PrimaryKeyViolationException exception) {}
   }
}
