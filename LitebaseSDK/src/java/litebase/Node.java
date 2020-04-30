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

package litebase;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.*;

// juliana@253_5: removed .idr files from all indices and changed its format. 
// juliana@253_6: the maximum number of keys of a index was duplicated. 
/**
 * This is the implementation of a B-Tree. It is used to store the table indices. It has some improvements for both memory usage, disk space, and 
 * speed, targeting the creation of indices, where the table's record is far greater than the index record.
 */
class Node
{
   /**
    * Indicates if a node is a leaf.
    */
   static final int LEAF = 0xFFFF;

   /**
    * The grow size of the node, which must be a power of 2.
    */
   static final int NODEGROWSIZE = 64;

   /**
    * The maximum number of nodes in an index.
    */ 
   static final int MAX_IDX = 65534; // juliana@253_6: The maximum number of keys of a index was duplicated. 
   
   /**
    * The size of the node.
    */
   int size;

   /**
    * The index of a node in the B-Tree.
    */
   int idx = -1;

   /**
    * The keys that this node stores.
    */
   Key[] keys;

   /**
    * This children nodes.
    */
   int [] children;

   /**
    * The index of this node.
    */
   Index index;

   /**
    * Indicates if a node is dirty.
    */
   boolean isDirty;

   /**
    * Creates a new node for an index.
    *
    * @param anIndex The index of the node to be created.
    */
   Node(Index anIndex)
   {
      // Creates this node keys.
      Key[] keysAux = keys = new Key[anIndex.btreeMaxNodes];
      int i = anIndex.btreeMaxNodes;
      while (--i >= 0)
         keysAux[i] = new Key(index = anIndex);

      children = new int[anIndex.btreeMaxNodes + 1]; // Each array has one extra component, to allow for possible overflow.
   }

   /**
    * Loads a node.
    * 
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void load() throws IOException, InvalidDateException
   {
      int i = -1,
          length;
      Index indexAux = index;
      XFile fnodes = indexAux.fnodes;
      Key[] keysAux = keys;
      int[] childrenAux = children;
      
      fnodes.setPos(idx * indexAux.nodeRecSize);
      fnodes.readBytes(indexAux.basbuf, 0, indexAux.nodeRecSize); // Reads all the record at once.

      // Loads the keys.
      DataStreamLB ds = indexAux.basds; // juliana@253_8: now Litebase supports weak cryptography.
      indexAux.bas.reset();
      length = size = ds.readUnsignedShort();
      while (++i < length)
         keysAux[i].load(ds);

      // Loads the node children.
      i = -1;
      while (++i <= length)
         childrenAux[i] = ds.readUnsignedShort();

      Convert.fill(childrenAux, i + 1, indexAux.btreeMaxNodes + 1, LEAF); // Fills the non-used indexes with TERMINAL.
   }

   /**
    * Saves a dirty key.
    *
    * @param currPos The current position in the file where the key should be saved.
    * @throws IOException If an internal method throws it.
    */
   void saveDirtyKey(int currPos) throws IOException
   {
      Index indexAux = index;
      
      // Positions the file pointer at the insert position.
      indexAux.fnodes.setPos(idx * indexAux.nodeRecSize + 2 + indexAux.keyRecSize * currPos + (indexAux.keyRecSize - Key.VALREC_SIZE));

      indexAux.bas.reset();
      indexAux.basds.writeInt(keys[currPos].record);
      indexAux.fnodes.writeBytes(indexAux.basbuf, 0, 4);
   }

   /**
    * Saves a node.
    *
    * @param isNew Indicates if it is a new node, not saved yet.
    * @param left The left child.
    * @param right The right child.
    * @return The position of this node.
    * @throws DriverException If the index gets too large.
    * @throws IOException If an internal method throws it.
    */
   int save(boolean isNew, int left, int right) throws IOException
   {
      Index indexAux = index;
      int i, 
          idxAux = idx,
          recSize = indexAux.nodeRecSize;
      NormalFile fnodes = indexAux.fnodes;

      if (isNew)
      {
         if ((idxAux = indexAux.nodeCount++) >= MAX_IDX)
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INDEX_LARGE));

