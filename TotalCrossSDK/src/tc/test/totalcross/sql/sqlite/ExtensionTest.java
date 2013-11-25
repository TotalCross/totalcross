package tc.test.totalcross.sql.sqlite;

import totalcross.sql.*;
import totalcross.unit.*;

public class ExtensionTest extends TestCase
{
   public void extFTS3() 
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");;
         Statement stat = conn.createStatement();
   
         stat.execute("create virtual table recipe using fts3(name, ingredients)");
         stat.execute("insert into recipe (name, ingredients) values('broccoli stew', 'broccoli peppers cheese tomatoes')");
         stat.execute("insert into recipe (name, ingredients) values('pumpkin stew', 'pumpkin onions garlic celery')");
   
         ResultSet rs = stat.executeQuery("select rowid, name, ingredients from recipe where ingredients match 'onions'");
         assertTrue(rs.next());
         assertEquals("pumpkin stew", rs.getString(2));
         stat.close();
         conn.close();
      } catch (Exception e) {fail(e);}
   }

   public void extFunctions() 
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");;
         Statement stat = conn.createStatement();

         ResultSet rs = stat.executeQuery("select cos(radians(45))");
         assertTrue(rs.next());
         assertEquals(0.707106781186548, rs.getDouble(1), 0.000000000000001);
         rs.close();

         rs = stat.executeQuery("select reverse(\"ACGT\")");
         assertTrue(rs.next());
         assertEquals("TGCA", rs.getString(1));
         rs.close();

         stat.close();
         conn.close();
      } catch (Exception e) {fail(e);}
   }

   public void testRun()
   {
      extFTS3();
      extFunctions();
   }

}
