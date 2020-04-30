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
 * Declares the functions to initialize, set, and process a select statement.
 */

#ifndef LITEBASE_SQLSELECTSTATEMENT_H
#define LITEBASE_SQLSELECTSTATEMENT_H

#include "Litebase.h"

/**
 * Creates and initializes a SQL select statement.
 *
 * @param parser The structure returned from the parsing process.
 * @param isPrepared Indicates if the delete statement is from a prepared statement.
 * @return A pointer to a <code>SQLSelectStatement</code> structure. 
 */
SQLSelectStatement* initSQLSelectStatement(LitebaseParser* parser, bool isPrepared);

/* 
 * Sets the value of a numeric parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param selectStmt A SQL select statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param type The type of the parameter.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @thows DriverException If the parameter index is invalid.
 */
bool setNumericParamValueSel(Context context, SQLSelectStatement* selectStmt, int32 index, VoidP value, int32 type);

/* 
 * Sets the value of a string parameter at the given index.
 *
 * @param context The thread context where the function is being executed.
 * @param selectStmt A SQL select statement.
 * @param index The index of the parameter.
 * @param value The value of the parameter.
 * @param length The length of the string.
 * @throws SQLParserException If a <code>null</code> is used as a parameter of a where clause.
 * @thows DriverException If the parameter index is invalid.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setParamValueStringSel(Context context, SQLSelectStatement* selectStmt, int32 index, JCharP value, int32 length);

/**
 * Clears all parameter values of a prepared statement select.
 *
 * @param selectStmt A SQL select statement.
 */
void clearParamValuesSel(SQLSelectStatement* selectStmt);

/**
 * Checks if all parameters values are defined.
 *
 * @param selectStmt A SQL select statement.
 * @return <code>true</code>, if all parameters values are defined; <code>false</code> otherwise.
 */
bool allParamValuesDefinedSel(SQLSelectStatement* selectStmt);

/**
 * Executes a select statement.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param selectStmt A SQL select statement.
 * @return A result set returned by the query execution.
 * @throws DriverException If the record can't be removed from the indices.
 * @throws OutOfMemoryError If a heap memory allocation fails.
 */
TCObject litebaseDoSelect(Context context, TCObject driver, SQLSelectStatement* selectStmt);

/**
 * Binds a select statement.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The Litebase connection.
 * @param selectStmt A SQL select statement.
 * @return <code>true</code>, if the statement was bound successfully; <code>false</code> otherwise.
 */
bool litebaseBindSelectStatement(Context context, TCObject driver, SQLSelectStatement* selectStmt);

/**
 * Tries to put as inner table a table that has an index used more often in the where clause, when the where clause has a comparison between 
 * fields from different tables. e.g.: <code>select * from table1, table2 where table1.field1 = table2.field2 </code> If only 
 * <code>table1.field1</code> has index, changes the select to: <code>select * from table2, table1 where table1.field1 = table2.field2</code>. 
 * If both tables has the same level of index using, sorts them by the row count.
 *
 * @param selectStmt A SQL select statement.
 */
void orderTablesToJoin(SQLSelectStatement* selectStmt);

/**
 * Binds the SQLSelectStatement to the select clause tables.
 *
 * @param context The thread context where the function is being executed.
 * @param selectStmt A SQL select statement.
 * @return <code>true</code> if the statement could be corrected bound; <code>false</code>, otherwise.
 */
bool bindSelectStatement(Context context, SQLSelectStatement* selectStmt);

/**
 * Validates the SQLSelectStatement.
 *
 * @param context The thread context where the function is being executed.
 * @param selectStmt The select statement to be validated.
 * @return <code>false</code> if a <code>SQLParseException</code> occurs; <code>true</code>, otherwise.
 * @throws SQLParseException If the order by and group by clauses do not match, if a query with group by is not well-formed, if there is a 
 * having clause without an aggregation, a field in the having clause is not in the select clause, there is no order by and there are aggregated 
 * functions mixed with real columns, or there is an aggregation with an order by clause and no group by clause.
 */
bool validateSelectStatement(Context context, SQLSelectStatement* selectStmt);

/**
 * Generates a table to store the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param selectStmt The select statement to be validated.
 * @return The temporary result set table or null if an error occurs.
 * @throws OutOfMemoryError If there is not enougth memory alloc memory. 
 */
Table* generateResultSetTable(Context context, TCObject driver, SQLSelectStatement* selectStmt);

/**
 * Generates a table to store the result set.
 *
 * @param context The thread context where the function is being executed.
 * @param tableList The table list of the select.
 * @param size The number of tables of the select.
 * @param whereClause the where clause of the select.
 * @param rsList Receives the temporary result set list.
 * @param heap A heap to perform some memory allocations.
 * @return <code>false</code>if an error occurs when appling the indices; <code>true</code>, otherwise.
 */
