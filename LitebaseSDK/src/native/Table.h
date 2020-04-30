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
 * Declares functions to manipulate table structures.
 */

#ifndef LITEBASE_TABLE_H
#define LITEBASE_TABLE_H

#include "Litebase.h"

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
int32 verifyIfIndexAlreadyExists(Context context, Table* table, uint8* columnNumbers, int32 indexCount);

/**
 * Drops an index.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose index is to dropped.
 * @param column The column of the index dropped.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the column does not have an index. 
 */
bool driverDropIndex(Context context, Table* table, int32 column);

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
bool driverDropComposedIndex(Context context, Table* table, uint8* columns, int32 size, int32 indexId, bool saveMD);

/**
 * Deletes all indices of a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose indices are to dropped.
 * @return The number of indices deleted.
 */
int32 deleteAllIndexes(Context context, Table* table);

/**
 * Computes the column offsets of the table columns.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated.
 */
bool computeColumnOffsets(Context context, Table* table);

/**
 * Loads the meta data of a table,
 *
 * @param context The thread context where the function is being executed.
 * @param table The table being loaded.
 * @param throwException Indicates that a <code>TableNotClosedException</code> should be thrown.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the table is in an incompatible format.
 * @throws TableNotClosedException If the table was not properly close when opened last time.
 */
bool tableLoadMetaData(Context context, Table* table, bool throwException); 

/**
 * Saves the table meta data
 *
 * @param context The thread context where the function is being executed.
 * @param table The table whose meta data is being saved.
 * @param saveType The kind of save. It can be one out of <code><B>TSMD_ONLY_DELETEDROWSCOUNT</B></code>, 
 * <code><B>TSMD_ONLY_PRIMARYKEYCOL</B></code>, <code><B>TSMD_EVERYTHING</B></code>, or <code><B>TSMD_ONLY_AUXROWID</B></code>.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated
 */
bool tableSaveMetaData(Context context, Table* table, int32 saveType);

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
                      SQLValue** defaultValues, int32 primaryKeyCol, int32 composedPK, int32 columnCount, int32 composedPKColsSize);

/**
 * Gets the table standart metadata size to save a table.
 *
 * @param table The table to be saved.
 * @param saveType The save type of the table, which increases or decreases its size.
 * @return The metadata size.
 */
int32 getTSMDSize(Table* table, int32 saveType);

/**
 * Gets the total size of the table column names.
 *
 * @param names The names of the columns.
 * @param count The number of colums.
 * @return the total size for the column names.
 */
int32 getStringsTotalSize(CharP* names, int32 count);

/**
 * Gets the total size needed to store the table default values.
 *
 * @param table The table.
 * @return the total size for the default values.
 */
int32 computeDefaultValuesMetadataSize(Table* table);

/**
 * Gets the total size needed to store the table composed indices.
 *
 * @param table The table.
 * @return the total size for the composed indices.
 */
int32 computeComposedIndicesTotalSize(Table* table);

/**
 * Reorder the values of a statement to match the table definition.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table of the statement.
 * @param fields The fields of the statement.
 * @param record The record of values of the statement.
 * @param storeNulls Indicates which values have a null.
 * @param nValues The number of values.
 * @param paramIndexes The indices of the parameters, if any, in the record.
 * @return <code>false</code> if there is an invalid field name; <code>true</code>, otherwise.
 * @throws DriverException if there is an invalid field name.
 */
bool reorder(Context context, Table* table, CharP* fields, SQLValue** record, uint8* storeNulls, uint8* nValues, uint8* paramIndexes);

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
bool sortTable(Context context, Table* table, SQLColumnListClause* groupByClause, SQLColumnListClause* orderByClause);

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
                                                                uint8* bufAux, int32 first, int32 last, int32 fieldsCount, Heap heap);

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
int32 compareSortRecords(int32 recSize, SQLValue** vals1, SQLValue** vals2, int8* types); 

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
void sortRecords(SQLValue*** sortValues, int32 recSize, int8* types, int32 first, int32 last, int32* vector); 

/** 
 * Does a radix sort on the given SQLValue array. Only integral types are allowed (SHORT, INT, LONG). This is faster than quicksort. Also used to 
 * build the indices from scratch.
 * 
 * @param source The values to be sorted. Only simple records for simple indices can be used.
 * @param length The number of values to be sorted.
 * @param type The type of the elements.
 * @param temp A temporary array for the sort.
 */
void radixSort(SQLValue*** source, int32 length, int32 type, SQLValue*** temp);

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
int64 radixPass(int32 start, SQLValue*** source, SQLValue*** dest, int32* count, int32* index, int32 type, int32 length);

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
                                                                                                              bool throwException, Heap heap); 

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
       SQLValue** defaultValues, int32 primaryKeyCol, int32 composedPK, uint8* composedPKCols, int32 composedPKColsSize, int32 count, Heap heap); 
                              
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
bool renameTable(Context context, TCObject driver, Table* table, CharP newTableName); // rnovais@566_10

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
bool renameTableColumn(Context context, Table* table, CharP oldColumn, CharP newColumn, bool reuseSpace); // rnovais@566_17

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
bool tableReIndex(Context context, Table* table, int32 column, bool isPKCreation, ComposedIndex* composedIndex);

