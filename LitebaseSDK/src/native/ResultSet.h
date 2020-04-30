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
 * This module declares functions for fetching a set or rows resulting from a <code>LitebaseConnection.executeQuery()</code> method call.
 * Here's an example:
 *
 * <pre>
 * ResultSet rs = driver.executeQuery(&quot;select name, salary, age from person&quot;);
 * while (rs.next())
 *    Vm.debug(pad(rs.getString(&quot;name&quot;), 32) + pad(rs.getString(&quot;salary&quot;), 16) 
 *                                                     + rs.getInt(&quot;age&quot;) + &quot; years&quot;);
 * </pre>
 *
 * Result sets cannot be constructed directly; instead, you must issue a sql to the driver.
 */

#ifndef LITEBASE_RESULTSET_H
#define LITEBASE_RESULTSET_H

#include "Litebase.h"

/**
 * Frees a result set structure.
 *
 * @param resultSet The resultSet to be freed.
 */
void freeResultSet(ResultSet* resultSet);

/**
 * Creates a result set structure.
 * 
 * @param table The table to be used by the result set, which can be temporary or not.
 * @param whereClause The where clause to evaluate the records of the table to be returned to the user.
 * @param heap The heap to allocate the result set structure.
 * @return The result set created.
 */
ResultSet* createResultSet(Table* table, SQLBooleanClause* whereClause, Heap heap);

/** 
 * Creates a simple result set structure and computes the indices.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table to be used by the result set, which can be temporary or not.
 * @param whereClause The where clause to evaluate the records of the table to be returned to the user.
 * @param heap The heap to allocate the result set structure.
 * @return The result set created.
 */
ResultSet* createSimpleResultSet(Context context, Table* table, SQLBooleanClause* whereClause, Heap heap);

/** 
 * Creates a result set structure and computes the indices for the returning of a select statement.
 *
 * @param context The thread context where the function is being executed.
 * @param table The table to be used by the result set, which can be temporary or not.
 * @param whereClause The where clause to evaluate the records of the table to be returned to the user.
 * @param heap The heap to allocate the result set structure.
 * @return The result set created.
 */
ResultSet* createResultSetForSelect(Context context, Table* table, SQLBooleanClause* whereClause, Heap heap);

/**
 * Gets the next record of the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @return <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
bool resultSetNext(Context context, ResultSet* resultSet);

/**
 * Gets the previous record of the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @return <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
bool resultSetPrev(Context context, ResultSet* resultSet);

/**
 * Given the column index (starting from 1), returns a short value that is represented by this column. Note that it is only possible to request 
 * this column as short if it was created with this precision or if the data being fetched is the result of a DATE or DATETIME SQL function.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int32 rsGetShort(ResultSet* resultSet, int32 column);

/**
 * Given the column index (starting from 1), returns an integer value that is represented by this column. Note that it is only possible to request 
 * this column as integer if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int32 rsGetInt(ResultSet* resultSet, int32 column);

/**
 * Given the column index (starting from 1), returns a long value that is represented by this column. Note that it is only possible to request 
 * this column as long if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0</code>.
 */
int64 rsGetLong(ResultSet* resultSet, int32 column);

/**
 * Given the column index (starting from 1), returns a float value that is represented by this column. Note that it is only possible to request 
 * this column as float if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
float rsGetFloat(ResultSet* resultSet, int32 column);

/**
 * Given the column index (starting from 1), returns a double value that is represented by this column. Note that it is only possible to request 
 * this column as double if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>0.0</code>.
 */
double rsGetDouble(ResultSet* resultSet, int32 column);

/**
 * Given the column index (starting from 1), returns a char array that is represented by this column. Note that it is only possible to request 
 * this column as a char array if it was created as a string.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param value A <code>SQLValue</code> to hold the char array.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
TCObject rsGetChars(Context context, ResultSet* resultSet, int32 column, SQLValue* value);

/**
 * Given the column index (starting from 1), fetches two integers values that are represented by this column. Note that it is only possible to 
 * request this column as date time if it was created with this precision.
 *
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param The structure that will hold the two returned integers.
 */
