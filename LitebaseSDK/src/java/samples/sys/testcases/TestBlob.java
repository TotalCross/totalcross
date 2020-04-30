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
import totalcross.io.*;
import totalcross.unit.*;
import totalcross.util.Random;

/** 
 * Tests the use of blobs.
 */
public class TestBlob extends TestCase
{
	/** 
	 * The main method of the test.
	 */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      byte[][] bytes = new byte[10][10];
      Random rand = new Random();
      int i = 10, 
          j, 
          ch = 'a';
      
      status("TestBlob: 1");
      
      // Drops the tables.
      if (driver.exists("blob0"))
         driver.executeUpdate("drop table blob0");
      if (driver.exists("blob1"))
         driver.executeUpdate("drop table blob1");
      if (driver.exists("blob2"))
         driver.executeUpdate("drop table blob2");
      if (driver.exists("blob3"))
         driver.executeUpdate("drop table blob3");

      status("TestBlob: 2");
      
      try // Invalid size multiplier.
      {
         driver.execute("create table blob0 (value blob(10 G))");
         fail("1");
      } catch (SQLParseException exception) {}

      try // Blob too big.
      {
         driver.execute("create table blob0 (value blob(11 M))");
         fail("2");
      } catch (SQLParseException exception) {}

      // Creates the tables.
      driver.execute("create table blob1 (value blob(100) not null)");
      driver.execute("create table blob2 (name varchar(10), picture blob(100 K))");
      driver.execute("create table blob3 (name varchar(10), id int, video blob(1 M))");

      // Checks that the tables exist.
      assertTrue(driver.exists("blob1") && driver.exists("blob2") && driver.exists("blob3"));

      try // A blob can't be in a primary key.
      {
         driver.execute("create table blob0 (value blob(100) primary key)");
         fail("3");
      } 
      catch (SQLParseException exception) {}
      assertFalse(driver.exists("blob0"));

      try // A blob can't be in a primary key.
      {
         driver.execute("create table blob0 (value blob(100), primary key(value))");
         fail("4");
      } 
      catch (SQLParseException exception) {}
      assertFalse(driver.exists("blob0"));

      try // A blob can't be in a composed primary key.
      {
         driver.execute("create table blob0 (value blob(100), age int, primary key(value, age))");
         fail("5");
      } 
      catch (SQLParseException exception) {}
      assertFalse(driver.exists("blob0"));

      try // A blob can't be in a composed primary key.
      {
         driver.execute("create table blob0 (value blob(100), age int, primary key(age, value))");
         fail("6");
      } 
      catch (SQLParseException exception) {}
      assertFalse(driver.exists("blob0"));

      try // There can't be a default value for blobs.
      {
         driver.execute("create table blob0 (value blob(100) default null, age int)");
         fail("7");
      } 
      catch (SQLParseException exception) {}
      assertFalse(driver.exists("blob0"));

      try // A blob column can't be indexed.
      {
         driver.execute("create index idx on blob1(value)");
         fail("8");
      } 
      catch (SQLParseException exception) {}

      try // A blob column can't be indexed.
      {
         driver.execute("create index idx on blob1(value, rowid)");
         fail("9");
      } 
      catch (SQLParseException exception) {}

      try // A blob can't be in a primary key.
      {
         driver.executeUpdate("alter table blob2 add primary key (picture)");
         fail("10");
      } 
      catch (SQLParseException exception) {}

      try // A blob can't be in a composed primary key.
      {
         driver.executeUpdate("alter table blob2 add primary key (name, picture)");
         fail("11");
      } 
      catch (SQLParseException exception) {}

      status("TestBlob: 3");
      
      String path = driver.getSourcePath();
      
      try // The index files can't exist because no index was created.
      {
         assertFalse(new File(path + "Test-blob1$1.idk", File.DONT_OPEN).exists());
         assertFalse(new File(path + "Test-blob1&1.idk", File.DONT_OPEN).exists());
         assertFalse(new File(path + "Test-blob2$1.idk", File.DONT_OPEN).exists());
         assertFalse(new File(path + "Test-blob2&1.idk", File.DONT_OPEN).exists());
      } 
      catch (IOException exception) {}

