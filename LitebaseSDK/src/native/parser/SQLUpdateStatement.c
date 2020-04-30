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
 * Defines the functions to initialize, set, and process an update statement.
 */

#include "SQLUpdateStatement.h"

/**
 * Constructs an update statement given the result of the parsing process.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param parser The result of the parsing process.
 * @param isPrepared Indicates if the delete statement is from a prepared statement.
 * @return A pointer to a <code>SQLUpdateStatement</code> structure or <code>null</code> if an error occurs. 
 * @throws SQLParseException If there is a field named "rowid".
 * @throws OutOfMemoryError If a heap memory allocation fails. 
 */
SQLUpdateStatement* initSQLUpdateStatement(Context context, TCObject driver, LitebaseParser* parser, bool isPrepared)
{
	TRACE("sqlUpdateStatement")
   Heap heap = parser->heap;

	// Creates a new update statement.
	SQLUpdateStatement* updateStmt = (SQLUpdateStatement*)TC_heapAlloc(heap, sizeof(SQLUpdateStatement));
   SQLBooleanClause* whereClause = updateStmt->whereClause = parser->whereClause;

	int32 i = updateStmt->nValues = parser->fieldValuesSize;
	Table* table;
	JCharP value;
	SQLValue* record;
	CharP* fields;
	
	updateStmt->heap = heap;
   updateStmt->type = CMD_UPDATE;

	if (isPrepared) // Some structures from the parser does not need to be reallocated when not using prepared statements.
	{
		updateStmt->rsTable = initSQLResultSetTable((*parser->tableList)->tableName, (*parser->tableList)->aliasTableName, heap);
      fields = updateStmt->fields = (CharP*)TC_heapAlloc(heap, i * TSIZE);
	   xmemmove(updateStmt->fields, parser->fieldNames, i * TSIZE);
		if (whereClause)
		{
			whereClause->fieldList = (SQLResultSetField**)TC_heapAlloc(heap, whereClause->fieldsCount * TSIZE);
			xmemmove(whereClause->fieldList, parser->whereFieldList, whereClause->fieldsCount * TSIZE);
			whereClause->paramList = (SQLBooleanClauseTree**)TC_heapAlloc(heap, whereClause->paramCount * TSIZE);
			xmemmove(whereClause->paramList, parser->whereParamList, whereClause->paramCount * TSIZE);
		}
	}
	else
	{
		updateStmt->rsTable = *parser->tableList;
		fields = updateStmt->fields = parser->fieldNames;
		if (whereClause)
		{
			whereClause->fieldList = parser->whereFieldList;
			whereClause->paramList = parser->whereParamList;
		}
	}
	
	// If it is not possible to load the table, frees the structures and returns.
	if (!(updateStmt->rsTable->table = table = getTable(context, driver, updateStmt->rsTable->tableName)))
		return null;

	// Alocates space for the record and the nulls.
	updateStmt->record = (SQLValue**)TC_heapAlloc(heap, i = table->columnCount * TSIZE);
	updateStmt->storeNulls = (uint8*)TC_heapAlloc(heap, NUMBEROFBYTES(i));

	// Allocates space for the list of the parameters. Worst case: all fields are parameters.
	updateStmt->paramIndexes = (uint8*)TC_heapAlloc(heap, i);
	updateStmt->paramDefined = (uint8*)TC_heapAlloc(heap, i);

   // juliana@262_1: now it is not allowed duplicated fields in an update statement.
   if ((i = updateStmt->nValues) >= table->columnCount)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DUPLICATED_COLUMN_NAME), "");
      return null;
   }    

   while (--i >= 0)
   {
      // juliana@230_40: rowid cannot be an update field.
      if (TC_hashCode(fields[i]) == HCROWID)
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_ROWID_CANNOT_BE_CHANGED), 0);
         return null;
      }   
      
      if ((value = parser->fieldValues[i])) // Only stores values that are not null.
      {
         record = updateStmt->record[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
         record->asChars = value;
         record->length = TC_JCharPLen(value);
      }
      else 
			setBit(updateStmt->storeNulls, i, true); 
   }

   return updateStmt;
}

/* 
 * Sets the value of a numeric parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param type The type of the parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @thows DriverException If the parameter type is incompatible with the column type.
 */
bool setNumericParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 type)
{
	TRACE("setNumericParamValueUpd")

	if (checkUpdateIndex(context, updateStmt, index)) // Checks if the index is within the range.
   {
      if (index < updateStmt->paramCount) // The parameter is in the update clause.
      {
         int32 i;
         SQLValue* record;
         
		   // Checks if the column type is the same of the value type.
         if (updateStmt->rsTable->table->columnTypes[i = updateStmt->paramIndexes[index]] != type)
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES), 0);
            return false;
         }

		   setUpdateRecord(updateStmt, index); // Sets the record in the given index.

		   // Sets the values of the parameter in its list.
         setBit(updateStmt->storeNulls, i, (record = updateStmt->record[i])->isNull = false);
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
            case FLOAT_TYPE : 
				   record->asFloat = (float)*((double*)value); 
				   break;
            case DOUBLE_TYPE : 
				   record->asDouble = *((double*)value); 
         }
         return true;
      }
      else // The parameter is in the where clause.
         return setNumericParamValue(context, updateStmt->whereClause->paramList[index - updateStmt->paramCount], value, type);
   }
   return false;
}

