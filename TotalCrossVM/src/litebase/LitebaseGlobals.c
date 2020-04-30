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
 * Defines all global variables used by Litebase.
 */

#include "LitebaseGlobals.h"

// Globas for driver creation.
Hashtable htCreatedDrivers = { 0 }; // The hash table for the created connections with Litebase.

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// The list of table files currently opened.
#if defined(POSIX) || defined(ANDROID)
XFilesList filesList; 
#endif

// Globals for the parser.
Hashtable reserved = { 0 };                 // Table containing the reserved words.
MemoryUsageHT memoryUsage = { 0 };          // Indicates how much memory a select sql command uses in its temporary .db.
uint8 is[256] = { 0 };                      // An array to help the selection of the kind of the token.
int8 function_x_datatype[10][7] = { // Matrix of data types which applies to the SQL functions.
      {FUNCTION_DT_UPPER, FUNCTION_DT_LOWER, FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },  
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },     
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                   
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                  
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                  
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                  
      {FUNCTION_DT_UPPER, FUNCTION_DT_LOWER, FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },  
      {FUNCTION_DT_NONE , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },         
      {FUNCTION_DT_YEAR , FUNCTION_DT_MONTH, FUNCTION_DT_DAY,  FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  }, 
      {FUNCTION_DT_YEAR , FUNCTION_DT_MONTH, FUNCTION_DT_DAY,  FUNCTION_DT_HOUR, FUNCTION_DT_MINUTE, FUNCTION_DT_SECOND, FUNCTION_DT_MILLIS}}; 

// An array with the names of the SQL data functions.     
CharP names[10] = {"year", "month", "day", "hour", "minute", "second", "millis", "abs", "upper", "lower"};

// Used to count bits in an index bitmap.
uint8 bitsInNibble[16] = {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4};

JChar questionMark[2] = {(JChar)'?', (JChar)'\0'}; // A jchar string representing "?".                           

// juliana@253_9: improved Litebase parser.
                                                                                       
// Classes used.                                                                       
TCClass litebaseConnectionClass = { 0 }; // LitebaseConnection
TCClass loggerClass = { 0 };             // Logger

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// Mutexes used.
DECLARE_MUTEX(parser); // Mutex for the parser.
DECLARE_MUTEX(log);    // Mutex for logging.
DECLARE_MUTEX(files);  // Mutex for the Litebase files list.

// rnovais@568_10 @570_1 juliana@226_5
// Aggregate functions table.
int8 aggregateFunctionsTypes[FUNCTION_AGG_SUM + 1] = {INT_TYPE, UNDEFINED_TYPE, UNDEFINED_TYPE, DOUBLE_TYPE, DOUBLE_TYPE};

// Data Type functions table.
int8 dataTypeFunctionsTypes[FUNCTION_DT_LOWER + 1] = {SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, 
                                                                                                      UNDEFINED_TYPE, CHARS_TYPE, CHARS_TYPE};
// Number of days in a month. 
uint8 monthDays[12] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

// Each type size in the .db file.
uint8 typeSizes[11] = {4, 2, 4, 8, 4, 8, 4, -1, 4, 8, 4}; // rnovais@567_2: added more sizes.

CharP errorMsgs_en[TOTAL_ERRORS] = { 0 }; // English error messages.
CharP errorMsgs_pt[TOTAL_ERRORS] = { 0 }; // Portuguese error messages.

// juliana@220_4: added a crc32 code for every record. Please update your tables.
int32 crcTable[CRC32_SIZE] = { 0 }; // The crc32 table used to calculate a crc32 for a record.

