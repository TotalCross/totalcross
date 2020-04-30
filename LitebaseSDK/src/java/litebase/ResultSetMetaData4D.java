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

/**
 * This native class returns useful information for the <code>ResultSet</code> columns. Note that the information can be retrieved even if 
 * <code>ResultSet</code> returns no data.
 * <P>Important: it is not possible to retrieve these information if the <code>ResultSet</code> is closed!
 */
public class ResultSetMetaData4D
{
   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>CHAR</code>.
    */
   public static final int CHAR_TYPE = 0;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>SHORT</code>.
    */
   public static final int SHORT_TYPE = 1;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>INT</code>.
    */
   public static final int INT_TYPE = 2;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>LONG</code>.
    */
   public static final int LONG_TYPE = 3;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>FLOAT</code>.
    */
   public static final int FLOAT_TYPE = 4;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>DOUBLE</code>.
    */
   public static final int DOUBLE_TYPE = 5;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>CHARS_NOCASE</code>.
    */
   public static final int CHAR_NOCASE_TYPE = 6;

   // The type BOOLEAN_TYPE is absent because a column will never have this type. This is only used with expressions and this type would have the 
   // value 7.
   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>DATE_TYPE</code>.
    */
   public static final int DATE_TYPE = 8;

   // rnovais@_567_2
   // rnovais@_567_2: stored as two ints.
   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>DATETIME_TYPE</code>.
    */
   public static final int DATETIME_TYPE = 9;

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>BLOB_TYPE</code>.
    */
   public static final int BLOB_TYPE = 10;

   /** 
    * The underlying <code>ResultSet</code>. 
    */
   ResultSet4D rs;
   
   // juliana@230_11: Litebase public class constructors are now not public any more. 
   /**
    * The constructor.
    */
   private ResultSetMetaData4D() {}
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Gets the number of columns for this <code>ResultSet</code>.
    *
    * @return The number of columns for this <code>ResultSet</code>.
    */
   public native int getColumnCount();

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
   /**
    * Given the column index (starting at 1), returns the display size. For chars, it will return the number of chars defined; for primitive types, 
    * it will return the number of decimal places it needs to be displayed correctly. Returns 0 if an error occurs.
    *
    * @param column The column index (starting at 1).
    * @return The display size or -1 if a problem occurs.
    * @throws IllegalArgumentException If the column index is invalid.
    */
   public native int getColumnDisplaySize(int column) throws IllegalArgumentException;

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
   /**
    * Given the column index (starting at 1), returns the column name. Note that if an alias is used to the column, the alias will be returned 
    * instead. If an error occurs, an empty string is returned. Note that LitebaseConnection 2.x tables must be recreated to be able to return this 
    * label information.
    *
    * @param column The column index (starting at 1).
    * @return The name or alias of the column, which can be an empty string if an error occurs.
    * @throws IllegalArgumentException If the column index is invalid.
    */
   public native String getColumnLabel(int column) throws IllegalArgumentException;

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
   /**
    * Given the column index (starting at 1), returns the column type.
    *
    * @param column The column index (starting at 1).
    * @return The column type, which can be: <b><code>SHORT_TYPE</b></code>, <b><code>INT_TYPE</b></code>, <b><code>LONG_TYPE</b></code>, 
    * <b><code>FLOAT_TYPE</b></code>, <b><code>DOUBLE_TYPE</b></code>, <b><code>CHAR_TYPE</b></code>, <b><code>CHAR_NOCASE_TYPE</b></code>, 
    * <b><code>DATE_TYPE</b></code>, <b><code>DATETIME_TYPE</b></code>, or <b><code>BLOB_TYPE</b></code>.
    * @throws IllegalArgumentException If the column index is invalid.
    */
   public native int getColumnType(int column) throws IllegalArgumentException;

