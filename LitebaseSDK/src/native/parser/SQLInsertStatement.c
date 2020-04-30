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

/**
 * Defines the functions to initialize, set, and process an insert statement.
 */

#include "SQLInsertStatement.h"

/**
 * Constructs an insert statement given the result of the parsing process.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param parser The result of the parsing process.
 * @return A pointer to a <code>SQLInsertStatement</code> structure. 
 * @throws SQLParseException If there is a field named "rowid". 
 */
SQLInsertStatement* initSQLInsertStatement(Context context, TCObject driver, LitebaseParser* parser)
{
	TRACE("initSQLInsertStatement")
	Heap heap = parser->heap;
	uint32 i = parser->fieldNamesSize + 1;
	JCharP value;
	SQLValue* column;
	SQLValue** record;

	// Creates a new insert statement.
   SQLInsertStatement* insertStmt = (SQLInsertStatement*)TC_heapAlloc(heap, sizeof(SQLInsertStatement));
   
	// On Litebase, a table has no alias name on insert. This has no sense. So the same name of the table will be used as an alias. The parser must
   // be changed to understand the alias table name.
	// Gets the statement base table.
	Table *table = insertStmt->table = getTable(context, driver, insertStmt->tableName = (*parser->tableList)->tableName);  
	
	insertStmt->type = CMD_INSERT;
   insertStmt->heap = heap;

	// If it is not possible to load the table, frees the structures and returns.
   if (!table)
      return null;

   if (((LitebaseParser*)parser)->fieldNamesSize) // Checks if it is not using the default order.
	{
		// Gets the fields and stores them.
		CharP* fields = insertStmt->fields = (CharP*)TC_heapAlloc(heap, i * TSIZE);
      CharP* fieldNames = parser->fieldNames;
		
      *fields = null;
		insertStmt->storeNulls = TC_heapAlloc(heap, NUMBEROFBYTES(table->columnCount));

      while (--i)
			// A field cannot have the same hash code of the rowid.
         if (TC_hashCode(fields[i] = fieldNames[i - 1]) == HCROWID)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_ROWID_CANNOT_BE_CHANGED), 0);
            return null;
         }
	} 
	else // The nulls info does not need to be recreated when all the fields are used in the insert.
		xmemset(insertStmt->storeNulls = table->storeNulls, false, NUMBEROFBYTES(table->columnCount));

	// Allocates the record: number of fields + rowid.
	record = insertStmt->record = (SQLValue**)TC_heapAlloc(heap, (i = table->columnCount) * TSIZE);
    
   // Allocates space for the list of the parameters. Worst case: all fields are parameters.
	insertStmt->paramIndexes = (uint8*)TC_heapAlloc(heap, i);
	insertStmt->paramDefined = (uint8*)TC_heapAlloc(heap, i);

   // juliana@227_9: corrected a possible crash if the number of columns of the insert were greater than the one of table definition.
   if ((i = parser->fieldValuesSize + 1) > table->columnCount)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION), 0);
      return null;
   }
   insertStmt->nFields = i;
   
	*record = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
   while (--i)
   {
      if ((value = parser->fieldValues[i - 1])) // Only stores values that are not null.
      {
         column = record[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
         column->asChars = value;
         column->length = TC_JCharPLen(value);
      }
      else 
			setBit(insertStmt->storeNulls, i, true); 
   }
	return insertStmt;
}

/* 
 * Sets the value of a numeric parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param type The type of the parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @thows DriverException If the parameter type is incompatible with the column type.
 */
bool setNumericParamValueIns(Context context, SQLInsertStatement* insertStmt, int32 index, VoidP value, int32 type)
{
	TRACE("setNumericParamValueIns")
   int32 i;
   SQLValue* record;

   if (checkInsertIndex(context, insertStmt, index)) // Checks if the index is within the range of the parameter count.
   {
	   // Checks if the column type is the same of the value type.
	   if (insertStmt->table->columnTypes[i = insertStmt->paramIndexes[index]] != type)
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES), 0);
         return false;
      }

	   // Sets the values of the parameter in its list.
      insertStmt->paramDefined[index] = true;
      setBit(insertStmt->storeNulls, i, (record = insertStmt->record[i])->isNull = false);
      switch (type)
      {
         case SHORT_TYPE: 
			   record->asShort = *((int16*)value); 
			   break;
         case INT_TYPE: 
			   record->asInt = *((int32*)value); 
			   break;
         case LONG_TYPE: 
			   record->asLong = *((int64*)value); 
			   break;
         case FLOAT_TYPE: 
			   record->asFloat = (float)*((double*)value); 
			   break;
         case DOUBLE_TYPE: 
			   record->asDouble = *((double*)value); 
      }
      return true;
   }
   return false;
}

