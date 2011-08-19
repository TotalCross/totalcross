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
   key->valRec = NO_VALUE; // The record key is not stored yet.
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
   to->valRec = from->valRec;
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
   PlainDB* plainDB = index->table->db;
   SQLValue* keys = key->keys;
   SQLValue* keyAux;
   int32* types = index->types;
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
   xmove4(&key->valRec, dataStream); // Reads the number that represents the record.
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
   int32* types = index->types;
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
   xmove4(dataStream, &key->valRec); // Writes the number that represents the record.
   return dataStream + 4;
}

/**
 * Adds a value in the repeated key structure.
 *
 * @param context The thread context where the function is being executed.
 * @param key The repeated key whose repeated value is being inserted.
 * @param record The value record to be inserted in the key.
 * @param isWriteDelayed Indicates that this key will be dirty after calling this method and must be saved.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool keyAddValue(Context context, Key* key, int32 record, bool isWriteDelayed)
{
	TRACE("keyAddValue")

   // This key will be dirty after calling this function and must be saved.
   if (key->valRec == NO_VALUE) // First value being stored? Store it in the valRec as the negative.
      key->valRec = -record - 1; // 0 is a valid record number, and also a valid value; so it is necessary to make a difference.
   else
   {
      Index* index = key->index;
      Table* table = index->table;
		if (!fileIsValid(index->fvalues.file))
      {
         char name[DBNAME_SIZE];
			xstrcpy(name, index->fnodes.name);
			name[xstrlen(name) - 1] = 'r';
	      if (!nfCreateFile(context, name, true, table->sourcePath, table->slot, &index->fvalues, CACHE_INITIAL_SIZE))
            return false;
			if (!tableSaveMetaData(context, table, TSMD_EVERYTHING))
            return false;
      }
      if (key->valRec < 0) // Is this the first repetition of the key? If so, it is necessary to move the value stored here to the values file.
      {
         if ((key->valRec = valueSaveNew(context, &index->fvalues,-key->valRec -1, NO_MORE, isWriteDelayed)) == -1)
            return false;
      }
      
      // Links to the next value and stores the value record.
      if ((key->valRec = valueSaveNew(context, &index->fvalues, record, key->valRec, isWriteDelayed)) == -1) 
         return false;
   }
   return true;
}

/**
 * Climbs on the key.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key being climbed.
 * @param monkey Used to climb on the values of the key.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
int32 defaultOnKey(Context context, Key* key, Monkey* monkey)
{
	TRACE("defaultOnKey")
   int32 idx = key->valRec;

   if (idx == NO_VALUE)
      return true;
   if (idx < 0) // If there are no values, there is nothing to be done.
      monkey->onValue(-idx - 1, monkey);
   else // Is it a value with no repetitions?
   {
      XFile* fvalues = &key->index->fvalues;
      monkeyOnValueFunc onValue = monkey->onValue;
      int32 record,
            next;

      while (idx != NO_MORE) // If there are repetitions, climbs on all the values.
      {
         nfSetPos(fvalues, VALUERECSIZE * idx);
         if (!valueLoad(context, &record, &next, fvalues))
            return -1;
         onValue(record, monkey);
         idx = next;
      }
   }
   return true;
}

/**
 * Removes a value of the repeated key structure.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key whose repeated value will be removed.
 * @param record The value record to be removed.
 * @return <code>REMOVE_SAVE_KEY</code>, <code>REMOVE_VALUE_ALREADY_SAVED</code>, or <code>REMOVE_ERROR</code>.
 * @throws DriverException If its not possible to find the key record to delete.
 */
int32 keyRemove(Context context, Key* key, int32 record)
{
	TRACE("keyRemove")
   int32 idx = key->valRec;

   if (idx != NO_VALUE)
   {
      if (idx < 0) // Is it a value with no repetitions?
      {
         if (record == -idx - 1) // If this is the record, all that is done is to set the key as empty.
         {
            key->valRec = NO_VALUE;
            return REMOVE_SAVE_KEY;
         }
      }
      else // Otherwise, it is necessary to find the record.
      {
         int32 lastRecord = -1,
               lastNext,
               auxRecord;
         XFile* fvalues = &key->index->fvalues;
         int32 lastPos = 0,
               pos;

         while (idx != NO_MORE)
         {
            nfSetPos(fvalues, pos = VALUERECSIZE * idx);
            if (!valueLoad(context, &auxRecord, &idx, fvalues))
               return REMOVE_ERROR;

            if (auxRecord == record)
            {
               if (lastRecord == -1) // The value removed is the last one.
               {
                  key->valRec = idx;
                  return REMOVE_SAVE_KEY;
               }
               else
               {
                  lastNext = idx;
                  nfSetPos(fvalues, lastPos);
                  if (!valueSave(context, lastRecord, lastNext, fvalues))
                     return REMOVE_ERROR;
                  return REMOVE_VALUE_ALREADY_SAVED;
               }
            }

            // Sets a new last value.
            lastPos = pos;
            lastRecord = auxRecord;
            lastNext = idx;
         }
      }
   }
   TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_IDX_RECORD_DEL));
   return REMOVE_ERROR;
}

/**
 * Compares two keys.
 *
 * @param key1 The first key to be compared.
 * @param key2 The second key to be compared.
 * @param isNull1 Indicates if the fist key is null
 */
int32 keyCompareTo(Key* key1, Key* key2, int32 size)
{
	TRACE("keyCompareTo")
   int32 r, 
         i = -1;
   int32* types = key1->index->types;
   SQLValue* keys1 = key1->keys;
   SQLValue* keys2 = key2->keys;
   
   while (++i < size) // Compares each key of the key. If a pair is not equal to each other, returns.
      if ((r = valueCompareTo(&keys1[i], &keys2[i], types[i], false, false)) != 0)
         return r;

   return 0;
}
