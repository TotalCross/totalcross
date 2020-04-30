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

// Constants.h,v 1.1.2.32 2010-08-25 21:34:46 juliana Exp

/**
 * Defines all constants used by Litebase.
 */

#ifndef LITEBASE_CONSTANTS_H
#define LITEBASE_CONSTANTS_H

// Table files.
#define DB_EXT   ".db"  // Database files.
#define DBO_EXT  ".dbo" // Database object files.
#define IDK_EXT  ".idk" // Index b-tree files.

// juliana@noidr_1: removed .idr files from all indices and changed its format. 

// Constants used in date format.  
#define DATE_MDY  1  // rnovais@567_2: month day year.
#define DATE_DMY  2  // rnovais@567_2: day month year.
#define DATE_YMD  3  // rnovais@567_2: year month day.

// juliana@114_9: the absence of primary key can't be zero because the rowid may be a primary key or it can have an index.
#define NO_PRIMARY_KEY   -1  // nowosad@200 - Indicates if table does not have primary key.
#define NO_COLUMN_NAMES  0   // nowosad@200 - Indicates if table has column names info.

// These numbers must be synchronized with ResultSetMetaData types.
#define UNDEFINED_TYPE     -1 // Undefined type.
#define CHARS_TYPE         0  // CHARS type. 
#define SHORT_TYPE         1  // SHORT type.
#define INT_TYPE           2  // INT type.
#define LONG_TYPE          3  // LONG type.
#define FLOAT_TYPE         4  // FLOAT type.
#define DOUBLE_TYPE        5  // DOUBLE type.
#define CHARS_NOCASE_TYPE  6  // CHARS type that has case insensitive comparison.
#define BOOLEAN_TYPE       7  // BOOLEAN type. Used for expressions.
#define DATE_TYPE          8  // rnovais@567_2: int
#define DATETIME_TYPE      9  // rnovais@567_2: stored as two int
#define BLOB_TYPE          10 // BLOB type.

// Constants for tables and indices.
#define DEFAULT_ROW_INC  10    // The default record increment when growing the table file.  
#define CACHE_SIZE       20    // The index cache size.
#define RECGROWSIZE      64    // The record increment for indices.
#define SECTOR_SIZE      512   // The record size used to calculate the number of keys per b-tree node.
#define MAX_IDX          65534 // The maximum number of nodes of an index. // juliana@noidr_2
#define DBNAME_SIZE      41    // Space for the name of the table plus the identification of the index, if needed.
#define COMP_IDX_PK_SIZE 64    // The space for composed indices in the header of .db.
#define DEFAULT_HEADER   512   // The default header size.
#define VERSION_TABLE    203   // The current table format version. // juliana@230_12

// Aggregate Functions supported
#define FUNCTION_AGG_NONE   -1 // No function.
#define FUNCTION_AGG_COUNT  0  // COUNT()
#define FUNCTION_AGG_MAX    1  // MAX()
#define FUNCTION_AGG_MIN    2  // MIN()
#define FUNCTION_AGG_AVG    3  // AVG()
#define FUNCTION_AGG_SUM    4  // SUM()

// rnovais@568_10: supported DtaType Functions 
#define FUNCTION_DT_NONE   -1 // No function. 
#define FUNCTION_DT_YEAR    0 // YEAR()
#define FUNCTION_DT_MONTH   1 // MONTH()
#define FUNCTION_DT_DAY     2 // DAY()
#define FUNCTION_DT_HOUR    3 // HOUR()
#define FUNCTION_DT_MINUTE  4 // MINUTE()
#define FUNCTION_DT_SECOND  5 // SECOND()
#define FUNCTION_DT_MILLIS  6 // MILLIS()
#define FUNCTION_DT_ABS     7 // rnovais@570_1: ABS()
#define FUNCTION_DT_UPPER   8 // rnovais@570_1: UPPER()
#define FUNCTION_DT_LOWER   9 // rnovais@570_1: LOWER()

