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
 * @param monkey A pointer to a structure used to transverse the index tree.
 * @return <code>false</code> if the key could be climbed; -1 if an error occurs, or <code>true</code>, otherwise.
 */
int32 markBitsOnKey(Context context, Key* key, Monkey* monkey)
{
	TRACE("markBitsOnKey")
   MarkBits* markBits = monkey->markBits;
   Key* leftKey = &markBits->leftKey;
   SQLValue* keys0 = key->keys;
   Index* index = key->index;
   Table* table = index->table;
   PlainDB* plainDB = table->db;
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
      
      // Fetches the string length.
      if (nfReadBytes(context, dbo, (uint8*)&length, 2) != 2 || !loadString(context, plainDB, keys0->asChars, keys0->length = length))
         return -1;
   }

   if (markBits->rightKey.index)
   {
      int32 comp = keyCompareTo(key, &markBits->rightKey, numberColumns);
      if (rightOp == OP_REL_LESS_EQUAL && comp > 0) // If key <= right key, stops. 
         return false;
      if (rightOp == OP_REL_LESS && comp >= 0) // if key < right key, stops.
         return false;
   }

   // For inclusion operations, just uses the value.
   if (leftOp == OP_REL_EQUAL || leftOp == OP_REL_GREATER_EQUAL || (leftOp == OP_REL_GREATER && markBits->isNoLongerEqual))
      return defaultOnKey(context, key, monkey); // Climbs on the values.

   if (leftOp == OP_REL_GREATER) // The key can still be equal.
   {
      if (keyCompareTo(leftKey, key, numberColumns))
      {
         markBits->isNoLongerEqual = true;
         return defaultOnKey(context, key, monkey); // Climbs on the values.
      }
   }
   else // OP_PAT_MATCH_LIKE
   {
      JCharP patStr,
             valStr;
      JChar dateTimeBuf16[24];
      DateTimeBuf dateTimeBuf;
		int32 valLen,
            type = *index->types;
		bool caseless = type == CHARS_NOCASE_TYPE;

      // juliana@230_3: corrected a bug of LIKE using DATE and DATETIME not returning the correct result.
      if (type == DATE_TYPE)
      {
         int32 asDate = keys0->asInt;
         xstrprintf(dateTimeBuf, "%04d/%02d/%02d", asDate / 10000, asDate / 100 % 100, asDate % 100);
         valStr = TC_CharP2JCharPBuf(dateTimeBuf, valLen = 10, dateTimeBuf16, true);
      }
      else if (type == DATETIME_TYPE)
      {
         int32 asDate = keys0->asDate,
               asTime = keys0->asTime;
         xstrprintf(dateTimeBuf, "%04d/%02d/%02d", asDate / 10000, asDate / 100 % 100, asDate % 100);
         xstrprintf(&dateTimeBuf[11], "%02d:%02d:%02d:%03d", asTime / 10000000, asTime / 100000 % 100, asTime / 1000 % 100, asTime % 1000);
         dateTimeBuf[10] = ' ';
         valStr = TC_CharP2JCharPBuf(dateTimeBuf, valLen = 23, dateTimeBuf16, true);
      }
      else
      {
         valStr = keys0->asChars;
         valLen = keys0->length;
      }

      patStr = (keys0 = leftKey->keys)->asChars;
		if (str16StartsWith(valStr, patStr, valLen, keys0->length, 0, caseless)) // Only starts with are used with indices.
         return defaultOnKey(context, key, monkey); // climb on the values
      return false;
   }
   return true; // Does not visit this value, but continues the search.
}

/**
 * Climbs on a value.
 *
 * @param record The record value to be climbed on.
 * @param monkey A pointer to a structure used to transverse the index tree.
 */
void markBitsOnValue(int32 record, Monkey* monkey)
{
	TRACE("markBitsOnValue")
   MarkBits* markBits = monkey->markBits;
   if (markBits->bitValue)
      markBits->indexBitmap->items[record >> 5] |= ((int32)1 << (record & 31));  // set
   else
      markBits->indexBitmap->items[record >> 5] &= ~((int32)1 << (record & 31)); // reset
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
   Monkey monkey;
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
   monkey.markBits = &markBits;

   // Tests bit set and reset.
   while ((i -= 4) >= 0)
   {
      monkey.markBits->bitValue = 1;
      markBitsOnValue(i, &monkey);
      ASSERT1_EQUALS(True, IntVectorisBitSet(monkey.markBits->indexBitmap, i));
      monkey.markBits->bitValue = 0;
      markBitsOnValue(i, &monkey);
      ASSERT1_EQUALS(False, IntVectorisBitSet(monkey.markBits->indexBitmap, i));
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
