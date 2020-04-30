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

// Created on 29/04/2004
import litebase.*;
import totalcross.io.*;
import totalcross.sys.*;
import totalcross.unit.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

/** 
 * The class that executes all test cases.
 */
public class AllTests extends TestSuite
{
   /**
    * Indicates if the logger is to be used or not during AllTests.
    */
   static boolean useLogger;
   
   /**
    * Indicates if the connections of the tests except for <code>TestAsciiTables</code> and <code>TestSourcePath</code> use ascii connections.
    */
   private static boolean isAscii;
   
   /**
    * Indicates if the connections of the tests except for <code>TestAsciiTables</code> and <code>TestSourcePath</code> use cryptography.
    */
   static boolean useCrypto;
   
   static
   {
      Settings.useNewFont = true;
   }
   
   /** 
    * Constructs all the test cases. Needs to be used with TotalCross. 
    */
   public AllTests()
   {
      super("Litebase Test Suite");
      if (Settings.platform.equals(Settings.ANDROID))
         Vm.debug(Vm.ALTERNATIVE_DEBUG);
      addTestCase(TestAddColumn.class);
      addTestCase(TestAsciiTables.class); // juliana@210_2: now Litebase supports tables with ascii strings.
      addTestCase(TestBigJoins.class);
      addTestCase(TestBlob.class);
      addTestCase(TestCachedRows.class);
      addTestCase(TestClosedLitebaseAndProcessLogs.class);
      addTestCase(TestComposedIndexAndPK.class);
      addTestCase(TestCryptoTables.class);
      addTestCase(TestDate_DateTime.class);
      addTestCase(TestDeleteAndMetaData.class);
      addTestCase(TestDeleteAndPurge.class);
      addTestCase(TestDrop.class);
      addTestCase(TestDuplicateEntry.class);
      addTestCase(TestEndianess.class);
      addTestCase(TestIndexIneqAndLike.class);
      addTestCase(TestIndexRebalance.class);
      addTestCase(TestInvalidArguments.class);
      addTestCase(TestJoin.class);
      addTestCase(TestLogger.class);
      addTestCase(TestMaxMin.class);
      addTestCase(TestMultipleConnection.class);
      addTestCase(TestNullAndDefaultValues.class);
      addTestCase(TestOrderBy.class);
      addTestCase(TestOrderByIndices.class);
      addTestCase(TestPreparedStatement.class);
      addTestCase(TestPrimaryKey.class);
      addTestCase(TestRename.class);
      addTestCase(TestResultSet.class); // juliana@211_4: solved bugs with result set dealing.
      addTestCase(TestReIndex2rowId.class);
      addTestCase(TestRowIdAndPurge.class);
      addTestCase(TestRowIterator.class);
      addTestCase(TestSelectClause_AggFunctions.class);
      addTestCase(TestSourcePath.class);
      addTestCase(TestSQLFunctions.class);
      addTestCase(TestTableRecovering.class);
      addTestCase(TestThread.class);
      addTestCase(TestVirtualRecords.class);
      addTestCase(TestWhereClause_Basic.class);
      addTestCase(TestWhereClause_Caseless.class);
      addTestCase(TestWhereClause_Indexes.class);
      Vm.tweak(Vm.TWEAK_DUMP_MEM_STATS, true);
   }

	/** 
	 * Initializes the user interface.
	 */
   public void initUI() // rnovais@570_77
   {
      String appSecretKey = Settings.appSecretKey;
      if (appSecretKey != null && appSecretKey.length() < 4)
         appSecretKey = Settings.appSecretKey = null;   
      useLogger = appSecretKey != null && appSecretKey.charAt(0) == 'y';
      isAscii = appSecretKey != null && appSecretKey.charAt(1) == 'y';
      useCrypto = appSecretKey != null && appSecretKey.charAt(2) == 'y';
      LitebaseConnection.logOnlyChanges = appSecretKey != null && appSecretKey.charAt(3) == 'y';
      if (useLogger)
         LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();
      super.initUI();
      setMenuBar(mbar = new MenuBar(new MenuItem[][]{mbar.getMenuItems()[0], 
                                   {new MenuItem("Config"), new MenuItem("isAscii", isAscii), new MenuItem("useCrypto", useCrypto), 
                                    new MenuItem("Drop all tables")}, 
                                   {new MenuItem("Logs"), new MenuItem("Use Logger", useLogger), 
                                    new MenuItem("Log only changes", LitebaseConnection.logOnlyChanges), new MenuItem("Erase All Loggers")}}));
   }

