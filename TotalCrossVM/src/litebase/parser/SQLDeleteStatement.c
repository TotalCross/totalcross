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
			whereClause->fieldList = (SQLResultSetField**)TC_heapAlloc(heap, whereClause->fieldsCount * TSIZE);
			xmemmove(whereClause->fieldList, parser->whereFieldList, whereClause->fieldsCount * TSIZE);
			whereClause->paramList = (SQLBooleanClauseTree**)TC_heapAlloc(heap, whereClause->paramCount * TSIZE);
			xmemmove(whereClause->paramList, parser->whereParamList, whereClause->paramCount * TSIZE);
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
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @thows IllegalStateException If the parameter index is invalid.
 */
bool setNumericParamValueDel(Context context, SQLDeleteStatement* deleteStmt, int32 index, VoidP value, int32 type)
{
	TRACE("setNumericParamValueDel")

	// Checks if the index is within the range.
	SQLBooleanClause* whereClause = deleteStmt->whereClause;
   if (index < 0 || !whereClause || index >= whereClause->paramCount)
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
      return false;
   }
   else
      return setNumericParamValue(context, whereClause->paramList[index], value, type);
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
 * @thows IllegalStateException If the parameter index is invalid.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setParamValueStringDel(Context context, SQLDeleteStatement* deleteStmt, int32 index, JCharP value, int32 length)
{
	TRACE("setParamValueStringDel")
   
	// Checks if the index is within the range.
	SQLBooleanClause* whereClause = deleteStmt->whereClause;
   if (index < 0 || !whereClause || index >= whereClause->paramCount)
   {
      TC_throwExceptionNamed(context, "java.lang.IllegalStateException", getMessage(ERR_INVALID_PARAMETER_INDEX), index);
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
	int32 nn=0,
		   i,
			id,
			columnCount,
			numberComposedIndexes;
	bool hasIndexes;
	Heap heap = null;

   if (!table)
	{
		TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_CANT_READ), deleteStmt->rsTable->tableName);
      return -1;
	}

   dbFile = &(plainDB = &table->db)->db;
	basbuf = plainDB->basbuf;

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

   // juliana@250_10: removed some cases when a table was marked as not closed properly without being changed.
   // juliana@226_4: now a table won't be marked as not closed properly if the application stops suddenly and the table was not modified since its 
   // last opening. 
   if (!setModified(context, table))
      return -1;

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
		Key tempKey;
		SQLValue** keys;
		SQLValue tempKeys[MAXIMUMS + 1]; 
		uint16* columnOffsets = table->columnOffsets;
      uint8* nulls = table->columnNulls;
		int32* columnSizes = table->columnSizes;
		int8* columnTypes = table->columnTypes;
      int8* colIdxTypes;
      uint8* columns;
      int32 maxSize = 0,
            maxSize0 = 0;
		
		heap = heapCreate();
		IF_HEAP_ERROR(heap)
		{
			TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
			goto error;
		}
		
		// juliana@223_14: solved possible memory problems.
		// guich@300: now all records are just marked as deleted instead of physical removal.
		if (!(rs = createSimpleResultSet(context, table, whereClause, heap)))
         return -1;

		rs->pos = -1;
		nn = 0;

		if (hasIndexes) // Removes the value of the index from each column of this record.
		{
		   // Calculates the maximum number of columns that a composed index has. 
		   i = numberComposedIndexes;
		   maxSize = 1;
		   while (--i >= 0)
		      maxSize = MAX(maxSize, composedIndexes[i]->numberColumns);
		   keys = newSQLValues(maxSize, heap);
   	   
		   // juliana@202_3: Solved a bug that could cause a GPF when using composed indices.
		   tempKey.keys = tempKeys;
   		 
			// Allocates all the necessary structure for updating the indices at once.
         i = columnCount;
			while (--i >= 0) 
			{
				if ((index = columnIndexes[i])) // The max size of a char column of a simple index.
			      maxSize0 = MAX(maxSize0, columnSizes[i]);
			   
			   // Calculates the maximum char column size for each column index of all the composed indices. 
			   id = numberComposedIndexes;
			   maxSize = 0;
			   while (--id >= 0)
			      if (i < (compIndex = composedIndexes[id])->numberColumns)
			         maxSize = MAX(maxSize, compIndex->index->colSizes[i]);   
			   
			   // Allocates the char buffers if necessary. The simple index will be allocated in the first record.
			   if (i > 0)
			   {
			      if (maxSize > 0)
			         keys[i]->asChars = (JCharP)TC_heapAlloc(heap, (maxSize << 1) + 2);
			   }
			   else
			   {
			      // Gets the greatest size between the first column of all the composed indices and the max size of all the simple indices.
			      if ((maxSize = MAX(maxSize, maxSize0)) > 0)
			         keys[0]->asChars = (JCharP)TC_heapAlloc(heap, (maxSize << 1) + 2);
			   }      			   
			}

			while (getNextRecord(context, rs, heap))
			{
				i = columnCount;
				
            // juliana@227_11: corrected a bug of an exception being thrown when trying to delete a row with a null in column which has an index.
            while (--i >= 0) // Simple indexes.
					if ((index = columnIndexes[i]) && isBitUnSet(nulls, i))
					{
						if (!readValue(context, plainDB, keys[0], columnOffsets[i], columnTypes[i], basbuf, false, false, false, -1, null))
						   goto error;
						keySet(&tempKey, &keys[0], index, 1);
						if (!indexRemoveValue(context, &tempKey, rs->pos))
						   goto error;
					}

				if ((i = numberComposedIndexes)) // Composed index.
					while (--i >= 0)
					{
						compIndex = composedIndexes[i];
						index = compIndex->index;
						id = compIndex->numberColumns;
                  colIdxTypes = index->types;
                  columns = compIndex->columns;
						while (--id >= 0)
							if (!readValue(context, plainDB, keys[id], columnOffsets[columns[id]], colIdxTypes[id], basbuf, false, false, false, -1, null))
							   goto error;
						keySet(&tempKey, keys, index, index->numberColumns);
						if (!indexRemoveValue(context, &tempKey, rs->pos))
						   goto error;
	               
					}
			
				// Logically deletes the record: changes the attribute to 'deleted'.
				xmove4(&id, basbuf);
            id = (id & ROW_ID_MASK) | ROW_ATTR_DELETED;
				xmove4(basbuf, &id);
				if (!plainRewrite(context, plainDB, rs->pos))
				   goto error;
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
				   goto error;
				nn++; // Increments the number of deleted rows.
			}
      table->deletedRowsCount += nn;
		heapDestroy(heap);
	}