bool createListResultSetForSelect(Context context, SQLResultSetTable** tableList, int32 size, SQLBooleanClause* whereClause, ResultSet** rsList, Heap heap);

/**
 * Generates an index bit map for a list of result sets.
 *
 * @param context The thread context where the function is being executed.
 * @param rsList The list of result sets.
 * @param size The number of tables of the select.
 * @param hasComposedIndex Indicates if the table has a composed index.
 * @param heap A heap to allocate temporary structures.
 * @return <code>true</code> if the function executed correctly; <code>false</code>, otherwise.
 */
bool generateIndexedRowsMap(Context context, ResultSet** rsList, int32 size, bool hasComposedIndex, Heap heap);

/**
 * Finds the rows that satisfy the query clause using the indices.
 *
 * @param context The thread context where the function is being executed.
 * @param rsList The result set list, one for each table.
 * @param size The number of tables of the select.
 * @param isJoin Indicates that the query has a join.
 * @param indexRsOnTheFly The index of the result set or -1 if the query is being indexed on the fly.
 * @param value The value to be indexed on the fly.
 * @param operator The operand type. Used only to index on the fly.
 * @param colIndex The index column. Used only to index on the fly.
 * @param heap A heap to allocate temporary structures.
 * @return <code>true</code> if the function executed correctly; <code>false</code>, otherwise.
 */
bool computeIndex(Context context, ResultSet** rsList, int32 size, bool isJoin, int32 indexRsOnTheFly, SQLValue* value, int32 operator, 
						                                                                                                      int32 colIndex, Heap heap);
/**
 * Merges two bitmaps into the first bitmap using the given boolean operator.
 *
 * @param bitmap1 The first bitmap.
 * @param bitmap2 The second bitmap.
 * @param booleanOp The boolean operator to be applied.
 */
void mergeBitmaps(IntVector* bitmap1, IntVector* bitmap2, int32 booleanOp);

/**
 * Concludes the calculation of the given aggregated function running totals based on the given record and the group count.
 * 
 * @param record The record that is the parameter for the aggregated function.
 * @param groupCount The result of a COUNT(*).
 * @param aggFunctionsRunTotals The results of the aggregated functions. 
 * @param aggFunctionsCodes The aggregated function codes.
 * @param aggFunctionsParamCols The columns that are parameters to the aggregated functions.
 * @param aggFunctionsRealParamCols The real columns that are parameters to the aggregated functions.
 * @param aggFunctionsColsCount The number of columns that are parameters to the aggregated functions.
 * @param columnTypes The types of the columns.
 * @param groupCountCols The count for the groups.
 */
void endAggFunctionsCalc(SQLValue** record, int32 groupCount, SQLValue* aggFunctionsRunTotals, int8* aggFunctionsCodes, 
								 int32* aggFunctionsParamCols, int32* aggFunctionsRealParamCols, int32 aggFunctionsColsCount, int8* columnTypes, 
								                                                                                              int32* groupCountCols);
/**
 * Creates a temporary table that stores only an integer value.
 *
 * @param context The thread context where the function is being executed.
 * @param driver The connection with Litebase.
 * @param intValue The value to be put in the table.
 * @param colName The column name of the single table column.
 * @return The table if the method executes correctlty; <code>null</code>, otherwise.
 * @throws OutOfMemoryError If there is not enougth memory alloc memory. 
 */
Table* createIntValueTable(Context context, TCObject driver, int32 intValue, CharP colName);

/** 
 * Binds the column information of the underlying tables to the select clause. 
 *
 * @param context The thread context where the function is being executed.
 * @param clause The select clause.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws SQLParseException In case of an unknown or ambiguous column name, the parameter and the function data types are incompatible, or the total
 * number of fields of the select exceeds the maximum.
 */
bool bindColumnsSQLSelectClause(Context context, SQLSelectClause* clause);

/**
 * Remaps a table column names, so it uses the alias names of the given field list, instead of the original names.
 * 
 * @param context The thread context where the function is being executed.
 * @param table The result set table.
 * @param fieldsList The field list of the select clause.
 * @param fieldsCount The number of fields of the select clause.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throw OutOfMemoryError If a memory allocation fails.
 */
bool remapColumnsNames2Aliases(Context context, Table* table, SQLResultSetField** fieldsList, int32 fieldsCount);

