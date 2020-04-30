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

// juliana@253_9: improved Litebase parser.

/**
 * Contains error messages with multi-language support. By now, only English and Portuguese are implemented.
 */
class LitebaseMessage
{
   // General errors.
   /**
    * "Error: "
    */
   static final int ERR_MESSAGE_START = 0;

   /**
    * " Near position "
    */
   static final int ERR_MESSAGE_POSITION = 1;

   /**
    * "Syntax error."
    */
   static final int ERR_SYNTAX_ERROR = 2;

   // Limit errors.
   /**
    * "Table name too big: must be <= 23."
    */
   static final int ERR_MAX_TABLE_NAME_LENGTH = 3;

   /**
    * "The maximum number of fields in a SELECT clause was exceeded."
    */
   static final int ERR_FIELDS_OVERFLOW = 4;
   
   /**
    * "The maximum number of columns was exceeded."
    */
   static final int ERR_COLUMNS_OVERFLOW = 5;

   // Column errors.
   /**
    * "Unknown column "
    */
   static final int ERR_UNKNOWN_COLUMN = 6;

   /**
    * "Invalid column name: "
    */
   static final int ERR_INVALID_COLUMN_NAME = 7;

   /**
    * "Invalid column number: "
    */
   static final int ERR_INVALID_COLUMN_NUMBER = 8;

   /**
    * "The following column(s) does (do) not have an associated index "
    */
   static final int ERR_COLUMN_DOESNOT_HAVE_AN_INDEX = 9;

   /**
    * "Column name in field list is ambiguous: "
    */
   static final int ERR_AMBIGUOUS_COLUMN_NAME = 10;

   /**
    * "Column not found: "
    */
   static final int ERR_COLUMN_NOT_FOUND = 11;

   /**
    * "Duplicated column name: "
    */
   static final int ERR_DUPLICATED_COLUMN_NAME = 12;

   // Primary key errors.
   /**
    * "A primary key was already defined for this table."
    */
   static final int ERR_PRIMARY_KEY_ALREADY_DEFINED = 13;

   /**
    * "Table does not have a primary key."
    */
   static final int ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY = 14;

   /**
    * "Statement creates a duplicated primary key in "
    */
   static final int ERR_STATEMENT_CREATE_DUPLICATED_PK = 15;

   // Type errors.
   /**
    * "Incompatible types."
    */
   static final int ERR_INCOMPATIBLE_TYPES = 16;

   /**
    * "Field size must be a positive interger value."
    */
   static final int ERR_FIELD_SIZE_IS_NOT_INT = 17;

   /**
    * "The maximum size of CHAR or VARCHAR is 65535."
    */
   static final int ERR_CHAR_TOO_BIG = 18;
   
   // Number of fields errors.
   /**
    * "The number of fields does not match the number of values "
    */
   static final int ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH = 19;

   /**
    * "The given number of values does not match the table definition."
    */
   static final int ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION = 20;

   // Default value errors.
   /**
    * "Length of default value is bigger than column size."
    */
   static final int ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER = 21;

   /**
    * "An added column declared as NOT NULL must have a not null default value."
    */
   static final int ERR_NOT_NULL_DEFAULT = 22; // juliana@253_22: added command ALTER TABLE ADD column.
   
   // Driver errors.
   /**
    * "This driver instance was closed and can't be used anymore. Please get a new instance of it."
    */
   static final int ERR_DRIVER_CLOSED = 23;

   /**
    * "ResultSet already closed!"
    */
   static final int ERR_RESULTSET_CLOSED = 24;
   
   /**
    * "RowIterator already closed!"
    */
   static final int ERR_ROWITERATOR_CLOSED = 25;
   
   /**
    * "ResultSetMetaData can't be used after the ResultSet is closed."
    */
   static final int ERR_RESULTSETMETADATA_CLOSED = 26;

   /**
    * "Cant't find native methods implementation for LitebaseConnection. Please install Litebase.dll/prc file."
    */
   static final int ERR_LITEBASEPRCDLL_NOT_FOUND = 27;

   /**
    * "The application id must be four characters long."
    */
   static final int ERR_INVALID_CRID = 28;
   
   /**
    * "The increment must be greater than 0 or -1."
    */
   static final int ERR_INVALID_INC = 29;
   
   // juliana@253_11: now a DriverException will be throw if an incorrect parameter is passed in LitebaseConnection.getInstance().
   /**
    * "Invalid connection parameter: "
    */
   static final int ERR_INVALID_PARAMETER = 30;
   
