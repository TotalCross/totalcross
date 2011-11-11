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

import totalcross.util.*;
import totalcross.sys.*;

%}
%token <sval> TK_IDENT TK_NUMBER TK_STR // All values are strings; Litebase handles this.

// Keywords                                                                                                                                                                    
%token TK_VARCHAR  TK_UPDATE  TK_VALUES TK_CREATE TK_ORDER  TK_WHERE TK_MONTH TK_FROM  TK_LONG TK_NULL TK_ADD  TK_MAX TK_NOT TK_AS  TK_TO
%token TK_DEFAULT  TK_DELETE  TK_RENAME TK_MILLIS TK_ALTER  TK_UPPER TK_INDEX TK_DROP  TK_LIKE TK_DESC TK_SUM  TK_AND TK_AVG TK_ABS TK_OR
%token TK_DATETIME TK_INSERT  TK_SECOND TK_NOCASE TK_SELECT TK_GROUP TK_LOWER TK_SHORT TK_YEAR TK_CHAR TK_INTO TK_INT TK_SET TK_MIN TK_BY
%token TK_DISTINCT TK_PRIMARY TK_HAVING TK_DOUBLE TK_MINUTE TK_FLOAT TK_COUNT TK_TABLE TK_BLOB TK_HOUR TK_DATE TK_KEY TK_ASC TK_DAY TK_IS TK_ON
                             
%token TK_INTERROGATION TK_GREATER_EQUAL TK_LESS_EQUAL TK_GREATER TK_LESS TK_EQUAL TK_DIFF TK_DOT TK_COMMA // Symbols.      

%type <sval> opt_alias_table_name opt_default_number opt_default_str opt_alias_name  table                      // String types. 
%type <ival> opt_primary_key      opt_multiplier     opt_direction   opt_not_null    opt_distinct   opt_nocase  // Integer types.

// Object types.          
%type <obj> sql_function_aggregation opt_having_clause opt_where_clause function_ident math_operator field pure_field a_exp b_exp exp

// Precedence table.
%left TK_AND TK_OR
%left TK_NOT

%%
sql_expression: // SQL expression.
   TK_CREATE TK_TABLE table '(' create_row_commalist opt_key')' // Create table.
   {
	   command = SQLElement.CMD_CREATE_TABLE;
	   tableList[0] = new SQLResultSetTable($3); // There's no alias table name here.
   }
 | TK_CREATE TK_INDEX TK_IDENT TK_ON table '(' colname_commalist ')' // Create Index.
   {
      command = SQLElement.CMD_CREATE_INDEX;
	   tableList[0] = new SQLResultSetTable($5); // There's no alias table name here.
   }
 | TK_DROP TK_TABLE table // Drop Table.
   {
	   command = SQLElement.CMD_DROP_TABLE;
	   tableList[0] = new SQLResultSetTable($3); // There's no alias table name here.
   }
 | TK_DROP TK_INDEX index_name TK_ON table // Drop index.
   {
      command = SQLElement.CMD_DROP_INDEX;
      tableList[0] = new SQLResultSetTable($5); // There's no alias table name here.
   }
 | TK_INSERT TK_INTO table opt_colname_commalist TK_VALUES '(' list_values ')' // Insert.
   {
      // If the default order is not used, the number of values must be equal to the number of fields.
      if (fieldNamesSize != 0 && fieldNamesSize != fieldValuesSize) 
	   	 throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
  + LitebaseMessage.getMessage(LitebaseMessage.ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH) + '(' + fieldNamesSize + " != " + fieldValuesSize + ')');
	  
	   command = SQLElement.CMD_INSERT;
	   tableList[0] = new SQLResultSetTable($3); // There's no alias table name here.
   }
 | TK_ALTER TK_TABLE table alter_stmt   // Alter table.
   {
	   tableList[0] = new SQLResultSetTable($3); // There's no alias table name here.
   }
 | TK_DELETE opt_from table opt_alias_table_name opt_where_clause // Delete.
   {
      command = SQLElement.CMD_DELETE;
	   tableList[0] = new SQLResultSetTable($3, aliasTableName);
	   if ($5 != null)
	      whereClause.expressionTree = (SQLBooleanClauseTree)$5;
   }
 | TK_UPDATE table opt_alias_table_name TK_SET update_exp_commalist opt_where_clause // Update.
   {
      String tableNameAux = (aliasTableName == null)? $2 : aliasTableName;
   
      if (secondFieldUpdateTableName != null) // Verifies if there was an error on field.tableName.
	   {
	      if (!tableNameAux.equals(firstFieldUpdateTableName))
		      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
		                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + firstFieldUpdateAlias);
		   else
		      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
		                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + secondFieldUpdateAlias);
	   } 
	   else if (firstFieldUpdateTableName != null && !tableNameAux.equals(firstFieldUpdateTableName))
	      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
	                               + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + firstFieldUpdateAlias);

	   command = SQLElement.CMD_UPDATE;
	   tableList[0] = new SQLResultSetTable($2,aliasTableName);
	   if ($6 != null)
	      whereClause.expressionTree = (SQLBooleanClauseTree)$6;
   }
 | TK_SELECT opt_distinct field_exp TK_FROM table_list opt_where_clause opt_group_by_clause opt_order_by_clause // Select.
   {
	   command = SQLElement.CMD_SELECT;
      select.tableList = new SQLResultSetTable[tableListSize];
      Vm.arrayCopy(tableList, 0, select.tableList, 0, tableListSize);

      // Checks if the first field is the wild card. If so, assigns null to list, to indicate that all fields must be included.
      if (select.fieldList[0].isWildcard)
      {
         select.fieldList = null;
         select.fieldsCount = 0;
      } 
      else 
      {
         // Compacts the resulting field list.
         SQLResultSetField[] compactFieldList = new SQLResultSetField[select.fieldsCount];
         Vm.arrayCopy(select.fieldList, 0, compactFieldList, 0, select.fieldsCount);
         select.fieldList = compactFieldList;
      }

	   if ($6 != null) // whereClause
	      whereClause.expressionTree = (SQLBooleanClauseTree)$6;
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
      aliasTableName = null;
   }
 | TK_IDENT
   {
      aliasTableName = $1;
   }
