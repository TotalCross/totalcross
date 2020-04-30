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

// juliana@253_5: removed .idr files from all indices and changed its format.
/**
 * These functions generate the result set indexed rows map from the associated table indexes applied to the associated WHERE clause. They should 
 * only be used if the result set has a WHERE clause.
 */

#include "MarkBits.h"

/**
 * Resets the object and the bitmap.
 *
 * @param markBits The mark bits to be reseted.
 * @param bits A new bitmap for the mark bits. 
 */
void markBitsReset(MarkBits* markBits, IntVector* bits)
{
	TRACE("markBitsReset")
   markBits->isNoLongerEqual = false;
   markBits->bitValue = true;
   markBits->indexBitmap = bits;
   xmemzero(bits->items, bits->size << 2);
}

/**
 * Climbs on a key.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key to be climbed on.
 * @param markBits The rows which will be returned to the result set.
 * @return <code>false</code> if the key could be climbed; -1 if an error occurs, or <code>true</code>, otherwise.
 */
int32 onKey(Context context, Key* key, MarkBits* markBits)
{
	TRACE("onKey")
   Key* leftKey = &markBits->leftKey;
   SQLValue* keys0 = key->keys;
   Index* index = key->index;
   Table* table = index->table;
   PlainDB* plainDB = &table->db;
   XFile* dbo = &plainDB->dbo;
   int32 numberColumns = index->numberColumns,
         leftOp = *markBits->leftOp,
         rightOp = *markBits->rightOp,
         length = 0,
         size = *index->colSizes;

   // juliana@230_2: solved a possible crash with LIKE "...%"
   if (!keys0->length && size) // A strinhg may not be loaded.
   {
      nfSetPos(dbo, keys0->asInt); // Gets and sets the string position in the .dbo.
      
      // Fetches the string length and the string itself.
      if (!nfReadBytes(context, dbo, (uint8*)&length, 2) || !loadString(context, plainDB, keys0->asChars, keys0->length = length))
         return -1;
   }

   if (markBits->rightKey.index)
   {
      int32 comp = keyCompareTo(null, key, &markBits->rightKey, numberColumns, null);
      if (rightOp == OP_REL_LESS_EQUAL && comp > 0) // If key <= right key, stops. 
         return false;
      if (rightOp == OP_REL_LESS && comp >= 0) // if key < right key, stops.
         return false;
   }

   // For inclusion operations, just uses the value.
   if (leftOp == OP_REL_EQUAL || leftOp == OP_REL_GREATER_EQUAL || (leftOp == OP_REL_GREATER && markBits->isNoLongerEqual))
      onValue(key->record, markBits); // Climbs on the value.
   else if (leftOp == OP_REL_GREATER) // The key can still be equal.
   {
      if (keyCompareTo(null, leftKey, key, numberColumns, null))
      {
         markBits->isNoLongerEqual = true;
         onValue(key->record, markBits); // Climbs on the value.
      }
   }
   else // OP_PAT_MATCH_LIKE
   {
      JCharP patStr,
             valStr;
      JChar dateTimeBuf16[24];
		int32 valLen,
            type = *index->types;
		bool caseless = type == CHARS_NOCASE_TYPE;

      // juliana@230_3: corrected a bug of LIKE using DATE and DATETIME not returning the correct result.
      if (type == DATE_TYPE)
      {
         int32 asDate = keys0->asInt;
         date2JCharP(asDate / 10000, asDate / 100 % 100, asDate % 100, valStr = dateTimeBuf16);
         valLen = 10;
      }
      else if (type == DATETIME_TYPE)
      {
         int32 asDate = keys0->asDate,
               asTime = keys0->asTime;
         dateTime2JCharP(asDate / 10000, asDate / 100 % 100, asDate % 100, 
                         asTime / 10000000, asTime / 100000 % 100, asTime / 1000 % 100, asTime % 1000, valStr = dateTimeBuf16);
         valLen = 23;
      }
      else
      {
         valStr = keys0->asChars;
         valLen = keys0->length;
      }

      patStr = (keys0 = leftKey->keys)->asChars;
		if (str16StartsWith(valStr, patStr, valLen, keys0->length, 0, caseless)) // Only starts with are used with indices.
         onValue(key->record, markBits); // climb on the value.
      else
         return false;
   }
   return true; // Does not visit this value, but continues the search.
}