   // juliana@253_20: added PreparedStatement.close().
   /**
    * "The prepared statement is already closed."
    */
   static final int ERR_PREPARED_CLOSED = 31;

   // Table errors.
   /**
    * "Table name not found: "
    */
   static final int ERR_TABLE_NAME_NOT_FOUND = 32;

   /**
    * "Table already created: "
    */
   static final int ERR_TABLE_ALREADY_CREATED = 33;
   
   /**
    * "It is not possible to open a table within a connection with a different string format."
    */
   static final int ERR_WRONG_STRING_FORMAT = 34; // juliana@210_2: now Litebase supports tables with ascii strings.

   /**
    * "It is not possible to open a table within a connection with a different cryptography format."
    */
   static final int ERR_WRONG_CRYPTO_FORMAT = 35; // juliana@crypto_1: now Litebase supports weak cryptography.

   // ROWID error.
   /**
    * "ROWID can't be changed by the user!"
    */
   static final int ERR_ROWID_CANNOT_BE_CHANGED = 36;
   
   // Prepared Statement errors.
   /**
    * "SQL statement does not return result set."
    */
   static final int ERR_QUERY_DOESNOT_RETURN_RESULTSET = 37;

   /**
    * "SQL statement does not perform updates in the database."
    */
   static final int ERR_QUERY_DOESNOT_PERFORM_UPDATE = 38;

   /**
    * "Not all parameters of the query had their values defined."
    */
   static final int ERR_NOT_ALL_PARAMETERS_DEFINED = 39;

   /**
    * "A value was not defined for the parameter "
    */
   static final int ERR_PARAMETER_NOT_DEFINED = 40;

   /**
    * "Invalid parameter index."
    */
   static final int ERR_INVALID_PARAMETER_INDEX = 41;

   // Rename errors.
   /**
    * "Can't rename table. This table already exists: "
    */
   static final int ERR_TABLE_ALREADY_EXIST = 42;

   /**
    * "Column already exists: "
    */
   static final int ERR_COLUMN_ALREADY_EXIST = 43;

   // Alias errors.
   /**
    * "Not unique table/alias: "
    */
   static final int ERR_NOT_UNIQUE_ALIAS_TABLE = 44;

   /**
    * "This alias is already being used in this expression: "
    */
   static final int ERR_DUPLICATE_ALIAS = 45;

   /**
    * "An alias is required for the aggregate function column."
    */
   static final int ERR_REQUIRED_ALIAS = 46;

   // Litebase.execute() error.
   /**
    * "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute()."
    */
   static final int ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED = 47;

   // Order by and group by errors.
   /**
    * "ORDER BY and GROUP BY clauses must match."
    */
   static final int ERR_ORDER_GROUPBY_MUST_MATCH = 48;

   /**
    * "No support for virtual columns in SQL queries with GROUP BY clause."
    */
   static final int ERR_VIRTUAL_COLUMN_ON_GROUPBY = 49;

   // Function errors.
   /**
    * "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause."
    */
   static final int ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT = 50;

   /**
    * " is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as
    * aliased aggregation functions."
    */
   static final int ERR_IS_NOT_AGGREG_FUNCTION = 51;

   /**
    * "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause."
    */
   static final int ERR_CANNOT_MIX_AGGREG_FUNCTION = 52;

   /**
    * "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause."
    */
   static final int ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY = 53;

   /**
    * " was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation 
    * funtions."
    */
   static final int ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION = 54;

   /**
    * "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields."
    */
   static final int ERR_SUM_AVG_WITH_DATE_DATETIME = 55;

   // DATETIME error.
   /**
    * "Value is not a DATETIME: "
    */
   static final int ERR_VALUE_ISNOT_DATETIME = 56;

   // Index errors.
   /**
    * "Index already created for column "
    */
   static final int ERR_INDEX_ALREADY_CREATED = 57;

   /**
    * "Can't drop a primary key index with drop index."
    */
   static final int ERR_DROP_PRIMARY_KEY = 58;
   
   /**
    * "Index too large. It can't have more than 65534 nodes."
    */
   static final int ERR_INDEX_LARGE = 59;

   // NOT NULL errors.
   /**
    * "Primary key can't have null."
    */
   static final int ERR_PK_CANT_BE_NULL = 60;

   /**
    * "Field can't be null: "
    */
   static final int ERR_FIELD_CANT_BE_NULL = 61;
   
