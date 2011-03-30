%{
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



#include "Litebase.h"

%}

%start sql_expression // Start symbol.

// Variable tokens 
%token <sval>   TK_IDENT         // Identifiers.
%token <sval16> TK_STR TK_NUMBER // A string must use uint16* because it came from Java, and a number is represented as a string. 

%token TK_INTERROGATION TK_GREATER_EQUAL TK_LESS_EQUAL TK_GREATER TK_LESS TK_EQUAL TK_DIFF TK_DOT TK_COMMA // Symbols          

// Reserved words.
%token TK_ABS      TK_ADD    TK_ALTER   TK_AND    TK_AS    TK_ASC      TK_AVG     TK_BLOB    TK_BY     TK_CHAR   TK_COUNT  TK_CREATE TK_DATE   
%token TK_DATETIME TK_DAY    TK_DEFAULT TK_DELETE TK_DESC  TK_DISTINCT TK_DOUBLE  TK_DROP    TK_FLOAT  TK_FROM   TK_GROUP  TK_HAVING TK_HOUR   
%token TK_INDEX    TK_INSERT TK_INT     TK_INTO   TK_IS    TK_KEY      TK_LIKE    TK_LONG    TK_LOWER  TK_MAX    TK_MILLIS TK_MIN    TK_MINUTE 
%token TK_MONTH    TK_NOCASE TK_NOT     TK_NULL   TK_ON    TK_OR       TK_ORDER   TK_PRIMARY TK_RENAME TK_SECOND TK_SELECT TK_SET    TK_SHORT 
%token TK_SUM      TK_TABLE  TK_TO      TK_UPDATE TK_UPPER TK_VALUES   TK_VARCHAR TK_WHERE   TK_YEAR       

// String non-terminals. 
%type <sval> table opt_alias_name opt_alias_table_name

// Unicode string non-terminals.
%type <sval16> opt_default

// Non-terminals that receive the value of a token.
%type <ival> opt_direction opt_multiplier opt_primary_key opt_not_null opt_nocase

// Object types.          
%type <obj> colname_commalist sql_function_aggregation opt_having_clause opt_where_clause function_ident math_operator create_row 
%type <obj> field             pure_field               field_def         a_exp            b_exp          exp

// Precedence table.
%left TK_AND TK_OR
%left TK_NOT

%%

sql_expression: // SQL expression.
   TK_CREATE TK_TABLE table '(' create_row_commalist opt_key ')' // Create table.
   {
      parserTP->command = CMD_CREATE_TABLE;
      parserTP->tableList[0] = initSQLResultSetTable($3, NULL, parserTP->heap); // There's no alias table name here.
   }
 | TK_CREATE TK_INDEX TK_IDENT TK_ON table '(' colname_commalist ')' // Create index.
   {
      parserTP->command = CMD_CREATE_INDEX;
      parserTP->tableList[0] = initSQLResultSetTable($5, NULL, parserTP->heap); // There's no alias table name here.
   }
 | TK_DROP TK_TABLE table // Drop Table.
   {
      parserTP->command = CMD_DROP_TABLE;
      parserTP->tableList[0] = initSQLResultSetTable($3, NULL, parserTP->heap); // There's no alias table name here.
   }
 | TK_DROP TK_INDEX index_name TK_ON table // Drop index.
   {
      parserTP->command = CMD_DROP_INDEX;
      parserTP->tableList[0] = initSQLResultSetTable($5, NULL, parserTP->heap); // There's no alias table name here.
   }
 | TK_INSERT TK_INTO table opt_colname_commalist TK_VALUES '(' list_values ')' // Insert.
   {
      if (parserTP->fieldNamesSize && parserTP->fieldNamesSize != parserTP->fieldValuesSize)
      {
         char error[15];
         xstrprintf(error, "(%d != %d)", parserTP->fieldNamesSize, parserTP->fieldValuesSize);
         errorWithoutPosition(ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH, error, parser);
         return 1;
      }
   
      parserTP->command = CMD_INSERT;
      parserTP->tableList[0] = initSQLResultSetTable($3, NULL, parserTP->heap); // There's no alias table name here.
   }
 | TK_ALTER TK_TABLE table alter_stmt // Alter table.
   {
      parserTP->tableList[0] = initSQLResultSetTable($3, NULL, parserTP->heap); // There's no alias table name here.
   }
 | TK_DELETE opt_from table opt_alias_table_name opt_where_clause // Delete.
   {
      parserTP->command = CMD_DELETE;
      parserTP->tableList[0] = initSQLResultSetTable($3, $4, parserTP->heap);
      if ($5)
         setBooleanClauseTreeOnWhereClause($5, parser); // Where clause.
   }
 | TK_UPDATE table opt_alias_table_name TK_SET update_exp_commalist opt_where_clause // Update.
   {
      CharP tableNameAux = $3? $3 : $2;

      if (secondFieldUpdateTableName) // Verifies if there was an error on field.tableName.
      {
         if (xstrcmp(tableNameAux, firstFieldUpdateTableName))
            errorWithoutPosition(ERR_INVALID_COLUMN_NAME, firstFieldUpdateAlias, parser);
         else
            errorWithoutPosition(ERR_INVALID_COLUMN_NAME, secondFieldUpdateAlias, parser);
         return 1;
      }
      else if (firstFieldUpdateTableName && xstrcmp(tableNameAux, firstFieldUpdateTableName))
      {
         errorWithoutPosition(ERR_INVALID_COLUMN_NAME, firstFieldUpdateAlias, parser);
         return 1;
      }
      
      parserTP->command = CMD_UPDATE;
      parserTP->tableList[0] = initSQLResultSetTable($2, $3, parserTP->heap);
      if ($6)
         setBooleanClauseTreeOnWhereClause($6, parser); // Where clause.
   }
 | TK_SELECT opt_distinct field_exp TK_FROM table_list opt_where_clause opt_group_by_clause opt_order_by_clause // Select.
   {
      parserTP->command = CMD_SELECT;
      parserTP->select.tableListSize = parserTP->tableListSize;

      // Checks if the first field is the wildcard. If so, assigns null to list, to indicate that all fields must be included.
      if (parserTP->selectFieldList[0]->isWildcard)
         parserTP->select.fieldsCount = 0;
      
      if ($6) 
         setBooleanClauseTreeOnWhereClause($6, parser); // Where clause.
   }
