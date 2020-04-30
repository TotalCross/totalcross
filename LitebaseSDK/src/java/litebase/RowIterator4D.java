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

import totalcross.sys.Time;
import totalcross.util.Date;

/**
 * Native class used to iterate through the rows of a database. It can access some attributes from the row that eases the control of which row was 
 * changed, deleted or is newer since a synchronization.
 */
public class RowIterator4D
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
    * The data for the current row. The whole row is included.
    */
   public byte[] data;

   /**
    * The rowid for the current row.
    */
   public int rowid;
   
   /**
    * The connection with Litebase.
    */
   Object driver;

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
   long table;

   // juliana@230_11: Litebase public class constructors are now not public any more. 
   /**
    * The constructor.
    */
   private RowIterator4D() {}
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Moves to the next record and fills the data members.
    *
    * @return <code>true</code> if it is possible to iterate to the next record. Otherwise, it will return <code>false</code>.
    */
   public native boolean next();

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Moves to the next record with an attribute different of SYNCED.
    *
    * @return <code>true</code> if it is possible to iterate to a next record not synced. Otherwise, it will return <code>false</code>.
    */
   public native boolean nextNotSynced();

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * If the attribute is currently NEW or UPDATED, this method sets them to SYNCED. Note that if the row is DELETED, the change will be ignored.
    */
   public native void setSynced();
   
   // juliana@270_29: added RowIterator.setNotSynced().
   /**
    * Forces the attribute to be NEW. This method will be useful if a row was marked as synchronized but was not sent to server for some problem.
    * If the row is marked as DELETED, its attribute won't be changed.
    */
   public native void setNotSynced();

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Closes this iterator.
    */
   public native void close();

   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Resets the counter to zero so it is possible to restart to fetch records.
    */
   public native void reset();

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a short contained in the current row.
    *
    * @param column The short column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    */
   public native short getShort(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns an integer contained in the current row.
    *
    * @param column The integer column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    */
   public native int getInt(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a long integer contained in the current row.
    *
    * @param column The long integer column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    */
   public native long getLong(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a floating point number contained in the current row.
    *
    * @param column The floating point number column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    */
   public native double getFloat(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a double precision floating point number contained in the current row.
    *
    * @param column The double precision floating point number column index, starting from 1.
    * @return The value of the column or 0 if the column is <code>null</code>.
    */
   public native double getDouble(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a string contained in the current row.
    *
    * @param column The string column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>.
    */
   public native String getString(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a blob contained in the current row.
    *
    * @param column The blob column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>.
    */
   public native byte[] getBlob(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a date contained in the current row.
    *
    * @param column The date column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>.
    */
   public native Date getDate(int column);

   // juliana@223_5: now possible null values are treated in RowIterator.
   /**
    * Returns a datetime contained in the current row.
    *
    * @param column The datetime column index, starting from 1.
    * @return The value of the column or <code>null</code> if the column is <code>null</code>.
    */
   public native Time getDateTime(int column);
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a 
   // DriverException.
   // juliana@223_5: now possible null values are treated in RowIterator.
   // juliana@225_14: RowIterator must throw an exception if its driver is closed.
   /**
    * Indicates if this column has a <code>NULL</code>.
    *
    * @param column The column index, starting from 1.
    * @return <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
    * @throws IllegalArgumentException If the column index is invalid.
    */
   public native boolean isNull(int column) throws IllegalArgumentException;
}
