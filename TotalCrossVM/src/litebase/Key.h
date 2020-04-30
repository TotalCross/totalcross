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
 * Declares functions to deal with the key of a record. It may be any of the SQL types.
 */

#ifndef LITEBASE_KEY_H
#define LITEBASE_KEY_H

#include "Litebase.h"
#include "LitebaseTypes.h"

/**
 * Sets a key of an index.
 *
 * @param key Yhe the index key to be set.
 * @param SQLValues The values used to set the index key.
 * @param index The index.
 * @param size The number of columns of the index.
 */
void keySet(Key* key, SQLValue** SQLValues, Index* index, int32 size);

/**
 * Sets a key using another key.
 *
 * @param to The destination key.
 * @param from The key used to set the other key.
 */
void keySetFromKey(Key* to, Key* from);

/**
 * Loads a key.
 *
 * @param key The key to be loaded.
 * @param dataStream The data stream where the record to be read to find the key value stored.
 * @return The dataStream offset of the number of bytes read.
 */
uint8* keyLoad(Key* key, uint8* dataStream);

/**
 * Saves a key.
 *
 * @param key The key to be saved.
 * @param dataStream The data stream where the record to be read to find the key value stored.
 * @return The dataStream offset of the number of bytes written.
 */
uint8* keySave(Key* key, uint8* dataStream);

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
int32 keyCompareTo(Context context, Key* key1, Key* key2, int32 size, PlainDB* plainDB);

#endif