;

table: // The table name.
   TK_IDENT
   {
      $$ = $1;
   }
;

opt_alias_table_name: // Optional alias table name.
   // null 
   {
      $$ = NULL;
   }
 | TK_IDENT
   {
      $$ = $1;
   }
;

index_name: // Index name. 
   '*'  // Deletes all indices except the primary key.
   { 
      parserTP->fieldNames[0] = "*";
   }
 | colname_commalist // Deletes the given indices.
;

create_row_commalist: // List of parameters of "CREATE TABLE".
   create_row
 | create_row_commalist TK_COMMA create_row
;

create_row: // The definition of each row.
   TK_IDENT TK_SHORT opt_primary_key opt_default opt_not_null // short
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }   
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, SHORT_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_INT opt_primary_key opt_default opt_not_null // int
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }      
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, INT_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_LONG opt_primary_key opt_default opt_not_null // long
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }   
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, LONG_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_FLOAT opt_primary_key opt_default opt_not_null // float
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }     
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, FLOAT_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_DOUBLE opt_primary_key opt_default opt_not_null // double
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }  
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, DOUBLE_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_CHAR '(' TK_NUMBER ')' opt_nocase opt_primary_key opt_default opt_not_null // char
   {
      IntBuf buffer;
      bool error;
      int32 size = TC_str2int(TC_JCharP2CharPBuf($4, -1, buffer), &error);
      
      if (size <= 0 || error) // The size must be a positive integer.
      {
         lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
         return 1;
      }
     
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, $6? CHARS_NOCASE_TYPE : CHARS_TYPE, size, $7, $8, $9, 
                                                                                                                                    parserTP->heap);
   }
 | TK_IDENT TK_DATE opt_primary_key opt_default opt_not_null // date
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, DATE_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_DATETIME opt_primary_key opt_default opt_not_null 
   {
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, DATETIME_TYPE, 0, $3, $4, $5, parserTP->heap);
   }
 | TK_IDENT TK_BLOB '(' TK_NUMBER opt_multiplier ')' opt_not_null // blob
   {
      IntBuf buffer;
      bool error;
      int32 size = TC_str2int(TC_JCharP2CharPBuf($4, -1, buffer), &error);
      
      if (size <= 0 || error) // The size must be a positive integer.
      {
         lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
         return 1;
      }
      
      if ($5 == 'k') // kilobytes
         size <<= 10; 
      else if ($5 == 'm') // megabytes
         size <<= 20;
         
      if (size > (10 << 20)) // There is a size limit for a blob!
      {
         lbError(ERR_BLOB_TOO_BIG, parser);
         return 1;
      }
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, BLOB_TYPE, size, 0, null, $7, parserTP->heap);
   }
 | TK_IDENT TK_VARCHAR '(' TK_NUMBER ')' opt_nocase opt_primary_key opt_default opt_not_null // varchar
   {
      IntBuf buffer;
      bool error;
      int32 size = TC_str2int(TC_JCharP2CharPBuf($4, -1, buffer), &error);
      
      if (size <= 0 || error) // The size must be a positive integer.
      {
         lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
         return 1;
      }
     
      if (parserTP->fieldListSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }
      parserTP->fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition($1, $6? CHARS_NOCASE_TYPE : CHARS_TYPE, size, $7, $8, $9, 
                                                                                                                                    parserTP->heap);
   }
