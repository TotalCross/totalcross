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
import totalcross.util.*;

/**
 * Tests indices with a lot of nodes.
 */
public class TestIndexRebalance extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test"); 
      if (driver.exists("CLIENTE"))
         driver.executeUpdate("drop table cliente");
      driver.execute("create table CLIENTE(CODIGO int primary key, NOME char(60), FANTASIA char(60),BAIRRO int)");
      driver.execute("create index IDX_CLIENTE_BAIRRO on CLIENTE(BAIRRO)");
      driver.execute("create index IDX_CLIENTE_NOME on CLIENTE(NOME)");
      driver.execute("create index IDX_CLIENTE_FANTASIA on CLIENTE(FANTASIA)");
      driver.execute("create index IDX_ROWID on CLIENTE(rowid)");
      driver.setRowInc("CLIENTE", 1125); // Requires two increments to fill all the 2250 rows.
      PreparedStatement psInsertCliente = driver.prepareStatement("insert into CLIENTE(CODIGO, NOME, FANTASIA, BAIRRO) values (?, ?, ?, ?)");
      PreparedStatement psDelete = driver.prepareStatement("delete from cliente where codigo=?");
      int i = -1;
      psInsertCliente.setInt(3, 2);
      try
      {
         Random r = new Random(100331);
         IntHashtable keys = new IntHashtable(8333);
         while (++i < 2250) // 250 of them will be deleted.
         {
            if (i % 100 == 0)
               status(Convert.toString(i).concat(" out of 2250"));
            
            int k;
            while (true) // make sure that a key that was not repeated will be got.
            {
               k = r.between(0, 100000);
               if ((k & 1) == 0)
                  k = -k;
               if (!keys.exists(k))
               {
                  keys.put(k,k);
                  break;
               }
            }
            psInsertCliente.setInt(0, k);
            psInsertCliente.setString(1,"NOME " + k);
            psInsertCliente.setString(2,"FANTASIA DO CLIENTE " + i);
            
            assertEquals(1, psInsertCliente.executeUpdate());
            if ((k & 7) == 0) // force some deletions
            {
               psDelete.setInt(0,k);
               assertEquals(1, psDelete.executeUpdate());
               try // Lets the key be used again 
               {
                  keys.remove(k);
               } 
               catch (ElementNotFoundException exception) {} 
            }

         }
         status("");
      }
      catch (PrimaryKeyViolationException exception)
      {
         fail("Primary key violation occured at " + i);
      }
      try // Tries to insert again the same last client.
      {
         psInsertCliente.executeUpdate();
         fail("Primary key not violated!");
      }
      catch (PrimaryKeyViolationException exception) {} // Test ok.

      driver.setRowInc("cliente", -1); // Sets rowInc to default.
      ResultSet resultSet = driver.executeQuery("select * from cliente where bairro = 2");
      assertEquals(1960, resultSet.getRowCount());
      resultSet.close();
      driver.closeAll();
      driver = AllTests.getInstance("Test");
      assertEquals(1960, (resultSet = driver.executeQuery("select * from cliente where bairro >= 2")).getRowCount());
      resultSet.close();
      int tempo = Vm.getTimeStamp();
      driver.purge("cliente");
      output("Purge took " + (Vm.getTimeStamp() - tempo) + " ms.");
      driver.execute("create index IDX on CLIENTE(rowid, nome)");
      assertEquals(1960, (resultSet = driver.executeQuery("select * from cliente where bairro <= 2")).getRowCount());
      resultSet.close();
      assertEquals(1960, driver.executeUpdate("update cliente set bairro = 3"));
      assertEquals(1960, (resultSet = driver.executeQuery("select * from cliente where bairro <= 3 and bairro >= 3")).getRowCount());
      resultSet.close();
      assertEquals(1960, (resultSet = driver.executeQuery("select * from cliente where nome like 'NOME %'")).getRowCount());
      resultSet.close();
      assertEquals(1960, driver.executeUpdate("delete from cliente"));
      driver.executeUpdate("drop table cliente");
      
      // Solved a index bug which could cause its corruption.
      if (driver.exists("t"))
         driver.executeUpdate("drop table t");
      driver.execute("create table t (x short)");
      driver.execute("create index idx on t(x)");
      i = 30;
      while (--i >= 0)
         driver.executeUpdate("insert into t values (1)");
      driver.executeUpdate("delete from t where rowid < 16");
      driver.executeUpdate("delete from t where rowid > 16");
      driver.executeUpdate("insert into t values (1)");
      driver.executeUpdate("delete from t where rowid = 16");
      
      driver.closeAll();
   }
}
