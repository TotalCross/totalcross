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
 * Defines functions to deal with the key of a record. It may be any of the SQL types.
 */

#include "Key.h"

/**
 * Sets a key of an index.
 *
 * @param key Yhe the index key to be set.
 * @param SQLValues The values used to set the index key.
 * @param index The index.
 * @param size The number of columns of the index.
 */
void keySet(Key* key, SQLValue** SQLValues, Index* index, int32 size)
{
	TRACE("keySet")
   key->index = index;

	// juliana@202_3: Solved a bug that could cause a GPF when using composed indices.
   while (--size >= 0)
      key->keys[size] = *SQLValues[size];
   key->record = NO_VALUE; // The record key is not stored yet.
}

/**
 * Sets a key using another key.
 *
 * @param to The destination key.
 * @param from The key used to set the other key.
 */
void keySetFromKey(Key* to, Key* from)
{
	TRACE("keySetFromKey")
   Index* index = to->index;
   SQLValue* toKeys = to->keys;
   SQLValue* fromKeys = from->keys;
   SQLValue* fromKey;
   SQLValue* toKey;
   int32* sizes = index->colSizes;
   int32 i = index->numberColumns;
   
   while (--i >= 0)
   {
      fromKey = &fromKeys[i];
      toKey = &toKeys[i];
      if (!sizes[i])
         xmemmove(toKey, fromKey, sizeof(SQLValue)); // full copy
      else
      {
			// juliana@202_8: corrected a bug that would cause string indices to be built incorrectly if they had more than one null value.
			if (fromKey->asChars)
			   xmemmove(toKey->asChars, fromKey->asChars, (toKey->length = fromKey->length) << 1);
			else
            *toKey->asChars = toKey->length = 0;

         toKey->asInt = fromKey->asInt;
      }
   }
   to->index = from->index;
   to->record = from->record;
}

/**
 * Loads a key.
 *
 * @param key The key to be loaded.
 * @param dataStream The data stream where the record to be read to find the key value stored.
 * @return The dataStream offset of the number of bytes read.
 */
uint8* keyLoad(Key* key, uint8* dataStream)
{
	TRACE("keyLoad")
   Index* index = key->index;
   PlainDB* plainDB = &index->table->db;
   SQLValue* keys = key->keys;
   SQLValue* keyAux;
   int8* types = index->types;
   int32* sizes = index->colSizes;
   int32 i = -1,
         n = index->numberColumns,
         pos;

   while (++i < n)
   {
      keyAux = &keys[i];
      if (sizes[i]) // String keys are not stored in the indices. Only their pointer is stored.
      {
         xmove4(&pos, dataStream);
         dataStream += 4;
         if (pos != keyAux->asInt) // If the position is the same, the string is already loaded.
			{
				keyAux->asInt = pos;
				keyAux->length = 0;
				keyAux->asChars[0] = 0;
			}
      }
      else
      {
         // juliana@230_12
         // Must pass true to isTemporary so that the method does not think that the number is a rowid.
         // If the value read is null, some bytes must be skipped in the stream.
         // Note: since we're writing only primitive types, we can use any PlainDB available.
         readValue(null, plainDB, keyAux, 0, types[i], dataStream, true, false, false, -1, null);
         dataStream += typeSizes[types[i]]; 
      }
   }
   xmove4(&key->record, dataStream); // Reads the number that represents the record.
   return dataStream + 4;
}

/**
 * Saves a key.
 *
 * @param key The key to be saved.
 * @param dataStream The data stream where the record to be read to find the key value stored.
 * @return The dataStream offset of the number of bytes written.
 */
uint8* keySave(Key* key, uint8* dataStream)
{
	TRACE("keySave")
   Index* index = key->index;
   SQLValue* keys = key->keys;
   int8* types = index->types;
   int32* sizes = index->colSizes;
	int32 i = -1,
         n = index->numberColumns;

   while (++i < n)
   {
      if (sizes[i]) 
      {
         xmove4(dataStream, &keys[i].asInt); // Saves only the string position in the .dbo.
         dataStream += 4;
      }
      else
      {
         // If the key is not a string, stores its value in the index file.
         // Note: since primitive types are being written, it is possible to use any PlainDB available.
         writeValue(null, null, &keys[i], dataStream, types[i], 0, true, true, false, false);
         dataStream += typeSizes[types[i]];
      }
   }

   xmove4(dataStream, &key->record); // Writes the number that represents the record.
   return dataStream + 4;
}

/**
 * Compares two keys.
 *
 * @param context The thread context where the function is being executed.
 * @param key1 The first key to be compared.
 * @param key2 The second key to be compared.
 * @param isNull1 Indicates if the fist key is null.
 * @param plainDB the plainDB of a table if it is necessary to load a string.
 * @return 0 if the keys are identical; a positive number if <code>key1</code> keys are greater than <code>key2</code> keys; otherwise, a negative 
 * number.
 */
int32 keyCompareTo(Context context, Key* key1, Key* key2, int32 size, PlainDB* plainDB)
{
	TRACE("keyCompareTo")
   int32 r, 
         i = -1;
   int8* types = key1->index->types;
   SQLValue* keys1 = key1->keys;
   SQLValue* keys2 = key2->keys;
   
   while (++i < size) // Compares each key of the key. If a pair is not equal to each other, returns.
      if ((r = valueCompareTo(context, &keys1[i], &keys2[i], types[i], false, false, plainDB)) != 0)
         return r;

   return 0;
}