;

opt_multiplier: // Blob size multiplier.
   // null // No multiplier.
   {
      $$ = 0;
   }
 | TK_IDENT 
   {
      if (($$ = $1[0]) != 'k' && $$ != 'm') // The multiplier must be Kilo or Mega.
      {
         lbError(ERR_INVALID_MULTIPLIER, parser);
         return 1;
      }
   }
;

opt_primary_key: // Optional primary key.
   // null
   {
      $$ = false;
   }
 | TK_PRIMARY TK_KEY
   {
      if (number_pk++ == 1)
	   {
	      lbError(ERR_PRIMARY_KEY_ALREADY_DEFINED, parser);
         return 1;
	   }
      $$ = true;
   }
;

opt_nocase: // When comparing this char column, is the case important?
   // null // The case is taken into consideration.
   {
      $$ = false;
   }
 | TK_NOCASE // The case must be taken into consideration.
   {
      $$ = true;
   }
;

opt_default: // Decides if the string or number has a default value and if it is null or not.
   // null // No default value.
   {
      $$ = null;
   }
 | TK_DEFAULT TK_STR // Default string.
   {
      $$ = $2;
   }
 | TK_DEFAULT TK_NUMBER // Default number.
   {
      $$ = $2;
   }
 | TK_DEFAULT TK_NULL // Default value is null.
   {
      $$ = null;
   }
;

opt_not_null: // Indicates if a column can store a null value.
   // NULL // It can store a null. 
   {
      $$ = false;
   }
 | TK_NOT TK_NULL // It can't store a null.
   {
      $$ = true;
   }
;

opt_key: // A primary key declared after the columns declaration.
   // null // No primary key declared after the columns declaration.
 | TK_COMMA TK_PRIMARY TK_KEY '(' primary_key_commalist ')' // There is a primary key.
   {
      if (number_pk++ == 1)
	   {
	      lbError(ERR_PRIMARY_KEY_ALREADY_DEFINED, parser);
         return 1;
	   }
   }
;

opt_colname_commalist: // Optional list of columns.
   // null // Empty list.
 | '(' colname_commalist ')' // There is a colum list.
;

primary_key_commalist: // The list of columns in the primary key.
   TK_IDENT // Simple primary key.
   {
      if (parserTP->fieldNamesSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      } 
      parserTP->fieldNames[parserTP->fieldNamesSize++] = $1;
   }

 | primary_key_commalist TK_COMMA TK_IDENT // Composed primary key.
   {
      if (parserTP->fieldNamesSize == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      } 
      parserTP->fieldNames[parserTP->fieldNamesSize++] = $3;
   }
;

colname_commalist: // A list of columns.
   TK_IDENT // It has only one column.
   {
      parserTP->fieldNames[parserTP->fieldNamesSize++] = $1;
   }
 | colname_commalist TK_COMMA TK_IDENT // It has more than one column.
   {
      parserTP->fieldNames[parserTP->fieldNamesSize++] = $3;
   }
;

list_values: // List of values of an insert statement.
   TK_STR // A single string.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = $1;
   }
 | TK_NUMBER // A single number.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = $1;
   }
 | TK_NULL // A single null.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = null;
   }
 | TK_INTERROGATION // A single variable for prepared statements.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = questionMark;
   }
 | list_values TK_COMMA TK_STR // A string in the list.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = $3;
   }
 | list_values TK_COMMA TK_NUMBER // A number in the list.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = $3;
   }
 | list_values TK_COMMA TK_NULL // A null in the list.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = null;
   }
 | list_values TK_COMMA TK_INTERROGATION // A parameter in the list.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = questionMark;
   }
;

