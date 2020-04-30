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
 * Declares all the types used by Litebase.
 */

#ifndef LITEBASE_TYPES_H
#define LITEBASE_TYPES_H

#include "tcvm.h"
#include "Constants.h"
#include "Macros.h"

#if defined(linux)
#include <stdint.h>
#endif

// Buffers for string buffers for converting to string of known types.
/**
 * Buffer for a date type of the form YYYY/MM/DD.
 */
typedef char DateBuf[11];

/**
 * Buffer for a datetime type of the form YYYY/MM/DD HH:MM:SS:MMM XM.
 */
typedef char DateTimeBuf[27];

/**
 * Function definition to list PDBs.
 */
typedef void (*tiPDBF_listPDBs_iiFunc)(NMParams p);

/**
 * A list of objects used to hold prepared statements that uses a specific table.
 */
TC_DeclareList(TCObject); 

// Typedefs for using Litebase file.
typedef struct XFile XFile;
typedef struct Key Key;
typedef void (*setPosFunc)(XFile* xFile, int32 position);
typedef bool (*growToFunc)(Context context, XFile* xFile, uint32 newSize);
typedef bool (*readBytesFunc)(Context context, XFile* xFile, uint8* buffer, int32 count);
typedef bool (*writeBytesFunc)(Context context, XFile* xFile, uint8* buffer, int32 count);
typedef bool (*closeFunc)(Context context, XFile* xFile);

// Typedefs for the structures used by Litebase.
// juliana@parser_1: improved Litebase parser.
typedef struct SQLValue SQLValue;
typedef struct SQLSelectClause SQLSelectClause;
typedef struct SQLColumnListClause SQLColumnListClause;
typedef struct LitebaseParser LitebaseParser;
typedef struct SQLBooleanClause SQLBooleanClause;
typedef struct SQLBooleanClauseTree SQLBooleanClauseTree;
typedef struct SQLDeleteStatement SQLDeleteStatement;
typedef struct SQLFieldDefinition SQLFieldDefinition;
typedef struct SQLInsertStatement SQLInsertStatement;
typedef struct SQLResultSetField SQLResultSetField;
typedef struct SQLResultSetTable SQLResultSetTable;
typedef struct SQLSelectStatement SQLSelectStatement;
typedef struct SQLUpdateStatement SQLUpdateStatement;
typedef struct PlainDB PlainDB;
typedef struct Table Table;
typedef struct IntVector IntVector;
typedef struct ShortVector ShortVector;
typedef struct ResultSet ResultSet;
typedef struct Node Node;
typedef struct MarkBits MarkBits;
typedef struct Index Index;
typedef struct ComposedIndex ComposedIndex;
typedef struct FirstLast FirstLast;
typedef struct MemoryUsageEntry MemoryUsageEntry;
typedef struct MemoryUsageHT MemoryUsageHT;
typedef struct StringArray StringArray; // juliana@227_20

/**
 * A generic file structure, which can be used for normal and memory files.
 */
struct XFile
{
	/** 
	 * Normal file: codewarrior does not like annonimous unions.
	 */
   NATIVE_FILE file;

	/** 
	 * A cache for the file so that the bytes do not needed to be loaded all the time.
	 */
	uint8* cache;
	
	/**
	 * Memory file buffer.
	 */
	uint8* fbuf;

	/**
	 * The current cache position.
	 */
	int32 cachePos;

	/**
	 * The initial position of the file into the cache.
	 */
	int32 cacheIni;

	/** 
	 * The final position of the file into the cache.
	 */
	int32 cacheEnd;

	/**
	 * The cache size.
	 */
	int32 cacheInitialSize; 

	/**
	 * Indicates the first position of the cache that is dirty.
	 */
	int32 cacheDirtyIni;

	/**
	 * Indicates the last position of the cache that is dirty.
	 */
	int32 cacheDirtyEnd;
	
	/**
	 * The file size.
	 */
   uint32 size;

	/**
	 * The current file position.
	 */ 
	int32 position;

	/**
	 * The last position of the file used.
	 */
   int32 finalPos;

	/**
	 * The file name, which is empty for a memory file.
	 */
   char name[DBNAME_SIZE]; 

   /** 
	 * Indicates if the cache is dirty and its contents needs to be saved later on.
	 */
	uint8 cacheIsDirty;

   // juliana@227_3: improved table files flush dealing.
   /**
    * Indicates if the cache file should not be flushed.
    */
   uint8 dontFlush;
   
   /**
    * Indicates if the table uses cryptography.
    */
   uint8 useCrypto; // juliana@crypto_1: now Litebase supports weak cryptography.

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
#if defined(POSIX) || defined(ANDROID)
   /**
    * The file full path;
    */
   char fullPath[MAX_PATHNAME + 1];

   /**
    * The timestamp of the last time the file was used.
    */ 
   int32 timeStamp;
#endif
};

#if defined(POSIX) || defined(ANDROID)
typedef struct XFilesList XFilesList;

