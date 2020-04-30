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
import totalcross.sys.*;
import totalcross.unit.TestCase;
import totalcross.util.Date;

/**
 * Tests the use of prepared statements.
 */
public class TestPreparedStatement extends TestCase
{
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");

      if (!driver.exists("Lancamento"))
      {
         driver.execute("create table Lancamento(data int, ndoc int, valor double, pago short, tipo short, dataExt char(10), origem char(20), " 
                                                                                                                          + "obs char(50) nocase)");
         driver.execute("CREATE INDEX IDX_0 ON Lancamento(rowid)");
      }
      else
         try
         {
            driver.executeUpdate("delete from lancamento");
         } 
         catch (DriverException exception)
         {
            assertTrue(exception.getMessage().startsWith("It is not possible to open a table within a connection with a different"));
            driver.executeUpdate("drop table lancamento");
            driver.execute("create table Lancamento(data int, ndoc int, valor double, pago short, tipo short, dataExt char(10), origem char(20), " 
                                                                                                                          + "obs char(50) nocase)");
         }

      PreparedStatement psListar = driver.prepareStatement("select rowid,pago,dataExt,ndoc,valor,tipo from lancamento where data <= ? and " 
                                                                                                   + "(data >= ? or pago=0) order by data");
      assertEquals(1, driver.executeUpdate("insert into lancamento (data,dataext,ndoc,valor,tipo,origem,obs,pago) values " 
                                                                + "(20051219,'19/12/2005',0000004,-444.00,0,'Alimentacao','',0)"));
      psListar.setInt(0, 20051231);
      psListar.setInt(1, 20051130);
      assertEquals(1, executePreparedQuery(psListar));
      assertEquals(1, driver.executeUpdate("insert into lancamento (data,dataext,ndoc,valor,tipo,origem,obs,pago) values " 
                                                                + "(20051220,'20/12/2005',0000088,-34.00,2,'Gas de Cozinha','',0)"));
      assertEquals(2, executePreparedQuery(psListar));
     
      try // A null can't be inserted in a prepared statement.
      {
         psListar.setNull(0);
         fail("1");
      } 
      catch (SQLParseException exception) {}
      
      try // A null can't be inserted in a prepared statement.
      {
         psListar.setString(0, null);
         fail("2");
      } 
      catch (SQLParseException exception) {}
      
      assertTrue(psListar.isValid());
      psListar.close();
      assertFalse(psListar.isValid());
      
      try
      {
         driver.executeUpdate("drop table PERSON");
      } 
      catch (DriverException exception) {} // Table not found.

      // Creates thr table.
      driver.execute("create table PERSON (FIRST_NAME CHAR(30), LAST_NAME CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, YEARS_EXP_JAVA INT, " 
                                                                                                                        + "YEARS_EXP_C INT )");

      // Creates the insert prepared statement.
      PreparedStatement preparedStmt = driver.prepareStatement("insert into PERSON values (?, ?, ?, ?, ?, ?) "); // added space at the end.

