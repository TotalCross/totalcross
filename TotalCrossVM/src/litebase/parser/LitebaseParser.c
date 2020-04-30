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
 * Defines the functions to initialize, set, and process the parser structure.
 */

// juliana@253_9: improved Litebase parser.

#include "LitebaseParser.h"

/**
 * Shows a parser error message without an extra message.
 *
 * @param error An error code.
 * @param parser A pointer to the parser structure.
 * @return <code>PARSER_ERROR<code> to indicate that an error has occurred.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
int32 lbError(int32 error, LitebaseParser* parser)
{
   TRACE("lbError")
   char errorMessage[1024];
   xstrcpy(errorMessage, getMessage(ERR_MESSAGE_START));
   xstrcat(errorMessage, getMessage(error));
   xstrcat(errorMessage, getMessage(ERR_MESSAGE_POSITION));
   TC_throwExceptionNamed(parser->context, "litebase.SQLParseException", errorMessage, parser->yyposition);
   return PARSER_ERROR;
}

/**
 * Shows a parser error message with an extra message.
 *
 * @param error An error message.
 * @param message An extra error message.
 * @param parser A pointer to the parser structure.
 * @return <code>PARSER_ERROR<code> to indicate that an error has occurred.
 * @throws SQLParseException To throw an exception if the error message that occurred.
 */