// where clause expression tree possible types.
#define WC_TYPE_AND_DIFF_RS  0 // It is an AND of expressions.
#define WC_TYPE_OR_DIFF_RS   1 // It is an OR of expressions.

// juliana@250_8: now the maximum number of columns, fields, tables, etc is 254 instead of 128 except on palm.
// guich@561_1: maximums for the parsing process. 
#define MAXIMUMS 254 // guich@561_1: maximums for the parsing process. 

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
#ifdef ANDROID
#define MAX_OPEN_FILES 512
#elif defined darwin
#define MAX_OPEN_FILES 128
#elif defined POSIX
#define MAX_OPEN_FILES 1024
#endif 

// Available operand types. 
#define OP_NONE                0 // No operator.
#define OP_BOOLEAN_AND         1 // Boolean operator AND.
#define OP_BOOLEAN_OR          2 // Boolean operator OR.
#define OP_BOOLEAN_NOT         3 // Boolean operator NOT.
#define OP_REL_EQUAL           4 // Relational operator =.
#define OP_REL_DIFF            5 // Relational operator != or <>.
#define OP_REL_GREATER         6 // Relational operator >.
#define OP_REL_LESS            7 // Relational operator <.
#define OP_REL_GREATER_EQUAL   8 // Relational operator >=.
#define OP_REL_LESS_EQUAL      9 // Relational operator <=.
#define OP_STRING_LITERAL     10 // A string literal.
#define OP_NUMERIC_LITERAL    11 // A numerical literal.
#define OP_IDENTIFIER         12 // An identifier.
#define OP_PAT_MATCH_LIKE     13 // LIKE operator.
#define OP_PAT_MATCH_NOT_LIKE 14 // NOT LIKE operator.
#define OP_PAT_IS             15 // rnovais@200_1: IS operator.
#define OP_PAT_IS_NOT         16 // rnovais@200_1: IS NOT operator.
#define OP_PAT_NULL           17 // rnovais@200_1: NULL operand.

/// Possible commands
#define CMD_CREATE_TABLE         1  // CREATE TABLE ...
#define CMD_CREATE_INDEX         2  // CREATE INDEX ...
#define CMD_DROP_TABLE           3  // DROP TABLE ...
#define CMD_DROP_INDEX           4  // DROP INDEX ...
#define CMD_ALTER_DROP_PK        5  // ALTER TABLE ... DROP PRIMAY KEY
#define CMD_ALTER_ADD_PK         6  // ALTER TABLE ... ADD PRIMARY KEY (...) 
#define CMD_ALTER_RENAME_TABLE   7  // ALTER TABLE ... RENAME TO ...
#define CMD_ALTER_RENAME_COLUMN  8  // ALTER TABLE ... RENAME ... TO ...
#define CMD_ALTER_ADD_COLUMN     9  // ALTER TABLE ADD... (new column definition) // juliana@add_1
#define CMD_SELECT               10 // SELECT ...
#define CMD_INSERT               11 // INSERT INTO ...
#define CMD_UPDATE               12 // UPDATE ...
#define CMD_DELETE               13 // DELETE ...

#define HCROWID    108705909   // hash code for the rowid.
#define LOGS_INT   1280264019  // LOGS_INT = Convert.chars2int("LOGS");
#define CRC32_SIZE 256         // The crc table size.

#define MAX_TABLE_NAME_LENGTH 23 // Maximum table file name.

// juliana@noidr_1: removed .idr files from all indices and changed its format.
// Constants for keys.
#define VALREC_SIZE                 4         // The size of the record of a key: always an int.
#define NO_VALUE                    0xFFFFFFFF // Represents a key that has no values attached to it.
#define LEAF                        0xFFFF    // A leaf node.

