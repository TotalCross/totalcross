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

// juliana@253_5: removed .idr files from all indices and changed its format. 
/**
 * Declares functions to manipulate table structures.
 */

#include "Table.h"

/**
 * Verifies if the index already exists.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose index is to be created.
 * @param columnNumbers The columns that are part of this index.
 * @param indexCount The number of columns of the index.
 * @return 0 for simple indices. For composed index, if there was this same index, it returns the negative number of the this old one; otherwise, 
 * it returns the new number.
 * @throws AlreadyCreatedException If an index already exists.
 */
int32 verifyIfIndexAlreadyExists(Context context, Table* table, uint8* columnNumbers, int32 indexCount)
{
	TRACE("verifyIfIndexAlreadyExists")
   int32 idx = -1, 
         i;
   if (indexCount == 1) // Simple index.
   {
      if (table->columnIndexes[idx = *columnNumbers])
      {
			TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_INDEX_ALREADY_CREATED), table->columnNames[idx]);
         return -1;
      }
      return 0;
   }
   else // Composed index.
   {
      ComposedIndex* currCompIndex;
      uint8* columns;
      int32 size = i = table->numberComposedIndexes, 
            j;

      if (!size)    // First index number.
         return 1; 

      while (--i >= 0)
      {
         currCompIndex = table->composedIndexes[i];
         columns = currCompIndex->columns;
         j = currCompIndex->numberColumns;

         // juliana@253_2: corrected a bug if a composed index with less columns were created after one with more columns.
         if (j == indexCount)
         {
            while (--j >= 0 && columnNumbers[j] == columns[j]);
         
            if (j < 0)
            {
               // Builds the exception message.
				   char errorMsg[1024];
               CharP* columnNames = table->columnNames;
   		      
               xstrcpy(errorMsg, columnNames[columnNumbers[j = 0]]);
               while (++j < indexCount)
				   {
					   xstrcat(errorMsg, ", ");
					   xstrcat(errorMsg, columnNames[columnNumbers[j]]);
				   }
               TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_INDEX_ALREADY_CREATED), errorMsg);
               return -1;
            }
         }
      }
      return size + 1;
   }
}

/**
 * Drops an index.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose index is to dropped.
 * @param column The column of the index dropped.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the column does not have an index. 
 */
bool driverDropIndex(Context context, Table* table, int32 column)
{
   TRACE("driverDropIndex") // Column does not have an index.
   Index* index = table->columnIndexes[column];

   if (!index)
   {
		TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_DOESNOT_HAVE_AN_INDEX), table->columnNames[column]);
      return false;
   }

   if (!indexRemove(context, index)) // Deletes the index of this table.
      return false;
   table->columnIndexes[column] = null; // Already freed.
   table->columnAttrs[column] &= ATTR_COLUMN_HAS_NO_INDEX; // Deletes the INDEX bit from the attributes. 
   
   // Saves the meta.
   return tableSaveMetaData(context, table, TSMD_ATLEAST_INDEXES); // guich@560_24
}

/**
 * Drops a composed index.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose index is to dropped.
 * @param columns The columns of the composed index.
 * @param size The number of columns of the composed index.
 * @param indexId The id of the composed index or -1 if its position is not known.
 * @param saveMD Indicates if the meta data is to be saved.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the table does not have the desired composed index to be dropped.
 */
bool driverDropComposedIndex(Context context, Table* table, uint8* columns, int32 size, int32 indexId, bool saveMD)
{
	TRACE("driverDropComposedIndex")
   ComposedIndex* compIndex = null;
   ComposedIndex** compIndices = table->composedIndexes;
   uint8* idxColumns;
   int32 indexCount = size, 
         i = table->numberComposedIndexes, 
         j = 0;

   if (indexId < 0)
      while (--i >= 0)
      {
         if ((compIndex = compIndices[i])->numberColumns == indexCount)
         {
            j = indexCount;
            idxColumns = compIndex->columns;
            while (--j >= 0 && columns[j] == idxColumns[j]);
               
            if (j < 0) 
               break;
         }
      }
   else
      compIndex = compIndices[i = indexId];

   if (i >= 0 && compIndex) // Removes the index.
   {
      if (!indexRemove(context, compIndex->index))
         return false;
      compIndex->index = null;
		
		// juliana@223_14: solved possible memory problems.
		// juliana@201_16: When a composed index is deleted, its information is now deleted from the metadata.
		if (table->numberComposedIndexes)
         (table->composedIndexes[i] = table->composedIndexes[--table->numberComposedIndexes])->indexId = i + 1;
      else
         table->composedIndexes[table->numberComposedIndexes = 0] = null;
   }
   else // The given columns do not have a composed index.
   {
      // Builds the exception message.
		char errorMsg[1024];
      CharP* columnNames = table->columnNames;
      xstrcpy(errorMsg, columnNames[columns[0]]);
		while (++j < indexCount)
		{
			xstrcpy(errorMsg, ", ");
			xstrcpy(errorMsg, columnNames[columns[j]]);
		}
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_DOESNOT_HAVE_AN_INDEX), errorMsg);
      return false;
   }
   if (saveMD)
      return tableSaveMetaData(context, table, TSMD_EVERYTHING);
   return true;
}

// juliana@227_6: drop index * on table_name wold make the index reapear after closing the driver and reusing table_name.
/**
 * Deletes all indices of a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose indices are to dropped.
 * @return The number of indices deleted.
 */
int32 deleteAllIndexes(Context context, Table* table)
{
	TRACE("deleteAllIndexes")
   int32 count = 0,
         i = table->columnCount,
         primaryKey = table->primaryKeyCol;
   bool ret = true;
   Index** columnIndexes = table->columnIndexes;
   uint8* columnAttrs = table->columnAttrs;
   ComposedIndex** composedIndexes = table->composedIndexes;

   // Unique index.
   while (--i >= 0)
      if (i != primaryKey && columnIndexes[i])  
      {
         ret &= indexRemove(context, columnIndexes[i]); 
         columnIndexes[i] = null;
         columnAttrs[i] &= ATTR_COLUMN_HAS_NO_INDEX;
         count++;
      }

	// juliana@201_33: When all indices are dropped by the user, the composed primary key can't be deleted.
   i = table->numberComposedIndexes;
   primaryKey = table->composedPK;
   while (--i >= 0) 
		if (i != primaryKey)
		{
			ret &= driverDropComposedIndex(context, table, composedIndexes[i]->columns, composedIndexes[i]->numberColumns, i, false);
			count++;
		}

   // juliana@230_33: corrected a bug of composed indices files returning after deleting all indices.
   if (!tableSaveMetaData(context, table, TSMD_EVERYTHING)) // guich@560_24 
      return -1;
   return ret? count : -1;
}

/**
 * Computes the column offsets of the table columns.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated.
 */
bool computeColumnOffsets(Context context, Table* table) // rnovais@568_10: changed from static to global.
{
	TRACE("computeColumnOffsets")
   uint16* offsets = table->columnOffsets;
   int8* types = table->columnTypes;
   bool notRecomputing = !offsets;
   int32 sum = 0,
         n = table->columnCount,
         i = -1;
   uint8* buffer;  
   
   if (notRecomputing) // Does not create the array 2 times.
      offsets = table->columnOffsets = (uint16*)TC_heapAlloc(table->heap, (n + 1) << 1);

   while (++i < n)
   {
      offsets[i] = sum; // Total offset till now.
      sum += typeSizes[types[i]]; // Gets the size of this column.
   }
   offsets[i] = sum; // The offset for the last column.

   // the number of bytes necessary to store the columns. Each column in a table correspond to one bit.
   // Added a number of bytes corresponding to the null values and to the crc code.
   sum += NUMBEROFBYTES(n) + 4; // juliana@220_4
   
   plainSetRowSize(&table->db, sum, buffer = TC_heapAlloc(table->heap, sum)); // Sets the new row size.

   if (notRecomputing)
   {
      Hashtable* htName2index;
      int32* columnHashes = table->columnHashes;
      table->htName2index = TC_htNew(n + 1, null);
      htName2index = &table->htName2index; 
      if (!htName2index->items)
      {
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
         return false;
      }
      if (!*table->name)
      {
         while (--n >= 0)
            TC_htPut32(htName2index, columnHashes[n], n);
      } 
      else
      {
         while (--n >= 0)
         {
            // juliana@223_14: solved possible memory problems.
            if (TC_htGet32Inv(htName2index, columnHashes[n]) >= 0)
            {
               plainRemove(context, &table->db, table->sourcePath);
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DUPLICATED_COLUMN_NAME), (table->columnNames[n]));
               return false;
            }
            TC_htPut32(htName2index, columnHashes[n], n);
         }
      }
   }
   return true;
}

/**
 * Loads the meta data of a table,
 *
 * @param context The thread context where the function is being executed.
 * @param table The table being loaded.
 * @param throwException Indicates that a <code>TableNotClosedException</code> should be thrown.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the table is in an incompatible format.
 * @throws TableNotClosedException If the table was not properly close when opened last time.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated.
 */
