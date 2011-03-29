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

// $Id: SQLInsertStatement.java,v 1.6.4.5.2.5.4.55 2011-01-03 20:05:15 juliana Exp $

package litebase;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.InvalidDateException;

/**
 * Internal use only. Represents a SQL <code>INSERT</code> statement.
 */
class SQLInsertStatement extends SQLStatement
{
   /** 
    * The base table used by the SQL expression. 
    */
   Table table;
   
   /**
    * The table name.
    */
   String tableName;
   
   /**
    * The fields used if the insert statement is not using the default order.
    */
   String[] fields;

   /**
    * The record to be inserted.
    */
   SQLValue[] record;

   /**
    * The number of the parameters if the insert statement is a preprared statement.
    */
   int paramCount;

   /**
    * The array with the indexes of the parameters.
    */
   byte[] paramIndexes;

   /**
    * An array that indicates if a parameters is defined or not.
    */
   boolean[] paramDefined;

   /**
    * An array that indicates if a null value will be stored in a field.
    */
   boolean[] storeNulls;

   /**
    * Constructs an insert statement given the result of the parsing process.
    *
    * @param parser The result of the parsing process.
    * @param driver The connection with Litebase
    * @throws SQLParseException If there is a field named "rowid".
    * @throws InvalidDateException If an internal method throws it.
    * @throws IOException If an internal method throws it.
    */
   SQLInsertStatement(LitebaseParser parser, LitebaseConnection driver) throws SQLParseException, InvalidDateException, IOException
   {
      int nFields = parser.fieldNamesSize + 1; // Number of fields + rowid.
      String value;

      type = SQLElement.CMD_INSERT;

      // On Litebase, a table has no alias name on insert. This has no sense. So the same name of the table will be used as an alias. The parser must
      // be changed to understand the alias table name.

      table = driver.getTable(tableName = parser.tableList[0].tableName); // Gets the statement base table.
      
      if (parser.fieldNamesSize != 0) // Checks if it is not using the default order.
      {
         // Gets the fields and stores them.
         String[] fieldsAux = fields = new String[nFields]; 
         String[] fieldNames = parser.fieldNames;
         storeNulls = new boolean[nFields];
         while (--nFields > 0)
            // A field cannot have the same hash code of the rowid.
            if ((fieldsAux[nFields] = fieldNames[nFields - 1]).hashCode() == SQLElement.hcRowId)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_ROWID_CANNOT_BE_CHANGED));
      }
      else // The nulls info does not need to be recreated when all the fields are used in the insert.
         Convert.fill(storeNulls = table.storeNulls, 0, table.columnCount, false);

      // Number of fields + rowid.
      SQLValue[] recordAux = record = SQLValue.newSQLValues(nFields = parser.fieldValuesSize + 1);  // Gets the values and stores them.
      boolean[] storeNullsAux = storeNulls;
      String[] fieldValues = parser.fieldValues;
      
      // Allocates space for the list of the parameters. Worst case: all fields are parameters.
      paramIndexes = new byte[nFields]; 
      paramDefined = new boolean[nFields];

      while (--nFields > 0)
         if ((value = fieldValues[nFields - 1]) != null) // Only stores values that are not null.
            recordAux[nFields].asString = value;
         else
            storeNullsAux[nFields] = recordAux[nFields].isNull = true;
   }

   /**
    * Sets the value of a short parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the short parameter.
    * @throws DriverException If the parameter index is invalid or the column type is not short.
    */
   void setParamValue(int index, short val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));

      int idx = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      
      if (table.columnTypes[idx] != SQLElement.SHORT) // The type must be short.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[idx];
      value.asShort = val;
      paramDefined[index] = true;
      
      value.isNull = storeNulls[idx] = false; // The value is not null.
   }

   /**
    * Sets the value of an integer parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the integer parameter.
    * @throws DriverException If the parameter index is invalid or the column type is not int.
    */
   void setParamValue(int index, int val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));

      int idx = paramIndexes[index] & 0xFF;
      
      if (table.columnTypes[idx] != SQLElement.INT) // The type must be int.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[idx];
      value.asInt = val;
      paramDefined[index] = true;
      
      value.isNull = storeNulls[idx] = false; // The value is not null.
   }

   /**
    * Sets the value of a long parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the long parameter.
    * @throws DriverException If the parameter index is invalid or the column type is not long.
    */
   void setParamValue(int index, long val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));

      int idx = paramIndexes[index] & 0xFF;
      
      if (table.columnTypes[idx] != SQLElement.LONG) // The type must be long.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // Sets the values of the parameter in its list.
      record[idx].asLong = val;
      paramDefined[index] = true;
      
      record[idx].isNull = storeNulls[idx] = false;  // The value is not null.
   }

   /**
    * Sets the value of a float parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the float parameter.
    * @throws DriverException If the parameter index is invalid or the column type is not float.
    */
   void setParamValue(int index, float val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));

      int idx = paramIndexes[index] & 0xFF;
      
      if (table.columnTypes[idx] != SQLElement.FLOAT) // The type must be float.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));

      // Sets the values of the parameter in its list.
      SQLValue value = record[idx];
      value.asDouble = val;
      paramDefined[index] = true;
      
      value.isNull = storeNulls[idx] = false; // The value is not null.
   }

   /**
    * Sets the value of a double parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the double parameter.
    * @throws DriverException If the parameter index is invalid or the column type is not double.
    */
   void setParamValue(int index, double val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));

      int idx = paramIndexes[index] & 0xFF;
      
      if (table.columnTypes[idx] != SQLElement.DOUBLE) // The type must be double. 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[idx];
      value.asDouble = val;
      paramDefined[index] = true;
      
      value.isNull = storeNulls[idx] = false; // The value is not null.
   }

   /**
    * Sets the value of a string parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setParamValue(int index, String val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      int idx = paramIndexes[index] & 0xFF; 
      
      if (table.columnTypes[idx] == SQLElement.BLOB) // The type can't be a blob. 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_STRING));
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[idx];
      value.asString = val; 
      storeNulls[idx] = value.isNull = val == null; 

      paramDefined[index] = true;
   }

   /**
    * Sets the value of a array of bytes (blob) parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws DriverException If the parameter index is invalid or the column type is not blob.
    */
   void setParamValue(int index, byte[] val) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      int idx = paramIndexes[index] & 0xFF;
      
      if (table.columnTypes[idx] != SQLElement.BLOB) // The type must be blob. 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[idx];
      value.asBlob = val; 
      storeNulls[idx] = value.isNull = val == null; 
      paramDefined[index] = true;
   }
   
   // juliana@223_3: PreparedStatement.setNull() now works for blobs.
   /**
    * Sets null in a given field. 
    *
    * @param index The index of the parameter.
    * @throws DriverException If the parameter index is invalid.
    */
   void setNull(int index) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      int idx = paramIndexes[index] & 0xFF;
      SQLValue value = record[idx]; 
      
      // Sets the null value.
      value.asBlob = null; 
      value.asString = null;
      storeNulls[idx] = value.isNull = paramDefined[index] = true;
   }

   /**
    * Clears all parameter values of a prepared statement insert.
    */
   void clearParamValues()
   {
      int i = paramCount,
          j;
      SQLValue value;
      SQLValue[] recordAux = record;
      byte[] paramIndexesAux = paramIndexes;
      boolean[] storeNullsAux = storeNulls;
      
      totalcross.sys.Convert.fill(paramDefined, 0, paramDefined.length, false);

      while (--i >= 0)
      {
         (value = recordAux[j = paramIndexesAux[i] & 0xFF]).asString = null;
         value.asBlob = null;
         value.isNull = storeNullsAux[j] = false;
      }
   }

   /**
    * Checks if all parameters values are defined.
    *
    * @return This method always returns <code>true</code> because if a parameter is not defined, <code>null</code> willbe inserted instead.
    */
   boolean allParamValuesDefined()
   {
      return true;
   }
   
   /**
    * Executes an insert statement.
    *
    * @param driver A connection with Litebase.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void litebaseDoInsert(LitebaseConnection driver) throws IOException, InvalidDateException
   {
      PlainDB plainDB = table.db;
      NormalFile dbFile = (NormalFile)plainDB.db;
      
      if (dbFile == null) // juliana@201_28: If a table is re-created after the prepared statement is parsed, there won't be a NPE.
         table = driver.getTable(tableName);
      
      // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since 
      // its last opening. 
      if (!table.isModified) // Sets the table as not closed properly.
      {
         dbFile.setPos(6);
         LitebaseConnection.oneByte[0] = (byte)(plainDB.isAscii? Table.IS_ASCII : 0);
         dbFile.writeBytes(LitebaseConnection.oneByte, 0, 1);
         dbFile.flushCache();
         table.isModified = true;
      }
      
      table.verifyNullValues(record, storeNulls,SQLElement.CMD_INSERT);
      table.writeRecord(record, -1);
   }

   /**
    * Binds an insert statement.
    *
    * @return A binded insert statement.
    * @throws SQLParseException If the number of values does not match the number of table fields.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException  If an internal method throws it.
    */
   SQLInsertStatement litebaseBindInsertStatement() throws SQLParseException, IOException, InvalidDateException, InvalidNumberException
   {
      int valuesCount = record.length, 
          i = 0;
      Table baseTable = table; // Gets the statement base table.
      
      paramCount = 0; 
      
      while (++i < valuesCount) // Checks if there are undefined values.
      {
         String string;
         if ((string = record[i].asString) != null && string.equals("?")) // Identifies the values that are placeholders for parameters.
            paramIndexes[paramCount++] = (byte)i;
      }

      if (fields != null) // No fields: The values are ordered.
         baseTable.reorder(this);

      if (record.length != baseTable.columnCount) // The record to be inserted size must math the table record size.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION));

      baseTable.convertStringsToValues(record);  // Converts the string values to their right types.
      return this;
   }
}