/**
 * Writes the records of a result set to a table.
 *
 * @param context The thread context where the function is being executed.
 * @param list The result set list, one for each table in the from field.
 * @param numTables The number of tables of the select.
 * @param rs2TableColIndexes The mapping between result set and table columns.
 * @param selectClause The select clause of the query.
 * @param columnIndexesTables Has the indices of the tables for each resulting column.
 * @param whereClauseType Indicates the where clause is an <code>AND</code> or an <code>OR</code>.
 * @param heap A heap to allocate temporary structures.
 * @return The total number of records added to the table or -1 if an error occurs.
 */
int32 writeResultSetToTable(Context context, ResultSet** list, int32 numTables, Table* table, int16* rs2TableColIndexes, 
                                             SQLSelectClause* selectClause, size_t* columnIndexesTables, int32 whereClauseType, Heap heap);

/**
 * Counts the number of ON bits.
 *
 * @param elements The array where the bits will be counted.
 * @param length The array length.
 * @return The number of on bits.
 */
int32 bitCount(int32* elements, int32 length);

/**
 * Executes a join operation.
 * 
 * @param context The thread context where the function is being executed.
 * @param list The list of the result sets.
 * @param numTables The number of tables of the select.
 * @param table The result set table.
 * @param rs2TableColIndexes The mapping between result set and table columns.
 * @param values The record to be joined with.
 * @param whereClauseType The type of operation used: <code>AND</code> or <code>OR</code>.
 * @param heap A heap to allocate temporary structures.
 * @return The number of records written to the temporary table or -1 if an error occurs.
 */
int32 performJoin(Context context, ResultSet** list, int32 numTables, Table* table, int16* rs2TableColIndexes, SQLValue** values, 
                                                                                    int32 whereClauseType, Heap heap);

/**
 * Gets the next record to perform the join operation.
 * 
 * @param context The thread context where the function is being executed.
 * @param rsIndex The index of the result set of the list used to get the next record.
 * @param verifyWhereCondition Indicates if the where clause needs to be verified.
 * @param totalRs The number of result sets (tables used in the join) in the result set list.
 * @param whereClauseType The type of expression in the where clause (OR or AND).
 * @param rsList The list of the result sets.
 * @param heap A heap to allocate temporary structures.
 * @return <code>VALIDATION_RECORD_OK</code>, <code>NO_RECORD</code>, <code>VALIDATION_RECORD_NOT_OK</code>,
 * <code>VALIDATION_RECORD_INCOMPLETE</code>, or -1 if an error occurs.
 */
int32 getNextRecordJoin(Context context, int32 rsIndex, bool verifyWhereCondition, int32 totalRs, int32 whereClauseType, ResultSet** rsList, 
                                                                                                                         Heap heap);

/**
 * Evaluates an expression tree for a join.
 * 
 * @param context The thread context where the function is being executed.
 * @param tree The expression tree to be evaluated.
 * @param rsList The list of the result sets.
 * @param totalRs The number of result sets (tables used in the join) in the result set list.
 * @param heap A heap to allocate temporary structures.
 * @return <code>VALIDATION_RECORD_OK</code>, <code>NO_RECORD</code>, <code>VALIDATION_RECORD_NOT_OK</code>,
 * <code>VALIDATION_RECORD_INCOMPLETE</code>, or -1 if an error occurs.
 */
int32 booleanTreeEvaluateJoin(Context context, SQLBooleanClauseTree* tree, ResultSet** rsList, int32 totalRs, Heap heap);

/**
 * Calculates aggregation functions. 
 *
 * @param context The thread context where the function is being executed.
 * @param record The record of the values to be used in the calculation.
 * @param nullsRecord The values of the record that are null.
 * @param aggFunctionsRunTotals The current totals for the aggregation functions.
 * @param aggFunctionsCodes The codes of the used aggregation functions.
 * @param aggFunctionsParamCols The columns that use aggregation functions.
 * @param aggFunctionsColsCount The number of columns that use aggregation functions.
 * @param columnTypes The types of the columns.
 * @param groupCountCols The columns that use count. 
 */
void performAggFunctionsCalc(Context context, SQLValue** record, uint8* nullsRecord, SQLValue* aggFunctionsRunTotals, int8* aggFunctionsCodes, 
                                              int32* aggFunctionsParamCols, int32 aggFunctionsColsCount, int8* columnTypes, int32* groupCountCols);

/**
 * Calculates the answer of a select without aggregation, join, order by, or group by without using a temporary table.
 * 
 * @param context The thread context where the function is being executed.
 * @param resultSet The result set of the table.
 * @param heap A heap to allocate temporary structures.
 */
void computeAnswer(Context context, ResultSet* resultSet, Heap heap);

/**
 * Finds the best index to use in a min() or max() operation.
 *
 * @param field The field which may have a min() or max() operation.
 */
void findMaxMinIndex(SQLResultSetField* field);

#endif

