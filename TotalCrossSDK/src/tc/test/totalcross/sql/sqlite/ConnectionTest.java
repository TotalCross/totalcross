package tc.test.totalcross.sql.sqlite;

import java.sql.SQLException;

import totalcross.io.*;
import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.unit.*;

/**
 * These tests check whether access to files is woring correctly and some Connection.close() cases.
 */
public class ConnectionTest extends TestCase
{
   public void executeUpdateOnClosedDB()
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");
         Statement stat = conn.createStatement();
         conn.close();
         stat.executeUpdate("create table A(id, name)");
      }
      catch (SQLException e)
      {
         return; // successfully detect the operation on the closed DB
      }
      fail("should not reach here");
   }

   public void openMemory()
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");
         conn.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void isClosed()
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");
         conn.close();
         assertTrue(conn.isClosed());
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void closeTest()
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");
         PreparedStatement prep = conn.prepareStatement("select null;");
         prep.executeQuery();
         conn.close();
         prep.clearParameters();
         fail("should raise exception");
      }
      catch (Exception e)
      {
      }
   }

   public void openInvalidLocation()
   {
      try
      {
         Connection conn = DriverManager.getConnection("jdbc:sqlite:/");
         conn.close();
         fail("should raise exception");
      }
      catch (Exception e)
      {
      }
   }

   public void openFile() throws Exception
   {
      File testDB = copyToTemp("J:\\_SWDevelop\\gitrepo\\TotalCross\\TotalCrossSDK\\src\\tc\\test\\totalcross\\sql\\sqlite\\", "sample.db");
      new TempFile(testDB);
      assertTrue(testDB.exists());
      Connection conn = DriverManager.getConnection("jdbc:sqlite:" + testDB);
      conn.close();
   }

   public static File copyToTemp(String folder, String fileName) throws IOException
   {
      String sdir = folder + "/target/";
      File dir = new File(sdir);
      if (!dir.exists())
         dir.createDir();

      File tmp = new File(sdir + fileName, File.CREATE_EMPTY);
      File in = new File(folder + fileName, File.READ_ONLY);
      in.copyTo(tmp);
      in.close();

      return tmp;
   }

   public void URIFilenames()
   {
      try
      {
         Connection conn1 = DriverManager.getConnection("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
         Statement stmt1 = conn1.createStatement();
         try {stmt1.executeUpdate("drop table tbl");} catch (Exception e) {}
         stmt1.executeUpdate("create table tbl (col int)");
         stmt1.executeUpdate("insert into tbl values(100)");
         stmt1.close();

         Connection conn2 = DriverManager.getConnection("jdbc:sqlite:file:memdb1?mode=memory&cache=shared");
         Statement stmt2 = conn2.createStatement();
         ResultSet rs = stmt2.executeQuery("select * from tbl");
         assertTrue(rs.next());
         assertEquals(100, rs.getInt(1));
         stmt2.close();

         Connection conn3 = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
         Statement stmt3 = conn3.createStatement();
         stmt3.executeUpdate("attach 'file:memdb1?mode=memory&cache=shared' as memdb1");
         rs = stmt3.executeQuery("select * from memdb1.tbl");
         assertTrue(rs.next());
         assertEquals(100, rs.getInt(1));
         stmt3.executeUpdate("create table tbl2(col int)");
         stmt3.executeUpdate("insert into tbl2 values(200)");
         stmt3.close();

         Connection conn4 = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
         Statement stmt4 = conn4.createStatement();
         rs = stmt4.executeQuery("select * from tbl2");
         assertTrue(rs.next());
         assertEquals(200, rs.getInt(1));
         rs.close();
         stmt4.close();
         conn4.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testRun()
   {
      URIFilenames();
      openMemory();
      isClosed();
      closeTest();
      openInvalidLocation();
   }
}
