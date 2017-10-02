/*********************************************************************************
 * TotalCross Software Development Kit *
 * Copyright (C) 2000-2014 SuperWaba Ltda. *
 * All Rights Reserved *
 * *
 * This library and virtual machine is distributed in the hope that it will *
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *
 * *
 * This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0 *
 * A copy of this license is located in file license.txt at the root of this *
 * SDK or can be downloaded here: *
 * http://www.gnu.org/licenses/lgpl-3.0.txt *
 * *
 *********************************************************************************/

package tc.samples.api.sql;

import tc.samples.api.BaseContainer;
import totalcross.sql.Connection;
import totalcross.sql.DriverManager;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sys.Convert;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.Label;
import totalcross.ui.ProgressBar;

/**
 * Performs a benchmark in the SQLite.
 */
public class SQLiteBenchSample extends BaseContainer {

  /**
   * The connection with SQLite.
   */
  private Connection driver;

  /**
   * The used statement.
   */
  private Statement statement;

  /**
   * The progress bar for the inserts.
   */
  private ProgressBar pbInserts;

  /**
   * The progress bar for the operations.
   */
  private ProgressBar pbTotal;

  /**
   * The number of records inserts for each kind of insert operation
   */
  private final static int NRECSBYOPERATION = 50000;

  /**
   * The number of records to be inserted.
   */
  private final static int NRECS = NRECSBYOPERATION * 6;

  /**
   * The refresh rate of the inserts progress bar.
   */
  private final static int REFRESH_MOD = NRECS / 50;

  /**
   * The refresh rate of the operations progress bar.
   */
  private final static int TOTAL_OF_OPERATIONS = 17;

  /**
   * The constructor.
   */
  public SQLiteBenchSample() {
    if (Settings.onJavaSE) {
      Settings.showDesktopMessages = false;
    }
    Vm.tweak(Vm.TWEAK_DUMP_MEM_STATS, true);
  }

  /**
   * Creates the table.
   * 
   * @throws Exception
   *         If an internal method throws it.
   */
  private void createTable() throws Exception {
    driver = DriverManager.getConnection("jdbc:sqlite:" + Convert.appendPath(Settings.appPath, "person.db"));

    log("Creating tables...", false);
    (statement = driver.createStatement()).executeUpdate("drop table if exists person");
    statement.execute("create table PERSON (NAME CHAR(8))");
  }

