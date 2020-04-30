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
 * This one tests if the endianess is correct on all platforms.
 */
public class TestEndianess extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); 
      
      if (driver.exists("testendianess"))
         driver.executeUpdate("drop table testendianess");
      
      driver.execute("create table testendianess(ID_PRODUTO int , ID_FORNECEDOR int, NM_NOME char(30), VL_VALOR double, vl_long long, " 
                                                                                                                     + "vl_float float) ");
      driver.executeUpdate("insert into testendianess values (10 ,20,'guilherme',3.14D," + 0x1234567890123456L + ",3.15f)");
      

      ResultSet rs = driver.executeQuery("select * from testendianess where ID_PRODUTO = 10");
      assertTrue(rs.next());
      rs.setDecimalPlaces(4, 2);
      rs.setDecimalPlaces(6, 2);

      // Tests double.
      double d = rs.getDouble("VL_VALOR");
      assertEquals(3.14d, d, 0.01);
      assertEquals("3.14", Convert.toString(d, 2));
      assertEquals("3.14", rs.getString("vl_valor"));

      // Tests float,
      double f = (double)rs.getFloat("VL_float");
      assertEquals(3.15, f, 0.01);
      assertEquals("3.15", Convert.toString(f,2));
      assertEquals("3.15", rs.getString("vl_float"));

      // Tests long.
      long l = rs.getLong("VL_long");
      assertEquals(0x1234567890123456L, l);
      assertEquals(Convert.toString(0x1234567890123456L), Convert.toString(l));
      assertEquals(Convert.toString(0x1234567890123456L), rs.getString("vl_long"));

      rs.close();
      driver.closeAll();
   }
}
