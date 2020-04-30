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
import totalcross.sys.Time;
import totalcross.unit.TestCase;

/**
 * Tests max and min aggregation functions with and without indices.
 */
public class TestMaxMin extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      LitebaseConnection connection = LitebaseConnection.getInstance("Test");
      
      if (connection.exists("person"))
         connection.executeUpdate("drop table person");
      connection.execute("create table person (name char(10), cpf char(10), birth datetime, id long)");
      PreparedStatement ps = connection.prepareStatement("insert into person values (?, ?, ?, ?)");
            
      testEmptyTable(connection);
      
      // Table without repetitions. 
      int i = 2000;
      Time time = new Time(2011, 7, 19, 0, 0, 0, 0);
      
      connection.setRowInc("person", 2000);
      while (--i >= 0)
      {
         ps.setString(0, "name" + i);
         ps.setString(1, "cpf" + (1999 - i));
         ps.setDateTime(2, time);
         ps.setLong(3, i);
         ps.executeUpdate();
         time.inc(0, 0, 1);
      }
      connection.setRowInc("person", -1);
      executeAllTests(connection);
      
      // Table with repetitions. 
      i = 2000;
      time.inc(0, 0, -2000);
      connection.setRowInc("person", 2000);
      while (--i >= 0)
      {
         ps.setString(0, "name" + i);
         ps.setString(1, "cpf" + (1999 - i));
         ps.setLong(3, i);
         ps.setDateTime(2, time);
         ps.executeUpdate();
         ps.setDateTime(2, (Time)null);
         ps.executeUpdate();
         time.inc(0, 0, 1);
      }
      connection.setRowInc("person", -1);
      executeAllTests(connection);
      connection.closeAll();
   }
   
   /**
    * Test with empty or all rows deleted.
    * 
    * @param connection The connection with Litebase.
    */
   private void testEmptyMaxMin(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc from person");
      assertEquals(0, resultSet.getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name > 'name0' and cpf > 'cpf0'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name > 'name0' and cpf < 'cpf999'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name < 'name999' and cpf > 'cpf0'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name0' and cpf = 'cpf1999'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name1999' and cpf = 'cpf0'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select count(*) as c, avg(rowid) as a, max(name) as maxn, min(name) as minn, " 
                                                         + "max(cpf) as maxc, min(cpf) as minc from person")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests datetime. 
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth is null")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth is not null")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth > '2011/07/19'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth < '2011/07/20'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth like '2011/07/19%'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth like '2011/07/18%'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(id) as maxi, min(id) as mini from person")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   /**
    * Does all tests with empty or all rows deleted.
    * 
    * @param connection The connection with Litebase.
    */
   private void testEmptyTable(LitebaseConnection connection)
   {
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (name)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (cpf)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (birth)");
      testEmptyMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (name, cpf)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (cpf, name)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (birth, rowid)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (id)");
      testEmptyMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testEmptyMaxMin(connection);
   }
   
   /**
    * Tests max or min with a populated table.
    * 
    * @param connection The connection with Litebase.
    */
   private void testMaxMin(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc from person");
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf0");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name > 'name0' and cpf > 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name > 'name0' and cpf < 'cpf999'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf0");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name < 'name999' and cpf > 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name0' and cpf = 'cpf1999'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name0");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf1999");
      assertEquals(resultSet.getString("minc"), "cpf1999");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name = 'name1999' and cpf = 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name1999");
      assertEquals(resultSet.getString("minn"), "name1999");
      assertEquals(resultSet.getString("maxc"), "cpf0");
      assertEquals(resultSet.getString("minc"), "cpf0");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select count(*) as c, avg(rowid) as a, max(name) as maxn, min(name) as minn, " 
                                                         + "max(cpf) as maxc, min(cpf) as minc from person")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf0");
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Empty tables should not return rows.
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests datetime. 
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:19") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:00") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertLower((resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth is null")).getRowCount(), 2);
      if (resultSet.next())
      {
         assertNull(resultSet.getDateTime(1));
         assertNull(resultSet.getDateTime(2));
      }
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth is not null")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:19") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:00") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth > '2011/07/19'")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:19") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:01") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth < '2011/07/20'")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:19") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:00") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth like '2011/07/19%'")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:19") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:00") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth like '2011/07/18%'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(id) as maxi, min(id) as mini from person")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(1999, resultSet.getLong(1));
      assertEquals(0, resultSet.getLong(2));
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   /**
    * Tests max or min with some deletes.
    * 
    * @param connection The connection with Litebase.
    */
   private void testMaxMinWithDelete(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc from person");
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name > 'name0' and cpf > 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name > 'name0' and cpf < 'cpf999'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name < 'name999' and cpf > 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name1' and cpf = 'cpf1998'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name1");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf1998");
      assertEquals(resultSet.getString("minc"), "cpf1998");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name = 'name1998' and cpf = 'cpf1'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name1998");
      assertEquals(resultSet.getString("minn"), "name1998");
      assertEquals(resultSet.getString("maxc"), "cpf1");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select count(*) as c, avg(rowid) as a, max(name) as maxn, min(name) as minn, " 
                                                         + "max(cpf) as maxc, min(cpf) as minc from person")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf1");
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Empty tables should not return rows.
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name999' or cpf = 'cpf0'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      // Tests datetime. 
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:18") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:01") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertLower((resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth is null")).getRowCount(), 2);
      if (resultSet.next())
      {
         assertNull(resultSet.getDateTime(1));
         assertNull(resultSet.getDateTime(2));
      }
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth is not null")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:18") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:01") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth > '2011/07/19'")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:18") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:01") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth < '2011/07/20'")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:18") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:01") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth like '2011/07/19%'")).getRowCount());
      assertTrue(resultSet.next());
      assertTrue(resultSet.getDateTime(1).toString().indexOf(":33:18") >= 0);
      assertTrue(resultSet.getDateTime(2).toString().indexOf(":00:01") >= 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select max(birth) as maxb, min(birth) as mimb from person where birth like '2011/07/18%'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(id) as maxi, min(id) as mini from person")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(1998, resultSet.getLong(1));
      assertEquals(1, resultSet.getLong(2));
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   /**
    * Execute all tests for the created tables.
    * 
    * @param connection The connection with Litebase.
    */
   private void executeAllTests(LitebaseConnection connection)
   {
      testMaxMin(connection);
      connection.execute("create index idx on person (name)");
      testMaxMin(connection);
      connection.execute("create index idx on person (cpf)");
      testMaxMin(connection);
      connection.execute("create index idx on person (birth)");
      testMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testMaxMin(connection);
      connection.execute("create index idx on person (name, cpf)");
      testMaxMin(connection);
      connection.execute("create index idx on person (cpf, name)");
      testMaxMin(connection);
      connection.execute("create index idx on person (birth, rowid)");
      testMaxMin(connection);
      connection.execute("create index idx on person (id)");
      testMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testMaxMin(connection);
      
      connection.executeUpdate("delete from person where name = 'name0' or cpf = 'cpf0' or name = 'name999' or cpf = 'cpf999'");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (name)");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (cpf)");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (birth)");
      testMaxMinWithDelete(connection);
      connection.executeUpdate("drop index * on person");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (name, cpf)");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (cpf, name)");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (birth, rowid)");
      testMaxMinWithDelete(connection);
      connection.execute("create index idx on person (id)");
      testMaxMinWithDelete(connection);
      connection.executeUpdate("drop index * on person");
      testMaxMinWithDelete(connection);
      
      connection.executeUpdate("delete person where name > 'name0'");
      testEmptyTable(connection);
      connection.purge("person");
      testEmptyTable(connection);
   }
}

