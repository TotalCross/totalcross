// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package samples.sys.bench;
import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;

/**
  * Performs a benchmark in the LitebaseConnection.
  */
public class BenchLitebase extends MainWindow
{
   /**
    * The connection with Litebase.
    */
   private LitebaseConnection driver = LitebaseConnection.getInstance("Test");
   
   /**
    * The progress bar for the inserts.
    */
   private ProgressBar pbInserts;
   
   /**
    * The list box for showing the results.
    */
   private ListBox results;
   
   /**
    * The number of records to be inserted.
    */
   private final static int NRECS = 50000; 
   
   /**
    * The number of records dived by two.
    */
   private final static int NRECS_DIV2 = NRECS >> 1;
   
   /**
    * The refresh rate of the inserts progress bar.
    */
   private final static int REFRESH_MOD = NRECS / 50;

   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The constructor.
    */
   public BenchLitebase()
   {
      if (Settings.onJavaSE)
         totalcross.sys.Settings.showDesktopMessages = false;
      Vm.tweak(Vm.TWEAK_DUMP_MEM_STATS,true);
      Vm.debug(Vm.ALTERNATIVE_DEBUG);
   }

   /**
    * Creates the table.
    */
   private void createTable()
   {
      try 
      {
         driver.executeUpdate("drop table person");
      }
      catch (DriverException exception) {}
      
      log("Creating tables...");
      driver.execute("create table PERSON (NAME CHAR(8))");
      driver.setRowInc("person", NRECS); // Allocates NRECS at once.
   }
   
   /**
    * inserts the records using prepared statement.
    * 
    * @return The time taken for the operation.
    */
   private int insertWithPS()
   {
      StringBuffer sb = new StringBuffer("a"); // Savea some gc() time.
      int refresh = REFRESH_MOD,
          time = Vm.getTimeStamp(),
          i = 0;
      PreparedStatement ps = driver.prepareStatement("insert into person values (?)");
      
      while (++i <= NRECS_DIV2)
      {
         ps.setString(0, sb.append(i).toString());
         ps.executeUpdate();
         if (refresh-- == 0)
         {
            pbInserts.setValue(500 * i / NRECS);
            refresh = REFRESH_MOD;
         }
         sb.setLength(1);
      }
      
      log("Creation time (PrepStat): " + (time = Vm.getTimeStamp() - time) + "ms");
      return time;
   }
   
   /**
    * inserts the records using normal statements.
    * 
    * @return The time taken for the operation.
    */
   private int insertNormal()
   {
      StringBuffer sb = new StringBuffer("a"); // Saves some gc() time.
      int refresh = REFRESH_MOD,
          i = NRECS_DIV2,
          time = Vm.getTimeStamp();
      
      sb.setLength(0);
      sb.append("insert into person values ('a");
      while (++i <= NRECS)
      {
         sb.setLength(29);
         driver.executeUpdate(sb.append(i).append("')").toString());
         if (refresh-- == 0)
         {
            pbInserts.setValue(500 * i / NRECS);
            refresh = REFRESH_MOD;
         }
      }
      driver.setRowInc("person", -1); // Returns the inc to the default value.
      pbInserts.setValue(500);
      log("Creation time (normal): " + (time = Vm.getTimeStamp() - time) + "ms");
      return time;
   }

   /**
    * Selects the before last element.
    * 
    * @return The time taken for the operation.
    */
   private int selectBeforeLast()
   {
      Vm.gc();
      log("Select name = 'a" + (NRECS - 1) + "'");
      
      int time = Vm.getTimeStamp();
      ResultSet resultSet = driver.executeQuery("select * from person where name = 'a" + (NRECS - 1) + "'");
     
      time = Vm.getTimeStamp() - time; 
      log("-> Found " + resultSet.getRowCount() + " elements");
      if (resultSet.next())
         log("-> Found: " + resultSet.getString(1));
      else
         log("*** Not found...");
      log("Finished: " + time + "ms");
      return time;
   }

   /**
    * Selects all the elements beginning with a9.
    * 
    * @return The time taken for the operation.
    */
   private int selectLikeA9()
   {
      Vm.gc();
      log("Select like 'a9%'");
      StringBuffer sb = new StringBuffer();
      int time = Vm.getTimeStamp(),
          i = -1;
      ResultSet resultSet = driver.executeQuery("select * from person where name like 'a9%'");
      
      time = Vm.getTimeStamp() - time;
      log("-> Found " + resultSet.getRowCount() + " elements");
      
      while (++ i < 5 && resultSet.next())
         sb.append(' ').append(resultSet.getString(1));
      log("First 5:" + sb);
      log("Finished: " + time + "ms");
      return time;
   }
   
   /**
    * Creates the index.
    * 
    * @return The time taken for the operation.
    */
   private int createIndex()
   {
      int time = Vm.getTimeStamp();
      
      driver.execute("CREATE INDEX IDX_NAME ON PERSON(NAME)");
      time = Vm.getTimeStamp() - time;
      log("= Index creation time: " + time + "ms");
      return time;
   }
   
