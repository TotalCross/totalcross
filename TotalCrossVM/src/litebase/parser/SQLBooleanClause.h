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
 * Internal use only. Represents a boolean clause (<code>WHERE</code> or <code>HAVING</code>) in a SQL query.
 */

#ifndef SQLBOOLEANCLAUSE_H
#define SQLBOOLEANCLAUSE_H

#include "Litebase.h"

/**
 * Creates and initializes a boolean clause.
 *
 * @param heap The heap to allocate a <code>SQLBooleanClause</code> structure. 
 * @return A pointer to a <code>SQLBooleanClause</code> structure. 
 */
SQLBooleanClause* initSQLBooleanClause(Heap heap);

/**
 * Applies the table indices to the boolean clause. The method will possibly transform the SQL boolean tree, eliminating the branches that can be 
 * resolved through the table indexes.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param tableIndices The table indices; each position in the array relates to a column in the table; a <code>null</code> value indicates no 
 * index on that column.
 * @param columnsCount The number of columns of the table.
 * @param hasComposedIndex Indicates if the table has a composed index.
 * @return <code>true</code>, if any table index was applied to the boolean clause; <code>false</code>, otherwise.
 */
bool applyTableIndexes(SQLBooleanClause* booleanClause, Index** tableIndexes, int32 columnsCount, bool hasComposedIndex);

/**
 * Tries to apply an index to a branch of the expression tree that contains a relational expression.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param branch A branch of the expression tree.
 * @param isLeft Indicates if the index is being applied to the left branch.
 * @param indexMap An index bitmap.
 */
void applyIndexToBranch(SQLBooleanClause* booleanClause, SQLBooleanClauseTree* branch, Index** indexesMap, bool isLeft);

/**
 * Applies the composed index and removes the correspondent branch of the tree.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param branch A branch of the expression tree.
 * @param columns The columns present in the expression tree.
 * @param operators The operators of the expression tree.
 * @param indexesValueTree The part of the tree that uses indices.
 * @param compIndex The composed index.
 * @return The current branch of the tree.
 */
SQLBooleanClauseTree* applyComposedIndexToBranch(SQLBooleanClause* booleanClause, SQLBooleanClauseTree* branch, uint8* columns, uint8* operators, 
																											 SQLBooleanClauseTree** indexesValueTree, ComposedIndex* compIndex);
					
/**
 * Applies the table indexes to the boolean clause. The method will possibly transform the SQL boolean tree, to eliminate the branches that can be 
 * resolved through the table indexes.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @return <code>true</code>, if any table index was applied to the boolean clause; <code>false</code>, otherwise.
 */
bool applyTableIndexesJoin(SQLBooleanClause* booleanClause);

// juliana@noidr_3: improved index application on filters when using joins.
/**
 * Tries to apply an index to a branch of the expression tree that contains a relational expression.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param branch The branch of the expression tree.
 * @param isLeft Indicates if the index is being applied to the left branch.
 */
void applyIndexToBranchJoin(SQLBooleanClause* booleanClause, SQLBooleanClauseTree* branch, bool isLeft);

/**
 * Evaluate the boolean clause, accordingly to values of the current record of the given <code>ResultSet</code>.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @return <code>true</code> if all parameter are defined; <code>false</code>, otherwise.
 * @throws DriverException if a parameter is not defined.
 */
bool sqlBooleanClausePreVerify(Context context, SQLBooleanClause* booleanClause);

/**
 * Evaluates the boolean clause, accordingly to values of the current record of the given <code>ResultSet</code>.
 *
 * @param resultSet the ResultSet used for the evaluation.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return 1, if the current record of the result set satisfies the boolean clause; 0  if the current record of the result set does not satisfy the 
 * boolean clause; -1, otherwise.
 */
int32 sqlBooleanClauseSatisfied(Context context, SQLBooleanClause* booleanClause, ResultSet* resultSet, Heap heap);
                                                                          
/**
 * Binds the column information of the underlying table list to the boolean clause.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param names2Index <code>IntHashtable</code> that maps the column names to the column indexes.
 * @param columnTypes The data types of each column.
 * @param tableList The table list of the select clause.
 * @param tableListSize The number of tables of the table list.
 * @param heap A heap to allocate some new <code>SQLBooleanClauseTree</code> nodes.
 * @return <code>true</code>, if the boolean clause was bound successfully; <code>false</code>, otherwise. 
 */
bool bindColumnsSQLBooleanClause(Context context, SQLBooleanClause* booleanClause, Hashtable* names2Index, int8* columnTypes, 
											                                                  SQLResultSetTable** tableList, int32 tableListSize, Heap heap);

/**
 * Verifies if the column names are correct and belongs to the table list and is used only to verify if where clause and having clause field list 
 * is the field list of the where/having clause.
 * 
 * @param context The thread context where the function is being executed.
 * @param fieldList The field list of the where/having clause.
 * @param fieldsCount The number of fields.
 * @param tableList The table list.
 * @param tableListSize The numbers of tables of the table list.
 * @return <code>true</code>, if field name verification found no problems; <code>false</code>, otherwise. 
 * @throws SQLParseException If there is an unknown or an ambiguos column name.
 */
bool verifyColumnNamesOnTableList(Context context, SQLResultSetField** fieldList, int32 fieldsCount, SQLResultSetTable** tableList, 
											                                                                    int32 tableListSize);

/**
 * Binds the column information of the underlying table to the boolean clause.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param rsTable The <code>SQLResultSetTable</code> table of the update or delete statement.
 * @param heap A heap to allocate some new <code>SQLBooleanClauseTree</code> nodes.
 * @return <code>true</code>, if the boolean clause was bound successfully; <code>false</code>, otherwise. 
 */
bool bindColumnsSQLBooleanClauseSimple(Context context, SQLBooleanClause* clause, SQLResultSetTable* rsTable, Heap heap);

/**
 * Verifies if the column names are correct and belongs to the table and is used only to verify if where clause field list is the field list of 
 * the where clause.
 * 
 * @param context The thread context where the function is being executed.
 * @param fieldList The field list of the where/having clause.
 * @param fieldsCount The number of fields.
 * @param rsTable The <code>SQLResultSetTable</code> table of the update or delete statement.
 * @return <code>true</code>, if field name verification found no problems; <code>false</code>, otherwise. 
 * @throws SQLParseException If there is an unknown or an ambiguos column name.
 */
bool verifyColumnNamesOnTable(Context context, SQLResultSetField** fieldList, int32 fieldsCount, SQLResultSetTable* rsTable);

/**
 * Validates a string value as a date or datetime according with its type. If it is well-formed, its value is transformed into one or two ints.
 *
 * @param context The thread context where the function is being executed.
 * @param value The value that will receive the date or datetime as integers.
 * @param valueType The expected type: date or datetime.
 * @return <code>true</code> if the string storing a date or a datetime is well-formed; <code>false</code>, otherwise.
 * @throws SQLParseException If the string is not well-formed.
 */
bool validateDateTime(Context context, SQLValue* value, int32 valueType);

#endif
