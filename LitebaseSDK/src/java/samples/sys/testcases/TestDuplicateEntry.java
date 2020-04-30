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
 * Tests tables with repeated records.
 */
public class TestDuplicateEntry extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      String insert = "INSERT INTO ACT_PRODUTO (ACTCODPRODUTO, ACTCODFABRICANTE, ACTDESCRICAO, ACTQTDEMBALAGEM, ACTCODUNIPRIMARIA, " 
            + "ACTCODUNIALTERNATIVA, ACTVALORLISTA, ACTTAXAIPI, ACTSTATUS, ACTEMBALAGEMMULTIPLA, ACTFABRICANTEID, ACTLINHAPRODUTOID, " 
            + "ACTPESOUNITARIO, ACTDATALTERACAO, ACTPRODUTOID, ACTDATINCLUSAO, ACTUSUARIOID, ACTCODIGOBARRAS, ACTCODBARRAUNIDADE, " 
            + "ACTPESOTOTAL, ACTOBSERVACAO, ACTGRUPOTRIBUTACAOID) VALUES ('1','1', 'EXTRATO DE TOMATE 1', 48, 'null', 'null', 1.570, " 
            + "2.000, null, null, 1, 1, 140.000, 20040318000000, 2, null, 1, '', 'null', 6720.000, '', null)";
      String delete = "delete act_produto where actcodproduto = '1'";
      ResultSet resultSet;
      
      try
      {
         driver.executeUpdate("drop table act_produto");
      }
      catch (DriverException exception) {} // Table not found.

      driver.execute("CREATE TABLE act_produto (actcodproduto char(30), actcodfabricante char(30), actdescricao char(70), actqtdembalagem long, "
                    + "actcoduniprimaria char(5), actcodunialternativa char(5), actvalorlista double, acttaxaipi double, actstatus int, " 
                    + "actembalagemmultipla long, actfabricanteid long, actlinhaprodutoid long, actpesounitario double, actdatalteracao long, " 
                    + "actprodutoid long, actdatinclusao long, actusuarioid long, actcodigobarras char(20), actcodbarraunidade char(30), "
                    + "actpesototal double, actobservacao char(255), actgrupotributacaoid long)");
      driver.execute("create index idx on act_produto(actpesototal)");
      assertEquals(1, driver.executeUpdate(insert));
      assertEquals(1, driver.executeUpdate(insert));
      assertEquals(2, driver.executeUpdate(delete));
      assertEquals(1, driver.executeUpdate(insert));
      assertEquals(1, driver.executeUpdate("delete act_produto"));
      assertEquals(0, driver.executeUpdate(delete));
      assertEquals(1, driver.executeUpdate(insert));

      try
      {
         assertNotNull(resultSet = driver.executeQuery("select * from act_produto where ACTGRUPOTRIBUTACAOID = 20050411095951L"));
         resultSet.close();
      }
      catch (SQLParseException exception)
      {
         if (Settings.onJavaSE) 
            exception.printStackTrace();
         fail("Exception thrown: " + exception.getMessage());
      }
      
      // Tests the purge for tables with big headers.
      driver.purge("act_produto");
      resultSet = driver.executeQuery("select rowid from act_produto");
      assertTrue(resultSet.next());
      assertEquals(resultSet.getInt("rowid"), 4);
      assertFalse(resultSet.next());
      resultSet.close();   
      
      driver.closeAll();
      testRepetedNameOnSelect();
   }

   /**
    * Tests duplicated name on the select clause.
    */
   private void testRepetedNameOnSelect()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");

      if (driver.exists("person"))
         driver.executeUpdate("drop table person");

      // Creates the table and stores data.
      driver.execute("create table person(name char(5), age int)");
      driver.executeUpdate("Insert into person values ('RLN', 20)");
      driver.executeUpdate("Insert into person values ('IOG', 18)");

      try
      {
         ResultSet rs = driver.executeQuery("Select name, name as t, age from person");
         assertTrue(rs.next());
         assertEquals("RLN", rs.getString("name"));
         assertEquals("RLN", rs.getString("t"));
         assertEquals(20, rs.getInt("age"));
         assertEquals("RLN\tRLN\t20", rs.rowToString());
         assertTrue(rs.next());
         assertEquals("IOG", rs.getString("name"));
         assertEquals("IOG", rs.getString("t"));
         assertEquals(18, rs.getInt("age"));
         assertEquals("IOG\tIOG\t18", rs.rowToString());
         rs.close();
      }
      catch (RuntimeException exception)
      {
         fail();
      }
      driver.closeAll();
   }
}
