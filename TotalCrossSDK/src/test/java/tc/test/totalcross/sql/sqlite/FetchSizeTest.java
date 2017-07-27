package tc.test.totalcross.sql.sqlite;

import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.unit.TestCase;

public class FetchSizeTest extends TestCase
{
  public void testFetchSize()
  {
    try
    {
      Connection conn = DriverManager.getConnection("jdbc:sqlite:");
      try {conn.createStatement().execute("drop table s1");} catch (Exception e) {}
      assertEquals(conn.prepareStatement("create table s1 (c1)").executeUpdate(), 0);
      PreparedStatement insertPrep = conn.prepareStatement("insert into s1 values (?)");
      insertPrep.setInt(1, 1);
      assertEquals(insertPrep.executeUpdate(), 1);
      insertPrep.setInt(1, 2);
      assertEquals(insertPrep.executeUpdate(), 1);
      insertPrep.setInt(1, 3);
      assertEquals(insertPrep.executeUpdate(), 1);
      insertPrep.setInt(1, 4);
      assertEquals(insertPrep.executeUpdate(), 1);
      insertPrep.setInt(1, 5);
      assertEquals(insertPrep.executeUpdate(), 1);
      insertPrep.close();

      PreparedStatement selectPrep = conn.prepareStatement("select c1 from s1");
      ResultSet rs = selectPrep.executeQuery();
      rs.setFetchSize(2);
      assertTrue(rs.next());
      assertTrue(rs.next());
      assertTrue(rs.next());
      assertTrue(rs.next());
      assertTrue(rs.next());
      assertFalse(rs.next());
      conn.close();
    }
    catch (Exception e)
    {
      fail(e);
    }
  }

  @Override
  public void testRun()
  {
    testFetchSize();
  }
}