void rsGetDateTimeValue(ResultSet* resultSet, int32 column, SQLValue* value);

/**
 * Given the column index (starting from 1), returns a byte array (blob) that is represented by this column. Note that it is only possible to request 
 * this column as a blob if it was created as a string.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
TCObject rsGetBlob(Context context, ResultSet* resultSet, int32 column);

/**
 * Given the column index (starting from 1), returns a string that is represented by this column. Any column type can be returned as a string. 
 * <code>Double</code>/<code>float</code> values formatting will use the precision set with the <code>setDecimalPlaces()</code> method.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set to be searched.
 * @param column The column index.
 * @param value A <code>SQLValue</code> to hold the char array.
 * @return The column value; if the value is SQL <code>NULL</code>, the value returned is <code>null</code>.
 */
TCObject rsGetString(Context context, ResultSet* resultSet, int32 column, SQLValue* value);

/**
 * Starting from the current cursor position, it reads all result set rows that are being requested. <code>first()</code>,  <code>last()</code>, 
 * <code>prev()</code>, or <code>next()</code> must be used to set the current position, but not  <code>beforeFirst()</code> or 
 * <code>afterLast()</code>. It doesn't return BLOB values. <code>null</code> is returned in their places instead.
 *
 * @param p->obj[0] The result set. 
 * @param p->retO receives a matrix, where <code>String[0]<code> is the first row, and <code>String[0][0], String[0][1]...</code> are the column 
 * elements of the first row. Returns <code>null</code> if here's no more element to be fetched. Double/float values will be formatted using the 
 * <code>setDecimalPlaces()</code> settings. If the value is SQL <code>NULL</code> or a <code>blob</code>, the value returned is <code>null</code>.
 * @param count The number of rows to be fetched, or -1 for all.
 * @throws DriverException If the result set or the driver is closed, or the result set position is invalid.
 * @throws IllegalArgumentException If count is less then -1.
 */
void getStrings(NMParams p, int32 count); // juliana@201_2: corrected a bug that would let garbage in the number of records parameter.

/**
 * Returns a column value of the result set given its type and column index. DATE will be returned as a single int. This function can't be used to
 * return a DATETIME.
 * 
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array or a blob if type is <code>UNDEFINED</code>, <code>CHARS</code>, or <code>BLOB</code>, 
 * respectively.
 */
void rsGetByIndex(NMParams p, int32 type);

/**
 * Returns a column value of the result set given its type and column name. DATE will be returned as a single int. This function can't be used to
 * return a DATETIME.
 * 
 * @param p->obj[0] The result set.
 * @param p->obj[1] The column name.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array or a blob if type is <code>UNDEFINED</code>, <code>CHARS</code>, or <code>BLOB</code>, 
 * respectively.
 * @throws NullPointerException If the column name is <code>null</code>.
 */
void rsGetByName(NMParams p, int32 type);

/**
 * Returns a column value of the result set given its type and column index. 
 * 
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param type The type of the column. <code>UNDEFINED</code> must be used to return anything except for blobs as strings.
 * @param p->retI receives an int or a short if type is <code>INT</code> or <code>SHORY</code>, respectively.
 * @param p->retL receives a long if type is <code>LONG</code>.
 * @param p->retD receives a float or a double if type is <code>FLOAT</code> or <code>DOUBLE</code>, respectively.
 * @param p->retO receives a string, a character array, a blob, a date, or a time if type is <code>UNDEFINED</code>, <code>CHARS</code>, 
 * <code>BLOB</code>, <code>DATE</code>, or <code>DATETIME</code>.
 * respectively.
 * @throws DriverException If the kind of return type asked is incompatible from the column definition type.
 */
void rsPrivateGetByIndex(NMParams p, int32 type);

/**
 * Given the column index (starting from 1), indicates if this column has a <code>NULL</code>.
 *
 * @param p->obj[0] The result set.
 * @param p->i32[0] The column index.
 * @param p->retI receives <code>true</code> if the value is SQL <code>NULL</code>; <code>false</code>, otherwise.
 */