  /**
   * Inserts the records using prepared statement.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int insertWithPS() throws Exception {
    StringBuffer sb = new StringBuffer("a"); // Savea some gc() time.
    int refresh = REFRESH_MOD, time = Vm.getTimeStamp(), i = 0;
    PreparedStatement ps = driver.prepareStatement("insert into person values (?)");

    while (++i <= NRECSBYOPERATION) {
      ps.setString(1, sb.append(i).toString());
      ps.executeUpdate();
      if (--refresh == 0) {
        int pbInsertsValue = ((pbTotal.getValue() - 1) * NRECSBYOPERATION) + i;
        pbInserts.setValue(pbInsertsValue);
        refresh = REFRESH_MOD;
      }
      sb.setLength(1);
    }
    pbInserts.setValue(pbTotal.getValue() * NRECSBYOPERATION);
    log("Creation time (PrepStat): " + (time = Vm.getTimeStamp() - time) + "ms", false);
    return time;
  }

  /**
   * Inserts the records using prepared statement in a batch operation.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int insertWithBatch() throws Exception {
    StringBuffer sb = new StringBuffer("a"); // Savea some gc() time.
    int refresh = REFRESH_MOD, time = Vm.getTimeStamp(), i = 0;
    PreparedStatement ps = driver.prepareStatement("insert into person values (?)");

    while (++i <= NRECSBYOPERATION) {
      ps.setString(1, sb.append(i).toString());
      ps.addBatch();
      if (--refresh == 0) {
        int pbInsertsValue = ((pbTotal.getValue() - 1) * NRECSBYOPERATION) + i;
        pbInserts.setValue(pbInsertsValue);
        refresh = REFRESH_MOD;
      }
      sb.setLength(1);
    }
    ps.executeBatch();
    pbInserts.setValue(pbTotal.getValue() * NRECSBYOPERATION);
    log("Creation time (Batch): " + (time = Vm.getTimeStamp() - time) + "ms", false);
    return time;
  }

  /**
   * Inserts the records using normal statements.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int insertNormal() throws Exception {
    StringBuffer sb = new StringBuffer("a"); // Saves some gc() time.
    int refresh = REFRESH_MOD, i = 0, time = Vm.getTimeStamp();

    sb.setLength(0);
    sb.append("insert into person values ('a");
    while (++i <= NRECSBYOPERATION) {
      sb.setLength(29);
      statement.executeUpdate(sb.append(i).append("')").toString());
      if (--refresh == 0) {
        int pbInsertsValue = ((pbTotal.getValue() - 1) * NRECSBYOPERATION) + i;
        pbInserts.setValue(pbInsertsValue);
        refresh = REFRESH_MOD;
      }
    }
    pbInserts.setValue(pbTotal.getValue() * NRECSBYOPERATION);
    log("Creation time (normal): " + (time = Vm.getTimeStamp() - time) + "ms", false);
    return time;
  }

  /**
   * Selects the before last element.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int selectBeforeLast() throws Exception {
    Vm.gc();
    log("Select name = 'a" + (NRECS - 1) + "'", false);

    int time = Vm.getTimeStamp();
    int count = 0;
    ResultSet resultSet = statement.executeQuery("select * from person where name = 'a" + (NRECS - 1) + "'");

    time = Vm.getTimeStamp() - time;

    while (resultSet.next()) {
      log("-> Found: " + resultSet.getString(1));
      count++;
    }

    if (count == 0) {
      log("*** Not found...", false);
    }
    log("-> Found " + count + " elements", false);
    log("Finished: " + time + "ms", false);
    return time;
  }

  /**
   * Selects all the elements beginning with a9.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int selectLikeA9() throws Exception {
    Vm.gc();
    log("Select like 'a9%'", false);
    StringBuffer sb = new StringBuffer();
    int time = Vm.getTimeStamp(), i = -1;
    ResultSet resultSet = statement.executeQuery("select * from person where name like 'a9%'");

    time = Vm.getTimeStamp() - time;

    while (++i < 5 && resultSet.next()) {
      sb.append(' ').append(resultSet.getString(1));
    }
    while (resultSet.next()) {
      i++;
    }
    log("-> Found " + i + " elements", false);
    log("First 5:" + sb);
    log("Finished: " + time + "ms", false);
    return time;
  }

  /**
   * Creates the index.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int createIndex() throws Exception {
    int time = Vm.getTimeStamp();

    statement.execute("CREATE INDEX IDX_NAME ON PERSON(NAME)");
    time = Vm.getTimeStamp() - time;
    log("= Index creation time: " + time + "ms", false);
    return time;
  }

  /**
   * Selects all the rows of the table.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int selectStar() throws Exception {
    Vm.gc();
    log("Select star", false);
    int time = Vm.getTimeStamp();
    int count = 0;
    ResultSet resultSet = statement.executeQuery("select * from person");

    time = Vm.getTimeStamp() - time;
    if (resultSet.next()) {
      log("-> Found: " + resultSet.getString(1));
      count++;
    }
    while (resultSet.next()) {
      count++;
    }

    if (count == 0) {
      log("*** Not found...", false);
    }
    log("-> Found " + count + " elements", false);
    log("Finished: " + time + "ms", false);
    return time;
  }

  /**
   * Fetches the number of rows of the table.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int selectCountStar() throws Exception {
    Vm.gc();
    log("Select count(*)", false);
    int time = Vm.getTimeStamp();
    int count = 0;
    ResultSet resultSet = statement.executeQuery("select count(*) as number from person");

    time = Vm.getTimeStamp() - time;
    while (resultSet.next()) {
      log("-> Found: " + resultSet.getString(1));
      count++;
    }

    if (count == 0) {
      log("*** Not found...", false);
    }
    log("-> Found " + count + " elements", false);
    log("Finished: " + time + "ms", false);
    return time;
  }

  /**
   * Selects the maximum element.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int selectMax() throws Exception {
    Vm.gc();
    log("Select max()", false);
    int time = Vm.getTimeStamp();
    int count = 0;
    ResultSet resultSet = statement.executeQuery("select max(name) as mname from person where name >= 'a0'");

    time = Vm.getTimeStamp() - time;
    while (resultSet.next()) {
      log("-> Found: " + resultSet.getString(1));
      count++;
    }

    if (count == 0) {
      log("*** Not found...", false);
    }
    log("-> Found " + count + " elements", false);
    log("Finished: " + time + "ms", false);
    return time;
  }

  /**
   * Does a select with order by.
   * 
   * @return The time taken for the operation.
   * @throws Exception
   *         If an internal method throws it.
   */
  private int selectOrderBy() throws Exception {
    Vm.gc();
    log("Select with order by", false);
    int time = Vm.getTimeStamp();
    int count = 0;
    ResultSet resultSet = statement.executeQuery("select * from person order by name");

    time = Vm.getTimeStamp() - time;
    if (resultSet.next()) {
      log("-> Found: " + resultSet.getString(1));
      count++;
    }
    while (resultSet.next()) {
      count++;
    }

    if (count == 0) {
      log("*** Not found...", false);
    }
    log("-> Found " + count + " elements", false);
    log("Finished: " + time + "ms", false);
    return time;
  }

