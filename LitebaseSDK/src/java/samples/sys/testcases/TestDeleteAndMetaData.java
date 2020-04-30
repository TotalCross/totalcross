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
      
      assertTrue(meta.hasDefaultValue(1));
      assertEquals("1", meta.getDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      assertNull(meta.getDefaultValue(2));
      
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
      try 
      {
         meta.getDefaultValue(0);
         fail("19");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getDefaultValue(3);
         fail("20");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertTrue(meta.hasDefaultValue("Tempo"));
      assertEquals("1", meta.getDefaultValue("Tempo"));
      assertTrue(meta.hasDefaultValue("time"));
      assertEquals("1", meta.getDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      assertNull(meta.getDefaultValue("name"));
      
      // Invalid Column name.
      try 
      {
         meta.hasDefaultValue(null);
         fail("21");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("juliana");
         fail("22");
      } 
      catch (DriverException exception) {}
      try 
      {
         meta.getDefaultValue(null);
         fail("23");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getDefaultValue("juliana");
         fail("24");
      } 
      catch (DriverException exception) {}
      
      assertEquals(1, meta.getPKColumnIndices("tabSync")[0]);
      assertEquals("time", meta.getPKColumnNames("tabSync")[0]);
      
      // Table is not in the select.
      try 
      {
         meta.getPKColumnIndices("tabsync2");
         fail("25");
      }
      catch (DriverException exception) {}
      try 
      {
         meta.getPKColumnNames("tabsync2");
         fail("26");
      }
      catch (DriverException exception) {}
      
      try 
      {
         meta.getPKColumnIndices(null);
         fail("27");
      }
      catch (NullPointerException exception) {}
      catch (DriverException exception) {}
      try 
      {
         meta.getPKColumnNames(null);
         fail("28");
      }
      catch (NullPointerException exception) {}
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
      assertTrue(meta.hasDefaultValue(2));
      assertEquals("1", meta.getDefaultValue(2));
      assertTrue(meta.hasDefaultValue("Tempo"));
      assertEquals("1", meta.getDefaultValue("Tempo"));
      assertTrue(meta.hasDefaultValue("time"));
      assertEquals("1", meta.getDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      assertNull(meta.getDefaultValue("name"));
      assertEquals(1, meta.getPKColumnIndices("tabsync")[0]);
      assertEquals("time", meta.getPKColumnNames("tabsync")[0]);
      rs.close();

      // Simple select: select * from tablename.
      assertEquals(2, (meta = (rs = driver.executeQuery("select * from tabsync")).getResultSetMetaData()).getColumnCount());
      assertEquals(5, meta.getColumnDisplaySize(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("29");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("30");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("31");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("32");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("long", meta.getColumnTypeName(1));
      assertEquals("chars", meta.getColumnTypeName(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("33");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("34");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("time", meta.getColumnLabel(1));
      assertEquals("name", meta.getColumnLabel(2));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("35");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(3);
         fail("36");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName("TIME"));
      assertEquals("tabsync", meta.getColumnTableName("namE"));
      
      // Ivalid column name.
      try 
      {
         meta.getColumnTableName("Tempo");
         fail("37");
      } 
      catch (DriverException exception) {}
      try
      {
         meta.getColumnTableName(null);
         fail("38");
      } 
      catch (NullPointerException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      
      // Invalid column index.
      try
      {
         meta.getColumnTableName(0);
         fail("39");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(3);
         fail("40");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull(1));
      assertTrue(meta.isNotNull(2));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(0);
         fail("41");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(3);
         fail("42");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(null);
         fail("43");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("tempo");
         fail("44");
      } 
      catch (DriverException exception) {}
      
      assertTrue(meta.hasDefaultValue(1));
      assertEquals("1", meta.getDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      assertNull(meta.getDefaultValue(2));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(0);
         fail("45");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(3);
         fail("46");
      } 
      catch (IllegalArgumentException exception) {}
      try 
      {
         meta.getDefaultValue(0);
         fail("47");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getDefaultValue(3);
         fail("48");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals(true, meta.hasDefaultValue("time"));
      assertEquals("1", meta.getDefaultValue("time"));
      assertEquals(false, meta.hasDefaultValue("name"));
      assertNull(meta.getDefaultValue("name"));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(null);
         fail("49");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("juliana");
         fail("50");
      } 
      catch (DriverException exception) {}
      try 
      {
         meta.getDefaultValue(null);
         fail("51");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getDefaultValue("juliana");
         fail("52");
      } 
      catch (DriverException exception) {}
      
      assertEquals(1, meta.getPKColumnIndices("tabsync")[0]);
      assertEquals("time", meta.getPKColumnNames("tabsync")[0]);
      
      rs.close();
      
      // Tests what happens if the result set is closed in a simple query.
      try
      {
         meta.getColumnCount();
         fail("53");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnDisplaySize(1);
         fail("54");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnType(1);
         fail("55");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnLabel(1);
         fail("56");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName(1);
         fail("57");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName("TIME");
         fail("58");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull(1);
         fail("59");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull("TIME");
         fail("60");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue(1);
         fail("61");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue("TIME");
         fail("62");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getDefaultValue(1);
         fail("63");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getDefaultValue("TIME");
         fail("64");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getPKColumnIndices("tabsync");
         fail("65");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getPKColumnNames("tabsync");
         fail("66");
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
         fail("67");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("68");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals(ResultSetMetaData.INT_TYPE, meta.getColumnType(1));
      assertEquals(ResultSetMetaData.LONG_TYPE, meta.getColumnType(2));
      assertEquals(ResultSetMetaData.CHAR_NOCASE_TYPE, meta.getColumnType(3));   
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("69");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("70");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("int", meta.getColumnTypeName(1));
      assertEquals("long", meta.getColumnTypeName(2));
      assertEquals("chars", meta.getColumnTypeName(3));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("71");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("72");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("rowid", meta.getColumnLabel(1));
      assertEquals("time", meta.getColumnLabel(2));
      assertEquals("name", meta.getColumnLabel(3));
      
      // Invalid Column index.
      try
      {
         meta.getColumnDisplaySize(0);
         fail("73");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnDisplaySize(4);
         fail("74");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName("rowid"));
      assertEquals("tabsync", meta.getColumnTableName("TIME"));
      assertEquals("tabsync", meta.getColumnTableName("namE"));
      
      // Ivalid column name.
      try 
      {
         meta.getColumnTableName("Tempo");
         fail("75");
      } 
      catch (DriverException exception) {}
      try
      {
         meta.getColumnTableName(null);
         fail("76");
      } 
      catch (NullPointerException exception) {}
      
      assertEquals("tabsync", meta.getColumnTableName(1));
      assertEquals("tabsync", meta.getColumnTableName(2));
      assertEquals("tabsync", meta.getColumnTableName(3));
      
      // Invalid column index.
      try
      {
         meta.getColumnTableName(0);
         fail("77");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(4);
         fail("78");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull(1));
      assertFalse(meta.isNotNull(2));
      assertTrue(meta.isNotNull(3));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(0);
         fail("79");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(4);
         fail("80");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.isNotNull("rowid"));
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      
      // Invalid Column index.
      try 
      {
         meta.isNotNull(null);
         fail("81");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("Tempo");
         fail("82");
      } 
      catch (DriverException exception) {}
      
      assertFalse(meta.hasDefaultValue(1));
      assertNull(meta.getDefaultValue(1));
      assertTrue(meta.hasDefaultValue(2));
      assertEquals("1", meta.getDefaultValue(2));
      assertFalse(meta.hasDefaultValue(3));
      assertNull(meta.getDefaultValue(3));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(0);
         fail("83");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(4);
         fail("84");
      } 
      catch (IllegalArgumentException exception) {}
      try 
      {
         meta.getDefaultValue(0);
         fail("85");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getDefaultValue(4);
         fail("86");
      } 
      catch (IllegalArgumentException exception) {}
      
      assertFalse(meta.hasDefaultValue("rowid"));
      assertNull(meta.getDefaultValue("rowid"));
      assertTrue(meta.hasDefaultValue("time"));
      assertEquals("1", meta.getDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      assertNull(meta.getDefaultValue("name"));
      
      // Invalid Column index.
      try 
      {
         meta.hasDefaultValue(null);
         fail("87");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("juliana");
         fail("88");
      } 
      catch (DriverException exception) {}
      try 
      {
         meta.getDefaultValue(null);
         fail("89");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getDefaultValue("juliana");
         fail("90");
      } 
      catch (DriverException exception) {}
      
      assertEquals(1, meta.getPKColumnIndices("tabsync")[0]);
      assertEquals("time", meta.getPKColumnNames("tabsync")[0]);
      
      rs.close();
      
      // Tests what happens if the result set is closed in a simple query.
      try
      {
         meta.getColumnCount();
         fail("91");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnDisplaySize(1);
         fail("92");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnType(1);
         fail("93");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnLabel(1);
         fail("94");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName(1);
         fail("95");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName("TIME");
         fail("96");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull(1);
         fail("97");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull("TIME");
         fail("98");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue(1);
         fail("99");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue("TIME");
         fail("100");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getDefaultValue(1);
         fail("101");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getDefaultValue("TIME");
         fail("102");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getPKColumnIndices("tabsync");
         fail("103");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getPKColumnNames("tabsync");
         fail("104");
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
         fail("105");
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
         fail("106");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getColumnTableName(0);
         fail("107");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getColumnTableName(5);
         fail("108");
      } 
      catch (IllegalArgumentException exception) {}
      assertFalse(meta.isNotNull(1));
      assertTrue(meta.isNotNull(2));
      assertFalse(meta.isNotNull(3));
      assertFalse(meta.isNotNull(4));
      try 
      {
         meta.isNotNull(0);
         fail("109");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.isNotNull(5);
         fail("110");
      } 
      catch (IllegalArgumentException exception) {}
      assertFalse(meta.isNotNull("time"));
      assertTrue(meta.isNotNull("name"));
      assertFalse(meta.isNotNull("life"));
      try 
      {
         meta.isNotNull(null);
         fail("111");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.isNotNull("Tempo");
         fail("112");
      } 
      catch (DriverException exception) {}
      assertTrue(meta.hasDefaultValue(1));
      assertEquals("1", meta.getDefaultValue(1));
      assertFalse(meta.hasDefaultValue(2));
      assertNull(meta.getDefaultValue(2));
      assertFalse(meta.hasDefaultValue(3));
      assertNull(meta.getDefaultValue(3));
      assertTrue(meta.hasDefaultValue(4));
      assertEquals("Juli", meta.getDefaultValue(4));
      try 
      {
         meta.hasDefaultValue(0);
         fail("113");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.hasDefaultValue(5);
         fail("114");
      } 
      catch (IllegalArgumentException exception) {}
      try 
      {
         meta.getDefaultValue(0);
         fail("115");
      } 
      catch (IllegalArgumentException exception) {}
      try
      {
         meta.getDefaultValue(5);
         fail("116");
      } 
      catch (IllegalArgumentException exception) {}
      assertTrue(meta.hasDefaultValue("time"));
      assertEquals("1", meta.getDefaultValue("time"));
      assertFalse(meta.hasDefaultValue("name"));
      assertNull(meta.getDefaultValue("name"));
      assertFalse(meta.hasDefaultValue("life"));
      assertNull(meta.getDefaultValue("life"));
      try 
      {
         meta.hasDefaultValue(null);
         fail("117");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.hasDefaultValue("Tempo");
         fail("118");
      } 
      catch (DriverException exception) {}
      try 
      {
         meta.getDefaultValue(null);
         fail("119");
      } 
      catch (NullPointerException exception) {}
      try
      {
         meta.getDefaultValue("Tempo");
         fail("120");
      } 
      catch (DriverException exception) {}
      assertEquals(1, meta.getPKColumnIndices("tabsync")[0]);
      assertEquals("time", meta.getPKColumnNames("tabsync")[0]);
      assertEquals(2, meta.getPKColumnIndices("tabsync2")[0]);
      assertEquals(1, meta.getPKColumnIndices("tabsync2")[1]);
      assertEquals("name", meta.getPKColumnNames("tabsync2")[0]);
      assertEquals("life", meta.getPKColumnNames("tabsync2")[1]);
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
         fail("121");
      }
      catch (DriverException exception) {}
      assertTrue(meta.isNotNull(2));
      assertFalse(meta.isNotNull(3));
      assertFalse(meta.isNotNull(4));
      try // Column does not have an underlining table.
      {
         meta.isNotNull("c");
         fail("122");
      }
      catch (DriverException exception) {}
      assertTrue(meta.isNotNull("n1"));
      assertFalse(meta.isNotNull("n2"));
      assertFalse(meta.isNotNull("abt"));
      try // Column does not have an underlining table.
      {
         meta.hasDefaultValue(1);
         fail("123");
      }
      catch (DriverException exception) {}
      assertFalse(meta.hasDefaultValue(2));
      assertNull(meta.getDefaultValue(2));
      assertTrue(meta.hasDefaultValue(3));
      assertEquals("Juli", meta.getDefaultValue(3));
      assertTrue(meta.hasDefaultValue(4));
      assertEquals("1", meta.getDefaultValue(4));
      try // Column does not have an underlining table.
      {
         meta.hasDefaultValue("c");
         fail("124");
      }
      catch (DriverException exception) {}
      try // Column does not have an underlining table.
      {
         meta.getDefaultValue("c");
         fail("125");
      }
      catch (DriverException exception) {}
      assertFalse(meta.hasDefaultValue("n1"));
      assertNull(meta.getDefaultValue("n1"));
      assertTrue(meta.hasDefaultValue("n2"));
      assertEquals("Juli", meta.getDefaultValue("n2"));
      assertTrue(meta.hasDefaultValue("abt"));
      assertEquals("1", meta.getDefaultValue("abt"));
      assertEquals(1, meta.getPKColumnIndices("tabsync")[0]);
      assertEquals("time", meta.getPKColumnNames("tabsync")[0]);
      assertEquals(2, meta.getPKColumnIndices("tabsync2")[0]);
      assertEquals(1, meta.getPKColumnIndices("tabsync2")[1]);
      assertEquals("name", meta.getPKColumnNames("tabsync2")[0]);
      assertEquals("life", meta.getPKColumnNames("tabsync2")[1]);
      rs.close();

      // Tests what happens if the result set is closed in a complex query.
      try
      {
         meta.getColumnCount();
         fail("126");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnDisplaySize(1);
         fail("127");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnType(1);
         fail("128");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnLabel(1);
         fail("129");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName(1);
         fail("130");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getColumnTableName("c");
         fail("131");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull(1);
         fail("132");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.isNotNull("c");
         fail("133");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue(1);
         fail("134");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.hasDefaultValue("c");
         fail("135");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getDefaultValue(1);
         fail("136");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getDefaultValue("c");
         fail("137");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getPKColumnIndices("tabsync");
         fail("138");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         meta.getPKColumnNames("tabsync2");
         fail("139");
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
         driver.execute("CREATE TABLE tabsync (time LONG primary key default 1, NAME CHAR(5) NOCASE not null)");
      } 
      catch (AlreadyCreatedException ace) {}
      try
      {
         driver.execute("CREATE TABLE tabsync2 (life LONG, NAME CHAR(5) NOCASE default 'Juli', primary key(name, life))");
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
         assertTrue(exception.getMessage().startsWith("It is not possible to open a table within a connection with a different"));
         driver.executeUpdate("drop table tabsync");
         driver.executeUpdate("drop table tabsync2");
         driver.execute("CREATE TABLE tabsync (time LONG primary key default 1, NAME CHAR(5) NOCASE not null)");
         driver.execute("CREATE TABLE tabsync2 (life LONG, NAME CHAR(5) NOCASE default 'Juli', primary key(name, life))");
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
         fail("140");
      } 
      catch (AlreadyCreatedException ace) {}
      try
      {
         driver.execute("CREATE TABLE tabsync2 (life LONG, NAME CHAR(5) NOCASE)");
         fail("141");
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
