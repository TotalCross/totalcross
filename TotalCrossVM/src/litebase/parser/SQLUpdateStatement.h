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
 * Declares the functions to initialize, set, and process an update statement.
 */

#ifndef LITEBASE_SQLUPDATESTATEMENT_H
#define LITEBASE_SQLUPDATESTATEMENT_H

#include "Litebase.h"

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
SQLUpdateStatement* initSQLUpdateStatement(Context context, TCObject driver, LitebaseParser* parse, bool isPrepared);

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
bool setNumericParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 type);

/* 
 * Sets the value of a string or blob parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param length The length of the string or blob.
 * @param isStr Indicates if the parameter is a string or a blob.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws SQLParserException If a <code>null</code> is used as a parameter of a where clause.
 * @thows DriverException If the parameter type is incompatible with the column type.
 */
bool setStrBlobParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 length, bool isStr);

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
bool setNullUpd(Context context, SQLUpdateStatement* updateStmt, int32 index);

/**
 * Throws an exception if the index to set a parameter in the update prepared statement is invalid.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL insert statement.
 * @param index The index of the parameter.
 * @throws IllegalArgumentException If the parameter index is invalid.
 */
bool checkUpdateIndex(Context context, SQLUpdateStatement* updateStmt, int32 index);

/**
 * Set a record position for an update prepared statement.
 *
 * @param updateStmt A SQL insert statement.
 * @param index The index of the parameter.
 */
void setUpdateRecord(SQLUpdateStatement* updateStmt, int32 index);

/**
 * Clears all parameter values of a prepared statement update.
 *
 * @param updateStmt A SQL update statement.
 */
void clearParamValuesUpd(SQLUpdateStatement* updateStmt);

/**
 * Checks if all parameters values are defined.
 *
 * @param updateStmt A SQL update statement.
 * @return <code>true</code>, if all parameters values are defined; <code>false</code> otherwise.
 */
bool allParamValuesDefinedUpd(SQLUpdateStatement* updateStmt);

/**
 * Executes an update statement.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @return The number of rows that were updated, or -1 if an error occurs.
 * @throws OutOfMemoryError If a memory allocation fails.
 * @throws DriverException If the table is not set. 
 */
int32 litebaseDoUpdate(Context context, SQLUpdateStatement* updateStmt);

/**
 * Binds an update statement.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 @throws <code>SQLParseException</code> if the number of fields is greater than 254. 
 */
bool litebaseBindUpdateStatement(Context context, SQLUpdateStatement* updateStmt);

#endif