  /**
   * Initializes the user interface.
   */
  @Override
  public void initUI() {
    super.initUI();
    add(new Label("Try also BenchLitebase, in the samples folder"), CENTER, TOP);
    // User interface.
    pbTotal = new ProgressBar(0, TOTAL_OF_OPERATIONS);
    add(pbInserts = new ProgressBar(0, 500), CENTER, AFTER + 5);
    add(pbTotal, CENTER, AFTER + 5);
    addLog(LEFT, AFTER + 5, FILL, FILL, null);
    pbInserts.suffix = " of " + NRECS;
    pbTotal.suffix = " of " + TOTAL_OF_OPERATIONS;

    // Executes the bench operations.
    repaintNow();
    try {
      createTable();

      driver.setAutoCommit(false);

      // inserts with synchronous mode ON (support to concurrent access)
      log("Inserts with synchronous mode ON");
      pbTotal.setValue(1);
      int time1 = insertWithPS();
      pbTotal.setValue(2);
      int time2 = insertNormal();
      pbTotal.setValue(3);
      int timeInsertsUsingBatch = insertWithBatch();
      driver.commit();

      // inserts with synchronous mode OFF (removing the support to concurrent access)
      {
        // pragma commands cannot be made inside a transaction
        driver.setAutoCommit(true);
        // It's very helpful when we have inserts and selects during the same transaction, and it's guarantee that have only one connection in this database
        driver.prepareStatement("pragma synchronous=off").executeUpdate();
        driver.setAutoCommit(false);
      }
      log("Inserts with synchronous mode OFF");
      pbTotal.setValue(4);
      int time1b = insertWithPS();
      pbTotal.setValue(5);
      int time2b = insertNormal();
      pbTotal.setValue(6);
      int timeInsertsUsingBatchb = insertWithBatch();
      driver.commit();
      {
        // pragma commands cannot be made inside a transaction
        driver.setAutoCommit(true);
        driver.prepareStatement("pragma synchronous=off").executeUpdate();
        driver.setAutoCommit(false);
      }
      driver.setAutoCommit(true);

      pbTotal.setValue(7);
      int time3 = selectBeforeLast();
      pbTotal.setValue(8);
      int time4 = selectLikeA9();
      pbTotal.setValue(9);
      int time5 = selectMax();
      pbTotal.setValue(10);
      int time6 = createIndex();
      pbTotal.setValue(11);
      int time7 = selectBeforeLast();
      pbTotal.setValue(12);
      int time8 = selectLikeA9();
      pbTotal.setValue(13);
      int time9 = selectMax();
      pbTotal.setValue(14);
      int time10 = selectStar();
      pbTotal.setValue(15);
      int time11 = selectCountStar();
      pbTotal.setValue(16);
      int time12 = selectOrderBy();
      pbTotal.setValue(17);

      statement.close();
      driver.close();

      // Logs the results.
      log("With concurrent access mode ON:");
      log(time1 + " " + time2 + " " + timeInsertsUsingBatch);
      log("With concurrent access mode OFF:");
      log(time1b + " " + time2b + " " + timeInsertsUsingBatchb);
      log(time3 + " " + time4 + " " + time5);
      log(time6 + " ");
      log(time7 + " " + time8 + " " + time9);
      log(time10 + " " + time11 + " " + time12);
      int totalInserts = (time1 + time2 + timeInsertsUsingBatch + time1b + time2b + timeInsertsUsingBatchb);
      log("total: "
          + (totalInserts + time3 + time4 + time5 + time6 + time7 + time8 + time9 + time10 + time11 + time12));
    } catch (Exception exception) {
      exception.printStackTrace();
      log(exception.getMessage());
    }

    log("Results are also in the console", false);
    lblog.selectLast();
  }
}