int32 lbErrorWithMessage(CharP error, CharP message, LitebaseParser* parser)
{
   TRACE("lbErrorWithMessage")
   char errorMessage[1024];
   xstrcpy(errorMessage, getMessage(ERR_MESSAGE_START));
   xstrcat(errorMessage, error);
   xstrcat(errorMessage, getMessage(ERR_MESSAGE_POSITION));
   TC_throwExceptionNamed(parser->context, "litebase.SQLParseException", errorMessage, message, parser->yyposition);
   return PARSER_ERROR;
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
	{
      TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
      return null;
   }
   
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
 * @param isAscending Indicates if the order by or group by sorting is in ascending or descending order.
 * @param isOrderBy Indicates if the field list where to add a list is a order or group by.
 * @param parser A pointer to the parser structure, which contains the list(s).
 * @return <code>true</code> if the number of fields has not reached its limit; <code>false</code>, otherwise.
 */
bool addColumnFieldOrderGroupBy(bool isAscending, bool isOrderBy, LitebaseParser* parser)
{
	TRACE("addColumnFieldOrderGroupBy")
   int32 hash;
	SQLColumnListClause* listClause = isOrderBy? &parser->orderBy : &parser->groupBy;
	SQLResultSetField** fieldList = isOrderBy? parser->orderByfieldList : parser->groupByfieldList;
   SQLResultSetField* field = parser->auxField;

	// The number of fields has reached the maximum.
   if (listClause->fieldsCount == MAXIMUMS)
   {
      lbError(ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY, parser);
      return false;
   }

	// Sets the field.
   field->tableColHashCode = hash = TC_hashCode(field->tableColName);
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

/**
 * The function that parses the sql string.
 *
 * @param parser The parser structure.
 * @return <code>true</code> if there are parser errors; <code>false</code>, otherwise. 
 */
bool yyparse(LitebaseParser* parser)
{
   TRACE("yyparse")
   int32 token = PARSER_EOF;
   CharP tableName;
   
   switch (yylex(parser))
   {
      case TK_ALTER: // Alter table.
         if (yylex(parser) != TK_TABLE || yylex(parser) != TK_IDENT)
            return lbError(ERR_SYNTAX_ERROR, parser);
         parser->tableList[0] = initSQLResultSetTable(parser->yylval, null, parser->heap); // There's no alias table name here.
         
         switch (yylex(parser))
         {
            case TK_ADD: // Adds a primary key or a new column.

                  // juliana@253_22: added command ALTER TABLE ADD column.
                  if ((token = createColumn(parser)) == TK_PRIMARY) // Adds a primary key.
                  {
                     if (parser->fieldListSize == 1)
                        return lbError(ERR_SYNTAX_ERROR, parser);
                     if (yylex(parser) != TK_KEY || yylex(parser) != TK_OPEN || colnameCommaList(parser) != TK_CLOSE)
                        return lbError(ERR_SYNTAX_ERROR, parser);                
                     parser->command = CMD_ALTER_ADD_PK;
                  }
                  else if (token == -1) // Adds a new column.
                  {
                     SQLFieldDefinition* field = parser->fieldList[0];
                     if (field->isNotNull && !field->defaultValue) // A field declared as not null must have a default value.
                        return lbError(ERR_NOT_NULL_DEFAULT, parser);
                     if (field->isPrimaryKey) // The new field can't be declared as a primary key when being added.
                     {   
                        if (field->defaultValue) // All the keys would be the same.
                           return lbErrorWithMessage(getMessage(ERR_STATEMENT_CREATE_DUPLICATED_PK), parser->tableList[0]->tableName, parser);
                        return lbError(ERR_PK_CANT_BE_NULL, parser); // All the keys would be null.
                     }
                     parser->command = CMD_ALTER_ADD_COLUMN;
                  }
                  break;
            
            case TK_DROP: // Drops a primary key.
               if (yylex(parser) != TK_PRIMARY || yylex(parser) != TK_KEY)
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->command = CMD_ALTER_DROP_PK;
               break;
            
            case TK_RENAME: // Renames the table or a column.
               if ((token = yylex(parser)) == TK_IDENT) // Rename column.
               {
                  parser->command = CMD_ALTER_RENAME_COLUMN;
                  parser->fieldNames[1] = parser->yylval;
                  token = yylex(parser);
               }
               else // Rename table.
                  parser->command = CMD_ALTER_RENAME_TABLE;
               
               // New name.
               if (token != TK_TO || yylex(parser) != TK_IDENT) 
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->fieldNames[0] = parser->yylval;
               
               break;
            
            default:
               return lbError(ERR_SYNTAX_ERROR, parser);
         }
         
         token = yylex(parser);
         break;
                     
      case TK_CREATE:
      {
         switch (yylex(parser))
         {
            case TK_TABLE: // Create table.
               if (yylex(parser) != TK_IDENT || yylex(parser) != TK_OPEN)
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->tableList[0] = initSQLResultSetTable(parser->yylval, null, parser->heap); // There's no alias table name here.
               
               // Primary key.
               if ((token = createColumnCommalist(parser)) == TK_PRIMARY && yylex(parser) == TK_KEY && yylex(parser) == TK_OPEN && colnameCommaList(parser) == TK_CLOSE)
               {
                  if (parser->numberPK == 1)
                     return lbError(ERR_PRIMARY_KEY_ALREADY_DEFINED, parser);
                  token = yylex(parser);
               }
               
               if (token != TK_CLOSE) // End of create table.
                  return lbError(ERR_SYNTAX_ERROR, parser);                  

               parser->command = CMD_CREATE_TABLE;
               break;
               
            case TK_INDEX: // Create index.
               if (yylex(parser) != TK_IDENT || yylex(parser) != TK_ON || yylex(parser) != TK_IDENT)
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->tableList[0] = initSQLResultSetTable(parser->yylval, null, parser->heap); // There's no alias table name here.
               if (yylex(parser) != TK_OPEN || colnameCommaList(parser) != TK_CLOSE) // Column name list.
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->command = CMD_CREATE_INDEX;
               break;
               
            default:
               return lbError(ERR_SYNTAX_ERROR, parser);
         }  
         
         token = yylex(parser);
         break;
      }
      case TK_DELETE: // Delete.
         if (!(((token = yylex(parser)) == TK_FROM && yylex(parser) == TK_IDENT) || token == TK_IDENT))
            return lbError(ERR_SYNTAX_ERROR, parser);
         tableName = parser->yylval;
         
         if ((token = yylex(parser)) == TK_IDENT) // Alias table name.
         {
            parser->tableList[0] = initSQLResultSetTable(tableName, parser->yylval, parser->heap);
            token = yylex(parser);
         }
         else 
            parser->tableList[0] = initSQLResultSetTable(tableName, null, parser->heap);
         
         token = optWhereClause(token, parser); // Where clause.
         parser->command = CMD_DELETE;
         break;
      
      case TK_DROP:
         switch (yylex(parser))
         {
            case TK_TABLE: // Drop table.
               if (yylex(parser) == TK_IDENT)
                  parser->tableList[0] = initSQLResultSetTable(parser->yylval, null, parser->heap); // There's no alias table name here.
               else
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->command = CMD_DROP_TABLE;
               break;
            
            case TK_INDEX: // Drop index.                             
               if ((token = colnameCommaList(parser)) != TK_ON || yylex(parser) != TK_IDENT) 
                  return lbError(ERR_SYNTAX_ERROR, parser);
               parser->tableList[0] = initSQLResultSetTable(parser->yylval, null, parser->heap); // There's no alias table name here.                     
               parser->command = CMD_DROP_INDEX;
               break;
            
            default:
               return lbError(ERR_SYNTAX_ERROR, parser);
         } 
         
         token = yylex(parser);
         break;
         
      case TK_INSERT: // Insert.
         if (yylex(parser) != TK_INTO || yylex(parser) != TK_IDENT)
            return lbError(ERR_SYNTAX_ERROR, parser);
         parser->tableList[0] = initSQLResultSetTable(parser->yylval, null, parser->heap); // There's no alias table name here.
                     
         if ((token = yylex(parser)) == TK_OPEN) // Reads the field list.
         {
            if (colnameCommaList(parser) != TK_CLOSE)
               return lbError(ERR_SYNTAX_ERROR, parser);   
            token = yylex(parser);
         }
         
         if (token != TK_VALUES || yylex(parser) != TK_OPEN || listValues(parser) != TK_CLOSE) // Reads the value list.
            return lbError(ERR_SYNTAX_ERROR, parser);
         
         // If the default order is not used, the number of values must be equal to the number of fields.
         if (parser->fieldNamesSize && parser->fieldNamesSize != parser->fieldValuesSize) 
         {
            char error[MAXIMUMS];
            xstrprintf(error, "%s (%d != %d) ", getMessage(ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH), parserTP->fieldNamesSize, 
                                                                                                        parserTP->fieldValuesSize);
            xstrcat(error, "%s .");
			return lbErrorWithMessage(error, "", parser);
         }
         parser->command = CMD_INSERT;
         token = yylex(parser);
         break;
         
      case TK_SELECT: // Select.
         if ((token = yylex(parser)) == TK_DISTINCT) 
            token = yylex(parser);
         if (fieldExp(token, parser) != TK_FROM)
            return lbError(ERR_SYNTAX_ERROR, parser);
         
         token = optWhereClause(tableList(parser), parser); // Table list and where clause.
         
         // order by and group by.
         if (token == TK_GROUP) 
         {
            if (yylex(parser) != TK_BY)
               return lbError(ERR_SYNTAX_ERROR, parser);
            token = groupByClause(parser);
         } 
         if (token == TK_ORDER) 
         {
            if (yylex(parser) != TK_BY)
               return lbError(ERR_SYNTAX_ERROR, parser);
            token = orderByClause(parser);
         }
         
         parser->command = CMD_SELECT;

         // Checks if the first field is the wild card. If so, assigns null to list, to indicate that all fields must be included.
         if (parser->selectFieldList[0]->isWildcard)
            parser->select.fieldsCount = 0;
         break;
   
      case TK_UPDATE: // Update.
      {
         CharP tableAlias = null;
         
         // Table name.
         if (yylex(parser) != TK_IDENT)
            return lbError(ERR_SYNTAX_ERROR, parser);
         tableAlias = tableName = parser->yylval;
         
         if ((token = yylex(parser)) == TK_IDENT) // Alias table name.
         {   
            tableAlias = parser->yylval;
            token = yylex(parser);
         }
         
         if (token != TK_SET) // set key word.
            return lbError(ERR_SYNTAX_ERROR, parser);
         
         token = optWhereClause(updateExpCommalist(parser), parser); // Update expression list and where clause.
         
         if (parser->secondFieldUpdateTableName) // Verifies if there was an error on field.tableName.
            return lbErrorWithMessage(getMessage(ERR_INVALID_COLUMN_NAME), xstrcmp(tableName, parser->firstFieldUpdateTableName)? 
                                      parser->firstFieldUpdateAlias : parser->secondFieldUpdateAlias, parser);
         else if (parser->firstFieldUpdateTableName && xstrcmp(tableAlias, parser->firstFieldUpdateTableName))
            return lbErrorWithMessage(getMessage(ERR_INVALID_COLUMN_NAME), parser->firstFieldUpdateAlias, parser);

         parser->command = CMD_UPDATE;
         parser->tableList[0] = initSQLResultSetTable(tableName, tableAlias, parser->heap);
         break;
      }
      
      default:
         return lbError(ERR_SYNTAX_ERROR, parser);        
   }
   
   if (token != PARSER_EOF)
      return lbError(ERR_SYNTAX_ERROR, parser); 
   return false; 
}

/**
 * Deals with a list of identifiers separated by commas.
 * 
 * @param parser The parser structure.
 * @return The token after the list of identifiers. 
 */
int32 colnameCommaList(LitebaseParser* parser)
{
   int32 token,
         fieldNamesSize = parser->fieldNamesSize;
   CharP* fieldNames = parser->fieldNames;
   
   do
   {
      if ((token = yylex(parser)) == TK_ASTERISK) // This is necessary for dropping all indices.
      {
         fieldNames[fieldNamesSize++] = "*";
         return yylex(parser);
      }
      
      if (token != TK_IDENT)
         return lbError(ERR_SYNTAX_ERROR, parser);
      if (fieldNamesSize == MAXIMUMS)
         return lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
      fieldNames[fieldNamesSize++] = parser->yylval; // Adds the column name.  
   }
   while ((token = yylex(parser)) == TK_COMMA);          
   parser->fieldNamesSize = fieldNamesSize;
   return token;
}

/**
 * Deals with a list of rows of a table being created.
 * 
 * @param parser The parser structure.
 * @return The token after the list of rows. 
 */
int32 createColumnCommalist(LitebaseParser* parser) 
{
   int32 token;
   
   while ((token = createColumn(parser)) == TK_COMMA);
   if (!parser->fieldListSize) // The number of columns can't be zero.
      return lbError(ERR_SYNTAX_ERROR, parser);

   return token;      
}

/**
 * Deals with a column declaration.
 * 
 * @param parser The parser structure.
 * @return The token after a column declaration. 
 */
int32 createColumn(LitebaseParser* parser) 
{
   int32 token,
         type,
         size = 0;
   bool isPrimaryKey = false,
        isNotNull = false;
   CharP columnName;
   JCharP strDefault = null;
         
   if ((token = yylex(parser)) == TK_PRIMARY) // The next token after ',' is a primary key declaration. This is not treated here.
      return token;
   
   // Column name.
   if (token != TK_IDENT)
      return lbError(ERR_SYNTAX_ERROR, parser);
   columnName = parser->yylval;
   
   // Column type.
   if ((type = yylex(parser)) == BOOLEAN_TYPE || type > BLOB_TYPE || type < CHARS_TYPE)
      return lbError(ERR_SYNTAX_ERROR, parser);
   if (type == TK_VARCHAR)
      type = CHARS_TYPE;
   
   if (type == CHARS_TYPE || type == BLOB_TYPE) // Size and multiplier. 
   {
      if (yylex(parser) == TK_OPEN && yylex(parser) == TK_NUMBER)
      {
         bool error;  
         IntBuf buffer;       
         if ((size = TC_str2int(TC_JCharP2CharPBuf((JCharP)parser->yylval, -1, buffer), &error)) <= 0 || error)
            return lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
      }
      else
         return lbError(ERR_SYNTAX_ERROR, parser);
      
      if (type == CHARS_TYPE && yylex(parser) != TK_CLOSE)
         return lbError(ERR_SYNTAX_ERROR, parser);
      else if (type == BLOB_TYPE) 
      {   
         if ((token = yylex(parser)) != TK_IDENT && token != TK_CLOSE)
            return lbError(ERR_SYNTAX_ERROR, parser);   
         if (token == TK_IDENT)
         {
            CharP multiplier = (CharP)parser->yylval;
            if (multiplier[0] == 'k' && !multiplier[1]) // kilobytes.
               size <<= 10;
            else if (multiplier[0] == 'm' && !multiplier[1]) // megabytes.
               size <<= 20;
            else
               return lbError(ERR_INVALID_MULTIPLIER, parser);
            if (yylex(parser) != TK_CLOSE)
               return lbError(ERR_SYNTAX_ERROR, parser);     
         }
         if (size > (10 << 20))  // There is a size limit for a blob!
            return lbError(ERR_BLOB_TOO_BIG, parser);
      }  
      
      // juliana@253_15: now an exception is thrown if the size of a CHAR or VARCHAR is greater than 65535. 
      else if (type == CHARS_TYPE && size > (MAX_SHORT_VALUE << 1) + 1)
         return lbErrorWithMessage(getMessage(ERR_INVALID_NUMBER), "unsigned short", parser);                
   }   
   
   if ((token = yylex(parser)) == TK_NOCASE) // No case.
   {   
      if (type == CHARS_TYPE)
         type = CHARS_NOCASE_TYPE;
      else
         return lbError(ERR_SYNTAX_ERROR, parser);
      token = yylex(parser);
   }

   if (token == TK_PRIMARY) // Simple primary key.
   {
      if (yylex(parser) != TK_KEY)
         return lbError(ERR_SYNTAX_ERROR, parser);
      if (parser->numberPK++ == 1)
         return lbError(ERR_PRIMARY_KEY_ALREADY_DEFINED, parser);
      if (type == BLOB_TYPE)
         return lbError(ERR_BLOB_PRIMARY_KEY, parser);
      token = yylex(parser);
      isPrimaryKey = true;
   }
   
   if (token == TK_DEFAULT) // Default value.
   {
      if ((token = yylex(parser)) == TK_NUMBER || token == TK_STR)
         strDefault = parser->yylval;
      else if (token != TK_NULL)
         return lbError(ERR_SYNTAX_ERROR, parser);
      
      if (type == BLOB_TYPE) // A blob can't have a default value.
         return lbError(ERR_BLOB_STRING, parser);
      
      // A numeric type must have a number as a default value. A string, date or datetime type must have a string as a default value.
      if (((type == CHARS_TYPE || type == CHARS_NOCASE_TYPE || type == DATE_TYPE || type == DATETIME_TYPE) && token == TK_NUMBER)
        || ((type > CHARS_TYPE && type < CHARS_NOCASE_TYPE) && token == TK_STR))
         return lbError(ERR_SYNTAX_ERROR, parser);

      token = yylex(parser);         
   }
   
   if (token == TK_NOT) // Not null.
   {
      if (yylex(parser) != TK_NULL)
         return lbError(ERR_SYNTAX_ERROR, parser);
      token = yylex(parser);
      isNotNull = true;
   }
   if (parser->fieldListSize == MAXIMUMS)
      return lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
   parser->fieldList[parser->fieldListSize++] = initSQLFieldDefinition(columnName, type, size, isPrimaryKey, strDefault, isNotNull, parser->heap);
   return token;
}

/**
 * Deals with an expression of an expression tree of a where clause.
 * 
 * @param token The first token of the expression.
 * @param parser The parser structure.
 * @return The token after the expression.
 */
int32 expression(int32 token, LitebaseParser* parser) 
{     
   if ((token = term(token, parser)) == TK_OR) // expression = term or expression | term
   {
      // juliana@213_1: changed the way a tree with ORs is built in order to speed up queries with indices.
      SQLBooleanClauseTree* tree = setOperandType(OP_BOOLEAN_OR, parser);      
      (tree->rightTree = parser->auxTree)->parent = tree;
      token = expression(yylex(parser), parser);
      (tree->leftTree = parser->auxTree)->parent = tree;         
      parser->auxTree = tree;   
   }

   return token;
}

/**
 * Deals with a term of an expression tree of a where clause.
 * 
 * @param token The first token of the term.
 * @param parser The parser structure.
 * @return The token after the term.
 */
int32 term(int32 token, LitebaseParser* parser)
{      
   if ((token = factor(token, parser)) == TK_AND) // term = factor or factor | term
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_BOOLEAN_AND, parser);
      (tree->rightTree = parser->auxTree)->parent = tree;
      token = term(yylex(parser), parser);
      (tree->leftTree = parser->auxTree)->parent = tree;
      parser->auxTree = tree;
   }

   return token;
}

