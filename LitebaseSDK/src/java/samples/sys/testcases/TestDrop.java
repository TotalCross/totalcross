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
import totalcross.io.*;
import totalcross.unit.TestCase;

/**
 * Tests drop table and drop index.
 */
public class TestDrop extends TestCase
{
   /** 
    * The path where the table files are stored.
    */
	private String path;

	/** 
	 * The main method of the test.
    */
	public void testRun()
   {
		LitebaseConnection driver = AllTests.getInstance("Test");

		// Gets and normalizes the path.
		path = driver.getSourcePath();
		if (!path.endsWith("/"))
		   path += "/";
		
		if (driver.exists("person")) // Drops the table if it exists.
			driver.executeUpdate("drop table person");

		driver.execute("create table person (id int primary key)");

	   // No table file can exist after dropping a table.
		driver.executeUpdate("drop table person");
		assertFalse(TableFileExist());

		driver.execute("create table person (id int)");
		driver.executeUpdate("alter table person add primary key(rowid)");

		driver.closeAll(); // Closes the table after creating it.

	   // No table file can exist after dropping a table.
		(driver = AllTests.getInstance("Test")).executeUpdate("drop table person");
		assertFalse(TableFileExist());

		driver.execute("create table person (id int)");
		driver.executeUpdate("alter table person add primary key (rowid)");

		try // Only drop primary key can be used to drop a primary key.
		{
		   driver.executeUpdate("drop index rowid on person");
		   fail("1");
		} 
		catch (DriverException exception) {}
		assertTrue(IndexExist());
		driver.executeUpdate("alter table person drop primary key");
		assertFalse(IndexExist());

		driver.executeUpdate("alter table person add primary key (id)");

		// Closes the table after adding the primary key again.
		driver.closeAll();
		driver = AllTests.getInstance("Test");

		try // Only drop primary key can be used to drop a primary key.
		{
		   driver.executeUpdate("drop index id on person");
		} 
		catch (DriverException exception) {}
		assertTrue(IndexExist());
		driver.executeUpdate("alter table person drop primary key");
		assertFalse(IndexExist());

		driver.executeUpdate("alter table person add primary key(id)");
		driver.execute("create index idx on person(rowid)");

	   // Only drop primary key can be used to drop a primary key.
		assertEquals(1, driver.executeUpdate("drop index * on person"));
		assertTrue(IndexExist());
		driver.closeAll();
      driver = AllTests.getInstance("Test");
      assertEquals(0, driver.executeUpdate("drop index * on person"));
		driver.executeUpdate("alter table person drop primary key");
		assertFalse(IndexExist());

		driver.executeUpdate("alter table person add primary key(rowid)");
		driver.closeAll();
		driver = AllTests.getInstance("Test");
		driver.executeUpdate("drop index * on person");
		assertTrue(IndexExist());
		driver.executeUpdate("alter table person drop primary key");
		assertFalse(IndexExist());

		// rowid in the primary key.
		driver.executeUpdate("alter table person add primary key(id, rowid)");
		try  // Only drop primary key can be used to drop a primary key.
		{
		   driver.executeUpdate("drop index rowid, id on person");
		   fail("1");
		} 
		catch (DriverException exception) {}
		assertTrue(IndexExist());

		try  // Only drop primary key can be used to drop a primary key.
		{
		   driver.executeUpdate("drop index id, rowid on person");
		   fail("2");
		} 
		catch (DriverException exception) {}

		driver.executeUpdate("alter table person drop primary key");
		assertFalse(IndexExist());

		// Closes the table after adding the primary key.
		driver.executeUpdate("alter table person add primary key(rowid, id)");
		driver.closeAll();
		driver = AllTests.getInstance("Test");

		try  // Only drop primary key can be used to drop a primary key.
		{
		   driver.executeUpdate("drop index id, rowid on person");
		   fail("3");
		} 
		catch (DriverException exception) {}
		assertTrue(IndexExist());

		try  // Only drop primary key can be used to drop a primary key.
		{
		   driver.executeUpdate("drop index rowid, id on person");
		   fail("4");
		} 
		catch (DriverException exception) {}
		assertTrue(IndexExist());

		driver.executeUpdate("alter table person drop primary key");
		assertFalse(IndexExist());
		
		// Drop table can't erase a table whose name is the prefix of the table being dropped.
		if (driver.exists("person2")) // Drops the table if it exists.
         driver.executeUpdate("drop table person2");
      driver.execute("create table person2 (id int primary key)");
      driver.executeUpdate("drop table person2");
      assertTrue(TableFileExist());
   
		driver.closeAll();
   }

	/** 
	 * Indicates if some table file exists.
	 *
	 * @return <code>true</code> if sone table file exists; <code>false</code>, otherwise.
	 */
	private boolean TableFileExist()
	{
		try
		{
			return new File(path + "Test-person.db", File.DONT_OPEN).exists() || new File(path + "Test-person.dbo", File.DONT_OPEN).exists()
	          || new File(path + "Test-person$0.idk", File.DONT_OPEN).exists() || new File(path + "Test-person$1.idk", File.DONT_OPEN).exists()
	          || new File(path + "Test-person&1.idk", File.DONT_OPEN).exists() || new File(path + "Test-person&2.idk", File.DONT_OPEN).exists();
		} 
		catch (IOException exception)
		{
			return true;
		}
	}

	/** 
	 * Indicates if some index file exists.
    *
    * @return <code>true</code> if sone index file exists; <code>false</code>, otherwise.
    */
	private boolean IndexExist()
	{
		try
		{
			return new File(path + "Test-person$0.idk", File.DONT_OPEN).exists() || new File(path + "Test-person$1.idk", File.DONT_OPEN).exists()
			    || new File(path + "Test-person&1.idk", File.DONT_OPEN).exists() || new File(path + "Test-person&2.idk", File.DONT_OPEN).exists();
		} 
		catch (IOException exception)
		{
			return true;
		}
	}
}
