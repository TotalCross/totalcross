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
      testPruebas();
   }
   
   /**
    * Tests if the rowid values are not wrong. 
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
      driver.executeUpdate("DELETE FROM palm where palm.rowid=5"); // Deletes the last one.
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
   
   /**
    * Tests if the table is not corrupted after some purges. 
    */
   private void testPruebas() // rnovais@570
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      
      if (driver.exists("motorCatalog"))
         driver.executeUpdate("drop table motorCatalog");
      driver.execute("create table motorCatalog(RFS int, rackID char(5), modelo char(12),serie char(18),error int, descripcion char(255))");
      driver.execute("CREATE INDEX IDX1Mot ON motorCatalog(RFS,rackID)");
      driver.execute("CREATE INDEX IDX3Mot ON motorCatalog(serie)");
      
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1250411014730',0,'')");
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1060511010528',0,'')");
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1040511002208',0,'')");
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1060511010316',0,'')");
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1040511002810',0,'')");
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1040511002458',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1040511003032',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'10900','0G-316-AA','1060511080240',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511163914',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511050258',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511080856',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511081104',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511005248',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511080640',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511080606',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11366','0G-316-AA','1060511080058',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511050050',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1050511152400',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511044944',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511182936',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511050216',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511050618',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511050336',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'11538','0G-316-AA','1060511184118',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1300411051452',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1270411015110',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1300411050724',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1150411180834',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1300411153840',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1300411154840',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1300411011122',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'12500','BG-313-AA','1300411154416',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1020511025150',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1020511023054',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1010511204142',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1020511025354',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1010511204308',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1020511025240',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1020511025534',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13104','BG-314-BA','1010511204640',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1060511164636',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1040511002532',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1050511231210',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1060511080206',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1060511080714',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1040511002922',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1040511003140',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'13919','0G-316-AA','1060511163836',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1060511080748',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1240411183034',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1060511011018',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1060511011318',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1240411183756',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1240411173532',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1240411182922',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14198','0G-316-AA','1060511011352',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1060511045038',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1060511080348',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1050511231410',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1060511081024',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1060511164422',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1040511203358',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1060511164542',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'14443','0G-316-AA','1060511011204',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1060511080132',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1060511010240',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1060511080024',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1060511011242',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1040511002424',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1040511002650',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1040511002256',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15861','0G-316-AA','1060511164120',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1060511163720',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1040511065154',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1060511005542',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1060511163258',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1060511163148',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1240411183902',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1060511163334',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'15872','0G-316-AA','1040511002724',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511182628',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511183120',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511164306',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511045548',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511183858',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1050511151804',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511164344',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'16513','0G-316-AA','1060511165000',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1350411014730',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1360511010528',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511002208',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1360511010316',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511002810',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511002458',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511003032',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1360511080240',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320411014730',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511010528',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511002208',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511010316',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511002810',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511002458',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511003032',0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22222','0G-316-AA','1320511080240',0,'')"); 
      
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1350411014730')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1360511010528')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1340511002208')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1360511010316')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1340511002810')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1340511002458')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1340511003032')"); 
      driver.purge("motorCatalog");
      driver.executeUpdate("delete from motorCatalog where (RFS=231635) and (RackID='22221') and (serie='1360511080240')"); 
      driver.purge("motorCatalog");
      
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1350411014730', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1360511010528', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511002208', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1360511010316', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511002810', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511002458', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1340511003032', " 
                                                                                                                                 + "0,'')"); 
      driver.executeUpdate("INSERT INTO motorCatalog(RFS,rackID,modelo,serie,error,descripcion) values (231635,'22221','0G-316-AA','1360511080240', " 
                                                                                                                                 + "0,'')");
      
      ResultSet resultSet = driver.executeQuery("select RFS, rackID, modelo,serie ,error , descripcion from motorCatalog where (RFS=231635) and " 
                                                                                                                            + "(RackID='16513')");
      assertEquals(8, resultSet.getRowCount());
      while (resultSet.next())
         assertEquals("16513", resultSet.getString(2));
      resultSet.close();
      
      assertEquals(8, (resultSet = driver.executeQuery("select RFS, rackID, modelo,serie ,error , descripcion from motorCatalog where (RFS=231635) " 
                                                                                                           + "and (RackID='22221')")).getRowCount());
      while (resultSet.next())
         assertEquals("22221", resultSet.getString(2));
      resultSet.close();

      assertEquals(8, (resultSet = driver.executeQuery("select RFS, rackID, modelo,serie ,error , descripcion from motorCatalog where (RFS=231635) " 
                                                                                                           + "and (RackID='22222')")).getRowCount());
      while (resultSet.next())
         assertEquals("22222", resultSet.getString(2));
      resultSet.close();
      
      driver.closeAll();      
   }
}