/* 
 * Sets the value of a string or blob parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param length The length of the string or blob.
 * @param isStr Indicates if the parameter is a string or a blob.
 * @throws SQLParserException If a <code>null</code> is used as a parameter of a where clause.
 * @thows DriverException If the parameter type is incompatible with the column type.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setStrBlobParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 length, bool isStr)
{
	TRACE("setStrBlobParamValueUpd")
   int32 i;
	SQLValue* record;
		
	if (checkUpdateIndex(context, updateStmt, index)) // Checks if the index is within the range.
   {
      if (index < updateStmt->paramCount) // The parameter is in the update clause.
      {
         // If the column is a blob, the value type must be a blob. 
         if ((!isStr && updateStmt->rsTable->table->columnTypes[i = updateStmt->paramIndexes[index]] != BLOB_TYPE)
		    || (isStr && updateStmt->rsTable->table->columnTypes[i = updateStmt->paramIndexes[index]] == BLOB_TYPE))
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES), 0);
            return false;
         }

		   setUpdateRecord(updateStmt, index); // Sets the record in the given index.
		   record = updateStmt->record[i];
		   
         if (value) // The value is not null.
         {
            if (isStr)
               record->asChars = value;
            else 
               record->asBlob = value;
            record->length = length;
            setBit(updateStmt->storeNulls, i, record->isNull = false);
         }
         else // The value is null.
            setBit(updateStmt->storeNulls, i, record->isNull = true);

         return true;
      }
      else // The parameter is in the where clause.
      {
         if (!value)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_PARAM_NULL)); 
            return false;
         }
         else
            return setParamValueString(context, updateStmt->whereClause->paramList[index - updateStmt->paramCount], value, length);
      }
   }
   return false;
}

// juliana@223_3: PreparedStatement.setNull() now works for blobs.
/**
 * Sets null in a given field. 
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @param index The index of the parameter.
 * @throws SQLParseException If the index is for the where clause.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setNullUpd(Context context, SQLUpdateStatement* updateStmt, int32 index)
{
	TRACE("setStrBlobParamValueUpd")
   int32 i;
	SQLValue* record;
		
	if (checkUpdateIndex(context, updateStmt, index)) // Checks if the index is within the range.
   {
      if (index < updateStmt->paramCount) // The parameter is in the update clause.
      {
		   setUpdateRecord(updateStmt, index); // Sets the record in the given index.

         // The value is null.
         (record = updateStmt->record[i = updateStmt->paramIndexes[index]])->asChars = null;
         record->asBlob = null;
         record->length = 0;
         setBit(updateStmt->storeNulls, i, record->isNull = true);

         return true;
      }
      else // The parameter is in the where clause.
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_PARAM_NULL));
         return false;
      }
   }
   return false;
}

/**
 * Throws an exception if the index to set a parameter in the update prepared statement is invalid.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL insert statement.
 * @param index The index of the parameter.
 * @throws IllegalArgumentException If the parameter index is invalid.
 */
bool checkUpdateIndex(Context context, SQLUpdateStatement* updateStmt, int32 index)
{
   TRACE("checkUpdateIndex")

   // Checks if the index is within the range.
   if (index < 0 || index >= updateStmt->paramCount + (updateStmt->whereClause? updateStmt->whereClause->paramCount : 0)) 
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
      return false;
   }
   return true;
}

/**
 * Set a record position for an update prepared statement.
 *
 * @param updateStmt A SQL insert statement.
 * @param index The index of the parameter.
 */
void setUpdateRecord(SQLUpdateStatement* updateStmt, int32 index)
{
   TRACE("setUpdateRecord")
   int32 i = updateStmt->paramIndexes[index];
   
   // It is not necessary to re-alocate a record value.
   if (updateStmt->record[i])
	   xmemzero(updateStmt->record[i], sizeof(SQLValue));
   else
      updateStmt->record[i] = (SQLValue*)TC_heapAlloc(updateStmt->heap, sizeof(SQLValue));

   // Sets the values of the parameter in its list.
   updateStmt->paramDefined[index] = true;
}

/**
 * Clears all parameter values of a prepared statement update.
 *
 * @param updateStmt A SQL update statement.
 */
void clearParamValuesUpd(SQLUpdateStatement* updateStmt)
{
	TRACE("clearParamValuesUpd")
   int32 i = updateStmt->paramCount,
			j;
	uint8* paramIndexes = updateStmt->paramIndexes;
	uint8* paramDefined = updateStmt->paramDefined;
	uint8* storeNulls = updateStmt->storeNulls;
	SQLValue** record =updateStmt->record;
	SQLBooleanClause* whereClause = updateStmt->whereClause;
	while (--i >= 0) // Cleans the parameter values of the update clause.
   {
      xmemzero(record[j = paramIndexes[i]], sizeof(SQLValue));
      setBit(storeNulls, j, paramDefined[j] = false);
	   record[j]->isNull = true;
   }

   if (whereClause) // Cleans the parameter values of the where clause.
	{
		SQLBooleanClauseTree** paramList = whereClause->paramList;
		i = whereClause->paramCount;
      while (--i >= 0)
         paramList[i]->isParamValueDefined = false;
	}
}

