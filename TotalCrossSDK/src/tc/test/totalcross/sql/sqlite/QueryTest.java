package tc.test.totalcross.sql.sqlite;

import java.sql.SQLException;

import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.*;
import totalcross.unit.*;
import totalcross.util.Date;

public class QueryTest extends TestCase
{
   public Connection getConnection() throws Exception 
   {
      return DriverManager.getConnection("jdbc:sqlite::memory:");
   }

   public void createTable()
   {
      try
      {
         Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         stmt.execute("CREATE TABLE IF NOT EXISTS sample " + "(id INTEGER PRIMARY KEY, descr VARCHAR(40))");
         stmt.close();

         stmt = conn.createStatement();
         try
         {
            ResultSet rs = stmt.executeQuery("SELECT * FROM sample");
            rs.next();
         }
         catch (Exception e)
         {
            fail(e);
         }

         conn.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void setDoubleTest() 
   {
      try
      {
         double f = 3.141597;
         Connection conn = getConnection();

         try {conn.createStatement().execute("drop table sample");} catch (Exception e) {}
         conn.createStatement().execute("create table sample (data NOAFFINITY)");
         PreparedStatement prep = conn.prepareStatement("insert into sample values(?)");
         prep.setDouble(1, f);
         prep.executeUpdate();

         PreparedStatement stmt = conn.prepareStatement("select * from sample where data > ?");
         stmt.setObject(1, new Double(3.0f));
         ResultSet rs = stmt.executeQuery();
         assertTrue(rs.next());
         double f2 = rs.getDouble(1);
         assertEquals(f, f2, 0.0000001);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void dateTimeTest() 
   {
      try
      {
         Connection conn = getConnection();

         totalcross.sys.Vm.gc();
         try {conn.createStatement().execute("drop table sample");} catch (Exception e) {}
         conn.createStatement().execute("create table sample (start_time datetime)");

         Date now = new Date();

         conn.createStatement().execute("insert into sample values(" + (Settings.onJavaSE ? now.getTime() : now.getSQLLong()) + ")");
         conn.createStatement().execute("insert into sample values('" + now.getSQLLong() + "')");

         ResultSet rs = conn.createStatement().executeQuery("select * from sample");
         assertTrue(rs.next());
         assertEquals(now, rs.getDate(1));
         assertTrue(rs.next());
         assertEquals(now, rs.getDate(1)); 

         PreparedStatement stmt = conn.prepareStatement("insert into sample values(?)");
         stmt.setDate(1, new Date(now.getDateInt()));
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void viewTest() 
   {
      try
      {
         Connection conn = getConnection();
         Statement st1 = conn.createStatement();
         // drop table if it already exists

         String tableName = "sample";
         Vm.gc(); st1.execute("DROP TABLE IF EXISTS " + tableName);
         st1.close();
         Statement st2 = conn.createStatement();
         st2.execute("DROP VIEW IF EXISTS " + tableName);
         st2.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void timeoutTest() 
   {
      try
      {
         Connection conn = getConnection();
         Statement st1 = conn.createStatement();

         st1.setQueryTimeout(1);

         st1.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void concatTest()
   {

      Connection conn = null;
      try
      {
         // create a database connection
         conn = getConnection();
         Statement statement = conn.createStatement();
         //statement.setQueryTimeout(10); // set timeout to 10 sec.

         statement.executeUpdate("drop table if exists person");
         statement.executeUpdate("create table person (id integer, name string, shortname string)");
         statement.executeUpdate("insert into person values(1, 'leo','L')");
         statement.executeUpdate("insert into person values(2, 'yui','Y')");
         statement.executeUpdate("insert into person values(3, 'abc', null)");

         statement.executeUpdate("drop table if exists message");
         statement.executeUpdate("create table message (id integer, subject string)");
         statement.executeUpdate("insert into message values(1, 'Hello')");
         statement.executeUpdate("insert into message values(2, 'World')");

         statement.executeUpdate("drop table if exists mxp");
         statement.executeUpdate("create table mxp (pid integer, mid integer, type string)");
         statement.executeUpdate("insert into mxp values(1,1, 'F')");
         statement.executeUpdate("insert into mxp values(2,1,'T')");
         statement.executeUpdate("insert into mxp values(1,2, 'F')");
         statement.executeUpdate("insert into mxp values(2,2,'T')");
         statement.executeUpdate("insert into mxp values(3,2,'T')");

         ResultSet rs = statement
               .executeQuery("select group_concat(ifnull(shortname, name)) from mxp, person where mxp.mid=2 and mxp.pid=person.id and mxp.type='T'");
         while (rs.next())
         {
            // read the result set
            assertEquals("Y,abc", rs.getString(1));
         }
         rs = statement.executeQuery("select group_concat(ifnull(shortname, name)) from mxp, person where mxp.mid=1 and mxp.pid=person.id and mxp.type='T'");
         while (rs.next())
         {
            // read the result set
            assertEquals("Y", rs.getString(1));
         }

         PreparedStatement ps = conn
               .prepareStatement("select group_concat(ifnull(shortname, name)) from mxp, person where mxp.mid=? and mxp.pid=person.id and mxp.type='T'");
         ps.clearParameters();
         ps.setInt(1, 2);
         rs = ps.executeQuery();
         while (rs.next())
         {
            // read the result set
            assertEquals("Y,abc", rs.getString(1));
         }
         ps.clearParameters();
         ps.setInt(1, 2);
         rs = ps.executeQuery();
         while (rs.next())
         {
            // read the result set
            assertEquals("Y,abc", rs.getString(1));
         }

      }
      catch (Exception e)
      {
         fail(e);
      }
      finally
      {
         try
         {
            if (conn != null)
               conn.close();
         }
         catch (SQLException e)
         {
         }
      }

   }

   public void testRun()
   {
      status("createTable");  createTable();
      status("concatTest");   concatTest();
      status("setDoubleTest"); setDoubleTest();
      status("dateTimeTest"); dateTimeTest();
      status("viewTest");     viewTest();
      status("timeoutTest");  timeoutTest();
   }

}