   /**
    * "A parameter in a where clause can't be null."
    */
   static final int ERR_PARAM_NULL = 62;

   // Result set errors.
   /**
    * "ResultSet in invalid record position."
    */
   static final int ERR_RS_INV_POS = 63;

   /**
    * "Invalid value for decimal places: "
    */
   static final int ERR_RS_DEC_PLACES_START = 64;

   /**
    * ". Must be in the range -1 to 40."
    */
   static final int ERR_RS_DEC_PLACES_END = 65;

   // File errors.
   /**
    * "Can't read from table."
    */
   static final int ERR_CANT_READ = 66;

   /**
    * "Can't load node: index corrupted."
    */
   static final int ERR_CANT_LOAD_NODE = 67;

   /**
    * "Table is corrupted: "
    */
   static final int ERR_TABLE_CORRUPTED = 68;
   
   /**
    * "Table not closed properly: "
    */
   static final int ERR_TABLE_NOT_CLOSED = 69; // juliana@220_2

   /**
    * "A properly closed table can't be used in recoverTable(): "
    */
   static final int ERR_TABLE_CLOSED = 70; // juliana@222_2
   
   /**
    * "Can't find index record position on delete."
    */
   static final int ERR_IDX_RECORD_DEL = 71;
   
   /**
    * "The table format is incompatible with Litebase version. Please update your tables."
    */
   static final int ERR_WRONG_VERSION = 72;
   
   /**
    * "The table format is not the previous one: "
    */
   static final int ERR_WRONG_PREV_VERSION = 73; // juliana@220_11

   /**
    * "Invalid path: " 
    */
   static final int ERR_INVALID_PATH = 74; // juliana@214_1
   
   /**
    * "Database not found."
    */
   static final int ERR_DB_NOT_FOUND = 75; // juliana@226_10
   
   // BLOB errors.
   /**
    * "The total size of a blob can't be greater then 10 Mb."
    */
   static final int ERR_BLOB_TOO_BIG = 76;

   /**
    * "This is not a valid size multiplier."
    */
   static final int ERR_INVALID_MULTIPLIER = 77;

   /**
    * "A blob type can't be part of a primary key."
    */
   static final int ERR_BLOB_PRIMARY_KEY = 78;

   /**
    * "A BLOB column can't be indexed."
    */
   static final int ERR_BLOB_INDEX = 79;

   /**
    * "A BLOB can't be in the where clause."
    */
   static final int ERR_BLOB_WHERE = 80;

   /**
    * "A BLOB can't be converted to a string."
    */
   static final int ERR_BLOB_STRING = 81;

   /**
    * "Blobs types can't be in ORDER BY or GROUP BY clauses.
    */
   static final int ERR_BLOB_ORDER_GROUP = 82;

   /**
    * "It is not possible to compare BLOBs."
    */
   static final int ERR_COMP_BLOBS = 83;

   /**
    * "It is only possible to insert or update a BLOB through prepared statements."
    */
   static final int ERR_BLOBS_PREPARED = 84;

   /**
    * Total Litebase possible errors.
    */
   static final int TOTAL_ERRORS = 85;
   
   // Error tables
   private static final String[] errorMsgs_en = new String[TOTAL_ERRORS];
   private static final String[] errorMsgs_pt = new String[TOTAL_ERRORS];

