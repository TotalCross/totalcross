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
 * This file defines the macros used by Litebase.
 */

#ifndef LITEBASE_MACROS_H
#define LITEBASE_MACROS_H

/** 
 * The following ifdef block is the standard way of creating macros which makes exporting from a DLL simpler. All files within this DLL are compiled 
 * with the TESTE_EXPORTS symbol defined on the command line. This symbol should not be defined on any project that uses this DLL. This way any other
 * project whose source files include this file see TESTE_API functions as being imported from a DLL, wheras this DLL sees symbols defined with this 
 * macro as being exported.
 */
#if defined (WIN32)
	#ifdef LB_EXPORTS
	#define LB_API __declspec(dllexport)
	#else
	#define LB_API __declspec(dllimport)
	#endif
#else
	#define LB_API extern
#endif

// Macro to trace Litebase function calls. This should be used in the beginning of every function.
#ifdef ENABLE_TRACE
	#define TRACE(x) bool unused = TC_trace(x);
#else
	#define TRACE(x)
#endif

// Macros for path separators.
#if defined (WIN32)
   #define PATH_SEPARATOR    '\\'
   #define NO_PATH_SEPARATOR '/'
#else
   #define PATH_SEPARATOR    '/'
   #define NO_PATH_SEPARATOR '\\'
#endif

// fdie 
// juliana@202_24: removed problem with double processing.
// juliana@210_6: removed problem with long processing.
// Supports ARM's double mixed endianness. This is handled by the READ_DOUBLE macro. The GCCE compiler of the new Symbian S60v3 generates non mixed 
// endianness doubles.
   #define READ_DOUBLE(destination, source) xmemmove(destination, source, 8) 

// Classes fields.
// DriverException
#define OBJ_DriverExceptionCause(o) FIELD_OBJ(o, OBJ_CLASS(o), 0) // DriverException.cause

// LitebaseConnection
#define OBJ_LitebaseIsAscii(o)      FIELD_I32(o, 0)					// LitebaseConnection.isAscii
#define OBJ_LitebaseUseCrypto(o)    FIELD_I32(o, 1)					// LitebaseConnection.useCrypto
#define OBJ_LitebaseDontFinalize(o) FIELD_I32(o, 2)					// LitebaseConnection.dontFinalize
#define OBJ_LitebaseKey(o)          FIELD_I32(o, 3)					// LitebaseConnection.key 
#define OBJ_LitebaseAppCrid(o)      FIELD_I32(o, 4)					// LitebaseConnection.appCrid

// LitebaseConnection.htTables
#define getLitebaseHtTables(o)    ((Hashtable*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 0))
#define setLitebaseHtTables(o, v) (FIELD_I64(o, OBJ_CLASS(o), 0) = (size_t)v)

// LitebaseConnection.sourcePath
#define getLitebaseSourcePath(o)    ((TCHARP)(size_t)FIELD_I64(o, OBJ_CLASS(o), 1))
#define setLitebaseSourcePath(o, v) (FIELD_I64(o, OBJ_CLASS(o), 1) = (size_t)v)

// LitebaseConnection.htPS // juliana@226_16
#define getLitebaseHtPS(o)    ((Hashtable*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 2))
#define setLitebaseHtPS(o, v) (FIELD_I64(o, OBJ_CLASS(o), 2) = (size_t)v)

// juliana@noidr_1: removed .idr files from all indices and changed its format.
// LitebaseConnection.nodes 
#define getLitebaseNodes(o)    ((int32*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 3))
#define setLitebaseNodes(o, v) (FIELD_I64(o, OBJ_CLASS(o), 3) = (size_t)v)

// PreparedStatement
#define OBJ_PreparedStatementType(o)          FIELD_I32(o, 0)               // PreparedStatement.type  
#define OBJ_PreparedStatementStoredParams(o)  FIELD_I32(o, 1)               // PreparedStatement.storedParams
#define OBJ_PreparedStatementDontFinalize(o)  FIELD_I32(o, 2)               // PreparedStatement.dontFinalize
#define OBJ_PreparedStatementSqlExpression(o) FIELD_OBJ(o, OBJ_CLASS(o), 0) // PreparedStatement.sqlExpression
#define OBJ_PreparedStatementDriver(o)        FIELD_OBJ(o, OBJ_CLASS(o), 1) // PreparedStatement.driver
#define OBJ_PreparedStatementObjParams(o)     FIELD_OBJ(o, OBJ_CLASS(o), 2) // PreparedStatement.ObjParams // juliana@222_8