      // Inserts the rows.
      // Row 1
      preparedStmt.setString(0, "guilherme");
      preparedStmt.setString(1, "hazan");
      preparedStmt.setDouble(2, 4400.50);
      preparedStmt.setDouble(3, 3400.80);
      preparedStmt.setInt(4, 10);
      preparedStmt.setInt(5, 6);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 2
      preparedStmt.setString(0, "raimundo");
      preparedStmt.setString(1, "correa");
      preparedStmt.setDouble(2, 3400.50);
      preparedStmt.setDouble(3, 3400.50);
      preparedStmt.setInt(4, 11);
      preparedStmt.setInt(5, 26);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 3
      preparedStmt.setString(0, "ricardo");
      preparedStmt.setString(1, "zorba");
      preparedStmt.setDouble(2, 10400.50);
      preparedStmt.setDouble(3, 5000.20);
      preparedStmt.setInt(4, 23);
      preparedStmt.setInt(5, 23);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 4
      preparedStmt.setString(0, "cher");
      preparedStmt.setString(1, "cher");
      preparedStmt.setDouble(2, 1000.50);
      preparedStmt.setDouble(3, 3400.50);
      preparedStmt.setInt(4, 4);
      preparedStmt.setInt(5, 3);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 5
      preparedStmt.setString(0, "lero");
      preparedStmt.setString(1, "lero");
      preparedStmt.setDouble(2, 2001.34);
      preparedStmt.setDouble(3, 1000.35);
      preparedStmt.setInt(4, 2);
      preparedStmt.setInt(5, 6);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 6
      preparedStmt.setString(0, "zico");
      preparedStmt.setString(1, "mengao");
      preparedStmt.setDouble(2, 1000.51);
      preparedStmt.setDouble(3, 1000.51);
      preparedStmt.setInt(4, 12);
      preparedStmt.setInt(5, 0);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 7
      preparedStmt.setString(0, "roberto");
      preparedStmt.setString(1, "dinamite");
      preparedStmt.setDouble(2, 2222.51);
      preparedStmt.setDouble(3, 1.21);
      preparedStmt.setInt(4, 10);
      preparedStmt.setInt(5, 10);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 8
      preparedStmt.setString(0, "socrates");
      preparedStmt.setString(1, "sampaio");
      preparedStmt.setDouble(2, 1111.50);
      preparedStmt.setDouble(3, 1111.50);
      preparedStmt.setInt(4, 7);
      preparedStmt.setInt(5, 11);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 9
      preparedStmt.setString(0, "paulo");
      preparedStmt.setString(1, "falcao");
      preparedStmt.setDouble(2, 2800.04);
      preparedStmt.setDouble(3, 1.21);
      preparedStmt.setInt(4, 15);
      preparedStmt.setInt(5, 12);
      assertEquals(1, preparedStmt.executeUpdate());

      // Row 10
      preparedStmt.setString(0, "leo");
      preparedStmt.setString(1, "junior");
      preparedStmt.setDouble(2, 2.50);
      preparedStmt.setDouble(3, 3400.50);
      preparedStmt.setInt(4, 4);
      preparedStmt.setInt(5, 5);
      assertEquals(1, preparedStmt.executeUpdate());

      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Run SQL queries to test the insert statements.
      assertEquals(10, executeQuery(driver, "select * from PERSON")); // No where clause

      // With where clause.
      assertEquals(2, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur and last_name like '%or%'"));
      assertEquals(9, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur or last_name like '%or%'"));
      assertEquals(4, executeQuery(driver, "select * from PERSON where salary_prev <= salary_cur and not years_exp_c > 10"));
      assertEquals(6, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 and years_exp_java >= 10) or FIRST_NAME > 'r'"));
      assertEquals(4, executeQuery(driver, "select * from PERSON where salary_prev < 3000 and (years_exp_java >= 10 or FIRST_NAME > 'r')"));
      assertEquals(2, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 or not years_exp_c >= years_exp_java) and not " 
                                                                                               + "(years_exp_java >= 10 or FIRST_NAME > 'r')"));
      assertEquals(8, executeQuery(driver, "select * from PERSON where salary_prev < 3000 or ((not years_exp_c >= years_exp_java) " 
                                                                                            + "and not years_exp_java >= 10) or FIRST_NAME > 'r'"));

      // Run SQL update command to test the insert statements.
      assertEquals(8, driver.executeUpdate("update PERSON set FIRST_NAME = 'x' where salary_prev < 3000 or ((not years_exp_c >= years_exp_java) " 
                                                                                  + "and not years_exp_java >= 10) or FIRST_NAME > 'r'"));
      
      // Tests SELECT prepared statements.
      preparedStmt = driver.prepareStatement("select * from PERSON where salary_prev < ? or ((not years_exp_c >= years_exp_java) " 
                                                                                           + "and not years_exp_java >= ?) or FIRST_NAME > ?");

      preparedStmt.setDouble(0, 3000);
      preparedStmt.setInt(1, 10);
      preparedStmt.setString(2, "r");
      assertEquals(8, executePreparedQuery(preparedStmt));

      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Tests SELECT with WHERE and HAVING.
      (preparedStmt = driver.prepareStatement("select years_exp_java, count(*) as total from person where years_exp_c > ? and last_name < ? group by " 
                                                                                                  + "years_exp_java having total = ?")).setInt(0, 1);
      preparedStmt.setString(1, "t");
      preparedStmt.setInt(2, 1);
      assertEquals(4, executePreparedQuery(preparedStmt));

      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Tests UPDATE prepared statements.
      (preparedStmt = driver.prepareStatement("update PERSON set FIRST_NAME = ?, last_name = ? where salary_prev < ? or " 
                    + "((not years_exp_c >= years_exp_java) and not years_exp_java >= ?) or FIRST_NAME not like ?")).setString(0, "roberto");
      preparedStmt.setString(1, "carlos");
      preparedStmt.setDouble(2, 3000);
      preparedStmt.setInt(3, 10);
      preparedStmt.setString(4, "g%");
      assertEquals(9, preparedStmt.executeUpdate());

      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Checks if the UPDATE worked.
      assertEquals(9, executeQuery(driver, "select * from PERSON where first_name = 'roberto' and last_name = 'carlos'"));
      
