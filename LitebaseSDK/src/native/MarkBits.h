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
 * @param monkey A pointer to a structure used to transverse the index tree.
 * @return <code>false</code> if the key could be climbed; -1 if an error occurs, or <code>true</code>, otherwise.
 */
int32 markBitsOnKey(Context context, Key* key, Monkey* monkey);

/**
 * Climbs on a value.
 *
 * @param value The value to be climbed on.
 * @param monkey A pointer to a structure used to transverse the index tree.
 */
void markBitsOnValue(Val* value, Monkey* monkey);

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