// juliana@noidr_1: removed .idr files from all indices and changed its format.
// Column attributes.
#define ATTR_COLUMN_HAS_INDEX    1   // This column has an index.
#define ATTR_COLUMN_HAS_DEFAULT  2   // This column has default value.
#define ATTR_COLUMN_IS_NOT_NULL  4   // This column can't be null.
#define ATTR_COLUMN_HAS_NO_INDEX -2  // ~ATTR_COLUMN_HAS_INDEX // juliana@227_6
#define ATTR_DEFAULT_AUX_ROWID   -1  // rnovais@570_61: Auxiliar default rowid.

// Constants that indicates what should be saved when saving table meta data.
#define TSMD_ONLY_DELETEDROWSCOUNT  1 // Saves only the deleted rows count and header information.
#define TSMD_ONLY_AUXROWID          2 //rnovais@570_61: Also save the auxiliary row id.
#define TSMD_ONLY_PRIMARYKEYCOL     3 // Also save the primary key column information.
#define TSMD_ATLEAST_INDEXES        4 // Also save index information. 
#define TSMD_EVERYTHING             5 // Save everything.

// Constants used to differenciate the kinds of tokens.
// juliana@parser_1: improved Litebase parser.
#define IS_ALPHA       1  // Is an alphabetic character [a..z]|[A..Z]. 
#define IS_DIGIT       2  // Is a digit [0..9].
#define IS_SIGN        4  // Is a sign (+ / -).
#define IS_END_NUM     8  // Is a numer type identifier (e|E|d|D|l|L).
#define IS_RELATIONAL  16 // Is a relational operator: '>', '<', and '!'.
#define IS_PUNCT       32 // Is a punctution symbol: ':', ',', '?', and '='. 
#define IS_OPERATOR    64 // Is a operator symbol: '*', '(', ')'.
#define IS_ALPHA_DIGIT 3  // (IS_ALPHA|IS_DIGIT|'_').
#define IS_START_DIGIT 6  // (IS_DIGIT|IS_SIGN).
#define PARSER_EOF     -1 // End of file.
#define PARSER_ERROR   -2 // Parser error.  

