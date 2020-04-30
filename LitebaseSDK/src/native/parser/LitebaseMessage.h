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
 * Declares the functions used to display Litebase error messages.
 */

#ifndef LITEBASE_LITEBASEMESSAGE_H
#define LITEBASE_LITEBASEMESSAGE_H

#include "Litebase.h"

/**
 * Initializes the error message arrays.
 */
void initLitebaseMessage(void);

/**
 * Gets the correct error message.
 * 
 * @param messageNumber The error message code.
 * @return The string with the desired error message.
 */
CharP getMessage(int32 messageNumber);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests that <code>getMessage()</code> returns the correct error message.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_getMessage(TestSuite* testSuite, Context currentContext);

/**
 * Tests that <code>initLitebaseMessage()</code> correctly initializes the error messages.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_initLitebaseMessage(TestSuite* testSuite, Context currentContext);

#endif

#endif