;

index_name: // Index name.
   '*' // Deletes all indices except the primary key.
   {
      fieldNames[fieldNamesSize++] = "*";
   }
 | colname_commalist  // Deletes the given indices.
;

create_row_commalist:  // List of parameters of "CREATE TABLE".
   create_row
 | create_row_commalist TK_COMMA create_row
;

create_row:  // The definition of each row.
   TK_IDENT TK_SHORT opt_primary_key opt_default_number opt_not_null // short
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.SHORT, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_INT opt_primary_key opt_default_number opt_not_null	// int
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.INT, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_LONG opt_primary_key opt_default_number opt_not_null // long
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.LONG, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_FLOAT opt_primary_key opt_default_number opt_not_null // float
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.FLOAT, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_DOUBLE opt_primary_key opt_default_number opt_not_null 	// double
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.DOUBLE, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_CHAR '(' TK_NUMBER ')' opt_nocase opt_primary_key opt_default_str opt_not_null // char
   {
      int size;

  	   try  // The size must be a positive integer.
  	   {
    	   if ((size = Convert.toInt($4)) <= 0)
    	      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
    	                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT) 
    	                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
	  	} 
	  	catch (InvalidNumberException exception)
	  	{
	  	   throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
    	                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT) 
    	                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
	  	}
	   fieldList[fieldListSize++] 
	                = new SQLFieldDefinition($1, (isNocase)? SQLElement.CHARS_NOCASE : SQLElement.CHARS, size, isPrimaryKey, strDefault, isNotNull);
	}
 | TK_IDENT TK_DATE opt_primary_key opt_default_str opt_not_null  // date
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.DATE, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_DATETIME opt_primary_key opt_default_str opt_not_null // datetime
   {
      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.DATETIME, 0, isPrimaryKey, strDefault, isNotNull);
   }
 | TK_IDENT TK_BLOB '(' TK_NUMBER opt_multiplier ')'  opt_not_null // blob
   {
      int size;  // The size must be a positive integer.
      
      try
  	   {
	      if ((size = Convert.toInt($4)) <= 0)
		      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
    	                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT) 
    	                              + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
	   } 
	   catch (InvalidNumberException exception)
  	   {
  	      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
    	                           + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT) 
    	                           + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
  	   }
  	  
      if ($5 == 'k') // kilobytes
         size <<= 10;
      else if ($5 == 'm') // megabytes
         size <<= 20;
      if (size > (10 << 20))  // There is a size limit for a blob!
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_TOO_BIG)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      fieldList[fieldListSize++] = new SQLFieldDefinition($1, SQLElement.BLOB, size, false, null, isNotNull);
   }
 | TK_IDENT TK_VARCHAR '(' TK_NUMBER ')' opt_nocase opt_primary_key opt_default_str opt_not_null // varchar
   {
  	   int size;
  	  
  	   try // The size must be a positive integer.
  	   {
	      if ((size = Convert.toInt($4)) <= 0)
		      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
    	                               + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT) 
    	                               + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
	   }
      catch (Exception exception)
	   {
	      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
    	                          + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT) 
    	                          + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
	   }

	   fieldList[fieldListSize++] 
	             = new SQLFieldDefinition($1, (isNocase)? SQLElement.CHARS_NOCASE : SQLElement.CHARS, size, isPrimaryKey, strDefault, isNotNull);
   }
;

opt_multiplier: // Blob size multiplier.
   // null // No multiplier.
   {
      $$ = 0;
   }
 | TK_IDENT
   {
      if ($1.equals("k") || $1.equals("m")) // The multiplier must be Kilo or Mega.
         $$ = $1.charAt(0);
      else
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_MULTIPLIER)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
   }
;

opt_primary_key: // Optional primary key.
   // null
   {
      isPrimaryKey = false; // No primary key.
   }
 | TK_PRIMARY TK_KEY
   {
      if (number_pk++ == 1) // There can't be two primary keys.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
	   isPrimaryKey = true;
   }
;

opt_nocase: // When comparing this char column, is the case important?
   // null // The case is taken into consideration.
   {
      isNocase = false;
   }
 | TK_NOCASE // The case must be taken into consideration.
   {
      isNocase = true;
   }
;

opt_default_number: // Optional default number.
   // null // No default number.
   {
      strDefault = null;
   }
 | TK_DEFAULT TK_NUMBER // Default number.
   {
      strDefault = $2;
   }
 | TK_DEFAULT TK_NULL // Default number is null.
   {
      strDefault = null;
   }
;

opt_default_str: // Optional default string.
   // null // No default string.
   {
      strDefault = null;
   }
 | TK_DEFAULT TK_STR // Default string.
   {
      strDefault = $2;
   }
 | TK_DEFAULT TK_NULL // Default string is null.
   {
      strDefault = null;
   }