// Reserved words.
#define NUM_RESERVED 61          // Number of reserved words.
#define HT_ABS			96370       // ABS reserved word hash code.
#define HT_ADD			96417       // ADD reserved word hash code.
#define HT_ALTER		92913686    // ALTER reserved word hash code.
#define HT_AND			96727       // AND reserved word hash code. 
#define HT_AS			3122        // AS reserved word hash code.
#define HT_ASC			96881       // ASC reserved word hash code.
#define HT_AVG			96978       // AVG reserved word hash code.
#define HT_BLOB		3026845     // BLOB reserved word hash code.
#define HT_BY			3159        // BY reserved word hash code.
#define HT_CHAR		3052374     // CHAR reserved word hash code.
#define HT_COUNT		94851343    // COUNT reserved word hash code.
#define HT_CREATE		-1352294148 // CREATE reserved word hash code.
#define HT_DATE		3076014     // DATE reserved word hash code. 
#define HT_DATETIME  1793702779  // DATETIME reserved word hash code.
#define HT_DAY			99228       // DAY reserved word hash code.
#define HT_DEFAULT	1544803905  // DEFAULT reserved word hash code.
#define HT_DELETE		-1335458389 // DELETE reserved word hash code.
#define HT_DESC		3079825     // DESC reserved word hash code.
#define HT_DISTINCT  288698108   // DISTINCT reserved word hash code.
#define HT_DOUBLE		-1325958191 // DOUBLE reserved word hash code.
#define HT_DROP		3092207     // DROP reserved word hash code.
#define HT_FLOAT		97526364    // FLOAT reserved word hash code.
#define HT_FROM		3151786     // FROM reserved word hash code.
#define HT_GROUP		98629247    // GROUP reserved word hash code.
#define HT_HAVING		-1224334299 // HAVING reserved word hash code.
#define HT_HOUR		3208676     // HOUR reserved word hash code.
#define HT_INDEX		100346066   // INDEX reserved word hash code.
#define HT_INSERT		-1183792455 // INSERT reserved word hash code.
#define HT_INT			104431      // INT reserved word hash code.
#define HT_INTO		3237472     // INTO reserved word hash code. 
#define HT_IS			3370        // IS reserved word hash code.
#define HT_KEY			106079      // KEY reserved word hash code.
#define HT_LIKE		3321751     // LIKE reserved word hash code.
#define HT_LONG		3327612     // LONG reserved word hash code.
#define HT_LOWER		103164673   // LOWER reserved word hash code.
#define HT_MAX			107876      // MAX reserved word hash code.
#define HT_MILLIS		-1074095546 // MILLIS reserved word hash code.
#define HT_MIN			108114      // MIN reserved word hash code.
#define HT_MINUTE		-1074026988 // MINUTE reserved word hash code.
#define HT_MONTH		104080000   // MONTH reserved word hash code.
#define HT_NOCASE		-1040203663 // NOCASE reserved word hash code.
#define HT_NOT			109267      // NOT reserved word hash code. 
#define HT_NULL		3392903     // NULL reserved word hash code. 
#define HT_ON			3551        // ON reserved word hash code.
#define HT_OR			3555        // OR reserved word hash code.
#define HT_ORDER		106006350   // ORDER reserved word hash code.
#define HT_PRIMARY	-314765822  // PRIMARY reserved word hash code.
#define HT_RENAME		-934594754  // RENAME reserved word hash code.
#define HT_SECOND		-906279820  // SECOND reserved word hash code.
#define HT_SELECT		-906021636  // SELECT reserved word hash code.
#define HT_SET			113762      // SET reserved word hash code.
#define HT_SHORT		109413500   // SHORT reserved word hash code.
#define HT_SUM			114251      // SUM reserved word hash code.
#define HT_TABLE		110115790   // TABLE reserved word hash code.
#define HT_TO			3707        // TO reserved word hash code.
#define HT_UPDATE		-838846263  // UPDATE reserved word hash code.
#define HT_UPPER		111499426   // UPPER reserved word hash code.
#define HT_VALUES		-823812830  // VALUES reserved word hash code.
#define HT_VARCHAR	236613373   // VARCHAR reserved word hash code.
#define HT_WHERE		113097959   // WHERE reserved word hash code.
#define HT_YEAR		3704893     // YEAR reserved word hash code.

#define MAX_RESERVED_SIZE 9 // The maximum size of a reserved word.

