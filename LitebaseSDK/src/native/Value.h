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
 * Declares functions for dealing with repeated values in an index.
 */

#ifndef LITEBASE_VALUES_H
#define LITEBASE_VALUES_H

#include "Litebase.h"

/**
 * Saves a new repeated value.
 *
 * @param context The thread context where the function is being executed.
 * @param fvalues The .idr file pointer.
 * @param record The new record to be saved.
 * @param next The next record to the one being saved.
 * @param isWriteDelayed Indicates if the index is to be saved now or later in the file.
 * @return <code>valRec</code> or -1 if an error occured when manipulating the index file.
 */
int32 valueSaveNew(Context context, XFile* fvalues, int32 record, int32 next, bool isWriteDelayed);

/**
 * Loads a value from the .idr file.
 *
 * @param context The thread context where the function is being executed.
 * @param value A pointer to the value to be loaded.
 * @param fvalues The .idr file.
 * @return <code>false</code> if an error occured when manipulating the index file; <code>true</code>, otherwise.
 */
bool valueLoad(Context context, Val* value, XFile* fvalues);

/**
 * Saves a value to the .idr file.
 *
 * @param context The thread context where the function is being executed.
 * @param value A pointer to the value to be loaded.
 * @param fvalues The .idr file.
 * @return <code>false</code> if an error occured when manipulating the index file; <code>true</code>, otherwise.
 */
bool valueSave(Context context, Val* value, XFile* fvalues);

/**
 * Reads 3 bytes or 24 bits from a buffer as an integer.
 *
 * @param buffer The buffer being read.
 * @return The integer represented by the 3 bytes.
 */ 
int32 read24(uint8* buffer);

/**
 * Writes an integer in a 3-byte or 24-bits buffer.
 *
 * @param buffer The buffer being written.
 * @param value A integer which only used 3 bytes.
 */ 
void write24(uint8* buffer, int32 value);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if <code>read24()</code> reads a 3-byte number from a buffer correctly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_read24(TestSuite* testSuite, Context currentContext);   

/**
 * Tests if <code>ValueLoad()</code> correctly loads a value stored in a .idr file.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_valueLoad(TestSuite* testSuite, Context currentContext); 

/**
 * Tests if <code>ValueSave()</code> correctly saves a value in a .idr file.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_valueSave(TestSuite* testSuite, Context currentContext); 

/**
 * Tests if <code>ValueSaveNew()</code> correctly saves a new value in a .idr file.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_valueSaveNew(TestSuite* testSuite, Context currentContext);

/**
 * Tests if <code>write24()</code> writes a 3-byte number into a buffer correctly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_write24(TestSuite* testSuite, Context currentContext);   

#endif

#endif