/**
 * Pointer to a list of currently opened Litebase files.
 */
struct XFilesList
{
   /**
    * An array with the used to store the list of Litebase files.
    */
   XFile* list[MAX_OPEN_FILES];

   /**
    * The number of positions used.
    */
   int32 count;
};
#endif

// juliana@noidr_1: removed .idr files from all indices and changed its format.
/** 
 * This structure represents the key of a record. It may be any of the SQL types defined here.
 */
struct Key
{
	/**
    * The record index or NO_VALUE.
    */
	int32 record;

	/**
    * The values stored in the key.
    */
   SQLValue* keys;

	/**
    * The index that has this key.
    */
   Index* index;
};

/**
 * Represents a value which can be inserted in a column of a table.
 */
struct SQLValue
{
   /**
    * Represents the <code>CHARS</code>, <code>VARCHAR</code>, <code>CHARS NOCASE</code>, and <code>VARCHAR NOCASE</code> data types.
    */
   JCharP asChars;

	/**
    * Represents the <code>BLOB</code> data type.
    */
	uint8* asBlob; // juliana@210_5: removed memory leaks concerning blobs.
   
	struct
   {
      uint32 length:31;
      uint32 isNull: 1;
   };
   union
   {
		/**
		 * Represents the <code>SHORT</code> data type.
		 */
      int16 asShort;

      /**
		 * Represents the <code>INT</code> data type.
       */
      int32 asInt;

		/**
		 * Represents the <code>LONG<code> data type.
       */
      int64 asLong;

		/**
		 * Represents the <code>FLOAT</code> data type.
       */
      float asFloat;

		/**
		 * Represents the <code>DOUBLE</code> data type.
       */
      double asDouble;

      struct // type DATETIME.
      {
			/**
			 * Represents the date part.
			 */
         int32 asDate;

			/**
			 * Represents the time part.
			 */ 
         int32 asTime;
      };
   };
};

struct SQLSelectClause
{
	/**
    * Number of fields found.
    */
   uint8 fieldsCount;

   /**
    * Indicates if the select clause has data type functions.
    */
   uint8 hasDTFunctions; // rnovais@568_10

	/**
    * Indicates if the select clause has aggregated functions.
    */
   uint8 hasAggFunctions;

	/**
    * Indicates if the select clause has real columns.
    */
   uint8 hasRealColumns;

	/**
    * Indicates if the select clause field list was built from a wildcard.
    */
   uint8 hasWildcard;

	/** 
    * Indicates the type of the select clause.
    * This will be better used when we implement a PLANNER for litebase. Up to now this only indicates if the select clause has a count(*).
	 */
   uint8 type;
   
	/**
	 * Indicates if the select clause comes from a prepared select statement.
	 */
	uint8 isPrepared;

	/**
	 * The number of tables in the table list.
	 */
	uint8 tableListSize;

   /**
    * The select sql query hash code for the memory usage hash table.
    */
   int32 sqlHashCode;

	/**
    * The resulting <code>ResultSet</code> table list.
    */
   SQLResultSetTable** tableList; 

	/**
    * The resulting <code>ResultSet</code> field list.
    */
   SQLResultSetField** fieldList;

	/**
    * The index of the fields.
    */
   Hashtable htName2index; 

   /**
	 * The heap used by the parser is stored here to allocate the structures for the select statement.
	 */
   Heap heap;
};

/**
 * This structure represents a SQL column list clause, like order by or group by. <BR>
 * <i>Note:</i> The select clause has a different structure <code>SQLSelectClause</code>, since it has a different complexity.
 */
struct SQLColumnListClause
{
   /**
    * Indicates that the index to be used is composed or not.
    */
   uint8 isComposed; // juliana@230_29: order by and group by now use indices on simple queries.

   /**
    * Indicates the index to use when doing a sort operation.
    */
   int16 index; // juliana@230_29: order by and group by now use indices on simple queries.

   /**
    * Number of fields. 
    */
   int32 fieldsCount;

   /** 
    * The column field list. 
    */
   SQLResultSetField** fieldList;

   /**
    * Backup for the tableColIndexes, used in prepared statements. 
    */
   uint8* fieldTableColIndexesBak; // guich@554_37
};

/* The type which returns the result of the parser */
struct LitebaseParser
{
	/**
    * The type of SQL command, which can be one of: <b><code>CMD_CREATE_TABLE</b></code>, <b><code>CMD_CREATE_INDEX</b></code>, 
    * <b><code>CMD_DROP_TABLE</b></code>, <b><code>CMD_DROP_INDEX</b></code>, <b><code>CMD_ALTER_DROP_PK</b></code>, 
    * <b><code>CMD_ALTER_ADD_PK</b></code>, <b><code>CMD_ALTER_RENAME_TABLE</b></code>, <b><code>CMD_ALTER_RENAME_COLUMN</b></code>, 
    * <b><code>CMD_SELECT</b></code>, <b><code>CMD_INSERT</b></code>, <b><code>CMD_UPDATE</b></code>, or <b><code>CMD_DELETE</b></code>.
    */
	uint8	command;
	
