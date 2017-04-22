/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package tc.samples.api.io;

import tc.samples.api.*;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.gfx.*;
import totalcross.util.*;

/** Creating pdb files in TotalCross - the Definitive Guide. */

public class PDBFileSample extends BaseContainer implements Runnable
{
   static final String CREATOR_ID = Settings.applicationId;
   static final String TYPEC = "TEST";
   static final String PDBFILE_NAME = TYPEC + "." + CREATOR_ID + "." + TYPEC; // must use this format - or you will not be able to read this database from conduits

   private PDBFile cat;
   private ResizeRecord rs;
   private DataStream ds;

   public void initUI()
   {
      super.initUI();
      addLog(LEFT, TOP, FILL, FILL, null);
      MainWindow.getMainWindow().runOnMainThread(this); // allow animation
   }
   
   public void run()
   {
      lblog.ihtBackColors = new IntHashtable(10);

      try
      {
         testCreate();
      }
      catch (totalcross.io.IOException e1)
      {
         log("testCreate FAILED: " + e1.getMessage());
         e1.printStackTrace();
      }

      try
      {
         if (openCatalog())
         {
            writeRecords();
            readRecords();
            insertRecords();
            readRecords();
            ds.close(); // ds already closes the catalog
         }
      }
      catch (totalcross.io.IOException e)
      {
         e.printStackTrace();
      }
   }

