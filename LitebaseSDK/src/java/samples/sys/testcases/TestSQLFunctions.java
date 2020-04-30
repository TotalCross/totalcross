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
 * This test verifies if all Litebase data type functions are working properly
 * FUNCTIONS: year, month, day, hour, minute, second, millis, upper, lower, and abs // rnovais@570_2.
 *
 * ABS(short, long, float, double) // rnovais@570_6.
 *
 * month(birth) = 2 and year(birth) = 2006 // rnovais@570_108.
 */
public class TestSQLFunctions extends TestCase
{
   LitebaseConnection driver = AllTests.getInstance("Test");
   
   /** 
    * The main test method.
    */
   public void testRun()
   {
      ResultSet resultSet;
      
      // First inserts the items into a new table.
      output("Create simple table and insert.");
      try
      {
         if (driver.exists("person"))
            driver.executeUpdate("drop table person");
         driver.execute("create table person(name char(16), amount int, amount1 short, amount2 long, amount3 float, amount4 double, birth Date, " 
                                                                                                                 + "years DateTime)");
         driver.executeUpdate("insert into person values ('Renato Novais',-1,-2,-100l,-1.2,-456.0, '  2007/5-3  ', ' 2007/11-2    12:08:01:234 ')");
         driver.executeUpdate("insert into person values ('indira gomes',13,-8,-25l,5.2,-154.0, '2006/7/8 ', '2006/08-21 0:08')");
         driver.executeUpdate("insert into person values ('Lucas Novais',-20,-456,48L,-5.9,-954.2, '2008/4/6', ' 2008/06/06  13:45 ')");
         driver.executeUpdate("insert into person values ('Zenes Lima',-15,-54, -5698L,-8.3,-456.5, '2005/9/12 ', '2005/01-4 1:50')");
      }
      catch (DriverException exception)
      {
         fail("1");
      }

      // Tests functions that are properly applied.
      // month(DATE).
      assertNotNull(resultSet = driver.executeQuery("Select month(years) as mon1, years from person"));
      assertEquals(4, resultSet.getRowCount());
      assertTrue(resultSet.next());
      try
      {
         resultSet.getInt(1);
         fail("2");
      }
      catch (DriverException exception) {}
      assertEquals(11, resultSet.getShort(1));
      assertTrue(resultSet.next());
      assertEquals(resultSet.getShort(1), 8);
      assertTrue(resultSet.next());
      assertEquals(resultSet.getShort("mon1"), 6);
      assertTrue(resultSet.next());
      assertEquals(1, resultSet.getShort(1));
      assertFalse(resultSet.next());
      
      // Tests functions with getStrings().
      assertTrue(resultSet.first());
      String[][] strings = resultSet.getStrings();
      assertEquals(4, strings.length);
      assertEquals("11", strings[0][0]);
      assertEquals("8", strings[1][0]);
      assertEquals("6", strings[2][0]);
      assertEquals("1", strings[3][0]);
      resultSet.close();
      
      // years(DATE).
      assertNotNull(resultSet = driver.executeQuery("Select year(years) as mon1, years from person where day(years) >= 6"));
      assertEquals(2, resultSet.getRowCount());
      assertTrue(resultSet.next());
      try
      {
         resultSet.getLong(1);
         fail("3");
      }
      catch (DriverException exception) {}
      assertEquals(2006, resultSet.getShort(1));
      assertTrue(resultSet.next());
      assertEquals(2008, resultSet.getShort("mon1"));
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertNotNull(resultSet = driver.executeQuery("Select hour(years) as h1, day(birth) as d1 from person where month(birth) != 7 " +
      		"                                                                                                 and hour(years) != 0"));
      assertEquals(3, resultSet.getRowCount());
      assertTrue(resultSet.next());
      try
      {
         resultSet.getDate(1);
         fail("4");
      }
      catch (DriverException exception) {}
      try
      {
         resultSet.getDateTime(2);
         fail("5");
      }
      catch (DriverException exception) {}
      assertEquals(12, resultSet.getShort(1));
      assertEquals(3, resultSet.getShort(2));
      assertEquals("12\t3", resultSet.rowToString());
      assertTrue(resultSet.next());
      assertEquals(13, resultSet.getShort("h1"));
      assertEquals(6, resultSet.getShort("d1"));
      assertEquals("13\t6", resultSet.rowToString());
      assertTrue(resultSet.next());
      assertEquals(1, resultSet.getShort(1));
      assertEquals(12, resultSet.getShort(2));
      assertEquals("1\t12", resultSet.rowToString());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertNotNull(resultSet = driver.executeQuery("Select  millis(years) as mil1, minute(years) as sec1 from person where birth > '2005/9-12' " 
                                                  + "order by years"));
      assertEquals(3, resultSet.getRowCount());
      assertTrue(resultSet.next());
      try
      {
         resultSet.getFloat(1);
         fail("6");
      }
      catch (DriverException exception) {}
      try
      {
         resultSet.getDouble(2);
         fail("7");
      }
      catch (DriverException exception) {}
      assertEquals(0, resultSet.getShort("mil1"));
      assertEquals(8, resultSet.getShort("sec1"));
      assertTrue(resultSet.next());
      assertEquals(234, resultSet.getShort(1));
      assertEquals(8, resultSet.getShort(2));
      assertTrue(resultSet.next());
      assertEquals(0, resultSet.getShort(1));
      assertEquals(45, resultSet.getShort(2));
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertNotNull(resultSet = driver.executeQuery("Select year(birth) as y1, month(birth) as m1, day(birth) as d1 from person " +
      		"                                                                                      where year(birth) = 2005"));
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(2005, resultSet.getShort(1));
      assertEquals(9, resultSet.getShort(2));
      assertEquals(12, resultSet.getShort(3));
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertNotNull(resultSet = driver.executeQuery("Select hour(years) as h1, minute(years) as m1, second(years) as d1 from person " +
      		                                                                                                        "where hour(years) >= 12"));
      assertEquals(2, resultSet.getRowCount());
      assertTrue(resultSet.next());
      try
      {
         resultSet.getFloat(1);
         fail("8");
      }
      catch (DriverException exception) {}
      assertEquals(12, resultSet.getShort(1));
      assertEquals(8, resultSet.getShort(2));
      assertEquals(1, resultSet.getShort(3));
      assertTrue(resultSet.next());
      assertEquals(13, resultSet.getShort("h1"));
      assertEquals(45, resultSet.getShort("m1"));
      assertEquals(0, resultSet.getShort("d1"));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // rnovais@570_2: ABS.
      assertNotNull(resultSet = driver.executeQuery("Select amount, abs(amount) as a1 from person where abs(amount)>13"));
      assertEquals(2, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(-20, resultSet.getInt(1));
      assertEquals(20, resultSet.getInt(2));
      assertTrue(resultSet.next());
      assertEquals(-15, resultSet.getInt(1));
      assertEquals(15, resultSet.getInt("a1"));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // rnovais@570_2: UPPER, LOWER.
      assertNotNull(resultSet = driver.executeQuery("Select amount, abs(amount) as a1, name, lower(name) as u1, upper(name) as u2  from person where" 
                                                                                   + " abs(amount)>12 and UPPER(name) > 'INDIRA GOMES'"));
      assertEquals(2, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(-20, resultSet.getInt(1));
      assertEquals(20, resultSet.getInt(2));
      assertEquals("Lucas Novais", resultSet.getString(3));
      assertEquals("lucas novais", resultSet.getString(4));
      assertEquals("LUCAS NOVAIS",resultSet.getString(5));
      assertEquals("-20\t20\tLucas Novais\tlucas novais\tLUCAS NOVAIS", resultSet.rowToString());
      assertTrue(resultSet.next());
      assertEquals(-15, resultSet.getInt(1));
      assertEquals(15, resultSet.getInt("a1"));
      assertEquals("Zenes Lima", resultSet.getString(3));
      assertEquals("zenes lima", resultSet.getString("u1"));
      assertEquals("ZENES LIMA", resultSet.getString("u2"));
      assertEquals("-15\t15\tZenes Lima\tzenes lima\tZENES LIMA", resultSet.rowToString());
      assertFalse(resultSet.next());
      
      // Tests functions with getStrings().
      assertTrue(resultSet.first());
      assertEquals(2, (strings = resultSet.getStrings()).length);
      assertEquals("Lucas Novais", strings[0][2]);
      assertEquals("lucas novais", strings[0][3]);
      assertEquals("LUCAS NOVAIS", strings[0][4]);
      assertEquals("Zenes Lima", strings[1][2]);
      assertEquals("zenes lima", strings[1][3]);
      assertEquals("ZENES LIMA", strings[1][4]);
      resultSet.close();
      
      // rnovais@570_6
      assertNotNull(resultSet = driver.executeQuery("Select abs(amount) as a0, abs(amount1) as a1, abs(amount2) as a2, abs(amount3) as a3, " 
                                                                     + "abs(amount4) as a4 from person where abs(amount)>13"));
      assertEquals(2, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(20, resultSet.getInt(1));
      assertEquals(456, resultSet.getShort(2));
      assertEquals(48, resultSet.getLong(3));
      assertEquals(5.9, resultSet.getFloat(4), 1e-3);
      assertEquals(954.2, resultSet.getDouble(5), 1e-3);
      assertTrue(resultSet.next());
      assertEquals(15, resultSet.getInt("a0"));
      assertEquals(54, resultSet.getShort("a1"));
      assertEquals(5698, resultSet.getLong("a2"));
      assertEquals(8.30, resultSet.getFloat("a3"), 1e-3);
      assertEquals(456.5, resultSet.getDouble("a4"), 1e-3);
      assertFalse(resultSet.next());
      resultSet.close();
      
      // rnovais@570_108
      assertNotNull(resultSet = driver.executeQuery("Select amount from person where month(birth) = 7 and year(birth) = 2006 and day(birth) != 9 and" 
                                                                                                                         + " hour(years) = 0"));
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(13, resultSet.getInt(1));
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertNotNull(resultSet = driver.executeQuery("Select amount from person where month(birth) < 5 and birth = '2008/4/6'"));
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(-20, resultSet.getInt(1));
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertNotNull(resultSet = driver.executeQuery("Select upper(name) as n1 from person where month(birth) >= 5 and birth != '2005/9/12'"));
      assertEquals(2, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals("RENATO NOVAIS", resultSet.getString(1));
      assertTrue(resultSet.next());
      assertEquals("INDIRA GOMES", resultSet.getString("n1"));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // rnovais@112_1
      assertNotNull(resultSet = driver.executeQuery("Select amount from person where lower(name) >= 'zenes lima'"));
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(-15, resultSet.getInt(1));
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests functions that aren't compatible with a data type.
      output("function that isn't compatible with data type: millis x date");
      try
      {
         driver.executeQuery("Select  millis(birth) as mil, years from person");
         fail("9");
      }
      catch (SQLParseException exception) {}
      output("function that isn't compatible with data type: second x date");
      try
      {
         driver.executeQuery("Select  year(birth) as y1, month(birth) as m1, day(birth) as d1 from person where second(birth) = 234");
         fail("10");
      }
      catch (SQLParseException exception) {}
      output("function that isn't compatible with data type: minute x date");
      try
      {
         driver.executeQuery("Select age from person where hour(birth) = 12");
         fail("11");
      }
      catch (SQLParseException exception) {}
      output("function that isn't compatible with data type: hour x date");
      try
      {
         driver.executeQuery("Select age from person where hour(birth) = 12");
         fail("12");
      }
      catch (SQLParseException exception) {}
      output("function that isn't compatible with data type: upper x date"); // rnovais@570_2
      try
      {
         driver.executeQuery("Select  upper(birth) as y1 from person");
         fail("13");
      }
      catch (SQLParseException exception) {}
      output("function that isn't compatible with data type: abs x char"); // rnovais@570_2
      try
      {
         driver.executeQuery("Select  abs(name) as y1 from person");
         fail("14");
      }
      catch (SQLParseException exception) {}
      
      try // There's no alias.
      {
         driver.executeQuery("Select  month(birth) from person ");
         fail("15");
      }
      catch (SQLParseException exception) {}
        
      driver.closeAll();
   }
}