   static
   {
      // Some errors have space at the end. This can't be changed.

      // English messages.
      // General errors.
      errorMsgs_en[ERR_MESSAGE_START] = "Error: ";
      errorMsgs_en[ERR_MESSAGE_POSITION] = " Near position ";
      errorMsgs_en[ERR_SYNTAX_ERROR] = "Syntax error.";

      // Limit errors.
      errorMsgs_en[ERR_MAX_TABLE_NAME_LENGTH] = "Table name too big: must be <= 23.";
      errorMsgs_en[ERR_FIELDS_OVERFLOW] = "The maximum number of fields in a SELECT clause was exceeded.";
      errorMsgs_en[ERR_COLUMNS_OVERFLOW] = "The maximum number of columns was exceeded.";  
      
      // Column errors.
      errorMsgs_en[ERR_UNKNOWN_COLUMN] = "Unknown column: ";
      errorMsgs_en[ERR_INVALID_COLUMN_NAME] = "Invalid column name: ";
      errorMsgs_en[ERR_INVALID_COLUMN_NUMBER] = "Invalid column number: ";
      errorMsgs_en[ERR_COLUMN_DOESNOT_HAVE_AN_INDEX] = "The following column(s) does (do) not have an associated index ";
      errorMsgs_en[ERR_AMBIGUOUS_COLUMN_NAME] = "Column name in field list is ambiguous: ";
      errorMsgs_en[ERR_COLUMN_NOT_FOUND] = "Column not found: ";
      errorMsgs_en[ERR_DUPLICATED_COLUMN_NAME] = "Duplicated column name ";

      // Primary key errors.
      errorMsgs_en[ERR_PRIMARY_KEY_ALREADY_DEFINED] = "A primary key was already defined for this table.";
      errorMsgs_en[ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY] = "Table does not have a primary key.";
      errorMsgs_en[ERR_STATEMENT_CREATE_DUPLICATED_PK] = "Statement creates a duplicated primary key in ";

      // Type errors.
      errorMsgs_en[ERR_INCOMPATIBLE_TYPES] = "Incompatible types";
      errorMsgs_en[ERR_FIELD_SIZE_IS_NOT_INT] = "Field size must be a positive interger value.";
      errorMsgs_en[ERR_CHAR_TOO_BIG] = "The maximum size of CHAR or VARCHAR is 65535.";
      
      // Number of fields errors.
      errorMsgs_en[ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH] = "The number of fields does not match the number of values ";
      errorMsgs_en[ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION] = "The given number of values does not match the table definition.";

      // Default value errors.
      errorMsgs_en[ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER] = "Length of default value is bigger than column size.";
      errorMsgs_en[ERR_NOT_NULL_DEFAULT] = "An added column declared as NOT NULL must have a not null default value.";
      
      // Driver errors.
      errorMsgs_en[ERR_DRIVER_CLOSED] = "This driver instance was closed and can't be used anymore. Please get a new instance of it.";
      errorMsgs_en[ERR_RESULTSET_CLOSED] = "ResultSet already closed!";
      errorMsgs_en[ERR_ROWITERATOR_CLOSED] = "RowIterator already closed!";
      errorMsgs_en[ERR_RESULTSETMETADATA_CLOSED] = "ResultSetMetaData can't be used after the ResultSet is closed.";
      errorMsgs_en[ERR_LITEBASEPRCDLL_NOT_FOUND] = "Can't find native methods implementation for LitebaseConnection. Please install Litebase.dll/prc " 
                                                 + "file.";
      errorMsgs_en[ERR_INVALID_CRID] = "The application id must be 4 characters long.";
      errorMsgs_en[ERR_INVALID_INC] = "The increment must be greater than 0 or -1.";
      errorMsgs_en[ERR_INVALID_PARAMETER] = "Invalid connection parameter: ";
      errorMsgs_en[ERR_PREPARED_CLOSED] = "The prepared statement is already closed.";
      
      // Table errors.
      errorMsgs_en[ERR_TABLE_NAME_NOT_FOUND] = "Table name not found: ";
      errorMsgs_en[ERR_TABLE_ALREADY_CREATED] = "Table already created: ";
      errorMsgs_en[ERR_WRONG_STRING_FORMAT] =  "It is not possible to open a table within a connection with a different string format.";
      errorMsgs_en[ERR_WRONG_CRYPTO_FORMAT] = "It is not possible to open a table within a connection with a different cryptography format.";
      
      // ROWID error.
      errorMsgs_en[ERR_ROWID_CANNOT_BE_CHANGED] = "ROWID can't be changed by the user!";

      // Prepared Statement errors.
      errorMsgs_en[ERR_QUERY_DOESNOT_RETURN_RESULTSET] = "SQL statement does not return result set.";
      errorMsgs_en[ERR_QUERY_DOESNOT_PERFORM_UPDATE] = "SQL statement does not perform updates in the database.";
      errorMsgs_en[ERR_NOT_ALL_PARAMETERS_DEFINED] = "Not all parameters of the query had their values defined.";
      errorMsgs_en[ERR_PARAMETER_NOT_DEFINED] = "A value was not defined for the parameter ";
      errorMsgs_en[ERR_INVALID_PARAMETER_INDEX] = "Invalid parameter index.";

      // Rename errors.
      errorMsgs_en[ERR_TABLE_ALREADY_EXIST] = "Can't rename table. This table already exists: ";
      errorMsgs_en[ERR_COLUMN_ALREADY_EXIST] = "Column already exists: ";

      // Alias errors.
      errorMsgs_en[ERR_NOT_UNIQUE_ALIAS_TABLE] = "Not unique table/alias: ";
      errorMsgs_en[ERR_DUPLICATE_ALIAS] = "This alias is already being used in this expression: ";
      errorMsgs_en[ERR_REQUIRED_ALIAS] = "An alias is required for the aggregate function column.";

      // Litebase.execute() error.
      errorMsgs_en[ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED] = "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute().";

      // Order by and group by errors.
      errorMsgs_en[ERR_ORDER_GROUPBY_MUST_MATCH] = "ORDER BY and GROUP BY clauses must match.";
      errorMsgs_en[ERR_VIRTUAL_COLUMN_ON_GROUPBY] = "No support for virtual columns in SQL queries with GROUP BY clause.";

      // Function errors.
      errorMsgs_en[ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT] = "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY " 
                                                        + "clause.";
      errorMsgs_en[ERR_IS_NOT_AGGREG_FUNCTION] = " is not an aggregation function. All fields present in a HAVING "
            + "clause must be listed in the SELECT clause as aliased aggregation funtions.";
      errorMsgs_en[ERR_CANNOT_MIX_AGGREG_FUNCTION] = "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY " 
                                                   + "clause.";
      errorMsgs_en[ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY] = "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause.";
      errorMsgs_en[ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION] = " was not listed in the SELECT clause. All fields present in a HAVING "
            + "clause must be listed in the SELECT clause as aliased aggregation funtions.";
      errorMsgs_en[ERR_SUM_AVG_WITH_DATE_DATETIME] = "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields.";

      // DATETIME error.
      errorMsgs_en[ERR_VALUE_ISNOT_DATETIME] = "Value is not a DATETIME: ";

      // Index error.
      errorMsgs_en[ERR_INDEX_ALREADY_CREATED] = "Index already created for column ";
      errorMsgs_en[ERR_DROP_PRIMARY_KEY] = "Can't drop a primary key index with drop index.";
      errorMsgs_en[ERR_INDEX_LARGE] = "Index too large. It can't have more than 65534 nodes.";
      
      // NOT NULL errors.
      errorMsgs_en[ERR_PK_CANT_BE_NULL] = "Primary key can't have null.";
      errorMsgs_en[ERR_FIELD_CANT_BE_NULL] = "Field can't be null: ";
      errorMsgs_en[ERR_PARAM_NULL] = "A parameter in a where clause can't be null.";
      
      // Result set errors.
      errorMsgs_en[ERR_RS_INV_POS] = "ResultSet in invalid record position.";
      errorMsgs_en[ERR_RS_DEC_PLACES_START] = "Invalid value for decimal places: ";
      errorMsgs_en[ERR_RS_DEC_PLACES_END] = ". Must be in the range -1 to 40.";

      // File errors.
      errorMsgs_en[ERR_CANT_READ] = "Can't read from table.";
      errorMsgs_en[ERR_CANT_LOAD_NODE] = "Can't load node: index corrupted.";
      errorMsgs_en[ERR_TABLE_CORRUPTED] = "Table is corrupted: ";
      errorMsgs_en[ERR_TABLE_NOT_CLOSED] = "Table not closed properly: ";
      errorMsgs_en[ERR_TABLE_CLOSED] = "A properly closed table can't be used in recoverTable(): "; // juliana@222_2
      errorMsgs_en[ERR_IDX_RECORD_DEL] = "Can't find index record position on delete.";
      errorMsgs_en[ERR_WRONG_VERSION] = "The table format is incompatible with Litebase version. Please update your tables.";
      errorMsgs_en[ERR_WRONG_PREV_VERSION] = "The table format is not the previous one: "; // juliana@220_11
      errorMsgs_en[ERR_INVALID_PATH] = "Invalid path: "; // juliana@214_1
      errorMsgs_en[ERR_DB_NOT_FOUND] = "Database not found."; // juliana@226_10
      
      // BLOB errors.
      errorMsgs_en[ERR_BLOB_TOO_BIG] = "The total size of a blob can't be greater then 10 Mb.";
      errorMsgs_en[ERR_INVALID_MULTIPLIER] = "This is not a valid size multiplier.";
      errorMsgs_en[ERR_BLOB_PRIMARY_KEY] = "A blob type can't be part of a primary key.";
      errorMsgs_en[ERR_BLOB_INDEX] = "A BLOB column can't be indexed.";
      errorMsgs_en[ERR_BLOB_WHERE] = "A BLOB can't be in the where clause.";
      errorMsgs_en[ERR_BLOB_STRING] = "A BLOB can't be converted to a string.";
      errorMsgs_en[ERR_BLOB_ORDER_GROUP] = "Blobs types can't be in ORDER BY or GROUP BY clauses.";
      errorMsgs_en[ERR_COMP_BLOBS] = "It is not possible to compare BLOBs.";
      errorMsgs_en[ERR_BLOBS_PREPARED] = "It is only possible to insert or update a BLOB through prepared statements using setBlob().";

      // Portuguese messages.
      // General errors.
      errorMsgs_pt[ERR_MESSAGE_START] = "Erro: ";
      errorMsgs_pt[ERR_MESSAGE_POSITION] = ". Pr�ximo � posi��o ";
      errorMsgs_pt[ERR_SYNTAX_ERROR] = "Erro de sintaxe.";

      // Limit errors.
      errorMsgs_pt[ERR_MAX_TABLE_NAME_LENGTH] = "Nome da tabela muito grande: deve ser <= 23.";
      errorMsgs_pt[ERR_FIELDS_OVERFLOW] = "O n�mero m�ximo de campos na cl�usula SELECT foi excedido.";
      errorMsgs_pt[ERR_COLUMNS_OVERFLOW] = "O n�mero m�ximo de colunas foi excedido."; 
      
      // Column errors.
      errorMsgs_pt[ERR_UNKNOWN_COLUMN] = "Coluna desconhecida ";
      errorMsgs_pt[ERR_INVALID_COLUMN_NAME] = "Nome de coluna inv�lido: ";
      errorMsgs_pt[ERR_INVALID_COLUMN_NUMBER] = "N�mero de coluna inv�lido: ";
      errorMsgs_pt[ERR_COLUMN_DOESNOT_HAVE_AN_INDEX] = "A(s) coluna(s) a seguir n�o tem (t�m) um ind�ce associado ";
      errorMsgs_pt[ERR_AMBIGUOUS_COLUMN_NAME] = "Nome de coluna amb�guo: ";
      errorMsgs_pt[ERR_COLUMN_NOT_FOUND] = "Coluna n�o encontrada: ";
      errorMsgs_pt[ERR_DUPLICATED_COLUMN_NAME] = "Nome de coluna duplicado ";

      // Primary key errors.
      errorMsgs_pt[ERR_PRIMARY_KEY_ALREADY_DEFINED] = "Uma chave primaria j� foi definida para esta tabela.";
      errorMsgs_pt[ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY] = "Tabela n�o tem chave prim�ria.";
      errorMsgs_pt[ERR_STATEMENT_CREATE_DUPLICATED_PK] = "Comando cria uma chave prim�ria duplicada em ";

      // Type errors.
      errorMsgs_pt[ERR_INCOMPATIBLE_TYPES] = "Tipos incompat�veis";
      errorMsgs_pt[ERR_FIELD_SIZE_IS_NOT_INT] = "Tamanho do campo deve ser um valor inteiro positivo.";
      errorMsgs_pt[ERR_CHAR_TOO_BIG] = "O tamanho m�ximo de um CHAR ou VARCHAR � 65535.";
      
      // Number of fields errors.
      errorMsgs_pt[ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH] = "O n�mero de campos � diferente do n�mero de valores ";
      errorMsgs_pt[ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION] = "O n�mero de valores dado n�o coincide com a defini��o da tabela.";

      // Default value errors.
      errorMsgs_pt[ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER] = "Tamanho do valor padr�o � maior que o tamanho definido para a coluna.";
      errorMsgs_pt[ERR_NOT_NULL_DEFAULT] = "Uma coluna adicionada declarada como NOT NULL deve ter um valor padr�o n�o nulo.";
      
      // Driver errors.
      errorMsgs_pt[ERR_DRIVER_CLOSED] = "Esta inst�ncia do driver est� fechada e n�o pode ser mais utilizada. Por favor, obtenha uma nova " 
                                      + "inst�ncia.";
      errorMsgs_pt[ERR_RESULTSET_CLOSED] = "ResultSet j� est� fechado!";
      errorMsgs_pt[ERR_ROWITERATOR_CLOSED] = "RowIterator j� est� fechado!";
      errorMsgs_pt[ERR_RESULTSETMETADATA_CLOSED] = "ResultSetMetaData n�o pode ser usado depois que o ResultSet estiver fechado.";
      errorMsgs_pt[ERR_LITEBASEPRCDLL_NOT_FOUND] = "N�o � poss�vel encontrar a implementa��o dos m�todos nativos "
            + "para o LitebaseConnection. Por favor, instale o arquivo Litebase.dll/prc.";
      errorMsgs_pt[ERR_INVALID_CRID] = "O id da aplica��o de ter 4 characteres.";
      errorMsgs_pt[ERR_INVALID_INC] = "O incremento deve ser maior do que 0 ou -1.";
      errorMsgs_pt[ERR_INVALID_PARAMETER] = "Par�metro de conex�o inv�lido: ";
      errorMsgs_pt[ERR_PREPARED_CLOSED] = "O prepared statement j� est� fechado.";
      
      // Table errors.
      errorMsgs_pt[ERR_TABLE_NAME_NOT_FOUND] = "Nome da tabela n�o encontrado: ";
      errorMsgs_pt[ERR_TABLE_ALREADY_CREATED] = "Tabela j� existe: ";
      errorMsgs_pt[ERR_WRONG_STRING_FORMAT] =  "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de strings diferente.";
      errorMsgs_pt[ERR_WRONG_CRYPTO_FORMAT] = "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de criptografia diferente.";
      
      // ROWID error.
      errorMsgs_pt[ERR_ROWID_CANNOT_BE_CHANGED] = "ROWID n�o pode ser mudado pelo usu�rio!";

      // Prepared Statement errors.
      errorMsgs_pt[ERR_QUERY_DOESNOT_RETURN_RESULTSET] = "Comando SQL n�o retorna um ResultSet.";
      errorMsgs_pt[ERR_QUERY_DOESNOT_PERFORM_UPDATE] = "Comando SQL n�o executa uma atualiza��o no banco de dados.";
      errorMsgs_pt[ERR_NOT_ALL_PARAMETERS_DEFINED] = "Nem todos os par�metros da consulta tiveram seus valores definidos.";
      errorMsgs_pt[ERR_PARAMETER_NOT_DEFINED] = "N�o foi definido um valor para o par�metro ";
      errorMsgs_pt[ERR_INVALID_PARAMETER_INDEX] = "�ndice de par�metro inv�lido.";

      // Rename errors.
      errorMsgs_pt[ERR_TABLE_ALREADY_EXIST] = "N�o � poss�vel renomear a tabela. Esta tabela j� existe: ";
      errorMsgs_pt[ERR_COLUMN_ALREADY_EXIST] = "Coluna j� existe: ";

      // Alias errors.
      errorMsgs_pt[ERR_NOT_UNIQUE_ALIAS_TABLE] = "Nome de tabela/alias repetido: ";
      errorMsgs_pt[ERR_DUPLICATE_ALIAS] = "Este alias j� est� sendo utilizado no sql: ";
      errorMsgs_pt[ERR_REQUIRED_ALIAS] = "Um alias � necess�rio para colunas com fun��o de agrega��o.";

      // Litebase.execute() error.
      errorMsgs_pt[ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED] = "Apenas CREATE TABLE e CREATE INDEX s�o permitidos no Litebase.execute().";

      // Order by and group by errors.
      errorMsgs_pt[ERR_ORDER_GROUPBY_MUST_MATCH] = "Cl�usulas ORDER BY e GROUP BY devem coincidir.";
      errorMsgs_pt[ERR_VIRTUAL_COLUMN_ON_GROUPBY] = "SQL com cl�usula GROUP BY n�o tem suporte para colunas virtuais.";

      // Function errors.
      errorMsgs_pt[ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT] = "Todas colunas que n�os�o fun��es de "
            + "agrega��o na cl�usula SELECT devem estar na cl�usula GROUP BY.";
      errorMsgs_pt[ERR_IS_NOT_AGGREG_FUNCTION] = " n�o � uma fun��o de agrega��o. Todos as colunas "
            + "da cl�usula HAVING devem ser listadas no SELECT utilizando alias.";
      errorMsgs_pt[ERR_CANNOT_MIX_AGGREG_FUNCTION] = "N�o � possivel misturar colunas reais e de agrega��o no SELECT sem cl�usula GROUP BY.";
      errorMsgs_pt[ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY] = "N�o � poss�vel ter fun��es de agrega��o com cl�usula ORDER BY sem cl�usula GROUP BY.";
      errorMsgs_pt[ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION] = " n�o foi listado no SELECT. Todas "
            + "as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.";
      errorMsgs_pt[ERR_SUM_AVG_WITH_DATE_DATETIME] = "Fun��es de agrega��o SUM e AVG n�o s�o usadas com colunas do tipo DATE e DATETIME.";

      // DATETIME error.
      errorMsgs_pt[ERR_VALUE_ISNOT_DATETIME] = "Valor n�o � um tipo DATETIME v�lido: ";

      // Index error.
      errorMsgs_pt[ERR_INDEX_ALREADY_CREATED] = "�ndice j� criado para a coluna ";
      errorMsgs_pt[ERR_DROP_PRIMARY_KEY] = "N�o � poss�vel remover uma chave prim�ria usando drop index.";
      errorMsgs_pt[ERR_INDEX_LARGE] = "�ndice muito grande. Ele n�o pode ter mais do que 65534 n�s.";
      
      // NOT NULL errors.
      errorMsgs_pt[ERR_PK_CANT_BE_NULL] = "Chave prim�ria n�o pode ter NULL.";
      errorMsgs_pt[ERR_FIELD_CANT_BE_NULL] = "Coluna n�o pode ser NULL: ";
      errorMsgs_pt[ERR_PARAM_NULL] = "Um par�metro em uma where clause n�o pode ser NULL.";
      
      // Result set errors.
      errorMsgs_pt[ERR_RS_INV_POS] = "ResultSet em uma posi��o de registro inv�lida.";
      errorMsgs_pt[ERR_RS_DEC_PLACES_START] = "Valor inv�lido para casas decimais: ";
      errorMsgs_pt[ERR_RS_DEC_PLACES_END] = ". Deve ficar entre - 1 e 40.";

      // File errors.
      errorMsgs_pt[ERR_CANT_READ] = "N�o � poss�vel ler da tabela.";
      errorMsgs_pt[ERR_CANT_LOAD_NODE] = "N�o � poss�vel carregar o n�: �ndice corrompido.";
      errorMsgs_pt[ERR_TABLE_CORRUPTED] = "Tabela est� corrompida: ";
      errorMsgs_pt[ERR_TABLE_NOT_CLOSED] = "Tabela n�o foi fechada corretamente: ";
      errorMsgs_pt[ERR_TABLE_CLOSED] = "Uma tabela fechada corretamente n�o pode ser usada no recoverTable(): "; // juliana@222_2
      errorMsgs_pt[ERR_IDX_RECORD_DEL] = "N�o � poss�vel achar a posi��o de registro no �ndice na exclus�o.";
      errorMsgs_pt[ERR_WRONG_VERSION] = "O formato de tabela n�o � compat�vel com a vers�o do Litebase. Por favor, atualize suas tabelas.";
      errorMsgs_pt[ERR_WRONG_PREV_VERSION] = "O formato de tabela n�o � o anterior: "; // juliana@220_11
      errorMsgs_pt[ERR_INVALID_PATH] = "Caminho inv�lido: "; // juliana@214_1
      errorMsgs_pt[ERR_DB_NOT_FOUND] = "Base de dados n�o encontrada."; // juliana@226_10
      
      // BLOB errors.
      errorMsgs_pt[ERR_BLOB_TOO_BIG] = "O tamanho total de um BLOB n�o pode ser maior do que 10 Mb.";
      errorMsgs_pt[ERR_INVALID_MULTIPLIER] = "O multiplicador de tamanho n�o � v�lido.";
      errorMsgs_pt[ERR_BLOB_PRIMARY_KEY] = "Um tipo BLOB n�o pode ser parte de uma chave prim�ria.";
      errorMsgs_pt[ERR_BLOB_INDEX] = "Uma coluna do tipo BLOB n�o pode ser indexada.";
      errorMsgs_pt[ERR_BLOB_WHERE] = "Um BLOB n�o pode estar na cl�usula WHERE.";
      errorMsgs_pt[ERR_BLOB_STRING] = "Um BLOB n�o pode ser convertido em uma string.";
      errorMsgs_pt[ERR_BLOB_ORDER_GROUP] = "Tipos BLOB n�o podem estar em cl�usulas ORDER BY ou GROUP BY.";
      errorMsgs_pt[ERR_COMP_BLOBS] = "N�o � poss�vel comparar BLOBs.";
      errorMsgs_pt[ERR_BLOBS_PREPARED] = "S� � poss�vel inserir ou atualizar um BLOB atrav�s prepared statements usando setBlob().";
   }

   /**
    * Gets the correct error message.
    * 
    * @param messageNumber The error message code.
    * @return The string with the desired error message.
    */
   static String getMessage(int messageNumber)
   {
      if (LitebaseConnection.language == LitebaseConnection.LANGUAGE_PT)
         return errorMsgs_pt[messageNumber];
      return errorMsgs_en[messageNumber];
   }
}