/**
 * Deals with a factor of an expression tree of a where clause.
 * 
 * @param token The first token of the factor.
 * @param parser The parser structure.
 * @return The token after the factor.
 */
int32 factor(int32 token, LitebaseParser* parser)
{
   SQLBooleanClauseTree* tree = null;
   SQLBooleanClauseTree* rightTree; 
   SQLBooleanClause* booleanClause;
   
   if (token == TK_OPEN) // factor = (expression)
   {
      if ((token = expression(yylex(parser), parser)) != TK_CLOSE)
         return lbError(ERR_SYNTAX_ERROR, parser);
      return yylex(parser);
   }
   
   if (token == TK_NOT)
   {
      if ((token = yylex(parser)) == TK_OPEN) // factor = not (expression)
      {
         if ((token = expression(yylex(parser), parser)) != TK_CLOSE)
            return lbError(ERR_SYNTAX_ERROR, parser);
         token = yylex(parser);
      }
      else // fator = not factor
         token = factor(token, parser);
      
      // The parent node will be the negation operator and the expression will be the right tree.
      tree = setOperandType(OP_BOOLEAN_NOT, parser);
      (tree->rightTree = parser->auxTree)->parent = tree;
      parser->auxTree = tree;
      
      return token;
   }
   
   // factor = single expression (< | > | = | <> | != | <= | >=) single expression
   if ((token = singleExp(token, parser)) == TK_EQUAL || token == TK_LESS || token == TK_DIFF || token == TK_GREATER || token == TK_GREATER_EQUAL 
    || token == TK_LESS_EQUAL)         
   {
      switch (token)
      {
         case TK_LESS:
            tree = setOperandType(OP_REL_LESS, parser);
            break;
         case TK_EQUAL:
            tree = setOperandType(OP_REL_EQUAL, parser);
            break;
         case TK_GREATER:
            tree = setOperandType(OP_REL_GREATER, parser);
            break;
         case TK_GREATER_EQUAL:
            tree = setOperandType(OP_REL_GREATER_EQUAL, parser);
            break;
         case TK_LESS_EQUAL:
            tree = setOperandType(OP_REL_LESS_EQUAL, parser);
            break;
         case TK_DIFF:
            tree = setOperandType(OP_REL_DIFF, parser);
      }
      (tree->leftTree = parser->auxTree)->parent = tree;
      token = singleExp(yylex(parser), parser);
      (tree->rightTree = parser->auxTree)->parent = tree;
      parser->auxTree = tree;
      return token;
   }
   
   if (token == TK_IS) // factor = single expression is [not] null.
   {
      if ((token = yylex(parser)) == TK_NOT)
      {
         tree = setOperandType(OP_PAT_IS_NOT, parser);
         token = yylex(parser);
      }
      else
         tree = setOperandType(OP_PAT_IS, parser);
      if (token != TK_NULL)
         return lbError(ERR_SYNTAX_ERROR, parser);  
      
      (tree->rightTree = setOperandType(OP_PAT_NULL, parser))->parent = (tree->leftTree = parser->auxTree)->parent = tree;
      parser->auxTree = tree;
      
      return yylex(parser);
   }
   
   if (token == TK_NOT) // factor = single expression not like [string | ?]
   {
      token = yylex(parser);
      tree = setOperandType(OP_PAT_MATCH_NOT_LIKE, parser);
   }
   else // factor = single expression like [string | ?]
      tree = setOperandType(OP_PAT_MATCH_LIKE, parser);
   
   booleanClause = getInstanceBooleanClause(parser);
   rightTree = initSQLBooleanClauseTree(booleanClause, parser->heap);
   
   if (token == TK_LIKE)
   {
      if ((token = yylex(parser)) == TK_STR) // string
         setOperandStringLiteral(rightTree, parser->yylval);
      else if (token == TK_INTERROGATION) // ?
      {
         if (booleanClause->paramCount == MAXIMUMS) // There is a maximum number of parameters.
            return lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
         rightTree->isParameter = true;
         if (parser->isWhereClause)
            parser->whereParamList[booleanClause->paramCount++] = rightTree;
         else
            parser->havingParamList[booleanClause->paramCount++] = rightTree;
      }
      else
         return lbError(ERR_SYNTAX_ERROR, parser);
      
      (tree->rightTree = rightTree)->parent = (tree->leftTree = parser->auxTree)->parent = tree;
      parser->auxTree = tree;   
      
      return yylex(parser);
   }
   
   return lbError(ERR_SYNTAX_ERROR, parser);  
}

