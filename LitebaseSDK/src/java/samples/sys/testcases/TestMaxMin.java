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
      connection.closeAll();
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
                                                         + "from person where name = 'name0' and cpf = 'cpf1999'")).getRowCount());
      assertTrue(resultSet.next());
      assertEquals(resultSet.getString("maxn"), "name0");
      assertEquals(resultSet.getString("minn"), "name0");
      assertEquals(resultSet.getString("maxc"), "cpf1999");
      assertEquals(resultSet.getString("minc"), "cpf1999");
      resultSet.close();
   }
}
