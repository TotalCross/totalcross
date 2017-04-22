package tc.test.totalcross.sql.sqlite;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.unit.*;
import totalcross.util.*;

/** These tests are designed to stress Statements on memory databases. */
public class StatementTest extends TestCase
{
   private Connection conn;
   private Statement stat;

   public void connect() throws Exception
   {
      conn = DriverManager.getConnection("jdbc:sqlite:");
      stat = conn.createStatement();
   }

   public void close() throws Exception
   {
      stat.close();
      conn.close();
   }

   public void executeUpdate()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS s1");
         assertEquals(stat.executeUpdate("create table s1 (c1);"), 0);
         assertEquals(stat.executeUpdate("insert into s1 values (0);"), 1);
         assertEquals(stat.executeUpdate("insert into s1 values (1);"), 1);
         assertEquals(stat.executeUpdate("insert into s1 values (2);"), 1);
         assertEquals(stat.executeUpdate("update s1 set c1 = 5;"), 3);
         // count_changes_pgrama. truncate_optimization
         assertEquals(stat.executeUpdate("delete from s1;"), 3);

         // multiple SQL statements
         assertEquals(stat.executeUpdate("insert into s1 values (11);" + "insert into s1 values (12)"), 2);
         assertEquals(stat.executeUpdate("update s1 set c1 = 21 where c1 = 11;" + "update s1 set c1 = 22 where c1 = 12;"
               + "update s1 set c1 = 23 where c1 = 13"), 2); // c1 = 13 does not exist
         assertEquals(stat.executeUpdate("delete from s1 where c1 = 21;" + "delete from s1 where c1 = 22;" + "delete from s1 where c1 = 23"), 2);

