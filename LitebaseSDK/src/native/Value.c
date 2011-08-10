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
 * Defines functions for dealing with repeated values in an index.
 */

#include "Value.h"

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
int32 valueSaveNew(Context context, XFile* fvalues, int32 record, int32 next, bool isWriteDelayed)
{
   TRACE("valueSaveNew")
	int32 idx = fvalues->finalPos / VALUERECSIZE; // Links that value.

   if (isWriteDelayed)
   {
      if (!(idx & (RECGROWSIZE - 1)) && !nfGrowTo(context, fvalues, (idx + RECGROWSIZE) * VALUERECSIZE)) // Grows more than 1 value per time.
		   return -1;
   }
   else if (!nfGrowTo(context, fvalues, (idx + 1) * VALUERECSIZE)) // Opens space for the value.
         return -1;
      
   fvalues->finalPos = (idx + 1) * VALUERECSIZE;
   nfSetPos(fvalues, idx * VALUERECSIZE); // Seeks write position.
   
   // Adds the new value.
   return valueSave(context, record, next, fvalues)? idx : -1;
}

/**
 * Loads a value from the .idr file.
 *
 * @param context The thread context where the function is being executed.
 * @param record The record being loaded.
 * @param next The next record to the one being loaded.
 * @param fvalues The .idr file.
 * @return <code>false</code> if an error occured when manipulating the index file; <code>true</code>, otherwise.
 */
bool valueLoad(Context context, int32* record, int32* next, XFile* fvalues)
{
	TRACE("valueLoad")
   uint8 valueBuf[VALUERECSIZE];
   if (nfReadBytes(context, fvalues, valueBuf, 6)) // Reads the value.
   {
      // Calculates the record and the next repeated value.
      *record = read24(valueBuf);
      *next = read24(&valueBuf[3]);
      return true;
   }
   return false;
}

/**
 * Saves a value to the .idr file.
 *
 * @param context The thread context where the function is being executed.
 * @param record The record to be saved.
 * @param next The next record to the one being saved.
 * @param fvalues The .idr file.
 * @return <code>false</code> if an error occured when manipulating the index file; <code>true</code>, otherwise.
 */
bool valueSave(Context context, int32 record, int32 next, XFile* fvalues)
{
	TRACE("valueSave")
   uint8 valueBuf[VALUERECSIZE];

   // Stores the record and the next repeated value in the buffer.
   write24(valueBuf, record);
   write24(&valueBuf[3], next);

   return nfWriteBytes(context, fvalues, valueBuf, 6); // Writes the value.
}

/**
 * Reads 3 bytes or 24 bits from a buffer as an integer.
 *
 * @param buffer The buffer being read.
 * @return The integer represented by the 3 bytes.
 */ 
int32 read24(uint8* buffer)
{
	TRACE("read24")
   int32 value = 0;
   uint8* dest = (uint8*)&value;
   dest[3] = 0;
   dest[2] = buffer[0];
   dest[1] = buffer[1];
   dest[0] = buffer[2];
   return value;
}

/**
 * Writes an integer in a 3-byte or 24-bits buffer.
 *
 * @param buffer The buffer being written.
 * @param value A integer which only used 3 bytes.
 */ 
