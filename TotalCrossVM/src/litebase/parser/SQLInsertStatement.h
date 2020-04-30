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
 * Declares the functions to initialize, set, and process an insert statement.
 */

#ifndef LITEBASE_SQLINSERTSTATEMENT_H
#define LITEBASE_SQLINSERTSTATEMENT_H

#include "Litebase.h"

/**
 * Constructs an insert statement given the result of the parsing process.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param parser The result of the parsing process.
 * @return A pointer to a <code>SQLInsertStatement</code> structure. 
 * @throws SQLParseException If there is a field named "rowid". 
 */
SQLInsertStatement* initSQLInsertStatement(Context context, TCObject driver, LitebaseParser* parser);

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
bool setNumericParamValueIns(Context context, SQLInsertStatement* insertStmt, int32 index, VoidP value, int32 type);

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
bool setStrBlobParamValueIns(Context context, SQLInsertStatement* insertStmt, int32 index, VoidP value, int32 len, bool isStr);

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
bool setNullIns(Context context, SQLInsertStatement* insertStmt, int32 index);

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
bool checkInsertIndex(Context context, SQLInsertStatement* insertStmt, int32 index);

/**
 * Clears all parameter values of a prepared statement insert.
 *
 * @param insertStmt A SQL insert statement.
 */
void clearParamValuesIns(SQLInsertStatement* insertStmt);

/**
 * Executes an insert statement.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * return <code>true</code> if the insertion was performed successfully; <code>false</code>, otherwise.
 */
bool litebaseDoInsert(Context context, SQLInsertStatement* insertStmt);

/**
 * Binds an insert statement.
 *
 * @param context The thread context where the function is being executed.
 * @param insertStmt A SQL insert statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 * @throws SQLParseException If the number of values inserted is different from the table definition.
 */
bool litebaseBindInsertStatement(Context context, SQLInsertStatement* insertStmt);

#endif