alter_stmt: // Defines what is going to be changed in the table.
   TK_RENAME opt_colname TK_TO TK_IDENT  // Renames the table or a column.
   {
      parserTP->fieldNames[0] = $4;
   }
 | TK_ADD TK_PRIMARY TK_KEY '(' colname_commalist ')' // Adds a primary key.
   {
      parserTP->command = CMD_ALTER_ADD_PK;
   }
 | TK_DROP TK_PRIMARY TK_KEY // Drops a primary key.
   {
      parserTP->command = CMD_ALTER_DROP_PK;
   }
;

opt_colname: // Optional column name when renaming.
   // null // If no column name is given, renames the table.
   {
      parserTP->command = CMD_ALTER_RENAME_TABLE;
   }
 | TK_IDENT // Otherwise, renames a column.
   {
      parserTP->command = CMD_ALTER_RENAME_COLUMN;
      parserTP->fieldNames[1] = $1;
   }
;

opt_from: // Optional keyword from.
   // null // No from.
 | TK_FROM // With from.
;

update_exp_commalist: // List of update expressions.
   update_exp // A single update expression.
 | update_exp_commalist TK_COMMA update_exp // Two or more update expressions.
;

update_exp: // Definition of an update expression.
   pure_field TK_EQUAL field_value
   {
      SQLResultSetField* field = $1;
     
      if (firstFieldUpdateTableName) // After the table name verification, the associated table name on the field name is discarded.
      {
         // There is an error: update has just one table. This error will raise an exception later on.
         if (xstrcmp(field->tableName, firstFieldUpdateTableName))
         {
            secondFieldUpdateTableName = field->tableName;
            secondFieldUpdateAlias = field->alias;
         }
      }
      else if (field->tableName)
      {
         firstFieldUpdateTableName = field->tableName;
         firstFieldUpdateAlias = field->alias;
      }
      
      parserTP->fieldNames[parserTP->fieldNamesSize++] = field->tableColName;
   }
;

field_value: // The value for an update field.
   TK_STR // A string.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = $1;
   }
 | TK_NUMBER // A number.
   {
     parserTP->fieldValues[parserTP->fieldValuesSize++] = $1;
   }
 | TK_NULL // null.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = null;
   }
 | TK_INTERROGATION // A prepared statement parameter.
   {
      parserTP->fieldValues[parserTP->fieldValuesSize++] = questionMark;
   }
;

opt_distinct: // Must the result set return distinct columns?
;

field_exp: // Select expression field.
   '*' // All fields.
   {
      // Adds a willcard field.
		SQLResultSetField* field = initSQLResultSetField(parserTP->heap);
		SQLSelectClause* select = &parserTP->select;
		
		if (select->fieldsCount == MAXIMUMS)
		{
			lbError(ERR_FIELDS_OVERFLOW, parser);
			return 1;
		}
		field->isWildcard = select->hasWildcard = true;
		parserTP->selectFieldList[select->fieldsCount++] = field;
   }
 | field_comma_list // The select field list.
;

field_comma_list: // The select field list.
   field_def // A single field.
 | field_comma_list TK_COMMA field_def // More than one field.
;

field_def: // Field definition.
   field opt_alias_name // The field and its optional alias.
   {
      SQLResultSetField* field = $1;
      SQLResultSetField** selectFieldList = parserTP->selectFieldList;
      int32 i = parserTP->select.fieldsCount - 1;
      
      // If the alias_name is null, the alias must be the name of the column. This was already done before.
      
      if (!$2) // If the alias is null and the field is a virtual column, raises an exception, since virtual columns require explicit aliases.
      {
         if (field->isVirtual)
         {
            lbError(ERR_REQUIRED_ALIAS, parser);
            return 1;
         }
         $2 = field->alias; // The null alias name is filled as tableColName or tableName.tableColName, which was set before.
      }

      while (--i >= 0) // Checks if the alias has not already been used by a predecessor.
      {
         if (strEq(selectFieldList[i]->alias, $2))
         {
            errorWithoutPosition(ERR_DUPLICATE_ALIAS, $2, parser);
            return 1;
         }
      }
      
      field->aliasHashCode = TC_hashCode(field->alias = $2); // Assigns the alias.
   }
;

opt_alias_name: // Optional alias.
   // null // No alias.
   {
      $$ = NULL;
   }
 | TK_AS TK_IDENT // There is an alias.
   {
      $$ = $2;
   }
;

