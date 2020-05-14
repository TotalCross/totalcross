// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.sys.testcases;

import totalcross.sys.*;
import totalcross.unit.TestCase;
import litebase.*;

/**
 * Does tests with tables which use or not cryptography.
 */
public class TestCryptoTables extends TestCase
{
   /**
    * Main test method.
    */
   public void testRun()
   {
      String tempPath = Convert.appendPath(Settings.appPath, "temp/"); // The path used.
      
      // Creates a table within an ascii connection.
      LitebaseConnection conn = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      
      // The table does not exist yet.
      assertFalse(conn.isOpen("person"));
      
      if (conn.exists("person"))
         conn.executeUpdate("drop table person");
      conn.execute("CREATE table PERSON (NAME CHAR(10))");
      assertTrue(conn.isOpen("person"));
      conn.closeAll();
      
      // Trying to use this table in an unicode connection must fail.
      conn = LitebaseConnection.getInstance("Test", tempPath);
      try
      {
         conn.executeUpdate("insert into person values ('Juliana')");
         fail("1");
      }
      catch (DriverException exception) {}
      conn.executeUpdate("drop table person");
      conn.closeAll();
      
      // Creates a table within an unicode connection.
      (conn = LitebaseConnection.getInstance("Test", "path = " + tempPath)).execute("create table person (name char(10))");
      conn.closeAll();
      
      // Trying to use this table in an ascii connection must fail.
      conn = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      try
      {
         conn.execute("create index idx on person(name)");
         fail("2");
      }
      catch (DriverException exception) {}
      conn.executeUpdate("drop table person");
      conn.closeAll();
      
      // Creates two connections which only differ in the string types. They must be different. 
      LitebaseConnection connAux = LitebaseConnection.getInstance("Test", "path = " + tempPath + " ; crypto");
      assertNotEquals(conn = LitebaseConnection.getInstance("Test", "path = " + tempPath), connAux); 
      
      // Since the paths and the application id are the same, the second connection can't create the table.
      conn.execute("create table person (name char(10))");
      
      conn.closeAll();
      try
      {
         connAux.execute("create table person (name char(10))");
         fail("3");
      }
      catch (AlreadyCreatedException exception) {}
      
      try // Trying to use an unicode table in an ascii connection must fail.
      {
         connAux.executeQuery("select * from person");
         fail("4");
      }
      catch (DriverException exception) {}
      connAux.closeAll();
      
      // Trying to use an unicode table in an ascii connection must fail, but it can't fail in an unicode connection.
      conn = LitebaseConnection.getInstance("Test", "path = " + tempPath);
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      try 
      {
         connAux.executeQuery("select * from person");
         fail("5");
      }
      catch (DriverException exception) {}
      assertNotNull(conn.executeQuery("select * from person"));
      conn.executeUpdate("drop table person");
      conn.closeAll();
      connAux.closeAll();
      
      // Creates and populates an ascii and an unicode table.
      conn = LitebaseConnection.getInstance("Test", "path = " + tempPath);
      if (conn.exists("person1"))
         conn.executeUpdate("drop table person1");
      conn.execute("create table person1 (name char(10) primary key)");
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      if (connAux.exists("person2"))
         connAux.executeUpdate("drop table person2");
      connAux.execute("create table person2 (name char(10) primary key)");
      
      int i = -1;
      while (++i < 100)
      {
         conn.executeUpdate("insert into person1 values ('Name" + i + "')");
         connAux.executeUpdate("insert into person2 values ('Name" + i + "')");
      }
      
      // It is not possible to use a table within a connection with a different kind of string.
      conn.executeUpdate("insert into person1 values ('Name100')");
      conn.closeAll();
      try
      {
         connAux.executeUpdate("insert into person1 values ('Name100')");
         fail("6");
      }
      catch (DriverException exception) {}
      connAux.executeUpdate("insert into person2 values ('Name100')");
      connAux.closeAll();
      conn = LitebaseConnection.getInstance("Test", "path = " + tempPath);
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      try
      {
         
         conn.executeUpdate("insert into person2 values ('Name100')");
         fail("7");
      }
      catch (DriverException exception) {}
      
      // Asserts that the elements were saved correctly.
      ResultSet rs1 = conn.executeQuery("select rowid, name from person1"),
                rs2 = connAux.executeQuery("select rowid, name from person2");
      assertEquals(101, rs1.getRowCount());
      assertEquals(101, rs2.getRowCount());
      i = -1;
      while (++i <= 100)
      {
         rs1.next();
         rs2.next();
         assertEquals("Name" + i, new String(rs1.getChars(2)));
         assertEquals("Name" + i, new String(rs2.getChars("name")));
      }
      rs1.close();
      rs2.close();
      
      // juliana@214_5 juliana@214_6: the strings must be trimmed.
      connAux.executeUpdate("drop table person2");
      connAux.execute("create table person2 (name char(1) primary key)");
      connAux.executeUpdate("insert into person2 values('')");
      connAux.executeUpdate("insert into person2 values('\\'')");
      
      try // 'A is to be stored as ' since the field size is 1. 
      {
         connAux.executeUpdate("insert into person2 values('\\'A')");
         fail("8");
      } 
      catch (PrimaryKeyViolationException exception) {}
      
      // Tests if the data was inserted correctly.
      assertTrue((rs2 = connAux.executeQuery("select * from person2")).next());
      assertEquals("", rs2.getString("name"));
      assertTrue(rs2.next());
      assertEquals("'", rs2.getString("name"));
      assertFalse(rs2.next());
      rs2.close();
      assertTrue((rs2 = connAux.executeQuery("select * from person2 where name like '\\''")).next());
      assertEquals("'", rs2.getString("name"));
      assertFalse(rs2.next());
      rs2.close();
      
      connAux.executeUpdate("drop table person2");
      connAux.execute("create table person2 (name char(2) primary key)");
      connAux.executeUpdate("insert into person2 values('')");
      connAux.executeUpdate("insert into person2 values('\\'')");
      connAux.executeUpdate("insert into person2 values('\\'A')");
      
      try // 'AA is to be stored as 'A since the field size is 2. 
      {
         connAux.executeUpdate("insert into person2 values('\\'AA')");
         fail("9");
      } 
      catch (PrimaryKeyViolationException exception) {}

      // Tests if the data was inserted correctly.
      assertTrue((rs2 = connAux.executeQuery("select * from person2")).next());
      assertEquals("", rs2.getString("name"));
      assertTrue(rs2.next());
      assertEquals("'", rs2.getString("name"));
      assertTrue(rs2.next());
      assertEquals("'A", rs2.getString("name"));
      assertFalse(rs2.next());
      rs2.close();
      assertTrue((rs2 = connAux.executeQuery("select * from person2 where name like '\\'%'")).next());
      assertEquals("'", rs2.getString("name"));
      assertTrue(rs2.next());
      assertEquals("'A", rs2.getString("name"));
      assertFalse(rs2.next());
      rs2.close();
      
      // The tables are opened by its current connections.
      assertTrue(conn.isOpen("PERSON1"));
      assertTrue(connAux.isOpen("PERSON2"));
      
      // The tables are opened by the other connection.
      assertFalse(connAux.isOpen("PERSON1"));
      assertFalse(conn.isOpen("PERSON2"));
      
      conn.closeAll();
      connAux.closeAll();
      
      conn = LitebaseConnection.getInstance("Test", "path = " + tempPath);
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      
      // The tables are still closed.
      assertFalse(conn.isOpen("PERSON1"));
      assertFalse(connAux.isOpen("PERSON2"));
      assertFalse(connAux.isOpen("PERSON1"));
      assertFalse(conn.isOpen("PERSON2"));
      
      conn.closeAll();
      connAux.closeAll();
      
      // Not all the tables have the same cryptography mode.
      try
      {
         LitebaseConnection.encryptTables("Test", tempPath, 1);
         fail("10");
      }
      catch (DriverException exception) {}
      try
      {
         LitebaseConnection.decryptTables("Test", tempPath, 1);
         fail("11");
      }
      catch (DriverException exception) {}
     
      // Drops the table with cryptography.
      (connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath)).executeUpdate("drop table person2");
      connAux.closeAll();
      
      // Encrypts the table and tests it.
      LitebaseConnection.encryptTables("Test", tempPath, 1);      
      conn = LitebaseConnection.getInstance("Test", tempPath);
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      try // The table can't be opened in a non-encrypted connection.
      {
         conn.executeQuery("select * from person1");
         fail("12");
      }
      catch (DriverException exception) {}
      conn.closeAll();
      connAux.executeQuery("select * from person1").close();            
      connAux.closeAll();
      
      // Decrypts the table and tests it.
      LitebaseConnection.decryptTables("Test", tempPath, 1);
      conn = LitebaseConnection.getInstance("Test", tempPath);
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      try // The table can't be opened in an encrypted connection.
      {
         connAux.executeQuery("select * from person1");
         fail("12");
      }
      catch (DriverException exception) {}
      connAux.closeAll();
      connAux = LitebaseConnection.getInstance("Test", "crypto; path = " + tempPath);
      conn.executeQuery("select * from person1").close();
      conn.closeAll();       
   }

}