   private void testCreate() throws totalcross.io.IOException
   {
      log("Testing PDBFile constructor...");
      try
      {
         cat = new PDBFile(null, PDBFile.CREATE_EMPTY);
         log(">>> Should have thrown a NullPointerException");
      }
      catch (NullPointerException e)
      {
      }

      try
      {
         cat = new PDBFile(TYPEC + "." + CREATOR_ID + "." + "TES", PDBFile.CREATE_EMPTY);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }

      try
      {
         cat = new PDBFile(TYPEC + "." + CREATOR_ID + ".." + "TEST", PDBFile.CREATE_EMPTY);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }

      try
      {
         cat = new PDBFile(TYPEC + "." + "TTT" + "." + "TEST", PDBFile.CREATE_EMPTY);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }

      cat = new PDBFile("CREATE_TEST" + "." + CREATOR_ID + "." + "TEST", PDBFile.CREATE_EMPTY);
      cat.close();

      try
      {
         cat = new PDBFile("CREATE_TEST" + "." + CREATOR_ID + "." + "TEST", 0);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      log("PDBFile construtor OK");

      log("Deleting files that may have been left by a previous test...");
      try
      {
         cat = new PDBFile("RENAME_TEST" + "." + CREATOR_ID + "." + "TEST", PDBFile.READ_WRITE);
         cat.delete();
      }
      catch (totalcross.io.FileNotFoundException e)
      {
      }

      try
      {
         cat = new PDBFile("CREATE2_TEST" + "." + CREATOR_ID + "." + "TEST", PDBFile.READ_WRITE);
         cat.delete();
      }
      catch (totalcross.io.FileNotFoundException e)
      {
      }

      log("Files deleted.");

      log("Open and rename file...");
      cat = new PDBFile("CREATE_TEST" + "." + CREATOR_ID + "." + "TEST", PDBFile.READ_WRITE);
      cat.rename("RENAME_TEST" + "." + CREATOR_ID + "." + "TEST");
      cat.close();

      cat = new PDBFile("RENAME_TEST" + "." + CREATOR_ID + "." + "TEST", PDBFile.READ_WRITE);
      log("File renamed");

      log("Testing records manipulation...");
      try
      {
         cat.addRecord(-1);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      try
      {
         cat.addRecord(76543);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      cat.addRecord(64);
      log("Added first record");
      cat.rename("CREATE2_TEST" + "." + CREATOR_ID + "." + "TEST");
      cat.addRecord(256);
      log("Added second record");
      try
      {
         cat.resizeRecord(-1);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      try
      {
         cat.resizeRecord(76543);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      cat.resizeRecord(128);
      log("Resized second record");

      try
      {
         cat.setRecordPos(-2);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      cat.setRecordPos(0);
      log("Moved to first record");
      try
      {
         cat.resizeRecord(-1);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      try
      {
         cat.resizeRecord(76543);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }
      cat.resizeRecord(32);
      log("Resized first record");

      if (cat.getRecordCount() != 2)
      {
         log(">>> getRecordCount should have returned 2");
         return;
      }

      cat.deleteRecord();
      if (cat.getRecordCount() != 1)
      {
         log(">>> getRecordCount should have returned 1");
         return;
      }

      try
      {
         cat.deleteRecord();
         log(">>> Should have thrown an IOException");
      }
      catch (totalcross.io.IOException e)
      {
      }

      String sbuf = "PDBFile Test";
      byte[] buf = sbuf.getBytes();

      cat.setRecordPos(0);
      log("Moved to first record.");
      int bufLen = buf.length;
      int bytesWritten1 = cat.writeBytes(buf, 0, buf.length);
      if (bufLen == bytesWritten1)
         log("Wrote bytes to record");
      else
         log(">>> write bytes: " + bytesWritten1);

      cat.setRecordOffset(0);
      log("Moved cursor");
      buf = new byte[128];
      int bytesRead = cat.readBytes(buf, 0, bytesWritten1);
      if (bytesRead == bufLen)
         log("Read bytes from record");
      else
         log(">>> read bytes: " + bytesRead);
      String s = new String(buf, 0, bytesRead);
      log("Record content: " + s);

      try
      {
         cat.skipBytes(-(bytesRead * 2));
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }

      try
      {
         cat.skipBytes(65536);
         log(">>> Should have thrown an IllegalArgumentIOException");
      }
      catch (totalcross.io.IllegalArgumentIOException e)
      {
      }

      cat.skipBytes(-bytesRead);
      bytesRead = cat.readBytes(buf, 0, bytesWritten1);
      if (bytesRead == bufLen)
         log("Read bytes from record again");
      else
         log(">>> read bytes: " + bytesRead);
      s = new String(buf, 0, bytesRead);
      log("Record content: " + s);

      cat.skipBytes(16);
      sbuf = "skipped 16 bytes";
      buf = sbuf.getBytes();

      bufLen = buf.length;
      int bytesWritten2 = cat.writeBytes(buf, 0, buf.length);
      if (bufLen == bytesWritten2)
         log("Wrote more bytes to record");
      else
         log(">>> write bytes: " + bytesWritten2);

      cat.setRecordOffset(0);
      buf = new byte[128];
      bytesRead = cat.readBytes(buf, 0, bytesWritten1 + 16 + bytesWritten2);
      if (bytesRead == bytesWritten1 + 16 + bytesWritten2)
         log("Read bytes from record again: " + bytesRead);
      else
         log(">>> read bytes: " + bytesRead);
      s = new String(buf, 0, bytesRead);
      log("Record content: " + s);
      log("Record content size: " + s.length());

      if (cat.getRecordOffset() != 44)
         log(">>> getRecordOffset should have returned 44");
      cat.addRecord(64);
      cat.addRecord(128);
      cat.setRecordPos(1);
      if (cat.getRecordOffset() != 0)
         log(">>> getRecordOffset should have returned 0");

      cat.setRecordPos(-1);
      if (cat.getRecordCount() != 3)
         log(">>> getRecordCount should have returned 3");

      cat.setRecordPos(0);
      int aux = cat.getRecordSize();
      if (aux != 128)
         log(">>> getRecordSize should have returned 128: " + aux);

      cat.readBytes(buf, 0, bytesRead);
      log("Checking if first record is ok: " + new String(buf, 0, bytesRead));

      cat.setRecordPos(2);
      cat.inspectRecord(buf, 0, 0);
      log("Checking with inspect: " + new String(buf));

      cat.addRecord(16, 1);
      cat.addRecord(20, 1);
      cat.addRecord(16);
      cat.addRecord(20);

      cat.setRecordAttributes(2, PDBFile.REC_ATTR_SECRET);
      if (cat.getRecordAttributes(2) == PDBFile.REC_ATTR_SECRET)
         log("Get and set record attributes - ok");
      else
         log(">>> Get or set record attributes failed");

      s = "teste!";
      buf = s.getBytes();
      cat.writeBytes(buf, 0, buf.length);

      s = "skipped";
      buf = s.getBytes();
      int searchResult = cat.searchBytes(buf, buf.length, 0);
      if (searchResult == -1)
         log("Search bytes 1 - ok");
      else
         log(">>> Search bytes 1 failed");

      cat.setRecordPos(0);
      searchResult = cat.searchBytes(buf, buf.length, 28);
      if (searchResult == 0)
         log("Search bytes 2 - ok");
      else
         log(">>> Search bytes 2 failed");

      s = "teste";
      buf = s.getBytes();
      searchResult = cat.searchBytes(buf, buf.length, 0);
      if (searchResult == 6)
         log("Search bytes 3 - ok");
      else
         log(">>> Search bytes 3 failed");

      log("Finished testing record operations.");
      log("Testing get and set attributes");
      cat.setAttributes(0);
      if (cat.getAttributes() != 0)
         log(">>> First getAttributes failed");

      cat.setAttributes(PDBFile.DB_ATTR_BACKUP);
      if (cat.getAttributes() != PDBFile.DB_ATTR_BACKUP)
         log(">>> Second getAttributes failed");

      cat.close();

      log("LIST PDBs");
      String list[] = PDBFile.listPDBs();
      if (list != null)
         for (int i = 0; i < list.length; i++)
            if (list[i] != null)
               log(list[i]);
            else
               log("null " + i);
   }
   
   void log(String s)
   {
      BaseContainer.log(s);
      if (s.startsWith(">"))
         lblog.ihtBackColors.put(lblog.size()-1, Color.RED);
   }

   /**
    * create and open the catalog
    *
    * @throws totalcross.io.IOException
    */
   public boolean openCatalog() throws totalcross.io.IOException
   {
      cat = new PDBFile(PDBFILE_NAME, PDBFile.CREATE_EMPTY);
      // create the streams
      rs = new ResizeRecord(cat, 512); // 512 is the initial size for each record. Cannot be 0!
      ds = new DataStream(rs);

      return true;
   }

   /** write some records */
   public void writeRecords() throws totalcross.io.IOException
   {
      String[] keys = {"a", "b", "c", "d", "e", "f", "g"};
      String[] values = {"add", "boss", "cow", "database", "equals", "fool", "guich"};

      log("* Writing records...");
      for (int i = 0; i < keys.length; i++)
      {
         rs.startRecord();
         // append the record
         ds.writeString(keys[i]);
         ds.writeString(values[i]);
         rs.endRecord();
      }

      log("* Records written.");
   }

   /** read all records in catalog */
   public void readRecords() throws totalcross.io.IOException
   {
      log("* Reading records");
      int numRecords = 0;
      numRecords = cat.getRecordCount();
      log("* Number of records: " + numRecords);
      for (int i = 0; i < numRecords; i++)
      {
         cat.setRecordPos(i);
         String key = ds.readString();
         String value = ds.readString();
         log(i + ": " + key + "," + value); // just show them
      }
   }

   public void insertRecords() throws totalcross.io.IOException
   {
      log("* Inserting records");
      // insert a new record before the 3rd record (letter "c")
      rs.startRecord(2); // records start from 0
      ds.writeString("bb");
      ds.writeString("because");
      rs.endRecord();

      // substitute the 4th record (now letter "c")
      /*
       * same of: cat.setRecordPos(3); cat.deleteRecord(); rs.startRecord(3);
       */
      if (rs.restartRecord(3))
      {
         ds.writeString("c");
         ds.writeString("comes");
         rs.endRecord();
      }

      // appends a new record to the end of the catalog
      rs.startRecord(); // records start from 0
      ds.writeString("TC");
      ds.writeString("TotalCross!");
      rs.endRecord();
   }
}
