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

// $Id: SQLUpdateStatement.c,v 1.1.2.6.4.62 2011-02-23 21:37:49 juliana Exp $

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
 * @return A pointer to a <code>SQLUpdateStatement</code> structure. 
 * @throws OutOfMemoryError If a heap memory allocation fails. 
 */
SQLUpdateStatement* initSQLUpdateStatement(Context context, Object driver, LitebaseParser* parser, bool isPrepared)
{
	TRACE("sqlUpdateStatement")
   Heap heap = parser->heap;

	// Creates a new update statement.
	SQLUpdateStatement* updateStmt = (SQLUpdateStatement*)TC_heapAlloc(heap, sizeof(SQLUpdateStatement));
   SQLBooleanClause* whereClause = updateStmt->whereClause = parser->whereClause;

	int32 i = updateStmt->nValues = parser->fieldValuesSize;
	Table* table;
	JCharP value;
	updateStmt->heap = heap;
   updateStmt->type = CMD_UPDATE;

	if (isPrepared) // Some structures from the parser does not need to be reallocated when not using prepared statements.
	{
		updateStmt->rsTable = initSQLResultSetTable((*parser->tableList)->tableName, (*parser->tableList)->aliasTableName, heap);
      updateStmt->fields = (CharP*)TC_heapAlloc(heap, i * PTRSIZE);
	   xmemmove(updateStmt->fields, parser->fieldNames, i * PTRSIZE);
		if (whereClause)
		{
			whereClause->fieldList = (SQLResultSetField**)TC_heapAlloc(heap, whereClause->fieldsCount << 2);
			xmemmove(whereClause->fieldList, parser->whereFieldList, whereClause->fieldsCount << 2);
			whereClause->paramList = (SQLBooleanClauseTree**)TC_heapAlloc(heap, whereClause->paramCount << 2);
			xmemmove(whereClause->paramList, parser->whereParamList, whereClause->paramCount << 2);
		}
	}
	else
	{
		updateStmt->rsTable = *parser->tableList;
		updateStmt->fields = parser->fieldNames;
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
	updateStmt->record = (SQLValue**)TC_heapAlloc(heap, i = table->columnCount * PTRSIZE);
	updateStmt->storeNulls = (uint8*)TC_heapAlloc(heap, i);

	// Allocates space for the list of the parameters. Worst case: all fields are parameters.
	updateStmt->paramIndexes = (uint8*)TC_heapAlloc(heap, i);
	updateStmt->paramDefined = (uint8*)TC_heapAlloc(heap, i);

	i = updateStmt->nValues;
   while (--i >= 0)
   {
      if ((value = parser->fieldValues[i])) // Only stores values that are not null.
      {
         updateStmt->record[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
         updateStmt->record[i]->asChars = value;
         updateStmt->record[i]->length = TC_JCharPLen(value);
      }
      else 
			updateStmt->storeNulls[i] = true; 
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
 * @thows DriverException If the parameter index is invalid or its type is incompatible with the column type.
 */
void setNumericParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 type)
{
	TRACE("setNumericParamValueUpd")
   int32 i;
	SQLBooleanClause* whereClause = updateStmt->whereClause;

	if (index < 0 || index >= updateStmt->paramCount + (whereClause? whereClause->paramCount : 0)) // Checks if the index is within the range.
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
   else
   if (index < updateStmt->paramCount) // The parameter is in the update clause.
   {
		// Checks if the column type is the same of the value type.
      if (updateStmt->rsTable->table->columnTypes[i = updateStmt->paramIndexes[index]] != type)
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES), 0);
         return;
      }

		// It is not necessary to re-alocate a record value.
      if (updateStmt->record[i])
			xmemzero(updateStmt->record[i], sizeof(SQLValue));
		else
         updateStmt->record[i] = (SQLValue*)TC_heapAlloc(updateStmt->heap, sizeof(SQLValue));

		// Sets the values of the parameter in its list.
      updateStmt->paramDefined[index] = true;
      updateStmt->storeNulls[i] = updateStmt->record[i]->isNull = false;
      switch (type)
      {
         case SHORT_TYPE: 
				updateStmt->record[i]->asShort = *((int16*)value); 
				break;
         case INT_TYPE: 
				updateStmt->record[i]->asInt = *((int32*)value); 
				break;
         case LONG_TYPE: 
				updateStmt->record[i]->asLong = *((int64*)value); 
				break;
         case FLOAT_TYPE : 
				updateStmt->record[i]->asFloat = (float)*((double*)value); 
				break;
         case DOUBLE_TYPE : 
				updateStmt->record[i]->asDouble = *((double*)value); 
      }
   }
   else // The parameter is in the where clause.
      setNumericParamValue(context, whereClause->paramList[index - updateStmt->paramCount], value, type);
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
 * @thows DriverException If the parameter index is invalid or its type is incompatible with the column type.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setStrBlobParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 length, bool isStr)
{
	TRACE("setStrBlobParamValueUpd")
   int32 i;
	SQLBooleanClause* whereClause = updateStmt->whereClause;
	SQLValue* record;
		
	if (index < 0 || index >= updateStmt->paramCount + (whereClause? whereClause->paramCount : 0)) // Checks if the index is within the range.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
      return false;
   }
   else if (index < updateStmt->paramCount) // The parameter is in the update clause.
   {
      // If the column is a blob, the value type must be a blob. 
      if ((!isStr && updateStmt->rsTable->table->columnTypes[i = updateStmt->paramIndexes[index]] != BLOB_TYPE)
		 || (isStr && updateStmt->rsTable->table->columnTypes[i = updateStmt->paramIndexes[index]] == BLOB_TYPE))
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES), 0);
         return false;
      }

		// It is not necessary to re-alocate a record value.
      if (updateStmt->record[i])
			xmemzero(record = updateStmt->record[i], sizeof(SQLValue));
		else
         record = updateStmt->record[i] = (SQLValue*)TC_heapAlloc(updateStmt->heap, sizeof(SQLValue));

		// Sets the values of the parameter in its list.
      updateStmt->paramDefined[index] = true;
      if (value) // The value is not null.
      {
         if (isStr)
            record->asChars = value;
         else 
            record->asBlob = value;
         record->length = length;
         updateStmt->storeNulls[i] = record->isNull = false;
      }
      else // The value is null.
         record->isNull = updateStmt->storeNulls[i] = true;

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
         return setParamValueString(context, whereClause->paramList[index - updateStmt->paramCount], value, length);
   }
}