/**
 * Deals with a single expression of an expression tree of a where clause.
 * 
 * @param token The first token of the single expression.
 * @param parser The parser structure.
 * @return The token after the single expression.
 */
int32 singleExp(int32 token, LitebaseParser* parser)
{
   int32 auxToken;
   SQLBooleanClauseTree* tree;
   
   if (token == TK_NUMBER) // single expression = number
   {
      // juliana@226a_20
      (tree = parser->auxTree = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), parser->heap))->operandValue.asChars = parser->yylval; 
      return yylex(parser);
   }
   else if (token == TK_STR) // single expression = string
   {
      setOperandStringLiteral((tree = parser->auxTree = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), parser->heap)), (JCharP)parser->yylval);
      return yylex(parser);
   }
   else if (token == TK_INTERROGATION) // single expression = ?
   {
      SQLBooleanClause* booleanClause = getInstanceBooleanClause(parser);

      if (booleanClause->paramCount == MAXIMUMS) // There is a maximum number of parameters.
         return lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
      
      if (parser->isWhereClause)
         (parser->whereParamList[booleanClause->paramCount++] = parser->auxTree = initSQLBooleanClauseTree(booleanClause, parser->heap))->isParameter = true;
      else
         (parser->havingParamList[booleanClause->paramCount++] = parser->auxTree = initSQLBooleanClauseTree(booleanClause, parser->heap))->isParameter = true;
      return yylex(parser);
   }
   else if ((auxToken = dataFunction(token, parser)) != -1) // single expression = function(...)
   {
      SQLBooleanClause* booleanClause = getInstanceBooleanClause(parser);
      int32 i = 1,
            index = booleanClause->fieldsCount,
            hashCode;
      SQLResultSetField* field = parser->auxField;
      Hashtable* fieldName2Index = &booleanClause->fieldName2Index;
      SQLResultSetField* paramField = field->parameter = initSQLResultSetField(parser->heap); // Creates the parameter field.
 
      (parser->auxTree = tree = initSQLBooleanClauseTree(booleanClause, parser->heap))->operandType = OP_IDENTIFIER;
      hashCode = tree->nameSqlFunctionHashCode = tree->nameHashCode = TC_hashCode((tree->operandName = field->tableColName));
   
      // generates different indexes to repeted columns on where clause.
      // Ex: where year(birth) = 2000 and day(birth) = 3.
      while (TC_htGet32Inv(fieldName2Index, tree->nameSqlFunctionHashCode) >= 0)
         tree->nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
              
      if (index == MAXIMUMS) // There is a maximum number of columns.
         return lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
   
      // Puts the hash code of the function name in the hash table.
      TC_htPut32(fieldName2Index, tree->nameSqlFunctionHashCode, index);
       
      // Sets the field and function parameter fields.
      paramField->alias = paramField->tableColName = field->alias = field->tableColName = tree->operandName;
      paramField->aliasHashCode = paramField->tableColHashCode = field->tableColHashCode = field->aliasHashCode = tree->nameHashCode;
      field->dataType = dataTypeFunctionsTypes[field->sqlFunction];
      field->isDataTypeFunction = field->isVirtual = true;
      
      // Puts the field in the field list.
      if (parser->isWhereClause)
         parser->whereFieldList[booleanClause->fieldsCount++] = field; 
      else
         parser->havingFieldList[booleanClause->fieldsCount++] = field; 
      
      return auxToken;
   }
   else if (token != TK_NULL)// single expression = pure field.
   {
      SQLBooleanClause* booleanClause = getInstanceBooleanClause(parser);                  
      int32 i = 1, 
            index = booleanClause->fieldsCount,
            hashCode;
      SQLResultSetField* field;
      Hashtable* fieldName2Index = &booleanClause->fieldName2Index;
      
      token = pureField(token, parser);
      field = parser->auxField;

      (parser->auxTree = tree = initSQLBooleanClauseTree(booleanClause, parser->heap))->operandType = OP_IDENTIFIER;
      hashCode = field->tableColHashCode = tree->nameSqlFunctionHashCode = tree->nameHashCode 
                                                                         = TC_hashCode((tree->operandName = field->tableColName));
                                                                         
      // rnovais@570_108: Generates different index to repeted columns on where
      // clause. Ex: where year(birth) = 2000 and birth = '2008/02/11'.
      while (TC_htGet32Inv(fieldName2Index, tree->nameSqlFunctionHashCode) >= 0)
         tree->nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
      
      if (index == MAXIMUMS) // There is a maximum number of columns.
         return lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);

      // Puts the hash code of the function name in the hash table.
      TC_htPut32(fieldName2Index, tree->nameSqlFunctionHashCode, index);

      field->aliasHashCode = TC_hashCode(field->alias); // Sets the hash code of the field alias.
      
      // Puts the field in the field list.
      if (parser->isWhereClause)
         parser->whereFieldList[booleanClause->fieldsCount++] = field; 
      else
         parser->havingFieldList[booleanClause->fieldsCount++] = field; 
      
      return token;
   } 
   return lbError(ERR_SYNTAX_ERROR, parser);
}

