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

package litebase;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.InvalidDateException;

/**
 * Internal use only. Represents a SQL <code>UPDATE</code> statement.
 */
class SQLUpdateStatement extends SQLStatement
{
   /** 
    * The base table used by the SQL expression. 
    */
   SQLResultSetTable rsTable;
   
   /**
    * The where clause.
    */
   SQLBooleanClause whereClause;

   /**
    * The fields used to update a record.
    */
   String[] fields;

   /**
    * The record to be inserted.
    */
   SQLValue[] record;

   /**
    * The number of the parameters if the update statement is a preprared statement.
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
    * Constructs an update statement given the result of the parsing process.
    *
    * @param parser The result of the parsing process.
    */
   SQLUpdateStatement(LitebaseParser parser)
   {
      int nValues = parser.fieldValuesSize; // Gets the values.
      String value;
      
      type = SQLElement.CMD_UPDATE;
      rsTable = parser.tableList[0]; // Sets the result table.
      fields = new String[nValues]; // Create an array of fields.

      // Creates an array to store the fact that a value is null or not.
      record = SQLValue.newSQLValues(nValues);
      storeNulls = new boolean[nValues];

      // Stores the fields.
      Vm.arrayCopy(parser.fieldNames, 0, fields, 0, nValues);

      // Allocates space for the list of the parameters. Worst case: all fields are parameters.
      paramIndexes = new byte[nValues];
      paramDefined = new boolean[nValues];
      
      while (--nValues >= 0)
      {
         value = parser.fieldValues[nValues];
         if (value != null) // Only stores values that are not null.
            record[nValues].asString = value;
         else 
            storeNulls[nValues] = record[nValues].isNull = true;
      }
      
      if ((whereClause = parser.whereClause) != null) // Process the where clause, if it exists.
      {
         // Compacts the resulting field list.
         SQLResultSetField[] compactFieldList = new SQLResultSetField[whereClause.fieldsCount];
         Vm.arrayCopy(whereClause.fieldList, 0, compactFieldList, 0, whereClause.fieldsCount);
         whereClause.fieldList = compactFieldList;

         // Compacts the parameter list.
         SQLBooleanClauseTree[] compactParamList = new SQLBooleanClauseTree[whereClause.paramCount];
         Vm.arrayCopy(whereClause.paramList, 0, compactParamList, 0, whereClause.paramCount);
         whereClause.paramList = compactParamList;
      }
   }

