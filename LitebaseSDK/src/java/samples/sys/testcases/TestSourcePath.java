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
import totalcross.unit.*;
import totalcross.sys.*;
import totalcross.io.*;

/**
 * Tests the use of a different source path for Litebase.
 */
public class TestSourcePath extends TestCase
{
   /**
    * Does the test with an especific path.
    * 
    * @param tempPath
    */
   private void doTest(String tempPath)
   {
   	try
   	{
	      Settings.dataPath = tempPath;
	      LitebaseConnection driver = LitebaseConnection.getInstance("Test"); 
	      if (driver.exists("twonames"))
	         driver.executeUpdate("drop table twonames");
	      try
	      {
	         driver.execute("CREATE TABLE twonames (name1 CHAR(100), name2 CHAR(100))");
	         driver.execute("CREATE INDEX IDX ON twonames(name1)");
	      } catch (AlreadyCreatedException ace) 
	      {
	         fail("Table already created. Exists didnt't work?");
	      }
	      assertEquals(1,driver.executeUpdate("Insert into twonames values ('guich','michelle\\\\')"));
	      ResultSet resultSet = driver.executeQuery("SELECT * FROM twonames");
	      assertTrue(resultSet.next());
	      assertEquals("michelle\\", resultSet.getString(2));
	      resultSet.close();

	      // Now closes the driver and tests again.
	      driver.closeAll();
	      driver = LitebaseConnection.getInstance("Test"); 
	      resultSet = driver.executeQuery("SELECT * FROM twonames");
	      assertTrue(resultSet.next());
	      resultSet.close();
	      driver.closeAll();

	      // Checks if the files exist.
	      File f;
         if (!(f = new File(tempPath + "Test-twonames.db", File.DONT_OPEN)).exists()) 
            fail("File doesn't exist. " + f.getPath());
         if (!(f = new File(tempPath + "Test-twonames.dbo", File.DONT_OPEN)).exists()) 
            fail("File doesn't exist. " + f.getPath());
         if (!(f = new File(tempPath + "Test-twonames$1.idk", File.DONT_OPEN)).exists())
            fail("File doesn't exist. " + f.getPath());
   	}
   	catch (IOException exception)
   	{
   		fail(exception.getMessage());
   	}
   }

   /** 
    * The main test method.
    */
   public void testRun()
   {
      String prevDataPath = Settings.dataPath; // Stores the previous data path.
      doTest(Convert.appendPath(Settings.appPath, "temp/"));
      
      // Tests memory card and folders with stress.
      try
      {
         String path = Convert.appendPath(Settings.appPath, "tempor�rio/");
         File file = new File(path);
         file.createDir();
         file.close();
         
         try
         {
            doTest(Convert.appendPath(File.getCardVolume().getPath(), "tempor�rio/"));
         } 
         catch (IOException exception)
         {
            doTest(path);
         }
         catch (NullPointerException exception)
         {
            doTest(path);
         }
      }
      catch (IOException exception) {}
   	
   	// Tests that the an exception will be thrown if a relative path is used on the device.
   	try
   	{
	      Settings.dataPath = ".";
	      LitebaseConnection.getInstance("Test");
         fail("1");
   	}
   	catch (DriverException exception) {}
   	try
      {
   	   Settings.dataPath = "./temp";
   	   LitebaseConnection.getInstance("Test");
         fail("2");
      } 
      catch (DriverException exception) {}
      try
      {
         Settings.dataPath = "/Litebase/../tables/";
         LitebaseConnection.getInstance("Test");
         fail("3");
      } 
      catch (DriverException exception) {}
      
      // The empty string is a valid dataPath.
      try
      {
         Settings.dataPath = "";
         LitebaseConnection.getInstance("Test").closeAll();    
      }
      catch (DriverException exception) 
      {
         fail("4");
      }
      
      // Null is a valid dataPath.
      try
      {
         Settings.dataPath = null;
         LitebaseConnection.getInstance("Test").closeAll();    
      }
      catch (DriverException exception) 
      {
         fail("5");
      }
      
      // Invalid data paths.
      try
      {
         Settings.dataPath = " ";
         LitebaseConnection.getInstance("Test");
         fail("6");
      }
      catch (DriverException exception) {}
      try
      {
         Settings.dataPath = "  ";
         LitebaseConnection.getInstance("Test");
         fail("7");
      }
      catch (DriverException exception) {}
      
      try
      {
         if (Settings.platform.equals(Settings.ANDROID))
         {
            Settings.dataPath = "/Litebase_DBs/";
            LitebaseConnection.getInstance("Test");
            fail("9");
         }
      } 
      catch (DriverException exception) {}
      Settings.dataPath = prevDataPath; // Restores the data path.
   }
}
