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
 * Implements the function used to load the pointers to TotalCross functions used by Litebase and delcares the function names.
 */

#include "TCVMLib.h"

#if defined (darwin) || defined (ANDROID)
#define GETPROCADDRESS(x) x
#else
#define GETPROCADDRESS(x) TC_getProcAddress(null, #x)
#endif

/**
 * Initializes the pointers to TotalCross functions used by Litebase. 
 */
void initTCVMLib()
{
   TC_tiF_create_sii = TC_getProcAddress(null, "tiF_create_sii");
   TC_CharP2JCharP = GETPROCADDRESS(CharP2JCharP);
   TC_CharP2JCharPBuf = GETPROCADDRESS(CharP2JCharPBuf);
   TC_CharP2TCHARPBuf = GETPROCADDRESS(CharP2TCHARPBuf);
   TC_CharPToLower = GETPROCADDRESS(CharPToLower);
   TC_JCharP2CharP = GETPROCADDRESS(JCharP2CharP);   
   TC_JCharP2CharPBuf = GETPROCADDRESS(JCharP2CharPBuf);
   TC_JCharP2TCHARPBuf = GETPROCADDRESS(JCharP2TCHARPBuf);
   TC_JCharPEqualsJCharP = GETPROCADDRESS(JCharPEqualsJCharP);
   TC_JCharPEqualsIgnoreCaseJCharP = GETPROCADDRESS(JCharPEqualsIgnoreCaseJCharP);
   TC_JCharPHashCode = GETPROCADDRESS(JCharPHashCode);
   TC_JCharPIndexOfJChar = GETPROCADDRESS(JCharPIndexOfJChar);
   TC_JCharPLen = GETPROCADDRESS(JCharPLen);
   TC_JCharToLower = GETPROCADDRESS(JCharToLower);
   TC_JCharToUpper = GETPROCADDRESS(JCharToUpper);
   TC_TCHARP2CharPBuf = GETPROCADDRESS(TCHARP2CharPBuf);
   TC_alert = GETPROCADDRESS(alert);
   TC_appendCharP = GETPROCADDRESS(appendCharP); // juliana@230_30
   TC_appendJCharP = GETPROCADDRESS(appendJCharP); // juliana@230_30
   TC_areClassesCompatible = GETPROCADDRESS(areClassesCompatible);
   TC_createArrayObject = GETPROCADDRESS(createArrayObject);
   TC_createObject = GETPROCADDRESS(createObject);
   TC_createStringObjectFromCharP = GETPROCADDRESS(createStringObjectFromCharP);
   TC_createStringObjectFromTCHARP = GETPROCADDRESS(createStringObjectFromTCHARP);;
   TC_createStringObjectWithLen = GETPROCADDRESS(createStringObjectWithLen);
   TC_debug = GETPROCADDRESS(debug);
   TC_double2str = GETPROCADDRESS(double2str);
   TC_executeMethod = GETPROCADDRESS(executeMethod);
   TC_getApplicationId = GETPROCADDRESS(getApplicationId);
   TC_getAppPath = GETPROCADDRESS(getAppPath);
   //TC_getDataPath = GETPROCADDRESS(getDataPath);
   TC_getDateTime = GETPROCADDRESS(getDateTime);
   TC_getErrorMessage = GETPROCADDRESS(getErrorMessage);
   TC_getSettingsPtr = GETPROCADDRESS(getSettingsPtr);
   TC_getTimeStamp = GETPROCADDRESS(getTimeStamp);
   TC_hashCode = GETPROCADDRESS(hashCode);
   TC_hashCodeFmt = GETPROCADDRESS(hashCodeFmt);
   TC_heapAlloc = GETPROCADDRESS(heapAlloc);
   TC_heapDestroyPrivate = GETPROCADDRESS(heapDestroyPrivate);
   TC_hstrdup = GETPROCADDRESS(hstrdup);
   TC_htFree = GETPROCADDRESS(htFree);
   TC_htFreeContext = GETPROCADDRESS(htFreeContext);
   TC_htGet32 = GETPROCADDRESS(htGet32);
   TC_htGet32Inv = GETPROCADDRESS(htGet32Inv);
   TC_htGetPtr = GETPROCADDRESS(htGetPtr);
   TC_htNew = GETPROCADDRESS(htNew);
   TC_htPut32 = GETPROCADDRESS(htPut32);
   TC_htPut32IfNew = GETPROCADDRESS(htPut32IfNew);
   TC_htPutPtr = GETPROCADDRESS(htPutPtr);
   TC_htRemove = GETPROCADDRESS(htRemove);
   TC_int2CRID =  GETPROCADDRESS(int2CRID);
   TC_int2str = GETPROCADDRESS(int2str);
   TC_listFiles = GETPROCADDRESS(listFiles);
   TC_loadClass = GETPROCADDRESS(loadClass);
   TC_long2str = GETPROCADDRESS(long2str);
   TC_privateHeapCreate = GETPROCADDRESS(privateHeapCreate);
   TC_privateHeapSetJump = GETPROCADDRESS(privateHeapSetJump);
   TC_privateXfree = GETPROCADDRESS(privateXfree);
   TC_privateXmalloc = GETPROCADDRESS(privateXmalloc);
   TC_privateXrealloc = GETPROCADDRESS(privateXrealloc);
   TC_setObjectLock = GETPROCADDRESS(setObjectLock);
   TC_str2double = GETPROCADDRESS(str2double);
   TC_str2int = GETPROCADDRESS(str2int);
   TC_str2long = GETPROCADDRESS(str2long);
   TC_throwExceptionNamed = GETPROCADDRESS(throwExceptionNamed);
   TC_throwNullArgumentException = GETPROCADDRESS(throwNullArgumentException);
   TC_toLower = GETPROCADDRESS(toLower);
   TC_trace = GETPROCADDRESS(trace);
   TC_validatePath = GETPROCADDRESS(validatePath); // juliana@214_1
#ifdef ENABLE_MEMORY_TEST
   TC_getCountToReturnNull = GETPROCADDRESS(getCountToReturnNull);
   TC_setCountToReturnNull = GETPROCADDRESS(setCountToReturnNull);
#endif 
}

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
   ASSERT1_EQUALS(NotNull, TC_CharP2TCHARPBuf);
   ASSERT1_EQUALS(NotNull, TC_CharPToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharP);
   ASSERT1_EQUALS(NotNull, TC_JCharP2CharPBuf);
   ASSERT1_EQUALS(NotNull, TC_JCharP2TCHARPBuf);
	ASSERT1_EQUALS(NotNull, TC_JCharPEqualsJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPEqualsIgnoreCaseJCharP);
   ASSERT1_EQUALS(NotNull, TC_JCharPHashCode);
   ASSERT1_EQUALS(NotNull, TC_JCharPIndexOfJChar);
	ASSERT1_EQUALS(NotNull, TC_JCharPLen);
   ASSERT1_EQUALS(NotNull, TC_JCharToLower);
   ASSERT1_EQUALS(NotNull, TC_JCharToUpper);
   ASSERT1_EQUALS(NotNull, TCHARP2CharPBuf);
   ASSERT1_EQUALS(NotNull, TC_alert);
   ASSERT1_EQUALS(NotNull, TC_createArrayObject);
   ASSERT1_EQUALS(NotNull, TC_createObject);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromCharP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectFromTCHARP);
   ASSERT1_EQUALS(NotNull, TC_createStringObjectWithLen);
   ASSERT1_EQUALS(NotNull, TC_debug);
   ASSERT1_EQUALS(NotNull, TC_double2str);
   ASSERT1_EQUALS(NotNull, TC_executeMethod);
	ASSERT1_EQUALS(NotNull, TC_getApplicationId);
   ASSERT1_EQUALS(NotNull, TC_getAppPath);
   ASSERT1_EQUALS(NotNull, TC_getDataPath);
   ASSERT1_EQUALS(NotNull, TC_getDateTime);
	ASSERT1_EQUALS(NotNull, TC_getErrorMessage);
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
   ASSERT1_EQUALS(NotNull, TC_privateHeapCreate);
   ASSERT1_EQUALS(NotNull, TC_privateHeapSetJump);
   ASSERT1_EQUALS(NotNull, TC_privateXfree);
   ASSERT1_EQUALS(NotNull, TC_privateXmalloc);
   ASSERT1_EQUALS(NotNull, TC_privateXrealloc);
   ASSERT1_EQUALS(NotNull, TC_setObjectLock);
   ASSERT1_EQUALS(NotNull, TC_str2double);
   ASSERT1_EQUALS(NotNull, TC_str2int);
   ASSERT1_EQUALS(NotNull, TC_str2long);
   ASSERT1_EQUALS(NotNull, TC_throwExceptionNamed);
   ASSERT1_EQUALS(NotNull, TC_throwNullArgumentException);
   ASSERT1_EQUALS(NotNull, TC_tiF_create_sii);
   ASSERT1_EQUALS(NotNull, TC_toLower);
   ASSERT1_EQUALS(NotNull, TC_trace);
   ASSERT1_EQUALS(NotNull, TC_validatePath); // juliana@214_1

#ifdef ENABLE_MEMORY_TEST
   ASSERT1_EQUALS(NotNull, TC_getCountToReturnNull);
	ASSERT1_EQUALS(NotNull, TC_setCountToReturnNull);
#endif 

finish: ;
}

#endif