         assertEquals(stat.executeUpdate("drop table s1;"), 0);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void emptyRS()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select null limit 0;");
         assertFalse(rs.next());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void singleRowRS()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select " + Integer.MAX_VALUE + ";");
         assertTrue(rs.next());
         assertEquals(rs.getInt(1), Integer.MAX_VALUE);
         assertEquals(rs.getString(1), Convert.toString(Integer.MAX_VALUE));
         assertEquals(rs.getDouble(1), new Integer(Integer.MAX_VALUE).intValue(), 0.001);
         assertFalse(rs.next());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void twoRowRS()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select 9 union all select 7;");
         assertTrue(rs.next());
         assertEquals(rs.getInt(1), 9);
         assertTrue(rs.next());
         assertEquals(rs.getInt(1), 7);
         assertFalse(rs.next());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void autoClose()
   {
      try
      {
         conn.createStatement().executeQuery("select 1;");
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void stringRS()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select \"Russell\";");
         assertTrue(rs.next());
         assertEquals(rs.getString(1), "Russell");
         assertFalse(rs.next());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void execute()
   {
      try
      {
         assertTrue(stat.execute("select null;"));
         ResultSet rs = stat.getResultSet();
         assertNotNull(rs);
         assertTrue(rs.next());
         assertNull(rs.getString(1));
         assertTrue(rs.wasNull());
         assertFalse(stat.getMoreResults());
         assertEquals(stat.getUpdateCount(), -1);

         assertTrue(stat.execute("select null;"));
         assertFalse(stat.getMoreResults());
         assertEquals(stat.getUpdateCount(), -1);

         Vm.gc(); stat.execute("DROP TABLE IF EXISTS test");
         assertFalse(stat.execute("create table test (c1);"));
         assertEquals(stat.getUpdateCount(), 0);
         assertFalse(stat.getMoreResults());
         assertEquals(stat.getUpdateCount(), -1);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void colNameAccess()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS tab");
         assertEquals(stat.executeUpdate("create table tab (id, firstname, surname);"), 0);
         assertEquals(stat.executeUpdate("insert into tab values (0, 'Bob', 'Builder');"), 1);
         assertEquals(stat.executeUpdate("insert into tab values (1, 'Fred', 'Blogs');"), 1);
         assertEquals(stat.executeUpdate("insert into tab values (2, 'John', 'Smith');"), 1);
         ResultSet rs = stat.executeQuery("select * from tab;");
         assertTrue(rs.next());
         assertEquals(rs.getInt("id"), 0);
         assertEquals(rs.getString("firstname"), "Bob");
         assertEquals(rs.getString("surname"), "Builder");
         assertTrue(rs.next());
         assertEquals(rs.getInt("id"), 1);
         assertEquals(rs.getString("firstname"), "Fred");
         assertEquals(rs.getString("surname"), "Blogs");
         assertTrue(rs.next());
         assertEquals(rs.getInt("id"), 2);
         assertEquals(rs.getString("id"), "2");
         assertEquals(rs.getString("firstname"), "John");
         assertEquals(rs.getString("surname"), "Smith");
         assertFalse(rs.next());
         rs.close();
         assertEquals(stat.executeUpdate("drop table tab;"), 0);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void nulls()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select null union all select null;");
         assertTrue(rs.next());
         assertNull(rs.getString(1));
         assertTrue(rs.wasNull());
         assertTrue(rs.next());
         assertNull(rs.getString(1));
         assertTrue(rs.wasNull());
         assertFalse(rs.next());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void tempTable()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS myTemp");
         assertEquals(stat.executeUpdate("create temp table myTemp (a);"), 0);
         assertEquals(stat.executeUpdate("insert into myTemp values (2);"), 1);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void insert1000()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS in1000");
         assertEquals(stat.executeUpdate("create table in1000 (a);"), 0);
         conn.setAutoCommit(false);
         for (int i = 0; i < 1000; i++)
            assertEquals(stat.executeUpdate("insert into in1000 values (" + i + ");"), 1);
         conn.commit();

         ResultSet rs = stat.executeQuery("select count(a) from in1000;");
         assertTrue(rs.next());
         assertEquals(rs.getInt(1), 1000);
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void batch()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS batch");
         stat.addBatch("create table batch (c1);");
         stat.addBatch("insert into batch values (1);");
         stat.addBatch("insert into batch values (1);");
         stat.addBatch("insert into batch values (2);");
         stat.addBatch("insert into batch values (3);");
         stat.addBatch("insert into batch values (4);");
         assertEquals(new int[] { 1, 1, 1, 1, 1, 1 }, stat.executeBatch());
         assertEquals(new int[] {}, stat.executeBatch());
         stat.clearBatch();
         stat.addBatch("insert into batch values (9);");
         assertEquals(new int[] { 1 }, stat.executeBatch());
         assertEquals(new int[] {}, stat.executeBatch());
         stat.clearBatch();
         stat.addBatch("insert into batch values (7);");
         stat.addBatch("insert into batch values (7);");
         assertEquals(new int[] { 1, 1 }, stat.executeBatch());
         stat.clearBatch();

         ResultSet rs = stat.executeQuery("select count(*) from batch;");
         assertTrue(rs.next());
         assertEquals(8, rs.getInt(1));
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void closeOnFalseNext()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS t1");
         stat.executeUpdate("create table t1 (c1);");
         conn.createStatement().executeQuery("select * from t1;").next();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void isBeforeFirst()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select 1 union all select 2;");
         assertTrue(rs.isBeforeFirst());
         assertTrue(rs.next());
         assertTrue(rs.isFirst());
         assertEquals(rs.getInt(1), 1);
         assertTrue(rs.next());
         assertFalse(rs.isBeforeFirst());
         assertFalse(rs.isFirst());
         assertEquals(rs.getInt(1), 2);
         assertFalse(rs.next());
         assertFalse(rs.isBeforeFirst());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void columnNaming()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS t1");
         stat.execute("DROP TABLE IF EXISTS t2");
         stat.executeUpdate("create table t1 (c1 integer);");
         stat.executeUpdate("create table t2 (c1 integer);");
         stat.executeUpdate("insert into t1 values (1);");
         stat.executeUpdate("insert into t2 values (1);");
         ResultSet rs = stat.executeQuery("select a.c1 AS c1 from t1 a, t2 where a.c1=t2.c1;");
         assertTrue(rs.next());
         assertEquals(rs.getInt("c1"), 1);
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void nullDate()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select null;");
         assertTrue(rs.next());
         assertEquals(rs.getDate(1), null);
         assertEquals(rs.getTime(1), null);
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void ambiguousColumnNaming()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS t1");
         stat.execute("DROP TABLE IF EXISTS t2");
         stat.executeUpdate("create table t1 (c1 int);");
         stat.executeUpdate("create table t2 (c1 int, c2 int);");
         stat.executeUpdate("insert into t1 values (1);");
         stat.executeUpdate("insert into t2 values (2, 1);");
         ResultSet rs = stat.executeQuery("select a.c1, b.c1 from t1 a, t2 b where a.c1=b.c2;");
         assertTrue(rs.next());
         assertEquals(rs.getInt("c1"), 1);
         rs.close();
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void failToDropWhenRSOpen()
   {
      Statement stat2 = null;
      try {stat2 = conn.createStatement();} catch (Exception ee) {}
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS t1");
         stat.executeUpdate("create table t1 (c1);");
         stat.executeUpdate("insert into t1 values (4);");
         stat.executeUpdate("insert into t1 values (4);");
         stat2.executeQuery("select * from t1;").next();
         stat.executeUpdate("drop table t1;");
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
         try {stat2.close();} catch (Exception ee) {}
      }
   }

   public void executeNoRS()
   {
      try
      {
         assertFalse(stat.execute("insert into test values (8);"));
         stat.getResultSet();
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void executeClearRS()
   {
      try
      {
         assertTrue(stat.execute("select null;"));
         assertNotNull(stat.getResultSet());
         assertFalse(stat.getMoreResults());
         stat.getResultSet();
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void batchReturnsResults()
   {
      try
      {
         stat.addBatch("select null;");
         stat.executeBatch();
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void noSuchTable()
   {
      try
      {
         stat.executeQuery("select * from doesnotexist;");
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void noSuchCol()
   {
      try
      {
         stat.executeQuery("select notacol from (select 1);");
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void noSuchColName()
   {
      try
      {
         ResultSet rs = stat.executeQuery("select 1;");
         assertTrue(rs.next());
         rs.getInt("noSuchColName");
         fail("should raise exception");
      }
      catch (Exception e)
      {/* ok */
      }
   }

   public void multipleStatements()
   {
      try
      {
         // ; insert into person values(1,'leo')
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS person");
         stat.executeUpdate("create table person (id integer, name string); " + "insert into person values(1, 'leo'); insert into person values(2, 'yui');");
         ResultSet rs = stat.executeQuery("select * from person");
         assertTrue(rs.next());
         assertTrue(rs.next());
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void blobTest()
   {
      try
      {
         Vm.gc(); stat.execute("DROP TABLE IF EXISTS Foo");
         stat.executeUpdate("CREATE TABLE Foo (KeyId INTEGER, Stuff BLOB)");
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
         Date day = new Date();

         Vm.gc(); stat.execute("DROP TABLE IF EXISTS day");
         stat.executeUpdate("create table day (time datatime)");
         PreparedStatement prep = conn.prepareStatement("insert into day values(?)");
         prep.setDate(1, day);
         prep.executeUpdate();
         ResultSet rs = stat.executeQuery("select * from day");
         assertTrue(rs.next());
         Date d = rs.getDate(1);
         assertEquals(day.getTime(), d.getTime());
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void maxRows()
   {
      try
      {
         stat.setMaxRows(1);
         ResultSet rs = stat.executeQuery("select 1 union select 2 union select 3");

         assertTrue(rs.next());
         assertEquals(1, rs.getInt(1));
         assertFalse(rs.next());

         rs.close();
         stat.setMaxRows(2);
         rs = stat.executeQuery("select 1 union select 2 union select 3");

         assertTrue(rs.next());
         assertEquals(1, rs.getInt(1));
         assertTrue(rs.next());
         assertEquals(2, rs.getInt(1));
         assertFalse(rs.next());

         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void testRun()
   {
      try
      {
         connect();
         
         emptyRS();
         executeUpdate();
         singleRowRS();
         twoRowRS();
         stringRS();
         execute();
         colNameAccess();
         nulls();
         tempTable();
         insert1000();
         batch();
         closeOnFalseNext();
         isBeforeFirst();
         columnNaming();
         nullDate();
         ambiguousColumnNaming();
         failToDropWhenRSOpen();
         executeNoRS();
         executeClearRS();
         batchReturnsResults();
         noSuchTable();
         noSuchCol();
         noSuchColName();
         multipleStatements();
         blobTest();
         dateTimeTest();
         maxRows();
         autoClose();
         
         close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
}
