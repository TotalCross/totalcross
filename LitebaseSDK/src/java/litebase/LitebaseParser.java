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

package litebase;

import totalcross.sys.*;
import totalcross.util.*;

// juliana@253_9: improved Litebase parser.

/**
 * This class calls <code>yyparse()</code> and builds the parser result.
 */
class LitebaseParser
{
   /**
    * <code>CHAR</code> keyword token.
    */
   final static int TK_CHAR = 0;
   
   /**
    * <code>SHORT</code> keyword token.
    */
   final static int TK_SHORT = 1;
   
   /**
    * <code>INT</code> keyword token.
    */
   final static int TK_INT = 2;
   
   /**
    * <code>LONG</code> keyword token.
    */
   final static int TK_LONG = 3;
   
   /**
    * <code>FLOAT</code> keyword token.
    */
   final static int TK_FLOAT = 4;
      
   /**
    * <code>DOUBLE</code> keyword token.
    */
   final static int TK_DOUBLE = 5;
   
   /**
    * <code>VARCHAR</code> keyword token.
    */
   final static int TK_VARCHAR = 6;
   
   /**
    * <code>NOCASE</code> keyword token.
    */
   final static int TK_NOCASE = 7;
   
   /**
    * <code>DATE</code> keyword token.
    */
   final static int TK_DATE = 8;
   
   /**
    * <code>DATETIME</code> keyword token.
    */
   final static int TK_DATETIME = 9;
   
   /**
    * <code>BLOB</code> keyword token.
    */
   final static int TK_BLOB = 10;
   
   /**
    * <code>ABS</code> keyword token.
    */
   final static int TK_ABS = 11;
   
   /**
    * <code>ADD</code> keyword token.
    */
   final static int TK_ADD = 12;
   
   /**
    * <code>ALTER</code> keyword token.
    */
   final static int TK_ALTER = 13;
   
   /**
    * <code>AND</code> keyword token.
    */
   final static int TK_AND = 14;
   
   /**
    * <code>AS</code> keyword token.
    */
   final static int TK_AS = 15;
   
   /**
    * <code>ASC</code> keyword token.
    */
   final static int TK_ASC = 16;
   
   /**
    * <code>AVG</code> keyword token.
    */
   final static int TK_AVG = 17;
 
   /**
    * <code>BY</code> keyword token.
    */
   final static int TK_BY = 18;

   /**
    * <code>COUNT</code> keyword token.
    */
   final static int TK_COUNT = 19;

   /**
    * <code>CREATE</code> keyword token.
    */
   final static int TK_CREATE = 20;

   /**
    * <code>DAY</code> keyword token.
    */
   final static int TK_DAY = 21;
   
   /**
    * <code>DEFAULT</code> keyword token.
    */
   final static int TK_DEFAULT = 22;
   
   /**
    * <code>DELETE</code> keyword token.
    */
   final static int TK_DELETE = 23;
   
   /**
    * <code>DESC</code> keyword token.
    */
   final static int TK_DESC = 24;
   
   /**
    * <code>DISTINCT</code> keyword token.
    */
   final static int TK_DISTINCT = 25;
   
   /**
    * <code>DROP</code> keyword token.
    */
   final static int TK_DROP = 26;  
   
   /**
    * <code>FROM</code> keyword token.
    */
   final static int TK_FROM = 27;
   
   /**
    * <code>GROUP</code> keyword token.
    */
   final static int TK_GROUP = 28;
   
   /**
    * <code>HAVING</code> keyword token.
    */
   final static int TK_HAVING = 29;
   
   /**
    * <code>HOUR</code> keyword token.
    */
   final static int TK_HOUR = 30;
   
   /**
    * <code>INDEX</code> keyword token.
    */
   final static int TK_INDEX = 31;
   
   /**
    * <code>INSERT</code> keyword token.
    */
   final static int TK_INSERT = 32;
 
   /**
    * <code>INTO</code> keyword token.
    */
   final static int TK_INTO = 33;
   
   /**
    * <code>IS</code> keyword token.
    */
   final static int TK_IS = 34;
   
   /**
    * <code>KEY</code> keyword token.
    */
   final static int TK_KEY = 35;
   
   /**
    * <code>LIKE</code> keyword token.
    */
   final static int TK_LIKE = 36;

   /**
    * <code>LOWER</code> keyword token.
    */
   final static int TK_LOWER = 37;
   
   /**
    * <code>MAX</code> keyword token.
    */
   final static int TK_MAX = 38;
   
   /**
    * <code>MILLIS</code> keyword token.
    */
   final static int TK_MILLIS = 39;
   
   /**
    * '(' token.
    */
   final static int TK_OPEN = 40;
   
   /**
    * ')' token.
    */
   final static int TK_CLOSE = 41;
   
   /**
    * '*' token.
    */
   final static int TK_ASTERISK = 42;
   
   /**
    * <code>MIN</code> keyword token.
    */
   final static int TK_MIN = 43;
   
   /**
    * ',' token.
    */
   final static int TK_COMMA = 44;
   
   /**
    * <code>MINUTE</code> keyword token.
    */
   final static int TK_MINUTE = 45;
   
   /**
    * '.' token.
    */
   final static int TK_DOT = 46;
   
   /**
    * <code>MONTH</code> keyword token.
    */
   final static int TK_MONTH = 47;

   /**
    * <code>NOT</code> keyword token.
    */
   final static int TK_NOT = 48;
   
   /**
    * <code>NULL</code> keyword token.
    */
   final static int TK_NULL = 49;
   
   /**
    * <code>ON</code> keyword token.
    */
   final static int TK_ON = 50;
   
   /**
    * <code>OR</code> keyword token.
    */
   final static int TK_OR = 51;
   
   /**
    * <code>ORDER</code> keyword token.
    */
   final static int TK_ORDER = 52;
   
   /**
    * <code>PRIMARY</code> keyword token.
    */
   final static int TK_PRIMARY = 53;
   