;

opt_not_null: // Indicates if a column can store a null value.
   // null // It can store a null.
   {
      isNotNull = false;
   }
 | TK_NOT TK_NULL // It can't store a null.
   {
      isNotNull = true;
   }
;

opt_key: // A primary key declared after the columns declaration.
   // null // No primary key declared after the columns declaration.
   {}
 | TK_COMMA TK_PRIMARY TK_KEY '(' primary_key_commalist ')' // There is a primary key.
   {
      if (number_pk++ == 1)  // There can't be two primary keys.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
   }
;

primary_key_commalist: // The list of columns in the primary key.
   TK_IDENT // Simple primary key.
   {
      fieldNames[fieldNamesSize++] = $1;
   }
 | primary_key_commalist TK_COMMA TK_IDENT // Composed primary key.
   {
      fieldNames[fieldNamesSize++] = $3;
   }
;

opt_colname_commalist: // Optional list of columns.
   // null // Empty list.
   {}
 | '(' colname_commalist ')' // There is a colum list.
   {}
;

colname_commalist: // A list of columns.
   TK_IDENT // It only has one column.
   {
      fieldNames[fieldNamesSize++] = $1; // Adds the column name.
   }
 | colname_commalist TK_COMMA TK_IDENT // It has more than one column.
   {
      fieldNames[fieldNamesSize++] = $3; // Adds the column name.
   }
;

list_values: // List of values of an insert statement.
   TK_STR // A single string.
   {
      fieldValues[fieldValuesSize++] = $1;
   }
 | TK_NUMBER // A single number.
   {
      fieldValues[fieldValuesSize++] = $1;
   }
 | TK_NULL // A single null.
   {
      fieldValues[fieldValuesSize++] = null;
   }
 | TK_INTERROGATION // A single variable for prepared statements.
   {
      fieldValues[fieldValuesSize++] = "?";
   }
 | list_values TK_COMMA TK_STR // A string in the list.
   {
      fieldValues[fieldValuesSize++] = $3;
   }
 | list_values TK_COMMA TK_NUMBER // A number in the list.
   {
      fieldValues[fieldValuesSize++] = $3;
   }
 | list_values TK_COMMA TK_NULL // A null in the list.
   {
      fieldValues[fieldValuesSize++] = null;
   }
 | list_values TK_COMMA TK_INTERROGATION // A parameter in the list.
   {
      fieldValues[fieldValuesSize++] = "?";
   }
;

alter_stmt: // Defines what is going to be changed in the table.
   TK_RENAME opt_colname TK_TO TK_IDENT // Renames the table or a column.
   {
      fieldNames[0] = $4;
   }
 | TK_ADD TK_PRIMARY TK_KEY '(' colname_commalist ')' // Adds a primary key.
   {
      command = ParserResult.CMD_ALTER_ADD_PK;
   }
 | TK_DROP TK_PRIMARY TK_KEY // Drops a primary key.
   {
      command = ParserResult.CMD_ALTER_DROP_PK;
   }
;

opt_colname: // Optional column name when renaming.
   // null // If no column name is given, renames the table.
   {
      command = ParserResult.CMD_ALTER_RENAME_TABLE;
   }
 | TK_IDENT // Otherwise, renames a column.
   {
      command = ParserResult.CMD_ALTER_RENAME_COLUMN;
	   fieldNames[1] = $1;
   }
;

opt_from: // Optional keyword from.
   // null // No from.
 {}
 | TK_FROM // With from.
;

update_exp_commalist: // List of update expressions.
   update_exp // A single update expression.
 | update_exp_commalist TK_COMMA update_exp // Two or more update expressions.
;

update_exp: // Definition of an update expression.
   pure_field TK_EQUAL field_value
   {
      SQLResultSetField field = (SQLResultSetField)$1;
   
	   if (firstFieldUpdateTableName == null) // After the table name verification, the associated table name on the field name is discarded.
	   {
	      if (field.tableName != null)
		   {
		      firstFieldUpdateTableName = field.tableName;
			   firstFieldUpdateAlias = field.alias;
		   }
	   } 
	   else if (!field.tableName.equals(firstFieldUpdateTableName)) 
	   
	   // Verifies if it is different.
      // There is an error: update has just one table. This error will raise an exception later on.	     
      {
		   secondFieldUpdateTableName = field.tableName;
			secondFieldUpdateAlias = field.alias;
		}
	   fieldNames[fieldNamesSize++] = field.tableColName;
   }
;

field_value: // The value for an update field.
   TK_STR // A string.
   {
      fieldValues[fieldValuesSize++] = $1;
   }
 | TK_NUMBER // A number.
   {
      fieldValues[fieldValuesSize++] = $1;
   }
 | TK_NULL // null.
   {
      fieldValues[fieldValuesSize++] = null;
   }
 | TK_INTERROGATION // A prepared statement parameter.
   {
      fieldValues[fieldValuesSize++] = "?";
   }
;

opt_distinct: // Must the result set return distinct columns?
   // null // The columns do not have to be distinct.
   {
      $$ = 0;
   }
 | TK_DISTINCT // The columns must be distinct.
   {
      $$ = 1;
   }
;