bool tableLoadMetaData(Context context, Table* table, bool throwException) // juliana@220_5
{
   TRACE("tableLoadMetaData")
   TCHARP sourcePath = table->sourcePath;
   CharP tableName = table->name;
	int32 flags,
         columnCount = 0,
         i = -1,
         numOfBytes,
         version = 0,
         nameLength,
         indexNameLength,
         stringLength;
   bool exist;
   PlainDB* plainDB = &table->db;
   XFile* dbFile = &plainDB->db;
   TCHAR indexName[MAX_PATHNAME];
   uint8 buffer[512]; // A buffer for small metadata.
   IntBuf intBuf;
   uint8* metadata = plainReadMetaData(context, plainDB, buffer); // Reads the meta data.
	uint8* ptr = metadata;
   uint8* columnAttrs;
   int8* columnTypes;
   int32* columnSizes;
   CharP* columnNames;
   int32* columnSizesIdx;
   int32* columnHashes;
   int8* columnTypesIdx;
   SQLValue** defaultValues;
	Heap heap = table->heap,
        idxHeap = null;
   NATIVE_FILE idxFile;
   
   if (!metadata) // juliana@223_14: solved possible memory problems.
   {
      nfClose(context, dbFile);
      nfClose(context, &plainDB->dbo);
      return false;
   }
   
   // juliana@253_8: now Litebase supports weak cryptography.
   if (!(((ptr[0] & USE_CRYPTO) == 0) ^ plainDB->db.useCrypto) && ptr[1] == ptr[2] == ptr[3] == 0 && (ptr[0] == 0 || ptr[0] == 1 || ptr[0] == 3))
	{
      // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
		nfClose(context, dbFile);
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_WRONG_CRYPTO_FORMAT), 0);
		goto error;
	}

   if (ptr[0] == 1)
      plainDB->useOldCrypto = true;
   
   plainDB->dbo.finalPos = plainDB->dbo.size; // Gets the last position of the blobs and strings file.
   xmove2(&plainDB->headerSize, ptr + 4); // Reads the header size.
   ptr += 6;

   // juliana@226_8: a table without metadata (with an empty .db, for instance) can't be recovered: it is corrupted.
   if (!plainDB->headerSize) // The header size can't be zero.
   {
      // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
		// juliana@253_13: corrected a possible crash on Palm when trying to recover a table with corrupted header.
		nfClose(context, dbFile);
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), &table->name[5]);
      return false;
   }
   
   // If the header needs to be bigger, re-creates the metadata buffer with the correct size and skips the bytes already read.
   if (plainDB->headerSize != DEFAULT_HEADER)
   {
      if (!(metadata = plainReadMetaData(context, plainDB, null)))
         return false;
      ptr = metadata + 6;
   }

   // Checks if the table strings has the same format of the connection.
	if ((((flags = *ptr++) & IS_ASCII) != 0) != plainDB->isAscii)
	{
      // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
		nfClose(context, dbFile);
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_WRONG_STRING_FORMAT));
		goto error;
	}
   
	// juliana@220_2: added TableNotCreatedException which will be raised whenever a table is not closed properly.
   // If the table was not correctly closed, throws an specific exception to the user.
   if (!(flags &= IS_SAVED_CORRECTLY))
   {
      if (throwException) 
      {
		   // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
		   nfClose(context, dbFile);
		   TC_throwExceptionNamed(context, "litebase.TableNotClosedException", getMessage(ERR_TABLE_NOT_CLOSED), &table->name[5]);
		   goto error;
      }
      else
         plainDB->wasNotSavedCorrectly = true;
   }

   // The tables version must be the same as Litebase version.
	xmove2(&version, ptr);
	
	// juliana@230_12: improved recover table to take .dbo data into consideration.
	if (version < VERSION_TABLE - 1)
	{
		// juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
		nfClose(context, dbFile);
		TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_WRONG_VERSION), version);
      goto error;
	}
   table->version = version;
      
   // The currentRowId is found from the last non-empty record, not from the metadata.
   xmove4(&table->deletedRowsCount, ptr + 2); // Deleted rows count.
   xmove4(&table->auxRowId, ptr + 6); // rnovais@570_61: reads the auxiliary rowid.
   table->primaryKeyCol = *(ptr + 10); // juliana@114_9: the simple primary key column.
   table->composedPK = *(ptr + 12); // The composed primary key index. 
   
    // The column count can't be negative.
   xmove2(&columnCount, ptr + 14); 
	ptr += 16;
   if ((table->columnCount = columnCount) <= 0)
   {
      // juliana@222_1: the table should not be marked as closed properly if it was not previously closed correctly.
		// juliana@253_13: corrected a possible crash on Palm when trying to recover a table with corrupted header.
		nfClose(context, dbFile);
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_TABLE_CORRUPTED), &table->name[5]);
		goto error;
   }

   columnHashes = table->columnHashes = (int32*)TC_heapAlloc(heap, columnCount << 2);
   columnTypes = table->columnTypes = (int8*)TC_heapAlloc(heap, columnCount);
   columnSizes = table->columnSizes = (int32*)TC_heapAlloc(heap, columnCount << 2);
   table->columnIndexes = (Index**)TC_heapAlloc(heap, columnCount * TSIZE);
   columnAttrs = table->columnAttrs = (uint8*)TC_heapAlloc(heap, columnCount);
   defaultValues = table->defaultValues = (SQLValue**)TC_heapAlloc(heap, columnCount * TSIZE);
   table->storeNulls = (uint8*)TC_heapAlloc(heap, NUMBEROFBYTES(columnCount)); 

   xmemmove(columnAttrs, ptr, columnCount); // Reads the column attributes.

   i = -1;
   ptr += columnCount;
   while (++i < columnCount) // Reads the column types.
      columnTypes[i] = *ptr++;

   xmemmove(columnSizes, ptr, columnCount << 2); // Reads the column sizes.

   // Reads the column names.
   ptr = readStringArray(ptr += (columnCount << 2), &table->columnNames, columnCount, heap);
   columnNames = table->columnNames;

   i = -1;
   while (++i < columnCount) // Computes the hashes.
      columnHashes[i] = TC_hashCode(columnNames[i]);

   if (!computeColumnOffsets(context, table)) // Computes the column offsets.
      goto error;

	// juliana@201_21: The null columns information must be created before openning the indices when reading the table meta data.
   table->columnNulls = (uint8*)TC_heapAlloc(heap, numOfBytes = NUMBEROFBYTES(columnCount));

   // juliana@224_5: corrected a bug that would throw an exception when re-creating an erased index file.
   getFullFileName(tableName, sourcePath, indexName);
   indexName[nameLength = tcslen(indexName)] = '$';

   i = columnCount;
   while (--i >= 0) // Loads the indices.
   { 
      if ((columnAttrs[i] & ATTR_COLUMN_HAS_INDEX))
      {
         idxHeap = heapCreate();
         IF_HEAP_ERROR(idxHeap)
         {
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            heapDestroy(idxHeap);
            table->columnIndexes[i] = null; // juliana@270_22: solved a possible crash when the table is corrupted.
            goto error;
         }
         
         // rnovais@110_2: verifies if the index file exists, otherwise makes the re-index.
         columnSizesIdx = (int32*)TC_heapAlloc(idxHeap, 4);
         columnTypesIdx = (int8*)TC_heapAlloc(idxHeap, 1);
         TC_CharP2TCHARPBuf(TC_int2str(i, intBuf), &indexName[nameLength + 1]);
         tcscat(indexName, TEXT(IDK_EXT));
         indexNameLength = tcslen(indexName);

         // juliana@224_5: corrected a bug that would throw an exception when re-creating an erased index file.
         // juliana@202_9: Corrected a bug that would cause indices that have an .idr whose files were erased to be built incorrectly. 
         if ((exist = lbfileExists(indexName)) && !flags)
         {     
            if ((exist = lbfileCreate(&idxFile, indexName, READ_WRITE))
             || (exist = lbfileSetSize(&idxFile, 0)) || (exist = lbfileClose(&idxFile)))
            {
               char buffer[1024];
               TC_TCHARP2CharPBuf(indexName, buffer);
               fileError(context, exist, buffer);
               heapDestroy(idxHeap);
               table->columnIndexes[i] = null; // juliana@270_22: solved a possible crash when the table is corrupted.
               goto error;
            }
            exist = false;
         }

         *columnSizesIdx = columnSizes[i];
         *columnTypesIdx = columnTypes[i];
         
         // juliana@230_8: corrected a possible index corruption if its files are deleted and the application crashes after recreating it.
         if (!indexCreateIndex(context, table, tableName, i, columnSizesIdx, columnTypesIdx, exist, idxHeap)
          || (!exist && flags && (!tableReIndex(context, table, i, false, null) || !setModified(context, table))))
         {
            heapDestroy(idxHeap);
            table->columnIndexes[i] = null; // juliana@270_22: solved a possible crash when the table is corrupted.
            goto error;
         }
      }
   }

	// juliana@213_6: current rowid was not being corrected fetched from the table.
   // Now the current rowid can be fetched.
	if (plainDB->rowCount)
	{		
		if (plainRead(context, plainDB, plainDB->rowCount - 1))
			if (table->auxRowId != ATTR_DEFAULT_AUX_ROWID) //rnovais@570_61
				table->currentRowId = table->auxRowId; 
			else
			{
				xmove4(&table->currentRowId, plainDB->basbuf);
				table->currentRowId = (table->currentRowId & ROW_ID_MASK) + 1;
			}
		else
			goto error;
	}

   i = -1;
   while (++i < columnCount) // Reads the default values.
   {
      if ((columnAttrs[i] & ATTR_COLUMN_HAS_DEFAULT)) // Tests if it has default values.
      {
         defaultValues[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
         switch (columnTypes[i])
         {
            case CHARS_TYPE:
            case CHARS_NOCASE_TYPE:
               stringLength = 0;
               xmove2(&stringLength, ptr);
					
					// juliana@252_5: corrected a bug when using a default value of type string which could become messed up.
					defaultValues[i]->asChars = (JCharP)TC_heapAlloc(heap, stringLength << 1);
               xmemmove(defaultValues[i]->asChars, (JCharP)(ptr + 2), stringLength << 1);
               
               // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values.
               ptr += (((defaultValues[i]->length = stringLength) << 1) + 2); 
               
               break;

            case SHORT_TYPE:
               xmove2(&defaultValues[i]->asShort, ptr);
					ptr += 2; // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values.
               break;

            case DATE_TYPE: // Stored as int.
            case INT_TYPE:
               xmove4(&defaultValues[i]->asInt, ptr);
					ptr += 4; // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values.
               break;

            case LONG_TYPE:
					xmove8(&defaultValues[i]->asLong, ptr);
               ptr += 8; // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values.
               break;

            case FLOAT_TYPE:
               xmove4(&defaultValues[i]->asFloat, ptr); 
			   	ptr += 4; // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values. 
               break;

            case DOUBLE_TYPE:
               READ_DOUBLE((uint8*)&defaultValues[i]->asDouble, ptr);
					ptr += 8; // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values.
               break;

            case DATETIME_TYPE:
               xmove4(&defaultValues[i]->asDate, ptr); // date
               xmove4(&defaultValues[i]->asTime, ptr + 4); // time
					ptr += 8; // juliana@202_11: Corrected a bug that would create a composed index when opening a table using default values.
         }
      }
   }

   // Reads the composed indices.
   if ((table->numberComposedIndexes = *ptr++) > 0) // Reads the composed indices.
   {
      int32 j,
            indexId,
            numColumns,
            size,
            numberComposedIndexes = table->numberComposedIndexes;
      uint8* columns;
      
      indexName[nameLength] = '&';
      i = -1;
      while (++i < numberComposedIndexes)
      {
         idxHeap = heapCreate();
         IF_HEAP_ERROR(idxHeap)
         {
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            heapDestroy(idxHeap);
            table->composedIndexes[indexId] = null; // juliana@270_22: solved a possible crash when the table is corrupted.
            goto error;
         }
         
         indexId = (int8)*ptr++; // The composed index id.
         numColumns = *ptr++; // Number of columns on the composed index.
         size = numColumns << 2;
         ptr++;
         columns = (uint8*)TC_heapAlloc(idxHeap, numColumns);
         columnSizesIdx = (int32*)TC_heapAlloc(idxHeap, size);
         columnTypesIdx = (int8*)TC_heapAlloc(idxHeap, numColumns);
			
         j = -1;
         while (++j < numColumns)
         {
            columns[j] = *ptr++; // Columns of this composed index.
            columnSizesIdx[j] = columnSizes[columns[j]];
            columnTypesIdx[j] = columnTypes[columns[j]];
         }
            
         TC_CharP2TCHARPBuf(TC_int2str(i + 1, intBuf), &indexName[nameLength + 1]);
         tcscat(indexName, TEXT(IDK_EXT));
         indexNameLength = tcslen(indexName);
            
         // juliana@224_5: corrected a bug that would throw an exception when re-creating an erased index file.
         // juliana@202_9: Corrected a bug that would cause indices that have an .idr whose files were erased to be built incorrectly. 
         if ((exist = lbfileExists(indexName)) && !flags)
         {     
            if ((exist = lbfileCreate(&idxFile, indexName, READ_WRITE))
             || (exist = lbfileSetSize(&idxFile, 0))
             || (exist = lbfileClose(&idxFile)))
            {
               char buffer[1024];
               TC_TCHARP2CharPBuf(indexName, buffer);
               fileError(context, exist, buffer);
               heapDestroy(idxHeap);
               table->composedIndexes[indexId] = null; // juliana@270_22: solved a possible crash when the table is corrupted.
               goto error;
            }
            exist = false;
         }

         // juliana@230_8: corrected a possible index corruption if its files are deleted and the application crashes after recreating it.   
         // One of the files may not exist.
         if (!indexCreateComposedIndex(context, table, table->name, columns, columnSizesIdx, columnTypesIdx, numColumns, indexId, false, exist, 
                                                                                                                                         idxHeap) 
          || (!exist && flags && (!tableReIndex(context, table, -1, false, table->composedIndexes[indexId - 1]) || !setModified(context, table))))
         {
            heapDestroy(idxHeap);
            table->composedIndexes[indexId] = null; // juliana@270_22: solved a possible crash when the table is corrupted.
            goto error;
         }
      }
   }

   // Reads the composed primary key.
   if ((columnCount = table->numberComposedPKCols = *ptr++) > 0) // Number of the composed primary key.
      xmemmove(table->composedPrimaryKeyCols = (uint8*)TC_heapAlloc(heap, columnCount), ptr, columnCount);
   
   if (plainDB->headerSize != DEFAULT_HEADER)
	   xfree(metadata);
	return true;
	
error:
   if (plainDB->headerSize != DEFAULT_HEADER)
      xfree(metadata);
   return false;
}

/**
 * Saves the table meta data
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose meta data is being saved.
 * @param saveType The kind of save. It can be one out of <code><B>TSMD_ONLY_DELETEDROWSCOUNT</B></code>, 
 * <code><B>TSMD_ONLY_PRIMARYKEYCOL</B></code>, <code><B>TSMD_EVERYTHING</B></code>, or <code><B>TSMD_ONLY_AUXROWID</B></code>.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated.
 */
bool tableSaveMetaData(Context context, Table* table, int32 saveType)
{
   // Stores the changeable information.
	TRACE("tableSaveMetaData")
   uint8 buf[SECTOR_SIZE]; // Avoids allocating memory when saving small parts - guich@570_97: increased from 30 to 512.
   uint32 n = table->columnCount,
          i = -1,
          size;
   uint8* ptr0 = null;
   uint8* ptr;
   uint8* columnAttrs = table->columnAttrs;
   int8* columnTypes = table->columnTypes;
   int32* columnSizes = table->columnSizes;
   SQLValue** defaultValues = table->defaultValues; 
   ComposedIndex* compIndex;
   bool ret = true;
   PlainDB* plainDB = &table->db;

   // Calculates the meta data size;
   size = getTSMDSize(table, saveType);
   if (saveType == TSMD_EVERYTHING)
      size += getStringsTotalSize(table->columnNames, table->columnCount) + computeDefaultValuesMetadataSize(table) 
           + computeComposedIndicesTotalSize(table);
      
   // Tries to use a static buffer if possible.
   if (size <= SECTOR_SIZE)
   {
      ptr0 = ptr = buf;
      xmemzero(ptr, size);
   }
   else if (!(ptr0 = ptr = (uint8*)xmalloc(size))) // Must create a temporary buffer
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return false;
   }

   // The strings and blobs final position is deprecated.
   
   *ptr = (plainDB->db.useCrypto? (plainDB->useOldCrypto? 1 : USE_CRYPTO) : 0); // juliana@253_8: now Litebase supports weak cryptography.
   xmove2(ptr + 4, &plainDB->headerSize); // Saves the header size.
   ptr += 6;
	*ptr++ = plainDB->isAscii? IS_ASCII | !table->isModified : !table->isModified; // juliana@226_4: table is not saved correctly yet if modified.
   
   // The table format version.
   i = table->version; // juliana@230_12
	xmove2(ptr, &i);

   xmove4(ptr + 2, &table->deletedRowsCount); // Saves the deleted rows count.
   ptr += 6;

   if (saveType != TSMD_ONLY_DELETEDROWSCOUNT) // More things other than the deleted rows count must be saved.
   {
      xmove4(ptr, &table->auxRowId); // rnovais@570_61: saves the auxiliary rowid.
      ptr += 4;

      if (saveType != TSMD_ONLY_AUXROWID) // More things other than the auxiliary row id must be saved.
      {
         // juliana@230_5: Corrected a AIOBE when using a table created on Windows 32, Windows CE, Linux, Palm, Android, iPhone, or iPad using 
         // primary key on BlackBerry and Eclipse.
         *ptr++ = table->primaryKeyCol; // Saves the primary key col.
         *ptr++ = 0;
         *ptr++ = table->composedPK;  // juliana@114_9: saves the composed primary key index.
         *ptr++ = 0;

         if (saveType != TSMD_ONLY_PRIMARYKEYCOL) // More things other than the primary key col must be saved.
         {
            xmove2(ptr, &n); // Saves the number of columns.
            ptr += 2;
            i = -1;
            xmemmove(ptr, columnAttrs, n); // Saves the column attributes.
            ptr += n;

            if (saveType == TSMD_EVERYTHING) // Stores the rest.
            {
               i = -1;
               while (++i < n) // Stores the column types.
                  *ptr++ = (uint8)columnTypes[i];
               xmemmove(ptr, columnSizes, n << 2); // Stores the column sizes.
               ptr = writeStringArray(ptr += (n << 2), table->columnNames, table->columnCount); // Stores the column names.

               i = 0;
               while (++i < n) // Saves the default values.
               {
                  if ((columnAttrs[i] & ATTR_COLUMN_HAS_DEFAULT))
                     switch (columnTypes[i])
                     {
                        case CHARS_NOCASE_TYPE:
                        case CHARS_TYPE:
                           ptr = writeString16(ptr, defaultValues[i]->asChars, MIN((int32)defaultValues[i]->length, columnSizes[i]));      
                           break;

                        case SHORT_TYPE:
                           xmove2(ptr, &defaultValues[i]->asShort);
                           ptr += 2;
                           break;

                        case DATE_TYPE:
                        case INT_TYPE:
                           xmove4(ptr, &defaultValues[i]->asInt);
                           ptr += 4;
                           break;

                        case LONG_TYPE:
                           xmove8(ptr, &defaultValues[i]->asLong);
                           ptr += 8;
                           break;

                        case FLOAT_TYPE:
                           xmove4(ptr, &defaultValues[i]->asFloat);
                           break;

                        case DOUBLE_TYPE:
                           ptr = (uint8*)READ_DOUBLE(ptr, (uint8*)&defaultValues[i]->asDouble);
                           break;

                        case DATETIME_TYPE:
                           xmove4(ptr, &defaultValues[i]->asDate);
                           xmove4(ptr + 4, &defaultValues[i]->asTime);
                           ptr += 8;
                     }
               }
               
               n = *ptr++ = table->numberComposedIndexes; // Number of composed indices.
               i = -1;
               while (++i < n) // Stores the composed indices.
               {
                  *ptr++ = (compIndex = table->composedIndexes[i])->indexId; // The composed index id.
                  *ptr = compIndex->numberColumns; // Number of columns on the composed index.
						ptr += 2;
						
						// juliana@201_16  
                  xmemmove(ptr, compIndex->columns, compIndex->numberColumns); // Columns of this composed index.
                  ptr += compIndex->numberColumns;
               }
               n = *ptr++ = table->numberComposedPKCols; // Number of columns on composed primary key. If 0, there's no composed primary key.
               xmemmove(ptr, table->composedPrimaryKeyCols, n); // Stores the composed primary key.
               ptr += n;
            }
         }
      }
   }

   if (!plainWriteMetaData(context, plainDB, ptr0, (int32)(ptr - ptr0)))
      ret = false;
   else
      flushCache(context, &plainDB->db); // juliana@223_11: table meta data is now always flushed imediately after being changed.
   if (size > SECTOR_SIZE)
      xfree(ptr0);
   return ret;
}

