/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: TestDeleteAndPurge.java,v 1.9.4.1.2.2.4.13 2011-02-17 20:26:03 juliana Exp $

package samples.sys.testcases;

import litebase.*;
import totalcross.unit.*;

/**
 * Tests if delete and purge behave as expected.
 */
public class TestDeleteAndPurge extends TestCase
{
   public void testRun()
   {
      // First inserts the items into a new table.
      LitebaseConnection driver = AllTests.getInstance("Test"); 
      if (driver.exists("CIDADE"))
         driver.executeUpdate("drop table CIDADE");
      driver.execute("create table CIDADE(CODIGO int, NOME char(60))");
      PreparedStatement psInsertCidade = driver.prepareStatement("insert into CIDADE(CODIGO, NOME) values(?,?)");
      driver.setRowInc("cidade", 200);
      
      int i = 200;
      while (--i >= 0)
      {
         psInsertCidade.clearParameters();
         psInsertCidade.setInt(0, i);
         psInsertCidade.setString(1,"NOME DA CIDADE " + i);
         assertEquals(1, psInsertCidade.executeUpdate());
      }
      driver.setRowInc("cidade", -1);
      driver.closeAll();
      
      // Now deletes and try to insert again.
      driver = AllTests.getInstance("Test"); // rnovais@_570_77
      try
      {
         assertEquals(200, driver.executeUpdate("delete CIDADE"));
      } 
      catch (DriverException exception) // guich@553_10: this error occured when the table name was not being converted to lowercase.
      {
         fail("Exception thrown: " + exception.getMessage());
      } 
      assertEquals(200, driver.purge("CIDADE"));

      psInsertCidade = driver.prepareStatement("insert into CIDADE(CODIGO, NOME) values(?,?)");
      driver.setRowInc("cidade", 201);
      i = 200;
      while (--i >= 0)
      {
         psInsertCidade.clearParameters();
         psInsertCidade.setInt(0, i);
         psInsertCidade.setString(1,"NOME DA CIDADE " + i);
         assertEquals(1, psInsertCidade.executeUpdate());
      }
      assertEquals(1, psInsertCidade.executeUpdate());
      driver.setRowInc("cidade", -1);
      assertEquals(201, driver.getRowCount("CIDADE"));
      ResultSet resultSet = driver.executeQuery("select * from cidade where codigo = 0");
      assertEquals(2, resultSet.getRowCount());
      resultSet.close();
      driver.closeAll();
      testRowIdAfterPurge();
   }
   
   /**
    * Tests if the rowid values are not 
    */
   private void testRowIdAfterPurge() // rnovais@570
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); 
      if (driver.exists("PALM") )
         driver.executeUpdate("drop table PALM");
      driver.execute("create table PALM (cod int)");
      driver.executeUpdate("INSERT INTO palm (cod) values (31)");
      driver.executeUpdate("INSERT INTO palm (cod) values (32)");
      driver.executeUpdate("INSERT INTO palm (cod) values (33)");
      driver.executeUpdate("INSERT INTO palm (cod) values (34)");
      driver.executeUpdate("INSERT INTO palm (cod) values (35)");
      driver.executeUpdate("DELETE FROM palm where rowid=5"); // Deletes the last one.
      driver.purge("palm");
      driver.closeAll();
      driver = AllTests.getInstance("Test"); 
      driver.executeUpdate("INSERT INTO palm (cod) values (36)");
      ResultSet rs = driver.executeQuery("SELECT rowid FROM palm where cod = 36");
      assertTrue(rs.next());
      assertEquals("6", rs.getString(1));
      rs.close();
      driver.closeAll();
   }
}
