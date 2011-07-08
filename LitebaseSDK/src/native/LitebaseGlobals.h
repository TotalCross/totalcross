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

/**
 * Declares all global variables used by Litebase.
 */

#ifndef LITEBASE_GLOBALS_H
#define LITEBASE_GLOBALS_H

#include "Litebase.h"
#include "LitebaseTypes.h"

// Empty structures.
extern IntVector emptyIntVector; // Empty IntVector.
extern Hashtable emptyHashtable; // Empty hash table.

// Globas for driver creation.
extern Hashtable htCreatedDrivers; // The hash table for the created connections with Litebase.
extern Heap hashTablesHeap;        // The heap to allocate the reserved words and memory usage hash tables.

// Globals for the parser.
extern Hashtable reserved;              // Table containing the reserved words.
extern Hashtable memoryUsage;           // Indicates how much memory a select sql command uses in its temporary .db.
extern uint8 is[256];                   // An array to help the selection of the kind of the token.
extern int8 function_x_datatype[10][7]; // Matrix of data types which applies to the SQL functions.
extern CharP names[10];                 // An array with the names of the SQL data functions.
extern uint8 bitsInNibble[16];          // Used to count bits in an index bitmap.
extern YYSTYPE yylval;                  // Variable where a token is stored.
extern JChar questionMark[2];           // A jchar string representing "?".
extern uint8 yytranslate[];             // YYTRANSLATE[YYLEX] -- Bison symbol number corresponding to YYLEX.
extern uint8 yyr1[];                    // YYR1[YYN] -- Symbol number of symbol that rule YYN derives.
extern uint8 yyr2[];                    // YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.
extern int16 yypact[];                  // YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing STATE-NUM.
extern int16 yydefgoto[];               // YYDEFGOTO[NTERM-NUM].
extern int16 yypgoto[];                 // YYPGOTO[NTERM-NUM].  
extern int16 yycheck[];                 // Check table to see if the lexer state is correct.
extern uint8 yystos[];                  // YYSTOS[STATE-NUM] -- The (internal number of the) accessing symbol of state STATE-NUM. 

// YYDEFACT[STATE-NAME] -- Default rule to reduce with in state STATE-NUM when YYTABLE doesn't specify something else to do. Zero means the default 
// is an error. 
extern uint8 yydefact[];

// YYTABLE[YYPACT[STATE-NUM]]. What to do in state STATE-NUM. If positive, shifts that token. If negative, reduces the rule which number is the 
// opposite. If zero, does what YYDEFACT says. If YYTABLE_NINF, syntax error.  
extern uint16 yytable[];

// Java methods called by Litebase.                                                                                           
extern Method newFile;           // new File(String name, int mode, int slot)                 
extern Method loggerLog;         // Logger.log(int level, String message, boolean prependInfo)
extern Method addOutputHandler;  // Logger.addOutputHandler()                                 
extern Method getLogger;         // Logger.getLogger()                                        
                                                                                              
// Classes used.                                                                              
extern Class litebaseConnectionClass; // LitebaseConnection                                   
extern Class loggerClass;             // Logger                                               
extern Class fileClass;               // File                                                 
extern Class throwableClass;          // Throwable
extern Class vectorClass;             // Vector

// Mutexes used.
extern DECLARE_MUTEX(parser); // Mutex for the parser.
extern DECLARE_MUTEX(log);    // Mutex for logging.

// rnovais@568_10 @570_1 
extern int8 aggregateFunctionsTypes[FUNCTION_AGG_SUM + 1];  // Aggregate functions table. 
extern int8 dataTypeFunctionsTypes[FUNCTION_DT_LOWER + 1];  // Data Type functions table.
extern uint8 monthDays[12];                                 // Number of days in a month. 
extern uint8 typeSizes[11];                                 // Each type size in the .db file.

// Error messages.
extern CharP errorMsgs_en[TOTAL_ERRORS]; // English error messages.
extern CharP errorMsgs_pt[TOTAL_ERRORS]; // Portuguese error messages.

extern int32 crcTable[CRC32_SIZE]; // The crc32 table used to calculate a crc32 for a record.