/**
 * Sets the meta data for a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table being set.
 * @param names The table column names.
 * @param hashes The table column names hash codes.
 * @param types The table column types.
 * @param sizes The table column sizes.
 * @param attrs The table column attributtes.
 * @param composedPKCols The table primary key column.
 * @param defaultValues The default values of the table columns.
 * @param primaryKeyCol The table primary key column.
 * @param composedPK The composed primary key index in the composed indices.
 * @param columnCount The number of columns of the table.
 * @param ComposedPKColsSize The number of composed primary keys.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws AlreadyCreatedException if the table is already created.
 */
bool tableSetMetaData(Context context, Table* table, CharP* names, int32* hashes, int8* types, int32* sizes, uint8* attrs, uint8* composedPKCols, 
                      SQLValue** defaultValues, int32 primaryKeyCol, int32 composedPK, int32 columnCount, int32 composedPKColsSize)
{
	TRACE("tableSetMetaData")
   Heap heap = table->heap;
	int32 numOfBytes = NUMBEROFBYTES(columnCount);

   table->columnCount = (uint8)columnCount; // Sets the number of columns.
	table->columnHashes = hashes; // Sets the column hashes.
   table->columnTypes = types; // Sets the column types.
   table->columnSizes = sizes; // Sets the column sizes.
	table->storeNulls = TC_heapAlloc(heap, numOfBytes); // Initializes the arrays for the nulls.
   table->columnNames = names; // Sets the column names.

   // The number of bytes necessary to store the nulls. Each column in a table correspond to one bit.
   table->columnNulls = TC_heapAlloc(heap, numOfBytes);

	if (!computeColumnOffsets(context, table)) // guich@570_97: computes the column offests.
      return false;
   
	if (*table->name) // juliana@201_14: It is not necessary to save the meta data in the .db for memory tables. 
	{	
      if (table->db.db.size) // The table can't be already created.
      {
         TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_TABLE_ALREADY_CREATED), table->name);
		   return false;
      }

      // Saves the meta data after everything was set.
      table->version = VERSION_TABLE; // juliana@230_12
      table->columnAttrs = attrs; // Sets the column attributes.
	   table->defaultValues = defaultValues; // Sets the defaut values.
      table->primaryKeyCol = (uint8)primaryKeyCol; // Primary key column.
      table->composedPK = (uint8)composedPK; // Composed primary key index. 

      // Sets the composed primary key info.
	   table->numberComposedPKCols = composedPKColsSize;
	   table->composedPrimaryKeyCols = composedPKCols;

      table->columnIndexes = (Index**)TC_heapAlloc(heap, columnCount * TSIZE); // Initializes the indices.
      return tableSaveMetaData(context, table, TSMD_EVERYTHING); // Saves the metadata after everything was set.
   }
   return true;
}

/**
 * Gests the table standart metadata size to save a table.
 *
 * @param table The table to be saved.
 * @param saveType The save type of the table, which increases or decreases its size.
 * @return The metadata size.
 */
int32 getTSMDSize(Table* table, int32 saveType)
{
	TRACE("getTSMDSize")
   int32 columnCount = table->columnCount, 
         size = 13; // dbo.finalPos + headerSize + flags + version + deletedRowsCount.

   if (saveType != TSMD_ONLY_DELETEDROWSCOUNT)
   {
      size += 4; // auxRowId. // rnovais@570_61
      if (saveType != TSMD_ONLY_AUXROWID)
      {
         size += 4; // primaryKeyCol + composedPK.
         if (saveType != TSMD_ONLY_PRIMARYKEYCOL)
            size += 2 + columnCount; // columnCount + columnAttributes.
      }
   }
   if (saveType == TSMD_EVERYTHING)
      size += columnCount + (columnCount << 2) + 2; // columnTypes + columnSizes + columnNames.length.
   
   return size;
}

/**
 * Gets the total size of the table column names.
 *
 * @param names The names of the columns.
 * @param count The number of colums.
 * @return the total size for the column names.
 */
int32 getStringsTotalSize(CharP* names, int32 count)
{
	TRACE("getStringsTotalSize")
   int32 size = 0;

   while (--count >= 0)
      size += xstrlen(*names++) + 2; // Includes the two bytes used for the length of each name.
   return size;
}

/**
 * Gets the total size needed to store the table default values.
 *
 * @param table The table.
 * @return the total size for the default values.
 */
int32 computeDefaultValuesMetadataSize(Table* table)
{
	TRACE("computeDefaultValuesMetadataSize")
   uint32 i = table->columnCount,
         size = 0;
   uint8* columnAttrs = table->columnAttrs;
   int8* columnTypes = table->columnTypes;
   SQLValue** defaultValues = table->defaultValues;

   while (--i)
   {
      if ((columnAttrs[i] & ATTR_COLUMN_HAS_DEFAULT))
         switch (columnTypes[i])
         {
            case CHARS_NOCASE_TYPE:
            case CHARS_TYPE:
               size += (defaultValues[i]->length << 1) + 2; // The stringh + its length.
               break;
            case SHORT_TYPE:
               size += 2;
               break;
            case DATE_TYPE:
            case INT_TYPE:
            case FLOAT_TYPE:
               size += 4;
               break;
            case LONG_TYPE:
            case DOUBLE_TYPE:
            case DATETIME_TYPE:
               size += 8;
         }
   }
   return size;
}

/**
 * Gets the total size needed to store the table composed indices.
 *
 * @param table The table.
 * @return the total size for the composed indices.
 */
int32 computeComposedIndicesTotalSize(Table* table)
{
   TRACE("computeComposedIndicesTotalSize")
   int32 size = 2 + table->numberComposedPKCols, // The number o composed PK + its number of columns.
         i = table->numberComposedIndexes;
   ComposedIndex** composedIndexes = table->composedIndexes;

   while (--i >= 0)
      size += 3 + composedIndexes[i]->numberColumns; // id + numberColumns + hasIdr + the columns. 
   return size;
}

/**
 * Reorder the values of a statement to match the table definition.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table of the statement.
 * @param fields The fields of the statement.
 * @param record The record of values of thestatement.
 * @param storeNulls Indicates which values have a null.
 * @param nValues The number of values.
 * @param paramIndexes The indices of the parameters, if any, in the record.
 * @return <code>false</code> if there is an invalid field name; <code>true</code>, otherwise.
 * @throws DriverException if there is an invalid field name.
 */
bool reorder(Context context, Table* table, CharP* fields, SQLValue** record, uint8* storeNulls, uint8* nValues, uint8* paramIndexes)
{
	TRACE("reorder")
   int32 count = table->columnCount,
         length = *nValues,
         i = -1,
         numParams = 0;

   // juliana@225_5: corrected a possible crash when the table has 128 columns.
   SQLValue* outRecord[MAXIMUMS + 1]; // Just to store the temporary values, which will be copied over later.
   
   SQLValue* value;
   uint8* tableStoreNulls = table->storeNulls;
   Hashtable* htName2index = &table->htName2index;
   JCharP asChars;

   if (!*fields)
      *fields = "rowid"; // Inserts the rowid.

   // Cleans the <code>storeNulls</code>.
   xmemzero(tableStoreNulls, NUMBEROFBYTES(count));
   xmemzero(outRecord, (MAXIMUMS + 1) * TSIZE); // juliana@225_5.
   
   // juliana@230_9: solved a bug of prepared statement wrong parameter dealing.
   // Finds the index of the field on the table and reorders the record.
   while (++i < length) // Makes sure that the fields are in table creation order.
   {
      int32 idx = TC_htGet32Inv(htName2index, TC_hashCode(fields[i]));
      if (idx < 0)
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_COLUMN_NAME), fields[i]);
         return false;
      }

      setBit(tableStoreNulls, idx, isBitSet(storeNulls, i)); 
      if ((value = outRecord[idx] = record[i]) && (asChars = value->asChars) && asChars[0] == (JChar)'?' && !asChars[1])
         paramIndexes[numParams++] = idx;
   }
   *nValues = count;

   xmemmove(record, outRecord, count * TSIZE); // Saves the ordered record.
	return true;
}

// juliana@220_3
/**
 * Sorts a table, using an ORDER BY or GROUP BY clause.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param groupByClause The group by clause.
 * @param orderByClause The order by clause.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError If a heap memory allocation fails. 
 */
bool sortTable(Context context, Table* table, SQLColumnListClause* groupByClause, SQLColumnListClause* orderByClause)
{
	TRACE("sortTable")
   PlainDB* plainDB = &table->db;
   int32 count = table->columnCount,
         totalRecords = plainDB->rowCount;
   SQLColumnListClause* sortListClause;
   uint8* bufAux; 
   Heap heap = heapCreate();

   IF_HEAP_ERROR(heap)
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      heapDestroy(heap);
      return false;
   }

   bufAux = TC_heapAlloc(heap, plainDB->rowSize); // juliana@114_8

   // Binds the sort lists to the temp table columns.
   if ((orderByClause && !bindColumnsSQLColumnListClause(context, orderByClause, &table->htName2index, table->columnTypes, null, 0))
    || (groupByClause && !bindColumnsSQLColumnListClause(context, groupByClause, &table->htName2index, table->columnTypes, null, 0)))
   {
      heapDestroy(heap);
      return false;
   }
  
   // Picks one of the Column List clauses as the sort list.
   // Quick sorts the table.
   // guich@223_16: corrected a bug that could crash the application on Windows 32 when the query has an order by or group by due to a compiler 
   // error.
   sortListClause = orderByClause? orderByClause : groupByClause;
   return quickSort(context, table, newSQLValues(count, heap), newSQLValues(count, heap), newSQLValues(count, heap), sortListClause->fieldList, 
                                                               bufAux, 0, totalRecords - 1, sortListClause->fieldsCount, heap);
}