      // Tests DELETE prepared statements.
      (preparedStmt = driver.prepareStatement("delete person where salary_prev <= ? and not years_exp_c > ?")).setDouble(0, 4000);
      preparedStmt.setInt(1, 10);
      assertEquals(6, preparedStmt.executeUpdate());
      
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      assertEquals(4, executeQuery(driver, "select * from PERSON")); // Checks if DELETE worked.

      // Tests exceptions.

      // Parameter values not defined.
      (preparedStmt = driver.prepareStatement("select years_exp_java, count(*) as total from person where years_exp_c > ? and last_name < ? group by " 
                                                                                                 + "years_exp_java having total = ?")).setInt(0, 1);
      preparedStmt.setInt(2, 1);

      try
      {
         assertEquals(4, executePreparedQuery(preparedStmt));
         fail("3");
      }
      catch (DriverException exception)
      {
         assertGreaterOrEqual(exception.getMessage().indexOf("had their values defined"), 0);
      }

      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Incompatible types.
      preparedStmt = driver.prepareStatement("select years_exp_java, count(*) as total from person where years_exp_c > ? and last_name < ? group by " 
                                                                                                      + "years_exp_java having total = ?");
      
      try
      {
         preparedStmt.setInt(1, 1);
         fail("4");
      }
      catch (DriverException exception) {}
      try
      {
         preparedStmt.setBlob(0, null);
         fail("5");
      }
      catch (SQLParseException exception) {}
      try
      {
         preparedStmt.setString(0, "a");
         fail("6");
      }
      catch (SQLParseException exception) {}
      try
      {
         preparedStmt.setDouble(0, 1);
         fail("7");
      }
      catch (DriverException exception) {}
      try
      {
         preparedStmt.setDate(0, new Date());
         fail("8");
      }
      catch (SQLParseException exception) {}
      try
      {
         preparedStmt.setDateTime(0, new Time());
         fail("9");
      }
      catch (SQLParseException exception) {}

      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Tests when there are no where clauses in the prepared statement.
      if (driver.exists("PEDIDO"))
         driver.executeUpdate("drop table pedido");
      driver.execute("create table PEDIDO(NUMERO int primary key, TOTAL double)");
      driver.executeUpdate("insert into pedido values (10,1000.54321)");
      driver.executeUpdate("insert into pedido values (11,2000)");
      driver.executeUpdate("insert into pedido values (12,3000)");
 
      ResultSet rs = driver.prepareStatement("select sum(TOTAL) as VALORTOTAL from PEDIDO").executeQuery();
      assertTrue(rs.first());
      rs.setDecimalPlaces(1, 4);
      assertEquals("6000.5432", rs.getString(1));
      rs.close();

      // Tests prepared statement reutilization for updates.
      if (!driver.exists("ITEMPEDIDO"))
      {
         driver.execute("create table ITEMPEDIDO(PEDIDO int, PRODUTO int, QTDE int, UNITARIO double, TOTAL double)");
         driver.execute("create index IDX_ITEMPEDIDO_PEDIDO on ITEMPEDIDO(PEDIDO)");
      }
      else
         try
         {
            driver.executeUpdate("delete itempedido");
         }
         catch (DriverException exception)
         {
            assertTrue(exception.getMessage().startsWith("It is not possible to open a table within a connection with a different"));
            driver.executeUpdate("drop table itempedido");
            driver.execute("create table ITEMPEDIDO(PEDIDO int, PRODUTO int, QTDE int, UNITARIO double, TOTAL double)");
            driver.execute("create index IDX_ITEMPEDIDO_PEDIDO on ITEMPEDIDO(PEDIDO)");
         }
      
      PreparedStatement psInsert = driver.prepareStatement("insert into ITEMPEDIDO(PEDIDO, PRODUTO, QTDE, UNITARIO, TOTAL) values(?, ?, ?, ?, ?)");
      