field_exp: // Select expression field.
   '*' // All fields.
   {
      // Adds a willcard field.
      SQLResultSetField field = new SQLResultSetField();
      field.isWildcard = true;
      select.fieldList[select.fieldsCount] = field;
      select.fieldsCount++;
      select.hasWildcard = true;
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
      SQLResultSetField field = (SQLResultSetField)$1;

      // If the alias_name is null, the alias must be the name of the column. This was already done before.

      // If the alias is null and the field is a virtual column, raises an exception, since virtual columns require explicit aliases.
      if ($2 == null)
      {
         if (field.isVirtual)
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                      + LitebaseMessage.getMessage(LitebaseMessage.ERR_REQUIRED_ALIAS)
                                      + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
                                      
         $2 = field.alias; // The null alias name is filled as tableColName or tableName.tableColName, which was set before.
      }

      // Checks if the alias has not already been used by a predecessor.
      SQLResultSetField[] resultFieldList = select.fieldList;
      int i = select.fieldsCount - 1;
      
      while (--i >= 0)
         if (resultFieldList[i].alias.equals($2))
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                      + LitebaseMessage.getMessage(LitebaseMessage.ERR_DUPLICATE_ALIAS) + $2);

      field.aliasHashCode = (field.alias = $2).hashCode(); // Assigns the alias.
   }
;

opt_alias_name: // Optional alias.
   // null // No alias.
   {
      $$ = null;
   }
 | TK_AS TK_IDENT // There is an alias.
   {
      $$ = $2;
   }
;

field: // A field.
   pure_field // A pure field.
   {
      SQLResultSetField field = (SQLResultSetField)$1;
   
      if (select.fieldsCount == SQLSelectClause.MAX_NUM_FIELDS) // The  maximum number of fields can't be reached.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      field.tableColHashCode = field.tableColName.hashCode();
      select.fieldList[select.fieldsCount++] = field;
      select.hasRealColumns = true;
	   $$ = field;
   }
 | function_ident // A function applied to a field.
   {
      SQLResultSetField field = (SQLResultSetField)$1;
   
      if (select.fieldsCount == SQLSelectClause.MAX_NUM_FIELDS) // The  maximum number of fields can't be reached.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      // Sets the field.
      field.isDataTypeFunction = field.isVirtual = true;
      field.dataType = SQLElement.dataTypeFunctionsTypes[field.sqlFunction];

      // Sets the function parameter.
      SQLResultSetField paramField = new SQLResultSetField();
      field.parameter = paramField;
      paramField.alias = paramField.tableColName = field.tableColName;
      paramField.tableColHashCode = paramField.tableColName.hashCode();
      field.tableColHashCode = paramField.aliasHashCode = paramField.tableColHashCode;

      // Sets the select statement.
      select.fieldList[select.fieldsCount++] = field;
      
	   $$ = field;
   }
 | sql_function_aggregation // An aggregation function applied to a field.
   {
      SQLResultSetField field = (SQLResultSetField)$1;

      if (select.fieldsCount == SQLSelectClause.MAX_NUM_FIELDS) // The maximum number of fields can't be reached. 
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      // Sets the field.
      field.isAggregatedFunction = field.isVirtual = true;
      field.dataType = SQLElement.aggregateFunctionsTypes[field.sqlFunction];

      // Sets the parameter, if there is such one.
      if (field.sqlFunction != SQLElement.FUNCTION_AGG_COUNT)
      {
         // Sets the function parameter.
         SQLResultSetField paramField = new SQLResultSetField();
         field.parameter = paramField;
         paramField.alias = paramField.tableColName = field.tableColName;
         paramField.tableColHashCode = paramField.tableColName.hashCode();
         field.tableColHashCode = paramField.aliasHashCode = paramField.tableColHashCode;
      }

      // Sets the select statement.
      select.fieldList[select.fieldsCount++] = field;
      select.hasAggFunctions = true;
      
	   $$ = field;
   }
;

pure_field: // A pure field.
   TK_IDENT // A simple field.
   {
      SQLResultSetField f = new SQLResultSetField();
	   f.tableColName = f.alias = $1;
	   $$ = f;
   }
 | TK_IDENT TK_DOT TK_IDENT // table.fieldName
   {
      SQLResultSetField f = new SQLResultSetField();
	   f.tableColName = $3;
	   f.tableName = f.alias = $1;
	   f.alias += '.' + f.tableColName;
	   $$ = f;
   }
;

table_list: // Table list.
   table_def // Single table.
 | table_list TK_COMMA table_def // More than on table.
;

table_def: // Table definition.
   table opt_as opt_alias_table_name // The table and its optional alias.
   {
      if (aliasTableName == null)
	      aliasTableName = $1;
      
      // The table name alias must be unique.
      int hash = aliasTableName.hashCode();
      if (tables.exists(hash))
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_UNIQUE_ALIAS_TABLE) + aliasTableName);
      else
      	tables.put(hash, tables.size());
      
	   tableList[tableListSize++] = new SQLResultSetTable($1, aliasTableName);
   }
;

opt_as: // Optional keyword as.
   // null // No as.
   {}
 | TK_AS // The keyword as is present.
;

opt_where_clause: // Optional where clause.
   // null // No where clause.
   {}
   {
      $$ = null;
   }
 | TK_WHERE a_exp // There is a where clause.
   {
      // Compacts the field list of the where clause.
	   SQLBooleanClause clause = parserResult.whereClause;
      SQLResultSetField[] compactFieldList = new SQLResultSetField[clause.fieldsCount];
      Vm.arrayCopy(clause.fieldList, 0, compactFieldList, 0, clause.fieldsCount);
      clause.fieldList = compactFieldList;

	   $$ = $2;
   }