	/** 
    * Counts the number of simple primary keys, which must be only one.
    */
	uint8 numberPK;

	/**
    * The number of fields in the field list.
    */
	uint8 fieldListSize;

	/**
    * The number of fields of values in the field values list.
    */
	uint8 fieldValuesSize;

	/**
    * The number of fields of the update field list.
    */
	uint8 fieldNamesSize;

	/**
    * This is used to differ between a where clause and a having clause. Before parsing the having clause, <code>isWhereClause</code> is set to 
    * false. So the <code>getInstanceBooleanClause()</code> method will return a having clause, otherwise it returns a where clause.
    */
   uint8 isWhereClause;

	/**
    * The last position of the buffer read.
    */
	uint16 yyposition; 

	/**
	 * The length of the sql string to be parsed.
	 */
   uint16 length;
	
	/**
	 * The name of a token.
	 */
	VoidP yylval; // juliana@parser_1: improved Litebase parser.
	
	// juliana@parser_1: improved Litebase parser.
	/** 
    * The first table name found in an update statement.
    */
   CharP firstFieldUpdateTableName;

   /** 
    * The first table alias found in an update statement.
    */
   CharP firstFieldUpdateAlias;

   /** 
    * The second table name found in an update statement, which indicates an error.
    */
   CharP secondFieldUpdateTableName;

   /** 
    * The second table alias found in an update statement, which indicates an error.
    */
   CharP secondFieldUpdateAlias;
	
	/**
    * The last char read.
    */
	JChar yycurrent;
	
	/**
    * An auxiliary expression tree.
    */
	SQLBooleanClauseTree* auxTree;
	
	/**
    * An auxiliary field.
    */
	SQLResultSetField* auxField;

   /**
    * Contains field values (strings) used on insert/update statements.
    */
   JCharP fieldValues[MAXIMUMS];

	/**
    * The field list for inserts, updates and indices.
    */
	CharP fieldNames[MAXIMUMS];

	/**
    * The resulting set table list, used with all statements.
    */
   SQLResultSetTable* tableList[MAXIMUMS];
	
   /**
    * The field list for the SQL commands except <code>SELECT</code>. 
    */
   SQLFieldDefinition* fieldList[MAXIMUMS];

	/**
	 * The field list for selects.
	 */
	SQLResultSetField* selectFieldList[MAXIMUMS];
   
	/**
    * The where clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause* whereClause;

	/**
    * The having clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause* havingClause;
   
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
	 * A pre-allocated field list for order by. 
	 */
	SQLResultSetField* orderByfieldList[MAXIMUMS];
   
	/**
	 * A pre-allocated field list for group by. 
	 */
	SQLResultSetField* groupByfieldList[MAXIMUMS];

	/**
    * A list of all fields referenced in the where boolean clause.
    */
   SQLResultSetField* whereFieldList[MAXIMUMS];

	/**
    * The list of trees that contains the paramameter list of the where boolean clause.
    */
   SQLBooleanClauseTree* whereParamList[MAXIMUMS];

	/**
    * A list of all fields referenced in the having boolean clause.
    */
   SQLResultSetField* havingFieldList[MAXIMUMS];

	/**
    * The list of trees that contains the paramameter list of the having boolean clause.
    */
   SQLBooleanClauseTree* havingParamList[MAXIMUMS];

   /**
    * A hashtable to be used on select statements to verify if it has repeated table names.
    */
	Hashtable tables;

	/**
    * The input device.
    */
	JCharP zzReaderChars; 

	/**
	 * The heap to allocate the parser structure.
	 */
   Heap heap;

	/**
	 * The thread context in order to throw exceptions. 
	 */ 
   Context context;
};

struct SQLBooleanClause
{
	/**
    * The number of fields.
    */
   uint8 fieldsCount;

	/**
    * The length of the parameter list.
    */
   uint8 paramCount;

	/**
    * The number of indexes to be applied.
    */
   uint8 appliedIndexesCount;

	/**
    * The boolean operator to be used to combine the result set of each index. Can be either <code>SQLElement.OP_BOOLEAN_AND</code>, 
    * <code>SQLElement.OP_BOOLEAN_OR</code>, or <code>SQLElement.OP_BOOLEAN_NONE</code> (in case only one index was used).
    */
   uint8 appliedIndexesBooleanOp;

	/**
    * Indicates if it is a where clause or a having clause.
    */
   uint8 isWhereClause;

	/**
    * Indicates if the result set will be indexed.
    */
   int8 appliedIndexRs;

	/**
    * Type of the where clause: <code><B>AND</B><code> of different result sets = 0, and <code><B>OR</B></code> of different result sets = 1.
    */
   uint8 type;

	/**
    * Resulting boolean clause expression tree.
    */
   SQLBooleanClauseTree* expressionTree;