field: // A field.
   pure_field // A pure field.
   {
      SQLSelectClause* select = &parserTP->select;
      SQLResultSetField* field = $$ = $1;

      if (select->fieldsCount == MAXIMUMS) // The number of fields has reached the maximum.
      {
         lbError(ERR_FIELDS_OVERFLOW, parser);
         return 1;
      }

	   // Sets the field.
      field->tableColHashCode = TC_hashCode(field->tableColName);
      parserTP->selectFieldList[select->fieldsCount++] = field;
      select->hasRealColumns = true;
   }
 | function_ident // A function applied to a field.
   {
      SQLResultSetField* paramField;
      SQLResultSetField* field = $$ = $1;
      SQLSelectClause* select = &parserTP->select;

      if (select->fieldsCount == MAXIMUMS)
      {
         lbError(ERR_FIELDS_OVERFLOW, parser);
         return 1;
      }

      // Sets the field.
      field->isDataTypeFunction = field->isVirtual = true;
      field->dataType = dataTypeFunctionsTypes[field->sqlFunction];

      // Sets the function parameter.
      field->parameter = paramField = initSQLResultSetField(parserTP->heap);
      field->tableColHashCode = paramField->aliasHashCode = paramField->tableColHashCode = TC_hashCode(paramField->alias = paramField->tableColName 
                                                                                                                         = field->tableColName);
      // Sets the select statement.
      parserTP->selectFieldList[select->fieldsCount++] = field;
      select->hasDTFunctions = true;
   }
 | sql_function_aggregation // An aggregation function applied to a field.
   {
      SQLResultSetField* field = $$ = $1;
      SQLSelectClause* select = &parserTP->select;
       
      if (select->fieldsCount == MAXIMUMS)
      {
         lbError(ERR_FIELDS_OVERFLOW, parser);
         return 1;
      }

	   // Sets the field.
	   field->isAggregatedFunction = field->isVirtual = true;
	   field->dataType = aggregateFunctionsTypes[field->sqlFunction];

      // Sets the parameter, if there is such one.
      if (field->sqlFunction != FUNCTION_AGG_COUNT)
      {
         SQLResultSetField* paramField = field->parameter = initSQLResultSetField(((LitebaseParser*)parser)->heap);
         field->tableColHashCode = paramField->aliasHashCode = paramField->tableColHashCode = TC_hashCode(paramField->alias = paramField->tableColName 
                                                                                                                            = field->tableColName);
      } else
         field->parameter = null;

      // Sets the select statement.
      parserTP->selectFieldList[select->fieldsCount++] = field;
      select->hasAggFunctions = true;
   }
;
 
pure_field: // A pure field.
   TK_IDENT // A simple field.
   {
      SQLResultSetField* field = $$ = initSQLResultSetField(parserTP->heap);
      field->tableColName = field->alias = $1;
   }
 | TK_IDENT TK_DOT TK_IDENT // table.fieldName
   {
     SQLResultSetField* field = $$ = initSQLResultSetField(parserTP->heap);
     CharP alias = field->alias = (CharP)TC_heapAlloc(parserTP->heap, xstrlen($3) + xstrlen($1) + 2);
     field->tableColName = $3;
     field->tableName = $1;
     xstrcpy(alias, $1);
     xstrcat(alias, ".");
     xstrcat(alias, $3);
   }
;

table_list: // Table list.
   table_def // Single table.
 | table_list TK_COMMA table_def // More than on table.
;

table_def: // Table definition.
   table opt_as opt_alias_table_name // The table and its optional alias.
   {
      int32 hash;
      
      if (!$3)
			$3 = $1;
			
		// The table name alias must be unique.
		if (TC_htGet32Inv(&parserTP->tables, (hash = TC_hashCode($3))) != -1)
		{
			errorWithoutPosition(ERR_NOT_UNIQUE_ALIAS_TABLE, $3, parser);
			return 1;
		}
		TC_htPut32(&parserTP->tables, hash, parserTP->tables.size);	
		parserTP->tableList[parserTP->tableListSize++] = initSQLResultSetTable($1, $3, parserTP->heap);
   }
;

/* The alias name can be preceeded by the toke as */
opt_as: // Optional keyword as.
   // null // No as.
 | TK_AS // The keyword as is present.
;

opt_where_clause: // Optional where clause.
   // null
   {
      $$ = NULL;
   }
 | TK_WHERE a_exp // There is a where clause.
   {
      $$ = $2;
   }
;

opt_group_by_clause: // Optional group by clause.
   // null // No group by.
 | TK_GROUP TK_BY group_by_commalist opt_having_clause // Group by present.
   {
      if ($4) // Adds the expression tree of the where clause.
	      parserTP->havingClause->expressionTree = parserTP->havingClause->origExpressionTree = $4;
   }
;