// Parser tokens.
// juliana@parser_1: improved Litebase parser.
#define TK_CHAR             0 // CHAR reserved word token.
#define TK_SHORT            1 // SHORT reserved word token.
#define TK_INT              2 // INT reserved word token.
#define TK_LONG             3 // LONG reserved word token.
#define TK_FLOAT            4 // FLOAT reserved word token.
#define TK_DOUBLE           5 // DOUBLE reserved word token.
#define TK_VARCHAR          6 // VARCHAR reserved word token.
#define TK_NOCASE           7 // NOCASE reserved word token.
#define TK_DATE             8 // DATE reserved word token.
#define TK_DATETIME         9 // DATETIME reserved word token.
#define TK_BLOB            10 // BLOB reserved word token.
#define TK_ABS             11 // ABS reserved word token.
#define TK_ADD             12 // ADD reserved word token.
#define TK_ALTER           13 // ALTER reserved word token.
#define TK_AND             14 // AND reserved word token.
#define TK_AS              15 // AS reserved word token.
#define TK_ASC             16 // ASC reserved word token.
#define TK_AVG             17 // AVG reserved word token.
#define TK_BY              18 // BY reserved word token.
#define TK_COUNT           19 // COUNT reserved word token.
#define TK_CREATE          20 // CREATE reserved word token.
#define TK_DAY             21 // DAY reserved word token.
#define TK_DEFAULT         22 // DEFAULT reserved word token.
#define TK_DELETE          23 // DELETE reserved word token.
#define TK_DESC            24 // DESC reserved word token.
#define TK_DISTINCT        25 // DISTINCT reserved word token.
#define TK_DROP            26 // DROP reserved word token.
#define TK_FROM            27 // FROM reserved word token.
#define TK_GROUP           28 // GROUP reserved word token.
#define TK_HAVING          29 // HAVING reserved word token.
#define TK_HOUR            30 // HOUR reserved word token.
#define TK_INDEX           31 // INDEX reserved word token.
#define TK_INSERT          32 // INSERT reserved word token.
#define TK_INTO            33 // INTO reserved word token.
#define TK_IS              34 // IS reserved word token.
#define TK_KEY             35 // KEY reserved word token.
#define TK_LIKE            36 // LIKE reserved word token.
#define TK_LOWER           37 // LOWER reserved word token.
#define TK_MAX             38 // MAX reserved word token.
#define TK_MILLIS          39 // MILLIS reserved word token.
#define TK_OPEN            40 // '(' token.
#define TK_CLOSE           41 // ')' token.
#define TK_ASTERISK        42 // '*' token.
#define TK_MIN             43 // MIN reserved word token.
#define TK_COMMA           44 // ',' token. 
#define TK_MINUTE          45 // MINUTE reserved word token.
#define TK_DOT             46 // '.' token. 
#define TK_MONTH           47 // MONTH reserved word token.
#define TK_NOT             48 // NOT reserved word token.
#define TK_NULL            49 // NULL reserved word token.
#define TK_ON              50 // ON reserved word token.
#define TK_OR              51 // OR reserved word token.
#define TK_ORDER           52 // ORDER reserved word token.
#define TK_PRIMARY         53 // PRIMARY reserved word token.
#define TK_RENAME          54 // RENAME reserved word token.
#define TK_SECOND          55 // SECOND reserved word token.
#define TK_SELECT          56 // SELECT reserved word token.
#define TK_SET             57 // SET reserved word token.
#define TK_SUM             58 // SUM reserved word token.
#define TK_TABLE           59 // TABLE reserved word token.
#define TK_LESS            60 // '<' token.
#define TK_EQUAL           61 // '=' token.
#define TK_GREATER         62 // '>' token.
#define TK_INTERROGATION   63 // '?' token.
#define TK_TO              64 // TO reserved word token.
#define TK_UPDATE          65 // UPDATE reserved word token.
#define TK_UPPER           66 // UPPER reserved word token.
#define TK_VALUES          67 // VALUES reserved word token.
#define TK_WHERE           68 // WHERE reserved word token.
#define TK_YEAR            69 // YEAR reserved word token.
#define TK_IDENT           70 // Identifier token.
#define TK_STR             71 // String token.
#define TK_NUMBER          72 // Number token. 
#define TK_GREATER_EQUAL   73 // '>=' token.
#define TK_LESS_EQUAL      74 // '<=' token.
#define TK_DIFF            75 // '<>' or '!=' token.

// Litebase languages.
#define LANGUAGE_EN  1 // English language.
#define LANGUAGE_PT  2 // Portuguese language.

// Litebase error messages.
// General errros.
#define ERR_MESSAGE_START     0  // "Error: "
#define ERR_MESSAGE_POSITION  1  // " Near position "
#define ERR_SYNTAX_ERROR      2  // "Syntax error."

// Limit errors.
#define ERR_MAX_NUM_FIELDS_REACHED          3  // "Maximum number of different fields was reached."
#define ERR_MAX_NUM_PARAMS_REACHED          4  // "Maximum number of paramList in the 'WHERE/HAVING' clause was reached."
#define ERR_MAX_COMP_INDICES                5  // "Maximum number of composed indices 32 was reached."
#define ERR_MAX_TABLE_NAME_LENGTH           6  // "Table name too big: must be <= 23."
#define ERR_FIELDS_OVERFLOW                 7  // "The maximum number of fields in a SELECT clause was exceeded."
#define ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY  8  // "Maximum number of columns exceeded in the 'ORDER BY/GROUP BY' clause."

