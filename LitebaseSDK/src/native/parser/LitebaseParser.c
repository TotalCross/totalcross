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
 * Defines the functions to initialize, set, and process the parser structure.
 */

#include "LitebaseParser.h"

/**
 * Shows a parser error message when the error position is known.
 *
 * @param error An error code.
 * @param parser A pointer to the parser structure.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
void lbError(int32 error, LitebaseParser* parser)
{
   TRACE("lbError")
   char errorMessage[1024];
   xstrcpy(errorMessage, getMessage(ERR_MESSAGE_START));
   xstrcat(errorMessage, getMessage(error));
   xstrcat(errorMessage, getMessage(ERR_MESSAGE_POSITION));
   TC_throwExceptionNamed(parser->context, "litebase.SQLParseException", errorMessage, parser->yyposition);
}

/**
 * Shows a parser error message when the error position is unknown.
 *
 * @param error An error code.
 * @param extraMsg An extra error message.
 * @param parser A pointer to the parser structure.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
void errorWithoutPosition(int32 error, CharP extraMsg, LitebaseParser* parser)
{
   TRACE("errorWithoutPosition")
   char errorMessage[1024];
   xstrcpy(errorMessage, getMessage(ERR_MESSAGE_START));
   xstrcat(errorMessage, getMessage(error));
	TC_throwExceptionNamed(parser->context, "litebase.SQLParseException", errorMessage, extraMsg);
}

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
LitebaseParser* initLitebaseParser(Context context, JCharP sqlStr, int32 sqlLen, bool isSelect, Heap heap)
{
	TRACE("litebaseParser")
   
	// The parser only needs to be allocated once per context.
	LitebaseParser* parser = context->litebasePtr;
   if (parser)
      xmemzero(parser, sizeof(LitebaseParser));
   else if (!(parser = context->litebasePtr = (LitebaseParser*)xmalloc(sizeof(LitebaseParser))))
	   return null;
   
	// Initializes some parser structures.
	parser->heap = heap;
	parser->zzReaderChars = sqlStr;
	parser->length = sqlLen;
	parser->select.type = -1;
   parser->isWhereClause = true;
   parser->context = context;
   parser->yycurrent = ' ';
   if (isSelect)
      parser->tables = TC_htNew(MAXIMUMS, heap);

   // Does the parsing.
   if (yyparse(parser))
      return null;
  
   return parser;
}

/**
 * Sets the operand type. 
 *
 * @param operandType The type of the operand.
 * @param parser A pointer to the parser structure.
 * @return A pointer to a <code>SQLBooleanTree</code> structure for a expression tree.
 */
SQLBooleanClauseTree* setOperandType(int32 operandType, LitebaseParser* parser)
{
	TRACE("setOperandType")
   SQLBooleanClauseTree* tree = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), parser->heap);
   tree->operandType = operandType;
   return tree;
}

/** 
 * Adds a column field to the order or group by field list. 
 *
 * @param field The field to be added to the list.
 * @param isAscending Indicates if the order by or group by sorting is in ascending or descending order.
 * @param isOrderBy Indicates if the field list where to add a list is a order or group by.
 * @param parser A pointer to the parser structure, which contains the list(s).
 * @return <code>true</code> if the number of fields has not reached its limit; <code>false</code>, otherwise.
 */
bool addColumnFieldOrderGroupBy(SQLResultSetField* field, bool isAscending, bool isOrderBy, LitebaseParser* parser)
{
	TRACE("addColumnFieldOrderGroupBy")
   int32 hash;
	SQLColumnListClause* listClause = isOrderBy? &parser->order_by : &parser->group_by;
	SQLResultSetField** fieldList = isOrderBy? parser->orderByfieldList : parser->groupByfieldList;

	// The number of fields has reached the maximum.
   if (listClause->fieldsCount == MAXIMUMS)
   {
      errorWithoutPosition(ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY, "", parser);
      return false;
   }

	// Sets the field.
   hash = TC_hashCode(field->tableColName);
   field->tableColHashCode = hash;
   field->aliasHashCode = hash;
   field->isAscending = isAscending;
   fieldList[listClause->fieldsCount++] = field;
   return true;
}

