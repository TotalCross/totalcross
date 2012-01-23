/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package litebase;

import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.util.*;

/**
 * This class calls <code>yyparse()</code> and builds the parser result.
 */
class LitebaseParser
{
   // Type tokens.
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
   
   // Other tokens.
   /**
    * <code>NOCASE</code> keyword token.
    */
   final static int TK_NOCASE = 7;
   
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
    * <code>MIN</code> keyword token.
    */
   final static int TK_MIN = 40;
   
   /**
    * <code>MINUTE</code> keyword token.
    */
   final static int TK_MINUTE = 41;
   
   /**
    * <code>MONTH</code> keyword token.
    */
   final static int TK_MONTH = 42;

   /**
    * <code>NOT</code> keyword token.
    */
   final static int TK_NOT = 43;
   
   /**
    * <code>NULL</code> keyword token.
    */
   final static int TK_NULL = 44;
   
   /**
    * <code>ON</code> keyword token.
    */
   final static int TK_ON = 45;
   
   /**
    * <code>OR</code> keyword token.
    */
   final static int TK_OR = 46;
   
   /**
    * <code>ORDER</code> keyword token.
    */
   final static int TK_ORDER = 47;
   
   /**
    * <code>PRIMARY</code> keyword token.
    */
   final static int TK_PRIMARY = 48;
   
   /**
    * <code>RENAME</code> keyword token.
    */
   final static int TK_RENAME = 49;
   
   /**
    * <code>SECOND</code> keyword token.
    */
   final static int TK_SECOND = 50;
   
   /**
    * <code>SELECY</code> keyword token.
    */
   final static int TK_SELECT = 51;
   
   /**
    * <code>SET</code> keyword token.
    */
   final static int TK_SET = 52;

   /**
    * <code>SUM</code> keyword token.
    */
   final static int TK_SUM = 53;
   
   /**
    * <code>TABLE</code> keyword token.
    */
   final static int TK_TABLE = 54;
   
   /**
    * <code>TO</code> keyword token.
    */
   final static int TK_TO = 55;
   
   /**
    * <code>UPDATE</code> keyword token.
    */
   final static int TK_UPDATE = 56;
   
   /**
    * <code>UPPER</code> keyword token.
    */
   final static int TK_UPPER = 57;
   
   /**
    * <code>VALUES</code> keyword token.
    */
   final static int TK_VALUES = 58;
   
   /**
    * <code>WHERE</code> keyword token.
    */
   final static int TK_WHERE = 59;
   
   /**
    * <code>YEAR</code> keyword token.
    */
   final static int TK_YEAR = 60;
   
   /**
    * Identifier token.
    */
   final static int TK_IDENT = 61;

   /**
    * Numerical token.
    */
   final static int TK_NUMBER = 62;

   /**
    * String token.
    */
   final static int TK_STR = 63;

   /**
    * <code>>=</code> token.
    */
   final static int TK_GREATER_EQUAL = 64;

   /**
    * <code><=</code> token.
    */
   final static int TK_LESS_EQUAL = 65;

   /**
    * <code>!=</code> or <code><></code> token.
    */
   final static int TK_DIFF = 66;
   
   /**
    * Error code.
    */
   final static int YYERRCODE = -1;

   /**
    * The 'lval' (result) got from <code>yylex()</code>.
    */
   String yylval;

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
    * A hash table to be used on select statements to verify if it has repeated table names.
    */
   IntHashtable tables;

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
    * @param lexer The lexical analizer.
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