         if (indexAux.isWriteDelayed)
         {
            if (idxAux * recSize == fnodes.size) // Grows more than 1 record per time.
               fnodes.growTo((idxAux + NODEGROWSIZE) * recSize);
         }
         else
            fnodes.growTo((idxAux + 1) * recSize); // Opens space for the node.
               
      }
      fnodes.setPos(idxAux * recSize); // Rewinds to insert position.
      
      DataStreamLB ds = indexAux.basds; // juliana@253_8: now Litebase supports weak cryptography.
      ByteArrayStream bas = indexAux.bas;
      Key[] keysAux = keys;
      int[] childrenAux = children;
      
      bas.reset();
      ds.writeShort(right - left);

      i = left - 1;
      while (++i < right) // Saves the keys.
         keysAux[i].save(ds);

      // Saves the children.
      i = left - 1;
      while (++i <= right)
         ds.writeShort(childrenAux[i]);
      
      // juliana@230_35: now the first level nodes of a b-tree index will be loaded in memory.
      if (isNew && idxAux > 0 && idxAux <= indexAux.btreeMaxNodes)
      {
         Node[] firstLevel = indexAux.firstLevel;
         Node node = firstLevel[idxAux - 1];
                 
         if (node == null)
            node = firstLevel[idxAux - 1] = new Node(indexAux);
            
         Key[] keys = node.keys;
         node.idx = idxAux;
         Vm.arrayCopy(childrenAux, left, node.children, 0, (i = node.size = right - left) + 1);
         while (--i >= 0)
         {
            keys[i].set(keysAux[i + left].keys);
            keys[i].record = keysAux[i + left].record;
         }
         node.isDirty = false;
      }

      ds.pad(bas.available()); // Fills the rest with zeros.
      fnodes.writeBytes(indexAux.basbuf, 0, bas.getPos());
      isDirty = false;
      return idxAux;
   }

   /**
    * Constructs a B-Tree node with at most k keys, initially with one element, item, and two children: left and right.
    *
    * @param item The key to be saved.
    * @param left The left child.
    * @param right The right child.
    */
   void set(Key item, int left, int right)
   {
      Key[] keysAux = keys;
      int[] childrenAux = children;
      
      size = 1;
      keysAux[0].set(item.keys);
      keysAux[0].record = item.record;
      childrenAux[0] = left;
      childrenAux[1] = right;
   }

   /**
    * Returns the index of the leftmost element of this node that is not less than item, using a binary search.
    *
    * @param item The key to be found.
    * @param isInsert Indicates if the method is called by <code>Index.insert()</code>
    * @return The position of the key.
    * @throws IOException If an internal method throws it.
    */
   int findIn(Key item, boolean isInsert) throws IOException
   {
      Index indexAux = index;
      PlainDB plainDB = indexAux.table.db;
      Key[] keysAux = keys;
      SQLValue[] itemKeys = item.keys;
      byte[] types = indexAux.types;
      int r = size - 1,
          l = (isInsert && indexAux.isOrdered && r > 0)? r : 0, // juliana@201_3: If the insertion is ordered, the position being seached is the last.
          m,
          comp;

      while (l <= r)
      {        
         if ((comp = Utils.arrayValueCompareTo(itemKeys, keysAux[m = (l + r) >> 1].keys, types, plainDB)) == 0)
            return m;
         else
         if (comp < 0)
            r = m - 1;
         else
            l = m + 1;
      }
      return l;
   }

   /**
    * Inserts element item, with left and right children at the right position in this node.
    *
    * @param item The key to be saved.
    * @param leftChild The left child of the node.
    * @param rightChild The right child of the node.
    * @param ins The position where to insert the key.
    * @throws IOException If an internal method throws it.
    */
   void insert(Key item, int leftChild, int rightChild, int ins) throws IOException
   {
      int sizeAux = size,
          l = sizeAux - ins;
      Key[] keysAux = keys;
      int[] childrenAux = children;
      
      if (l > 0)
      {
         int i = sizeAux + 1;
         while (--i > ins)
         {
            keysAux[i].set(keysAux[i - 1].keys);
            keysAux[i].record = keysAux[i - 1].record;
         }
         Vm.arrayCopy(childrenAux, ins + 1, childrenAux, ins + 2, l);
      }
      keysAux[ins].set(item.keys);
      keysAux[ins].record = item.record;
      childrenAux[ins] = leftChild;
      childrenAux[ins + 1] = rightChild;
      sizeAux = ++size;

      if (index.isWriteDelayed)  // Only saves the key if it is not to be saved later.
         isDirty = true;
      else
         save(false, 0, sizeAux);
   }

   /**
    * Sets the flag that indicates if the not should have its write process delayed or not.
    *
    * @param delayed The new value of the flag.
    * @throws IOException If an internal method throws it.
    */
   void setWriteDelayed(boolean delayed) throws IOException
   {
      if (index.isWriteDelayed && isDirty && !delayed) // Before changing the flag, flushs the node.
         save(false, 0, size);
   }
}
