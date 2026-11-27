// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

import totalcross.db.sqlite.SQLiteConnection;
import totalcross.io.File;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.Settings;
import totalcross.ui.MainWindow;
import totalcross.ui.image.Image;

/** Deployed native startup probe and downstream Linux functional smoke. */
public final class LinuxStartupProbe extends MainWindow {
  @Override
  public void initUI() {
    System.out.println("TC_PATH_MARKER appPath=" + Settings.appPath + " vmPath=" + Settings.vmPath);
    System.out.println("TC_STARTUP_MARKER");
    try {
      if (Settings.appPath == null || Settings.appPath.length() == 0
          || Settings.vmPath == null || Settings.vmPath.length() == 0) {
        throw new IllegalStateException("native path discovery returned an empty directory");
      }
      if (Settings.platform.equals(Settings.LINUX) && Settings.appPath.length() < 200) {
        checkFile();
        checkSqlite();
        checkJpeg();
      }
      System.out.println("TC_STARTUP_COMPLETE");
      MainWindow.exit(0);
    } catch (Throwable failure) {
      System.out.println("TC_STARTUP_FAILURE=" + failure.getClass().getName()
          + ":" + failure.getMessage());
      MainWindow.exit(1);
    }
  }

  private static void checkFile() throws Exception {
    String path = Settings.appPath + "/native-startup-file.bin";
    byte[] expected = "native-startup-file".getBytes();
    File output = new File(path, File.CREATE_EMPTY);
    if (output.writeBytes(expected, 0, expected.length) != expected.length) {
      throw new IllegalStateException("file write length mismatch");
    }
    output.close();

    byte[] actual = new byte[expected.length];
    File input = new File(path, File.READ_WRITE);
    int length = input.readBytes(actual, 0, actual.length);
    input.close();
    if (length != expected.length) {
      throw new IllegalStateException("file read length mismatch: " + length);
    }
    for (int i = 0; i < expected.length; i++) {
      if (actual[i] != expected[i]) {
        throw new IllegalStateException("file read content mismatch at " + i);
      }
    }
    new File(path).delete();
    System.out.println("TC_FILE_CONTROL_PASS");
  }

  private static void checkSqlite() throws Exception {
    String path = Settings.appPath + "/native-startup.sqlite";
    SQLiteConnection database = new SQLiteConnection("jdbc:sqlite:" + path, path);
    Statement statement = database.createStatement();
    statement.executeUpdate("create table if not exists startup_probe (value integer)");
    statement.executeUpdate("delete from startup_probe");
    statement.executeUpdate("insert into startup_probe values (1)");
    statement.close();
    database.close();

    database = new SQLiteConnection("jdbc:sqlite:" + path, path);
    statement = database.createStatement();
    ResultSet result = statement.executeQuery("pragma integrity_check");
    if (!result.next() || !"ok".equals(result.getString(1))) {
      throw new IllegalStateException("SQLite integrity_check did not return ok");
    }
    result.close();
    statement.close();
    database.close();
    System.out.println("TC_SQLITE_CONTROL_PASS integrity_check=ok");
  }

  private static void checkJpeg() throws Exception {
    Image jpeg = new Image(Settings.appPath + "/comparison-control.jpg");
    if (jpeg.getWidth() != 8 || jpeg.getHeight() != 8) {
      throw new IllegalStateException("known-good JPEG dimensions changed");
    }
    Image canvas = new Image(16, 16);
    canvas.getGraphics().drawImage(jpeg, 0, 0);
    System.out.println("TC_JPEG_CONTROL_PASS width=8 height=8");
  }
}
