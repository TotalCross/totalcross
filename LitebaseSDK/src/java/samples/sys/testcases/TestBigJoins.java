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
import totalcross.util.Random;

/**
 * Tests joins with big table to ensure that the use indices.
 */
public class TestBigJoins  extends TestCase
{
   /**
    * The connection with Litebase.
    */
   private LitebaseConnection connection = AllTests.getInstance("Test");
   
   /**
    * Main test method.
    */
   public void testRun()
   {
      String[] tables;
      test2Tables();
      test3Tables();
      test4Tables();
      
      // Lists all the table names and tests if all tables of this test are in the list.
      assertGreaterOrEqual((tables = connection.listAllTables()).length, 9);
      hasTableName(tables, "animal");
      hasTableName(tables, "ordenha");
      hasTableName(tables, "precos");
      hasTableName(tables, "produtos");
      hasTableName(tables, "estoque");
      hasTableName(tables, "componenttype");
      hasTableName(tables, "componentdefinition");
      hasTableName(tables, "component");
      hasTableName(tables, "panel");
      
      connection.closeAll();
   }

   /**
    * Tests a very big join with 2 tables and aggregation.
    */
   private void test2Tables()
   {
      // Drops the tables.
      if (connection.exists("animal"))
         connection.executeUpdate("drop table animal");
      if (connection.exists("ordenha"))
         connection.executeUpdate("drop table ordenha");
      
      // Creates the tables.
      connection.execute("CREATE TABLE ANIMAL (ID INT, APELIDO CHAR(50) NOT NULL, CODIGO_PROPRIEDADE INT NOT NULL, CODIGO_CLIENTE INT NOT NULL)");
      connection.execute("CREATE TABLE ORDENHA (NUMERO INT NOT NULL, ID_ANIMAL INT NOT NULL, LEITE DOUBLE, DATA_HORA DATETIME)");
      
      // Prepared statements.
      PreparedStatement psInsertAnimal = connection.prepareStatement("INSERT INTO ANIMAL VALUES (?, ?, ?, ?)"),
                        psInsertOrdenha = connection.prepareStatement("INSERT INTO ORDENHA VALUES (?, ?, ?, ?)");
      
      // Huge inserts.
      connection.setRowInc("animal", 10000);
      connection.setRowInc("ordenha", 30000);

      int id = 0,
          num;
      Time time = new Time();
      
      // Insert data.
      while (++id <= 10000) 
      {
         psInsertAnimal.setInt(0, id);
         psInsertAnimal.setString(1, "Apelido "+id);
         psInsertAnimal.setInt(2, 1);
         psInsertAnimal.setInt(3, 34530);
         psInsertAnimal.executeUpdate();
         
         num = 0;
         while (++num <= 3) 
         {
            time.update();
            psInsertOrdenha.setInt(0, num); 
            psInsertOrdenha.setInt(1, id); 
            psInsertOrdenha.setDouble(2, 21.5);
            psInsertOrdenha.setDateTime(3, time);
            psInsertOrdenha.executeUpdate();
         }
      }
      
      // Stops batch loading.
      connection.setRowInc("animal", -1);
      connection.setRowInc("ordenha", -1);
      
      // Adds the primary keys.
      connection.executeUpdate("alter table animal add primary key (id)");
      connection.executeUpdate("alter table ordenha add primary key (numero, id_animal)");
      
      // Adds the indices.
      connection.execute("CREATE INDEX idx on ANIMAL(CODIGO_CLIENTE)");
      connection.execute("CREATE INDEX idx on ANIMAL(CODIGO_PROPRIEDADE)");
      connection.execute("CREATE INDEX idx on ORDENHA(ID_ANIMAL)");
      connection.execute("CREATE INDEX idx on ORDENHA(LEITE)");
 
      // Does the best select ordering.
      id = Vm.getTimeStamp();
      ResultSet resultSet = connection.executeQuery("SELECT SUM(ORDENHA.LEITE) AS TOTAL FROM ORDENHA, ANIMAL WHERE ORDENHA.LEITE > 0 " 
                                      + "AND ANIMAL.CODIGO_CLIENTE = 34530 AND ANIMAL.CODIGO_PROPRIEDADE = 1 AND ANIMAL.ID = ORDENHA.ID_ANIMAL");     
      resultSet.first();
      assertEquals(645000.0, resultSet.getDouble(1), 0.0001);
      output((Vm.getTimeStamp() - id) + " ms.");
      resultSet.close();
   }
   
