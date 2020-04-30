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
 * Declares functions to deal with important prepared statements.
 */

#ifndef LITEBASE_PREPAREDSTATEMENT_H
#define LITEBASE_PREPAREDSTATEMENT_H

/**
 * Frees a prepared statement.
 *
 * @param unused Parameter for htFree().
 * @param statement The prepared statement to be freed.
 */
void freePreparedStatement(int32 unused, TCObject statement);

/**
 * Sets numeric parameters in a prepared statement.
 *
 * @param p->obj[0] The prepared statement.
 * @param p->i32[0] The index of the parameter value to be set, starting from 0.
 * @param p->i32[1] The value of the parameter.   
 * @param type The type of the parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws OutOfMemoryError If a memory allocation fails.
 */
bool psSetNumericParamValue(NMParams p, int32 type);

/**
 * Sets a string parameter in a prepared statement.
 *
 * @param context The thread context where the function is being executed.
 * @param stmt The prepared statement object.
 * @param string The string object to be inserted.
 * @param index The parameter index.
 * @param stringLength The string length.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise. 
 * @throws OutOfMemoryError If a memory allocation fails.
 */
bool psSetStringParamValue(Context context, TCObject stmt, TCObject string, int32 index, int32 stringLength);

/**
 * Returns the sql used in this statement in a string buffer. If logging is disabled, returns the sql without the arguments. If logging is enabled, 
 * returns the real sql, filled with the arguments. Used only for the logger.
 *
 * @param context The thread context where the function is being executed.
 * @param statement The prepared statement.
 * @return the sql used in this statement as a <code>StringBuffer</code> object.
 */
TCObject toStringBuffer(Context context, TCObject statement);

/**
 * Resets a where clause because the <code>expression</code> may change between runs of a prepared statement with a where clause.
 *
 * @param whereClause the were clause to be reseted.
 * @param heap A heap to allocate the clone of the where clause expression tree.
 */
void resetWhereClause(SQLBooleanClause* whereClause, Heap heap);

/**
 * Resets an order by or group by clause because the <code>tableColIndex</code> may change between runs of a prepared statement with a sort field. 
 * So, it is necessary to cache the <code>tableColIndex</code> of order by fields.
 *
 * @param orderByClause the order by clause to be reseted.
 */
void resetColumnListClause(SQLColumnListClause* columnListClause);

/**
 * Stores the null values of prepared statement in the table.
 *
 * @param table The Table used in the prepared statement.
 * @param record The prepared statement record.
 * @param storeNulls The prepared statement field that indicates if it is to store nulls in the parameters or not.
 * @param paramDefined Indicates which parameters are defined.
 * @param paramIndexes The parameters indexes.
 * @param nValues The number of fields or values of the prepared statement.
 * @param paramCount The number of parameters.
 * @param isPreparedUpdateStmt Indicates if the prepared statement is an update prepared statement or not.
 */
void rearrangeNullsInTable(Table* table, SQLValue** record, uint8* storeNulls, uint8* paramDefined,  uint8* paramIndexes, int32 nValues, 
                                                                                                                          int32 paramCount);

/**
 * Tests if the prepared statement or the driver where it was created is closed.
 *
 * @param p->obj[0] The prepared statement object.
 * @throws IllegalStateException If the prepared statement or driver is closed.
 */
bool testPSClosed(NMParams params);

#endif