/**
 * Deals with a list of values of an insert.
 * 
 * @param parser The parser structure.
 * @return The token after the list of values.
 */
int32 listValues(LitebaseParser* parser)
{
   int32 token,
       size = 0;
   JCharP* values = parser->fieldValues;
   
   do
      switch (token = yylex(parser))
      {
         case TK_NULL: // Null.
            size++;
            break;
         case TK_INTERROGATION: // A variable for prepared statements.
            values[size++] = questionMark;
            break;
         case TK_NUMBER: // A number.
         case TK_STR: // A string.
            values[size++] = (JCharP)parser->yylval;
            break;
         default: // The list of values is finished or an error occurred.
         { 
            if (!size) // There must be a value to be inserted.
               return lbError(ERR_SYNTAX_ERROR, parser); 
            return token;
         }
      }
   while ((token = yylex(parser)) == TK_COMMA); 
   parser->fieldValuesSize = size;
   return token;      
}

/**
 * Deals with a table list of a select.
 * 
 * @param parser The parser structure.
 * @return The token after the list of tables.
 */
int32 tableList(LitebaseParser* parser)
{
   int32 token,
       size = 0,
       hash;
   CharP tableName,
         tableAlias;
   Hashtable* tables = &parser->tables;
   SQLResultSetTable** list = parser->tableList;
   
   do
   {
      if ((token = yylex(parser)) != TK_IDENT) // Not a table name, return.
      {
         if (!(parser->select.tableListSize = size)) // There must be at least a table.
            return lbError(ERR_SYNTAX_ERROR, parser); 
         return token;
      }
      tableName = tableAlias = (CharP)parser->yylval; // Table name.
      
      // Table alias.
      if ((token = yylex(parser)) == TK_AS)
         token = yylex(parser);
      if (token == TK_IDENT)
      {
         tableAlias = (CharP)parser->yylval;
         token = yylex(parser);
      }
   
      // The table name alias must be unique.
      if (TC_htGet32Inv(&parserTP->tables, (hash = TC_hashCode(tableAlias))) != -1)
         return lbErrorWithMessage(getMessage(ERR_NOT_UNIQUE_ALIAS_TABLE), tableAlias, parser);
      else
         TC_htPut32(tables, hash, size);
      
      list[size++] = initSQLResultSetTable(tableName, tableAlias, parser->heap);
   }
   while (token == TK_COMMA);
   parser->select.tableListSize = size;
   return token;
}

