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
 * Declares the functions to initialize, set, and process the parser structure.
 */

#ifndef LITEBASEPARSER_H
#define LITEBASEPARSER_H

#include "Litebase.h"

/**
 * Shows a parser error message without an extra message.
 *
 * @param error An error code.
 * @param parser A pointer to the parser structure.
 * @return <code>PARSER_ERROR<code> to indicate that an error has occurred.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
int32 lbError(int32 error, LitebaseParser* parser);

/**
 * Shows a parser error message with an extra message.
 *
 * @param error An error message.
 * @param message An extra error message.
 * @param parser A pointer to the parser structure.
 * @return <code>PARSER_ERROR<code> to indicate that an error has occurred.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
int32 lbErrorWithMessage(CharP error, CharP message, LitebaseParser* parser);

/**
 * Initializes and parses a sql string. 
 *
 * @param context The thread context where the function is being executed.
 * @param sqlStr The sql unicode string.
 * @param sqlLen The length of the sql string.
 * @param isSelect Indicates if a sql command is a select.
 * @param heap A heap to allocate the parser structure.
 * @return A pointer to a <code>LitebaseParser</code> structure if the string does not have parser errors or <code>null</code>, otherwise.
 */
LitebaseParser* initLitebaseParser(Context context, JCharP sqlStr, int32 sqlLen, bool isSelect, Heap heap);

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
 * @param isAscending Indicates if the order by or group by sorting is in ascending or descending order.
 * @param isOrderBy Indicates if the field list where to add a list is a order or group by.
 * @param parser A pointer to the parser structure, which contains the list(s).
 * @return <code>true</code> if the number of fields has not reached its limit; <code>false</code>, otherwise.
 */
bool addColumnFieldOrderGroupBy(bool isAscending, bool isOrderBy, LitebaseParser* parser);

/**
 * Gets a especific boolean clause: it will get a where clause if there is a where clause; otherwise, it will get a having clause.
 *
 * @param parser A pointer to the parser structure.
 * @return A pointer to a <code>SQLBooleanClause</code>.
 */
SQLBooleanClause* getInstanceBooleanClause(LitebaseParser* parser);

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

/**
 * The function that parses the sql string.
 *
 * @param parser The parser structure.
 * @return <code>true</code> if there are parser errors; <code>false</code>, otherwise. 
 */
bool yyparse(LitebaseParser* parser);

/**
 * Deals with a list of identifiers separated by commas.
 * 
 * @param parser The parser structure.
 * @return The token after the list of identifiers. 
 */
int32 colnameCommaList(LitebaseParser* parser);

/**
 * Deals with a list of rows of a table being created.
 * 
 * @param parser The parser structure.
 * @return The token after the list of rows. 
 */
int32 createColumnCommalist(LitebaseParser* parser); 

/**
 * Deals with a column declaration.
 * 
 * @param parser The parser structure.
 * @return The token after a column declaration. 
 */
int32 createColumn(LitebaseParser* parser);

/**
 * Deals with an expression of an expression tree of a where clause.
 * 
 * @param token The first token of the expression.
 * @param parser The parser structure.
 * @return The token after the expression.
 */
int32 expression(int32 token, LitebaseParser* parser);

/**
 * Deals with a term of an expression tree of a where clause.
 * 
 * @param token The first token of the term.
 * @param parser The parser structure.
 * @return The token after the term.
 */
int32 term(int32 token, LitebaseParser* parser);

/**
 * Deals with a factor of an expression tree of a where clause.
 * 
 * @param token The first token of the factor.
 * @param parser The parser structure.
 * @return The token after the factor.
 */
int32 factor(int32 token, LitebaseParser* parser);

/**
 * Deals with a single expression of an expression tree of a where clause.
 * 
 * @param token The first token of the single expression.
 * @param parser The parser structure.
 * @return The token after the single expression.
 */
int32 singleExp(int32 token, LitebaseParser* parser);

/**
 * Deals with a list of values of an insert.
 * 
 * @param parser The parser structure.
 * @return The token after the list of values.
 */
int32 listValues(LitebaseParser* parser);

/**
 * Deals with a table list of a select.
 * 
 * @param parser The parser structure.
 * @return The token after the list of tables.
 */
int32 tableList(LitebaseParser* parser);

/**
 * Deals with a list of expressions of a select.
 * 
 * @param parser The parser structure.
 * @return The token after the list of expressions.
 */
int32 fieldExp(int32 token, LitebaseParser* parser);

/**
 * Deals with a list of update expressions.
 * 
 * @param parser The parser structure.
 * @return The token after the list of update expressions.
 */
int32 updateExpCommalist(LitebaseParser* parser);

/**
 * Deals with a field.
 * 
 * @param token A token to be used by the field.
 * @param parser The parser structure.
 * @return The token after the field.
 */
int32 field(int32 token, LitebaseParser* parser);

/**
 * Deals with a pure field.
 * 
 * @param token A token to be used by the pure field.
 * @param parser The parser structure.
 * @return The token after the pure field.
 */
int32 pureField(int32 token, LitebaseParser* parser);

/**
 * Deals with a data function.
 * 
 * @param token A token witch is possibly a data function token.
 * @param parser The parser structure.
 * @return The next token or -1 if it is not a data function. 
 */
int32 dataFunction(int32 token, LitebaseParser* parser);

/**
 * Deals with a aggregation function.
 * 
 * @param token A token witch is possibly a data function token.
 * @param parser The parser structure.
 * @return The next token or -1 if it is not a data function. 
 */
int32 aggFunction(int32 token, LitebaseParser* parser);

/**
 * Deals with a possible where clause.
 * 
 * @param token The token where if it is a where clause.
 * @param parser The parser structure.
 * @return The token received if it is not a where clause or the token after the where clause.
 */
int32 optWhereClause(int32 token, LitebaseParser* parser);

/**
 * Deals with an order by clause.
 * 
 * @param parser The parser structure.
 * @return The first token after the order by clause.
 */
int32 orderByClause(LitebaseParser* parser);

/**
 * Deals with a group by clause.
 * 
 * @param parser The parser structure.
 * @return The first token after the group by clause.
 */
int32 groupByClause(LitebaseParser* parser);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the function <code>lbError()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_lbError(TestSuite* testSuite, Context currentContext);   

/**
 * Tests if the function <code>lbErrorWithMessage()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_lbErrorWithMessage(TestSuite* testSuite, Context currentContext);

/**
 * Tests if the function <code>initLitebaseParser()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_initLitebaseParser(TestSuite* testSuite, Context currentContext);

#endif

#endif
