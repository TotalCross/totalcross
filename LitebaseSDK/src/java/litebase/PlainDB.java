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

package litebase;

import totalcross.io.*;
import totalcross.sys.Convert;
import totalcross.util.*;

/**
 * This has the implementation of a database in a plain binary file. The data and the metadata (header) is written in one file (.db). The strings and  
 * the blobs are written in the .dbo file. The current number of records inside the database is discovered only when the database is open by getting 
 * its size and discounting the header size. This has a double advantage: it is not necessary to waste space storing the current record count, and it 
 * is not needed to save the record count at each insertion. 
 * 
 * This also has the implementation of a temporary database for <code>ResultSet</code> tables.
 */
class PlainDB
{
   /**
    * The minimum space for composed indices and primary key in the table header.
    */
   private static final int COMP_IDX_PK_SIZE = 64;

   /**
    * The database (.db) file.
    */
   XFile db;

   /**
    * The strings and blobs (.dbo) file.
    */
   XFile dbo;

   /**
    * The table header size.
    */
   int headerSize = 512;

   /**
    * The size of a row.
    */
   int rowSize;

   /**
    * The number of rows.
    */
   int rowCount;

   /**
    * The current row increment when inserting data on the table.
    */
   int rowInc = Utils.DEFAULT_ROW_INC;

   /**
    * The number of rows available.
    */
   int rowAvail;

   /**
    * An stream to write and read data from the table.
    */
   ByteArrayStream bas;

   /**
    * A buffer to read a row.
    */
   byte[] basbuf;

   /**
    * The data stream to read data from the table.
    */
   DataStreamLB basds; // juliana@253_8: now Litebase supports weak cryptography.

   /**
    * The data stream to read from the .dbo.
    */
   DataStreamLB dsdbo; // juliana@253_8: now Litebase supports weak cryptography.

   /**
    * The table name.
    */
   String name;
   
   /**
    * Indicates if the tables of this connection use ascii or unicode strings.
    */
   boolean isAscii; // juliana@210_2: now Litebase supports tables with ascii strings.
   
   /**
    * Indicates if the table uses cryptography.
    */
   boolean useCrypto; // juliana@253_8: now Litebase supports weak cryptography.
   
   /**
    * Indicates whether a table used the wrong cryptography format.
    */
   boolean useOldCrypto;
   
   /**
    * The driver where this table file was created.
    */
   LitebaseConnection driver;
   
   /**
    * Creates a new <code>PlainDB</code>, loading or creating the table with the given name or creating a temporary table.
    *
    * @param name The name of the table.
    * @param sourcePath The path where the table is to be open or created.
    * @param create Defines if the file will be created if it doesn't exist.
    * @throws IOException If an internal method throws it. 
    */
   PlainDB(String aName, String sourcePath, boolean create) throws IOException
   {
      // rnovais@101_1
      // Opens or creates the .db file.
      db = openFile(aName == null? null : (name = aName) + NormalFile.DB_EXT, create, sourcePath);

      // Opens or creates the .dbo file.
      try
      { 
         dbo = openFile(aName == null? null : aName + NormalFile.DBO_EXT, create, sourcePath);
      }
      catch (IOException exception) // juliana@222_7: .db should be closed if .dbo cannot be openned on desktop and BlackBerry.
      {
         db.close();
         throw exception;
      }
   }

   /**
    * Sets the size of a row.
    * 
    * @param newRowSize The new row size.
    * @param buffer A buffer. 
    */
   void setRowSize(int newRowSize, byte[] buffer)
   {
      rowSize = newRowSize;
      
      // juliana@253_8: now Litebase supports weak cryptography.
      basds = new DataStreamLB(bas = new ByteArrayStream(basbuf = buffer), useCrypto);
      dsdbo = new DataStreamLB(dbo, useCrypto);
      
      int size = db.size - headerSize;
      if (size >= 0)
         rowCount = size / rowSize; // Finds how many records are there.
   }

   // rnovais@570_75
   /**
    * Opens or creates a file, which can be a memory file or a disk file.
    * 
    * @param name The file name.
    * @param create Indicates if the file is to be created.
    * @param sourcePath The file path.
    * @return The file handle.
    * @throws IOException If an internal method throws it.
    */
   XFile openFile(String name, boolean create, String sourcePath) throws IOException
   {
      if (name == null)
         return new MemoryFile();
      else
         return new NormalFile(Utils.getFullFileName(name, sourcePath), create, -1);
   }

