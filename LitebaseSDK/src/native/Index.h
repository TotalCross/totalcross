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

// juliana@noidr_1: removed .idr files from all indices and changed its format. 
/**
 * Declares functions to deal a B-Tree header.
 */

#ifndef LITEBASE_INDEX_H
#define LITEBASE_INDEX_H

#include "Litebase.h"

/**
 * Creates a composed index.
 *
 * @param id The index id.
 * @param columns The columns of this index.
 * @param numberColumns The number of columns of this index.
 * @param heap The heap to allocate the compoded index structure.
 * @return The composed index.
 */
ComposedIndex* createComposedIndex(int32 id, uint8* columns, int32 numberColumns, Heap heap);

/**
 * Constructs an index structure.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table of the index.
 * @param keyTypes The types of the columns of the index.
 * @param colSizes The column sizes.
 * @param name The name of the index table.
 * @param numberColumns The number of columns of the index.
 * @param exist Indicates that the index files already exist. 
 * @param heap A heap to allocate the index structure.
 * @return The index created or <code>null</code> if an error occurs.
 * @throws DriverException If is not possible to create the index files.
 */
Index* createIndex(Context context, Table* table, int8* keyTypes, int32* colSizes, CharP name, int32 numberColumns, bool exist, Heap heap);

/**
 * Creates an index.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table name whose index is to be created.
 * @param columnHashes The hashes of the index columns.
 * @param isPKCreation Indicates if the index to be created is the primary key.
 * @param indexCount The column numbers of the index.
 * @param composedPKCols The columns of the composed primary key.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 * @throws DriverException If a column for the index does not exist or is of type blob.
 * @throws SQLParseException If a column for the index is of type blob.
 */
bool driverCreateIndex(Context context, Table* table, int32* columnHashes, bool isPKCreation, int32 indexCount, uint8* composedPKCols);

/**
 * Removes a value from the index.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key to be removed.
 * @param record The record being removed.
 * @return <code>true</code> If the value was removed; <code>false</code> otherwise.
 * @throws DriverException If its not possible to find the key record to delete or the index is corrupted.
 */
bool indexRemoveValue(Context context, Key* key, int32 record);

/**
 * Loads a node.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index.
 * @param idx The index of the value to be loaded.
 * @return The node or <code>null</code> in case of an error.
 * @throws DriverException If the index is corrupted.
 */
Node* indexLoadNode(Context context, Index* index, int32 idx);

/**
 * Finds the given key and make the monkey climb on the values.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key to be found.
 * @param markBits The rows which will be returned to the result set.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 * @throws DriverException If the index is corrupted.
 */
bool indexGetValue(Context context, Key* key, MarkBits* markBits);

/**
 * Climbs on the nodes that are greater or equal than the current one.
 *
 * @param context The thread context where the function is being executed.
 * @param node The node to be compared with.
 * @param start The first key of the node to be searched.
 * @param markBits The rows which will be returned to the result set.
 * @param stop Indicates when the climb process can be finished.
 * @return If it has to stop the climbing process or not, or <code>false</code> if an error occured.
 */
bool indexClimbGreaterOrEqual(Context context, Node* node, int32 start, MarkBits* markBits, bool* stop);

/**
 * Starts from the root to find the left key, then climbs from it until the end.
 *
 * @param context The thread context where the function is being executed.
 * @param left The left key.
 * @param markBits The rows which will be returned to the result set.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 * @throws DriverException If the index is corrupted.
 * @throws OutOfMemoryError If there is not enougth memory allocate memory. 
 */
bool indexGetGreaterOrEqual(Context context, Key* left, MarkBits* markBits);

/**
 * Splits the overflown node of this B-Tree. The stack ancestors contains all ancestors of the node, together with the known insertion position in 
 * each of these ancestors.
 *
 * @param context The thread context where the function is being executed.
 * @param curr The current node.
 * @param count The number of elements in the ancestors array.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 */
bool indexSplitNode(Context context, Node* curr, int32 count);

 /**
 * Removes the index files.
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index to be removed.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 */
bool indexRemove(Context context, Index* index);

/**
 * Closes the index files.
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index to be removed.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 */
bool indexClose(Context context, Index* index);