   /**
    * Tests a join with 3 tables.
    */
   private void test3Tables()
   {
      Random r = new Random(100331);
      
      // Drops the tables.
      if (connection.exists("precos"))
         connection.executeUpdate("drop table precos");
      if (connection.exists("produtos"))
         connection.executeUpdate("drop table produtos");
      if (connection.exists("estoque"))
         connection.executeUpdate("drop table estoque");
      
      // Creates the tables.
      connection.execute("create table precos (CODPROD char(14), CODCONDPAG char(14), PRCVENDALONG long, codlista char(3))");
      connection.execute("create table produtos (CODPROD char(14), CODGRP char(14), SITUACAO char(14), DESCRPROD char(40), UNIDADE char(14))");
      connection.execute("create table estoque (CODPROD char(14), num int, cod_un_neg char(14))");
      
      // Prepared statements.
      PreparedStatement psPrecos = connection.prepareStatement("insert into precos values(?, ?, ?, ?)"),
                        psProdutos = connection.prepareStatement("insert into produtos values(?, ?, ?, ?, ?)"),
                        psEstoque = connection.prepareStatement("insert into estoque values (?, ?, ?)");
      
      // Huge inserts.
      connection.setRowInc("precos", 10000);
      connection.setRowInc("produtos", 5000);
      connection.setRowInc("estoque", 10000);  
        
      int i = -1,
          j;
      
      // Inserts data.
      while (++i < 2000)
      {
         j = -1;
         while (++j < 5)
         {
            if (r.between(0, 1) == 1)
            {
               psPrecos.setString(0, "nome" + i);
               psPrecos.setString(1, "nome" + i);
               psPrecos.setLong(2, i + j);
               psPrecos.setString(3, "l" + j);
               psPrecos.executeUpdate();
            }
            psEstoque.setString(0, "nome" + i);
            psEstoque.setInt(1, j);
            psEstoque.setString(2, "nome" + j);
            psEstoque.executeUpdate();
         }
         psProdutos.setString(0, "nome" + i);
         psProdutos.setString(1, "nome" + i);
         psProdutos.setString(2, "nome" + i);
         psProdutos.setString(3, "nome" + i);
         psProdutos.setString(4, "nome" + i);
         psProdutos.executeUpdate();
      }
         
      // Stops batch loading.
      connection.setRowInc("precos", -1);
      connection.setRowInc("produtos", -1);
      connection.setRowInc("estoque", -1);
                  
      // Creates the indices.
      connection.execute("create index idx on precos (CODPROD)");
      connection.execute("create index idx on precos (CODlista)");
      connection.execute("create index idx on produtos (CODPROD)");
      connection.execute("create index idx on estoque (CODPROD)");

      i = Vm.getTimeStamp();
      j = 0;
      
      ResultSet resultSet = connection.executeQuery("select * from precos, produtos, estoque where estoque.CODPROD = produtos.CODPROD and produtos.CODPROD = precos.CODPROD and codlista = 'l1'");
      String column2;
      
      output((Vm.getTimeStamp() - i) + " ms.");
      
      try // Checks the results.
      {
         while (resultSet.next())
         {
            assertEquals(resultSet.getString(1), column2 = resultSet.getString(2));
            assertEquals(Convert.toLong(column2.substring(4)), resultSet.getLong(3) - 1);
            assertEquals("l1", resultSet.getString(4));
            assertEquals(column2, resultSet.getString(5));
            assertEquals(column2, resultSet.getString(6));
            assertEquals(column2, resultSet.getString(7));
            assertEquals(column2, resultSet.getString(8));
            assertEquals(column2, resultSet.getString(9));
            assertEquals(column2, resultSet.getString(10));
            assertEquals(j, resultSet.getInt(11));
            assertEquals(j, Convert.toInt(resultSet.getString(12)));
            j = (j + 1) % 5;
         }
      }
      catch (InvalidNumberException exception) {}
      
      resultSet.close();
   }
   
