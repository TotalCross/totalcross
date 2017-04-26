/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package samples.sys.testcases;

import litebase.LitebaseConnection;
import totalcross.sys.Vm;
import totalcross.unit.TestCase;

/** 
 * Tests the oppening and deletion of Litebase logger files. 
 */
public class TestLogger  extends TestCase
{
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      // Turn logging off and erases all log files.
      if (AllTests.useLogger)
      {
         LitebaseConnection.logger.dispose(true);
         LitebaseConnection.logger = null;
      }
      LitebaseConnection.deleteLogFiles();
      
      assertEquals(0, LitebaseConnection.deleteLogFiles()); // No logger files to be deleted.
      
      // Creates one logger file and tries to delete it when using and after using it.
      LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
      assertEquals(0, LitebaseConnection.deleteLogFiles());
      LitebaseConnection.logger.dispose(true);
      LitebaseConnection.logger = null;
      assertEquals(1, LitebaseConnection.deleteLogFiles());
      
      // Creates two logger files. Just the not currently used one can be deleted.
      int time = Vm.getTimeStamp();
      LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
      LitebaseConnection.logger.dispose(true);
      LitebaseConnection.logger = null;
      while (Vm.getTimeStamp() - time < 2000)
         Vm.sleep(1);
      LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
      assertEquals(1, LitebaseConnection.deleteLogFiles());
      LitebaseConnection.logger.dispose(true);
      LitebaseConnection.logger = null;
      assertEquals(1, LitebaseConnection.deleteLogFiles());
      
      // Creates three logger files. Just the not currently used ones can be deleted.
      time = Vm.getTimeStamp();
      LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
      LitebaseConnection.logger.dispose(true);
      LitebaseConnection.logger = null;
      while (Vm.getTimeStamp() - time < 1000)
         Vm.sleep(1);
      time = Vm.getTimeStamp();
      LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
      LitebaseConnection.logger.dispose(true);
      LitebaseConnection.logger = null;
      while (Vm.getTimeStamp() - time < 1000)
         Vm.sleep(1);
      LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
      assertEquals(2, LitebaseConnection.deleteLogFiles());
      LitebaseConnection.logger.dispose(true);
      LitebaseConnection.logger = null;
      assertEquals(1, LitebaseConnection.deleteLogFiles());
      
      if (AllTests.useLogger) // If the tests are using logger, turn it on again.
         LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();   
   }
}
