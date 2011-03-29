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

// $Id: TCVMLib.c,v 1.1.2.63 2011-01-25 14:53:03 juliana Exp $

/**
 * Implements the function used to load the pointers to TotalCross functions used by Litebase and delcares the function names.
 */

#include "TCVMLib.h"

/**
 * Initializes the pointers to TotalCross functions used by Litebase. 
 */
void initTCVMLib()
{
   TC_CharP2JCharP = TC_getProcAddress(null, "CharP2JCharP");
   TC_CharP2JCharPBuf = TC_getProcAddress(null, "CharP2JCharPBuf");
   TC_CharPToLower = TC_getProcAddress(null, "CharPToLower");
   TC_JCharP2CharP = TC_getProcAddress(null, "JCharP2CharP");
   TC_JCharP2CharPBuf = TC_getProcAddress(null, "JCharP2CharPBuf");
	TC_JCharPEqualsJCharP = TC_getProcAddress(null, "JCharPEqualsJCharP");
   TC_JCharPEqualsIgnoreCaseJCharP = TC_getProcAddress(null, "JCharPEqualsIgnoreCaseJCharP");
   TC_JCharPHashCode = TC_getProcAddress(null, "JCharPHashCode");
   TC_JCharPIndexOfJChar = TC_getProcAddress(null, "JCharPIndexOfJChar");
	TC_JCharPLen = TC_getProcAddress(null, "JCharPLen");
   TC_JCharToLower = TC_getProcAddress(null, "JCharToLower");
   TC_JCharToUpper = TC_getProcAddress(null, "JCharToUpper");
   TC_alert = TC_getProcAddress(null, "alert");
   TC_createArrayObject = TC_getProcAddress(null, "createArrayObject");
   TC_createObject = TC_getProcAddress(null, "createObject");
   TC_createObjectWithoutCallingDefaultConstructor = TC_getProcAddress(null, "createObjectWithoutCallingDefaultConstructor");
   TC_createStringObjectFromCharP = TC_getProcAddress(null, "createStringObjectFromCharP");
   TC_createStringObjectWithLen = TC_getProcAddress(null, "createStringObjectWithLen");
   TC_debug = TC_getProcAddress(null, "debug");
   TC_double2str = TC_getProcAddress(null, "double2str");
   TC_executeMethod = TC_getProcAddress(null, "executeMethod");
	TC_getApplicationId = TC_getProcAddress(null, "getApplicationId");
   TC_getAppPath = TC_getProcAddress(null, "getAppPath");
   TC_getDataPath = TC_getProcAddress(null, "getDataPath");
   TC_getDateTime = TC_getProcAddress(null, "getDateTime");
	TC_getErrorMessage = TC_getProcAddress(null, "getErrorMessage");
   TC_getMethod = TC_getProcAddress(null, "getMethod");
   TC_getSettingsPtr = TC_getProcAddress(null, "getSettingsPtr");
   TC_getTimeStamp = TC_getProcAddress(null, "getTimeStamp");
   TC_hashCode = TC_getProcAddress(null, "hashCode");
   TC_hashCodeFmt = TC_getProcAddress(null, "hashCodeFmt");
   TC_heapAlloc = TC_getProcAddress(null, "heapAlloc");
   TC_heapDestroyPrivate = TC_getProcAddress(null, "heapDestroyPrivate");
   TC_hstrdup = TC_getProcAddress(null, "hstrdup");
   TC_htFree = TC_getProcAddress(null, "htFree");
   TC_htFreeContext = TC_getProcAddress(null, "htFreeContext");
   TC_htGet32 = TC_getProcAddress(null, "htGet32");
   TC_htGet32Inv = TC_getProcAddress(null, "htGet32Inv");
   TC_htGetPtr = TC_getProcAddress(null, "htGetPtr");
   TC_htNew = TC_getProcAddress(null, "htNew");
   TC_htPut32 = TC_getProcAddress(null, "htPut32");
   TC_htPut32IfNew = TC_getProcAddress(null, "htPut32IfNew");
   TC_htPutPtr = TC_getProcAddress(null, "htPutPtr");
   TC_htRemove = TC_getProcAddress(null, "htRemove");
   TC_int2CRID =  TC_getProcAddress(null, "int2CRID");
   TC_int2str = TC_getProcAddress(null, "int2str");
   TC_listFiles = TC_getProcAddress(null, "listFiles");
   TC_loadClass = TC_getProcAddress(null, "loadClass");
   TC_long2str = TC_getProcAddress(null, "long2str");
   TC_newStack = TC_getProcAddress(null, "newStack");
   TC_privateHeapCreate = TC_getProcAddress(null, "privateHeapCreate");
   TC_privateHeapSetJump = TC_getProcAddress(null, "privateHeapSetJump");
   TC_privateXfree = TC_getProcAddress(null, "privateXfree");
   TC_privateXmalloc = TC_getProcAddress(null, "privateXmalloc");
   TC_privateXrealloc = TC_getProcAddress(null, "privateXrealloc");
   TC_setObjectLock = TC_getProcAddress(null, "setObjectLock");
   TC_stackPop = TC_getProcAddress(null, "stackPop");
   TC_stackPush = TC_getProcAddress(null, "stackPush");
   TC_str2double = TC_getProcAddress(null, "str2double");
   TC_str2int = TC_getProcAddress(null, "str2int");
   TC_str2long = TC_getProcAddress(null, "str2long");
   TC_throwExceptionNamed = TC_getProcAddress(null, "throwExceptionNamed");
   TC_throwNullArgumentException = TC_getProcAddress(null, "throwNullArgumentException");
	TC_tiPDBF_listPDBs_ii = TC_getProcAddress(null, "tiPDBF_listPDBs_ii");
   TC_toLower = TC_getProcAddress(null, "toLower");
   TC_trace = TC_getProcAddress(null, "trace");
   TC_validatePath = TC_getProcAddress(null, "validatePath"); // juliana@214_1
#ifdef PALMOS
   TC_getLastVolume = TC_getProcAddress(null, "getLastVolume");
#endif
#ifdef ENABLE_MEMORY_TEST
   TC_getCountToReturnNull = TC_getProcAddress(null, "getCountToReturnNull");
	TC_setCountToReturnNull = TC_getProcAddress(null, "setCountToReturnNull");
#endif 
}