// juliana@noidr_1: removed .idr files from all indices and changed its format. 
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
bool indexCreateIndex(Context context, Table* table, CharP fullTableName, int32 columnIndex, int32* columnSizes, int8* columnTypes, bool exist, 
                                                                                                                                    Heap heap);

// juliana@noidr_1: removed .idr files from all indices and changed its format. 
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
                                                             int32 numberColumns, int32 newIndexNumber, bool increaseArray, bool exist, Heap heap);

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
                                                                  int32 fieldsCount, bool isTempBlob, Heap heap, StringArray** stringArray);

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
bool writeRecord(Context context, Table* table, SQLValue** values, int32 recPos, Heap heap);

/**
 * Writes a record from an array of values in a result set.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param values The record to be written in the result set table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool writeRSRecord(Context context, Table* table, SQLValue** values);

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
bool checkPrimaryKey(Context context, Table* table, SQLValue** values, int32 recPos, bool newRecord, Heap heap);

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
bool verifyNullValues(Context context, Table* table, SQLValue** record, int32 statementType, int32 nValues);

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
bool convertStringsToValues(Context context, Table* table, SQLValue** record, uint32 nValues);

/** 
 * Updates the CRC32 value with the values of the given buffer. 
 * 
 * @param buffer The buffer.
 * @param length The number of bytes to be used to update the CRC code.
 * @param oldCRC The previous CRC32 value.
 * @return The CRC32 code updated to include the buffer data.
 */
int32 updateCRC32(uint8* buffer, int32 length, int32 oldCRC);

/** 
 * Makes the table for a fast CRC. 
 */
void make_crc_table(void);

/**
 * Resets the auxiliary rowid.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool resetAuxRowId(Context context, Table* table);

/**
 * Changes the state of a row to updated.
 *
 * @param id The rowid to have its atribute changed.
 * @return The rowid with its atribute changed to updated.
 */
int32 rowUpdated(int32 id);

/**
 * Frees a table when closing a Litebase connection.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool freeTableHT(Context context, Table* table);

/**
 * Closes a table.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table.
 * @param isDelete Indicates if the table is to be deleted.
 * @param updatePos Indicates if the .db file is to be truncated or not.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool freeTable(Context context, Table* table, bool isDelete, bool updatePos);

/**
 * Gets the value of a column of the underlying table used by the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set whose table will be read.
 * @param column The number of the column from which the value will be fetched.
 * @param value The structure where the value will be stored.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool getTableColValue(Context context, ResultSet* resultSet, int32 column, SQLValue* value);

/**
 * Indicates if a table already exists on disk or not.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param name The table name.
 * @return <code>true</code> if the table already exists or the table name is too big; <code>false</code>, othewise.
 * @throws AlreadyCreatedException If the table is already created.
 * @throws DriverException If the path is too long.
 */
bool tableExistsByName(Context context, TCObject driver, CharP name);

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
bool getDiskTableName(Context context, int32 crid, CharP name, CharP buffer); 

/**
 * Given a table name as an object, returns its table.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param name The table name as a string object.
 * @return <code>null<code> if an error occurs; a table handle, otherwise.
 * @throws DriverException If the table name is too big.
 */
Table* getTableFromName(Context context, TCObject driver, TCObject name);

/**
 * Given a table name as an uint8 string, returns its table.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param tableName The table name as an uint8 string.
 * @return <code>null<code> if an error occurs; a table handle, otherwise.
 * @throws DriverException If the table name is too big.
 * @throws OutOfMemoryError If there is not enougth memory to be allocated
 */
Table* getTable(Context context, TCObject driver, CharP tableName);

/**
 * Reads a string from a buffer.
 *
 * @param buffer The buffer being read.
 * @param string The string returned.
 * @param heap The heap where the string will be allocated.
 * @return The buffer remaning after reading the string.
 */
uint8* readString(uint8* buffer, CharP* string, Heap heap); 

/**
 * Reads a string array from a buffer.
 *
 * @param buffer The buffer being read.
 * @param strings The string array returned.
 * @param count The column count.
 * @param heap The heap where the string array will be allocated.
 * @return The buffer remaning after reading the string array.
 */
uint8* readStringArray(uint8* buffer, CharP** strings, int32 count, Heap heap);

/**
 * Writes a string to a buffer.
 *
 * @param buffer The buffer being written.
 * @param string The string to be written.
 * @return The buffer remaning after writing the string.
 */
uint8* writeString(uint8* buffer, CharP string);

/**
 * Writes a string array to a buffer.
 *
 * @param buffer The buffer being written.
 * @param strings The string array to be written.
 * @param count The column count.
 * @return The buffer remaning after writing the string array.
 */
uint8* writeStringArray(uint8* buffer, CharP* strings, int32 count);

/**
 * Writes a unicode string to a buffer.
 *
 * @param buffer The buffer being written.
 * @param string The string to be written.
 * @param lengtg The string length.
 * @return The buffer remaning after writing the string.
 */
uint8* writeString16(uint8* buffer, JCharP string, int32 length);

/**
 * Changes a table to the modified state whenever it is modified.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The table to be set as modified.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setModified(Context context, Table* table);

/**
 * Rands between two numbers.
 *
 * @param low The first and smaller number.
 * @param high The second and greater number.
 * @return a pseudo-random number between them.
 */
int32 randBetween(int32 low, int32 high);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if rowUpdated() works correctly. 
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_rowUpdated(TestSuite* testSuite, Context currentContext);

#endif

#endif