   /**
    * Adds a new record. The file pointer is positioned in the record's beginning so that the data can be written. Usually the record is first 
    * added, then the contents are written.
    * 
    * @throws IOException If an internal method throws it. 
    */
   void add() throws IOException
   {
      if (--rowAvail <= 0) // Checks if there are no more space pre-allocated.
      {
         db.growTo((rowCount + rowInc) * rowSize + headerSize);
         rowAvail = rowInc;
      }
      db.setPos(headerSize + rowCount * rowSize); // Sets the position to the start of the record.
      bas.reset(); // Prepares the buffer to be written.
   }

   /**
    * Writes the data of the bas into the current file position.
    * 
    * @throws IOException If an internal method throws it.
    */
   void write() throws IOException
   {
      rowCount++;
      db.writeBytes(basbuf, 0, rowSize);
   }

   /**
    * Reads a row at the given position into bas.
    * 
    * @param pos The .db file record to be read.
    * @throws IOException If an internal method throws it.
    */
   void read(int pos) throws IOException
   {
      db.setPos(headerSize + pos * rowSize);
      bas.reset();
      db.readBytes(basbuf, 0, rowSize);
   }

   /**
    * Rewrites a row at the given position.
    * 
    * @param pos The .db file record to be read.
    * @throws IOException If an internal method throws it.
    */
   void rewrite(int pos) throws IOException
   {
      db.setPos(headerSize + pos * rowSize);
      db.writeBytes(basbuf, 0, rowSize);
   }

   // juliana@253_19: corrected a possible table corruption after a purge or a rename table only on Java SE.
   /**
    * Renames the files to the new given name.
    * 
    * @param newName The new table name.
    * @param sourcePath The files path.
    * @throws IOException If an internal method throws it.
    */
   void rename(String newName, String sourcePath) throws IOException
   {
      String newFullName = Utils.getFullFileName(newName, sourcePath) + NormalFile.DB_EXT;
      
      ((NormalFile)db).rename(newFullName);  // rnovais@570_75  // Renames the .db file.
      try
      {
         ((NormalFile)dbo).rename(newFullName + 'o'); // Renames the .dbo file. // rnovais@570_75
      }
      catch (IOException exception) // Unlikely to occur
      {
         // If the file could not be renamed, the .db file should be renamed back.
         newFullName = Utils.getFullFileName(name, sourcePath) + NormalFile.DB_EXT;
         ((NormalFile)db).rename(newFullName);
         throw exception;
      }

      name = newName;      
   }

   /**
    * Writes the given metadata to the header of the .db file.
    * 
    * @param buf The data to be written.
    * @param len The data length.
    * @throws IOException If an internal method throws it.
    */
   void writeMetaData(byte[] buf, int len) throws IOException
   {
      XFile dbFile = db;
      
      if (dbFile.size == 0) // The metadata size must have a free space for future composed indices or composed primary key.
      {
         int size = headerSize;
         
         // juliana@230_7: corrected a possible exception or crash when the table has too many columns and composed indices or PKs.
         while (len > size || size - len < COMP_IDX_PK_SIZE)
            size <<= 1;
         dbFile.growTo(headerSize = size);
         
         // juliana@223_15: solved a bug that could corrupt tables created with a very large metadata size.
         // juliana@253_8: now Litebase supports weak cryptography.
         dbFile.setPos(4);
         buf[4] = (byte)(useCrypto? size ^ 0xAA : size);
         buf[5] = (byte)(useCrypto? (size >> 8) ^ 0xAA : (size >> 8));
      }
      
      dbFile.setPos(0);
      dbFile.writeBytes(buf, 0, len);
   }

   /**
    * Reads the user metadata from the .db file header.
    * 
    * @return The metadata.
    * @throws IOException If an internal method throws it.
    */
   byte[] readMetaData() throws IOException
   {
      byte[] buf = new byte[headerSize];
      db.setPos(0);
      db.readBytes(buf, 0, headerSize);
      return buf;
   }

