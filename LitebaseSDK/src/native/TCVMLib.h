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
