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



/**
 * Defines the functions to initialize, set, and process a delete statement.
 */

#include "SQLDeleteStatement.h"

/**
 * Initializes a SQL detete statement for a given SQL. 
 *
 * @param parser The parse structure with parse information concerning the SQL.
 * @param isPrepared Indicates if the delete statement is from a prepared statement.
 * @return A pointer to a <code>SQLDeleteStatement</code> structure. 
 */
SQLDeleteStatement* initSQLDeleteStatement(LitebaseParser* parser, bool isPrepared)
{
	TRACE("initSQLDeleteStatement")
   SQLDeleteStatement* deleteStmt = (SQLDeleteStatement*)TC_heapAlloc(parser->heap, sizeof(SQLDeleteStatement));
   SQLBooleanClause* whereClause = deleteStmt->whereClause = parser->whereClause;
	Heap heap = deleteStmt->heap = parser->heap;
	deleteStmt->type = CMD_DELETE;
	
   if (isPrepared) // It is only necessary to re-allocate the parser structures if the statement is from a prepared statement.
	{
		deleteStmt->rsTable = initSQLResultSetTable((*parser->tableList)->tableName, (*parser->tableList)->aliasTableName, heap);
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
		deleteStmt->rsTable = *parser->tableList;
		if (whereClause)
		{
			whereClause->fieldList = parser->whereFieldList;
			whereClause->paramList = parser->whereParamList;
		}
	}

	return deleteStmt;
}

/* 
 * Sets the value of a numeric parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param deleteStmt A SQL delete statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param type The type of the parameter.
 * @thows DriverException If the parameter index is invalid.
 */
void setNumericParamValueDel(Context context, SQLDeleteStatement* deleteStmt, int32 index, VoidP value, int32 type)
{
	TRACE("setNumericParamValueDel")

	// Checks if the index is within the range.
	SQLBooleanClause* whereClause = deleteStmt->whereClause;
   if (index < 0 || !whereClause || index >= whereClause->paramCount)
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
   else
      setNumericParamValue(context, whereClause->paramList[index], value, type);
}

/* 
 * Sets the value of a string parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param deleteStmt A SQL delete statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param length The length of the string.
 * @throws SQLParserException If a <code>null</code> is used as a parameter of a where clause.
 * @thows DriverException If the parameter index is invalid.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setParamValueStringDel(Context context, SQLDeleteStatement* deleteStmt, int32 index, JCharP value, int32 length)
{
	TRACE("setParamValueStringDel")
   
	// Checks if the index is within the range.
	SQLBooleanClause* whereClause = deleteStmt->whereClause;
   if (index < 0 || !whereClause || index >= whereClause->paramCount)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
      return false;
   }
   else if (!value)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_PARAM_NULL)); 
      return false;
   }
   else
      return setParamValueString(context, whereClause->paramList[index], value, length);
}

/**
 * Clears all parameter values of a prepared statement delete.
 *
 * @param deleteStmt A SQL delete statement.
 */
void clearParamValuesDel(SQLDeleteStatement* deleteStmt)
{
	TRACE("clearParamValuesDel")
   if (deleteStmt->whereClause)
	{
		int32 i = deleteStmt->whereClause->paramCount;
		SQLBooleanClauseTree** paramList = deleteStmt->whereClause->paramList;
      while (--i >= 0)
         paramList[i]->isParamValueDefined = false;
	}
}

/**
 * Checks if all parameters values are defined.
 *
 * @param deleteStmt A SQL delete statement.
 * @return <code>true</code>, if all parameters values are defined; <code>false</code> otherwise.
 */
bool allParamValuesDefinedDel(SQLDeleteStatement* deleteStmt)
{
	TRACE("allParamValuesDefinedDel")
   if (deleteStmt->whereClause)
	{
		int32 i = deleteStmt->whereClause->paramCount;
		SQLBooleanClauseTree** paramList = deleteStmt->whereClause->paramList;
      while (--i >= 0)
         if (!paramList[i]->isParamValueDefined)
            return false;
	}
   return true;
}

/**
 * Executes a delete statement.
 *
 * @param context The thread context where the function is being executed.
 * @param deleteStmt A SQL delete statement.
 * @return The number of rows deleted.
 * @throws DriverException If the record can't be removed from the indices.
 * @throws OutOfMemoryError If a heap memory allocation fails.
 */
