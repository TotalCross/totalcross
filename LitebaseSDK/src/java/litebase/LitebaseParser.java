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

import totalcross.util.*;
import totalcross.sys.*;

/**
 * This class calls <code>yyparse()</code> and builds the parser result.
 */
class LitebaseParser
{
   // Tokens
   /**
    * <code>ABS</code> keyword token.
    */
   final static int TK_ABS = 0;
   
   /**
    * <code>ADD</code> keyword token.
    */
   final static int TK_ADD = 1;
   
   /**
    * <code>ALTER</code> keyword token.
    */
   final static int TK_ALTER = 2;
   
   /**
    * <code>AND</code> keyword token.
    */
   final static int TK_AND = 3;
   
   /**
    * <code>AS</code> keyword token.
    */
   final static int TK_AS = 4;
   
   /**
    * <code>ASC</code> keyword token.
    */
   final static int TK_ASC = 5;
   
   /**
    * <code>AVG</code> keyword token.
    */
   final static int TK_AVG = 6;
   
   /**
    * <code>BLOB</code> keyword token.
    */
   final static int TK_BLOB = 7;
   
   /**
    * <code>BY</code> keyword token.
    */
   final static int TK_BY = 8;
   
   /**
    * <code>CHAR</code> keyword token.
    */
   final static int TK_CHAR = 9;
   
   /**
    * <code>COUNT</code> keyword token.
    */
   final static int TK_COUNT = 10;

   /**
    * <code>CREATE</code> keyword token.
    */
   final static int TK_CREATE = 11;
   
   /**
    * <code>DATE</code> keyword token.
    */
   final static int TK_DATE = 12;
   
   /**
    * <code>DATETIME</code> keyword token.
    */
   final static int TK_DATETIME = 13;
   
   /**
    * <code>DAY</code> keyword token.
    */
   final static int TK_DAY = 14;
   
   /**
    * <code>DEFAULT</code> keyword token.
    */
   final static int TK_DEFAULT = 15;
   
   /**
    * <code>DELETE</code> keyword token.
    */
   final static int TK_DELETE = 16;
   
   /**
    * <code>DESC</code> keyword token.
    */
   final static int TK_DESC = 17;
   
   /**
    * <code>DISTINCT</code> keyword token.
    */
   final static int TK_DISTINCT = 18;
   
   /**
    * <code>DOUBLE</code> keyword token.
    */
   final static int TK_DOUBLE = 19;
   
   /**
    * <code>DROP</code> keyword token.
    */
   final static int TK_DROP = 20;
   
   /**
    * <code>FLOAT</code> keyword token.
    */
   final static int TK_FLOAT = 21;
   
   /**
    * <code>FROM</code> keyword token.
    */
   final static int TK_FROM = 22;
   
   /**
    * <code>GROUP</code> keyword token.
    */
   final static int TK_GROUP = 23;
   
   /**
    * <code>HAVING</code> keyword token.
    */
   final static int TK_HAVING = 24;
   
   /**
    * <code>HOUR</code> keyword token.
    */
   final static int TK_HOUR = 25;
   
   /**
    * <code>INDEX</code> keyword token.
    */
   final static int TK_INDEX = 26;
   
   /**
    * <code>INSERT</code> keyword token.
    */
   final static int TK_INSERT = 27;
   
   /**
    * <code>INT</code> keyword token.
    */
   final static int TK_INT = 28;
   
   /**
    * <code>INTO</code> keyword token.
    */
   final static int TK_INTO = 29;
   
   /**
    * <code>IS</code> keyword token.
    */
   final static int TK_IS = 30;
   
   /**
    * <code>KEY</code> keyword token.
    */
   final static int TK_KEY = 31;
   
   /**
    * <code>LIKE</code> keyword token.
    */
   final static int TK_LIKE = 32;
   
   /**
    * <code>LONG</code> keyword token.
    */
   final static int TK_LONG = 33;
   
   /**
    * <code>LOWER</code> keyword token.
    */
   final static int TK_LOWER = 34;
   
   /**
    * <code>MAX</code> keyword token.
    */
   final static int TK_MAX = 35;
   
   /**
    * <code>MILLIS</code> keyword token.
    */
   final static int TK_MILLIS = 36;
   
   /**
    * <code>MIN</code> keyword token.
    */
   final static int TK_MIN = 37;
   