/**
 * Gets a especific boolean clause: it will get a where clause if there is a where clause; otherwise, it will get a having clause.
 *
 * @param parser A pointer to the parser structure.
 * @return A pointer to a <code>SQLBooleanClause</code>.
 */
SQLBooleanClause* getInstanceBooleanClause(LitebaseParser* parser)
{
	TRACE("getInstanceBooleanClause")
   if (parser->isWhereClause) // It is a where clause.
   {
      if (!parser->whereClause)
			parser->whereClause = initSQLBooleanClause(parser->heap);
      return parser->whereClause;
   }
   if (!parser->havingClause) // It is a having clause.
      parser->havingClause = initSQLBooleanClause(parser->heap);
   return parser->havingClause;
}

/**
 * Sets an expression tree in a where clause.
 *
 * @param tree An expression tree.
 * @param parser A pointer to the parser structure.
 */
void setBooleanClauseTreeOnWhereClause(SQLBooleanClauseTree* tree, LitebaseParser* parser)
{
	TRACE("setBooleanClauseTreeOnWhereClause")
   parser->whereClause->expressionTree = parser->whereClause->origExpressionTree = tree;
   parser->whereClause->isWhereClause = true; // It indicates that it is a where clause.
}

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
													                                                                                               Heap heap)
{	
	TRACE("initSQLFieldDefinition")
   SQLFieldDefinition *sqlFieldDefinition = (SQLFieldDefinition*)TC_heapAlloc(heap, sizeof(SQLFieldDefinition));
   sqlFieldDefinition->fieldName = fieldName;
   sqlFieldDefinition->fieldType = fieldType;
   sqlFieldDefinition->fieldSize = fieldSize;
   sqlFieldDefinition->isPrimaryKey = isPrimaryKey;
   sqlFieldDefinition->defaultValue = defaultValue;
   sqlFieldDefinition->isNotNull = isNotNull;
	return sqlFieldDefinition;
}

/**
 * Initializes a result set field.
 *
 * @param heap A heap to alocate the result set field structure.
 * @return A pointer to a result set field structure.
 */
SQLResultSetField* initSQLResultSetField(Heap heap)
{
	TRACE("initSQLResultSetField")
   SQLResultSetField* sqlResultSetField = (SQLResultSetField*)TC_heapAlloc(heap, sizeof(SQLResultSetField));
   sqlResultSetField->tableColIndex = -1;
   sqlResultSetField->dataType = UNDEFINED_TYPE;
   sqlResultSetField->sqlFunction = FUNCTION_AGG_NONE;
   sqlResultSetField->isAscending = 1;
	return sqlResultSetField;
}

/**
 * Initializes a result set table structure using a table name and its optional alias name.
 *
 * @param tableName the name of the new table.
 * @param aliasTableName the optional alias name of the new table (it can be null).
 * @return a pointer to the new result set table structure.
 */