// juliana@223_3: PreparedStatement.setNull() now works for blobs.
/**
 * Sets null in a given field. 
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @param index The index of the parameter.
 * @throws SQLParseException If the index is for the where clause.
 * @throws DriverException If the parameter index is invalid.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setNullUpd(Context context, SQLUpdateStatement* updateStmt, int32 index)
{
	TRACE("setStrBlobParamValueUpd")
   int32 i;
	SQLBooleanClause* whereClause = updateStmt->whereClause;
	SQLValue* record;
		
	if (index < 0 || index >= updateStmt->paramCount + (whereClause? whereClause->paramCount : 0)) // Checks if the index is within the range.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
      return false;
   }
   else if (index < updateStmt->paramCount) // The parameter is in the update clause.
   {
		// It is not necessary to re-alocate a record value.
      if (updateStmt->record[i = updateStmt->paramIndexes[index]])
			xmemzero(record = updateStmt->record[i], sizeof(SQLValue));
		else
         record = updateStmt->record[i] = (SQLValue*)TC_heapAlloc(updateStmt->heap, sizeof(SQLValue));

		// Sets the values of the parameter in its list.
      updateStmt->paramDefined[index] = true;

      // The value is null.
      record->asChars = null;
      record->asBlob = null;
      record->length = 0;
      record->isNull = updateStmt->storeNulls[i] = true;

      return true;
   }
   else // The parameter is in the where clause.
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_PARAM_NULL));
      return false;
   }
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
		j = paramIndexes[i];
      paramDefined[j] = storeNulls[j] = false;
      xmemzero(record[j], sizeof(SQLValue));
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
 * return The number of rows that were updated, or -1 if an error occurs.
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
		heapDestroy(heap);
		TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
		return -1;
	}

   if (!table)
	{
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_CANT_READ), updateStmt->rsTable->tableName);
		heapDestroy(heap);
      return -1;
	}

   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   if (!table->isModified)
   {
      PlainDB* plainDB = table->db;
      XFile* dbFile = &plainDB->db;
      
      nn = (plainDB->isAscii? IS_ASCII : 0);
	   nfSetPos(dbFile, 6);
	   if (nfWriteBytes(context, dbFile, (uint8*)&nn, 1) && flushCache(context, dbFile)) // Flushs .db.
         table->isModified = true;
	   else
      {
         heapDestroy(heap);
         return false;
      }
   }

	// Verifies if there are any parameters missing and the nulls do not violate a null restriction.
   // Creates the result set that will be used to update the rows.
   if (!verifyNullValues(context, table, record, CMD_UPDATE, updateStmt->nValues)
	 || !sqlBooleanClausePreVerify(context, updateStmt->whereClause)
    || !(rs = createSimpleResultSet(context, table, updateStmt->whereClause, heap)))
	{
		heapDestroy(heap);
		return -1;
	}
   
   nn = 0;
   
   // juliana@223_14: solved possible memory problems.
   while (true)
   {
      IF_HEAP_ERROR(heap)
	   {
		   heapDestroy(heap);
		   TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
		   return -1;
	   }
      if (!getNextRecord(context, rs, heap))
         break;
      if (writeRecord(context, table, record, rs->pos, heap))
         nn++;
      else
      {
         heapDestroy(heap);
         return -1;
      }
   }
   heapDestroy(heap);
   return nn;
}

/**
 * Binds an update statement.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 * @throws <code>SQLParseException</code> if the number of fields is greater than 128. 
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
   uint8 *storeNulls = updateStmt->storeNulls;

   // juliana@227_17: corrected a possible crash if one tries to update more than 128 fields in a table.
   if (valuesCount > MAXIMUMS)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_MAX_NUM_FIELDS_REACHED), 0);
      return false;
   }

   while (++i < valuesCount) // Checks if there are undefined values.
	   // Identifies the values that are placeholders for parameters.
      if (record[i] && record[i]->asChars && record[i]->asChars[0] == (JChar)'?' && !record[i]->asChars[1]) 
         paramIndexes[paramCount++] = i;

   updateStmt->paramCount = paramCount;

   // Makes sure the fields are in correct order, aligned with the table order.
   if (!reorder(context, table, fields, record, storeNulls, &updateStmt->nValues, paramIndexes, false))
		return false;
	
   updateStmt->record = record;
	xmemset(storeNulls, false, table->columnCount);
   xmemmove(storeNulls, table->storeNulls, table->columnCount);

   // Converts the values to be updated into its correct type and binds the where clause to its table. 
   if (!convertStringsToValues(context, table, record, updateStmt->nValues) 
    || (updateStmt->whereClause
     && !bindColumnsSQLBooleanClause(context, updateStmt->whereClause, &table->htName2index, table->columnTypes, &updateStmt->rsTable, 1, updateStmt->heap)))
		return false;

   return true;
}