   /**
    * <code>MINUTE</code> keyword token.
    */
   final static int TK_MINUTE = 38;
   
   /**
    * <code>MONTH</code> keyword token.
    */
   final static int TK_MONTH = 39;
   
   /**
    * <code>NOCASE</code> keyword token.
    */
   final static int TK_NOCASE = 40;
   
   /**
    * <code>NOT</code> keyword token.
    */
   final static int TK_NOT = 41;
   
   /**
    * <code>NULL</code> keyword token.
    */
   final static int TK_NULL = 42;
   
   /**
    * <code>ON</code> keyword token.
    */
   final static int TK_ON = 43;
   
   /**
    * <code>OR</code> keyword token.
    */
   final static int TK_OR = 44;
   
   /**
    * <code>ORDER</code> keyword token.
    */
   final static int TK_ORDER = 45;
   
   /**
    * <code>PRIMARY</code> keyword token.
    */
   final static int TK_PRIMARY = 46;
   
   /**
    * <code>RENAME</code> keyword token.
    */
   final static int TK_RENAME = 47;
   
   /**
    * <code>SECOND</code> keyword token.
    */
   final static int TK_SECOND = 48;
   
   /**
    * <code>SELECY</code> keyword token.
    */
   final static int TK_SELECT = 49;
   
   /**
    * <code>SET</code> keyword token.
    */
   final static int TK_SET = 50;
   
   /**
    * <code>SHORT</code> keyword token.
    */
   final static int TK_SHORT = 51;
   
   /**
    * <code>SUM</code> keyword token.
    */
   final static int TK_SUM = 52;
   
   /**
    * <code>TABLE</code> keyword token.
    */
   final static int TK_TABLE = 53;
   
   /**
    * <code>TO</code> keyword token.
    */
   final static int TK_TO = 54;
   
   /**
    * <code>UPDATE</code> keyword token.
    */
   final static int TK_UPDATE = 55;
   
   /**
    * <code>UPPER</code> keyword token.
    */
   final static int TK_UPPER = 56;
   
   /**
    * <code>VALUES</code> keyword token.
    */
   final static int TK_VALUES = 57;
   
   /**
    * <code>VARCHAR</code> keyword token.
    */
   final static int TK_VARCHAR = 58;
   
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
    * <code>></code> token.
    */
   final static int TK_GREATER = 65;

   /**
    * <code><=</code> token.
    */
   final static int TK_LESS_EQUAL = 66;
   
   /**
    * <code><</code> token.
    */
   final static int TK_LESS = 67;

   /**
    * <code>==</code> token.
    */
   final static int TK_EQUAL = 68;

   /**
    * <code>!=</code> or <code><></code> token.
    */
   final static int TK_DIFF = 69;
   
   /**
    * <code>,</code> token.
    */
   final static int TK_COMMA = 70;
   
   /**
    * <code>.</code> token.
    */
   final static int TK_DOT = 71;
   
   /**
    * <code>?</code> token.
    */
   final static int TK_INTERROGATION = 72;

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
      return lexer.yylex();
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

   // ###############################################################
   // method: yyparse : parse input and execute indicated items
   // ###############################################################
   private int yyparse()
   {
      boolean isPrimaryKey = false, // Indicates if a table field is the primary key.
              isNotNull = false, // Indicates if a field can have a <code>null</code> value.
              isNocase = false; // <code>true</code> indicates caseless comparison. <code>false</code>, otherwise.
      String tableNameAux,
             strDefault = null, // Stores a default value for a field.
             aliasTableName = null, // An alias for the table name.
             firstFieldUpdateTableName = null, // The first table name found in an update statement.
             firstFieldUpdateAlias = null, // The first table alias found in an update statement. 
             secondFieldUpdateTableName = null, // The second table name found in an update statement, which indicates an error.
             secondFieldUpdateAlias = null; // The second table alias found in an update statement, which indicates an error. 
      int size,
          hash,
          index;
      SQLResultSetField field;
      SQLResultSetField[] resultFieldList;
      SQLBooleanClause clause;
      SQLColumnListClause group_order_by;
      SQLBooleanClauseTree tree;
      
      SQLSelectClause selectClause = select;      
      SQLBooleanClause booleanClauseAux;
      
      return 0;
   }      
}