#ifdef PALMOS
int __setjmp(__attribute__ ((unused)) register jmp_buf env)
{
  asm volatile ("stmia a1,{v1-v8,sp,lr}\n");
  asm volatile ("mov a1,$0\n");   // return 0 to indicate initial setjmp
  asm volatile ("bx lr\n");       // return to caller using interworking return
  return 0;                       // never reached, just to prevent a warning
}
#endif

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the TotalCross functions were loaded successfully.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(initTCVMLib)
{
   UNUSED(currentContext)
   ASSERT1_EQUALS(NotNull, TC_CharP2JCharP);
   ASSERT1_EQUALS(NotNull, TC_CharP2JCharPBuf);
   ASSERT1_EQUALS(NotNull, TC_CharPToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharP);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharPBuf);
	ASSERT1_EQUALS(NotNull, TC_JCharPEqualsJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPEqualsIgnoreCaseJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPHashCode);
   ASSERT1_EQUALS(NotNull, TC_JCharPIndexOfJChar);
	ASSERT1_EQUALS(NotNull, TC_JCharPLen);
   ASSERT1_EQUALS(NotNull, TC_JCharToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharToUpper);
   ASSERT1_EQUALS(NotNull, TC_alert);
   ASSERT1_EQUALS(NotNull, TC_createArrayObject);
   ASSERT1_EQUALS(NotNull, TC_createObject);
   ASSERT1_EQUALS(NotNull, TC_createObjectWithoutCallingDefaultConstructor);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromCharP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectWithLen);
   ASSERT1_EQUALS(NotNull, TC_debug);
   ASSERT1_EQUALS(NotNull, TC_double2str);
   ASSERT1_EQUALS(NotNull, TC_executeMethod);
	ASSERT1_EQUALS(NotNull, TC_getApplicationId);
   ASSERT1_EQUALS(NotNull, TC_getAppPath);
   ASSERT1_EQUALS(NotNull, TC_getDataPath);
   ASSERT1_EQUALS(NotNull, TC_getDateTime);
	ASSERT1_EQUALS(NotNull, TC_getErrorMessage);
   ASSERT1_EQUALS(NotNull, TC_getMethod);
   ASSERT1_EQUALS(NotNull, TC_getSettingsPtr);
   ASSERT1_EQUALS(NotNull, TC_getTimeStamp);
   ASSERT1_EQUALS(NotNull, TC_hashCode);
   ASSERT1_EQUALS(NotNull, TC_hashCodeFmt);
   ASSERT1_EQUALS(NotNull, TC_heapAlloc);
   ASSERT1_EQUALS(NotNull, TC_heapDestroyPrivate);
   ASSERT1_EQUALS(NotNull, TC_hstrdup);
   ASSERT1_EQUALS(NotNull, TC_htFree);
   ASSERT1_EQUALS(NotNull, TC_htFreeContext);
   ASSERT1_EQUALS(NotNull, TC_htGet32);
   ASSERT1_EQUALS(NotNull, TC_htGet32Inv);
   ASSERT1_EQUALS(NotNull, TC_htGetPtr);
   ASSERT1_EQUALS(NotNull, TC_htNew);
   ASSERT1_EQUALS(NotNull, TC_htPut32);
   ASSERT1_EQUALS(NotNull, TC_htPut32IfNew);
   ASSERT1_EQUALS(NotNull, TC_htPutPtr);
   ASSERT1_EQUALS(NotNull, TC_htRemove);
   ASSERT1_EQUALS(NotNull, TC_int2CRID);
   ASSERT1_EQUALS(NotNull, TC_int2str);
   ASSERT1_EQUALS(NotNull, TC_listFiles);
   ASSERT1_EQUALS(NotNull, TC_loadClass);
   ASSERT1_EQUALS(NotNull, TC_long2str);
   ASSERT1_EQUALS(NotNull, TC_newStack);
   ASSERT1_EQUALS(NotNull, TC_privateHeapCreate);
   ASSERT1_EQUALS(NotNull, TC_privateHeapSetJump);
   ASSERT1_EQUALS(NotNull, TC_privateXfree);
   ASSERT1_EQUALS(NotNull, TC_privateXmalloc);
   ASSERT1_EQUALS(NotNull, TC_privateXrealloc);
   ASSERT1_EQUALS(NotNull, TC_setObjectLock);
   ASSERT1_EQUALS(NotNull, TC_stackPop);
   ASSERT1_EQUALS(NotNull, TC_stackPush);
   ASSERT1_EQUALS(NotNull, TC_str2double);
   ASSERT1_EQUALS(NotNull, TC_str2int);
   ASSERT1_EQUALS(NotNull, TC_str2long);
   ASSERT1_EQUALS(NotNull, TC_throwExceptionNamed);
   ASSERT1_EQUALS(NotNull, TC_throwNullArgumentException);
	ASSERT1_EQUALS(NotNull, TC_tiPDBF_listPDBs_ii);
   ASSERT1_EQUALS(NotNull, TC_toLower);
   ASSERT1_EQUALS(NotNull, TC_trace);
   ASSERT1_EQUALS(NotNull, TC_validatePath); // juliana@214_1
#ifdef PALMOS
   ASSERT1_EQUALS(NotNull, TC_getLastVolume);
#endif
#ifdef ENABLE_MEMORY_TEST
   ASSERT1_EQUALS(NotNull, TC_getCountToReturnNull);
	ASSERT1_EQUALS(NotNull, TC_setCountToReturnNull);
#endif 

finish: ;
}

#endif