      try // A blob can't be in a where clause.
      {
         driver.executeQuery("select * from blob1 where value > 100");
         fail("12");
      } 
      catch (SQLParseException exception) {}

      try // A blob can't be in an order / group by clause.
      {
         driver.executeQuery("select value from blob1 order by value");
         fail("13");
      } 
      catch (SQLParseException exception) {}

      try // A blob can't be in an order / group by clause.
      {
         driver.executeQuery("select value from blob1 group by value");
         fail("14");
      } 
      catch (SQLParseException exception) {}

      try // A blob can only be inserted throught prepared statements.
      {
         driver.executeUpdate("insert into blob1 values (1)");
         fail("15");
      } 
      catch (DriverException exception) {}

      try // A blob can only be inserted throught prepared statements.
      {
         driver.executeUpdate("insert into blob1 values ('a')");
         fail("16");
      } 
      catch (DriverException exception) {}
      
      try // A blob can only be updated throught prepared statements.
      {
         driver.executeUpdate("update blob1 set value = 3 where rowid = 1");
         fail("17");
      } 
      catch (DriverException exception) {}

      try // A blob can't be in a where clause.
      {
         driver.prepareStatement("select * from blob1 where value = ?");
         fail("18");
      } 
      catch (SQLParseException exception) {}

      try // A blob can't be in a where clause.
      {
         driver.prepareStatement("delete from blob1 where value = ?");
         fail("19");
      } 
      catch (SQLParseException exception) {}

      status("TestBlob: 4");
      
      while (--i >= 0) // Creates random blobs.
      {
         j = 10;
         while (--j >= 0)
            bytes[i][j] = (byte)rand.nextInt(128);
      }

      // Creates prepared statements for each table.
      PreparedStatement preparedStmt1 = driver.prepareStatement("Insert into blob1 values (?)");
      PreparedStatement preparedStmt2 = driver.prepareStatement("insert into blob2 (picture, name) values (?, ?)");
      PreparedStatement preparedStmt3 = driver.prepareStatement("insert into blob3 (id, video, name) values (?, ?, ?)");

      driver.execute("create index idx on blob2(name)");
      
      try // The blob column of this table can't be null.
      {
         preparedStmt1.setBlob(0, null);
         preparedStmt1.executeUpdate();
         fail("20");
      } 
      catch (DriverException exception) {}

      try // The blob column of this table can't be null.
      {
         preparedStmt1.setBlob(0, bytes[0]);
         preparedStmt1.clearParameters();
         preparedStmt1.executeUpdate();
         fail("21");
      } 
      catch (DriverException exception) {}

      status("TestBlob: 5");
      
      // Populates the tables.
      i = -1;
      while (++i < 10)
      {
         preparedStmt1.setBlob(0, bytes[i]);
         assertEquals(1, preparedStmt1.executeUpdate());

         preparedStmt2.setBlob(0, bytes[i]);
         preparedStmt2.setString(1, ch + "\'");
         assertEquals(1, preparedStmt2.executeUpdate());

         preparedStmt3.setBlob(1, bytes[i]);
         preparedStmt3.setString(2, "\'" + ch);
         preparedStmt3.setInt(0, ch++);
         assertEquals(1, preparedStmt3.executeUpdate());
      }

      status("TestBlob: 6");
      
      // Queries the tables.
      ResultSet rs1 = driver.executeQuery("select * from blob1");
      ResultSet rs2 = driver.executeQuery("select * from blob2");
      ResultSet rs3 = driver.executeQuery("select * from blob3");
      assertEquals(10, rs1.getRowCount());
      assertEquals(10, rs2.getRowCount());
      assertEquals(10, rs3.getRowCount());

      // Checks that each query is correct.
      ch = 'a';
      i = -1;
      while (++i < 10)
      {
         rs1.next();
         rs2.next();
         rs3.next();
         assertEquals(bytes[i], rs1.getBlob("value"));
         assertEquals(bytes[i], rs2.getBlob("picture"));
         assertEquals(bytes[i], rs3.getBlob("video"));
         assertEquals(ch + "\'", rs2.getString("name"));
         assertEquals("\'" + ch, rs3.getString("name"));
         assertEquals(ch++, rs3.getInt("id"));
         assertNull(rs1.getString("value"));
         assertNull(rs2.getString("picture"));
         assertNull(rs3.getString("video"));
      }
      rs1.close();
      rs2.close();
      rs3.close();

