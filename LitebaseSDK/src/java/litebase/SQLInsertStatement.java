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

   // juliana@253_14: corrected a possible AIOBE if the number of parameters of a prepared statement were greater than 128.
   /**
    * The array with the indexes of the parameters.
    */
   short[] paramIndexes; 

   /**
    * An array that indicates if a parameters is defined or not.
    */
   boolean[] paramDefined;

   /**
    * An array that indicates if a null value will be stored in a field.
    */
   byte[] storeNulls;

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
         storeNulls = new byte[(nFields + 7) >> 3];
         while (--nFields > 0)
            // A field cannot have the same hash code of the rowid.
            if ((fieldsAux[nFields] = fieldNames[nFields - 1]).hashCode() == SQLElement.hcRowId)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_ROWID_CANNOT_BE_CHANGED));
      }
      else // The nulls info does not need to be recreated when all the fields are used in the insert.
         Convert.fill(storeNulls = table.storeNulls, 0, storeNulls.length, 0);

      // Number of fields + rowid.
      SQLValue[] recordAux = record = SQLValue.newSQLValues(nFields = parser.fieldValuesSize + 1);  // Gets the values and stores them.
      byte[] storeNullsAux = storeNulls;
      String[] fieldValues = parser.fieldValues;
      
      // Allocates space for the list of the parameters. Worst case: all fields are parameters.
      // juliana@253_14: corrected a possible AIOBE if the number of parameters of a prepared statement were greater than 128.
      paramIndexes = new short[nFields]; 
      paramDefined = new boolean[nFields];

      while (--nFields > 0)
         if ((value = fieldValues[nFields - 1]) != null) // Only stores values that are not null.
            recordAux[nFields].asString = value;
         else
            Utils.setBit(storeNullsAux, nFields, recordAux[nFields].isNull = true);
   }

   /**
    * Sets the value of a short parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the short parameter.
    */
   void setParamValue(int index, short val)
   {
      int columnIndex = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      checkIndex(index, columnIndex, SQLElement.SHORT, SQLElement.UNDEFINED); // Checks the parameter index and type.
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asShort = val;
      
      Utils.setBit(storeNulls, columnIndex, value.isNull = false); // The value is not null. 
   }

   /**
    * Sets the value of an integer parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the integer parameter.
    */
   void setParamValue(int index, int val)
   {
      int columnIndex = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      checkIndex(index, columnIndex, SQLElement.INT, SQLElement.UNDEFINED); // Checks the parameter index and type.
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asInt = val;
      
      Utils.setBit(storeNulls, columnIndex, value.isNull = false); // The value is not null. 
   }

   /**
    * Sets the value of a long parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the long parameter.
    */
   void setParamValue(int index, long val)
   {
      int columnIndex = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      checkIndex(index, columnIndex, SQLElement.LONG, SQLElement.UNDEFINED); // Checks the parameter index and type.
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asLong = val;
      
      Utils.setBit(storeNulls, columnIndex, value.isNull = false); // The value is not null. 
   }

   /**
    * Sets the value of a float parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the float parameter.
    */
   void setParamValue(int index, float val)
   {
      int columnIndex = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      checkIndex(index, columnIndex, SQLElement.FLOAT, SQLElement.UNDEFINED); // Checks the parameter index and type.
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asDouble = val;
      
      Utils.setBit(storeNulls, columnIndex, value.isNull = false); // The value is not null. 
   }

   /**
    * Sets the value of a double parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the double parameter.
    */
   void setParamValue(int index, double val) throws DriverException
   {
      int columnIndex = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      checkIndex(index, columnIndex, SQLElement.DOUBLE, SQLElement.UNDEFINED); // Checks the parameter index and type.
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asDouble = val;
      
      Utils.setBit(storeNulls, columnIndex, value.isNull = false); // The value is not null. 
   }

   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.   
   /**
    * Sets the value of a string parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws DriverException If the parameter column type is BLOB.
    * @throws IllegalArgumentException If the index is out of range.
    */
   void setParamValue(int index, String val) throws DriverException, IllegalArgumentException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      int columnIndex = paramIndexes[index] & 0xFF; 
      
      if (table.columnTypes[columnIndex] == SQLElement.BLOB) // The type can't be a blob. 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_STRING));
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asString = val; 
      Utils.setBit(storeNulls, columnIndex, value.isNull = (val == null)); // Sets whether the value is or not null.

      paramDefined[index] = true;
   }

   /**
    * Sets the value of a array of bytes (blob) parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    */
   void setParamValue(int index, byte[] val)
   {
      int columnIndex = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
      checkIndex(index, columnIndex, SQLElement.BLOB, SQLElement.UNDEFINED); // Checks the parameter index and type.
      
      // Sets the values of the parameter in its list.
      SQLValue value = record[columnIndex];
      value.asBlob = val; 
      Utils.setBit(storeNulls, columnIndex, value.isNull = (val == null)); // Sets whether the value is or not null. 
   }
   
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.   
   // juliana@223_3: PreparedStatement.setNull() now works for blobs.
   /**
    * Sets null in a given field. 
    *
    * @param index The index of the parameter.
    * @throws IllegalArgumentException If the index is out of range.
    */
   void setNull(int index) throws DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range of the parameter count.
         throw new IllegalArgumentException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
      
      int columnIndex = paramIndexes[index] & 0xFF;
      SQLValue value = record[columnIndex]; 
      
      // Sets the null value.
      value.asBlob = null; 
      value.asString = null;
      Utils.setBit(storeNulls, columnIndex, value.isNull = paramDefined[index] = true); // Sets whether the value is or not null.
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
      
      // juliana@253_14: corrected a possible AIOBE if the number of parameters of a prepared statement were greater than 128.
      short[] paramIndexesAux = paramIndexes;
      byte[] storeNullsAux = storeNulls;
      
      Convert.fill(paramDefined, 0, paramDefined.length, false);
      
      while (--i >= 0)
      {
         (value = recordAux[j = paramIndexesAux[i] & 0xFF]).asString = null;
         Utils.setBit(storeNullsAux, j, value.isNull = false);
         value.asBlob = null;
      }
   }

   /**
    * Does nothing. If a parameter is not defined, <code>null</code> will be inserted instead.
    */
   void allParamValuesDefined() {}
   
   /**
    * Executes an insert statement.
    *
    * @param driver A connection with Litebase.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void litebaseDoInsert(LitebaseConnection driver) throws IOException, InvalidDateException
   {
      Table tableAux = table;
      PlainDB plainDB = tableAux.db;
      NormalFile dbFile = (NormalFile)plainDB.db;
      
      if (dbFile == null) // juliana@201_28: If a table is re-created after the prepared statement is parsed, there won't be a NPE.
         table = tableAux = driver.getTable(tableName);
      
      tableAux.verifyNullValues(record, storeNulls,SQLElement.CMD_INSERT);
      
      // juliana@250_10: removed some cases when a table was marked as not closed properly without being changed.
      // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since 
      // its last opening. 
      tableAux.setModified(); // Sets the table as not closed properly.
      
      tableAux.writeRecord(record, -1);
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
            paramIndexes[paramCount++] = (short)i;
      }

      if (fields != null) // No fields: The values are ordered.
         baseTable.reorder(this);

      if (record.length != baseTable.columnCount) // The record to be inserted size must math the table record size.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION));

      baseTable.convertStringsToValues(record);  // Converts the string values to their right types.
      return this;
   }
   
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.   
   /**
    * Checks the prepared statement parameter index.
    * 
    * @param index The index of the prepared statement parameter.
    * @param columnIndex The column index of the parameter.
    * @param type1 The main column type of the method.
    * @param type2 Used only for strings to test if the column is a char nocase.
    * @throws IllegalArgumentException If the index is out of range.
    * @throws DriverException If the column is not of type requested. 
    */
   void checkIndex(int index, int columnIndex, int type1, int type2) throws IllegalArgumentException, DriverException
   {
      if (index < 0 || index >= paramCount) // Checks if the index is within the range.
         throw new IllegalArgumentException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
   
      int type = table.columnTypes[columnIndex];
      if (type != type1 && type != type2) // Check the column type.
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // juliana@230_37: solved a possible bug when using prepared statements without issuing PreparedStatement.clearAllParameters().      
      record[columnIndex].asString = null;
      
      paramDefined[index] = true; // The parameter will be defined.
   }
}