      // Inserts 3 orders with 4 items.
      int i = 4,
          j;
      
      while (--i >= 1)
      {
         j = 5;
         while (--j >= 1)
         {
            psInsert.clearParameters();
            psInsert.setInt(0, i);
            psInsert.setInt(1, j);
            psInsert.setInt(2, 1);
            psInsert.setDouble(3, 1);
            psInsert.setDouble(4, 1);
            assertEquals(1, psInsert.executeUpdate());
         }
      }
      
      PreparedStatement psUpdate = driver.prepareStatement("update ITEMPEDIDO set QTDE = ?, UNITARIO = ?, TOTAL = ? where PEDIDO = ? and " 
                                                                                                                       + "PRODUTO = ?");
      // Updates the first item of the first order.
      psUpdate.clearParameters();
      psUpdate.setInt(0, 2);
      psUpdate.setDouble(1, 1);
      psUpdate.setDouble(2, 2);
      psUpdate.setInt(3, 1);
      psUpdate.setInt(4, 1);
      assertEquals(1, psUpdate.executeUpdate());
      
      // Updates the second item of the first order.
      psUpdate.clearParameters();
      psUpdate.setInt(0, 2);
      psUpdate.setDouble(1, 1);
      psUpdate.setDouble(2, 2);
      psUpdate.setInt(3, 1);
      psUpdate.setInt(4, 2);
      assertEquals(1, psUpdate.executeUpdate());

      assertTrue(psInsert.isValid());
      assertTrue(psUpdate.isValid());
      psInsert.close();
      psUpdate.close();
      assertFalse(psInsert.isValid());
      assertFalse(psUpdate.isValid());
      
      // Tests shorts and floats using prepared statement.
      if (driver.exists("teste"))
         driver.executeUpdate("drop table teste");
      driver.execute("create table teste(id int primary key, sh1 short, x float)");
      
      (preparedStmt = driver.prepareStatement("insert into teste values (?, ?, ?)")).setInt(0, 3);
      preparedStmt.setShort(1, (short)5);
      preparedStmt.setFloat(2, 7);
      preparedStmt.executeUpdate();
      (rs = driver.executeQuery("select * from teste")).first();
      assertEquals(3, rs.getInt(1));
      assertEquals(5, rs.getShort(2)); 
      assertEquals(7, rs.getFloat(3), 0.001);
      rs.close();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      (preparedStmt = driver.prepareStatement("update teste set sh1 = ?, x = ?")).setShort(0, (short)6);
      preparedStmt.setFloat(1, 8);
      preparedStmt.executeUpdate();
      (rs = driver.executeQuery("select * from teste")).first();
      assertEquals(3, rs.getInt(1));
      assertEquals(6, rs.getShort(2)); 
      assertEquals(8, rs.getFloat(3), 0.001);
      rs.close();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      (preparedStmt = driver.prepareStatement("select * from teste where sh1 = ? and x = ?")).setShort(0, (short)6);
      preparedStmt.setFloat(1, 8);
      (rs = preparedStmt.executeQuery()).first();
      assertEquals(3, rs.getInt(1));
      assertEquals(6, rs.getShort(2)); // o valor lido � sempre 0!!
      assertEquals(8, rs.getFloat(3), 0.001);
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      rs.close();
      
      (preparedStmt = driver.prepareStatement("delete from teste where sh1 = ? and x = ?")).setShort(0, (short)6);
      preparedStmt.setFloat(1, 8);
      preparedStmt.executeUpdate();
      rs = driver.executeQuery("select * from teste");
      rs.first();
      assertEquals(0, rs.getRowCount());
      rs.close();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      if (driver.exists("teste2"))
         driver.executeUpdate("drop table teste2");
      driver.execute("create table teste2 (id2 int)");
      
      (preparedStmt = driver.prepareStatement("select * from teste, teste2 where id = id2 and sh1 = ? and x = ?")).setShort(0, (short)6);
      preparedStmt.setFloat(1, 8);
      