      // Tests updates with blobs, writing them in the reverse order.
      preparedStmt3 = driver.prepareStatement("update blob3 set video = ? where rowid = ?");
      i = -1;
      while (++i < 10)
      {
         preparedStmt3.setBlob(0, bytes[i]);
         preparedStmt3.setInt(1, 10 - i);
         assertEquals(1, preparedStmt3.executeUpdate());
      }

      // Checks that the blobs were updated with success.    
      assertEquals(10, (rs3 = driver.executeQuery("select * from blob3")).getRowCount());
      i = 10;
      while (--i >= 0)
      {
         rs3.next();
         assertEquals(bytes[i], rs3.getBlob("video"));
      }
      rs3.close();

      // Creates bigger blobs.
      bytes = new byte[10][11];
      i = 10;
      while (--i >= 0)
      {
         j = 10;
         while (--j >= 0)
            bytes[i][j] = (byte)rand.nextInt(128);
      }

      preparedStmt1 = driver.prepareStatement("update blob1 set value = ? where rowid = ?");
      preparedStmt2 = driver.prepareStatement("update blob2 set picture = ? where rowid = ?");
      
      status("TestBlob: 7");

      // Does updates with bigger blobs.
      i = -1;
      while (++i < 10)
      {
      	preparedStmt1.setBlob(0, bytes[i]);
      	preparedStmt1.setInt(1, i + 1);
      	preparedStmt2.setBlob(0, bytes[i]);
      	preparedStmt2.setInt(1, i + 1);
         preparedStmt3.setBlob(0, bytes[i]);
         preparedStmt3.setInt(1, i + 1);
         assertEquals(1, preparedStmt1.executeUpdate());
         assertEquals(1, preparedStmt2.executeUpdate());
         assertEquals(1, preparedStmt3.executeUpdate());
      }

      // Queries the tables.
      assertEquals(10, (rs1 = driver.executeQuery("select value from blob1")).getRowCount());
      assertEquals(10, (rs2 = driver.executeQuery("select picture from blob2")).getRowCount());
      assertEquals(10, (rs3 = driver.executeQuery("select video from blob3")).getRowCount());

      status("TestBlob: 8");

      // Checks that each query is correct.
      i = -1;
      while (++i < 10)
      {
         rs1.next();
         rs2.next();
         rs3.next();
         assertEquals(bytes[i], rs1.getBlob(1));
         assertEquals(bytes[i], rs2.getBlob(1));
         assertEquals(bytes[i], rs3.getBlob(1));
      }
      rs1.close();
      rs2.close();
      rs3.close();

      status("TestBlob: 9");
      
      // Inserts a null in a blob.
      (preparedStmt2 = driver.prepareStatement("insert into blob2 (picture, name) values (?, ?)")).setBlob(0, null);
      preparedStmt2.setString(1, "Juliana");
      assertEquals(1, preparedStmt2.executeUpdate());
      assertEquals(1, (rs2 = driver.executeQuery("select * from blob2 where rowid = 11")).getRowCount());
      rs2.next();
      assertNull(rs2.getBlob(2));
      rs2.close();
      
      // Updates a blob that was null.;
      (preparedStmt2 = driver.prepareStatement("update blob2 set picture = ? where rowid = 11")).setBlob(0, bytes[0]);
      assertEquals(1, preparedStmt2.executeUpdate());
      assertEquals(1, (rs2 = driver.executeQuery("select * from blob2 where rowid = 11")).getRowCount());
      rs2.next();
      assertNotNull(rs2.getBlob(2));
      assertEquals("Juliana", rs2.getString(1));
      rs2.close();
      
      (preparedStmt2 = driver.prepareStatement("insert into blob2 (picture, name) values (?, ?)")).setNull(0);
      assertEquals(1, preparedStmt2.executeUpdate());
      assertEquals(1, (rs2 = driver.executeQuery("select * from blob2 where rowid = 12")).getRowCount());
      rs2.next();
      assertNull(rs2.getBlob(2));
      rs2.close();
      
