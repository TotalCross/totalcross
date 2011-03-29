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

// $Id: TCVMLib.h,v 1.1.2.41 2011-01-25 14:53:04 juliana Exp $

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
