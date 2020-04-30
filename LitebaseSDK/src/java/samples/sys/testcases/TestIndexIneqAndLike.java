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
 * Tests the use of indices with like and 
 * catch error found in 555_3
 */
public class TestIndexIneqAndLike extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); 
      int n = 120;
      if (driver.exists("cliente"))
         driver.executeUpdate("drop table cliente");
      driver.execute("create table CLIENTE(CODIGO int, NOME char(60) NOCASE)");
      driver.execute("CREATE INDEX IDX ON cliente(NoME)");
      driver.execute("CREATE INDEX IDX ON cliente(codigo)");
      PreparedStatement ps = driver.prepareStatement("insert into cliente values (?,?)");
      StringBuffer sb = new StringBuffer("nOme");
      driver.setRowInc("cliente", n);
      int i = 120;
      while (--i >= 0)
         if (i % 10 != 0)
         {
            sb.setLength(4);
            ps.setInt(0, i);
            ps.setString(1, sb.append(i).toString());
            ps.executeUpdate();
         }
      driver.setRowInc("cliente", -1);

      // Select using like.
      testResult(driver.executeQuery("select * from cliente where nome like 'nome2%'"), 10);
      testResult(driver.executeQuery("select * from cliente where nome like 'Nome1%'"), 28);
      
      // Select using not not like. 
      testResult(driver.executeQuery("select * from cliente where not nome not like 'nome2%'"), 10);
      testResult(driver.executeQuery("select * from cliente where not nome not like 'Nome1%'"), 28);
      
      // Select using inequalities.
      testResult(driver.executeQuery("select * from cliente where codigo >= 26 and codigo <= 36"), 10);
      testResult(driver.executeQuery("select * from cliente where not codigo < 26 and not codigo > 36"), 10);
      testResult(driver.executeQuery("select * from cliente where not (codigo < 26 or codigo > 36)"), 10);
      testResult(driver.executeQuery("select * from cliente where codigo >  26 and codigo <= 36"), 9);
      testResult(driver.executeQuery("select * from cliente where not codigo <= 26 and not codigo > 36"), 9);
      testResult(driver.executeQuery("select * from cliente where not (codigo <= 26 or codigo > 36)"), 9);
      testResult(driver.executeQuery("select * from cliente where codigo >  26 and codigo <  36"), 8);
      testResult(driver.executeQuery("select * from cliente where codigo >= 26"), 85);
      testResult(driver.executeQuery("select * from cliente where codigo > 26"), 84);
      testResult(driver.executeQuery("select * from cliente where codigo <= 26"), 24);
      testResult(driver.executeQuery("select * from cliente where codigo < 26"), 23);
      
      // nOme1, nOme2, nOme11-nOme19, nOme101-nOme109, nOme111-nOme119
      testResult(driver.executeQuery("select * from cliente where nome < 'Nome20'"),29); 
      
      driver.closeAll();
   }
   
   /** 
    * Tests if the result set returned the expected number of rows.
    * 
    * @param rs The result set.
    * @param expected The number of rows expected.
    */
   private void testResult(ResultSet rs, int expected)
   {
      int returned = rs.getRowCount();
      
      if (expected != returned)
      {
         while (rs.next())
            output(rs.getString(1) + " / " + rs.getString(2));
         assertEquals(expected, returned);
      }
   }
}