   /**
    * Given the column index (starting at 1), returns the name of the column type.
    *
    * @param column The column index (starting at 1).
    * @return The name of the column type, which can be: <b><code>chars</b></code>, <b><code>short</b></code>, <b><code>int</b></code>, 
    * <b><code>long</b></code>, <b><code>float</b></code>, <b><code>double</b></code>, <b><code>date</b></code>, <b><code>datetime</b></code>, 
    * <b><code>blob</b></code>, or null if an error occurs.
    */
   public native String getColumnTypeName(int column);

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.
   /**
    * Given the column index, (starting at 1) returns the name of the table it came from.
    *
    * @param columnIdx The column index.
    * @return The name of the table it came from or <code>null</code> if the column index does not exist.
    * @throws IllegalArgumentException If the column index is invalid.
    */
   public native String getColumnTableName(int columnIdx) throws IllegalArgumentException;
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Given the column name or alias, returns the name of the table it came from.
    *
    * @param columnName The column name.
    * @return The name of the table it came from or <code>null</code> if the column name does not exist.
    * @throws DriverException If the column was not found.
    * @throws NullPointerException if the column name is null.
    */
   public native String getColumnTableName(String columnName) throws DriverException, NullPointerException;

   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   // juliana@227_2: added methods to indicate if a column of a result set is not null or has default values.
   /**
    * Indicates if a column of the result set has default value.
    * 
    * @param columnIndex The column index.
    * @return <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
    * @throws DriverException If the column does not have an underlining table.
    */
   public native boolean hasDefaultValue(int columnIndex) throws DriverException;
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Indicates if a column of the result set has default value.
    * 
    * @param columnName The column name.
    * @return <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
    * @throws DriverException If the column was not found or does not have an underlining table.
    * @throws NullPointerException if the column name is null.
    */
   public native boolean hasDefaultValue(String columnName) throws DriverException, NullPointerException;
  
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Indicates if a column of the result set is not null.
    * 
    * @param columnIndex The column index.
    * @return <code>true</code> if the column is not null; <code>false</code>, otherwise. 
    * @throws DriverException If the column does not have an underlining table.
    */
   public native boolean isNotNull(int columnIndex) throws DriverException;
   
   // juliana@230_27: if a public method in now called when its object is already closed, now an IllegalStateException will be thrown instead of a 
   // DriverException.
   /**
    * Indicates if a column of the result set is not null.
    * 
    * @param columnName The column name.
    * @return <code>true</code> if the column is not null; <code>false</code>, otherwise. 
    * @throws DriverException If the column was not found or does not have an underlining table.
    * @throws NullPointerException if the column name is null.
    */
   public native boolean isNotNull(String columnName) throws DriverException, NullPointerException;

   // juliana@253_3: added methods to return the primary key columns of a table.
   /**
    * Returns the primary key column indices of a table.
    * 
    * @param tableName The table name.
    * @return <code>null</code> if the given table does not have primary key or an array with the column indices of the primary key.
    * @throws NullPointerException if the table name is null.
    */
   public native byte[] getPKColumnIndices(String tableName) throws NullPointerException;
   
   /**
    * Returns the primary key column names of a table.
    * 
    * @param tableName The table name.
    * @return <code>null</code> if the given table does not have primary key or an array with the column names of the primary key.
    * @throws NullPointerException if the table name is null.
    */
   public native String[] getPKColumnNames(String tableName) throws NullPointerException;
   
   // juliana@253_4: added methods to return the default value of a column.
   /**
    * Returns the default value of a column.
    * 
    * @param columnIndex The column index.
    * @return The default value of the column as a string or <code>null</code> if there is no default value.
    * @throws DriverException If the column index does not have an underlining table.
    */
   public native String getDefaultValue(int columnIndex) throws DriverException;
   
   /**
    * Returns the default value of a column.
    * 
    * @param columnName The column name.
    * @return The default value of the column as a string or <code>null</code> if there is no default value.
    * @throws DriverException If the column name does not have an underlining table.
    * @throws NullPointerException if the column name is null.
    */
   public native String getDefaultValue(String columnName) throws DriverException, NullPointerException;
}