	/**
	 * The original boolean clause expression tree. This is necessary when freeing the tree.
	 */
   SQLBooleanClauseTree* origExpressionTree;

	/**
    * The associated result set.
    */
   ResultSet* resultSet;

	/**
    * Table that maps the field name to an index in the field list.
    */
   Hashtable fieldName2Index;

   /**
    * A backup of the expression tree.
    */
   SQLBooleanClauseTree* expressionTreeBak;

   /**
    * A list of all fields referenced in the boolean clause.
    */
   SQLResultSetField** fieldList;

	/**
    * The list of trees that contains the parameter list of the boolean clause.
    */
   SQLBooleanClauseTree** paramList;

	/**
    * The constant values to be used by the indexes that were applied to the boolean clause.
    */
   SQLBooleanClauseTree* appliedIndexesValueTree[MAX_NUM_INDEXES_APPLIED];

	/**
    * The tables of the correspondent indexes.
    */
   Table* appliedIndexesTables[MAX_NUM_INDEXES_APPLIED];

	/**
    * The composed indices applied.
    */
   ComposedIndex* appliedComposedIndexes[MAX_NUM_INDEXES_APPLIED];

	/**
    * The columns whose indexes were applied to the boolean clause. A column can be listed more than once, in case it is listed more than once in the 
    * boolean clause.
    */
   uint8 appliedIndexesCols[MAX_NUM_INDEXES_APPLIED];

	/**
    * The relational operators to be used by the indexes that were applied to the boolean clause.
    */
   uint8 appliedIndexesRelOps[MAX_NUM_INDEXES_APPLIED];
};

struct SQLBooleanClauseTree
{
   /**
    * Tree operand type.
    */
   uint8 operandType;

   /**
    * Indicate if the value type is a floating point type.
    */
   uint8 isFloatingPointType;

	/**
    * The associated table column index of the operand.
    */
   uint8 colIndex;

	/**
    * Indicates if this is a parameter.
    */
   uint8 isParameter;

	/**
    * Indicates if the parameter value is defined.
    */
   uint8 isParamValueDefined;

	/**
    * Pattern matching type.
    */
   uint8 patternMatchType;

	/**
    * Position of the % in the string.
    */
   uint8 posPercent; // rnovais@568_1

   /**
    * Indicates if the left and right tree are identifiers.
    */
   uint8 bothAreIdentifier; 

	/**
    * Indicates if it has an associated index. Used on join <code>table1.field1 = table2.field2</code>.
    */
   uint8 hasIndex;

   /**
    * The length of <code>strToMatch</code>
    */ 
   uint8 lenToMatch;

	/**
    * The index of the correspondent result set.
    */
   int8 indexRs; 

	/**
    * The value data type.
    */
   int8 valueType;

	/**
    * The operand name hash code.
    */
   int32 nameHashCode;

	/**
    * The operand name hash code used only for sql functions.
    */
   int32 nameSqlFunctionHashCode; // rnovais@570_108

	// Subtrees
	/**
    * The left tree.
    */
   SQLBooleanClauseTree* leftTree;

	/**
    * The right tree.
    */
   SQLBooleanClauseTree* rightTree;

	/**
    * The parent tree.
    */
   SQLBooleanClauseTree* parent;

	/**
    * The associated SQLBooleanClause.
    */
   SQLBooleanClause* booleanClause;

	/**
    * Tree operand name.
    */
   CharP operandName;

	/**
    * String to do the pattern match.
    */
	JCharP strToMatch;

   /**
    * Tree operand value.
    */
   SQLValue operandValue;

   /**
    * The current value. Used only on joins comparing table fields like <code>table1.field1 <operator> table2.field2</code>.
    */
   SQLValue valueJoin; 
};

/**
 * Represents a SQL <code>DELETE</code> statement.
 */
struct SQLDeleteStatement
{
   /**
    * The statement type, which indicates that this is a DELETE statement.
    */
   uint8 type;

   /**
    * The where clause of the delete statement.
    */
   SQLBooleanClause* whereClause;

   /**
    * The structure with the table of the delete statement.
    */
   SQLResultSetTable* rsTable;

   /** 
    * The heap to allocate memory for the delete statement.
    */
   Heap heap;
};

/**
 * Represents a field of a statement except for SELECTs.
 */
struct SQLFieldDefinition
{
   /**
    * The name of the field.
    */
   CharP fieldName;

   /**
    * The default value of a field can contain a string, a number or a Date/Datetime value. This must be converted to the correct type later. If 
    * <code>defaultValue</code> is <code>null</code>, the default value was not defined.
    */
   JCharP defaultValue;

   /**
    * Only used for chars / chars no case / blob types. For other types it is equal to zero.
    */
	int32 fieldSize;

