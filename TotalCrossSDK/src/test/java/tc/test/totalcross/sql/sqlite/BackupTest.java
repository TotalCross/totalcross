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

import totalcross.io.File;
import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.Settings;
import totalcross.unit.TestCase;

public class BackupTest extends TestCase {
  static String absPath = Settings.appPath;

  public void backupAndRestore() {
    try {
      output("Backup path " + absPath);

      // memory DB to file
      Connection conn = DriverManager.getConnection("jdbc:sqlite:");
      Statement stmt = conn.createStatement();
      try {
        stmt.executeUpdate("drop table sample");
      } catch (Exception e) {
      }
      stmt.executeUpdate("create table sample(id, name)");
      stmt.executeUpdate("insert into sample values(1, \"leo\")");
      stmt.executeUpdate("insert into sample values(2, \"gui\")");

      stmt.executeUpdate("backup to '" + absPath + "/bak.db'");
      stmt.close();

      // open another memory database
      Connection conn2 = DriverManager.getConnection("jdbc:sqlite:");
      Statement stmt2 = conn2.createStatement();
      stmt2.execute("restore from '" + absPath + "/bak.db'");
      ResultSet rs = stmt2.executeQuery("select * from sample");
      int count = 0;
      while (rs.next()) {
        count++;
      }

      assertEquals(2, count);
      rs.close();

    } catch (Exception e) {
      fail(e);
    }
  }

  public void memoryToDisk() {
    File tmpFile = null;
    try {
      if (Settings.onJavaSE) {
        return; // skip this test in pure-java mode
      }

      Connection conn = DriverManager.getConnection("jdbc:sqlite:");
      Statement stmt = conn.createStatement();
      try {
        stmt.executeUpdate("drop table sample");
      } catch (Exception e) {
      }
      stmt.executeUpdate("create table sample(id integer primary key autoincrement, name)");
      for (int i = 0, n = Settings.platform.equals(Settings.ANDROID) ? 100 : 1000; i < n; i++) {
        stmt.executeUpdate("insert into sample(name) values(\"leo\")");
      }

      String p = absPath + "/backup-test.sqlite";
      stmt.executeUpdate("backup to '" + p + "'");
      stmt.close();
    } catch (Exception e) {
      fail(e);
    } finally {
      if (tmpFile != null) {
        try {
          tmpFile.delete();
        } catch (Exception ee) {
        }
      }
    }
  }

  @Override
  public void testRun() {
    backupAndRestore();
    memoryToDisk();
  }
}
