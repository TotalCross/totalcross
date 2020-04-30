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
import totalcross.sys.Convert;
import totalcross.sys.Settings;
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
      testInvalidInc(driver); // Tests invalid increments.
      testInvalidRowidAlter(driver); // Tries to alter the rowid.
      testTooBigSelect(driver); // Tries to do a select * with too many fields.      
      driver.closeAll();
      testLongPath(); // Tests too long paths.
      testInvalidCrid(); // Tests invalid application id sizes.
      testInvalidParameter(); // Tests invalid connection parameter. 
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
    * Tests invalid increments.
    * 
    * @param driver The connection with Litebase.
    */
   private void testInvalidInc(LitebaseConnection driver)
   {
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      driver.execute("create table person (id int)");
      try
      {
         driver.setRowInc("person", 0);
         fail("62");
      }
      catch (IllegalArgumentException exception) {}
      try
      {
         driver.setRowInc("person", -2);
         fail("63");
      }
      catch (IllegalArgumentException exception) {}
   }
   
   /**
    * Tests the use of tables with too many columns.
    * 
    * @param driver The connection with Litebase.
    */
   private void testTooManyColumns(LitebaseConnection driver)
   {
      StringBuffer sBuffer = new StringBuffer(3916);
      
      if (driver.exists("person"))
         driver.executeUpdate("drop table person");
      
      sBuffer.append("create table person (a0 int");
      int i = 0;
      while (++i < 255)
         sBuffer.append(", a").append(i).append(" int");
      sBuffer.append(")");
      
      try // Tries to create a table with too many columns.
      {
         driver.execute(sBuffer.toString());      
         fail("64");
      } 
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("create table person (");
      i = -1;
      while (++i < 254)
         sBuffer.append("a").append(i).append(" int, ");
      sBuffer.append("primary key(rowid");
      i = -1;
      while (++i < 254)
         sBuffer.append(", a").append(i);
      sBuffer.append("))");
      
      try // Tries to create a table with too many columns in the composed primary key.
      {
         driver.execute(sBuffer.toString());      
         fail("65");
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
      
      try // The 33� composed index creation will fail.
      {
         driver.execute("create index idx on person (rowid, u2)");
         fail("66");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (DriverException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("insert into person values (?");
      i = 0;
      while (++i < 255)
         sBuffer.append(", ?");
      sBuffer.append(')');
      
      // Too many columns to be inserted.
      try 
      {
         driver.prepareStatement(sBuffer.toString());
         fail("67");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("insert into person values (0");
      i = 0;
      while (++i < 255)
         sBuffer.append(", 0");
      sBuffer.append(')');
      
      try 
      {
         driver.executeUpdate(sBuffer.toString());
         fail("68");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("update person set a0 = ?");
      i = 0;
      while (++i < 255)
         sBuffer.append(", a").append(i).append(" = ?");
      sBuffer.append(')');
      
      // Too many columns to be updated.
      try 
      {
         driver.prepareStatement(sBuffer.toString());
         fail("69");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("update person set a0 = 0");
      i = 0;
      while (++i < 255)
         sBuffer.append(", a").append(i).append(" = 0");
      sBuffer.append(')');
      
      try 
      {
         driver.executeUpdate(sBuffer.toString());
         fail("70");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("select * from person where a0 = ?");
      i = -1;
      while (++i < 254)
         sBuffer.append(" and a").append(i).append(" = ?");
      
      // Too many columns to be selected.
      try 
      {
         driver.prepareStatement(sBuffer.toString());
         fail("71");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
      catch (SQLParseException exception) {}
      
      sBuffer.setLength(0);
      sBuffer.append("select * from person where a0 = 0");
      i = -1;
      while (++i < 254)
         sBuffer.append(" and a").append(i).append(" = 0");
      
      try
      {
         driver.executeQuery(sBuffer.toString());
         fail("72");
      }
      catch (ArrayIndexOutOfBoundsException exception) {}
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
         fail("73");
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
         fail("74");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.execute("create index invalidindex");
         fail("75");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("alter table employee");
         fail("76");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("drop tablexxxxxx");
         fail("77");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("drop index xxxxxxxx");
         fail("78");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into employee () values()");
         fail("79");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("delete data from employee where id = 99");
         fail("80");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update lastname='updated' where id = 99");
         fail("81");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update lastname='updated' where id = 99");
         fail("82");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select table employee");
         fail("83");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.prepareStatement("select employee where id = ?");
         fail("84");
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
         fail("85");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("alter table notable drop primary key");
         fail("86");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop table company");
         fail("87");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop index deptindex on notable");
         fail("88");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("insert into company(id,name) values (1,'name')");
         fail("89");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("delete from company");
         fail("90");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("update company set name='updated'");
         fail("91");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeQuery("select * from company");
         fail("92");
      }
      catch (DriverException exception) {}
      try
      {
         driver.purge("company");
         fail("93");
      }
      catch (DriverException exception) {}
      try
      {
         driver.convert("company");
         fail("94");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getRowCount("company");
         fail("95");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getCurrentRowId("company");
         fail("96");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getRowCountDeleted("company");
         fail("97");
      }
      catch (DriverException exception) {}
      try
      {
         driver.getRowIterator("company");
         fail("98");
      }
      catch (DriverException exception) {}
      try
      {
         driver.recoverTable("company");
         fail("99");
      }
      catch (DriverException exception) {}
      try
      {
         driver.setRowInc("company", -1);
         fail("100");
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
         fail("101");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("alter table employee add primary key(nocolumn) ");
         fail("102");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("alter table employee rename nocolumn to newcolumn");
         fail("103");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop index firstname on employee");
         fail("104");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("insert into employee (id,lastname,firstname,years,dept,nocolumn) values(1,'lname1','fname1',1,1,'nocolumn')");
         fail("105");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("delete from employee where gender = 'M'");
         fail("106");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update employee set middlename='updated' where id = 99");
         fail("107");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select middlename from employee");
         fail("108");
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
      driver.execute("create table bignumbers (s short, i int, l long, c char(1))");
      
      // Invalid short values.
      try
      {
         driver.executeQuery("select * from bignumbers where s = 32768");
         fail("109");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where s = -32769");
         fail("110");
      }
      catch (SQLParseException exception) {}
      
      // Invalid int values.
      try
      {
         driver.executeQuery("select * from bignumbers where i = 2147483648");
         fail("111");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where i = -2147483649");
         fail("112");
      }
      catch (SQLParseException exception) {}
      
      // Invalid long values.
      try
      {
         driver.executeQuery("select * from bignumbers where l = 9223372036854775808");
         fail("113");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where l = -9223372036854775809");
         fail("114");
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
         fail("115");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into bignumbers (s) values (-32769)");
         fail("116");
      }
      catch (SQLParseException exception) {}
      
      // Invalid int values.
      try
      {
         driver.executeUpdate("insert into bignumbers (i) values (2147483648)");
         fail("117");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into bignumbers (i) values (-2147483649)");
         fail("118");
      }
      catch (SQLParseException exception) {}
      
      // Invalid long values.
      try
      {
         driver.executeUpdate("insert into bignumbers (l) values (9223372036854775808)");
         fail("119");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into bignumbers (l) values (-9223372036854775809)");
         fail("120");
      }
      catch (SQLParseException exception) {}
      
      // Valid numeric values.
      driver.executeUpdate("insert into bignumbers values (+32767, +2147483647, +9223372036854775807, 'j')");
      driver.executeUpdate("insert into bignumbers values (-32768, -2147483648, -9223372036854775808, 'i')");
      
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
         fail("121");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeQuery("select * from bignumbers where s = 0.0");
         fail("122");
      }
      catch (SQLParseException exception) {}
      
      // Invalid number.
      try
      {
         driver.executeQuery("select * from bignumbers where s = 'juliana'");
         fail("123");
      }
      catch (SQLParseException exception) {}
      
      // This can't crash Litebase.
      driver.executeQuery("select * from bignumbers where c = 'Juliana Carpes Imperial'").close();
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
         fail("124");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("insert into employee (id,lastname) values ('lname101', 101)");
         fail("125");
      }
      catch (SQLParseException exception) {}
      
      try
      {
         driver.executeUpdate("update employee set middlename='updated' where id = 99");
         fail("126");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update employee set id = 'lname101', lastname = 101)");
         fail("127");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update employee set id = 'lname101', id = 'lname101', id = 'lname101', id = 'lname101', id = 'lname101', id = 'lname101'");
         fail("128");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tries to alter the rowid.
    * 
    * @param driver The connection with Litebase.
    */
   private void testInvalidRowidAlter(LitebaseConnection driver)
   {
      try
      {
         driver.executeUpdate("insert into employee (id, rowid, lastname) values (1, 2, 'imperial')");
         fail("129");
      }
      catch (SQLParseException exception) {}
      try
      {
         driver.executeUpdate("update employee set id = 1, rowid = 2, lastname = 'imperial'");
         fail("130");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Tries to do a select * with too many fields.
    * 
    * @param driver The connection with Litebase.
    */
   private void testTooBigSelect(LitebaseConnection driver)
   {
      StringBuffer sBuffer = new StringBuffer(1201);
      int i = 0;
      
      if (driver.exists("person1"))
         driver.executeUpdate("drop table person1");
      sBuffer.append("create table person1 (a0 int");
      while (++i < 128)
         sBuffer.append(", a").append(i).append(" int");
      sBuffer.append(")");
      driver.execute(sBuffer.toString());
      
      sBuffer.setLength(0);
      i = 0;
      if (driver.exists("person2"))
         driver.executeUpdate("drop table person2");
      sBuffer.append("create table person2 (a0 int");      
      while (++i < 128)
         sBuffer.append(", a").append(i).append(" int");
      sBuffer.append(")");
      driver.execute(sBuffer.toString());
      
      sBuffer.setLength(0);
      i = 0;
      if (driver.exists("person3"))
         driver.executeUpdate("drop table person3");
      sBuffer.append("create table person3 (a0 int");      
      while (++i < 128)
         sBuffer.append(", a").append(i).append(" int");
      sBuffer.append(")");
      driver.execute(sBuffer.toString());
      
      try // Too many columns for a select.
      { 
         driver.executeQuery("select * from person1, person2, person3");
         fail("131");
      }
      catch (SQLParseException exception) {}
   }
   
   /**
    * Does tests with very long paths.
    */
   private void testLongPath()
   {
      StringBuffer sBuffer = new StringBuffer(256);      
      String path;
      int i;
      
      sBuffer.append(Convert.appendPath(Settings.appPath, "/"));
      i = 256 - sBuffer.length();
      while (--i >= 0)
         sBuffer.append('a');
      
      
      try // Path too long.
      {
         LitebaseConnection.getInstance("Test", sBuffer.toString()); 
         fail("132");
      }
      catch (DriverException exception) {}
         
      // Path + table name too long. 
      sBuffer.setLength(245);
      LitebaseConnection driver = LitebaseConnection.getInstance("Test", path = sBuffer.toString());
      try
      {
         driver.exists("person");
         fail("133");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeUpdate("drop table person");
         fail("134");
      }
      catch (DriverException exception) {}
      try
      {
         driver.execute("create table person (id int)");
         fail("135");
      }
      catch (DriverException exception) {}
      try
      {
         driver.executeQuery("select * from person");
         fail("136");
      }
      catch (DriverException exception) {}
      try
      {
         driver.recoverTable("person");
         fail("137");
      }
      catch (DriverException exception) {}
      try
      {
         driver.convert("person");
         fail("138");
      }      
      catch (DriverException exception) {}
      driver.closeAll();

      try
      {
         new File(path).delete();
      }
      catch (IOException exception)
      {
         exception.printStackTrace();
         fail("139");  
      }
      
      // File + table name too long for purge.
      sBuffer.setLength(244);
      driver = LitebaseConnection.getInstance("Test", path = sBuffer.toString());
      if (driver.exists("p"))
         driver.executeUpdate("drop table p");    
      driver.execute("create table p (id int)");
      driver.executeUpdate("insert into p values (0)");
      driver.executeUpdate("insert into p values (1)");
      driver.executeUpdate("delete p where id = 0");
      try
      {
         driver.purge("p");
         fail("140");
      }
      catch (DriverException exception) {}
      driver.executeUpdate("drop table p");      
      driver.closeAll();
      
      try
      {
         File file = new File(path + "/Test-p_.db");
         if (file.exists())
            file.delete();
         new File(path).delete();
      }
      catch (IOException exception)
      {
         exception.printStackTrace();
         fail("141");  
      }
   }
   
   private void testInvalidParameter()
   {
      try
      {
         LitebaseConnection.getInstance("Test", "xpto; chars_type = ascii");
         fail("140");
      }
      catch (DriverException exception) {}
      try
      {
         LitebaseConnection.getInstance("Test", "crypto; xpto");
         fail("141");
      }
      catch (DriverException exception) {}
   }
}