   /**
    * The type of the field. It can be: <B><code>NUMBER</code></B>, <B><code>UNDEFINED</code></B>, <B><code>CHARS</code></B>, 
    * <B><code>SHORT</code></B>, <B><code>INT</code></B>, <B><code>LONG</code></B>, <B><code>FLOAT</code></B>, <B><code>DOUBLE</code></B>, 
    * <B><code>CHARS_NOCASE</code></B>, <B><code>BOOLEAN</code></B>, <B><code>DATE</code></B>, or <B><code>DATE_TIME</code></B>.
    */
   uint8 fieldType;

   /**
    * Defines if the field can be null or not.
    */
	uint8 isNotNull;

   /**
    * Indicates if the field is the primary key.
    */
   uint8 isPrimaryKey;
};

/** 
 * Represents a SQL <code>INSERT</code> statement.
 */
struct SQLInsertStatement
{
   /**
    * The statement type, which indicates that this is a INSERT statement.
    */
   uint8 type;

   /**
    * The number of values to be inserted.
    */
   uint8 nFields;

   /**
    * The number of the parameters if the insert statement is a preprared statement.
    */
   uint8 paramCount;

   /**
    * The array with the indexes of the parameters.
    */
   uint8* paramIndexes;

   /**
    * An array that indicates if a parameters is defined or not.
    */
   uint8* paramDefined;

   /**
    * An array that indicates if a null value will be stored in a field.
    */
   uint8* storeNulls; 

   /**
    * The table name.
    */
   CharP tableName;

   /** 
    * The base table used by the SQL expression. 
    */
	Table* table;

   /**
    * The fields used if the insert statement is not using the default order.
    */
   CharP* fields;

   /**
    * The record to be inserted.
    */
   SQLValue** record;

   /** 
    * The heap to allocate memory for the insert statement.
    */
   Heap heap;
};

/**
 * Represents a field of a <code>ResultSet</code>.
 */
struct SQLResultSetField
{
   /**
    * Indicates what resultset it belongs.
    */
   uint8 indexRs;

   /**
    * Indicates if this is a wildcard field.
    */
   uint8 isWildcard;

   /**
    * Indicates if the field represents a virtual column (not mapped directly to the underlying table).
    */
   uint8 isVirtual;

   /**
    * Indicates if the function is an aggregated function.
    */
   uint8 isAggregatedFunction;

   /**
    * Indicates if the result is to be shown in ascending or decreasing order for fields from order by clause.
    */
   uint8 isAscending;

   /**
    * Indicates if the function is a data type function.
    */
   uint8 isDataTypeFunction;

   /**
    * The index of the column that this field represents in the underlying table. For virtual fields, this value equals -1.
    */
   uint8 tableColIndex; // juliana@227_1: solved a problem with selecting all the columns of a 128-column table.

   /**
    * Indicates that the index to be used is composed or not.
    */
   uint8 isComposed; // juliana@230_21: MAX() and MIN() now use indices on simple queries.

   /**
    * The sql function that this field represents.
    */
   int8 sqlFunction;

   /**
    * The data type.
    */
   int8 dataType;
   
   /**
    * Indicates the index to use when doing a max() or min() operation.
    */
   int8 index; // juliana@230_21: MAX() and MIN() now use indices on simple queries.

   /** 
    * The column name hash code. 
    */
   int32 tableColHashCode;

   /**
    * The field alias hash code.
    */
   int32 aliasHashCode;

   /**
    * The size of the field; only used in chars types and blob.
    */
   int32 size;

   /**
    * Indicates the table name it belongs. The parser sets its value. e. g.<br>
    * <code>select person.age from test</code>: tableName = person.
    */
   CharP tableName;

   /**
    * The field alias.
    */
   CharP alias;

   /**
    * The name of the column that this field represents in the underlying table. For virtual fields, this value equals <code>null</code>.
    */
   CharP tableColName;

   /**
    * The parameter of the function.<br>
    * <i>Note:</i> It is declared as <code>ResultSetField</code> to allow nested function calls in the future.
    */
   SQLResultSetField* parameter;

   /**
    * Indicates what table it belongs.
    */
   Table* table;
};

/**
 * Represents a table of various statements, except for inserts.
 */
struct SQLResultSetTable
{
   /**
    * The object table, filled when binding the statement.
    */
   Table* table;

   /**
    * The name of the table, filled during the parsing process.
    */
   CharP tableName;

   /**
    * The Table alias.
    */
   CharP aliasTableName;

   /**
    * The alias table name hash code.
    */
   int32 aliasTableNameHashCode;
};

/**
 * Represents a SQL <code>SELECT</code> statement.
 */
struct SQLSelectStatement
{
   /**
    * The statement type, which indicates that this is a SELECT statement.
    */
   uint8 type;

   /**
    * The select clause of the statement.
    */
   SQLSelectClause* selectClause;

   /**
    * The group by clause of the statement.
    */
   SQLColumnListClause* groupByClause;

   /**
    * The order by clause of the statement.
    */
   SQLColumnListClause* orderByClause;

   /**
    * The where clause of the statement.
    */
   SQLBooleanClause* whereClause;

   /**
    * The having clause of the statement.
    */
   SQLBooleanClause* havingClause;
};

