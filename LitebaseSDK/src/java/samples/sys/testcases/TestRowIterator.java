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
import totalcross.sys.Time;
import totalcross.unit.*;
import totalcross.util.*;

/** 
 * Tests the <code>RowIterator</code> class.
 */
public class TestRowIterator extends TestCase
{
   /** 
    * The mais test method.
    */
   public void testRun()
   {
      LitebaseConnection driver = AllTests.getInstance("Test");
      int i = -1,
          updated = 0,
          deleted = 0,
          newed = 0,
          synced = 0;

      try // Drops the table.
      {
         driver.executeUpdate("drop table person");
      } 
      catch (DriverException exception) {}

      try // Creates the table.
      {
         driver.execute("CREATE TABLE PERSON (NAME CHAR(500) NOCASE, ADDRESS char(700), id int, age short, cpf long, salary float, "
                                                                                     + "size double, birth date, arrival datetime, pic blob(20))");
      } 
      catch (AlreadyCreatedException exception)
      {
         fail("1");
      }

      PreparedStatement ps = driver.prepareStatement("INSERT INTO person VALUES (?, ?, ?, ?, ?, ?, ?, '2005/9-12', '2006/08-21 12:08:01:234', ?)");
      
      while (++i < 20)  // Populates the table.
      {
      	ps.setString(0, "Name" + i);
      	ps.setString(1, "Addr" + i);
      	ps.setInt(2, i);
      	ps.setShort(3, (short)(i + 10));
      	ps.setLong(4, 111111111 + i);
      	ps.setFloat(5, 1000.00 + i);
      	ps.setDouble(6, 3.1415 + i);
         ps.setBlob(7, ("Name" + i).getBytes());
         assertEquals(1, ps.executeUpdate());
      }

      RowIterator it = driver.getRowIterator("PERSON"); // Gets the row iterator.

      i = 0;
      while (it.next())
      {
         // Confirms the row id and that the NEW attribute is set.
         assertEquals(++i, it.rowid);
         assertEquals(RowIterator.ROW_ATTR_NEW, it.attr);
         
         if (5 <= i && i <= 15) // Simulates that these rows have been synchronized.
            it.setSynced();
      }

      // Tests the nextNotSynced().
      it.reset();
      i = 0;
      while (it.nextNotSynced())
         i++;
      assertEquals(9, i);

      // Changes some rows.
      driver.executeUpdate("update person set name ='guilherme' where name like '%1'");
      driver.executeUpdate("delete person where rowid = 2");

      it.reset();
      i = 0;

      while (it.next()) // Iterates through the iterator.
      {
         // Checks the values of the iterator.
      	if (i % 10 != 1)
      		assertEquals("Name" + i, it.getString(1));
         else
      		assertEquals("guilherme", it.getString(1));
      	assertFalse(it.isNull(1));
      	assertEquals("Addr" + i, it.getString(2));
      	assertEquals(i, it.getInt(3));
      	assertEquals(10 + i, it.getShort(4));
      	assertEquals(111111111 + i, it.getLong(5));
      	assertEquals(1000.00 + i, it.getFloat(6), 0.0000001);
      	assertEquals(3.1415 + i, it.getDouble(7), 0.0000001);
      	try
      	{
      	   assertEquals(new Date(20050912), it.getDate(8));
      	} 
      	catch (InvalidDateException exception) {}
      	assertEquals(new Time(2006,8,21,12,8,01,234), it.getDateTime(9));
      	assertEquals(("Name" + i++).getBytes(), it.getBlob(10));

         // Index out of bounds.
      	try 
      	{
      		it.getString(-1);
            fail("2");
      	} 
      	catch (IllegalArgumentException exception) {}
      	try
      	{
      		it.getString(11);
            fail("3");
      	} 
      	catch (IllegalArgumentException exception) {}

      	try // Wrong type
      	{
      		it.getBlob(1);
            fail("4");
      	} 
      	catch (DriverException exception) {}

      	switch (it.attr) // Tests the attributtes of the row.
         {
            case RowIterator.ROW_ATTR_DELETED:
               it.setSynced();
               deleted++;
               break;
            case RowIterator.ROW_ATTR_SYNCED:
               synced++;
               break;
            case RowIterator.ROW_ATTR_NEW:
               newed++;
               break;
            case RowIterator.ROW_ATTR_UPDATED:
               updated++;
         }
      }

      assertEquals(updated, 1);
      assertEquals(deleted, 1);
      assertEquals(newed, 8);
      assertEquals(synced, 10);
      it.close();

      // The row iterator can't be used if it is closed.
      // close() and reset() need not be checked.
      try
      {
         it.next();
         fail("5");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         it.getString(1);
         fail("6");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         it.nextNotSynced();
         fail("7");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         it.setSynced();
         fail("8");
      } 
      catch (IllegalStateException exception) {}
      try
      {
         it.close();
         fail("9"); 
      }
      catch (IllegalStateException exception) {}

      driver.closeAll();
      deleted = synced = newed = updated = 0;
      driver = AllTests.getInstance("Test");

      // Inserts a new record.
      driver.executeUpdate("INSERT INTO PERSON (NAME) VALUES ('Juliana')");

      // Checks the attributes again.
      it = driver.getRowIterator("PERSON"); // Gets the iterator for the table.
      while (it.next()) // Iterates through the iterator.
         switch (it.attr) // Tests the attributtes of the row.
         {
            case RowIterator.ROW_ATTR_DELETED:
               it.setSynced();
               deleted++;
               break;
            case RowIterator.ROW_ATTR_SYNCED:
               synced++;
               break;
            case RowIterator.ROW_ATTR_NEW:
               newed++;
               break;
            case RowIterator.ROW_ATTR_UPDATED:
               updated++;
         }
      assertEquals(updated, 1);
      assertEquals(deleted, 1); // The deleted row is still here.
      assertEquals(newed, 9); // One more new row.
      assertEquals(synced, 10);

      deleted = synced = newed = updated = 0;
      it.close();
      driver.purge("Person");  // Eliminates the deleted row.
      it = driver.getRowIterator("person"); // It is necessary to get a new one.

      // Checks agaim.
      while (it.next()) // Iterates through the iterator.
         switch (it.attr) // Tests the attributtes of the row.
         {
            case RowIterator.ROW_ATTR_DELETED:
               it.setSynced();
               deleted++;
               break;
            case RowIterator.ROW_ATTR_SYNCED:
               synced++;
               break;
            case RowIterator.ROW_ATTR_NEW:
               newed++;
               break;
            case RowIterator.ROW_ATTR_UPDATED:
               updated++;
         }
      assertEquals(updated, 1);
      assertEquals(deleted, 0); // The deleted row is not here anymore.
      assertEquals(newed, 9);
      assertEquals(synced, 10);
      
      // Tests row iterator with null values.
      assertEquals(20, driver.executeUpdate("update person set name = null"));
      it = driver.getRowIterator("person"); // It is necessary to get a new one.
      while (it.next()) // Iterates through the iterator.
      {
         assertNull(it.getString(1));
         assertTrue(it.isNull(1));
         assertTrue(it.attr == RowIterator.ROW_ATTR_UPDATED || it.attr == RowIterator.ROW_ATTR_NEW);
      }

      it.reset();
      
      assertEquals(1, driver.executeUpdate("delete person where rowid = 1"));
      
      // All non-deleted rows are marked as new.
      while (it.next())
         it.setNotSynced();
      it.reset();
      while (it.next())
         if (it.rowid == 1)
            assertEquals(it.attr, RowIterator.ROW_ATTR_DELETED);
         else
            assertEquals(it.attr, RowIterator.ROW_ATTR_NEW);
      
      driver.closeAll();
      it.reset();
      
      // The row iterator must not be used with the driver closed.
      try
      {
         it.next(); 
         fail("10");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.nextNotSynced(); 
         fail("11");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.setSynced(); 
         fail("12");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getShort(4); 
         fail("13");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getInt(3); 
         fail("14");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getLong(5); 
         fail("15");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getFloat(6); 
         fail("16");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getDouble(7); 
         fail("17");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getString(1); 
         fail("18");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getBlob(10); 
         fail("19");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getDate(8); 
         fail("20");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.getDateTime(9); 
         fail("21");
      }
      catch (IllegalStateException exception) {}
      try
      {
         it.isNull(1); 
         fail("22");
      }
      catch (IllegalStateException exception) {}
      
      try
      {
         it.close();
         fail("23"); 
      }
      catch (IllegalStateException exception) {}
   }
}