/**
 * Deals with a list of expressions of a select.
 * 
 * @param parser The parser structure.
 * @return The token after the list of expressions.
 */
int32 fieldExp(int32 token, LitebaseParser* parser)
{
   SQLSelectClause* select = &parser->select;
   SQLResultSetField** resultFieldList = parser->selectFieldList;  
   int32 i; 
   
   if (token == TK_ASTERISK) // All fields.
   {
      // Adds a wildcard field.
      (resultFieldList[select->fieldsCount++] = initSQLResultSetField(parser->heap))->isWildcard = true;
      token = yylex(parser);
   }
   else if (token != PARSER_ERROR)
   {
      CharP alias = null;

      do
      {
         if (token == TK_COMMA) // Gets the next field list token.
            token = yylex(parser);
         
         if ((token = field(token, parser)) == TK_AS) // There is an alias.
         {
            if (yylex(parser) != TK_IDENT)
               return lbError(ERR_SYNTAX_ERROR, parser); 
            alias = (CharP)parser->yylval;
            token = yylex(parser);
         }
         else if (token != PARSER_ERROR)
         {
            // If the alias_name is null, the alias must be the name of the column. This was already done before.
            // If the alias is null and the field is a virtual column, raises an exception, since virtual columns require explicit aliases.
            if (parser->auxField->isVirtual)
               return lbError(ERR_REQUIRED_ALIAS, parser);
                                         
            alias = parser->auxField->alias; // The null alias name is filled as tableColName or tableName.tableColName, which was set before.
         }
         else
            return PARSER_ERROR;
         
         // Checks if the alias has not already been used by a predecessor.
         i = select->fieldsCount - 1;
         
         while (--i >= 0)
            if (strEq(resultFieldList[i]->alias, alias))
               return lbErrorWithMessage(getMessage(ERR_DUPLICATE_ALIAS), alias, parser);

         parser->auxField->aliasHashCode = TC_hashCode(parser->auxField->alias = alias); // Assigns the alias.
      }
      while (token == TK_COMMA);
   }
   return token;
}

