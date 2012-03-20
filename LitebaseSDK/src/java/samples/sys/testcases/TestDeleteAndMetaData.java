/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package samples.sys.testcases;

import litebase.*;
import totalcross.unit.*;

/** 
 * This tests if a table was successfully deleted, if the insert of a Long is working and the meta data class.
 */
public class TestDeleteAndMetaData extends TestCase
{
   LitebaseConnection driver;
   ResultSet rs;

   /** 
    * Tests the result set meta data.
    */
   private void testMetaData()
   {
      // In table's order.
      rs = driver.executeQuery("select time as Tempo, name from tabsync order by rowid");
      ResultSetMetaData meta = rs.getResultSetMetaData();

      assertEquals(2, meta.getColumnCount());
      assertEquals(20, meta.getColumnDisplaySize(1));
      assertEquals(5, meta.getColumnDisplaySize(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("1");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("2");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnType(0);
         fail("3");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnType(3);
         fail("4");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("long", meta.getColumnTypeName(1));
      assertEquals("chars", meta.getColumnTypeName(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnTypeName(0);
         fail("5");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTypeName(3);
         fail("6");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("tempo", meta.getColumnLabel(1));
      assertEquals("name", meta.getColumnLabel(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnLabel(0);
         fail("7");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnLabel(3);
         fail("8");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName("time"));
      assertEquals("tabsync", meta.getColumnTableName("Tempo"));
      assertEquals("tabsync", meta.getColumnTableName("name"));
      
      // Invalid column name.
      try
      {
         meta.getColumnTableName(null);
         fail("9");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getColumnTableName("barbara");
         fail("10");
      }
      catch (DriverException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      
      // Invalid Column index.
      try 
      {
         meta.getColumnTableName(0);
         fail("11");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(3);
         fail("12");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull(1));
      assertTrue(meta.isNotNull(2));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(0);
         fail("13");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(3);
         fail("14");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull("Tempo"));
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      
      // Invalid Column name.
      try 
      {
         meta.isNotNull(null);
         fail("15");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("juliana");
         fail("16");
      } 
      catch (DriverException exception) {}
      
      assertFalse(meta.hasDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(0);
         fail("17");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(3);
         fail("18");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.hasDefaultValue("Tempo"));
      assertFalse(meta.hasDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      
      // Invalid Column name.
      try 
      {
         meta.hasDefaultValue(null);
         fail("19");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("juliana");
         fail("20");
      } 
      catch (DriverException exception) {}
      
      rs.close();

      // Out of table's order.
      assertEquals(2, (meta = (rs = driver.executeQuery("select name, time as Tempo from tabsync")).getResultSetMetaData()).getColumnCount());
      assertEquals(5, meta.getColumnDisplaySize(1));
      assertEquals(20, meta.getColumnDisplaySize(2));
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(2));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(1));
      assertEquals("chars", meta.getColumnTypeName(1));
      assertEquals("long", meta.getColumnTypeName(2));
      assertEquals("name", meta.getColumnLabel(1));
      assertEquals("tempo", meta.getColumnLabel(2));
      assertEquals("tabsync", meta.getColumnTableName("time"));
      assertEquals("tabsync", meta.getColumnTableName("Tempo"));
      assertEquals("tabsync", meta.getColumnTableName("name"));
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      assertTrue(meta.isNotNull(1));
      assertFalse(meta.isNotNull(2));
      assertFalse(meta.isNotNull("Tempo"));
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      assertFalse(meta.hasDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      assertFalse(meta.hasDefaultValue("Tempo"));
      assertFalse(meta.hasDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      rs.close();

      // Simple select: select * from tablename.
      assertEquals(2, (meta = (rs = driver.executeQuery("select * from tabsync")).getResultSetMetaData()).getColumnCount());
      assertEquals(5, meta.getColumnDisplaySize(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("21");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("22");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("23");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("24");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("long", meta.getColumnTypeName(1));
      assertEquals("chars", meta.getColumnTypeName(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("25");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("26");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("time", meta.getColumnLabel(1));
      assertEquals("name", meta.getColumnLabel(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("27");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("28");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName("TIME"));
      assertEquals("tabsync", meta.getColumnTableName("namE"));
      
      // Ivalid column name.
      try 
      {
         meta.getColumnTableName("Tempo");
         fail("29");
      } 
      catch (DriverException exception) {}
      try
      {
         meta.getColumnTableName(null);
         fail("30");
      } 
      catch (NullPointerException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      
      // Invalid column index.
      try
      {
         meta.getColumnTableName(0);
         fail("31");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(3);
         fail("32");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull(1));
      assertTrue(meta.isNotNull(2));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(0);
         fail("33");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(3);
         fail("34");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(null);
         fail("35");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("tempo");
         fail("36");
      } 
      catch (DriverException exception) {}
      
      assertFalse(meta.hasDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(0);
         fail("37");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(3);
         fail("38");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.hasDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(null);
         fail("39");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("juliana");
         fail("40");
      } 
      catch (DriverException exception) {}
      
      rs.close();
      
      // Tests what happens if the result set is closed in a simple query.
      try
      {
         meta.getColumnCount();
         fail("41");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnDisplaySize(1);
         fail("42");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnType(1);
         fail("43");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnLabel(1);
         fail("44");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName(1);
         fail("45");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName("TIME");
         fail("46");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull(1);
         fail("47");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull("TIME");
         fail("48");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue(1);
         fail("49");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue("TIME");
         fail("50");
      } 
      catch (IllegalStateException exception) {}
      
      // Simple select: select rowid, time, name from tablename.
      assertEquals(3, (meta = (rs = driver.executeQuery("select rowid, time, name from tabsync")).getResultSetMetaData()).getColumnCount());
      assertEquals(11, meta.getColumnDisplaySize(1));
      assertEquals(20, meta.getColumnDisplaySize(2));
      assertEquals(5, meta.getColumnDisplaySize(3));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("51");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("52");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals(ResultSetMetaData.INT_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(2));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(3));   
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("53");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("54");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("int", meta.getColumnTypeName(1));
      assertEquals("long", meta.getColumnTypeName(2));
      assertEquals("chars", meta.getColumnTypeName(3));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("55");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("56");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("rowid", meta.getColumnLabel(1));
      assertEquals("time", meta.getColumnLabel(2));
      assertEquals("name", meta.getColumnLabel(3));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("57");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("58");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName("rowid"));
      assertEquals("tabsync", meta.getColumnTableName("TIME"));
      assertEquals("tabsync", meta.getColumnTableName("namE"));
      
      // Ivalid column name.
      try 
      {
         meta.getColumnTableName("Tempo");
         fail("59");
      } 
      catch (DriverException exception) {}
      try
      {
         meta.getColumnTableName(null);
         fail("60");
      } 
      catch (NullPointerException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      assertEquals("tabsync", meta.getColumnTableName(3));
      
      // Invalid column index.
      try
      {
         meta.getColumnTableName(0);
         fail("61");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(4);
         fail("62");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull(1));
      assertFalse(meta.isNotNull(2));
      assertTrue(meta.isNotNull(3));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(0);
         fail("63");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(4);
         fail("64");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull("rowid"));
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(null);
         fail("65");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("Tempo");
         fail("66");
      } 
      catch (DriverException exception) {}
      
      assertFalse(meta.hasDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      assertFalse(meta.hasDefaultValue(3));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(0);
         fail("67");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(4);
         fail("68");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.hasDefaultValue("rowid"));
      assertFalse(meta.hasDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(null);
         fail("69");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("juliana");
         fail("70");
      } 
      catch (DriverException exception) {}
      
      rs.close();
      
      // Tests what happens if the result set is closed in a simple query.
      try
      {
         meta.getColumnCount();
         fail("71");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnDisplaySize(1);
         fail("72");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnType(1);
         fail("73");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnLabel(1);
         fail("74");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName(1);
         fail("75");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName("TIME");
         fail("76");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull(1);
         fail("77");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull("TIME");
         fail("78");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue(1);
         fail("79");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue("TIME");
         fail("80");
      } 
      catch (IllegalStateException exception) {}
      
      // Join
      assertEquals(4, (meta = (rs = driver.executeQuery("select * from tabsync, tabsync2")).getResultSetMetaData()).getColumnCount());
      assertEquals(5, meta.getColumnDisplaySize(2));
      assertEquals(5, meta.getColumnDisplaySize(4));
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(2));
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(3));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(4));
      assertEquals("long", meta.getColumnTypeName(1));
      assertEquals("chars", meta.getColumnTypeName(2));
      assertEquals("long", meta.getColumnTypeName(3));
      assertEquals("chars", meta.getColumnTypeName(4));
      assertEquals("time", meta.getColumnLabel(1));
      assertEquals("name", meta.getColumnLabel(2));
      assertEquals("life", meta.getColumnLabel(3));
      assertEquals("name", meta.getColumnLabel(4));
      assertEquals("tabsync", meta.getColumnTableName("TIME"));
      try
      {
         meta.getColumnTableName("Tempo");
         fail("81");
      } 
      catch (DriverException exception) {}
      assertEquals("tabsync", meta.getColumnTableName("namE"));
      assertEquals("tabsync2", meta.getColumnTableName("life"));
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      assertEquals("tabsync2", meta.getColumnTableName(3));
      assertEquals("tabsync2", meta.getColumnTableName(4));
      try
      {
         meta.getColumnTableName(null);
         fail("81");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getColumnTableName(0);
         fail("82");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(5);
         fail("83");
      } 
      catch (IllegalArgumentException exception) {}
      assertFalse(meta.isNotNull(1));
      assertTrue(meta.isNotNull(2));
      assertFalse(meta.isNotNull(3));
      assertFalse(meta.isNotNull(4));
      try 
      {
         meta.isNotNull(0);
         fail("84");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(5);
         fail("85");
      } 
      catch (IllegalArgumentException exception) {}
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      assertFalse(meta.isNotNull("life"));
      try 
      {
         meta.isNotNull(null);
         fail("86");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("Tempo");
         fail("87");
      } 
      catch (DriverException exception) {}
      assertFalse(meta.hasDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      assertFalse(meta.hasDefaultValue(3));
      assertTrue(meta.hasDefaultValue(4));
      try 
      {
         meta.hasDefaultValue(0);
         fail("88");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(5);
         fail("89");
      } 
      catch (IllegalArgumentException exception) {}
      assertFalse(meta.hasDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      assertFalse(meta.hasDefaultValue("life"));
      try 
      {
         meta.hasDefaultValue(null);
         fail("90");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("Tempo");
         fail("91");
      } 
      catch (DriverException exception) {}
      rs.close();

      // Aggregation and functions.
      assertEquals(4, (meta = (rs = driver.executeQuery("select count(*) as c, upper(tabsync.name) as n1, lower(tabsync2.name) as n2, " 
                                                     + "abs(tabsync.time) as abt from tabsync, tabsync2")).getResultSetMetaData()).getColumnCount());
      assertEquals(11, meta.getColumnDisplaySize(1));
      assertEquals(5, meta.getColumnDisplaySize(2));
      assertEquals(5, meta.getColumnDisplaySize(3));
      assertEquals(20, meta.getColumnDisplaySize(4));
      assertEquals(ResultSetMetaData.INT_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.CHAR_TYPE, meta.getColumnType(2));
      assertEquals(ResultSetMetaData.CHAR_TYPE, meta.getColumnType(3));
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(4));
      assertEquals("int", meta.getColumnTypeName(1));
      assertEquals("chars", meta.getColumnTypeName(2));
      assertEquals("chars", meta.getColumnTypeName(3));
      assertEquals("long", meta.getColumnTypeName(4));
      assertEquals("c", meta.getColumnLabel(1));
      assertEquals("n1", meta.getColumnLabel(2));
      assertEquals("n2", meta.getColumnLabel(3));
      assertEquals("abt", meta.getColumnLabel(4));
      assertNull(meta.getColumnTableName(1));
      assertNull(meta.getColumnTableName("c"));
      assertEquals("tabsync", meta.getColumnTableName(2));
      assertEquals("tabsync", meta.getColumnTableName("n1"));
      assertEquals("tabsync2", meta.getColumnTableName(3));
      assertEquals("tabsync2", meta.getColumnTableName("n2"));
      assertEquals("tabsync", meta.getColumnTableName(4));
      assertEquals("tabsync", meta.getColumnTableName("abt"));
      try // Column does not have an underlining table.
      {
         meta.isNotNull(1);
         fail("92");
      }
      catch (DriverException exception) {}
      assertTrue(meta.isNotNull(2));
      assertFalse(meta.isNotNull(3));
      assertFalse(meta.isNotNull(4));
      try // Column does not have an underlining table.
      {
         meta.isNotNull("c");
         fail("93");
      }
      catch (DriverException exception) {}
      assertTrue(meta.isNotNull("n1"));
      assertFalse(meta.isNotNull("n2"));
      assertFalse(meta.isNotNull("abt"));
      try // Column does not have an underlining table.
      {
         meta.hasDefaultValue(1);
         fail("94");
      }
      catch (DriverException exception) {}
      assertFalse(meta.hasDefaultValue(2));
      assertTrue(meta.hasDefaultValue(3));
      assertFalse(meta.hasDefaultValue(4));
      try // Column does not have an underlining table.
      {
         meta.hasDefaultValue("c");
         fail("95");
      }
      catch (DriverException exception) {}
      assertFalse(meta.hasDefaultValue("n1"));
      assertTrue(meta.hasDefaultValue("n2"));
      assertFalse(meta.hasDefaultValue("abt"));
      rs.close();

      // Tests what happens if the result set is closed in a complex query.
      try
      {
         meta.getColumnCount();
         fail("96");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnDisplaySize(1);
         fail("97");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnType(1);
         fail("98");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnLabel(1);
         fail("99");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName(1);
         fail("100");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName("c");
         fail("101");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull(1);
         fail("102");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull("c");
         fail("103");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue(1);
         fail("104");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue("c");
         fail("105");
      } 
      catch (IllegalStateException exception) {}
   }

   /** 
    * The test main method.
    */
   public void testRun()
   {
      driver = AllTests.getInstance("Test");

      // Creates the tables.
      try
      {
         driver.execute("CREATE TABLE tabsync (time LONG primary key, NAME CHAR(5) NOCASE not null)");
      } 
      catch (AlreadyCreatedException ace) {}
      try
      {
         driver.execute("CREATE TABLE tabsync2 (life LONG, NAME CHAR(5) NOCASE default 'Juli')");
      } 
      catch (AlreadyCreatedException ace) {}

      // Deletes their elements.
      try
      {
         driver.executeUpdate("DELETE tabsync");
         driver.executeUpdate("DELETE tabsync2");
      } 
      catch (DriverException exception)
      {
         assertTrue(exception.getMessage().equals("It is not possible to open a table within a connection with a different string format."));
         driver.executeUpdate("drop table tabsync");
         driver.executeUpdate("drop table tabsync2");
         driver.execute("CREATE TABLE tabsync (time LONG primary key, NAME CHAR(5) NOCASE not null)");
         driver.execute("CREATE TABLE tabsync2 (life LONG, NAME CHAR(5) NOCASE default 'Juli')");
      }

      // Inserts records.
      assertEquals(1, driver.executeUpdate("INSERT INTO tabsync VALUES (1, 'Hi')"));
      assertEquals(1, driver.executeUpdate("INSERT INTO tabsync2 VALUES (2, 'Hi')"));
      driver.closeAll();
      driver = AllTests.getInstance("Test");

      // Tries to create the tables again.
      try
      {
         driver.execute("CREATE TABLE tabsync (time LONG, NAME CHAR(5) NOCASE)");
         fail("59");
      } 
      catch (AlreadyCreatedException ace) {}
      try
      {
         driver.execute("CREATE TABLE tabsync2 (life LONG, NAME CHAR(5) NOCASE)");
         fail("60");
      } 
      catch (AlreadyCreatedException ace) {}

      // Checks that the long value is correct.
      assertTrue((rs = driver.executeQuery("SELECT time FROM tabsync")).first());
      assertEquals(1, rs.getRowCount());
      assertEquals(1, rs.getLong(1));
      assertEquals(1, rs.getLong("time"));
      rs.close();

      driver.executeUpdate("DELETE tabsync"); // Empties it.
      assertEquals(1, driver.executeUpdate("INSERT INTO tabsync VALUES (2, 'Hi')"));
      testMetaData();  // Tests metadata.
      driver.closeAll();
   }
}