void rsPrivateIsNull(NMParams params);

/**
 * Verifies if the result set and the column index are valid.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param column The result set table column being searched.
 * @return <code>true</code> if the the result set and the column index are valid; <code>false</code>, otherwise.
 * @throws DriverException If the result set position is invalid.
 * @throws IllegalArgumentException If the column index is invalid.
 */
bool verifyRSState(Context context, ResultSet* resultSet, int32 column);

/**
 * Gets the next record of a result set. This function is to be used by the result sets created internally by the Litebase code, not by external 
 * result sets.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return <code>true</code> if there is a next record to go to in the result set; <code>false</code>, otherwise.
 */
bool getNextRecord(Context context, ResultSet* resultSet, Heap heap);

// rnovais@567_2
/**
 * Formats a int date into a date string according with the device formatting settings.
 *
 * @param buffer The buffer where the date will be stored as a string.
 * @param intDate An integer representing a date.
 */
void formatDate(CharP buffer, int32 intDate);

/**
 * Formats an int time into a time according with the device formatting settings.
 * 
 * @param buffer The buffer where the date will be stored as a string.
 * @param intDate An integer representing a time.
 */
void formatTime(CharP buffer, int32 intTime);

/**
 * Pads a numeric string with zeros on the left to format dates and times.
 *
 * @param buffer The string which stores a date or a time.
 * @param value The date or time part to be inserted in the string.
 * @param order The decimal order of the value being inserted in the string.
 * @return The buffer string address offset by the number of decimal orders.
 */
CharP zeroPad(CharP buffer, int32 value, uint32 order);

/** 
 * Calculates the hash code of a string object.
 *
 * @param stringObj The string object.
 * @return The hash code of the string object.
 */
int32 identHashCode(TCObject stringObj);

/**
 * Applies a function when fetching data from the result set.
 * 
 * @param params->currentContext The thread context where the function is being executed.
 * @param params->retO The returned data as a string if the user wants the table data in this format.
 * @param value The value where the function will be applied.
 * @param field The field where the function is being applied.
 * @param type The type of the field being returned.
 */
void rsApplyDataTypeFunction(NMParams params, SQLValue* value, SQLResultSetField* field, int32 type);

/**
 * Creates a string to return to the user.
 * 
 * @param params->currentContext The thread context where the function is being executed.
 * @param params->retO The returned data as a string if the user wants the table data in this format.
 * @param value The value where the function will be applied.
 * @param type The type of the value being returned to the user.
 * @param decimalPlaces The number of decimal places if the value is a floating point number.
 */
void createString(NMParams params, SQLValue* value, int32 type, int32 decimalPlaces);

/**
 * Loads the physical table where a string or blob is stored and its position in the .dbo file. 
 *
 * @param buffer A buffer where is stored the string position in the result set dbo.
 * @param plainDB The result set plainDB, which will become the physical one if the query uses a temporary table.
 * @param position The position of the string or blob in the physical dbo.
 */
void loadPlainDBAndPosition(uint8* buffer, PlainDB** plainDB, int32* position);

/**
 * Tests if the result set or the driver where it was created is closed.
 *
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set object.
 * @throws IllegalStateException If the result set or driver is closed.
 */
bool testRSClosed(Context context, TCObject resultSet);

/**
 * Returns a table used in a select given its name.
 * 
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param tableName The table name.
 * @return The table with the given name or <code>null</code> if an exception occurs.
 * @throws DriverException if the given table name is not used in the select.
 */
Table* getTableRS(Context context, ResultSet* resultSet, CharP tableName);

/**
 * Gets the default value of a column.
 * 
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set.
 * @param tableName The name of the table.
 * @param index The column index.
 * @return The default value of the column as a string or <code>null</code> if there is no default value.
 * @throws DriverException If the column index is of a column of type <code>BLOB</code>.
 */
TCObject getDefault(Context context, ResultSet* resultSet, CharP tableName, int32 index);

#endif