void write24(uint8* buffer, int32 value)
{
	TRACE("write24")
   uint8* src = (uint8*)&value;
   buffer[2] = src[0];
   buffer[1] = src[1];
   buffer[0] = src[2];
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if <code>read24()</code> reads a 3-byte number from a buffer correctly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(read24)
{
   uint8 buffer[3];
   int32 i = 16777216;
   UNUSED(currentContext)

   while ((i -= 2048) >= 0)
   {
      xmemzero(buffer, 3);
      write24(buffer, i);
      ASSERT2_EQUALS(I32, i, read24(buffer));
   }

finish: ;
}  

/**
 * Tests if <code>ValueLoad()</code> correctly loads a value stored in a .idr file.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(valueLoad)
{
   int32 i = 16777216,
         record, 
         next;
   XFile file;
   char sourcePath[MAX_PATHNAME];

   // Creates the file.
#ifdef PALMOS
   ASSERT2_EQUALS(I32, 1, checkApppath(currentContext, sourcePath, null));
#else
   ASSERT2_EQUALS(I32, -1, checkApppath(currentContext, sourcePath, null));
#endif
   ASSERT1_EQUALS(True, nfCreateFile(currentContext, "teste.idr", true, sourcePath, 1, &file, -1));
   
   while ((i -= 2048) >= 0) // Stores the values.
      ASSERT1_EQUALS(True, valueSave(currentContext, i, i, &file));

   nfSetPos(&file, 0);
   i = 16777216;

   while ((i -= 2048) >= 0) // Loads the values and checks if they were save correctly.
   {
      ASSERT1_EQUALS(True, valueLoad(currentContext, &record, &next, &file));
      ASSERT2_EQUALS(I32, next, i);
      ASSERT2_EQUALS(I32, record, i);
   }

   ASSERT1_EQUALS(True, nfRemove(currentContext, &file, sourcePath, 1)); // Removes the file.

finish: ;
}

/**
 * Tests if <code>ValueSave()</code> correctly saves a value in a .idr file.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(valueSave)
{
   int32 i = 16777216,
         record,
         next;
   XFile file;
   char sourcePath[MAX_PATHNAME];

   // Creates the file.
#ifdef PALMOS
   ASSERT2_EQUALS(I32, 1, checkApppath(currentContext, sourcePath, null));
#else
   ASSERT2_EQUALS(I32, -1, checkApppath(currentContext, sourcePath, null));
#endif
   ASSERT1_EQUALS(True, nfCreateFile(currentContext, "teste.idr", true, sourcePath, 1, &file, -1));
   
   while ((i -= 2048) >= 0) // Stores the values.
      ASSERT1_EQUALS(True, valueSave(currentContext, i, i, &file));

   nfSetPos(&file, 0);
   i = 16777216;

   while ((i -= 2048) >= 0) // Loads the values and checks if they were save correctly.
   {
      ASSERT1_EQUALS(True, valueLoad(currentContext, &record, &next, &file));
      ASSERT2_EQUALS(I32, next, i);
      ASSERT2_EQUALS(I32, record, i);
   }

   ASSERT1_EQUALS(True, nfRemove(currentContext, &file, sourcePath, 1)); // Removes the file.

finish: ;
} 

/**
 * Tests if <code>ValueSaveNew()</code> correctly saves a new value in a .idr file.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(valueSaveNew)
{
   int32 i = 16777216,
         j = 0,
         record,
         next;
   XFile file;
   char sourcePath[MAX_PATHNAME];

   // Creates the file.
#ifdef PALMOS
   ASSERT2_EQUALS(I32, 1, checkApppath(currentContext, sourcePath, null));
#else
   ASSERT2_EQUALS(I32, -1, checkApppath(currentContext, sourcePath, null));
#endif
   ASSERT1_EQUALS(True, nfCreateFile(currentContext, "teste.idr", true, sourcePath, 1, &file, -1));

   while ((i -= 2048) >= 0) // Stores the values.
      ASSERT2_EQUALS(I32, j++, valueSaveNew(currentContext, &file, i, i, true));

   nfSetPos(&file, 0);
   i = 16777216;

   while ((i -= 2048) >= 0) // Loads the values and checks if they were save correctly.
   {
      ASSERT1_EQUALS(True, valueLoad(currentContext, &record, &next, &file));
      ASSERT2_EQUALS(I32, next, i);
      ASSERT2_EQUALS(I32, record, i);
   }

   ASSERT1_EQUALS(True, nfRemove(currentContext, &file, sourcePath, 1)); // Removes the file.
 
   i = 16777216;
   j = 0;

   // Creates the file.
#ifdef PALMOS
   ASSERT2_EQUALS(I32, 1, checkApppath(currentContext, sourcePath, null));
#else
   ASSERT2_EQUALS(I32, -1, checkApppath(currentContext, sourcePath, null));
#endif
   ASSERT1_EQUALS(True, nfCreateFile(currentContext, "teste.idr", true, sourcePath, 1, &file, -1));

   while ((i -= 2048) >= 0) // Stores the values.
      ASSERT2_EQUALS(I32, j++, valueSaveNew(currentContext, &file, i, i, false));

   nfSetPos(&file, 0);
   i = 16777216;

   while ((i -= 2048) >= 0) // Loads the values and checks if they were save correctly.
   {
      ASSERT1_EQUALS(True, valueLoad(currentContext, &record, &next, &file));
      ASSERT2_EQUALS(I32, next, i);
      ASSERT2_EQUALS(I32, record, i);
   }

   ASSERT1_EQUALS(True, nfRemove(currentContext, &file, sourcePath, 1)); // Removes the file.
 
finish : ;
}

/**
 * Tests if <code>write24()</code> writes a 3-byte number into a buffer correctly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(write24)
{
   uint8 buffer[3];
   int32 i = 16777216;
   UNUSED(currentContext)

   while ((i -= 2048) >= 0)
   {
      xmemzero(buffer, 3);
      write24(buffer, i);
      ASSERT2_EQUALS(I32, i, read24(buffer));
   }

finish: ;
} 

#endif
