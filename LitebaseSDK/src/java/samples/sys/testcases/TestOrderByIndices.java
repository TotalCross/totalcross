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
 * Does tests with order by and group by queries using indices or not.
 */
public class TestOrderByIndices extends TestCase
{
   /**
    * Main test method.
    */
   public void testRun()
   {
      LitebaseConnection connection = LitebaseConnection.getInstance("Test");
      
      if (connection.exists("person"))
         connection.executeUpdate("drop table person");
      connection.execute("create table person (name char(10), cpf char(10), birth datetime)");
      PreparedStatement ps = connection.prepareStatement("insert into person values (?, ?, ?)");
            
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
         ps.executeUpdate();
         time.inc(0, 0, 1);
      }
      connection.setRowInc("person", -1);
      executeAllTests(connection);
      
      // Table with repetitions. 
      connection.executeUpdate("drop table person");
      connection.execute("create table person (name char(10) not null, cpf char(10) not null, birth datetime not null)");
      ps = connection.prepareStatement("insert into person values (?, ?, ?)");
      i = 2000;
      time.inc(0, 0, -2000);
      connection.setRowInc("person", 2000);
      while (--i >= 0)
      {
         ps.setString(0, "name" + i);
         ps.setString(1, "cpf" + (1999 - i));
         ps.setDateTime(2, time);
         ps.executeUpdate();
         ps.executeUpdate();
         time.inc(0, 0, 1);
      }
      connection.setRowInc("person", -1);
      executeAllTestsWithRep(connection);
      connection.closeAll();
   }
   
   /**
    * Test with empty or all rows deleted.
    * 
    * @param connection The connection with Litebase.
    */
   private void testEmptyOrderBy(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select name from person order by name");
      assertEquals(0, resultSet.getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select cpf from person order by cpf")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select birth from person order by birth")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
            
      assertEquals(0, (resultSet = connection.executeQuery("select name, cpf from person order by name, cpf")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select cpf, name from person order by cpf, name")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select birth, rowid from person order by birth, rowid")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select rowid, birth from person order by rowid, birth")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select name from person where name = 'name0' group by name")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select cpf from person where cpf > 'cpf0' group by cpf")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select birth from person where birth != '2011/07/19' group by birth")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery(
                                   "select name, cpf from person where name = 'name0' and cpf = 'cpf1999' group by name, cpf")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery(
                                   "select name, cpf from person where name = 'name0' or cpf = 'cpf0' group by cpf, name")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery(
                                   "select birth, rowid from person where birth like '2011/0%' group by birth, rowid")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals(0, (resultSet = connection.executeQuery("select birth, rowid from person where rowid > 0 group by rowid, birth")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   /**
    * Tests order by and group by with a populated table.
    * 
    * @param connection The connection with Litebase.
    */
   private void testOrderBy(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select name from person order by name");
      int count = resultSet.getRowCount();
      assertTrue(count == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select cpf from person order by cpf")).getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth from person order by birth")).getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
            
      assertTrue((count = (resultSet = connection.executeQuery("select name, cpf from person order by name, cpf"))
                                                 .getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select cpf, name from person order by cpf, name"))
                                                 .getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth, rowid from person order by birth, rowid"))
                                                 .getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select rowid, birth from person order by rowid, birth"))
                                                 .getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select name from person where name = 'name0' group by name")).getRowCount(), 1);
      testSortAsc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select cpf from person where cpf > 'cpf0' group by cpf")).getRowCount(), 1999);
      testSortAsc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select birth from person where birth != '2011/07/19' group by birth"))
                                          .getRowCount(), 1999);
      testSortAsc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select name, cpf from person where name = 'name0' and cpf = 'cpf1999' group by name, cpf"))
                                          .getRowCount(), 1);
      testSortAsc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select cpf, name from person where name = 'name0' or cpf = 'cpf0' group by cpf, name"))
                                          .getRowCount(), 2);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth, rowid from person where birth like '2011/0%' group by birth, rowid"))
                                                 .getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth, rowid from person where rowid > 0 group by rowid, birth"))
                                                 .getRowCount()) == 2000 || count == 4000);
      testSortAsc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select rowid from person where rowid = 0 group by rowid"))
                                          .getRowCount(), 0);
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   /**
    * Tests order by and group by with some deletes.
    * 
    * @param connection The connection with Litebase.
    */
   private void testOrderByWithDelete(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select name from person order by name desc");
      int count = resultSet.getRowCount();
      assertTrue(count == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select cpf from person order by cpf desc"))
                                                 .getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth from person order by birth desc"))
                                                 .getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
            
      assertTrue((count = (resultSet = connection.executeQuery("select name, cpf from person order by name desc, cpf desc"))
                                                 .getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select cpf, name from person order by cpf desc, name desc"))
                                                 .getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth, rowid from person order by birth desc, rowid desc"))
                                                 .getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select rowid, birth from person order by rowid desc, birth desc"))
                                                 .getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select name from person where name = 'name1' group by name order by name desc"))
                                          .getRowCount(), 1);
      testSortDesc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select cpf from person where cpf > 'cpf1' group by cpf order by cpf desc"))
                                          .getRowCount(), 1995);
      testSortDesc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select birth from person where birth != '2011/07/19' group by birth order by birth desc"))
                                          .getRowCount(), 1996);
      testSortDesc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery(
      "select name, cpf from person where name = 'name0' and cpf = 'cpf1999' group by name, cpf order by name desc, cpf desc")).getRowCount(), 0);
      assertFalse(resultSet.next());
      resultSet.close();
      
      assertEquals((resultSet = connection.executeQuery(
      "select cpf, name from person where name = 'name1' or cpf = 'cpf1' group by cpf, name order by cpf desc, name desc")).getRowCount(), 2);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth, rowid from person where birth like '2011/0%' group by birth, rowid " 
                                                             + "order by birth desc, rowid desc")).getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertTrue((count = (resultSet = connection.executeQuery("select birth, rowid from person where rowid > 0 group by rowid, birth " + 
                                                               "order by rowid desc, birth desc")).getRowCount()) == 1996 || count == 3992);
      testSortDesc(resultSet);
      
      assertEquals((resultSet = connection.executeQuery("select rowid from person where rowid = 1 group by rowid order by rowid desc"))
                                          .getRowCount(), 0);
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
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person add primary key (name)");
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (cpf)");
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (birth)");
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (name, cpf)");
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (cpf, name)");
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (birth, rowid)");
      testEmptyOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      testEmptyOrderBy(connection);
   }
   
   /**
    * Execute all tests for the created table without repetition.
    * 
    * @param connection The connection with Litebase.
    */
   private void executeAllTests(LitebaseConnection connection)
   {
      testOrderBy(connection);
      connection.executeUpdate("alter table person add primary key (name)");
      testOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (cpf)");
      testOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (birth)");
      testOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (name, cpf)");
      testOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (cpf, name)");
      testOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (birth, rowid)");
      testOrderBy(connection);
      connection.executeUpdate("alter table person drop primary key");
      testOrderBy(connection);
      
      connection.executeUpdate("delete from person where name = 'name0' or cpf = 'cpf0' or name = 'name999' or cpf = 'cpf999'");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person add primary key (name)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (cpf)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (birth)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (name, cpf)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (cpf, name)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person drop primary key");
      connection.executeUpdate("alter table person add primary key (birth, rowid)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("alter table person drop primary key");
      testOrderByWithDelete(connection);
      
      connection.executeUpdate("delete person where name > 'name0'");
      testEmptyTable(connection);
      connection.purge("person");
      testEmptyTable(connection);
   }
   
   /**
    * Execute all tests for the created table with repetitions.
    * 
    * @param connection The connection with Litebase.
    */
   private void executeAllTestsWithRep(LitebaseConnection connection)
   {
      testOrderBy(connection);
      connection.execute("create index idx on person(name)");
      testOrderBy(connection);
      connection.executeUpdate("drop index name on person");
      connection.execute("create index idx on person(cpf)");
      testOrderBy(connection);
      connection.executeUpdate("drop index cpf on person");
      connection.execute("create index idx on person(birth)");
      testOrderBy(connection);
      connection.executeUpdate("drop index birth on person");
      connection.execute("create index idx on person(name, cpf)");
      testOrderBy(connection);
      connection.executeUpdate("drop index name, cpf on person");
      connection.execute("create index idx on person(cpf, name)");
      testOrderBy(connection);
      connection.executeUpdate("drop index cpf, name on person");
      connection.execute("create index idx on person(birth, rowid)");
      testOrderBy(connection);
      connection.executeUpdate("drop index birth, rowid on person");
      testOrderBy(connection);
      
      connection.executeUpdate("delete from person where name = 'name0' or cpf = 'cpf0' or name = 'name999' or cpf = 'cpf999'");
      testOrderByWithDelete(connection);
      connection.execute("create index idx on person(name)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("drop index name on person");
      connection.execute("create index idx on person(cpf)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("drop index cpf on person");
      connection.execute("create index idx on person(birth)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("drop index birth on person");
      connection.execute("create index idx on person(name, cpf)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("drop index name, cpf on person");
      connection.execute("create index idx on person(cpf, name)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("drop index cpf, name on person");
      connection.execute("create index idx on person(birth, rowid)");
      testOrderByWithDelete(connection);
      connection.executeUpdate("drop index birth, rowid on person");
      testOrderByWithDelete(connection);
      
      connection.executeUpdate("delete person where name > 'name0'");
      testEmptyTable(connection);
      connection.purge("person");
      testEmptyTable(connection);
   }
   
   /**
    * Tests ascending sort results.
    * 
    * @param resultSet The result of ascending sorts. 
    */
   private void testSortAsc(ResultSet resultSet)
   {
      resultSet.next();
      
      ResultSetMetaData rsMD = resultSet.getResultSetMetaData();
      
      if (rsMD.getColumnType(1) == ResultSetMetaData.INT_TYPE)
      {
         int first = resultSet.getInt(1),
             last;
         while (resultSet.next())
         {
            last = resultSet.getInt(1);
            assertLowerOrEqual(first, last);
            first = last;
         }
      }
      else
      {
         String first = resultSet.getString(1),
                last;
         while (resultSet.next())
         {
            last = resultSet.getString(1);
            assertLowerOrEqual(first.compareTo(last), 0);
            first = last;
         }
      }
      resultSet.close();
   }
   
   /**
    * Tests descending sort results.
    * 
    * @param resultSet The result of descending sorts. 
    */
   private void testSortDesc(ResultSet resultSet)
   {
      resultSet.next();
      
      ResultSetMetaData rsMD = resultSet.getResultSetMetaData();
      
      if (rsMD.getColumnType(1) == ResultSetMetaData.INT_TYPE)
      {
         int first = resultSet.getInt(1),
             last;
         while (resultSet.next())
         {
            last = resultSet.getInt(1);
            assertGreaterOrEqual(first, last);
            first = last;
         }
      }
      else
      {
         String first = resultSet.getString(1),
                last;
         while (resultSet.next())
         {
            last = resultSet.getString(1);
            assertGreaterOrEqual(first.compareTo(last), 0);
            first = last;
         }
      }
      resultSet.close();
   }
}
