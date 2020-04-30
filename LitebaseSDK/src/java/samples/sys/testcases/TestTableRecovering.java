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
import totalcross.sys.*;
import totalcross.unit.TestCase;
import totalcross.util.Random;

/**
 * Tests <code>LitebaseConnection.recoverTable()</code>.
 */
public class TestTableRecovering extends TestCase
{  
   /**
    * The main method of the test.
    */
   public void testRun()
   {
      int record = 11,
          i,
          pos;
      PreparedStatement prepared;
      Random rand = new Random();
      File dbFile;
      byte[] garbage = "garbage".getBytes();
      LitebaseConnection driver = AllTests.getInstance();
      
      // Gets the files paths.
      String tablePath = driver.getSourcePath() + "gqVX-person.db";
      driver.closeAll();
      
      byte[] oneByte = new byte[1];
      byte[] blankBuffer = new byte[1024];
      String message;
      
      if (AllTests.useCrypto)
      {
         i = garbage.length;
         while (--i >= 0)
            garbage[i] ^= 0xAA;
      }   
      
      try
      {
         while (--record >= 0) // Repeats the test corrupting a different record, from the first (0) till after the last (10).
         {
            // Drops, creates and populates a table to be used in the test.
            driver = AllTests.getInstance();
            if (driver.exists("person"))
               driver.executeUpdate("drop table person");
            
            try // The table does not exist yet.
            {
               driver.isTableProperlyClosed("person");
               fail("1");
            }
            catch (DriverException exception) {}
            
            driver.execute("create table person (id int not null, name char(30) default 'Maria', cpf long not null, photo blob(2), gender char(1), " 
                         + "birth datetime, primary key(id, cpf))"); 
            driver.execute("create index idx on person(rowid)");
            driver.execute("create index idx on person(name, gender, birth)");
            prepared = driver.prepareStatement("insert into person values(?, ?, ?, ?, ?, ?)");
            i = 10;
            
            assertTrue(driver.isTableProperlyClosed("person"));
            
            while (--i >= 0)
            {
               prepared.setInt(0, i);
               prepared.setString(1, i + "");
               prepared.setLong(2, i);
               prepared.setBlob(3, ("name" + i).getBytes());
               prepared.setString(4, (i % 2 == 0)? "F" : "M");
               prepared.setDateTime(5, new Time());
               prepared.executeUpdate();
            }
            driver.executeUpdate("update person set id = 1");
            driver.purge("person");
            driver.closeAll(); // Must close the table before recovering it.
            driver = AllTests.getInstance();
            
            try // There is nothing to be recovered. 
            {
               driver.recoverTable("peRson");
               fail("2");
            }
            catch (DriverException exception) {}
            dbFile = new File(tablePath, File.READ_WRITE); // The table is closed after recovering it.
            
            // Pretends that the table was not closed correctly.   
            dbFile.setPos(6);
            dbFile.readBytes(oneByte, 0, 1);
            
            if (AllTests.useCrypto)
               oneByte[0] ^= 0xAA;
            oneByte[0] = (byte)(oneByte[0] & 2);
            if (AllTests.useCrypto)
               oneByte[0] ^= 0xAA;
            dbFile.setPos(6);
            dbFile.writeBytes(oneByte, 0, 1);
            
            // Corrupts the table database file.
            if ((pos = 512 + 41 * record + rand.nextInt(33)) + 7 > dbFile.getSize())
               dbFile.setSize(pos + 7);
            dbFile.setPos(pos);
            dbFile.writeBytes(garbage);
            dbFile.close();
            
            try // It is not possible to open a corrupted table.
            {
               driver.executeQuery("select * from person");
               fail("3");
            } 
            catch (TableNotClosedException exception) {}
            
            assertFalse(driver.isTableProperlyClosed("person"));
            assertEquals(record < 10, driver.recoverTable("person")); // If the corruption occurs after the last record, nothing needs to be recovered.
            assertTrue(driver.isTableProperlyClosed("person"));
            
            ResultSet resultSet = driver.executeQuery("select * from person");
            
            // If the corruption occurs after the last records, all the records are available.
            assertEquals(record < 10? 9 : 10, resultSet.getRowCount()); 
            
            resultSet.close();
            
            // Posix platforms share file handlers.
            if (!(Settings.platform.equals(Settings.ANDROID) || Settings.platform.equals(Settings.IPHONE)))
            {
               assertTrue(driver.isTableProperlyClosed("person"));
               try // Table being used.
               {
                  driver.recoverTable("peRson");
                  fail("4");
               }
               catch (DriverException exception) {}
               try // Table being used.
               {
                  driver.convert("peRson");
                  fail("5");
               }
               catch (DriverException exception) {}
            }
            
            // Convert must fail because the table version used is the current one.
            driver.closeAll();
            driver = AllTests.getInstance();
            try 
            {
               driver.convert("person");
               fail("6");
            }
            catch (DriverException exception) {}
            
            driver.executeUpdate("drop table person");
            
            new File(tablePath, File.CREATE_EMPTY).close();
            new File(tablePath + 'o', File.CREATE_EMPTY).close();
            try // Empty file: table corrupted.
            {
               driver.recoverTable("person");
               fail("7");
            }
            catch (DriverException exception) 
            {
               assertTrue((message = exception.getMessage()).indexOf("corrupted") != -1 || message.indexOf("format") != -1 
                                                                                        || message.indexOf("read") != -1);
            }
            try // Empty file: table corrupted.
            {
               driver.recoverTable("person");
               fail("8");
            }
            catch (DriverException exception) 
            {
               assertTrue((message = exception.getMessage()).indexOf("corrupted") != -1 || message.indexOf("format") != -1 
                                                                                        || message.indexOf("read") != -1);
            }
            
            File file = new File(tablePath, File.CREATE_EMPTY);
            file.setSize(1024);
            file.writeBytes(blankBuffer);
            file.close();
            assertFalse(driver.isTableProperlyClosed("person"));
            try // Blank file: table corrupted.
            {
               driver.recoverTable("person");
               fail("9");
            }
            catch (DriverException exception) 
            {
               assertTrue((message = exception.getMessage()).indexOf("corrupted") != -1 || message.indexOf("format") != -1);
            }            
            
            // Erases the file.
            file = new File(tablePath, File.DONT_OPEN);
            file.delete();
            
            driver.closeAll();
         }
      }
      catch (IOException exception) 
      {
         fail("10");
      }
   }
}
