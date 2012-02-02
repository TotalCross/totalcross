/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

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
 * Adds a value in the repeated key structure.
 *
 * @param context The thread context where the function is being executed.
 * @param key The repeated key whose repeated value is being inserted.
 * @param record The value record to be inserted in the key.
 * @param isWriteDelayed Indicates that this key will be dirty after calling this method and must be saved.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool keyAddValue(Context context, Key* key, int32 record, bool isWriteDelayed);

/**
 * Climbs on the key.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key being climbed.
 * @param markBits The rows which will be returned to the result set.
 * @return <code>-1</code> if an error occurs; <code>true</code>, otherwise.
 */
int32 defaultOnKey(Context context, Key* key, MarkBits* mmarkBits);

/**
 * Removes a value of the repeated key structure.
 *
 * @param context The thread context where the function is being executed.
 * @param key The key whose repeated value will be removed.
 * @param record The value record to be removed.
 * @return <code>REMOVE_SAVE_KEY</code>, <code>REMOVE_VALUE_ALREADY_SAVED</code>, or <code>REMOVE_ERROR</code>.
 * @throws DriverException If its not possible to find the key record to delete.
 */
int32 keyRemove(Context context, Key* key, int32 record);

/**
 * Compares two keys.
 *
 * @param key1 The first key to be compared.
 * @param key2 The second key to be compared.
 * @param isNull1 Indicates if the fist key is null.
 * @return 0 if the keys are identical; a positive number if <code>key1</code> keys are greater than <code>key2</code> keys; otherwise, a negative 
 * number.  
 */
int32 keyCompareTo(Key* key1, Key* key2, int32 size);

#endif
