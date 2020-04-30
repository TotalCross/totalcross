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
 * Declares functions to deal with a SQL column list clause, like order by or group by.
 */

#ifndef LITEBASE_SQLCOLUMNLISTCLAUSE_H
#define LITEBASE_SQLCOLUMNLISTCLAUSE_H

#include "Litebase.h"

/**
 * Compares two SQL column lists clauses. They can only be considered equal if they list the same column list in the same sequence.
 *
 * @param clause1 The first list used in the comparison.
 * @param clause1 The second list used in the comparison.
 * @return <code>true</code>, if both column lists list the same column sequence; <code>false</code>, otherwise.
 */
bool sqlcolumnlistclauseEquals(SQLColumnListClause* clause1, SQLColumnListClause* clause2);

/**
 * Checks if the column list contains the given column.
 *
 * @param clause The column list clause.
 * @param colIndex The column index of the column being searched for.
 * @return <code>true</code> if the column is in the column list clause; <code>false</code>, otherwise.
 */
bool sqlcolumnlistclauseContains(SQLColumnListClause* clause, int32 colIndex);

/** 
 * Binds the column information of the underlying order or group by clause to the select clause. 
 *
 * @param context The thread context where the function is being executed.
 * @param clause The column list clause.
 * @param names2Index The select clause columns hash table.
 * @param columTypes The select clause tables column types.
 * @param tableList The select clause tables.
 * @param tableListSize The number of tables of the select clause.
 * @throws SQLParseException If the column in a group or order by clause is not in the select clause or there is a column of type blob in the 
 * clause.
 */
bool bindColumnsSQLColumnListClause(Context context, SQLColumnListClause* clause, Hashtable* names2Index, int8* columnTypes, 
                                                                                  SQLResultSetTable** tableList, int32 tableListSize);

/**
 * Finds the best index to use in a sort operation.
 *
 * @param clause An order or group by clause.
 */
void findSortIndex(SQLColumnListClause* clause);

#endif
