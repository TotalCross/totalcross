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

// juliana@noidr_1: removed .idr files from all indices and changed its format.
/**
 * These functions generate the result set indexed rows map from the associated table indexes applied to the associated WHERE clause. They should 
 * only be used if the result set has a WHERE clause.
 */

#ifndef LITEBASE_MARKBITS_H
#define LITEBASE_MARKBITS_H

#include "Litebase.h"

/**
 * Resets the object and the bitmap.
 *
 * @param markBits The mark bits to be reseted.
 * @param bits A new bitmap for the mark bits. 
 */
void markBitsReset(MarkBits* markBits, IntVector* bits);

/**
 * Climbs on a key.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key to be climbed on.
 * @param markBits The rows which will be returned to the result set.
 * @return <code>false</code> if the key could be climbed; -1 if an error occurs, or <code>true</code>, otherwise.
 */
int32 onKey(Context context, Key* key, MarkBits* markBits);

/**
 * Climbs on a value.
 *
 * @param record The record value to be climbed on.
 * @param markBits The rows which will be returned to the result set.
 */
void onValue(int32 record, MarkBits* markBits);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests the correctnes of <code>markBitsOnValue()</code>.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_markBitsOnValue(TestSuite* testSuite, Context currentContext);

/**
 * Tests the correctnes of <code>markBitsReset()</code>.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_markBitsReset(TestSuite* testSuite, Context currentContext);

#endif

#endif
