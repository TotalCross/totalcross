package tc.test.totalcross.sql.sqlite;

import totalcross.sql.*;
import totalcross.sys.*;
import totalcross.unit.*;

/**
 * These tests assume that Statements and PreparedStatements are working as per normal and test the interactions of
 * commit(), rollback() and setAutoCommit(boolean) with multiple connections to the same db.
 */
public class TransactionTest extends TestCase
{
   private Connection conn1, conn2, conn3;
   private Statement stat1, stat2, stat3;

   boolean done;
   String tmpName = Settings.appPath + "/test-trans.db";

   public void connect() throws Exception
   {
      conn1 = DriverManager.getConnection("jdbc:sqlite:file:" + tmpName + "?cache=private");
      conn2 = DriverManager.getConnection("jdbc:sqlite:file:" + tmpName + "?cache=private");
      conn3 = DriverManager.getConnection("jdbc:sqlite:file:" + tmpName + "?cache=private");

      stat1 = conn1.createStatement();
      stat2 = conn2.createStatement();
      stat3 = conn3.createStatement();
   }

   public void close() throws Exception
   {
      stat1.close();
      stat2.close();
      stat3.close();
      conn1.close();
      conn2.close();
      conn3.close();
   }

   public void multiConn()
   {
      try
      {
         stat1.execute("DROP TABLE IF EXISTS test");
         stat1.executeUpdate("create table test (c1);");
         stat1.executeUpdate("insert into test values (1);");
         stat2.executeUpdate("insert into test values (2);");
         stat3.executeUpdate("insert into test values (3);");

         ResultSet rs = stat1.executeQuery("select sum(c1) from test;");
         assertTrue(rs.next());
         assertEquals(rs.getInt(1), 6);
         rs.close();

         rs = stat3.executeQuery("select sum(c1) from test;");
         assertTrue(rs.next());
         assertEquals(rs.getInt(1), 6);
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void locking()
   {
      try
      {
         stat1.execute("DROP TABLE IF EXISTS test");
         stat1.executeUpdate("create table test (c1);");
         stat1.executeUpdate("begin immediate;");
         stat2.executeUpdate("select * from test;");
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void insert()
   {
      try
      {
         ResultSet rs;
         String countSql = "select count(*) from trans;";
         stat1.execute("DROP TABLE IF EXISTS trans;");
         conn1.setAutoCommit(false);
         stat1.executeUpdate("create table trans (c1);");

         assertEquals(1, stat1.executeUpdate("insert into trans values (4);"));

         // transaction not yet commited, conn1 can see, conn2 can not
         rs = stat1.executeQuery(countSql);
         assertTrue(rs.next());
         assertEquals(1, rs.getInt(1));
         rs.close();
         try
         {
         rs = stat2.executeQuery(countSql);
         fail("should raise exception");
         }
         catch (Exception ss) {}

         conn1.commit();

         // all connects can see data
         rs = stat2.executeQuery(countSql);
         assertTrue(rs.next());
         assertEquals(1, rs.getInt(1));
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void rollback()
   {
      try
      {
         String select = "select * from trans;";
         ResultSet rs;

         stat1.execute("DROP TABLE IF EXISTS trans");
         stat1.executeUpdate("create table trans (c1);");
         conn1.setAutoCommit(false);
         stat1.executeUpdate("insert into trans values (3);");

         rs = stat1.executeQuery(select);
         assertTrue(rs.next());
         rs.close();

         conn1.rollback();

         rs = stat1.executeQuery(select);
         assertFalse(rs.next());
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void multiRollback()
   {
      try
      {
         ResultSet rs;

         stat1.execute("DROP TABLE IF EXISTS t");
         stat1.executeUpdate("create table t (c1);");
         conn1.setAutoCommit(false);
         stat1.executeUpdate("insert into t values (1);");
         conn1.commit();
         stat1.executeUpdate("insert into t values (1);");
         conn1.rollback();
         stat1.addBatch("insert into t values (2);");
         stat1.addBatch("insert into t values (3);");
         stat1.executeBatch();
         conn1.commit();
         stat1.addBatch("insert into t values (7);");
         stat1.executeBatch();
         conn1.rollback();
         stat1.executeUpdate("insert into t values (4);");
         conn1.setAutoCommit(true);
         stat1.executeUpdate("insert into t values (5);");
         conn1.setAutoCommit(false);
         PreparedStatement p = conn1.prepareStatement("insert into t values (?);");
         p.setInt(1, 6);
         p.executeUpdate();
         p.setInt(1, 7);
         p.executeUpdate();

         // conn1 can see (1+...+7), conn2 can see (1+...+5)
         rs = stat1.executeQuery("select sum(c1) from t;");
         assertTrue(rs.next());
         assertEquals(1 + 2 + 3 + 4 + 5 + 6 + 7, rs.getInt(1));
         rs.close();
         rs = stat2.executeQuery("select sum(c1) from t;");
         assertTrue(rs.next());
         assertEquals(1 + 2 + 3 + 4 + 5, rs.getInt(1));
         rs.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void transactionsDontMindReads()
   {
      try
      {
         stat1.execute("DROP TABLE IF EXISTS t");
         stat1.executeUpdate("create table t (c1);");
         stat1.executeUpdate("insert into t values (1);");
         stat1.executeUpdate("insert into t values (2);");
         ResultSet rs = stat1.executeQuery("select * from t;");
         assertTrue(rs.next()); // select is open

         conn2.setAutoCommit(false);
         stat1.executeUpdate("insert into t values (2);");

         rs.close();
         conn2.commit();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   Exception secondError;
   public void secondConnWillWait() 
   {
      if (Settings.onJavaSE) // guich: AFAZER: no pda esta passando direto
      try
      {
         stat1.execute("DROP TABLE IF EXISTS t;");
         stat1.executeUpdate("create table t (c1);");
         stat1.executeUpdate("insert into t values (1);");
         stat1.executeUpdate("insert into t values (2);");
         ResultSet rs = stat1.executeQuery("select * from t;"); // this will lock the database
         assertTrue(rs.next());

         done = false;
         new Thread()
         {
            public void run()
            {
               try               
               {
                  stat2.executeUpdate("insert into t values (3);"); // this should wait until *** is called
               }
               catch (Exception e)
               {
                  secondError = e;
               }
               done = true;
            }
         }.start();
         
         Vm.sleep(100); // wait the thread start
         rs.close(); // ***

         while (!done)
            Vm.sleep(100);
         if (secondError != null)
            fail(secondError);
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void secondConnMustTimeout()
   {
      try
      {
         stat1.setQueryTimeout(1);
         stat1.execute("DROP TABLE IF EXISTS t");
         stat1.executeUpdate("create table t (c1);");
         stat1.executeUpdate("insert into t values (1);");
         stat1.executeUpdate("insert into t values (2);");
         ResultSet rs = stat1.executeQuery("select * from t;");
         assertTrue(rs.next());

         stat2.executeUpdate("insert into t values (3);"); // can't be done
         fail("should raise exception");
      }
      catch (Exception e)
      {
         //stat2.close();
      }
   }

   public void cantUpdateWhileReading()
   {
      try
      {
         Statement stat2 = conn1.createStatement();
         stat1.close();
         stat1 = conn1.createStatement();
         stat1.execute("DROP TABLE IF EXISTS t");
         stat1.executeUpdate("create table t (c1);");
         stat1.executeUpdate("insert into t values (1);");
         stat1.executeUpdate("insert into t values (2);");
         ResultSet rs = stat2.executeQuery("select * from t;");
         assertTrue(rs.next());

         // commit now succeeds since sqlite 3.6.5
         stat1.executeUpdate("insert into t values (3);"); // can't be done
         stat2.close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }

   public void cantCommit()
   {
      try
      {
         conn1.commit();
         fail("should raise exception");
      }
      catch (Exception e)
      {
      }
   }

   public void cantRollback()
   {
      try
      {
         conn1.rollback();
         fail("should raise exception");
      }
      catch (Exception e)
      {
      }
   }

   public void testRun()
   {
      try
      {
         connect(); cantUpdateWhileReading();    close();
         connect(); secondConnMustTimeout();     close();
         connect(); multiConn();                 close();
         connect(); locking();                   close();
         connect(); insert();                    close();
         connect(); rollback();                  close();
         connect(); multiRollback();             close();
         connect(); transactionsDontMindReads(); close();
         connect(); cantCommit();                close();
         connect(); cantRollback();              close();
         connect(); secondConnWillWait();        close();
      }
      catch (Exception e)
      {
         fail(e);
      }
   }
}