// TotalCross functions used by Litebase.
CharP2JCharPFunc TC_CharP2JCharP = { 0 };
CharP2JCharPBufFunc TC_CharP2JCharPBuf = { 0 };
CharP2TCHARPBufFunc TC_CharP2TCHARPBuf = { 0 };
CharPToLowerFunc TC_CharPToLower = { 0 };
JCharP2CharPFunc TC_JCharP2CharP = { 0 };
JCharP2CharPBufFunc TC_JCharP2CharPBuf = { 0 };
JCharP2TCHARPBufFunc TC_JCharP2TCHARPBuf = { 0 };
JCharPEqualsJCharPFunc TC_JCharPEqualsJCharP = { 0 };
JCharPEqualsIgnoreCaseJCharPFunc TC_JCharPEqualsIgnoreCaseJCharP = { 0 };
JCharPHashCodeFunc TC_JCharPHashCode = { 0 };
JCharPIndexOfJCharFunc TC_JCharPIndexOfJChar = { 0 };
JCharPLenFunc TC_JCharPLen = { 0 };
JCharToLowerFunc TC_JCharToLower = { 0 };
JCharToUpperFunc TC_JCharToUpper = { 0 };
TCHARP2CharPBufFunc TC_TCHARP2CharPBuf = { 0 };
alertFunc TC_alert = { 0 };
appendCharPFunc TC_appendCharP = { 0 }; // juliana@230_30
appendJCharPFunc TC_appendJCharP = { 0 }; // juliana@230_30
areClassesCompatibleFunc TC_areClassesCompatible = { 0 };
createArrayObjectFunc TC_createArrayObject = { 0 };
createObjectFunc TC_createObject = { 0 };
createStringObjectFromCharPFunc TC_createStringObjectFromCharP = { 0 };
createStringObjectFromTCHARPFunc TC_createStringObjectFromTCHARP = { 0 };
createStringObjectWithLenFunc TC_createStringObjectWithLen = { 0 };
debugFunc TC_debug = { 0 };
double2strFunc TC_double2str = { 0 };
executeMethodFunc TC_executeMethod = { 0 };
getApplicationIdFunc TC_getApplicationId = { 0 };
getAppPathFunc TC_getAppPath = { 0 };
getDataPathFunc TC_getDataPath = { 0 };
getDateTimeFunc TC_getDateTime = { 0 };
getErrorMessageFunc TC_getErrorMessage = { 0 };
getProcAddressFunc TC_getProcAddress = { 0 };
getSettingsPtrFunc TC_getSettingsPtr = { 0 };
getTimeStampFunc TC_getTimeStamp = { 0 };
hashCodeFunc TC_hashCode = { 0 };
hashCodeFmtFunc TC_hashCodeFmt = { 0 };
heapAllocFunc TC_heapAlloc = { 0 };
heapDestroyPrivateFunc TC_heapDestroyPrivate = { 0 };
hstrdupFunc TC_hstrdup = { 0 };
htFreeFunc TC_htFree = { 0 };
htFreeContextFunc TC_htFreeContext = { 0 }; 
htGet32Func TC_htGet32 = { 0 };
htGet32InvFunc TC_htGet32Inv = { 0 };
htGetPtrFunc TC_htGetPtr = { 0 };
htNewFunc TC_htNew = { 0 };
htPut32Func TC_htPut32 = { 0 };
htPut32IfNewFunc TC_htPut32IfNew = { 0 };
htPutPtrFunc TC_htPutPtr = { 0 };
htRemoveFunc TC_htRemove = { 0 };
int2CRIDFunc TC_int2CRID = { 0 };
int2strFunc TC_int2str = { 0 };
listFilesFunc TC_listFiles = { 0 };
loadClassFunc TC_loadClass = { 0 };
long2strFunc TC_long2str = { 0 };
privateHeapCreateFunc TC_privateHeapCreate = { 0 };
privateHeapSetJumpFunc TC_privateHeapSetJump = { 0 };
privateXfreeFunc TC_privateXfree = { 0 };
privateXmallocFunc TC_privateXmalloc = { 0 };
privateXreallocFunc TC_privateXrealloc = { 0 };
setObjectLockFunc TC_setObjectLock = { 0 };
str2doubleFunc TC_str2double = { 0 };
str2intFunc TC_str2int = { 0 };
str2longFunc TC_str2long = { 0 };
throwExceptionNamedFunc TC_throwExceptionNamed = { 0 };
throwNullArgumentExceptionFunc TC_throwNullArgumentException = { 0 };
tiF_create_siiFunc TC_tiF_create_sii = { 0 };
toLowerFunc TC_toLower = { 0 };
traceFunc TC_trace = { 0 };
validatePathFunc TC_validatePath = { 0 }; // juliana@214_1

#ifdef ENABLE_MEMORY_TEST
getCountToReturnNullFunc TC_getCountToReturnNull = { 0 };
setCountToReturnNullFunc TC_setCountToReturnNull = { 0 };
#endif
