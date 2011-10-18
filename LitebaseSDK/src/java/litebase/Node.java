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

package litebase;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * This is the implementation of a B-Tree. It is used to store the table indices. It has some improvements for both memory usage, disk space, and 
 * speed, targeting the creation of indices, where the table's record is far greater than the index record.
 */
class Node
{
   /**
    * Indicates if a node is a leaf.
    */
   static final int LEAF = -1;

   /**
    * The grow size of the node, which must be a power of 2.
    */
   static final int NODEGROWSIZE = 64;

   /**
    * The maximum number of nodes in an index.
    */ 
   private static final int MAX_IDX = 32767;
   
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
   short[] children;

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

      children = new short[anIndex.btreeMaxNodes + 1]; // Each array has one extra component, to allow for possible overflow.
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
      short[] childrenAux = children;
      
      fnodes.setPos(idx * indexAux.nodeRecSize);
      fnodes.readBytes(indexAux.basbuf, 0, indexAux.nodeRecSize); // Reads all the record at once.

      // Loads the keys.
      DataStreamLE ds = indexAux.basds;
      indexAux.bas.reset();
      length = size = ds.readUnsignedShort();
      while (++i < length)
         keysAux[i].load(ds);

      // Loads the node children.
      i = -1;
      while (++i <= length)
         childrenAux[i] = ds.readShort();

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
      indexAux.basds.writeInt(keys[currPos].valRec);
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
            if ((idxAux & (NODEGROWSIZE - 1)) == 0) // Grows more than 1 record per time.
               fnodes.growTo((idxAux + NODEGROWSIZE) * recSize);
         }
         else
            fnodes.growTo((idxAux + 1) * recSize); // Opens space for the node.
               
      }
      fnodes.setPos(idxAux * recSize); // Rewinds to insert position.
      
      DataStreamLE ds = indexAux.basds;
      ByteArrayStream bas = indexAux.bas;
      Key[] keysAux = keys;
      short[] childrenAux = children;
      
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
         Node node = firstLevel[idxAux - 1] = new Node(indexAux);
         Key[] keys = node.keys;
         
         node.idx = idxAux;
         Vm.arrayCopy(childrenAux, left, node.children, 0, (i = node.size = right - left) + 1);
         while (--i >= 0)
         {
            keys[i].set(keysAux[i + left].keys);
            keys[i].valRec = keysAux[i + left].valRec;
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
      short[] childrenAux = children;
      
      size = 1;
      keysAux[0].set(item.keys);
      keysAux[0].valRec = item.valRec;
      childrenAux[0] = (short)left;
      childrenAux[1] = (short)right;
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
      PlainDB db = indexAux.table.db;
      int[] sizes = indexAux.colSizes;
      byte[] types = indexAux.types;
      Key[] keysAux = keys;
      SQLValue[] idxRec;
      SQLValue[] itemKeys = item.keys;
      SQLValue sqlValue;
      XFile dbo = db.dbo;
      int r = size - 1,
          l = (isInsert && indexAux.isOrdered && r > 0)? r : 0, // juliana@201_3: If the insertion is ordered, the position being seached is the last.
          m,
          i,
          comp;

      while (l <= r)
      {
         i = (idxRec = keysAux[m = (l + r) >> 1].keys).length;
         while (--i >= 0) // A string may not be loaded.
            if ((sqlValue = idxRec[i]).asString == null && sizes[i] > 0)
            {
               dbo.setPos(sqlValue.asInt); // Gets and sets the string position in the .dbo.
               sqlValue.asString = db.loadString();
            }
         if ((comp = Utils.arrayValueCompareTo(itemKeys, idxRec, types)) == 0)
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
      short[] childrenAux = children;
      
      if (l > 0)
      {
         int i = sizeAux + 1;
         while (--i > ins)
         {
            keysAux[i].set(keysAux[i - 1].keys);
            keysAux[i].valRec = keysAux[i - 1].valRec;
         }
         Vm.arrayCopy(childrenAux, ins + 1, childrenAux, ins + 2, l);
      }
      keysAux[ins].set(item.keys);
      keysAux[ins].valRec = item.valRec;
      childrenAux[ins] = (short)leftChild;
      childrenAux[ins + 1] = (short)rightChild;
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