int32 litebaseDoDelete(Context context, SQLDeleteStatement* deleteStmt)
{
	TRACE("litebaseDoDelete")
   Table* table = deleteStmt->rsTable->table;
	SQLBooleanClause* whereClause = deleteStmt->whereClause;
	Index** columnIndexes;
	ComposedIndex* compIndex;
	ComposedIndex** composedIndexes;
	PlainDB* plainDB;
   XFile* dbFile;
	uint8* basbuf;
	Index* index;
	int32 nn,
		   i,
			id,
			columnCount,
			column,
			numberComposedIndexes;
	bool hasIndexes;

   if (!table)
	{
		TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_CANT_READ), deleteStmt->rsTable->tableName);
      return -1;
	}

   dbFile = &(plainDB = table->db)->db;
	basbuf = plainDB->basbuf;

   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   if (!table->isModified)
   {
      XFile* dbFile = &plainDB->db;
      
      i = (plainDB->isAscii? IS_ASCII : 0);
	   nfSetPos(dbFile, 6);
	   if (nfWriteBytes(context, dbFile, (uint8*)&i, 1) && flushCache(context, dbFile)) // Flushs .db.
         table->isModified = true;
	   else
         return -1;
   }

	// If there are indices, this is needed to remove the values from them.
	numberComposedIndexes = table->numberComposedIndexes;
	columnIndexes = table->columnIndexes;
	hasIndexes = (composedIndexes = table->composedIndexes) != null; // juliana@201_6
	i = columnCount = table->columnCount;
	while (--i >= 0)
		if (columnIndexes[i])
		{
			hasIndexes = true;
			break;
		}

   if (!whereClause) // Deletes the whole table.
   {
		if (hasIndexes) // If the whole table is being deleted, just empties all indexes.
		{
			i = columnCount;
			while (--i >= 0)
				if (columnIndexes[i] && !indexDeleteAllRows(context, columnIndexes[i]))
			      return -1;

			if ((i = numberComposedIndexes)) // juliana@201_6: It now deletes the erases the composed index when deleting the whole table. 
				while (--i >= 0)
					if (!indexDeleteAllRows(context, composedIndexes[i]->index))
					   return -1;
		}

      // juliana@227_10: Corrected a bug of a delete with no where clause not taking the already deleted rows into consideration when returning
      // the number of deleted rows.
		nn = plainDB->rowCount - table->deletedRowsCount;
      i = table->deletedRowsCount = plainDB->rowCount;
		
      while (--i >= 0)
		{
			// Logically deletes the record: changes the attribute to 'deleted'.
			if (!plainRead(context, plainDB, i))
				return -1;
			xmove4(&id, basbuf);
         id = (id & ROW_ID_MASK) | ROW_ATTR_DELETED;
			xmove4(basbuf, &id);
			if (!plainRewrite(context, plainDB, i))
				return -1;
		}
   }
	else
	{
		ResultSet* rs;
		Val tempVal;
		Key tempKey;
		SQLValue* vs;
		SQLValue** ki;
      SQLValue* keyOne[1];
		SQLValue ***keys;
		uint16* columnOffsets = table->columnOffsets;
      uint8* nulls = table->columnNulls[0];
		int32* columnSizes = table->columnSizes;
		int32* columnTypes = table->columnTypes;
      int32* colIdxSizes;
      int32* colIdxTypes;
      uint8* columns;
		Heap heap = heapCreate();

		IF_HEAP_ERROR(heap)
		{
			heapDestroy(heap);
			TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
			return -1;
		}
		
		// juliana@223_14: solved possible memory problems.
		// guich@300: now all records are just marked as deleted instead of physical removal.
		if (!(rs = createSimpleResultSet(context, table, whereClause, heap)))
         return -1;

		vs = (SQLValue*)TC_heapAlloc(heap, columnCount * sizeof(SQLValue));
		keys = (SQLValue***)TC_heapAlloc(heap, numberComposedIndexes * PTRSIZE);
	   
		// juliana@202_3: Solved a bug that could cause a GPF when using composed indices.
		tempKey.keys = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue) * columnCount);

		rs->pos = -1;
		nn = 0;

		if (hasIndexes) // Removes the value of the index from each column of this record.
		{
			// Allocates all the necessary structure for updating the indices at once.
         i = columnCount;
			while (--i >= 0) // Simple indexes.
				if ((index = columnIndexes[i]) && (columnTypes[i] == CHARS_TYPE || columnTypes[i] == CHARS_NOCASE_TYPE))
			      vs[i].asChars = (JCharP)TC_heapAlloc(heap, (columnSizes[i] << 1) + 2);

			i = numberComposedIndexes;
			while (--i >= 0)
			{
			   ki = keys[i] = (SQLValue**)TC_heapAlloc(heap, (compIndex = composedIndexes[i])->numberColumns * PTRSIZE);
				id = compIndex->numberColumns;
				while (--id >= 0)
				{
					column = compIndex->columns[id];
					ki[id] = (SQLValue*)TC_heapAlloc(rs->heap, sizeof(SQLValue));
					if ((columnTypes[column] == CHARS_TYPE || columnTypes[column] == CHARS_NOCASE_TYPE))
						ki[id]->asChars = (JCharP)TC_heapAlloc(heap, (columnSizes[column] << 1) + 2);
				}
			}

			while (getNextRecord(context, rs, heap))
			{
				i = columnCount;
				
            // juliana@227_11: corrected a bug of an exception being thrown when trying to delete a row with a null in column which has an index.
            while (--i >= 0) // Simple indexes.
					if ((index = columnIndexes[i]) && isBitUnSet(nulls, i))
					{
						if (!readValue(context, plainDB, &vs[i], columnOffsets[i], columnTypes[i], basbuf, false, false, false, null))
						{
							heapDestroy(heap);
							return -1;
						}
                  *keyOne = &vs[i];
						keySet(&tempKey, keyOne, index, 1);
						valueSet(tempVal, rs->pos);
						if (!indexRemoveValue(context, &tempKey, &tempVal))
						{
							heapDestroy(heap);
							return -1;
						}
					}

				if ((i = numberComposedIndexes)) // Composed index.
					while (--i >= 0)
					{
						ki = keys[i];
						compIndex = composedIndexes[i];
						index = compIndex->index;
						id = compIndex->numberColumns;
                  colIdxSizes = index->colSizes;
                  colIdxTypes = index->types;
                  columns = compIndex->columns;
						while (--id >= 0)
						{
							if (!readValue(context, plainDB, ki[id], columnOffsets[columns[id]], colIdxTypes[id], basbuf, false, false, false, null))
							{
               			heapDestroy(heap);
								return -1;
							}
						}
						keySet(&tempKey, ki, index, index->numberColumns);
						valueSet(tempVal, rs->pos);
						if (!indexRemoveValue(context, &tempKey, &tempVal))
						{
							heapDestroy(heap);
							return -1;
						}
	               
					}
			
				// Logically deletes the record: changes the attribute to 'deleted'.
				xmove4(&id, basbuf);
            id = (id & ROW_ID_MASK) | ROW_ATTR_DELETED;
				xmove4(basbuf, &id);
				if (!plainRewrite(context, plainDB, rs->pos))
				{
               heapDestroy(heap);
					return -1;
				}
				nn++; // Increments the number of deleted rows.
			}
		}
		else
         while (getNextRecord(context, rs, heap))
			{
            // Logically deletes the record: changes the attribute to 'deleted'.
				xmove4(&id, basbuf);
				id = (id & ROW_ID_MASK) | ROW_ATTR_DELETED;
            xmove4(basbuf, &id);
				if (!plainRewrite(context, plainDB, rs->pos))
				{
               heapDestroy(heap);
					return -1;
				} 
				nn++; // Increments the number of deleted rows.
			}
      table->deletedRowsCount += nn;
		heapDestroy(heap);
	}
   if (nn > 0 && !tableSaveMetaData(context, table, TSMD_ONLY_DELETEDROWSCOUNT))
      return -1;
   
   // juliana@227_3: improved table files flush dealing.
	if (plainDB->rowInc == DEFAULT_ROW_INC) // juliana@202_23: flushs the files to disk when row increment is the default.
	{
      if (dbFile->cacheIsDirty && !flushCache(context, dbFile)) // Flushs .db.
         return -1;
		if (plainDB->dbo.cacheIsDirty && !flushCache(context, &plainDB->dbo)) // Flushs .dbo.
		   return -1;
	}
	return nn;
}

/**
 * Binds a <code>SQL DELETE</code> expression.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param deleteStmt A SQL delete statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 */
bool litebaseBindDeleteStatement(Context context, Object driver, SQLDeleteStatement* deleteStmt)
{
	TRACE("litebaseBindDeleteStatement")
   Table *table = deleteStmt->rsTable->table = getTable(context, driver, deleteStmt->rsTable->tableName);

	// Binds the delete statement to its table.
   if (!table || (deleteStmt->whereClause && !bindColumnsSQLBooleanClauseSimple(context, deleteStmt->whereClause, deleteStmt->rsTable, 
		                                                                                                            deleteStmt->heap)))
      return false;

   return true;
}