// PreparedStatement.paramsAsStrs
#define getPreparedStatementParamsAsStrs(o)    ((JCharP*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 0))
#define setPreparedStatementParamsAsStrs(o, v) (FIELD_I64(o, OBJ_CLASS(o), 0) = (size_t)v)

// PreparedStatement.paramsPos
#define getPreparedStatementParamsPos(o)    ((int16*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 1))
#define setPreparedStatementParamsPos(o, v) (FIELD_I64(o, OBJ_CLASS(o), 1) = (size_t)v)

// PreparedStatement.paramsLength
#define getPreparedStatementParamsLength(o)    ((int16*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 2))
#define setPreparedStatementParamsLength(o, v) (FIELD_I64(o, OBJ_CLASS(o), 2) = (size_t)v)

// PreparedStatement.statement
#define getPreparedStatementStatement(o)    ((size_t)FIELD_I64(o, OBJ_CLASS(o), 3))
#define setPreparedStatementStatement(o, v) (FIELD_I64(o, OBJ_CLASS(o), 3) = (size_t)v)

// ResultSet
#define OBJ_ResultSetDontFinalize(o) FIELD_I32(o, 0) // ResultSet.dontFinalize

// ResultSet.bag
#define getResultSetBag(o)    ((ResultSet*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 0))
#define setResultSetBag(o, v) (FIELD_I64(o, OBJ_CLASS(o), 0) = (size_t)v)

// ResultSetMetaData
#define OBJ_ResultSetMetaData_ResultSet(o) FIELD_OBJ(o, OBJ_CLASS(o), 0) // ResultSetMetaData.resultSet

// RowIterator
#define OBJ_RowIteratorRowid(o)     FIELD_I32(o, 0) // RowIterator.rowid
#define OBJ_RowIteratorAttr(o)      FIELD_I32(o, 1) // RowIterator.attr
#define OBJ_RowIteratorRowNumber(o) FIELD_I32(o, 2) // RowIterator.rowNumber

// RowIterator.table
#define getRowIteratorTable(o)    ((Table*)(size_t)FIELD_I64(o, OBJ_CLASS(o), 0))
#define setRowIteratorTable(o, v) (FIELD_I64(o, OBJ_CLASS(o), 0) = (size_t)v)

#define OBJ_RowIteratorData(o)   FIELD_OBJ(o, OBJ_CLASS(o), 0) // RowIterator.data    
#define OBJ_RowIteratorDriver(o) FIELD_OBJ(o, OBJ_CLASS(o), 1) // RowIterator.driver   

// Methods
#define loggerLog        &loggerClass->methods[17] // Logger.log(int level, String message, boolean prependInfo)
#define loggerLogInfo    &loggerClass->methods[18] // Logger.logInfo(StringBuffer message) // juliana@230_30
#define addOutputHandler &loggerClass->methods[14] // Logger.addOutputHandler()  
#define getLogger        &loggerClass->methods[3]  // Logger.getLogger()

// Bitmap.                       // Sets all bits of a bitmap.
#define setBitOn(items, index)   (items)[(index) >> 3] |= ((int32)1 << ((index) & 7))       // Sets a bit of a bitmap on.
#define setBitOff(items, index)  (items)[(index) >> 3] &= ~((int32)1 << ((index) & 7))      // Sets a bit of a bitmap off.
#define isBitSet(items, index)   ((items)[(index) >> 3] & ((int32)1 << ((index) & 7))) != 0 // Verifies if a bit is set.
#define isBitUnSet(items, index) !(((items)[(index) >> 3] & ((int32)1 << ((index) & 7))))   // Verifies if a bit is unset.

// Checks if a bit is set in a <code>IntVector</code>.
#define IntVectorisBitSet(v, index) ((v)->items[(index) >> 5] & ((int32)1 << ((index) & 31)))