/**
 * Checks if all parameters values are defined.
 *
 * @param updateStmt A SQL update statement.
 * @return <code>true</code>, if all parameters values are defined; <code>false</code> otherwise.
 */
bool allParamValuesDefinedUpd(SQLUpdateStatement* updateStmt)
{
	TRACE("allParamValuesDefinedUpd")
   int32 i = updateStmt->paramCount;
	uint8* paramDefined = updateStmt->paramDefined;
	SQLBooleanClause* whereClause = updateStmt->whereClause;

   while (--i >= 0) // Checks if all the parameters of the update clause are defined.
      if (!paramDefined[i])
         return false;

   if (whereClause) // Checks if all pararameters of the where clause are defined.
	{	
      SQLBooleanClauseTree** paramList = whereClause->paramList;
		i = whereClause->paramCount;
		while (--i >= 0)
         if (!paramList[i]->isParamValueDefined)
            return false;
	}

   return true;
}

/**
 * Executes an update statement.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @return The number of rows that were updated, or -1 if an error occurs.
 * @throws OutOfMemoryError If a memory allocation fails.
 * @throws DriverException If the table is not set. 
 */
int32 litebaseDoUpdate(Context context, SQLUpdateStatement* updateStmt)
{
	TRACE("litebaseDoUpdate")
   Table* table = updateStmt->rsTable->table;
   SQLValue** record = updateStmt->record;
   int32 nn;
   ResultSet* rs;
	Heap heap = heapCreate();
	
	IF_HEAP_ERROR(heap)
	{
		TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
	   goto error;
	}

   if (!table)
	{
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_CANT_READ), updateStmt->rsTable->tableName);
		goto error;
	}

   // juliana@250_10: removed some cases when a table was marked as not closed properly without being changed.
   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   // Verifies if there are any parameters missing and the nulls do not violate a null restriction.
   // Creates the result set that will be used to update the rows.
   if (!verifyNullValues(context, table, record, CMD_UPDATE, updateStmt->nValues)
	 || !sqlBooleanClausePreVerify(context, updateStmt->whereClause)
    || !(rs = createSimpleResultSet(context, table, updateStmt->whereClause, heap))
    || !setModified(context, table))
	   goto error;
   
   nn = 0;
   
   // juliana@223_14: solved possible memory problems.
   while (true)
   {
      IF_HEAP_ERROR(heap)
	   {
		   TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
		   goto error;
	   }
      if (!getNextRecord(context, rs, heap))
         break;
      if (writeRecord(context, table, record, rs->pos, heap))
         nn++;
      else
         goto error;
   }
   heapDestroy(heap);
   return nn;

error:
   heapDestroy(heap);
   return -1;
}

/**
 * Binds an update statement.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 * @throws <code>SQLParseException</code> if the number of fields is greater than 254. 
 */
bool litebaseBindUpdateStatement(Context context, SQLUpdateStatement* updateStmt)
{
	TRACE("litebaseBindUpdateStatement")
   int32 i = -1, 
	      valuesCount = updateStmt->nValues, 
			paramCount = 0;

   Table *table = updateStmt->rsTable->table; // Gets the statement base table.
   SQLValue** record = updateStmt->record;
   CharP* fields = updateStmt->fields;
   uint8* paramIndexes = updateStmt->paramIndexes;
   uint8* storeNulls = updateStmt->storeNulls;
   JCharP asChars;

   // juliana@227_17: corrected a possible crash if one tries to update more than 128 fields in a table.
   if (valuesCount > MAXIMUMS)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_MAX_NUM_FIELDS_REACHED), 0);
      return false;
   }

   while (++i < valuesCount) // Checks if there are undefined values.
	   // Identifies the values that are placeholders for parameters.
      if (record[i] && (asChars = record[i]->asChars) && asChars[0] == (JChar)'?' && !asChars[1]) 
         paramIndexes[paramCount++] = i;

   updateStmt->paramCount = paramCount;

   // Makes sure the fields are in correct order, aligned with the table order.
   if (!reorder(context, table, fields, record, storeNulls, &updateStmt->nValues, paramIndexes))
		return false;
	
   updateStmt->record = record;
   xmemmove(storeNulls, table->storeNulls, NUMBEROFBYTES(table->columnCount));

   // Converts the values to be updated into its correct type and binds the where clause to its table. 
   if (!convertStringsToValues(context, table, record, updateStmt->nValues) 
    || (updateStmt->whereClause
     && !bindColumnsSQLBooleanClause(context, updateStmt->whereClause, &table->htName2index, table->columnTypes, &updateStmt->rsTable, 1, updateStmt->heap)))
		return false;

   return true;
}