   // juliana@212_8
   // juliana@210_2: now Litebase supports tables with ascii strings.
   // juliana@253_8: now Litebase supports weak cryptography.
   /**
    * Closes the table files.
    *
    * @param updatePos Indicates if <code>finalPos</code> must be re-calculated to shrink the file. 
    * @throws IOException If an internal method throws it.
    */
   void close(boolean updatePos) throws IOException
   {
      ByteArrayStream tsmdBas = new ByteArrayStream(7);
      DataStreamLB tsmdDs = new DataStreamLB(tsmdBas, useCrypto); // Creates a new stream.
      byte[] buffer = tsmdBas.getBuffer();

      // Stores the changeable information.
      Convert.fill(buffer, 0, 4, 0);
      buffer[0] = (byte)(useCrypto? (useOldCrypto? 1 : Table.USE_CRYPTO) : 0);
      
      tsmdDs.skipBytes(4);
      tsmdDs.writeShort(headerSize);
      
      // The table format must also be saved.
      tsmdDs.writeByte(isAscii? (Table.IS_ASCII | Table.IS_SAVED_CORRECTLY) : Table.IS_SAVED_CORRECTLY);
      
      writeMetaData(tsmdBas.getBuffer(), tsmdBas.getPos());

      if (updatePos)
         db.finalPos = rowCount * rowSize + headerSize; // Calculates .db used space: .db won't have zeros at the end.
      
      // Closes the files.
      db.close();
      dbo.close();

      dbo = db = null;
   }

   /**
    * Removes the table files.
    * 
    * @throws IOException If an internal method throws it.
    */
   public void remove() throws IOException
   {
      ((NormalFile)db).f.delete();
      ((NormalFile)dbo).f.delete();
      db = dbo = null;
   }

   // juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
   // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.
   // juliana@253_8: now Litebase supports weak cryptography.
   /**
    * Reads a value from a PlainDB.
    * 
    * @param value The value to be read.
    * @param offset The offset of the value in its row.
    * @param colType The type of the value.
    * @param stream The stream where the row data is stored.
    * @param isTemporary Indicates if this is a result set table.
    * @param isNull Indicates if the value is null.
    * @param isTempBlob Indicates if the blob is being read for a temporary table.
    * @return The total offset in the row.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   int readValue(SQLValue value, int offset, int colType, DataStreamLB stream, boolean isTemporary, boolean isNull, boolean isTempBlob) 
                                                                                                    throws IOException, InvalidDateException
   {
      if (isNull) // Only reads non-null values.
         return offset;
      
      switch (colType)
      {
         // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets. 
         case SQLElement.CHARS:
         case SQLElement.CHARS_NOCASE:
            if (isTemporary)
            {
               dbo.setPos(stream.readInt());
               value.asInt = dsdbo.readInt();
               value.asLong = dsdbo.readInt();
               PlainDB plainDB = ((Table)driver.htTables.get((int)value.asLong)).db;
               plainDB.dbo.setPos(value.asInt);
               value.asString = plainDB.loadString();
            }
            else
            {   
               if ((value.asInt = stream.readInt()) < dbo.finalPos && value.asInt >= 0)
               {
                  dbo.setPos(value.asInt); // Reads the string position in the .dbo and sets its position.
                  value.asLong = Utils.subStringHashCode(name, 5);
                  value.asString = loadString();
               }
               else
                  value.asString = "";
            }
            break;

         case SQLElement.SHORT:
            value.asShort = stream.readShort(); // Reads the short.
            break;

         case SQLElement.INT:
            value.asInt = stream.readInt();
            
            // juliana@230_38: corrected possible indices problems when updating an integer field on JavaSE and BlackBerry.
            if (((ByteArrayStream)stream.getStream()).getPos() == 4 && !isTemporary) // Is it the row id?
               value.asInt = value.asInt & Utils.ROW_ID_MASK; // Masks out the attributes.
            break;

         case SQLElement.LONG:
            value.asLong = stream.readLong();
            break;

         case SQLElement.FLOAT:
            value.asDouble = stream.readFloat();
            break;

         case SQLElement.DOUBLE:
            value.asDouble = stream.readDouble();
            break;
            
         case SQLElement.DATE:
            value.asInt = stream.readInt();
            break;

         case SQLElement.DATETIME:
            value.asInt = stream.readInt(); // Reads the date.
            value.asShort = stream.readInt(); // Reads the time.
            break;

         case SQLElement.BLOB: // juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
            if (isTempBlob) // A blob is being read to a temporary table.
            {
               value.asInt = stream.readInt();
               value.asLong = Utils.subStringHashCode(name, 5);
            } 
            else if (isTemporary) // A blob is being returned to the result set.
            {
               dbo.setPos(stream.readInt());
               int pos = dsdbo.readInt();
               PlainDB plainDB = ((Table)driver.htTables.get(dsdbo.readInt())).db;
               plainDB.dbo.setPos(pos);
               value.asBlob = new byte[plainDB.dsdbo.readInt()];
               if (value.asBlob.length > 0)
                  plainDB.dsdbo.readBytes(value.asBlob);
               
            }
            else // A blob is being returned to the result set.
            {
               int pos = stream.readInt();
               
               if (pos < dbo.finalPos && pos >= 0)
               {
                  dbo.setPos(pos); // Reads the blob position in the .dbo and sets its position.
                  if (value.asInt != -1)
                  {
                     value.asBlob = new byte[dsdbo.readInt()]; // Creates the blob with its size.
                     if (value.asBlob.length > 0) // juliana@212_8: when reading a file, an exception must not be thrown when reading zero bytes.
                        dsdbo.readBytes(value.asBlob); // Reads the blob.
                  }
                  else
                     value.asInt = dsdbo.readInt();
               }
               else
                  value.asInt = 0;
            }
      }
      return offset + Utils.typeSizes[colType];
            
   }

   // juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
   // juliana@253_8: now Litebase supports weak cryptography.
   /**
    * Writes a value to a table column.
    * 
    * @param type The type of the column.
    * @param value The value to be written.
    * @param ds The buffer where the value is stored before going to the table.
    * @param valueOk Indicates if the value is to be written.
    * @param addingNewRecord Indicates if it is an update or an insert.
    * @param colSize The column size of the value.
    * @param offset The offset of the string or blob in an update.
    * @param isTemporary Indicates if a temporary table is being used.
    * @throws IOException If an internal method throws it.
    */
   void writeValue(int type, SQLValue value, DataStreamLB ds, boolean valueOk, boolean addingNewRecord, int colSize, int offset, boolean isTemporary) 
                                                                                                                        throws IOException
   {
      if (!valueOk) // Only writes non-null values and values being changed.
         ds.skipBytes(Utils.typeSizes[type]); // If the value is null or is not updated, just skips its size.
      else
         switch (type)
         {
            // juliana@226_9: strings are not loaded anymore in the temporary table when building result sets. 
            case SQLElement.CHARS_NOCASE:
            case SQLElement.CHARS:
            {
               XFile dboFile = dbo;
               
               if (isTemporary)
               {
                  if ((dboFile.finalPos + 8) > dboFile.size) 
                     dboFile.growTo(dboFile.size + 8 * (rowInc > 16? rowInc : 16)); // If the .dbo is full, grows it.
                  dboFile.setPos(dboFile.finalPos);
                  ds.writeInt(dboFile.pos);
                  dsdbo.writeInt(value.asInt);
                  dsdbo.writeInt((int)value.asLong);
                  dboFile.finalPos = dboFile.pos;
               }
               else
               {
                  // juliana@225_7.
                  // juliana@210_2: now Litebase supports tables with ascii strings.
                  int c = value.asString.length(), // The string that is bigger than its field definiton was already trimmed.
                      size = isAscii? c + 2 : (c << 1) + 2; // Computes the string size.
                  
                  // guich@201_8: grows using rowInc instead of 16 if rowInc > 16.
                  // juliana@201_20: only grows .dbo if it is going to be increased.
                  // juliana@212_7: The size of the string must be taken into consideration because it can be zero.
                  if ((dboFile.finalPos + size) > dboFile.size) 
                     dboFile.growTo(dboFile.size + 2 + size * (rowInc > 16? rowInc : 16)); // If the .dbo is full, grows it.
                  
                  // juliana@202_21: Always writes the string at the end of the .dbo. This removes possible bugs when doing updates.
                  dboFile.setPos(dboFile.finalPos);
                  ds.writeInt(value.asInt = dboFile.pos); // The string position for an index and writes it in the .db
   
                  // Writes the string to the buffer.
                  if  (isAscii) // juliana@210_2: now Litebase supports tables with ascii strings.
                  {
                     String asString = value.asString;
                     int i = -1;
                     dsdbo.writeShort(c); // juliana@214_5: must trim ascii strings if they are longer than the field size definition.
                     while (++i < c)
                        dsdbo.writeByte(asString.charAt(i));
                  }
                  else
                     dsdbo.writeChars(value.asString, c);
   
                  dboFile.finalPos = dboFile.pos; // juliana@202_21: the final positon now is always the new positon.
               }             
               break;
            }
            case SQLElement.SHORT:
               ds.writeShort(value.asShort);
               break;

            case SQLElement.DATE:
            case SQLElement.INT:
               ds.writeInt(value.asInt);
               break;

            case SQLElement.LONG:
               ds.writeLong(value.asLong);
               break;

            case SQLElement.FLOAT:
               ds.writeFloat(value.asDouble);
               break;

            case SQLElement.DOUBLE:
               ds.writeDouble(value.asDouble);
               break;

            case SQLElement.DATETIME:
               ds.writeInt(value.asInt); // Writes the date.
               ds.writeInt(value.asShort); // Writes the time.
               break;

            case SQLElement.BLOB: // juliana@220_3: blobs are not loaded anymore in the temporary table when building result sets.
            {
               XFile dboFile = dbo;
               
               if (isTemporary) // The position of a blob and its table is being written to the temporary table.
               {
                  if ((dboFile.finalPos + 8) > dboFile.size) 
                     dboFile.growTo(dboFile.size + 8 * (rowInc > 16? rowInc : 16)); // If the .dbo is full, grows it.
                  dboFile.setPos(dboFile.finalPos);
                  ds.writeInt(dboFile.pos);
                  dsdbo.writeInt(value.asInt);
                  dsdbo.writeInt((int)value.asLong);
                  dboFile.finalPos = dboFile.pos;
               }
               else
               {
                  int size = Math.min(value.asBlob.length, colSize), // Trims a blob that is bigger than its field definiton.
                      oldPos = 0;
                  
                  // guich@201_8: grows using rowInc instead of 16 if rowInc > 16.
                  // juliana@201_20: only grows .dbo if it is going to be increased.
                  // juliana@212_7: The size of the blob must be taken into consideration because it can be zero.
                  if (addingNewRecord && (dboFile.finalPos + size + 4) >= (dboFile.size + 1)) 
                     dboFile.growTo(dboFile.size + 4 + size * (rowInc > 16? rowInc : 16)); // If the .dbo is full, grows it.
   
                  // It is an insert or the size of the blob is greater then the old, writes the blob at the end of the .dbo. 
                  if (addingNewRecord)
                     dboFile.setPos(dboFile.finalPos);
                  else
                  {
                     oldPos = dboFile.pos;
                     dboFile.setPos(oldPos - offset); // The blob was read before.
                  }
                  ds.writeInt(dboFile.pos); // Writes its position in the ds.
                  dsdbo.writeInt(size); // Writes the blob size to .dbo.
                  if (size > 0) // juliana@212_8: when reading a file, an exception must not be thrown when writing zero bytes.
                     dsdbo.writeBytes(value.asBlob, 0, size); // Writes the blob itself to .dbo.
   
                  // It is an insert or the size of the blob is greater then the old one, the final positon is the new positon.
                  if (addingNewRecord)
                     dboFile.finalPos = dboFile.pos;
                  
                  else // Otherwise, restores the old position.
                     dboFile.setPos(oldPos); 
               }
            }
         }

   }

   /**
    * Tests if a record of a table is not deleted.
    *
    * @return <code>false</code> if the record is deleted; <code>true</code> otherwise.
    * @throws IOException If an internal method throws it.
    */
   boolean recordNotDeleted() throws IOException
   {
      bas.reset(); // Resets read position.
      boolean notDeleted = (basds.readInt() & Utils.ROW_ATTR_MASK) != Utils.ROW_ATTR_DELETED;
      bas.reset(); // Resets read position.
      return notDeleted;
   }
   
   String loadString() throws IOException
   {
      int length = dsdbo.readUnsignedShort();
      if (isAscii) // juliana@210_2: now Litebase supports tables with ascii strings.
      {
         byte[] buf = driver.buffer;
         if (buf.length < length)
            driver.buffer = buf = new byte[length];
         dsdbo.readBytes(buf, 0, length);
         return length != 0? new String(buf, 0, length) : ""; // Reads the string.
      }
      else
      {
         char[] chars = driver.valueAsChars;
         if (chars.length < length)
            driver.valueAsChars = chars = new char[length];
         dsdbo.readChars(chars, length);            
         return length != 0? new String(chars, 0, length) : ""; // Reads the string.
      }
   }
}