// Column errors.
#define ERR_UNKNOWN_COLUMN                9  // "Unknown column "
#define ERR_INVALID_COLUMN_NAME           10 // "Invalid column name: "
#define ERR_INVALID_COLUMN_NUMBER         11 // "Invalid column number: " 
#define ERR_COLUMN_DOESNOT_HAVE_AN_INDEX  12 // "The following column(s) does (do) not have an associated index "
#define ERR_AMBIGUOUS_COLUMN_NAME         13 // "Column name in field list is ambiguous: " 
#define ERR_COLUMN_NOT_FOUND              14 // "Column not found: "
#define ERR_DUPLICATED_COLUMN_NAME        15 // "Duplicated column name: "

// Primary key errors.
#define ERR_PRIMARY_KEY_ALREADY_DEFINED     16 // "A primary key was already defined for this table."
#define ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY  17 // "Table does not have a primary key."
#define ERR_STATEMENT_CREATE_DUPLICATED_PK  18 // "Statement creates a duplicated primary key in "

// Type errors.
#define ERR_INCOMPATIBLE_TYPES     19 // "Incompatible types."
#define ERR_FIELD_SIZE_IS_NOT_INT  20 // "Field size must be a positive interger value."
#define ERR_INVALID_NUMBER         21 // "Value is not a valid number for the desired type: "
#define ERR_DATA_TYPE_FUNCTION     22 // "Incompatible data type for the function call: "

// Number of fields errors.
#define ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH  23 // "The number of fields does not match the number of values "
#define ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION      24 // "The given number of values does not match the table definition."

// Default value errors.
#define ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER  25 // "Length of default value is bigger than column size."
#define ERR_NOT_NULL_DEFAULT                26 // "An added column declared as NOT NULL must have a not null default value."

// Driver errors.  
#define ERR_DRIVER_CLOSED             27 // "This driver instance was closed and can't be used anymore. Please get a new instance of it."
#define ERR_RESULTSET_CLOSED          28 // "ResultSet already closed!"
#define ERR_RESULTSETMETADATA_CLOSED  29 // "ResultSetMetaData can't be used after the ResultSet is closed. "
#define ERR_INVALID_CRID              30 // "The application id must be four characters long."
#define ERR_INVALID_INC               31 // "The increment must be greater than 0 or -1."
#define ERR_ROWITERATOR_CLOSED        32 // "Iterator already closed."
#define ERR_PREPARED_STMT_CLOSED      33 // "Prepared statement closed. Please prepare it again."
#define ERR_INVALID_PARAMETER         34 // "Invalid connection parameter: %s". // juliana@253_11

// Table errors.
#define ERR_TABLE_NAME_NOT_FOUND   35 // "Table name not found: "
#define ERR_TABLE_ALREADY_CREATED  36 // "Table already created: "
#define ERR_WRONG_STRING_FORMAT    37 // "It is not possible to open a table within a connection with a different string format."
#define ERR_WRONG_CRYPTO_FORMAT    38 // "It is not possible to open a table within a connection with a different cryptography format."
   
// ROWID errors.
#define ERR_ROWID_CANNOT_BE_CHANGED  39 // "ROWID can't be changed by the user!"

// Prepared Statement errors.
#define ERR_QUERY_DOESNOT_RETURN_RESULTSET  40 // "SQL statement does not return result set."
#define ERR_QUERY_DOESNOT_PERFORM_UPDATE    41 // "SQL statement does not perform updates in the database."
#define ERR_NOT_ALL_PARAMETERS_DEFINED      42 // "Not all parameters of the query had their values defined."
#define ERR_PARAMETER_NOT_DEFINED           43 // "A value was not defined for the parameter "
#define ERR_INVALID_PARAMETER_INDEX         44 // "Invalid parameter index."

// Rename errors. 
#define ERR_TABLE_ALREADY_EXIST   45 // "Can't rename table. This table already exists: "
#define ERR_COLUMN_ALREADY_EXIST  46 // "Column already exists: "