      // Tests a select prepared statement with group by.
      if (driver.exists("table1"))
         driver.executeUpdate("drop table table1");
      driver.execute("create table table1 (field1 int NOT NULL, field2 char(50) NOT NULL, primary key (field1))");
      preparedStmt = driver.prepareStatement("insert into table1 values(?, 'Thiago')");
      i = -1;
      while (++i < 5)
      {
         preparedStmt.setInt(0, i + 1);
         preparedStmt.executeUpdate();
      }
      rs = (preparedStmt = driver.prepareStatement("select field2 from table1 group by field2")).executeQuery();
      assertTrue(rs.next());
      assertEquals("Thiago", rs.getString(1));
      assertFalse(rs.next());
      rs.close();
      rs = preparedStmt.executeQuery();
      assertTrue(rs.next());
      assertEquals("Thiago", rs.getString(1));
      assertFalse(rs.next());
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      rs.close();
      
      // Tests a select prepared statement with where clause and indices.
      if (driver.exists("teste"))
         driver.executeUpdate("drop table teste");
      driver.execute("create table teste (idPessoa int primary key, Nome varchar(50), TipoOp short)");
      driver.executeUpdate("insert into teste values (-2, 'joao', 0)");
      preparedStmt = driver.prepareStatement("select * from teste where ( (1 != 1) or (Upper(Nome) = ? and 1 = 1)) and (TipoOp < 3 and idPessoa != -2)");
      preparedStmt.setString(0, "JOAO");
      assertFalse((rs = preparedStmt.executeQuery()).first());
      rs.close();
      preparedStmt.setString(0, "JOAO");
      
      try // Invalid index.
      {
         preparedStmt.setString(2, "JOAO"); 
         fail("10");
      }
      catch (DriverException exception) {}
      
      assertFalse((rs = preparedStmt.executeQuery()).first());
      rs.close();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Tests a select prepared statement without parameters and like.
      if (driver.exists("property"))
         driver.executeUpdate("drop table property");
      driver.execute("create table property (keyP char(100) NOT NULL, value char(200), descr char(100) NOT NULL, status int NOT NULL, " 
                                                                                                                + "primary key (keyP))");
      driver.executeUpdate("insert into property (keyP, value, descr, status) values ('ws_prod', NULL, 'Produ��o', 1)");
      driver.executeUpdate("insert into property (keyP, value, descr, status) values ('ws_test', NULL, 'Teste', 2)");
      driver.executeUpdate("insert into property (keyP, value, descr, status) values ('tables_bkp', 'true', 'Criar c�pia de seguran�a', 1)");
      driver.executeUpdate("insert into property (keyP, value, descr, status) values ('tables_drop', NULL, 'Excluir tabelas', 3)");
      driver.executeUpdate("insert into property (keyP, value, descr, status) values ('tables_restore', NULL, 'Recuperar tabelas', 3)");
       
      assertTrue((rs = (preparedStmt = driver.prepareStatement("select property.value from property where keyP like 'ws%'")).executeQuery()).next());
      rs.close();
      
      // Invalid indices.
      try
      {
         preparedStmt.setInt(0, 0);
         fail("11");
      }
      catch (DriverException exception) {}
      try
      {
         preparedStmt.setInt(0, -1);
         fail("12");
      }
      catch (DriverException exception) {}
      
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      preparedStmt = driver.prepareStatement("select * from teste, teste2");
      
      // Tests what happens if a table being used by a prepared statement is dropped.
      driver.executeUpdate("drop table teste");
      try
      {
         preparedStmt.executeQuery();
         fail("13");
      }
      catch (IllegalStateException exception) {}
      catch (DriverException exception) {}
      driver.execute("create table teste(id int primary key, sh1 short, x float)");
      try
      {
         preparedStmt.executeQuery();
      }
      catch (DriverException exception) {}
      catch (IllegalStateException exception) {}
      
      preparedStmt = driver.prepareStatement("select * from teste, teste2 where id = id2 and sh1 = ? and x = ?");
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      // Tests update prepared statements with some data to be changed already given.
      if (driver.exists("rota")) 
         driver.executeUpdate("DROP TABLE rota");
      driver.execute("CREATE TABLE rota (id_rota_dia CHAR(1), ds_rota CHAR(20) NOT NULL, cd_rota INT NOT NULL, dt_abertura DATE, PRIMARY KEY(cd_rota))");
      driver.executeUpdate("INSERT INTO rota(ds_rota, cd_rota) VALUES ('Rota 1', 1)");
      driver.executeUpdate("INSERT INTO rota(ds_rota, cd_rota) VALUES ('Rota 2', 2)");
      driver.executeUpdate("INSERT INTO rota(ds_rota, cd_rota) VALUES ('Rota 3', 3)");
      driver.executeUpdate("INSERT INTO rota(ds_rota, cd_rota) VALUES ('Rota 4', 4)");
      
