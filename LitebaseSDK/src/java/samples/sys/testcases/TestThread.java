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

package samples.sys.testcases;

import litebase.*;
import totalcross.sys.*;
import totalcross.unit.TestCase;

/**
 * An internal class that is used to tests threads with Litebase.
 */
final class LBThread implements Runnable
{
   /**
    * The thread.
    */
   Thread t;
   
   /**
    * A table id: 1 or 2.
    */
	int tableId;
	
	/**
	 * Indicates if the thread finished correctly.
	 */
   boolean finishedGracefully;
   
   /**
    * Indicates that the thread must only sleep when it is running on a computer.
    */
   final static boolean doSleep = Settings.platform.equals(Settings.WIN32) || Settings.platform.equals(Settings.LINUX) || Settings.onJavaSE;

   /** 
    * The thread run() method.
    */
	public void run()
	{
	   try
	   {
	      // Opens the connection with Litebase: one for each thread.
	      LitebaseConnection conn = AllTests.getInstance("Test");
	      if (doSleep) {
	    	  Thread.yield();
	      }
	      
	      // Creates the table.
	      if (conn.exists("person" + tableId))
	         conn.executeUpdate("drop table person" + tableId);
	      conn.execute("create table person" + tableId + " (id int primary key, name char(30))");
	      if (doSleep) {
	    	  Thread.yield();
	      }
	      
	      // Empties the table.
	      conn.executeUpdate("delete from person" + tableId);

	      // Populates the table.
	      int i = -1;
	      PreparedStatement stmt = conn.prepareStatement("insert into person" + tableId + " (id,name) values(?, ?)");
	      while (++i < 100)
	      {
	         if (i == 50 && doSleep) {
	        	 Thread.yield();
	         }
	         stmt.setInt(0, i);
	         stmt.setString(1, "Name " + i);
	         stmt.executeUpdate();
	      }
	      if (doSleep) {
	    	  Thread.yield();
	      }
      	
	      // Searches the entire table to see if it was built correctly.
         ResultSet resultSet = conn.executeQuery("select * from person" + tableId);
         int rowCount = resultSet.getRowCount();
         if (rowCount != 100)
         	throw new Exception(rowCount + " != 100");
         String name;
         i = -1;
         while (++i < 100)
         {
            resultSet.next();
            name = resultSet.getString("Name");
            if (!name.equals("Name " + i))
            	throw new Exception(name + " != Name " + i);
            if (doSleep) {
            	Thread.yield();
            }
         }
         
         // Searches the table using the index.
         resultSet = conn.executeQuery("select * from person" + tableId + " where id >= 50");
         rowCount = resultSet.getRowCount();
         resultSet.close();
         if (rowCount != 50)
            throw new Exception(rowCount + " != 50"); 
         if (doSleep) {
        	 Thread.yield();
         }
         conn.closeAll();
         
         LitebaseConnection.setLogger(LitebaseConnection.getLogger()); // Tests the logger.
         finishedGracefully = true; // The thread finished correctly.
	   }
	   catch (Exception exception)
	   {
	      exception.printStackTrace(); // Shows the error message.
	      throw new RuntimeException(exception.getMessage());
	   }
	}
}

/**
 * Tests Litebase with threads.
 */
public class TestThread extends TestCase
{
   /** 
    * The main test method.
    */
	public void testRun()
   {
	   LBThread test1, 
	            test2;
	   test1 = new LBThread();
      test1.tableId = 1;
      test1.t = new Thread(test1);
      test1.t.start();
      test2 = new LBThread();
      test2.tableId = 2;
      test2.t = new Thread(test2);
      test2.t.start();
      while (test1.t.isAlive() || test2.t.isAlive())
      	Vm.sleep(10);
      output("Thread 1 " + (test1.finishedGracefully? "finished" : "aborted"));
      output("Thread 2 " + (test2.finishedGracefully? "finished" : "aborted"));

      if (!test1.finishedGracefully)
      	fail("thread 1 aborted");
      if (!test2.finishedGracefully)
         fail("thread 2 aborted");
   }
}