/* 
 * Sets the value of a string or blob parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param length The length of the string or blob.
 * @param isStr Indicates if the parameter is a string or a blob.
 * @thows DriverException If the parameter type is incompatible with the column type.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setStrBlobParamValueIns(Context context, SQLInsertStatement* insertStmt, int32 index, VoidP value, int32 length, bool isStr)
{
	TRACE("setStrBlobParamValueIns")
   int32 i;
	SQLValue* record;

   if (checkInsertIndex(context, insertStmt, index)) // Checks if the index is within the range of the parameter count.
   {
	   // If the column is a blob, the value type must be a blob. 
      if ((!isStr && insertStmt->table->columnTypes[i = insertStmt->paramIndexes[index]] != BLOB_TYPE)
	    || (isStr && insertStmt->table->columnTypes[i = insertStmt->paramIndexes[index]] == BLOB_TYPE))
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES), 0);
         return false;
      }

	   record = insertStmt->record[i];

	   // Sets the values of the parameter in its list.
      insertStmt->paramDefined[index] = true;
      if (value) // The value is not null.
      {
         if (isStr)
            record->asChars = value;
         else
            record->asBlob = value;
         record->length = length;
         setBit(insertStmt->storeNulls, i, record->isNull = false);
      }
      else // The value is null. 
		   setBit(insertStmt->storeNulls, i, record->isNull = true);

      return true;
   }
   return false;
}

// juliana@223_3: PreparedStatement.setNull() now works for blobs.
/**
 * Sets null in a given field. 
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * @param index The index of the parameter.
 * @throws DriverException If the parameter index is invalid.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setNullIns(Context context, SQLInsertStatement* insertStmt, int32 index)
{
   TRACE("setNull")
	SQLValue* record;
	int32 i;
   
   if (checkInsertIndex(context, insertStmt, index)) // Checks if the index is within the range of the parameter count.
   {
      // The value is null.
      (record = insertStmt->record[i = insertStmt->paramIndexes[index]])->length = 0;
      record->asChars = null;
      record->asBlob = null;
      
      // Sets the values of the parameter in its list.
      setBit(insertStmt->storeNulls, i, insertStmt->paramDefined[index] = record->isNull = true);
      
      return true;
   }
   return false;
}

/**
 * Throws an exception if the index to set a parameter in the insert prepared statement is invalid.
 * If the index is correct, it erases or creates the record.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * @param index The index of the parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws IllegalArgumentException If the parameter index is invalid.
 */
bool checkInsertIndex(Context context, SQLInsertStatement* insertStmt, int32 index)
{
   int32 i;
   
   if (index < 0 || index >= insertStmt->paramCount)
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalArgumentException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
      return false;
   }
   
   if (insertStmt->record[i = insertStmt->paramIndexes[index]])
      xmemzero(insertStmt->record[i], sizeof(SQLValue));
	else
		insertStmt->record[i] = (SQLValue*)TC_heapAlloc(insertStmt->heap, sizeof(SQLValue));
		
   return true;
}

/**
 * Clears all parameter values of a prepared statement insert.
 *
 * @param insertStmt A SQL insert statement.
 */
void clearParamValuesIns(SQLInsertStatement* insertStmt)
{
	TRACE("clearParamValuesIns")
   int32 i = insertStmt->paramCount,
	      j;
	uint8* paramIndexes = insertStmt->paramIndexes;
	uint8* paramDefined = insertStmt->paramDefined;
	uint8* storeNulls = insertStmt->storeNulls;
	SQLValue** record = insertStmt->record;
   
   xmemzero(paramDefined, i);
   
   while (--i >= 0)
   {
		xmemzero(record[j = paramIndexes[i]], sizeof(SQLValue));
      setBit(storeNulls, j, paramDefined[j] = false);
	   record[j]->isNull = true;
   }
}

/**
 * Executes an insert statement.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * return <code>true</code> if the insertion was performed successfully; <code>false</code>, otherwise.
 */
bool litebaseDoInsert(Context context, SQLInsertStatement* insertStmt)
{
	TRACE("litebaseDoInsert")
   Table* table = insertStmt->table;
   Heap heap = heapCreate(); // juliana@223_14: solved possible memory problems.

	IF_HEAP_ERROR(heap)
	{
		TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
	   goto error;
	}

   if (!table)
	{
		TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_CANT_READ), insertStmt->tableName);
      goto error;
	}

   // juliana@250_10: removed some cases when a table was marked as not closed properly without being changed.
   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening.
   // Verifies if the nulls do not violate a null restriction and writes the record.  
   if (!verifyNullValues(context, table, insertStmt->record, CMD_INSERT, 0)
    || !setModified(context, table)
    || !writeRecord(context, table, insertStmt->record, -1, heap))
      goto error;
      
   heapDestroy(heap);
   return true;

error:
   heapDestroy(heap);
   return false;
}

/**
 * Binds an insert statement.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 * @throws SQLParseException If the number of values inserted is different from the table definition.
 */
bool litebaseBindInsertStatement(Context context, SQLInsertStatement* insertStmt)
{
	TRACE("litebaseBindInsertStatement")
   int32 i = 0, 
	      valuesCount = insertStmt->nFields,
			paramCount = 0;
   Table* table = insertStmt->table; // Gets the statement base table.  
   uint8* storeNulls = insertStmt->storeNulls;
   SQLValue** record = insertStmt->record;
   CharP* fields = insertStmt->fields;
   uint8* paramIndexes = insertStmt->paramIndexes;

	while (++i < valuesCount) // Checks if there are undefined values.
	   // Identifies the values that are placeholders for parameters.
      if (record[i] && record[i]->asChars && record[i]->asChars[0] == (JChar)'?' && !record[i]->asChars[1]) 
         paramIndexes[paramCount++] = i;

   // No fields: The values are ordered.
   if (fields && !reorder(context, table, fields, record, storeNulls, &insertStmt->nFields, paramIndexes))
		return false;

   if (insertStmt->nFields != table->columnCount) // The record to be inserted size must math the table record size.
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION), insertStmt->nFields - 1);
		return false;
   }
   
   if (!convertStringsToValues(context, table, record, insertStmt->nFields)) // Converts the string values to their right types.
		return false;

   insertStmt->paramCount = paramCount;
   insertStmt->record = record;
   return true;
}