;

opt_group_by_clause:  // Optional group by clause.
   // null. // No group by.
   {}
 | TK_GROUP TK_BY group_by_commalist opt_having_clause // Group by present.
   {
      // Compacts the group by field list.
      SQLColumnListClause group_by = parserResult.group_by;
      SQLResultSetField[] compactFieldList = new SQLResultSetField[group_by.fieldsCount];
      Vm.arrayCopy(group_by.fieldList, 0, compactFieldList, 0, group_by.fieldsCount);
      group_by.fieldList = compactFieldList;

  	   if ($4 != null) // Adds the expression tree of the where clause.
         havingClause.expressionTree = (SQLBooleanClauseTree)$4;
   }

group_by_commalist: // Group by field list.
   field // A single field.
   {
      select.fieldsCount--; // Removes this field from the select list.
      addColumnFieldOrderGroupBy((SQLResultSetField)$1, true, false); // Adds this field to the group by field list.
   }
 | group_by_commalist TK_COMMA field // More than one field.
   {
      select.fieldsCount--; // Removes this field from the select list.
      addColumnFieldOrderGroupBy((SQLResultSetField)$3, true, false); // Adds this field to the group by field list.
   }
;

opt_having_clause: // Optional having expression.
   // null
   {
      $$ = null;
   }
 | token_having a_exp
   {
      // Compacts the having clause field list.
      SQLBooleanClause clause = parserResult.havingClause;
	   SQLResultSetField[] compactFieldList = new SQLResultSetField[clause.fieldsCount];
      Vm.arrayCopy(clause.fieldList, 0, compactFieldList, 0, clause.fieldsCount);
	   clause.fieldList = compactFieldList;

	   $$ = $2;
   }
;

token_having: // Indicates if the clause is a where or a having clause.
   TK_HAVING
   {
      isWhereClause = false;
   }
;

opt_order_by_clause: // Optional order by clause.
   // null
   {}
 | TK_ORDER TK_BY order_by_commalist
   {
      // Compacts the order by field list.
      SQLResultSetField[] compactFieldList = new SQLResultSetField[order_by.fieldsCount];
      Vm.arrayCopy(order_by.fieldList, 0, compactFieldList, 0, order_by.fieldsCount);
      order_by.fieldList = compactFieldList;
   }
;

order_by_commalist: // Order by field list.
   order_by_field // A single field.
 | order_by_commalist TK_COMMA order_by_field // More than one field.
;

order_by_field: // Order by field.
   field opt_direction // A field with the order direction.
   {
      select.fieldsCount--;
      addColumnFieldOrderGroupBy((SQLResultSetField)$1, ($2 == 0), true);
   }
;

opt_direction: // Order direction.
   // null // Default order: ascending.
   {}
   {
      $$ = 0;
   }
 | TK_ASC // Ascending order.
   {
      $$ = 0;
   }
 | TK_DESC // Descending order.
   {
      $$ = 1;
   }
;

a_exp: // Expression tree for a where clause and having clause.
   b_exp // Another expression type.
   {
      $$ = $1;
   }
 | a_exp TK_AND a_exp // And expression.
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_BOOLEAN_AND);
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   (tree.rightTree = (SQLBooleanClauseTree)$3).parent = tree;
	   $$ = tree;
   }
 | a_exp TK_OR a_exp // Or expression.
   {
      // juliana@213_1: changed the way a tree with ORs is built in order to speed up queries with indices.
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_BOOLEAN_OR);
	   (tree.leftTree = (SQLBooleanClauseTree)$3).parent = tree;
	   (tree.rightTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
 | '(' a_exp ')' // An expression between parenthesis.
   {
      $$ = $2;
   }
 | TK_NOT '(' a_exp ')' // A negated expression between parenthesis.
   {
      // The parent node will be the negation operator and the expression will be the right tree.
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_BOOLEAN_NOT);
	   (tree.rightTree = (SQLBooleanClauseTree)$3).parent = tree;
	   $$ = tree;
   }
;

