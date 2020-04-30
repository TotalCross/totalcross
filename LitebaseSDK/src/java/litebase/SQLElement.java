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

/**
 * Internal use only. Represents a SQL element, like data types, operands, and functions. It also has useful methods.
 */
class SQLElement
{
   // Possible SQL Commands.
   /**
    * No command.
    */
   static final int CMD_NONE = -1;
   
   /**
    * Represents the SQL command <code>CREATE TABLE...</code>.
    */
   static final int CMD_CREATE_TABLE = 1;

   /**
    * Represents the SQL command <code>CREATE INDEX...</code>.
    */
   static final int CMD_CREATE_INDEX = 2;

   /**
    * Represents the SQL command <code>DROP TABLE...</code>.
    */
   static final int CMD_DROP_TABLE = 3;

   /**
    * Represents the SQL command <code>DROP INDEX...</code>.
    */
   static final int CMD_DROP_INDEX = 4;

   /**
    * Represents the SQL command <code>ALTER TABLE... DROP PRIMAY KEY</code>.
    */
   static final int CMD_ALTER_DROP_PK = 5;

   /**
    * Represents the SQL command <code>ALTER TABLE... ADD PRIMAY KEY</code>.
    */
   static final int CMD_ALTER_ADD_PK = 6;

   /**
    * Represents the SQL command <code>ALTER TABLE... RENAME TO... </code>.
    */
   static final int CMD_ALTER_RENAME_TABLE = 7;

   /**
    * Represents the SQL command <code>ALTER TABLE... RENAME... TO...</code>.
    */
   static final int CMD_ALTER_RENAME_COLUMN = 8;

   /**
    * Represents the SQL command <code>ALTER TABLE ADD... (new column definition) </code>.
    */
   static final int CMD_ALTER_ADD_COLUMN = 9; // juliana@253_22: added command ALTER TABLE ADD column.

   /**
    * Represents the SQL command <code>SELECT [DISTINCT]...</code>.
    */
   static final int CMD_SELECT = 10;

   /**
    * Represents the SQL command <code>INSERT INTO...</code>.
    */
   static final int CMD_INSERT = 11;

   /**
    * Represents the SQL command <code>UPDATE...</code>.
    */
   static final int CMD_UPDATE = 12;

   /**
    * Represents the SQL command <code>DELETE [FROM]...</code>.
    */
   static final int CMD_DELETE = 13;
   
   // SQL Data Types.
   /**
    * Undefined type, which includes any type
    */
   static final int UNDEFINED = -1;

   // The following numbers must be synchronized with ResultSetMetaData types.
   /**
    * String type.
    */
   static final int CHARS = 0;

   /**
    * Short type.
    */
   static final int SHORT = 1;

   /**
    * Integer type.
    */
   static final int INT = 2;

   /**
    * Long type.
    */
   static final int LONG = 3;

   /**
    * Single precision floating point number.
    */
   static final int FLOAT = 4;

   /**
    * Double precision floating point number.
    */
   static final int DOUBLE = 5;

   /**
    * String type that has case insensitive comparison.
    */
   static final int CHARS_NOCASE = 6; // nowosad@200

   /**
    * Boolean type.
    */
   static final int BOOLEAN = 7;

   /**
    * Date type.
    */
   static final int DATE = 8; // rnovais@567_2: stored as an int.

   /**
    * Datetime type.
    */
   static final int DATETIME = 9; // rnovais@567_2: stored as two ints.

   /**
    * Blob type.
    */
   static final int BLOB = 10;

   // Aggregation functions supported.
   /**
    * No aggregation function.
    */
   static final int FUNCTION_AGG_NONE = -1;
   
   /**
    * <code>COUNT</code> aggregation function. It counts the number of elements.
    */
   static final int FUNCTION_AGG_COUNT = 0;

   /**
    * <code>MAX</code> aggregation function. It gets the maximum value.
    */
   static final int FUNCTION_AGG_MAX = 1;

   /**
    * <code>MIN</code> aggregation function. It gets the minimum value.
    */
   static final int FUNCTION_AGG_MIN = 2;

   /**
    * <code>AVG</code> aggregation function. It calculates the average of the values.
    */
   static final int FUNCTION_AGG_AVG = 3;

   /**
    * <code>SUM</code> aggregation function. It sumns the values.
    */
   static final int FUNCTION_AGG_SUM = 4;

   // rnovais@568_10: supported data type functions.
   /**
    * No function.
    */
   static final int FUNCTION_DT_NONE = -1;

   /**
    * <code>YEAR</code> function. It returns the year of a date or a datetime data type.
    */
   static final int FUNCTION_DT_YEAR = 0;

   /**
    * <code>MONTH</code> function. It returns the month of a date or a datetime data type.
    */
   static final int FUNCTION_DT_MONTH = 1;

   /**
    * <code>DAY</code> function. It returns the day of a date or a datetime data type.
    */
   static final int FUNCTION_DT_DAY = 2;

   /**
    * <code>HOUR</code> function. It returns the hours of a datetime data type.
    */
   static final int FUNCTION_DT_HOUR = 3;

   /**
    * <code>MINUTE</code> function. It returns the minutes of a datetime data type.
    */
   static final int FUNCTION_DT_MINUTE = 4;

   /**
    * <code>SECOND</code> function. It returns the seconds of a datetime data type.
    */
   static final int FUNCTION_DT_SECOND = 5;

   /**
    * <code>MILLIS</code> function. It returns the milli secconds of a datetime data type.
    */
   static final int FUNCTION_DT_MILLIS = 6;

   /**
    * <code>ABS</code> function. It returns the absolute value of a number data type.
    */
   static final int FUNCTION_DT_ABS = 7; // rnovais@570_1

