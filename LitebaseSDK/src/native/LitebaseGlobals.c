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

/**
 * Defines all global variables used by Litebase.
 */

#include "LitebaseGlobals.h"

// Globas for driver creation.
Hashtable htCreatedDrivers; // The hash table for the created connections with Litebase.

// juliana@closeFiles_1: removed possible problem of the IOException with the message "Too many open files".
// The list of table files currently opened.
#if defined(POSIX) || defined(ANDROID)
XFilesList filesList; 
#endif

// Globals for the parser.
Hashtable reserved;                 // Table containing the reserved words.
MemoryUsageHT memoryUsage;          // Indicates how much memory a select sql command uses in its temporary .db.
uint8 is[256];                      // An array to help the selection of the kind of the token.
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
TCClass litebaseConnectionClass; // LitebaseConnection
TCClass loggerClass;             // Logger

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

CharP errorMsgs_en[TOTAL_ERRORS]; // English error messages.
CharP errorMsgs_pt[TOTAL_ERRORS]; // Portuguese error messages.

// juliana@220_4: added a crc32 code for every record. Please update your tables.
int32 crcTable[CRC32_SIZE]; // The crc32 table used to calculate a crc32 for a record.

// TotalCross functions used by Litebase.
CharP2JCharPFunc TC_CharP2JCharP;
CharP2JCharPBufFunc TC_CharP2JCharPBuf;
CharPToLowerFunc TC_CharPToLower;
JCharP2CharPFunc TC_JCharP2CharP;
JCharP2CharPBufFunc TC_JCharP2CharPBuf;
JCharPEqualsJCharPFunc TC_JCharPEqualsJCharP;
JCharPEqualsIgnoreCaseJCharPFunc TC_JCharPEqualsIgnoreCaseJCharP;
JCharPHashCodeFunc TC_JCharPHashCode;
JCharPIndexOfJCharFunc TC_JCharPIndexOfJChar;
JCharPLenFunc TC_JCharPLen;
JCharToLowerFunc TC_JCharToLower;
JCharToUpperFunc TC_JCharToUpper;
alertFunc TC_alert;
appendCharPFunc TC_appendCharP; // juliana@230_30
appendJCharPFunc TC_appendJCharP; // juliana@230_30
areClassesCompatibleFunc TC_areClassesCompatible;
createArrayObjectFunc TC_createArrayObject;
createObjectFunc TC_createObject;
createStringObjectFromCharPFunc TC_createStringObjectFromCharP;
createStringObjectFromTCHARPFunc TC_createStringObjectFromTCHARP;
createStringObjectWithLenFunc TC_createStringObjectWithLen;
debugFunc TC_debug;
double2strFunc TC_double2str;
executeMethodFunc TC_executeMethod;
getApplicationIdFunc TC_getApplicationId;
getAppPathFunc TC_getAppPath;
getDataPathFunc TC_getDataPath;
getDateTimeFunc TC_getDateTime;
getErrorMessageFunc TC_getErrorMessage;
getProcAddressFunc TC_getProcAddress;
getSettingsPtrFunc TC_getSettingsPtr;
getTimeStampFunc TC_getTimeStamp;
hashCodeFunc TC_hashCode;
hashCodeFmtFunc TC_hashCodeFmt;
heapAllocFunc TC_heapAlloc;
heapDestroyPrivateFunc TC_heapDestroyPrivate;
hstrdupFunc TC_hstrdup;
htFreeFunc TC_htFree;
htFreeContextFunc TC_htFreeContext; 
htGet32Func TC_htGet32;
htGet32InvFunc TC_htGet32Inv;
htGetPtrFunc TC_htGetPtr;
htNewFunc TC_htNew;
htPut32Func TC_htPut32;
htPut32IfNewFunc TC_htPut32IfNew;
htPutPtrFunc TC_htPutPtr;
htRemoveFunc TC_htRemove;
int2CRIDFunc TC_int2CRID;
int2strFunc TC_int2str;
listFilesFunc TC_listFiles;
loadClassFunc TC_loadClass;
long2strFunc TC_long2str;
privateHeapCreateFunc TC_privateHeapCreate;
privateHeapSetJumpFunc TC_privateHeapSetJump;
privateXfreeFunc TC_privateXfree;
privateXmallocFunc TC_privateXmalloc;
privateXreallocFunc TC_privateXrealloc;
setObjectLockFunc TC_setObjectLock;
str2doubleFunc TC_str2double;
str2intFunc TC_str2int;
str2longFunc TC_str2long;
throwExceptionNamedFunc TC_throwExceptionNamed;
throwNullArgumentExceptionFunc TC_throwNullArgumentException;
tiF_create_siiFunc TC_tiF_create_sii;
toLowerFunc TC_toLower;
traceFunc TC_trace;
validatePathFunc TC_validatePath; // juliana@214_1
#ifdef PALMOS
getLastVolumeFunc TC_getLastVolume;
#endif
#ifdef ENABLE_MEMORY_TEST
getCountToReturnNullFunc TC_getCountToReturnNull;
setCountToReturnNullFunc TC_setCountToReturnNull;
#endif