      // Inserts a null in a blob without a prepared statement.
      assertEquals(1, driver.executeUpdate("insert into blob2 (picture, name) values (null, 0)"));
      assertEquals(1, (rs2 = driver.executeQuery("select * from blob2 where rowid = 13")).getRowCount());
      rs2.next();
      assertNull(rs2.getBlob(2));
      rs2.close();

      status("TestBlob: 10");
      
      // Updates a blob with null.
      preparedStmt3.setBlob(0, null);
      assertEquals(1, preparedStmt3.executeUpdate());
      assertEquals(1, (rs3 = driver.executeQuery("select * from blob3 where rowid = 10")).getRowCount());
      rs3.next();
      assertNull(rs3.getBlob("video"));
      rs3.close();
      preparedStmt3.setNull(0);
      assertEquals(1, preparedStmt3.executeUpdate());
      assertEquals(1, (rs3 = driver.executeQuery("select * from blob3 where rowid = 10")).getRowCount());
      rs3.next();
      assertNull(rs3.getBlob("video"));
      rs3.close();
      
      // Updates a blob with null without a prepared statement.
      assertEquals(1, driver.executeUpdate("update blob3 set video = null where rowid = 10"));
      assertEquals(1, (rs3 = driver.executeQuery("select * from blob3 where rowid = 10")).getRowCount());
      rs3.next();
      assertNull(rs3.getBlob("video"));
      rs3.close();
      
      (preparedStmt3 = driver.prepareStatement("update blob3 set video = ? where rowid = 10")).setBlob(0, bytes[9]);
      assertEquals(1, preparedStmt3.executeUpdate());
      assertEquals(1, (rs3 = driver.executeQuery("select * from blob3 where rowid = 10")).getRowCount());
      rs3.next();
      assertEquals(bytes[9], rs3.getBlob("video"));
      rs3.close();
      
      status("TestBlob: 11");
      
      // Inserts an empty string and an empty blob.
      (preparedStmt2 = driver.prepareStatement("insert into blob2 (picture, name) values (?, ?)")).setBlob(0, new byte[0]);
      preparedStmt2.setString(1, "");
      assertEquals(1, preparedStmt2.executeUpdate());
      assertEquals(1, (rs2 = driver.executeQuery("select * from blob2 where rowid = 14")).getRowCount());
      rs2.next();
      assertEquals(0, rs2.getChars("name").length);
      assertEquals(0, rs2.getBlob("picture").length);
      rs2.close();
      
      // Updates to an empty string and an empty blob.
      (preparedStmt2 = driver.prepareStatement("update blob2 set picture = ?, name = '' where rowid = 1")).setBlob(0, new byte[0]);
      assertEquals(1, preparedStmt2.executeUpdate());
      assertEquals(2, (rs2 = driver.executeQuery("select * from blob2 where name = ''")).getRowCount());
      rs2.next();
      assertEquals(0, rs2.getChars("name").length);
      assertEquals(0, rs2.getBlob("picture").length);
      rs2.next();
      assertEquals(0, rs2.getChars("name").length);
      assertEquals(0, rs2.getBlob("picture").length);
      rs2.close();
      
      status("TestBlob: 12");
      
      // juliana@202_3
      // Tests order by with a table with blobs.
      assertEquals(10, (rs3 = driver.executeQuery("select * from blob3 order by id desc")).getRowCount());
      i = 9;
      ch = 'i';
      rs3.next();
      while (--i >= 10)
      {
         rs3.next();
         assertEquals("\'" + ch, rs3.getString(1));
         assertEquals(ch--, rs3.getInt(2));
         assertEquals(bytes[i], rs3.getBlob(3));
         
      }
      rs3.close();
      
      // Tests that a blob bigger than its definition is trimmed.
      preparedStmt2.setBlob(0, new byte[102401]);
      assertEquals(1, preparedStmt2.executeUpdate());
      assertEquals(1, (rs2 = driver.executeQuery("select * from blob2 where rowid = 1")).getRowCount());
      rs2.next();
      assertEquals(0, rs2.getChars("name").length);
      assertEquals(102400, rs2.getBlob("picture").length);
      assertEquals("\t", rs2.rowToString());
      rs2.close();
      
      driver.closeAll();
   }
}