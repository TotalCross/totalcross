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
 * Declares functions for a memory file, ie, a file that is allocated in memory and never dumped to disk. Used for result sets.
 */

#ifndef LITEBASE_MEMORY_FILE_H
#define LITEBASE_MEMORY_FILE_H

#include "Litebase.h"

/**
 * Sets the position in the memory file.
 *
 * @param xFile The memory file.
 * @param position The new position in the buffer.
 */
void mfSetPos(XFile* xFile, int32 position);

/**
 * Increases or shrinks the buffer for the memory file.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @param newSize the new size of the buffer.
 * @return <code>true</code> if it was possible to allocate the new memory file buffer; <code>false</code>, otherwise.
 * @throws OutOfMemoryError If there is not enougth memory allocate memory. 
 */
bool mfGrowTo(Context context, XFile* xFile, uint32 newSize);

/**
 * Reads bytes from the buffers.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @param buffer The byte array to read data into.
 * @param count The number of bytes to read.
 * @return <code>true</code>.
 */
bool mfReadBytes(Context context, XFile* xFile, uint8* buffer, int32 count);

/**
 * Writes bytes into the buffer.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @param buffer The byte array to write data from.
 * @param count The number of bytes to write.
 * @return <code>true</code>.
 */
bool mfWriteBytes(Context context, XFile* xFile, uint8* buffer, int32 count);

/**
 * Closes a memory file, by freeing its memory buffers.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @return Always <code>true</code>. It has a return value in order to be compatible with <code>nfClose()</code>. 
 */
bool mfClose(Context context, XFile* xFile);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if <code>mfClose()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_mfClose(TestSuite* testSuite, Context currentContext);   

/**
 * Tests if <code>mfGrowTo()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_mfGrowTo(TestSuite* testSuite, Context currentContext);  

/**
 * Tests if <code>mfReadBytes()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_mfReadBytes(TestSuite* testSuite, Context currentContext);

/**
 * Tests if <code>mfSetPos()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_mfSetPos(TestSuite* testSuite, Context currentContext); 

/**
 * Tests if <code>mfWriteBytes()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_mfWriteBytes(TestSuite* testSuite, Context currentContext);

#endif

#endif
