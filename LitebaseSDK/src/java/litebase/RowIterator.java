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

import totalcross.sys.*;
import totalcross.util.*;
import totalcross.io.*;

/**
 * Used to iterate through the rows of a database. It can access some attributes from the row that eases the control of which row was 
 * changed, deleted or is newer since a synchronization.
 */
public class RowIterator
{
   /**
    * Indicates if the a row was synced.
    */
   public static final int ROW_ATTR_SYNCED = 0;

   /**
    * Indicates if the row is new.
    */
   public static final int ROW_ATTR_NEW = 1;

   /**
    * Indicates if the row was updated.
    */
   public static final int ROW_ATTR_UPDATED = 2;

   /**
    * Indicates if the row was deleted.
    */
   public static final int ROW_ATTR_DELETED = 3;

   /**
    * The shift for the row attributes.
    */
   private static final int ROW_ATTR_SHIFT = 30;

   /**
    * The data for the current row. The whole row is included.
    */
   public byte[] data;

   /**
    * The rowid for the current row.
    */
   public int rowid;

   /**
    * The attribute for this row. It is necessary to use the constants beginning with <B><CODE>ROW_ATTR_</B><CODE> to compare or assign.
    */
   public int attr;

   /**
    * The number of the row. Note: this must be READ ONLY. Changing it will corrupt your database.
    */
   public int rowNumber;

   /** 
    * The table. For internal use only. 
    */
   private Table table;

   /**
    * An stream to write and read data from the current row.
    */
   protected ByteArrayStream bas;

   /**
    * The data stream to read data from the current row.
    */
   protected DataStreamLB basds; // juliana@253_8: now Litebase supports weak cryptography.