// juliana@250_1: corrected a possible crash when doing ordering operations.
// juliana@220_3
// juliana@227_20: corrected order by or group by with strings being too slow.
/**
 * Quick sort method used to sort the table.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param pivot The pivot of this partition;
 * @param someRecord1 An auxiliar record to avoid re-creating it.
 * @param someRecord2 An auxiliar record to avoid re-creating it.
 * @param fieldList The order of comparison of the fields.
 * @param bufAux A buffer to store the records.
 * @param first The first index of this partition.
 * @param last The last index of this partition.
 * @param fieldsCount The number of fields in the field list.
 * @param heap A heap to allocate temporary sort structures.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool quickSort(Context context, Table* table, SQLValue** pivot, SQLValue** someRecord1, SQLValue** someRecord2, SQLResultSetField** fieldList, 
                                                                uint8* bufAux, int32 first, int32 last, int32 fieldsCount, Heap heap)
{
	TRACE("quickSort")
   PlainDB* plainDB = &table->db;   
   SQLResultSetField* field;
   int32* vector = table->nodes;
   uint8* basbuf = plainDB->basbuf;
   uint8* columnNulls1 = table->columnNulls;
   uint8 columnNulls2[NUMBEROFBYTES(MAXIMUMS + 1)],
         columnNulls3[NUMBEROFBYTES(MAXIMUMS + 1)];
   int32 low,
         count = fieldsCount,
         high = last - first + 1, 
         pivotIndex, // guich@212_3: now using random partition (improves worst case 2000x).
         rowSize = plainDB->rowSize;
   uint32 size = 2,
         columnSize;
   StringArray** stringArray = (StringArray**)TC_heapAlloc(heap, high * TSIZE);
   StringArray* tempStringArray;

   while (--high >= 0)
      stringArray[high] = (StringArray*)TC_heapAlloc(heap, sizeof(StringArray) * fieldsCount);

   // juliana@268_2: solved possible crash using order by when a string order by field does not appear in the select field.
	while (--count >= 0) // Only loads columns used by the sorting process.
	{
      pivotIndex = (field = fieldList[count])->tableColIndex;
		if ((columnSize = field->table->columnSizes[TC_htGet32(&field->table->htName2index, field->tableColHashCode)]))
		{
			pivot[pivotIndex]->asChars = (JCharP)TC_heapAlloc(heap, (columnSize << 1) + 2);
			someRecord1[pivotIndex]->asChars = (JCharP)TC_heapAlloc(heap, (columnSize << 1) + 2);
			someRecord2[pivotIndex]->asChars = (JCharP)TC_heapAlloc(heap, (columnSize << 1) + 2);
		}
	}

   vector[0] = first;
   vector[1] = last;
   while (size) // guich@212_3: removed recursion (storing in a IntVector).
   {
      high = vector[--size];
      low = vector[--size];
      
      // juliana@213_3: last can't be equal to first.		
      if (!readRecord(context, table, pivot, pivotIndex = (last = high) == (first = low)? last : randBetween(first, last), columnNulls3, fieldList, 
                                                                                                 fieldsCount, true, heap, stringArray))
         goto error;

		// Partition
		while (true)
		{
			while (high >= low) // Finds the partitions.
			{
            // Only loads columns used by the sorting process.
				if (!readRecord(context, table, someRecord1, low, columnNulls1, fieldList, fieldsCount, true, heap, stringArray))
				   goto error;
				if (compareRecords(someRecord1, pivot, columnNulls1, columnNulls3, fieldsCount, fieldList) >= 0)
					break;
				low++;
			}

			xmemmove(bufAux, basbuf, rowSize); // juliana@114_8

			while (high >= low)
			{ 
				if (!readRecord(context, table, someRecord2, high, columnNulls2, fieldList, fieldsCount, true, heap, stringArray))
				   goto error;
				if (compareRecords(someRecord2, pivot, columnNulls2, columnNulls3, fieldsCount, fieldList) <= 0)
					break;
				high--;
			}

			if (low <= high) // Swap the records.
			{
				// juliana@114_8: optimized the swap of the records. Now the buffer is written at once.
				tempStringArray = stringArray[low];
            stringArray[low] = stringArray[high];
            stringArray[high] = tempStringArray;
            plainRewrite(context, plainDB, low++);
				xmemmove(basbuf, bufAux, rowSize);
				plainRewrite(context, plainDB, high--);
			}
			else break;
		}
		
      // Sorts the partitions.
      if (first < high) 
      {
         vector[size++] = first;
         vector[size++] = high;
      }
      if (low < last)
      {
         vector[size++] = low;
         vector[size++] = last;
      }
	}

	heapDestroy(heap);
   return true;

error:
   heapDestroy(heap);
   return false;
}

/**
 * Compares two records. Used for sorting the table to build the indices from scratch.
 * 
 * @param recSize The size of the records being compared.
 * @param vals1 The first record of the comparison.
 * @param vals2 The second record of the comparison.
 * @param types The types of the record values.
 * @return A positive number if vals1 > vals2; 0 if vals1 == vals2; -1, otherwise. It will return <code>MAX_INT_VALUE</code> if both records are 
 * equal but the record of the first is greater than the second, and <code>MIN_INT_VALUE</code> if both records are equal but the record of the 
 * first is less than the second.
 */
int32 compareSortRecords(int32 recSize, SQLValue** vals1, SQLValue** vals2, int8* types) // juliana@201_3
{
	TRACE("compareSortRecords")
   int32 i = -1,
         result;

   while (++i < recSize) // Does the comparison between the values till one of them is different from zero. 
      if ((result = valueCompareTo(null, vals1[i], vals2[i], types[i], false, false, null)) != 0)
         return result;
   
   // The values are equal. Compares with the record index.
   if ((result = types[0] == DATETIME_TYPE) || result == LONG_TYPE || result == DOUBLE_TYPE)
	{
	   if (vals1[0]->length > vals2[0]->length)
         return MAX_SHORT_VALUE;
      if (vals1[0]->length < vals2[0]->length)
         return MIN_SHORT_VALUE;
      return 0;
	}   
	if (vals1[0]->asTime > vals2[0]->asTime)
      return MAX_SHORT_VALUE;
   if (vals1[0]->asTime < vals2[0]->asTime)
      return MIN_SHORT_VALUE;
   return 0; 
}

// juliana@250_1: corrected a possible crash when doing ordering operations.
/**
 * Quick sort used for sorting the table to build the indices from scratch. This one is simpler than the sort used for order / gropu by.
 * Uses a stack instead of a recursion.
 * 
 * @param sortValues The records to be sorted.
 * @param recSize The size of the records being sorted.
 * @param types The types of the record values. 
 * @param first The first element of current partition.
 * @param last The last element of the current.
 * @param vector A temporary array to use in the recursion.
 */
void sortRecords(SQLValue*** sortValues, int32 recSize, int8* types, int32 first, int32 last, int32* vector) // juliana@201_3
{
	TRACE("sortRecords")
   SQLValue** mid;
   SQLValue** tempValues;
   int32 low,
         high,
         i;
   uint32 size = 2;
         
   // guich@212_3: checks if the values are already in order.
   i = first;
   while (++i <= last)
      if (compareSortRecords(recSize, sortValues[i - 1], sortValues[i], types) > 0)
         break;
   if (i > last)
      return;
   
   // Not fully sorted?
   vector[0] = first;
   vector[1] = last;
   while (size) // guich@212_3: removed recursion (storing in a stack).
   {
      high = vector[--size];
      low = vector[--size];

      // juliana@213_3: last can't be equal to first.
		mid = sortValues[(last = high) == (first = low)? last : randBetween(first, last)];

      while (true) // Finds the partitions.
      {
         while (high >= low && compareSortRecords(recSize, mid, sortValues[low], types)  > 0)
            low++;
         while (high >= low && compareSortRecords(recSize, mid, sortValues[high], types) < 0)
            high--;

         if (low <= high)
         {
            tempValues = sortValues[low];
            sortValues[low++] = sortValues[high];
            sortValues[high--] = tempValues;
         }
         else break;
      }

      // Sorts the partitions.
      if (first < high) 
      {
         vector[size++] = first;
         vector[size++] = high;
      }
      if (low < last)
      {
         vector[size++] = low;
         vector[size++] = last;
      }
   }
}

/** 
 * Does a radix sort on the given SQLValue array. Only integral types are allowed (SHORT, INT, LONG). This is faster than quicksort. Also used to 
 * build the indices from scratch.
 * 
 * @param source The values to be sorted. Only simple records for simple indices can be used.
 * @param length The number of values to be sorted.
 * @param type The type of the elements.
 * @param temp A temporary array for the sort.
 */
void radixSort(SQLValue*** source, int32 length, int32 type, SQLValue*** temp) // juliana@201_3
{
	TRACE("radixSort")
   int32 count[256];
   int32 index[256];
   int32 byteCount = (type == INT_TYPE)? 4 : (type == SHORT_TYPE)? 2 : 8, 
         i = 0;
   int64 mask = 0xFF,
         bits;
   SQLValue*** aux;
   
   xmemzero(count, 1024);
   xmemzero(index, 1024);

   bits = radixPass(0, source, temp, count, index, type, length);
   i = 0;
   while (++i < byteCount)
   {
      mask <<= 8;
      if ((bits & mask) != 0) // Any bits in this range?
      {
         // Swaps the from/to arrays.
         aux = source; 
         source = temp; 
         temp = aux; 

         radixPass(i, source, temp, count, index, type, length);  // Yes, sort.
      }
   }
   if (temp != source) // If the final sorted array is not at the source, copies to it.
      xmemmove(source, temp, length * TSIZE);
} 

/**
 * Executes a pass of the radix sort.
 * 
 * @param start Start bit.
 * @param source The source array,
 * @param dest The dest array where the operations with the source are copied to.
 * @param count A temporary array.
 * @param index A temporary array.
 * @param type The type of the values being sorted.
 * @param length The number of values to be sorted.
 * @return A number of bits.
 */
int64 radixPass(int32 start, SQLValue*** source, SQLValue*** dest, int32* count, int32* index, int32 type, int32 length) // juliana@201_3
{
	TRACE("radixPass")
   int32 i = 0,
         n = length, 
         ibits = 0,
         b, 
         ishift = start << 3;
   int64 lbits = 0, 
         lshift = (int64)start << 3, 
         lb;

   if (start > 0) 
      xmemzero(count, 1024);

   switch (type)
   {
      case INT_TYPE:
		case DATE_TYPE: // juliana@214_9: index creation for date types could create corrupted indices.
         if (start == 0)
            while (--n >= 0) 
            {
               count[(b = (*source[i++])->asInt) & 0xFF]++; 
               ibits |= b;
            }
         else if (start == 3)
            while (--n >= 0) 
               count[((*source[i++])->asInt >> ishift) + 128]++;
         else
            while (--n >= 0) 
               count[((*source[i++])->asInt >> ishift) & 0xFF]++;
         break;

      case SHORT_TYPE:
         if (start == 0)
            while (--n >= 0) 
            {
               count[(b = (*source[i++])->asShort) & 0xFF]++; 
               ibits |= b;
            }
         else
            while (--n >= 0) 
               count[((*source[i++])->asShort >> ishift) + 128]++;
         break;
      case LONG_TYPE:
         if (start == 0)
            while (--n >= 0) 
            {
               count[(int32)((lb = (*source[i++])->asLong) & 0xFF)]++; 
               lbits |= lb;
            }
         else if (start == 7)
            while (--n >= 0) 
               count[(int32)(((*source[i++])->asLong >> lshift) + 128)]++;
         else
            while (--n >= 0) 
               count[(int32)(((*source[i++])->asLong >> lshift) & 0xFF)]++; 
   }            

   index[0] = i = 0;
   n = 255; 
   while (--n >= 0)
   {
      index[i + 1] = index[i] + count[i];
      i++;
   }

   i = 0; 
   n = length;
   switch (type)
   {
      case INT_TYPE:
		case DATE_TYPE: // juliana@214_9: index creation for date types could create corrupted indices.
         if (start == 0)
            while (--n >= 0)
            {
               dest[index[((*source[i])->asInt) & 0xFF]++] = source[i];
               i++;
            }
         else if (start == 3)
            while (--n >= 0)
            {
               dest[index[((*source[i])->asInt >> ishift) + 128]++] = source[i];
               i++;
            }
         else
            while (--n >= 0)
            { 
               dest[index[((*source[i])->asInt >> ishift) & 0xFF]++] = source[i];
               i++;
            }
         break;
      case SHORT_TYPE:
         if (start == 0)
            while (--n >= 0)
            {
               dest[index[((*source[i])->asShort) & 0xFF]++] = source[i];
               i++;
            }
         else
            while (--n >= 0)
            {
               dest[index[((*source[i])->asShort >> ishift) + 128]++] = source[i];
               i++;
            }
         break;
      case LONG_TYPE:
         if (start == 0)
            while (--n >= 0)
            {
               dest[index[(int32)(((*source[i])->asLong) & 0xFF)]++] = source[i];
               i++;
            }
         else if (start == 7)
            while (--n >= 0)
            {
               dest[index[(int32)(((*source[i])->asLong >> lshift) + 128)]++] = source[i];
               i++;
            }
         else
            while (--n >= 0)
            {
               dest[index[(int32)(((*source[i])->asLong >> lshift) & 0xFF)]++] = source[i];
               i++;
            }
   }            
   
   return type == LONG_TYPE? lbits : ibits;
}

// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Creates the table files and loads its meta data if it was already created.
 *
 * @param context The thread context where the function is being executed.
 * @param name The name of the table.
 * @param sourcePath The path of the table on disk.
 * @param create Indicates if the table is to be created or just opened.
 * @param isAscii Indicates if the table strings are to be stored in the ascii format or in the unicode format.
 * @param useCrypto Indicates if the table uses cryptography.
 * @param nodes An array of nodes indices.
 * @param throwException Indicates that a TableNotClosedException should be thrown.
 * @param heap The table heap.
 * @return The table created or <code>null</code> if an error occurs.
 */
Table* tableCreate(Context context, CharP name, TCHARP sourcePath, bool create, bool isAscii, bool useCrypto, int32* nodes, 
                                                                                           bool throwException, Heap heap) // juliana@220_5
{
   TRACE("tableCreate")
   Table* table = (Table*)TC_heapAlloc(heap, sizeof(Table));
   PlainDB* plainDB = &table->db;

   table->currentRowId = 1;
   table->auxRowId = ATTR_DEFAULT_AUX_ROWID; // rnovais@570_61
   table->sourcePath = sourcePath;
   table->heap = heap; 
   table->nodes = nodes;
   
   IF_HEAP_ERROR(heap)
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      goto error;
   }

   if (!createPlainDB(context, &table->db, name, create, useCrypto, sourcePath)) // Creates or opens the table files.    
      goto error;

   if (name && (plainDB->db.size || create)) // The table is already created if the .db is not empty.
   {
		xstrcpy(table->name, name);
		plainDB->isAscii = isAscii;
      if (plainDB->db.size && !tableLoadMetaData(context, table, throwException)) // juliana@220_5
			goto error; // juliana@220_8: does not let the table be truncated if an error occurs when loading its metadata.
   } 
   return table;
   
error:
   freeTable(context, table, false, false);
   return null;
}

// juliana@114_9
/**
 * Creates a table, which can be stored on disk or on memory (result set table).
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param tableName The table name.
 * @param names The table column names.
 * @param hashes The table column hashes.
 * @param types The table column types.
 * @param sizes The table column sizes.
 * @param attrs The table cxlumn attributes.
 * @param defaultValues The table column default values.
 * @param primaryKeyCol The primary key column.
 * @param composedPK The composed primary key index in the composed indices.
 * @param composedPKCols The columnns that are part of the composed primary key.
 * @param ComposedPKColsSize The size of the composed primary key.
 * @param count The column count.
 * @param heap A heap to allocate the table.
 * @return The table handle or <code>null</code> if an error occurs.
 * @throws AlreadyCreatedException If the table already exists.
 * @throws OutOfMemoryError If an memory allocation fails.
 */