// juliana@270_32: corrected a bug of a delete not updating the number of total deleted rows in the metadata when there is an index corruption.
error:
   if (nn > 0 && !tableSaveMetaData(context, table, TSMD_ONLY_DELETEDROWSCOUNT))
      return -1;
   
   // juliana@227_3: improved table files flush dealing.
   // juliana@270_25: corrected a possible lose of records in recover table when 10 is passed to LitebaseConnection.setRowInc().
   if (!dbFile->dontFlush) // juliana@202_23: flushs the files to disk when row increment is the default.
	{
      if (dbFile->cacheIsDirty && !flushCache(context, dbFile)) // Flushs .db.
         return -1;
		if (plainDB->dbo.cacheIsDirty && !flushCache(context, &plainDB->dbo)) // Flushs .dbo.
		   return -1;
	}
	return nn;
	
   heapDestroy(heap);
   return -1;
}

/**
 * Binds a <code>SQL DELETE</code> expression.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param deleteStmt A SQL delete statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 */
bool litebaseBindDeleteStatement(Context context, TCObject driver, SQLDeleteStatement* deleteStmt)
{
	TRACE("litebaseBindDeleteStatement")
   Table *table = deleteStmt->rsTable->table = getTable(context, driver, deleteStmt->rsTable->tableName);

	// Binds the delete statement to its table.
   if (!table || (deleteStmt->whereClause && !bindColumnsSQLBooleanClauseSimple(context, deleteStmt->whereClause, deleteStmt->rsTable, 
		                                                                                                            deleteStmt->heap)))
      return false;

   return true;
}
