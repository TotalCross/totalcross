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
 * This has the function declarations for a database in a plain binary file. The data and the metadata (header) is written in one file (.db). The 
 * strings and the blobs are written in the .dbo file. The current number of records inside the database is discovered only when the database is open 
 * by getting its size and discounting the header size. This has a double advantage: it is not necessary to waste space storing the current record 
 * count, and it is not needed to save the record count at each insertion. 
 * 
 * This also has function declarations for a temporary database for <code>ResultSet</code> tables.
 */

#ifndef LITEBASE_PLAINDB_H
#define LITEBASE_PLAINDB_H

#include "Litebase.h"

/**
 * Creates a new <code>PlainDB</code>, loading or creating the table with the given name or creating a temporary table.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB Receives the new <code>PlainDB</code> or <code>null</code> if an error occurs.
 * @param name The name of the table.
 * @param create Defines if the file will be created if it doesn't exist.
 * @param useCrypto Indicates if the table uses cryptography.
 * @param sourcePath The path where the table is to be open or created.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool createPlainDB(Context context, PlainDB* plainDB, CharP name, bool create, bool useCrypto, TCHARP sourcePath);

/**
 * Sets the size of a row.
 * 
 * @param plainDB The <code>PlainDB</code>.
 * @param newRowSize The new row size.
 * @param buffer A buffer for the <code>PlainDB</code>.
 */
void plainSetRowSize(PlainDB* plainDB, int32 newRowSize, uint8* buffer);

/**
 * Adds a new record. The file pointer is positioned in the record's beginning so that the data can be written. Usually the record is first added, 
 * then the contents are written.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainAdd(Context context, PlainDB* plainDB);

/**
 * Writes the data of the .db file buffer into the current file position.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainWrite(Context context, PlainDB* plainDB);

/**
 * Reads a row at the given position into the .db file buffer.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param record The record to be read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRead(Context context, PlainDB* plainDB, int32 record);

/**
 * Rewrites a row at the given position.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param record The .db file record to be read.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRewrite(Context context, PlainDB* plainDB, int32 record);

/**
 * Renames the files to the new given name.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param newName The new table name.
 * @param sourcePath The files path.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRename(Context context, PlainDB* plainDB, CharP newName, TCHARP sourcePath);

/**
 * Writes the given metadata to the header of the .db file.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param buffer The data to be written.
 * @param length The data length.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainWriteMetaData(Context context, PlainDB* plainDB, uint8* buffer, int32 length);

/**
 * Reads the user metadata from the .db file header.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param buffer An static buffer for reading the metadata.
 * @return The metadata.
 * @throws OutOfMemoryError If there is not enougth memory allocate memory. 
 * @return <code>null</code> if an error occurs; a metadata buffer, otherwise.
 */
uint8* plainReadMetaData(Context context, PlainDB* plainDB, uint8* buffer);

/**
 * Closes the table files.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param updatePos Indicates if <code>finalPos</code> must be re-calculated to shrink the file. 
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainClose(Context context, PlainDB* plainDB, bool updatePos);

/**
 * Removes the table files.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param sourcePath The files path.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainRemove(Context context, PlainDB* plainDB, TCHARP sourcePath);

/**
 * Sets the .db file pointer to point to a record position.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param record The record position.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainSetPos(Context context, PlainDB* plainDB, int32 record);

/**
 * Compresses the memory file buffers at the current position. This is necessary so that the memory tables do not have unused space after computing 
 * the select.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool plainShrinkToSize(Context context, PlainDB* plainDB); 

/**
 * Reads a value from a PlainDB.
 *
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param value The value to be read.
 * @param offset The offset of the value in its row.
 * @param colType The type of the value.
 * @param buffer The buffer where the row data is stored.
 * @param isTemporary Indicates if this is a result set table or the value or the integer of a rowid index is to be loaded.
 * @param isNull Indicates if the value is null.
 * @param isTempBlob Indicates if the blob is being read for a temporary table.
 * @param size The column size of the string being read.
 * @param heap A heap to allocate temporary strings.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 */
bool readValue(Context context, PlainDB* plainDB, SQLValue* value, int32 offset, int32 colType, uint8* buffer, bool isTemporary, bool isNull, 
                                                                                                bool isTempBlob, int32 size, Heap heap);

/**
 * Writes a value to a table column.
 * 
 * @param context The thread context where the function is being executed.
 * @param plainDB The <code>PlainDB</code>.
 * @param value The value to be written.
 * @param buffer The buffer where the value is stored before going to the table.
 * @param colType The type of the column.
 * @param colSize The column size of the value.
 * @param isValueOk Indicates if the value is to be written.
 * @param addingNewRecord Indicates if it is an update or an insert.
 * @param isNull Indicates that the value being inserted is a null.
 * @paran isTempBlob Indicates if a temporary table is being used.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 */
bool writeValue(Context context, PlainDB* plainDB, SQLValue* value, uint8* buffer, int32 colType, int32 colSize, bool isValueOk, bool addingNewRecord, bool isNull, bool isTempBlob);

/**
 * Tests if a record of a table is not deleted.
 *
 * @param buffer The buffer where the table record is stored.
 * @return <code>false</code> if the record is deleted; <code>true</code> otherwise.
 */
bool recordNotDeleted(uint8* buffer);

/**
 * Loads a string from a table taking the storage format into consideration.
 *
 * @param context The thread context where the function is being executed. 
 * @param plainDB The <code>PlainDB</code>.
 * @param string The buffer where the string will be stored.
 * @param length The length of the string to be loaded.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 */
bool loadString(Context context, PlainDB* plainDB, JCharP string, int32 length);

#endif