   /**
    * Sets the value of a short parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the short parameter.
    * @throws DriverException If the column type is not short.
    */
   void setParamValue(int index, short val) throws DriverException
   {
      checkIndex(index); // Checks if the index is within the range.

      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF; // guich@lb225_1: masks out the sign bit in all reads of paramIndexes.
         
         if (rsTable.table.columnTypes[idx] != SQLElement.SHORT) // The type must be short.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));

         record[idx].asShort = val;
         paramDefined[index] = true;
         record[idx].isNull = storeNulls[idx] = false; // The value is not null.
      }
      else // The parameter is in the where clause.
         whereClause.paramList[index - paramCount].setParamValue(val);
   }

   /**
    * Sets the value of a integer parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the integer parameter.
    * @throws DriverException If the column type is not int.
    */
   void setParamValue(int index, int val) throws DriverException
   {
      checkIndex(index); // Checks if the index is within the range.

      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF;
         
         if (rsTable.table.columnTypes[idx] != SQLElement.INT) // The type must be int.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
         
         record[idx].asInt = val;
         paramDefined[index] = true;
         record[idx].isNull = storeNulls[idx] = false; // The value is not null.
      }
      else // The parameter is in the where clause.
         whereClause.paramList[index - paramCount].setParamValue(val);
   }

   /**
    * Sets the value of a long parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the long parameter.
    * @throws DriverException If the column type is not long.
    */
   void setParamValue(int index, long val) throws DriverException
   {
      checkIndex(index); // Checks if the index is within the range.
      
      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF;
         
         if (rsTable.table.columnTypes[idx] != SQLElement.LONG) // The type must be long.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
         
         record[idx].asLong = val;
         paramDefined[index] = true;
         record[idx].isNull = storeNulls[idx] = false; // The value is not null.
      }
      else // The parameter is in the where clause.
         whereClause.paramList[index - paramCount].setParamValue(val);
   }

   /**
    * Sets the value of a float parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the float parameter.
    * @throws DriverException If the column type is not float.
    */
   void setParamValue(int index, float val) throws DriverException
   {
      checkIndex(index); // Checks if the index is within the range.
      
      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF;
         
         if (rsTable.table.columnTypes[idx] != SQLElement.FLOAT) // The type must be float.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
         
         record[idx].asDouble = val;
         paramDefined[index] = true;
         record[idx].isNull = storeNulls[idx] = false; // The value is not null.
      }
      else // The parameter is in the where clause.
         whereClause.paramList[index - paramCount].setParamValue(val);
   }

   /**
    * Sets the value of a double parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the double parameter.
    * @throws DriverException If the column type is not double.
    */
   void setParamValue(int index, double val) throws DriverException
   {
      checkIndex(index); // Checks if the index is within the range.
      
      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF;
         
         if (rsTable.table.columnTypes[idx] != SQLElement.DOUBLE) // The type must be double.
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
         
         record[idx].asDouble = val;
         paramDefined[index] = true;
         record[idx].isNull = storeNulls[idx] = false; // The value is not null.
      }
      else // The parameter is in the where clause.
         whereClause.paramList[index - paramCount].setParamValue(val);
   }

   /**
    * Sets the value of a string parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws DriverException If the column type is BLOB.
    * @throws SQLParserException If a <code>null</code> is used as a parameter of a where clause.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void setParamValue(int index, String val) throws DriverException, SQLParseException, InvalidNumberException, InvalidDateException
   {
      checkIndex(index); // Checks if the index is within the range.
      
      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF;
         
         if (rsTable.table.columnTypes[idx] == SQLElement.BLOB) // The type can't be a blob. 
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_STRING));
         
         record[idx].asString = val; // Sets the values of the parameter in its list.
         if (val != null) // The value is not null.
            record[idx].isNull = storeNulls[idx] = false;
         else // The value is null.
            record[idx].isNull = storeNulls[idx] = true;
         paramDefined[index] = true;
      }
      else // The parameter is in the where clause.
      {
         if (val == null) // A null can't be in a parameter of a where clause.
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PARAM_NULL)); 
         whereClause.paramList[index - paramCount].setParamValue(val);
      }
   }

   /**
    * Sets the value of a blob parameter at the given index.
    *
    * @param index The index of the parameter.
    * @param val The value of the string parameter.
    * @throws DriverException If the column type is not blob.
    * @throws SQLParseException If the index is for the where clause.
    */
   void setParamValue(int index, byte[] val) throws DriverException, SQLParseException
   {
      checkIndex(index); // Checks if the index is within the range.
      
      if (index >= paramCount) // // The parameter is in the where clause.
         // Since blobs can't be in the were clause, an exception will be raised.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_WHERE));
         
      // The parameter is in the update clause.
      int idx = paramIndexes[index] & 0xFF;
      
      if (rsTable.table.columnTypes[idx] != SQLElement.BLOB) // The type must be blob. 
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      record[idx].asBlob = val; // Stores the value.
      
      if (val != null) // The value is not null.
         record[idx].isNull = storeNulls[idx] = false;
      else // The value is null.
         record[idx].isNull = storeNulls[idx] = true;
      paramDefined[index] = true;
   }
   
   // juliana@223_3: PreparedStatement.setNull() now works for blobs.
   /**
    * Sets null in a given field. 
    *
    * @param index The index of the parameter.
    * @throws SQLParseException If the index is for the where clause.
    */
   void setNull(int index) throws SQLParseException
   {
      checkIndex(index); // Checks if the index is within the range.
      
      if (index < paramCount) // The parameter is in the update clause.
      {
         int idx = paramIndexes[index] & 0xFF;
         SQLValue value = record[idx];
         
         // Sets the null value in its list.
         value.asString = null; 
         value.asBlob = null;
         value.isNull = storeNulls[idx] = paramDefined[index] = true;
      }
      else // The parameter is in the where clause.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PARAM_NULL)); 
   }

   /**
    * Clear all parameter values of a prepared statement update.
    */
   void clearParamValues()
   {
      int i = paramCount;
      SQLValue value;
      SQLBooleanClause clause = whereClause;

      totalcross.sys.Convert.fill(paramDefined, 0, paramDefined.length, false); // Cleans the parameter values of the update clause.

      while (--i >= 0)
      {
         int j = paramIndexes[i] & 0xFF;
         value = record[j];
         value.asString = null;
         value.asBlob = null;
         value.isNull = storeNulls[j] = false;
      }

      if (clause != null) // Cleans the parameter values of the where clause.
      {
         SQLBooleanClauseTree[] paramList = clause.paramList;
         i = clause.paramCount;
         while (--i >= 0)
            paramList[i].isParamValueDefined = false;
      }
   }

   /**
    * Checks if all parameters values are defined.
    *
    * @throws DriverException If not all parameters values are defined.
    */
   void allParamValuesDefined()
   {
      int i = paramCount;
      boolean[] paramDefinedAux = paramDefined;
      SQLBooleanClause clause = whereClause;

      // Checks if all the parameters of the update clause are defined.
      while (--i >= 0)
         if (!paramDefinedAux[i])
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_ALL_PARAMETERS_DEFINED));

      if (clause != null)
      {
         SQLBooleanClauseTree[] paramList = clause.paramList;
         
         // Checks if all pararameters of the where clause are defined.
         i = clause.paramCount;
         while (--i >= 0)
            if (!paramList[i].isParamValueDefined)
               throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_ALL_PARAMETERS_DEFINED));
      }
   }
   
   /**
    * Executes an update statement.
    *
    * @param driver A connection with Litebase.
    * @return The number of rows updated.
    * @throws IOException If an internal method throws it. 
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it. 
    */
   int litebaseDoUpdate(LitebaseConnection driver) throws IOException, InvalidDateException, InvalidNumberException
   {
      if (rsTable.table.db.db == null) // juliana@201_28: If a table is re-created after the prepared statement is parsed, there won't be a NPE.
         rsTable.table = driver.getTable(rsTable.tableName);
      
      Table table = rsTable.table;
      PlainDB plainDB = table.db;
      NormalFile dbFile = (NormalFile)plainDB.db;
      
      // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since 
      // its last opening. 
      if (!table.isModified) // Sets the table as not closed properly.
      {
         dbFile.setPos(6);
         
         // juliana@230_13: removed some possible strange behaviours when using threads.
         driver.oneByte[0] = (byte)(plainDB.isAscii? Table.IS_ASCII : 0);
         dbFile.writeBytes(driver.oneByte, 0, 1);
         
         dbFile.flushCache();
         table.isModified = true;
      }
      
      int records = 0;
      
      table.verifyNullValues(record, storeNulls, SQLElement.CMD_UPDATE);
      ResultSet rs = table.createSimpleResultSet(whereClause); // Creates the result set that will be used to update the rows.
     
      if (whereClause != null)  // Verifies if there are any parameters missing.
         whereClause.sqlBooleanClausePreVerify();

      while (rs.getNextRecord()) 
      {
         table.writeRecord(record, rs.pos);
         records++;
      }
      
      return records;
   }

   // nowosad@200
   /**
    * Binds a <code>SQL UPDATE</code> expression.
    *
    * @param updateStmt The update statement to be binded.
    * @return The update statement binded.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it. 
    * @throws InvalidNumberException If an internal method throws it. 
    */
   SQLUpdateStatement litebaseBindUpdateStatement(LitebaseConnection driver) throws IOException, InvalidDateException, InvalidNumberException
   {
      Table table = rsTable.table = driver.getTable(rsTable.tableName); // Gets the statement table.
      int valuesCount = record.length;
      
      paramCount = 0;
      int i = -1;
      while (++i < valuesCount) // Checks if there are undefined values.
      {
         String string;
         if ((string = record[i].asString) != null && string.equals("?")) // Identifies the values that are placeholders for parameters.
            paramIndexes[paramCount++] = (byte)i;
      }

      table.reorder(this);  // Makes sure the fields are in correct order, aligned with the table order.
      table.convertStringsToValues(record); // Converts the values to be updated into its correct type.

      if (whereClause != null) // Binds the where clause to its table.
         whereClause.bindColumnsSQLBooleanClause(table.htName2index, table.columnTypes, rsTable);

      return this;
   }
   
   // juliana@230_28: if a public method receives an invalid argument, now an IllegalArgumentException will be thrown instead of a DriverException.   
   /**
    * Checks the prepared statement parameter index.
    * 
    * @param index The index of the prepared statement parameter.
    * @throws IllegalArgumentException If the index is out of range.
    */
   void checkIndex(int index) throws IllegalArgumentException
   {
      if (index < 0 || index >= (paramCount + (whereClause == null? 0 : whereClause.paramCount))) // Checks if the index is within the range.
         throw new IllegalArgumentException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_PARAMETER_INDEX));
   }
}
