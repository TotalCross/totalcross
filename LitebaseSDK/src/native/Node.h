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
 * Declares functions to manipulate a B-Tree. It is used to store the table indices. It has some improvements for both memory usage, disk space, and 
 * speed, targeting the creation of indices, where the table's record is far greater than the index record.
 */

#ifndef LITEBASE_NODE_H
#define LITEBASE_NODE_H

#include "Litebase.h"

/**
 * Creates a new node for an index.
 *
 * @param index The index of the node to be created.
 * @return The node created.
 */
Node* createNode(Index* index);

/**
 * Loads a node.
 * 
 * @param context The thread context where the function is being executed.
 * @param node The node being loaded.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nodeLoad(Context context, Node* node);

/**
 * Saves a dirty key.
 *
 * @param context The thread context where the function is being executed.
 * @param node The node being saved.
 * @param currPos The current position in the file where the key should be saved.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nodeSaveDirtyKey(Context context, Node* node, int32 currPos);

/**
 * Saves a node.
 *
 * @param context The thread context where the function is being executed.
 * @param node The node being saved.
 * @param isNew Indicates if it is a new node, not saved yet.
 * @param left The left child.
 * @param right The right child.
 * @return The position of this node.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the index gets too large.
 */
int32 nodeSave(Context context, Node* node, bool isNew, int32 left, int32 right);

/**
 * Constructs a B-Tree node with at most k keys, initially with one element, item, and two children: left and right.
 *
 * @param node The node being saved.
 * @param key The key to be saved.
 * @param left The left child.
 * @param right The right child.
 */
void nodeSet(Node* node, Key* key, int32 left, int32 right);

/**
 * Returns the index of the leftmost element of this node that is not less than item, using a binary search.
 *
 * @param context The thread context where the function is being executed.
 * @param node The node being searched.
 * @param key The key to be found.
 * @param isInsert Indicates that the function is called by <code>indexInsert()</code>
 * @return The position of the key.
 */
int32 nodeFindIn(Context context, Node* node, Key* key, bool isInsert); 

/**
 * Inserts element item, with left and right children at the right position in this node.
 *
 * @param context The thread context where the function is being executed.
 * @param node The node where a key will be inserted.
 * @param key The key to be saved.
 * @param leftChild The left child of the node.
 * @param rightChild The right child of the node.
 * @param insPos The position where to insert the key.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nodeInsert(Context context, Node* node, Key* key, int32 leftChild, int32 rightChild, int32 insPos);

/**
 * Sets the flag that indicates if the not should have its write process delayed or not.
 *
 * @param context The thread context where the function is being executed.
 * @param node The node whose flag will be updated.
 * @param delayed The new value of the flag.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool nodeSetWriteDelayed(Context context, Node* node, bool delayed);

#endif