/**
 * Represents a SQL <code>UPDATE</code> statement.
 */
struct SQLUpdateStatement
{
   /**
    * The statement type, which indicates that this is an UPDATE statement.
    */
   uint8 type;

   /**
    * The number of values to be updated.
    */
   uint8 nValues;

   /**
    * The number of the parameters if the update statement is a preprared statement.
    */
   uint8 paramCount;

   /**
    * The array with the indexes of the parameters.
    */
   uint8* paramIndexes;

   /**
    * An array that indicates if a parameters is defined or not.
    */
   uint8* paramDefined;

   /**
    * An array that indicates if a null value will be stored in a field.
    */
   uint8* storeNulls; 

   /** 
    * The base table used by the SQL expression. 
    */
   SQLResultSetTable* rsTable;

   /**
    * The where clause.
    */
   SQLBooleanClause* whereClause;

   /**
    * The fields used to update a record.
    */
   CharP* fields;

   /**
    * The record to be inserted.
    */
   SQLValue** record;

   /** 
    * The heap to allocate memory for the update statement.
    */
   Heap heap;
};

 /**
 * Stores the table files and some variables concerning them.
 */
struct PlainDB
{
   /**
    * Indicates if a table was not correctly closed when was oppened for the last time.
    */
   uint8 wasNotSavedCorrectly;

   /**
    * Indicates whether a table used the wrong cryptography format.
    */
   uint8 useOldCrypto;

   /**
    * Indicates if the tables of this connection use ascii or unicode strings.
    */
	uint8 isAscii; // juliana@210_2: now Litebase supports tables with ascii strings.
   
   /**
    * The size of a row.
    */
   uint16 rowSize;

   /**
    * The table header size.
    */
   uint16 headerSize;

   /**
    * The number of rows.
    */
   int32 rowCount;

   /**
    * The current row increment when inserting data on the table.
    */
   int32 rowInc;

   /**
    * The number of rows available.
    */
   int32 rowAvail; // rnovais@112_2
   
   /**
    * A buffer to read a row.
    */
   uint8* basbuf;

   /**
    * The table name. It is empty when the table is temporary.
    */
   char name[DBNAME_SIZE];

   /**
    * The database (.db) file.
    */
   XFile db;

   /**
    * The strings and blobs (.dbo) file.
    */
   XFile dbo;

   /**
    * The pointer to a function to set the position of the record in the files.
    */
	setPosFunc setPos;

   /**
    * The pointer to a function to grow the files.
    */ 
   growToFunc growTo;

   /**
    * The pointer to a function to read bytes from the file.
    */ 
   readBytesFunc readBytes;

   /** 
    * The pointer to a function to write bytes to the file.
    */
   writeBytesFunc writeBytes;

   /** 
    * The pointer to a function to close the file.
    */
   closeFunc close;
};

/**
 * A growable int array.
 */
struct IntVector 
{
   /**
    * The array itself.
    */
   int32* items;

   /**
    * Allocated length of the array.
    */
   int16 length; 

   /**
    * Current number of items count.
    */
   int16 size;

   /**
    * A heap to store the array.
    */
   Heap heap;
} ;

/**
 * The table structure.
 */
struct Table
{
   /**
    * The number of columns of this table.
    */
   uint8 columnCount;

   /**
    * Number of composed primary key columns.
    */
   uint8 numberComposedPKCols;

   /**
    * Number of composed indices.
    */
   uint8 numberComposedIndexes;

   /**
    * Indicates that a table has been modified and must be marked as not closed properly after opened and before closed.
    */
   uint8 isModified; // juliana@226_4

   /**
    * The table version.
    */
   uint8 version; // juliana@230_12

   /**
    * Indicates if the table was updated after the last time it was opened.
    */
   uint8 wasUpdated; // juliana@270_27: now purge will also really purge the table if it only suffers updates.

   /**
    * The primary key column.
    */
   int8 primaryKeyCol; // juliana@114_9

   /**
    * The index of the composed primary key.
    */
   int8 composedPK;

   /**
    * Used to order the tables.
    */
   int16 weight; 

   /**
    * The counter of the current <code>rowid</code>. The <code>rowid</code> is continuously incremented so that two elements will never have the same
    * one, even if elements are deleted. <p>The record attributes are stored in the first two bits of the <code>rowid</code>.
    */
   int32 currentRowId;

   /**
    * The attributes of the row.
    */
   int32 auxRowId; // rnovais@570_61

   /**
    * The number of deleted rows, which is always logical.
    */
   int32 deletedRowsCount; 

   /**
    * Used to return the number of rows that a select without a where clause returned.
    */
   int32 answerCount; // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.

   /**
    * The maximum length of the bit map representing all table rows.
    */
   int32 allRowsBitmapLength; // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.

   /**
    * The column attributes.
    */
   uint8* columnAttrs;

   /**
    * Just for the case when the column has a default value but the user explicited the insert or update of a null.
    */
   uint8* storeNulls;