group_by_commalist: // Group by field list.
   field // A single field.
   {
      parserTP->select.fieldsCount--; // Removes this field from the select list.
      if (!addColumnFieldOrderGroupBy($1, true, false, parser)) // Adds this field to the group by field list.
         return 1;
   }
 | group_by_commalist TK_COMMA field // More than one field.
   {
      parserTP->select.fieldsCount--; // Removes this field from the select list.
      if (!addColumnFieldOrderGroupBy($3, true, false, parser)) // Adds this field to the group by field list.
         return 1;
   }
;

opt_having_clause: // Optional having expression.
   // null
   {
      $$ = NULL;
   }
 | token_having a_exp
   {
      $$ = $2;
   }
;

token_having: // Indicates if the clause is a where or a having clause.
   TK_HAVING
   {
      parserTP->isWhereClause = false;
   }
;

opt_order_by_clause: // Order by field list.
   // null
 | TK_ORDER TK_BY order_by_commalist // More than one field.
;

order_by_commalist:
   order_by_field
 | order_by_commalist TK_COMMA order_by_field
;

order_by_field: // Order by field.
   field opt_direction // A field with the order direction.
   {
      parserTP->select.fieldsCount--;
      if (!addColumnFieldOrderGroupBy($1, $2, true, parser))
         return 1;
   }
;

opt_direction: // Order direction.
   // null // Default order: ascending.
   {
      $$ = true;
   }
 | TK_ASC // Ascending order.
   {
      $$ = true;
   }
 | TK_DESC // Descending order.
   {
      $$ = false;
   }
;

a_exp: // Expression tree for a where clause and having clause.
   '(' a_exp ')' // An expression between parenthesis.
   {
      $$ = $2;
   }
 | TK_NOT '(' a_exp ')' // A negated expression between parenthesis.
   {
      // The parent node will be the negation operator and the expression will be the right tree.
      SQLBooleanClauseTree* tree = setOperandType(OP_BOOLEAN_NOT, parser);
      $$ = (tree->rightTree = $3)->parent = tree;
   }
 | b_exp // Another expression type.
   {
      $$ = $1;
   }
 | a_exp TK_OR a_exp // Or expression.
   {
      // juliana@213_1: changed the way a tree with ORs is built in order to speed up queries with indices.
      SQLBooleanClauseTree* tree = setOperandType(OP_BOOLEAN_OR, parser);
      (tree->leftTree = $3)->parent = tree;
      $$ = (tree->rightTree = $1)->parent = tree;
   }
 | a_exp TK_AND a_exp // And expression.
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_BOOLEAN_AND, parser);
      (tree->leftTree = $1)->parent = tree;
      $$ = (tree->rightTree = $3)->parent = tree;
   }
;

b_exp: // The second expression type.
   TK_NOT b_exp // Negation expression.
   {
      // The parent node will be the negation operator and the expression will be the right tree.
      SQLBooleanClauseTree* tree = setOperandType(OP_BOOLEAN_NOT, parser);
      $$ = (tree->rightTree = $2)->parent = tree;
   }
 | exp math_operator exp // Math expression.
   {
      SQLBooleanClauseTree* tree = $2;
      (tree->leftTree = $1)->parent = tree;
      $$ = (tree->rightTree = $3)->parent = tree;
   }
 | exp TK_IS TK_NULL // Null expression.
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_PAT_IS, parser);
      (tree->rightTree = setOperandType(OP_PAT_NULL, parser))->parent = tree;
      $$ = (tree->leftTree = $1)->parent = tree;
   }
 | exp TK_IS TK_NOT TK_NULL // Not null expression.
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_PAT_IS_NOT, parser);
      (tree->rightTree = setOperandType(OP_PAT_NULL, parser))->parent = tree;
      $$ = (tree->leftTree = $1)->parent = tree;
   }
 | exp TK_LIKE TK_STR // Like expression.
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_PAT_MATCH_LIKE, parser);
      SQLBooleanClauseTree* rightTree = initSQLBooleanClauseTree(getInstanceBooleanClause(parserTP), parserTP->heap);
      
      setOperandStringLiteral(rightTree, $3);
      (tree->rightTree = rightTree)->parent = tree;
      $$ = (tree->leftTree = $1)->parent = tree;
   }
 | exp TK_NOT TK_LIKE TK_STR // Not like expression.
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_PAT_MATCH_NOT_LIKE, parser);
      SQLBooleanClauseTree* rightTree = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), parserTP->heap);
      
      setOperandStringLiteral(rightTree, $4);
      (tree->rightTree = rightTree)->parent = tree;
      $$ = (tree->leftTree = $1)->parent = tree;
   }
 | exp TK_LIKE TK_INTERROGATION // Like ? (prepared statement).
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_PAT_MATCH_LIKE, parser);
      SQLBooleanClause* whereClause = getInstanceBooleanClause(parser);
      SQLBooleanClauseTree* rightTree = initSQLBooleanClauseTree(whereClause, parserTP->heap);
      
      if (whereClause->paramCount == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
         return 1;
      }
      
      rightTree->isParameter = true;
      if (parserTP->isWhereClause)
		   parserTP->whereParamList[whereClause->paramCount++] = rightTree;
	   else
		   parserTP->havingParamList[whereClause->paramCount++] = rightTree;
      (tree->rightTree = rightTree)->parent = tree;
      $$ = (tree->leftTree = $1)->parent = tree;
   }
 | exp TK_NOT TK_LIKE TK_INTERROGATION // Like not ? (prepared statement).
   {
      SQLBooleanClauseTree* tree = setOperandType(OP_PAT_MATCH_NOT_LIKE, parser);
      SQLBooleanClause* whereClause = getInstanceBooleanClause(parser);
      SQLBooleanClauseTree* rightTree = initSQLBooleanClauseTree(whereClause, parserTP->heap);
     
      if (whereClause->paramCount == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
         return 1;
      }
     
      rightTree->isParameter = true;
      if (parserTP->isWhereClause)
		   parserTP->whereParamList[whereClause->paramCount++] = rightTree;
	   else
		   parserTP->havingParamList[whereClause->paramCount++] = rightTree;
      (tree->rightTree = rightTree)->parent = tree;
      $$ = (tree->leftTree = $1)->parent = tree;
   }