b_exp: // The second expression type.
   TK_NOT b_exp // Negation expression.
   {
      // The parent node will be the negation operator and the expression will be the right tree.
	   SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_BOOLEAN_NOT);
	   (tree.rightTree = (SQLBooleanClauseTree)$2).parent = tree;
	   $$ = tree;
   }
 | exp math_operator exp // Math expression.
   {
      SQLBooleanClauseTree tree = (SQLBooleanClauseTree)$2;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   (tree.rightTree = (SQLBooleanClauseTree)$3).parent = tree;
	   $$ = tree;
   }
 | exp TK_IS TK_NULL // Null expression.
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_PAT_IS);
      (tree.rightTree = setOperandType(SQLElement.OP_PAT_NULL)).parent = tree;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
 | exp TK_IS TK_NOT TK_NULL // Not null expression.
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_PAT_IS_NOT);
	   (tree.rightTree = setOperandType(SQLElement.OP_PAT_NULL)).parent = tree;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
 | exp TK_LIKE TK_STR // Like expression.
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_PAT_MATCH_LIKE);
	   SQLBooleanClauseTree rightTree = new SQLBooleanClauseTree(parserResult.getInstanceBooleanClause());
	   rightTree.setOperandStringLiteral($3);
	   (tree.rightTree = rightTree).parent = tree;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
 | exp TK_NOT TK_LIKE TK_STR // Not like expression.
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_PAT_MATCH_NOT_LIKE);
	   SQLBooleanClauseTree rightTree = new SQLBooleanClauseTree(parserResult.getInstanceBooleanClause());
      rightTree.setOperandStringLiteral($4);
	   (tree.rightTree = rightTree).parent = tree;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
 | exp TK_LIKE TK_INTERROGATION // Like ? (prepared statement).
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_PAT_MATCH_LIKE);
	   SQLBooleanClause whereClause = getInstanceBooleanClause();
	   SQLBooleanClauseTree rightTree = new SQLBooleanClauseTree(whereClause);
	  
	   if (whereClause.paramCount == SQLElement.MAX_NUM_PARAMS) // There is a maximum number of parameters.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_PARAMS_REACHED)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      rightTree.isParameter = true;
      whereClause.paramList[whereClause.paramCount++] = rightTree;
	   (tree.rightTree = rightTree).parent = tree;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
 | exp TK_NOT TK_LIKE TK_INTERROGATION // Like not ? (prepared statement).
   {
      SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_PAT_MATCH_NOT_LIKE);
	   SQLBooleanClause whereClause = getInstanceBooleanClause();
	   SQLBooleanClauseTree rightTree = new SQLBooleanClauseTree(whereClause);

      if (whereClause.paramCount == SQLElement.MAX_NUM_PARAMS) // There is a maximum number of parameters.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_PARAMS_REACHED)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      rightTree.isParameter = true;
      whereClause.paramList[whereClause.paramCount++] = rightTree;
      (tree.rightTree = rightTree).parent = tree;
	   (tree.leftTree = (SQLBooleanClauseTree)$1).parent = tree;
	   $$ = tree;
   }
;

exp: // A single expression.
   pure_field // A field.
   {
      SQLBooleanClauseTree tree = new SQLBooleanClauseTree(getInstanceBooleanClause());
	   field = (SQLResultSetField)$1;
               
      int i = 1, 
          index = tree.booleanClause.fieldsCount;

      tree.operandType = SQLElement.OP_IDENTIFIER;
      int hashCode = field.tableColHashCode = tree.nameSqlFunctionHashCode = tree.nameHashCode = (tree.operandName = field.tableColName).hashCode();

      // rnovais@570_108: Generates different index to repeted columns on where
      // clause. Ex: where year(birth) = 2000 and birth = '2008/02/11'.
      while (tree.booleanClause.fieldName2Index.exists(tree.nameSqlFunctionHashCode))
         tree.nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
      
      if (index == SQLElement.MAX_NUM_COLUMNS) // There is a maximum number of columns.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_FIELDS_REACHED));

      // Puts the hash code of the function name in the hash table.
      tree.booleanClause.fieldName2Index.put(tree.nameSqlFunctionHashCode, index);

      field.aliasHashCode = field.alias.hashCode(); // Sets the hash code of the field alias.

      // Puts the field in the field list.
      tree.booleanClause.fieldList[index] = field;
      tree.booleanClause.fieldsCount++; 
      
	   $$ = tree;
   }
 | TK_NUMBER // A number.
   {
      SQLBooleanClauseTree tree = new SQLBooleanClauseTree(getInstanceBooleanClause());
      
      if (tree.operandValue == null)
         tree.operandValue = new SQLValue();
       
      tree.operandValue.asString = $1; // juliana@226a_20
      
      $$ = tree;
   }
 | TK_STR // A string.
   {
      SQLBooleanClauseTree tree = new SQLBooleanClauseTree(getInstanceBooleanClause());
	   tree.setOperandStringLiteral($1);
	   $$ = tree;
   }
 | TK_INTERROGATION // A ? (prepared statement parameter).
   {
      SQLBooleanClause whereClause = getInstanceBooleanClause();
	   SQLBooleanClauseTree tree = new SQLBooleanClauseTree(whereClause);

	   if (whereClause.paramCount == SQLElement.MAX_NUM_PARAMS) // There is a maximum number of parameters.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_PARAMS_REACHED)
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

      tree.isParameter = true;
      whereClause.paramList[whereClause.paramCount++] = tree;
	   $$ = tree;
   }
 | function_ident // A function applied to a field.
   {
      SQLBooleanClauseTree tree = new SQLBooleanClauseTree(getInstanceBooleanClause());
	   field = (SQLResultSetField)$1;
	  
	   i = 1;
      index = tree.booleanClause.fieldsCount;

      tree.operandType = SQLElement.OP_IDENTIFIER;
      int hashCode = tree.nameSqlFunctionHashCode = tree.nameHashCode = (tree.operandName = field.tableColName).hashCode();
   
      // generates different indexes to repeted columns on where clause.
      // Ex: where year(birth) = 2000 and day(birth) = 3.
      while (tree.booleanClause.fieldName2Index.exists(tree.nameSqlFunctionHashCode))
         tree.nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
              
      if (index == SQLElement.MAX_NUM_COLUMNS) // There is a maximum number of columns.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_FIELDS_REACHED));
   
      // Puts the hash code of the function name in the hash table.
      tree.booleanClause.fieldName2Index.put(tree.nameSqlFunctionHashCode, index);
   
      paramField = field.parameter = new SQLResultSetField(); // Creates the parameter field.
              
      // Sets the field and function parameter fields.
      paramField.alias = paramField.tableColName = field.alias = field.tableColName = tree.operandName;
      paramField.aliasHashCode = paramField.tableColHashCode = field.tableColHashCode = field.aliasHashCode = tree.nameHashCode;
      field.dataType = SQLElement.dataTypeFunctionsTypes[field.sqlFunction];
      field.isDataTypeFunction = field.isVirtual = true;
   
      // Puts the field in the field list.
      tree.booleanClause.fieldList[index] = field;
      tree.booleanClause.fieldsCount++;
	  
	   $$ = tree;
   }
