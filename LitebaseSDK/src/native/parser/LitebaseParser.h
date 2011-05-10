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
 * Declares the functions to initialize, set, and process the parser structure.
 */

#ifndef LITEBASEPARSER_H
#define LITEBASEPARSER_H

#include "Litebase.h"

/**
 * Shows a parser error message when the error position is known.
 *
 * @param error An error code.
 * @param parser A pointer to the parser structure.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
void lbError(int32 error, LitebaseParser* parser);

/**
 * Shows a parser error message when the error position is unknown.
 *
 * @param error An error code.
 * @param extraMsg An extra error message.
 * @param parser A pointer to the parser structure.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
void errorWithoutPosition(int32 error, CharP extraMsg, LitebaseParser* parser);

/**
 * Initializes and parses a sql string. 
 *
 * @param context The thread context where the function is being executed.
 * @param sqlStr The sql unicode string.
 * @param sqlLen The length of the sql string.
 * @param heap A heap to allocate the parser structure.
 * @return A pointer to a <code>LitebaseParser</code> structure if the string does not have parser errors or <code>null</code>, otherwise.
 */
LitebaseParser* initLitebaseParser(Context context, JCharP sqlStr, int32 sqlLen, Heap heap);

/**
 * Sets the operand type. 
 *
 * @param operandType The type of the operand.
 * @param parser A pointer to the parser structure.
 * @return A pointer to a <code>SQLBooleanTree</code> structure for a expression tree.
 */
SQLBooleanClauseTree* setOperandType(int32 operandType, LitebaseParser* parse);

/** 
 * Adds a column field to the order or group by field list. 
 *
 * @param field The field to be added to the list.
 * @param isAscending Indicates if the order by or group by sorting is in ascending or descending order.
 * @param isOrderBy Indicates if the field list where to add a list is a order or group by.
 * @param parser A pointer to the parser structure, which contains the list(s).
 * @return <code>true</code> if the number of fields has not reached its limit; <code>false</code>, otherwise.
 */
bool addColumnFieldOrderGroupBy(SQLResultSetField* field, bool isAscending, bool isOrderBy, LitebaseParser* parser);

/**
 * Gets a especific boolean clause: it will get a where clause if there is a where clause; otherwise, it will get a having clause.
 *
 * @param parser A pointer to the parser structure.
 * @return A pointer to a <code>SQLBooleanClause</code>.
 */
SQLBooleanClause* getInstanceBooleanClause(LitebaseParser* parser);

/**
 * Sets an expression tree in a where clause.
 *
 * @param tree An expression tree.
 * @param parser A pointer to the parser structure.
 */
void setBooleanClauseTreeOnWhereClause(SQLBooleanClauseTree* tree, LitebaseParser* parser);

/**
 * The function that parses the sql string.
 *
 * @param YYPARSE_PARAM The parser structure.
 * @return <code>true</code> if there are no parser errors; <code>false</code>, otherwise. 
 */
bool yyparse(VoidP YYPARSE_PARAM);

/**
 * Initializes a field definition for a table being created. 
 *
 * @param fieldName The name of the new field.
 * @param fieldType The type of the new field.
 * @param fieldSize The size of the new field (it is zero if the type is not CHARS or CHARS_NOCASE).
 * @param isPrimaryKey Indicates if the new field is the primary key.
 * @param defaultValue The default value of the new field (it can be null if there is no default value).
 * @param isNotNull Indicates if the new field can be null or not.
 * @return a pointer to the new result set table structure.
 */
SQLFieldDefinition* initSQLFieldDefinition(CharP fieldName, int32 fieldType, int32 fieldSize, bool isPrimaryKey, JCharP defaultValue, bool isNotNull, 
													                                                                                               Heap heap);

/**
 * Initializes a result set field.
 *
 * @param heap A heap to alocate the result set field structure.
 * @return A pointer to a result set field structure.
 */
SQLResultSetField* initSQLResultSetField(Heap heap);

/**
 * Initializes a result set table structure using a table name and its optional alias name.
 *
 * @param tableName the name of the new table.
 * @param aliasTableName the optional alias name of the new table (it can be null).
 * @return a pointer to the new result set table structure.
 */
SQLResultSetTable* initSQLResultSetTable(CharP tableName, CharP aliasTableName, Heap heap);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the function <code>lbError()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_lbError(TestSuite* testSuite, Context currentContext);   

/**
 * Tests if the function <code>errorWithoutPosition()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_errorWithoutPosition(TestSuite* testSuite, Context currentContext);

/**
 * Tests if the function <code>initLitebaseParser()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_initLitebaseParser(TestSuite* testSuite, Context currentContext);

#endif

#endif