Table* driverCreateTable(Context context, TCObject driver, CharP tableName, CharP* names, int32* hashes, int8* types, int32* sizes, uint8* attrs, 
       SQLValue** defaultValues, int32 primaryKeyCol, int32 composedPK, uint8* composedPKCols, int32 composedPKColsSize, int32 count, Heap heap)
{
	TRACE("driverCreateTable")
   Table* table = null;
   TCHARP sourcePath = getLitebaseSourcePath(driver);
	int32 appCrid = OBJ_LitebaseAppCrid(driver);
   Hashtable* htTables = getLitebaseHtTables(driver);

   if (!tableName) // Temporary table.
	{
	   // rnovais@570_75 juliana@220_5
		if (!(table = tableCreate(context, null, sourcePath, true, false, false, getLitebaseNodes(driver), true, heap))) 
         return null; 

      table->db.headerSize = 0;

      IF_HEAP_ERROR(heap)
      {
         goto error;
      }

      table->isModified = true; // juliana@226_4
      table->answerCount = -1; // juliana@230_14
		if (!tableSetMetaData(context, table, null, hashes, types, sizes, null, null, null, -1, -1, count, 0))
         goto error;
	}
   else // Normal table.
   {
		char name[DBNAME_SIZE];

      // guich@_105: First, checks if the table was already created in this connection.
      if ((table = (Table*)TC_htGetPtr(htTables, TC_hashCode(tableName))))
      {
         TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_TABLE_ALREADY_CREATED), table->name);
         return null;
      }
   
      if (!getDiskTableName(context, appCrid, tableName, name)) // Gets the table real name.
         return null;
   
		// juliana@220_5
		// juliana@253_8: now Litebase supports weak cryptography.  
		if (!(table = tableCreate(context, name, sourcePath, true, OBJ_LitebaseIsAscii(driver), OBJ_LitebaseUseCrypto(driver), 
                                                                                              getLitebaseNodes(driver), true, heap)))
		   goto error;

      IF_HEAP_ERROR(heap)
      {
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);        
			goto error;
		}

      // Doesn't put in the hashtable the temporary tables.
      // Stores the meta data.
		// Creates the index for the primary key or the composed index for the composed primary key. // juliana@114_9
      if (!TC_htPutPtr(htTables,  TC_hashCode(tableName), table)
       || !tableSetMetaData(context, table, names, hashes, types, sizes, attrs, composedPKCols, defaultValues, primaryKeyCol, composedPK, count, 
                                                                                                                              composedPKColsSize)
       || (primaryKeyCol != NO_PRIMARY_KEY && !driverCreateIndex(context, table, &hashes[primaryKeyCol], 0, 1, null))
       || (composedPKColsSize > 0 && !driverCreateIndex(context, table, null, 0, composedPKColsSize, composedPKCols))) 
		{
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);        
			goto error;
		}
	}
   return table;
   
error:
   freeTable(context, table, true, false);
	TC_htRemove(htTables, TC_hashCode(tableName));
	return null;
}

/**
 * Renames a table. This never happens to be a temporary <code>ResultSet</code> memory table.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The LitebaseConnection.
 * @param table The table being renamed.
 * @param newTableName The new table name.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError If a memory allocation fails. 
 */
bool renameTable(Context context, TCObject driver, Table* table, CharP newTableName) // rnovais@566_10
{
   TRACE("renameTable")
	char result[DBNAME_SIZE];
   IntBuf intBuf;
   char oldTableName[DBNAME_SIZE];
   TCHARP sourcePath = table->sourcePath;
	int32 i,
         length;
	Hashtable* htTables = getLitebaseHtTables(driver);
   Index** columnIndexes = table->columnIndexes;
   ComposedIndex** composedIndexes = table->composedIndexes;

   // Gets the real name.
   xstrcpy(oldTableName, &table->name[5]);
   xmemmove(result, table->name, 5);
   xstrcpy(&result[5], newTableName);
   length = xstrlen(result);

   // Renames the table.
   if (!plainRename(context, &table->db, result, sourcePath))
	   return false;
   xstrcpy(table->name, result);

   // Renames the indices.
   i = table->columnCount;
   result[length] = '$';
	while (--i >= 0)
   {
		if (columnIndexes[i])
      {
			xstrcpy(&result[length + 1], TC_int2str(i, intBuf));
         if (!indexRename(context, columnIndexes[i], result)) // Renames the index files.
            return false;
      }
   }

   // juliana@220_17: rename table now renames the composed indices.
   i = table->numberComposedIndexes;
   result[length] = '&';
   while (--i >= 0)  
   {
		xstrcpy(&result[length + 1], TC_int2str(i + 1, intBuf));
		if (!indexRename(context, composedIndexes[i]->index, result)) // Renames the index files.
         return false;
   }

	if (!TC_htPutPtr(htTables, TC_hashCode(newTableName), table)) // Adds the new table name to the hash table.
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return false;
   }
   TC_htRemove(htTables, TC_hashCode(oldTableName)); // Removes the old table name from the hash table.
   return true;
}

/**
 * Renames a column of a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose column is being renamed.
 * @param oldColumn The name of the old column.
 * @param newColumn The name of the new column.
 * @param reuseSpace Indicates if the column name space can be reused.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the old column does not exist or the new column already exists.
 * @throws OutOfMemoryError If a memory allocation fails. 
 */
bool renameTableColumn(Context context, Table* table, CharP oldColumn, CharP newColumn, bool reuseSpace) // rnovais@566_17
{
   TRACE("renameColumnTable")
   Hashtable* htName2index = &table->htName2index;
   int32 oldHash = TC_hashCode(oldColumn),
         newHash = TC_hashCode(newColumn),
         oldIdx = TC_htGet32Inv(htName2index, oldHash), // Gets the old column name. It must exist.
         newIdx = TC_htGet32Inv(htName2index, newHash); // The new column name can't exist.

   if (oldIdx < 0) 
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_NOT_FOUND), oldColumn);
      return false;
   }
   if (newIdx >= 0)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_COLUMN_ALREADY_EXIST), newColumn);
      return false;
   }

   // Changes the column information.
   if (!TC_htPut32(&table->htName2index, newHash, oldIdx))
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return false;
   }
   TC_htRemove(&table->htName2index, oldHash);
   table->columnHashes[oldIdx] = oldHash;
   if (reuseSpace)
      xstrcpy(table->columnNames[oldIdx], newColumn);
   else
      table->columnNames[oldIdx] = newColumn;
   return tableSaveMetaData(context, table, TSMD_EVERYTHING);
}

// juliana@201_3: if an index is created after populating or purging the table, its nodes will be full in order to improve its usage and search speed.
/**
 * Re-builds an index of a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table being re-indexed.
 * @param column The table column number of the index or -1 for a composed index.
 * @param isPKCreation Indicates that the index is of a primary key. 
 * @param composedIndex The composed index to be rebuilt or null in case of a simple index.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If there is a null in the primary key or a duplicated key.
 * @throws OutOfMemoryError If a heap memory allocation fails. 
 */
bool tableReIndex(Context context, Table* table, int32 column, bool isPKCreation, ComposedIndex* composedIndex)
{
   TRACE("tableReIndex")
   Heap heap;
   PlainDB* plainDB = &table->db;
   uint8* basbuf = plainDB->basbuf;
   Index* index = (column != -1)? table->columnIndexes[column] : composedIndex->index; // Gets the index.
   int32 n = plainDB->rowCount,  
         i = -1;
	bool isDelayed = index->isWriteDelayed;

	if (!indexDeleteAllRows(context, index)) // Cleans the index values.
      return false;
   if (!indexSetWriteDelayed(context, index, true)) // This makes the index creation faster.
      goto error1;

   if (index->isOrdered && !composedIndex) // Simple index using rowid.
	{
		SQLValue emptyValue;
	   SQLValue* value = &emptyValue;
		xmemzero(&emptyValue, sizeof(SQLValue));

      while (++i < n)
		{
         if (!plainRead(context, plainDB, i)) // Reads the row.
            goto error1;
         if (!recordNotDeleted(basbuf)) // Only gets non-deleted records.
            continue;  
            
         // juliana@230_12
			if (!readValue(context, plainDB, &emptyValue, 0, INT_TYPE, basbuf, false, false, false, -1, null) // juliana@220_3
          || !indexAddKey(context, index, &value, i))
            goto error1;
		}
	}
	else
	{
		int32 k = 0,
            rows = plainDB->rowCount, // juliana@284_1: Solved a possible application crash when recreating indices.
            j = rows,
            columnCount = table->columnCount,
            indexCount = index->numberColumns,
            bytes = NUMBEROFBYTES(columnCount),
            indexSize = index->numberColumns,
            type,
            offset = 0,
            compare;
		bool isNull;
      SQLValue*** values;
      uint8* columnNulls0 = table->columnNulls;
      uint8* nullsPosition = basbuf + table->columnOffsets[columnCount];
      uint8* columns = null;
      uint16* columnOffsets = table->columnOffsets;
      int8* types = index->types;
      
      if (!rows) // juliana@223_14: solved possible memory problems.
         return indexSetWriteDelayed(context, index, isDelayed);

      // juliana@223_14: solved possible memory problems.
      heap = heapCreate();
		IF_HEAP_ERROR(heap)
		{
			TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
			goto error2;
		}

      IF_HEAP_ERROR(table->heap) // juliana@223_14: solved possible memory problems.
      {
			TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
			goto error2;
		}

      // juliana@270_25: solved a possible crash when an OutOfMemoryError occurs when creating or recreating indices.
      IF_HEAP_ERROR(index->heap)
      {
         TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
			goto error2;
      }

      type = *types;
      if (column != -1)
         offset = columnOffsets[column];
      else
         columns = composedIndex->columns;
		
      // Allocates the records for the sorting.
      values = (SQLValue***)TC_heapAlloc(heap, TSIZE * rows);
	   while (--j >= 0)
		   values[j] = (SQLValue**)TC_heapAlloc(heap, TSIZE * indexCount);

		while (++i < n)
		{
         isNull = false; // Resets the null info.

			if (!plainRead(context, plainDB, i)) // Reads the row.
            goto error2;
			if (!recordNotDeleted(basbuf)) // Only gets non-deleted records.
            continue;

			// juliana@201_22: the null columns information wasn't being read when re-creating an index.
			xmemmove(columnNulls0, nullsPosition, bytes);

         j = indexSize;
         while (--j >= 0)
			   values[k][j] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));
			
         if (column != -1)
			{
			   // juliana@230_12
				if (!readValue(context, plainDB, *values[k], offset, type, basbuf, false, isNull = isBitSet(columnNulls0, column), false, -1, heap))
				   goto error2;

				// juliana@202_12: Corrected null values dealing when building an index.
				// juliana@202_10: Corrected a bug that would cause a DriverException if there was a null in an index field when creating it after the table is populated.
				if (isPKCreation && columnNulls0 && isNull)
				{				
               TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_PK_CANT_BE_NULL), 0);
               goto error2;
				}
			}
			else
			{
				j = indexSize;
            while (--j >= 0)
				{
				   // juliana@230_12
					if (!readValue(context, plainDB, values[k][j], columnOffsets[columns[j]], types[j], basbuf, false, isNull |= isBitSet(columnNulls0, columns[j]), false, -1, heap))
				      goto error2;
				   
					// juliana@202_12: Corrected null values dealing when building an index.
					// juliana@202_10: Corrected a bug that would cause a DriverException if there was a null in an index field when creating it after the table is populated.
					if (isPKCreation && columnNulls0 && isNull)
					{
						TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_PK_CANT_BE_NULL));
                  goto error2;
					}
				}
			}

			if (isNull) // Do not store null records.
				rows--;
			else
				// juliana@202_7: Corrected a bug that would cause long and double indices to be built incorrectly.
				// The record value is stored in an empty field of the first record column value.
            if (type == DATETIME_TYPE || type == LONG_TYPE || type == DOUBLE_TYPE)
					(*values[k++])->length = i;
				else
					(*values[k++])->asTime = i;
      }

      rows = k; // juliana@270_22: solved a possible crash when the table is corrupted on Android and possibly on other platforms.
		if (!index->isOrdered)
		{
         // A radix sort is done for integer types. It is much more efficient than quick sort.
			if (indexSize == 1 && (type == SHORT_TYPE || type == INT_TYPE || type == LONG_TYPE || type == DATE_TYPE))
			{
				SQLValue*** tempValues = (SQLValue***)TC_heapAlloc(heap, rows * TSIZE);
				radixSort(values, rows, type, tempValues);
			}
			else
				sortRecords(values, indexSize, types, 0, rows - 1, table->nodes); 
			index->isOrdered = true; // The index elements will be inserted in the right order.
      }		

      k = -1;
      while (++k < rows)
		{
         // If it is primary key, check first if there is violation.
			if (isPKCreation && k > 0 && (!(compare = compareSortRecords(indexSize, values[k], values[k - 1], types)) || compare == MIN_SHORT_VALUE 
			                                                                                                          || compare == MAX_SHORT_VALUE))
			{
				TC_throwExceptionNamed(context, "litebase.PrimaryKeyViolationException", getMessage(ERR_STATEMENT_CREATE_DUPLICATED_PK), table->name);
			   
            goto error2; // juliana@223_14: solved possible memory problems.
			}

			// juliana@202_7: Corrected a bug that would cause long and double indices to be built incorrectly.
			if (((type == DATETIME_TYPE || type == LONG_TYPE || type == DOUBLE_TYPE) && !indexAddKey(context, index, values[k], (*values[k])->length))
			 || (type != DATETIME_TYPE && type != LONG_TYPE && type != DOUBLE_TYPE && !indexAddKey(context, index, values[k], (*values[k])->asTime)))
            goto error2; // juliana@223_14: solved possible memory problems.
		}

		if (!composedIndex || column) // An index beggining with rowid is always ordered.
         index->isOrdered = false;
      heapDestroy(heap);
	}
	
	return indexSetWriteDelayed(context, index, isDelayed); // Uses the user desired delayed settings again. 
	
error2:
   heapDestroy(heap);
   if (column) // An index beggining with rowid is always ordered.
      index->isOrdered = false;  
error1:
   indexSetWriteDelayed(context, index, isDelayed);
   return false;
}