;

math_operator: // Math operator.
   TK_EQUAL // ==
   {
      $$ = setOperandType(SQLElement.OP_REL_EQUAL);
   }
 | TK_DIFF // <> or !=
   {
      $$ = setOperandType(SQLElement.OP_REL_DIFF);
   }
 | TK_GREATER // >
   {
      $$ = setOperandType(SQLElement.OP_REL_GREATER);
   }
 | TK_LESS // <
   {
      $$ = setOperandType(SQLElement.OP_REL_LESS);
   }
 | TK_GREATER_EQUAL // >=
   {
      $$ = setOperandType(SQLElement.OP_REL_GREATER_EQUAL);
   }
 | TK_LESS_EQUAL // <=
   {
      $$ = setOperandType(SQLElement.OP_REL_LESS_EQUAL);
   }
;

function_ident: // A function applied to a field.
   TK_ABS '(' pure_field ')' // Abs function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_ABS;
	   $$ = f;
   }
 | TK_UPPER '(' pure_field ')' // Upper function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_UPPER;
	   $$ = f;
   }
 | TK_LOWER '(' pure_field ')' // Lower function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_LOWER;
	   $$ = f;
   }
 | TK_YEAR '(' pure_field ')' // Year function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_YEAR;
	   $$ = f;
   }
 | TK_MONTH '(' pure_field ')' // Month function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_MONTH;
	   $$ = f;
   }
 | TK_DAY '(' pure_field ')' // Day function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_DAY;
	   $$ = f;
   }
 | TK_HOUR '(' pure_field ')' // Hour function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
      f.sqlFunction = SQLElement.FUNCTION_DT_HOUR;
	   $$ = f;
   }
 | TK_MINUTE '(' pure_field ')' // Minute function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
      f.sqlFunction = SQLElement.FUNCTION_DT_MINUTE;
      $$ = f;
   }
 | TK_SECOND '(' pure_field ')' // Second function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
	   f.sqlFunction = SQLElement.FUNCTION_DT_SECOND;
	   $$ = f;
   }
 | TK_MILLIS '(' pure_field ')' // Millis function.
   {
      SQLResultSetField f = (SQLResultSetField)$3;
      f.sqlFunction = SQLElement.FUNCTION_DT_MILLIS;
	   $$ = f;
   }
;

sql_function_aggregation: // An aggregation function applied to a field.
   TK_COUNT '(' '*' ')' // Count aggregation function.
   {
      SQLResultSetField field = new SQLResultSetField();
	   field.sqlFunction = SQLElement.FUNCTION_AGG_COUNT;
	   $$ = field;
   }
 | TK_MAX '(' pure_field ')' // Max aggregation function.
   {
      SQLResultSetField field = (SQLResultSetField)$3;
	   field.sqlFunction = SQLElement.FUNCTION_AGG_MAX;
	   $$ = field;
   }
 | TK_MIN '(' pure_field ')' // Min aggregation function.
   {
      SQLResultSetField field = (SQLResultSetField)$3;
	   field.sqlFunction = SQLElement.FUNCTION_AGG_MIN;
	   $$ = field;
   }
 | TK_AVG '(' pure_field ')' // Avg aggregation function.
   {
      SQLResultSetField field = (SQLResultSetField)$3;
	   field.sqlFunction = SQLElement.FUNCTION_AGG_AVG;
	   $$ = field;
   }
 | TK_SUM '(' pure_field ')' // Sum aggregation function.
   {
      SQLResultSetField field = (SQLResultSetField)$3;
	   field.sqlFunction = SQLElement.FUNCTION_AGG_SUM;
	   $$ = field;
   }
;