/**
 * Empties the index files, since the rows were deleted.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index to be erased.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 * @throws DriverException If it is not possible to truncate the index files.
 */
bool indexDeleteAllRows(Context context, Index* index);

/** 
 * Delays the write to disk, caching them at memory. 
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index.
 * @param delayed Indicates if the writing process is to be done later or not.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 */
bool indexSetWriteDelayed(Context context, Index* index, bool delayed);

/**
 * Adds a key to an index.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index where the key is going to be inserted.
 * @param values The key to be inserted.
 * @param record The record of the key in the table.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 * @throws DriverException If the index is corrupted.
 */
bool indexAddKey(Context context, Index* index, SQLValue** values, int32 record);

/**
 * Renames the index files.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index which will be renamed.
 * @param newName The new name for the index.
 * @return <code>false</code> if an error occured; <code>true</code>, otherwise.
 */
bool indexRename(Context context, Index* index, CharP newName);

/**
 * Finds the minimum value of an index in a range.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index where to find the minimum value.
 * @param sqlValue The minimum value inside the given range to be returned.
 * @param bitMap The table bitmap which indicates which rows will be in the result set. 
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool findMinValue(Context context, Index* index, SQLValue* sqlValue, IntVector* bitMap);

/**
 * Finds the maximum value of an index in a range.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index where to find the minimum value.
 * @param bitMap The table bitmap which indicates which rows will be in the result set.
 * @param sqlValue The maximum value inside the given range to be returned.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool findMaxValue(Context context, Index* index, SQLValue* sqlValue, IntVector* bitMap);

/**
 * Loads a string from the table if needed.
 *
 * @param context The thread context where the function is being executed.
 * @param index The index where to find the minimum value. 
 * @param sqlValue The record structure which will hold (holds) the string.
 * @return <code>false</false> if an error occurs; <code>true</code>, otherwise or no record was found.
 */
bool loadStringForMaxMin(Context context, Index* index, SQLValue* sqlValue);

/**
 * Returns a node already loaded or loads it if there is empty space in the cache node to avoid loading already loaded nodes.
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index where a node is going to be fetched.
 * @return The loaded node, a new cache node with the requested node loaded, a first level node, or <code>null</code> if it is not 
 * already loaded and its cache is full.
 */
Node* getLoadedNode(Context context, Index* index, int32 idx);

/**
 * Sorts the records of a table into a temporary table using an index in the ascending order.
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index being used to sort the query results.
 * @param bitMap The table bitmap which indicates which rows will be in the result set.
 * @param tempTable The temporary table for the result set.
 * @param record A record for writing in the temporary table.
 * @param columnIndexes Has the indices of the tables for each resulting column.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the index is corrupted.
 */
bool sortRecordsAsc(Context context, Index* index, IntVector* bitMap, Table* tempTable, SQLValue** record, int16* columnIndexes, Heap heap);

/**
 * Sorts the records of a table into a temporary table using an index in the descending order.
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index being used to sort the query results.
 * @param bitMap The table bitmap which indicates which rows will be in the result set.
 * @param tempTable The temporary table for the result set.
 * @param record A record for writing in the temporary table.
 * @param columnIndexes Has the indices of the tables for each resulting column.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the index is corrupted.
 */
bool sortRecordsDesc(Context context, Index* index, IntVector* bitMap, Table* tempTable, SQLValue** record, int16* columnIndexes, Heap heap); 

/**
 * Writes all the records with a specific key in the temporary table that satisfy the query where clause. 
 * 
 * @param context The thread context where the function is being executed.
 * @param index The index being used to sort the query results.
 * @param valRec The record index.
 * @param bitMap The table bitmap which indicates which rows will be in the result set.
 * @param tempTable The temporary table for the result set.
 * @param record A record for writing in the temporary table.
 * @param columnIndexes Has the indices of the tables for each resulting column.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool writeKey(Context context, Index* index, int32 valRec, IntVector* bitMap, Table* tempTable, SQLValue** record, int16* columnIndexes);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if <code>createComposedIndex()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_createComposedIndex(TestSuite* testSuite, Context currentContext);

#endif

#endif
