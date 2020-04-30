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
 * Tests where clauses with and without indices.
 */
public class TestWhereClause_Indexes extends TestCase
{
   /** 
    * The main test method.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");

      String[] insertRows =  // Rows to insert.
      {
         "insert into PERSON values ('null', 'null', 0.0, 0.0, 0, 0)",
         "insert into PERSON values ('guilherme', 'Rio de Janeiro', 4400.50, 3400.80, 10, 6)",
         "insert into PERSON values ('raimundo', 'Fortaleza', 3400.50, 3400.50, 11, 26)",
         "insert into PERSON values ('ricardo', 'Natal', 10400.50, 5000.20, 23, 23)",
         "insert into PERSON values ('cher', 'Fortaleza', 1000.50, 3400.50, 4, 3)",
         "insert into PERSON values ('maria', 'Paraty', 2001.34, 1000.35, 2, 6)",
         "insert into PERSON values ('zico', 'Ouro Preto', 1000.51, 1000.51, 12, 0)",
         "insert into PERSON values ('roberto', 'Rio de Janeiro', 2222.51, 1.21, 10, 10)",
         "insert into PERSON values ('socrates', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
         "insert into PERSON values ('paulo', 'Rio de Janeiro', 2800.04, 1.21, 15, 12)",
         "insert into PERSON values ('leo', 'Natal', 2.50, 3400.50, 4, 5)",
         "insert into PERSON values ('maria', 'Foz do Igua�u', 4400.50, 3400.80, 10, 6)",
         "insert into PERSON values ('guilherme', 'Porto Seguro', 3400.50, 3400.50, 11, 26)",
         "insert into PERSON values ('zanata', 'Florianopolis', 10400.50, 5000.20, 23, 23)",
         "insert into PERSON values ('roberto', 'Natal', 1000.50, 3400.50, 4, 3)",
         "insert into PERSON values ('maria', 'Fortaleza', 2001.34, 1000.35, 2, 6)",
         "insert into PERSON values ('maria', 'Porto Seguro', 1000.51, 1000.51, 12, 0)",
         "insert into PERSON values ('roberto', 'Ouro Preto', 2222.51, 1.21, 10, 10)",
         "insert into PERSON values ('paulo', 'Porto Seguro', 1111.50, 1111.50, 7, 11)",
         "insert into PERSON values ('cher', 'Rio de Janeiro', 2800.04, 1.21, 15, 12)",
         "insert into PERSON values ('maria', 'Rio de Janeiro', 2.50, 3400.50, 4, 5)", 
         "insert into PERSON values (null, null, null, null, null, null)"
      };
      int numRows = insertRows.length;

      try
      {
         driver.executeUpdate("drop table PERSON");
      }
      catch (DriverException exception) {} // Table not found.

      // Creates the table.
      driver.execute("create table PERSON (FIRST_NAME CHAR(30), CITY CHAR(30), SALARY_CUR DOUBLE, SALARY_PREV DOUBLE, YEARS_EXP_JAVA INT, " 
                                                                                                                   + "YEARS_EXP_C INT )");
      
      // Inserts rows.
      int i = numRows;
      while (--i >= 0)
         assertEquals(1, driver.executeUpdate(insertRows[i]));

      // Run SQL queries to test the WHERE clause evaluation using indexes usage.
      testQueries(driver); // Queries without index.
      
      // Queries with index.
      // Creates the indices.
      driver.execute("create index idx_name on PERSON(FIRST_NAME)");
      driver.execute("create index idx_city on PERSON(city)");
      driver.execute("create index idx_exp_C on PERSON(years_exp_C)");

      try // A primary key can't have null.
      {
         driver.executeUpdate("alter table person add primary key (SALARY_CUR)");
         fail();
      }
      catch (DriverException exception) {}
      
      testQueries(driver); // Repeats queries.
      
      // A more realistic example.
      try
      {
         driver.executeUpdate("drop table parecer");
      }
      catch (DriverException exception) {} // Table not found.

      // Creates the tables and indices.
      driver.execute("create table parecer(numero_registro int primary key, nome_obra char(08), fase_obra char(04), Codigo_Etapa short, "
                   + "numero_ord_serv int, codigo_atividade short, quant_atividade Double, codigo_empreiteiro char(08), data_prevista_ini char(10), " 
                                        + "data_prevista_fim char(10), data_vistoria char(10), check_list char(25), Ident_usuario char(10)) ");
      driver.execute("Create index idxParObra on parecer(nome_obra)");
      driver.execute("Create index idxParFase on parecer(Fase_obra)");
      driver.execute("Create index idxParOs on parecer(numero_ord_serv)");
      driver.execute("Create index idxParAtiv on parecer(Codigo_Atividade)");
      driver.execute("Create index idxParEtp on parecer(Codigo_Etapa)");

      // Inserts the rows.
      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                         + "Values(1, 'OBRA 01', 'CA26', 1, 1, 3, 1, '1', '01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                         + "Values(2, 'OBRA 01', 'CA27', 1, 100, 2, 1, '1', '01/01/2006', '01/01/2006' ,'01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario)  " 
                         + "Values(3, 'OBRA 01', 'CA27', 1, 2, 3, 1, '1', '01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                         + "Values(4, 'OBRA 01', 'CA30', 1, 4, 1, 1, '1', '01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                         + "Values(5, 'OBRA 01', 'CA30', 1, 10, 2, 1, '1',"
                         + "'01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                         + "Values(6, 'OBRA 01', 'CA30', 1, 11, 3, 1, '1', '01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                         + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                         + "Values(7, 'OBRA 02', 'CA30', 1, 12, 1, 1, '1', '01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      driver.executeUpdate("Insert into parecer(numero_registro, nome_obra, fase_obra, Codigo_Etapa, numero_ord_serv , codigo_atividade , " 
                        + "quant_atividade , codigo_empreiteiro , data_prevista_ini, data_prevista_fim, data_vistoria, check_list, Ident_usuario) " 
                        + "Values(8, 'OBRA 02', 'CA30', 1, 13, 3, 1, '1', '01/01/2006', '01/01/2006', '01/01/2006', 'BBBBBBBBBBBBBBBBBBBBB', '')");

      // Does a query to test the indices.
      ResultSet resultSet = driver.executeQuery("Select codigo_Empreiteiro, Codigo_Atividade, Numero_Ord_Serv, Data_Prevista_Ini, Data_Prevista_Fim,"
          + " Data_Vistoria From parecer Where (numero_ord_serv > 0) And (Codigo_Etapa = 001) And (Nome_obra = 'OBRA 01') And (Fase_Obra = 'CA27')");
      assertEquals(2, resultSet.getRowCount());
      resultSet.close();
      
      // Another realistic sample.
      if (driver.exists("visitaDetalhe"))
         driver.executeUpdate("drop table visitaDetalhe");
      driver.execute("create table visitaDetalhe ( codigoVendedor char( 6 ), dataRoteiro char( 8 ), codigoCliente char( 8 ), status char( 2 ), " 
     + "dataRemarcacao char( 8 ), sequencia char( 3 ), estagio int, acoes char( 30 ), obs1 char( 30 ), obs2 char( 30 ), proximosPassos char( 30 ), " 
     + "contato1 char( 15 ), contato2 char( 15 ), codigoContato1 char( 6 ), codigoContato2 char( 6 ), ddd1 char( 3 ), telefone1 char( 10 ), " 
     + "ddd2 char( 3 ), telefone2 char( 10 ), potencial int, fechamento char( 3 ), programado char( 1 ), email char( 40 ), codSegmento char( 4 ) )");
      driver.execute("create index idx_dataRoteiro on visitaDetalhe(dataRoteiro)");
      assertEquals(0, driver.executeUpdate("delete visitaDetalhe  where dataRoteiro <> '20110211'"));
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00000301','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00000401','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )"); 
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00000401','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00001501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00001501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00001701','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00002501','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00002501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00002501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00003001','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");

      assertEquals(10, driver.executeUpdate("delete visitaDetalhe  where dataRoteiro <> '20110211'"));
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00000301','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00000401','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )"); 
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00000401','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00001501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00001501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00001701','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00002501','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00002501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00002501','03','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      driver.executeUpdate("insert into visitaDetalhe values ( '700001','20101001','00003001','00','00000000','000',0,'','','','','','','','','',''," 
                                                                                                                      + "'','',0,'---','S','','' )");
      assertEquals(10, driver.executeUpdate("delete visitaDetalhe  where dataRoteiro <> '20110211'"));
      
      try
      {
         driver.executeUpdate("drop table PERSON");
      }
      catch (DriverException exception) {} // Table not found.
      
      // Inserts 0 in the index and tries to find the record when deleting it.
      driver.execute("create table person (id int)");
      driver.execute("create index idx on person (id)");
      assertEquals(1, driver.executeUpdate("insert into person values (0)"));
      assertEquals(1, driver.executeUpdate("delete from person p where p.rowid = 1"));
      assertEquals(0, driver.getRowCount("person"));
      assertEquals(1, driver.getRowCountDeleted("person"));
      assertEquals(0, driver.executeUpdate("delete from person"));
      assertEquals(0, driver.getRowCount("person"));
      assertEquals(1, driver.getRowCountDeleted("person"));
      assertEquals(1, driver.executeUpdate("insert into person values (0)"));
      assertEquals(1, driver.executeUpdate("insert into person values (null)"));
      assertEquals(1, driver.executeUpdate("delete from person p where p.id is not null"));
      assertEquals(2, driver.getRowCountDeleted("person"));
      assertEquals(1, driver.getRowCount("person"));
      assertEquals(1, driver.executeUpdate("delete from person p where p.id is null"));
      assertEquals(3, driver.getRowCountDeleted("person"));
      assertEquals(0, driver.getRowCount("person"));
      
      // One more realistic sample.
      if (driver.exists("cliente"))
         driver.executeUpdate("drop table cliente");
      driver.execute("create table cliente (CODCLI char( 10 ), CODIGO char( 10 ), NOMEFANTASIA char( 50 ), RAZAOSOCIAL char( 65 ), " 
     + "CONTATO char( 50 ), ENDERECO char( 55 ), BAIRRO char( 20 ), CIDADE char( 40 ), ESTADO char( 2 ), CEP char( 8 ), TELEFONE char( 55 ), " 
     + "CNPJCPF char( 18 ), INSCRESTADUALRG char( 20 ), PESSOA char( 1 ), EMAIL char( 50 ), BLOQUEIAVENDA char( 2 ), CODVENDED char( 10 ), " 
     + "LIMITECREDITO double, OBS char( 200 ), TRANSMITIDO char( 1 ), CODCATEGORIA char( 10 ), TITULOSVENCIDOS double, NUMEROLOGRADOURO char( 10 ), " 
     + "CODFP char( 10 ), CODTRANS char( 10 ), CODPRECO char( 10 ), COMPLEMENTOLOGRADOURO char( 50 ) )");
      driver.execute("create index idx_rowid on cliente(rowid)");
      driver.execute("create index idx_CODCLI on cliente(CODCLI)");
      driver.execute("create index idx_NOMEFANTASIA on cliente(NOMEFANTASIA)");
      driver.execute("create index idx_RAZAOSOCIAL on cliente(RAZAOSOCIAL)");
      driver.execute("create index idx_CODVENDED on cliente(CODVENDED)");
      driver.executeQuery("select rowid,NOMEFANTASIA,RAZAOSOCIAL,CODIGO,BLOQUEIAVENDA,CNPJCPF,CODCLI from cliente " 
                        + "where RAZAOSOCIAL like 'A%' and CODVENDED = '00000074' order by NOMEFANTASIA").close();
      
      // One more realistic sample.
      if (driver.exists("cadclientes"))
         driver.executeUpdate("drop table cadclientes");
      driver.execute("create table CADCLIENTES (id int, CODCLIPRE char(5), RAZAO char(60), ENDERECO char(40), REFENDER char(25), BAIRRO char(20), " 
   + "CIDADE char(20), UF char(2), CEP char(8), CNPJ char(14), CPF char(11), INSCEST char(15), INSCMUNIC char(15), RG char(15), ORGEMIS char(10), " 
   + "TEL char(14), FAX char(14), CONTATO char(15), DATCAD datetime, CODVEND char(3), PROXCLI char(5), EMAIL char(60), CODESTAB char(3), " 
   + "TIPOCLI char(1), ENV char(1), REENV char(1), FANTASIA char(40), SUBBAIRRO char(20), primary key (id))");
      driver.executeUpdate("insert into CADCLIENTES values (-1, null, 'TESTE', 'ASD', null, 'ASD', 'ASD', 'AL', '22222222', null, '10101142714', " 
                         + "null, null, 'ASD', 'ASD', '123', '', 'ASD', null, '029', null, '', '001', 'F', 'N', 'N', 'TESTE', '')");
      driver.executeUpdate("update CADCLIENTES set id = 37, CODCLIPRE = 'YTPEE', ENV = 'S' where id = -1");
      (resultSet = driver.executeQuery("select id from CADCLIENTES")).first();
      assertEquals(37, resultSet.getInt("id"));
      resultSet.close();
      driver.closeAll();
   };

   /** 
    * Tests a query.
    *
    * @param driver The connection with Litebase.
    * @param sql The query to be executed
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
    * Tests Litebase queries with and without indices.
    * 
    * @param driver
    */
   private void testQueries(LitebaseConnection driver)
   {
      // Exact key search.
      assertEquals(2, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'guilherme'"));
      assertEquals(19, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'guilherme' and FIRST_NAME is not null"));

      // Exact search with OR.
      assertEquals(5, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'guilherme' or FIRST_NAME = 'roberto'"));
      assertEquals(7, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'roberto' or city = 'Porto Seguro'"));

      // Exact search with AND.
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and city = 'Paraty'"));
      assertEquals(3, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and YEARS_EXP_C = 6"));

      // Exact query with function.
      assertEquals(2, executeQuery(driver, "select * from PERSON where upper(FIRST_NAME) = 'GUILHERME'"));
      assertEquals(19, executeQuery(driver, "select * from PERSON where upper(FIRST_NAME) != 'GUILHERME'"));
      
      // Non-exact search with OR.
      assertEquals(13, executeQuery(driver, "select * from PERSON where salary_prev < 3000 or years_exp_C = 10 or years_exp_java > 20"));
      assertEquals(6, executeQuery(driver, "select * from PERSON where city = 'Ouro Preto' or SALARY_CUR > 10000 or years_exp_C = 12"));
      assertEquals(9, executeQuery(driver, "select * from PERSON where city = 'Paraty' or years_exp_Java > 10 or years_exp_C > 18"));
      assertEquals(19, executeQuery(driver, "select * from PERSON where (years_exp_C > 10 or years_exp_C < 10) and years_exp_C is not null"));

      // Non-exact search with AND.
      assertEquals(2, executeQuery(driver, "select * from PERSON where salary_prev < 3000 and years_exp_C = 10 and years_exp_java > 3"));
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' and SALARY_CUR > 3000 and years_exp_C = 6"));
      assertEquals(2, executeQuery(driver, "select * from PERSON where city = 'Rio de Janeiro' and years_exp_Java > 10 and years_exp_C > 3"));
      assertEquals(2, executeQuery(driver, "select * from PERSON where city = 'Rio de Janeiro' and years_exp_C < 10 and years_exp_C > 3"));

      // Complex queries.
      assertEquals(5, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 and years_exp_C = 10) or city = 'Natal'"));
      assertEquals(8, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'maria' or (SALARY_CUR > 3000 and years_exp_C = 6) " 
                                                                                                               + "or city = 'Fortaleza'"));
      assertEquals(5, executeQuery(driver, "select * from PERSON where city = 'Rio de Janeiro' and (years_exp_Java > 10 or years_exp_C > 3)"));
      assertEquals(2, executeQuery(driver, "select * from PERSON where (salary_prev < 3000 or years_exp_C = 10) and city = 'Porto Seguro' " 
                                                                                                             + "and years_exp_C > 2"));
      
      // Null queries.
      assertEquals(20, executeQuery(driver, "select * from PERSON where FIRST_NAME != 'null' and FIRST_NAME is not null"));
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME = 'null'"));
      assertEquals(21, executeQuery(driver, "select * from PERSON where FIRST_NAME is not null"));
      assertEquals(1, executeQuery(driver, "select * from PERSON where FIRST_NAME is null"));
   }
}
