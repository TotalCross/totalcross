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

// $Id: Node.java,v 1.1.2.29 2011-01-03 20:05:13 juliana Exp $

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
    * Indicates if the write of the node is delayed.
    */
   boolean isWriteDelayed;

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
      keys = new Key[anIndex.btreeMaxNodes];
      int i = anIndex.btreeMaxNodes;
      while (--i >= 0)
         keys[i] = new Key(index = anIndex);

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
      int i = -1;
      index.fnodes.setPos(idx * index.nodeRecSize);
      index.fnodes.readBytes(index.basbuf, 0, index.nodeRecSize); // Reads all the record at once.

      // Loads the keys.
      DataStreamLE ds = index.basds;
      index.bas.reset();
      size = ds.readUnsignedShort();
      while (++i < size)
         keys[i].load(ds);

      // Loads the node children.
      i = -1;
      while (++i <= size)
         children[i] = ds.readShort();

      Convert.fill(children, i + 1, index.btreeMaxNodes + 1, LEAF); // Fills the non-used indexes with TERMINAL.
   }

   /**
    * Saves a dirty key.
    *
    * @param currPos The current position in the file where the key should be saved.
    * @throws IOException If an internal method throws it.
    */
   void saveDirtyKey(int currPos) throws IOException
   {
      // Positions the file pointer at the insert position.
      index.fnodes.setPos(idx * index.nodeRecSize + 2 + index.keyRecSize * currPos + (index.keyRecSize - Key.VALREC_SIZE));

      index.bas.reset();
      index.basds.writeInt(keys[currPos].valRec);
      index.fnodes.writeBytes(index.basbuf, 0, 4);
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
      int i, 
          idx = this.idx,
          recSize = index.nodeRecSize;
      NormalFile fnodes = index.fnodes;

      if (isNew)
      {
         if ((idx = index.nodeCount++) >= MAX_IDX)
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INDEX_LARGE));

         if (index.root.isWriteDelayed)
         {
            if ((idx & (NODEGROWSIZE - 1)) == 0) // Grows more than 1 record per time.
               fnodes.growTo((idx + NODEGROWSIZE) * recSize);
         }
         else
            fnodes.growTo((idx + 1) * recSize); // Opens space for the node.
               
      }
      fnodes.setPos(idx * recSize); // Rewinds to insert position.
      index.bas.reset();
      
      DataStreamLE ds = index.basds;
      ds.writeShort(right - left);

      i = left - 1;
      while (++i < right) // Saves the keys.
         keys[i].save(ds);

      // Saves the children.
      i = left - 1;
      while (++i <= right)
         ds.writeShort(children[i]);

      ds.pad(index.bas.available()); // Fills the rest with zeros.
      fnodes.writeBytes(index.basbuf, 0, index.bas.getPos());
      isDirty = false;
      return idx;
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
      size = 1;
      keys[0].set(item.keys);
      keys[0].valRec = item.valRec;
      children[0] = (short)left;
      children[1] = (short)right;
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
      int r = size - 1,
          l = (isInsert && index.isOrdered && r > 0)? r : 0, // juliana@201_3: If the insertion is ordered, the position being seached is the last.
          m,
          i,
          comp;
      PlainDB db = index.table.db;
      int[] types = index.types;
      Key[] keysAux = keys;
      XFile dbo = db.dbo;
      DataStreamLE dsdbo = db.dsdbo;
      
      while (l <= r)
      {
         i = keysAux[m = (l + r) >> 1].keys.length;
         
         while (--i >= 0) // A string may not be loaded.
            if (keysAux[m].keys[i].asString == null && (types[i] == SQLElement.CHARS || types[i] == SQLElement.CHARS_NOCASE))
            {
               dbo.setPos(keysAux[m].keys[i].asInt); // Gets and sets the string position in the .dbo.
               int length = dsdbo.readUnsignedShort();
               if (db.isAscii) // juliana@210_2: now Litebase supports tables with ascii strings.
               {
                  byte[] buf = db.buffer;
                  if (buf.length < length)
                     db.buffer = buf = new byte[length];
                  dsdbo.readBytes(buf, 0, length);
                  keysAux[m].keys[i].asString = new String(buf, 0, length); // Reads the string.
               }
               else
               {
                  char[] chars = db.valueAsChars;
                  if (chars.length < length)
                     db.valueAsChars = chars = new char[length];
                  dsdbo.readChars(chars, length);            
                  keysAux[m].keys[i].asString = new String(chars, 0, length); // Reads the string.
               }
            }
         if ((comp = Utils.arrayValueCompareTo(item.keys, keysAux[m].keys, index.types)) == 0)
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
      int l = size - ins;
      if (l > 0)
      {
         int i = size + 1;
         while (--i > ins)
         {
            keys[i].set(keys[i - 1].keys);
            keys[i].valRec = keys[i - 1].valRec;
         }
         Vm.arrayCopy(children, ins + 1, children, ins + 2, l);
      }
      keys[ins].set(item.keys);
      keys[ins].valRec = item.valRec;
      children[ins] = (short)leftChild;
      children[ins + 1] = (short)rightChild;
      size++;

      if (isWriteDelayed)  // Only saves the key if it is not to be saved later.
         isDirty = true;
      else
         save(false, 0, size);
   }

   /**
    * This methods allows to climb on the tree, in order. Just implemented the <code>Monkey</code> interface, which will be called each time a key 
    * is found.
    *
    * @param monkey The monkey objects.
    * @param nodes The nodes to be climbed on.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void climb(Monkey monkey, Vector nodes) throws IOException, InvalidDateException
   {
      Node curr = null;
      int i = size;
      if (children[0] == LEAF)
      {
         while (--i >= 0)
            monkey.onKey(keys[i]);
      }
      else
      {
         try
         {
            curr = (Node)nodes.pop();
         }
         catch (ElementNotFoundException exception)
         {
            curr = new Node(index);
         }
         curr.idx = children[i = size];
         curr.load();
         curr.climb(monkey, nodes);
         while (--i >= 0)
         {
            curr.idx = children[i];
            curr.load();
            curr.climb(monkey, nodes);
            monkey.onKey(keys[i]); // There is always one extra node pointer per node.    
         }
         nodes.push(curr);
      }
   }

   /**
    * Sets the flag that indicates if the not should have its write process delayed or not.
    *
    * @param delayed The new value of the flag.
    * @throws IOException If an internal method throws it.
    */
   void setWriteDelayed(boolean delayed) throws IOException
   {
      if (isWriteDelayed && isDirty) // Before changing the flag, flushs the node.
         save(false, 0, size);
      isWriteDelayed = delayed;
   }
}