;

exp: // A single expression.
   pure_field // A field.
   {
		SQLBooleanClause* booleanClause = getInstanceBooleanClause(parser);
		SQLBooleanClauseTree* booleanClauseTree = $$ = initSQLBooleanClauseTree(booleanClause, parserTP->heap);
      int32 i = 1,
            index = booleanClause->fieldsCount,
            hash;
		SQLResultSetField* field = $1;
		Hashtable* fieldName2Index = &booleanClause->fieldName2Index;
		CharP operandName = booleanClauseTree->operandName = field->tableColName;
		
		booleanClauseTree->operandType = OP_IDENTIFIER;
		hash = field->tableColHashCode = booleanClauseTree->nameSqlFunctionHashCode = booleanClauseTree->nameHashCode = TC_hashCode(operandName);
		
		// rnovais@570_108: Generates different index to repeted columns on where clause. Ex: where year(birth) = 2000 and birth = '2008/02/11'.
		while (TC_htGet32Inv(fieldName2Index, hash) >= 0)
			hash = TC_hashCodeFmt("si", operandName, i++);
		
		if (booleanClause->fieldsCount == MAXIMUMS)
		{
	      lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
			return 1;
		}
		
		// Puts the hash code of the function name in the hash table.
		TC_htPut32(fieldName2Index, booleanClauseTree->nameSqlFunctionHashCode = hash, index);
		
		field->aliasHashCode = TC_hashCode(field->alias); // Sets the hash code of the field alias.

      // Puts the field in the field list.
		if (parserTP->isWhereClause)
	      parserTP->whereFieldList[booleanClause->fieldsCount++] = field;
	   else
         parserTP->havingFieldList[booleanClause->fieldsCount++] = field;
   }
 | TK_NUMBER // A number.
   {
      SQLBooleanClauseTree* tree = $$ = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), parserTP->heap);
      tree->operandValue.asChars = $1; // juliana@227_1
   }
 | TK_STR // A string.
   {
      SQLBooleanClauseTree* tree = $$ = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), parserTP->heap);
      setOperandStringLiteral(tree, $1);
   }
 | TK_INTERROGATION // A ? (prepared statement parameter).
   {
      SQLBooleanClause* whereClause = getInstanceBooleanClause(parser);
      SQLBooleanClauseTree* tree = $$ = initSQLBooleanClauseTree(whereClause, parserTP->heap);
     
      if (whereClause->paramCount == MAXIMUMS)
      {
         lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
         return 1;
      }
     
      tree->isParameter = true;
      if (parserTP->isWhereClause)
		   parserTP->whereParamList[whereClause->paramCount++] = tree;
	   else
		   parserTP->havingParamList[whereClause->paramCount++] = tree;
   }
 | function_ident
   {
      SQLBooleanClause* booleanClause = getInstanceBooleanClause(parser);
      SQLBooleanClauseTree* booleanClauseTree = $$ = initSQLBooleanClauseTree(booleanClause, parserTP->heap);
      Hashtable* fieldName2Index = &booleanClause->fieldName2Index;
      int32 i = 1,
            index = booleanClause->fieldsCount,
            hash;
      SQLResultSetField* field = $1;
      SQLResultSetField* paramField = field->parameter = initSQLResultSetField(parserTP->heap); // Creates the parameter field.
	   CharP operandName = booleanClauseTree->operandName = paramField->alias = paramField->tableColName = field->alias = field->tableColName;
      
      booleanClauseTree->operandType = OP_IDENTIFIER; 
      hash = paramField->aliasHashCode = paramField->tableColHashCode = field->tableColHashCode = field->aliasHashCode 
                                       = booleanClauseTree->nameSqlFunctionHashCode = booleanClauseTree->nameHashCode = TC_hashCode(operandName);

      // generates different indexes to repeted columns on where clause.
      // Ex: where year(birth) = 2000 and day(birth) = 3.
      while (TC_htGet32Inv(fieldName2Index, hash) >= 0)
         hash = TC_hashCodeFmt("si", operandName, i++);

      if (booleanClause->fieldsCount == MAXIMUMS) // There is a maximum number of columns.
      {
         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
         return 1;
      }
      
      // Puts the hash code of the function name in the hash table.
      TC_htPut32(fieldName2Index, booleanClauseTree->nameSqlFunctionHashCode = hash, index);

      // Sets the field and function parameter fields.
      field->dataType = dataTypeFunctionsTypes[field->sqlFunction];
      field->isVirtual = field->isDataTypeFunction = true;

      // Puts the field in the field list.
      if (parserTP->isWhereClause)
	      parserTP->whereFieldList[booleanClause->fieldsCount++] = field;
	   else
         parserTP->havingFieldList[booleanClause->fieldsCount++] = field;
   }
