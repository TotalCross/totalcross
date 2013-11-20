// --------------------------------------
// sqlite-jdbc Project
//
// BackupTest.java
// Since: Feb 18, 2009
//
// $URL$
// $Author$
// --------------------------------------
package tc.test.totalcross.sql.sqlite;

import totalcross.unit.*;
import totalcross.io.*;
import totalcross.sql.*;
import totalcross.sys.*;

public class BackupTest extends TestCase
{
   static String absPath = Settings.appPath;

   public void backupAndRestore()
   {
      File tmpFile = null;
      try
      {
         // create a memory database
         tmpFile = new File(absPath + "/backup-test.sqlite", File.CREATE_EMPTY);

         // memory DB to file
         Connection conn = DriverManager.getConnection("jdbc:sqlite:");
         Statement stmt = conn.createStatement();
         stmt.executeUpdate("create table sample(id, name)");
         stmt.executeUpdate("insert into sample values(1, \"leo\")");
         stmt.executeUpdate("insert into sample values(2, \"gui\")");

         stmt.executeUpdate("backup to " + absPath);
         stmt.close();

         // open another memory database
         Connection conn2 = DriverManager.getConnection("jdbc:sqlite:");
         Statement stmt2 = conn2.createStatement();
         stmt2.execute("restore from " + absPath);
         ResultSet rs = stmt2.executeQuery("select * from sample");
         int count = 0;
         while (rs.next())
         {
            count++;
         }

         assertEquals(2, count);
         rs.close();

      }
      catch (Exception e)
      {
         fail(e);
      }
      finally
      {
         if (tmpFile != null)
            try
            {
               tmpFile.delete();
            }
            catch (Exception ee)
            {
            }
      }
   }

   public void memoryToDisk()
   {
      File tmpFile = null;
      try
      {
         if (Settings.onJavaSE)
            return; // skip this test in pure-java mode

         Connection conn = DriverManager.getConnection("jdbc:sqlite:");
         Statement stmt = conn.createStatement();
         stmt.executeUpdate("create table sample(id integer primary key autoincrement, name)");
         for (int i = 0; i < 10000; i++)
            stmt.executeUpdate("insert into sample(name) values(\"leo\")");

         tmpFile = new File(absPath + "/backup-test.sqlite", File.CREATE_EMPTY);
         // System.err.println("backup start");
         stmt.executeUpdate("backup to " + absPath);
         stmt.close();
         // System.err.println("backup done.");
      }
      catch (Exception e)
      {
         fail(e);
      }
      finally
      {
         if (tmpFile != null)
            try
            {
               tmpFile.delete();
            }
            catch (Exception ee)
            {
            }
      }
   }

   public void testRun()
   {
      backupAndRestore();
      memoryToDisk();
   }
}
