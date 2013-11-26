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

   /*
    * public void readOnly() throws SQLException { // set read only mode Hashtable config = new Hashtable(10);
    * config.setReadOnly(true); Connection conn = DriverManager.getConnection("jdbc:sqlite:", config.toProperties());
    * Statement stat = conn.createStatement(); try { assertTrue(conn.isReadOnly()); // these updates must be forbidden
    * in read-only mode stat.executeUpdate("create table A(id, name)");
    * stat.executeUpdate("insert into A values(1, 'leo')"); fail("read only flag is not properly set"); } catch
    * (SQLException e) { // success } finally { stat.close(); conn.close(); } config.setReadOnly(true); // should be a
    * no-op try { conn.setReadOnly(false); fail("should not change read only flag after opening connection"); } catch
    * (SQLException e) { assertEquals(e.getMessage(),"Cannot change read-only flag after establishing a connection.");
    * // was: contains } finally { conn.close(); } } public void foreignKeys() throws SQLException { SQLiteConfig config
    * = new SQLiteConfig(); config.enforceForeignKeys(true); Connection conn =
    * DriverManager.getConnection("jdbc:sqlite:", config.toProperties()); Statement stat = conn.createStatement(); try {
    * stat
    * .executeUpdate("create table track(id integer primary key, name, aid, foreign key (aid) references artist(id))");
    * stat.executeUpdate("create table artist(id integer primary key, name)");
    * stat.executeUpdate("insert into artist values(10, 'leo')");
    * stat.executeUpdate("insert into track values(1, 'first track', 10)"); // OK try {
    * stat.executeUpdate("insert into track values(2, 'second track', 3)"); // invalid reference } catch (SQLException
    * e) { return; // successfully detect violation of foreign key constraints }
    * fail("foreign key constraint must be enforced"); } finally { stat.close(); conn.close(); } } public void
    * canWrite() throws SQLException { SQLiteConfig config = new SQLiteConfig(); config.enforceForeignKeys(true);
    * Connection conn = DriverManager.getConnection("jdbc:sqlite:", config.toProperties()); Statement stat =
    * conn.createStatement(); try { assertFalse(conn.isReadOnly()); } finally { stat.close(); conn.close(); } } public
    * void synchronous() throws SQLException { SQLiteConfig config = new SQLiteConfig();
    * config.setSynchronous(SynchronousMode.OFF); Connection conn = DriverManager.getConnection("jdbc:sqlite:",
    * config.toProperties()); Statement stat = conn.createStatement(); try { ResultSet rs =
    * stat.executeQuery("pragma synchronous"); if (rs.next()) { ResultSetMetaData rm = rs.getMetaData(); int i =
    * rm.getColumnCount(); int synchronous = rs.getInt(1); assertEquals(0, synchronous); } } finally { stat.close();
    * conn.close(); } }
    */

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

/*         Connection conn3 = DriverManager.getConnection("jdbc:sqlite:file::memory:?cache=shared");
         Statement stmt3 = conn3.createStatement();
         stmt3.executeUpdate("attach 'file:memdb1?mode=memory&cache=shared' as memdb1");
         rs = stmt3.executeQuery("select * from memdb1");
         assertTrue(rs.next());
         assertEquals(100, rs.getInt(1));
*/
         try {stmt1.executeUpdate("drop table tbl2");} catch (Exception e) {}
         stmt1.executeUpdate("create table tbl2(col int)");
         stmt1.executeUpdate("insert into tbl2 values(200)");
         stmt1.close();

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
      openMemory();
      isClosed();
      closeTest();
      openInvalidLocation();
      URIFilenames();
   }
}