      Date date = new Date();
      String dateStr = date.toString();
      
      (preparedStmt = driver.prepareStatement("UPDATE rota SET dt_abertura = ?, id_rota_dia = 'A' WHERE cd_rota = ?")).clearParameters();
      preparedStmt.setDate(0, date);
      preparedStmt.setInt(1, 2);
      preparedStmt.executeUpdate();
      (rs = driver.executeQuery("select dt_abertura from rota where cd_rota = 2")).first();
      assertEquals(1, rs.getRowCount());
      assertEquals(dateStr, rs.getDate(1).toString());
      rs.close();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      (preparedStmt = driver.prepareStatement("UPDATE rota SET id_rota_dia = 'A', dt_abertura = ? WHERE cd_rota = ?")).clearParameters();
      preparedStmt.setDate(0, date);
      preparedStmt.setInt(1, 1);
      preparedStmt.executeUpdate();
      (rs = driver.executeQuery("select dt_abertura from rota where cd_rota = 1")).first();
      assertEquals(1, rs.getRowCount());
      assertEquals(dateStr, rs.getDate(1).toString());
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      rs.close();
      
      preparedStmt = null;
      Vm.gc(); 
      
      // Tests toString().
      if (driver.exists("exemplo")) 
         driver.executeUpdate("drop table exemplo");
      driver.execute("CREATE TABLE exemplo(campo1 INT, campo2 CHAR(50), PRIMARY KEY(campo1))");
         
      (preparedStmt = driver.prepareStatement("INSERT INTO exemplo(campo1, campo2) VALUES (?, ?)")).toString();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      (preparedStmt = driver.prepareStatement("INSERT INTO exemplo(campo1, campo2) VALUES (?, ?)")).toString();
      assertTrue(preparedStmt.isValid());
      preparedStmt.close();
      assertFalse(preparedStmt.isValid());
      
      try // executeUpdate() with a select.
      {
         driver.prepareStatement("select * from teste").executeUpdate();
         fail("14");
      }
      catch (DriverException exception) {}
      
      // executeQuery() without a select.
      try 
      {
         driver.prepareStatement("create table teste2 (id2 int)").executeQuery();
         fail("15");
      }
      catch (DriverException exception) {}
      try 
      {
         driver.prepareStatement("create index idx on teste2(id2)").executeQuery();
         fail("16");
      }
      catch (DriverException exception) {}
      try 
      {
         driver.prepareStatement("insert into teste2 values (0)").executeQuery();
         fail("17");
      }
      catch (DriverException exception) {}
      try 
      {
         driver.prepareStatement("delete from teste2").executeQuery();
         fail("18");
      }
      catch (DriverException exception) {}
      try 
      {
         driver.prepareStatement("update teste2 set id2 = 1").executeQuery();
         fail("19");
      }
      catch (DriverException exception) {}
      try 
      {
         driver.prepareStatement("alter table teste2 drop primary key").executeQuery();
         fail("20");
      }
      catch (DriverException exception) {}
      
      try
      {
         psInsert.close();
         fail("21");
      }
      catch (IllegalStateException exception) {}
      try
      {
         psUpdate.close();
         fail("22");
      }
      catch (IllegalStateException exception) {}
      try
      {
         psListar.close();
         fail("23");
      }
      catch (IllegalStateException exception) {}
      
      driver.closeAll();
      
      try
      {
         preparedStmt.close();
         fail("24");
      }
      catch (IllegalStateException exception) {}
   }

   /**
    * Execute a query and returns the number of rows that answers the query.
    * 
    * @param driver The connection with Litebase.
    * @param sql The query to be executed.
    * @return The total number of rows that answers the query.
    */
   private int executeQuery(LitebaseConnection driver, String sql)
   {
      int count = 0;
      ResultSet rs = driver.executeQuery(sql);
      while (rs.next())
         count++;
      rs.close();
      return count;
   }

   /**
    * Execute a prepated query and returns the number of rows that answers the query.
    * 
    * @param stmt A prepared query.
    * @return The total number of rows that answers the query.
    */
   private int executePreparedQuery(PreparedStatement stmt)
   {
      int count = 0;
      ResultSet rs = stmt.executeQuery();
      while (rs.next())
         count++;
      rs.close();
      return count;
   }

}