   /**
    * <code>UPPER</code> function. It converts the characters of a string to upper case.
    */
   static final int FUNCTION_DT_UPPER = 8; // rnovais@570_1

   /**
    * <code>LOWER</code> function. It converts the characters of a string to lower case.
    */
   static final int FUNCTION_DT_LOWER = 9; // rnovais@570_1

   // juliana@250_8: now the maximum number of columns, fields, tables, etc is 254 instead of 128 except on palm.
   // Maximum constants.
   /**
    * Maximum number of columns supported in a column list clause.
    */
   static int MAX_NUM_COLUMNS = 254; // guich@561_1

   /**
    * Maximum number of parameters supported.
    */
   static int MAX_NUM_PARAMS = 254; // guich@561_1

   // Available operand / operator types.
   /**
    * No operand / operator.
    */
   static final int OP_NONE = 0;

   /**
    * Boolean operator <code>AND</code>.
    */
   static final int OP_BOOLEAN_AND = 1;

   /**
    * Boolean operator <code>OR</code>.
    */
   static final int OP_BOOLEAN_OR = 2;

   /**
    * Boolean operator <code>NOT</code>.
    */
   static final int OP_BOOLEAN_NOT = 3;
   
   // juliana@253_9: improved Litebase parser.
   /**
    * Relational operator <code><</code>.
    */
   static final int OP_REL_LESS = LitebaseParser.TK_LESS;
   
   /**
    * Relational operator <code>=</code>.
    */
   static final int OP_REL_EQUAL = LitebaseParser.TK_EQUAL;
   
   /**
    * Relational operator <code>></code>.
    */
   static final int OP_REL_GREATER = LitebaseParser.TK_GREATER;

   /**
    * Relational operator <code>>=</code>.
    */
   static final int OP_REL_GREATER_EQUAL = LitebaseParser.TK_GREATER_EQUAL;

   /**
    * Relational operator <code><=</code>.
    */
   static final int OP_REL_LESS_EQUAL = LitebaseParser.TK_LESS_EQUAL;
   
   /**
    * Relational operator <code>!=</code> or <code><></code>.
    */
   static final int OP_REL_DIFF = LitebaseParser.TK_DIFF;

   /**
    * The operand is an identifier.
    */
   static final int OP_IDENTIFIER = 10;

   /**
    * String operator <code>LIKE</code>.
    */
   static final int OP_PAT_MATCH_LIKE = 11;

   /**
    * String operator <code>NOT LIKE</code>.
    */
   static final int OP_PAT_MATCH_NOT_LIKE = 12;

   /**
    * Operator <code>IS</code>.
    */
   static final int OP_PAT_IS = 13;

   /**
    * Operator <code>IS NOT</code>.
    */
   static final int OP_PAT_IS_NOT = 14;

   /**
    * Operand <code>NULL</code>.
    */
   static final int OP_PAT_NULL = 15;

   /**
    * <code>hashCode("rowid");</code>
    */
   static final int hcRowId = 108705909;

   /**
    * The maximum length of the name of a table when is created as a plain file.
    */
   static final int MAX_TABLE_NAME_LENGTH_AS_PLAIN_FILE = 23;

   // Aggregation functions table.
   /**
    * Array containing aggregation function names.
    */
   static final String[] aggregateFunctionsNames = {"count", "max", "min", "avg", "sum" };

   // juliana@226_5
   /**
    * Array containing aggregation function types.
    */
   static final byte[] aggregateFunctionsTypes = {INT, UNDEFINED, UNDEFINED, DOUBLE, DOUBLE};

   // rnovais@_568_10 @_570_1
   // Data type functions table.
   /**
    * Array containing data type function names.
    */
   static final String[] dataTypeFunctionsNames = {"year", "month", "day", "hour", "minute", "second", "millis", "abs", "upper", "lower"};

   /**
    * Array containing data type function types.
    */
   static final byte[] dataTypeFunctionsTypes = {SHORT, SHORT, SHORT, SHORT, SHORT, SHORT, SHORT, UNDEFINED, CHARS, CHARS};

   // rnovais@_568_10
   /**
    * Functions X data type. Each row corresponds to one data type: <code>CHARS</code>, <code>SHORT</code>,
    * <code>INT</code>, <code>LONG</code>, <code>FLOAT</code>, <code>DOUBLE</code>, <code>CHARS_NOCASE</code>,
    * <code>BOOLEAN</code>, <code>DATE</code>, and <code>DATETIME</code>. The order can't be changed and it contains the
    * function codes supported by the data types.
    */
   static final byte[][] function_x_datatype = {
         {FUNCTION_DT_UPPER, FUNCTION_DT_LOWER}, // rnovais@_570_1
         {FUNCTION_DT_ABS}, // rnovais@_570_5
         {FUNCTION_DT_ABS}, // rnovais@_570_1
         {FUNCTION_DT_ABS}, // rnovais@_570_5
         {FUNCTION_DT_ABS}, // rnovais@_570_5
         {FUNCTION_DT_ABS}, // rnovais@_570_5
         {FUNCTION_DT_UPPER, FUNCTION_DT_LOWER}, // rnovais@_570_1
         {}, 
         {FUNCTION_DT_YEAR, FUNCTION_DT_MONTH, FUNCTION_DT_DAY},
         {FUNCTION_DT_YEAR, FUNCTION_DT_MONTH, FUNCTION_DT_DAY, FUNCTION_DT_HOUR, FUNCTION_DT_MINUTE, FUNCTION_DT_SECOND, FUNCTION_DT_MILLIS}};
}