// Alias errors.
#define ERR_NOT_UNIQUE_ALIAS_TABLE  47 // "Not unique table/alias: "
#define ERR_DUPLICATE_ALIAS         48 // "This alias is already being used in this expression: " 
#define ERR_REQUIRED_ALIAS          49 // "An alias is required for the aggregate function column."

// Litebase.execute() error.
#define ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED  50 // "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute()."

// Order by and group by errors.
#define ERR_ORDER_GROUPBY_MUST_MATCH   51 // "ORDER BY and GROUP BY clauses must match."
#define ERR_VIRTUAL_COLUMN_ON_GROUPBY  52 // "No support for virtual columns in SQL queries with GROUP BY clause."

// Function errors.
#define ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT    53 // "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause."

// " is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions."
#define ERR_IS_NOT_AGGREG_FUNCTION             54

// "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause."
#define ERR_CANNOT_MIX_AGGREG_FUNCTION         55 

#define ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY  56 // "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause."

// " was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
#define ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION  57

#define ERR_SUM_AVG_WITH_DATE_DATETIME         58 // "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields."

// DATE and DATETIME errors.
#define ERR_VALUE_ISNOT_DATE      59 // "Value is not a DATE: "
#define ERR_VALUE_ISNOT_DATETIME  60 // "Value is not a DATETIME: "

// Index errors.
#define ERR_INDEX_ALREADY_CREATED  61 // "Index already created for column "
#define ERR_DROP_PRIMARY_KEY       62 // "Can't drop a primary key index with drop index."
#define ERR_INDEX_LARGE            63 // "Index too large. It can't have more than 32767 nodes."

// NOT NULL errors. 
#define ERR_PK_CANT_BE_NULL     64  // "Primary key can't have null." 
#define ERR_FIELD_CANT_BE_NULL  65  // "Field can't be null: "
#define ERR_PARAM_NULL          66  // "A parameter in a where clause can't be null."

// Result set errors. 
#define ERR_RS_INV_POS           67 // "ResultSet in invalid record position: ."
#define ERR_RS_DEC_PLACES_START  68 // "Invalid value for decimal places: . It must range from -1 to 40."

// File errors.
#define ERR_CANT_READ          69 // "Can't read from table "
#define ERR_CANT_LOAD_NODE     70 // "Can't load leaf node!"
#define ERR_TABLE_CORRUPTED    71 // "Table is corrupted: "
#define ERR_TABLE_NOT_CLOSED   72 // "Table not closed properly: " // juliana@220_2
#define ERR_TABLE_CLOSED       73 // "A properly closed table can't be used in recoverTable(): " // juliana@222_2
#define ERR_IDX_RECORD_DEL     74 // "Can't find index record position on delete."
#define ERR_WRONG_VERSION      75 // "The table format is incompatible with Litebase version. Please update your tables."
#define ERR_WRONG_PREV_VERSION 76 // "The table format is not the previous one: "
#define ERR_INVALID_PATH       77 // "Invalid path: " // juliana@214_1
#define ERR_INVALID_POS        78 // "Invalid pos: "
#define ERR_DB_NOT_FOUND       79 // "Database not found." // juliana@226_10
#define ERR_TABLE_OPENED       80 // "An opened table can't be recovered or converted: " // juliana@230_12

// BLOB errors.
#define ERR_BLOB_TOO_BIG        81 // "The total size of a blob can't be greater then 10 Mb."  
#define ERR_INVALID_MULTIPLIER  82 // "This is not a valid size multiplier." 
#define ERR_BLOB_PRIMARY_KEY    83 // "A blob type can't be part of a primary key."
#define ERR_BLOB_INDEX          84 // "A BLOB column can't be indexed."
#define ERR_BLOB_WHERE          85 // "A BLOB can't be in the where clause."
#define ERR_BLOB_STRING         86 // "A BLOB can't be converted to a string."
#define ERR_BLOB_ORDER_GROUP    87 // "Blobs types can't be in ORDER BY or GROUP BY clauses.
#define ERR_COMP_BLOBS          88 // "It is not possible to compare BLOBs."
#define ERR_BLOBS_PREPARED      89 // "It is only possible to insert or update a BLOB through prepared statements."