   /** 
    * Detects the menu items events other than File.
    * 
    * @param event The event being handled.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED && event.target == mbar) 
      {   
         switch (mbar.getSelectedIndex())
         {
            case 101:
               isAscii = !isAscii;
               break;
            case 102:
               useCrypto = !useCrypto;                                                                 
               break;
            case 103: // Drops All Tables. 
               dropAllTables();
               break;
            case 201: // Sets or unsets the logger usage.
               useLogger = !useLogger;
               if (useLogger)
                  LitebaseConnection.logger = LitebaseConnection.getDefaultLogger();
               else
               {
                  if (LitebaseConnection.logger != null)
                  {
                     LitebaseConnection.logger.dispose(true);
                     LitebaseConnection.logger = null;
                  }
               }
               break;
            case 202:
               LitebaseConnection.logOnlyChanges = !LitebaseConnection.logOnlyChanges;
               break;
            case 203: // Deletes all the logger files.
               LitebaseConnection.deleteLogFiles();
         }
         Settings.appSecretKey = (useLogger? "y" : "n") + (isAscii? 'y' : 'n') + (useCrypto? 'y' : 'n') + (LitebaseConnection.logOnlyChanges? 'y' : 'n');
      } 
         
      super.onEvent(event);
   }

   /**
    * Drops all tables used for the testcases. Notice that all the tables in the folders used by the tests and with the creation id "Test"
    * will be erased even if they are from another application.
    */
   private void dropAllTables()
   {
      int i = 7;
      String temporario;
      String tempPath = Convert.appendPath(Settings.appPath, "temp/");
      
      try
      {
         temporario = Convert.appendPath(File.getCardVolume().getPath(), "tempor�rio/");
      }
      catch (IOException exception)
      {
         temporario = Convert.appendPath(Settings.appPath, "tempor�rio/");
      }
      catch (NullPointerException exception)
      {
         temporario = Convert.appendPath(Settings.appPath, "tempor�rio/");
      }

      // The paths used by AllTests.
      String[] paths = {Settings.appPath, Settings.dataPath, tempPath, tempPath + "a/", tempPath + "b/", temporario, "/"};

      // The subfolders used by AllTests.
      File[] folders = new File[7];
         
      while (--i >= 0) // Erases the table files from this application.
      {
         try
         {
            
            if (paths[i] != null)
            {
               folders[i] = new File(paths[i], File.DONT_OPEN);
               LitebaseConnection.dropDatabase("Test", paths[i], -1);
            }
         }
         catch (DriverException exception) {}
         catch (IOException exception) {}
      }

      i = 6;
      while (--i >= 2) // Erases the folders, being careful to erase the empty folders first.
         try
         {
            folders[i].delete();
         } 
         catch (IOException exception) {}
   }
   
   /**
    * Does the tests using the kind of string selected. 
    * 
    * @return A driver connection using ascii or unicode characters depending on which option was made and the application id as the application id
    * used by Litebase.
    */
   static LitebaseConnection getInstance()
   {
      return LitebaseConnection.getInstance(Settings.applicationId, 
                                "chars_type =" + (isAscii? "ascii" : "unicode") + (useCrypto? "; crypto" : "") + "; path = " + Settings.appPath);
   }
   
   /**
    * Does the tests using the kind of string selected. 
    * 
    * @param appCrid The application id given by the user.
    * @return A driver connection using ascii or unicode characters depending on which option was made.
    */
   static LitebaseConnection getInstance(String appCrid)
   {
      return LitebaseConnection.getInstance(appCrid,
                                "chars_type =" + (isAscii? "ascii" : "unicode") + (useCrypto? "; crypto" : "") + "; path = " + Settings.appPath);
   }
   
   /**
    * Does the tests using the kind of string selected. 
    * 
    * @param appCrid The application id given by the user.
    * @param path The path where the tables are to be created.
    * @return A driver connection using ascii or unicode characters depending on which option was made and the path given by the user.
    */
   static LitebaseConnection getInstance(String appCrid, String path)
   {
      return LitebaseConnection.getInstance(appCrid, 
                                            "chars_type =" + (isAscii? "ascii" : "unicode") + (useCrypto? "; crypto" : "") + "; path = " + path);
   }
}

