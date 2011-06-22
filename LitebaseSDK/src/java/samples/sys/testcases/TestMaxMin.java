package samples.sys.testcases;

import litebase.*;
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
      connection.execute("create table person (name char(10), cpf char(10))");
      PreparedStatement ps = connection.prepareStatement("insert into person values (?, ?)");
            
      testEmptyTable(connection);
      
      int i = 2000;
      while (--i >= 0)
      {
         ps.setString(0, "name" + i);
         ps.setString(1, "cpf" + (1999 - i));
         ps.executeUpdate();
      }
      
      testMaxMin(connection);
      connection.execute("create index idx on person (name)");
      testMaxMin(connection);
      connection.execute("create index idx on person (cpf)");
      testMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testMaxMin(connection);
      connection.execute("create index idx on person (name, cpf)");
      testMaxMin(connection);
      connection.execute("create index idx on person (cpf, name)");
      testMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testMaxMin(connection);
      
      connection.executeUpdate("delete person");
      testEmptyTable(connection);
      connection.purge("person");
      testEmptyTable(connection);
      
      connection.closeAll();
   }
   
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
      
      // Empty tables should not return rows.
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name'")).getRowCount());
      assertFalse(resultSet.next());
      resultSet.close();
   }
   
   private void testEmptyTable(LitebaseConnection connection)
   {
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (name)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (cpf)");
      testEmptyMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (name, cpf)");
      testEmptyMaxMin(connection);
      connection.execute("create index idx on person (cpf, name)");
      testEmptyMaxMin(connection);
      connection.executeUpdate("drop index * on person");
      testEmptyMaxMin(connection);
   }
   
   private void testMaxMin(LitebaseConnection connection)
   {
      ResultSet resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc from person");
      assertEquals(1, resultSet.getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf0");
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name > 'name0' and cpf > 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf1");
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name > 'name0' and cpf < 'cpf999'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name1");
      assertEquals(resultSet.getString("maxc"), "cpf998");
      assertEquals(resultSet.getString("minc"), "cpf0");
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name < 'name999' and cpf > 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name998");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf1");
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name0' and cpf = 'cpf1999'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name0");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf1999");
      assertEquals(resultSet.getString("minc"), "cpf1999");
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
            + "from person where name = 'name1999' and cpf = 'cpf0'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name1999");
      assertEquals(resultSet.getString("minn"), "name1999");
      assertEquals(resultSet.getString("maxc"), "cpf0");
      assertEquals(resultSet.getString("minc"), "cpf0");
      resultSet.close();
      
      assertEquals(1, (resultSet = connection.executeQuery("select count(*) as c, avg(rowid) as a, max(name) as maxn, min(name) as minn, " 
                                                         + "max(cpf) as maxc, min(cpf) as minc from person")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name999");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf999");
      assertEquals(resultSet.getString("minc"), "cpf0");
      resultSet.close();
      
      // Empty tables should not return rows.
      assertEquals(0, (resultSet = connection.executeQuery("select max(name) as maxn, min(name) as minn, max(cpf) as maxc, min(cpf) as minc " 
                                                         + "from person where name = 'name'")).getRowCount());
      resultSet.close();
   }
}
