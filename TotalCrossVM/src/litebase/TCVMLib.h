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
 * Declares the function used to load the pointers to TotalCross functions used by Litebase.
 */

#ifndef LITEBASE_TCVMLIB_H
#define LITEBASE_TCVMLIB_H

#include "Litebase.h"

/**
 * Initializes the pointers to TotalCross functions used by Litebase. 
 */
void initTCVMLib(void);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the TotalCross functions were loaded successfully.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_initTCVMLib(TestSuite* testSuite, Context currentContext);// TCVMLib_test.h

#endif

#endif
