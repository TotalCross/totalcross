/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: ResultSetMetaData.java,v 1.10.4.1.2.6.4.35 2011-02-18 15:56:32 juliana Exp $

package litebase;

import totalcross.io.IOException;
import totalcross.util.InvalidDateException;

/**
 * This class returns useful information for the <code>ResultSet</code> columns. Note that the information can be retrieved even if the 
 * <code>ResultSet</code> returns no data.
 * <P>Important: it is not possible to retrieve these information if the <code>ResultSet</code> is closed!
 */
public class ResultSetMetaData
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

   // The type BOOLEAN_TYPE is absent because a column will never have this type.
   // This is only used with expressions and this type would have the value 7.

   /**
    * Used by the <code>getColumnType</code> method to indicate that the column is of type <code>DATE_TYPE</code>.
    */
   public static final int DATE_TYPE = 8;

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
   private ResultSet rs;

   /**
    * Constructs a new <code>ResultSetMetaData</code> for a given <code>ResultSet</code>.
    *
    * @param aRs The ResultSet.
    */
   ResultSetMetaData(ResultSet aRs)
   {
      rs = aRs;
   }

   /**
    * Gets the number of columns for this <code>ResultSet</code>.
    *
    * @return The number of columns for this <code>ResultSet</code>.
    * @throws DriverException If the result or the driver is closed.
    */
   public int getColumnCount() throws DriverException
   {
      // juliana@211_4: solved bugs with result set dealing.
      if (rs.table == null) // guich@564_4
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (rs.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      return rs.isSimpleSelect ? rs.columnCount - 1 : rs.columnCount; // juliana@114_10: skips the rowid.
   }

   /**
    * Given the column index (starting at 1), returns the display size. For chars, it will return the number of chars defined; for primitive types, 
    * it will return the number of decimal places it needs to be displayed correctly. Returns 0 if an error occurs.
    *
    * @param column The column index (starting at 1).
    * @return The display size or -1 if a problem occurs.
    * @throws DriverException If the result set or the driver is closed, or the column index is out of bounds.
    */
   public int getColumnDisplaySize(int column) throws DriverException
   {
      // juliana@211_4: solved bugs with result set dealing.
      if (rs.table == null) // guich@564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (rs.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
      if (column <= 0 || (rs.isSimpleSelect && column >= rs.columnCount) || (!rs.isSimpleSelect && column > rs.columnCount)) 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NUMBER));
      
      if (rs.isSimpleSelect) // juliana@114_10: skips the rowid.
         column++;
      
      switch (rs.table.columnTypes[column - 1])
      {
         case ResultSetMetaData.SHORT_TYPE:
            return 6; // guich@560_4: adjusted the sizes
         case ResultSetMetaData.INT_TYPE:
            return 11;
         case ResultSetMetaData.LONG_TYPE:
            return 20;
         case ResultSetMetaData.FLOAT_TYPE:
            return 13;
         case ResultSetMetaData.DOUBLE_TYPE:
            return 21;
         case ResultSetMetaData.CHAR_TYPE:
         case ResultSetMetaData.CHAR_NOCASE_TYPE:
            return rs.table.columnSizes[column - 1];
         case ResultSetMetaData.DATE_TYPE:
            return 11; // rnovais@570_12
         case ResultSetMetaData.DATETIME_TYPE:
            return 31; // (11 + 20) // rnovais@570_12
         // BLOBs can't be displayed.
      }
      return -1;

   }

   /**
    * Given the column index (starting at 1), returns the column name. Note that if an alias is used to the column, the alias will be returned 
    * instead. If an error occurs, an empty string is returned. Note that LitebaseConnection 2.x tables must be recreated to be able to return this label information.
    *
    * @param column The column index (starting at 1).
    * @return The name or alias of the column, which can be an empty string if an error occurs.
    * @throws DriverException If the result set or the driver is closed, or the column index is out of bounds.
    */
   public String getColumnLabel(int column) throws DriverException
   {
      // juliana@211_4: solved bugs with result set dealing.
      if (rs.table == null) // guich@564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (rs.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
      if (column <= 0 || (rs.isSimpleSelect && column >= rs.columnCount) || (!rs.isSimpleSelect && column > rs.columnCount)) 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NUMBER));

      if (rs.table.columnNames != null)
         return rs.table.columnNames[rs.isSimpleSelect? column: column - 1]; // juliana@114_10: skips the rowid.
      return "";
   }

   /**
    * Given the column index (starting at 1), returns the column type.
    *
    * @param column The column index (starting at 1).
    * @return The column type, which can be: <b><code>SHORT_TYPE</b></code>, <b><code>INT_TYPE</b></code>, <b><code>LONG_TYPE</b></code>, 
    * <b><code>FLOAT_TYPE</b></code>, <b><code>DOUBLE_TYPE</b></code>, <b><code>CHAR_TYPE</b></code>, <b><code>CHAR_NOCASE_TYPE</b></code>, 
    * <b><code>DATE_TYPE</b></code>, <b><code>DATETIME_TYPE</b></code>, or <b><code>BLOB_TYPE</b></code>.
    * @throws DriverException If the result set or the driver is closed, or the column index is out of bounds.
    */
   public int getColumnType(int column) throws DriverException
   {
      // juliana@211_4: solved bugs with result set dealing.
      if (rs.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (rs.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
      if (column <= 0 || (rs.isSimpleSelect && column >= rs.columnCount) || (!rs.isSimpleSelect && column > rs.columnCount)) 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NUMBER));

      return rs.table.columnTypes[rs.isSimpleSelect? column: column - 1]; // juliana@114_10: skips the rowid.
   }

   /**
    * Given the column index (starting at 1), returns the name of the column type.
    *
    * @param column The column index (starting at 1).
    * @return The name of the column type, which can be: <b><code>chars</b></code>, <b><code>short</b></code>, <b><code>int</b></code>, 
    * <b><code>long</b></code>, <b><code>float</b></code>, <b><code>double</b></code>, <b><code>date</b></code>, <b><code>datetime</b></code>, 
    * <b><code>blob</b></code>, or null if an error occurs.
    */
   public String getColumnTypeName(int column) 
   {
      switch (getColumnType(column)) // Gets the string representation of the type.
      { 
         case CHAR_TYPE:
         case CHAR_NOCASE_TYPE:
            return "chars";
         case SHORT_TYPE:
            return "short";
         case INT_TYPE:
            return "int";
         case LONG_TYPE:
            return "long";
         case FLOAT_TYPE:
            return "float";
         case DOUBLE_TYPE:
            return "double";
         case DATE_TYPE:
            return "date";
         case DATETIME_TYPE:
            return "datetime";
         case BLOB_TYPE:
            return "blob";
      }
      return null;
   }

   /**
    * Given the column index, (starting at 1) returns the name of the table it came from.
    *
    * @param columnIdx The column index.
    * @return The name of the table it came from.
    * @throws DriverException If the result set or the driver is closed, or if the column was not found.
    */
   public String getColumnTableName(int columnIdx) throws DriverException
   {
      // juliana@211_4: solved bugs with result set dealing.
      if (rs.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (rs.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      // juliana@213_5: Now a DriverException is thrown instead of returning an invalid value.
      if (columnIdx <= 0 || (rs.isSimpleSelect && columnIdx >= rs.columnCount) || (!rs.isSimpleSelect && columnIdx > rs.columnCount)) 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NUMBER));
      
      return rs.fields[columnIdx - 1].tableName;
   }
   
   /**
    * Given the column name or alias, returns the name of the table it came from.
    *
    * @param columnName The column name.
    * @return The name of the table it came from or <code>null</code> if the column name does not exist.
    * @throws DriverException If the result set or the driver is closed, or if the column was not found.
    */
   public String getColumnTableName(String columnName) throws DriverException
   {
      // juliana@211_4: solved bugs with result set dealing.
      if (rs.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (rs.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      SQLResultSetField[] fields = rs.fields;
      int i = -1, 
          len = fields.length;

      while (++i < len) // Gets the name of the table or its alias given the column name.
         if (columnName.equalsIgnoreCase(fields[i].tableColName) || columnName.equalsIgnoreCase(fields[i].alias))
            return fields[i].tableName;
      throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COLUMN_NOT_FOUND));
   }
   
   // juliana@227_2: added methods to indicate if a column of a result set is not null or has default values.
   /**
    * Indicates if a column of the result set has default value.
    * 
    * @param columnIndex The column index.
    * @return <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
    * @throws DriverException If the result set or the driver is closed.
    */
   public boolean hasDefaultValue(int columnIndex) throws DriverException
   {
      ResultSet resultSet = rs;
      if (resultSet.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (resultSet.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      try // Gets the table column info.
      {
         SQLResultSetField field = resultSet.fields[columnIndex - 1];
       
         return ((resultSet.driver.getTable(getColumnTableName(columnIndex))).columnAttrs[field.tableColIndex >= 0? field.tableColIndex 
                                                                             : field.parameter.tableColIndex] & Utils.ATTR_COLUMN_HAS_DEFAULT) != 0;
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
      catch (InvalidDateException exception) {}
      return false;
   }
   
   /**
    * Indicates if a column of the result set has default value.
    * 
    * @param columnName The column name.
    * @return <code>true</code> if the column has a default value; <code>false</code>, otherwise. 
    * @throws DriverException If the result set or the driver is closed, or if the column was not found.
    */
   public boolean hasDefaultValue(String columnName) throws DriverException
   {
      ResultSet resultSet = rs;
      if (resultSet.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (resultSet.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      SQLResultSetField[] fields = resultSet.fields;
      SQLResultSetField field;
      int i = -1, 
          len = fields.length;

      while (++i < len) // Gets the name of the table or its alias given the column name and gets the table column info.
      {
         if (columnName.equalsIgnoreCase((field = fields[i]).tableColName) || columnName.equalsIgnoreCase(fields[i].alias))
            try
            {
               return ((resultSet.driver.getTable(field.tableName).columnAttrs[field.tableColIndex >= 0? field.tableColIndex 
                                                                  : field.parameter.tableColIndex]) & Utils.ATTR_COLUMN_HAS_DEFAULT) != 0;
            }
            catch (IOException exception)
            {
               throw new DriverException(exception);
            }
            catch (InvalidDateException exception) {}
      }
      if (i == len)
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COLUMN_NOT_FOUND));
      
      return false;
   }
   
   /**
    * Indicates if a column of the result set is not null.
    * 
    * @param columnIndex The column index.
    * @return <code>true</code> if the column is not null; <code>false</code>, otherwise. 
    * @throws DriverException If the result set or the driver is closed.
    */
   public boolean isNotNull(int columnIndex) throws DriverException
   {
      ResultSet resultSet = rs;
      if (resultSet.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (resultSet.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      try // Gets the table column info.
      {
         SQLResultSetField field = resultSet.fields[columnIndex - 1];
         
         return ((resultSet.driver.getTable(getColumnTableName(columnIndex))).columnAttrs[field.tableColIndex >= 0? field.tableColIndex 
                                                                             : field.parameter.tableColIndex] & Utils.ATTR_COLUMN_IS_NOT_NULL) != 0;
      }
      catch (IOException exception)
      {
         throw new DriverException(exception);
      }
      catch (InvalidDateException exception) {}
      return false;
   }
   
   /**
    * Indicates if a column of the result set is not null.
    * 
    * @param columnName The column name.
    * @return <code>true</code> if the column is not null; <code>false</code>, otherwise. 
    * @throws DriverException If the result set or the driver is closed, or if the column was not found.
    */
   public boolean isNotNull(String columnName) throws DriverException
   {
      ResultSet resultSet = rs;
      if (resultSet.table == null) // guich@_564_4: the result set can't be closed.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_RESULTSETMETADATA_CLOSED));
      if (resultSet.driver.htTables == null) // juliana@227_4: the connection where the result set was created can't be closed while using it.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_DRIVER_CLOSED));
      
      SQLResultSetField[] fields = resultSet.fields;
      SQLResultSetField field;
      int i = -1, 
          len = fields.length;

      while (++i < len) // Gets the name of the table or its alias given the column name and gets the table column info.
         if (columnName.equalsIgnoreCase((field = fields[i]).tableColName) || columnName.equalsIgnoreCase(fields[i].alias))
            try
            {
               return ((resultSet.driver.getTable(field.tableName).columnAttrs[field.tableColIndex >= 0? field.tableColIndex 
                                                                  : field.parameter.tableColIndex]) & Utils.ATTR_COLUMN_IS_NOT_NULL) != 0;
            }
            catch (IOException exception)
            {
               throw new DriverException(exception);
            }
            catch (InvalidDateException exception) {}
      
      if (i == len)
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COLUMN_NOT_FOUND));
      
      return false;
   }
}