SQLResultSetTable* initSQLResultSetTable(CharP tableName, CharP aliasTableName, Heap heap)
{
	TRACE("initSQLResultSetTable")

	// Allocates the table structure.
   SQLResultSetTable* sqlResultSetTable = (SQLResultSetTable*)TC_heapAlloc(heap, sizeof(SQLResultSetTable));
   
	sqlResultSetTable->tableName = tableName; // Sets the table name.

	// If the alias is null, it receives the name of the table. If the alias is not null, the alias name is set with the alias passed as a parameter.
	sqlResultSetTable->aliasTableNameHashCode = TC_hashCode((sqlResultSetTable->aliasTableName = (!aliasTableName) ? tableName : aliasTableName));
   
	return sqlResultSetTable;
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the function <code>lbError()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(lbError)
{
   LitebaseParser* parser = (LitebaseParser*)xmalloc(sizeof(LitebaseParser));
   
   parser->context = currentContext;
   lbError(2, parser);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;
   xfree(parser);
finish: ;
}   

/**
 * Tests if the function <code>errorWithoutPosition()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(errorWithoutPosition)
{
   LitebaseParser* parser = (LitebaseParser*)xmalloc(sizeof(LitebaseParser));
   
   parser->context = currentContext;
   errorWithoutPosition(2, "", parser);
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;
   xfree(parser);
finish: ;
}

/**
 * Tests if the function <code>initLitebaseParser()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(initLitebaseParser)
{
   Heap heap = heapCreate();
   JChar buffer[400];
   CharP string;
   int32 length;

   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      TEST_FAIL(tc, "OutOfMemoryError");
      goto finish;
   }
   
   // Null string.
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, null, 0, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   // Empty string.
   buffer[0] = 0;
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, 0, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   TC_CharP2JCharPBuf(" ", 1, buffer, true);
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, 1, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   TC_CharP2JCharPBuf("  ", 2, buffer, true);
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, 2, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   string = "create table bookentry(name char(30), address char(50), phone char(20), birthday int, salary float, married short, gender short, lastUpdated long)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "CREATE INDEX IDX_0 ON bookentry(rowid)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select rowid, name, address, phone, birthday, salary, married, gender, lastUpdated from bookentry";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "delete bookentry where rowid = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string ="insert into bookentry values (?, ?, ?, ?, ?, ?, ?, ?)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update bookentry set name = ?, address = ?, phone = ?, birthday = ?, salary = ?, married = ?, gender = ?, lastUpdated = ? where rowid = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from bookentry where rowid = ?"; 
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table photodb(name char(20), photo blob(16384))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into photodb values (?,?)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select photo from photodb where name = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table person";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   
   string = "create table PERSON (NAME CHAR(8))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   
   string = "insert into person values (?)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   
   string = "insert into person values ('a')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from person where name = 'a'";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   
   string = "CREATE INDEX IDX_NAME ON PERSON(NAME)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from person";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select count(*) as number from person";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select max(rowid) as number from person";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "CREATE table PERSON (NAME CHAR(10))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person values ('Juliana')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create index idx on person(name)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table person (name char(10))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table person1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table person1 (name char(10) primary key)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table person2";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table person2 (name char(10) primary key)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person1 values ('Name')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person2 values ('Name')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person1 values ('Name100')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person2 values ('Name100')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select rowid, name from person1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select rowid, name from person2";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table person2 (name char(1) primary key)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person2 values('')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person2 values('\\'')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person2 values('\\'A')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from person2";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from person2 where name like '\\''";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table person2 (name char(2) primary key)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into person2 values('\\'AA')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from person2 where name like '\\'%'";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table blob0";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table blob1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table blob2";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table blob3";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob0 (value blob(10 G))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   string = "create table blob0 (value blob(11 M))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   string = "create table blob1 (value blob(100) not null)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob2 (name varchar(10), picture blob(100 K))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob3 (name varchar(10), id int, video blob(1 M))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob0 (value blob(100) primary key)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   string = "create table blob0 (value blob(100), primary key(value))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob0 (value blob(100), age int, primary key(value, age))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob0 (value blob(100), age int, primary key(age, value))";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table blob0 (value blob(100) default null, age int)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(Null, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(NotNull, currentContext->thrownException);
   currentContext->thrownException = null;

   string = "create index idx on blob1(value)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create index idx on blob1(value, rowid)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "alter table blob2 add primary key (picture)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "alter table blob2 add primary key (name, picture)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob1 where value > 100";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select value from blob1 order by value";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   
   string = "select value from blob1 group by value";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into blob1 values (1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into blob1 values ('a')";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update blob1 set value = 3 where rowid = 1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob1 where value = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "delete from blob1 where value = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "Insert into blob1 values (?)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into blob2 (picture, name) values (?, ?)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into blob3 (id, video, name) values (?, ?, ?)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create index idx on blob2(name)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob2";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob3";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update blob3 set video = ? where rowid = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update blob1 set value = ? where rowid = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update blob2 set picture = ? where rowid = ?";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select value from blob1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select picture from blob2";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select video from blob3";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);
   
   string = "select * from blob2 where rowid = 11";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob2 where rowid = 12";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "insert into blob2 (picture, name) values (null, 0)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob2 where rowid = 13";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob3 where rowid = 10";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update blob3 set video = null where rowid = 10";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob2 where rowid = 14";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "update blob2 set picture = ?, name = ? where rowid = 1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob2 where name = ''";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob3 order by id desc";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from blob2 where rowid = 1";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 38";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (38L, '38', 'ADENILZA xxxxxxxxxxxxxxxxxxxxxE', 'ADENILZA 111111111111111111111E', '11.111.222//4443-22', '', 1, 1, '', 4, '3423423421', '', '', '1', 20051110101123, 20051110101123, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 114L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (114L, '114', 'NIETO yyyyyyyyyyyyyyyyyyyyE', 'NIETO 2222222222222222222EE', '22.222.333//3333-33', '', 1, 1, '', 4, '4342342423', '', '', '1', 20051110101123, 20051110101123, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 161L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (161L, '161', 'ANTONIO bbbbbbbbbbbbbbbbbbbbbbE', 'ANTONIO 33333333333333333333333', '44.444.444//4441-44', '', 1, 1, '', 4, '5435656458', '', '', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);   
   
   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 421L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (421L, '421', 'CLAUDIOMIR cccccccccccccccccMEE', 'CLAUDIOMIR 444444444444444444EE', '55.555.555//5555-26', '', 1, 1, '', 4, '', '', '', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 443L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (443L, '443', 'MARIA dddddddddddddddddSO', 'MARIA 55555555555555555SO', '777.777.777-20', '', 1, 1, '', 4, '6756756756', '', '', '2', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 941L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (941L, '941', 'J.B.eeeeeeeeeeeeeeeeeeeeeeeeeeE', 'J.B.6666666666666666666666666EE', '88.888.888//8889-83', '', 1, 1, '', 4, '8655676565', '', '', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);   
   
   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 968L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (968L, '968', 'JARDELIO fffffffffffffffffffffffE', 'JARDELIO 7777777777777777777777EE','99.999.999//9999-82', '', 1, 1, '', 4, '7656456547', '', '', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1217L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (1217L, '1217', 'N.gggggggggggggggE', 'N.C.8888888888888E', '00.000.000//1111-16', '', 1, 1, '', 4, '4532432439', '', '', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1450L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (1450L, '1450', 'OTTO COMhhhhhhhhhhhhhhhhhhhhhhhE', 'OTTO 9999999999999999999999999EE', '22.222.222//2222-17', '', 1, 1, '', 4, '4535345340', '', '', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1585L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "INSERT INTO ACT_CLIENTE VALUES (1585L, '1585', 'OROSINO iiiiiiiiiiiiiiiiiiiE', 'OROSINO 0000000000000000000E', '33.333.333//3333-33', '', 1, 1, '', 4, '5435345357', '', '30        ', '1', 20051110101124, 20051110101124, 1)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "DELETE ACT_CLIENTE WHERE ACTCLIENTEID = 1664L";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "drop table act_cliente";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "create table act_cliente (actclienteid long, actcodcliente char(30), actrazaosocial char(100), actnomefantasia char(100), actie char(20), actcnpj char(20), actstatus long, acttipocliente long, actmail char(255), actvendedorid long, acttelefone char(18), actobservacao char(255), actfax char(18), actcodtabela char(20), actdatinclusao long, actdatalteracao long, actusuarioid long)";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, false, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);   

   string = "select actrazaosocial from act_cliente where actrazaosocial='ADENILZA xxxxxxxxxxxxxxxxxxxxxE'";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   string = "select * from act_cliente";
   TC_CharP2JCharPBuf(string, length = xstrlen(string), buffer, true);
   ASSERT1_EQUALS(NotNull, initLitebaseParser(currentContext, buffer, length, true, heap));
   ASSERT1_EQUALS(Null, currentContext->thrownException);

   xfree(currentContext->litebasePtr);
   heapDestroy(heap);

finish: ;
}

#endif
