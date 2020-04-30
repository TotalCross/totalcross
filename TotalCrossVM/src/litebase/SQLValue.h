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
 * Declares functions to deal with a a value which can be inserted in a column of a table.
 */

#ifndef LITEBASE_SQLVALUE_H
#define LITEBASE_SQLVALUE_H

#include "Litebase.h"

/**
 * Creates an array of <code>SQLValue</code>s.
 * 
 * @param count The array size.
 * @param heap The heap to allocate the array.
 * @return The <code>SQLValue</code> array.
 */
SQLValue** newSQLValues(int32 count, Heap heap);

/**
 * Applies the function on the value. 
 * 
 * @param value The value where the function will be applied.
 * @param sqlFunction The code of the function to be applied.
 * @param paramDataType The data type of the parameter.
 */
void applyDataTypeFunction(SQLValue* value, int32 sqlFunction, int32 paramDataType); 

/**
 * Compares 2 values.
 *
 * @param context The thread context where the function is being executed. 
 * @param value1 The fist value used in the comparison.
 * @param value1 The second value used in the comparison.
 * @param type The types of the values being compared.
 * @param isNull1 Indicates if the value being compared is null.
 * @param isNull2 Indicates if the value being compared against is null.
 * @param plainDB the plainDB of a table if it is necessary to load a string.
 * @return 0 if the values are identical; a positive number if the value being compared is greater than the one being compared against; otherwise,
 * a negative number.
 */
int32 valueCompareTo(Context context, SQLValue* value1, SQLValue* value2, int32 type, bool isNull1, bool isNull2, PlainDB* plainDB);

#ifdef ENABLE_TEST_SUITE

/**
 * Checks if <code>applyDataTypeFunction()<code> correctly applies the data type functions.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_applyDataTypeFunction(TestSuite* testSuite, Context currentContext);

/**
 * Checks if <code>newSQLValues()<code> correctly creates an array of SQLValues.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_newSQLValues(TestSuite* testSuite, Context currentContext);

/**
 * Tests if <code>valueCompareTo</code> correctly compares <code>SQLValues</code>.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_valueCompareTo(TestSuite* testSuite, Context currentContext);

/**
 * Gets the next time instant, increasing second by one and adjusting the other time values.
 *
 * @param hour The hour.
 * @param minute The minute.
 * @param second The second.
 */
void getNextTime(int32* hour, int32* minute, int32* second);

/**
 * Gets the next day, increasing day by one and adjusting the other date values.
 *
 * @param year The year.
 * @param month The month.
 * @param day The day.
 */
void getNextDate(int32* year, int32* month, int32* day);

#endif

#endif
