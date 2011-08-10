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
 * @return A pointer to a <code>SQLUpdateStatement</code> structure. 
 * @throws OutOfMemoryError If a heap memory allocation fails. 
 */
SQLUpdateStatement* initSQLUpdateStatement(Context context, Object driver, LitebaseParser* parse, bool isPrepared);

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
void setNumericParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 type);

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
bool setStrBlobParamValueUpd(Context context, SQLUpdateStatement* updateStmt, int32 index, VoidP value, int32 length, bool isStr);

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
bool setNullUpd(Context context, SQLUpdateStatement* updateStmt, int32 index);

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
 * return The number of rows that were updated, or -1 if an error occurs.
 */
int32 litebaseDoUpdate(Context context, SQLUpdateStatement* updateStmt);

/**
 * Binds an update statement.
 *
 * @param context The thread context where the function is being executed.
 * @param updateStmt A SQL update statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 @throws <code>SQLParseException</code> if the number of fields is greater than 128. 
 */
bool litebaseBindUpdateStatement(Context context, SQLUpdateStatement* updateStmt);

#endif
