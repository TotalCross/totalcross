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
 * Declares the functions to initialize, set, and process a delete statement.
 */

#ifndef LITEBASE_SQLDELETESTATEMENT_H
#define LITEBASE_SQLDELETESTATEMENT_H

#include "Litebase.h"

/**
 * Initializes a SQL detete statement for a given SQL. 
 *
 * @param parser The parse structure with parse information concerning the SQL.
 * @param isPrepared Indicates if the delete statement is from a prepared statement.
 * @return A pointer to a <code>SQLDeleteStatement</code> structure. 
 */
SQLDeleteStatement* initSQLDeleteStatement(LitebaseParser* parser, bool isPrepared);

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
bool setNumericParamValueDel(Context context, SQLDeleteStatement* deleteStmt, int32 index, VoidP value, int32 type);

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
bool setParamValueStringDel(Context context, SQLDeleteStatement* deleteStmt, int32 index, JCharP value, int32 length);

/**
 * Clears all parameter values of a prepared statement delete.
 *
 * @param deleteStmt A SQL delete statement.
 */
void clearParamValuesDel(SQLDeleteStatement* deleteStmt);

/**
 * Checks if all parameters values are defined.
 *
 * @param deleteStmt A SQL delete statement.
 * @return <code>true</code>, if all parameters values are defined; <code>false</code> otherwise.
 */
bool allParamValuesDefinedDel(SQLDeleteStatement* deleteStmt);

/**
 * Executes a delete statement.
 *
 * @param context The thread context where the function is being executed.
 * @param deleteStmt A SQL delete statement.
 * @return The number of rows deleted.
 * @throws DriverException If the record can't be removed from the indices.
 * @throws OutOfMemoryError If a heap memory allocation fails.
 */
int32 litebaseDoDelete(Context context, SQLDeleteStatement* deleteStmt);

/**
 * Binds a <code>SQL DELETE</code> expression.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param deleteStmt A SQL delete statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 */
bool litebaseBindDeleteStatement(Context context, TCObject driver, SQLDeleteStatement* deleteStmt);

#endif