   /**
    * An iterator cannot be constructed directly; it must be created throught the method <code>LitebaseConnection.getRowIterator()</code>.
    *
    * @param driver The Litebase driver.
    * @param tableName The name of the table for which the row iterator will be created.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   protected RowIterator(LitebaseConnection driver, String tableName) throws IOException, InvalidDateException
   {
      table = driver.getTable(tableName);
      rowNumber = -1;
      
      // juliana@253_8: now Litebase supports weak cryptography.
      basds = new DataStreamLB(bas = new ByteArrayStream(data = new byte[table.db.rowSize]), table.db.useCrypto);
   }

   /**
    * Moves to the next record and fills the data members.
    *
    * @return <code>true</code> if it is possible to iterate to the next record. Otherwise, it will return <code>false</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public boolean next() throws DriverException
   {
      checkState(); // juliana@230_27
      
      PlainDB plainDb = table.db;
      
      try
      {
         if (++rowNumber < plainDb.rowCount)
         {
            plainDb.read(rowNumber);
            int id = plainDb.basds.readInt();
            Vm.arrayCopy(plainDb.basbuf, 0, data, 0, plainDb.rowSize);
            rowid = id & Utils.ROW_ID_MASK; // Masks out the attributes.
            attr = ((id & Utils.ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3;
            
            // juliana@223_5: now possible null values are treated in RowIterator.
            plainDb.bas.reset();
            table.readNullBytesOfRecord(0, false, 0);
            
            return true;
         }
         return false;
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   /**
    * Moves to the next record with an attribute different of SYNCED.
    *
    * @return <code>true</code> if it is possible to iterate to a next record not synced. Otherwise, it will return
    *         <code>false</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public boolean nextNotSynced() throws DriverException
   {
      checkState(); // juliana@230_27
      
      PlainDB plainDb = table.db;
      
      try
      {
         while (++rowNumber < plainDb.rowCount)
         {
            plainDb.read(rowNumber);
            int id = plainDb.basds.readInt();
            if ((id & Utils.ROW_ATTR_MASK) == Utils.ROW_ATTR_SYNCED)
               continue;
            Vm.arrayCopy(plainDb.basbuf, 0, data, 0, plainDb.rowSize);
            rowid = id & Utils.ROW_ID_MASK; // Masks out the attributes.
            attr = ((id & Utils.ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3;
            
            // juliana@223_5: now possible null values are treated in RowIterator.
            plainDb.bas.reset();
            table.readNullBytesOfRecord(0, false, 0);
            
            return true;
         }
   
         return false;
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   /**
    * If the attribute is currently NEW or UPDATED, this method sets them to SYNCED. Note that if the row is DELETED, the change will be ignored.
    *
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public void setSynced() throws DriverException
   {
      checkState(); // juliana@230_27
      
      PlainDB plainDb = table.db;
      plainDb.bas.reset();

      try // The record is assumed to have been already read.
      {
         int id = plainDb.basds.readInt();
         int oldAttr = (((id & Utils.ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3); // guich@560_19
         int newAttr = attr = ROW_ATTR_SYNCED;
         if (newAttr != oldAttr && oldAttr != ROW_ATTR_DELETED)
         {
            plainDb.bas.reset();
            plainDb.basds.writeInt((id & Utils.ROW_ID_MASK) | newAttr); // Sets the new attribute.
            plainDb.rewrite(rowNumber);
         }
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }
   
   // juliana@270_29: added RowIterator.setNotSynced().
   /**
    * Forces the attribute to be NEW. This method will be useful if a row was marked as synchronized but was not sent to server for some problem.
    * If the row is marked as DELETED, its attribute won't be changed.
    *
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public void setNotSynced()
   {
      checkState(); // juliana@230_27
      
      PlainDB plainDb = table.db;
      plainDb.bas.reset();
   
      try 
      {
         int id = plainDb.basds.readInt();
         int oldAttr = (((id & Utils.ROW_ATTR_MASK) >> ROW_ATTR_SHIFT) & 3); // guich@560_19
         int newAttr = attr = Utils.ROW_ATTR_NEW;
         if (newAttr != oldAttr && oldAttr != ROW_ATTR_DELETED)
         {
            plainDb.bas.reset();
            plainDb.basds.writeInt((id & Utils.ROW_ID_MASK) | newAttr); // Sets the new attribute.
            plainDb.rewrite(rowNumber);
         }
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   /**
    * Closes this iterator.
    * 
    * @throws DriverException if an <code>IOException</code> occurs.
    */
   public void close()
   {
      checkState(); // juliana@230_27
      
      // juliana@227_22: RowIterator.close() now flushes the setSynced() calls.
      NormalFile dbFile = (NormalFile)table.db.db;
      if (dbFile.cacheIsDirty) 
         try
         {
            dbFile.flushCache();
         }
         catch (IOException exception)
         {
            throw new DriverException(exception);
         }
         
      data = null;
      table = null;
   }