/**
 * Deals with a list of update expressions.
 * 
 * @param parser The parser structure.
 * @return The token after the list of update expressions.
 */
int32 updateExpCommalist(LitebaseParser* parser)
{
   int32 token,
         size = 0;
   JCharP* values = parser->fieldValues;
   CharP* names = parser->fieldNames;
   SQLResultSetField* field;
   
   do
   {
      if (pureField(yylex(parser), parser) != TK_EQUAL) // field being updated.
         return lbError(ERR_SYNTAX_ERROR, parser); 
      field = parser->auxField;
      
      // New value.
      if ((token = yylex(parser)) == TK_STR || token == TK_NUMBER) // A string or a number.
         values[size++] = (JCharP)parser->yylval;
      else if (token == TK_NULL) // null
         size++;
      else if (token == TK_INTERROGATION) // A prepared statement parameter.
         values[size++] = questionMark; 
      else
         return lbError(ERR_SYNTAX_ERROR, parser);
      
      if (!parser->firstFieldUpdateTableName) // After the table name verification, the associated table name on the field name is discarded.
      {
         if (field->tableName)
         {
            parser->firstFieldUpdateTableName = field->tableName;
            parser->firstFieldUpdateAlias = field->alias;
         }
      } 
      else if (xstrcmp(field->tableName, parser->firstFieldUpdateTableName)) 
      
      // Verifies if it is different.
      // There is an error: update has just one table. This error will raise an exception later on.        
      {
         parser->secondFieldUpdateTableName = field->tableName;
         parser->secondFieldUpdateAlias = field->alias;
      }
      names[size - 1] = field->tableColName;
   }
   while ((token = yylex(parser)) == TK_COMMA);
   if (!(parser->fieldNamesSize = parser->fieldValuesSize = size))
      return lbError(ERR_SYNTAX_ERROR, parser);
   return token;
}

/**
 * Deals with a field.
 *
 * @param token A token to be used by the field.
 * @param parser The parser structure.
 * @return The token after the field.
 */
int32 field(int32 token, LitebaseParser* parser)
{
   int32 tokenAux;
   SQLSelectClause* select = &parser->select;
   SQLResultSetField* field = null;
   
   if (token == TK_IDENT) // A pure field.
   {
      token = pureField(token, parser);

      if (select->fieldsCount == MAXIMUMS) // The  maximum number of fields can't be reached.
         return lbError(ERR_FIELDS_OVERFLOW, parser);
                                   
      parser->selectFieldList[select->fieldsCount++] = field = parser->auxField;
      field->tableColHashCode = TC_hashCode(field->tableColName);
      select->hasRealColumns = true;
      tokenAux = token;
   }
   else 
   {
      if ((tokenAux = dataFunction(token, parser)) >= 0) // A function applied to a field.
      {
         SQLResultSetField* paramField = (field = parser->auxField)->parameter = initSQLResultSetField(parser->heap);
         
         // Sets the field.
         field->isDataTypeFunction = field->isVirtual = true;
         field->dataType = dataTypeFunctionsTypes[field->sqlFunction];

         // Sets the function parameter.
         paramField->alias = paramField->tableColName = field->tableColName;
         paramField->tableColHashCode = TC_hashCode(paramField->tableColName);
         field->tableColHashCode = paramField->aliasHashCode = paramField->tableColHashCode; 
      } 
      else if ((tokenAux = aggFunction(token, parser)) >= 0) // An aggregation function applied to a field.
      {
         // Sets the field.
         field = parser->auxField;
         field->isAggregatedFunction = field->isVirtual = true;
         field->dataType = aggregateFunctionsTypes[field->sqlFunction];

         // Sets the parameter, if there is such one.
         if (field->sqlFunction != FUNCTION_AGG_COUNT)
         {
            // Sets the function parameter.
            SQLResultSetField* paramField = field->parameter = initSQLResultSetField(parser->heap);
            paramField->alias = paramField->tableColName = field->tableColName;
            paramField->tableColHashCode = TC_hashCode(paramField->tableColName);
            field->tableColHashCode = paramField->aliasHashCode = paramField->tableColHashCode;
         }

         select->hasAggFunctions = true;
      }
      else
         return lbError(ERR_SYNTAX_ERROR, parser); 
      
      if (select->fieldsCount == MAXIMUMS) // The maximum number of fields can't be reached. 
         return lbError(ERR_FIELDS_OVERFLOW, parser);
      parser->selectFieldList[select->fieldsCount++] = field; // Sets the select statement.
   }

   return tokenAux;
}

/**
 * Deals with a pure field.
 * 
 * @param token A token to be used by the pure field.
 * @param parser The parser structure.
 * @return The token after the pure field.
 */
int32 pureField(int32 token, LitebaseParser* parser)
{
   SQLResultSetField* field = parser->auxField = initSQLResultSetField(parser->heap);
   
   if ((token = yylex(parser)) == TK_DOT) // table.fieldName
   {
      CharP alias;
      
      field->tableName = (CharP)parser->yylval;
      if (yylex(parser) != TK_IDENT)
         return lbError(ERR_SYNTAX_ERROR, parser); 
      alias = field->alias = (CharP)TC_heapAlloc(parser->heap, xstrlen(field->tableName) + xstrlen(field->tableColName = (CharP)parser->yylval) + 2);
      xstrcpy(alias, field->tableName);
      xstrcat(alias, ".");
      xstrcat(alias, field->tableColName);
      token = yylex(parser);
   }
   else // A simple field.
      field->tableColName = field->alias = (CharP)parser->yylval;
   
   return token;
}