   /**
    * <code>RENAME</code> keyword token.
    */
   final static int TK_RENAME = 54;
   
   /**
    * <code>SECOND</code> keyword token.
    */
   final static int TK_SECOND = 55;
   
   /**
    * <code>SELECY</code> keyword token.
    */
   final static int TK_SELECT = 56;
   
   /**
    * <code>SET</code> keyword token.
    */
   final static int TK_SET = 57;

   /**
    * <code>SUM</code> keyword token.
    */
   final static int TK_SUM = 58;
   
   /**
    * <code>TABLE</code> keyword token.
    */
   final static int TK_TABLE = 59;
   
   /**
    * '<' token.
    */
   final static int TK_LESS = 60;
   
   /**
    * '=' token.
    */
   final static int TK_EQUAL = 61;
   
   /**
    * '>' token.
    */
   final static int TK_GREATER = 62;
   
   /**
    * '?' token.
    */
   final static int TK_INTERROGATION = 63;
   
   /**
    * <code>TO</code> keyword token.
    */
   final static int TK_TO = 64;
   
   /**
    * <code>UPPER</code> keyword token.
    */
   final static int TK_UPPER = 65;
   
   /**
    * <code>VALUES</code> keyword token.
    */
   final static int TK_VALUES = 66;
   
   /**
    * <code>UPDATE</code> keyword token.
    */
   final static int TK_UPDATE = 67;
   
   /**
    * <code>WHERE</code> keyword token.
    */
   final static int TK_WHERE = 68;
   
   /**
    * <code>YEAR</code> keyword token.
    */
   final static int TK_YEAR = 69;
   
   /**
    * Identifier token.
    */
   final static int TK_IDENT = 70;

   /**
    * Numerical token.
    */
   final static int TK_NUMBER = 71;

   /**
    * String token.
    */
   final static int TK_STR = 72;

   /**
    * <code>'>='</code> token.
    */
   final static int TK_GREATER_EQUAL = 73;

   /**
    * <code>'<='</code> token.
    */
   final static int TK_LESS_EQUAL = 74;

   /**
    * <code>'!='</code> or <code>'<>'</code> token.
    */
   final static int TK_DIFF = 75;

   /**
    * The 'lval' (result) got from <code>yylex()</code>.
    */
   String yylval;
   
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
    * The lexical analyzer.
    */
   private LitebaseLex lexer;
   
   /**
    * The type of SQL command, which can be one of: <b><code>CMD_CREATE_TABLE</b></code>, <b><code>CMD_CREATE_INDEX</b></code>, 
    * <b><code>CMD_DROP_TABLE</b></code>, <b><code>CMD_DROP_INDEX</b></code>, <b><code>CMD_ALTER_DROP_PK</b></code>, 
    * <b><code>CMD_ALTER_ADD_PK</b></code>, <b><code>CMD_ALTER_RENAME_TABLE</b></code>, <b><code>CMD_ALTER_RENAME_COLUMN</b></code>, 
    * <b><code>CMD_SELECT</b></code>, <b><code>CMD_INSERT</b></code>, <b><code>CMD_UPDATE</b></code>, or <b><code>CMD_DELETE</b></code>.
    */
   int command;
   
   /**
    * The number of fields in the field list.
    */
   int fieldListSize;
   
   /**
    * The number of fields of values in the field values list.
    */
   int fieldValuesSize;

   /**
    * The number of fields of the update field list.
    */
   int fieldNamesSize;
   
   /**
    * The number of tables in the table list.
    */
   int tableListSize;

   /** 
    * Counts the number of simple primary keys, which must be only one.
    */
   private int number_pk;
   
   /**
    * This is used to differ between a where clause and a having clause. Before parsing the having clause, <code>isWhereClause</code> is set to 
    * false. So the <code>getInstanceBooleanClause()</code> method will return a having clause, otherwise it returns a where clause.
    */
   boolean isWhereClause = true;
   
   /**
    * Contains field values (strings) used on insert/update statements.
    */
   String[] fieldValues;

   /**
    * The field list for inserts, updates and indices.
    */
   String[] fieldNames;
   
   /**
    * The resulting set table list, used with all statements.
    */
   SQLResultSetTable[] tableList;

   /**
    * The field list for the SQL commands except <code>SELECT</code>. 
    */
   SQLFieldDefinition[] fieldList;

   /**
    * The where clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause whereClause;

   /**
    * The having clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause havingClause;
   
   /**
    * An auxiliary expression tree.
    */
   SQLBooleanClauseTree auxTree;
   
   /**
    * An auxiliary field.
    */
   SQLResultSetField auxField;

   /**
    * The initial part of the <code>SELECT</code> statement
    */
   SQLSelectClause select;

   /**
    * The order by part of a <code>SELECT</code> statement.
    */
   SQLColumnListClause orderBy;

   /**
    * The group by part of a <code>SELECT</code> statement.
    */
   SQLColumnListClause groupBy;

   /**
    * The lex main method.
    *
    * @return The token code, -1 if the end of file was reached or 256 if there was a lexical error.
    */
   private int yylex()
   {
      return lexer.yylex();
   }
   
