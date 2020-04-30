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
 * Tests sequences of inserts and deletes
 */
public class TestCachedRows extends TestCase // Catchs error found in 555_3
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      String[] querysSQL =
      {
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 38",
         "INSERT INTO ACT_CLIENTE VALUES (38L, '38', 'ADENILZA xxxxxxxxxxxxxxxxxxxxxE', 'ADENILZA 111111111111111111111E', '11.111.222//4443-22', " 
                                + "'', 1, 1, '', 4, '3423423421', '', '', '1', 20051110101123, 20051110101123, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 114L",
         "INSERT INTO ACT_CLIENTE VALUES (114L, '114', 'NIETO yyyyyyyyyyyyyyyyyyyyE', 'NIETO 2222222222222222222EE', '22.222.333//3333-33', '', 1, " 
                                + "1, '', 4, '4342342423', '', '', '1', 20051110101123, 20051110101123, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 161L",
         "INSERT INTO ACT_CLIENTE VALUES (161L, '161', 'ANTONIO bbbbbbbbbbbbbbbbbbbbbbE', 'ANTONIO 33333333333333333333333', '44.444.444//4441-44', " 
                                + "'', 1, 1, '', 4, '5435656458', '', '', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 421L",
         "INSERT INTO ACT_CLIENTE VALUES (421L, '421', 'CLAUDIOMIR cccccccccccccccccMEE', 'CLAUDIOMIR 444444444444444444EE', '55.555.555//5555-26', " 
                                + "'', 1, 1, '', 4, '', '', '', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 443L",
         "INSERT INTO ACT_CLIENTE VALUES (443L, '443', 'MARIA dddddddddddddddddSO', 'MARIA 55555555555555555SO', '777.777.777-20', '', 1, 1, '', 4, "
                                + "'6756756756', '', '', '2', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 941L",
         "INSERT INTO ACT_CLIENTE VALUES (941L, '941', 'J.B.eeeeeeeeeeeeeeeeeeeeeeeeeeE', 'J.B.6666666666666666666666666EE', '88.888.888//8889-83', " 
                                + "'', 1, 1, '', 4, '8655676565', '', '', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 968L",
         "INSERT INTO ACT_CLIENTE VALUES (968L, '968', 'JARDELIO fffffffffffffffffffffffE', 'JARDELIO 7777777777777777777777EE',"
                                + " '99.999.999//9999-82', '', 1, 1, '', 4, '7656456547', '', '', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1217L",
         "INSERT INTO ACT_CLIENTE VALUES (1217L, '1217', 'N.gggggggggggggggE', 'N.C.8888888888888E', '00.000.000//1111-16', '', 1, 1, '', 4, "
                                + "'4532432439', '', '', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1450L",
         "INSERT INTO ACT_CLIENTE VALUES (1450L, '1450', 'OTTO COMhhhhhhhhhhhhhhhhhhhhhhhE', 'OTTO 9999999999999999999999999EE', "
                                + "'22.222.222//2222-17', '', 1, 1, '', 4, '4535345340', '', '', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1585L",
         "INSERT INTO ACT_CLIENTE VALUES (1585L, '1585', 'OROSINO iiiiiiiiiiiiiiiiiiiE', 'OROSINO 0000000000000000000E', "
                                + "'33.333.333//3333-33', '', 1, 1, '', 4, '5435345357', '', '30        ', '1', 20051110101124, 20051110101124, 1)",
         "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1664L"
      };

      LitebaseConnection driver = AllTests.getInstance("Test");
      int numSQL = querysSQL.length;

      if (driver.exists("act_cliente")) driver.executeUpdate("drop table act_cliente");
      driver.execute("create table act_cliente (actclienteid long, actcodcliente char(30), actrazaosocial char(100), "
            + "actnomefantasia char(100), actie char(20), actcnpj char(20), actstatus long, acttipocliente long, actmail char(255), "
            + "actvendedorid long, acttelefone char(18), actobservacao char(255), actfax char(18), actcodtabela char(20), "
            + "actdatinclusao long, actdatalteracao long, actusuarioid long)");

      int i = -1;
      while (++i < numSQL)
         assertEquals(i & 1, driver.executeUpdate(querysSQL[i])); // Delete must return 0; insert must return 1.

      driver.closeAll();
      
      // rnovais@570_77
      ResultSet rs = (driver = AllTests.getInstance("Test"))
                             .executeQuery("select actrazaosocial from act_cliente where actrazaosocial='ADENILZA xxxxxxxxxxxxxxxxxxxxxE'");
      assertEquals(1, rs.getRowCount()); // Would be 6 without 555_3 fix.
      rs.close();
      assertEquals(10, (rs = driver.executeQuery("select * from act_cliente")).getRowCount());
      rs.close();
      driver.closeAll();
   }
}