   /**
    * Resets the counter to zero so it is possible to restart to fetch records.
    */
   public void reset()
   {
      rowNumber = -1;
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns a short contained in the current row.
    *
    * @param column The short column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public short getShort(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.SHORT, SQLElement.UNDEFINED))
            return 0;
         return basds.readShort(); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns an integer contained in the current row.
    *
    * @param column The integer column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public int getInt(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.INT, SQLElement.UNDEFINED))
            return 0;
         return basds.readInt(); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns a long integer contained in the current row.
    *
    * @param column The long integer column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public long getLong(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.LONG, SQLElement.UNDEFINED))
            return 0;
         return basds.readLong(); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns a floating point number contained in the current row.
    *
    * @param column The floating point number column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public double getFloat(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.FLOAT, SQLElement.UNDEFINED))
            return 0;
         return basds.readFloat(); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns a double precision floating point number contained in the current row. Returns 0 if the column is null.
    *
    * @param column The double precision floating point number column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public double getDouble(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.DOUBLE, SQLElement.UNDEFINED))
            return 0;
         return basds.readDouble(); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   /**
    * Returns a string contained in the current row. Note that this method must be used only with CHAR types.
    *
    * @param column The string column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public String getString(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.CHARS, SQLElement.CHARS_NOCASE))
            return null;
         
         PlainDB plainDB = table.db;
         plainDB.dbo.setPos(basds.readInt()); // Finds the string position in the .dbo.
         return plainDB.loadString();     
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   /**
    * Returns a blob contained in the current row.
    *
    * @param column The blob column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public byte[] getBlob(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.BLOB, SQLElement.UNDEFINED))
            return null;
         
         PlainDB db = table.db;
         DataStreamLB ds = db.dsdbo; // juliana@253_8: now Litebase supports weak cryptography.
         db.dbo.setPos(basds.readInt()); // Finds the blob position in the .dbo.

         // Reads it.
         byte[] object = new byte[ds.readInt()]; // Finds the blob size.
         ds.readBytes(object);
         return object;

      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns a date contained in the current row. 
    *
    * @param column The date column index, starting from 1.
    * @return The value of the column  or <code>null</code> if the column is <code>null</code>
    * @throws DriverException If an <code>IOException</code> or an <code>InvalidDateException</code> occurs.
    */
   public Date getDate(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.DATE, SQLElement.UNDEFINED))
            return null;
         return new Date(basds.readInt()); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
      catch (InvalidDateException exception) 
      {
         return null;
      }
   }

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Returns a datetime contained in the current row. Returns null if the column is null.
    *
    * @param column The datetime column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>.
    * @throws DriverException If an <code>IOException</code> occurs.
    */
   public Time getDateTime(int column) throws DriverException
   {
      try
      {
         if (readColumn(column, SQLElement.DATETIME, SQLElement.UNDEFINED))
            return null;
         return new Time(basds.readInt(), basds.readInt()); // Reads the value.
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
   }
   
   // juliana@223_5: now possible null values are treated in RowIterator.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Indicates if this column has a <code>NULL</code>.
    *
    * @param column The column index, starting from 1.
    * @return <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
    * @throws IllegalArgumentException If the column index is invalid.
    */
   public boolean isNull(int column) throws IllegalArgumentException
   {
      checkState();
      
      Table tableAux = table;
      
      // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a 
      // DriverException.
      if (column < 0 || column >= tableAux.columnCount)
         throw new IllegalArgumentException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NUMBER) + column);
         
      return (table.columnNulls[0][column >> 3] & (1 << (column & 7))) != 0;
   }
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Checks if the driver or the row iterator are closed.
    * 
    * @throws IllegalStateException if one of them is closed.
    */
   private void checkState()
   {
      if (table == null)
         throw new IllegalStateException(LitebaseMessage.getMessage(LitebaseMessage.ERR_ROWITERATOR_CLOSED));
      if (table.db == null)
         throw new IllegalStateException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
   }
   
   /**
    * Checks if the driver or the row iterator is closed, if the column index is invalid or if the type of the column requested is imcompatible with
    * the get method type and reads the requested value.
    * 
    * @param column The column index.
    * @param type1 The main column type of the method.
    * @param type2 Used only for strings to test if the column is a char nocase.
    * @return <code>true</code> if the value stored is SQL <code>NULL</code>; <code>false</code>, otherwise.
    * @throws DriverException If the column is not of type requested.
    * @throws IllegalArgumentException If the column index is invalid.
    * @throws IOException If an internal method throws it.
    */
   private boolean readColumn(int column, int type1, int type2) throws IllegalArgumentException, DriverException, IOException
   {
      checkState(); // juliana@230_27
      
      Table tableAux = table;
      
      // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a 
      // DriverException.
      if (column < 0 || column >= tableAux.columnCount)
         throw new IllegalArgumentException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NUMBER) + column);
         
      int type = tableAux.columnTypes[column];
      if (type != type1 && type != type2) // Check the column type.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));

      bas.setPos(tableAux.columnOffsets[column]); // Finds the value position.
      
      // juliana@223_5: now possible null values are treated in RowIterator.
      // Treats possible null values.
      return (tableAux.columnNulls[0][column >> 3] & (1 << (column & 7))) != 0;    
   }
}