   /**
    * Selects all the rows of the table.
    * 
    * @return The time taken for the operation.
    */
   private int selectStar()
   {
      Vm.gc();
      log("Select star");
      int time = Vm.getTimeStamp();
      ResultSet resultSet = driver.executeQuery("select * from person");
      
      time = Vm.getTimeStamp() - time;
      log("-> Found " + resultSet.getRowCount() + " elements");
      if (resultSet.next())
         log("-> Found: " + resultSet.getString(1));
      else
         log("*** Not found...");
      log("Finished: " + time + "ms");
      return time;
   }
   
   /**
    * Fetches the number of rows of the table.
    * 
    * @return  The time taken for the operation.
    */
   private int selectCountStar()
   {
      Vm.gc();
      log("Select count(*)");
      int tempo = Vm.getTimeStamp();
      ResultSet resultSet = driver.executeQuery("select count(*) as number from person");
      
      tempo = Vm.getTimeStamp() - tempo;
      log("-> Found " + resultSet.getRowCount() + " elements");
      if (resultSet.next())
         log("-> Found: " + resultSet.getString(1));
      else
         log("*** Not found...");
      log("Finished: " + tempo + "ms");
      return tempo;
   }
   
   /**
    * Selects the maximum element.
    * 
    * @return  The time taken for the operation.
    */
   private int selectMax()
   {
      Vm.gc();
      log("Select max()");
      int time = Vm.getTimeStamp();
      ResultSet resultSet = driver.executeQuery("select max(name) as mname from person where name >= 'a0'");
      
      time = Vm.getTimeStamp() - time;
      log("-> Found " + resultSet.getRowCount() + " elements");
      if (resultSet.next())
         log("-> Found: " + resultSet.getString(1));
      else
         log("*** Not found...");
      log("Finished: " + time + "ms");
      return time;
   }
   
   /**
    * Does a select with order by.
    * 
    * @return  The time taken for the operation.
    */
   private int selectOrderBy()
   {
      Vm.gc();
      log("Select with order by");
      int time = Vm.getTimeStamp();
      ResultSet resultSet = driver.executeQuery("select * from person order by name");
      
      time = Vm.getTimeStamp() - time;
      log("-> Found " + resultSet.getRowCount() + " elements");
      if (resultSet.next())
         log("-> Found: " + resultSet.getString(1));
      else
         log("*** Not found...");
      log("Finished: " + time + "ms");
      return time;
   }

   /** 
    * Logs the results on the debug console and on the list box.
    *
    * @param string The string to be logged.
    */
   private void log(String string)
   {
      Vm.debug(string);
      results.add(string);
   }

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      // User interface.
      ProgressBar pbTotal = new ProgressBar(0, 14);
      add(pbInserts = new ProgressBar(0, 500), CENTER, AFTER + 5);
      add(pbTotal, CENTER, AFTER + 5);
      add(results = new ListBox());
      pbInserts.suffix = "00 of " + NRECS;
      pbTotal.suffix = " of 14";
      results.setRect(LEFT, AFTER + 5, FILL, FILL);

      // Executes the bench operations.
      repaintNow();
      createTable();                  
      pbTotal.setValue(1);
      int time1 = insertWithPS();            
      pbTotal.setValue(2);
      int time2 = insertNormal();            
      pbTotal.setValue(3);
      int time3 = selectBeforeLast();        
      pbTotal.setValue(4);
      int time4 = selectLikeA9();            
      pbTotal.setValue(5); 
      int time5 = selectMax();
      pbTotal.setValue(6);
      int time6 = createIndex();             
      pbTotal.setValue(7); 
      int time7 = selectBeforeLast();        
      pbTotal.setValue(8);
      int time8 = selectLikeA9();
      pbTotal.setValue(9);
      int time9 = selectMax();              
      pbTotal.setValue(10);
      int time10 = selectStar();              
      pbTotal.setValue(11);
      int time11 = selectCountStar();         
      pbTotal.setValue(12); 
      int time12 = selectOrderBy();              
      pbTotal.setValue(13); 
      
      driver.executeUpdate("drop index * on person");
      driver.executeUpdate("alter table person add primary key(name)");
      
      int time13 = selectOrderBy();              
      pbTotal.setValue(14);
      
      // Logs the results.
      log(time1+ " " + time2);
      log(time3 + " " + time4 + " " + time5);
      log(time6 + " ");
      log(time7 + " " + time8 + " " + time9);
      log(time10 + " " + time11 + " " + time12 + " " + time13);
      log("total: " + (time1 + time2 + time3 + time4 + time5 + time6 + time7 + time8 + time9 + time10 + time11 + time12));
      log("Results are also in the console");
      
      results.selectLast();
      results.requestFocus();
   }
}