/**
 * Climbs on a value.
 *
 * @param record The record value to be climbed on.
 * @param markBits The rows which will be returned to the result set.
 */
void onValue(int32 record, MarkBits* markBits)
{
	TRACE("onValue")
   if (record != NO_VALUE)
   {   
      if (markBits->bitValue)
         markBits->indexBitmap->items[record >> 5] |= ((int32)1 << (record & 31));  // set
      else
         markBits->indexBitmap->items[record >> 5] &= ~((int32)1 << (record & 31)); // reset
   }
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests the correctnes of <code>markBitsOnValue()</code>.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(markBitsOnValue)
{
   MarkBits markBits;
   Heap heap = heapCreate();
   IntVector vector;
   int32 i = 8192;
   int32* array;
   UNUSED(currentContext)
   IF_HEAP_ERROR(heap)
   {
      heapDestroy(heap);
      TEST_FAIL(tc, "OutOfMemoryError");
      goto finish;
   }

   // Initializes the structures.
	xmemzero(&markBits, sizeof(MarkBits));
   xmemzero(&vector, sizeof(IntVector));
   array = TC_heapAlloc(heap, 32768);
	vector.length = 8192;
	vector.items = array;
   markBitsReset(&markBits, &vector);

   // Tests bit set and reset.
   while ((i -= 4) >= 0)
   {
      markBits.bitValue = 1;
      onValue(i, &markBits);
      ASSERT1_EQUALS(True, IntVectorisBitSet(markBits.indexBitmap, i));
      markBits.bitValue = 0;
      onValue(i, &markBits);
      ASSERT1_EQUALS(False, IntVectorisBitSet(markBits.indexBitmap, i));
   }

   heapDestroy(heap);
finish : ;
}

/**
 * Tests the correctnes of <code>markBitsReset()</code>.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(markBitsReset)
{
	MarkBits markBits; 
	IntVector vector;
	int32 array[3];
   UNUSED(currentContext)

	// Initializes the structures.
	xmemzero(&markBits, sizeof(MarkBits));
   xmemzero(&vector, sizeof(IntVector));
   xmemzero(array, 12);
	vector.length = 3;
	vector.items = array;

	// MarkBits is initialized with an empty vector.
	markBitsReset(&markBits, &vector);
   ASSERT1_EQUALS(True, markBits.bitValue);
	ASSERT1_EQUALS(False, markBits.isNoLongerEqual);
	ASSERT2_EQUALS(I32, 3, markBits.indexBitmap->length);
   ASSERT2_EQUALS(I32, 0, markBits.indexBitmap->size);

	// MarkBits is initialized with an unary vector.
	vector.items[0] = 1;
	vector.size = 1;
	markBitsReset(&markBits, &vector);
   ASSERT1_EQUALS(True, markBits.bitValue);
	ASSERT1_EQUALS(False, markBits.isNoLongerEqual);
	ASSERT2_EQUALS(I32, 3, markBits.indexBitmap->length);
   ASSERT2_EQUALS(I32, 1, markBits.indexBitmap->size);

	// MarkBits is initialized with a binary vector.
	vector.items[0] = vector.items[1] = 2;
	vector.size = 2;
	markBitsReset(&markBits, &vector);
   ASSERT1_EQUALS(True, markBits.bitValue);
	ASSERT1_EQUALS(False, markBits.isNoLongerEqual);
	ASSERT2_EQUALS(I32, 3, markBits.indexBitmap->length);
   ASSERT2_EQUALS(I32, 2, markBits.indexBitmap->size);

	// MarkBits is initialized with a ternary vector.
	vector.items[0] = vector.items[1] = vector.items[2] = 2;
	vector.size = 3;
	markBitsReset(&markBits, &vector);
   ASSERT1_EQUALS(True, markBits.bitValue);
	ASSERT1_EQUALS(False, markBits.isNoLongerEqual);
	ASSERT2_EQUALS(I32, 3, markBits.indexBitmap->length);
   ASSERT2_EQUALS(I32, 3, markBits.indexBitmap->size);

finish: ;
}

#endif