      // The maximum number of columns in a list clause can't be reached.
      if (listClause.fieldsCount == SQLElement.MAX_NUM_COLUMNS)
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY));

      field.tableColHashCode = field.aliasHashCode = field.tableColName.hashCode();
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

   /**
    * Parses input and execute indicated items.
    * @throws InvalidNumberException If an internal method throws it.
    */
   private void yyparse() throws InvalidNumberException
   {     
      int token;
      String tableName;
      
      switch (yylex())
      {
         case TK_ALTER: // Alter table.
            if (yylex() != TK_TABLE || yylex() != TK_IDENT)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
            
            switch (yylex())
            {
               case TK_ADD: // Adds a primary key.
                  if (yylex() != TK_PRIMARY || yylex() != TK_KEY || yylex() != '(' || colnameCommalist() != ')')
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);                  
                  command = SQLElement.CMD_ALTER_ADD_PK;
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
            break;
                        
         case TK_CREATE:
         {
            switch (yylex())
            {
               case TK_TABLE: // Create table.
                  if (yylex() != TK_IDENT || yylex() != '(')
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
                  
                  // Primary key.
                  if ((token = createColumnCommalist()) == TK_PRIMARY && yylex() == TK_KEY && colnameCommalist() == ')')
                  {
                     if (number_pk == 1)
                        yyerror(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED);   
                  }
                  else if (token != ')')
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);                  

                  command = SQLElement.CMD_CREATE_TABLE;
                  break;
                  
               case TK_INDEX: // Create index.
                  if (yylex() != TK_IDENT || yylex() != TK_ON || yylex() != TK_IDENT)
                     yyerror(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED);
                  tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.
                  if (yylex() != '(' && colnameCommalist() != ')')
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  command = SQLElement.CMD_CREATE_INDEX;
                  break;
                  
               default:
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            }     
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
               tableList[0] = new SQLResultSetTable(tableName);
            
            // Where clause.
            if (token == TK_WHERE) 
               token = aExp();   
            if (token != LitebaseLex.YYEOF)
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            
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
                  token = colnameCommalist(); // Indices to be dropped.             
                  if (token != TK_ON || yylex() != TK_IDENT) // Table whose index(es) will be dropped.
                     yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
                  tableList[0] = new SQLResultSetTable(yylval); // There's no alias table name here.                     
                  command = SQLElement.CMD_DROP_INDEX;
                  break;
               
               default:
                  yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
            }  
            break;
            
         case TK_INSERT: // Insert.
            break;
            
         case TK_SELECT: // Select.
            break;
            
         case TK_UPDATE: // Update.
            break;
         
         default:
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);         
      }
      
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
      while ((token = createColumn()) == ',');
      return token;      
   }
   
   /**
    * Deals with a column declaration.
    * 
    * @return The token after a row declaration. 
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
      
      // The next token after ',' is a primary key declaration. This is not treated here.
      if ((token = yylex()) == TK_PRIMARY)
         return token;
      
      // Column name.
      if (token != TK_IDENT)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      columnName = yylval;
      
      // Column type.
      if ((type = yylex()) == 7 || type > SQLElement.BLOB)
         yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      if (type == TK_VARCHAR)
         type = SQLElement.CHARS;
      
      if (type == SQLElement.CHARS || type == SQLElement.BLOB) // Size and multiplier. 
      {
         if (yylex() == '(' && yylex() == TK_NUMBER)
         {
            if ((size = Convert.toInt(yylval)) <= 0)
               yyerror(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT);
         }
         else
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         
         if (type == SQLElement.CHARS && yylex() != ')')
            yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         else if (type == SQLElement.BLOB) 
         {   
            if ((token = yylex()) != TK_IDENT && token != ')')
               yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);   
            if (token == TK_IDENT)
            {
               if (yylval.equals("k")) // kilobytes.
                  size <<= 10;
               else if (yylval.equals("m")) // megabytes.
                  size <<= 20;
               else
                  yyerror(LitebaseMessage.ERR_INVALID_MULTIPLIER);
               if (yylex() != ')')
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
         if (type == SQLElement.BLOB)
            yyerror(LitebaseMessage.ERR_BLOB_STRING);
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

   private int aExp()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   private int colnameCommalist()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   

   private void yyerror(int error)
   {
      throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
              + LitebaseMessage.getMessage(error) + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
   }
   
}