// TotalCross functions used by Litebase.
extern CharP2JCharPFunc TC_CharP2JCharP;
extern CharP2JCharPBufFunc TC_CharP2JCharPBuf;
extern CharPToLowerFunc TC_CharPToLower;
extern JCharP2CharPBufFunc TC_JCharP2CharPBuf;
extern JCharP2CharPFunc TC_JCharP2CharP;
extern JCharPEqualsJCharPFunc TC_JCharPEqualsJCharP;
extern JCharPEqualsIgnoreCaseJCharPFunc TC_JCharPEqualsIgnoreCaseJCharP;
extern JCharPHashCodeFunc TC_JCharPHashCode;
extern JCharPIndexOfJCharFunc TC_JCharPIndexOfJChar;
extern JCharPLenFunc TC_JCharPLen;
extern JCharToLowerFunc TC_JCharToLower;
extern JCharToUpperFunc TC_JCharToUpper;
extern areClassesCompatibleFunc TC_areClassesCompatible;
extern alertFunc TC_alert;
extern createArrayObjectFunc TC_createArrayObject;
extern createObjectFunc TC_createObject;
extern createObjectWithoutCallingDefaultConstructorFunc TC_createObjectWithoutCallingDefaultConstructor;
extern createStringObjectFromCharPFunc TC_createStringObjectFromCharP;
extern createStringObjectWithLenFunc TC_createStringObjectWithLen;
extern debugFunc TC_debug;
extern double2strFunc TC_double2str;
extern executeMethodFunc TC_executeMethod;
extern getApplicationIdFunc TC_getApplicationId;
extern getAppPathFunc TC_getAppPath;
extern getDataPathFunc TC_getDataPath;
extern getDateTimeFunc TC_getDateTime;
extern getErrorMessageFunc TC_getErrorMessage;
extern getMethodFunc TC_getMethod;
extern getProcAddressFunc TC_getProcAddress;
extern getSettingsPtrFunc TC_getSettingsPtr;
extern getTimeStampFunc TC_getTimeStamp;
extern hashCodeFmtFunc TC_hashCodeFmt;
extern hashCodeFunc TC_hashCode;
extern heapAllocFunc TC_heapAlloc;
extern heapDestroyPrivateFunc TC_heapDestroyPrivate;
extern hstrdupFunc TC_hstrdup;
extern htFreeFunc TC_htFree;
extern htFreeContextFunc TC_htFreeContext; 
extern htGet32Func TC_htGet32;
extern htGet32InvFunc TC_htGet32Inv;
extern htGetPtrFunc TC_htGetPtr;
extern htNewFunc TC_htNew;
extern htPut32Func TC_htPut32;
extern htPut32IfNewFunc TC_htPut32IfNew;
extern htPutPtrFunc TC_htPutPtr;
extern htRemoveFunc TC_htRemove;
extern int2CRIDFunc TC_int2CRID;
extern int2strFunc TC_int2str;
extern listFilesFunc TC_listFiles;
extern loadClassFunc TC_loadClass;
extern long2strFunc TC_long2str;
extern newStackFunc TC_newStack;
extern privateHeapCreateFunc TC_privateHeapCreate;
extern privateHeapSetJumpFunc TC_privateHeapSetJump;
extern privateXfreeFunc TC_privateXfree;
extern privateXmallocFunc TC_privateXmalloc;
extern privateXreallocFunc TC_privateXrealloc;
extern setObjectLockFunc TC_setObjectLock;
extern stackPopFunc TC_stackPop;
extern stackPushFunc TC_stackPush;
extern str2doubleFunc TC_str2double;
extern str2intFunc TC_str2int;
extern str2longFunc TC_str2long;
extern throwExceptionNamedFunc TC_throwExceptionNamed;
extern throwNullArgumentExceptionFunc TC_throwNullArgumentException;
extern toLowerFunc TC_toLower;
extern traceFunc TC_trace;
extern validatePathFunc TC_validatePath; // juliana@214_1
#ifdef PALMOS
extern getLastVolumeFunc TC_getLastVolume;
#endif
#ifdef ENABLE_MEMORY_TEST
extern getCountToReturnNullFunc TC_getCountToReturnNull;
extern setCountToReturnNullFunc TC_setCountToReturnNull;
#endif

#endif