/**
 * Creates a simple index for the table for the given column.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param fullTableName The table disk name.
 * @param columnIndex The column of the index.
 * @param columnSizes The sizes of the columns.
 * @param columnTypes The types of the columns.
 * @param exist Indicates that the index files already exist. 
 * @param heap A heap to allocate the index structure.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool indexCreateIndex(Context context, Table* table, CharP fullTableName, int32 columnIndex, int32* columnSizes, int8* columnTypes, bool exist, Heap heap)
{
	TRACE("indexCreateIndex")
   char indexName[DBNAME_SIZE];
   IntBuf intBuf;
   int32 tableLen = xstrlen(fullTableName);

   // The index name.
   xstrcpy(indexName, fullTableName);
   indexName[tableLen] = '$';
   xstrcpy(&indexName[tableLen + 1], TC_int2str(columnIndex, intBuf));
   
   // rnovais@113_1
   if (!(table->columnIndexes[columnIndex] = createIndex(context, table, columnTypes, columnSizes, indexName, 1, exist, heap))) 
      return false;

   // rowid is always an ordered index.
   table->columnIndexes[columnIndex]->isOrdered = !columnIndex; // guich@110_5
   table->columnAttrs[columnIndex] |= ATTR_COLUMN_HAS_INDEX;
   return true;
}

/**
 * Creates a composed index for a given table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param fullTableName The table disk name.
 * @param columnIndexes he columns of the index.
 * @param columnSizes The sizes of the columns.
 * @param columnTypes The types of the columns.
 * @param numberColumns The number of columns of the index.
 * @param newIndexNumber An id for the composed index.
 * @param increaseArray Indicates if the composed indices array must be increased.
 * @param exist Indicates that the index files already exist. 
 * @param heap A heap to allocate the index structure.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the maximum number of composed indices was achieved.
 */
bool indexCreateComposedIndex(Context context, Table* table, CharP fullTableName, uint8* columnIndexes, int32* columnSizes, int8* columnTypes, 
                                                             int32 numberColumns, int32 newIndexNumber, bool increaseArray, bool exist, Heap heap)
{
	TRACE("indexCreateComposedIndex")
   char indexName[DBNAME_SIZE];
   IntBuf intBuf;
   int32 tableLen = xstrlen(fullTableName),
         size = table->numberComposedIndexes,
         id = (newIndexNumber < 0)? -newIndexNumber : newIndexNumber; // newIndexNumber < 0 means that this was a index.
   ComposedIndex* composedIndex;
   
   // The index name.
   xstrcpy(indexName, fullTableName);
   indexName[tableLen] = '&';
   xstrcpy(&indexName[tableLen + 1], TC_int2str(id, intBuf)); // Passes the newIndex index id.

   composedIndex = createComposedIndex(id, columnIndexes, numberColumns, table->heap);
   if (increaseArray && table->numberComposedIndexes == MAX_NUM_INDEXES_APPLIED)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_COMP_INDICES));
      return false;
   }

   // Creates the index of the composed index.
   if (!(composedIndex->index = createIndex(context, table, columnTypes, columnSizes, indexName, numberColumns, exist, heap)))
      return false;

   if (increaseArray)
   {
      table->composedIndexes[size] = composedIndex; // New composed index.
      table->numberComposedIndexes++;
   }
   else
      table->composedIndexes[id - 1] = composedIndex;
   
	composedIndex->index->isOrdered = !*columnIndexes; // The rowid is the column 0.
   return true;
}

// juliana@220_3
/**
 * Reads the entire record from a table.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param record An array where the record filed values will be stored.
 * @param recPos The record index.
 * @param columnNulls A buffer where the nulls will be stored.
 * @param fieldList A field list that indicates which fields to read from the table. 
 * @param fieldsCount The number of fields in the field list.
 * @param isTempBlob Indicates if a blob must be loaded or not.
 * @param heap A heap to allocate the temporary strings when sorting a temporary table.
 * @param stringArray A temporary string array used when sorting a temporary table. 
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool readRecord(Context context, Table* table, SQLValue** record, int32 recPos, uint8* columnNulls, SQLResultSetField** fieldList, 
                                                                  int32 fieldsCount, bool isTempBlob, Heap heap, StringArray** stringArray)
{
	TRACE("readRecord")
   int32 i, 
         j,
         asLength;
   bool isTemp = !*table->name;
   PlainDB* plainDB = &table->db;
   uint8* basbuf = plainDB->basbuf;
   uint16* columnOffsets = table->columnOffsets;
   int8* columnTypes = table->columnTypes;
   int32* columnSizes = table->columnSizes; // juliana@230_12
   JCharP asString;

   if (!plainRead(context, plainDB, recPos))
      return false;

   // juliana@226_12: corrected a bug that could make aggregation function not work properly.
   if (!isTemp)
   {
      xmove4(&i, plainDB->basbuf);
      if ((i & ROW_ATTR_MASK) == ROW_ATTR_DELETED)
         return true;
   }

   i = fieldList? fieldsCount : table->columnCount;
   xmemmove(columnNulls, basbuf + columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   if (fieldList) // Reads only the columns used during sorting. 
      while (--i >= 0)
      {
         // juliana@227_20: corrected order by or group by with strings being too slow.
         j = fieldList[i]->tableColIndex;
         if ((columnTypes[j] != CHARS_TYPE && columnTypes[j] != CHARS_NOCASE_TYPE) || !stringArray[recPos][i].string)
         {
            // juliana@230_12
            if (!readValue(context, plainDB, record[j], columnOffsets[j], columnTypes[j], basbuf, isTemp, isBitSet(columnNulls, j), isTempBlob, -1,
                                                                                                                                    null))
               return false;
            if (record[j]->length)
            {
               asString = stringArray[recPos][i].string = (JCharP)TC_heapAlloc(heap, 
                                                                  (asLength = ((stringArray[recPos][i].length = record[j]->length) + 1) << 1) + 2);
               xmemmove(asString, record[j]->asChars, asLength);
            }
         }
         else
         {
            record[j]->length = stringArray[recPos][i].length;
            xmemmove(record[j]->asChars, stringArray[recPos][i].string, record[j]->length << 1);
         }

      }
      
   // juliana@230_12: improved recover table to take .dbo data into consideration.
   else // Reads all columns of the table.
      while (--i >= 0)
         if (!readValue(context, plainDB, record[i], columnOffsets[i], columnTypes[i], basbuf, isTemp, isBitSet(columnNulls, i), isTempBlob, columnSizes[i], heap))
            return false;
   return true;
}  

// juliana@253_8: now Litebase supports weak cryptography.
/**
 * Writes a record on a disk table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param values The values to be written on the table.
 * @param recPos The record position.
 * @param heap A heap to allocate temporary structures.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool writeRecord(Context context, Table* table, SQLValue** values, int32 recPos, Heap heap)
{
   TRACE("writeRecord")
   PlainDB* plainDB = &table->db;
   XFile* db = &plainDB->db;
   XFile* dbo = &plainDB->dbo;
   int32 columnCount = table->columnCount,
         i = columnCount,
         writePos = -1,
         rowid = 0,
         primaryKeyCol = table->primaryKeyCol, 
         j,
         numberOfBytes = NUMBEROFBYTES(columnCount),
         type, 
         offset,
         crc32, // juliana@230_12
         oldPos,
         k; // juliana@270_23
   bool changePos,
        addingNewRecord = recPos == -1,
        valueOk,
        hasIndex,
        isNullVOld,
        isNull;
   int8* columnTypes = table->columnTypes;
   int32* columnSizes = table->columnSizes;
   uint16* columnOffsets = table->columnOffsets;
   uint8* basbuf = plainDB->basbuf;
   uint8* columnNulls0 = table->columnNulls;
   uint8 columnNulls1[NUMBEROFBYTES(MAXIMUMS + 1)];
   uint8* storeNulls = table->storeNulls;
   uint8* columnAttrs = table->columnAttrs;
   uint8* buffer = basbuf + table->columnOffsets[columnCount];
   SQLValue** defaultValues = table->defaultValues;
   SQLValue* vOlds[MAXIMUMS + 1];
   SQLValue* tempRecord;
   Index* idx;
   Index** columnIndexes = table->columnIndexes;
   Key tempKey;
   SQLValue value;
  
   xmemzero(columnNulls0, numberOfBytes); // First of all, clear the columnNulls used.  
   
   while (--i) // 0 = rowid = never is null.
   {
      // juliana@226_11: corrected a constant Java String truncation when using it with an insert or update prepared statement and its size were 
      // bigger than the column definition.
      // juliana@225_7: a PrimaryKeyViolation was not being thrown when two strings with the same prefix were inserted and the field definition had 
      // the size of the prefix and a primary key.
      // juliana@214_6: must trim strings during index update if they are longer than the field size definition.
      if (columnSizes[i] && (tempRecord = values[i]) && (int32)tempRecord->length > (j = columnSizes[i]))
         tempRecord->length = j;

      if (isBitSet(storeNulls, i)) // If not explicit to store null.
         setBitOn(columnNulls0, i);
      else if (addingNewRecord)
      {
         if (!definedAsNotNull(columnAttrs[i])) // Can be null.
         {
            if (!values[i] || values[i]->isNull)
            {
               if (!defaultValues[i]) // It doesn't have a default value.
                  setBitOn(columnNulls0, i); // Sets the column as null.
               else
                  values[i] = defaultValues[i]; // If it doesn't have a value, stores the default value.
            }
         }
         else if (!values[i]) // At this moment, if it can't be null, necessarily it has a default value.
            values[i] = defaultValues[i];
      }
   }
   
   // If there is a primary key column, there can't be repeated values.
   if (primaryKeyCol != NO_PRIMARY_KEY && values[primaryKeyCol] 
    && !checkPrimaryKey(context, table, &values[primaryKeyCol], recPos, addingNewRecord, heap))
      return false;
   if ((i = table->numberComposedPKCols) > 0)
   {
      uint8* composedPrimaryKeyCols = table->composedPrimaryKeyCols;
      
      while (--i >= 0)
         vOlds[i] = values[composedPrimaryKeyCols[i]];
      if (!checkPrimaryKey(context, table, vOlds, recPos, addingNewRecord, heap))
         return false; // guich@564_18: changed from -1 to 0.
      xmemzero(vOlds,  columnCount * TSIZE);
   }

   if (addingNewRecord) // Adding a record?
   {
      if (!plainAdd(context, plainDB))
         return false;
      writePos = plainDB->rowCount;
      (*values)->asInt = rowid = table->currentRowId; // Writes the rowId, marking the attribute as new.
      if (!resetAuxRowId(context, table))
         return false;
   }
   else
   {
      if (!plainRead(context, plainDB, recPos)) // May have to read the value before deleting the index value.
         return false;
      writePos = recPos;
      xmove4(&rowid, basbuf);
      table->wasUpdated = true; // Indicates if the table was updated after the last time it was opened.
   }

   tempKey.keys = &value;

   i = -1;

   IF_HEAP_ERROR(table->heap)
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return false;
   }

   IF_HEAP_ERROR(heap)
   {
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return false;
   }

   // Verifies if the value to be read is null. IMPORTANT: the array of bytes can't be stored in table.columnNulls[0]. This variable is in use. 
   // Then, it will be read in Table.columnNulls[1].
   xmemmove(columnNulls1, buffer, numberOfBytes);

   while (++i < columnCount)
   {
      isNullVOld = false;
      isNull = isBitSet(columnNulls0, i);
		idx = columnIndexes[i]; // If a new value is being written, the table index (if any) needs to be updated.
      hasIndex = (valueOk = (values[i] || isNull)) && idx; // Only if this row is being updated.
      type = columnTypes[i];
      offset = columnOffsets[i];
      changePos = false;

      if (!addingNewRecord) // Reads the previous value if it is an update.
      {
         vOlds[i] = (SQLValue*)TC_heapAlloc(heap, sizeof(SQLValue));

         // juliana@230_12
         // The offset is already positioned and is restored after read.
         if (!readValue(context, plainDB, vOlds[i], offset, type, basbuf, false, isNullVOld = isBitSet(columnNulls1, i), false, -1, heap)) 
            return false;
            
         // juliana@238_5: corrected a possible table corruption when using blobs.
         // juliana@202_19: UPDATE could corrupt .dbo.
		   // juliana@202_21: Always writes the string at the end of the .dbo. This removes possible bugs when doing updates.
         // A blob in the .dbo must have its position changed if the the new value is greater than the old one.
         if (valueOk && type == BLOB_TYPE && values[i] && vOlds[i]->length < values[i]->length)
            changePos = true;
      }

      // Writes the value.
      if (!writeValue(context, plainDB, values[i], &basbuf[offset], type, columnSizes[i], valueOk, addingNewRecord || changePos, isNull, false))
         return false;

      // If the new and the old values are null and the column is not the rowid, sets the value as null.
      if (i > 0 && isNullVOld && (!values[i] || values[i]->isNull))
         setBitOn(columnNulls0, i);

      if (hasIndex)
      {
         // juliana@225_4: corrected a bug that could not build the index correctly if there was the value 0 inserted in the index.
         // Updating key? Removes the old one and adds the new one.
         if (addingNewRecord || valueCompareTo(null, vOlds[i], values[i], type, isNullVOld, isNull, null)) 
         {
            if (!isNull) // If it is updating a 'non-null value' to 'null value', only removes it.
            {
               oldPos = db->position;
               tempRecord = values[i];
               IF_HEAP_ERROR(idx->heap)
               {
                  TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
                  return false;
               }
               
               if (!indexAddKey(context, idx, &tempRecord, writePos)) // juliana@223_14: solved possible memory problems.
                  return false;
               nfSetPos(db, oldPos);
            }
            if (!addingNewRecord && !isNullVOld)
            {
               oldPos = dbo->position; // juliana@202_19: UPDATE could corrupt .dbo.
               tempRecord = vOlds[i];
               keySet(&tempKey, &tempRecord, idx, 1);
               if (!indexRemoveValue(context, &tempKey, writePos))
                  return false;
               dbo->position = oldPos; // juliana@202_19: UPDATE could corrupt .dbo.
            }
         }
      }
   }

   if ((j = i = table->numberComposedIndexes)) // Fills the composed indices.
   {
      ComposedIndex* compIndex;
      ComposedIndex** composedIndexes = table->composedIndexes;
      Index* index;
      SQLValue* vals[MAXIMUMS];
      SQLValue* oldVals = null;
      uint8* columns;
      int32 maxNumberColumns = 0,
            column;
      bool remove, // juliana@230_43
           change; // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.

      // Allocates the records for the composed indices just once, using the maximum size.
      while (--j >= 0)
         maxNumberColumns = MAX(maxNumberColumns, composedIndexes[j]->numberColumns);
      
      if (!addingNewRecord)
         oldVals = (SQLValue*)TC_heapAlloc(heap, maxNumberColumns * sizeof(SQLValue));
      
      // If a composed index has all column values equal to null, it is not possible to store this row in the index. Composed indices that have at 
      // least one field that is not null can be stored in the index, but the null values must be handled. This implies in changing the index format. 
      // Maybe using the same way like null values are handled on tables. This certainly will decrease the index performance. For simplicity, the key 
      // will be stored in a composed index only if all values are not null. This is a project choice.
      while (--i >= 0)
      {
         compIndex = composedIndexes[i];
         index = compIndex->index;
         j = compIndex->numberColumns;
         columns = compIndex->columns;
         valueOk = remove = true;
			change = false; // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.
			oldPos = db->position; // juliana@201_4: corrected a bug that could corrupt the table when updating the composed index.
         
         while (--j >= 0)
         {
            column = columns[j];
            if (isBitSet(columnNulls0, column)) // Only stores non-null values.
               valueOk = false;
               
            // juliana@230_43: solved a possible exception if updating a table with composed indices and nulls.
            if (isBitSet(columnNulls1, column))
               remove = false;

            // Sets the old and new index values.
            // juliana@282_2: Doesn't update the composed index or PK if the old values are the same of the new ones. 
            if (values[column]) // juliana@201_18: can't reuse values. Otherwise, it will spoil the next update.
            {
               vals[j] = values[column];
               if (addingNewRecord || valueCompareTo(null, values[column], vOlds[column], columnTypes[column], !remove, !valueOk, null))
                  change = true;
            }
            else
               vals[j] = vOlds[column];
            if (!addingNewRecord)
               xmemmove(&oldVals[j], vOlds[column], sizeof(SQLValue));
         }

         // juliana@230_43: solved a possible exception if updating a table with composed indices and nulls.
         // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.
         if (!addingNewRecord && remove && change) // Removes the old composed index entry.
         {
            tempKey.index = index;
            tempKey.record = NO_VALUE;
            tempKey.keys = oldVals;
            if (!indexRemoveValue(context, &tempKey, writePos))
               return false;
         }  

         // juliana@252_2: corrected a bug of possible composed index corruption when updating or deleting data.
         if (valueOk && change) // juliana@201_4: corrected a bug that could corrupt the table when updating the composed index.
         {
            if (!indexAddKey(context, index, vals, writePos))
               return false;
            nfSetPos(db, oldPos);
         }
      }
   }
   
   xmemmove(buffer, columnNulls0, numberOfBytes); // After the columns, stores the bytes of the null values.

   // juliana@220_4: added a crc32 code for every record.
   k = basbuf[3]; // juliana@270_23: Corrected a RowIterator bug of an update changing an updated row to synced again.
   basbuf[3] = 0; // juliana@222_5: The crc was not being calculated correctly for updates.
   i = columnOffsets[columnCount] + numberOfBytes;
   
   // juliana@230_12: improved recover table to take .dbo data into consideration.
   crc32 = updateCRC32(basbuf, i, 0);
	
   if (table->version == VERSION_TABLE)
   {      
      int32 length;

      j = columnCount;
      while (--j)
         if (columnTypes[j] == CHARS_TYPE || columnTypes[j] == CHARS_NOCASE_TYPE)
         {
            if (values[j] && isBitUnSet(columnNulls0, j))
               crc32 = updateCRC32((uint8*)values[j]->asChars, values[j]->length << 1, crc32);
            else if (!addingNewRecord && isBitUnSet(columnNulls0, j) && !vOlds[j]->isNull && vOlds[j]->asChars)
               crc32 = updateCRC32((uint8*)vOlds[j]->asChars, vOlds[j]->length << 1, crc32);
         }
         else if (columnTypes[j] == BLOB_TYPE)
         {	
        	   if (values[j] && isBitUnSet(columnNulls0, j))
            {
               // juliana@253_12: corrected a possible table corruption when adding a small blob.
               // juliana@239_4: corrected a non-desired possible row delete when recovering a table with blobs.
               length = MIN((uint32)columnSizes[j], values[j]->length); 
        	      crc32 = updateCRC32((uint8*)&length, 4, crc32);
            }
     	      else if (!addingNewRecord && isBitUnSet(columnNulls0, j) && !vOlds[j]->isNull)
            {
               length = vOlds[j]->length;
     	         crc32 = updateCRC32((uint8*)&length, 4, crc32);
            }
         }
   }
   xmove4(&basbuf[i], &crc32); 
   basbuf[3] = k; // juliana@270_23: Corrected a RowIterator bug of an update changing an updated row to synced again.
 
   if (rowid > 0) // Now the record's attribute has to be updated.
   {
      int32 id = addingNewRecord? (rowid & ROW_ID_MASK) | ROW_ATTR_NEW : rowUpdated(rowid);
      xmove4(basbuf, &id);
   }

   // Writes the row.
   if (!(addingNewRecord? plainWrite(context, plainDB) : plainRewrite(context, plainDB, writePos)))
		return false;

   if (addingNewRecord)
      table->currentRowId = rowid + 1;

   // juliana@227_3: improved table files flush dealing.
	// juliana@202_23: Flushs the files to disk when row increment is the default.
   // juliana@270_25: corrected a possible lose of records in recover table when 10 is passed to LitebaseConnection.setRowInc().
   // Flushs .db and .dbo.
   if (!db->dontFlush)
      if ((db->cacheIsDirty && !flushCache(context, db)) || (dbo->cacheIsDirty && !flushCache(context, dbo))) 
         return false;

   return true;
}

/**
 * Writes a record from an array of values in a result set.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param values The record to be written in the result set table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool writeRSRecord(Context context, Table* table, SQLValue** values)
{
	TRACE("writeRSRecord")
   int32 n = table->columnCount;
	PlainDB* plainDB = &table->db;
	uint8* basbuf = plainDB->basbuf;
   int32* sizes = table->columnSizes;
   int8* types = table->columnTypes;
   uint8* nulls = table->columnNulls;
	uint16* offsets = table->columnOffsets;

   if (!plainAdd(context, plainDB)) // Adds a new row to the result set table.
      return false;

   xmemmove(&basbuf[offsets[n]], nulls, NUMBEROFBYTES(n)); // Writes the null values.

   // Writes the columns into a temporary buffer.
   while (--n >= 0) 
      if (!writeValue(context, plainDB, values[n], &basbuf[offsets[n]], types[n], sizes[n], values[n] != null, true, 
                                                                                            nulls[n >> 3] & (1 << (n & 7)), true))
         return false;
   return plainWrite(context, plainDB); // Finally, writes the row.
}

/**
 * Checks if a primary key constraint was violated
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param values The values inserted in the table.
 * @param recPos The position of vals record.
 * @param newRecord Indicates if it is an inserted or an updated record.
 * @param heap A heap to allocate values read from the table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If a member of the primary key is null.
 * @throws PrimaryKeyViolation If a there is a repeated primary key.
 */