   /**
    * Contains the null values.
    */
   uint8* columnNulls; 

   /**
    * The composed primary key columns.
    */
   uint8* composedPrimaryKeyCols; 

   /**
    * A map with rows that satisfy totally the query WHERE clause.
    */
   uint8* allRowsBitmap; // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.

   /**
    * Column offsets within the record.
    */
   uint16* columnOffsets;

   /**
    * Column types (<code>SHORT</code>, <code>INT</code>, <code>LONG</code>, <code>FLOAT</code>, <code>DOUBLE</code>, <code>CHARS</code>, 
    * CHARS_NOCASE</code>)
    */
   int8* columnTypes; 

   /**
    * The hashes of the column names.
    */
   int32* columnHashes;

   /**
    * Column sizes (only used for CHAR and BLOB types).
    */
   int32* columnSizes;

   /**
    * The full name of the table.
    */
   char name[DBNAME_SIZE];

   /**
    * The path where the table is stored if it is stored in disk.
    */
	TCHARP sourcePath;

   /**
    * The column names. If <code>null</code>, the column names are not available because it is a temporary table.
    */
   CharP* columnNames;
   
   /**
    * Existing column indices for each column, or <code>null</code> if the column has no index.
    */
   Index** columnIndexes;
   
   /**
    * The corresponding files of the table.
    */
   PlainDB db;

   /**
    * Given a column name, returns its index for this table. <code>rowid</code>, a special column, is always column 0.
    */
   Hashtable htName2index;
   
   /**
    * Contains the default values for the columns.
    */
   SQLValue** defaultValues;
   
   /**
    * An array of nodes indices.
    */
   int32* nodes; // juliana@noidr_2: the maximum number of keys of a index was duplicated.

   /**
    * Existing composed column indices for each column, or <code>null</code> if the table has no composed index.
    */
   ComposedIndex* composedIndexes[MAX_NUM_INDEXES_APPLIED]; 
   
   /**
    * A linked list of prepared statements that use this table.
    */
   TCObjects* preparedStmts;

   /**
    * A heap to allocate the table structure.
    */
   Heap heap;
};

/**
 * Represents a set or rows resulting from a <code>LitebaseConnection.executeQuery()</code> method call.
 */
struct ResultSet
{
   /** 
    * Indicates if it is a select of the form <code>select * from table</code> or not. 
    */
   uint8 isSimpleSelect; // juliana@210_1: select * from table_name does not create a temporary table anymore.
   
   /**
    * Indicates if the select table is temporary or not.
    */
   uint8 isTempTable; // juliana@223_14: solved possible memory problems.
   
   /** 
    * Counts the number of indices when running <code>generateIndexedRowsMap()</code>. 
    */
   uint8 indexCount; 

   /**
    * When <code>rowsBitmap</code> is generated, indicates what is the boolean relationship between the rows marked in the bitmap and any remaining 
    * WHERE clause.
    */
   uint8 rowsBitmapBoolOp;

   // juliana@223_13: corrected a bug that could break the application when freeing a result set of a prepared statement.
   /**
    * Indicates that this <code>ResultSet</code> was generated by a select prepared statement.
    */
   uint8 isPrepared;

   /** 
    * The index of the correspodent result set. 
    */
   int8 indexRs; 

   /** 
    * The number of columns in this result set. 
    */
   uint16 columnCount;

   /** 
    * Current record position being read. 
    */
   int32 pos;

   /**
    * The number of valid records of this result set.
    */
   int32 answerCount; // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.

   /**
    * An array with the number of decimal places that is used to format <code>float</code> and <code>double</code> values, when being retrieved using 
    * the <code>getString()</code> method. This can be set at runtime by the user, and it is -1 as default.
    */
   int8* decimalPlaces;

   /**
    * A map with rows that satisfy totally the query WHERE clause.
    */
   uint8* allRowsBitmap; // juliana@230_14: removed temporary tables when there is no join, group by, order by, and aggregation.

   /** 
    * The associated table for the result set. 
    */
   Table* table; 

   /**
    * A map with rows that satisfy totally or partially the query WHERE clause; generated using the table indices.
    */
   IntVector rowsBitmap;

   /** 
    * The indices used in this result set. 
    */
   IntVector indexes;

   /**
    * An auxiliary map with rows that satisfy totally or partially the query WHERE clause; generated from the table indices.
    */
   IntVector auxRowsBitmap;

   /** 
    * Contains the hash of the all possible colunm names in the select statement. 
    */
   Hashtable intHashtable;

   /** 
    * The WHERE clause associated with the result set. 
    */
   SQLBooleanClause* whereClause;

   /**
    * The select clause of the sql that generated this result set.
    */
   SQLSelectClause* selectClause;
   
   /**
    * Generates the result set indexed rows map from the associated table indexes applied to the associated WHERE clause.
    */
   MarkBits* markBits;

   /**
    * A heap to allocate the result set structure.
    */
   Heap heap;

   /**
    * The connection with Litebase.
    */
   TCObject driver;
};

