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
import totalcross.sys.Time;
import totalcross.sys.Vm;
import totalcross.unit.*;
import totalcross.util.Date;

/** 
 * Tests What happens when the driver is closed and tests <code>processLogs()</code>.
 */
public class TestClosedLitebaseAndProcessLogs extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");

      // Tests prepared statement methods with the driver closed.
      if (driver.exists("tabps"))
         driver.executeUpdate("drop table tabps");
      driver.execute("create table tabps (name char(30))");
      PreparedStatement psIns = driver.prepareStatement("insert into tabps values ('?')");
      PreparedStatement psSel = driver.prepareStatement("select * from tabps where name = ?");

      psIns.setString(0, "vera");
      psIns.executeUpdate();
      psSel.setString(0, "vera");
      ResultSet resultSet = psSel.executeQuery();
      ResultSetMetaData rsMetaData = resultSet.getResultSetMetaData();
      
      driver.closeAll();
      
      // All method calls must fail.
      
      // PreparedStatement methods.
      try
      {
         psSel.setString(0, "vera");
         fail("1");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psSel.executeQuery();  
         fail("2");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.clearParameters();  
         fail("3");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setBlob(0, null); 
         fail("4");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setDate(0, new Date());
         fail("5");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setDateTime(0, (Date)new Date()); 
         fail("6");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setDateTime(0, (Time)new Time());  
         fail("7");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setDouble(0, 0);  
         fail("8");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setInt(0, 0);
         fail("9");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setLong(0, 0);  
         fail("10");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setNull(0); 
         fail("11");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         psIns.setString(0, null); 
         fail("12");
      } 
      catch (IllegalStateException exception) {}
      try
      {
   	   psIns.executeUpdate();
         fail("13");
      } 
      catch (IllegalStateException exception) {}
      
      // LitebaseConnection methods.
      try
      {
         driver.convert("tabps");
         fail("14");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.closeAll();
         fail("15");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.execute("");
         fail("16");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.executeQuery("");
         fail("17");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.executeUpdate("");
         fail("18");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.exists("tabps");
         fail("19");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.getCurrentRowId("tabps");
         fail("20");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.getRowCount("tabps");
         fail("21");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.getRowCountDeleted("tabps");
         fail("22");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.getRowIterator("tabps");
         fail("23");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.prepareStatement("");
         fail("24");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.purge("tabps");
         fail("25");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.recoverTable("tabps");
         fail("26");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.setRowInc("tabps", 100);
         fail("27");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         driver.isOpen("tabps");
         fail("28");
      } 
      catch (IllegalStateException exception) {}
      
      // ResultSet methods.
      try
      {
         resultSet.absolute(0);
         fail("29");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.afterLast();
         fail("30");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.beforeFirst();
         fail("31");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.first();
         fail("32");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getBlob(0);
         fail("33");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getBlob("name");
         fail("34");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getChars(0);
         fail("35");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getChars("name");
         fail("36");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getDate(0);
         fail("37");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getDate("name");
         fail("38");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getDateTime(0);
         fail("39");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getDateTime("name");
         fail("40");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getDouble(0);
         fail("41");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getDouble("name");
         fail("42");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getFloat(0);
         fail("43");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getFloat("name");
         fail("44");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getInt(0);
         fail("45");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getInt("name");
         fail("46");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getLong(0);
         fail("47");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getLong("name");
         fail("48");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getResultSetMetaData();
         fail("49");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getRow();
         fail("50");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getRowCount();
         fail("51");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getShort(0);
         fail("52");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getShort("name");
         fail("53");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getString(0);
         fail("54");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getString("name");
         fail("55");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getStrings();
         fail("56");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.getStrings(-1);
         fail("57");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.isNull(0);
         fail("58");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.isNull("name");
         fail("59");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.last();
         fail("60");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.next();
         fail("61");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.prev();
         fail("62");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.relative(0);
         fail("63");
      }
      catch (IllegalStateException exception) {}
      try
      {
         resultSet.setDecimalPlaces(0, 2);
         fail("64");
      }
      catch (IllegalStateException exception) {}
      
      // ResultSetMetaData methods.
      try
      {
         rsMetaData.getColumnCount();
         fail("65");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getColumnDisplaySize(0);
         fail("66");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getColumnLabel(0);
         fail("67");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getColumnTableName(0);
         fail("68");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getColumnTableName("name");
         fail("69");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getColumnType(0);
         fail("70");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getColumnTypeName(0);
         fail("71");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.hasDefaultValue(0);
         fail("72");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.hasDefaultValue("name");
         fail("73");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.isNotNull(0);
         fail("74");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.isNotNull("name");
         fail("75");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getPKColumnIndices("name");
         fail("76");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getPKColumnNames("name");
         fail("77");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getDefaultValue(0);
         fail("78");
      }
      catch (IllegalStateException exception) {}
      try
      {
         rsMetaData.getDefaultValue("name");
         fail("79");
      }
      catch (IllegalStateException exception) {}
      
      driver = null;
      resultSet.close();
      Vm.gc();
      
      if ((driver = AllTests.getInstance("Test")).exists("PRODUTO"))
      	driver.executeUpdate("drop table produto");

      // guich@566_31: another case that was causing a "Memory error".
      // Tets the logging processing.
      String[] sql =
      {
         "new LitebaseConnection(Test,null)",
         "create table PRODUTO (IDPRODUTO int, IDPRODUTOERP char(10), IDGRUPOPRODUTO int, IDSUBGRUPOPRODUTO int, IDEMPRESA char(20), "
               + "DESCRICAO char(100), UNDCAIXA char(10), PESO float, UNIDADEMEDIDA char(3), EMBALAGEM char(10), PORCTROCA float, PERMITETROCA int)",
         "create index IDX_PRODUTO_1 on PRODUTO(IDPRODUTO)",
         "create index IDX_PRODUTO_2 on PRODUTO(IDGRUPOPRODUTO)",
         "create index IDX_PRODUTO_3 on PRODUTO(IDEMPRESA)",
         "create index IDX_PRODUTO_4 on PRODUTO(DESCRICAO)",
         "closeAll",
         "new LitebaseConnection(Test,null)",
      };
      driver.closeAll();
      assertNotNull(driver = LitebaseConnection.processLogs(sql, null, false));
      assertEquals(1, driver
                  .executeUpdate("insert into PRODUTO values(1, '19132',2, 1, '1, 2, 3', 'ABSORVENTE SILHO ABAS', '5', 13, 'PCT', '20X30', 10.f, 0)"));
      
      // Tests if gc won't make the Litebase instance close.
      int i = 100;
      while (--i >= 0)
      {
         testTemporaryConnection(); // Gets another Litebase instance.
         driver.exists("tableName"); // The connection can't be closed because of the garbage collector.
      }
      driver.closeAll();
      
   }
   
   /**
    * Gets the current Litebase Instance and does nothing with that. 
    */
   private void testTemporaryConnection()
   {
      AllTests.getInstance("Test");
   }
}