;

math_operator: // Math operator.
   TK_EQUAL // ==
   {
      $$ = setOperandType(OP_REL_EQUAL, parser);
   }
 | TK_DIFF // <> or !=
   {
      $$ = setOperandType(OP_REL_DIFF, parser);
   }
 | TK_GREATER // >
   {
      $$ = setOperandType(OP_REL_GREATER, parser);
   }
 | TK_LESS // <
   {
      $$ = setOperandType(OP_REL_LESS, parser);
   }
 | TK_GREATER_EQUAL // >=
   {
      $$ = setOperandType(OP_REL_GREATER_EQUAL, parser);
   }
 | TK_LESS_EQUAL // <=
   {
      $$ = setOperandType(OP_REL_LESS_EQUAL, parser);
   }
;

function_ident: // A function applied to a field.
   TK_ABS '(' pure_field ')' // Abs function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_ABS;
   }
 | TK_UPPER '(' pure_field ')' // Upper function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_UPPER;
   }
 | TK_LOWER '(' pure_field ')' // Lower function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_LOWER;
   }
 | TK_YEAR '(' pure_field ')' // Year function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_YEAR;
   }
 | TK_MONTH '(' pure_field ')' // Month function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_MONTH;
   }
 | TK_DAY '(' pure_field ')' // Day function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_DAY;
   }
 | TK_HOUR '(' pure_field ')' // Hour function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_HOUR;
   }
 | TK_MINUTE '(' pure_field ')' // Minute function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_MINUTE;
   }
 | TK_SECOND '(' pure_field ')' // Second function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_SECOND;
   }
 | TK_MILLIS '(' pure_field ')' // Millis function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_DT_MILLIS;
   }
;

sql_function_aggregation: // An aggregation function applied to a field.
   TK_COUNT '(' '*' ')' // Count aggregation function.
   {
      SQLResultSetField* field = $$ = initSQLResultSetField(parserTP->heap);
      field->sqlFunction = FUNCTION_AGG_COUNT;
   }
 | TK_MAX '(' pure_field ')' // Max aggregation function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_AGG_MAX;
   }
 | TK_MIN '(' pure_field ')' // Min aggregation function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_AGG_MIN;
   }
 | TK_AVG '(' pure_field ')' // Avg aggregation function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_AGG_AVG;
   }
 | TK_SUM '(' pure_field ')' // Sum aggregation function.
   {
      SQLResultSetField* field = $$ = $3;
      field->sqlFunction = FUNCTION_AGG_SUM;
   }
;

%%