bool checkPrimaryKey(Context context, Table* table, SQLValue** values, int32 recPos, bool newRecord, Heap heap)
{
	TRACE("checkPrimaryKey")
   Key tempKey;
   int32 primaryKeyCol = table->primaryKeyCol, // Simple primary key column.
         size,
         i;
   bool hasChanged = false;
   SQLValue oldValues[MAXIMUMS + 1]; 
   Index* index;
   uint8* columns;
   
   if (primaryKeyCol == -1)
   {
      columns = table->composedIndexes[table->composedPK]->columns; // Gets the columns of the index.
      i = size = table->numberComposedPKCols;
      index = table->composedIndexes[table->composedPK]->index;
   }
   else
   {
      columns = (uint8*)&primaryKeyCol; // Gets the column of the index.
      i = size = 1;
      index = table->columnIndexes[primaryKeyCol];
   }

   xmemzero(oldValues, size * sizeof(SQLValue));
   if (!newRecord) // An update.
   {
      PlainDB* plainDB = &table->db;
      int8* types = index->types;
      uint16* offsets = table->columnOffsets;
      uint8* basbuf = plainDB->basbuf;

      if (!plainRead(context, plainDB, recPos)) // Reads the table row.
         return false;
      while (--i >= 0)
      {
         // juliana@220_3 juliana@230_12
         // If it is updating a record, reads the old value and checks if a primary key value has changed. 
         if (!readValue(context, plainDB, &oldValues[i], offsets[columns[i]], types[i], basbuf, false, false, false, -1, heap)) 
            return false;

         // Tests if the primary key has not changed.
         if (values[i] && valueCompareTo(null, &oldValues[i], values[i], types[i], false, false, null))
            hasChanged = true;
 
         if (!values[i]) // Uses the old value. 
            values[i] = &oldValues[i];
      }
      i = size;
   }

   while (--i >= 0) // There can't be a null in a primary key.
      if (!values[i])
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_PK_CANT_BE_NULL), 0);
         return false;
      }

   if (hasChanged || newRecord) // Sees if the record does not violate the primary key.
   {
      tempKey.keys = oldValues;
      keySet(&tempKey, values, index, size);
      return indexGetValue(context, &tempKey, null);
   }
   return true;
}

/**
 * Verifies the null and default values of a statement.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param record The record to be inserted or updated.
 * @param statementType The type of the statement, which can be <code>SQLElement.STMT_INSERT</code> or <code>SQLElement.CMD_UPDATE</code>.
 * @param nValues The number of values being verified.
 * @return <code>false</code> if a null violation occurs; <code>true</code>, otherwise.
 * @throws DriverException If a primary key is or a <code>NOT NULL</code> field is is <code>null</code>.
 */
bool verifyNullValues(Context context, Table* table, SQLValue** record, int32 statementType, int32 nValues)
{
	TRACE("verifyNullValues")
   int32 i = table->primaryKeyCol;
   uint8* storeNulls = table->storeNulls;
   uint8* attrs = table->columnAttrs;

   if (statementType == CMD_INSERT) // Insert statement.
   {
      SQLValue** defaultValues = table->defaultValues;

      // The primary key can't be null.
      if ((i != NO_PRIMARY_KEY) && (isBitSet(storeNulls, i) || ((!record[i] || record[i]->isNull) && !defaultValues[i])))
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_PK_CANT_BE_NULL));
         return false;
      }

      i = table->columnCount;
      while (--i)
         if ((!record[i] || record[i]->isNull) && !defaultValues[i] && definedAsNotNull(attrs[i])) // A not null field can't have a null.
            {
               TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_FIELD_CANT_BE_NULL), table->columnNames[i]);
               return false;
            }
   }
   else // Update statement.
   {
      if ((i != NO_PRIMARY_KEY) && (isBitSet(storeNulls, i))) // The primary key can't be null.
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_PK_CANT_BE_NULL));
         return false;
      }
      i = nValues;
      while (--i >= 0)
         if (isBitSet(storeNulls, i) && definedAsNotNull(attrs[i])) // If it is to store a null but a null can't be stored.
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_FIELD_CANT_BE_NULL), table->columnNames[i]);
            return false;
         }
   }
   return true;
}

// juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than the type range.
/**
 * Converts the strings of the record into the real values, accordingly to the given table column types.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param record The record whose strings are to be transformed in their real types.
 * @param nValues The number of values in the record.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws SQLParseException If a conversion from string to a number or date/datetime fails.
 * @throws DriverException If a blob is passed in a statement that is not prepared.
 */
bool convertStringsToValues(Context context, Table* table, SQLValue** record, uint32 nValues)
{
	TRACE("convertStringsToValues")
   DoubleBuf buffer; // greatest type
   int8* columnTypes = table->columnTypes;
   bool error = false;
   int32 type,
         length;
   JCharP asChars;
   CharP strVal;
   SQLValue* value;

   while (--nValues) // 0 = rowid.
   {
      // If the column is storing a null, the string is considered to be null.
      asChars = (value = record[nValues])? value->asChars : null;

      // A blob can't be set in a normal statement (if the string is not null, it won't be a "?".
      if ((type = columnTypes[nValues]) == BLOB_TYPE && asChars && (asChars[0] != '?' || asChars[1]))
      {
         TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_BLOBS_PREPARED));
         return false;
      }

      // Ignores null values, blobs or unset parameters.
      if ((!asChars || (asChars[0] == '?' && !asChars[1])) || type == BLOB_TYPE)
         continue;

      if (type != CHARS_TYPE && type != CHARS_NOCASE_TYPE)
      {
         if ((length = value->length) > 39)
         {
            strVal = TC_JCharP2CharP(asChars, length);
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), strVal? strVal : "", "number");
            xfree(strVal);
            return false;
         }
         strVal = TC_JCharP2CharPBuf(asChars, length, buffer);
         
         switch (type)
         {
            case SHORT_TYPE:
               value->asShort = str2short(strVal, &error);
               break;

            case INT_TYPE:
               value->asInt = TC_str2int(strVal, &error);
               break;

            case LONG_TYPE:
               value->asLong = TC_str2long(strVal, &error);
               break;

            case FLOAT_TYPE:
               value->asFloat = str2float(strVal, &error);
               break;

            case DOUBLE_TYPE:
               value->asDouble = TC_str2double(strVal, &error);
               break;

            case DATE_TYPE:  // rnovais@567_2
            case DATETIME_TYPE:  // rnovais@567_2
               if (!testAndPrepareDateAndTime(context, value, strVal, type))
                  return false;
         }
         if (error)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), strVal, "number");
            return false;
         }
      }
   }
   return true;
}

// juliana@230_12: improved recover table to take .dbo data into consideration.
// juliana@253_8: now Litebase supports weak cryptography.
/** 
 * Updates the CRC32 value with the values of the given buffer. 
 * 
 * @param buffer The buffer.
 * @param length The number of bytes to be used to update the CRC code.
 * @param oldCRC The previous CRC32 value.
 * @return The CRC32 code updated to include the buffer data.
 */