   /**
    * Tests many joins with 4 tables which answers only one row.
    */
   private void test4Tables()
   {
      // Drops the tables.       
      if (connection.exists("ComponentType"))
         connection.executeUpdate("drop table ComponentType");
      if (connection.exists("ComponentDefinition"))
         connection.executeUpdate("drop table ComponentDefinition");
      if (connection.exists("Panel"))
         connection.executeUpdate("drop table Panel");
      if (connection.exists("Component"))
         connection.executeUpdate("drop table Component");
      
      // Creates the tables.
      connection.execute("create table ComponentType (id long, name char(20))");
      connection.execute("create table ComponentDefinition (id long, name char(20))");
      connection.execute("create table Panel (id long, name char(20))");
      connection.execute("create table Component (id long, typeid long, componentdefinitionid long, panelid long, name char(20))");   
   
      // Inserts data into ComponentType.
      int j = 0;
      PreparedStatement ps = connection.prepareStatement("insert into ComponentType values (?, ?)");
      connection.setRowInc("ComponentType", 4);
      while (++j <= 4)
      {
         ps.setLong(0, j);
         ps.setString(1, "Type " + j);
         ps.executeUpdate();
      }
      connection.setRowInc("ComponentType", -1);
      
      // Inserts data into ComponentDefinition.
      j = 0;
      ps = connection.prepareStatement("insert into ComponentDefinition values (?, ?)");
      connection.setRowInc("ComponentDefinition", 40);
      while (++j <= 40)
      {
         ps.setLong(0, j);
         ps.setString(1, "Definition " + j);
         ps.executeUpdate();
      }
      connection.setRowInc("ComponentDefinition", -1);
      
      // Inserts data into Panel.
      j = 0;
      ps = connection.prepareStatement("insert into Panel values (?, ?)");
      connection.setRowInc("Panel", 400);
      while (++j <= 400)
      {
         ps.setLong(0, j);
         ps.setString(1, "Panel " + j);
         ps.executeUpdate();
      }
      connection.setRowInc("Panel", -1);
      
      // Inserts data into Component.
      j = 0;
      ps = connection.prepareStatement("insert into Component values (?, ?, ?, ?, ?)");
      connection.setRowInc("Component", 4000);
      while (++j <= 4000)
      {
         ps.setLong(0, j);
         ps.setLong(1, (j / 1000) + 1);
         ps.setLong(2, (j / 100) + 1);
         ps.setLong(3, (j / 10) + 1);
         ps.setString(4, "Component " + j);
         ps.executeUpdate();
      }
      connection.setRowInc("Component", -1);
      
      // Adds the primary keys.
      connection.executeUpdate("alter table ComponentType add primary key (id)");
      connection.executeUpdate("alter table ComponentDefinition add primary key (id)");
      connection.executeUpdate("alter table Panel add primary key (id)");
      connection.executeUpdate("alter table Component add primary key (id)");
      
      // Creates the indices.
      connection.execute("create index typeid on component(typeid)");
      connection.execute("create index compdefid on component(componentdefinitionid)");
      connection.execute("create index panelid on component(panelid)");
      
      String baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, Panel, ComponentDefinition, " 
                                                                                                                            + "ComponentType where ",
             d = "Component.componentdefinitionid = ComponentDefinition.id",
             i = "Component.id = 1",
             p = "Component.panelid = Panel.id",
             t = "Component.typeid = ComponentType.id";
      
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, Panel, ComponentType, " 
                                                                                                                     + "ComponentDefinition where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentDefinition, Panel, " 
                                                                                                              + "ComponentType where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentDefinition, ComponentType, " 
                                                                                                                                   + "Panel where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentType, Panel, " 
                                                                                                              + "ComponentDefinition where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Component, ComponentType, ComponentDefinition, " 
                                                                                                                             + "Panel where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, Component, ComponentDefinition, " 
                                                                                                                     + "ComponentType where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, Component, ComponentType, " 
                                                                                                          + "ComponentDefinition where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentDefinition, Component, " 
                                                                                                          + "ComponentType where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentDefinition, ComponentType, " 
                                                                                                                               + "Component where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentType, Component, " 
                                                                                                          + "ComponentDefinition where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);
      
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from Panel, ComponentType, ComponentDefinition, " 
                                                                                                                         + "Component where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Component, Panel, " 
                                                                                                                        + "ComponentType where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Component, ComponentType," 
                                                                                                                         + " Panel where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Panel, Component, " 
                                                                                                                        + "ComponentType where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);
      
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, Panel, ComponentType, " 
                                                                                                                               + "Component where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, ComponentType, Component," 
                                                                                                                       + " Panel where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentDefinition, ComponentType, Panel, " 
                                                                                                                        + "Component where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Component, Panel, " 
                                                                                                                  + "ComponentDefinition where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);
      
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Component, ComponentDefinition," 
                                                                                                                            + " Panel  where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);
      
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Panel, Component, " 
                                                                                                   + "ComponentDefinition where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, Panel, ComponentDefinition, Component where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);
      
      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, ComponentDefinition, Component," 
                                                                                                                   + " Panel where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);

      baseSQL = "select Component.name, ComponentType.name, ComponentDefinition.name, Panel.name from ComponentType, ComponentDefinition, Panel, " 
                                                                                                                  + "Component where ";
      executeTestQuery(baseSQL + d + " and " + i + " and " + p + " and " + t);
      executeTestQuery(baseSQL + d + " and " + i + " and " + t + " and " + p);
      executeTestQuery(baseSQL + d + " and " + p + " and " + i + " and " + t);
      executeTestQuery(baseSQL + d + " and " + p + " and " + t + " and " + i);
      executeTestQuery(baseSQL + d + " and " + t + " and " + i + " and " + p);
      executeTestQuery(baseSQL + d + " and " + t + " and " + p + " and " + i);
      executeTestQuery(baseSQL + i + " and " + d + " and " + p + " and " + t);
      executeTestQuery(baseSQL + i + " and " + d + " and " + t + " and " + p);
      executeTestQuery(baseSQL + i + " and " + p + " and " + d + " and " + t);
      executeTestQuery(baseSQL + i + " and " + p + " and " + t + " and " + d);
      executeTestQuery(baseSQL + i + " and " + t + " and " + d + " and " + p);
      executeTestQuery(baseSQL + i + " and " + t + " and " + p + " and " + d);
      executeTestQuery(baseSQL + p + " and " + d + " and " + i + " and " + t);
      executeTestQuery(baseSQL + p + " and " + d + " and " + t + " and " + i);
      executeTestQuery(baseSQL + p + " and " + i + " and " + d + " and " + t);
      executeTestQuery(baseSQL + p + " and " + i + " and " + t + " and " + d);
      executeTestQuery(baseSQL + p + " and " + t + " and " + d + " and " + i);
      executeTestQuery(baseSQL + p + " and " + t + " and " + i + " and " + d);
      executeTestQuery(baseSQL + t + " and " + d + " and " + i + " and " + p);
      executeTestQuery(baseSQL + t + " and " + d + " and " + p + " and " + i);
      executeTestQuery(baseSQL + t + " and " + i + " and " + d + " and " + p);
      executeTestQuery(baseSQL + t + " and " + i + " and " + p + " and " + d);
      executeTestQuery(baseSQL + t + " and " + p + " and " + d + " and " + i);
      executeTestQuery(baseSQL + t + " and " + p + " and " + i + " and " + d);
   }
   
   /**
    * Tests if all queries return only one row and if they are fast enough.
    * 
    * @param sql The string to be tested.
    */
   private void executeTestQuery(String sql)
   {
      int time = Vm.getTimeStamp();
      ResultSet resultSet = connection.executeQuery(sql);
      assertEquals(1, resultSet.getRowCount());
      assertGreater(1000, Vm.getTimeStamp() - time);
      resultSet.close();
   }
   
   /**
    * Asserts that a given table name is in the table list. 
    * 
    * @param array The list of table names.
    * @param tableName The table name being searched.
    */
   private void hasTableName(String[] array, String tableName)
   {
      int length = array.length;
      
      while (--length >= 0)
         if (array[length].equals(tableName))
            return;
      fail();
   }
}