%%
   /** 
    * The lexical analyzer.
    */
   private LitebaseLex lexer;

   /**
    * Indicates if a table field is the primary key.
    */
   private boolean isPrimaryKey;

   /**
    * Indicates if a field can have a <code>null</code> value.
    */
   private boolean isNotNull;

   /** 
    * Indicates if the case of a string field shoul be compared taking its case into consideration or not. <code>true</code> indicates caseless 
    * comparison. <code>false</code>, otherwise.
    */
   private boolean isNocase;

   /** 
    * Stores a default value for a field.
    */
   private String strDefault;

   /** 
    * An alias for the table name.
    */
   private String aliasTableName;

   /** 
    * The first table name found in an update statement.
    */
   private String firstFieldUpdateTableName;

   /** 
    * The first table alias found in an update statement.
    */
   private String firstFieldUpdateAlias;

   /** 
    * The second table name found in an update statement, which indicates an error.
    */
   private String secondFieldUpdateTableName;

   /** 
    * The second table alias found in an update statement, which indicates an error.
    */
   private String secondFieldUpdateAlias;

   /** 
    * Counts the number of simple primary keys, which must be only one.
    */
   private int number_pk = 0;
   
   /**
    * The type of SQL command, which can be one of: <b><code>CMD_CREATE_TABLE</b></code>, <b><code>CMD_CREATE_INDEX</b></code>, 
    * <b><code>CMD_DROP_TABLE</b></code>, <b><code>CMD_DROP_INDEX</b></code>, <b><code>CMD_ALTER_DROP_PK</b></code>, 
    * <b><code>CMD_ALTER_ADD_PK</b></code>, <b><code>CMD_ALTER_RENAME_TABLE</b></code>, <b><code>CMD_ALTER_RENAME_COLUMN</b></code>, 
    * <b><code>CMD_SELECT</b></code>, <b><code>CMD_INSERT</b></code>, <b><code>CMD_UPDATE</b></code>, or <b><code>CMD_DELETE</b></code>.
    */
   int command;

   /**
    * The resulting set table list, used with all statements.
    */
   SQLResultSetTable[] tableList;

   /**
    * The number of tables in the table list.
    */
   int tableListSize;

   /**
    * The field list for the SQL commands except <code>SELECT</code>. 
    */
   SQLFieldDefinition[] fieldList;

   /**
    * The number of fields in the field list.
    */
   int fieldListSize;

   /**
    * Contains field values (strings) used on insert/update statements.
    */
   String[] fieldValues;

   /**
    * The number of fields of values in the field values list.
    */
   int fieldValuesSize;

   /**
    * The field list for inserts, updates and indices.
    */
   String[] fieldNames;

   /**
    * The number of fields of the update field list.
    */
   int fieldNamesSize;

   /**
    * This is used to differ between a where clause and a having clause. Before parsing the having clause, <code>isWhereClause</code> is set to 
    * false. So the <code>getInstanceBooleanClause()</code> method will return a having clause, otherwise it returns a where clause.
    */
   boolean isWhereClause = true;

   /**
    * The where clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause whereClause;

   /**
    * The having clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause havingClause;

   /**
    * The initial part of the <code>SELECT</code> statement
    */
   SQLSelectClause select;

   /**
    * The order by part of a <code>SELECT</code> statement.
    */
   SQLColumnListClause order_by;

   /**
    * The group by part of a <code>SELECT</code> statement.
    */
   SQLColumnListClause group_by;
   
   /**
    * A hashtable to be used on select statements to verify if it has repeated table names.
    */
   IntHashtable tables;

   /** 
    * The lex main method.
    *
    * @return The token code, -1 if the end of file was reached or 256 if there was a lexical error.
    */
   private int yylex()
   {
   	yylval = new LitebaseParserVal();
      return = lexer.yylex();
   }

   /**
    * The method which executes the parser process.
    *
    * @param sql The sql command to be parsed.
    * @param parser The parser object which will be filled with the result of the parsing process.
    * @param lexer The lexical analizer.
    */
   static void parser(String sql, LitebaseParser parser, LitebaseLex lexer)
   {
      LitebaseParser yyparser = parser; // Initializes the parser.
      
      // juliana@224_2: improved memory usage on BlackBerry.
      yyparser.lexer = lexer;
      lexer.zzReaderChars = sql;
      lexer.yyparser = parser;
      lexer.yycurrent = ' ';
      lexer.yyposition = 0;      
      
      yyparser.yyparse();
   }

   /** 
    * Sets the operand type.
    *
    * @param The operand type.
    * @return A boolean clause tree with this operand type.
    */
   private SQLBooleanClauseTree setOperandType(int operandType)
   {
      SQLBooleanClauseTree tree = new SQLBooleanClauseTree(getInstanceBooleanClause());
      tree.operandType = operandType;
      return tree;
   }

   /** 
    * Adds a column field to the order field list.
    *
    * @param field The field to be added.
    * @param isAscending Indicates the ordering used.
    * @param isOrder by <code>true</code> if the field comes from an order be clause; <code>false</code>, otherwise.
    */
   private void addColumnFieldOrderGroupBy(SQLResultSetField field, boolean isAscending, boolean isOrderBy)
   {
      SQLColumnListClause listClause = isOrderBy? getInstanceColumnListClauseOrderBy() : getInstanceColumnListClauseGroupBy();

      if (listClause.fieldsCount == SQLElement.MAX_NUM_COLUMNS) // The  maximum number of columns in a list clause can't be reached.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                   + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY));

      int hashCode = field.tableColName.hashCode();
      field.tableColHashCode = hashCode;
      field.aliasHashCode = hashCode;
      field.isAscending = isAscending;
      listClause.fieldList[listClause.fieldsCount++] = field;
   }
   
   /**
    * Gets an instance of a where clause or a having clause, depending of what clause is used in the <code>SELECT</code> statement.
    *
    * @return The instance of a where clause or a having clause.
    */
   SQLBooleanClause getInstanceBooleanClause()
   {
      if (isWhereClause) // where clause
      {
         if (whereClause == null)
            whereClause = new SQLBooleanClause();
         return whereClause;
      }
      else // having clause
      {
         SQLBooleanClause having = havingClause;
         if (having == null)
            having = havingClause = new SQLBooleanClause();
         having.isWhereClause = false; 
         return having;
      }
   }

   /**
    * Gets an instance of an order by clause used in the <code>SELECT</code> statement.
    *
    * @return The order by clause.
    */
   SQLColumnListClause getInstanceColumnListClauseOrderBy()
   {
      if (order_by == null)
         order_by = new SQLColumnListClause();
      return order_by;
   }

   /**
    * Gets an instance of a group by clause used in the <code>SELECT</code> statement.
    *
    * @return The group by clause.
    */
   SQLColumnListClause getInstanceColumnListClauseGroupBy()
   {
      if (group_by == null)
         group_by = new SQLColumnListClause();
      return group_by;
   }