/**
 * This is the implementation of a B-Tree.
 */
struct Node // for B-tree
{
   /**
    * Indicates if a node is dirty.
    */
   uint8 isDirty;

   /**
    * The index of a node in the B-Tree.
    */
   uint16 idx;

   /**
    * The size of the node.
    */
   uint16 size;
   
   // juliana@noidr_2: the maximum number of keys of a index was duplicated.
   /**
    * This children nodes.
    */
   uint16* children; // Each array has one extra component, to allow for possible overflow.
   
   /**
    * The index of this node.
    */
   Index* index;

   /**
    * The keys that this node stores.
    */
   Key* keys;
};

/**
 * Generates the result set indexed rows map from the associated table indexes applied to the associated WHERE clause.
 */
struct MarkBits
{
   /**
    * Indicates if a value is equal or not.
    */
   uint8 isNoLongerEqual;

   /**
    * The value of a bit of the bitmap.
    */
   uint8 bitValue;

   /**
    * The left operator.
    */
   uint8* leftOp;

   /**
    * The right operator.
    */
   uint8* rightOp;

   /**
    * The index bitmap of the where clause.
    */
   IntVector* indexBitmap;

   /**
    * The left key.
    */
   Key leftKey;

   /**
    * The right key.
    */
   Key rightKey;
} ;

/**
 * Represents a B-Tree header.
 */
struct Index // renamed from BTree to Index
{
   /**
    * If the keys are mostly ordered (like the rowid), makes the nodes more full.
    */
   uint8 isOrdered; // guich@110_5
   
   /**
    * Indicates if the write of the node is delayed.
    */
   uint8 isWriteDelayed;

   /**
    * The number of columns of the index: 1 means simple index.
    */
   uint8 numberColumns;

   /**
    * The maximun number of keys per node.
    */
   uint8 btreeMaxNodes;

   /**
    * A cache of node.
    */
   uint8 cacheI;

   /**
    * The size of the keys.
    */
   uint8 keyRecSize;

   /**
    * The current number of nodes in the nodes array.
    */
   uint8 nodesArrayCount;

   /**
    * The size of the nodes.
    */
	uint16 nodeRecSize;

   /**
    * The number of nodes.
    */
   uint16 nodeCount;

   /**
    * A buffer to be used to save and load data from the index.
    */
   uint8 basbuf[SECTOR_SIZE];

   /**
    * The types of the columns of the index.
    */
   int8* types;

   /**
    * The sizes of the columns of the index.
    */
   int32* colSizes;

   /**
    * The name of the index table.
    */
   char name[DBNAME_SIZE];

   /**
    * The nodes file.
    */
   XFile fnodes;

   // juliana@noidr_1: removed .idr files from all indices and changed its format.

   /**
    * The cache of the index.
    */
   Node* cache[CACHE_SIZE];
   
// juliana@230_35: now the first level nodes of a b-tree index will be loaded in memory.
   /**
    * The first level of the index B-tree.
    */
   Node** firstLevel;

   /**
    * The table of the index.
    */
   Table* table;

   /**
    * The root of the tree.
    */
   Node* root;

   /**
	 * The heap to allocate the index structure.
	 */
   Heap heap;
   
   /**
    * An array for climbing on index nodes.
    */
   size_t nodes[4]; // juliana@230_32: corrected a bug of searches in big indices not returning all the results.
};

/**
 * Represents a composed index.
 */
struct ComposedIndex
{
   /**
    * Identifies the composed index.
    */
   uint8 indexId;   

   /**
    * The number of columns of the composed index.
    */
   uint8 numberColumns; 

   /**
    * The columns index of the composed index.
    */
   uint8* columns;     

   /**
    * The index itself.
    */
   Index* index;
};

/**
 * The information stored for each query concerning the temporary tables size.
 */
struct MemoryUsageEntry
{
   /**
    * The hash code key.
    */
   int32 key;

   /**
    * Temporary .db size.
    */
   int32 dbSize;

   /**
    * Temporary .dbo size.
    */
   int32 dboSize;

   /**
    * The pointer to the next hash table entry.
    */
   MemoryUsageEntry* next;
};

/**
 * The hash table that stores the information for each query concerning the temporary tables size.
 */
struct MemoryUsageHT
{
   /**
    * The information matrix.
    */
   MemoryUsageEntry** items;
   
   /**
    * The hash table size.
    */
   int32 size;
   
   /**
    * Used to mask the hash key.
    */
   int32 hash;
   
   /**
    * The capacity.
    */
   int32 threshold;
};

// juliana@227_20: corrected order by or group by with strings being too slow.
/**
 * An structure used to sort tables with strings.
 */
struct StringArray
{
   /* 
    * The length of the loaded string. 
    */
   int32 length;
   
   /*
    * The string loaded.
    */
   JCharP string;
};

#ifdef ENABLE_TEST_SUITE
typedef struct TestSuite TestSuite;
#endif

#endif 