// Implements a stack using an <code>IntVector</code> or a <code>ShortVector</code>.
#define IntVectorPop(intVector)              intVector.items[--intVector.size]      // pop
#define IntVectorPush(intVector, value)      IntVectorAdd(intVector, value)         // push

// Returns the number of bytes necessary to store null value information concerning the columns. Each column in a table corresponds to one bit.
#define NUMBEROFBYTES(colCount) (((colCount) + 7) >> 3)

// Verifies if the column is defined as not null.
#define definedAsNotNull(byte) ((((byte) & ATTR_COLUMN_IS_NOT_NULL)))

// Turns a string representing the application id of 4 characters into an integer. 
#define getAppCridInt(cridStr) \
(uint32)((uint32)((cridStr)[0]) << 24 | (uint32)((cridStr)[1]) << 16 | (uint32)((cridStr)[2]) << 8 | (uint32)((cridStr)[3]))

// Parser macros.
#define YYPARSE_PARAM    parser                                                      // yyparse() parameter.
#define parserTP         ((LitebaseParser*)parser)                                   // Typed parameter.
#define YYTRANSLATE(YYX) ((uint32)(YYX) <= YYMAXUTOK? yytranslate[YYX] : YYUNDEFTOK) // Translates YYLEX symbols.                                          // Pops parser stacks.

// Gets the current character.
#define GET_YYCURRENT(yycurrent) yycurrent = parser->yyposition < parser->length ? zzReaderChars[parser->yyposition++] : PARSER_EOF

// Inserts a character in the buffer of the current token.
#define INSERT_CHAR(yycurrent) yycurrent = parser->yyposition++ < parser->length ? zzReaderChars[parser->yyposition - 1] : PARSER_EOF

// Updates the hash code of a sql select command.
#define CALCULATE_HASH(character) if (parser->yyposition > 0) hash = (hash << 5) - hash + (int32)(character)

// juliana@noidr_1: removed .idr files from all indices and changed its format.
// Key macros.
// Indicates if two keys are equal.
#define keyEquals(context, key1, key2, size, plainDB) (key2 && !keyCompareTo(context, key1, key2, size, plainDB))

// Indicates if a node of an index is a leaf node.
#define nodeIsLeaf(node) (*node->children == LEAF) 

// Linked list declaration.
#define TC_DeclareList(type)										    \
typedef struct type##s												    \
{																			    \
   struct type##s* next;											    \
   struct type##s* prev;											    \
   type value;															    \
} type##s;																    \
type##s* TC_##type##sAdd(type##s* list, type value, Heap h); \
type##s* TC_##type##sRemove(type##s* list, type value)		 \

// Linked list declaration, with functions to add and remove an element.
// The function to add an element searchs the list to see if there is a free node to add the element before alocating anything.
// The funtion to remove an element only invalidates the list position, which can be re-used later on.
// juliana@221_1: solved a problem that could reduce the free memory too much if many prepared statements were created and collected many times.
#define TC_ImplementList(type)                                                 \
type##s* TC_##type##sAdd(type##s* list, type value, Heap heap)                 \
{                                                                              \
   if (!list)                                                                  \
   {                                                                           \
      (list = (type##s*)TC_heapAlloc(heap, sizeof(type##s)))->value = value;   \
      list->next = list->prev = list;                                          \
   }                                                                           \
   else                                                                        \
   {                                                                           \
	   type##s* head = list;										                      \
      type##s *element;                                                        \
		do										                                           \
		{															                            \
			if (!head->value)								                               \
			{														                            \
				head->value = value;							                            \
				return list;										                         \
			}														                            \
			head = head->next;								                            \
		} while (head != list);															       \
      (element = (type##s*)TC_heapAlloc(heap,sizeof(type##s)))->value = value; \
      element->prev = list->prev;                                              \
      element->next = list;                                                    \
      list->prev =list->prev->next = element;                                  \
   }                                                                           \
   return list;                                                                \
}                                                                              \
																	                            \
type##s* TC_##type##sRemove(type##s* list, type value)                         \
{                                                                              \
   type##s* head = list;                                                       \
   if (head)													                            \
      do                                                                       \
      {                                                                        \
         if (list->value == value)                                             \
				list->value = null;								                         \
         list = list->next;                                                    \
      } while (head != list);                                                  \
   return head;                                                                \
}											
#endif
