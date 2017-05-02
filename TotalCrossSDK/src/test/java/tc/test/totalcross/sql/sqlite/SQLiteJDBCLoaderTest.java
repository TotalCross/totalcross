package tc.test.totalcross.sql.sqlite;

import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.*;
import totalcross.unit.*;

public class SQLiteJDBCLoaderTest extends TestCase
{
   public void query()
   {
      try
      {
         Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
         Statement statement = connection.createStatement();
         statement.setQueryTimeout(30); // set timeout to 30 sec.

         Vm.gc(); statement.execute("DROP TABLE IF EXISTS person");
         statement.executeUpdate("create table person ( id integer, name string)");
         statement.executeUpdate("insert into person values(1, 'leo')");
         statement.executeUpdate("insert into person values(2, 'yui')");

         ResultSet rs = statement.executeQuery("select * from person order by id");
         while (rs.next())
         {
            // read the resultset
            rs.getInt(1);
            rs.getString(2);
         }
         connection.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testRun()
   {
      query();
   }
}