/**
 * Deals with a data function.
 * 
 * @param token A token witch is possibly a data function token.
 * @param parser The parser structure.
 * @return The next token or -1 if it is not a data function. 
 */
int32 dataFunction(int32 token, LitebaseParser* parser)
{
   int32 function;
   
   switch (token)
   {
      case TK_ABS: // Abs function.
         function = FUNCTION_DT_ABS;
         break;
      case TK_DAY: // Day function.
         function = FUNCTION_DT_DAY;
         break;
      case TK_HOUR: // Hour function.
         function = FUNCTION_DT_HOUR;
         break;
      case TK_LOWER: // Lower function.
         function = FUNCTION_DT_LOWER;
         break;
      case TK_MILLIS: // Millis function.
         function = FUNCTION_DT_MILLIS;
         break;
      case TK_MINUTE: // Minute function.
         function = FUNCTION_DT_MINUTE;
         break;
      case TK_MONTH: // Month function.
         function = FUNCTION_DT_MONTH;
         break;
      case TK_SECOND: // Second function.
         function = FUNCTION_DT_SECOND;
         break;
      case TK_UPPER: // Upper function.
         function = FUNCTION_DT_UPPER;
         break;
      case TK_YEAR: // Year function.
         function = FUNCTION_DT_YEAR;
         break;
      default:
         return -1;
   }
   if (yylex(parser) != TK_OPEN || pureField(yylex(parser), parser) != TK_CLOSE)
      return lbError(ERR_SYNTAX_ERROR, parser); 
   parser->auxField->sqlFunction = function;
   return yylex(parser);
}

/**
 * Deals with a aggregation function.
 * 
 * @param token A token witch is possibly a data function token.
 * @param parser The parser structure.
 * @return The next token or -1 if it is not a data function. 
 */
int32 aggFunction(int32 token, LitebaseParser* parser)
{
   int function;
   
   switch (token)
   {
      case TK_AVG:
         function = FUNCTION_AGG_AVG;
         break;
      case TK_COUNT:
         function = FUNCTION_AGG_COUNT;
         break;
      case TK_MAX:
         function = FUNCTION_AGG_MAX;
         break;
      case TK_MIN:
         function = FUNCTION_AGG_MIN;
         break;
      case TK_SUM:
         function = FUNCTION_AGG_SUM;
         break;
      default:
         return -1;
   }
   if (token == TK_COUNT)
   {
      if (yylex(parser) != TK_OPEN || yylex(parser) != TK_ASTERISK || yylex(parser) != TK_CLOSE)
         return lbError(ERR_SYNTAX_ERROR, parser);
      parser->auxField = initSQLResultSetField(parser->heap);
   }
   else if (yylex(parser) != TK_OPEN || pureField(yylex(parser), parser) != TK_CLOSE)
      return lbError(ERR_SYNTAX_ERROR, parser);
   parser->auxField->sqlFunction = function; 
   return yylex(parser);
}

/**
 * Deals with a possible where clause.
 * 
 * @param token The token where if it is a where clause.
 * @param parser The parser structure.
 * @return The token received if it is not a where clause or the token after the where clause.
 */
int32 optWhereClause(int32 token, LitebaseParser* parser)
{
   if (token == TK_WHERE) // Where clause.
   {
      SQLBooleanClause* whereClause = getInstanceBooleanClause(parser);
      token = expression(yylex(parser), parser); 
      whereClause->expressionTree = whereClause->origExpressionTree = parser->auxTree;
      whereClause->isWhereClause = true; // It indicates that it is a where clause.
   }
   return token;
}

/**
 * Deals with an order by clause.
 * 
 * @param parser The parser structure.
 * @return The first token after the order by clause.
 */
int32 orderByClause(LitebaseParser* parser)
{
   int32 token;
   bool direction;
   SQLSelectClause* select = &parser->select;
   
   do
   {
      direction = true;
      
      // Ascending or descending order.
      if ((token = field(yylex(parser), parser)) == TK_ASC)
         token = yylex(parser);
      else if (token == TK_DESC)
      {
         direction = false;
         token = yylex(parser);
      }
      else if (token == PARSER_ERROR)
         return PARSER_ERROR;      
      
      select->fieldsCount--;
      addColumnFieldOrderGroupBy(direction, true, parser);
   }
   while (token == TK_COMMA);
   
   return token;
}

/**
 * Deals with a group by clause.
 * 
 * @param parser The parser structure.
 * @return The first token after the group by clause.
 */
int32 groupByClause(LitebaseParser* parser)
{
   int32 token;
   SQLSelectClause* select = &parser->select;
   
   do
   {  
      token = field(yylex(parser), parser);
      select->fieldsCount--;
      addColumnFieldOrderGroupBy(true, false, parser);
   }
   while (token == TK_COMMA);

   if (token == TK_HAVING) // Adds the expression tree of the where clause.
   {
      parser->isWhereClause = false;  // Indicates if the clause is a where or a having clause.
      token = expression(yylex(parser), parser);
      parser->havingClause->expressionTree = parser->auxTree;
   }
   
   return token;
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
 * Tests if the function <code>lbErrorWithMessage()</code> in fact creates an exception.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(lbErrorWithMessage)
{
   LitebaseParser* parser = (LitebaseParser*)xmalloc(sizeof(LitebaseParser));
   
   parser->context = currentContext;
   lbErrorWithMessage(getMessage(2), "", parser);
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
