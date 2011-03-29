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

// $Id: TestInvalidArguments.java,v 1.1.2.21 2011-03-21 21:28:51 juliana Exp $

package samples.sys.testcases;

import litebase.*;
import totalcross.unit.TestCase;

/**
 * Tests Litebase methods with invalid arguments. This is necessary to test control conditions.
 */
public class TestInvalidArguments extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      testNullString(driver); // Tests null strings passed to LitebaseConnection methods.
      testEmptyString(driver); // Tests empty strings passed to LitebaseConnection methods.
      testLexicalError(driver); // Tests sql commands with lexical errors.
      testDuplicatedColumn(driver); // Tests table creations with duplicated column names.
      testLongTableName(driver); // Tests too long table names.
      testTooManyColumns(driver); // Tests the use of tables with too many columns.
      testInvalidCreateIndex(driver); // Tests a create index on a non-existing column.
      testWrongSyntax(driver); // Tests some syntaticaly wrong sql commands.
      testNonExistingTable(driver); // Tests Litebase commands that use non-existing tables.
      testNonExistingColumn(driver); // Tests Litebase SQLs that use unknown columns.
      testWrongInsertUpdate(driver); // Tests wrong inserts and updates.
      testTooLargeNumbers(driver); // Tests some large numbers greater than the declared type. 
      testWrongNumberTypesInWhere(driver); // Tests bigger strings and wrong types in the where clause.
      driver.closeAll();
      testInvalidCrid(); // Tests invalid application id sizes.
   }

   /**
    * Tests null strings passed to LitebaseConnection methods.
    * 
    * @param driver The connection with Litebase.
    */
   private void testNullString(LitebaseConnection driver)
   {
      try
      {
         driver.getRowCount(null); // juliana@201_31
         fail("1");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         driver.execute(null);
         fail("2");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.executeQuery(null);
         fail("3");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.executeUpdate(null);
         fail("4");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.exists(null);
         fail("5");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.getCurrentRowId(null);
         fail("6");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.getRowCountDeleted(null);
         fail("7");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.getRowIterator(null);
         fail("8");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.prepareStatement(null);
         fail("9");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.purge(null);
         fail("10");
      }
      catch (NullPointerException exception) {}

      try
      {
         driver.setRowInc(null, -1);
         fail("11");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         driver.isOpen(null);
         fail("12");
      }
      catch (NullPointerException exception) {}
      
      try
      {
         LitebaseConnection.dropDatabase(null, driver.getSourcePath(), -1);
         fail("13");
      }
      catch (NullPointerException exception) {}
      catch (DriverException exception) {}
      
      try
      {
         LitebaseConnection.dropDatabase("Test", null, -1);
         fail("14");
      }
      catch (NullPointerException exception) {}
   }

   /**
    * Tests empty strings passed to LitebaseConnection methods.
    * 
    * @param driver The connection with Litebase.
    */
   private void testEmptyString(LitebaseConnection driver)
   {
      assertFalse(driver.exists("")); // juliana@201_31: an empty table can't exist.
      
      try
      {
         driver.getRowCount("");
         fail("15");
      }
      catch (DriverException exception) 
      {
         exception.printStackTrace();
      }
      
      try
      {
         driver.execute("");
         fail("16");
      }
      catch (SQLParseException exception) {}

      try
      {
         driver.executeQuery("");
         fail("17");
      }
      catch (SQLParseException exception) {}

      try
      {
         driver.executeUpdate("");
         fail("18");
      }
      catch (SQLParseException exception) {}

      try
      {
         driver.getCurrentRowId("");
         fail("19");
      }
      catch (DriverException exception) {}

      try
      {
         driver.getRowCountDeleted("");
         fail("20");
      }
      catch (DriverException exception) {}

      try
      {
         driver.getRowIterator("");
         fail("21");
      }
      catch (DriverException exception) {}

      try
      {
         driver.prepareStatement("");
         fail("22");
      }
      catch (SQLParseException exception) {}

      try
      {
         driver.purge("");
         fail("23");
      }
      catch (DriverException exception) {}

      try
      {
         driver.setRowInc("", -1);
         fail("24");
      }
      catch (DriverException exception) {}

      assertFalse(driver.isOpen(""));
      
      try
      {
         LitebaseConnection.dropDatabase("", driver.getSourcePath(), -1);
         fail("25");
      }
      catch (DriverException exception) {}
      
      try
      {
         LitebaseConnection.dropDatabase("Test", "", -1);
         fail("26");
      }
      catch (DriverException exception) {}
   }

   /**
    * Tests sql commands with lexical errors.
    * 
    * @param driver The connection with Litebase.
    */
   private void testLexicalError(LitebaseConnection driver)
   {
      try // Inexistent character.
      {
         driver.execute("%");
         fail("27");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.execute("!");
         fail("28");
      }
      catch (SQLParseException exception) {}

      try // Inexistent token.
      {
         driver.execute("=!");
         fail("29");
      }
      catch (SQLParseException exception) {}

      try // Inexistent token.
      {
         driver.execute("><");
         fail("30");
      }
      catch (SQLParseException exception) {}

      try // A single quote can't be in a string.
      {
         driver.execute("'''");
         fail("31");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select *! from tab where x > 5");
         fail("32");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select !* from tab where x > 5");
         fail("33");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select * from ! tab where x > 5");
         fail("34");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select * from tab ! where x > 5");
         fail("35");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select * from tab where !x > 5");
         fail("36");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select * from tab where x >! 5");
         fail("37");
      }
      catch (SQLParseException exception) {}

      try // Inexistent character.
      {
         driver.executeQuery("select * from tab where x > 5!");
         fail("38");
      }
      catch (SQLParseException exception) {}
      
      try // Invalid identifier.
      {
         driver.executeQuery("select * from -tab where x > 5");
         fail("39");
      }
      catch (SQLParseException exception) {}
      
      try // Invalid number.
      {
         driver.executeQuery("select * from tab where x > 5i");
         fail("40");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tests table creations with duplicated column names.
    * 
    * @param driver The connection with Litebase.
    */
   private void testDuplicatedColumn(LitebaseConnection driver)
   {
      if (driver.exists("t"))
         driver.executeUpdate("drop table t");
      
      try // Repeated x.
      {
         driver.execute("create table t (x int, x char(5))");
         fail("41");
      }
      catch (SQLParseException exception) {}

      try // Repeated x.
      {
         driver.execute("create table t (x int, y int, x char(5))");
         fail("42");
      }
      catch (SQLParseException exception) {}

      try // Repeated x
      {
         driver.execute("create table t (y int, x int, x char(5))");
         fail("43");
      }
      catch (SQLParseException exception) {}
      
      try // rowid is already used as a column.
      {
         driver.execute("create table t (rowid int)");
         fail("44");
      }
      catch (SQLParseException exception) {}

      try // rowid is already used as a column.
      {
         driver.execute("create table t (x int, rowid int)");
         fail("45");
      }
      catch (SQLParseException exception) {}

      try // rowid is already used as a column.
      {
         driver.execute("create table t (rowid int, x char(5))");
         fail("46");
      }
      catch (SQLParseException exception) {}
   }

   /**
    * Tests too long table names.
    * 
    * @param driver The connection with Litebase.
    */
   private void testLongTableName(LitebaseConnection driver)
   {
      try // It is possible to create a table with the maximum table name length. 
      {
         if (driver.exists("R1234567891234567890123"))
            driver.executeUpdate("drop table R1234567891234567890123");
         driver.execute("create table R1234567891234567890123 (FIRST_NAME CHAR(30) PRIMARY KEY, CITY CHAR(30) )");
      }
      catch (SQLParseException exception)
      {
         fail("47");
      }
      
      // rnovais@570_114: Tries to create tables with a name longer than the max size (23).
      try // Tries to create one with (max size + 1) = 24 characters.
      {
         driver.execute("create table R12345678912345678901234 (FIRST_NAME CHAR(30) PRIMARY KEY, CITY CHAR(30) )");
         fail("48");
      }
      catch (SQLParseException exception) {}

      try // Tries to create one with 41 characters.
      {
         driver.execute("create table R12345678901234567890123456789012345678901234567890 (FIRST_NAME CHAR(30) PRIMARY KEY, CITY CHAR(30) )");
         fail("49");
      }
      catch (SQLParseException exception) {}
      
      try // Tries to create one with 51 characters.
      {
         driver.execute(
               "create table R123456789012345678901234567890123456789012345678901234567890 (FIRST_NAME CHAR(30) PRIMARY KEY, CITY CHAR(30) )");
         fail("50");
      }
      catch (SQLParseException exception) {}
      
      try // It is possible to use a table with max length.
      {
         driver.executeUpdate("insert into R1234567891234567890123 values('juliana', 'Rio de Janeiro')");
      }
      catch (SQLParseException exception)
      {
         fail("51");
      }
      
      // It is not possible to use tables larger then the maximum length.
      try // Tries to drop one with (max size + 1) = 24 characters.
      {
         driver.executeUpdate("drop table R12345678912345678901234");
         fail("52");
      }
      catch (DriverException exception) {}
      
      try // Tries to do an insert in a table with 41 characters.
      {
         driver.executeUpdate("insert into R12345678901234567890123456789012345678901234567890 values('juliana', 'Rio de Janeiro')");
         fail("53");
      }
      catch (DriverException exception) {}
      
      try // Tries to do a select in a table with 51 characters.
      {
         driver.executeQuery("select * from R123456789012345678901234567890123456789012345678901234567890");
         fail("54");
      }
      catch (DriverException exception) {}
   }
   
   /**
    * Tests Invalid application Id sizes.
    */
   private void testInvalidCrid()
   {
      try // Null
      {
         AllTests.getInstance(null); 
         fail("55");
      } 
      catch (NullPointerException exception) {}
      try // size 0 != 4
      {
         AllTests.getInstance(""); 
         fail("56");
      } 
      catch (DriverException exception) {}
      try // size 1 != 4
      {
         AllTests.getInstance("T"); 
         fail("57");
      } 
      catch (DriverException exception) {}
      try // size 2 != 4
      {
         AllTests.getInstance("Te"); 
         fail("58");
      } 
      catch (DriverException exception) {}
      try // size 3 != 4
      {
         AllTests.getInstance("Tes"); 
         fail("59");
      } 
      catch (DriverException exception) {}
      try // size 5 != 4
      {
         AllTests.getInstance("Tests"); 
         fail("60");
      } 
      catch (DriverException exception) {}
      try // size 6 != 4
      {
         AllTests.getInstance("2Tests"); 
         fail("61");
      } 
      catch (DriverException exception) {}
   }
   
   /**
    * Tests the use of tables with too many columns.
    * 
    * @param driver The connection with Litebase.
    */
   private void testTooManyColumns(LitebaseConnection driver)
   {
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      
      try // Tries to create a table with too many columns.
      {
         driver.execute("create table person (q0 int, q1 int, q2 int, q3 int, q4 int, w0 int, w1 int, w2 int, w3 int, w4 int," 
   + "e0 int, e1 int, e2 int, e3 int, e4 int, r0 int, r1 int, r2 int, r3 int, r4 int, t0 int, t1 int, t2 int, t3 int, t4 int," 
   + "y0 int, y1 int, y2 int, y3 int, y4 int, u0 int, u1 int, u2 int, u3 int, u4 int, i0 int, i1 int, i2 int, i3 int, i4 int,"
   + "o0 int, o1 int, o2 int, o3 int, o4 int, p0 int, p1 int, p2 int, p3 int, p4 int, a0 int, a1 int, a2 int, a3 int, a4 int,"
   + "s0 int, s1 int, s2 int, s3 int, s4 int, d0 int, d1 int, d2 int, d3 int, d4 int, f0 int, f1 int, f2 int, f3 int, f4 int,"
   + "g0 int, g1 int, g2 int, g3 int, g4 int, h0 int, h1 int, h2 int, h3 int, h4 int, j0 int, j1 int, j2 int, j3 int, j4 int," 
   + "k0 int, k1 int, k2 int, k3 int, k4 int, l0 int, l1 int, l2 int, l3 int, l4 int, z0 int, z1 int, z2 int, z3 int, z4 int," 
   + "x0 int, x1 int, x2 int, x3 int, x4 int, c0 int, c1 int, c2 int, c3 int, c4 int, v0 int, v1 int, v2 int, v3 int, v4 int,"
   + "b0 int, b1 int, b2 int, b3 int, b4 int, n0 int, n1 int, n2 int, n3 int, n4 int, m0 int, m1 int, m2 int, m3 int, m4 int)");
      
         fail("62");
      } 
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      try // Tries to create a table with too many columns in the composed primary key.
      {
         driver.execute("create table person (x int, primary key(q0, q1, q2, q3, q4, w0, w1, w2, w3, w4, e0, e1, e2, e3, e4,"
  + "r0, r1, r2, r3, r4, t0, t1, t2, t3, t4, y0, y1, y2, y3, y4, u0, u1, u2, u3, u4, i0, i1, i2, i3, i4, o0, o1, o2, o3, o4,"
  + "p0, p1, p2, p3, p4, a0, a1, a2, a3, a4, s0, s1, s2, s3, s4, d0, d1, d2, d3, d4, f0, f1, f2, f3, f4, g0, g1, g2, g3, g4,"
  + "h0, h1, h2, h3, h4, j0, j1, j2, j3, j4, k0, k1, k2, k3, k4, l0, l1, l2, l3, l4, z0, z1, z2, z3, z4, x0, x1, x2, x3, x4,"
  + "c0, c1, c2, c3, c4, v0, v1, v2, v3, v4, b0, b1, b2, b3, b4, n0, n1, n2, n3, n4, m0, m1, m2, m3, m4))");
      
         fail("63");
      } 
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      // Too many composed indices.
      driver.execute("create table person (q0 int, q1 int, q2 int, q3 int, q4 int, w0 int, w1 int, w2 int, w3 int, w4 int," 
            + "e0 int, e1 int, e2 int, e3 int, e4 int, r0 int, r1 int, r2 int, r3 int, r4 int, t0 int, t1 int, t2 int, t3 int, t4 int," 
            + "y0 int, y1 int, y2 int, y3 int, y4 int, u0 int, u1 int, u2 int, u3 int, u4 int, i0 int, i1 int, i2 int, i3 int, i4 int,"
            + "o0 int, o1 int, o2 int, o3 int, o4 int, p0 int, p1 int, p2 int, p3 int, p4 int, a0 int, a1 int, a2 int, a3 int, a4 int,"
            + "s0 int, s1 int, s2 int, s3 int, s4 int, d0 int, d1 int, d2 int, d3 int, d4 int, f0 int, f1 int, f2 int, f3 int, f4 int,"
            + "g0 int, g1 int, g2 int, g3 int, g4 int, h0 int, h1 int, h2 int, h3 int, h4 int, j0 int, j1 int, j2 int, j3 int, j4 int," 
            + "k0 int, k1 int, k2 int, k3 int, k4 int, l0 int, l1 int, l2 int, l3 int, l4 int, z0 int, z1 int, z2 int, z3 int, z4 int," 
            + "x0 int, x1 int, x2 int, x3 int, x4 int, c0 int, c1 int, c2 int, c3 int, c4 int, v0 int, v1 int, v2 int, v3 int, v4 int,"
            + "b0 int, b1 int, b2 int, b3 int, b4 int, n0 int, n1 int, n2 int, n3 int, n4 int, m0 int, m1 int, m2 int)");
      
      driver.execute("create index idx on person (rowid, q0)");
      driver.execute("create index idx on person (rowid, q1)");
      driver.execute("create index idx on person (rowid, q2)");
      driver.execute("create index idx on person (rowid, q3)");
      driver.execute("create index idx on person (rowid, q4)");
      driver.execute("create index idx on person (rowid, w0)");
      driver.execute("create index idx on person (rowid, w1)");
      driver.execute("create index idx on person (rowid, w2)");
      driver.execute("create index idx on person (rowid, w3)");
      driver.execute("create index idx on person (rowid, w4)");
      driver.execute("create index idx on person (rowid, e0)");
      driver.execute("create index idx on person (rowid, e1)");
      driver.execute("create index idx on person (rowid, e2)");
      driver.execute("create index idx on person (rowid, e3)");
      driver.execute("create index idx on person (rowid, e4)");
      driver.execute("create index idx on person (rowid, r0)");
      driver.execute("create index idx on person (rowid, r1)");
      driver.execute("create index idx on person (rowid, r2)");
      driver.execute("create index idx on person (rowid, r3)");
      driver.execute("create index idx on person (rowid, r4)");
      driver.execute("create index idx on person (rowid, t0)");
      driver.execute("create index idx on person (rowid, t1)");
      driver.execute("create index idx on person (rowid, t2)");
      driver.execute("create index idx on person (rowid, t3)");
      driver.execute("create index idx on person (rowid, t4)");
      driver.execute("create index idx on person (rowid, y0)");
      driver.execute("create index idx on person (rowid, y1)");
      driver.execute("create index idx on person (rowid, y2)");
      driver.execute("create index idx on person (rowid, y3)");
      driver.execute("create index idx on person (rowid, y4)");
      driver.execute("create index idx on person (rowid, u0)");
      driver.execute("create index idx on person (rowid, u1)");
      
      try // The 33º composed index creation will fail.
      {
         driver.execute("create index idx on person (rowid, u2)");
         fail("64");
      }
      catch (DriverException exception) {}
      
      // Too many columns to be inserted.
      try 
      {
         driver.prepareStatement("insert into person values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " 
                                          + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " 
                                          + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " 
                                          + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         fail("65");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      try 
      {
         driver.executeUpdate("insert into person values (0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " 
                                          + "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " 
                                          + "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " 
                                          + "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
         fail("66");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      // Too many columns to be updated.
      try 
      {
         driver.prepareStatement("update person set q0 = ?, q1 = ?, q2 = ?, q3 = ?, q4 = ?, w0 = ?, w1 = ?, w2 = ?, w3 = ?, w4 = ?, e0 = ?, e1 = ?, " 
         + "e2 = ?, e3 = ?, e4 = ?, r0 = ?, r1 = ?, r2 = ?, r3 = ?, r4 = ?, t0 = ?, t1 = ?, t2 = ?, t3 = ?, t4 = ?, y0 = ?, y1 = ?, y2 = ?, y3 = ?, " 
         + "y4 = ?, u0 = ?, u1 = ?, u2 = ?, u3 = ?, u4 = ?, i0 = ?, i1 = ?, i2 = ?, i3 = ?, i4 = ?, o0 = ?, o1 = ?, o2 = ?, o3 = ?, o4 = ?, p0 = ?, " 
         + "p1 = ?, p2 = ?, p3 = ?, p4 = ?, a0 = ?, a1 = ?, a2 = ?, a3 = ?, a4 = ?, s0 = ?, s1 = ?, s2 = ?, s3 = ?, s4 = ?, d0 = ?, d1 = ?, d2 = ?, " 
         + "d3 = ?, d4 = ?, f0 = ?, f1 = ?, f2 = ?, f3 = ?, f4 = ?, g0 = ?, g1 = ?, g2 = ?, g3 = ?, g4 = ?, h0 = ?, h1 = ?, h2 = ?, h3 = ?, h4 = ?, " 
         + "j0 = ?, j1 = ?, j2 = ?, j3 = ?, j4 = ?, k0 = ?, k1 = ?, k2 = ?, k3 = ?, k4 = ?, l0 = ?, l1 = ?, l2 = ?, l3 = ?, l4 = ?, z0 = ?, z1 = ?, " 
         + "z2 = ?, z3 = ?, z4 = ?, x0 = ?, x1 = ?, x2 = ?, x3 = ?, x4 = ?, c0 = ?, c1 = ?, c2 = ?, c3 = ?, c4 = ?, v0 = ?, v1 = ?, v2 = ?, v3 = ?, " 
         + "v4 = ?, b0 = ?, b1 = ?, b2 = ?, b3 = ?, b4 = ?, n0 = ?, n1 = ?, n2 = ?, n3 = ?, n4 = ?, m0 = ?, m1 = ?, m2 = ?, m3 = ?");
         fail("67");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      try 
      {
         driver.executeUpdate("update person set q0 = 0, q1 = 0, q2 = 0, q3 = 0, q4 = 0, w0 = 0, w1 = 0, w2 = 0, w3 = 0, w4 = 0, e0 = 0, e1 = 0, " 
         + "e2 = 0, e3 = 0, e4 = 0, r0 = 0, r1 = 0, r2 = 0, r3 = 0, r4 = 0, t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0, y0 = 0, y1 = 0, y2 = 0, y3 = 0, " 
         + "y4 = 0, u0 = 0, u1 = 0, u2 = 0, u3 = 0, u4 = 0, i0 = 0, i1 = 0, i2 = 0, i3 = 0, i4 = 0, o0 = 0, o1 = 0, o2 = 0, o3 = 0, o4 = 0, p0 = 0, " 
         + "p1 = 0, p2 = 0, p3 = 0, p4 = 0, a0 = 0, a1 = 0, a2 = 0, a3 = 0, a4 = 0, s0 = 0, s1 = 0, s2 = 0, s3 = 0, s4 = 0, d0 = 0, d1 = 0, d2 = 0, " 
         + "d3 = 0, d4 = 0, f0 = 0, f1 = 0, f2 = 0, f3 = 0, f4 = 0, g0 = 0, g1 = 0, g2 = 0, g3 = 0, g4 = 0, h0 = 0, h1 = 0, h2 = 0, h3 = 0, h4 = 0, " 
         + "j0 = 0, j1 = 0, j2 = 0, j3 = 0, j4 = 0, k0 = 0, k1 = 0, k2 = 0, k3 = 0, k4 = 0, l0 = 0, l1 = 0, l2 = 0, l3 = 0, l4 = 0, z0 = 0, z1 = 0, " 
         + "z2 = 0, z3 = 0, z4 = 0, x0 = 0, x1 = 0, x2 = 0, x3 = 0, x4 = 0, c0 = 0, c1 = 0, c2 = 0, c3 = 0, c4 = 0, v0 = 0, v1 = 0, v2 = 0, v3 = 0, " 
         + "v4 = 0, b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, n0 = 0, n1 = 0, n2 = 0, n3 = 0, n4 = 0, m0 = 0, m1 = 0, m2 = 0, m3 = 0");
         fail("68");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      // Too many columns to be selected.
      try 
      {
         driver.prepareStatement("select * from person where q0 = ? and q1 = ? and q2 = ? and q3 = ? and q4 = ? and w0 = ? and w1 = ? and w2 = ? " 
+ "and w3 = ? and w4 = ? and e0 = ? and e1 = ? and e2 = ? and e3 = ? and e4 = ? and r0 = ? and r1 = ? and r2 = ? and r3 = ? and r4 = ? and t0 = ? " 
+ "and t1 = ? and t2 = ? and t3 = ? and t4 = ? and y0 = ? and y1 = ? and y2 = ? and y3 = ? and y4 = ? and u0 = ? and u1 = ? and u2 = ? and u3 = ? " 
+ "and u4 = ? and i0 = ? and i1 = ? and i2 = ? and i3 = ? and i4 = ? and o0 = ? and o1 = ? and o2 = ? and o3 = ? and o4 = ? and p0 = ? and p1 = ? " 
+ "and p2 = ? and p3 = ? and p4 = ? and a0 = ? and a1 = ? and a2 = ? and a3 = ? and a4 = ? and s0 = ? and s1 = ? and s2 = ? and s3 = ? and s4 = ? " 
+ "and d0 = ? and d1 = ? and d2 = ? and d3 = ? and d4 = ? and f0 = ? and f1 = ? and f2 = ? and f3 = ? and f4 = ? and g0 = ? and g1 = ? and g2 = ? " 
+ "and g3 = ? and g4 = ? and h0 = ? and h1 = ? and h2 = ? and h3 = ? and h4 = ? and j0 = ? and j1 = ? and j2 = ? and j3 = ? and j4 = ? and k0 = ? " 
+ "and k1 = ? and k2 = ? and k3 = ? and k4 = ? and l0 = ? and l1 = ? and l2 = ? and l3 = ? and l4 = ? and z0 = ? and z1 = ? and z2 = ? and z3 = ? " 
+ "and z4 = ? and x0 = ? and x1 = ? and x2 = ? and x3 = ? and x4 = ? and c0 = ? and c1 = ? and c2 = ? and c3 = ? and c4 = ? and v0 = ? and v1 = ? " 
+ "and v2 = ? and v3 = ? and v4 = ? and b0 = ? and b1 = ? and b2 = ? and b3 = ? and b4 = ? and n0 = ? and n1 = ? and n2 = ? and n3 = ? and n4 = ? " 
+ "and m0 = ? and m1 = ? and m2 = ? and rowid = ?");
         fail("69");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from person where q0 = 0 and q1 = 0 and q2 = 0 and q3 = 0 and q4 = 0 and w0 = 0 and w1 = 0 and w2 = 0 " 
+ "and w3 = 0 and w4 = 0 and e0 = 0 and e1 = 0 and e2 = 0 and e3 = 0 and e4 = 0 and r0 = 0 and r1 = 0 and r2 = 0 and r3 = 0 and r4 = 0 and t0 = 0 " 
+ "and t1 = 0 and t2 = 0 and t3 = 0 and t4 = 0 and y0 = 0 and y1 = 0 and y2 = 0 and y3 = 0 and y4 = 0 and u0 = 0 and u1 = 0 and u2 = 0 and u3 = 0 " 
+ "and u4 = 0 and i0 = 0 and i1 = 0 and i2 = 0 and i3 = 0 and i4 = 0 and o0 = 0 and o1 = 0 and o2 = 0 and o3 = 0 and o4 = 0 and p0 = 0 and p1 = 0 " 
+ "and p2 = 0 and p3 = 0 and p4 = 0 and a0 = 0 and a1 = 0 and a2 = 0 and a3 = 0 and a4 = 0 and s0 = 0 and s1 = 0 and s2 = 0 and s3 = 0 and s4 = 0 " 
+ "and d0 = 0 and d1 = 0 and d2 = 0 and d3 = 0 and d4 = 0 and f0 = 0 and f1 = 0 and f2 = 0 and f3 = 0 and f4 = 0 and g0 = 0 and g1 = 0 and g2 = 0 " 
+ "and g3 = 0 and g4 = 0 and h0 = 0 and h1 = 0 and h2 = 0 and h3 = 0 and h4 = 0 and j0 = 0 and j1 = 0 and j2 = 0 and j3 = 0 and j4 = 0 and k0 = 0 " 
+ "and k1 = 0 and k2 = 0 and k3 = 0 and k4 = 0 and l0 = 0 and l1 = 0 and l2 = 0 and l3 = 0 and l4 = 0 and z0 = 0 and z1 = 0 and z2 = 0 and z3 = 0 " 
+ "and z4 = 0 and x0 = 0 and x1 = 0 and x2 = 0 and x3 = 0 and x4 = 0 and c0 = 0 and c1 = 0 and c2 = 0 and c3 = 0 and c4 = 0 and v0 = 0 and v1 = 0 " 
+ "and v2 = 0 and v3 = 0 and v4 = 0 and b0 = 0 and b1 = 0 and b2 = 0 and b3 = 0 and b4 = 0 and n0 = 0 and n1 = 0 and n2 = 0 and n3 = 0 and n4 = 0 " 
+ "and m0 = 0 and m1 = 0 and m2 = 0 and rowid = 0");
         fail("70");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tests a create index on a non-existing column.
    * 
    * @param driver The connection with Litebase.
    */
   private void testInvalidCreateIndex(LitebaseConnection driver)
   {
      if (driver.exists("person")) 
         driver.executeUpdate("drop table person");
      driver.execute("create table person(id int primary key, lastname char(20), firstname char(20), age int, dept int)");
      try 
      {
         driver.execute("create index nocolumnindex on person(nocolumn)");
         fail("71");
      } 
      catch (DriverException exception) {}
   }
   
   /**
    * Tests some syntaticaly wrong sql commands.
    * 
    * @param driver The connection with Litebase.
    */
   private void testWrongSyntax(LitebaseConnection driver)
   {
      try
      {
         driver.execute("create table invalidsql XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
         fail("72");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create index invalidindex");
         fail("73");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table employee");
         fail("74");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("drop tablexxxxxx");
         fail("75");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("drop index xxxxxxxx");
         fail("76");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into employee () values()");
         fail("77");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("delete data from employee where id = 99");
         fail("78");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update lastname='updated' where id = 99");
         fail("79");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update lastname='updated' where id = 99");
         fail("80");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select table employee");
         fail("81");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.prepareStatement("select employee where id = ?");
         fail("82");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tests sql commands that use non-existing tables.
    * 
    * @param driver The connection with Litebase.
    */
   private void testNonExistingTable(LitebaseConnection driver)
   {
      try
      {
         driver.execute("create index deptindex on invalidtable(dept)");
         fail("83");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("alter table notable drop primary key");
         fail("84");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop table company");
         fail("85");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop index deptindex on notable");
         fail("86");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("insert into company(id,name) values (1,'name')");
         fail("87");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("delete from company");
         fail("88");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("update company set name='updated'");
         fail("89");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeQuery("select * from company");
         fail("90");
      }
      catch (DriverException exception) {}
      try
      {
         driver.purge("company");
         fail("91");
      }
      catch (DriverException exception) {}
      try
      {
         driver.convert("company");
         fail("92");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getRowCount("company");
         fail("93");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getCurrentRowId("company");
         fail("94");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getRowCountDeleted("company");
         fail("95");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getRowIterator("company");
         fail("96");
      }
      catch (DriverException exception) {}
      try
      {
         driver.recoverTable("company");
         fail("97");
      }
      catch (DriverException exception) {}
      try
      {
         driver.setRowInc("company", -1);
         fail("98");
      }
      catch (DriverException exception) {}
   }
   
   /**
    * Tests Litebase SQLs that use unknown columns.
    * 
    * @param driver The connection with Litebase.
    */
   private void testNonExistingColumn(LitebaseConnection driver)
   {
      if (driver.exists("employee"))
         driver.executeUpdate("drop table employee");
      driver.execute("create table employee(id int, lastname char(20), firstname char(20), age int, dept int)");
      
      try
      {
         driver.execute("create index nocolumnindex on employee(nocolumn)");
         fail("99");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("alter table employee add primary key(nocolumn) ");
         fail("100");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("alter table employee rename nocolumn to newcolumn");
         fail("101");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop index firstname on employee");
         fail("102");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("insert into employee (id,lastname,firstname,years,dept,nocolumn) values(1,'lname1','fname1',1,1,'nocolumn')");
         fail("103");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("delete from employee where gender = 'M'");
         fail("104");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update employee set middlename='updated' where id = 99");
         fail("105");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select middlename from employee");
         fail("106");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tests some large numbers greater than the declared type. 
    * 
    * @param driver The connection with Litebase.
    */
   private void testTooLargeNumbers(LitebaseConnection driver)
   {
      if (driver.exists("bignumbers"))
         driver.executeUpdate("drop table bignumbers");
      driver.execute("create table bignumbers (s short, i int, l long)");
      
      // Invalid short values.
      try
      {
         driver.executeQuery("select * from bignumbers where s = 32768");
         fail("107");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where s = -32769");
         fail("108");
      }
      catch (SQLParseException exception) {}
      
      // Invalid int values.
      try
      {
         driver.executeQuery("select * from bignumbers where i = 2147483648");
         fail("109");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where i = -2147483649");
         fail("110");
      }
      catch (SQLParseException exception) {}
      
      // Invalid long values.
      try
      {
         driver.executeQuery("select * from bignumbers where l = 9223372036854775808");
         fail("111");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where l = -9223372036854775809");
         fail("112");
      }
      catch (SQLParseException exception) {}
      
      // Valid numeric values.
      driver.executeQuery("select * from bignumbers where s = +32767").close();
      driver.executeQuery("select * from bignumbers where s = -32768").close();
      driver.executeQuery("select * from bignumbers where i = +2147483647").close();
      driver.executeQuery("select * from bignumbers where i = -2147483648").close();
      driver.executeQuery("select * from bignumbers where l = +9223372036854775807").close();
      driver.executeQuery("select * from bignumbers where l = -9223372036854775808").close();
   
      // Invalid short values.
      try
      {
         driver.executeUpdate("insert into bignumbers (s) values (32768)");
         fail("113");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into bignumbers (s) values (-32769)");
         fail("114");
      }
      catch (SQLParseException exception) {}
      
      // Invalid int values.
      try
      {
         driver.executeUpdate("insert into bignumbers (i) values (2147483648)");
         fail("115");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into bignumbers (i) values (-2147483649)");
         fail("116");
      }
      catch (SQLParseException exception) {}
      
      // Invalid long values.
      try
      {
         driver.executeUpdate("insert into bignumbers (l) values (9223372036854775808)");
         fail("117");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into bignumbers (l) values (-9223372036854775809)");
         fail("118");
      }
      catch (SQLParseException exception) {}
      
      // Valid numeric values.
      driver.executeUpdate("insert into bignumbers values (+32767, +2147483647, +9223372036854775807)");
      driver.executeUpdate("insert into bignumbers values (-32768, -2147483648, -9223372036854775808)");
      
      // Tests valid numeric values insertion.
      ResultSet resultSet = driver.executeQuery("select * from bignumbers");
      assertTrue(resultSet.next());
      assertEquals(32767, resultSet.getShort("s"));
      assertEquals(2147483647, resultSet.getInt("i"));
      assertEquals(9223372036854775807L, resultSet.getLong("l"));
      assertTrue(resultSet.next());
      assertEquals(-32768, resultSet.getShort("s"));
      assertEquals(-2147483648, resultSet.getInt("i"));
      assertEquals(-9223372036854775808L, resultSet.getLong("l"));
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   /**
    * Tests bigger strings and wrong types in the where clause.
    * 
    * @param driver The connection with Litebase.
    */
   private void testWrongNumberTypesInWhere(LitebaseConnection driver)
   {
      // Invalid numeric types.
      PreparedStatement prepStmt = driver.prepareStatement("select * from bignumbers where s = ?");
      try
      {
         prepStmt.setInt(0, 0);
         fail("119");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where s = 0.0");
         fail("120");
      }
      catch (SQLParseException exception) {}
      
      // Invalid number.
      try
      {
         driver.executeQuery("select * from bignumbers where s = 'juliana'");
         fail("121");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tests wrong inserts and updates.
    * 
    * @param driver The connection with Litebase.
    */
   private void testWrongInsertUpdate(LitebaseConnection driver)
   {
      try
      {
         driver.executeUpdate("insert into employee (id,lastname) values (101,'lname101','fname101',101,1)");
         fail("122");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into employee (id,lastname) values ('lname101', 101)");
         fail("123");
      }
      catch (SQLParseException exception) {}
      
      try
      {
         driver.executeUpdate("update employee set middlename='updated' where id = 99");
         fail("124");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update employee set id = 'lname101', lastname = 101)");
         fail("125");
      }
      catch (SQLParseException exception) {}
   }
}