   /**
    * The method which executes the parser process.
    *
    * @param sql The sql command to be parsed.
    * @param parser The parser object which will be filled with the result of the parsing process.
    * @param lexer The lexical analyzer.
    * @throws InvalidNumberException If an internal method throws it. 
    */
   static void parser(String sql, LitebaseParser parser, LitebaseLex lexer) throws InvalidNumberException
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
      if (orderBy == null)
         orderBy = new SQLColumnListClause();
      return orderBy;
   }

   /**
    * Gets an instance of a group by clause used in the <code>SELECT</code> statement.
    *
    * @return The group by clause.
    */
   SQLColumnListClause getInstanceColumnListClauseGroupBy()
   {
      if (groupBy == null)
         groupBy = new SQLColumnListClause();
      return groupBy;
   }

   /**
    * Parses input and execute indicated items.
    * @throws InvalidNumberException If an internal method throws it.
    */
   private void yyparse() throws InvalidNumberException
   {     
      int token = LitebaseLex.YYEOF;
      String tableName;
      
      switch (yylex())
      {
         case TK_ALTER: // Alter table.
            if (yylex() != TK_TABLE || yylex() != TK_IDENT)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
            
            switch (yylex())
            {
               case TK_ADD: // Adds a primary key or a new column.
                  
                  // juliana@253_22: added command ALTER TABLE ADD column.
                  if ((token = createColumn()) == TK_PRIMARY) // Adds a primary key.
                  {
                     if (fieldListSize == 1)
                        yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                     if (yylex() != TK_KEY || yylex() != TK_OPEN || colnameCommaList() != TK_CLOSE)
                        yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);                  
                     command = SQLElement.CMD_ALTER_ADD_PK;
                  }
                  else if (token == -1) // Adds a new column.
                  {
                     SQLFieldDefinition field = fieldList[0];
                     if (field.isNotNull && field.defaultValue == null) // A field declared as not null must have a default value.
                        yyerror(LitebaseMessage.ERR_NOT_NULL_DEFAULT);
                     if (field.isPrimaryKey) // The new field can't be declared as a primary key when being added.
                     {   
                        if (field.defaultValue != null) // All the keys would be the same.
                           yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_STATEMENT_CREATE_DUPLICATED_PK) 
                                                                       + tableList[0].tableName);
                        
                        yyerror(LitebaseMessage.ERR_PK_CANT_BE_NULL); // All the keys would be null.
                     }
                     command = SQLElement.CMD_ALTER_ADD_COLUMN;
                  }
                  
                  break;
               
               case TK_DROP: // Drops a primary key.
                  if (yylex() != TK_PRIMARY || yylex() != TK_KEY)
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  command = SQLElement.CMD_ALTER_DROP_PK;
                  break;
               
               case TK_RENAME: // Renames the table or a column.
                  if ((token = yylex()) == TK_IDENT) // Rename column.
                  {
                     command = SQLElement.CMD_ALTER_RENAME_COLUMN;
                     fieldNames[1] = yylval;
                     token = yylex();
                  }
                  else // Rename table.
                     command = SQLElement.CMD_ALTER_RENAME_TABLE;
                  
                  // New name.
                  if (token != TK_TO || yylex() != TK_IDENT) 
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  fieldNames[0] = yylval;
                  
                  break;
               
               default:
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            }
            
            token = yylex();
            break;
                        
         case TK_CREATE:
         {
            switch (yylex())
            {
               case TK_TABLE: // Create table.
                  if (yylex() != TK_IDENT || yylex() != TK_OPEN)
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
                  
                  // Primary key.
                  if ((token = createColumnCommalist()) == TK_PRIMARY && yylex() == TK_KEY && yylex() == TK_OPEN && colnameCommaList() == TK_CLOSE)
                  {
                     if (number_pk == 1)
                        yyerror(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED);
                     token = yylex();
                  }
                  
                  if (token != TK_CLOSE) // End of create table.
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);                  

                  command = SQLElement.CMD_CREATE_TABLE;
                  break;
                  
               case TK_INDEX: // Create index.
                  if (yylex() != TK_IDENT || yylex() != TK_ON || yylex() != TK_IDENT)
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
                  if (yylex() != TK_OPEN || colnameCommaList() != TK_CLOSE) // Column name list.
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  command = SQLElement.CMD_CREATE_INDEX;
                  break;
                  
               default:
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            }  
            
            token = yylex();
            break;
         }
         case TK_DELETE: // Delete.
            if (!(((token = yylex()) == TK_FROM && yylex() == TK_IDENT) || token == TK_IDENT))
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            tableName = yylval;
            
            if ((token = yylex()) == TK_IDENT) // Alias table name.
            {
               tableList[0] = new SQLResultSetTable(tableName, yylval);
               token = yylex();
            }
            else 
               tableList[0] = new SQLResultSetTable(tableName, tableName);
            
            token = optWhereClause(token); // Where clause.
            command = SQLElement.CMD_DELETE;
            break;
         
         case TK_DROP:
            switch (yylex())
            {
               case TK_TABLE: // Drop table.
                  if (yylex() == TK_IDENT)
                     tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
                  else
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  command = SQLElement.CMD_DROP_TABLE;
                  break;
               
               case TK_INDEX: // Drop index.                             
                  if ((token = colnameCommaList()) != TK_ON || yylex() != TK_IDENT) 
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.                     
                  command = SQLElement.CMD_DROP_INDEX;
                  break;
               
               default:
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            } 
            
            token = yylex();
            break;
            
         case TK_INSERT: // Insert.
            if (yylex() != TK_INTO || yylex() != TK_IDENT)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
                        
            if ((token = yylex()) == TK_OPEN) // Reads the field list.
            {
               if (colnameCommaList() != TK_CLOSE)
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);   
               token = yylex();
            }
            
            if (token != TK_VALUES || yylex() != TK_OPEN || listValues() != TK_CLOSE) // Reads the value list.
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            
            // If the default order is not used, the number of values must be equal to the number of fields.
            if (fieldNamesSize != 0 && fieldNamesSize != fieldValuesSize) 
               yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH) 
                                                                           + '(' + fieldNamesSize + " != " + fieldValuesSize + ')');
     
            command = SQLElement.CMD_INSERT;
            token = yylex();
            break;
            
         case TK_SELECT: // Select.
            if ((token = yylex()) == TK_DISTINCT) 
               token = yylex();
            if (fieldExp(token) != TK_FROM)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            
            token = optWhereClause(tableList()); // Table list and where clause.
            
            // order by and group by.
            if (token == TK_GROUP) 
            {
               if (yylex() != TK_BY)
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
               token = groupByClause();
            } 
            if (token == TK_ORDER) 
            {
               if (yylex() != TK_BY)
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
               token = orderByClause();
            }
            
            SQLSelectClause selectAux = select;
            
            command = SQLElement.CMD_SELECT;
            selectAux.tableList = new SQLResultSetTable[tableListSize];
            Vm.arrayCopy(tableList, 0, selectAux.tableList, 0, tableListSize);

            // Checks if the first field is the wild card. If so, assigns null to list, to indicate that all fields must be included.
            if (selectAux.fieldList[0].isWildcard)
            {
               selectAux.fieldList = null;
               selectAux.fieldsCount = 0;
            } 
            else 
            {
               // Compacts the resulting field list.
               SQLResultSetField[] compactFieldList = new SQLResultSetField[selectAux.fieldsCount];
               Vm.arrayCopy(selectAux.fieldList, 0, compactFieldList, 0, selectAux.fieldsCount);
               selectAux.fieldList = compactFieldList;
            }

            break;
            
         case TK_UPDATE: // Update.
            String tableAlias = null;
            
            // Table name.
            if (yylex() != TK_IDENT)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            tableAlias = tableName = yylval;
            
            if ((token = yylex()) == TK_IDENT) // Alias table name.
            {   
               tableAlias = yylval;
               token = yylex();
            }
            
            if (token != TK_SET) // set key word.
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            
            token = optWhereClause(updateExpCommalist()); // Update expression list and where clause.
            
            if (secondFieldUpdateTableName != null) // Verifies if there was an error on field.tableName.
            {
               if (!tableAlias.equals(firstFieldUpdateTableName))
                  yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + firstFieldUpdateAlias);
               else
                  yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + secondFieldUpdateAlias);
            } 
            else if (firstFieldUpdateTableName != null && !tableAlias.equals(firstFieldUpdateTableName))
               yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + firstFieldUpdateAlias);

            command = SQLElement.CMD_UPDATE;
            tableList[0] = new SQLResultSetTable(tableName, tableAlias);
            break;
         
         default:
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);         
      }
      if (token != LitebaseLex.YYEOF)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);     
   }
   
   /**
    * Deals with a list of identifiers separated by commas.
    * 
    * @return The token after the list of identifiers. 
    */
   private int colnameCommaList()
   {
      int token,
          fieldNamesSizeAux = fieldNamesSize;
      String[] fieldNamesAux = fieldNames;
      
      do
      {
         if ((token = yylex()) == TK_ASTERISK) // This is necessary for dropping all indices.
         {
            fieldNamesAux[fieldNamesSizeAux++] = "*";
            return yylex();
         }
         
         if (token != TK_IDENT)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         fieldNamesAux[fieldNamesSizeAux++] = yylval; // Adds the column name.  
      }
      while ((token = yylex()) == TK_COMMA);          
      fieldNamesSize = fieldNamesSizeAux;
      return token;
   }
   
   /**
    * Deals with a list of rows of a table being created.
    * 
    * @return The token after the list of rows. 
    * @throws InvalidNumberException If an internal method throws it.
    */
   private int createColumnCommalist() throws InvalidNumberException
   {
      int token;
      
      while ((token = createColumn()) == TK_COMMA);
      if (fieldListSize == 0) // The number of columns can't be zero.
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);

      return token;      
   }
   
   /**
    * Deals with a column declaration.
    * 
    * @return The token after a column declaration. 
    * @throws InvalidNumberException If an internal method throws it.
    */
   private int createColumn() throws InvalidNumberException
   {
      int token,
          type,
          size = 0;
      boolean isPrimaryKey = false,
              isNotNull = false;
      String columnName,
             strDefault = null;
            
      if ((token = yylex()) == TK_PRIMARY) // The next token after ',' is a primary key declaration. This is not treated here.
         return token;
      
      // Column name.
      if (token != TK_IDENT)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      columnName = yylval;
      
      // Column type.
      if ((type = yylex()) == SQLElement.BOOLEAN || type > SQLElement.BLOB || type < SQLElement.CHARS)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      if (type == TK_VARCHAR)
         type = SQLElement.CHARS;
      
      if (type == SQLElement.CHARS || type == SQLElement.BLOB) // Size and multiplier. 
      {
         if (yylex() == TK_OPEN && yylex() == TK_NUMBER)
         {
            if ((size = Convert.toInt(yylval)) <= 0)
               yyerror(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT);
            
            // juliana@253_15: now an exception is thrown if the size of a CHAR or VARCHAR is greater than 65535. 
            if (type == SQLElement.CHARS && size > (Convert.MAX_SHORT_VALUE << 1) + 1)
               yyerror(LitebaseMessage.ERR_CHAR_TOO_BIG);
         }
         else
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         
         if (type == SQLElement.CHARS && yylex() != TK_CLOSE)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         else if (type == SQLElement.BLOB) 
         {   
            if ((token = yylex()) != TK_IDENT && token != TK_CLOSE)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);   
            if (token == TK_IDENT)
            {
               if (yylval.equals("k")) // kilobytes.
                  size <<= 10;
               else if (yylval.equals("m")) // megabytes.
                  size <<= 20;
               else
                  yyerror(LitebaseMessage.ERR_INVALID_MULTIPLIER);
               if (yylex() != TK_CLOSE)
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);   
            }
            if (size > (10 << 20))  // There is a size limit for a blob!
               yyerror(LitebaseMessage.ERR_BLOB_TOO_BIG);
         }            
      }   
      
      if ((token = yylex()) == TK_NOCASE) // No case.
      {   
         if (type == SQLElement.CHARS)
            type = SQLElement.CHARS_NOCASE;
         else
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         token = yylex();
      }

      if (token == TK_PRIMARY) // Simple primary key.
      {
         if (yylex() != TK_KEY)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         if (number_pk++ == 1)
            yyerror(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED);
         if (type == SQLElement.BLOB)
            yyerror(LitebaseMessage.ERR_BLOB_PRIMARY_KEY);
         token = yylex();
         isPrimaryKey = true;
      }
      
      if (token == TK_DEFAULT) // Default value.
      {
         if ((token = yylex()) == TK_NUMBER || token == TK_STR)
            strDefault = yylval;
         else if (token != TK_NULL)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         
         if (type == SQLElement.BLOB) // A blob can't have a default value.
            yyerror(LitebaseMessage.ERR_BLOB_STRING);
         
         // A numeric type must have a number as a default value. A string, date or datetime type must have a string as a default value.
         if (((type == SQLElement.CHARS || type == SQLElement.CHARS_NOCASE || type == SQLElement.DATE || type == SQLElement.DATETIME) && token == TK_NUMBER)
           || ((type > SQLElement.CHARS && type < SQLElement.CHARS_NOCASE) && token == TK_STR))
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);

         token = yylex();         
      }
      
      if (token == TK_NOT) // Not null.
      {
         if (yylex() != TK_NULL)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
         token = yylex();
         isNotNull = true;
      }
      
      fieldList[fieldListSize++] = new SQLFieldDefinition(columnName, type, size, isPrimaryKey, strDefault, isNotNull);
      return token;
   }

   /**
    * Deals with an expression of an expression tree of a where clause.
    * 
    * @param token The first token of the expression.
    * @return The token after the expression.
    */
   private int expression(int token) 
   {     
      if ((token = term(token)) == TK_OR) // expression = term or expression | term
      {
         // juliana@213_1: changed the way a tree with ORs is built in order to speed up queries with indices.
         SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_BOOLEAN_OR);
         
         (tree.rightTree = auxTree).parent = tree;
         token = expression(yylex());
         (tree.leftTree = auxTree).parent = tree;         
         auxTree = tree;   
      }

      return token;
   }
   
   /**
    * Deals with a term of an expression tree of a where clause.
    * 
    * @param token The first token of the term.
    * @return The token after the term.
    */
   private int term(int token)
   {      
      if ((token = factor(token)) == TK_AND) // term = factor or factor | term
      {
         SQLBooleanClauseTree tree = setOperandType(SQLElement.OP_BOOLEAN_AND);
         (tree.rightTree = auxTree).parent = tree;
         token = term(yylex());
         (tree.leftTree = auxTree).parent = tree;
         auxTree = tree;
      }

      return token;
   }
   
   /**
    * Deals with a factor of an expression tree of a where clause.
    * 
    * @param token The first token of the factor.
    * @return The token after the factor.
    */
   private int factor(int token)
   {
      SQLBooleanClauseTree tree = null; 
      
      if (token == TK_OPEN) // factor = (expression)
      {
         if ((token = expression(yylex())) != TK_CLOSE)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         return yylex();
      }
      
      if (token == TK_NOT)
      {
         if ((token = yylex()) == TK_OPEN) // factor = not (expression)
         {
            if ((token = expression(yylex())) != TK_CLOSE)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            token = yylex();
         }
         else // fator = not factor
            token = factor(token);
         
         // The parent node will be the negation operator and the expression will be the right tree.
         tree = setOperandType(SQLElement.OP_BOOLEAN_NOT);
         (tree.rightTree = auxTree).parent = tree;
         auxTree = tree;
         
         return token;
      }
      
      // factor = single expression (< | > | = | <> | != | <= | >=) single expression
      if ((token = singleExp(token)) == TK_EQUAL || token == TK_LESS || token == TK_DIFF || token == TK_GREATER || token == TK_GREATER_EQUAL 
       || token == TK_LESS_EQUAL)         
      {
         tree = setOperandType(token);
         (tree.leftTree = auxTree).parent = tree;
         token = singleExp(yylex());
         (tree.rightTree = auxTree).parent = tree;
         auxTree = tree;
         return token;
      }
      
      if (token == TK_IS) // factor = single expression is [not] null.
      {
         if ((token = yylex()) == TK_NOT)
         {
            tree = setOperandType(SQLElement.OP_PAT_IS_NOT);
            token = yylex();
         }
         else
            tree = setOperandType(SQLElement.OP_PAT_IS);
         if (token != TK_NULL)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);   
         
         (tree.rightTree = setOperandType(SQLElement.OP_PAT_NULL)).parent = (tree.leftTree = auxTree).parent = tree;
         auxTree = tree;
         
         return yylex();
      }
      
      if (token == TK_NOT) // factor = single expression not like [string | ?]
      {
         token = yylex();
         tree = setOperandType(SQLElement.OP_PAT_MATCH_NOT_LIKE);
      }
      else // factor = single expression like [string | ?]
         tree = setOperandType(SQLElement.OP_PAT_MATCH_LIKE);
      
      SQLBooleanClause whereClause = getInstanceBooleanClause();
      SQLBooleanClauseTree rightTree = new SQLBooleanClauseTree(whereClause);
      
      if (token == TK_LIKE)
      {
         if ((token = yylex()) == TK_STR) // string
            rightTree.setOperandStringLiteral(yylval);
         else if (token == TK_INTERROGATION) // ?
         {
            rightTree.isParameter = true;
            whereClause.paramList[whereClause.paramCount++] = rightTree;
         }
         else
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         
         (tree.rightTree = rightTree).parent = (tree.leftTree = auxTree).parent = tree;
         auxTree = tree;   
         
         return yylex();
      }
      
      yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);  
      return -1;
   }
   
   /**
    * Deals with a single expression of an expression tree of a where clause.
    * 
    * @param token The first token of the single expression.
    * @return The token after the single expression.
    */
   private int singleExp(int token)
   {
      int auxToken;
      SQLBooleanClauseTree tree;
      
      if (token == TK_NUMBER) // single expression = number
      {
         if ((tree = auxTree = new SQLBooleanClauseTree(getInstanceBooleanClause())).operandValue == null)
            tree.operandValue = new SQLValue();
          
         tree.operandValue.asString = yylval; // juliana@226a_20
         return yylex();
      }
      else if (token == TK_STR) // single expression = string
      {
         (auxTree = new SQLBooleanClauseTree(getInstanceBooleanClause())).setOperandStringLiteral(yylval);
         return yylex();
      }
      else if (token == TK_INTERROGATION) // single expression = ?
      {
         SQLBooleanClause whereClause = getInstanceBooleanClause();

         (whereClause.paramList[whereClause.paramCount++] = auxTree = new SQLBooleanClauseTree(whereClause)).isParameter = true;
         return yylex();
      }
      else if ((auxToken = dataFunction(token)) != -1) // single expression = function(...)
      {
         SQLBooleanClause booleanClause = getInstanceBooleanClause();
         int i = 1,
             index = booleanClause.fieldsCount;
         SQLResultSetField field = auxField;

         (auxTree = tree = new SQLBooleanClauseTree(booleanClause)).operandType = SQLElement.OP_IDENTIFIER;
         int hashCode = tree.nameSqlFunctionHashCode = tree.nameHashCode = (tree.operandName = field.tableColName).hashCode();
      
         // generates different indexes to repeted columns on where clause.
         // Ex: where year(birth) = 2000 and day(birth) = 3.
         while (booleanClause.fieldName2Index.exists(tree.nameSqlFunctionHashCode))
            tree.nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
      
         // Puts the hash code of the function name in the hash table.
         booleanClause.fieldName2Index.put(tree.nameSqlFunctionHashCode, index);
      
         SQLResultSetField paramField = field.parameter = new SQLResultSetField(); // Creates the parameter field.
                 
         // Sets the field and function parameter fields.
         paramField.alias = paramField.tableColName = field.alias = field.tableColName = tree.operandName;
         paramField.aliasHashCode = paramField.tableColHashCode = field.tableColHashCode = field.aliasHashCode = tree.nameHashCode;
         field.dataType = SQLElement.dataTypeFunctionsTypes[field.sqlFunction];
         field.isDataTypeFunction = field.isVirtual = true;
         booleanClause.fieldList[booleanClause.fieldsCount++] = field; // Puts the field in the field list.
         
         return auxToken;
      }
      else if (token != TK_NULL) // single expression = pure field.
      {
         token = pureField(token);
         
         SQLBooleanClause booleanClause = getInstanceBooleanClause();                  
         int i = 1, 
             index = booleanClause.fieldsCount;
         SQLResultSetField field = auxField;

         (auxTree = tree = new SQLBooleanClauseTree(booleanClause)).operandType = SQLElement.OP_IDENTIFIER;
         int hashCode = field.tableColHashCode = tree.nameSqlFunctionHashCode = tree.nameHashCode = (tree.operandName = field.tableColName).hashCode();

         // rnovais@570_108: Generates different index to repeted columns on where
         // clause. Ex: where year(birth) = 2000 and birth = '2008/02/11'.
         while (booleanClause.fieldName2Index.exists(tree.nameSqlFunctionHashCode))
            tree.nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;

         // Puts the hash code of the function name in the hash table.
         booleanClause.fieldName2Index.put(tree.nameSqlFunctionHashCode, index);

         field.aliasHashCode = field.alias.hashCode(); // Sets the hash code of the field alias.
         booleanClause.fieldList[booleanClause.fieldsCount++] = field; // Puts the field in the field list.
         
         return token;
      } 
      
      yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);  
      return -1;
   }
   
   /**
    * Deals with a list of values of an insert.
    * 
    * @return The token after the list of values.
    */
   private int listValues()
   {
      int token,
          size = 0;
      String[] values = fieldValues;
      
      do
         switch (token = yylex())
         {
            case TK_NULL: // Null.
               size++;
               break;
            case TK_INTERROGATION: // A variable for prepared statements.
               values[size++] = "?";
               break;
            case TK_NUMBER: // A number.
            case TK_STR: // A string.
               values[size++] = yylval;
               break;
            default: // The list of values is finished or an error occurred.
            { 
               if (size == 0) // There must be a value to be inserted.
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
               return token;
            }
         }
      while ((token = yylex()) == TK_COMMA); 
      fieldValuesSize = size;
      return token;      
   }
   
   /**
    * Deals with a table list of a select.
    * 
    * @return The token after the list of tables.
    */
   private int tableList()
   {
      int token,
          size = 0;
      String tableName,
             tableAlias;
      IntHashtable tables = new IntHashtable(4);
      SQLResultSetTable[] list = tableList;
      
      do
      {
         if ((token = yylex()) != TK_IDENT) // Not a table name, return.
         {
            if ((tableListSize = size) == 0) // There must be at least a table.
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
            return token;
         }
         tableName = tableAlias = yylval; // Table name.
         
         // Table alias.
         if ((token = yylex()) == TK_AS)
            token = yylex();
         if (token == TK_IDENT)
         {
            tableAlias = yylval;
            token = yylex();
         }
      
         // The table name alias must be unique.
         int hash = tableAlias.hashCode();
         if (tables.exists(hash))
            yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_UNIQUE_ALIAS_TABLE) + tableAlias);
         else
            tables.put(hash, tables.size());
         
         list[size++] = new SQLResultSetTable(tableName, tableAlias);
      }
      while (token == TK_COMMA);
      tableListSize = size;
      return token;
   }
   
   /**
    * Deals with a list of expressions of a select.
    * 
    * @param token A token to be used by the list of expressions. 
    * @return The token after the list of expressions.
    */
   private int fieldExp(int token)
   {
      SQLSelectClause selectAux = select;
      SQLResultSetField[] resultFieldList = selectAux.fieldList;
      
      if (token == TK_ASTERISK) // All fields.
      {
         // Adds a wildcard field.
         (resultFieldList[selectAux.fieldsCount++] = new SQLResultSetField()).isWildcard = true;
         token = yylex();
      }
      else
      {
         String alias = null;

         do
         {
            if (token == TK_COMMA) // Gets the next field list token.
               token = yylex();
            
            if ((token = field(token)) == TK_AS) // There is an alias.
            {
               if (yylex() != TK_IDENT)
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
               alias = yylval;
               token = yylex();
            }
            else
            {
               // If the alias_name is null, the alias must be the name of the column. This was already done before.
               // If the alias is null and the field is a virtual column, raises an exception, since virtual columns require explicit aliases.
               if (auxField.isVirtual)
                  yyerror(LitebaseMessage.ERR_REQUIRED_ALIAS);
                                            
               alias = auxField.alias; // The null alias name is filled as tableColName or tableName.tableColName, which was set before.
            }
            
            // Checks if the alias has not already been used by a predecessor.
            int i = selectAux.fieldsCount - 1;
            
            while (--i >= 0)
               if (resultFieldList[i].alias.equals(alias))
                  yyerrorWithMessage(LitebaseMessage.getMessage(LitebaseMessage.ERR_DUPLICATE_ALIAS) + alias);

            auxField.aliasHashCode = (auxField.alias = alias).hashCode(); // Assigns the alias.
         }
         while (token == TK_COMMA);
      }
      return token;
   }
   
   /**
    * Deals with a list of update expressions.
    * 
    * @return The token after the list of update expressions.
    */
   private int updateExpCommalist()
   {
      int token,
          size = 0;
      String[] values = fieldValues;
      String[] names = fieldNames;
      SQLResultSetField field;
      
      do
      {
         if (pureField(yylex()) != TK_EQUAL) // field being updated.
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
         field = auxField;
         
         // New value.
         if ((token = yylex()) == TK_STR || token == TK_NUMBER) // A string or a number.
            values[size++] = yylval;
         else if (token == TK_NULL) // null
            size++;
         else if (token == TK_INTERROGATION) // A prepared statement parameter.
            values[size++] = "?"; 
         else
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         
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
         names[size - 1] = field.tableColName;
      }
      while ((token = yylex()) == TK_COMMA);
      if ((fieldNamesSize = fieldValuesSize = size) == 0)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      return token;
   }
   
   /**
    * Deals with a field.
    * 
    * @param token A token to be used by the field.
    * @return The token after the field.
    */
   private int field(int token)
   {
      int tokenAux;
      SQLSelectClause selectAux = select;
      SQLResultSetField field = null;
      
      if (token == TK_IDENT) // A pure field.
      {
         token = pureField(token);                                      
         selectAux.fieldList[selectAux.fieldsCount++] = field = auxField;
         field.tableColHashCode = field.tableColName.hashCode();
         selectAux.hasRealColumns = true;
         tokenAux = token;
      }
      else 
      {
         if ((tokenAux = dataFunction(token)) != -1) // A function applied to a field.
         {
            // Sets the field.
            field = auxField;
            field.isDataTypeFunction = field.isVirtual = true;
            field.dataType = SQLElement.dataTypeFunctionsTypes[field.sqlFunction];
   
            // Sets the function parameter.
            SQLResultSetField paramField = field.parameter = new SQLResultSetField();
            paramField.alias = paramField.tableColName = field.tableColName;
            paramField.tableColHashCode = paramField.tableColName.hashCode();
            field.tableColHashCode = paramField.aliasHashCode = paramField.tableColHashCode; 
         } 
         else if ((tokenAux = aggFunction(token)) != -1) // An aggregation function applied to a field.
         {
            // Sets the field.
            field = auxField;
            field.isAggregatedFunction = field.isVirtual = true;
            field.dataType = SQLElement.aggregateFunctionsTypes[field.sqlFunction];
   
            // Sets the parameter, if there is such one.
            if (field.sqlFunction != SQLElement.FUNCTION_AGG_COUNT)
            {
               // Sets the function parameter.
               SQLResultSetField paramField = field.parameter = new SQLResultSetField();
               paramField.alias = paramField.tableColName = field.tableColName;
               paramField.tableColHashCode = paramField.tableColName.hashCode();
               field.tableColHashCode = paramField.aliasHashCode = paramField.tableColHashCode;
            }
   
            selectAux.hasAggFunctions = true;
         }
         else
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
         
         selectAux.fieldList[select.fieldsCount++] = field; // Sets the select statement.
      }
   
      return tokenAux;
   }
   
   /**
    * Deals with a pure field.
    * 
    * @param token A token to be used by the pure field.
    * @return The token after the pure field.
    */
   private int pureField(int token)
   {
      SQLResultSetField field = auxField = new SQLResultSetField();
      
      if ((token = yylex()) == TK_DOT) // table.fieldName
      {
         field.tableName = field.alias = yylval;
         if (yylex() != TK_IDENT)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
         field.alias += '.' + (field.tableColName = yylval);
         token = yylex();
      }
      else // A simple field.
         field.tableColName = field.alias = yylval;
      
      return token;
   }
   
   /**
    * Deals with a data function.
    * 
    * @param token A token witch is possibly a data function token.
    * @return The next token or -1 if it is not a data function. 
    */
   private int dataFunction(int token)
   {
      int function;
      
      switch (token)
      {
         case TK_ABS: // Abs function.
            function = SQLElement.FUNCTION_DT_ABS;
            break;
         case TK_DAY: // Day function.
            function = SQLElement.FUNCTION_DT_DAY;
            break;
         case TK_HOUR: // Hour function.
            function = SQLElement.FUNCTION_DT_HOUR;
            break;
         case TK_LOWER: // Lower function.
            function = SQLElement.FUNCTION_DT_LOWER;
            break;
         case TK_MILLIS: // Millis function.
            function = SQLElement.FUNCTION_DT_MILLIS;
            break;
         case TK_MINUTE: // Minute function.
            function = SQLElement.FUNCTION_DT_MINUTE;
            break;
         case TK_MONTH: // Month function.
            function = SQLElement.FUNCTION_DT_MONTH;
            break;
         case TK_SECOND: // Second function.
            function = SQLElement.FUNCTION_DT_SECOND;
            break;
         case TK_UPPER: // Upper function.
            function = SQLElement.FUNCTION_DT_UPPER;
            break;
         case TK_YEAR: // Year function.
            function = SQLElement.FUNCTION_DT_YEAR;
            break;
         default:
            return -1;
      }
      if (yylex() != TK_OPEN || pureField(yylex()) != TK_CLOSE)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR); 
      auxField.sqlFunction = function;
      return yylex();
   }
   
   /**
    * Deals with a aggregation function.
    * 
    * @param token A token witch is possibly a data function token.
    * @return The next token or -1 if it is not a data function. 
    */
   private int aggFunction(int token)
   {
      int function;
      
      switch (token)
      {
         case TK_AVG:
            function = SQLElement.FUNCTION_AGG_AVG;
            break;
         case TK_COUNT:
            function = SQLElement.FUNCTION_AGG_COUNT;
            break;
         case TK_MAX:
            function = SQLElement.FUNCTION_AGG_MAX;
            break;
         case TK_MIN:
            function = SQLElement.FUNCTION_AGG_MIN;
            break;
         case TK_SUM:
            function = SQLElement.FUNCTION_AGG_SUM;
            break;
         default:
            return -1;
      }
      if (token == TK_COUNT)
      {
         if (yylex() != TK_OPEN || yylex() != TK_ASTERISK || yylex() != TK_CLOSE)
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         auxField = new SQLResultSetField();
      }
      else if (yylex() != TK_OPEN || pureField(yylex()) != TK_CLOSE)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      auxField.sqlFunction = function; 
      return yylex();
   }
   
   /**
    * Deals with a possible where clause.
    * 
    * @param token The token where if it is a where clause.
    * @return The token received if it is not a where clause or the token after the where clause.
    */
   private int optWhereClause(int token)
   {
      if (token == TK_WHERE) // Where clause.
      {
         token = expression(yylex()); 
         
         // Compacts the field list of the where clause.
         SQLBooleanClause clause = whereClause;
         SQLResultSetField[] compactFieldList = new SQLResultSetField[clause.fieldsCount];
         Vm.arrayCopy(clause.fieldList, 0, compactFieldList, 0, clause.fieldsCount);
         clause.fieldList = compactFieldList;
         clause.expressionTree = auxTree;      
      }
      return token;
   }

   /**
    * Deals with an order by clause.
    * 
    * @return The first token after the order by clause.
    */
   private int orderByClause()
   {
      int token;
      boolean direction;
      SQLSelectClause selectAux = select;
      
      do
      {
         direction = true;
         
         // Ascending or descending order.
         if ((token = field(yylex())) == TK_ASC)
            token = yylex();
         else if (token == TK_DESC)
         {
            direction = false;
            token = yylex();
         }
         
         selectAux.fieldsCount--;
         addColumnFieldOrderGroupBy(auxField, direction, true);
      }
      while (token == TK_COMMA);
      
      // Compacts the order by field list.
      SQLColumnListClause orderByAux = orderBy;
      SQLResultSetField[] compactFieldList = new SQLResultSetField[orderByAux.fieldsCount];
      Vm.arrayCopy(orderByAux.fieldList, 0, compactFieldList, 0, orderByAux.fieldsCount);
      orderByAux.fieldList = compactFieldList;
      
      return token;
   }
   
   /**
    * Deals with a group by clause.
    * 
    * @return The first token after the group by clause.
    */
   private int groupByClause()
   {
      int token;
      SQLSelectClause selectAux = select;
      
      do
      {  
         token = field(yylex());
         selectAux.fieldsCount--;
         addColumnFieldOrderGroupBy(auxField, true, false);
      }
      while (token == TK_COMMA);
      
      // Compacts the group by field list.
      SQLColumnListClause groupByAux = groupBy;
      SQLResultSetField[] compactFieldList = new SQLResultSetField[groupByAux.fieldsCount];
      Vm.arrayCopy(groupByAux.fieldList, 0, compactFieldList, 0, groupByAux.fieldsCount);
      groupByAux.fieldList = compactFieldList;

      if (token == TK_HAVING) // Adds the expression tree of the where clause.
      {
         isWhereClause = false;  // Indicates if the clause is a where or a having clause.
         token = expression(yylex());
         havingClause.expressionTree = auxTree;
         
         // Compacts the having clause field list.
         SQLBooleanClause clause = havingClause;
         compactFieldList = new SQLResultSetField[clause.fieldsCount];
         Vm.arrayCopy(clause.fieldList, 0, compactFieldList, 0, clause.fieldsCount);
         clause.fieldList = compactFieldList;
      }
      
      return token;
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
    * @param isOrder by <code>true</code> if the field comes from an order be clause. <code>false</code>, otherwise.
    */
   private void addColumnFieldOrderGroupBy(SQLResultSetField field, boolean isAscending, boolean isOrderBy)
   {
      SQLColumnListClause listClause = isOrderBy? getInstanceColumnListClauseOrderBy() 
                                                : getInstanceColumnListClauseGroupBy();
      
      field.tableColHashCode = field.aliasHashCode = field.tableColName.hashCode();
      field.isAscending = isAscending;
      listClause.fieldList[listClause.fieldsCount++] = field;
   }
   
   /**
    * Error message with code.
    * 
    * @param error The error code.
    * @throws SQLParseException
    */
   void yyerror(int error) throws SQLParseException
   {
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
              + LitebaseMessage.getMessage(error) + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
   } 
   
   /**
    * Error message with a error message.
    * 
    * @param message The error message.
    * @throws SQLParseException
    */
   private void yyerrorWithMessage(String message)
   {
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) + message + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
   }
}