int32 updateCRC32(uint8* buffer, int32 length, int32 oldCRC)
{
   TRACE("computeCRC32")      
   oldCRC = ~oldCRC;
   
   while (--length >= 0)
      oldCRC = crcTable[(oldCRC ^ *buffer++) & 0xff] ^ (((uint32)oldCRC) >> 8);
	return ~oldCRC;
}

/** 
 * Makes the table for a fast CRC. 
 */
void make_crc_table(void)
{
   TRACE("make_crc_table")
   int32 n = 256, 
		   c, 
			k;

	while (--n >= 0)
   {
      c = n;
      k = 8;
      while (--k >= 0)
      {
         if ((c & 1) != 0)
            c = 0xedb88320 ^ (((uint32)c) >> 1);
         else
            c = (((uint32)c) >> 1);
      }
      crcTable[n] = c;
   }
}

/**
 * Resets the auxiliary rowid.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool resetAuxRowId(Context context, Table* table) // rnovais@570_61
{
	TRACE("resetAuxRowId")
   if (table->auxRowId != ATTR_DEFAULT_AUX_ROWID)
   {
      XFile* db = &table->db.db;
		int32 pos = db->position;

      table->auxRowId = ATTR_DEFAULT_AUX_ROWID;
      if (!tableSaveMetaData(context, table, TSMD_ONLY_AUXROWID))
         return false;
      nfSetPos(db, pos);
      return true;
   }
   return true;
}

/**
 * Changes the state of a row to updated.
 *
 * @param id The rowid to have its atribute changed.
 * @return The rowid with its atribute changed to updated.
 */
int32 rowUpdated(int32 id)
{
	TRACE("rowUpdated")
   if ((id & ROW_ATTR_MASK) == ROW_ATTR_SYNCED)
      return (id & ROW_ID_MASK) | ROW_ATTR_UPDATED;				 
   return id;
}

/**
 * Frees a table when closing a Litebase connection.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool freeTableHT(Context context, Table* table)
{
   TRACE("freeTableHT")
   return freeTable(context, table, 0, true);
}

/**
 * Closes a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param isDelete Indicates if the table is to be deleted.
 * @param updatePos Indicates if the .db file is to be truncated or not.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool freeTable(Context context, Table* table, bool isDelete, bool updatePos)
{
	TRACE("freeTable")
   bool ret = true;
	
   if (table)
   {
      int32 n = table->columnCount,
            i;
      Index** columnIndexes = table->columnIndexes;
      ComposedIndex** composedIndexes = table->composedIndexes;
      Index* idx;
      TCObject obj;
      TCObjects* list = table->preparedStmts;
      TCObjects* preparedStmts = table->preparedStmts;
      TCHARP sourcePath = table->sourcePath;

      TC_htFree(&table->htName2index, null); // Frees the column names hash table.
      xfree(table->allRowsBitmap); // juliana@230_14

      if (table->columnIndexes) // Frees the simple indices in a normal table.
         while (--n >= 0)
         {
            if ((idx = columnIndexes[n]))
            {
               if (!indexClose(context, idx))
                  ret = false;
               columnIndexes[n] = null; 
            }
         }

      if (*table->name && (i = table->numberComposedIndexes)) // Frees the composed indices in a normal table.
      {
         while (--i >= 0)
         {
            if (composedIndexes[i] && (idx = composedIndexes[i]->index))
            {
               if (!indexClose(context, idx))
                  ret = false;
               composedIndexes[i]->index = null;
            }
         }
      }

      if (isDelete) // Frees or removes the table.
         ret &= plainRemove(context, &table->db, sourcePath);
      else
         ret &= plainClose(context, &table->db, updatePos);

      // juliana@221_1: solved a problem that could reduce the free memory too much if many prepared statements were created and collected many 
      // times.
		if (preparedStmts)
      {
         Hashtable* htPS;
         TCObject sqlObj;
         do
		   {
            // juliana@226_16: prepared statement is now a singleton.
			   if ((obj = list->value))
            {
               htPS = getLitebaseHtPS(OBJ_PreparedStatementDriver(obj));
				   sqlObj = OBJ_PreparedStatementSqlExpression(obj);
               TC_htRemove(htPS, TC_JCharPHashCode(String_charsStart(sqlObj), String_charsLen(sqlObj)));
               freePreparedStatement(0, obj);
            }
			   list = TC_TCObjectsRemove(list, obj);
			   list = list->next;
		   } while (preparedStmts != list);
      }
      heapDestroy(table->heap);
   }
   return ret;
}

/**
 * Gets the value of a column of the underlying table used by the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set whose table will be read.
 * @param column The number of the column from which the value will be fetched.
 * @param value The structure where the value will be stored.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool getTableColValue(Context context, ResultSet* resultSet, int32 column, SQLValue* value)
{
   TRACE("getTableColValue")
   Table* table = resultSet->table;

   // juliana@230_12
   return readValue(context, &table->db, value, table->columnOffsets[column], table->columnTypes[column], table->db.basbuf, !*table->name, 
                                                isBitSet(table->columnNulls, column), true, table->columnSizes[column], null);
}

/**
 * Indicates if a table already exists on disk or not.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param name The table name.
 * @return <code>true</code> if the table already exists; <code>false</code>, othewise.
 * @throws AlreadyCreatedException If the table is already created.
 * @throws DriverException If the path is too long.
 */
bool tableExistsByName(Context context, TCObject driver, CharP name)
{
   TRACE("tableExistsByName")
   TCHAR fullName[MAX_PATHNAME];
   char bufName[DBNAME_SIZE];
   TCHARP sourcePath = getLitebaseSourcePath(driver);

   // juliana@252_3: corrected a possible crash if the path had more than 255 characteres.
   if (xstrlen(name) + tcslen(sourcePath) + 10 > MAX_PATHNAME)
   {     
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INVALID_PATH), sourcePath); 
      return false; 
   }
   
   // Verifies if the table exists checking if the .db exists. The name must be already on lower case.
   if (!getDiskTableName(context, OBJ_LitebaseAppCrid(driver), name, bufName))
      return true;
   getFullFileName(bufName, sourcePath, fullName);
   tcscat(fullName, TEXT(DB_EXT));

   if (lbfileExists(fullName))
   {
      // The .db file already exists. So, the table is considered to exists.
      TC_throwExceptionNamed(context, "litebase.AlreadyCreatedException", getMessage(ERR_TABLE_ALREADY_CREATED), name);
      return true;
   }
   return false;
}

/** 
 * Gets the table name on disk.
 *
 * @param context The thread context where the function is being executed.
 * @param crid The application id for the connection.
 * @param name The table name.
 * @param buffer The buffer for the table name on disk.
 * @return <code>false</code> if table name is too big; <code>true</code>, otherwise. 
 * @throws DriverException If the table name is too big.
 */
bool getDiskTableName(Context context, int32 crid, CharP name, CharP buffer)
{
	TRACE("getDiskTableName")
   CharP to = &buffer[5];
   
   if (xstrlen(name) > MAX_TABLE_NAME_LENGTH) // The table name can't be too big because of palm os.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      return false;
   }

   // Gets the application id as a string.
   TC_int2CRID(crid, buffer);
   buffer[4] = '-';
      
   while (*name) // guich@553_9: converts to lowercase.
      *to++ = TC_toLower(*name++);
   *to = 0;
   return true;
}

/**
 * Given a table name as an object, returns its table.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param name The table name as a string object.
 * @return <code>null<code> if an error occurs; a table handle, otherwise.
 * @throws DriverException If the table name is too big.
 */
Table* getTableFromName(Context context, TCObject driver, TCObject name)
{
	TRACE("getTableFromName")
   char tableName[DBNAME_SIZE];
   int32 length = String_charsLen(name);

	if (length > MAX_TABLE_NAME_LENGTH) // The table name can't be too big because of palm os.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      return null;
   }

   TC_JCharP2CharPBuf(String_charsStart(name), length, tableName);
   TC_CharPToLower(tableName);
   return getTable(context, driver, tableName);
}

/**
 * Given a table name as an uint8 string, returns its table.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param tableName The table name as an uint8 string.
 * @return <code>null<code> if an error occurs; a table handle, otherwise.
 * @throws DriverException If the table name is too big.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated.
 */
Table* getTable(Context context, TCObject driver, CharP tableName)
{
	TRACE("getTable")
   char name[DBNAME_SIZE];
   Table* table = null;
	Hashtable* htTables = getLitebaseHtTables(driver);
   int32 length = xstrlen(tableName),
         appCrid = OBJ_LitebaseAppCrid(driver),
         hashCode;

   if (length > MAX_TABLE_NAME_LENGTH) // The table name can't be too big because of palm os.
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_MAX_TABLE_NAME_LENGTH));
      return null;
   }

   TC_CharPToLower(tableName); // The hash code must be in lower case so that the program is more efficient.

   // Opens a not already opened table.
   if (!(table = (Table*)TC_htGetPtr(htTables, hashCode = TC_hashCode(tableName))))
   {
      // Gets the table name.
      if (getDiskTableName(context, appCrid, tableName, name))
      {
         Heap heap = heapCreate();

         IF_HEAP_ERROR(heap)
         {
            heapDestroy(heap);
            TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
            return null;
         }
         heap->greedyAlloc = true;

         // Opens it. It must have been already created.
         // juliana@220_5
         // juliana@253_8: now Litebase supports weak cryptography.
         if ((table = tableCreate(context, name, getLitebaseSourcePath(driver), false, OBJ_LitebaseIsAscii(driver), OBJ_LitebaseUseCrypto(driver), 
                                                                                       getLitebaseNodes(driver), true, heap)) && table->db.db.size)
         {
            if (!TC_htPutPtr(htTables, hashCode, table)) // Puts the table hash code in the hash table of opened tables.
            {
               freeTable(context, table, false, false);
               TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
               return null;
            }
         }
         else
            return null;
      }
   }
   return table;
}

/**
 * Reads a string from a buffer.
 *
 * @param buffer The buffer being read.
 * @param string The string returned.
 * @param heap The heap where the string will be allocated.
 * @return The buffer remaning after reading the string.
 */
uint8* readString(uint8* buffer, CharP* string, Heap heap)
{
   TRACE("readString")
   uint32 length = 0;

   xmove2(&length, buffer);
   xmemmove((*string = (CharP)TC_heapAlloc(heap, length + 1)), buffer += 2, length);
   return buffer + length;
}

/**
 * Reads a string array from a buffer.
 *
 * @param buffer The buffer being read.
 * @param strings The string array returned.
 * @param count The column count.
 * @param heap The heap where the string array will be allocated.
 * @return The buffer remaning after reading the string array.
 */
uint8* readStringArray(uint8* buffer, CharP** strings, int32 count, Heap heap)
{
	TRACE("readStringArray")
   int32 i = -1;

   buffer += 2;
   *strings = (CharP*)TC_heapAlloc(heap, count * TSIZE);
   while (++i < count)
      buffer = readString(buffer, &((*strings)[i]), heap);
   return buffer;
}

/**
 * Writes a string to a buffer.
 *
 * @param buffer The buffer being written.
 * @param string The string to be written.
 * @return The buffer remaning after writing the string.
 */
uint8* writeString(uint8* buffer, CharP string)
{
	TRACE("writeString")
   uint32 length = xstrlen(string);
   
   xmove2(buffer, &length);
   xmemmove(buffer + 2, string, length);
   return buffer + length + 2;
}

/**
 * Writes a string array to a buffer.
 *
 * @param buffer The buffer being written.
 * @param strings The string array to be written.
 * @param count The column count.
 * @return The buffer remaning after writing the string array.
 */
uint8* writeStringArray(uint8* buffer, CharP* strings, int32 count)
{
	TRACE("writeStringArray")
   int32 i = -1;

   buffer += 2;
   while (++i < count)
      buffer = writeString(buffer, strings[i]);
   return buffer;
}

/**
 * Writes a unicode string to a buffer.
 *
 * @param buffer The buffer being written.
 * @param string The string to be written.
 * @param lengtg The string length.
 * @return The buffer remaning after writing the string.
 */
uint8* writeString16(uint8* buffer, JCharP string, int32 length)
{
	TRACE("writeString16")
   xmove2(buffer, &length);
   xmemmove(buffer + 2, string, length <<= 1);
   return buffer + length + 2;
}

/**
 * Changes a table to the modified state whenever it is modified.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table to be set as modified.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setModified(Context context, Table* table)
{
   TRACE("setModified")

   if (!table->isModified)
   {
      PlainDB* plainDB = &table->db;
      XFile* dbFile = &plainDB->db;
      int32 isAscii;
      
      isAscii = (plainDB->isAscii? IS_ASCII : 0);
	   nfSetPos(dbFile, 6);
	   if (nfWriteBytes(context, dbFile, (uint8*)&isAscii, 1) && flushCache(context, dbFile)) // Flushs .db.  
         table->isModified = true; 
	   else
	      return false;
   }
   return true;
}


int32 randBetween(int32 low, int32 high)
{
   TRACE("randBetween")

   if (low == high) 
      high++;
   return low + (((uint32)rand()) % (high - low + 1));
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if rowUpdated() works correctly. 
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(rowUpdated)
{
   UNUSED(currentContext)
   ASSERT2_EQUALS(I32, 2147483648U, rowUpdated(0));
   ASSERT2_EQUALS(I32, 2147483658U, rowUpdated(10));
   ASSERT2_EQUALS(I32, 2147483748U, rowUpdated(100));
   ASSERT2_EQUALS(I32, 2147484648U, rowUpdated(1000));
   ASSERT2_EQUALS(I32, 2147493648U, rowUpdated(10000));
   ASSERT2_EQUALS(I32, 2147583648U, rowUpdated(100000));
   ASSERT2_EQUALS(I32, 2148483648U, rowUpdated(1000000));
   ASSERT2_EQUALS(I32, 2157483648U, rowUpdated(10000000));
   ASSERT2_EQUALS(I32, 2247483648U, rowUpdated(100000000));
   ASSERT2_EQUALS(I32, 3147483648U, rowUpdated(1000000000));
   
finish: ;
}

#endif