#define TOTAL_ERRORS  90 // Total Litebase possible errors.

#define MAX_NUM_INDEXES_APPLIED 32 // The maximum number of indexes to be applied. 

// Pattern matching types.
#define PAT_MATCH_STARTS_WITH  1 // %...
#define PAT_MATCH_ENDS_WITH    2 // ...%
#define PAT_MATCH_CONTAINS     3 // %...%
#define PAT_MATCH_ANYTHING     4 // %
#define PAT_MATCH_MIDDLE       5 // rnovais@568_1: accept % in the middle: ...%... 
#define PAT_MATCH_EQUAL        6 // rnovais@568_1: accept without %.

#define PAT_MATCH_CHAR_ZERO_MORE '%' // Pattern matching characters

// Possibles type for the select clause.
#define COUNT_WITH_WHERE  1 // select count(*) from table_name where ... 

#define CACHE_INITIAL_SIZE  2048 // Table files initial cache size.
#define INDEX_SORT_MAX_TIME 40   // The maximum time (in seconds) that will be taken to sort a table before creating the index.

// Join operation constants.
#define NO_RECORD                        0 // The end of the table.
#define VALIDATION_RECORD_OK             1 // The row can be used.
#define VALIDATION_RECORD_NOT_OK         2 // The row can't be used.
#define VALIDATION_RECORD_INCOMPLETE     3 // Must continue the validation.
#define VALIDATION_RECORD_INCOMPLETE_OK  4 // Used internally on booleanTreeEvaluateJoin(). The current branch was validated as true.

// guich@_300: addes support for basic synchronization.
#define ROW_ATTR_SYNCED   0X00000000L // Indicates if the a row was synced. 
#define ROW_ATTR_NEW      0X40000000L // Indicates if the row is new.
#define ROW_ATTR_UPDATED  0x80000000L // Indicates if the row was updated.
#define ROW_ATTR_DELETED  0XC0000000L // Indicates if the row was deleted.
#define ROW_ID_MASK       0x3FFFFFFFL // The rowid mask.
#define ROW_ATTR_MASK     0xC0000000L // The row attributes mask.
#define ROW_ATTR_SHIFT    30L         // The shift for the row attributes.

// juliana@230_16: solved a bug with row iterator.
#define ROW_ATTR_SYNCED_MASK  0 // Indicates if the a row was synced.  
#define ROW_ATTR_DELETED_MASK 3 // Indicates if the row was deleted.

// juliana@210_2: now Litebase supports tables with ascii strings.
// Flags for saving the table.
#define IS_SAVED_CORRECTLY  1 // Indicates that a table was saved correctly.
#define IS_ASCII            2 // Indicates that the table strings are to be saved in the ascii format.
#define USE_CRYPTO          3 // Indicates that the table uses weak cryptography.

// Numerical limits.
#define MIN_SHORT_VALUE   (int16)-32768              // The minimum short value: -32768.
#define MAX_SHORT_VALUE   (int16)32767               // The maximum short value: 32767.

#ifdef POSIX // juliana@226_18: minimum float and double values for POSIX are different from IEEE values.
   #define MIN_FLOAT_VALUE   1.17549435e-38
   #define MIN_DOUBLE_VALUE  2.2250738585072014e-308
#else
   #define MIN_FLOAT_VALUE   1.4e-45    // The minimum float value: 1.4e-45f. 
   #define MIN_DOUBLE_VALUE  4.9E-324   // The minimum double value: 4.9E-324. 
#endif 
#define MAX_FLOAT_VALUE   3.4028235e+38 // The maximum float value: 3.4028235e+38f. 

#endif
