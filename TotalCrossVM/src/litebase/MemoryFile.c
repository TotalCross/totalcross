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
 * Defines functions for a memory file, ie, a file that is allocated in memory and never dumped to disk. Used for result sets.
 */

#include "MemoryFile.h"

/**
 * Sets the position in the memory file.
 *
 * @param xFile The memory file.
 * @param position The new position in the buffer.
 */
void mfSetPos(XFile* xFile, int32 position)
{
	TRACE("mfSetPos")
   xFile->position = position;
}

/**
 * Increases or shrinks the buffer for the memory file.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @param newSize the new size of the buffer.
 * @return <code>true</code> if it was possible to allocate the new memory file buffer; <code>false</code>, otherwise.
 * @throws OutOfMemoryError If there is not enougth memory allocate memory. 
 */
bool mfGrowTo(Context context, XFile* xFile, uint32 newSize)
{
	TRACE("mfGrowTo")
   if ((xFile->fbuf = xrealloc(xFile->fbuf, newSize)))
   {
      xFile->size = newSize;
      return true;
   }
   TC_throwExceptionNamed(context, "java.lang.OutOfMemoryError", null);
   return false;
}

/**
 * Reads bytes from the buffers.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @param buffer The byte array to read data into.
 * @param count The number of bytes to read.
 * @return <code>true</code>.
 */
bool mfReadBytes(Context context, XFile* xFile, uint8* buffer, int32 count)
{
	TRACE("mfReadBytes")
	UNUSED(context)
   xmemmove(buffer, &xFile->fbuf[xFile->position], count);
   xFile->position += count;
   return true;
}

/**
 * Writes bytes into the buffer.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @param buffer The byte array to write data from.
 * @param count The number of bytes to write.
 * @return <code>true</code>.
 */
bool mfWriteBytes(Context context, XFile* xFile, uint8* buffer, int32 count)
{
	TRACE("mfWriteBytes")
	UNUSED(context)
   xmemmove(&xFile->fbuf[xFile->position], buffer, count);
   xFile->position += count;
   return true;
}

/**
 * Closes a memory file, by freeing its memory buffers.
 *
 * @param context The thread context where the function is being executed.
 * @param xFile The memory file.
 * @return Always <code>true</code>. It has a return value in order to be compatible with <code>nfClose()</code>. 
 */
bool mfClose(Context context, XFile* xFile)
{
	TRACE("mfClose")
	UNUSED(context)
   xfree(xFile->fbuf);
   return true;
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if <code>mfClose()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(mfClose)
{
   XFile file;
   int32 i = 2097152;

   file.fbuf = null; // Initializes the buffer.

   while ((i -= 2048) >= 0) // Tests if the buffer is really created and destroyed.
   {
      mfGrowTo(currentContext, &file, i);
      mfClose(currentContext, &file);
      ASSERT1_EQUALS(Null, file.fbuf);
   }

finish: ;
} 

/**
 * Tests if <code>mfGrowTo()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(mfGrowTo)
{
   XFile file;
   int32 i = 2097152;
   
   file.fbuf = null; // Initializes the buffer.

   while ((i -= 2048) >= 0) // Tests if the buffer is really grown.
   {
      mfGrowTo(currentContext, &file, i);
      ASSERT2_EQUALS(I32, i, file.size);
   }

   xfree(file.fbuf); // Destroys the buffer.

finish: ;
}

/**
 * Tests if <code>mfReadBytes()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(mfReadBytes)
{
   XFile file;
   int32 i = 2097152,
         count = 0;
   uint8* buffer1;
   uint8* buffer2;

   // Allocates the buffers.
   buffer1 = xmalloc(4096);
   buffer2 = xmalloc(4096);
   if (!buffer1 || !buffer2)
   {
      xfree(buffer1);
      xfree(buffer2);
      TEST_FAIL(tc, "OutOfMemoryError");
      goto finish;
   }

   // Initializes the memory file.
   file.fbuf = null; 
   file.position = 0;
   mfGrowTo(currentContext, &file, 16384);

   while ((i -= 2048) >= 0) // Writes data.
   {
      xmemset(buffer1, i % 255, count);
      ASSERT1_EQUALS(True, mfWriteBytes(currentContext, &file, buffer1, count));
      mfSetPos(&file, count);
      ASSERT1_EQUALS(True, mfReadBytes(currentContext, &file, buffer1, count));
      xmemset(buffer2, i % 255, count);
      ASSERT3_EQUALS(Block, buffer1, buffer2, count);
      mfSetPos(&file, ++count);
   }

   // Destroys the buffers.
   xfree(file.fbuf); 
   xfree(buffer1);
   xfree(buffer2);

finish: ;
}

/**
 * Tests if <code>mfSetPos()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(mfSetPos)
{
   XFile file;
   int32 i = 2097152;
   UNUSED(currentContext)

   while ((i -= 2048) >= 0) // Tests if the memory file position is really set.
   {
      mfSetPos(&file, i);
      ASSERT2_EQUALS(I32, i, file.position);
   }

finish: ;
}

/**
 * Tests if <code>mfWriteBytes()</code> works properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(mfWriteBytes)
{
   XFile file;
   int32 i = 2097152,
         count = 0;
   uint8* buffer1;
   uint8* buffer2;

   // Allocates the buffers.
   buffer1 = xmalloc(4096);
   buffer2 = xmalloc(4096);
   if (!buffer1 || !buffer2)
   {
      xfree(buffer1);
      xfree(buffer2);
      TEST_FAIL(tc, "OutOfMemoryError");
      goto finish;
   }

   // Initializes the memory file.
   file.fbuf = null; 
   file.position = 0;
   mfGrowTo(currentContext, &file, 16384);

   while ((i -= 2048) >= 0) // Writes data.
   {
      xmemset(buffer1, i % 255, count);
      ASSERT1_EQUALS(True, mfWriteBytes(currentContext, &file, buffer1, count));
      mfSetPos(&file, count);
      ASSERT1_EQUALS(True, mfReadBytes(currentContext, &file, buffer1, count));
      xmemset(buffer2, i % 255, count);
      ASSERT3_EQUALS(Block, buffer1, buffer2, count);
      mfSetPos(&file, ++count);
   }

   // Destroys the buffers.
   xfree(file.fbuf); 
   xfree(buffer1);
   xfree(buffer2);

finish: ;
}

#endif
